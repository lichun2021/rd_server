package com.hawk.activity.type.impl.rewardOrder.entity;

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
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkException;
import org.hawk.os.HawkTime;
import org.hawk.util.JsonUtils;
import org.hibernate.annotations.GenericGenerator;
import com.hawk.activity.AchieveActivityEntity;
import com.hawk.activity.type.IActivityDataEntity;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.rewardOrder.cfg.RewardOrderCfg;
import com.hawk.serialize.string.SerializeHelper;

@Entity
@Table(name = "activity_reward_order")
public class RewardOrderEntity extends AchieveActivityEntity implements IActivityDataEntity {

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
	@Column(name = "achieveItems", nullable = true)
	private String achieveItems;
	
    @IndexProp(id = 5)
	@Column(name = "createTime", nullable = false)
	private long createTime;

    @IndexProp(id = 6)
	@Column(name = "updateTime", nullable = false)
	private long updateTime;
	
	/** 手动刷新的次数 (如果配置的每日刷新，则此值需要刷新) **/
    @IndexProp(id = 7)
	@Column(name = "refreshCnt", nullable = false)
	private int refreshCnt;

	/** 是否首次刷新悬赏令 **/
    @IndexProp(id = 8)
	@Column(name = "firstRefresh", nullable = true)
	private boolean firstRefresh;
	
	/** 显示的四个悬赏令 **/
    @IndexProp(id = 9)
	@Column(name = "orderInfo", nullable = true)
	private String orderInfo;
	
	/** 下次刷新的时间 **/
    @IndexProp(id = 10)
	@Column(name = "nextFreshTime", nullable = false)
	private long nextFreshTime;

    @IndexProp(id = 11)
	@Column(name = "invalid", nullable = false)
	private boolean invalid;
	
	/****
	 * 今日完成次数，跨天刷为0
	 */
    @IndexProp(id = 12)
	@Column(name = "finishCnt", nullable = false)
	private int finishCnt;
	
	/** 已经领取的悬赏令 **/
	@Transient
	private RewardOrder order;
	
	/** 展示给玩家的四个悬赏令 **/
	@Transient
	private List<RewardOrder> orderInfoList = new ArrayList<>();
	
	@Transient
	private List<AchieveItem> itemList = new CopyOnWriteArrayList<AchieveItem>();
	
	public RewardOrderEntity(){}
	
	public RewardOrderEntity(String playerId){
		this.playerId = playerId;
	}
	
	public RewardOrderEntity(String playerId, int termId){
		this.playerId = playerId;
		this.termId = termId;
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
		return false;
	}

	@Override
	protected void setInvalid(boolean invalid) {
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

	public RewardOrder getOrder() {
		return order;
	}

	public void setOrder(RewardOrder order) {
		this.order = order;
	}

	public boolean isFirstRefresh() {
		return firstRefresh;
	}

	public void setFirstRefresh(boolean firstRefresh) {
		this.firstRefresh = firstRefresh;
	}

	public String getOrderInfo() {
		return orderInfo;
	}

	public void setOrderInfo(String orderInfo) {
		this.orderInfo = orderInfo;
	}

	public List<RewardOrder> getOrderInfoList() {
		return orderInfoList;
	}

	public void setOrderInfoList(List<RewardOrder> orderInfoList) {
		this.orderInfoList = orderInfoList;
	}
	
	public long getNextFreshTime() {
		return nextFreshTime;
	}

	public void setNextFreshTime(long nextFreshTime) {
		this.nextFreshTime = nextFreshTime;
	}

	public void removeShowOrder(RewardOrder order){
		if(order != null){
			this.orderInfoList.remove(order);
		}
		orderInfo = JsonUtils.Object2Json(orderInfoList);
	}
	
	public void addOrder(RewardOrder order){
		if(order != null){
			this.orderInfoList.add(order);
		}
		orderInfo = JsonUtils.Object2Json(orderInfoList);
	}
	
	public boolean containOrder(int orderId){
		for(RewardOrder order : orderInfoList){
			if(order.getId() == orderId){
				return true;
			}
		}
		return false;
	}
	
	public void receiveOrder(int orderId){
		RewardOrder receiveOrder = null;
		for(RewardOrder order : orderInfoList){
			if(order.getId() == orderId){
				receiveOrder = order;
				order.setState(RewardOrder.TAKE);
				order.setBeginTime(HawkTime.getMillisecond());
				break;
			}
		}
		order = receiveOrder;
	}
	
	public void giveUpOrder(){
		removeShowOrder(order);
		order = null;
	}
	
	public void takeOrderReward(){
		removeShowOrder(order);
		order = null;
	}
	
	public void orderFail(){
		removeShowOrder(order);
		order = null;
	}

	@Override
	public void beforeWrite() {
		if(order != null){
			this.achieveItems = SerializeHelper.collectionToString(this.itemList, SerializeHelper.ELEMENT_DELIMITER);
		}
		orderInfo = JsonUtils.Object2Json(orderInfoList);
	}

	@Override
	public void afterRead() {
		if (achieveItems != null && !achieveItems.trim().equals("")) {
			this.itemList.clear();
			SerializeHelper.stringToList(AchieveItem.class, this.achieveItems, this.itemList);
		}
		decode();
	}
	
	private void decode(){
		try {
			orderInfoList = JsonUtils.String2List(orderInfo, RewardOrder.class);
			for(RewardOrder order : orderInfoList){
				if(order.getState() == RewardOrder.TAKE || order.getState() == RewardOrder.FINISH){
					this.order = order;
				}
				//设置config
				RewardOrderCfg config = HawkConfigManager.getInstance().getConfigByKey(RewardOrderCfg.class, order.getId());
				order.setConfig(config);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	public void addItem(AchieveItem item) {
		this.itemList.add(item);
		this.notifyUpdate();
	}
	
	public List<AchieveItem> getItemList() {
		return itemList;
	}
	
	public void clearItem(){
		this.itemList.clear();
		this.achieveItems = null;
		this.notifyUpdate();
	}
	
	/***
	 * 活动结束，同步赏令状态
	 */
	public void onSendMailReward(){
		if(order != null){
			order.setTakeRewar(true);
		}
	}
	
	public boolean systemFresh(long nowTime){
		return nowTime >= nextFreshTime;
	}

	public int getRefreshCnt() {
		return refreshCnt;
	}

	public void setRefreshCnt(int refreshCnt) {
		this.refreshCnt = refreshCnt;
	}

	public int getFinishCnt() {
		return finishCnt;
	}

	public void setFinishCnt(int finishCnt) {
		this.finishCnt = finishCnt;
	}
}
