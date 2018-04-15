package com.apps.meet.userauthentication;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by dharamvir on 15/04/2018.
 */

public class Notification {

    //Push Notification

    public static void notifyUsers(ArrayList<String> tokens, ArrayList<String> keys, ArrayList<String> values, Context context) {

        String requestUrl = Utils.NOTIFICATION_REQUEST_URL;

        JSONObject jsonObject = new JSONObject();
        // JSONObject js = new JSONObject();

        try {

            jsonObject.put("keys", keys);
            jsonObject.put("values", values);
            jsonObject.put("device_tokens", tokens);
            jsonObject.put("body", Utils.NOTIFICATION_BODY);
            jsonObject.put("title", Utils.NOTIFICATION_TITLE);
            jsonObject.put("app_id", Utils.APP_ID);
        } catch (JSONException j) {
            j.printStackTrace();
        }

        //  String response = DisplayContactsActivity.postObject(requestUrl, jsonObject);
        postJSON(requestUrl, jsonObject, context);

    }

    public static void postJSON(String requestUrl, final JSONObject js, Context context) {

        final RequestQueue requestQueue = Volley.newRequestQueue(context);

        StringRequest jsonObjReq = new StringRequest(
                Request.Method.POST, requestUrl,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        Log.d("NotificationSent", response);

                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Notification", "Error in onErrorResponse: " + error.getMessage());

            }
        }) {

            /**
             * Passing some request headers
             */


            @Override
            public byte[] getBody () throws AuthFailureError {
                Log.d("Notification", js.toString());
                return js.toString().getBytes();
            }

            @Override
            public String getBodyContentType() {
                return "application/json";
            }

        };

        requestQueue.add(jsonObjReq);
    }


}
