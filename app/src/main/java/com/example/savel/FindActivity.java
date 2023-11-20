package com.example.savel;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FetchPlaceResponse;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.maps.android.ui.IconGenerator;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class FindActivity extends AppCompatActivity    implements
        GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener,
        OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback {

    private LinearLayout mother;
    private HorizontalScrollView scrollView;
    boolean isexpand = false;
    private List<Location> locations = new ArrayList<>();
    private List<Marker> markers = new ArrayList<>();
    private List<Marker> search_markers = new ArrayList<>();
    private SlidingUpPanelLayout layout;
    private TextView tv_result;
    private RecyclerView rc;
    private long pressedTime;
    private Polyline lastPolyline;
    private PlacesClient placesClient;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean locationPermissionGranted;
    private GoogleMap map;
    private SupportMapFragment mapFragment;
    private static final int DEFAULT_ZOOM = 15;
    private int searchComplete = 0;
    private int searchResult = 0;
    private List<Location> searchLocations = new ArrayList<>();
    private DirectionsResponse directionsResponse;
    private float distance;

    FirebaseAuth mAuth;
    FirebaseUser currentUser;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Locale.setDefault(new Locale("en"));
        setContentView(R.layout.activity_find);
        Intent intent = getIntent();
        TravelSetting setting = (TravelSetting) intent.getSerializableExtra("setting");
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        Places.initialize(getApplicationContext(), getString(R.string.maps_api_key));
        placesClient = Places.createClient(this);

        // Construct a FusedLocationProviderClient.
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        mother = findViewById(R.id.mother);
        scrollView = findViewById(R.id.scroll);
        tv_result = findViewById(R.id.tv_result);
        ImageView iv_del = findViewById(R.id.delete);
        rc = findViewById(R.id.rc);
        rc.setLayoutManager(new LinearLayoutManager(this));
        rc.addItemDecoration(new DividerItemDecoration(FindActivity.this, 1));
        layout = findViewById(R.id.main_panel);
        layout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
        layout.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {

            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
                   System.out.println(newState);
            }
        });
        CardView bar = findViewById(R.id.bar);
        CardView search_bar = findViewById(R.id.search);
        ImageView iv_search = findViewById(R.id.iv_search);
        ImageView iv_expand = findViewById(R.id.iv_expand);
        ImageView iv_back = findViewById(R.id.back);
        ImageView iv_done = findViewById(R.id.iv_done);
        iv_done.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                Location dep = null;
                Location arr = null;

                for(int i = 0; i < locations.size(); i++){
                    if(locations.get(i).type == 0){
                        dep = locations.get(i);
                    }else if(locations.get(i).type == 1){
                        arr = locations.get(i);
                    }
                }

                if(dep != null && arr != null){
                     saveTravelData(locations, directionsResponse, setting);
                }else {
                    Toast.makeText(FindActivity.this, "Please set the route including the origin and destination.", Toast.LENGTH_LONG).show();
                }
            }
        });

        iv_expand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isexpand) {
                    mother.setVisibility(View.GONE);
                    isexpand = false;
                }else {
                    mother.setVisibility(View.VISIBLE);
                    isexpand = true;
                }
            }
        });
        iv_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bar.setVisibility(View.GONE);
                search_bar.setVisibility(View.VISIBLE);
            }
        });
        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bar.setVisibility(View.VISIBLE);
                search_bar.setVisibility(View.GONE);
                for(Marker marker : search_markers){
                    marker.remove();
                }
                search_markers.clear();
                layout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
            }
        });

        EditText editText = findViewById(R.id.ed_search);
        editText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    //키패드 내리기
                    InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);

                    //처리
                    String s = editText.getText().toString();
                    if(s.length() > 0){
                        SearchLocation(s);
                    }
                    return true;
                }
                return false;
            }
        });

        iv_del.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                  editText.setText(null);
                for(Marker marker : search_markers){
                    marker.remove();
                }
                search_markers.clear();
                layout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
            }
        });
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
                map.getUiSettings().setMyLocationButtonEnabled(true);
                View locationButton = ((View) mapFragment.getView().findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
                RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
                rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
                rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
                rlp.setMargins(0, 180, 180, 0);
            } else {
                map.setMyLocationEnabled(false);
                map.getUiSettings().setMyLocationButtonEnabled(false);
                getLocationPermission();
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
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

    public void SearchLocation(String query) {
        searchResult = 0;
        searchComplete = 0;
        searchLocations.clear();

        AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();
        FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                // Call either setLocationBias() OR setLocationRestriction().
                //.setLocationRestriction(bounds)
                .setQuery(query)
                .setSessionToken(token)
                .build();

        placesClient.findAutocompletePredictions(request).addOnSuccessListener((response) -> {
            List<AutocompletePrediction> list = response.getAutocompletePredictions();
            if(list.size() == 0){
                Toast.makeText(FindActivity.this, "No results found", Toast.LENGTH_LONG).show();
            }else {
                for (AutocompletePrediction prediction : list) {
                    SearchPlaceId(prediction.getPlaceId(), list.size());
                }
            }
        }).addOnFailureListener((exception) -> {
            Toast.makeText(FindActivity.this, "No results found", Toast.LENGTH_LONG).show();
        });

    }

    public void SearchPlaceId(String placeId, int size){

        final List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG);

        final FetchPlaceRequest request = FetchPlaceRequest.newInstance(placeId, placeFields);

        placesClient.fetchPlace(request).addOnCompleteListener(new OnCompleteListener<FetchPlaceResponse>() {
            @Override
            public void onComplete(@NonNull Task<FetchPlaceResponse> task) {
                searchComplete++;
                if(task.isSuccessful()){
                    searchResult++;
                    FetchPlaceResponse response = task.getResult();
                    Place places = response.getPlace();
                    if(places.getLatLng() != null) {
                        CustomLatLng lng = new CustomLatLng();
                        lng.sLatLng(places.getLatLng());
                        Location location = new Location(places.getName(), places.getAddress(), -1, lng, true, false);
                        searchLocations.add(location);
                    }
                }

                if(searchComplete == size){
                    tv_result.setText(searchResult + " search result");
                    setSearchMarker(searchLocations);
                    MoveCamera(searchLocations);
                    SearchListAdopter adopter = new SearchListAdopter(searchLocations, search_markers, FindActivity.this);
                    rc.setAdapter(adopter);
                    new Handler().postDelayed(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            layout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
                            //딜레이 후 시작할 코드 작성
                        }
                    }, 100);
                }
            }
        });
    }



    public void showDialog(Location location, boolean isEdit, Marker marker, TextView textView){
        AlertDialog.Builder builder = new AlertDialog.Builder(FindActivity.this);
        View view = LayoutInflater.from(FindActivity.this).inflate(R.layout.location_dialog, (RelativeLayout) findViewById(R.id.dialog));
        EditText ed_location = view.findViewById(R.id.ed_email);
        TextView btn_dep = view.findViewById(R.id.btn_travel);
        TextView btn_arr = view.findViewById(R.id.btn_arr);
        TextView btn_check = view.findViewById(R.id.btn_guard);
        ed_location.setText(location.address);

        if(isEdit && location.type != -1){
            if (location.type == 0) {
                clear_btn(btn_dep, btn_arr, btn_check, btn_dep);
            }else if(location.type == 1){
                clear_btn(btn_dep, btn_arr, btn_check, btn_check);
            }else {
                clear_btn(btn_dep, btn_arr, btn_check, btn_arr);
            }
        }
        btn_dep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clear_btn(btn_dep, btn_arr, btn_check, btn_dep);
                location.type = 0;

            }
        });
        btn_arr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clear_btn(btn_dep, btn_arr, btn_check, btn_arr);
                location.type = 1;
            }
        });
        btn_check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clear_btn(btn_dep, btn_arr, btn_check, btn_check);
                location.type = 2;
            }
        });
        builder.setView(view);
        builder.setCancelable(false);
        builder.setPositiveButton("OK", null);
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                         String s = ed_location.getText().toString().trim();
                         if(s.length() > 0){
                             if(location.type == -1){
                                 Toast.makeText(FindActivity.this, "Please select Place Type", Toast.LENGTH_LONG).show();
                             }else {
                                 location.address = s;
                                 if(!isEdit){
                                     locations.add(location);
                                 }
                                 if(location.type != 2){
                                     for(Location loc : locations){
                                         if(loc != location && loc.type == location.type){
                                             locations.remove(loc);
                                             break;
                                         }
                                     }
                                 }
                                 setMarker();
                                 addAllLocationView();
                                 getDirections();
                                 if(marker != null){
                                     marker.remove();
                                 }
                                 if(textView != null) {
                                     textView.setText("Edit");
                                 }
                                 if(layout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED) {
                                     layout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                                 }
                                 dialog.dismiss();
                             }
                         }else {
                             ed_location.setError("Please input Place Address");
                         }
                    }
                });
            }
        });
        alertDialog.show();
    }
    public void MoveCamera(List<Location> locations){
        double top = 0;
        double bottom = Integer.MAX_VALUE;
        double left = Integer.MAX_VALUE;
        double right = 0;
        for(Location location : locations){
            top = Math.max(top, location.latLng.longitude); //고위도
            bottom = Math.min(bottom, location.latLng.longitude); //저위도
            left = Math.min(left, location.latLng.latitude);
            right = Math.max(right, location.latLng.latitude);
        }
        LatLngBounds latLngBounds = new LatLngBounds(new LatLng(left, bottom), new LatLng(right, top));
        System.out.println(latLngBounds);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(latLngBounds, 100);
        map.moveCamera(cameraUpdate);

    }

    public void addAllLocationView(){
        mother.setVisibility(View.VISIBLE);
        isexpand = true;
        mother.removeAllViews();
        Location dep = null;
        Location arr = null;

        for(int i = 0; i < locations.size(); i++){
            if(locations.get(i).type == 0){
                dep = locations.get(i);
            }else if(locations.get(i).type == 1){
                arr = locations.get(i);
            }
        }
        if(dep != null){
            addLocationView(dep, 0, false);
        }
        int m = 0;
        for(int i = 0; i < locations.size(); i++){
            if(locations.get(i).type == 2){
                m++;
                addLocationView(locations.get(i), m, false);
            }
        }
        if(arr != null){
            addLocationView(arr, 0, true);
        }
        new Handler().postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                scrollView.fullScroll(ScrollView.FOCUS_RIGHT);
                //딜레이 후 시작할 코드 작성
            }
        }, 100);// 0.6초 정도 딜레이를 준 후 시작

    }
    public void addLocationView(Location location, int index, boolean isfinal){
        LocationView locationView = new LocationView(FindActivity.this);
        setLocationView(location, locationView, index, isfinal);
        mother.addView(locationView);
    }

    public void removeLocation(Location location){
         locations.remove(location);
         setMarker();
         getDirections();
         addAllLocationView();
    }
    public void setLocationView(Location location, LocationView locationView, int index, boolean isfinal){
         locationView.setTag(location);
         TextView tv_title = locationView.findViewById(R.id.tv_title);
         TextView tv_location = locationView.findViewById(R.id.tv_location);
         ImageView iv_icon = locationView.findViewById(R.id.iv_icon);
         ImageView iv_next = locationView.findViewById(R.id.iv_next);
         if(isfinal){
             iv_next.setVisibility(View.GONE);
         }else {
             iv_next.setVisibility(View.VISIBLE);
         }
         ImageView iv_delete = locationView.findViewById(R.id.iv_delete);
         RelativeLayout btn_edit = locationView.findViewById(R.id.btn_edit);
         tv_title.setText(location.type == 0 ? "Origin" : location.type == 1 ? "Destination" : "Check Point " + index);
         iv_icon.setImageResource(location.type > 1 ? R.drawable.where_to_vote_48px : R.drawable.flag_48px);
         tv_location.setText(location.address);
         btn_edit.setTag(locationView);
         iv_delete.setTag(locationView);
         iv_delete.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 LocationView loc = (LocationView) view.getTag();
                 removeLocation((Location) loc.getTag());
             }
         });
         btn_edit.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                  LocationView loc = (LocationView) view.getTag();
                  showDialog((Location) loc.getTag(), true, null, null);
             }
         });
    }

    public void setMarker(){
        for(Marker marker : markers){
            marker.remove();
        }
        Location dep = null;
        Location arr = null;
        View view = LayoutInflater.from(FindActivity.this).inflate(R.layout.icon, (RelativeLayout) findViewById(R.id.rv));
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
            .position(dep.latLng.gLatLng()).title("Origin").icon(BitmapDescriptorFactory.fromBitmap(iconFactory.makeIcon()))
            );
            de.setTag(dep);
            markers.add(de);
        }
        int m = 0;
        iv_icon.setImageResource(R.drawable.done_48px);
        iv_marker.setImageTintList(ColorStateList.valueOf(Color.parseColor("#F89E3E")));
        fill.setBackgroundColor(Color.parseColor("#F89E3E"));
        for(int i = 0; i < locations.size(); i++){
            if(locations.get(i).type == 2){
                m++;
                textView.setText("Check Point " + (m));
                Marker check = map.addMarker(new MarkerOptions()
                        .position(locations.get(i).latLng.gLatLng()).title("Check Point " + (m)).icon(BitmapDescriptorFactory.fromBitmap(iconFactory.makeIcon()))
                );
                check.setTag(locations.get(i));
                markers.add(check);
            }
        }
        if(arr != null){
            iv_icon.setImageResource(R.drawable.flag_48px);
            iv_marker.setImageTintList(ColorStateList.valueOf(Color.parseColor("#F44336")));
            fill.setBackgroundColor(Color.parseColor("#F44336"));
            textView.setText("Destination");
            Marker ar = map.addMarker(new MarkerOptions()
                    .position(arr.latLng.gLatLng()).title(String.format("%.2fkm", getDistance())).icon(BitmapDescriptorFactory.fromBitmap(iconFactory.makeIcon()))
            );
            ar.setTag(arr);
            ar.showInfoWindow();
            markers.add(ar);
        }
    }

    public float getDistance(){
        float dist = 0;

        if(directionsResponse != null){
            List<DirectionsRoute> routes = directionsResponse.routes;
            for(DirectionsRoute route : routes) {
                List<DirectionsLeg> legs = route.legs;
                for (DirectionsLeg leg : legs) {
                    dist += Double.parseDouble(leg.distance.value);
                }
            }
        }else {
            Location dep = null;
            Location arr = null;
            for(int i = 0; i < locations.size(); i++){
                if(locations.get(i).type == 0){
                    dep = locations.get(i);
                }else if(locations.get(i).type == 1){
                    arr = locations.get(i);
                }else {
                    if(dep == null){
                        dep = locations.get(i);
                    }
                    if(arr == null || arr.type != 1){
                        arr = locations.get(i);
                    }
                }
            }

            Location last = dep;
            if(dep != arr && arr != null){
                for(int i = 0; i < locations.size(); i++){
                    Location location = locations.get(i);
                    if(last != location && location != arr){
                         float[] res = new float[3];
                         android.location.Location.distanceBetween(last.latLng.latitude, last.latLng.longitude, location.latLng.latitude, location.latLng.longitude, res);
                         dist += res[0];
                         last = location;
                    }
                }
                float[] res = new float[3];
                android.location.Location.distanceBetween(last.latLng.latitude, last.latLng.longitude, arr.latLng.latitude, arr.latLng.longitude, res);
                dist += res[0];
            }

        }

        return distance = dist / 1000;

    }

    public void getDirections(){
        Location dep = null;
        Location arr = null;

        for(int i = 0; i < locations.size(); i++){
            if(locations.get(i).type == 0){
                dep = locations.get(i);
            }else if(locations.get(i).type == 1){
                arr = locations.get(i);
            }else {
                if(dep == null){
                    dep = locations.get(i);
                }
                if(arr == null || arr.type != 1){
                    arr = locations.get(i);
                }
            }
        }

        if(dep != arr && arr != null){
            String origin = dep.latLng.latitude + "," + dep.latLng.longitude;
            String dest = arr.latLng.latitude + "," + arr.latLng.longitude;

            StringBuilder waypoints = new StringBuilder();
            for(int i = 0; i < locations.size(); i++){
                Location location = locations.get(i);
                if(location != dep && location != arr){
                     String latlng = location.latLng.latitude+","+location.latLng.longitude;
                     waypoints.append("via:").append(latlng).append("|");
                }
            }
            String way = null;
            if(waypoints.length() != 0){
                waypoints.deleteCharAt(waypoints.length()-1);
                way = waypoints.toString();
            }


            LoadingView loadingView = new LoadingView(FindActivity.this);
            loadingView.show("Searching for a route...");
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("https://maps.googleapis.com")
                    .addConverterFactory(GsonConverterFactory.create())
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .build();

            DirectionsRetrofit retrofitService = retrofit.create(DirectionsRetrofit.class);
            Call<DirectionsResponse> call = retrofitService.getPosts(origin, dest,  "walking", way, getString(R.string.maps_api_key));
            call.enqueue(new Callback<DirectionsResponse>() {
                @Override
                public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
                    loadingView.stop();
                    if(response.body() != null && response.body().status.equals("OK")){
                        directionsResponse = response.body();
                        setDirectionPath(directionsResponse);
                    }else {
                        Toast.makeText(FindActivity.this, "Failed to find recommended route", Toast.LENGTH_LONG).show();
                        setPath();
                    }
                }

                @Override
                public void onFailure(Call<DirectionsResponse> call, Throwable t) {
                    loadingView.stop();
                    System.out.println(t);
                    setPath();
                    Toast.makeText(FindActivity.this, t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });

        }else {
            setPath();
        }

    }

    public void setSearchMarker(List<Location> locations){
        View view = LayoutInflater.from(FindActivity.this).inflate(R.layout.icon, (RelativeLayout) findViewById(R.id.rv));
        ImageView iv_marker = view.findViewById(R.id.imageView);
        ImageView iv_icon = view.findViewById(R.id.iv_icon);
        View fill = view.findViewById(R.id.view);
        TextView textView = view.findViewById(R.id.textView);
        IconGenerator iconFactory = new IconGenerator(this);
        iconFactory.setContentView(view);
        iconFactory.setBackground(null);

        for(Marker marker : search_markers){
            marker.remove();
        }
        search_markers.clear();
        iv_icon.setImageResource(R.drawable.search_48px);
        iv_marker.setImageTintList(ColorStateList.valueOf(Color.parseColor("#00ACC1")));
        fill.setBackgroundColor(Color.parseColor("#00ACC1"));
        for(int i = 0; i < locations.size(); i++){
            Location location = locations.get(i);
            textView.setText(location.name);
            Marker check = map.addMarker(new MarkerOptions()
                    .position(locations.get(i).latLng.gLatLng()).title(location.name).icon(BitmapDescriptorFactory.fromBitmap(iconFactory.makeIcon()))
            );
            check.setTag(location);
            check.showInfoWindow();
            search_markers.add(check);
        }
    }


    public void setPath(){
        if(lastPolyline != null) lastPolyline.remove();
        List<LatLng> list = new ArrayList<>();
        Location dep = null;
        Location arr = null;
        for(int i = 0; i < locations.size(); i++){
            if(locations.get(i).type == 0){
                dep = locations.get(i);
            }else if(locations.get(i).type == 1){
                arr = locations.get(i);
            }
        }
        if(dep != null){
            list.add(dep.latLng.gLatLng());
        }
        for(int i = 0; i < locations.size(); i++){
            if(locations.get(i).type == 2){
                 list.add(locations.get(i).latLng.gLatLng());
            }
        }
        if(arr != null){
            list.add(arr.latLng.gLatLng());
        }
        if(list.size() >= 2) {
            PolylineOptions polylineOptions = new PolylineOptions();
            polylineOptions.addAll(list);
            polylineOptions.color(Color.parseColor("#1E88E5"));
            polylineOptions.width(30);
            lastPolyline = map.addPolyline(polylineOptions);
            lastPolyline.setJointType(JointType.ROUND);
            lastPolyline.setStartCap(new RoundCap());
            lastPolyline.setEndCap(new RoundCap());

        }

    }

    public void setDirectionPath(DirectionsResponse directionsResponse){

        List<DirectionsRoute> routes = directionsResponse.routes;
        List<LatLng> list = new ArrayList<>();

        Location dep = null;
        Location arr = null;

        for(int i = 0; i < locations.size(); i++){
            if(locations.get(i).type == 0){
                dep = locations.get(i);
            }else if(locations.get(i).type == 1){
                arr = locations.get(i);
            }else {
                if(dep == null){
                    dep = locations.get(i);
                }
                if(arr == null || arr.type != 1){
                    arr = locations.get(i);
                }
            }
        }


        for(DirectionsRoute route : routes){
            List<DirectionsLeg> legs = route.legs;
            for(DirectionsLeg leg : legs){
                for(int i = 0; i < leg.steps.size(); i++) {
                    DirectionsStep step = leg.steps.get(i);
                    LatLng start = new LatLng(step.start_location.lat, step.start_location.lng);
                    LatLng end = new LatLng(step.end_location.lat, step.end_location.lng);
                    if(i == 0){
                        list.add(dep.latLng.gLatLng());
                        list.add(start);
                    }
                    list.add(start);
                    list.add(end);

                    if(i == leg.steps.size()-1){
                        list.add(end);
                        list.add(arr.latLng.gLatLng());
                    }
                }
            }
        }

        if(lastPolyline != null) lastPolyline.remove();
        if(list.size() >= 2) {
            PolylineOptions polylineOptions = new PolylineOptions();
            polylineOptions.addAll(list);
            polylineOptions.color(Color.parseColor("#1E88E5"));
            polylineOptions.width(20);
            lastPolyline = map.addPolyline(polylineOptions);
            lastPolyline.setJointType(JointType.ROUND);
            lastPolyline.setStartCap(new RoundCap());
            lastPolyline.setEndCap(new RoundCap());
        }
    }

    public void clear_btn(TextView dep, TextView arr, TextView check, TextView select){
        dep.setBackgroundResource(R.drawable.stroke_login);
        dep.setTextColor(Color.parseColor("#222222"));
        arr.setBackgroundResource(R.drawable.stroke_login);
        arr.setTextColor(Color.parseColor("#222222"));
        check.setBackgroundResource(R.drawable.stroke_login);
        check.setTextColor(Color.parseColor("#222222"));
        select.setBackgroundResource(R.drawable.stroke_activie);
        select.setTextColor(Color.WHITE);
    }

    public void ReverseGeo(LatLng latLng) throws UnsupportedEncodingException {
        LoadingView loadingView = new LoadingView(FindActivity.this);
        loadingView.show("loading...");
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://maps.googleapis.com")
                .addConverterFactory(GsonConverterFactory.create())
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();

        ReverseRetrofit retrofitService = retrofit.create(ReverseRetrofit.class);
        Call<ReverseGeoResponse> call = retrofitService.getPosts(latLng.latitude+","+latLng.longitude, getString(R.string.maps_api_key));
        call.enqueue(new Callback<ReverseGeoResponse>() {
            @Override
            public void onResponse(Call<ReverseGeoResponse> call, Response<ReverseGeoResponse> response) {
                loadingView.stop();
                  if(response.body() != null && response.body().status.equals("OK")){
                       ReverseGeoResult result = response.body().results.get(0);
                       CustomLatLng lng = new CustomLatLng();
                       lng.sLatLng(latLng);
                       showDialog(new Location("", result.formatted_address,-1, lng, false, false), false, null, null);
                  }else {
                      Toast.makeText(FindActivity.this, "Failed to get address", Toast.LENGTH_LONG).show();
                  }
            }

            @Override
            public void onFailure(Call<ReverseGeoResponse> call, Throwable t) {
                loadingView.stop();
                Toast.makeText(FindActivity.this, "Failed to get address", Toast.LENGTH_LONG).show();
            }
        });

    }


    @Override
    public void onBackPressed() {
        if (layout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED) {
            layout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        } else {
            if (pressedTime == 0) {
                Toast.makeText(FindActivity.this, "Press once more to exit.", Toast.LENGTH_LONG).show();
                pressedTime = System.currentTimeMillis();
            } else {
                int seconds = (int) (System.currentTimeMillis() - pressedTime);

                if (seconds > 2000) {
                    Toast.makeText(FindActivity.this, "Press once more to exit.", Toast.LENGTH_LONG).show();
                    pressedTime = 0;
                } else {
                    super.onBackPressed();

                    finish(); // app 종료 시키기
                }
            }
        }
    }


    @SuppressLint("PotentialBehaviorOverride")
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.map = googleMap;
        map.setOnMyLocationButtonClickListener(this);
        map.setOnMyLocationClickListener(this);
        getLocationPermission();
        updateLocationUI();
        if(locationPermissionGranted){
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<android.location.Location>() {
                @Override
                public void onSuccess(android.location.Location location) {
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                            new LatLng(location.getLatitude(),
                                    location.getLongitude()), DEFAULT_ZOOM));
                }
            });
        }
        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(@NonNull LatLng latLng) {
                if(layout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED){
                    layout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                }else {
                    try {
                        ReverseGeo(latLng);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(@NonNull Marker marker) {
                Location location = (Location) marker.getTag();
                showDialog(location, !location.isSearch, marker, null);
                return false;
            }
        });
    }

    @Override
    public boolean onMyLocationButtonClick() {
        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull android.location.Location location) {

    }

    public void saveTravelData(List<Location> locations, DirectionsResponse directionsResponse, TravelSetting travelSetting){
        LoadingView loadingView = new LoadingView(FindActivity.this);
        loadingView.show("Saving data...");
        Travel travel = new Travel(locations, directionsResponse, travelSetting, distance);
        db.collection("travels").add(travel).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
            @Override
            public void onComplete(@NonNull Task<DocumentReference> task) {
                 if(task.isSuccessful()){
                     DocumentReference documentReference = task.getResult();
                     String id = documentReference.getId();
                     db.collection("users").document(travelSetting.traveler.uid).update("travel", FieldValue.arrayUnion(id)).addOnCompleteListener(new OnCompleteListener<Void>() {
                         @Override
                         public void onComplete(@NonNull Task<Void> task) {
                             if(task.isSuccessful()){
                                 db.collection("users").document(currentUser.getUid()).update("travel", FieldValue.arrayUnion(id)).addOnCompleteListener(new OnCompleteListener<Void>() {
                                     @Override
                                     public void onComplete(@NonNull Task<Void> task) {
                                         if(task.isSuccessful()){
                                             Toast.makeText(FindActivity.this, "Data save succeeded.", Toast.LENGTH_LONG).show();
                                             finish();
                                         }else {
                                             loadingView.stop();
                                             Toast.makeText(FindActivity.this, "Failed to save data.", Toast.LENGTH_LONG).show();
                                         }
                                     }
                                 });
                             }else {
                                 loadingView.stop();
                                 Toast.makeText(FindActivity.this, "Failed to save data.", Toast.LENGTH_LONG).show();
                             }
                         }
                     });
                 }else {
                     loadingView.stop();
                     Toast.makeText(FindActivity.this, "Failed to save data.", Toast.LENGTH_LONG).show();
                 }
            }
        });
    }
}