package src.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import src.structures.*;
import src.utils.TextProcessor;

public class BM25 {
    private InvertedIndex invertedIndex;
    // atribut parameter k dan b
    private double k = 1.2;
    private double b = 0.75;

    public BM25(InvertedIndex invertedIndex) {
        this.invertedIndex = invertedIndex;
    }

    // method untuk menghitung bobot wt
    private double calculateWt(String term) {
        int nt = invertedIndex.getDocumentFrequency(term);
        int n = invertedIndex.getTotalDoc();
        if (nt == 0) {
            return 0.0;
        }
        return Math.log(0.5 * ((double) n / nt));
    }

    // method untuk kalkulasi skor bm25
    public List<Map.Entry<Integer, Double>> scoreBM25(String queryText) {
        LinkedList<String> queryTerms = TextProcessor.tokenizeString(queryText);
        HashMap<Integer, Double> docScore = new HashMap<>();
        // ambil rata-rata panjang dokumen
        double avgdl = invertedIndex.getAvgdl();

        // loop tiap term pada query
        for (String term : queryTerms) {
            double wt = calculateWt(term);
            if (wt == 0.0)
                continue;
            List<Posting> postings = invertedIndex.getPostingList(term);

            // looping setiap dokumen yang mengandung term
            for (Posting posting : postings) {
                int docId = posting.getDocId();
                int tf = posting.getTf();
                // ambil panjang dokumen spesifik
                int ld = invertedIndex.getDocument(docId).getLength();

                double numerator = tf * (k + 1) * wt;
                // rumus bm25 dengan normalisasi panjang dokumen
                double denominator = tf + k * (1 - b + b * (ld / avgdl));

                double termScore = numerator / denominator;

                double currentScore = docScore.getOrDefault(docId, 0.0);
                docScore.put(docId, currentScore + termScore);
            }
        }
        return sortResults(docScore);
    }

    // method untuk mengurutkan hasil
    private List<Map.Entry<Integer, Double>> sortResults(HashMap<Integer, Double> scoresMap) {
        List<Map.Entry<Integer, Double>> sortedList = new ArrayList<>(scoresMap.entrySet());

        sortedList.sort((a, c) -> c.getValue().compareTo(a.getValue()));

        return sortedList;
    }
}