package com.kidozh.discuzhub.results

import android.text.TextUtils
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.kidozh.discuzhub.activities.BaseStatusActivity
import com.kidozh.discuzhub.entities.ErrorMessage

@JsonIgnoreProperties(ignoreUnknown = true)
open class BaseResult {
    @JsonProperty("Version")
    var apiVersion: Int = 4

    @JsonProperty("Charset")
    var charset: String = "UTF-8"

    @JsonIgnore
    fun getCharsetType():Int{
        return when (charset) {
            "GBK" -> {
                BaseStatusActivity.CHARSET_GBK
            }
            "BIG5" -> {
                BaseStatusActivity.CHARSET_BIG5
            }
            else -> {
                BaseStatusActivity.CHARSET_UTF8
            }
        }
    }

    @JvmField
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonProperty("Message")
    var message: MessageResult? = null

    @JvmField
    @JsonIgnoreProperties(ignoreUnknown = true)
    var error = ""
    @JsonIgnore
    open fun isError(): Boolean {
        return message != null || !TextUtils.isEmpty(error)
    }

    val errorMessage: ErrorMessage?
        get() = if (message != null) {
            ErrorMessage(message!!.key, message!!.content)
        } else if (!TextUtils.isEmpty(error)) {
            ErrorMessage(error, error)
        } else {
            null
        }
}