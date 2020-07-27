package pingidsdk.pingidentity.com.simpledemo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pingidsdk.pingidentity.com.simpledemo.qr.CameraPreview;
import pingidsdk.pingidentity.com.simpledemo.qr.CameraSource;
import pingidsdk.pingidentity.com.simpledemo.qr.QrTracker;
import pingidsdk.pingidentity.com.simpledemo.qr.QrTrackerFactory;

public class CameraActivity extends AppCompatActivity implements QrTracker.QrCodeUpdateListener {

    private static final String TAG = "CameraActivity";

    // permission request codes need to be < 256
    private static final int RC_HANDLE_CAMERA_PERM = 2;

    private CameraSource mCameraSource;
    private CameraPreview mPreview;

    private Button cancel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        mPreview = (CameraPreview) findViewById(R.id.camera_preview);

        Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/HelveticaNeueLight.ttf");
        cancel = (Button) findViewById(R.id.cancelButton);

        cancel.setTypeface(typeface);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelCamera();
            }
        });
        // Check for the camera permission before accessing the camera.  If the
        // permission is not granted yet, request permission.
        int rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (rc == PackageManager.PERMISSION_GRANTED) {
            createCameraSource();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, RC_HANDLE_CAMERA_PERM);
        }

    }

    /**
     * Creates and starts the camera.  Note that this uses a higher resolution in comparison
     * to other detection examples to enable the barcode detector to detect small barcodes
     * at long distances.
     */
    private void createCameraSource() {
        Context context = getApplicationContext();

        // A barcode detector is created to track barcodes.  An associated multi-processor instance
        // is set to receive the barcode detection results, track the barcodes, and maintain
        // graphics for each barcode on screen.  The factory is used by the multi-processor to
        // create a separate tracker instance for each barcode.
        BarcodeDetector barcodeDetector = new BarcodeDetector.Builder(context).build();
        QrTrackerFactory barcodeFactory = new QrTrackerFactory(this);
        barcodeDetector.setProcessor(new MultiProcessor.Builder<>(barcodeFactory).build());

        // Creates and starts the camera.  Note that this uses a higher resolution in comparison
        // to other detection examples to enable the barcode detector to detect small barcodes
        // at long distances.
        CameraSource.Builder builder = new CameraSource.Builder(getApplicationContext(), barcodeDetector)
                .setRequestedPreviewSize(1600, 1024)
                .setRequestedFps(15.0f)
                .setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);

        mCameraSource = builder.build();
    }

    /**
     * Starts or restarts the camera source, if it exists.  If the camera source doesn't exist yet
     * (e.g., because onResume was called before the camera source was created), this will be called
     * again when the camera source is created.
     */
    private void startCameraSource() throws SecurityException {
        // check that the device has play services available.
        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(getApplicationContext());
        if (code != ConnectionResult.SUCCESS) {
            Dialog dlg =
                    GoogleApiAvailability.getInstance().getErrorDialog(this, code, 3001);
            dlg.show();
        }
        if (mCameraSource != null) {
            try {
                mPreview.start(mCameraSource);
            } catch (IOException e) {
                Log.e(TAG, "Unable to start camera source.", e);
                mCameraSource.release();
                mCameraSource = null;
            }
        }
    }

    /**
     * Restarts the camera.
     */
    @Override
    protected void onResume() {
        super.onResume();
        startCameraSource();
    }

    /**
     * Stops the camera.
     */
    @Override
    protected void onPause() {
        super.onPause();
        if (mPreview != null) {
            mPreview.stop();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case RC_HANDLE_CAMERA_PERM: {
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    createCameraSource();
                }else{
                    finish();
                }
                break;
            }

        }
    }

    @Override
    public void onQrCodeDetected(Barcode barcode) {
        mPreview.stop();

        if(tryToDecode(barcode)) {
            try {
                Thread.sleep(200);
            } catch (Exception e) {
                e.printStackTrace();
            }
            Intent result = new Intent();
            result.putExtra("token", barcode.displayValue);
            setResult(Activity.RESULT_OK, result);
            finish();
        }else{
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showFailureLayout(true);

                }
            });
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @SuppressLint("MissingPermission")
                @Override
                public void run() {
                    showFailureLayout(false);
                    try {
                        mPreview.start(mCameraSource);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }, 2000);
        }

    }

    private boolean tryToDecode(Barcode barcode){
        String regex = ".*pingidsdk\\?authentication_token=.*";
        Pattern patt = Pattern.compile(regex);
        Matcher matcher = patt.matcher(barcode.displayValue);
        return matcher.matches();
    }

    private void showFailureLayout(boolean b){
        RelativeLayout failureLayout = (RelativeLayout) findViewById(R.id.layoutFailure);
        if(b) {
            failureLayout.setVisibility(View.VISIBLE);
        }else{
            failureLayout.setVisibility(View.GONE);
        }
    }

    private void cancelCamera(){
        onBackPressed();
    }
}
