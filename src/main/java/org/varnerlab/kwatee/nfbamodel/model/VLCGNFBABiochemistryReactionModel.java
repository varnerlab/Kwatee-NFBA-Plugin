package org.varnerlab.kwatee.nfbamodel.model;

import java.util.Hashtable;
import java.util.StringTokenizer;
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
 * Created by jeffreyvarner on 11/22/15.
 */
public class VLCGNFBABiochemistryReactionModel implements VLCGNFBAModelComponent {

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

        // Create reactant and product vectors -
        Vector<VLCGNFBASpeciesModel> reactant_vector = new Vector<VLCGNFBASpeciesModel>();
        Vector<VLCGNFBASpeciesModel> product_vector = new Vector<VLCGNFBASpeciesModel>();

        // Parse -
        _parseReactionString((String) getModelComponent(VLCGNFBABiochemistryReactionModel.REACTION_REACTANTS), reactant_vector, false);
        _parseReactionString((String) getModelComponent(VLCGNFBABiochemistryReactionModel.REACTION_PRODUCTS), product_vector, true);

        // Cache the product and reactant vector -
        setModelComponent(VLCGNFBABiochemistryReactionModel.BIOCHEMISTRY_REACTION_REACTANT_VECTOR,reactant_vector);
        setModelComponent(VLCGNFBABiochemistryReactionModel.BIOCHEMISTRY_REACTION_PRODUCT_VECTOR,product_vector);

        // Create the enzyme model -
        if (_model_component_table.containsKey(VLCGNFBABiochemistryReactionModel.REACTION_ENZYME_SYMBOL) == true){

            // get the symbol -
            String enzyme_symbol = (String)_model_component_table.get(VLCGNFBABiochemistryReactionModel.REACTION_ENZYME_SYMBOL);

            // Create species model -
            VLCGNFBASpeciesModel enzyme_model = new VLCGNFBASpeciesModel();
            enzyme_model.setModelComponent(VLCGNFBASpeciesModel.SPECIES_COEFFICIENT,"0.0");
            enzyme_model.setModelComponent(VLCGNFBASpeciesModel.SPECIES_SYMBOL,enzyme_symbol);

            // set -
            setModelComponent(VLCGNFBABiochemistryReactionModel.BIOCHEMISTRY_REACTION_ENZYME_MODEL,enzyme_model);
        }

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

    // private methods -
    private void _parseReactionString(String frag, Vector<VLCGNFBASpeciesModel> vector, boolean isProduct) throws Exception {

        // Ok, this method contains the logic to cut up the reaction strings -

        // Cut around the +'s'
        StringTokenizer tokenizer=new StringTokenizer(frag,"+",false);
        while (tokenizer.hasMoreElements()) {
            // Get a data from the tokenizer -
            Object dataChunk=tokenizer.nextToken();

            // Create new symbol wrapper
            VLCGNFBASpeciesModel symbol = new VLCGNFBASpeciesModel();

            // Check to see if this dataChunk string contains a *
            if (((String)dataChunk).contains("*")) {
                // If I get here, then the string contains a stoichiometric coefficient

                // Cut around the *'s
                StringTokenizer tokenizerCoeff=new StringTokenizer((String)dataChunk,"*",false);
                int intCoeffCounter = 1;
                while (tokenizerCoeff.hasMoreElements()) {

                    Object dataCoeff = tokenizerCoeff.nextToken();

                    if (intCoeffCounter==1) {
                        if (isProduct){
                            symbol.setModelComponent(VLCGNFBASpeciesModel.SPECIES_COEFFICIENT,dataCoeff);
                        }
                        else {
                            symbol.setModelComponent(VLCGNFBASpeciesModel.SPECIES_COEFFICIENT, dataCoeff);
                        }

                        // Update the counter
                        intCoeffCounter++;
                    }
                    else if (intCoeffCounter==2) {
                        symbol.setModelComponent(VLCGNFBASpeciesModel.SPECIES_SYMBOL,(String)dataCoeff);
                    }
                }
            }
            else {
                // If I get here, then no coefficient
                if (isProduct) {
                    // If this metabolite is in a product string, then coeff is positive
                    symbol.setModelComponent(VLCGNFBASpeciesModel.SPECIES_SYMBOL,(String)dataChunk);
                    symbol.setModelComponent(VLCGNFBASpeciesModel.SPECIES_COEFFICIENT,"1.0");
                }
                else {
                    // If this metabolite is in a reactant string, then coeff is negative
                    symbol.setModelComponent(VLCGNFBASpeciesModel.SPECIES_SYMBOL,(String)dataChunk);
                    symbol.setModelComponent(VLCGNFBASpeciesModel.SPECIES_COEFFICIENT,"1.0");
                }
            }

            // Add to symbol wrapper to the vector -
            vector.addElement(symbol);
        }
    }
}
