package org.cnii.testapp;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.ActionBarActivity;
import android.widget.EditText;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.StringRes;
import org.androidannotations.annotations.sharedpreferences.Pref;

import java.util.ArrayList;

/**
 * Created by egslava on 04/08/14.
 */
@EActivity(R.layout.activity_login)
public class LoginActivity extends ActionBarActivity {

    public static final int FORBIDDEN = 6; // wtf???

    @ViewById
    EditText etUsername, etPassword;

    @Bean
    Validator validator;

    @StringRes
    String invalidPassword;

    @Pref
    Prefs_ prefs;

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(getApplicationInfo().packageName + ".REFRESH_MARKERS")){
                ArrayList<Marker> markers = Marker.parse(intent.getStringExtra("message"));
                MapsActivity_.intent(context).markers(markers).start();
                unregisterReceiver(receiver);
                finish();
            }else if (intent.getAction().equals(getApplicationInfo().packageName + ".CONNECTION_IS_CLOSED")){
                int code = intent.getIntExtra("code", 0);
                if (code == FORBIDDEN){
                    showError(invalidPassword);
                }else{
                    showError(intent.getStringExtra("reason"));
                }

                disconnect();
            }
        }
    };

    @UiThread
    void disconnect() {
        NetworkService_.intent(this).disconnect().start();//
        unregisterReceiver(receiver);
    }

    @Click
    void connect() {
        String username = validator.validate(etUsername, Validator.CAN_NOT_BE_BLANK);
        String password = validator.validate(etPassword, Validator.CAN_NOT_BE_BLANK);

        if (username == null || password == null){
            return;
        }

        NetworkService_.intent(this).login(username, password).start();

        IntentFilter filter = new IntentFilter();
        filter.addAction(getApplicationInfo().packageName + ".REFRESH_MARKERS");
        filter.addAction(getApplicationInfo().packageName + ".CONNECTION_IS_CLOSED");
        registerReceiver(receiver, filter);

        prefs.edit()
                .username().put(username)
                .password().put(password);
    }

    void showError(String message){
        new AlertDialog.Builder(this)
                .setTitle(android.R.string.dialog_alert_title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

}
