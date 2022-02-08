package com.example.fcmexample

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.HasDefaultViewModelProviderFactory
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.fcmexample.databinding.ActivityMainBinding
import com.example.fcmexample.db.FCMExampleDB
import com.example.fcmexample.utils.PREFS_NAME
import com.example.fcmexample.utils.TAG
import com.example.fcmexample.utils.TOKEN
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import timber.log.Timber

class MainActivity : AppCompatActivity(), HasDefaultViewModelProviderFactory {

    private val NAME = MainActivity::class.java.simpleName + " "

    private lateinit var binding: ActivityMainBinding
    private val fcmViewModel: FCMViewModel by viewModels()
    private val clipboardManager by lazy { getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager }
    private val sharedPreferences by lazy { getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }

    override fun getDefaultViewModelProviderFactory(): ViewModelProvider.Factory {
        return object : ViewModelProvider.NewInstanceFactory() {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(FCMViewModel::class.java)) {
                    return FCMViewModel(FCMExampleDB.getDatabase(this@MainActivity)) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }

    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            Timber.tag(TAG).d("$NAME resultLauncher result.resultCode = ${result.resultCode}")
            if (result.resultCode == Activity.RESULT_OK) {
                Timber.tag(TAG)
                    .d("$NAME resultLauncher adapter count = ${recycler.adapter?.itemCount}")

                Handler(Looper.getMainLooper()).postDelayed({
                    recycler.adapter?.itemCount
                        ?.let { binding.recycler.smoothScrollToPosition(it - 1) }
                }, 1000)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.apply {
            lifecycleOwner = this@MainActivity
            viewModel = fcmViewModel
            copyClick = View.OnClickListener {
                clipboardManager.setPrimaryClip(
                    ClipData.newPlainText(
                        "token", sharedPreferences.getString(
                            TOKEN, ""
                        )
                    )
                )
                Snackbar.make(binding.root, "Copied to clipboard", Snackbar.LENGTH_SHORT).show()
            }
            recycler.adapter = NotificationListAdapter(ItemClickListener {

            })

            viewModel?.goToSendActivity?.observe(this@MainActivity, Observer {
                it.getIfNotHandled()?.run { openSendActivity() }
            })
        }

    }

    private fun openSendActivity() {
        Timber.tag(TAG).d("$NAME openSendActivity")
        Intent(this, SendActivity::class.java).run {
            resultLauncher.launch(this)
        }
    }


}