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

package org.openmrs.module.kenyacore.form;

import org.openmrs.Form;
import org.openmrs.FormResource;
import org.openmrs.api.context.Context;
import org.openmrs.customdatatype.datatype.FreeTextDatatype;
import org.openmrs.module.htmlformentry.HtmlForm;
import org.openmrs.module.htmlformentry.HtmlFormEntryUtil;
import org.openmrs.module.kenyacore.UiResource;
import org.openmrs.ui.framework.resource.ResourceFactory;
import org.openmrs.util.OpenmrsUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Form utility methods
 */
public class FormUtils {

	protected static final String RESOURCE_HFE_XML_PATH = "hfeXmlPath";

	/**
	 * Gets the XML resource path of the given form (null if form doesn't have an XML resource)
	 * @param form the form
	 * @return the XML resource
	 */
	public static UiResource getFormXmlResource(Form form) {
		FormResource resource = Context.getFormService().getFormResource(form, RESOURCE_HFE_XML_PATH);
		return resource != null ? new UiResource((String) resource.getValue()) : null;
	}

	/**
	 * Set the XML resource path of the given form
	 * @param form the form
	 * @param xmlResource the UI resource
	 */
	public static void setFormXmlResource(Form form, UiResource xmlResource) {
		FormResource resXmlPath = Context.getFormService().getFormResource(form, RESOURCE_HFE_XML_PATH);

		if (resXmlPath == null) {
			resXmlPath = new FormResource();
			resXmlPath.setForm(form);
			resXmlPath.setName(RESOURCE_HFE_XML_PATH);
			resXmlPath.setDatatypeClassname(FreeTextDatatype.class.getName());
		}

		resXmlPath.setValue(xmlResource.toString());

		Context.getFormService().saveFormResource(resXmlPath);
	}

	/**
	 * Gets an HTML from a form
	 * @param form the form
	 * @param resourceFactory the resourceFactory
	 * @return the Html form
	 * @throws RuntimeException if form has no xml path or path is invalid
	 */
	public static HtmlForm getHtmlForm(Form form, ResourceFactory resourceFactory) throws IOException {
		UiResource xmlResource = getFormXmlResource(form);

		if (xmlResource == null) {
			// Look in the database
			HtmlForm hf = HtmlFormEntryUtil.getService().getHtmlFormByForm(form);
			if (hf != null) {
				return hf;
			}

			throw new RuntimeException("Form has no XML path or persisted html form");
		}

		String xml = resourceFactory.getResourceAsString(xmlResource.getProvider(), "htmlforms/" + xmlResource.getPath());

		if (xml == null) {
			throw new RuntimeException("Form XML could not be loaded from path " + xmlResource + "");
		}

		HtmlForm hf = new HtmlForm();
		hf.setForm(form);
		hf.setCreator(form.getCreator());
		hf.setDateCreated(form.getDateCreated());
		hf.setChangedBy(form.getChangedBy());
		hf.setDateChanged(form.getDateChanged());
		hf.setXmlData(xml);
		return hf;
	}

	/**
	 * Renders an HTML tag
	 * @param name the tag name
	 * @param attributes the tag attributes
	 * @param closed whether tag should be closed
	 * @return start tag html
	 */
	public static String htmlTag(String name, Map<String, Object> attributes, boolean closed) {
		List<String> items = new ArrayList<String>();
		items.add(name);

		if (attributes != null) {
			for (Map.Entry<String, Object> attribute : attributes.entrySet()) {
				String val = attribute.getValue().toString().replace("\"", "&#34;");
				items.add(attribute.getKey() + "=\"" + val + "\"");
			}
		}

		return "<" + OpenmrsUtil.join(items, " ") + (closed ? " />" : ">");
	}
}