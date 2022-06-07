package com.lunar.quickswapserver;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ReadInboxSMS {

    public ReadInboxSMS(Context context)
    {
//        // public static final String INBOX = "content://sms/inbox";
//// public static final String SENT = "content://sms/sent";
//// public static final String DRAFT = "content://sms/draft";
//        Cursor cursor = context.getContentResolver().query(Uri.parse("content://sms/inbox"), null, null, null, null);
//
//        if (cursor.moveToFirst()) { // must check the result to prevent exception
//            do {
//                String msgData = "",sender;
//                for(int idx=0;idx<cursor.getCount();idx++)
//                {
//                    if (cursor.getString(cursor.getColumnIndexOrThrow("type")).contains("1")) {
//                        msgData += " " + cursor.getColumnIndexOrThrow("body")+ ":" + cursor.getString(idx);
//
//                    } else {
//                       // objSms.setFolderName("sent");
//                    }
//                }
//               System.out.println("Messag: "+msgData);
//            } while (cursor.moveToNext());
//        } else {
//            // empty box, no SMS
//        }
    }
    public List<Map> getAllSms(Context mActivity) {
        List<Map> lstSms = new ArrayList<Map>();

        Uri message = Uri.parse("content://sms/");
        ContentResolver cr = mActivity.getContentResolver();

        Cursor c = cr.query(message, null, null, null, null);

        int totalSMS = c.getCount();

        if (c.moveToFirst()) {
            for (int i = 0; i < totalSMS; i++) {

//                objSms = new Sms();
//                objSms.setId(c.getString(c.getColumnIndexOrThrow("_id")));
//                objSms.setAddress(c.getString(c
//                        .getColumnIndexOrThrow("address")));
//                objSms.setMsg(c.getString(c.getColumnIndexOrThrow("body")));
//                objSms.setReadState(c.getString(c.getColumnIndex("read")));
//                objSms.setTime(c.getString(c.getColumnIndexOrThrow("date")));
                String message1="";
                if (c.getString(c.getColumnIndexOrThrow("type")).contains("1")) {
                    message1=c.getString(c.getColumnIndexOrThrow("body"));
                    System.out.println();
                } else {
                 //   objSms.setFolderName("sent");
                }

System.out.println(message1);
                c.moveToNext();
            }
        }
        // else {
        // throw new RuntimeException("You have no SMS");
        // }
        c.close();

        return lstSms;
    }

}
