package com.example.getoutthere.organizer;

import android.content.ContentValues;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.getoutthere.R;
import com.example.getoutthere.event.Event;
import com.example.getoutthere.repositories.EventRepository;
import com.example.getoutthere.utils.QRCodeGenerator;
import com.google.zxing.WriterException;

import java.io.OutputStream;

public class EventQrCodeActivity extends AppCompatActivity {

    private ImageView qrCodeImage;
    private Button backButton;
    private Button exportButton;
    private EventRepository eventRepository;
    private QRCodeGenerator qrCodeGenerator;

    private Bitmap qrCodeBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_qr_code);

        qrCodeImage = findViewById(R.id.qrCodeImage);
        backButton = findViewById(R.id.backButton);
        exportButton = findViewById(R.id.exportButton);


        eventRepository = new EventRepository();
        qrCodeGenerator = new QRCodeGenerator();

        String eventId = getIntent().getStringExtra("eventId");
        if (eventId == null || eventId.isEmpty()) {
            Toast.makeText(this, "Missing event id", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // go back a page when clicked
        backButton.setOnClickListener(v -> finish());

        // save qr code as image to photos gallery
        exportButton.setOnClickListener(v -> {
            if (qrCodeBitmap == null) {
                Toast.makeText(this, "QR code is not ready yet", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean saved = saveQrCodePhotos(qrCodeBitmap, "event_qr_" + eventId + ".png");
            if (saved) {
                Toast.makeText(this, "QR code saved to Photos", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to save QR code", Toast.LENGTH_SHORT).show();
            }
        });

        eventRepository.getEventById(eventId, new EventRepository.RepositoryCallback<Event>() {
            @Override
            public void onSuccess(Event event) {
                try {
                    String qrContent = event.getQrCodeContent();
                    if (qrContent == null || qrContent.isEmpty()) {
                        Toast.makeText(EventQrCodeActivity.this, "No QR data was found", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    qrCodeBitmap = qrCodeGenerator.generateQRCode(qrContent);
                    qrCodeImage.setImageBitmap(qrCodeBitmap);
                    exportButton.setEnabled(true);
                } catch (WriterException e) {
                    Toast.makeText(EventQrCodeActivity.this, "Failed to generate The QR code", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(EventQrCodeActivity.this, "Failed to load the event", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // used https://stackoverflow.com/questions/71729415/saving-an-image-and-displaying-it-in-gallery as a resource, 2026-03-13
    // https://developer.android.com/reference/android/provider/MediaStore
    private boolean saveQrCodePhotos(Bitmap bitmap, String fileName) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/GetOutThere");
            values.put(MediaStore.Images.Media.IS_PENDING, 1);
        }

        Uri uri = null;
        try {
            uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            if (uri == null) {
                return false;
            }

            OutputStream outputStream = getContentResolver().openOutputStream(uri);
            if (outputStream == null) {
                return false;
            }

            boolean compressed = bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            outputStream.flush();
            outputStream.close();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues updateValues = new ContentValues();
                updateValues.put(MediaStore.Images.Media.IS_PENDING, 0);
                getContentResolver().update(uri, updateValues, null, null);
            }

            return compressed;
        } catch (Exception e) {
            if (uri != null) {
                getContentResolver().delete(uri, null, null);
            }
            return false;
        }
    }
}