package com.app.todolist

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.app.todolist.databinding.ActivityMainBinding
import com.app.todolist.ui.home.HomeFragment
import com.app.todolist.ui.profile.ProfileFragment
import com.app.todolist.ui.task.TaskListFragment
import com.app.todolist.ui.task.form.AddTaskActivity

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Tampilkan HomeFragment saat aplikasi pertama kali dibuka
        if (savedInstanceState == null) {
            replaceFragment(HomeFragment())
        }

        // Tangani klik pada menu navigasi bawah
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    replaceFragment(HomeFragment())
                    true
                }
                R.id.nav_tasks -> {
                    replaceFragment(TaskListFragment())
                    true
                }
                R.id.nav_add -> {
                    // Tambah masih berupa Activity, jadi kita panggil pakai Intent
                    startActivity(Intent(this, AddTaskActivity::class.java))
                    // Kembalikan 'false' agar sorotan menu tetap di halaman sebelumnya
                    false
                }
                R.id.nav_profile -> {
                    replaceFragment(ProfileFragment())
                    true
                }
                else -> false
            }
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}