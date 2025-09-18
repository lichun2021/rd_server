package com.hawk.activity.type.impl.developFastOld.entity;

import com.hawk.game.protocol.Activity;
import com.hawk.serialize.string.DataArray;
import com.hawk.serialize.string.SplitEntity;

import java.util.List;

public class DevelopFastOldTask implements SplitEntity {
    private int taskId;
    /** 状态*/
    private int state;
    private int value;

    public DevelopFastOldTask(){

    }

    public static DevelopFastOldTask valueOf(int taskId){
        DevelopFastOldTask task = new DevelopFastOldTask();
        task.setTaskId(taskId);
        task.setState(Activity.DevelopFastTaskState.DF_NOT_REACH_VALUE);
        return task;
    }

    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    @Override
    public DevelopFastOldTask newInstance() {
        return new DevelopFastOldTask();
    }

    @Override
    public void serializeData(List<Object> dataList) {
        dataList.add(taskId);
        dataList.add(state);
        dataList.add(value);
    }

    @Override
    public void fullData(DataArray dataArray) {
        dataArray.setSize(3);
        taskId = dataArray.getInt();
        state = dataArray.getInt();
        value = dataArray.getInt();
    }

    @Override
    public String toString() {
        return "AchieveItem [taskId=" + taskId + ", state=" + state + ", value=" + value + "]";
    }

    public DevelopFastOldTask getCopy(){
        DevelopFastOldTask copy = new DevelopFastOldTask();
        copy.setTaskId(taskId);
        copy.setState(state);
        copy.setValue(value);
        return copy;
    }

    public Activity.DevelopFastTaskItem.Builder toPB(){
        Activity.DevelopFastTaskItem.Builder builder = Activity.DevelopFastTaskItem.newBuilder();
        builder.setId(this.getTaskId());
        builder.setState(Activity.DevelopFastTaskState.valueOf(this.getState()));
        builder.setValue(this.getValue());
        return builder;
    }
}
