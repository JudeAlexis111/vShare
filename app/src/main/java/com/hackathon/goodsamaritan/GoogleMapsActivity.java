package com.hackathon.goodsamaritan;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Debug;
import android.util.Log;
import android.view.InflateException;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.compat.Place;
import com.google.android.libraries.places.compat.ui.PlaceAutocompleteFragment;
import com.google.android.libraries.places.compat.ui.PlaceSelectionListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class GoogleMapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    Location currentLocation;
    Bitmap smallMarker;
    LatLng addMarker;
    Place Marker;
    FusedLocationProviderClient fusedLocationProviderClient;
    private static final int REQUEST_CODE = 101;
    private static View view;
    public boolean Init = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        final SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        //mapFragment.getMapAsync(this);
        final Button ShowDialog = (Button) findViewById(R.id.button2);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        fetchLastLocation();

        final FirebaseDatabase database = FirebaseDatabase.getInstance();

        ShowDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder mBuilder = new AlertDialog.Builder(GoogleMapsActivity.this);
                if(view != null){
                    ViewGroup parent = (ViewGroup) view.getParent();
                    if(parent != null){
                        parent.removeView(view);
                    }
                }
                try {
                    view = getLayoutInflater().inflate(R.layout.dialog_make_icon, null);
                } catch (InflateException e) {
                    //e.printStackTrace();
                }

                mBuilder.setView(view);
                final AlertDialog dialog = mBuilder.create();
                dialog.show();

                Button con = (Button) view.findViewById(R.id.button3);
                final Spinner spinner = (Spinner) view.findViewById(R.id.spinner2);
                final EditText editText = (EditText) view.findViewById(R.id.editText3) ;

                PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

                autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                    @Override
                    public void onPlaceSelected(Place place) {
                        addMarker = place.getLatLng();
                        Marker = place;
                    }

                    @Override
                    public void onError(Status status) {

                    }
                });

                con.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //mapFragment.getMapAsync(GoogleMapsActivity.this);
                        if(addMarker != null) {
                            DatabaseReference myRef = database.getReference((String) Marker.getName() + "/lat");
                            myRef.setValue(addMarker.latitude);
                            myRef = database.getReference((String) Marker.getName() + "/long");
                            myRef.setValue(addMarker.longitude);
                            myRef = database.getReference((String) Marker.getName() + "/type");
                            myRef.setValue(spinner.getSelectedItem().toString());
                            dialog.dismiss();
                            myRef = database.getReference((String) Marker.getName() + "/notes");
                            myRef.setValue(editText.getText().toString());
                            dialog.dismiss();
                        }
                        else{
                            Toast.makeText(getApplicationContext(), "Fill Out All Fields", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }
        });

        DatabaseReference myRef = database.getReference();

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d("hi", String.valueOf(dataSnapshot.getChildrenCount()));
                if(Init) {
                    mMap.clear();

                    for (DataSnapshot post : dataSnapshot.getChildren()) {
                        if (post.child("type").getValue(String.class) != null) {
                            Log.d("hi", post.child("type").getValue(String.class));
                            //Log.d("hi", String.valueOf(post.child("type").getValue(String.class).equals("Food")));

                            if(post.child("type").getValue(String.class).equals("Protective Gear")){
                                BitmapDrawable bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.drawable.mask);
                                Bitmap b = bitmapdraw.getBitmap();
                                Bitmap smallMarker = Bitmap.createScaledBitmap(b, 200, 200, false);
                                mMap.addMarker(new MarkerOptions().position(new LatLng(post.child("lat").getValue(Double.class), post.child("long").getValue(Double.class))).title(post.getKey()).icon(BitmapDescriptorFactory.fromBitmap(smallMarker)).snippet(post.child("notes").getValue(String.class)));
                            }
                            else if(post.child("type").getValue(String.class).equals("Toilet Paper")){
                                BitmapDrawable bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.drawable.paper);
                                Bitmap b = bitmapdraw.getBitmap();
                                Bitmap smallMarker = Bitmap.createScaledBitmap(b, 100, 100, false);
                                mMap.addMarker(new MarkerOptions().position(new LatLng(post.child("lat").getValue(Double.class), post.child("long").getValue(Double.class))).title(post.getKey()).icon(BitmapDescriptorFactory.fromBitmap(smallMarker)).snippet(post.child("notes").getValue(String.class)));
                            }
                            else if(post.child("type").getValue(String.class).equals("Food")){
                                BitmapDrawable bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.drawable.pizza);
                                Bitmap b = bitmapdraw.getBitmap();
                                Bitmap smallMarker = Bitmap.createScaledBitmap(b, 140, 140, false);
                                mMap.addMarker(new MarkerOptions().position(new LatLng(post.child("lat").getValue(Double.class), post.child("long").getValue(Double.class))).title(post.getKey()).icon(BitmapDescriptorFactory.fromBitmap(smallMarker)).snippet(post.child("notes").getValue(String.class)));
                            }
                            else if(post.child("type").getValue(String.class).equals("Medical Supplies")){
                                BitmapDrawable bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.drawable.aid);
                                Bitmap b = bitmapdraw.getBitmap();
                                Bitmap smallMarker = Bitmap.createScaledBitmap(b, 140, 140, false);
                                mMap.addMarker(new MarkerOptions().position(new LatLng(post.child("lat").getValue(Double.class), post.child("long").getValue(Double.class))).title(post.getKey()).icon(BitmapDescriptorFactory.fromBitmap(smallMarker)).snippet(post.child("notes").getValue(String.class)));
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void fetchLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
            return;
        }
        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if(location != null){
                    currentLocation = location;
                    Toast.makeText(getApplicationContext(), currentLocation.getLatitude() + " " + currentLocation.getLongitude(), Toast.LENGTH_SHORT).show();
                    SupportMapFragment supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
                    supportMapFragment.getMapAsync(GoogleMapsActivity.this);
                }
            }
        });
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        //fetchLastLocation();
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        //mMap.addMarker(new MarkerOptions().position(latLng).title("Marker in Sydney"));

        if(addMarker != null){
            mMap.addMarker(new MarkerOptions().position(addMarker).title("Marker in Place"));
        }

        if(currentLocation != null){
            LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13));
        }


        Init = true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case REQUEST_CODE:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    fetchLastLocation();
                }

        }
    }




}
