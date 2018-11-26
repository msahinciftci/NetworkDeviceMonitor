package bta.networkmonitor;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Xml;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.github.mikephil.charting.data.Entry;
import com.stealthcopter.networktools.MACTools;
import com.stealthcopter.networktools.WakeOnLan;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.net.NetworkInterface;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class ShowPingsActivity extends AppCompatActivity {
    public static int pingCount = 6;
    ListView showPingLV;
    ArrayList<DeviceObject> devices;
    Button newDeviceButton;
    Button wakeButton;
    ShowPingsAdapter adapter;
    Context c = ShowPingsActivity.this;

    public static String macAddressFormatter(String macAddress) {
        StringBuilder mac = new StringBuilder();
        try {
            if (macAddress.length() == 17)
                mac = new StringBuilder(macAddress.replace("-", ":"));
            else if (macAddress.length() == 12) {
                String[] macArray = splitMacAddress(macAddress, 2);
                for (int i = 0; i < macArray.length; i++) {
                    if (i != macArray.length - 1)
                        mac.append(macArray[i]).append(":");
                    else mac.append(macArray[i]);
                }
            }
            mac = new StringBuilder(mac.toString().toUpperCase());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mac.toString();
    }

    public static String[] splitMacAddress(String macAddress, int interval) {
        int arrayLength = (int) Math.ceil(((macAddress.length() / (double) interval)));
        String[] result = new String[arrayLength];
        int j = 0;
        int lastIndex = result.length - 1;
        for (int i = 0; i < lastIndex; i++) {
            result[i] = macAddress.substring(j, j + interval);
            j += interval;
        } //Add the last bit
        result[lastIndex] = macAddress.substring(j);

        return result;
    }

    public static String GetPhoneIpAddress(Context context) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(WIFI_SERVICE);
        assert wifiManager != null;
        int ipAddress = wifiManager.getConnectionInfo().getIpAddress();
        return String.format("%d.%d.%d.%d", (ipAddress & 0xff), (ipAddress >> 8 & 0xff),
                (ipAddress >> 16 & 0xff), (ipAddress >> 24 & 0xff));
    }

    public static boolean IsOnSameLan(Context context, String ip) {
        String[] deviceIPArray = GetPhoneIpAddress(context).split("\\.");
        String[] ipArray = ip.split("\\.");

        return (deviceIPArray[0].equals(ipArray[0]) && deviceIPArray[1].equals(ipArray[1])) || HasVPN();
    }

    private static boolean HasVPN() {
        List<String> networkList = new ArrayList<>();
        try {
            for (NetworkInterface networkInterface : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                if (networkInterface.isUp())
                    networkList.add(networkInterface.getName());
            }
        } catch (Exception e) {
            e.getMessage();
            e.printStackTrace();
        }
        return networkList.contains("tun0");
    }

    public static ArrayList<DeviceObject> LoadDevices(Context context) {
        ArrayList<DeviceObject> deviceObjects = new ArrayList<>();
        try {
            File path = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/NetworkMonitor/");
            if (!path.exists()) {
                path.mkdir();
            }
            File file = new File(path + "/devices.xml");
            if (!file.exists()) {
                file.createNewFile();
                return deviceObjects;
            }
            deviceObjects = new ArrayList<>();
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(file);

            NodeList deviceList = doc.getElementsByTagName("Device");
            for (int deviceNo = 0; deviceNo < deviceList.getLength(); deviceNo++) {
                String ip = ((Element) deviceList.item(deviceNo)).getElementsByTagName("Ip").item(0).getTextContent();
                String mac = ((Element) deviceList.item(deviceNo)).getElementsByTagName("Mac").item(0).getTextContent();
                String label = ((Element) deviceList.item(deviceNo)).getElementsByTagName("Label").item(0).getTextContent();
                deviceObjects.add(new DeviceObject(context, ip, mac, label));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Collections.sort(deviceObjects);
        return deviceObjects;
    }

    public static void SaveDevices(ArrayList<DeviceObject> hl) {
        File path = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/NetworkMonitor/");
        if (!path.exists()) {
            path.mkdir();
        }
        File file = new File(path + "/devices.xml");
        StringWriter writer = new StringWriter();
        try {
            file.createNewFile();

            FileOutputStream fileos = new FileOutputStream(file);
            XmlSerializer xmlSerializer = Xml.newSerializer();
            xmlSerializer.setOutput(writer);

            xmlSerializer.startDocument("UTF-8", true);
            xmlSerializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);

            xmlSerializer.startTag("", "Devices");

            for (int i = 0; i < hl.size(); i++) {
                xmlSerializer.startTag("", "Device");
                xmlSerializer.startTag("", "Ip");
                xmlSerializer.text(hl.get(i).ip);
                xmlSerializer.endTag("", "Ip");
                xmlSerializer.startTag("", "Mac");
                xmlSerializer.text(hl.get(i).mac);
                xmlSerializer.endTag("", "Mac");
                xmlSerializer.startTag("", "Label");
                xmlSerializer.text(hl.get(i).label);
                xmlSerializer.endTag("", "Label");
                xmlSerializer.endTag("", "Device");
            }
            xmlSerializer.endTag("", "Devices");
            xmlSerializer.endDocument();
            fileos.write(writer.toString().getBytes());
            fileos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_pings);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy);

        devices = new ArrayList<>();
        showPingLV = findViewById(R.id.pingListView);
        newDeviceButton = findViewById(R.id.newDeviceButton);
        wakeButton = findViewById(R.id.wakeBtn);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(this,
                READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{READ_EXTERNAL_STORAGE}, 571);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(this,
                WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{WRITE_EXTERNAL_STORAGE}, 572);
        }
        if (LoadDevices(this) != null)
            if (!LoadDevices(this).isEmpty())
                devices = LoadDevices(this);
        adapter = new ShowPingsAdapter(c, R.layout.show_pings_single_row_layout, devices);
        showPingLV.setAdapter(adapter);
        startMonitor();

        newDeviceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), EditDevicesActivity.class);
                if (devices.size() > 0)
                    SaveDevices(devices);
                startActivity(i);
            }
        });
        wakeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Dialog d = new Dialog(c);
                d.requestWindowFeature(Window.FEATURE_NO_TITLE);
                d.setCancelable(true);
                d.setContentView(R.layout.wake_on_lan_dialog_layout);
                d.show();
                Button wakeBtnDialog = d.findViewById(R.id.wakeButtonDialog);
                final EditText et = d.findViewById(R.id.wakeEditText);
                wakeBtnDialog.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        try {
                            final String macAddress = macAddressFormatter(et.getText().toString().trim());
                            if (MACTools.isValidMACAddress(macAddress)) {
                                WakeOnLan.sendWakeOnLan("255.255.255.255", macAddress);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(c, "Request sent to : " + macAddress, Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } else
                                Toast.makeText(c, "Invalid mac address", Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            Toast.makeText(c, e.getMessage(), Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }

    private void setAlertDialog(final AlertDialog dialog, final boolean show) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (show && !dialog.isShowing())
                    dialog.show();
                else if (!show && dialog.isShowing())
                    dialog.dismiss();
            }
        });
    }

    private void startMonitor() {
        try {
            final AlertDialog alertDialog = new AlertDialog.Builder(c).create();
            alertDialog.setTitle("No Connection");
            alertDialog.setMessage("You're not connected on a network.");
            alertDialog.setCancelable(false);
            if (devices.isEmpty())
                if (LoadDevices(c) != null)
                    if (!LoadDevices(c).isEmpty())
                        devices = LoadDevices(c);
            final long period = 5000;
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    if (!GetPhoneIpAddress(c).equals("0.0.0.0")) {
                        setAlertDialog(alertDialog, false);
                        for (int i = 0; i < devices.size(); i++)
                            if (devices.get(i) != null)
                                if (IsOnSameLan(c, devices.get(i).ip))
                                    devices.get(i).startPinging(getCurrentTime());
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (showPingLV.getAdapter() != null) {
                                    Collections.sort(devices);
                                    ((ShowPingsAdapter) showPingLV.getAdapter()).notifyDataSetChanged();
                                }
                            }
                        });
                    } else setAlertDialog(alertDialog, true);
                }
            }, 0, period);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        return sdf.format(new Date());
    }

    public void deleteDevice(View v) {
        final int position = showPingLV.getPositionForView((View) v.getParent());
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Delete Device");
        alert.setMessage(devices.get(position).ip + " will be deleted. Do you accept?");
        alert.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // continue with delete
                dialog.cancel();
            }
        });

        alert.setNegativeButton("Delete", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // close dialog
                devices.remove(position);
                SaveDevices(devices);
            }
        });
        alert.show();
    }

    public void wakeDevice(View v) {
        final int position = showPingLV.getPositionForView((View) v.getParent());
        if (devices.get(position).isRed) {
            final String mac = devices.get(position).mac;
            AlertDialog.Builder alert = new AlertDialog.Builder(c);
            alert.setTitle("Send Wake-On-Lan");
            alert.setMessage("Do you want to send wake request to " + devices.get(position).label + " with " +
                    devices.get(position).mac + " address?");
            alert.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            alert.setNegativeButton("Send", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    try {
                        WakeOnLan.sendWakeOnLan("255.255.255.255", mac);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(c, "Request sent.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            alert.show();
        }
    }

    public void editDevice(View v) {
        final int position = showPingLV.getPositionForView((View) v.getParent());
        final Dialog dialog = new Dialog(c);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.add_device_dialog_layout);
        Button dialogAcceptBtn = dialog.findViewById(R.id.dialogAcceptButton);
        final EditText ipET = dialog.findViewById(R.id.dialogIpEditText);
        final EditText labelET = dialog.findViewById(R.id.dialogLabelEditText);
        final EditText macET = dialog.findViewById(R.id.dialogMacEditText);
        ipET.setText(devices.get(position).ip);
        macET.setText(devices.get(position).mac);
        labelET.setText(devices.get(position).label);
        dialog.show();
        dialogAcceptBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String lab = labelET.getText().toString().trim();
                String ip = ipET.getText().toString().replace(" ", "");
                String mac = macAddressFormatter(macET.getText().toString().replace(" ", ""));
                if (!lab.equals(""))
                    devices.get(position).label = lab;
                if (!ip.equals("") && !mac.equals("")) {
                    if (!ip.equals(devices.get(position).ip)) { //Has ip changed?
                        if (checkIfDeviceExists('i', position, ip, mac)) { // Yes. Is it duplicate?
                            Toast.makeText(c, "Device with " + ip + " ip address already exists!", Toast.LENGTH_SHORT).show();
                        } else { // No, it's not duplicate. Then check the mac address
                            if (!mac.equals(devices.get(position).mac)) { //Has mac changed?
                                if (checkIfDeviceExists('m', position, ip, mac)) //Yes. Is it duplicate?
                                    Toast.makeText(c, "Device with " + mac + " mac address already exists!", Toast.LENGTH_SHORT).show();
                                else { // No. Then save it.
                                    devices.get(position).ip = ip;
                                    devices.get(position).mac = mac;
                                    for (int i = 0; i < pingCount; i++)
                                        if (devices.get(position) != null)
                                            devices.get(position).entries.add(new Entry(i, 0));
                                    Collections.sort(devices);
                                    SaveDevices(devices);
                                    ((ShowPingsAdapter) showPingLV.getAdapter()).notifyDataSetChanged();
                                    dialog.dismiss();
                                }
                            } else { // Mac address didn't change.
                                devices.get(position).ip = ip;
                                devices.get(position).mac = mac;
                                for (int i = 0; i < pingCount; i++)
                                    if (devices.get(position) != null)
                                        devices.get(position).entries.add(new Entry(i, 0));
                                Collections.sort(devices);
                                SaveDevices(devices);
                                ((ShowPingsAdapter) showPingLV.getAdapter()).notifyDataSetChanged();
                                dialog.dismiss();
                            }
                        }
                    } else { // Ip hasn't changed. Check for mac
                        if (!mac.equals(devices.get(position).mac)) { //Has mac changed?
                            if (checkIfDeviceExists('m', position, ip, mac)) //Yes. Does it exist?
                                Toast.makeText(c, "Device with " + mac + " mac address already exists!", Toast.LENGTH_SHORT).show();
                            else { // No. Then save it.
                                devices.get(position).mac = mac;
                                Collections.sort(devices);
                                SaveDevices(devices);
                                ((ShowPingsAdapter) showPingLV.getAdapter()).notifyDataSetChanged();
                                dialog.dismiss();
                            }
                        } else { // Ip and mac haven't changed. Close dialog.
                            dialog.dismiss();
                        }
                    }
                } else
                    Toast.makeText(c, "Required fields cannot be empty.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean checkIfDeviceExists(char type, int position, String ip, String mac) {
        switch (type) {
            case 'i':
                for (int i = 0; i < devices.size(); i++)
                    if (devices.get(i).ip.equals(ip) && i != position)
                        return true;
            case 'm':
                for (int i = 0; i < devices.size(); i++)
                    if (devices.get(i).mac.equals(mac) && i != position)
                        return true;
            case 'b':
                for (int i = 0; i < devices.size(); i++)
                    if ((devices.get(i).mac.equals(ip) || devices.get(i).ip.equals(mac)) && i != position)
                        return true;
        }
        return false;
    }
}

