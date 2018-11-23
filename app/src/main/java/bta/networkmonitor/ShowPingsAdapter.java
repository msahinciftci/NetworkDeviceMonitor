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
    ArrayList<HostObject> hosts;
    private Context context;
    private LayoutInflater inflater;

    public ShowPingsAdapter(@NonNull Context context, int resource, ArrayList<HostObject> hosts) {
        super(context, resource);
        this.context = context;
        this.hosts = hosts;
    }

    @Override
    public int getCount() {
        return hosts.size();
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
                v = inflater.inflate(R.layout.show_pings_row, parent, false);
                h.statusIV = v.findViewById(R.id.statusIV);
                h.chart = v.findViewById(R.id.chart);
                h.infoTV = v.findViewById(R.id.hostInfoTextView);
                v.setTag(h);
            }

            if (hosts.get(position) != null) {
                h.infoTV.setText(hosts.get(position).ip + "\n" + hosts.get(position).mac + "\n" + hosts.get(position).label);
                if (!IsOnSameLan(context, hosts.get(position).ip))
                    setHostStatus(h, 'w');
                else if (hosts.get(position).isRed) {
                    setHostStatus(h, 'd');
                    if (hosts.get(position).notified == false) {
                        showNotification(hosts.get(position));
                        MediaPlayer mp = MediaPlayer.create(context, R.raw.long_beep);
                        mp.start();
                        hosts.get(position).notified = true;
                    }
                } else {
                    setHostStatus(h, 'n');
                }
            }
            setChart(h.chart, hosts.get(position));
            notifyDataSetChanged();
            h.chart.invalidate();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return v;
    }

    private void setChart(LineChart ch, HostObject h) {
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

    private void setHostStatus(ViewHolder h, char status) {
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

    public void showNotification(HostObject hostObject) {
        String[] splitted = hostObject.ip.split("\\.");
        Toast.makeText(context, "Connection lost with device: " + hostObject.label + " - " + hostObject.ip,
                Toast.LENGTH_SHORT).show();
        final int id = Integer.parseInt(splitted[splitted.length - 1]);
        PendingIntent pi = PendingIntent.getActivity(context, id, new Intent(context, ShowPingsActivity.class), 0);
        Notification notification = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.logo)
                .setContentTitle("Connection lost")
                .setContentText("Connection lost with device: " + hostObject.label + " - " + hostObject.ip)
                .setContentIntent(pi)
                .build();

        final NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(id, notification);
        Handler h = new Handler();
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
