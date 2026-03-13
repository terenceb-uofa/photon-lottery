package com.example.getoutthere.utils;

import android.graphics.Bitmap;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;

// The following code used information from https://www.geeksforgeeks.org/android/how-to-generate-qr-code-in-android/, 2026-03-12

public class QRCodeGenerator {

    public Bitmap generateQRCode(String text) throws WriterException {
        BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
        return barcodeEncoder.encodeBitmap(text, BarcodeFormat.QR_CODE, 400, 400);
    }

}
