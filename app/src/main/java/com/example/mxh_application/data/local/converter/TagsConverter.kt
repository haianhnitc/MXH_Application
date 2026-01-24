package com.example.mxh_application.data.local.converter

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class TagsConverter {
    
    private val gson = Gson()
    
    @TypeConverter
    fun fromTags(tags: List<String>?): String {
        if (tags == null) {
            return "[]"
        }
        return gson.toJson(tags)
    }
    
    @TypeConverter
    fun toTags(tagsString: String): List<String> {
        if (tagsString.isEmpty()) {
            return emptyList()
        }
        val type = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(tagsString, type)
    }
}
