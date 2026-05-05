package com.pos.clothingstore.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.navigation.NavigationView
import com.pos.clothingstore.R
import com.pos.clothingstore.ui.backup.BackupFragment
import com.pos.clothingstore.ui.pos.POSFragment
import com.pos.clothingstore.ui.products.ProductsFragment

/**
 * النشاط الرئيسي - يضم DrawerLayout مع التنقل بين الشاشات الثلاث
 */
class MainActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = getString(R.string.store_name)

        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)

        // فتح القائمة بالضغط على أيقونة الشريط
        toolbar.setNavigationOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        // التنقل بين الشاشات
        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_pos -> loadFragment(POSFragment())
                R.id.nav_products -> loadFragment(ProductsFragment())
                R.id.nav_backup -> loadFragment(BackupFragment())
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        // تحميل شاشة البيع افتراضياً
        if (savedInstanceState == null) {
            loadFragment(POSFragment())
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
            if (currentFragment is POSFragment) {
                super.onBackPressed()
            } else {
                loadFragment(POSFragment())
                navView.setCheckedItem(R.id.nav_pos)
            }
        }
    }
}
