package com.veyiskuralay.ataunikampus.fragments;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.veyiskuralay.ataunikampus.R;

import androidx.fragment.app.Fragment;

public class AboutFragment extends Fragment {


    // Hakkında sayfasının gösterilmesi
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_about, container, false);
    }

}
