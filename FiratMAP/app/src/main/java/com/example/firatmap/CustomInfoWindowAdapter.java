package com.example.firatmap;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

public class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {
    private Context mContext;
    private final View mWindow;

    public CustomInfoWindowAdapter(Context mContext) {
        this.mContext = mContext;
        mWindow = LayoutInflater.from(mContext).inflate(R.layout.custom_marker_info,null);
    }

    private void renderWindow(Marker marker,View view){
        String title = marker.getTitle();
        TextView textTitle = (TextView) view.findViewById(R.id.marker_info_title);
        textTitle.setText(title);
    }

    @Override
    public View getInfoWindow(@NonNull Marker marker) {
        renderWindow(marker,mWindow);
        return mWindow;
    }

    @Nullable
    @Override
    public View getInfoContents(@NonNull Marker marker) {
        renderWindow(marker,mWindow);
        return mWindow;
    }
}
