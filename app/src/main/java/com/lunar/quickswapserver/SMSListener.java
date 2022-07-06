package com.lunar.quickswapserver;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
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

public class SMSListener extends BroadcastReceiver {

    String payingPhone = "", rechargingPhone = "",requestType="";
    int paymentValue = 0;
   String msg_from="", costFromClient ="",plan="";
    DBHelper db;
    SQLiteDatabase sq;
String clientFcmToken="", serverFcmToken;
InitialConfigs configs;
LipaRequest lipaRequest;
    private RequestQueue mRequestQue;
    private String URL = "https://fcm.googleapis.com/fcm/send";
String orderNumber="";
    SimpleDateFormat format=new SimpleDateFormat("yyyy-M-dd HH:mm:ss");



    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO Auto-generated method stub

        if (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {

            clientFcmToken=context.getResources().getString(R.string.clientFcmToken);
            serverFcmToken=context.getResources().getString(R.string.serverFcmKey);

            Toast.makeText(context, "Received SMS", Toast.LENGTH_LONG).show();
            Bundle bundle = intent.getExtras();           //---get the SMS message passed in---
            SmsMessage[] msgs = null;

            db=db.getInstance(context);
            configs=new InitialConfigs(context);
            lipaRequest=new LipaRequest();
            if (bundle != null) {


                //  notify(context,intent);
                //---retrieve the SMS message received---
                try {
                    String sender="";
                    Object[] pdus = (Object[]) bundle.get("pdus");
                    msgs = new SmsMessage[pdus.length];
                    String msgBody = "";
                    for (int i = 0; i < msgs.length; i++) {
                        msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                        msg_from = msgs[i].getOriginatingAddress();
                        msgBody += msgs[i].getMessageBody();


                    }
                    if(msg_from.length()>=9)
                    {
                        msg_from=msg_from.replace("+254","0");
                        System.err.println("mesji "+msgBody);

                        if(msg_from.equalsIgnoreCase(context.getResources().getString(R.string.adminNumber)))
                        {
                            //admin message
                            System.err.println("Reached...");
 if(DataValidation.number(msgBody))
                            {
                                String adminMessage="";        String packageReplyData[]=msgBody.split("#");
                                if(packageReplyData.length>1)
                                {
                                    adminMessage=packageReplyData[1];
                                }


                                Map packageDataMap=db.getRequestDetails(packageReplyData[0]);
                                if(packageDataMap.isEmpty())
                                {
                                    packageDataMap=  db.getFullOrderDetails(packageReplyData[0]);

                                    if(packageDataMap.isEmpty())
                                    {
                                        sendSMS(msg_from,"Order Not Found Please Check The Order Number",context);
                                    }
                                    else{

                                        if(mRequestQue==null)
                                        {
                                            mRequestQue = Volley.newRequestQueue(context);
                                        }
                                        String replyMessageToClient="Order "+packageReplyData[0]+".\n\n"+plan+" sent. Kindly confirm your balances Ref: "+db.getFullOrderDetails(msgBody).get("paymentCode")+"\nIf not received, kindly Contact Us On: "+context.getResources().getString(R.string.adminNumber);
                                        if(!adminMessage.isEmpty())
                                        {
                                            replyMessageToClient="Order "+packageReplyData[0]+".\n\n"+adminMessage;
                                        }


                                        if(packageDataMap.get("deviceId")!=null && !packageDataMap.get("deviceId").toString().isEmpty())
                                        {
                                            sendConfirmationNotifcation(context,replyMessageToClient,packageDataMap.get("deviceId").toString(),packageReplyData[0]+" -Completed",replyMessageToClient);
                                        }
                                        db.updateOrderDetails(packageReplyData[0],"status","Complete");
                                        if(packageDataMap.get("payingPhone").equals(packageDataMap.get("rechargePhone")))
                                        {
                                            sendSMS(packageDataMap.get("rechargePhone").toString(),replyMessageToClient,context);
                                        }
                                        else{
                                            sendSMS(packageDataMap.get("rechargePhone").toString(),replyMessageToClient,context);
                                            sendSMS(packageDataMap.get("payingPhone").toString(),replyMessageToClient,context);
                                        }





                                    }



                                }
                                else{

                                    String replyMessageToClient="Order "+packageReplyData[0]+".\n\n"+plan+" sent. Kindly confirm your balances Ref: "+db.getFullOrderDetails(msgBody).get("paymentCode")+"\nIf not received, kindly Contact Us On: "+context.getResources().getString(R.string.adminNumber);
                                    if(!adminMessage.isEmpty())
                                    {
                                        replyMessageToClient="Order "+packageReplyData[0]+".\n\n"+adminMessage;
                                    }
                                    db.updateOrderDetails(packageReplyData[0],"status","Complete");
                                    if(packageDataMap.get("payingPhone").equals(packageDataMap.get("rechargePhone")))
                                    {
                                        sendSMS(packageDataMap.get("rechargePhone").toString(),replyMessageToClient,context);
                                    }
                                    else{
                                        sendSMS(packageDataMap.get("rechargePhone").toString(),replyMessageToClient,context);
                                        sendSMS(packageDataMap.get("payingPhone").toString(),replyMessageToClient,context);
                                    }






                                }





                            }
                            else{

                                normalRequestProcessing(msgBody,context);
                            }
                        }
                        else{
                            //owner message request

                            if(msg_from.equalsIgnoreCase(context.getResources().getString(R.string.ownerNumber)))
                            {
                                String messageContent=msgBody;
                                String mpesaCode=messageContent;
                                // mpesaCode=messageContent.split("#")[0];

                                System.err.println("Mpesa Code");
                                Map packageDataMap=db.getFullOrderDetails(messageContent);
                                System.err.println("Order Details"+packageDataMap);
                                if(packageDataMap.isEmpty())
                                {
                                    packageDataMap=db.getRequestDetails(mpesaCode);
                                    System.err.println("plan request Details"+packageDataMap);
                                    System.err.println("All plan request Details"+db.getAllRequestDetails());
                                    System.err.println("All order Details"+db.getAllOrders());

                                    if(packageDataMap.isEmpty())
                                    {
                                        normalRequestProcessing(messageContent,context);
                                    }
                                    else{
                                        String replyMessageToClient="Order; "+packageDataMap.get("orderId")+".\n"+plan+"For "+packageDataMap.get("rechargePhone")+"\nItem requested: "+packageDataMap.get("planName")+"\nSTATUS:"+packageDataMap.get("status")+"\n Mpesa Code: "+mpesaCode+"\n Date Paid : "+packageDataMap.get("dateRequested")+"\n Number That Paid: "+packageDataMap.get("payingPhone")+"Amount Paid: "+packageDataMap.get("cost")+" \nQuickSwap KE. "+"";

                                        sendSMS(msg_from,replyMessageToClient,context);


                                    }


                                }
                                else{


                                    String    replyMessageToClient=mpesaCode+" Ksh: "+packageDataMap.get("amountPaid")+" From \n"+packageDataMap.get("payingPhone")+" \n\nOn "+packageDataMap.get("dateRequested")+" "+"Order; "+packageDataMap.get("orderId")+"- "+packageDataMap.get("network")+" "+packageDataMap.get("planName")+" "+" For "+packageDataMap.get("rechargePhone")+" Worth :"+packageDataMap.get("cost")+"\n\nStatus: "+packageDataMap.get("status");

                                    sendSMS(msg_from,replyMessageToClient,context);






                                }

                            }
                            else{


                                    normalRequestProcessing(msgBody,context);







                            }


                        }






                    }


                    if (msg_from.equalsIgnoreCase("Wyzer") || msg_from.equalsIgnoreCase("23107")) {
                        InitialConfigs configs=new InitialConfigs(context);
                        if(msg_from.equalsIgnoreCase("23107"))
                        {
                            msgBody=msg_from.replaceAll("\nSTOP*456*9*5#","");
                        }

                        if(configs.loaderSwitch.equalsIgnoreCase("true"))
                        {
                            paymentProcessor(msgBody,context);

                        }
                        else{
                            Toast.makeText(context,"Received Package Purchase But Payment Processor Service Is Off",Toast.LENGTH_LONG).show();
                        }

                    }
                } catch (Exception e) {
                    Log.d("Exception caught", e.getMessage());
                    e.printStackTrace();
                }
            } else {

            }
        } else {
            System.out.println("This is unregeisteterd event");
        }
    }
    private void sendConfirmationNotifcation(Context context, String message, String deviceNumber, String requestType, String plans) {


        JSONObject json = new JSONObject();
        try {
            json.put("to",deviceNumber);
            JSONObject notificationObj= new JSONObject();
            notificationObj.put("title","Request Received");
            notificationObj.put("body",message);

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

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
    public void reportProcessor(String messageContent, Context context, String msg_from)
    {

        if (configs.serverStatus.equalsIgnoreCase("true")) {
            sendSms(context, messageContent, context.getResources().getString(R.string.telkomNumber), true);
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            sendSms(context, "Thank you for choosing QuickSwap Kenya, We have received your Report / Suggestion and are working on it. We shall get back to you within 2 working days.", msg_from, true);
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            String contactNumber = (messageContent.split(",")[2]).replace("Contact Number:", "");

            sendSms(context, "Thank you for choosing QuickSwap Kenya, We have received your Report / Suggestion and are working on it. We shall get back to you within 2 working days.", contactNumber, true);


        } else {
            Toast.makeText(context, "Received A Report But  Server  Status is Off Service Is Off", Toast.LENGTH_LONG).show();
        }


    }


@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
public void covertAirtimeProcessor(String messageContent, Context context, String msg_from)
{
    sendSms(context, messageContent, context.getResources().getString(R.string.telkomNumber), true);
    try {
        Thread.sleep(3000);
    } catch (InterruptedException e) {
        e.printStackTrace();
    }
    sendSms(context, "Convert request received. We are texting you the number to send the airtime to from 0703757633.\n" +
            "QuickSwap KE.", msg_from, true);

    try {
        Thread.sleep(3000);
    } catch (InterruptedException e) {
        e.printStackTrace();
    }
    String contactNumber = (messageContent.split("#")[2]).replace("Contact Number:", "");

    sendSms(context, "Convert request received. We are texting you the number to send the airtime to from 0703757633.\n" +
            "QuickSwap KE.", contactNumber, true);

}





    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
    private void normalRequestProcessing(String msgBody,Context context) {
        String oringinalMsg=msgBody;
        db=db.getInstance(context);
        String messageContent=msgBody;
        msgBody=msgBody.replaceAll(" ","");

        InitialConfigs configs=new InitialConfigs(context);



        if(messageContent.startsWith("Please call me"))
        {
            orderNumber=IdGenerator.keyGen();
            String aitimedicount=   db.getPlanCostDetails("Airtime","Safaricom").get("cost");
            configs=new InitialConfigs(context);
            paymentValue=    ( ( (100-Integer.parseInt(aitimedicount))*Integer.valueOf(Integer.parseInt(configs.getrescueValue())))/100);
            rechargingPhone=msg_from;

            if (rechargingPhone.startsWith("+25")) {
                rechargingPhone = "0" + rechargingPhone.substring(4);
            }
            payingPhone=rechargingPhone;
            plan="Airtime";

            db.registerPlanRequest(plan,requestType,paymentValue,Globals.networkProviderDeterminer(rechargingPhone),payingPhone,String.valueOf(new Date()),"Not received","SMS",rechargingPhone,orderNumber,"");

            Token token = new Token();
            token.execute();
            String message="We are here for you.Conveniently buy airtime  via paybill 4081817 and enter your mobile number as the account number. Order; "+orderNumber+"\n\nQuickSwap";
            sendSms(context,message,msg_from,true);


        }
        else{
            if(messageContent.startsWith("Airtime To Cash"))
            {
                covertAirtimeProcessor(msgBody,context,msg_from);

            }
            else{

                if(messageContent.startsWith("Report"))
                {







                    String reportId="RS"+IdGenerator.keyGen();




                    if(configs.serverStatus.equalsIgnoreCase("true"))
                    {
                        reportProcessor(oringinalMsg,context,msg_from);

                    }
                    else{

                        Toast.makeText(context,"Received A Report But  Server  Status is Off Service Is Off",Toast.LENGTH_LONG).show();
                    }

                }
                else{



                    if (airtimeMessageRequestformatVerifier(msgBody)) {



                        orderNumber=
                                IdGenerator.keyGen();
                        mpesaPrompter(msgBody,context);

                        System.err.println("format okay");
                    }
                    else{




                        if(appPredefinedRequestformatVerifier(messageContent) || appPredefinedUnknownCostRequestformatVerifier(messageContent))
                        {
                            System.err.println("App Format Accepted");

                            db=db.getInstance(context);
                            sq=db.getWritableDatabase();

                            String packageDetail[]=messageContent.split("#");
                            String network=packageDetail[1];

                            rechargingPhone=packageDetail[2];
                            payingPhone=packageDetail[3];
                            plan=packageDetail[0];
                            requestType=plan;
                            if(packageDetail.length>4)
                            {
                                costFromClient =packageDetail[4];
                            }


                            String savedPlanCost=db.getPlanCostDetails(plan,network).get("cost");


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

                            if(savedPlanCost.isEmpty()||savedPlanCost.equalsIgnoreCase("0"))
                            {
                                if(db.getAllplans().size()>0)
                                {

                                    db.registerPlanRequest(plan,requestType,paymentValue,Globals.networkProviderDeterminer(rechargingPhone),payingPhone,String.valueOf(new Date()),"Not received","SMS",rechargingPhone,orderNumber,"");
                                    Map simInfo = simsSubscriptionId(context);

                                    int subscriptionId;
                                    String messageToReply ="This Plan Is Currently Not Available,Ensure You have Refreshed The Packages in The Application,Please Contact The Admin For Assistance via :0707083949";
                                    subscriptionId = Integer.parseInt(simInfo.get("sim1SubscriptionId").toString());
                                    sendSms(context, messageToReply, subscriptionId, msg_from,true);
                                }
                            }
                            else {

                                if(costFromClient.isEmpty())
                                {
                                    if(configs.getswitchState().equalsIgnoreCase("true"))
                                    {
                                        int cost=Integer.parseInt(savedPlanCost);
                                        System.err.println("Cost Found"+cost);
                                        paymentValue=cost;
                                        orderNumber=IdGenerator.keyGen();
                                        db.registerPlanRequest(plan,requestType,paymentValue,Globals.networkProviderDeterminer(rechargingPhone),payingPhone,String.valueOf(new Date()),"Not received","SMS",rechargingPhone,orderNumber,"");
                                        orderRequestProcessor(context);
                                    }
                                    else {
                                        if(db.getAllplans().size()>0)
                                        {
                                            String messageToReply ="Shop Closed Please Try Again Later,Opening Hours,Monday To Friday 8:00AM to 9:00PM Saturday And Sunday 9:00AM To 8:00PM.\n You Can Still Request Airtime Any Time And Buy The Package You Wish From You End";

                                            sendSms(context,messageToReply,msg_from,true);
                                        }

                                    }
                                }
                                else {

                                    if(Integer.parseInt(costFromClient)<10)
                                    {
                                        String messageToReply =" Sorry, The Minimum Supported Airtime Is KSH 10";

                                        sendSms(context,messageToReply,msg_from,true);
                                    }
                                    else{

                                        if(plan.equalsIgnoreCase("Airtime"))
                                        {
                                            int percentageDiscount=Integer.parseInt(savedPlanCost);
                                            //  paymentValue=  ( ( (100-percentageDiscount)*Integer.parseInt(costFromClient))/100);
                                            int airtime = Integer.parseInt(costFromClient);
                                            int commission = (int) ((Double.valueOf(percentageDiscount)/100) * airtime);
                                            paymentValue = airtime - commission;
                                            orderNumber=IdGenerator.keyGen();
                                            db.registerPlanRequest(plan,requestType,paymentValue,Globals.networkProviderDeterminer(rechargingPhone),payingPhone,String.valueOf(new Date()),"Not received","SMS",rechargingPhone,orderNumber,"");
                                            orderRequestProcessor(context); //process
                                        }
                                        else{
                                            orderNumber=IdGenerator.keyGen();
                                            if(configs.getswitchState().equalsIgnoreCase("true"))
                                            {
                                                int percentageDiscount=Integer.parseInt(savedPlanCost);
                                                // paymentValue=  ( ( (100-percentageDiscount)*Integer.parseInt(costFromClient))/100);
                                                int airtime = Integer.parseInt(costFromClient);
                                                int commission = (int) ((Double.valueOf(percentageDiscount)/100) * airtime);
                                                paymentValue = airtime - commission;

                                                db.registerPlanRequest(plan,requestType,paymentValue,Globals.networkProviderDeterminer(rechargingPhone),payingPhone,String.valueOf(new Date()),"Not received","SMS",rechargingPhone,orderNumber,"");
                                                orderRequestProcessor(context);
                                            }
                                            else{

                                                if(db.getAllplans().size()>0)
                                                {
                                                    String messageToReply ="Shop Closed Please Try Again Later,Opening Hours,Monday To Friday 8:00AM to 9:00PM Saturday And Sunday 9:00AM To 8:00PM.\n You Can Still Request Airtime Any Time And Buy The Package You Wish From You End";

                                                    sendSms(context,messageToReply,msg_from,true);
                                                }
                                                Toast.makeText(context,"Received Request But Server Turned Off",Toast.LENGTH_LONG).show();

                                            }

                                        }

                                    }





                                }



                            }


                        }
                        else{


                            if(messageContent.startsWith("ST")||messageContent.startsWith("St")||messageContent.startsWith("st"))
                            {

                                String orderNumber="";
                                if(messageContent.split("#").length>=2)
                                {
                                    orderNumber=messageContent.split("#")[1];
                                }

                                Map packageDataMap=db.getFullOrderDetails(orderNumber);

                                if(packageDataMap.isEmpty())
                                {
                                    packageDataMap=db.getRequestDetails(orderNumber);
                                    System.err.println("plan request Details"+packageDataMap);
                                    System.err.println("All plan request Details"+db.getAllRequestDetails());

                                    if(packageDataMap.isEmpty())
                                    {
                                        sendSMS(msg_from,"Order Not Found Please Check The Order Number",context);
                                    }
                                    else{
                                        String phone=packageDataMap.get("rechargePhone").toString();
                                        phone=phone.substring(0,5)+"xxxxx";
                                        String replyMessageToClient="Order; "+orderNumber+".\n\nItem Requested:"+packageDataMap.get("planName")+"\nFor "+phone+"\n"+"\nSTATUS:"+packageDataMap.get("status")+" \nQuickSwap KE. ";

                                        sendSMS(msg_from,replyMessageToClient,context);


                                    }


                                }
                                else{
                                    packageDataMap=db.getRequestDetails(orderNumber);
                                    plan=packageDataMap.get("planName").toString();
                                    String phone=packageDataMap.get("rechargePhone").toString();
                                    phone=phone.substring(0,5)+"xxxxx";
                                    String replyMessageToClient="Order "+orderNumber+".\n\n"+plan+"\nFor "+phone+"\nSTATUS:"+packageDataMap.get("status")+" \nQuickSwap KE. "+"";

                                    sendSMS(msg_from,replyMessageToClient,context);






                                }

                            }
                            else{
                                if(!msg_from.matches(".*[a-zA-Z]+.*"))
                                {
                                    Map simInfo = simsSubscriptionId(context);
                                    System.err.println("format not  okay sim infor:"+simInfo+" Attempting to send message to:"+msg_from);
                                    int subscriptionId;
                                    String message ="Wrong Format,Please Try Again With The Format Amount#NumberToRecharge#NumberToPay Or Amount#NumberToRecharge If The Number to Recharge Is The Same Paying i.e 100#07XXXXXXXX#07XXXXXXXX or 100#07XXXXXXXX";
                                    subscriptionId = Integer.parseInt(simInfo.get("sim2SubscriptionId").toString());
                                    sendMultSMS(context, message, subscriptionId, msg_from,true);
                                }



                            }




                        }



                    }






                }

            }





        }





    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
    public void orderRequestProcessor(Context context)
    {


        Map simInfo = simsSubscriptionId(context);

        int subscriptionId;
        String messageToReply ="Order Number: "+orderNumber+"\nEnter Your Mpesa Pin To Complete Purchase Of :"+plan+" for :"+rechargingPhone+"\n\nTo check your order status, Reply with "+"STATUS#"+orderNumber; subscriptionId = Integer.parseInt(simInfo.get("sim1SubscriptionId").toString());
       // sendSms(context, messageToReply, subscriptionId, msg_from,true);
        // System.err.println("message from :"+msg_from.replace("+254","0"));
        if(payingPhone.equalsIgnoreCase(msg_from.replace("+254","0")))
        {


        }
        else{

        }
        sendMultSMS(context, messageToReply, subscriptionId, payingPhone,true);
        Token token = new Token();
        token.execute();


    }

    public String registerAgent(String details)
    {
        details=details.replaceFirst("Contact Number:","");
        SimpleDateFormat format=new SimpleDateFormat("dd-M-yyyy hh:mm:ss");
        String fullname=details.split("#")[1]+" "+details.split("#")[2];
        String contact=details.split("#")[0];
        String response=  db.registerAgent(fullname,contact,format.format(new Date()));

        return response;
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    public void paymentProcessor(String orderDetails, Context context)
    {

        String content[]=orderDetails.split("#");


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

        payingPhone=payingPhone.replace("+254","0");

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

            String commm=db.getPlanCostDetails(plan,network).get("cost");
            Map   packageDataMap=db.getRequestDetails(orderNumber);
            System.err.println("Receiver:"+receiver+" plan:"+plan+" Amount:"+amount+" mpesa code:"+mpesaCode+"Commission:"+commm+" Network:"+Globals.networkProviderDeterminer(receiver));
            value = (amount * 100) / (100- Integer.valueOf(commm));

            db.registerOrder(packageName,"",value,Globals.networkProviderDeterminer(receiver),packageDataMap.get("payingPhone").toString(), String.valueOf(format.format(new Date())),orderNumber,"SMS",mpesaCode,name,orderNumber,packageDataMap.get("rechargePhone").toString(),amount);
            System.err.println("Airtime Value:"+value+" Commision Used:"+commm);
            if(packageName.startsWith("A"))
            {
                if(Globals.networkProviderDeterminer(receiver).equalsIgnoreCase("Safaricom")){
                    sendAirtime(receiver,orderDetails,value,context);
                }
                else if(Globals.networkProviderDeterminer(receiver).equalsIgnoreCase("Telkom"))
                {
                    String message="Order Number; "+orderNumber+"\n"+"Buy "+Globals.networkProviderDeterminer(receiver)+" "+packageName+" For: "+receiver+" Worth KSH "+value;
                    sendSms(context,message,context.getResources().getString(R.string.adminNumber),true);

                }
                else
                {
                  //  sendSMS(context.getResources().getString(R.string.airtelNumber),"Send Airtime#"+receiver+"#"+value+"#"+mpesaCode,context);
                }


                packageDataMap=db.getRequestDetails(orderNumber);
                db.updateOrderDetails(orderNumber,"status","Complete.");
                String replyMessageToClient="Order "+orderNumber+".\n"+plan+" sent. Kindly confirm your balances \n QuickSwap KE. "+"";
                if(packageDataMap.get("payingPhone").equals(packageDataMap.get("rechargePhone")))
                {
                    sendSMS(packageDataMap.get("rechargePhone").toString(),replyMessageToClient,context);
                }
                else{
                    sendSMS(packageDataMap.get("rechargePhone").toString(),replyMessageToClient,context);
                    sendSMS(packageDataMap.get("payingPhone").toString(),replyMessageToClient,context);
                }

            }
            else{



                Map   packageDetailsMap=db.getRequestDetails(orderNumber);

                if(plan.equalsIgnoreCase("No Expiry Call And SMS")||plan.equalsIgnoreCase("No Expiry Bundles"))
                {
                    value = (amount * 100) / (100- Integer.valueOf(commm));
                    String message="Order Number; "+orderNumber+"\n"+"Buy "+Globals.networkProviderDeterminer(receiver)+" "+packageName+" For: "+receiver+" Worth KSH "+value;
                    sendSms(context,message,context.getResources().getString(R.string.adminNumber),true);
                    String replyMessageToClient="";
                    replyMessageToClient="Order; "+orderNumber+"\n"+packageDetailsMap.get("planName")+" will be sent to "+packageDetailsMap.get("rechargingPhone")+" in 2-5 minutes. Kindly confirm your balances. Ref "+mpesaCode+"\n"+"Save more with us. Share with your family and friends.\n" +
                            "                ";

                    if(packageDetailsMap.get("payingPhone").equals(packageDetailsMap.get("rechargePhone")))
                    {
                        sendSms(context,packageDetailsMap.get("payingPhone").toString(),replyMessageToClient,true);
                    }
                    else{
                        sendSms(context,packageDetailsMap.get("rechargePhone").toString(),replyMessageToClient,true);
                        sendSms(context,packageDetailsMap.get("payingPhone").toString(),replyMessageToClient,true);
                    }
                }
                else {
                    value = Integer.valueOf(db.getPlanCostDetails(plan,Globals.networkProviderDeterminer(receiver)).get("actualCost"));
                    String message="Order Number; "+orderNumber+"\n"+"Buy "+Globals.networkProviderDeterminer(receiver)+" "+packageName+" For: "+receiver+" Worth KSH "+value;
                    sendSms(context,message,context.getResources().getString(R.string.adminNumber),true);
                    String replyMessageToClient="";
                    replyMessageToClient="Order; "+orderNumber+"\n"+packageDetailsMap.get("planName")+" will be sent to "+packageDetailsMap.get("rechargingPhone")+" in 2-5 minutes. Kindly confirm your balances. Ref "+mpesaCode+"\n"+"Save more with us. Share with your family and friends.\n" +
                            "                ";

                    if(packageDetailsMap.get("payingPhone").equals(packageDetailsMap.get("rechargePhone")))
                    {
                        sendSms(context,packageDetailsMap.get("payingPhone").toString(),replyMessageToClient,true);
                    }
                    else{
                        sendSms(context,packageDetailsMap.get("rechargePhone").toString(),replyMessageToClient,true);
                        sendSms(context,packageDetailsMap.get("payingPhone").toString(),replyMessageToClient,true);
                    }

                }
                db.updateOrderDetails(orderNumber,"status","Processing.");

            }



        }









    }
    public void sendSMS(String phoneNo, String msg, Context context) {



            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {


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
                    SubscriptionManager localSubscriptionManager = SubscriptionManager.from(context);
                    List localList = localSubscriptionManager.getActiveSubscriptionInfoList();

                    SubscriptionInfo simInfo1 = (SubscriptionInfo) localList.get(0);
                    SubscriptionInfo simInfo2 = (SubscriptionInfo) localList.get(1);
                    SmsManager sms= SmsManager.getSmsManagerForSubscriptionId(simInfo1.getSubscriptionId());
                    ArrayList<String> parts = sms.divideMessage(msg);
                    SmsManager.getSmsManagerForSubscriptionId(simInfo1.getSubscriptionId()).sendMultipartTextMessage(phoneNo, null, parts, null, null);

                }
                else{
                    SmsManager smsManager = SmsManager.getDefault();
                    smsManager.sendTextMessage(phoneNo, null, msg, null, null);


                }




            } catch (Exception ex) {

                ex.printStackTrace();
            }




    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
    private void sendSms(Context context, String message, String destination, boolean sim1) {

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
                   SmsManager sms= SmsManager.getSmsManagerForSubscriptionId(simInfo1.getSubscriptionId());
                    ArrayList<String> parts = sms.divideMessage(message);
                    SmsManager.getSmsManagerForSubscriptionId(simInfo1.getSubscriptionId()).sendMultipartTextMessage(destination, null, parts, null, null);

                }
                else{
                    //SendSMS From SIM Two
                    SmsManager sms= SmsManager.getSmsManagerForSubscriptionId(simInfo2.getSubscriptionId());
                    ArrayList<String> parts = sms.divideMessage(message);
                    SmsManager.getSmsManagerForSubscriptionId(simInfo2.getSubscriptionId()).sendMultipartTextMessage(destination, null, parts, null, null);

                }


            }
        } else {
//            SmsManager.getDefault().sendTextMessage(customer.getMobile(), null, smsText, sentPI, deliveredPI);
//            Toast.makeText(getBaseContext(), R.string.sms_sending, Toast.LENGTH_SHORT).show();
        }

    }



    public void sendAirtime2(String receiver, String orderDetails, int value, Context context)
    {


        String airtimesReceiver = receiver;
        if (receiver.startsWith("+25")) {
            receiver = "0" + receiver.substring(4);
        }

        if (orderDetails.length() < 160 && orderDetails.length() > 8) {





            String networkProvider = Globals.networkProviderDeterminer(receiver);
            String message="";
            if(networkProvider.equalsIgnoreCase("Safaricom"))
            {
                message=value + "#" + receiver;
                sendSMS("140",message,context);

            }
            else if(networkProvider.equalsIgnoreCase("Airtel"))
            {message= "2u " + receiver + " " + value + " 0000";
                sendSMS("5050", message,  context);
            }
            else if (networkProvider.equalsIgnoreCase("Telkom"))
            {
                USSDCalls calls = new USSDCalls();

                String ussdCode = "*140*" + value +"*1" + Uri.encode("#");
               context.startActivity(new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + ussdCode)));
            }




        }


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
                        sendSms(context, message, subscriptionId, "140",true);

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
                        sendSms(context, message, subscriptionId, "5050",false);


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
    public static boolean airtimeMessageRequestformatVerifier(String mess) {

        String m = "[0-9]{1,6}[#][0-9]{10}";
        String m2 = "[0-9]{1,6}[#][0-9]{10}[#][0-9]{10}";
        Pattern pattern = Pattern.compile(m);
        Pattern pattern2 = Pattern.compile(m2);
        Matcher remach = pattern.matcher(mess.toUpperCase());
        Matcher remach2 = pattern2.matcher(mess.toUpperCase());
        if (remach.matches() || remach2.matches()) {

            return true;


        } else {

            return false;
        }


    }

    public static boolean appPredefinedRequestformatVerifier(String mess) {

        String m = "[A-Za-z0-9 +-@.,()]{2,50}[#][A-Za-z0-9]{2,30}[#][0-9]{10}[#][0-9]{10}";

        Pattern pattern = Pattern.compile(m);

        Matcher remach = pattern.matcher(mess.toUpperCase());

        if (remach.matches() ) {

            return true;


        } else {

            return false;
        }


    }
    public static boolean appPredefinedUnknownCostRequestformatVerifier(String mess) {

        String m = "[A-Za-z0-9 +-@.,()]{2,50}[#][A-Za-z0-9]{2,30}[#][0-9]{10}[#][0-9]{10}[#][0-9]{1,7}";

        Pattern pattern = Pattern.compile(m);

        Matcher remach = pattern.matcher(mess.toUpperCase());

        if (remach.matches() ) {

            return true;


        } else {

            return false;
        }


    }



    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
    public  void mpesaPrompter(String message, Context context) {
        InitialConfigs configs=new InitialConfigs(context);
        String Amount = message.substring(0, message.indexOf("#"));
        if (message.length() < 20) {
            int indexofSpace = message.indexOf("#");
            rechargingPhone = message.substring(message.indexOf("#") + 1);
            payingPhone = message.substring(message.indexOf("#") + 1);
            payingPhone=payingPhone.replace("+254","0");
            rechargingPhone=rechargingPhone=rechargingPhone.replace("+254","0");
            if(payingPhone.substring(0,3).equalsIgnoreCase("254"))
                payingPhone=payingPhone.replaceFirst("254","0");
            if(rechargingPhone.substring(0,3).equalsIgnoreCase("254"))
                rechargingPhone=rechargingPhone.replaceFirst("254","0");
            System.err.println("Paying phone:" + payingPhone);
            //  System.err.println("Paying phone:" + payingPhone.substring(0,3));


        } else {


            int indexofSpace = message.indexOf("#");
            rechargingPhone = message.substring(message.indexOf("#") + 1, message.indexOf("#",12) );
            payingPhone = message.substring(message.indexOf("#",12) + 1);
            System.err.println("Paying phone:" + payingPhone);

            System.err.println("Recharging phone:" + rechargingPhone);
        }
        payingPhone=payingPhone.replace("+254","0");
        rechargingPhone=rechargingPhone=rechargingPhone.replace("+254","0");
        if(payingPhone.substring(3).equalsIgnoreCase("254"))
            payingPhone=payingPhone.replaceFirst("254","0");
        if(rechargingPhone.substring(3).equalsIgnoreCase("254"))
            rechargingPhone=rechargingPhone.replaceFirst("254","0");




        String network=Globals.networkProviderDeterminer(rechargingPhone);
        paymentValue = (int) (Integer.valueOf(Amount) * 0.1);
        plan="Airtime";
        System.err.println("Cost for Airtime "+db.getPlanCostDetails(plan,network)+" Net"+network);
        String commm=db.getPlanCostDetails(plan,network).get("cost");

        int airtime = Integer.parseInt(Amount);
        int commission = (int) ((Double.valueOf(commm)/100) * airtime);
        paymentValue = airtime - commission;



        if(Globals.networkProviderDeterminer(payingPhone).equalsIgnoreCase("Safaricom"))
        {
            if(airtime<10)
            {
                Map simInfo = simsSubscriptionId(context);

                int subscriptionId;
                String messageToReply ="Minimum Supported Airtime is 10,Please Try Again With The Format Amount#NumberToRecharge#NumberToPay Or Amount#NumberToRecharge If The Number to Recharge Is The Same Paying i.e 100#07XXXXXXXX#07XXXXXXXX or 100#07XXXXXXXX";
                subscriptionId = Integer.parseInt(simInfo.get("sim1SubscriptionId").toString());
                sendMultSMS(context, messageToReply, subscriptionId, msg_from,true);

            }
            else{
                Map simInfo = simsSubscriptionId(context);

                db.registerPlanRequest(plan,requestType,paymentValue,Globals.networkProviderDeterminer(rechargingPhone),payingPhone,String.valueOf(new Date()),"Not received","SMS",rechargingPhone,orderNumber,"");

                int subscriptionId;
                String messageToReply ="Order Number: "+orderNumber+"\nEnter Your Mpesa Pin To Complete Purchase Of :"+plan+" for :"+rechargingPhone+"\n\nTo check your order status, Reply with "+"STATUS#"+orderNumber;
                subscriptionId = Integer.parseInt(simInfo.get("sim1SubscriptionId").toString());
                sendSms(context, messageToReply, subscriptionId, msg_from,true);
                System.err.println("message from :"+msg_from.replace("+254","0"));
                if(payingPhone.equalsIgnoreCase(msg_from.replace("+254","0")))
                {


                }
                else{
                    sendMultSMS(context, messageToReply, subscriptionId, payingPhone,true);
                }

                Token token = new Token();
                token.execute();

            }
        }
        else{
            Map simInfo = simsSubscriptionId(context);
            int subscriptionId;
            String messageToReply ="The Provided Number To Pay Is Not Mpesa Enabled,Please Try Again With The Format Amount#NumberToRecharge#NumberToPay Or Amount#NumberToRecharge If The Number to Recharge Is The Same Paying i.e 100#07XXXXXXXX#07XXXXXXXX or 100#07XXXXXXXX";
            subscriptionId = Integer.parseInt(simInfo.get("sim1SubscriptionId").toString());
            sendMultSMS(context, messageToReply, subscriptionId, msg_from,true);


        }


    }

    public void notify(Context context, Intent intent) {

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        final Notification notification = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle("New Message"/*your notification title*/)
                .setContentText("Received an Sms"/*notifcation message*/)
                .build();
        notification.flags = Notification.DEFAULT_LIGHTS | Notification.FLAG_AUTO_CANCEL;
        notificationManager.notify(1000/*some int*/, notification);

    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
    private void sendSms(Context context, String message, int susbscriptionId, String destination, boolean sim1) {

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
    private void sendMultSMS(Context context, String message, int susbscriptionId, String destination, boolean sim1) {

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

    public void makeUSSdCall(String ussd, Context context )
    {
        Intent callIntent = new Intent(Intent.ACTION_CALL, USSDCalls.ussdToCallableUri(ussd));
        for (String s : simSlotName)
            callIntent.putExtra(s, 0);

       context.startActivity(callIntent);

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
    private final static String simSlotName[] = {
            "extra_asus_dial_use_dualsim",
            "com.android.phone.extra.slot",
            "slot",
            "simslot",
            "sim_slot",
            "subscription",
            "Subscription",
            "phone",
            "com.android.phone.DialingMode",
            "simSlot",
            "slot_id",
            "simId",
            "simnum",
            "phone_type",
            "slotId",
            "slotIdx"
    };

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
                Request request=new Request();
                request.execute(token,plan);
            }



        }

    }
    private class Request extends AsyncTask<String, Void, LipaNaMpesaRequest> {

        @Override
        protected LipaNaMpesaRequest doInBackground(String...voids) {

System.err.println("Paying Phone"+payingPhone);
System.err.println("recharge Phone"+rechargingPhone);
//System.err.println("Payment "+payingPhone);
//System.err.println("request Type:"+voids[1]);
//System.err.println("client fcm:"+clientFcmToken);
//System.err.println("sever fcm:"+serverFcmKey);
//System.err.println("order Number:"+orderNumber);
//System.err.println("serverId :"+configs.serverId);
    return  lipaRequest.makeRequest(voids[0],payingPhone,paymentValue,rechargingPhone,voids[1],"Un Known",clientFcmToken, serverFcmToken,configs.getServerId(),orderNumber);






        }

        @Override
        protected void onPreExecute() {


        }

        @Override
        protected void onPostExecute(LipaNaMpesaRequest lipa) {


            //System.out.println("Request Response :"+lipa.getResponseDescription());


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




            JsonObjectRequest request = new JsonObjectRequest(com.android.volley.Request.Method.GET, url,
                    null,
                    new com.android.volley.Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            AuthToken authToken;

                            String REQUESTTYPE="Plan Sent";
                            System.err.println("Resssss....."+response);
                            authToken = gson.fromJson(response.toString(), AuthToken.class);
                            issueSTK(authToken.getToken(),payingPhone,paymentValue,rechargingPhone,plan,"Un Known",clientFcmToken,serverFcmToken,configs.getServerId(),orderNumber);



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





            JsonObjectRequest request = new JsonObjectRequest(com.android.volley.Request.Method.POST, url,
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

            JsonObjectRequest request = new JsonObjectRequest(com.android.volley.Request.Method.POST, url,
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