/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */

package org.openmrs.module.kenyacore.test;

import org.openmrs.calculation.BaseCalculation;
import org.openmrs.calculation.parameter.SimpleParameterDefinition;
import org.openmrs.calculation.patient.PatientCalculation;
import org.openmrs.calculation.patient.PatientCalculationContext;
import org.openmrs.calculation.result.CalculationResultMap;
import org.openmrs.module.kenyacore.calculation.BooleanResult;

import java.util.Collection;
import java.util.Map;

/**
 * Dummy calculation for testing that returns true for everybody and has parameters
 */
public class TestCalculationWithParams extends BaseCalculation implements PatientCalculation {

	public TestCalculationWithParams() {
		addParameterDefinition(new SimpleParameterDefinition("param1", String.class.getName(), "Param #1", false));
		addParameterDefinition(new SimpleParameterDefinition("param2", Integer.class.getName(), "Param #2", false));
	}

	/**
	 * @see org.openmrs.calculation.patient.PatientCalculation#evaluate(java.util.Collection, java.util.Map, org.openmrs.calculation.patient.PatientCalculationContext)
	 */
	@Override
	public CalculationResultMap evaluate(Collection<Integer> cohort, Map<String, Object> parameterValues, PatientCalculationContext context) {
		CalculationResultMap ret = new CalculationResultMap();

		for (int ptId : cohort) {
			ret.put(ptId, new BooleanResult(true, this, context));
		}

		return ret;
	}
}