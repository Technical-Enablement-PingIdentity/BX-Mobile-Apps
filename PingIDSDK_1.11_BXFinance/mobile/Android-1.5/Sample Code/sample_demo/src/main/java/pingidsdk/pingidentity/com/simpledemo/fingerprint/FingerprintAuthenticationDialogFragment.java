package pingidsdk.pingidentity.com.simpledemo.fingerprint;

import android.app.DialogFragment;
import android.content.Context;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.RequiresApi;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import pingidsdk.pingidentity.com.simpledemo.BaseActivity;
import pingidsdk.pingidentity.com.simpledemo.R;

/**
 * A dialog which uses fingerprint APIs to authenticate the user, and falls back to password
 * authentication if fingerprint is not available.
 * Created by evgeniymishustin on 11/5/18.
 */
@RequiresApi(api = Build.VERSION_CODES.M)
public class FingerprintAuthenticationDialogFragment extends DialogFragment
        implements FingerprintHelper.Callback  {

    private FingerprintHelper mFingerprintHelper;
    private FingerprintManager.CryptoObject mCryptoObject;
    private BaseActivity mActivity;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Do not create a new Fragment when the Activity is re-created such as orientation changes.
        setRetainInstance(true);
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Material_Light_Dialog);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (BaseActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getDialog().setTitle(getString(R.string.auth_please_authenticate));
        View v = inflater.inflate(R.layout.fingerprint_dialog_container, container, false);
        Button mCancelButton = (Button) v.findViewById(R.id.cancel_button);
        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });


        mFingerprintHelper = new FingerprintHelper(
                mActivity.getSystemService(FingerprintManager.class),
                (ImageView) v.findViewById(R.id.fingerprint_icon),
                (TextView) v.findViewById(R.id.fingerprint_status), this);
        // If fingerprint authentication is not available
        if (!mFingerprintHelper.isFingerprintAuthAvailable()) {

        }
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        mFingerprintHelper.startListening(mCryptoObject);
    }

    @Override
    public void onPause() {
        super.onPause();
        mFingerprintHelper.stopListening();
    }

    @Override
    public void onAuthenticated() {
        mActivity.onFingerPrintAuthenticatedCallback();
    }

    @Override
    public void onError() {

    }
}
