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
import org.varnerlab.kwatee.nfbamodel.model.VLCGNFBABiochemistryReactionModel;
import org.varnerlab.kwatee.nfbamodel.model.VLCGNFBASpeciesModel;
import org.varnerlab.kwatee.nfbamodel.model.VLCGSimpleControlLogicModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Vector;

public class VLCGNFBAJuliaLFBAModelDelegate {

    // instance variables -
    private VLCGCopyrightFactory copyrightFactory = VLCGCopyrightFactory.getSharedInstance();
    private java.util.Date today = Calendar.getInstance().getTime();
    private SimpleDateFormat date_formatter = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");

    public String buildStoichiometricMatrixBuffer(VLCGNFBAModelTreeWrapper model_tree, VLCGTransformationPropertyTree property_tree) throws Exception {

        // Method variables -
        StringBuilder buffer = new StringBuilder();

        // stoichiometric matrix is NSPECIES x NREACTIONS big -
        ArrayList<VLCGNFBASpeciesModel> species_model_array = model_tree.getListOfSpeciesModelsFromModelTree();
        for (VLCGNFBASpeciesModel species_model : species_model_array){

            // Get species symbol, and home compartment -
            String species_symbol = (String)species_model.getModelComponent(VLCGNFBASpeciesModel.SPECIES_SYMBOL);

            // ok, we have a biochemical species -
            String row_string = model_tree.getStoichiometricCoefficientsForSpeciesInModel(species_symbol);
            buffer.append(row_string);
        }

        return buffer.toString();
    }

    public String buildTypesLibraryBuffer(VLCGNFBAModelTreeWrapper model_tree, VLCGTransformationPropertyTree property_tree) throws Exception {

        // Method variables -
        StringBuilder buffer = new StringBuilder();

        // Copyright notice -
        String copyright = copyrightFactory.getJuliaCopyrightHeader();
        buffer.append(copyright);

        // Create types -
        buffer.append("\n");
        buffer.append("# Define the custom species model type - \n");
        buffer.append("type SpeciesModel\n");
        buffer.append("\n");
        buffer.append("\t# Model instance variables - \n");
        buffer.append("\tspecies_index::Int\n");
        buffer.append("\tspecies_symbol::AbstractString\n");
        buffer.append("\tspecies_lower_bound::Float64\n");
        buffer.append("\tspecies_upper_bound::Float64\n");
        buffer.append("\tis_species_measured::Bool\n");
        buffer.append("\tspecies_measurement_array::Array{Float64,3}\n");
        buffer.append("\tspecies_constraint_type::Int32\n");
        buffer.append("\n");
        buffer.append("\t# Constructor - \n");
        buffer.append("\tfunction SpeciesModel()\n");
        buffer.append("\t\tthis = new();\n");
        buffer.append("\tend\n");
        buffer.append("end\n");

        buffer.append("\n");
        buffer.append("# Define the custom flux model type - \n");
        buffer.append("type FluxModel\n");
        buffer.append("\n");
        buffer.append("\t# Model instance variables - \n");
        buffer.append("\tflux_index::Int\n");
        buffer.append("\tflux_symbol::AbstractString\n");
        buffer.append("\tflux_lower_bound::Float64\n");
        buffer.append("\tflux_upper_bound::Float64\n");
        buffer.append("\tflux_constraint_type::Int32\n");
        buffer.append("\tflux_gamma_array::Array{Float64,1}\n");
        buffer.append("\tflux_bound_alpha::Float64\n");
        buffer.append("\tflux_bounds_model::Function\n");
        buffer.append("\tflux_obj_coeff::Float64\n");
        buffer.append("\n");
        buffer.append("\t# Constructor - \n");
        buffer.append("\tfunction FluxModel()\n");
        buffer.append("\t\tthis = new();\n");
        buffer.append("\tend\n");
        buffer.append("end\n");

        return buffer.toString();
    }

    public String buildBoundsFunctionBuffer(VLCGNFBAModelTreeWrapper model_tree, VLCGTransformationPropertyTree property_tree) throws Exception {

        // Method variables -
        StringBuilder buffer = new StringBuilder();

        // We need to get the imports -
        String typelib_filename = property_tree.lookupKwateeTypesLibraryName() + ".jl";
        buffer.append("include(\"");
        buffer.append(typelib_filename);
        buffer.append("\")\n");

        // We are using GLPK constants -
        buffer.append("using GLPK\n");

        // Copyright notice -
        String copyright = copyrightFactory.getJuliaCopyrightHeader();
        buffer.append(copyright);

        // Show the species index map -
        buffer.append("\n");
        buffer.append("# Species vector - \n");
        ArrayList<VLCGNFBASpeciesModel> species_model_array = model_tree.getListOfSpeciesModelsFromModelTree();
        int species_index = 1;
        for (VLCGNFBASpeciesModel species_model : species_model_array) {

            // Get data -
            String symbol = (String) species_model.getModelComponent(VLCGNFBASpeciesModel.SPECIES_SYMBOL);

            // write the comment line -
            buffer.append("# ");
            buffer.append(species_index);
            buffer.append("\t");
            buffer.append(symbol);
            buffer.append("\n");

            // update -
            species_index++;
        }

        // Show the reactions -
        buffer.append("\n");
        buffer.append("# Reaction model vector - \n");
        ArrayList<VLCGNFBABiochemistryReactionModel> reaction_model_array = model_tree.getListOfBiochemicalReactionModelsFromModelTree();
        int reaction_index = 1;
        for (VLCGNFBABiochemistryReactionModel reaction_model : reaction_model_array){

            // Get the data -
            String reaction_key = (String)reaction_model.getModelComponent(VLCGNFBABiochemistryReactionModel.REACTION_NAME);
            String comment = (String)reaction_model.getModelComponent(VLCGNFBABiochemistryReactionModel.FORMATTED_RAW_RECORD);

            // write the line -
            buffer.append("# ");
            buffer.append(reaction_index);
            buffer.append("\t");
            buffer.append(reaction_key);
            buffer.append("\t => \t");
            buffer.append(comment);
            buffer.append("\n");

            // update -
            reaction_index++;
        }


        // Get the function name -
        buffer.append("\n");
        String function_name = property_tree.lookupKwateeBoundsFunctionName();
        buffer.append("function ");
        buffer.append(function_name);
        buffer.append("(flux_name::AbstractString, flux_model::FluxModel, species_abundance_array, control_variable::Float64)\n");

        buffer.append("# ----------------------------------------------------------------------------------- #\n");
        buffer.append("# ");
        buffer.append(function_name);
        buffer.append(".jl was generated using the Kwatee code generation system.\n");
        buffer.append("# ");
        buffer.append(function_name);
        buffer.append(": Updates the flux bounds for flux with key \n");
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
        buffer.append("# flux_name  - name of the flux whose bounds we are checking \n");
        buffer.append("# flux_model - custom flux model \n");
        buffer.append("# control_variable - value of the control variable for this flux \n");
        buffer.append("# species_abundance_array - value of the system state at current time step \n");
        buffer.append("# \n");
        buffer.append("# Return arguments: \n");
        buffer.append("# lower_bound - value of the new lower bound \n");
        buffer.append("# upper_bound - value of the new upper bound \n");
        buffer.append("# constraint_type - value of the GLPK constraint type \n");
        buffer.append("# ----------------------------------------------------------------------------------- #\n");
        buffer.append("\n");
        buffer.append("# Default is to pass the bounds and constraint type back - \n");
        buffer.append("lower_bound = flux_model.flux_lower_bound;\n");
        buffer.append("upper_bound = flux_model.flux_upper_bound;\n");
        buffer.append("constraint_type = flux_model.flux_constraint_type\n");
        buffer.append("\n");
        buffer.append("# Default bounds update rule is a power-law (user can override if they wish) - \n");
        buffer.append("gamma_array = flux_model.flux_gamma_array;\n");
        buffer.append("alpha = flux_model.flux_bound_alpha;\n");
        buffer.append("idx = find(x->(x>0),gamma_array);\n");
        buffer.append("\n");
        buffer.append("index_vector = collect(1:length(idx))\n");
        buffer.append("tmp_array = ones(Float64,length(idx))\n");
        buffer.append("for index in index_vector\n");
        buffer.append("\tlocal_index = idx[index];\n");
        buffer.append("\ttmp_array[index] = species_abundance_array[local_index]^gamma_array[local_index];\n");
        buffer.append("end\n");

        // Logic block -
        buffer.append("\n");
        buffer.append("# Bound update logic goes here .... \n");
        buffer.append("# ... \n");

        // global checks -
        buffer.append("\n");
        buffer.append("# Check on computed bounds - \n");
        buffer.append("if (lower_bound == upper_bound)\n");
        buffer.append("\tconstraint_type = GLPK.FX\n");
        buffer.append("end\n");

        buffer.append("\n");
        buffer.append("return (lower_bound, upper_bound, constraint_type);\n");
        buffer.append("end\n");

        return buffer.toString();
    }

    public String buildDataDictionaryFunctionBuffer(VLCGNFBAModelTreeWrapper model_tree, VLCGTransformationPropertyTree property_tree) throws Exception {

        // Method variables -
        StringBuilder buffer = new StringBuilder();

        // We need to get the typelib import -
        String typelib_filename = property_tree.lookupKwateeTypesLibraryName() + ".jl";
        buffer.append("include(\"");
        buffer.append(typelib_filename);
        buffer.append("\")\n");

        // We need to get the import -
        String bounds_filename = property_tree.lookupKwateeBoundsFunctionName() + ".jl";
        buffer.append("include(\"");
        buffer.append(bounds_filename);
        buffer.append("\")\n");

        // We are using GLPK constants -
        buffer.append("using GLPK\n");

        // Copyright notice -
        String copyright = copyrightFactory.getJuliaCopyrightHeader();
        buffer.append(copyright);

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
        buffer.append("stoichiometric_matrix = float(open(readdlm,");
        buffer.append("\"");
        buffer.append(fully_qualified_stoichiometric_matrix_path);
        buffer.append("\"));\n");
        buffer.append("(number_of_species,number_of_fluxes) = size(stoichiometric_matrix);\n");

        // formulate species models -
        buffer.append("\n");
        buffer.append("# Generate the species model array - \n");
        buffer.append("species_model_dictionary = buildSpeciesModelDictionary();\n");

        // formulate the flux models -
        buffer.append("\n");
        buffer.append("# Generate the flux model array - \n");
        buffer.append("flux_model_dictionary = buildFluxModelDictionary();\n");

        // Is this min or max?
        buffer.append("\n");
        buffer.append("# Set the min or max flag (default is min) - \n");
        buffer.append("min_flag = true;\n");

        buffer.append("\n");
        buffer.append("# Formulate control parameter array - \n");
        int number_of_control_terms = model_tree.calculateTheTotalNumberOfControlTerms();
        buffer.append("control_parameter_array = zeros(");
        buffer.append(number_of_control_terms);
        buffer.append(",2);\n");

        ArrayList<String> reaction_name_list = model_tree.getListOfReactionNamesFromModelTree();
        int control_index = 1;
        for (String reaction_name : reaction_name_list){


            if (model_tree.isThisReactionRegulated(reaction_name)) {

                // Get the vector of transfer function wrappers -
                ArrayList<VLCGSimpleControlLogicModel> control_model_vector = model_tree.getControlModelListFromModelTreeForReactionWithName(reaction_name);
                for (VLCGSimpleControlLogicModel control_model : control_model_vector){

                    // Get the comment from tghe control model
                    String comment = (String)control_model.getModelComponent(VLCGSimpleControlLogicModel.CONTROL_COMMENT);
                    String header_comment = model_tree.buildControlCommentStringForControlConnectionWithName((String)control_model.getModelComponent(VLCGSimpleControlLogicModel.CONTROL_NAME));

                    // write the gain line -
                    buffer.append("# ");
                    buffer.append(header_comment);
                    buffer.append("\n");
                    buffer.append("control_parameter_array[");
                    buffer.append(control_index);
                    buffer.append(",1] = 0.1;\t#\t");
                    buffer.append(control_index);
                    buffer.append(" Gain: \t");
                    buffer.append(comment);
                    buffer.append("\n");

                    // write the order line -
                    buffer.append("control_parameter_array[");
                    buffer.append(control_index);
                    buffer.append(",2] = 1.0;\t#\t");
                    buffer.append(control_index);
                    buffer.append(" Order: \t");
                    buffer.append(comment);
                    buffer.append("\n\n");

                    // update counter -
                    control_index++;
                }
            }
        }

        // ok, we need to setup the bounds parameter array -
        buffer.append("\n");
        buffer.append("# Formulate the dilution selection array - \n");
        buffer.append("dilution_selection_array = [\n");
        ArrayList<VLCGNFBASpeciesModel> species_model_array = model_tree.getListOfSpeciesModelsFromModelTree();
        int species_index = 1;
        for (VLCGNFBASpeciesModel species_model : species_model_array) {

            // Get data -
            String symbol = (String) species_model.getModelComponent(VLCGNFBASpeciesModel.SPECIES_SYMBOL);

            // write the line -
            if (symbol.contains("gene_") == true) {

                buffer.append("\t0.0\t;\t");
            }
            else {

                buffer.append("\t1.0\t;\t");
            }


            buffer.append("#\t");
            buffer.append(species_index);
            buffer.append("\t");
            buffer.append(symbol);
            buffer.append("\n");

            // update the species index -
            species_index++;
        }
        buffer.append("];\n");

        buffer.append("\n");
        buffer.append("# ---------------------------- DO NOT EDIT BELOW THIS LINE -------------------------- #\n");
        buffer.append("data_dictionary = Dict();\n");
        buffer.append("data_dictionary[\"STOICHIOMETRIC_MATRIX\"] = stoichiometric_matrix;\n");
        buffer.append("data_dictionary[\"CONTROL_PARAMETER_ARRAY\"] = control_parameter_array;\n");
        buffer.append("data_dictionary[\"DILUTION_SELECTION_ARRAY\"] = dilution_selection_array;\n");
        buffer.append("data_dictionary[\"MIN_FLAG\"] = min_flag;\n");
        buffer.append("data_dictionary[\"SPECIES_MODEL_DICTIONARY\"] = species_model_dictionary;\n");
        buffer.append("data_dictionary[\"FLUX_MODEL_DICTIONARY\"] = flux_model_dictionary;\n");
        buffer.append("data_dictionary[\"NUMBER_OF_SPECIES\"] = number_of_species;\n");
        buffer.append("data_dictionary[\"NUMBER_OF_FLUXES\"] = number_of_fluxes;\n");
        buffer.append("# ----------------------------------------------------------------------------------- #\n");

        // last line -
        buffer.append("return data_dictionary;\n");
        buffer.append("end\n");

        // Build the buildSpeciesModelArray function -
        buffer.append("\n");
        buffer.append("# ----------------------------------------------------------------------------------- #\n");
        buffer.append("# Helper function: buildSpeciesModelDictionary\n");
        buffer.append("# Constructs a dictionary of species models\n");
        buffer.append("# Generated using the Kwatee code generation system \n");
        buffer.append("#\n");
        buffer.append("# Input arguments: \n");
        buffer.append("# N/A\n");
        buffer.append("# \n");
        buffer.append("# Return arguments: \n");
        buffer.append("# species_model_dictionary  - Dictionary of SpeciesModels key'd by species symbol \n");
        buffer.append("# ----------------------------------------------------------------------------------- #\n");
        buffer.append("function buildSpeciesModelDictionary()\n");
        buffer.append("\n");
        buffer.append("# function variables - \n");
        buffer.append("species_model_dictionary = Dict{AbstractString,SpeciesModel}();\n");
        buffer.append("\n");

        // ok - lets build the species models -
        species_model_array = model_tree.getListOfSpeciesModelsFromModelTree();
        species_index = 1;
        for (VLCGNFBASpeciesModel species_model : species_model_array){

            // Get data -
            String symbol = (String)species_model.getModelComponent(VLCGNFBASpeciesModel.SPECIES_SYMBOL);
            String balanced = (String)species_model.getModelComponent(VLCGNFBASpeciesModel.SPECIES_BALANCED_FLAG);

            // String -
            String julia_model_name = symbol+"_model";


            // write the code -
            buffer.append("# species_symbol: ");
            buffer.append(species_index);
            buffer.append(" ");
            buffer.append(symbol);
            buffer.append(" - \n");
            buffer.append(julia_model_name);
            buffer.append(" = SpeciesModel();\n");

            // Setup the index -
            buffer.append(julia_model_name);
            buffer.append(".species_index = ");
            buffer.append(species_index++);
            buffer.append(";\n");

            // Setup the symbol -
            buffer.append(julia_model_name);
            buffer.append(".species_symbol = string(\"");
            buffer.append(symbol);
            buffer.append("\");\n");

            if (balanced.equalsIgnoreCase("true")){

                // Setup the type -
                buffer.append(julia_model_name);
                buffer.append(".species_constraint_type = GLPK.FX;\n");
            }
            else {

                // Setup the type -
                buffer.append(julia_model_name);
                buffer.append(".species_constraint_type = GLPK.LO;\n");
            }

            buffer.append(julia_model_name);
            buffer.append(".species_lower_bound = 0.0;\n");
            buffer.append(julia_model_name);
            buffer.append(".species_upper_bound = 0.0;\n");

            // Add the default measurment fields -
            buffer.append(julia_model_name);
            buffer.append(".is_species_measured = false;\n");

            // add this model to the array -
            buffer.append("species_model_dictionary[\"");
            buffer.append(symbol);
            buffer.append("\"] = ");
            buffer.append(julia_model_name);
            buffer.append(";\n");
            buffer.append(julia_model_name);
            buffer.append(" = 0;\n");

            // add a trailing new line -
            buffer.append("\n");
        }

        buffer.append("return species_model_dictionary;\n");
        buffer.append("end\n");

        // Build the buildFluxModelArray function -
        buffer.append("\n");
        buffer.append("# ----------------------------------------------------------------------------------- #\n");
        buffer.append("# Helper function: buildFluxModelDictionary\n");
        buffer.append("# Constructs a dictionary of flux models\n");
        buffer.append("# Generated using the Kwatee code generation system \n");
        buffer.append("#\n");
        buffer.append("# Input arguments: \n");
        buffer.append("# N/A\n");
        buffer.append("# \n");
        buffer.append("# Return arguments: \n");
        buffer.append("# flux_model_dictionary  - Dictionary of FluxModels key'd by flux symbol \n");
        buffer.append("# ----------------------------------------------------------------------------------- #\n");
        buffer.append("function buildFluxModelDictionary()\n");
        buffer.append("\n");
        buffer.append("# function variables - \n");
        buffer.append("flux_model_dictionary = Dict{AbstractString,FluxModel}();\n");
        buffer.append("\n");

        // ok, get the fluxes from the model tree -
        ArrayList<VLCGNFBABiochemistryReactionModel> reaction_model_array = model_tree.getListOfBiochemicalReactionModelsFromModelTree();
        int reaction_index = 1;
        for (VLCGNFBABiochemistryReactionModel reaction_model : reaction_model_array){

            // Get data from the reaction model -
            String flux_symbol = (String)reaction_model.getModelComponent(VLCGNFBABiochemistryReactionModel.REACTION_NAME);
            String comment_line = (String)reaction_model.getModelComponent(VLCGNFBABiochemistryReactionModel.FORMATTED_RAW_RECORD);

            // ok, write the record -
            String julia_model_name = flux_symbol+"_model";

            // comment line -
            buffer.append("# ");
            buffer.append(reaction_index);
            buffer.append(" ");
            buffer.append(comment_line);
            buffer.append("\n");

            // declaration line -
            buffer.append(julia_model_name);
            buffer.append(" = FluxModel();\n");

            // index -
            buffer.append(julia_model_name);
            buffer.append(".flux_index = ");
            buffer.append(reaction_index++);
            buffer.append("\n");

            // name -
            buffer.append(julia_model_name);
            buffer.append(".flux_symbol = \"");
            buffer.append(flux_symbol);
            buffer.append("\"\n");

            // type -
            buffer.append(julia_model_name);
            buffer.append(".flux_constraint_type = GLPK.DB;\n");

            // lower bound -
            buffer.append(julia_model_name);
            buffer.append(".flux_lower_bound = 0.0;\n");

            // upper bound -
            buffer.append(julia_model_name);
            buffer.append(".flux_upper_bound = 1.0;\n");

            // callback -
            buffer.append(julia_model_name);
            buffer.append(".flux_bounds_model = Bounds;\n");

            // gamma array -
            buffer.append(julia_model_name);
            buffer.append(".flux_gamma_array = vec([");

            // formulate the gamma array for this flux -
            String gamma_array = model_tree.getGammaArrayForReactionWithName(flux_symbol);
            buffer.append(gamma_array);
            buffer.append("]);\n");

            // add flux bound alpha -
            buffer.append(julia_model_name);
            buffer.append(".flux_bound_alpha = 1.0;\n");

            // object coeff -
            buffer.append(julia_model_name);
            buffer.append(".flux_obj_coeff = 0.0;\n");

            // add to the dictionary -
            buffer.append("flux_model_dictionary[\"");
            buffer.append(flux_symbol);
            buffer.append("\"] = ");
            buffer.append(julia_model_name);
            buffer.append(";\n");
            buffer.append(julia_model_name);
            buffer.append(" = 0;\n");

            // add a trailing new line -
            buffer.append("\n");
        }


        buffer.append("return flux_model_dictionary;\n");
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

        // import the control file -
        String control_filename = property_tree.lookupKwateeControlFunctionName() + ".jl";
        driver.append("include(\"");
        driver.append(control_filename);
        driver.append("\")\n");

        // import GLPK -
        driver.append("using GLPK\n");
        driver.append("\n");

        // Copyright notice -
        String copyright = copyrightFactory.getJuliaCopyrightHeader();
        driver.append(copyright);

        // Get the function name -
        String function_name = property_tree.lookupKwateeDriverFunctionName();
        driver.append("function ");
        driver.append(function_name);
        driver.append("(time_index, species_abundance_array, specific_growth_rate, step_size, data_dictionary; steady_state_flag=true)\n");

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
        driver.append("dsa = data_dictionary[\"DILUTION_SELECTION_ARRAY\"];\n");
        driver.append("stoichiometric_matrix = data_dictionary[\"STOICHIOMETRIC_MATRIX\"];\n");
        driver.append("(number_of_species,number_of_fluxes) = size(stoichiometric_matrix);\n");

        // Setup the control function call
        driver.append("\n");
        driver.append("# Call the control function - \n");
        driver.append("control_array = Control(time, species_abundance_array, data_dictionary);\n");

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
        driver.append("flux_model_dictionary = data_dictionary[\"FLUX_MODEL_DICTIONARY\"];\n");
        driver.append("for (key,flux_model::FluxModel) in flux_model_dictionary\n");
        driver.append("\n");
        driver.append("\t# Get the default flux bounds and name - \n");
        driver.append("\tflux_index = flux_model.flux_index;\n");
        driver.append("\tflux_symbol = flux_model.flux_symbol;\n");
        driver.append("\tobj_coeff = flux_model.flux_obj_coeff;\n");

        // Update the bounds -
        driver.append("\n");
        driver.append("\t# Update the bounds for this flux - \n");
        driver.append("\tbounds_function = flux_model.flux_bounds_model;\n");
        driver.append("\t(flux_lower_bound, flux_upper_bound, flux_constraint_type) = bounds_function(key, flux_model, species_abundance_array, control_array[flux_index]);\n");

        driver.append("\n");
        driver.append("\t# Set the bounds in GLPK - \n");
        driver.append("\tGLPK.set_col_name(lp_problem, flux_index, flux_symbol);\n");
        driver.append("\tGLPK.set_col_bnds(lp_problem, flux_index, flux_constraint_type, flux_lower_bound, flux_upper_bound);\n");
        driver.append("\n");
        driver.append("\t# Set the objective function value in GLPK - \n");
        driver.append("\tGLPK.set_obj_coef(lp_problem, flux_index, obj_coeff);\n");
        driver.append("end\n");

        // Setup constraints -
        driver.append("\n");
        driver.append("# Setup problem constraints for the metabolites - \n");
        driver.append("species_model_dictionary = data_dictionary[\"SPECIES_MODEL_DICTIONARY\"];\n");
        driver.append("for (key,species_model::SpeciesModel) in species_model_dictionary\n");
        driver.append("\n");
        driver.append("\t# Get data for the GLPK problem from the species model array - \n");
        driver.append("\tspecies_index = species_model.species_index;\n");
        driver.append("\tspecies_symbol = species_model.species_symbol;\n");
        driver.append("\tspecies_constraint_type = species_model.species_constraint_type;\n");

        // steady state flag?
        driver.append("\n");
        driver.append("\t# Are we solving for a steady-state flux distribution?\n");
        driver.append("\tspecies_lower_bound = species_model.species_lower_bound;\n");
        driver.append("\tspecies_upper_bound = species_model.species_upper_bound;\n");
        driver.append("\tif steady_state_flag == false \n");
        driver.append("\t\tspecies_lower_bound = -(1.0/step_size)*(1 - dsa[species_index]*step_size*specific_growth_rate)*species_abundance_array[species_index];\n");
        driver.append("\t\tspecies_constraint_type = GLPK.LO;\n");
        driver.append("\tend\n");

        driver.append("\n");
        driver.append("\t# Is this species measured?\n");
        driver.append("\tif species_model.is_species_measured == true\n");
        driver.append("\n");
        driver.append("\t\tmeasurement_array = species_model.species_measurement_array;\n");
        driver.append("\t\tmeasured_value = measurement_array[time_index,2];\n");
        driver.append("\t\tmeasured_value_std = measurement_array[time_index,3];\n");
        driver.append("\t\tmeasured_value_upper_bound = measured_value + measured_value_std;\n");
        driver.append("\t\tmeasured_value_lower_bound = measured_value - measured_value_std;\n");
        driver.append("\n");
        driver.append("\t\t# Check: is lower bound negative?\n");
        driver.append("\t\tif (measured_value_lower_bound<0.0)\n");
        driver.append("\t\t\tmeasured_value_lower_bound = 0.0;\n");
        driver.append("\t\tend\n");
        driver.append("\n");
        driver.append("\t\tspecies_lower_bound =  (1.0/step_size)*(measured_value_lower_bound - (1 - dsa[species_index]*step_size*specific_growth_rate)*species_abundance_array[species_index]);\n");
        driver.append("\t\tspecies_upper_bound =  (1.0/step_size)*(measured_value_upper_bound - (1 - dsa[species_index]*step_size*specific_growth_rate)*species_abundance_array[species_index]);\n");
        driver.append("\t\tspecies_constraint_type = GLPK.DB;\n");
        driver.append("\tend\n");
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
        driver.append("species_index_vector = collect(1:number_of_species);\n");
        driver.append("flux_index_vector = collect(1:number_of_fluxes);\n");
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
        driver.append("solver_parameters.presolve = GLPK.ON;\n");
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

//        // bounds model -
//        driver.append("\n");
//        driver.append("function evalPowerLawBoundsModel(x,gamma_array)\n");
//        driver.append("\n");
//        driver.append("# find non-zero gamma - \n");
//        driver.append("\tidx_array = collect(1:length(x));\n");
//        driver.append("\ttmp_vector = ones(Float64,length(x))\n");
//        driver.append("\tfor index in idx_array\n");
//        driver.append("\t\ttmp_vector[index] = pow(x[index],gamma_array[index]);\n");
//        driver.append("\tend\n");
//        driver.append("\n");
//        driver.append("return prod(tmp_vector);\n");
//        driver.append("end\n");

        // return -
        return driver.toString();
    }

    public String buildControlFunctionBuffer(VLCGNFBAModelTreeWrapper model_tree,VLCGTransformationPropertyTree property_tree) throws Exception {

        // Method variables -
        StringBuffer buffer = new StringBuffer();

        // Get the control function name -
        String control_function_name = property_tree.lookupKwateeControlFunctionName();

        // Copyright notice -
        String copyright = copyrightFactory.getJuliaCopyrightHeader();
        buffer.append(copyright);

        // Fill in the buffer -
        buffer.append("function ");
        buffer.append(control_function_name);
        buffer.append("(t,x,data_dictionary)\n");
        buffer.append("# ---------------------------------------------------------------------- #\n");
        buffer.append("# ");
        buffer.append(control_function_name);
        buffer.append(".jl was generated using the Kwatee code generation system.\n");
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
        buffer.append("# Arguments: \n");
        buffer.append("# t  - current time \n");
        buffer.append("# x  - state vector \n");
        buffer.append("# data_dictionary  - Data dictionary instance (holds model parameters) \n");
        buffer.append("# ---------------------------------------------------------------------- #\n");
        buffer.append("\n");
        buffer.append("# Set a default value for the allosteric control variables - \n");
        buffer.append("EPSILON = 1.0e-3;\n");
        buffer.append("number_of_fluxes = data_dictionary[\"NUMBER_OF_FLUXES\"];\n");
        buffer.append("control_vector = ones(number_of_fluxes);\n");
        buffer.append("control_parameter_array = data_dictionary[\"CONTROL_PARAMETER_ARRAY\"];\n");
        buffer.append("\n");

        buffer.append("# Alias the species vector - \n");
        ArrayList<VLCGNFBASpeciesModel> species_model_list = model_tree.getListOfSpeciesModelsFromModelTree();
        int species_index = 1;
        for (VLCGNFBASpeciesModel species_model : species_model_list) {

            // Get the species symbol -
            String species_symbol = (String) species_model.getModelComponent(VLCGNFBASpeciesModel.SPECIES_SYMBOL);

            // write the symbol =
            buffer.append(species_symbol);
            buffer.append(" = x[");
            buffer.append(species_index++);
            buffer.append("];\n");
        }

        // Build the list of control elements -
        buffer.append("\n");
        buffer.append("# Build the control vector - \n");
        ArrayList<String> reaction_name_list = model_tree.getListOfReactionNamesFromModelTree();
        int reaction_index = 1;
        int control_index = 1;
        for (String reaction_name : reaction_name_list) {

            // is this reaction regulated?
            if (model_tree.isThisReactionRegulated(reaction_name)) {

                // ok, we have a regulation term for this reaction
                buffer.append("# ----------------------------------------------------------------------------------- #\n");
                buffer.append("transfer_function_vector = Float64[];\n");
                buffer.append("\n");


                // Get the transfer functions for this reaction -
                ArrayList<VLCGSimpleControlLogicModel> control_model_list = model_tree.getControlModelListFromModelTreeForReactionWithName(reaction_name);
                for (VLCGSimpleControlLogicModel control_model : control_model_list) {

                    // Get the comment -
                    String comment = (String) control_model.getModelComponent(VLCGSimpleControlLogicModel.CONTROL_COMMENT);
                    buffer.append("# ");
                    buffer.append(comment);
                    buffer.append("\n");


                    // Get the data from the model -
                    String actor = (String) control_model.getModelComponent(VLCGSimpleControlLogicModel.CONTROL_ACTOR);
                    String type = (String) control_model.getModelComponent(VLCGSimpleControlLogicModel.CONTROL_TYPE);

                    // Check the type -
                    if (type.equalsIgnoreCase("repression") || type.equalsIgnoreCase("inhibition")) {

                        // write -

                        // check do we have a zero inhibitor?
                        buffer.append("if (");
                        buffer.append(actor);
                        buffer.append("<EPSILON);\n");
                        buffer.append("\tpush!(transfer_function_vector,1.0);\n");
                        buffer.append("else\n");
                        buffer.append("\tpush!(transfer_function_vector,1.0 - (control_parameter_array[");
                        buffer.append(control_index);
                        buffer.append(",1]*(");
                        buffer.append(actor);
                        buffer.append(")^control_parameter_array[");
                        buffer.append(control_index);
                        buffer.append(",2])/(1+");
                        buffer.append("control_parameter_array[");
                        buffer.append(control_index);
                        buffer.append(",1]*(");
                        buffer.append(actor);
                        buffer.append(")^control_parameter_array[");
                        buffer.append(control_index);
                        buffer.append(",2]));\n");
                        buffer.append("end\n");
                        buffer.append("\n");
                    } else {

                        // write -
                        buffer.append("push!(transfer_function_vector,(control_parameter_array[");
                        buffer.append(control_index);
                        buffer.append(",1]*(");
                        buffer.append(actor);
                        buffer.append(")^control_parameter_array[");
                        buffer.append(control_index);
                        buffer.append(",2])/(1+");
                        buffer.append("control_parameter_array[");
                        buffer.append(control_index);
                        buffer.append(",1]*(");
                        buffer.append(actor);
                        buffer.append(")^control_parameter_array[");
                        buffer.append(control_index);
                        buffer.append(",2]));\n");
                    }

                    // update control_index -
                    control_index++;
                }

                // integrate the transfer functions -
                buffer.append("control_vector[");
                buffer.append(reaction_index);
                buffer.append("] = mean(transfer_function_vector);\n");
                buffer.append("transfer_function_vector = 0;\n");
                buffer.append("# ----------------------------------------------------------------------------------- #\n");
                buffer.append("\n");
            }

            // update the counter -
            reaction_index++;
        }

        // last line -
        buffer.append("\n");
        buffer.append("# Return the control vector - \n");
        buffer.append("return control_vector;\n");
        buffer.append("end\n");

        // return the buffer -
        return buffer.toString();
    }
}
