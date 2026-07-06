package com.app.todolist

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.app.todolist.databinding.ActivityMainBinding
import com.app.todolist.ui.home.HomeFragment
import com.app.todolist.ui.profile.ProfileFragment
import com.app.todolist.ui.task.TaskListFragment
import com.app.todolist.ui.task.form.AddTaskActivity
import com.app.todolist.util.NotificationHelper

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    companion object {
        /** Dikirim dari Activity lain (AddTaskActivity, NotificationListActivity, dst)
         *  supaya MainActivity tahu tab mana yang harus ditampilkan saat dibuka. */
        const val EXTRA_SELECTED_TAB = "extra_selected_tab"
    }

    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* diamkan hasilnya baik granted/denied — reminder cuma skip kalau ditolak */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        NotificationHelper.ensureChannel(this)
        requestNotificationPermissionIfNeeded()

        if (savedInstanceState == null) {
            val requestedTab = intent.getIntExtra(EXTRA_SELECTED_TAB, R.id.nav_home)
            showTab(requestedTab)
            binding.bottomNav.selectedItemId = requestedTab
        }

        // Tangani klik pada menu navigasi bawah
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> { showTab(R.id.nav_home); true }
                R.id.nav_tasks -> { showTab(R.id.nav_tasks); true }
                R.id.nav_add -> {
                    // Tambah masih berupa Activity, jadi kita panggil pakai Intent
                    startActivity(Intent(this, AddTaskActivity::class.java))
                    // Kembalikan 'false' agar sorotan menu tetap di halaman sebelumnya
                    false
                }
                R.id.nav_profile -> { showTab(R.id.nav_profile); true }
                else -> false
            }
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    /**
     * Dipanggil ketika MainActivity yang SUDAH ADA di back stack menerima Intent baru
     * (misal dari AddTaskActivity yang startActivity ke sini dengan FLAG_ACTIVITY_CLEAR_TOP).
     * Tanpa ini, tab tujuan dari EXTRA_SELECTED_TAB tidak akan diproses ulang.
     */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val requestedTab = intent.getIntExtra(EXTRA_SELECTED_TAB, R.id.nav_home)
        showTab(requestedTab)
        binding.bottomNav.selectedItemId = requestedTab
    }

    private fun showTab(tabId: Int) {
        val fragment = when (tabId) {
            R.id.nav_tasks -> TaskListFragment()
            R.id.nav_profile -> ProfileFragment()
            else -> HomeFragment()
        }
        replaceFragment(fragment)
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}