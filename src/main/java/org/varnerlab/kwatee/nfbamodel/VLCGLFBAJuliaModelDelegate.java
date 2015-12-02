package org.varnerlab.kwatee.nfbamodel;

/*
 * Copyright (c) 2015. Varnerlab,
 * School of Chemical Engineering,
 * Purdue University, West Lafayette IN 46077 USA.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 * Created by jeffreyvarner on 12/1/2015
 */

import org.varnerlab.kwatee.foundation.VLCGCopyrightFactory;
import org.varnerlab.kwatee.foundation.VLCGTransformationPropertyTree;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class VLCGLFBAJuliaModelDelegate {

    // instance variables -
    private VLCGCopyrightFactory copyrightFactory = VLCGCopyrightFactory.getSharedInstance();
    private java.util.Date today = Calendar.getInstance().getTime();
    private SimpleDateFormat date_formatter = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");

    public String buildDataDictionaryFunctionBuffer(VLCGNFBAModelTreeWrapper model_tree, VLCGTransformationPropertyTree property_tree) throws Exception {

        // Method variables -
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

        // Get the path to the stoichiometric matrix -
        String fully_qualified_stoichiometric_matrix_path = property_tree.lookupKwateeStoichiometricMatrixFilePath();
        buffer.append("S = float(open(readdlm,");
        buffer.append("\"");
        buffer.append(fully_qualified_stoichiometric_matrix_path);
        buffer.append("\"));\n");
        buffer.append("(NSPECIES,NREACTIONS) = size(S);\n");

        buffer.append("\n");
        buffer.append("# ---------------------------- DO NOT EDIT BELOW THIS LINE -------------------------- #\n");
        buffer.append("data_dictionary = Dict();\n");
        buffer.append("data_dictionary[\"STOICHIOMETRIC_MATRIX\"] = S;\n");
        buffer.append("# ----------------------------------------------------------------------------------- #\n");

        // last line -
        buffer.append("return data_dictionary;\n");
        buffer.append("end\n");

        // return -
        return buffer.toString();
    }

    public String buildDriverFunctionBuffer(VLCGNFBAModelTreeWrapper model_tree, VLCGTransformationPropertyTree property_tree) throws Exception {

        // String buffer -
        StringBuilder driver = new StringBuilder();

        // We need to get the imports -
        String balance_filename = property_tree.lookupKwateeBalanceFunctionName() + ".jl";
        driver.append("include(\"");
        driver.append(balance_filename);
        driver.append("\")\n");
        driver.append("using GLPK;\n");
        driver.append("\n");

        // Copyright notice -
        String copyright = copyrightFactory.getJuliaCopyrightHeader();
        driver.append(copyright);

        // Get the function name -
        String function_name = property_tree.lookupKwateeDriverFunctionName();
        driver.append("function ");
        driver.append(function_name);
        driver.append("(TSTART,TSTOP,Ts,data_dictionary)\n");

        driver.append("# ----------------------------------------------------------------------------------- #\n");
        driver.append("# ");
        driver.append(function_name);
        driver.append(".jl was generated using the Kwatee code generation system.\n");
        driver.append("# ");
        driver.append(function_name);
        driver.append(": Solves the flux balance analysis problem from TSTART to TSTOP given the model encoded in data_dictionary.\n");
        driver.append("# Username: ");
        driver.append(property_tree.lookupKwateeModelUsername());
        driver.append("\n");
        driver.append("# Type: ");
        driver.append(property_tree.lookupKwateeModelType());
        driver.append("\n");
        driver.append("# Version: ");
        driver.append(property_tree.lookupKwateeModelVersion());
        driver.append("\n");
        driver.append("# Generation timestamp: ");
        driver.append(date_formatter.format(today));
        driver.append("\n");
        driver.append("# \n");
        driver.append("# Input arguments: \n");
        driver.append("# TSTART  - Time start \n");
        driver.append("# TSTOP  - Time stop \n");
        driver.append("# Ts - Time step \n");
        driver.append("# data_dictionary  - Data dictionary instance (holds model parameters) \n");
        driver.append("# \n");
        driver.append("# Return arguments: \n");
        driver.append("# TSIM - Simulation time vector \n");
        driver.append("# FLUX - Flux array (NTIME x NFLUXES) \n");
        driver.append("# EFV - Exit Flag Vector holds the exit flags from GLPK \n");
        driver.append("# ----------------------------------------------------------------------------------- #\n");
        driver.append("\n");

        // Get the stoichiometric array -
        driver.append("# Get the stoichiometric_matrix from data_dictionary - \n");
        driver.append("stoichiometric_matrix = data_dictionary[\"STOICHIOMETRIC_MATRIX\"];\n");
        driver.append("(number_of_metabolites,number_of_reactions) = size(stoichiometric_matrix);\n");

        // setup the GLPK problem -
        driver.append("\n");
        driver.append("# Setup the GLPK problem - \n");
        driver.append("lp_problem = GLPK.Prob();\n");
        driver.append("GLPK.set_prob_name(lp_problem, \"sample\");\n");
        driver.append("GLPK.set_obj_name(lp_problem, \"objective\")\n");

        driver.append("\n");
        driver.append("# Are we doing min -or- max?\n");
        driver.append("min_flag = data_dictionary[\"MIN_FLAG\"];\n");
        driver.append("if min_flag == true\n");
        driver.append("\tGLPK.set_obj_dir(lp_problem, GLPK.MIN);\n");
        driver.append("else\n");
        driver.append("\tGLPK.set_obj_dir(lp_problem, GLPK.MAX);\n");
        driver.append("end\n");

        // How many constraints do we have?
        driver.append("\n");
        driver.append("# Set the number of constraints - \n");
        driver.append("GLPK.add_rows(lp_problem,number_of_metabolites);\n");

        // setup default bounds on fluxes -
        driver.append("\n");
        driver.append("# Setup default bounds on fluxes - \n");
        driver.append("default_flux_bounds_array = data_dictionary[\"FLUX_BOUNDS_VECTOR\"];\n");
        driver.append("flux_index_vector = collect(1:number_of_reactions);\n");
        driver.append("for flux_index in flux_index_vector\n");
        driver.append("\t");
        driver.append("end\n");


        // Formulate the objective vector -
        driver.append("\n");
        driver.append("# Formulate the objective function - \n");
        driver.append("objective_index_array = data_dictionary[\"OBJECTIVE_INDEX_VECTOR\"];\n");
        driver.append("objective_function_array = zeros(number_of_reactions);\n");

        driver.append("\tobjective_function_array[objective_index_array] = 1;\n");
        driver.append("else\n");
        driver.append("\tobjective_function_array[objective_index_array] = -1;\n");
        driver.append("end\n");

        // Setup the default bounds -
        driver.append("\n");
        driver.append("# Setup the default bounds (we'll correct these below with the control functions) \n");


        // Setup the balanced and unbalanced arrays -
        driver.append("\n");
        driver.append("# Setup the balanced and unbalanced arrays - \n");
        driver.append("balanced_stoichiometry_block = data_dictionary[\"BALANCED_BLOCK\"];\n");
        driver.append("(number_balanced_metabolites,number_balanced_reactions) = size(balanced_stoichiometry_block);\n");

        // Formulate the right hand side for the balanced metabolites -
        driver.append("\n");
        driver.append("# Formulate the right hand side for the balanced metabolites - \n");
        driver.append("bV = zeros(number_balanced_metabolites);\n");

        // Formulate the CTYPE and VARTYPE arrays -
        driver.append("\n");
        driver.append("# Formulate the CTYPE array (constraint type = {});\n");



        // Correct the bounds -
        driver.append("\n");
        driver.append("# Correct the bounds using the control function - \n");



        // return -
        return driver.toString();
    }
}
