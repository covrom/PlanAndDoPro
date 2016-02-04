package pro.tsov.plananddopro;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Date;


public class TrackTextView extends TextView {

    private Context context;
    private int eventType=0;
    private Date eventDate;
    private boolean enabledState=false;
    private boolean leftConnected = false;
    private boolean rightConnected = false;
    private boolean hasComment = false;
    private int todayDay;
    private int todayYear;
    private int monthDay;
    private boolean isToday;
    private boolean inFuture;

    public TrackTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setEventDate(new Date());
        this.context = context;
    }

    public TrackTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setEventDate(new Date());
        this.context = context;
    }

    public TrackTextView(Context context) {
        super(context);
        setEventDate(new Date());
        this.context = context;
    }

    public void setEventDate(Date eventDate) {
        this.eventDate = eventDate;

        Calendar cal = Calendar.getInstance();

        todayDay = cal.get(Calendar.DAY_OF_YEAR);
        todayYear = cal.get(Calendar.YEAR);

        cal.setTime(eventDate);

        monthDay = cal.get(Calendar.DAY_OF_MONTH);

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

    public void build(){
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
        if (hasComment) {
            Paint paint = new Paint();
            paint.setStrokeWidth(TrackFactory.convertDiptoPix(context,2));
            paint.setColor(ContextCompat.getColor(context, R.color.commentpoint));
            int off = TrackFactory.convertDiptoPix(context,8);
            int rad = TrackFactory.convertDiptoPix(context, 3);
            canvas.drawCircle(getWidth()-(int)(off*1.6),off,rad, paint);

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

            }else{
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
    }
}
