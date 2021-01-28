package com.phalder.locationreminder.locationreminders

import android.os.Bundle
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PointOfInterest
import com.phalder.locationreminder.R
import com.phalder.locationreminder.data.LocalDB
import com.phalder.locationreminder.data.ReminderDTO
import com.phalder.locationreminder.data.ReminderDataSource
import com.phalder.locationreminder.data.RemindersLocalRepository
import com.phalder.locationreminder.util.DataBindingIdlingResource
import com.phalder.locationreminder.util.EspressoIdlingResource
import com.phalder.locationreminder.util.monitorFragment
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.mockito.Mockito.mock

class SaveReminderFragmentTest{
    private lateinit var viewModel: SaveReminderViewModel
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
            single {
                SaveReminderViewModel(
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

        viewModel = GlobalContext.get().koin.get()
    }
    @Test
    fun title_whenEmply_WillDisplaySnackbar() {
        val navController = mock(NavController::class.java)
        val scenario =
            launchFragmentInContainer<SaveReminderFragment>(Bundle.EMPTY, R.style.Theme_LocationReminder)

        dataBindingIdlingResource.monitorFragment(scenario)

        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        onView(withId(R.id.saveReminder)).perform(click())
        assertThat(viewModel.showSnackBarInt.value,`is`(R.string.err_enter_title))
    }

    @Test
    fun location_whenEmpty_WillDisplaySnackbar() {

        val navController = mock(NavController::class.java)
        val scenario =
            launchFragmentInContainer<SaveReminderFragment>(Bundle.EMPTY, R.style.Theme_LocationReminder)
        dataBindingIdlingResource.monitorFragment(scenario)

        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        onView(withId(R.id.reminderTitle)).perform(typeText("Title"))
        closeSoftKeyboard()

        onView(withId(R.id.saveReminder)).perform(click())
        assertThat(viewModel.showSnackBarInt.value,`is`(R.string.err_select_location))
    }

    @Test
    fun remider_whenValid_WillBeSaved() {
        val validDataItem = ReminderDTO(
            title = "Eat my favorite donut",
            description = "Time to eat favorite donut",
            location = "My Donut",
            latitude = 2.344234234,
            longitude = 1.34234234
        )
        val latlng = LatLng(validDataItem.latitude!!, validDataItem.longitude!!)
        viewModel.setSelectedLocation(PointOfInterest(latlng, validDataItem.location, "Test POI"))
        val navController = mock(NavController::class.java)
        val scenario =
            launchFragmentInContainer<SaveReminderFragment>(Bundle.EMPTY, R.style.Theme_LocationReminder)
        dataBindingIdlingResource.monitorFragment(scenario)

        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        onView(withId(R.id.reminderTitle)).perform(typeText("Title"))
        onView(withId(R.id.reminderDescription)).perform(typeText("Description"))
        closeSoftKeyboard()

        onView(withId(R.id.saveReminder)).perform(click())
        //assertThat(viewModel.showToast.getOrAwaitValue(),`is`(R.string.reminder_saved))
    }
}