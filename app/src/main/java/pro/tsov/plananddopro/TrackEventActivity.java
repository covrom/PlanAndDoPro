/*
 * Copyright (c) Roman Tsovanyan
 */

package pro.tsov.plananddopro;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class TrackEventActivity extends AppCompatActivity implements OnDateSelectedListener, DayViewDecorator, View.OnClickListener {

    public long currentRowId;
    public Date currentDay;
    private TrackRec trackrec;
    private MaterialCalendarView calendar;
    private EventDecorator decor_plan,decor_skip,decor_due,decor_cancel;

    private static final String PREF_MONTH = "pro.tsov.plananddopro.trackactivitymonth";
    private static final String PREF_YEAR = "pro.tsov.plananddopro.trackactivityyear";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) return;

        setContentView(R.layout.activity_track_event);

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




//        calendar = (MaterialCalendarView) findViewById(R.id.calendarView);
//        calendar.setOnDateChangedListener(this);
//        calendar.setTitleFormatter(new DateFormatTitleFormatter(new SimpleDateFormat(
//                "MMMM yyyy", Locale.getDefault()
//        )));
//        calendar.setSelectedDate(CalendarDay.today());
//        calendar.addDecorator(this);
//        calendar.setDateTextAppearance(R.style.TrackCalTextAppearance);
//
//        //currentDay = CalendarDay.today().getDate();
//
//        decor_plan = new EventDecorator(this);
//        decor_skip = new EventDecorator(this);
//        decor_due = new EventDecorator(this);
//        decor_cancel = new EventDecorator(this);
//
//        OneDayDecorator decor_today = new OneDayDecorator();
//
//        calendar.addDecorator(decor_plan);
//        calendar.addDecorator(decor_skip);
//        calendar.addDecorator(decor_due);
//        calendar.addDecorator(decor_cancel);
//        calendar.addDecorator(decor_today);


        Intent i = getIntent();
        currentRowId = i.getLongExtra(EditEventActivity.ACTION_EXTRA_EVENTID, -1);
        trackrec = new TrackRec(currentRowId);
        refreshFromDB();

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

        private void refreshFromDB(){
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_track, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.track_toeditor){
            Intent i = new Intent(this,EditEventActivity.class);
            i.putExtra(EditEventActivity.ACTION_EXTRA_EVENTID, currentRowId);//новый
            startActivityForResult(i, 2);
        }
        if (id == R.id.track_sendtabbed) {
            TrackRec tr = new TrackRec(currentRowId);
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, tr.getTabbedText(PlanDoDBOpenHelper.getInstance(this)));
            sendIntent.setType("text/plain");
            startActivity(sendIntent);
        }
        return super.onOptionsItemSelected(item);
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
        int today = cal.get(Calendar.DAY_OF_YEAR);
        int todayYear = cal.get(Calendar.YEAR);
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
                boolean inYear = cal.get(Calendar.YEAR) == todayYear;
                boolean isToday = inYear && inMonth && (cal.get(Calendar.DAY_OF_YEAR) == today);

                boolean inFuture = cal.get(Calendar.YEAR) > todayYear;
                if (inYear) {
                    inFuture = cal.get(Calendar.DAY_OF_YEAR) > today;
                }

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
                TextView atDay = (TextView) cellRv.findViewById(R.id.at_day);
                atDay.setText(Integer.toString(cal.get(Calendar.DAY_OF_MONTH)));

                if (isToday) {
                    atDay.setTextColor(ContextCompat.getColor(this, R.color.foreground_textday_black));
                    atDay.setTypeface(null, Typeface.BOLD);
                } else if (inMonth) {

                    atDay.setTextColor(ContextCompat.getColor(this, R.color.foreground_textday_black));

                    if (et[day] == 1) {
                        //это был план, тогда меняем его на выполнено, если дата текущая или ранее, или очищаем, если дата в будущем
                        if (inFuture || isToday) {
                            atDay.setBackgroundResource(R.drawable.w_back_plan);
                        } else {
                            atDay.setBackgroundResource(R.drawable.w_back_skip);
                        }
                        lastIs2 = false;
                    } else if (et[day] == 2) {

                        if (lastIs2 && nextIs2)
                            atDay.setBackgroundResource(R.drawable.w_back_due);
                        else if (lastIs2 && !nextIs2)
                            atDay.setBackgroundResource(R.drawable.w_back_due_noright);
                        else if (!lastIs2 && nextIs2)
                            atDay.setBackgroundResource(R.drawable.w_back_due_noleft);
                        else if (!lastIs2 && !nextIs2)
                            atDay.setBackgroundResource(R.drawable.w_back_due_noall);

                        lastIs2 = true;
                    } else if (et[day] == 3) {
                        atDay.setBackgroundResource(R.drawable.w_back_cancel);
                        lastIs2 = false;
                    } else {
                        if (lastIs2 && nextIs2)
                            atDay.setBackgroundResource(R.drawable.w_back_empty_chain);
                        else
                            atDay.setBackgroundResource(R.drawable.w_back_empty);
                    }

                }

                ((ViewGroup)rowRv).addView(cellRv);

                cal.add(Calendar.DAY_OF_MONTH, 1);
            }
            rv.addView(rowRv);
        }


//        //0 - ничего
//        ArrayList<CalendarDay> dates_plan = new ArrayList<>();  //1 и из будущего, включая сегодня
//        ArrayList<CalendarDay> dates_skip = new ArrayList<>();  //1 и из прошлого
//        ArrayList<CalendarDay> dates_due = new ArrayList<>();   //2 - выполнено
//        ArrayList<CalendarDay> dates_cancel = new ArrayList<>();//3 - не выполнено
//
//        for (Entry<Date,Integer> entry : trackrec.trackdates.entrySet()){
//            int et = entry.getValue();
//            if (et==1){
//                CalendarDay dt = CalendarDay.from(entry.getKey());
//                CalendarDay today = CalendarDay.from(new Date());
//                if (today.isAfter(dt))
//                    {dates_skip.add(dt);}
//                else
//                    {dates_plan.add(dt);}
//            }
//            else if (et==2){
//                dates_due.add(CalendarDay.from(entry.getKey()));
//            }
//            else if (et==3){
//                dates_cancel.add(CalendarDay.from(entry.getKey()));
//            }
//        }
//
//        decor_plan.fillEventDecorator(ContextCompat.getColor(this,R.color.track_plan), dates_plan);
//        decor_skip.fillEventDecorator(ContextCompat.getColor(this, R.color.track_skip), dates_skip);
//        decor_due.fillEventDecorator(ContextCompat.getColor(this, R.color.track_due), dates_due);
//        decor_cancel.fillEventDecorator(ContextCompat.getColor(this, R.color.track_cancel), dates_cancel);
//
//        calendar.invalidateDecorators();


    }


    @Override
    public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
        //If you change a decorate, you need to invalidate decorators
        //если дата не равна выбранной, то ничего не делаем, иначе меняем статус по алгоритму
        Date selDate = date.getDate();

        CalendarDay selCalDate = CalendarDay.from(selDate);
        CalendarDay today = CalendarDay.from(new Date());

//        if (!currentDay.equals(selDate)){
            currentDay = selDate;
//        }else{
            if (decor_plan.containDate(selDate)){
                //это был план, тогда меняем его на выполнено, если дата текущая или ранее, или очищаем, если дата в будущем
                if(today.isBefore(selCalDate)){
                    deleteCurrRec();
                }else{
                    makeCurrExec();
                }
            }
            else if(decor_skip.containDate(selDate)){
                //это был пропущенный план, он становится выполнен
                makeCurrExec();
            }
            else if(decor_due.containDate(selDate)){
                //это было выполнено, теперь меняем на не выполнено
                makeCurrNoExec();
            }
            else if(decor_cancel.containDate(selDate)){
                //это было не выполнено - очищаем
                deleteCurrRec();
            }
            else{
                //если ничего не было, то стало запланировано, даже если пропущено в прошлом
                makeCurrPlan();
            }
//        }
    }

    private void makeCurrPlan() {
        PlanDoDBOpenHelper helper = PlanDoDBOpenHelper.getInstance(this);
        helper.updateTrackEventOnDate(currentRowId, currentDay, 1);
        sendRefreshWidget(this);
        refreshDecorators();
        ShowToast(calendar.getContext(), R.string.to_planned, currentDay);
    }

    private void makeCurrNoExec() {
        PlanDoDBOpenHelper helper = PlanDoDBOpenHelper.getInstance(this);
        helper.updateTrackEventOnDate(currentRowId, currentDay, 3);
        sendRefreshWidget(this);
        refreshDecorators();
        ShowToast(calendar.getContext(), R.string.to_cancel, currentDay);
    }

    private void makeCurrExec() {
        PlanDoDBOpenHelper helper = PlanDoDBOpenHelper.getInstance(this);
        helper.updateTrackEventOnDate(currentRowId, currentDay, 2);
        sendRefreshWidget(this);
        refreshDecorators();
        ShowToast(calendar.getContext(), R.string.to_due, currentDay);
    }

    public void deleteCurrRec(){
        //удаляем запись
        PlanDoDBOpenHelper helper = PlanDoDBOpenHelper.getInstance(this);
        helper.delTrackEventOnDate(currentRowId, currentDay);
        sendRefreshWidget(this);
        refreshDecorators();
        ShowToast(calendar.getContext(), R.string.to_clean, currentDay);
    }

    private void ShowToast(Context context, int resID, Date day) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd");
        String ttx = context.getResources().getString(resID) + " " + sdf.format(day);
        Toast tt = Toast.makeText(context, ttx, Toast.LENGTH_SHORT);
        tt.setGravity(Gravity.BOTTOM, 0, 8);
        tt.show();
    }

    public static void sendRefreshWidget(Context ctx){
        Intent intent = new Intent(ctx, TrackWidget.class);
        intent.setAction(TrackWidget.ACTION_REFRESH);
        ctx.sendBroadcast(intent);
        TrackAlarmReceiver.sendActionOverAlarm(ctx,5000,false);
    }


    @Override
    public boolean shouldDecorate(CalendarDay day) {
        return true;
    }

    @Override
    public void decorate(DayViewFacade view) {
        view.setSelectionDrawable(ContextCompat.getDrawable(this, R.drawable.my_selector));
    }

}
