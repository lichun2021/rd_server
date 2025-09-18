package com.hawk.activity.type.impl.changeServer.entity;

import com.hawk.activity.type.IActivityDataEntity;
import org.hawk.db.HawkDBEntity;
import org.hibernate.annotations.GenericGenerator;

import org.hawk.annotation.IndexProp;
import javax.persistence.*;

@Entity
@Table(name = "activity_change_server")
public class ChangeServerEntity extends HawkDBEntity implements IActivityDataEntity {
    @Id
    @GenericGenerator(name = "uuid", strategy = "org.hawk.uuid.HawkUUIDGenerator")
    @GeneratedValue(generator = "uuid")
    @IndexProp(id = 1)
    @Column(name = "id", unique = true, nullable = false)
    private String id;

    @IndexProp(id = 2)
    @Column(name = "playerId", nullable = false)
    private String playerId = null;

    @IndexProp(id = 3)
    @Column(name = "termId", nullable = false)
    private int termId;

    @IndexProp(id = 4)
    @Column(name = "createTime", nullable = false)
    private long createTime;

    @IndexProp(id = 5)
    @Column(name = "updateTime", nullable = false)
    private long updateTime;

    @IndexProp(id = 6)
    @Column(name = "invalid", nullable = false)
    private boolean invalid;

    @IndexProp(id = 7)
    @Column(name = "costDiamon", nullable = false)
    private long costDiamon;

    @IndexProp(id = 8)
    @Column(name = "costGold", nullable = false)
    private long costGold;

    @IndexProp(id = 9)
    @Column(name = "consumeVit", nullable = false)
    private long consumeVit;

    @IndexProp(id = 10)
    @Column(name = "consumeSpeedTool", nullable = false)
    private long consumeSpeedTool;

    public ChangeServerEntity(){

    }

    public ChangeServerEntity(String playerId, int termId) {
        this.playerId = playerId;
        this.termId = termId;
    }

    @Override
    public int getTermId() {
        return termId;
    }

    public void setTermId(int termId) {
        this.termId = termId;
    }

    public String getPlayerId() {
        return playerId;
    }

    @Override
    public String getPrimaryKey() {
        return id;
    }

    @Override
    public void setPrimaryKey(String primaryKey) {
        this.id = primaryKey;
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
    protected void setInvalid(boolean invalid) {
        this.invalid = invalid;
    }

    public long getCostDiamon() {
        return costDiamon;
    }

    public void setCostDiamon(long costDiamon) {
        this.costDiamon = costDiamon;
    }

    public long getCostGold() {
        return costGold;
    }

    public void setCostGold(long costGold) {
        this.costGold = costGold;
    }

    public long getConsumeVit() {
        return consumeVit;
    }

    public void setConsumeVit(long consumeVit) {
        this.consumeVit = consumeVit;
    }

    public long getConsumeSpeedTool() {
        return consumeSpeedTool;
    }

    public void setConsumeSpeedTool(long consumeSpeedTool) {
        this.consumeSpeedTool = consumeSpeedTool;
    }

    public long getTotalScore(){
        return costDiamon + costGold + consumeVit + consumeSpeedTool;
    }
}
