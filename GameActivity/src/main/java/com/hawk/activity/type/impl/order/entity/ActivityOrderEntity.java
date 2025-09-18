package com.hawk.activity.type.impl.order.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.persistence.Column;
import org.hawk.annotation.IndexProp;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hawk.db.HawkDBEntity;
import org.hibernate.annotations.GenericGenerator;

import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.IActivityDataEntity;
import com.hawk.serialize.string.SerializeHelper;

/**
 * 红警占领活动数据存储
 * @author Jesse
 *
 */
@Entity
@Table(name = "activity_order")
public class ActivityOrderEntity extends HawkDBEntity implements IActivityDataEntity,IOrderDateEntity {

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
	@Column(name = "weekCycle", nullable = false)
	private int weekCycle;

	/** 战令进阶id */
    @IndexProp(id = 5)
	@Column(name = "authorityId", nullable = false)
	private int authorityId;
	
	/** 战令经验 */
    @IndexProp(id = 6)
	@Column(name = "exp", nullable = false)
	private int exp;
	
	/** 战令等级 */
    @IndexProp(id = 7)
	@Column(name = "level", nullable = false)
	private int level;

	/** 额外经验购买信息 */
    @IndexProp(id = 8)
	@Column(name = "expBuyInfo", nullable = false)
	private String expBuyInfo;

	/** 战令任务信息 */
    @IndexProp(id = 9)
	@Column(name = "orderItems", nullable = false)
	private String orderItems;

    @IndexProp(id = 10)
	@Column(name = "historyItems", nullable = false)
	private String historyItems;

    @IndexProp(id = 11)
	@Column(name = "createTime", nullable = false)
	private long createTime;

    @IndexProp(id = 12)
	@Column(name = "updateTime", nullable = false)
	private long updateTime;

    @IndexProp(id = 13)
	@Column(name = "invalid", nullable = false)
	private boolean invalid;

	@Transient
	private List<OrderItem> orderList = new CopyOnWriteArrayList<>();

	@Transient
	private List<OrderItem> historyOrderList = new CopyOnWriteArrayList<>();

	@Transient
	private List<Integer> expBuyList = new CopyOnWriteArrayList<>();

	public ActivityOrderEntity() {
	}

	public ActivityOrderEntity(String playerId) {
		this.playerId = playerId;
	}

	public ActivityOrderEntity(String playerId, int termId) {
		this.playerId = playerId;
		this.termId = termId;
	}

	@Override
	public void beforeWrite() {
		this.orderItems = SerializeHelper.collectionToString(this.orderList, SerializeHelper.ELEMENT_DELIMITER);
		this.historyItems = SerializeHelper.collectionToString(this.historyOrderList, SerializeHelper.ELEMENT_DELIMITER);
		this.expBuyInfo = SerializeHelper.collectionToString(expBuyList);
	}


	@Override
	public void afterRead() {
		this.orderList.clear();
		this.historyOrderList.clear();
		this.expBuyList.clear();
		SerializeHelper.stringToList(OrderItem.class, this.orderItems, this.orderList);
		SerializeHelper.stringToList(OrderItem.class, this.historyItems, this.historyOrderList);
		SerializeHelper.stringToList(Integer.class, expBuyInfo, expBuyList);
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

	public int getWeekCycle() {
		return weekCycle;
	}

	public void setWeekCycle(int weekCycle) {
		this.weekCycle = weekCycle;
	}

	public int getExp() {
		return exp;
	}

	public void setExp(int exp) {
		this.exp = exp;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public int getAuthorityId() {
		return authorityId;
	}
	
	/**
	 * 是否激活进阶权限
	 * @return
	 */
	public boolean isAdvance(){
		return authorityId != 0;
	}

	public void setAuthorityId(int authorityId) {
		this.authorityId = authorityId;
	}

	public String getExpBuyInfo() {
		return expBuyInfo;
	}

	public void setExpBuyInfo(String expBuyInfo) {
		this.expBuyInfo = expBuyInfo;
	}

	public String getOrderItems() {
		return orderItems;
	}

	public void setOrderItems(String orderItems) {
		this.orderItems = orderItems;
	}

	public List<Integer> getExpBuyList() {
		return expBuyList;
	}

	public void setExpBuyList(List<Integer> expBuyList) {
		this.expBuyList = expBuyList;
	}

	public void setHistoryItems(String historyItems) {
		this.historyItems = historyItems;
	}

	public String getHistoryItems() {
		return historyItems;
	}

	public List<OrderItem> getOrderList() {
		return orderList;
	}

	public List<OrderItem> getHistoryOrderList() {
		return historyOrderList;
	}
	
	/**
	 * 初始化战令任务列表
	 * @param orderList
	 */
	public void resetOrderList(List<OrderItem> orderList) {
		this.orderList = orderList;
		this.notifyUpdate();
	}
	
	/**
	 * 重置战令经验购买数据
	 */
	public void resetExpBuyInfo(){
		this.expBuyList = new ArrayList<>();
		this.notifyUpdate();
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
	public int getActivityType() {
		return ActivityType.ORDER_ACTIVITY.intValue();
	}
}
