package com.example.discountgap.data

import android.content.SharedPreferences

interface PreferencesDataSource {
    fun getString(key: String, defaultValue: String? = null): String?
    fun putString(key: String, value: String?)
    fun getBoolean(key: String, defaultValue: Boolean): Boolean
    fun putBoolean(key: String, value: Boolean)
    fun getInt(key: String, defaultValue: Int): Int
    fun putInt(key: String, value: Int)
    fun remove(key: String)
}

class SharedPreferencesDataSource(
    private val sharedPreferences: SharedPreferences
) : PreferencesDataSource {

    override fun getString(key: String, defaultValue: String?): String? {
        return sharedPreferences.getString(key, defaultValue)
    }

    override fun putString(key: String, value: String?) {
        sharedPreferences.edit().putString(key, value).apply()
    }

    override fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return sharedPreferences.getBoolean(key, defaultValue)
    }

    override fun putBoolean(key: String, value: Boolean) {
        sharedPreferences.edit().putBoolean(key, value).apply()
    }

    override fun getInt(key: String, defaultValue: Int): Int {
        return sharedPreferences.getInt(key, defaultValue)
    }

    override fun putInt(key: String, value: Int) {
        sharedPreferences.edit().putInt(key, value).apply()
    }

    override fun remove(key: String) {
        sharedPreferences.edit().remove(key).apply()
    }
}

class InMemoryPreferencesDataSource : PreferencesDataSource {
    private val map = mutableMapOf<String, Any?>()

    override fun getString(key: String, defaultValue: String?): String? {
        return map[key] as? String ?: defaultValue
    }

    override fun putString(key: String, value: String?) {
        if (value == null) {
            map.remove(key)
        } else {
            map[key] = value
        }
    }

    override fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return map[key] as? Boolean ?: defaultValue
    }

    override fun putBoolean(key: String, value: Boolean) {
        map[key] = value
    }

    override fun getInt(key: String, defaultValue: Int): Int {
        return map[key] as? Int ?: defaultValue
    }

    override fun putInt(key: String, value: Int) {
        map[key] = value
    }

    override fun remove(key: String) {
        map.remove(key)
    }
}
