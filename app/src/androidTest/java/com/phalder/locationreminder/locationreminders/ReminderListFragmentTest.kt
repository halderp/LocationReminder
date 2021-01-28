package com.phalder.locationreminder.locationreminders

import android.os.Bundle
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.phalder.locationreminder.R
import com.phalder.locationreminder.data.LocalDB
import com.phalder.locationreminder.data.ReminderDTO
import com.phalder.locationreminder.data.ReminderDataSource
import com.phalder.locationreminder.data.RemindersLocalRepository
import com.phalder.locationreminder.util.DataBindingIdlingResource
import com.phalder.locationreminder.util.EspressoIdlingResource
import com.phalder.locationreminder.util.getString
import com.phalder.locationreminder.util.monitorFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.androidx.viewmodel.dsl.viewModel
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import java.lang.reflect.Array.get


@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
@MediumTest
class ReminderListFragmentTest{

    private lateinit var repository: ReminderDataSource
    private val dataBindingIdlingResource = DataBindingIdlingResource()

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun registerIdlingResources(): Unit = IdlingRegistry.getInstance().run {
        register(EspressoIdlingResource.countingIdlingResource)
        register(dataBindingIdlingResource)
    }

    @After
    fun unregisterIdlingResource(): Unit = IdlingRegistry.getInstance().run {
        unregister(EspressoIdlingResource.countingIdlingResource)
        unregister(dataBindingIdlingResource)
    }

    @Before
    fun setup() {
        stopKoin()

        val appModule = module {
            viewModel {
                RemindersListViewModel(
                    ApplicationProvider.getApplicationContext(),
                    get() as ReminderDataSource
                )
            }

            single<ReminderDataSource> { RemindersLocalRepository(get()) }
            single { LocalDB.createRemindersDao(ApplicationProvider.getApplicationContext()) }
        }

        startKoin {
            androidContext(ApplicationProvider.getApplicationContext())
            modules(listOf(appModule))
        }

        repository = GlobalContext.get().koin.get()

        runBlocking {
            repository.deleteAllReminders()
        }
    }

    @Test
    fun withoutAnyReminders_displayNoDataString() {
        val scenario =
            launchFragmentInContainer<ReminderListFragment>(Bundle.EMPTY, R.style.Theme_LocationReminder)
        val navController = mock(NavController::class.java)

        dataBindingIdlingResource.monitorFragment(scenario)
        scenario.onFragment { Navigation.setViewNavController(it.view!!, navController) }

        onView(withText(getString(R.string.no_data))).check(matches(isDisplayed()))
    }

    @Test
    fun fabButton_onClicked_navigatesToSaveReminderFragment() {
        val scenario =
            launchFragmentInContainer<ReminderListFragment>(Bundle.EMPTY, R.style.Theme_LocationReminder)
        val navController = mock(NavController::class.java)
        dataBindingIdlingResource.monitorFragment(scenario)

        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        onView(withId(R.id.addReminderFAB)).perform(click())
        verify(navController).navigate(ReminderListFragmentDirections.actionReminderListFragmentToSaveReminderFragment())
    }

    @Test
    fun validReminder_displaysOnScreen() {
        val validDataItem = ReminderDTO(
            title = "Eat my favorite donut",
            description = "Time to eat favorite donut",
            location = "My Donut",
            latitude = 2.344234234,
            longitude = 1.34234234
        )

        runBlocking {
            repository.saveReminder(validDataItem)
        }

        val scenario =
            launchFragmentInContainer<ReminderListFragment>(Bundle.EMPTY, R.style.Theme_LocationReminder)
        val navController = mock(NavController::class.java)
        dataBindingIdlingResource.monitorFragment(scenario)

        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        onView(withText(validDataItem.title)).check(matches(isDisplayed()))
        onView(withText(validDataItem.description)).check(matches(isDisplayed()))
        onView(withText(validDataItem.location)).check(matches(isDisplayed()))
    }
}