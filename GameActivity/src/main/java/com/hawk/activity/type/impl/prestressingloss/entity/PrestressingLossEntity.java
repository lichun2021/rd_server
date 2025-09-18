package com.hawk.activity.type.impl.prestressingloss.entity;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.persistence.Column;
import org.hawk.annotation.IndexProp;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkTime;
import org.hibernate.annotations.GenericGenerator;

import com.hawk.activity.AchieveActivityEntity;
import com.hawk.activity.type.IActivityDataEntity;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.prestressingloss.cfg.PrestressingLossKVCfg;
import com.hawk.serialize.string.SerializeHelper;

/**
 * 预流失（干预）活动
 * 
 * @author lating
 *
 */
@Entity
@Table(name = "activity_prestressing_loss")
public class PrestressingLossEntity extends AchieveActivityEntity implements IActivityDataEntity{
	
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
	@Column(name = "loginDays", nullable = false)
	private int loginDays;
	
	/** 活动成就项数据 */
    @IndexProp(id = 5)
	@Column(name = "achieveItems", nullable = false)
	private String achieveItems;
	
    @IndexProp(id = 6)
	@Column(name = "openTime", nullable = false)
	private long openTime;
	
    @IndexProp(id = 7)
	@Column(name = "loginTime", nullable = false)
	private long loginTime;
	
    @IndexProp(id = 8)
	@Column(name = "openTerm", nullable = false)
	private int openTerm;
	
	/** 冷却期实际值（单位ms） */
    @IndexProp(id = 9)
	@Column(name = "coolTimeVal", nullable = false)
	private long coolTimeVal;
	
	/** 空置期实际值（单位ms） */
    @IndexProp(id = 10)
	@Column(name = "vacancyTimeVal", nullable = false)
	private long vacancyTimeVal;

    @IndexProp(id = 11)
	@Column(name = "createTime", nullable = false)
	private long createTime;

    @IndexProp(id = 12)
	@Column(name = "updateTime", nullable = false)
	private long updateTime;

    @IndexProp(id = 13)
	@Column(name = "invalid", nullable = false)
	private boolean invalid;
	
	@Transient
	private long requestDataTime = 0;
	
	@Transient
	private long tickTime = 0;
	
	@Transient
	private boolean activityOpen = false;
	
	@Transient
	private List<AchieveItem> itemList = new CopyOnWriteArrayList<AchieveItem>();

	public PrestressingLossEntity() {
	}
	
	public PrestressingLossEntity(String playerId, int termId) {
		this.playerId = playerId;
		this.termId = termId;
		this.achieveItems = "";
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

	public int getLoginDays() {
		return loginDays;
	}

	public void setLoginDays(int loginDays) {
		this.loginDays = loginDays;
	}

	public void setItemList(List<AchieveItem> itemList) {
		this.itemList = itemList;
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
	
	public String getAchieveItems() {
		return achieveItems;
	}
	
	public void setAchieveItems(String achieveItems) {
		this.achieveItems = achieveItems;
	}
	
	public void addItem(AchieveItem item) {
		this.itemList.add(item);
		this.notifyUpdate();
	}
	
	public List<AchieveItem> getItemList() {
		return itemList;
	}
	
	public void resetItemList(List<AchieveItem> newList) {
		itemList.clear();
		itemList.addAll(newList);
	}
	
	public long getLoginTime() {
		return loginTime;
	}

	public void setLoginTime(long loginTime) {
		this.loginTime = loginTime;
	}
	
	public long getOpenTime() {
		return openTime;
	}

	public void setOpenTime(long openTime) {
		this.openTime = openTime;
	}
	
	public int getOpenTerm() {
		return openTerm;
	}

	public void setOpenTerm(int openTerm) {
		this.openTerm = openTerm;
	}
	
	public long getCoolTimeVal() {
		return coolTimeVal;
	}

	public void setCoolTimeVal(long coolTimeVal) {
		this.coolTimeVal = coolTimeVal;
	}

	public long getVacancyTimeVal() {
		return vacancyTimeVal;
	}

	public void setVacancyTimeVal(long vacancyTimeVal) {
		this.vacancyTimeVal = vacancyTimeVal;
	}
	
	public long getRequestDataTime() {
		return requestDataTime;
	}

	public void resetRequestDataTime(long requestDataTime) {
		this.requestDataTime = requestDataTime;
	}
	
	public long getTickTime() {
		return tickTime;
	}

	public void resetTickTime(long tickTime) {
		this.tickTime = tickTime;
	}

	public boolean isActivityOpen() {
		return activityOpen;
	}

	public void resetActivityOpen(boolean activityOpen) {
		this.activityOpen = activityOpen;
	}
	
	@Override
	public void beforeWrite() {
		this.achieveItems = SerializeHelper.collectionToString(this.itemList, SerializeHelper.ELEMENT_DELIMITER);
	}
	
	@Override
	public void afterRead() {
		this.itemList.clear();
		SerializeHelper.stringToList(AchieveItem.class, this.achieveItems, this.itemList);
		long now = HawkTime.getMillisecond();
		PrestressingLossKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(PrestressingLossKVCfg.class);
		if (now - this.openTime < cfg.getCircleTime()) {
			this.activityOpen = true;
		} 
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
