package com.i3cnam.gofast.views;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.i3cnam.gofast.R;
import com.i3cnam.gofast.communication.CommInterface;
import com.i3cnam.gofast.communication.Communication;
import com.i3cnam.gofast.model.User;

public class ConfigureAccount extends AppCompatActivity {
    private String mPhoneNumber;
    private boolean declareUser = true;
    private EditText nicknameEdit;
    private TextView foundMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configure_account);
        // get the telephone number
        TelephonyManager tMgr = (TelephonyManager)this.getSystemService(Context.TELEPHONY_SERVICE);
        mPhoneNumber = tMgr.getLine1Number();
        // get the telephone number field
        EditText phoneNumberField = (EditText) findViewById(R.id.phoneNumberField);
        // fill the telephone number field
        phoneNumberField.setText(mPhoneNumber);
        // get the nickname field
        nicknameEdit = (EditText) findViewById(R.id.editNickname);
        // get the message field
        foundMessage = (TextView) findViewById(R.id.foundMessage);
        new TaskRetrieveAccount().execute();
    }

    public void recordUser(View view) {
        if (nicknameEdit.getText().toString().equals("")) {
            Toast.makeText(ConfigureAccount.this, R.string.empyNicknameMsg, Toast.LENGTH_SHORT).show();
        }
        else {
            if (declareUser) {
                new TaskCreateAccount().execute(nicknameEdit.getText().toString(), mPhoneNumber);
            }
            else {
                writeSharedPreferences(nicknameEdit.getText().toString(), mPhoneNumber);
            }
        }
    }

    private void writeSharedPreferences(String nickname, String phoneNumber){
        SharedPreferences prefs = getSharedPreferences("X", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("userNickname", nickname);
        editor.putString("userPhoneNumber", phoneNumber);
        editor.commit();
        // Start main activity
        Intent intent = new Intent(this, Main.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);

    }

    private class TaskRetrieveAccount extends AsyncTask<String, String,String> {
        protected String doInBackground(String... urls) {
            // call the service to know is the phone number is already registered
            CommInterface comm = new Communication();
            return comm.retrieveAccount(mPhoneNumber);
        }
        protected void onPostExecute(String result) {
            if (result != null) {
                // put the received nickname into field and disable it
                nicknameEdit.setEnabled(false);
                nicknameEdit.setText(result);
                // put the information into message field
                foundMessage.setText(result + getString(R.string.accountFound));
                // tag the user as existing
                declareUser = false;
            }
            else {
                declareUser = true;
            }
        }
    }

    private class TaskCreateAccount extends AsyncTask<String, String,String> {
        private User declared;
        protected String doInBackground(String... urls) {
            // call the service to know is the phone number is already registered
            CommInterface comm = new Communication();
            declared = new User(urls[0],urls[1]);
            return comm.declareUser(declared);
        }
        protected void onPostExecute(String result) {
            switch (result) {
                case "ok":
                    writeSharedPreferences(declared.getNickname(), declared.getPhoneNumber());
                    break;
                case "existing":
                    writeSharedPreferences(declared.getNickname(), declared.getPhoneNumber());
                    break;
                case "taken":
                    foundMessage.setText(getString(R.string.nicknameTaken1) + declared.getNickname() + getString(R.string.nicknameTaken));
                    break;
            }
        }
    }

}
