package com.example.landmarkremark.repository

import android.app.Activity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.landmarkremark.interfaces.IOnCompleteListener
import com.example.landmarkremark.models.LocationData
import com.example.landmarkremark.utilities.SharedPreferenceUtils
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import timber.log.Timber
import java.util.Date

object MainRepository {
    private var auth = FirebaseAuth.getInstance()
    private var dbRef: DatabaseReference = FirebaseDatabase.getInstance().getReference("locations")
    private val locations = MutableLiveData<List<LocationData>>()

    private var userId: String? = null
    private var email: String? = null

    private var firebaseUser: FirebaseUser? = null
    private var baseLocationId: String? = null

    fun init() {
        baseLocationId = dbRef.push().key
        userId = SharedPreferenceUtils.getUserId()
        email = SharedPreferenceUtils.getEmail()
        firebaseUser = FirebaseAuth.getInstance().currentUser
    }

    fun getFirebaseUser(): FirebaseUser? {
        return auth.currentUser
    }

    fun signInWithEmailAndPassword(email: String, password: String, activity: Activity, listener: IOnCompleteListener) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(activity, OnCompleteListener { task ->
            if(task.isSuccessful) {
                listener.onSuccess()
            }else {
                listener.onError(task.exception)
            }
        })
    }

    fun createUserWithEmailAndPassword(email: String, password: String, activity: Activity, listener: com.example.landmarkremark.interfaces.IOnCompleteListener) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(activity, OnCompleteListener { task ->
            if(task.isSuccessful) {
                listener.onSuccess()
            }else {
                listener.onError(task.exception)
            }
        })
    }

    fun signOut() {
        auth.signOut()
    }

    fun setLocations(locationDataList: MutableList<LocationData>? = locations.value?.toMutableList()) {
        val newLocationList = mutableListOf<LocationData>()
        locationDataList?.let {
            for (locationData: LocationData in it) {
                if (!newLocationList.contains(locationData))
                    newLocationList.add(locationData)
            }
        }
        locations.value = newLocationList
    }

    fun getLocations(): LiveData<List<LocationData>> {
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                Timber.e(error.toException(), "getLocations")
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                val newLocationList = mutableListOf<LocationData>()
                for (locationSnapShot: DataSnapshot in snapshot.children) {
                    val location = locationSnapShot.getValue(LocationData::class.java)
                    location?.let {
                        // get all public notes and all my notes
                        if(it.visibility == "public" || it.creatorId == userId) {
                            newLocationList.add(it)
                        }
                    }
                }
                newLocationList.sortByDescending {
                    it.createdTime
                }
                setLocations(newLocationList)
            }
        })
        return locations
    }

    fun getMyLocations(): MutableList<LocationData>? {
        return locations.value?.filter {
            it.creatorId == userId
        }?.toMutableList()
    }

    fun writeNote(
        title: String,
        description: String,
        lat: Double?,
        lng: Double?,
        extra: String? = "",
        visibility: String? = "public",
        imageUrl: String? = ""
    ): LocationData? {
        userId ?: return null

        val createdTime = Date().time.toString()
        val location = LocationData(
            title,
            description,
            createdTime,
            userId,
            email,
            lat,
            lng,
            extra,
            visibility,
            imageUrl
        )

        baseLocationId?.let {
            dbRef.child(it + createdTime).setValue(location)
        }
        getLocations()
        return location
    }

    fun clear() {
        baseLocationId = null
        userId = null
        email = null
        firebaseUser = null
    }
}