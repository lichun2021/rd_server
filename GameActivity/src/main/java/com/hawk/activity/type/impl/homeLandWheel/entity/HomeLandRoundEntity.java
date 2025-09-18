package com.hawk.activity.type.impl.homeLandWheel.entity;

import com.hawk.activity.type.ActivityDataEntity;
import com.hawk.activity.type.IActivityDataEntity;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.exchangeTip.IExchangeTipEntity;
import com.hawk.serialize.string.SerializeHelper;
import org.hawk.annotation.IndexProp;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.*;

@Entity
@Table(name = "activity_homeland_round")
public class HomeLandRoundEntity extends ActivityDataEntity implements IActivityDataEntity, IExchangeTipEntity {
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

    @IndexProp(id = 4)
    @Column(name = "loginDays", nullable = false)
    private String loginDays;

    //每日抽奖次数
    @IndexProp(id = 5)
    @Column(name = "drawTimes", nullable = false)
    private int drawTimes;

    //商店兑换
    @IndexProp(id = 6)
    @Column(name = "exchangeItems", nullable = false)
    private String exchangeItems;

    @IndexProp(id = 7)
    @Column(name = "createTime", nullable = false)
    private long createTime;

    @IndexProp(id = 8)
    @Column(name = "updateTime", nullable = false)
    private long updateTime;

    @IndexProp(id = 9)
    @Column(name = "invalid", nullable = false)
    private boolean invalid;
    /**
     * 兑换提醒信息
     */
    @IndexProp(id = 10)
    @Column(name = "playerPoint", nullable = false)
    private String playerPoint;
    /**
     * 当前层
     */
    @IndexProp(id = 11)
    @Column(name = "currentFloor", nullable = false)
    private int currentFloor;
    /**
     * 活动成就项数据
     */
    @IndexProp(id = 12)
    @Column(name = "achieveItems", nullable = false)
    private String achieveItems;

    /**
     * 上一次层数变化
     */
    @IndexProp(id = 13)
    @Column(name = "lastFloorChange", nullable = false)
    private int lastFloorChange;
    /**
     * 保底计数器: Key=楼层, Value=在该层连续保护抽取未升层的次数
     */
    @Transient
    private final Map<Integer, Integer> pityCounters = new HashMap<>();
    @Transient
    private Map<Integer, Integer> exchangeItemMap = new HashMap<>();
    /**
     * 兑换提醒
     */
    @Transient
    private Set<Integer> playerPoints = new HashSet<>();
    @Transient
    private List<AchieveItem> itemList = new ArrayList<>();

    public HomeLandRoundEntity() {

    }

    public HomeLandRoundEntity(String playerId, int termId) {
        this.playerId = playerId;
        this.termId = termId;
        this.loginDays = "";
        this.achieveItems = "";
    }

    @Override
    public void beforeWrite() {
        this.achieveItems = SerializeHelper.collectionToString(this.itemList);
        this.exchangeItems = SerializeHelper.mapToString(exchangeItemMap);
        this.playerPoint = SerializeHelper.collectionToString(this.playerPoints, SerializeHelper.BETWEEN_ITEMS);
    }

    @Override
    public void afterRead() {
        exchangeItemMap = SerializeHelper.stringToMap(exchangeItems);
        playerPoints = SerializeHelper.stringToSet(Integer.class, playerPoint, SerializeHelper.BETWEEN_ITEMS);
        this.itemList = SerializeHelper.stringToList(AchieveItem.class, this.achieveItems);
    }

    public int getDrawTimes() {
        return drawTimes;
    }

    public void setDrawTimes(int drawTimes) {
        this.drawTimes = drawTimes;
    }

    public Map<Integer, Integer> getExchangeItemMap() {
        return exchangeItemMap;
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
    public void setLoginDaysStr(String loginDays) {
        this.loginDays = loginDays;
    }

    @Override
    public String getLoginDaysStr() {
        return loginDays;
    }

    public int getExchangeCount(int exchangeId) {
        return this.exchangeItemMap.getOrDefault(exchangeId, 0);
    }

    public void addExchangeCount(int eid, int count) {
        if (count <= 0) {
            return;
        }
        count += this.getExchangeCount(eid);
        this.exchangeItemMap.put(eid, count);
        this.notifyUpdate();
    }

    public int getCurrentFloor() {
        return currentFloor;
    }

    public void setCurrentFloor(int currentFloor) {
        this.currentFloor = currentFloor;
    }

    public int getPityCount(int floor) {
        return pityCounters.getOrDefault(floor, 0);
    }

    public void resetPityCount(int floor) {
        pityCounters.put(floor, 0);
    }

    public void resetAllPityCounts() {
        pityCounters.clear();
    }

    public void incrementPityCount(int floor) {
        pityCounters.merge(floor, 1, Integer::sum);
    }

    @Override
    public Set<Integer> getTipSet() {
        return playerPoints;
    }

    @Override
    public void setTipSet(Set<Integer> tips) {
        this.playerPoints = tips;
    }

    public List<AchieveItem> getItemList() {
        return itemList;
    }

    public void setItemList(List<AchieveItem> itemList) {
        this.itemList = itemList;
    }

    public int getLastFloorChange() {
        return lastFloorChange;
    }

    public void setLastFloorChange(int lastFloorChange) {
        this.lastFloorChange = lastFloorChange;
    }
}
