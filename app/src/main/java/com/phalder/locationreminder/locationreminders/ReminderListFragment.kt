package com.phalder.locationreminder.locationreminders

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.databinding.DataBindingUtil
import com.phalder.locationreminder.R
import com.phalder.locationreminder.base.BaseFragment
import com.phalder.locationreminder.base.NavigationCommand
import com.phalder.locationreminder.databinding.FragmentReminderListBinding
import com.phalder.locationreminder.utils.setDisplayHomeAsUpEnabled
import com.phalder.locationreminder.utils.setTitle
import com.phalder.locationreminder.utils.setup
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * A simple [Fragment] subclass.
 * Use the [ReminderListFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ReminderListFragment : BaseFragment() {

    //use Koin to retrieve the ViewModel instance
    override val _viewModel: RemindersListViewModel by viewModel()
    private lateinit var binding: FragmentReminderListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding =  DataBindingUtil.inflate(inflater,R.layout.fragment_reminder_list, container, false)
        binding.viewModel = _viewModel

        binding.refreshLayout.setOnRefreshListener { _viewModel.loadReminders() }
        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(false)

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        //load the reminders list on the ui
        _viewModel.loadReminders()
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        setupRecyclerView()
        binding.addReminderFAB.setOnClickListener {
            navigateToAddReminder()
        }
    }
    private fun setupRecyclerView() {
        val adapter = RemindersListAdapter {
        }
        //setup the recycler view using the extension function
        binding.reminderssRecyclerView.setup(adapter)
    }
    private fun navigateToAddReminder() {
        //use the navigationCommand live data to navigate between the fragments
        _viewModel.navigationCommand.postValue(
            NavigationCommand.To(ReminderListFragmentDirections.actionReminderListFragmentToSaveReminderFragment())
        )
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.options_menu, menu)
    }

}