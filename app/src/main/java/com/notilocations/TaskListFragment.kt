package com.notilocations

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.notilocations.database.FullLocationTask
import com.notilocations.databinding.FragmentTaskListBinding

class TaskListFragment : Fragment() {
    private lateinit var binding: FragmentTaskListBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_task_list,
            container,
            false
        )

        //set gear icon to different color based on the theme
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.context)
        if (sharedPreferences?.getBoolean("dark_theme", false) == true) {
            binding.settingsButton.setImageResource(R.drawable.gear_icon_white)
        } else {
            binding.settingsButton.setImageResource(R.drawable.gear_icon)
        }

        val recyclerLayout = LinearLayoutManager(this.context)
        val recyclerAdapter = TaskListAdapter(this)

        //Links the task Recycler and our variables
        binding.taskRecycler.apply {
            setHasFixedSize(true)
            layoutManager = recyclerLayout
            adapter = recyclerAdapter
        }

        val viewModel = ViewModelProvider(this).get(NotiLocationsViewModel::class.java)
        viewModel.getActiveFullLocationTasks()
            .observe(this.viewLifecycleOwner, object : Observer<List<FullLocationTask>> {
                private val adapter = recyclerAdapter
                override fun onChanged(t: List<FullLocationTask>?) {
                    if (t != null) {
                        adapter.setLocationTasks(t)
                        //if the recycler view is empty, display empty message
                        if (adapter.itemCount == 0) {
                            binding.addTaskText.visibility = View.VISIBLE
                            binding.taskRecycler.visibility = View.GONE
                        }
                        //display recycler view as it has items in it
                        else {
                            binding.addTaskText.visibility = View.GONE
                            binding.taskRecycler.visibility = View.VISIBLE
                        }
                    }
                }
            })

        val swipeHandler = SwipeNotiLocationTaskCallback(requireContext(), recyclerAdapter)
        val itemTouchHelper = ItemTouchHelper(swipeHandler)
        itemTouchHelper.attachToRecyclerView(binding.taskRecycler)

        createClickListeners()

        return binding.root
    }

    /**
     * Sets up the click listeners.
     */
    private fun createClickListeners() {
        binding.addTask.setOnClickListener { v: View ->
            val action = SwipeViewFragmentDirections.actionSwipeViewToCreateTaskFragment()
            v.findNavController().navigate(action)
        }

        binding.settingsButton.setOnClickListener { v: View ->
            val action = SwipeViewFragmentDirections.actionSwipeViewToSettingsFragment()
            v.findNavController().navigate(action)
        }
    }
}