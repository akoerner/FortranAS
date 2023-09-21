import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.Map;
import java.util.ArrayList;

import org.antlr.v4.runtime.*;

import java.util.zip.CRC32;

public class JSONConverter {


    public static long generateCRC32Hash(String input) {
        CRC32 crc32 = new CRC32();
        crc32.update(input.getBytes());
        return crc32.getValue();
    }

    public static String toJSON(Map<String, Object> data) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setPrettyPrinting();
        gsonBuilder.registerTypeAdapter(CommonToken.class, new CommonTokenSerializer());
        Gson gson = gsonBuilder.create();
        String json = gson.toJson(data);
        return json;
    }

}



