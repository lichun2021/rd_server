package com.hawk.activity.type.impl.blood_corps.entity;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

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
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.game.protocol.Activity.BloodScoreInfo;
import com.hawk.gamelib.player.PowerChangeData;
import com.hawk.serialize.string.SerializeHelper;

@Entity
@Table(name = "activity_blood_corps")
public class BloodCorpsEntity  extends AchieveActivityEntity implements IActivityDataEntity {

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
	@Column(name = "totalScore", nullable = false)
	private int totalScore;
	
    @IndexProp(id = 5)
	@Column(name = "buildScore", nullable = false)
	private int buildScore;
	
    @IndexProp(id = 6)
	@Column(name = "techScore", nullable = false)
	private int techScore;
	
    @IndexProp(id = 7)
	@Column(name = "armyScore", nullable = false)
	private int armyScore;
	
	/** 活动成就项数据 */
    @IndexProp(id = 8)
	@Column(name = "achieveItems", nullable = false)
	private String achieveItems;
	
    @IndexProp(id = 9)
	@Column(name = "createTime", nullable = false)
	private long createTime;

    @IndexProp(id = 10)
	@Column(name = "updateTime", nullable = false)
	private long updateTime;

    @IndexProp(id = 11)
	@Column(name = "invalid", nullable = false)
	private boolean invalid;
	
	@Transient
	private List<AchieveItem> itemList = new CopyOnWriteArrayList<AchieveItem>();

	public BloodCorpsEntity() {
	}
	public BloodCorpsEntity(String playerId, int termId) {
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
	
	public int getTotalScore() {
		return totalScore;
	}
	
	public void setTotalScore(int totalScore) {
		this.totalScore = totalScore;
	}

	public int getBuildScore() {
		return buildScore;
	}

	public void setBuildScore(int buildScore) {
		this.buildScore = buildScore;
	}

	public int getTechScore() {
		return techScore;
	}

	public void setTechScore(int techScore) {
		this.techScore = techScore;
	}

	public int getArmyScore() {
		return armyScore;
	}

	public void setArmyScore(int armyScore) {
		this.armyScore = armyScore;
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
	
	public void resetItemList(List<AchieveItem> itemList) {
		this.itemList = itemList;
		this.notifyUpdate();
	}
	
	/**
	 * 添加相关积分
	 * @param changeData
	 */
	public void addScore(PowerChangeData changeData) {
		boolean needUpdate = false;
		int totalAdd = 0;
		if (changeData.getBuildBattleChange() != 0) {
			int buildScoreChange = changeData.getBuildBattleChange();
			this.buildScore += buildScoreChange;
			totalAdd += buildScoreChange;
			needUpdate = true;
		}
		
		if ((changeData.getTechBattleChange() + changeData.getPlantScienceBattlePoint())> 0) {
			int techScoreChange = changeData.getTechBattleChange() + changeData.getPlantScienceBattlePoint();
			this.techScore += techScoreChange;
			totalAdd += techScoreChange;
			needUpdate = true;
		}
		if (changeData.getArmyBattleChange() > 0) {
			int armyScoreChange = changeData.getArmyBattleChange();
			this.armyScore += armyScoreChange;
			totalAdd += armyScoreChange;
			needUpdate = true;
		}
		
		this.totalScore += totalAdd;
		
		if (needUpdate) {
			notifyUpdate();
		}
	}
	
	/**
	 * 重置活动积分
	 */
	public void resetScore() {
		boolean needUpdate = false;
		if (this.totalScore != 0) {
			this.totalScore = 0;
			needUpdate = true;
		}

		if (this.buildScore != 0) {
			this.buildScore = 0;
			needUpdate = true;
		}

		if (this.techScore != 0) {
			this.techScore = 0;
			needUpdate = true;
		}

		if (this.armyScore != 0) {
			this.armyScore = 0;
			needUpdate = true;
		}

		if (needUpdate) {
			notifyUpdate();
		}
	}
	
	/**
	 * 构建玩家活动积分信息
	 * @return
	 */
	public BloodScoreInfo.Builder genScoreInfo(){
		BloodScoreInfo.Builder scoreInfo = BloodScoreInfo.newBuilder();
		scoreInfo.setTotalScore(this.totalScore);
		scoreInfo.setBuildScore(this.buildScore);
		scoreInfo.setTechScore(this.techScore);
		scoreInfo.setArmyScore(this.armyScore);
		return scoreInfo;
	}
	
	@Override
	public void beforeWrite() {
		this.achieveItems = SerializeHelper.collectionToString(this.itemList, SerializeHelper.ELEMENT_DELIMITER);
	}
	
	@Override
	public void afterRead() {
		this.itemList.clear();
		SerializeHelper.stringToList(AchieveItem.class, this.achieveItems, this.itemList);
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
