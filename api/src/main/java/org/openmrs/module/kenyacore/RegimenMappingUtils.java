package org.openmrs.module.kenyacore;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Form;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.EncounterService;
import org.openmrs.api.FormService;
import org.openmrs.api.context.Context;
import org.openmrs.ui.framework.SimpleObject;
import org.openmrs.util.OpenmrsUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;

public class RegimenMappingUtils {
    public static final String GP_IL_CONFIG_DIR = "kenyaemrIL.drugsMappingDirectory";
    public static final Locale LOCALE = Locale.ENGLISH;
    public static final String DRUG_REGIMEN_EDITOR_FORM = "da687480-e197-11e8-9f32-f2801f1b9fd1";
    public static final String DRUG_REGIMEN_EDITOR_ENCOUNTER_TYPE = "7dffc392-13e7-11e9-ab14-d663bd873d93";

    /**
     * TODO: remove this method once clean up in all modules
     * Gets mappings for KenyaEMR-Nascop codes drug mapping
     * The mapping file is a json array with the following structure:
     * {
     *  "nascop_code": "AF1A",
     *  "drug_name": "TDF+3TC+EFV",
     *  "concept_id": 1234,
     * }
     * @return json array
     */
    @Deprecated
    public static JSONArray getNacopCodesMapping(String string) {

        File configFile = OpenmrsUtil.getDirectoryInApplicationDataDirectory(Context.getAdministrationService().getGlobalProperty(GP_IL_CONFIG_DIR));
        String fullFilePath = configFile.getPath() + File.separator + "KenyaEMR_Nascop_Codes_Drugs_Map.json";
        JSONParser jsonParser = new JSONParser();
        try {
            //Read JSON file
            FileReader reader = new FileReader(fullFilePath);
            Object obj = jsonParser.parse(reader);
            JSONArray drugsMap = (JSONArray) obj;

            return drugsMap;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Reading content from bundled mapping json file for regimens in KenyaEMR
     * @return
     */
    public static JSONArray getNacopCodesMapping() {

        JSONParser jsonParser = new JSONParser();
        try {
            //Read JSON file
            Object obj = jsonParser.parse(readBundledRegimenMappingFile());
            JSONArray drugsMap = (JSONArray) obj;

            return drugsMap;

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Iterates through the mapping document and returns an item matching on key and value
     * @param key to match
     * @param value to match
     * @return JSONObject
     */
    public static JSONObject getDrugEntryByKeyAndValue(String key, String value) {

        JSONArray config = RegimenMappingUtils.getNacopCodesMapping();
        if (config != null) {
            for (int i = 0; i < config.size(); i++) {
                JSONObject o = (JSONObject) config.get(i);
                if (o.get(key).toString().equals(value)) {
                    return o;
                }
            }
        }
        return null;
    }

    /**
     * Gets drug object using drug name
     * @param drugName
     * @return
     */
    public static String getDrugNascopCodeByDrugNameAndRegimenLine(String drugName, String regimenLine) {

        if (StringUtils.isBlank(regimenLine) || StringUtils.isBlank(drugName)) {
            return null;
        }
        JSONArray config = RegimenMappingUtils.getNacopCodesMapping();
        if (config != null) {
            for (int i = 0; i < config.size(); i++) {
                JSONObject o = (JSONObject) config.get(i);
                if (o.get("drug_name").toString().equals(drugName)) {
                    JSONObject nascop_codes = (JSONObject) o.get("nascop_codes");
                    if (nascop_codes.get(regimenLine) != null) {
                        return nascop_codes.get(regimenLine).toString();
                    }
                }
            }
        }
        return null;
    }

    /**
     * Utility method for getting the data formatter
     * @param pattern
     * @return
     */
    public static SimpleDateFormat getSimpleDateFormat(String pattern) {
        return new SimpleDateFormat(pattern);
    }

    /**
     * Utility method that builds regimen events and returns a simple object
     * @param obsList
     * @param e
     * @return
     */
    public static SimpleObject buildRegimenChangeObject(Set<Obs> obsList, Encounter e) {

        String CURRENT_DRUGS = "1193AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
        String REASON_REGIMEN_STOPPED_CODED = "1252AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
        String REASON_REGIMEN_STOPPED_NON_CODED = "5622AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
        String DATE_REGIMEN_STOPPED = "1191AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
        String CURRENT_DRUG_NON_STANDARD ="1088AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
        String REGIMEN_LINE_CONCEPT ="163104AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";



        String regimen = null;
        String regimenShort = null;
        String regimenLine = null;
        String regimenUuid = null;
        String endDate = null;
        String startDate = e != null? getSimpleDateFormat("yyyy-MM-dd").format(e.getEncounterDatetime()) : "";
        Set<String> changeReason = new HashSet<String>();

        StringBuilder nonstandardRegimen = new StringBuilder();
        for(Obs obs:obsList) {

            if (obs.getConcept().getUuid().equals(CURRENT_DRUGS) ) {
                regimen = obs.getValueCoded() != null ? obs.getValueCoded().getFullySpecifiedName(LOCALE).getName() : "Unresolved Regimen name";
                try {
                    regimenShort = getRegimenNameFromRegimensXMLString(obs.getValueCoded().getUuid(), readBundledRegimenMappingFile());
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                regimenUuid = obs.getValueCoded() != null ? obs.getValueCoded().getUuid() : "";
            } else if (obs.getConcept().getUuid().equals(CURRENT_DRUG_NON_STANDARD) ) {
                nonstandardRegimen.append(obs.getValueCoded().getFullySpecifiedName(LOCALE).getName().toUpperCase() + "/");
                regimenUuid = obs.getValueCoded() != null ? obs.getValueCoded().getUuid() : "";
            } else if (obs.getConcept().getUuid().equals(REASON_REGIMEN_STOPPED_CODED)) {
                String reason = obs.getValueCoded() != null ?  obs.getValueCoded().getName().getName() : "";
                if (reason != null)
                    changeReason.add(reason);
            } else if (obs.getConcept().getUuid().equals(REASON_REGIMEN_STOPPED_NON_CODED)) {
                String reason = obs.getValueText();
                if (reason != null)
                    changeReason.add(reason);
            } else if (obs.getConcept() != null && obs.getConcept().getUuid().equals(DATE_REGIMEN_STOPPED)) {
                if(obs.getValueDatetime() != null){
                    endDate = getSimpleDateFormat("yyyy-MM-dd") .format(obs.getValueDatetime());
                }
            } else if (obs.getConcept().getUuid().equals(REGIMEN_LINE_CONCEPT) ) {
                regimenLine = obs.getValueText();
            }


        }
        if(nonstandardRegimen.length() > 0) {
            return SimpleObject.create(
                    "startDate", startDate,
                    "endDate", endDate != null? endDate : "",
                    "regimenShortDisplay", (nonstandardRegimen.toString()).substring(0,nonstandardRegimen.length() - 1) ,
                    "regimenLine", regimenLine != null ? regimenLine : "",
                    "regimenLongDisplay", (nonstandardRegimen.toString()).substring(0,nonstandardRegimen.length() - 1),
                    "changeReasons", changeReason,
                    "regimenUuid", regimenUuid,
                    "current",endDate != null ? false : true

            );
        }

        if(regimen != null) {
            return SimpleObject.create(
                    "startDate", startDate,
                    "endDate", endDate != null? endDate : "",
                    "regimenShortDisplay", regimenShort != null ? regimenShort : regimen,
                    "regimenLine", regimenLine != null ? regimenLine : "",
                    "regimenLongDisplay", regimen,
                    "changeReasons", changeReason,
                    "regimenUuid", regimenUuid,
                    "current",endDate != null ? false : true

            );
        }
        return SimpleObject.create(
                "startDate",  "",
                "endDate",  "",
                "regimenShortDisplay", "",
                "regimenLine",  "",
                "regimenLongDisplay", "",
                "changeReasons", "",
                "regimenUuid", "",
                "current",""

        );

        //return null;
    }

    /**
     * Utility method that gets the printable/common regimen name from a concept reference
     * @param conceptRef
     * @param regimenJson
     * @return
     * @throws IOException
     */
    public static String getRegimenNameFromRegimensXMLString(String conceptRef, String regimenJson) throws IOException {
        System.out.println("Reading from bundled json mapping");
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode conf = (ArrayNode) mapper.readTree(regimenJson);

        for (Iterator<JsonNode> it = conf.iterator(); it.hasNext(); ) {
            ObjectNode node = (ObjectNode) it.next();
            if (node.get("conceptRef").asText().equals(conceptRef)) {
                return node.get("drug_name").asText();
            }
        }

        return "Unknown";
    }

    /**
     * Returns the last regimen editor encounter for a given patient and program
     * @param patient
     * @param category
     * @return
     */
    public static Encounter getLastEncounterForProgram (Patient patient, String category) {

        FormService formService = Context.getFormService();
        EncounterService encounterService = Context.getEncounterService();
        String ARV_TREATMENT_PLAN_EVENT_CONCEPT = "1255AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
        String TB_TREATMENT_PLAN_CONCEPT = "1268AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
        List<SimpleObject> history = new ArrayList<SimpleObject>();
        String categoryConceptUuid = category.equals("ARV")? ARV_TREATMENT_PLAN_EVENT_CONCEPT : TB_TREATMENT_PLAN_CONCEPT;

        EncounterType et = encounterService.getEncounterTypeByUuid(DRUG_REGIMEN_EDITOR_ENCOUNTER_TYPE);
        Form form = formService.getFormByUuid(DRUG_REGIMEN_EDITOR_FORM);

        List<Encounter> encs = AllEncounters(patient, et, form);
        NavigableMap<Date, Encounter> programEncs = new TreeMap<Date, Encounter>();
        for (Encounter e : encs) {
            if (e != null) {
                Set<Obs> obs = e.getObs();
                if (programEncounterMatching(obs, categoryConceptUuid)) {
                    programEncs.put(e.getEncounterDatetime(), e);
                }
            }
        }
        if (!programEncs.isEmpty()) {
            return programEncs.lastEntry().getValue();
        }
        return null;
    }

    /**
     * Retrieves the first encounter when a patient was started on drugs
     * @param patient
     * @param category
     * @return
     */
    public static Encounter getFirstEncounterForProgram (Patient patient, String category) {

        FormService formService = Context.getFormService();
        EncounterService encounterService = Context.getEncounterService();
        String ARV_TREATMENT_PLAN_EVENT_CONCEPT = "1255AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
        String TB_TREATMENT_PLAN_CONCEPT = "1268AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
        List<SimpleObject> history = new ArrayList<SimpleObject>();
        String categoryConceptUuid = category.equals("ARV")? ARV_TREATMENT_PLAN_EVENT_CONCEPT : TB_TREATMENT_PLAN_CONCEPT;

        EncounterType et = encounterService.getEncounterTypeByUuid(DRUG_REGIMEN_EDITOR_ENCOUNTER_TYPE);
        Form form = formService.getFormByUuid(DRUG_REGIMEN_EDITOR_FORM);

        List<Encounter> encs = AllEncounters(patient, et, form);
        NavigableMap<Date, Encounter> programEncs = new TreeMap<Date, Encounter>();
        for (Encounter e : encs) {
            if (e != null) {
                Set<Obs> obs = e.getObs();
                if (programEncounterMatching(obs, categoryConceptUuid)) {
                    programEncs.put(e.getEncounterDatetime(), e);
                }
            }
        }
        if (!programEncs.isEmpty()) {
            return programEncs.firstEntry().getValue();
        }
        return null;
    }

    /**
     * Returns all encounters entered using a particular form
     * @param patient
     * @param type
     * @param form
     * @return
     */
    public static List<Encounter> AllEncounters(Patient patient, EncounterType type, Form form) {
        List<Encounter> encounters = Context.getEncounterService().getEncounters(patient, null, null, null, Collections.singleton(form), Collections.singleton(type), null, null, null, false);
        return encounters;
    }

    /**
     * A helper method that helps scan through regimen event encounter to check for the program
     * It helps distinguish events for particular patient programs i.e. HIV, TB, PMTCT, etc
     * @param obs
     * @param conceptUuidToMatch is the program UUID
     * @return
     */
    public static boolean programEncounterMatching(Set<Obs> obs, String conceptUuidToMatch) {
        for (Obs o : obs) {
            if (o.getConcept().getUuid().equals(conceptUuidToMatch)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Reads bundled regimen mapping file
     * @return
     */
    public static String readBundledRegimenMappingFile() {
        InputStream stream = RegimenMappingUtils.class.getClassLoader().getResourceAsStream("KenyaEMR_Nascop_Codes_Drugs_Map.json");
        ObjectMapper mapper = new ObjectMapper();
        try {
            ArrayNode result = mapper.readValue(stream, ArrayNode.class);
            return result.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
