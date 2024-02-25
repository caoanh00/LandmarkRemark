package com.example.landmarkremark.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.landmarkremark.models.LocationData
import com.example.landmarkremark.repository.MainRepository

class MainViewModel : ViewModel() {
    private val _repository = MainRepository

    fun init() {
        _repository.init()
    }

    fun signOut() {
        _repository.signOut()
    }

    fun setLocations() {
        val locationData = getLocations().value?.toMutableList()
        _repository.setLocations(locationData)
    }

    fun getLocations(): LiveData<List<LocationData>> {
        return _repository.getLocations()
    }

    fun getMyLocations(): MutableList<LocationData>? {
        return _repository.getMyLocations()
    }
}