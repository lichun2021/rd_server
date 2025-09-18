package com.hawk.game.entity;

import org.hawk.annotation.IndexProp;
import org.hawk.db.HawkDBEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "xqhx")
public class PlayerXQHXEntity extends HawkDBEntity {
    @Id
    @Column(name = "playerId", nullable = false)
    @IndexProp(id = 1)
    private String playerId = "";

    @Column(name = "season")
    @IndexProp(id = 2)
    private int season;

    @Column(name = "usedPoint")
    @IndexProp(id = 3)
    private int usedPoint;

    @Column(name = "createTime", nullable = false)
    @IndexProp(id = 4)
    protected long createTime = 0;

    @Column(name = "updateTime")
    @IndexProp(id = 5)
    protected long updateTime = 0;

    @Column(name = "invalid")
    @IndexProp(id = 6)
    protected boolean invalid;

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public int getSeason() {
        return season;
    }

    public void setSeason(int season) {
        this.season = season;
    }

    public int getUsedPoint() {
        return usedPoint;
    }

    public void setUsedPoint(int usedPoint) {
        this.usedPoint = usedPoint;
    }

    @Override
    public String getPrimaryKey() {
        return playerId;
    }

    @Override
    public void setPrimaryKey(String primaryKey) {
        throw new UnsupportedOperationException("pay state entity primaryKey is playerId");
    }

    public String getOwnerKey() {
        return playerId;
    }

    @Override
    public long getCreateTime() {
        return createTime;
    }

    @Override
    protected void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    @Override
    public long getUpdateTime() {
        return updateTime;
    }

    @Override
    protected void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public boolean isInvalid() {
        return invalid;
    }

    @Override
    public void setInvalid(boolean invalid) {
        this.invalid = invalid;
    }
}
