package com.phalder.locationreminder.locationreminders

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.phalder.locationreminder.R
import com.phalder.locationreminder.base.NavigationCommand
import com.phalder.locationreminder.data.ReminderDTO
import com.phalder.locationreminder.data.Result
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers.equalToIgnoringCase
import org.hamcrest.core.Is
import org.junit.Before
import org.junit.Test

import org.junit.Assert.*
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@Config(manifest= Config.NONE)
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {

    private lateinit var fakeDataSource: FakeDataSource
    private lateinit var saveReminderviewModel: SaveReminderViewModel

    @get:Rule
    val instantTaskExecRule = InstantTaskExecutorRule()

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setUp() {
        fakeDataSource = FakeDataSource()
        saveReminderviewModel = SaveReminderViewModel(
            ApplicationProvider.getApplicationContext(),
            fakeDataSource
        )
    }

    @Test
    fun reminderTitle_nullOremptyTile_shouldReturnFalse() {
        // Given
        val dataItemWithNullTitle = ReminderDataItem(
            title = null,
            description = "test",
            location = "test",
            latitude = 2.213123123,
            longitude = 1.023432423,
        )
        val dataItemWithEmptyTitle = ReminderDataItem(
            title = "",
            description = "test",
            location = "test",
            latitude = 2.213123123,
            longitude = 1.023432423,
        )

        // when
        val returnValueNull = saveReminderviewModel.validateEnteredData(dataItemWithNullTitle)
        val returnValueEmpty = saveReminderviewModel.validateEnteredData(dataItemWithEmptyTitle)

        // then
        MatcherAssert.assertThat(returnValueNull, `is`(false))
        MatcherAssert.assertThat(returnValueEmpty, `is`(false))

    }

    @Test
    fun reminderLocation_nullOremptyTile_shouldReturnFalse() {
        // Given
        val dataItemWithNullLocation = ReminderDataItem(
            title = "test",
            description = "test",
            location = null,
            latitude = 2.213123123,
            longitude = 1.023432423,
        )
        val dataItemWithEmptyLocation = ReminderDataItem(
            title = "test",
            description = "test",
            location = "",
            latitude = 2.213123123,
            longitude = 1.023432423,
        )

        // when
        val returnValueNull = saveReminderviewModel.validateEnteredData(dataItemWithNullLocation)
        val returnValueEmpty = saveReminderviewModel.validateEnteredData(dataItemWithEmptyLocation)

        // then
        MatcherAssert.assertThat(returnValueNull, `is`(false))
        MatcherAssert.assertThat(returnValueEmpty, `is`(false))

    }
    @Test
    fun snackBarInt_nullOremptyTile_shouldReturnErrorMessage()= runBlockingTest{
        // Given
        val dataItemWithNullTitle = ReminderDataItem(
            title = null,
            description = "test",
            location = "test",
            latitude = 2.213123123,
            longitude = 1.023432423,
        )
        // when
        val returnValueNull = saveReminderviewModel.validateEnteredData(dataItemWithNullTitle)

        // then
        val value = saveReminderviewModel.showSnackBarInt.getOrAwaitValue()
        val expectedValue = R.string.err_enter_title
        assertThat(value, `is`(expectedValue))

    }
    @Test
    fun showToast_reminderSaved_shouldDisplayValidString()= runBlockingTest{
        // Given
        val validDataItem = ReminderDataItem(
            title = "Test",
            description = "test",
            location = "test",
            latitude = 2.213123123,
            longitude = 1.023432423,
        )
        // when
        saveReminderviewModel.saveReminder(validDataItem)

        // then
        val value = saveReminderviewModel.showToast.getOrAwaitValue()
        assertThat(value.isNotEmpty(), `is`(true))

    }


    @Test
    fun saveReminder_validDataItem_reminderSaved() = runBlockingTest {
        // Given
        val validDataItem = ReminderDataItem(
            title = "Test",
            description = "test",
            location = "test",
            latitude = 2.213123123,
            longitude = 1.023432423,
        )
        // when
        saveReminderviewModel.saveReminder(validDataItem)

        // then
        val result = fakeDataSource.getReminder(validDataItem.id)
        var value : ReminderDTO
        if (result is Result.Success<ReminderDTO>) {
            value = result.data
            assertThat(value.id, equalToIgnoringCase(validDataItem.id))
        }

    }

    @Test
    fun navigationCommand_validDataItem_shouldNavigateBack() = runBlockingTest {
        // Given
        val validDataItem = ReminderDataItem(
            title = "Test",
            description = "test",
            location = "test",
            latitude = 2.213123123,
            longitude = 1.023432423,
        )
        // when
        saveReminderviewModel.validateAndSaveReminder(validDataItem)

        // then
        val value = saveReminderviewModel.navigationCommand.getOrAwaitValue()
        assertThat(value.equals(NavigationCommand.Back), `is`(true))

    }

}