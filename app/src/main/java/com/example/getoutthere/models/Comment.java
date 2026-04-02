package com.example.getoutthere.models;
import com.google.firebase.Timestamp;
/**
 * Represents a comment under an event.
 **/
public class Comment {
    private String entrantId;
    private String entrantName;
    private String content;
    private Timestamp timestamp;

    // Required empty constructor for Firestore
    public Comment() { }

    public Comment(String entrantId, String entrantName, String content, Timestamp timestamp) {
        this.entrantId = entrantId;
        this.entrantName = entrantName;
        this.content = content;
        this.timestamp = timestamp;
    }

    public String getEntrantId() { return entrantId; }
    public void setEntrantId(String entrantId) { this.entrantId = entrantId; }

    public String getEntrantName() { return entrantName; }
    public void setEntrantName(String entrantName) { this.entrantName = entrantName; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Timestamp getTimestamp() { return timestamp; }
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }
}
