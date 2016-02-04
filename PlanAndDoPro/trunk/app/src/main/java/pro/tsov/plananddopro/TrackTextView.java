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

                if (leftConnected && rightConnected)
                    drawable = ContextCompat.getDrawable(context, R.drawable.w_back_due).mutate();
                else if (leftConnected && !rightConnected)
                    drawable = ContextCompat.getDrawable(context, R.drawable.w_back_due_noright).mutate();
                else if (!leftConnected && rightConnected)
                    drawable = ContextCompat.getDrawable(context, R.drawable.w_back_due_noleft).mutate();
                else if (!leftConnected && !rightConnected)
                    drawable = ContextCompat.getDrawable(context, R.drawable.w_back_due_noall).mutate();
            }
            else if (eventType==3){
                drawable = ContextCompat.getDrawable(context, R.drawable.w_back_cancel).mutate();
            }
            else{
                if(leftConnected && rightConnected)
                    drawable = ContextCompat.getDrawable(context, R.drawable.w_back_empty_chain).mutate();
                else
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
            paint.setColor(ContextCompat.getColor(context, R.color.commentpoint));
            int off = TrackFactory.convertDiptoPix(context,8);
            int rad = TrackFactory.convertDiptoPix(context, 4);
            canvas.drawCircle(getWidth()-off,off,rad, paint);
        }
    }
}
