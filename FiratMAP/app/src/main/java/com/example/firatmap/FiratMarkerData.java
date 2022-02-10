package com.example.firatmap;

import java.util.ArrayList;
import java.util.List;

public class FiratMarkerData {
    public int id;
    public String title;
    public String description;
    public double latitude;
    public double longitude;
    public String icon;
    public List<Departments> departments;

    public FiratMarkerData(int id, String title,String description, float latitude, float longitude, List<Departments> departmants) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
        this.icon = id+".png";
        this.departments = departmants;
    }
    public FiratMarkerData(FiratMarker marker) {
        this.id = marker.getID();
        this.title = marker.getTitle();
        this.description = marker.getDescription();
        this.latitude = marker.getLatLng().latitude;
        this.longitude = marker.getLatLng().longitude;
        this.icon = marker.getID()+".png";
        this.departments = marker.getDepartments();
    }


}
