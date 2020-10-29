package com.rohitthebest.manageyourrenters.utils

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.rohitthebest.manageyourrenters.database.entity.Renter

class ConversionWithGson {

    companion object {

        private val gson = Gson()

        fun convertRenterToJSONString(renter: Renter): String {

            return gson.toJson(renter)
        }

        fun convertJSONtoRenter(jsonString: String?): Renter {

            val type = object : TypeToken<Renter?>() {}.type
            return gson.fromJson(jsonString, type)
        }


    }
}