package com.lunar.quickswapserver;


import android.content.Context;
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


public class BlackListAdapter extends RecyclerView.Adapter<BlackListAdapter.ViewHolder>{

    ArrayList list;

    static String selectedNumber ="",action="",tel1toEdit;
    static  int pointer=0;
    Context context;
  static   DBHelper dbHelper;
    // RecyclerView recyclerView;
    public BlackListAdapter(ArrayList lists,Context context) {
        list=lists;
        this.context=context;
        dbHelper=dbHelper.getInstance(context);
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem= layoutInflater.inflate(R.layout.blacklist, parent, false);
        ViewHolder viewHolder = new ViewHolder(listItem);
        return viewHolder;
    }



    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        if(list.size()>0)
        {

            holder.sn.setText(String.valueOf(position+1));
            holder.phone.setText(((Map) list.get(position)).get("phoneNumber").toString());






        }

        holder.tableRow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

//                Toast.makeText(view.getContext(),"click on item: "+((Map) list.get(position)).get("memberNumber").toString(),Toast.LENGTH_LONG).show();
            }
        });

    }


    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public TextView phone;
        public TextView sn;


        public TableRow tableRow;
        public Button delete;
        public ViewHolder(View itemView) {
            super(itemView);

            this.phone =itemView.findViewById(R.id.memberName);
         ;

            this.sn=itemView.findViewById(R.id.sn);

            this.delete =itemView.findViewById(R.id.editbtn);


            tableRow = itemView.findViewById(R.id.tableRow);

            this.delete.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
            pointer= Integer.parseInt(sn.getText().toString());
            selectedNumber =phone.getText().toString();
            tel1toEdit=phone.getText().toString();

            switch (v.getId())
            {

                case R.id.deletebtn:

                    dbHelper.deleteBlackListNumber(selectedNumber);


                    break;


            }
        }
    }


}