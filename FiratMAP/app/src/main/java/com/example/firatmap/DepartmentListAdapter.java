package com.example.firatmap;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public class DepartmentListAdapter extends ArrayAdapter<Departments> implements Filterable {
    ArrayList<Departments> itemsModelSl ;
    ArrayList<Departments> itemsModelListFiltered;
    public DepartmentListAdapter(Context context, ArrayList<Departments> firatMarkerArrayList){
        super(context,R.layout.departments_list_item,firatMarkerArrayList);
        itemsModelSl=firatMarkerArrayList;
        itemsModelListFiltered = itemsModelSl;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.departments_list_item,parent,false);

        //ImageView imageView = convertView.findViewById(R.id.departmant_item_imageview);
        TextView textView = view.findViewById(R.id.departmant_item_title);
        Button button = view.findViewById(R.id.departmant_item_button);
        /*if(marker.icon!=null){
            System.out.println("--null");
            MainActivity.instance.fireBaseHelper.setImageView(imageView,marker.icon);
        }*/
        textView.setText(itemsModelListFiltered.get(position).name);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println(itemsModelListFiltered.get(position).name);
            }
        });
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
