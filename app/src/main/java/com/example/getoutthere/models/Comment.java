package com.example.getoutthere.models;

import com.google.firebase.Timestamp;

/**
 * Represents a comment made by an entrant under a specific event.
 * <p>
 * This model class stores the details of a comment, including the unique ID
 * and name of the entrant who posted it, the text content of the comment,
 * and the exact timestamp of when it was created. It is designed to be fully
 * compatible with Firebase Firestore for easy serialization and deserialization.
 * </p>
 *
 * Outstanding Issues:
 * - None
 */

/**
 * Data model representing an event comment.
 * @version 1.0
 */
public class Comment {
    private String entrantId;
    private String entrantName;
    private String content;
    private Timestamp timestamp;

    /**
     * Required empty constructor for Firebase Firestore object deserialization.
     */
    public Comment() { }

    /**
     * Constructs a new Comment with the specified details.
     *
     * @param entrantId The unique identifier of the entrant making the comment.
     * @param entrantName The display name of the entrant.
     * @param content The actual text content of the comment.
     * @param timestamp The exact date and time the comment was posted.
     */
    public Comment(String entrantId, String entrantName, String content, Timestamp timestamp) {
        this.entrantId = entrantId;
        this.entrantName = entrantName;
        this.content = content;
        this.timestamp = timestamp;
    }

    /**
     * Gets the unique identifier of the entrant who posted the comment.
     *
     * @return The entrant's ID.
     */
    public String getEntrantId() { return entrantId; }

    /**
     * Sets the unique identifier of the entrant who posted the comment.
     *
     * @param entrantId The new entrant ID.
     */
    public void setEntrantId(String entrantId) { this.entrantId = entrantId; }

    /**
     * Gets the display name of the entrant who posted the comment.
     *
     * @return The entrant's name.
     */
    public String getEntrantName() { return entrantName; }

    /**
     * Sets the display name of the entrant who posted the comment.
     *
     * @param entrantName The new entrant name.
     */
    public void setEntrantName(String entrantName) { this.entrantName = entrantName; }

    /**
     * Gets the text content of the comment.
     *
     * @return The comment content.
     */
    public String getContent() { return content; }

    /**
     * Sets the text content of the comment.
     *
     * @param content The new comment content.
     */
    public void setContent(String content) { this.content = content; }

    /**
     * Gets the timestamp of when the comment was posted.
     *
     * @return The comment's timestamp.
     */
    public Timestamp getTimestamp() { return timestamp; }

    /**
     * Sets the timestamp of when the comment was posted.
     *
     * @param timestamp The new timestamp.
     */
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }
}