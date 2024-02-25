package com.example.landmarkremark.ui.collections

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.landmarkremark.databinding.FragmentCollectionsBinding
import com.example.landmarkremark.interfaces.IRecyclerViewListener
import com.example.landmarkremark.models.LocationData
import com.example.landmarkremark.ui.main.MainActivity
import com.example.landmarkremark.ui.main.MainViewModel
import timber.log.Timber

class CollectionsFragment : Fragment(), IRecyclerViewListener {

    private lateinit var binding: FragmentCollectionsBinding

    private lateinit var mainViewModel: MainViewModel
    private val adapter: CollectionsAdapter by lazy {
        CollectionsAdapter(this)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val mainActivity = activity as MainActivity
        mainViewModel = ViewModelProvider(mainActivity).get(MainViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCollectionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mainViewModel.getLocations().observe(viewLifecycleOwner, Observer {
            adapter.setList(mainViewModel.getMyLocations())
            adapter.notifyDataSetChanged()
            binding.collectionEmpty.visibility = if(mainViewModel.getMyLocations()?.isNullOrEmpty() == true) VISIBLE else GONE
        })
        binding.collectionRecyclerView.adapter = adapter
        binding.collectionRecyclerView.layoutManager = LinearLayoutManager(view.context)
    }

    override fun onRecyclerViewItemClickListener(arg1: Any?, arg2: Any?, arg3: Any?) {
        Timber.d("checkSearch item selected: ${arg1 as LocationData}")
        (activity as? MainActivity)?.goToExploreFragment(arg1)
    }
}