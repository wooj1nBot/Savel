package com.example.savel;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    FirebaseUser currentUser;
    private FirebaseFirestore db;
    int cnt = 0;
    User traveler;
    User guardian;
    CardView btn_traveler;
    CardView btn_date;
    CardView btn_time;
    CardView btn_arrtime;
    CircleImageView profile;
    TextView tv_name;
    TextView tv_email;
    TextView tv_date;
    TextView tv_time;
    TextView tv_arrtime;
    EditText ed_purpose;
    Date departureDate;
    Date arrivalDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        btn_traveler = findViewById(R.id.traveler);
        btn_date = findViewById(R.id.date);
        btn_time = findViewById(R.id.departure_time);
        btn_arrtime = findViewById(R.id.arrival_time);
        profile = findViewById(R.id.profile);
        tv_name = findViewById(R.id.tv_name);
        tv_email = findViewById(R.id.tv_email);
        tv_date = findViewById(R.id.tv_date);
        tv_time = findViewById(R.id.tv_depdate);
        tv_arrtime = findViewById(R.id.tv_arrtime);
        ed_purpose = findViewById(R.id.ed_purpose);
        ImageView back = findViewById(R.id.iv_back);
        ImageView done = findViewById(R.id.iv_done);

        back.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                 finish();
            }
        });
        done.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                if(traveler != null){
                    if(departureDate != null){
                        if(arrivalDate != null){
                             if(arrivalDate.getTime() > departureDate.getTime()){
                                 String purpose = ed_purpose.getText().toString();
                                 if(purpose.length() > 0){
                                     TravelSetting travelSetting = new TravelSetting(traveler, guardian, departureDate, arrivalDate, purpose);
                                     Intent intent = new Intent(SettingActivity.this, FindActivity.class);
                                     intent.putExtra("setting", travelSetting);
                                     startActivity(intent);
                                     finish();
                                 }else {
                                     Toast.makeText(SettingActivity.this, "Please enter the purpose of travel.", Toast.LENGTH_SHORT).show();
                                 }
                             }else {
                                 Toast.makeText(SettingActivity.this, "The Travel arrival time must be later than the departure time.", Toast.LENGTH_SHORT).show();
                             }
                        }else {
                            Toast.makeText(SettingActivity.this, "Please enter travel arrival date and time.", Toast.LENGTH_SHORT).show();
                        }
                    }else {
                        Toast.makeText(SettingActivity.this, "Please enter travel departure date and time.", Toast.LENGTH_SHORT).show();
                    }
                }else {
                    Toast.makeText(SettingActivity.this, "Please select a traveler.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        Intent intent = getIntent();
        TravelSetting setting = (TravelSetting) intent.getSerializableExtra("setting");
        if(setting != null){
            initSetting(setting);
        }
        btn_traveler.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                showFriendsDialog();
            }
        });
        getDate();
        getDepartureTime();
        getArrivalTime();
    }
    public void initSetting(TravelSetting setting){
        setTraveler(setting.traveler);
        departureDate = setting.departureTime;
        arrivalDate = setting.arrivalTime;
        ed_purpose.setText(setting.purpose);
    }

    public void showFriendsDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(SettingActivity.this);
        View view = LayoutInflater.from(SettingActivity.this).inflate(R.layout.traveler_dialog, (RelativeLayout) findViewById(R.id.dialog));
        RecyclerView rc = view.findViewById(R.id.rc);
        rc.setLayoutManager(new LinearLayoutManager(this));
        ProgressBar progressBar = view.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);
        builder.setView(view);
        builder.setCancelable(false);
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
        getUserData(rc, progressBar, alertDialog);
    }

    public void getUserData(RecyclerView rc , ProgressBar progressBar, AlertDialog dialog){

        db.collection("users").document(currentUser.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    Map<String, Object > map = task.getResult().getData();
                    if(map != null){
                        String uid = (String) map.get("uid");
                        String name = (String) map.get("name");
                        String email = (String) map.get("email");
                        String token = (String) map.get("token");
                        String profile = map.containsKey("profile") ? (String) map.get("profile") : null;
                        guardian = new User(name, email, uid, profile, token);
                        List<String> friends = (List<String>) map.get("friends");
                        if(friends != null) {
                            getFriendData(friends, rc, progressBar, dialog);
                        }
                    }
                }else {
                    progressBar.setVisibility(View.GONE);
                }
            }
        });
    }

    public void getDate(){
        Locale.setDefault(Locale.ENGLISH);
        Calendar c = Calendar.getInstance();
        if(departureDate == null) {
            departureDate = c.getTime();
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy E, MMM dd", new Locale("en", "US"));
        String getDate = sdf.format(departureDate);
        tv_date.setText(getDate);
        btn_date.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                c.setTime(departureDate);
                DatePickerDialog dialog = new DatePickerDialog(SettingActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(departureDate);
                        cal.set(i, i1, i2);
                        departureDate = new Date(cal.getTimeInMillis());
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy E, MMM dd", new Locale("en", "US"));
                        String getDate = sdf.format(departureDate);
                        tv_date.setText(getDate);
                    }
                }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DATE));
                dialog.show();
            }
        });
    }

    public void getDepartureTime(){
        Locale.setDefault(Locale.ENGLISH);
        Calendar c = Calendar.getInstance();
        if(departureDate == null) {
            departureDate = c.getTime();
        }
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", new Locale("en", "US"));
        String getDate = sdf.format(departureDate);
        tv_time.setText(getDate);
        btn_time.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                c.setTime(departureDate);
                TimePickerDialog timePickerDialog = new TimePickerDialog(SettingActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int i, int i1) {
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(departureDate);
                        cal.set(Calendar.HOUR_OF_DAY, i);
                        cal.set(Calendar.MINUTE, i1);
                        departureDate = cal.getTime();
                        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", new Locale("en", "US"));
                        String getDate = sdf.format(departureDate);
                        tv_time.setText(getDate);
                    }
                }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), false);
                timePickerDialog.show();
            }
        });
    }
    public void getArrivalTime(){
        Locale.setDefault(Locale.ENGLISH);
        Calendar c = Calendar.getInstance();
        if(arrivalDate == null) {
            arrivalDate = departureDate;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", new Locale("en", "US"));
        String getDate = sdf.format(arrivalDate);
        tv_arrtime.setText(getDate);
        btn_arrtime.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                c.setTime(arrivalDate);
                TimePickerDialog timePickerDialog = new TimePickerDialog(SettingActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int i, int i1) {
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(arrivalDate);
                        cal.set(Calendar.HOUR_OF_DAY, i);
                        cal.set(Calendar.MINUTE, i1);
                        arrivalDate = cal.getTime();
                        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", new Locale("en", "US"));
                        String getDate = sdf.format(arrivalDate);
                        tv_arrtime.setText(getDate);
                    }
                }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), false);
                timePickerDialog.show();
            }
        });
    }

    public void getFriendData(List<String> friends, RecyclerView rc , ProgressBar progressBar, AlertDialog dialog){
        List<User> users = new ArrayList<>();
        cnt = 0;
        for(String uid : friends){
            db.collection("users").document(uid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    cnt++;
                    if(task.isSuccessful()){
                        Map<String, Object> map = task.getResult().getData();
                        if(map != null){
                            String uid = (String) map.get("uid");
                            String name = (String) map.get("name");
                            String email = (String) map.get("email");
                            String token = (String) map.get("token");
                            String profile = map.containsKey("profile") ? (String) map.get("profile") : null;
                            User user = new User(name, email, uid, profile, token);
                            users.add(user);
                        }
                    }
                    if(cnt == friends.size()){
                        progressBar.setVisibility(View.GONE);
                        System.out.println(users.size());
                        UserListAdopter userListAdopter = new UserListAdopter(users, true, SettingActivity.this, dialog);
                        rc.setAdapter(userListAdopter);
                    }
                }
            });
        }
    }

    public void setTraveler(User traveler){
        this.traveler = traveler;
        tv_name.setVisibility(View.VISIBLE);
        tv_email.setVisibility(View.VISIBLE);
        tv_name.setText(traveler.name);
        tv_email.setText(traveler.email);
        if(traveler.profile != null){
            Glide.with(SettingActivity.this).load(Uri.parse(traveler.profile)).into(profile);
        }else {
            profile.setImageResource(R.drawable.profile);
        }
    }
}