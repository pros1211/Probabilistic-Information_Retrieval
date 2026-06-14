import java.io.*;
import java.util.*;

public class Main {
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
        /// atribut query test dari cranfield dataset (hanya diambil 5-10)
        File queryFile = new File("./datasets/cran.qry");
        // atribut file relevance judgment dari cranfield
        File relevanceFile = new File("./datasets/cranqrel");

        // inverted index
        InvertedIndex dictionary = new InvertedIndex(docFile);
        // bangun inverted index yang menyimpan jumlah term freq
        dictionary.buildIndex();
        // total dokumen di parsing
        System.out.println("total dokumen: " + dictionary.getTotalDoc());
        // inisialisasi BIMmodel dengan inverted index yang dibuat
        BIMModel bimModel = new BIMModel(dictionary);

        TwoPoisson twoPoissonModel = new TwoPoisson(dictionary);
        // load test query dari cran.qry
        HashMap<Integer, String> testQueries = Evaluator.loadQueries(queryFile);
        // load relevance judgment untuk evaluasi dokumen hasil BIM model
        HashMap<Integer, HashSet<Integer>> relevanceJudgement = Evaluator.loadRelevance(relevanceFile);

        // total precision hasil pencarian
        double totalPrecisionBIM = 0.0;
        double totalPrecisionTP = 0.0;
        // query dievaluasi
        int queriesEvaluated = 0;
        // loop test query cranfield sebanyak 5
        for (Map.Entry<Integer, String> entry : testQueries.entrySet()) {
            if (queriesEvaluated >= 5) {
                break;
            }
            // ambil id query dan text query
            int queryId = entry.getKey();
            String queryText = entry.getValue();
            System.out.println("query: " + queryText);
            // jika query tidak ada di relevance judgment maka tidak dievaluasi
            if (!relevanceJudgement.containsKey(queryId))
                continue;
            // ambil list dokumen relevan dengan query id
            HashSet<Integer> relevantDocs = relevanceJudgement.get(queryId);
            // hitung skor BIM berdasarkan inverted index
            List<Map.Entry<Integer, Double>> results = bimModel.scoreQuery(queryText);
            // limit hanya 10 dokumen teratas yang ditampilkan
            int BIMTrue = 0;
            int limit = Math.min(10, results.size());
            System.out.println("Hasil Top " + limit + " Dokumen (BIM Model):");
            // loop tiap dokumen hasil BIM
            for (int i = 0; i < limit; i++) {
                int docId = results.get(i).getKey();
                double score = results.get(i).getValue();
                boolean isRelevant = relevantDocs.contains(docId);
                if (relevantDocs.contains(docId)) {
                    BIMTrue++;
                }
                String status = isRelevant ? "[Relevan]" : "[Tidak relevan]";
                System.out.printf("Rank %d | DocID: %d | Skor: %.4f %s\n", (i + 1), docId, score, status);
            }
            // hitung nilai precision query dari 10 dokumen teratas hasil kalkulasi BIM
            double precisionBIM = (double) BIMTrue / 10;
            totalPrecisionBIM += precisionBIM;

            // two poisson model
            List<Map.Entry<Integer, Double>> tpResults = twoPoissonModel.scoreTwoPoisson(queryText);
            int tpHits = 0;
            int limitTp = Math.min(10, tpResults.size());

            System.out.println("\n[Hasil Top 10 - Two-Poisson Model]");
            for (int i = 0; i < limitTp; i++) {
                int docId = tpResults.get(i).getKey();
                double score = tpResults.get(i).getValue();
                boolean isRelevant = relevantDocs.contains(docId);
                if (isRelevant)
                    tpHits++;

                String status = isRelevant ? "[Relevan]" : "[Tidak relevan]";
                System.out.printf("Rank %d | DocID: %d | Skor: %.4f %s\n", (i + 1), docId, score, status);
            }
            queriesEvaluated++;
            double tpPrecision = (double) tpHits / 10;
            System.out.printf("\n>> Precision@10 Query %d | BIM: %.0f%% vs Two-Poisson: %.0f%%\n",
                    queryId, precisionBIM * 100, tpPrecision * 100);
            totalPrecisionTP += tpPrecision;
        }
        double meanBim = totalPrecisionBIM / queriesEvaluated;
        double meanTp = totalPrecisionTP / queriesEvaluated;
        System.out.printf("\n--- HASIL AKHIR ---\n");
        System.out.printf("Total Query Dievaluasi : %d\n", queriesEvaluated);
        System.out.printf("Rata-rata BIM Model         : %.4f (%.2f%%)\n", meanBim, meanBim * 100);
        System.out.printf("Rata-rata Two-Poisson Model : %.4f (%.2f%%)\n", meanTp, meanTp * 100);

    }

}