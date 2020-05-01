package com.example.foodbud;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextDetector;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    int PERMISSION_ID = 44;
    FusedLocationProviderClient mFusedLocationClient;
    private Button captureImgBtn, detectTxtBtn, viewMap;
    private ImageView imgView;
    private TextView txtView;
    private Bitmap imageBitmap;
    private LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            Location mLastLocation = locationResult.getLastLocation();

            Appconstants.LATITUDE = mLastLocation.getLatitude();
            Appconstants.LONGITUDE = mLastLocation.getLongitude();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewMap = findViewById(R.id.viewmap);
        captureImgBtn = findViewById(R.id.capture_img_btn);
        detectTxtBtn = findViewById(R.id.detect_image_txt_btn);
        imgView = findViewById(R.id.image_view);
        txtView = findViewById(R.id.text_display);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        getLastLocation();


        captureImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
                txtView.setText("");
                detectTxtBtn.setVisibility(View.VISIBLE);

            }
        });

        detectTxtBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                detectTextFromImg();
            }
        });


        viewMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Appconstants.TEXTCAPTURED != "NA") {
                    Intent ii = new Intent(MainActivity.this, MapsActivity.class);
                    ii.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(ii);
                    finish();
                } else {
                    txtView.setText("Invalid Input");
                }
            }
        });
        viewMap.setVisibility(View.INVISIBLE);
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            imageBitmap = (Bitmap) extras.get("data");
            imgView.setImageBitmap(imageBitmap);
        }
    }

    private void detectTextFromImg() {

        FirebaseVisionImage firebaseVisionImage = FirebaseVisionImage.fromBitmap(imageBitmap);
        FirebaseVisionTextDetector firebaseVisionTextDetector = FirebaseVision.getInstance().getVisionTextDetector();
        firebaseVisionTextDetector.detectInImage(firebaseVisionImage).addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
            @Override
            public void onSuccess(FirebaseVisionText firebaseVisionText) {
                detectTextFromImage(firebaseVisionText);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, "Error:" + e.getMessage(), Toast.LENGTH_SHORT).show();

                Log.d("Error:", e.getMessage());
            }
        });
    }

    private void detectTextFromImage(FirebaseVisionText firebaseVisionText) {

        List<FirebaseVisionText.Block> blocklist = firebaseVisionText.getBlocks();
        if (blocklist.size() == 0) {
            txtView.setText("Retake Image");

        } else {
            for (FirebaseVisionText.Block block : firebaseVisionText.getBlocks()) {
                Appconstants.TEXTCAPTURED = block.getText();

                txtView.setText(Appconstants.TEXTCAPTURED);
                viewMap.setVisibility(View.VISIBLE);


                if (Appconstants.TEXTCAPTURED.equals("Indian") ||
                        Appconstants.TEXTCAPTURED.equals("Tandoor Garden") ||
                        Appconstants.TEXTCAPTURED.equals("Punjab Tandoor") ||
                        Appconstants.TEXTCAPTURED.equals("Curry Hut")) {

                    Appconstants.RESTURANT_TYPE = "Indian";

                } else if (Appconstants.TEXTCAPTURED.equals("Chinese") ||
                        Appconstants.TEXTCAPTURED.equals("Panda Express") ||
                        Appconstants.TEXTCAPTURED.equals("Quickly") ||
                        Appconstants.TEXTCAPTURED.equals("Noodle St.")) {

                    Appconstants.RESTURANT_TYPE = "Chinese";

                } else if (Appconstants.TEXTCAPTURED.equals("Italian") ||
                        Appconstants.TEXTCAPTURED.equals("Angelo's and Vinci's Ristorante") ||
                        Appconstants.TEXTCAPTURED.equals("Roman Cucina") ||
                        Appconstants.TEXTCAPTURED.equals("Brunos Italian Kitchen")) {

                    Appconstants.RESTURANT_TYPE = "Italian";

                } else {
                    Appconstants.TEXTCAPTURED = "NA";
                }

            }
        }
    }

    @SuppressLint("MissingPermission")
    private void getLastLocation() {
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                mFusedLocationClient.getLastLocation().addOnCompleteListener(
                        new OnCompleteListener<Location>() {
                            @Override
                            public void onComplete(@NonNull Task<Location> task) {
                                Location location = task.getResult();
                                if (location == null) {
                                    requestNewLocationData();
                                } else {

                                    Appconstants.LATITUDE = location.getLatitude();
                                    Appconstants.LONGITUDE = location.getLongitude();

                                }
                            }
                        }
                );

            } else {
                Toast.makeText(this, "Turn on location", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }

        } else {
            requestPermissions();
        }
    }

    @SuppressLint("MissingPermission")
    private void requestNewLocationData() {

        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(0);
        mLocationRequest.setFastestInterval(0);
        mLocationRequest.setNumUpdates(1);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mFusedLocationClient.requestLocationUpdates(
                mLocationRequest, mLocationCallback,
                Looper.myLooper()
        );

    }

    private boolean checkPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        return false;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                PERMISSION_ID
        );
    }

    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
                LocationManager.NETWORK_PROVIDER
        );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_ID) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (checkPermissions()) {
            getLastLocation();
        }

    }


}