package com.example.firatmap;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.SearchView;

import com.google.android.gms.maps.model.Marker;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;

public class LayoutControl {
    private static LayoutControl instance;
    LinearLayout contentLayout;
    LinearLayout departmentListLayout;
    LinearLayout mapLayout;
    LinearLayout bottomLayout;
    private LayoutControl(){
        contentLayout = MainActivity.instance.findViewById(R.id.content_layout);
        departmentListLayout = contentLayout.findViewById(R.id.departmants_layout);
        mapLayout = contentLayout.findViewById(R.id.map_layout);
        bottomLayout = MainActivity.instance.findViewById(R.id.bottom_layout);
        bottomLayout.findViewById(R.id.bottom_lay_map_button).setOnClickListener(this::OnBottomMapButtonClick);
        bottomLayout.findViewById(R.id.bottom_lay_dep_button).setOnClickListener(this::OnBottomDepButtonClick);
        loadMakerBottom();
    }
    public static LayoutControl getInstance(){
        if(instance==null){
            instance = new LayoutControl();
        }
        return instance;
    }
    public void OnBottomMapButtonClick(View view){
        openMapLayout();
    }
    public void OnBottomDepButtonClick(View view){
        openDepartmentLayout();
    }

    public void openMapLayout(){
        mapLayout.setVisibility(View.VISIBLE);
        departmentListLayout.setVisibility(View.GONE);
    }

    public void openDepartmentLayout(){
        mapLayout.setVisibility(View.GONE);
        departmentListLayout.setVisibility(View.VISIBLE);
    }

    public void loadMakerBottom(){
        MK_bottomSheetDialog = LayoutControl.createBottomSheetLayout();
        MK_textview = MK_bottomSheetDialog.findViewById(R.id.t_name);
        MK_button = MK_bottomSheetDialog.findViewById(R.id.bt_find);
    }

    BottomSheetDialog MK_bottomSheetDialog;
    ImageView MK_imageview;
    TextView MK_textview;
    Button MK_button;

    public void openMarkerBottomSheetLayout(FiratMarker marker){

        MK_textview.setText(marker.getTitle());
        ArrayList<Departments> departments = new ArrayList<>();
        for (Departments dep:marker.getDepartments()) {
            departments.add(dep);
        }
        ListView listView = MK_bottomSheetDialog.findViewById(R.id.bottom_dep_list);
        LoadDepartmentsToListView(departments,listView);

        MK_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.instance.displayTrack(marker.getLatLng());
                MK_bottomSheetDialog.dismiss();
            }
        });
        MK_bottomSheetDialog.show();
    }
    public static void loadDepartmentSearchView(ArrayAdapter<Departments> arrayAdapter){
        SearchView searchView = MainActivity.instance.findViewById(R.id.departments_search);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                /*for (int i=0;i<MainActivity.departments.size();i++) {
                    if(arrayAdapter.getItem(i).name.contains(query)){
                        arrayAdapter.
                    }
                }*/
                arrayAdapter.getFilter().filter(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                arrayAdapter.getFilter().filter(newText);
                return true;
            }
        });
    }
    public static void LoadDepartmentsToListView(ArrayList<Departments> arrayList, ListView listView){
        DepartmentListAdapter listAdapter = new DepartmentListAdapter(MainActivity.instance,arrayList);
        listView.setAdapter(listAdapter);
        listView.setClickable(true);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                System.out.println(i);
            }
        });
        loadDepartmentSearchView(listAdapter);
    }
    public static BottomSheetDialog createBottomSheetLayout(){
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(MainActivity.instance);
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_dialog);
        bottomSheetDialog.setCanceledOnTouchOutside(true);
        return bottomSheetDialog;
    }

}
