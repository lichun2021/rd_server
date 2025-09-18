package com.hawk.activity.type.impl.order.activityOrderTwo.entity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
@Table(name = "activity_order_two")
public class OrderTwoEntity extends HawkDBEntity implements IActivityDataEntity,IOrderDateEntity {

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

	/** 商店购买信息 */
    @IndexProp(id = 8)
	@Column(name = "buyInfo", nullable = false)
	private String buyInfo;

	/** 战令任务信息 */
    @IndexProp(id = 9)
	@Column(name = "orderItems", nullable = false)
	private String orderItems;

    @IndexProp(id = 10)
	@Column(name = "historyItems", nullable = false)
	private String historyItems;

    @IndexProp(id = 11)
	@Column(name = "weekNumber", nullable = false)
	private int weekNumber;

    @IndexProp(id = 12)
	@Column(name = "weekTime", nullable = false)
	private long weekTime;
	
    @IndexProp(id = 13)
	@Column(name = "createTime", nullable = false)
	private long createTime;

    @IndexProp(id = 14)
	@Column(name = "updateTime", nullable = false)
	private long updateTime;

    @IndexProp(id = 15)
	@Column(name = "invalid", nullable = false)
	private boolean invalid;
    
    @IndexProp(id = 16)
	@Column(name = "rewardNormalLevel", nullable = false)
	private String rewardNormalLevel;
    
    @IndexProp(id = 17)
	@Column(name = "rewardAdvanceLevel", nullable = false)
	private String rewardAdvanceLevel;
    

	@Transient
	private List<OrderItem> orderList = new CopyOnWriteArrayList<>();

	@Transient
	private List<OrderItem> historyOrderList = new CopyOnWriteArrayList<>();
	
	/** 已经购买的信息 **/
	@Transient
	private Map<Integer, Integer> buyMsg = new HashMap<Integer, Integer>();
	
	
	/** 普通领奖等级信息 **/
	@Transient
	private Map<Integer, Long> rewardNormalLevelMap = new HashMap<Integer, Long>();
	
	
	/** 进阶等奖等级信息 **/
	@Transient
	private Map<Integer, Long> rewardAdvanceLevelMap = new HashMap<Integer, Long>();
	

	public OrderTwoEntity() {
	}

	public OrderTwoEntity(String playerId) {
		this.playerId = playerId;
	}

	public OrderTwoEntity(String playerId, int termId) {
		this.playerId = playerId;
		this.termId = termId;
	}

	@Override
	public void beforeWrite() {
		this.orderItems = SerializeHelper.collectionToString(this.orderList, SerializeHelper.ELEMENT_DELIMITER);
		this.historyItems = SerializeHelper.collectionToString(this.historyOrderList, SerializeHelper.ELEMENT_DELIMITER);
		this.buyInfo = SerializeHelper.mapToString(this.buyMsg);
		this.rewardNormalLevel = SerializeHelper.mapToString(this.rewardNormalLevelMap);
		this.rewardAdvanceLevel = SerializeHelper.mapToString(this.rewardAdvanceLevelMap);
	}


	@Override
	public void afterRead() {
		this.orderList.clear();
		this.historyOrderList.clear();
		SerializeHelper.stringToList(OrderItem.class, this.orderItems, this.orderList);
		SerializeHelper.stringToList(OrderItem.class, this.historyItems, this.historyOrderList);
		buyMsg = SerializeHelper.stringToMap(this.buyInfo, Integer.class, Integer.class);
		rewardNormalLevelMap = SerializeHelper.stringToMap(this.rewardNormalLevel, Integer.class, Long.class);
		rewardAdvanceLevelMap = SerializeHelper.stringToMap(this.rewardAdvanceLevel, Integer.class, Long.class);
		
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

	

	public String getOrderItems() {
		return orderItems;
	}

	public void setOrderItems(String orderItems) {
		this.orderItems = orderItems;
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
	
	public Map<Integer, Integer> getBuyMsg() {
		return buyMsg;
	}
	
	/**
	 * 获取商店已购买数量
	 * @param shopId
	 * @return
	 */
	public int getBaughtCnt(int shopId){
		if(buyMsg.containsKey(shopId)){
			return buyMsg.get(shopId);
		}
		return 0;
	}
	
	/**
	 * 购买战令商店道具
	 * @param shopId
	 * @param count
	 */
	public void buyShopItem(int shopId, int count) {
		if (buyMsg.containsKey(shopId)) {
			buyMsg.put(shopId, buyMsg.get(shopId) + count);
		} else {
			buyMsg.put(shopId, count);
		}
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
		return ActivityType.ORDER_TWO_ACTIVITY.intValue();
	}

	public int getWeekNumber() {
		return weekNumber;
	}

	public void setWeekNumber(int weekNumber) {
		this.weekNumber = weekNumber;
	}
	public void addWeekNumber() {
		this.weekNumber++;
	}

	public long getWeekTime() {
		return weekTime;
	}

	public void setWeekTime(long weekTime) {
		this.weekTime = weekTime;
	}
	
	public Map<Integer, Long> getRewardAdvanceLevelMap() {
		return rewardAdvanceLevelMap;
	}
	
	public Map<Integer, Long> getRewardNormalLevelMap() {
		return rewardNormalLevelMap;
	}
	
	
	
	public void addRewardAdvanceLevel(int level,long time){
		rewardAdvanceLevelMap.put(level, time);
		this.notifyUpdate();
	}
	
	public void addRewardNormalLevel(int level,long time){
		rewardNormalLevelMap.put(level, time);
		this.notifyUpdate();
	}
	
}
