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

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class ActivityMain extends PreferenceActivity
{
	private static Dialog mLevelDialog = null;
	private static Dialog mVolumeDialog = null;

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		MyPreference.setContext(this);
		
		addPreferencesFromResource(R.layout.activity_main);

		updateLevelsPrefTexts(this, MyPreference.getUrgencyLevel());
		updateVolumePrefTexts(MyPreference.getRingVolume());
		updateScopePrefTexts(MyPreference.getScope());
		
		MyPrefClickListener myPrefClickListener = new MyPrefClickListener();
		MyPrefChangeListener myPrefChangeListener = new MyPrefChangeListener();

		Preference pref = null;
		
		pref = findPreference(getResources().getString(R.string.pref_dummy_level_key));
		if (pref != null)
		{
			pref.setOnPreferenceClickListener(myPrefClickListener);
		}

		pref = findPreference(getResources().getString(R.string.pref_volume_override_key));
		if (pref != null)
		{
			pref.setOnPreferenceClickListener(myPrefClickListener);
		}

		pref = findPreference(getResources().getString(R.string.pref_scope_key));
		if (pref != null)
		{
			pref.setOnPreferenceChangeListener(myPrefChangeListener);
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		menu.add(Menu.NONE, 2, Menu.NONE, R.string.optmenu_help_text).setIcon(android.R.drawable.ic_menu_help);
		menu.add(Menu.NONE, 1, Menu.NONE, R.string.optmenu_share_text).setIcon(android.R.drawable.ic_menu_share);
		menu.add(Menu.NONE, 0, Menu.NONE, R.string.optmenu_about_text).setIcon(android.R.drawable.ic_menu_info_details);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
		case 0:
			dialogAbout();
			return true;
		case 1:
			dialogShare();
			return true;
		case 2:
			startActivity(new Intent(this, ActivityHelp.class));
			return true;
		}
		return false;
	}
	
	@SuppressLint("InflateParams")
	protected Dialog onCreateDialog(int id)
	{
		Dialog dialog = null;

		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		switch (id)
		{
		case 0:
			{
				dialog = new Dialog(this);
	
				dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
				dialog.setContentView(R.layout.dialog_about);
	
				dialogSetBackgroundTransparent(dialog);

				TextView textView = (TextView) dialog.findViewById(R.id.aboutText1);
				String text = getResources().getString(R.string.copyright_part1);
				textView.setText(Html.fromHtml(String.format(text, getResources().getString(R.string.appVersion), getResources().getString(R.string.appDate))));
	
				textView = (TextView) dialog.findViewById(R.id.aboutText2);
				textView.setText(R.string.copyright_part2);
				textView.setMovementMethod(LinkMovementMethod.getInstance());
			}
			break;
			
		case 1:
			MyPreference.setContext(this);

			builder.setTitle(getLevelTitleText(this, MyPreference.getUrgencyLevel()));
			builder.setPositiveButton(R.string.ok_button_text, new DialogInterface.OnClickListener()
			{
				@Override
				public void onClick(DialogInterface di, int which)
				{
					levelDialogButtonOkClick();
				}
			});
			builder.setNegativeButton(R.string.cancel_button_text, null);
			builder.setView(getLayoutInflater().inflate(R.layout.dialog_level, null));
			
			dialog = builder.create();
			
			mLevelDialog = dialog;
			
			break;
			
		case 2:
			builder.setTitle(R.string.pref_volume_dialog_title);
			builder.setMessage(R.string.pref_volume_dialog_message);
			builder.setPositiveButton(R.string.ok_button_text, new DialogInterface.OnClickListener()
			{
				@Override
				public void onClick(DialogInterface di, int which)
				{
					volumeDialogButtonOkClick();
				}
			});
			builder.setNegativeButton(R.string.cancel_button_text, null);
			builder.setView(getLayoutInflater().inflate(R.layout.dialog_volume, null));
			
			dialog = builder.create();
			
			mVolumeDialog = dialog;
			
			break;
			
		default:
			break;
		}
		
		return dialog;
	}

	protected void onPrepareDialog(int id, Dialog dialog, Bundle args)
	{
		switch (id)
		{
		case 1:
			if (mLevelDialog != null)
			{
				updateLevelDialogTexts(MyPreference.getUrgencyLevel());
				
				SeekBar pref = (SeekBar) mLevelDialog.findViewById(R.id.levelSeekBar);
				
				if (pref != null)
				{
					pref.setMax(Config.levelCountOverall - 1);
					pref.setProgress(MyPreference.getUrgencyLevel());
					
					pref.setOnSeekBarChangeListener(
						new OnSeekBarChangeListener()
						{
							@Override
							public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
							{
								updateLevelDialogTexts(progress);
							}

							@Override
							public void onStartTrackingTouch(SeekBar seekBar)
							{
							}
							@Override
							public void onStopTrackingTouch(SeekBar seekBar)
							{
							}
						});
				}
			}
			break;

		case 2:
			if (mVolumeDialog != null)
			{
				updateVolumeDialogTexts(MyPreference.getRingVolume());
				
				SeekBar pref = (SeekBar) mVolumeDialog.findViewById(R.id.volumeSeekBar);
				
				if (pref != null)
				{
					pref.setMax(10);
					pref.setProgress(MyPreference.getRingVolume());
					
					pref.setOnSeekBarChangeListener(
						new OnSeekBarChangeListener()
						{
							@Override
							public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
							{
								if (progress < 1)
								{
									progress = 1;
									seekBar.setProgress(progress);
								}
								
								updateVolumeDialogTexts(progress);
							}

							@Override
							public void onStartTrackingTouch(SeekBar seekBar)
							{
							}
							@Override
							public void onStopTrackingTouch(SeekBar seekBar)
							{
							}
						});
				}
			}
			break;
		}
	}
	
	private class MyPrefClickListener implements Preference.OnPreferenceClickListener
	{
		@Override
		public boolean  onPreferenceClick(Preference preference)
		{
			boolean retVal = true;
			
			if (getResources().getString(R.string.pref_dummy_level_key).equals(preference.getKey()))
			{
				dialogLevel();
			}
			else if (getResources().getString(R.string.pref_volume_override_key).equals(preference.getKey()))
			{
				updateVolumePrefTexts(MyPreference.getRingVolume());

				if (MyPreference.isRingVolumeOverriden())
					dialogVolume();
			}

			return retVal;
		}
	}

	private class MyPrefChangeListener implements Preference.OnPreferenceChangeListener
	{
		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue)
		{
			boolean retVal = true;
			
			if (getResources().getString(R.string.pref_scope_key).equals(preference.getKey()))
			{
				updateScopePrefTexts(Integer.valueOf((String) newValue));
			}

			return retVal;
		}
	}

	public void levelDialogButtonOkClick()
	{
		if (mLevelDialog != null)
		{
			SeekBar pref = (SeekBar) mLevelDialog.findViewById(R.id.levelSeekBar);
			
			if (pref != null)
			{
				MyPreference.setContext(mLevelDialog.getContext());
				MyPreference.setUrgencyLevel(pref.getProgress());
			}

			updateLevelsPrefTexts(this, MyPreference.getUrgencyLevel());
		}
	}
	
	public void volumeDialogButtonOkClick()
	{
		if (mVolumeDialog != null)
		{
			SeekBar pref = (SeekBar) mVolumeDialog.findViewById(R.id.volumeSeekBar);
			
			if (pref != null)
			{
				MyPreference.setContext(mVolumeDialog.getContext());
				MyPreference.setRingVolume(pref.getProgress());
			}

			updateVolumePrefTexts(MyPreference.getRingVolume());
		}
	}
	
	@SuppressWarnings("deprecation")
	private void dialogVolume()
	{
		showDialog(2);
	}

	@SuppressWarnings("deprecation")
	private void dialogLevel()
	{
		showDialog(1);
	}

	@SuppressWarnings("deprecation")
	private void dialogAbout()
	{
		showDialog(0);
	}

	private void dialogShare()
	{
		Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
		if (sharingIntent != null)
		{
			sharingIntent.setType("text/plain");
			sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getResources().getText(R.string.share_subject));
			sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, getResources().getText(R.string.app_link));
			startActivity(Intent.createChooser(sharingIntent, getResources().getText(R.string.share_title)));
		}
	}

	private void updateLevelDialogTexts(int level)
	{
		if (mLevelDialog != null)
		{
			TextView view = null;
			
			view = (TextView) mLevelDialog.findViewById(R.id.levelStatus);
			
			if (view != null)
			{
				String text = getLevelSummaryText(mLevelDialog.getContext(), level, false);
				view.setText(text);
			}
			
			String text = getLevelTitleText(mLevelDialog.getContext(), level);

			mLevelDialog.setTitle(text);
		}
	}
	
	public static void updateLevelsPrefTexts(Context context, int level)
	{
		if (context != null)
		{
			@SuppressWarnings("deprecation")
			Preference pref = (Preference) ((PreferenceActivity) context).findPreference(context.getResources().getString(R.string.pref_dummy_level_key));
			
			if (pref != null)
			{
				String summary = getLevelSummaryText(context, level, true);
				String title = getLevelTitleText(context, level);
				
				pref.setTitle(title);
				pref.setSummary(summary);
			}
		}
	}
	
	private void updateVolumeDialogTexts(int volume)
	{
		if (mVolumeDialog != null)
		{
			String text = mVolumeDialog.getContext().getResources().getString(R.string.pref_volume_summary_text);
			String title = String.format(text, 10*volume);
				
			mVolumeDialog.setTitle(title);
		}
	}
	
	public static void updateVolumePrefTexts(Context context, int volume)
	{
		if (context != null)
		{
			@SuppressWarnings("deprecation")
			Preference pref = (Preference) ((PreferenceActivity) context).findPreference(context.getResources().getString(R.string.pref_volume_override_key));
			
			if (pref != null)
			{
				String text = "";
				String summary = "";
				
				if (MyPreference.isRingVolumeOverriden())
				{
					text = context.getResources().getString(R.string.pref_volume_summary_text);
					summary = String.format(text, 10*volume);
				}
				else
				{
					summary = context.getResources().getString(R.string.pref_volume_default_summary_text);
				}
				
				pref.setSummary(summary);
			}
		}
	}
	
	public void updateVolumePrefTexts(int volume)
	{
		updateVolumePrefTexts(this, volume);
	}
	
	public static void updateScopePrefTexts(Context context, int scope)
	{
		if (context != null)
		{
			@SuppressWarnings("deprecation")
			Preference pref = (Preference) ((PreferenceActivity) context).findPreference(context.getResources().getString(R.string.pref_scope_key));
			
			if (pref != null)
			{
				String[] entries = context.getResources().getStringArray(R.array.pref_scope_entries);
				String[] values = context.getResources().getStringArray(R.array.pref_scope_values);
				String summary = "";
	
				for (int i=0; i < values.length; i++)
				{
					try
					{
						if (scope == Integer.valueOf(values[i]))
						{
							summary = entries[i];
							break;
						}
					}
					catch (RuntimeException e)
					{
					}
				}
				
				pref.setTitle(summary);
			}
		}
	}
	
	public void updateScopePrefTexts(int scope)
	{
		updateScopePrefTexts(this, scope);
	}

	public static String getSingleLevelDescription(Context context, int level)
	{
		String output = "";
		
		if (context != null)
		{
			switch (level)
			{
			case Magic.flickerUrgencyMode:
				output = context.getResources().getString(R.string.level_1_adjective);
				break;
			case Magic.flameUrgencyMode:
				output = context.getResources().getString(R.string.level_2_adjective);
				break;
			case Magic.fireUrgencyMode:
				output = context.getResources().getString(R.string.level_3_adjective);
				break;
			case Magic.wildfireUrgencyMode:
				output = context.getResources().getString(R.string.level_4_adjective);
				break;
			}
		}
		
		return output;
	}
	
	public static String getLevelTitleText(Context context, int level)
	{
		String output = "";
		
		if (context != null)
		{
			String text = context.getResources().getString(R.string.level_title_text);
			output = String.format(text, 1+Magic.getIntensityFromLevel(level), 1+Magic.getPersistenceFromLevel(level));
		}
		
		return output;
	}
	
	public static String getLevelSummaryText(Context context, int level, boolean descriptive)
	{
		String output = "";
		
		if (context != null)
		{
			int ringCount = Magic.getUrgencyLevelRingCount(level) - 1;
			int timeRatioPercent = (int) Math.round(Magic.getUrgencyLevelRingTimeRatio(level) * 100);
			
			output = context.getResources().getQuantityString(R.plurals.level_summary_text_descriptive, ringCount, ringCount, timeRatioPercent);
		}
		
		return output;
	}

	public static void dialogSetBackgroundTransparent(Dialog dialog)
	{
		Window dialogWindow = dialog.getWindow();
		if (dialogWindow != null)
		{
			View dialogDecorView = dialogWindow.getDecorView();

			if (dialogDecorView != null)
				dialogDecorView.setBackgroundColor(0);
		}
	}
}
