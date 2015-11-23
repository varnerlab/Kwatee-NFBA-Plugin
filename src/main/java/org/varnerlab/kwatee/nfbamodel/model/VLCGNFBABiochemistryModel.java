package org.varnerlab.kwatee.nfbamodel.model;

import java.util.Hashtable;

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
 * Created by jeffreyvarner on 11/22/15.
 */
public class VLCGNFBABiochemistryModel implements VLCGNFBAModelComponent {

    // Instance variables -
    private Hashtable _model_component_table = new Hashtable();

    // hastable keys -
    public static final String REACTION_ENZYME_SYMBOL = "reaction_enzyme_symbol";
    public static final String REACTION_COMPARTMENT_SYMBOL = "reaction_compartment_symbol";
    public static final String REACTION_NAME = "reaction_symbol";
    public static final String REACTION_FORWARD = "reaction_forward";
    public static final String REACTION_REVERSE = "reaction_reverse";
    public static final String REACTION_REACTANTS = "reaction_reactants";
    public static final String REACTION_PRODUCTS = "reaction_products";
    public static final String RAW_RECORD = "raw_reaction_string";
    public static final String FORMATTED_RAW_RECORD = "formatted_raw_record";

    public static final String BIOCHEMISTRY_REACTION_REACTANT_VECTOR = "reactant_vector";
    public static final String BIOCHEMISTRY_REACTION_PRODUCT_VECTOR = "product_vector";
    public static final String BIOCHEMISTRY_REACTION_ENZYME_MODEL = "enzyme_model";

    @Override
    public Object getModelComponent(String key) throws Exception {

        if (key == null || _model_component_table.containsKey(key) == false){
            throw new Exception("Missing biochemical reaction component. Can't find key = "+key+" table: "+_model_component_table);
        }

        return _model_component_table.get(key);
    }

    @Override
    public Object doExecute() throws Exception {
        return null;
    }

    @Override
    public void setModelComponent(String key, Object value) {

        if (key == null && value == null){
            return;
        }

        // store the reaction component -
        _model_component_table.put(key,value);
    }
}
