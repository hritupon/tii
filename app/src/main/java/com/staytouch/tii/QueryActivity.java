package com.staytouch.tii;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class QueryActivity extends AppCompatActivity {

    private EditText queryEditText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_query);
        queryEditText =(EditText)findViewById(R.id.query);
        Utils.alignHintEditText(queryEditText, "Type query here...");
    }
}
