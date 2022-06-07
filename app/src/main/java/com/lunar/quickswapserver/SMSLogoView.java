package com.lunar.quickswapserver;

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class SMSLogoView extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {
    Spinner statusfilter;
    ProgressBar bar;
    RecyclerView recyclerView;
    static AlertDialog.Builder alert ;
    ArrayList list;
    Button clear;
    Button refresh;
    SMSLogAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smslogo_view);

        Globals.context=getApplicationContext();
        statusfilter=(Spinner)findViewById(R.id.statusFilter);
        clear=findViewById(R.id.clearbtn);
        recyclerView=findViewById(R.id.smsLogRecycleview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        bar=(ProgressBar)findViewById(R.id.progressBar);
refresh=findViewById(R.id.refresh);



        ArrayList list=new ArrayList();

        list.add("All");
        list.add("Failed");
        list.add("completed");
        ArrayAdapter<String> adp1 = new ArrayAdapter<String>(getApplicationContext(),
                android.R.layout.simple_spinner_item, list);

        statusfilter.setAdapter(adp1);


refresh.setOnClickListener(this);

clear.setOnClickListener(this);



        SMS fetcher=new SMS();
        fetcher.execute("All");
        alert= new AlertDialog.Builder(this);
        alert.setTitle("Confirm Action");
        alert.setMessage("Are you sure you want to delete?");
        alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                if(SMSLogAdapter.action.equalsIgnoreCase("delete"))
                {
                    SMS fetcher;
                   DBHelper dbHelper=new DBHelper(SMSLogoView.this);
             dbHelper.clearAitimeLogs();
              fetcher  =new SMS();
                fetcher.execute("All");

                }

                dialog.dismiss();
            }
        });
        alert.setNegativeButton("No", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });


        SMS rights=new SMS();
        rights.execute("All");
        statusfilter.setOnItemSelectedListener(this);
        refresh.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        
        switch (view.getId())
        {case        R.id.refresh:

            SMS fetcher = new SMS();
            fetcher.execute("All");

            break;

            case        R.id.clearbtn:


                DBHelper dbHelper=new DBHelper(SMSLogoView.this);
                dbHelper.clearAitimeLogs();
                fetcher  =new SMS();
                fetcher.execute("All");

                break;
            
        }
        
       

    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        SMS fetcher=null;
        switch (adapterView.getId())
        {

            case R.id.statusFilter:
                SMS sms=new SMS();
                sms.execute(statusfilter.getSelectedItem().toString());
                break;






        }
    }


    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    private class SMS extends AsyncTask<String, Void, ArrayList> {

        @Override
        protected ArrayList doInBackground(String...voids) {

            list=SMSOperations.getAllSms(voids[0], SMSLogoView.this);

            return  list;




        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            bar.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(ArrayList result) {

            recyclerView.removeAllViews();
            adapter = new SMSLogAdapter(result);
            recyclerView.setAdapter(adapter);
            // System.out.println(adapter);
            adapter.notifyDataSetChanged();
            bar.setVisibility(View.INVISIBLE);
        }

    }




    private class SMSDelete extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String...voids) {

            

            return "";




        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            bar.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(String result) {

          
            bar.setVisibility(View.INVISIBLE);
        }

    }



}