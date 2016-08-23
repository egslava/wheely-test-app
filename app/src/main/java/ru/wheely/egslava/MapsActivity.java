package ru.wheely.egslava;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.FragmentById;
import org.androidannotations.annotations.SystemService;
import org.androidannotations.annotations.res.StringRes;

import java.util.ArrayList;

@EActivity(R.layout.activity_maps)
public class MapsActivity extends FragmentActivity  {

    @FragmentById
    SupportMapFragment mapFragment;

    @StringRes
    String i;

    @Extra
    ArrayList<Marker> markers;

    private GoogleMap map; // Might be null if Google Play services APK is not available.

    @AfterViews
    public void init(){
        setUpMapIfNeeded();
    }

    @Override
    protected void onResume() {
        registerReceiver(markerUpdater, new IntentFilter(getApplicationInfo().packageName + ".REFRESH_MARKERS"));
        super.onResume();
    }

    @Override
    protected void onPause() {
        unregisterReceiver(markerUpdater);
        super.onPause();
    }

    @SystemService
    LocationManager     locationManager;

    BroadcastReceiver markerUpdater = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("message");
            markers = Marker.parse(message);
            setUpMap(false);
        }
    };

    private void setUpMapIfNeeded() {
        if (map == null) {
            map = mapFragment.getMap();
            if (map != null) {
                setUpMap(true);
            }
        }
    }

    private void setUpMap(boolean animate) {
        map.clear();
        Location lkl = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        LatLng currentPos = new LatLng(lkl.getLatitude(), lkl.getLongitude());
        map.addMarker(new MarkerOptions().position(currentPos).title(i));

        if (animate){
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentPos, 10));
        }

        if (markers == null) {
            return;
        }
        for(Marker marker : markers){
            map.addMarker(new MarkerOptions().position(marker.toLatLng()).title(String.valueOf(marker.id)));
        }
    }
}
