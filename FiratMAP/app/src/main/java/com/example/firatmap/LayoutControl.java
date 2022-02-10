package com.example.firatmap;

import android.graphics.Bitmap;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

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
        MainActivity.instance.LoadDepartmentsToLayout();
    }

    public void loadMakerBottom(){
        MK_bottomSheetDialog = LayoutControl.createBottomSheetLayout();
        MK_textview = MK_bottomSheetDialog.findViewById(R.id.t_name);
        MK_button = MK_bottomSheetDialog.findViewById(R.id.bt_find);
        MK_description = MK_bottomSheetDialog.findViewById(R.id.bottom_sheet_description);
        MK_imageview = MK_bottomSheetDialog.findViewById(R.id.bottom_sheet_img);
    }

    BottomSheetDialog MK_bottomSheetDialog;
    ImageView MK_imageview;
    TextView MK_textview;
    TextView MK_description;
    ImageButton MK_button;
    EditText searchView;

    public void openMarkerBottomSheetLayout(FiratMarker marker){
        MK_textview.setText(marker.getTitle());
        MK_description.setText(marker.getDescription());
        Bitmap btm = marker.getIcon();
        if (btm!=null) {
            MK_imageview.setImageBitmap(btm);
        }else{
            MK_imageview.setImageResource(R.drawable.ic_facility);
        }

        ArrayList<Departments> departments = new ArrayList<>();

        for (Departments dep:marker.getDepartments()) {
            departments.add(dep);
        }

        if(departments.size() == 0){
            MK_bottomSheetDialog.findViewById(R.id.bottom_sheet_departments).setVisibility(View.GONE);
        }else{
            MK_bottomSheetDialog.findViewById(R.id.bottom_sheet_departments).setVisibility(View.VISIBLE);
        }

        ListView listView = MK_bottomSheetDialog.findViewById(R.id.bottom_dep_list);
        LoadDepartmentsToListView(departments,listView,ListType.BOTTOM_VIEW);

        MK_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.instance.displayTrack(marker.getLatLng());
                MK_bottomSheetDialog.dismiss();
            }
        });
        MK_bottomSheetDialog.show();
    }
    public void loadDepartmentSearchView(ArrayAdapter<Departments> arrayAdapter){
        searchView = MainActivity.instance.findViewById(R.id.departments_search);
        searchView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                arrayAdapter.getFilter().filter(charSequence);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }
    public void search(String text){
        openDepartmentLayout();
        searchView.setText(text);
    }
    public void LoadDepartmentsToListView(ArrayList<Departments> arrayList, ListView listView,ListType listType){
        DepartmentListAdapter listAdapter = new DepartmentListAdapter(MainActivity.instance,arrayList,listType);
        listView.setAdapter(listAdapter);
        listView.setClickable(true);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                MainActivity.instance.showFacilityOnMap(((Departments)adapterView.getItemAtPosition(i)).parent);
            }
        });
        loadDepartmentSearchView(listAdapter);
    }
    public static BottomSheetDialog createBottomSheetLayout(){
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(
                MainActivity.instance,R.style.BottomSheetDialogTheme
        );
        View bottomSheetView = LayoutInflater.from(MainActivity.instance.getApplicationContext())
                .inflate(
                        R.layout.bottom_sheet_dialog,
                        (LinearLayout)MainActivity.instance.findViewById(R.id.bottomSheetContainer)
                );
        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.setCanceledOnTouchOutside(true);
        return bottomSheetDialog;
    }

}
