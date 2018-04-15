package com.apps.meet.userauthentication;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;
import com.afollestad.materialdialogs.MaterialDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


public class DisplayContactsActivity extends AppCompatActivity {

    private static final String TAG = "DisplayContactsActivity";
    public static final String IMAGE_ACCESS_URL = "http://contactsyncer.com/uploads/contact_apps/";
    public static final int REQUEST_PERMISSION_CODE = 1;
    public static DisplayContactsActivity sActivityContext;

    private Cursor mCursor;
    private RecyclerView mRecList;
    private List<String> mPhoneList;
    private List<String> mNameList;
    private ArrayList<String> mDeviceTokens = new ArrayList<>();
    private MaterialDialog mProgressDialog;
    private ArrayList<ContactInfo> result;
    private Boolean mFromAddContact = false;
    String mPhone;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent in = getIntent();
        String code = in.getStringExtra("code");
        mPhone = in.getStringExtra("phone");

        mProgressDialog = new MaterialDialog.Builder(DisplayContactsActivity.this)
                .cancelable(false)
                .progress(true, 100)
                .content("Please wait...")
                .build();

        mProgressDialog.show();

        Log.d("DisplayContactsActivity", "phone is " + mPhone + " and code is " + code);

        ((ImageView) findViewById(R.id.add_contact_below)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mFromAddContact = true;

                Intent intent = new Intent(Intent.ACTION_INSERT,
                        ContactsContract.Contacts.CONTENT_URI);
                startActivity(intent);

            }
        });


        mRecList = (RecyclerView) findViewById(R.id.cardList);
        mRecList.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        mRecList.setLayoutManager(llm);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            EnableRuntimePermission();
        } else {
            createList();
        }
    }

    private List<ContactInfo> createList() {

        List<ContactInfo> result = new ArrayList<ContactInfo>();
        mPhoneList = new ArrayList<>();
        mNameList = new ArrayList<>();
        mCursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);

        while (mCursor.moveToNext()) {

            String name = mCursor.getString(mCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String phonenumber = mCursor.getString(mCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

            if (!phonenumber.contains("+")) {
                phonenumber = phonenumber.replaceAll("[\\W_]", "").replaceFirst("^0*", "");
            }
            else {
                phonenumber = "+" + phonenumber.replaceAll("[\\W_]", "").replaceFirst("^0*", "");
            }

            if (!mPhoneList.contains(phonenumber)) {
                mPhoneList.add(phonenumber);

                mNameList.add(name);
            }
        }

        mCursor.close();
        new VerifyPhoneNumbers().execute();
        return result;
    }

    public void EnableRuntimePermission() {

        if (ActivityCompat.shouldShowRequestPermissionRationale(
                DisplayContactsActivity.this,
                Manifest.permission.READ_CONTACTS)) {

            Toast.makeText(DisplayContactsActivity.this, "CONTACTS permission allows us to Access CONTACTS app", Toast.LENGTH_LONG).show();

        } else {

            ActivityCompat.requestPermissions(DisplayContactsActivity.this, new String[]{
                    Manifest.permission.READ_CONTACTS}, REQUEST_PERMISSION_CODE);

        }
    }

    @Override
    public void onRequestPermissionsResult(int RC, String per[], int[] PResult) {

        switch (RC) {

            case REQUEST_PERMISSION_CODE:

                if (PResult.length > 0 && PResult[0] == PackageManager.PERMISSION_GRANTED) {

                    createList();

                    //  Toast.makeText(DisplayContactsActivity.this,"Permission Granted, Now your application can access CONTACTS.", Toast.LENGTH_LONG).show();

                } else {

                    Toast.makeText(DisplayContactsActivity.this, "Permission Canceled, Now your application cannot access CONTACTS.", Toast.LENGTH_LONG).show();

                }
                break;
        }
    }

    public static String postObject(String completeUrl, JSONObject jsonObject, final Activity context) {
        DataOutputStream dataOutputStream;
        InputStream is;
        String jsonstring1 = "";

        try {
            String jsonstring = jsonObject.toString();
            URL url = new URL(completeUrl);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setConnectTimeout(15000);
            httpURLConnection.setReadTimeout(15000);

            dataOutputStream = new DataOutputStream(httpURLConnection.getOutputStream());
            dataOutputStream.write(jsonstring.getBytes());
            //Log.d("url calling in post",""+dataOutputStream);


            dataOutputStream.flush();
            dataOutputStream.close();

            int httpResult = httpURLConnection.getResponseCode();
            if (httpResult == HttpURLConnection.HTTP_OK) {
                is = new BufferedInputStream(httpURLConnection.getInputStream());
                Scanner s = new Scanner(is).useDelimiter("\\A");
                if (s.hasNext()) {
                    jsonstring1 = s.next();
                }
            }

        } catch (MalformedURLException e) {
            Log.d("error", "malformedUrl in Post");
        } catch (final IOException e) {
            context.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(e instanceof SocketTimeoutException || e instanceof UnknownHostException) {
                        Utils.showNetworkDialog(context);
                    }
                }
            });

            Log.d("error", "IOException in Post");
            e.printStackTrace();
            return null;

        } catch (Exception e) {
            Log.d("error", "Exception in Post");
        }


        return jsonstring1;
    }

    public void onContactClick(int pos) {

        Log.d("name clicked is ", result.get(pos).name + "  token is " + mDeviceTokens.get(pos));

        ArrayList<String> deviceTokens = new ArrayList<>();
        ArrayList<String> keys = new ArrayList<>();
        ArrayList<String> values = new ArrayList<>();

        deviceTokens.add(mDeviceTokens.get(pos));
        keys.add("key1");
        keys.add("key2");
        keys.add("key3");
        keys.add("key4");
        values.add("value1");
        values.add("value2");
        values.add("value3");
        values.add("value4");

        Notification.notifyUsers(deviceTokens, keys, values, this);

    }


    protected class VerifyPhoneNumbers extends AsyncTask<String, Void, ArrayList<ContactInfo>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected ArrayList<ContactInfo> doInBackground(String... params) {

            String requestUrl = Utils.VERIFY_PHONE_NUMBERS_REQUEST_URL;


            JSONObject jsonObject = new JSONObject();

            try {

                jsonObject.put("phonenumbers", mPhoneList);
                jsonObject.put("secret", Utils.SECRET_KEY);


            } catch (JSONException j) {
                j.printStackTrace();
            }

            String response = postObject(requestUrl, jsonObject, DisplayContactsActivity.this);

            if (response == null) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(DisplayContactsActivity.this, "Internal error occured! Please try again!", Toast.LENGTH_SHORT).show();
                    }
                });

                return null;
            }

            Log.d(TAG, response);

            JSONObject obj = null;
            try {
                obj = new JSONObject(response);
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }

// Retrieve number array from JSON object.
            JSONArray array = obj.optJSONArray("isPhoneExists");
            JSONArray tokens = obj.optJSONArray("tokens");
            JSONArray imageUrls = obj.optJSONArray("imageUrl");

            result = new ArrayList<>();

            mDeviceTokens.clear();
            result.clear();

            ArrayList<String> imgurls = new ArrayList<>();

            for (int i = 0; i < array.length(); i++) {


                //check if phone number exists on server
                if (array.optString(i).equals("y") && !mDeviceTokens.contains(tokens.optString(i))) {
                    ContactInfo ci = new ContactInfo();
                    ci.phoneNumber = mPhoneList.get(i);
                    ci.name = mNameList.get(i);
                    mDeviceTokens.add(tokens.optString(i));
                        ci.imageURL = imageUrls.optString(i);
                        result.add(ci);

                }

            }

            return result;

        }

        @Override
        protected void onPostExecute(ArrayList<ContactInfo> s) {
            super.onPostExecute(s);

            if (s == null) {
                Utils.showNetworkDialog(DisplayContactsActivity.this);
                mProgressDialog.cancel();
                return;
            }

            if(result.size() > 0) {
                ((RelativeLayout)findViewById(R.id.no_contacts_layout)).setVisibility(View.GONE);
                ContactAdapter ca = new ContactAdapter(s, DisplayContactsActivity.this);
                mRecList.setAdapter(ca);


            } else {
                ((RelativeLayout)findViewById(R.id.no_contacts_layout)).setVisibility(View.VISIBLE);
            }

            mProgressDialog.cancel();


        }
    }


    @Override
    protected void onResume() {
        super.onResume();

        //new contact added, refresh the list
        if (mFromAddContact) {
            createList();
        }

        mFromAddContact = false;
        //getDelegate().onStart();
    }


}
