package com.lunar.quickswapserver;


import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import org.apache.xerces.impl.dv.util.Base64;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Administrator
 */
public class AuthToken {
    @SerializedName("access_token")
    private String token;
    @SerializedName("expires_in")
    private String expiryTime;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }


    public String getExpiryTime() {
        return expiryTime;
    }

    public void setExpiryTime(String expiryTime) {
        this.expiryTime = expiryTime;
    }

    public  AuthToken getAccessToken() throws UnsupportedEncodingException {











        try {
            String app_key = "yBIf50DIGeUu2dRzZHWVOurNj0nAtA19";
            String app_secret = "oadlZaBFbXCaEkzZ";
            String appKeySecret = app_key + ":" + app_secret;
            byte[] bytes = appKeySecret.getBytes("ISO-8859-1");
            String auth = Base64.encode(bytes);
            Gson gson = new Gson();
//            URL url = new URL("https://api.safaricom.co.ke/oauth/v1/generate?grant_type=client_credentials");
//            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
//            httpURLConnection.setRequestMethod("GET");
//
//            httpURLConnection.setRequestProperty("authorization", "Basic " + auth);
//            httpURLConnection.setRequestProperty("cache-control", "no-cache");
//            InputStream inputStream = httpURLConnection.getInputStream();
//            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
//            System.out.println("vvvv:"+gson.fromJson(bufferedReader, AuthToken.class));



            OkHttpClient client = new OkHttpClient();


            Request request = new Request.Builder()
                    .url("https://api.safaricom.co.ke/oauth/v1/generate?grant_type=client_credentials")
                    .get()
                    .addHeader("authorization", "Basic " + auth)
                    .addHeader("cache-control", "no-cache")
                    .build();
            Response response = client.newCall(request).execute();

            AuthToken authToken = gson.fromJson(response.body().string(), AuthToken.class);
            response.close();
            return authToken;
        } catch (Exception ex) {
            ex.printStackTrace();
//            if(ex.toString().contains("Unknow"))
//            {
//                MainActivity.errorCode="Error : No Internet Connection";
//            }
//            else{
//                MainActivity.errorCode="Error :"+ex.toString();
//            }

            return null;

        }

    }
}


