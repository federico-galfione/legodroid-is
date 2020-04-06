package com.example.softwareengineeringapp.ui.level3;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.softwareengineeringapp.R;

public class Level3Fragment extends Fragment {

    private Level3ViewModel level3ViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        level3ViewModel =
                ViewModelProviders.of(this).get(Level3ViewModel.class);
        View root = inflater.inflate(R.layout.fragment_level3, container, false);
        level3ViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {

            }
        });
        return root;
    }
}