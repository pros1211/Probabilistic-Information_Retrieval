
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

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
}
