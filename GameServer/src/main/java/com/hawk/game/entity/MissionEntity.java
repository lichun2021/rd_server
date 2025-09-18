package com.hawk.game.entity;

import org.hawk.annotation.IndexProp;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hawk.db.HawkDBEntity;
import org.hawk.os.HawkOSOperator;

/**
 * 任务实体对象。。。
 *
 * @author luke
 */
@Entity
@Table(name = "mission")
public class MissionEntity extends HawkDBEntity {
	@Id
	@Column(name = "id", unique = true, nullable = false)
    @IndexProp(id = 1)
	private String id;

	@Column(name = "playerId", nullable = false)
    @IndexProp(id = 2)
	private String playerId = "";

	@Column(name = "cfgId", nullable = false)
    @IndexProp(id = 3)
	private int cfgId;

	@Column(name = "num", nullable = false)
    @IndexProp(id = 4)
	private int num = 0;

	@Column(name = "state", nullable = false)
    @IndexProp(id = 5)
	private int state = 0;
	
	@Column(name = "unfinishPreMission")
    @IndexProp(id = 6)
	private String unfinishPreMission;

	@Column(name = "createTime", nullable = false)
    @IndexProp(id = 7)
	protected long createTime = 0;

	@Column(name = "updateTime")
    @IndexProp(id = 8)
	protected long updateTime = 0;

	@Column(name = "invalid")
    @IndexProp(id = 9)
	protected boolean invalid;

	@Transient
	private int typeId;
	
	@Transient
	private List<Integer> unfinishPreMissionIds = new ArrayList<>();
	
	public MissionEntity() {
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

	public int getCfgId() {
		return cfgId;
	}

	public void setCfgId(int cfgId) {
		this.cfgId = cfgId;
	}

	public int getTypeId() {
		return typeId;
	}

	public void resetTypeId(int typeId) {
		this.typeId = typeId;
	}

	public int getNum() {
		return num;
	}

	public void setNum(int num) {
		this.num = num;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}
	
	public String getUnfinishPreMission() {
		return unfinishPreMission;
	}

	public void setUnfinishPreMission(String unfinishPreMission) {
		this.unfinishPreMission = unfinishPreMission;
	}
	
	public void addUnfinishPreMission(List<Integer> preMissionIds) {
		for (Integer id : preMissionIds) {
			unfinishPreMissionIds.add(id);
		}
		
		this.notifyUpdate();
	}
	
	public void removeUnfinishPreMission(Integer preMissionIds) {
		unfinishPreMissionIds.remove(preMissionIds);
		this.notifyUpdate();
	}
	
	public List<Integer> getUnfinishPreMissions() {
		return unfinishPreMissionIds;
	}
	
	@Override
	public void beforeWrite() {
		if (unfinishPreMissionIds != null && !unfinishPreMissionIds.isEmpty()) {
			StringBuilder sb = new StringBuilder();
			for (int id : unfinishPreMissionIds) {
				sb.append(id).append(",");
			}
			unfinishPreMission = sb.deleteCharAt(sb.length() - 1).toString();
		}
	}

	@Override
	public void afterRead() {
		unfinishPreMissionIds = new ArrayList<>();
		if (!HawkOSOperator.isEmptyString(unfinishPreMission)) {
			String[] missionIds = unfinishPreMission.split(",");
			for (String missionId : missionIds) {
				unfinishPreMissionIds.add(Integer.valueOf(missionId));
			}
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
