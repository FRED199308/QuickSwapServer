package com.lunar.quickswapserver;


import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.telephony.SmsManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class AgentsAdapter extends RecyclerView.Adapter<AgentsAdapter.ViewHolder>{

    ArrayList list;

    static String selectedBundle="",action="";
    static  int pointer=0;
    String network="";

   public Context context;
    DBHelper db;
    SQLiteDatabase sq;
    // RecyclerView recyclerView;
    public AgentsAdapter(ArrayList lists, Context context) {
        list=lists;
        this.network=network;
this.context=context;

        db=db.getInstance(context);
        sq=db.getWritableDatabase();
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem= layoutInflater.inflate(R.layout.agents_layout, parent, false);
        ViewHolder viewHolder = new ViewHolder(listItem);
        return viewHolder;
    }



    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        if(list.size()>0)
        {
//System.err.println("was called"+list.get(position).toString()+"  network :"+network+ "cost:"+db.getPlanDetails(list.get(position).toString(),network));


            holder.agentName.setText(((Map)list.get(position)).get("agentName").toString());


            holder.contact.setText(((Map)list.get(position)).get("contact").toString());
            holder.rowId.setText(((Map)list.get(position)).get("rowId").toString());


            String approvalStatus=((Map)list.get(position)).get("status").toString();
            if(approvalStatus.equalsIgnoreCase("Approved"))
            {
                holder.status.setChecked(true);
            }
            else{
                holder.status.setChecked(false);
            }





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
        public EditText agentName;

        public TableRow tableRow;
        TextView rowId;
        public EditText contact;
        public CheckBox status;
        public EditText agentCost;
        public Button edit,delete;
        public ViewHolder(View itemView) {
            super(itemView);
            this.agentName = itemView.findViewById(R.id.agentName);
            this.status = itemView.findViewById(R.id.status);


            tableRow = itemView.findViewById(R.id.tableRow);
            edit = itemView.findViewById(R.id.editbtn);
            delete = itemView.findViewById(R.id.deletebtn);
            agentCost = itemView.findViewById(R.id.agentCost);
            rowId = itemView.findViewById(R.id.rowId);

            contact = itemView.findViewById(R.id.contactNumber);


            this.delete.setOnClickListener(this);
            this.edit.setOnClickListener(this);
            this.status.setOnClickListener(this);

        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
        @Override
        public void onClick(View v) {

            selectedBundle= agentName.getText().toString();
            switch (v.getId())
            {
                case R.id.editbtn:

if(contact.getText().toString().isEmpty()|| contact.getText().toString().equalsIgnoreCase("0"))
{
    Toast.makeText(context,"Invalid Contact Number", Toast.LENGTH_LONG).show();
}
else {
    String approvalStatus="";
    if(status.isChecked())
    {
        approvalStatus="Approved";

    }
    else{
        approvalStatus="Pending";
    }

    db.updateAgentDetails(agentName.getText().toString(), contact.getText().toString(),rowId.getText().toString(),approvalStatus);
    Toast.makeText(context,"Updated", Toast.LENGTH_LONG).show();
   // System.err.println(db.getAllplans());
}




                    break;

                case R.id.deletebtn:



                    db.deleteAgent(agentName.getText().toString(), contact.getText().toString());
                    Toast.makeText(context,"Agent Deleted", Toast.LENGTH_LONG).show();




                    break;

                case R.id.status:
                    String approvalStatus="";
                    if(status.isChecked())
                    {
                        approvalStatus="Approved";
                        String phone=contact.getText().toString();
                        sendSms(context,"Hello "+agentName.getText().toString()+", Your Request To Be An Agent Has Been Approved\n Always Use Your Nominated Number To Enjoy Special Discounts",phone,true);
                    }
                    else{
                        approvalStatus="Pending";
                    }

                    db.updateAgentDetails(agentName.getText().toString(), contact.getText().toString(),rowId.getText().toString(),approvalStatus);
                    Toast.makeText(context,"Updated", Toast.LENGTH_LONG).show();
                    break;

            }
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
    private void sendSms(Context context, String message,  String destination, boolean sim1) {

//        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
//            requestPermissions(new Activity(),new String[]{Manifest.permission.SEND_SMS}, 23);
//            return;
//        }
//
//        SmsManager sms = SmsManager.getDefault();
//        ArrayList<String> parts = sms.divideMessage(message);
//        SmsManager.getSmsManagerForSubscriptionId(susbscriptionId).sendMultipartTextMessage(destination, null, parts, null,
//                null);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            SubscriptionManager localSubscriptionManager = SubscriptionManager.from(context);
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the Order grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            if (localSubscriptionManager.getActiveSubscriptionInfoCount() > 1) {
                List localList = localSubscriptionManager.getActiveSubscriptionInfoList();

                SubscriptionInfo simInfo1 = (SubscriptionInfo) localList.get(0);
                SubscriptionInfo simInfo2 = (SubscriptionInfo) localList.get(1);


                if(sim1)
                {
                    //SendSMS From SIM One
                    SmsManager.getSmsManagerForSubscriptionId(simInfo1.getSubscriptionId()).sendTextMessage(destination, null, message, null, null);

                }
                else{
                    //SendSMS From SIM Two
                    SmsManager.getSmsManagerForSubscriptionId(simInfo2.getSubscriptionId()).sendTextMessage(destination, null, message, null, null);

                }


            }
        } else {
//            SmsManager.getDefault().sendTextMessage(customer.getMobile(), null, smsText, sentPI, deliveredPI);
//            Toast.makeText(getBaseContext(), R.string.sms_sending, Toast.LENGTH_SHORT).show();
        }

    }

}