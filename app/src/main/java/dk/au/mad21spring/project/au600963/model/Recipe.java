package dk.au.mad21spring.project.au600963.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity
public class Recipe implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private int uid;
    public String name;
    public String time;
    public String ingrediens;
    public String description;
    public String imgUrl;

    public Recipe(String name, String time, String ingrediens, String description, String imgUrl) {
        this.name = name;
        this.time = time;
        this.ingrediens = ingrediens;
        this.description = description;
        this.imgUrl = imgUrl;
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getIngrediens() {
        return ingrediens;
    }
    public void setIngrediens(String ingrediens) {
        this.ingrediens = ingrediens;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public String getImgUrl() {
        return imgUrl;
    }
    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }
}
