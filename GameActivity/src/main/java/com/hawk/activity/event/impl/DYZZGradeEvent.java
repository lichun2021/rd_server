package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;
import com.hawk.activity.event.speciality.OrderEvent;

public class DYZZGradeEvent extends ActivityEvent implements OrderEvent {
    private int grade;
    private int seasonTerm;
    public DYZZGradeEvent(){ super(null);}
    public DYZZGradeEvent(String playerId, int grade, int seasonTerm) {
        super(playerId, true);
        this.grade = grade;
        this.seasonTerm = seasonTerm;
    }

    public int getGrade() {
        return grade;
    }

    public int getSeasonTerm() {
        return seasonTerm;
    }
}
