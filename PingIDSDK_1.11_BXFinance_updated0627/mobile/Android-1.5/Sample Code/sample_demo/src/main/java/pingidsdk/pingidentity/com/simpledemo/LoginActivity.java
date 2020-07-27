//
// Class Name : LoginActivity
// App name : Moderno
//
// A login screen that offers login via username/password + MFA by PingID for Customers
//
// See LICENSE.txt for this sample’s licensing information and LICENSE_SDK.txt for the PingID SDK library licensing information.
// Created by Ping Identity on 3/23/17.
// Copyright © 2017 Ping Identity. All rights reserved.
//

package pingidsdk.pingidentity.com.simpledemo;

import android.app.Activity;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import net.openid.appauth.AppAuthConfiguration;
import net.openid.appauth.AuthorizationException;
import net.openid.appauth.AuthorizationRequest;
import net.openid.appauth.AuthorizationResponse;
import net.openid.appauth.AuthorizationService;
import net.openid.appauth.AuthorizationServiceConfiguration;
import net.openid.appauth.AuthorizationServiceDiscovery;
import net.openid.appauth.CodeVerifierUtil;
import net.openid.appauth.ResponseTypeValues;
import net.openid.appauth.TokenResponse;
import net.openid.appauth.browser.BrowserBlacklist;
import net.openid.appauth.browser.Browsers;
import net.openid.appauth.browser.VersionRange;
import net.openid.appauth.browser.VersionedBrowserMatcher;
import net.openid.appauth.connectivity.ConnectionBuilder;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;
import pingidsdk.pingidentity.com.simpledemo.oauth.UnsecuredConnectionBuilder;
import pingidsdkclient.PingID;

import static pingidsdk.pingidentity.com.simpledemo.PingIDSdkDemoApplication.addLogLine;
import static pingidsdk.pingidentity.com.simpledemo.PingIDSdkDemoApplication.showStatus;

public class LoginActivity extends BaseActivity {

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final String EXTRA_AUTH_SERVICE_DISCOVERY = "extra_auth";
    private static final int OTP_INPUT_REQUEST = 9002;
    private static final String OIDC_ISSUER_PORT = "9032";

    public final String TAG = LoginActivity.class.getName();
    private String sum = "";
    // UI references.
    private EditText mUsernameView;
    private EditText mPasswordView;
    private ProgressBar progressBar;

    private Button scanButton;
    Typeface font;

    //otp helper value
    private String authSession;

    // The OAuth client ID. This is configured in your PingFederate administration console under OAuth Settings > Client Management.
    private static final String OIDC_CLIENT_ID = "PingID_SDK_Sample";

    // The redirect URI that PingFederate will send the user back to after the authorization step. To avoid
    // collisions, this should be a reverse domain formatted string. You must define this in your OAuth client in PingFederate.
    private static final String OIDC_REDIRECT_URI = "pingidsdksample://cb";

    //the scope to send to the ping federate
    private static final String OIDC_SCOPE = "openid profile";


    private AuthorizationService mAuthService;

    String payload = null;
    private boolean pingFedTriggerAuth = false;

    //auth types for requests to the customer server
    public enum OperationType {
        AUTH_USER("auth_user"),
        AUTH_OFFLINE_USER("auth_offline_user");
        private String name;
        OperationType(String name) {
            this.name = name;
        }
        public String getName() {
            return name;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //if this activity was launched from a click on a notification
        //display the addDeviceDialog
        if (getIntent() != null && getIntent().getExtras() != null && getIntent().getExtras().containsKey(TRUST_LEVELS) && getIntent().getExtras().getStringArrayList(TRUST_LEVELS) != null) {
            List<String> trustLevels = getIntent().getExtras().getStringArrayList(TRUST_LEVELS);
            displayAddDeviceToNetworkDialog(trustLevels);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        font = Typeface.createFromAsset(getAssets(), "fonts/HelveticaNeueLight.ttf");
        AppAuthConfiguration.Builder builder = new AppAuthConfiguration.Builder();
        builder.setConnectionBuilder(getConnectionBuilder());
        /*
         * Need the next string to resolve some bug with Samsung devices
         * where custom tabs browser activity never returns result
         */
        builder.setBrowserMatcher(new BrowserBlacklist(
                new VersionedBrowserMatcher(
                        Browsers.SBrowser.PACKAGE_NAME,
                        Browsers.SBrowser.SIGNATURE_SET,
                        true, // when this browser is used via a custom tab
                        VersionRange.atMost("5.3")
                )));

        mAuthService = new AuthorizationService(this, builder.build());

        mUsernameView = findViewById(R.id.username);

        mPasswordView = findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    login();
                    return true;
                }
                return false;
            }
        });

        scanButton = (Button) findViewById(R.id.buttonScan_login);
        scanButton.setTypeface(font);
        scanButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                launchScanActivity();
            }
        });

        hideScanButton(!PingID.getInstance().isDeviceTrusted());

        Button mSignInButton =  findViewById(R.id.signin);
        mSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                login();
            }
        });

        Button mPingFedSignInButton = findViewById(R.id.signin_pingfed);
        mPingFedSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                authenticateWithPingFederate();
            }
        });

        //load username, password and auth type from the app preferences
        mUsernameView.setText(AppPreferences.getUsername(this));
        mPasswordView.setText(AppPreferences.getPassword(this));

        //handle app config values
        try {
            //  PingID.getInstance().setPushDisabled(false);
        } catch (Exception e) {
            e.printStackTrace();
            addLogLine("PingID Consumer lib error : " + e.getMessage());
        }

        progressBar = findViewById(R.id.progressBar);

        //set version number in the bottom of the login screen
        TextView mVersionView = findViewById(R.id.version);
        mVersionView.setText(String.format("v%s",BuildConfig.VERSION_NAME));

        int year = Calendar.getInstance().get(Calendar.YEAR);
        TextView copyrightView = findViewById(R.id.copyright);
        copyrightView.setText(String.format(getString(R.string.copyright), year));

        checkPlayServices();

        addLogLine("App initialized");
        Intent intent = getIntent();
        if (intent != null) {
            Log.d(TAG, "Intent received");
            // Parse the authorization response
            AuthorizationResponse response = AuthorizationResponse.fromIntent(intent);
            AuthorizationException ex = AuthorizationException.fromIntent(intent);

            if (response != null || ex != null) {
            }

            if (response != null) {
                Log.d(TAG, "Received AuthorizationResponse.");
                mAuthService.performTokenRequest(response.createTokenExchangeRequest(), new AuthorizationService.TokenResponseCallback() {
                    @Override
                    public void onTokenRequestCompleted(@Nullable TokenResponse response, @Nullable AuthorizationException ex) {
                        pingFedTriggerAuth = true;
                        changeScreenAvailability(false);
                        PingID.getInstance().postIDPAuthenticationStepWithDataCenter(PingID.PIDDataCenterType.PIDDataCenterTypeNA);
                    }
                });
            } else {
                Log.i(TAG, "Authorization failed: " + ex);
            }

        } else {
            Log.d(TAG, "NO Intent received");
        }
        changeScreenAvailability(true);

        View modernoImageView = findViewById(R.id.moderno_image_view);
        modernoImageView.setOnLongClickListener(v -> {
            if (BuildConfig.DEBUG) {
                displayAppPrefs(LoginActivity.this);
            }
            return false;
        });
    }

    /*
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void login() {

        // Store values at the time of the login attempt.
        final String username = mUsernameView.getText().toString();
        final String password = mPasswordView.getText().toString();

        changeScreenAvailability(false);

        //save the username and password
        AppPreferences.setUsername(this, mUsernameView.getText().toString());
        AppPreferences.setPassword(this, mPasswordView.getText().toString());
        //start a new authentication
        try {
            attemptLogin(username, password,  null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
     * Attempts to get the authorization token from the PingFederate.
     */
    private void authenticateWithPingFederate() {
        // The OIDC issuer from which the configuration will be discovered. This is your base PingFederate server URL.
        final String OIDC_ISSUER = AppPreferences.getOidcIssuer(getApplicationContext());

        changeScreenAvailability(false);

        AppPreferences.setUsername(this, mUsernameView.getText().toString());
        AppPreferences.setPassword(this, mPasswordView.getText().toString());
        //Configuration Discovery Request
        String discoveryEndpoint = OIDC_ISSUER + "/.well-known/openid-configuration";

        final AuthorizationServiceConfiguration.RetrieveConfigurationCallback retrieveCallback =
                new AuthorizationServiceConfiguration.RetrieveConfigurationCallback() {

                    @Override
                    public void onFetchConfigurationCompleted(
                            @Nullable final AuthorizationServiceConfiguration serviceConfiguration,
                            @Nullable AuthorizationException ex) {
                        if (ex != null) {
                            Log.w(TAG, "Failed to retrieve configuration for " + OIDC_ISSUER, ex);
                            Toast.makeText(getApplicationContext(), "Failed to retrieve configuration for " + OIDC_ISSUER, Toast.LENGTH_LONG).show();
                            changeScreenAvailability(true);
                        } else {
                            Log.d(TAG, "configuration retrieved for " + OIDC_ISSUER
                                    + ", proceeding");
                            /*
                             * Retrieve payload from PingID SDK in asynchronous way
                             */
                            try {
                                PingID.getInstance().generatePayload(new PingID.PayloadCallback() {
                                    @Override
                                    public void onPayloadGenerated(String payload) {
                                        authorize(serviceConfiguration, payload);
                                    }
                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        }
                    }
                };
        AuthorizationServiceConfiguration.fetchFromUrl(Uri.parse(discoveryEndpoint), retrieveCallback, getConnectionBuilder());
    }

    private ConnectionBuilder getConnectionBuilder() {
        return UnsecuredConnectionBuilder.INSTANCE;
    }

    private void authorize(AuthorizationServiceConfiguration serviceConfiguration, String payload) {
        // NOTE: Required for PingFederate 8.1 and below for the .setCodeVerifier() option below
        // to generate "plain" code_challenge_method these versions of PingFederate do not support
        // S256 PKCE.
        String codeVerifier = CodeVerifierUtil.generateRandomCodeVerifier();

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                changeScreenAvailability(true);
            }
        });

        //set-up payload as additional parameters of request
        Map<String, String> additionalParams = new HashMap<>();
        additionalParams.put("payload", payload);

        //build an authorization request
        AuthorizationRequest authRequest = new AuthorizationRequest.Builder(
                serviceConfiguration,
                OIDC_CLIENT_ID,
                ResponseTypeValues.CODE,
                Uri.parse(OIDC_REDIRECT_URI))
                .setScope(OIDC_SCOPE)
                .setCodeVerifier(codeVerifier, codeVerifier, "plain")
                .setAdditionalParameters(additionalParams)
                .build();


        mAuthService.performAuthorizationRequest(
                authRequest,
                createPostAuthorizationIntent(
                        this.getApplicationContext(),
                        authRequest,
                        serviceConfiguration.discoveryDoc));
        finish();
    }

    private PendingIntent createPostAuthorizationIntent(
            @NonNull Context context,
            @NonNull AuthorizationRequest request,
            @Nullable AuthorizationServiceDiscovery discoveryDoc) {

        Intent intent = new Intent(context, this.getClass());
        if (discoveryDoc != null) {
            intent.putExtra(EXTRA_AUTH_SERVICE_DISCOVERY, discoveryDoc.docJson.toString());
        }

        return PendingIntent.getActivity(context, request.hashCode(), intent, 0);
    }

    public void attemptLogin(final String username, final String password, final String userAnswer, final String passcode) {
        try {
            PingID.getInstance().generatePayload(new PingID.PayloadCallback() {
                @Override
                public void onPayloadGenerated(String payload) {
                    if (payload != null && payload.length() > 4050) {
                        System.out.println("payload generated length=" + payload.length());
                        for (int i = 0; i < payload.length(); i += 4050) {
                            int endI = Math.min(i + 4050, payload.length());
                            System.out.println(payload.substring(i, endI));
                        }
                    }
                    else {
                        System.out.println("payload generated " + payload);
                    }
                    attemptLogin(username, password, payload, userAnswer, passcode);
                }

            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /*
     * Attempts to contact the server to authenticate the user
     *d3M6e3dlOiBbXX0sbXM6e2E6IG0sIHBUOiBHU00sblQ6IExURSxuTzogIjAxMiBNb2JpbGUiLH0=
     * @param username - Username
     * @param password - Password YzJhMGI3NDk0YTZjYzIxNw==
     * @param clientPayload - A string containing a client payload in case this function is execute a second time in a flow
     * @param userAnswer - A string containing the type if device pairing in case this function is execute a second time in a flow
     */
    public void attemptLogin(String username, String password, String clientPayload, final String userAnswer, String passcode) {

        Log.d(TAG, "attemptLoginRequest start");

        try {
            final pingidsdk.pingidentity.com.simpledemo.beans.LoginRequest request = new pingidsdk.pingidentity.com.simpledemo.beans.LoginRequest();

            if(passcode == null) {
                //this may be a second attempt to login (if a primary device of this account is offline) so we generate a payload if
                //the one passed as a parameter is null
                if (clientPayload == null) {
                    payload = PingID.getInstance().generatePayload();
                } else {
                    payload = clientPayload;
                }
                //prepare the LoginRequest data
                request.setAuthType("pingID_online");
                request.setOperation(OperationType.AUTH_USER.name);
                request.setUser(username);
                request.setPassword(password);
                request.setPingIdPayload(payload);
            } else {
                String updatedPayload = PingID.getInstance().updateExistingPayloadWithUserSelection(PingID.PIDTrustLevel.PIDTrustLevelTrusted.getName(), "none");
                request.setSessionId(authSession);
                request.setOperation(OperationType.AUTH_OFFLINE_USER.getName());
                request.setPingIdPayload(updatedPayload);
                request.setOtp(passcode);
            }

            //reset
            PingIDSdkDemoApplication.getInstance().setHomeActivityLoginFromAttemptLogin(false);

            String requestUrl = AppPreferences.getCustomerServerBaseUrl(getApplicationContext());

            AsyncHttpClient client = new AsyncHttpClient();
            client.setTimeout(40000);

            Gson gson = new GsonBuilder().disableHtmlEscaping().create();
            String json = gson.toJson(request);
            Log.d(TAG, "requestUrl=" + requestUrl + " request=" + json);
            StringEntity entity = new StringEntity(json, "UTF-8");
            changeScreenAvailability(false);
            AsyncHttpResponseHandler responseHandler = new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(final int statusCode, Header[] headers, final byte[] responseBody) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            String responseBodyString = "";
                            try {
                                responseBodyString = new String(responseBody);

                                //check if the response is empty (error)
                                if (responseBodyString.length() == 0) {
                                    addLogLine("response is empty");
                                    changeScreenAvailability(true);
                                    return;
                                }
                                Log.d(TAG, "attemptLogin onSuccess, statusCode=" + statusCode + ",responseBody" + responseBodyString);
                                addLogLine("authenticate response received : " + responseBodyString);

                                Gson gson = new Gson();
                                pingidsdk.pingidentity.com.simpledemo.beans.LoginResponse response = gson.fromJson(responseBodyString, pingidsdk.pingidentity.com.simpledemo.beans.LoginResponse.class);

                                //for automatic pairing
                                if (response.getStatus() == 1018) {
                                    if (response.getAvailableDevicesForAuthentication() != null && response.getAvailableDevicesForAuthentication().size() > 0) {
                                        addLogLine("Select device Scenario not supported in this demo.");
                                    } else {
                                        showStatus("Login request, status : " + response.getStatus() + " - " + response.getDescription());
                                    }
                                } else if (response.getStatus() == 1015) {
                                    showStatus("Login request, status : " + response.getStatus() + " - " + response.getDescription());
                                    authSession = response.getAuthSessionID();
                                    PingID.getInstance().setServerPayload(response.getServerPayload(), null, response.getCurrentAuthenticatingDeviceData() != null ? response.getCurrentAuthenticatingDeviceData().getName() : null);

                                    //launchOtpActivity();
                                } else if (response.getStatus() == 1006) {
                                    showStatus("Login request, status : " + response.getStatus() + " - " + response.getDescription());
                                    displayAlertDialog(getString(R.string.error_authentication_failed), response.getDescription(), getString(R.string.ok), null);
                                } else if(response.getStatus() == 1025) {
                                    showStatus("Login request, status : " + response.getStatus() + " - " + response.getDescription());
                                    displayAlertDialog(getString(R.string.error_authentication_failed), response.getDescription(), getString(R.string.ok), null);
                                } else {
                                    showStatus("Login request, status : " + response.getStatus() + " - " + response.getDescription());


                                    LoginActivity.this.sum=response.getSum();

                                    if (response.getServerPayload() != null) {
                                        PingID.getInstance().setServerPayload(response.getServerPayload(), userAnswer, response.getCurrentAuthenticatingDeviceData() != null ? response.getCurrentAuthenticatingDeviceData().getName() : null);
                                    }


                                    //bypassed device
                                    if (response.getStatus() == 1026) {
                                        launchHomeActivity();
                                    }

                                    if (response.isPingIdAuthenticated()) {
                                        PingIDSdkDemoApplication.getInstance().setHomeActivityLoginFromAttemptLogin(true);
                                        launchHomeActivity();
                                    }
                                }

                                //enable the Sign On button
                                changeScreenAvailability(true);

                            } catch (Exception e) {

                                changeScreenAvailability(true);
                                showStatus("Error processing auth response -  " + e.getMessage() + ", response=" + responseBodyString);
                                displayAlertDialog(getString(R.string.error), e.getMessage() ,getString(R.string.close), null);
                                e.printStackTrace();
                            }
                        }
                    });
                }

                @Override
                public void onFailure(final int statusCode, Header[] headers, byte[] responseBody, final Throwable error) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d(TAG, "attemptLogin onFailure, statusCode=" + statusCode);
                            error.printStackTrace();
                            changeScreenAvailability(true);
                            displayAlertDialog(getString(R.string.error), getString(R.string.error_connection_failed),getString(R.string.close), null);
                            showStatus(error.getMessage());
                        }
                    });

                }
            };
            responseHandler.setUsePoolThread(true);
            client.post(this, requestUrl, null, entity, "application/json", responseHandler);

        } catch (Exception e) {
            e.printStackTrace();
            showStatus("Error logging into the system -  " + e.getMessage());
            changeScreenAvailability(true);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PLAY_SERVICES_UPDATE_REQUEST) {
            addLogLine("onActivityResult from Play services triggered");
            //we need to initialize the mobile PingID Consumer client again to verify that Google Play services is updated,
            //or simply go on without online authentication...
        }
        if(requestCode == OTP_INPUT_REQUEST && resultCode == Activity.RESULT_OK) {
            if(data != null && data.getExtras() != null)
                try {
                    attemptLogin(null, null, payload, null, data.getExtras().getString("passcode"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
        }
    }

//    private void tryToAuthenticateWithOtp(String passcode) throws Exception {
//
//        String updatedPayload = PingID.getInstance().updateExistingPayloadWithUserSelection(PingID.PIDTrustLevel.PIDTrustLevelTrusted.getName(), "none");
//
//        final pingidsdk.pingidentity.com.simpledemo.beans.LoginRequest request = new pingidsdk.pingidentity.com.simpledemo.beans.LoginRequest();
//        request.setSessionId(authSession);
//        request.setOperation(OperationType.AUTH_OFFLINE_USER.getName());
//        request.setPingIdPayload(updatedPayload);
//        request.setOtp(passcode);
//        String requestUrl = AppPreferences.DEFAULT_CUSTOMER_SERVER_BASE_URL + "pidc";
//
//        AsyncHttpClient client = new AsyncHttpClient();
//        client.setTimeout(40000);
//
//        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
//        String json = gson.toJson(request);
//        Log.d(TAG, "requestUrl=" + requestUrl + " request=" + json);
//        StringEntity entity = new StringEntity(json, "UTF-8");
//        changeScreenAvailability(false);
//        client.post(this, requestUrl, null, entity, "application/json", new AsyncHttpResponseHandler() {
//
//            @Override
//            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
//
//                changeScreenAvailability(true);
//                String responseBodyString = new String(responseBody);
//                System.out.println(responseBodyString);
//                Gson gson = new Gson();
//                pingidsdk.pingidentity.com.simpledemo.beans.LoginResponse response = gson.fromJson(responseBodyString, pingidsdk.pingidentity.com.simpledemo.beans.LoginResponse.class);
//                if(response.getStatus() == 0){
//                    LoginActivity.this.sum = response.getSum();
//                    launchHomeActivity();
//                }
//            }
//
//            @Override
//            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
//                Log.d(TAG, "attemptLogin onFailure, statusCode=" + statusCode);
//                error.printStackTrace();
//                changeScreenAvailability(true);
//               // displayAlertDialog(getString(R.string.error), getString(R.string.error_connection_failed),getString(R.string.close), null);
//                showStatus(error.getMessage());
//            }
//        });
//    }

    @Override
    void changeScreenAvailability(boolean available) {
        Log.i(TAG, "changeScreenAvailability called with available=" + available);
        if (available) {
            if (progressBar != null) {
                progressBar.setVisibility(View.INVISIBLE);
            }
        } else {
            if (progressBar != null) {
                progressBar.setVisibility(View.VISIBLE);
            }
        }
        Button mSignInButton = (Button) findViewById(R.id.signin);
        if (mSignInButton != null) {
            mSignInButton.setEnabled(available);
        }
    }

    @Override
    void hideScanButton(boolean hide) {
        if(scanButton != null) {
            if(hide) {
                scanButton.setVisibility(View.INVISIBLE);
            } else {
                scanButton.setVisibility(View.VISIBLE);
            }
        }
    }

    /*
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     * NOTE : If Google Play services are not supported on the device -
     * online authentication will not be possible with PingID for Customers.
     */
    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                try{
                    apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST).show();
                } catch(WindowManager.BadTokenException badTokenException) {
                    //in case the problem is that the app is in the background and cannot display the dialog
                    Log.e(TAG,"The application is in the background and therefore cannot display the dialog");
                } catch(Throwable throwable) {
                    throwable.printStackTrace();
                }
            } else {
                Log.i(TAG, "Google Play services are not supported on this device.");
                finish();
            }
            return false;
        }
        return true;
    }

    //launch the home activity
    protected void launchHomeActivityPingFed() {
        if(!pingFedTriggerAuth) {
            return;
        }
        pingFedTriggerAuth = false;
        Intent intent = new Intent(this, HomeActivity.class);
        if (sum != null) {
            intent.putExtra(HomeActivity.KEY_SUM, sum);
        }
        startActivity(intent);
        finish();
    }

    //launch the home activity
    protected void launchHomeActivity() {
        Intent intent = new Intent(this, HomeActivity.class);
        if (sum != null) {
            intent.putExtra(HomeActivity.KEY_SUM, sum);
        }
        startActivity(intent);
        finish();
    }

    //launch the home activity
    protected void launchOtpActivity() {
        Intent intent = new Intent(this, OtpActivity.class);
        if(authSession != null) {
            intent.putExtra("sessionId", authSession);
        }
        startActivityForResult(intent, OTP_INPUT_REQUEST);
    }

    /**
     * edit parameters dynamically
     */
    protected void displayAppPrefs(final Context context) {
        try {
            final Dialog dialog = new Dialog(context);
            setTheme(R.style.AppCompactTheme);
            dialog.setContentView(R.layout.dialog_settings);
            dialog.setTitle(getString(R.string.app_prefs_title));

            final EditText customerServerBaseUrl = dialog.findViewById(R.id.customer_server_base_url);
            customerServerBaseUrl.setText(AppPreferences.getCustomerServerBaseUrl(context));
            final EditText apiId = dialog.findViewById(R.id.api_id);
            apiId.setText(AppPreferences.getApiId(context));
            final EditText oidcIssuer = dialog.findViewById(R.id.oidc_issuer);
            oidcIssuer.setText(AppPreferences.getOidcIssuer(context));
            final EditText pushSenderId = dialog.findViewById(R.id.push_sender_id);
            pushSenderId.setText(AppPreferences.getPushSenderId(context));
            final EditText deviceVerificationApiKey = dialog.findViewById(R.id.device_verification_api_key);
            deviceVerificationApiKey.setText(AppPreferences.getDeviceVerificationApiKey(context));
            final CheckBox rootDetectionCheckBox = dialog.findViewById(R.id.root_detection_check_box);
            rootDetectionCheckBox.setChecked(AppPreferences.isRootDetectionActive(context));

            Button buttonOk = dialog.findViewById(R.id.buttonOk);
            Button buttonReset = dialog.findViewById(R.id.buttonReset);

            // if button is clicked, close the custom dialog
            buttonOk.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean isChanged = false;
                    String customerServerBaseUrlString = customerServerBaseUrl.getText().toString();
                    String oidcIssuerString = oidcIssuer.getText().toString();
                    if (!customerServerBaseUrlString.equals(AppPreferences.getCustomerServerBaseUrl(context)) ||
                            !apiId.getText().toString().equals(AppPreferences.getApiId(context)) ||
                            !pushSenderId.getText().toString().equals(AppPreferences.getPushSenderId(context)) ||
                            !deviceVerificationApiKey.getText().toString().equals(AppPreferences.getDeviceVerificationApiKey(context)) ||
                            !oidcIssuerString.equals(AppPreferences.getOidcIssuer(context)) ||
                            rootDetectionCheckBox.isChecked() != AppPreferences.isRootDetectionActive(context)) {
                        isChanged = true;
                    }

                    AppPreferences.setCustomerServerBaseUrl(context, customerServerBaseUrlString);
                    AppPreferences.setApiId(context, apiId.getText().toString());
                    AppPreferences.setPushSenderId(context, pushSenderId.getText().toString());
                    AppPreferences.setDeviceVerificationApiKey(context, deviceVerificationApiKey.getText().toString());

                    // copy the url to oidc
                    if (oidcIssuerString.isEmpty() && customerServerBaseUrlString.length() > 6) {
                        int baseUrlSize = customerServerBaseUrlString.indexOf(":",6);
                        if (baseUrlSize != -1) {
                            oidcIssuerString = customerServerBaseUrlString.substring(0, baseUrlSize + 1) + OIDC_ISSUER_PORT;
                        }
                    }
                    AppPreferences.setOidcIssuer(context, oidcIssuerString);
                    AppPreferences.setRootDetectionActive(context, rootDetectionCheckBox.isChecked());

                    dialog.dismiss();

                    if (isChanged) {
                        Toast.makeText(context, "Please restart the Moderno app", Toast.LENGTH_LONG).show();
                    }
                }
            });

            buttonReset.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean isChanged = false;
                   if (!BuildConfig.CUSTOMER_SERVER_BASE_URL.equals(AppPreferences.getCustomerServerBaseUrl(context)) ||
                            !BuildConfig.APP_ID.equals(AppPreferences.getApiId(context)) ||
                            !BuildConfig.PUSH_SENDER_ID.equals(AppPreferences.getPushSenderId(context)) ||
                            !BuildConfig.DEVICE_VERIFICATION_API_KEY.equals(AppPreferences.getDeviceVerificationApiKey(context)) ||
                            !BuildConfig.OIDC_ISSUER.equals(AppPreferences.getOidcIssuer(context)) ||
                            BuildConfig.IS_ROOT_DETECTION_ACTIVE != AppPreferences.isRootDetectionActive(context)) {
                        isChanged = true;
                    }

                    AppPreferences.setCustomerServerBaseUrl(context, BuildConfig.CUSTOMER_SERVER_BASE_URL);
                    customerServerBaseUrl.setText(AppPreferences.getCustomerServerBaseUrl(context));
                    AppPreferences.setApiId(context, BuildConfig.APP_ID);
                    apiId.setText(AppPreferences.getApiId(context));
                    AppPreferences.setOidcIssuer(context, BuildConfig.OIDC_ISSUER);
                    oidcIssuer.setText(AppPreferences.getOidcIssuer(context));
                    AppPreferences.setPushSenderId(context, BuildConfig.PUSH_SENDER_ID);
                    pushSenderId.setText(AppPreferences.getPushSenderId(context));
                    AppPreferences.setDeviceVerificationApiKey(context, BuildConfig.DEVICE_VERIFICATION_API_KEY);
                    deviceVerificationApiKey.setText(AppPreferences.getDeviceVerificationApiKey(context));
                    AppPreferences.setRootDetectionActive(context, BuildConfig.IS_ROOT_DETECTION_ACTIVE);
                    rootDetectionCheckBox.setChecked(AppPreferences.isRootDetectionActive(context));

                    if (isChanged) {
                        Toast.makeText(context, "Please restart the Moderno app", Toast.LENGTH_LONG).show();
                    }
                }
            });

            try {
                dialog.show();
            } catch (WindowManager.BadTokenException badTokenException) {
                // in case the problem is that the app is in the background and cannot display the dialog
                Log.e(TAG, "The application is in the background and therefore cannot display the dialog");
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        } catch(Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mAuthService != null) {
            mAuthService.dispose();
        }
    }

}
