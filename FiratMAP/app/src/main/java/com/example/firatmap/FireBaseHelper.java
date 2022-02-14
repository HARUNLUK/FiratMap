package com.example.firatmap;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.RuntimeExecutionException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FireBaseHelper {
    public static FireBaseHelper instance;
    private String dataBaseURL = "https://firatmap-default-rtdb.europe-west1.firebasedatabase.app/";
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReferenceLocationIcons;

    private FireBaseHelper() {
        firebaseDatabase = FirebaseDatabase.getInstance(dataBaseURL);
        databaseReference = firebaseDatabase.getReference();
        firebaseStorage = FirebaseStorage.getInstance("gs://firatmap.appspot.com");
        storageReferenceLocationIcons = firebaseStorage.getReference();
    }
    public static FireBaseHelper getInstance(){
        if(instance==null){
            instance = new FireBaseHelper();
        }
        return instance;
    }
    public void addMarker(FiratMarker marker){
        boolean found=false;
        for (FiratMarker mar:MainActivity.markers) {
            if (mar.getTitle().equals(marker.getTitle())){
                found =true;
            }
        }
        if(found){
            return;
        }
        MainActivity.markers.add(marker);
        MainActivity.instance.googleMap.addMarker(marker.getMarkerOption());
        DatabaseReference ref = databaseReference.child("Locations");
        FiratMarkerData data = new FiratMarkerData(marker);
        DatabaseReference markerRef = ref.child(Integer.toString(marker.getID()));
        markerRef.setValue(data);
        for (int i=0;i<data.departments.size();i++){
            markerRef.child("departments").child(Integer.toString(i)).setValue(data.departments.get(i).name);
        }
    }
    public void addMarkers(List<FiratMarker> markers){
        for (FiratMarker marker:markers) {
            addMarker(marker);
        }
    }
    public void getFireBaseLocations(){
        DatabaseReference ref = databaseReference.child("Locations");
        ref.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> _task) {
                try {
                    Iterable<DataSnapshot> task = _task.getResult().getChildren();
                    for (DataSnapshot c : task) {
                        Double latitude=Double.parseDouble(c.child("latitude").getValue().toString());
                        Double longitude=Double.parseDouble(c.child("longitude").getValue().toString());
                        LatLng latLng = new LatLng(latitude,longitude);
                        int key = Integer.parseInt(c.getKey().toString());
                        String name =  c.child("title").getValue().toString();
                        String desciption="";

                        try{
                            desciption = c.child("description").getValue().toString();
                        }catch (Exception e){
                            e.printStackTrace();
                        }

                        if(name.contains("\n")){
                            name = name.split("\n")[1];
                        }
                        List<Departments> departments = new ArrayList<Departments>();

                        for (DataSnapshot data: c.child("departments").getChildren()){
                            Departments department = new Departments(data.getValue().toString(),key);
                            departments.add(department);
                            MainActivity.departments.add(department);
                        }
                        if(departments.size()==0){
                            MainActivity.departments.add(new Departments(name,key));
                        }
                        FiratMarker marker = new FiratMarker(key,latLng,name,desciption,null,departments);
                        MainActivity.markers.add(marker);
                    }

                    MainActivity.instance.LoadDepartmentsToLayout();
                    for(FiratMarker marker : MainActivity.markers){
                        getIconFromFirebase(marker);
                        Marker nMaker =  MainActivity.instance.googleMap.addMarker(marker.getMarkerOption());

                        marker.setMarker(nMaker);
                        marker.getMarker().setIcon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView(marker.getTitle(),null)));
                    }
                }catch (RuntimeExecutionException e){
                    getFireBaseLocations();
                }
            }
        });
    }
    public void getIconFromFirebase(FiratMarker marker){
        System.out.println(marker.getID());
        StorageReference locationIconRef = storageReferenceLocationIcons.child("Locaiton_Icons/"+marker.getID()+".png");
        try {
            final File picFile = File.createTempFile(Integer.toString(marker.getID()),"png");
            locationIconRef.getFile(picFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    Bitmap bitmap = BitmapFactory.decodeFile(picFile.getAbsolutePath());
                    if(bitmap != null && marker !=null){
                        int height = 130;
                        int width = 130;
                        System.out.println("++"+bitmap);
                        bitmap = Bitmap.createScaledBitmap(bitmap, width, height, false);
                        marker.setIcon(bitmap);
                        marker.getMarker().setIcon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView(marker.getTitle(),bitmap)));
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    System.out.println(marker.getID()+"****" +e);
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    private Bitmap getMarkerBitmapFromView(String title,Bitmap btm) {
        View customMarkerView = (LayoutInflater.from(MainActivity.instance.getApplicationContext()).inflate(R.layout.custom_marker_view, null));

        TextView text = (TextView) customMarkerView.findViewById(R.id.customText);
        text.setText(title);
        if(btm!=null){
            ImageView markerImageView = (ImageView) customMarkerView.findViewById(R.id.customImg);
            markerImageView.setImageBitmap(btm);
        }
        customMarkerView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        customMarkerView.layout(0, 0, customMarkerView.getMeasuredWidth(), customMarkerView.getMeasuredHeight());
        customMarkerView.buildDrawingCache();
        Bitmap returnedBitmap = Bitmap.createBitmap(customMarkerView.getMeasuredWidth(), customMarkerView.getMeasuredHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        canvas.drawColor(Color.WHITE, PorterDuff.Mode.SRC_IN);
        Drawable drawable = customMarkerView.getBackground();
        if (drawable != null)
            drawable.draw(canvas);
        customMarkerView.draw(canvas);
        return returnedBitmap;
    }
    public void setImageView(ImageView imageView, String imageName){
        StorageReference locationIconRef = storageReferenceLocationIcons.child("Locaiton_Icons/"+imageName);
        try{
            final File picFile = File.createTempFile(imageName,"png");
            locationIconRef.getFile(picFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    Bitmap bitmap = BitmapFactory.decodeFile(picFile.getAbsolutePath());
                    if (bitmap!=null){
                        imageView.setImageBitmap(bitmap);
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    System.out.println(e);
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
