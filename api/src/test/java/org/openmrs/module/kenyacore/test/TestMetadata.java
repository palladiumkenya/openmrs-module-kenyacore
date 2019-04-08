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

import org.openmrs.module.metadatadeploy.bundle.AbstractMetadataBundle;
import org.springframework.stereotype.Component;

import static org.openmrs.module.metadatadeploy.bundle.CoreConstructors.encounterType;
import static org.openmrs.module.metadatadeploy.bundle.CoreConstructors.form;

/**
 * Metadata for unit tests
 */
@Component
public class TestMetadata extends AbstractMetadataBundle {

	public static final class _EncounterType {
		public static final String REGISTRATION = "E8DC105B-0A3F-4931-96D8-B049F2CC97E5";
		public static final String CONSULTATION = "D24D3F87-6150-4A07-9336-B0E98E58FA49";
		public static final String LAB_RESULTS = "89C20A33-133B-4351-9166-2B453CD8E872";
	}

	public static final class _Form {
		public static final String REGISTRATION = "80469953-D3EE-47CC-B178-A2B90C5CF64D";
		public static final String CONSULTATION = "6B33F0C3-4DBF-40F2-A32D-93E788592301";
		public static final String PROGRESS_NOTE = "4F163D44-7A65-4492-8A9C-C91F478A5D02";
		public static final String LAB_RESULTS = "51647632-D496-4AE8-9FDA-64BC62B9089E";
	}

	/**
	 * @see org.openmrs.module.metadatadeploy.bundle.AbstractMetadataBundle#install()
	 */
	@Override
	public void install() {
		install(encounterType("Registration", "Description", _EncounterType.REGISTRATION));
		install(encounterType("Consultation", "Description", _EncounterType.CONSULTATION));
		install(encounterType("Lab Results", "Description", _EncounterType.LAB_RESULTS));

		install(form("Registration", "", _EncounterType.REGISTRATION, "1.0", _Form.REGISTRATION));
		install(form("Consultation", "", _EncounterType.CONSULTATION, "1.0", _Form.CONSULTATION));
		install(form("Progress note", "", _EncounterType.CONSULTATION, "1.0", _Form.PROGRESS_NOTE));
		install(form("Lab Results", "", _EncounterType.LAB_RESULTS, "1.0", _Form.LAB_RESULTS));
	}
}