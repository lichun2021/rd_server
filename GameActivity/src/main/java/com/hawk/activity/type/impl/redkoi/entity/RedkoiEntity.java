package com.hawk.activity.type.impl.redkoi.entity;

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
@Table(name = "activity_redkoi")
public class RedkoiEntity extends HawkDBEntity implements IActivityDataEntity {

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

    @IndexProp(id = 4)
	@Column(name = "turnId", nullable = false)
	private String turnId;
	
    @IndexProp(id = 5)
	@Column(name = "freeTimes", nullable = false)
	private int freeTimes;
	
    @IndexProp(id = 6)
	@Column(name = "curChoseAward", nullable = false)
	private int curChoseAward;
	
    @IndexProp(id = 7)
	@Column(name = "wishPoints", nullable = false)
	private String wishPoints;
	
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
	private Map<Integer,Integer> wishPointMap = new HashMap<Integer,Integer>(); 
	
	public RedkoiEntity(){}
	
	public RedkoiEntity(String playerId,int tremId) {
		this.playerId = playerId;
		this.termId = tremId;
		this.freeTimes = 0;
		this.curChoseAward = 0;
		this.wishPoints = "";
		this.turnId = "";
		
	}
	
	@Override
	public long getCreateTime() {
		return createTime;
	}

	@Override
	public String getPrimaryKey() {
		return this.id;
	}

	@Override
	public long getUpdateTime() {
		return this.updateTime;
	}

	@Override
	public boolean isInvalid() {
		return this.invalid;
	}

	@Override
	protected void setCreateTime(long arg0) {
		this.createTime = arg0;
	}

	@Override
	protected void setInvalid(boolean arg0) {
		this.invalid = arg0;
	}

	@Override
	public void setPrimaryKey(String arg0) {
		this.id = arg0;
	}

	@Override
	protected void setUpdateTime(long arg0) {
		this.updateTime = arg0;
	}
	
	
	@Override
	public void beforeWrite() {
		this.wishPoints = SerializeHelper.mapToString(this.wishPointMap);
	}
	
	@Override
	public void afterRead() {
		this.wishPointMap = SerializeHelper.stringToMap(this.wishPoints, Integer.class, Integer.class);
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

	public String getWishPoints() {
		return wishPoints;
	}

	public void setWishPoints(String wishPoints) {
		this.wishPoints = wishPoints;
	}


	public int getFreeTimes() {
		return freeTimes;
	}

	public void setFreeTimes(int freeTimes) {
		this.freeTimes = freeTimes;
	}

	public Map<Integer, Integer> getWishPointMap() {
		return wishPointMap;
	}

	public void setWishPointMap(Map<Integer, Integer> wishPointMap) {
		this.wishPointMap = wishPointMap;
	}

	public int getCurChoseAward() {
		return curChoseAward;
	}

	public void setCurChoseAward(int curChoseAward) {
		this.curChoseAward = curChoseAward;
	}

	public String getTurnId() {
		return turnId;
	}

	public void setTurnId(String turnId) {
		this.turnId = turnId;
	}

	
	

	
	
	
	
	
}
