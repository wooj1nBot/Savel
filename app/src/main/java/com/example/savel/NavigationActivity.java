package com.example.savel;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.maps.android.PolyUtil;
import com.google.maps.android.SphericalUtil;
import com.google.maps.android.ui.IconGenerator;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import de.hdodenhof.circleimageview.CircleImageView;

public class NavigationActivity extends AppCompatActivity  implements
        GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener,
        OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback {

    FirebaseAuth mAuth;
    FirebaseUser currentUser;
    private FirebaseFirestore db;
    private GoogleMap map;
    private SupportMapFragment mapFragment;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private boolean locationPermissionGranted;
    private LocationCallback locationCallback;
    private LocationRequest mLocationRequest;
    private List<Polyline> polylines = new ArrayList<>();
    private Button btn_loc;
    private Button btn_guide;
    private Button btn_mode;
    private ImageView btn_close;
    private TextView tv_guide;
    private ImageView iv_guide;
    private boolean isMoved = false;
    private SlidingUpPanelLayout layout;
    private Button btn_route;
    private DirectionsResponse response;
    private TextView tv_dist;
    private DirectionsStep currentStep;
    private com.example.savel.Location origin;
    private com.example.savel.Location dest;
    private CardView guideView;
    private CardView messageView;
    private TextView tv_message;
    private ImageView iv_message;
    private HashMap<DirectionsStep, Marker> markers_step = new HashMap<>();
    private List<Marker> markers = new ArrayList<>();
    private Travel travel;
    private String travelId;
    private List<LatLng> route_list = new ArrayList<>();
    private LatLng myLocation;
    private boolean isStart = false;
    private DatabaseReference mDatabase;
    private Marker travelerMarker;

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final int DEFAULT_ZOOM = 20;
    private static final int DEFAULT_ZOOM_ROUTE = 15;
    private static final double DEFAULT_TOLERANCE = 40;
    private static final int DEFAULT_ROUTE_CHECK_TIME = 1000 * 60;
    private static final int DEFAULT_PURPOSE_CHECK_TIME = 1000 * 60 * 5;
    private static final int DEFAULT_VIBRATE_TIME = 1000;
    private static final int CAMERA_MODE_NAVIGATION = 1;
    private static final int CAMERA_MODE_MAP = 2;
    private int Camera_Mode = CAMERA_MODE_NAVIGATION;

    private boolean isTraveler = true;
    private Timer timer;
    private Timer purpose;

    ListenerRegistration registration;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Locale.setDefault(new Locale("en"));
        setContentView(R.layout.activity_navigation);
        Intent intent = getIntent();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.keepSynced(true);

        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(NavigationActivity.this);

        layout = findViewById(R.id.main_panel);
        layout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        travelId = intent.getStringExtra("travel");
        if(travelId == null) {

        }
        btn_loc = findViewById(R.id.btn_loc);
        btn_mode = findViewById(R.id.btn_mode);
        btn_route = findViewById(R.id.btn_route);
        btn_guide = findViewById(R.id.btn_guide);
        tv_guide = findViewById(R.id.tv_guide);
        iv_guide = findViewById(R.id.iv_guide);
        btn_close = findViewById(R.id.iv_close);
        tv_dist = findViewById(R.id.tv_dist);
        messageView = findViewById(R.id.messageview);
        tv_message = findViewById(R.id.tv_message);
        iv_message = findViewById(R.id.iv_message);
        guideView = findViewById(R.id.guideview);

        btn_close.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                 guideView.setVisibility(View.GONE);
                 btn_guide.setVisibility(View.VISIBLE);
            }
        });
        btn_guide.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                guideView.setVisibility(View.VISIBLE);
                btn_guide.setVisibility(View.GONE);
            }
        });

        btn_mode.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                 if(Camera_Mode == CAMERA_MODE_NAVIGATION){
                     Camera_Mode = CAMERA_MODE_MAP;
                     btn_mode.setText("Map Mode");
                     btn_mode.setTextColor(getColor(R.color.colorPrimary));
                     btn_mode.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
                 }else {
                     Camera_Mode = CAMERA_MODE_NAVIGATION;
                     btn_mode.setText("Navigation Mode");
                     btn_mode.setTextColor(Color.WHITE);
                     btn_mode.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.colorPrimary)));
                 }
            }
        });

        btn_route.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                if(origin == null) return;

                Camera_Mode = CAMERA_MODE_MAP;
                btn_mode.setText("Map Mode");
                btn_mode.setTextColor(getColor(R.color.colorPrimary));
                btn_mode.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(origin.latLng.gLatLng(), DEFAULT_ZOOM_ROUTE));
            }
        });

        btn_loc.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                if(map == null || myLocation == null) return;
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                        myLocation, DEFAULT_ZOOM));
            }
        });




    }

    public void getTravelerData(Travel travel){
          db.collection("users").document(travel.getTravelSetting().traveler.uid).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
              @Override
              public void onSuccess(DocumentSnapshot documentSnapshot) {
                  Map<String, Object > map = documentSnapshot.getData();
                  if(map != null){
                      travel.travelSetting.traveler.token = (String) map.get("token");
                      travel.travelSetting.traveler.profile = (String) map.get("profile");
                  }
              }
          });
    }

    public void getGuardianData(Travel travel){
        db.collection("users").document(travel.getTravelSetting().guardian.uid).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Map<String, Object > map = documentSnapshot.getData();
                if(map != null){
                    travel.travelSetting.guardian.token = (String) map.get("token");
                    travel.travelSetting.guardian.profile = (String) map.get("profile");
                }
            }
        });
    }



    public void setInit(String id){
        LoadingView loadingView = new LoadingView(NavigationActivity.this);
        loadingView.show("Loading Data...");


        registration = db.collection("travels").document(id).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException error) {
                loadingView.stop();
                if (error != null) {
                    Toast.makeText(NavigationActivity.this, "Failed to load data", Toast.LENGTH_LONG).show();
                    return;
                }
                if(documentSnapshot != null){

                    travel = documentSnapshot.toObject(Travel.class);
                    if(travel != null) {
                        getTravelerData(travel);
                        getGuardianData(travel);

                        if(travel.travelSetting.getTraveler().uid.equals(currentUser.getUid())){
                            isTraveler = true;
                            registration.remove();
                        }else {
                            isTraveler = false;
                            btn_loc.setText("Location");
                        }

                        setMarker(travel.locations);

                        for (int i = 0; i < travel.locations.size(); i++) {
                            if (travel.locations.get(i).type == 0) {
                                origin = travel.locations.get(i);
                            } else if (travel.locations.get(i).type == 1) {
                                dest = travel.locations.get(i);
                            }
                        }
                        if (travel.directionsResponse != null) {
                            response = travel.directionsResponse;
                            setAllStepIcon();
                            if (currentStep.html_instructions != null) {
                                setGuide(currentStep);
                            }
                        }
                        drawAllPath();

                        if(isTraveler){
                            getLocationPermission();
                            updateLocationUI();
                            setTravelerWork();
                        }else {
                            map.setMyLocationEnabled(false);
                            map.getUiSettings().setMyLocationButtonEnabled(false);
                            getTravelerLocation();
                        }



                    }else {
                        Toast.makeText(NavigationActivity.this, "Failed to load data", Toast.LENGTH_LONG).show();
                    }
                }else {
                    Toast.makeText(NavigationActivity.this, "Failed to load data", Toast.LENGTH_LONG).show();
                }

            }
        });
    }

    public void setAllStepIcon(){
        List<DirectionsRoute> routes = travel.directionsResponse.routes;
        for (DirectionsRoute route : routes) {
            List<DirectionsLeg> legs = route.legs;
            for (DirectionsLeg leg : legs) {
                for (int i = 0; i < leg.steps.size(); i++) {
                    DirectionsStep step = leg.steps.get(i);
                    setStepIcon(step);
                    if (!step.isPassed && currentStep == null) {
                        currentStep = step;
                    }
                    if(i == leg.steps.size()-1){
                        if(currentStep == null){
                            currentStep = step;
                        }
                    }
                }
            }
        }
    }

    public void setMarker(List<com.example.savel.Location> locations){

        for(Marker marker : markers){
            marker.remove();
        }

        com.example.savel.Location dep = null;
        com.example.savel.Location arr = null;
        View view = LayoutInflater.from(NavigationActivity.this).inflate(R.layout.icon, (RelativeLayout) findViewById(R.id.rv));
        ImageView iv_marker = view.findViewById(R.id.imageView);
        ImageView iv_icon = view.findViewById(R.id.iv_icon);
        View fill = view.findViewById(R.id.view);
        TextView textView = view.findViewById(R.id.textView);
        IconGenerator iconFactory = new IconGenerator(this);
        iconFactory.setContentView(view);
        iconFactory.setBackground(null);

        for(int i = 0; i < locations.size(); i++){
            if(locations.get(i).type == 0){
                dep = locations.get(i);
            }else if(locations.get(i).type == 1){
                arr = locations.get(i);
            }
        }
        if(dep != null){
            iv_icon.setImageResource(R.drawable.flag_48px);
            iv_marker.setImageTintList(ColorStateList.valueOf(Color.parseColor("#F44336")));
            fill.setBackgroundColor(Color.parseColor("#F44336"));
            textView.setText("Origin");
            Marker de = map.addMarker(new MarkerOptions()
                    .position(dep.latLng.gLatLng()).title(dep.address).icon(BitmapDescriptorFactory.fromBitmap(iconFactory.makeIcon()))
            );
            de.setTag(dep);
            markers.add(de);
        }
        int m = 0;
        iv_icon.setImageResource(R.drawable.done_48px);


        for(int i = 0; i < locations.size(); i++){
            if(locations.get(i).type == 2){
                m++;
                if(locations.get(i).isPassed){
                    iv_marker.setImageTintList(ColorStateList.valueOf(Color.parseColor("#43A047")));
                    fill.setBackgroundColor(Color.parseColor("#43A047"));
                    textView.setText("Check Point " + (m) + "-Arrived");
                }else {
                    iv_marker.setImageTintList(ColorStateList.valueOf(Color.parseColor("#F89E3E")));
                    fill.setBackgroundColor(Color.parseColor("#F89E3E"));
                    textView.setText("Check Point " + (m));
                }

                Marker check = map.addMarker(new MarkerOptions()
                        .position(locations.get(i).latLng.gLatLng()).title(locations.get(i).address).icon(BitmapDescriptorFactory.fromBitmap(iconFactory.makeIcon()))
                );
                check.setTag(locations.get(i));
                markers.add(check);
            }
        }
        if(arr != null){
            iv_icon.setImageResource(R.drawable.flag_48px);
            if(arr.isPassed){
                iv_marker.setImageTintList(ColorStateList.valueOf(Color.parseColor("#43A047")));
                fill.setBackgroundColor(Color.parseColor("#43A047"));
                textView.setText("Destination-Arrived");
            }else {
                iv_marker.setImageTintList(ColorStateList.valueOf(Color.parseColor("#F44336")));
                fill.setBackgroundColor(Color.parseColor("#F44336"));
                textView.setText("Destination");
            }

            Marker ar = map.addMarker(new MarkerOptions()
                    .position(arr.latLng.gLatLng()).title(arr.address).icon(BitmapDescriptorFactory.fromBitmap(iconFactory.makeIcon()))
            );
            ar.setTag(arr);
            markers.add(ar);
        }
    }

    public void setStepIcon(DirectionsStep step){

        if(markers_step.containsKey(step)) markers_step.get(step).remove();
        markers_step.remove(step);

        LatLng dot = new LatLng(step.start_location.lat, step.start_location.lng);
        View view;
        if(step.isArrived){
            view = LayoutInflater.from(NavigationActivity.this).inflate(R.layout.custom_marker2, (RelativeLayout) findViewById(R.id.rv));
        }else {
            view = LayoutInflater.from(NavigationActivity.this).inflate(R.layout.custom_marker, (RelativeLayout) findViewById(R.id.rv));
        }

        FloatingActionButton fb = view.findViewById(R.id.fb);
        IconGenerator iconFactory = new IconGenerator(this);
        iconFactory.setContentView(view);
        iconFactory.setBackground(null);

        int icon = -1;

        if(step.maneuver != null) {
            switch (step.maneuver) {
                case "turn-slight-left":
                    icon = R.drawable.turn_slight_left_48px;
                    break;
                case "turn-sharp-left":
                    icon = R.drawable.turn_sharp_left_48px;
                    break;
                case "turn-left":
                    icon = R.drawable.turn_left_48px;
                    break;
                case "turn-slight-right":
                    icon = R.drawable.turn_slight_right_48px;
                    break;
                case "turn-sharp-right":
                    icon = R.drawable.turn_sharp_right_48px;
                    break;
                case "keep-right":
                    icon = R.drawable.east_48px;
                    break;
                case "keep-left":
                    icon = R.drawable.west_48px;
                    break;
                case "uturn-left":
                    icon = R.drawable.u_turn_left_48px;
                    break;
                case "uturn-right":
                    icon = R.drawable.u_turn_right_48px;
                    break;
                case "turn-right":
                    icon = R.drawable.turn_right_48px;
                    break;
                case "straight":
                    icon = R.drawable.straight_48px;
                    break;
                case "ramp-left":
                    icon = R.drawable.ramp_left_48px;
                    break;
                case "ramp-right":
                    icon = R.drawable.ramp_right_48px;
                    break;
                case "merge":
                    icon = R.drawable.merge_48px;
                    break;
                case "fork-left":
                    icon = R.drawable.fork_left_48px;
                    break;
                case "fork-right":
                    icon = R.drawable.fork_right_48px;
                    break;

                default:
                    icon = -1;
                    break;
            }
        }

        if(icon != -1){
            fb.setImageResource(icon);
        }else {
            fb.setImageResource(R.drawable.signpost_48px);
        }
        Marker marker = map.addMarker(new MarkerOptions()
                .position(dot).icon(BitmapDescriptorFactory.fromBitmap(iconFactory.makeIcon()))
        );
        markers_step.put(step, marker);

    }

    public void changeStepIcon(DirectionsStep step){

        View view = LayoutInflater.from(NavigationActivity.this).inflate(R.layout.custom_marker2, (RelativeLayout) findViewById(R.id.rv));
        FloatingActionButton fb = view.findViewById(R.id.fb);
        IconGenerator iconFactory = new IconGenerator(this);
        iconFactory.setContentView(view);
        iconFactory.setBackground(null);

        int icon = -1;

        if(step.maneuver != null) {
            switch (step.maneuver) {
                case "turn-slight-left":
                    icon = R.drawable.turn_slight_left_48px;
                    break;
                case "turn-sharp-left":
                    icon = R.drawable.turn_sharp_left_48px;
                    break;
                case "turn-left":
                    icon = R.drawable.turn_left_48px;
                    break;
                case "turn-slight-right":
                    icon = R.drawable.turn_slight_right_48px;
                    break;
                case "turn-sharp-right":
                    icon = R.drawable.turn_sharp_right_48px;
                    break;
                case "keep-right":
                    icon = R.drawable.east_48px;
                    break;
                case "keep-left":
                    icon = R.drawable.west_48px;
                    break;
                case "uturn-left":
                    icon = R.drawable.u_turn_left_48px;
                    break;
                case "uturn-right":
                    icon = R.drawable.u_turn_right_48px;
                    break;
                case "turn-right":
                    icon = R.drawable.turn_right_48px;
                    break;
                case "straight":
                    icon = R.drawable.straight_48px;
                    break;
                case "ramp-left":
                    icon = R.drawable.ramp_left_48px;
                    break;
                case "ramp-right":
                    icon = R.drawable.ramp_right_48px;
                    break;
                case "merge":
                    icon = R.drawable.merge_48px;
                    break;
                case "fork-left":
                    icon = R.drawable.fork_left_48px;
                    break;
                case "fork-right":
                    icon = R.drawable.fork_right_48px;
                    break;

                default:
                    icon = -1;
                    break;
            }
        }

        if(icon != -1){
            fb.setImageResource(icon);
        }else {
            fb.setImageResource(R.drawable.signpost_48px);
        }

        Marker marker = markers_step.get(step);
        if(marker != null){
            marker.setIcon(BitmapDescriptorFactory.fromBitmap(iconFactory.makeIcon()));
        }

    }

    public void saveTravelData(Travel travel){
        db.collection("travels").document(travelId).set(travel);
    }

    public DirectionsStep findSteps(LatLng loc, DirectionsResponse directionsResponse){
        List<DirectionsRoute> routes = directionsResponse.routes;
        for(DirectionsRoute route : routes) {
            List<DirectionsLeg> legs = route.legs;
            for (DirectionsLeg leg : legs) {
                for (int i = 0; i < leg.steps.size(); i++) {
                    DirectionsStep step = leg.steps.get(i);
                    LatLng start = new LatLng(step.start_location.lat, step.start_location.lng);
                    if(!step.isArrived) {
                        if (calculateLocationDifference(start, loc) <= DEFAULT_TOLERANCE) {
                            for (int j = 0; j < i; j++) {
                                leg.steps.get(j).isPassed = true;
                            }
                            step.isArrived = true;
                            return step;
                        }
                    }
                }
            }
        }
        return null;
    }

    public void endAllSteps(DirectionsResponse directionsResponse){
        List<DirectionsRoute> routes = directionsResponse.routes;
        for(DirectionsRoute route : routes) {
            List<DirectionsLeg> legs = route.legs;
            for (DirectionsLeg leg : legs) {
                for (int i = 0; i < leg.steps.size(); i++) {
                    DirectionsStep step = leg.steps.get(i);
                    step.isArrived = true;
                    step.isPassed = true;
                }
            }
        }
    }

    public boolean isArrivedDest(LatLng loc, com.example.savel.Location dest){
        return calculateLocationDifference(dest.getLatLng().gLatLng(), loc) <= DEFAULT_TOLERANCE;
    }

    public int findCheckPoint(LatLng loc, DirectionsResponse directionsResponse){

        if(travel == null) return -1;

        List<com.example.savel.Location> locations = travel.locations;
        int k = 0;
        for(int i = 0; i < locations.size(); i++){
            com.example.savel.Location location = locations.get(i);
            if(location.type == 2){
                if(!location.isPassed){
                    if(calculateLocationDifference(location.latLng.gLatLng(), loc) <= DEFAULT_TOLERANCE){
                        location.isPassed = true;
                        return k+1;
                    }
                }
               k++;
            }
        }

        if(directionsResponse != null) {
            List<DirectionsRoute> routes = directionsResponse.routes;
            for (DirectionsRoute route : routes) {
                List<DirectionsLeg> legs = route.legs;
                for (DirectionsLeg leg : legs) {
                    List<DirectionsViaWaypoint> waypoints = leg.via_waypoint;
                    for (int i = 0; i < waypoints.size(); i++) {
                        DirectionsViaWaypoint waypoint = waypoints.get(i);
                        if (calculateLocationDifference(new LatLng(waypoint.location.lat, waypoint.location.lng), loc) <= DEFAULT_TOLERANCE) {
                            k = 0;
                            for(int j = 0; j < locations.size(); j++){
                                if(locations.get(j).type == 2){
                                    if(!locations.get(j).isPassed) {
                                        if (k == i) {
                                            locations.get(j).isPassed = true;
                                            return k + 1;
                                        }
                                    }
                                    k++;
                                }
                            }
                        }
                    }
                }
            }
        }

        return -1;
    }


    public void drawPath(LatLng start, LatLng end, boolean isPassed){
        PolylineOptions polylineOptions = new PolylineOptions();
        polylineOptions.add(start);
        polylineOptions.add(end);
        polylineOptions.color(isPassed ? Color.parseColor("#AAAAAA") : Color.parseColor("#1E88E5"));
        polylineOptions.width(20);
        Polyline polyline = map.addPolyline(polylineOptions);
        polyline.setJointType(JointType.ROUND);
        polyline.setStartCap(new RoundCap());
        polyline.setEndCap(new RoundCap());
        polylines.add(polyline);
    }



    public void drawAllPath(){
        route_list.clear();
        for(Polyline polyline : polylines){
            polyline.remove();
        }

        if(travel.directionsResponse != null) {
            response = travel.directionsResponse;

            List<DirectionsRoute> routes = travel.directionsResponse.routes;

            for (DirectionsRoute route : routes) {
                List<DirectionsLeg> legs = route.legs;
                for (DirectionsLeg leg : legs) {
                    for (int i = 0; i < leg.steps.size(); i++) {
                        DirectionsStep step = leg.steps.get(i);
                        LatLng start = new LatLng(step.start_location.lat, step.start_location.lng);
                        LatLng end = new LatLng(step.end_location.lat, step.end_location.lng);
                        if (i == 0) {
                            drawPath(origin.latLng.gLatLng(), start, step.isPassed);
                            route_list.add(origin.latLng.gLatLng());
                            route_list.add(start);
                        }
                        route_list.add(start);
                        route_list.add(end);

                        if (i == leg.steps.size() - 1) {
                            drawPath(end, dest.latLng.gLatLng(), step.isPassed);
                            route_list.add(end);
                            route_list.add(dest.latLng.gLatLng());
                        }

                        drawPath(start, end, step.isPassed);

                        if (!step.isPassed && currentStep == null) {
                            currentStep = step;
                        }

                    }
                }
            }
        }else {

            route_list.add(origin.latLng.gLatLng());

            com.example.savel.Location dep = null;
            com.example.savel.Location arr = null;

            for(int i = 0; i < travel.locations.size(); i++){
                if(travel.locations.get(i).type == 0){
                    dep = travel.locations.get(i);
                }else if(travel.locations.get(i).type == 1){
                    arr = travel.locations.get(i);
                }
            }
            com.example.savel.Location last = dep;

            for(int i = 0; i < travel.locations.size(); i++){
                if(travel.locations.get(i).type == 2){
                    com.example.savel.Location location = travel.locations.get(i);
                     if(location != dep && location != arr){
                         route_list.add(location.latLng.gLatLng());
                         drawPath(last.latLng.gLatLng(), location.latLng.gLatLng(), location.isPassed);
                         last = location;
                     }
                }
            }
            route_list.add(dest.latLng.gLatLng());
            drawPath(last.latLng.gLatLng(), arr.latLng.gLatLng(), arr.isPassed);
        }
    }

    public boolean isLocationOnPath(LatLng loc, List<LatLng> path){
        return PolyUtil.isLocationOnPath(loc, path, false, DEFAULT_TOLERANCE);
    }


    private float calculateLocationDifference(LatLng lastLocation, LatLng firstLocation) {
        float[] dist = new float[1];
        Location.distanceBetween(lastLocation.latitude, lastLocation.longitude, firstLocation.latitude, firstLocation.longitude, dist);
        return dist[0];
    }

    public void setGuide(DirectionsStep step){
        guideView.setVisibility(View.VISIBLE);
        btn_guide.setVisibility(View.GONE);

        createVibration();
        tv_guide.setText(Html.fromHtml(step.html_instructions).toString().trim());
        int icon = -1;
        if(step.maneuver != null) {
            switch (step.maneuver) {
                case "turn-slight-left":
                    icon = R.drawable.turn_slight_left_48px;
                    break;
                case "turn-sharp-left":
                    icon = R.drawable.turn_sharp_left_48px;
                    break;
                case "turn-left":
                    icon = R.drawable.turn_left_48px;
                    break;
                case "turn-slight-right":
                    icon = R.drawable.turn_slight_right_48px;
                    break;
                case "turn-sharp-right":
                    icon = R.drawable.turn_sharp_right_48px;
                    break;
                case "keep-right":
                    icon = R.drawable.east_48px;
                    break;
                case "keep-left":
                    icon = R.drawable.west_48px;
                    break;
                case "uturn-left":
                    icon = R.drawable.u_turn_left_48px;
                    break;
                case "uturn-right":
                    icon = R.drawable.u_turn_right_48px;
                    break;
                case "turn-right":
                    icon = R.drawable.turn_right_48px;
                    break;
                case "straight":
                    icon = R.drawable.straight_48px;
                    break;
                case "ramp-left":
                    icon = R.drawable.ramp_left_48px;
                    break;
                case "ramp-right":
                    icon = R.drawable.ramp_right_48px;
                    break;
                case "merge":
                    icon = R.drawable.merge_48px;
                    break;
                case "fork-left":
                    icon = R.drawable.fork_left_48px;
                    break;
                case "fork-right":
                    icon = R.drawable.fork_right_48px;
                    break;

                default:
                    icon = -1;
                    break;
            }
        }
        if(icon != -1){
            iv_guide.setImageResource(icon);
        }else {
            iv_guide.setImageResource(R.drawable.signpost_48px);
        }
        if(step.distance != null){
            tv_dist.setText(step.distance.value+"m");
        }
    }

    @Override
    public boolean onMyLocationButtonClick() {
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        locationPermissionGranted = false;
        if (requestCode
                == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {// If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationPermissionGranted = true;
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
        updateLocationUI();
    }

    private void startLocationUpdates() {
        fusedLocationProviderClient.requestLocationUpdates(mLocationRequest,
                locationCallback,
                Looper.getMainLooper());
        isStart = false;
    }

    private void createLocationRequest() {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(500);
        mLocationRequest.setFastestInterval(500);
        mLocationRequest.setPriority(Priority.PRIORITY_HIGH_ACCURACY);
    }

    private void createVibration(){
        Vibrator vibrator = (Vibrator)getSystemService(VIBRATOR_SERVICE);
        vibrator.vibrate(DEFAULT_VIBRATE_TIME);
    }

    private void updateLocationUI() {
        if (map == null) {
            return;
        }
        LatLngBounds USBounds = new LatLngBounds(
                new LatLng(24, 125), // SW bounds
                new LatLng(48, 67)  // NE bounds
        );
        map.moveCamera(CameraUpdateFactory.newLatLngBounds(USBounds, 0));

        try {
            if (locationPermissionGranted) {
                map.setMyLocationEnabled(true);
                map.getUiSettings().setMyLocationButtonEnabled(false);
            } else {
                getLocationPermission();
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    public void updateTravelerLocationMarker(LatLng latLng, User traveler){
        if(travelerMarker != null) travelerMarker.remove();

        View view = LayoutInflater.from(NavigationActivity.this).inflate(R.layout.marker_profile, (RelativeLayout) findViewById(R.id.rv));

        CircleImageView circleImageView = view.findViewById(R.id.profile);
        IconGenerator iconFactory = new IconGenerator(this);
        iconFactory.setContentView(view);
        iconFactory.setBackground(null);
        Activity activity = NavigationActivity.this;
        if (activity.isFinishing()){
            circleImageView.setImageResource(R.drawable.profile);
        }else {
            if(traveler.profile != null){
                Glide.with(NavigationActivity.this).load(Uri.parse(traveler.profile)).into(circleImageView);
            }else {
                circleImageView.setImageResource(R.drawable.profile);
            }
        }

        travelerMarker = map.addMarker(new MarkerOptions()
                .position(latLng).icon(BitmapDescriptorFactory.fromBitmap(iconFactory.makeIcon()))
        );

    }

    public void showMessage(String message, int type){
        createVibration();
        tv_message.setText(message);
        if(type == 0){ //warning
            iv_message.setImageResource(R.drawable.warning_48px);
        }else if(type == 1){ //good
            iv_message.setImageResource(com.zhihu.matisse.R.drawable.ic_check_white_18dp);
        }else if(type == 2){ //메시지
            iv_message.setImageResource(R.drawable.chat_48px);
        }
        messageView.setVisibility(View.VISIBLE);
        ObjectAnimator animator = ObjectAnimator.ofFloat(messageView, "alpha", 0f, 1f);
        animator.setDuration(1000);
        animator.start();
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                ObjectAnimator animator = ObjectAnimator.ofFloat(messageView, "alpha", 1f, 1f);
                animator.setDuration(1500);
                animator.start();
                animator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        ObjectAnimator animator = ObjectAnimator.ofFloat(messageView, "alpha", 1f, 0f);
                        animator.setDuration(1000);
                        animator.start();
                        animator.addListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                messageView.setVisibility(View.GONE);
                            }
                        });
                    }
                });
            }
        });



    }

    @Override
    protected void onResume() {
        super.onResume();
        if (locationPermissionGranted) {
            startLocationUpdates();
        }
        setPurposeTask();
        setTimerTask();
    }


    @Override
    protected void onPause() {
        super.onPause();
        if (locationPermissionGranted) {
            stopLocationUpdates();
        }
        if(purpose != null){
            purpose.cancel();
        }
        if(timer != null){
            timer.cancel();
        }
    }

    private void stopLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }




    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    private void setTravelerLocation(LatLng latLng){
        mDatabase.child("travels").child(travelId).child("location").setValue(new CustomLatLng(latLng.latitude, latLng.longitude)).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

            }
        });
    }

    private void getTravelerLocation(){
        mDatabase.child("travels").child(travelId).child("location").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()){
                        CustomLatLng lng = snapshot.getValue(CustomLatLng.class);
                        if(lng != null){
                              myLocation = lng.gLatLng();
                              updateTravelerLocationMarker(myLocation, travel.travelSetting.traveler);
                              moveCamera(null, myLocation);
                        }else {
                            map.moveCamera(CameraUpdateFactory.newLatLngZoom(origin.latLng.gLatLng(), DEFAULT_ZOOM_ROUTE));
                        }
                    }else {
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(origin.latLng.gLatLng(), DEFAULT_ZOOM_ROUTE));
                    }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void sendFCM(User traveler, String token, int status, int check){

        Map<String,String> map = new HashMap<>();
        map.put("travelId",travelId);
        map.put("profile",traveler.getProfile());

        if(status == Travel.USER_STATUS_MOVING){
             map.put("title", "Travel started");
             map.put("message", traveler.name + " has started a travel");
        }else if(status == Travel.USER_STATUS_ARRIVAL){
            map.put("title", "Arrived at destination");
            map.put("message", traveler.name + " has arrived at destination");
        }else if(status == Travel.USER_STATUS_WARNING){
            map.put("title", "Out of Path");
            map.put("message", traveler.name + " has strayed from the path.");
        }else if(status == Travel.USER_STATUS_NOT_WARNING){
            map.put("title", "In of Path");
            map.put("message", traveler.name + " is on the right path.");
        }else if(status == Travel.USER_STATUS_ARRIVAL_CHECKPOINT){
            map.put("title", "Arrived at check point");
            map.put("message", traveler.name + " has arrived at Check Point " + check);
        }
        FCM_Push fcm_push = new FCM_Push(NavigationActivity.this, token, map);
        fcm_push.send();
    }

    public void moveCamera(Location location, LatLng myLocation){
        if(Camera_Mode == CAMERA_MODE_NAVIGATION) {
            CameraPosition cameraPosition;
            if(location == null){
                cameraPosition = new CameraPosition.Builder()
                        .target(myLocation)
                        .zoom(DEFAULT_ZOOM)
                        .build();
            }else{
                cameraPosition = new CameraPosition.Builder()
                        .target(myLocation)
                        .bearing(location.getBearing())
                        .zoom(DEFAULT_ZOOM)
                        .build();
            }
            map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 100, null);
        }
    }

    public void setTravelerWork(){
        if(locationPermissionGranted) {

            createLocationRequest();
            locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    if (locationResult == null) {
                        return;
                    }

                    Location location = locationResult.getLastLocation();
                    if(location == null) return;

                    myLocation = new LatLng(location.getLatitude(),
                            location.getLongitude());

                    setTravelerLocation(myLocation);

                    moveCamera(location, myLocation);

                    if(travel == null) return;

                    if(response != null) {
                        DirectionsStep step = findSteps(myLocation, response);
                        if(step != null){
                            setGuide(step);
                            changeStepIcon(step);
                            currentStep = step;
                            drawAllPath();
                            saveTravelData(travel);
                        }
                    }

                    int marker = findCheckPoint(myLocation, response);
                    if(marker != -1){
                        showMessage("Arrival at Check Point" + marker, 0);
                        travel.user_status = Travel.USER_STATUS_ARRIVAL_CHECKPOINT;
                        saveTravelData(travel);
                        if(response == null){
                            drawAllPath();
                        }
                        setMarker(travel.locations);
                        sendFCM(travel.getTravelSetting().traveler, travel.getTravelSetting().guardian.token, Travel.USER_STATUS_ARRIVAL_CHECKPOINT, marker);
                    }

                    if(travel.user_status != Travel.USER_STATUS_ARRIVAL) {
                        if (isArrivedDest(myLocation, dest) && !dest.isPassed) {
                            showMessage("Arrival at Destination", 0);
                            dest.isPassed = true;
                            travel.user_status = Travel.USER_STATUS_ARRIVAL;
                            endAllSteps(response);
                            saveTravelData(travel);
                            drawAllPath();
                            setMarker(travel.locations);
                            sendFCM(travel.getTravelSetting().traveler, travel.getTravelSetting().guardian.token, Travel.USER_STATUS_ARRIVAL, marker);
                        }
                    }

                    if(!isStart && travel.user_status != Travel.USER_STATUS_ARRIVAL){
                        if(travel.user_status == Travel.USER_STATUS_NOT_MOVING){
                            if(isLocationOnPath(myLocation, route_list)){
                                isStart = true;
                                travel.user_status = Travel.USER_STATUS_MOVING;
                                saveTravelData(travel);
                                sendFCM(travel.getTravelSetting().traveler, travel.getTravelSetting().guardian.token, Travel.USER_STATUS_MOVING, 0);
                            }
                        }else {
                            isStart = true;
                            travel.user_status = Travel.USER_STATUS_MOVING;
                            saveTravelData(travel);
                            sendFCM(travel.getTravelSetting().traveler, travel.getTravelSetting().guardian.token, Travel.USER_STATUS_MOVING, 0);
                        }

                    }
                }
            };

            startLocationUpdates();

        }
    }

    public void setTimerTask(){
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                NavigationActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(travel != null && travel.user_status != Travel.USER_STATUS_ARRIVAL){
                            if(travel.user_status != Travel.USER_STATUS_NOT_MOVING){
                                if(myLocation != null && route_list.size() > 1) {
                                    if (!isLocationOnPath(myLocation, route_list)) {
                                        showMessage("You have strayed from the path.", 0);
                                        if(travel.user_status != Travel.USER_STATUS_WARNING){
                                            sendFCM(travel.getTravelSetting().traveler, travel.getTravelSetting().guardian.token, Travel.USER_STATUS_WARNING, 0);
                                        }
                                        travel.user_status = Travel.USER_STATUS_WARNING;

                                    }else {
                                        if(travel.user_status == Travel.USER_STATUS_WARNING){
                                            showMessage("You are on the right path.", 1);
                                            travel.user_status = Travel.USER_STATUS_NOT_WARNING;
                                            sendFCM(travel.getTravelSetting().traveler, travel.getTravelSetting().guardian.token, Travel.USER_STATUS_NOT_WARNING, 0);
                                        }
                                    }
                                    saveTravelData(travel);
                                }
                            }
                        }
                    }
                });
            }
        };
        timer = new Timer();
        timer.schedule(timerTask, 0, DEFAULT_ROUTE_CHECK_TIME);
    }

    public void setPurposeTask(){
        TimerTask purposeTask = new TimerTask() {
            @Override
            public void run() {
                NavigationActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(travel != null && travel.user_status != Travel.USER_STATUS_ARRIVAL && travel.getTravelSetting().traveler.uid.equals(currentUser.getUid())){
                            showMessage(travel.travelSetting.purpose, 1);
                        }
                    }
                });
            }
        };
        purpose = new Timer();
        purpose.schedule(purposeTask, 0, DEFAULT_PURPOSE_CHECK_TIME);
    }




    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.map = googleMap;
        map.setOnMyLocationButtonClickListener(this);
        map.setOnMyLocationClickListener(this);

        setInit(travelId);

    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {

    }

}