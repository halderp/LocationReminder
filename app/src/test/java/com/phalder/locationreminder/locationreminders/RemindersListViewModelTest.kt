package com.phalder.locationreminder.locationreminders

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.phalder.locationreminder.data.ReminderDTO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is.`is`
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(manifest=Config.NONE)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {

    private lateinit var fakeDataSource: FakeDataSource
    private lateinit var remindersListViewModel: RemindersListViewModel
    private lateinit var validDataItem: ReminderDTO

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setup() {
        // create the fakedata source and view model to test
        fakeDataSource = FakeDataSource()
        remindersListViewModel = RemindersListViewModel(
            ApplicationProvider.getApplicationContext(),
            fakeDataSource
        )

        validDataItem = ReminderDTO(
            title = "Eat my favorite donut",
            description = "Time to eat favorite donut",
            location = "My Donut",
            latitude = 2.344234234,
            longitude = 1.34234234
        )
    }
    @Test
    fun showLoading_loadReminders_IsshowLoadingTrue() = runBlockingTest{
        mainCoroutineRule.pauseDispatcher()
        remindersListViewModel.loadReminders()
        assertThat(remindersListViewModel.showLoading.getOrAwaitValue(),`is`(true) )

    }
    @Test
    fun showLoading_loadReminders_IsshowLoadingFalse() = runBlockingTest{
        remindersListViewModel.loadReminders()
        assertThat(remindersListViewModel.showLoading.getOrAwaitValue(),`is`(false) )
    }

    @Test
    fun loadReminders_noReminders_showsNoDataIsTrue(){
        // Given
        // This is part of setup
        // When
        remindersListViewModel.loadReminders()
        // Then
        val value = remindersListViewModel.showNoData.getOrAwaitValue()
        assertThat(value,`is`(true))
    }
    @Test
    fun loadReminders_validReminders_showsNoDataIsFalse()= runBlockingTest{
        // Given
        fakeDataSource.saveReminder(validDataItem)
        // When
        remindersListViewModel.loadReminders()
        // Then
        val value = remindersListViewModel.showNoData.getOrAwaitValue()
        assertThat(value,`is`(false))
    }
    @Test
    fun loadReminders_noReminders_reminderListIsEmpty() = runBlockingTest {
        // Given
        // This is part of setup
        // When
        remindersListViewModel.loadReminders()
        // Then
        val value = remindersListViewModel.remindersList.getOrAwaitValue()
        assertThat(value.isEmpty(),`is`(true))
    }

    @Test
    fun loadReminders_withReminders_reminderListIsNonEmpty() = runBlockingTest {
        // Given
        fakeDataSource.saveReminder(validDataItem)

        // When
        remindersListViewModel.loadReminders()
        // Then
        val value = remindersListViewModel.remindersList.getOrAwaitValue()
        assertThat(value.isEmpty(),`is`(false))
    }

    @Test
    fun showSnackBar_remindersUnavailable_showsError() = runBlockingTest {
        // Given
        fakeDataSource.setShouldReturnError(true)

        //When
        remindersListViewModel.loadReminders()

        //Then
        assertThat(remindersListViewModel.showSnackBar.getOrAwaitValue().isNotEmpty(),`is`(true))
    }

}
