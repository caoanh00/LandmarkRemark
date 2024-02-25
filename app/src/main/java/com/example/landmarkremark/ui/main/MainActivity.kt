package com.example.landmarkremark.ui.main

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.landmarkremark.R
import com.example.landmarkremark.databinding.ActivityMainBinding
import com.example.landmarkremark.models.LocationData
import com.example.landmarkremark.ui.collections.CollectionsFragment
import com.example.landmarkremark.ui.map.MapFragment
import com.example.landmarkremark.ui.profile.ProfileFragment
import com.google.android.material.navigation.NavigationBarView

class MainActivity : AppCompatActivity(), NavigationBarView.OnItemSelectedListener {

    companion object {
        const val ADD_LOCATION_NOTE_REQUEST_CODE = 100
    }

    private val mapFragment: MapFragment by lazy {
        MapFragment()
    }
    private val collectionsFragment: CollectionsFragment by lazy {
        CollectionsFragment()
    }
    private val profileFragment: ProfileFragment by lazy {
        ProfileFragment()
    }

    private lateinit var mainViewModel: MainViewModel
    private lateinit var activeFragment: Fragment

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setActionBar()
        setViewModels()
        setNavController()
    }

    private fun setViewModels() {
        mainViewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        mainViewModel.init()
        mainViewModel.setLocations()
    }

    private fun setActionBar() {
        setSupportActionBar(binding.mainToolbar.toolbar)
    }

    /**
     * Set Navigation Controllers
     */
    private fun setNavController() {
        // Set selected item to be map fragment
        binding.mainBottomNavView.selectedItemId = R.id.navigation_bottom_map

        // Set bottom navigation item onSelectedListener
        binding.mainBottomNavView.setOnItemSelectedListener(this)

        // Set active fragment to exploreFragment
        activeFragment = mapFragment
        supportFragmentManager.beginTransaction().add(R.id.main_fragment_container, activeFragment)
            .commit()

        setActionBarButtons(R.id.navigation_bottom_map)
    }

    /**
     * Set action bar buttons onClick and drawable
     * Set drawer's menu to select the current bottom navigation tab
     */
    private fun setActionBarButtons(itemId: Int) {
        when (itemId) {
            R.id.navigation_bottom_map -> {
                binding.mainToolbar.toolbar.title = getString(R.string.title_menu_explore)
            }

            R.id.navigation_bottom_collections -> {
                binding.mainToolbar.toolbar.title = getString(R.string.title_menu_collections)
            }

            R.id.navigation_bottom_profile -> {
                binding.mainToolbar.toolbar.title = getString(R.string.title_menu_profile)
            }
        }
    }

    /**
     * Set all navigation item selected
     * Drawer's navigation item will just call their bottom navigation's counterpart
     * Bottom navigation items will replace the current active fragment
     */
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.navigation_bottom_map -> {
                replaceActiveFragment(mapFragment, item.itemId)
            }

            R.id.navigation_bottom_collections -> {
                replaceActiveFragment(collectionsFragment, item.itemId)
            }

            R.id.navigation_bottom_profile -> {
                replaceActiveFragment(profileFragment, item.itemId)
            }
        }
        return true
    }

    /**
     * Replace active fragment with the fragment selected
     */
    private fun replaceActiveFragment(fragment: Fragment, itemId: Int) {
        if (activeFragment != fragment) {
            if (supportFragmentManager.fragments.contains(fragment)) {
                // Show fragment if supportFragmentManager contains it
                supportFragmentManager.beginTransaction().show(fragment).hide(activeFragment)
                    .commit()
            } else {
                // Add fragment if supportFragmentManager doesn't contain it
                supportFragmentManager.beginTransaction()
                    .add(R.id.main_fragment_container, fragment).hide(activeFragment).commit()
            }
            activeFragment = fragment
        }
        setActionBarButtons(itemId)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            ADD_LOCATION_NOTE_REQUEST_CODE -> {
                if (resultCode == Activity.RESULT_OK) {
                    mainViewModel.getLocations()
                    val locationData = intent.getParcelableExtra<LocationData>("locationData")
                    goToExploreFragment(locationData)
                }
            }
        }
    }

    fun goToExploreFragment(locationData: LocationData? = null) {
        if (activeFragment != mapFragment) {
            mapFragment.showLocationInfo(locationData)
            binding.mainBottomNavView.findViewById<View>(R.id.navigation_bottom_map).callOnClick()
        }
    }
}
