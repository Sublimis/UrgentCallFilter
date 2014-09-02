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
import org.json.JSONObject;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.Uri;
import android.os.SystemClock;
import android.provider.ContactsContract.PhoneLookup;

public class Magic
{
	private Context mContext = null;
	private AudioManager mAudioManager = null;
	private boolean mIsRinging = false;
	private String mNumber = null;
	private int mRingerModeOriginal = -1;
	private int mVolumeOriginal = -1;
	private int mVolumeMax = -1;
	
	private JSONArray mData = new JSONArray();
	
	private static final String jsonTimestampKey = "timestamp";
	private static final String jsonNumberKey = "number";
	private static final String jsonRingDurationKey = "ringDuration";

	public static final int flickerUrgencyMode = 0;
	public static final int flameUrgencyMode = 1;
	public static final int fireUrgencyMode = 2;
	public static final int wildfireUrgencyMode = 3;
	
	public Magic(Context context, String number, boolean isRingStart)
	{
		if (context != null)
		{
			mContext = context;
			mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
			mIsRinging = isRingStart;
			mNumber = number;

			if (mAudioManager != null)
			{
				mRingerModeOriginal = mAudioManager.getRingerMode();
				mVolumeOriginal = mAudioManager.getStreamVolume(AudioManager.STREAM_RING);
				mVolumeMax = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_RING);
			}
			
			MyPreference.setContext(context);
		}
	}
	
	public class RingData
	{
		public int mRingCount;
		public double mRingTimeRatio;
		public long mRingTimePeriod;
		public long mRingDurationCumulative;
	}
	
	private void setRingerAudible()
	{
		if (mAudioManager != null)
			mAudioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
	}
	
	private void restoreRingerMode()
	{
		if (mAudioManager != null)
		{
			int mode = MyPreference.getOriginalRingerMode();
			
			if (mode >= 0)
				mAudioManager.setRingerMode(mode);
		}
	}
	
	private boolean isRingerInRightMode()
	{
		boolean retVal = false;
		
		if (mAudioManager != null)
		{
			int ringerScope = MyPreference.getScope();
			
			switch (mRingerModeOriginal)
			{
			case AudioManager.RINGER_MODE_SILENT:
				if ((ringerScope & 1) != 0)
					retVal = true;
				break;
			case AudioManager.RINGER_MODE_VIBRATE:
				if ((ringerScope & 2) != 0)
					retVal = true;
				break;
			case AudioManager.RINGER_MODE_NORMAL:
				if ((ringerScope & 4) != 0)
				{
					if (mVolumeOriginal < mVolumeMax)
						retVal = true;
				}
				break;
			}
		}
		
		return retVal;
	}
	
	private void setRingerVolume()
	{
		if (MyPreference.isRingVolumeOverriden() || mRingerModeOriginal == AudioManager.RINGER_MODE_NORMAL)
		{
			if (mAudioManager != null)
			{
				int volumeToSet = (int) Math.round((double) MyPreference.getRingVolume() * mVolumeMax / 10.);
				
				if (mRingerModeOriginal == AudioManager.RINGER_MODE_NORMAL)
					volumeToSet = mVolumeMax;
				
				MyPreference.setOriginalRingVolume(mVolumeOriginal);
			
				mAudioManager.setStreamVolume(AudioManager.STREAM_RING, volumeToSet, 0);
			}
		}
		else
		{
			MyPreference.setOriginalRingVolume(-1);
		}
	}
	
	private void restoreRingerVolume()
	{
		int originalVolume = MyPreference.getOriginalRingVolume();
		
		if (originalVolume >= 0)
		{
			if (mAudioManager != null)
			{
				mAudioManager.setStreamVolume(AudioManager.STREAM_RING, originalVolume, 0);
			}

			MyPreference.setOriginalRingVolume(-1);
		}
	}
	
	private boolean isCallerEligible()
	{
		boolean retVal = true;

		if (MyPreference.isContactsOnly())
		{
			if (!strValidAndNotEmpty(mNumber))
			{
				retVal = false;
			}
			else
			{
				try
				{
					Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(mNumber));
					
					if (mContext != null)
					{
						ContentResolver contentResolver = mContext.getContentResolver();
						
						if (contentResolver != null)
						{
							Cursor cur = contentResolver.query(uri, new String[] {PhoneLookup._ID}, null, null, null);
						
							if (cur == null || cur.getCount() <= 0)
							{
								retVal = false;
							}
							
							if (cur != null)
							{
								cur.close();
							}
						}
					}
				}
				catch (Exception e)
				{
				}
			}
		}
		
		return retVal;
	}
	
	@SuppressWarnings("deprecation")
	private void notification()
	{
		if (MyPreference.isNotifications())
		{
			if (mContext != null)
			{
				NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
			
				if (notificationManager != null)
				{
					Notification notification = new Notification(R.drawable.icon, null, System.currentTimeMillis());
					Intent notificationIntent = new Intent(mContext, ActivityMain.class);
					
					if (notification != null && notificationIntent != null)
					{
						notificationIntent.setFlags(notificationIntent.getFlags() | Intent.FLAG_ACTIVITY_NEW_TASK);
						PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
						
						notification.setLatestEventInfo(mContext, mContext.getResources().getString(R.string.notification_title),
								mContext.getResources().getString(R.string.notification_text), pendingIntent);
						
						notificationManager.notify(0, notification);
					}
				}
			}
		}
	}
	
	public static int getIntensityFromLevel(int level)
	{
		final int retVal = level / Config.levelCountSingle;
		
		return retVal;
	}

	public static int getPersistenceFromLevel(int level)
	{
		final int retVal = level % Config.levelCountSingle;
		
		return retVal;
	}
	
	public static int getUrgencyLevelRingCount(int level)
	{
		int retVal = 0;
		
		final int intensity = getIntensityFromLevel(level);

		switch (intensity)
		{
		case Magic.flameUrgencyMode:
		default:
			retVal = Config.ringsCountFlame;
			break;
		case Magic.flickerUrgencyMode:
			retVal = Config.ringsCountFlicker;
			break;
		case Magic.fireUrgencyMode:
			retVal = Config.ringsCountFire;
			break;
		case Magic.wildfireUrgencyMode:
			retVal = Config.ringsCountWildfire;
			break;
		}
		
		return retVal;
	}
	
	public static int getUrgencyLevelRingCount()
	{
		return getUrgencyLevelRingCount(MyPreference.getUrgencyLevel());
	}
	
	public static double getUrgencyLevelRingTimeRatio(int level)
	{
		double retVal = 0;
		
		final int persistence = getPersistenceFromLevel(level);

		switch (persistence)
		{
		case Magic.flameUrgencyMode:
		default:
			retVal = Config.ringTimeRatioFlame;
			break;
		case Magic.flickerUrgencyMode:
			retVal = Config.ringTimeRatioFlicker;
			break;
		case Magic.fireUrgencyMode:
			retVal = Config.ringTimeRatioFire;
			break;
		case Magic.wildfireUrgencyMode:
			retVal = Config.ringTimeRatioWildfire;
			break;
		}
		
		return retVal;
	}
	
	public static double getUrgencyLevelRingTimeRatio()
	{
		return getUrgencyLevelRingTimeRatio(MyPreference.getUrgencyLevel());
	}
	
	private void loadData()
	{
		JSONArray obj = MyPreference.getData();
		
		if (obj != null)
			mData = obj;
		
		purgeData();
	}
	
	private void saveData()
	{
		MyPreference.setData(mData);
	}
	
	/**
	 * Deletes old numbers from the set ie. numbers with timestamps
	 * that have no chance of being eligible for the ring.
	 */
	private void purgeData()
	{
        long currentTime = SystemClock.elapsedRealtime();
        
		if (mData != null && mData.length() > 0)
		{
			for (int i = 0; i < mData.length(); i++)
			{
				JSONObject flame = jsonGetObject(mData, i);
				
				if (flame != null)
				{
					long timestamp = jsonGetLong(flame, jsonTimestampKey, 0);
					
					if (timestamp + Config.maxTimePeriod < currentTime)
					{
						mData = jsonArrayRemove(mData, i);
						
						i--;
						
						continue;
					}
				}
			}
		}		
	}
	
	private void insertSetRingDuration()
	{
        long currentTime = SystemClock.elapsedRealtime();
        
		if (mData != null && mData.length() > 0)
		{
			int i = mData.length() - 1;
			
			JSONObject flame = jsonGetObject(mData, i);
			
			if (flame != null)
			{
				long timestamp = jsonGetLong(flame, jsonTimestampKey, 0);
				long ringDurationOld = jsonGetLong(flame, jsonRingDurationKey, 0);
				
				if (timestamp + Config.maxRingDuration > currentTime)
				{
					if (ringDurationOld <= 0)
					{
						jsonPutLong(flame, jsonRingDurationKey, currentTime - timestamp);
						
						try
						{
							mData.put(i, flame);
						}
						catch (Exception e)
						{
						}
					}
				}
			}
		}		
	}
	
	private void insertNewRing()
	{
		if (mData != null)
		{
	        long currentTime = SystemClock.elapsedRealtime();

	        JSONObject obj = new JSONObject();
			
			jsonPutString(obj, jsonNumberKey, mNumber);
			jsonPutLong(obj, jsonTimestampKey, currentTime);
			jsonPutLong(obj, jsonRingDurationKey, 0);
			
			mData.put(obj);
		}
	}
	
	private RingData calculateRingScore()
	{
		RingData retVal = new RingData();
		// we count this current ring as one
		retVal.mRingCount = 1;

		long firstRingTimestamp = 0;
        long currentTime = SystemClock.elapsedRealtime();

        if (mData != null && mData.length() > 0)
		{
			for (int i = mData.length()-1; i >= 0; i--)
			{
				if (retVal.mRingCount >= getUrgencyLevelRingCount())
				{
					break;
				}
				else
				{
					JSONObject flame = jsonGetObject(mData, i);
					
					if (flame != null)
					{
						String number = jsonGetString(flame, jsonNumberKey, null);
						long timestamp = jsonGetLong(flame, jsonTimestampKey, 0);
						long ringDuration = jsonGetLong(flame, jsonRingDurationKey, 0);
						
						if (strEqual(number, mNumber))
						{
							if (firstRingTimestamp <= 0 || timestamp < firstRingTimestamp)
							{
								firstRingTimestamp = timestamp;
							}
							
							retVal.mRingCount += 1;
							retVal.mRingDurationCumulative += ringDuration;
						}
					}
				}
			}
			
			retVal.mRingTimePeriod = currentTime - firstRingTimestamp;
			
			if (retVal.mRingTimePeriod > 0 && firstRingTimestamp > 0)
				retVal.mRingTimeRatio = (double) retVal.mRingDurationCumulative / retVal.mRingTimePeriod;
			else
				retVal.mRingTimeRatio = 0;
		}		
		
		return retVal;
	}
	
	private boolean isRingEligible()
	{
		boolean retVal = false;
		
		RingData ringData = calculateRingScore();
		
		if (ringData.mRingCount >= getUrgencyLevelRingCount())
		{
			if (ringData.mRingTimeRatio >= getUrgencyLevelRingTimeRatio())
			{
				retVal = true;
			}
		}
		
		return retVal;
	}
	
	public void doTheMagic()
	{
		if (MyPreference.isEnabled())
		{
			if (mIsRinging)
			{
				if (isRingerInRightMode())
				{
					if (isCallerEligible())
					{
						loadData();
						
						if (isRingEligible())
						{
							setRingerAudible();
							setRingerVolume();
						
							MyPreference.setOriginalRingerMode(mRingerModeOriginal);
							
							notification();
						}
	
						insertNewRing();
						
						saveData();
					}
				}
			}
			else
			{
				if (MyPreference.getOriginalRingerMode() >= 0)
				{
					restoreRingerVolume();
					restoreRingerMode();

					MyPreference.setOriginalRingerMode(-1);
				}

				loadData();
				
				insertSetRingDuration();
				
				saveData();
			}
		}
	}
	
	public static long jsonGetLong(JSONObject jsonObject, String name, long defValue)
	{
		long retVal = defValue;
		
		try
		{
			retVal = jsonObject.getLong(name);
		}
		catch (Exception e)
		{
		}
		
		return retVal;
	}
	
	public static void jsonPutLong(JSONObject jsonObject, String name, long value)
	{
		try
		{
			jsonObject.put(name, value);
		}
		catch (Exception e)
		{
		}
	}
	
	public static String jsonGetString(JSONObject jsonObject, String name, String defValue)
	{
		String retVal = defValue;
		
		try
		{
			retVal = jsonObject.getString(name);
		}
		catch (Exception e)
		{
		}
		
		return retVal;
	}
	
	public static void jsonPutString(JSONObject jsonObject, String name, String value)
	{
		try
		{
			jsonObject.put(name, value);
		}
		catch (Exception e)
		{
		}
	}
	
	public static JSONObject jsonGetObject(JSONArray jsonArray, int index)
	{
		JSONObject retVal = null;
		
		try
		{
			retVal = jsonArray.getJSONObject(index);
		}
		catch (Exception e)
		{
		}
		
		return retVal;
	}
	
	public static JSONArray jsonArrayRemove(JSONArray jsonArray, int index)
	{
		JSONArray newJsonArray = jsonArray;
		
		if (jsonArray != null)
		{
			newJsonArray = new JSONArray();
			int len = jsonArray.length();
			
			for (int i = 0; i < len; i++)
			{
				// Excluding the item at position
				if (i != index)
				{
					try
					{
						newJsonArray.put(jsonArray.get(i));
					}
					catch (Exception e)
					{
					}
				}
			}
		}
		
		return newJsonArray;
	}

	public static boolean strEqual(String str1, String str2)
	{
		if (str1 != null)
		{
			return str1.equals(str2);
		}
		else if (str2 != null)
		{
			return str2.equals(str1);
		}
		else
		{
			return true;
		}
	}

	public static boolean strValidAndNotEmpty(String str)
	{
		if (str != null && !("".equals(str)))
			return true;
		else
			return false;
	}
}
