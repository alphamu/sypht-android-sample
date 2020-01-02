package com.alimuzaffar.sypht.onedrive.util

import android.content.Context
import android.content.SharedPreferences

/*
 * A Singleton for managing your SharedPreferences.
 *
 * You should make sure to change the SETTINGS_NAME to what you want
 * and choose the operating made that suits your needs, the default is
 * MODE_PRIVATE.
 *
 * IMPORTANT: The class is not thread safe. It should work fine in most
 * circumstances since the write and read operations are fast. However
 * if you call edit for bulk updates and do not commit your changes
 * there is a possibility of data loss if a background thread has modified
 * preferences at the same time.
 *
 * Usage:
 *
 * int sampleInt = Prefs.getInstance(context).getInt(Key.SAMPLE_INT);
 * Prefs.getInstance(context).set(Key.SAMPLE_INT, sampleInt);
 *
 * If Prefs.getInstance(Context) has been called once, you can
 * simple use Prefs.getInstance() to save some precious line space.
 */
class MapIds private constructor(context: Context) {
    private val mPref: SharedPreferences
    private var mEditor: SharedPreferences.Editor? = null
    private var mBulkUpdate = false

    private var count = 0
    private var loading = 0

    operator fun set(key: String, value: String?) {
        doEdit()
        mEditor!!.putString(key, value)
        doCommit()
    }

    operator fun get(key: String): String? {
        return mPref.getString(key, null)
    }

    fun hasLoading(): Boolean {
        return mPref.all.values.any {
            (it as String).isNullOrBlank()
        }
    }

    fun containsKey(key: String) : Boolean {
        return mPref.all.containsKey(key)
    }

    /**
     * Remove keys from SharedPreferences.
     *
     * @param keys The name of the key(s) to be removed.
     */
    fun remove(vararg keys: String?) {
        doEdit()
        for (key in keys) {
            mEditor!!.remove(key)
        }
        doCommit()
    }

    /**
     * Remove all keys from SharedPreferences.
     */
    fun clear() {
        doEdit()
        mEditor!!.clear()
        doCommit()
    }

    fun edit() {
        mBulkUpdate = true
        mEditor = mPref.edit()
    }

    fun commit() {
        mBulkUpdate = false
        mEditor!!.commit()
        mEditor = null
    }

    private fun doEdit() {
        if (!mBulkUpdate && mEditor == null) {
            mEditor = mPref.edit()
        }
    }

    private fun doCommit() {
        if (!mBulkUpdate && mEditor != null) {
            mEditor!!.commit()
            mEditor = null
        }
    }

    companion object {
        private const val SETTINGS_NAME = "ids_map"
        private lateinit var sSharedPrefs: MapIds
        fun init(context: Context): MapIds? {
            if (!Companion::sSharedPrefs.isInitialized) {
                sSharedPrefs =
                    MapIds(context.applicationContext)
            }
            return sSharedPrefs
        }
        @get:Synchronized
        val instance: MapIds
            get() {
                if (Companion::sSharedPrefs.isInitialized) {
                    return sSharedPrefs
                }
                throw IllegalArgumentException("Should use getInstance(Context) at least once before using this method.")
            }
    }

    init {
        mPref = context.getSharedPreferences(
            "$SETTINGS_NAME:${Prefs.instance.getEmail()}",
            Context.MODE_PRIVATE
        )
    }
}