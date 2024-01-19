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
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.PriorityBlockingQueue;

import org.apache.commons.text.similarity.CosineDistance;
import org.apache.commons.text.similarity.JaroWinklerSimilarity;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

public class CloneCalculator {


    private CosineDistance cosineDistance = new CosineDistance();
    private JaroWinklerSimilarity jaroWinklerSimilarity = new JaroWinklerSimilarity();
    private SorensenDiceCoefficientCalculator sorensenDiceCoefficientCalculator = new SorensenDiceCoefficientCalculator();
    private BLEUScoreCalculator bLEUScoreCalculator; 
    
    //private ConcurrentWeakReferenceQueue<Map<String, Object>> clones = new ConcurrentWeakReferenceQueue<>();
    //private final BlockingQueue<Map<String, Object>> clones = new BlockingQueue<>();
    //private final BlockingQueue<Map<String, Object>> clones;
    //private final BlockingQueue<Map<String, Object>> clones = new ArrayBlockingQueue<>(20000000);
   // private final PriorityBlockingQueue<Map<String, Object>> clones = new PriorityBlockingQueue<>(20000000);


    //private final Queue<Map<String, Object>> clones = new ConcurrentLinkedQueue<>();
    //private final Queue<Map<String, Object>> clones = new PriorityBlockingQueue<>();
    private long cloneCalculations = 0;
    
    private Map<String, Object> config;

    private boolean bleu_score_enabled;
    private boolean jaro_winkler_similarity_enabled;
    private boolean sorensen_dice_coefficient_enabled;
    private boolean cosine_similarity_enabled;

    private  EventBus eventBus;


    public CloneCalculator(Map<String, Object> config, EventBus eventBus) {
        this.eventBus = eventBus;
        this.config = config;
        this.bLEUScoreCalculator = new BLEUScoreCalculator((double[]) config.get("bleu_score_weights"));
        this.bleu_score_enabled = ((Boolean)config.get("bleu_score_enabled")).booleanValue();
        this.cosine_similarity_enabled = ((Boolean)config.get("cosine_similarity_enabled")).booleanValue();
        this.jaro_winkler_similarity_enabled = ((Boolean)config.get("jaro_winkler_similarity_enabled")).booleanValue();
        this.sorensen_dice_coefficient_enabled = ((Boolean)config.get("sorensen_dice_coefficient_enabled")).booleanValue();
    }

    public long totalWork(){
        return this.cloneCalculations;
    }

    public static String getUUID(String reference, String candidate) {
        if (reference.compareTo(candidate) < 0) {
            return HashingTools.toUUID(reference + candidate);
        } else if (reference.compareTo(candidate) > 0) {
            return HashingTools.toUUID(candidate + reference);
        } else {
            return HashingTools.toUUID(reference);
        }
    }

    public static double round(double number, int precision) {
        double scale = Math.pow(10, precision);
        return Math.round(number * scale) / scale;
    }

    public void calculateClones(String uuid, String reference, String candidate) {

        this.cloneCalculations++;
        //String uuid = CloneCalculator.getUUID(reference, candidate);


        //if (clones.containsKey(uuid)) {
        //   return (double)((CloneCalculator.clones.get(uuid)).get("bleu_score")); 
        //}

        if (reference.equals(candidate)){
            uuid = null;
            reference = null;
            candidate = null;
            return;
        }

        //double bleuScore = round(BLEUScoreCalculator.calculateSentenceLevelBLEUScore(reference, candidate), 2);
        //double bleuScore = BLEUScoreCalculator.calculateSentenceLevelBLEUScore(reference, candidate);
        //double bleu_score = 0.0;
        //double cosine_similarity = 0.0;
        //double sorensen_dice_coefficient = 0.0;
        //double jaro_winkler_similarity = 0.0;
        //double cosine_similarity = round(cosineDistance.apply(reference, candidate), 4);
        //double sorensen_dice_coefficient = round(SorensenDiceCoefficientCalculator.calculateSorensenDiceCoefficient(reference, candidate), 2);
        //double jaro_winkler_similarity = jaroWinklerSimilarity.apply(reference, candidate);
        
        //double cosineSimilarity = 0;
        //double sorensen_dice_coefficient = 0;


        //if (FortranASDatabase.cloneExists(uuid)){

        //}
        Map<String, Object> map = Map.of(
                "uuid", uuid,
                "reference_uuid", HashingTools.toUUID(reference),
                "candidate_uuid", HashingTools.toUUID(candidate),
                "bleu_score", (double) (bleu_score_enabled ? round(bLEUScoreCalculator.apply(reference, candidate).doubleValue(), 2) : -1d),
                "cosine_similarity", (double) (cosine_similarity_enabled ? round(cosineDistance.apply(reference, candidate).doubleValue(), 2) : -1d),
                "sorensen_dice_coefficient", (double) (sorensen_dice_coefficient_enabled ? round(sorensenDiceCoefficientCalculator.apply(reference, candidate).doubleValue(), 2) : -1d),
                "jaro_winkler_similarity", (double) (jaro_winkler_similarity_enabled ? round(jaroWinklerSimilarity.apply(reference, candidate), 2) : -1d)
        );

        if(((double)map.get("bleu_score")) >= ((double)config.get("minimum_bleu_score"))){
            eventBus.post(map);
        }
        //try{
        //this.clones.put(new ComparableMap(map));
        //}catch(java.lang.InterruptedException e){
         //   e.printStackTrace();
        //}
        //this.clones.offer(map);
        //this.clones.add(map);


        //this.enqueue(uuid, reference, candidate, bleu_score);
        //this.enqueue(uuid, reference, candidate, bleu_score, cosine_similarity, sorensen_dice_coefficient, jaro_winkler_similarity);
        //map = null;
        uuid = null;
        reference = null;
        candidate = null;
    
    }

}

