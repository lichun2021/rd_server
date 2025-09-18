package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

public class NewStartActiveEvent extends ActivityEvent {
    private String name;
    private int icon;
    private String pfIcon;
    private String oldPlayerId;
    private String oldServerId;
    private int playerLevel;
    private int vipLevel;
    private int baseLevel;
    private int heroCount;
    private int equipTechLevel;
    private int jijiaLevel;

    public NewStartActiveEvent(){ super(null);}

    public NewStartActiveEvent(String playerId, String name, int icon, String pfIcon, String oldPlayerId, String oldServerId,
                               int playerLevel, int vipLevel, int baseLevel, int heroCount, int equipTechLevel, int jijiaLevel) {
        super(playerId, true);
        this.name = name;
        this.icon = icon;
        this.pfIcon = pfIcon;
        this.oldPlayerId = oldPlayerId;
        this.oldServerId = oldServerId;
        this.playerLevel = playerLevel;
        this.vipLevel = vipLevel;
        this.baseLevel = baseLevel;
        this.heroCount = heroCount;
        this.equipTechLevel = equipTechLevel;
        this.jijiaLevel = jijiaLevel;
    }

    public String getName() {
        return name;
    }

    public int getIcon() {
        return icon;
    }

    public String getPfIcon() {
        return pfIcon;
    }

    public String getOldPlayerId() {
        return oldPlayerId;
    }

    public String getOldServerId() {
        return oldServerId;
    }

    public int getPlayerLevel() {
        return playerLevel;
    }

    public int getVipLevel() {
        return vipLevel;
    }

    public int getBaseLevel() {
        return baseLevel;
    }

    public int getHeroCount() {
        return heroCount;
    }

    public int getEquipTechLevel() {
        return equipTechLevel;
    }

    public int getJijiaLevel() {
        return jijiaLevel;
    }
}
