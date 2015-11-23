package org.varnerlab.kwatee.nfbamodel.parserdelegate;

import java.util.StringTokenizer;
import org.varnerlab.kwatee.nfbamodel.model.VLCGNFBAGeneExpressionControlModel;

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
public class VLCGNFBAGeneExpressionControlParserDelegate implements VLCGParserHandlerDelegate {

    // Instance variables -
    VLCGNFBAGeneExpressionControlModel _model = null;

    @Override
    public Object parseLine(String line) throws Exception {

        // ok, create a model instance -
        _model = new VLCGNFBAGeneExpressionControlModel();

        // Store the raw string -
        _model.setModelComponent(VLCGNFBAGeneExpressionControlModel.GENE_EXPRESSION_CONTROL_RAW_STRING,_formatReactionString(line));

        // Parse this line -
        StringTokenizer stringTokenizer = new StringTokenizer(line,",");
        int counter = 1;
        while (stringTokenizer.hasMoreElements()) {

            // Get the token -
            String token = (String) stringTokenizer.nextToken();

            if (counter == 1) {

                _model.setModelComponent(VLCGNFBAGeneExpressionControlModel.GENE_EXPRESSION_CONTROL_NAME, token);
            } else if (counter == 2) {

                String strTmp = ((String) token).replace("-", "_");
                _model.setModelComponent(VLCGNFBAGeneExpressionControlModel.GENE_EXPRESSION_CONTROL_ACTOR, strTmp);
            } else if (counter == 3) {

                String strTmp = ((String) token).replace("-", "_");
                _model.setModelComponent(VLCGNFBAGeneExpressionControlModel.GENE_EXPRESSION_CONTROL_TARGET, strTmp);
            } else if (counter == 4) {
                // remove the ;
                String strTmp = token.substring(0, token.length() - 1);
                _model.setModelComponent(VLCGNFBAGeneExpressionControlModel.GENE_EXPRESSION_CONTROL_TYPE, strTmp);
            } else {
                throw new Exception(this.getClass().toString() + " does not support more than four tokens. Incorrect format for line:"+line);
            }

            // update the counter -
            counter++;
        }

        // return the model -
        return _model;
    }

    private String _formatReactionString(String line) throws Exception {

        // method variables -
        int counter = 1;
        StringBuffer buffer = new StringBuffer();

        // Items -
        String name = "";
        String actor = "";
        String type = "";
        String target = "";

        // split around the ','
        StringTokenizer stringTokenizer = new StringTokenizer(line,",");
        while (stringTokenizer.hasMoreElements()){

            // Get the token -
            String token = (String)stringTokenizer.nextToken();

            if (counter == 1){
                name = token;
            }
            else if (counter == 2){
                actor = token;
            }
            else if (counter == 3){
                target = token;
            }
            else if (counter == 4){
                String tmp = token.substring(0,token.length() - 1);
                type = tmp;
            }


            // update the counter -
            counter++;
        }

        // Build the buffer -
        buffer.append(name);
        buffer.append(": ");
        buffer.append(actor);
        buffer.append(" ");
        buffer.append(type);
        buffer.append(" ");
        buffer.append(target);

        // return the buffer -
        return buffer.toString();
    }
}
