
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonArray;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.zip.CRC32;
import java.io.BufferedReader;
import java.io.FileReader;

import org.antlr.v4.runtime.*;




public class JSONConverter {

    private static final String DELIMITER = " ";

    public static long generateCRC32Hash(String input) {
        CRC32 crc32 = new CRC32();
        crc32.update(input.getBytes());
        return crc32.getValue();
    }

    public static String toJSONExcludeNested(Map<String, Object> data) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setPrettyPrinting();
        Gson gson = gsonBuilder.create();

        Map<String, Object> topLevelData = data.entrySet()
                .stream()
                .filter(entry -> !(entry.getValue() instanceof Map) && !(entry.getValue() instanceof Iterable))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        String json = gson.toJson(topLevelData);
        return json;
    }

    public static String toJSON(Map<String, Object> data) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setPrettyPrinting();
        gsonBuilder.registerTypeAdapter(CommonToken.class, new CommonTokenSerializer());
        Gson gson = gsonBuilder.create();
        String json = gson.toJson(data);
        return json;
    }


    public static List<List<String>> loadStrings(String jsonFile) {
        return loadStrings(jsonFile, DELIMITER);
    }

    public static List<List<String>> loadStrings(String jsonFile, String delimiter) {
        List<List<String>> stringList = new ArrayList<>();

        try {
            BufferedReader reader = new BufferedReader(new FileReader(jsonFile));
            StringBuilder jsonString = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonString.append(line);
            }
            reader.close();

            JsonParser parser = new JsonParser();
            JsonElement jsonElement = parser.parse(jsonString.toString());

            extractAndSplitStrings(jsonElement, stringList, delimiter);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return stringList;
    }

    private static void extractAndSplitStrings(JsonElement jsonElement, List<List<String>> stringList, String delimiter) {
        if (jsonElement.isJsonObject()) {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            for (String key : jsonObject.keySet()) {
                if (key.equals("string")) {
                    String stringValue = jsonObject.get(key).getAsString();
                    String[] splitValues = stringValue.split(delimiter);
                    List<String> splitList = new ArrayList<>();
                    for (String splitValue : splitValues) {
                        splitList.add(splitValue);
                    }
                    stringList.add(splitList);
                } else {
                    extractAndSplitStrings(jsonObject.get(key), stringList, delimiter);
                }
            }
        } else if (jsonElement.isJsonArray()) {
            JsonArray jsonArray = jsonElement.getAsJsonArray();
            for (JsonElement element : jsonArray) {
                extractAndSplitStrings(element, stringList, delimiter);
            }
        }
    }



}



