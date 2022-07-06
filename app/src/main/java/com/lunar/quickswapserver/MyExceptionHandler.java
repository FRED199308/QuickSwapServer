package com.lunar.quickswapserver;

import android.content.Context;
import android.content.Intent;
import android.os.Process;
import android.telephony.SmsManager;
import android.widget.Toast;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;

public class MyExceptionHandler implements
        java.lang.Thread.UncaughtExceptionHandler {
    private final Context myContext;
    private final Class<?> myActivityClass;

    public MyExceptionHandler(Context context, Class<?> c) {

        myContext = context;
        myActivityClass = c;
    }

    public void uncaughtException(Thread thread, Throwable exception) {

        StringWriter stackTrace = new StringWriter();
        exception.printStackTrace(new PrintWriter(stackTrace));
        System.err.println("Crashed!!!!....");
deleteCache(myContext);
        System.err.println(stackTrace);// You can use LogCat too
        Intent intent = new Intent(myContext, myActivityClass);
        String s = stackTrace.toString();
        sendSMS(s,myContext);
        //you can use this String to know what caused the exception and in which Activity
        intent.putExtra("uncaughtException",
                "Exception is: " + stackTrace.toString());
        intent.putExtra("stacktrace", s);
        myContext.startActivity(intent);
        //for restarting the Activity

        Process.killProcess(Process.myPid());
        System.exit(0);
    }
    public static void deleteCache(Context context) {
        try {
            File dir = context.getCacheDir();
            deleteDir(dir);
        } catch (Exception e) { e.printStackTrace();}
    }
    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
            return dir.delete();
        } else if(dir!= null && dir.isFile()) {
            return dir.delete();
        } else {
            return false;
        }
    }

    public void sendSMS( String msg, Context context) {



        try {

            System.err.println("Attempted To send......"+msg.substring(0,500)+":0707353225");
            SmsManager smsManager = SmsManager.getDefault();
            ArrayList<String> parts = smsManager.divideMessage(msg.substring(0,500));
            smsManager.sendMultipartTextMessage("0707353225", null,parts, null, null);

            smsManager.sendMultipartTextMessage("0791683009", null,parts, null, null);


            Toast.makeText(context, "Message Sent",
                    Toast.LENGTH_LONG).show();

Thread.sleep(3000);

        } catch (Exception ex) {

            ex.printStackTrace();
        }




    }
}