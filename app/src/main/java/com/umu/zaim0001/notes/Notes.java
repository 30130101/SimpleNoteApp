package com.umu.zaim0001.notes;

//model class of a note

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class Notes {
    private String name;
    private String text;
    private String dateTime;
    private String location;

    public Notes(String name, String text, String location){
        this.name=name;
        this.text=text;
        this.location = location;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault());
        this.dateTime = sdf.format(new Date());
    }

    public String getName() {
        return name;
    }
    public String getNote(){
        return (text);
    }

    public String getDateTime() {
        return dateTime;
    }

    public String getLocation() { return location; }


}
