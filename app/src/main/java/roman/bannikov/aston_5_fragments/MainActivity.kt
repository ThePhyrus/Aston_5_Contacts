package roman.bannikov.aston_5_fragments

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    private lateinit var pLauncher: ActivityResultLauncher<Array<String>>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.act)
        pLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
                if (it[Manifest.permission.READ_CONTACTS] == true) {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.list, ContactListFragment.newInstance())
                        .commit()
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
            supportFragmentManager.beginTransaction()
                .replace(R.id.list, ContactListFragment.newInstance())
                .commit()
        }

    }

    override fun onBackPressed() {
        val fManager = supportFragmentManager
        if (fManager.backStackEntryCount > 0)
            supportFragmentManager.popBackStack()
        else
            super.onBackPressed()
    }
}