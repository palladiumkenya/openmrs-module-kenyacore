package org.openmrs.module.kenyacore.filesystemglobalproperty;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.openmrs.GlobalProperty;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * A util class for managing configurations/settings defined in the filesystem.
 * It maintains a Map<String,String> of property and value
 */
public class FsGlobalProperty {
    public static Map<String,String> configurations;

    /**
     * Loads settings from a file
     * @param filePath
     */
    public static void loadConfigurationsFromFileSystem(String filePath) {
        String data = "";
        FileInputStream fis = null;
        configurations = new HashMap<>();
        try {
            fis = new FileInputStream(filePath);
        } catch (FileNotFoundException e) {
            System.err.println("The configuration file for file-based settings was not found!");
            e.printStackTrace();
            return;
        }
        try {
            data = IOUtils.toString(fis, "UTF-8");
        } catch (IOException e) {
            System.err.println("The configuration file for file-based settings could not be loaded!");
            e.printStackTrace();
            return;
        }
        ObjectNode config = null;
        if (StringUtils.isNotBlank(data)) {
            ObjectMapper mapper = new ObjectMapper();
            try {
                config = (ObjectNode) mapper.readTree(data);
                Iterator<String> iterator = config.getFieldNames();
                ObjectNode finalConfig = config;
                iterator.forEachRemaining(key -> {
                    String propertyValue = finalConfig.get(key).getTextValue();
                    configurations.put(key, propertyValue);
                });

            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("There was an error reading the file-based configuration file. Check that the JSON object is well formed");
                return;
            }
        }
    }

    public static String getPropertyByName(String globalProperty) {
        return configurations.get(globalProperty);
    }
    public static String getPropertyByGlobalProperty(GlobalProperty globalProperty) {
        return configurations.get(globalProperty.getProperty());
    }
}
