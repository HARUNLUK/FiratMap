package com.example.firatmap;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DepartmentListAdapter extends ArrayAdapter<Departments> implements Filterable {
    ListType listType;
    ArrayList<Departments> itemsModelSl ;
    ArrayList<Departments> itemsModelListFiltered;
    public DepartmentListAdapter(Context context, ArrayList<Departments> firatMarkerArrayList,ListType listType){
        super(context,R.layout.departments_list_item,firatMarkerArrayList);
        itemsModelSl=firatMarkerArrayList;
        itemsModelListFiltered = itemsModelSl;
        this.listType = listType;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.departments_list_item,parent,false);
        //ImageView imageView = convertView.findViewById(R.id.departmant_item_imageview);
        TextView textViewFacility = view.findViewById(R.id.department_facility_title);
        TextView textViewDepartment = view.findViewById(R.id.department_item_title);
        ImageView imageView = view.findViewById(R.id.department_item_img);

        if(listType == ListType.BOTTOM_VIEW){
            imageView.setVisibility(View.GONE);
            textViewFacility.setVisibility(View.GONE);
        }

        //Button button = view.findViewById(R.id.department_item_button);
        /*if(marker.icon!=null){
            System.out.println("--null");
            MainActivity.instance.fireBaseHelper.setImageView(imageView,marker.icon);
        }*/

        int facilityId = itemsModelListFiltered.get(position).parent;
        for (FiratMarker marker: MainActivity.markers) {
            if(facilityId == marker.getID()){
                textViewFacility.setText(marker.getTitle());
                if(marker.getIcon() == null){
                    imageView.setImageResource(R.drawable.ic_facility);
                }else{
                    imageView.setImageBitmap(marker.getIcon());
                }
            }
        }

        textViewDepartment.setText(itemsModelListFiltered.get(position).name);
        return view;
    }

    @Override
    public int getCount() {
        return itemsModelListFiltered.size();
    }

    @Nullable
    @Override
    public Departments getItem(int position) {
        return itemsModelListFiltered.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                FilterResults result = new FilterResults();
                if (charSequence == null || charSequence.length() == 0){
                    result.count = itemsModelSl.size();
                    result.values = itemsModelSl;
                }else{
                    List<Departments> resultModel = new ArrayList<>();
                    String searchStr = MainActivity.englishConvert(charSequence.toString());
                    for (Departments department : itemsModelSl){
                        String departmentName = MainActivity.englishConvert(department.name);
                        if (departmentName.toUpperCase().contains(searchStr.toUpperCase())) {
                            resultModel.add(department);
                        }
                        result.count = resultModel.size();
                        result.values = resultModel;
                    }
                }
                return result;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                itemsModelListFiltered = (ArrayList<Departments>) filterResults.values;
                notifyDataSetChanged();
            }
        };
        return filter;
    }
}
enum ListType{
    LAYOUT,
    BOTTOM_VIEW
}
