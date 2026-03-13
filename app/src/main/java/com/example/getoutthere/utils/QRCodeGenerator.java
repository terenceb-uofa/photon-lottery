package com.example.getoutthere.utils;

import android.graphics.Bitmap;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;

// The following code used information from https://www.geeksforgeeks.org/android/how-to-generate-qr-code-in-android/, 2026-03-12

/**
 * Generates QR code images from text input.
 * <p>
 * This utility class is responsible for converting the stored event qr code string url into a QR code
 * bitmap that can be displayed in the app.
 * <p>
 * Outstanding Issues:
 * - None
 *  @author Yousaf Cheema
 *  @version 1.0
 */


public class QRCodeGenerator {

    /**
     * Generates a QR code bitmap from the given text.
     * The given text in this case is the qr code content url stored in the firestore in the event
     *
     * @param text the text to encode into the QR code
     * @return a Bitmap containing the generated QR code
     * @throws WriterException if the QR code cannot be generated
     */
    public Bitmap generateQRCode(String text) throws WriterException {
        BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
        return barcodeEncoder.encodeBitmap(text, BarcodeFormat.QR_CODE, 400, 400);
    }
}