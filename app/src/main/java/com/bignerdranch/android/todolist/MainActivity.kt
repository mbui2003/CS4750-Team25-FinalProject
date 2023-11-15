package com.bignerdranch.android.todolist

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatDelegate

class MainActivity : AppCompatActivity() {

    private val PREFS_NAME = "MyPrefsFile"
    private val PREF_CURRENT_THEME = "current_theme"

    override fun onCreate(savedInstanceState: Bundle?) {
        // Retrieve the saved theme mode and apply it
        val sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val isDarkMode = sharedPreferences.getBoolean(PREF_CURRENT_THEME, false)
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

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
        val sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val isDarkMode = sharedPreferences.getBoolean(PREF_CURRENT_THEME, false)

        if (isDarkMode) {
            // Switch to light mode
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            sharedPreferences.edit().putBoolean(PREF_CURRENT_THEME, false).apply()
        } else {
            // Switch to dark mode
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            sharedPreferences.edit().putBoolean(PREF_CURRENT_THEME, true).apply()
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