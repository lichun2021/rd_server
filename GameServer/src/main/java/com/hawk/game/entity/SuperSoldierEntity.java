package com.hawk.game.entity;

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
import com.hawk.game.player.supersoldier.SuperSoldier;

@Entity
@Table(name = "super_soldier")
public class SuperSoldierEntity extends HawkDBEntity {
	@Id
	@GenericGenerator(name = "uuid", strategy = "org.hawk.uuid.HawkUUIDGenerator")
	@GeneratedValue(generator = "uuid")
	@Column(name = "id", unique = true, nullable = false)
	@IndexProp(id = 1)
	private String id;

	@Column(name = "soldierId", nullable = false)
	@IndexProp(id = 2)
	private int soldierId;

	@Column(name = "state")
	@IndexProp(id = 3)
	private int state;

	@Column(name = "star")
	@IndexProp(id = 4)
	private int star;

	@Column(name = "step")
	@IndexProp(id = 5)
	private int step;

	@Column(name = "skin")
	@IndexProp(id = 6)
	private int skin;

	@Column(name = "shareCount")
	@IndexProp(id = 7)
	private int shareCount;

	@Column(name = "office")
	@IndexProp(id = 8)
	private int office;

	@Column(name = "cityDefense")
	@IndexProp(id = 9)
	private int cityDefense;// 城防官

	@Column(name = "exp", nullable = false)
	@IndexProp(id = 10)
	private int exp;

	@Column(name = "skillSerialized", nullable = false)
	@IndexProp(id = 11)
	private String skillSerialized;

	@Column(name = "passiveSkillSerialized", nullable = false)
	@IndexProp(id = 12)
	private String passiveSkillSerialized;

	@Column(name = "playerId", nullable = false)
	@IndexProp(id = 13)
	private String playerId = "";

	@Column(name = "createTime", nullable = false)
	@IndexProp(id = 14)
	protected long createTime = 0;

	@Column(name = "updateTime")
	@IndexProp(id = 15)
	protected long updateTime = 0;

	@Column(name = "invalid")
	@IndexProp(id = 16)
	protected boolean invalid;

	@Column(name = "anyWhereUnlock")
	@IndexProp(id = 17)
	protected int anyWhereUnlock; // 无处不在
	
	@Column(name = "energySerialized", nullable = false)
	@IndexProp(id = 18)
	private String energySerialized; // 赋能

	@Column(name = "skinSerialized", nullable = false)
	@IndexProp(id = 19)
	private String skinSerialized; // 赋能
	
	@Transient
	private SuperSoldier soldierObj;

	@Transient
	private boolean changed;

	public SuperSoldierEntity() {
		asyncLandPeriod(GsConfig.getInstance().getEntitySyncPeriod());
	}

	@Override
	public void beforeWrite() {
		if (null != soldierObj) {
			this.skillSerialized = soldierObj.serializSkill();
			this.passiveSkillSerialized = soldierObj.serializPassiveSkill();
			this.energySerialized = soldierObj.getSoldierEnergy().serializ();
			this.skinSerialized = soldierObj.serializUnlockSkin();
		}
		this.changed = false;
		super.beforeWrite();
	}

	@Override
	public void afterRead() {
		SuperSoldier.create(this);
		super.afterRead();
	}

	public boolean isChanged() {
		return changed;
	}

	public void setChanged(boolean changed) {
		this.changed = changed;
	}

	public SuperSoldier getSoldierObj() {
		if (!soldierObj.isEfvalLoad()) {
			soldierObj.loadEffVal();
		}
		return soldierObj;
	}

	public void recordSObj(SuperSoldier soldierObj) {
		this.soldierObj = soldierObj;
	}

	public int getOffice() {
		return office;
	}

	public void setOffice(int office) {
		this.office = office;
	}

	public int getSkin() {
		return skin;
	}

	public void setSkin(int skin) {
		this.skin = skin;
	}

	public int getSoldierId() {
		return soldierId;
	}

	public void setSoldierId(int soldierId) {
		this.soldierId = soldierId;
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

	public String getSkillSerialized() {
		return skillSerialized;
	}

	public void setSkillSerialized(String skillSerialized) {
		this.skillSerialized = skillSerialized;
	}

	public int getExp() {
		return exp;
	}

	public void setExp(int exp) {
		this.exp = exp;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public int getStar() {
		return star;
	}

	public void setStar(int star) {
		this.star = star;
	}

	public String getPassiveSkillSerialized() {
		return passiveSkillSerialized;
	}

	public void setPassiveSkillSerialized(String passiveSkillSerialized) {
		this.passiveSkillSerialized = passiveSkillSerialized;
	}

	public int getCityDefense() {
		return cityDefense;
	}

	public void setCityDefense(int cityDefense) {
		this.cityDefense = cityDefense;
	}

	public int getStep() {
		return step;
	}

	public void setStep(int step) {
		this.step = step;
	}

	public int getShareCount() {
		return shareCount;
	}

	public void setShareCount(int shareCount) {
		this.shareCount = shareCount;
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

	public int getAnyWhereUnlock() {
		return anyWhereUnlock;
	}

	public void setAnyWhereUnlock(int anyWhereUnlock) {
		this.anyWhereUnlock = anyWhereUnlock;
	}

	public String getEnergySerialized() {
		return energySerialized;
	}

	public void setEnergySerialized(String energySerialized) {
		this.energySerialized = energySerialized;
	}

	public String getSkinSerialized() {
		return skinSerialized;
	}

	public void setSkinSerialized(String skinSerialized) {
		this.skinSerialized = skinSerialized;
	}
}
