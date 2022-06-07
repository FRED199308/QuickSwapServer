package com.lunar.quickswapserver;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.Date;
import java.util.List;

import static androidx.core.app.ActivityCompat.requestPermissions;

public class USSDCalls {


    private static final int REQUEST_READ_PHONE_STATE =23 ;

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void dialUssd(String ussdCode, int sim, Context context, String phone, int amount, int value, String network) {
DBHelper dbHelper=new DBHelper(context);
        if (ussdCode.equalsIgnoreCase("")) return;


        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
           requestPermissions(new Activity(),new String[]{Manifest.permission.CALL_PHONE}, 234);
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {


            TelephonyManager manager = (TelephonyManager) context.getSystemService(context.TELEPHONY_SERVICE);
            TelephonyManager manager2 =






                    manager.createForSubscriptionId(sim);

            // TelephonyManager managerMain = (sim == 0) ? manager : manager2;
            ;
            System.out.println("network"+ manager.getNetworkOperator());

            manager2.sendUssdRequest(ussdCode, new TelephonyManager.UssdResponseCallback() {
                @Override
                public void onReceiveUssdResponse(TelephonyManager telephonyManager, String request, CharSequence response) {
                    super.onReceiveUssdResponse(telephonyManager, request, response);

                    Log.e("TAG", "onReceiveUssdResponse:  Ussd Response = " + response.toString().trim());
                    String remark="completed";
                    if(response.toString().startsWith("You have"))
                    {
                        remark="completed" ;
                    }
                    else
                    {
                        remark="Failed";
                    }
dbHelper.insertAirtimeRecord("Unknown",phone,amount, String.valueOf(new Date()),value,"Success",remark,response.toString(),network);

                }

                @Override
                public void onReceiveUssdResponseFailed(TelephonyManager telephonyManager, String request, int failureCode) {
                    super.onReceiveUssdResponseFailed(telephonyManager, request, failureCode);

                    Log.e("TAG", "onReceiveUssdResponseFailed: " + "" + failureCode + request);
                    String remark="completed";

                    dbHelper.insertAirtimeRecord("Unknown",phone,amount, String.valueOf(new Date()),value,"Failed","Failed",failureCode + request,network);
                }
            }, new Handler(Looper.getMainLooper()) {
                @Override
                public void handleMessage(Message msg) {
                    Log.e("ERROR", "error");
                }
            });
        }

    }
    public boolean runtimepermissions(Activity activity, Context context) {
        if (Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            activity.requestPermissions(new String[]{Manifest.permission.READ_PHONE_STATE, Manifest.permission.CALL_PHONE}, 100);
            System.out.println("has permissions");
            return true;

        }

        return false;
    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
    public void sims(Context context) {


        int permissionCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE);

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
           // ActivityCompat.requestPermissions(context, new String[]{Manifest.permission.READ_PHONE_STATE}, REQUEST_READ_PHONE_STATE);
        } else {
            System.out.println("you got permissions");
            SubscriptionManager subscriptionManager = (SubscriptionManager) context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);

            List<SubscriptionInfo> subscriptionInfoList = subscriptionManager.getActiveSubscriptionInfoList();

            for (SubscriptionInfo subscriptionInfo : subscriptionInfoList) {
                int subscriptionId = subscriptionInfo.getSubscriptionId();
                Log.d("Sims", "subscriptionId:" + subscriptionId);
            }

            if (subscriptionInfoList != null) {
                if (subscriptionInfoList.size() == 1) {
                    String sim1 = subscriptionInfoList.get(0).getDisplayName().toString();
//            tvSim1.setText(sim1);
                } else {
                    String sim1 = subscriptionInfoList.get(0).getDisplayName().toString();
                    String sim2 = subscriptionInfoList.get(1).getDisplayName().toString();

                }

            }
        }







    }
    public static Uri ussdToCallableUri(String ussd) {

        String uriString = "";

        if(!ussd.startsWith("tel:"))
            uriString += "tel:";

        for(char c : ussd.toCharArray()) {

            if(c == '#')
                uriString += Uri.encode("#");
            else
                uriString += c;
        }

        return Uri.parse(uriString);
    }
}
