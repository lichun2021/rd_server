package com.hawk.game.world;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentHashMap;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;


import com.hawk.game.protocol.World;
import org.apache.commons.lang.StringUtils;
import org.hawk.annotation.IndexProp;
import org.hawk.app.HawkApp;
import org.hawk.config.HawkConfigManager;
import org.hawk.db.HawkDBEntity;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;

import com.alibaba.fastjson.JSONObject;
import com.hawk.common.AccountRoleInfo;
import com.hawk.game.GsConfig;
import com.hawk.game.battle.BattleService;
import com.hawk.game.config.EquipResearchCfg;
import com.hawk.game.config.FoggyFortressCfg;
import com.hawk.game.config.PrivateSettingOptionCfg;
import com.hawk.game.config.TreasureHuntResCfg;
import com.hawk.game.config.WorldEnemyCfg;
import com.hawk.game.config.WorldMapConstProperty;
import com.hawk.game.config.WorldPylonCfg;
import com.hawk.game.config.WorldResourceCfg;
import com.hawk.game.config.WorldStrongpointCfg;
import com.hawk.game.entity.EquipResearchEntity;
import com.hawk.game.entity.item.DressItem;
import com.hawk.game.entity.item.SpyMarkItem;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.LocalRedis;
import com.hawk.game.guild.manor.AbstractBuildable;
import com.hawk.game.guild.manor.GuildManorObj;
import com.hawk.game.guild.manor.building.GuildDragonTrap;
import com.hawk.game.guild.manor.building.GuildManorSuperMine;
import com.hawk.game.module.mechacore.PlayerMechaCore;
import com.hawk.game.module.plantsoldier.strengthen.PlantSoldierSchool;
import com.hawk.game.nation.NationService;
import com.hawk.game.nation.NationalBuilding;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerData;
import com.hawk.game.protocol.Dress.DressEditData;
import com.hawk.game.protocol.Dress.DressEditType;
import com.hawk.game.protocol.Dress.DressType;
import com.hawk.game.protocol.Manhattan.PBDeployedSwInfo;
import com.hawk.game.protocol.WarFlag.FlageState;
import com.hawk.game.protocol.World.EquipTechLevel;
import com.hawk.game.protocol.World.MonsterType;
import com.hawk.game.protocol.World.PBTreaCollRec;
import com.hawk.game.protocol.World.SignatureState;
import com.hawk.game.protocol.World.SnowballGoalInfo;
import com.hawk.game.protocol.World.SnowballKickInfo;
import com.hawk.game.protocol.World.SpyMark;
import com.hawk.game.protocol.World.StrongpointStatus;
import com.hawk.game.protocol.World.SuperVipSkinEffect;
import com.hawk.game.protocol.World.WorldPointDetailPB;
import com.hawk.game.protocol.World.WorldPointPB;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.protocol.World.WorldShowDress;
import com.hawk.game.protocol.WorldPoint.PointData;
import com.hawk.game.queryentity.AccountInfo;
import com.hawk.game.service.GuildManorService;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.RelationService;
import com.hawk.game.service.WarFlagService;
import com.hawk.game.service.flag.FlagCollection;
import com.hawk.game.service.warFlag.IFlag;
import com.hawk.game.superweapon.SuperWeaponService;
import com.hawk.game.superweapon.weapon.IWeapon;
import com.hawk.game.util.BuilderUtil;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.KeyValuePair;
import com.hawk.game.util.WorldUtil;
import com.hawk.game.world.march.IWorldMarch;
import com.hawk.game.world.march.impl.CollectWorldResMarch;
import com.hawk.game.world.object.CakeShareInfo;
import com.hawk.game.world.object.DragonBoatInfo;
import com.hawk.game.world.object.FoggyInfo;
import com.hawk.game.world.object.GhostInfo;
import com.hawk.game.world.proxy.WorldPointProxy;
import com.hawk.game.world.service.WorldChristmasWarService;
import com.hawk.game.world.service.WorldGundamService;
import com.hawk.game.world.service.WorldMonsterService;
import com.hawk.game.world.service.WorldNianService;
import com.hawk.game.world.service.WorldPlayerService;
import com.hawk.game.world.service.WorldPointService;
import com.hawk.game.world.service.WorldRobotService;
import com.hawk.game.world.service.WorldSnowballService;
import com.hawk.serialize.string.SerializeHelper;

/**
 * 世界点
 * 
 * @author julia
 *
 */
@Entity
@Table(name = "world_point")
public class WorldPoint extends HawkDBEntity {
	// 组合世界坐标
	@Id
	@Column(name = "id", unique = true, nullable = false)
	@IndexProp(id = 1)
	private int id;

	// 坐标x
	@Column(name = "x", nullable = false)
	@IndexProp(id = 2)
	private int x;

	// 坐标y
	@Column(name = "y", nullable = false)
	@IndexProp(id = 3)
	private int y;

	// 所属区块id
	@Column(name = "areaId", nullable = false)
	@IndexProp(id = 4)
	private int areaId;

	// 资源带id
	@Column(name = "zoneId", nullable = false)
	@IndexProp(id = 5)
	private int zoneId;

	// 点类型，人，怪，资源, 部队
	@Column(name = "pointType", nullable = false)
	@IndexProp(id = 6)
	private int pointType;

	// 点状态, 目前使用于据点，分为 初始状态，占领状态，空白状态
	@Column(name = "pointStatus", nullable = false)
	@IndexProp(id = 7)
	private int pointStatus;

	// 玩家唯一id
	@Column(name = "playerId", nullable = true)
	@IndexProp(id = 8)
	private String playerId;

	// 玩家名字
	@Column(name = "playerName", nullable = true)
	@IndexProp(id = 9)
	private String playerName;

	// 玩家等级
	@Column(name = "cityLevel", nullable = true)
	@IndexProp(id = 10)
	private int cityLevel;

	// 玩家头像
	@Column(name = "playerIcon", nullable = true)
	@IndexProp(id = 11)
	private int playerIcon;

	// 上次活跃时间
	// 在据点中表示tick的剩余时间
	@Column(name = "lastActiveTime", nullable = true)
	@IndexProp(id = 12)
	private long lastActiveTime = HawkTime.getMillisecond();

	// 资源id
	@Column(name = "resourceId", nullable = true)
	@IndexProp(id = 13)
	private int resourceId;

	// 怪物id
	@Column(name = "monsterId", nullable = true)
	@IndexProp(id = 14)
	private int monsterId;

	// 资源或者怪物的声明开始时间
	@Column(name = "lifeStartTime", nullable = false)
	@IndexProp(id = 15)
	private long lifeStartTime;

	// 资源剩余数量
	@Column(name = "remainResNum", nullable = true)
	@IndexProp(id = 16)
	private long remainResNum;

	// 怪物剩余血量
	@Column(name = "remainBlood", nullable = true)
	@IndexProp(id = 17)
	private int remainBlood;

	// 驻扎行军部队Id
	@Column(name = "marchId", nullable = true)
	@IndexProp(id = 18)
	private String marchId;

	// 归属领地id
	@Column(name = "guildId", nullable = true)
	@IndexProp(id = 19)
	private String guildId;

	// 联盟建筑id TODO zhenyu.shang 此处需要修改，只存建筑的序列即可
	@Column(name = "guildBuildId", nullable = true)
	@IndexProp(id = 20)
	private String guildBuildId;

	// 建筑配置id
	@Column(name = "buildingId", nullable = true)
	@IndexProp(id = 21)
	private int buildingId;

	// 保护失效时间
	@Column(name = "protectedEndTime", nullable = true)
	@IndexProp(id = 22)
	private long protectedEndTime;

	// 被攻击状态失效时间
	@Column(name = "commonHurtEndTime", nullable = true)
	@IndexProp(id = 23)
	private long commonHurtEndTime;

	// 城点上的buff信息
	@Column(name = "showEffect")
	@IndexProp(id = 24)
	private String showEffect;

	// 拥有者
	@Column(name = "ownerId", nullable = true)
	@IndexProp(id = 25)
	private String ownerId;

	// 迷雾信息
	@Column(name = "foggyInfo", nullable = true)
	@IndexProp(id = 26)
	private String foggyInfo;

	// 创建时间
	@Column(name = "createTime", nullable = false)
	@IndexProp(id = 27)
	private long createTime = 0;

	// 更新时间
	@Column(name = "updateTime")
	@IndexProp(id = 28)
	private long updateTime;

	// 是否有效
	@Column(name = "invalid")
	@IndexProp(id = 29)
	private boolean invalid;

	// 行军表情
	@Column(name = "emoticon")
	@IndexProp(id = 30)
	private int emoticon;

	// 行军表情使用时间
	@Column(name = "emoticonUseTime")
	@IndexProp(id = 31)
	private long emoticonUseTime;
	
	// 个保法相关开关信息
	@Column(name = "personalProtectInfo", nullable = true)
	@IndexProp(id = 32)
	private String personalProtectInfo;

	// 装备科技等级
	@Column(name = "equipTechLevel")
	@IndexProp(id = 33)
	private String equipTechLevel = "";

	@Column(name = "plantMilitaryLevel")
	@IndexProp(id = 34)
	private int plantMilitaryLevel;
	
	@Column(name = "atkManhattanSw")
	@IndexProp(id = 35)
	private int atkManhattanSw;
	
	@Column(name = "defManhattanSw")
	@IndexProp(id = 36)
	private int defManhattanSw;
	
	@Column(name = "atkSwSkillId")
	@IndexProp(id = 37)
	private int atkSwSkillId;
	
	@Column(name = "defSwSkillId")
	@IndexProp(id = 38)
	private int defSwSkillId;

	@Column(name = "plantMilitaryShow")
	@IndexProp(id = 39)
	private int plantMilitaryShow;
	
	/** 机甲核心外显 */
	@Column(name = "mechaCoreShow")
	@IndexProp(id = 40)
	private String mechaCoreShow = "";
	
	
	// 世界场景对象id
	@Transient
	private int aoiObjId = 0;

	// 迷雾要塞对象
	@Transient
	private FoggyInfo foggyInfoObj;

	@Transient
	private List<PBTreaCollRec> resTreaColRecordList;

	@Transient
	SnowballKickInfo.Builder snowballKickInfo;
	
	@Transient
	SnowballGoalInfo.Builder snowballGoalInfo;
	

	@Transient
	DragonBoatInfo dragonBoatInfo;

	@Transient
	GhostInfo ghostInfo;

	@Transient
	CakeShareInfo cakeShareInfo;
	
	/** 
	 * 至尊vip皮肤特效结束时间
	 */
	@Transient
	private long superVipSkinEffEndTime;
	@Transient
	private int superVipSkinLevel;
	
	@Transient
	private Map<Integer, Integer> equipTechLevelMap = new ConcurrentHashMap<>();
	
	// 避免外部直接调用, 因为要根据xy计算id值
	public WorldPoint() {

	}

	public WorldPoint(int x, int y, int areaId, int zoneId, int pointType) {
		this.x = x;
		this.y = y;
		this.areaId = areaId;
		this.zoneId = zoneId;
		this.pointType = pointType;
		this.id = GameUtil.combineXAndY(x, y);
		this.lastActiveTime = HawkTime.getMillisecond();
		playerId = "";
		playerName = "";
		marchId = "";
		guildId = "";
		guildBuildId = "";
		showEffect = "";
		ownerId = "";
		foggyInfo = "";
	}

	public int getPlantMilitaryLevel() {
		return plantMilitaryLevel;
	}

	public void setPlantMilitaryLevel(int plantMilitaryLevel) {
		this.plantMilitaryLevel = plantMilitaryLevel;
	}

	public int getPlantMilitaryShow() {
		return plantMilitaryShow;
	}

	public void setPlantMilitaryShow(int plantMilitaryShow) {
		this.plantMilitaryShow = plantMilitaryShow;
	}

	public int getAtkManhattanSw() {
		return atkManhattanSw;
	}

	public void setAtkManhattanSw(int atkManhattanSw) {
		this.atkManhattanSw = atkManhattanSw;
	}

	public int getDefManhattanSw() {
		return defManhattanSw;
	}

	public void setDefManhattanSw(int defManhattanSw) {
		this.defManhattanSw = defManhattanSw;
	}
	
	public int getAtkSwSkillId() {
		return atkSwSkillId;
	}

	public void setAtkSwSkillId(int atkSwSkillId) {
		this.atkSwSkillId = atkSwSkillId;
	}

	public int getDefSwSkillId() {
		return defSwSkillId;
	}

	public void setDefSwSkillId(int defSwSkillId) {
		this.defSwSkillId = defSwSkillId;
	}
	
	public String getMechaCoreShow() {
		return mechaCoreShow;
	}

	public void setMechaCoreShow(String mechaCoreShow) {
		this.mechaCoreShow = mechaCoreShow;
	}

	public PBDeployedSwInfo.Builder getDeployedSwInfo() {
		PBDeployedSwInfo.Builder builder = PBDeployedSwInfo.newBuilder();
		builder.setAtkSwId(this.getAtkManhattanSw());
		builder.setDefSwId(this.getDefManhattanSw());
		builder.setAtkSwSkillId(this.getAtkSwSkillId());
		builder.setDefSwSkillId(this.getDefSwSkillId());
		return builder;
	}
	

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public int getAreaId() {
		return areaId;
	}

	public void setAreaId(int areaId) {
		this.areaId = areaId;
	}

	public int getZoneId() {
		return zoneId;
	}

	public void setZoneId(int zoneId) {
		this.zoneId = zoneId;
	}

	public int getPointType() {
		return pointType;
	}

	public void setPointType(int pointType) {
		this.pointType = pointType;
	}

	public int getPointStatus() {
		return pointStatus;
	}

	public void setRemainResNum(long remainResNum) {
		this.remainResNum = remainResNum;
	}

	public long getRemainResNum() {
		return remainResNum;
	}

	public void setPointStatus(int pointStatus) {
		this.pointStatus = pointStatus;
	}

	public String getPlayerId() {
		return playerId;
	}

	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}

	public String getPlayerName() {
		return playerName;
	}

	public void setPlayerName(String playerName) {
		this.playerName = playerName;
	}

	public int getCityLevel() {
		return cityLevel;
	}

	public void setCityLevel(int cityLevel) {
		this.cityLevel = cityLevel;
	}

	public int getPlayerIcon() {
		return playerIcon;
	}

	public void setPlayerIcon(int playerIcon) {
		this.playerIcon = playerIcon;
	}

	public long getLastActiveTime() {
		return lastActiveTime;
	}

	public void setLastActiveTime(long lastActiveTime) {
		this.lastActiveTime = lastActiveTime;
	}

	public int getResourceId() {
		return resourceId;
	}

	public void setResourceId(int resourceId) {
		this.resourceId = resourceId;
	}

	public int getMonsterId() {
		return monsterId;
	}

	public void setMonsterId(int monsterId) {
		this.monsterId = monsterId;
	}

	public void setRemainBlood(int remainBlood) {
		this.remainBlood = remainBlood;
	}

	public long getCommonHurtEndTime() {
		return commonHurtEndTime;
	}

	public void setCommonHurtEndTime(long commonHurtEndTime) {
		this.commonHurtEndTime = commonHurtEndTime;
	}

	public String getShowEffect() {
		return showEffect;
	}

	public void setShowEffect(String showEffect) {
		this.showEffect = showEffect;
	}

	public String getOwnerId() {
		return ownerId;
	}

	public void setOwnerId(String ownerId) {
		this.ownerId = ownerId;
	}

	public String getMarchId() {
		return marchId;
	}

	public void setMarchId(String marchId) {
		this.marchId = marchId;
	}

	public String getGuildId() {
		return guildId;
	}

	public void setGuildId(String guildId) {
		this.guildId = guildId;
	}

	public String getGuildBuildId() {
		return guildBuildId;
	}

	public void setGuildBuildId(String guildBuildId) {
		this.guildBuildId = guildBuildId;
	}

	public int getBuildingId() {
		return buildingId;
	}

	public void setBuildingId(int buildingId) {
		this.buildingId = buildingId;
	}

	public long getProtectedEndTime() {
		return protectedEndTime;
	}

	public long getShowProtectedEndTime() {
		// 全服保护
		long globalProtectEndTime = 0;
		boolean brokenProtect = GlobalData.getInstance().isBrokenProtect(playerId);
		if (!brokenProtect) {
			globalProtectEndTime = GlobalData.getInstance().getGlobalProtectEndTime();
		}
		return Math.max(globalProtectEndTime, protectedEndTime);
	}

	public void setProtectedEndTime(long protectedEndTime) {
		this.protectedEndTime = protectedEndTime;
	}

	public long getLifeStartTime() {
		return lifeStartTime;
	}

	public void setLifeStartTime(long lifeStartTime) {
		this.lifeStartTime = lifeStartTime;
	}

	public String getFoggyInfo() {
		return foggyInfo;
	}

	public void setFoggyInfo(String foggyInfo) {
		this.foggyInfo = foggyInfo;
	}

	@Override
	public long getCreateTime() {
		return createTime;
	}

	@Override
	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}

	@Override
	public long getUpdateTime() {
		return updateTime;
	}

	@Override
	public void setUpdateTime(long updateTime) {
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

	public int getEmoticon() {
		return emoticon;
	}

	public void setEmoticon(int emoticon) {
		this.emoticon = emoticon;
	}

	public long getEmoticonUseTime() {
		return emoticonUseTime;
	}

	public void setEmoticonUseTime(long emoticonUseTime) {
		this.emoticonUseTime = emoticonUseTime;
	}

	@Override
	public String getPrimaryKey() {
		return String.valueOf(id);
	}

	@Override
	public void setPrimaryKey(String primaryKey) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void notifyUpdate(boolean async, int period) {
		if (pointType == 0 || pointType == WorldPointType.MONSTER_VALUE) {
			return;
		}

		WorldPointProxy.getInstance().update(this);

		// 父类调用
		if (GsConfig.getInstance().getWorldPointProxy() == 0) {
			super.notifyUpdate(async, period);
		}
	}

	@Override
	public void beforeWrite() {
		if (this.foggyInfoObj != null) {
			this.foggyInfo = JSONObject.toJSONString(foggyInfoObj);
		}
	}

	@Override
	public void afterRead() {
		if (this.foggyInfo != null) {
			this.foggyInfoObj = JSONObject.parseObject(this.foggyInfo, FoggyInfo.class);
		}
	}

	/**
	 * 初始化迷雾要塞对象
	 * 
	 * @param foggyInfo
	 */
	public void initFoggyInfo(FoggyInfo foggyInfo) {
		this.foggyInfoObj = foggyInfo;
	}

	/**
	 * 获取迷雾要塞对象
	 * 
	 * @return
	 */
	public FoggyInfo getFoggyInfoObj() {
		return foggyInfoObj;
	}

	/**
	 * 获取aoi对象的id
	 * 
	 * @return
	 */
	public int getAoiObjId() {
		return aoiObjId;
	}

	/**
	 * 重置aoi对象的id值
	 * 
	 * @param aoiObjId
	 */
	public void resetAoiObjId(int aoiObjId) {
		this.aoiObjId = aoiObjId;
	}

	/**
	 * 战斗时获取怪物血量
	 * 
	 * @return
	 */
	public int getRemainBlood() {
		return remainBlood;
	}

	/**
	 * 怪物生成初始化时调用，一生只有一次
	 * 
	 * @param blood
	 */
	public void initMonsterBlood(int blood) {
		setRemainBlood(GameUtil.combineXAndY(blood, blood));
	}

	/**
	 * 扣除指定怪物血量
	 * 
	 * @param blood
	 */
	public void deductMonsterBlood(int blood) {
		int[] bloods = GameUtil.splitXAndY(remainBlood);
		int remain = bloods[0];
		int maxBlood = bloods[1];
		remain = Math.max(remain - blood, 0);
		setRemainBlood(GameUtil.combineXAndY(remain, maxBlood));
	}

	/**
	 * 获取资源配置对象
	 * 
	 * @return
	 */
	public WorldResourceCfg getResourceCfg() {
		if (pointType == WorldPointType.RESOURCE_VALUE) {
			return HawkConfigManager.getInstance().getConfigByKey(WorldResourceCfg.class, resourceId);
		}
		return null;
	}

	/**
	 * 获取怪物配置对象
	 * 
	 * @return
	 */
	public WorldEnemyCfg getEnemyCfg() {
		if (pointType == WorldPointType.MONSTER_VALUE ||
				pointType == WorldPointType.ROBOT_VALUE) {
			return HawkConfigManager.getInstance().getConfigByKey(WorldEnemyCfg.class, monsterId);
		}
		return null;
	}

	public FoggyFortressCfg getFoggyFortressCfg() {
		if (pointType == WorldPointType.FOGGY_FORTRESS_VALUE) {
			return HawkConfigManager.getInstance().getConfigByKey(FoggyFortressCfg.class, monsterId);
		}
		return null;
	}

	public WorldStrongpointCfg getStrongpointCfg() {
		if (pointType == WorldPointType.STRONG_POINT_VALUE) {
			return HawkConfigManager.getInstance().getConfigByKey(WorldStrongpointCfg.class, monsterId);
		}
		return null;
	}

	/**
	 * 是否生命到期死亡
	 * 
	 * @return
	 */
	public boolean isLifeEndDead(int futureTime) {
		long lifePeriod = 0;

		if (pointType == WorldPointType.RESOURCE_VALUE) {
			return false;
		}

		if (pointType == WorldPointType.MONSTER_VALUE || pointType == WorldPointType.ROBOT_VALUE) {
			return false;
		}

		if (pointType == WorldPointType.STRONG_POINT_VALUE) {
			return false;
		}

		if (pointType == WorldPointType.FOGGY_FORTRESS_VALUE) {
			FoggyFortressCfg cfg = getFoggyFortressCfg();
			if (cfg != null) {
				lifePeriod = cfg.getLifeTime() * 1000;
			}
		}

		if (lifePeriod > 0 && HawkApp.getInstance().getCurrentTime() >= getLifeStartTime() + lifePeriod - futureTime) {
			return true;
		}

		return false;
	}

	/**
	 * @return true表示该点可被攻击
	 */
	public boolean canBeAttack() {
		return pointType == WorldPointType.PLAYER_VALUE ||
				pointType == WorldPointType.KING_PALACE_VALUE;
	}

	/**
	 * @return true表示该点可被攻击
	 */
	public boolean needJoinGuild() {
		return pointType == WorldPointType.KING_PALACE_VALUE;
	}

	/**
	 * 初始化玩家信息
	 * 
	 * @param player
	 * @return
	 */
	public boolean initPlayerInfo(PlayerData playerData) {
		setPlayerId(playerData.getPlayerEntity().getId());
		setPlayerIcon(playerData.getPlayerEntity().getIcon());
		setCityLevel(playerData.getConstructionFactoryLevel());
		setPlayerName(playerData.getPlayerEntity().getName());
		initEquipTechInfo(playerData);
		initManhattanInfo(playerData);
		initMechacoreShow(playerData);
		try {
			PlantSoldierSchool plantSchoolObj = playerData.getPlantSoldierSchoolEntity().getPlantSchoolObj();
			setPlantMilitaryLevel(plantSchoolObj.getMaxSoldierPlantMilitaryLevel());
			setPlantMilitaryShow(plantSchoolObj.getOutShowSwitchState());
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		return true;
	}
	
	/**
	 * 装备科技信息
	 * @param playerData
	 */
	private void initEquipTechInfo(PlayerData playerData) {
		if (!GameUtil.checkPhaseOneArmourTechMaxLevel(playerData)) {
			return;
		}
		
		List<EquipResearchEntity> equipResearchEntityList = playerData.getEquipResearchEntityList();
		for (EquipResearchEntity researchEntity : equipResearchEntityList) {
			EquipResearchCfg researchCfg = HawkConfigManager.getInstance().getConfigByKey(EquipResearchCfg.class, researchEntity.getResearchId());
			if (researchCfg != null && researchCfg.getPhaseTwo() > 0) {
				updateEquipTechLevel(researchEntity.getResearchId(), researchEntity.getResearchLevel());
			}
		}
	}
	
	/**
	 * 超武部署信息
	 * @param playerData
	 */
	private void initManhattanInfo(PlayerData playerData) {
		PBDeployedSwInfo.Builder builder = playerData.getDeployedSwInfo();
		this.setAtkManhattanSw(builder.getAtkSwId());
		this.setAtkSwSkillId(builder.getAtkSwSkillId());
		this.setDefManhattanSw(builder.getDefSwId());
		this.setDefSwSkillId(builder.getDefSwSkillId());
	}
	
	/**
	 * 机甲核心外显信息
	 * @param playerData
	 */
	private void initMechacoreShow(PlayerData playerData) {
		PlayerMechaCore mechacore = playerData.getMechaCoreEntity().getMechaCoreObj();
		this.setMechaCoreShow(mechacore.serializeUnlockedCityShow());
	}

	/**
	 * 玩家预占用的点不可见
	 * 
	 * @return
	 */
	public boolean isVisible() {
		if (pointType == WorldPointType.PLAYER_VALUE && HawkOSOperator.isEmptyString(playerId)) {
			return false;
		}

		return true;
	}

	/**
	 * 转换为协议所需pb格式
	 * 
	 * @param viewerId
	 *            观察者ID
	 * @return
	 */
	public WorldPointPB.Builder toBuilder(WorldPointPB.Builder builder, String viewerId) {
		builder.setPointX(x);
		builder.setPointY(y);
		builder.setPointType(WorldPointType.valueOf(pointType));

		String thisPlayerId = playerId;
		if (!HawkOSOperator.isEmptyString(thisPlayerId)) {
			builder.setPlayerId(thisPlayerId);
			builder.setPlayerName(HawkOSOperator.isEmptyString(playerName) ? "" : playerName);
			builder.setCityLevel(cityLevel);
			if (!HawkOSOperator.isEmptyString(personalProtectInfo)) {
				String[] infos = personalProtectInfo.split(",");
				for (String info : infos) {
					builder.addPersonalProtectSwitch(Integer.parseInt(info));
				}
			} else {
				int switchCount = HawkConfigManager.getInstance().getConfigSize(PrivateSettingOptionCfg.class);
				int switchIntCount = switchCount / 31;
				if (switchCount % 31 > 0) {
					switchIntCount += 1;
				}
				for (int i = 1; i <= switchIntCount; i++) {
					builder.addPersonalProtectSwitch(0);
				}
			}

			String guildId = GuildService.getInstance().getPlayerGuildId(thisPlayerId);
			if (GuildService.getInstance().isGuildExist(guildId)) {
				builder.setGuildId(guildId);
				builder.setGuildTag(GuildService.getInstance().getGuildTag(guildId));
				builder.setGuildFlag(GuildService.getInstance().getGuildFlag(guildId));
				if (buildingId > 0 && guildBuildId != null) {
					builder.setManorState(GuildManorService.getInstance().getBuildStat(buildingId, guildBuildId));
				}
			}

			AccountInfo account = GlobalData.getInstance().getAccountInfoByPlayerId(thisPlayerId);
			if (account != null) {
				builder.setServerId(GlobalData.getInstance().getMainServerId(account.getServerId()));
			} else if (WorldRobotService.getInstance().isRobotId(playerId)) {
				builder.setServerId(GsConfig.getInstance().getServerId());
			}

			builder.setOfficerId(GameUtil.getOfficerId(thisPlayerId));
			RelationService relationService = RelationService.getInstance();
			String guardPlayerId = relationService.getGuardPlayer(playerId);
			if (!HawkOSOperator.isEmptyString(guardPlayerId)) {
				builder.setGuardPlayerId(guardPlayerId);
				builder.setDressId(relationService.getDressId(playerId));

				WorldPoint guardWorldPoint = WorldPlayerService.getInstance().getPlayerWorldPoint(guardPlayerId);
				if (guardWorldPoint != null) {
					builder.setGuardPlyaerServerId(GsConfig.getInstance().getServerId());
					builder.setGuardPlayerX(guardWorldPoint.getX());
					builder.setGuardPlayerY(guardWorldPoint.getY());
				}
			}
		}

		if (!HawkOSOperator.isEmptyString(marchId)) {
			builder.setMarchId(marchId);
		}

		if (monsterId > 0) {
			builder.setMonsterId(monsterId);
			int maxBlood = 0;

			if (pointType == WorldPointType.MONSTER_VALUE) {
				WorldEnemyCfg monsterCfg = HawkConfigManager.getInstance().getConfigByKey(WorldEnemyCfg.class, monsterId);
				if (monsterCfg != null && (monsterCfg.getType() == MonsterType.TYPE_7_VALUE || monsterCfg.getType() == MonsterType.TYPE_8_VALUE)) {
					maxBlood = WorldMonsterService.getInstance().getMaxEnemyBlood(monsterId);
				}
			} else if (pointType == WorldPointType.GUNDAM_VALUE) {
				maxBlood = WorldGundamService.getInstance().getGundamInitBlood(monsterId);
			} else if (pointType == WorldPointType.NIAN_VALUE) {
				maxBlood = WorldNianService.getInstance().getNianInitBlood(monsterId);
			} else if (pointType == WorldPointType.CHRISTMAS_BOSS_VALUE) {
				maxBlood = WorldChristmasWarService.getInstance().getInitBlood();
			}

			builder.setMonsterMaxBlood(maxBlood);
			builder.setRemainBlood(remainBlood);
		}

		if (resourceId > 0) {
			builder.setResourceId(resourceId);
		}

		// 联盟建筑
		if (pointType == WorldPointType.GUILD_TERRITORY_VALUE && GuildService.getInstance().isGuildExist(guildId)) {
			builder.setGuildId(guildId);
			builder.setTerriId(buildingId);
			builder.setGuildFlag(GuildService.getInstance().getGuildFlag(guildId));

			builder.setGuildTag(GuildService.getInstance().getGuildTag(guildId));
			if (buildingId > 0 && guildBuildId != null) {
				AbstractBuildable buildable = GuildManorService.getInstance().getBuildable(this);
				if (buildable != null) {
					int stat = buildable.getbuildStat();
					if (stat > 0) {
						builder.setManorState(stat);
						String[] keys = guildBuildId.split("_");
						switch (buildable.getBuildType()) {
						case GUILD_BASTION:
							GuildManorObj obj = (GuildManorObj) buildable;
							builder.setManorBuildIndex(Integer.parseInt(keys[1]));
							builder.setManorBuildName(obj.getEntity().getManorName());
							builder.setManorBuildTime(obj.getEntity().getPlaceTime());
							builder.setManorComTime(obj.isComplete() ? obj.getEntity().getCompleteTime() : 0);
							break;
						case GUILD_MINE:
							GuildManorSuperMine mine = (GuildManorSuperMine) buildable;
							builder.setResourceId(mine.getResType());
							builder.setManorBuildIndex(Integer.parseInt(keys[2]));
							break;
						case GUILD_BARTIZAN:
						case GUILD_STOREHOUSE:
							builder.setManorBuildIndex(Integer.parseInt(keys[2]));
							break;
						case GUILD_DRAGON_TRAP:
							GuildDragonTrap trap = (GuildDragonTrap) buildable;
							builder.setGuildDragonTrapState(trap.inFight()?1:0);
							builder.setGuildDragonTrapOpenLimit(trap.getOpenTimeLimit());
							builder.setManorBuildIndex(Integer.parseInt(keys[2]));
							builder.setGuildDragonTrapFightEndTime(trap.getFightEndTime());
						default:
							break;
						}
					}
				}
			}

			if (!HawkOSOperator.isEmptyString(viewerId)) {
				if (WorldMarchService.getInstance().hasManorMarch(viewerId, id)) {
					builder.setHasMarchStop(true);
				}
			}
		} else if (WorldUtil.isPlayerPoint(this)) {
			if (!HawkOSOperator.isEmptyString(viewerId)) {
				if (WorldMarchService.getInstance().isHasAssistanceMarch(viewerId, id)) {
					builder.setHasMarchStop(true);
				}
			}
			if (plantMilitaryLevel > 0){
				builder.setPlantMilitaryLevel(plantMilitaryLevel);
				builder.setPlantMilitaryShow(plantMilitaryShow);
			}
			builder.setDeployedSwInfo(this.getDeployedSwInfo());
			builder.setMechacoreShowInfo(this.getMechaCoreShow());
			
			if(GlobalData.getInstance().isOnline(thisPlayerId)){
				builder.setCityPlantLevel(GlobalData.getInstance().makesurePlayer(thisPlayerId).getCityPlantLv());
			}else {
				AccountRoleInfo acRoleInfo = GlobalData.getInstance().getAccountRoleInfo(playerId);
				if(Objects.nonNull(acRoleInfo)){
					builder.setCityPlantLevel(acRoleInfo.getCityPlantLevel());
				}
			}
			int cityBaseShow = WorldPointService.getInstance().getCityBaseShow(playerId);
			if (cityBaseShow != 0) {
				builder.setShowEffect(cityBaseShow);
			}

			long timeNow = HawkTime.getMillisecond();
			// 装扮
			for (Entry<Integer, DressItem> entry : WorldPointService.getInstance().getShowDress(playerId).entrySet()) {
				WorldShowDress.Builder dressInfo = WorldShowDress.newBuilder();
				DressItem dressItem = entry.getValue();
				dressInfo.setDressType(DressType.valueOf(entry.getKey()));
				dressInfo.setModelType(dressItem.getModelType());
				dressInfo.setRemainShowTime(GameUtil.getDressRemainTime(dressItem) / 1000);
				if (dressItem.getShowType() != 0 && dressItem.getShowEndTime() > timeNow) {
					dressInfo.setShowType(dressItem.getShowType());
					dressInfo.setShowRemainTime((dressItem.getShowEndTime() - timeNow) / 1000);
				}
				builder.addDressShow(dressInfo);
			}
			builder.setDressTitleType(WorldPointService.getInstance().getDressTitleType(playerId));
			
			builder.setShowEquipTech(WorldPointService.getInstance().getShowEquipTech(playerId));
			builder.setEquipStarShow(WorldPointService.getInstance().getEquipStarShow(playerId));
			builder.setStarExploreShow(WorldPointService.getInstance().getStarExploreShow(playerId));
			//烟花
			builder.setFireworks(WorldPointService.getInstance().getFireWorkType(playerId));
			// 签名
			KeyValuePair<SignatureState, String> signatureInfo = WorldPointService.getInstance().getPlayerSignatureInfo(playerId);
			builder.setSignatureState(signatureInfo.getKey());
			if (signatureInfo.getKey().equals(SignatureState.CAN_SIGNATURE)) {
				builder.setSignature(signatureInfo.getValue());
			}
			
			builder.setHasOffensiveMarch(WorldMarchService.getInstance().hasOffensiveMarch(playerId));
			builder.setOffensiveMarchTime(WorldMarchService.getInstance().getOffsiveStateTime(playerId));
			
			DressEditData.Builder collegDressEdit = DressEditData.newBuilder();
			collegDressEdit.setType(DressEditType.COLLEGE_SHOW);
			String collegeName = WorldPointService.getInstance().getCollegeNameShow(playerId);
			if(!HawkOSOperator.isEmptyString(collegeName)){
				collegDressEdit.setData(collegeName);
			}
			builder.addDressEditDatas(collegDressEdit);
			
			
		} else if (WorldUtil.isPresidentPoint(this)) {
			boolean hasMarchStop = false;
			for (String marchId : WorldMarchService.getInstance().getPresidentMarchs()) {
				WorldMarch march = WorldMarchService.getInstance().getWorldMarch(marchId);
				if (march.getPlayerId().equals(viewerId)) {
					hasMarchStop = true;
				}
			}
			builder.setHasMarchStop(hasMarchStop);
		} else if (WorldUtil.isPresidentTowerPoint(this)) {
			boolean hasMarchStop = false;
			for (String marchId : WorldMarchService.getInstance().getPresidentTowerMarchs(id)) {
				WorldMarch march = WorldMarchService.getInstance().getWorldMarch(marchId);
				if (march.getPlayerId().equals(viewerId)) {
					hasMarchStop = true;
				}
			}
			builder.setHasMarchStop(hasMarchStop);
		} else if (WorldUtil.isSuperWeaponPoint(this)) {
			boolean hasMarchStop = false;
			for (String marchId : WorldMarchService.getInstance().getSuperWeaponMarchs(id)) {
				WorldMarch march = WorldMarchService.getInstance().getWorldMarch(marchId);
				if (march.getPlayerId().equals(viewerId)) {
					hasMarchStop = true;
				}
			}
			builder.setHasMarchStop(hasMarchStop);

			IWeapon weapon = SuperWeaponService.getInstance().getWeapon(id);
			builder.setSuperWeaponType(weapon.getWeaponCfg().getId());
			Player viewer = GlobalData.getInstance().makesurePlayer(viewerId);
			if (viewer == null) {
				builder.setSwHasGuildSignUp(false);
				builder.setSwHasSignUp(false);
			} else {
				builder.setSwHasGuildSignUp(viewer.hasGuild() && weapon.checkSignUp(viewer.getGuildId()));
				builder.setSwHasSignUp(weapon.hasSignUpGuild());
			}
		} else if (WorldUtil.isFortressPoint(this)) {
			boolean hasMarchStop = false;
			for (String marchId : WorldMarchService.getInstance().getFortressMarchs(id)) {
				WorldMarch march = WorldMarchService.getInstance().getWorldMarch(marchId);
				if (march != null && march.getPlayerId().equals(viewerId)) {
					hasMarchStop = true;
				}
			}
			builder.setHasMarchStop(hasMarchStop);

		}

		// 城点保护时间
		long showProtectedEndTime = getShowProtectedEndTime();
		int pointProtectNotify = WorldMapConstProperty.getInstance().getPointProtectNotify();
		if (WorldUtil.isPlayerPoint(this) && showProtectedEndTime - HawkTime.getMillisecond() > GsConst.MINUTE_MILLI_SECONDS * pointProtectNotify) {
			builder.setProtectedEndTime(Long.MAX_VALUE);
		} else {
			builder.setProtectedEndTime(showProtectedEndTime);
		}

		// 普通伤害结束时间点
		if (commonHurtEndTime > 0) {
			builder.setCommonEndTime(commonHurtEndTime);
		}

		if (pointType == WorldPointType.YURI_FACTORY_VALUE) {
			builder.setCommonEndTime(lifeStartTime);
		}

		// 专属属性
		if (!HawkOSOperator.isEmptyString(ownerId)) {
			builder.setOwnerId(ownerId);
			AccountInfo accountInfo = GlobalData.getInstance().getAccountInfoByPlayerId(ownerId);
			if (accountInfo != null) {
				builder.setOwnerName(accountInfo.getPlayerName());
			}
		}

		if (pointType == WorldPointType.STRONG_POINT_VALUE) {
			builder.setStrongpointId(monsterId);
			builder.setStatus(StrongpointStatus.valueOf(pointStatus));
			builder.setLastActiveTime(lastActiveTime);
			if (!HawkOSOperator.isEmptyString(marchId)) {
				WorldMarch march = WorldMarchService.getInstance().getWorldMarch(marchId);
				// 并发情况下可能导致march为null
				if (march != null) {
					builder.setLastActiveTime(lastActiveTime - (HawkTime.getMillisecond() - march.getResStartTime()));
				}
			}
			if (!HawkOSOperator.isEmptyString(viewerId) && viewerId.equals(playerId)) {
				builder.setHasMarchStop(true);
			}
		}

		if (pointType == WorldPointType.TH_RESOURCE_VALUE) {
			builder.setStrongpointId(resourceId);
			if (!HawkOSOperator.isEmptyString(marchId)) {
				WorldMarch march = WorldMarchService.getInstance().getWorldMarch(marchId);
				// 并发情况下可能导致march为null
				if (march != null) {
					builder.setLastActiveTime(march.getResEndTime() - HawkTime.getMillisecond());
				}
			} else {
				TreasureHuntResCfg cfg = HawkConfigManager.getInstance().getConfigByKey(TreasureHuntResCfg.class, resourceId);
				if (cfg != null) {
					builder.setLastActiveTime(cfg.getTickTime());
				}
			}
			if (!HawkOSOperator.isEmptyString(viewerId) && viewerId.equals(playerId)) {
				builder.setHasMarchStop(true);
			}
		}

		if (pointType == WorldPointType.PYLON_VALUE) {
			builder.setStrongpointId(resourceId);
			if (!HawkOSOperator.isEmptyString(marchId)) {
				WorldMarch march = WorldMarchService.getInstance().getWorldMarch(marchId);
				// 并发情况下可能导致march为null
				if (march != null) {
					builder.setLastActiveTime(march.getResEndTime() - HawkTime.getMillisecond());
				}
			} else {
				WorldPylonCfg cfg = HawkConfigManager.getInstance().getConfigByKey(WorldPylonCfg.class, resourceId);
				if (cfg != null) {
					builder.setLastActiveTime(cfg.getTickTime());
				}
			}
			if (!HawkOSOperator.isEmptyString(viewerId) && viewerId.equals(playerId)) {
				builder.setHasMarchStop(true);
			}
		}
		
		// 国家建筑
		if(pointType == WorldPointType.NATIONAL_BUILDING_POINT_VALUE){
			NationalBuilding building = NationService.getInstance().getNationalBuildingByPoint(this.id);
			if(building != null) {
				builder.setNationalBuildType(building.getBuildType().getNumber());
				builder.setNationalBuildState(building.getBuildState().getNumber());
				builder.setNationalBuildLvl(building.getEntity().getLevel());
				builder.setNationalBuildEndTime(building.getCurrentBuildEndTime());
				
				
				builder.setNationalBuildVal(building.getNationBuildingVal());
				
				builder.setNationalRunningEndTime(building.getRunningEndTime());
				builder.setNationalRunningTotalTime(building.getRunningTotalTime());
				builder.setNationalRunningParams(building.runningStateParam());
			}
		}

		// 是否在反击态
		if (pointType == WorldPointType.QUARTERED_VALUE
				|| pointType == WorldPointType.RESOURCE_VALUE
				|| pointType == WorldPointType.PLAYER_VALUE) {
			if (Objects.nonNull(playerId)) {
				boolean attacker = GuildService.getInstance().isCounterattacker(viewerId, playerId);
				builder.setCounterAttack(attacker);
			}
		}
		if (!HawkOSOperator.isEmptyString(viewerId)) {
			Player viewer = GlobalData.getInstance().makesurePlayer(viewerId);
			SpyMarkItem spyMark = WorldMarchService.getInstance().getSpyMark(viewer, id, marchId, pointType);
			if (spyMark != null) {
				SpyMark.Builder spyMarkBuilder = SpyMark.newBuilder();
				spyMarkBuilder.setSpyTime(spyMark.getStartTime());
				spyMarkBuilder.setMailId(spyMark.getMailId());
				builder.setSpyMark(spyMarkBuilder);
			}
		}

		if (pointType == WorldPointType.WAR_FLAG_POINT_VALUE) {
			IFlag flag = FlagCollection.getInstance().getFlag(guildBuildId);
			builder.setGuildFlag(GuildService.getInstance().getGuildFlag(guildId));
			builder.setOwnerId(flag.getOwnerId());
			builder.setGuildId(flag.getCurrentId());
			
			if (flag.getRemoveTime() > 0) {
				builder.setManorState(FlageState.FLAG_REMOVE_VALUE);
			} else {
				builder.setManorState(flag.getState());
			}
			builder.setFlagView(WarFlagService.getInstance().getFlagViewStatus(viewerId, flag.getFlagId()));
			String guildTag = GuildService.getInstance().getGuildTag(flag.getCurrentId());
			builder.setGuildTag(guildTag == null ? "" : guildTag);
			builder.setManorComTime(flag.getCompleteTime());
			builder.setManorBuildTime(flag.getPlaceTime());
			boolean hasMarchStop = false;
			BlockingDeque<String> flagMarchs = WorldMarchService.getInstance().getFlagMarchs(flag.getFlagId());
			for (String flagMarchId : flagMarchs) {
				IWorldMarch march = WorldMarchService.getInstance().getMarch(flagMarchId);
				if (march == null) {
					continue;
				}
				if (!march.getPlayerId().equals(viewerId)) {
					continue;
				}
				hasMarchStop = true;
			}
			builder.setHasMarchStop(hasMarchStop);
			builder.setIsCenter(flag.isCenter());
			if (flag.isCenter()) {
				builder.setIsCenterActive(flag.isCenterActive());
			}
			builder.setFlagOccupyLife(flag.getCurrOccupyLife());
		}

		if (pointType == WorldPointType.RESOURC_TRESURE_VALUE) {
			builder.setResourceId(resourceId);
			builder.setLastActiveTime(protectedEndTime);
			builder.setManorBuildTime(lifeStartTime);
			builder.setPlayerName(HawkOSOperator.isEmptyString(playerName) ? "" : playerName);
			String guildTag = GuildService.getInstance().getGuildTag(guildId);
			if (StringUtils.isNotEmpty(guildTag)) {
				builder.setGuildTag(guildTag);
			}
			// 取采集记录
			if (Objects.nonNull(resTreaColRecordList)) {
				builder.addAllTreaCollrec(resTreaColRecordList);
			}
		}
		
		if (pointType == WorldPointType.CHRISTMAS_BOX_VALUE) {
			builder.setResourceId(monsterId);
			builder.setPlayerName(HawkOSOperator.isEmptyString(playerName) ? "" : playerName);
			if (!HawkOSOperator.isEmptyString(marchId)) {
				WorldMarch march = WorldMarchService.getInstance().getWorldMarch(marchId);
				// 并发情况下可能导致march为null
				if (march != null) {
					builder.setLastActiveTime(march.getResEndTime());
				}
			}
			
			if (!HawkOSOperator.isEmptyString(playerId) && playerId.equals(viewerId)) {
				builder.setHasMarchStop(true);
			}
		}
		
		if (pointType == WorldPointType.SPACE_MECHA_BOX_VALUE || pointType == WorldPointType.SPACE_MECHA_MONSTER_VALUE) {
			builder.setGuildId(guildId);
		}

		// 只有玩家城点才能（在没有行军时）使用行军表情
		if (pointType == WorldPointType.PLAYER_VALUE) {
			BuilderUtil.buildPlayerEmotion(builder, emoticonUseTime, emoticon);
		} else if (!HawkOSOperator.isEmptyString(marchId)) {
			WorldMarch march = WorldMarchService.getInstance().getWorldMarch(marchId);
			BuilderUtil.buildMarchEmotion(builder, march);
		} else {
			WorldMarch march = WorldMarchService.getInstance().getMarchByPoint(this);
			if (march != null) {
				BuilderUtil.buildMarchEmotion(builder, march);
			}
		}

		if (pointType == WorldPointType.SNOWBALL_VALUE) {
			if (snowballKickInfo != null) {
				builder.setSnowballInfo(snowballKickInfo);
			}
			builder.setSnowballGuildAtk(WorldSnowballService.getInstance().hasGuildKicked(viewerId, monsterId));
			String lastKickBall = WorldSnowballService.getInstance().getLastKickBall(monsterId);
			if (!HawkOSOperator.isEmptyString(lastKickBall)) {
				builder.setSnowballLastAtk(lastKickBall);
			}
		}

		int[] buildingGoalShowInfo = WorldSnowballService.getInstance().getBuildingGoalShowInfo(this);
		if (buildingGoalShowInfo != null) {
			builder.setSnowballGoalCount(buildingGoalShowInfo[0]);
			builder.setSnowballTargetCount(buildingGoalShowInfo[1]);
			builder.setSnowballOwnGoalCount(WorldSnowballService.getInstance().getOwnGoalCount(this, viewerId));
		}
		
		builder.setSuperVipSkin(superVipSkinBuilder());
		builder.addAllEquipTechLevel(equipTechLevelToBuilder());

		World.WorldFormation.Builder formationInfo = getFormationInfo(viewerId);
		if (formationInfo != null) {
			builder.setFormation(formationInfo);
		}
		return builder;
	}
	
	/**
	 * 至尊vip集结特效皮肤
	 * @return
	 */
	private SuperVipSkinEffect.Builder superVipSkinBuilder() {
		SuperVipSkinEffect.Builder superVipBuilder = SuperVipSkinEffect.newBuilder();
		superVipBuilder.setSkinLevel(this.getSuperVipSkinLevel());
		superVipBuilder.setSkinEndTime(this.getSuperVipSkinEffEndTime());
		return superVipBuilder;
	}
	
	/**
	 * 装备科技等级
	 * 
	 * @return
	 */
	private List<EquipTechLevel> equipTechLevelToBuilder() {
		List<EquipTechLevel> builderList = new ArrayList<>();
		for (Entry<Integer, Integer> entry : equipTechLevelMap.entrySet()) {
			EquipTechLevel.Builder builder = EquipTechLevel.newBuilder();
			builder.setTechId(entry.getKey());
			builder.setLevel(entry.getValue());
			builderList.add(builder.build());
		}
		
		return builderList;
	}
	
	/**
	 * 转换为协议所需pb格式
	 * 
	 * @param viewerId
	 *            观察者ID
	 * @return
	 */
	public WorldPointDetailPB.Builder toDetailBuilder(String viewerId) {
		WorldPointDetailPB.Builder builder = WorldPointDetailPB.newBuilder();
		builder.setPointX(x);
		builder.setPointY(y);
		builder.setPointType(WorldPointType.valueOf(pointType));

		Player player = null;
		if (!HawkOSOperator.isEmptyString(playerId)) {
			player = GlobalData.getInstance().makesurePlayer(playerId);
		}

		if (!HawkOSOperator.isEmptyString(playerId)) {
			builder.setDuelPower(BattleService.getInstance().getDuelPower());
			builder.setPlayerId(playerId);
			builder.setPlayerName(HawkOSOperator.isEmptyString(playerName) ? "" : playerName);
			builder.setCityLevel(cityLevel);
			builder.setPlayerIcon(playerIcon);
			Player snapshot = GlobalData.getInstance().makesurePlayer(playerId);
			if (snapshot != null && !HawkOSOperator.isEmptyString(snapshot.getPfIcon())) {
				builder.setPlayerPfIcon(snapshot.getPfIcon());
			}
			String guildId = GuildService.getInstance().getPlayerGuildId(playerId);
			if (GuildService.getInstance().isGuildExist(guildId)) {
				builder.setGuildId(guildId);
				builder.setGuildTag(GuildService.getInstance().getGuildTag(guildId));
				builder.setGuildFlag(GuildService.getInstance().getGuildFlag(guildId));
				if (buildingId > 0 && guildBuildId != null) {
					builder.setManorState(GuildManorService.getInstance().getBuildStat(buildingId, guildBuildId));
				}
			}

			if (player != null) {
				String mainServerId = GlobalData.getInstance().getMainServerId(player.getServerId());
				builder.setServerId(mainServerId);
			}

			builder.setOfficerId(GameUtil.getOfficerId(playerId));
			RelationService relationService = RelationService.getInstance();
			String guardPlayerId = relationService.getGuardPlayer(playerId);
			if (!HawkOSOperator.isEmptyString(guardPlayerId)) {
				builder.setGuardPlayerId(guardPlayerId);
				builder.setDressId(relationService.getDressId(playerId));

				WorldPoint guardWorldPoint = WorldPlayerService.getInstance().getPlayerWorldPoint(guardPlayerId);
				if (guardWorldPoint != null) {
					builder.setGuardPlyaerServerId(GsConfig.getInstance().getServerId());
					builder.setGuardPlayerX(guardWorldPoint.getX());
					builder.setGuardPlayerY(guardWorldPoint.getY());
				}
			}
		}

		if (!HawkOSOperator.isEmptyString(marchId)) {
			builder.setMarchId(marchId);
		}

		if (monsterId > 0) {
			builder.setMonsterId(monsterId);
			// 怪物最大血量
			int maxBlood = 0;

			if (pointType == WorldPointType.MONSTER_VALUE) {
				maxBlood = WorldMonsterService.getInstance().getMaxEnemyBlood(monsterId);
			} else if (pointType == WorldPointType.TH_MONSTER_VALUE) {
				maxBlood = WorldMonsterService.getInstance().getMaxEnemyBlood(monsterId);
			} else if (pointType == WorldPointType.GUNDAM_VALUE) {
				maxBlood = WorldGundamService.getInstance().getGundamInitBlood(monsterId);
			} else if (pointType == WorldPointType.NIAN_VALUE) {
				maxBlood = WorldNianService.getInstance().getNianInitBlood(monsterId);
			} else if (pointType == WorldPointType.CHRISTMAS_BOSS_VALUE) {
				maxBlood = WorldChristmasWarService.getInstance().getInitBlood();
			}

			builder.setMonsterMaxBlood(maxBlood);

			builder.setRemainBlood(remainBlood);

			WorldEnemyCfg monsterCfg = HawkConfigManager.getInstance().getConfigByKey(WorldEnemyCfg.class, monsterId);
			if (monsterCfg != null && (monsterCfg.getType() == MonsterType.TYPE_7_VALUE || monsterCfg.getType() == MonsterType.TYPE_8_VALUE)) {
				int pointId = GameUtil.combineXAndY(x, y);
				// 伤害加成次数
				int addBuffTimes = LocalRedis.getInstance().getAtkNewMonsterTimes(viewerId, pointId);
				builder.setAddBuffTimes(addBuffTimes);
				// 伤害加成剩余时间
				long addBuffStartTime = LocalRedis.getInstance().getLastAtkNewMonsterTime(viewerId, pointId);
				long contiTime = WorldMapConstProperty.getInstance().getWorldNewMonsterAtkBuffContiTime() * 1000L;
				builder.setAddBuffLeftTime((contiTime + addBuffStartTime) - HawkTime.getMillisecond());

			}
		}

		if (resourceId > 0) {
			builder.setResourceId(resourceId);
			builder.setTotalRemainResNum(remainResNum);

			if (!HawkOSOperator.isEmptyString(playerId)) {
				IWorldMarch march = WorldMarchService.getInstance().getPlayerMarch(playerId, marchId);
				if (march instanceof CollectWorldResMarch) {
					CollectWorldResMarch collectWorldResMarch = (CollectWorldResMarch) march;
					// 资源类型
					WorldResourceCfg cfg = HawkConfigManager.getInstance().getConfigByKey(WorldResourceCfg.class, resourceId);
					int resType = cfg.getResType();

					// 采集速度
					double speed = collectWorldResMarch.getMarchEntity().getCollectSpeed();
					builder.setCollectSpeed(speed);
					long alreadyCollect = collectWorldResMarch.getCollectResNum(player, HawkTime.getMillisecond(), resType, speed);
					builder.setCurrentRemainResNum(remainResNum - alreadyCollect);
				}
			}

			if (!HawkOSOperator.isEmptyString(showEffect)) {
				builder.setShowEffect(Integer.parseInt(showEffect.split("_")[1]));
			}
		}

		// 联盟建筑
		if (pointType == WorldPointType.GUILD_TERRITORY_VALUE && GuildService.getInstance().isGuildExist(guildId)) {
			builder.setGuildId(guildId);
			builder.setTerriId(buildingId);
			builder.setGuildFlag(GuildService.getInstance().getGuildFlag(guildId));

			builder.setGuildTag(GuildService.getInstance().getGuildTag(guildId));
			if (buildingId > 0 && guildBuildId != null) {
				AbstractBuildable buildable = GuildManorService.getInstance().getBuildable(this);
				if (buildable != null) {
					int stat = buildable.getbuildStat();
					if (stat > 0) {
						builder.setManorState(stat);
						builder.setManorBuildLife((int) buildable.getbuildLife());
						String[] keys = guildBuildId.split("_");
						switch (buildable.getBuildType()) {
						case GUILD_BASTION:
							GuildManorObj obj = (GuildManorObj) buildable;
							builder.setManorBuildIndex(Integer.parseInt(keys[1]));
							builder.setManorBuildName(obj.getEntity().getManorName());
							builder.setManorBuildTime(obj.getEntity().getPlaceTime());
							builder.setManorComTime(obj.isComplete() ? obj.getEntity().getCompleteTime() : 0);
							break;
						case GUILD_MINE:
							GuildManorSuperMine mine = (GuildManorSuperMine) buildable;
							builder.setResourceId(mine.getResType());
							builder.setTotalRemainResNum(mine.getResourceNum());
						case GUILD_BARTIZAN:
						case GUILD_STOREHOUSE:
							builder.setManorBuildIndex(Integer.parseInt(keys[2]));
							break;
						case GUILD_DRAGON_TRAP:
							GuildDragonTrap trap = (GuildDragonTrap) buildable;
							builder.setGuildDragonTrapState(trap.inFight()?1:0);
							builder.setGuildDragonTrapOpenLimit(trap.getOpenTimeLimit());
							builder.setManorBuildIndex(Integer.parseInt(keys[2]));
							builder.setGuildDragonTrapFightEndTime(trap.getFightEndTime());
						default:
							break;
						}
					}
				}
			}

			if (!HawkOSOperator.isEmptyString(viewerId)) {
				if (WorldMarchService.getInstance().hasManorMarch(viewerId, id)) {
					builder.setHasMarchStop(true);
				}
			}
		} else if (WorldUtil.isPlayerPoint(this)) {
			if (!HawkOSOperator.isEmptyString(viewerId)) {
				if (WorldMarchService.getInstance().isHasAssistanceMarch(viewerId, id)) {
					builder.setHasMarchStop(true);
				}
			}
			if (plantMilitaryLevel > 0){
				builder.setPlantMilitaryLevel(plantMilitaryLevel);
				builder.setPlantMilitaryShow(plantMilitaryShow);
			}

			builder.setDeployedSwInfo(this.getDeployedSwInfo());
			builder.setMechacoreShowInfo(this.getMechaCoreShow());
			if(Objects.nonNull(player)){
				builder.setCityPlantLevel(player.getCityPlantLv());
			}

			int cityBaseShow = WorldPointService.getInstance().getCityBaseShow(playerId);
			if (cityBaseShow != 0) {
				builder.setShowEffect(cityBaseShow);
			}

			// 装扮
			long timeNow = HawkTime.getMillisecond();
			for (Entry<Integer, DressItem> entry : WorldPointService.getInstance().getShowDress(playerId).entrySet()) {
				WorldShowDress.Builder dressInfo = WorldShowDress.newBuilder();
				DressItem dressItem = entry.getValue();
				dressInfo.setDressType(DressType.valueOf(entry.getKey()));
				dressInfo.setModelType(dressItem.getModelType());
				dressInfo.setRemainShowTime(GameUtil.getDressRemainTime(dressItem) / 1000);
				if (dressItem.getShowType() != 0 && dressItem.getShowEndTime() > timeNow) {
					dressInfo.setShowType(dressItem.getShowType());
					dressInfo.setShowRemainTime((dressItem.getShowEndTime() - timeNow) / 1000);
				}
				builder.addDressShow(dressInfo);
			}
			builder.setDressTitleType(WorldPointService.getInstance().getDressTitleType(playerId));
			// 签名
			KeyValuePair<SignatureState, String> signatureInfo = WorldPointService.getInstance().getPlayerSignatureInfo(playerId);
			builder.setSignatureState(signatureInfo.getKey());
			if (signatureInfo.getKey().equals(SignatureState.CAN_SIGNATURE)) {
				builder.setSignature(signatureInfo.getValue());
			}
			
			builder.setShowEquipTech(WorldPointService.getInstance().getShowEquipTech(playerId));
			builder.setEquipStarShow(WorldPointService.getInstance().getEquipStarShow(playerId));
			builder.setStarExploreShow(WorldPointService.getInstance().getStarExploreShow(playerId));
			//烟花
			builder.setFireworks(WorldPointService.getInstance().getFireWorkType(playerId));
			
			builder.setHasOffensiveMarch(WorldMarchService.getInstance().hasOffensiveMarch(playerId));
			builder.setOffensiveMarchTime(WorldMarchService.getInstance().getOffsiveStateTime(playerId));
			
			DressEditData.Builder collegDressEdit = DressEditData.newBuilder();
			collegDressEdit.setType(DressEditType.COLLEGE_SHOW);
			String collegeName = WorldPointService.getInstance().getCollegeNameShow(playerId);
			if(!HawkOSOperator.isEmptyString(collegeName)){
				collegDressEdit.setData(collegeName);
			}
			builder.addDressEditDatas(collegDressEdit);
			
		} else if (WorldUtil.isPresidentPoint(this)) {
			boolean hasMarchStop = false;
			for (String marchId : WorldMarchService.getInstance().getPresidentMarchs()) {
				WorldMarch march = WorldMarchService.getInstance().getWorldMarch(marchId);
				if (march.getPlayerId().equals(viewerId)) {
					hasMarchStop = true;
				}
			}
			builder.setHasMarchStop(hasMarchStop);
		} else if (WorldUtil.isPresidentTowerPoint(this)) {
			boolean hasMarchStop = false;
			for (String marchId : WorldMarchService.getInstance().getPresidentTowerMarchs(id)) {
				WorldMarch march = WorldMarchService.getInstance().getWorldMarch(marchId);
				if (march.getPlayerId().equals(viewerId)) {
					hasMarchStop = true;
				}
			}
			builder.setHasMarchStop(hasMarchStop);
		} else if (WorldUtil.isSuperWeaponPoint(this)) {
			boolean hasMarchStop = false;
			for (String marchId : WorldMarchService.getInstance().getSuperWeaponMarchs(id)) {
				WorldMarch march = WorldMarchService.getInstance().getWorldMarch(marchId);
				if (march.getPlayerId().equals(viewerId)) {
					hasMarchStop = true;
				}
			}
			builder.setHasMarchStop(hasMarchStop);

			IWeapon weapon = SuperWeaponService.getInstance().getWeapon(id);
			builder.setSuperWeaponType(weapon.getWeaponCfg().getId());
			Player viewer = GlobalData.getInstance().makesurePlayer(viewerId);
			builder.setSwHasGuildSignUp(viewer.hasGuild() && weapon.checkSignUp(viewer.getGuildId()));
			builder.setSwHasSignUp(weapon.hasSignUpGuild());
		} else if (WorldUtil.isFortressPoint(this)) {
			boolean hasMarchStop = false;
			for (String marchId : WorldMarchService.getInstance().getFortressMarchs(id)) {
				WorldMarch march = WorldMarchService.getInstance().getWorldMarch(marchId);
				if (march.getPlayerId().equals(viewerId)) {
					hasMarchStop = true;
				}
			}
			builder.setHasMarchStop(hasMarchStop);
		}

		// 城点保护时间
		long showProtectedEndTime = getShowProtectedEndTime();
		int pointProtectNotify = WorldMapConstProperty.getInstance().getPointProtectNotify();
		if (WorldUtil.isPlayerPoint(this) && showProtectedEndTime - HawkTime.getMillisecond() > GsConst.MINUTE_MILLI_SECONDS * pointProtectNotify) {
			builder.setProtectedEndTime(Long.MAX_VALUE);
		} else {
			builder.setProtectedEndTime(showProtectedEndTime);
		}

		// 普通伤害结束时间点
		if (commonHurtEndTime > 0) {
			builder.setCommonEndTime(commonHurtEndTime);
		}

		if (pointType == WorldPointType.YURI_FACTORY_VALUE) {
			builder.setCommonEndTime(lifeStartTime);
		}

		if (pointType == WorldPointType.FOGGY_FORTRESS_VALUE) {
			builder.setPower(foggyInfoObj.getTotalPower());
			FoggyFortressCfg foggyCfg = HawkConfigManager.getInstance().getConfigByKey(FoggyFortressCfg.class, monsterId);
			long leftTime = (lifeStartTime + foggyCfg.getLifeTime() * 1000) - HawkTime.getMillisecond();
			builder.setCommonEndTime(leftTime);
		}

		// 专属属性
		if (!HawkOSOperator.isEmptyString(ownerId)) {
			builder.setOwnerId(ownerId);
			AccountInfo accountInfo = GlobalData.getInstance().getAccountInfoByPlayerId(ownerId);
			if (accountInfo != null) {
				builder.setOwnerName(accountInfo.getPlayerName());
			}
		}

		if (pointType == WorldPointType.STRONG_POINT_VALUE) {
			builder.setStrongpointId(monsterId);
			builder.setStatus(StrongpointStatus.valueOf(pointStatus));
			builder.setLastActiveTime(lastActiveTime);
			if (!HawkOSOperator.isEmptyString(marchId)) {
				WorldMarch march = WorldMarchService.getInstance().getWorldMarch(marchId);
				builder.setLastActiveTime(lastActiveTime - (HawkTime.getMillisecond() - march.getResStartTime()));
			}
			if (!HawkOSOperator.isEmptyString(viewerId) && viewerId.equals(playerId)) {
				builder.setHasMarchStop(true);
			}
		}

		if (pointType == WorldPointType.TH_RESOURCE_VALUE) {
			builder.setStrongpointId(resourceId);
			if (!HawkOSOperator.isEmptyString(marchId)) {
				WorldMarch march = WorldMarchService.getInstance().getWorldMarch(marchId);
				// 并发情况下可能导致march为null
				if (march != null) {
					builder.setLastActiveTime(march.getResEndTime() - HawkTime.getMillisecond());
				}
			} else {
				TreasureHuntResCfg cfg = HawkConfigManager.getInstance().getConfigByKey(TreasureHuntResCfg.class, resourceId);
				if (cfg != null) {
					builder.setLastActiveTime(cfg.getTickTime());
				}
			}
			if (!HawkOSOperator.isEmptyString(viewerId) && viewerId.equals(playerId)) {
				builder.setHasMarchStop(true);
			}
		}

		if (pointType == WorldPointType.PYLON_VALUE) {
			builder.setStrongpointId(resourceId);
			if (!HawkOSOperator.isEmptyString(marchId)) {
				WorldMarch march = WorldMarchService.getInstance().getWorldMarch(marchId);
				// 并发情况下可能导致march为null
				if (march != null) {
					builder.setLastActiveTime(march.getResEndTime() - HawkTime.getMillisecond());
				}
			} else {
				WorldPylonCfg cfg = HawkConfigManager.getInstance().getConfigByKey(WorldPylonCfg.class, resourceId);
				if (cfg != null) {
					builder.setLastActiveTime(cfg.getTickTime());
				}
			}
			if (!HawkOSOperator.isEmptyString(viewerId) && viewerId.equals(playerId)) {
				builder.setHasMarchStop(true);
			}
		}
		
		// 国家建筑
		if(pointType == WorldPointType.NATIONAL_BUILDING_POINT_VALUE){
			NationalBuilding building = NationService.getInstance().getNationalBuildingByPoint(this.id);
			if(building != null) {
				builder.setNationalBuildType(building.getBuildType().getNumber());
				builder.setNationalBuildState(building.getBuildState().getNumber());
				builder.setNationalBuildLvl(building.getEntity().getLevel());
				builder.setNationalBuildEndTime(building.getCurrentBuildEndTime());
				
				builder.setNationalBuildVal(building.getNationBuildingVal());
				
				builder.setNationalRunningEndTime(building.getRunningEndTime());
				builder.setNationalRunningTotalTime(building.getRunningTotalTime());
				builder.setNationalRunningParams(building.runningStateParam());
			}
		}

		// 是否在反击态
		if (pointType == WorldPointType.QUARTERED_VALUE
				|| pointType == WorldPointType.RESOURCE_VALUE
				|| pointType == WorldPointType.PLAYER_VALUE) {
			if (Objects.nonNull(playerId)) {
				boolean attacker = GuildService.getInstance().isCounterattacker(viewerId, playerId);
				builder.setCounterAttack(attacker);
			}
		}

		if (!HawkOSOperator.isEmptyString(viewerId)) {
			Player viewer = GlobalData.getInstance().makesurePlayer(viewerId);
			SpyMarkItem spyMark = WorldMarchService.getInstance().getSpyMark(viewer, id, marchId, pointType);
			if (spyMark != null) {
				SpyMark.Builder spyMarkBuilder = SpyMark.newBuilder();
				spyMarkBuilder.setSpyTime(spyMark.getStartTime());
				spyMarkBuilder.setMailId(spyMark.getMailId());
				builder.setSpyMark(spyMarkBuilder);
			}
		}

		if (pointType == WorldPointType.WAR_FLAG_POINT_VALUE) {
			IFlag flag = FlagCollection.getInstance().getFlag(guildBuildId);
			builder.setGuildFlag(GuildService.getInstance().getGuildFlag(guildId));
			builder.setOwnerId(flag.getOwnerId());
			builder.setGuildId(flag.getCurrentId());
			if (flag.getRemoveTime() > 0) {
				builder.setManorState(FlageState.FLAG_REMOVE_VALUE);
			} else {
				builder.setManorState(flag.getState());
			}
			builder.setManorBuildLife(flag.getCurrBuildLife());
			builder.setTargetId(flag.getFlagId());
			builder.setFlagView(WarFlagService.getInstance().getFlagViewStatus(viewerId, flag.getFlagId()));

			boolean hasMarchStop = false;
			BlockingDeque<String> flagMarchs = WorldMarchService.getInstance().getFlagMarchs(flag.getFlagId());
			for (String flagMarchId : flagMarchs) {
				IWorldMarch march = WorldMarchService.getInstance().getMarch(flagMarchId);
				if (march == null) {
					continue;
				}
				if (!march.getPlayerId().equals(viewerId)) {
					continue;
				}
				hasMarchStop = true;
			}
			builder.setHasMarchStop(hasMarchStop);

			String guildTag = GuildService.getInstance().getGuildTag(flag.getCurrentId());
			builder.setGuildTag(guildTag == null ? "" : guildTag);

			builder.setManorComTime(flag.getCompleteTime());
			builder.setManorBuildTime(flag.getPlaceTime());
			builder.setIsCenter(flag.isCenter());
			if (flag.isCenter()) {
				builder.setIsCenterActive(flag.isCenterActive());
			}
			builder.setFlagOccupyLife(flag.getCurrOccupyLife());
		}
		if (pointType == WorldPointType.RESOURC_TRESURE_VALUE) {
			builder.setResourceId(resourceId);
			builder.setLastActiveTime(protectedEndTime);
			builder.setManorBuildTime(lifeStartTime);
			builder.setPlayerName(HawkOSOperator.isEmptyString(playerName) ? "" : playerName);
			String guildTag = GuildService.getInstance().getGuildTag(guildId);
			if (StringUtils.isNotEmpty(guildTag)) {
				builder.setGuildTag(guildTag);
			}
			// 取采集记录
			if (Objects.nonNull(resTreaColRecordList)) {
				builder.addAllTreaCollrec(resTreaColRecordList);
			}
		}
		
		if (pointType == WorldPointType.CHRISTMAS_BOX_VALUE) {
			builder.setResourceId(monsterId);
			builder.setPlayerName(HawkOSOperator.isEmptyString(playerName) ? "" : playerName);
			if (!HawkOSOperator.isEmptyString(marchId)) {
				WorldMarch march = WorldMarchService.getInstance().getWorldMarch(marchId);
				// 并发情况下可能导致march为null
				if (march != null) {
					builder.setLastActiveTime(march.getResEndTime());
				}
			}
			
			if (!HawkOSOperator.isEmptyString(playerId) && playerId.equals(viewerId)) {
				builder.setHasMarchStop(true);
			}
		}

		// 只有玩家城点才能（在没有行军时）使用行军表情
		if (pointType == WorldPointType.PLAYER_VALUE) {
			BuilderUtil.buildPlayerEmotion(builder, emoticonUseTime, emoticon);
		} else if (!HawkOSOperator.isEmptyString(marchId)) {
			WorldMarch march = WorldMarchService.getInstance().getWorldMarch(marchId);
			BuilderUtil.buildMarchEmotion(builder, march);
		} else {
			WorldMarch march = WorldMarchService.getInstance().getMarchByPoint(this);
			if (march != null) {
				BuilderUtil.buildMarchEmotion(builder, march);
			}
		}

		if (pointType == WorldPointType.SNOWBALL_VALUE) {
			builder.setSnowballGuildAtk(WorldSnowballService.getInstance().hasGuildKicked(viewerId, monsterId));
			builder.setSnowballStage(WorldSnowballService.getInstance().getCurrentStage());
			builder.setSnowballAtkTimes(WorldSnowballService.getInstance().kickTimes(viewerId, monsterId));
			String lastKickBall = WorldSnowballService.getInstance().getLastKickBall(monsterId);
			if (!HawkOSOperator.isEmptyString(lastKickBall)) {
				builder.setSnowballLastAtk(lastKickBall);
			}
		}

		int[] buildingGoalShowInfo = WorldSnowballService.getInstance().getBuildingGoalShowInfo(this);
		if (buildingGoalShowInfo != null) {
			builder.setSnowballGoalCount(buildingGoalShowInfo[0]);
			builder.setSnowballTargetCount(buildingGoalShowInfo[1]);
			builder.setSnowballOwnGoalCount(WorldSnowballService.getInstance().getOwnGoalCount(this, viewerId));
		}
		
		builder.setSuperVipSkin(superVipSkinBuilder());
		builder.addAllEquipTechLevel(equipTechLevelToBuilder());
		
		World.WorldFormation.Builder formationInfo = getFormationInfo(viewerId);
		if (formationInfo != null) {
			builder.setFormation(formationInfo);
		}
		return builder;
	}

	public void addResTreaColRecord(PBTreaCollRec record) {
		if (Objects.isNull(resTreaColRecordList)) {
			resTreaColRecordList = new ArrayList<>();
		}
		resTreaColRecordList.add(record);
	}

	public void addSnowballKickInfo(SnowballKickInfo.Builder info) {
		this.snowballKickInfo = info;
	}

	public SnowballKickInfo.Builder getSnowballKickInfo() {
		return this.snowballKickInfo;
	}

	public SnowballGoalInfo.Builder getSnowballGoalInfo() {
		return snowballGoalInfo;
	}

	public void addSnowballGoalInfo(SnowballGoalInfo.Builder snowballGoalInfo) {
		this.snowballGoalInfo = snowballGoalInfo;
	}

	public DragonBoatInfo getDragonBoatInfo() {
		return dragonBoatInfo;
	}

	public void setDragonBoatInfo(DragonBoatInfo dragonBoatInfo) {
		this.dragonBoatInfo = dragonBoatInfo;
	}

	public GhostInfo getGhostInfo() {
		return ghostInfo;
	}

	public void setGhostInfo(GhostInfo ghostInfo) {
		this.ghostInfo = ghostInfo;
	}

	public CakeShareInfo getCakeShareInfo() {
		return cakeShareInfo;
	}

	public void setCakeShareInfo(CakeShareInfo cakeShareInfo) {
		this.cakeShareInfo = cakeShareInfo;
	}

	public PointData.Builder buildPointData() {
		PointData.Builder builder = PointData.newBuilder();
		builder.setId(this.getId());
		builder.setX(this.getX());
		builder.setY(this.getY());
		builder.setAreaId(this.getAreaId());
		builder.setZoneId(this.getZoneId());
		builder.setPointType(this.getPointType());
		builder.setPointStatus(this.getPointStatus());
		builder.setPlayerId(this.getPlayerId());
		builder.setPlayerName(this.getPlayerName());
		builder.setCityLevel(this.getCityLevel());
		builder.setPlayerIcon(this.getPlayerIcon());
		builder.setLastActiveTime(this.getLastActiveTime());
		builder.setResourceId(this.getResourceId());
		builder.setMonsterId(this.getMonsterId());
		builder.setLifeStartTime(this.getLifeStartTime());
		builder.setRemainResNum(this.getRemainResNum());
		builder.setRemainBlood(this.getRemainBlood());
		builder.setMarchId(this.getMarchId());
		builder.setGuildId(this.getGuildId());
		builder.setGuildBuildId(this.getGuildBuildId());
		builder.setBuildingId(this.getBuildingId());
		builder.setProtectedEndTime(this.getProtectedEndTime());
		builder.setCommonHurtEndTime(this.getCommonHurtEndTime());
		builder.setShowEffect(this.getShowEffect());
		builder.setOwnerId(this.getOwnerId());
		builder.setFoggyInfo(this.getFoggyInfo());
		builder.setCreateTime(this.getCreateTime());
		builder.setUpdateTime(this.getUpdateTime());
		builder.setPlantMilitaryLevel(this.getPlantMilitaryLevel());
		builder.setPlantMilitaryShow(this.getPlantMilitaryShow());
		builder.setAtkSwId(this.getAtkManhattanSw());
		builder.setDefSwId(this.getDefManhattanSw());
		builder.setAtkSwSkillId(this.getAtkSwSkillId());
		builder.setDefSwSkillId(this.getDefSwSkillId());
		builder.setMechacoreShowInfo(this.getMechaCoreShow());
		if (this.getPersonalProtectInfo() != null) {
			builder.setPersonalProtectInfo(this.getPersonalProtectInfo());
		}
		builder.setEquipTechLevel(this.getEquipTechLevel());
		// 不要在setInvalid之后进行set操作，切记！！！
		builder.setInvalid(this.isInvalid());
		return builder;
	}
	
	public void mergeFromPointData(PointData.Builder builder) {
		this.setId(builder.getId());
		this.setX(builder.getX());
		this.setY(builder.getY());
		this.setAreaId(builder.getAreaId());
		this.setZoneId(builder.getZoneId());
		this.setPointType(builder.getPointType());
		this.setPointStatus(builder.getPointStatus());
		this.setPlayerId(builder.getPlayerId());
		this.setPlayerName(builder.getPlayerName());
		this.setCityLevel(builder.getCityLevel());
		this.setPlayerIcon(builder.getPlayerIcon());
		this.setLastActiveTime(builder.getLastActiveTime());
		this.setResourceId(builder.getResourceId());
		this.setMonsterId(builder.getMonsterId());
		this.setLifeStartTime(builder.getLifeStartTime());
		this.setRemainResNum(builder.getRemainResNum());
		this.setRemainBlood(builder.getRemainBlood());
		this.setMarchId(builder.getMarchId());
		this.setGuildId(builder.getGuildId());
		this.setGuildBuildId(builder.getGuildBuildId());
		this.setBuildingId(builder.getBuildingId());
		this.setProtectedEndTime(builder.getProtectedEndTime());
		this.setCommonHurtEndTime(builder.getCommonHurtEndTime());
		this.setShowEffect(builder.getShowEffect());
		this.setOwnerId(builder.getOwnerId());
		this.setFoggyInfo(builder.getFoggyInfo());
		this.setCreateTime(builder.getCreateTime());
		this.setUpdateTime(builder.getUpdateTime());
		this.setPlantMilitaryLevel(builder.getPlantMilitaryLevel());
		this.setPlantMilitaryShow(builder.getPlantMilitaryShow());
		this.setAtkManhattanSw(builder.getAtkSwId());
		this.setDefManhattanSw(builder.getDefSwId());
		this.setAtkSwSkillId(builder.getAtkSwSkillId());
		this.setDefSwSkillId(builder.getDefSwSkillId());
		this.setMechaCoreShow(builder.getMechacoreShowInfo());
		if (builder.hasPersonalProtectInfo() && builder.getPersonalProtectInfo() != null) {
			this.setPersonalProtectInfo(builder.getPersonalProtectInfo());
		}
		if (!HawkOSOperator.isEmptyString(builder.getEquipTechLevel())) {
			this.setEquipTechLevel(builder.getEquipTechLevel());
			this.equipTechLevelMap.putAll(SerializeHelper.stringToMap(equipTechLevel, Integer.class, Integer.class, "_", ","));
		}
		// 不要在setInvalid之后进行set操作，切记！！！
		this.setInvalid(builder.getInvalid());
	}

	public String getPersonalProtectInfo() {
		return personalProtectInfo;
	}

	public void setPersonalProtectInfo(String personalProtectInfo) {
		this.personalProtectInfo = personalProtectInfo;
	}

	public long getSuperVipSkinEffEndTime() {
		return superVipSkinEffEndTime;
	}

	public void setSuperVipSkinEffEndTime(long superVipSkinEffEndTime) {
		this.superVipSkinEffEndTime = superVipSkinEffEndTime;
	}
	
	public int getSuperVipSkinLevel() {
		return superVipSkinLevel;
	}

	public void setSuperVipSkinLevel(int superVipSkinLevel) {
		this.superVipSkinLevel = superVipSkinLevel;
	}
	
	public String getEquipTechLevel() {
		return equipTechLevel;
	}

	public void setEquipTechLevel(String equipTechLevel) {
		this.equipTechLevel = equipTechLevel;
	}

	public void updateEquipTechLevel(int techId, int level) {
		equipTechLevelMap.put(techId, level);
		equipTechLevel = SerializeHelper.mapToString(equipTechLevelMap, "_", ",");
		notifyUpdate();
	}

	/**
	 * 填充个人编队信息
	 * @param viewerId
	 */
	private World.WorldFormation.Builder getFormationInfo(String viewerId) {
		try {
			if (HawkOSOperator.isEmptyString(viewerId)) {
				return null;
			}
//			List<Integer> condation = new ArrayList<>();
//			condation.add(WorldPointType.RESOURCE_VALUE);
//			condation.add(WorldPointType.STRONG_POINT_VALUE);
//			condation.add(WorldPointType.QUARTERED_VALUE);
//			if (!condation.contains(pointType)) {
//				return null;
//			}
			if (HawkOSOperator.isEmptyString(marchId)) {
				return null;
			}
			if (!viewerId.equals(playerId)) {
				return null;
			}
			IWorldMarch march = WorldMarchService.getInstance().getMarch(marchId);
			if (march == null) {
				return null;
			}
			int formation = march.getMarchEntity().getFormation();
			if (formation == 0) {
				return null;
			}
			World.WorldFormation.Builder formationBuilder = World.WorldFormation.newBuilder();
			formationBuilder.setIndex(formation);
			return formationBuilder;
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return null;
	}
}
