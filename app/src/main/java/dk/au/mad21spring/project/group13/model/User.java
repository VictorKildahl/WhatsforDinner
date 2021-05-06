package dk.au.mad21spring.project.group13.model;

public class User {
    public String todaysDinner;
    public long timestamp;
    public String uid;

    public User(){}

    public User(String todaysDinner, long timestamp) {
        this.todaysDinner = todaysDinner;
        this.timestamp = timestamp;
    }

    public String getTodaysDinner() {
        return todaysDinner;
    }

    public void setTodaysDinner(String todaysDinner) {
        this.todaysDinner = todaysDinner;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
