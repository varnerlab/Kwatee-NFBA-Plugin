package org.varnerlab.kwatee.nfbamodel;

import org.varnerlab.kwatee.foundation.VLCGOutputHandler;
import org.varnerlab.kwatee.foundation.VLCGTransformationPropertyTree;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.InputStream;
import java.io.*;
import java.lang.reflect.Method;

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
public class VLCGNFBAWriteJuliaNFBAModel implements VLCGOutputHandler {

    // Instance variables -
    private VLCGTransformationPropertyTree _transformation_properties_tree = null;
    private VLCGNFBAJuliaNFBAModelDelegate _model_delegate = new VLCGNFBAJuliaNFBAModelDelegate();
    private XPathFactory _xpath_factory = XPathFactory.newInstance();
    private XPath _xpath = _xpath_factory.newXPath();
    private Document _generation_tree = null;

    @Override
    public void writeResource(Object object) throws Exception {

        // Get the tree wrapper -
        VLCGNFBAModelTreeWrapper model_wrapper = (VLCGNFBAModelTreeWrapper)object;

        // Get the generation_tree -
        _generation_tree = _readControlTreeFromJARArchive();

        // Get the method and path data from the generation_tree -
        NodeList generate_node_list = _lookupPropertyCollectionFromTreeUsingXPath(".//file");
        int number_of_files_to_generate = generate_node_list.getLength();
        for (int file_index = 0;file_index<number_of_files_to_generate;file_index++){

            // Get the node -
            Node generate_node = generate_node_list.item(file_index);

            // Get the attributes for this node -
            NamedNodeMap attribute_map = generate_node.getAttributes();
            String logic_method_name = attribute_map.getNamedItem("logic_method").getNodeValue();
            String path_method_name = attribute_map.getNamedItem("path_method").getNodeValue();

            System.out.println("Calling logic_method_name = "+logic_method_name+" is model_wrapper ok? "+model_wrapper);

            // Build a logic method from name, and call the method -
            Method logic_method_instance = _model_delegate.getClass().getMethod(logic_method_name,model_wrapper.getClass(),_transformation_properties_tree.getClass());
            String buffer = (String)logic_method_instance.invoke(_model_delegate,model_wrapper,_transformation_properties_tree);

            // Build the path method from name, call the path method -
            Method path_method_instance = _transformation_properties_tree.getClass().getMethod(path_method_name);
            String fully_qualified_path = (String)path_method_instance.invoke(_transformation_properties_tree);

            // write the file -
            _writeBuffer(fully_qualified_path,buffer);
        }
    }

    @Override
    public void setPropertiesTree(VLCGTransformationPropertyTree properties_tree) {

        if (properties_tree == null){
            return;
        }

        _transformation_properties_tree = properties_tree;
    }

    // Private helper methods -
    private Document _readControlTreeFromJARArchive() throws Exception {

        // Method variables -
        DocumentBuilder document_builder = null;
        Document local_generation_tree = null;

        // Read the Control.xml file -
        InputStream input = getClass().getResourceAsStream("/Control.xml");
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        reader.mark(0);
        String dataRecord = null;
        StringBuilder xml_buffer = new StringBuilder();
        while ((dataRecord = reader.readLine()) != null) {

            xml_buffer.append(dataRecord);
            xml_buffer.append("\n");
        }

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        document_builder = factory.newDocumentBuilder();
        local_generation_tree = document_builder.parse(new InputSource(new StringReader(xml_buffer.toString())));

        return local_generation_tree;
    }

    // private helper methods -
    private void _writeBuffer(String path,String buffer) throws Exception {

        // Create writer
        File oFile = new File(path);
        BufferedWriter writer = new BufferedWriter(new FileWriter(oFile));

        // Write buffer to file system and close writer
        writer.write(buffer);
        writer.close();
    }

    private NodeList _lookupPropertyCollectionFromTreeUsingXPath(String xpath_string) throws Exception {

        if (xpath_string == null) {
            throw new Exception("Null xpath in property lookup call.");
        }

        // Exceute the xpath -
        NodeList node_list = null;
        try {

            node_list = (NodeList) _xpath.evaluate(xpath_string, _generation_tree, XPathConstants.NODESET);
        }
        catch (Exception error) {
            error.printStackTrace();
            System.out.println("ERROR: Property lookup failed. The following XPath "+xpath_string+" resulted in an error - "+error.toString());
        }

        // return -
        return node_list;
    }
}
