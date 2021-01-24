package com.phalder.locationreminder.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.phalder.locationreminder.data.ReminderDTO
import com.phalder.locationreminder.data.RemindersDao
import com.phalder.locationreminder.data.RemindersDatabase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@SmallTest
class RemindersDaoTest {
    private lateinit var database: RemindersDatabase
    private lateinit var dao: RemindersDao

    private val reminderDTO = ReminderDTO(
        title = "LocalTest",
        description = "Local testing",
        location = "local test",
        latitude = 2.343434343,
        longitude = 1.231424,
    )

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun initDatabase() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).build()

        dao = database.reminderDao()
    }
    @After
    fun closeDatabase() {
        database.close()
    }


    @Test
    fun saveReminder_validReminder_RetrieveDBSucceeds() = runBlockingTest {
        dao.saveReminder(reminderDTO)

        val reminder = dao.getReminderById(reminderDTO.id)

        assertThat<ReminderDTO>(reminder as ReminderDTO, notNullValue())
        assertThat(reminder.id,`is`(reminderDTO.id))
        assertThat(reminder.title,`is`(reminderDTO.title))
        assertThat(reminder.description,`is`(reminderDTO.description))
        assertThat(reminder.location,`is`(reminderDTO.location))
        assertThat(reminder.latitude,`is`(reminderDTO.latitude))
        assertThat(reminder.longitude,`is`(reminderDTO.longitude))

    }

    @Test
    fun getReminders_insertIntoDB_successfullyInserted() = runBlockingTest {
        dao.saveReminder(reminderDTO)

        val reminders = dao.getReminders()

        assertThat(reminders.size,`is`(1))
        assertThat(reminders.contains(reminderDTO),`is`(true))
    }

    @Test
    fun deleteAllReminders_deleteFromDB_deletedSuccessfully() = runBlockingTest {
        dao.saveReminder(reminderDTO)
        var reminders = dao.getReminders()
        assertThat(reminders.size,`is`(1))

        dao.deleteAllReminders()
        reminders = dao.getReminders()
        assertThat(reminders.isEmpty(),`is`(true))
    }

}