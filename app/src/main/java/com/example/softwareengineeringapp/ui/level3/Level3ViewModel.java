package com.example.softwareengineeringapp.ui.level3;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class Level3ViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public Level3ViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is level 3 fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}