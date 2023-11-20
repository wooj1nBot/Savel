package com.example.savel;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.jaygoo.widget.RangeSeekBar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class Travels_Adopter extends RecyclerView.Adapter<Travels_Adopter.ViewHolder>{

    Map<String, Travel> travelMap;
    List<String> list;
    Context context;
    private FirebaseFirestore db;
    FirebaseAuth mAuth;
    FirebaseUser currentUser;

    Travels_Adopter(Map<String, Travel> travelMap , List<String> list){
        this.list = list;
        this.travelMap = travelMap;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) ;
        View view = inflater.inflate(R.layout.travel_list_layout, parent, false) ;
        return new ViewHolder(view);
    }



    @Override
    public void onBindViewHolder(@NonNull Travels_Adopter.ViewHolder holder, int position) {
        String id = list.get(position);
        Travel travel = travelMap.get(id);
        String traveler = null;

        if (travel != null) {
            if(position == 0){
                if(travel.status == Travel.TRAVEL_STATUS_BEFORE_START){
                    holder.tv_type.setText("Upcoming Travel");
                    holder.tv_type.setVisibility(View.VISIBLE);
                }
                if(travel.status == Travel.TRAVEL_STATUS_START){
                    holder.tv_type.setText("Started Travel");
                    holder.tv_type.setVisibility(View.VISIBLE);
                }
                if(travel.status == Travel.TRAVEL_STATUS_END){
                    holder.tv_type.setText("Ended Travel");
                    holder.tv_type.setVisibility(View.VISIBLE);
                }
            }else {
                if(Objects.requireNonNull(travelMap.get(list.get(position - 1))).status != travel.status){
                    if(travel.status == Travel.TRAVEL_STATUS_BEFORE_START){
                        holder.tv_type.setText("Upcoming Travel");
                        holder.tv_type.setVisibility(View.VISIBLE);
                    }
                    if(travel.status == Travel.TRAVEL_STATUS_START){
                        holder.tv_type.setText("Started Travel");
                        holder.tv_type.setVisibility(View.VISIBLE);
                    }
                    if(travel.status == Travel.TRAVEL_STATUS_END){
                        holder.tv_type.setText("Ended Travel");
                        holder.tv_type.setVisibility(View.VISIBLE);
                    }
                }else {
                    holder.tv_type.setVisibility(View.GONE);
                }
            }

            traveler = travel.travelSetting.getTraveler().name;
            String profile = travel.travelSetting.getTraveler().profile;
            String guardprofile = travel.travelSetting.getGuardian().profile;
            Date departure = travel.travelSetting.departureTime;
            Date arrival = travel.travelSetting.arrivalTime;
            holder.tv_name.setText(traveler);
            if(profile != null){
                Glide.with(context).load(Uri.parse(profile)).into(holder.iv_profile);
            }else {
                holder.iv_profile.setImageResource(R.drawable.profile);
            }
            holder.tv_gard_name.setText(travel.travelSetting.getGuardian().name);
            if(guardprofile != null){
                Glide.with(context).load(Uri.parse(guardprofile)).into(holder.iv_gard_profile);
            }else {
                holder.iv_gard_profile.setImageResource(R.drawable.profile);
            }


            SimpleDateFormat sdf = new SimpleDateFormat("yyyy E, MMM dd", new Locale("en", "US"));
            String getDate = sdf.format(departure);
            holder.tv_dep_date.setText(getDate);
            sdf = new SimpleDateFormat("hh:mm a", new Locale("en", "US"));
            holder.tv_dep.setText(sdf.format(departure));
            holder.tv_arr.setText(sdf.format(arrival));
            holder.tv_purpose.setText(travel.travelSetting.purpose);
            holder.tv_distance.setText(String.format("%.2fkm", travel.distance));
            Location dep = null;
            Location arr = null;
            holder.seekBar.setRange(0, travel.locations.size()-1);
            holder.seekBar.setSteps(travel.locations.size()-1);
            CharSequence[] sequences = new CharSequence[travel.locations.size()];
            sequences[0] = "Origin";
            int cnt = 0;
            int passed = 0;
            for(int i = 0; i < travel.locations.size(); i++){
                if(travel.locations.get(i).type == 0){
                    dep = travel.locations.get(i);
                }else if(travel.locations.get(i).type == 1){
                    arr = travel.locations.get(i);
                }else {
                    cnt++;
                    sequences[cnt] = "P" + cnt;
                    if(travel.locations.get(i).isPassed){
                        passed = cnt;
                    }
                }
            }
            sequences[travel.locations.size()-1] = "Dest";
            holder.seekBar.setTickMarkTextArray(sequences);
            if(dep != null){
                holder.tv_origin.setText(dep.address);
            }
            if(arr != null){
                holder.tv_dest.setText(arr.address);
                if(arr.isPassed){
                    holder.seekBar.setProgress(travel.locations.size()-1);
                }else {
                    holder.seekBar.setProgress(passed);
                }
            }
            holder.seekBar.setEnabled(false);

            if(travel.status == Travel.TRAVEL_STATUS_START){
                 holder.messageView.setVisibility(View.VISIBLE);
            }else {
                holder.messageView.setVisibility(View.GONE);
            }

            Object[] textViews = new Object[]{false, holder.tv_origin, holder.tv_dest, holder.tv_purpose};
            holder.tv_more.setTag(textViews);

            holder.tv_more.setOnClickListener(new OnSingleClickListener() {
                @Override
                public void onSingleClick(View v) {
                    Object[] data = (Object[]) v.getTag();
                    boolean ist = (boolean) data[0];
                    TextView tv_origin = (TextView) data[1];
                    TextView tv_dest = (TextView) data[2];
                    TextView tv_purpose = (TextView) data[3];

                    if(ist){
                        tv_origin.setMaxLines(2);
                        tv_dest.setMaxLines(2);
                        tv_purpose.setMaxLines(2);
                        ((TextView) v).setText("Read more");
                        data[0] = false;
                    }else {
                        tv_origin.setMaxLines(10);
                        tv_dest.setMaxLines(10);
                        tv_purpose.setMaxLines(10);
                        ((TextView) v).setText("Read less");
                        data[0] = true;
                    }
                    v.setTag(data);
                }
            });

            switch (travel.user_status){
                case Travel.USER_STATUS_NOT_MOVING:
                    holder.iv_warning.setImageResource(R.drawable.hourglass_empty_48px);
                    holder.iv_warning.setImageTintList(ColorStateList.valueOf(Color.parseColor("#F89E3E")));
                    holder.tv_warning.setText("Not departing");
                    break;
                case Travel.USER_STATUS_MOVING:
                    holder.iv_warning.setImageResource(R.drawable.directions_run_48px);
                    holder.iv_warning.setImageTintList(ColorStateList.valueOf(Color.parseColor("#43A047")));
                    holder.tv_warning.setText("On the move");
                    break;
                case Travel.USER_STATUS_ARRIVAL:
                    holder.iv_warning.setImageResource(R.drawable.flag_48px);
                    holder.iv_warning.setImageTintList(ColorStateList.valueOf(Color.parseColor("#F44336")));
                    holder.tv_warning.setText("Arrival");
                    break;
                case Travel.USER_STATUS_WARNING:
                    holder.iv_warning.setImageResource(R.drawable.warning_48px);
                    holder.iv_warning.setImageTintList(ColorStateList.valueOf(Color.parseColor("#F89E3E")));
                    holder.tv_warning.setText("Off-Path");
                    break;
                case Travel.USER_STATUS_NOT_WARNING:
                    holder.iv_warning.setImageResource(R.drawable.done_48px);
                    holder.iv_warning.setImageTintList(ColorStateList.valueOf(Color.parseColor("#43A047")));
                    holder.tv_warning.setText("In-Path");
                    break;
                case Travel.USER_STATUS_ARRIVAL_CHECKPOINT:
                    holder.iv_warning.setImageResource(R.drawable.where_to_vote_48px);
                    holder.iv_warning.setImageTintList(ColorStateList.valueOf(Color.parseColor("#F89E3E")));
                    holder.tv_warning.setText("Check Point");
                    break;
            }

        }



    }

    public void removeTravel(Travel travel, String id){
        list.remove(id);
        db.collection("users").document(travel.getTravelSetting().traveler.uid).update("travel", FieldValue.arrayRemove(id));
        db.collection("users").document(travel.getTravelSetting().guardian.uid).update("travel", FieldValue.arrayRemove(id));
        db.collection("travels").document(id).delete();
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tv_name;
        TextView tv_gard_name;
        TextView tv_origin;
        TextView tv_dest;
        TextView tv_dep;
        TextView tv_dep_date;
        TextView tv_arr;
        TextView tv_purpose;
        TextView tv_distance;
        TextView tv_type;
        TextView tv_more;
        ImageView iv_warning;
        TextView tv_warning;
        CircleImageView iv_profile;
        CircleImageView iv_gard_profile;
        RangeSeekBar seekBar;
        CardView messageView;

        ViewHolder(View itemView) {
            super(itemView);
            tv_name = itemView.findViewById(R.id.tv_name);
            iv_profile = itemView.findViewById(R.id.profile);
            tv_arr = itemView.findViewById(R.id.tv_arr);
            tv_dep = itemView.findViewById(R.id.tv_dep);
            tv_origin = itemView.findViewById(R.id.tv_origin);
            tv_dep_date = itemView.findViewById(R.id.tv_depdate);
            tv_dest = itemView.findViewById(R.id.tv_dest);
            tv_purpose = itemView.findViewById(R.id.tv_purpose);
            tv_distance = itemView.findViewById(R.id.tv_distance);
            tv_type = itemView.findViewById(R.id.tv_type);
            iv_warning = itemView.findViewById(R.id.icon);
            tv_warning = itemView.findViewById(R.id.tv_message);
            tv_more = itemView.findViewById(R.id.tv_more);
            seekBar = itemView.findViewById(R.id.step);
            tv_gard_name = itemView.findViewById(R.id.tv_guard_name);
            iv_gard_profile = itemView.findViewById(R.id.guard_profile);
            CardView mother = itemView.findViewById(R.id.mother);
            messageView = itemView.findViewById(R.id.messageview);
            mother.setOnClickListener(new OnSingleClickListener() {
                @Override
                public void onSingleClick(View v) {
                    int pos = getAdapterPosition() ;
                    if (pos != RecyclerView.NO_POSITION) {
                        Travel travel = travelMap.get(list.get(pos));
                        if(travel.status != Travel.TRAVEL_STATUS_BEFORE_START){
                            Intent intent = new Intent(context, NavigationActivity.class);
                            intent.putExtra("travel", list.get(pos));
                            context.startActivity(intent);
                        }else {
                            Toast.makeText(context, "It's a travel that hasn't started.", Toast.LENGTH_SHORT).show();
                        }

                    }
                }
            });
            mother.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    int pos = getAdapterPosition() ;
                    if (pos != RecyclerView.NO_POSITION) {
                          Travel travel = travelMap.get(list.get(pos));
                          if(currentUser.getUid().equals(travel.travelSetting.guardian.uid)){
                              AlertDialog.Builder builder = new AlertDialog.Builder(context);
                              builder.setTitle("Travel delete").setMessage("Are you sure you want to delete this trip?");
                              builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                  @Override
                                  public void onClick(DialogInterface dialogInterface, int i) {
                                          removeTravel(travel, list.get(pos));
                                  }
                              });
                              builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                                  @Override
                                  public void onClick(DialogInterface dialogInterface, int i) {
                                      dialogInterface.dismiss();
                                  }
                              });
                              AlertDialog alertDialog = builder.create();
                              alertDialog.show();
                          }
                    }
                    return true;
                }
            });
        }
    }
}
