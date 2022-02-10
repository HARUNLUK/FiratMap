package com.example.firatmap;
import android.graphics.Bitmap;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;
import java.util.Random;

public class FiratMarker{
    private int ID;
    private Marker marker;
    private LatLng latLng;
    private String title;
    private String description;
    private List<Departments> departments;
    private Bitmap Icon;
    public FiratMarker(int ID, LatLng latLng, String name,String description ,Bitmap markerIcon, List<Departments> departments) {
        if (ID==0){
            this.ID = findId();
        }else{
            this.ID = ID;
        }
        this.description = description;
        this.latLng = latLng;
        this.title = name;
        this.departments = departments;
    }
    public MarkerOptions getMarkerOption(){

        MarkerOptions markerOption = new MarkerOptions()
                .title(title)
                .snippet(Integer.toString(ID))
                .position(latLng)
                .anchor(0.5f, 0.5f);

        return markerOption;
    }
    @Override
    public String toString() {
        return "dftMarker{" +
                "latLng=" + latLng +
                ", name='" + title + '\'' +
                '}';
    }
    public int findId(){
        int ID;
        do {
            ID = randomFromTo(1000,10000);
        }while (isIDFound(ID) != null);
        return ID;
    }
    int randomFromTo(int a, int b){
        Random rnd = new Random();
        return rnd.nextInt(b-a)+a;
    }
    public FiratMarker isIDFound(int ID){
        for (FiratMarker firatMarker : MainActivity.markers){
            if(firatMarker.ID == ID){
                return firatMarker;
            }
        }
        return null;
    }
    public int getID() {
        return ID;
    }
    public void setID(int ID) {
        this.ID = ID;
    }
    public Marker getMarker() {
        return marker;
    }
    public void setMarker(Marker marker) {
        this.marker = marker;
    }
    public LatLng getLatLng() {
        return latLng;
    }
    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String name) {
        this.title = name;
    }
    public List<Departments> getDepartments() {
        return departments;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public void setDepartments(List<Departments> departments) {
        this.departments = departments;
    }

    public Bitmap getIcon() {
        return Icon;
    }

    public void setIcon(Bitmap icon) {
        marker.setIcon(BitmapDescriptorFactory.fromBitmap(icon));
        Icon = icon;
    }
}
