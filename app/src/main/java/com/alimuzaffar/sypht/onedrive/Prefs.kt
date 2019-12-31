package com.alimuzaffar.sypht.onedrive

import android.content.Context
import android.content.SharedPreferences
import com.alimuzaffar.sypht.onedrive.Prefs.Key.ACCESS_TOKEN

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
class Prefs private constructor(context: Context) {
    private val mPref: SharedPreferences
    private var mEditor: SharedPreferences.Editor? = null
    private var mBulkUpdate = false

    /**
     * Class for keeping all the keys used for shared preferences in one place.
     */
    object Key {
        /* Recommended naming convention:
         * ints, floats, doubles, longs:
         * SAMPLE_NUM or SAMPLE_COUNT or SAMPLE_INT, SAMPLE_LONG etc.
         *
         * boolean: IS_SAMPLE, HAS_SAMPLE, CONTAINS_SAMPLE
         *
         * String: SAMPLE_KEY, SAMPLE_STR or just SAMPLE
         */
        const val ACCESS_TOKEN = "access_token"
    }

    fun put(key: String?, `val`: String?) {
        doEdit()
        mEditor!!.putString(key, `val`)
        doCommit()
    }

    fun put(key: String?, `val`: Int) {
        doEdit()
        mEditor!!.putInt(key, `val`)
        doCommit()
    }

    fun put(key: String?, `val`: Boolean) {
        doEdit()
        mEditor!!.putBoolean(key, `val`)
        doCommit()
    }

    fun put(key: String?, `val`: Float) {
        doEdit()
        mEditor!!.putFloat(key, `val`)
        doCommit()
    }

    /**
     * Convenience method for storing doubles.
     *
     *
     * There may be instances where the accuracy of a double is desired.
     * SharedPreferences does not handle doubles so they have to
     * cast to and from String.
     *
     * @param key The name of the preference to store.
     * @param val The new value for the preference.
     */
    fun put(key: String?, `val`: Double) {
        doEdit()
        mEditor!!.putString(key, `val`.toString())
        doCommit()
    }

    fun put(key: String?, `val`: Long) {
        doEdit()
        mEditor!!.putLong(key, `val`)
        doCommit()
    }

    fun getString(key: String?, defaultValue: String?): String? {
        return mPref.getString(key, defaultValue)
    }

    fun getString(key: String?): String? {
        return mPref.getString(key, null)
    }

    fun getInt(key: String?): Int {
        return mPref.getInt(key, 0)
    }

    fun getInt(key: String?, defaultValue: Int): Int {
        return mPref.getInt(key, defaultValue)
    }

    fun getLong(key: String?): Long {
        return mPref.getLong(key, 0)
    }

    fun getLong(key: String?, defaultValue: Long): Long {
        return mPref.getLong(key, defaultValue)
    }

    fun getFloat(key: String?): Float {
        return mPref.getFloat(key, 0f)
    }

    fun getFloat(key: String?, defaultValue: Float): Float {
        return mPref.getFloat(key, defaultValue)
    }

    /**
     * Convenience method for retrieving doubles.
     *
     *
     * There may be instances where the accuracy of a double is desired.
     * SharedPreferences does not handle doubles so they have to
     * cast to and from String.
     *
     * @param key The name of the preference to fetch.
     */
    fun getDouble(key: String?): Double {
        return getDouble(key, 0.0)
    }

    /**
     * Convenience method for retrieving doubles.
     *
     *
     * There may be instances where the accuracy of a double is desired.
     * SharedPreferences does not handle doubles so they have to
     * cast to and from String.
     *
     * @param key The name of the preference to fetch.
     */
    fun getDouble(key: String?, defaultValue: Double): Double {
        return try {
            java.lang.Double.valueOf(mPref.getString(key, defaultValue.toString())!!)
        } catch (nfe: NumberFormatException) {
            defaultValue
        }
    }

    fun getBoolean(key: String?, defaultValue: Boolean): Boolean {
        return mPref.getBoolean(key, defaultValue)
    }

    fun getBoolean(key: String?): Boolean {
        return mPref.getBoolean(key, false)
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

    fun setAccessToken(accessToken:String) {
        put(ACCESS_TOKEN, accessToken)
    }

    fun getAccessToken(): String {
        return getString(ACCESS_TOKEN, "")!!
    }

    companion object {
        private const val SETTINGS_NAME = "default_settings"
        private lateinit var sSharedPrefs: Prefs
        fun init(context: Context): Prefs? {
            if (!::sSharedPrefs.isInitialized) {
                sSharedPrefs = Prefs(context.applicationContext)
            }
            return sSharedPrefs
        }
        @get:Synchronized
        val instance: Prefs
            get() {
                if (::sSharedPrefs.isInitialized) {
                    return sSharedPrefs
                }
                throw IllegalArgumentException("Should use getInstance(Context) at least once before using this method.")
            }
    }

    init {
        mPref = context.getSharedPreferences(
            SETTINGS_NAME,
            Context.MODE_PRIVATE
        )
    }
}