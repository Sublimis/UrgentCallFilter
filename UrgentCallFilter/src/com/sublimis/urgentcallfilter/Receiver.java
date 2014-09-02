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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;

public class Receiver extends BroadcastReceiver
{
	public static final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
	public static final String WAP_PUSH_RECEIVED = "android.provider.Telephony.WAP_PUSH_RECEIVED";

	@Override
    public void onReceive(Context context, Intent intent)
    {
		try
		{
			if (context != null && intent != null)
			{
				if (TelephonyManager.ACTION_PHONE_STATE_CHANGED.equals(intent.getAction()))
				{
					MyPreference.setContext(context);

					if (MyPreference.isEnabled())
					{
				        if (TelephonyManager.EXTRA_STATE_RINGING.equals(intent.getStringExtra(TelephonyManager.EXTRA_STATE)))
						{
				        	//ringing
				        	
							String number = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
							
							// Note: number can be null if incoming call has hidden number
							if (number == null)
							{
								// null number is reserved for ending the call ring, and also cannot be inserted to database
								number = "";
							}
	
							Magic magic = new Magic(context, number, true);
							magic.doTheMagic();
						}
						else
						{
							// no ringing
							
							Magic magic = new Magic(context, null, false);
							magic.doTheMagic();
						}
					}
				}
			}
		}
		catch (Exception e)
		{
		}
    }
}
