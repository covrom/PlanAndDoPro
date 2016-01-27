/*
 * Copyright (c) Roman Tsovanyan
 */

package pro.tsov.plananddopro;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.support.v4.content.WakefulBroadcastReceiver;

import java.util.Calendar;

public class TrackAlarmReceiver extends WakefulBroadcastReceiver {
    public static final String ACTION_SET_TODAY_ALARMS =
            "pro.tsov.plananddopro.ACTION_SET_TODAY_ALARMS";

    public static final String ACTION_SET_TODAY_COMPLETE =
            "pro.tsov.plananddopro.ACTION_SET_TODAY_COMPLETE";

    public static final String ACTION_SET_TODAY_CANCEL =
            "pro.tsov.plananddopro.ACTION_SET_TODAY_CANCEL";

    @Override
    public void onReceive(Context context, Intent intent) {
        //здесь критично время исполнения, поэтому, все делаем через запуск асинхронного сервиса
        Intent i = new Intent(context, TrackAlarmService.class);
        if (intent.getAction().equalsIgnoreCase(ACTION_SET_TODAY_COMPLETE)){
            i.setAction(ACTION_SET_TODAY_COMPLETE);
            i.putExtra(EditEventActivity.ACTION_EXTRA_EVENTID, intent.getLongExtra(EditEventActivity.ACTION_EXTRA_EVENTID, -1));
            i.putExtra(TrackWidget.ACTION_EXTRA_PUSHDATEISO,intent.getStringExtra(TrackWidget.ACTION_EXTRA_PUSHDATEISO));
        }
        else if (intent.getAction().equalsIgnoreCase(ACTION_SET_TODAY_CANCEL)){
            i.setAction(ACTION_SET_TODAY_CANCEL);
            i.putExtra(EditEventActivity.ACTION_EXTRA_EVENTID, intent.getLongExtra(EditEventActivity.ACTION_EXTRA_EVENTID, -1));
            i.putExtra(TrackWidget.ACTION_EXTRA_PUSHDATEISO,intent.getStringExtra(TrackWidget.ACTION_EXTRA_PUSHDATEISO));
        }
        else {
            i.setAction(ACTION_SET_TODAY_ALARMS);
        }
        startWakefulService(context, i);
        //context.startService(i);
    }

    //получаем интент для запуска ресивера
    public static PendingIntent peAction(Context ctx){
        //Intent intentToFire = new Intent(TrackAlarmReceiver.ACTION_SET_TODAY_ALARMS);
        Intent intentToFire = new Intent(ctx, TrackAlarmReceiver.class);
        intentToFire.setAction(ACTION_SET_TODAY_ALARMS);
        return PendingIntent.getBroadcast(ctx, 3846551, intentToFire, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    //оповещение на запуск сервиса
    public static void sendActionOverAlarm(Context ctx,long deltamillis, boolean alignToHour){
        AlarmManager alarmManager = (AlarmManager)ctx.getSystemService(Context.ALARM_SERVICE);
        if (alignToHour){
            Calendar rcal = Calendar.getInstance();
            rcal.setTimeInMillis(rcal.getTimeInMillis() + deltamillis);
            if (rcal.get(Calendar.MINUTE)>=30) rcal.add(Calendar.HOUR_OF_DAY,1);
            rcal.set(Calendar.MINUTE,0);
            rcal.set(Calendar.SECOND,0);
            alarmManager.set(AlarmManager.RTC_WAKEUP, rcal.getTimeInMillis(), peAction(ctx));
        }
        else {
            alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + deltamillis, peAction(ctx));
        }
    }
}
