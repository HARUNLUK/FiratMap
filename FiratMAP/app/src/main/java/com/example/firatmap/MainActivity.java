package com.example.firatmap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.PointOfInterest;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnPoiClickListener {

    public static MainActivity instance;
    public static ArrayList<FiratMarker> markers = new ArrayList<>();
    public static ArrayList<Departments> departments = new ArrayList<>();
    FireBaseHelper fireBaseHelper;
    GoogleMap googleMap;
    FusedLocationProviderClient fusedLocationProviderClient;
    String locationAddress;
    Location location;
    LayoutControl layoutControl;
    LatLng firatlatLng;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(instance != null){
            return;
        }
        instance=this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        layoutControl = LayoutControl.getInstance();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true);
            fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                @Override
                public void onComplete(Task<Location> task) {
                    location = task.getResult();
                    if (location != null) {
                        Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
                        try {
                            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(),1);
                            locationAddress = addresses.get(0).getAddressLine(0);
                            Toast.makeText(MainActivity.this, locationAddress, Toast.LENGTH_LONG).show();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            return;
        } else {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);
        }

    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 44: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getLocation();
                    googleMap.setMyLocationEnabled(true);
                }
                return;
            }

        }
    }
    @Override
    public void onMapReady(GoogleMap _googleMap) {
        googleMap = _googleMap;
        googleMap.setOnMarkerClickListener(this);
        googleMap.setOnPoiClickListener(this);
        firatlatLng = new LatLng(38.6777569,39.1997669);
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(firatlatLng,16));
        LoadLocations();
        getLocation();
    }

    public void LoadLocations(){
        fireBaseHelper = FireBaseHelper.getInstance();
        fireBaseHelper.getFireBaseLocations();
    }

    public void LoadDepartmentsToLayout(){
        ListView listView = findViewById(R.id.departments_listview);
        LayoutControl.LoadDepartmentsToListView(departments,listView);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        FiratMarker fmarker=null;
        for(FiratMarker m: markers){
            if(marker.getSnippet().equals(Integer.toString(m.getID()))){
                fmarker = m;
            }
        }

        layoutControl.openMarkerBottomSheetLayout(fmarker);
        return false;
    }
    public void displayTrack(LatLng targetLatLang){
        try {
            getLocation();
            Toast.makeText(this,locationAddress+"",Toast.LENGTH_LONG).show();
            Uri uri = Uri.parse("https://www.google.co.in/maps/dir/"+location.getLatitude()+
                    ","+location.getLongitude()+
                    "/"+targetLatLang.latitude+","+targetLatLang.longitude);
            Intent intent = new Intent(Intent.ACTION_VIEW,uri);
            intent.setPackage("com.google.android.apps.maps");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }catch (ActivityNotFoundException e){
            Uri uri = Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.apps.maps");
            Intent intent = new Intent(Intent.ACTION_VIEW,uri);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    @Override
    public void onPoiClick(@NonNull PointOfInterest pointOfInterest) {
        List<Departments> poidepartments = new ArrayList<Departments>();
        FiratMarker newFirat = new FiratMarker(0,pointOfInterest.latLng,pointOfInterest.name,null,null);
        departments.add(new Departments("TestInner",newFirat.getID()));
        departments.add(new Departments("TestInner",newFirat.getID()));
        departments.add(new Departments("TestInner",newFirat.getID()));
        newFirat.setDepartments(poidepartments);
        fireBaseHelper.addMarker(newFirat);
    }
    public void showFacilityOnMap(int id){
        for (FiratMarker mark: markers) {
            if(id == mark.getID()){
                System.out.println("//*/*-/-*/--*"+mark.getTitle());
                layoutControl.openMapLayout();
                layoutControl.openMarkerBottomSheetLayout(mark);
            }
        }
    }

    public void speak(View view) {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,"Start Speaking");
        startActivityForResult(intent,100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        List<Departments> SearchedDepartments = new ArrayList<>();
        if(requestCode == 100 && resultCode == RESULT_OK){
            String search = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS).get(0);
            for (Departments dep: departments) {
                if(englishConvert(dep.name).toUpperCase().contains(englishConvert(search.toUpperCase()))){
                    SearchedDepartments.add(dep);
                }
            }
            if(SearchedDepartments.size() == 1){
                showFacilityOnMap(SearchedDepartments.get(0).parent);
            }
        }
    }
    public static String englishConvert(String text){
        String[] charsI = new String[]{"İ","i","Ü","ü","Ö","ö","Ç","ç","Ğ","ğ","Ş","ş"};
        String[] charsA = new String[]{"I","ı","U","u","O","o","C","c","G","g","S","s"};
        for (int i=0;i<charsI.length;i++) {
            text=text.replaceAll(charsI[i],charsA[i]);
        }
        return text;
    }
}