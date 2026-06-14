public class Document {
    private int docId;
    private String name;
    private int length;

    public Document(int docId, String name, int length) {
        this.docId = docId;
        this.name = name;
        this.length = length;
    }

    public int getDocId() {
        return docId;
    }

    public void setDocId(int docId) {
        this.docId = docId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

}
