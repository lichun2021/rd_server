package com.hawk.game.module.mechacore.entity;

import org.hawk.annotation.IndexProp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import org.hawk.db.HawkDBEntity;
import org.hibernate.annotations.GenericGenerator;
import com.hawk.game.GsConfig;
import com.hawk.game.module.mechacore.PlayerMechaCore;

@Entity
@Table(name = "mecha_core")
public class MechaCoreEntity extends HawkDBEntity {
	@Id
	@GenericGenerator(name = "uuid", strategy = "org.hawk.uuid.HawkUUIDGenerator")
	@GeneratedValue(generator = "uuid")
	@Column(name = "id", unique = true, nullable = false)
	@IndexProp(id = 1)
	private String id;
	
	@Column(name = "playerId", nullable = false)
	@IndexProp(id = 2)
	private String playerId = "";
	
	/**
	 * 机甲核心突破等级
	 */
	@Column(name = "rankLevel")
	@IndexProp(id = 3)
	private int rankLevel;

	/**
	 * 机甲核心科技等级
	 */
	@Column(name = "techInfo")
	@IndexProp(id = 4)
	private String techInfo = "";
	
	/**
	 * 槽位信息
	 */
	@Column(name = "slotInfo")
	@IndexProp(id = 5)
	private String slotInfo = "";
	
	/**
	 * 套装信息
	 */
	@Column(name = "suitInfo")
	@IndexProp(id = 6)
	private String suitInfo = "";
	
	/**
	 * 已解锁的套装数量
	 */
	@Column(name = "suitCount")
	@IndexProp(id = 7)
	private int suitCount;
	
	/**
	 * 当前生效的套装
	 */
	@Column(name = "workSuit")
	@IndexProp(id = 8)
	private int workSuit;
	
	/**
	 * 已解锁的外显id
	 */
	@Column(name = "unlockedCityShow")
	@IndexProp(id = 9)
	private String unlockedCityShow = "";
	
	@Column(name = "createTime", nullable = false)
	@IndexProp(id = 10)
	protected long createTime = 0;

	@Column(name = "updateTime")
	@IndexProp(id = 11)
	protected long updateTime = 0;

	@Column(name = "invalid")
	@IndexProp(id = 12)
	protected boolean invalid;

	@Transient
	private PlayerMechaCore mechaCore;
	
	public MechaCoreEntity() {
		asyncLandPeriod(GsConfig.getInstance().getEntitySyncPeriod());
	}

	@Override
	public void beforeWrite() {
		if (mechaCore != null) {
			techInfo = mechaCore.serializeTechInfo();
			slotInfo = mechaCore.serializeSlotInfo();
			suitInfo = mechaCore.serializeSuitInfo();
			unlockedCityShow = mechaCore.serializeUnlockedCityShow();
		}
		super.beforeWrite();
	}

	@Override
	public void afterRead() {
		PlayerMechaCore.create(this);
		super.afterRead();
	}

	public void recordMechaCoreObj(PlayerMechaCore mechaCoreObj) {
		this.mechaCore = mechaCoreObj;
	}
	
	public PlayerMechaCore getMechaCoreObj() {
		return getMechaCoreObj(true);
	}
	
	public PlayerMechaCore getMechaCoreObj(boolean effvalLoad) {
		if (mechaCore == null) {
			PlayerMechaCore.create(this);
		}
		if (effvalLoad && !mechaCore.isEfvalLoad()) {
			mechaCore.loadEffVal();
		}
		return mechaCore;
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

	public int getRankLevel() {
		return rankLevel;
	}

	public void setRankLevel(int rankLevel) {
		this.rankLevel = rankLevel;
	}
	
	public String getTechInfo() {
		return techInfo;
	}

	public void setTechInfo(String techInfo) {
		this.techInfo = techInfo;
	}

	public String getSlotInfo() {
		return slotInfo;
	}

	public void setSlotInfo(String slotInfo) {
		this.slotInfo = slotInfo;
	}
	
	public String getSuitInfo() {
		return suitInfo;
	}

	public void setSuitInfo(String suitInfo) {
		this.suitInfo = suitInfo;
	}

	public int getWorkSuit() {
		return workSuit;
	}

	public void setWorkSuit(int workSuit) {
		this.workSuit = workSuit;
	}
	
	public int getSuitCount() {
		return suitCount;
	}

	public void setSuitCount(int suitCount) {
		this.suitCount = suitCount;
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

	@Override
	public String getPrimaryKey() {
		return id;
	}

	@Override
	public void setPrimaryKey(String primaryKey) {
		this.id = primaryKey;
	}

	public String getOwnerKey() {
		return playerId;
	}

	public String getUnlockedCityShow() {
		return unlockedCityShow;
	}
	
}
