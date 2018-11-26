package bta.networkmonitor;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;

import static bta.networkmonitor.ShowPingsActivity.macAddressFormatter;

public class EditDevicesActivity extends AppCompatActivity {

    Button addDeviceButton, viewCountButton;
    EditDevicesAdapter adapter;
    EditText viewCountET;
    ListView addDevicesLV;
    int count;
    ArrayList<DeviceObject> nDevices;
    ArrayList<DeviceObject> devices;
    Context c = EditDevicesActivity.this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_devices);
        addDeviceButton = findViewById(R.id.addDeviceButton);
        addDevicesLV = findViewById(R.id.addDeviceLV);
        viewCountButton = findViewById(R.id.viewCountBtn);
        viewCountET = findViewById(R.id.viewCountET);
        if (nDevices == null)
            nDevices = new ArrayList<>();
        setButtonsState(false);
        devices = ShowPingsActivity.LoadDevices(c);
        viewCountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!viewCountET.getText().toString().replace(" ", "").isEmpty()) {
                    count = Integer.parseInt(viewCountET.getText().toString().replace(" ", ""));
                    if (nDevices.isEmpty())
                        for (int i = 0; i < count; i++)
                            nDevices.add(new DeviceObject(c, "", "", ""));
                    adapter = new EditDevicesAdapter(c, R.layout.edit_devices_row, nDevices);
                    addDevicesLV.setAdapter(adapter);
                    setButtonsState(true);
                }
            }
        });

        addDevicesLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                final Dialog dialog = new Dialog(c);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setCancelable(true);
                dialog.setContentView(R.layout.add_device_dialog_layout);
                Button dialogAcceptBtn = dialog.findViewById(R.id.dialogAcceptButton);
                final EditText ipET = dialog.findViewById(R.id.dialogIpEditText);
                final EditText macET = dialog.findViewById(R.id.dialogMacEditText);
                final EditText labelET = dialog.findViewById(R.id.dialogLabelEditText);
                if (!nDevices.get(position).ip.equals(""))
                    ipET.setText(nDevices.get(position).ip);
                if (!nDevices.get(position).mac.equals(""))
                    macET.setText(nDevices.get(position).mac);
                if (!nDevices.get(position).label.equals(""))
                    labelET.setText(nDevices.get(position).label);
                dialog.show();
                final TextView ipTV = view.findViewById(R.id.editDeviceIpTextView);
                final TextView macTV = view.findViewById(R.id.editDeviceMacTextViev);
                final TextView labelTV = view.findViewById(R.id.editDeviceLabelTextView);
                dialogAcceptBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        nDevices.get(position).ip = ipET.getText().toString().replace(" ", "");
                        nDevices.get(position).mac = macAddressFormatter(macET.getText().toString().replace(" ", ""));
                        nDevices.get(position).label = labelET.getText().toString().trim();
                        ipTV.setText(nDevices.get(position).ip);
                        macTV.setText(nDevices.get(position).mac);
                        labelTV.setText(nDevices.get(position).label);
                        adapter.notifyDataSetChanged();
                        dialog.dismiss();
                    }
                });

            }
        });
        addDeviceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (count != 0) {
                    for (int i = 0; i < count; i++) {
                        try {
                            View ev = addDevicesLV.getChildAt(i);
                            TextView ipTextView = ev.findViewById(R.id.editDeviceIpTextView);
                            TextView labelTextView = ev.findViewById(R.id.editDeviceLabelTextView);
                            TextView macTextView = ev.findViewById(R.id.editDeviceMacTextViev);
                            if (ipTextView != null && labelTextView != null) {
                                String ip = ipTextView.getText().toString().trim();
                                String mac = macTextView.getText().toString().trim();
                                String label = labelTextView.getText().toString().trim();
                                if (!ip.equals("") && !mac.equals("") && !deviceExists('b', ip, mac)) {
                                    devices.add(new DeviceObject(c, ip, mac, label));
                                    Collections.sort(devices);
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                ShowPingsActivity.SaveDevices(devices);
                nDevices.clear();
                addDevicesLV.deferNotifyDataSetChanged();
                setButtonsState(false);
                Intent i = new Intent(c, ShowPingsActivity.class);
                i.putExtra("Changed", true);
                startActivity(i);
            }
        });
    }

    private void setButtonsState(boolean isStarted) {
        if (!isStarted) {
            viewCountButton.setVisibility(View.VISIBLE);
            viewCountET.setVisibility(View.VISIBLE);
            addDeviceButton.setVisibility(View.INVISIBLE);
        } else {
            viewCountButton.setVisibility(View.INVISIBLE);
            viewCountET.setVisibility(View.INVISIBLE);
            addDeviceButton.setVisibility(View.VISIBLE);
        }
    }

    private boolean deviceExists(char type, String ip, String mac) {
        switch (type) {
            case 'i':
                for (int i = 0; i < devices.size(); i++)
                    if (devices.get(i).ip.equals(ip))
                        return true;
            case 'm':
                for (int i = 0; i < devices.size(); i++)
                    if (devices.get(i).mac.equals(mac))
                        return true;
            case 'b':
                for (int i = 0; i < devices.size(); i++)
                    if (devices.get(i).mac.equals(ip) || devices.get(i).ip.equals(mac))
                        return true;
        }
        return false;
    }
}
