package com.hawk.game.entity;

import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hawk.annotation.IndexProp;
import org.hawk.db.HawkDBEntity;
import org.hibernate.annotations.GenericGenerator;

import com.hawk.game.player.hero.PlayerHero;

@Entity
@Table(name = "hero")
public class HeroEntity extends HawkDBEntity {
	@Id
	@GenericGenerator(name = "uuid", strategy = "org.hawk.uuid.HawkUUIDGenerator")
	@GeneratedValue(generator = "uuid")
	@Column(name = "id", unique = true, nullable = false)
	@IndexProp(id = 1)
	private String id;

	@Column(name = "heroId", nullable = false)
	@IndexProp(id = 2)
	private int heroId;

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

	@Column(name = "attrSerialized", nullable = false)
	@IndexProp(id = 13)
	private String attrSerialized;

	@Column(name = "equipSerialized", nullable = false)
	@IndexProp(id = 14)
	private String equipSerialized;

	@Column(name = "talentSerialized", nullable = false)
	@IndexProp(id = 19)
	private String talentSerialized;

	@Column(name = "talentOpen", nullable = false)
	@IndexProp(id = 20)
	private int talentOpen;

	@Column(name = "playerId", nullable = false)
	@IndexProp(id = 15)
	private String playerId = "";

	@Column(name = "createTime", nullable = false)
	@IndexProp(id = 16)
	protected long createTime = 0;

	@Column(name = "updateTime")
	@IndexProp(id = 17)
	protected long updateTime = 0;

	@Column(name = "invalid")
	@IndexProp(id = 18)
	protected boolean invalid;
	
	@Column(name = "skinSerialized")
	@IndexProp(id = 21)
	private String skinSerialized;
	
	@Column(name = "soulSerialized")
	@IndexProp(id = 22)
	private String soulSerialized="";

	@Transient
	private PlayerHero heroObj;

	@Transient
	private boolean changed;

	public HeroEntity() {
		// asyncLandPeriod(GsConfig.getInstance().getEntitySyncPeriod());
	}

	@Override
	public void beforeWrite() {
		if (null != heroObj) {
			this.skillSerialized = heroObj.serializSkill();
			this.passiveSkillSerialized = heroObj.serializPassiveSkill();
			this.talentSerialized = heroObj.serializTalent();
			this.skinSerialized = heroObj.serializSkin();
			this.attrSerialized = "";
			this.equipSerialized = "";
			this.soulSerialized = heroObj.getSoul().serializ();
		}
		this.changed = false;
		super.beforeWrite();
	}

	@Override
	public void afterRead() {
		PlayerHero.create(this);
		super.afterRead();
	}

	public boolean isChanged() {
		return changed;
	}

	public void setChanged(boolean changed) {
		this.changed = changed;
	}

	public PlayerHero getHeroObj() {
		return getHeroObj(true);
	}
	
	public PlayerHero getHeroObj(boolean effvalLoad) {
		if (Objects.nonNull(heroObj) && effvalLoad && !heroObj.isEfvalLoad()) {
			heroObj.loadEffVal();
		}
		return heroObj;
	}

	public void recordHeroObj(PlayerHero heroObj) {
		this.heroObj = heroObj;
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

	public int getHeroId() {
		return heroId;
	}

	public void setHeroId(int heroId) {
		this.heroId = heroId;
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

	public String getAttrSerialized() {
		return attrSerialized;
	}

	public void setAttrSerialized(String attrSerialized) {
		this.attrSerialized = attrSerialized;
	}

	public String getEquipSerialized() {
		return equipSerialized;
	}

	public void setEquipSerialized(String equipSerialized) {
		this.equipSerialized = equipSerialized;
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

	public String getTalentSerialized() {
		return talentSerialized;
	}

	public void setTalentSerialized(String talentSerialized) {
		this.talentSerialized = talentSerialized;
	}

	public String getOwnerKey() {
		return playerId;
	}

	public int getTalentOpen() {
		return talentOpen;
	}

	public void setTalentOpen(int talentOpen) {
		this.talentOpen = talentOpen;
	}

	public String getSkinSerialized() {
		return skinSerialized;
	}

	public void setSkinSerialized(String skinSerialized) {
		this.skinSerialized = skinSerialized;
	}

	public String getSoulSerialized() {
		return soulSerialized;
	}

	public void setSoulSerialized(String soulSerialized) {
		this.soulSerialized = soulSerialized;
	}

}
