package bta.networkmonitor;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

@SuppressWarnings("All")
public class EditHostsAdapter extends ArrayAdapter {

    Context context;
    ArrayList<HostObject> nHosts;
    LayoutInflater l;

    public EditHostsAdapter(@NonNull Context context, int resource, ArrayList<HostObject> nHosts) {
        super(context, resource);
        this.context = context;
        this.nHosts = nHosts;
    }

    @Override
    public int getCount() {
        if (nHosts.size() > 0) return nHosts.size();
        else return 0;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View v, @NonNull ViewGroup parent) {
        try {
            ViewHolder h = new ViewHolder();
            l = LayoutInflater.from(context);
            if (v != null)
                h = (ViewHolder) v.getTag();
            else {
                v = l.inflate(R.layout.edit_hosts_row, parent, false);
                h.ipTV = v.findViewById(R.id.editHostIpTextView);
                h.macTV = v.findViewById(R.id.editHostMacTextViev);
                h.labelTV = v.findViewById(R.id.editHostLabelTextView);
                h.ipTV.setText(nHosts.get(position).ip);
                h.macTV.setText(nHosts.get(position).mac);
                h.labelTV.setText(nHosts.get(position).label);
                v.setTag(h);
            }
            notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return v;
    }

    class ViewHolder {
        TextView ipTV;
        TextView labelTV;
        TextView macTV;
    }
}
