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
import org.varnerlab.kwatee.nfbamodel.model.VLCGNFBASpeciesModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class VLCGNFBAJuliaLFBAModelDelegate {

    // instance variables -
    private VLCGCopyrightFactory copyrightFactory = VLCGCopyrightFactory.getSharedInstance();
    private java.util.Date today = Calendar.getInstance().getTime();
    private SimpleDateFormat date_formatter = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");

    public String buildDataDictionaryFunctionBuffer(VLCGNFBAModelTreeWrapper model_tree, VLCGTransformationPropertyTree property_tree) throws Exception {

        // Method variables -
        StringBuilder buffer = new StringBuilder();

        // We are using GLPK constants -
        buffer.append("include(\"GLPK_constants.jl\");\n");

        // Copyright notice -
        String copyright = copyrightFactory.getJuliaCopyrightHeader();
        buffer.append(copyright);

        // Create types -
        buffer.append("\n");
        buffer.append("# Define the custom species model type - \n");
        buffer.append("type SpeciesModel\n");
        buffer.append("\n");
        buffer.append("\t# Model instance variables - \n");
        buffer.append("\tspecies_symbol::String\n");
        buffer.append("\tspecies_lower_bound::Float64\n");
        buffer.append("\tspecies_upper_bound::Float64\n");
        buffer.append("\tspecies_constraint_type::Int32\n");
        buffer.append("\n");
        buffer.append("\t# Constructor - \n");
        buffer.append("\tfunction SpeciesModel()\n");
        buffer.append("\t\tthis = new ();\n");
        buffer.append("\tend\n");
        buffer.append("end\n");

        buffer.append("\n");
        buffer.append("# Define the custom flux model type - \n");
        buffer.append("type FluxModel\n");
        buffer.append("\n");
        buffer.append("\t# Model instance variables - \n");
        buffer.append("\tflux_symbol::String\n");
        buffer.append("\tflux_lower_bound::Float64\n");
        buffer.append("\tflux_upper_bound::Float64\n");
        buffer.append("\tflux_constraint_type::Int32\n");
        buffer.append("\tflux_obj_coeff::Float64\n");
        buffer.append("\n");
        buffer.append("\t# Constructor - \n");
        buffer.append("\tfunction FluxModel()\n");
        buffer.append("\t\tthis = new ();\n");
        buffer.append("\tend\n");
        buffer.append("end\n");


        // Get the function name -
        buffer.append("\n");
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

        // formulate species models -
        buffer.append("\n");
        buffer.append("# Generate the species model array - \n");
        buffer.append("species_model_array = buildSpeciesModelArray();\n");

        // formulate the flux models -
        buffer.append("\n");
        buffer.append("# Generate the flux model array - \n");
        buffer.append("flux_model_array = buildFluxModelArray();\n");

        // Is this min or max?
        buffer.append("\n");
        buffer.append("# Set the min or max flag (default is min) - \n");
        buffer.append("min_flag = true;\n");

        buffer.append("\n");
        buffer.append("# ---------------------------- DO NOT EDIT BELOW THIS LINE -------------------------- #\n");
        buffer.append("data_dictionary = Dict();\n");
        buffer.append("data_dictionary[\"STOICHIOMETRIC_MATRIX\"] = S;\n");
        buffer.append("data_dictionary[\"MIN_FLAG\"] = min_flag;\n");
        buffer.append("data_dictionary[\"SPECIES_MODEL_ARRAY\"] = species_model_array;\n");
        buffer.append("data_dictionary[\"FLUX_MODEL_ARRAY\"] = flux_model_array;\n");
        buffer.append("# ----------------------------------------------------------------------------------- #\n");

        // last line -
        buffer.append("return data_dictionary;\n");
        buffer.append("end\n");

        // Build the buildSpeciesModelArray function -
        buffer.append("\n");
        buffer.append("# ----------------------------------------------------------------------------------- #\n");
        buffer.append("# Helper function: buildSpeciesModelArray\n");
        buffer.append("# Constructs an array of species models\n");
        buffer.append("# Generated using the Kwatee code generation system \n");
        buffer.append("#\n");
        buffer.append("# Input arguments: \n");
        buffer.append("# N/A\n");
        buffer.append("# \n");
        buffer.append("# Return arguments: \n");
        buffer.append("# species_model_array  - number_of_species x 1 array of SpeciesModels \n");
        buffer.append("# ----------------------------------------------------------------------------------- #\n");
        buffer.append("function buildSpeciesModelArray()\n");
        buffer.append("\n");
        buffer.append("# function variables - \n");
        buffer.append("species_model_array = SpeciesModel[];\n");


        // ok - lets build the species models -
        ArrayList<VLCGNFBASpeciesModel> species_model_array = model_tree.getListOfSpeciesModelsFromModelTree();
        


        buffer.append("return species_model_array;\n");
        buffer.append("end\n");

        // Build the buildFluxModelArray function -
        buffer.append("\n");
        buffer.append("# ----------------------------------------------------------------------------------- #\n");
        buffer.append("# Helper function: buildFluxModelArray\n");
        buffer.append("# Constructs an array of species models\n");
        buffer.append("# Generated using the Kwatee code generation system \n");
        buffer.append("#\n");
        buffer.append("# Input arguments: \n");
        buffer.append("# N/A\n");
        buffer.append("# \n");
        buffer.append("# Return arguments: \n");
        buffer.append("# flux_model_array  - number_of_fluxes x 1 array of FluxModels \n");
        buffer.append("# ----------------------------------------------------------------------------------- #\n");
        buffer.append("function buildFluxModelArray()\n");
        buffer.append("\n");
        buffer.append("# function variables - \n");
        buffer.append("flux_model_array = FluxModel[];\n");
        buffer.append("return flux_model_array;\n");
        buffer.append("end\n");

        // return -
        return buffer.toString();
    }

    public String buildDriverFunctionBuffer(VLCGNFBAModelTreeWrapper model_tree, VLCGTransformationPropertyTree property_tree) throws Exception {

        // String buffer -
        StringBuilder driver = new StringBuilder();

        // We need to get the imports -
        String balance_filename = property_tree.lookupKwateeDataDictionaryFunctionName() + ".jl";
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
        driver.append("(data_dictionary)\n");

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
        driver.append("# data_dictionary  - Data dictionary instance (holds species and flux models etc) \n");
        driver.append("# \n");
        driver.append("# Return arguments: \n");
        driver.append("# objective_value\t - value of the objective returned by GLPK\n");
        driver.append("# calculated_flux_array\t - optimal metabolic flux array (number_of_fluxes x 1)\n");
        driver.append("# uptake_array\t - stoichiometrix_matrix*flux_array (number_of_species x 1) \n");
        driver.append("# exit_flag\t - Exit flag returned by GLPK \n");
        driver.append("# ----------------------------------------------------------------------------------- #\n");
        driver.append("\n");

        // Get the stoichiometric array -
        driver.append("# Get the stoichiometric_matrix from data_dictionary - \n");
        driver.append("stoichiometric_matrix = data_dictionary[\"STOICHIOMETRIC_MATRIX\"];\n");
        driver.append("(number_of_species,number_of_fluxes) = size(stoichiometric_matrix);\n");

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
        driver.append("# Set the number of constraints and fluxes - \n");
        driver.append("GLPK.add_rows(lp_problem, number_of_species);\n");
        driver.append("GLPK.add_cols(lp_problem, number_of_fluxes);\n");

        // setup default bounds on fluxes -
        driver.append("\n");
        driver.append("# Setup flux bounds, and objective function - \n");
        driver.append("flux_model_array = data_dictionary[\"FLUX_MODEL_ARRAY\"];\n");
        driver.append("flux_index_vector = collect(1:number_of_fluxes);\n");
        driver.append("for flux_index in flux_index_vector\n");
        driver.append("\n");
        driver.append("\t# Get the default flux bounds and name - \n");
        driver.append("\tflux_model = flux_model_array[species_index];\n");
        driver.append("\tlower_bound = flux_model.flux_lower_bound;\n");
        driver.append("\tupper_bound = flux_model.flux_upper_bound;\n");
        driver.append("\tflux_symbol = flux_model.flux_symbol;\n");
        driver.append("\tobj_coeff = flux_model.flux_obj_coeff;\n");
        driver.append("\n");
        driver.append("\t# Set the bounds in GLPK - \n");
        driver.append("\tGLPK.set_col_name(lp_problem, flux_index, flux_symbol);\n");
        driver.append("\tGLPK.set_col_bnds(lp_problem, flux_index, GLPK.DB, lower_bound, upper_bound);\n");
        driver.append("\n");
        driver.append("\t# Set the objective function value in GLPK - \n");
        driver.append("\tGLPK.set_obj_coef(lp_problem, flux_index, obj_coeff);\n");
        driver.append("end\n");

        // Setup constraints -
        driver.append("\n");
        driver.append("# Setup problem constraints for the metabolites - \n");
        driver.append("species_model_array = data_dictionary[\"SPECIES_MODEL_ARRAY\"];\n");
        driver.append("species_index_vector = collect(1:number_of_species);\n");
        driver.append("for species_index in species_index_vector\n");
        driver.append("\n");
        driver.append("\t# Get data for the GLPK problem from the species model array - \n");
        driver.append("\tspecies_model = species_model_array[species_index];\n");
        driver.append("\tspecies_symbol = species_model.species_symbol;\n");
        driver.append("\tspecies_constraint_type = species_model.species_constraint_type;\n");
        driver.append("\tspecies_balance_lower_bound = species_model.species_balance_lower_bound;\n");
        driver.append("\tspecies_balance_upper_bound = species_model.species_balance_upper_bound;\n");
        driver.append("\n");
        driver.append("\t# Set the species bounds in GLPK - \n");
        driver.append("\tGLPK.set_row_name(lp_problem, species_index, species_symbol);\n");
        driver.append("\tGLPK.set_row_bnds(lp_problem, species_index, species_constraint_type, species_lower_bound, species_upper_bound);\n");
        driver.append("\n");
        driver.append("end\n");

        // Setup the stoichiometric array -
        driver.append("\n");
        driver.append("# Setup the stoichiometric array - \n");
        driver.append("counter = 1;\n");
        driver.append("row_index_array = zeros(Int,number_of_species*number_of_fluxes);\n");
        driver.append("col_index_array = zeros(Int,number_of_species*number_of_fluxes);\n");
        driver.append("flat_stoichiometric_array = zeros(Float64,number_of_species*number_of_fluxes);\n");
        driver.append("for species_index in species_index_vector\n");
        driver.append("\tfor flux_index in flux_index_vector\n");
        driver.append("\t\trow_index_array[counter] = species_index;\n");
        driver.append("\t\tcol_index_array[counter] = flux_index;\n");
        driver.append("\t\tflat_stoichiometric_array[counter] = stoichiometric_matrix[species_index,flux_index];\n");
        driver.append("\t\tcounter+=1;\n");
        driver.append("\tend\n");
        driver.append("end\n");
        driver.append("GLPK.load_matrix(lp_problem, number_of_species*number_of_fluxes, row_index_array, col_index_array, flat_stoichiometric_array);\n");

        // Setup solver parameters -
        driver.append("\n");
        driver.append("# Set solver parameters \n");
        driver.append("solver_parameters = GLPK.SimplexParam();\n");
        driver.append("solver_parameters.msg_lev = GLPK.MSG_ERR;\n");
        driver.append("param.presolve = GLPK.ON;\n");
        driver.append("GLPK.init_smcp(solver_parameters);\n");

        // Solve -
        driver.append("\n");
        driver.append("# Call the solver -\n");
        driver.append("exit_flag = GLPK.simplex(lp_problem, solver_parameters);\n");

        // get obj_value -
        driver.append("\n");
        driver.append("# Get the objective function value -\n");
        driver.append("objective_value = GLPK.get_obj_val(lp_problem);\n");

        // Get flux -
        driver.append("\n");
        driver.append("# Get the calculated flux values from GLPK - \n");
        driver.append("calculated_flux_array = zeros(Float64,number_of_fluxes);\n");
        driver.append("for flux_index in flux_index_vector\n");
        driver.append("\tcalculated_flux_array[flux_index] = GLPK.get_col_prim(lp_problem, flux_index);\n");
        driver.append("end\n");

        // Calculate the uptake_array -
        driver.append("\n");
        driver.append("# Calculate the uptake array - \n");
        driver.append("uptake_array = stoichiometric_matrix*calculated_flux_array;\n");

        // Return -
        driver.append("\n");
        driver.append("# Formulate the return tuple -\n");
        driver.append("return (objective_value, calculated_flux_array, uptake_array, exit_flag);\n");
        driver.append("end\n");

        // return -
        return driver.toString();
    }
}
