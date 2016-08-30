/*
 * Copyright (c) 2014. Queen Mary University of London
 * Kleomenis Katevas, k.katevas@qmul.ac.uk
 *
 * This file is part of SensingKit-Android library.
 * For more information, please visit http://www.sensingkit.org
 *
 * SensingKit-Android is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SensingKit-Android is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with SensingKit-Android.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.sensingkit.sensingkitlib.data;

import org.sensingkit.sensingkitlib.SKSensorType;

import java.util.Locale;

/**
 *  An instance of SKGravityData encapsulates measurements related to the Gravity sensor.
 */
public class SKGravityData extends SKAbstractData {

    @SuppressWarnings("unused")
    private static final String TAG = "SKGravityData";

    protected final float x;
    protected final float y;
    protected final float z;

    /**
     * Initialize the instance
     *
     * @param timestamp Time in milliseconds (the difference between the current time and midnight, January 1, 1970 UTC)
     *
     * @param x X-axis value
     *
     * @param y Y-axis value
     *
     * @param z Z-axis value
     */
    public SKGravityData(long timestamp, float x, float y, float z) {

        super(SKSensorType.GRAVITY, timestamp);

        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * Get the Gravity sensor data in csv format
     *
     * @return String in csv format:  timestamp, x-axis, y-axis, z-axis
     */
    @Override
    public String getDataInCSV() {
        return String.format(Locale.US, "%d,%f,%f,%f", this.timestamp, this.x, this.y, this.z);
    }

    /**
     * Get the Gravity sensor X-axis value
     *
     * @return X-axis value
     */
    @SuppressWarnings("unused")
    public float getX() {
        return this.x;
    }

    /**
     * Get the Gravity sensor Y-axis value
     *
     * @return Y-axis value
     */
    @SuppressWarnings("unused")
    public float getY() {
        return this.y;
    }

    /**
     * Get the Gravity sensor Z-axis value
     *
     * @return Z-axis value
     */
    @SuppressWarnings("unused")
    public float getZ() {
        return this.z;
    }

}
