package com.xl.cashdemo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        findViewById(R.id.btn_cash).setOnClickListener(v -> {

        });


        findViewById(R.id.btn_cash2).setOnClickListener(v -> {
            byte[] a = new byte[1024 * 1024 * 1000000];
        });


        findViewById(R.id.btn_cash3).setOnClickListener(v -> {

            try {
                Thread.sleep(1000 * 100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        });


        findViewById(R.id.test1).setOnClickListener(v -> Toast.makeText(MainActivity.this, "测试1", Toast.LENGTH_SHORT).show());


        findViewById(R.id.test2).setOnClickListener(v -> Toast.makeText(MainActivity.this, "测试2", Toast.LENGTH_SHORT).show());


        findViewById(R.id.test3).setOnClickListener(v -> Toast.makeText(MainActivity.this, "测试3", Toast.LENGTH_SHORT).show());


    }

}