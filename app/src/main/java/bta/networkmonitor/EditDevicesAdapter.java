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

public class EditDevicesAdapter extends ArrayAdapter {

    private Context context;
    private ArrayList<DeviceObject> nDevices;
    private LayoutInflater layoutInflater;

    EditDevicesAdapter(@NonNull Context context, int resource, ArrayList<DeviceObject> nDevices) {
        super(context, resource);
        this.context = context;
        this.nDevices = nDevices;
    }

    @Override
    public int getCount() {
        if (nDevices.size() > 0) return nDevices.size();
        else return 0;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        try {
            ViewHolder holder = new ViewHolder();
            layoutInflater = LayoutInflater.from(context);
            if (convertView != null)
                holder = (ViewHolder) convertView.getTag();
            else {
                convertView = layoutInflater.inflate(R.layout.edit_devices_row, parent, false);
                holder.ipTV = convertView.findViewById(R.id.editDeviceIpTextView);
                holder.macTV = convertView.findViewById(R.id.editDeviceMacTextViev);
                holder.labelTV = convertView.findViewById(R.id.editDeviceLabelTextView);
                holder.ipTV.setText(nDevices.get(position).ip);
                holder.macTV.setText(nDevices.get(position).mac);
                holder.labelTV.setText(nDevices.get(position).label);
                convertView.setTag(holder);
            }
            notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return convertView;
    }

    class ViewHolder {
        TextView ipTV;
        TextView labelTV;
        TextView macTV;
    }
}
