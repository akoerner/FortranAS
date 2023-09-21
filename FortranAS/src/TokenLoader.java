import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class TokenLoader {

    private static Map<String, Integer> tokenMap = loadTokens();

    public static Map<String, Integer> loadTokens() {
        Map<String, Integer> tokenMap = new HashMap<>();

        InputStream inputStream = TokenLoader.class.getResourceAsStream("/fortran/fortran90/Fortran90Lexer.tokens");

        if (inputStream != null) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split("=");
                    if (parts.length == 2) {
                        String tokenName = parts[0].trim();
                        int tokenType = Integer.parseInt(parts[1].trim());
                        tokenMap.put(tokenName, tokenType);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.err.println("Fortran90Lexer.tokens file not found in the JAR.");
        }

        return tokenMap;
    }

    public static String getTokenName(int tokenType) {
        if (TokenLoader.tokenMap == null){
            TokenLoader.loadTokens();
        }
        for (Map.Entry<String, Integer> entry : TokenLoader.tokenMap.entrySet()) {
            if (entry.getValue() == tokenType) {
                return entry.getKey();
            }
        }
        return null;
    }
}
