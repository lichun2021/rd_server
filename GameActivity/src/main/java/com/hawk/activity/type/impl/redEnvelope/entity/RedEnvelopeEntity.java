package com.hawk.activity.type.impl.redEnvelope.entity;

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
import com.hawk.serialize.string.SerializeHelper;

@Entity
@Table(name = "activity_red_envelope")
public class RedEnvelopeEntity extends AchieveActivityEntity implements IActivityDataEntity {

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
	
	/** 系统红包领取详情 **/
    @IndexProp(id = 5)
	@Column(name = "recieveInfo", nullable = false)
	private String recieveInfo;

    @IndexProp(id = 6)
	@Column(name = "updateTime", nullable = false)
	private long updateTime;

    @IndexProp(id = 7)
	@Column(name = "invalid", nullable = false)
	private boolean invalid;
	
	@Transient
	private List<Integer> recieveLists = new ArrayList<>(); 
	
	public RedEnvelopeEntity() {}
	
	public RedEnvelopeEntity(String playerId, int termId){
		this.playerId = playerId;
		this.termId = termId;
	}
	
	@Override
	public void beforeWrite() {
		recieveInfo = SerializeHelper.collectionToString(recieveLists);
	}

	@Override
	public void afterRead() {
		this.recieveLists.clear();
		SerializeHelper.stringToList(Integer.class, recieveInfo, recieveLists);
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
	
	/***
	 * 抢到了一个红包
	 * @param stageId
	 */
	public void addRecieve(int stageId){
		this.recieveLists.add(stageId);
	}
	
	public boolean isRecieve(int stageId){
		return this.recieveLists.contains(stageId);
	}
}
