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

package org.openmrs.module.kenyacore.etl;

import java.util.LinkedHashSet;
import java.util.Set;

import org.openmrs.module.kenyacore.AbstractContentConfiguration;

/**
 * Configuration for ETLs
 */
public class ETLConfiguration extends AbstractContentConfiguration {

	private Set<String> ddlProcedures;
	private Set<String> dmlProcedures;
	private Set<String> incrementalUpdatesProcedures;
	private Set<String> dataToolDbProcedures;



	/**
	 * Gets the procedures for DDL
	 * @return the stored procedure names
	 */
	public Set<String> getDdlProcedures() {
		if (ddlProcedures == null) {
			ddlProcedures = new LinkedHashSet<String>();
		}

		return ddlProcedures;
	}

	/**
	 * Sets the procedures for DDL
	 * @param ddlProcedures the DDL procedures
	 */
	public void setDdlProcedures(Set<String> ddlProcedures) {
		this.ddlProcedures = ddlProcedures;
	}

	/**
	 * Gets the procedures for DML
	 * @return the stored procedure names
	 */
	public Set<String> getDmlProcedures() {
		if (dmlProcedures == null) {
			dmlProcedures = new LinkedHashSet<String>();
		}

		return dmlProcedures;
	}

	/**
	 * Sets the procedures for DML
	 * @param dmlProcedures the DML procedures
	 */
	public void setDmlProcedures(Set<String> dmlProcedures) {
		this.dmlProcedures = dmlProcedures;
	}

	/**
	 * Gets the procedures for incremental updates
	 * @return the stored procedure names
	 */
	public Set<String> getIncrementalUpdatesProcedures() {
		if (incrementalUpdatesProcedures == null) {
			incrementalUpdatesProcedures = new LinkedHashSet<String>();
		}

		return incrementalUpdatesProcedures;
	}

	/**
	 * Sets the procedures for incremental updates
	 * @param incrementalUpdatesProcedures the procedures for incremental updates
	 */
	public void setIncrementalUpdatesProcedures(Set<String> incrementalUpdatesProcedures) {
		this.incrementalUpdatesProcedures = incrementalUpdatesProcedures;
	}

	/**
	 * Gets the stored procedures for datatool tables
	 * @return the stored procedure names
	 */
	public Set<String> getDataToolDbProcedures() {
		if (dataToolDbProcedures == null) {
			dataToolDbProcedures = new LinkedHashSet<String>();
		}

		return dataToolDbProcedures;
	}

	/**
	 * Sets the stored procedures for datatool tables
	 * @param dataToolDbProcedures the stored procedure names
	 */
	public void setDataToolDbProcedures(Set<String> dataToolDbProcedures) {
		this.dataToolDbProcedures = dataToolDbProcedures;
	}

}