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
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.google.gson.JsonArray;

import org.apache.xerces.impl.dv.util.Base64;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
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

    String plan="";
    String registrationId="";

    InitialConfigs configs;
    private RequestQueue mRequestQue;
    LipaRequest lipaRequest;
    DBHelper db ;
   static JsonObjectRequest request;
    SimpleDateFormat format=new SimpleDateFormat("yyyy-M-dd HH:mm:ss");
    private String URL = "https://fcm.googleapis.com/fcm/send";
String orderNumber="";
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {

        orderNumber = "";
        //if(configs.getServerId().equalsIgnoreCase("hdh"))
        if(lipaRequest==null)
        {
            lipaRequest = new LipaRequest();
        }
        if(db==null)
        {
            db = db.getInstance(this);
        }
        super.onMessageReceived(remoteMessage);



     if(configs==null)
     {
         configs = new InitialConfigs(this);
     }


        if(mRequestQue==null)
        {
            mRequestQue = Volley.newRequestQueue(getApplicationContext());
            System.err.println(" Created New Thread");

        }
        else{
            System.err.println("Didnt Create New Thread");
        }
       // FirebaseMessaging.getInstance().subscribeToTopic("all");
        Map<String, String> extraData = remoteMessage.getData();

        String networkPlan = extraData.get("network");
        String planrequeted = extraData.get("plan");
        registrationId = extraData.get("deviceId");


        if (!configs.getswitchState().equalsIgnoreCase("true")) {

            String messageToReply = "Sorry! We are experiencing technical difficulties, we'll return shortly. We apologise for any inconveniences this may cause.";

            sendConfirmationNotifcation(this, messageToReply, registrationId, rechargingPhone, null);


        }
        else {



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

            if(requestType.equalsIgnoreCase("Status")&& extraData.get("plan")!=null)
            {

                sendNotification(extraData.get("plan"),"Status Request",R.string.blackColor);

                if(configs.serverStatus.equalsIgnoreCase("true"))
                {
                    statusProcessor(extraData.get("plan"));

                }
                else{
                    Toast.makeText(this,"Received A Report But  Server  Status is Off Service Is Off",Toast.LENGTH_LONG).show();
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

                
                sendNotification(planrequeted, "Request Received", R.string.blackColor);
                String messageContent = planrequeted;
                System.err.println("App Format Accepted");
                orderNumber = IdGenerator.keyGen();
                System.err.println("Order Number:" + orderNumber);
                db = db.getInstance(this);



                String packageDetail[] = messageContent.split("#");
                String network = packageDetail[1];
                plan = packageDetail[0];

                rechargingPhone = packageDetail[2];
                payingPhone = packageDetail[3];
                if (packageDetail.length > 4) {
                    costfromClient = packageDetail[4];
                }
                else {

                    costfromClient = "";

                }
//                System.err.println("Pack Deatils" + messageContent);
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

                   // System.err.println("Is Agent");
                    savedPlanCost = db.getPlanCostDetails(plan, network).get("agentCost");
                    if (savedPlanCost.isEmpty()) {
                        savedPlanCost = db.getPlanCostDetails(plan, network).get("cost");
                    }

                }


                if (savedPlanCost.isEmpty() || savedPlanCost.equalsIgnoreCase("0")) {
                    if (db.getAllplans().size() > 0) {
                        db.registerPlanRequest(plan,requestType,paymentValue,Globals.networkProviderDeterminer(rechargingPhone),payingPhone,String.valueOf(new Date()),"Not received","Online",rechargingPhone,orderNumber,registrationId);
                        String messageToReply = "This Plan Is Currently Not Available,Ensure You have Refreshed The Packages in The Application, Or Please Contact The Admin For Assistance via :0703757633";

                        sendConfirmationNotifcation(this, messageToReply, registrationId, "Not Found", null);
                    }
                } else {


                    if (costfromClient.isEmpty()) {
                        //process Known Cost Packages like daily sms
                        if (configs.getswitchState().equalsIgnoreCase("true")) {
                            int cost = Integer.parseInt(savedPlanCost);
                            System.err.println("Cost Found" + cost);
                            paymentValue = cost;
                            db.registerPlanRequest(plan,requestType,paymentValue,Globals.networkProviderDeterminer(rechargingPhone),payingPhone,String.valueOf(new Date()),"Not received","Online",rechargingPhone,orderNumber,registrationId);
                            orderRequestProcessor(this);
                        } else {
                            if (configs.getServerStatus().equalsIgnoreCase("true")) {
                                String messageToReply = "Shop Closed Please Try Again Later,Opening Hours,Monday To Friday 8:00AM to 9:00PM Saturday And Sunday 9:00AM To 8:00PM.\n You Can Still Request Airtime Any Time And Buy The Package You Wish From You End";

                                sendConfirmationNotifcation(this, messageToReply, registrationId, "Shop Closed", null);

                            }

                        }

                    } else {


                        if (plan.equalsIgnoreCase("Airtime")) {
                            int percentageDiscount = Integer.parseInt(savedPlanCost);

                            // paymentValue=  ( ( (100-percentageDiscount)*Integer.parseInt(costfromClient))/100);
                            int airtime = Integer.parseInt(costfromClient);
                            int commission = (int) ((Double.valueOf(percentageDiscount) / 100) * airtime);

                            paymentValue = airtime - commission;
                            db.registerPlanRequest(plan,requestType,paymentValue,Globals.networkProviderDeterminer(rechargingPhone),payingPhone,String.valueOf(new Date()),"Not received","Online",rechargingPhone,orderNumber,registrationId);
                            orderRequestProcessor(this); //process
                            System.err.println("Retrieved Details:" + db.getRequestDetails(orderNumber));
                        } else {

                            if (configs.getswitchState().equalsIgnoreCase("true")) {
                                int percentageDiscount = Integer.parseInt(savedPlanCost);
                                //  paymentValue=  ( ( (100-percentageDiscount)*Integer.parseInt(costfromClient))/100);
                                int airtime = Integer.parseInt(costfromClient);
                                int commission = (int) ((Double.valueOf(percentageDiscount) / 100) * airtime);
                                paymentValue = airtime - commission;
                                db.registerPlanRequest(plan,requestType,paymentValue,Globals.networkProviderDeterminer(rechargingPhone),payingPhone,String.valueOf(new Date()),"Not received","Online",rechargingPhone,orderNumber,registrationId);
                                orderRequestProcessor(this);
                            } else {
                                if (configs.getServerStatus().equalsIgnoreCase("true")) {
                                    String messageToReply = "Shop Closed Please Try Again Later,Opening Hours,Monday To Friday 8:00AM to 9:00PM Saturday And Sunday 9:00AM To 8:00PM.\n You Can Still Request Airtime Any Time And Buy The Package You Wish From You End";

                                    sendConfirmationNotifcation(this, messageToReply, registrationId, "Shop Closed", null);

                                }

                            }

                        }


                    }


                }

//db.close();
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
                   // db.close();

                }


            }
        }


    }


    }


    private void statusProcessor(String messageContent)
    {
        
        String orderNumber="";
        if(messageContent.split("#").length>=2)
        {
            orderNumber=messageContent.split("#")[1];
        }
        Map packageDataMap=db.getFullOrderDetails(orderNumber);
        System.err.println("Order Details"+packageDataMap);
        if(packageDataMap.isEmpty())
        {
            packageDataMap=db.getRequestDetails(orderNumber);
            System.err.println("plan request Details"+packageDataMap);
            System.err.println("All plan request Details"+db.getAllRequestDetails());

            if(packageDataMap.isEmpty())
            {
                sendConfirmationNotifcation(this,"Order Not Found Please Check The Order Number",registrationId,"status",null);

            }
            else{

                String phone=packageDataMap.get("rechargePhone").toString();
                phone=phone.substring(0,5)+"xxxxx";
                String replyMessageToClient="Order; "+orderNumber+".\n\n"+"Item requested: "+packageDataMap.get("planName")+"\nFor "+phone+"\nSTATUS:"+packageDataMap.get("status")+" \nQuickSwap KE. ";

                sendConfirmationNotifcation(this,replyMessageToClient,registrationId,"status",null);



            }


        }
        else{
            String phone=packageDataMap.get("rechargePhone").toString();
            phone=phone.substring(0,5)+"xxxxx";
            String replyMessageToClient="Order "+orderNumber+".\n"+"\nItem requested: "+packageDataMap.get("planName")+"\nFor "+phone+"\nSTATUS:"+packageDataMap.get("status")+" \nQuickSwap KE. ";

            sendConfirmationNotifcation(this,replyMessageToClient,registrationId,"status",null);







        }
       // db.close();

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
//db.close();
    return response;
}

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
    public void orderRequestProcessor(Context context)
    {


        Map simInfo = simsSubscriptionId(context);

        int subscriptionId;
        String messageToReply ="Order Number: "+orderNumber+"\nEnter Your Mpesa Pin To Complete Purchase Of :"+plan+" for :"+rechargingPhone+"\n\nTo check your order status, Reply with "+"STATUS#"+orderNumber;
        subscriptionId = Integer.parseInt(simInfo.get("sim1SubscriptionId").toString());

        sendConfirmationNotifcation(context,messageToReply,registrationId,"Plan Request Confirmation",plan);
       sendSms(context, messageToReply,  rechargingPhone,true);

        if(payingPhone.equalsIgnoreCase(rechargingPhone.replace("+254","0")))
        {


        }
        else{

        }
      //  sendSms(context, messageToReply,  payingPhone,true);
//        Token token = new Token();
//        token.execute();
        authTok(this);

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





         request = new JsonObjectRequest(com.android.volley.Request.Method.POST, "https://api.lunar.cyou/api/packages.php",
                json,
                response -> {
                   String REQUESTTYPE="Plan Sent";
sendConfirmationNotifcation(context,message,deviceNumber,REQUESTTYPE,"");
                    Log.d("MUR", "onResponse: "+response);
                    


                }, error -> {
                    System.err.println(error);
                    Log.d("MUR", "onError............: "+error.networkResponse);
            
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


            JSONObject extraData = new JSONObject();
            extraData.put("message",message);
            extraData.put("plans","sent");
            extraData.put("title",requestType);
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
             request = new JsonObjectRequest(com.android.volley.Request.Method.POST, URL,
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



    @RequiresApi(api = Build.VERSION_CODES.M)
    public void paymentProcessor(String orderDetails, Context context)
    {
        
        System.err.println("haha1");
        String content[]=orderDetails.split("#");
        String plantype="";
        String costPrice="";

        String orderNumber="",name="";
        if(content.length>4)
        {
            orderNumber=content[4].replaceAll(" ","");
        }
        if(content.length>5)
        {
            name=content[5];
        }



        String receiver=content[2].replaceAll(" ",""),packageName=content[0].replaceFirst("TILL ", "");
        int amount= Double.valueOf(content[1]).intValue(), value;
        String mpesaCode=content[3];

        if(receiver.substring(0,3).equalsIgnoreCase("254"))
            receiver=receiver.replaceFirst("254","0");
        Map <String,String>  packageDetailsMap=db.getRequestDetails(orderNumber);
        payingPhone=packageDetailsMap.get("payingPhone");

        if (orderDetails.startsWith("TILL")) {
            if(content.length>4)
            {
                orderNumber=content[4];
            }
            if(content.length>5)
            {
                name=content[5];
            }

            orderDetails = orderDetails.replaceFirst("TILL ", "");


            if (receiver.startsWith("+25")) {
                receiver = "0" + receiver.substring(4);
            }
            plan=packageName;
            String network=Globals.networkProviderDeterminer(receiver);
            System.err.println(db.getPlanCostDetails(plan,Globals.networkProviderDeterminer(receiver)));

            String commm=db.getPlanCostDetails(plan,Globals.networkProviderDeterminer(receiver)).get("cost");
            String agentDetails=db.getAgentDetails(payingPhone).get("agentName");
            if(!agentDetails.isEmpty()&&db.getAgentDetails(payingPhone).get("status").equalsIgnoreCase("Approved"))
            {
                commm=db.getPlanCostDetails(plan,Globals.networkProviderDeterminer(receiver)).get("agentCost");


            }
            costPrice=db.getPlanCostDetails(plan,network).get("cost");
            plantype=db.getPlanCostDetails(plan,network).get("planType");
            Map   packageDataMap=db.getRequestDetails(orderNumber);
            System.err.println("Receiver:"+receiver+" plan:"+plan+" Amount:"+amount+" mpesa code:"+mpesaCode+"Commission:"+commm+" Network:"+Globals.networkProviderDeterminer(receiver));
            value = (amount * 100) / (100- Integer.valueOf(commm));

            db.registerOrder(packageName,"",value,Globals.networkProviderDeterminer(receiver),packageDataMap.get("payingPhone").toString(), String.valueOf(format.format(new Date())),orderNumber,"Online",mpesaCode,name,orderNumber,packageDataMap.get("rechargePhone").toString(),amount);
            System.err.println("Airtime Value:"+value+" Commision Used:"+commm);
            if(packageName.startsWith("A"))
            {
                if(Globals.networkProviderDeterminer(receiver).equalsIgnoreCase("Telkom")){



                    String message="Order Number; "+orderNumber+"\n"+"Buy "+Globals.networkProviderDeterminer(receiver)+" "+packageName+" For: "+receiver+" Worth KSH "+value+" MpesaCode: "+mpesaCode;
                    sendSms(context,message,context.getResources().getString(R.string.adminNumber),true);
                    db.updateOrderDetails(orderNumber,"status","Processing");
                    String replyMessageToClient="";
                    replyMessageToClient="Order; "+orderNumber+"\n"+packageName+" will be sent to "+receiver+" in 2-5 minutes. Kindly confirm your balances. Ref "+mpesaCode+"\n\n"+"Save more with us. Share with your family and friends.\n";
                    if(packageDataMap.get("deviceId")!=null && !packageDataMap.get("deviceId").toString().isEmpty())
                    {
                        sendConfirmationNotifcation(this,replyMessageToClient,packageDataMap.get("deviceId").toString(),orderNumber+" -Processing",replyMessageToClient);
                    }
                    if(packageDetailsMap.get("payingPhone").equals(packageDetailsMap.get("rechargePhone")))
                    {
                        sendSms(context,packageDetailsMap.get("payingPhone").toString(),replyMessageToClient,true);
                    }
                    else{
                        sendSms(context,replyMessageToClient,packageDetailsMap.get("rechargePhone").toString(),true);
                        sendSms(context,replyMessageToClient,packageDetailsMap.get("payingPhone").toString(),true);
                    }




                }
                else
                {
                    sendAirtime(receiver,orderDetails,value,this);
                    packageDataMap=db.getRequestDetails(orderNumber);
                    db.updateOrderDetails(orderNumber,"status","Complete.");


                    String replyMessageToClient="Order "+orderNumber+".\n"+plan+" sent. Kindly confirm your balances \n\n If Not Received, Kindly Contact Us On: "+context.getResources().getString(R.string.adminNumber);

                   if(packageDataMap.get("deviceId")!=null && !packageDataMap.get("deviceId").toString().isEmpty())
                   {
                       sendConfirmationNotifcation(this,replyMessageToClient,packageDataMap.get("deviceId").toString(),orderNumber+" -Completed",replyMessageToClient);
                   }
                    if(packageDataMap.get("payingPhone").equals(packageDataMap.get("rechargePhone")))
                    {
                        sendSms(context,replyMessageToClient,packageDataMap.get("rechargePhone").toString(),true);
                    }
                    else{
                        sendSms(context,replyMessageToClient,packageDataMap.get("rechargePhone").toString(),true);
                        sendSms(context,replyMessageToClient,packageDataMap.get("payingPhone").toString(),true);
                    }
                }





            }
            else{



               // Map   packageDetailsMap=db.getRequestDetails(orderNumber);

                if(plan.equalsIgnoreCase("No Expiry Call And SMS")||plan.equalsIgnoreCase("No Expiry Bundles"))
                {
                    value = (amount * 100) / (100- Integer.valueOf(commm));
                    String message="Order Number; "+orderNumber+"\n"+"Buy "+Globals.networkProviderDeterminer(receiver)+" PlanType: "+plantype+" "+packageName+" For: "+receiver+" Worth KSH "+value+" MpesaCode: "+mpesaCode;
                    sendSms(context,message,context.getResources().getString(R.string.adminNumber),true);
                    String replyMessageToClient="";
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    replyMessageToClient="Order; "+orderNumber+"\n"+packageDetailsMap.get("planName")+" will be sent to "+packageDetailsMap.get("rechargingPhone")+" in 2-5 minutes. Kindly confirm your balances. Ref "+mpesaCode+"\n"+"Save more with us. Share with your family and friends.\n";

                    if(packageDataMap.get("deviceId")!=null && !packageDataMap.get("deviceId").toString().isEmpty())
                    {
                        sendConfirmationNotifcation(this,replyMessageToClient,packageDataMap.get("deviceId").toString(),orderNumber+" -Processing",replyMessageToClient);
                    }
                    sendSms(context,replyMessageToClient,receiver,true);


                }
                else {

                    value = Integer.valueOf(db.getPlanCostDetails(plan,Globals.networkProviderDeterminer(receiver)).get("actualCost"));
                    String message="Order Number; "+orderNumber+"\n"+"Buy "+Globals.networkProviderDeterminer(receiver)+" "+packageDetailsMap.get("planType").toString()+" "+packageName+" For: "+receiver+" Worth KSH "+value+" MpesaCode: "+mpesaCode+" Amount Paid Ksh :"+amount+" Cost:Ksh "+costPrice;
                    sendSms(context,message,context.getResources().getString(R.string.adminNumber),true);
                    String replyMessageToClient="";
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    replyMessageToClient="Order; "+orderNumber+"\n"+packageDetailsMap.get("planName")+" will be sent to "+receiver+" in 2-5 minutes. Kindly confirm your balances. Ref "+mpesaCode+"\n"+"Save more with us. Share with your family and friends.\n";

                    if(packageDataMap.get("deviceId")!=null && !packageDataMap.get("deviceId").toString().isEmpty())
                    {
                        sendConfirmationNotifcation(this,replyMessageToClient,packageDataMap.get("deviceId").toString(),orderNumber+" -Processing",replyMessageToClient);
                    }
                    sendSms(context,replyMessageToClient,receiver,true);



                }
                db.updateOrderDetails(orderNumber,"status","Processing.");

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
      //      db.registerOrder(packageName,"",value,Globals.networkProviderDeterminer(receiver),receiver, String.valueOf(format.format(new Date())),"Un known","SMS",mpesaCode,paidorderNumber);
//            if(Globals.networkProviderDeterminer(receiver).equalsIgnoreCase("Telkom"))
//            {
//                String message="Buy "+Globals.networkProviderDeterminer(receiver)+" "+packageName+" For: "+receiver+" Worth KSH "+value+" Order Number: "+paidorderNumber;
//                sendSms(context,message,context.getResources().getString(R.string.telkomNumber),true);
//
//            }
//            else{
//                sendAirtime(receiver,orderDetails,value,context);
//            }
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
                    SmsManager sms = SmsManager.getDefault();
                    ArrayList<String> parts = sms.divideMessage(message);
                    SmsManager.getSmsManagerForSubscriptionId(simInfo1.getSubscriptionId()).sendMultipartTextMessage(destination, null, parts, null, null);


                }
                else{
                    //SendSMS From SIM Two
                    SmsManager sms = SmsManager.getDefault();
                    ArrayList<String> parts = sms.divideMessage(message);
                    SmsManager.getSmsManagerForSubscriptionId(simInfo2.getSubscriptionId()).sendMultipartTextMessage(destination, null, parts, null, null);


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
    public void authTok(Context context)
    {

        Gson gson=new Gson();
        JSONObject json = new JSONObject();
        try {
            String app_key = "yBIf50DIGeUu2dRzZHWVOurNj0nAtA19";
            String app_secret = "oadlZaBFbXCaEkzZ";
            String appKeySecret = app_key + ":" + app_secret;
            byte[] bytes = appKeySecret.getBytes("ISO-8859-1");
            String auth = Base64.encode(bytes);
            String url="https://api.safaricom.co.ke/oauth/v1/generate?grant_type=client_credentials";
            JSONArray array=new JSONArray();




             request = new JsonObjectRequest(Request.Method.GET, url,
                    null,
                    new com.android.volley.Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            AuthToken authToken;
                            String REQUESTTYPE="Plan Sent";
                            System.err.println("Resssss....."+response);
                            authToken = gson.fromJson(response.toString(), AuthToken.class);
                            issueSTK(authToken.getToken(),payingPhone,paymentValue,rechargingPhone,plan,registrationId,getResources().getString(R.string.clientFcmToken),getResources().getString(R.string.serverFcmKey),configs.getServerId(),orderNumber);



                            Log.d("MUR", "onResponse: "+response);
                        }
                    }, new com.android.volley.Response.ErrorListener() {
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
                    header.put("cache-control", "no-cache");
                    header.put("authorization", "Basic " + auth);
                    return header;
                }
            };
          

            mRequestQue.add(request);
        }
        catch (Exception e)

        {
            e.printStackTrace();
        }






    }





    public void issueSTK(String token, String PhoneNumber, double Amount, String rechargingPhone, String requestType,String deviceId,String fcmToken,String serverAdress,String serverId,String orderNumber)
    {
        if(PhoneNumber.startsWith("0"))
        {
            PhoneNumber=PhoneNumber.replaceFirst("0", "254");
        }
        if(PhoneNumber.startsWith("+254"))
        {

            PhoneNumber=PhoneNumber.substring(1);
        }
        Gson gson=new Gson();
        JSONObject json = new JSONObject();



        try {

            json.put("BusinessShortCode","7631176");
            json.put("Password","NzYzMTE3NmU4ZjIyM2U3OTBlNWIxMWVhMzliMjZiNjk2N2ExOGQzYzA5OGJiMjI3YjZiNWJiZDE0OWIyNDA5MTJlZGJhODUyMDE5MDIxNjE2NTYyNw==");
            json.put("Timestamp","20190216165627");
            json.put("TransactionType","CustomerBuyGoodsOnline");
            json.put("Amount",Amount);
            json.put("PartyA",PhoneNumber);
            json.put("PartyB","9587279");
            json.put("PhoneNumber",PhoneNumber);
            json.put("CallBackURL","https://api.lunar.cyou/api/lipacallback.php");
            json.put("AccountReference","Airtime");
            json.put("TransactionDesc","Purchase");

            String url="https://api.safaricom.co.ke/mpesa/stkpush/v1/processrequest";





             request = new JsonObjectRequest(Request.Method.POST, url,
                    json,
                    new com.android.volley.Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {

                            try {
                                if(response.get("ResponseCode").equals("0"))
                                {
                                   saveAirtimeRequest(rechargingPhone,Amount,response.getString("CheckoutRequestID"),response.getString("MerchantRequestID"),requestType,deviceId,fcmToken,serverAdress,serverId,orderNumber);

                                }
                                else {
                                    System.err.println("Error"+response.toString());

                                }
                            } catch (JSONException e) {


                                e.printStackTrace();
                            }


                            Log.d("MUR", "onResponse: "+response);
                            
                        }

                    }, new com.android.volley.Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    System.err.println(json);
                    // As of f605da3 the following should work
                    NetworkResponse response = error.networkResponse;
                    if (error instanceof ServerError && response != null) {
                        try {
                            String res = new String(response.data,
                                    HttpHeaderParser.parseCharset(response.headers, "utf-8"));
                            // Now you can use any deserializer to make sense of data
                            JSONObject obj = new JSONObject(res);
                            System.err.println(res);
                        } catch (UnsupportedEncodingException e1) {
                            // Couldn't properly decode data to string
                            e1.printStackTrace();
                        } catch (JSONException e2) {
                            // returned data is not JSONObject?
                            e2.printStackTrace();
                        }
                    }
                    
                }
            }
            ){
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> header = new HashMap<>();
                    header.put("content-type","application/json");

                    header.put("authorization", "Bearer " + token);
                    return header;
                }
            };
            request.setRetryPolicy(new DefaultRetryPolicy(20 * 1000, 5, 1.0f));

            mRequestQue.add(request);
        }
        catch (Exception e)

        {
            e.printStackTrace();
        }






    }

    public void saveAirtimeRequest(String phone, double amount, String checkoutRequestID, String merchantRequestID, String requestType,String deviceId,String fcmToken,String serverAdress,String serverId,String orderNumber)
    {

   try {

       JSONObject data = new JSONObject();

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
   int e=0;
       String url = "https://api.lunar.cyou/api/airtimerequest.php";

        request = new JsonObjectRequest(Request.Method.POST, url,
               data,
               new com.android.volley.Response.Listener<JSONObject>() {
                   @Override
                   public void onResponse(JSONObject response) {
                       AuthToken authToken;
                       String REQUESTTYPE="Plan Sent";
                       System.err.println("Resssss....."+response);
                       try {
                           if(response.get("responseCode").equals("200"))
                           {

                               System.err.println("sucess");

                           }
                           else {

                           }
                       } catch (JSONException e) {
                           e.printStackTrace();
                       }


                       Log.d("MUR", "onResponse: "+response);
                       
                   }
               }, new com.android.volley.Response.ErrorListener() {
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
       request.setRetryPolicy(new DefaultRetryPolicy(20 * 1000, 5, 1.0f));
       mRequestQue.add(request);


   }
   catch (Exception sq){

   }

    }
}
