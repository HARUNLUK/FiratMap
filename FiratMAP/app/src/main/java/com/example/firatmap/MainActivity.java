package com.example.firatmap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.PointOfInterest;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnPoiClickListener {

    public static MainActivity instance;
    public static ArrayList<FiratMarker> markers = new ArrayList<>();
    public static ArrayList<Departments> departments = new ArrayList<>();
    FireBaseHelper fireBaseHelper;
    GoogleMap googleMap;
    FusedLocationProviderClient fusedLocationProviderClient;
    public Location location;
    LayoutControl layoutControl;
    LatLng firatLatLng;


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
                        MainActivity.instance.location = location;
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
                    checkLocation();
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
        firatLatLng = new LatLng(38.6777569,39.1997669);
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(firatLatLng,15));
        checkLocation();
        LoadLocations();
        getLocation();
        googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this,R.raw.map_style));
    }

    public void LoadLocations(){
        fireBaseHelper = FireBaseHelper.getInstance();
        fireBaseHelper.getFireBaseLocations();
    }

    public void LoadDepartmentsToLayout(){
        ListView listView = findViewById(R.id.departments_listview);
        layoutControl.LoadDepartmentsToListView(departments,listView,ListType.LAYOUT);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        getLocation();
        FiratMarker fmarker=null;
        for(FiratMarker m: markers){
            if(marker.getSnippet().equals(Integer.toString(m.getID()))){
                fmarker = m;
            }
        }
        markerZoom(fmarker.getLatLng(),16);
        layoutControl.openMarkerBottomSheetLayout(fmarker);
        return true;
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void displayTrack(LatLng targetLatLang){
        try {
            if(location==null){
                checkLocation();
                return;
            }
            getLocation();
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

    private void checkLocation() {
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch(Exception ex) {}

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch(Exception ex) {}

        if(!gps_enabled && !network_enabled) {
            // notify user
            new AlertDialog.Builder(this)
                    .setMessage("Please Open Your Device's Location Settings")
                    .setPositiveButton("Open", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        }
                    })
                    .setNegativeButton("Cancel",null)
                    .show();
        }
    }

    @Override
    public void onPoiClick(@NonNull PointOfInterest pointOfInterest) {
        List<Departments> poidepartments = new ArrayList<Departments>();
        FiratMarker newFirat = new FiratMarker(0,pointOfInterest.latLng,pointOfInterest.name,"",null,null);
        poidepartments.add(new Departments("TestInner",newFirat.getID()));
        poidepartments.add(new Departments("TestInner",newFirat.getID()));
        poidepartments.add(new Departments("TestInner",newFirat.getID()));
        newFirat.setDepartments(poidepartments);
        fireBaseHelper.addMarker(newFirat);
    }
    public void showFacilityOnMap(int id){
        for (FiratMarker mark: markers) {
            if(id == mark.getID()){
                System.out.println("//*/*-/-*/--*"+mark.getTitle());
                layoutControl.openMapLayout();
                layoutControl.openMarkerBottomSheetLayout(mark);
                markerZoom(mark.getLatLng(),18);
            }
        }
    }

    public void markerZoom(LatLng latLng,float distance){
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,distance));
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
            }else{
                layoutControl.search(search);
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