package com.example.componmentdemo.api;

import android.content.Context;
import com.example.wing.IComponentService;

public interface IComponentBService extends IComponentService {

            void openPage(Context context);

            String requestServiceInfo(String a,String b);

}
