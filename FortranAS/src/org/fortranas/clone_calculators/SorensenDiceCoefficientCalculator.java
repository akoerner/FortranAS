import org.apache.commons.text.similarity.SimilarityScore;

public class SorensenDiceCoefficientCalculator implements SimilarityScore<Double> {

    public Double apply(final CharSequence left, final CharSequence right) {
        return new Double(calculateSorensenDiceCoefficient(left.toString(), right.toString()));
    }

    public static double calculateSorensenDiceCoefficient(String reference, String candidate) {
        if (reference == null || candidate == null || reference.isEmpty() || candidate.isEmpty()) {
            throw new IllegalArgumentException("Input strings cannot be null or empty");
        }

        char[] refChars = reference.toCharArray();
        char[] candChars = candidate.toCharArray();

        int intersectionCount = 0;
        for (char refChar : refChars) {
            for (char candChar : candChars) {
                if (refChar == candChar) {
                    intersectionCount++;
                    break;
                }
            }
        }

        int totalChars = refChars.length + candChars.length;
        double diceCoefficient = (2.0 * intersectionCount) / totalChars;

        return diceCoefficient;
    }

}

