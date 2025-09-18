package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;
import com.hawk.activity.event.speciality.OrderEvent;

public class CWGradeEvent extends ActivityEvent implements OrderEvent {
    private int grade;

    public CWGradeEvent(){ super(null);}
    public CWGradeEvent(String playerId, int grade) {
        super(playerId, true);
        this.grade = grade;
    }

    public int getGrade() {
        return grade;
    }
}
