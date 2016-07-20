package com.i3cnam.gofast;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class Main extends AppCompatActivity {
    public final static String USER_TYPE = "com.i3cnam.gofast.USER_TYPE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void selectUserType(View view) {
//        Intent intent = new Intent(this, GooglePlacesAutocompleteActivity.class);
        Intent intent = new Intent(this, ConfigureTravel.class);
        String userType = view.getTag().toString();
        intent.putExtra(USER_TYPE, userType);
        startActivity(intent);
    }
}
