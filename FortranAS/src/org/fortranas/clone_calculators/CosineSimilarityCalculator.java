import java.util.HashMap;
import java.util.Map;

public class CosineSimilarityCalculator {

    public static double calculateCosineSimilarity(String reference, String candidate) {
        String[] tokensReference = reference.split("\\s+");
        String[] tokensCandidate = candidate.split("\\s+");

        Map<String, Integer> tfReference = buildTermFrequencyVector(tokensReference);
        Map<String, Integer> tfCandidate = buildTermFrequencyVector(tokensCandidate);

        double dotProduct = calculateDotProduct(tfReference, tfCandidate);
        double magnitudeReference = calculateMagnitude(tfReference);
        double magnitudeCandidate = calculateMagnitude(tfCandidate);

        if (magnitudeReference == 0 || magnitudeCandidate == 0) {
            return 0.0;
        } else {
            return dotProduct / (magnitudeReference * magnitudeCandidate);
        }
    }

    private static Map<String, Integer> buildTermFrequencyVector(String[] tokens) {
        Map<String, Integer> tfVector = new HashMap<>();
        for (String token : tokens) {
            tfVector.put(token, tfVector.getOrDefault(token, 0) + 1);
        }
        return tfVector;
    }

    private static double calculateDotProduct(Map<String, Integer> vector1, Map<String, Integer> vector2) {
        double dotProduct = 0;
        for (String term : vector1.keySet()) {
            if (vector2.containsKey(term)) {
                dotProduct += vector1.get(term) * vector2.get(term);
            }
        }
        return dotProduct;
    }

    private static double calculateMagnitude(Map<String, Integer> vector) {
        double magnitude = 0;
        for (int value : vector.values()) {
            magnitude += Math.pow(value, 2);
        }
        return Math.sqrt(magnitude);
    }

}

