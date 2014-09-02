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

public class Config
{
	public static final int levelCountSingle = 4;
	public static final int levelCountOverall = 16;

	public static final int levelDefault = 5;
	
	public static final int scopeDefault = 1;

	public static final int ringsCountFlicker = 2;
	public static final int ringsCountFlame = 3;
	public static final int ringsCountFire = 4;
	public static final int ringsCountWildfire = 5;

	public static final double ringTimeRatioFlicker = 0.2;
	public static final double ringTimeRatioFlame = 0.3;
	public static final double ringTimeRatioFire = 0.4;
	public static final double ringTimeRatioWildfire = 0.5;

	public static final int maxRingDuration = 2 * 60 * 1000;
	public static final int maxTimePeriod = 60 * 60 * 1000;
}
