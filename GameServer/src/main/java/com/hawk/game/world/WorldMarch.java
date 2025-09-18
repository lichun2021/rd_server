package com.hawk.game.world;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Collectors;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang.math.NumberUtils;
import org.hawk.annotation.IndexProp;
import org.hawk.config.HawkConfigManager;
import org.hawk.db.HawkDBEntity;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.hawk.game.config.MarchEmoticonProperty;
import com.hawk.game.config.YuriRevengeCfg;
import com.hawk.game.crossactivity.resourcespree.ResourceSpreeBoxWorldMarch;
import com.hawk.game.entity.item.DressItem;
import com.hawk.game.global.GlobalData;
import com.hawk.game.item.AwardItems;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.lianmengxzq.march.XZQMassJoinMarch;
import com.hawk.game.lianmengxzq.march.XZQMassMarch;
import com.hawk.game.lianmengxzq.march.XZQSingleMarch;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.module.spacemecha.MechaSpaceInfo;
import com.hawk.game.module.spacemecha.SpaceMechaService;
import com.hawk.game.module.spacemecha.config.SpaceMechaEnemyCfg;
import com.hawk.game.module.spacemecha.worldmarch.SpaceMechaBoxMarch;
import com.hawk.game.module.spacemecha.worldmarch.SpaceMechaEmptyMarch;
import com.hawk.game.module.spacemecha.worldmarch.SpaceMechaMainMassJoinMarch;
import com.hawk.game.module.spacemecha.worldmarch.SpaceMechaMainMassMarch;
import com.hawk.game.module.spacemecha.worldmarch.SpaceMechaMainSingleMarch;
import com.hawk.game.module.spacemecha.worldmarch.SpaceMechaMonsterAttackMarch;
import com.hawk.game.module.spacemecha.worldmarch.SpaceMechaStrongHoldMassJoinMarch;
import com.hawk.game.module.spacemecha.worldmarch.SpaceMechaStrongHoldMassMarch;
import com.hawk.game.module.spacemecha.worldmarch.SpaceMechaStrongHoldSingleMarch;
import com.hawk.game.module.spacemecha.worldmarch.SpaceMehaSlaveSingleMarch;
import com.hawk.game.module.spacemecha.worldpoint.SpaceWorldPoint;
import com.hawk.game.player.Player;
import com.hawk.game.player.hero.PlayerHero;
import com.hawk.game.player.supersoldier.SuperSoldier;
import com.hawk.game.protocol.Armour.ArmourSuitType;
import com.hawk.game.protocol.Dress.DressType;
import com.hawk.game.protocol.MechaCore.MechaCoreSuitType;
import com.hawk.game.protocol.World;
import com.hawk.game.protocol.World.WorldMarchPB;
import com.hawk.game.protocol.World.WorldMarchRelation;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.service.GuildService;
import com.hawk.game.util.AlgorithmUtil;
import com.hawk.game.util.EffectParams;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.WorldUtil;
import com.hawk.game.world.march.IWorldMarch;
import com.hawk.game.world.march.impl.AgencyCoasterMarch;
import com.hawk.game.world.march.impl.AgencyMonsterMarch;
import com.hawk.game.world.march.impl.AgencyRescurMarch;
import com.hawk.game.world.march.impl.ArmyQuarteredMarch;
import com.hawk.game.world.march.impl.AssistanceResMarch;
import com.hawk.game.world.march.impl.AssistanceSingleMarch;
import com.hawk.game.world.march.impl.AttackGhostMarch;
import com.hawk.game.world.march.impl.AttackMonster;
import com.hawk.game.world.march.impl.AttackPlayerMarch;
import com.hawk.game.world.march.impl.CakeShareMarch;
import com.hawk.game.world.march.impl.CaptiveReleaseMarch;
import com.hawk.game.world.march.impl.CenterFlagMarch;
import com.hawk.game.world.march.impl.ChristmasBoxMarch;
import com.hawk.game.world.march.impl.ChristmasMassJoinMarch;
import com.hawk.game.world.march.impl.ChristmasMassMarch;
import com.hawk.game.world.march.impl.ChristmasSingleMarch;
import com.hawk.game.world.march.impl.CollectResTreasureMarch;
import com.hawk.game.world.march.impl.CollectWorldResMarch;
import com.hawk.game.world.march.impl.DragonboatMarch;
import com.hawk.game.world.march.impl.EspionageMarch;
import com.hawk.game.world.march.impl.FoggyMassJoinMarch;
import com.hawk.game.world.march.impl.FoggyMassMarch;
import com.hawk.game.world.march.impl.FoggySingleMarch;
import com.hawk.game.world.march.impl.FortressMassJoinMarch;
import com.hawk.game.world.march.impl.FortressMassMarch;
import com.hawk.game.world.march.impl.FortressSingleMarch;
import com.hawk.game.world.march.impl.GundamMassJoinMarch;
import com.hawk.game.world.march.impl.GundamMassMarch;
import com.hawk.game.world.march.impl.GundamSingleMarch;
import com.hawk.game.world.march.impl.HiddenMarch;
import com.hawk.game.world.march.impl.ManorAssistanceMarch;
import com.hawk.game.world.march.impl.ManorAssistanceMassJoinMarch;
import com.hawk.game.world.march.impl.ManorAssistanceMassMarch;
import com.hawk.game.world.march.impl.ManorBuildMarch;
import com.hawk.game.world.march.impl.ManorCollectMarch;
import com.hawk.game.world.march.impl.ManorDragonTrapMassJoinMarch;
import com.hawk.game.world.march.impl.ManorDragonTrapMassMarch;
import com.hawk.game.world.march.impl.ManorMassJoinMarch;
import com.hawk.game.world.march.impl.ManorMassMarch;
import com.hawk.game.world.march.impl.ManorRepairMarch;
import com.hawk.game.world.march.impl.ManorSingleMarch;
import com.hawk.game.world.march.impl.MassJoinSingleMarch;
import com.hawk.game.world.march.impl.MassMonsterJoinMarch;
import com.hawk.game.world.march.impl.MassMonsterMarch;
import com.hawk.game.world.march.impl.MassSingleMarch;
import com.hawk.game.world.march.impl.NationalConstructMarch;
import com.hawk.game.world.march.impl.NewMonsterMarch;
import com.hawk.game.world.march.impl.NianBoxMarch;
import com.hawk.game.world.march.impl.NianMassJoinMarch;
import com.hawk.game.world.march.impl.NianMassMarch;
import com.hawk.game.world.march.impl.NianSingleMarch;
import com.hawk.game.world.march.impl.OverlordBlessingMarch;
import com.hawk.game.world.march.impl.PresidentMassJoinMarch;
import com.hawk.game.world.march.impl.PresidentMassMarch;
import com.hawk.game.world.march.impl.PresidentSingleMarch;
import com.hawk.game.world.march.impl.PresidentTowerMassJoinMarch;
import com.hawk.game.world.march.impl.PresidentTowerMassMarch;
import com.hawk.game.world.march.impl.PresidentTowerSingleMarch;
import com.hawk.game.world.march.impl.PylonMarch;
import com.hawk.game.world.march.impl.RandomChest;
import com.hawk.game.world.march.impl.SnowballMarch;
import com.hawk.game.world.march.impl.SpyMarch;
import com.hawk.game.world.march.impl.StrongpointMarch;
import com.hawk.game.world.march.impl.SuperWeaponMassJoinMarch;
import com.hawk.game.world.march.impl.SuperWeaponMassMarch;
import com.hawk.game.world.march.impl.SuperWeaponSingleMarch;
import com.hawk.game.world.march.impl.TreasureHuntMarch;
import com.hawk.game.world.march.impl.TreasureHuntMonsterMassJoinMarch;
import com.hawk.game.world.march.impl.TreasureHuntMonsterMassMarch;
import com.hawk.game.world.march.impl.TreasureHuntResMarch;
import com.hawk.game.world.march.impl.WarFlagMarch;
import com.hawk.game.world.march.impl.WarFlagMassJoinMarch;
import com.hawk.game.world.march.impl.WarFlagMassMarch;
import com.hawk.game.world.march.impl.WarehouseGetMarch;
import com.hawk.game.world.march.impl.WarehouseStoreMarch;
import com.hawk.game.world.march.impl.YuriRevengeMonsterMarch;
import com.hawk.game.world.march.impl.YuriStrikeMonsterMarch;
import com.hawk.game.world.service.WorldPointService;
import com.hawk.serialize.string.SerializeHelper;

/**
 * 出征信息
 * 
 * @author julia
 *
 */
@Entity
@Table(name = "world_march")
public class WorldMarch extends HawkDBEntity implements Comparable<WorldMarch> {
	@Id
	@Column(name = "marchId", unique = true, nullable = false)
    @IndexProp(id = 1)
	private String marchId;

	// 玩家id
	@Column(name = "playerId", nullable = false)
    @IndexProp(id = 2)
	private String playerId;

	// 玩家名称
	@Column(name = "playerName", nullable = false)
    @IndexProp(id = 3)
	private String playerName;

	// 出发点
	@Column(name = "origionId", nullable = false)
    @IndexProp(id = 4)
	private int origionId;

	// 终点
	@Column(name = "terminalId", nullable = false)
    @IndexProp(id = 5)
	private int terminalId;
	
	// 警报触发点
	@Column(name = "alarmPointId", nullable = false)
    @IndexProp(id = 6)
	private int alarmPointId;

	// 加速倍数
	@Column(name = "speedUpTimes", nullable = true)
    @IndexProp(id = 7)
	private int speedUpTimes;

	// 使用道具的客户端像素点
	@Column(name = "itemUseX", nullable = true)
    @IndexProp(id = 8)
	private double itemUseX;

	// 使用道具的客户端像素点
	@Column(name = "itemUseY", nullable = true)
    @IndexProp(id = 9)
	private double itemUseY;

	// 使用道具的时间
	@Column(name = "itemUseTime", nullable = true)
    @IndexProp(id = 10)
	private long itemUseTime;

	// 使用道具的点
	@Column(name = "callBackX", nullable = true)
    @IndexProp(id = 11)
	private double callBackX;

	// 使用道具的点
	@Column(name = "callBackY", nullable = true)
    @IndexProp(id = 12)
	private double callBackY;

	// 召回时间点
	@Column(name = "callBackTime", nullable = true)
    @IndexProp(id = 13)
	private long callBackTime;

	// 行军类型 ,采集打怪
	@Column(name = "marchType", nullable = false)
    @IndexProp(id = 14)
	private int marchType;

	// 行军意图标识
	@Column(name = "marchIntention", nullable = false)
    @IndexProp(id = 15)
	private int marchIntention;

	// 出征，回程，执行
	@Column(name = "marchStatus", nullable = false)
    @IndexProp(id = 16)
	private int marchStatus;
	
	//行军处理标记位
	@Column(name = "marchProcMask", nullable = false)
    @IndexProp(id = 17)
	private int marchProcMask;

	// 开始时间
	@Column(name = "startTime", nullable = false)
    @IndexProp(id = 18)
	private long startTime;

	// 整个行程需要的时间
	@Column(name = "marchJourneyTime", nullable = false)
    @IndexProp(id = 19)
	private long marchJourneyTime;

	// 结束时间
	@Column(name = "endTime", nullable = false)
    @IndexProp(id = 20)
	private long endTime;

	// 收集资源的开始时间
	@Column(name = "resStartTime", nullable = true)
    @IndexProp(id = 21)
	private long resStartTime;

	// 收集资源结束的时间
	@Column(name = "resEndTime", nullable = true)
    @IndexProp(id = 22)
	private long resEndTime;

	// 采集速度的总值
	@Column(name = "collectSpeed", nullable = true)
    @IndexProp(id = 23)
	private double collectSpeed;

	// 采集素的的基础值
	@Column(name = "collectBaseSpeed", nullable = true)
    @IndexProp(id = 24)
	private double collectBaseSpeed;
	
	// 集结准备的时间
	@Column(name = "massReadyTime", nullable = false)
    @IndexProp(id = 25)
	private long massReadyTime;

	// 去程行军到达目标点的时间
	@Column(name = "reachTime", nullable = false)
    @IndexProp(id = 26)
	private long reachTime;

	// 目标id,怪id，资源id，等等等等
	@Column(name = "targetId", nullable = false)
    @IndexProp(id = 27)
	private String targetId;

	// 要攻打的目标点的点类型
	@Column(name = "targetPointType", nullable = false)
    @IndexProp(id = 28)
	private int targetPointType;

	// 目标点的附带属性，不同需求填不同值，侦查填资源ID;
	@Column(name = "targetPointField", nullable = true)
    @IndexProp(id = 29)
	private String targetPointField;

	// 队长id
	@Column(name = "leaderPlayerId", nullable = true)
    @IndexProp(id = 30)
	private String leaderPlayerId;

	// 携带军队存库信息
	@Column(name = "armyStr", nullable = false)
    @IndexProp(id = 31)
	private String armyStr;

	// 携带英雄存库信息
	@Column(name = "heroIdStr", nullable = true)
    @IndexProp(id = 32)
	private String heroIdStr;
	
	@Column(name = "superSoldierId", nullable = true)
    @IndexProp(id = 33)
	private int superSoldierId;

	// 援助字串
	@Column(name = "assistantStr", nullable = true)
    @IndexProp(id = 34)
	private String assistantStr;

	// 奖励字串
	@Column(name = "awardStr", nullable = true)
    @IndexProp(id = 35)
	private String awardStr;

	// 额外奖励字串
	@Column(name = "awardExtraStr", nullable = true)
    @IndexProp(id = 36)
	private String awardExtraStr;

	// 打怪次数
	@Column(name = "attackTimes", nullable = true)
    @IndexProp(id = 37)
	private int attackTimes = 0;

	// 领地驻军的到达时间
	@Column(name = "manorMarchReachTime", nullable = true)
    @IndexProp(id = 38)
	private long manorMarchReachTime = 0;
	
	// 上次探索的时间
	@Column(name = "lastExploreTime", nullable = true)
    @IndexProp(id = 39)
	private long lastExploreTime = 0;
	
	// 上次探索的时间
	@Column(name = "isOffensive", nullable = true)
    @IndexProp(id = 40)
	private int isOffensive = 0;
		
	// 购买行军格子次数
	@Column(name = "buyItemTimes", nullable = true)
    @IndexProp(id = 41)
	private int buyItemTimes = 0;
	
	//箭塔伤害记录
	@Column(name = "towerAttackInfo")
    @IndexProp(id = 42)
	private String towerAttackInfo;

	// 行军附带作用号
	@Column(name = "effect")
    @IndexProp(id = 43)
	private String effect;

	// 攻打野怪体力消耗
	@Column(name = "vitCost")
    @IndexProp(id = 44)
	private int vitCost;
	
	// 记录创建时间
	@Column(name = "createTime", nullable = false)
    @IndexProp(id = 45)
	private long createTime = 0;

	// 最后一次更新时间
	@Column(name = "updateTime")
    @IndexProp(id = 46)
	private long updateTime;

	// 记录是否无效
	@Column(name = "invalid")
    @IndexProp(id = 47)
	private boolean invalid;
	
	// 记录是否时额外侦查队列
	@Column(name = "extraSpyMarch")
    @IndexProp(id = 48)
	private boolean extraSpyMarch;

	// 铠甲套装
	@Column(name = "armourSuit")
	@IndexProp(id = 49)
	private int armourSuit;

	// 天赋
	@Column(name = "talentType")
	@IndexProp(id = 50)
	private int talentType;
	
	// 能量源
	@Column(name = "superLab")
	@IndexProp(id = 51)
	private int superLab;
	
	// 行军表情
	@Column(name = "emoticon")
	@IndexProp(id = 52)
	private int emoticon;
	
	// 行军表情使用时间
	@Column(name = "emoticonUseTime")
	@IndexProp(id = 53)
	private long emoticonUseTime;

	// 个人编队
	@Column(name = "formation")
	@IndexProp(id = 54)
	private int formation;

	// 皮肤信息
	@Column(name = "dressStr")
	@IndexProp(id = 55)
	private String dressStr;
	
	@Column(name = "extraInfo")
	@IndexProp(id = 56)
	private String extraInfo = "";
	
	//机甲核心套装
	@Column(name = "mechacoreSuit")
	@IndexProp(id = 57)
	private int mechacoreSuit;

	//超武进攻
	@Column(name = "manhattanAtkSwId")
	@IndexProp(id = 58)
	private int manhattanAtkSwId;
	//超武防守
	@Column(name = "manhattanDefSwId")
	@IndexProp(id = 59)
	private int manhattanDefSwId;

	// 携带的军队信息
	@Transient
	private List<ArmyInfo> armys;

	// 行军附带作用号
	@Transient
	private List<Integer> effectList;
	
	@Transient
	private AwardItems awardItems;

	// 出发点x
	@Transient
	private int origionX;

	// 出发点y
	@Transient
	private int origionY;

	// 终点x
	@Transient
	private int terminalX;

	// 终点y
	@Transient
	private int terminalY;
	
	// 自动打野行军标识
	@Transient
	private int autoMarchIdentify;
	
	// 自动拉锅行军标识
	@Transient
	private int autoResourceIdentify;

	@Transient
	private EffectParams effParams;
	@Transient
	private Set<String> reportSend;
	
	//藏宝图行军，同时发出行军个数标识
	@Transient
	private int treasureCount;
	
	
	// 自动打野行军标识
	@Transient
	private int autoMassJoinIdentify;
	
	public WorldMarch() {
		this.dressStr = "";
		this.awardItems = AwardItems.valueOf();
		resetEffect(new ArrayList<>());
	}

	/**
	 * DB读取结束初始化
	 */
	public void init() {
		armys = WorldUtil.convertStringToArmyList(armyStr);
		
		// 起始坐标
		int[] xy = GameUtil.splitXAndY(origionId);
		this.origionX = xy[0];
		this.origionY = xy[1];

		// 终点坐标
		xy = GameUtil.splitXAndY(terminalId);
		this.terminalX = xy[0];
		this.terminalY = xy[1];

		// 采集携带资源
		AwardItems award = AwardItems.valueOf();
		if (!HawkOSOperator.isEmptyString(awardStr)) {
			// 这里不能直接调用 award.addItemInfos()方法，因为award.addItemInfos()会对count=0进行拦截，会出问题。
			List<ItemInfo> awardItems = ItemInfo.valueListOf(awardStr);
			for (ItemInfo awardItem : awardItems) {
				award.addNewItem(awardItem.getType(), awardItem.getItemId(), (int)awardItem.getCount());
			}
		}
		setAwardItems(award);
		
		if (!HawkOSOperator.isEmptyString(effect)) {
			this.effectList = SerializeHelper.stringToList(Integer.class, effect, ",");
		}
	}

	public String getPlayerName() {
		return playerName;
	}

	public String getLeaderPlayerId() {
		return leaderPlayerId;
	}

	public double getCollectSpeed() {
		return collectSpeed;
	}

	public void setCollectSpeed(double collectSpeed) {
		this.collectSpeed = collectSpeed;
	}

	public double getCollectBaseSpeed() {
		return collectBaseSpeed;
	}

	public void setCollectBaseSpeed(double collectBaseSpeed) {
		this.collectBaseSpeed = collectBaseSpeed;
	}

	public void setLeaderPlayerId(String leaderPlayerId) {
		this.leaderPlayerId = leaderPlayerId;
	}

	public void setAttackTimes(int attackTimes) {
		this.attackTimes = attackTimes;
	}

	public void setPlayerName(String playerName) {
		this.playerName = playerName;
	}

	public void setAssistantStr(String assistantStr) {
		this.assistantStr = assistantStr;
	}

	public void setResStartTime(long resStartTime) {
		this.resStartTime = resStartTime;
	}

	public void setResEndTime(long resEndTime) {
		this.resEndTime = resEndTime;
	}

	public void setAwardExtraStr(String awardExtraStr) {
		this.awardExtraStr = awardExtraStr;
	}

	public void setItemUseTime(long itemUseTime) {
		this.itemUseTime = itemUseTime;
	}

	public void setSpeedUpTimes(int speedUpTimes) {
		this.speedUpTimes = speedUpTimes;
	}

	public void setMarchId(String marchId) {
		this.marchId = marchId;
	}

	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}

	public void setOrigionId(int origionId) {
		this.origionId = origionId;
		int[] xy = GameUtil.splitXAndY(origionId);
		this.origionX = xy[0];
		this.origionY = xy[1];
	}

	public void setTerminalId(int terminalId) {
		this.terminalId = terminalId;
		int[] xy = GameUtil.splitXAndY(terminalId);
		this.terminalX = xy[0];
		this.terminalY = xy[1];
	}

	public int getAlarmPointId() {
		return alarmPointId;
	}

	public void setAlarmPointId(int alarmPointId) {
		this.alarmPointId = alarmPointId;
	}

	public void setMarchType(int marchType) {
		this.marchType = marchType;
	}

	public void setMarchStatus(int marchStatus) {
		this.marchStatus = marchStatus;
	}

	public void setMarchProcMask(int marchProcMask) {
		this.marchProcMask = marchProcMask;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

	public void setTargetId(String targetId) {
		this.targetId = targetId;
	}

	public void setItemUseX(double itemUseX) {
		this.itemUseX = itemUseX;
	}

	public void setTargetPointType(int targetPointType) {
		this.targetPointType = targetPointType;
	}

	public void setItemUseY(double itemUseY) {
		this.itemUseY = itemUseY;
	}

	public void setArmyStr(String armyStr) {
		this.armyStr = armyStr;
	}

	public void setAwardStr(String awardStr) {
		this.awardStr = awardStr;
	}

	public void setArmys(List<ArmyInfo> armys) {
		this.armys = armys;
		if (armys != null) {
			setArmyStr(WorldUtil.convertArmyListToDbString(armys));
		}
	}

	public void setMassReadyTime(long massReadyTime) {
		this.massReadyTime = massReadyTime;
	}

	public AwardItems getAwardItems() {
		return awardItems;
	}

	public void setTargetPointField(String targetPointField) {
		this.targetPointField = targetPointField;
	}

	public void setAwardItems(AwardItems awardItems) {
		this.awardItems = awardItems;
		if (this.awardItems != null) {
			setAwardStr(awardItems.toDbString());
		} else {
			this.awardItems = AwardItems.valueOf();
			setAwardStr(null);
		}
	}
	
	public void saveAwardItems(){
		if (this.awardItems != null) {
			setAwardStr(awardItems.toDbString());
		}
	}

	public void setCallBackTime(long callBackTime) {
		this.callBackTime = callBackTime;
	}

	public void setCallBackX(double callbackX) {
		this.callBackX = callbackX;
	}

	public void setCallBackY(double callbackY) {
		this.callBackY = callbackY;
	}

	public void setMarchJourneyTime(long marchJourneyTime) {
		this.marchJourneyTime = marchJourneyTime;
	}

	public void setManorMarchReachTime(long manorMarchReachTime) {
		this.manorMarchReachTime = manorMarchReachTime;
	}

	public void setReachTime(long reachTime) {
		this.reachTime = reachTime;
	}

	public void setMarchIntention(int marchIntention) {
		this.marchIntention = marchIntention;
	}

	public void setBuyItemTimes(int buyItemTimes) {
		this.buyItemTimes = buyItemTimes;
	}
	
	public void setLastExploreTime(long lastExploreTime) {
		this.lastExploreTime = lastExploreTime;
	}

	
	public void setIsOffensive(int isOffensive) {
		this.isOffensive = isOffensive;
	}

	public String getTowerAttackInfo() {
		return towerAttackInfo;
	}

	public void setTowerAttackInfo(String towerAttackInfo) {
		this.towerAttackInfo = towerAttackInfo;
	}
	
	public String getHeroIdStr() {
		return heroIdStr;
	}

	public void setHeroIdStr(String heroIdStr) {
		this.heroIdStr = heroIdStr;
	}

	public List<Integer> getHeroIdList() {
		if(Objects.isNull(heroIdStr)){
			return Collections.emptyList();
		}
		
		return Splitter.on(",").omitEmptyStrings().splitToList(heroIdStr).stream()
				.mapToInt(NumberUtils::toInt)
				.mapToObj(Integer::valueOf)
				.collect(Collectors.toList());
	}

	public List<Integer> getDressList() {
		if(HawkOSOperator.isEmptyString(dressStr)){
			return Collections.emptyList();
		}
		
		return Splitter.on(",").omitEmptyStrings().splitToList(dressStr).stream()
				.mapToInt(NumberUtils::toInt)
				.mapToObj(Integer::valueOf)
				.collect(Collectors.toList());
	}
	public void setHeroIdList(List<Integer> heroIdList) {
		this.heroIdStr = Joiner.on(",").join(heroIdList);
	}
	
	public void setDressList(List<Integer> dressList) {
		this.dressStr = Joiner.on(",").join(dressList);
	}

	public String getTargetPointField() {
		return targetPointField;
	}

	public double getItemUseY() {
		return itemUseY;
	}

	public double getItemUseX() {
		return itemUseX;
	}

	public int getTargetPointType() {
		return targetPointType;
	}

	public int getBuyItemTimes() {
		return buyItemTimes;
	}

	public int getMarchIntention() {
		return marchIntention;
	}

	public long getReachTime() {
		return reachTime;
	}

	public long getManorMarchReachTime() {
		return manorMarchReachTime;
	}

	public double getCallbackX() {
		return callBackX;
	}

	public double getCallbackY() {
		return callBackY;
	}

	public long getCallBackTime() {
		return callBackTime;
	}

	public long getMassReadyTime() {
		return massReadyTime;
	}

	public int getAttackTimes() {
		return attackTimes;
	}

	public String getMarchId() {
		return marchId;
	}

	public String getPlayerId() {
		return playerId;
	}

	public int getOrigionId() {
		return origionId;
	}

	public int getTerminalId() {
		return terminalId;
	}

	public int getMarchType() {
		return marchType;
	}

	public int getMarchStatus() {
		return marchStatus;
	}

	public int getMarchProcMask() {
		return marchProcMask;
	}

	public long getMarchJourneyTime() {
		return marchJourneyTime;
	}

	public long getStartTime() {
		return startTime;
	}

	public long getEndTime() {
		return endTime;
	}

	public String getArmyStr() {
		return armyStr;
	}

	public String getAwardStr() {
		return awardStr;
	}

	public List<ArmyInfo> getArmys() {
		return armys;
	}

	public String getTargetId() {
		return targetId;
	}

	public long getItemUseTime() {
		return itemUseTime;
	}

	public int getSpeedUpTimes() {
		return speedUpTimes;
	}

	public String getAwardExtraStr() {
		return awardExtraStr;
	}

	public String getAssistantStr() {
		return assistantStr;
	}

	public int getOrigionX() {
		return origionX;
	}

	public long getResStartTime() {
		return resStartTime;
	}

	public long getResEndTime() {
		return resEndTime;
	}

	public int getOrigionY() {
		return origionY;
	}

	public int getTerminalX() {
		return terminalX;
	}

	public int getTerminalY() {
		return terminalY;
	}

	public long getLastExploreTime() {
		return lastExploreTime;
	}

	public int getVitCost() {
		return vitCost;
	}

	public void setVitCost(int vitCost) {
		this.vitCost = vitCost;
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

	@Override
	public int compareTo(WorldMarch march) {
		if (this.getReachTime() < march.getReachTime()) {
			return -1;
		}

		if (this.getReachTime() > march.getReachTime()) {
			return 1;
		}

		if (this.getManorMarchReachTime() < march.getManorMarchReachTime()) {
			return -1;
		}

		if (this.getManorMarchReachTime() > march.getManorMarchReachTime()) {
			return 1;
		}

		if (this.createTime < march.getCreateTime()) {
			return -1;
		}

		if (this.createTime > march.getCreateTime()) {
			return 1;
		}

		return 0;
	}

	/**
	 * 是否是攻击性的行
	 * @return
	 */
	public boolean isOffensive() {
		return isOffensive > 0;
	}
	
	public int getArmyFreeCount() {
		int count = 0;
		for (ArmyInfo army : armys) {
			count += army.getFreeCnt();
		}
		return count;
	}
	
	/**
	 * 获取战斗用军队信息
	 */
	public List<ArmyInfo> getArmyCopy() {
		List<ArmyInfo> armysInfo = new ArrayList<>();
		for (ArmyInfo armyInfo : armys) {
			armysInfo.add(armyInfo.getCopy());
		}
		return armysInfo;
	}

	/**
	 * 返程
	 */
	public void changeOrigionTerminal() {
		int temp = this.origionId;
		setOrigionId(this.terminalId);
		setTerminalId(temp);
		this.startTime = HawkTime.getMillisecond();
		this.marchStatus = WorldMarchStatus.MARCH_STATUS_RETURN_BACK_VALUE;
	}

	/**
	 * 计算行军线和点的距离
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public boolean isVisibleOnPos(int x, int y, int viewRadius) {
		int oriX = origionX;
		int oriY = origionY;
		if (callBackX > 1.0 && callBackY > 1.0) {
			oriX = (int) Math.floor(callBackX);
			oriY = (int) Math.floor(callBackY);
		}

		int oriDist = (x - oriX) * (x - oriX) + (y - oriY) * (y - oriY);
		int terDist = (x - terminalX) * (x - terminalX) + (y - terminalY) * (y - terminalY);

		// 和起点以及终点的距离判断
		if (oriDist <= viewRadius * viewRadius || terDist <= viewRadius * viewRadius) {
			return true;
		}

		if ((x >= Math.min(oriX, terminalX) && x <= Math.max(oriX, terminalX))
				|| (y >= Math.min(oriY, terminalY) && y <= Math.max(oriY, terminalY))) {
			// 点和线段的距离判断
			return AlgorithmUtil.getDisPointToLine(x, y, oriX, oriY, terminalX, terminalY) <= viewRadius;
		}

		return false;
	}

	/**
	 * 构建出征信息builder
	 * 
	 * @param relation
	 * @return
	 */
	public WorldMarchPB.Builder toBuilder(WorldMarchPB.Builder builder, WorldMarchRelation relation) {
		builder.setPlayerId(playerId);

		if (!HawkOSOperator.isEmptyString(playerName)) {
			builder.setPlayerName(playerName);
		} else {
			builder.setPlayerName(GlobalData.getInstance().getPlayerNameById(playerId));
		}

		builder.setEndTime(endTime);
		builder.setMarchJourneyTime(marchJourneyTime);
		builder.setExtraSpyMarch(extraSpyMarch);
		builder.setAutoMonster(autoMarchIdentify / 100);
		builder.setAutoResource(autoResourceIdentify>0?1:0);
		builder.setTreasureCount(treasureCount);
		String guildTag = GuildService.getInstance().getPlayerGuildTag(playerId);
		if (guildTag != null && !"".equals(guildTag)) {
			builder.setGuildTag(guildTag);
		}

		// 像素坐标
		if (itemUseX != 0 && itemUseY != 0) {
			builder.setItemUseTime(itemUseTime);
			builder.setItemUseX(itemUseX);
			builder.setItemUseY(itemUseY);
		}

		builder.setMarchId(marchId);
		builder.setMarchStatus(WorldMarchStatus.valueOf(marchStatus));
		builder.setMarchType(WorldMarchType.valueOf(marchType));

		builder.setOrigionX(origionX);
		builder.setOrigionY(origionY);
		builder.setRelation(relation);

		if (speedUpTimes > 0) {
			builder.setSpeedTimes(speedUpTimes);
		}

		builder.setStartTime(startTime);
		builder.setTargetId(targetId);

		builder.setTerminalX(terminalX);
		builder.setTerminalY(terminalY);

		if (callBackX > 0 || callBackY > 0) {
			builder.setCallBackX(callBackX);
			builder.setCallBackY(callBackY);
			builder.setCallBackTime(callBackTime);
		}

		if (resEndTime > 0) {
			builder.setResEndTime(resEndTime);
		}

		if (resStartTime > 0) {
			builder.setResStartTime(resStartTime);
		}

		if (massReadyTime > 0) {
			builder.setMassReadyTime(massReadyTime);
		}
		
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		List<Integer> heroIdList = getHeroIdList();
		if(!heroIdList.isEmpty()){
			if (player == null) {
				WorldPoint point = WorldPointService.getInstance().getWorldPoint(getTerminalId());
				if (point != null && (point.getPointType() == WorldPointType.SPACE_MECHA_MAIN_VALUE || point.getPointType() == WorldPointType.SPACE_MECHA_SLAVE_VALUE)) {
					SpaceWorldPoint spacePoint = (SpaceWorldPoint) point;
					player = spacePoint.getNpcPlayer(getOrigionId());
				}
			}
			
			if (player != null) {
				List<PlayerHero> heros = player.getHeroByCfgId(heroIdList);
				for(PlayerHero hero: heros){
					builder.addHeroList(hero.toPBobj());
					int showProficiencyEffect = hero.getShowProficiencyEffect();
					if(showProficiencyEffect>0){
						builder.addProficiencyEffect(showProficiencyEffect);
					}
				}
			}
		}
		if(superSoldierId>0){
			Optional<SuperSoldier> ssoldierOp = player.getSuperSoldierByCfgId(superSoldierId);
			if(ssoldierOp.isPresent()){
				builder.setSsoldier(ssoldierOp.get().toPBobj());
			}
		}
		
		//部署的进攻型超武Id
		if (player != null) {
			builder.setDeployedSwInfo(player.getDeployedSwInfo());
			builder.setMechacoreShowInfo(player.getMechacoreShowInfo());
		}
		
		for (int i = 0; i < armys.size(); i++) {
			ArmyInfo info = armys.get(i);
			if (info == null) {
				continue;
			}
			if (info.getFreeCnt() <= 0) {
				continue;
			}
			builder.addArmy(info.toArmySoldierPB(player).build());
		}
		//尤里复仇需要加模型信息
		if(marchType == WorldMarchType.YURI_MONSTER_VALUE){
			YuriRevengeMonsterMarch yuriMarch = (YuriRevengeMonsterMarch) WorldMarchService.getInstance().getMarch(marchId);
			if(yuriMarch != null){
				int round = yuriMarch.getRound();
				YuriRevengeCfg cfg = HawkConfigManager.getInstance().getConfigByKey(YuriRevengeCfg.class, round);
				builder.setModelLvl(cfg.getModelLevel());
			}
		} else if (marchType == WorldMarchType.SPACE_MECHA_MONSTER_ATTACK_MARCH_VALUE) {
			MechaSpaceInfo spaceObject = SpaceMechaService.getInstance().getGuildSpace(this.getTargetId());
			Integer enemyId = spaceObject.getMarchEnemyMap().get(marchId);
			if (enemyId != null) {
				SpaceMechaEnemyCfg enemyCfg = HawkConfigManager.getInstance().getConfigByKey(SpaceMechaEnemyCfg.class, enemyId);
				builder.setModelLvl(enemyCfg.getModelLevel());
			} else {
				HawkLog.errPrintln("spaceMecha march to builder error, guildId: {}, marchId: {}", this.getTargetId(), marchId);
			}
		}
		
		if (effectList != null && !effectList.isEmpty()) {
			for (Integer eff : effectList) {
				builder.addShowEff(eff);
			}
		}
		
		if (marchType == WorldMarchType.COLLECT_RESOURCE_VALUE) {
			builder.setCollectBaseSpeed(collectBaseSpeed);
			builder.setCollectSpeed(collectSpeed);
		}
		
		DressItem marchDress = WorldPointService.getInstance().getShowDress(playerId, DressType.MARCH_DRESS_VALUE);
		if (marchDress != null) {
			long now = HawkTime.getMillisecond();
			if (marchDress.getStartTime() + marchDress.getContinueTime() > now) {
				builder.setMarchShowDress(marchDress.getModelType());
				if (marchDress.getShowEndTime() > now) {
					builder.setMarchDressShowType(marchDress.getShowType());
				}
			}
		}

		ArmourSuitType armourSuit = ArmourSuitType.valueOf(getArmourSuit());
		if (armourSuit != null) {
			builder.setArmourSuit(armourSuit);
			if (player != null) {
				try {
					builder.setArmourBrief(player.genArmourBriefInfo(armourSuit));
				} catch (Exception e) {
					HawkException.catchException(e);
				}
			}
		}
		
		builder.setIsOffensive(isOffensive());
		buildEmoticon(builder);
		if (formation != 0) {
			World.WorldFormation.Builder formationBuilder =  World.WorldFormation.newBuilder();
			formationBuilder.setIndex(formation);
			builder.setFormation(formationBuilder);
		}
		return builder;
	}
	
	/**
	 * 设置行军表情
	 * @param builder
	 */
	public void buildEmoticon(WorldMarchPB.Builder builder) {
		if (emoticonUseTime > 0) {
			builder.setEmoticonId(emoticon);
			// 去程行军或回程行军
			if (marchStatus == WorldMarchStatus.MARCH_STATUS_MARCH_VALUE || marchStatus == WorldMarchStatus.MARCH_STATUS_RETURN_BACK_VALUE) {
				if (emoticonUseTime > startTime) {
					builder.setEmoticonEndTime(endTime);
				}
			} else {  
				// 停留类行军
				long period = MarchEmoticonProperty.getInstance().getEmoticonPeriod();
				if (emoticonUseTime > reachTime && emoticonUseTime + period > HawkTime.getMillisecond()) {
					builder.setEmoticonEndTime(emoticonUseTime + period);
				}
			}			
		}
	}
	
	/**
	 * 封裝行军具体类
	 * @return
	 */
	public IWorldMarch wrapUp(){
		this.init();
		IWorldMarch march = null;
		switch (marchType) {
			case WorldMarchType.COLLECT_RESOURCE_VALUE: //采集
				march = new CollectWorldResMarch(this);
				break;
			case WorldMarchType.ATTACK_MONSTER_VALUE: //杀怪
				march = new AttackMonster(this);
				break;
			case WorldMarchType.ATTACK_PLAYER_VALUE: //攻击单人基地
				march = new AttackPlayerMarch(this);
				break;
			case WorldMarchType.ASSISTANCE_VALUE: //援助
				march = new AssistanceSingleMarch(this);
				break;
			case WorldMarchType.ARMY_QUARTERED_VALUE: //驻扎
				march = new ArmyQuarteredMarch(this);
				break;
			case WorldMarchType.SPY_VALUE: //侦察
				march = new SpyMarch(this);
				break;
			case WorldMarchType.MASS_VALUE: //集结攻打单人基地
				march = new MassSingleMarch(this);
				break;
			case WorldMarchType.MASS_JOIN_VALUE: //加入集结攻打单人基地
				march = new MassJoinSingleMarch(this);
				break;
			case WorldMarchType.ASSISTANCE_RES_VALUE: //资源援助盟友
				march = new AssistanceResMarch(this);
				break;
			case WorldMarchType.CAPTIVE_RELEASE_VALUE: // 抓将遣返
				march = new CaptiveReleaseMarch(this);
				break;
			case WorldMarchType.MANOR_SINGLE_VALUE: // 单人攻占联盟领地
				march = new ManorSingleMarch(this);
				break;
			case WorldMarchType.MANOR_MASS_VALUE: // 集结攻占联盟领地
				march = new ManorMassMarch(this);
				break;
			case WorldMarchType.MANOR_MASS_JOIN_VALUE: // 集结攻占联盟领地参与者
				march = new ManorMassJoinMarch(this);
				break;
			case WorldMarchType.MANOR_ASSISTANCE_MASS_VALUE: // 联盟领地集结援助
				march = new ManorAssistanceMassMarch(this);
				break;
			case WorldMarchType.MANOR_ASSISTANCE_MASS_JOIN_VALUE: // 联盟领地集结援助加入者
				march = new ManorAssistanceMassJoinMarch(this);
				break;
			case WorldMarchType.MANOR_COLLECT_VALUE: // 联盟超级矿采集行军类型
				march = new ManorCollectMarch(this);
				break;
			case WorldMarchType.MANOR_ASSISTANCE_VALUE: // 联盟领地单人援助
				march = new ManorAssistanceMarch(this);
				break;
			case WorldMarchType.PRESIDENT_SINGLE_VALUE: // 单人攻占总统府
				march = new PresidentSingleMarch(this);
				break;
			case WorldMarchType.PRESIDENT_MASS_VALUE: // 集结攻占总统府
				march = new PresidentMassMarch(this);
				break;
			case WorldMarchType.PRESIDENT_MASS_JOIN_VALUE: // 集结攻占总统府参与者
				march = new PresidentMassJoinMarch(this);
				break;
			case WorldMarchType.MANOR_BUILD_VALUE: // 建造联盟领地（包含建筑）
				march = new ManorBuildMarch(this);
				break;
			case WorldMarchType.MANOR_REPAIR_VALUE: // 修复联盟领地（包含建筑）
				march = new ManorRepairMarch(this);
				break;
			case WorldMarchType.WAREHOUSE_STORE_VALUE: // 去仓库存放
				march = new WarehouseStoreMarch(this);
				break;
			case WorldMarchType.WAREHOUSE_GET_VALUE: // 去仓库取回
				march = new WarehouseGetMarch(this);
				break;
			case WorldMarchType.RANDOM_BOX_VALUE: // 随机宝箱
				march = new RandomChest(this);
				break;
			case WorldMarchType.MONSTER_MASS_VALUE: // 攻打野怪集结
				march = new MassMonsterMarch(this);
				break;
			case WorldMarchType.MONSTER_MASS_JOIN_VALUE: // 攻打野怪集结加入
				march = new MassMonsterJoinMarch(this);
				break;
			case WorldMarchType.YURI_MONSTER_VALUE: // 尤里复仇怪物行军
				march = new YuriRevengeMonsterMarch(this);
				break;
			case WorldMarchType.PRESIDENT_TOWER_SINGLE_VALUE:
				march = new PresidentTowerSingleMarch(this);
				break;
			case WorldMarchType.PRESIDENT_TOWER_MASS_VALUE:
				march = new PresidentTowerMassMarch(this);
				break;
			case WorldMarchType.PRESIDENT_TOWER_MASS_JOIN_VALUE:
				march = new PresidentTowerMassJoinMarch(this);
				break;
			case WorldMarchType.STRONGPOINT_VALUE:
				march = new StrongpointMarch(this);
				break;
			case WorldMarchType.NEW_MONSTER_VALUE:
				march = new NewMonsterMarch(this);
				break;
			case WorldMarchType.FOGGY_FORTRESS_MASS_VALUE:
				march = new FoggyMassMarch(this);
				break;
			case WorldMarchType.FOGGY_FORTRESS_MASS_JOIN_VALUE:
				march = new FoggyMassJoinMarch(this);
				break;
			case WorldMarchType.HIDDEN_MARCH_VALUE:
				march = new HiddenMarch(this);
				break;
			case WorldMarchType.SUPER_WEAPON_SINGLE_VALUE:
				march = new SuperWeaponSingleMarch(this);
				break;
			case WorldMarchType.SUPER_WEAPON_MASS_VALUE:
				march = new SuperWeaponMassMarch(this);
				break;
			case WorldMarchType.SUPER_WEAPON_MASS_JOIN_VALUE:
				march  = new SuperWeaponMassJoinMarch(this);
				break;
			case WorldMarchType.XZQ_SINGLE_VALUE:
				march = new XZQSingleMarch(this);
				break;
			case WorldMarchType.XZQ_MASS_VALUE:
				march = new XZQMassMarch(this);
				break;
			case WorldMarchType.XZQ_MASS_JOIN_VALUE:
				march  = new XZQMassJoinMarch(this);
				break;
			case WorldMarchType.FOGGY_SINGLE_VALUE:
				march = new FoggySingleMarch(this);
				break;
			case WorldMarchType.YURI_STRIKE_MARCH_VALUE:
				march = new YuriStrikeMonsterMarch(this);
				break;
			case WorldMarchType.GUNDAM_SINGLE_VALUE:
				march = new GundamSingleMarch(this);
				break;
			case WorldMarchType.GUNDAM_MASS_VALUE:
				march = new GundamMassMarch(this);
				break;
			case WorldMarchType.GUNDAM_MASS_JOIN_VALUE:
				march = new GundamMassJoinMarch(this);
				break;
			case WorldMarchType.NIAN_SINGLE_VALUE:
				march = new NianSingleMarch(this);
				break;
			case WorldMarchType.NIAN_MASS_VALUE:
				march = new NianMassMarch(this);
				break;
			case WorldMarchType.NIAN_MASS_JOIN_VALUE:
				march = new NianMassJoinMarch(this);
				break;
			case WorldMarchType.TREASURE_HUNT_VALUE:
				march = new TreasureHuntMarch(this);
				break;
			case WorldMarchType.OVERLORD_BLESSING_MARCH_VALUE:
				march = new OverlordBlessingMarch(this);
				break;
			case WorldMarchType.TREASURE_HUNT_RESOURCE_VALUE:
				march = new TreasureHuntResMarch(this);
				break;
			case WorldMarchType.TREASURE_HUNT_MONSTER_MASS_VALUE:
				march = new TreasureHuntMonsterMassMarch(this);
				break;
			case WorldMarchType.TREASURE_HUNT_MONSTER_MASS_JOIN_VALUE:
				march = new TreasureHuntMonsterMassJoinMarch(this);
				break;
			case WorldMarchType.WAR_FLAG_MARCH_VALUE:
				march = new WarFlagMarch(this);
				break;
			case WorldMarchType.COLLECT_RES_TREASURE_VALUE:
				march = new CollectResTreasureMarch(this);
				break;
			case WorldMarchType.FORTRESS_SINGLE_VALUE:
				march = new FortressSingleMarch(this);
				break;
			case WorldMarchType.FORTRESS_MASS_VALUE:
				march = new FortressMassMarch(this);
				break;
			case WorldMarchType.FORTRESS_JOIN_VALUE:
				march = new FortressMassJoinMarch(this);
				break;
			case WorldMarchType.NIAN_BOX_MARCH_VALUE:
				march = new NianBoxMarch(this);
				break;
			case WorldMarchType.PYLON_MARCH_VALUE:
				march = new PylonMarch(this);
				break;
			case WorldMarchType.SNOWBALL_MARCH_VALUE:
				march = new SnowballMarch(this);
				break;
			case WorldMarchType.CHRISTMAS_SINGLE_VALUE:
				march = new ChristmasSingleMarch(this);
				break;
			case WorldMarchType.CHRISTMAS_MASS_VALUE:
				march = new ChristmasMassMarch(this);
				break;
			case WorldMarchType.CHRISTMAS_MASS_JOIN_VALUE:
				march = new ChristmasMassJoinMarch(this);
				break;
			case WorldMarchType.CHRISTMAS_BOX_MARCH_VALUE:
				march = new ChristmasBoxMarch(this);
				break;
			case WorldMarchType.ESPIONAGE_MARCH_VALUE:
				march = new EspionageMarch(this);
				break;
			case WorldMarchType.DRAGON_BOAT_MARCH_VALUE:
				march = new DragonboatMarch(this);
				break;
			case WorldMarchType.CENTER_FLAG_REWARD_MARCH_VALUE:
				march = new CenterFlagMarch(this);
				break;
				
			case WorldMarchType.WAR_FLAG_MASS_VALUE: // 战旗集结行军
				march = new WarFlagMassMarch(this);
				break;
				
			case WorldMarchType.WAR_FLAG_MASS_JOIN_VALUE: // 战旗集结加入行军
				march = new WarFlagMassJoinMarch(this);
				break;
			case WorldMarchType.GHOST_TOWER_MARCH_VALUE:
				march = new AttackGhostMarch(this);
				break;
			case WorldMarchType.CAKE_SHARE_MARCH_VALUE:	//蛋糕分享行军
				march = new CakeShareMarch(this);
				break;
			case WorldMarchType.AGENCY_MARCH_RESCUR_VALUE:
				march = new AgencyRescurMarch(this);
				break;
			case WorldMarchType.AGENCY_MARCH_MONSTER_VALUE:
				march = new AgencyMonsterMarch(this);
				break;
			case WorldMarchType.AGENCY_MARCH_COASTER_VALUE:
				march = new AgencyCoasterMarch(this);
				break;
			case WorldMarchType.NATIONAL_BUILDING_MARCH_VALUE:
				march = new NationalConstructMarch(this);
				break;
			case WorldMarchType.RESOURCE_SPREE_BOX_MARCH_VALUE:
				march = new ResourceSpreeBoxWorldMarch(this);
				break;
			case WorldMarchType.SPACE_MECHA_MAIN_MARCH_SINGLE_VALUE: // 星甲召唤主舱体守护单人行军
				march = new SpaceMechaMainSingleMarch(this);
				break;
			case WorldMarchType.SPACE_MECHA_MAIN_MARCH_MASS_VALUE: // 星甲召唤主舱体守护集结行军
				march = new SpaceMechaMainMassMarch(this);
				break;
			case WorldMarchType.SPACE_MECHA_MAIN_MARCH_MASS_JOIN_VALUE: // 星甲召唤主舱体守护集结加入行军 
				march = new SpaceMechaMainMassJoinMarch(this);
				break;
			case WorldMarchType.SPACE_MECHA_SLAVE_MARCH_SINGLE_VALUE: // 星甲召唤子舱体守护单人行军
				march = new SpaceMehaSlaveSingleMarch(this);
				break;
			case WorldMarchType.SPACE_MECHA_MONSTER_ATTACK_MARCH_VALUE: // 进攻星甲召唤舱体的野怪行军
				march = new SpaceMechaMonsterAttackMarch(this);
				break;
			case WorldMarchType.SPACE_MECHA_ATK_STRONG_HOLD_SINGLE_VALUE: // 进攻敌方据点单人行军
				march = new SpaceMechaStrongHoldSingleMarch(this);
				break;
			case WorldMarchType.SPACE_MECHA_ATK_STRONG_HOLD_MASS_VALUE: // 进攻敌方据点集结行军
				march = new SpaceMechaStrongHoldMassMarch(this);
				break;
			case WorldMarchType.SPACE_MECHA_ATK_STRONG_HOLD_MASS_JOIN_VALUE: // 进攻敌方据点集结加入行军
				march = new SpaceMechaStrongHoldMassJoinMarch(this);
				break;
			case WorldMarchType.SPACE_MECHA_BOX_COLLECT_VALUE: // 星甲召唤舱体相关的宝箱采集行军
				march = new SpaceMechaBoxMarch(this);
				break;
			case WorldMarchType.SPACE_MECHA_EMPTY_MARCH_VALUE:
				march = new SpaceMechaEmptyMarch(this);
				break;
			case WorldMarchType.DRAGON_ATTACT_MASS_VALUE:
				march = new ManorDragonTrapMassMarch(this);
				break;
			case WorldMarchType.DRAGON_ATTACT_MASS_JOIN_VALUE:
				march = new ManorDragonTrapMassJoinMarch(this);
				break;
			default:
				break;
		}
		return march;
	}
	
	public void addWorldMarchProcMask(int mask){
		//设置此对象记录内存对象即可，不再调用set方法
		this.marchProcMask = this.marchProcMask | mask;
	}
	
	/**
	 * 添加箭塔击杀信息
	 */
	public void addTowerKillInfo(Map<Integer, Integer> map){
		this.setTowerAttackInfo(JSON.toJSONString(map));
	}
	
	@Override
	public String getPrimaryKey() {
		return marchId;
	}

	@Override
	public void setPrimaryKey(String primaryKey) {
		marchId = primaryKey;
	}
	
	/**
	 * 行军上添加作用号
	 * @param effects
	 */
	public void resetEffect(List<Integer> effects) {
		this.effectList = new ArrayList<>();
		for (Integer eff : effects) {
			this.effectList.add(eff);
		}
		this.effect = SerializeHelper.collectionToString(effectList, ",");
		notifyUpdate();
	}

	public List<Integer> getEffectList() {
		return new ArrayList<>(effectList);
	}

	public int getSuperSoldierId() {
		return superSoldierId;
	}

	public void setSuperSoldierId(int superSoldierId) {
		this.superSoldierId = superSoldierId;
	}
	
	public boolean isExtraSpyMarch() {
		return extraSpyMarch;
	}

	public void setExtraSpyMarch(boolean extraSpyMarch) {
		this.extraSpyMarch = extraSpyMarch;
	}
	
	public int getAutoMarchIdentify() {
		return autoMarchIdentify;
	}

	public void setAutoMarchIdentify(int autoMarchIdentify) {
		this.autoMarchIdentify = autoMarchIdentify;
	}
	
	
	public int getAutoResourceIdentify() {
		return autoResourceIdentify;
	}

	public void setAutoResourceIdentify(int autoResourceIdentify) {
		this.autoResourceIdentify = autoResourceIdentify;
	}

	public int getArmourSuit() {
		return armourSuit;
	}

	public void setArmourSuit(int armourSuit) {
		this.armourSuit = armourSuit;
	}

	public int getTalentType() {
		return talentType;
	}

	public void setTalentType(int talentType) {
		this.talentType = talentType;
	}

	public int getSuperLab() {
		return superLab;
	}

	public void setSuperLab(int superLab) {
		this.superLab = superLab;
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
	
	public int getTreasureCount() {
		return treasureCount;
	}
	
	public void setTreasureCount(int treasureCount) {
		this.treasureCount = treasureCount;
	}

	/**
	 * 行军参数 计算作用号用
	 */
	public EffectParams getEffectParams() {
		if (effParams != null) {
			return effParams;
		}
		
		EffectParams effParams = new EffectParams();
		effParams.setArmys(getArmyCopy());
		effParams.setHeroIds(getHeroIdList());
		effParams.setSuperSoliderId(getSuperSoldierId());
		effParams.setArmourSuit(ArmourSuitType.valueOf(armourSuit));
		effParams.setMechacoreSuit(MechaCoreSuitType.valueOf(mechacoreSuit));
		effParams.setTalent(talentType);
		effParams.setSuperLab(superLab);
		effParams.setDressList(getDressList());
		effParams.setMarch(this);
		effParams.setManhattanAdkSwId(manhattanAtkSwId);
		effParams.setManhattanDefSwId(manhattanDefSwId);
		this.effParams = effParams;
		
		return this.effParams;
	}
	
	public void resetEffectParams() {
		EffectParams effParams = new EffectParams();
		effParams.setArmys(getArmyCopy());
		effParams.setHeroIds(getHeroIdList());
		effParams.setSuperSoliderId(getSuperSoldierId());
		effParams.setArmourSuit(ArmourSuitType.valueOf(armourSuit));
		effParams.setMechacoreSuit(MechaCoreSuitType.valueOf(mechacoreSuit));
		effParams.setTalent(talentType);
		effParams.setSuperLab(superLab);
		effParams.setDressList(getDressList());
		effParams.setManhattanAdkSwId(manhattanAtkSwId);
		effParams.setManhattanDefSwId(manhattanDefSwId);
		this.effParams = effParams;
	}

	public Set<String> getReportSend() {
		if(Objects.isNull(reportSend)){
			reportSend = new CopyOnWriteArraySet<>();
		}
		return reportSend;
	}

	public int getFormation() {
		return formation;
	}

	public void setFormation(int formation) {
		this.formation = formation;
	}
	
	public String getExtraInfo() {
		return extraInfo;
	}

	public void setExtraInfo(String extraInfo) {
		this.extraInfo = extraInfo;
	}
	
	public int getMechacoreSuit() {
		return mechacoreSuit;
	}

	public void setMechacoreSuit(int mechacoreSuit) {
		if (mechacoreSuit <= 0) {
			mechacoreSuit = MechaCoreSuitType.MECHA_ONE_VALUE;
		}
		this.mechacoreSuit = mechacoreSuit;
	}

	public void setEffParams(EffectParams effParams) {
		this.effParams = effParams;
	}

	public int getManhattanAtkSwId() {
		return manhattanAtkSwId;
	}

	public void setManhattanAtkSwId(int manhattanAtkSwId) {
		this.manhattanAtkSwId = manhattanAtkSwId;
	}

	public int getManhattanDefSwId() {
		return manhattanDefSwId;
	}

	public void setManhattanDefSwId(int manhattanDefSwId) {
		this.manhattanDefSwId = manhattanDefSwId;
	}
	
	
	public int getAutoMassJoinIdentify() {
		return autoMassJoinIdentify;
	}
				
	public void setAutoMassJoinIdentify(int autoMassJoinIdentify) {
		this.autoMassJoinIdentify = autoMassJoinIdentify;
	}
}
