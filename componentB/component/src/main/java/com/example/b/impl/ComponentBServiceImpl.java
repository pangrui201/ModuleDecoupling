package com.example.b.impl;

import android.content.Context;
import android.content.Intent;

import com.example.b.page.BActivity;
import com.example.componmentdemo.api.IComponentBService;
import com.example.wing.ComponentService;
import com.example.wing.ComptServiceManager;
import com.example.wing.IComponentService;
//import com.google.auto.service.AutoService;


@ComponentService(IComponentService.class)
//@AutoService(IComponentService.class)
public class ComponentBServiceImpl implements IComponentBService {
    @Override
    public void openPage(Context context) {
        Intent intent = new Intent(context, BActivity.class);
        context.startActivity(intent);
    }

    @Override
    public String requestServiceInfo(String a, String b) {
        return "FROM B: "+a+b;
    }
}
