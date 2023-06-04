package com.example.myapplication.view;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.Api.NetworkService;
import com.example.myapplication.R;
import com.example.myapplication.adapters.ComplectAdapter;
import com.example.myapplication.model.Complect;
import com.example.myapplication.model.Main;
import com.example.myapplication.model.WeatherAll;
import com.example.myapplication.viewmodel.LoggedInViewModel;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoggedInFragment extends Fragment {
    public static final String COMPLECT_EXTRA="complect";
    private LoggedInViewModel loggedInViewModel;
    private TextView city;
    private TextView tempreture;
    private TextView nameUser;
    private RecyclerView rv;
    private DatabaseReference bd;
    private List<Complect> listComplect;
    private ComplectAdapter adapter;
    private String town;
    private ComplectAdapter.OnComplectClickListener listener;
    private FloatingActionButton fab;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        listComplect = new ArrayList<>();
        bd = FirebaseDatabase.getInstance().getReference();
        loggedInViewModel = new ViewModelProvider(this).get(LoggedInViewModel.class);
        listener=new ComplectAdapter.OnComplectClickListener() {
            @Override
            public void onComplectClick(Complect state, int position) {
                Bundle bundle=new Bundle();
                bundle.putSerializable(COMPLECT_EXTRA,state);
                getParentFragmentManager().setFragmentResult("requestKey",bundle);
                Navigation.findNavController(getView()).navigate(R.id.action_loggedInFragment_to_ComplectFragment);
            }
        };
        getGeoLocation();
        getCloses();
        loggedInViewModel.getLoggedOutLiveData().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean loggedOut) {
                if (loggedOut) {
                    Toast.makeText(getContext(), "User Logged Out", Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(getView()).navigate(R.id.action_loggedInFragment_to_loginRegisterFragment);
                }
            }
        });
    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_logged_in, container, false);
            city=view.findViewById(R.id.city);
            tempreture=view.findViewById(R.id.weather_text_view);
            rv=view.findViewById(R.id.type_of_closed);
            nameUser=view.findViewById(R.id.nameUser);
            fab=view.findViewById(R.id.fab);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Navigation.findNavController(getView()).navigate(R.id.action_loggedInFragment_to_ComplectFragment);
                }
            });

//                loggedInViewModel.logOut();
        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        ActivityResultLauncher<String[]> locationPermissionRequest=
                registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(),result -> {
                    Boolean fileLocationGranted=result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION,false);
                    Boolean coarseLocationGranted=result.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION,false);
                    if(fileLocationGranted!=null&&fileLocationGranted)
                    {

                    }
                    else if(coarseLocationGranted!=null&&coarseLocationGranted)
                    {

                    }
                    else {

                    }
                });
        locationPermissionRequest.launch(new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        });
        getGeoLocation();
    }
    private void getGeoLocation()
    {
        LocationProvider provider = new LocationProvider();
        provider.requestLocation(getActivity(), location -> {
            Geocoder geocoder = new Geocoder(getActivity());
            try {
                List<Address> addresses = geocoder.getFromLocation(location.getLatitude(),
                        location.getLongitude(), 1);
                town = addresses.get(0).getLocality();
                city.setText(town);
                NetworkService.getInstance().getJSONApi().getWeatherByCity("Калининград", NetworkService.KEY, "metric", "ru").
                        enqueue(new Callback<WeatherAll>() {
                            @Override
                            public void onResponse(Call<WeatherAll> call, Response<WeatherAll> response) {
                                WeatherAll weatherAll = response.body();
                                if(weatherAll!=null) {
                                    Main main = weatherAll.getMain();
                                    tempreture.setText(Integer.toString((int) main.getTemp()));
                                }
                            }
                            @Override
                            public void onFailure(Call<WeatherAll> call, Throwable t) {
                                Log.d("error", t.getMessage());
                            }
                        });
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
    private void getCloses()
    {
        loggedInViewModel.getUserLiveData().observe(this, new Observer<FirebaseUser>()
        {
            @Override
            public void onChanged(FirebaseUser firebaseUser)
            {
                if (firebaseUser != null)
                {
                    Query users = bd.child("Users");
                    users.addValueEventListener(new ValueEventListener()
                    {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot)
                        {
                            listComplect.clear();
                            for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                                if (firebaseUser.getEmail().equals(postSnapshot.child("email").getValue().toString())) {
                                    String name = postSnapshot.child("firstname").getValue().toString();
                                    String email = postSnapshot.child("email").getValue().toString();
                                    nameUser.setText(getString(R.string.Hello) + name);
                                    Query complects = bd.child("Complects");
                                    complects.addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                                                if (email.equals(postSnapshot.child("email").getValue().toString())) {
                                                    Complect complect = new Complect();
                                                    complect.setFootwear(postSnapshot.child("Footwear").getValue().toString());
                                                    complect.setHeadgear(postSnapshot.child("Headgear").getValue().toString());
                                                    complect.setOuterwear(postSnapshot.child("Outerwear").getValue().toString());
                                                    complect.setPants(postSnapshot.child("Pants").getValue().toString());
                                                    complect.setShirt(postSnapshot.child("Shirt").getValue().toString());
                                                    complect.setEmail(email);
                                                    complect.setTemp1(Integer.parseInt(postSnapshot.child("temp1").getValue().toString()));
                                                    complect.setTemp2(Integer.parseInt(postSnapshot.child("temp2").getValue().toString()));
                                                    listComplect.add(complect);
                                                }
                                            }
                                            adapter = new ComplectAdapter(listComplect, getActivity(),listener);
                                            rv.setAdapter(adapter);
                                        }
                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                                }
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
                else
                {

                }
            }
        });
    }
}