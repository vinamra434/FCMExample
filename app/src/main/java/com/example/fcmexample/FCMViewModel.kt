package com.example.fcmexample

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.fcmexample.db.FCMExampleDB
import com.example.fcmexample.utils.Event

class FCMViewModel(private val db: FCMExampleDB) : ViewModel() {

    val notifications = db.notificationsDao().getNotificationsObservable()

    val goToSendActivity: MutableLiveData<Event<Map<String, String>>> =
        MutableLiveData<Event<Map<String, String>>>()

    fun navigateToSendActivity() {
        goToSendActivity.postValue(Event(emptyMap()))
    }

}