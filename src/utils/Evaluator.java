package src.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class Evaluator {
    public static HashMap<Integer, String> loadQueries(File queryFile) throws IOException {
        HashMap<Integer, String> queries = new HashMap<>();
        BufferedReader br = new BufferedReader(new FileReader(queryFile));

        String line;
        int currentQid = -1;
        StringBuilder currentQueryText = new StringBuilder();
        boolean readingWords = false;

        while ((line = br.readLine()) != null) {
            if (line.startsWith(".I")) {
                if (currentQid != -1) {
                    queries.put(currentQid, currentQueryText.toString().trim());
                }
                currentQid = Integer.parseInt(line.substring(3).trim());
                currentQueryText.setLength(0);
                readingWords = false;
            } else if (line.startsWith(".W")) {
                readingWords = true;
            } else if (readingWords) {
                currentQueryText.append(line).append(" ");
            }
        }
        if (currentQid != -1) {
            queries.put(currentQid, currentQueryText.toString().trim());
        }
        br.close();
        return queries;
    }

    public static HashMap<Integer, HashSet<Integer>> loadRelevance(File qrelFile) throws IOException {
        HashMap<Integer, HashSet<Integer>> relevanceList = new HashMap<>();
        BufferedReader br = new BufferedReader(new FileReader(qrelFile));
        String line;

        while ((line = br.readLine()) != null) {

            String[] parts = line.trim().split("\\s+");

            if (parts.length >= 2) {
                int queryId = Integer.parseInt(parts[0]);
                int docId = Integer.parseInt(parts[1]);

                relevanceList.putIfAbsent(queryId, new HashSet<>());
                relevanceList.get(queryId).add(docId);
            }
        }
        br.close();
        return relevanceList;
    }
    
    // method untuk kalkulasi matriks evaluasi lengkap
    public static void calculateMetrics(List<Map.Entry<Integer, Double>> results, HashSet<Integer> relevantDocs) {
        int totalRetrieved = results.size();
        int totalRelevant = relevantDocs.size();
        int truePositive = 0;

        // array untuk recall dan precision point
        double[] precisions = new double[totalRetrieved];
        double[] recalls = new double[totalRetrieved];

        // loop tiap dokumen hasil untuk hitung precision dan recall pada setiap titik
        for (int i = 0; i < totalRetrieved; i++) {
            int docId = results.get(i).getKey();
            if (relevantDocs.contains(docId)) {
                truePositive++;
            }
            precisions[i] = (double) truePositive / (i + 1);
            recalls[i] = (totalRelevant > 0) ? (double) truePositive / totalRelevant : 0.0;
        }

        // kalkulasi precision dan recall keseluruhan
        double overallPrecision = totalRetrieved > 0 ? (double) truePositive / totalRetrieved : 0.0;
        double overallRecall = totalRelevant > 0 ? (double) truePositive / totalRelevant : 0.0;

        System.out.printf("Precision: %.4f | Recall: %.4f\n", overallPrecision, overallRecall);

        // kalkulasi precision@K untuk beberapa variasi nilai k
        int[] kValues = {5, 10, 15};
        for (int k : kValues) {
            int retrievedK = Math.min(k, totalRetrieved);
            int tpK = 0;
            for (int i = 0; i < retrievedK; i++) {
                if (relevantDocs.contains(results.get(i).getKey())) {
                    tpK++;
                }
            }
            double pAtK = retrievedK > 0 ? (double) tpK / retrievedK : 0.0;
            System.out.printf("Precision@%d: %.4f | ", k, pAtK);
        }
        System.out.println();

        // kalkulasi 11-point average precision dengan interpolasi
        double[] elevenPoints = new double[11];
        for (int i = 0; i <= 10; i++) {
            double targetRecall = i / 10.0;
            double maxPrecision = 0.0;
            for (int j = 0; j < totalRetrieved; j++) {
                // cari nilai precision tertinggi di sebelah kanan titik recall sekarang
                if (recalls[j] >= targetRecall) {
                    if (precisions[j] > maxPrecision) {
                        maxPrecision = precisions[j];
                    }
                }
            }
            elevenPoints[i] = maxPrecision;
        }

        // jumlahkan total dari 11 titik
        double sum11Point = 0.0;
        for (double p : elevenPoints) {
            sum11Point += p;
        }
        // bagi untuk mendapatkan rata-rata
        double avg11Point = sum11Point / 11.0;
        System.out.printf("11-Point Average Precision: %.4f\n", avg11Point);
    }
}
