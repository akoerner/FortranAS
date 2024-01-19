import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class TokenLoader {

    private static Map<String, Integer> tokenMap = new HashMap<String, Integer>();

    public static void loadTokens(String lexerClassName) {
        String tokenDirectoryName = lexerClassName.replace("Lexer", "").toLowerCase();
        String tokenFileBaseDirectory; 
        String tokenFile = lexerClassName + ".tokens";
        InputStream inputStream;


        tokenFileBaseDirectory="/org/fortranas/antlr4/generated/fortran/" + tokenDirectoryName + "/";
        //System.out.println("Using tokenFileBaseDirectory: " + tokenFileBaseDirectory + tokenFile); 
        inputStream = TokenLoader.class.getResourceAsStream(tokenFileBaseDirectory + tokenFile);
        
        if (inputStream == null) {
            tokenFileBaseDirectory="/generated/antlr4_generated/fortran/" + tokenDirectoryName + "/";
            //System.out.println("Using tokenFileBaseDirectory: " + tokenFileBaseDirectory + tokenFile); 
            inputStream = TokenLoader.class.getResourceAsStream(tokenFileBaseDirectory + tokenFile);
        }
        //System.out.println("Loading token file: " + tokenFileBaseDirectory + "/" + tokenFile);

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
            System.err.println("ERROR: " + lexerClassName + ".tokens file not found in the JAR.");
            System.exit(0);
        }

    }

    public static String getTokenName(int tokenType, String lexerClassName) {
        if (TokenLoader.tokenMap.size() == 0){
            TokenLoader.loadTokens(lexerClassName);
        }
        for (Map.Entry<String, Integer> entry : TokenLoader.tokenMap.entrySet()) {
            if (entry.getValue() == tokenType) {
                return entry.getKey();
            }
        }
        return null;
    }
}
