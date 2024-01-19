import java.util.*;
import java.util.stream.Collectors;
import java.util.Arrays;
import java.util.List;

import java.util.Map;
import java.util.HashMap;
import java.util.Queue;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.text.similarity.SimilarityScore;

public class BLEUScoreCalculator implements SimilarityScore<Double> {

    private static final double[] BLEU_1_WIGHTS = {1, 0, 0, 0};
    private static final double[] BLEU_2_WIGHTS = {0.5, 0.5, 0, 0};
    private static final double[] BLEU_3_WIGHTS = {0.33, 0.33, 0.33, 0};
    private static final double[] BLEU_4_WIGHTS = {0.25, 0.25, 0.25, 0.25};
    private static final double[] DEFAULT_WEIGHTS = BLEU_2_WIGHTS;

    private double[] weights;

    public BLEUScoreCalculator(final double[] weights) {
        this.weights = weights;
    }

    public Double apply(final CharSequence left, final CharSequence right) {
        return new Double(calculateBLEU(left.toString(), right.toString(), this.weights));
    }

    public static List<String> convertStringToList(String input) {
        String[] splitArray = input.split(" ");
        List<String> resultList = Arrays.asList(splitArray);
        splitArray = null;
        return resultList;
    }

    public static double calculateBLEU(final String reference, final String candidate, double[] weights) {

        if (reference.equals(candidate)){
            return 1d;
        }


        List<String> referenceNgrams = new ArrayList<>(convertStringToList(reference));
        List<List<String>> references = new ArrayList<>();
        references.add(referenceNgrams);
        List<String> candidateNgrams = convertStringToList(candidate);

        double bleuScore = calculateBLEU(references, candidateNgrams, weights);
        referenceNgrams = null;
        references = null;
        candidateNgrams = null;
        return bleuScore;
    }

    public static double calculateBLEU(List<List<String>> reference, List<String> candidate, double[] weights) {

        double[] precision = new double[weights.length];
        double brevityPenalty;

        for (int n = 1; n <= weights.length; n++) {
            List<String> candidateNgrams = generateNgrams(candidate, n);
            List<List<String>> referenceNgrams = generateReferenceNgrams(reference, n);

            int candidateNgramCount = candidateNgrams.size();
            int matchingNgramCount = countMatchingNgrams(candidateNgrams, referenceNgrams);

            if (candidateNgramCount > 0) {
                precision[n - 1] = (double) matchingNgramCount / candidateNgramCount;
            } else {
                precision[n - 1] = 0.0;
            }
            candidateNgrams = null;
            referenceNgrams = null;
        }

        int candidateLength = candidate.size();
        int closestReferenceLength = findClosestReferenceLength(reference, candidateLength);
        if (candidateLength > closestReferenceLength) {
            brevityPenalty = 1.0;
        } else {
            brevityPenalty = Math.exp(1.0 - closestReferenceLength / (double) candidateLength);
        }

        double geometricMean = 0.0;
        for (int i = 0; i < weights.length; i++) {
            geometricMean += weights[i] * Math.log(precision[i]);
        }
        geometricMean = Math.exp(geometricMean);

        double bleuScore = brevityPenalty * geometricMean;
        precision = null;
        return bleuScore;
    }

    private static List<String> generateNgrams(List<String> words, int n) {
        List<String> ngrams = new ArrayList<>();
        for (int i = 0; i < words.size() - n + 1; i++) {
            List<String> ngramTokens = words.subList(i, i + n);
            ngrams.add(String.join(" ", ngramTokens));
        }
        return ngrams;
    }

    private static List<List<String>> generateReferenceNgrams(List<List<String>> reference, int n) {
        return reference.stream().map(sentence -> generateNgrams(sentence, n)).collect(Collectors.toList());
    }

    private static int countMatchingNgrams(List<String> candidateNgrams, List<List<String>> referenceNgrams) {
        int count = 0;
        for (String candidateNgram : candidateNgrams) {
            for (List<String> refNgramList : referenceNgrams) {
                if (refNgramList.contains(candidateNgram)) {
                    refNgramList.remove(candidateNgram);
                    count++;
                    break;
                }
            }
        }
        return count;
    }

    private static int findClosestReferenceLength(List<List<String>> reference, int candidateLength) {
        int closestLength = Integer.MAX_VALUE;
        for (List<String> ref : reference) {
            int refLength = ref.size();
            if (Math.abs(refLength - candidateLength) < Math.abs(closestLength - candidateLength)) {
                closestLength = refLength;
            }
        }
        return closestLength;
    }
}

