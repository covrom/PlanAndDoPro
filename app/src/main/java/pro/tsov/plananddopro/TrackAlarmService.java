/*
 * Copyright (c) Roman Tsovanyan
 */

package pro.tsov.plananddopro;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

public class TrackAlarmService extends IntentService {

    public TrackAlarmService(String name) {
        super(name);
    }
    public TrackAlarmService() {
        super("TrackAlarmService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent.getAction().equalsIgnoreCase(TrackAlarmReceiver.ACTION_SET_TODAY_ALARMS)){
            //вызывается при загрузке устройства
            //и потом при обновлении данных в БД

            //устанавливаем нотификации по трекам, на сегодня, если время прошло
            PlanDoDBOpenHelper db = PlanDoDBOpenHelper.getInstance(this);
            Cursor c = db.getAllTodayPlannedEvents();
            if (c.moveToFirst()) {
                do {
                    long eventId = c.getLong(c.getColumnIndex("_id"));
                    String eventName = c.getString(c.getColumnIndex("name"));
                    String eventDesc = c.getString(c.getColumnIndex("describe"));
                    String eventHour = c.getString(c.getColumnIndex("notifyhour"));
                    String eventIcon = c.getString(c.getColumnIndex("itemicon"));
                    int ahour = Arrays.asList(EditEventActivity.spindata).indexOf(eventHour)+5;
                    if(ahour>23)ahour=ahour-24;

                    Calendar cal = Calendar.getInstance();
                    int currHour = cal.get(Calendar.HOUR_OF_DAY);
                    if (ahour<=currHour) {
                        cal.set(Calendar.HOUR_OF_DAY, ahour);
                        cal.set(Calendar.MINUTE, 0);
                        cal.set(Calendar.SECOND, 0);
                        cal.set(Calendar.MILLISECOND, 0);

                        Intent notificationIntent = new Intent(this, TrackEventFragment.class);
                        notificationIntent.putExtra(EditEventActivity.ACTION_EXTRA_EVENTID, eventId);
                        //notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                        DateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd");

                        Intent completeIntent = new Intent(this, TrackAlarmReceiver.class);
                        completeIntent.putExtra(EditEventActivity.ACTION_EXTRA_EVENTID, eventId);
                        completeIntent.putExtra(TrackWidget.ACTION_EXTRA_PUSHDATEISO, iso8601Format.format(cal.getTime()));
                        completeIntent.setAction(TrackAlarmReceiver.ACTION_SET_TODAY_COMPLETE);

                        Intent cancelIntent = new Intent(this, TrackAlarmReceiver.class);
                        cancelIntent.putExtra(EditEventActivity.ACTION_EXTRA_EVENTID, eventId);
                        cancelIntent.putExtra(TrackWidget.ACTION_EXTRA_PUSHDATEISO, iso8601Format.format(cal.getTime()));
                        cancelIntent.setAction(TrackAlarmReceiver.ACTION_SET_TODAY_CANCEL);

                        // по клику на уведомлении откроется активити
                        Notification.Builder nb = new Notification.Builder(this)
                                .setSmallIcon(R.drawable.ic_stat_1) //иконка уведомления
                                .setAutoCancel(true) //уведомление закроется по клику на него
                                .setDefaults(Notification.DEFAULT_ALL) // звук, вибро и диодный индикатор выставляются по умолчанию
                                .setTicker(eventName) //текст, который отобразится вверху статус-бара при создании уведомления
                                .setContentTitle(eventName) //заголовок уведомления
                                .setContentText(eventDesc) // Основной текст уведомления
                                .setContentIntent(PendingIntent.getActivity(this, (int) eventId, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT))
                                .setWhen(cal.getTimeInMillis()) //отображаемое время уведомления
                                .setShowWhen(true)
                                .setOnlyAlertOnce(true)
                                .addAction(android.R.drawable.ic_menu_close_clear_cancel, getResources().getString(R.string.track_cancel), PendingIntent.getBroadcast(this, (int) eventId, cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT))
                                .addAction(R.drawable.ic_stat_1, getResources().getString(R.string.track_due), PendingIntent.getBroadcast(this, (int) eventId, completeIntent, PendingIntent.FLAG_UPDATE_CURRENT));

                        if (eventIcon.equals("--")){
                            nb.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
                        }else{
                            nb.setLargeIcon(TrackFactory.buildNotifyIcon(this, eventIcon));
                        }

                        Notification notification = nb.build(); //генерируем уведомление
                        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                        int notifyId = (int) eventId;
                        manager.notify(notifyId, notification); // отображаем его пользователю.
                    }

                } while (c.moveToNext());
            }

            //переносим на текущую неделю и обновляем виджет, если переход между неделями был больше 5 мин назад
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
            Calendar cal = Calendar.getInstance();
            long lastdt = cal.getTimeInMillis();
            long lastprevnext = sp.getLong("lastprevnext",0);
            if ((lastdt-lastprevnext)>600000){
                sp.edit()
                        .putInt("currday", cal.get(Calendar.DAY_OF_YEAR))
                        .putInt("curryear", cal.get(Calendar.YEAR))
                        .putLong("lastprevnext", lastdt)
                        .commit();

                Intent i = new Intent(this, TrackWidget.class);
                i.setAction(TrackWidget.ACTION_REFRESH);
                sendBroadcast(i);
            }

        }
        else if(intent.getAction().equalsIgnoreCase(TrackAlarmReceiver.ACTION_SET_TODAY_COMPLETE)){
            DateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd");
            String dateISO = intent.getStringExtra(TrackWidget.ACTION_EXTRA_PUSHDATEISO);
            try {
                long eventId = intent.getLongExtra(EditEventActivity.ACTION_EXTRA_EVENTID, -1);
                makeCurrExec(this,eventId,iso8601Format.parse(dateISO));
                int intId = (int) eventId;
                NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                manager.cancel(intId);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        else if(intent.getAction().equalsIgnoreCase(TrackAlarmReceiver.ACTION_SET_TODAY_CANCEL)){
            DateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd");
            String dateISO = intent.getStringExtra(TrackWidget.ACTION_EXTRA_PUSHDATEISO);
            try {
                long eventId = intent.getLongExtra(EditEventActivity.ACTION_EXTRA_EVENTID, -1);
                makeCurrNoExec(this, eventId, iso8601Format.parse(dateISO));
                int intId = (int) eventId;
                NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                manager.cancel(intId);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        TrackAlarmReceiver.sendActionOverAlarm(this,3600000,true);
        TrackAlarmReceiver.completeWakefulIntent(intent);
    }

    private void makeCurrNoExec(Context context, long currentRowId, Date currentDay) {
        PlanDoDBOpenHelper helper = PlanDoDBOpenHelper.getInstance(context);
        TrackRec trc = new TrackRec(currentRowId);
        helper.readTrackToRec(trc);
        helper.updateTrackEventOnDate(currentRowId, currentDay, 3, trc.trackcomments.get(EventCalendar.roundDate(currentDay)));
        notifyWidgetsDataChanged(context);
    }

    private void makeCurrExec(Context context, long currentRowId, Date currentDay) {
        PlanDoDBOpenHelper helper = PlanDoDBOpenHelper.getInstance(context);
        TrackRec trc = new TrackRec(currentRowId);
        helper.readTrackToRec(trc);
        helper.updateTrackEventOnDate(currentRowId, currentDay, 2, trc.trackcomments.get(EventCalendar.roundDate(currentDay)));
        notifyWidgetsDataChanged(context);
    }

    public void notifyWidgetsDataChanged(Context context){
        Intent intent = new Intent(context, TrackWidget.class);
        intent.setAction(TrackWidget.ACTION_REFRESH);
        context.sendBroadcast(intent);
//        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        TrackAlarmReceiver.sendActionOverAlarm(context,5000,false);
    }

}
