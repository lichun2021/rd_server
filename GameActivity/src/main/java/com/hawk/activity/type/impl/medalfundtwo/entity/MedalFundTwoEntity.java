package com.hawk.activity.type.impl.medalfundtwo.entity;

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
@Table(name = "activity_medal_fund_two")
public class MedalFundTwoEntity extends HawkDBEntity implements IActivityDataEntity{
	@Id
	@GenericGenerator(name = "uuid", strategy = "org.hawk.uuid.HawkUUIDGenerator")
    @GeneratedValue(generator = "uuid")
    @IndexProp(id = 1)
	@Column(name = "id", unique = true, nullable = false)
	private String id;
	
    @IndexProp(id = 2)
	@Column(name = "playerId", nullable = false)
	private String playerId;
	
    @IndexProp(id = 3)
	@Column(name = "termId", nullable = false)
	private int termId;
	
	/**购买数据 */
    @IndexProp(id = 4)
	@Column(name = "buyInfo", nullable = false)
	private String buyInfo;
	
	/**日常任务数据 */
    @IndexProp(id = 5)
	@Column(name = "dailyTask", nullable = false)
	private String dailyTask;
	
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
	private Map<Integer, Integer> daliyTaskMap = new HashMap<>();
	
	@Transient
	private Map<Integer, Integer> buyInfoMap = new HashMap<>();

	public MedalFundTwoEntity() {
	}
	
	public MedalFundTwoEntity(String playerId) {
		this.playerId = playerId;
		this.daliyTaskMap = new HashMap<>();
		this.buyInfoMap = new HashMap<>();
	}
	
	public MedalFundTwoEntity(String playerId, int termId) {
		this.playerId = playerId;
		this.termId = termId;
		this.daliyTaskMap = new HashMap<>();
		this.buyInfoMap = new HashMap<>();
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


	public String getDaliyTask() {
		return dailyTask;
	}

	public void setDaliyTask(String daliyTask) {
		this.dailyTask = daliyTask;
	}


	public String getBuyInfo() {
		return buyInfo;
	}

	public void setBuyInfo(String buyInfo) {
		this.buyInfo = buyInfo;
	}

	public Map<Integer, Integer> getDaliyTaskMap() {
		return daliyTaskMap;
	}

	public void setDaliyTaskMap(Map<Integer, Integer> daliyTaskMap) {
		this.daliyTaskMap = daliyTaskMap;
	}

	public Map<Integer, Integer> getBuyInfoMap() {
		return buyInfoMap;
	}

	public void setBuyInfoMap(Map<Integer, Integer> buyInfoMap) {
		this.buyInfoMap = buyInfoMap;
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
	public void beforeWrite() {
		this.dailyTask = SerializeHelper.mapToString(daliyTaskMap);
		this.buyInfo = SerializeHelper.mapToString(buyInfoMap);
	}
	
	@Override
	public void afterRead() {
		this.daliyTaskMap = SerializeHelper.stringToMap(dailyTask);
		this.buyInfoMap = SerializeHelper.stringToMap(buyInfo);
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
