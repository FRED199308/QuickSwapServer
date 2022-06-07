package com.lunar.quickswapserver;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

public class SMSOperations {
    public static ArrayList getAllSms(String status, Context context) {

        if(status==null || status.equalsIgnoreCase("All"))
        {

            DBHelper db=new DBHelper(context);
            SQLiteDatabase sq=db.getWritableDatabase();
            return  db.getAllLOgs();


        }
        else{
            DBHelper db=new DBHelper(context);
            SQLiteDatabase sq=db.getWritableDatabase();
            return  db.FilterLOgs(status);
        }



    }

}
