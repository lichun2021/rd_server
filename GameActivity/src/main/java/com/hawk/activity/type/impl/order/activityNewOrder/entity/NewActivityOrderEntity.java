package com.hawk.activity.type.impl.order.activityNewOrder.entity;

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
import com.hawk.activity.type.impl.order.entity.IOrderDateEntity;
import com.hawk.activity.type.impl.order.entity.OrderItem;
import com.hawk.serialize.string.SerializeHelper;

/**
 * 红警占领活动数据存储
 * @author Jesse
 *
 */
@Entity
@Table(name = "activity_order_new")
public class NewActivityOrderEntity extends HawkDBEntity implements IActivityDataEntity,IOrderDateEntity {

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

	/** 战令进阶id */
    @IndexProp(id = 4)
	@Column(name = "authorityId", nullable = false)
	private int authorityId;
	
	/** 战令经验 */
    @IndexProp(id = 5)
	@Column(name = "exp", nullable = false)
	private int exp;
	
	

	/** 领奖信息*/
    @IndexProp(id = 6)
	@Column(name = "rewardInfo", nullable = false)
	private String rewardInfo;
	/** 额外经验购买信息 */
    @IndexProp(id = 7)
	@Column(name = "expBuyInfo", nullable = false)
	private String expBuyInfo;

	/** 战令任务信息 */
    @IndexProp(id = 8)
	@Column(name = "orderItems", nullable = false)
	private String orderItems;


    @IndexProp(id = 9)
	@Column(name = "createTime", nullable = false)
	private long createTime;

    @IndexProp(id = 10)
	@Column(name = "updateTime", nullable = false)
	private long updateTime;

    @IndexProp(id = 11)
	@Column(name = "invalid", nullable = false)
	private boolean invalid;

	@Transient
	private List<OrderItem> orderList = new CopyOnWriteArrayList<>();

	@Transient
	private List<Integer> expBuyList = new CopyOnWriteArrayList<>();
	
	@Transient
	private List<Integer> rewardList = new CopyOnWriteArrayList<>();

	public NewActivityOrderEntity() {
	}

	public NewActivityOrderEntity(String playerId) {
		this.playerId = playerId;
	}

	public NewActivityOrderEntity(String playerId, int termId) {
		this.playerId = playerId;
		this.termId = termId;
	}

	@Override
	public void beforeWrite() {
		this.orderItems = SerializeHelper.collectionToString(this.orderList, SerializeHelper.ELEMENT_DELIMITER);
		this.expBuyInfo = SerializeHelper.collectionToString(expBuyList);
		this.rewardInfo = SerializeHelper.collectionToString(rewardList);
	}


	@Override
	public void afterRead() {
		this.orderList.clear();
		this.expBuyList.clear();
		this.rewardList.clear();
		SerializeHelper.stringToList(OrderItem.class, this.orderItems, this.orderList);
		SerializeHelper.stringToList(Integer.class, expBuyInfo, expBuyList);
		SerializeHelper.stringToList(Integer.class, rewardInfo, rewardList);
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

	

	public int getExp() {
		return exp;
	}

	public void setExp(int exp) {
		this.exp = exp;
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

	

	public List<OrderItem> getOrderList() {
		return orderList;
	}

	
	
	
	
	
	public String getRewardInfo() {
		return rewardInfo;
	}

	public void setRewardInfo(String rewardInfo) {
		this.rewardInfo = rewardInfo;
	}

	public List<Integer> getRewardList() {
		return rewardList;
	}

	public void setRewardList(List<Integer> rewardList) {
		this.rewardList = rewardList;
	}
	
	/**
	 * 添加等级领奖记录
	 * @param level
	 */
	public void addRewardAchive(int level){
		if(this.rewardList.contains(level)){
			return;
		}
		this.rewardList.add(level);
		this.notifyUpdate();
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
		return ActivityType.NEW_ORDER_ACTIVITY.intValue();
	}
}
