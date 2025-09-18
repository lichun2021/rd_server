package com.hawk.game.entity;

import com.hawk.serialize.string.SerializeHelper;
import org.hawk.annotation.IndexProp;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hawk.db.HawkDBEntity;

import com.hawk.game.cfgElement.ArmourStarExplores;
import com.hawk.game.module.lianmengfgyl.march.entity.FGYLPlayerEntity;
import com.hawk.game.player.equip.CommanderObject;
import org.hawk.os.HawkTime;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 指挥官实体对象
 *
 * @author Jesse
 */
@Entity
@Table(name = "commander")
public class CommanderEntity extends HawkDBEntity {

	@Id
	@Column(name = "playerId", unique = true, nullable = false)
    @IndexProp(id = 1)
	private String playerId;

	@Column(name = "equipInfo", nullable = false)
    @IndexProp(id = 2)
	private String equipInfo;

	@Column(name = "createTime", nullable = false)
    @IndexProp(id = 3)
	protected long createTime = 0;

	@Column(name = "updateTime")
    @IndexProp(id = 4)
	protected long updateTime = 0;

	@Column(name = "invalid")
    @IndexProp(id = 5)
	protected boolean invalid;
	
	/**
	 * 星能探索
	 */
	@Column(name = "starExplore")
	@IndexProp(id = 6)
	private String starExplore="";

	@Column(name = "starExploreCollect")
	@IndexProp(id = 7)
	private String starExploreCollect="";
	
	@Column(name = "soulResetCd")
    @IndexProp(id = 8)
	protected long soulResetCd = 0;

	@Column(name = "superSoldierSkin")
	@IndexProp(id = 9)
	private String superSoldierSkin="";

	@Column(name = "shopData")
	@IndexProp(id = 10)
	private String shopData="";

	@Column(name = "getDressTime")
	@IndexProp(id = 11)
	private long getDressTime;

	@Column(name = "getDressCount")
	@IndexProp(id = 12)
	private int getDressCount;
	
	
	@Column(name = "fgylData")
	@IndexProp(id = 13)
	private String fgylData = "";
	
	@Column(name = "mtpremarch")
	@IndexProp(id = 14)
	private int mtpremarch;

	@Transient
	private Set<Integer> superSoldierSkins = new HashSet<>();

	/** 兑换数量 */
	@Transient
	private Map<Integer, Integer> shopDataMap = new HashMap<>();

	/**
	 * 星能探索
	 */
	@Transient
	private ArmourStarExplores starExplores;
	
	@Transient
	private CommanderObject commanderObject;
	
	
	@Transient
	private FGYLPlayerEntity fgylPlayerEntity = new FGYLPlayerEntity();

	// 通知指挥官信息变更
	@Transient
	private boolean changed;

	public String getPlayerId() {
		return playerId;
	}

	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}

	public String getEquipInfo() {
		return equipInfo;
	}

	public void setEquipInfo(String equipInfo) {
		this.equipInfo = equipInfo;
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
	public void beforeWrite() {
		if (commanderObject != null) {
			this.equipInfo = commanderObject.serializEquip();
		}
		this.changed = false;
		starExplore = starExplores.serialize();
		starExploreCollect = starExplores.serializeCollect();
		this.superSoldierSkin = SerializeHelper.collectionToString(this.superSoldierSkins,SerializeHelper.ATTRIBUTE_SPLIT);
		//兑换数据转字符串
		this.shopData = SerializeHelper.mapToString(shopDataMap);
		if(this.fgylPlayerEntity != null){
			this.fgylData = this.fgylPlayerEntity.serializ();
		}
		super.beforeWrite();
	}

	@Override
	public void afterRead() {
		this.commanderObject = new CommanderObject(this);
		this.commanderObject.init();
		starExplores = ArmourStarExplores.unSerialize(this, starExplore, starExploreCollect);
		this.superSoldierSkins = SerializeHelper.stringToSet(Integer.class, this.superSoldierSkin, SerializeHelper.ATTRIBUTE_SPLIT,null,null);
		this.shopDataMap = SerializeHelper.stringToMap(this.shopData, Integer.class, Integer.class);
		this.fgylPlayerEntity = new FGYLPlayerEntity();
		this.fgylPlayerEntity.mergeFrom(this.fgylData);
		super.afterRead();
	}

	/**
	 * 注意:String数据发生变化,需手动调用,确保数据正常落地
	 * 
	 * @param changed
	 */
	public void notifyChanged(boolean changed) {
		this.changed = changed;
		this.notifyUpdate();
	}

	public void recordCommanderObj(CommanderObject commanderObject) {
		this.commanderObject = commanderObject;
		this.notifyUpdate();
	}

	public CommanderObject getCommanderObject() {
		return commanderObject;
	}
	
	@Override
	public String getPrimaryKey() {
		return this.playerId;
	}

	@Override
	public void setPrimaryKey(String primaryKey) {
		throw new UnsupportedOperationException();
	}
	
	public String getOwnerKey() {
		return playerId;
	}

	public ArmourStarExplores getStarExplores() {
		return starExplores;
	}

	public void setStarExplores(ArmourStarExplores starExplores) {
		this.starExplores = starExplores;
	}

	public long getSoulResetCd() {
		return soulResetCd;
	}

	public void setSoulResetCd(long soulResetCd) {
		this.soulResetCd = soulResetCd;
	}

	public Set<Integer> getSuperSoldierSkins() {
		return superSoldierSkins;
	}

	public Map<Integer, Integer> getShopDataMap() {
		return shopDataMap;
	}

	public void setShopDataMap(Map<Integer, Integer> shopDataMap) {
		this.shopDataMap = shopDataMap;
	}

	public int getGetDressCount() {
		long now = HawkTime.getMillisecond();
		if(!HawkTime.isSameWeek(now, getDressTime)){
			getDressTime = now;
			getDressCount = 0;
			notifyUpdate();
		}
		return getDressCount;
	}

	public void setGetDressCount(int getDressCount) {
		this.getDressCount = getDressCount;
	}
	
	public FGYLPlayerEntity getFgylPlayerEntity() {
		return fgylPlayerEntity;
	}

	public int getMtpremarch() {
		return mtpremarch;
	}

	public void setMtpremarch(int mtpremarch) {
		this.mtpremarch = mtpremarch;
	}
	
	
}
