package com.hawk.game.module.lianmengyqzz.march.entitiy;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hawk.annotation.IndexProp;
import org.hawk.db.HawkDBEntity;
import org.hibernate.annotations.GenericGenerator;


@Entity
@Table(name = "player_yqzz")
public class PlayerYQZZEntity extends HawkDBEntity {
	@Id
	@GenericGenerator(name = "uuid", strategy = "org.hawk.uuid.HawkUUIDGenerator")
	@GeneratedValue(generator = "uuid")
	@Column(name = "id", unique = true, nullable = false)
	@IndexProp(id = 1)
	private String id;
	@Column(name = "playerId", nullable = false)
	@IndexProp(id = 2)
	private String playerId = "";
	
	@Column(name = "termId", nullable = false)
	@IndexProp(id = 3)
	private int termId;
	
	@Column(name = "achieveSerialized", nullable = false)
	@IndexProp(id = 4)
	private String achieveSerialized;
	
	@Column(name = "leaveBattleTime")
	@IndexProp(id = 5)
	protected long leaveBattleTime;
	
	@Column(name = "createTime", nullable = false)
	@IndexProp(id = 6)
	protected long createTime = 0;

	@Column(name = "updateTime")
	@IndexProp(id = 7)
	protected long updateTime = 0;

	@Column(name = "invalid")
	@IndexProp(id = 8)
	protected boolean invalid;
	
	@Column(name = "playerGuild")
	@IndexProp(id = 9)
	protected String playerGuild = "";
	
	
	
	

	@Transient
	private PlayerYQZZData yqzzDataObj;

	public PlayerYQZZEntity() {
	}

	@Override
	public void beforeWrite() {
		if (null != yqzzDataObj) {
			achieveSerialized = yqzzDataObj.serializAchieve();
		}
		super.beforeWrite();
	}

	@Override
	public void afterRead() {
		PlayerYQZZData.create(this);
		super.afterRead();
	}

	public void recordYQZZObj(PlayerYQZZData data) {
		this.yqzzDataObj = data;
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

	public String getAchieveSerialized() {
		return achieveSerialized;
	}

	public void setAchieveSerialized(String achieveSerialized) {
		this.achieveSerialized = achieveSerialized;
	}
	
	public void setLeaveBattleTime(long leaveBattleTime) {
		this.leaveBattleTime = leaveBattleTime;
	}
	
	public long getLeaveBattleTime() {
		return leaveBattleTime;
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

	
	public String getPlayerGuild() {
		return playerGuild;
	}
	
	public void setPlayerGuild(String playerGuild) {
		this.playerGuild = playerGuild;
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
	public String getOwnerKey() {
		return playerId;
	}

	public PlayerYQZZData getPlayerYQZZData() {
		return this.yqzzDataObj;
	}

	

}
