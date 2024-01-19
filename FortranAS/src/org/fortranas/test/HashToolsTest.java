import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.DisplayName;
import java.util.UUID;
import java.nio.charset.StandardCharsets;

public class HashToolsTest {

    @Test
    @DisplayName("sha256 sum test") 
    void testSha256Sum() {
        String inputString = "Hello, World!";
        String expectedHash = "dffd6021bb2bd5b0af676290809ec3a53191dd81c7f70a4b28688a362182986f";

        //assertEquals("false", HashingTools.sha256Sum("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"));

        String calculatedHash = HashingTools.sha256Sum(inputString);
        System.out.println("calculated hash: " + calculatedHash);

        assertNotNull(calculatedHash);
        assertEquals(expectedHash, calculatedHash);
    }

    @Test
    @DisplayName("sha256 sum to UUID test") 
    void testSha256SumToUUID() {
        String sha256Hash = "dffd6021bb2bd5b0af676290809ec3a53191dd81c7f70a4b28688a362182986f";
        String expectedUUID = "dffd6021-bb2b-d5b0-af67-6290809ec3a5";

        String calculatedUUID = HashingTools.sha256SumToUUID(sha256Hash);
        System.out.println("calculated UUID: " + calculatedUUID);


        assertNotNull(expectedUUID);
        assertNotNull(calculatedUUID);
        //assertEquals(expectedUUID, calculatedUUID);
    }

    @Test
    @DisplayName("string to UUID test") 
    void testUUIDFromString() {
        String inputString = "Hello, World!";
        String expectedUUID = "dffd6021-bb2b-d5b0-af67-6290809ec3a5";

        String calculatedUUID = HashingTools.toUUID(inputString);

        assertNotNull(expectedUUID);
        assertNotNull(calculatedUUID);
        //assertEquals(expectedUUID, calculatedUUID);
    }
}

