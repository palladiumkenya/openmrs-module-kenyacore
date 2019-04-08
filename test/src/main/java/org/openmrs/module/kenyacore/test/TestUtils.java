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

import org.codehaus.jackson.map.ObjectMapper;
import org.openmrs.CareSetting;
import org.openmrs.Concept;
import org.openmrs.DrugOrder;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Form;
import org.openmrs.FreeTextDosingInstructions;
import org.openmrs.GlobalProperty;
import org.openmrs.Location;
import org.openmrs.LocationAttribute;
import org.openmrs.LocationAttributeType;
import org.openmrs.Obs;
import org.openmrs.Order;
import org.openmrs.OrderFrequency;
import org.openmrs.OrderType;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PatientProgram;
import org.openmrs.Person;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonAttributeType;
import org.openmrs.Privilege;
import org.openmrs.Program;
import org.openmrs.Provider;
import org.openmrs.Relationship;
import org.openmrs.RelationshipType;
import org.openmrs.SimpleDosingInstructions;
import org.openmrs.Visit;
import org.openmrs.VisitType;
import org.openmrs.api.OrderContext;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.customdatatype.CustomDatatype;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import static org.junit.Assert.assertNotNull;

/**
 * Utility methods for unit tests
 */
public class TestUtils {

	/**
	 * Convenience method to create a new date
	 * @param year the year
	 * @param month the month
	 * @param day the day
	 * @return the date
	 * @throws IllegalArgumentException if date values are not valid
	 */
	public static Date date(int year, int month, int day) {
		return date(year, month, day, 0, 0, 0);
	}

	/**
	 * Convenience method to create a new date with time
	 * @param year the year
	 * @param month the month
	 * @param day the day
	 * @param hour the hour
	 * @param minute the minute
	 * @param second the second
	 * @return the date
	 * @throws IllegalArgumentException if date values are not valid
	 */
	public static Date date(int year, int month, int day, int hour, int minute, int second) {
		Calendar cal = new GregorianCalendar(year, month - 1, day, hour, minute, second);
		cal.setLenient(false);
		return cal.getTime();
	}

	/**
	 * Gets a patient from the test data
	 * @param id the patient id
	 * @return the patient
	 * @throws IllegalArgumentException if patient doesn't exist
	 */
	public static Patient getPatient(int id) {
		Patient patient = Context.getPatientService().getPatient(id);
		if (patient == null) {
			throw new IllegalArgumentException("No such patient with id " + id);
		}
		return patient;
	}

	/**
	 * create and save a patient
	 * @param @param patient the patient
	 * @return the saved patient
	 */
	public static Patient savePatient(Patient patient) {
		PatientService patientService = Context.getPatientService();
		return patientService.savePatient(patient);
	}

	/**
	 * Create and save an encounter
	 * @param patient the patient
	 * @param type the encounter type
	 * @param date the encounter date
	 * @return the saved encounter
	 */
	public static Encounter saveEncounter(Patient patient, EncounterType type, Date date, Obs... obss) {
		return saveEncounter(patient, type, null, date, obss);
	}

	/**
	 * Create and save an encounter
	 * @param patient the patient
	 * @param form the form
	 * @param date the encounters date
	 * @return the saved encounter
	 */
	public static Encounter saveEncounter(Patient patient, Form form, Date date, Obs... obss) {
		return saveEncounter(patient, Context.getEncounterService().getEncounterType(2), form, date, obss);
	}

	/**
	 * Create and save an encounter
	 * @param patient the patient
	 * @param type the encounter type
	 * @param form the form
	 * @param date the encounters date
	 * @return the saved encounter
	 */
	public static Encounter saveEncounter(Patient patient, EncounterType type, Form form, Date date, Obs... obss) {
		Encounter encounter = new Encounter();
		encounter.setPatient(patient);
		encounter.setProvider(Context.getUserService().getUser(1)); // Super user
		encounter.setLocation(Context.getLocationService().getLocation(1)); // Unknown Location
		encounter.setEncounterType(type);
		encounter.setForm(form);
		encounter.setEncounterDatetime(date);
		Context.getEncounterService().saveEncounter(encounter);

		for (Obs obs : obss) {
			encounter.addObs(obs);
		}

		return Context.getEncounterService().saveEncounter(encounter);
	}

	/**
	 * Create and save a visit
	 * @param patient the patient
	 * @param type the visit type
	 * @param start the start date
	 * @param stop the stop date
	 * @return the aved visit
	 */
	public static Visit saveVisit(Patient patient, VisitType type, Date start, Date stop, Encounter... encounters) {
		Visit visit = new Visit();
		visit.setPatient(patient);
		visit.setVisitType(type);
		visit.setStartDatetime(start);
		visit.setStopDatetime(stop);
		Context.getVisitService().saveVisit(visit);

		for (Encounter encounter : encounters) {
			visit.addEncounter(encounter);
		}

		return Context.getVisitService().saveVisit(visit);
	}

	/**
	 * Enroll a patient in a program
	 * @param patient the patient
	 * @param program the program
	 * @param date the enroll date
	 * @return the patient program
	 */
	public static PatientProgram enrollInProgram(Patient patient, Program program, Date date) {
		return enrollInProgram(patient, program, date, null);
	}

	/**
	 * Enroll a patient in a program
	 * @param patient the patient
	 * @param program the program
	 * @param start the enroll date
	 * @param completed the completed date
	 * @return the patient program
	 */
	public static PatientProgram enrollInProgram(Patient patient, Program program, Date start, Date completed) {
		PatientProgram pp = new PatientProgram();
		pp.setPatient(patient);
		pp.setProgram(program);
		pp.setDateEnrolled(start);
		pp.setDateCompleted(completed);
		return Context.getProgramWorkflowService().savePatientProgram(pp);
	}

	/**
	 * Saves a drug order
	 * @param patient the patient
	 * @param concept the drug concept
	 * @param start the start date
	 * @param end the end date
	 * @return the drug order
	 *//*
	public static DrugOrder saveDrugOrder(Patient patient, Concept concept, Date start, Date end) {

		CareSetting outpatient = Context.getOrderService().getCareSettingByName("OUTPATIENT");
		OrderType drugOrderType = Context.getOrderService().getOrderTypeByUuid(OrderType.DRUG_ORDER_TYPE_UUID);

		DrugOrder order = new DrugOrder();
		//order.setOrderType(drugOrderType);
		//order.setOrderType(Context.getOrderService().getOrderType(2));
		order.setPatient(patient);
		List<Provider> provider = (List<Provider>) Context.getProviderService().getProvidersByPerson(Context.getUserService().getUser(1).getPerson());
		//order.setEncounter(buildEncounter());
		order.setOrderer(provider.get(0));
		order.setConcept(concept);
		order.setDateActivated(start);

		if (end != null) {
			order.setAction(Order.Action.DISCONTINUE);
		}

		OrderContext orderContext = new OrderContext();
		orderContext.setCareSetting(outpatient);
		orderContext.setOrderType(drugOrderType);
		return (DrugOrder) Context.getOrderService().saveOrder(order, orderContext);
	}*/

	/**
	 * Saves a drug order
	 * @param patient the patient
	 * @param concept the drug concept
	 * @param start the start date
	 * @param end the end date
	 * @return the drug order
	 */
	public static DrugOrder saveDrugOrder(Patient patient, Concept concept, Date start, Date end) {

		CareSetting outpatient = Context.getOrderService().getCareSettingByName("OUTPATIENT");
		OrderType drugOrderType = Context.getOrderService().getOrderTypeByUuid(OrderType.DRUG_ORDER_TYPE_UUID);

		DrugOrder order = new DrugOrder();
		order.setPatient(patient);
		List<Provider> provider = (List<Provider>) Context.getProviderService().getProvidersByPerson(Context.getUserService().getUser(1).getPerson());
		Encounter e = Context.getEncounterService().getEncounter(3);
		order.setEncounter(e);
		order.setOrderer(provider.get(0));
		order.setConcept(concept);
		order.setDateActivated(start);
		order.setDose(2.0);
		order.setDoseUnits(Context.getConceptService().getConcept(51));
		order.setRoute(Context.getConceptService().getConcept(22));
		OrderFrequency orderFrequency = Context.getOrderService().getOrderFrequency(1);
		order.setFrequency(orderFrequency);


		if (end != null) {
			order.setAction(Order.Action.DISCONTINUE);
		}

		OrderContext orderContext = new OrderContext();
		orderContext.setCareSetting(outpatient);
		orderContext.setOrderType(drugOrderType);
		return (DrugOrder) Context.getOrderService().saveOrder(order, orderContext);
	}

	/**
	 * Saves a drug order
	 * @param patient the patient
	 * @param
	 * @param start the start date
	 * @param end the end date
	 * @return the drug order
	 */
	public static DrugOrder saveDrugOrder(Patient patient, Concept conceptOrdered, Concept doseUnit, Date start, Date end, Encounter encounter) {

		OrderType drugOrderType = Context.getOrderService().getOrderTypeByUuid(OrderType.DRUG_ORDER_TYPE_UUID);
		CareSetting careSetting = Context.getOrderService().getCareSetting(2);
		//place drug order
		DrugOrder order = new DrugOrder();/*
		Encounter encounter = encounterService.getEncounter(3);*/
		order.setEncounter(encounter);
		order.setPatient(patient);
		order.setConcept(conceptOrdered);
		order.setCareSetting(careSetting);
		order.setOrderer(Context.getProviderService().getProvider(1));
		order.setDateActivated(start);
		order.setOrderType(drugOrderType);
		order.setDosingType(FreeTextDosingInstructions.class);
		order.setInstructions("None");
		order.setDosingInstructions("Test Instruction");
		return (DrugOrder) Context.getOrderService().saveOrder(order, null);
	}

	/**
	 * Saves a drug order
	 * @param patient the patient
	 * @param
	 * @param start the start date
	 * @param end the end date
	 * @return the drug order
	 */
	public static DrugOrder saveDrugOrderWithFreeTestInstructions(Patient patient, Concept conceptOrdered, Date start, Encounter encounter) {

		OrderType drugOrderType = Context.getOrderService().getOrderTypeByUuid(OrderType.DRUG_ORDER_TYPE_UUID);
		CareSetting careSetting = Context.getOrderService().getCareSetting(2);
		//place drug order
		DrugOrder order = new DrugOrder();
		order.setEncounter(encounter);
		order.setPatient(patient);
		order.setConcept(conceptOrdered);
		order.setCareSetting(careSetting);
		order.setOrderer(Context.getProviderService().getProvider(1));
		order.setDateActivated(start);
		order.setOrderType(drugOrderType);
		order.setDosingType(FreeTextDosingInstructions.class);
		order.setInstructions("None");
		order.setDosingInstructions("Test Instruction");
		return (DrugOrder) Context.getOrderService().saveOrder(order, null);
	}

	public static DrugOrder saveDrugOrder(Patient patient, Concept concept, Date start, Date end, Encounter encounter) {

		CareSetting outpatient = Context.getOrderService().getCareSettingByName("OUTPATIENT");
		OrderType drugOrderType = Context.getOrderService().getOrderTypeByUuid(OrderType.DRUG_ORDER_TYPE_UUID);

		DrugOrder order = new DrugOrder();
		order.setPatient(patient);
		List<Provider> provider = (List<Provider>) Context.getProviderService().getProvidersByPerson(Context.getUserService().getUser(1).getPerson());
		order.setEncounter(encounter);
		order.setOrderer(provider.get(0));
		order.setConcept(concept);
		order.setDateActivated(start);
		order.setDose(2.0);
		order.setDoseUnits(Context.getConceptService().getConcept(51));
		order.setRoute(Context.getConceptService().getConcept(22));
		OrderFrequency orderFrequency = Context.getOrderService().getOrderFrequency(1);
		order.setFrequency(orderFrequency);


		if (end != null) {
			order.setAction(Order.Action.DISCONTINUE);
		}

		OrderContext orderContext = new OrderContext();
		orderContext.setCareSetting(outpatient);
		orderContext.setOrderType(drugOrderType);
		return (DrugOrder) Context.getOrderService().saveOrder(order, orderContext);
	}

	private static Encounter buildEncounter() {
		// First, create a new Encounter
		Encounter enc = new Encounter();
		enc.setLocation(Context.getLocationService().getLocation(1));
		enc.setEncounterType(Context.getEncounterService().getEncounterType(1));
		enc.setEncounterDatetime(new Date());
		enc.setPatient(Context.getPatientService().getPatient(6));
		enc.addProvider(Context.getEncounterService().getEncounterRole(1), Context.getProviderService().getProvider(1));
		return enc;
	}

	/**
	 * Saves an untyped global property
	 * @param property the property name
	 * @param value the property value
	 * @return the global property
	 */
	public static GlobalProperty saveGlobalProperty(String property, String value) {
		GlobalProperty gp = Context.getAdministrationService().getGlobalPropertyObject(property);
		if (gp == null) {
			gp = new GlobalProperty();
			gp.setProperty(property);
		}
		gp.setPropertyValue(String.valueOf(value));
		return Context.getAdministrationService().saveGlobalProperty(gp);
	}

	/**
	 * Saves a typed global property
	 * @param property the property name
	 * @param value the property value
	 * @return the global property
	 */
	public static GlobalProperty saveGlobalProperty(String property, Object value, Class<? extends CustomDatatype> datatypeClass) {
		GlobalProperty gp = Context.getAdministrationService().getGlobalPropertyObject(property);
		if (gp == null) {
			gp = new GlobalProperty();
			gp.setProperty(property);
		}
		if (value != null) {
			gp.setValue(value);
		}
		gp.setDatatypeClassname(datatypeClass.getName());
		return Context.getAdministrationService().saveGlobalProperty(gp);
	}

	/**
	 * Saves a location attribute
	 * @param location the location
	 * @param type the attribute type
	 * @param value the attribute value
	 * @return the location attribute
	 */
	public static LocationAttribute saveLocationAttribute(Location location, LocationAttributeType type, Object value) {
		LocationAttribute attr = new LocationAttribute();
		attr.setOwner(location);
		attr.setAttributeType(type);
		attr.setValue(value);

		location.addAttribute(attr);

		Context.getLocationService().saveLocation(location);
		return attr;
	}

	/**
	 * Saves a relationship
	 * @param personA the person A
	 * @param type the relationship type
	 * @param personB the person B
	 * @return the relationship
	 */
	public static Relationship saveRelationship(Person personA, RelationshipType type, Person personB) {
		Relationship relationship = new Relationship();
		relationship.setPersonA(personA);
		relationship.setRelationshipType(type);
		relationship.setPersonB(personB);
		return Context.getPersonService().saveRelationship(relationship);
	}

	/**
	 * Create and save a patient identifier
	 * @param patient the patient
	 * @param type the identifier type
	 * @param value the identifier value
	 * @return the patient identifier
	 */
	public static PatientIdentifier savePatientIdentifier(Patient patient, PatientIdentifierType type, String value) {
		PatientIdentifier pid = new PatientIdentifier();
		pid.setPatient(patient);
		pid.setIdentifierType(type);
		pid.setIdentifier(value);
		pid.setLocation(Context.getLocationService().getLocation(1)); // Unknown Location

		patient.addIdentifier(pid);

		return Context.getPatientService().savePatientIdentifier(pid);
	}

	/**
	 * Saves a person attribute
	 * @param person the person
	 * @param type the attribute type
	 * @param value the attribute value
	 * @return the person attribute
	 */
	public static PersonAttribute savePersonAttribute(Person person, PersonAttributeType type, Object value) {
		PersonAttribute attr = new PersonAttribute();
		attr.setPerson(person);
		attr.setAttributeType(type);
		attr.setValue(String.valueOf(value));

		person.addAttribute(attr);

		Context.getPersonService().savePerson(person);
		return attr;
	}

	/**
	 * Save a numeric obs
	 * @param patient the patient
	 * @param concept the concept
	 * @param val the numeric value
	 * @param date the date
	 * @return the obs
	 */
	public static Obs saveObs(Patient patient, Concept concept, double val, Date date) {
		Obs obs = new Obs(patient, concept, date, null);
		obs.setValueNumeric(val);
		return Context.getObsService().saveObs(obs, null);
	}

	/**
	 * Save a datetime obs
	 * @param patient the patient
	 * @param concept the concept
	 * @param val the datetime value
	 * @param date the date
	 * @return the obs
	 */
	public static Obs saveObs(Patient patient, Concept concept, Date val, Date date) {
		Obs obs = new Obs(patient, concept, date, null);
		obs.setValueDatetime(val);
		return Context.getObsService().saveObs(obs, null);
	}

	/**
	 * Save a coded obs
	 * @param patient the patient
	 * @param concept the concept
	 * @param val the datetime value
	 * @param date the date
	 * @return the obs
	 */
	public static Obs saveObs(Patient patient, Concept concept, Concept val, Date date) {
		Obs obs = new Obs(patient, concept, date, null);
		obs.setValueCoded(val);
		return Context.getObsService().saveObs(obs, null);
	}

	/**
	 * Save a privilege
	 * @param privilege the privilege
	 * @param description the description
	 * @return the privilege
	 */
	public static Privilege savePrivilege(String privilege, String description) {
		Privilege obj = new Privilege(privilege, description);
		return Context.getUserService().savePrivilege(obj);
	}

	/**
	 * Modifies a constant in a constants class. If the constant is final and not based on a runtime expression, then
	 * it's value will have been inlined by the compiler and this method will have no effect.
	 * @param constantsClass the class of constants
	 * @param fieldName the field name of the constant
	 * @param newValue the new value for the constant
	 * @throws Exception if field not found or couldn't be modified
	 */
	public static void modifyConstant(Class<?> constantsClass, String fieldName, Object newValue) throws Exception {
		Field field = constantsClass.getDeclaredField(fieldName);
		field.setAccessible(true);

		int existingModifiers = field.getModifiers();

		// Remove final modifier from field
		Field modifiersField = Field.class.getDeclaredField("modifiers");
		modifiersField.setAccessible(true);
		modifiersField.setInt(field, existingModifiers & ~Modifier.FINAL);

		field.set(null, newValue);

		// Reset previous modifiers
		modifiersField.setInt(field, existingModifiers);
	}

	/**
	 * Gets the target of a proxy object
	 * @param proxy the proxy
	 * @param <T> the target type
	 * @return the target of the proxy
	 * @throws Exception
	 */
	public static <T> T getProxyTarget(Object proxy) throws Exception {
		while ((AopUtils.isJdkDynamicProxy(proxy))) {
			return getProxyTarget(((Advised) proxy).getTargetSource().getTarget());
		}
		return (T) proxy;
	}

	/**
	 * Prints an object as JSON
	 * @param object the object
	 * @throws java.io.IOException
	 */
	public static void printJson(Object object) {
		try {
			System.out.println(new ObjectMapper().writeValueAsString(object));
		}
		catch (IOException ex) {
			ex.printStackTrace();
		}
	}
}