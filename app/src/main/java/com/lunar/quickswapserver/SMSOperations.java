package com.lunar.quickswapserver;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

public class SMSOperations {
    DBHelper db;
    public  ArrayList getAllSms(String status, Context context) {

        if(status==null || status.equalsIgnoreCase("All"))
        {

          db=db.getInstance(context);
            SQLiteDatabase sq=db.getWritableDatabase();
            return  db.getAllLOgs();


        }
        else{
             db=db.getInstance(context);
            SQLiteDatabase sq=db.getWritableDatabase();
            return  db.FilterLOgs(status);
        }



    }

}
