package org.cnii.testapp;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.EIntentService;
import org.androidannotations.annotations.EService;
import org.androidannotations.annotations.ServiceAction;
import org.androidannotations.annotations.SystemService;

import de.tavendo.autobahn.WebSocket;
import de.tavendo.autobahn.WebSocketConnection;
import de.tavendo.autobahn.WebSocketException;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */

@EIntentService
public class NetworkService extends IntentService
        implements WebSocket.ConnectionHandler, LocationListener{

    private WebSocketConnection connection;

    public NetworkService() {
        super("NetworkService");
    }

    volatile String username, password;

    @SystemService
    LocationManager locationManager;

    @AfterInject
    void init(){
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
    }

    @ServiceAction
    void login(String user, String pass){

        try {
            username = user;
            password = pass;

            if (connection != null && connection.isConnected()){
                return;
            }
            connection = new WebSocketConnection();

            String url = String.format("ws://mini-mdt.wheely.com?username=%s&password=%s", username, password);
            connection.connect(url, this);

            while(username != null && password != null){
                Thread.sleep(3000);
            }

            disconnect();
        } catch (WebSocketException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @ServiceAction
    void disconnect(){
        username = null;
        password = null;

        if (connection != null){
            if (connection.isConnected()){
                connection.disconnect();
            }
            connection = null;
        }
    }

    @ServiceAction
    void sendPosition(LatLng position){
        String message = String.format("{\"lat\": %s, \"lon\": %s}", position.latitude, position.longitude);
        connection.sendTextMessage(message);
    }

    @Override protected void onHandleIntent(Intent intent) {}

    @Override
    public void onOpen() {
        Log.d("WebSocket", "Connection is opened");
        sendCurrentPosition();
    }

    private void sendCurrentPosition() {
        Location lkl = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if (lkl == null){
            return;
        }
        LatLng currentPos = new LatLng(lkl.getLatitude(), lkl.getLongitude());
        sendPosition(currentPos);
    }

    @Override
    public void onClose(int code, String reason) {
        try {
            Log.d("WebSocket - onClose", "Connection is closed: " + reason);
            Intent intent = new Intent(getApplicationInfo().packageName + ".CONNECTION_IS_CLOSED");
            intent.putExtra("code", code);
            intent.putExtra("reason", reason);
            sendBroadcast(intent);

            Thread.sleep(5000);

            if(code != LoginActivity.FORBIDDEN){
                connection.reconnect();
                Log.d("WebSocket", "Reconnecting");
            }else{
                disconnect();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onTextMessage(String payload) {
        Intent intent = new Intent(getApplicationInfo().packageName + ".REFRESH_MARKERS");
        intent.putExtra("message", payload);
        sendBroadcast(intent);
        Log.d("WebSocket - onTextMessage", payload);

    }

    @Override
    public void onRawTextMessage(byte[] payload) {}

    @Override
    public void onBinaryMessage(byte[] payload) {}


    @Override
    public void onLocationChanged(Location location) {
        if (connection != null && connection.isConnected()){
            sendPosition(new LatLng(location.getLatitude(), location.getLongitude()));
        }
    }

    @Override
    public boolean stopService(Intent name) {
        disconnect();
        return super.stopService(name);
    }

    @Override public void onStatusChanged(String provider, int status, Bundle extras) {}

    @Override public void onProviderEnabled(String provider) {}

    @Override public void onProviderDisabled(String provider) {}
}
