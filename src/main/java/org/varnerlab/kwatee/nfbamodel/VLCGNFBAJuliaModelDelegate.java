package org.varnerlab.kwatee.nfbamodel;

import org.varnerlab.kwatee.foundation.VLCGCopyrightFactory;
import org.varnerlab.kwatee.foundation.VLCGTransformationPropertyTree;

import java.text.SimpleDateFormat;
import java.util.Calendar;

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
public class VLCGNFBAJuliaModelDelegate {


    // instance variables -
    private VLCGCopyrightFactory copyrightFactory = VLCGCopyrightFactory.getSharedInstance();
    private java.util.Date today = Calendar.getInstance().getTime();
    private SimpleDateFormat date_formatter = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");


    public String buildDataDictionaryFunctionBuffer(VLCGNFBAModelTreeWrapper model_tree, VLCGTransformationPropertyTree property_tree) throws Exception {

        // String builder -
        StringBuilder buffer = new StringBuilder();

        // Copyright notice -
        String copyright = copyrightFactory.getJuliaCopyrightHeader();
        buffer.append(copyright);

        // Get the function name -
        String function_name = property_tree.lookupKwateeDataDictionaryFunctionName();
        buffer.append("function ");
        buffer.append(function_name);
        buffer.append("(TSTART,TSTOP,Ts)\n");

        buffer.append("# ----------------------------------------------------------------------------------- #\n");
        buffer.append("# ");
        buffer.append(function_name);
        buffer.append(".jl was generated using the Kwatee code generation system.\n");
        buffer.append("# ");
        buffer.append(function_name);
        buffer.append(": Stores model parameters as key - value pairs in a Julia Dict() \n");
        buffer.append("# Username: ");
        buffer.append(property_tree.lookupKwateeModelUsername());
        buffer.append("\n");
        buffer.append("# Type: ");
        buffer.append(property_tree.lookupKwateeModelType());
        buffer.append("\n");
        buffer.append("# Version: ");
        buffer.append(property_tree.lookupKwateeModelVersion());
        buffer.append("\n");
        buffer.append("# Generation timestamp: ");
        buffer.append(date_formatter.format(today));
        buffer.append("\n");
        buffer.append("# \n");
        buffer.append("# Input arguments: \n");
        buffer.append("# TSTART  - Time start \n");
        buffer.append("# TSTOP  - Time stop \n");
        buffer.append("# Ts - Time step \n");
        buffer.append("# \n");
        buffer.append("# Return arguments: \n");
        buffer.append("# data_dictionary  - Data dictionary instance (holds model parameters) \n");
        buffer.append("# ----------------------------------------------------------------------------------- #\n");
        buffer.append("\n");

        // return -
        return buffer.toString();
    }
}
