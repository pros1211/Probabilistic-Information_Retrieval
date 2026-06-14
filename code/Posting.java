public class Posting {
    private int docId;
    private int tf;

    public Posting(int docId, int termFreq) {
        this.docId = docId;
        this.tf = termFreq;
    }

    public int getDocId() {
        return docId;
    }

    public int getTf() {
        return tf;
    }

    public void incrementTf() {
        this.tf++;
    }
}
