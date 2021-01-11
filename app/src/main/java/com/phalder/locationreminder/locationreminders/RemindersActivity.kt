package com.phalder.locationreminder.locationreminders

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.phalder.locationreminder.R
import com.phalder.locationreminder.authentication.AuthenticationActivity

class RemindersActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reminders)
        setSupportActionBar(findViewById(R.id.toolbar))

        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.options_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.logout -> {
                // add the logout implementation
                AuthUI.getInstance().signOut(this)
                FirebaseAuth.getInstance().signOut()
                // navigate the user back to the Authentication screen
                startActivity(Intent(this, AuthenticationActivity::class.java))
                finish()
            }
        }
        return super.onOptionsItemSelected(item)

    }
}