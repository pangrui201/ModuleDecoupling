package com.example.componmentdemo.api;

import android.content.Context;

import com.example.wing.IComponentService;

public interface IComponentAService extends IComponentService {

    String search(String url);

    void openSearchPage(Context context);
}
