package com.lunar.quickswapserver;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class ReceiveNotificationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(null);

//        TextView categotyTv = findViewById(R.id.category);
//        TextView brandTv = findViewById(R.id.brand);

        if (getIntent().hasExtra("category")){
            String category = getIntent().getStringExtra("category");
            String brand = getIntent().getStringExtra("brandId");
//            categotyTv.setText(category);
//            brandTv.setText(brand);
        }
    }
}
