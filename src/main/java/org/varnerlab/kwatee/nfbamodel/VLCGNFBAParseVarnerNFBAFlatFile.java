package org.varnerlab.kwatee.nfbamodel;

import org.varnerlab.kwatee.foundation.VLCGInputHandler;
import org.varnerlab.kwatee.foundation.VLCGTransformationPropertyTree;
import org.varnerlab.kwatee.nfbamodel.model.VLCGNFBAModelComponent;
import org.varnerlab.kwatee.nfbamodel.parserdelegate.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Hashtable;
import java.util.Vector;

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
        return null;
    }

    // private helper methods -
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
