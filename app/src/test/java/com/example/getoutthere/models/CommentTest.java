package com.example.getoutthere;

import static org.junit.Assert.assertEquals;
import com.example.getoutthere.models.Comment;
import com.google.firebase.Timestamp;
import org.junit.Test;
import java.util.Date;

/**
 * Unit test for the Comment model.
 * Verifies that the data container correctly holds and returns user input.
 */

// The tests in this file have been generated through Claude AI
public class CommentTest {

    @Test
    public void testCommentDataIntegrity() {
        String entrantId = "user123";
        String entrantName = "Terence";
        String content = "This event looks polished!";
        Timestamp now = new Timestamp(new Date());

        Comment comment = new Comment(entrantId, entrantName, content, now);

        assertEquals("Entrant ID should match", entrantId, comment.getEntrantId());
        assertEquals("Name should match", entrantName, comment.getEntrantName());
        assertEquals("Content should match", content, comment.getContent());
        assertEquals("Timestamp should match", now, comment.getTimestamp());
    }
}