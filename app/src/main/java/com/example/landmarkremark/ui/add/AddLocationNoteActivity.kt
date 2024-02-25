package com.example.landmarkremark.ui.add

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import com.example.landmarkremark.R
import com.example.landmarkremark.databinding.ActivityAddLoactionNoteBinding
import com.example.landmarkremark.models.LocationData
import com.example.landmarkremark.repository.MainRepository
import com.example.landmarkremark.widgets.LoadingDialog
import com.google.android.gms.maps.model.LatLng


class AddLocationNoteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddLoactionNoteBinding
    private lateinit var loadingDialog: LoadingDialog
    private var latLng: LatLng? = null
    private var locationData: LocationData? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddLoactionNoteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.mainToolbar.toolbar)
        binding.mainToolbar.toolbar.title = getString(R.string.add_location_note_title)
        binding.mainToolbar.toolbar.navigationIcon = AppCompatResources.getDrawable(this, R.drawable.ic_back)
        binding.mainToolbar.toolbar.setNavigationOnClickListener {
            finish()
        }

        loadingDialog = LoadingDialog(this)
        val intent = intent
        latLng = intent.getParcelableExtra<Parcelable>("latLng") as LatLng?

        binding.addLocationNoteNext.setOnClickListener {
            locationData = MainRepository.writeNote(
                binding.addLocationNoteInfoTitle.text.toString(),
                binding.addLocationNoteInfoDescription.text.toString(),
                latLng?.latitude,
                latLng?.longitude,
                extra = "",
                visibility = if (binding.addLocationNoteInfoVisibilityCheckbox.isChecked) "public" else "private"
            )
            MainRepository.getMyLocations()
            returnLocationData()
        }
    }

    private fun returnLocationData(){
        val intent = Intent()
        intent.putExtra("locationData", locationData)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }
}