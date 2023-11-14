package com.bignerdranch.android.todolist

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatDelegate

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.fragment_task_main, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        updateThemeIcon(menu?.findItem(R.id.action_darkmode))
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_darkmode -> {
                toggleTheme()
                true
            }
            // Handle other menu items if needed
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun toggleTheme() {
        when (resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) {
            android.content.res.Configuration.UI_MODE_NIGHT_NO -> {
                // Switch to dark mode
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
            android.content.res.Configuration.UI_MODE_NIGHT_YES -> {
                // Switch to light mode
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }
    }

    private fun updateThemeIcon(item: MenuItem?) {
        when (resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) {
            android.content.res.Configuration.UI_MODE_NIGHT_NO -> {
                // Set dark mode icon
                item?.setIcon(R.drawable.ic_menu_darkmode)
            }
            android.content.res.Configuration.UI_MODE_NIGHT_YES -> {
                // Set light mode icon
                item?.setIcon(R.drawable.ic_menu_lightmode)
            }
        }
    }
}