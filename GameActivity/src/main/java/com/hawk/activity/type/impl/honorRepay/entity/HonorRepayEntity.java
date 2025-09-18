package com.hawk.activity.type.impl.honorRepay.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import org.hawk.annotation.IndexProp;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import org.hawk.db.HawkDBEntity;
import org.hibernate.annotations.GenericGenerator;
import com.hawk.activity.type.IActivityDataEntity;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.serialize.string.SerializeHelper;

@Entity
@Table(name = "activity_honor_repay")
public class HonorRepayEntity extends HawkDBEntity implements IActivityDataEntity{
    @Id
    @GenericGenerator(name = "uuid", strategy = "org.hawk.uuid.HawkUUIDGenerator")
    @GeneratedValue(generator = "uuid")
    @IndexProp(id = 1)
    @Column(name = "id", unique = true, nullable = false)
    private String id;

    @IndexProp(id = 2)
    @Column(name = "playerId", nullable = false)
    private String playerId;

    @IndexProp(id = 3)
    @Column(name = "termId", nullable = false)
    private int termId;

    /** 活动成就项数据 */
    @IndexProp(id = 4)
    @Column(name = "achieveItems", nullable = false)
    private String achieveItems;

    /** 返利领奖数据  四档  0:1, */
    @IndexProp(id = 5)
    @Column(name = "receiveReward", nullable = false)
    private String receiveReward;

    /**购买次数*/
    @IndexProp(id = 6)
    @Column(name = "buyTimes", nullable = false)
    private int buyimes;

    @IndexProp(id = 7)
    @Column(name = "createTime", nullable = false)
    private long createTime;

    @IndexProp(id = 8)
    @Column(name = "updateTime", nullable = false)
    private long updateTime;

    @IndexProp(id = 9)
    @Column(name = "invalid", nullable = false)
    private boolean invalid;

    @Transient
    private List<AchieveItem> itemList = new ArrayList<>();


    @Transient
    private Map<Integer, Integer> receiveRewardMap = new HashMap<>();

    public HonorRepayEntity() {
    }

    public HonorRepayEntity(String playerId, int termId) {
        this.playerId = playerId;
        this.termId = termId;
        this.itemList = new ArrayList<>();
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

    public int getTermId() {
        return termId;
    }

    public void setTermId(int termId) {
        this.termId = termId;
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

    @Override
    public String getPrimaryKey() {
        return this.id;
    }

    @Override
    public void setPrimaryKey(String primaryKey) {
        this.id = primaryKey;
    }

    @Override
    public void beforeWrite() {
        this.achieveItems = SerializeHelper.collectionToString(this.itemList, SerializeHelper.ELEMENT_DELIMITER);
        this.receiveReward = SerializeHelper.mapToString(this.receiveRewardMap);
    }

    @Override
    public void afterRead() {
    	this.itemList.clear();
        SerializeHelper.stringToList(AchieveItem.class, this.achieveItems, this.itemList);
        this.receiveRewardMap = SerializeHelper.stringToMap(this.receiveReward);
    }

    public String getAchieveItems() {
        return achieveItems;
    }

    public void setAchieveItems(String achieveItems) {
        this.achieveItems = achieveItems;
    }

    public List<AchieveItem> getItemList() {
        return itemList;
    }

    public void setItemList(List<AchieveItem> itemList) {
        this.itemList = itemList;
    }

    public int getBuyimes() {
        return buyimes;
    }

    public void setBuyimes(int buyimes) {
        this.buyimes = buyimes;
    }

    public Map<Integer, Integer> getReceiveRewardMap() {
        return receiveRewardMap;
    }

    public void setReceiveRewardMap(Map<Integer, Integer> receiveRewardMap) {
        this.receiveRewardMap = receiveRewardMap;
    }

}
