package com.example.bluetoothsdkapp;

import android.os.Bundle;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

public class bluetooth_link extends AppCompatActivity {

    private ListView blue_list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_link);
        initView();
    }

    private void initView() {
        blue_list = (ListView) findViewById(R.id.blue_list);
    }
}
