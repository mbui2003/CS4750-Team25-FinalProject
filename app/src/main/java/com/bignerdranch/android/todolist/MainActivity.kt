package com.bignerdranch.android.todolist

import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.fragment.NavHostFragment
import java.util.UUID

class MainActivity : AppCompatActivity() {

    private val EXTRA_TASK_ID = "EXTRA_TASK_ID"
    private val PREFS_NAME = "MyPrefsFile"
    private val PREF_CURRENT_THEME = "current_theme"

    private val taskViewModel: TaskViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Retrieve the saved theme mode and apply it
        val sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val isDarkMode = sharedPreferences.getBoolean(PREF_CURRENT_THEME, false)
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        // Check if the activity is recreated due to a theme change
        if (savedInstanceState == null) {
            // Check if there is a task ID in the intent
            val taskIdString = intent.getStringExtra(EXTRA_TASK_ID)
            taskIdString?.let {
                val taskId = UUID.fromString(it)
                navigateToTaskDetail(taskId)
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // Save the task ID to the bundle
        taskViewModel.taskId.value?.let { outState.putString(EXTRA_TASK_ID, it.toString()) }
    }

    private fun navigateToTaskDetail(taskId: UUID) {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.fragment_container) as NavHostFragment
        val navController = navHostFragment.navController

        // Set task ID in ViewModel
        taskViewModel.setTaskId(taskId)

        val action = TaskListFragmentDirections.showTaskDetail(taskId)
        navController.navigate(action)
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

class TaskViewModel : ViewModel() {
    private val _taskId = MutableLiveData<UUID>()
    val taskId: LiveData<UUID> get() = _taskId

    fun setTaskId(taskId: UUID) {
        _taskId.value = taskId
    }
}
