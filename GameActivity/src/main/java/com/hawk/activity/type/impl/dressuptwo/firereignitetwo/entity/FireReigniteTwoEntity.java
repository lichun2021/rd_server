package com.hawk.activity.type.impl.dressuptwo.firereignitetwo.entity;

import com.hawk.activity.type.IActivityDataEntity;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.serialize.string.SerializeHelper;
import org.hawk.db.HawkDBEntity;
import org.hawk.os.HawkOSOperator;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Column;
import org.hawk.annotation.IndexProp;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 圣诞节系列活动二:冬日装扮活动
 * @author hf
 */
@Entity
@Table(name = "activity_dress_fire_reignite_two")
public class FireReigniteTwoEntity extends HawkDBEntity implements IActivityDataEntity{

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
	
	/** 经验总数*/
    @IndexProp(id = 4)
	@Column(name = "exp", unique = true, nullable = false)
	private int exp;
	/** 已经领取的宝箱数据*/
    @IndexProp(id = 5)
	@Column(name = "recBoxInfo", nullable = false)
	private String recBoxInfo;

	/** 今天兑换物品的次数*/
    @IndexProp(id = 6)
	@Column(name = "exchangeNum", nullable = false)
	private int exchangeNum;
	
    @IndexProp(id = 7)
	@Column(name = "createTime", nullable = false)
	private long createTime;

    @IndexProp(id = 8)
	@Column(name = "updateTime", nullable = false)
	private long updateTime;

    @IndexProp(id = 9)
	@Column(name = "invalid", nullable = false)
	private boolean invalid;

	@Transient
	private Set<Integer> recBoxSet = new HashSet<>();

	public FireReigniteTwoEntity() {
	}
	
	public FireReigniteTwoEntity(String playerId, int termId) {
		this.playerId = playerId;
		this.termId = termId;
		this.exp = 0;
		this.exchangeNum = 0;
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

	public int getExp() {
		return exp;
	}

	public void setExp(int exp) {
		this.exp = exp;
	}
	public void addExp(int value) {
		this.exp = exp + value;
		notifyUpdate();
	}
	public int getRecBoxNum() {
		return recBoxSet.size();
	}
	public void addRecBox(int boxId) {
		this.recBoxSet.add(boxId);
		notifyUpdate();
	}

	public int getExchangeNum() {
		return exchangeNum;
	}

	public void setExchangeNum(int exchangeNum) {
		this.exchangeNum = exchangeNum;
	}

	public void addExchangeNum(int num) {
		this.exchangeNum = exchangeNum + num;
		notifyUpdate();
	}


	@Override
	public void beforeWrite() {
		this.recBoxInfo = SerializeHelper.collectionToString(this.recBoxSet,SerializeHelper.ATTRIBUTE_SPLIT);
	}

	@Override
	public void afterRead() {
		if (!HawkOSOperator.isEmptyString(recBoxInfo)) {
			this.recBoxSet = SerializeHelper.stringToSet(Integer.class, recBoxInfo, SerializeHelper.ATTRIBUTE_SPLIT);
		}

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

}
