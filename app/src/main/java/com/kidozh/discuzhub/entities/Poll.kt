package com.kidozh.discuzhub.entities

import android.util.Log
import com.fasterxml.jackson.annotation.*
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.kidozh.discuzhub.entities.Poll
import java.io.IOException
import java.io.Serializable
import java.util.*

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.ALWAYS)
class Poll : Serializable {
    @JvmField
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JsonDeserialize(using = OneZeroBooleanDeserializer::class)
    var multiple = false

    @JvmField
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "s")
    var expirations: Date? = null

    @JvmField
    @JsonProperty("maxchoices")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    var maxChoices = 0

    @JvmField
    @JsonProperty("voterscount")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    var votersCount = 0

    @JvmField
    @JsonProperty("visiblepoll")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JsonDeserialize(using = OneZeroBooleanDeserializer::class)
    var resultVisible = false

    @JvmField
    @JsonProperty("allowvote")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JsonDeserialize(using = OneZeroBooleanDeserializer::class)
    var allowVote = false

    @JvmField
    @JsonProperty("polloptions")
    @JsonDeserialize(using = OptionsDeserializer::class)
    var options: List<Option> = ArrayList()

    @JvmField
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonIgnore
    var remaintime: List<String>? = null

    val checkedOptionNumber: Int
        get() = run {
            var count = 0
            for (i in options.indices) {
                if (options[i].checked) {
                    count += 1
                }
            }
            count
        }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.ALWAYS)
    class Option : Serializable {
        @JvmField
        @JsonProperty("polloptionid")
        var id: String = ""

        @JvmField
        @JsonProperty("polloption")
        var name: String = ""

        @JvmField
        @JsonProperty("votes")
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var voteNumber = 0
        @JvmField
        var width: String? = null

        @JvmField
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var percent = 0f

        @JvmField
        @JsonProperty("color")
        var colorName: String? = null

        @JvmField
        @JsonProperty("imginfo")
        @JsonDeserialize(using = ImageInfoDeserializer::class)
        @JsonIgnore
        var imageInfo: ImageInfo? = null


        @JsonIgnore
        var checked = false
    }


    @JsonIgnoreProperties(ignoreUnknown = true)
    class ImageInfo : Serializable {
        var aid: String? = null
        var poid: String? = null
        var tid: String? = null
        var pid: String? = null
        var uid: String? = null
        var filename: String? = null
        var filesize: String? = null
        var attachment: String? = null
        var remote: String? = null
        var width: String? = null
        var thumb: String? = null

        @JsonProperty("dateline")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "s")
        var updateAt: Date? = null

        @JsonProperty("small")
        var smallURL: String? = null

        
        @JsonProperty("big")
        var bigURL: String? = null
    }

    class OptionsDeserializer : JsonDeserializer<List<Option>>() {
        @Throws(IOException::class, JsonProcessingException::class)
        override fun deserialize(p: JsonParser, ctxt: DeserializationContext): List<Option> {
            val currentToken = p.currentToken
            Log.d(TAG, "Get json text " + p.text)
            if (currentToken == JsonToken.START_OBJECT) {
                val codec = p.codec
                val optionMapperNode = codec.readTree<JsonNode>(p)
                var cnt = 1
                val objectMapper = ObjectMapper()
                val options: MutableList<Option> = ArrayList()
                while (true) {
                    val cntString = cnt.toString()
                    if (optionMapperNode.has(cntString)) {
                        val optionObj = optionMapperNode[cntString]
                        val parsedOption = objectMapper.treeToValue(optionObj, Option::class.java)
                        options.add(parsedOption)
                    } else {
                        break
                    }
                    cnt += 1
                }
                return options
            }
            return ArrayList()
        }
    }

    class ImageInfoDeserializer : JsonDeserializer<ImageInfo?>() {
        @Throws(IOException::class, JsonProcessingException::class)
        override fun deserialize(p: JsonParser, ctxt: DeserializationContext): ImageInfo? {
            val currentToken = p.currentToken
            if (currentToken == JsonToken.START_ARRAY) {
                return null
            } else if (currentToken == JsonToken.START_OBJECT) {
                val codec = p.codec
                return codec.readValue(p, ImageInfo::class.java)
            }
            return null
        }
    }

    class OneZeroBooleanDeserializer : JsonDeserializer<Boolean>() {
        @Throws(IOException::class)
        override fun deserialize(jp: JsonParser, ctxt: DeserializationContext): Boolean {
            val currentToken = jp.currentToken
            if (currentToken == JsonToken.VALUE_STRING) {
                val text = jp.text
                return if ("0" == text || "" == text) {
                    java.lang.Boolean.FALSE
                } else {
                    java.lang.Boolean.TRUE
                }
            } else if (currentToken == JsonToken.VALUE_NULL) {
                return java.lang.Boolean.FALSE
                //return null
            }
            throw ctxt.mappingException("Can't parse boolean value: " + jp.text)
        }
    }

    companion object {
        var TAG = Poll::class.java.simpleName
    }
}

class PollJsonDeserializer : JsonDeserializer<Poll?>() {
    companion object{
        val TAG = PollJsonDeserializer::class.simpleName
    }

    @Throws(IOException::class, JsonProcessingException::class)
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Poll? {
        return when (p.currentToken) {

            JsonToken.START_OBJECT -> {
                Log.d(TAG,"Poll start at ${p.text}")
                val mapper = jacksonObjectMapper()
                return mapper.readValue(p,object : TypeReference<Poll>(){})

            }
            JsonToken.START_ARRAY -> {
                null
            }
            else -> {
                null
            }
        }
    }
}