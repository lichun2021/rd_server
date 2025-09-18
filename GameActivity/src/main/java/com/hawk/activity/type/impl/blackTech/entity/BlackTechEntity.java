package com.hawk.activity.type.impl.blackTech.entity;


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
import org.hawk.os.HawkException;
import org.hibernate.annotations.GenericGenerator;

import com.hawk.activity.type.IActivityDataEntity;
import com.hawk.serialize.string.SerializeHelper;

@Entity
@Table(name="activity_black_tech")
public class BlackTechEntity extends HawkDBEntity implements IActivityDataEntity {
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
	@Column(name = "createTime", nullable = false)
	private long createTime;

    @IndexProp(id = 5)
	@Column(name = "updateTime", nullable = false)
	private long updateTime;

    @IndexProp(id = 6)
	@Column(name = "invalid", nullable = false)
	private boolean invalid;
	
	//当前随机到的pool id buff 激活之后置为0
    @IndexProp(id = 7)
	@Column(name = "poolId", nullable = false)
	private int poolId;
	
	//当前激活的pool id,buff 激活之前为无效字段
    @IndexProp(id = 8)
	@Column(name = "buffId", nullable = false)
	private int buffId;
	
    @IndexProp(id = 9)
	@Column(name = "deadline", nullable = false)
	private long deadline;

    @IndexProp(id = 10)
	@Column(name = "drawTimes", nullable = false)
	private int drawTimes;
	
    @IndexProp(id = 11)
	@Column(name = "activeTimes", nullable = false)
	private int activeTimes;
	
    @IndexProp(id = 12)
	@Column(name = "buyRecord", nullable = false)
	private String buyRecord;
	
	@Transient
	private Map<Integer,Integer> buyRecordMap = new HashMap<>();
	public BlackTechEntity(){}
	
	public BlackTechEntity(String playerId, int termId) {
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
	
	public int getActiveTimes() {
		return this.activeTimes;
	}

	public void setActiveTimes(int cnt) {
		this.activeTimes = cnt;
	}

	public Map<Integer,Integer> getBuyRecordMap(){
		return this.buyRecordMap;
	}
	
	public int getBuyTimes( int id ){
		Integer val = buyRecordMap.get(id);
		if(null != val){
			return val;
		}
		return 0;
	}
	
	public void addBuyTimes(int id, int v){
		Integer val = buyRecordMap.get(id);
		if(null != val){
			buyRecordMap.put(id, val + v );
			return;
		}
		buyRecordMap.put(id, v);
	}
	
	public long getDeadline() {
		return this.deadline;
	}

	public void setDeadline(long deadline) {
		this.deadline = deadline;
	}
	
	public int getDrawTimes() {
		return drawTimes;
	}

	public void setDrawTimes(int drawTimes) {
		this.drawTimes = drawTimes;
	}

	public void setPoolId(int poolId) {
		this.poolId = poolId;
	}

	public int getPoolId() {
		return poolId;
	}
	
	public void setBuffId(int buffId) {
		this.buffId = buffId;
	}

	public int getBuffId() {
		return this.buffId;
	}
	@Override
	public void beforeWrite() {
		try{
			this.buyRecord = SerializeHelper.mapToString(buyRecordMap);
		}catch(Exception e){
			HawkException.catchException(e);
		}
	}

	@Override
	public void afterRead() {
		try {
			this.buyRecordMap = SerializeHelper.stringToMap(buyRecord, Integer.class, Integer.class);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
}
