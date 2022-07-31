package com.example.componmentdemo;

import android.app.Application;
import android.util.Log;

import com.example.wing.internal.ComponentServiceLoader;
import com.example.wing.ComptServiceManager;
import com.example.wing.IComponentService;

import java.util.Iterator;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
       ComptServiceManager.getInstance().init();
    }


}
