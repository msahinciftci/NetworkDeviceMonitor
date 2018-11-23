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

import static bta.networkmonitor.ShowPingsActivity.macAddressFormatter;
import java.util.ArrayList;
import java.util.Collections;

@SuppressWarnings("All")
public class EditHostsActivity extends AppCompatActivity {

    Button addHostButton, viewCountButton;
    EditHostsAdapter adapter;
    EditText viewCountET;
    ListView addHostsLV;
    int count;
    ArrayList<HostObject> nHosts;
    ArrayList<HostObject> hosts;
    Context c = EditHostsActivity.this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_hosts);
        addHostButton = findViewById(R.id.addHostButton);
        addHostsLV = findViewById(R.id.addHostLV);
        viewCountButton = findViewById(R.id.viewCountBtn);
        viewCountET = findViewById(R.id.viewCountET);
        if (nHosts == null)
            nHosts = new ArrayList<>();
        setButtonsState(false);
        hosts = ShowPingsActivity.LoadHosts(c);
        viewCountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!viewCountET.getText().toString().replace(" ", "").isEmpty()) {
                    count = Integer.parseInt(viewCountET.getText().toString().replace(" ", ""));
                    if (nHosts.isEmpty())
                        for (int i = 0; i < count; i++)
                            nHosts.add(new HostObject(c, "", "", ""));
                    adapter = new EditHostsAdapter(c, R.layout.edit_hosts_row, nHosts);
                    addHostsLV.setAdapter(adapter);
                    setButtonsState(true);
                }
            }
        });

        addHostsLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                final Dialog dialog = new Dialog(c);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setCancelable(true);
                dialog.setContentView(R.layout.host_dialog_layout);
                Button dialogAcceptBtn = dialog.findViewById(R.id.dialogAcceptButton);
                final EditText ipET = dialog.findViewById(R.id.dialogIpEditText);
                final EditText macET = dialog.findViewById(R.id.dialogMacEditText);
                final EditText labelET = dialog.findViewById(R.id.dialogLabelEditText);
                if (!nHosts.get(position).ip.equals(""))
                    ipET.setText(nHosts.get(position).ip);
                if (!nHosts.get(position).mac.equals(""))
                    macET.setText(nHosts.get(position).mac);
                if (!nHosts.get(position).label.equals(""))
                    labelET.setText(nHosts.get(position).label);
                dialog.show();
                final TextView ipTV = view.findViewById(R.id.editHostIpTextView);
                final TextView macTV = view.findViewById(R.id.editHostMacTextViev);
                final TextView labelTV = view.findViewById(R.id.editHostLabelTextView);
                dialogAcceptBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        nHosts.get(position).ip = ipET.getText().toString().replace(" ", "");
                        nHosts.get(position).mac = macAddressFormatter(macET.getText().toString().replace(" ", ""));
                        nHosts.get(position).label = labelET.getText().toString().trim();
                        ipTV.setText(nHosts.get(position).ip);
                        macTV.setText(nHosts.get(position).mac);
                        labelTV.setText(nHosts.get(position).label);
                        adapter.notifyDataSetChanged();
                        dialog.dismiss();
                    }
                });

            }
        });
        addHostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (count != 0) {
                    for (int i = 0; i < count; i++) {
                        try {
                            View ev = addHostsLV.getChildAt(i);
                            TextView ipTextView = ev.findViewById(R.id.editHostIpTextView);
                            TextView labelTextView = ev.findViewById(R.id.editHostLabelTextView);
                            TextView macTextView = ev.findViewById(R.id.editHostMacTextViev);
                            if (ipTextView != null && labelTextView != null) {
                                String ip = ipTextView.getText().toString().trim();
                                String mac = macTextView.getText().toString().trim();
                                String label = labelTextView.getText().toString().trim();
                                if (!ip.equals("") && !mac.equals("") && !deviceExists('b', ip, mac)) {
                                    hosts.add(new HostObject(c, ip, mac, label));
                                    Collections.sort(hosts);
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                ShowPingsActivity.SaveHosts(hosts);
                nHosts.clear();
                addHostsLV.deferNotifyDataSetChanged();
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
            addHostButton.setVisibility(View.INVISIBLE);
        } else {
            viewCountButton.setVisibility(View.INVISIBLE);
            viewCountET.setVisibility(View.INVISIBLE);
            addHostButton.setVisibility(View.VISIBLE);
        }
    }

    private boolean deviceExists(char type, String ip, String mac) {
        switch (type) {
            case 'i':
                for (int i = 0; i < hosts.size(); i++)
                    if (hosts.get(i).ip.equals(ip))
                        return true;
            case 'm':
                for (int i = 0; i < hosts.size(); i++)
                    if (hosts.get(i).mac.equals(mac))
                        return true;
            case 'b':
                for (int i = 0; i < hosts.size(); i++)
                    if (hosts.get(i).mac.equals(ip) || hosts.get(i).ip.equals(mac))
                        return true;
        }
        return false;
    }
}
