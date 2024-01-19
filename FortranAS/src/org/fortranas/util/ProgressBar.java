import java.text.DecimalFormat;

public class ProgressBar {

    private static long startTime;
    private static final DecimalFormat df = new DecimalFormat("#0.00");

    public static void start() {
        if(ProgressBar.startTime == 0){
            //System.out.println("Progress | Elapsed Time (hh:mm:ss) | Estimated Time (hh:mm:ss)| Calculations/Second (estimated) | Completed calculations/required calculations (estimated)");
            ProgressBar.startTime = System.currentTimeMillis();
        }
    }

    public static void stop() {
        ProgressBar.startTime = 0;
        ProgressBar.startTime = System.currentTimeMillis();
    }

    public static void updateProgressBar(long currentStep, long totalSteps) {
        ProgressBar.start();
        int progress = (int) ((double) currentStep / totalSteps * 100);
        if (progress > 100){
            progress = 100;
        }
        long elapsedTime = System.currentTimeMillis() - ProgressBar.startTime;
        long estimatedTime = (long) ((double) elapsedTime / currentStep * (totalSteps - currentStep));

        if(estimatedTime < 0){
            estimatedTime = 0;
        }

        double percent = (double) currentStep / totalSteps * 100;
        if (percent > 100.0) {
            percent = 100.00;
        }

        String progressBar = getProgressBarString(progress);
        String elapsedTimeStr = formatTime(elapsedTime);
        String estimatedTimeStr = formatTime(estimatedTime);
        double estimatedStepsPerSecond = currentStep == 0 ? 0 : (double) currentStep / (elapsedTime / 1000.0);
        System.out.print("\r[" + progressBar + "] " + df.format(percent) + "% " +
                "| " + elapsedTimeStr + " " + 
                "| " + estimatedTimeStr + " " + 
                "| " + df.format(estimatedStepsPerSecond) + " " + 
                "| " + currentStep + "/" + totalSteps
                );
        progressBar = null;
        elapsedTimeStr = null;
        estimatedTimeStr = null;
        if(progress >= 100){
            System.out.println();
        }
        System.out.flush();
    }



    private static String getProgressBarString(int progress) {
        int barLength = 30;
        if (progress > 100){
            progress = 100;
        }
        int numBars = (int) (progress / (100.0 / barLength));
        StringBuilder progressBar = new StringBuilder();
        for (int i = 0; i < barLength; i++) {
            if (i < numBars) {
                progressBar.append('=');
            } else {
                progressBar.append(' ');
            }
        }
        return progressBar.toString();
    }

    private static String formatTime(long milliseconds) {
        long seconds = milliseconds / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;

        return String.format("%02d:%02d:%02d", (int)hours, (int)(minutes % 60), (int)(seconds % 60));
    }
}

