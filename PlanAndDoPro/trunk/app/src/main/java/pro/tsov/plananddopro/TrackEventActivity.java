/*
 * Copyright (c) Roman Tsovanyan
 */

package pro.tsov.plananddopro;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class TrackEventActivity extends AppCompatActivity implements View.OnClickListener {

    public long currentRowId;
    public Date currentDay;
    private TrackRec trackrec;
    private static final String PREF_MONTH = "pro.tsov.plananddopro.trackactivitymonth";
    private static final String PREF_YEAR = "pro.tsov.plananddopro.trackactivityyear";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) return;

        setContentView(R.layout.activity_track_event);

        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        toolbar.setTitle("");

        FloatingActionButton editFAB = (FloatingActionButton) findViewById(R.id.editFAB);
        editFAB.setRippleColor(ContextCompat.getColor(this, R.color.colorAccentBright));
        editFAB.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                editEvent();
            }
        });

        FloatingActionButton sendFAB = (FloatingActionButton) findViewById(R.id.sendFAB);
        sendFAB.setRippleColor(ContextCompat.getColor(this, R.color.colorAccentBright));
        sendFAB.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sendEvent();
            }
        });

        currentDay = Calendar.getInstance().getTime();

        Button btnPrev = (Button) findViewById(R.id.prev_month_button);
        btnPrev.setTypeface(Typeface.createFromAsset(getResources().getAssets(), "fontawesome-webfont.ttf"));
        btnPrev.setOnClickListener(this);

        Button btnNext = (Button) findViewById(R.id.next_month_button);
        btnNext.setTypeface(Typeface.createFromAsset(getResources().getAssets(), "fontawesome-webfont.ttf"));
        btnNext.setOnClickListener(this);

        Button btnTitl = (Button) findViewById(R.id.month_label);
        btnTitl.setText(new SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(currentDay));
        btnTitl.setOnClickListener(this);

        Intent i = getIntent();
        currentRowId = i.getLongExtra(EditEventActivity.ACTION_EXTRA_EVENTID, -1);
        trackrec = new TrackRec(currentRowId);
        refreshFromDB();

        LocalBroadcastManager bManager = LocalBroadcastManager.getInstance(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(TrackWidget.ACTION_REFRESH);
        bManager.registerReceiver(bReceiver, intentFilter);

    }


    private BroadcastReceiver bReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(TrackWidget.ACTION_REFRESH)) {
                refreshFromDB();
            }
        }
    };

    private void editEvent(){
        Intent i = new Intent(this, EditEventActivity.class);
        i.putExtra(EditEventActivity.ACTION_EXTRA_EVENTID, currentRowId);//новый
        startActivityForResult(i, 2);
    }

    private void sendEvent() {
        TrackRec tr = new TrackRec(currentRowId);
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, tr.getTabbedText(PlanDoDBOpenHelper.getInstance(this)));
        sendIntent.setType("text/plain");
        startActivity(sendIntent);
    }

    @Override
    public void onClick(View v) {
        int vid = v.getId();
        if (vid == R.id.prev_month_button) prevMonth();
        if (vid == R.id.next_month_button) nextMonth();
        if (vid == R.id.month_label) resetMonth();
    }

    private void prevMonth(){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        Calendar cal = Calendar.getInstance();
        int thisMonth = sp.getInt(PREF_MONTH, cal.get(Calendar.MONTH));
        int thisYear = sp.getInt(PREF_YEAR, cal.get(Calendar.YEAR));
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.MONTH, thisMonth);
        cal.set(Calendar.YEAR, thisYear);
        cal.add(Calendar.MONTH, -1);
        sp.edit()
                .putInt(PREF_MONTH, cal.get(Calendar.MONTH))
                .putInt(PREF_YEAR, cal.get(Calendar.YEAR))
                .apply();
        refreshFromDB();
    }

    private void nextMonth() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        Calendar cal = Calendar.getInstance();
        int thisMonth = sp.getInt(PREF_MONTH, cal.get(Calendar.MONTH));
        int thisYear = sp.getInt(PREF_YEAR, cal.get(Calendar.YEAR));
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.MONTH, thisMonth);
        cal.set(Calendar.YEAR, thisYear);
        cal.add(Calendar.MONTH, 1);
        sp.edit()
                .putInt(PREF_MONTH, cal.get(Calendar.MONTH))
                .putInt(PREF_YEAR, cal.get(Calendar.YEAR))
                .apply();
        refreshFromDB();
    }

    private void resetMonth() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        sp.edit().remove(PREF_MONTH).remove(PREF_YEAR).apply();
        refreshFromDB();
    }

    public void refreshFromDB(){
        if (currentRowId != -1) {
            //считываем из базы
            PlanDoDBOpenHelper helper = PlanDoDBOpenHelper.getInstance(this);
            EventRec rec = helper.getEventData(currentRowId);
            if (rec != null) {

                ////////////////////////////////////////////
                ((TextView) findViewById(R.id.textViewName)).setText(rec.name);
                ((TextView) findViewById(R.id.textViewDesc)).setText(rec.describe);
                if (rec.icon.equals("--")) {
                    ((TextView) findViewById(R.id.textViewIcon)).setText("    ");
                } else {
                    TextView v = ((TextView) findViewById(R.id.textViewIcon));
                    v.setText(rec.icon);
                    v.setTypeface(Typeface.createFromAsset(getResources().getAssets(), "flaticon.ttf"));
                }

            }
            refreshDecorators();
        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==2) refreshFromDB();
    }

    public void refreshDecorators(){

        PlanDoDBOpenHelper db = PlanDoDBOpenHelper.getInstance(this);
        db.readTrackToRec(trackrec);

        int numWeeks = 6;
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        ViewGroup rv = (ViewGroup) findViewById(R.id.calendar);

        Calendar cal = Calendar.getInstance();
        int thisMonth = sp.getInt(PREF_MONTH, cal.get(Calendar.MONTH));
        int thisYear = sp.getInt(PREF_YEAR, cal.get(Calendar.YEAR));
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.MONTH, thisMonth);
        cal.set(Calendar.YEAR, thisYear);

        ((Button) findViewById(R.id.month_label)).setText(DateFormat.format("MMMM yyyy", cal));

        cal.set(Calendar.DAY_OF_MONTH, 1);
        int monthStartDayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        if (monthStartDayOfWeek == Calendar.SUNDAY)
                cal.add(Calendar.DAY_OF_MONTH, -6);
            else cal.add(Calendar.DAY_OF_MONTH, 2-monthStartDayOfWeek);

        rv.removeAllViews();

        LayoutInflater inflater = (LayoutInflater) getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View headerRowRv = inflater.inflate(R.layout.activity_track_row_header,null);

        DateFormatSymbols dfs = DateFormatSymbols.getInstance();
        String[] weekdays = dfs.getShortWeekdays();
        for (int day = Calendar.MONDAY; day <= Calendar.SATURDAY; day++) {
            View atCellHeader = inflater.inflate(R.layout.activity_track_cell_header, (ViewGroup) headerRowRv,false);
            TextView dayRv = (TextView) atCellHeader.findViewById(R.id.at_cell_header);
            dayRv.setText(weekdays[day]);
            ((ViewGroup)headerRowRv).addView(atCellHeader);
        }
        View atCellHeader = inflater.inflate(R.layout.activity_track_cell_header, (ViewGroup) headerRowRv, false);
        TextView dayRv = (TextView) atCellHeader.findViewById(R.id.at_cell_header);
        dayRv.setText(weekdays[Calendar.SUNDAY]);
        ((ViewGroup) headerRowRv).addView(atCellHeader);

        rv.addView(headerRowRv);

        for (int week = 0; week < numWeeks; week++) {

            int[] et = db.readWeekTrackForWidget(currentRowId, cal);
            int beforeEt = db.readBeforeWeekTrackForWidget(currentRowId, cal);
            int afterEt = db.readAfterWeekTrackForWidget(currentRowId, cal);
            boolean lastIs2 = beforeEt == 2;
            boolean nextIs2;

            View rowRv = inflater.inflate(R.layout.activity_track_row_week, null);

            for (int day = 0; day < 7; day++) {
                boolean inMonth = cal.get(Calendar.MONTH) == thisMonth;

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

                View cellRv = inflater.inflate(R.layout.activity_track_cell_day,(ViewGroup) rowRv,false);
                TrackTextView atDay = (TrackTextView) cellRv.findViewById(R.id.at_day);
                atDay.setEventDate(cal.getTime());
                atDay.setEventType(et[day]);

                if (inMonth) {
                    atDay.setEnabledState(true);
                    atDay.setLeftConnected(lastIs2);
                    atDay.setRightConnected(nextIs2);
                    //atDay.setHasComment(true);
                    atDay.setCurrentRowId(currentRowId);
                    atDay.build();

                    if (et[day] == 1) {
                        lastIs2 = false;
                    } else if (et[day] == 2) {
                        lastIs2 = true;
                    } else if (et[day] == 3) {
                        lastIs2 = false;
                    }

                }

                ((ViewGroup)rowRv).addView(cellRv);

                cal.add(Calendar.DAY_OF_MONTH, 1);
            }
            rv.addView(rowRv);
        }

    }

}
