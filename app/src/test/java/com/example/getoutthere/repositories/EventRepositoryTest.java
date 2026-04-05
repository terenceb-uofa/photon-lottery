package com.example.getoutthere.repositories;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.getoutthere.event.Event;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

import java.lang.Exception;

// This test is AI generated with Anthropics Claude Code, Prompt: "Generate a unit test for the following code", Date: Mar 13, 2026

/**
 * Unit tests for EventRepository.
 *
 * These tests verify that events are created, retrieved,
 * and updated correctly through the repository.
 */
public class EventRepositoryTest {

    /**
     * Verifies that createEvent assigns an ID, default waitlist count,
     * default QR code content, and saves the event successfully when
     * no poster image is provided.
     */
    @Test
    public void createEvent_withoutPoster_savesEventSuccessfully() {
        try (MockedStatic<FirebaseFirestore> firestoreStatic = mockStatic(FirebaseFirestore.class);
             MockedStatic<FirebaseStorage> storageStatic = mockStatic(FirebaseStorage.class)) {

            FirebaseFirestore firestore = mock(FirebaseFirestore.class);
            FirebaseStorage storage = mock(FirebaseStorage.class);
            StorageReference storageReference = mock(StorageReference.class);
            CollectionReference collectionReference = mock(CollectionReference.class);
            DocumentReference generatedDocument = mock(DocumentReference.class);
            DocumentReference saveDocument = mock(DocumentReference.class);

            @SuppressWarnings("unchecked")
            Task<Void> saveTask = mock(Task.class);

            firestoreStatic.when(FirebaseFirestore::getInstance).thenReturn(firestore);
            storageStatic.when(FirebaseStorage::getInstance).thenReturn(storage);

            when(storage.getReference()).thenReturn(storageReference);
            when(firestore.collection("events")).thenReturn(collectionReference);
            when(collectionReference.document()).thenReturn(generatedDocument);
            when(generatedDocument.getId()).thenReturn("event123");
            when(collectionReference.document("event123")).thenReturn(saveDocument);
            when(saveDocument.set(any(Event.class))).thenReturn(saveTask);

            mockTaskSuccess(saveTask, null);

            EventRepository repository = new EventRepository();
            Event event = new Event();
            event.setName("Test Event");

            @SuppressWarnings("unchecked")
            EventRepository.RepositoryCallback<String> callback = mock(EventRepository.RepositoryCallback.class);

            repository.createEvent(event, null, callback);

            assertEquals("event123", event.getId());
            assertEquals(0, event.getCurrentWaitlistCount());
            assertEquals("getoutthere://event/event123", event.getQrCodeContent());

            verify(saveDocument).set(event);
            verify(callback).onSuccess("event123");
            verify(callback, never()).onFailure(any(Exception.class));
        }
    }

    /**
     * Verifies that getEventById returns the event when the Firestore document exists.
     */
    @Test
    public void getEventById_existingEvent_returnsEvent() {
        try (MockedStatic<FirebaseFirestore> firestoreStatic = mockStatic(FirebaseFirestore.class);
             MockedStatic<FirebaseStorage> storageStatic = mockStatic(FirebaseStorage.class)) {

            FirebaseFirestore firestore = mock(FirebaseFirestore.class);
            FirebaseStorage storage = mock(FirebaseStorage.class);
            StorageReference storageReference = mock(StorageReference.class);
            CollectionReference collectionReference = mock(CollectionReference.class);
            DocumentReference documentReference = mock(DocumentReference.class);
            DocumentSnapshot documentSnapshot = mock(DocumentSnapshot.class);

            @SuppressWarnings("unchecked")
            Task<DocumentSnapshot> getTask = mock(Task.class);

            firestoreStatic.when(FirebaseFirestore::getInstance).thenReturn(firestore);
            storageStatic.when(FirebaseStorage::getInstance).thenReturn(storage);

            when(storage.getReference()).thenReturn(storageReference);
            when(firestore.collection("events")).thenReturn(collectionReference);
            when(collectionReference.document("event123")).thenReturn(documentReference);
            when(documentReference.get()).thenReturn(getTask);

            Event storedEvent = new Event();
            storedEvent.setName("Loaded Event");

            when(documentSnapshot.exists()).thenReturn(true);
            when(documentSnapshot.toObject(Event.class)).thenReturn(storedEvent);
            when(documentSnapshot.getId()).thenReturn("event123");

            mockTaskSuccess(getTask, documentSnapshot);

            EventRepository repository = new EventRepository();

            @SuppressWarnings("unchecked")
            EventRepository.RepositoryCallback<Event> callback = mock(EventRepository.RepositoryCallback.class);

            repository.getEventById("event123", callback);

            assertEquals("event123", storedEvent.getId());
            verify(callback).onSuccess(storedEvent);
            verify(callback, never()).onFailure(any(Exception.class));
        }
    }

    /**
     * Verifies that getEventById returns an error when the event document does not exist.
     */
    @Test
    public void getEventById_missingEvent_callsFailure() {
        try (MockedStatic<FirebaseFirestore> firestoreStatic = mockStatic(FirebaseFirestore.class);
             MockedStatic<FirebaseStorage> storageStatic = mockStatic(FirebaseStorage.class)) {

            FirebaseFirestore firestore = mock(FirebaseFirestore.class);
            FirebaseStorage storage = mock(FirebaseStorage.class);
            StorageReference storageReference = mock(StorageReference.class);
            CollectionReference collectionReference = mock(CollectionReference.class);
            DocumentReference documentReference = mock(DocumentReference.class);
            DocumentSnapshot documentSnapshot = mock(DocumentSnapshot.class);

            @SuppressWarnings("unchecked")
            Task<DocumentSnapshot> getTask = mock(Task.class);

            firestoreStatic.when(FirebaseFirestore::getInstance).thenReturn(firestore);
            storageStatic.when(FirebaseStorage::getInstance).thenReturn(storage);

            when(storage.getReference()).thenReturn(storageReference);
            when(firestore.collection("events")).thenReturn(collectionReference);
            when(collectionReference.document("missingEvent")).thenReturn(documentReference);
            when(documentReference.get()).thenReturn(getTask);

            when(documentSnapshot.exists()).thenReturn(false);

            mockTaskSuccess(getTask, documentSnapshot);

            EventRepository repository = new EventRepository();

            @SuppressWarnings("unchecked")
            EventRepository.RepositoryCallback<Event> callback = mock(EventRepository.RepositoryCallback.class);

            repository.getEventById("missingEvent", callback);

            ArgumentCaptor<Exception> exceptionCaptor = ArgumentCaptor.forClass(Exception.class);
            verify(callback).onFailure(exceptionCaptor.capture());

            assertNotNull(exceptionCaptor.getValue());
            assertEquals("Event not found", exceptionCaptor.getValue().getMessage());
            verify(callback, never()).onSuccess(any());
        }
    }

    /**
     * Verifies that updateEvent saves the updated event successfully
     * when no new poster image is provided.
     */
    @Test
    public void updateEvent_withoutPoster_updatesSuccessfully() {
        try (MockedStatic<FirebaseFirestore> firestoreStatic = mockStatic(FirebaseFirestore.class);
             MockedStatic<FirebaseStorage> storageStatic = mockStatic(FirebaseStorage.class)) {

            FirebaseFirestore firestore = mock(FirebaseFirestore.class);
            FirebaseStorage storage = mock(FirebaseStorage.class);
            StorageReference storageReference = mock(StorageReference.class);
            CollectionReference collectionReference = mock(CollectionReference.class);
            DocumentReference documentReference = mock(DocumentReference.class);

            @SuppressWarnings("unchecked")
            Task<Void> saveTask = mock(Task.class);

            firestoreStatic.when(FirebaseFirestore::getInstance).thenReturn(firestore);
            storageStatic.when(FirebaseStorage::getInstance).thenReturn(storage);

            when(storage.getReference()).thenReturn(storageReference);
            when(firestore.collection("events")).thenReturn(collectionReference);
            when(collectionReference.document("event123")).thenReturn(documentReference);
            when(documentReference.set(any(Event.class))).thenReturn(saveTask);

            mockTaskSuccess(saveTask, null);

            EventRepository repository = new EventRepository();
            Event event = new Event();
            event.setId("event123");
            event.setName("Updated Event");

            @SuppressWarnings("unchecked")
            EventRepository.RepositoryCallback<Void> callback = mock(EventRepository.RepositoryCallback.class);

            repository.updateEvent(event, null, callback);

            verify(documentReference).set(event);
            verify(callback).onSuccess(null);
            verify(callback, never()).onFailure(any(Exception.class));
        }
    }

    /**
     * Mocks a successful Firebase task and immediately triggers its success listener.
     *
     * @param task the mocked task
     * @param result the result returned to the success listener
     * @param <T> the task result type
     */
    private <T> void mockTaskSuccess(Task<T> task, T result) {
        when(task.addOnSuccessListener(any())).thenAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            OnSuccessListener<T> listener = invocation.getArgument(0);
            listener.onSuccess(result);
            return task;
        });

        when(task.addOnFailureListener(any())).thenReturn(task);
    }

    /**
     * Mocks a failed Firebase task and immediately triggers its failure listener.
     *
     * @param task the mocked task
     * @param exception the exception returned to the failure listener
     * @param <T> the task result type
     */
    private <T> void mockTaskFailure(Task<T> task, Exception exception) {
        when(task.addOnSuccessListener(any())).thenReturn(task);

        when(task.addOnFailureListener(any())).thenAnswer(invocation -> {
            OnFailureListener listener = invocation.getArgument(0);
            listener.onFailure(exception);
            return task;
        });
    }
}