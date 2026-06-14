import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class InvertedIndex {
    // atribut untuk menyimpan path file dataset
    private File folderPath;
    // atribut posting list yang menyimpan dictionary dan list posting yang
    // menyimpan term freq pada dokumen berdasarkan doc id
    private HashMap<String, List<Posting>> postingList = new HashMap<>();
    // list doc yang menyimpan seluruh list doc yang memiliki suatu term
    HashMap<Integer, Document> listDoc = new HashMap<>();
    // atribut panjang rata rata dokumen keseluruhan
    private double avgdl = 0.0;
    // atribut banyak dokumen
    private int totalDocuments = 0;

    public InvertedIndex(File folderPath) {
        this.folderPath = folderPath;
    }

    // method untuk membangun inverted index
    public void buildIndex() throws IOException {
        // menghitung panjang seluruh dokumen
        long totalLengthAllDocs = 0;
        BufferedReader br = new BufferedReader(new FileReader(folderPath));
        String line;
        String currentTag = "";
        int currentDocID = -1;
        StringBuilder currentTitle = new StringBuilder();
        StringBuilder currentContent = new StringBuilder();
        while ((line = br.readLine()) != null) {
            // jika line sekarang adalah index dokumen di cranfield
            if (line.startsWith(".I")) {
                // jika sebelumnya sudah pernah membaca dokumen maka simpan title dan isi
                // dokumen sebelumnya
                if (currentDocID != -1) {
                    String fullText = currentTitle.toString() + " " + currentContent.toString();
                    int docLength = processAndIndexDocument(currentDocID, fullText);
                    totalLengthAllDocs += docLength;
                }
                currentDocID = Integer.parseInt(line.substring(3).trim());
                currentTitle.setLength(0);
                currentContent.setLength(0);
                currentTag = "";
            }
            // jika baris sekarang adalah tag .T , .A, .B, .W maka simpan
            else if (line.startsWith(".") && line.length() == 2) {
                currentTag = line;
            } else {
                // simpan jika tag yang sekarang adalah title atau words
                if (currentTag.equals(".T")) {
                    currentTitle.append(line).append(" ");
                } else if (currentTag.equals(".W")) {
                    currentContent.append(line).append(" ");
                }
            }
        }
        // ambil dokumen terakhir
        if (currentDocID != -1) {
            String fullText = currentTitle.toString() + " " + currentContent.toString();
            int docLength = processAndIndexDocument(currentDocID, fullText);
            totalLengthAllDocs += docLength;
        }
        this.totalDocuments = listDoc.size();
        if (this.totalDocuments > 0) {
            this.avgdl = (double) totalLengthAllDocs / this.totalDocuments;
        }
    }

    // method tokenisasi isi dokumen dan hitung term frequency di dokumen
    private int processAndIndexDocument(int docId, String content) {
        LinkedList<String> terms = TextProcessor.tokenizeString(content);

        HashMap<String, Integer> termFreqDoc = new HashMap<>();
        // loop tiap term pada dokumen hasil tokenisasi untuk hitung term freq di
        // dokumen sekarang
        for (String term : terms) {
            if (termFreqDoc.containsKey(term)) {
                int currentFrequency = termFreqDoc.get(term);
                termFreqDoc.put(term, currentFrequency + 1);
            } else {
                termFreqDoc.put(term, 1);
            }
        }

        int docLength = terms.size();
        // tambahkan dokumen ke list document
        Document document = new Document(docId, "Doc-" + docId, docLength);
        listDoc.put(docId, document);

        for (String term : termFreqDoc.keySet()) {
            int tf = termFreqDoc.get(term);
            // jika belum ada di posting list maka simpan dan inisialisasi array list untuk
            // posting list tiap dokumen berisi tf
            postingList.putIfAbsent(term, new ArrayList<>());

            postingList.get(term).add(new Posting(docId, tf));
        }

        return docLength;
    }

    public int getDocumentFrequency(String term) {
        if (postingList.containsKey(term)) {
            return postingList.get(term).size();
        }
        return 0;
    }

    public int getTotalDoc() {
        return this.totalDocuments;
    }

    public List<Posting> getPostingList(String term) {
        return postingList.get(term);
    }

    public String getName(int docId) {
        return listDoc.get(docId).getName();
    }
}
