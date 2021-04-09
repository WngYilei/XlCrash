package com.xl.cashdemo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.xl.cashdemo.databinding.ActivityIndexBinding;

public class IndexActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityIndexBinding indexBinding = DataBindingUtil.setContentView(this, R.layout.activity_index);
        indexBinding.btnCash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(IndexActivity.this, MainActivity.class));
            }
        });

        indexBinding.btnInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(IndexActivity.this, InfoActivity.class));
            }
        });
    }
}