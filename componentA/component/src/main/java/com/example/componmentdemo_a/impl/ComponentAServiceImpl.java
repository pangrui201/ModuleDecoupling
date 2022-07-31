package com.example.componmentdemo_a.impl;

import android.content.Context;

import com.example.componmentdemo.api.IComponentAService;
import com.example.componmentdemo.api.IComponentBService;
import com.example.wing.ComponentService;
import com.example.wing.IComponentService;
import com.example.wing.ComptServiceManager;


@ComponentService(IComponentService.class)
public class ComponentAServiceImpl implements IComponentAService {
    @Override
    public String search(String url) {
       IComponentBService comptBService = ComptServiceManager.getInstance().getComponentService(IComponentBService.class);
        String result = comptBService.requestServiceInfo("data1","data2");
        return result;
    }

    @Override
    public void openSearchPage(Context context) {

    }
}
