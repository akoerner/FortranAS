import java.io.FileInputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class FortranASConfig {
    private Map<String, Object> config;

    public FortranASConfig(String configFile) throws IOException {
        File file = new File(configFile);
        if (!file.exists()) {
            throw new IOException("ERROR: FortranAS config file not found: " + configFile);
        }
        loadConfig(configFile);
    }

    private void loadConfig(String configFile) {
        Properties properties = new Properties();
        config = new HashMap<>();

        try (FileInputStream input = new FileInputStream(configFile);
             InputStreamReader isr = new InputStreamReader(input, StandardCharsets.UTF_8)) {

            properties.load(isr);

            for (String key : properties.stringPropertyNames()) {
                String value = properties.getProperty(key).trim();
                config.put(key, parseValue(value));
            }

            if (properties.containsKey("bleu_score_weights")) {
                String[] values = properties.getProperty("bleu_score_weights").split(",");
                double[] bleuScoreWeights = new double[values.length];

                for (int i = 0; i < values.length; i++) {
                    bleuScoreWeights[i] = Double.parseDouble(values[i].trim());
                }

                config.put("bleu_score_weights", bleuScoreWeights);
            }
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }
    }

    private static boolean isInteger(String s) {
        if(s.contains(".")){
            return false;
        }
        try {
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static boolean isFloat(String s) {
        try {
            Float.parseFloat(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static boolean isDouble(String s) {
        try {
            Double.parseDouble(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private Object parseValue(String value) {
        if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
            return Boolean.parseBoolean(value);
        }
        if (isInteger(value)) {
            return Integer.parseInt(value);
        } else if (isDouble(value)) {
            return Double.parseDouble(value);
        } else if (isFloat(value)) {
            return Float.parseFloat(value);
        }

        return value;
    }

    public Map<String, Object> getConfig() {
        return config;
    }


    public void printConfig() {
        System.out.println("    FortranAS Configuration Settings:");

        for (Map.Entry<String, Object> entry : config.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            System.out.print("      " + key + ": ");

            if (value instanceof double[]) {
                double[] doubleArray = (double[]) value;
                System.out.print("[");
                for (int i = 0; i < doubleArray.length; i++) {
                    System.out.print(doubleArray[i]);
                    if (i < doubleArray.length - 1) {
                        System.out.print(", ");
                    }
                }
                System.out.println("]");
            } else {
                System.out.println(value);
            }
        }
    }

}

