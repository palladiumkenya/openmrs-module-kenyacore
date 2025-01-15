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

package org.openmrs.module.kenyacore.report;

import org.openmrs.module.kenyacore.AbstractContentConfiguration;
import org.openmrs.module.kenyacore.program.ProgramDescriptor;

import java.util.Map;
import java.util.Set;

/**
 * Configuration for reports
 */
public class ReportConfiguration extends AbstractContentConfiguration {

	private Set<ReportDescriptor> commonReports;

	private Map<ProgramDescriptor, Set<ReportDescriptor>> programReports;

	private Set<ReportDescriptor> cohortAnalysis;

	public Set<ReportDescriptor> getEhrReports() {
		return ehrReports;
	}

	public Set<ReportDescriptor> getSpecialClinicsReport() {
		return specialClinics;
	}

	public Set<ReportDescriptor> specialClinics;

	public void setSpecialClinics(Set<ReportDescriptor> specialClinics) {
		this.specialClinics = specialClinics;
	}

	public void setEhrReports(Set<ReportDescriptor> ehrReports) {
		this.ehrReports = ehrReports;
	}

	private Set<ReportDescriptor> ehrReports;

	/**
	 * Gets the cohort analysis reports
	 * @return the report descriptors
	 */
	public Set<ReportDescriptor> getCohortAnalysis() {
		return cohortAnalysis;
	}

	/**
	 * Sets the common reports
	 * @param cohortAnalysis the report descriptors
	 */
	public void setCohortAnalysis(Set<ReportDescriptor> cohortAnalysis) {
		this.cohortAnalysis = cohortAnalysis;
	}

	/**
	 * Gets the common reports
	 * @return the report descriptors
	 */
	public Set<ReportDescriptor> getCommonReports() {
		return commonReports;
	}

	/**
	 * Sets the common reports
	 * @param commonReports the report descriptors
	 */
	public void setCommonReports(Set<ReportDescriptor> commonReports) {
		this.commonReports = commonReports;
	}

	/**
	 * Gets the program specific reports
	 * @return the map of program and report descriptors
	 */
	public Map<ProgramDescriptor, Set<ReportDescriptor>> getProgramReports() {
		return programReports;
	}

	/**
	 * Sets the program specific reports
	 * @param programReports the map of program and report descriptors
	 */
	public void setProgramReports(Map<ProgramDescriptor, Set<ReportDescriptor>> programReports) {
		this.programReports = programReports;
	}
}