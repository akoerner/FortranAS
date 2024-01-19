
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.antlr.v4.runtime.*;

public class TokenStreamToJsonConverter {

    public static String convertTokenStreamToJson(CommonTokenStream tokens) {
        Gson gson = new Gson();

        JsonArray tokenArray = new JsonArray();

        for (Token token : tokens.getTokens()) {
            JsonObject tokenObject = new JsonObject();
            tokenObject.addProperty("type", token.getType());
            tokenObject.addProperty("text", token.getText());
            tokenObject.addProperty("start", token.getStartIndex());
            tokenObject.addProperty("stop", token.getStopIndex());
            tokenArray.add(tokenObject);
        }

        JsonObject tokenStreamObject = new JsonObject();
        tokenStreamObject.add("tokens", tokenArray);

        return gson.toJson(tokenStreamObject);
    }

}

