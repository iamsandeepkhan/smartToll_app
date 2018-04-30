package com.example.android.metro;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;

/**
 * Class to scan QR code
 * @author Sandeep Khan
 */
public class QRScanner extends AppCompatActivity {

    private CameraSource cameraSource;
    private Handler mHandler;
    private String code;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrscanner);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mHandler = new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(Message msg) {
                Intent intent = new Intent();
                intent.putExtra("qrcode",code);
                setResult(Activity.RESULT_OK,intent);
                finish();
            }
        };

        final SurfaceView cameraView = (SurfaceView)findViewById(R.id.surface_view);

        BarcodeDetector barcodeDetector = new BarcodeDetector.Builder(this)
                                                .setBarcodeFormats(Barcode.QR_CODE)
                                                .build();
        final CameraSource.Builder cameraBuilder = new CameraSource.Builder(this,barcodeDetector)
                                                    .setFacing(CameraSource.CAMERA_FACING_BACK)
                                                    .setRequestedPreviewSize(1600,1024)
                                                    .setRequestedFps(15.0f);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH){
            cameraBuilder.setAutoFocusEnabled(true);
        }

        cameraSource = cameraBuilder.build();

        cameraView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                try {
                    cameraSource.start(cameraView.getHolder());
                } catch (SecurityException e) {
                    Log.e("QRScanner Activity", e.getMessage());
                    cameraSource.release();
                    cameraSource = null;
                }catch (IOException e){
                    Log.e("QRScanner Activity", e.getMessage());
                    cameraSource.release();
                    cameraSource = null;
                }
            }
            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                cameraSource.stop();
            }
        });


        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {

            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> barcodes = detections.getDetectedItems();
                if(barcodes.size()>0){
                    code = barcodes.valueAt(0).displayValue;
                    mHandler.sendEmptyMessage(0);
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(cameraSource!=null)
            cameraSource.release();
        cameraSource = null;
    }
}
