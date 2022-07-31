package com.example.componmentdemo_a.page;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.example.componmentdemo.api.IComponentBService;
import com.example.wing.ComptServiceManager;
import com.example.componmentdemo_a.R;


public class AActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_component_a);
        TextView aBtn =  findViewById(R.id.aBtn);
        aBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                IComponentBService bService  =  ComptServiceManager.getInstance().getComponentService(IComponentBService.class);
                String bStr = bService.requestServiceInfo("a","b");
                aBtn.setText(bStr);
            }
        });
    }
}