package com.example.softwareengineeringapp.ui.level1;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.softwareengineeringapp.MainActivity;

public class Level1ViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public Level1ViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is level 1 fragment");

    }

    public LiveData<String> getText() {
        return mText;
    }
}