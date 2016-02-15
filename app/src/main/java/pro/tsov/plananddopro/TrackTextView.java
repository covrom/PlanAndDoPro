package pro.tsov.plananddopro;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.TypedValue;
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
    private TrackTextViewListener listener;
    private int widgetBitmapWidth;

    public interface TrackTextViewListener{ public void onCalendarElementChanged(TrackTextView trView);}

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

    public void setListener(TrackTextViewListener listener) {
        this.listener = listener;
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

    public int getWeekRow(){
        //0-5
        Calendar cal = Calendar.getInstance();
        cal.setFirstDayOfWeek(Calendar.MONDAY);
        cal.setTime(this.eventDate);
        return cal.get(Calendar.WEEK_OF_MONTH)-1;
    }

    public int getDayColumn() {
        //0-6
        Calendar cal = Calendar.getInstance();
        cal.setTime(this.eventDate);
        if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) return 6;
        else return cal.get(Calendar.DAY_OF_WEEK)-1;
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

    public void setWidgetBitmapWidth(int widgetBitmapWidth) {
        this.widgetBitmapWidth = widgetBitmapWidth;
    }

    public void build(){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        int selYear = sp.getInt(PREF_SELYEAR, 2000);
        int selDay = sp.getInt(PREF_SELDAY, 1);
        isSelected = selYear == eventYear && selDay == eventDay;

        if (enabledState) setText(Integer.toString(monthDay));
        else setText("");

        if (enabledState) {
            Drawable drawable=null;
            if (eventType==1){
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

    public void cnvDraw(Canvas canvas,int width,int height){
        int off;
        int rad;
        Paint paint = new Paint();
        Paint wiredPaint = new Paint();
        paint.setAntiAlias(true);
//        paint.setSubpixelText(true);
        paint.setStrokeWidth(convertDiptoPix(context, 2));

        if (hasComment) {
            paint.setColor(ContextCompat.getColor(context, R.color.commentpoint));

            off = convertDiptoPix(context, 8);
            rad = convertDiptoPix(context, 3);
            canvas.drawCircle(width - (int) (off * 1.6), off, rad, paint);
        }
        if (isSelected) {
            wiredPaint.setColor(ContextCompat.getColor(context, R.color.commentpointsel));
            wiredPaint.setStyle(Paint.Style.STROKE);
            wiredPaint.setStrokeWidth(convertDiptoPix(context, 2));
            off = convertDiptoPix(context, 4);
            canvas.drawRect(off, off, width - off, height - off, wiredPaint);
        }

        if (eventType == 2) {
            off = convertDiptoPix(context, 8);
            rad = convertDiptoPix(context, 3);
            paint.setColor(ContextCompat.getColor(context, R.color.commentpoint));
            canvas.drawCircle(width - off, height / 2, rad, paint);
            canvas.drawCircle(off, height / 2, rad, paint);

            rad = convertDiptoPix(context, 2);
            paint.setColor(ContextCompat.getColor(context, R.color.track_due_chain));
            canvas.drawCircle(width - off, height / 2, rad, paint);
            canvas.drawCircle(off, height / 2, rad, paint);

            if (leftConnected) {
                paint.setColor(ContextCompat.getColor(context, R.color.commentpoint));
                paint.setStrokeWidth(convertDiptoPix(context, 2));
                canvas.drawLine(0, height / 2, off, height / 2, paint);
                paint.setColor(ContextCompat.getColor(context, R.color.track_due_chain));
                paint.setStrokeWidth(convertDiptoPix(context, 1));
                canvas.drawLine(0, height / 2, off, height / 2, paint);
            }
            if (rightConnected) {
                paint.setColor(ContextCompat.getColor(context, R.color.commentpoint));
                paint.setStrokeWidth(convertDiptoPix(context, 2));
                canvas.drawLine(width - off, height / 2, width, height / 2, paint);
                paint.setColor(ContextCompat.getColor(context, R.color.track_due_chain));
                paint.setStrokeWidth(convertDiptoPix(context, 1));
                canvas.drawLine(width - off, height / 2, width, height / 2, paint);
            }

        } else if (!(eventType == 1 || eventType == 3)) {
            if (leftConnected && rightConnected) {
                paint.setColor(ContextCompat.getColor(context, R.color.commentpoint));
                paint.setStrokeWidth(convertDiptoPix(context, 2));
                canvas.drawLine(0, height / 2, width, height / 2, paint);
                paint.setColor(ContextCompat.getColor(context, R.color.track_due_chain));
                paint.setStrokeWidth(convertDiptoPix(context, 1));
                canvas.drawLine(0, height / 2, width, height / 2, paint);
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!enabledState) return;
        cnvDraw(canvas, canvas.getWidth(), canvas.getHeight());
    }

    public Bitmap buildForWidget() {

        int width = convertDiptoPix(context, widgetBitmapWidth);
        int height = convertDiptoPix(context, 30);

        Bitmap myBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas myCanvas = new Canvas(myBitmap);

        Drawable drawable = null;
        if (eventType == 1) {
            if (inFuture || isToday) {
                drawable = ContextCompat.getDrawable(context, R.drawable.w_back_plan).mutate();
            } else {
                drawable = ContextCompat.getDrawable(context, R.drawable.w_back_skip).mutate();
            }
        } else if (eventType == 2) {
            if (leftConnected && rightConnected)
                drawable = ContextCompat.getDrawable(context, R.drawable.w_back_due).mutate();
            else if (leftConnected && !rightConnected)
                drawable = ContextCompat.getDrawable(context, R.drawable.w_back_due_noright).mutate();
            else if (!leftConnected && rightConnected)
                drawable = ContextCompat.getDrawable(context, R.drawable.w_back_due_noleft).mutate();
            else if (!leftConnected && !rightConnected)
                drawable = ContextCompat.getDrawable(context, R.drawable.w_back_due_noall).mutate();
        } else if (eventType == 3) {
            drawable = ContextCompat.getDrawable(context, R.drawable.w_back_cancel).mutate();
        } else {
                if(leftConnected && rightConnected)
                    drawable = ContextCompat.getDrawable(context, R.drawable.w_back_empty_chain).mutate();
                else
                    drawable = ContextCompat.getDrawable(context, R.drawable.w_back_empty).mutate();
        }
        drawable.setBounds(0,0,width,height);
        drawable.draw(myCanvas);

        float fontSizeSP = 18;
        int fontSizePX = convertDiptoPix(context, fontSizeSP);

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setSubpixelText(true);
        paint.setTypeface(Typeface.create("sans-serif-condensed", Typeface.NORMAL));
        paint.setTextSize(fontSizePX);
        if (isToday)
            paint.setColor(ContextCompat.getColor(context, R.color.foreground_today));
        else
            paint.setColor(ContextCompat.getColor(context, R.color.foreground_textday));

        String daytxt = Integer.toString(monthDay);
        Rect bounds = new Rect();
        paint.getTextBounds(daytxt,0,daytxt.length(),bounds);
        int textWidth = bounds.width();
        int textHeight = bounds.height();
        myCanvas.drawText(daytxt, (width - textWidth) / 2, height - (height - textHeight) / 2, paint);

        int off;
        int rad;
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStrokeWidth(convertDiptoPix(context, 2));

        if (hasComment) {
            paint.setColor(ContextCompat.getColor(context, R.color.commentpoint));

            off = convertDiptoPix(context, 4);
            rad = convertDiptoPix(context, 3);
            myCanvas.drawCircle(width - (int) (off * 1.6), off, rad, paint);
        }

        return myBitmap;
    }

    private void makeCurrPlan() {
        PlanDoDBOpenHelper helper = PlanDoDBOpenHelper.getInstance(context);
        TrackRec trc = new TrackRec(currentRowId);
        helper.readTrackToRec(trc);
        helper.updateTrackEventOnDate(currentRowId, eventDate, 1,trc.trackcomments.get(EventCalendar.roundDate(eventDate)));
//        eventType = 1;
        sendRefreshWidget();
        ShowToast(context, R.string.to_planned, eventDate);
//        build();
//        invalidate();
    }

    private void makeCurrNoExec() {
        PlanDoDBOpenHelper helper = PlanDoDBOpenHelper.getInstance(context);
        TrackRec trc = new TrackRec(currentRowId);
        helper.readTrackToRec(trc);
        helper.updateTrackEventOnDate(currentRowId, eventDate, 3, trc.trackcomments.get(EventCalendar.roundDate(eventDate)));
//        eventType = 3;
        sendRefreshWidget();
        ShowToast(context, R.string.to_cancel, eventDate);
//        build();
//        invalidate();
    }

    private void makeCurrExec() {
        PlanDoDBOpenHelper helper = PlanDoDBOpenHelper.getInstance(context);
        TrackRec trc = new TrackRec(currentRowId);
        helper.readTrackToRec(trc);
        helper.updateTrackEventOnDate(currentRowId, eventDate, 2, trc.trackcomments.get(EventCalendar.roundDate(eventDate)));
//        eventType = 2;
        sendRefreshWidget();
        ShowToast(context, R.string.to_due, eventDate);
//        build();
//        invalidate();
    }

    public void deleteCurrRec() {
        //удаляем запись
        PlanDoDBOpenHelper helper = PlanDoDBOpenHelper.getInstance(context);
        helper.delTrackEventOnDate(currentRowId, eventDate);
//        eventType = 0;
        sendRefreshWidget();
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

    public void sendRefreshWidget() {
        Intent intent = new Intent(context, TrackWidget.class);
        intent.setAction(TrackWidget.ACTION_REFRESH);
        context.sendBroadcast(intent);
        TrackAlarmReceiver.sendActionOverAlarm(context, 5000, false);
        listener.onCalendarElementChanged(this);
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
//            Intent intent = new Intent(context, TrackWidget.class);
//            intent.setAction(TrackWidget.ACTION_REFRESH);
//            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
            listener.onCalendarElementChanged(this);
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

    public static int convertDiptoPix(Context context, float dip) {
        int value = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, context.getResources().getDisplayMetrics());
        return value;
    }

}
