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

package org.sensingkit.sensingkitlib.sensors;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import org.sensingkit.sensingkitlib.SKException;
import org.sensingkit.sensingkitlib.SKExceptionErrorCode;
import org.sensingkit.sensingkitlib.SKSensorType;
import org.sensingkit.sensingkitlib.data.SKAbstractData;

public abstract class SKAbstractNativeSensor extends SKAbstractSensor {

    @SuppressWarnings("unused")
    private static final String TAG = "SKAbstractNativeSensor";

    private final SensorManager mSensorManager;
    private final Sensor mSensor;
    private final SensorEventListener mSensorEventListener;

    protected SKAbstractNativeSensor(final Context context, final SKSensorType sensorType) throws SKException {
        super(context, sensorType);

        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(getSensorType(sensorType));

        mSensorEventListener = new SensorEventListener() {

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
                // Ignore
            }

            @Override
            public void onSensorChanged(SensorEvent event) {

                // Build the data object
                SKAbstractData data = buildData(event);

                // Submit sensor data object
                submitSensorData(data);
            }
        };
    }

    @Override
    public void startSensing() throws SKException {

        this.isSensing = true;

        boolean status = mSensorManager.registerListener(mSensorEventListener, mSensor, SensorManager.SENSOR_DELAY_NORMAL);

        if (!status) {
            throw new SKException(TAG, "SensorModule '" + getSensorName() + "' could not be started.", SKExceptionErrorCode.UNKNOWN_ERROR);
        }
    }

    @Override
    public void stopSensing() {

        mSensorManager.unregisterListener(mSensorEventListener);

        this.isSensing = false;
    }

    protected abstract SKAbstractData buildData(SensorEvent event);

    @SuppressLint("InlinedApi")  // There is a check in SKSensorManager
    private static int getSensorType(SKSensorType sensorType) throws SKException{

        switch (sensorType) {

            case ACCELEROMETER:
                return Sensor.TYPE_ACCELEROMETER;

            case GRAVITY:
                return Sensor.TYPE_GRAVITY;

            case LINEAR_ACCELERATION:
                return Sensor.TYPE_LINEAR_ACCELERATION;

            case GYROSCOPE:
                return Sensor.TYPE_GYROSCOPE;

            case ROTATION:
                return Sensor.TYPE_ROTATION_VECTOR;

            case MAGNETOMETER:
                return Sensor.TYPE_MAGNETIC_FIELD;

            case AMBIENT_TEMPERATURE:
                return Sensor.TYPE_AMBIENT_TEMPERATURE;

            case STEP_DETECTOR:
                return Sensor.TYPE_STEP_DETECTOR;

            case STEP_COUNTER:
                return Sensor.TYPE_STEP_COUNTER;

            case LIGHT:
                return Sensor.TYPE_LIGHT;

            case HUMIDITY:
                return Sensor.TYPE_RELATIVE_HUMIDITY;

            case AIR_PRESSURE:
                return Sensor.TYPE_PRESSURE;

            default:
                throw new SKException(TAG, "Not a native SensorModule.", SKExceptionErrorCode.UNKNOWN_ERROR);

        }
    }

}