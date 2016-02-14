package pro.tsov.plananddopro;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class EventCalendar extends LinearLayout implements TrackTextView.TrackTextViewListener {

    private Context context;
    private ArrayList<TrackTextView> caldays = new ArrayList<>();
    private EventCalendarListener listener;
    private PlanDoDBOpenHelper db;
    private SharedPreferences sp;
    private long currentRowId;

    @Override
    public void onCalendarElementChanged(TrackTextView trView) {
        build();
        listener.onEventCalendarChanged();
        invalidate();
    }

    public interface EventCalendarListener {
        public void onEventCalendarChanged();
    }

    public void setListener(EventCalendarListener listener) {
        this.listener = listener;
    }

    private void init(){

        db = PlanDoDBOpenHelper.getInstance(context);
        sp = PreferenceManager.getDefaultSharedPreferences(context);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View headerRowRv = inflater.inflate(R.layout.cal_track_row_header, null);
        DateFormatSymbols dfs = DateFormatSymbols.getInstance();
        String[] weekdays = dfs.getShortWeekdays();
        int[] selectdays = {Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY, Calendar.FRIDAY, Calendar.SATURDAY, Calendar.SUNDAY};
        for (int day = 0; day <= 6; day++) {
            View atCellHeader = inflater.inflate(R.layout.cal_track_cell_header, (ViewGroup) headerRowRv, false);
            TextView dayRv = (TextView) atCellHeader.findViewById(R.id.at_cell_header);
            dayRv.setText(weekdays[selectdays[day]]);
            ((ViewGroup) headerRowRv).addView(atCellHeader);
        }

        addView(headerRowRv);

        //создаем дни месяца - 6 недель по 7 дней
        for (int week = 0; week < 6; week++){
            View rowRv = inflater.inflate(R.layout.cal_track_row_week, null);
            for (int day = 0; day < 7; day++) {
                View cellRv = inflater.inflate(R.layout.cal_track_cell_day, (ViewGroup) rowRv, false);
                TrackTextView atDay = (TrackTextView) cellRv.findViewById(R.id.at_day);
                atDay.setListener(this);
                caldays.add(atDay);
                ((ViewGroup) rowRv).addView(cellRv);
            }
            addView(rowRv);
        }
    }

    public EventCalendar(Context context) {
        super(context);
        this.context = context;
        init();
    }

    public EventCalendar(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    public EventCalendar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init();
    }

    public void setCurrentRowId(long currentRowId) {
        this.currentRowId = currentRowId;
    }

    public void build(){
        TrackRec trackrec = new TrackRec(currentRowId);
        db.readTrackToRec(trackrec);

        Calendar cal = Calendar.getInstance();
        int thisMonth = sp.getInt(TrackEventFragment.PREF_MONTH, cal.get(Calendar.MONTH));
        int thisYear = sp.getInt(TrackEventFragment.PREF_YEAR, cal.get(Calendar.YEAR));
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.MONTH, thisMonth);
        cal.set(Calendar.YEAR, thisYear);

        //начало календаря, возможно из прошлого месяца
        int monthStartDayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        if (monthStartDayOfWeek == Calendar.SUNDAY)
            cal.add(Calendar.DAY_OF_MONTH, -6);
        else cal.add(Calendar.DAY_OF_MONTH, 2 - monthStartDayOfWeek);

        int posArrayList = 0;

        for (int week = 0; week < 6; week++) {

            int[] et = db.readWeekTrackForWidget(currentRowId, cal);
            int beforeEt = db.readBeforeWeekTrackForWidget(currentRowId, cal);
            int afterEt = db.readAfterWeekTrackForWidget(currentRowId, cal);
            boolean lastIs2 = beforeEt == 2;
            boolean nextIs2;

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

                TrackTextView atDay = caldays.get(posArrayList);
                atDay.setEventDate(cal.getTime());
                atDay.setEventType(et[day]);

                if (inMonth) {
                    atDay.setEnabledState(true);
                    atDay.setLeftConnected(lastIs2);
                    atDay.setRightConnected(nextIs2);
                    //atDay.setHasComment(true);
                    atDay.setCurrentRowId(currentRowId);

                    if (et[day] == 1) {
                        lastIs2 = false;
                    } else if (et[day] == 2) {
                        lastIs2 = true;
                    } else if (et[day] == 3) {
                        lastIs2 = false;
                    }

                }
                else {
                    atDay.setEnabledState(false);
                }

                atDay.build();
                atDay.invalidate();

                cal.add(Calendar.DAY_OF_MONTH, 1);
                posArrayList++;
            }

        }


    }

    public Date getSelectedDate(){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        int selYear = sp.getInt(TrackTextView.PREF_SELYEAR, 2000);
        int selDay = sp.getInt(TrackTextView.PREF_SELDAY, 1);

        if(selYear==2000&&selDay==1) return null;

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR,selYear);
        cal.set(Calendar.DAY_OF_YEAR, selDay);
        cal.set(Calendar.HOUR_OF_DAY,0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }
}
