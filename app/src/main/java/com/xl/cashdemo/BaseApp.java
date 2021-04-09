package com.xl.cashdemo;

import android.app.Application;

import com.xl.xlcash.XlCash;

public class BaseApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        XlCash.install(this, "1064902365@qq.com");
    }


}
