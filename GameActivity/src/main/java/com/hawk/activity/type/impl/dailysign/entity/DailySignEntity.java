package com.hawk.activity.type.impl.dailysign.entity;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.persistence.Column;
import org.hawk.annotation.IndexProp;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hawk.os.HawkException;
import org.hibernate.annotations.GenericGenerator;

import com.googlecode.protobuf.format.JsonFormat;
import com.googlecode.protobuf.format.JsonFormat.ParseException;
import com.hawk.activity.AchieveActivityEntity;
import com.hawk.activity.type.IActivityDataEntity;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.game.protocol.Activity.PBDailySignTermRewards;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.serialize.string.SerializeHelper;

@Entity
@Table(name = "activity_daily_sign")
public class DailySignEntity extends AchieveActivityEntity implements IActivityDataEntity {
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
	@Column(name = "termRewards", nullable = false)
	private String termRewards;

    @IndexProp(id = 5)
	@Column(name = "achieveItems", nullable = false)
	private String achieveItems;
	
    @IndexProp(id = 6)
	@Column(name = "createTime", nullable = false)
	private long createTime;

    @IndexProp(id = 7)
	@Column(name = "updateTime", nullable = false)
	private long updateTime;

    @IndexProp(id = 8)
	@Column(name = "invalid", nullable = false)
	private boolean invalid;

    @IndexProp(id = 9)
	@Column(name = "signDays", nullable = false)
	private int signDays;

	/** 签到天数 **/
    @IndexProp(id = 10)
	@Column(name = "signToday", nullable = false)
	private int signToday;

	/** 补签天数 **/
    @IndexProp(id = 11)
	@Column(name = "resignDays", nullable = false)
	private int resignDays;

	/** 当前的配置是第几期的配置 **/
    @IndexProp(id = 12)
	@Column(name = "cfgPoolId", nullable = false)
	private int cfgPoolId;
	
	@Transient
	private List<AchieveItem> itemList = new CopyOnWriteArrayList<AchieveItem>();
	
	@Transient
	private List<RewardItem> termRewardList = new CopyOnWriteArrayList<RewardItem>();
	
	public DailySignEntity() {
	}

	public DailySignEntity(String playerId, int termId) {
		this.playerId = playerId;
		this.termId = termId;
		this.signDays = 0;
		this.signToday = 0;
		this.resignDays = 0;
		this.cfgPoolId = 0;
	}

	@Override
	public void beforeWrite() {
		this.achieveItems = SerializeHelper.collectionToString(this.itemList, SerializeHelper.ELEMENT_DELIMITER);
		PBDailySignTermRewards.Builder builder = PBDailySignTermRewards.newBuilder();
		builder.addAllTermRewards(termRewardList);
		this.termRewards = JsonFormat.printToString(builder.build());
	}

	@Override
	public void afterRead() {
		//做数据存失败修正
		if(0 == this.cfgPoolId){
			this.cfgPoolId = 1;
		}
		this.itemList = SerializeHelper.stringToList(AchieveItem.class, this.achieveItems);
		try {
			PBDailySignTermRewards.Builder builder = PBDailySignTermRewards.newBuilder();
			JsonFormat.merge(this.termRewards, builder);
			this.termRewardList.addAll(builder.getTermRewardsList());
		} catch ( ParseException e) {
			HawkException.catchException(e);
		}
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

	public int getSignDays() {
		return signDays;
	}

	public void setSignDays(int signDays) {
		this.signDays = signDays;
	}

	public int getSignToday() {
		return signToday;
	}

	public void setSignToday(int signToday) {
		this.signToday = signToday;
	}

	public int getResignDays() {
		return resignDays;
	}

	public void setResignDays(int resignDays) {
		this.resignDays = resignDays;
	}

	public int getTermId() {
		return termId;
	}

	public void setTermId(int termId) {
		this.termId = termId;
	}

	public int getCfgPoolId() {
		return cfgPoolId;
	}

	public void setCfgPoolId(int cfgPoolId) {
		this.cfgPoolId = cfgPoolId;
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
	
	@Override
	public List<AchieveItem> getItemList() {
		return itemList;
	}
	
	public void resetItemList(List<AchieveItem> itemList) {
		this.itemList = itemList;
		this.notifyUpdate();
	}
	
	
	public String getTermRewards() {
		return termRewards;
	}
	
	public void setTermRewards(String termRewards) {
		this.termRewards = termRewards;
	}
	
	public void addTermRewards(RewardItem item) {
		this.termRewardList.add(item);
	}
	
	public List<RewardItem> getTermRewardList() {
		return termRewardList;
	}
}
