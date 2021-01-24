package com.phalder.locationreminder.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.phalder.locationreminder.data.ReminderDTO
import com.phalder.locationreminder.data.RemindersDatabase
import com.phalder.locationreminder.data.RemindersLocalRepository
import com.phalder.locationreminder.data.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@MediumTest
class RemindersLocalRepositoryTest {

    private lateinit var reminderDB: RemindersDatabase
    private lateinit var reminderLocalRepo: RemindersLocalRepository


    private val reminderDTO = ReminderDTO(
        title = "LocalTest",
        description = "Local testing",
        location = "local test",
        latitude = 2.343434343,
        longitude = 1.231424,
    )
    private val anotherReminderDTO = ReminderDTO(
        title = "LocalTest1",
        description = "Local testing1",
        location = "local test1",
        latitude = 3.343434343,
        longitude = 2.231424,
    )

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun initDatabase() {
        reminderDB = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).allowMainThreadQueries().build()

        reminderLocalRepo = RemindersLocalRepository(reminderDB.reminderDao(), Dispatchers.Main)
    }

    @After
    fun closeDatabase() {
        reminderDB.close()
    }

    // runBlocking is used here because of https://github.com/Kotlin/kotlinx.coroutines/issues/1204
    // TODO: Replace with runBlockingTest once issue is resolved
    @Test
    fun saveReminder_validReminder_retrieveSucceeds() = runBlocking {
        reminderLocalRepo.saveReminder(reminderDTO)

        val result = reminderDTO.id?.let { reminderLocalRepo.getReminder(it) }

        result as Result.Success

        assertThat(result.data.id, `is`(reminderDTO.id))
        assertThat(result.data.title, `is`(reminderDTO.title))
        assertThat(result.data.description, `is`(reminderDTO.description))
        assertThat(result.data.latitude, `is`(reminderDTO.latitude))
        assertThat(result.data.longitude, `is`(reminderDTO.longitude))

    }

    @Test
    fun deleteAllReminders_validReminder_returnEmptyList() = runBlocking{
        //GIven
        reminderLocalRepo.saveReminder(reminderDTO)

        // When
        reminderLocalRepo.deleteAllReminders()
        val result = reminderLocalRepo.getReminders()

        //Then
        result as Result.Success
        assertThat(result.data.isEmpty(), `is`(true))
    }

    @Test
    fun shouldReturnError_saveReminder_retrievesReminder() = runBlocking {
        // GIVEN - a new reminder saved in the database
        reminderLocalRepo.saveReminder(reminderDTO)

        // WHEN
        val result = reminderLocalRepo.getReminder("1234")
        result as Result.Error

        // THEN
        assertThat(result.message, `is`("Reminder not found!"))


    }

    @Test
    fun getReminders_validReminders_retrieveSucceeds() = runBlocking {
        // GIVEN - 2 reminders saved in the database
        reminderLocalRepo.saveReminder(reminderDTO)
        reminderLocalRepo.saveReminder(anotherReminderDTO)

        // WHEN
        val result = reminderLocalRepo.getReminders()
        result as Result.Success

        // THEN -
        assertThat(result.data.size, `is`(2))
        assertThat(result.data[0].id, `is`(reminderDTO.id))
        assertThat(result.data[1].id, `is`(anotherReminderDTO.id))

    }
}

