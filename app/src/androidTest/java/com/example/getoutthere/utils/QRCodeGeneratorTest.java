package com.example.getoutthere.utils;

import android.graphics.Bitmap;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.zxing.WriterException;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(AndroidJUnit4.class)
public class QRCodeGeneratorTest {

    @Test
    public void testGenerateQRCodeReturnsValidBitmap() throws WriterException {
        // Arrange
        QRCodeGenerator generator = new QRCodeGenerator();
        String testEventId = "sample-event-id-123";

        // Act
        Bitmap qrCode = generator.generateQRCode(testEventId);

        // Assert
        assertNotNull("Generated QR Code Bitmap should not be null", qrCode);
        assertEquals("QR Code width should be exactly 400", 400, qrCode.getWidth());
        assertEquals("QR Code height should be exactly 400", 400, qrCode.getHeight());
    }
}