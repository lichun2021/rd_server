package com.hawk.activity.type.impl.luckGetGold.entity;

import com.hawk.activity.type.IActivityDataEntity;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.serialize.string.SerializeHelper;
import org.hawk.annotation.IndexProp;
import org.hawk.db.HawkDBEntity;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "activity_luck_get_gold")
public class LuckGetGoldEntity extends HawkDBEntity implements IActivityDataEntity {
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

    /** 活动成就项数据 */
    @IndexProp(id = 7)
    @Column(name = "achieveItems", nullable = false)
    private String achieveItems;

    @IndexProp(id = 8)
    @Column(name = "resetTime", nullable = false)
    private long resetTime;

    @IndexProp(id = 9)
    @Column(name = "achieveChoose", nullable = false)
    private int achieveChoose;

    @IndexProp(id = 10)
    @Column(name = "poolChoose", nullable = false)
    private int poolChoose;

    @IndexProp(id = 11)
    @Column(name = "freeCount", nullable = false)
    private int freeCount;

    @IndexProp(id = 12)
    @Column(name = "dailyDrawCount", nullable = false)
    private int dailyDrawCount;

    @IndexProp(id = 13)
    @Column(name = "totalDrawCount", nullable = false)
    private int totalDrawCount;

    /** 活动成就 */
    @Transient
    private List<AchieveItem> itemList = new ArrayList<AchieveItem>();

    public LuckGetGoldEntity(){

    }

    public LuckGetGoldEntity(String playerId, int termId){
        this.playerId = playerId;
        this.termId = termId;
        this.poolChoose = 1;
    }

    @Override
    public void beforeWrite() {
        //成就数据转换成字符串
        this.achieveItems = SerializeHelper.collectionToString(this.itemList, SerializeHelper.ELEMENT_DELIMITER);
    }

    @Override
    public void afterRead() {
        //字符串转换成成就数据
        this.itemList = SerializeHelper.stringToList(AchieveItem.class, this.achieveItems);
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

    public List<AchieveItem> getItemList() {
        return itemList;
    }

    public void setItemList(List<AchieveItem> itemList) {
        this.itemList = itemList;
    }

    public long getResetTime() {
        return resetTime;
    }

    public void setResetTime(long resetTime) {
        this.resetTime = resetTime;
    }

    public int getAchieveChoose() {
        return achieveChoose;
    }

    public void setAchieveChoose(int achieveChoose) {
        this.achieveChoose = achieveChoose;
    }

    public int getPoolChoose() {
        return poolChoose;
    }

    public void setPoolChoose(int poolChoose) {
        this.poolChoose = poolChoose;
    }

    public int getFreeCount() {
        return freeCount;
    }

    public void setFreeCount(int freeCount) {
        this.freeCount = freeCount;
    }

    public int getDailyDrawCount() {
        return dailyDrawCount;
    }

    public void setDailyDrawCount(int dailyDrawCount) {
        this.dailyDrawCount = dailyDrawCount;
    }

    public int getTotalDrawCount() {
        return totalDrawCount;
    }

    public void setTotalDrawCount(int totalDrawCount) {
        this.totalDrawCount = totalDrawCount;
    }
}
