/*
 * Copyright (c) Roman Tsovanyan
 */

package pro.tsov.plananddopro;

import java.util.Date;
import java.util.HashMap;

public class TrackRec {
    public long eventId;
    public HashMap<Date,Integer> trackdates;
    public HashMap<Date,String> trackcomments;

    public TrackRec (long eventId){
        this.eventId = eventId;
        trackdates = new HashMap<>();
        trackcomments = new HashMap<>();
    }
}
