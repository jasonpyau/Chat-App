package com.jasonpyau.chatapp.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateFormat {
    
    private DateFormat() {};

    public static String MMddyyyy() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy");
        String date = simpleDateFormat.format(new Date());
        return date;
    }

    public static Long getUnixTime() {
        return System.currentTimeMillis()/1000L;
    }
}
