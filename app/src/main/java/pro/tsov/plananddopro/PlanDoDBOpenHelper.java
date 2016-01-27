/*
 * Copyright (c) Roman Tsovanyan
 */

package pro.tsov.plananddopro;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.prolificinteractive.materialcalendarview.CalendarDay;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class PlanDoDBOpenHelper extends SQLiteOpenHelper {
    private static PlanDoDBOpenHelper sInstance;

    // Database Info
    private static final String DATABASE_NAME = "plandoDatabase";
    private static final String LOGD_NAME = "pro.tsov.plananddopro";
    private static final int DATABASE_VERSION = 3;

    public static synchronized PlanDoDBOpenHelper getInstance(Context context) {
        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (sInstance == null) {
            sInstance = new PlanDoDBOpenHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    /**
     * Constructor should be private to prevent direct instantiation.
     * Make a call to the static method "getInstance()" instead.
     */
    private PlanDoDBOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Called when the database connection is being configured.
    // Configure database settings for things like foreign key support, write-ahead logging, etc.
    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    // Called when the database is created for the FIRST time.
    // If a database already exists on disk with the same DATABASE_NAME, this method will NOT be called.
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TRACKS_TABLE = "CREATE TABLE tracks (_id INTEGER PRIMARY KEY AUTOINCREMENT," + // Define a primary key
                "eventId INTEGER REFERENCES events," + // Define a foreign key
                "eventDay DATE," +
                "eventType INTEGER," +
                "comment TEXT" +
                ")";
        String CREATE_TRACKS_INDEX = "CREATE INDEX tracks_eventday_idx ON tracks(eventDay)";

        String CREATE_EVENTS_TABLE = "CREATE TABLE events (_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT," +
                "describe TEXT," +
                "icon TEXT," +
                "notifyhour TEXT" +
                ")";

        db.execSQL(CREATE_EVENTS_TABLE);
        db.execSQL(CREATE_TRACKS_TABLE);
        db.execSQL(CREATE_TRACKS_INDEX);
    }

    // Called when the database needs to be upgraded.
    // This method will only be called if a database already exists on disk with the same DATABASE_NAME,
    // but the DATABASE_VERSION is different than the version of the database that exists on disk.
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion == 1 && newVersion >= 2) {
            db.beginTransaction();
            try {
                db.execSQL("alter table events add column notifyhour TEXT;");
                ContentValues cv = new ContentValues();
                cv.put("notifyhour","--");
                db.update("events", cv, null, null);
                db.setTransactionSuccessful();
            }finally {
                db.endTransaction();
            }
        }
        if (oldVersion == 2 && newVersion >= 3) {
            db.beginTransaction();
            try {
                db.execSQL("alter table events add column icon TEXT;");
                ContentValues cv = new ContentValues();
                cv.put("icon", "--");
                db.update("events", cv, null, null);
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        }
    }

    // возвращает ROWID вставленной записи
    public long addEventRec(EventRec post) {
        // Create and/or open the database for writing
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = post.ContentValues();

        long Result = -1;

        // It's a good idea to wrap our insert in a transaction. This helps with performance and ensures
        // consistency of the database.
        db.beginTransaction();
        try {
            // Notice how we haven't specified the primary key. SQLite auto increments the primary key column.
            Result = db.insertOrThrow("events", null, values);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.d(LOGD_NAME, "Error while trying to add event to database");
        } finally {
            db.endTransaction();
        }

        return Result;
    }

    public int updateEventRec(EventRec post, long rowid) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = post.ContentValues();

        int Result = 0;

        db.beginTransaction();
        try {
            Result = db.update("events", values, "rowid = ?", new String[]{String.valueOf(rowid)});
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.d(LOGD_NAME, "Error while trying to update event in database");
        } finally {
            db.endTransaction();
        }
        return Result;
    }

    public void delEventRec(long rowid) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            //тут еще надо удалить все связанные данные из трэкинга
            db.delete("tracks", "eventId = " + String.valueOf(rowid), null);
            db.delete("events", "rowid = " + String.valueOf(rowid), null);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.d(LOGD_NAME, "Error while trying to delete event from database");
        } finally {
            db.endTransaction();
        }
    }

    public Cursor getAllEventsData() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor Result = null;
        try{
            String POSTS_SELECT_QUERY =
                "SELECT events._id as _id, events.name as name, events.describe as describe, events.notifyhour as notifyhour, MAX(DATE(tracks.eventDay)) as lasteventdue, 0 as clockicon, events.icon as itemicon FROM events LEFT JOIN tracks ON (events._id = tracks.eventId) and (eventType=2) GROUP BY events._id,events.name,events.describe,events.notifyhour";
                //"SELECT events._id as _id, events.name as name, events.describe as describe FROM events";
            Result = db.rawQuery(POSTS_SELECT_QUERY, null);
        } catch (Exception e) {
            Log.d(LOGD_NAME, "Error while trying to read events cursor from database");
        }
        return Result;
    }

    public Cursor getAllTodayPlannedEvents() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor Result = null;
        try{
            String POSTS_SELECT_QUERY =
                    "SELECT events._id as _id, events.name as name, events.describe as describe, events.notifyhour as notifyhour, events.icon as itemicon FROM events INNER JOIN tracks ON (events._id = tracks.eventId) and (date(tracks.eventDay)=DATE('now')) and (tracks.eventType = 1) and (events.notifyhour<>'--')";
            //"SELECT events._id as _id, events.name as name, events.describe as describe FROM events";
            Result = db.rawQuery(POSTS_SELECT_QUERY, null);
            //Result = db.query("events", null, null, null, null, null, null);
        } catch (Exception e) {
            Log.d(LOGD_NAME, "Error while trying to read today events cursor from database");
        }
        return Result;
    }

    public Cursor getAllEventsDataForWidget() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor Result = null;
        try{
            String POSTS_SELECT_QUERY =
                    "SELECT events._id as _id, events.name as name, events.describe as describe, events.icon as itemicon  FROM events";
            //"SELECT events._id as _id, events.name as name, events.describe as describe FROM events";
            Result = db.rawQuery(POSTS_SELECT_QUERY, null);
            //Result = db.query("events", null, null, null, null, null, null);
        } catch (Exception e) {
            Log.d(LOGD_NAME, "Error while trying to read widget events cursor from database");
        }
        return Result;
    }

    public int[] readWeekTrackForWidget(long eventId, Calendar cal_to_week_start){

        DateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd");
        HashMap<String,Integer> hash = new HashMap<>();

        Calendar cal = (Calendar) cal_to_week_start.clone();
        while(cal.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY){
            cal.add(Calendar.DAY_OF_YEAR, -1);
        }

        Calendar cal_weekend = (Calendar) cal.clone();
        int i=0;
        while(cal_weekend.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY){
            hash.put(iso8601Format.format(cal_weekend.getTime()), i);
            i++;
            cal_weekend.add(Calendar.DAY_OF_YEAR, 1);
        }
        hash.put(iso8601Format.format(cal_weekend.getTime()), i);//седьмой день

        CalendarDay calWeekendDay = CalendarDay.from(cal_weekend);

        int[] Result = {0,0,0,0,0,0,0};

        SQLiteDatabase db = this.getReadableDatabase();
        try{

            //берем неделю
            String SELECT_QUERY =
                    "SELECT eventId, eventDay, eventType FROM tracks WHERE (eventId="
                            //+ String.valueOf(eventId) + ") ORDER BY DATE(eventDay) ASC";
                            + String.valueOf(eventId)
                            + ") AND (eventDay >= DATE('"+iso8601Format.format(cal.getTime())+"')) AND (eventDay <= DATE('"+iso8601Format.format(cal_weekend.getTime())+"')) ORDER BY DATE(eventDay) ASC";

            Cursor cursor = db.rawQuery(SELECT_QUERY, null);

            if (cursor.moveToFirst()) {
                do {
                    String curDay = cursor.getString(cursor.getColumnIndex("eventDay"));
                    Result[hash.get(curDay)]=cursor.getInt(cursor.getColumnIndex("eventType"));
                } while(cursor.moveToNext());
            }
            cursor.close();

        } catch (Exception e) {
            Log.d(LOGD_NAME, "Error while trying to read event data from database");
        }

        return Result;
    }

    public int readBeforeWeekTrackForWidget(long eventId, Calendar cal_to_week_start) {

        DateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd");

        Calendar cal = (Calendar) cal_to_week_start.clone();
        while (cal.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
            cal.add(Calendar.DAY_OF_YEAR, -1);
        }

        Calendar cal_weekend = (Calendar) cal.clone();
        int i = 0;
        while (cal_weekend.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
            i++;
            cal_weekend.add(Calendar.DAY_OF_YEAR, 1);
        }

        int Result = 0;

        SQLiteDatabase db = this.getReadableDatabase();
        try {

            //берем первый раньше недели
            String SELECT_QUERY =
                    "SELECT eventId, eventDay, eventType FROM tracks WHERE (eventId="
                            + String.valueOf(eventId)
                            + ") AND (eventType!=0) AND (eventDay < DATE('" + iso8601Format.format(cal.getTime()) + "')) ORDER BY DATE(eventDay) DESC LIMIT 1";

            Cursor cursor = db.rawQuery(SELECT_QUERY, null);

            if (cursor.moveToFirst()) {
                do {
                    Result = cursor.getInt(cursor.getColumnIndex("eventType"));
                } while (cursor.moveToNext());
            }
            cursor.close();

        } catch (Exception e) {
            Log.d(LOGD_NAME, "Error while trying to read event data from database");
        }

        return Result;
    }

    public int readAfterWeekTrackForWidget(long eventId, Calendar cal_to_week_start) {

        DateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd");

        Calendar cal = (Calendar) cal_to_week_start.clone();
        while (cal.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
            cal.add(Calendar.DAY_OF_YEAR, -1);
        }

        Calendar cal_weekend = (Calendar) cal.clone();
        int i = 0;
        while (cal_weekend.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
            i++;
            cal_weekend.add(Calendar.DAY_OF_YEAR, 1);
        }

        int Result = 0;

        SQLiteDatabase db = this.getReadableDatabase();
        try {

            //берем первый позже недели
            String SELECT_QUERY =
                    "SELECT eventId, eventDay, eventType FROM tracks WHERE (eventId="
                            + String.valueOf(eventId)
                            + ") AND (eventType!=0) AND (eventDay > DATE('" + iso8601Format.format(cal_weekend.getTime()) + "')) ORDER BY DATE(eventDay) ASC LIMIT 1";

            Cursor cursor = db.rawQuery(SELECT_QUERY, null);

            if (cursor.moveToFirst()) {
                do {
                    Result = cursor.getInt(cursor.getColumnIndex("eventType"));
                } while (cursor.moveToNext());
            }
            cursor.close();

        } catch (Exception e) {
            Log.d(LOGD_NAME, "Error while trying to read event data from database");
        }

        return Result;
    }

    public EventRec getEventData(long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        EventRec Result = null;
        try{
            String POSTS_SELECT_QUERY =
                    "SELECT events._id as _id, events.name as name, events.describe as describe, events.icon as icon, events.notifyhour as notifyhour FROM events WHERE events._id="
                     + String.valueOf(id);
            Cursor cursor = db.rawQuery(POSTS_SELECT_QUERY, null);
            if (cursor.moveToFirst()) {
                do {
                    Result = new EventRec();
                    Result.name = cursor.getString(cursor.getColumnIndex("name"));
                    Result.describe = cursor.getString(cursor.getColumnIndex("describe"));
                    Result.icon = cursor.getString(cursor.getColumnIndex("icon"));
                    Result.notifyhour = cursor.getString(cursor.getColumnIndex("notifyhour"));
                } while(cursor.moveToNext());

            }
            cursor.close();
        } catch (Exception e) {
            Log.d(LOGD_NAME, "Error while trying to read event data from database");
        }
        return Result;
    }

    public void readTrackToRec(TrackRec rec){
        SQLiteDatabase db = this.getReadableDatabase();
        try{
            rec.trackdates.clear();
            rec.trackcomments.clear();

            String SELECT_QUERY =
                    "SELECT eventDay, eventType, comment  FROM tracks WHERE eventId="
                            + String.valueOf(rec.eventId)+" ORDER BY DATE(eventDay) ASC";

            Cursor cursor = db.rawQuery(SELECT_QUERY, null);
            if (cursor.moveToFirst()) {
                do {
                    String dateTime = cursor.getString(cursor.getColumnIndex("eventDay"));

                    DateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd");
                    try {
                        Date date = iso8601Format.parse(dateTime);
                        rec.trackdates.put(date, cursor.getInt(cursor.getColumnIndex("eventType")));
                        rec.trackcomments.put(date, cursor.getString(cursor.getColumnIndex("comment")));
                    } catch (ParseException e) {
                        Log.e(LOGD_NAME, "Parsing ISO8601 datetime failed", e);
                    }


//                    long when = date.getTime();
//                    int flags = 0;
//                    flags |= android.text.format.DateUtils.FORMAT_SHOW_TIME;
//                    flags |= android.text.format.DateUtils.FORMAT_SHOW_DATE;
//                    flags |= android.text.format.DateUtils.FORMAT_ABBREV_MONTH;
//                    flags |= android.text.format.DateUtils.FORMAT_SHOW_YEAR;
//
//                    String finalDateTime = android.text.format.DateUtils.formatDateTime(context,
//                            when + TimeZone.getDefault().getOffset(when), flags);

                } while(cursor.moveToNext());
            }
            cursor.close();
        } catch (Exception e) {
            Log.d(LOGD_NAME, "Error while trying to read event data from database");
        }

    }

    public void delTrackEventOnDate(long eventId, Date dt) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            DateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd");
            db.delete("tracks", "(eventId = " + String.valueOf(eventId)+") and (eventDay = DATE('"+iso8601Format.format(dt)+"'))", null);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.d(LOGD_NAME, "Error while trying to delete track from database");
        } finally {
            db.endTransaction();
        }
    }

    public void updateTrackEventOnDate(long eventId, Date dt, int eventType) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            DateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd");
            db.delete("tracks", "(eventId = " + String.valueOf(eventId)+") and (eventDay = DATE('"+iso8601Format.format(dt)+"'))", null);

            ContentValues values = new ContentValues();
            values.put("eventId", eventId);
            values.put("eventDay",iso8601Format.format(dt));
            values.put("eventType", eventType);
            db.insertOrThrow("tracks", null, values);

            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.d(LOGD_NAME, "Error while trying to delete track from database");
        } finally {
            db.endTransaction();
        }
    }
}
