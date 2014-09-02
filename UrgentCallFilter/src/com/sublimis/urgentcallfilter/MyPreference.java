/*
    Copyright 2013-2014. Sublimis

    This file is part of Urgent Call Filter app for Android.

    Urgent Call Filter is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    The software is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this software. If not, see <http://www.gnu.org/licenses/>.
*/

package com.sublimis.urgentcallfilter;

import org.json.JSONArray;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.preference.PreferenceManager;

public class MyPreference
{
	private static Context mContext = null;

	public synchronized static void setContext(Context context)
	{
		if (mContext == null)
			mContext = context.getApplicationContext();
	}
	
	private static SharedPreferences getPrefs()
	{
		if (mContext != null)
			return PreferenceManager.getDefaultSharedPreferences(mContext);
		else
			return null;
	}

	private static Editor getPrefsEditor()
	{
		SharedPreferences sharedPrefs = getPrefs();
		
		if (sharedPrefs != null)
			return sharedPrefs.edit();
		else
			return null;
	}

	private static Resources getResources()
	{
		if (mContext != null)
			return mContext.getResources();
		else
			return null;
	}
	
	private static String getStringResource(int resId)
	{
		Resources res = getResources();
		
		if (res != null)
			return res.getString(resId);
		else
			return null;
	}
	
	private static boolean getBooleanPref(int prefKeyResId, boolean defaultValue)
	{
		String prefKey = getStringResource(prefKeyResId);
		SharedPreferences sharedPrefs = getPrefs();
		
		if (sharedPrefs != null)
			return sharedPrefs.getBoolean(prefKey, defaultValue);
		else
			return defaultValue;
	}
	
	@SuppressWarnings("unused")
	private static void setBooleanPref(int prefKeyResId, boolean newValue)
	{
		String prefKey = getStringResource(prefKeyResId);
		Editor spEditor = getPrefsEditor();
		
		if (spEditor != null)
		{
			spEditor.putBoolean(prefKey, newValue);
			spEditor.commit();
		}
	}
	
	public static int getIntPref(int prefKeyResId, int defaultValue)
	{
		String prefKey = getStringResource(prefKeyResId);
		SharedPreferences sharedPrefs = getPrefs();
		
		if (sharedPrefs != null)
			return sharedPrefs.getInt(prefKey, defaultValue);
		else
			return defaultValue;
	}
	
	public static void setIntPref(int prefKeyResId, int newValue)
	{
		String prefKey = getStringResource(prefKeyResId);
		Editor spEditor = getPrefsEditor();
		
		if (spEditor != null)
		{
			spEditor.putInt(prefKey, newValue);
			spEditor.commit();
		}
	}
	
	private static String getStringPref(int prefKeyResId, String defaultValue)
	{
		String prefKey = getStringResource(prefKeyResId);
		SharedPreferences sharedPrefs = getPrefs();
		
		if (sharedPrefs != null)
			return sharedPrefs.getString(prefKey, defaultValue);
		else
			return defaultValue;
	}
	
	private static void setStringPref(int prefKeyResId, String newValue)
	{
		String prefKey = getStringResource(prefKeyResId);
		Editor spEditor = getPrefsEditor();
		
		if (spEditor != null)
		{
			spEditor.putString(prefKey, newValue);
			spEditor.commit();
		}
	}
	
	
	public static boolean isEnabled()
	{
		return getBooleanPref(R.string.pref_enabled_key, true);
	}

	public static JSONArray getData()
	{
		String input = getStringPref(R.string.pref_database_key, null);
		
		JSONArray obj = null;
		
		try
		{
			obj = new JSONArray(input);
		}
		catch (Exception e)
		{
		}
		
		return obj;
	}

	public static void setData(JSONArray data)
	{
		String output = null;
		
		if (data != null)
			output = data.toString();
		
		setStringPref(R.string.pref_database_key, output);
	}

	public static int getUrgencyLevel()
	{
		return getIntPref(R.string.pref_level_key, Config.levelDefault);
	}

	public static void setUrgencyLevel(int newLevel)
	{
		setIntPref(R.string.pref_level_key, newLevel);
	}

	public static int getOriginalRingerMode()
	{
		return getIntPref(R.string.pref_original_ringer_mode_key, -1);
	}

	public static void setOriginalRingerMode(int mode)
	{
		setIntPref(R.string.pref_original_ringer_mode_key, mode);
	}

	public static boolean isRingVolumeOverriden()
	{
		return getBooleanPref(R.string.pref_volume_override_key, true);
	}

	public static int getRingVolume()
	{
		return getIntPref(R.string.pref_volume_key, 5);
	}

	public static void setRingVolume(int newValue)
	{
		setIntPref(R.string.pref_volume_key, newValue);
	}

	public static int getOriginalRingVolume()
	{
		return getIntPref(R.string.pref_original_volume_key, -1);
	}

	public static void setOriginalRingVolume(int newValue)
	{
		setIntPref(R.string.pref_original_volume_key, newValue);
	}

	public static int getScope()
	{
		int retVal = Config.scopeDefault;
		
		try
		{
			retVal = Integer.valueOf(getStringPref(R.string.pref_scope_key, Integer.toString(Config.scopeDefault)));
		}
		catch (RuntimeException e)
		{
		}
		
		return retVal;
	}

	public static boolean isNotifications()
	{
		return getBooleanPref(R.string.pref_notification_key, true);
	}

	public static boolean isContactsOnly()
	{
		return getBooleanPref(R.string.pref_contactsonly_key, true);
	}
}
