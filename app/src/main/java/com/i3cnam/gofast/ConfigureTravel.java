package com.i3cnam.gofast;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class ConfigureTravel extends AppCompatActivity {
    public final static String USER_TYPE = "com.i3cnam.gofast.USER_TYPE";
    private String userType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configure_travel);

        Intent intent = getIntent();
        this.userType = intent.getStringExtra(Main.USER_TYPE);
    }

    public void validConfigureTravel(View view) {
        Intent intent = new Intent(this, PassengerMap.class);
        intent.putExtra(USER_TYPE, this.userType);
        startActivity(intent);
    }
}
