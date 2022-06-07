package com.lunar.quickswapserver;

import android.content.Context;
import android.widget.Toast;

public class Globals {

   public static Context context;

    public static void toaster(String message)
    {
        Toast.makeText(Globals.context,message, Toast.LENGTH_LONG).show();
    }
    public static String networkProviderDeterminer(String phone)
    {
        if(phone.startsWith("011")||
                phone.startsWith("070")||
                phone.startsWith("071")||
                phone.startsWith("072")||
                phone.startsWith("074")||
                phone.startsWith("0757")||
                phone.startsWith("0758")||
                phone.startsWith("0759")||
                phone.startsWith("0768")||
                phone.startsWith("0769")||
                phone.startsWith("079")||
                phone.startsWith("0110")||
                phone.startsWith("0111"))
        {
            return "Safaricom";
        }
        else if(phone.startsWith("073")||
                phone.startsWith("0750")||
                phone.startsWith("010")||
                phone.startsWith("0751")||
                phone.startsWith("0752")||
                phone.startsWith("0753")||
                phone.startsWith("0754")||
                phone.startsWith("0755")||
                phone.startsWith("0756")||
                phone.startsWith("0761")||
                phone.startsWith("0762")||
                phone.startsWith("0763")||
                phone.startsWith("0764")||
                phone.startsWith("0765")||
                phone.startsWith("0766")||
                phone.startsWith("078")||
                phone.startsWith("0786")||
                phone.startsWith("0787")||
                phone.startsWith("0788")||
                phone.startsWith("0789")||
                phone.startsWith("0100")||
                phone.startsWith("0101")||
                phone.startsWith("0102")){
            return "Airtel";
        }
        else if(phone.startsWith("077"))
        {
            return "Telkom";
        }
        else if(phone.startsWith("076")){
            return "Equitel";
        }
        else{
            return "Unknown Network";
        }


    }}
