package com.example.fcmexample

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.HasDefaultViewModelProviderFactory
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.fcmexample.databinding.ActivitySendBinding
import com.example.fcmexample.utils.Status
import timber.log.Timber

class SendActivity : AppCompatActivity(), HasDefaultViewModelProviderFactory {

    private val NAME = SendActivity::class.java.simpleName + " "
    private lateinit var binding: ActivitySendBinding
    private val sendViewModel: SendViewModel by viewModels()


    override fun getDefaultViewModelProviderFactory(): ViewModelProvider.Factory {
        return object : ViewModelProvider.NewInstanceFactory() {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(SendViewModel::class.java)) {
                    return SendViewModel(this@SendActivity) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_send)
        binding.apply {

            //back press action
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setDisplayShowHomeEnabled(true)

            lifecycleOwner = this@SendActivity

            viewModel = sendViewModel

            viewModel?.bodyField?.observe(this@SendActivity, Observer {
                if (etBody.text.toString() != it) etBody.setText(it)
            })


            viewModel?.bodyValidation?.observe(this@SendActivity, Observer {
                when (it.status) {
                    Status.ERROR -> tilBody.error = it.data?.run { getString(this) }
                    else -> tilBody.isErrorEnabled = false
                }
            })

            viewModel?.topicValidation?.observe(this@SendActivity, Observer {
                when (it.status) {
                    Status.ERROR -> tilTopic.error = it.data?.run { getString(this) }
                    else -> tilTopic.isErrorEnabled = false
                }
            })

            viewModel?.finishAcitivty?.observe(this@SendActivity, Observer {
                it.getIfNotHandled()?.run {
                    setResult(Activity.RESULT_OK, Intent())
                    finish()
                }
            })
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Timber.d(NAME, "onOptionsItemSelected()")
        when (item.itemId) {
            //back press action
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }


}