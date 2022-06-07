package com.lunar.quickswapserver;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class PlanRegistration extends AppCompatActivity implements View.OnClickListener {
Spinner network,category;
    Button savebtn;
    ProgressBar bar;
    TextView desc,agentDesc;
EditText planName,cost,actualCost,agentCost;
DBHelper db;
SQLiteDatabase sq;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plan_registration);
network=findViewById(R.id.network);
category=findViewById(R.id.category);
cost=findViewById(R.id.cost);
desc=findViewById(R.id.desc);
agentDesc=findViewById(R.id.agentDesc);
agentCost=findViewById(R.id.agentCost);
        actualCost=findViewById(R.id.actualCost);
planName=findViewById(R.id.planNameField);
        savebtn=findViewById(R.id.savebtn);
        bar=findViewById(R.id.progressBar);
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

db=new DBHelper(this);
        sq=db.getWritableDatabase();
        String sex []={"Select Level","Safaricom","Airtel","Telkom"};
        ArrayAdapter adp1 = new ArrayAdapter<String>(getApplicationContext(),
                android.R.layout.simple_spinner_item,sex);
        adp1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        network.setAdapter(adp1);

String catr[]=getResources().getString(R.string.packages).split(";");
        adp1 = new ArrayAdapter<String>(getApplicationContext(),
                android.R.layout.simple_spinner_item,catr);
        adp1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        category.setAdapter(adp1);

        savebtn.setOnClickListener(this);
        category.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(category.getSelectedItemPosition()>6)
                {
                    desc.setText("Enter % Discount For Airtime Non Agent");
                    agentDesc.setText("Enter % Discount For Airtime For Agent");
                    actualCost.setText("100");

                }
                else{
                    desc.setText("Non Agent Cost In Ksh");
                    agentDesc.setText("Agent Cost in Ksh");

                    actualCost.setText("");

                }
                System.err.println("ssssssssss");
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

    }

    @Override
    public void onClick(View view) {

        switch (view.getId())
        {
            case R.id.savebtn:
if(planName.getText().toString().isEmpty())
{
    Toast.makeText(this,"Invalid Plan Name", Toast.LENGTH_LONG).show();
}
else{
    if(cost.getText().toString().isEmpty())
    {
        Toast.makeText(this,"Invalid Cost", Toast.LENGTH_LONG).show();
    }
    else{

        if(actualCost.getText().toString().isEmpty())
        {
            Toast.makeText(this,"Invalid Actual Cost Value", Toast.LENGTH_LONG).show();
        }
        else{
            if(agentCost.getText().toString().isEmpty())
            {
                Toast.makeText(this,"Invalid Agent Cost Value", Toast.LENGTH_LONG).show();
            }
            else{

                db.registerPlan(planName.getText().toString(),category.getSelectedItem().toString(), Integer.parseInt(cost.getText().toString()),network.getSelectedItem().toString(),actualCost.getText().toString(),agentCost.getText().toString());

                Toast.makeText(this,"Plan Saved", Toast.LENGTH_LONG).show();
                planName.setText("");
                cost.setText("");
                category.setSelection(0);

            }

        }

    }
}

                break;
        }

    }
}