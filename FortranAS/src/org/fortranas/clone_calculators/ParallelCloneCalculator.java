import java.util.List;
import java.util.Map;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

public class ParallelCloneCalculator {

    private long estimatedWork = 0;
    private volatile boolean progressBarRun = false;
    private CloneCalculator cloneCalculator;
    private Map<String, Object> config;
    private EventBus eventBus;

    public ParallelCloneCalculator(Map<String, Object> config, EventBus eventBus) {
        this.eventBus = eventBus;
        this.config = config;
        this.cloneCalculator = new CloneCalculator(config, eventBus);
    }


    public CloneCalculator getCloneCalculator(){
        return this.cloneCalculator;
    }

    private class CloneCalculationTask implements Runnable {
        private final List<Map<String, Object>> references;
        private final List<Map<String, Object>> candidates;
        private final int startIndex;
        private final int endIndex;

        public CloneCalculationTask(List<Map<String, Object>> references, List<Map<String, Object>> candidates, int startIndex, int endIndex) {
            this.references = references;
            this.candidates = candidates;
            this.startIndex = startIndex;
            this.endIndex = endIndex;
        }

        @Override
        public void run() {
            for (int i = startIndex; i < endIndex; i++) {
                Map<String, Object> ref = references.get(i);
                for (Map<String, Object> can : candidates) {
                    String referenceSubtree = String.valueOf(ref.get("subtree_string"));
                    String candidateSubtree = String.valueOf(can.get("subtree_string"));
                    String uuid = CloneCalculator.getUUID(referenceSubtree, candidateSubtree);
                    cloneCalculator.calculateClones(uuid, referenceSubtree, candidateSubtree);
                    referenceSubtree = null;
                    candidateSubtree = null;
                }
            }
        }
    }

    public void progressBarThreadStart() {
        if (!progressBarRun) {
            progressBarRun = true;
            Thread progressBarThread = new Thread(() -> {
                while (progressBarRun) {
                    try {
                        ProgressBar.updateProgressBar(this.cloneCalculator.totalWork(), estimatedWork);
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
            progressBarThread.start();
        }
    }

    public void progressBarThreadStop() {
        progressBarRun = false;
        System.out.println();
    }

    public void calculateClones(List<Map<String, Object>> subtrees) {
        int numThreads = Runtime.getRuntime().availableProcessors() * 2 ;

        ExecutorService executorService = Executors.newFixedThreadPool(
                numThreads,
                new ThreadFactory() {
                    @Override
                    public Thread newThread(Runnable r) {
                        Thread thread = new Thread(r);
                        thread.setPriority(Thread.MAX_PRIORITY);
                        return thread;
                    }
                }
        );


        List<Map<String, Object>> candidates = new CopyOnWriteArrayList<>(subtrees);
        List<Map<String, Object>> references = new CopyOnWriteArrayList<>(subtrees);

        this.estimatedWork = ((long)references.size() * (long)candidates.size());

        this.progressBarThreadStart();
        int chunkSize = references.size() / numThreads;

        for (int i = 0; i < numThreads; i++) {
            int start = i * chunkSize;
            int end = (i == numThreads - 1) ? references.size() : (i + 1) * chunkSize;

            Runnable task = new CloneCalculationTask(references, candidates, start, end);
            executorService.execute(task);
        }

        executorService.shutdown();
        try {
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        this.progressBarThreadStop();
    }

}
