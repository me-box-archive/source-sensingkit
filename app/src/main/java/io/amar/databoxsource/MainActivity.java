package io.amar.databoxsource;

import android.app.Activity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;

import org.sensingkit.sensingkitlib.*;
import org.sensingkit.sensingkitlib.data.SKLightData;
import org.sensingkit.sensingkitlib.data.SKSensorData;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

/**
 * Created by amar on 30/08/16.
 */
public class MainActivity extends Activity {

    SensingKitLibInterface mSensingKitLib;
    Map<String, SKSensorType> string2sensor = new HashMap<>();

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.log);

        TextView log = (TextView) findViewById(R.id.log);
        log.setMovementMethod(new ScrollingMovementMethod());

        try {
            mSensingKitLib = SensingKitLib.getSensingKitLib(this);

            for (SKSensorType sensorType : SKSensorType.values()) {
                mSensingKitLib.registerSensor(sensorType);
                mSensingKitLib.startContinuousSensingWithSensor(SKSensorType.LIGHT);
            }
        } catch (SKException e) {
        }


        new NanoHTTPD(8080) {
            {
                try {
                    start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Map<String, Runnable> handlers = new HashMap<>();

                for (SKSensorType sensorType : SKSensorType.values()) {
                    System.out.println(sensorType.toString().toLowerCase());
                    handlers.put(sensorType.toString().toLowerCase(), () -> {
                                try {
                                    MainActivity.this.mSensingKitLib.subscribeSensorDataListener(sensorType,
                                            (final SKSensorType moduleType, final SKSensorData sensorData) ->
                                                    sensorData.getDataInCSV()
                                    );
                                } catch (SKException e) {
                                }
                            }
                    );
                }
            }

            private String[] parseURI(String uri) {
                if (uri == null)
                    return new String[]{};
                if (uri.startsWith("/"))
                    uri = uri.substring(1);
                if (uri.endsWith("/"))
                    uri = uri.substring(0, uri.length() - 1);

                uri = uri.toLowerCase();
                return uri.split("/");
            }

            @Override
            public Response serve(IHTTPSession session) {
                String[] path = parseURI(session.getUri());
                InputStream inputStream = new ByteArrayInputStream("aaaaaaaa".getBytes(Charset.forName("UTF-8")));

                return newChunkedResponse(Response.Status.OK, "text/csv", inputStream);
            }
        };
    }

    private void log(String msg) {
        TextView log = (TextView) findViewById(R.id.log);
        log.append(msg + "\n");
    }
}
