package com.hawk.activity.type.impl.pandoraBox.entity;

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
@Table(name="activity_pandora_box")
public class PandoraBoxEntity extends HawkDBEntity implements IActivityDataEntity{

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
	
	/** 已经免费爽过的次数 (如果配置隔天重置，则此值需要重置) **/
    @IndexProp(id = 4)
	@Column(name = "freeCount", nullable = true)
	private int freeCount;

    @IndexProp(id = 5)
	@Column(name = "createTime", nullable = false)
	private long createTime;

    @IndexProp(id = 6)
	@Column(name = "updateTime", nullable = false)
	private long updateTime;

    @IndexProp(id = 7)
	@Column(name = "storeInfo", nullable = false)
	private String storeInfo;
	
    @IndexProp(id = 8)
	@Column(name = "score", nullable = false)
	private int score;
	
    @IndexProp(id = 9)
	@Column(name = "invalid", nullable = false)
	private boolean invalid;
	
    @IndexProp(id = 10)
	@Column(name ="lotteryCount", nullable = false)
	private int lotteryCount;
	
	@Transient
	private Map<Integer, Integer> storeInfos = new HashMap<>();
	
	public PandoraBoxEntity() {
	}
	
	public PandoraBoxEntity(String playerId) {
		this.playerId = playerId;
		this.storeInfo = "";
	}
	
	public PandoraBoxEntity(String playerId, int termId) {
		this.playerId = playerId;
		this.termId = termId;
		this.storeInfo = "";
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

	public String getStoreInfo() {
		return storeInfo;
	}

	public void setStoreInfo(String storeInfo) {
		this.storeInfo = storeInfo;
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

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}

	public int getFreeCount() {
		return freeCount;
	}

	public void setFreeCount(int freeCount) {
		this.freeCount = freeCount;
	}

	public Map<Integer, Integer> getStoreInfos() {
		return storeInfos;
	}

	public void setStoreInfos(Map<Integer, Integer> storeInfos) {
		this.storeInfos = storeInfos;
	}

	@Override
	public void beforeWrite() {
		this.storeInfo = SerializeHelper.mapToString(storeInfos);
	}

	@Override
	public void afterRead() {
		this.storeInfos = SerializeHelper.stringToMap(storeInfo);
	}
	
	@Override
	public boolean isInvalid() {
		return this.invalid;
	}

	@Override
	protected void setInvalid(boolean invalid) {
		this.invalid = invalid;		
	}		
	
	public int getBuyNum(Integer id) {
		Integer num = this.storeInfos.get(id);
		if (num == null) {
			return 0;
		} else {
			return num;
		}
	}
	
	public void addBuyNum(Integer id, int buyNum) {
		int oldNum = getBuyNum(id);
		this.storeInfos.put(id, oldNum + buyNum);		
		this.notifyUpdate();
	}

	public int getLotteryCount() {
		return lotteryCount;
	}

	public void setLotteryCount(int lotteryCount) {
		this.lotteryCount = lotteryCount;
	}
}
