package com.lunar.quickswapserver;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.telephony.SmsManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.JsonArray;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    String payingPhone = "", rechargingPhone = "",requestType="", costfromClient ="";
    int paymentValue = 0;
    String msg_from="";
    String requestMode="";
    DBHelper db;
    String plan="";
    String registrationId="";
    SQLiteDatabase sq;
    InitialConfigs configs;
    private RequestQueue mRequestQue;
    LipaRequest lipaRequest;
    private String URL = "https://fcm.googleapis.com/fcm/send";
String orderNumber="";
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        Thread.setDefaultUncaughtExceptionHandler(new MyExceptionHandler(this,
                Home.class));
        orderNumber = "";
        //if(configs.getServerId().equalsIgnoreCase("hdh"))
        if(lipaRequest==null)
        {
            lipaRequest = new LipaRequest();
        }

        super.onMessageReceived(remoteMessage);
        if(db==null)
        {
            db = new DBHelper(this);
        }
     if(configs==null)
     {
         configs = new InitialConfigs(this);
     }


        if(mRequestQue==null)
        {
            mRequestQue = Volley.newRequestQueue(this);
        }
        FirebaseMessaging.getInstance().subscribeToTopic("all");
        Map<String, String> extraData = remoteMessage.getData();

        String networkPlan = extraData.get("network");
        String planrequeted = extraData.get("plan");
        registrationId = extraData.get("deviceId");


        if (!configs.getswitchState().equalsIgnoreCase("true")) {

            String messageToReply = "Sorry! We are experiencing technical difficulties, we'll return shortly. We apologise for any inconveniences this may cause.";

            sendConfirmationNotifcation(this, messageToReply, registrationId, rechargingPhone, null);


        } else {



        if (extraData.get("requestType") != null) {
            requestType = extraData.get("requestType");
        }

        if (requestType.equalsIgnoreCase("paidOrder")) {


            if (configs.loaderSwitch.equalsIgnoreCase("true")) {
                paymentProcessor(extraData.get("orderDetails"), this);
                planrequeted = "Payment";
                sendNotification(extraData.get("orderDetails"), "Payment", R.string.redColor);

            } else {
                Toast.makeText(this, "Received Package Purchase But Payment Processor Service Is Off", Toast.LENGTH_LONG).show();
            }

        }
        if (requestType.equalsIgnoreCase("AgentRegistration")) {


            if (configs.getServerStatus().equalsIgnoreCase("true")) {

                System.err.println(extraData.get("plan"));
                System.err.println("kkkkkkkkkkkkkkkkkkk");
                String response = registerAgent(extraData.get("plan"));
                if (response.equalsIgnoreCase("Saved")) {
                    sendConfirmationNotifcation(this, "\"Thank you for choosing QuickSwap Kenya,Your Request To Be An Agent Has Been Received And Is Awaiting Approval.Always Use Your Nominated Number to Pay When Ordering On behalf Of Your Customers", registrationId, "Report Received", null);


                } else {
                    sendConfirmationNotifcation(this, response, registrationId, "Report Received", null);

                }

            } else {
                Toast.makeText(this, "Received Agent Registration Request but Service Is Off", Toast.LENGTH_LONG).show();
            }

        }

        if (requestType.equalsIgnoreCase("Report") && extraData.get("plan") != null) {


            if (configs.serverStatus.equalsIgnoreCase("true")) {
                sendSms(this, extraData.get("plan"), getResources().getString(R.string.telkomNumber), true);


                String contactNumber = (extraData.get("plan").split(",")[2]).replace("Contact Number:", "");
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                sendSms(this, "Thank you for choosing QuickSwap Kenya, We have received your Report / Suggestion and are working on it. We shall get back to you within 2 working days.", contactNumber, true);

                sendConfirmationNotifcation(this, "\"Thank you for choosing QuickSwap Kenya, We have received your Report / Suggestion and are working on it. We shall get back to you within 2 working days.\"", registrationId, "Report Received", null);

            } else {

                Toast.makeText(this, "Received A Report But Payment Server  Status is Off Service Is Off", Toast.LENGTH_LONG).show();
            }

        }
        if (requestType.equalsIgnoreCase("Airtime Conversion") && configs.getServerStatus().equalsIgnoreCase("true") && extraData.get("plan") != null) {
            sendSms(this, extraData.get("plan"), getResources().getString(R.string.telkomNumber), true);
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            String contactNumber = (extraData.get("plan").split("#")[2]).replace("Contact Number:", "");

            sendSms(this, "Convert request received. We are texting you the number to send the airtime to from 0703757633.\n" +
                    "QuickSwap KE.", contactNumber, true);
            sendConfirmationNotifcation(this, "Convert request received. We are texting you the number to send the airtime to from 0703757633.\n" +
                    "QuickSwap KE.", registrationId, "Conversion Request Received", null);

            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


        }


        if (configs.getServerStatus().equalsIgnoreCase("true")) {
            if (planrequeted != null && (appPredefinedRequestformatVerifier(planrequeted) || appPredefinedUnknownCostRequestformatVerifier(planrequeted))) {


                sendNotification(planrequeted, "Plan Request", R.string.blackColor);
                String messageContent = planrequeted;
                System.err.println("App Format Accepted");
                orderNumber = IdGenerator.keyGen();
                System.err.println("Order Number:" + orderNumber);
                db = new DBHelper(this);
                sq = db.getWritableDatabase();

                String packageDetail[] = messageContent.split("#");
                String network = packageDetail[1];
                plan = packageDetail[0];

                rechargingPhone = packageDetail[2];
                payingPhone = packageDetail[3];
                if (packageDetail.length > 4) {
                    costfromClient = packageDetail[4];
                } else {
                    costfromClient = "";
                }
                System.err.println("Pack Deatils" + messageContent);
                requestType = "Plan#" + plan + network;
                System.err.println("Network: " + network);
                System.err.println("Cost: " + costfromClient);
                System.err.println("plan: " + plan);
                System.err.println("paying phone: " + payingPhone);
                System.err.println("receiver: " + rechargingPhone);

                String savedPlanCost = db.getPlanCostDetails(plan, network).get("cost");

                String agentDetails = db.getAgentDetails(payingPhone).get("agentName");
                if (agentDetails.isEmpty() || !db.getAgentDetails(payingPhone).get("status").equalsIgnoreCase("Approved")) {
                    System.err.println("Not An Agent");
                    savedPlanCost = db.getPlanCostDetails(plan, network).get("cost");
                } else {
                    System.err.println("Is Agent");
                    savedPlanCost = db.getPlanCostDetails(plan, network).get("agentCost");
                    if (savedPlanCost.isEmpty()) {
                        savedPlanCost = db.getPlanCostDetails(plan, network).get("cost");
                    }

                }


                if (savedPlanCost.isEmpty() || savedPlanCost.equalsIgnoreCase("0")) {
                    if (db.getAllplans().size() > 0) {
                        db.registerPlanRequest(plan, requestType, Integer.parseInt("0"), network, rechargingPhone, String.valueOf(new Date()), "Unknown", "Online", orderNumber, payingPhone);
                        String messageToReply = "This Plan Is Currently Not Available,Ensure You have Refreshed The Packages in The Application, Or Please Contact The Admin For Assistance via :0703757633";

                        sendConfirmationNotifcation(this, messageToReply, registrationId, rechargingPhone, null);
                    }
                } else {


                    if (costfromClient.isEmpty()) {
                        //process Known Cost Packages like daily sms
                        if (configs.getswitchState().equalsIgnoreCase("true")) {
                            int cost = Integer.parseInt(savedPlanCost);
                            System.err.println("Cost Found" + cost);
                            paymentValue = cost;
                            db.registerPlanRequest(plan, requestType, cost, network, rechargingPhone, String.valueOf(new Date()), "Unknown", "Online", orderNumber, payingPhone);
                            orderRequestProcessor(this);
                        } else {
                            if (configs.getServerStatus().equalsIgnoreCase("true")) {
                                String messageToReply = "Shop Closed Please Try Again Later,Opening Hours,Monday To Friday 8:00AM to 9:00PM Saturday And Sunday 9:00AM To 8:00PM.\n You Can Still Request Airtime Any Time And Buy The Package You Wish From You End";

                                sendConfirmationNotifcation(this, messageToReply, registrationId, "Plan Request Confirmation", null);

                            }

                        }

                    } else {


                        if (plan.equalsIgnoreCase("Airtime")) {
                            int percentageDiscount = Integer.parseInt(savedPlanCost);

                            // paymentValue=  ( ( (100-percentageDiscount)*Integer.parseInt(costfromClient))/100);
                            int airtime = Integer.parseInt(costfromClient);
                            int commission = (int) ((Double.valueOf(percentageDiscount) / 100) * airtime);

                            paymentValue = airtime - commission;
                            db.registerPlanRequest(plan, requestType, paymentValue, network, rechargingPhone, String.valueOf(new Date()), "Unknown", "Online", orderNumber, payingPhone);
                            orderRequestProcessor(this); //process
                            System.err.println("Retrieved Details:" + db.getRequestDetails(orderNumber));
                        } else {

                            if (configs.getswitchState().equalsIgnoreCase("true")) {
                                int percentageDiscount = Integer.parseInt(savedPlanCost);
                                //  paymentValue=  ( ( (100-percentageDiscount)*Integer.parseInt(costfromClient))/100);
                                int airtime = Integer.parseInt(costfromClient);
                                int commission = (int) ((Double.valueOf(percentageDiscount) / 100) * airtime);
                                paymentValue = airtime - commission;
                                db.registerPlanRequest(plan, requestType, paymentValue, network, rechargingPhone, String.valueOf(new Date()), "Unknown", "Online", orderNumber, payingPhone);
                                orderRequestProcessor(this);
                            } else {
                                if (configs.getServerStatus().equalsIgnoreCase("true")) {
                                    String messageToReply = "Shop Closed Please Try Again Later,Opening Hours,Monday To Friday 8:00AM to 9:00PM Saturday And Sunday 9:00AM To 8:00PM.\n You Can Still Request Airtime Any Time And Buy The Package You Wish From You End";

                                    sendConfirmationNotifcation(this, messageToReply, registrationId, "Plan Request Confirmation", null);

                                }

                            }

                        }


                    }


                }


            } else {

                if (planrequeted.equalsIgnoreCase("All Plans")) {


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

                                    System.err.println("Token token" + token);
                                    sendPackagesToServer(MyFirebaseMessagingService.this, token, "All plans", registrationId, "Packages", db.getAllplans());

                                }
                            });

                }


            }
        }


    }


    }
    private void sendNotification(String messageBody,String title,int color) {
        Intent intent = new Intent(this, NotificationMessages.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("title",title);
        intent.putExtra("message",messageBody);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        String channelId = IdGenerator.keyGen();
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.service)
                        .setContentTitle(title)
                        .setContentText(messageBody)
                        .setAutoCancel(false)
                        .setSound(defaultSoundUri)
                        .setColor(color)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(Integer.parseInt(channelId) /* ID of notification */, notificationBuilder.build());
    }
    public boolean appPredefinedRequestformatVerifier(String mess) {

        String m = "[A-Za-z0-9 +-@.,()]{2,50}[#][A-Za-z0-9]{2,30}[#][0-9]{10}[#][0-9]{10}";

        Pattern pattern = Pattern.compile(m);

        Matcher remach = pattern.matcher(mess.toUpperCase());

        if (remach.matches() ) {

            return true;


        } else {

            return false;
        }


    }
public String registerAgent(String details)
{
    details=details.replaceFirst("Contact Number:","");
    SimpleDateFormat format=new SimpleDateFormat("yyyy-M-dd hh:mm:ss");
    String fullname=details.split("#")[1]+" "+details.split("#")[2];
    String contact=details.split("#")[0];
  String response=  db.registerAgent(fullname,contact,format.format(new Date()));

    return response;
}

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
    public void orderRequestProcessor(Context context)
    {



        int subscriptionId;
        String messageToReply ="Please Enter Your Mpesa Pin Once Prompted By Safaricom To Complete Purchase Of:"+plan+" for :"+rechargingPhone+" Thank you";

        sendConfirmationNotifcation(this, messageToReply, registrationId, "Plan Request Confirmation",null);
        // System.err.println("message from :"+msg_from.replace("+254","0"));
        if(payingPhone.equalsIgnoreCase(rechargingPhone.replace("+254","0")))
        {


        }
        else{
            //   sendMultSMS(context, messageToReply, subscriptionId, payingPhone,true);
        }
        sendSms(context, messageToReply,  payingPhone,true);
        Token token = new Token();
        token.execute();


    }

    public  boolean appPredefinedUnknownCostRequestformatVerifier(String mess) {

        String m = "[A-Za-z0-9 +-@.,()]{2,50}[#][A-Za-z0-9]{2,30}[#][0-9]{10}[#][0-9]{10}[#][0-9]{1,7}";

        Pattern pattern = Pattern.compile(m);

        Matcher remach = pattern.matcher(mess.toUpperCase());

        if (remach.matches() ) {

            return true;


        } else {

            return false;
        }


    }

public boolean sendPackagesToServer(Context context, String serverToken, String message, String deviceNumber, String requestType, ArrayList plans)
{
    JSONObject json = new JSONObject();
    try {
JSONArray array=new JSONArray();
        String[] records=new String[plans.size()];
for(int i=0;i<plans.size();i++)
{

    Map map=(Map) plans.get(i);
    JSONObject ob=new JSONObject();
   ob.put("planname",map.get("planname").toString());
   ob.put("cost",map.get("cost").toString());
   ob.put("network",map.get("network").toString());
   ob.put("planType",map.get("planType").toString());

    array.put(ob);
}


        json.put("packages",array);
        json.put("serverToken",serverToken);



        JsonObjectRequest request = new JsonObjectRequest(com.android.volley.Request.Method.POST, "https://api.lunar.cyou/api/packages.php",
                json,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                       String REQUESTTYPE="Plan Sent";
sendConfirmationNotifcation(context,message,deviceNumber,REQUESTTYPE,"");
                        Log.d("MUR", "onResponse: "+response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.err.println(error);
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
        mRequestQue.add(request);
    }
    catch (JSONException e)

    {
        e.printStackTrace();
    }
return true;

}
    private void sendConfirmationNotifcation(Context context, String message, String deviceNumber, String requestType, String plans) {


        JSONObject json = new JSONObject();
        try {
            json.put("to",deviceNumber);
            JSONObject notificationObj = new JSONObject();
            notificationObj.put("title","Request Received");
            notificationObj.put("body",message);

            JSONObject extraData = new JSONObject();
            extraData.put("message",message);
            extraData.put("plans","sent");
            extraData.put("requestType",requestType);
            JSONObject extraDataPriority = new JSONObject();
            extraDataPriority.put("priority","high");
            extraDataPriority.put("TTL","0");


           // json.put("notification",notificationObj);
            json.put("priority","high");
            json.put("data",extraData);
            json.put("android",extraDataPriority);


            JsonArray array=new JsonArray();
            array.add(deviceNumber);

            System.err.println("load:"+json);
            JsonObjectRequest request = new JsonObjectRequest(com.android.volley.Request.Method.POST, URL,
                    json,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {

                            Log.d("MUR", "onResponse: "+response);
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    System.err.println(error);
                    Log.d("MUR", "onError: "+error.networkResponse);
                }
            }
            ){
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> header = new HashMap<>();
                    header.put("content-type","application/json");
                    header.put("authorization","key=AAAApiDN6pQ:APA91bFeGukqabOFIDmcs2R8w97v8cSfSesbPdryNE-R-cW482bAyIZXaAXCF0n29lve2ijrogLHgsOzetEA8X5yM1X77oodi3u1woW5eqMolcZM_sZKQskr4FrgKY99jzHEBu9eCzyx");
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

    private class Token extends AsyncTask<String, Void, String>
    {

        @Override
        protected String doInBackground(String...voids) {

            try {
                AuthToken authToken=new AuthToken();
                return authToken.getAccessToken().getToken();
            } catch (Exception e) {
                e.printStackTrace();
                return "Error :"+e.toString();
            }


        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();


        }

        @Override
        protected void onPostExecute(String token) {

            if(token.startsWith("Error"))
            {



            }
            else{
                MpesaRequest request=new MpesaRequest();
                request.execute(token,plan);
            }



        }

    }
    private class MpesaRequest extends AsyncTask<String, Void, LipaNaMpesaRequest> {

        @Override
        protected LipaNaMpesaRequest doInBackground(String...voids) {

            if(payingPhone!=null&&!payingPhone.isEmpty())
            {
                return  lipaRequest.makeRequest(voids[0],payingPhone,paymentValue,rechargingPhone,voids[1],registrationId,getResources().getString(R.string.clientFcmToken),getResources().getString(R.string.serverFcmKey),configs.getServerId(),orderNumber);

            }
            else{
                return  null;
            }



        }

        @Override
        protected void onPreExecute() {


        }

        @Override
        protected void onPostExecute(LipaNaMpesaRequest lipa) {


            //System.out.println("Request Response :"+lipa.getResponseDescription());


        }

    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    public void paymentProcessor(String orderDetails, Context context)
    {

        String content[]=orderDetails.split("#");

        String paidorderNumber="";
        if(content.length>4)
        {
            paidorderNumber=content[4];
        }
        payingPhone =db.getRequestDetails(paidorderNumber).get("payingPhone");


        String receiver=content[2].replaceAll(" ",""),packageName=content[0].replaceFirst("TILL ", "");
        int amount= Double.valueOf(content[1]).intValue(), value;
        String mpesaCode=content[3];
        if (orderDetails.startsWith("TILL")) {
            orderDetails = orderDetails.replaceFirst("TILL ", "");


            if (receiver.startsWith("+25")) {
                receiver = "0" + receiver.substring(4);
            }
            plan=packageName;

            String commm=db.getPlanCostDetails(plan,Globals.networkProviderDeterminer(receiver)).get("cost");
            String agentDetails=db.getAgentDetails(payingPhone).get("agentName");
            if(!agentDetails.isEmpty()&&db.getAgentDetails(payingPhone).get("status").equalsIgnoreCase("Approved"))
            {
                commm=db.getPlanCostDetails(plan,Globals.networkProviderDeterminer(receiver)).get("agentCost");


            }
            System.err.println("Receiver:"+receiver+" plan:"+plan+" Amount:"+amount+" mpesa code:"+mpesaCode);
            value = (amount * 100) / (100- Integer.valueOf(commm));
            SimpleDateFormat format=new SimpleDateFormat("dd-M-yyyy hh:mm:ss");
            db.registerOrder(packageName,"",value,Globals.networkProviderDeterminer(receiver),receiver, String.valueOf(format.format(new Date())),"Un known","SMS",mpesaCode,paidorderNumber);
            System.err.println("Airtime Value:"+value+" Commision Used:"+commm);
            if(packageName.startsWith("A"))
            {

             if(Globals.networkProviderDeterminer(receiver).equalsIgnoreCase("Telkom"))
             {
                 String message="Buy "+Globals.networkProviderDeterminer(receiver)+" "+packageName+" For: "+receiver+" Worth KSH "+value+" Order Number: "+paidorderNumber;
                 sendSms(context,message,context.getResources().getString(R.string.telkomNumber),true);

             }
             else{
                 sendAirtime(receiver,orderDetails,value,context);
             }
            }
            else{
                if(plan.equalsIgnoreCase("No Expiry Call And SMS")||plan.equalsIgnoreCase("No Expiry Bundles"))
                {
                    value = (amount * 100) / (100- Integer.valueOf(commm));
                    String message="Buy "+Globals.networkProviderDeterminer(receiver)+" "+packageName+" For: "+receiver+" Worth KSH "+value+" Order Number: "+paidorderNumber;
                    sendSms(context,message,context.getResources().getString(R.string.telkomNumber),true);
                }
                else {
                    value = Integer.valueOf(db.getPlanCostDetails(plan,Globals.networkProviderDeterminer(receiver)).get("actualCost"));
                    String message="Buy "+Globals.networkProviderDeterminer(receiver)+" "+packageName+" For: "+receiver+" Worth KSH "+value+" Order Number: "+paidorderNumber;
                    sendSms(context,message,context.getResources().getString(R.string.telkomNumber),true);
                }


            }



        }
        else
            {


            String airtimesReceiver = receiver;
            if (receiver.startsWith("+25")) {
                receiver = "0" + receiver.substring(4);
            }
            plan=packageName;

            String commm=db.getPlanCostDetails(plan,Globals.networkProviderDeterminer(receiver)).get("cost");
            System.err.println("Receiver:"+receiver+" Plan Cost:"+commm+" plan"+plan);
            value = (int) ((Double.valueOf(amount) * (((100+ Double.valueOf(commm)))/100)));
            SimpleDateFormat format=new SimpleDateFormat("dd-M-yyyy hh:mm:ss");
            db.registerOrder(packageName,"",value,Globals.networkProviderDeterminer(receiver),receiver, String.valueOf(format.format(new Date())),"Un known","SMS",mpesaCode,paidorderNumber);
            if(Globals.networkProviderDeterminer(receiver).equalsIgnoreCase("Telkom"))
            {
                String message="Buy "+Globals.networkProviderDeterminer(receiver)+" "+packageName+" For: "+receiver+" Worth KSH "+value+" Order Number: "+paidorderNumber;
                sendSms(context,message,context.getResources().getString(R.string.telkomNumber),true);

            }
            else{
                sendAirtime(receiver,orderDetails,value,context);
            }
        }








    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
    private void sendSms(Context context, String message,  String destination, boolean sim1) {

//        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
//            requestPermissions(new Activity(),new String[]{Manifest.permission.SEND_SMS}, 23);
//            return;
//        }
//
//        SmsManager sms = SmsManager.getDefault();
//        ArrayList<String> parts = sms.divideMessage(message);
//        SmsManager.getSmsManagerForSubscriptionId(susbscriptionId).sendMultipartTextMessage(destination, null, parts, null,
//                null);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            SubscriptionManager localSubscriptionManager = SubscriptionManager.from(context);
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the Order grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            if (localSubscriptionManager.getActiveSubscriptionInfoCount() > 1) {
                List localList = localSubscriptionManager.getActiveSubscriptionInfoList();

                SubscriptionInfo simInfo1 = (SubscriptionInfo) localList.get(0);
                SubscriptionInfo simInfo2 = (SubscriptionInfo) localList.get(1);


                if(sim1)
                {
                    //SendSMS From SIM One
                    SmsManager.getSmsManagerForSubscriptionId(simInfo1.getSubscriptionId()).sendTextMessage(destination, null, message, null, null);

                }
                else{
                    //SendSMS From SIM Two
                    SmsManager.getSmsManagerForSubscriptionId(simInfo2.getSubscriptionId()).sendTextMessage(destination, null, message, null, null);

                }


            }
        } else {
//            SmsManager.getDefault().sendTextMessage(customer.getMobile(), null, smsText, sentPI, deliveredPI);
//            Toast.makeText(getBaseContext(), R.string.sms_sending, Toast.LENGTH_SHORT).show();
        }

    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
    public Map simsSubscriptionId(Context context) {
        Map map=new HashMap();

        int permissionCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE);

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(new Activity(), new String[]{Manifest.permission.READ_PHONE_STATE}, 100);
        } else {
            System.out.println("you got permissions");
            SubscriptionManager subscriptionManager = (SubscriptionManager) context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);

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





        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {


            //   runtimepermissions();


        }
        else {



        }
        return  map;

    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void sendAirtime(String receiver, String orderDetails, int value, Context context)
    {


        String airtimesReceiver = receiver;
        if (receiver.startsWith("+25")) {
            receiver = "0" + receiver.substring(4);
        }

        if (orderDetails.length() < 160 && orderDetails.length() > 8) {





            String networkProvider = Globals.networkProviderDeterminer(receiver);

            USSDCalls calls = new USSDCalls();

            Map simInfo = simsSubscriptionId(context);

            int subscriptionId;
            if (networkProvider.equalsIgnoreCase("safaricom")) {
                String ussd = "*140*" + value + "*" + receiver + "#";
                //calls.dialUssd(ussd,1,context);


                if (simInfo.containsKey("sim1")) {


                    if (simInfo.get("sim1").toString().equalsIgnoreCase("Safaricom") || simInfo.get("sim1").toString().contains("Safaricom")) {

//                                        if (android.os.Build.VERSION.SDK_INT < 26) {
                        String message = value + "#" + receiver;
                        subscriptionId = Integer.parseInt(simInfo.get("sim2SubscriptionId").toString());
                        sendSms(context, message, "140",true);

//                                        } else {
//
//
//                                            subscriptionId = Integer.parseInt(simInfo.get("sim1SubscriptionId").toString());
//                                            calls.dialUssd(ussd, subscriptionId, context, receiver, value, Integer.valueOf(amount), networkProvider);
//                                            System.out.println("I used this subscriptionid: " + subscriptionId);
//
//                                        }


                    }
                }
//                                if (simInfo.containsKey("sim2")) {
//
//                                    if (android.os.Build.VERSION.SDK_INT < 26) {
//                                        String message = value + "#" + receiver;
//                                        subscriptionId = Integer.parseInt(simInfo.get("sim2SubscriptionId").toString());
//                                        sendSms(context, message, subscriptionId, "140");
//
//                                    } else {
//                                        if (simInfo.get("sim2").toString().equalsIgnoreCase("Safaricom") || simInfo.get("sim2").toString().contains("Safaricom")) {
//
//                                            subscriptionId = Integer.parseInt(simInfo.get("sim2SubscriptionId").toString());
//                                            calls.dialUssd(ussd, subscriptionId, context, receiver, value, Integer.valueOf(amount), networkProvider);
//                                            System.out.println("I used this subscriptionid: " + subscriptionId);
//                                        }
//                                    }
//                                }

            }
            else if (networkProvider.equalsIgnoreCase("Airtel") || networkProvider.equalsIgnoreCase("Equitel")) {
                String ussd = "*140*" + value + "*" + receiver + "*1#";
                String message = "2u " + receiver + " " + value + " 0000";
//                                if (simInfo.containsKey("sim1")) {
//                                    System.out.println(simInfo);
//                                    if (simInfo.get("sim1").toString().equalsIgnoreCase("Airtel KE") || simInfo.get("sim1").toString().contains("Airtel KE")) {
//                                        subscriptionId = Integer.parseInt(simInfo.get("sim1SubscriptionId").toString());
//                                        sendSms(context, message, subscriptionId, "5050");
////
////                                    calls.dialUssd(ussd, subscriptionId, context,receiver,value,Integer.valueOf(amount),networkProvider);
//                                        //    System.out.println("I used this subscriptionid: " + subscriptionId);
//                                    }
//                                }
                if (simInfo.containsKey("sim2")) {
                    if (simInfo.get("sim2").toString().equalsIgnoreCase("Airtel") || simInfo.get("sim2").toString().contains("Airtel KE")) {

                        // subscriptionId = Integer.parseInt(simInfo.get("sim2SubscriptionId").toString());
                        subscriptionId = Integer.parseInt(simInfo.get("sim2SubscriptionId").toString());
                        sendSms(context, message, "5050",false);


//                                    calls.dialUssd(ussd, subscriptionId, context,receiver,value,Integer.valueOf(amount),networkProvider);
//                                    System.out.println("I used this subscriptionid: " + subscriptionId);
                    }
                }

            }
            else if (networkProvider.equalsIgnoreCase("Telkom")) {

                String ussd = "*140*" + value + "*" + receiver + "*1#";
//                            if (simInfo.containsKey("sim1")) {
//                                if (simInfo.get("sim1").toString().equalsIgnoreCase("Telkom") || simInfo.get("sim1").toString().contains("Telkom")) {
//
//                                    subscriptionId = Integer.parseInt(simInfo.get("sim1SubscriptionId").toString());
//                                    calls.dialUssd(ussd, subscriptionId, context,receiver,value,Integer.valueOf(amount),networkProvider);
//                                    System.out.println("I used this subscriptionid: " + subscriptionId);
//                                }
//                            }
                if (simInfo.containsKey("sim2")) {

                    if (simInfo.get("sim2").toString().equalsIgnoreCase("Telkom") || simInfo.get("sim2").toString().contains("Telkom")) {
                        subscriptionId = Integer.parseInt(simInfo.get("sim2SubscriptionId").toString());
                        subscriptionId = Integer.parseInt(simInfo.get("sim2SubscriptionId").toString());
                        calls.dialUssd(ussd, subscriptionId, context,receiver,value, Integer.valueOf(value),networkProvider);
                        System.out.println("I used this subscriptionid: " + subscriptionId);
                    }
                }
            }


        }


    }
    public void ToastShopClose(Context context)
    {
       // Toast.makeText(context,"Received Request But The Server Turned Off",Toast.LENGTH_LONG).show();
    }


    public void airtimeProcessor(Context context)
    {


        // Toast.makeText(context,"Received Request But The Server Turned Off",Toast.LENGTH_LONG).show();
    }
}
