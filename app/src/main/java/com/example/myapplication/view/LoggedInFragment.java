package com.example.myapplication.view;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.Api.NetworkService;
import com.example.myapplication.R;
import com.example.myapplication.adapters.ComplectAdapter;
import com.example.myapplication.model.Complect;
import com.example.myapplication.model.Main;
import com.example.myapplication.model.Weather;
import com.example.myapplication.model.WeatherAll;
import com.example.myapplication.viewmodel.LoggedInViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoggedInFragment extends Fragment {
    private LoggedInViewModel loggedInViewModel;
    private TextView city;
    private TextView tempreture;
    private TextView nameUser;
    private RecyclerView rv;
    private DatabaseReference bd;
    private List<Complect> listComplect;
    private ComplectAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        listComplect=new ArrayList<>();
        bd= FirebaseDatabase.getInstance().getReference();
        loggedInViewModel = new ViewModelProvider(this).get(LoggedInViewModel.class);
        loggedInViewModel.getUserLiveData().observe(this, new Observer<FirebaseUser>() {
            @Override
            public void onChanged(FirebaseUser firebaseUser) {
                if (firebaseUser != null) {
                    Query users= bd.child("Users");
                    users.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot postSnapshot: snapshot.getChildren()) {
                                if(firebaseUser.getEmail().equals(postSnapshot.child("email").getValue().toString()))
                                {
                                    String name=postSnapshot.child("firstname").getValue().toString();
                                    String email=postSnapshot.child("email").getValue().toString();
                                    nameUser.setText(getString(R.string.Hello)+name);
                                    Query complects= bd.child("Complects");
                                    complects.addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            for (DataSnapshot postSnapshot: snapshot.getChildren())
                                            {
                                                if(email.equals(postSnapshot.child("email").getValue().toString()))
                                                {
                                                    Complect complect=new Complect();
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
                                            adapter=new ComplectAdapter(listComplect,getActivity());
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
                    NetworkService.getInstance().getJSONApi().getWeatherByCity("Kaliningrad",NetworkService.KEY,"metric","ru").
                            enqueue(new Callback<WeatherAll>() {
                                @Override
                                public void onResponse(Call<WeatherAll> call, Response<WeatherAll> response) {
                                    WeatherAll weatherAll=response.body();
                                    Main main=weatherAll.getMain();
                                    tempreture.setText(Integer.toString((int) main.getTemp()));
                                    city.setText(weatherAll.getName());
                                }

                                @Override
                                public void onFailure(Call<WeatherAll> call, Throwable t) {
                                    Log.d("error",t.getMessage());
                                }
                            });
                } else {

                }
            }
        });

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

//        logOutButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                loggedInViewModel.logOut();
//            }
//        });

        return view;
    }
}