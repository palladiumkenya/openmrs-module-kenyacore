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
    public static final String TEST_ORDER_TYPE_UUID = "1814ee89-2abf-42d7-920b-d138740d56d4";

    /**
     * Gets mappings for KenyaEMR-Nascop codes drug mapping
     * The mapping file is a json array with the following structure:
     * {
     *  "nascop_code": "AF1A",
     *  "drug_name": "TDF+3TC+EFV",
     *  "concept_id": 1234,
     *  "drug_type":"G",
     *  "regimen_line":"First Line"
     * }
     * @return json array
     */
    public static JSONArray getNacopCodesMapping() {

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
     * Helper method for getting a config entry by concept_id
     * @param conceptId
     * @return
     */
    public static JSONObject getDrugEntryByConceptId(int conceptId) {

        JSONArray config = RegimenMappingUtils.getNacopCodesMapping();
        if (config != null) {
            for (int i = 0; i < config.size(); i++) {
                JSONObject o = (JSONObject) config.get(i);
                if ((Integer) o.get("concept_id") == conceptId) {
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
                    regimenShort = getRegimenNameFromRegimensXMLString(obs.getValueCoded().getUuid(), getRegimenConceptJson());
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

        ObjectMapper mapper = new ObjectMapper();
        ArrayNode conf = (ArrayNode) mapper.readTree(regimenJson);

        for (Iterator<JsonNode> it = conf.iterator(); it.hasNext(); ) {
            ObjectNode node = (ObjectNode) it.next();
            if (node.get("conceptRef").asText().equals(conceptRef)) {
                return node.get("name").asText();
            }
        }

        return "Unknown";
    }

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

    public static List<Encounter> AllEncounters(Patient patient, EncounterType type, Form form) {
        List<Encounter> encounters = Context.getEncounterService().getEncounters(patient, null, null, null, Collections.singleton(form), Collections.singleton(type), null, null, null, false);
        return encounters;
    }

    public static boolean programEncounterMatching(Set<Obs> obs, String conceptUuidToMatch) {
        for (Obs o : obs) {
            if (o.getConcept().getUuid().equals(conceptUuidToMatch)) {
                return true;
            }
        }
        return false;
    }
    /**
     * TODO: Read this from the json file provided for drug mapping
     * JSON version of the KenyaEMR drug mapping
     * @return
     */
    public static String getRegimenConceptJson() {
        String json = "[\n" +
                "  {\n" +
                "    \"name\": \"TDF/3TC/NVP\",\n" +
                "    \"conceptRef\": \"162565AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\n" +
                "    \"regimenLine\": \"adult_first\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"name\": \"TDF/3TC/EFV\",\n" +
                "    \"conceptRef\": \"164505AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\n" +
                "    \"regimenLine\": \"adult_first\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"name\": \"AZT/3TC/NVP\",\n" +
                "    \"conceptRef\": \"1652AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\n" +
                "    \"regimenLine\": \"adult_first\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"name\": \"AZT/3TC/EFV\",\n" +
                "    \"conceptRef\": \"160124AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\n" +
                "    \"regimenLine\": \"adult_first\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"name\": \"D4T/3TC/NVP\",\n" +
                "    \"conceptRef\": \"792AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\n" +
                "    \"regimenLine\": \"adult_first\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"name\": \"D4T/3TC/EFV\",\n" +
                "    \"conceptRef\": \"160104AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\n" +
                "    \"regimenLine\": \"adult_first\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"name\": \"TDF/3TC/AZT\",\n" +
                "    \"conceptRef\": \"98e38a9c-435d-4a94-9b66-5ca524159d0e\",\n" +
                "    \"regimenLine\": \"adult_first\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"name\": \"AZT/3TC/DTG\",\n" +
                "    \"conceptRef\": \"6dec7d7d-0fda-4e8d-8295-cb6ef426878d\",\n" +
                "    \"regimenLine\": \"adult_first\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"name\": \"TDF/3TC/DTG\",\n" +
                "    \"conceptRef\": \"9fb85385-b4fb-468c-b7c1-22f75834b4b0\",\n" +
                "    \"regimenLine\": \"adult_first\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"name\": \"ABC/3TC/DTG\",\n" +
                "    \"conceptRef\": \"4dc0119b-b2a6-4565-8d90-174b97ba31db\",\n" +
                "    \"regimenLine\": \"adult_first\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"name\": \"AZT/3TC/LPV/r\",\n" +
                "    \"conceptRef\": \"162561AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\n" +
                "    \"regimenLine\": \"adult_second\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"name\": \"AZT/3TC/ATV/r\",\n" +
                "    \"conceptRef\": \"164511AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\n" +
                "    \"regimenLine\": \"adult_second\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"name\": \"TDF/3TC/LPV/r\",\n" +
                "    \"conceptRef\": \"162201AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\n" +
                "    \"regimenLine\": \"adult_second\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"name\": \"TDF/3TC/ATV/r\",\n" +
                "    \"conceptRef\": \"164512AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\n" +
                "    \"regimenLine\": \"adult_second\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"name\": \"D4T/3TC/LPV/r\",\n" +
                "    \"conceptRef\": \"162560AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\n" +
                "    \"regimenLine\": \"adult_second\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"name\": \"AZT/TDF/3TC/LPV/r\",\n" +
                "    \"conceptRef\": \"c421d8e7-4f43-43b4-8d2f-c7d4cfb976a4\",\n" +
                "    \"regimenLine\": \"adult_second\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"name\": \"ETR/RAL/DRV/RTV\",\n" +
                "    \"conceptRef\": \"337b6cfd-9fa7-47dc-82b4-d479c39ef355\",\n" +
                "    \"regimenLine\": \"adult_second\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"name\": \"ETR/TDF/3TC/LPV/r\",\n" +
                "    \"conceptRef\": \"7a6c51c4-2b68-4d5a-b5a2-7ba420dde203\",\n" +
                "    \"regimenLine\": \"adult_second\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"name\": \"ABC/3TC/LPV/r\",\n" +
                "    \"conceptRef\": \"162200AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\n" +
                "    \"regimenLine\": \"adult_second\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"name\": \"ABC/3TC/ATV/r\",\n" +
                "    \"conceptRef\": \"dddd9cf2-2b9c-4c52-84b3-38cfe652529a\",\n" +
                "    \"regimenLine\": \"adult_second\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"name\": \"ABC/3TC/LPV/r\",\n" +
                "    \"conceptRef\": \"162200AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\n" +
                "    \"regimenLine\": \"child_first\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"name\": \"ABC/3TC/NVP\",\n" +
                "    \"conceptRef\": \"162199AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\n" +
                "    \"regimenLine\": \"child_first\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"name\": \"ABC/3TC/EFV\",\n" +
                "    \"conceptRef\": \"162563AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\n" +
                "    \"regimenLine\": \"child_first\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"name\": \"AZT/3TC/ABC\",\n" +
                "    \"conceptRef\": \"817AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\n" +
                "    \"regimenLine\": \"child_first\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"name\": \"D4T/3TC/ABC\",\n" +
                "    \"conceptRef\": \"b9fea00f-e462-4ea5-8d40-cc10e4be697e\",\n" +
                "    \"regimenLine\": \"child_first\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"name\": \"TDF/ABC/LPV/r\",\n" +
                "    \"conceptRef\": \"162562AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\n" +
                "    \"regimenLine\": \"child_first\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"name\": \"ABC/DDI/LPV/r\",\n" +
                "    \"conceptRef\": \"162559AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\n" +
                "    \"regimenLine\": \"child_first\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"name\": \"ABC/TDF/3TC/LPV/r\",\n" +
                "    \"conceptRef\": \"077966a6-4fbd-40ce-9807-2d5c2e8eb685\",\n" +
                "    \"regimenLine\": \"child_first\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"name\": \"RHZE\",\n" +
                "    \"conceptRef\": \"1675AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\n" +
                "    \"regimenLine\": \"adult_intensive\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"name\": \"RHZ\",\n" +
                "    \"conceptRef\": \"768AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\n" +
                "    \"regimenLine\": \"adult_intensive\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"name\": \"SRHZE\",\n" +
                "    \"conceptRef\": \"1674AAAAAAAAAAAAAAAAAAAAAAAAA\",\n" +
                "    \"regimenLine\": \"adult_intensive\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"name\": \"RfbHZE\",\n" +
                "    \"conceptRef\": \"07c72be8-c575-4e26-af09-9a98624bce67\",\n" +
                "    \"regimenLine\": \"adult_intensive\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"name\": \"RfbHZ\",\n" +
                "    \"conceptRef\": \"9ba203ec-516f-4493-9b2c-4ded6cc318bc\",\n" +
                "    \"regimenLine\": \"adult_intensive\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"name\": \"SRfbHZE\",\n" +
                "    \"conceptRef\": \"fce8ba26-8524-43d1-b0e1-53d8a3c06c00\",\n" +
                "    \"regimenLine\": \"adult_intensive\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"name\": \"S (1 gm vial)\",\n" +
                "    \"conceptRef\": \"84360AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\n" +
                "    \"regimenLine\": \"adult_intensive\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"name\": \"E\",\n" +
                "    \"conceptRef\": \"75948AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\n" +
                "    \"regimenLine\": \"child_intensive\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"name\": \"RH\",\n" +
                "    \"conceptRef\": \"1194AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\n" +
                "    \"regimenLine\": \"child_intensive\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"name\": \"RHE\",\n" +
                "    \"conceptRef\": \"159851AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\n" +
                "    \"regimenLine\": \"child_intensive\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"name\": \"EH\",\n" +
                "    \"conceptRef\": \"1108AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\n" +
                "    \"regimenLine\": \"child_intensive\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"name\": \"RAL/3TC/DRV/RTV\",\n" +
                "    \"conceptRef\": \"5b8e4955-897a-423b-ab66-7e202b9c304c\",\n" +
                "    \"regimenLine\": \"Adult (third line)\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"name\": \"RAL/3TC/DRV/RTV/AZT\",\n" +
                "    \"conceptRef\": \"092604d3-e9cb-4589-824e-9e17e3cb4f5e\",\n" +
                "    \"regimenLine\": \"Adult (third line)\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"name\": \"RAL/3TC/DRV/RTV/TDF\",\n" +
                "    \"conceptRef\": \"c6372744-9e06-40cf-83e5-c794c985b6bf\",\n" +
                "    \"regimenLine\": \"Adult (third line)\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"name\": \"ETV/3TC/DRV/RTV\",\n" +
                "    \"conceptRef\": \"1995c4a1-a625-4449-ab28-aae88d0f80e6\",\n" +
                "    \"regimenLine\": \"Adult (third line)\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"name\": \"AZT/3TC/LPV/r\",\n" +
                "    \"conceptRef\": \"162561AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\n" +
                "    \"regimenLine\": \"Child (second line)\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"name\": \"AZT/3TC/ATV/r\",\n" +
                "    \"conceptRef\": \"164511AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\n" +
                "    \"regimenLine\": \"Child (second line)\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"name\": \"ABC/3TC/ATV/r\",\n" +
                "    \"conceptRef\": \"dddd9cf2-2b9c-4c52-84b3-38cfe652529a\",\n" +
                "    \"regimenLine\": \"Child (third line)\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"name\": \"RAL/3TC/DRV/RTV\",\n" +
                "    \"conceptRef\": \"5b8e4955-897a-423b-ab66-7e202b9c304c\",\n" +
                "    \"regimenLine\": \"Child (third line)\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"name\": \"RAL/3TC/DRV/RTV/AZT\",\n" +
                "    \"conceptRef\": \"092604d3-e9cb-4589-824e-9e17e3cb4f5e\",\n" +
                "    \"regimenLine\": \"Child (third line)\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"name\": \"ETV/3TC/DRV/RTV\",\n" +
                "    \"conceptRef\": \"1995c4a1-a625-4449-ab28-aae88d0f80e6\",\n" +
                "    \"regimenLine\": \"Child (third line)\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"name\": \"RAL/3TC/DRV/RTV/ABC\",\n" +
                "    \"conceptRef\": \"0e74f7aa-85ab-4e92-9f97-79e76e618689\",\n" +
                "    \"regimenLine\": \"Child (third line)\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"name\": \"AZT/3TC/RAL/DRV/r\",\n" +
                "    \"conceptRef\": \"a1183b26-8e87-457c-8d7d-00a96b17e046\",\n" +
                "    \"regimenLine\": \"Child (third line)\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"name\": \"ABC/3TC/RAL/DRV/r\",\n" +
                "    \"conceptRef\": \"02302ab5-dcb2-4337-a792-d6cf1082fc1d\",\n" +
                "    \"regimenLine\": \"Child (third line)\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"name\": \"TDF/3TC/DTG/DRV/r\",\n" +
                "    \"conceptRef\": \"5f429c76-2976-4374-a69e-d2d138dd16bf\",\n" +
                "    \"regimenLine\": \"Adult (third line)\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"name\": \"TDF/3TC/RAL/DRV/r\",\n" +
                "    \"conceptRef\": \"9b9817dd-4c84-4093-95c3-690d65d24b99\",\n" +
                "    \"regimenLine\": \"Adult (third line)\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"name\": \"TDF/3TC/DTG/EFV/DRV/r\",\n" +
                "    \"conceptRef\": \"f2acaf9b-3da9-4d71-b0cf-fd6af1073c9e\",\n" +
                "    \"regimenLine\": \"Adult (third line)\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"name\": \"ABC/3TC/RAL\",\n" +
                "    \"conceptRef\": \"7af7ebbe-99da-4a43-a23a-c3866c5d08db\",\n" +
                "    \"regimenLine\": \"Child (first line)\"\n" +
                "  }\n" +
                "]";
        return json;
    }
}
