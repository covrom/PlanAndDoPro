package pro.tsov.plananddopro;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class TrackTextView extends TextView implements View.OnClickListener {

    private Context context;
    private int eventType=0;
    private Date eventDate;
    private boolean enabledState=false;
    private boolean leftConnected = false;
    private boolean rightConnected = false;
    private boolean hasComment = false;
    private int todayDay;
    private int todayYear;
    private int eventDay;
    private int eventYear;
    private int monthDay;
    private boolean isToday;
    private boolean inFuture;
    private boolean isSelected;
    private long currentRowId=-1;
    public static final String PREF_SELDAY = "pro.tsov.plananddopro.trackactivityselectedmonth";
    public static final String PREF_SELYEAR = "pro.tsov.plananddopro.trackactivityselectedyear";

    public TrackTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setEventDate(new Date());
        this.context = context;
        setOnClickListener(this);
    }

    public TrackTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setEventDate(new Date());
        this.context = context;
        setOnClickListener(this);
    }

    public TrackTextView(Context context) {
        super(context);
        setEventDate(new Date());
        this.context = context;
        setOnClickListener(this);
    }

    public void setEventDate(Date eventDate) {
        this.eventDate = eventDate;

        Calendar cal = Calendar.getInstance();

        todayDay = cal.get(Calendar.DAY_OF_YEAR);
        todayYear = cal.get(Calendar.YEAR);

        cal.setTime(eventDate);

        monthDay = cal.get(Calendar.DAY_OF_MONTH);
        eventDay = cal.get(Calendar.DAY_OF_YEAR);
        eventYear = cal.get(Calendar.YEAR);

        boolean inYear  = cal.get(Calendar.YEAR) == todayYear;
        isToday = inYear && (cal.get(Calendar.DAY_OF_YEAR) == todayDay);

        inFuture = cal.get(Calendar.YEAR) > todayYear;
        if (inYear) {
            inFuture = cal.get(Calendar.DAY_OF_YEAR) > todayDay;
        }
    }

    public void setEventType(int eventType) {
        this.eventType = eventType;
    }

    public void setEnabledState(boolean enabledState) {
        this.enabledState = enabledState;
    }

    public void setHasComment(boolean hasComment) {
        this.hasComment = hasComment;
    }

    public void setLeftConnected(boolean leftConnected) {
        this.leftConnected = leftConnected;
    }

    public void setRightConnected(boolean rightConnected) {
        this.rightConnected = rightConnected;
    }

    public void setCurrentRowId(long currentRowId) {
        this.currentRowId = currentRowId;
    }

    public void build(){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        int selYear = sp.getInt(PREF_SELYEAR, 2000);
        int selDay = sp.getInt(PREF_SELDAY, 1);
        isSelected = selYear == eventYear && selDay == eventDay;

        setText(Integer.toString(monthDay));

        if (enabledState) {
            Drawable drawable=null;
            if (eventType==1){
                //это был план, тогда меняем его на выполнено, если дата текущая или ранее, или очищаем, если дата в будущем
                if (inFuture||isToday)
                {
                    drawable = ContextCompat.getDrawable(context, R.drawable.w_back_plan).mutate();
                }
                else
                {
                    drawable = ContextCompat.getDrawable(context, R.drawable.w_back_skip).mutate();
                }
            }
            else if (eventType==2){
                drawable = ContextCompat.getDrawable(context, R.drawable.w_back_do).mutate();
            }
            else if (eventType==3){
                drawable = ContextCompat.getDrawable(context, R.drawable.w_back_cancel).mutate();
            }
            else{
                drawable = ContextCompat.getDrawable(context, R.drawable.w_back_empty).mutate();
            }
            setBackground(drawable);

            if (isToday)
                setTypeface(null, Typeface.BOLD);
            else
                setTypeface(null, Typeface.NORMAL);

        }
        else
            setBackground(null);

        setTextColor(ContextCompat.getColor(context, R.color.foreground_textday_black));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!enabledState) return;

        int off;
        int rad;
        Paint paint = new Paint();
        paint.setStrokeWidth(TrackFactory.convertDiptoPix(context, 2));

        if (hasComment||isSelected) {
            paint.setColor(ContextCompat.getColor(context, isSelected ? R.color.commentpointsel : R.color.commentpoint));

            off = TrackFactory.convertDiptoPix(context,8);
            rad = TrackFactory.convertDiptoPix(context, 3);
            canvas.drawCircle(getWidth()-(int)(off*1.6),off,rad, paint);
        }

        if (eventType==2){
            off = TrackFactory.convertDiptoPix(context,8);
            rad = TrackFactory.convertDiptoPix(context, 3);
            paint.setColor(ContextCompat.getColor(context, R.color.commentpoint));
            canvas.drawCircle(getWidth() - off, getHeight() / 2, rad, paint);
            canvas.drawCircle(off, getHeight() / 2, rad, paint);

            rad = TrackFactory.convertDiptoPix(context, 2);
            paint.setColor(ContextCompat.getColor(context, R.color.track_due_chain));
            canvas.drawCircle(getWidth() - off, getHeight() / 2, rad, paint);
            canvas.drawCircle(off,getHeight()/2,rad, paint);

            if(leftConnected){
                paint.setColor(ContextCompat.getColor(context, R.color.commentpoint));
                paint.setStrokeWidth(TrackFactory.convertDiptoPix(context, 2));
                canvas.drawLine(0, getHeight() / 2, off, getHeight() / 2, paint);
                paint.setColor(ContextCompat.getColor(context, R.color.track_due_chain));
                paint.setStrokeWidth(TrackFactory.convertDiptoPix(context, 1));
                canvas.drawLine(0, getHeight() / 2, off, getHeight() / 2, paint);
            }
            if(rightConnected){
                paint.setColor(ContextCompat.getColor(context, R.color.commentpoint));
                paint.setStrokeWidth(TrackFactory.convertDiptoPix(context, 2));
                canvas.drawLine(getWidth() - off, getHeight() / 2, getWidth(), getHeight() / 2, paint);
                paint.setColor(ContextCompat.getColor(context, R.color.track_due_chain));
                paint.setStrokeWidth(TrackFactory.convertDiptoPix(context, 1));
                canvas.drawLine(getWidth() - off, getHeight() / 2, getWidth(), getHeight() / 2, paint);
            }

        }else if (!(eventType == 1|| eventType == 3)){
            if(leftConnected&&rightConnected){
                paint.setColor(ContextCompat.getColor(context, R.color.commentpoint));
                paint.setStrokeWidth(TrackFactory.convertDiptoPix(context, 2));
                canvas.drawLine(0, getHeight() / 2, getWidth(), getHeight() / 2, paint);
                paint.setColor(ContextCompat.getColor(context, R.color.track_due_chain));
                paint.setStrokeWidth(TrackFactory.convertDiptoPix(context, 1));
                canvas.drawLine(0, getHeight() / 2, getWidth(), getHeight() / 2, paint);
            }
        }

    }

    private void makeCurrPlan() {
        PlanDoDBOpenHelper helper = PlanDoDBOpenHelper.getInstance(context);
        helper.updateTrackEventOnDate(currentRowId, eventDate, 1);
//        eventType = 1;
        sendRefreshWidget(context);
        ShowToast(context, R.string.to_planned, eventDate);
//        build();
//        invalidate();
    }

    private void makeCurrNoExec() {
        PlanDoDBOpenHelper helper = PlanDoDBOpenHelper.getInstance(context);
        helper.updateTrackEventOnDate(currentRowId, eventDate, 3);
//        eventType = 3;
        sendRefreshWidget(context);
        ShowToast(context, R.string.to_cancel, eventDate);
//        build();
//        invalidate();
    }

    private void makeCurrExec() {
        PlanDoDBOpenHelper helper = PlanDoDBOpenHelper.getInstance(context);
        helper.updateTrackEventOnDate(currentRowId, eventDate, 2);
//        eventType = 2;
        sendRefreshWidget(context);
        ShowToast(context, R.string.to_due, eventDate);
//        build();
//        invalidate();
    }

    public void deleteCurrRec() {
        //удаляем запись
        PlanDoDBOpenHelper helper = PlanDoDBOpenHelper.getInstance(context);
        helper.delTrackEventOnDate(currentRowId, eventDate);
//        eventType = 0;
        sendRefreshWidget(context);
        ShowToast(context, R.string.to_clean, eventDate);
//        build();
//        invalidate();
    }

    private void ShowToast(Context ctx, int resID, Date day) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd");
        String ttx = ctx.getResources().getString(resID) + " " + sdf.format(day);
        Toast tt = Toast.makeText(ctx, ttx, Toast.LENGTH_SHORT);
        tt.setGravity(Gravity.BOTTOM, 0, 8);
        tt.show();
    }

    public static void sendRefreshWidget(Context ctx) {
        Intent intent = new Intent(ctx, TrackWidget.class);
        intent.setAction(TrackWidget.ACTION_REFRESH);
        ctx.sendBroadcast(intent);
        LocalBroadcastManager.getInstance(ctx).sendBroadcast(intent);
        TrackAlarmReceiver.sendActionOverAlarm(ctx, 5000, false);
    }

    @Override
    public void onClick(View v) {
        if (!enabledState) return;

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        int selYear = sp.getInt(PREF_SELYEAR, 2000);
        int selDay = sp.getInt(PREF_SELDAY, 1);

        //если дата не равна выбранной, то ничего не делаем (показываем коммент), иначе меняем статус по алгоритму
        if (!(selYear==eventYear && selDay==eventDay)){
            sp.edit()
                    .putInt(PREF_SELYEAR, eventYear)
                    .putInt(PREF_SELDAY, eventDay)
                    .apply();
            Intent intent = new Intent(context, TrackWidget.class);
            intent.setAction(TrackWidget.ACTION_REFRESH);
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        }
        else {
            if (eventType == 1) {
                //это был план, тогда меняем его на выполнено, если дата текущая или ранее, или очищаем, если дата в будущем
                if (inFuture) {
                    deleteCurrRec();
                } else {
                    makeCurrExec();
                }
            } else if (eventType == 2) {
                //это было выполнено, теперь меняем на не выполнено
                makeCurrNoExec();
            } else if (eventType == 3) {
                //это было не выполнено - очищаем
                deleteCurrRec();
            } else {
                //если ничего не было, то стало запланировано, даже если пропущено в прошлом
                makeCurrPlan();
            }
        }

    }
}
