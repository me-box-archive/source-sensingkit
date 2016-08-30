package io.amar.databoxsource;

import android.app.Activity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;

import org.sensingkit.sensingkitlib.*;
import org.sensingkit.sensingkitlib.data.SKLightData;
import org.sensingkit.sensingkitlib.data.SKSensorData;

import java.io.IOException;

import fi.iki.elonen.NanoHTTPD;

/**
 * Created by amar on 30/08/16.
 */
public class MainActivity extends Activity {

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.log);

        TextView log = (TextView) findViewById(R.id.log);
        log.setMovementMethod(new ScrollingMovementMethod());

        try {
            SensingKitLibInterface mSensingKitLib = SensingKitLib.getSensingKitLib(this);
            mSensingKitLib.registerSensor(SKSensorType.LIGHT);
            mSensingKitLib.subscribeSensorDataListener(SKSensorType.LIGHT,
                    new SKSensorDataListener() {
                        @Override
                        public void onDataReceived(final SKSensorType moduleType, final SKSensorData sensorData) {
                            // We have data!
                            log(Float.toString(((SKLightData) sensorData).getLight()));
                        }
                    });
            mSensingKitLib.startContinuousSensingWithSensor(SKSensorType.LIGHT);
        } catch (SKException e) {
        }

        new NanoHTTPD(8080) {
            {
                try {
                    start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public Response serve(IHTTPSession session) {
                return newFixedLengthResponse("<html><body><h1>Hello world!</h1></body></html>\n");
            }
        };
    }

    private void log(String msg) {
        TextView log = (TextView) findViewById(R.id.log);
        log.append(msg + "\n");
    }
}
