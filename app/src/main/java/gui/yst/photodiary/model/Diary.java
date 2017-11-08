package gui.yst.photodiary.model;

import java.io.Serializable;

public class Diary implements Serializable{
    private String id;
    private String title;
    private long datetime;
    private String location;
    private String comment;

    public Diary(){

    }

    public Diary(String id, String title, long datetime, String location, String comment){
        this.id = id;
        this.title = title;
        this.datetime = datetime;
        this.location = location;
        this.comment = comment;
    }

    public Diary(String title, long datetime, String location, String comment){
        this.title = title;
        this.datetime = datetime;
        this.location = location;
        this.comment = comment;
    }

    public String getId(){
        return this.id;
    }

    public void setId(String id){
        this.id = id;
    }

    public String getTitle(){
        return this.title;
    }

    public void setTitle(String title){
        this.title = title;
    }

    public long getDatetime(){
        return this.datetime;
    }

    public void setDatetime(long datetime){
        this.datetime = datetime;
    }

    public String getLocation(){
        return this.location;
    }

    public void setLocation(String location){
        this.location = location;
    }

    public String getComment(){
        return this.comment;
    }

    public void setComment(String comment){
        this.comment = comment;
    }
}
