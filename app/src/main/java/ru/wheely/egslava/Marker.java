package ru.wheely.egslava;

import com.google.android.gms.maps.model.LatLng;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by egslava on 04/08/14.
 */
public class Marker implements Serializable{
    public int id;
    public double lat;
    public double lon;

    public LatLng toLatLng(){
        return new LatLng(lat, lon);
    }

    public static ArrayList<Marker> parse(String json) {
        try {
            return new ObjectMapper().readValue(json, new TypeReference<ArrayList<Marker>>() {});
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
