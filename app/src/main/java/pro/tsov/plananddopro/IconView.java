package pro.tsov.plananddopro;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

public class IconView extends TextView {

    public static final String FONT = "flaticon.ttf";

    private static Typeface mFont;
    private String mIcon;

    /**
     * Returns the Typeface from the given context with the given name typeface
     *
     * @param context  Context to get the assets from
     * @param typeface name of the ttf file
     * @return Typeface from the given context with the given name
     */
    public static Typeface getTypeface(Context context, String typeface) {
        if (mFont == null) {
            mFont = Typeface.createFromAsset(context.getAssets(), typeface);
        }
        return mFont;
    }

    public IconView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        setTypeface(IconView.getTypeface(context, FONT));
        setText(mIcon);
    }

    public void setIcon(int iconResId) {
        setText(iconResId);
    }

    public void setIcon(String iconString) {
        setText(iconString);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthSize = getMeasuredWidth();
        int heightSize = getMeasuredHeight();
        int size = Math.max(widthSize, heightSize);
        setMeasuredDimension(size, size);
    }
}