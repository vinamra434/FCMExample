package com.example.fcmexample

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.example.fcmexample.utils.*
import com.example.fcmsender.FCMSender
import com.example.fcmsender.MessageType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

class SendViewModel(private val context: Context) : ViewModel() {

    private val NAME = SendViewModel::class.java.simpleName + " ";
    private val validationsList: MutableLiveData<List<Validator.Validation>> = MutableLiveData()

    private val sharedPreferences by lazy {
        context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )
    }

    val finishAcitivty: MutableLiveData<Event<Map<String, String>>> = MutableLiveData()
    val bodyField: MutableLiveData<String> = MutableLiveData()
    private val notificationTypeField: MutableLiveData<Boolean> = MutableLiveData()
    private val titleField: MutableLiveData<String> = MutableLiveData()
    private val topicField: MutableLiveData<String> = MutableLiveData()

    val bodyValidation: LiveData<Resource<Int>> =
        filterValidation(Validator.Validation.Field.BODY)
    val topicValidation: LiveData<Resource<Int>> =
        filterValidation(Validator.Validation.Field.TOPIC)


    private fun filterValidation(field: Validator.Validation.Field) =
        Transformations.map(validationsList) {
            it.find { validation -> validation.field == field }
                ?.run { return@run this.resource }
                ?: Resource.unknown()
        }

    fun onTypeChange(isChecked: Boolean) = notificationTypeField.postValue(isChecked)

    fun onTitleChange(title: CharSequence) = titleField.postValue(title.toString())

    fun onTopicChange(title: CharSequence) = topicField.postValue(title.toString())

    fun onBodyChange(desc: CharSequence) = bodyField.postValue(desc.toString())


    fun onSendMessage() {
        Timber.tag(TAG).d("$NAME onSendMessage()")

        val type: MessageType? =
            notificationTypeField.value?.run { if (this) MessageType.DATA else MessageType.NOTIFICATION }
        val title = titleField.value?.trim()
        val body = bodyField.value?.trim()
        val topic = topicField.value?.trim()

        val token: String? = sharedPreferences.getString(
            TOKEN, ""
        )

        val validations = Validator.validateInputFields(body, topic)
        validationsList.postValue(validations)

        if (validations.isNotEmpty()) {

            val successValidation = validations.filter { it.resource.status == Status.SUCCESS }

            if (successValidation.size == validations.size) {

                Timber.tag(TAG).d(
                    "$NAME onSendMessage() request params are:"
                            + "\ntoken:\t $token"
                            + "\ntopic:\t $topic"
                            + "\nbody:\t $body"
                            + "\ntitle:\t $title"
                            + "\ntype:\t $type"
                            + "\n"
                )

                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val response = FCMSender.FCMMessageBuilder()
                            .setMessageType(type ?: MessageType.NOTIFICATION)
                            .setTitle(title.toString())
                            .setBody(body.toString())
                            .setTopic(topic.toString())
                            .build()
                            .sendTo(token.toString())

                        if (response.success == 1) {
                            Timber.tag(TAG).d("onSendMessage() notification success")
                            finishAcitivty.postValue(Event(emptyMap()))
                        } else {
                            Timber.tag(TAG).d("onSendMessage() notification failure")
                        }

                    } catch (e: Exception) {
                        Timber.tag(TAG).e("onSendMessage caught exception $e")

                    }
                }
            }
        }
    }
}