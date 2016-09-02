package io.amar.databoxsource;

import android.app.Activity;
import android.graphics.Typeface;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.format.Formatter;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;

import org.sensingkit.sensingkitlib.*;
import org.sensingkit.sensingkitlib.data.SKLightData;
import org.sensingkit.sensingkitlib.data.SKSensorData;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

/**
 * Created by amar on 30/08/16.
 */
public class MainActivity extends Activity {

    SensingKitLibInterface mSensingKitLib;
    Map<String, SKSensorType> sensors = new HashMap<>();

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.log);

        TextView log = (TextView) findViewById(R.id.log);
        log.setMovementMethod(new ScrollingMovementMethod());
        log.setTypeface(Typeface.MONOSPACE);

        log("Databox Mobile Source");
        log("=====================");
        log();
        log("NB: All available sensors will be enabled and can stream to your Databox on demand at maximum fidelity. This app is still a WIP, and a newer version will have a UI that gives you more control.");
        log();
        log("-----------------------------------------");
        log();

        log("Registering available sensors...");

        try {
            mSensingKitLib = SensingKitLib.getSensingKitLib(this);

            for (SKSensorType sensorType : SKSensorType.values()) {
                try {
                    mSensingKitLib.registerSensor(sensorType);
                } catch (Exception e1) {
                    // permissions, that Eddystone thing.
                }
                if (mSensingKitLib.isSensorRegistered(sensorType)) {
                    log("Sensor registered: " + sensorType.toString().toLowerCase().replace('_', '-'));
                    sensors.put(sensorType.toString().toLowerCase().replace('_', '-'), sensorType);
                }
            }
        } catch (SKException e) {
            e.printStackTrace();
        }

        log("Sensors registered!");
        log();
        log("-----------------------------------------");
        log();

        log("Your local IP is " + getIPAddress(true));
        log("Configure your Databox mobile driver to use that IP as a source.");
        log("If you're not on the same LAN/VPN as your Databox, or otherwise behind a NAT, forward port 8080 and use your external IP.");

        //log();
        //log("-----------------------------------------");
        //log();

        new NanoHTTPD(8080) {
            {
                try {
                    start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
                } catch (IOException e) {
                    e.printStackTrace();
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
                if (path.length < 1 || path[0].length() < 1) {
                    String res = "<html><head><title>Databox Mobile Source</title></head><body>";
                    res += "<h1>Databox Mobile Source</h1>";
                    res += "<h2>Available Sensors</h2>";
                    res += "<ul>";
                    for (String sensor : sensors.keySet())
                        res += "<li>" + sensor + "</li>";
                    res += "</ul></body></html>\n";
                    return newFixedLengthResponse(res);
                }
                SKSensorType sensor = sensors.get(path[0]);
                if (sensor == null)
                    return newFixedLengthResponse(Response.Status.NOT_FOUND, NanoHTTPD.MIME_PLAINTEXT, "Sensor \"" + path[0] + "\" Not Found"); // TODO: Take name out

                PipedInputStream in = new PipedInputStream();
                final PipedOutputStream out;
                PipedOutputStream _out;
                try {
                    _out = new PipedOutputStream(in);
                } catch (IOException e) {
                    _out = null;
                    e.printStackTrace();
                }
                out = _out;

                final String sensorName = path[0];
                final String ip = session.getHeaders().get("http-client-ip");

                try {
                    MainActivity.this.mSensingKitLib.subscribeSensorDataListener(sensor,
                            new SKSensorDataListener() {
                                @Override
                                public void onDataReceived(final SKSensorType sensorType, final SKSensorData sensorData) {
                                    try {
                                        out.write((sensorData.getDataInCSV() + "\n").getBytes(Charset.forName("UTF-8")));
                                    } catch (Exception e) {
                                        try {
                                            MainActivity.this.mSensingKitLib.unsubscribeSensorDataListener(sensorType, this);
                                        } catch (SKException e1) {
                                            e1.printStackTrace();
                                        }
                                        try {
                                            MainActivity.this.mSensingKitLib.stopContinuousSensingWithSensor(sensorType);
                                        } catch (SKException e1) {
                                            e1.printStackTrace();
                                        }
                                        try {
                                            out.close();
                                        } catch (IOException e1) {
                                            e1.printStackTrace();
                                        }
                                        //log("No longer streaming " + sensorName + " data to " + ip + ".");
                                    }
                                }
                            });
                    if (!mSensingKitLib.isSensorRegistered(sensor))
                        mSensingKitLib.registerSensor(sensor);
                    if (!mSensingKitLib.isSensorSensing(sensor))
                        mSensingKitLib.startContinuousSensingWithSensor(sensor);

                    //log("Now streaming " + sensorName + " data to " + ip + ".");
                } catch (SKException e) {
                    e.printStackTrace();
                }

                return newChunkedResponse(Response.Status.OK, MIME_PLAINTEXT, in);
            }
        };
    }

    private void log(String msg) {
        TextView log = (TextView) findViewById(R.id.log);
        log.append(msg + "\n");
    }

    private void log() {
        log("");
    }

    /**
     * Get IP address from first non-localhost interface
     * Source: http://stackoverflow.com/questions/6064510/how-to-get-ip-address-of-the-device
     */
    public static String getIPAddress(boolean useIPv4) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress();
                        //boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
                        boolean isIPv4 = sAddr.indexOf(':')<0;

                        if (useIPv4) {
                            if (isIPv4)
                                return sAddr;
                        } else {
                            if (!isIPv4) {
                                int delim = sAddr.indexOf('%'); // drop ip6 zone suffix
                                return delim<0 ? sAddr.toUpperCase() : sAddr.substring(0, delim).toUpperCase();
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) { } // for now eat exceptions
        return "";
    }
}
