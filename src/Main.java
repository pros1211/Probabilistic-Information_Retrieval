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

    // method untuk menampilkan hasil dan kalkulasi metrik agar format seragam
    public static void displayResults(String modelName, List<Map.Entry<Integer, Double>> results, HashSet<Integer> relevantDocs) {
        int limit = Math.min(10, results.size());
        System.out.println("\n");
        System.out.println("TOP " + limit + " " + modelName.toUpperCase() + "");
        System.out.println("Rank\tDocID\tSkor\tRelevan?");
        for (int i = 0; i < limit; i++) {
            int docId = results.get(i).getKey();
            double score = results.get(i).getValue();
            boolean isRelevant = relevantDocs.contains(docId);
            System.out.printf(" %2d\t%d\t%.4f\t  %s\n", (i + 1), docId, score, isRelevant ?  "✅" : "❌");
        }
        System.out.println();
        System.out.println("METRIK EVALUASI " + modelName.toUpperCase());
        Evaluator.calculateMetrics(results, relevantDocs);
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
        BM11 bm11Model = new BM11(dictionary);
        BM25 bm25Model = new BM25(dictionary);

        // load test query dari cran.qry
        HashMap<Integer, String> testQueries = Evaluator.loadQueries(queryFile);
        // load relevance judgment untuk evaluasi dokumen hasil
        HashMap<Integer, HashSet<Integer>> relevanceJudgement = Evaluator.loadRelevance(relevanceFile);

        // sorting query ID agar urutan evaluasi konsisten pada setiap run
        List<Integer> sortedQueryIds = new ArrayList<>(testQueries.keySet());
        Collections.sort(sortedQueryIds);

        // query dievaluasi
        int queriesEvaluated = 0;
        
        // loop test query cranfield sebanyak 5 (sebagai sampel pengujian)
        for (int queryId : sortedQueryIds) {
            if (queriesEvaluated >= 5) {
                break;
            }
            
            String queryText = testQueries.get(queryId);
            System.out.println("\n\n\n\n");
            System.out.println("====================================================");
            System.out.println("Query #" + queryId + "\n“" + queryText + "”");
            
            // jika query tidak ada di relevance judgment maka tidak dievaluasi
            if (!relevanceJudgement.containsKey(queryId))
                continue;
                
            // ambil list dokumen relevan dengan query id
            HashSet<Integer> relevantDocs = relevanceJudgement.get(queryId);

            // 1. Evaluasi BIM Model
            List<Map.Entry<Integer, Double>> resultsBIM = bimModel.scoreQuery(queryText);
            displayResults("BIM Model", resultsBIM, relevantDocs);

            // 2. Evaluasi Two-Poisson Model
            List<Map.Entry<Integer, Double>> resultsTP = twoPoissonModel.scoreTwoPoisson(queryText);
            displayResults("Two-Poisson Model", resultsTP, relevantDocs);

            // 3. Evaluasi BM11 Model
            List<Map.Entry<Integer, Double>> resultsBM11 = bm11Model.scoreBM11(queryText);
            displayResults("BM11 Model", resultsBM11, relevantDocs);

            // 4. Evaluasi BM25 Model
            List<Map.Entry<Integer, Double>> resultsBM25 = bm25Model.scoreBM25(queryText);
            displayResults("BM25 Model", resultsBM25, relevantDocs);

            queriesEvaluated++;
        }
        
        System.out.printf("\n--- PROSES EVALUASI SELESAI ---\n");
        System.out.printf("Total Query Dievaluasi : %d\n", queriesEvaluated);
    }
}