package com.example.myapplication.view;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentResultListener;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.myapplication.R;
import com.example.myapplication.model.Complect;
import com.squareup.picasso.Picasso;

public class ComplectFragment extends Fragment {
    private Complect complect;
    private ImageView footwearImage,headerImage, outwearImage,pantsImage,shirtImage;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getParentFragmentManager().setFragmentResultListener("requestKey", getActivity(),
                new FragmentResultListener() {
                    @Override
                    public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                        complect=(Complect)result.getSerializable(LoggedInFragment.COMPLECT_EXTRA);
                    }
                });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view= inflater.inflate(R.layout.fragment_complect, container, false);
        if(complect!=null) {
            footwearImage = view.findViewById(R.id.footwear_image);
            headerImage = view.findViewById(R.id.headgear_image);
            outwearImage = view.findViewById(R.id.outerwear_image);
            pantsImage = view.findViewById(R.id.pants_image);
            shirtImage = view.findViewById(R.id.shirt_image);
            if (!complect.getFootwear().equals("")) {
                footwearImage.setVisibility(View.VISIBLE);
                Picasso.get().load(complect.getFootwear()).into(footwearImage);
            }
            if (!complect.getHeadgear().equals("")) {
                headerImage.setVisibility(View.VISIBLE);
                Picasso.get().load(complect.getHeadgear()).into(headerImage);
            }
            if (!complect.getOuterwear().equals("")) {
                outwearImage.setVisibility(View.VISIBLE);
                Picasso.get().load(complect.getOuterwear()).into(outwearImage);
            }
            if (!complect.getPants().equals("")) {
                pantsImage.setVisibility(View.VISIBLE);
                Picasso.get().load(complect.getPants()).into(pantsImage);
            }
            if (!complect.getFootwear().equals("")) {
                shirtImage.setVisibility(View.VISIBLE);
                Picasso.get().load(complect.getShirt()).into(shirtImage);
            }
        }
        return  view;
    }
}