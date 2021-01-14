package com.phalder.locationreminder.locationreminders

import com.phalder.locationreminder.R
import com.phalder.locationreminder.base.BaseRecyclerViewAdapter

//Use data binding to show the reminder on the item
class RemindersListAdapter(callBack: (selectedReminder: ReminderDataItem) -> Unit) :
    BaseRecyclerViewAdapter<ReminderDataItem>(callBack) {
    override fun getLayoutRes(viewType: Int) = R.layout.item_reminder
}