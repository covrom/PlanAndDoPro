/*
 * Copyright (c) Roman Tsovanyan
 */

package pro.tsov.plananddopro;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class TrackWidget extends AppWidgetProvider {

    public static final String ACTION_REFRESH = "pro.tsov.plananddopro.refreshtrackwidget";
    public static final String ACTION_PREV = "pro.tsov.plananddopro.prevweektrackwidget";
    public static final String ACTION_NEXT = "pro.tsov.plananddopro.nextweektrackwidget";
    public static final String ACTION_PUSHLIST = "pro.tsov.plananddopro.pushlist";
    public static final String ACTION_EXTRA_PUSHDATEISO = "pro.tsov.plananddopro.pressedDateISO";
    public static final String ACTION_EXTRA_CURRENTTYPE = "pro.tsov.plananddopro.pressedEventType";
    public static final String ACTION_EXTRA_CURRENTINFUTURE = "pro.tsov.plananddopro.pressedEventInPast";

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
    }

    public void notifyWidgetsDataChanged(Context context){
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        ComponentName trWidget = new ComponentName(context,TrackWidget.class);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(trWidget);
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widgetListView);
        TrackAlarmReceiver.sendActionOverAlarm(context,5000,false);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        String action = intent.getAction();

        if (action.equalsIgnoreCase(ACTION_REFRESH)){
            //обновляем данные виджетов
            notifyWidgetsDataChanged(context);
        }
        else if(action.equalsIgnoreCase(ACTION_PREV)){
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
            Calendar cal = Calendar.getInstance();
            int thisYear = sp.getInt("curryear", cal.get(Calendar.YEAR));
            int thisDay = sp.getInt("currday", cal.get(Calendar.DAY_OF_YEAR));
            cal.set(Calendar.YEAR, thisYear);
            cal.set(Calendar.DAY_OF_YEAR, thisDay);
            cal.add(Calendar.DAY_OF_YEAR, -7);

            sp.edit()
                    .putInt("currday", cal.get(Calendar.DAY_OF_YEAR))
                    .putInt("curryear", cal.get(Calendar.YEAR))
                    .commit();

            redrawWidgets(context);
            notifyWidgetsDataChanged(context);
        }
        else if(action.equalsIgnoreCase(ACTION_NEXT)){
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
            Calendar cal = Calendar.getInstance();
            int thisYear = sp.getInt("curryear", cal.get(Calendar.YEAR));
            int thisDay = sp.getInt("currday", cal.get(Calendar.DAY_OF_YEAR));
            cal.set(Calendar.YEAR, thisYear);
            cal.set(Calendar.DAY_OF_YEAR, thisDay);
            cal.add(Calendar.DAY_OF_YEAR, 7);

            sp.edit()
                    .putInt("currday", cal.get(Calendar.DAY_OF_YEAR))
                    .putInt("curryear", cal.get(Calendar.YEAR))
                    .commit();

            redrawWidgets(context);
            notifyWidgetsDataChanged(context);
        }
        else if(action.equalsIgnoreCase(ACTION_PUSHLIST)){
            DateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd");
            long eventId = intent.getLongExtra(EditEventActivity.ACTION_EXTRA_EVENTID, -1);
            String dateISO = intent.getStringExtra(ACTION_EXTRA_PUSHDATEISO);
            int eventType = intent.getIntExtra(ACTION_EXTRA_CURRENTTYPE, -1);
            boolean inFuture = intent.getBooleanExtra(ACTION_EXTRA_CURRENTINFUTURE, false);
            if (dateISO==null){
                //открываем трекер события
                Intent i = new Intent(context,MainActivity.class);
                i.putExtra(EditEventActivity.ACTION_EXTRA_EVENTID, eventId);
                i.setFlags (Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(i);
            }
            else{
                //меняем статус события на дату и обновляем виджет
                if (eventType==1){
                    //это был план, тогда меняем его на выполнено, если дата текущая или ранее, или очищаем, если дата в будущем
                    if(inFuture){
                        try {
                            deleteCurrRec(context,eventId,iso8601Format.parse(dateISO));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }else{
                        try {
                            makeCurrExec(context,eventId,iso8601Format.parse(dateISO));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                }
                else if(eventType==2){
                    //это было выполнено, теперь меняем на не выполнено
                    try {
                        makeCurrNoExec(context,eventId,iso8601Format.parse(dateISO));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
                else if(eventType==3){
                    //это было не выполнено - очищаем
                    try {
                        deleteCurrRec(context,eventId,iso8601Format.parse(dateISO));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
                else{
                    //если ничего не было, то стало запланировано, даже если пропущено в прошлом
                    try {
                        makeCurrPlan(context,eventId,iso8601Format.parse(dateISO));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }

            }

        }

    }

    private void makeCurrPlan(Context context, long currentRowId, Date currentDay) {
        PlanDoDBOpenHelper helper = PlanDoDBOpenHelper.getInstance(context);
        TrackRec trc = new TrackRec(currentRowId);
        helper.readTrackToRec(trc);
        helper.updateTrackEventOnDate(currentRowId, currentDay, 1, trc.trackcomments.get(currentDay));
        notifyWidgetsDataChanged(context);
        ShowToast(context, R.string.to_planned,currentDay);
    }

    private void makeCurrNoExec(Context context, long currentRowId, Date currentDay) {
        PlanDoDBOpenHelper helper = PlanDoDBOpenHelper.getInstance(context);
        TrackRec trc = new TrackRec(currentRowId);
        helper.readTrackToRec(trc);
        helper.updateTrackEventOnDate(currentRowId, currentDay, 3, trc.trackcomments.get(currentDay));
        notifyWidgetsDataChanged(context);
        ShowToast(context, R.string.to_cancel,currentDay);
    }

    private void makeCurrExec(Context context, long currentRowId, Date currentDay) {
        PlanDoDBOpenHelper helper = PlanDoDBOpenHelper.getInstance(context);
        TrackRec trc = new TrackRec(currentRowId);
        helper.readTrackToRec(trc);
        helper.updateTrackEventOnDate(currentRowId, currentDay, 2, trc.trackcomments.get(currentDay));
        notifyWidgetsDataChanged(context);
        ShowToast(context, R.string.to_due,currentDay);
    }

    public void deleteCurrRec(Context context, long currentRowId, Date currentDay){
        //удаляем запись
        PlanDoDBOpenHelper helper = PlanDoDBOpenHelper.getInstance(context);
        helper.delTrackEventOnDate(currentRowId, currentDay);
        notifyWidgetsDataChanged(context);
        ShowToast(context, R.string.to_clean,currentDay);
    }

    private void ShowToast(Context context, int resID, Date day){
        SimpleDateFormat sdf = new SimpleDateFormat("dd");
        String ttx = context.getResources().getString(resID) + " " + sdf.format(day);
        Toast tt = Toast.makeText(context, ttx, Toast.LENGTH_SHORT);
        tt.setGravity(Gravity.TOP, 0, 0);
        tt.show();
    }
    @Override
    public void onRestored(Context context, int[] oldWidgetIds, int[] newWidgetIds) {
        super.onRestored(context, oldWidgetIds, newWidgetIds);
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
        drawWidget(context, appWidgetId);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        for (int appWidgetId : appWidgetIds) {
            drawWidget(context, appWidgetId);
        }
    }

    private void redrawWidgets(Context context) {
        int[] appWidgetIds = AppWidgetManager.getInstance(context).getAppWidgetIds(
                new ComponentName(context, TrackWidget.class));
        for (int appWidgetId : appWidgetIds) {
            drawWidget(context, appWidgetId);
        }
    }

    private void drawWidget(Context context, int appWidgetId) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);

        RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget);

        Calendar cal = Calendar.getInstance();
        int thisYear = sp.getInt("curryear", cal.get(Calendar.YEAR));
        int thisDayOfYear = sp.getInt("currday", cal.get(Calendar.DAY_OF_YEAR));
        cal.set(Calendar.YEAR, thisYear);
        cal.set(Calendar.DAY_OF_YEAR, thisDayOfYear);
        //конец недели
        while(cal.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY){
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }

        DateFormat headFormat = new SimpleDateFormat("w '/' MMM yy");
        rv.setTextViewText(R.id.month_label, headFormat.format(cal.getTime()));

        DateFormatSymbols dfs = DateFormatSymbols.getInstance();
        String[] weekdays = dfs.getShortWeekdays();
        rv.setTextViewText(R.id.dc1,weekdays[Calendar.MONDAY]);
        rv.setTextViewText(R.id.dc2,weekdays[Calendar.TUESDAY]);
        rv.setTextViewText(R.id.dc3,weekdays[Calendar.WEDNESDAY]);
        rv.setTextViewText(R.id.dc4,weekdays[Calendar.THURSDAY]);
        rv.setTextViewText(R.id.dc5, weekdays[Calendar.FRIDAY]);
        rv.setTextViewText(R.id.dc6, weekdays[Calendar.SATURDAY]);
        rv.setTextViewText(R.id.dc7, weekdays[Calendar.SUNDAY]);

        rv.setOnClickPendingIntent(R.id.prev_month_button,
                PendingIntent.getBroadcast(context, 0,
                        new Intent(context, TrackWidget.class)
                                .setAction(ACTION_PREV),
                        PendingIntent.FLAG_UPDATE_CURRENT));

        rv.setOnClickPendingIntent(R.id.next_month_button,
                PendingIntent.getBroadcast(context, 0,
                        new Intent(context, TrackWidget.class)
                                .setAction(ACTION_NEXT),
                        PendingIntent.FLAG_UPDATE_CURRENT));

        //открыть основную активность
        rv.setOnClickPendingIntent(R.id.month_label,
                PendingIntent.getActivity(context, 0,
                        new Intent(context, MainActivity.class),
                        PendingIntent.FLAG_UPDATE_CURRENT));


        Intent intent = new Intent(context,TrackService.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);

        rv.setRemoteAdapter(R.id.widgetListView, intent);

        //шаблонный интент на нажатие элемента списка
        Intent templateIntent = new Intent(context,TrackWidget.class);
        templateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        templateIntent.setAction(ACTION_PUSHLIST);
        PendingIntent templatePendingIntent = PendingIntent.getBroadcast(context,0,templateIntent,PendingIntent.FLAG_UPDATE_CURRENT);

        rv.setPendingIntentTemplate(R.id.widgetListView, templatePendingIntent);

        appWidgetManager.updateAppWidget(appWidgetId, rv);

    }

}
