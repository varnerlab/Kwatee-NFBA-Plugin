package org.varnerlab.kwatee.nfbamodel;

import org.varnerlab.kwatee.nfbamodel.model.VLCGNFBASpeciesModel;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.util.ArrayList;
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
