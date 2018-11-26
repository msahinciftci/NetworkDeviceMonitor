package bta.networkmonitor;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import com.github.mikephil.charting.data.Entry;
import com.stealthcopter.networktools.Ping;
import com.stealthcopter.networktools.ping.PingResult;

import java.util.ArrayList;

import static bta.networkmonitor.ShowPingsActivity.IsOnSameLan;
import static bta.networkmonitor.ShowPingsActivity.pingCount;

class DeviceObject implements Comparable<DeviceObject> {
    private Context context;
    String label;
    String ip;
    String mac;
    ArrayList<Entry> entries;
    ArrayList<String> times = new ArrayList<>();
    private int timeOutCount;
    boolean isRed;
    boolean notified = false;

    DeviceObject(Context context, String ip, String mac, String label) {
        this.context = context;
        this.label = label;
        this.ip = ip;
        this.mac = mac;
        this.entries = new ArrayList<>();
        for (int i = 0; i < pingCount; i++) {
            entries.add(new Entry(i, 0));
            times.add("00:00:00");
        }
    }

    private void addEntry(int ms, String time) {
        try {
            if (entries.size() > 0 && entries.size() < pingCount) {
                entries.add(new Entry(entries.size(), ms));
                addTime(time);
            } else {
                entries.remove(0);
                for (int i = 0; i < entries.size(); i++)
                    if (entries.get(i) != null) {
                        entries.get(i).setX(i);
                    }
                entries.add(new Entry(entries.size(), ms));
                addTime(time);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("Error ", e.getMessage());
        }
    }

    private void addTime(String time) {
        try {
            if (times.size() - 1 > pingCount) {
                for (int i = 0; i < times.size() - pingCount - 1; i++)
                    times.remove(0);
            }
            times.remove(0);
            times.add(time);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void startPinging(String time) {
        new SendPing(time).execute();
    }

    @Override
    public int compareTo(@NonNull DeviceObject deviceObject) {
        String[] firstIP = ip.split("\\.");
        String[] secondIP = deviceObject.ip.split("\\.");
        if (IsOnSameLan(context, ip) && IsOnSameLan(context, deviceObject.ip)) {
            for (int i = 0; i < 4; i++) {
                int result = compareThem(Integer.parseInt(firstIP[i]), Integer.parseInt(secondIP[i]));
                if (result != 0)
                    return result;
            }
            return 0;
        } else if (!IsOnSameLan(context, ip) && !IsOnSameLan(context, deviceObject.ip)) {
            for (int i = 0; i < 4; i++) {
                int result = compareThem(Integer.parseInt(firstIP[i]), Integer.parseInt(secondIP[i]));
                if (result != 0)
                    return result;
            }
            return 1;
        } else if (!IsOnSameLan(context, ip) && IsOnSameLan(context, deviceObject.ip)) {
            return 1;
        } else return -1;
    }

    private int compareThem(int i1, int i2) {
        if (i1 < i2)
            return -1;
        else if (i1 > i2)
            return 1;
        else {
            return 0;
        }
    }

    private class SendPing extends AsyncTask<Void, Void, Void> {
        String time;

        SendPing(String time) {
            this.time = time;
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                PingResult pingResult = Ping.onAddress(ip).setTimeOutMillis(1000).doPing();
                if (pingResult != null && pingResult.fullString != null) {
                    String result = pingResult.fullString.split("time=")[1].split(" ms")[0].split("\\.")[0];
                    if (!result.isEmpty()) {
                        addEntry(Integer.parseInt(result), time);
                        timeOutCount = 0;
                    } else {
                        addEntry(0, time);
                        timeOutCount++;
                    }
                } else {
                    addEntry(0, time);
                    timeOutCount++;
                }
            } catch (Exception e) {
                e.printStackTrace();
                addEntry(0, time);
                timeOutCount++;
            }
            if (timeOutCount > 9) {
                isRed = true;
            } else {
                isRed = false;
                notified = false;
            }
            return null;
        }
    }

}