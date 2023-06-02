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
import com.example.myapplication.model.Main;
import com.example.myapplication.model.Weather;
import com.example.myapplication.model.WeatherAll;
import com.example.myapplication.viewmodel.LoggedInViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoggedInFragment extends Fragment {
    private LoggedInViewModel loggedInViewModel;
    private TextView city;
    private TextView tempreture;
    private RecyclerView rv;
    private DatabaseReference bd;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bd= FirebaseDatabase.getInstance().getReference();
        loggedInViewModel = new ViewModelProvider(this).get(LoggedInViewModel.class);
        loggedInViewModel.getUserLiveData().observe(this, new Observer<FirebaseUser>() {
            @Override
            public void onChanged(FirebaseUser firebaseUser) {
                if (firebaseUser != null) {
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
//        logOutButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                loggedInViewModel.logOut();
//            }
//        });

        return view;
    }
}