package roman.bannikov.aston_5_fragments

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat


class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        requestPermissions() // and launch fragment
    }


    private fun requestPermissions() {
        val pLauncher: ActivityResultLauncher<Array<String>> =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
                if (it[Manifest.permission.READ_CONTACTS] == true) {
                    launchMainFragment()
                }
            }
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.READ_CONTACTS
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            pLauncher.launch(
                arrayOf(
                    Manifest.permission.READ_CONTACTS,
                    Manifest.permission.WRITE_CONTACTS
                )
            )
        } else {
            launchMainFragment()
        }
    }


    private fun launchMainFragment() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.list, ContactListFragment.newInstance())
            .commit()
    }


}