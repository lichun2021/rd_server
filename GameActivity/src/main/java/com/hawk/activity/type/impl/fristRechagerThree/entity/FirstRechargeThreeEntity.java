package com.hawk.activity.type.impl.fristRechagerThree.entity;

import com.hawk.activity.type.IActivityDataEntity;
import com.hawk.serialize.string.SerializeHelper;
import org.hawk.db.HawkDBEntity;
import org.hibernate.annotations.GenericGenerator;

import org.hawk.annotation.IndexProp;
import javax.persistence.*;
import java.util.HashMap;
import java.util.Map;

/**
 * 新首充数据库类
 */
@Entity
@Table(name = "activity_first_recharge_three")
public class FirstRechargeThreeEntity extends HawkDBEntity implements IActivityDataEntity {
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

    /** 激活时间*/
    @IndexProp(id = 7)
    @Column(name = "activeTime", nullable = false)
    private long activeTime;

    /** 当前已经弹窗的等级*/
    @IndexProp(id = 8)
    @Column(name = "payCount", nullable = false)
    private int payCount;

    /** 领奖状态*/
    @IndexProp(id = 9)
    @Column(name = "rewardState", nullable = false)
    private String rewardState;


    /** 领奖状态map*/
    @Transient
    private Map<Integer, Integer> rewardStateMap = new HashMap<>();

    
    public FirstRechargeThreeEntity(){

    }

    public FirstRechargeThreeEntity(String playerId, int termId){
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

    @Override
    public String getPrimaryKey() {
        return this.id;
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
    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    @Override
    public long getUpdateTime() {
        return updateTime;
    }

    @Override
    public void setUpdateTime(long updateTime) {
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

    @Override
    public void beforeWrite() {
        //转换成字符串
        this.rewardState = SerializeHelper.mapToString(this.rewardStateMap);
    }

    @Override
    public void afterRead() {
        //转换成map
        this.rewardStateMap = SerializeHelper.stringToMap(this.rewardState);
    }

    public long getActiveTime() {
        return activeTime;
    }

    public void setActiveTime(long activeTime) {
        this.activeTime = activeTime;
    }

    public int getPayCount() {
        return payCount;
    }

    public void setPayCount(int payCount) {
        this.payCount = payCount;
    }


    public Map<Integer, Integer> getRewardStateMap() {
        return rewardStateMap;
    }
}
