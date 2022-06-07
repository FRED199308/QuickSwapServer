package com.lunar.quickswapserver;

import android.Manifest;
import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int REQUEST_READ_PHONE_STATE = 20;
    Button balbtn,logs,savebtn,setup,plans;
    TextView bal,smsbalance;

    private String number;
    private TelephonyManager telephonyManager;
    RadioButton sim1, sim2;
    DBHelper db;
    SQLiteDatabase sq;
    private RequestQueue mRequestQue;
    private String URL = "https://fcm.googleapis.com/fcm/send";
   AlertDialog.Builder alert;
    private static final int MY_PERMISSIONS_REQUEST_SMS_RECEIVE =100 ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.RECEIVE_SMS, Manifest.permission.CALL_PHONE, Manifest.permission.READ_SMS, Manifest.permission.SEND_SMS, Manifest.permission.READ_PHONE_STATE, Manifest.permission.SEND_SMS, Manifest.permission.ACCESS_NOTIFICATION_POLICY},
                MY_PERMISSIONS_REQUEST_SMS_RECEIVE);
      //  startService(new Intent(this, Recharger.class));
//        if (!isAccessibilityOn (this, WhatsappAccessibilityService.class)) {
//            Intent intent = new Intent (Settings.ACTION_ACCESSIBILITY_SETTINGS);
//            this.startActivity (intent);
//        }
        db=new DBHelper(this);
        sq=db.getWritableDatabase();
        balbtn = findViewById(R.id.balbtn);
        Globals.context = this;
        sim1 = findViewById(R.id.sim1);
        logs=findViewById(R.id.logs);
        savebtn=findViewById(R.id.savebtn);
        plans=findViewById(R.id.plans);
        setup=findViewById(R.id.setup);

        sim2 = findViewById(R.id.sim2);
        bal = findViewById(R.id.bal);
        smsbalance = findViewById(R.id.smsBalance);
        balbtn.setOnClickListener(this);
        logs.setOnClickListener(this);
        setup.setOnClickListener(this);
        plans.setOnClickListener(this);

        savebtn.setOnClickListener(this);


        mRequestQue = Volley.newRequestQueue(this);
        FirebaseMessaging.getInstance().subscribeToTopic("news");

InitialConfigs configs=new InitialConfigs(this);
//tillcommision.setText(configs.tillCommision);

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            //  Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                            return;
                        }

                        // Get new FCM registration token
                        String token = task.getResult();

                        System.err.println("Token token"+token);
                    }
                });
        FirebaseMessaging.getInstance().subscribeToTopic("all");

System.out.println(db.getAllLOgs());

    }



    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.savebtn:
                InitialConfigs configs=new InitialConfigs(this);


                Toast.makeText(this,"New Commissions Set Successfully", Toast.LENGTH_LONG).show();


                sendNotification();
                break;



            case R.id.setup:


                alert= new AlertDialog.Builder(this);
                alert.setTitle("Confirm Action");
                alert.setMessage("Are you sure you want Prepare The Enviroment\n This  will Delete All Plans History and You Will Have To Create The Afresh");
                alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        createEnviroment();
                        dialog.dismiss();


                    }
                });

                alert.setNegativeButton("No", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                alert.show();

                break;

            case R.id.balbtn:



                BalanceGetter getter=new BalanceGetter();
                getter.execute();









//
//                if(sim1.isChecked()) {
////                    if (android.os.Build.VERSION.SDK_INT < 26 ) {
//                        Map simInfo = simsSubscriptionId();
//                     String ussd = "*144#";
//                        System.out.println(simInfo.toString());
//                        AlertDialog.Builder alert = new AlertDialog.Builder(this);
//                        alert.setTitle("Simcards Infor");
//                        alert.setMessage(simInfo.toString());
//                        alert.create();
//                        alert.show();
////                        String code = "*" + 144 + Uri.encode("#");
////                        Intent tent = new Intent("android.intent.action.CALL", Uri.parse("tel:" + code));
////                        tent.putExtra("com.android.phone.extra.slot", 0);
////                        startActivity(tent);
//
////                    }
////                    else {
////                        // do something for phones running an SDK before lollipop
////
////
////                    Map simInfo = simsSubscriptionId();
////                    String ussd = "*144#";
////                    System.out.println(simInfo);
////                    int subscriptionId = 0;
////                    if (simInfo.containsKey("sim1")) {
////                        if (simInfo.get("sim1").toString().equalsIgnoreCase("Safaricom") || simInfo.get("sim1").toString().contains("Safaricom")) {
////                            ussd = "*144#";
////                            subscriptionId = Integer.parseInt(simInfo.get("sim1SubscriptionId").toString());
////                            dialUssd(ussd, subscriptionId, this);
////                            System.out.println("I used this subscriptionid: " + subscriptionId);
////                        }
////                        if (simInfo.get("sim1").toString().equalsIgnoreCase("Airtel KE") || simInfo.get("sim1").toString().contains("Airtel")) {
////                            ussd = "*133#";
////                            subscriptionId = Integer.parseInt(simInfo.get("sim1SubscriptionId").toString());
////                            dialUssd(ussd, subscriptionId, this);
////
////                            System.out.println("I used this subscriptionid: " + subscriptionId);
////                        }
////                        if (simInfo.get("sim1").toString().equalsIgnoreCase("Telkom") || simInfo.get("sim1").toString().contains("telkom")) {
////                            ussd = "*144#";
////                            subscriptionId = Integer.parseInt(simInfo.get("sim1SubscriptionId").toString());
////                            dialUssd(ussd, subscriptionId, this);
////                            System.out.println("I used this subscriptionid: " + subscriptionId);
////                        }
////                    }
////
////
////                }
//
//
//                }
//                else {
//
//                    if (android.os.Build.VERSION.SDK_INT < 26 ) {
//                        String code = "*" + 133 + Uri.encode("#");
//                        Intent tent = new Intent("android.intent.action.CALL", Uri.parse("tel:" + code));
//                        tent.putExtra("com.android.phone.extra.slot", 0);
//                        startActivity(tent);
//
//                    } else {
//                        // do something for phones running an SDK before lollipop
//
//
//                    Map simInfo = simsSubscriptionId();
//                    String ussd = "*144#";
//                    System.out.println(simInfo);
//                    int subscriptionId = 0;
//                    if (simInfo.containsKey("sim2")) {
//                        if (simInfo.get("sim2").toString().equalsIgnoreCase("Safaricom") || simInfo.get("sim2").toString().contains("Safaricom")) {
//                            ussd = "*144#";
//                            subscriptionId = Integer.parseInt(simInfo.get("2").toString());
//                            dialUssd(ussd, subscriptionId, this);
//                            System.out.println("I used this subscriptionid: " + subscriptionId);
//                        }
//                        if (simInfo.get("sim2").toString().equalsIgnoreCase("Airtel KE") || simInfo.get("sim2").toString().contains("Airtel") || simInfo.get("sim2").toString().equalsIgnoreCase("Airtel")) {
//                            ussd = "*133#";
//                            subscriptionId = Integer.parseInt(simInfo.get("sim2SubscriptionId").toString());
//                            dialUssd(ussd, subscriptionId, this);
//
//                            System.out.println("I used this subscriptionid: " + subscriptionId);
//                        }
//                        if (simInfo.get("sim2").toString().equalsIgnoreCase("Telkom") || simInfo.get("sim2").toString().contains("telcom")) {
//                            ussd = "*144#";
//                            subscriptionId = Integer.parseInt(simInfo.get("sim2SubscriptionId").toString());
//                            dialUssd(ussd, subscriptionId, this);
//                            System.out.println("I used this subscriptionid: " + subscriptionId);
//                        }
//                    }
//
//
//                }
//
//                }
                break;
            case R.id.logs:
                Intent intent = new Intent(getApplicationContext(), SMSLogoView.class);
                startActivity(intent);
                break;
            case R.id.plans:
                 intent = new Intent(getApplicationContext(), Home.class);
                startActivity(intent);
                break;
        }




    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
    public Map simsSubscriptionId() {
        Map map=new HashMap();

        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, REQUEST_READ_PHONE_STATE);
        } else {
            System.out.println("you got permissions");
            SubscriptionManager subscriptionManager = (SubscriptionManager) getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);

            List<SubscriptionInfo> subscriptionInfoList = subscriptionManager.getActiveSubscriptionInfoList();
            int i=0;
            for (SubscriptionInfo subscriptionInfo : subscriptionInfoList) {
                i++;
                int subscriptionId = subscriptionInfo.getSubscriptionId();
                //     Log.d("Sims", "subscriptionId:" + subscriptionId);
                map.put("sim"+ String.valueOf(i)+"SubscriptionId",subscriptionId);
            }

            if (subscriptionInfoList != null) {
                map.put("responseCode","200");
                if (subscriptionInfoList.size() == 1) {

                    String sim1 = subscriptionInfoList.get(0).getDisplayName().toString();
                    map.put("sim1",sim1);
//            tvSim1.setText(sim1);
                } else {
                    String sim1 = subscriptionInfoList.get(0).getDisplayName().toString();
                    String sim2 = subscriptionInfoList.get(1).getDisplayName().toString();
                    //   System.out.println("sim 1:"+sim1);

                    //   System.out.println("sims :"+sim2);
                    map.put("sim1",sim1); map.put("sim2",sim2);
                }

            }
            else {
                map.put("responseCode","301");
            }
        }





        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {


         //   runtimepermissions();


        }
        else {



        }
        return  map;

    }



    @RequiresApi(api = Build.VERSION_CODES.M)
    public void dialUssd(String ussdCode, int sim, Context context) {

        if (ussdCode.equalsIgnoreCase("")) return;


        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.CALL_PHONE}, 234);
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {


            TelephonyManager manager = (TelephonyManager) context.getSystemService(context.TELEPHONY_SERVICE);
            TelephonyManager manager2 = manager.createForSubscriptionId(sim);

            // TelephonyManager managerMain = (sim == 0) ? manager : manager2;
            ;
            System.out.println("network"+ manager.getNetworkOperator());

            manager2.sendUssdRequest(ussdCode, new TelephonyManager.UssdResponseCallback() {
                @Override
                public void onReceiveUssdResponse(TelephonyManager telephonyManager, String request, CharSequence response) {
                    super.onReceiveUssdResponse(telephonyManager, request, response);

                    Log.e("TAG", "onReceiveUssdResponse:  Ussd Response = " + response.toString().trim());
                    bal.setText(response.toString());


                }

                @Override
                public void onReceiveUssdResponseFailed(TelephonyManager telephonyManager, String request, int failureCode) {
                    super.onReceiveUssdResponseFailed(telephonyManager, request, failureCode);
                    bal.setText(failureCode + request);
                    Log.e("TAG", "onReceiveUssdResponseFailed: " + "" + failureCode + request);
                }
            }, new Handler(Looper.getMainLooper()) {
                @Override
                public void handleMessage(Message msg) {
                    Log.e("ERROR", "error");
                }
            });
        }

    }



    private  class BalanceGetter extends AsyncTask<String, Void, String> {


        @Override
        protected String doInBackground(String... strings) {
            return   Balance.smsBalance();
        }
        @Override
        protected void onPostExecute(String result) {

            smsbalance.setText("SMS Balance: "+result);

        }

    }
    private boolean isAccessibilityOn (Context context, Class<? extends AccessibilityService> clazz) {
        int accessibilityEnabled = 0;
        final String service = context.getPackageName () + "/" + clazz.getCanonicalName ();
        try {
            accessibilityEnabled = Settings.Secure.getInt (context.getApplicationContext ().getContentResolver (), Settings.Secure.ACCESSIBILITY_ENABLED);
        } catch (Settings.SettingNotFoundException ignored) {  }

        TextUtils.SimpleStringSplitter colonSplitter = new TextUtils.SimpleStringSplitter (':');

        if (accessibilityEnabled == 1) {
            String settingValue = Settings.Secure.getString (context.getApplicationContext ().getContentResolver (), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (settingValue != null) {
                colonSplitter.setString (settingValue);
                while (colonSplitter.hasNext ()) {
                    String accessibilityService = colonSplitter.next ();

                    if (accessibilityService.equalsIgnoreCase (service)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }


    public Boolean createEnviroment()
    {
        sq.execSQL("DROP TABLE IF EXISTS airtimelogs");
        sq.execSQL("DROP TABLE IF EXISTS plans");
db.creatLogsTable();
db.creatpackagesTable();

        Toast.makeText(this,"Enviroment Set Successfully", Toast.LENGTH_LONG).show();


        return true;
    }


    private void sendNotification() {

        JSONObject json = new JSONObject();
        try {
            json.put("to","/topics/"+"all");
            JSONObject notificationObj = new JSONObject();
            notificationObj.put("title","any title");
            notificationObj.put("body","any body");

            JSONObject extraData = new JSONObject();
            extraData.put("brandId","puma");
            extraData.put("category","Shoes");



            json.put("notification",notificationObj);
            json.put("data",extraData);

System.err.println(json);
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, URL,
                    json,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {

                            Log.d("MUR", "onResponse: ");
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d("MUR", "onError: "+error.networkResponse);
                }
            }
            ){
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> header = new HashMap<>();
                    header.put("content-type","application/json");
                    header.put("authorization","key=AAAAjApy9fo:APA91bFtI987sk4xRlt7GHvc_XSPUKfIAwir5TJqYjHUBwQ-pIKuAYY43o3hTa1N-0YpnxZXTblduDVyEpkUZaoTJYokCnJEctaXrwCl1XVktx8EXP6MaSdDXoyH0gpchRvCnpkFamRO");
                    return header;
                }
            };
            mRequestQue.add(request);
        }
        catch (JSONException e)

        {
            e.printStackTrace();
        }
    }
}