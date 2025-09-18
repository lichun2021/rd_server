package com.hawk.activity.type.impl.homeland.entity;

import com.alibaba.fastjson.JSONArray;
import com.hawk.activity.type.ActivityDataEntity;
import com.hawk.activity.type.IActivityDataEntity;
import com.hawk.activity.type.impl.exchangeTip.IExchangeTipEntity;
import com.hawk.serialize.string.SerializeHelper;
import org.hawk.annotation.IndexProp;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.*;

@Entity
@Table(name = "activity_homeland_puzzle")
public class HomeLandPuzzleEntity extends ActivityDataEntity implements IActivityDataEntity, IExchangeTipEntity {
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

    // 总抽奖次数
    @IndexProp(id = 5)
    @Column(name = "drawCount", nullable = false)
    private int drawCount;

    // 组合奖的保底计数器
    @IndexProp(id = 6)
    @Column(name = "pCombine", nullable = false)
    private int pCombine;

    // 大奖的保底计数器
    @IndexProp(id = 7)
    @Column(name = "pGrandPrize", nullable = false)
    private int pGrandPrize;
    // 存储被大奖顶替掉的保底组合图案
    @IndexProp(id = 8)
    @Column(name = "pItem", nullable = false)
    private int pItem;

    // 存储当前周期内已收集的组合图案ID。
    @IndexProp(id = 9)
    @Column(name = "collectedCombinationItems", nullable = false)
    private String collectedCombinationItem;

    //大奖获取奖励次数
    @IndexProp(id = 10)
    @Column(name = "grandPrizeWon", nullable = false)
    private int grandPrizeWon;

    //免费次数
    @IndexProp(id = 11)
    @Column(name = "freeTimes", nullable = false)
    private int freeTimes;

    //商店兑换
    @IndexProp(id = 12)
    @Column(name = "exchangeItems", nullable = false)
    private String exchangeItems;

    //抽奖记录
    @IndexProp(id = 13)
    @Column(name = "recordItems", nullable = false)
    private String recordItems;

    //商店
    @IndexProp(id = 14)
    @Column(name = "shopItems", nullable = false)
    private String shopItems;

    @IndexProp(id = 15)
    @Column(name = "createTime", nullable = false)
    private long createTime;

    @IndexProp(id = 16)
    @Column(name = "updateTime", nullable = false)
    private long updateTime;

    @IndexProp(id = 17)
    @Column(name = "invalid", nullable = false)
    private boolean invalid;
    /**
     * 兑换提醒信息
     */
    @IndexProp(id = 18)
    @Column(name = "playerPoint", nullable = false)
    private String playerPoint;
    @Transient
    private Set<Integer> collectedCombinationItemSet = new HashSet<>();
    @Transient
    private final Map<Long, HomeLandPuzzleRecord> recordItemMap = new LinkedHashMap<>();
    @Transient
    private Map<Integer, Integer> exchangeItemMap = new HashMap<>();
    @Transient
    private Map<Integer, Integer> shopItemMap = new HashMap<>();
    /**
     * 兑换提醒
     */
    @Transient
    private Set<Integer> playerPoints = new HashSet<>();

    public HomeLandPuzzleEntity() {

    }

    public HomeLandPuzzleEntity(String playerId, int termId) {
        this.playerId = playerId;
        this.termId = termId;
        this.loginDays = "";
    }

    public String recordDataSerialize() {
        JSONArray arr = new JSONArray();
        recordItemMap.values().stream().map(HomeLandPuzzleRecord::serialize).forEach(arr::add);
        return arr.toJSONString();
    }

    private void loadRecordData() {
        JSONArray arr = JSONArray.parseArray(recordItems);
        if (arr == null) {
            return;
        }
        arr.forEach(str -> {
            HomeLandPuzzleRecord record = new HomeLandPuzzleRecord();
            record.mergeFrom(str.toString());
            recordItemMap.put(record.getTime(), record);
        });
    }

    @Override
    public void beforeWrite() {
        this.recordItems = recordDataSerialize();
        this.collectedCombinationItem = SerializeHelper.collectionToString(collectedCombinationItemSet, SerializeHelper.BETWEEN_ITEMS);
        this.exchangeItems = SerializeHelper.mapToString(exchangeItemMap);
        this.shopItems = SerializeHelper.mapToString(shopItemMap);
        //提醒数据转字符串
        this.playerPoint = SerializeHelper.collectionToString(this.playerPoints, SerializeHelper.BETWEEN_ITEMS);
    }

    @Override
    public void afterRead() {
        loadRecordData();
        collectedCombinationItemSet = SerializeHelper.stringToSet(Integer.class, collectedCombinationItem, SerializeHelper.BETWEEN_ITEMS);
        exchangeItemMap = SerializeHelper.stringToMap(exchangeItems);
        shopItemMap = SerializeHelper.stringToMap(shopItems);
        //字符串转提醒数据
        playerPoints = SerializeHelper.stringToSet(Integer.class, playerPoint, SerializeHelper.BETWEEN_ITEMS);
    }

    public int getDrawCount() {
        return drawCount;
    }

    public void setDrawCount(int drawCount) {
        this.drawCount = drawCount;
    }

    public int getPCombine() {
        return pCombine;
    }

    public void setPCombine(int pCombine) {
        this.pCombine = pCombine;
    }

    public int getPGrandPrize() {
        return pGrandPrize;
    }

    public void setPGrandPrize(int pGrandPrize) {
        this.pGrandPrize = pGrandPrize;
    }

    public int getPItem() {
        return pItem;
    }

    public void setPItem(int pItem) {
        this.pItem = pItem;
    }

    public Set<Integer> getCollectedCombinationItemSet() {
        return collectedCombinationItemSet;
    }

    public int getGrandPrizeWon() {
        return grandPrizeWon;
    }

    public void setGrandPrizeWon(int grandPrizeWon) {
        this.grandPrizeWon = grandPrizeWon;
    }

    public int getFreeTimes() {
        return freeTimes;
    }

    public void setFreeTimes(int freeTimes) {
        this.freeTimes = freeTimes;
    }

    public Map<Long, HomeLandPuzzleRecord> getRecordItemMap() {
        return recordItemMap;
    }

    public Map<Integer, Integer> getShopItemMap() {
        return shopItemMap;
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

    public int getShopCount(int giftId) {
        return this.shopItemMap.getOrDefault(giftId, 0);
    }

    public void addShopCount(int giftId, int count) {
        if (count <= 0) {
            return;
        }
        count += this.getShopCount(giftId);
        this.shopItemMap.put(giftId, count);
        this.notifyUpdate();
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

    @Override
    public Set<Integer> getTipSet() {
        return playerPoints;
    }

    @Override
    public void setTipSet(Set<Integer> tips) {
        this.playerPoints = tips;
    }
}
