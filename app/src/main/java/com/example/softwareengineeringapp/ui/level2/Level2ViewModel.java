package com.example.softwareengineeringapp.ui.level2;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class Level2ViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public Level2ViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is level 2 fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}