package com.maher.irrigator.sharedTool

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson

object SharedPreferencesTool {
    private val fileName = "com.maher.irrigator"

    private fun getEditor(context: Context?): SharedPreferences.Editor? {
        val preferences = getSharedPreferences(context)
        return preferences?.edit()
    }

    private fun getSharedPreferences(context: Context?): SharedPreferences? {

        return context?.getSharedPreferences(fileName, Context.MODE_PRIVATE)
    }

    fun saveObject(context: Context?, key: String, myObject: Any?) {
        val editor = getEditor(context)
        val gson = Gson()
        val json = gson.toJson(myObject)
        editor?.putString(key, json)
        editor?.apply()
    }

    fun <H> getObject(context: Context?, key: String, className: Class<H>): H {
        val gson = Gson()
        val sharedPreferences = getSharedPreferences(context)
        val json = sharedPreferences?.getString(key, "")
        return gson.fromJson(json, className)
    }

    fun setBoolean(context: Context?, remmberMe: Boolean, key: String) {
        val editor = getEditor(context)
        editor?.putBoolean(key, remmberMe)
        editor?.apply()
    }

    fun getBoolean(context: Context, key: String): Boolean? {
        val sharedPreferences = getSharedPreferences(context)
        return sharedPreferences?.getBoolean(key, false)
    }

    fun getBooleanLang(context: Context, key: String): Boolean? {
        val sharedPreferences = getSharedPreferences(context)
        return sharedPreferences?.getBoolean(key, true)
    }

    fun getInt(context: Context, key: String): Int? {
        val sharedPreferences = getSharedPreferences(context)
        return sharedPreferences?.getInt(key, -1)
    }

    fun setInt(context: Context, key: String, value: Int) {
        val editor = getEditor(context)
        editor?.putInt(key, value)
        editor?.apply()
    }

    fun getLong(context: Context, key: String): Long? {
        val sharedPreferences = getSharedPreferences(context)
        return sharedPreferences?.getLong(key, -1)
    }

    fun setLong(context: Context, key: String, value: Long) {
        val editor = getEditor(context)
        editor?.putLong(key, value)
        editor?.apply()
    }

    fun getString(context: Context, key: String): String? {
        val sharedPreferences = getSharedPreferences(context)
        return sharedPreferences?.getString(key, "")
    }

    fun setString(context: Context, key: String, value: String) {
        val editor = getEditor(context)
        editor?.putString(key, value)
        editor?.apply()
    }

    fun clearObject(context: Context?) {
        val settings = context?.getSharedPreferences(fileName, Context.MODE_PRIVATE)
        settings?.edit()?.clear()?.apply()
    }

    fun clearObject(context: Context, key: String) {
        val settings = context.getSharedPreferences(fileName, Context.MODE_PRIVATE)
        settings.edit().remove(key).apply()
    }
}
