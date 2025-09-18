package com.hawk.activity.type.impl.redPackage.entity;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
import com.hawk.serialize.string.SerializeHelper;

@Entity
@Table(name = "activity_red_package")
public class RedPackageEntity extends AchieveActivityEntity implements IActivityDataEntity {

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
	@Column(name = "recieveInfo", nullable = false)
	private String recieveInfo;
	
    @IndexProp(id = 5)
	@Column(name = "createTime", nullable = false)
	private long createTime;

    @IndexProp(id = 6)
	@Column(name = "updateTime", nullable = false)
	private long updateTime;

    @IndexProp(id = 7)
	@Column(name = "invalid", nullable = false)
	private boolean invalid;
	
	
	
	@Transient
	private Map<Integer, Integer> recieveMap = new ConcurrentHashMap<Integer, Integer>();
	
	
	public RedPackageEntity() {}
	
	public RedPackageEntity(String playerId, int termId){
		this.playerId = playerId;
		this.termId = termId;
	}
	
	@Override
	public void beforeWrite() {
		this.recieveInfo = SerializeHelper.mapToString(this.recieveMap);
	}

	@Override
	public void afterRead() {
		SerializeHelper.stringToMap(this.recieveInfo, Integer.class, Integer.class, this.recieveMap);
	}

	@Override
	public String getPrimaryKey() {
		return this.id;
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

	public String getRecieveInfo() {
		return recieveInfo;
	}

	public void setRecieveInfo(String recieveInfo) {
		this.recieveInfo = recieveInfo;
	}
	
	
	
	public boolean isRecieve(int stageId){
		return this.recieveMap.containsKey(stageId);
	}
	
	
	public void addRecieve(int stage,int score){
		if(this.recieveMap.containsKey(stage)){
			return;
		}
		this.recieveMap.put(stage, score);
		this.notifyUpdate();
	}
	
	
	public int getScore(int stage){
		if(this.recieveMap.containsKey(stage)){
			return this.recieveMap.get(stage);
		}
		return 0;
	}
	
}
