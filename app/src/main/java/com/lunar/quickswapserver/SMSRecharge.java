package com.lunar.quickswapserver;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class SMSRecharge extends AppCompatActivity {
TextView bal,message;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smsrecharge);
        bal=findViewById(R.id.bal);
       message=findViewById(R.id.message);

        String instructions="M-Pesa Pay Bill\n" +
                "1.Using your MPesa-enabled phone, select \"Pay Bill\" from the M-Pesa-\n" +
                " menu\n" +
                "2.Enter LUNAR TECH SOLUTION Business Number 4036505\n" +
                "3.Enter your LUNAR TECH SOLUTION Account Number.Your account number-\n" +
                " is "+"  "+"asapbiz.sms \n" +
                "4.Enter the Amount of credits you want to buy\n" +
                "5.Confirm that all the details are correct and press Ok\n" +
                "6.Your Account will automatically be credited on your system.\n" +
                "For help call 0707353225";
        message.setText(instructions);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setElevation(20);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        BalanceGetter getter=new BalanceGetter();
        getter.execute();
        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


    }




    private  class BalanceGetter extends AsyncTask<String, Void, String> {


        @Override
        protected String doInBackground(String... strings) {
            return   Balance.smsBalance();
        }
        @Override
        protected void onPostExecute(String result) {

            bal.setText("Current SMS Balance: "+result);

        }

    }
}