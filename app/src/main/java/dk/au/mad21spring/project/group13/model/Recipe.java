package dk.au.mad21spring.project.group13.model;

import java.io.Serializable;


public class Recipe implements Serializable {
    public String uid;
    public String name;
    public String time;
    public String ingrediens;
    public String instruction;
    public String description;
    public String imgUrl;

    public Recipe(){}

    public Recipe(String name, String time, String ingrediens, String instruction, String description, String imgUrl) {
        this.name = name;
        this.time = time;
        this.ingrediens = ingrediens;
        this.instruction = instruction;
        this.description = description;
        this.imgUrl = imgUrl;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
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

    public String getInstruction() {
        return instruction;
    }
    public void setInstruction(String instruction) {
        this.instruction = instruction;
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
