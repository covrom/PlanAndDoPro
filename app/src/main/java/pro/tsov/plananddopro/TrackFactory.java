/*
 * Copyright (c) Roman Tsovanyan
 */

package pro.tsov.plananddopro;

import android.appwidget.AppWidgetManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.util.TypedValue;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class TrackFactory implements RemoteViewsService.RemoteViewsFactory {

    private Context context;
    private ContentResolver cr;
    private Cursor c;
    private PlanDoDBOpenHelper db;
    private int appWidgetId;


    public TrackFactory(Context ctx, Intent intent){
        this.context = ctx;
        this.cr = ctx.getContentResolver();
        appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);
    }

    @Override
    public void onCreate() {
        //запрос на курсор на выводимые данные
        db = PlanDoDBOpenHelper.getInstance(context);
        c = db.getAllEventsDataForWidget();
    }

    @Override
    public void onDataSetChanged() {
        c = db.getAllEventsDataForWidget();
    }

    @Override
    public void onDestroy() {
        c.close();
    }

    @Override
    public int getCount() {
        if (c!=null) return c.getCount(); else return 0;
    }

    @Override
    public RemoteViews getViewAt(int position) {

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        Bundle options = appWidgetManager.getAppWidgetOptions(appWidgetId);
        int widgetWidthDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
        if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
            widgetWidthDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        Resources res = context.getResources();

        c.moveToPosition(position);
        int idIdx = c.getColumnIndex("_id");
        long id = c.getLong(idIdx);

        RemoteViews rv = new RemoteViews(context.getPackageName(),R.layout.widget_item);

        rv.setTextViewText(R.id.widgetEventName, c.getString(c.getColumnIndex("name")));
        String itemicon = c.getString(c.getColumnIndex("itemicon"));
        if (itemicon.equals("--")) itemicon="";
        rv.setImageViewBitmap(R.id.widgetIcon, buildIcon(context, itemicon));

        Calendar cal = Calendar.getInstance();
        int today = cal.get(Calendar.DAY_OF_YEAR);
        int todayYear = cal.get(Calendar.YEAR);
        int thisMonth = cal.get(Calendar.MONTH);

        //получаем начало недели из настроек, либо вычисляем от текущей даты
        int thisYear = sp.getInt("curryear", cal.get(Calendar.YEAR));
        int thisDayOfYear = sp.getInt("currday", cal.get(Calendar.DAY_OF_YEAR));
        cal.set(Calendar.YEAR, thisYear);
        cal.set(Calendar.DAY_OF_YEAR, thisDayOfYear);
        //начало недели
        while(cal.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY){
            cal.add(Calendar.DAY_OF_YEAR, -1);
        }

        int[] et = db.readWeekTrackForWidget(id,cal);
        String[] cmnts = db.readWeekCommentsForWidget(id, cal);

        int beforeEt = db.readBeforeWeekTrackForWidget(id, cal);
        int afterEt = db.readAfterWeekTrackForWidget(id, cal);

        boolean lastIs2 = beforeEt==2;
        boolean nextIs2;

        for (int day = 0; day < 7; day++) {
            boolean inMonth = cal.get(Calendar.MONTH) == thisMonth;
            boolean inYear  = cal.get(Calendar.YEAR) == todayYear;
            boolean isToday = inYear && inMonth && (cal.get(Calendar.DAY_OF_YEAR) == today);

            boolean inFuture = cal.get(Calendar.YEAR) > todayYear;
            if (inYear) {
                inFuture = cal.get(Calendar.DAY_OF_YEAR) > today;
            }

            int cellViewId = R.id.tr1;
            if (day==1) cellViewId = R.id.tr2;
            if (day==2) cellViewId = R.id.tr3;
            if (day==3) cellViewId = R.id.tr4;
            if (day==4) cellViewId = R.id.tr5;
            if (day==5) cellViewId = R.id.tr6;
            if (day==6) cellViewId = R.id.tr7;

            //если последний = 2 и ближайший следующий тоже, то цепочка, и не меняем, иначе - не цепочка
            nextIs2 = false;
            for (int nextDay = day + 1; nextDay <= 7; nextDay++) {
                if (nextDay == 7) {
                    if (afterEt == 2) {
                        nextIs2 = true;
                        break;
                    }
                } else {
                    if (et[nextDay] == 2) {
                        nextIs2 = true;
                    }
                    if (et[nextDay] != 0) break;
                }
            }

//            TrackTextView atDay = new TrackTextView(context);
//            atDay.setEventDate(cal.getTime());
//            atDay.setEventType(et[day]);
//            atDay.setEnabledState(true);
//            atDay.setLeftConnected(lastIs2);
//            atDay.setRightConnected(nextIs2);
//            String atDayComment = cmnts[day];
//            atDay.setHasComment(!(atDayComment == null || atDayComment.equals("")));
//            atDay.setCurrentRowId(id);
//            atDay.setWidgetBitmapWidth((widgetWidthDp - 32) / 7);
//
//            rv.setImageViewBitmap(cellViewId, atDay.buildForWidget());
//
//            if (et[day] == 1) {
//                lastIs2 = false;
//            } else if (et[day] == 2) {
//                lastIs2 = true;
//            } else if (et[day] == 3) {
//                lastIs2 = false;
//            }

            String atDayComment = cmnts[day];
            boolean hasComment = !(atDayComment == null || atDayComment.equals(""));

            //выбираем стиль по статусу дня из курсора

            if (et[day]==1){
                //это был план, тогда меняем его на выполнено, если дата текущая или ранее, или очищаем, если дата в будущем
                if (inFuture||isToday)
                {
                    rv.setInt(cellViewId, "setBackgroundResource", hasComment? R.drawable.w_back_plan_c:R.drawable.w_back_plan);
                }
                else
                {
                    rv.setInt(cellViewId, "setBackgroundResource", hasComment ? R.drawable.w_back_skip_c:R.drawable.w_back_skip);
                }
                lastIs2 = false;
            }
            else if (et[day]==2){

                if (lastIs2 && nextIs2)
                    rv.setInt(cellViewId, "setBackgroundResource", hasComment? R.drawable.w_back_due_c:R.drawable.w_back_due);
                else if (lastIs2 && !nextIs2)
                    rv.setInt(cellViewId, "setBackgroundResource", hasComment ? R.drawable.w_back_due_noright_c:R.drawable.w_back_due_noright);
                else if (!lastIs2 && nextIs2)
                    rv.setInt(cellViewId, "setBackgroundResource", hasComment ? R.drawable.w_back_due_noleft_c:R.drawable.w_back_due_noleft);
                else if (!lastIs2 && !nextIs2)
                    rv.setInt(cellViewId, "setBackgroundResource", hasComment ? R.drawable.w_back_due_noall_c:R.drawable.w_back_due_noall);

                lastIs2 = true;
            }
            else if (et[day]==3){
                rv.setInt(cellViewId, "setBackgroundResource", hasComment ? R.drawable.w_back_cancel_c:R.drawable.w_back_cancel);
                lastIs2 = false;
            }
            else{
                if(lastIs2 && nextIs2)
                    rv.setInt(cellViewId, "setBackgroundResource", hasComment ? R.drawable.w_back_empty_chain_c:R.drawable.w_back_empty_chain);
                else
                    rv.setInt(cellViewId, "setBackgroundResource", hasComment ? R.drawable.w_back_empty_c:R.drawable.w_back_empty);
            }

            rv.setTextViewText(cellViewId, Integer.toString(cal.get(Calendar.DAY_OF_MONTH)));
            if (isToday)
                rv.setTextColor(cellViewId, ContextCompat.getColor(context, R.color.foreground_today));
            else
                rv.setTextColor(cellViewId, ContextCompat.getColor(context, R.color.foreground_textday));

            //интент на нажатие дня недели, дополняющий шаблонный интент
            //исполняется в самом виджете, т.к. идет работа только с БД
            DateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd");
            Intent dayIntent = new Intent();
            dayIntent.putExtra(EditEventActivity.ACTION_EXTRA_EVENTID,id);
            dayIntent.putExtra(TrackWidget.ACTION_EXTRA_PUSHDATEISO, iso8601Format.format(cal.getTime()));
            dayIntent.putExtra(TrackWidget.ACTION_EXTRA_CURRENTTYPE,et[day]);
            dayIntent.putExtra(TrackWidget.ACTION_EXTRA_CURRENTINFUTURE,inFuture);
            dayIntent.setAction(TrackWidget.ACTION_PUSHLIST);
            rv.setOnClickFillInIntent(cellViewId, dayIntent);

            cal.add(Calendar.DATE, 1);
        }

        //интент на нажатие названия, дополняющий шаблонный интент
        Intent activityIntent = new Intent();
        activityIntent.putExtra(EditEventActivity.ACTION_EXTRA_EVENTID,id);
        activityIntent.setAction(TrackWidget.ACTION_PUSHLIST);
        rv.setOnClickFillInIntent(R.id.widgetEventName, activityIntent);
        rv.setOnClickFillInIntent(R.id.widgetIcon, activityIntent);

        return rv;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        if (c!=null) return c.getLong(c.getColumnIndex("_id")); else return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    public static Bitmap buildIcon(Context context, String icontext) {
        float fontSizeSP = 18;
        int fontSizePX = convertDiptoPix(context, fontSizeSP);
        int pad = (fontSizePX / 9);

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setSubpixelText(true);
        paint.setTypeface(Typeface.createFromAsset(context.getAssets(), "flaticon.ttf"));
        paint.setColor(ContextCompat.getColor(context, R.color.foreground_text));
        paint.setTextSize(fontSizePX);


        int textWidth = (int) (paint.measureText(icontext) + pad * 2);
        int height = (int) (fontSizePX / 0.75);
        Bitmap myBitmap = Bitmap.createBitmap(textWidth, height, Bitmap.Config.ARGB_8888);
        Canvas myCanvas = new Canvas(myBitmap);
        myCanvas.drawText(icontext, pad, fontSizePX, paint);
        return myBitmap;
    }

    public static int convertDiptoPix(Context context, float dip) {
        int value = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, context.getResources().getDisplayMetrics());
        return value;
    }

    public static Bitmap buildNotifyIcon(Context context, String icontext) {
        float fontSizeSP = 48;
        int fontSizePX = convertDiptoPix(context, fontSizeSP);
        int pad = (fontSizePX / 9);

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setSubpixelText(true);
        paint.setTypeface(Typeface.createFromAsset(context.getAssets(), "flaticon.ttf"));
        paint.setColor(ContextCompat.getColor(context, R.color.notifycolor));
        paint.setTextSize(fontSizePX);


        int textWidth = (int) (paint.measureText(icontext) + pad * 2);
        int height = (int) (fontSizePX / 0.75);
        Bitmap myBitmap = Bitmap.createBitmap(textWidth, height, Bitmap.Config.ARGB_8888);
        Canvas myCanvas = new Canvas(myBitmap);
        myCanvas.drawText(icontext, pad, fontSizePX, paint);
        return myBitmap;
    }

}
