package com.example.savel;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;
import java.util.List;

public class SearchListAdopter  extends RecyclerView.Adapter<SearchListAdopter.ViewHolder>{

    List<Location> locations;
    List<Marker> markers;
    List<TextView> textViews = new ArrayList<>();
    Context context;
    FindActivity findActivity;

    SearchListAdopter(List<Location> locations, List<Marker> markers, FindActivity findActivity){
        this.locations = locations;
        this.markers = markers;
        this.findActivity = findActivity;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) ;
        View view = inflater.inflate(R.layout.location_search_list, parent, false) ;
        return new SearchListAdopter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
          Location location = locations.get(position);
          holder.tv_title.setText(location.name);
          holder.tv_road.setText(location.address);
          holder.btn_select.setTag(position);
          if(textViews.size() > position){
              textViews.set(position, holder.tv_btn);
          }else {
              textViews.add(holder.tv_btn);
          }
          holder.btn_select.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View view) {
                  int pos = (int) view.getTag();
                  findActivity.showDialog(locations.get(pos), false, markers.get(pos), textViews.get(pos));
              }
          });
    }

    @Override
    public int getItemCount() {
        return locations.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tv_title;
        TextView tv_road;
        TextView tv_btn;
        CardView btn_select;

        ViewHolder(View itemView) {
            super(itemView);
            tv_title = itemView.findViewById(R.id.tv_title);
            tv_road = itemView.findViewById(R.id.tv_road);
            tv_btn = itemView.findViewById(R.id.tv_btn);
            btn_select = itemView.findViewById(R.id.btn_select);
        }
    }
}
