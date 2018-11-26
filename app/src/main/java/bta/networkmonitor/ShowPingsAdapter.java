package bta.networkmonitor;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.util.ArrayList;

import static android.content.Context.NOTIFICATION_SERVICE;
import static bta.networkmonitor.ShowPingsActivity.IsOnSameLan;

@SuppressWarnings("All")
class ShowPingsAdapter extends ArrayAdapter {
    ArrayList<DeviceObject> devices;
    private Context context;
    private LayoutInflater inflater;

    public ShowPingsAdapter(@NonNull Context context, int resource, ArrayList<DeviceObject> devices) {
        super(context, resource);
        this.context = context;
        this.devices = devices;
    }

    @Override
    public int getCount() {
        return devices.size();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View v, @NonNull ViewGroup parent) {
        try {
            ViewHolder h = new ViewHolder();
            inflater = LayoutInflater.from(context);
            if (v != null) {
                h = (ViewHolder) v.getTag();
            } else {
                v = inflater.inflate(R.layout.show_pings_single_row_layout, parent, false);
                h.statusIV = v.findViewById(R.id.statusIV);
                h.chart = v.findViewById(R.id.chart);
                h.infoTV = v.findViewById(R.id.deviceInfoTextView);
                v.setTag(h);
            }

            if (devices.get(position) != null) {
                h.infoTV.setText(devices.get(position).ip + "\n" + devices.get(position).mac + "\n" + devices.get(position).label);
                if (!IsOnSameLan(context, devices.get(position).ip)) {
                    setDeviceStatus(h, 'w');
                } else if (devices.get(position).isRed) {
                    setDeviceStatus(h, 'd');
                    if (devices.get(position).notified == false) {
                        setNotification(true, devices.get(position));
                        MediaPlayer mp = MediaPlayer.create(context, R.raw.long_beep);
                        mp.start();
                        devices.get(position).notified = true;
                    }
                } else {
                    setDeviceStatus(h, 'n');
                    setNotification(false, devices.get(position));
                }
            }
            setChart(h.chart, devices.get(position));
            notifyDataSetChanged();
            h.chart.invalidate();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return v;
    }

    private void setChart(LineChart ch, DeviceObject h) {
        try {
            LineDataSet lineDataSet = new LineDataSet(h.entries, "");
            LineData lineData = new LineData(lineDataSet);
            if (h.entries.isEmpty())
                ch.clear();
            else
                ch.setData(lineData);
            Description desc = new Description();
            desc.setText("");
            ch.setDoubleTapToZoomEnabled(false);
            ch.setDescription(desc);
            ch.setVisibleYRange(0, 501, null);
            ch.setScaleYEnabled(false);
            ch.setDragEnabled(false);
            ch.setTouchEnabled(false);
            XAxis xAxis = ch.getXAxis();
            xAxis.setValueFormatter(new XAxisVF(h.times));
            xAxis.setGranularity(1f);
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            ch.moveViewTo(h.entries.get(h.entries.size() - 1).getX(), h.entries.get(h.entries.size() - 1).getY(), null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setDeviceStatus(ViewHolder h, char status) {
        switch (status) {
            case 'n': //'n' -> normal
                h.infoTV.setTextColor(Color.GREEN);
                h.statusIV.setVisibility(View.INVISIBLE);
                break;
            case 'd': //'d' -> disconnected
                h.infoTV.setTextColor(Color.RED);
                h.statusIV.setImageResource(R.drawable.disconnect);
                h.statusIV.setVisibility(View.VISIBLE);
                break;
            case 'w': //'w' -> wi-fi
                h.infoTV.setTextColor(Color.BLACK);
                h.statusIV.setImageResource(R.drawable.wifi);
                h.statusIV.setVisibility(View.VISIBLE);
                break;
        }
    }

    public void setNotification(boolean showNotification, DeviceObject deviceObject) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        // The code below will turn ip of device's last three group of numbers into notification id of the device.
        // For example if device ip is 192.168.13.12, then notification id set for the device will be 1681312
        String[] splittedIP = deviceObject.ip.split("\\.");
        int notificationId = Integer.parseInt(splittedIP[1] + splittedIP[2] + splittedIP[3]);
        if (showNotification) {
            Toast.makeText(context, "Connection lost with device: " + deviceObject.label + " - " + deviceObject.ip,
                    Toast.LENGTH_SHORT).show();
            PendingIntent pi = PendingIntent.getActivity(context, notificationId, new Intent(context, ShowPingsActivity.class), 0);
            Notification notification = new NotificationCompat.Builder(context)
                    .setSmallIcon(R.drawable.logo)
                    .setContentTitle("Connection lost")
                    .setContentText("Connection lost with device: " + deviceObject.label + " - " + deviceObject.ip)
                    .setContentIntent(pi)
                    .build();
            notificationManager.notify(notificationId, notification);
            Handler h = new Handler();
        } else {
            notificationManager.cancel(notificationId);
        }

        /*The code snippet below will make the notification removed after 10 seconds.

        long delayInMilliseconds = 10000;
        h.postDelayed(new Runnable() {
            public void run() {
                notificationManager.cancel(id);
            }
        }, delayInMilliseconds);
        */
    }

    class ViewHolder {
        TextView infoTV;
        LineChart chart;
        ImageView statusIV;
    }

    public class XAxisVF implements IAxisValueFormatter {
        private String[] mValues;

        public XAxisVF(ArrayList<String> times) {
            try {
                this.mValues = times.toArray(new String[0]);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public String getFormattedValue(float value, AxisBase axis) {
            // "value" represents the position of the label on the axis (x or y)
            return mValues[(int) value];
        }
    }
}
