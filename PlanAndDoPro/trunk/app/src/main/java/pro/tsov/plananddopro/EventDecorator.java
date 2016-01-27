/*
 * Copyright (c) Roman Tsovanyan
 */

package pro.tsov.plananddopro;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.content.ContextCompat;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;

public class EventDecorator implements DayViewDecorator {

    private int color = Color.WHITE;
    public HashSet<CalendarDay> dates = new HashSet<>();
    private Context context;

    public EventDecorator(Context ctx){
        super();
        context = ctx;
    }

    public void fillEventDecorator(int color, Collection<CalendarDay> dates) {
        this.color = color;
        this.dates = new HashSet<>(dates);
    }

    @Override
    public boolean shouldDecorate(CalendarDay day) {

        return dates.contains(day);
    }

    @Override
    public void decorate(DayViewFacade view) {

        //view.addSpan(new DotSpan(20, color));
        GradientDrawable drawable = (GradientDrawable) ContextCompat.getDrawable(context,R.drawable.w_back_empty).mutate();
        drawable.setColor(color);
        view.setBackgroundDrawable(drawable);

    }

    public boolean containDate(Date dt) {
        return dates.contains(CalendarDay.from(dt));
    }
}
