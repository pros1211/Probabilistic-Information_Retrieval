import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class BIMModel {
    private InvertedIndex invertedIndex;

    public BIMModel(InvertedIndex invertedIndex) {
        this.invertedIndex = invertedIndex;
    }

    private double calculateWt(String term) {
        int n = invertedIndex.getTotalDoc();
        int nt = invertedIndex.getDocumentFrequency(term);
        if (nt == 0) {
            return 0.0;
        }
        return Math.log(0.5 * ((double) n / nt));
    }

    public List<Map.Entry<Integer, Double>> scoreQuery(String textQuery) {
        LinkedList<String> queryList = TextProcessor.tokenizeString(textQuery);
        HashSet<String> queryTerms = new HashSet<>(queryList);
        HashMap<Integer, Double> documentScore = new HashMap<>();
        for (String term : queryTerms) {
            double wt = calculateWt(term);
            if (wt == 0.0)
                continue;
            List<Posting> postings = invertedIndex.getPostingList(term);
            for (Posting posting : postings) {
                int docId = posting.getDocId();
                double currentScore = documentScore.getOrDefault(docId, 0.0);
                documentScore.put(docId, currentScore + wt);
            }
        }
        return sortResults(documentScore);
    }

    private List<Map.Entry<Integer, Double>> sortResults(HashMap<Integer, Double> scoresMap) {
        List<Map.Entry<Integer, Double>> sortedList = new ArrayList<>(scoresMap.entrySet());

        sortedList.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        return sortedList;
    }
}
