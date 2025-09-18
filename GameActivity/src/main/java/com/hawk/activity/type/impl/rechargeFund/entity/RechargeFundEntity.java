package com.hawk.activity.type.impl.rechargeFund.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
@Table(name="activity_recharge_fund")
public class RechargeFundEntity extends HawkDBEntity implements IActivityDataEntity{

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
	@Column(name = "rechargeNum", nullable = false)
	private int rechargeNum;
	
    @IndexProp(id = 5)
	@Column(name = "investInfo", nullable = false)
	private String investInfo;
	
    @IndexProp(id = 6)
	@Column(name = "diyReward", nullable = false)
	private String diyReward;
	
    @IndexProp(id = 7)
	@Column(name = "rewardedInfo", nullable = false)
	private String rewardedInfo;
	
    @IndexProp(id = 8)
	@Column(name = "createTime", nullable = false)
	private long createTime;

    @IndexProp(id = 9)
	@Column(name = "updateTime", nullable = false)
	private long updateTime;

    @IndexProp(id = 10)
	@Column(name = "invalid", nullable = false)
	private boolean invalid;
	
	@Transient
	private Map<Integer, String> diyMap = new HashMap<>();
	
	@Transient
	private List<Integer> investList = new ArrayList<>();
	
	@Transient
	private List<Integer> rewardedList = new ArrayList<>();
	
	public RechargeFundEntity() {
	}
	
	public RechargeFundEntity(String playerId) {
		this.playerId = playerId;
	}
	
	public RechargeFundEntity(String playerId, int termId) {
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

	public String getDiyReward() {
		return diyReward;
	}

	public void setDiyReward(String diyReward) {
		this.diyReward = diyReward;
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
	public void beforeWrite() {
		this.diyReward = SerializeHelper.mapToString(this.diyMap);
		this.investInfo = SerializeHelper.collectionToString(this.investList, SerializeHelper.ELEMENT_DELIMITER);
		this.rewardedInfo = SerializeHelper.collectionToString(this.rewardedList, SerializeHelper.ELEMENT_DELIMITER);
	}

	@Override
	public void afterRead() {
		this.diyMap = SerializeHelper.stringToMap(this.diyReward, Integer.class, String.class);
		this.investList = SerializeHelper.stringToList(Integer.class, this.investInfo);
		this.rewardedList = SerializeHelper.stringToList(Integer.class, this.rewardedInfo);
	}
	
	public int getRechargeNum() {
		return rechargeNum;
	}

	public void setRechargeNum(int rechargeNum) {
		this.rechargeNum = rechargeNum;
	}

	public String getInvestInfo() {
		return investInfo;
	}

	public void setInvestInfo(String investInfo) {
		this.investInfo = investInfo;
	}

	public Map<Integer, String> getDiyMap() {
		return diyMap;
	}
	
	public void modifyDiyMap(Map<Integer, String> selectMap) {
		if (selectMap == null || selectMap.isEmpty()) {
			return;
		}
		this.diyMap.putAll(selectMap);
		this.notifyUpdate();
	}

	public void setDiyMap(Map<Integer, String> diyMap) {
		this.diyMap = diyMap;
	}

	public String getRewardedInfo() {
		return rewardedInfo;
	}

	public List<Integer> getInvestList() {
		return investList;
	}
	
	public void addInvestId(int giftId) {
		this.investList.add(giftId);
		notifyUpdate();
	}

	public List<Integer> getRewardedList() {
		return rewardedList;
	}
	
	public void addRewardedId(int awardId) {
		this.rewardedList.add(awardId);
		notifyUpdate();
	}

	@Override
	public boolean isInvalid() {
		return this.invalid;
	}

	@Override
	protected void setInvalid(boolean invalid) {
		this.invalid = invalid;		
	}		
}
