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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.GlobalProperty;
import org.openmrs.api.context.Context;
import org.openmrs.module.kenyacore.ContentManager;
import org.openmrs.module.kenyacore.CoreUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * ETLConfiguration manager
 * Scans through ETL extensions and build procedures for DDL, DML, and Datatool
 */
@Component
public class ETLConfigurationManager implements ContentManager {

	protected static final Log log = LogFactory.getLog(ETLConfigurationManager.class);


	private List<String> coreDDlProcedures = new ArrayList<String>();
	private List<String> addonDDlProcedures = new ArrayList<String>();
	private List<String> coreDMLProcedures = new ArrayList<String>();
	private List<String> addonDMLProcedures = new ArrayList<String>();
	private List<String> coreIncrementalUpdatesProcedures = new ArrayList<String>();
	private List<String> addonIncrementalUpdatesProcedures = new ArrayList<String>();
	private List<String> coreDatatoolDatabaseProcedures = new ArrayList<String>();
	private List<String> addonDatatoolDatabaseProcedures = new ArrayList<String>();


	/**
	 * @see org.openmrs.module.kenyacore.ContentManager#getPriority()
	 */
	@Override
	public int getPriority() {
		return 90;
	}

	/**
	 * @see org.openmrs.module.kenyacore.ContentManager#refresh()
	 */
	@Override
	public synchronized void refresh() {

		String recreateEtlsOnStartupConfig = Context.getAdministrationService().getGlobalProperty("kenyaemr.reacreate_etls_on_startup");

		if (recreateEtlsOnStartupConfig != null && recreateEtlsOnStartupConfig.equalsIgnoreCase("no")) {
			System.out.println("Skipping recreation of ETL tables. Please set the value of kenyaemr.reacreate_etls_on_startup global property to yes to enable recreation on startup");
			return;
		}

		ETLProcedureBuilder procedureBuilder = new ETLProcedureBuilder();
		procedureBuilder.buildProcedures();
		procedureBuilder.runDDL();
		procedureBuilder.runDML();
		procedureBuilder.runDatatoolProcedures();
		System.out.println("Completed refreshing ETL extensions ....");

		System.out.println("------------------------------------------------------------------------------");
	}


}