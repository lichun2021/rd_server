package com.hawk.activity.type.impl.backFlow.returnGift.entity;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.Column;
import org.hawk.annotation.IndexProp;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hawk.db.HawkDBEntity;
import org.hibernate.annotations.GenericGenerator;

import com.hawk.activity.type.IActivityDataEntity;
import com.hawk.serialize.string.SerializeHelper;

@Entity
@Table(name = "activity_return_gift")
public class ReturnGiftEntity extends HawkDBEntity implements IActivityDataEntity {

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

	/**
	 * 回流次数
	 */
    @IndexProp(id = 4)
	@Column(name = "backCount", nullable = false)
	private int backCount;

	/** 低折回馈购买信息 **/
    @IndexProp(id = 5)
	@Column(name = "buyInfos", nullable = true)
	private String buyInfos;
	
	/**
	 * 玩家回归奖励类型
	 */
    @IndexProp(id = 6)
	@Column(name = "backType", nullable = false)
	private int backType;
	
	/**
	 * 当前期开始时间
	 */
    @IndexProp(id = 7)
	@Column(name = "overTime", nullable = false)
	private long overTime;
	
	/**
	 * 当前期开始时间
	 */
    @IndexProp(id = 8)
	@Column(name = "startTime", nullable = false)
	private long startTime;

    @IndexProp(id = 9)
	@Column(name = "createTime", nullable = false)
	private long createTime;

    @IndexProp(id = 10)
	@Column(name = "updateTime", nullable = false)
	private long updateTime;

    @IndexProp(id = 11)
	@Column(name = "invalid", nullable = false)
	private boolean invalid;

	/** 已经购买的信息 **/
	@Transient
	private Map<Integer, Integer> buyMsg = new HashMap<Integer, Integer>();

	public ReturnGiftEntity() {
	}

	public ReturnGiftEntity(String playerId) {
		this.playerId = playerId;
	}

	public ReturnGiftEntity(String playerId, int termId) {
		this.playerId = playerId;
		this.termId = termId;
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

	public int getBackCount() {
		return backCount;
	}

	public void setBackCount(int backCount) {
		this.backCount = backCount;
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

	@Override
	public void beforeWrite() {
		buyInfos = SerializeHelper.mapToString(buyMsg);
	}

	@Override
	public void afterRead() {
		buyMsg = SerializeHelper.stringToMap(buyInfos, Integer.class, Integer.class);
	}
	
	/***
	 * 判断是否可以购买
	 * @param chestId
	 * @param count
	 * @param maxCount
	 * @return
	 */
	public boolean canBuy(int chestId, int count, int maxCount){
		if(count > maxCount || count <= 0){
			return false;
		}
		Integer buyObj = buyMsg.get(chestId);
		int already = buyObj == null ? 0 : buyObj.intValue();
		if(already + count > maxCount){
			return false;
		}
		return true;
	}
	
	public void onPlayerBuy(int chestId, int count){
		if(count <= 0){
			return;
		}
		Integer buyObj = buyMsg.get(chestId);
		int already = buyObj == null ? 0 : buyObj.intValue();
		int nowBuy = already + count;
		buyMsg.put(chestId, nowBuy);
	}

	
	public String getBuyInfos() {
		return buyInfos;
	}

	public void setBuyInfos(String buyInfos) {
		this.buyInfos = buyInfos;
	}

	public Map<Integer, Integer> getBuyMsg() {
		return buyMsg;
	}

	public void setBuyMsg(Map<Integer, Integer> buyMsg) {
		this.buyMsg = buyMsg;
	}

	public void resetBuyMsg(){
		this.buyMsg = new HashMap<>();
		this.notifyUpdate();
	}

	@Override
	public String getPrimaryKey() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setPrimaryKey(String arg0) {
		// TODO Auto-generated method stub

	}

	public int getBackType() {
		return backType;
	}

	public void setBackType(int backType) {
		this.backType = backType;
	}

	public long getOverTime() {
		return overTime;
	}

	public void setOverTime(long overTime) {
		this.overTime = overTime;
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	
}
