package com.hawk.game.entity;

import org.hawk.annotation.IndexProp;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hawk.db.HawkDBEntity;
import org.hawk.os.HawkTime;
import org.hibernate.annotations.GenericGenerator;

import com.hawk.game.GsConfig;

/**
 * 天赋实体对象
 *
 * @author
 */
@Entity
@Table(name = "talent")
public class TalentEntity extends HawkDBEntity {
	@Id
	@GenericGenerator(name = "uuid", strategy = "org.hawk.uuid.HawkUUIDGenerator")
	@GeneratedValue(generator = "uuid")
	@Column(name = "id", unique = true, nullable = false)
    @IndexProp(id = 1)
	private String id = null;

	@Column(name = "playerId", nullable = false)
    @IndexProp(id = 2)
	private String playerId = "";

	@Column(name = "talentId", nullable = false)
    @IndexProp(id = 3)
	private int talentId = 0;

	@Column(name = "level")
    @IndexProp(id = 4)
	private int level = 0;

	@Column(name = "type")
    @IndexProp(id = 5)
	private int type = 0;

	@Column(name = "skillId")
    @IndexProp(id = 6)
	private int skillId = 0;
	
	@Column(name = "skillRefTime")
    @IndexProp(id = 7)
	private long skillRefTime = 0;
	
	@Column(name = "skillState")
    @IndexProp(id = 8)
	private long skillState = 0;
	
	@Column(name = "createTime", nullable = false)
    @IndexProp(id = 9)
	protected long createTime = 0;

	@Column(name = "updateTime")
    @IndexProp(id = 10)
	protected long updateTime = 0;

	@Column(name = "invalid")
    @IndexProp(id = 11)
	protected boolean invalid;

	@Column(name = "castSkillTime")
    @IndexProp(id = 12)
	protected long castSkillTime;
	
	public TalentEntity() {
		this.createTime = HawkTime.getMillisecond();
		asyncLandPeriod(GsConfig.getInstance().getEntitySyncPeriod());
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

	public int getTalentId() {
		return talentId;
	}

	public void setTalentId(int talentId) {
		this.talentId = talentId;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	
	public int getSkillId() {
		return skillId;
	}

	public void setSkillId(int skillId) {
		this.skillId = skillId;
	}

	public long getSkillRefTime() {
		return skillRefTime;
	}

	public void setSkillRefTime(long skillRefTime) {
		this.skillRefTime = skillRefTime;
	}

	public long getSkillState() {
		return skillState;
	}

	public void setSkillState(long skillState) {
		this.skillState = skillState;
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

	public long getCastSkillTime() {
		return castSkillTime;
	}

	public void setCastSkillTime(long castSkillTime) {
		this.castSkillTime = castSkillTime;
	}
}
