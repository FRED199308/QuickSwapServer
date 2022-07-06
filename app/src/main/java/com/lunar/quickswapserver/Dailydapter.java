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


public class Dailydapter extends RecyclerView.Adapter<Dailydapter.ViewHolder>{

    ArrayList list;

    static String selectedBundle="",action="";
    static  int pointer=0;
    String network="";
   public Context context;
    DBHelper db;
    SQLiteDatabase sq;
    // RecyclerView recyclerView;
    public Dailydapter(ArrayList lists, Context context, String network, String category) {
        list=lists;
        this.network=network;
this.context=context;

        db=db.getInstance(context);
        sq=db.getWritableDatabase();
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem= layoutInflater.inflate(R.layout.bundles_layout, parent, false);
        ViewHolder viewHolder = new ViewHolder(listItem);
        return viewHolder;
    }



    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        if(list.size()>0)
        {
//System.err.println("was called"+list.get(position).toString()+"  network :"+network+ "cost:"+db.getPlanDetails(list.get(position).toString(),network));

            System.err.println(list.get(position));
            holder.bundlePackage.setText(((Map)list.get(position)).get("planname").toString());

String plan=((Map)list.get(position)).get("planname").toString();
            holder.cost.setText(((Map)list.get(position)).get("cost").toString());
            holder.agentCost.setText(((Map)list.get(position)).get("agentCost").toString());






        }
        else {
            System.err.println("was never called");
        }
//
//        holder.tableRow.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
////                Toast.makeText(view.getContext(),"click on item: "+((Map) list.get(position)).get("admissionNumber").toString(),Toast.LENGTH_LONG).show();
//            }
//        });

    }


    @Override
    public int getItemCount() {
        return list.size();
    }

    public  class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public EditText bundlePackage;

        public TableRow tableRow;
        public EditText cost;
        public EditText agentCost;
        public Button edit,delete;
        public ViewHolder(View itemView) {
            super(itemView);
            this.bundlePackage = itemView.findViewById(R.id.bundle);


            tableRow = itemView.findViewById(R.id.tableRow);
            edit = itemView.findViewById(R.id.editbtn);
            delete = itemView.findViewById(R.id.deletebtn);
            agentCost = itemView.findViewById(R.id.agentCost);

            cost = itemView.findViewById(R.id.cost);


            this.delete.setOnClickListener(this);
            this.edit.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {

            selectedBundle=bundlePackage.getText().toString();
            switch (v.getId())
            {
                case R.id.editbtn:

if(cost.getText().toString().isEmpty()||cost.getText().toString().equalsIgnoreCase("0"))
{
    Toast.makeText(context,"Invalid Cost Amount", Toast.LENGTH_LONG).show();
}
else {
    db.updateplanDetails(bundlePackage.getText().toString(),network, Integer.parseInt(cost.getText().toString()),Integer.parseInt(agentCost.getText().toString()));
    Toast.makeText(context,"Updated", Toast.LENGTH_LONG).show();
   // System.err.println(db.getAllplans());
}




                    break;

                case R.id.deletebtn:



                    db.deletplanDetails(bundlePackage.getText().toString(),network, Integer.parseInt(cost.getText().toString()));
                    Toast.makeText(context,"Plan Deleted", Toast.LENGTH_LONG).show();




                    break;

            }
        }
    }


}