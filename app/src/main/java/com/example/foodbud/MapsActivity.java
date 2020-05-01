package com.example.foodbud;

import android.os.Bundle;

import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;


        checkResturants();
    }

    public String loadJSONFromAsset() {
        String json = null;
        try {
            InputStream is = MapsActivity.this.getAssets().open("data.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    public void checkResturants() {

        try {
            JSONArray arr = new JSONArray(loadJSONFromAsset());


            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);


                if (Appconstants.RESTURANT_TYPE.equals(obj.getString("keyword"))) {


                    double latitude = Double.parseDouble(obj.getString("lat"));
                    double longitude = Double.parseDouble(obj.getString("lon"));


                    LatLng position = new LatLng(latitude, longitude);

                    mMap.addMarker(new MarkerOptions()
                            .position(position)
                            .title(obj.getString("resname"))
                            .snippet(obj.getString("tag")));

                    if (Appconstants.TEXTCAPTURED.equals(obj.getString("resname"))) {
                        mMap.addMarker(new MarkerOptions()
                                .position(position)
                                .title(obj.getString("resname"))
                                .snippet(obj.getString("tag"))).showInfoWindow();


                    }
                    CameraUpdate location = CameraUpdateFactory.newLatLngZoom(
                            position, 15);
                    mMap.animateCamera(location);

                }
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

}