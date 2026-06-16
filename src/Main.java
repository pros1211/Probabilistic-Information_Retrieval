package src;

import java.io.*;
import java.util.*;

import src.models.*;
import src.structures.*;
import src.utils.Evaluator;
import src.utils.TextProcessor;

public class Main {
    // method untuk melakukan pengecekan term frequency
    public static void cekTermFrequency(InvertedIndex index, String queryText) {
        System.out.println("Query Asli: " + queryText);

        LinkedList<String> queryTerms = TextProcessor.tokenizeString(queryText);
        System.out.println("Token Query: " + queryTerms);

        for (String term : queryTerms) {
            System.out.println("\nterm: [" + term + "]");

            List<Posting> postings = index.getPostingList(term);

            if (postings == null || postings.isEmpty()) {
                System.out.println("term ini tidak ditemukan di database.");
                continue;
            }

            for (Posting posting : postings) {
                int docId = posting.getDocId();
                int tf = posting.getTf();

                System.out.println("  -> DocID: " + docId + " | TF: " + tf + " kali");
            }
        }
        System.out.println("====================================\n");
    }

    public static void main(String[] args) throws IOException {
        // atribut docFile cranfield berisi dokumen cranfield
        File docFile = new File("./datasets/cran.all.1400");
        // atribut query test dari cranfield dataset
        File queryFile = new File("./datasets/cran.qry");
        // atribut file relevance judgment dari cranfield
        File relevanceFile = new File("./datasets/cranqrel");

        // inisialisasi struktur inverted index
        InvertedIndex dictionary = new InvertedIndex(docFile);
        // bangun inverted index yang menyimpan jumlah term freq
        dictionary.buildIndex();
        // total dokumen yang berhasil di parsing
        System.out.println("total dokumen: " + dictionary.getTotalDoc());
        
        // inisialisasi semua model probabilistik dengan inverted index yang dibuat
        BIMModel bimModel = new BIMModel(dictionary);
        TwoPoisson twoPoissonModel = new TwoPoisson(dictionary);
        BM10 bm10Model = new BM10(dictionary);
        BM25 bm25Model = new BM25(dictionary);

        // load test query dari cran.qry
        HashMap<Integer, String> testQueries = Evaluator.loadQueries(queryFile);
        // load relevance judgment untuk evaluasi dokumen hasil
        HashMap<Integer, HashSet<Integer>> relevanceJudgement = Evaluator.loadRelevance(relevanceFile);

        // query dievaluasi
        int queriesEvaluated = 0;
        
        // loop test query cranfield sebanyak 5 (sebagai sampel pengujian)
        for (Map.Entry<Integer, String> entry : testQueries.entrySet()) {
            if (queriesEvaluated >= 5) {
                break;
            }
            
            // ambil id query dan text query
            int queryId = entry.getKey();
            String queryText = entry.getValue();
            System.out.println("\n=======================================================");
            System.out.println("QUERY ID: " + queryId + " | TEKS: " + queryText);
            System.out.println("=======================================================");
            
            // jika query tidak ada di relevance judgment maka tidak dievaluasi
            if (!relevanceJudgement.containsKey(queryId))
                continue;
                
            // ambil list dokumen relevan dengan query id
            HashSet<Integer> relevantDocs = relevanceJudgement.get(queryId);

            // 1. Evaluasi BIM Model
            List<Map.Entry<Integer, Double>> resultsBIM = bimModel.scoreQuery(queryText);
            int limitBIM = Math.min(10, resultsBIM.size());
            System.out.println("\n[Hasil Top " + limitBIM + " - BIM Model]");
            for (int i = 0; i < limitBIM; i++) {
                int docId = resultsBIM.get(i).getKey();
                double score = resultsBIM.get(i).getValue();
                boolean isRelevant = relevantDocs.contains(docId);
                System.out.printf("Rank %d | DocID: %d | Skor: %.4f %s\n", (i + 1), docId, score, isRelevant ? "[Relevan]" : "[Tidak relevan]");
            }
            System.out.println("--- Metrik Evaluasi BIM ---");
            Evaluator.calculateMetrics(resultsBIM, relevantDocs);

            // 2. Evaluasi Two-Poisson Model
            List<Map.Entry<Integer, Double>> resultsTP = twoPoissonModel.scoreTwoPoisson(queryText);
            int limitTP = Math.min(10, resultsTP.size());
            System.out.println("\n[Hasil Top " + limitTP + " - Two-Poisson Model]");
            for (int i = 0; i < limitTP; i++) {
                int docId = resultsTP.get(i).getKey();
                double score = resultsTP.get(i).getValue();
                boolean isRelevant = relevantDocs.contains(docId);
                System.out.printf("Rank %d | DocID: %d | Skor: %.4f %s\n", (i + 1), docId, score, isRelevant ? "[Relevan]" : "[Tidak relevan]");
            }
            System.out.println("--- Metrik Evaluasi Two-Poisson ---");
            Evaluator.calculateMetrics(resultsTP, relevantDocs);

            // 3. Evaluasi BM10 Model
            List<Map.Entry<Integer, Double>> resultsBM10 = bm10Model.scoreBM10(queryText);
            int limitBM10 = Math.min(10, resultsBM10.size());
            System.out.println("\n[Hasil Top " + limitBM10 + " - BM10 Model]");
            for (int i = 0; i < limitBM10; i++) {
                int docId = resultsBM10.get(i).getKey();
                double score = resultsBM10.get(i).getValue();
                boolean isRelevant = relevantDocs.contains(docId);
                System.out.printf("Rank %d | DocID: %d | Skor: %.4f %s\n", (i + 1), docId, score, isRelevant ? "[Relevan]" : "[Tidak relevan]");
            }
            System.out.println("--- Metrik Evaluasi BM10 ---");
            Evaluator.calculateMetrics(resultsBM10, relevantDocs);

            // 4. Evaluasi BM25 Model
            List<Map.Entry<Integer, Double>> resultsBM25 = bm25Model.scoreBM25(queryText);
            int limitBM25 = Math.min(10, resultsBM25.size());
            System.out.println("\n[Hasil Top " + limitBM25 + " - BM25 Model]");
            for (int i = 0; i < limitBM25; i++) {
                int docId = resultsBM25.get(i).getKey();
                double score = resultsBM25.get(i).getValue();
                boolean isRelevant = relevantDocs.contains(docId);
                System.out.printf("Rank %d | DocID: %d | Skor: %.4f %s\n", (i + 1), docId, score, isRelevant ? "[Relevan]" : "[Tidak relevan]");
            }
            System.out.println("--- Metrik Evaluasi BM25 ---");
            Evaluator.calculateMetrics(resultsBM25, relevantDocs);

            queriesEvaluated++;
        }
        
        System.out.printf("\n--- PROSES EVALUASI SELESAI ---\n");
        System.out.printf("Total Query Dievaluasi : %d\n", queriesEvaluated);
    }
}