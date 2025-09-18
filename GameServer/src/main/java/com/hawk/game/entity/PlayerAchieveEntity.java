package com.hawk.game.entity;

import org.hawk.annotation.IndexProp;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hawk.db.HawkDBEntity;
import org.hawk.os.HawkOSOperator;
import org.hibernate.annotations.GenericGenerator;

import com.hawk.game.item.PlayerAchieveItem;

/**
 * 玩家成就实体
 * @author golden
 *
 */
@Entity
@Table(name = "player_achieve")
public class PlayerAchieveEntity extends HawkDBEntity {
	@Id
	@GenericGenerator(name = "uuid", strategy = "org.hawk.uuid.HawkUUIDGenerator")
	@GeneratedValue(generator = "uuid")
	@Column(name = "id", unique = true, nullable = false)
    @IndexProp(id = 1)
	private String id;

	@Column(name = "playerId", nullable = false)
    @IndexProp(id = 2)
	private String playerId = "";

	// 任务列表 结构: achieveType_currValue_awardTimes
	@Column(name = "missions", nullable = false)
    @IndexProp(id = 3)
	private String missions = "";

	@Column(name = "createTime", nullable = false)
    @IndexProp(id = 4)
	protected long createTime = 0;

	@Column(name = "updateTime")
    @IndexProp(id = 5)
	protected long updateTime = 0;

	@Column(name = "invalid")
    @IndexProp(id = 6)
	protected boolean invalid;

	@Transient
	private List<PlayerAchieveItem> missionItems;

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

	public String getMissions() {
		return missions;
	}

	public void setMissions(String missions) {
		this.missions = missions;
	}

	public long getCreateTime() {
		return createTime;
	}

	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}

	public long getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(long updateTime) {
		this.updateTime = updateTime;
	}

	public boolean isInvalid() {
		return invalid;
	}

	public void setInvalid(boolean invalid) {
		this.invalid = invalid;
	}

	public List<PlayerAchieveItem> getMissionItems() {
		return missionItems;
	}

	public void setMissionItems(List<PlayerAchieveItem> missionItems) {
		this.missionItems = missionItems;
	}

	public void updateMissionItems(List<PlayerAchieveItem> missionItems) {
		this.missionItems = missionItems;
		notifyUpdate();
	}

	public void updateBattleMissionItem(PlayerAchieveItem item) {
		for (int i = 0; i < missionItems.size(); i++) {
			PlayerAchieveItem missionItem = missionItems.get(i);
			if (missionItem.getCfgId() == item.getCfgId()) {
				missionItem = item;
			}
		}
		this.notifyUpdate();
	}
	
	/**
	 * 获取指定配置的成就item
	 * 
	 * @param cfgId
	 * @return
	 */
	public PlayerAchieveItem getAchieveItem(int groupId) {
		for (int i = 0; i < missionItems.size(); i++) {
			PlayerAchieveItem missionItem = missionItems.get(i);
			if (missionItem.getCfgId() == groupId) {
				return missionItem;
			}
		}
		return null;
	}
	
	@Override
	public void afterRead() {
		missionItems = new ArrayList<PlayerAchieveItem>();
		if (!HawkOSOperator.isEmptyString(missions)) {
			String[] missionArr = missions.split(",");
			for (String missionStr : missionArr) {
				String[] mission = missionStr.split("_");
				PlayerAchieveItem missionItem = new PlayerAchieveItem(Integer.parseInt(mission[0]), 0, Integer.parseInt(mission[2]), Integer.parseInt(mission[3]));
				missionItem.setValue(Long.parseLong(mission[1]));
				missionItems.add(missionItem);
			}
		}
	}

	@Override
	public void beforeWrite() {
		if (missionItems == null) {
			return;
		}
		
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < missionItems.size(); i++) {
			if (i > 0) {
				result.append(",");
			}
			result.append(missionItems.get(i).toString());
		}
		missions = result.toString();
	}
	
	@Override
	public String getPrimaryKey() {
		return id;
	}

	@Override
	public void setPrimaryKey(String primaryKey) {
		id = primaryKey;
	}
	
	public String getOwnerKey() {
		return playerId;
	}
}
