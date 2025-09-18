package com.hawk.activity.type.impl.directGift.entity;

import com.hawk.activity.type.IActivityDataEntity;
import com.hawk.serialize.string.SerializeHelper;
import org.hawk.annotation.IndexProp;
import org.hawk.db.HawkDBEntity;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "activity_direct_gift")
public class DirectGiftEntity extends HawkDBEntity implements IActivityDataEntity {

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
    @Column(name = "buyGiftTimes")
    private String buyGiftTimes;

    @IndexProp(id = 5)
    @Column(name = "createTime", nullable = false)
    private long createTime;

    @IndexProp(id = 6)
    @Column(name = "updateTime", nullable = false)
    private long updateTime;

    @IndexProp(id = 7)
    @Column(name = "invalid", nullable = false)
    private boolean invalid;

    @Transient
    private Map<Integer,Integer> buyGiftTimesMap = new HashMap<>();

    public DirectGiftEntity(){

    }
    public DirectGiftEntity(String playerId, int termId) {
        this.playerId = playerId;
        this.termId = termId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    @Override
    public int getTermId() {
        return termId;
    }

    public void setTermId(int termId) {
        this.termId = termId;
    }

    @Override
    public String getPrimaryKey() {
        return this.id;
    }

    @Override
    public void setPrimaryKey(String s) {
        this.id = s;
    }

    @Override
    public long getCreateTime() {
        return this.createTime;
    }

    @Override
    protected void setCreateTime(long l) {
        this.createTime = l;
    }

    @Override
    public long getUpdateTime() {
        return this.updateTime;
    }

    @Override
    protected void setUpdateTime(long l) {
        this.updateTime = l;
    }

    @Override
    public boolean isInvalid() {
        return this.invalid;
    }

    @Override
    protected void setInvalid(boolean b) {
        this.invalid = b;
    }

    @Override
    public void beforeWrite() {
        buyGiftTimes = SerializeHelper.mapToString(buyGiftTimesMap);
    }

    @Override
    public void afterRead() {
        buyGiftTimesMap = SerializeHelper.stringToMap(buyGiftTimes,Integer.class,Integer.class);
    }

    public int getBuyTimes(int id){
        return buyGiftTimesMap.getOrDefault(id,0);
    }

    public void addBuyTimes(int id, int times){
        int newTimes = this.getBuyTimes(id) + times;
        this.buyGiftTimesMap.put(id, newTimes);
        notifyUpdate();
    }

    public Map<Integer, Integer> getBuyGiftTimesMap() {
        return buyGiftTimesMap;
    }
    
    public void decBuyTimes(int id){
        int newTimes = this.getBuyTimes(id) - 1;
        newTimes = Math.max(newTimes, 0);
        this.buyGiftTimesMap.put(id, newTimes);
        notifyUpdate();
    }
}
