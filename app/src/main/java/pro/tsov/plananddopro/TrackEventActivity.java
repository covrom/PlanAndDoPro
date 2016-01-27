/*
 * Copyright (c) Roman Tsovanyan
 */

package pro.tsov.plananddopro;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;
import com.prolificinteractive.materialcalendarview.format.DateFormatTitleFormatter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Map.Entry;

public class TrackEventActivity extends AppCompatActivity implements OnDateSelectedListener, DayViewDecorator {

    public long currentRowId;
    public Date currentDay;
    private TrackRec trackrec;
    private MaterialCalendarView calendar;
    private EventDecorator decor_plan,decor_skip,decor_due,decor_cancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) return;

        setContentView(R.layout.activity_track_event);

        calendar = (MaterialCalendarView) findViewById(R.id.calendarView);
        calendar.setOnDateChangedListener(this);
        calendar.setTitleFormatter(new DateFormatTitleFormatter(new SimpleDateFormat(
                "MMM yyyy", Locale.getDefault()
        )));
        calendar.setSelectedDate(CalendarDay.today());
        calendar.addDecorator(this);
        calendar.setDateTextAppearance(R.style.TrackCalTextAppearance);

        currentDay = CalendarDay.today().getDate();

        decor_plan = new EventDecorator(this);
        decor_skip = new EventDecorator(this);
        decor_due = new EventDecorator(this);
        decor_cancel = new EventDecorator(this);

        OneDayDecorator decor_today = new OneDayDecorator();

        calendar.addDecorator(decor_plan);
        calendar.addDecorator(decor_skip);
        calendar.addDecorator(decor_due);
        calendar.addDecorator(decor_cancel);
        calendar.addDecorator(decor_today);

        Intent i = getIntent();
        currentRowId = i.getLongExtra(EditEventActivity.ACTION_EXTRA_EVENTID, -1);
        trackrec = new TrackRec(currentRowId);
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
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==2) refreshFromDB();
    }

    public void refreshDecorators(){

        PlanDoDBOpenHelper helper = PlanDoDBOpenHelper.getInstance(this);
        helper.readTrackToRec(trackrec);
        //0 - ничего
        ArrayList<CalendarDay> dates_plan = new ArrayList<>();  //1 и из будущего, включая сегодня
        ArrayList<CalendarDay> dates_skip = new ArrayList<>();  //1 и из прошлого
        ArrayList<CalendarDay> dates_due = new ArrayList<>();   //2 - выполнено
        ArrayList<CalendarDay> dates_cancel = new ArrayList<>();//3 - не выполнено

        for (Entry<Date,Integer> entry : trackrec.trackdates.entrySet()){
            int et = entry.getValue();
            if (et==1){
                CalendarDay dt = CalendarDay.from(entry.getKey());
                CalendarDay today = CalendarDay.from(new Date());
                if (today.isAfter(dt))
                    {dates_skip.add(dt);}
                else
                    {dates_plan.add(dt);}
            }
            else if (et==2){
                dates_due.add(CalendarDay.from(entry.getKey()));
            }
            else if (et==3){
                dates_cancel.add(CalendarDay.from(entry.getKey()));
            }
        }

        decor_plan.fillEventDecorator(ContextCompat.getColor(this,R.color.track_plan), dates_plan);
        decor_skip.fillEventDecorator(ContextCompat.getColor(this, R.color.track_skip), dates_skip);
        decor_due.fillEventDecorator(ContextCompat.getColor(this, R.color.track_due), dates_due);
        decor_cancel.fillEventDecorator(ContextCompat.getColor(this, R.color.track_cancel), dates_cancel);

        calendar.invalidateDecorators();
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
