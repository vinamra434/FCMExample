package com.example.fcmexample.utils

import com.example.fcmexample.R

object Validator {

    fun validateInputFields(
        body: String?,
        topic: String?
    ): List<Validation> =
        ArrayList<Validation>().apply {
            when {
                body.isNullOrEmpty() ->
                    add(
                        Validation(
                            Validation.Field.BODY,
                            Resource.error(R.string.body_empty)
                        )
                    )
                else ->
                    add(Validation(Validation.Field.BODY, Resource.success()))
            }
            when {
                topic.isNullOrEmpty() ->
                    add(
                        Validation(
                            Validation.Field.TOPIC,
                            Resource.error(R.string.topic_empty)
                        )
                    )
                else ->
                    add(Validation(Validation.Field.TOPIC, Resource.success()))
            }


        }


    data class Validation(val field: Field, val resource: Resource<Int>) {

        enum class Field {
            BODY,
            TOPIC
        }
    }
}