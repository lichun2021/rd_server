package com.hawk.activity.type.impl.cakeShare.entity;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

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
@Table(name = "activity_cake_share")
public class CakeShareEntity  extends HawkDBEntity implements IActivityDataEntity {

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
	@Column(name = "cakeGifts", nullable = false)
	private String cakeGifts;
	
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
	private List<String> cakeGiftList = new CopyOnWriteArrayList<String>();
	
	
	public CakeShareEntity() {
	}
	
	public CakeShareEntity(String playerId, int termId) {
		this.playerId = playerId;
		this.termId = termId;
		this.cakeGifts = "";
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
	public String getPrimaryKey() {
		return this.id;
	}

	@Override
	public void setPrimaryKey(String primaryKey) {
		this.id = primaryKey;
	}
	
	@Override
	public void beforeWrite() {
		this.cakeGifts = SerializeHelper.collectionToString(this.cakeGiftList, SerializeHelper.ELEMENT_DELIMITER);
	}
	
	@Override
	public void afterRead() {
		this.cakeGiftList.clear();
		SerializeHelper.stringToList(String.class, this.cakeGifts, this.cakeGiftList);
	}

	public List<String> getCakeGiftList() {
		return cakeGiftList;
	}

	public void addCakeAwardRecord(String cakeId){
		if(this.cakeGiftList.contains(cakeId)){
			return;
		}
		this.cakeGiftList.add(cakeId);
		this.notifyUpdate();
	}
	
	
	
	
	
	

}
