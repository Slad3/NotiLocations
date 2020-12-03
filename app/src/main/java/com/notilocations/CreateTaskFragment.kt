package com.notilocations

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import com.notilocations.databinding.FragmentCreateTaskBinding

class CreateTaskFragment : Fragment() {
    private val args: CreateTaskFragmentArgs by navArgs()
    private lateinit var binding: FragmentCreateTaskBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate<FragmentCreateTaskBinding>(
            inflater,
            R.layout.fragment_create_task,
            container,
            false
        )

        val notiLocationTask =
            CreateTaskFragmentArgs.fromBundle(requireArguments()).notiLocationTask

        if (notiLocationTask != null) {
            if (notiLocationTask.hasLocation()) {
                binding.addLocation.text = getString(R.string.updateLocation)
            }

            if (notiLocationTask.hasTask()) {
                binding.titleInput.setText(notiLocationTask.task?.title)
                binding.descriptionInput.setText(notiLocationTask.task?.description)
                binding.titleInput.hint = notiLocationTask.task?.title
                binding.descriptionInput.hint = notiLocationTask.task?.description

                if (notiLocationTask.maxSpeed != null) {
                    binding.maxSpeedInput.setText(notiLocationTask.maxSpeed?.toString())
                    binding.maxSpeedInput.hint = notiLocationTask.maxSpeed?.toString()
                }

                if (notiLocationTask.distance != null) {
                    binding.distanceInput.setText(notiLocationTask.distance?.toString())
                    binding.distanceInput.hint = notiLocationTask.distance?.toString()
                }

                binding.triggerOnExitInput.isChecked = notiLocationTask.triggerOnExit
            }

            if (notiLocationTask.hasLocationTaskId()) {
                binding.deleteButton.setOnClickListener { v: View ->
                    //delete the task
                    val viewModel: NotiLocationsViewModel by viewModels()
                    if (notiLocationTask.getDatabaseLocationTask() != null) {
                        viewModel.deleteLocationTask(notiLocationTask.getDatabaseLocationTask()!!)
                    }

                    binding.titleInput.onEditorAction(EditorInfo.IME_ACTION_DONE)
                    binding.descriptionInput.onEditorAction(EditorInfo.IME_ACTION_DONE)

                    v.findNavController().navigate(R.id.action_createTaskFragment_to_swipeView)
                }
            } else {
                binding.deleteButton.visibility = View.INVISIBLE
            }
        } else {
            binding.deleteButton.visibility = View.INVISIBLE
        }

        binding.addLocation.setOnClickListener { v: View ->
            navigateListener(v, notiLocationTask, true)
        }

        binding.submitTask.setOnClickListener { v: View ->
            navigateListener(v, notiLocationTask, false)
        }

        return binding.root
    }

    private fun navigateListener(
        v: View,
        notiLocationTask: NotiLocationTask?,
        defaultToMap: Boolean
    ) {
        if (TextUtils.isEmpty(binding.titleInput.text.toString())) {
            Toast.makeText(context, "Please enter a title for your task", Toast.LENGTH_SHORT).show()
        } else {
            val viewModel: NotiLocationsViewModel by viewModels()
            binding.titleInput.onEditorAction(EditorInfo.IME_ACTION_DONE)
            binding.descriptionInput.onEditorAction(EditorInfo.IME_ACTION_DONE)

            if (notiLocationTask != null) {

                if (notiLocationTask.hasTask()) {
                    notiLocationTask.task?.title = binding.titleInput.text.toString()
                    notiLocationTask.task?.description = binding.descriptionInput.text.toString()
                } else {
                    notiLocationTask.task = NotiTask(
                        null,
                        binding.titleInput.text.toString(),
                        binding.descriptionInput.text.toString()
                    )
                }

                notiLocationTask.maxSpeed = binding.maxSpeedInput.text.toString().toIntOrNull()
                notiLocationTask.distance = binding.distanceInput.text.toString().toFloatOrNull()
                notiLocationTask.triggerOnExit = binding.triggerOnExitInput.isChecked

                if (notiLocationTask.hasLocation() && !defaultToMap) {
                    viewModel.syncNotiLocationTask(notiLocationTask)
                    v.findNavController().navigate(R.id.action_createTaskFragment_to_swipeView)
                } else {

                    val action =
                        CreateTaskFragmentDirections.actionCreateTaskFragmentToMapsFragment(
                            notiLocationTask
                        )
                    v.findNavController().navigate(action)
                }
            } else {
                val newTask = NotiTask(
                    null,
                    binding.titleInput.text.toString(),
                    binding.descriptionInput.text.toString()

                )
                val newNotiLocationTask = NotiLocationTask(
                    maxSpeed = binding.maxSpeedInput.text.toString().toIntOrNull(),
                    distance = binding.distanceInput.text.toString().toFloatOrNull(),
                    triggerOnExit = binding.triggerOnExitInput.isChecked,
                    task = newTask
                )

                val action = CreateTaskFragmentDirections.actionCreateTaskFragmentToMapsFragment(
                    newNotiLocationTask
                )
                v.findNavController().navigate(action)
            }
        }
    }
}