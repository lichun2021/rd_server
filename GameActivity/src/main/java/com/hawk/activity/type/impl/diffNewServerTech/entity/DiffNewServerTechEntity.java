package com.hawk.activity.type.impl.diffNewServerTech.entity;

import com.hawk.activity.type.IActivityDataEntity;
import com.hawk.serialize.string.SerializeHelper;
import org.hawk.db.HawkDBEntity;
import org.hibernate.annotations.GenericGenerator;

import org.hawk.annotation.IndexProp;
import javax.persistence.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "activity_diff_new_server_tech")
public class DiffNewServerTechEntity extends HawkDBEntity implements IActivityDataEntity {
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
    @Column(name = "rewardGet", nullable = false)
    private String rewardGet;

    @IndexProp(id = 8)
    @Column(name = "buffGet", nullable = false)
    private String buffGet;

    @Transient
    private List<Integer> rewardGetList = new ArrayList<>();

    @Transient
    private List<Integer> buffGetList = new ArrayList<>();

    public DiffNewServerTechEntity(){

    }

    public DiffNewServerTechEntity(String playerId, int termId) {
        this.playerId = playerId;
        this.termId = termId;
    }

    @Override
    public void beforeWrite() {
        this.rewardGet = SerializeHelper.collectionToString(this.rewardGetList,SerializeHelper.ATTRIBUTE_SPLIT);
        this.buffGet = SerializeHelper.collectionToString(this.buffGetList,SerializeHelper.ATTRIBUTE_SPLIT);
    }

    @Override
    public void afterRead() {
        this.rewardGetList = SerializeHelper.cfgStr2List(this.rewardGet, SerializeHelper.ATTRIBUTE_SPLIT);
        this.buffGetList = SerializeHelper.cfgStr2List(this.buffGet, SerializeHelper.ATTRIBUTE_SPLIT);
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



    public List<Integer> getRewardGetList() {
        return rewardGetList;
    }

    public List<Integer> getBuffGetList() {
        return buffGetList;
    }
}
