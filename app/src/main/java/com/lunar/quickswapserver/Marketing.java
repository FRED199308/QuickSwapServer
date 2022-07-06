package com.lunar.quickswapserver;

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import dmax.dialog.SpotsDialog;

public class Marketing extends AppCompatActivity implements View.OnClickListener {
CheckBox paying,receiving;
Spinner sendMode,senderId;
ProgressBar bar,analysisProgressBar;
TextView messageCounter;
Button sendbtn,analysisbtn;

    HashSet phoneNumbersSet;
    DBHelper db;
    String recipientsPhoneNumber="";

    RequestQueue mRequestQue;
EditText messageText,title;
    private SpotsDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_marketing);

senderId=findViewById(R.id.senderId);
sendMode=findViewById(R.id.sendMode);
paying=findViewById(R.id.paying);
messageText =findViewById(R.id.message);
title=findViewById(R.id.title);
receiving=findViewById(R.id.recharging);
sendbtn=findViewById(R.id.sendBtn);
messageCounter=findViewById(R.id.messageCounter);
bar=findViewById(R.id.progress);
        progressDialog = new SpotsDialog(this, R.style.Custom);
        mRequestQue = Volley.newRequestQueue(this);
        String send []={"SMS","Notification"};
        ArrayAdapter adp1 = new ArrayAdapter<String>(getApplicationContext(),
                android.R.layout.simple_spinner_item,send);
        adp1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sendMode.setAdapter(adp1);
String seder[]={"Wyzer"};
        adp1 = new ArrayAdapter<String>(getApplicationContext(),
                android.R.layout.simple_spinner_item,seder);
        adp1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        senderId.setAdapter(adp1);
       sendbtn.setOnClickListener(this);
        sendMode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(sendMode.getSelectedItemPosition()==0)
                {
                    senderId.setVisibility(View.VISIBLE);
                    paying.setVisibility(View.VISIBLE);
                    receiving.setVisibility(View.VISIBLE);
                    title.setVisibility(View.INVISIBLE);




                }
                else{
                    senderId.setVisibility(View.INVISIBLE);
                    paying.setVisibility(View.INVISIBLE);
                    title.setVisibility(View.VISIBLE);
                    receiving.setVisibility(View.INVISIBLE);

                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        messageText.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // TODO Auto-generated method stub
            }

            @Override
            public void afterTextChanged(Editable s) {
                String pp=   messageCounter(messageCounter.getText().toString());
                messageCounter.setText("Message Count:"+pp);

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // TODO Auto-generated method stub
            }


        });

    }
    public String messageCounter(String message)
    {
        String messagePart="";
        int counter=message.length();
        if(counter<161)
        {
            messagePart="1";
            return (counter+"("+messagePart+")");
        }
        else if(counter>160&&counter<321){
            messagePart="2";
            return(counter+"("+messagePart+")");
        }
        else if(counter>321&&counter<481){
            messagePart="3";
            return(counter+"("+messagePart+")");
        }
        else if(counter>321&&counter<481){
            messagePart="4";
            return(counter+"("+messagePart+")");
        }
        else if(counter>481&&counter<641){
            messagePart="5";
            return(counter+"("+messagePart+")");
        }
        else if(counter>641&&counter<801){
            messagePart="6";
            return(counter+"("+messagePart+")");
        }
        else if(counter==0)
        {
            return "Character Counter";
        }
        else{
            return(String.valueOf(counter));
        }

    }
    @Override
    public void onClick(View view) {


      String recipients,se;

        if(sendMode.getSelectedItemPosition()>0)
        {
if(messageText.getText().toString().isEmpty())
{
    Toast.makeText(this, "Invalid Message", Toast.LENGTH_SHORT).show();
}
else{
    if(title.getText().toString().isEmpty())
    {
        Toast.makeText(this, "Notification Title", Toast.LENGTH_SHORT).show();
    }
    else{


        AlertDialog.Builder    alert= new AlertDialog.Builder(this);
        alert.setTitle("Confirm Action");
        alert.setMessage("You are About To Send Notification To All Installed Devices");
        alert.setPositiveButton("Proceed", new DialogInterface.OnClickListener() {

            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(DialogInterface dialog, int which) {

                sendNotificationMessage(messageText.getText().toString(),"asapbiz",getResources().getString(R.string.clientFcmToken),title.getText().toString(),getResources().getString(R.string.tillNumber));

                dialog.dismiss();


            }
        });

        alert.setNegativeButton("No", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(Marketing.this,"Send Postponed", Toast.LENGTH_LONG).show();
                dialog.dismiss();
            }
        });
        alert.show();



    }
}

        }
        else {
            if(!paying.isChecked() && !receiving.isChecked())
            {
                Toast.makeText(this, "Please Select The Recipients", Toast.LENGTH_LONG).show();
            }
            else{

                db = db.getInstance(this);
                if(paying.isChecked()&& receiving.isChecked())
                {
                    progressDialog.show();
                    phoneNumbersSet= db.getAllPhoneNumbers();
                    Iterator it=phoneNumbersSet.iterator();
                    while (it.hasNext())
                    {
                        recipientsPhoneNumber=   it.next().toString().replaceFirst("0","254")+","+recipientsPhoneNumber;
                    }

                  progressDialog.dismiss();
                    AlertDialog.Builder    alert= new AlertDialog.Builder(this);
                    alert.setTitle("Confirm Action");
                    alert.setMessage("You are About To Send Bulk SMS To All Phone Numbers  No of Recipients: "+phoneNumbersSet.size());
                    alert.setPositiveButton("Proceed", new DialogInterface.OnClickListener() {

                        @RequiresApi(api = Build.VERSION_CODES.O)
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            progressDialog.show();

                            sendBulkMessages(messageText.getText().toString(),recipientsPhoneNumber);
                            dialog.dismiss();


                        }
                    });

                    alert.setNegativeButton("No", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(Marketing.this,"Send Postponed", Toast.LENGTH_LONG).show();
                            dialog.dismiss();
                        }
                    });
                    alert.show();




                }
                else{
                    if(paying.isChecked())
                    {
                        progressDialog.show();
                   phoneNumbersSet= db.getAllPayingNumbers();
                        System.err.println(phoneNumbersSet);

                        Iterator it=phoneNumbersSet.iterator();
                        while (it.hasNext())
                        {
                            recipientsPhoneNumber=   it.next().toString().replaceFirst("0","254")+","+recipientsPhoneNumber;
                        }



                        progressDialog.dismiss();
                        AlertDialog.Builder    alert= new AlertDialog.Builder(this);
                        alert.setTitle("Confirm Action");
                        alert.setMessage("You are About To Send Bulk SMS To All Number Ever Made Payments No of Recipients: "+phoneNumbersSet.size());
                        alert.setPositiveButton("Proceed", new DialogInterface.OnClickListener() {

                            @RequiresApi(api = Build.VERSION_CODES.O)
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                progressDialog.show();

                                sendBulkMessages(messageText.getText().toString(),recipientsPhoneNumber);
                                dialog.dismiss();


                            }
                        });

                        alert.setNegativeButton("No", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(Marketing.this,"Send Postponed", Toast.LENGTH_LONG).show();
                                dialog.dismiss();
                            }
                        });
                        alert.show();





                    }
                    if(receiving.isChecked())
                    {   progressDialog.show();
                       phoneNumbersSet= db.getAllRechargeNumbers();

                        System.err.println(phoneNumbersSet);
                        Iterator it=phoneNumbersSet.iterator();
                        while (it.hasNext())
                        {
                            recipientsPhoneNumber=   it.next().toString().replaceFirst("0","254")+","+recipientsPhoneNumber;
                        }





                        progressDialog.dismiss();
                        AlertDialog.Builder    alert= new AlertDialog.Builder(this);
                        alert.setTitle("Confirm Action");
                        alert.setMessage("You are About To Send Bulk SMS To All Number Ever Received A Package No of Recipients: "+phoneNumbersSet.size());
                        alert.setPositiveButton("Proceed", new DialogInterface.OnClickListener() {

                            @RequiresApi(api = Build.VERSION_CODES.O)
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                progressDialog.show();

                                sendBulkMessages(messageText.getText().toString(),recipientsPhoneNumber);
                                dialog.dismiss();


                            }
                        });

                        alert.setNegativeButton("No", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(Marketing.this,"Send Postponed", Toast.LENGTH_LONG).show();
                                dialog.dismiss();
                            }
                        });
                        alert.show();






                    }
                }






            }

        }






    }

    public void sendNotificationMessage(String message, String userId, String fcmServerKey, String title, String tillNumber)
    {

progressDialog.show();
        try {

            JSONObject data = new JSONObject();

            data.put("fcmServerKey",fcmServerKey);
            data.put("status","0");
            data.put("userId",userId);
            data.put("message",message);
            data.put("title",title);

            data.put("tillNumber",tillNumber);

            int e=0;
            String url = "https://api.lunar.cyou/api/sendFcmNotification.php";

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url,
                    data,
                    new com.android.volley.Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                           progressDialog.dismiss();
                            try {
                                if(response.get("responseCode").equals("200"))
                                {

                                    Toast.makeText(Marketing.this, "Notifications Sent", Toast.LENGTH_SHORT).show();
                           messageText.setText("");

                                }
                                else {
                                    Toast.makeText(Marketing.this, "Failed", Toast.LENGTH_LONG).show();

                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }


                            Log.d("MUR", "onResponse: "+response);
                        }
                    }, new com.android.volley.Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    progressDialog.dismiss();
                    Toast.makeText(Marketing.this, "An Error Occured:"+error.networkResponse, Toast.LENGTH_SHORT).show();
                    Log.d("MUR", "onError............: "+error.networkResponse);
                }
            }
            ){
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> header = new HashMap<>();
                    header.put("content-type","application/json");

                    return header;
                }
            };
            request.setRetryPolicy(new DefaultRetryPolicy(20 * 1000, 5, 1.0f));
            mRequestQue.add(request);

        }
        catch (Exception sq){

        }

    }


    public void sendBulkMessages(String message, String recipients)
    {

        progressDialog.show();
        try {
            recipients=recipientsPhoneNumber.substring(0,recipientsPhoneNumber.length()-1);

            JSONObject data = new JSONObject();



            String json = "{\"senderid\":\""+senderId.getSelectedItem()+"\",\"secretkey\":\""+"asapbiz@2021!"+"\",\"userid\":\""+"asapbiz"+"\",\"message\":\""+message+"\",\"recipient\":\""+recipients+"\"";

            String m="";

            m=m+"}";
            json=json+m;
System.err.println(json+" hellllllllll..");
            int e=0;
            data=new JSONObject(json);
            String url = "https://api.lunar.cyou/api/sendsms.php";

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url,
                    data,
                    new com.android.volley.Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            progressDialog.dismiss();
                            System.err.println("Sent "+response);
                            try {

    Toast.makeText(Marketing.this, "SMS Sent", Toast.LENGTH_LONG).show();

messageText.setText("");

                            } catch (Exception e) {
                                e.printStackTrace();
                            }


                            Log.d("MUR", "onResponse: "+response);
                        }
                    }, new com.android.volley.Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    progressDialog.dismiss();
                    Toast.makeText(Marketing.this, "An Error Occured:"+error.networkResponse, Toast.LENGTH_SHORT).show();
                    Log.d("MUR", "onError............: "+error.networkResponse);
                }
            }
            ){
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> header = new HashMap<>();
                    header.put("content-type","application/json");

                    return header;
                }
            };
            request.setRetryPolicy(new DefaultRetryPolicy(20 * 1000, 5, 1.0f));
            mRequestQue.add(request);

        }
        catch (Exception sq){

        }

    }
    private class SendStatusFetcher extends AsyncTask<String, Integer, String> {

        @Override
        protected String doInBackground(String...voids) {

            String query_url = "https://api.lunar.cyou/api/sendsms.php";
            String json = "{ \"data\":{\"senderid\":\""+voids[0]+"\",\"secretkey\":\""+"airtimeexchange@2021!"+"\",\"userid\":\""+"airtimeexchange"+"\",\"message\":\""+voids[1]+"\",\"recipient\":\""+voids[2]+"\",\"fcmToken\":\""+voids[3]+"\"";

            String m="";

            m=m+"}}";
            json=json+m;
            // System.err.println(json);
            try {
                URL url = new URL(query_url);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(5000);
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setDoOutput(true);
                conn.setDoInput(true);
                conn.setRequestMethod("POST");
                OutputStream os = conn.getOutputStream();
                os.write(json.getBytes("UTF-8"));
                os.close();
                // read the response
                InputStream in = new BufferedInputStream(conn.getInputStream());
                String result = IOUtils.toString(in, "UTF-8");

                JSONObject myResponse = new JSONObject(result);



                in.close();
                conn.disconnect();
                if(myResponse.getString("ResponseCode").equalsIgnoreCase("0")){
                    return " Ksh "+myResponse.getString("actual_Balance")+",Or Sms: "+myResponse.getString("sms_Balance")+" Item(s)";

                }
                else{
                    return "Error";
                }
            } catch (Exception e) {
                System.out.println(e);
                return "Error Occured"+e.getMessage();
            }
        }

        @Override
        protected void onPreExecute() {
            analysisProgressBar.setVisibility(View.VISIBLE);
            analysisbtn.setEnabled(false);
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            if (analysisProgressBar != null) {
                analysisProgressBar.setProgress(values[0]);
            }
        }
        @Override
        protected void onPostExecute(String result) {
            analysisProgressBar.setVisibility(View.INVISIBLE);
            analysisbtn.setEnabled(true);




        }

    }

    private class AnalysisRequester extends AsyncTask<String, Void, String> {
        String analysisId="";
        @Override
        protected String doInBackground(String...voids) {
            Map map;


            String responseCode="";
            if(responseCode.equalsIgnoreCase("200"))
            {

                return      null;

            }
            else{
                return  null;
            }



        }


        @Override
        protected void onPostExecute(String result) {
            bar.setVisibility(View.INVISIBLE);

            if(result.equalsIgnoreCase("Analysis Started"))
            {
                SendStatusFetcher analysisStatusFetcher=new SendStatusFetcher();
                analysisStatusFetcher.execute(analysisId);

                Globals.toaster("Analysis Started Please Wait...");
            }
            else{

                Globals.toaster(result);
            }


        }

    }
}