package org.varnerlab.kwatee.nfbamodel;

import org.varnerlab.kwatee.nfbamodel.model.VLCGNFBABiochemistryReactionModel;
import org.varnerlab.kwatee.nfbamodel.model.VLCGNFBASpeciesModel;
import org.varnerlab.kwatee.nfbamodel.model.VLCGSimpleControlLogicModel;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.util.ArrayList;
import java.util.Vector;
import java.util.concurrent.ExecutionException;

/**
 * Copyright (c) 2015 Varnerlab,
 * School of Chemical Engineering,
 * Purdue University, West Lafayette IN 46077 USA.
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * <p>
 * Created by jeffreyvarner on 11/20/15.
 */
public class VLCGNFBAModelTreeWrapper {

    // instance variables -
    private Document _model_tree = null;
    private XPathFactory _xpath_factory = XPathFactory.newInstance();
    private XPath _xpath = _xpath_factory.newXPath();

    // constructor -
    public VLCGNFBAModelTreeWrapper(Document document){

        // grab the document -
        _model_tree = document;
    }

    // ======================================================================== //
    // define a set of helper methods which are called by the model delegate to
    // to extract data from the AST -
    // ======================================================================== //
    public int calculateTheTotalNumberOfControlTerms() throws Exception {

        // method variables -
        int number_of_control_terms = 0;

        String xpath_string = ".//control/@name";
        NodeList nodeList = _lookupPropertyCollectionFromTreeUsingXPath(xpath_string);
        number_of_control_terms = nodeList.getLength();

        // return -
        return number_of_control_terms;
    }

    public String buildReactionCommentStringForReactionWithName(String reaction_name) throws Exception {

        // Method variables -
        String comment_string = "";

        // Xpath -
        String xpath_string = "//*[@name=\""+reaction_name+"\"]/@formatted_record";
        System.out.println(xpath_string);
        NodeList node_list = _lookupPropertyCollectionFromTreeUsingXPath(xpath_string);

        // We should have *only* a single node -
        if (node_list.getLength()!=1){
            throw new Exception("Reaction names must be unique. There appear to be multiple reactions named "+reaction_name);
        }

        Node reaction_node = node_list.item(0);
        comment_string = reaction_node.getNodeValue();

        // return -
        return comment_string;
    }

    public String buildControlCommentStringForControlConnectionWithName(String reaction_name) throws Exception {

        // Method variables -
        String comment_string = "";

        // Xpath -
        String xpath_string = "//control[@name=\""+reaction_name+"\"]/@raw_control_string";
        NodeList node_list = _lookupPropertyCollectionFromTreeUsingXPath(xpath_string);

        // We should have *only* a single node -
        if (node_list.getLength()!=1){
            throw new Exception("Reaction names must be unique. There appear to be multiple reactions named "+reaction_name);
        }

        Node reaction_node = node_list.item(0);
        comment_string = reaction_node.getNodeValue();

        // return -
        return comment_string;
    }

    public ArrayList<VLCGSimpleControlLogicModel> getControlModelListFromModelTreeForReactionWithName(String reaction_name) throws Exception {

        // method variables -
        ArrayList<VLCGSimpleControlLogicModel> control_vector = new ArrayList<VLCGSimpleControlLogicModel>();

        // ok, check - do we have a control statement with this reaction name as the target?
        String xpath_string = ".//control[@target=\""+reaction_name+"\"]";
        NodeList node_list = _lookupPropertyCollectionFromTreeUsingXPath(xpath_string);
        int number_of_transfer_functions = node_list.getLength();
        for (int transfer_function_index = 0;transfer_function_index<number_of_transfer_functions;transfer_function_index++){

            // Get the node -
            Node control_node = node_list.item(transfer_function_index);

            // Get the data from this node -
            NamedNodeMap attribute_map = control_node.getAttributes();
            Node type_node = attribute_map.getNamedItem("type");
            Node actor_node = attribute_map.getNamedItem("actor");
            Node name_node = attribute_map.getNamedItem("name");

            // Create a comment -
            String comment = name_node.getNodeValue()+" target: "+reaction_name+" actor: "+actor_node.getNodeValue()+" type: "+type_node.getNodeValue();

            // Build the wrapper -
            VLCGSimpleControlLogicModel transfer_function_model = new VLCGSimpleControlLogicModel();
            transfer_function_model.setModelComponent(VLCGSimpleControlLogicModel.CONTROL_ACTOR,actor_node.getNodeValue());
            transfer_function_model.setModelComponent(VLCGSimpleControlLogicModel.CONTROL_TARGET,reaction_name);
            transfer_function_model.setModelComponent(VLCGSimpleControlLogicModel.CONTROL_TYPE,type_node.getNodeValue());
            transfer_function_model.setModelComponent(VLCGSimpleControlLogicModel.CONTROL_NAME,name_node.getNodeValue());
            transfer_function_model.setModelComponent(VLCGSimpleControlLogicModel.CONTROL_COMMENT,comment);

            // add to the vector -
            control_vector.add(transfer_function_model);
        }

        return control_vector;
    }
    public boolean isThisATranslationReaction(String reaction_name) throws Exception {

        // method variables -
        boolean return_flag = false;

        // ok, check - do we have a control statement with this reaction name as the target?
        String xpath_string = ".//reaction[@name=\""+reaction_name+"\"]/@type";
        String reaction_type = _lookupPropertyValueFromTreeUsingXPath(xpath_string);

        if (reaction_type.equalsIgnoreCase("TRANSLATION")){
            return_flag = true;
        }

        // return -
        return return_flag;
    }

    public boolean isThisReactionRegulated(String reaction_name) throws Exception {

        // method variables -
        boolean return_flag = false;

        // ok, check - do we have a control statement with this reaction name as the target?
        String xpath_string = ".//control[@target=\""+reaction_name+"\"]";
        NodeList node_list = _lookupPropertyCollectionFromTreeUsingXPath(xpath_string);

        if (node_list.getLength()>0){
            return_flag = true;
        }

        // return -
        return return_flag;
    }

    public ArrayList<String> getListOfReactionNamesFromModelTree() throws Exception {

        // method variables -
        ArrayList<String> name_vector = new ArrayList<String>();

        // Get reaction names -
        String xpath_string = ".//reaction/@name";
        NodeList node_list = _lookupPropertyCollectionFromTreeUsingXPath(xpath_string);

        // ok, so we need to grab the node values, and return the string symbols
        int number_of_nodes = node_list.getLength();
        for (int node_index = 0; node_index < number_of_nodes; node_index++) {

            // Grab the node value -
            String node_value = node_list.item(node_index).getNodeValue();

            if (name_vector.contains(node_value) == false) {
                name_vector.add(node_value);
            }
        }

        return name_vector;
    }

    // what index is the species with symbol?
    public int lookupIndexForSpeciesWithSymbol(String species_symbol) throws Exception {

        // method variables -
        int species_index = -1;

        // get a list of species symbols -
        ArrayList<VLCGNFBASpeciesModel> species_model_list = this.getListOfSpeciesModelsFromModelTree();
        ArrayList<String> symbol_array = new ArrayList<String>();
        for (VLCGNFBASpeciesModel species_model : species_model_list){

            // Get the symbol -
            String local_symbol = (String)species_model.getModelComponent(VLCGNFBASpeciesModel.SPECIES_SYMBOL);
            symbol_array.add(local_symbol);
        }

        // lookup -
        species_index = symbol_array.indexOf(species_symbol);

        // return -
        return species_index;
    }

    // Get the array of reaction orders for this reaction -
    public String getGammaArrayForReactionWithName(String reaction_name) throws Exception {

        // method variables -
        StringBuilder array_buffer = new StringBuilder();

        // how many species do we have?
        int number_of_species = this.calculateTheTotalNumberOfModelSpecies();

        System.out.println("How many species - "+number_of_species);

        // Build a tmp array of zeros -
        String[] tmp_array = new String[number_of_species];
        for (int col_index = 0;col_index<number_of_species;col_index++){
            tmp_array[col_index] = "0.0";
        }

        // Get the reactant symbols for this reaction -
        String xpath_reactants = ".//reaction[@name=\""+reaction_name+"\"]/listOfReactants/speciesReference/@species";
        NodeList reactant_node_list = _lookupPropertyCollectionFromTreeUsingXPath(xpath_reactants);
        int number_of_reactants = reactant_node_list.getLength();
        for (int reactant_index = 0;reactant_index<number_of_reactants;reactant_index++){

            // get reactant node value -
            String reactant_symbol = reactant_node_list.item(reactant_index).getNodeValue();

            // ok, what index is this symbol?
            int species_index = this.lookupIndexForSpeciesWithSymbol(reactant_symbol);
            if (species_index != -1){

                tmp_array[species_index] = "1.0";
            }
        }

        // The gamma array will also depend the enzyme level -
        String xpath_enzyme = ".//reaction[@name=\""+reaction_name+"\"]/@enzyme_symbol";
        String enzyme_symbol = _lookupPropertyValueFromTreeUsingXPath(xpath_enzyme);
        if (enzyme_symbol.equalsIgnoreCase("[]") == false){

            // ok, what index is this symbol?
            int species_index = this.lookupIndexForSpeciesWithSymbol(enzyme_symbol);
            if (species_index != -1){

                tmp_array[species_index] = "1.0";
            }
        }

        // populate the array_buffer -
        int col_index = 0;
        for (String element_value : tmp_array){

            array_buffer.append(element_value);

            if (col_index<number_of_species - 1){
                array_buffer.append(" ");
            }

            col_index++;
        }

        System.out.println("array buffer - "+array_buffer.toString());

        // return -
        return array_buffer.toString();
    }

    // get the dimensions of the system -
    public String getStoichiometricCoefficientsForSpeciesInModel(String species_symbol) throws Exception {

        // method variables -
        StringBuilder row_buffer = new StringBuilder();

        // how many *total* reactions do we have?
        int number_of_reactions = this.calculateTheTotalNumberOfReactionTerms();

        // Build a tmp array of zeros -
        String[] tmp_array = new String[number_of_reactions];
        for (int col_index = 0;col_index<number_of_reactions;col_index++){
            tmp_array[col_index] = "0.0";
        }

        if (species_symbol.contains("gene_")){
            int col_index = 0;
            for (String element_value : tmp_array){
                row_buffer.append(element_value);

                if (col_index<number_of_reactions - 1){
                    row_buffer.append(" ");
                }

                col_index++;
            }

            // add a new-line -
            row_buffer.append("\n");

            return row_buffer.toString();
        }

        // Lookup reaction names that this species is a *reactant* in -
        String reaction_name_xpath = ".//listOfReactions/reaction/listOfReactants/speciesReference[@species=\""+species_symbol+"\"]/../../@name";
        NodeList reaction_name_list = _lookupPropertyCollectionFromTreeUsingXPath(reaction_name_xpath);
        int number_of_local_reactions = reaction_name_list.getLength();
        for (int local_reaction_index = 0;local_reaction_index<number_of_local_reactions;local_reaction_index++) {

            // Get name value -
            String local_reaction_name = reaction_name_list.item(local_reaction_index).getNodeValue();

            // is this a translation reaction?
            if (isThisATranslationReaction(local_reaction_name) == false) {

                // Get the reaction index -
                String xpath_reaction_index = ".//listOfReactions/reaction[@name=\"" + local_reaction_name + "\"]/@index";
                String reaction_index = _lookupPropertyValueFromTreeUsingXPath(xpath_reaction_index);

                // Get stoichiometric coefficient -
                String xpath_stochiometric_coefficient = ".//listOfReactions/reaction[@name=\"" + local_reaction_name + "\"]/listOfReactants/speciesReference[@species=\"" + species_symbol + "\"]/@stoichiometry";
                String stochiometric_coefficient = _lookupPropertyValueFromTreeUsingXPath(xpath_stochiometric_coefficient);

                // update the tmp_array -
                tmp_array[Integer.parseInt(reaction_index) - 1] = "-" + stochiometric_coefficient;
            }
        }

        // Lookup reactions names that this species is a *product* in -
        reaction_name_xpath = ".//listOfReactions/reaction/listOfProducts/speciesReference[@species=\""+species_symbol+"\"]/../../@name";
        reaction_name_list = _lookupPropertyCollectionFromTreeUsingXPath(reaction_name_xpath);
        number_of_local_reactions = reaction_name_list.getLength();
        for (int local_reaction_index = 0;local_reaction_index<number_of_local_reactions;local_reaction_index++) {

            // Get name value -
            String local_reaction_name = reaction_name_list.item(local_reaction_index).getNodeValue();

            // Get the reaction index -
            String xpath_reaction_index = ".//listOfReactions/reaction[@name=\""+local_reaction_name+"\"]/@index";
            String reaction_index = _lookupPropertyValueFromTreeUsingXPath(xpath_reaction_index);

            // Get stoichiometric coefficient -
            String xpath_stochiometric_coefficient = ".//listOfReactions/reaction[@name=\""+local_reaction_name+"\"]/listOfProducts/speciesReference[@species=\""+species_symbol+"\"]/@stoichiometry";
            String stochiometric_coefficient = _lookupPropertyValueFromTreeUsingXPath(xpath_stochiometric_coefficient);

            // update the tmp_array -
            tmp_array[Integer.parseInt(reaction_index) - 1] = stochiometric_coefficient;
        }

        // populate the row_buffer -
        int col_index = 0;
        for (String element_value : tmp_array){
            row_buffer.append(element_value);

            if (col_index<number_of_reactions - 1){
                row_buffer.append(" ");
            }

            col_index++;
        }

        // add a new-line -
        row_buffer.append("\n");

        // return -
        return row_buffer.toString();
    }

    // How many species do we have?
    public int calculateTheTotalNumberOfModelSpecies() throws Exception {

        // get reaction list-
        ArrayList<VLCGNFBASpeciesModel> species_model_array = this.getListOfSpeciesModelsFromModelTree();
        return species_model_array.size();
    }

    // how many reactions do we have?
    public int calculateTheTotalNumberOfReactionTerms() throws Exception {

        // get reaction list-
        ArrayList<VLCGNFBABiochemistryReactionModel> reaction_model_array = this.getListOfBiochemicalReactionModelsFromModelTree();
        return reaction_model_array.size();
    }

    // get list of reaction models from tree -
    public ArrayList<VLCGNFBABiochemistryReactionModel> getListOfBiochemicalReactionModelsFromModelTree() throws Exception {

        // Use xpath to lookup the reaction models, construct array and return -
        ArrayList<VLCGNFBABiochemistryReactionModel> reaction_model_array = new ArrayList<VLCGNFBABiochemistryReactionModel>();

        // Get the reaction models -
        String xpath_reactions = ".//listOfReactions/reaction";
        NodeList reaction_nodes = _lookupPropertyCollectionFromTreeUsingXPath(xpath_reactions);
        int number_of_nodes = reaction_nodes.getLength();
        for (int node_index = 0; node_index<number_of_nodes; node_index++){

            // Get the attributes -
            Node reaction_node = reaction_nodes.item(node_index);
            NamedNodeMap attributes = reaction_node.getAttributes();

            // Lookup data for this reaction -
            String reaction_name = attributes.getNamedItem("name").getNodeValue();
            String formatted_record = attributes.getNamedItem("formatted_record").getNodeValue();

            // Build reaction wrapper -
            VLCGNFBABiochemistryReactionModel reaction_model = new VLCGNFBABiochemistryReactionModel();
            reaction_model.setModelComponent(VLCGNFBABiochemistryReactionModel.REACTION_NAME,reaction_name);
            reaction_model.setModelComponent(VLCGNFBABiochemistryReactionModel.FORMATTED_RAW_RECORD,formatted_record);

            // add to the array, go around again -
            reaction_model_array.add(reaction_model);
        }

        return reaction_model_array;
    }

    // Get the species models from tree -
    public ArrayList<VLCGNFBASpeciesModel> getListOfSpeciesModelsFromModelTree() throws Exception {

        // Use xpath to lookup the species models, construct array and return -
        ArrayList<VLCGNFBASpeciesModel> species_model_array = new ArrayList<VLCGNFBASpeciesModel>();

        // Formulate the xpath -
        String xpath_query = ".//species";
        NodeList species_nodes = _lookupPropertyCollectionFromTreeUsingXPath(xpath_query);
        int number_of_nodes = species_nodes.getLength();
        for (int node_index = 0; node_index<number_of_nodes; node_index++){

            // Get the attributes -
            Node species_node = species_nodes.item(node_index);
            NamedNodeMap attributes = species_node.getAttributes();

            // Lookup the data for this node -
            String species_symbol = attributes.getNamedItem("symbol").getNodeValue();
            String balanced = attributes.getNamedItem("is_species_balanced").getNodeValue();

            // Create species model -
            VLCGNFBASpeciesModel species_model = new VLCGNFBASpeciesModel();
            species_model.setModelComponent(VLCGNFBASpeciesModel.SPECIES_SYMBOL,species_symbol);
            species_model.setModelComponent(VLCGNFBASpeciesModel.SPECIES_BALANCED_FLAG,balanced);

            // add to the array -
            species_model_array.add(species_model);
        }

        return species_model_array;
    }

    private NodeList _lookupPropertyCollectionFromTreeUsingXPath(String xpath_string) throws Exception {

        if (xpath_string == null) {
            throw new Exception("Null xpath in property lookup call.");
        }

        // Exceute the xpath -
        NodeList node_list = null;
        try {

            node_list = (NodeList) _xpath.evaluate(xpath_string, _model_tree, XPathConstants.NODESET);

        }
        catch (Exception error) {
            error.printStackTrace();
            System.out.println("ERROR: Property lookup failed. The following XPath "+xpath_string+" resulted in an error - "+error.toString());
        }

        // return -
        return node_list;
    }

    /**
     * Return the string value obtained from executing the XPath query passed in as an argument
     * @param xpath_string
     * @return String - get property from uxml tree by executing string in strXPath
     */
    private String _lookupPropertyValueFromTreeUsingXPath(String xpath_string) throws Exception {

        if (xpath_string == null)
        {
            throw new Exception("ERROR: Null xpath in property lookup call.");
        }

        // Method attributes -
        String property_string = null;

        try {
            Node propNode = (Node) _xpath.evaluate(xpath_string, _model_tree, XPathConstants.NODE);
            if (propNode != null){

                property_string = propNode.getNodeValue();
            }
            else {

                property_string = null;
            }
        }
        catch (Exception error)
        {
            error.printStackTrace();
            System.out.println("ERROR: Property lookup failed. The following XPath "+xpath_string+" resulted in an error - "+error.toString());
        }

        return property_string;
    }

}
