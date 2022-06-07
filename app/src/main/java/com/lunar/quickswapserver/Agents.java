package com.lunar.quickswapserver;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import java.util.ArrayList;

public class Agents extends AppCompatActivity {
    AgentsAdapter adapter;
    ArrayList list;
RecyclerView recyclerView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agents);
DBHelper dbHelper=new DBHelper(this);

        recyclerView = findViewById(R.id.agentsRecycleview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AgentsAdapter(dbHelper.getagentsDetails(),this);
        System.err.println(dbHelper.getagentsDetails());
       recyclerView.setAdapter(adapter);
    }




}