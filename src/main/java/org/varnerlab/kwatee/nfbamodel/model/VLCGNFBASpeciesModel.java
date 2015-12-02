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
 * Created by jeffreyvarner on 10/30/15.
 */
public class VLCGNFBASpeciesModel implements VLCGNFBAModelComponent {

    // instance variables -
    private Hashtable _reaction_component_table = new Hashtable();

    // hastable keys -
    public static final String SPECIES_SYMBOL = "species_symbol";
    public static final String SPECIES_COEFFICIENT = "species_coefficient";
    public static final String SPECIES_RULE_TYPE = "species_rule_type";
    public static final String SPECIES_SPECIES_TYPE = "species_type";
    public static final String SPECIES_INITIAL_CONDITION = "species_initial_condition";
    public static final String SPECIES_BALANCED_FLAG = "species_balanced_flag";

    public static final String RAW_RECORD = "raw_record";

    public Boolean containsKey(String key) throws Exception {

        // method variables -
        Boolean table_contains_key = false;

        if (_reaction_component_table.containsKey(key)) {
            table_contains_key = true;
        }

        // return default -
        return table_contains_key;
    }

    @Override
    public Object getModelComponent(String key) throws Exception {

        if (key == null || _reaction_component_table.containsKey(key) == false){
            throw new Exception("Missing species model component. Can't find key = "+key);
        }

        return _reaction_component_table.get(key);
    }

    @Override
    public void setModelComponent(String key, Object value) {

        if (key == null && value == null){
            return;
        }

        // store the reaction component -
        _reaction_component_table.put(key,value);
    }

    @Override
    public Object doExecute() throws Exception {
        return null;
    }
}
