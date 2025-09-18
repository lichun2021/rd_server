package com.hawk.activity.type.impl.dressTreasure.entity;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.persistence.Column;

import com.hawk.activity.type.impl.exchangeTip.IExchangeTipEntity;
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
@Table(name="activity_dress_treasure")
public class DressTreasureEntity extends HawkDBEntity implements IActivityDataEntity, IExchangeTipEntity {

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
	@Column(name = "exchangeMsg", nullable = false)
	private String exchangeMsg;
	
	
    @IndexProp(id = 5)
	@Column(name = "awards", nullable = false)
	private String awards;
	
    @IndexProp(id = 6)
	@Column(name = "randomId", nullable = false)
	private int randomId;
	
    @IndexProp(id = 7)
	@Column(name = "awardScoreFrom", nullable = false)
	private int awardScoreFrom;
	
    @IndexProp(id = 8)
	@Column(name = "awardScoreTo", nullable = false)
	private int awardScoreTo;
	
    @IndexProp(id = 9)
	@Column(name = "resetCount", nullable = false)
	private int resetCount;

    @IndexProp(id = 10)
	@Column(name = "createTime", nullable = false)
	private long createTime;

    @IndexProp(id = 11)
	@Column(name = "updateTime", nullable = false)
	private long updateTime;

    @IndexProp(id = 12)
	@Column(name = "invalid", nullable = false)
	private boolean invalid;

	/** 兑换提醒信息 */
	@IndexProp(id = 13)
	@Column(name = "tips", nullable = false)
	private String tips;
	
	@Transient
	private List<Integer> awardList = new CopyOnWriteArrayList<Integer>();
	
	@Transient
	private Map<Integer, Integer> exchangeNumMap = new ConcurrentHashMap<>();

	/** 兑换提醒 */
	@Transient
	private Set<Integer> tipSet = new HashSet<>();
	
	public DressTreasureEntity(){}
	
	public DressTreasureEntity(String playerId, int termId) {
		this.playerId = playerId;
		this.termId = termId;
	}
	
	
	@Override
	public void beforeWrite() {
		this.exchangeMsg = SerializeHelper.mapToString(exchangeNumMap);
		this.awards = SerializeHelper.collectionToString(this.awardList,SerializeHelper.BETWEEN_ITEMS);
		this.tips = SerializeHelper.collectionToString(this.tipSet,SerializeHelper.ATTRIBUTE_SPLIT);
	}

	@Override
	public void afterRead() {
		SerializeHelper.stringToMap(this.exchangeMsg, Integer.class, Integer.class,this.exchangeNumMap);
		SerializeHelper.stringToList(Integer.class, this.awards, SerializeHelper.BETWEEN_ITEMS,this.awardList);
		this.tipSet = SerializeHelper.stringToSet(Integer.class, this.tips, SerializeHelper.ATTRIBUTE_SPLIT,null);
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
		return invalid;
	}

	@Override
	protected void setInvalid(boolean invalid) {
		this.invalid = invalid;
	}

	public int getTermId() {
		return termId;
	}

	public void setTermId(int termId) {
		this.termId = termId;
	}
	
	public int getRandomId() {
		return randomId;
	}

	public void setRandomId(int randomId) {
		this.randomId = randomId;
	}

	public int getAwardScoreFrom() {
		return awardScoreFrom;
	}
	
	public void setAwardScoreFrom(int awardScoreFrom) {
		this.awardScoreFrom = awardScoreFrom;
	}

	public int getAwardScoreTo() {
		return awardScoreTo;
	}

	public void setAwardScoreTo(int awardScoreTo) {
		this.awardScoreTo = awardScoreTo;
	}

	public int getResetCount() {
		return resetCount;
	}

	public void setResetCount(int resetCount) {
		this.resetCount = resetCount;
	}

	public List<Integer> getAwardList() {
		return awardList;
	}


	public Map<Integer, Integer> getExchangeNumMap() {
		return exchangeNumMap;
	}
	
	
	public int getAwardCount(){
		return this.awardList.size();
	}
	
	public void addAwardId(int id){
		this.awardList.add(id);
		this.notifyUpdate();
	}


	public void clearAwards(){
		this.awardList.clear();
		this.notifyUpdate();
	}
	
	
	public int getExchangeCount(int exchangeId){
		return this.exchangeNumMap.getOrDefault(exchangeId,0);
	}
	
	
	public void addExchangeCount(int eid,int count){
		if(count <=0){
			return;
		}
		count += this.getExchangeCount(eid);
		this.exchangeNumMap.put(eid, count);
		this.notifyUpdate();
	}


	@Override
	public Set<Integer> getTipSet() {
		return tipSet;
	}

	@Override
	public void setTipSet(Set<Integer> tipSet) {
		this.tipSet = tipSet;
	}
	
	
}
