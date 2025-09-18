package com.hawk.activity.type.impl.continuousRecharge.entity;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import org.hawk.annotation.IndexProp;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.GenericGenerator;

import com.hawk.activity.AchieveActivityEntity;
import com.hawk.activity.type.IActivityDataEntity;
import com.hawk.activity.type.impl.continuousRecharge.item.ContinuousRechargeItem;
import com.hawk.serialize.string.SerializeHelper;

/**
 * 连续充值活动实体
 * @author golden
 *
 */
@Entity
@Table(name = "activity_continuous_recharge")
public class ContinuousRechargeEntity extends AchieveActivityEntity implements IActivityDataEntity {

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
	@Column(name = "historyRecharge", nullable = false)
	private String historyRecharge;
	
    @IndexProp(id = 5)
	@Column(name = "currentRecharge", nullable = false)
	private String currentRecharge;
	
    @IndexProp(id = 6)
	@Column(name = "createTime", nullable = false)
	private long createTime;

    @IndexProp(id = 7)
	@Column(name = "updateTime", nullable = false)
	private long updateTime;

    @IndexProp(id = 8)
	@Column(name = "invalid", nullable = false)
	private boolean invalid;

	@Transient
	List<ContinuousRechargeItem> history;
	
	@Transient
	ContinuousRechargeItem current;
	
	public ContinuousRechargeEntity() {
		
	}
	
	public ContinuousRechargeEntity(String playerId, int termId) {
		this.playerId = playerId;
		this.termId = termId;
		this.history = new ArrayList<>();
		this.current = new ContinuousRechargeItem(1, 0);
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

	public String getHistoryRecharge() {
		return historyRecharge;
	}

	public void setHistoryRecharge(String historyRecharge) {
		this.historyRecharge = historyRecharge;
	}

	public String getCurrentRecharge() {
		return currentRecharge;
	}

	public void setCurrentRecharge(String currentRecharge) {
		this.currentRecharge = currentRecharge;
	}

	public long getCreateTime() {
		return createTime;
	}

	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}

	public long getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(long updateTime) {
		this.updateTime = updateTime;
	}

	public boolean isInvalid() {
		return invalid;
	}

	public void setInvalid(boolean invalid) {
		this.invalid = invalid;
	}

	public List<ContinuousRechargeItem> getHistory() {
		return history;
	}

	public ContinuousRechargeItem getCurrent() {
		return current;
	}

	/**
	 * 添加当前数量
	 * @param add
	 */
	public void addCurrentNum(int add) {
		current.addCount(add);
		notifyUpdate();
	}
	
	/**
	 * 添加历史记录
	 * @param add
	 */
	public void addHistory(ContinuousRechargeItem add) {
		ContinuousRechargeItem item = new ContinuousRechargeItem();
		item.setDay(add.getDay());
		item.setCount(add.getCount());
		item.setReceived(add.getReceived());
		
		history.add(item);
		notifyUpdate();
	}
	
	/**
	 * 重置当前
	 */
	public void resetCurrent() {
		int day = history.size() + 1;
		current = new ContinuousRechargeItem(day, 0);
		notifyUpdate();
	}
	
	@Override
	public void afterRead(){		
		history = SerializeHelper.stringToList(ContinuousRechargeItem.class, historyRecharge);
		current = new ContinuousRechargeItem(currentRecharge);
	}
	
	@Override
	public void beforeWrite(){		
		historyRecharge = SerializeHelper.collectionToString(history, SerializeHelper.ELEMENT_DELIMITER, SerializeHelper.ATTRIBUTE_SPLIT);
		currentRecharge = current.toString();
	}
	
	@Override
	public String getPrimaryKey() {
		return this.id;
	}

	@Override
	public void setPrimaryKey(String primaryKey) {
		this.id = primaryKey;
	}
}
