package com.hawk.activity.type.impl.honourHeroReturn.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.persistence.Column;
import org.hawk.annotation.IndexProp;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.hawk.activity.type.ActivityDataEntity;
import com.hawk.activity.type.impl.achieve.AchieveType;
import org.hawk.os.HawkTime;
import org.hibernate.annotations.GenericGenerator;

import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.serialize.string.SerializeHelper;

/**
 * @author richard
 */
@Entity
@Table(name = "activity_honour_hero_return")
public class HonourHeroReturnEntity extends ActivityDataEntity {

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

    /**
     * 单抽次数
     */
    @IndexProp(id = 7)
    @Column(name = "oneLotteryCount", nullable = false)
    private String oneLotteryCount;
    /**
     * 10连抽次数
     */
    @IndexProp(id = 8)
    @Column(name = "tenLotteryCount", nullable = false)
    private String tenLotteryCount;
    /**
     * 关注的兑换id列表
     **/
    @IndexProp(id = 9)
    @Column(name = "exchangeMsg", nullable = false)
    private String exchangeMsg;
    /** 关注的兑换id列表 **/
    @IndexProp(id = 10)
    @Column(name = "playerPoint", nullable = false)
    private String playerPoint;
    /**
     * 活动成就项数据
     */
    @IndexProp(id = 11)
    @Column(name = "achieveItems", nullable = false)
    private String achieveItems;

    /**
     *  最后达成每日积分的日期，用于每日积分成就
     */
    @IndexProp(id = 12)
    @Column(name = "lotteryPage", nullable = false)
    private int lotteryPage;

    @IndexProp(id = 13)
    @Column(name = "loginDays", nullable = false)
    private String loginDays;

    @Transient
    private List<AchieveItem> itemList = new ArrayList<AchieveItem>();

    @Transient
    private Map<Integer, Integer> oneLotteryCountMap = new ConcurrentHashMap<>();

    @Transient
    private Map<Integer, Integer> tenLotteryCountMap = new ConcurrentHashMap<>();

    @Transient
    private Map<Integer, Integer> exchangeNumMap = new ConcurrentHashMap<>();
    @Transient
    private List<Integer> playerPoints = new CopyOnWriteArrayList<Integer>();

    public HonourHeroReturnEntity() {
    }

    public HonourHeroReturnEntity(String playerId, int termId) {
        this.playerId = playerId;
        this.termId = termId;
        this.oneLotteryCount = "";
        this.tenLotteryCount = "";
        this.achieveItems = "";
        this.exchangeMsg = "";
        this.lotteryPage = 1;
        this.loginDays = "";
    }

    @Override
    public void beforeWrite() {
        this.achieveItems = SerializeHelper.collectionToString(this.itemList, SerializeHelper.ELEMENT_DELIMITER);
        this.exchangeMsg = SerializeHelper.mapToString(exchangeNumMap);
        this.oneLotteryCount = SerializeHelper.mapToString(oneLotteryCountMap);
        this.tenLotteryCount = SerializeHelper.mapToString(tenLotteryCountMap);
        this.playerPoint = SerializeHelper.collectionToString(playerPoints, SerializeHelper.ATTRIBUTE_SPLIT);
    }

    @Override
    public void afterRead() {
    	this.itemList.clear();
        SerializeHelper.stringToList(AchieveItem.class, this.achieveItems, this.itemList);
        this.stringToLoginDaysList();
        exchangeNumMap = SerializeHelper.stringToMap(exchangeMsg, Integer.class, Integer.class);
        oneLotteryCountMap = SerializeHelper.stringToMap(oneLotteryCount, Integer.class, Integer.class);
        tenLotteryCountMap = SerializeHelper.stringToMap(tenLotteryCount, Integer.class, Integer.class);
        playerPoints = SerializeHelper.cfgStr2List(playerPoint, SerializeHelper.ATTRIBUTE_SPLIT);
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

    public int getTermId() {
        return termId;
    }

    public void setTermId(int termId) {
        this.termId = termId;
    }

    public int getOneLotteryCountToday() {
        int day = HawkTime.getYearDay();
        return this.oneLotteryCountMap.getOrDefault(day, 0);
    }

    public void addOneLotteryCount() {
        int day = HawkTime.getYearDay();
        int count = this.oneLotteryCountMap.getOrDefault(day, 0);
        count++;
        this.oneLotteryCountMap.put(day, count);
        this.notifyUpdate();
    }

    public void addTenLotteryCount() {
        int day = HawkTime.getYearDay();
        int count = this.tenLotteryCountMap.getOrDefault(day, 0);
        count++;
        this.tenLotteryCountMap.put(day, count);
        this.notifyUpdate();
    }

    public int getTotalLotteryCount() {
        int count = 0;
        for (Map.Entry<Integer, Integer> entry : this.oneLotteryCountMap.entrySet()) {
            count += entry.getValue();
        }
        for (Map.Entry<Integer, Integer> entry : this.tenLotteryCountMap.entrySet()) {
            count += entry.getValue() * 10;
        }
        return count;
    }

    public List<AchieveItem> getItemList() {
        return itemList;
    }

    public void setItemList(List<AchieveItem> itemList) {
        this.itemList = itemList;
        this.notifyUpdate();
    }

    public Map<Integer, Integer> getExchangeNumMap() {
        return exchangeNumMap;
    }


    public int getExchangeCount(int exchangeId) {
        return this.exchangeNumMap.getOrDefault(exchangeId, 0);
    }

    public void addExchangeCount(int eid, int count) {
        if (count <= 0) {
            return;
        }
        count += this.getExchangeCount(eid);
        this.exchangeNumMap.put(eid, count);
        this.notifyUpdate();
    }

    @Override
    public void setLoginDaysStr(String loginDays) {
        this.loginDays = loginDays;
    }

    @Override
    public String getLoginDaysStr() {
        return loginDays;
    }

    public AchieveItem getDailyScoreItem(){
        for(AchieveItem item: this.itemList){
            if(item.getAchieveId() == AchieveType.HONOUR_HERO_RETURN_DAILY_SCORE.getValue()){
                return item;
            }
        }
        return null;
    }

    public int getLotteryPage() {
        return lotteryPage;
    }

    public void setLotteryPage(int lotteryPage) {
        this.lotteryPage = lotteryPage;
        this.notifyUpdate();
    }

    public void addTips(int id){
        if(!playerPoints.contains(id)){
            playerPoints.add(id);
        }
        this.notifyUpdate();
    }

    public void removeTips(int id){
        playerPoints.remove(new Integer(id));
        this.notifyUpdate();
    }

    public List<Integer> getPlayerPoints() {
        return playerPoints;
    }
}
