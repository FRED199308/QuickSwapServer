package com.lunar.quickswapserver;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Map;


public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.ViewHolder>{

    ArrayList list;

    static String selectedOrder ="",action="";
    static  int pointer=0;
    // RecyclerView recyclerView;
    DBHelper db;
    SQLiteDatabase sq;
    Context context;
    public OrderAdapter(ArrayList lists, Context context) {
        list=lists;
this.context=context;
        db=new DBHelper(context);
        sq=db.getWritableDatabase();

    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem= layoutInflater.inflate(R.layout.orderslayout, parent, false);
        ViewHolder viewHolder = new ViewHolder(listItem);
        return viewHolder;
    }



    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        if(list.size()>0)
        {
System.out.println(list);
            holder.sn.setText(String.valueOf(position+1));
            holder.phone.setText(((Map) list.get(position)).get("rechargePhone").toString());
            holder.amount.setText(((Map) list.get(position)).get("cost").toString());
            holder.network.setText(((Map) list.get(position)).get("network").toString());
            holder.date.setText(((Map) list.get(position)).get("dateRequested").toString());
            holder.mpesacode.setText(((Map) list.get(position)).get("paymentCode").toString());


        }

        holder.tableRow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

//                Toast.makeText(view.getContext(),"click on item: "+((Map) list.get(position)).get("admissionNumber").toString(),Toast.LENGTH_LONG).show();
            }
        });

    }


    @Override
    public int getItemCount() {
        return list.size();
    }

    public  class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView network;
        public EditText phone;
        public TextView mpesacode;
        public TextView sn;

        public TextView amount, date;
        public TextView stream;
        public TableRow tableRow;
        public Button delete;
        public ViewHolder(View itemView) {
            super(itemView);
            this.network = itemView.findViewById(R.id.network);
            this.phone =itemView.findViewById(R.id.phone);
            this.amount =itemView.findViewById(R.id.amount);
            this.mpesacode =itemView.findViewById(R.id.mpesaCode);

            this.sn=itemView.findViewById(R.id.sn);
this.date =itemView.findViewById(R.id.date);

            this.delete=itemView.findViewById(R.id.deletebtn);

            tableRow = itemView.findViewById(R.id.tableRow);
            this.delete.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
            pointer= Integer.parseInt(sn.getText().toString());
            selectedOrder = phone.getText().toString();
            switch (v.getId())
            {
                case R.id.deletebtn:


                    action="delete";

                    db.deleteOrderDetails(phone.getText().toString(),mpesacode.getText().toString(),amount.getText().toString());
                    Toast.makeText(context,"Order Deleted", Toast.LENGTH_LONG).show();



                    break;

            }
        }
    }


}