package com.hawk.activity.type.impl.roseGift.entity;

import com.hawk.activity.type.IActivityDataEntity;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
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
@Table(name = "activity_rose_gift")
public class RoseGiftEntity extends HawkDBEntity implements IActivityDataEntity {
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

    /** 获得花瓣数量 */
    @IndexProp(id = 7)
    @Column(name = "selfNum", nullable = false)
    private int selfNum;

    /** 今天是否充值 */
    @IndexProp(id = 8)
    @Column(name = "isPayToday", nullable = false)
    private boolean isPayToday;

    /** 活动成就项数据 */
    @IndexProp(id = 9)
    @Column(name = "achieveItems", nullable = false)
    private String achieveItems;

    /** 抽奖数量信息 */
    @IndexProp(id = 10)
    @Column(name = "drawInfo", nullable = false)
    private String drawInfo;

    /** 兑换数量信息 */
    @IndexProp(id = 11)
    @Column(name = "exchangeInfo", nullable = false)
    private String exchangeInfo;

    /** 兑换提醒信息 */
    @IndexProp(id = 12)
    @Column(name = "playerPoint", nullable = false)
    private String playerPoint;

    /** 活动成就 */
    @Transient
    private List<AchieveItem> itemList = new ArrayList<AchieveItem>();

    /** 抽奖数量 */
    @Transient
    private Map<Integer, Integer> drawNumMap = new HashMap<>();

    /** 兑换数量 */
    @Transient
    private Map<Integer, Integer> exchangeNumMap = new HashMap<>();

    /** 兑换提醒 */
    @Transient
    private List<Integer> playerPoints = new ArrayList<Integer>();

    /**
     * 构造函数
     * 必须有空参数的，数据库模块解析需要
     */
    public RoseGiftEntity(){

    }

    /**
     * 构造函数
     * @param playerId 玩家id
     * @param termId 活动期数
     */
    public RoseGiftEntity(String playerId, int termId){
        this.playerId = playerId;
        this.termId = termId;
        this.achieveItems = "";
        this.drawInfo = "";
        this.exchangeInfo = "";
        this.playerPoint = "";
    }

    /**
     * 存库前置操作
     */
    @Override
    public void beforeWrite() {
        //成就数据转换成字符串
        this.achieveItems = SerializeHelper.collectionToString(this.itemList, SerializeHelper.ELEMENT_DELIMITER);
        //抽奖数据转字符串
        this.drawInfo = SerializeHelper.mapToString(drawNumMap);
        //兑换数据转字符串
        this.exchangeInfo = SerializeHelper.mapToString(exchangeNumMap);
        //提醒数据转字符串
        this.playerPoint = SerializeHelper.collectionToString(this.playerPoints,SerializeHelper.ATTRIBUTE_SPLIT);
    }

    /**
     * 读库后置操作
     */
    @Override
    public void afterRead() {
        //字符串转换成成就数据
        itemList = SerializeHelper.stringToList(AchieveItem.class, this.achieveItems);
        //字符串转抽奖数据
        drawNumMap = SerializeHelper.stringToMap(drawInfo, Integer.class, Integer.class);
        //字符串转兑换数据
        exchangeNumMap = SerializeHelper.stringToMap(exchangeInfo, Integer.class, Integer.class);
        //字符串转提醒数据
        playerPoints = SerializeHelper.cfgStr2List(playerPoint, SerializeHelper.ATTRIBUTE_SPLIT);
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

    public int getSelfNum() {
        return selfNum;
    }

    public void setSelfNum(int selfNum) {
        this.selfNum = selfNum;
    }

    public boolean isPayToday() {
        return isPayToday;
    }

    public void setPayToday(boolean payToday) {
        isPayToday = payToday;
    }

    public List<AchieveItem> getItemList() {
        return itemList;
    }

    public void setItemList(List<AchieveItem> itemList) {
        this.itemList = itemList;
    }

    public List<Integer> getPlayerPoints() {
        return playerPoints;
    }

    public void setPlayerPoints(List<Integer> playerPoints) {
        this.playerPoints = playerPoints;
    }

    public Map<Integer, Integer> getDrawNumMap() {
        return drawNumMap;
    }

    public Map<Integer, Integer> getExchangeNumMap() {
        return exchangeNumMap;
    }
}
