package com.lunar.quickswapserver;

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Map;

public class OrdersView extends AppCompatActivity implements View.OnKeyListener {
    RecyclerView recyclerView;
    EditText search;
    ProgressBar bar;
    ArrayList list;
DBHelper helper;
    static AlertDialog.Builder alert ;
    OrderAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders_view);
        search=findViewById(R.id.searchfield);
        helper=helper.getInstance(this);
        bar=findViewById(R.id.progressBar);
        recyclerView = findViewById(R.id.orderRecycleview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        search.setSingleLine();
        search.setOnKeyListener(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setElevation(20);
        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        //streams.setOnItemSelectedListener(this);
        alert= new AlertDialog.Builder(this);
        alert.setTitle("Confirm Action");
        alert.setMessage("Are you sure you want to delete?");
        alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                if(OrderAdapter.action.equalsIgnoreCase("delete"))
                {
                    DeleteOrder deleteOrder=new DeleteOrder();
                    deleteOrder.execute(OrderAdapter.selectedOrder);
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

        DataFetcher fetcher=new DataFetcher();
        fetcher.execute();

    }


    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if(v.getId()==search.getId())
        {

            System.out.println("Written");

        }


        String searchInfor=search.getText().toString();
        if(!searchInfor.isEmpty())
        {
            OrderSearcher userSearcher=new OrderSearcher();
            userSearcher.execute(searchInfor);
        }

        return false;
    }


    private class DataFetcher extends AsyncTask<String, Void, ArrayList> {

        @Override
        protected ArrayList doInBackground(String...voids) {

            return       list=helper.getAllOrders();




        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            bar.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(ArrayList result) {
            System.out.println("resul:"+result);
            recyclerView.removeAllViews();
            adapter = new OrderAdapter(result, OrdersView.this);
            recyclerView.setAdapter(adapter);

            adapter.notifyDataSetChanged();
            bar.setVisibility(View.INVISIBLE);
        }

    }

    private class OrderSearcher extends AsyncTask<String, Void, ArrayList> {

        @Override
        protected ArrayList doInBackground(String...voids) {

            return       list=helper.getOrderDetails(voids[0]);




        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            bar.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(ArrayList result) {
            recyclerView.removeAllViews();
            adapter = new OrderAdapter(result, OrdersView.this);
            recyclerView.setAdapter(adapter);

            adapter.notifyDataSetChanged();
            bar.setVisibility(View.INVISIBLE);
        }

    }
    private class DeleteOrder extends AsyncTask<String, Void, Map> {

        @Override
        protected Map doInBackground(String...voids) {

         //   return      helper.deleteOrder(voids[0]);

return null;


        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            bar.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(Map result) {

                DataFetcher fetcher=new DataFetcher();
                fetcher.execute();
                Globals.toaster(result.get("Deleted").toString());

//            recyclerView.setAdapter(adapter);
//
//            adapter.notifyDataSetChanged();
            bar.setVisibility(View.INVISIBLE);
        }

    }


}
