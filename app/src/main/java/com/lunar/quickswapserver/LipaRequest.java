/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lunar.quickswapserver;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 *
 * @author Administrator
 */
public class LipaRequest {

    public   Context context=null;
    @SerializedName("MerchantRequestID")
    private String MerchantRequestID;
    @SerializedName("CheckoutRequestID")
    private String CheckoutRequestID;
    @SerializedName("ResponseCode")
    private String ResponseCode;
    @SerializedName("ResponseDescription")
    private String ResponseDescription;
    @SerializedName("CustomerMessage")
    private String CustomerMessage;

    public String getMerchantRequestID() {
        return MerchantRequestID;
    }

    public void setMerchantRequestID(String MerchantRequestID) {
        this.MerchantRequestID = MerchantRequestID;
    }

    public String getCheckoutRequestID() {
        return CheckoutRequestID;
    }

    public void setCheckoutRequestID(String CheckoutRequestID) {
        this.CheckoutRequestID = CheckoutRequestID;
    }

    public String getResponseCode() {
        return ResponseCode;
    }

    public void setResponseCode(String ResponseCode) {
        this.ResponseCode = ResponseCode;
    }

    public String getResponseDescription() {
        return ResponseDescription;
    }

    public void setResponseDescription(String ResponseDescription) {
        this.ResponseDescription = ResponseDescription;
    }

    public String getCustomerMessage() {
        return CustomerMessage;
    }

    public void setCustomerMessage(String CustomerMessage) {
        this.CustomerMessage = CustomerMessage;
    }


    public LipaNaMpesaRequest makeRequest(String token, String PhoneNumber, double Amount, String rechargingPhone, String requestType,String deviceId,String fcmToken,String serverAdress,String serverId,String orderNumber) {
        int connectionAttempts=0;
        try {
            if(PhoneNumber.startsWith("0"))
            {
                PhoneNumber=PhoneNumber.replaceFirst("0", "254");
            }
            if(PhoneNumber.startsWith("+254"))
            {

                PhoneNumber=PhoneNumber.substring(1);
            }
            // System.err.println("phone "+PhoneNumber);

            OkHttpClient client = new OkHttpClient();

            MediaType mediaType = MediaType.parse("application/json");
            RequestBody body = RequestBody.create(mediaType, "{"
                    + "\"BusinessShortCode\": \"7631176\","
                    + " \"Password\": \"NzYzMTE3NmU4ZjIyM2U3OTBlNWIxMWVhMzliMjZiNjk2N2ExOGQzYzA5OGJiMjI3YjZiNWJiZDE0OWIyNDA5MTJlZGJhODUyMDE5MDIxNjE2NTYyNw==\","
                    + " \"Timestamp\": \"20190216165627\","
                    + " \"TransactionType\": \"CustomerBuyGoodsOnline\","
                    + " \"Amount\": " + Amount + ","
                    + " \"PartyA\": \"254707353225\","
                    + "\"PartyB\": \"9587279\","
                    + " \"PhoneNumber\":" + PhoneNumber + ","

                    + " \"CallBackURL\": \"https://api.lunar.cyou/api/lipacallback.php\","
                    + "\"AccountReference\": \"Airtime\","
                    + "\"TransactionDesc\": \"Purchase\""
                    + "}");
            Request request = new Request.Builder()
                    .url("https://api.safaricom.co.ke/mpesa/stkpush/v1/processrequest")
                    .post(body)
                    .addHeader("authorization", "Bearer " + token)
                    .addHeader("content-type", "application/json")
                    .build();

            Response response = client.newCall(request).execute();
            System.out.println("Amount"+Amount);
        //     System.out.println(response.body().string());
            Gson gson = new Gson();
            LipaNaMpesaRequest lipaNaMpesaRequest = gson.fromJson(response.body().string(), LipaNaMpesaRequest.class);
            response.close();
            if( lipaNaMpesaRequest.getResponseCode()!=null && lipaNaMpesaRequest.getResponseCode().equalsIgnoreCase("0"))
            {


Map map=saveAirtimeRequest(0,rechargingPhone,Amount,lipaNaMpesaRequest.getCheckoutRequestID(),lipaNaMpesaRequest.getMerchantRequestID(),requestType,deviceId,fcmToken,serverAdress,serverId,orderNumber);



            }
            else {

            }
            return lipaNaMpesaRequest;

        } catch (IOException ex) {
            Logger.getLogger(LipaNaMpesaRequest.class.getName()).log(Level.SEVERE, null, ex);

            return null;
        }

    }


    public  Map saveAirtimeRequest(int errorCounter,String phone, double amount, String checkoutRequestID, String merchantRequestID, String requestType,String deviceId,String fcmToken,String serverAdress,String serverId,String orderNumber)
    {
        Map MAP=new HashMap();
        final ArrayList<Map> result=new ArrayList<Map>();
        Map map=new HashMap();


        try {

            Gson gson=new Gson();
            Map data=new HashMap();
            data.put("amount",amount);
            data.put("status","0");
            data.put("phoneNumber",phone);
            data.put("MerchantRequestID",merchantRequestID);
            data.put("CheckoutRequestID",checkoutRequestID);
            data.put("requestType",requestType);
            data.put("tillNumber","9587279");
            data.put("deviceId",deviceId);
            data.put("fcmToken",fcmToken);
            data.put("serverAddress",serverAdress);
            data.put("serverId",serverId);
            data.put("orderNumber",orderNumber);
System.err.println("dtat"+gson.toJson(data).getBytes("UTF-8"));
            String query_url = "https://api.lunar.cyou/api/airtimerequest.php";
            URL url = new URL(query_url);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setRequestProperty("Bypass-Tunnel-Reminder", "application/jsocharset=UTF-8");
            httpURLConnection.setRequestProperty("Content-Type", "application/json; utf-8");
            OutputStream os = httpURLConnection.getOutputStream();
            os.write(gson.toJson(data).getBytes("UTF-8"));
            os.close();

            InputStream in = new BufferedInputStream(httpURLConnection.getInputStream());
            String results = IOUtils.toString(in, "UTF-8");
          //  System.err.println(results);
            JSONObject myResponse = new JSONObject(results);

in.close();
//



            System.err.println(myResponse);
            if(myResponse.get("responseCode").equals("200"))
            {

                System.err.println("sucess");
                return null;
            }
            else {
                errorCounter++;
                if(errorCounter<3)
                {
                    System.err.println("Retrying.....");
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    return saveAirtimeRequest(errorCounter, phone,amount, checkoutRequestID,merchantRequestID, requestType,deviceId, fcmToken,serverAdress, serverId,orderNumber);

                }
                else{
                    return null;
                }
            }


        }
        catch (Exception e)
        {
            MAP=new HashMap();
            e.printStackTrace();

            MAP.put("responseDescription",e.toString());
            MAP.put("responseCode","301");
            return MAP;

        }




    }


}
