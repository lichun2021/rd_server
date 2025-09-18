package com.hawk.game.player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import com.hawk.game.module.*;
import com.hawk.game.module.homeland.PlayerHomeLandModule;
import com.hawk.game.module.homeland.cfg.HomeLandConstKVCfg;
import com.hawk.game.protocol.*;
import org.apache.commons.lang.StringUtils;
import org.hawk.annotation.MessageHandler;
import org.hawk.app.HawkApp;
import org.hawk.app.HawkAppObj;
import org.hawk.app.HawkObjModule;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBManager;
import org.hawk.db.serializer.HawkEntitySerializer;
import org.hawk.delay.HawkDelayAction;
import org.hawk.log.HawkLog;
import org.hawk.msg.HawkMsg;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.net.session.HawkSession;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkRand;
import org.hawk.os.HawkTime;
import org.hawk.profiler.HawkProfilerAnalyzer;
import org.hawk.task.HawkTaskManager;
import org.hawk.tuple.HawkTuple2;
import org.hawk.uuid.HawkUUIDGenerator;
import org.hawk.xid.HawkXID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Table;
import com.google.protobuf.ProtocolMessageEnum;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.event.impl.CityResourceCollectEvent;
import com.hawk.activity.event.impl.ConsumeMoneyEvent;
import com.hawk.activity.event.impl.GivenItemCostEvent;
import com.hawk.activity.event.impl.GrowUpBoostItemConsumeEvent;
import com.hawk.activity.event.impl.JoinGuildEvent;
import com.hawk.activity.event.impl.PlayerLevelUpEvent;
import com.hawk.activity.event.impl.VipLevelupEvent;
import com.hawk.activity.event.impl.VitreceiveConsumeEvent;
import com.hawk.activity.helper.PlayerActivityData;
import com.hawk.activity.helper.PlayerActivityDataSerialize;
import com.hawk.activity.helper.PlayerDataHelper;
import com.hawk.activity.type.impl.growUpBoost.GrowUpBoostActivity;
import com.hawk.activity.type.impl.growUpBoost.cfg.GrowUpBoostScoreCfg;
import com.hawk.activity.type.impl.monthcard.entity.ActivityMonthCardEntity;
import com.hawk.game.GsApp;
import com.hawk.game.GsConfig;
import com.hawk.game.activity.ActivityModule;
import com.hawk.game.activity.activityWeek.ActivityWeekCalenderModule;
import com.hawk.game.activity.impl.yurirevenge.PlayerYuriRevengeModule;
import com.hawk.game.cfgElement.ArmourEffObject;
import com.hawk.game.config.ArmourAdditionalCfg;
import com.hawk.game.config.ArmourBreakthroughCfg;
import com.hawk.game.config.ArmourCfg;
import com.hawk.game.config.ArmourPoolCfg;
import com.hawk.game.config.BattleSoldierCfg;
import com.hawk.game.config.BuildAreaCfg;
import com.hawk.game.config.BuildingCfg;
import com.hawk.game.config.CommanderConstProperty;
import com.hawk.game.config.ConstProperty;
import com.hawk.game.config.CustomKeyCfg;
import com.hawk.game.config.GameConstCfg;
import com.hawk.game.config.GuildConstProperty;
import com.hawk.game.config.ItemCfg;
import com.hawk.game.config.PayGiftCfg;
import com.hawk.game.config.PlayerLevelExpCfg;
import com.hawk.game.config.ProtoCheckConfig;
import com.hawk.game.config.SysControlProperty;
import com.hawk.game.config.VipCfg;
import com.hawk.game.config.VipSuperCfg;
import com.hawk.game.config.WorldMapConstProperty;
import com.hawk.game.config.WorldMarchConstProperty;
import com.hawk.game.crossactivity.CrossActivityService;
import com.hawk.game.crossproxy.CrossService;
import com.hawk.game.crossproxy.model.CsPlayer;
import com.hawk.game.data.ProtectSoldierInfo;
import com.hawk.game.data.RevengeInfo;
import com.hawk.game.data.RevengeSoldierInfo;
import com.hawk.game.data.TimeLimitStoreConditionInfo;
import com.hawk.game.entity.ArmourEntity;
import com.hawk.game.entity.ArmyEntity;
import com.hawk.game.entity.BuildingBaseEntity;
import com.hawk.game.entity.CommanderEntity;
import com.hawk.game.entity.CustomDataEntity;
import com.hawk.game.entity.DailyDataEntity;
import com.hawk.game.entity.GuildInfoObject;
import com.hawk.game.entity.GuildMemberObject;
import com.hawk.game.entity.HeroEntity;
import com.hawk.game.entity.ItemEntity;
import com.hawk.game.entity.ManhattanEntity;
import com.hawk.game.entity.MoneyReissueEntity;
import com.hawk.game.entity.PlantTechEntity;
import com.hawk.game.entity.PlayerBaseEntity;
import com.hawk.game.entity.PlayerEntity;
import com.hawk.game.entity.PlayerGiftEntity;
import com.hawk.game.entity.PlayerRelationEntity;
import com.hawk.game.entity.PlayerShopEntity;
import com.hawk.game.entity.QueueEntity;
import com.hawk.game.entity.StatisticsEntity;
import com.hawk.game.entity.StatusDataEntity;
import com.hawk.game.entity.SuperSoldierEntity;
import com.hawk.game.entity.YuriStrikeEntity;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.LocalRedis;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.guild.guildrank.GuildRankMgr;
import com.hawk.game.invoker.PlayerQuitGuildInvoker;
import com.hawk.game.invoker.UnbanPersonalRankMsgInvoker;
import com.hawk.game.invoker.UpdateRankScoreMsgInvoker;
import com.hawk.game.item.ArmourAttrTemplate;
import com.hawk.game.item.AwardItems;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.lianmengcyb.CYBORGConst.CYBORGState;
import com.hawk.game.lianmengcyb.CYBORGRoomManager;
import com.hawk.game.lianmengcyb.player.module.CYBORGPlayerModule;
import com.hawk.game.lianmengjunyan.LMJYConst.PState;
import com.hawk.game.lianmengjunyan.LMJYRoomManager;
import com.hawk.game.lianmengjunyan.player.module.LMJYPlayerModule;
import com.hawk.game.lianmengstarwars.SWConst.SWState;
import com.hawk.game.lianmengstarwars.SWRoomManager;
import com.hawk.game.lianmengstarwars.player.module.SWPlayerModule;
import com.hawk.game.lianmengxzq.PlayerXZQModule;
import com.hawk.game.log.BehaviorLogger;
import com.hawk.game.log.BehaviorLogger.Params;
import com.hawk.game.manager.AssembleDataManager;
import com.hawk.game.module.agency.PlayerAgencyModule;
import com.hawk.game.module.autologic.PlayerAutoMassJoinModule;
import com.hawk.game.module.autologic.PlayerAutoModule;
import com.hawk.game.module.college.PlayerCollegeModule;
import com.hawk.game.module.crossTalent.CrossTalentModule;
import com.hawk.game.module.dayazhizhan.battleroom.DYZZConst.DYZZState;
import com.hawk.game.module.dayazhizhan.battleroom.DYZZRoomManager;
import com.hawk.game.module.dayazhizhan.battleroom.player.module.DYZZPlayerModule;
import com.hawk.game.module.dayazhizhan.playerteam.module.PlayerDYZZModule;
import com.hawk.game.module.dayazhizhan.playerteam.module.PlayerDYZZSeasonModule;
import com.hawk.game.module.guildTeam.PlayerGuildTeamModule;
import com.hawk.game.module.hospice.PlayerHospiceModule;
import com.hawk.game.module.lianmengXianquhx.XQHXConst.XQHXState;
import com.hawk.game.module.lianmengXianquhx.XQHXRoomManager;
import com.hawk.game.module.lianmengXianquhx.player.module.XQHXPlayerModule;
import com.hawk.game.module.lianmengfgyl.battleroom.FGYLConst.FGYLState;
import com.hawk.game.module.lianmengfgyl.battleroom.FGYLRoomManager;
import com.hawk.game.module.lianmengfgyl.battleroom.player.module.FGYLPlayerModule;
import com.hawk.game.module.lianmengfgyl.march.module.PlayerFGYLWarModule;
import com.hawk.game.module.lianmengtaiboliya.TBLYConst.TBLYState;
import com.hawk.game.module.lianmengtaiboliya.TBLYRoomManager;
import com.hawk.game.module.lianmengtaiboliya.player.module.TBLYPlayerModule;
import com.hawk.game.module.lianmengyqzz.battleroom.YQZZConst.YQZZState;
import com.hawk.game.module.lianmengyqzz.battleroom.YQZZRoomManager;
import com.hawk.game.module.lianmengyqzz.battleroom.player.module.YQZZPlayerModule;
import com.hawk.game.module.lianmengyqzz.march.entitiy.PlayerYQZZData;
import com.hawk.game.module.lianmengyqzz.march.module.PlayerYQZZModule;
import com.hawk.game.module.lianmenxhjz.battleroom.XHJZConst.XHJZState;
import com.hawk.game.module.lianmenxhjz.battleroom.XHJZRoomManager;
import com.hawk.game.module.lianmenxhjz.battleroom.player.module.XHJZPlayerModule;
import com.hawk.game.module.material.PlayerMeterialModule;
import com.hawk.game.module.mechacore.PlayerMechaCore;
import com.hawk.game.module.mechacore.PlayerMechacoreModule;
import com.hawk.game.module.mechacore.cfg.MechaCoreConstCfg;
import com.hawk.game.module.mechacore.entity.MechaCoreModuleEntity;
import com.hawk.game.module.nation.PlayerNationContructionModule;
import com.hawk.game.module.nation.PlayerNationMissionModule;
import com.hawk.game.module.nation.PlayerNationShipFactoryModule;
import com.hawk.game.module.nation.PlayerNationTechModule;
import com.hawk.game.module.nationMilitary.PlayerNationMilitaryModule;
import com.hawk.game.module.nationMilitary.entity.NationMilitaryEntity;
import com.hawk.game.module.nationMilitary.rank.NationMilitaryRankObj;
import com.hawk.game.module.obelisk.PlayerObeliskModule;
import com.hawk.game.module.plantfactory.PlayerPlantFactoryModule;
import com.hawk.game.module.plantfactory.PlayerPlantTechModule;
import com.hawk.game.module.plantsoldier.advance.PlantSoldierAdvanceModule;
import com.hawk.game.module.plantsoldier.science.PlantScience;
import com.hawk.game.module.plantsoldier.science.PlayerPlantScienceModule;
import com.hawk.game.module.plantsoldier.strengthen.PlantSoldierSchool;
import com.hawk.game.module.plantsoldier.strengthen.PlayerPlantSoldierSchoolModule;
import com.hawk.game.module.plantsoldier.strengthen.plantMilitary.PlantSoldierMilitary;
import com.hawk.game.module.plantsoldier.strengthen.plantMilitary.cfg.PlantSoldierMilitaryCfg;
import com.hawk.game.module.plantsoldier.strengthen.soldierStrengthen.SoldierStrengthen;
import com.hawk.game.module.schedule.PlayerScheduleModule;
import com.hawk.game.module.soldierExchange.PlayerSoldierExchangeModule;
import com.hawk.game.module.spacemecha.module.PlayerSpaceMechaModule;
import com.hawk.game.module.staffofficer.PlayerStaffOfficerModule;
import com.hawk.game.module.staffofficer.StaffOfficerSkillCollection;
import com.hawk.game.module.toucai.PlayerMedalFactoryModule;
import com.hawk.game.msg.CommanderExpAddMsg;
import com.hawk.game.msg.CommanderLevlUpMsg;
import com.hawk.game.msg.DailyDataClearMsg;
import com.hawk.game.msg.HeroItemChangedMsg;
import com.hawk.game.msg.PlayerAssembleMsg;
import com.hawk.game.msg.PlayerLoginMsg;
import com.hawk.game.msg.PlayerUnlockImageMsg;
import com.hawk.game.msg.PlayerUnlockImageMsg.UnlockType;
import com.hawk.game.msg.RefreshEffectMsg;
import com.hawk.game.msg.RemoveArmourMsg;
import com.hawk.game.msg.SessionClosedMsg;
import com.hawk.game.msg.UseItemMsg;
import com.hawk.game.nation.hospital.module.NationalHospitalModule;
import com.hawk.game.nation.hospital.module.TszzNationalHospitalModule;
import com.hawk.game.nation.wearhouse.module.NationalWarehouseModule;
import com.hawk.game.player.hero.PlayerHero;
import com.hawk.game.player.itemadd.ItemAddLogicEnum;
import com.hawk.game.player.laboratory.PlayerLaboratoryModule;
import com.hawk.game.player.manhattan.PlayerManhattan;
import com.hawk.game.player.manhattan.PlayerManhattanModule;
import com.hawk.game.player.midas.MidasService;
import com.hawk.game.player.roleexchange.RoleExchangeService;
import com.hawk.game.player.skill.talent.ITalentSkill;
import com.hawk.game.player.skill.talent.TalentSkillContext;
import com.hawk.game.player.strength.PlayerStrengthFactory;
import com.hawk.game.player.supersoldier.PlayerSuperSoldierModule;
import com.hawk.game.player.supersoldier.SuperSoldier;
import com.hawk.game.player.tick.PlayerTickEnum;
import com.hawk.game.player.tick.PlayerTickTimeLine;
import com.hawk.game.player.vipsuper.PlayerVipSuper;
import com.hawk.game.protocol.World.PresetMarchManhattan;
import com.hawk.game.protocol.Anchor;
import com.hawk.game.protocol.Armour.ArmourBriefInfo;
import com.hawk.game.protocol.Armour.ArmourSuitType;
import com.hawk.game.protocol.Building.PushLastResCollectTime;
import com.hawk.game.protocol.Building.ResBuildingInfo;
import com.hawk.game.protocol.Chat.HPChatState;
import com.hawk.game.protocol.Common.SynSystemEvent;
import com.hawk.game.protocol.Common.SystemEvent;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.Const.BuildingType;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.ItemType;
import com.hawk.game.protocol.Const.LimitType;
import com.hawk.game.protocol.Const.MailRewardStatus;
import com.hawk.game.protocol.Const.PlayerAttr;
import com.hawk.game.protocol.Const.SoldierType;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.IDIP.IDIPNotice;
import com.hawk.game.protocol.IDIP.NoticeMode;
import com.hawk.game.protocol.IDIP.NoticeType;
import com.hawk.game.protocol.Login.HPLogin;
import com.hawk.game.protocol.Login.HPPlayerKickout;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.Manhattan.PBDeployedSwInfo;
import com.hawk.game.protocol.Manhattan.PBManhattanInfo;
import com.hawk.game.protocol.MechaCore.MechaCoreSuitType;
import com.hawk.game.protocol.MechaCore.PBMechaCoreInfo;
import com.hawk.game.protocol.National.NationRDInfo;
import com.hawk.game.protocol.National.NationRDInfoList;
import com.hawk.game.protocol.National.NationRedDot;
import com.hawk.game.protocol.Player.HealthGameRemindPB;
import com.hawk.game.protocol.Player.LoginWay;
import com.hawk.game.protocol.Player.PLAYER_STATUS_SYN;
import com.hawk.game.protocol.Player.PlayerStatus;
import com.hawk.game.protocol.Player.ReportType;
import com.hawk.game.protocol.Player.ZKType;
import com.hawk.game.protocol.Rank.RankType;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.Status.IdipMsgCode;
import com.hawk.game.protocol.SysProtocol.HPErrorCode;
import com.hawk.game.protocol.SysProtocol.HPOperateSuccess;
import com.hawk.game.protocol.YuriStrike.YuriState;
import com.hawk.game.rank.RankService;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.MissionService;
import com.hawk.game.service.PlayerImageService;
import com.hawk.game.service.PlotBattleService;
import com.hawk.game.service.RelationService;
import com.hawk.game.service.chat.ChatService;
import com.hawk.game.service.mail.GuildMailService;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mail.SystemMailService;
import com.hawk.game.service.mssion.MissionManager;
import com.hawk.game.service.mssion.event.EventConsumMoney;
import com.hawk.game.service.mssion.event.EventGuildJoin;
import com.hawk.game.service.mssion.event.EventGuildMemberChange;
import com.hawk.game.service.mssion.event.EventGuildScore;
import com.hawk.game.service.mssion.event.EventPlayerUpLevel;
import com.hawk.game.service.mssion.event.EventResourceProduction;
import com.hawk.game.service.pushgift.PushGiftConditionEnum;
import com.hawk.game.strengthenguide.StrengthenGuideManager;
import com.hawk.game.strengthenguide.msg.SGCommanderLevlUpMsg;
import com.hawk.game.strengthenguide.msg.SGPlayerLoginMsg;
import com.hawk.game.strengthenguide.msg.SGPlayerVipLevelUpMsg;
import com.hawk.game.util.BuilderUtil;
import com.hawk.game.util.ConfigUtil;
import com.hawk.game.util.EffectParams;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.GsConst.DiamondPresentReason;
import com.hawk.game.util.GsConst.IDIPBanType;
import com.hawk.game.util.GsConst.IDIPDailyStatisType;
import com.hawk.game.util.GsConst.MissionFunType;
import com.hawk.game.util.GsConst.ModuleType;
import com.hawk.game.util.GsConst.ScoreType;
import com.hawk.game.util.GsConst.VipRelatedDateType;
import com.hawk.game.util.LogUtil;
import com.hawk.game.util.MapUtil;
import com.hawk.game.util.ProtoUtil;
import com.hawk.game.util.RandomUtil;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.march.IWorldMarch;
import com.hawk.game.world.march.submarch.ChristmasMarch;
import com.hawk.game.world.service.WorldChristmasWarService;
import com.hawk.game.world.service.WorldPlayerService;
import com.hawk.game.world.service.WorldPointService;
import com.hawk.game.yuriStrikes.PlayerYuristrikeModule;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.health.entity.UpdateUserInfoResult;
import com.hawk.log.Action;
import com.hawk.log.LogConst.AddOrReduce;
import com.hawk.log.LogConst.IMoneyType;
import com.hawk.log.LogConst.LogInfoType;
import com.hawk.log.LogConst.NetWork;
import com.hawk.log.LogConst.PowerChangeReason;
import com.hawk.log.LogConst.TelecomOper;
import com.hawk.log.Source;
import com.hawk.sdk.SDKConst;
import com.hawk.sdk.SDKConst.HealthGameFlag;
import com.hawk.sdk.SDKManager;
import com.hawk.sdk.config.HealthCfg;
import com.hawk.sdk.msdk.entity.PayItemInfo;

/**
 * 玩家对象
 *
 * @author hawk
 *
 */
public class Player extends HawkAppObj {
	/**
	 * 协议日志记录器
	 */
	public static final Logger logger = LoggerFactory.getLogger("Server");

	/** 虎牢关战场id */
	private String lmjyRoomId;
	/** 虎牢关 1 准备进入 2 在战场中 */
	private PState lmjyState;
	public long lmjyCD;
	public int lmjyQuitCnt;

	/** 锦标赛关战场id */
	private String tblyRoomId;
	/** 锦标赛 1 准备进入 2 在战场中 */
	private TBLYState tblyState;
	private XHJZState xhjzState;
	private String xhjzRoomId;

	private String swRoomId;
	private SWState swState;

	private String cyborgRoomId;
	private CYBORGState cyborgState;
	
	private String dyzzRoomId;
	private DYZZState dyzzState;

	private String yqzzRoomId;
	private YQZZState yqzzState;
	
	private String fgylRoomId;
	private FGYLState fgylState;
	
	private String xqhxRoomId;
	private XQHXState xqhxState;
	
	/**
	 * 挂载玩家数据管理集合
	 */
	private PlayerData playerData;
	/**
	 * 同步推送组件
	 */
	private PlayerPush playerPush;
	/**
	 * 客户端ip
	 */
	private String sessionIp;
	/**
	 * 激活状态
	 */
	private int activeState = 0;
	/**
	 * 首次登录标志:1-新用户首次登录，0-非首次登录
	 */
	private int firstLogin = 0;
	
	/**
	 * player tick计时，以及其它计时器的集中统一管理
	 */
	private PlayerTickTimeLine tickTimeObject = new PlayerTickTimeLine();
	
	/**
	 * 世界aoi对象的id
	 */
	private int aoiObjId = 0;
	/**
	 * 切入后台的时间
	 */
	private long background;
	/**
	 * 平台信息json
	 */
	private JSONObject pfTokenJson = null;
	/**
	 * 是否处于零收益状态
	 */
	public volatile boolean zeroEarningState = false;
	/**
	 * 是否调用idip接口改变钻石数量
	 */
	private boolean diamondsChange = false;
	/**
	 * 玩家设备信息
	 */
	private JSONObject phoneInfo = null;
	/**
	 * 玩家主城等级
	 */
	private int cityLevel = 0;
	/**
	 * 玩家应用ID
	 */
	private String gameAppId = null;
	/**
	 * 玩家平台ID
	 */
	private int platId = -1;
	/**
	 * 玩家渠道ID
	 */
	private String channelId = null;
	/**
	 * 玩家处于锁定状态,不可进行任何操作协议
	 * 本来玩家应该只有一个状态字段但是原来的boolean无法拓展.
	 */
	private boolean isLocker;
	
	/**
	 * 是否是成年人
	 */
	private boolean adult;

	private HPChatState chatState;

	/**
	 * 登录方式
	 */
	private LoginWay loginWay;

	/**
	 *  上一次上报战力时的战力值
	 */
	private long lastPowerScore;
	/**
	 * 同步锁
	 */
	private Object syncObj = new Object();

	/**
	 * 玩家被攻击时间戳
	 */
	private long beAttacked;
	/**
	 * 保存客户端的登录信息,给跨服的时候用.
	 */
	private HPLogin.Builder hpLogin;
	/**
	 * 跨服状态标志位
	 */
	private int crossStatus;

	/**
	 * 新兵救援信息
	 */
	private ProtectSoldierInfo protectSoldierInfo;
	/**
	 * 大R复仇信息
	 */
	private RevengeInfo revengeInfo;
	/**
	 * 大R复仇损失兵力信息
	 */
	private List<RevengeSoldierInfo> lossTroopInfoList;
	/**
	 * 大R复仇商店购买信息
	 */
	private Map<Integer, Integer> revengeShopBuyInfo;
	/**
	 * 攻打年兽次数
	 * {@link ChristmasMarch} 圣诞行军也用了这个清注意.
	 */
	private Map<String, Integer> atkNianTimesMap = new HashMap<>();

	/**
	 * 限时商店条件达成信息
	 */
	private Map<Integer, TimeLimitStoreConditionInfo> timeLimitStoreCondition;
	/**
	 * 正在出售中的商品库
	 */
	private TimeLimitStoreConditionInfo onSellStoreCondition;
	/**
	 * 贵族商城是否有红点标识
	 */
	private boolean vipShopRedPoint;
	/**
	 * 二级密码
	 */
	private String playerSecPasswd;
	/** 
	 * ip归属地信息  
	 */
	private String ipBelongsAddr = "";
	
	/**
	 * 铠甲套装
	 */
	private Table<Integer, Integer, String> armourSuit;
	/**
	 *  暂时用于存储圣诞boss每波捡取的宝箱
	 */
	private Map<String, Integer> memoryData = new HashMap<>();
	/**上一次推送的联盟帮助数量*/
	public int lastRefreshGuildHelpNum = -1;
	/** 上一次铁头触发了立即冷却*/
	public boolean lastBuff1551Trigered;
	/**缓兵之计开启时，在此技能持续期间，我方每阵亡和受伤士兵*/
	public int skill10303DurningDead;
	/** 买量需要上报的信息  */
	public String oaidOrCaid;
	public String useragent;
	public String idfa;
	public int gamematrix;
	
	/** 国家相关红点信息 */
	private Map<NationRedDot, Integer> nationRdMap = new HashMap<>();
	
	/** 国家相关红点标记 */
	private String nationMapMark = "";
	
	/** 玩家至尊会员信息  */
	private PlayerVipSuper vipSuperObject;
	
	/**
	 * 赠送时间限制道具
	 */
	private Map<Integer, Long> sendTimeLimitTool = new HashMap<>();
	
	/**
	 * 协议计数
	 */
	private Map<Integer, LoadingCache<Integer, AtomicInteger>> protoCounter;
	
	/**
	 * 协议封禁截至时间
	 */
	private Map<Integer, Long> protoBan;

	/**
	 * 心悦角色交易状态（为0表示默认非）
	 */
	private int roleExchangeState = 0;
	
	/**
	 * 联盟编队信息变更标记（用于对GuildFormationModule中ontick一秒查一次redis的情况进行优化）
	 */
	private AtomicInteger guildFormationChangeMark = new AtomicInteger(0);
	
	/**
	 * 构造函数
	 *
	 * @param xid
	 */
	public Player(HawkXID xid) {
		super(xid);
		initModules();
		activeState = GsConst.PlayerState.OFFLINE;
		vipSuperObject = new PlayerVipSuper(this);
		protoCounter = new HashMap<>();
		protoBan = new HashMap<>();
	}

	/**
	 * 初始化模块
	 *
	 */
	public void initModules() {
		// 登录模块
		registerModule(GsConst.ModuleType.LOGIN_MODULE, new PlayerLoginModule(this));
		// 建筑模块
		registerModule(GsConst.ModuleType.BUILDING_MODULE, new PlayerBuildingModule(this));
		// 物品模块
		registerModule(GsConst.ModuleType.ITEM_MODULE, new PlayerItemModule(this));
		// 礼包模块（超值礼包、vip贵族礼包）
		registerModule(GsConst.ModuleType.GIFT_MOUDLE, new PlayerGiftModule(this));
		// 天赋模块
		registerModule(GsConst.ModuleType.TALENT_MODULE, new PlayerTalentModule(this));
		// 科技模块
		registerModule(GsConst.ModuleType.TECHNOLOGY_MODULE, new PlayerTechnologyModule(this));
		// 远征科技模块
		registerModule(GsConst.ModuleType.CROSS_TECH_MODULE, new PlayerCrossTechModule(this));
		// 军队模块
		registerModule(GsConst.ModuleType.ARMY_MODULE, new PlayerArmyModule(this));
		// 角色功能操作模块
		registerModule(GsConst.ModuleType.OPERATION_MODULE, new PlayerOperationModule(this));
		// 聊天模块
		registerModule(GsConst.ModuleType.CHAT_MODULE, new PlayerChatModule(this));
		// 角色联盟信息
		registerModule(GsConst.ModuleType.GUILD_MODULE, new PlayerGuildModule(this));
		registerModule(GsConst.ModuleType.STORE_HOUSE, new PlayerStorehouseModule(this));
		registerModule(GsConst.ModuleType.WARE_HOUSE, new PlayerManorWarehouseModule(this));
		// 英雄
		registerModule(GsConst.ModuleType.HERO, new PlayerHeroModule(this));
		registerModule(GsConst.ModuleType.BINGZHONGZHUANHUAN, new PlayerSoldierExchangeModule(this));
		registerModule(GsConst.ModuleType.LABRATORY, new PlayerLaboratoryModule(this));
		registerModule(GsConst.ModuleType.SUPER_SOLDIER, new PlayerSuperSoldierModule(this));
		registerModule(GsConst.ModuleType.GUILD_BIG_GIFT, new PlayerGuildBigGiftModule(this));
		registerModule(GsConst.ModuleType.YURI_STRIKE, new PlayerYuristrikeModule(this));
		registerModule(GsConst.ModuleType.GUILD_HOSPICE, new PlayerHospiceModule(this));
		registerModule(GsConst.ModuleType.LMJY_MODULE, new LMJYPlayerModule(this));
		registerModule(GsConst.ModuleType.TBLY_MODULE, new TBLYPlayerModule(this));
		registerModule(GsConst.ModuleType.CYBORG_MODULE, new CYBORGPlayerModule(this));
		registerModule(GsConst.ModuleType.SW_MODULE, new SWPlayerModule(this));
		registerModule(GsConst.ModuleType.DYZZ_MODULE, new DYZZPlayerModule(this));
		registerModule(GsConst.ModuleType.YQZZ_MODULE, new YQZZPlayerModule(this));
//		registerModule(GsConst.ModuleType.SUPER_LAB, new PlayerSuperLabModule(this));
		// 邮件模块
		registerModule(GsConst.ModuleType.MAIL_MODULE, new PlayerMailModule(this));
		// 世界模块
		registerModule(GsConst.ModuleType.WORLD_MODULE, new PlayerWorldModule(this));
		// 排行模块
		registerModule(GsConst.ModuleType.RANK_MODULE, new PlayerRankModule(this));
		// 任务模块
		registerModule(GsConst.ModuleType.MISSION_MODULE, new PlayerMissionModule(this));
		// 世界行军模块
		registerModule(GsConst.ModuleType.WORLD_MARCH_MODULE, new PlayerMarchModule(this));
		// 充值模块
		registerModule(GsConst.ModuleType.RECHARGE_MODULE, new PlayerRechargeModule(this));
		// GM模块
		registerModule(GsConst.ModuleType.GM_MODULE, new PlayerGmModule(this));
		// 装备系统模块
		registerModule(GsConst.ModuleType.EQUIP_MODULE, new PlayerEquipModule(this));
		// 联盟领地模块
		registerModule(GsConst.ModuleType.GUILD_MANOR_MODULE, new PlayerGuildManorModule(this));
		// 联盟反击
		registerModule(GsConst.ModuleType.GUILD_COUNTER, new PlayerGuildCounterModule(this));
		// 奖励模块
		registerModule(GsConst.ModuleType.REWARD_MODULE, new PlayerRewardModule(this));
		// 活动模块
		registerModule(GsConst.ModuleType.ACTIVITY_MODULE, new ActivityModule(this));
		// 新手模块
		registerModule(GsConst.ModuleType.NEWLY_MODULE, new PlayerNewlyModule(this));
		// 国王战模块
		registerModule(GsConst.ModuleType.PRESIDENT_MODULE, new PlayerPresidentModule(this));
		// 腾讯安全SDK模块
		registerModule(GsConst.ModuleType.TESS_SDK, new PlayerTssSdkModule(this));
		// 初始化问卷调查模块
		registerModule(GsConst.ModuleType.QUESTIONNAIRE, new PlayerQuestionnaireModule(this));
		// 许愿池
		registerModule(GsConst.ModuleType.WISHING, new PlayerWishingModule(this));
		// 码头
		registerModule(GsConst.ModuleType.WHARF, new PlayerWharfModule(this));
		// 酒馆
		registerModule(GsConst.ModuleType.TAVERN, new PlayerTavernModule(this));
		// 剧情任务模块
		registerModule(GsConst.ModuleType.STORY_MISSSION, new PlayerStoryMissionModule(this));
		// 初始化配置检测模块
		registerModule(GsConst.ModuleType.CONFIG_CHECK, new ClientCommonModule(this));
		// 队列模块
		registerModule(GsConst.ModuleType.QUEUE_MODULE, new PlayerQueueModule(this));
		// 旅行商人
		registerModule(GsConst.ModuleType.TRAVEL_SHOP, new PlayerTravelShopModule(this));
		// 尤里复仇模块
		registerModule(GsConst.ModuleType.YURI_REVENGE, new PlayerYuriRevengeModule(this));
		// 好友系统
		registerModule(GsConst.ModuleType.RELATION, new PlayerRelationModule(this));
		// 玩家定时事件系统
		registerModule(GsConst.ModuleType.TIMER, new PlayerTimerModule(this));
		// 剧情战役
		registerModule(GsConst.ModuleType.PLOT_BATTLE, new PlayerPlotBattleModule(this));
		// 装扮
		registerModule(GsConst.ModuleType.PLAYER_DRESS, new PlayerDressModule(this));
		// 推送礼包
		registerModule(GsConst.ModuleType.PUSH_GIFT, new PlayerPushGiftModule(this));
		// 军衔
		registerModule(GsConst.ModuleType.MILITARY_RANK, new PlayerMilitaryRankModule(this));
		// 超级武器战模块
		registerModule(GsConst.ModuleType.SUPER_WEAPON, new PlayerSuperWeaponModule(this));
		// 累积在线
		registerModule(GsConst.ModuleType.ACCUMULATE_ONLINE, new PlayerAccumulateOnlineModule(this));
		// 线上紧急逻辑处理模块
		registerModule(GsConst.ModuleType.URGENCY_LOGIC, new PlayerUrgencyModule(this));
		registerModule(GsConst.ModuleType.PLAYER_IMAGE, new PlayerImageModule(this));
		// 幽灵来袭
		registerModule(GsConst.ModuleType.GHOST_STRIKE, new GhostStrikeModule(this));
		// 玩家成就
		registerModule(GsConst.ModuleType.PLAYER_ACHIEVE, new PlayerAchieveModule(this));
		// 战争学院
		registerModule(GsConst.ModuleType.WAR_COLLEGE, new PlayerWarCollegeModule(this));
		// 我要变强
		registerModule(GsConst.ModuleType.STRENGTHEN_GUIDE, new PlayerStrengthenGuideModule(this));
		// 分享模块
		registerModule(GsConst.ModuleType.SHARE_MODULE, new PlayerShareModule(this));
		// 跨服模块
		registerModule(GsConst.ModuleType.CROSS_SERVER, new PlayerCSModule(this));
		// 跨服活动模块
		registerModule(GsConst.ModuleType.CROSS_ACTIVITY, new PlayerCrossActivityModule(this));
		// 红包模块
		registerModule(GsConst.ModuleType.RED_ENVELOPE_MODULE, new PlayerRedPacketModule(this));
		// 战地之王
		registerModule(GsConst.ModuleType.WAR_FLAG, new PlayerWarFlagModule(this));
		// 军事学院
		registerModule(GsConst.ModuleType.MILITARY_COLLEGE, new PlayerCollegeModule(this));
		// 泰伯利亚之战
		registerModule(GsConst.ModuleType.TIBERIUM_WAR, new PlayerTiberiumModule(this));
		// 装备系统模块
		registerModule(GsConst.ModuleType.ARMOUR_MODULE, new PlayerArmourModule(this));
		// 泰伯利亚之战
		registerModule(GsConst.ModuleType.CHAMPIONSHIP, new PlayerGuildChampionshipModule(this));
		// 星球大战.
		registerModule(GsConst.ModuleType.STAR_WARS, new PlayerStarWarsModule(this));
		// 活动周历
		registerModule(GsConst.ModuleType.ACTIVITY_WEEK_CALENDER, new ActivityWeekCalenderModule(this));
		// 攻防模拟战
		registerModule(GsConst.ModuleType.SIMULATE_WAR, new PlayerSimulateWarModule(this));
		// 赛博之战
		registerModule(GsConst.ModuleType.CYBORG_WAR, new PlayerCyborgModule(this));
		// 账号注销
		registerModule(GsConst.ModuleType.ACCOUNT_CANCELLATION, new PlayerAccountCancellationModule(this));
		// 幽灵工厂
		registerModule(GsConst.ModuleType.GHOST_TOWER, new PlayerGhostTowerModule(this));
		// 小战区
		registerModule(GsConst.ModuleType.XZQ_MODULE, new PlayerXZQModule(this));
		// 星海激战赛制
		registerModule(GsConst.ModuleType.XHJZ_WAR, new PlayerXHJZModule(this));
		// 先驱回响赛制
		registerModule(GsConst.ModuleType.XQHX_WAR_MOUDLE, new PlayerXQHXModule(this));
		//通用商店
		registerModule(ModuleType.SHOP, new PlayerShopModule(this));
		//联盟组队功能
		registerModule(ModuleType.GUILD_TEAM, new PlayerGuildTeamModule(this));
		registerModule(ModuleType.CMW_MOUDLE, new PlayerCMWModule(this));
		// 方尖碑
		registerModule(GsConst.ModuleType.OBELISK_MODULE, new PlayerObeliskModule(this));
		registerModule(GsConst.ModuleType.PLANT_FACTORY, new PlayerPlantFactoryModule(this));
		registerModule(GsConst.ModuleType.PLANT_TECH, new PlayerPlantTechModule(this));
		registerModule(GsConst.ModuleType.PLANT_SOLDIER_SCHOOL, new PlayerPlantSoldierSchoolModule(this));
		registerModule(GsConst.ModuleType.PLANT_SOLDIER_ADVANCE, new PlantSoldierAdvanceModule(this));

		// 情报中心 
		registerModule(GsConst.ModuleType.AGENCY_MODULE, new PlayerAgencyModule(this));

		//泰能科技树
		registerModule(GsConst.ModuleType.PLANT_SCIENCE, new PlayerPlantScienceModule(this));
		
		// 迁服模块
		registerModule(GsConst.ModuleType.IMMGRATION, new PlayerImmgrationModule(this));
		
		
		registerModule(GsConst.ModuleType.DYZZ_WAR, new PlayerDYZZModule(this));
		
		// 国家基础和建设处
		registerModule(GsConst.ModuleType.NATIONAL_CONSTRUCTION, new PlayerNationContructionModule(this));
		// 国家任务中心
		registerModule(GsConst.ModuleType.NATIONAL_MISSION, new PlayerNationMissionModule(this));
		// 国家医院
		registerModule(GsConst.ModuleType.NATIONAL_HOSPITAL, new NationalHospitalModule(this));
		registerModule(GsConst.ModuleType.NATIONAL_HOSPITAL_TSZZ, new TszzNationalHospitalModule(this));
		// 国家仓库
		registerModule(GsConst.ModuleType.NATIONAL_WAREHOUSE, new NationalWarehouseModule(this));
		// 国家科技中心
		registerModule(GsConst.ModuleType.NATIONAL_TECH, new PlayerNationTechModule(this));
		// 国家飞船制造厂
		registerModule(GsConst.ModuleType.NATIONAL_SHIP_FACTORY, new PlayerNationShipFactoryModule(this));
		
		// 超时空特权卡 自动拉锅
		registerModule(ModuleType.AUTO_GATHER, new PlayerAutoModule(this));
		// 国家军功
		registerModule(ModuleType.NATION_MILITARY, new PlayerNationMilitaryModule(this));
		registerModule(ModuleType.CROSS_TALENT, new CrossTalentModule(this));
		//达雅赛季
		registerModule(GsConst.ModuleType.DYZZ_SEASON, new PlayerDYZZSeasonModule(this));
		//月球之战
		registerModule(GsConst.ModuleType.YQZZ_WAR_MODULE, new PlayerYQZZModule(this));
		// 星甲召唤
		registerModule(GsConst.ModuleType.SPACE_MECHA_MODULE, new PlayerSpaceMechaModule(this));
		// 联盟编队
		registerModule(GsConst.ModuleType.GUILD_FORMATION, new GuildFormationModule(this));
		registerModule(GsConst.ModuleType.STAFF_OFFICE, new PlayerStaffOfficerModule(this));
		// 终身卡
		registerModule(GsConst.ModuleType.LIFETIME_CARD, new PlayerLifetimeCardModule(this));
		registerModule(GsConst.ModuleType.MEDAL_FACTORY, new PlayerMedalFactoryModule(this));
		registerModule(GsConst.ModuleType.XHJZ, new XHJZPlayerModule(this));
		registerModule(GsConst.ModuleType.XQHX, new XQHXPlayerModule(this));
		// 超武
		registerModule(GsConst.ModuleType.MANHATTAN, new PlayerManhattanModule(this));
		// 机甲核心
		registerModule(GsConst.ModuleType.MECHA_CORE, new PlayerMechacoreModule(this));
		// 每日必买
		registerModule(GsConst.ModuleType.DAILY_GIFT_BUY_MOUDLE, new PlayerDailyGiftBuyModule(this));
		registerModule(GsConst.ModuleType.FGYL, new FGYLPlayerModule(this));		
		registerModule(GsConst.ModuleType.FGYL_WAR_MOUDLE, new PlayerFGYLWarModule(this));		
		registerModule(GsConst.ModuleType.MTTRUCK, new PlayerMeterialModule(this));// 押镖
		 //自动集结
        registerModule(GsConst.ModuleType.AUTO_MASS_JOIN, new PlayerAutoMassJoinModule(this));
        //待办事项
        registerModule(GsConst.ModuleType.SCHEDULE, new PlayerScheduleModule(this));
        //家园
        registerModule(GsConst.ModuleType.HOME_LAND_MODULE, new PlayerHomeLandModule(this));
        // 最后注册空闲模块, 用来消息收尾处理(必须放在最后面, 别动)
        registerModule(GsConst.ModuleType.IDLE_MODULE, new PlayerIdleModule(this));

	}

	/**
	 * 绑定玩家数据
	 * @param playerData
	 */
	public void updateData(PlayerData playerData) {
		this.playerData = playerData;

		// 推送组件的创建
		if (playerPush == null) {
			playerPush = new PlayerPush(this);
		}
	}

	/**
	 * 获取玩家数据
	 */
	public PlayerData getData() {
		if (playerData == null) {
			HawkLog.errPrintln("player get data null, xid: {}", getXid());
		}

		return playerData;
	}

	/**
	 * 玩家作用号模块
	 */
	public PlayerEffect getEffect() {
		return getData().getPlayerEffect();
	}

	/**
	 * 获取玩家同步对象
	 */
	public PlayerPush getPush() {
		return playerPush;
	}

	public void setPlayerPush(PlayerPush playerPush) {
		this.playerPush = playerPush;
	}

	/**
	 * 获取场景aoi对象id
	 */
	public int getAoiObjId() {
		return aoiObjId;
	}

	/**
	 * 设置场景aoi对象id
	 * 
	 * @param aoiObjId
	 */
	public void setAoiObjId(int aoiObjId) {
		this.aoiObjId = aoiObjId;
	}

	/**
	 * 是否在后台
	 * 
	 * @return
	 */
	public boolean isBackground() {
		return background > 0;
	}

	/**
	 * 玩家下线时重置变量
	 */
	public void resetParam() {
		this.getTickTimeLine().setOnlineTickTime(0);
		playerData.setOperationCount(0);
	}

	/**
	 * 设置在后台
	 * 
	 * @param background
	 */
	public void setBackground(long backgroundTime) {
		this.background = backgroundTime;
	}

	public long getBackground() {
		return background;
	}

	/**
	 * 获取平台的token信息
	 * 
	 * @return
	 */
	public JSONObject getPfTokenJson() {
		return pfTokenJson;
	}

	/**
	 * 设置 平台的token信息
	 * 
	 * @param pfTokenJson
	 */
	public void setPfTokenJson(JSONObject pfTokenJson) {
		this.pfTokenJson = pfTokenJson;

		if (pfTokenJson != null && pfTokenJson.containsKey("channelId")) {
			channelId = pfTokenJson.getString("channelId");
		}
	}

	/**
	 * 清理玩家数据
	 */
	public void clearData(boolean isRemove) {
		setBackground(0);
		setActiveState(GsConst.PlayerState.OFFLINE);
	}

	/**
	 * 获取实体对象
	 */
	public PlayerEntity getEntity() {
		return getData().getPlayerEntity();
	}

	/**
	 * 获取基础实时落地实体
	 * @return
	 */
	public PlayerBaseEntity getPlayerBaseEntity() {
		return getData().getPlayerBaseEntity();
	}

	/**
	 * 创建时间
	 */
	public long getCreateTime() {
		return getData().getPlayerEntity().getCreateTime();
	}

	/**
	 * 获取激活状态
	 */
	public int getActiveState() {
		return activeState;
	}

	/**
	 * 设置激活状态
	 *
	 * @param activeState
	 */
	public void setActiveState(int activeState) {
		this.activeState = activeState;
	}

	/**
	 * 是否为激活在线状态
	 */
	public boolean isActiveOnline() {
		if (!isSessionActive()) {
			return false;
		}

		return activeState == GsConst.PlayerState.ONLINE;
	}

	/**
	 * 是否idip需要改钻石
	 */
	public void idipChangeDiamonds(boolean diamondsChange) {
		this.diamondsChange = diamondsChange;
	}

	/**
	 * 取得当前所有已解锁的兵种id
	 * 
	 * @return
	 */
	public List<Integer> getUnlockedSoldierIds() {
		Collection<Integer> set = getData().getBuildingEntities().stream()
				.map(entity -> HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, entity.getBuildingCfgId()))
				.filter(cfg -> cfg != null && cfg.getUnlockedSoldierIds() != null)
				.flatMap(cfg -> cfg.getUnlockedSoldierIds().stream())
				.collect(Collectors.toSet());
		return ImmutableList.copyOf(set);
	}

	/**
	 * 获取玩家id
	 */
	public String getId() {
		return getData().getPlayerEntity().getId();
	}

	/**
	 * 获取puid
	 */
	public String getPuid() {
		return getData().getPlayerEntity().getPuid();
	}

	public String getOpenId() {
		return getData().getPlayerEntity().getOpenid();
	}

	/**
	 * 获取玩家的主服
	 * 没有合服则是本服, 合服就是合服后的主服.
	 * <br>
	 * ！！！！！ 主区不等于玩家当前所在区服 因为玩家可能跨服.
	 * <br>
	 * 获取玩家的注册服调用{@link #getServerId()}
	 * @return
	 */
	public String getMainServerId() {
		return GlobalData.getInstance().getMainServerId(this.getServerId());
	}

	/**
	 * 本方法仅用于查找注册所在服务器. 取得当前服务器请使用 获取玩家的注册服调用{@link #getMainServerId()}
	 * 获取玩家的注册区服serverId
	 * 
	 * <br>
	 * 如果想获取玩家当前的主区调用 {@link #getMainServerId()}
	 */
	@Deprecated
	public String getServerId() {
		return getData().getPlayerEntity().getServerId();
	}

	/**
	 * 获取玩家名字
	 */
	public String getName() {
		return getData().getPlayerEntity().getName();
	}

	/**
	 * 获取utf-8编码之后的玩家名字
	 * @return
	 */
	public String getNameEncoded() {
		return getData().getPlayerEntity().getNameEncoded();
	}

	/**
	 * 获取带联盟简称的玩家名字
	 */
	public String getNameWithGuildTag() {
		return GameUtil.getPlayerNameWithGuildTag(getGuildId(), getName());
	}

	/**
	 * 获得玩家头像
	 */
	public int getIcon() {
		return getData().getPlayerEntity().getIcon();
	}

	/**
	 * 获取平台头像
	 * @return
	 */
	public String getPfIcon() {
		return getData().getPfIcon();
	}

	/***
	 * 获取玩家显示的vip等级
	 * @return
	 */
	public int getShowVIPLevel() {
		return PlayerImageService.getInstance().getShowVIPLevel(this);
	}

	/**
	 * 获取渠道信息
	 * 
	 */
	public String getChannel() {
		return getData().getPlayerEntity().getChannel();
	}

	/**
	 * 获取国家信息
	 * 
	 */
	public String getCountry() {
		return getData().getPlayerEntity().getCountry();
	}

	/**
	 * 获取设备id
	 * 
	 */
	public String getDeviceId() {
		return getData().getPlayerEntity().getDeviceId();
	}

	/**
	 * 获取经验
	 * 
	 */
	public int getExp() {
		return getData().getPlayerBaseEntity().getExp();
	}

	/**
	 * 获取因拆除建筑扣除的经验值
	 * @return
	 */
	public int getExpDec() {
		return getData().getPlayerBaseEntity().getExpDec();
	}

	/**
	 * 获取vip经验
	 * @return
	 */
	public int getVipExp() {
		return getData().getPlayerEntity().getVipExp();
	}

	/**
	 * 获取客户端当前版本号
	 * 
	 */
	public String getAppVersion() {
		String version = getData().getPlayerEntity().getVersion();
		if(version != null && version.indexOf("_") > 0) {
			version = version.split("_")[1];
		}
		return version;
	}

	/**
	 * 获取客户端的语言设置
	 * 
	 */
	public String getLanguage() {
		return getData().getPlayerEntity().getLang();
	}

	/**
	 * 获取玩家最近登陆时间
	 * 
	 */
	public long getLoginTime() {
		return getData().getPlayerEntity().getLoginTime();
	}

	/**
	 * 获取上一次下线时间
	 */
	public long getLogoutTime() {
		return getData().getPlayerEntity().getLogoutTime();
	}

	/**
	 * 获取玩家历史累计在线时长（单位秒）
	 * @return
	 */
	public int getOnlineTimeHistory() {
		return getData().getPlayerEntity().getOnlineTimeHistory();
	}

	/**
	 * 获取玩家登陆的ip地址
	 */
	public String getClientIp() {
		if (HawkOSOperator.isEmptyString(sessionIp) && session != null) {
			String address = this.session.getAddress();
			if (address != null) {
				sessionIp = address.substring(1, address.indexOf(":"));
			}
		}

		if (sessionIp == null) {
			sessionIp = "";
		}

		return sessionIp;
	}

	public void setCityLevel(int level) {
		cityLevel = level;
	}

	/**
	 * 获取玩家主城等级
	 * @return
	 */
	public int getCityLevel() {
		if (cityLevel <= 0) {
			cityLevel = getData().getConstructionFactoryLevel();
		}

		return cityLevel;
	}

	/**
	 * 获取玩家应用ID
	 * @return
	 */
	public String getGameAppId() {
		if (gameAppId == null) {
			gameAppId = SDKManager.getInstance().getAppId(getChannel());
			if (gameAppId == null) {
				gameAppId = "";
				HawkLog.logPrintln("get gameAppId failed, playerId: {}, channel: {}", getId(), getChannel());
			}
		}

		return gameAppId;
	}

	/**
	 * 获取玩家平台ID
	 * @return
	 */
	public int getPlatId() {
		if (platId < 0) {
			platId = GameUtil.changePlatform2Int(getPlatform());
		}

		return platId;
	}

	/**
	 * 获取玩家的渠道ID
	 * platform: "android"
	   channel: "guest"
	 * @return
	 */
	public String getChannelId() {
		if (HawkOSOperator.isEmptyString(channelId)) {
			return "0";
		}

		return channelId;
	}

	/**
	 * 设置首次登陆状态
	 * 
	 * @param firstLogin
	 */
	public void setFirstLogin(int firstLogin) {
		this.firstLogin = firstLogin;
	}

	/**
	 * 是否为首次登陆
	 */
	public int getFirstLogin() {
		return firstLogin;
	}

	/**
	 * 获取平台信息
	 */
	public String getPlatform() {
		return getData().getPlayerEntity().getPlatform();
	}

	/**
	 * 获取手机信息
	 */
	public String getPhoneInfo() {
		assertNull();
		if (phoneInfo == null) {
			return null;
		}
		return phoneInfo.toJSONString();
	}

	public String getTelecomOper() {
		assertNull();
		return TelecomOper.valueOf(phoneInfo.getIntValue("mobileNetISP")).strVal();
	}

	public String getNetwork() {
		assertNull();
		return NetWork.valueOf(phoneInfo.getIntValue("mobileNetType")).strVal();
	}

	public String getClientHardware() {
		assertNull();
		return phoneInfo.getString("deviceMode");
	}

	/**
	 * 设备信息空判断
	 */
	private void assertNull() {
		if (phoneInfo == null) {
			String phoneInfoStr = RedisProxy.getInstance().getPlayerPhoneInfo(getId());
			phoneInfo = HawkOSOperator.isEmptyString(phoneInfoStr) ? null : JSONObject.parseObject(phoneInfoStr);
		}
	}

	/**
	 * 获取钻石
	 */
	public int getGold() {
		return getData().getPlayerBaseEntity().getGold();
	}

	/**
	 * 获取钻石
	 * @return
	 */
	public int getDiamonds() {
		return getPlayerBaseEntity().getDiamonds();
	}

	/**
	 * 获取体力
	 */
	public int getVit() {
		return getData().getPlayerEntity().getVit();
	}

	/**
	 * 获取金币
	 */
	public int getCoin() {
		return getData().getPlayerBaseEntity().getCoin();
	}

	/**
	 * 获取玩家战力值
	 */
	public long getPower() {
		return getData().getPlayerEntity().getBattlePoint();
	}

	/**
	 * 获取玩家等级
	 */
	public int getLevel() {
		return getData().getPlayerBaseEntity().getLevel();
	}

	/**
	 * 获取玩家大本等级
	 */
	public int getCityLv() {
		return getData().getConstructionFactoryLevel();
	}

	/** 获取玩家泰能等级*/
	public int getCityPlantLv() {
		for (PlantTechEntity enttiy : getData().getPlantTechEntities()) {
			if (Const.BuildingType.CONSTRUCTION_FACTORY_VALUE == enttiy.getBuildType()) {
				return enttiy.getTechObj().getCfg().getLevel();
			}
		}
		return 0;
	}

	/**
	 * 获取军衔等级
	 * @return
	 */
	public int getMilitaryRankLevel() {
		int militaryExp = getData().getPlayerEntity().getMilitaryExp();
		return GameUtil.getMilitaryRankByExp(militaryExp);
	}

	/**
	 * 获取玩家vip等级
	 */
	public int getVipLevel() {
		return getData().getPlayerEntity().getVipLevel();
	}

	/**
	 * 根据类型获取可掠夺资源(非保护资源 + 资源田中已产出未收取的量)
	 */
	public long[] getPlunderResAry(int[] RES_TYPE) {
		if (getData().getEffVal(Const.EffType.CITY_RES_PROTECT) > 0) {
			return new long[RES_TYPE.length];
		}

		int[] RES_LV = WorldMapConstProperty.getInstance().getResLv();
		if (RES_LV == null || RES_TYPE.length != RES_LV.length) {
			return new long[RES_TYPE.length];
		}

		BuildingCfg warehouseBuildCfg = playerData.getBuildingCfgByType(BuildingType.WAREHOUSE);

		int cityLv = getCityLv();
		// 仓库资源保护作用号
		int wareHouseBuffVal = getData().getEffVal(EffType.RES_PROTECTED_VAL);
		int wareHouseBuffPer = getData().getEffVal(EffType.RES_PROTECTED_PER);
		long[] count = new long[RES_TYPE.length];

		// 资源田已产出未收取的资源量
		Map<Integer, Long> outputRes = getResBuildOutput();

		for (int i = 0; i < RES_TYPE.length; i++) {
			if (cityLv < RES_LV[i]) {
				count[i] = 0;
				continue;
			}

			int type = RES_TYPE[i];
			long unsafeTotal = getResByType(type) + outputRes.getOrDefault(type, 0L);
			if (warehouseBuildCfg == null) {
				count[i] = unsafeTotal;
				continue;
			}

			int effPerSum = wareHouseBuffPer
					+ getEffPerByResType(type, EffType.GOLDORE_PROTECTED_PER, EffType.OIL_PROTECTED_PER, EffType.TOMBARTHITE_PROTECTED_PER, EffType.STEEL_PROTECTED_PER);
			int protect = warehouseBuildCfg.getResProtectByType(type);
			protect += Math.round(GsConst.EFF_PER * effPerSum * protect);
			protect += wareHouseBuffVal;
			count[i] = unsafeTotal > protect ? unsafeTotal - protect : 0;
		}

		return count;
	}

	/**
	 * 根据资源类型获取对应的作用号百分比
	 * @param resType 资源类型 
	 * @param effTypes 注意：作用号数组，数组元素（对应资源类型）的顺序，必须同switch下case的先后顺序保持一致！！！
	 * 
	 * @return
	 */
	public int getEffPerByResType(int resType, EffType... effTypes) {
		if (effTypes.length < GsConst.RES_TYPE.length) {
			throw new RuntimeException("effTypes parameter length error");
		}

		switch (resType) {
		case PlayerAttr.GOLDORE_VALUE:
		case PlayerAttr.GOLDORE_UNSAFE_VALUE:
			return this.getEffect().getEffVal(effTypes[0]);

		case PlayerAttr.OIL_VALUE:
		case PlayerAttr.OIL_UNSAFE_VALUE:
			return this.getEffect().getEffVal(effTypes[1]);

		case PlayerAttr.TOMBARTHITE_VALUE:
		case PlayerAttr.TOMBARTHITE_UNSAFE_VALUE:
			return this.getEffect().getEffVal(effTypes[2]);

		case PlayerAttr.STEEL_VALUE:
		case PlayerAttr.STEEL_UNSAFE_VALUE:
			return this.getEffect().getEffVal(effTypes[3]);

		default:
			return 0;
		}
	}

	/**
	 * 获取资源田中已产出还未收取的资源量
	 * 
	 * @return
	 */
	public Map<Integer, Long> getResBuildOutput() {

		Map<Integer, Long> map = new HashMap<>(BuildingCfg.resBuildingLimitTypes().length);
		for (int attr : GsConst.RES_TYPE) {
			map.put(attr, 0L);
		}

		long now = HawkTime.getMillisecond();
		for (BuildingBaseEntity entity : getData().getBuildingEntities()) {
			BuildingCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, entity.getBuildingCfgId());
			if (cfg == null || !cfg.isResBuilding()) {
				continue;
			}

			if (entity.getLastResCollectTime() > now) {
				entity.setLastResCollectTime(now);
				continue;
			}
			long timeLong = now - entity.getLastResCollectTime();
			switch (cfg.getBuildType()) {
			case BuildingType.OIL_WELL_VALUE:
				map.merge(PlayerAttr.OIL_UNSAFE_VALUE, GameUtil.resStore(this, entity.getId(), cfg, timeLong), (v1, v2) -> v1 + v2);
				break;
			case BuildingType.STEEL_PLANT_VALUE:
				map.merge(PlayerAttr.STEEL_UNSAFE_VALUE, GameUtil.resStore(this, entity.getId(), cfg, timeLong), (v1, v2) -> v1 + v2);
				break;
			case BuildingType.RARE_EARTH_SMELTER_VALUE:
				map.merge(PlayerAttr.TOMBARTHITE_UNSAFE_VALUE, GameUtil.resStore(this, entity.getId(), cfg, timeLong), (v1, v2) -> v1 + v2);
				break;
			case BuildingType.ORE_REFINING_PLANT_VALUE:
				map.merge(PlayerAttr.GOLDORE_UNSAFE_VALUE, GameUtil.resStore(this, entity.getId(), cfg, timeLong), (v1, v2) -> v1 + v2);
				break;
			default:
				break;
			}

		}

		return ImmutableMap.copyOf(map);
	}

	/**
	 * 获取可被军队消耗的石油数量
	 * @return
	 */
	public long getUnsafeOil() {
		int oilProtectBuffVal = getData().getEffVal(EffType.RES_PROTECTED_VAL);
		int oilProtectBuffPer = getData().getEffVal(EffType.RES_PROTECTED_PER) + getData().getEffVal(EffType.OIL_PROTECTED_PER);
		long hasUnsafeOil = getResByType(PlayerAttr.OIL_UNSAFE_VALUE);
		BuildingCfg cfg = getData().getBuildingCfgByType(BuildingType.WAREHOUSE);
		if (cfg != null) {
			int protect = cfg.getResProtectByType(PlayerAttr.OIL_UNSAFE_VALUE);
			protect += Math.round(GsConst.EFF_PER * oilProtectBuffPer * protect);
			protect += oilProtectBuffVal;
			hasUnsafeOil = hasUnsafeOil > protect ? hasUnsafeOil - protect : 0;
		}

		return hasUnsafeOil;
	}

	/**
	 * 按比例减少资源储量 只能是4种资源建筑
	 */
	public void decResStoreByPercent(BuildingType buildType, double decPercent) {
		List<BuildingBaseEntity> resBuildings = this.getData().getBuildingListByType(buildType);
		long now = HawkTime.getMillisecond();
		PushLastResCollectTime.Builder pushBuilder = PushLastResCollectTime.newBuilder();
		for (BuildingBaseEntity entity : resBuildings) {
			// 判断建筑状态
			BuildingCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, entity.getBuildingCfgId());
			if (cfg == null || !cfg.isResBuilding()) {
				continue;
			}

			// 产出速率
			double outputRate = GameUtil.getResOutputRate(this, entity.getId(), cfg);
			// 资源建筑最大储量
			double outputLimit = outputRate / cfg.getResPerHour() * cfg.getResLimit();
			// 当前产出量
			long timeLong = HawkTime.getMillisecond() - entity.getLastResCollectTime();
			long product = (long) (timeLong * 1.0D / GsConst.HOUR_MILLI_SECONDS * outputRate);
			product = (long) Math.min(outputLimit, product);

			long storeRemain = (long) (Math.max(0, product * (1 - decPercent)));
			long latestCollect = now - (long) (storeRemain * 1D / outputRate * GsConst.HOUR_MILLI_SECONDS);
			entity.setLastResCollectTime(Math.max(entity.getLastResCollectTime(), latestCollect));

			ResBuildingInfo.Builder builder = ResBuildingInfo.newBuilder();
			builder.setId(entity.getId());
			builder.setTime(entity.getLastResCollectTime());
			pushBuilder.addBuildingInfos(builder);
		}

		// 同步收取时间
		HawkProtocol resp = HawkProtocol.valueOf(HP.code.PUSH_LAST_RESOURCE_COLLECT_TIME_S, pushBuilder);
		this.sendProtocol(resp);
	}

	/**
	 * 获取金矿
	 */
	public long getGoldore() {
		return getData().getPlayerBaseEntity().getGoldore();
	}

	/**
	 * 获取金矿(非受保护的)
	 */
	public long getGoldoreUnsafe() {
		return getData().getPlayerBaseEntity().getGoldoreUnsafe();
	}

	/**
	 * 获取石油
	 */
	public long getOil() {
		return getData().getPlayerBaseEntity().getOil();
	}

	/**
	 * 获取石油(非受保护的)
	 */
	public long getOilUnsafe() {
		return getData().getPlayerBaseEntity().getOilUnsafe();
	}

	/**
	 * 获取钢铁
	 */
	public long getSteel() {
		return getData().getPlayerBaseEntity().getSteel();
	}

	/**
	 * 获取钢铁(非受保护的)
	 */
	public long getSteelUnsafe() {
		return getData().getPlayerBaseEntity().getSteelUnsafe();
	}

	/**
	 * 获取合金
	 */
	public long getTombarthite() {
		return getData().getPlayerBaseEntity().getTombarthite();
	}

	/**
	 * 获取合金(非受保护的)
	 */
	public long getTombarthiteUnsafe() {
		return getData().getPlayerBaseEntity().getTombarthiteUnsafe();
	}

	/**
	 * 获取钢铁（别的玩家侦查）
	 */
	public long getSteelSpy() {
		if (getCityLv() >= WorldMapConstProperty.getInstance().getCanCollectSteelLevel()) {
			return getSteel();
		}
		return 0;
	}

	/**
	 * 获取合金（别的玩家侦查）
	 */
	public long getTombarthiteSpy() {
		if (getCityLv() >= WorldMapConstProperty.getInstance().getCanCollectTombarthiteLevel()) {
			return getTombarthite();
		}
		return 0;
	}

	/**
	 * 通知错误码
	 *
	 * @param hpCode
	 * @param errCode
	 * @param errFlag 默认为0
	 */
	public void sendError(int hpCode, int errCode, int errFlag, String... params) {
		HPErrorCode.Builder builder = HPErrorCode.newBuilder();
		builder.setHpCode(hpCode);
		builder.setErrCode(errCode);
		builder.setErrFlag(errFlag);
		for (String param : params) {
			builder.addParams(param);
		}
		sendProtocol(HawkProtocol.valueOf(HP.sys.ERROR_CODE, builder));
	}

	/**
	 * 通知错误码
	 *
	 * @param hpCode
	 * @param errCode
	 * @param errFlag 默认为0
	 */
	public void sendError(int hpCode, ProtocolMessageEnum errCode, int errFlag) {
		sendError(hpCode, errCode.getNumber(), errFlag);
	}

	/**
	 * 发送协议
	 *
	 * @param protocol
	 */
	@Override
	public boolean sendProtocol(HawkProtocol protocol) {
		return sendProtocol(protocol, 0);
	}

	/**
	 * 发送协议
	 *
	 * @param protocol
	 */
	@Override
	public boolean sendProtocol(HawkProtocol protocol, long delayTime) {
		if (GsConfig.getInstance().isProtocolSecure()) {
			int needCompressLimit = GsConfig.getInstance().getProtocolCompressSize();
			int beforeSize = protocol.getSize();
			if (needCompressLimit > 0 && protocol.getSize() >= needCompressLimit && !this.isCsPlayer()) {
				if (protocol.getReserve() == 0) {
					protocol = ProtoUtil.compressProtocol(protocol);
				}
			}
			// 处理之后是否还超过大小
			if (needCompressLimit > 0 && protocol.getSize() >= needCompressLimit) {
				HawkLog.logPrintln("send protocol size overflow, protocol: {}, beforeSize: {}, afterSize: {}", protocol.getType(), beforeSize, protocol.getSize());
			}
		}

		return super.sendProtocol(protocol, delayTime);
	}

	/**
	 * 通用的操作成功回复协议
	 */
	public void responseSuccess(int hpCode) {
		HPOperateSuccess.Builder builder = HPOperateSuccess.newBuilder().setHpCode(hpCode);
		sendProtocol(HawkProtocol.valueOf(HP.sys.OPERATE_SUCCESS, builder));
	}

	public void notifyPlayerKickout(int reason, String msg) {
		HPPlayerKickout.Builder builder = HPPlayerKickout.newBuilder();
		builder.setReason(reason);
		if (!HawkOSOperator.isEmptyString(msg)) {
			builder.setMsg(msg);
		}
		sendProtocol(HawkProtocol.valueOf(HP.code.PLAYER_KICKOUT_S, builder));

	}

	/**
	 *  踢出玩家
	 * @param reason 踢出类型 
	 * @param notify 是否通知玩家
	 * @param msg 具体踢人原因
	 */
	public void kickout(int reason, boolean notify, String msg) {
		if (getData().getPlayerEntity() != null) {
			HawkLog.logPrintln("player been kickout, playerId: {}, reason: {}", getData().getPlayerEntity().getId(), reason);
		}

		if (notify) {
			notifyPlayerKickout(reason, msg);
		}

		if (session != null) {
			// 把回话绑定的玩家对象解除, 不再进行本回话的协议处理响应
			session.setAppObject(null);

			// 过1秒断开之前的连接
			final HawkSession kickoutSession = this.session;
			GsApp.getInstance().addDelayAction(1000, new HawkDelayAction() {
				@Override
				protected void doAction() {
					kickoutSession.close();
				}
			});

			GsApp.getInstance().postMsg(this, SessionClosedMsg.valueOf());
		}
	}

	/**
	 * 锁定玩家
	 */
	public void lockPlayer() {
		setLocker(true);
		synPlayerStatus(PlayerStatus.START);
	}

	/**
	 * 解锁玩家
	 */
	public void unLockPlayer() {
		unLockPlayer(Status.SysError.SUCCESS_OK_VALUE);
	}

	/**
	 * 解锁玩家保证玩家在迁服失败之后还能继续登录原来的服
	 * @param errorCode
	 */
	public void unLockPlayer(int errorCode) {
		setLocker(false);
		synPlayerStatus(PlayerStatus.FINISH, errorCode);
	}

	/**
	 * 帧更新
	 */
	@Override
	public boolean onTick() {
		if (!isActiveOnline()) {
			return true;
		}
		
		long currentTime = HawkApp.getInstance().getCurrentTime();
		if (currentTime < this.getTickTimeLine().getNextTickTime()) {
			return true;
		}
		
		this.getTickTimeLine().setNextTickTime(currentTime + 1000L);
		for(PlayerTickEnum playerTicker : PlayerTickEnum.values()) {
			playerTicker.getTickLogic().onTick(this);
		}
		
		return super.onTick();
	}
	
	/**
	 * 城点保护罩检测
	 * @param entity
	 * @param currentTime
	 */
	public long onCityShieldChange(StatusDataEntity entity, long currentTime) {
		long endTime = entity.getEndTime();
		if (entity.getStatusId() != Const.EffType.CITY_SHIELD_VALUE) {
			return endTime;
		}

		// 新手保护时间已过，但新手引导还未走完
		CustomDataEntity customDataEntity = getData().getCustomDataEntity(CustomKeyCfg.getTutorialKey());
		if (entity.getVal() == GsConst.ProtectState.NEW_PLAYER && customDataEntity != null
				&& !customDataEntity.getArg().equals(String.valueOf(GsConst.NEWBIE_COMPLETE_VALUE))) {
			endTime = Integer.MAX_VALUE;
			entity.setEndTime(endTime);
		} else if (endTime > 0) {
			entity.setEndTime(0);
			entity.setVal(GsConst.ProtectState.NO_BUFF);
			cityShieldStatusClear(entity);
			LogUtil.logCityShieldChange(this, false, 0, true);
		}

		if (endTime <= currentTime && !entity.isInitiative()) {
			SystemMailService.getInstance().sendMail(MailParames.newBuilder()
					.setPlayerId(getId())
					.setMailId(MailId.CITY_SHIELD_END)
					.build());
			try {
				WorldPoint worldPoint = WorldPlayerService.getInstance().getPlayerWorldPoint(getId());
				if (worldPoint != null) {
					WorldPointService.getInstance().getWorldScene().update(worldPoint.getAoiObjId());
				}
			} catch (Exception e) {

			}
		}
		return endTime;
	}

	/**
	 * 城点保护罩破罩提前通知
	 * @param entity 
	 * @param leftTime
	 */
	public void cityShieldRemovePrepareNotice(StatusDataEntity entity, long leftTime) {
		if (entity.getStatusId() != Const.EffType.CITY_SHIELD_VALUE) {
			return;
		}

		if (entity.getVal() == GsConst.ProtectState.NEW_PLAYER && getCityLv() < ConstProperty.getInstance().getProtectNotDisLevel()) {
			return;
		}

		if (leftTime < TimeUnit.MINUTES.toMillis(10) && leftTime > TimeUnit.MINUTES.toMillis(9) && !entity.isShieldNoticed()) {
			entity.resetShieldNoticed(true);
			SystemMailService.getInstance().sendMail(MailParames.newBuilder()
					.setPlayerId(getId())
					.setMailId(MailId.CITY_SHIELD_10_END)
					.build());
			try {
				WorldPoint worldPoint = WorldPlayerService.getInstance().getPlayerWorldPoint(getId());
				if (worldPoint != null) {
					WorldPointService.getInstance().getWorldScene().update(worldPoint.getAoiObjId());
				}
			} catch (Exception e) {

			}
		}
	}

	/**
	 * Buf(status)变化时处理
	 * 
	 * @param statusId
	 */
	public void onBufChange(int statusId, long endTime) {
		getPush().syncPlayerEffect(EffType.valueOf(statusId));

		switch (statusId) {
		case EffType.CITY_SHIELD_VALUE: {
			if (!GsConfig.getInstance().isRobotMode()) {
				WorldPlayerService.getInstance().updateWorldPointProtected(getId(), endTime);
			}
			break;
		}
		case EffType.RES_COLLECT_VALUE:
		case EffType.RES_COLLECT_BUF_VALUE:
		case EffType.RES_COLLECT_SKILL_VALUE:
		case EffType.RES_COLLECT_BOOST_VALUE: {
			// 只有生效的时候通知改变，结束的时候不改变
			if (endTime > HawkTime.getMillisecond()) {
				WorldMarchService.getInstance().changeResourceSpeed(this, 0);
			}
			break;
		}
		case EffType.RES_GOLD_COLLECT_VALUE:
		case EffType.RES_GOLD_COLLECT_BOOST_VALUE:
			WorldMarchService.getInstance().changeResourceSpeed(this, PlayerAttr.GOLDORE_UNSAFE_VALUE);
			break;
		case EffType.RES_OIL_COLLECT_VALUE:
		case EffType.RES_OIL_COLLECT_BOOST_VALUE:
			WorldMarchService.getInstance().changeResourceSpeed(this, PlayerAttr.OIL_UNSAFE_VALUE);
			break;
		case EffType.RES_STEEL_COLLECT_VALUE:
		case EffType.RES_STEEL_COLLECT_BOOST_VALUE:
			WorldMarchService.getInstance().changeResourceSpeed(this, PlayerAttr.STEEL_UNSAFE_VALUE);
			break;
		case EffType.RES_ALLOY_COLLECT_VALUE:
		case EffType.RES_ALLOY_COLLECT_BOOST_VALUE:
			WorldMarchService.getInstance().changeResourceSpeed(this, PlayerAttr.TOMBARTHITE_UNSAFE_VALUE);
			break;
		default:
			break;
		}
	}

	/**
	 * 协议响应
	 *
	 * @param protocol
	 */
	@Override
	public boolean onProtocol(HawkProtocol protocol) {
		if (!checkBeforeOnProtocol(protocol)) {
			return false;
		}

		if (!RoleExchangeService.getInstance().checkProtocol(protocol, this)) {
			return false;
		}
		if (LMJYRoomManager.getInstance().onProtocol(protocol, this)) {
			return true;
		}
		if (TBLYRoomManager.getInstance().onProtocol(protocol, this)) {
			return true;
		}
		if (SWRoomManager.getInstance().onProtocol(protocol, this)) {
			return true;
		}
		if (CYBORGRoomManager.getInstance().onProtocol(protocol, this)) {
			return true;
		}
		if (DYZZRoomManager.getInstance().onProtocol(protocol, this)) {
			return true;
		}
		if (YQZZRoomManager.getInstance().onProtocol(protocol, this)) {
			return true;
		}
		if (XHJZRoomManager.getInstance().onProtocol(protocol, this)) {
			return true;
		}
		if (FGYLRoomManager.getInstance().onProtocol(protocol, this)) {
			return true;
		}
		if (XQHXRoomManager.getInstance().onProtocol(protocol, this)) {
			return true;
		}
		
		// 检测协议访问次数
		if (!checkProtoCounter(protocol.getType())) {
			return false;
		}
		
		long startTime = 0;
		if (getData() != null && getData().getPlayerEntity() != null && GameConstCfg.getInstance().isSecOperSpecialPlayer(this.getId())) {
			startTime = HawkTime.getMillisecond();
		}
		
		// 父类进行处理
		boolean result = super.onProtocol(protocol);
		// 安全日志记录协议数据
		if (GsConfig.getInstance().isSecTlogPrintEnable() && getData() != null) {
			int operationCnt = getData().getOperationCount();
			if (LogUtil.logSecOperationFlow(this, protocol, operationCnt, result, startTime)) {
				getData().setOperationCount(operationCnt + 1);
			}
		}

		return result;
	}
	
	@Override
	public boolean onMessage(HawkMsg msg) {	
		if (YQZZRoomManager.getInstance().onMessage(msg, this)) {
			return true;
		}
		
		return super.onMessage(msg);
	}
	
	/**
	 * 协议校验
	 * 
	 * @param protocol
	 * @return
	 */
	private boolean checkBeforeOnProtocol(HawkProtocol protocol) {
		// 如果玩家锁住，不处理任何协议
		if (this.isLocker()) {
			logger.info("block player: {}, protocolType: {}", this.getId(), protocol.getType());
			return false;
		}
		/**
		 * csplayer 和player 需要根据不同的跨服状态做一些处理.
		 * csPlayer可以靠online 状态去拦截协议.
		 */
		if (this.getData() != null) {
			if (this.isCsPlayer()) {
				// cs player 在准备退出跨服和完成退出之后所有的协议都不处理了,并且完成退出之后,玩家会从accountInfo里面移除掉,不能makesureplayer出来.
				if (this.isCrossStatus(GsConst.PlayerCrossStatus.PREPARE_EXIT_CROSS, GsConst.PlayerCrossStatus.EXIT_CROSS_MARCH_FINAL, GsConst.PlayerCrossStatus.EXIT_CROSS)) {
					logger.info("cs player can not handle protocol id:{}, protocol:{}", this.getId(), protocol.getType());
					return false;
				}
			} else {
				// 处于跨服中的玩家不能接受任何的协议
				if (this.isCrossStatus(GsConst.PlayerCrossStatus.PREPARE_CROSS)) {
					logger.info("player can not handle protocol id:{}, protocol:{}", this.getId(), protocol.getType());
					return false;
				} else if (CrossService.getInstance().isEmigrationPlayer(this.getId())) {
					return true;
				}
			}
		}

		// 玩家登录流程未走完的情况下，不接受任何协议
		if (this.getActiveState() != GsConst.PlayerState.ONLINE && !protocol.checkType(HP.code.LOGIN_C)) {
			HawkLog.errPrintln("player recv logic protocol when unlogin, playerId: {}, protocol: {}",
					getXid().getUUID(), protocol.getType());
			return false;
		}

		// 零收益状态下受零收益控制的协议不做处理
		if (this.getActiveState() == GsConst.PlayerState.ONLINE && isZeroEarningState() && SysControlProperty.getInstance().isUnderZeroEarningControl(protocol.getType())) {
			sendError(protocol.getType(), Status.SysError.ZERO_EARNING_STATE, 0);
			// sendIDIPZeroEarningMsg();
			return false;
		}

		// 登录协议、心跳协议、配置校验协议，不用走以下额外校验
		if (protocol.checkType(HP.code.LOGIN_C) || protocol.checkType(HP.sys.HEART_BEAT) || protocol.checkType(HP.code.CFG_CHECK_C_VALUE)) {
			return true;
		}

		// 普通操作协议， 去除后台状态
		if (background > 0) {
			setBackground(0);
		}

		// 普通操作协议，检测到有钻石数量修改标记，强制离线
		try {
			if (diamondsChange) {
				List<MoneyReissueEntity> entityList = getData().getMoneyReissueEntityList();
				if (!entityList.isEmpty()) {
					HawkLog.logPrintln("player forced to offline, money reissue detected, playerId: {}", getId());
					kickout(Status.IdipMsgCode.IDIP_CHANGE_DIAMOND_OFFLINE_VALUE, true, null);
					return false;
				}
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}

		return true;
	}

	/**
	 * 协议异常的处理
	 * 
	 * @param protocolId
	 * @param errorCode
	 * @param errorMsg
	 */
	@Override
	protected void onProtocolException(int protocolId, int errorCode, String errorMsg) {
		sendError(protocolId, errorCode, 0);
	}

	/**
	 * 回话关闭回调
	 */
	@Override
	public void onSessionClosed() {
		// 清理数据
		clearData(false);

		// 管理器未设置缓存超时移除, 则玩家下线移除对象
		if (GsApp.getInstance().getObjMan(GsConst.ObjType.PLAYER).getObjTimeout() <= 0) {
			GsApp.getInstance().removeObj(getXid());
		}

		sessionIp = "";
		firstLogin = 0;
		super.onSessionClosed();
	}

	@MessageHandler
	public boolean onPlayerAssembleMsg(PlayerAssembleMsg msg) {
		if (getActiveState() == GsConst.PlayerState.LOGINING) {
			if (!msg.getSession().isActive()) {
				HawkLog.errPrintln("player assemble discard, playerId: {}", getId());
				return false;
			}

			long startTime = HawkTime.getMillisecond();
			for (Entry<Integer, HawkObjModule> entry : objModules.entrySet()) {
				try {
					PlayerModule playerModule = (PlayerModule) entry.getValue();
					playerModule.onPlayerAssemble();
					long now = HawkTime.getMillisecond();
					long costtime = now - startTime;
					if (costtime >= GsConfig.getInstance().getTaskTimeout()) {
						HawkLog.logPrintln("player assemble module: {}, playerId: {}, costtime: {}", playerModule.getClass().getSimpleName(), getId(), costtime);
						HawkProfilerAnalyzer.getInstance().addMsgHandleInfo("playerAssemble-" + playerModule.getClass().getSimpleName(), costtime);
					}
					startTime = now;
				} catch (Exception e) {
					HawkException.catchException(e);
				}
			}
		} else {
			HawkLog.errPrintln("player assemble state error, playerId: {}, state: {}", getId(), getActiveState());
		}
		return true;
	}

	@MessageHandler
	public boolean onPlayerLoginMsg(PlayerLoginMsg msg) {
		if (getActiveState() == GsConst.PlayerState.LOGINING) {
			if (!msg.getSession().isActive()) {
				HawkLog.errPrintln("player login discard, playerId: {}", getId());
				return false;
			}

			long startTime = HawkTime.getMillisecond();
			for (Entry<Integer, HawkObjModule> entry : objModules.entrySet()) {
				try {
					PlayerModule playerModule = (PlayerModule) entry.getValue();
					playerModule.onPlayerLogin();
					long now = HawkTime.getMillisecond();
					long costtime = now - startTime;
					if (costtime >= GsConfig.getInstance().getTaskTimeout()) {
						HawkLog.logPrintln("player login module: {}, playerId: {}, costtime: {}", playerModule.getClass().getSimpleName(), getId(), costtime);
						HawkProfilerAnalyzer.getInstance().addMsgHandleInfo("playerLogin-" + playerModule.getClass().getSimpleName(), costtime);
					}
					startTime = now;
				} catch (Exception e) {
					HawkException.catchException(e);
				}
			}

			// 判断是机器人账户，添加必要物资和等级
			if (isRobot()) {
				makeRobotRich();
			}

			// 我要变强登录事件
			StrengthenGuideManager.getInstance().postMsg(new SGPlayerLoginMsg(this));

		} else {
			HawkLog.errPrintln("player login state error, playerId: {}, state: {}", getId(), getActiveState());
		}
		return true;
	}

	/**
	 * 玩家组装和登录
	 * 
	 * @return
	 */
	public boolean doPlayerAssembleAndLogin(HawkSession session, HPLogin loginCmd) {
		// 玩家数据组装
		onPlayerAssembleMsg(PlayerAssembleMsg.valueOf(loginCmd, session));

		// 玩家登录处理
		onPlayerLoginMsg(PlayerLoginMsg.valueOf(session));

		return true;
	}

	/**
	 * 给机器人兵力和道具
	 */
	protected void makeRobotRich() {
		AwardItems awardItems = AwardItems.valueOf();

		// 给黄金
		awardItems.addItem(Const.ItemType.PLAYER_ATTR_VALUE, PlayerAttr.GOLDORE_UNSAFE_VALUE, 999999999);
		// 给石油
		awardItems.addItem(Const.ItemType.PLAYER_ATTR_VALUE, PlayerAttr.OIL_UNSAFE_VALUE, 999999999);
		// 给铀矿
		awardItems.addItem(Const.ItemType.PLAYER_ATTR_VALUE, PlayerAttr.STEEL_UNSAFE_VALUE, 999999999);
		// 给合金
		awardItems.addItem(Const.ItemType.PLAYER_ATTR_VALUE, PlayerAttr.TOMBARTHITE_UNSAFE_VALUE, 999999999);
		// 给水晶
		awardItems.addItem(Const.ItemType.PLAYER_ATTR_VALUE, PlayerAttr.GOLD_VALUE, 59999000);
		// 给生成野怪物品
		awardItems.addItem(Const.ItemType.TOOL_VALUE, 1400001, 999);
		awardItems.rewardTakeAffectAndPush(this, Action.GM_AWARD);
		logger.info("robot reward, playerId: {}, playerName: {}, reward: {}", getId(), getName(), awardItems.toDbString());
	}
	
	/**
	 * 机器人登录处理
	 * @return
	 */
	public boolean onRobotAssembleMsg() {
		if (!this.getOpenId().startsWith("robot")) {
			return false;
		}
		
		long startTime = HawkTime.getMillisecond();
		for (Entry<Integer, HawkObjModule> entry : objModules.entrySet()) {
			try {
				PlayerModule playerModule = (PlayerModule) entry.getValue();
				playerModule.onPlayerAssemble();
				long now = HawkTime.getMillisecond();
				long costtime = now - startTime;
				if (costtime >= GsConfig.getInstance().getTaskTimeout()) {
					HawkLog.logPrintln("player assemble module: {}, playerId: {}, costtime: {}", playerModule.getClass().getSimpleName(), getId(), costtime);
				}
				startTime = now;
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		return true;
	}

	/**
	 * 机器人登录处理
	 * @return
	 */
	public boolean onRobotLoginMsg() {
		if (!this.getOpenId().startsWith("robot")) {
			return false;
		}
		
		long startTime = HawkTime.getMillisecond();
		for (Entry<Integer, HawkObjModule> entry : objModules.entrySet()) {
			try {
				PlayerModule playerModule = (PlayerModule) entry.getValue();
				playerModule.onPlayerLogin();
				long now = HawkTime.getMillisecond();
				long costtime = now - startTime;
				if (costtime >= GsConfig.getInstance().getTaskTimeout()) {
					HawkLog.logPrintln("player login module: {}, playerId: {}, costtime: {}", playerModule.getClass().getSimpleName(), getId(), costtime);
				}
				startTime = now;
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}

		// 判断是机器人账户，添加必要物资和等级
		if (isRobot()) {
			makeRobotRich();
		}
		
		return true;
	}

	@MessageHandler
	public boolean onSessionClosedMsg(SessionClosedMsg msg) {
		// 记录回话关闭
		HawkLog.logPrintln("player session closed, playerId: {}, activeState: {}", msg.getTarget().getUUID(), getActiveState());

		// 正常的玩家数据才走下线流程
		if (getActiveState() == GsConst.PlayerState.ONLINE) {
			long startTime = HawkTime.getMillisecond();
			for (Entry<Integer, HawkObjModule> entry : objModules.entrySet()) {
				try {
					PlayerModule playerModule = (PlayerModule) entry.getValue();
					playerModule.onPlayerLogout();
					long now = HawkTime.getMillisecond();
					long costtime = now - startTime;
					if (costtime >= GsConfig.getInstance().getTaskTimeout()) {
						HawkLog.logPrintln("player logout module: {}, playerId: {}, costtime: {}", playerModule.getClass().getSimpleName(), getId(), costtime);
						HawkProfilerAnalyzer.getInstance().addMsgHandleInfo("playerLogout-" + playerModule.getClass().getSimpleName(), costtime);
					}
					startTime = now;
				} catch (Exception e) {
					HawkException.catchException(e);
				}
			}
		} else {
			// 没有初始化数据的玩家对象, 直接清理
			onSessionClosed();
		}

		return true;
	}

	/**
	 * 重置每日数据
	 * @param msg
	 * @return
	 */
	@MessageHandler
	public boolean onDailyDataClearMsg(DailyDataClearMsg msg) {
		// 推送五点钟事件
		this.noticeSystemEvent(SystemEvent.FIVE_COLOCK);

		DailyDataEntity dailyDataEntity = getData().getDailyDataEntity();
		dailyDataEntity.clear();
		LocalRedis.getInstance().clearFriendPresentGift(getId());

		PlayerGiftEntity playerGiftEntity = getData().getPlayerGiftEntity();
		playerGiftEntity.clearDailyGiftAdvice();
		return true;
	}

	public void noticeSystemEvent(SystemEvent event) {
		SynSystemEvent.Builder sbuilder = SynSystemEvent.newBuilder();
		sbuilder.setEven(event);

		sendProtocol(HawkProtocol.valueOf(HP.code.SYN_SYSTEM_EVENT_VALUE, sbuilder));
	}

	/**
	 * 增加体力
	 * 
	 * @param isRecover true表示是自动恢复
	 */
	public void increaseVit(int count, Action action, boolean isRecover) {
		if (count <= 0) {
			logger.error("increaseVit, playerId: {}, add: {}, action: {}", getId(), count, action.strValue());
			throw new RuntimeException("increaseVit");
		}

		int oldCount = getVit();
		int newCount = oldCount + count;
		int maxCount = getMaxVit();
		if (isRecover) {
			// 自动恢复体力受限于当前等级对应的最大体力值，若发现当前体力大于上限，则不做任何处理
			if (oldCount >= maxCount) {
				return;
			}
			newCount = Math.min(newCount, maxCount);
		}

		// 所有类型的体力值增加都受限于最大体力值上限
		newCount = Math.min(newCount, ConstProperty.getInstance().getActualVitLimit());
		getEntity().setVit(newCount);
		LogUtil.logVitChange(this, oldCount, newCount, newCount - oldCount, AddOrReduce.ADD, action);

		if (newCount >= maxCount) {
			getEntity().setVitTime(0);
		}

		BehaviorLogger.log4Service(this, Source.ATTR_CHANGE, action,
				Params.valueOf("playerAttr", PlayerAttr.VIT_VALUE),
				Params.valueOf("add", count),
				Params.valueOf("after", getVit()));
	}

	/** 
	 * 最大体力值 
	 */
	public int getMaxVit() {
		PlayerLevelExpCfg cfg = getCurPlayerLevelCfg();
		int maxVit = cfg.getVitPoint();

		int effVal1 = (int) Math.round(GsConst.EFF_PER * getData().getEffVal(EffType.PLAYER_VIT_MAX_PER) * maxVit);
		int effVal2 = getData().getEffVal(EffType.PLAYER_VIT_MAX_NUM);
		effVal2 += getData().getEffVal(EffType.PLAYER_VIT_MAX_NUM_CARD);
		VipCfg vipCfg = HawkConfigManager.getInstance().getConfigByKey(VipCfg.class, getVipLevel());
		if (vipCfg != null && getData().getVipActivated()) {
			maxVit *= (1 + vipCfg.getRecoverEnergyLimitAdd() * 1.0D / 100);
		}

		return maxVit + effVal1 + effVal2;
	}

	/** 
	 * 获取当前等级配置，无需判断null
	 */
	public PlayerLevelExpCfg getCurPlayerLevelCfg() {
		PlayerLevelExpCfg cfg = HawkConfigManager.getInstance().getConfigByKey(PlayerLevelExpCfg.class, getLevel());
		if (cfg == null) {
			logger.error("getCurPlayerLevelCfg, playerId: {}, level: {}", getId(), getLevel());
			throw new RuntimeException("current level cfg not found");
		}
		return cfg;
	}

	/**
	 * 增加水晶
	 *
	 * @param gold
	 * @param action
	 */
	public void increaseGold(long gold, Action action) {
		increaseGold(gold, action, true);
	}

	/**
	 * 增加水晶
	 * 
	 * @param gold
	 * @param action
	 * @param needLog
	 */
	public void increaseGold(long gold, Action action, boolean needLog) {
		if (gold <= 0) {
			logger.error("increaseGold, playerId: {}, add: {}, action: {}", getId(), gold, action.strValue());
			throw new RuntimeException("increaseRmbGold");
		}

		long oldNum = getData().getPlayerBaseEntity().getGold();
		if (oldNum + gold > Integer.MAX_VALUE) {
			getPlayerBaseEntity().setGold(Integer.MAX_VALUE);
		} else {
			getPlayerBaseEntity().setGold((int) (getData().getPlayerBaseEntity().getGold() + gold));
		}

		GameUtil.scoreBatch(this, ScoreType.GOLD, getPlayerBaseEntity().getGold());
		if (needLog) {
			LogUtil.logMoneyFlow(this, action, LogInfoType.money_add, gold, IMoneyType.MT_GOLD);
		}
		BehaviorLogger.log4Service(this, Source.ATTR_CHANGE, action,
				Params.valueOf("playerAttr", PlayerAttr.GOLD_VALUE),
				Params.valueOf("add", gold),
				Params.valueOf("after", getGold()));
	}

	/**
	 * 消耗水晶
	 * @param needGold
	 * @param action
	 * @return 水晶不够时消耗钻石产生的订单号
	 */
	public boolean consumeGold(int needGold, Action action) {
		int now = getGold();
		if (needGold <= 0 || needGold > now) {
			logger.error("consumeGold, playerId: {}, has: {}, cost: {}, action: {}",
					getId(), getGold(), needGold, action.strValue());
			throw new RuntimeException("consumeGold");
		}
		// 金币充足，直接扣除
		if (getPlayerBaseEntity().getGold() < needGold) {
			return false;
		}

		getPlayerBaseEntity().setGold(getGold() - needGold);
		GameUtil.scoreBatch(this, ScoreType.GOLD, getPlayerBaseEntity().getGold());
		LogUtil.logMoneyFlow(this, action, LogInfoType.money_consume, needGold, IMoneyType.MT_GOLD);
		RedisProxy.getInstance().idipDailyStatisAdd(getId(), IDIPDailyStatisType.GOLD_CONSUME, needGold);
		BehaviorLogger.log4Service(this, Source.ATTR_CHANGE, action,
				Params.valueOf("playerAttr", PlayerAttr.GOLD_VALUE),
				Params.valueOf("sub", needGold),
				Params.valueOf("after", getGold()));
		// 活动事件
		ActivityManager.getInstance().postEvent(new ConsumeMoneyEvent(getId(), PlayerAttr.GOLD_VALUE, needGold));
		MissionManager.getInstance().postMsg(this, new EventConsumMoney(needGold));
		return true;
	}

	/**
	 * 增加钻石
	 * @param diamond
	 * @param action
	 */
	public boolean increaseDiamond(int diamond, Action action) {
		return increaseDiamond(diamond, action, null, DiamondPresentReason.GAMEPLAY);
	}

	/**
	 * 增加钻石
	 * @param diamond  钻石数
	 * @param action
	 * @param extendParam  心悦大R代充扩展参数
	 * @param save 添加钻石失败时是否要记录下钻石数
	 */
	public boolean increaseDiamond(int diamond, Action action, String extendParam, String presentReason) {
		if (diamond <= 0) {
			logger.error("increase diamond, playerId: {}, add: {}, action: {}", getId(), diamond, action.strValue());
			return false;
		}

		if (!SDKManager.getInstance().isPayOpen()) {
			getPlayerBaseEntity().setDiamonds(getDiamonds() + diamond);
			getPush().syncPlayerDiamonds();
			HawkLog.logPrintln("increase diamond, playerId: {}, add: {}, afterCount: {}, action: {}", getId(), diamond, getDiamonds(), action.strValue());
			return true;
		}

		int retCode = present(diamond, extendParam, action.name(), presentReason);
		if (retCode != SDKConst.ResultCode.SUCCESS) {
			addMoneyReissueItem(diamond, action, extendParam); //存储到玩家身上后面再加
			HawkLog.errPrintln("increaseDiamond failed, playerId: {}, action: {}, diamond: {}, retCode: {}", getId(), action.name(), diamond, retCode);
			return false;
		}

		LogUtil.logMoneyFlow(this, action, LogInfoType.money_add, diamond, IMoneyType.MT_DIAMOND);
		BehaviorLogger.log4Service(this, Source.ATTR_CHANGE, action,
				Params.valueOf("playerAttr", PlayerAttr.DIAMOND_VALUE),
				Params.valueOf("add", diamond),
				Params.valueOf("after", getDiamonds()),
				Params.valueOf("extendParam", extendParam));
		return true;
	}

	/**
	 * 消耗钻石
	 * @param diamond
	 * @param action
	 * @param payItems 消耗金条的物品信息
	 * 
	 * @return 消耗钻石产生的订单号
	 */
	public String consumeDiamonds(int diamond, Action action, List<PayItemInfo> payItems) {
		if (diamond <= 0 || diamond > getDiamonds()) {
			logger.error("consumeDiamonds, playerId: {}, has: {}, cost: {}, action: {}",
					getId(), getDiamonds(), diamond, action.strValue());
			throw new RuntimeException("consumeDiamonds");
		}

		// 消耗钻石
		if (!SDKManager.getInstance().isPayOpen()) {
			getPlayerBaseEntity().setDiamonds(getDiamonds() - diamond);
			getPush().syncPlayerDiamonds();
			if (!diamondsConsumeIgnoreAction(action)) {
				ActivityManager.getInstance().postEvent(new ConsumeMoneyEvent(getId(), PlayerAttr.DIAMOND_VALUE, diamond));
			}
			return "abc";
		}

		// 已开启充值，根据不同类型支付SDK，调用不同支付接口
		int sdkType = SDKManager.getInstance().getSdkType();
		if (sdkType != SDKConst.SDKType.MSDK) {
			HawkLog.errPrintln("msdk config error, sdkType: {}, required type: {}", sdkType, SDKConst.SDKType.MSDK);
			return null;
		}

		String billno = pay(diamond, action.name(), payItems);
		if (HawkOSOperator.isEmptyString(billno)) {
			throw new RuntimeException("consumeDiamonds: " + action.name());
		}

		getPush().syncPlayerDiamonds();
		LogUtil.logMoneyFlow(this, action, LogInfoType.money_consume, diamond, IMoneyType.MT_DIAMOND);
		// 活动事件
		if (!diamondsConsumeIgnoreAction(action)) {
			ActivityManager.getInstance().postEvent(new ConsumeMoneyEvent(getId(), PlayerAttr.DIAMOND_VALUE, diamond));
		}
		RedisProxy.getInstance().idipDailyStatisAdd(getId(), IDIPDailyStatisType.DIAMOND_CONSUME, diamond);
		BehaviorLogger.log4Service(this, Source.ATTR_CHANGE, action,
				Params.valueOf("playerAttr", PlayerAttr.DIAMOND_VALUE),
				Params.valueOf("sub", diamond),
				Params.valueOf("after", getDiamonds()));

		return billno;
	}
	
	/**
	 * 判断是否是忽略金条消耗的行为
	 * @param action
	 * @return
	 */
	private boolean diamondsConsumeIgnoreAction(Action action) {
		return action == Action.IMMGRATION_DIAMONDS;
	}

	/**
	 * 增加金币
	 *
	 * @param coin
	 * @param action
	 */
	public void increaseCoin(int coin, Action action) {
		if (coin <= 0) {
			logger.error("increaseCoin, playerId: {}, add: {}, action: {}", getId(), coin, action.strValue());
			throw new RuntimeException("increaseCoin");
		}

		getPlayerBaseEntity().setCoin(getCoin() + coin);

		BehaviorLogger.log4Service(this, Source.ATTR_CHANGE, action,
				Params.valueOf("playerAttr", PlayerAttr.COIN_VALUE),
				Params.valueOf("add", coin),
				Params.valueOf("after", getCoin()));
	}

	/**
	 * 增加联盟贡献
	 */
	public void increaseGuildContribution(int value, Action action, boolean needLog) {
		if (value <= 0) {
			logger.error("increaseGuildCont, playerId: {}, add: {}, action: {}", getId(), value, action.strValue());
			throw new RuntimeException("increaseGuildContribution");
		}
		getPlayerBaseEntity().setGuildContribution(getGuildContribution() + value);
		// 刷新任务
		MissionManager.getInstance().postMsg(getId(), new EventGuildScore(value, getGuildContribution()));

		if (needLog) {
			LogUtil.logResourceFlow(this, action, LogInfoType.resource_add, PlayerAttr.GUILD_CONTRIBUTION_VALUE, getGuildContribution(), value);
		}

		BehaviorLogger.log4Service(this, Source.ATTR_CHANGE, action,
				Params.valueOf("playerAttr", PlayerAttr.GUILD_CONTRIBUTION),
				Params.valueOf("add", value),
				Params.valueOf("after", getGuildContribution()));
	}

	/**
	 * 增加军演积分
	 * 
	 * @param value
	 * @param action
	 */
	public void increaseMilitaryScore(int value, Action action, boolean needLog) {
		if (value <= 0) {
			logger.error("increaseMilitaryScore, playerId: {}, add: {}, action: {}", getId(), value, action.strValue());
			throw new RuntimeException("increaseMilitaryScore");
		}

		getPlayerBaseEntity().setGuildMilitaryScore(getMilitaryScore() + value);

		if (needLog) {
			LogUtil.logResourceFlow(this, action, LogInfoType.resource_add, PlayerAttr.MILITARY_SCORE_VALUE, getMilitaryScore(), value);
		}

		BehaviorLogger.log4Service(this, Source.ATTR_CHANGE, action,
				Params.valueOf("playerAttr", PlayerAttr.MILITARY_SCORE),
				Params.valueOf("add", value),
				Params.valueOf("after", getMilitaryScore()));
	}
	
	/**
	 * 增加国家军功
	 * @param addCnt
	 * @param resType 类型id
	 * @param action
	 * @return 实际增加值
	 */
	public int increaseNationMilitary(int addCnt, int resType, Action action, boolean needLog) {
		if (addCnt <= 0) {
			return 0;
		}

		NationMilitaryEntity commanderEntity = getData().getNationMilitaryEntity();
		switch (resType) {
		case PlayerAttr.NATION_MILITARY_VALUE:
			addCnt = NationMilitaryRankObj.getInstance().correctMilitaryExp(this, addCnt);
			commanderEntity.setNationMilitaryExp(commanderEntity.getNationMilitaryExp() + addCnt);
			if (needLog) {// 资源流水日志
				LogUtil.logResourceFlow(this, action, LogInfoType.resource_add, PlayerAttr.NATION_MILITARY_VALUE, commanderEntity.getNationMilitaryExp(), addCnt);
			}
			break;
		case PlayerAttr.NATION_MILITARY_BATTLE_VALUE:
			addCnt = NationMilitaryRankObj.getInstance().correctBattleMilitaryExp(this, addCnt);
			commanderEntity.setNationMilitaryBattleExp(commanderEntity.getNationMilitaryBattleExp() + addCnt);
			commanderEntity.setNationMilitaryExp(commanderEntity.getNationMilitaryExp() + addCnt);
			if (needLog) {// 资源流水日志
				LogUtil.logResourceFlow(this, action, LogInfoType.resource_add, PlayerAttr.NATION_MILITARY_VALUE, commanderEntity.getNationMilitaryExp(), addCnt);
				LogUtil.logResourceFlow(this, action, LogInfoType.resource_add, PlayerAttr.NATION_MILITARY_BATTLE_VALUE, commanderEntity.getNationMilitaryBattleExp(), addCnt);
			}
			break;
		default:
			break;
		}
		NationMilitaryRankObj.getInstance().zaddScore(this);
		BehaviorLogger.log4Service(this, Source.ATTR_CHANGE, action,
				Params.valueOf("playerAttr", resType),
				Params.valueOf("add", addCnt),
				Params.valueOf("after", commanderEntity.getNationMilitaryExp()));
		return addCnt;
	}

	/**
	 * 增加赛博积分
	 * 
	 * @param value
	 * @param action
	 */
	public void increaseCyborgScore(int value, Action action, boolean needLog) {
		if (value <= 0) {
			logger.error("increaseCyborgScore, playerId: {}, add: {}, action: {}", getId(), value, action.strValue());
			throw new RuntimeException("increaseCyborgScore");
		}

		getPlayerBaseEntity().setCyborgScore(getCyborgScore() + value);

		if (needLog) {
			LogUtil.logResourceFlow(this, action, LogInfoType.resource_add, PlayerAttr.CYBORG_SCORE_VALUE, getCyborgScore(), value);
		}

		BehaviorLogger.log4Service(this, Source.ATTR_CHANGE, action,
				Params.valueOf("playerAttr", PlayerAttr.CYBORG_SCORE),
				Params.valueOf("add", value),
				Params.valueOf("after", getMilitaryScore()));
	}
	
	
	/**
	 * 增加达雅积分
	 * 
	 * @param value
	 * @param action
	 */
	public void increaseDYZZScore(int value, Action action, boolean needLog) {
		if (value <= 0) {
			logger.error("increaseDYZZScore, playerId: {}, add: {}, action: {}", getId(), value, action.strValue());
			throw new RuntimeException("increaseDYZZScore");
		}

		getPlayerBaseEntity().setDyzzScore(getDYZZScore() + value);

		if (needLog) {
			LogUtil.logResourceFlow(this, action, LogInfoType.resource_add, PlayerAttr.DYZZ_SCORE_VALUE, getDYZZScore(), value);
		}

		BehaviorLogger.log4Service(this, Source.ATTR_CHANGE, action,
				Params.valueOf("playerAttr", PlayerAttr.DYZZ_SCORE),
				Params.valueOf("add", value),
				Params.valueOf("after", getMilitaryScore()));
	}

	/**
	 * 增加航海远征战略点
	 * @param value
	 * @param action
	 * @param needLog
	 */
	public void increaseCrossTalentPoint(int value, Action action, boolean needLog) {
		if (value <= 0) {
			logger.error("increaseCrossTalentPoint, playerId: {}, add: {}, action: {}", getId(), value, action.strValue());
			throw new RuntimeException("increaseDYZZScore");
		}

		// 添加跨服战略点
		CrossActivityService.getInstance().addCrossTalent(this, value);
		
		if (needLog) {
			LogUtil.logResourceFlow(this, action, LogInfoType.resource_add, PlayerAttr.CROSS_TALENT_POINT_VALUE, 0, value);
		}

		BehaviorLogger.log4Service(this, Source.ATTR_CHANGE, action,
				Params.valueOf("playerAttr", PlayerAttr.CROSS_TALENT_POINT_VALUE),
				Params.valueOf("add", value),
				Params.valueOf("after", 0));
	}
	
	/**
	 * 消费金币
	 *
	 * @param coin
	 * @param action
	 */
	public void consumeCoin(int coin, Action action) {
		if (coin <= 0 || coin > getCoin()) {
			logger.error("consumeCoin, playerId: {}, has: {}, cost: {}, action: {}",
					getId(), getCoin(), coin, action.strValue());
			throw new RuntimeException("consumeCoin");
		}

		getPlayerBaseEntity().setCoin(getCoin() - coin);

		BehaviorLogger.log4Service(this, Source.ATTR_CHANGE, action,
				Params.valueOf("playerAttr", PlayerAttr.COIN_VALUE),
				Params.valueOf("sub", coin),
				Params.valueOf("after", getCoin()));
	}

	/**
	 * 增加物品
	 */
	public List<ItemEntity> increaseTools(ItemInfo itemAdd, Action action, ItemCfg itemCfg, boolean countCheck) {
		int itemId = itemAdd.getItemId();
		final int itemCount = (int) itemAdd.getCount();
		if (!ConfigUtil.checkItemType(ItemType.TOOL_VALUE, itemId)) {
			return null;
		}
		if (itemCfg.getItemType() == Const.ToolType.HERO_VALUE) {
			HawkApp.getInstance().postMsg(this.getXid(), HeroItemChangedMsg.valueOf(itemAdd.clone(), itemCfg));
		}

		if (countCheck && (itemCount < 0 || itemCount > ConstProperty.getInstance().getMaxAddItemNum())) {
			logger.error("increaseTool, playerId: {}, itemId: {}, add: {}, action: {}", getId(), itemId, itemCount, action.strValue());
			throw new RuntimeException("increaseTools");
		}

		int addCount = itemCount;
		List<ItemEntity> changedItems = new ArrayList<ItemEntity>();
		List<ItemEntity> items = getData().getItemsByItemId(itemId);
		if (items.size() > 0) {
			for (ItemEntity item : items) {
				int addCnt = Math.min(addCount, itemCfg.getPile_max() - item.getItemCount());
				if (addCnt <= 0) {
					continue;
				}
				item.setItemCount(item.getItemCount() + addCnt);
				item.setNew(true);
				addCount -= addCnt;
				changedItems.add(item);
				if (addCount == 0) {
					break;
				}
			}
		}

		while (addCount > 0) {
			if (itemCfg == null) {
				break;
			}

			int cnt;
			if (itemCfg.getPile_max() == 0) { // 无上限
				cnt = addCount;
			} else {
				cnt = Math.min(addCount, itemCfg.getPile_max());
			}

			Optional<ItemEntity> optional = getData().getItemEntities().stream().filter(e -> e.getItemId() == itemId && e.getItemCount() == 0).findAny();
			if (optional.isPresent()) {
				ItemEntity itemEntity = optional.get();
				itemEntity.setItemCount(cnt);
				itemEntity.setNew(true);
				changedItems.add(itemEntity);
			} else {
				ItemEntity itemEntity = new ItemEntity();
				itemEntity.setItemId(itemId);
				itemEntity.setItemCount(cnt);
				itemEntity.setPlayerId(getId());
				itemEntity.setId(HawkUUIDGenerator.genUUID());
				if (itemEntity.create(true)) {
					getData().addItemEntity(itemEntity);
					changedItems.add(itemEntity);
				}
			}
			addCount -= cnt;
		}
		
		//道具添加后需要执行的额外逻辑
		for(ItemAddLogicEnum enumObj : ItemAddLogicEnum.values()) {
			try {
				enumObj.getLogicObj().addLogic(this, itemId, itemCount, action);
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}

		BehaviorLogger.log4Service(this, Source.TOOLS_ADD, action,
				Params.valueOf("itemId", itemId),
				Params.valueOf("add", itemCount),
				Params.valueOf("after", getData().getItemNumByItemId(itemId)));

		return changedItems;
	}

	public List<ItemEntity> increaseTools(ItemInfo itemAdd, Action action, ItemCfg itemCfg) {
		return this.increaseTools(itemAdd, action, itemCfg, true);
	}

	/**
	 * 消耗物品
	 */
	public void consumeTool(String id, int itemType, int disCount, Action action) {
		if (disCount <= 0) {
			logger.error("consumeTool, playerId: {}, itemId: {}, cost: {}, action: {}",
					getId(), id, disCount, action.strValue());
			throw new RuntimeException("consumeTool disCount error");
		}

		ItemEntity item = getData().getItemById(id);
		if (item == null || item.getItemCount() <= 0 || disCount > item.getItemCount()) {
			logger.error("consumeTool, playerId: {}, entityId: {}, itemId: {}, itemCnt: {}, cost: {}, action: {}",
					getId(), id, item == null ? 0 : item.getItemId(), item == null ? 0 : item.getItemCount(), disCount, action.strValue());
			throw new RuntimeException("consumeTool item error");
		}

		int remainCnt = item.getItemCount() - disCount;
		item.setItemCount(remainCnt);
		
		//此处不在执行db删除操作，留待特定时间段登录时操作
//		if (remainCnt <= 0) {
//			getData().getItemEntities().remove(item);
//			item.delete(true);
//		}

		Set<Integer> itemIdSet = AssembleDataManager.getInstance().getPushGiftTriggerParamSet(PushGiftConditionEnum.ITEM_CONSUME);
		if (itemIdSet.contains(item.getItemId())) {
			HawkApp.getInstance().postMsg(this, UseItemMsg.valueOf(item.getItemId(), disCount));
		}
		if(GrowUpBoostScoreCfg.useAddScore(action,item.getItemId())){
			GrowUpBoostActivity.onActivityItemConsumeEvnet(new GrowUpBoostItemConsumeEvent(this.getId(),item.getItemId(), disCount,action.intItemVal()));
		}
		if(ConstProperty.getInstance().isItemPostEvnt(item.getItemId())){
			ActivityManager.getInstance().postEvent(
					new GivenItemCostEvent(this.getId(),item.getItemId(), disCount));
		}
		BehaviorLogger.log4Service(this, Source.TOOLS_REMOVE, action,
				Params.valueOf("itemId", item.getItemId()),
				Params.valueOf("id", item.getId()),
				Params.valueOf("sub", disCount),
				Params.valueOf("after", remainCnt),
				Params.valueOf("before", remainCnt + disCount));
	}

	public void increaseResource(long addCnt, int resType, Action action) {
		increaseResource(addCnt, resType, action, true);
	}

	/**
	 * 增加资源
	 * @param addCnt
	 * @param resType 类型id
	 * @param action
	 */
	public void increaseResource(long addCnt, int resType, Action action, boolean needLog) {
		if (addCnt <= 0) {
			logger.error("increaseResource, playerId: {}, type: {}, add: {}, action: {}",
					getId(), resType, addCnt, action.strValue());
			switch (resType) {
			case PlayerAttr.GOLDORE_VALUE:
				throw new RuntimeException("increaseGoldore");
			case PlayerAttr.GOLDORE_UNSAFE_VALUE:
				throw new RuntimeException("increaseGoldoreUnsafe");
			case PlayerAttr.OIL_VALUE:
				throw new RuntimeException("increaseOil");
			case PlayerAttr.OIL_UNSAFE_VALUE:
				throw new RuntimeException("increaseOilUnsafe");
			case PlayerAttr.STEEL_VALUE:
				throw new RuntimeException("increaseSteel");
			case PlayerAttr.STEEL_UNSAFE_VALUE:
				throw new RuntimeException("increaseSteelUnsafe");
			case PlayerAttr.TOMBARTHITE_VALUE:
				throw new RuntimeException("increaseTombarthite");
			default:
				break;
			}
		}

		long afterCount = 0;
		switch (resType) {
		case PlayerAttr.GOLDORE_VALUE:
			getPlayerBaseEntity().setGoldore(getGoldore() + addCnt);
			afterCount = getGoldore();
			break;
		case PlayerAttr.GOLDORE_UNSAFE_VALUE:
			getPlayerBaseEntity().setGoldoreUnsafe(getGoldoreUnsafe() + addCnt);
			afterCount = getGoldoreUnsafe();
			break;
		case PlayerAttr.OIL_VALUE:
			getPlayerBaseEntity().setOil(getOil() + addCnt);
			afterCount = getOil();
			break;
		case PlayerAttr.OIL_UNSAFE_VALUE:
			getPlayerBaseEntity().setOilUnsafe(getOilUnsafe() + addCnt);
			afterCount = getOilUnsafe();
			break;
		case PlayerAttr.STEEL_VALUE:
			getPlayerBaseEntity().setSteel(getSteel() + addCnt);
			afterCount = getSteel();
			break;
		case PlayerAttr.STEEL_UNSAFE_VALUE:
			getPlayerBaseEntity().setSteelUnsafe(getSteelUnsafe() + addCnt);
			afterCount = getSteelUnsafe();
			break;
		case PlayerAttr.TOMBARTHITE_VALUE:
			getPlayerBaseEntity().setTombarthite(getTombarthite() + addCnt);
			afterCount = getTombarthite();
			break;
		case PlayerAttr.TOMBARTHITE_UNSAFE_VALUE:
			getPlayerBaseEntity().setTombarthiteUnsafe(getTombarthiteUnsafe() + addCnt);
			afterCount = getTombarthiteUnsafe();
			break;
		default:
			break;
		}

		// 资源流水日志
		if (needLog) {
			LogUtil.logResourceFlow(this, action, LogInfoType.resource_add, resType, afterCount, addCnt);
		}

		BehaviorLogger.log4Service(this, Source.ATTR_CHANGE, action,
				Params.valueOf("playerAttr", resType),
				Params.valueOf("add", addCnt),
				Params.valueOf("after", afterCount));
	}

	/**
	 * 根据资源类型取资源数量
	 */
	public long getResByType(int type) {
		return getResbyType(PlayerAttr.valueOf(type));
	}

	/**
	 * 根据资源类型取资源数量
	 */
	public long getResbyType(PlayerAttr type) {
		switch (type) {
		case GOLDORE:
			return getGoldore();
		case GOLDORE_UNSAFE:
			return getGoldoreUnsafe();
		case OIL:
			return getOil();
		case OIL_UNSAFE:
			return getOilUnsafe();
		case STEEL:
			return getSteel();
		case STEEL_UNSAFE:
			return getSteelUnsafe();
		case TOMBARTHITE:
			return getTombarthite();
		case TOMBARTHITE_UNSAFE:
			return getTombarthiteUnsafe();
		case GUILD_CONTRIBUTION:
			return getGuildContribution();
		case MILITARY_SCORE:
			return getMilitaryScore();
		case CYBORG_SCORE:
			return getCyborgScore();
		case DYZZ_SCORE:
			return getDYZZScore();
		default:
			break;
		}

		return 0;
	}

	/**
	 * 根据资源类型取资源数量，包括安全资源和非安全资源
	 * @param type
	 * @return
	 */
	public long getAllResByType(int type) {
		switch (type) {
		case PlayerAttr.GOLDORE_VALUE:
		case PlayerAttr.GOLDORE_UNSAFE_VALUE:
			return getGoldore() + getGoldoreUnsafe();
		case PlayerAttr.OIL_VALUE:
		case PlayerAttr.OIL_UNSAFE_VALUE:
			return getOil() + getOilUnsafe();
		case PlayerAttr.STEEL_VALUE:
		case PlayerAttr.STEEL_UNSAFE_VALUE:
			return getSteel() + getSteelUnsafe();
		case PlayerAttr.TOMBARTHITE_VALUE:
		case PlayerAttr.TOMBARTHITE_UNSAFE_VALUE:
			return getTombarthite() + getTombarthiteUnsafe();
		default:
			break;
		}

		return 0;
	}

	/**
	 * 消耗资源
	 * 
	 * @param subCnt
	 * @param resType
	 * @param action
	 */
	public void consumeResource(long subCnt, int resType, Action action) {
		PlayerAttr playerAttr = PlayerAttr.valueOf(resType);
		if (subCnt <= 0) {
			logger.error("consumeResource, playerId: {}, type: {}, cost: {}, action: {}",
					getId(), resType, subCnt, action.strValue());
			if (playerAttr != null) {
				throw new RuntimeException("consume resource - " + playerAttr.name().toLowerCase() + ": " + subCnt);
			}
		}

		long retVal = 0;
		switch (resType) {
		case PlayerAttr.GOLDORE_VALUE:
			retVal = getGoldore();
			break;
		case PlayerAttr.GOLDORE_UNSAFE_VALUE:
			retVal = getGoldoreUnsafe();
			break;
		case PlayerAttr.OIL_VALUE:
			retVal = getOil();
			break;
		case PlayerAttr.OIL_UNSAFE_VALUE:
			retVal = getOilUnsafe();
			break;
		case PlayerAttr.STEEL_VALUE:
			retVal = getSteel();
			break;
		case PlayerAttr.STEEL_UNSAFE_VALUE:
			retVal = getSteelUnsafe();
			break;
		case PlayerAttr.TOMBARTHITE_VALUE:
			retVal = getTombarthite();
			break;
		case PlayerAttr.TOMBARTHITE_UNSAFE_VALUE:
			retVal = getTombarthiteUnsafe();
			break;
		default:
			break;
		}

		if (subCnt > retVal) {
			logger.error("consumeResource, playerId: {}, has: {}, cost: {}, action: {}",
					getId(), retVal, subCnt, action.strValue());
			if (action.equals(Action.ATTACKED_BY_PLAYER) || action.equals(Action.PRESIDENT_REVENUED)) {
				subCnt = retVal;
			} else {
				if (playerAttr != null) {
					throw new RuntimeException("consume resource - " + playerAttr.name().toLowerCase());
				}
			}
		}

		switch (resType) {
		case PlayerAttr.GOLDORE_VALUE:
			getPlayerBaseEntity().setGoldore(getGoldore() - subCnt);
			retVal = getGoldore();
			break;
		case PlayerAttr.GOLDORE_UNSAFE_VALUE:
			getPlayerBaseEntity().setGoldoreUnsafe(getGoldoreUnsafe() - subCnt);
			retVal = getGoldoreUnsafe();
			break;
		case PlayerAttr.OIL_VALUE:
			getPlayerBaseEntity().setOil(getOil() - subCnt);
			retVal = getOil();
			break;
		case PlayerAttr.OIL_UNSAFE_VALUE:
			getPlayerBaseEntity().setOilUnsafe(getOilUnsafe() - subCnt);
			retVal = getOilUnsafe();
			break;
		case PlayerAttr.STEEL_VALUE:
			getPlayerBaseEntity().setSteel(getSteel() - subCnt);
			retVal = getSteel();
			break;
		case PlayerAttr.STEEL_UNSAFE_VALUE:
			getPlayerBaseEntity().setSteelUnsafe(getSteelUnsafe() - subCnt);
			retVal = getSteelUnsafe();
			break;
		case PlayerAttr.TOMBARTHITE_VALUE:
			getPlayerBaseEntity().setTombarthite(getTombarthite() - subCnt);
			retVal = getTombarthite();
			break;
		case PlayerAttr.TOMBARTHITE_UNSAFE_VALUE:
			getPlayerBaseEntity().setTombarthiteUnsafe(getTombarthiteUnsafe() - subCnt);
			retVal = getTombarthiteUnsafe();
			break;
		default:
			break;
		}

		// 资源流水日志
		LogUtil.logResourceFlow(this, action, LogInfoType.resource_sub, resType, retVal, subCnt);

		BehaviorLogger.log4Service(this, Source.ATTR_CHANGE, action,
				Params.valueOf("playerAttr", resType),
				Params.valueOf("sub", subCnt),
				Params.valueOf("after", retVal));
	}

	/**
	 * 消耗体力
	 * 
	 * @param vit
	 * @param action
	 */
	public void consumeVit(int vit, Action action) {
		if (vit <= 0) {
			logger.error("consumeVit, playerId: {}, cost: {}, action: {}", getId(), vit, action.strValue());
			throw new RuntimeException("consumeVit");
		}

		int oldVit = getVit();
		int newVit = oldVit - vit;
		getEntity().setVit(newVit);

		int maxVit = getMaxVit();
		if (oldVit >= maxVit && newVit < maxVit) {
			getEntity().setVitTime(HawkTime.getMillisecond());
		}

		LogUtil.logVitChange(this, oldVit, newVit, vit, AddOrReduce.REDUCE, action);

		// 活动事件
		ActivityManager.getInstance().postEvent(new VitreceiveConsumeEvent(getId(), vit));

		BehaviorLogger.log4Service(this, Source.ATTR_CHANGE, action,
				Params.valueOf("playerAttr", PlayerAttr.VIT_VALUE),
				Params.valueOf("cost", vit),
				Params.valueOf("after", newVit));
	}

	/**
	 * 增加等级
	 */
	public void increaseLevel(int level, Action action) {
		if (level <= 0) {
			throw new RuntimeException("increaseLevel");
		}

		int newLevel = getLevel() + level;
		if (newLevel > PlayerLevelExpCfg.getMaxLevel()) {
			newLevel = PlayerLevelExpCfg.getMaxLevel();
		}

		if (getLevel() != newLevel) {
			getPlayerBaseEntity().setLevel(newLevel);
			getPlayerBaseEntity().setLevelUpTime(HawkTime.getMillisecond());
		}
		HawkTaskManager.getInstance().postMsg(this.getXid(), PlayerUnlockImageMsg.valueOf(UnlockType.PLAYERLEVEL, newLevel));
		MissionManager.getInstance().postMsg(this, new EventPlayerUpLevel(newLevel - level, newLevel));

		BehaviorLogger.log4Service(this, Source.ATTR_CHANGE, action,
				Params.valueOf("playerAttr", PlayerAttr.LEVEL_VALUE),
				Params.valueOf("add", level),
				Params.valueOf("after", newLevel));
	}

	/**
	 * 扣除vip经验
	 */
	public void decreaceVipExp(int subVipExp, Action action) {
		if (subVipExp <= 0) {
			logger.error("decreaceVipExp, playerId: {}, dec: {}, action: {}", getId(), subVipExp, action.strValue());
			throw new RuntimeException("decreaceVipExp");
		}

		int vipExp = getEntity().getVipExp();
		int newVipExp = vipExp > subVipExp ? vipExp - subVipExp : 0;
		getEntity().setVipExp(newVipExp);
		int vipLevel = getVipLevel();
		int finalLevel = 0;
		for (int level = vipLevel - 1; level >= 0; level--) {
			VipCfg vipCfg = HawkConfigManager.getInstance().getConfigByKey(VipCfg.class, level);
			if (vipCfg == null) {
				continue;
			}

			if (vipCfg.getVipExp() <= newVipExp) {
				finalLevel = level;
				break;
			}
		}

		getEntity().setVipLevel(finalLevel);
		getPush().syncPlayerInfo();
		// 推送vip作用号变化
		getEffect().initEffectVip(this);
		// vip变更log
		LogUtil.logVipExpFlow(this, vipLevel, vipExp, newVipExp, subVipExp, action);

		BehaviorLogger.log4Service(this, Source.ATTR_CHANGE, action,
				Params.valueOf("playerAttr", PlayerAttr.VIP_POINT_VALUE),
				Params.valueOf("sub", subVipExp),
				Params.valueOf("after", newVipExp),
				Params.valueOf("oldLevel", vipLevel),
				Params.valueOf("newLevel", getVipLevel()));
	}

	/**
	 * 增加vip经验
	 */
	public void increaseVipExp(int addExp, Action action) {
		if (addExp <= 0) {
			logger.error("increaseVipExp, playerId: {}, add: {}, action: {}", getId(), addExp, action.strValue());
			throw new RuntimeException("increaseVipExp");
		}

		int newExp = Math.max(0, getEntity().getVipExp() + addExp);
		getEntity().setVipExp(newExp);

		int oldLevel = Math.max(0, getVipLevel());
		final int maxLevel = VipCfg.getMaxLevel();
		// vip等级达到当前配置上限，继续获得vip经验无法提升vip等级
		if (oldLevel >= maxLevel) {
			return;
		}

		int level = oldLevel;
		for (level++; level <= maxLevel; level++) {
			VipCfg vipCfg = HawkConfigManager.getInstance().getConfigByKey(VipCfg.class, level);
			if (vipCfg == null) {
				level--;
				HawkLog.errPrintln("cannot find vip config, playerId: {}, level: {}", getId(), level);
				break;
			}

			int levelUpNeedExp = vipCfg.getVipExp();
			if (newExp < levelUpNeedExp) {
				level--;
				break;
			}

			// vip经验值一直累加
			// newExp -= levelUpNeedExp;
		}

		if (level > maxLevel) {
			level = maxLevel;
		}

		if (level > oldLevel) {
			getEntity().setVipLevel(level);
			RedisProxy.getInstance().vipGiftRefresh(getId(), VipRelatedDateType.VIP_BENEFIT_TAKEN,
					GsApp.getInstance().getCurrentTime() - GsConst.DAY_MILLI_SECONDS);
			GameUtil.scoreBatch(this, ScoreType.VIP_LEVEL, level);
			HawkTaskManager.getInstance().postMsg(this.getXid(), PlayerUnlockImageMsg.valueOf(UnlockType.VIPLEVEL, level));
			StrengthenGuideManager.getInstance().postMsg(new SGPlayerVipLevelUpMsg(this));
			ActivityManager.getInstance().postEvent(new VipLevelupEvent(getId(), oldLevel, level));
		}

		BehaviorLogger.log4Service(this, Source.ATTR_CHANGE, action,
				Params.valueOf("playerAttr", PlayerAttr.VIP_POINT_VALUE),
				Params.valueOf("add", addExp),
				Params.valueOf("after", newExp),
				Params.valueOf("oldLevel", oldLevel),
				Params.valueOf("newLevel", level));
	}

	/**
	 * 扣除经验值
	 * @param exp
	 * @param action
	 */
	public void decreaseExp(int subExp, Action action) {
		if (subExp <= 0) {
			logger.error("decreaseExp, playerId: {}, dec: {}, action: {}",
					getId(), subExp, action.strValue());
			throw new RuntimeException("decreaseExp");
		}

		int exp = getExp();
		int newExp = exp > subExp ? exp - subExp : 0;
		getPlayerBaseEntity().setExp(newExp);
		int playerLevel = getLevel();
		LogUtil.logPlayerExpFlow(this, exp - newExp, playerLevel, playerLevel, 0, action);
		getPush().syncPlayerInfo();
		BehaviorLogger.log4Service(this, Source.ATTR_CHANGE, action,
				Params.valueOf("playerAttr", PlayerAttr.EXP_VALUE),
				Params.valueOf("dec", subExp),
				Params.valueOf("after", newExp),
				Params.valueOf("playerLevel", getLevel()));
	}

	/**
	 * 增加经验
	 */
	public void increaseExp(int exp, Action action, boolean push) {
		if (exp <= 0) {
			logger.error("increaseExp, playerId: {}, add: {}, action: {}", getId(), exp, action.strValue());
			throw new RuntimeException("increaseExp");
		}

		int oldLevel = Math.max(1, getLevel());
		final int maxLevel = PlayerLevelExpCfg.getMaxLevel();

		// 经验加成
		exp *= (1 + getData().getEffVal(EffType.PLAYER_EXP_ADD_PER) * GsConst.EFF_PER);
		int newExp = getExp() + exp;
		if (getExp() > 0 && newExp < 0) {
			newExp = Integer.MAX_VALUE - 1;
		}

		int level = oldLevel;

		for (level++; level <= maxLevel; level++) {
			PlayerLevelExpCfg cfg = HawkConfigManager.getInstance().getConfigByKey(PlayerLevelExpCfg.class, level);
			if (cfg == null) {
				HawkLog.errPrintln("cannot find level exp config, level: {}", level);
				level--;
				break;
			}

			int levelUpNeedExp = cfg.getExp();
			if (newExp < levelUpNeedExp) {
				level--;
				break;
			}

			newExp -= levelUpNeedExp;
		}

		if (level > maxLevel) {
			level = maxLevel;
		}

		getPlayerBaseEntity().setExp(newExp);
		getPlayerBaseEntity().setLevel(level);
		if (level != oldLevel) {
			getPlayerBaseEntity().setLevelUpTime(HawkTime.getMillisecond());
		}
		LogUtil.logPlayerExpFlow(this, exp, oldLevel, level, 0, action);
		Set<Integer> levelSet = AssembleDataManager.getInstance().getPushGiftTriggerParamSet(PushGiftConditionEnum.COMMANDER_EXP);
		if (levelSet.contains(level)) {
			HawkApp.getInstance().postMsg(this, CommanderExpAddMsg.valueOf());
		}

		if (level > oldLevel) {
			onPlayerLevelUp(oldLevel, level);
			GameUtil.scoreBatch(this, ScoreType.PLAYER_LEVEL, level);
			HawkTaskManager.getInstance().postMsg(this.getXid(), PlayerUnlockImageMsg.valueOf(UnlockType.PLAYERLEVEL, level));
			MissionManager.getInstance().postMsg(this, new EventPlayerUpLevel(oldLevel, level));
		} else if (push) {
			getPush().syncPlayerInfo();
		}

		BehaviorLogger.log4Service(this, Source.ATTR_CHANGE, action,
				Params.valueOf("playerAttr", PlayerAttr.EXP_VALUE),
				Params.valueOf("add", exp),
				Params.valueOf("after", newExp),
				Params.valueOf("oldLevel", oldLevel),
				Params.valueOf("newLevel", level));
	}

	/**
	 * 指挥官升级
	 * @param oldLevel
	 * @param finalLevel
	 * @param exp
	 * @param action
	 */
	private void onPlayerLevelUp(int oldLevel, final int finalLevel) {
		// 刷新领主等级排行榜
		updateRankScore(MsgId.PLAYER_LEVELUP_RANK_REFRESH, RankType.PLAYER_GRADE_KEY, finalLevel);
		// 刷新任务
		MissionService.getInstance().refreshPlayerLevelConditionMission(this);
		// 任务操作
		MissionService.getInstance().missionRefresh(this, MissionFunType.FUN_PLAYER_LEVEL, 0, finalLevel - oldLevel);
		// 活动事件
		ActivityManager.getInstance().postEvent(new PlayerLevelUpEvent(getId(), finalLevel));
		refreshPowerElectric(PowerChangeReason.COMMANDER_LVUP);
		// 推送礼包
		HawkTaskManager.getInstance().postMsg(this.getXid(), new CommanderLevlUpMsg(oldLevel, finalLevel));

		StrengthenGuideManager.getInstance().postMsg(new SGCommanderLevlUpMsg(this));
	}

	/**
	 * 玩家监狱可容纳数量
	 */
	public int getMaxCaptiveNum() {
		BuildingCfg cfg = getData().getBuildingCfgByType(BuildingType.PRISON);
		if (cfg != null) {
			return CommanderConstProperty.getInstance().getCaptiveNum();
		}
		return 0;
	}

	public int getMaxMassJoinMarchNum() {
		return getMaxMassJoinMarchNum(false);
	}
	
	/**
	 * 获取玩家发起集结的最大的可参与行军队伍数目
	 */
	public int getMaxMassJoinMarchNum(boolean isNationMarch) {
		int defNum = WorldMarchConstProperty.getInstance().getAssemblyQueueNum();
		defNum += getData().getEffVal(EffType.GUILD_TEAM_MEMBER);
		if (isNationMarch) {
			defNum += getData().getEffVal(EffType.CROSS_EFF_10001);	
		}
		return defNum;
	}

	/**
	 * 集结格子上限,新加
	 */
	public int getMaxMassJoinMarchNum(IWorldMarch march) {
		return getMaxMassJoinMarchNum(march.isNationMassMarch());
	}
	
	/**
	 * 是否是国家行军
	 * @param effParams
	 * @return
	 */
	public int getMaxMarchSoldierNum(EffectParams effParams) {
		return getMaxMarchSoldierNum(effParams, false);
	}
	
	/**
	 * 玩家单次出征最大出兵人口数量
	 */
	public int getMaxMarchSoldierNum(EffectParams effParams, boolean isNationMarch) {
		BuildingCfg cfg = getData().getBuildingCfgByType(BuildingType.FIGHTING_COMMAND);
		double amount = cfg == null ? 0 : cfg.getAttackUnitLimit();

		amount += getData().getPlayerEffect().getEffVal(EffType.TROOP_STRENGTH_NUM, effParams);
		amount *= 1 + getData().getPlayerEffect().getEffVal(EffType.TROOP_STRENGTH_PER, effParams) * GsConst.EFF_PER;
		int effVal210 = getData().getPlayerEffect().getEffVal(EffType.TROOP_STRENGTH_PER_SKILL, effParams);
		int effVal1557 = effVal210 == 0 ? 0 : getData().getPlayerEffect().getEffVal(EffType.HERO_1557, effParams);
		
		amount *= 1 + getData().getPlayerEffect().getEffVal(EffType.TROOP_STRENGTH_PER_ITEM, effParams) * GsConst.EFF_PER
				+ effVal210 * GsConst.EFF_PER + effVal1557 * GsConst.EFF_PER;

		// 浮点数计算可能带来误差，手动向上取整
		long ret = (long) (amount * 1000);
		ret = (ret / 1000) + (ret % 1000 > 0 ? 1 : 0);

		if (isNationMarch) {
			int effVal10002 = getEffect().getEffVal(EffType.CROSS_EFF_10002);
			ret += effVal10002;
		}
		return (int) ret;
	}

	/**
	 * 当前玩家的最大出征队伍数量
	 */
	public int getMaxMarchNum() {
		int defaultCount = WorldMarchConstProperty.getInstance().getWorldMarchBaseNum();
		int effectCount = getData().getEffVal(EffType.MARCH_TROOP_NUM);
		VipSuperCfg cfg = HawkConfigManager.getInstance().getConfigByKey(VipSuperCfg.class, getActivatedVipSuperLevel());
		int superVipCount = cfg != null ? cfg.getTroopTeamNum() : 0;
		return defaultCount + effectCount + superVipCount;
	}

	/**
	 * 获取玩家最大的出兵数量
	 * @return
	 */
	public int getMaxAllMarchSoldierNum(EffectParams effParams) {
		return getMaxMarchNum() * getMaxMarchSoldierNum(effParams);
	}

	/**
	 * 计算大使馆还可以容纳多少人口.
	 */
	public int getMaxAssistSoldier() {
		BuildingCfg buildingCfg = getData().getBuildingCfgByType(BuildingType.EMBASSY);
		int limit = buildingCfg != null ? buildingCfg.getAssistUnitLimit() : 0;
		int effVal = getData().getEffVal(EffType.GUILD_REINFORCE_ARMY);

		return limit + effVal;
	}

	/**
	 * 计算医务所还能容纳伤兵的人口
	 */
	public int getRemainDisabledCap() {
		int woundedTotalLimit = getMaxCapNum();
		int cannonCapMax = getEffect().getEffVal(EffType.PROCTED_CANNON_8_CAP);
		int woundedCount = 0;

		int woundedCannon = 0;
		for (ArmyEntity army : getData().getArmyEntities()) {
			BattleSoldierCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, army.getArmyId());
			if (cfg.isPlantSoldier()) {
				continue;
			}
			
			if (cfg.getSoldierType() == SoldierType.CANNON_SOLDIER_8) {
				woundedCannon += army.getWoundedCount();
			} else {
				woundedCount += army.getWoundedCount();
			}
		}
		if (cannonCapMax >= woundedCannon) {
			return Math.max(0, woundedTotalLimit - woundedCount);
		} else {
			return Math.max(0, woundedTotalLimit - woundedCount - (woundedCannon - cannonCapMax));
		}
	}

	
	/**
	 * 获取泰能医院剩余容量
	 * @return
	 */
	public int getPlantRemainDisabledCap(){
		int woundedTotalLimit = getPlantMaxCapNum();
		int woundedCount = 0;
		for (ArmyEntity army : getData().getArmyEntities()) {
			BattleSoldierCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, army.getArmyId());
			if (cfg.isPlantSoldier()) {
				woundedCount += army.getWoundedCount();
			} 
		}
		return Math.max(0, woundedTotalLimit - woundedCount);
	}
	
	/**
	 * 获取采矿车额外伤兵收纳容量
	 * @return
	 */
	public int getCannonCap() {
		int maxNum = getEffect().getEffVal(EffType.PROCTED_CANNON_8_CAP);
		int woundedCannon = 0;

		for (ArmyEntity army : getData().getArmyEntities()) {
			BattleSoldierCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, army.getArmyId());
			if(cfg.getSoldierType() == SoldierType.CANNON_SOLDIER_8 && !cfg.isPlantSoldier()){
				woundedCannon += army.getWoundedCount();
			}
		}
		return Math.max(0, maxNum - woundedCannon);
	}

	/**
	 * 计算医务所最大容纳伤兵的人口数量
	 */
	public int getMaxCapNum() {
		List<BuildingBaseEntity> hospitals = getData().getBuildingListByType(BuildingType.HOSPITAL_STATION);
		if (hospitals == null || hospitals.size() == 0) {
			return 0;
		}
		int limit = 0;
		for (BuildingBaseEntity building : hospitals) {
			BuildingCfg buildingCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, building.getBuildingCfgId());
			limit += buildingCfg.getWoundedLimit();
		}

		// 计算作用号的影响
		int effVal1 = (int) Math.round(GsConst.EFF_PER * getData().getEffVal(EffType.CITY_HURT_PER) * limit);
		int effVal2 = getData().getEffVal(EffType.CITY_HURT_NUM);

		return limit + effVal1 + effVal2;
	}
	
	
	/**
	 * 获取泰能医院最大容量
	 * @return
	 */
	public int getPlantMaxCapNum(){
		List<BuildingBaseEntity> hospitals = getData().getBuildingListByType(BuildingType.PLANT_HOSPITAL);
		if (hospitals == null || hospitals.size() == 0) {
			return 0;
		}
		int limit = 0;
		for (BuildingBaseEntity building : hospitals) {
			BuildingCfg buildingCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, building.getBuildingCfgId());
			limit += buildingCfg.getWoundedLimit();
		}
		//4101作用号加成
		int add4101 = this.getEffect().getEffVal(EffType.PLANT_SOLDIER_4101);
		limit = limit + add4101;
		return limit;
	}

	/**
	 * 获得市场负重上限
	 */
	public int getMarketBurden() {
		BuildingCfg myCfg = getData().getBuildingCfgByType(BuildingType.TRADE_CENTRE);
		if (myCfg == null) {
			return 0;
		}
		int marketBurden = myCfg.getMarketBurden();
		marketBurden += getData().getEffVal(EffType.GUILD_TRADE_WEIGHT);
		return marketBurden;
	}

	/**
	 * 战斗力刷新，如果有变化推送前台
	 */
	public void refreshPowerElectric(PowerChangeReason reason) {
		refreshPowerElectric(false, reason);
	}

	/**
	 * 战斗力刷新，如果有变化推送前台
	 * @param isArmyCure 是否伤兵治疗完成刷新
	 */
	public void refreshPowerElectric(boolean isArmyCure, PowerChangeReason reason) {
		getData().getPowerElectric().refreshPowerElectric(this, isArmyCure, true, reason);
	}

	/**
	 * 加入联盟
	 */
	@SuppressWarnings("deprecation")
	public void joinGuild(String guildId, boolean isCreate) {
		getData().getStatisticsEntity().addJoinGuildCnt(1);
		GuildMemberObject member = GuildService.getInstance().getGuildMemberObject(getId());
		member.updateJoinGuildTimes(member.getJoinGuildTimes() + 1);
		int joinTimes = member.getJoinGuildTimes();
		if (joinTimes == 1) {
			AwardItems award = AwardItems.valueOf(ConstProperty.getInstance().getFirstJoinGuildReward());
			GuildMailService.getInstance().sendMail(MailParames.newBuilder()
					.setPlayerId(getId())
					.setMailId(MailId.JOIN_GUILD_REWARD)
					.addContents(GameUtil.guild4MailContents(guildId))
					.addRewards(award.getAwardItems())
					.setAwardStatus(MailRewardStatus.NOT_GET)
					.build());
		} else if (joinTimes <= GuildConstProperty.getInstance().getJoinAllianceMailNum()) {
			AwardItems award = AwardItems.valueOf(GuildConstProperty.getInstance().getJoinAllianceMailAward());
			GuildMailService.getInstance().sendMail(MailParames.newBuilder()
					.setPlayerId(getId())
					.setMailId(MailId.JOIN_GUILD_REWARD_OTHER)
					.addContents(GameUtil.guild4MailContents(guildId))
					.addRewards(award.getAwardItems())
					.setAwardStatus(MailRewardStatus.NOT_GET)
					.build());

		}

		// 注册天数小于n天的玩家,入盟会收到教学邮件
		if (getPlayerRegisterDays() <= GuildConstProperty.getInstance().getInitDaySendMail()) {
			GuildMailService.getInstance().sendMail(MailParames.newBuilder()
					.setPlayerId(getId())
					.setMailId(MailId.GAME_REGULAR_TEACHING)
					.build());
			GuildMailService.getInstance().sendMail(MailParames.newBuilder()
					.setPlayerId(getId())
					.setMailId(MailId.GUILD_REGULAR_TEACHING)
					.addContents(GuildService.getInstance().getGuildName(guildId))
					.build());
		}

		// 加入联盟时发送进入联盟的消息(创建时除外)
		if (!isCreate) {
			ChatService.getInstance().addWorldBroadcastMsg(Const.ChatType.CHAT_ALLIANCE, Const.NoticeCfgId.JOIN_GUILD_MSG, this);
		}

		ActivityManager.getInstance().postEvent(new JoinGuildEvent(getId()));
		BehaviorLogger.log4Service(this, Source.GUILD_OPRATION, Action.GUILD_JOIN, Params.valueOf("guildId", getGuildId()));
		// 任务刷新：加入联盟
		MissionManager.getInstance().postMsg(this, new EventGuildJoin(guildId));
		// 联盟当前人数
		int guildMemberNum = GuildService.getInstance().getGuildMemberNum(guildId);
		MissionManager.getInstance().postMsg(this, new EventGuildMemberChange(guildId, guildMemberNum));

		// 排行榜更新入盟的时间
		GuildRankMgr.getInstance().onPlayerJoinGuild(this.getId(), guildId);
	}

	/**
	 * 退出联盟
	 */
	public void quitGuild(String guildId) {
		String playerId = getId();
		msgCall(MsgId.QUIT_GUILD, GuildService.getInstance(), new PlayerQuitGuildInvoker(playerId));
		GlobalData.getInstance().quitGuild(getId(), guildId);
		WorldPlayerService.getInstance().noticeAllianceChange(getId());
		// 退出联盟对联盟排行榜的影响
		GuildRankMgr.getInstance().onPlayerExitGuild(getId(), guildId);
		PlotBattleService.getInstance().playerLeaveGuild(guildId, playerId);

		// 联盟当前人数
		int guildMemberNum = GuildService.getInstance().getGuildMemberNum(guildId);
		MissionManager.getInstance().postMsg(this, new EventGuildMemberChange(guildId, guildMemberNum));
	}

	/** 联盟Id
	 */
	public String getGuildId() {
		String guildId = GuildService.getInstance().getPlayerGuildId(getId());
		GuildInfoObject guild = GuildService.getInstance().getGuildInfoObject(guildId);
		return guild == null ? "" : guild.getId();
	}
	
	public String getGuildNumId() {
		String guildId = GuildService.getInstance().getPlayerGuildId(getId());
		GuildInfoObject guild = GuildService.getInstance().getGuildInfoObject(guildId);
		return guild == null ? "" : guild.getNumTypeId();
	}

	/**
	 * 是否已经加入联盟
	 */
	public boolean hasGuild() {
		return !HawkOSOperator.isEmptyString(getGuildId());
	}

	/**
	 * 获取联盟贡献
	 */
	public long getGuildContribution() {
		return getPlayerBaseEntity().getGuildContribution();
	}

	/**
	 * 消耗联盟贡献
	 */
	public void consumeGuildContribution(int value, Action action) {
		if (value <= 0 || value > getGuildContribution()) {
			logger.error("consumeGuildContribution, playerId: {}, has: {}, cost: {}, action: {}",
					getId(), getGuildContribution(), value, action.strValue());
			throw new RuntimeException("consumeGuildContribution");
		}
		getPlayerBaseEntity().setGuildContribution(getGuildContribution() - value);

		LogUtil.logResourceFlow(this, action, LogInfoType.resource_sub, PlayerAttr.GUILD_CONTRIBUTION_VALUE, getGuildContribution(), value);

		BehaviorLogger.log4Service(this, Source.ATTR_CHANGE, action,
				Params.valueOf("playerAttr", PlayerAttr.GUILD_CONTRIBUTION),
				Params.valueOf("sub", value),
				Params.valueOf("after", getGuildContribution()));

	}

	/**
	 * 获取学院id
	 * @return
	 */
	public String getCollegeId() {
		return getData().getCollegeMemberEntity().getCollegeId();
	}

	/**
	 * 获取玩家学院权限
	 * @return
	 */
	public int getCollegeAuth() {
		return getData().getCollegeMemberEntity().getAuth();
	}

	/**
	 * 是否加入学院
	 * @return
	 */
	public boolean hasCollege() {
		return !HawkOSOperator.isEmptyString(getData().getCollegeMemberEntity().getCollegeId());
	}

	/**
	 * 获取军演积分
	 * @return
	 */
	public long getMilitaryScore() {
		return getPlayerBaseEntity().getGuildMilitaryScore();
	}

	/**
	 * 消耗军演积分
	 * @param value
	 * @param action
	 */
	public void consumeMilitaryScore(long value, Action action) {
		if (value <= 0 || value > getMilitaryScore()) {
			logger.error("consumeMilitaryScore, playerId: {}, has: {}, cost: {}, action: {}",
					getId(), getMilitaryScore(), value, action.strValue());
			throw new RuntimeException("consumeMilitaryScore");
		}
		getPlayerBaseEntity().setGuildMilitaryScore(getMilitaryScore() - value);

		LogUtil.logResourceFlow(this, action, LogInfoType.resource_sub, PlayerAttr.MILITARY_SCORE_VALUE, getMilitaryScore(), value);

		BehaviorLogger.log4Service(this, Source.ATTR_CHANGE, action,
				Params.valueOf("playerAttr", PlayerAttr.MILITARY_SCORE),
				Params.valueOf("sub", value),
				Params.valueOf("after", getMilitaryScore()));
	}

	/**
	 * 获取赛博积分
	 * @return
	 */
	public long getCyborgScore() {
		return getPlayerBaseEntity().getCyborgScore();
	}

	/**
	 * 消耗赛博积分
	 * @param value
	 * @param action
	 */
	public void consumeCyborgScore(long value, Action action) {
		if (value <= 0 || value > getCyborgScore()) {
			logger.error("consumeCyborgScore, playerId: {}, has: {}, cost: {}, action: {}",
					getId(), getCyborgScore(), value, action.strValue());
			throw new RuntimeException("consumeCyborgScore");
		}
		getPlayerBaseEntity().setCyborgScore(getCyborgScore() - value);

		LogUtil.logResourceFlow(this, action, LogInfoType.resource_sub, PlayerAttr.CYBORG_SCORE_VALUE, getCyborgScore(), value);

		BehaviorLogger.log4Service(this, Source.ATTR_CHANGE, action,
				Params.valueOf("playerAttr", PlayerAttr.CYBORG_SCORE),
				Params.valueOf("sub", value),
				Params.valueOf("after", getCyborgScore()));
	}
	
	
	/**
	 * 获取达雅积分
	 * @return
	 */
	public long getDYZZScore() {
		return getPlayerBaseEntity().getDyzzScore();
	}

	/**
	 * 消耗达雅积分
	 * @param value
	 * @param action
	 */
	public void consumeDYZZScore(long value, Action action) {
		if (value <= 0 || value > getDYZZScore()) {
			logger.error("consumeDYZZScore, playerId: {}, has: {}, cost: {}, action: {}",
					getId(), getDYZZScore(), value, action.strValue());
			throw new RuntimeException("consumeDYZZScore");
		}
		getPlayerBaseEntity().setDyzzScore(getDYZZScore() - value);

		LogUtil.logResourceFlow(this, action, LogInfoType.resource_sub, PlayerAttr.DYZZ_SCORE_VALUE, getDYZZScore(), value);

		BehaviorLogger.log4Service(this, Source.ATTR_CHANGE, action,
				Params.valueOf("playerAttr", PlayerAttr.DYZZ_SCORE),
				Params.valueOf("sub", value),
				Params.valueOf("after", getDYZZScore()));
	}

	/** 联盟名称
	 */
	public String getGuildName() {
		return GuildService.getInstance().getGuildName(getGuildId());
	}

	/** 联盟简称
	 */
	public String getGuildTag() {
		return Optional.ofNullable(GuildService.getInstance().getGuildTag(getGuildId())).orElse("");
	}

	/**
	 * 获得联盟权限
	 */
	public int getGuildAuthority() {
		if (!hasGuild()) {
			return Const.GuildAuthority.L0_VALUE;
		}
		return GuildService.getInstance().getPlayerGuildAuthority(getId());
	}

	/**
	 * 获得联盟旗帜
	 */
	public int getGuildFlag() {
		return GuildService.getInstance().getGuildFlag(getGuildId());
	}
	
	/**
	 * 获取盟主名称
	 * @return
	 */
	public String getGuildLeaderName(){
		return GuildService.getInstance().getGuildLeaderName(getGuildId());
	}
	
	/**
	 * 获取盟主ID
	 * @return
	 */
	public String getGuildLeaderId(){
		return GuildService.getInstance().getGuildLeaderId(getGuildId());
	}

	/**
	 * 获取免费建造时间
	 * @return
	 */
	public int getFreeBuildingTime() {
		int freeTime = ConstProperty.getInstance().getFreeTime();
		freeTime += getData().getEffVal(EffType.CITY_BUILD_FREE_TIME);
		return freeTime;
	}

	/**
	 * 获取免费科技研究时间
	 * @return
	 */
	public int getFreeTechTime() {
		int freeTime = ConstProperty.getInstance().getScienceFreeTime();
		freeTime += getData().getEffVal(EffType.CITY_TECH_FREE_TIME);
		return freeTime;
	}

	/**
	 * 解锁建筑区块
	 *  @param buildAreaId 区块id
	 */
	public void unlockArea(int buildAreaId) {
		getData().getPlayerBaseEntity().addUnlockedArea(buildAreaId);
	}

	/**
	 * 判断该玩家是否处于零收益状态
	 * @return
	 */
	public boolean isZeroEarningState() {
		return getZeroEarningTime() > HawkApp.getInstance().getCurrentTime();
	}

	/**
	 * 获取零收益状态结束时间
	 * @return
	 */
	public long getZeroEarningTime() {
		return getEntity().getZeroEarningTime();
	}

	/**
	 * 获取上一次军队消耗石油的时间
	 * @return
	 */
	public long getOilConsumeTime() {
		return getEntity().getOilConsumeTime();
	}

	/**
	 * 发送IDIP消息(非登录时调用此接口)
	 */
	public void sendIDIPZeroEarningMsg() {
		sendIDIPZeroEarningMsg(-1);
	}

	/**
	 * 发送IDIP消息
	 * 
	 * @param msgCode 只有登录的时候才传此参数
	 */
	public void sendIDIPZeroEarningMsg(int msgCode) {
		// 登录时才成立
		if (msgCode >= 0) {
			zeroEarningState = true;
		}

		// 已经处于非零收益状态了, 此处不能通过调用isZeroEarningState方法来判断
		if (!zeroEarningState) {
			return;
		}

		long endTime = getZeroEarningTime();
		// 零收益状态结束了
		if (msgCode < 0 && endTime <= HawkTime.getMillisecond()) {
			relieveBanRank();
			zeroEarningState = false;
			endTime = 0;
			msgCode = IdipMsgCode.ZERO_EARNING_RELEASE_VALUE;
		}

		msgCode = msgCode >= 0 ? msgCode : IdipMsgCode.ZERO_EARNING_VALUE;
		sendIdipNotice(NoticeType.ZERO_EARNING, NoticeMode.MSG_BOX, endTime, msgCode);
	}

	/**
	 * 禁止排行榜解除
	 */
	private void relieveBanRank() {
		String playerId = getId();
		RedisProxy.getInstance().removeIDIPBanInfo(playerId, IDIPBanType.BAN_ZERO_EARNING);
		RankService.getInstance().dealMsg(MsgId.PERSONAL_RANK_UNBAN, new UnbanPersonalRankMsgInvoker(playerId));
	}

	/**
	 * 取得英雄 按照配置文件ID
	 */
	public Optional<PlayerHero> getHeroByCfgId(int heroId) {
		return getData().getHeroEntityList().stream().filter(h -> h.getHeroId() == heroId).map(HeroEntity::getHeroObj).filter(Objects::nonNull).findFirst();
	}

	/**
	 * 获得商店
	 * @param shopId 商店id
	 * @return 商店
	 */
	public Optional<PlayerShopEntity> getShopById(int shopId) {
		return getData().getShopEntityList().stream().filter(s -> s.getShopId() == shopId).findFirst();
	}

	/**
	 * 取得超级兵按照配置文件ID
	 */
	public Optional<SuperSoldier> getSuperSoldierByCfgId(int soldierId) {
		return getAllSuperSoldier().stream().filter(e -> e.getCfgId() == soldierId).findAny();
	}

	/**
	 * 取得英雄 按照配置文件ID
	 */
	public List<PlayerHero> getHeroByCfgId(List<Integer> heroIdList) {
		return getData().getHeroEntityByCfgId(heroIdList).stream().map(HeroEntity::getHeroObj).collect(Collectors.toList());
	}

	/** 泰能战士研究院*/
	public PlantSoldierSchool getPlantSoldierSchool() {
		return getData().getPlantSoldierSchoolEntity().getPlantSchoolObj();
	}

	/**
	 * 所有英雄 
	 */
	public List<PlayerHero> getAllHero() {
		List<PlayerHero> result = getData().getHeroEntityList().stream()
				.map(HeroEntity::getHeroObj)
				.filter(Objects::nonNull)
				.collect(Collectors.toList());
		return result;
	}

	/**
	 * 所有超级兵
	 */
	public List<SuperSoldier> getAllSuperSoldier() {
		List<SuperSoldier> result = getData().getSuperSoldierEntityList().stream()
				.map(SuperSoldierEntity::getSoldierObj)
				.collect(Collectors.toList());
		return result;
	}
	
	/**
	 * 获取超武聚能底座
	 * @return
	 */
	public PlayerManhattan getManhattanBase() {
		Optional<PlayerManhattan> optional = getData().getManhattanEntityList().stream().filter(e -> e.getBase() > 0).map(ManhattanEntity::getManhattanObj).findAny();
		if (optional.isPresent()) {
			return optional.get();
		}
		return null;
	}
	
	/**
	 * 获取所有超级武器
	 * @return
	 */
	public List<PlayerManhattan> getAllManhattanSW() {
		List<PlayerManhattan> result = getData().getManhattanEntityList().stream()
				.filter(e -> e.getBase() <= 0)
				.map(ManhattanEntity::getManhattanObj)
				.collect(Collectors.toList());
		return result;
	}
	
	/**
	 * 获取指定配置id的超级武器数据
	 * @param cfgId
	 * @return
	 */
	public PlayerManhattan getManhattanSWByCfgId(int cfgId) {
		Optional<PlayerManhattan> optional = getData().getManhattanEntityList().stream().filter(e -> e.getSwId() == cfgId).map(ManhattanEntity::getManhattanObj).findAny();
		if (optional.isPresent()) {
			return optional.get();
		}
		return null;
	}
	
	/**
	 * 判断指定配置id的超级武器
	 * @param desiredIds
	 * @return
	 */
	public boolean manhattanSWContains(Set<Integer> desiredIds) {
		Set<Integer> availableSwIds = getData().getManhattanEntityList().stream().map(e -> e.getSwId())
				.collect(Collectors.toSet());
		return availableSwIds.containsAll(desiredIds);
	}
	/**
	 * 获取机甲核心数据
	 * @return
	 */
	public PlayerMechaCore getPlayerMechaCore() {
		return getData().getMechaCoreEntity().getMechaCoreObj();
	}
	
	/**
	 * 获取机甲核心的指定模块
	 * @param moduleUuid
	 * @return
	 */
	public MechaCoreModuleEntity getMechaCoreModuleEntity(String moduleUuid) {
		if (HawkOSOperator.isEmptyString(moduleUuid)) {
			return null;
		}
		Optional<MechaCoreModuleEntity> optional = getData().getMechaCoreModuleEntityList().stream().filter(e -> e.getId().equals(moduleUuid)).findAny();
		if (optional.isPresent()) {
			return optional.get();
		}
		return null;
	}

	/**
	 * 快速产出资源
	 * 
	 * @param timeLong 时长
	 * @param safe 是否产出安全资源
	 */
	public void productResourceQuickly(long timeLong, boolean safe) {
		List<BuildingBaseEntity> resBuildings = getData().getBuildingListByLimitType(BuildingCfg.resBuildingLimitTypes());
		AwardItems award = AwardItems.valueOf();
		// 遍历所有资源建筑
		for (BuildingBaseEntity entity : resBuildings) {
			BuildingCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, entity.getBuildingCfgId());
			if (cfg == null || !cfg.isResBuilding()) {
				continue;
			}

			collectResource(entity.getId(), entity.getBuildingCfgId(), timeLong, award, safe);
		}

		if (award.hasAwardItem()) {
			award.rewardTakeAffectAndPush(this, Action.BUILDING_COLLECT_RES);
		}
	}

	/**
	 * 产出资源
	 * 
	 * @param buildingId
	 * @param buildCfgId 
	 * @param timeLong 收取该时长产出的资源
	 * @param award 收取的资源先以奖励的形式存下来
	 * @param safe 是否产出安全资源
	 * 
	 * @return
	 */
	public boolean collectResource(String buildingId, int buildCfgId, long timeLong, AwardItems award, boolean safe) {
		BuildingCfg buildCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, buildCfgId);
		if (!buildCfg.isResBuilding()) {
			return false;
		}

		// 资源产出
		final long product = GameUtil.resStore(this, buildingId, buildCfg, timeLong);

		double[] arr = new double[4];
		switch (buildCfg.getBuildType()) {
		case BuildingType.ORE_REFINING_PLANT_VALUE:
			arr[0] = product;
			break;
		case BuildingType.OIL_WELL_VALUE:
			arr[1] = product;
			break;
		case BuildingType.STEEL_PLANT_VALUE:
			arr[2] = product;
			break;
		case BuildingType.RARE_EARTH_SMELTER_VALUE:
			arr[3] = product;
			break;
		default:
			break;
		}

		if (product > 0) {
			addRes(arr[0], arr[1], arr[2], arr[3], award, safe);
		}

		return true;
	}

	/**
	 * 添加资源
	 * @param addTotalOre   黄金
	 * @param addTotalOil   石油
	 * @param addTotalSteel 铀矿
	 * @param addTotalRare  合金
	 * @param award 资源存储
	 */
	public void addRes(double addTotalOre, double addTotalOil, double addTotalSteel, double addTotalRare, AwardItems award, boolean safe) {
		if (safe) {
			addSafeRes(addTotalOre, addTotalOil, addTotalSteel, addTotalRare, award);
			return;
		}

		addRes(addTotalOre, PlayerAttr.GOLDORE_UNSAFE_VALUE, award);
		addRes(addTotalOil, PlayerAttr.OIL_UNSAFE_VALUE, award);
		addRes(addTotalSteel, PlayerAttr.STEEL_UNSAFE_VALUE, award);
		addRes(addTotalRare, PlayerAttr.TOMBARTHITE_UNSAFE_VALUE, award);

		// 活动事件
		CityResourceCollectEvent event = new CityResourceCollectEvent(getId());
		event.addCollectResource(PlayerAttr.GOLDORE_UNSAFE_VALUE, addTotalOre);
		event.addCollectResource(PlayerAttr.OIL_UNSAFE_VALUE, addTotalOil);
		event.addCollectResource(PlayerAttr.STEEL_UNSAFE_VALUE, addTotalSteel);
		event.addCollectResource(PlayerAttr.TOMBARTHITE_UNSAFE_VALUE, addTotalRare);
		ActivityManager.getInstance().postEvent(event);
	}

	/**
	 * 添加安全资源
	 * 
	 * @param addTotalOre
	 * @param addTotalOil
	 * @param addTotalSteel
	 * @param addTotalRare
	 * @param award
	 */
	public void addSafeRes(double addTotalOre, double addTotalOil, double addTotalSteel, double addTotalRare, AwardItems award) {
		addRes(addTotalOre, PlayerAttr.GOLDORE_VALUE, award);
		addRes(addTotalOil, PlayerAttr.OIL_VALUE, award);
		addRes(addTotalSteel, PlayerAttr.STEEL_VALUE, award);
		addRes(addTotalRare, PlayerAttr.TOMBARTHITE_VALUE, award);

		// 活动事件
		CityResourceCollectEvent event = new CityResourceCollectEvent(getId());
		event.addCollectResource(PlayerAttr.GOLDORE_VALUE, addTotalOre);
		event.addCollectResource(PlayerAttr.OIL_VALUE, addTotalOil);
		event.addCollectResource(PlayerAttr.STEEL_VALUE, addTotalSteel);
		event.addCollectResource(PlayerAttr.TOMBARTHITE_VALUE, addTotalRare);
		ActivityManager.getInstance().postEvent(event);
	}

	/**'
	 * 添加资源
	 * @param addCount
	 * @param resType
	 * @param award
	 */
	private void addRes(double addCount, int resType, AwardItems award) {
		if (addCount <= 0) {
			return;
		}

		int add = (int) Math.ceil(addCount);
		award.addItem(ItemType.PLAYER_ATTR_VALUE, resType, add);
		MissionService.getInstance().missionRefresh(this, MissionFunType.FUN_RESOURCE_TYPE_NUMBER, resType, add);
		MissionManager.getInstance().postMsg(this, new EventResourceProduction(resType, add));
	}

	/**
	 * 取得指定英雄身上的做用号
	 */
	public int getHeroEffectValue(int heroId, EffType eType) {
		Optional<PlayerHero> heroOP = getHeroByCfgId(heroId);
		if (heroOP.isPresent()) {
			return heroOP.get().getBattleEffect(eType,EffectParams.getDefaultVal());
		}
		return 0;
	}

	/**
	 * 获取玩家已经使用过的技能id列表
	 * @return
	 */
	public List<Integer> getCastedSkill() {
		List<Integer> retList = new ArrayList<>();
		retList.addAll(getData().castedTalentSkill());
		return retList;
	}

	/**
	 * 刷新排行榜
	 * @param msgId
	 * @param rankType
	 * @param score
	 */
	public void updateRankScore(int msgId, RankType rankType, long score) {
		if (GameUtil.isNpcPlayer(getId())) {
			return;
		}

		// 跨服状态的玩家不发送
		if (isInDungeonMap()) {
			return;
		}

		// csPlayer就代表的是一个跨服玩家.
		if (this.isCsPlayer()) {
			return;
		}
		RankService.getInstance().dealMsg(msgId, new UpdateRankScoreMsgInvoker(this, rankType, score));
	}
    
    public int getMechaCoreRankLevel() {
    	return getPlayerMechaCore().getRankLevel();
    }

	/**
	 * 移除技能buff
	 * @param skillId
	 */
	public void removeSkillBuff(int skillId) {
		ITalentSkill skill = TalentSkillContext.getInstance().getSkill(skillId);
		if (skill != null) {
			skill.removeSkillBuff(this);
		}
	}

	/**
	 * 添加指定结束时间的buff
	 * @param buffId
	 * @param endTime
	 * @return
	 */
	public StatusDataEntity addStatusBuff(int buffId, long endTime) {
		StatusDataEntity entity = getData().addStatusBuff(buffId, null, endTime);
		// buff处理
		if (isActiveOnline()) {
			onBufChange(entity.getStatusId(), entity.getEndTime());
		}

		return entity;
	}

	/**
	 * 使用道具添加buff
	 * @param buffId
	 * @return
	 */
	public StatusDataEntity addStatusBuff(int buffId) {
		return addStatusBuff(buffId, null);
	}

	/**
	 * 使用道具添加buff
	 * @param buffId
	 * @param targetId buff作用对象
	 * @return
	 */
	public StatusDataEntity addStatusBuff(int buffId, String targetId) {
		StatusDataEntity entity = getData().addStatusBuff(buffId, targetId);
		// buff处理
		if (isActiveOnline()) {
			onBufChange(entity.getStatusId(), entity.getEndTime());
		}

		return entity;
	}

	/**
	 * 删除城市保护buff
	 */
	public void removeCityShield() {
		if (getData().getCityShieldTime() <= HawkTime.getMillisecond()) {
			return;
		}

		// 护盾消失发送邮件
		SystemMailService.getInstance().sendMail(MailParames.newBuilder()
				.setPlayerId(getId())
				.setMailId(MailId.CITY_SHIELD_REMOVE)
				.build());

		// 去除城点的保护状态
		StatusDataEntity entity = getData().removeCityShield();
		WorldPlayerService.getInstance().updateWorldPointProtected(getId(), entity.getEndTime());
		cityShieldStatusClear(entity);
	}

	/**
	 * 删除城点保护罩后改变状态数据
	 * @param entity
	 */
	private void cityShieldStatusClear(StatusDataEntity entity) {
		entity.setInitiative(true);
		// 统计+1
		StatisticsEntity statisticsEntity = getData().getStatisticsEntity();
		statisticsEntity.addAtkInProtectCnt(1);
		// 同步buff增益效果显示
		if (isActiveOnline()) {
			getPush().syncPlayerStatusInfo(false, entity);
			getPush().syncPlayerEffect(EffType.CITY_SHIELD);
		}
	}

	/**
	 * 城防兵 (地雷,陷阱...)
	 * 
	 * @return
	 */
	public List<ArmyEntity> defArmy() {
		ConfigIterator<BattleSoldierCfg> it = HawkConfigManager.getInstance().getConfigIterator(BattleSoldierCfg.class);
		Set<Integer> weaponIds = it.stream().filter(BattleSoldierCfg::isDefWeapon).map(BattleSoldierCfg::getId).collect(Collectors.toSet());
		List<ArmyEntity> defSoldiers = this.getData().getArmyEntities().stream().filter(s -> weaponIds.contains(s.getArmyId())).collect(Collectors.toList());
		return defSoldiers;
	}

	/**
	 * 行军部队 (大兵,坦克,飞机...)
	 * 
	 * @return
	 */
	public List<ArmyEntity> marchArmy() {
		List<ArmyEntity> result = new ArrayList<>(this.getData().getArmyEntities());
		result.removeAll(defArmy());
		return result;
	}

	public boolean isRobot() {
		return GsConfig.getInstance().isDebug() && this.getPuid().startsWith("robot") && GsConfig.getInstance().isRobotMode();
	}

	/**
	 * 获得聊天屏蔽的玩家
	 * @return
	 */
	public Set<String> getShieldPlayers() {
		return new HashSet<>(getData().getShieldPlayers());
	}

	/**
	 * 添加聊天屏蔽玩家
	 * @param shieldPlayerId
	 */
	public void addShieldPlayer(String shieldPlayerId) {
		getData().getShieldPlayers().add(shieldPlayerId);
	}

	/**
	 * 删除聊天屏蔽玩家
	 * @param shieldPlayerId
	 */
	public void removeShieldPlayer(String shieldPlayerId) {
		getData().getShieldPlayers().remove(shieldPlayerId);
	}

	/**
	 * 获取已开放的资源类型
	 * @return
	 */
	public List<Integer> getUnlockedResourceType() {
		List<Integer> resTypes = new ArrayList<>();
		int count = getData().getBuildingNumLimit(LimitType.LIMIT_TYPE_BUIDING_STEEL_VALUE);
		if (count > 0) {
			resTypes.add(PlayerAttr.STEEL_UNSAFE_VALUE);
		}

		count = getData().getBuildingNumLimit(LimitType.LIMIT_TYPE_BUIDING_ORE_REFINING_VALUE);
		if (count > 0) {
			resTypes.add(PlayerAttr.GOLDORE_UNSAFE_VALUE);
		}

		count = getData().getBuildingNumLimit(LimitType.LIMIT_TYPE_BUIDING_OIL_WELL_VALUE);
		if (count > 0) {
			resTypes.add(PlayerAttr.OIL_UNSAFE_VALUE);
		}

		count = getData().getBuildingNumLimit(LimitType.LIMIT_TYPE_BUIDING_RARE_EARTH_VALUE);
		if (count > 0) {
			resTypes.add(PlayerAttr.TOMBARTHITE_UNSAFE_VALUE);
		}

		return resTypes;
	}

	/**
	 * 判断第二建造队列是否已开启
	 * @return
	 */
	public boolean isSecondaryBuildQueueUsable() {
		if (getData().isSecondBuildUnlock()) {
			return true;
		} 
		
		QueueEntity queue = getData().getPaidQueue();
		if (queue != null && queue.getEnableEndTime() > HawkTime.getMillisecond()) {
			return true;
		}

		return false;
	}

	@MessageHandler
	public void refreshEffectEvent(RefreshEffectMsg msg) {
		this.getPush().syncPlayerEffect(EffType.valueOf(msg.getEffectId()));
	}

	/**
	 * 刷新vip福利礼包
	 */
	public void refreshVipBenefitBox() {
		boolean taken = RedisProxy.getInstance().getVipBoxStatus(getId(), 0);
		VipCfg vipCfg = HawkConfigManager.getInstance().getConfigByKey(VipCfg.class, getVipLevel());
		if (vipCfg != null && !taken) {
			MailParames.Builder mailParames = MailParames.newBuilder().setMailId(MailId.VIP_BENEFIT_BOX_REFRESH)
					.setPlayerId(getId()).setRewards(vipCfg.getVipBenefitItems()).setAwardStatus(MailRewardStatus.NOT_GET);
			SystemMailService.getInstance().sendMail(mailParames.build());
		}

		RedisProxy.getInstance().batchUpdateVipBenefitRefreshInfo(getId());
		logger.info("refreshVipBenefitBox crossDay, playerId: {}, vipLevel: {}", getId(), getVipLevel());

		getPush().syncVipBoxStatus(0, false);
	}

	/**
	 * 拉取账户余额
	 * @return
	 */
	public int checkBalance() {
		return MidasService.getInstance().checkBalance(this, pfTokenJson);
	}
	
	/**
	 * 充值成功处理
	 * @param playerSaveAmt 历史累计充值钻石数（不含赠送部分）
	 * @param rechargeAmt   充值钻石数量（不含赠送部分）
	 * @param diamonds      充值前拥有的钻石数
	 */
	public void rechargeSuccess(int playerSaveAmt, int rechargeAmt, int diamonds) {
		MidasService.getInstance().rechargeSuccess(this, playerSaveAmt, rechargeAmt, diamonds);
	}

	/**
	 * 消费支付
	 * @param diamond
	 */
	public String pay(int diamond, String actionName, List<PayItemInfo> payItems) {
		return MidasService.getInstance().pay(this, diamond, actionName, payItems);
	}

	/**
	 * 取消支付
	 * @param diamond
	 * @param billno
	 * @return
	 */
	public boolean cancelPay(int diamond, String billno) {
		return MidasService.getInstance().cancelPay(this, diamond, billno);
	}

	/**
	 * 货币赠送
	 * @param diamond
	 * @param extendParam
	 * @return
	 */
	public int present(int diamond, String extendParam, String actionName, String presentReason) {
		return MidasService.getInstance().present(this, diamond, extendParam, actionName, presentReason);
	}

	/**
	 * 道具直购
	 * @param giftCfg
	 * @return
	 */
	public String payBuyItems(PayGiftCfg giftCfg) {
		return MidasService.getInstance().payBuyItems(this, giftCfg);
	}


	/**
	 * 是否为锁定状态
	 * 
	 * @return
	 */
	public boolean isLocker() {
		return isLocker;
	}

	/**
	 * 设定登陆状态
	 * 
	 * @param isLocker
	 */
	public void setLocker(boolean isLocker) {
		this.isLocker = isLocker;
	}

	/**
	 * 同步玩家状态用于迁服
	 * @param status
	 */
	public void synPlayerStatus(PlayerStatus status) {
		this.synPlayerStatus(status, Status.SysError.SUCCESS_OK_VALUE);
	}

	/**
	 * 同步玩家状态用于迁服
	 * @param status
	 */
	public void synPlayerStatus(PlayerStatus status, int errorCode) {
		PLAYER_STATUS_SYN.Builder sbuilder = PLAYER_STATUS_SYN.newBuilder();
		sbuilder.setStatus(status);
		sbuilder.setErrorCode(errorCode);

		this.sendProtocol(HawkProtocol.valueOf(HP.code.PLAYER_STATUS_SYN_VALUE, sbuilder));
	}

	/**
	 * 获取玩家所有的序列化信息
	 * @return
	 */
	public PlayerSerializeData getPlayerSerializeData() {
		PlayerSerializeData playerSerializeData = new PlayerSerializeData();

		// 玩家数据处理
		JSONObject playerDataJson = PlayerSerializer.serializePlayerData(getData(), new LinkedList<>());
		playerSerializeData.setPlayerData(playerDataJson);

		// 只 序列化好友部分
		Map<String, PlayerRelationEntity> relationMap = RelationService.getInstance().getPlayerRelationMap(this.getId());
		List<PlayerRelationEntity> entityList = new ArrayList<>(relationMap.values());
		if (!entityList.isEmpty()) {
			// Entity 里面有个Automic 类会导致直接序列化有问题,所以采用以下这种方式
			JSONArray relationArray = new JSONArray();
			try {
				for (PlayerRelationEntity entity : entityList) {
					relationArray.add(HawkEntitySerializer.serialize(entity));
				}

				playerSerializeData.setRelationList(relationArray);
			} catch (Exception e) {
				HawkException.catchException(e, "serial relation entity error");
			}
		}

		// 活动数据
		PlayerActivityData activityData = PlayerDataHelper.getInstance().getPlayerData(getId(), false);
		if (activityData != null) {
			JSONObject activityJson = PlayerActivityDataSerialize.serializePlayerActivityData(activityData, new ArrayList<>());
			playerSerializeData.setActivityData(activityJson);
		}

		return playerSerializeData;
	}

	/**
	 * 获取玩家的基本信息JSON Str
	 * @return
	 */
	public String toAnchorJsonStr() {
		return toAnchorJsonObj().toJSONString();
	}

	public JSONObject toAnchorJsonObj() {
		JSONObject json = new JSONObject();
		json.put(String.valueOf(Anchor.PlayerInfoRedisKey.PLAYERID_VALUE), getId());
		json.put(String.valueOf(Anchor.PlayerInfoRedisKey.PLAYERNAME_VALUE), getName());
		json.put(String.valueOf(Anchor.PlayerInfoRedisKey.SERVERID_VALUE), GsConfig.getInstance().getServerId());
		json.put(String.valueOf(Anchor.PlayerInfoRedisKey.ICON_VALUE), getIcon());
		json.put(String.valueOf(Anchor.PlayerInfoRedisKey.PFICON_VALUE), getPfIcon());
		json.put(String.valueOf(Anchor.PlayerInfoRedisKey.VIP_VALUE), getVipLevel());
		json.put(String.valueOf(Anchor.PlayerInfoRedisKey.PLAYERLEVEL_VALUE), getLevel());
		json.put(String.valueOf(Anchor.PlayerInfoRedisKey.AREAID_VALUE), GsConfig.getInstance().getAreaId());
		json.put(String.valueOf(Anchor.PlayerInfoRedisKey.OPENID_VALUE), getOpenId());
		json.put(String.valueOf(Anchor.PlayerInfoRedisKey.PLATID_VALUE), getPlatId());
		json.put(String.valueOf(Anchor.PlayerInfoRedisKey.CITYLEVEL_VALUE), getCityLevel());

		json.put(String.valueOf(Anchor.PlayerInfoRedisKey.GAMEAPPID_VALUE), getGameAppId());
		json.put(String.valueOf(Anchor.PlayerInfoRedisKey.CHANNEL_VALUE), getChannel());
		json.put(String.valueOf(Anchor.PlayerInfoRedisKey.ZONEAREAID_VALUE), getServerId());
		json.put(String.valueOf(Anchor.PlayerInfoRedisKey.POWER_VALUE), getPower());
		json.put(String.valueOf(Anchor.PlayerInfoRedisKey.PFICONURL_VALUE), playerData.getPrimitivePfIcon());

		if (this.hasGuild()) {
			json.put(String.valueOf(Anchor.PlayerInfoRedisKey.GUILDINFO_VALUE), getGuildTag() + "," + getGuildName());
			json.put(String.valueOf(Anchor.PlayerInfoRedisKey.GUILDID_VALUE), getGuildId());
		}

		return json;
	}

	/**
	 * 更新在线时长提醒时间点
	 */
	public void updateRemindTime() {
		if (!GlobalData.getInstance().isHealthGameEnable() || !HealthCfg.getInstance().isHealthGameRemind()) {
			return;
		}

		long loginTime = getLoginTime();
		int[] timeArray = GlobalData.getInstance().getHealthGameConf().getOnceGameRestTimeArray(isAdult());
		long maxTime = timeArray[timeArray.length - 1] * 1000;
		if (getNextRemindTime() - loginTime >= maxTime) {
			loginTime += ((getNextRemindTime() - loginTime) / maxTime) * maxTime;
		}

		for (int time : timeArray) {
			long nextTime = loginTime + time * 1000;
			if (nextTime > getNextRemindTime()) {
				setNextRemindTime(nextTime);
				HawkLog.debugPrintln("player update next health remind time, playerId: {}, nextTime: {}", getId(), HawkTime.formatTime(nextTime));
				break;
			}
		}
	}

	/**
	 * 健康游戏信息提醒
	 * @param userInfo
	 */
	public void healthGameRemind(UpdateUserInfoResult userInfo) {
		// 服务器向客户端推送健康引导信息开关标识（不包含中控提醒）
		if (!HealthCfg.getInstance().isHealthGameRemind()) {
			return;
		}

		int healthGameFlag = userInfo.getHealthy_game_flag();
		switch (healthGameFlag) {
		// 通知前端弹窗提示休息
		case HealthGameFlag.REMIND_REST: {
			sendHealthGameRemind(ReportType.ACCUMULATE_TIME_LONG_VALUE, userInfo.getAccumu_time(), 0, 0);
			break;
		}
		// 通知前端强制下线休息
		case HealthGameFlag.FORCE_EXIT: {
			int restTime = userInfo.getForce_exit_rest_time();
			int accumulateTime = userInfo.getAccumu_time();
			sendHealthGameRemind(ReportType.ACCUMULATE_FORCE_EIXT_VALUE, accumulateTime, restTime, 0);
			break;
		}
		// 宵禁
		case HealthGameFlag.GAME_CURFEW: {
			long endTime = userInfo.getCurfew_end_time() * 1000L;
			sendHealthGameRemind(ReportType.CURFEW_GAME_VALUE, 0, 0, endTime);
			break;
		}
		// 禁玩
		case HealthGameFlag.GAME_BAN: {
			long endTime = userInfo.getBan_end_time() * 1000L;
			sendHealthGameRemind(ReportType.BAN_GAME_VALUE, 0, 0, endTime);
			break;
		}
		default:
			break;
		}
	}

	/**
	 * 发送健康游戏弹窗提示
	 * @param type
	 * @param peroidTime
	 * @param restTime
	 */
	public void sendHealthGameRemind(int type, int peroidTime, int restTime, long endTime) {
		if (!HealthCfg.getInstance().isHealthGameRemind()) {
			return;
		}

		HealthGameRemindPB.Builder builder = HealthGameRemindPB.newBuilder();
		builder.setType(ReportType.valueOf(type));
		if (peroidTime > 0) {
			builder.setPeriodTime(peroidTime);
		}

		if (restTime > 0) {
			builder.setRestTime(restTime);
		}

		if (endTime > 0) {
			builder.setEndTime(endTime);
		}

		HawkLog.debugPrintln("notify client health remind, playerId: {}, type: {}, periodTime: {}, restTime: {}, endTime: {}",
				getId(), ReportType.valueOf(type), peroidTime, restTime, HawkTime.formatTime(endTime));

		sendProtocol(HawkProtocol.valueOf(HP.code.HEALTH_GAME_REMIND_S, builder));
	}

	/**
	 * 中控接口调用
	 * 
	 * @param type
	 * @param endTime
	 * @param zkTitle
	 * @param zkMsg
	 * @param zkTraceId
	 * @param jsonStr
	 */
	public void sendHealthGameRemind(int type, long endTime, String zkTitle, String zkMsg, String zkTraceId, String jsonStr) {
		HealthGameRemindPB.Builder builder = HealthGameRemindPB.newBuilder();
		builder.setType(ReportType.valueOf(type));
		if (!HawkOSOperator.isEmptyString(zkTitle)) {
			builder.setZkTitle(zkTitle);
		}

		if (!HawkOSOperator.isEmptyString(zkMsg)) {
			builder.setZkMsg(zkMsg);
		}

		if (!HawkOSOperator.isEmptyString(zkTraceId)) {
			builder.setZkTraceId(zkTraceId);
		}

		if (!HawkOSOperator.isEmptyString(jsonStr)) {
			builder.setZkType(ZKType.ZK_VERIFY);
			builder.setJsonStr(jsonStr);
		} else if (endTime > 0) {
			builder.setZkType(ZKType.ZK_BAN);
			builder.setEndTime(endTime);
		} else {
			builder.setZkType(ZKType.ZK_REMIND);
		}

		HawkLog.debugPrintln("notify client health remind, playerId: {}, type: {}, endTime: {}, zkTitle: {}, zkMsg: {}, zkTraceId: {}, jsonStr: {}",
				getId(), ReportType.valueOf(type), HawkTime.formatTime(endTime), zkTitle, zkMsg, zkTraceId, jsonStr);

		sendProtocol(HawkProtocol.valueOf(HP.code.HEALTH_GAME_REMIND_S, builder));
	}

	/**
	 * 增加军衔经验(功勋)
	 * @param add
	 */
	public void increaseMilitaryRankExp(int add, Action action) {
		if (add < 0) {
			throw new RuntimeException("increase military rank exp error !");
		}

		int beforeExp = getEntity().getMilitaryExp();
		int afterExp = Math.min(beforeExp + add, AssembleDataManager.getInstance().getMaxMilitaryRankExp());
		int beforeLevel = GameUtil.getMilitaryRankByExp(beforeExp);
		int afterLevel = GameUtil.getMilitaryRankByExp(afterExp);

		// 设置更改后经验
		getEntity().setMilitaryExp(afterExp);

		// 同步军衔信息
		getPush().syncPlayerInfo();

		// 升级处理：1、刷新作用号 2、重置津贴
		if (afterLevel > beforeLevel) {
			getEffect().resetEffectMilitaryRank(this);
			getData().getDailyDataEntity().setMilitaryRankRecieve(false);
			getPush().syncMilitaryRankAwardState();
		}

		BehaviorLogger.log4Service(this, Source.ATTR_CHANGE, action,
				Params.valueOf("playerAttr", PlayerAttr.MILITARY_EXP),
				Params.valueOf("add", add),
				Params.valueOf("beforeExp", beforeExp),
				Params.valueOf("afterExp", afterExp),
				Params.valueOf("beforeLevel", beforeLevel),
				Params.valueOf("afterLevel", afterLevel));
	}

	/**
	 * 添加钻石补发记录
	 * 
	 * @param moneyCount
	 * @param action
	 * @param additionalParam
	 */
	public void addMoneyReissueItem(int moneyCount, Action action, String additionalParam) {
		if (moneyCount == 0) {
			HawkLog.logPrintln("add money reissue param error, count is zero, playerId: {}, action: {}, additionalParam: {}",
					getId(), action.name(), additionalParam);

			return;
		}

		MoneyReissueEntity entity = new MoneyReissueEntity();
		entity.setPlayerId(getId());
		entity.setCount(moneyCount);
		entity.setSource(action.intItemVal());
		entity.setReissueParam(additionalParam == null ? "" : additionalParam);

		List<MoneyReissueEntity> entityList = getData().getMoneyReissueEntityList();
		if (HawkDBManager.getInstance().create(entity)) {
			entityList.add(entity);
			// 记录日志信息
			HawkLog.logPrintln("add money reissue entity succ, playerId: {}, count: {}, action: {}, additionalParam: {}",
					getId(), moneyCount, action.name(), additionalParam);

		} else {
			HawkLog.logPrintln("add money reissue entity failed, playerId: {}, count: {}, action: {}, additionalParam: {}",
					getId(), moneyCount, action.name(), additionalParam);
		}
	}

	/**
	 * 获取玩家的登录态
	 * @return
	 */
	public String getAccessToken() {
		String accessToken = "";
		if (getPfTokenJson() == null) {
			return accessToken;
		}

		JSONArray tokenArray = getPfTokenJson().getJSONArray("token");
		if (tokenArray != null && tokenArray.size() > 0) {
			accessToken = SDKManager.getInstance().getAccessToken(getChannel(), tokenArray);
		}

		return accessToken;
	}

	/**
	 * 判断玩家当日使用道具获得金币数量是否已达上限
	 * 
	 * @return
	 */
	public int getToolBackGoldToday(int golds) {
		String timeCountInfo = LocalRedis.getInstance().getToolBackGold(getId());
		HawkTuple2<Long, Integer> tuple = GameUtil.spliteTimeAndCount(getId(), timeCountInfo);
		if (tuple.second == -1) {
			LocalRedis.getInstance().updateToolBackGold(getId(), 0);
			if (golds > ConstProperty.getInstance().getGainCrystalLimitByUseItem()) {
				return -1;
			}

			return golds;
		}

		if (tuple.second + golds > ConstProperty.getInstance().getGainCrystalLimitByUseItem()) {
			HawkLog.errPrintln("use item back gold failed, toolBackGolds touch limit, playerId: {}, already backGolds: {}, backGold: {}", getId(), tuple.second, golds);
			return -1;
		}

		return tuple.second + golds;
	}

	/**
	 * 获取玩家注册天数
	 * @return
	 */
	public int getPlayerRegisterDays() {
		long registerAM0Time = HawkTime.getAM0Date(new Date(getCreateTime())).getTime();
		return (int) ((HawkTime.getMillisecond() - registerAM0Time) / GsConst.DAY_MILLI_SECONDS + 1);
	}

	public HPChatState getChatState() {
		return chatState;
	}

	public void setChatState(HPChatState chatState) {
		this.chatState = chatState;
	}

	/**
	 * 发送idip消息
	 * @param msg
	 */
	public void sendIdipMsg(String idipMsg) {
		sendIdipNotice(NoticeType.SEND_MSG, NoticeMode.MSG_BOX, -1, idipMsg);
	}

	public void sendIdipNotice(NoticeType type, NoticeMode mode, long relieveTime, int msgCode) {
		IDIPNotice.Builder notice = IDIPNotice.newBuilder();
		notice.setType(type);
		notice.setMode(mode);
		notice.setRelieveTime(relieveTime);
		notice.setMsgCode(msgCode);
		sendProtocol(HawkProtocol.valueOf(HP.code.IDIP_NOTICE_SYNC_S_VALUE, notice));
	}

	public void sendIdipNotice(NoticeType type, NoticeMode mode, long relieveTime, String msg) {
		IDIPNotice.Builder notice = IDIPNotice.newBuilder();
		notice.setType(type);
		notice.setMode(mode);
		if (relieveTime >= 0) {
			notice.setRelieveTime(relieveTime);
		}
		notice.setMsg(msg);
		sendProtocol(HawkProtocol.valueOf(HP.code.IDIP_NOTICE_SYNC_S_VALUE, notice));
	}

	/**
	 * 地块是否被尤里占领
	 * 
	 * @param buildType
	 * @param buildIndex
	 * @return
	 */
	public boolean isBuildingLockByYuri(int buildType, String buildIndex) {
		// 非共享地块不会被尤里占领
		if (!BuildAreaCfg.isShareBlockBuildType(buildType)) {
			return false;
		}

		return isBuildingLockByYuri(buildIndex);
	}

	/**
	 * 地块是否被尤里占领
	 * @return
	 */
	public boolean isBuildingLockByYuri(String buildIndex) {
		int areaId = BuildAreaCfg.getAreaByBlock(Integer.valueOf(buildIndex));
		YuriStrikeEntity yuriStrikeEntity = getData().getYuriStrikeEntity();
		if (YuriState.valueOf(yuriStrikeEntity.getState()) == YuriState.YURI_HOLD && yuriStrikeEntity.getAreaIdLock() == areaId) {
			return true;
		}

		return false;
	}

	/**
	 * 获取单次造兵数量上限
	 * 
	 * @return
	 */
	public int getMaxTrainNum() {
		int maxTrainCnt = 0;
		for (BuildingBaseEntity building : getData().getBuildingListByType(BuildingType.SOLDIER_CONTROL_HALL)) {
			BuildingCfg buildingCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, building.getBuildingCfgId());
			if (buildingCfg == null) {
				continue;
			}
			maxTrainCnt += buildingCfg.getTrainQuantity();
		}
		maxTrainCnt += ConstProperty.getInstance().getNewTrainQuantity() + getData().getEffVal(Const.EffType.CITY_ARMY_TRAIN_NUM);
		return maxTrainCnt;
	}

	/**
	 * 计算兵种荣耀等级
	 */
	public int getSoldierStar(int armyId) {
		return getData().getSoldierStar(armyId);
	}

	public int getSoldierStep(int armyId) {
		try {
			BattleSoldierCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, armyId);
			if (cfg.getBuilding() == BuildingType.PRISM_TOWER_VALUE) {// 如果是尖塔 会有两个
				return 0;
			}

			for (PlantTechEntity enttiy : getData().getPlantTechEntities()) {
				if (cfg.getBuilding() == enttiy.getBuildType()) {
					return enttiy.getTechObj().getCfg().getLevel();
				}
			}

		} catch (Exception e) {
			HawkException.catchException(e, "armyId = " + armyId);
		}
		return 0;
	}

	/**泰能兵技能强化等级*/
	public int getSoldierPlantSkillLevel(int armyId) {
		try {

			BattleSoldierCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, armyId);
			if (cfg.isPlantSoldier()) {
				final SoldierStrengthen sthen = getPlantSoldierSchool().getSoldierStrengthenByType(cfg.getSoldierType());
				return sthen.getPlantStrengthLevel();
			}
		} catch (Exception e) {
			HawkException.catchException(e, "getSoldierPlantSkillLevel armyId = " + armyId);
		}
		return 0;
	}
	/**泰能兵军衔等级*/
	public int getSoldierPlantMilitaryLevel(int armyId){
		try {

			BattleSoldierCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, armyId);
			if (cfg.isPlantSoldier()) {
				PlantSoldierMilitary military = getPlantSoldierSchool().getSoldierMilitaryByType(cfg.getSoldierType());
				PlantSoldierMilitary military3 = getPlantSoldierSchool().getSoldierMilitary3ByType(cfg.getSoldierType());
				return military.getMilitaryLevel() + military3.getMilitaryLevel();
			}
		} catch (Exception e) {
			HawkException.catchException(e, "getSoldierPlantMilitaryLevel armyId = " + armyId);
		}
		return 0;
	}
	/**任意泰能兵的最高等级*/
	public int getMaxSoldierPlantMilitaryLevel(){
		ConfigIterator<PlantSoldierMilitaryCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(PlantSoldierMilitaryCfg.class);
		int maxLevel = 0;
		while (configIterator.hasNext()){
			PlantSoldierMilitaryCfg cfg = configIterator.next();
			int soldierType = cfg.getSoldierType();
			PlantSoldierMilitary military = getPlantSoldierSchool().getSoldierMilitaryByType(SoldierType.valueOf(soldierType));
			PlantSoldierMilitary military3 = getPlantSoldierSchool().getSoldierMilitary3ByType(SoldierType.valueOf(soldierType));
			maxLevel = Math.max(maxLevel, military.getMilitaryLevel() + military3.getMilitaryLevel());
		}
		return maxLevel;
	}
	/**获取玩家城点位置*/
	public int getPlayerPos() {
		return WorldPlayerService.getInstance().getPlayerPos(this.getId());
	}

	/**获取玩家坐标xy*/
	public int[] getPosXY() {
		int[] playerpos = WorldPlayerService.getInstance().getPlayerPosXY(this.getId());
		return playerpos;
	}

	public LoginWay getLoginWay() {
		return loginWay;
	}

	public void setLoginWay(LoginWay loginWay) {
		this.loginWay = loginWay;
	}

	public long getLastPowerScore() {
		return lastPowerScore;
	}

	public void setLastPowerScore(long lastPowerScore) {
		this.lastPowerScore = lastPowerScore;
	}

	public String getLmjyRoomId() {
		return lmjyRoomId;
	}

	public void setLmjyRoomId(String lmjyRoomId) {
		this.lmjyRoomId = lmjyRoomId;
	}

	public PState getLmjyState() {
		return lmjyState;
	}

	public void setLmjyState(PState lmjyState) {
		this.lmjyState = lmjyState;
	}

	public String getSwRoomId() {
		return swRoomId;
	}

	public void setSwRoomId(String swRoomId) {
		this.swRoomId = swRoomId;
	}

	public SWState getSwState() {
		return swState;
	}

	public void setSwState(SWState swState) {
		this.swState = swState;
	}

	public long getBeAttacked() {
		return beAttacked;
	}

	public void setBeAttacked(long beAttacked) {
		this.beAttacked = beAttacked;
	}

	public boolean isCsPlayer() {
		return false;
	}

	public CsPlayer getCsPlayer() {
		return null;
	}

	public HPLogin.Builder getHpLogin() {
		return hpLogin;
	}

	public void setHpLogin(HPLogin.Builder hpLogin) {
		this.hpLogin = hpLogin;
	}

	public int getCrossStatus() {
		return crossStatus;
	}

	public void setCrossStatus(int crossStatus) {
		this.crossStatus = crossStatus;
	}

	/**
	 * 处于某个状态.
	 * 多个状态就是或的关系.
	 * @author  jm
	 * @param crossStatus
	 * @return
	 */
	public boolean isCrossStatus(int... crossStatuses) {
		boolean result = false;
		for (int tmpCorssStatus : crossStatuses) {
			result |= (tmpCorssStatus == crossStatus);
		}

		return result;
	}

	public long getCrossBackTime() {
		return this.getTickTimeLine().getCrossBackTime();
	}

	public void setCrossBackTime(long crossBackTime) {
		this.getTickTimeLine().setCrossBackTime(crossBackTime);
	}

	public Object getSyncObj() {
		return syncObj;
	}

	/**
	 * 计算石油转换作用号
	 * @param player
	 * @param items
	 */
	public void calcOilChangeEff(AwardItems items) {
		int oilChangeEff = getEffect().getEffVal(EffType.RES_TO_PROTECTED);
		int afterSafe = 0;
		if (oilChangeEff > 0) {
			for (ItemInfo item : items.getAwardItems()) {
				if (item.getItemType() != ItemType.PLAYER_ATTR) {
					continue;
				}
				if (item.getItemId() != PlayerAttr.OIL_UNSAFE_VALUE) {
					continue;
				}
				int afterUnsafe = (int) Math.ceil(GsConst.EFF_PER * (10000 - oilChangeEff) * item.getCount());
				afterUnsafe = Math.max(afterUnsafe, 0);

				afterSafe += (int) Math.ceil(GsConst.EFF_PER * oilChangeEff * item.getCount());
				afterSafe = Math.max(afterSafe, 0);

				item.setCount(afterUnsafe);
			}
		}
		items.addItem(new ItemInfo(ItemType.PLAYER_ATTR_VALUE, PlayerAttr.OIL_VALUE, afterSafe));
	}

	/**
	 * 获取新兵救援信息
	 * 
	 * @param init 不存在时是否要初始化创建
	 * 
	 * @return
	 */
	public ProtectSoldierInfo getProtectSoldierInfo(boolean init) {
		if (protectSoldierInfo == null) {
			protectSoldierInfo = RedisProxy.getInstance().getProtectSoldierInfo(getId());
			if (protectSoldierInfo == null && init) {
				protectSoldierInfo = new ProtectSoldierInfo(getId());
			}
		}

		return protectSoldierInfo;
	}

	public void setProtectSoldierInfo(ProtectSoldierInfo protectSoldierInfo) {
		this.protectSoldierInfo = protectSoldierInfo;
	}

	/**
	 * 刷新新兵救援信息
	 */
	public void refreshProctectSoldierInfo() {
		long timeLong = ConstProperty.getInstance().getRescueDuration() * 1000L;
		if (HawkApp.getInstance().getCurrentTime() - getCreateTime() >= timeLong) {
			return;
		}

		try {
			ProtectSoldierInfo protectSoldierInfo = getProtectSoldierInfo(true);
			int oldReceiveDayCount = protectSoldierInfo.getReceiveCountDay();
			protectSoldierInfo.setReceiveCountDay(0);
			getPush().pushProtectSoldierInfo();
			if (oldReceiveDayCount > 0) {
				RedisProxy.getInstance().updateProtectSoldierInfo(protectSoldierInfo, ConstProperty.getInstance().getRescueDuration());
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/**
	 * 获取大R复仇信息
	 * 
	 * @param init 不存在时是否要初始化创建
	 * @return
	 */
	public RevengeInfo getRevengeInfo(boolean init) {
		if (!init) {
			return revengeInfo;
		}

		if (revengeInfo == null) {
			revengeInfo = RedisProxy.getInstance().getRevengeInfo(getId());
			if (revengeInfo == null) {
				revengeInfo = new RevengeInfo(getId());
			}
		}

		return revengeInfo;
	}

	public void setRevengeInfo(RevengeInfo revengeInfo) {
		this.revengeInfo = revengeInfo;
	}

	/**
	 * 获取大R复仇损失兵力信息
	 * @return
	 */
	public List<RevengeSoldierInfo> getLossTroopInfoList() {
		if (lossTroopInfoList == null) {
			lossTroopInfoList = RedisProxy.getInstance().getAllRevengeDeadSoldierInfo(getId());
		}

		return lossTroopInfoList;
	}

	/**
	 * 重置大R复仇商人相关信息
	 */
	public void resetRevengeInfo() {
		revengeInfo = null;
		lossTroopInfoList = null;
		revengeShopBuyInfo = null;
	}

	/**
	 * 获取大R复仇折扣商品信息
	 * 
	 * @return
	 */
	public Map<Integer, Integer> getRevengeShopBuyInfo() {
		if (revengeShopBuyInfo == null) {
			revengeShopBuyInfo = RedisProxy.getInstance().getRevengeShopBuyInfo(getId());
		}
		return revengeShopBuyInfo;
	}

	/**
	 * 获取攻击年兽次数
	 */
	public int getAtkNianTimes(String nianUuid) {
		Integer times = atkNianTimesMap.get(nianUuid);
		if (times == null) {
			return 0;
		}
		return times;
	}

	/**
	 * 增加攻击年兽次数
	 */
	public void incrementAtkNianTimes(String nianUuid) {
		int beforeTimes = getAtkNianTimes(nianUuid);
		atkNianTimesMap.put(nianUuid, beforeTimes + 1);
	}

	/**
	 * 获取限时商店条件达成信息
	 * @return
	 */
	public Map<Integer, TimeLimitStoreConditionInfo> getTimeLimitStoreConditionMap() {
		if (timeLimitStoreCondition == null) {
			timeLimitStoreCondition = RedisProxy.getInstance().getTimeLimitStoreCondition(getId());
			if (timeLimitStoreCondition == null) {
				timeLimitStoreCondition = new HashMap<Integer, TimeLimitStoreConditionInfo>();
			}
		}

		return timeLimitStoreCondition;
	}

	/**
	 * 获取限时商店条件达成信息
	 * 
	 * @return
	 */
	public TimeLimitStoreConditionInfo getTimeLimitStoreCondition(int triggerType) {
		if (timeLimitStoreCondition == null) {
			getTimeLimitStoreConditionMap();
		}

		return timeLimitStoreCondition.get(triggerType);
	}

	/**
	 * 添加限时商店条件达成信息
	 * @param triggerType
	 * @param conditionInfo
	 */
	public void addTimeLimitStoreCondition(TimeLimitStoreConditionInfo conditionInfo) {
		timeLimitStoreCondition.put(conditionInfo.getTriggerType(), conditionInfo);
	}

	/**
	 * 重置数据
	 */
	public void resetTimeLimitStoreCondition() {
		this.timeLimitStoreCondition = null;
		this.onSellStoreCondition = null;
	}

	/**
	 * 获取正在出售的商品库信息
	 * 
	 * @return
	 */
	public TimeLimitStoreConditionInfo getOnSellStoreCondition() {
		return onSellStoreCondition;
	}

	public void setOnSellStoreCondition(TimeLimitStoreConditionInfo onSellStoreCondition) {
		this.onSellStoreCondition = onSellStoreCondition;
	}

	public boolean isVipShopRedPoint() {
		return vipShopRedPoint;
	}

	public void setVipShopRedPoint(boolean vipShopRedPoint) {
		this.vipShopRedPoint = vipShopRedPoint;
	}

	/**
	 * 刷新贵族商城气泡红点
	 */
	public void refreshVipShopRedPoint() {
		long now = HawkTime.getMillisecond();
		// 如果玩家登录后一直挂着，第二天0点跨周了，状态设置为true，点击后状态改成了false，第三天0点根据loginTime判断还是跨周，状态又重新设置为true
		// 这种情况的前提条件是跨周前登录游戏，然后至少24小时以上持续在线
		if (!isVipShopRedPoint() && !HawkTime.isSameWeek(now, getLoginTime())) {
			setVipShopRedPoint(true);
			LocalRedis.getInstance().addVipShopRedPoint(getId());
			sendProtocol(HawkProtocol.valueOf(HP.code.VIPSHOP_RED_POINT_SYNC));
		}
	}

	public String getTBLYRoomId() {
		return tblyRoomId;
	}

	public void setTBLYRoomId(String tblyRoomId) {
		this.tblyRoomId = tblyRoomId;
	}

	public TBLYState getTBLYState() {
		return tblyState;
	}

	public void setTBLYState(TBLYState tblyState) {
		this.tblyState = tblyState;
	}

	/**玩家所在副本地图*/
	public String getDungeonMap() {
		if (getTBLYState() == TBLYState.GAMEING) {
			return "Tiberium";
		}
		if (getLmjyState() == PState.GAMEING) {
			return "Drill";
		}
		if (getSwState() == SWState.GAMEING) {
			return "StarWars";
		}
		if (getCYBORGState() == CYBORGState.GAMEING) {
			return "Cyborg";
		}
		if (getDYZZState() == DYZZState.GAMEING) {
			return "Dyzz";
		}
		if (getYQZZState() == YQZZState.GAMEING) {
			return "MoonWar";
		}
		if (getXhjzState() == XHJZState.GAMEING) {
			return "Xhjz";
		}
		if (getFgylState() == FGYLState.GAMEING) {
			return "fgyl";
		}
		if (getXQHXState() == XQHXState.GAMEING) {
			return "xqhx";
		}
		return "";
	}

	/**玩家当前正在副本中玩耍*/
	public boolean isInDungeonMap() {
		return StringUtils.isNotEmpty(this.getDungeonMap());
	}

	/**
	 * 获取铠甲套装位置穿戴铠甲
	 */
	public String getArmourSuit(int suitId, int pos) {
		return getArmourSuit().get(suitId, pos);
	}

	/**
	 * 获取铠甲套装
	 */
	public Map<Integer, String> getArmourSuit(int suitId) {
		return getArmourSuit().row(suitId);
	}

	/**
	 * 获取铠甲套装
	 */
	public Table<Integer, Integer, String> getArmourSuit() {
		return (armourSuit == null) ? resetArmourSuit() : armourSuit;
	}

	/**
	 * 穿戴铠甲
	 */
	public void wearArmour(int suit, int pos, String armourId) {
		getArmourSuit().put(suit, pos, armourId);
	}

	/**
	 * 卸下铠甲
	 */
	public void takeOffArmour(int suit, int pos) {
		getArmourSuit().remove(suit, pos);
	}

	/**
	 * 初始化铠甲套装
	 */
	public Table<Integer, Integer, String> resetArmourSuit() {

		Table<Integer, Integer, String> armourSuit = HashBasedTable.create();

		List<ArmourEntity> armours = getData().getArmourEntityList();

		for (ArmourEntity armour : armours) {

			if (armour.getSuitSet().isEmpty()) {
				continue;
			}

			for (Integer suit : armour.getSuitSet()) {
				ArmourCfg armourCfg = HawkConfigManager.getInstance().getConfigByKey(ArmourCfg.class, armour.getArmourId());
				armourSuit.put(suit, armourCfg.getPos(), armour.getId());
			}
		}

		this.armourSuit = armourSuit;

		return this.armourSuit;
	}

	/**
	 * 获取套装上的铠甲
	 */
	public List<ArmourEntity> getSuitArmours(int suit) {
		List<ArmourEntity> armours = new ArrayList<>();

		List<ArmourEntity> armourEntityList = getData().getArmourEntityList();
		for (ArmourEntity armour : armourEntityList) {
			if (armour.getSuitSet().contains(suit)) {
				armours.add(armour);
			}
		}
		return armours;
	}

	/**
	 * 添加铠甲
	 */
	public void addArmour(Integer armourPoolId) {
		logger.info("addArmour, playerId:{}, armourPoolId:{}", getId(), armourPoolId);
		if (armourPoolId == null) {
			return;
		}

		ArmourPoolCfg poolCfg = HawkConfigManager.getInstance().getConfigByKey(ArmourPoolCfg.class, armourPoolId);
		if (poolCfg == null) {
			logger.error("add armour error, armourPoolId not exit, playerId:{}, armourPoolId:{}", getId(), armourPoolId);
			return;
		}
		ArmourCfg amourCfg = HawkConfigManager.getInstance().getConfigByKey(ArmourCfg.class, poolCfg.getArmourId());
		if (amourCfg == null) {
			logger.error("add armour error, armourId not exit, playerId:{}, armourId:{}", getId(), poolCfg.getArmourId());
			return;
		}

		ArmourEntity entity = new ArmourEntity();
		entity.setId(HawkUUIDGenerator.genUUID());
		entity.setPlayerId(getId());
		entity.setArmourId(poolCfg.getArmourId());
		entity.setQuality(poolCfg.getQuality());
		// 随机额外属性
		List<ArmourEffObject> extrAttrs = new ArrayList<>();
		// 常规铠甲额外属性随机
		if (!amourCfg.isSuper()) {
			List<Integer> alreadyRandType = new ArrayList<>();
			ArmourBreakthroughCfg armourQualityCfg = HawkConfigManager.getInstance().getConfigByKey(ArmourBreakthroughCfg.class, poolCfg.getQuality());
			List<Integer> randomList = armourQualityCfg.randomAttrQualityList();
			if (poolCfg.getQuality() >= 3 && getEffect().getEffVal(EffType.ARMOUR_1600) > 0) {
				randomList.add(poolCfg.getQuality());
			}

			for (int i = 0; i < randomList.size();) {
				int quality = randomList.get(i);
				List<ArmourAdditionalCfg> attrCfgs = AssembleDataManager.getInstance().getArmourAdditionCfgs(1, quality);
				ArmourAdditionalCfg randAttrCfg = RandomUtil.random(attrCfgs);
				ArmourAttrTemplate randAttr = RandomUtil.random(randAttrCfg.getAttrList());
				if (alreadyRandType.contains(randAttr.getEffect())) {
					continue;
				}
				extrAttrs.add(new ArmourEffObject(randAttrCfg.getId(), randAttr.getEffect(), HawkRand.randInt(randAttr.getRandMin(), randAttr.getRandMax())));
				alreadyRandType.add(randAttr.getEffect());
				i++;
			}
			Collections.shuffle(extrAttrs);

		} else {
			// 神器铠甲额外属性随机
			List<Integer> superExtraAttrIds = amourCfg.getSuperExtraAttrIds();
			for (Integer superExtraAttrId : superExtraAttrIds) {
				ArmourAdditionalCfg extraAttrCfg = HawkConfigManager.getInstance().getConfigByKey(ArmourAdditionalCfg.class, superExtraAttrId);
				ArmourAttrTemplate randAttr = RandomUtil.random(extraAttrCfg.getAttrList());
				extrAttrs.add(new ArmourEffObject(extraAttrCfg.getId(), randAttr.getEffect(), HawkRand.randInt(randAttr.getRandMin(), randAttr.getRandMax())));
			}

			// 特技属性，随机n条
			List<Integer> skillAttrList = amourCfg.getSkillAttrList();
			Collections.shuffle(skillAttrList);
			int skillAttrCount = HawkRand.randInt(1, skillAttrList.size());
			for (int i = 0; i < skillAttrCount; i++) {
				int skillAttrId = skillAttrList.get(i);
				ArmourAdditionalCfg extraAttrCfg = HawkConfigManager.getInstance().getConfigByKey(ArmourAdditionalCfg.class, skillAttrId);
				ArmourAttrTemplate randAttr = RandomUtil.random(extraAttrCfg.getAttrList());
				entity.addSkillAttrEff(new ArmourEffObject(extraAttrCfg.getId(), randAttr.getEffect(), HawkRand.randInt(randAttr.getRandMin(), randAttr.getRandMax())));
			}
			entity.setSuper(true);
			entity.setEndTime(HawkTime.getMillisecond() + amourCfg.getLifeTime());
		}

		for (ArmourEffObject attr : extrAttrs) {
			entity.addExtraAttrEff(attr);
		}

		// 初始等级为0
		if (entity.isSuper()) {
			entity.setLevel(40);
		} else {
			entity.setLevel(0);
		}

		entity.create(true);
		getData().getArmourEntityList().add(entity);
		getPush().syncArmourInfo(entity);
		if (entity.isSuper()) {
			GlobalData.getInstance().addSuperArmourInfo(entity.getId(), entity.getPlayerId(), entity.getEndTime());
		}

		logger.info("addArmour, playerId:{}, armourPoolId:{}, armourId:{}, quality:{}, level:{}, attr:{}", getId(), armourPoolId, entity.getArmourId(), entity.getQuality(),
				entity.getLevel(), Arrays.toString(entity.getExtraAttrEff().toArray()));
		LogUtil.logArmourAdd(this, entity.getId(), entity.getArmourId(), entity.getQuality());
	}

	/**
	 * 删除铠甲
	 */
	@MessageHandler
	public boolean removeArmour(RemoveArmourMsg msg) {

		String armourId = msg.getInfo().getId();

		// 清空铠甲穿戴的套装
		ArmourEntity armour = getData().getArmourEntity(armourId);
		armour.clearSuit(false);

		// 重置套装
		resetArmourSuit();

		// 删除铠甲
		this.getData().removeArmourEntity(armour);
		armour.delete();

		// 重新计算作用号
		getEffect().resetEffectArmour(this);

		// 同步套装
		getPush().syncArmourSuitInfo();

		// 同步铠甲删除
		getPush().syncArmourDelete(armourId);

		// 刷新战力
		refreshPowerElectric(PowerChangeReason.ARMOUR_CHANGE);

		logger.info("remove super armour, playerId:{}, armourId:{}", getId(), armourId);

		return true;
	}

	/**取得指定页套装*/
	public ArmourBriefInfo genArmourBriefInfo(ArmourSuitType suit) {
		return BuilderUtil.genArmourBattleInfo(getData(), suit.getNumber()).build();
	}

	/**
	 * 获取玩家设置的主力兵种信息
	 * @return
	 */
	public Set<SoldierType> getMainForce() {
		Set<SoldierType> set = new HashSet<>();
		CustomDataEntity customDataEntity = getData().getCustomDataEntity(GsConst.PLAYER_MAIN_FORCE);
		if (customDataEntity == null || HawkOSOperator.isEmptyString(customDataEntity.getArg())) {
			return set;
		}
		String[] arrs = customDataEntity.getArg().split(",");
		for (String arr : arrs) {
			try {
				int type = Integer.valueOf(arr);
				set.add(SoldierType.valueOf(type));
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		return set;
	}

	/** 超能实验室是否激活过 */
	public boolean isSuperLabActive(int labId) {
		// 旧超能废弃
		return true;
	}

	public String getPlayerSecPasswd() {
		if (!HawkOSOperator.isEmptyString(playerSecPasswd) && getSecPasswdExpiryTime() > 0
				&& HawkTime.getMillisecond() >= getSecPasswdExpiryTime()) {
			playerSecPasswd = null;
		}

		return playerSecPasswd;
	}

	public void setPlayerSecPasswd(String playerSecPasswd) {
		this.playerSecPasswd = playerSecPasswd;
		setSecPasswdExpiryTime(0);
	}

	public void setSecPasswdExpiryTime(long secPasswdExpiryTime) {
		this.getTickTimeLine().setSecPasswdExpiryTime(secPasswdExpiryTime);
	}

	public long getSecPasswdExpiryTime() {
		return this.getTickTimeLine().getSecPasswdExpiryTime();
	}

	/**
	 * 是否可以领取宝箱
	 * @return
	 */
	public boolean canReceiveChristmasBox() {
		int number = getReceivedChristmasBoxNumber();

		return number < WorldMarchConstProperty.getInstance().getChristmasBoxReceiveLimit();
	}

	public int getReceivedChristmasBoxNumber() {
		String refreshUuid = WorldChristmasWarService.getInstance().getRefreshUuid();
		return memoryData.getOrDefault(refreshUuid, 0);
	}

	public void receiveChristmasBox(int number) {
		String refreshUuid = WorldChristmasWarService.getInstance().getRefreshUuid();
		MapUtil.appendIntValue(memoryData, refreshUuid, number);
	}

	public void setCYBORGRoomId(String uuid) {
		this.cyborgRoomId = uuid;
	}

	public String getCYBORGRoomId() {
		return this.cyborgRoomId;
	}

	public void setCYBORGState(CYBORGState state) {
		this.cyborgState = state;
	}

	public CYBORGState getCYBORGState() {
		return this.cyborgState;
	}

	public int getPlayerGhostTowerStage() {
		return this.getData().getPlayerGhostTowerEntity().getStageId();
	}

	public long getPlayerGhostTowerProductTime() {
		return this.getData().getPlayerGhostTowerEntity().getProductTime();
	}

	public void setPlayerGhostTowerStage(int stageId) {
		this.getData().getPlayerGhostTowerEntity().setStageId(stageId);
	}

	public void setPlayerGhostTowerProductTime(long productTime) {
		this.getData().getPlayerGhostTowerEntity().setProductTime(productTime);
	}

	/**
	 * 玩家协议因间隔被拒
	 * 
	 * @param protocol
	 */
	protected void onProtocolElapseDeny(HawkProtocol protocol) {
		sendError(protocol.getType(), Status.SysError.PROTOCOL_BUSY, 0);
	}

	/**
	 * 检测军事特权礼包半价特权
	 * 
	 * @return
	 */
	public int checkMonthCardPriceCut() {
		ActivityMonthCardEntity entity = GameUtil.getMonthCardEntity(this.getId());
		// 月卡生效期间不管用
		if (entity != null && !entity.getEfficientCardList(ConstProperty.getInstance().getGoldPrivilegeType()).isEmpty()) {
			return -1;
		}

		int itemCount = this.getData().getItemNumByItemId(ConstProperty.getInstance().getGoldPrivilegeDiscountItem());
		if (itemCount <= 0) {
			return -1;
		}

		int notifyTime = 0;
		CustomDataEntity customData = this.getData().getCustomDataEntity(GsConst.GOLD_PRIVILEGE_NOTITY_VALIDTIME);
		if (customData != null) {
			notifyTime = customData.getValue();
		}

		// 返回大于0的值表示还在半价提醒有效期内
		if (HawkTime.getSeconds() <= notifyTime) {
			return notifyTime;
		}

		// 返回0表示可享受半价，但不再提醒
		return 0;
	}
	
	
	public PlantScience getPlantScience(){
		return this.getData().getPlantScienceEntity().getSciencObj();
	}
	
	public PlayerYQZZData getPlayerYQZZData(){
		return this.getData().getPlayerYqzzEntity().getPlayerYQZZData();
	}

	public String getDYZZRoomId() {
		return dyzzRoomId;
	}

	public void setDYZZRoomId(String dyzzRoomId) {
		this.dyzzRoomId = dyzzRoomId;
	}

	public DYZZState getDYZZState() {
		return dyzzState;
	}

	public void setDYZZState(DYZZState dyzzState) {
		this.dyzzState = dyzzState;
	}
	
	/**
	 * 获取玩家去兵战力
	 */
	public int getNoArmyPower() {
		PowerElectric powerElectric = getData().getPowerElectric();
		long totalPoint = powerElectric.getPowerData().getTotalPoint();
		long armyPoint = powerElectric.getPowerData().getArmyBattlePoint();
		int trapPoint = powerElectric.getPowerData().getTrapBattlePoint();
		int noArmyPoint = (int) Math.max(totalPoint - armyPoint - trapPoint, 0);
		return noArmyPoint;
	}
	
	/**
	 * 获取玩家战力
	 * @return
	 */
	public long getStrength() {
		return PlayerStrengthFactory.getInstance().calcStrength(this);
	}
	
	/**
	 * 更新红点
	 * @param nationRedDot
	 */
	public void updateNationRD(NationRedDot nationRedDot){
		this.nationRdMap.put(nationRedDot, 1);
	}
	
	public void updateNationRDAndNotify(NationRedDot nationRedDot){
		this.nationRdMap.put(nationRedDot, 1);
		syncNationRedDot();
	}
	
	/**
	 * 去掉红点
	 * @param nationRedDot
	 */
	public void rmNationRD(NationRedDot nationRedDot){
		this.nationRdMap.put(nationRedDot, 0);
	}
	
	public void rmNationRDAndNotify(NationRedDot nationRedDot){
		this.nationRdMap.put(nationRedDot, 0);
		syncNationRedDot();
	}
	
	
	/**
	 * 推动国家小红点
	 * @param checkModify 是否检测变化
	 */
	public void syncNationRedDot(boolean checkModify) {
		String currentMark = nationRdMap.toString();
		if (!currentMark.equals(nationMapMark)) {
			syncNationRedDot();
			nationMapMark = currentMark;
		}
	}
	
	/**
	 * 推动国家小红点
	 */
	public void syncNationRedDot() {
		// 跨服玩家一律不推送红点
		if(isCsPlayer()){
			return;
		}
		NationRDInfoList.Builder builder = NationRDInfoList.newBuilder();
		for (Entry<NationRedDot, Integer> entry : nationRdMap.entrySet()) {
			NationRDInfo.Builder info = NationRDInfo.newBuilder();
			info.setRdkey(entry.getKey());
			info.setVal(entry.getValue());
			builder.addRdlist(info);
		}
		this.sendProtocol(HawkProtocol.valueOf(HP.code2.NATIONAL_REDDOT_SYNC_S_VALUE, builder));
	}
	
	/**
	 * 获取玩家至尊vip信息
	 * @return
	 */
	public PlayerVipSuper getSuperVipObject() {
		return vipSuperObject;
	}
	
	/**
	 * 获取有效的vip等级
	 * 
	 * @return
	 */
	public int getActivatedVipSuperLevel() {
		return getSuperVipObject().getActivatedSuperVipLevel();
	}
	
	/**
	 * 获取至尊vip皮肤特效已激活的等级
	 * 
	 * @return
	 */
	public int getSuperVipSkinActivatedLevel() {
		return getSuperVipObject().getSuperVipSkinActivatedLevel();
	}
	
	/**
	 * 获取至尊vip等级
	 * @return
	 */
	public int getSuperVipLevel() {
		if (getSuperVipObject().isSuperVipOpen()) {
			return getSuperVipObject().getSuperVipInfo().getActualLevel();
		}
		
		return 0;
	}

	public String getYQZZRoomId() {
		return yqzzRoomId;
	}

	public void setYQZZRoomId(String yqzzRoomId) {
		this.yqzzRoomId = yqzzRoomId;
	}

	public YQZZState getYQZZState() {
		return yqzzState;
	}

	public void setYQZZState(YQZZState yqzzState) {
		this.yqzzState = yqzzState;
	}
	
	/**
	 * 获取ip推属地
	 * 
	 * @return
	 */
	public String getIpBelongsAddr() {
		if (GameUtil.isWin32Platform(this) || ConstProperty.getInstance().getShowIpOnline() == 0) {
			return "";
		}
		
		// 缓存10分钟
		if (!HawkOSOperator.isEmptyString(ipBelongsAddr) && HawkApp.getInstance().getCurrentTime() - this.getTickTimeLine().getIpBelongsAddrRefreshTime() < HawkTime.MINUTE_MILLI_SECONDS * 10) {
			return ipBelongsAddr;
		}
		
		try {
			String clientIp = getClientIp();
			// 防止获取离线玩家的ip时获取不到，所以要将玩家ip缓存起来
			if (HawkOSOperator.isEmptyString(clientIp)) {
				clientIp = RedisProxy.getInstance().getRedisSession().getString("playerClientIp:" + getId());
			} else {
				RedisProxy.getInstance().getRedisSession().setString("playerClientIp:" + getId(), clientIp);
			}
			ipBelongsAddr = GameUtil.convertIP2Address(clientIp);
			this.getTickTimeLine().setIpBelongsAddrRefreshTime(HawkApp.getInstance().getCurrentTime());
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		return ipBelongsAddr == null ? "" : ipBelongsAddr;
	}

	/**
	 * 初始化赠送保护道具
	 */
	public void initSendTimeLimitTool() {
		sendTimeLimitTool = RedisProxy.getInstance().getSendTimeLimitTool(getId());
	}
	
	/**
	 * 获取赠送保护道具获取
	 * @return
	 */
	public Map<Integer, Long> getSendTimeLimitTool() {
		return sendTimeLimitTool;
	}
	
	/**
	 * 获取赠送保护道具获取时间
	 * @return
	 */
	public long getSendTimeLimitTool(int itemId) {
		return sendTimeLimitTool.getOrDefault(itemId, 0L);
	}

	/**
	 * 更新赠送时间限制道具
	 * @param itemId
	 */
	public void updateSendTimeLimitTool(int itemId) {
		if (sendTimeLimitTool.containsKey(itemId)) {
			return;
		}
		sendTimeLimitTool.put(itemId, HawkTime.getMillisecond());
		RedisProxy.getInstance().updateSendTimeLimitTool(this.getId(), sendTimeLimitTool);
	}

	/**
	 * 检测协议请求
	 * @param protoType
	 * @return
	 */
	public boolean checkProtoCounter(int protoType) {
		long currentTime = HawkTime.getMillisecond();
		ProtoCheckConfig cfg = HawkConfigManager.getInstance().getConfigByKey(ProtoCheckConfig.class, protoType);
		if (cfg == null) {
			return true;
		}

		// 检测封禁时间
		long banEndTime = protoBan.getOrDefault(protoType, 0L);
		if (banEndTime > currentTime) {
			sendError(protoType, Status.SysError.PROTOCOL_COUNTER_BAN_VALUE, 0, HawkTime.formatTime(banEndTime));
			return false;
		}
		try {
			// 记录30分钟内每条协议的收取次数
			LoadingCache<Integer, AtomicInteger> counter = protoCounter.get(protoType);
			if (counter == null) {
				counter = CacheBuilder.newBuilder().recordStats()
						.expireAfterWrite(cfg.getCacheSecond(), TimeUnit.SECONDS)
						.build(new CacheLoader<Integer, AtomicInteger>() {
							@Override
							public AtomicInteger load(Integer minute) throws Exception {
								return new AtomicInteger(0);
							}
						});
				protoCounter.put(protoType, counter);
			}

			// 增加协议访问次数
			int minute = HawkTime.getCalendar(false).get(Calendar.MINUTE);
			counter.get(minute).addAndGet(1);

			int count = counter.asMap().values().stream().mapToInt(i -> i.get()).sum();
			
			if (count > cfg.getCountLimit()) {
				// 设置封禁时间
				banEndTime = currentTime + cfg.getBanSecond() * 1000L;
				protoBan.put(protoType, banEndTime);
				logger.info("check proto counter, playerId:{}, proto:{}, count:{}, banEndTime:{}", getId(), protoType, count, HawkTime.formatTime(banEndTime));
				return false;
			}
		} catch (ExecutionException e) {
			HawkException.catchException(e);
		}
		
		return true;
	}
	
	/**
	 * 清除协议计数
	 * @param protoType
	 * @return
	 */
	public boolean clearProtoCounter(int protoType) {
		protoBan.remove(protoType);
		protoCounter.remove(protoType);
		logger.info("clear proto counter, playerId:{}", getId());
		return true;
	}
	
	public StaffOfficerSkillCollection getStaffOffic(){
		return getData().getStaffOffic();
	}

	public long getHealthGameUpdateTime() {
		return this.getTickTimeLine().getHealthGameUpdateTime();
	}

	public void setHealthGameUpdateTime(long healthGameUpdateTime) {
		this.getTickTimeLine().setHealthGameUpdateTime(healthGameUpdateTime);
	}

	public long getNextRemindTime() {
		return this.getTickTimeLine().getNextRemindTime();
	}

	public void setNextRemindTime(long nextRemindTime) {
		this.getTickTimeLine().setNextRemindTime(nextRemindTime);
	}

	public boolean isAdult() {
		return adult;
	}

	public void setAdult(boolean adult) {
		this.adult = adult;
	}
	
	public long getHealthGameTickTime() {
		return this.getTickTimeLine().getHealthGameTickTime();
	}

	public void setHealthGameTickTime(long healthGameTickTime) {
		this.getTickTimeLine().setHealthGameTickTime(healthGameTickTime);
	}
	
	public void setCareBanStartTime(long startTime) {
		this.getTickTimeLine().setCareBanStartTime(startTime);
	}

	public void setScoreBatchTime(long scoreBatchTime) {
		this.getTickTimeLine().setScoreBatchTime(scoreBatchTime);
	}
	
	public long getLastContinueOnlineTime() {
		return this.getTickTimeLine().getLastContinueOnlineTime();
	}

	public void setLastContinueOnlineTime(long lastContinueOnlineTime) {
		this.getTickTimeLine().setLastContinueOnlineTime(lastContinueOnlineTime);
	}
	
	public PlayerTickTimeLine getTickTimeLine() {
		return tickTimeObject;
	}
	
	public int getGuildFormationChangeMark() {
		return guildFormationChangeMark.get();
	}
	
	public void setGuildFormationChangeMark(int mark) {
		guildFormationChangeMark.set(mark);
	}
	
	/**
	 * 设置角色交易状态
	 * @param state
	 */
	public void setRoleExchangeState(int state) {
		this.roleExchangeState = state;
	}
	
	public int getRoleExchangeState() {
		return this.roleExchangeState;
	}

	public XHJZState getXhjzState() {
		return xhjzState;
	}

	public void setXhjzState(XHJZState xhjzState) {
		this.xhjzState = xhjzState;
	}

	public String getXhjzRoomId() {
		return xhjzRoomId;
	}

	public void setXhjzRoomId(String xhjzRoomId) {
		this.xhjzRoomId = xhjzRoomId;
	}
	
/**
     * 构建超武信息
     *
     * @return
     */
    public PBManhattanInfo.Builder buildManhattanInfo() {
        return buildManhattanInfo(PresetMarchManhattan.getDefaultInstance());
    }

    /**
     * 构建超武信息(设置进攻防守)
     *
     * @return
     */
    public PBManhattanInfo.Builder buildManhattanInfo(PresetMarchManhattan presetMarchManhattan) {
        PBManhattanInfo.Builder builder = PBManhattanInfo.newBuilder();
        int totalPower = 0;
        PlayerManhattan manhattanBase = this.getManhattanBase();
        if (manhattanBase != null) {
            manhattanBase.buildManhattanInfo(builder, presetMarchManhattan);
            totalPower += manhattanBase.getPower();
        }
        List<PlayerManhattan> manhattanList = this.getAllManhattanSW();
        for (PlayerManhattan manhattan : manhattanList) {
            manhattan.buildManhattanInfo(builder, presetMarchManhattan);
            totalPower += manhattan.getPower();
        }
        builder.setPower(totalPower);
        return builder;
    }
	
	/**
	 * 检查超武功能是否已解锁
	 * @return
	 */
	public boolean checkManhattanFuncUnlock() {
		int cityLevel = this.getCityLevel();
		if (cityLevel < ConstProperty.getInstance().getManhattanUnlockLevel()) {
			return false;
		}
		BuildingBaseEntity buildingEntity = getData().getBuildingEntityByType(BuildingType.MANHATTAN);
		return buildingEntity != null;
	}
	
	/**
	 * 获取部署的超武信息
	 * @return
	 */
	public PBDeployedSwInfo.Builder getDeployedSwInfo() {
		if (getData() == null) {
			return PBDeployedSwInfo.newBuilder();
		}
		
		return getData().getDeployedSwInfo();
	}
	
	/**
	 * 检查机甲核心养成线功能是否已解锁
	 * @return
	 */
	public boolean checkMechacoreFuncUnlock() {
		if (!MechaCoreConstCfg.getInstance().checkServerDelay()) {
			return false;
		}
		int cityLevel = this.getCityLevel();
		if (cityLevel < MechaCoreConstCfg.getInstance().getBaseLimit()) {
			return false;
		}
		if (this.getVipLevel() < MechaCoreConstCfg.getInstance().getVipLimit()) {
			return false;
		}
		return true;
	}
	
	public PBMechaCoreInfo.Builder buildMechacoreInfo(MechaCoreSuitType suit) {
		return this.getPlayerMechaCore().builderMailInfo(suit);
	}
	
	public String getMechacoreShowInfo() {
		return getPlayerMechaCore().serializeUnlockedCityShow();
	}
	public boolean isGetDressEnough(){
		CommanderEntity entity = getData().getCommanderEntity();
		return entity.getGetDressCount() >= ConstProperty.getInstance().getGetDressNumLimit();
	}
    /**
     * 检查超家园是否已解锁
     *
     * @return
     */
    public boolean checkHomeLandFuncUnlock() {
        int cityLevel = this.getCityLevel();
        HomeLandConstKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(HomeLandConstKVCfg.class);
        if( cityLevel < cfg.getBaseLimit()){
            return false;
        }
        BuildingBaseEntity buildingEntity = getData().getBuildingEntityByType(BuildingType.HOMELAND);
        return buildingEntity != null;
    }

	public void addGetDressNum(){
		CommanderEntity entity = getData().getCommanderEntity();
		entity.setGetDressCount(entity.getGetDressCount() + 1);
	}

	/**
	 * 充值总额（折算成金条数）
	 * @return
	 */
	public int getRechargeTotal() {
		int rechargeTotal = this.getData().getPlayerBaseEntity().getRechargeTotal();
		if (rechargeTotal < 0) {
			rechargeTotal = this.getData().getRechargeTotal();
			this.getData().getPlayerBaseEntity().setRechargeTotal(rechargeTotal);
		}
		return rechargeTotal;
	}
	
	/**
	 * 充值总额累计
	 * @param amount
	 */
	public void rechargeTotalAdd(int amount) {
		if (amount > 0) {
			this.getData().getPlayerBaseEntity().rechargeTotalAdd(amount);
		}
	}

	public String getFgylRoomId() {
		return fgylRoomId;
	}

	public void setFgylRoomId(String fgylRoomId) {
		this.fgylRoomId = fgylRoomId;
	}

	public FGYLState getFgylState() {
		return fgylState;
	}

	public void setFgylState(FGYLState fgylState) {
		this.fgylState = fgylState;
	}

	public String getXQHXRoomId() {
		return xqhxRoomId;
	}

	public void setXQHXRoomId(String xqhxRoomId) {
		this.xqhxRoomId = xqhxRoomId;
	}

	public XQHXState getXQHXState() {
		return xqhxState;
	}

	public void setXQHXState(XQHXState xqhxState) {
		this.xqhxState = xqhxState;
	}
	
}
