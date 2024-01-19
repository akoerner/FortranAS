import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

/**
 * Utility class for various hashing operations.
 */
public class HashingTools {

    /**
     * Prints the hexadecimal representation of a byte array.
     *
     * @param byteArray The byte array to be printed.
     */
    public static void printBytes(byte[] byteArray) {
        for (byte b : byteArray) {
            System.out.print(String.format("%02X ", b));
        }
        System.out.println();
    }

    /**
     * Reverses the byte order of a byte array.
     *
     * @param array The byte array to be reversed.
     * @return The reversed byte array.
     */
    public static byte[] reverseByteOrder(byte[] array) {
        int length = array.length;
        byte[] reversedArray = new byte[length];

        for (int i = 0; i < length; i++) {
            reversedArray[i] = array[length - i - 1];
        }

        array = null;
        return reversedArray;
    }

    /**
     * Converts a byte array to its hexadecimal string representation.
     *
     * @param bytes The byte array to be converted.
     * @return The hexadecimal string representation.
     */
    public static String bytesToHexString(byte[] bytes) {
        StringBuilder hexString = new StringBuilder(2 * bytes.length);

        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
            hex = null;
        }

        bytes = null;
        return hexString.toString();
    }

    /**
     * Calculates the SHA-256 hash of a given input string.
     *
     * @param input The input string to be hashed.
     * @return The hexadecimal representation of the SHA-256 hash.
     */
    public static String sha256Sum(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));

            return HashingTools.bytesToHexString(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null; // Handle the exception as needed
        }
    }

    /**
     * Converts a SHA-256 hash in hexadecimal format to a formatted UUID string.
     *
     * @param hexSHA256 The SHA-256 hash in hexadecimal format.
     * @return The formatted UUID string.
     */
    public static String sha256SumToUUID(String hexSHA256) {
        try {
            if (!hexSHA256.matches("^[0-9a-fA-F]+$")) {
                throw new NumberFormatException("Invalid hex string: " + hexSHA256);
            }

            String formattedUUID = String.format(
                    "%s-%s-%s-%s-%s",
                    hexSHA256.substring(0, 8),
                    hexSHA256.substring(8, 12),
                    hexSHA256.substring(12, 16),
                    hexSHA256.substring(16, 20),
                    hexSHA256.substring(20, 32)
            );

            hexSHA256 = null;
            return formattedUUID;
        } catch (NumberFormatException e) {
            // Handle exception (e.g., log it or throw a custom exception)
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Generates a UUID based on the SHA-256 hash of the input string.
     *
     * @param input The input string to generate the UUID from.
     * @return The generated UUID.
     */
    public static String toUUID(String input) {
        return HashingTools.sha256SumToUUID(HashingTools.sha256Sum(input));
    }

    /**
     * Calculates the SHA-256 hash of a file.
     *
     * @param fileName The name of the file to hash.
     * @return The hexadecimal representation of the SHA-256 hash.
     */
    public static String fileSha256sum(String fileName) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");

            try (DigestInputStream dis = new DigestInputStream(new FileInputStream(fileName), md)) {
                byte[] buffer = new byte[8192];
                while (dis.read(buffer) != -1) {
                }
                buffer = null;
            }

            byte[] digest = md.digest();

            StringBuilder result = new StringBuilder();
            for (byte b : digest) {
                result.append(String.format("%02x", b));
            }

            digest = null;
            return result.toString();

        } catch (NoSuchAlgorithmException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}

