package com.example.firatmap;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
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
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    FirebaseStorage firebaseStorage;
    StorageReference storageReferenceLocationIcons;

    private FireBaseHelper() {
        firebaseDatabase = FirebaseDatabase.getInstance(dataBaseURL);
        databaseReference = firebaseDatabase.getReference();
        databaseReference.child("TeknoF").setValue("added");
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
        ref.child(Integer.toString(marker.getID())).setValue(data);
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

                        List<Departments> departments = new ArrayList<Departments>();

                        for (DataSnapshot data: c.child("departments").getChildren()){
                            Departments department = new Departments(data.child("name").getValue().toString(),key);
                            departments.add(department);
                            MainActivity.departments.add(department);
                        }

                        FiratMarker marker = new FiratMarker(key,latLng,name,null,departments);
                        MainActivity.markers.add(marker);
                    }

                    MainActivity.instance.LoadDepartmentsToLayout();
                    for(FiratMarker marker : MainActivity.markers){
                        getIconFromFirebase(marker);
                        marker.setMarker(MainActivity.instance.googleMap.addMarker(marker.getMarkerOption()));
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
                        bitmap = Bitmap.createScaledBitmap(bitmap, width, height, false);
                        marker.getMarker().setIcon(BitmapDescriptorFactory.fromBitmap(bitmap));
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
