package com.lunar.quickswapserver;

import android.Manifest;
import android.accessibilityservice.AccessibilityService;
import android.annotation.SuppressLint;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

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

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import dmax.dialog.SpotsDialog;

public class Home extends AppCompatActivity implements View.OnClickListener {
    private final int MY_PERMISSIONS_REQUEST_SMS_RECEIVE = 100;
    ImageView dailyBundles,agents,minutes,monthlyBundle,weeklyBundles,sms,rescue,addpackage,smsCheck,env,order,blacklist,airtime,poker,gifts;
EditText serverAdress;
String deviceId="";
    private SpotsDialog progressDialog;
    InitialConfigs configs;
    DBHelper db;
    SQLiteDatabase sq;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        rescue=findViewById(R.id.rescueOption);
        serverAdress=findViewById(R.id.serverAndress);
        minutes=findViewById(R.id.minutes);
        dailyBundles=findViewById(R.id.dailyBundles);
        weeklyBundles=findViewById(R.id.weeklyBundles);
        monthlyBundle=findViewById(R.id.monthlyBundles);
        addpackage=findViewById(R.id.addPackage);
        smsCheck=findViewById(R.id.smsChecker);
        env=findViewById(R.id.enviromentset);
        order=findViewById(R.id.paidOrders);
        blacklist=findViewById(R.id.blackList);
        airtime=findViewById(R.id.airtime);
        poker=findViewById(R.id.poker);
        gifts=findViewById(R.id.gift);
        agents=findViewById(R.id.agents);

        sms=findViewById(R.id.sms);
        db=new DBHelper(this);
        sq=db.getWritableDatabase();
        progressDialog = new SpotsDialog(this, R.style.Custom);
        dailyBundles.setOnClickListener(this);
        smsCheck.setOnClickListener(this);
        blacklist.setOnClickListener(this);
        order.setOnClickListener(this);
        poker.setOnClickListener(this);
        agents.setOnClickListener(this);
        gifts.setOnClickListener(this);

        env.setOnClickListener(this);
        airtime.setOnClickListener(this);
        addpackage.setOnClickListener(this);
        weeklyBundles.setOnClickListener(this);
        monthlyBundle.setOnClickListener(this);
        rescue.setOnClickListener(this);
        sms.setOnClickListener(this);
        configs=new InitialConfigs(this);
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.RECEIVE_SMS,Manifest.permission.CALL_PHONE,Manifest.permission.READ_PHONE_STATE,Manifest.permission.READ_SMS,Manifest.permission.SEND_SMS,Manifest.permission.SEND_SMS,Manifest.permission.ACCESS_NOTIFICATION_POLICY},
                MY_PERMISSIONS_REQUEST_SMS_RECEIVE);

        minutes.setOnClickListener(this);
//        MyFirebaseMessagingService myFirebaseMessagingService=new MyFirebaseMessagingService();
//        myFirebaseMessagingService.FcmToken();
        startService(new Intent(this, Recharger.class));
//        if (!isAccessibilityOn (this, WhatsappAccessibilityService.class)) {
//            Intent intent = new Intent (Settings.ACTION_ACCESSIBILITY_SETTINGS);
//            this.startActivity (intent);
//        }




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
                   serverAdress.setText(token);
deviceId=token;
                           System.err.println("Token token"+token);
                        configs.saveApplicationDetails(configs.getrescueValue(),configs.getswitchState(),configs.getloaderSwitch(), Home.this,configs.getServerStatus(),token);
                    }
                });


        Switch onOffSwitch = (Switch)  findViewById(R.id.on_off_switch);
        Switch paymentSwitch = (Switch)  findViewById(R.id.paymentswitch);
        Switch isSever = (Switch)  findViewById(R.id.isSever);

        if(configs.getswitchState().equalsIgnoreCase("true"))
        {
            onOffSwitch.setChecked(true);
        }
        else{
            onOffSwitch.setChecked(false);
        }

        if(configs.getloaderSwitch().equalsIgnoreCase("true"))
        {
            paymentSwitch.setChecked(true);
        }
        else{
            paymentSwitch.setChecked(false);
        }

        if(configs.getServerStatus()!=null&&configs.getServerStatus().equalsIgnoreCase("true"))
        {
            isSever.setChecked(true);
        }
        else{
            isSever.setChecked(false);
        }
        onOffSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                {
                    configs.setswitchState("true");
                    configs.saveApplicationDetails(configs.getrescueValue(),"true",configs.getloaderSwitch(), Home.this,configs.getServerStatus(),configs.getServerId());
                }
                else{
                    configs.saveApplicationDetails(configs.getrescueValue(),"false",configs.getloaderSwitch(), Home.this,configs.getServerStatus(),configs.getServerId());
                    configs.setswitchState("false");
                }
            }



        });

        paymentSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                {
                    configs.setloaderSwitch("true");
                    configs.saveApplicationDetails(configs.getrescueValue(),configs.getswitchState(),"true", Home.this,configs.getServerStatus(),configs.getServerId());
                }
                else{
                    configs.saveApplicationDetails(configs.getrescueValue(),configs.getswitchState(),"false", Home.this,configs.getServerStatus(),configs.getServerId());
                    configs.setloaderSwitch("false");
                }
            }



        });




        isSever.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                {
                    configs.setServerStatus("true");
                    configs.saveApplicationDetails(configs.getrescueValue(),configs.getswitchState(),configs.getloaderSwitch(), Home.this,"true",configs.getServerId());
                }
                else{
                    configs.saveApplicationDetails(configs.getrescueValue(),configs.getswitchState(),configs.getloaderSwitch(), Home.this,"false",configs.getServerId());
                    configs.setServerStatus("false");
                }
            }



        });



        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent intent = new Intent();
            String packageName = getPackageName();
            PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + packageName));
                startActivity(intent);
            }
        }

        Thread.setDefaultUncaughtExceptionHandler(new MyExceptionHandler(this,
                Home.class));

    }
    public void Poker() {
        String query_url = "https://api.safaricom.co.ke/mpesa/c2b/v2/registerurl";

        try {
            URL url = new URL(query_url);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5000);
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestMethod("GET");

            InputStream in = new BufferedInputStream(conn.getInputStream());
            String result = IOUtils.toString(in, "UTF-8");

          System.err.println(result);


            in.close();
            conn.disconnect();

        } catch (Exception e) {

        }
    }




    @Override
    public void onClick(View view) {
        switch (view.getId())
        {

            case R.id.dailyBundles:
           Intent intent = new Intent(getApplicationContext(), DailyBundles.class);
                startActivity(intent);
                System.err.println("Orders"+db.getAllOrders());
                break;


            case R.id.weeklyBundles:
                intent = new Intent(getApplicationContext(), WeeklyBundles.class);
                intent.putExtra("title","Weekly");
                intent.putExtra("package","Daily");

                startActivity(intent);

                break;

            case R.id.gift:
                intent = new Intent(getApplicationContext(), Gifts.class);
                intent.putExtra("title","Weekly");
                intent.putExtra("package","Daily");

                startActivity(intent);

                break;

            case R.id.addPackage:
                intent = new Intent(getApplicationContext(), PlanRegistration.class);


                startActivity(intent);

                break;
            case R.id.monthlyBundles:
                intent = new Intent(getApplicationContext(), MonthlyBundles.class);
                intent.putExtra("title","Weekly");
                intent.putExtra("package","Daily");

                startActivity(intent);

                break;

            case R.id.minutes:
                intent = new Intent(getApplicationContext(), Minutes.class);
                intent.putExtra("spendAmount","Amount To Spend");
                intent.putExtra("package","Daily");

                startActivity(intent);

                break;
            case R.id.sms:
                intent = new Intent(getApplicationContext(), SMS.class);
                intent.putExtra("spendAmount","Amount To Spend");
                intent.putExtra("package","Daily");

                startActivity(intent);

                break;
            case R.id.airtime:
                intent = new Intent(getApplicationContext(), Airtime.class);

                intent.putExtra("package","Airtime");

                startActivity(intent);

                break;

            case R.id.paidOrders:
                intent = new Intent(getApplicationContext(), OrdersView.class);
                intent.putExtra("spendAmount","Amount To Spend");
                intent.putExtra("package","Daily");

                startActivity(intent);

                break;

            case R.id.blackList:
               Toast.makeText(this,"Comming Soon", Toast.LENGTH_LONG).show();

                break;

            case R.id.smsChecker:
                intent = new Intent(getApplicationContext(), SMSRecharge.class);
                intent.putExtra("spendAmount","Amount To Spend");
                intent.putExtra("package","Daily");

                startActivity(intent);

                break;
            case R.id.agents:
                intent = new Intent(getApplicationContext(), Agents.class);
                intent.putExtra("spendAmount","Amount To Spend");
                intent.putExtra("package","Daily");

                startActivity(intent);

                break;

            case R.id.poker:

                clientPoker();

                break;

            case R.id.enviromentset:
                AlertDialog.Builder    alert= new AlertDialog.Builder(this);
                alert.setTitle("Confirm Action");
                alert.setMessage("Are you sure you want Prepare The Enviroment\n This  will Delete All Plans History and You Will Have To Create The Afresh");
                alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        createEnviroment();
                       //createCustomEnviroment();
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



            case R.id.rescueOption:
                  alert= new AlertDialog.Builder(this);

// Set up the input
                final EditText input = new EditText(this);

// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_CLASS_NUMBER);
                alert.setView(input);
                InitialConfigs configs=new InitialConfigs(Home.this);
                input.setText(configs.getrescueValue());
                System.err.println("Rescue"+configs.getrescueValue());
                alert.setTitle("Confirm Action");
                alert.setMessage("This Option Gives You The Ability To Configure The Amount Customer Will Pay Incase He/She Sends A Please Call Me To this Number,The System Automaticaly Loads The Airtime of Specified Value The Current Set Value is KSH "+configs.getrescueValue());
                alert.setPositiveButton("Save", new DialogInterface.OnClickListener() {

                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
if(DataValidation.number(input.getText().toString()))
{
    configs.saveApplicationDetails(input.getText().toString(),configs.getswitchState(),configs.getloaderSwitch(), Home.this,configs.getServerStatus(),configs.getServerId());
    Toast.makeText(Home.this,"Rescue Value Saved", Toast.LENGTH_LONG).show();
    dialog.dismiss();
}
else{
    Toast.makeText(Home.this,"Invalid Value", Toast.LENGTH_LONG).show();
}




                    }
                });

                alert.setNegativeButton("Revert", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                alert.show();
                break;


        }

    }
    public Boolean createEnviroment()
    {

        sq.execSQL("DROP TABLE IF EXISTS airtimelogs");
        sq.execSQL("DROP TABLE IF EXISTS plans");
        sq.execSQL("DROP TABLE IF EXISTS plansRequests");
        sq.execSQL("DROP TABLE IF EXISTS orders");
        sq.execSQL("DROP TABLE IF EXISTS agents");
        sq.close();
        db.creatLogsTable();
        db.creatpackagesTable();
        db.creatpackagesRequestsTable();
        db.createOrdersTable();
        db.createAgentTable();


        Toast.makeText(this,"Enviroment Set Successfully", Toast.LENGTH_LONG).show();


        return true;
    }
    public Boolean createCustomEnviroment()
    {

       db.createAgentTable();

        Toast.makeText(this,"Custom Enviroment Created Successfully", Toast.LENGTH_LONG).show();


        return true;
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


    public void clientPoker() {
        progressDialog.show();
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        JSONObject mainObj = new JSONObject();
        try {
            mainObj.put("to", deviceId);
            JSONObject notiObject = new JSONObject();
            notiObject.put("title", "Server Rechability");
            notiObject.put("body", "Server Reachable By Clients");
            notiObject.put("icon", "icon"); // enter icon that exists in drawable only


            JSONObject extraData = new JSONObject();

            extraData.put("plan","Self Poker Success");


            mainObj.put("notification", notiObject);
            mainObj.put("data", extraData);
;

            System.err.println("note"+mainObj);
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, "https://fcm.googleapis.com/fcm/send", mainObj, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    progressDialog.dismiss();
                    System.err.println("respo:"+response);
                    Toast.makeText(Home.this,"Request Sent Wait For Reply:Response"+response, Toast.LENGTH_LONG).show();

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    // code run is got error
                    progressDialog.dismiss();
                    System.err.println(error.networkResponse);
                    Toast.makeText(Home.this,"Error:"+error.networkResponse, Toast.LENGTH_LONG).show();
                }
            }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {


                    Map<String, String> header = new HashMap<>();
                    header.put("content-type", "application/json");
                    header.put("authorization", "key=" + "AAAA6uReyfQ:APA91bGA9HMKC0b-nT0mTitppGti_DZ5AOlWxEKTcTDlgkjFv-kFUIXVJMa53R1Yk7nNZWO0HvcL0mKLpyKJ-9VqnaImIMwX-4dQjdH-gmrCTVj5w82LeLySC3P7UjnQ7g2xG7feM5UP");
                    return header;


                }
            };
            requestQueue.add(request);


        } catch (JSONException e) {
            e.printStackTrace();
        }




    }

}