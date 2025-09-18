package com.hawk.activity.type.impl.roulette.entity;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import javax.persistence.Column;
import org.hawk.annotation.IndexProp;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hawk.db.HawkDBEntity;
import org.hawk.tuple.HawkTuple2;
import org.hibernate.annotations.GenericGenerator;

import com.hawk.activity.type.IActivityDataEntity;
import com.hawk.serialize.string.SerializeHelper;

@Entity
@Table(name="activity_roulette")
public class RouletteEntity extends HawkDBEntity implements IActivityDataEntity{

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
	@Column(name = "freeTimes", nullable = false)
	private int freeTimes;
	
    @IndexProp(id = 5)
	@Column(name = "boxReward", nullable = false)
	private String boxReward;
	
    @IndexProp(id = 6)
	@Column(name = "createTime", nullable = false)
	private long createTime;

    @IndexProp(id = 7)
	@Column(name = "updateTime", nullable = false)
	private long updateTime;

    @IndexProp(id = 8)
	@Column(name = "itemset", nullable = false)
	private String itemset;
	
    @IndexProp(id = 9)
	@Column(name = "score", nullable = false)
	private int score;
	
    @IndexProp(id = 10)
	@Column(name = "invalid", nullable = false)
	private boolean invalid;
	
	@Transient
	private Map<Integer, String> itemsetMap = new HashMap<>();
	
	@Transient
	private TreeMap<Integer, Integer> boxRewardMap = new TreeMap<>();
	
	public RouletteEntity() {
	}
	
	public RouletteEntity(String playerId) {
		this.playerId = playerId;
		this.itemset = "";
	}
	
	public RouletteEntity(String playerId, int termId) {
		this.playerId = playerId;
		this.termId = termId;
		this.itemset = "";
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
		return itemset;
	}

	public void setStoreInfo(String storeInfo) {
		this.itemset = storeInfo;
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

	public Map<Integer, String> getItemSetMap() {
		return itemsetMap;
	}

	public void setItemSetMap(int k, String v) {
		this.itemsetMap.put(k, v);
	}
	
	public HawkTuple2<Integer,Integer> getCurBoxReward(){
		if(boxRewardMap.isEmpty()){
			return new HawkTuple2<Integer,Integer>(1,0);
		}
		return new HawkTuple2<Integer,Integer>(boxRewardMap.lastEntry().getKey(), boxRewardMap.lastEntry().getValue());
	}
	
	public void setCurBoxTimes( int curId, int curTimes ){
		boxRewardMap.put(curId, curTimes);
	}
	
	public int getFreeTimes(){
		return this.freeTimes;
	}
	
	public void setFreeTimes(int val){
		this.freeTimes = val;
	}
	
	@Override
	public void beforeWrite() {
		this.itemset = SerializeHelper.mapToString(itemsetMap);
		this.boxReward = SerializeHelper.mapToString(boxRewardMap);
	}

	@Override
	public void afterRead() {
		this.itemsetMap = SerializeHelper.stringToMap(itemset,Integer.class,String.class);
		this.boxRewardMap.putAll(SerializeHelper.stringToMap(boxReward,Integer.class,Integer.class));
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
