import com.google.gson.*;
import java.lang.reflect.Type;

import org.antlr.v4.runtime.*;

public class CommonTokenSerializer implements JsonSerializer<CommonToken> {
    @Override
    public JsonElement serialize(CommonToken token, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject tokenObject = new JsonObject();
        tokenObject.addProperty("type", token.getType());
        tokenObject.addProperty("text", token.getText());
        tokenObject.addProperty("start", token.getStartIndex());
        tokenObject.addProperty("stop", token.getStopIndex());
        return tokenObject;
    }
}
