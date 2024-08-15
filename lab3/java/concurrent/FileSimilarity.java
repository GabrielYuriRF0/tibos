import java.io.*;
import java.util.*;

public class FileSimilarity {

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: java Sum filepath1 filepath2 filepathN");
            System.exit(1);
        }

        // Create a map to store the fingerprint for each file
        Map<String, List<Long>> fileFingerprints = new HashMap<>();
        
        ArrayList<Thread> myThreads = new ArrayList<>();
        //ArrayList<Thread> myThreads2 = new ArrayList<>();
        // Calculate the fingerprint for each file
        for (String path : args) {
            Thread myThread = new Thread(new FirstFileSum(path, fileFingerprints), path);
            myThreads.add(myThread);
            myThread.start();
            //List<Long> fingerprint = fileSum(path);
            //fileFingerprints.put(path, fingerprint);
        }

        for(Thread thread : myThreads){
            thread.join();
        }

        // Compare each pair of files
        for (int i = 0; i < args.length; i++) {
            Thread myThread2 = new Thread(new CompareSimi(i, args, fileFingerprints));
            myThread2.start();
        }
    }

    private static List<Long> fileSum(String filePath) throws IOException{
        File file = new File(filePath);
        List<Long> chunks = new ArrayList<>();
        try (FileInputStream inputStream = new FileInputStream(file)) {
            byte[] buffer = new byte[100];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                long sum = sum(buffer, bytesRead);
                chunks.add(sum);
                
            }
        }
        System.currentTimeMillis();
        return chunks;
    }

    private static long sum(byte[] buffer, int length) {
        long sum = 0;
        for (int i = 0; i < length; i++) {
            sum += Byte.toUnsignedInt(buffer[i]);
        }
        return sum;
    }

    private static float similarity(List<Long> base, List<Long> target) {
        int counter = 0;
        List<Long> targetCopy = new ArrayList<>(target);

        for (Long value : base) {
            if (targetCopy.contains(value)) {
                counter++;
                targetCopy.remove(value);
            }
        }

        return (float) counter / base.size();
    }

    public static class FirstFileSum implements Runnable{
        private final String path;
        private final Map<String, List<Long>> fileFingerprints;

        public FirstFileSum(String path, Map<String, List<Long>> fileFingerPrint){
            this.path = path;
            this.fileFingerprints = fileFingerPrint;
            
        }

        @Override
        public void run(){
            try{
                List<Long> fingerprint = fileSum(path);
                synchronized(this){
                    this.fileFingerprints.put(path, fingerprint);
                }
            } catch (Exception e){
                
            }
            
        }
    }

    public static class CompareSimi implements Runnable{
        private final String[] args ;
        private final int i;
        private final Map<String, List<Long>> fileFingerprints;

        public CompareSimi(int i, String[] args, Map<String, List<Long>> fileFingerprints){
            this.args = args;
            this.i = i;
            this.fileFingerprints = fileFingerprints;
            
        }

        @Override
        public void run(){
            try{
                for (int j = i + 1; j < args.length; j++) {
                    String file1 = args[i];
                    String file2 = args[j];
                    List<Long> fingerprint1 = fileFingerprints.get(file1);
                    List<Long> fingerprint2 = fileFingerprints.get(file2);
                    float similarityScore = similarity(fingerprint1, fingerprint2);
                    System.out.println("Similarity between " + file1 + " and " + file2 + ": " + (similarityScore * 100) + "%");
                }
            } catch (Exception e){
                
            }
            
        }
    }
}
