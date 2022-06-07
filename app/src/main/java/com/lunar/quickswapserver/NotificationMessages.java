package com.lunar.quickswapserver;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class NotificationMessages extends AppCompatActivity {
Button back;
TextView title;
TextView body;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_messages);

        title=findViewById(R.id.title);
        body=findViewById(R.id.body);


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


        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String message = extras.getString("message");
            title.setText("Request Received");
        body.setText(message);
        System.err.println("The Message:"+message);

        }
    }
}