package com.hawk.game.module.mechacore.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import org.hawk.annotation.IndexProp;
import org.hawk.db.HawkDBEntity;
import org.hibernate.annotations.GenericGenerator;

import com.hawk.game.protocol.MechaCore.MechaCoreModuleAttrPB;
import com.hawk.game.protocol.MechaCore.MechaCoreModulePB;
import com.hawk.game.protocol.MechaCore.MechaCoreSuitType;
import com.hawk.serialize.string.SerializeHelper;

/**
 * 机甲核心的模块数据
 * @author lating
 */
@Entity
@Table(name = "mecha_core_module")
public class MechaCoreModuleEntity extends HawkDBEntity {
	/**
	 * 唯一id，uuid
	 */
	@Id
	@GenericGenerator(name = "uuid", strategy = "org.hawk.uuid.HawkUUIDGenerator")
	@GeneratedValue(generator = "uuid")
	@Column(name = "id", unique = true, nullable = false)
    @IndexProp(id = 1)
	private String id;
	
	/**
	 * 玩家id
	 */
	@Column(name = "playerId", nullable = false)
    @IndexProp(id = 2)
	private String playerId;
	
	/**
	 * 配置id
	 */
	@Column(name = "cfgId", nullable = false)
    @IndexProp(id = 3)
	private int cfgId;

	/**
	 * 品质
	 */
	@Column(name = "quality", nullable = false)
    @IndexProp(id = 4)
	private int quality;
	
	/**
	 * 随机属性
	 */
	@Column(name = "randomAttr", nullable = false)
    @IndexProp(id = 5)
	private String randomAttr;
	
	/**
	 * 锁定
	 */
	@Column(name = "locked")
    @IndexProp(id = 6)
	protected boolean locked;
	
	/**
	 * 哪些套装装备了该模块
	 */
	@Column(name = "loadSuitInfo")
	@IndexProp(id = 7)
	private String loadSuitInfo = "";
	
	@Column(name = "createTime", nullable = false)
    @IndexProp(id = 8)
	protected long createTime = 0;

	@Column(name = "updateTime")
    @IndexProp(id = 9)
	protected long updateTime = 0;

	@Column(name = "invalid")
    @IndexProp(id = 10)
	protected boolean invalid;

	/**
	 * 随机属性
	 */
	@Transient
	private List<MechaCoreModuleEffObject> randomAttrEff = new ArrayList<>();
	
	/**
	 * 套装
	 */
	@Transient
	private List<Integer> suitList = new ArrayList<>();
	
	@Override
	public void beforeWrite() {
		randomAttr = SerializeHelper.collectionToString(randomAttrEff, SerializeHelper.BETWEEN_ITEMS, SerializeHelper.ATTRIBUTE_SPLIT);
		loadSuitInfo = SerializeHelper.collectionToString(suitList);
	}

	@Override
	public void afterRead() {
		randomAttrEff = SerializeHelper.stringToList(MechaCoreModuleEffObject.class, randomAttr, SerializeHelper.BETWEEN_ITEMS, SerializeHelper.ATTRIBUTE_SPLIT, new ArrayList<>());
		suitList = SerializeHelper.stringToList(Integer.class, loadSuitInfo);
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

	public int getQuality() {
		return quality;
	}

	public void setQuality(int quality) {
		this.quality = quality;
	}

	public String getRandomAttr() {
		return randomAttr;
	}

	public void setRandomAttr(String randomAttr) {
		this.randomAttr = randomAttr;
	}
	
	public String getLoadSuitInfo() {
		return loadSuitInfo;
	}

	public void setLoadSuitInfo(String loadSuitInfo) {
		this.loadSuitInfo = loadSuitInfo;
	}

	public List<Integer> getSuitList() {
		return suitList;
	}
	
	public void addSuit(int suit) {
		if (!suitList.contains(suit)) {
			suitList.add(suit);
			notifyUpdate();
		}
	}
	
	public void removeSuit(int suit) {
		suitList.remove(Integer.valueOf(suit));
		notifyUpdate();
	}
	
	/**
	 * 是否已装载
	 * @return
	 */
	public boolean isLoaded() {
		return !suitList.isEmpty();
	}
	
	public boolean isLocked() {
		return locked;
	}

	public void setLocked(boolean locked) {
		this.locked = locked;
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

	public List<MechaCoreModuleEffObject> getRandomAttrEff() {
		return randomAttrEff;
	}
	
	public void addRandomAttrEff(MechaCoreModuleEffObject effect) {
		randomAttrEff.add(effect);
		notifyUpdate();
	}
	
	public MechaCoreModuleEffObject getRandomAttr(int attrId) {
		Optional<MechaCoreModuleEffObject> op = randomAttrEff.stream().filter(e -> e.getAttrId() == attrId).findAny();
		if (op.isPresent()) {
			return op.get();
		}
		return null;
	}
	
	public void removeRandomAttr(MechaCoreModuleEffObject attrObj) {
		randomAttrEff.remove(attrObj);
		notifyUpdate();
	}

	@Override
	public String getPrimaryKey() {
		return this.id;
	}

	@Override
	public void setPrimaryKey(String primaryKey) {
		this.id = primaryKey;
	}
	
	public String getOwnerKey() {
		return playerId;
	}

	public MechaCoreModulePB.Builder toBuilder() {
		MechaCoreModulePB.Builder moduleInfo = MechaCoreModulePB.newBuilder();
		moduleInfo.setUuid(this.getId());
		moduleInfo.setCfgId(this.getCfgId());
		moduleInfo.setQuality(this.getQuality());
		moduleInfo.setLocked(this.isLocked() ? 1 : 0);
		for (MechaCoreModuleEffObject effObj : this.getRandomAttrEff()) {
			MechaCoreModuleAttrPB.Builder attrInfo = MechaCoreModuleAttrPB.newBuilder();
			attrInfo.setAttrId(effObj.getAttrId());
			attrInfo.setAttrType(effObj.getEffectType());
			attrInfo.setAttrValue(effObj.getEffectValue());
			moduleInfo.addModuleAttr(attrInfo);
		}
		for(int suit : this.getSuitList()) {
			moduleInfo.addSuit(MechaCoreSuitType.valueOf(suit));
		}
		return moduleInfo;
	}
	
	public MechaCoreModuleEntity copy() {
		MechaCoreModuleEntity copyEntity = new MechaCoreModuleEntity();
		copyEntity.setId(this.id);
		copyEntity.setPlayerId(this.playerId);
		copyEntity.setCfgId(this.cfgId);
		copyEntity.setQuality(this.quality);
		copyEntity.setRandomAttr(randomAttr);
		copyEntity.setLocked(this.locked);
		copyEntity.setLoadSuitInfo(this.loadSuitInfo);
		copyEntity.setCreateTime(this.createTime);
		copyEntity.setUpdateTime(this.updateTime);
		copyEntity.setInvalid(this.invalid);
		return copyEntity;
	}
	
	public void refresh(MechaCoreModuleEntity entity) {
		this.setPlayerId(entity.getPlayerId());
		this.setCfgId(entity.getCfgId());
		this.setQuality(entity.getQuality());
		this.setRandomAttr(entity.getRandomAttr());
		this.setLocked(entity.isLocked());
		this.setLoadSuitInfo(entity.getLoadSuitInfo());
		this.afterRead();
	}
}
