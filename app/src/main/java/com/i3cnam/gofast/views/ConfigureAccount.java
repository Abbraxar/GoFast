package com.i3cnam.gofast.views;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
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
import com.i3cnam.gofast.communication.GofastCommunicationException;
import com.i3cnam.gofast.model.User;

import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.URLEncoder;

public class ConfigureAccount extends AppCompatActivity {
    private String mPhoneNumber;
    private boolean declareUser = true;
    private EditText nicknameEdit;
    private TextView foundMessage;
    private EditText phoneNumberField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configure_account);
        // get the telephone number
        TelephonyManager tMgr = (TelephonyManager)this.getSystemService(Context.TELEPHONY_SERVICE);
        mPhoneNumber = tMgr.getLine1Number();
        // get the telephone number field
        phoneNumberField = (EditText) findViewById(R.id.phoneNumberField);
        // fill the telephone number field
        phoneNumberField.setText(mPhoneNumber);
        // get the nickname field
        nicknameEdit = (EditText) findViewById(R.id.editNickname);
        // get the message field
        foundMessage = (TextView) findViewById(R.id.foundMessage);
        new TaskRetrieveAccount(this).execute(mPhoneNumber);
    }

    public void recordUser(View view) {
        if (nicknameEdit.getText().toString().equals("")) {
            Toast.makeText(ConfigureAccount.this, R.string.empyNicknameMsg, Toast.LENGTH_SHORT).show();
        }
        else {
            try {
                String nickname  = URLEncoder.encode(nicknameEdit.getText().toString(), "UTF-8");
                String phoneNumber  = URLEncoder.encode(phoneNumberField.getText().toString(), "UTF-8");
                if (declareUser) {
                    new TaskCreateAccount(this).execute(nickname, phoneNumber);
                }
                else {
                    writeSharedPreferences(nickname, phoneNumber);
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    }

    private void writeSharedPreferences(String nickname, String phoneNumber){
        SharedPreferences prefs = getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE);
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
        Context context;
        boolean error = false;

        public TaskRetrieveAccount(Context context) {
            this.context = context;
        }

        protected String doInBackground(String... urls) {
            // call the service to know is the phone number is already registered
            CommInterface comm = new Communication();
            try {
                return comm.retrieveAccount(mPhoneNumber);
            } catch (GofastCommunicationException e) {
                error = true;
                return null;
            }
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
            if (error) {
                new AlertDialog.Builder(context)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle(R.string.serverUnavailableTitle)
                        .setMessage(getString(R.string.serverUnavailable))
                        .setPositiveButton(R.string.ok, null)
                        .show();
            }
        }
    }

    private class TaskCreateAccount extends AsyncTask<String, String,String> {
        private User declared;
        Context context;
        boolean error = false;

        public TaskCreateAccount(Context context) {
            this.context = context;
        }

        protected String doInBackground(String... urls) {
            // call the service to know is the phone number is already registered
            CommInterface comm = new Communication();
            declared = new User(urls[0],urls[1]);
            try {
                return comm.declareUser(declared);
            } catch (GofastCommunicationException e) {
                error = true ;
                return null;
            }
        }
        protected void onPostExecute(String result) {

            if (error) {
                new AlertDialog.Builder(context)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle(R.string.serverUnavailableTitle)
                        .setMessage(getString(R.string.serverUnavailable))
                        .setPositiveButton(R.string.ok, null)
                        .show();
            }
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
