package com.apps.meet.userauthentication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.iid.FirebaseInstanceId;
import com.hbb20.CountryCodePicker;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class PhoneAuthActivity extends AppCompatActivity implements
        View.OnClickListener {

    private static final String TAG = "PhoneAuthActivity";

    private static final String KEY_VERIFY_IN_PROGRESS = "key_verify_in_progress";

    private static final int STATE_INITIALIZED = 1;
    private static final int STATE_CODE_SENT = 2;
    private static final int STATE_VERIFY_FAILED = 3;
    private static final int STATE_VERIFY_SUCCESS = 4;
    private static final int STATE_SIGNIN_FAILED = 5;
    private static final int STATE_SIGNIN_SUCCESS = 6;

    public static final String MyPREFERENCES = "MyPrefs" ;

    // [START declare_auth]
    private FirebaseAuth mAuth;
    // [END declare_auth]

    private boolean mVerificationInProgress = false;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    private ViewGroup mPhoneNumberViews;
    private ViewGroup mSignedInViews;


    private EditText mPhoneNumberField;
    private EditText mVerificationField;
    private String mPackagesNames;

    private Button mStartButton;
    private Button mVerifyButton;
    private Button mResendButton;
    private Button mSignOutButton;
    private CountryCodePicker ccp;

    SharedPreferences sharedpreferences;
    private MaterialDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_auth);

        FirebaseApp.initializeApp(this);

        mProgressDialog = new MaterialDialog.Builder(PhoneAuthActivity.this)
                .cancelable(false)
                .progress(true, 100)
                .content("Please wait...")
                .build();

      //  mProgressDialog.show();

        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);

            ccp = (CountryCodePicker) findViewById(R.id.ccp);
            ccp.setContentColor(Color.WHITE);

            if (sharedpreferences.getString("code", null) != null) {
                Log.d("code is ", sharedpreferences.getString("code", null) + sharedpreferences.getString("phone", null));

                //User already signed in
                Intent in = new Intent(PhoneAuthActivity.this, DisplayContactsActivity.class);
                in.putExtra("code", sharedpreferences.getString("code", null));
                in.putExtra("phone", sharedpreferences.getString("phone", null));

                startActivity(in);
                PhoneAuthActivity.this.finish();

            }


            if (savedInstanceState != null) {
                onRestoreInstanceState(savedInstanceState);
            }


            mPhoneNumberViews = (ViewGroup) findViewById(R.id.phone_auth_fields);
            mSignedInViews = (ViewGroup) findViewById(R.id.signed_in_buttons);


            mPhoneNumberField = (EditText) findViewById(R.id.field_phone_number);
            mVerificationField = (EditText) findViewById(R.id.field_verification_code);

            mStartButton = (Button) findViewById(R.id.button_start_verification);
            mVerifyButton = (Button) findViewById(R.id.button_verify_phone);
            mResendButton = (Button) findViewById(R.id.button_resend);
            mSignOutButton = (Button) findViewById(R.id.sign_out_button);

            // Assign click listeners
            mStartButton.setOnClickListener(this);
            mVerifyButton.setOnClickListener(this);
            mResendButton.setOnClickListener(this);
            mSignOutButton.setOnClickListener(this);

            // [START initialize_auth]
            mAuth = FirebaseAuth.getInstance();
            // [END initialize_auth]

            // Initialize phone auth callbacks
            // [START phone_auth_callbacks]
            mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                @Override
                public void onVerificationCompleted(PhoneAuthCredential credential) {
                    // This callback will be invoked in two situations:
                    // 1 - Instant verification. In some cases the phone number can be instantly
                    //     verified without needing to send or enter a verification code.
                    // 2 - Auto-retrieval. On some devices Google Play services can automatically
                    //     detect the incoming verification SMS and perform verificaiton without
                    //     user action.
                    Log.d(TAG, "onVerificationCompleted:" + credential);
                    // [START_EXCLUDE silent]
                    mVerificationInProgress = false;
                    // [END_EXCLUDE]

                    // [START_EXCLUDE silent]
                    // Update the UI and attempt sign in with the phone credential
                    updateUI(STATE_VERIFY_SUCCESS, credential);
                    // [END_EXCLUDE]
                    signInWithPhoneAuthCredential(credential);
                }

                @Override
                public void onVerificationFailed(FirebaseException e) {
                    // This callback is invoked in an invalid request for verification is made,
                    // for instance if the the phone number format is not valid.
                    Log.w(TAG, "onVerificationFailed", e);
                    // [START_EXCLUDE silent]
                    mVerificationInProgress = false;
                    // [END_EXCLUDE]

                    if (e instanceof FirebaseAuthInvalidCredentialsException) {
                        // Invalid request
                        // [START_EXCLUDE]
                        mPhoneNumberField.setError("Invalid phone number.");
                        // [END_EXCLUDE]
                    } else if (e instanceof FirebaseTooManyRequestsException) {
                        // The SMS quota for the project has been exceeded
                        // [START_EXCLUDE]
                        Snackbar.make(findViewById(android.R.id.content), "Quota exceeded.",
                                Snackbar.LENGTH_SHORT).show();
                        // [END_EXCLUDE]
                    }

                    // Show a message and update the UI
                    // [START_EXCLUDE]
                    updateUI(STATE_VERIFY_FAILED);
                    // [END_EXCLUDE]
                }

                @Override
                public void onCodeSent(String verificationId,
                                       PhoneAuthProvider.ForceResendingToken token) {
                    // The SMS verification code has been sent to the provided phone number, we
                    // now need to ask the user to enter the code and then construct a credential
                    // by combining the code with a verification ID.
                    Log.d(TAG, "onCodeSent:" + verificationId);

                    // Save verification ID and resending token so we can use them later
                    mVerificationId = verificationId;
                    mResendToken = token;

                    // [START_EXCLUDE]
                    // Update UI
                    updateUI(STATE_CODE_SENT);
                    // [END_EXCLUDE]
                }
            };

        // [END phone_auth_callbacks]
    }

    // [START on_start_check_user]
    @Override
    public void onStart() {
        super.onStart();

        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);

      //  updateSignInCreds();

        // [START_EXCLUDE]
        if (mVerificationInProgress && validatePhoneNumber()) {
            startPhoneNumberVerification(mPhoneNumberField.getText().toString());
        }
        // [END_EXCLUDE]
    }
    // [END on_start_check_user]

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_VERIFY_IN_PROGRESS, mVerificationInProgress);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mVerificationInProgress = savedInstanceState.getBoolean(KEY_VERIFY_IN_PROGRESS);
    }


    private void startPhoneNumberVerification(String phoneNumber) {
        // [START start_phone_auth]

        phoneNumber = "+" + ccp.getSelectedCountryCode() + phoneNumber;
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks);        // OnVerificationStateChangedCallbacks
        // [END start_phone_auth]

        mVerificationInProgress = true;
    }

    private void verifyPhoneNumberWithCode(String verificationId, String code) {
         // [START verify_with_code]
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        // [END verify_with_code]
        signInWithPhoneAuthCredential(credential);
    }

    // [START resend_verification]
    private void resendVerificationCode(String phoneNumber,
                                        PhoneAuthProvider.ForceResendingToken token) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks,         // OnVerificationStateChangedCallbacks
                token);             // ForceResendingToken from callbacks
    }
    // [END resend_verification]

    // [START sign_in_with_phone]
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");

                            FirebaseUser user = task.getResult().getUser();

                            updateSignInCreds(task);
                            // [START_EXCLUDE]



                            // [END_EXCLUDE]
                        } else {
                            // Sign in failed, display a message and update the UI
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                                // [START_EXCLUDE silent]
                                mVerificationField.setError("Invalid code.");
                                // [END_EXCLUDE]
                            }
                            else if(task.getException() instanceof FirebaseNetworkException) {


                            }
                            // [START_EXCLUDE silent]
                            // Update UI
                            updateUI(STATE_SIGNIN_FAILED);
                            // [END_EXCLUDE]
                        }
                    }
                });
    }
    // [END sign_in_with_phone]

    private void signOut() {
        mAuth.signOut();
        updateUI(STATE_INITIALIZED);
    }

    private void updateUI(int uiState) {
        updateUI(uiState, mAuth.getCurrentUser(), null);
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            updateUI(STATE_SIGNIN_SUCCESS, user);
        } else {
            updateUI(STATE_INITIALIZED);
        }
    }

    private void updateUI(int uiState, FirebaseUser user) {
        updateUI(uiState, user, null);
    }

    private void updateUI(int uiState, PhoneAuthCredential cred) {
        updateUI(uiState, null, cred);
    }

    private void updateUI(int uiState, FirebaseUser user, PhoneAuthCredential cred) {
        switch (uiState) {
            case STATE_INITIALIZED:
                // Initialized state, show only the phone number field and start button
                enableViews(mStartButton, mPhoneNumberField);
                disableViews(mVerifyButton, mResendButton, mVerificationField);
                break;
            case STATE_CODE_SENT:
                // Code sent state, show the verification field, the
                enableViews(mVerifyButton, mResendButton, mPhoneNumberField, mVerificationField);
                disableViews(mStartButton);

                if(mProgressDialog != null && mProgressDialog.isShowing())
                    mProgressDialog.cancel();

                break;
            case STATE_VERIFY_FAILED:
                // Verification has failed, show all options
                enableViews(mStartButton, mVerifyButton, mResendButton, mPhoneNumberField,
                        mVerificationField);

                if(mProgressDialog != null && mProgressDialog.isShowing())
                    mProgressDialog.cancel();

                break;
            case STATE_VERIFY_SUCCESS:


                // Verification has succeeded, proceed to firebase sign in
                disableViews(mStartButton, mVerifyButton, mResendButton, mPhoneNumberField,
                        mVerificationField);

                // Set the verification text based on the credential
                if (cred != null) {
                    if (cred.getSmsCode() != null) {
                        mVerificationField.setText(cred.getSmsCode());
                    } else {
                        mVerificationField.setText(R.string.instant_validation);
                    }
                }

                break;
            case STATE_SIGNIN_FAILED:
                // No-op, handled by sign-in check
                if(mProgressDialog != null && mProgressDialog.isShowing())
                    mProgressDialog.cancel();

                break;
            case STATE_SIGNIN_SUCCESS:
                // Np-op, handled by sign-in check

                if(sharedpreferences.getString("id", null) != null) {
                    mProgressDialog.cancel();
                    Intent in = new Intent(this, DisplayContactsActivity.class);


                    SharedPreferences.Editor editor = sharedpreferences.edit();
                    editor.putString("code", ccp.getSelectedCountryCode());
                    editor.putString("phone", mPhoneNumberField.getText().toString());

                    editor.commit();

                    startActivity(in);


                    this.finish();
                }

                break;
        }

        if (user == null) {
            // Signed out
            mPhoneNumberViews.setVisibility(View.VISIBLE);
            mSignedInViews.setVisibility(View.GONE);

        } else {
            // Signed in
        }
    }



    private void updateSignInCreds(final Task<AuthResult> task) {

        // Instantiate the RequestQueue.
        if(!mProgressDialog.isShowing())
            mProgressDialog.show();

        RequestQueue queue = Volley.newRequestQueue(this);
        String url = Utils.SIGN_UP_REQUEST_URL + "?phone=" + mPhoneNumberField.getText().toString() + "&phone_with_code=" + ccp.getSelectedCountryCode() + mPhoneNumberField.getText().toString() + "&device_token=" + FirebaseInstanceId.getInstance().getToken() + "&secret=" + Utils.SECRET_KEY;

        Log.d(TAG, url);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>(){
                    @Override
                    public void onResponse(String response) {

                        Log.d(TAG, response);
                        try {
                            JSONObject jsonObject = new JSONObject(response);

                            SharedPreferences.Editor editor = sharedpreferences.edit();
                            editor.putString("id", jsonObject.getString("userID"));

                            editor.commit();

                            if(jsonObject.getString("userID") != null)
                            updateUI(STATE_SIGNIN_SUCCESS, task.getResult().getUser());
                            else
                                Utils.showNetworkDialog(PhoneAuthActivity.this);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        // Display the first 500 characters of the response string.
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                mProgressDialog.cancel();
                Utils.showNetworkDialog(PhoneAuthActivity.this);
                Toast.makeText(PhoneAuthActivity.this, "Internal error occured! Please try again!", Toast.LENGTH_SHORT).show();
            }
        });
// Add the request to the RequestQueue.
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 2, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(stringRequest);
    }

    private boolean validatePhoneNumber() {
        String phoneNumber = mPhoneNumberField.getText().toString();
        if (TextUtils.isEmpty(phoneNumber)) {
            mPhoneNumberField.setError("Invalid phone number.");
            return false;
        }

        return true;
    }

    private void enableViews(View... views) {
        for (View v : views) {
            v.setEnabled(true);
        }
    }

    private void disableViews(View... views) {
        for (View v : views) {
            v.setEnabled(false);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_start_verification:
                if (!validatePhoneNumber()) {
                    return;
                }

                mProgressDialog.show();
                startPhoneNumberVerification(mPhoneNumberField.getText().toString());
                break;
            case R.id.button_verify_phone:
                String code = mVerificationField.getText().toString();
                if (TextUtils.isEmpty(code)) {
                    mVerificationField.setError("Cannot be empty.");
                    return;
                }
                mProgressDialog.show();
                verifyPhoneNumberWithCode(mVerificationId, code);
                break;
            case R.id.button_resend:
                resendVerificationCode(mPhoneNumberField.getText().toString(), mResendToken);
                break;
            case R.id.sign_out_button:
                signOut();
                break;
        }
    }
}
