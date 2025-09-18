package com.hawk.activity.type.impl.changeServer.action;

public class ChangeSvrActivityAction {
    private ChangeSvrActivityActionType type;
    private String from;
    private String to;
    public ChangeSvrActivityAction(ChangeSvrActivityActionType type,String from,String to){
        this.type = type;
        this.from = from;
        this.to = to;
    }

    public ChangeSvrActivityActionType getType() {
        return type;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }
}
