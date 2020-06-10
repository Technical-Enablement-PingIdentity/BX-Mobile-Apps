package pingidsdk.pingidentity.com.simpledemo;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import java.util.Calendar;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;
import pingidsdkclient.PingID;

import static pingidsdk.pingidentity.com.simpledemo.PingIDSdkDemoApplication.showStatus;


public class OtpActivity extends BaseActivity {

    private EditText editText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);

        int year = Calendar.getInstance().get(Calendar.YEAR);
        TextView copyrightView = findViewById(R.id.copyright);
        copyrightView.setText(String.format(getString(R.string.copyright), year));

        TextView tv = (TextView) findViewById(R.id.passcode_prompt_textview);
        Typeface face = Typeface.createFromAsset(getAssets(),
                "fonts/HelveticaNeueLight.ttf");
        tv.setTypeface(face);
        //set version number in the bottom of the login screen
        TextView mVersionView = (TextView)findViewById(R.id.version_otp);
        mVersionView.setText(String.format("v%s",BuildConfig.VERSION_NAME));
        final Button button = (Button) findViewById(R.id.signinwithpass);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String passcode = editText.getText().toString();
                Intent intent = new Intent();
                intent.putExtra("passcode", passcode);
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        });

        editText = (EditText) findViewById(R.id.passcode_input);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.toString().length() > 0) {
                    if(!button.isEnabled()) {
                        button.setEnabled(true);
                    }
                } else {
                    if(button.isEnabled()) {
                        button.setEnabled(false);
                    }
                }
            }
        });
    }

    @Override
    void changeScreenAvailability(boolean available) {
        //do nothing
    }

    @Override
    void hideScanButton(boolean hide) {

    }
}
