package com.lunar.quickswapserver;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Map;


public class SMSLogAdapter extends RecyclerView.Adapter<SMSLogAdapter.ViewHolder>{

    ArrayList list;

    static String selectedsms="",action="";
    static  int pointer=0;
    // RecyclerView recyclerView;
    public SMSLogAdapter(ArrayList lists) {
        list=lists;


    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem= layoutInflater.inflate(R.layout.smsloglayout, parent, false);
        ViewHolder viewHolder = new ViewHolder(listItem);
        ;
        return viewHolder;
    }



    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        System.out.println("size:"+list.size());
        if(list.size()>0)
        {
            System.out.println(((Map) list.get(position)).get("phone").toString());
            holder.sn.setText(String.valueOf(position+1));
            holder.phone.setText(((Map) list.get(position)).get("phone").toString());
            holder.remarks.setText(((Map) list.get(position)).get("remarks").toString());
            holder.response.setText(((Map) list.get(position)).get("response").toString());
            holder.date.setText(((Map) list.get(position)).get("rechargeDate").toString());
            holder.status.setText(((Map) list.get(position)).get("status").toString());
            holder.network.setText(((Map) list.get(position)).get("network").toString());




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

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView response;
        public TextView phone;
        public TextView sn;
        public TextView network;
        public TextView remarks;
        public TextView status;
        public TextView date;
        public TableRow tableRow;
       // public Button refresh;
        public ViewHolder(View itemView) {
            super(itemView);

            this.response = itemView.findViewById(R.id.response);
            this.phone=itemView.findViewById(R.id.phone);
            this.remarks=itemView.findViewById(R.id.remarks);
            this.date=itemView.findViewById(R.id.date);
            this.sn=itemView.findViewById(R.id.sn);
            this.status=itemView.findViewById(R.id.status);
    //        this.refresh=itemView.findViewById(R.id.refresh);
            this.network=itemView.findViewById(R.id.network);

            tableRow = itemView.findViewById(R.id.tableRow);

        //    this.refresh.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            pointer= Integer.parseInt(sn.getText().toString());
            selectedsms=network.getText().toString();

            switch (v.getId())
            {
//                case R.id.delete:
//
//
//                    action="delete";
//
//                    SMSLogoView.alert.setMessage("Are you sure you Delete this SMS from SMSLog");
//                    SMSLogoView.alert.show();
//
//
//
//
//                    break;

            }
        }
    }


}