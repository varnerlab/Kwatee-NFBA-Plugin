package org.varnerlab.kwatee.nfbamodel;

import org.varnerlab.kwatee.foundation.VLCGInputHandler;
import org.varnerlab.kwatee.foundation.VLCGTransformationPropertyTree;
import org.varnerlab.kwatee.nfbamodel.model.*;
import org.varnerlab.kwatee.nfbamodel.parserdelegate.*;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.StringJoiner;
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
public class VLCGNFBAParseVarnerNFBAFlatFile implements VLCGInputHandler {

    // Instance variables -
    private VLCGTransformationPropertyTree _transformation_properties_tree = null;
    private final String _package_name_parser_delegate = "org.varnerlab.kwatee.nfbamodel.parserdelegate";
    private Hashtable<Class,Vector<VLCGNFBAModelComponent>> _model_component_table = new Hashtable();

    @Override
    public void setPropertiesTree(VLCGTransformationPropertyTree properties_tree) {

        if (properties_tree == null){
            return;
        }

        _transformation_properties_tree = properties_tree;
    }

    @Override
    public void loadResource(Object o) throws Exception {

        // Where is the file that I need to load?
        String resource_file_path = _transformation_properties_tree.lookupKwateeNetworkFilePath();
        if (resource_file_path != null){

            // ok, we have what appears to be a path, read the PBPK file at this location -
            // this will populate the _model_component_table -
            _readModelFlatFile(resource_file_path);
        }
        else {
            throw new Exception("Missing resource file path. Can't find the NFBA description to parse?");
        }
    }

    @Override
    public Object getResource(Object o) throws Exception {


        // ok, here we go ... we need to turn the component models into an AST
        // then write the AST to disk in the debug folder -

        // Method variables -
        StringBuilder xml_buffer = new StringBuilder();
        DocumentBuilder document_builder = null;
        Document model_tree = null;

        // Generate sections and add them to the tree -
        xml_buffer.append("<?xml version=\"1.0\" encoding=\"utf-8\" standalone=\"yes\"?>\n");
        xml_buffer.append("<NFNAModel>\n");

        // Build list of species -
        String global_list_of_species = _buildGlobalListOfSpeciesFromModelTree();
        xml_buffer.append("<listOfSpecies>\n");
        xml_buffer.append(global_list_of_species);
        xml_buffer.append("</listOfSpecies>\n");

        // get number of each type of reactions -
        int number_of_biochemical_reactions = numberOfBiochemicalReactionsInModelTree();
        int number_of_translation_reactions = numberOfTranslationReactionsInModelTree();
        int number_of_expression_reactions = numberOfGeneExpressionReactionsInModelTree();

        // Build list of reactions -
        String global_list_of_gene_expression_reactions = _buildGlobalListOfGeneExpressionReactionsFromModelTree(0);
        String global_list_of_translation_reactions = _buildGlobalListOfTranslationReactionsFromModelTree(number_of_expression_reactions);
        String global_list_of_biochemical_reactions = _buildGlobalListOfBiochemicalReactionsFromModelTree((number_of_expression_reactions+number_of_translation_reactions));
        xml_buffer.append("<listOfReactions>\n");
        xml_buffer.append(global_list_of_gene_expression_reactions);
        xml_buffer.append(global_list_of_translation_reactions);
        xml_buffer.append(global_list_of_biochemical_reactions);
        xml_buffer.append("</listOfReactions>\n");

        // build list of biochemical control terms -
        String biochemical_control_terms = _buildGlobalListOfBiochemicalControlTermsFromModelTree();
        xml_buffer.append("<listOfBiochemicalControlTerms>\n");
        xml_buffer.append(biochemical_control_terms);
        xml_buffer.append("</listOfBiochemicalControlTerms>\n");

        // build list of gene expression control terms -
        String gene_expression_control_terms = _buildGlobalListOfGeneExpressionControlTermsFromModelTree();
        xml_buffer.append("<listOfGeneExpressionControlTerms>\n");
        xml_buffer.append(gene_expression_control_terms);
        xml_buffer.append("</listOfGeneExpressionControlTerms>\n");

        // close -
        xml_buffer.append("</NFNAModel>\n");

        // Convert the string buffer into an XML Document object -
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        document_builder = factory.newDocumentBuilder();
        model_tree = document_builder.parse(new InputSource(new StringReader(xml_buffer.toString())));

        // write the tree to the debug folder -
        // Get the debug path -
        String debug_path = _transformation_properties_tree.lookupKwateeDebugPath();
        if (debug_path != null){

            // ok, we have a path - is this path legit?
            File oFile = new File(debug_path);
            if (oFile.isDirectory()){

                // Create new path -
                String fully_qualified_model_path = debug_path+"NFBA_AST.xml";

                // Write the AST file -
                File ast_file = new File(fully_qualified_model_path);
                BufferedWriter writer = new BufferedWriter(new FileWriter(ast_file));

                // Write buffer to file system and close writer
                writer.write(xml_buffer.toString());
                writer.close();
            }
        }

        // return the wrapped model_tree -
        VLCGNFBAModelTreeWrapper model_wrapper = new VLCGNFBAModelTreeWrapper(model_tree);
        return model_wrapper;
    }

    // private helper methods -
    private int numberOfBiochemicalReactionsInModelTree() throws Exception {

        int number_of_records = 0;

        String class_name_key = _package_name_parser_delegate + ".VLCGFBABiochemistryReactionParserDelegate";
        Vector<VLCGNFBAModelComponent> biochemistry_component_vector = _model_component_table.get(Class.forName(class_name_key));
        number_of_records = biochemistry_component_vector.size();

        return number_of_records;
    }

    private int numberOfGeneExpressionReactionsInModelTree() throws Exception {

        int number_of_records = 0;

        String gene_expression_class_name_key = _package_name_parser_delegate + ".VLCGNFBAGeneExpressionParserDelegate";
        Vector<VLCGNFBAModelComponent> gene_expression_component_vector = _model_component_table.get(Class.forName(gene_expression_class_name_key));
        number_of_records = gene_expression_component_vector.size();

        return number_of_records;
    }

    private int numberOfTranslationReactionsInModelTree() throws Exception {

        int number_of_records = 0;

        String translation_class_name_key = _package_name_parser_delegate + ".VLCGNFBATranslationParserDelegate";
        Vector<VLCGNFBAModelComponent> translation_component_vector = _model_component_table.get(Class.forName(translation_class_name_key));
        number_of_records = translation_component_vector.size();

        return number_of_records;
    }

    private String _buildGlobalListOfBiochemicalControlTermsFromModelTree() throws Exception {

        // Method variables -
        StringBuilder buffer = new StringBuilder();
        int counter = 1;

        String translation_class_name_key = _package_name_parser_delegate + ".VLCGNFBABiochemistryControlParserDelegate";
        Vector<VLCGNFBAModelComponent> model_component_vector = _model_component_table.get(Class.forName(translation_class_name_key));
        for (VLCGNFBAModelComponent model_component: model_component_vector){

            // Cast -
            VLCGNFBABiochemistryControlModel control_model = (VLCGNFBABiochemistryControlModel)model_component;

            // Get the data from the model -
            String name = (String)control_model.getModelComponent(VLCGNFBABiochemistryControlModel.BIOCHEMISTRY_CONTROL_NAME);
            String actor = (String)control_model.getModelComponent(VLCGNFBABiochemistryControlModel.BIOCHEMISTRY_CONTROL_ACTOR);
            String target = (String)control_model.getModelComponent(VLCGNFBABiochemistryControlModel.BIOCHEMISTRY_CONTROL_TARGET);
            String type = (String)control_model.getModelComponent(VLCGNFBABiochemistryControlModel.BIOCHEMISTRY_CONTROL_TYPE);
            String raw_record = (String)control_model.getModelComponent(VLCGNFBABiochemistryControlModel.FORMATTED_RAW_RECORD);

            // write -
            buffer.append("\t<control index=\"");
            buffer.append(counter++);
            buffer.append("\" name=\"");
            buffer.append(name);
            buffer.append("\" actor=\"");
            buffer.append(actor);
            buffer.append("\" target=\"");
            buffer.append(target);
            buffer.append("\" type=\"");
            buffer.append(type);
            buffer.append("\" raw_control_string=\"");
            buffer.append(raw_record);
            buffer.append("\"/>\n");
        }

        // return -
        return buffer.toString();
    }

    private String _buildGlobalListOfGeneExpressionControlTermsFromModelTree() throws Exception {

        // Method variables -
        StringBuilder buffer = new StringBuilder();
        int counter = 1;

        String translation_class_name_key = _package_name_parser_delegate + ".VLCGNFBAGeneExpressionControlParserDelegate";
        Vector<VLCGNFBAModelComponent> model_component_vector = _model_component_table.get(Class.forName(translation_class_name_key));
        for (VLCGNFBAModelComponent model_component: model_component_vector){

            // Cast -
            VLCGNFBAGeneExpressionControlModel control_model = (VLCGNFBAGeneExpressionControlModel)model_component;

            // Get the data from the model -
            String name = (String)control_model.getModelComponent(VLCGNFBAGeneExpressionControlModel.GENE_EXPRESSION_CONTROL_NAME);
            String actor = (String)control_model.getModelComponent(VLCGNFBAGeneExpressionControlModel.GENE_EXPRESSION_CONTROL_ACTOR);
            String target = (String)control_model.getModelComponent(VLCGNFBAGeneExpressionControlModel.GENE_EXPRESSION_CONTROL_TARGET);
            String type = (String)control_model.getModelComponent(VLCGNFBAGeneExpressionControlModel.GENE_EXPRESSION_CONTROL_TYPE);
            String raw_record = (String)control_model.getModelComponent(VLCGNFBAGeneExpressionControlModel.GENE_EXPRESSION_CONTROL_RAW_STRING);

            // write -
            buffer.append("\t<control index=\"");
            buffer.append(counter++);
            buffer.append("\" name=\"");
            buffer.append(name);
            buffer.append("\" actor=\"");
            buffer.append(actor);
            buffer.append("\" target=\"");
            buffer.append(target);
            buffer.append("\" type=\"");
            buffer.append(type);
            buffer.append("\" raw_control_string=\"");
            buffer.append(raw_record);
            buffer.append("\"/>\n");
        }


        // return -
        return buffer.toString();
    }

    private String _buildGlobalListOfTranslationReactionsFromModelTree(int offset) throws Exception {

        // Method variables -
        StringBuilder buffer = new StringBuilder();
        int reaction_counter = 1+offset;

        String translation_class_name_key = _package_name_parser_delegate + ".VLCGNFBATranslationParserDelegate";
        Vector<VLCGNFBAModelComponent> translation_component_vector = _model_component_table.get(Class.forName(translation_class_name_key));
        for (VLCGNFBAModelComponent model_component: translation_component_vector) {

            // Cast -
            VLCGNFBATranslationModel reaction_model = (VLCGNFBATranslationModel )model_component;

            // Get data from the model -
            String enzyme_symbol = (String)reaction_model.getModelComponent(VLCGNFBATranslationModel.TRANSLATION_RIBOSOME_SYMBOL);
            String mrna_symbol = (String)reaction_model.getModelComponent(VLCGNFBATranslationModel.TRANSLATION_MRNA_SYMBOL);
            String protein_symbol = (String)reaction_model.getModelComponent(VLCGNFBATranslationModel.TRANSLATION_PROTEIN_SYMBOL);
            String reaction_name = (String)reaction_model.getModelComponent(VLCGNFBATranslationModel.TRANSLATION_REACTION_NAME);
            String formatted_record = (String)reaction_model.getModelComponent(VLCGNFBATranslationModel.TRANSLATION_REACTION_RAW_STRING);

            // Write the record -
            buffer.append("\t<reaction index=\"");
            buffer.append(reaction_counter++);
            buffer.append("\" name=\"");
            buffer.append(reaction_name);
            buffer.append("\" enzyme_symbol=\"");
            buffer.append(enzyme_symbol);
            buffer.append("\" type=\"TRANSLATION");
            buffer.append("\" formatted_record=\"");
            buffer.append(formatted_record);
            buffer.append("\">\n");

            // list of reactants -
            buffer.append("\t\t<listOfReactants>\n");
            buffer.append("\t\t\t<speciesReference species=\"");
            buffer.append(mrna_symbol);
            buffer.append("\" stoichiometry=\"1.0\"/>\n");
            buffer.append("\t\t</listOfReactants>\n");

            // list of products -
            buffer.append("\t\t<listOfProducts>\n");
            buffer.append("\t\t\t<speciesReference species=\"");
            buffer.append(protein_symbol);
            buffer.append("\" stoichiometry=\"1.0\"/>\n");
            buffer.append("\t\t</listOfProducts>\n");
            buffer.append("\t</reaction>\n");

        }

        // return buffer -
        return buffer.toString();
    }

    public String _buildGlobalListOfGeneExpressionReactionsFromModelTree(int offset) throws Exception {

        // Method variables -
        StringBuilder buffer = new StringBuilder();
        int reaction_counter = 1+offset;

        String gene_expression_class_name_key = _package_name_parser_delegate + ".VLCGNFBAGeneExpressionParserDelegate";
        Vector<VLCGNFBAModelComponent> gene_expression_component_vector = _model_component_table.get(Class.forName(gene_expression_class_name_key));
        for (VLCGNFBAModelComponent model_component: gene_expression_component_vector) {

            // Cast -
            VLCGNFBAGeneExpressionModel reaction_model = (VLCGNFBAGeneExpressionModel)model_component;

            // Get data from the model -
            String enzyme_symbol = (String)reaction_model.getModelComponent(VLCGNFBAGeneExpressionModel.GENE_EXPRESSION_RNA_POLYMERASE_SYMBOL);
            String gene_symbol = (String)reaction_model.getModelComponent(VLCGNFBAGeneExpressionModel.GENE_EXPRESSION_GENE_SYMBOL);
            String rna_symbol = (String)reaction_model.getModelComponent(VLCGNFBAGeneExpressionModel.GENE_EXPRESSION_MRNA_SYMBOL);
            String reaction_name = (String)reaction_model.getModelComponent(VLCGNFBAGeneExpressionModel.GENE_EXPRESSION_REACTION_NAME);
            String formatted_record = (String)reaction_model.getModelComponent(VLCGNFBAGeneExpressionModel.GENE_EXPRESSION_REACTION_RAW_STRING);

            // Write the record -
            buffer.append("\t<reaction index=\"");
            buffer.append(reaction_counter++);
            buffer.append("\" name=\"");
            buffer.append(reaction_name);
            buffer.append("\" enzyme_symbol=\"");
            buffer.append(enzyme_symbol);
            buffer.append("\" type=\"EXPRESSION");
            buffer.append("\" formatted_record=\"");
            buffer.append(formatted_record);
            buffer.append("\">\n");

            // list of reactants -
            buffer.append("\t\t<listOfReactants>\n");
            buffer.append("\t\t\t<speciesReference species=\"");
            buffer.append(gene_symbol);
            buffer.append("\" stoichiometry=\"1.0\"/>\n");
            buffer.append("\t\t</listOfReactants>\n");

            // list of products -
            buffer.append("\t\t<listOfProducts>\n");
            buffer.append("\t\t\t<speciesReference species=\"");
            buffer.append(rna_symbol);
            buffer.append("\" stoichiometry=\"1.0\"/>\n");
            buffer.append("\t\t</listOfProducts>\n");
            buffer.append("\t</reaction>\n");
        }

        // return buffer -
        return buffer.toString();
    }


    public String _buildGlobalListOfBiochemicalReactionsFromModelTree(int offset) throws Exception {

        // Method variables -
        StringBuilder buffer = new StringBuilder();
        int reaction_counter = 1+offset;

        // Get the biochemistry reactions -
        String class_name_key = _package_name_parser_delegate + ".VLCGFBABiochemistryReactionParserDelegate";
        Vector<VLCGNFBAModelComponent> biochemistry_component_vector = _model_component_table.get(Class.forName(class_name_key));
        for (VLCGNFBAModelComponent model_component: biochemistry_component_vector){

            // Get the reaction model -
            VLCGNFBABiochemistryReactionModel reaction_model = (VLCGNFBABiochemistryReactionModel)model_component;

            // Get data from the reaction model -
            String enzyme_symbol = (String)reaction_model.getModelComponent(VLCGNFBABiochemistryReactionModel.REACTION_ENZYME_SYMBOL);
            String reaction_name = (String)reaction_model.getModelComponent(VLCGNFBABiochemistryReactionModel.REACTION_NAME);
            String formatted_record = (String)reaction_model.getModelComponent(VLCGNFBABiochemistryReactionModel.FORMATTED_RAW_RECORD);
            String reverse_flag = (String)reaction_model.getModelComponent(VLCGNFBABiochemistryReactionModel.REACTION_REVERSE);

            Vector<VLCGNFBASpeciesModel> reactant_species_model_vector = (Vector)reaction_model.getModelComponent(VLCGNFBABiochemistryReactionModel.BIOCHEMISTRY_REACTION_REACTANT_VECTOR);
            Vector<VLCGNFBASpeciesModel> product_species_model_vector = (Vector)reaction_model.getModelComponent(VLCGNFBABiochemistryReactionModel.BIOCHEMISTRY_REACTION_PRODUCT_VECTOR);

            if (reverse_flag.equalsIgnoreCase("-inf")){

                // Forward record -
                String local_forward_reaction_record = _buildReactionRecordForReactantAndProductSpeciesVector(reactant_species_model_vector,
                        product_species_model_vector,
                        enzyme_symbol,
                        reaction_name,
                        formatted_record,
                        reaction_counter++);

                // reverse -
                String local_reverse_reaction_record = _buildReactionRecordForReactantAndProductSpeciesVector(product_species_model_vector,
                        reactant_species_model_vector,
                        enzyme_symbol,
                        reaction_name+"_reverse",
                        "-1*("+formatted_record+")",
                        reaction_counter++);

                // add the records -
                buffer.append(local_forward_reaction_record);
                buffer.append(local_reverse_reaction_record);
            }
            else {

                String local_reaction_record = _buildReactionRecordForReactantAndProductSpeciesVector(reactant_species_model_vector,
                        product_species_model_vector,
                        enzyme_symbol,
                        reaction_name,
                        formatted_record,
                        reaction_counter++);

                buffer.append(local_reaction_record);
            }
        }


        // return -
        return buffer.toString();
    }


    private String _buildReactionRecordForReactantAndProductSpeciesVector(Vector<VLCGNFBASpeciesModel> reactant_species_model_vector,
                                                                          Vector<VLCGNFBASpeciesModel> product_species_model_vector,
                                                                          String enzyme_symbol,
                                                                          String reaction_name,
                                                                          String formatted_record,
                                                                          int reaction_counter) throws Exception {

        // method attributes -
        StringBuilder buffer = new StringBuilder();

        // bulid reaction record -
        buffer.append("\t<reaction index=\"");
        buffer.append(reaction_counter);
        buffer.append("\" name=\"");
        buffer.append(reaction_name);
        buffer.append("\" enzyme_symbol=\"");
        buffer.append(enzyme_symbol);
        buffer.append("\" type=\"BIOCHEMISTRY");
        buffer.append("\" formatted_record=\"");
        buffer.append(formatted_record);
        buffer.append("\">\n");

        // list of reactants -
        buffer.append("\t\t<listOfReactants>\n");
        for (VLCGNFBASpeciesModel species_model: reactant_species_model_vector){

            // Get the species symbol, and the coefficient -
            String species_symbol = (String)species_model.getModelComponent(VLCGNFBASpeciesModel.SPECIES_SYMBOL);
            String species_coefficient = (String)species_model.getModelComponent(VLCGNFBASpeciesModel.SPECIES_COEFFICIENT);

            // write the record -
            buffer.append("\t\t\t<speciesReference species=\"");
            buffer.append(species_symbol);
            buffer.append("\" stoichiometry=\"");
            buffer.append(species_coefficient);
            buffer.append("\"/>\n");
        }

        buffer.append("\t\t</listOfReactants>\n");
        buffer.append("\t\t<listOfProducts>\n");

        // list of products -
        for (VLCGNFBASpeciesModel species_model: product_species_model_vector){

            // Get the species symbol, and the coefficient -
            String species_symbol = (String)species_model.getModelComponent(VLCGNFBASpeciesModel.SPECIES_SYMBOL);
            String species_coefficient = (String)species_model.getModelComponent(VLCGNFBASpeciesModel.SPECIES_COEFFICIENT);

            // write the record -
            buffer.append("\t\t\t<speciesReference species=\"");
            buffer.append(species_symbol);
            buffer.append("\" stoichiometry=\"");
            buffer.append(species_coefficient);
            buffer.append("\"/>\n");
        }

        buffer.append("\t\t</listOfProducts>\n");
        buffer.append("\t</reaction>\n");

        // return -
        return buffer.toString();
    }

    private String _buildGlobalListOfSpeciesFromModelTree() throws Exception {


        // Method variables -
        StringBuilder buffer = new StringBuilder();
        Vector<String> tmp_species_symbol_vector = new Vector<String>();

        // Get genes symbols -
        String gene_expression_class_name_key = _package_name_parser_delegate + ".VLCGNFBAGeneExpressionParserDelegate";
        Vector<VLCGNFBAModelComponent> gene_expression_component_vector = _model_component_table.get(Class.forName(gene_expression_class_name_key));
        for (VLCGNFBAModelComponent model_component: gene_expression_component_vector) {

            // Get the gene name -
            String gene_symbol = (String)model_component.getModelComponent(VLCGNFBAGeneExpressionModel.GENE_EXPRESSION_GENE_SYMBOL);

            // add this to the global list -
            if (tmp_species_symbol_vector.contains(gene_symbol) == false){
                tmp_species_symbol_vector.addElement(gene_symbol);
            }
        }

        // Get the mRNA symbols -
        String translation_class_name_key = _package_name_parser_delegate + ".VLCGNFBATranslationParserDelegate";
        Vector<VLCGNFBAModelComponent> translation_component_vector = _model_component_table.get(Class.forName(translation_class_name_key));
        for (VLCGNFBAModelComponent model_component: translation_component_vector) {

            // Get the gene name -
            String mRNA_symbol = (String)model_component.getModelComponent(VLCGNFBATranslationModel.TRANSLATION_MRNA_SYMBOL);

            // add this to the global list -
            if (tmp_species_symbol_vector.contains(mRNA_symbol) == false){
                tmp_species_symbol_vector.addElement(mRNA_symbol);
            }
        }

        // Get the species generated by translation -
        for (VLCGNFBAModelComponent model_component: translation_component_vector) {

            // Get the gene name -
            String protein_symbol = (String) model_component.getModelComponent(VLCGNFBATranslationModel.TRANSLATION_PROTEIN_SYMBOL);

            // add this to the global list -
            if (tmp_species_symbol_vector.contains(protein_symbol) == false) {
                tmp_species_symbol_vector.addElement(protein_symbol);
            }
        }


        // Get the biochemistry reactions -
        String class_name_key = _package_name_parser_delegate + ".VLCGFBABiochemistryReactionParserDelegate";
        Vector<VLCGNFBAModelComponent> biochemistry_component_vector = _model_component_table.get(Class.forName(class_name_key));
        for (VLCGNFBAModelComponent model_component: biochemistry_component_vector){

            // Get the enzyme symbol -
            String enzyme_symbol = (String)model_component.getModelComponent(VLCGNFBABiochemistryReactionModel.REACTION_ENZYME_SYMBOL);

            // Add the enzyme symbol?
            if (tmp_species_symbol_vector.contains(enzyme_symbol) == false) {
                tmp_species_symbol_vector.addElement(enzyme_symbol);
            }

            // Get the reactant species symbol vector from the model component -
            Vector<VLCGNFBASpeciesModel> reactant_vector = (Vector)model_component.getModelComponent(VLCGNFBABiochemistryReactionModel.BIOCHEMISTRY_REACTION_REACTANT_VECTOR);
            for (VLCGNFBASpeciesModel species_model : reactant_vector){

                // Get the reactant string -
                String reactant_symbol = (String)species_model.getModelComponent(VLCGNFBASpeciesModel.SPECIES_SYMBOL);

                // check -
                if (tmp_species_symbol_vector.contains(reactant_symbol) == false){

                    tmp_species_symbol_vector.addElement(reactant_symbol);
                }
            }

            // Get the product symbol -
            Vector<VLCGNFBASpeciesModel> product_vector = (Vector)model_component.getModelComponent(VLCGNFBABiochemistryReactionModel.BIOCHEMISTRY_REACTION_PRODUCT_VECTOR);
            for (VLCGNFBASpeciesModel species_model : product_vector){

                // Get the reactant string -
                String product_symbol = (String)species_model.getModelComponent(VLCGNFBASpeciesModel.SPECIES_SYMBOL);

                // check -
                if (tmp_species_symbol_vector.contains(product_symbol) == false){
                    tmp_species_symbol_vector.addElement(product_symbol);
                }
            }
        }

        // ok, we have a unqiue list of species models -
        for (String species_symbol: tmp_species_symbol_vector){

            if (species_symbol.equalsIgnoreCase("[]") == false){

                buffer.append("\t\t");
                buffer.append("<species symbol=\"");
                buffer.append(species_symbol);
                buffer.append("\" is_species_balanced=\"");

                // do we have an external species?
                if (species_symbol.contains("_xt")){
                    buffer.append("false\"");
                }
                else {
                    buffer.append("true\"");
                }

                buffer.append(" initial_condition=\"0.0\"/>\n");
            }
        }

        // return the completed buffer -
        return buffer.toString();
    }


    private void _readModelFlatFile(String fileName) throws Exception {

        // method allocation -
        VLCGParserHandlerDelegate parser_delegate = null;

        // check -
        if (fileName == null){
            throw new Exception("Missing or null requirements for parsing the NFBA flat file.");
        }

        BufferedReader inReader = new BufferedReader(new FileReader(fileName));
        inReader.mark(0);
        String dataRecord = null;
        Vector<VLCGNFBAModelComponent> model_components_vector = null;
        while ((dataRecord = inReader.readLine()) != null) {

            int whitespace = dataRecord.length();

            // Need to check to make sure I have do not have a comment
            if (!dataRecord.contains("//") && whitespace != 0) {

                // Does this record start with a #pragma?
                if (dataRecord.contains("#pragma") == true){

                    // ok, this is a handler directive -
                    String[] tmp = dataRecord.split(" ");
                    String handler_class_name = tmp[tmp.length - 1];

                    // Create fully quaified class name -
                    String fully_qualified_handler_name = _package_name_parser_delegate+"."+handler_class_name;

                    // Create the handler -
                    parser_delegate = (VLCGParserHandlerDelegate)Class.forName(fully_qualified_handler_name).newInstance();

                    // Create a new vector -
                    model_components_vector = new Vector();
                }
                else {

                    // this is a "regular" line in the file -
                    // Do we have a parser handler?
                    if (parser_delegate == null){
                        throw new Exception("The parser delegate is null. Check your #pragma parser directives.");
                    }

                    // If we get here, we have a parser delegate ...
                    VLCGNFBAModelComponent modelComponent = (VLCGNFBAModelComponent)parser_delegate.parseLine(dataRecord);
                    modelComponent.doExecute();

                    // add this component to the vector -
                    model_components_vector.addElement(modelComponent);

                    // Add this vector to the hashtable -
                    _model_component_table.put(parser_delegate.getClass(),model_components_vector);
                }
            }
        }

        // close -
        inReader.close();
    }
}
