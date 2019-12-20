package com.example.softwareengineeringapp.ui.level1;

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

public class Level1Fragment extends Fragment {

    private Level1ViewModel level1ViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        level1ViewModel =
                ViewModelProviders.of(this).get(Level1ViewModel.class);
        View root = inflater.inflate(R.layout.fragment_level1, container, false);
        final TextView textView = root.findViewById(R.id.text_level1);
        level1ViewModel.getText().observe(this, (@Nullable String s) -> textView.setText(s));
        return root;
    }
}