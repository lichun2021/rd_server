package com.hawk.activity.type.impl.seasonActivity.entity;

import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.IActivityDataEntity;
import com.hawk.activity.type.impl.exchangeTip.IExchangeTipEntity;
import com.hawk.activity.type.impl.order.entity.IOrderDateEntity;
import com.hawk.activity.type.impl.order.entity.OrderItem;
import com.hawk.serialize.string.SerializeHelper;
import org.hawk.db.HawkDBEntity;
import org.hibernate.annotations.GenericGenerator;

import org.hawk.annotation.IndexProp;
import javax.persistence.*;
import java.util.*;

@Entity
@Table(name = "activity_season")
public class SeasonActivityEntity extends HawkDBEntity implements IActivityDataEntity, IExchangeTipEntity, IOrderDateEntity {
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
    @Column(name = "orderItems", nullable = false)
    private String orderItems;

    /** 战令等级 */
    @IndexProp(id = 8)
    @Column(name = "orderLevel", nullable = false)
    private int orderLevel;

    /** 战令经验 */
    @IndexProp(id = 9)
    @Column(name = "orderExp", nullable = false)
    private int orderExp;

    /** 战令购买标志 */
    @IndexProp(id = 10)
    @Column(name = "authorityId", nullable = false)
    private int authorityId;

    /** 战令普通奖励领奖标志 */
    @IndexProp(id = 11)
    @Column(name = "orderRewardLevel", nullable = false)
    private String orderRewardLevel;

    /** 战令高级奖励领奖标志 */
    @IndexProp(id = 12)
    @Column(name = "orderRewardAdLevel", nullable = false)
    private String orderRewardAdLevel;

    /** 兑换数量信息 */
    @IndexProp(id = 13)
    @Column(name = "exchange", nullable = false)
    private String exchange;

    /** 兑换提醒信息 */
    @IndexProp(id = 14)
    @Column(name = "tips", nullable = false)
    private String tips;

    /** 前端等级 */
    @IndexProp(id = 15)
    @Column(name = "clientLevel", nullable = false)
    private int clientLevel;

    /** 战令任务 */
    @Transient
    private List<OrderItem> orderList = new ArrayList<>();

    /** 战令普通奖励领奖标志 */
    @Transient
    private List<Integer> orderRewardLevelList = new ArrayList<>();

    /** 战令高级奖励领奖标志 */
    @Transient
    private List<Integer> orderRewardAdLevelList = new ArrayList<>();

    /** 兑换数量 */
    @Transient
    private Map<Integer, Integer> exchangeMap = new HashMap<>();

    /** 兑换提醒 */
    @Transient
    private Set<Integer> tipSet = new HashSet<>();

    public SeasonActivityEntity(){

    }

    public SeasonActivityEntity(String playerId, int termId){
        this.playerId = playerId;
        this.termId = termId;
        this.orderLevel = 1;
    }

    /**
     * 入库前
     */
    @Override
    public void beforeWrite() {
        this.orderItems = SerializeHelper.collectionToString(this.orderList, SerializeHelper.ELEMENT_DELIMITER);
        exchange = SerializeHelper.mapToString(exchangeMap);
        this.tips = SerializeHelper.collectionToString(this.tipSet,SerializeHelper.ATTRIBUTE_SPLIT);
        this.orderRewardLevel = SerializeHelper.collectionToString(this.orderRewardLevelList,SerializeHelper.ATTRIBUTE_SPLIT);
        this.orderRewardAdLevel = SerializeHelper.collectionToString(this.orderRewardAdLevelList,SerializeHelper.ATTRIBUTE_SPLIT);
    }

    /**
     * 读取后
     */
    @Override
    public void afterRead() {
    	this.orderList.clear();
        SerializeHelper.stringToList(OrderItem.class, this.orderItems, this.orderList);
        exchangeMap = SerializeHelper.stringToMap(exchange, Integer.class, Integer.class);
        SerializeHelper.stringToSet(Integer.class, this.tips, SerializeHelper.ATTRIBUTE_SPLIT,null,this.tipSet);
        orderRewardLevelList = SerializeHelper.cfgStr2List(orderRewardLevel, SerializeHelper.ATTRIBUTE_SPLIT);
        orderRewardAdLevelList = SerializeHelper.cfgStr2List(orderRewardAdLevel, SerializeHelper.ATTRIBUTE_SPLIT);
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

    @Override
    public Set<Integer> getTipSet() {
        return tipSet;
    }

    @Override
    public void setTipSet(Set<Integer> tipSet) {
        this.tipSet = tipSet;
    }

    @Override
    public int getActivityType() {
        return ActivityType.SEASON_ACTIVITY.intValue();
    }

    public List<OrderItem> getOrderList() {
        return orderList;
    }

    public void setOrderList(List<OrderItem> orderList) {
        this.orderList = orderList;
    }

    public Map<Integer, Integer> getExchangeMap() {
        return exchangeMap;
    }

    public int getOrderLevel() {
        return orderLevel;
    }

    public void setOrderLevel(int orderLevel) {
        this.orderLevel = orderLevel;
    }

    public int getOrderExp() {
        return orderExp;
    }

    public void setOrderExp(int orderExp) {
        this.orderExp = orderExp;
    }

    public int getAuthorityId() {
        return authorityId;
    }

    public void setAuthorityId(int authorityId) {
        this.authorityId = authorityId;
    }

    public int getClientLevel() {
        return clientLevel;
    }

    public void setClientLevel(int clientLevel) {
        this.clientLevel = clientLevel;
    }

    public List<Integer> getOrderRewardLevelList() {
        return orderRewardLevelList;
    }

    public List<Integer> getOrderRewardAdLevelList() {
        return orderRewardAdLevelList;
    }
}
