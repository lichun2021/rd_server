package com.hawk.game.world;

import static com.hawk.game.util.GsConst.ModuleType.AUTO_GATHER;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;

import org.hawk.app.HawkApp;
import org.hawk.app.HawkAppObj;
import org.hawk.collection.ConcurrentHashSet;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBManager;
import org.hawk.helper.HawkAssert;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.task.HawkTaskManager;
import org.hawk.tickable.HawkPeriodTickable;
import org.hawk.xid.HawkXID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hawk.game.GsApp;
import com.hawk.game.GsConfig;
import com.hawk.game.battle.NpcPlayer;
import com.hawk.game.config.BattleSoldierCfg;
import com.hawk.game.config.BuffCfg;
import com.hawk.game.config.ConstProperty;
import com.hawk.game.config.DressCfg;
import com.hawk.game.config.DressGroupCfg;
import com.hawk.game.config.GameConstCfg;
import com.hawk.game.config.NationTechCfg;
import com.hawk.game.config.TreasureHuntConstProperty;
import com.hawk.game.config.WarFeverCfg;
import com.hawk.game.config.WorldEnemyCfg;
import com.hawk.game.config.WorldMapConstProperty;
import com.hawk.game.config.WorldMarchConstProperty;
import com.hawk.game.config.WorldResourceCfg;
import com.hawk.game.config.YuristrikeCfg;
import com.hawk.game.crossactivity.CrossActivityService;
import com.hawk.game.crossfortress.CrossFortressService;
import com.hawk.game.crossfortress.IFortress;
import com.hawk.game.entity.ArmyEntity;
import com.hawk.game.entity.DailyDataEntity;
import com.hawk.game.entity.GlobalBuffEntity;
import com.hawk.game.entity.PlayerEntity;
import com.hawk.game.entity.PlayerMonsterEntity;
import com.hawk.game.entity.item.DressItem;
import com.hawk.game.entity.item.SpyMarkItem;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.LocalRedis;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.guild.manor.AbstractBuildable;
import com.hawk.game.guild.manor.building.GuildManorSuperMine;
import com.hawk.game.invoker.MarchBeforeRemoveMsgInvoker;
import com.hawk.game.invoker.MarchVitReturnBackMsgInvoker;
import com.hawk.game.item.AwardItems;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.item.MarchSpeedItem;
import com.hawk.game.item.WarFlagSignUpItem;
import com.hawk.game.lianmengxzq.march.XZQMarchsCollect;
import com.hawk.game.log.BehaviorLogger;
import com.hawk.game.log.BehaviorLogger.Params;
import com.hawk.game.manager.AssembleDataManager;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.march.AutoMonsterMarchParam;
import com.hawk.game.march.MarchSet;
import com.hawk.game.module.autologic.PlayerAutoModule;
import com.hawk.game.module.spacemecha.MechaSpaceInfo;
import com.hawk.game.module.spacemecha.SpaceMechaService;
import com.hawk.game.module.spacemecha.config.SpaceMechaEnemyCfg;
import com.hawk.game.module.spacemecha.worldpoint.SpaceWorldPoint;
import com.hawk.game.module.spacemecha.worldpoint.StrongHoldWorldPoint;
import com.hawk.game.msg.CalcDeadArmy;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerData;
import com.hawk.game.player.hero.PlayerHero;
import com.hawk.game.player.supersoldier.SuperSoldier;
import com.hawk.game.president.PresidentFightService;
import com.hawk.game.president.PresidentTower;
import com.hawk.game.protocol.Armour.ArmourSuitType;
import com.hawk.game.protocol.Army.ArmyChangeCause;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.Const.BuildingType;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.ItemType;
import com.hawk.game.protocol.Const.Result;
import com.hawk.game.protocol.Const.SoldierType;
import com.hawk.game.protocol.GuildWar.PushAllQuarteredMarch;
import com.hawk.game.protocol.GuildWar.PushQuarteredMarchType;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.MechaCore.MechaCoreSuitType;
import com.hawk.game.protocol.World.AtkMonsterAutoMarchStatusPB;
import com.hawk.game.protocol.World.HPBattleResultInfoSync;
import com.hawk.game.protocol.World.KickSnowballDirection;
import com.hawk.game.protocol.World.MarchArmyDetailInfo;
import com.hawk.game.protocol.World.MarchData;
import com.hawk.game.protocol.World.MarchEvent;
import com.hawk.game.protocol.World.MarchEventSync;
import com.hawk.game.protocol.World.NotifyAssistantMarchChange;
import com.hawk.game.protocol.World.TowardsMarchPush;
import com.hawk.game.protocol.World.WorldMarchPB;
import com.hawk.game.protocol.World.WorldMarchRelation;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.queryentity.AccountInfo;
import com.hawk.game.service.BuffService;
import com.hawk.game.service.GuildManorService;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.WarFlagService;
import com.hawk.game.service.flag.FlagCollection;
import com.hawk.game.service.mail.GuildMailService;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mail.YuriMailService;
import com.hawk.game.service.warFlag.IFlag;
import com.hawk.game.superweapon.SuperWeaponService;
import com.hawk.game.superweapon.weapon.IWeapon;
import com.hawk.game.util.AlgorithmPoint;
import com.hawk.game.util.EffectParams;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.LogUtil;
import com.hawk.game.util.WorldUtil;
import com.hawk.game.world.march.IWorldMarch;
import com.hawk.game.world.march.PassiveMarch;
import com.hawk.game.world.march.PlayerMarch;
import com.hawk.game.world.march.impl.CollectWorldResMarch;
import com.hawk.game.world.march.impl.YuriRevengeMonsterMarch;
import com.hawk.game.world.march.submarch.IReportPushMarch;
import com.hawk.game.world.march.submarch.MassMarch;
import com.hawk.game.world.object.Point;
import com.hawk.game.world.service.WorldPlayerService;
import com.hawk.game.world.service.WorldPointService;
import com.hawk.game.world.service.WorldResourceService;
import com.hawk.game.world.thread.WorldThreadScheduler;
import com.hawk.game.world.thread.tasks.NotifyMarchUpdateTask;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.Action;
import com.hawk.log.Source;
import com.hawk.log.LogConst.ArmyChangeReason;
import com.hawk.log.LogConst.ArmySection;
import com.hawk.serialize.string.SerializeHelper;

/**
 * 世界行军全局服务类
 * @author zhenyu.shang
 * @since 2017年8月15日
 */
public class WorldMarchService extends HawkAppObj {

	public static Logger logger = LoggerFactory.getLogger("Server");

	/**
	 * 所有的出征信息
	 */
	private Map<String, IWorldMarch> marchs;
	/**
	 * 玩家主动发起的出征信息
	 */
	private Map<String, BlockingQueue<IWorldMarch>> playerMarchs;
	/**
	 * 玩家被动发起的出征信息，攻击，援助，集结，侦查
	 */
	private Map<String, BlockingQueue<IWorldMarch>> playerPassiveMarchs;
	/**
	 * 起点终点信息记录
	 */
	private Map<Integer, BlockingQueue<String>> worldPointMarchs;
	/**
	 * 有联盟的玩家主动发起的出征信息，本联盟成员发起的集结，单人攻击(打基地，采集打人，驻扎打人) key-- guildId value-- 行军列表
	 */
	private Map<String, ConcurrentHashSet<String>> guildMarchs;
	/**
	 * 国王战行军
	 */
	private BlockingDeque<String> presidentMarchs;
	/**
	 * 国王战箭塔行军
	 */
	private Map<Integer, BlockingDeque<String>> presidentTowerMarchs;
	/**
	 * 超级武器行军
	 */
	private Map<Integer, BlockingDeque<String>> superWeaponMarchs;
	/**
	 * 远征要塞行军
	 */
	private Map<Integer, BlockingDeque<String>> fortressMarchs;
	/**
	 * 领地行军
	 */
	private Map<Integer, BlockingDeque<String>> manorMarch;
	/**
	 * 战旗行军
	 */
	private Map<String, BlockingDeque<String>> flagMarch;
	/**
	 * 自动打野行军参数
	 */
	private Map<String, AutoMonsterMarchParam> autoMarchParamMap;
	
	/**
	 * 获取玩家上次使用雪球攻击的时间
	 */
	private Map<String, Long> lastSnowballAtkTime;
	
	/**
	 * 攻击行为行军返回时间
	 */
	private Map<String, Long> offsiveMarchBackTime = new ConcurrentHashMap<>();
	
	
	/**
	 * 小战区行军
	 */
	private XZQMarchsCollect xzqMarchs;
	
	/**
	 * 单例对象
	 */
	private static WorldMarchService instance = null;

	/**
	 * 获取实体对象
	 * @return
	 */
	public static WorldMarchService getInstance() {
		return instance;
	}

	public WorldMarchService(HawkXID xid) {
		super(xid);
		// 设置实例
		instance = this;
	}

	/**
	 * 初始化地图管理器
	 * @return
	 */
	public boolean init() {
		marchs = new ConcurrentHashMap<String, IWorldMarch>();
		playerMarchs = new ConcurrentHashMap<String, BlockingQueue<IWorldMarch>>();
		playerPassiveMarchs = new ConcurrentHashMap<String, BlockingQueue<IWorldMarch>>();
		worldPointMarchs = new ConcurrentHashMap<Integer, BlockingQueue<String>>();
		guildMarchs = new ConcurrentHashMap<String, ConcurrentHashSet<String>>();
		presidentMarchs = new LinkedBlockingDeque<String>();
		manorMarch = new ConcurrentHashMap<Integer, BlockingDeque<String>>();
		presidentTowerMarchs = new ConcurrentHashMap<Integer, BlockingDeque<String>>();
		superWeaponMarchs = new ConcurrentHashMap<Integer, BlockingDeque<String>>();
		fortressMarchs = new ConcurrentHashMap<Integer, BlockingDeque<String>>();
		flagMarch = new ConcurrentHashMap<String, BlockingDeque<String>>();
		autoMarchParamMap = new ConcurrentHashMap<String, AutoMonsterMarchParam>();
		lastSnowballAtkTime = new ConcurrentHashMap<>();
		xzqMarchs = new XZQMarchsCollect();
		HawkLog.logPrintln("world march service init start");
		
		// 行军管理器初始化
		Set<String> marchPlayerIds = initWorldMarch();
		try {
			// army检测
			boolean openArmyCheck = GameConstCfg.getInstance().isOpenArmyCheck();
			if (openArmyCheck) {
				for (String playerId : marchPlayerIds) {
					Player player = GlobalData.getInstance().makesurePlayer(playerId);
					checkAndFixArmy(player, true);
					
					AccountInfo account = GlobalData.getInstance().getAccountInfoByPlayerId(playerId);
					if (account == null) {
						logger.error("account is null, playerId:{}", playerId);
						continue;
					}
					account.setArmyFixed(true);
				}
			}
			
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		HawkLog.logPrintln("world march service init end");
		
		return true;
	}

	/**
	 * 初始化世界行军
	 */
	private Set<String> initWorldMarch() {
		Set<String> playerIdSet = new HashSet<String>();
		
		// 加载所有行军
		List<WorldMarch> marchList = HawkDBManager.getInstance().query("from WorldMarch where invalid = 0");
		
		for (WorldMarch march : marchList) {
			
			try {
				
				Player player = GlobalData.getInstance().makesurePlayer(march.getPlayerId());
				if (player == null) {
					logger.error("marchInitError, player null, marchId:{}, playerId:{}", march.getMarchId(), march.getPlayerId());
					continue;
				}
				
				IWorldMarch worldMarch = WorldMarchService.getInstance().getMarch(march.getMarchId());
				if (worldMarch != null) {
					logger.error("marchInitError, march init already, marchId:{}, playerId:{}", march.getMarchId(), march.getPlayerId());
					continue;
				}
				
				// 组装行军
				worldMarch = march.wrapUp();
				
				if (worldMarch == null) {
					march.delete(false);
					logger.error("marchInitError, march type cant find, marchId:{}, marchType:{}, playerId:{}", march.getMarchId(), march.getMarchType(), march.getPlayerId());
					continue;
				}
				
				// 注册行军
				worldMarch.register();
				
				// 有行军的玩家id
				playerIdSet.add(worldMarch.getPlayerId());
				
				// 日志记录
				logger.info("marchInitSuccess, marchId:{}, playerId:{} ", march.getMarchId(), worldMarch.getPlayerId());
				
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}

		// 预先加载玩家数据
		preloadMarchPlayerData(playerIdSet);
		
		// 创建行军更新机制, 并加入世界线程
		WorldThreadScheduler.getInstance().addWorldTickable(new HawkPeriodTickable(500) {
			@Override
			public void onPeriodTick() {
				long beginTimeMs = HawkTime.getMillisecond();
				try {
					onWorldMarchTick();
				} catch (Exception e) {
					HawkException.catchException(e);
				} finally {
					// 时间消耗的统计信息
					long costTimeMs = HawkTime.getMillisecond() - beginTimeMs;
					if (costTimeMs > GsConfig.getInstance().getProtoTimeout()) {
						logger.warn("process march tick too much time, costtime: {}", costTimeMs);
					}
				}
			}
		});
		
		return playerIdSet;
	}

	/**
	 * 预加载玩家数据
	 */
	public boolean preloadMarchPlayerData(Set<String> playerIds) {
		try {
			
			long activeTime = 6 * 3600 * 1000L;
			long currTime = HawkTime.getMillisecond();
			
			HawkLog.logPrintln("wait preload player data");
			
			for (String playerId : playerIds) {
				
				Player player = GlobalData.getInstance().makesurePlayer(playerId);
				
				// 加载基础数据
				PlayerEntity playerEntity = player.getData().getPlayerEntity();
				player.getData().getPlayerBaseEntity();
				player.getData().getArmyEntities();
				
				// 最近活跃的才加载
				if (currTime - playerEntity.getLoginTime() <= activeTime || currTime - playerEntity.getLogoutTime() <= activeTime) {
					player.getData().loadStart();
				}

				HawkLog.logPrintln("preload player data success: {}", playerId);
			}

			HawkLog.logPrintln("preload player data success, count: {}, costtime: {}", playerIds.size(), HawkTime.getMillisecond() - currTime);

			return true;
		} catch (Exception e) {
			HawkException.catchException(e);
		} finally {
			
		}
		return false;
	}
	
	/**
	 * 更新点上的行军信息
	 * @param march
	 */
	public void updatePointMarchInfo(IWorldMarch march, boolean removeFromPoint) {
		int origionX = march.getMarchEntity().getOrigionX();
		int origionY = march.getMarchEntity().getOrigionY();

		// 是否从点上移除行军
		if (!removeFromPoint) {
			addWorldPointMarch(origionX, origionY, march);
			addWorldPointMarch(march.getMarchEntity().getTerminalX(), march.getMarchEntity().getTerminalY(), march);
		} else {
			removeWorldPointMarch(origionX, origionY, march);
			removeWorldPointMarch(march.getMarchEntity().getTerminalX(), march.getMarchEntity().getTerminalY(), march);
		}
		
		// 如果有召回信息
		double callBackX = march.getMarchEntity().getCallbackX();
		double callBackY = march.getMarchEntity().getCallbackY();
		if (callBackX > 1.0 && callBackY > 1.0) {
			origionX = (int) callBackX;
			origionY = (int) callBackY;
		}
	}

	/**
	 * 行军心跳处理
	 */
	public void onWorldMarchTick() {
		if (!GsApp.getInstance().isInitOK()) {
			return;
		}
		
		for (IWorldMarch march : marchs.values()) {
			march.heartBeats();
		}
	}

	/**
	 * 获取玩家的X类型的行军列表
	 * 
	 * @param playerId
	 * @return
	 */
	public Set<IWorldMarch> getPlayerTypeMarchs(String playerId, int marchType) {
		Set<IWorldMarch> collectMarchs = new HashSet<IWorldMarch>();
		BlockingQueue<IWorldMarch> playerMarchSet = getPlayerMarch(playerId);

		Iterator<IWorldMarch> iterator = playerMarchSet.iterator();
		while (iterator.hasNext()) {
			IWorldMarch march = iterator.next();
			if (march.getMarchType().getNumber() == marchType) {
				collectMarchs.add(march);
			}
		}
		return collectMarchs;
	}

	public Collection<IWorldMarch> getMarchsValue() {
		return Collections.unmodifiableCollection(marchs.values());
	}

	public IWorldMarch getMarch(String marchId) {
		if(Objects.isNull(marchId)){
			return null;
		}
		return marchs.get(marchId);
	}

	public WorldMarch getWorldMarch(String marchId) {
		IWorldMarch worldMarch = getMarch(marchId);
		if (worldMarch != null) {
			return worldMarch.getMarchEntity();
		}
		return null;
	}

	public boolean checkMarchExist(String marchId) {
		return marchs.containsKey(marchId);
	}

	public void registerMarchs(IWorldMarch march) {
		marchs.put(march.getMarchId(), march);
	}

	public int getMarchsSize() {
		return marchs.size();
	}

	public void onlyRemoveMarch(String marchId) {
		marchs.remove(marchId);
	}

	/**
	 * 获取玩家出征队列数量
	 * 
	 * @param playerId
	 * @return
	 */
	public int getPlayerMarchCount(String playerId) {
		BlockingQueue<IWorldMarch> marchs = getPlayerMarch(playerId);
		if (marchs != null) {
			return marchs.size();
		}
		return 0;
	}

	/**
	 * 获得玩家主动出征的行军
	 * @param playerId
	 * @return
	 */
	public BlockingQueue<IWorldMarch> getPlayerMarch(String playerId) {
		HawkAssert.notNull(playerId);
		if (!playerMarchs.containsKey(playerId)) {
			playerMarchs.put(playerId, new LinkedBlockingQueue<IWorldMarch>());
		}
		return playerMarchs.get(playerId);
	}

	/**
	 * 获取玩家在某个点上停留的行军
	 * @param playerId
	 * @param pointId
	 * @return
	 */
	public List<IWorldMarch> getPlayerPointMarch(String playerId, int pointId) {
		List<IWorldMarch> retMarchs = new ArrayList<IWorldMarch>();
		BlockingQueue<IWorldMarch> playerMarch = getPlayerMarch(playerId);
		for (IWorldMarch march : playerMarch) {
			if (WorldUtil.isMarchState(march.getMarchEntity())) {
				continue;
			}
			if (WorldUtil.isReturnBackMarch(march)) {
				continue;
			}
			if (march.getMarchEntity().getTerminalId() != pointId) {
				continue;
			}
			retMarchs.add(march);
		}
		return retMarchs;
	}
	
	/**
	 * 根据队列类型
	 * 
	 * @param playerId
	 * @param marchType 队列类型
	 * @param marchStatus 队列状态
	 * @return
	 */
	public IWorldMarch getPlayerMarch(String playerId, String marchId) {
		IWorldMarch march = getMarch(marchId);
		if (march != null && !march.getMarchEntity().isInvalid() && march.getPlayerId().equals(playerId)) {
			return march;
		}
		return null;
	}

	/**
	 * 根据队列类型
	 * 
	 * @param playerId
	 * @param marchType
	 *            队列类型
	 * @return
	 */
	public List<IWorldMarch> getPlayerMarch(String playerId, int marchType) {
		BlockingQueue<IWorldMarch> marchs = getPlayerMarch(playerId);
		if (marchs == null || marchs.size() == 0) {
			return Collections.emptyList();
		}

		List<IWorldMarch> retMarchs = new ArrayList<IWorldMarch>();
		for (IWorldMarch march : marchs) {
			if (march != null && marchType == march.getMarchType().getNumber()) {
				retMarchs.add(march);
			}
		}
		return retMarchs;
	}

	/**
	 * 获取玩家发起的集结行军
	 */
	public List<IWorldMarch> getPlayerMassMarch(String playerId) {
		List<IWorldMarch> retMarchs = new ArrayList<>();
		for (IWorldMarch march : getPlayerMarch(playerId)) {
			if (!march.isMassMarch()) {
				continue;
			}
			retMarchs.add(march);
		}
		return retMarchs;
	}
	
	/**
	 * 获取自动打野行军
	 * 
	 * @param playerId
	 * @return
	 */
	public List<IWorldMarch> getAutoMonsterMarch(String playerId) {
		List<IWorldMarch> marchs = new ArrayList<IWorldMarch>();
		for (IWorldMarch march : getPlayerMarch(playerId)) {
			if (march.getMarchEntity().getAutoMarchIdentify() > 0) {
				marchs.add(march);
			}
		}
		
		return marchs;
	}

	/**
	 * 集结行军主动出发
	 * 
	 * @param march
	 */
	public void doMassMarchStart(WorldMarch march) {
		long currentTime = HawkTime.getMillisecond();
		long hasWaitTime = currentTime - march.getMassReadyTime();
		march.setStartTime(HawkTime.getMillisecond());
		march.setEndTime(march.getEndTime() - hasWaitTime);
	}

	/**
	 * 是否有发起的集结类型的行军
	 */
	public boolean hasMassMarch(String playerId) {
		Optional<IWorldMarch> op = getPlayerMarch(playerId).stream().filter(march -> march.isMassMarch()).findAny();
		return op.isPresent();
	}

	/**
	 * 添加主动行军到玩家主动表，内部调用
	 * @param march
	 */
	public void registerPlayerMarch(IWorldMarch march) {
		BlockingQueue<IWorldMarch> marchs = getPlayerMarch(march.getPlayerId());
		marchs.add(march);
	}

	/**
	 * 删除主动行军
	 * @param playerId
	 * @param march
	 */
	public void removePlayerMarch(IWorldMarch march) {
		BlockingQueue<IWorldMarch> playerMarch = getPlayerMarch(march.getPlayerId());
		if (playerMarch == null) {
			return;
		}
		playerMarch.remove(march);
	}

	/**
	 * 增加被动行军
	 * @param playerId
	 * @param march
	 */
	public void registerPlayerPassiveMarch(String playerId, IWorldMarch march) {
		if (HawkOSOperator.isEmptyString(playerId)) {
			logger.error("registerPlayerPassiveMarch error, march:{}", march.toString());
			return;
		}
		BlockingQueue<IWorldMarch> marchs = getPlayerPassiveMarch(playerId);
		marchs.add(march);
	}

	/**
	 * 获得玩家的被动行军表
	 * @param playerId
	 * @return
	 */
	public BlockingQueue<IWorldMarch> getPlayerPassiveMarch(String playerId) {
		HawkAssert.notNull(playerId);
		if (!playerPassiveMarchs.containsKey(playerId)) {
			playerPassiveMarchs.put(playerId, new LinkedBlockingQueue<IWorldMarch>());
		}
		return playerPassiveMarchs.get(playerId);
	}

	/**
	 * 获得玩家被动出征的信息
	 * 
	 * @param playerId
	 * @param marchType
	 * @param marchStatus
	 * @return
	 */
	public Set<IWorldMarch> getPlayerPassiveMarchs(String playerId, int marchType, int marchStatus) {
		BlockingQueue<IWorldMarch> marchs = getPlayerPassiveMarch(playerId);
		if (marchs == null || marchs.size() == 0) {
			return Collections.emptySet();
		}

		Set<IWorldMarch> retMarchs = new HashSet<IWorldMarch>();
		for (IWorldMarch march : marchs) {
			if (march != null && marchType == march.getMarchType().getNumber() && marchStatus == march.getMarchEntity().getMarchStatus()) {
				retMarchs.add(march);
			}
		}
		return retMarchs;
	}

	/**
	 * 获得玩家被动出征的信息
	 * 
	 * @param playerId
	 * @param marchType
	 * @param marchStatus
	 * @return
	 */
	public Set<IWorldMarch> getPlayerPassiveMarchs(String playerId, int marchType) {
		BlockingQueue<IWorldMarch> marchs = getPlayerPassiveMarch(playerId);
		Set<IWorldMarch> retMarchs = new HashSet<IWorldMarch>();
		for (IWorldMarch march : marchs) {
			if (march != null && marchType == march.getMarchType().getNumber()) {
				retMarchs.add(march);
			}
		}
		return retMarchs;
	}

	/**
	 * 删除被动行军
	 * @param playerId
	 * @param marchId
	 */
	public void removePlayerPassiveMarch(String playerId, IWorldMarch march) {
		BlockingQueue<IWorldMarch> playerMarch = getPlayerPassiveMarch(playerId);
		if (playerMarch == null) {
			return;
		}
		playerMarch.remove(march);
	}

	/**
	 * 获取联盟战争
	 * @param guildId
	 * @return
	 */
	public Collection<IWorldMarch> getGuildMarchs(String guildId) {
		List<IWorldMarch> retMarchs = new ArrayList<IWorldMarch>();
		
		if (HawkOSOperator.isEmptyString(guildId)) {
			return Collections.unmodifiableCollection(retMarchs);
		}
		
		ConcurrentHashSet<String> guildMarchIds = getGuildMarchIds(guildId);
		for (String marchId : guildMarchIds) {
			IWorldMarch march = getMarch(marchId);
			if(march == null || march.getMarchEntity().isInvalid()){
				logger.warn("guildMarchs march is null , remove marchId: {}", marchId);
				rmGuildMarch(marchId);
				continue;
			}
			retMarchs.add(march);
		}
		
		return Collections.unmodifiableCollection(retMarchs);
	}
	
	/**
	 * 获取联盟战争
	 * @param guildId
	 * @return
	 */
	private ConcurrentHashSet<String> getGuildMarchIds(String guildId) {
		if (!guildMarchs.containsKey(guildId)) {
			guildMarchs.put(guildId, new ConcurrentHashSet<String>());
		}
		return guildMarchs.get(guildId);
	}
	
	/**
	 * 添加联盟战争
	 * @param worldMarch
	 */
	public void addGuildMarch(IWorldMarch worldMarch) {
		// 行军发起玩家id
		String fromPlayerId = worldMarch.getPlayerId();
		if (worldMarch.getMarchType() != WorldMarchType.YURI_MONSTER && worldMarch.getMarchType() != WorldMarchType.SPACE_MECHA_MONSTER_ATTACK_MARCH 
			&& worldMarch.getMarchType() != WorldMarchType.SPACE_MECHA_EMPTY_MARCH && !HawkOSOperator.isEmptyString(fromPlayerId)) {
			String fromGuildId = GuildService.getInstance().getPlayerGuildId(fromPlayerId);
			addGuildMarch(fromGuildId, worldMarch.getMarchId());
		}

		// 行军目标方联盟战争
		int terminalId = worldMarch.getMarchEntity().getTerminalId();
		WorldPoint terminaPoint = WorldPointService.getInstance().getWorldPoint(terminalId);
		String toGuildId = WorldPointService.getInstance().getGuildIdByPoint(terminaPoint);
		
		if (!HawkOSOperator.isEmptyString(toGuildId)) {
			addGuildMarch(toGuildId, worldMarch.getMarchId());
		}
	}
	
	/**
	 * 添加联盟战争
	 * @param guildId
	 * @param marchId
	 */
	public void addGuildMarch(String guildId, String marchId) {
		if (HawkOSOperator.isEmptyString(marchId) || HawkOSOperator.isEmptyString(guildId)) {
			return;
		}
		
		ConcurrentHashSet<String> guildMarchIds = getGuildMarchIds(guildId);
		if (!guildMarchIds.contains(marchId)) {
			guildMarchIds.add(marchId);
			
			// 推送联盟战争条数
			GuildService.getInstance().pushGuildWarCount(guildId);
		}
	}
	
	/**
	 * 删除联盟战争
	 * @param guildId
	 */
	public void removeGuildAllMarch(String guildId) {
		if (guildMarchs.containsKey(guildId)) {
			guildMarchs.remove(guildId);
		}
	}
	
	/**
	 * 删除联盟战争(删除双方联盟的联盟战争显示)
	 * @param marchId
	 */
	public void rmGuildMarch(String marchId) {
		if (HawkOSOperator.isEmptyString(marchId)) {
			return;
		}
		
		for (String guildId : guildMarchs.keySet()) {
			ConcurrentHashSet<String> guildMarchIds = getGuildMarchIds(guildId);
			if (!guildMarchIds.contains(marchId)) {
				continue;
			}
			guildMarchIds.remove(marchId);
			
			// 推送联盟战争条数
			GuildService.getInstance().pushGuildWarCount(guildId);
		}
	}
	
	/**
	 * 删除联盟战争(只删除一方联盟的联盟战争显示)
	 * @param marchId
	 */
	public void rmGuildMarch(String marchId, String guildId) {
		if (HawkOSOperator.isEmptyString(marchId) || HawkOSOperator.isEmptyString(guildId)) {
			return;
		}
		
		ConcurrentHashSet<String> guildMarchIds = getGuildMarchIds(guildId);
		if (!guildMarchIds.contains(marchId)) {
			return;
		}
		
		// 删除联盟战争
		guildMarchIds.remove(marchId);
		// 推送联盟战争条数
		GuildService.getInstance().pushGuildWarCount(guildId);
	}
	
	public Collection<IWorldMarch> getWorldPointMarch(int pointId) {
		int[] pos = GameUtil.splitXAndY(pointId);
		return getWorldPointMarch(pos[0], pos[1]);
	}
	/**
	 * 获取起点终点行军
	 * @param x
	 * @param y
	 * @return
	 */
	public Collection<IWorldMarch> getWorldPointMarch(int x, int y) {
		BlockingQueue<String> worldPointMarch = worldPointMarchs.get(GameUtil.combineXAndY(x, y));
		BlockingQueue<IWorldMarch> marchs = new LinkedBlockingQueue<IWorldMarch>();
		if (worldPointMarch == null) {
			return Collections.unmodifiableCollection(marchs);
		}
		for (String marchId : worldPointMarch) {
			IWorldMarch march = getMarch(marchId);
			if(march == null){
				worldPointMarch.remove(marchId);
				logger.warn("worldPointMarch march is null , remove marchId: {}", marchId);
				continue;
			}
			marchs.add(march);
		}
		return Collections.unmodifiableCollection(marchs);
	}

	protected void addWorldPointMarch(int x, int y, IWorldMarch march) {
		if (x < 0 || y < 0 || march == null) {
			return;
		}
		BlockingQueue<String> worldPointMarch = worldPointMarchs.get(GameUtil.combineXAndY(x, y));
		if (worldPointMarch == null) {
			worldPointMarch = new LinkedBlockingQueue<String>();
		}
		worldPointMarch.add(march.getMarchId());
		worldPointMarchs.put(GameUtil.combineXAndY(x, y), worldPointMarch);
	}

	public void removeWorldPointMarch(int x, int y, IWorldMarch march) {
		BlockingQueue<String> worldPointMarch = worldPointMarchs.get(GameUtil.combineXAndY(x, y));
		if (worldPointMarch == null) {
			return;
		}
		worldPointMarch.remove(march.getMarchId());
	}

	public void removeWorldPointAllMarch(int x, int y) {
		// 此处是因为在初始化时,worldPointMarch还未创建
		if (worldPointMarchs == null) {
			return;
		}
		int pointId = GameUtil.combineXAndY(x, y);
		if (worldPointMarchs.containsKey(pointId)) {
			worldPointMarchs.remove(pointId);
		}
	}

	/**
	 * 获取首都行军
	 * @return
	 */
	public BlockingDeque<String> getPresidentMarchs() {
		return presidentMarchs;
	}

	/**
	 * 更换司令部驻军队长
	 * @param pointId
	 * @param targetPlayerId
	 */
	public void changePresidentMarchLeader(String targetPlayerId) {
		String changeMarchId = null;
		
		BlockingDeque<String> marchs = getPresidentMarchs();
		for (String marchId : marchs) {
			IWorldMarch march = WorldMarchService.getInstance().getMarch(marchId);
			if (!targetPlayerId.equals(march.getPlayerId())) {
				continue;
			}
			changeMarchId = march.getMarchId();
			break;
		}
		
		if (HawkOSOperator.isEmptyString(changeMarchId)) {
			return;
		}
		
		marchs.remove(changeMarchId);
		marchs.addFirst(changeMarchId);
	}
	
	/**
	 * 更换战旗驻军队长
	 */
	public void changeFlagMarchLeader(String flagId, String targetPlayerId) {
		String changeMarchId = null;
		
		BlockingDeque<String> marchs = getFlagMarchs(flagId);
		for (String marchId : marchs) {
			IWorldMarch march = WorldMarchService.getInstance().getMarch(marchId);
			if (!targetPlayerId.equals(march.getPlayerId())) {
				continue;
			}
			changeMarchId = march.getMarchId();
			break;
		}
		
		if (HawkOSOperator.isEmptyString(changeMarchId)) {
			return;
		}
		
		marchs.remove(changeMarchId);
		marchs.addFirst(changeMarchId);
	}
	
	/**
	 * 添加首都行军
	 * @param march
	 * @param init 是否是初始化
	 */
	public void addPresidentMarch(IWorldMarch march, boolean init) {
		BlockingDeque<String> marchs = getPresidentMarchs();
		
		if (init) {
			if (march.getPlayerId().equals(march.getMarchEntity().getLeaderPlayerId())) {
				marchs.addFirst(march.getMarchId());
			} else {
				marchs.add(march.getMarchId());
			}
		} else {
			if(marchs.isEmpty()){
				march.getMarchEntity().setLeaderPlayerId(march.getPlayerId());
				marchs.add(march.getMarchId());
				
				// 国王战广播驻军变更
				PresidentFightService.getInstance().getPresidentCity().broadcastPresidentInfo(null);
			} else {
				WorldMarch leader = getWorldMarch(marchs.getFirst());
				march.getMarchEntity().setLeaderPlayerId(leader.getPlayerId());
				marchs.add(march.getMarchId());
			}
		}
	}

	/**
	 * 移除首都行军
	 * @param marchId
	 */
	private void rmPresidentMarch(String marchId) {
		if (!presidentMarchs.contains(marchId)) {
			return;
		}
		presidentMarchs.remove(marchId);
	}

	/**
	 * 是否有首都行军
	 * @return
	 */
	public boolean hasPresidentMarch() {
		return !presidentMarchs.isEmpty();
	}

	/**
	 * 国王战驻扎队长行军
	 * @return
	 */
	public String getPresidentLeaderMarch() {
		if (hasPresidentMarch()) {
			return presidentMarchs.getFirst();
		}
		return null;
	}
	
	/**
	 * 总统府队长
	 * @return
	 */
	public Player getPresidentLeader() {
		String leaderMarchId = getPresidentLeaderMarch();
		if (HawkOSOperator.isEmptyString(leaderMarchId)) {
			return null;
		}
		
		IWorldMarch leaderMarch = getMarch(leaderMarchId);
		Player leader = GlobalData.getInstance().makesurePlayer(leaderMarch.getPlayerId());
		return leader;
	}
	
	/**
	 * 战旗队长
	 */
	public Player getFlagLeader(String flagId) {
		String leaderMarchId = getFlagLeaderMarchId(flagId);
		if (HawkOSOperator.isEmptyString(leaderMarchId)) {
			return null;
		}
		
		IWorldMarch leaderMarch = getMarch(leaderMarchId);
		if (leaderMarch == null) {
			return null;
		}
		Player leader = GlobalData.getInstance().makesurePlayer(leaderMarch.getPlayerId());
		return leader;
	}
	
	/**
	 * 是否有首都行军
	 * @return
	 */
	public boolean hasPresidentTowerMarch(int index) {
		return !getPresidentTowerMarchs(index).isEmpty();
	}
	
	/**
	 * 总统府箭塔队长
	 * @return
	 */
	public Player getPresidentTowerLeader(int pointId) {
		String leaderMarchId = getPresidentTowerLeaderMarchId(pointId);
		if (HawkOSOperator.isEmptyString(leaderMarchId)) {
			return null;
		}
		
		IWorldMarch leaderMarch = getMarch(leaderMarchId);
		Player leader = GlobalData.getInstance().makesurePlayer(leaderMarch.getPlayerId());
		return leader;
	}
	
	/**
	 * 更换司令部箭塔驻军队长
	 * @param pointId
	 * @param targetPlayerId
	 */
	public void changePresidentTowerMarchLeader(int pointId, String targetPlayerId) {
		String changeMarchId = null;
		
		BlockingDeque<String> marchs = getPresidentTowerMarchs(pointId);
		for (String marchId : marchs) {
			IWorldMarch march = WorldMarchService.getInstance().getMarch(marchId);
			if (!targetPlayerId.equals(march.getPlayerId())) {
				continue;
			}
			changeMarchId = march.getMarchId();
			break;
		}
		
		if (HawkOSOperator.isEmptyString(changeMarchId)) {
			return;
		}
		
		marchs.remove(changeMarchId);
		marchs.addFirst(changeMarchId);
	}
	
	/**
	 * 领地行军
	 * @param manorId
	 * @return
	 */
	public BlockingDeque<String> getManorMarchs(WorldPoint wp) {
		if (wp == null) {
			return new LinkedBlockingDeque<String>();
		}
		return getManorMarchs(wp.getId());
	}

	public BlockingDeque<String> getManorMarchs(int pointId) {
		if (!manorMarch.containsKey(pointId)) {
			BlockingDeque<String> marchs = new LinkedBlockingDeque<String>();
			manorMarch.put(pointId, marchs);
		}
		return manorMarch.get(pointId);
	}

	/**
	 * 获取战旗行军
	 */
	public BlockingDeque<String> getFlagMarchs(String flagId) {
		if (!flagMarch.containsKey(flagId)) {
			BlockingDeque<String> marchs = new LinkedBlockingDeque<String>();
			flagMarch.put(flagId, marchs);
		}
		return flagMarch.get(flagId);
	}
	
	public void addManorMarchs(int pointId, IWorldMarch march, boolean isInit) {
		BlockingDeque<String> marchs = getManorMarchs(pointId);
		//初始化需要判断队长行军
		if(isInit){
			if(march.getPlayerId().equals(march.getMarchEntity().getLeaderPlayerId())){
				marchs.addFirst(march.getMarchId());
			} else {
				marchs.add(march.getMarchId());
			}
		} else {
			//如果没有行军，则此行军为队长行军
			if(marchs.isEmpty()){
				march.getMarchEntity().setLeaderPlayerId(march.getPlayerId());
			} else {
				WorldMarch leader = getWorldMarch(marchs.getFirst());
				march.getMarchEntity().setLeaderPlayerId(leader.getPlayerId());
			}
			marchs.add(march.getMarchId());
		}
	}

	/**
	 * 添加战旗行军
	 */
	public void addFlagMarchs(String flagId, IWorldMarch march, boolean isInit) {
		BlockingDeque<String> marchs = getFlagMarchs(flagId);
		if (marchs.contains(march.getMarchId())) {
			return;
		}
		//初始化需要判断队长行军
		if(isInit){
			if(march.getPlayerId().equals(march.getMarchEntity().getLeaderPlayerId())){
				marchs.addFirst(march.getMarchId());
			} else {
				marchs.add(march.getMarchId());
			}
		} else {
			//如果没有行军，则此行军为队长行军
			if(marchs.isEmpty()){
				march.getMarchEntity().setLeaderPlayerId(march.getPlayerId());
			} else {
				WorldMarch leader = getWorldMarch(marchs.getFirst());
				if (leader == null) {
					marchs.removeFirst();
					march.getMarchEntity().setLeaderPlayerId(march.getPlayerId());
				} else {
					march.getMarchEntity().setLeaderPlayerId(leader.getPlayerId());
				}
			}
			marchs.add(march.getMarchId());
		}
	}
	
	public void removeManorMarch(int pointId, String marchId) {
		BlockingDeque<String> marchs = getManorMarchs(pointId);
		if (!marchs.contains(marchId)) {
			return;
		}
		marchs.remove(marchId);
		
		//移除队列后需要重置march的leaderId
		if(!marchs.isEmpty()){
			String leaderId = marchs.getFirst();
			for (String mid : marchs) {
				WorldMarch march = getWorldMarch(mid);
				if(march == null){
					continue;
				}
				march.setLeaderPlayerId(leaderId);
			}
		}
	}

	public void removeFlagMarch(String flagId, String marchId) {
		BlockingDeque<String> marchs = getFlagMarchs(flagId);
		if (!marchs.contains(marchId)) {
			return;
		}
		marchs.remove(marchId);
		
		//移除队列后需要重置march的leaderId
		if(!marchs.isEmpty()){
			String leaderId = marchs.getFirst();
			for (String mid : marchs) {
				WorldMarch march = getWorldMarch(mid);
				if(march == null){
					continue;
				}
				march.setLeaderPlayerId(leaderId);
			}
		}
	}
	
	public void changeManorMarchLeader(int pointId, String targetPlayerId) {
		String changeMarchId = null;
		
		BlockingDeque<String> marchs = getManorMarchs(pointId);
		for (String marchId : marchs) {
			IWorldMarch march = WorldMarchService.getInstance().getMarch(marchId);
			if (!targetPlayerId.equals(march.getPlayerId())) {
				continue;
			}
			changeMarchId = march.getMarchId();
			break;
		}
		
		if (HawkOSOperator.isEmptyString(changeMarchId)) {
			return;
		}
		
		marchs.remove(changeMarchId);
		marchs.addFirst(changeMarchId);
	}
	
	public void changeFlagLeader(String flagId, String targetPlayerId) {
		String changeMarchId = null;
		
		BlockingDeque<String> marchs = getFlagMarchs(flagId);
		for (String marchId : marchs) {
			IWorldMarch march = WorldMarchService.getInstance().getMarch(marchId);
			if (!targetPlayerId.equals(march.getPlayerId())) {
				continue;
			}
			changeMarchId = march.getMarchId();
			break;
		}
		
		if (HawkOSOperator.isEmptyString(changeMarchId)) {
			return;
		}
		
		marchs.remove(changeMarchId);
		marchs.addFirst(changeMarchId);
	}
	
	public void removeAllManorMarch(int pointId) {
		if (!manorMarch.containsKey(pointId)) {
			return;
		}
		manorMarch.remove(pointId);
	}

	/**
	 * 获取总统府箭塔行军
	 * @param pointId
	 * @return
	 */
	public BlockingDeque<String>getPresidentTowerMarchs(int pointId) {
		if (!presidentTowerMarchs.containsKey(pointId)) {
			BlockingDeque<String> marchs = new LinkedBlockingDeque<String>();
			presidentTowerMarchs.put(pointId, marchs);
		}
		return presidentTowerMarchs.get(pointId);
	}
	
	/**
	 * 获取总统府箭塔行军
	 * @param pointId
	 * @return
	 */
	public List<IWorldMarch> getPresidentTowerStayMarchs(int pointId) {
		BlockingDeque<String> marchIds = getPresidentTowerMarchs(pointId);
		List<IWorldMarch> stayMarchs = new ArrayList<IWorldMarch>();
		
		for (String marchId : marchIds) {
			IWorldMarch march = getMarch(marchId);
			if (march == null || march.getMarchEntity().isInvalid()) {
				continue;
			}
			stayMarchs.add(march);
		}
		return stayMarchs;
	}
	
	/**
	 * 获取总统府箭塔队长行军id
	 * @param pointId
	 * @return
	 */
	public String getPresidentTowerLeaderMarchId(int pointId) {
		BlockingDeque<String> marchIds = getPresidentTowerMarchs(pointId);
		if (marchIds.isEmpty()) {
			return null;
		}
		return marchIds.getFirst();
	}
	
	
	/**
	 * 添加总统府箭塔行军
	 * @param pointId
	 * @param march
	 * @param isInit
	 */
	public void addPresidentTowerMarch(int pointId, IWorldMarch march, boolean isInit) {
		BlockingDeque<String> marchs = getPresidentTowerMarchs(pointId);
		//初始化需要判断队长行军
		if(isInit){
			if(march.getPlayerId().equals(march.getMarchEntity().getLeaderPlayerId())){
				marchs.addFirst(march.getMarchId());
			} else {
				marchs.add(march.getMarchId());
			}
		} else {
			//如果没有行军，则此行军为队长行军
			if(marchs.isEmpty()){
				march.getMarchEntity().setLeaderPlayerId(march.getPlayerId());
				marchs.add(march.getMarchId());
				
				// 广播箭塔状态改变
				PresidentTower presidentTower = PresidentFightService.getInstance().getPresidentTower(pointId);
				presidentTower.broadcastPresidentTowerInfo(null);
				
			} else {
				WorldMarch leaderMarch = getWorldMarch(marchs.getFirst());
				march.getMarchEntity().setLeaderPlayerId(leaderMarch.getPlayerId());
				marchs.add(march.getMarchId());
			}
		}
	}
	
	/**
	 * 移除总统府箭塔行军
	 * @param pointId
	 * @param rmMarchId
	 */
	public void removePresidentTowerMarch(int pointId, String rmMarchId) {
		BlockingDeque<String> towerMarchIds = getPresidentTowerMarchs(pointId);
		if (!towerMarchIds.contains(rmMarchId)) {
			return;
		}
		
		// 是否是队长行军
		boolean isLeaderMarch = rmMarchId.equals(towerMarchIds.getFirst());
		
		towerMarchIds.remove(rmMarchId);
		
		if (towerMarchIds.isEmpty()) {
			PresidentFightService.getInstance().changeTowerOccuption(null, pointId, null, null);
			return;
		}
		
		if (isLeaderMarch)  {
			// 重设队长
			String newLeaderMarchId = towerMarchIds.getFirst();
			IWorldMarch leaderMarch = getMarch(newLeaderMarchId);
			Player newLeader = GlobalData.getInstance().makesurePlayer(leaderMarch.getPlayerId());
			for (String marchId : towerMarchIds) {
				IWorldMarch march = getMarch(marchId);
				march.getMarchEntity().setLeaderPlayerId(newLeader.getId());
			}
			
			// 广播箭塔状态改变
			PresidentTower presidentTower = PresidentFightService.getInstance().getPresidentTower(pointId);
			presidentTower.broadcastPresidentTowerInfo(null);
		}
		
	}
	
	/**
	 * 生成一个行军对象
	 */
	public IWorldMarch genMarch(Player player, int marchType, int origionId, int terminalId, String targetId,
			WorldPoint terminalPoint, int waitTime, String assistantStr, boolean needStoreDB, EffectParams effParams) {

		if(player == null){
			player = new NpcPlayer(HawkXID.nullXid());
		}
		
		long startTime = HawkTime.getMillisecond();

		WorldMarch march = new WorldMarch();
		march.setArmys(effParams.getArmys());
		march.setStartTime(startTime);
		march.setMarchStatus(WorldMarchStatus.MARCH_STATUS_MARCH_VALUE);
		march.setOrigionId(origionId);
		march.setTerminalId(terminalId);
		march.setAlarmPointId(terminalId);
		march.setTargetId(targetId);
		march.setMarchType(marchType);
		march.setSuperSoldierId(effParams.getSuperSoliderId());
		if (player != null) {
			march.setPlayerId(player.getId());
			march.setPlayerName(player.getName());
		}
		if(Objects.nonNull(effParams.getHeroIds())){
			march.setHeroIdList(effParams.getHeroIds());
		}
		
		if (effParams.getArmourSuit() != null) {
			int armourSuit = effParams.getArmourSuit().getNumber();
			if (armourSuit > 0 && armourSuit <= player.getEntity().getArmourSuitCount()) {
				march.setArmourSuit(armourSuit);
			}
		}
		MechaCoreSuitType mechaSuit = effParams.getMechacoreSuit();
		if (mechaSuit != null && player.getPlayerMechaCore().isSuitUnlocked(mechaSuit.getNumber())) {
			march.setMechacoreSuit(mechaSuit.getNumber());
		} else {
			march.setMechacoreSuit(player.getPlayerMechaCore().getWorkSuit());
		}

		List<Integer> dressList = getMarchDressGroup(player.getData(), effParams);
		if (dressList.size() > 0) {
			march.setDressList(dressList);
			effParams.setDressList(dressList);
		}
		
		march.setTalentType(effParams.getTalent());
		march.setSuperLab(effParams.getSuperLab());
		// 把个人编队记录下来
		if (effParams.getWorldmarchReq() != null) {
			int formation = effParams.getWorldmarchReq().getFormation();
			if (formation != 0) {
				march.setFormation(formation);
			}
		}
		// 超武
		int atkId = effParams.getManhattanAtkSwId();
		if (atkId > 0) {
			march.setManhattanAtkSwId(atkId);
		}
		int defId = effParams.getManhattanDefSwId();
		if (defId > 0) {
			march.setManhattanDefSwId(defId);
		}
		// 援助
		if (!HawkOSOperator.isEmptyString(assistantStr)) {
			march.setAssistantStr(assistantStr);
		}

		if (terminalPoint != null) {
			march.setTargetPointType(terminalPoint.getPointType());

			if (terminalPoint.getPointType() == WorldPointType.RESOURCE_VALUE) {
				march.setTargetPointField(String.valueOf(terminalPoint.getResourceId()));
			}
			
			// 需求：首次攻击2/3级野怪，行军时间特殊处理。(这里加个标记，方便行军时间计算)
			if (marchType == WorldMarchType.ATTACK_MONSTER_VALUE) {
				int currentKillLvl = player.getData().getMonsterEntity().getMaxLevel();
				int monsterId = Integer.parseInt(targetId);
				WorldEnemyCfg targetMonsterCfg = HawkConfigManager.getInstance().getConfigByKey(WorldEnemyCfg.class, monsterId);
				if (targetMonsterCfg.getLevel() > currentKillLvl
						&& WorldMarchConstProperty.getInstance().isMonsterLevelId(targetMonsterCfg.getLevel())) {
					march.setTargetPointField(String.valueOf(targetMonsterCfg.getLevel()));
				}
			}
			
			// 需求：前三次攻击新版野怪，行军时间特殊处理。(这里加个标记，方便行军时间计算)
			if (marchType == WorldMarchType.NEW_MONSTER_VALUE) {
				PlayerMonsterEntity monsterEntity = player.getData().getMonsterEntity();
				int attackNewMonsterTimes = monsterEntity.getAttackNewMonsterTimes();
				ArrayList<Integer> newMonsterSpecialTimeArr = WorldMapConstProperty.getInstance().getNewMonsterSpecialTimeArr();
				if (attackNewMonsterTimes < newMonsterSpecialTimeArr.size()) {
					march.setTargetPointField(String.valueOf(attackNewMonsterTimes));
				}
			}
		}
		
		// 使用额外侦查行军队列
		if (marchType == WorldMarchType.SPY_VALUE && !isExtraSypMarchOccupied(player) && isExtraSpyMarchOpen(player)) {
			march.setExtraSpyMarch(true);
		}

		// 组装
		IWorldMarch iWorldMarch = march.wrapUp();
		
		// marchId设置放在前面
		march.setMarchId(HawkOSOperator.randomUUID());

		// 队员行军携带玩家的队长playerId
		if (iWorldMarch.isMassJoinMarch()) {
			// 集结行军的leader处理
			IWorldMarch massMarch = getMarch(targetId);
			if (massMarch != null) {
				march.setLeaderPlayerId(massMarch.getPlayerId());
			}
		}

		// 国王战进攻
		if (marchType == WorldMarchType.PRESIDENT_SINGLE_VALUE
				|| marchType == WorldMarchType.PRESIDENT_MASS_VALUE
				|| marchType == WorldMarchType.PRESIDENT_ASSISTANCE_MASS_VALUE) {

			march.setLeaderPlayerId(player.getId());
		}

		// 行军时间
		long needTime = iWorldMarch.getMarchNeedTime();
		march.setEndTime(startTime + needTime);
		march.setMarchJourneyTime((int) needTime);

		// 集结等待时间
		if (waitTime > 0) {
			waitTime *= 1000;
			
			// 作用号618：集结所需时间减少 -> 实际集结时间 = 基础集结时间 * （1 - 作用值/10000）；向上取整；不得小于0
			waitTime *= 1 - player.getEffect().getEffVal(EffType.GUILD_MASS_TIME_REDUCE_PER, effParams) * GsConst.EFF_PER;
			waitTime = waitTime > 0 ? waitTime : 0;
			
			march.setMarchStatus(WorldMarchStatus.MARCH_STATUS_WAITING_VALUE);
			march.setMassReadyTime(march.getStartTime());
			march.setStartTime(march.getStartTime() + waitTime);
			march.setEndTime(march.getEndTime() + waitTime);
		}

		// 此处注意，活动的怪物行军是不需要入库的
		if (needStoreDB && !march.create(true)) {
			logger.error("gen march failed, marchData {} ", march);
			return null;
		}
		logger.info("gen march success, marchData: {} ", march);
		
		return iWorldMarch;
	}

	public List<Integer> getMarchDressGroup(PlayerData playerData, EffectParams effParams) {
		try {
			if(GameUtil.isNpcPlayer(playerData.getPlayerId())){
				return Collections.emptyList();
			}
			// 用户展示装扮
			Map<Integer, DressItem> showDresses = WorldPointService.getInstance().getShowDress(playerData.getPlayerBaseEntity().getPlayerId());
			List<Integer> dressList = effParams.getDressList();
			// dressList为空直接返回，在获取作用号时会默认使用身上的装扮
			if (dressList.isEmpty()) {
				return dressList;
			}
			// 玩家选定的基地装扮
			DressCfg baseDressCfg = HawkConfigManager.getInstance().getConfigByKey(DressCfg.class, dressList.get(0));
			List<DressCfg> newDressList = new ArrayList<>();
			for (Entry<Integer, DressItem> showDress : showDresses.entrySet()) {
				DressItem dressItem = showDress.getValue();
				DressCfg dressCfg = AssembleDataManager.getInstance().getDressCfg(dressItem.getDressType(), dressItem.getModelType());
				if (dressCfg.getDressType() == baseDressCfg.getDressType()) {
					newDressList.add(baseDressCfg);
					continue;
				}
				newDressList.add(dressCfg);
			}
			Optional<DressCfg> marchDressCfgOp = Optional.empty();
			// 自动激活套装
			if (effParams.isActivateDressGroup()) {
				ConfigIterator<DressGroupCfg> dressGroupIter = HawkConfigManager.getInstance().getConfigIterator(DressGroupCfg.class);
				while (dressGroupIter.hasNext()) {
					DressGroupCfg dressGroupCfg = dressGroupIter.next();
					if (dressGroupCfg.getDressIdList().contains(baseDressCfg.getDressId())) {
						DressCfg dressCfg = HawkConfigManager.getInstance().getConfigByKey(DressCfg.class, dressGroupCfg.getDressIdList().get(1));
						if (playerData.getDressEntity().hasDress(dressCfg.getDressType(), dressCfg.getModelType())) {
							marchDressCfgOp = Optional.of(dressCfg);
						}
						break;
					}
				}
			}

			List<Integer> rntDressList = new ArrayList<>();
			for (DressCfg dressCfg : newDressList) {
				if (marchDressCfgOp.isPresent() && marchDressCfgOp.get().getDressType() == dressCfg.getDressType()) {
					rntDressList.add(marchDressCfgOp.get().getDressId());
					continue;
				}
				rntDressList.add(dressCfg.getDressId());
			}

			return rntDressList;
		} catch (Exception e) {
			HawkException.catchException(e);
		}

		return Collections.emptyList();
	}


	/**
	 * 删除行军数据
	 * @param march
	 * @return
	 */
	public boolean removeMarch(IWorldMarch march) {
		if (march == null || march.getMarchEntity().isInvalid()) {
			return false;
		}
		
		Player player = GlobalData.getInstance().makesurePlayer(march.getPlayerId());
		march.remove(); // 缓存移除

		// 联盟战争界面移除
		rmGuildMarch(march.getMarchId());
		if (player != null) {
			player.getPush().pushMarchRemove(march.getMarchId()); // 个人推送
		}

		// 集结类型的队列删除时需要告诉队员
		if (march.isMassMarch()) {
			// 集结参与的行军
			Set<IWorldMarch> joinMarchs = getMassJoinMarchs(march, false);
			for (IWorldMarch tempMarch : joinMarchs) {
				if (tempMarch == null || tempMarch.getMarchEntity().isInvalid()) {
					continue;
				}
				Player tempPlayer = GlobalData.getInstance().getActivePlayer(tempMarch.getPlayerId());
				if (tempPlayer != null) {
					tempPlayer.getPush().pushMarchRemove(march.getMarchId());
				}
			}
		} else if (march.isMassJoinMarch()) {
			player.getPush().pushMassJoinMarchRemove(march.getMarchEntity().getTargetId());
		}
		
		// 通知行军事件
		notifyMarchEvent(MarchEvent.MARCH_DELETE_VALUE, march.getMarchEntity());
		// 从点信息中移除行军id信息
		updatePointMarchInfo(march, true);
		
		// 移除战斗异常行军消耗记录数据
		if (ConstProperty.getInstance().isOpenfightUnusualReturnDia()) {
			LocalRedis.getInstance().removeMarchSpeedConsume(march.getMarchId());
		}
		
		// 行为日志记录
		BehaviorLogger.log4Service(player, Source.MARCH, Action.REMOVE_MARCH, Params.valueOf("marchId", march.getMarchId()), Params.valueOf("marchType", march.getMarchType()),
				Params.valueOf("marchStatus", march.getMarchEntity().getMarchStatus()), Params.valueOf("targetId", march.getMarchEntity().getTargetId()),
				Params.valueOf("terminalId", march.getMarchEntity().getTerminalId()),
				Params.valueOf("callBackX", march.getMarchEntity().getCallbackX()), Params.valueOf("callBackY", march.getMarchEntity().getCallbackY()));
		
		logger.info("world march remove, marchData: {}", march);
		return true;
	}

	/**
	 * 发起行军
	 */
	public IWorldMarch startMarch(Player player, int marchType, int terminalId, String targetId, EffectParams effParams) {
		return startMarch(player, marchType, terminalId, targetId, null, 0, effParams);
	}
	
	/**
	 * 发起行军
	 */
	public IWorldMarch startMarch(Player player, int marchType, int terminalId, String targetId, String assistantStr, int waitTime, EffectParams effParams) {
		return startMarch(player, marchType, terminalId, targetId, assistantStr, waitTime, 0, 0, 0,effParams);
	}

	/**
	 * 发起行军
	 */
	public IWorldMarch startMarch(Player player, int marchType, int terminalId, String targetId, String assistantStr, int waitTime, int autoMarchIdentify, int autoResourceIdentify,int treasureCount,EffectParams effParams) {
		
		// TODO 机器人模式下，判断行军数量是否达到目标上限，达到后不再发起行军
		if (player.isInDungeonMap() || GsConfig.getInstance().isRobotMode() && marchs.size() >= GameConstCfg.getInstance().getMarchCountLimit() && player.getOpenId().startsWith("robot_puid")) {
			HawkLog.debugPrintln("start march failed, world march count overhead");
			return null;
		}
				
		// 起始点id
		int origionId = WorldPlayerService.getInstance().getPlayerPos(player.getId());

		// 目标点
		WorldPoint terminalPoint = WorldPointService.getInstance().getWorldPoint(terminalId);

		// 生成行军
		IWorldMarch march = genMarch(player, marchType, origionId, terminalId, targetId, terminalPoint, waitTime, assistantStr, true, effParams);
		
		march.getMarchEntity().setAutoMarchIdentify(autoMarchIdentify);
		march.getMarchEntity().setAutoResourceIdentify(autoResourceIdentify);
		march.getMarchEntity().setTreasureCount(treasureCount);
		// 攻击性行为的行军，破罩
		if (isOffensiveAction(player.getId(), WorldMarchType.valueOf(marchType), terminalPoint)) {
			march.getMarchEntity().setIsOffensive(1);
			player.removeCityShield();
			
			WarFeverCfg warFeverCfg = HawkConfigManager.getInstance().getConfigByKey(WarFeverCfg.class, player.getCityLevel());
			if (warFeverCfg != null) {
				player.getPlayerBaseEntity().setWarFeverEndTime(HawkTime.getMillisecond() + warFeverCfg.getWarFeverTime());
				player.getPush().syncPlayerInfo();
			}
			
			// 打破全服保护
			GlobalData.getInstance().addBrokenProtectPlayer(player.getId());
			LogUtil.logOffensiveMarch(player, marchType, march.getMarchEntity().getTargetPointType());
			player.getPush().syncPlayerInfo();
			
			WorldPlayerService.getInstance().updateWorldPointProtected(player.getId(), 0L);
			
			WorldPoint worldPoint = WorldPlayerService.getInstance().getPlayerWorldPoint(player.getId());
			if (worldPoint != null) {
				WorldPointService.getInstance().getWorldScene().update(worldPoint.getAoiObjId());
			}
		}
		
		List<PlayerHero> OpHero = player.getHeroByCfgId(march.getMarchEntity().getHeroIdList());
		for (PlayerHero hero : OpHero) {
			hero.goMarch(march);
		}
		
		Optional<SuperSoldier> sso = player.getSuperSoldierByCfgId(march.getMarchEntity().getSuperSoldierId());
		if (sso.isPresent()) {
			sso.get().goMarch(march);
		}
		
		// 行军上需要显示的作用号
		List<Integer> marchShowEffList = new ArrayList<>();
		int[] marchShowEffs = WorldMarchConstProperty.getInstance().getMarchShowEffArray();
		if (marchShowEffs != null) {
			for (int i = 0; i < marchShowEffs.length; i++) {
				int effVal = player.getEffect().getEffVal(EffType.valueOf(marchShowEffs[i]));
				if (effVal > 0) {
					marchShowEffList.add(marchShowEffs[i]);
				}
			}
		}
		
		if (!marchShowEffList.isEmpty()) {
			march.getMarchEntity().resetEffect(marchShowEffList);
		}
		
		// 把个人编队记录下来
		if (effParams.getWorldmarchReq() != null) {
			int formation = effParams.getWorldmarchReq().getFormation();
			if (formation != 0) {
				march.getMarchEntity().setFormation(formation);
			}
		}
		
		// 保存行军并推送给对应的玩家
		saveAndPushWorldMarch(player, march);

		// 目标警示提示
		int[] pos = GameUtil.splitXAndY(terminalId);
		notifyTowardsMarch(march.getMarchEntity(), terminalPoint, pos[0], pos[1]);
		
		// 加入行军警报
		march.onMarchStart();

		// 加入联盟箭塔攻击范围
		if (WorldUtil.canBeAttackedByBartizan(march.getMarchEntity())) {
			GuildManorService.getInstance().addToGuildBartizan(march);
		}
		
		if (march.getMarchEntity().isOffensive()) {
			GameUtil.notifyDressShow(player.getId());
		}

		return march;
	}
	
	/**
	 * 向自己内部发起的行军, 直接到达
	 * @param player
	 * @param marchType
	 * @param armys
	 * @param heroId
	 * @param waitTime
	 * @return
	 */
	public IWorldMarch startSelfMarch(Player player, int marchType, int waitTime, EffectParams params) {
		// 起终点都是玩家城点
		int origionId = WorldPlayerService.getInstance().getPlayerPos(player.getId());
		// 目标点
		WorldPoint terminalPoint = WorldPointService.getInstance().getWorldPoint(origionId);
		
		// 生成行军
		IWorldMarch march = genMarch(player, marchType, origionId, origionId, String.valueOf(origionId), terminalPoint, waitTime, null, true, params);
		
		// 保存行军并推送给对应的玩家
		saveAndPushWorldMarch(player, march);
		// 添加start
		march.onMarchStart();	
		return march;
	}
	
	/**
	 * 发起尤里来袭行军 
	 */
	public IWorldMarch startYuriStrikeMarch(Player player, YuristrikeCfg cfg){
		WorldPoint yuriStrikePoint = WorldPointService.getInstance().createYuriStrikePoint(player.getId());
		if (yuriStrikePoint == null) {
			return null;
		}
		int terminalId = WorldPlayerService.getInstance().getPlayerPos(player.getId());
		IWorldMarch march = startMonsterMarch(null, WorldMarchType.YURI_STRIKE_MARCH_VALUE, yuriStrikePoint.getId(), terminalId, player, player.getId(), cfg.getEnemyList(), 0, 0, true, true);
		HawkProtocol protocol = HawkProtocol.valueOf(HP.code.WORLD_MARCH_ADD_PUSH_VALUE, march.getMarchEntity().toBuilder(WorldMarchPB.newBuilder(), WorldMarchRelation.YURI_STRIKE_RELATION));
		player.sendProtocol(protocol);
		return march;
	}

	/**
	 * 发起母旗行军
	 */
	public IWorldMarch startCenterFlagMarch(String playerId, WarFlagSignUpItem signUpInfo, IFlag flag) {
		WorldPoint worldPoint = WorldPointService.getInstance().getWorldPoint(flag.getPointId());
		if (worldPoint == null) {
			return null;
		}
		
		int terminalId = WorldPlayerService.getInstance().getPlayerPos(playerId);
		// 玩家世界点不存在，直接发奖
		if (terminalId == 0) {
			WarFlagService.getInstance().sendCenterAwardMail(signUpInfo, flag.getPointId());
		} else {
			Player player = GlobalData.getInstance().makesurePlayer(playerId);
			IWorldMarch march = startMonsterMarch(flag.getOwnerId(), WorldMarchType.CENTER_FLAG_REWARD_MARCH_VALUE, flag.getPointId(), terminalId, player, player.getId(), new ArrayList<>(), 0, 0, true, true);
			List<WarFlagSignUpItem> award = new ArrayList<>();
			award.add(signUpInfo);
			march.getMarchEntity().setAwardStr(SerializeHelper.collectionToString(award, SerializeHelper.ELEMENT_DELIMITER, SerializeHelper.BETWEEN_ITEMS));
			
			HawkProtocol protocol = HawkProtocol.valueOf(HP.code.WORLD_MARCH_ADD_PUSH_VALUE, march.getMarchEntity().toBuilder(WorldMarchPB.newBuilder(), WorldUtil.getRelation(march.getMarchEntity(), player)));
			player.sendProtocol(protocol);
			return march;
		}
		return null;
	}
	
	/**
	 * 发起怪物行军
	 *  
	 * @return
	 */
	public IWorldMarch startMonsterMarch(String guildId, int marchType, int origionId, int terminalId, Player player, String targetId, List<ArmyInfo> armys, List<Integer> heroIds, boolean needStoreDB) {
		return startMonsterMarch(guildId, marchType, origionId, terminalId, player, targetId, armys, heroIds, 0, 0, false, needStoreDB);
	}
	
	/**
	 * 发起怪物行军
	 *  
	 * @return
	 */
	public IWorldMarch startMonsterMarch(String guildId, int marchType, int origionId, int terminalId, Player player, String targetId, List<ArmyInfo> armys, int round, long nextPushTime, boolean lastRound, boolean needStoreDB) {
		return startMonsterMarch(guildId, marchType, origionId, terminalId, player, targetId, armys, Collections.emptyList(), round, nextPushTime, lastRound, needStoreDB);
	}
	
	/**
	 * 发起行军
	 * 
	 * @param player		玩家
	 * @param marchType		行军类型
	 * @param terminalId	目标点
	 * @param targetId		目标id(资源id等)
	 * @param armys			部队
	 * @return IWorldMarch
	 */
	public IWorldMarch startMonsterMarch(String guildId, int marchType, int origionId, int terminalId, Player player, String targetId, List<ArmyInfo> armys, List<Integer> heroIds, int round, long nextPushTime, boolean lastRound, boolean needStoreDB) {
		// 目标点
		WorldPoint terminalPoint = WorldPointService.getInstance().getWorldPoint(terminalId);
		
		EffectParams effParams = new EffectParams();
		effParams.setArmys(armys);
		if (!heroIds.isEmpty()) {
			effParams.setHeroIds(heroIds);
		}
		
		// 生成行军
		IWorldMarch march = genMarch(null, marchType, origionId, terminalId, targetId, terminalPoint, 0, null, needStoreDB, effParams);
		
		// 保存行军
		march.register();
		if (marchType == WorldMarchType.YURI_MONSTER_VALUE) {
			// 如果是尤里复仇怪，需要存入玩家的guildId
			YuriRevengeMonsterMarch yuriRevengeMonsterMarch = (YuriRevengeMonsterMarch) march;
			yuriRevengeMonsterMarch.setGuildId(guildId);
			yuriRevengeMonsterMarch.setRound(round);
			//存入当前波次信息
			yuriRevengeMonsterMarch.setNextPushTime(nextPushTime);
			yuriRevengeMonsterMarch.setLastRound(lastRound);
		} else if (marchType == WorldMarchType.SPACE_MECHA_MONSTER_ATTACK_MARCH_VALUE) {
			WorldPoint point = WorldPointService.getInstance().getWorldPoint(origionId);
			int enemyId = point.getMonsterId();
			if (point.getPointType() == WorldPointType.SPACE_MECHA_STRONG_HOLD_VALUE) {
				StrongHoldWorldPoint strongholdPoint = (StrongHoldWorldPoint) point;
				enemyId = strongholdPoint.getEnemyId();
			}
			
			SpaceMechaEnemyCfg monsterCfg = HawkConfigManager.getInstance().getConfigByKey(SpaceMechaEnemyCfg.class, enemyId);
			march.getMarchEntity().setPlayerName(monsterCfg.getName());
			MechaSpaceInfo spaceObj = SpaceMechaService.getInstance().getGuildSpace(guildId);
			spaceObj.getMarchEnemyMap().put(march.getMarchId(), enemyId);
			spaceObj.addEnemyMarch(march);
		} else if(marchType == WorldMarchType.SPACE_MECHA_EMPTY_MARCH_VALUE) {
			WorldPoint point = WorldPointService.getInstance().getWorldPoint(origionId);
			SpaceMechaEnemyCfg monsterCfg = HawkConfigManager.getInstance().getConfigByKey(SpaceMechaEnemyCfg.class, point.getMonsterId());
			march.getMarchEntity().setPlayerName(monsterCfg.getName());
		}
		
		// 通知行军事件
		notifyMarchEvent(MarchEvent.MARCH_ADD_VALUE, march.getMarchEntity());
		// 加入行军警报
		march.onMarchStart();
		return march;
	}

	/**
	 * 通知行军事件
	 * 
	 * @param marchDeleteValue
	 * @param march
	 */
	public void notifyMarchEvent(int eventType, WorldMarch march) {
		int marchStatus = march.getMarchStatus();
		if (eventType == MarchEvent.MARCH_UPDATE_VALUE &&
				!(marchStatus == WorldMarchStatus.MARCH_STATUS_MARCH_VALUE 
				|| marchStatus == WorldMarchStatus.MARCH_STATUS_MARCH_REACH_VALUE
				|| marchStatus == WorldMarchStatus.MARCH_STATUS_RETURN_BACK_VALUE)) {
			return;
		}
		
		// 年兽的不同步
		if (GameConstCfg.getInstance().isNianOptimize() &&
				march.getMarchType() == WorldMarchType.NIAN_SINGLE_VALUE) {
			if (marchStatus == WorldMarchStatus.MARCH_STATUS_RETURN_BACK_VALUE) {
				return;
			}
		}
		
		NotifyMarchUpdateTask marchUpdateTask = new NotifyMarchUpdateTask(eventType, march);
		if(GsConfig.getInstance().isUseAsyncBroacastMarch()){
			HawkTaskManager.getInstance().postExtraTask(marchUpdateTask);
		} else {
			marchUpdateTask.run();
		}
	}

	/**
	 * 目标警示提示
	 * 当玩家向另外一名玩家正在行军的野怪、资源点、驻扎点行军时。另一名玩家会受到提示
	 * @param initMarch 发起方的行军
	 * @param worldPoint 目标点
	 */
	public void notifyTowardsMarch(WorldMarch initMarch, WorldPoint worldPoint, int posX, int posY) {
		// 只处理 野怪、资源点、驻扎点
		if (worldPoint != null
				&& worldPoint.getPointType() != WorldPointType.RESOURCE_VALUE
				&& worldPoint.getPointType() != WorldPointType.QUARTERED_VALUE
				&& worldPoint.getPointType() != WorldPointType.MONSTER_VALUE
				&& worldPoint.getPointType() != WorldPointType.STRONG_POINT_VALUE) {
			return;
		}
		
		// 新版野怪不提示
		if (initMarch.getMarchType() == WorldMarchType.NEW_MONSTER_VALUE) {
			return;
		}
		
		String initPlayerId = initMarch.getPlayerId();
		Player initPlayer = GlobalData.getInstance().makesurePlayer(initPlayerId);
		
		// 查找朝向目标点行军中状态的行军
		List<IWorldMarch> towardsMarch = new ArrayList<IWorldMarch>();
		Collection<IWorldMarch> worldPointMarchs = getWorldPointMarch(posX, posY);
		for (IWorldMarch iWorldMarch : worldPointMarchs) {
			if (iWorldMarch.isReturnBackMarch()) {
				continue;
			}
			if (!iWorldMarch.isMarchState()) {
				continue;
			}
			towardsMarch.add(iWorldMarch);
		}
		
		for (IWorldMarch iWorldMarch : towardsMarch) {
			String playerId = iWorldMarch.getMarchEntity().getPlayerId();
			Player player = GlobalData.getInstance().getActivePlayer(playerId);
			if (player == null) {
				continue;
			}
			TowardsMarchPush.Builder builder = TowardsMarchPush.newBuilder();
			builder.setPosX(posX);
			builder.setPosY(posY);
			builder.setInitPlayerName(initPlayer.getName());
			builder.addRelationType(WorldUtil.getRelation(initMarch, player));
			builder.setMarchType(iWorldMarch.getMarchType());
			player.sendProtocol(HawkProtocol.valueOf(HP.code.TOWARDS_MARCH_PUSH, builder));
		}
	}

	/**
	 * 获得已到达队长家的参与者行军列表
	 * 
	 * @param massMarch 参数是队长的行军
	 * 
	 * @return
	 */
	public Set<IWorldMarch> getMassJoinMarchs(IWorldMarch worldMarch, boolean needReach) {
		// 等待和到达的行军
		Set<IWorldMarch> retMarchs = new HashSet<IWorldMarch>();
		// 不是队长行军
		if (worldMarch.isMassMarch()) {
			MassMarch massMarch = (MassMarch) worldMarch;
			// 已到达队长家的行军
			BlockingQueue<IWorldMarch> passiveMarchs = getPlayerPassiveMarch(massMarch.getPlayerId());
			if (passiveMarchs == null || passiveMarchs.size() == 0) {
				return retMarchs;
			}
			for (IWorldMarch march : passiveMarchs) {
				if(march instanceof PlayerMarch){
					PassiveMarch passiveMarch = (PassiveMarch) march;
					// 类型不匹配
					if (passiveMarch == null || passiveMarch.getMarchEntity().isInvalid() || massMarch.getJoinMassType() != passiveMarch.getMarchType()) {
						continue;
					}
					// 返程加入行军不处理
					if (passiveMarch.getMarchEntity().getMarchStatus() == WorldMarchStatus.MARCH_STATUS_RETURN_BACK_VALUE) {
						continue;
					}
					if (needReach && passiveMarch.getMarchEntity().getMarchStatus() != WorldMarchStatus.MARCH_STATUS_JOIN_MARCH_VALUE
							&& passiveMarch.getMarchEntity().getMarchStatus() != WorldMarchStatus.MARCH_STATUS_WAITING_VALUE
							&& passiveMarch.getMarchEntity().getMarchStatus() != WorldMarchStatus.MARCH_STATUS_MARCH_REACH_VALUE
							&& passiveMarch.getMarchEntity().getMarchStatus() != WorldMarchStatus.MARCH_STATUS_MANOR_BREAK_VALUE) {
						continue;
					}
					if (massMarch.getMarchId().equals(passiveMarch.getTargetId())) {
						retMarchs.add(march);
					}
				}
			}
		}
		return retMarchs;
	}





	/**
	 * 广播集结队长的行军给队员
	 * 
	 * @param massMarch
	 */
	public void broadcastMassMarch2Team(IWorldMarch massMarch) {
		if (massMarch == null || massMarch.getMarchEntity().isInvalid()) {
			return;
		}
		Set<IWorldMarch> joinMarchs = getMassJoinMarchs(massMarch, false);
		if (joinMarchs == null) {
			return;
		}
		WorldMarchPB.Builder builder = massMarch.getMarchEntity().toBuilder(WorldMarchPB.newBuilder(), WorldMarchRelation.TEAM_LEADER);
		for (IWorldMarch tempMarch : joinMarchs) {
			if (tempMarch == null || tempMarch.getMarchEntity().isInvalid()) {
				continue;
			}
			// 同步集结行军的队长信息
			Player tempPlayer = GlobalData.getInstance().getActivePlayer(tempMarch.getPlayerId());
			if (tempPlayer != null) {
				HawkProtocol massProtocol = HawkProtocol.valueOf(HP.code.WORLD_MARCH_UPDATE_PUSH_VALUE, builder);
				tempPlayer.sendProtocol(massProtocol);
			}
		}
	}

	/**
	 * 正常行军返回（无需处理奖励、军队数据）
	 * @param march 行军对象
	 */
	public boolean onMarchReturn(IWorldMarch iWorldMarch, long returnStartTime, long marchTime) {
		WorldMarch march = iWorldMarch.getMarchEntity();
		if (march == null || march.isInvalid()) {
			return false;
		}
		
		// 从联盟战争中移除
		rmGuildMarch(iWorldMarch.getMarchId());
		
		// 没有兵立即返回
		if (march.getMarchType() != WorldMarchType.RANDOM_BOX_VALUE && march.getMarchType() != WorldMarchType.SPY_VALUE && march.getMarchType() != WorldMarchType.ASSISTANCE_RES_VALUE
				&& march.getMarchType() != WorldMarchType.WAREHOUSE_GET_VALUE && march.getMarchType() != WorldMarchType.WAREHOUSE_STORE_VALUE
				&& march.getMarchType() != WorldMarchType.TREASURE_HUNT_VALUE
				&& march.getMarchType() != WorldMarchType.OVERLORD_BLESSING_MARCH_VALUE
				&& march.getMarchType() != WorldMarchType.NIAN_BOX_MARCH_VALUE
				&& march.getMarchType() != WorldMarchType.ESPIONAGE_MARCH_VALUE
				&& march.getMarchType() != WorldMarchType.DRAGON_BOAT_MARCH_VALUE
				&& march.getMarchType() != WorldMarchType.AGENCY_MARCH_COASTER_VALUE
				&& march.getMarchType() != WorldMarchType.AGENCY_MARCH_RESCUR_VALUE
				&& march.getMarchType() != WorldMarchType.RESOURCE_SPREE_BOX_MARCH_VALUE
				&& (march.getArmys() == null || WorldUtil.getFreeArmyCnt(march.getArmys()) <= 0)) {
			onMarchReturnImmediately(iWorldMarch, march.getArmys());
			iWorldMarch.onMarchReturn();
			return false;
		}
		
		// 移除国王战驻军信息
		if (iWorldMarch.isPresidentMarch() && iWorldMarch.isReachAndStopMarch()) {
			removePresidentMarch(iWorldMarch);
		}
		
		// 移除国王战箭塔驻军信息
		if (iWorldMarch.isPresidentTowerMarch() && iWorldMarch.isReachAndStopMarch()) {
			removePresidentTowerMarch(iWorldMarch.getMarchEntity().getTerminalId(), iWorldMarch.getMarchId());
		}
				
		// 移除领地驻军信息
		if (WorldUtil.isManorMarch(march)) {
			removeManorMarch(march.getTerminalId(), march.getMarchId());
		}

		// 移除战旗驻军信息
		if (iWorldMarch.isWarFlagMarch()) {
			removeFlagMarch(march.getTargetId(), march.getMarchId());
		}
		
		// 移除超级武器驻军信息
		if (iWorldMarch.isSuperWeaponMarch() && iWorldMarch.isReachAndStopMarch()) {
			removeSuperWeaponMarch(iWorldMarch.getMarchEntity().getTerminalId(), iWorldMarch.getMarchId());
		}
		
		if (iWorldMarch.isXZQMarch() && iWorldMarch.isReachAndStopMarch()) {
			removeXZQMarch(iWorldMarch.getMarchEntity().getTerminalId(), iWorldMarch.getMarchId());
		}
		
		// 移除航海远征驻军信息
		if (iWorldMarch.isFortressMarch() && iWorldMarch.isReachAndStopMarch()) {
			removeFortressMarch(iWorldMarch.getMarchEntity().getTerminalId(), iWorldMarch.getMarchId());
		}
		
		GuildManorService.getInstance().rmFromGuildBartizan(iWorldMarch);

		// 先删除原有目标点的行军, (此处一定要注意，不能跟下方的update合并处理)
		updatePointMarchInfo(iWorldMarch, true);

		// 行军返回，修改行军信息, 翻转目标点
		march.changeOrigionTerminal();
		march.setStartTime(returnStartTime);
		if (marchTime == 0) {
			marchTime = iWorldMarch.getMarchNeedTime();
		}
		march.setEndTime(march.getStartTime() + marchTime);
		march.setMarchJourneyTime((int) marchTime);

		// 修改状态
		march.setMarchStatus(WorldMarchStatus.MARCH_STATUS_RETURN_BACK_VALUE);

		// 行军上需要显示的作用号
		List<Integer> marchShowEffList = new ArrayList<>();
		int[] marchShowEffs = WorldMarchConstProperty.getInstance().getMarchShowEffArray();
		if (marchShowEffs != null) {
			for (int i = 0; i < marchShowEffs.length; i++) {
				int effVal = iWorldMarch.getPlayer().getEffect().getEffVal(EffType.valueOf(marchShowEffs[i]));
				if (effVal > 0) {
					marchShowEffList.add(effVal);
				}
			}
		}

		if (!marchShowEffList.isEmpty()) {
			iWorldMarch.getMarchEntity().resetEffect(marchShowEffList);
		}
		
		// 在添加目标点返回的行军 (下方的update)
		updatePointMarchInfo(iWorldMarch, false);

		// 刷新出征
		iWorldMarch.updateMarch();
		
		iWorldMarch.onMarchReturn();
		
		// 日志记录
		logger.info("world march return, marchData: {}", march);
		return true;
	}

	/**
	 * 行军立即回城，然后移除行军信息
	 * 
	 * @param march
	 * @param leftList
	 * @return
	 */
	public boolean onMarchReturnImmediately(IWorldMarch march, List<ArmyInfo> leftList) {
		// 重置行军的兵力
		march.getMarchEntity().setArmys(leftList);
		
		if (march.isPresidentMarch() && march.isReachAndStopMarch()) {
			removePresidentMarch(march);
		}
		if (march.isPresidentTowerMarch() && march.isReachAndStopMarch()) {
			removePresidentTowerMarch(march.getMarchEntity().getTerminalId(), march.getMarchId());
		}
		if (march.isManorMarch()) {
			removeManorMarch(march.getMarchEntity().getTerminalId(), march.getMarchId());
		}
		if (march.isSuperWeaponMarch()) {
			removeSuperWeaponMarch(march.getMarchEntity().getTerminalId(), march.getMarchId());
		}
		if (march.isXZQMarch()) {
			removeXZQMarch(march.getMarchEntity().getTerminalId(), march.getMarchId());
		}
		if (march.isWarFlagMarch()) {
			removeFlagMarch(march.getMarchEntity().getTargetId(), march.getMarchId());
		}
		if (march.isFortressMarch()) {
			removeFortressMarch(march.getMarchEntity().getTerminalId(), march.getMarchId());
		}
		
		// 移除行军前的计算
		accountMarchBeforeRemove(march);

		logger.info("world march return immediately， marchData： {}", march);

		rmGuildMarch(march.getMarchId());
		// 真正移除行军
		return removeMarch(march);
	}

	/**
	 * 行军返回(不带奖励)，本方法使用行军的到达时间作为回程时间，适用场景：[不需要停留的行军；起服之后计算过程的行军]
	 * 
	 * @param march
	 *            行军对象
	 * @param selfArmys
	 *            本次行军要带回的军队，如无变化可传null
	 * @param backId
	 *            回程的起点
	 * @return
	 */
	public boolean onMarchReturn(IWorldMarch march, List<ArmyInfo> selfArmys, int backId) {
		return onMarchReturn(march, null, selfArmys, backId);
	}

	/**
	 * 行军返回，本方法使用行军的到达时间作为回程时间，适用场景：[不需要停留的行军；起服之后计算过程的行军]
	 * 
	 * @param march
	 *            行军对象
	 * @param awardItems
	 *            本次行军要带回的奖励
	 * @param selfArmys
	 *            本次行军要带回的军队，如无变化可传null
	 * @param backId
	 *            回程的起点
	 * @return
	 */
	public boolean onMarchReturn(IWorldMarch march, AwardItems awardItems, List<ArmyInfo> selfArmys, int backId) {
		if (march == null || march.getMarchEntity().isInvalid()) {
			return false;
		}

		// 添加返回行军的奖励
		if (awardItems != null) {
			march.getMarchEntity().setAwardItems(awardItems);
		}

		// 死亡部队立即结算
		if (getDeadArmyCnt(selfArmys) > 0) {
			selfArmys = calcDeadArmy(march.getPlayerId(), selfArmys);
		}

		march.getMarchEntity().setArmys(selfArmys);

		logger.info("world march callback return, marchData: {}", march);

		if (march.getMarchType() == WorldMarchType.ASSISTANCE_RES) {
			// 行军返回
			onMarchReturn(march, HawkTime.getMillisecond(), 0);
			return true;
		}

		// 没有兵力是直接移除行军
		if (march.getMarchType() != WorldMarchType.SPY && (march.getMarchEntity().getArmys() == null || WorldUtil.getFreeArmyCnt(selfArmys) <= 0)) {
			// 移除行军前的结算
			accountMarchBeforeRemove(march);
			// 行军移除
			removeMarch(march);
		} else {
			if (backId > 0) {
				march.getMarchEntity().setCallBackTime(HawkTime.getMillisecond());
				int[] xy = GameUtil.splitXAndY(backId);
				march.getMarchEntity().setCallBackX(xy[0]);
				march.getMarchEntity().setCallBackY(xy[1]);
			}
			// 行军返回
			onMarchReturn(march, HawkTime.getMillisecond(), 0);
		}
		
		// 计算掠夺量
		if (march.isGrabResMarch() && Objects.nonNull(awardItems)) {
			for (ItemInfo item : awardItems.getAwardItems()) {
				int typeWeight = WorldMarchConstProperty.getInstance().getResWeightByType(item.getItemId());
				int grebRes = (int)item.getCount() * typeWeight;
				LocalRedis.getInstance().incrementGrabResWeightDayCount(march.getPlayerId(),item.getItemId(), grebRes);
			}
		}
		return true;
	}

	/**
	 * 行军返回，召回一类的主动调回行军，需要确认返回的时间，适用场景：[需要当场确认回程时间的行军]
	 * 
	 * @param march
	 *            行军对象
	 * @param returnTime
	 *            召回时间
	 * @param awardItems
	 *            本次行军要带回的奖励
	 * @param selfArmys
	 *            本次行军要带回的军队，如无变化可传null
	 * @param backId
	 *            回程的起点
	 * @return
	 */
	public boolean onMarchReturn(IWorldMarch march, long returnTime, AwardItems awardItems, List<ArmyInfo> selfArmys, double backX, double backY) {
		if (march == null || march.getMarchEntity().isInvalid()) {
			return false;
		}

		// 死亡部队立即结算
		if (getDeadArmyCnt(selfArmys) > 0) {
			selfArmys = calcDeadArmy(march.getPlayerId(), selfArmys);
		}

		march.getMarchEntity().setAwardItems(awardItems);
		march.getMarchEntity().setArmys(selfArmys);

		logger.info("world march return with award and armys, marchData: {}", march);

		// 没有兵的特殊处理
		boolean armyEmpty = (march.getMarchEntity().getArmys() == null || WorldUtil.getFreeArmyCnt(selfArmys) <= 0);
		if (armyEmpty) {
			if (march.getMarchType() != WorldMarchType.RANDOM_BOX && march.getMarchType() != WorldMarchType.SPY && march.getMarchType() != WorldMarchType.ASSISTANCE_RES
					&& march.getMarchType() != WorldMarchType.WAREHOUSE_GET && march.getMarchType() != WorldMarchType.WAREHOUSE_STORE
						&& march.getMarchType() != WorldMarchType.DRAGON_BOAT_MARCH && march.getMarchType() != WorldMarchType.RESOURCE_SPREE_BOX_MARCH
						&& march.getMarchType() != WorldMarchType.OVERLORD_BLESSING_MARCH) {
				// 移除行军前的结算
				accountMarchBeforeRemove(march);
				// 行军移除
				removeMarch(march);
				return true;
			}
		}

		long marchTime = 0;
		if (backX > 0 && backY > 0) {
			march.getMarchEntity().setCallBackTime(returnTime);
			march.getMarchEntity().setCallBackX(backX);
			march.getMarchEntity().setCallBackY(backY);
			marchTime = returnTime - march.getMarchEntity().getStartTime();
		}
		// 行军返回
		onMarchReturn(march, returnTime, marchTime);
		return true;
	}

	/**
	 * 目标点改变，无行为返回
	 * 
	 * @param march
	 * @return
	 */
	public boolean onPlayerNoneAction(IWorldMarch march, long returnStartTime) {
		if (march == null || march.getMarchEntity().isInvalid()) {
			return false;
		}
		logger.info("march none action return, marchData {}", march);
		onMarchReturn(march, returnStartTime, 0);

		return true;
	}

	/**
	 * 打怪行军非战斗结束回城时，返还体力
	 * 
	 * @param march
	 */
	public void onMonsterRelatedMarchAction(IWorldMarch march) {
		int marchType = march.getMarchType().getNumber();
		if (marchType == WorldMarchType.MONSTER_MASS_JOIN_VALUE || marchType == WorldMarchType.MONSTER_MASS_VALUE || marchType == WorldMarchType.ATTACK_MONSTER_VALUE
				|| marchType == WorldMarchType.TREASURE_HUNT_MONSTER_MASS_VALUE || marchType == WorldMarchType.TREASURE_HUNT_MONSTER_MASS_JOIN_VALUE 
				|| marchType == WorldMarchType.AGENCY_MARCH_MONSTER_VALUE
				|| marchType == WorldMarchType.AGENCY_MARCH_RESCUR_VALUE
				|| marchType == WorldMarchType.AGENCY_MARCH_COASTER_VALUE) {
			// 对于参与集结的行军，返回时要返还体力
			Player player = GlobalData.getInstance().makesurePlayer(march.getPlayerId());
			player.dealMsg(MsgId.RETURN_VIT, new MarchVitReturnBackMsgInvoker(player, march));
		}
	}

	/**
	 * 删除国王战行军
	 * 
	 * @param march
	 */
	public void removePresidentMarch(IWorldMarch rmMarch) {
		BlockingDeque<String> presidentMarchs = getPresidentMarchs();
		if (!presidentMarchs.contains(rmMarch.getMarchId())) {
			return;
		}
		
		boolean isLeaderMarch = rmMarch.getMarchId().equals(getPresidentLeaderMarch());
		
		// 删除行军，然后重设队长
		rmPresidentMarch(rmMarch.getMarchId());

		if (!hasPresidentMarch()) {
			// 国王战广播驻军变更
			PresidentFightService.getInstance().getPresidentCity().broadcastPresidentInfo(null);
			return;
		}
		
		if (isLeaderMarch) {
			// 重设队长
			String newLeaderMarchId = getPresidentLeaderMarch();
			IWorldMarch leaderMarch = getMarch(newLeaderMarchId);
			Player newLeader = GlobalData.getInstance().makesurePlayer(leaderMarch.getPlayerId());

			for (String marchId : getPresidentMarchs()) {
				IWorldMarch march = getMarch(marchId);
				march.getMarchEntity().setLeaderPlayerId(newLeader.getId());
			}
			// 国王战广播驻军变更
			PresidentFightService.getInstance().getPresidentCity().broadcastPresidentInfo(null);
		}
	}

	/**
	 * 获取首都上驻扎的行军
	 * 
	 * @return
	 */
	public List<IWorldMarch> getPresidentQuarteredMarchs() {
		// 驻扎的行军表
		List<IWorldMarch> quarteredMarchList = new ArrayList<IWorldMarch>();
		Iterator<String> presidentMarchs = getPresidentMarchs().iterator();
		while (presidentMarchs.hasNext()) {
			String presidentMarchId = presidentMarchs.next();
			IWorldMarch march = getMarch(presidentMarchId);
			if (march != null) {
				quarteredMarchList.add(march);
			}
		}
		return quarteredMarchList;
	}

	/**
	 * 登录时推送首都的所有驻军
	 * 
	 * @param pushType
	 * @param guildId
	 * @param manorId
	 * @param pointId
	 * @return
	 */
	public HawkProtocol getQuarteredMarchAllProtocol(PushQuarteredMarchType pushType, String guildId) {
		PushAllQuarteredMarch.Builder allBuilder = PushAllQuarteredMarch.newBuilder();
		allBuilder.setPushType(pushType);
		if (!HawkOSOperator.isEmptyString(guildId)) {
			allBuilder.setGuildId(guildId);
		} else {
			allBuilder.setGuildId("");
		}
		return HawkProtocol.valueOf(HP.code.PUSH_ALL_QUARTERED_MARCHS, allBuilder);
	}

	/**
	 * 重选首都驻军队长
	 */
	public boolean resetPresidentQuarteredMarchs() {
		if (!hasPresidentMarch()) {
			return false;
		}

		List<IWorldMarch> allMarchs = new ArrayList<IWorldMarch>();
		Iterator<String> presidentMarchs = getPresidentMarchs().iterator();
		while (presidentMarchs.hasNext()) {
			String presidentMarchId = presidentMarchs.next();
			IWorldMarch march = getMarch(presidentMarchId);
			if (march == null || march.getMarchEntity().isInvalid()) {
				continue;
			}
			if (march.getPlayerId().equals(march.getMarchEntity().getLeaderPlayerId())) {
				return false;
			}
			allMarchs.add(march);
		}
		// 没有行军
		if (allMarchs.size() <= 0) {
			return false;
		}
		Collections.sort(allMarchs);

		// 设置队长
		IWorldMarch leaderMarch = allMarchs.get(0);
		leaderMarch.getMarchEntity().setLeaderPlayerId(leaderMarch.getPlayerId());

		// 设置队长行军
		Player leader = GlobalData.getInstance().makesurePlayer(leaderMarch.getPlayerId());

		int maxMassSoldierNum = leaderMarch.getMaxMassJoinSoldierNum(leader);
		reCalculateQuarteredMarchs(leader.getMaxMarchNum() + leaderMarch.getMarchEntity().getBuyItemTimes(), maxMassSoldierNum, leader.getId(), allMarchs);

		// 重推驻军所有信息
		Player player = GlobalData.getInstance().makesurePlayer(leaderMarch.getPlayerId());
		HawkProtocol protocol = getQuarteredMarchAllProtocol(PushQuarteredMarchType.PUSH_QUARTERED_PRESIDENT, player.getGuildId());
		GuildService.getInstance().broadcastProtocol(player.getGuildId(), protocol);

		return true;
	}

	/**
	 * 对要删除的行军提前结算
	 */
	public void accountMarchBeforeRemove(IWorldMarch worldMarch) {
		logger.info("world march account before remove, marchData: {}", worldMarch);

		final Player player = GlobalData.getInstance().makesurePlayer(worldMarch.getPlayerId());
		if (player == null) {
			return;
		}
		// 投递回玩家线程执行
		player.dealMsg(MsgId.MARCH_BEFORE_REMOVE, new MarchBeforeRemoveMsgInvoker(player, worldMarch));
		// 从箭塔移除行军
		GuildManorService.getInstance().rmFromGuildBartizan(worldMarch);
	}

	/**
	 * 获取死亡数量
	 * 
	 * @param armyList
	 * @return
	 */
	private int getDeadArmyCnt(List<ArmyInfo> armyList) {
		int cnt = 0;
		if (armyList != null) {
			for (ArmyInfo info : armyList) {
				cnt += info.getDeadCount();
			}
		}
		return cnt;
	}

	/**
	 * 行军中死亡部队处理
	 * 
	 * @param march
	 * @param selfArmys
	 */
	private List<ArmyInfo> calcDeadArmy(String playerId, List<ArmyInfo> selfArmys) {
		final List<ArmyInfo> armyDeadList = new ArrayList<>();
		List<ArmyInfo> armyLeftList = new ArrayList<>();
		for (ArmyInfo armyInfo : selfArmys) {
			ArmyInfo leftArmy = armyInfo.getCopy();
			if (armyInfo.getDeadCount() > 0) {
				armyDeadList.add(armyInfo.getCopy());
				leftArmy.setTotalCount(leftArmy.getTotalCount() - leftArmy.getDeadCount());
				leftArmy.setDeadCount(0);
			}
			armyLeftList.add(leftArmy);
		}
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		if (player != null) {
			HawkLog.logPrintln("army dead info: {}, playerId: {}", selfArmys, playerId);
			armyDeadList.stream().forEach(e -> e.setDirectDeadCount(e.getDeadCount()));
			HawkApp.getInstance().postMsg(player, CalcDeadArmy.valueOf(armyDeadList));
		}
		return armyLeftList;
	}

	/**
	 * 获得对应资源负重
	 * 
	 * @param list
	 * @param resType
	 * @return
	 */
	public long getArmyCarryResNum(Player player, List<ArmyInfo> list, int resType, EffectParams effParams) {
		double num = 0;
		
		double eff352Val = GsConst.EFF_PER * player.getEffect().getEffVal(EffType.RES_TROOP_WEIGHT, effParams);
		double eff374Val = GsConst.EFF_PER * player.getEffect().getEffVal(EffType.SODLIER_8_RES_TROOP_WEIGHT, effParams);
		
		// 总负重
		for (ArmyInfo armyInfo : list) {
			BattleSoldierCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, armyInfo.getArmyId());
			double addNum = cfg.getLoad() * armyInfo.getFreeCnt();
			
			if (armyInfo.getType() == SoldierType.CANNON_SOLDIER_8) {
				addNum *= 1 + eff374Val + eff352Val;
			} else {
				addNum *= 1  + eff352Val;
			}
			
			num += addNum;
		}
		
		// 资源权重
		int weight = WorldMarchConstProperty.getInstance().getResWeightByType(resType);
		return (long)(num / weight);
	}

	/**
	 * 设置最终的已采集资源
	 * 
	 * @param march
	 * @param resType
	 * @param value
	 */
	public void resetCollectResource(WorldMarch march, long value, int resType) {
		AwardItems items = march.getAwardItems();
		List<ItemInfo> itemInfos = items.getAwardItems();
		if (itemInfos.size() <= 0) {
			return;
		}

		for (ItemInfo itemInfo : itemInfos) {
			if (itemInfo == null) {
				continue;
			}
			
			if(itemInfo.getType() == ItemType.PLAYER_ATTR_VALUE * GsConst.ITEM_TYPE_BASE && itemInfo.getItemId() == resType){
				itemInfo.setCount(value);
			}
		}
		march.saveAwardItems();
	}

	/**
	 * 获取行军采集的资源数量
	 * 
	 * @param march
	 * @return
	 */
	public int getMarchCollectResource(WorldMarch march) {
		AwardItems items = march.getAwardItems();

		List<ItemInfo> itemInfos = items.getAwardItems();
		if (itemInfos.size() <= 0) {
			return 0;
		}

		ItemInfo itemInfo = itemInfos.get(0);
		if (itemInfo == null) {
			return 0;
		}

		return (int)itemInfo.getCount();
	}

	/**
	 * 采集一定量某种资源需要的时间
	 * 
	 * @param player
	 * @param resType
	 * @param resourceNum
	 * @return
	 */
	public long getTimeByResource(double resourceNum, double speed) {
		// 最终结算时间
		return (long) (Math.ceil(resourceNum * 1000) / speed);
	}

	/**
	 * 采集道具速度变化通知
	 * @param player
	 * @param endTime
	 * @param resType
	 */
	public void changeResourceSpeed(Player player, int resType) {
		changeCollectSpeed(player, resType);
		changeSuperMineSpeed(player, resType);
	}
	
	/**
	 * 更改普通矿采集速度
	 * @param player
	 * @param endTime
	 * @param resType
	 */
	private void changeCollectSpeed(Player player, int resType) {
		Set<IWorldMarch> collectMarchs = getPlayerTypeMarchs(player.getId(), WorldMarchType.COLLECT_RESOURCE_VALUE);

		for (IWorldMarch march : collectMarchs) {

			// 不是采集中
			if (march.getMarchEntity().getMarchStatus() != WorldMarchStatus.MARCH_STATUS_MARCH_COLLECT_VALUE) {
				continue;
			}

			// 资源类型判断
			WorldResourceCfg cfg = HawkConfigManager.getInstance().getConfigByKey(WorldResourceCfg.class, Integer.parseInt(march.getMarchEntity().getTargetId()));
			if (resType > 0 && cfg != null && resType != cfg.getResType()) {
				continue;
			}

			CollectWorldResMarch collectWorldResMarch = (CollectWorldResMarch) march;
			boolean isbackHome = collectWorldResMarch.doCollectRes(true);

			// 采集完成回家
			if (isbackHome) {
				continue;
			}

			// 更新行军状态
			march.updateMarch();

			int terminalId = march.getMarchEntity().getTerminalId();
			WorldPoint worldPoint = WorldPointService.getInstance().getWorldPoint(terminalId);
			if (worldPoint != null) {
				WorldPointService.getInstance().getWorldScene().update(worldPoint.getAoiObjId());
			}

			logger.info("world march change collect resource speed, marchData: {}", march);
		}
	}
	
	/**
	 * 更改超级矿采集速度
	 * @param player
	 * @param resType
	 */
	private void changeSuperMineSpeed(Player player, int resType) {
		Set<IWorldMarch> collectMarchs = getPlayerTypeMarchs(player.getId(), WorldMarchType.MANOR_COLLECT_VALUE);
		
		for (IWorldMarch march : collectMarchs) {
			
			// 不是采集中
			if (march.getMarchEntity().getMarchStatus() != WorldMarchStatus.MARCH_STATUS_MARCH_COLLECT_VALUE) {
				continue;
			}
			
			WorldPoint worldPoint = WorldPointService.getInstance().getWorldPoint(march.getMarchEntity().getTerminalId());
			if (worldPoint == null) {
				continue;
			}
			
			AbstractBuildable buildable = GuildManorService.getInstance().getBuildable(worldPoint);
			if (buildable == null || buildable.getBuildType() != Const.TerritoryType.GUILD_MINE) {
				continue;
			}
			
			GuildManorSuperMine mine = (GuildManorSuperMine) buildable;
			if (resType > 0 && resType != mine.getResType()) {
				continue;
			}
			
			// 采集速度
			double speed = WorldUtil.getCollectSpeed(march.getPlayer(), mine.getResType(), 0, worldPoint, march.getMarchEntity().getEffectParams());
			if(speed == 0){
				speed = 1;
			}
			march.getMarchEntity().setCollectSpeed(speed);
			march.getMarchEntity().setCollectBaseSpeed(WorldUtil.getCollectBaseSpeed(march.getPlayer(), mine.getResType(), march.getMarchEntity().getEffectParams()));
		}
	}
	
	/**
	 * 重新计算驻军信息，退出多余的行军和士兵
	 * 
	 * @param maxMarchNum
	 * @param remainSpace
	 *            人口上限
	 * @param leaderPlayerId
	 * @param tempMarchs
	 */
	private void reCalculateQuarteredMarchs(int maxMarchNum, int remainSpace, String leaderPlayerId, List<IWorldMarch> tempMarchs) {
		// 退回装不下的行军条目
		if (maxMarchNum < tempMarchs.size()) {
			// 超出数量的行军执行返回
			for (int i = maxMarchNum; i < tempMarchs.size(); i++) {
				try {
					IWorldMarch worldMarch = tempMarchs.get(i);
					onMarchReturn(worldMarch, GsApp.getInstance().getCurrentTime(), 0);
				} catch (Exception e) {
					HawkException.catchException(e);
				}
			}

			// 清理超出数量的行军列表对象
			tempMarchs.subList(maxMarchNum, tempMarchs.size()).clear();
		}

		// 设置其它队员的行军队长ID
		for (int i = 0; i < tempMarchs.size(); i++) {

			IWorldMarch tempMarch = tempMarchs.get(i);

			// 没有空位
			if (remainSpace <= 0) {
				onMarchReturn(tempMarch, GsApp.getInstance().getCurrentTime(), 0);
				continue;
			}

			// 计算留下来的士兵人口
			int nowPoputationCnt = WorldUtil.calcSoldierCnt(tempMarch.getMarchEntity().getArmys());
			if (nowPoputationCnt <= remainSpace) {
				tempMarch.getMarchEntity().setLeaderPlayerId(leaderPlayerId);
				remainSpace -= nowPoputationCnt;
			} else {
				List<ArmyInfo> stayList = new ArrayList<ArmyInfo>();
				List<ArmyInfo> backList = new ArrayList<ArmyInfo>();
				WorldUtil.splitArmyList(tempMarch.getMarchEntity().getArmys(), remainSpace, backList, stayList);
				
				// 若有返回的士兵和英雄,生成新行军
				if (backList.size() > 0) {
					Player player = GlobalData.getInstance().makesurePlayer(tempMarch.getPlayerId());
					onNewMarchReturn(player, tempMarch, backList, new ArrayList<>(), 0, null);
				}
				// 修改驻军情况
				resetMarchArmys(tempMarch, stayList);
			}
		}
	}

	/**
	 * 援助部分兵力带部分资源或者士兵回家
	 * 
	 * @param palyer
	 *            玩家，也是本次行军终点
	 * @param oldMarch
	 *            原来的行军信息
	 * @param backArmys
	 *            带回的兵力信息
	 */
	public void onNewMarchReturn(Player player, IWorldMarch oldMarch, List<ArmyInfo> backArmys, List<Integer> heros,int superSoldierId, ArmourSuitType armourSuit) {
		if (player == null) {
			player = GlobalData.getInstance().makesurePlayer(oldMarch.getPlayerId());
		}

		EffectParams params = new EffectParams();
		params.setArmys(backArmys);
		params.setHeroIds(heros);
		params.setSuperSoliderId(superSoldierId);
		params.setArmourSuit(armourSuit);
		params.setMechacoreSuit(null);
		
		IWorldMarch march = genMarch(player, oldMarch.getMarchType().getNumber(), oldMarch.getMarchEntity().getTerminalId(), oldMarch.getMarchEntity().getOrigionId(),
				oldMarch.getMarchEntity().getTargetId(), null, 0, null, true, params);
		
		march.getMarchEntity().setTargetPointType(WorldPointType.PLAYER_VALUE);
		march.getMarchEntity().setMarchStatus(WorldMarchStatus.MARCH_STATUS_RETURN_BACK_VALUE);
		saveAndPushWorldMarch(player, march);
		logger.info("world march split new return, oldMarch: {}, newMarch: {}", oldMarch, march);
	}

	/**
	 * 存储到缓存列表,并推送给客户端
	 * 
	 * @param player
	 * @param march
	 */
	protected void saveAndPushWorldMarch(Player player, IWorldMarch march) {
		march.register();

		HawkProtocol protocol = HawkProtocol.valueOf(HP.code.WORLD_MARCH_ADD_PUSH_VALUE, march.getMarchEntity().toBuilder(WorldMarchPB.newBuilder(), WorldMarchRelation.SELF));
		player.sendProtocol(protocol);

		if (march.isMassJoinMarch()) {
			IWorldMarch massMarch = getMarch(march.getMarchEntity().getTargetId()); // 获取集结的队长行军
			if (massMarch != null) {
				HawkProtocol massProtocol = HawkProtocol.valueOf(HP.code.WORLD_MARCH_ADD_PUSH_VALUE, massMarch.getMarchEntity().toBuilder(WorldMarchPB.newBuilder(), WorldMarchRelation.TEAM_LEADER));
				player.sendProtocol(massProtocol);
			}
		}
		notifyMarchEvent(MarchEvent.MARCH_ADD_VALUE, march.getMarchEntity()); // 通知行军事件
	}

	/**
	 * 重置某个行军的兵力信息
	 * 
	 * @param march
	 * @param remainArmys
	 * @return
	 */
	public boolean resetMarchArmys(IWorldMarch march, List<ArmyInfo> remainArmys) {
		march.getMarchEntity().setArmys(remainArmys);
		// 更新行军状态
		march.updateMarch();
		logger.info("world march reset army, remainArmys: {}, marchData: {}", remainArmys, march);
		return true;
	}

	/**
	 * 检查玩家是否因行军不能退出（踢出）联盟
	 * 
	 * @param playerId 目标玩家
	 * @return true 表示可退可踢
	 */
	public boolean checkCanQuitGuild(String playerId) {
		BlockingQueue<IWorldMarch> marchs = getPlayerMarch(playerId);
		for (IWorldMarch march : marchs) {
			// 联盟超级矿的特殊处理
			if (march.getMarchType() == WorldMarchType.MANOR_COLLECT) {
				continue;
			}
			// 返回途中的不受影响
			if (march.getMarchEntity().getMarchStatus() == WorldMarchStatus.MARCH_STATUS_RETURN_BACK_VALUE) {
				continue;
			}
			// 有国王战行军，不能退盟
			if (march.isPresidentMarch() || march.isPresidentTowerMarch() || march.isSuperWeaponMarch() || march.isWarFlagMarch() || march.isFortressMarch()) {
				return false;
			}
			// 有集结中/加入集结的行军 不能退出联盟
			if (march.isMassMarch() || march.isMassJoinMarch()) {
				if (march.isReachAndStopMarch()) {
					continue;
				}
				return false;
			}
		}
		return true;
	}

	/**
	 * 获取援军兵力人口总数
	 * 
	 * @param player
	 * @return
	 */
	public int getAssistArmySoldierTotal(Player player) {
		BlockingQueue<IWorldMarch> marchList = getPlayerPassiveMarch(player.getId());
		if (marchList == null) {
			return 0;
		}
		List<WorldMarch> list = new ArrayList<WorldMarch>();
		for (IWorldMarch iWorldMarch : marchList) {
			if (iWorldMarch.getMarchType() == WorldMarchType.ASSISTANCE && iWorldMarch.getMarchEntity().getMarchStatus() == WorldMarchStatus.MARCH_STATUS_MARCH_ASSIST_VALUE) {
				list.add(iWorldMarch.getMarchEntity());
			}
		}
		int total = WorldUtil.getMarchArmyTotal(list.toArray(new WorldMarch[0]));
		return total;
	}

	/**
	 * 判断是否为行军的成员(集结)
	 * 
	 * @return
	 */
	public boolean isTeamMember(Player player, IWorldMarch march) {
		if (march.isMassMarch()) {
			MassMarch massMarch = (MassMarch) march;
			int marchType = massMarch.getJoinMassType().getNumber();
			// 队长的被动行军列表
			BlockingQueue<IWorldMarch> marchs = getPlayerPassiveMarch(march.getPlayerId());
			if (marchs == null || marchs.size() == 0) {
				return false;
			}
			// 有队员，判断是否有该march队员
			for (IWorldMarch passiveMarch : marchs) {
				if (passiveMarch.getMarchType().getNumber() != marchType) {
					continue;
				}
				if (passiveMarch.getPlayerId().equals(player.getId())) {
					return true;
				}
			}
		} else {
			// 找出所在队的所有行军
			IWorldMarch massMarch = getMarch(march.getMarchEntity().getTargetId());
			if (massMarch == null || massMarch.getMarchEntity().isInvalid()) {
				return false;
			}
			if (massMarch.getPlayerId().equals(player.getId())) {
				return true;
			}
			Set<IWorldMarch> marchs = getMassJoinMarchs(massMarch, false);
			if (marchs == null || marchs.size() == 0) {
				return false;
			}

			// 判断有没有一个人是我
			for (IWorldMarch tempMarch : marchs) {
				if (tempMarch.getPlayerId().equals(player.getId())) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * 行军召回
	 * 
	 * @param march
	 * @param pixelX
	 * @param pixelY
	 * @param callBackX
	 * @param callBackY
	 * @return
	 */
	public boolean onMarchCallBack(IWorldMarch march) {
		if (march == null || march.getMarchEntity().isInvalid()) {
			return false;
		}

		logger.info("world march callback, marchData: {}", march);

		if (march.isReachAndStopMarch()) {
			if (march.isPresidentMarch()) { 
				removePresidentMarch(march);
			}
			if (march.isPresidentTowerMarch()) {
				removePresidentTowerMarch(march.getMarchEntity().getTerminalId(), march.getMarchId());
			}
			if (march.isManorMarch()) {
				removeManorMarch(march.getMarchEntity().getTerminalId(), march.getMarchId());
			}
			if (march.isSuperWeaponMarch()) {
				removeSuperWeaponMarch(march.getMarchEntity().getTerminalId(), march.getMarchId());
			}
			if (march.isXZQMarch()) {
				removeXZQMarch(march.getMarchEntity().getTerminalId(), march.getMarchId());
			}
			if (march.isWarFlagMarch()) {
				removeFlagMarch(march.getMarchEntity().getTargetId(), march.getMarchId());
			}
			if (march.isFortressMarch()) {
				removeFortressMarch(march.getMarchEntity().getTerminalId(), march.getMarchId());
			}
			if (march.isGuildSpaceMarch()) {
				WorldPoint worldPoint = WorldPointService.getInstance().getWorldPoint(march.getTerminalId());
				if (worldPoint != null && worldPoint instanceof SpaceWorldPoint) {
					SpaceWorldPoint spacePoint = (SpaceWorldPoint) worldPoint;
					spacePoint.removeDefMarch(march);
				}
			}
		}

		updatePointMarchInfo(march, true);

		onMarchReturn(march, HawkTime.getMillisecond(), 0);
		
		// 更新
		march.updateMarch();

		return true;
	}

	/**
	 * 采集资源召回，或者被打败回家
	 * 
	 * @param march
	 */
	public void onResourceMarchCallBack(IWorldMarch worldMarch, long currentTime, List<ArmyInfo> leftArmyList, long marchTime) {
		if (worldMarch instanceof CollectWorldResMarch) {
			CollectWorldResMarch march = (CollectWorldResMarch) worldMarch;
			// 设置剩余兵力
			march.getMarchEntity().setArmys(leftArmyList);

			// 采集目标点
			WorldPoint worldPoint = WorldPointService.getInstance().getWorldPoint(march.getMarchEntity().getTerminalId());
			if (worldPoint == null || worldPoint.getMarchId() == null || !worldPoint.getMarchId().equals(march.getMarchId())) {
				return;
			}
			
			// 移除向资源点行军的联盟战争界面信息
			String guildId = GuildService.getInstance().getPlayerGuildId(worldPoint.getPlayerId());
			Collection<IWorldMarch> guildMarchs = getGuildMarchs(guildId);
			for (IWorldMarch guildMarch : guildMarchs) {
				if (guildMarch.getMarchEntity().getTerminalId() == worldPoint.getId()) {
					rmGuildMarch(guildMarch.getMarchId());
					continue;
				}
			}
			
			if (!march.doCollectRes(false)) {
				// 回城
				onMarchReturn(march, currentTime, marchTime);
				// 通知资源采集
				if (worldPoint != null) {
					WorldResourceService.getInstance().notifyResourceGather(worldPoint, getMarchCollectResource(march.getMarchEntity()));
				}
				
				// tlog日志统计
				try {
					WorldResourceCfg cfg = HawkConfigManager.getInstance().getConfigByKey(WorldResourceCfg.class, Integer.parseInt(march.getMarchEntity().getTargetId()));
					if (cfg != null) {
						Player player = GlobalData.getInstance().makesurePlayer(march.getPlayerId());
						Long resourceNum = 0L;
						AwardItems awardItems = march.getMarchEntity().getAwardItems();
						for (long count : awardItems.getAwardItemsCount().values()) {
							resourceNum += count;
						}
						LogUtil.logWorldCollect(player, cfg.getId(), cfg.getResType(), cfg.getLevel(), resourceNum, HawkTime.getMillisecond() - march.getMarchEntity().getResStartTime());
					}
				} catch (Exception e) {
					HawkException.catchException(e);
				}
			}
			logger.info("resource march call back, marchData: {}", march);
		}
	}

	/**
	 * 在一个玩家基地是否有我的援助队列
	 * 
	 * @return
	 */
	public IWorldMarch getAssistanceMarch(String playerId, String targetPlayerId) {
		Set<IWorldMarch> marchs = getPlayerPassiveMarchs(targetPlayerId, WorldMarchType.ASSISTANCE_VALUE, WorldMarchStatus.MARCH_STATUS_MARCH_ASSIST_VALUE);
		if (marchs == null) {
			return null;
		}
		for (IWorldMarch march : marchs) {
			if (playerId.equals(march.getPlayerId())) {
				return march;
			}
		}
		return null;
	}

	/**
	 * 行军加速
	 * 
	 * @param march
	 * @param timeReducePercent
	 *            时间减少百分比
	 * @param speedUpTimes
	 *            速度倍数变化
	 * @param itemUseY
	 * @param itemUseX
	 * @param consumeItems
	 *            加速消耗道具
	 */

	public void marchSpeedUp(String marchId, int timeReducePercent, int speedUpTimes, List<MarchSpeedItem> speedConsume, String playerId) {
		IWorldMarch worldMarch = WorldMarchService.getInstance().getMarch(marchId);
		if (worldMarch == null) {
			return;
		}
		
		WorldMarch march = worldMarch.getMarchEntity();
		if (march.isInvalid()) {
			return;
		}

		if (!worldMarch.isMarchState() && !worldMarch.isReturnBackMarch()) {
			return;
		}
		
		// 首先总时间缩短
		long current = HawkTime.getMillisecond();
		long resaveTime = (march.getEndTime() - current) * timeReducePercent / 100;
		march.setEndTime(march.getEndTime() - resaveTime);

		if (ConstProperty.getInstance().isOpenfightUnusualReturnDia()) {
			// 更新行军消耗
			List<MarchSpeedItem> speedInfos = LocalRedis.getInstance().getMarchSpeedConsume(march.getMarchId());
			if (speedInfos != null && speedInfos.size() > 0) {
				speedConsume.addAll(speedInfos); // 添加已消耗物品
				speedConsume = MarchSpeedItem.mergeSpeedList(speedConsume); // 合并同类型
				LocalRedis.getInstance().updateMarchSpeedConsume(march.getMarchId(), MarchSpeedItem.speedListToStr(speedConsume), march.getMarchJourneyTime()); // 更新redis缓存
			} else {
				LocalRedis.getInstance().updateMarchSpeedConsume(march.getMarchId(), MarchSpeedItem.speedListToStr(speedConsume), march.getMarchJourneyTime());
			}
		}

		// 计算当前坐标点和时间并记录
		AlgorithmPoint currPoint = WorldUtil.getMarchCurrentPosition(march);
		if (currPoint != null) {
			if (!Double.isNaN(currPoint.getX()) && !Double.isNaN(currPoint.getY())) {
				march.setItemUseX(currPoint.getX());
				march.setItemUseY(currPoint.getY());
			} else {
				march.setItemUseX(0.0d);
				march.setItemUseY(0.0d);
			}
			march.setSpeedUpTimes(march.getSpeedUpTimes() + speedUpTimes);
			march.setItemUseTime(HawkTime.getMillisecond());
			int px = (int) Math.floor(march.getItemUseX());
			int py = (int) Math.floor(march.getItemUseY());
			if (px == march.getTerminalX() && py == march.getTerminalY()) {
				logger.info("status:{} px:{} py:{} tx:{} ty:{}", march.getMarchStatus(), px, py, march.getTerminalX(), march.getTerminalY());
				return;
			}
			// 更新行军状态
			worldMarch.updateMarch();
		}
		
		if (worldMarch.isMassMarch()) {
			Set<IWorldMarch> joinMarchs = getMassJoinMarchs(worldMarch, false);
			WorldMarchPB.Builder builder = WorldMarchPB.newBuilder();
			for (IWorldMarch joinMarch : joinMarchs) {
				joinMarch.getMarchEntity().setEndTime(march.getEndTime());
				// 发给自己
				Player player = GlobalData.getInstance().getActivePlayer(joinMarch.getMarchEntity().getPlayerId());
				if (player != null) {
					HawkProtocol protocol = HawkProtocol.valueOf(HP.code.WORLD_MARCH_UPDATE_PUSH_VALUE, joinMarch.getMarchEntity().toBuilder(builder.clear(), WorldMarchRelation.SELF));
					player.sendProtocol(protocol);
				}
			}
		}
		
		if(worldMarch instanceof IReportPushMarch){
			((IReportPushMarch) worldMarch).reachTimeChange();
		}
		
//		Player own = GlobalData.getInstance().makesurePlayer(playerId);
//		WorldMarchPB.Builder builder = WorldMarchPB.newBuilder();
//		HawkProtocol protocol = HawkProtocol.valueOf(HP.code.WORLD_MARCH_UPDATE_PUSH_VALUE, worldMarch.getMarchEntity().toBuilder(builder.clear(), WorldMarchRelation.SELF));
//		own.sendProtocol(protocol);
		
		// 记录
		logger.info("world march speed up, marchData: {}, speedUpTimes: {}", march, speedUpTimes);
	}

	/**
	 * 行军购买集结参与者格子数目
	 * 
	 * @param march
	 */
	public void addMarchBuyItems(WorldMarch march) {
		march.setBuyItemTimes(march.getBuyItemTimes() + 1);
	}

	/**
	 * 玩家离开世界
	 * 
	 * @param player
	 */
	public void onPlayerLeave(Player player) {

	}

	/**
	 * 世界地图进入一个玩家
	 * 
	 * @param player
	 * @param y
	 * @param x
	 */
	public MarchSet onPlayerEnter(Player player, int x, int y) {
		try {
			Set<IWorldMarch> marchs = calcInviewMarchs(player, x, y);
			MarchSet marchSet = new MarchSet();
			for (IWorldMarch iWorldMarch : marchs) {
				marchSet.add(iWorldMarch.getMarchId());
			}
			
			// 移除机甲单人返回的行军
			Iterator<IWorldMarch> iterator = marchs.iterator();
			while(iterator.hasNext()) {
				IWorldMarch march = iterator.next();
				
				if (GameConstCfg.getInstance().isNianOptimize()
						&& march != null
						&& march.getMarchType() == WorldMarchType.NIAN_SINGLE
						&& !march.getPlayerId().equals(player.getId())
						&& march.getMarchStatus() == WorldMarchStatus.MARCH_STATUS_RETURN_BACK_VALUE) {
					iterator.remove();
				}
			}
			
			pushInviewMarchs(player, marchSet, MarchEvent.MARCH_SYNC_VALUE);
			return marchSet;
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return new MarchSet();
	}

	/**
	 * 计算视野内行军
	 * 
	 * @param player
	 * @param x
	 * @param y
	 * @return
	 */
	public Set<IWorldMarch> calcInviewMarchs(Player player, int posX, int posY) {
		// 获取所使用的常量
		WorldMapConstProperty constCfg = WorldMapConstProperty.getInstance();
		int viewRadiusX = GameConstCfg.getInstance().getViewXRadius();
		int viewRadiusY = GameConstCfg.getInstance().getViewYRadius();
		int viewRadius = Math.max(viewRadiusX, viewRadiusY);

		Set<IWorldMarch> marchSet = new HashSet<IWorldMarch>();
		
		if (player != null) {
			// 被动行军
			BlockingQueue<IWorldMarch> passiveMarch = getPlayerPassiveMarch(player.getId());
			marchSet.addAll(passiveMarch);
			// 联盟行军
			if (player.hasGuild()) {
				Collection<IWorldMarch> marchMap = getGuildMarchs(player.getGuildId());
				marchSet.addAll(marchMap);
			}
		}
		
		// 视野内起点终点行军
		int startX = Math.max(0, posX - viewRadiusX);
		int endX = Math.min(posX + viewRadiusX, constCfg.getWorldMaxX());
		int startY = Math.max(0, posY - viewRadiusY);
		int endY = Math.min(posY + viewRadiusY, constCfg.getWorldMaxY());
		for (int x = startX; x <= endX; x++) {
			for (int y = startY; y <= endY; y++) {
				Collection<IWorldMarch> marchs = getWorldPointMarch(x, y);
				if (marchs != null && marchs.size() > 0) {
					marchSet.addAll(marchs);
				}
			}
		}
		// 去除因为距离过远不可见的
		Iterator<IWorldMarch> iterator = marchSet.iterator();
		while (iterator.hasNext()) {
			WorldMarch march = iterator.next().getMarchEntity();

			if (march == null || march.isInvalid()) {
				iterator.remove();
				continue;
			}

			// 距离超出视野内的行军不用同步
			if (!march.isVisibleOnPos(posX, posY, viewRadius)) {
				iterator.remove();
				continue;
			}

			// 非行军途中的行军不用同步;非集结等待状态的行军不用同步
			if (march.getMarchStatus() != WorldMarchStatus.MARCH_STATUS_MARCH_VALUE && march.getMarchStatus() != WorldMarchStatus.MARCH_STATUS_RETURN_BACK_VALUE
					&& !(WorldUtil.isMassMarch(march.getMarchType()) && march.getMarchStatus() == WorldMarchStatus.MARCH_STATUS_WAITING_VALUE)) {
				iterator.remove();
				continue;
			}
		}
		return marchSet;
	}

	/**
	 * 观察者视角切换
	 * 
	 * @param player
	 */
	public MarchSet onPlayerMove(Player player, MarchSet inviewMarchs, int x, int y) {
		if (x <= 0 && y <= 0) {
			return new MarchSet();
		}
		
		// 当前视野内的行军
		Set<IWorldMarch> marchs = calcInviewMarchs(player, x, y);
		
		// 之前视野内的行军，如果滑动后还在视野内，则还是加到当前视野内
		int viewRadius = Math.max(GameConstCfg.getInstance().getViewXRadius(), GameConstCfg.getInstance().getViewYRadius());
		for (String marchId : inviewMarchs) {
			IWorldMarch march = getMarch(marchId);
			if (march != null && march.getMarchEntity().isVisibleOnPos(x, y, viewRadius)) {
				marchs.add(march);
			}
		}
		
		// 移除机甲单人行军
		Iterator<IWorldMarch> iterator = marchs.iterator();
		while(iterator.hasNext()) {
			IWorldMarch march = iterator.next();
			if (GameConstCfg.getInstance().isNianOptimize()
					&& march != null
					&& march.getMarchType() == WorldMarchType.NIAN_SINGLE
					&& !march.getPlayerId().equals(player.getId())
					&& march.getMarchStatus() == WorldMarchStatus.MARCH_STATUS_RETURN_BACK_VALUE) {
				iterator.remove();
			}
		}
		
		// 当前的
		MarchSet currentSet  = new MarchSet();
		for (IWorldMarch march : marchs) {
			currentSet.add(march.getMarchId());
		}
		
		try {
			// 推送增量的
			MarchSet pushAddSet = new MarchSet();
			for (String marchId : currentSet) {
				if (!inviewMarchs.contains(marchId)) {
					pushAddSet.add(marchId);
				}
			}
			pushInviewMarchs(player, pushAddSet, MarchEvent.MARCH_ADD_VALUE);
			
			// 推送删除的
			MarchSet pushDelSet = new MarchSet();
			for (String marchId : inviewMarchs) {
				if (!currentSet.contains(marchId)) {
					pushDelSet.add(marchId);
				}
			}
			pushInviewMarchs(player, pushDelSet, MarchEvent.MARCH_DELETE_VALUE);
			
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	
		return currentSet;
	}

	/**
	 * 推送视野内的行军
	 * 
	 * @param player
	 * @param marchSet
	 */
	private void pushInviewMarchs(Player player, MarchSet marchSet, int eventType) {
		if (player != null && player.isActiveOnline() && marchSet != null) {
			MarchEventSync.Builder builder = MarchEventSync.newBuilder();
			builder.setEventType(eventType);
			
			WorldMarchPB.Builder marchBuilder = WorldMarchPB.newBuilder();
			for (String marchId : marchSet) {
				try {
					IWorldMarch march = getMarch(marchId);
					if (march != null) {
						// 自己的行军不走这种同步模式
						WorldMarchRelation relation = WorldUtil.getRelation(march.getMarchEntity(), player);
						if (relation.equals(WorldMarchRelation.SELF)) {
							continue;
						}

						MarchData.Builder dataBuilder = MarchData.newBuilder();
						dataBuilder.setMarchId(marchId);
						
						if (eventType != MarchEvent.MARCH_DELETE_VALUE) {
							dataBuilder.setMarchPB(march.getMarchEntity().toBuilder(marchBuilder.clear(), relation));
						}
						
						builder.addMarchData(dataBuilder);
					}
				} catch (Exception e) {
					HawkException.catchException(e);
				}
			}

			// 发送剩余数据
			HawkProtocol protocol = HawkProtocol.valueOf(HP.code.WORLD_MARCH_EVENT_SYNC_VALUE, builder);
			player.sendProtocol(protocol);
		}
	}

	/**
	 * 玩家迁城时的行军处理
	 * 
	 * @param targetPlayer
	 * @return
	 */
	public boolean mantualMoveCityProcessMarch(Player targetPlayer) {
		AutoMonsterMarchParam autoMarchParam = WorldMarchService.getInstance().getAutoMarchParam(targetPlayer.getId());
		if (autoMarchParam != null) {
			autoMarchParam.setCityMoving(true);
		}

		//收回自动拉锅行军
		PlayerAutoModule autoModule = targetPlayer.getModule(AUTO_GATHER);
		if(null != autoModule){
			autoModule.setCityMoving(true);
		}

		long currentTime = HawkTime.getMillisecond();
		// 援军返回，城点中的援军立即返回行军，走在半路的援军，立即原地返回行军。
		BlockingQueue<IWorldMarch> worldMarchs = getPlayerPassiveMarch(targetPlayer.getId());
		if (worldMarchs != null) {
			for (IWorldMarch march : worldMarchs) {
				if (march == null || march.getMarchEntity().isInvalid() || march.isReturnBackMarch()) {
					continue;
				}
				
				logger.info("mantual move city process passive march, marchData: {}", march);
				BehaviorLogger.log4Service(targetPlayer, Source.WORLD_ACTION, Action.REMOVE_MARCH_WHEN_MANTUAL_CITY, Params.valueOf("marchData", march));
				
				//2018-5-15 已将所有被动行军,移动至各自行军类进行处理
				march.targetMoveCityProcess(targetPlayer, currentTime);
			}
		}

		// 要删除的行军表 城外的部队，包括所有的状态，立即回到城点。（如果发起了组队或参加了组队，则集结队伍解散）
		worldMarchs = getPlayerMarch(targetPlayer.getId());
		for (IWorldMarch march : worldMarchs) {
			logger.info("mantual move city process self march, marchData: {}", march);
			march.moveCityProcess(currentTime);
			removeMarch(march);
		}
		return true;
	}

	/**
	 * 
	 * @param targetPlayer
	 * @param march
	 * @param currentTime
	 */
	public void assistanceMarchPlayerMoveCityProcess(Player targetPlayer, IWorldMarch march, long currentTime) {

		// 行军返回
		AlgorithmPoint point = WorldUtil.getMarchCurrentPosition(march.getMarchEntity());
		onMarchReturn(march, currentTime, march.getMarchEntity().getAwardItems(), march.getMarchEntity().getArmys(), point.getX(), point.getY());

		// 向玩家援助
		final Player tarPlayer = GlobalData.getInstance().makesurePlayer(march.getMarchEntity().getTargetId());
		if (tarPlayer == null) {
			return;
		}

		int icon = GuildService.getInstance().getGuildFlagByPlayerId(march.getPlayerId());
		Object[] subTitle = new Object[] { tarPlayer.getName() };

		MailParames.Builder mailParames = MailParames.newBuilder().setPlayerId(march.getPlayerId()).addSubTitles(subTitle).setIcon(icon);
		if (march.getMarchType() == WorldMarchType.ASSISTANCE_RES) {
			// 发邮件---资源援助者高迁或被打飞
			mailParames.addContents(tarPlayer.getName()).setMailId(MailId.RES_ASSISTANCE_FAILED_TARGET_CHANGED);
		} else if (march.getMarchType() == WorldMarchType.ASSISTANCE) {
			// 发邮件---士兵援助失败（受助者高迁或被打飞）
			mailParames.addContents(tarPlayer.getName()).setMailId(MailId.SOILDER_ASSISTANCE_FAILED_TARGET_CHANGED);
		}
		GuildMailService.getInstance().sendMail(mailParames.build());
	}

	/**
	 * 战斗异常返还加速道具
	 * 
	 * @param march
	 */
	public void returnSpeedTools(WorldMarch march) {
		List<MarchSpeedItem> speedConsume = LocalRedis.getInstance().getMarchSpeedConsume(march.getMarchId());
		if (speedConsume != null && speedConsume.size() > 0) {
			for (MarchSpeedItem speedInfo : speedConsume) {
				Player player = GlobalData.getInstance().makesurePlayer(speedInfo.getPlayerId());
				AwardItems awardItems = AwardItems.valueOf();
				awardItems.addItemInfos(MarchSpeedItem.getItemInfo(speedInfo)); // 获取加速消耗物品
				awardItems.rewardTakeAffectAndPush(player, Action.BATTLE_EXCEPTION_BACK_TOOL); // 返还物品
			}
			LocalRedis.getInstance().removeMarchSpeedConsume(march.getMarchId());
		}
	}

	/**
	 * 集结成员迁城时行军处理
	 * 
	 * @param march
	 * @param currentTime
	 */
	public void massJoinPlayerMarchMoveCityProcess(IWorldMarch march, long currentTime) {
		if (march.isReturnBackMarch()) {
			onMarchReturnImmediately(march, march.getMarchEntity().getArmys());
			return;
		}
		
		IWorldMarch leaderMarch = WorldMarchService.getInstance().getMarch(march.getMarchEntity().getTargetId());
		if (leaderMarch != null && leaderMarch.getMarchEntity().getMarchStatus() == WorldMarchStatus.MARCH_STATUS_WAITING_VALUE) {
			// 发邮件---发车前有人主动离开
			Set<IWorldMarch> marchers = WorldMarchService.getInstance().getMassJoinMarchs(leaderMarch, false);
			marchers.add(march);
			marchers.add(leaderMarch);
			
			String name = GlobalData.getInstance().getPlayerNameById(march.getPlayerId());
			int icon = GuildService.getInstance().getGuildFlagByPlayerId(march.getPlayerId());
			
			for (IWorldMarch worldMarch : marchers) {
				GuildMailService.getInstance().sendMail(MailParames.newBuilder()
								.setPlayerId(worldMarch.getPlayerId())
								.setMailId(MailId.MASS_PLAYER_LEAVE)
								.addSubTitles(name)
								.addContents(name)
								.setIcon(icon)
								.build());
			}
		}
		
		if (leaderMarch != null && leaderMarch.isMarchState()) {
			// 当前位置
			AlgorithmPoint point = WorldUtil.getMarchCurrentPosition(leaderMarch.getMarchEntity());
			// 队员行军返回
			Set<IWorldMarch> joinMarchs = getMassJoinMarchs(leaderMarch, false);
			for (IWorldMarch joinMarch : joinMarchs) {
				if (joinMarch.getMarchId().equals(march.getMarchId()) || march.getMarchEntity().getMarchStatus() != WorldMarchStatus.MARCH_STATUS_JOIN_MARCH_VALUE) {
					continue;
				}
				
				joinMarch.getMarchEntity().setCallBackX(point.getX());
				joinMarch.getMarchEntity().setCallBackY(point.getY());
				joinMarch.getMarchEntity().setCallBackTime(currentTime);
				
				onMonsterRelatedMarchAction(joinMarch);
				onMarchCallBack(joinMarch);
			}
			// 队长行军返回
			onMonsterRelatedMarchAction(leaderMarch);
			leaderMarch.getMarchEntity().setCallBackX(point.getX());
			leaderMarch.getMarchEntity().setCallBackY(point.getY());
			leaderMarch.getMarchEntity().setCallBackTime(currentTime);
			onMarchCallBack(leaderMarch);
			
			
			// 发送迁城邮件
			String movePlayerName = GlobalData.getInstance().getPlayerNameById(march.getPlayerId());
			for (IWorldMarch joinMarch : joinMarchs) {
				sendMassMemberMoveCityMail(joinMarch.getPlayerId(), movePlayerName);
			}
			sendMassMemberMoveCityMail(leaderMarch.getPlayerId(), movePlayerName);
			
		}
		// 自己的行军瞬间返回
		onMonsterRelatedMarchAction(march);
		onMarchReturnImmediately(march, march.getMarchEntity().getArmys());
	}

	/**
	 * 迁城时处理集结类型的行军
	 * 
	 * @param massMarch
	 */
	public void mantualMassMarch(IWorldMarch massMarch, long currentTime) {
		if (massMarch.isReturnBackMarch()) {
			// 队长返回
			onMarchReturnImmediately(massMarch, massMarch.getMarchEntity().getArmys());
			return;
		}
		
		AlgorithmPoint point = WorldUtil.getMarchCurrentPosition(massMarch.getMarchEntity());
		Set<IWorldMarch> joinMarchs = getMassJoinMarchs(massMarch, false);
		
		// 发送迁城邮件
		String movePlayerName = GlobalData.getInstance().getPlayerNameById(massMarch.getPlayerId());
		for (IWorldMarch joinMarch : joinMarchs) {
			sendMassMemberMoveCityMail(joinMarch.getPlayerId(), movePlayerName);
		}
		sendMassMemberMoveCityMail(massMarch.getPlayerId(), movePlayerName);
		
		// 队长已出发
		if (massMarch.isMarchState()) {
			// 队员返回
			for (IWorldMarch joinMarch : joinMarchs) {
				if (joinMarch.isReturnBackMarch()) {
					continue;
				}

				joinMarch.getMarchEntity().setCallBackX(point.getX());
				joinMarch.getMarchEntity().setCallBackY(point.getY());
				joinMarch.getMarchEntity().setCallBackTime(currentTime);
				
				onMonsterRelatedMarchAction(joinMarch);
				onMarchCallBack(joinMarch);
			}
			
		} else if (massMarch.getMarchEntity().getMarchStatus() == WorldMarchStatus.MARCH_STATUS_WAITING_VALUE) {
			for (IWorldMarch joinMarch : joinMarchs) {
				if (joinMarch.isReturnBackMarch()) {
					continue;
				}
				
				onMonsterRelatedMarchAction(joinMarch);
				onPlayerNoneAction(joinMarch, currentTime);
				
				if (joinMarch.getMarchType() == WorldMarchType.MONSTER_MASS_JOIN) {
					YuriMailService.getInstance().sendMail(MailParames.newBuilder().setPlayerId(joinMarch.getMarchEntity().getPlayerId()).setMailId(MailId.MASS_MONSTER_TEAM_DISSOLVE).build());
				}
			}

			if (massMarch.getMarchType() == WorldMarchType.MONSTER_MASS) {
				YuriMailService.getInstance().sendMail(MailParames.newBuilder().setPlayerId(massMarch.getPlayerId()).setMailId(MailId.MASS_MONSTER_TEAM_DISSOLVE).build());
			}
		}
		
		// 队长返回
		onMonsterRelatedMarchAction(massMarch);
		onMarchReturnImmediately(massMarch, massMarch.getMarchEntity().getArmys());
	}

	/**
	 * 发邮件:参与集结的玩家迁城
	 * @param playerId
	 * @param moveMemberName
	 */
	private void sendMassMemberMoveCityMail(String playerId, String moveMemberName) {
		GuildMailService.getInstance().sendMail(MailParames
				.newBuilder()
				.setPlayerId(playerId)
				.setMailId(MailId.MASS_MEMBER_MOVE_CITY)
				.addContents(moveMemberName)
				.build());
	}

	/**
	 * 检查是否向同一目标点发起过同类型的行军
	 * 
	 * @param playerId
	 * @param point
	 * @return
	 */
	public boolean hasSameMarch(String playerId, WorldPoint point, int marchType) {
		BlockingQueue<IWorldMarch> marchSet = getPlayerMarch(playerId);
		if (marchSet == null || marchSet.size() <= 0) {
			return false;
		}
		for (IWorldMarch march : marchSet) {
			if (march == null || march.getMarchEntity().isInvalid()) {
				continue;
			}
			if (march.getMarchEntity().getTerminalId() == point.getId() && march.getMarchEntity().getMarchType() == marchType) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 是否有空闲出征队列
	 * 
	 * @return
	 */
	public boolean isHasFreeMarch(Player player) {
		//机器人不受行军数量限制
		if(player.isRobot()){
			return true;
		}
		// 行军个数
		int marchCount = getPlayerMarchCount(player.getId());

		// 释放将领的行军不占用行军队列
		List<IWorldMarch> captiveMarchs = getPlayerMarch(player.getId(), WorldMarchType.CAPTIVE_RELEASE_VALUE);
		if (captiveMarchs != null) {
			marchCount -= captiveMarchs.size();
		}

		int maxMarchCnt = player.getMaxMarchNum();
		if (marchCount > maxMarchCnt) {
			return false;
		}
		
		// 行军队列数达到上限时，判断额外侦查行军队列是否占用；没有占用额外侦查队列就达上限了，说明真的时达到了上限，此时如果是发起非侦查行军，直接return不让发起，侦查行军看情况
		if (marchCount == maxMarchCnt && !isExtraSypMarchOccupied(player)) {
			return false;
		}
		
		return true;
	}

	/**
	 * 判断额外侦查行军队列是否被占用
	 * 
	 * @param player
	 * @return
	 */
	public boolean isExtraSypMarchOccupied(Player player) {
		List<IWorldMarch> spyMarchs = getPlayerMarch(player.getId(), WorldMarchType.SPY_VALUE);
		for (IWorldMarch march : spyMarchs) {
			if (march.isExtraSpyMarch()) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * 判断额外侦查队列是否可以使用了
	 * 
	 * @param player
	 * @return
	 */
	public boolean isExtraSpyMarchOpen(Player player) {
		int radarLevel = player.getData().getBuildingMaxLevel(BuildingType.RADAR_VALUE);
		if (radarLevel < ConstProperty.getInstance().getExtraSpyRadarLv()) {
			return false;
		}
		
		return true;
	}

	
	public void sendBattleResultInfo(IWorldMarch march, boolean isWin, List<ArmyInfo> atkArmyList, List<ArmyInfo> defArmyList, boolean isMonsterDead) {
		sendBattleResultInfo(march, isWin, atkArmyList, defArmyList, isMonsterDead, false, null);
	}
	
	public void sendBattleResultInfo(IWorldMarch march, boolean isWin, List<ArmyInfo> atkArmyList, List<ArmyInfo> defArmyList, boolean isMonsterDead, boolean deadlyStrike) {
		sendBattleResultInfo(march, isWin, atkArmyList, defArmyList, isMonsterDead, deadlyStrike, null);
	}
	
	/**
	 * 发送战斗结果builder 用于前台播放战斗动画
	 * 
	 * @param atkArmyList
	 *            我方战斗结果
	 * @param defArmyList
	 *            敌方战斗结果
	 * @param isWin
	 *            攻击方战斗
	 */
	public void sendBattleResultInfo(IWorldMarch march, boolean isWin, List<ArmyInfo> atkArmyList, List<ArmyInfo> defArmyList, boolean isMonsterDead, boolean deadlyStrike, KickSnowballDirection direction) {
		HPBattleResultInfoSync.Builder builder = HPBattleResultInfoSync.newBuilder();
		builder.setMarchId(march.getMarchId());
		for (ArmyInfo army : atkArmyList) {
			builder.addMyArmyId(army.getArmyId());
		}
		for (ArmyInfo army : defArmyList) {
			builder.addOppArmyId(army.getArmyId());
		}
		builder.setIsMonsterDead(isMonsterDead);

		if (isWin) {
			builder.setIsWin(Result.SUCCESS_VALUE);
		} else {
			builder.setIsWin(Result.FAIL_VALUE);
		}

		WorldPoint worldPoint = WorldPointService.getInstance().getWorldPoint(march.getTerminalId());
		if (worldPoint != null) {
			if (worldPoint.getPointType() == WorldPointType.PLAYER_VALUE) {
				if (isWin) {
					long commonHurtEndTime = worldPoint.getCommonHurtEndTime();
					commonHurtEndTime = GameUtil.getOnFireEndTime(commonHurtEndTime);
					WorldPointService.getInstance().notifyWorldPointBeAttacked(worldPoint, commonHurtEndTime);
				}
			}
		}
		
		builder.setIsDeadlyStrike(deadlyStrike);
		
		if (direction != null) {
			builder.setDirection(direction);
		}
		
		if (GameConstCfg.getInstance().isNianOptimize() && march.getMarchType() == WorldMarchType.NIAN_SINGLE) {
			march.getPlayer().sendProtocol(HawkProtocol.valueOf(HP.code.BATTLE_INFO_S_VALUE, builder));
		} else {
			broadcastBattleResult(march.calcPointViewers(0, 0), builder);
		}
		
		// 客户端要求雪球给自己单独同步一份
		if (march.getMarchType() == WorldMarchType.SNOWBALL_MARCH) {
			march.getPlayer().sendProtocol(HawkProtocol.valueOf(HP.code.BATTLE_INFO_S_VALUE, builder));
		}
	}

	/**
	 * 广播战斗信息
	 * 
	 * @param marchId
	 * @param builder
	 * @param player
	 * @param targetPlayer
	 */
	public void broadcastBattleResult(Set<String> viewerIds, HPBattleResultInfoSync.Builder builder) {
		try {
			if (viewerIds != null && viewerIds.size() > 0) {
				for (String playerId : viewerIds) {
					Player player = GlobalData.getInstance().getActivePlayer(playerId);
					if (player != null) {
						player.sendProtocol(HawkProtocol.valueOf(HP.code.BATTLE_INFO_S_VALUE, builder));
					}
				}
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}


	/**
	 * 战斗后更新行军数据
	 * 
	 * @param march
	 *            行军对象
	 * @param armys
	 *            行军的军队
	 * @return
	 */
	public boolean updateMarchArmy(IWorldMarch march, List<ArmyInfo> armys) {
		if (march == null || march.getMarchEntity().isInvalid()) {
			return false;
		}

		if (armys == null) {
			armys = new ArrayList<ArmyInfo>();
		}

		march.getMarchEntity().setArmys(armys);

		// 元首战争类型
		if (march.isPresidentMarch()) {
			List<IWorldMarch> marchs = getPresidentQuarteredMarchs();
			for (IWorldMarch tempMarch : marchs) {
				if (tempMarch == null) {
					continue;
				}
				// Player player =
				// GlobalData.getInstance().makesurePlayer(march.getPlayerId());
				// HawkProtocol protocol =
				// WorldUtil.getQuarteredMarchUpdateProtocol(PushQuarteredMarchType.PUSH_QUARTERED_PRESIDENT,
				// player.getGuildId(), 0, getGuildWarShowPB(march));
				// GuildService.getInstance().broadcastProtocol(player.getGuildId(),
				// protocol);
				break;
			}
		}
		
		if (march.getPlayer() != null && march.getPlayer().isActiveOnline()) {
			WorldMarchPB.Builder builder = WorldMarchPB.newBuilder();
			HawkProtocol protocol = HawkProtocol.valueOf(HP.code.WORLD_MARCH_UPDATE_PUSH_VALUE, march.getMarchEntity().toBuilder(builder.clear(), WorldMarchRelation.SELF));
			march.getPlayer().sendProtocol(protocol);
		}
		
		return true;
	}

	/**
	 * 清空已援助的资源
	 * 
	 * @param march
	 */
	public void clearAssistantResource(WorldMarch march) {
		if (march.getMarchType() != WorldMarchType.ASSISTANCE_RES_VALUE) {
			return;
		}
		march.setAssistantStr(null);
	}

	/**
	 * 行军使命结束，不需要做回兵回资源等后续操作，原地删除，请慎用！
	 * 
	 * @param march
	 * @return
	 */
	public boolean onWorldMarchOver(IWorldMarch march) {
		logger.info("world march over, marchData: {}", march);
		return removeMarch(march);
	}

	/**
	 * 世界点上是否有其它联盟的玩家
	 * 
	 * @param playerId
	 * @param wp
	 * @return
	 */
	public boolean hasEnemyInPoint(String playerId, WorldPoint wp) {
		if (HawkOSOperator.isEmptyString(playerId) || wp == null) {
			return false;
		}
		switch (wp.getPointType()) {
		case WorldPointType.GUILD_TERRITORY_VALUE:
			BlockingDeque<String> marchIds = getManorMarchs(wp.getId());
			Iterator<String> iter = marchIds.iterator();
			while (iter.hasNext()) {
				IWorldMarch march = getMarch(iter.next());
				if(march != null && march.isReachAndStopMarch() && !GuildService.getInstance().isInTheSameGuild(playerId, march.getPlayerId())
						&& !playerId.equals(march.getPlayerId())){
					return true;
				}
			}
			break;
		default:
			break;
		}
		return false;
	}

	/**
	 * 解散首都驻扎行军
	 */
	public void dissolveAllPresidentQuarteredMarchs() {
		BlockingDeque<String> marchIds = getPresidentMarchs();
		for (String marchId : marchIds) {
			IWorldMarch worldMarch = getMarch(marchId);
			if (worldMarch == null || worldMarch.getMarchEntity().isInvalid()) {
				continue;
			}
			int armyCount = worldMarch.getArmys().stream().mapToInt(a -> a.getFreeCnt()).sum();
			LogUtil.logCrossActivtyControlPresident(worldMarch.getPlayer(), worldMarch.getPlayer().getGuildId(), armyCount);
			onMarchReturn(worldMarch, HawkTime.getMillisecond(), 0);
		}
	}

	/**
	 * 解散箭塔驻扎行军
	 */
	public void dissolveAllPresidentTowerQuarteredMarchs(int pointId) {
		BlockingDeque<String> marchIds = getPresidentTowerMarchs(pointId);
		for (String marchId : marchIds) {
			IWorldMarch worldMarch = getMarch(marchId);
			if (worldMarch == null || worldMarch.getMarchEntity().isInvalid()) {
				continue;
			}
			onMarchReturn(worldMarch, HawkTime.getMillisecond(), 0);
		}
	}
	
	/**
	 * 退出联盟，处理玩家行军
	 * 
	 * @param playerId
	 * @return false表示执行失败，不能退出联盟，true表示成功执行
	 */
	public boolean doQuitGuild(String playerId, String guildId) {
		if (HawkOSOperator.isEmptyString(playerId) || HawkOSOperator.isEmptyString(guildId)) {
			return false;
		}
		if (!checkCanQuitGuild(playerId)) {
			return false;
		}
		
		doQuitGuildPassiveMach(playerId, guildId); // 退出联盟被动行军处理
		doQuitGuildMach(playerId, guildId); // 退出联盟主动行军处理
		
		return true;
	}

	/**
	 * 退出联盟被动行军处理
	 * 
	 * @param playerId
	 */
	private void doQuitGuildPassiveMach(String playerId, String guildId) {
		BlockingQueue<IWorldMarch> worldMarchs = getPlayerPassiveMarch(playerId);
		for (IWorldMarch march : worldMarchs) {
			// 移除联盟战争相关
			rmGuildMarch(march.getMarchId());
			
			// 返回类型行军 || 非援助类型行军 不处理
			if (march.isReturnBackMarch() || (!march.isAssistanceMarch() && !march.isManorMarch())) {
				continue;
			}
			
			boolean marchState = WorldUtil.isMarchState(march.getMarchEntity());
			
			// 行军中的队伍从当前点回
			if (marchState) {
				AlgorithmPoint currPoint = WorldUtil.getMarchCurrentPosition(march.getMarchEntity());
				onMarchReturn(march, HawkTime.getMillisecond(), march.getMarchEntity().getAwardItems(), march.getMarchEntity().getArmys(), currPoint.getX(), currPoint.getY());
				
			} else {
				// 非行军中的队伍从目标点返回
				onMarchReturn(march, HawkTime.getMillisecond(), 0);
			}
			
			if (march.isAssistanceMarch()) {
				// 士兵援助行军变化通知
				Player targetPlayer = GlobalData.getInstance().makesurePlayer(march.getMarchEntity().getTargetId());
				if (targetPlayer != null) {
					WorldMarchService.getInstance().notifyAssistanceMarchChange(targetPlayer, march.getMarchId());
				}
			}
			
			logger.info("doQuitGuildPassiveMarch, marchState:{}, march:{}", marchState, march.getMarchEntity().toString());
		}
	}

	/**
	 * 退出联盟主动行军处理
	 * 
	 * @param playerId
	 */
	private void doQuitGuildMach(String playerId, String guildId) {
		BlockingQueue<IWorldMarch> worldMarchs = getPlayerMarch(playerId);
		for (IWorldMarch march : worldMarchs) {
			// 返回类型行军不处理
			if (march.isReturnBackMarch()) {
				continue;
			}
			
			// 移除联盟战争相关
			rmGuildMarch(march.getMarchId());
			
			// 非援助类型行军 && 非领地行军 不处理
			if (!march.isAssistanceMarch() && !march.isManorMarch()
					&& !march.isWarFlagMarch()) {
				continue;
			}
			
			// 是否是行军状态中
			boolean marchState = WorldUtil.isMarchState(march.getMarchEntity());
			
			// 行军中的队伍从当前点回
			if (marchState) {
				AlgorithmPoint currPoint = WorldUtil.getMarchCurrentPosition(march.getMarchEntity());
				onMarchReturn(march, HawkTime.getMillisecond(), march.getMarchEntity().getAwardItems(), march.getMarchEntity().getArmys(), currPoint.getX(), currPoint.getY());
				
			} else {
				// 非行军中的队伍从目标点返回
				onMarchReturn(march, HawkTime.getMillisecond(), 0);
			}
			
			if (march.isAssistanceMarch()) {
				// 士兵援助行军变化通知
				Player targetPlayer = GlobalData.getInstance().makesurePlayer(march.getMarchEntity().getTargetId());
				if (targetPlayer != null) {
					WorldMarchService.getInstance().notifyAssistanceMarchChange(targetPlayer, march.getMarchId());
				}
			}
			logger.info("doQuitGuildMarch, marchState:{}, march:{}", marchState, march.getMarchEntity().toString());
		}
	}

	/**
	 * 判断目标点是否有自己的驻军
	 * 
	 * @param player
	 * @param point
	 * @return true为有驻军
	 */
	public boolean existMarchOnPoint(Player player, WorldPoint point) {
		Collection<IWorldMarch> terminalPtMarchs = getWorldPointMarch(point.getX(), point.getY());
		if (terminalPtMarchs == null || terminalPtMarchs.size() == 0) {
			return false;
		}

		for (IWorldMarch march : terminalPtMarchs) {
			WorldMarch targetMarch = march.getMarchEntity();
			if (targetMarch != null
					&& player.getId().equals(targetMarch.getPlayerId())
					&& (targetMarch.getMarchStatus() == WorldMarchStatus.MARCH_STATUS_MARCH_COLLECT_VALUE
							|| targetMarch.getMarchStatus() == WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED_VALUE
							|| targetMarch.getMarchStatus() == WorldMarchStatus.MARCH_STATUS_MARCH_ASSIST_VALUE
							|| targetMarch.getMarchStatus() == WorldMarchStatus.MARCH_STATUS_MANOR_BREAK_VALUE
							|| targetMarch.getMarchStatus() == WorldMarchStatus.MARCH_STATUS_MANOR_BUILD_VALUE
							|| targetMarch.getMarchStatus() == WorldMarchStatus.MARCH_STATUS_MANOR_GARRASION_VALUE
							|| targetMarch.getMarchStatus() == WorldMarchStatus.MARCH_STATUS_MANOR_REPAIR_VALUE
							|| targetMarch.getMarchStatus() == WorldMarchStatus.MARCH_STATUS_EXPLORE_VALUE
							|| targetMarch.getMarchStatus() == WorldMarchStatus.MARCH_STATUS_OCCUPY_VALUE)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * 是否在该基地有援助士兵的行军
	 * 
	 * @param viewerId
	 * @param pointId
	 * @return
	 */
	public boolean hasManorMarch(String playerId, int pointId) {
		BlockingQueue<IWorldMarch> marchs = getPlayerMarch(playerId);
		return marchs.stream().anyMatch(march -> march.isManorMarch() && pointId == march.getMarchEntity().getTerminalId() && march.isReachAndStopMarch());
	}

	/**
	 * 是否在该基地有援助士兵的行军
	 * 
	 * @param viewerId
	 * @param pointId
	 * @return
	 */
	public boolean isHasAssistanceMarch(String viewerId, int pointId) {
		BlockingQueue<IWorldMarch> marchs = getPlayerMarch(viewerId);
		if (marchs == null) {
			return false;
		}
		for (IWorldMarch march : marchs) {
			if (march == null || march.getMarchEntity().isInvalid()) {
				continue;
			}
			if (march.getMarchType() != WorldMarchType.ASSISTANCE) {
				continue;
			}
			if (march.getMarchEntity().getTerminalId() == pointId && march.getMarchEntity().getMarchStatus() == WorldMarchStatus.MARCH_STATUS_MARCH_ASSIST_VALUE) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 是否有国王战相关行军
	 * @param playerId
	 * @return
	 */
	public boolean hasPresidentMarch(String playerId) {
		BlockingQueue<IWorldMarch> playerMarch = getPlayerMarch(playerId);
		for (IWorldMarch march : playerMarch) {
			if (march.isPresidentMarch() || march.isPresidentTowerMarch()) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 是否是攻击性行为的行军(发起时判断走此方法)
	 * @param playerId
	 * @param marchType
	 * @param point
	 * @return
	 */
	public boolean isOffensiveAction(String playerId, WorldMarchType marchType, WorldPoint terminalPoint) {
		if (terminalPoint == null) {
			return false;
		}

		if (marchType.equals(WorldMarchType.GUNDAM_SINGLE)
				|| marchType.equals(WorldMarchType.GUNDAM_MASS)
				|| marchType.equals(WorldMarchType.GUNDAM_MASS_JOIN)
				
				||marchType.equals(WorldMarchType.NIAN_SINGLE)
				|| marchType.equals(WorldMarchType.NIAN_MASS)
				|| marchType.equals(WorldMarchType.NIAN_MASS_JOIN)
				
				|| marchType.equals(WorldMarchType.CHRISTMAS_MASS)
				|| marchType.equals(WorldMarchType.CHRISTMAS_MASS_JOIN)
				|| marchType.equals(WorldMarchType.CHRISTMAS_SINGLE)
				|| marchType.equals(WorldMarchType.PYLON_MARCH)) {
			return false;
		}
		
		if (marchType.equals(WorldMarchType.MONSTER_MASS)
				|| marchType.equals(WorldMarchType.MONSTER_MASS_JOIN)) {
			return false;
		}
		
		if (marchType.equals(WorldMarchType.ATTACK_PLAYER)
				
				|| marchType.equals(WorldMarchType.MASS)
				|| marchType.equals(WorldMarchType.MASS_JOIN)

				

				|| marchType.equals(WorldMarchType.SUPER_WEAPON_SINGLE)
				|| marchType.equals(WorldMarchType.SUPER_WEAPON_MASS)
				|| marchType.equals(WorldMarchType.SUPER_WEAPON_MASS_JOIN)
				
				|| marchType.equals(WorldMarchType.XZQ_SINGLE)
				|| marchType.equals(WorldMarchType.XZQ_MASS)
				|| marchType.equals(WorldMarchType.XZQ_MASS_JOIN)

				|| marchType.equals(WorldMarchType.FORTRESS_SINGLE)
				|| marchType.equals(WorldMarchType.FORTRESS_MASS)
				|| marchType.equals(WorldMarchType.FORTRESS_JOIN)
				
				|| marchType.equals(WorldMarchType.ESPIONAGE_MARCH)
						
				|| marchType.equals(WorldMarchType.WAR_FLAG_MASS)
				|| marchType.equals(WorldMarchType.WAR_FLAG_MASS_JOIN)) {

			return true;
		}
		if(marchType.equals(WorldMarchType.PRESIDENT_SINGLE)
				|| marchType.equals(WorldMarchType.PRESIDENT_MASS)
				|| marchType.equals(WorldMarchType.PRESIDENT_MASS_JOIN)

				|| marchType.equals(WorldMarchType.PRESIDENT_TOWER_SINGLE)
				|| marchType.equals(WorldMarchType.PRESIDENT_TOWER_MASS)
				|| marchType.equals(WorldMarchType.PRESIDENT_TOWER_MASS_JOIN)){
			if(!CrossActivityService.getInstance().isOpen()){
				return true;
			}
			
		}
		if (marchType.equals(WorldMarchType.SPY)) {
			if(terminalPoint.getPointType() == WorldPointType.KING_PALACE_VALUE ||
					terminalPoint.getPointType() == WorldPointType.CAPITAL_TOWER_VALUE){
				if(CrossActivityService.getInstance().isOpen()){
					return false;
				}
			}
			if(terminalPoint.getPointType() == WorldPointType.PYLON_VALUE){
				return false;
			}
			return terminalPoint.getPointType() != WorldPointType.FOGGY_FORTRESS_VALUE;
		}
		
		if (marchType.equals(WorldMarchType.ARMY_QUARTERED)
				|| marchType.equals(WorldMarchType.COLLECT_RESOURCE)
				|| marchType.equals(WorldMarchType.STRONGPOINT)
				|| marchType.equals(WorldMarchType.TREASURE_HUNT_RESOURCE)) {
			
			int[] pos = GameUtil.splitXAndY(terminalPoint.getId());
			Collection<IWorldMarch> worldPointMarchs = getWorldPointMarch(pos[0], pos[1]);
			for (IWorldMarch march : worldPointMarchs) {
				if (!march.isReachAndStopMarch()) {
					continue;
				}
				
				if (!GuildService.getInstance().isInTheSameGuild(playerId, march.getPlayerId())) {
					return true;
				}
			}
			
		}
		
		if (marchType.equals(WorldMarchType.MANOR_SINGLE)
				|| marchType.equals(WorldMarchType.MANOR_MASS)
				|| marchType.equals(WorldMarchType.MANOR_MASS_JOIN)) {
			if (!GuildService.getInstance().isPlayerInGuild(terminalPoint.getGuildId(), playerId)) {
				return true;
			}
		}
		
		if (marchType.equals(WorldMarchType.WAR_FLAG_MARCH)) {
			IFlag flag = FlagCollection.getInstance().getFlag(terminalPoint.getGuildBuildId());
			if (!GuildService.getInstance().isPlayerInGuild(flag.getCurrentId(), playerId)) {
				return true;
			}
		}
		
		//如果是圣诞宝箱行军
		if (marchType.equals(WorldMarchType.CHRISTMAS_BOX_MARCH)) {
			String occyPlayerId = terminalPoint.getPlayerId();
			//有人占领
			if (!HawkOSOperator.isEmptyString(occyPlayerId)) {
				return !GuildService.getInstance().isInTheSameGuild(playerId, occyPlayerId);
			}
		}
		
		return false;
	}

	/**
	 * 是否有攻击性行为的行军(添加城市保护时使用此方法)
	 */
	public boolean hasOffensiveMarch(String playerId) {
		BlockingQueue<IWorldMarch> marchs = getPlayerMarch(playerId);
		for (IWorldMarch march : marchs) {
			
			// add 1.4.88.89 驻扎类(采集据点驻扎)行军，只有出发行军中才判断攻击性
			if (isQuarterTypeMarch(march) && march.getMarchStatus() != WorldMarchStatus.MARCH_STATUS_MARCH_VALUE) {
				continue;
			}
			
			if (march.getMarchEntity().isOffensive()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 获取攻击状态时间
	 */
	public long getOffsiveStateTime(String playerId) {
		if (hasOffensiveMarch(playerId)) {
			return getOffensiveMarchStartTime(playerId);
		} else {
			return offsiveMarchBackTime.getOrDefault(playerId, 0L);
		}
	}
	
	/**
	 * 获取攻击行为行军发起的时间(最近的一条)
	 */
	public long getOffensiveMarchStartTime (String playerId) {
		long retTime = 0L;
		BlockingQueue<IWorldMarch> marchs = getPlayerMarch(playerId);
		for (IWorldMarch march : marchs) {
			
			if (isQuarterTypeMarch(march) && march.getMarchStatus() != WorldMarchStatus.MARCH_STATUS_MARCH_VALUE) {
				continue;
			}
			
			if (!march.getMarchEntity().isOffensive()) {
				continue;
			}
			
			retTime = Math.max(retTime, march.getMarchEntity().getStartTime());
		}
		return retTime;
	}
	
	/**
	 * 更新攻击行为行军返回时间
	 */
	public void updateOffsiveMarchBackTime(String playerId) {
		offsiveMarchBackTime.put(playerId, HawkTime.getMillisecond());
		GameUtil.notifyDressShow(playerId);
	}
	
	private boolean isQuarterTypeMarch(IWorldMarch march) {
		WorldMarchType marchType = march.getMarchType();
		return marchType.equals(WorldMarchType.ARMY_QUARTERED)
				|| marchType.equals(WorldMarchType.COLLECT_RESOURCE)
				|| marchType.equals(WorldMarchType.STRONGPOINT);
	}
	
	/**
	 * 获取超级武器行军
	 * @param pointId
	 * @return
	 */
	public BlockingDeque<String> getSuperWeaponMarchs(int pointId) {
		if (!superWeaponMarchs.containsKey(pointId)) {
			BlockingDeque<String> marchs = new LinkedBlockingDeque<String>();
			superWeaponMarchs.put(pointId, marchs);
		}
		return superWeaponMarchs.get(pointId);
	}
	
	/**
	 * 获取远征要塞行军
	 */
	public BlockingDeque<String> getFortressMarchs(int pointId) {
		if (!fortressMarchs.containsKey(pointId)) {
			BlockingDeque<String> marchs = new LinkedBlockingDeque<String>();
			fortressMarchs.put(pointId, marchs);
		}
		return fortressMarchs.get(pointId);
	}
	
	/**
	 * 更换超级武器驻军队长
	 * @param pointId
	 * @param targetPlayerId
	 */
	public void changeSuperWeaponMarchLeader(int pointId, String targetPlayerId) {
		String changeMarchId = null;
		
		BlockingDeque<String> marchs = getSuperWeaponMarchs(pointId);
		for (String marchId : marchs) {
			IWorldMarch march = WorldMarchService.getInstance().getMarch(marchId);
			if (!targetPlayerId.equals(march.getPlayerId())) {
				continue;
			}
			changeMarchId = march.getMarchId();
			break;
		}
		
		if (HawkOSOperator.isEmptyString(changeMarchId)) {
			return;
		}
		
		marchs.remove(changeMarchId);
		marchs.addFirst(changeMarchId);
	}
	
	/**
	 * 更换远征要塞驻军队长
	 */
	public void changeFortressMarchLeader(int pointId, String targetPlayerId) {
		String changeMarchId = null;
		
		BlockingDeque<String> marchs = getFortressMarchs(pointId);
		for (String marchId : marchs) {
			IWorldMarch march = WorldMarchService.getInstance().getMarch(marchId);
			if (!targetPlayerId.equals(march.getPlayerId())) {
				continue;
			}
			changeMarchId = march.getMarchId();
			break;
		}
		
		if (HawkOSOperator.isEmptyString(changeMarchId)) {
			return;
		}
		
		marchs.remove(changeMarchId);
		marchs.addFirst(changeMarchId);
	}
	
	/**
	 * 添加超级武器行军
	 * @param pointId
	 * @param march
	 * @param isInit
	 */
	public void addSuperWeaponMarch(int pointId, IWorldMarch march, boolean isInit) {
		BlockingDeque<String> marchs = getSuperWeaponMarchs(pointId);
		//初始化需要判断队长行军
		if(isInit){
			if(march.getPlayerId().equals(march.getMarchEntity().getLeaderPlayerId())){
				marchs.addFirst(march.getMarchId());
			} else {
				marchs.add(march.getMarchId());
			}
		} else {
			//如果没有行军，则此行军为队长行军
			if(marchs.isEmpty()){
				march.getMarchEntity().setLeaderPlayerId(march.getPlayerId());
				marchs.add(march.getMarchId());
				
				// 广播超级武器状态改变
				IWeapon weapon = SuperWeaponService.getInstance().getWeapon(pointId);
				weapon.broadcastSingleSuperWeaponInfo(null);
				
			} else {
				WorldMarch leaderMarch = getWorldMarch(marchs.getFirst());
				march.getMarchEntity().setLeaderPlayerId(leaderMarch.getPlayerId());
				marchs.add(march.getMarchId());
			}
		}
	}
	
	/**
	 * 添加远征要塞行军
	 */
	public void addFortressMarch(int pointId, IWorldMarch march, boolean isInit) {
		BlockingDeque<String> marchs = getFortressMarchs(pointId);
		//初始化需要判断队长行军
		if(isInit){
			if(march.getPlayerId().equals(march.getMarchEntity().getLeaderPlayerId())){
				marchs.addFirst(march.getMarchId());
			} else {
				marchs.add(march.getMarchId());
			}
		} else {
			//如果没有行军，则此行军为队长行军
			if(marchs.isEmpty()){
				march.getMarchEntity().setLeaderPlayerId(march.getPlayerId());
				marchs.add(march.getMarchId());
				
				// 广播超级武器状态改变
				IFortress fortress = CrossFortressService.getInstance().getFortress(pointId);
				fortress.broadcastSingleInfo(null);
				
			} else {
				WorldMarch leaderMarch = getWorldMarch(marchs.getFirst());
				march.getMarchEntity().setLeaderPlayerId(leaderMarch.getPlayerId());
				marchs.add(march.getMarchId());
			}
		}
	}
	
	/**
	 * 是否有超级武器行军
	 * @return
	 */
	public boolean hasSuperWeaponMarch(int index) {
		return !getSuperWeaponMarchs(index).isEmpty();
	}
	
	/**
	 * 是否有航海远征行军
	 */
	public boolean hasFortressMarch(int pointId) {
		return !getFortressMarchs(pointId).isEmpty();
	}
	
	/**
	 * 获取超级武器队长行军id
	 * @param pointId
	 * @return
	 */
	public String getSuperWeaponLeaderMarchId(int pointId) {
		BlockingDeque<String> marchIds = getSuperWeaponMarchs(pointId);
		if (marchIds.isEmpty()) {
			return null;
		}
		return marchIds.getFirst();
	}
	
	public String getFortressLeaderMarchId(int pointId) {
		BlockingDeque<String> marchIds = getFortressMarchs(pointId);
		if (marchIds.isEmpty()) {
			return null;
		}
		return marchIds.getFirst();
	}
	
	public String getFlagLeaderMarchId(String flagId) {
		BlockingDeque<String> marchIds = getFlagMarchs(flagId);
		if (marchIds.isEmpty()) {
			return null;
		}
		return marchIds.getFirst();
	}
	
	/**
	 * 超级武器队长
	 * @return
	 */
	public Player getSuperWeaponLeader(int pointId) {
		String leaderMarchId = getSuperWeaponLeaderMarchId(pointId);
		if (HawkOSOperator.isEmptyString(leaderMarchId)) {
			return null;
		}
		
		IWorldMarch leaderMarch = getMarch(leaderMarchId);
		Player leader = GlobalData.getInstance().makesurePlayer(leaderMarch.getPlayerId());
		return leader;
	}
	
	public Player getFortressLeader(int pointId) {
		String leaderMarchId = getFortressLeaderMarchId(pointId);
		if (HawkOSOperator.isEmptyString(leaderMarchId)) {
			return null;
		}
		
		IWorldMarch leaderMarch = getMarch(leaderMarchId);
		if (leaderMarch == null) {
			return null;
		}
		
		Player leader = GlobalData.getInstance().makesurePlayer(leaderMarch.getPlayerId());
		return leader;
	}
	
	/**
	 * 获取超级武器行军
	 * @param pointId
	 * @return
	 */
	public List<IWorldMarch> getSuperWeaponStayMarchs(int pointId) {
		BlockingDeque<String> marchIds = getSuperWeaponMarchs(pointId);
		List<IWorldMarch> stayMarchs = new ArrayList<IWorldMarch>();
		
		for (String marchId : marchIds) {
			IWorldMarch march = getMarch(marchId);
			if (march == null || march.getMarchEntity().isInvalid()) {
				continue;
			}
			stayMarchs.add(march);
		}
		return stayMarchs;
	}
	
	public List<IWorldMarch> getFortressStayMarchs(int pointId) {
		BlockingDeque<String> marchIds = getFortressMarchs(pointId);
		List<IWorldMarch> stayMarchs = new ArrayList<IWorldMarch>();
		
		for (String marchId : marchIds) {
			IWorldMarch march = getMarch(marchId);
			if (march == null || march.getMarchEntity().isInvalid()) {
				continue;
			}
			stayMarchs.add(march);
		}
		return stayMarchs;
	}
	
	/**
	 * 移除超级武器行军
	 * @param pointId
	 * @param rmMarchId
	 */
	public void removeSuperWeaponMarch(int pointId, String rmMarchId) {
		BlockingDeque<String> marchIds = getSuperWeaponMarchs(pointId);
		if (!marchIds.contains(rmMarchId)) {
			return;
		}
		
		// 是否是队长行军
		boolean isLeaderMarch = rmMarchId.equals(marchIds.getFirst());
		
		marchIds.remove(rmMarchId);
		
		if (marchIds.isEmpty()) {
			// 广播超级武器状态改变
			IWeapon weapon = SuperWeaponService.getInstance().getWeapon(pointId);
			weapon.changeOccuption(null, null);
			return;
		}
		
		if (isLeaderMarch)  {
			// 重设队长
			String newLeaderMarchId = marchIds.getFirst();
			IWorldMarch leaderMarch = getMarch(newLeaderMarchId);
			Player newLeader = GlobalData.getInstance().makesurePlayer(leaderMarch.getPlayerId());
			String leaderId = newLeader.getId();
			for (String marchId : marchIds) {
				IWorldMarch march = getMarch(marchId);
				march.getMarchEntity().setLeaderPlayerId(leaderId);
			}
			
			// 广播超级武器状态改变
			IWeapon weapon = SuperWeaponService.getInstance().getWeapon(pointId);
			weapon.broadcastSingleSuperWeaponInfo(null);
		}
	}
	
	
	public void removeFortressMarch(int pointId, String rmMarchId) {
		BlockingDeque<String> marchIds = getFortressMarchs(pointId);
		if (!marchIds.contains(rmMarchId)) {
			return;
		}
		
		// 是否是队长行军
		boolean isLeaderMarch = rmMarchId.equals(marchIds.getFirst());
		
		marchIds.remove(rmMarchId);
		
		if (marchIds.isEmpty()) {
			// 广播超级武器状态改变
			IFortress frortress = CrossFortressService.getInstance().getFortress(pointId);
			frortress.changeOccuption(null);
			return;
		}
		
		if (isLeaderMarch)  {
			// 重设队长
			String newLeaderMarchId = marchIds.getFirst();
			IWorldMarch leaderMarch = getMarch(newLeaderMarchId);
			Player newLeader = GlobalData.getInstance().makesurePlayer(leaderMarch.getPlayerId());
			String leaderId = newLeader.getId();
			for (String marchId : marchIds) {
				IWorldMarch march = getMarch(marchId);
				march.getMarchEntity().setLeaderPlayerId(leaderId);
			}
			
			// 广播超级武器状态改变
			IFortress frortress = CrossFortressService.getInstance().getFortress(pointId);
			frortress.broadcastSingleInfo(null);
		}
	}
	/**
	 * 解散超级武器行军
	 */
	public void dissolveAllSuperWeaponQuarteredMarchs(int pointId) {
		BlockingDeque<String> marchIds = getSuperWeaponMarchs(pointId);
		for (String marchId : marchIds) {
			IWorldMarch worldMarch = getMarch(marchId);
			if (worldMarch == null || worldMarch.getMarchEntity().isInvalid()) {
				continue;
			}
			onMarchReturn(worldMarch, HawkTime.getMillisecond(), 0);
		}
	}
	
	public void dissolveAllFortressQuarteredMarchs(int pointId) {
		BlockingDeque<String> marchIds = getFortressMarchs(pointId);
		for (String marchId : marchIds) {
			IWorldMarch worldMarch = getMarch(marchId);
			if (worldMarch == null || worldMarch.getMarchEntity().isInvalid()) {
				continue;
			}
			onMarchReturn(worldMarch, HawkTime.getMillisecond(), 0);
		}
	}
	
	/**
	 * 士兵援助行军变化通知
	 * @param targetPlayer
	 * @param marchId
	 */
	public void notifyAssistanceMarchChange(Player targetPlayer, String marchId) {
		if (targetPlayer == null || HawkOSOperator.isEmptyString(marchId)) {
			return;
		}
		NotifyAssistantMarchChange.Builder builder = NotifyAssistantMarchChange.newBuilder();
		builder.addMarchId(marchId);
		targetPlayer.sendProtocol(HawkProtocol.valueOf(HP.code.ASSISTANCE_MARCH_CHANGE_NOTICE_VALUE, builder));
	}
	
	/**
	 * 添加侦查标记
	 * @param player
	 * @param pointId
	 * @param mailId
	 */
	public void addSpyMark(Player player, int pointId, String mailId, String marchId) {
		int currentTime = HawkTime.getSeconds();
		int spyMarkDisappearTime = WorldMarchConstProperty.getInstance().getSpyMarkDisappearTime();
		player.getEntity().removeOverTimeSpyMark(currentTime, spyMarkDisappearTime);
		player.getEntity().addSpyMark(new SpyMarkItem(pointId, currentTime, mailId, marchId));
		
		WorldPoint worldPoint = WorldPointService.getInstance().getWorldPoint(pointId);
		if (worldPoint != null) {
			WorldPointService.getInstance().getWorldScene().update(worldPoint.getAoiObjId());
		}
	}

	/**
	 * 获取侦查标记
	 * @param player
	 * @param pointId
	 * @param marchId
	 * @return
	 */
	public SpyMarkItem getSpyMark(Player player, int pointId, String marchId, int pointType) {
		long current = HawkTime.getSeconds();
		int spyMarkDisappearTime = WorldMarchConstProperty.getInstance().getSpyMarkDisappearTime();
		List<SpyMarkItem> spyMarkInfo = player.getEntity().getSpyMarkInfo();
		for (SpyMarkItem spyMark : spyMarkInfo) {
			if (spyMark.getPointId() != pointId || spyMark.getStartTime() - current > spyMarkDisappearTime) {
				continue;
			}
			if ((pointType == WorldPointType.RESOURCE_VALUE || pointType == WorldPointType.QUARTERED_VALUE)
					&& (HawkOSOperator.isEmptyString(marchId) || !marchId.equals(spyMark.getMarchId()))) {
				continue;
			}
			return spyMark;
		}
		return null;
	}
	
	/**
	 * 获取玩家资源援助次数
	 * @return
	 */
	public int getPlayerAssistanceResTimes(Player player) {
		int assDay = 0;
		int assTimes = 0;
		String resAssStr = RedisProxy.getInstance().getPlayerVar(player.getId(), GsConst.RES_ASS);
		if (!HawkOSOperator.isEmptyString(resAssStr)) {
			int resAssInt = Integer.parseInt(resAssStr);
			int[] resAssSplit = GameUtil.splitXAndY(resAssInt);
			assDay = resAssSplit[0];
			assTimes = resAssSplit[1];
		}
		if (assDay != HawkTime.getYearDay()) {
			assDay = HawkTime.getYearDay();
			assTimes = 0;
		}
		return assTimes;
	}
	
	/**
	 * 更新玩家资源援助次数
	 * @return
	 */
	public void updatePlayerAssistanceResTimes(Player player, int times) {
		int day = HawkTime.getYearDay();
		int resAssInt = GameUtil.combineXAndY(day, times);
		RedisProxy.getInstance().updatePlayerVar(player.getId(), GsConst.RES_ASS, String.valueOf(resAssInt));
	}
	
	/**
	 * 获取玩家被资源援助次数
	 * @return
	 */
	public int getPlayerBeAssistanceResTimes(Player player) {
		int assDay = 0;
		int assTimes = 0;
		String resAssStr = RedisProxy.getInstance().getPlayerVar(player.getId(), GsConst.RES_BE_ASS);
		if (!HawkOSOperator.isEmptyString(resAssStr)) {
			int resAssInt = Integer.parseInt(resAssStr);
			int[] resAssSplit = GameUtil.splitXAndY(resAssInt);
			assDay = resAssSplit[0];
			assTimes = resAssSplit[1];
		}
		if (assDay != HawkTime.getYearDay()) {
			assDay = HawkTime.getYearDay();
			assTimes = 0;
		}
		return assTimes;
	}
	
	/**
	 * 更新玩家被资源援助次数
	 * @return
	 */
	public void updatePlayerBeAssistanceResTimes(Player player, int times) {
		int day = HawkTime.getYearDay();
		int resAssInt = GameUtil.combineXAndY(day, times);
		RedisProxy.getInstance().updatePlayerVar(player.getId(), GsConst.RES_BE_ASS, String.valueOf(resAssInt));
	}
	
	/**
	 * 检测并修复玩家士兵(慎用，暂时只能在起服或者脚本中调用)
	 * @param player
	 * @param fix
	 */
	public boolean checkAndFixArmy(Player player, boolean fix) {
		boolean needFixArmy = false;
		
		try {
			// army上出征数量
			Map<Integer, Integer> armyMap = getArmyMap(player);
			
			// 行军上出征数量
			Map<Integer, Integer> marchMap = getMarchMap(player);
			
			needFixArmy = isNeedFixArmy(player, armyMap, marchMap);
			
			// 是否需要修复
			if (fix && needFixArmy) {
				fixArmy(player, armyMap, marchMap);
				player.getPush().syncArmyInfo(ArmyChangeCause.DEFAULT);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
			needFixArmy = false;
		}
		
		return needFixArmy;
	}
	
	/**
	 * 获取army上出征数量
	 * @param player
	 * @return
	 */
	private Map<Integer, Integer> getArmyMap(Player player) {
		
		Map<Integer, Integer> armyMap = new HashMap<>();
		List<ArmyEntity> armyEntities = player.getData().getArmyEntities();
		for (ArmyEntity armyEntity : armyEntities) {
			if (armyEntity.getMarch() <= 0) {
				continue;
			}
			armyMap.put(armyEntity.getArmyId(), armyEntity.getMarch());
		}
		return armyMap;
	}
	
	/**
	 * 获取march上出征数量
	 * @param player
	 * @return
	 */
	private Map<Integer, Integer> getMarchMap(Player player) {
		Map<Integer, Integer> marchMap = new HashMap<>();
		BlockingQueue<IWorldMarch> marchs = WorldMarchService.getInstance().getPlayerMarch(player.getId());
		for (IWorldMarch march : marchs) {
			for (ArmyInfo army : march.getArmys()) {
				int armyId = army.getArmyId();
				int totalCount = army.getTotalCount();
				
				if (marchMap.containsKey(armyId)) {
					marchMap.put(armyId, totalCount + marchMap.get(armyId));
				} else {
					marchMap.put(armyId, totalCount);
				}
			}
		}
		return marchMap;
	}
	
	/**
	 * 是否需要修复
	 * @param armyMap
	 * @param marchMap
	 * @return
	 */
	private boolean isNeedFixArmy(Player player, Map<Integer, Integer> armyMap, Map<Integer, Integer> marchMap) {
		boolean needFix = false;
		// 比对
		for (Entry<Integer, Integer> army : armyMap.entrySet()) {
			int armyId = army.getKey();
			int marchCount = army.getValue();
			
			if (!marchMap.containsKey(armyId)) {
				needFix = true;
				break;
			}
			
			if (marchCount != marchMap.get(armyId)) {
				needFix = true;
				break;
			}
		}
		
		if (needFix) {
			logger.info("scan fixArmy player, playerId:{}, armyMap:{}, marchMap:{}", player.getId(), armyMap.toString(), marchMap.toString());
		}
		return needFix;
	}
	
	/**
	 * 修复玩家士兵
	 * @param armyMap
	 * @param marchMap
	 */
	private void fixArmy(Player player, Map<Integer, Integer> armyMap, Map<Integer, Integer> marchMap) {
		for (Entry<Integer, Integer> army : armyMap.entrySet()) {
			//士兵Id
			int armyId = army.getKey();
			//army上出征的数量
			int armyMarch = army.getValue();
			//实际出征的数量
			int realMarch = 0;
			if (marchMap.containsKey(armyId)) {
				realMarch = marchMap.get(armyId);
			}
			
			if (armyMarch == realMarch) {
				continue;
			}
			
			ArmyEntity armyEntity = player.getData().getArmyEntity(armyId);
			if (armyEntity== null) {
				logger.error("fixArmy error, armyEntity null, playerId:{}, armyId:{}", player.getId(), armyId);
				continue;
			}
			
			int entityMarchBefore = armyEntity.getMarch();
			int entityFreeBefore = armyEntity.getFree();
			
			try {
				if (realMarch < armyMarch) {
					int armyDiff = armyMarch - realMarch;
					armyEntity.addFree(armyDiff);
					armyEntity.addMarch(-armyDiff);
				}
				
				if (realMarch > armyMarch) {
					logger.error("fixArmy error, realMarch > armyMarch");
				}
				
				logger.error("fixArmy, playerId:{}, openId:{}, playerName:{}, platForm:{}, cheannel:{}, armyUuid:{}, armyId:{}, realMarch:{}, armyMarch:{}, entityMarchBefore:{}, entityFreeBefore:{}, entityMarchAfter:{}, entityFreeAfter:{}", 
						player.getId(), player.getOpenId(), player.getName(), player.getPlatform(), player.getChannel(), armyEntity.getId(), armyId, realMarch, armyMarch, entityMarchBefore, entityFreeBefore, armyEntity.getMarch(), armyEntity.getFree());
			} catch (Exception e) {
				logger.error("fixArmy error, playerId:{}, armyId:{}, realMarch:{}, armyMarch:{}, entityMarchBefore:{}, entityFreeBefore:{}, entityMarchAfter:{}, entityFreeAfter:{}", 
						player.getId(), armyId, realMarch, armyMarch, entityMarchBefore, entityFreeBefore, armyEntity.getMarch(), armyEntity.getFree());
				HawkException.catchException(e);
			}
		}
	}
	
	/**
	 * 是否是跨服限制类型行军
	 * @param player
	 * @param marchType
	 * @return
	 */
	public boolean isCrossMarchLimit(Player player, WorldMarchType marchType) {
		if (player.isCsPlayer() && WorldUtil.isCrossLimitMarch(marchType)) {
			return true;
		}
		return false;
	}
	
	/**
	 * 发起寻宝行军 
	 */
	public void startTreasureHuntMarch(Player player,int itemCount){
		logger.info("startTreasureHurtMarch, playerId:{},count:{}", player.getId(),itemCount);
		int[] pos = GameUtil.splitXAndY(WorldPlayerService.getInstance().getPlayerPos(player.getId()));
		int maxDis = TreasureHuntConstProperty.getInstance().getMaxSearchRadius();
		List<Point> allPointList = WorldPointService.getInstance().getRhoAroundPointsFree(pos[0], pos[1], maxDis);
		//默认降序
		Collections.shuffle(allPointList);
		for(int i=0;i< itemCount;i++){
			if(allPointList.size() <= 0){
				break;
			}
			Point point = allPointList.remove(allPointList.size() -1);
			// 目标点
			int terminalId = GameUtil.combineXAndY(point.getX(), point.getY());
			// 发起行军
			WorldMarchService.getInstance().startMarch(player, WorldMarchType.TREASURE_HUNT_VALUE, terminalId, "", null, 0, 0, 0, itemCount, new EffectParams());
			LogUtil.logTreasureHuntToolUse(player);
			logger.info("startTreasureHurtMarch, playerId:{},count:{}", player.getId(),itemCount);
				
		}
		logger.info("startTreasureHurtMarch end, playerId:{},count:{}", player.getId(),itemCount);
	}
	
	/**
	 * 获取行军builder返回信息
	 */
	public MarchArmyDetailInfo.Builder getMarchDetialInfo(IWorldMarch march) {
		MarchArmyDetailInfo.Builder info = MarchArmyDetailInfo.newBuilder();
		Player player = march.getPlayer();
		if (player == null || player.getName() == null) {
			return info;
		}
		
		info.setPlayerName(player.getName());
		for (PlayerHero hero : march.getHeros()) {
			info.addHeros(hero.toArmyHeroPb());
			info.addHeroList(hero.toPBobj());
		}
		
		for (ArmyInfo army : march.getArmys()) {
			info.addArmys(army.toArmySoldierPB(player));
		}
		Optional<SuperSoldier> ssoldierOp = player.getSuperSoldierByCfgId(march.getSuperSoldierId());
		if(ssoldierOp.isPresent()){
			info.setSsoldier(ssoldierOp.get().toPBobj());
		}
		ArmourSuitType armourSuit = ArmourSuitType.valueOf(march.getMarchEntity().getArmourSuit());
		if (armourSuit != null) {
			info.setArmourSuit(armourSuit);
		}
		return info;
	}
	
	/**
	 * 获取自动打野行军参数
	 * 
	 * @param playerId
	 * @return
	 */
	public AutoMonsterMarchParam getAutoMarchParam(String playerId) {
		return autoMarchParamMap.get(playerId);
	}
	
	public void addAutoMarchParam(String playerId, AutoMonsterMarchParam autoMarchParam) {
		autoMarchParamMap.put(playerId, autoMarchParam);
	}
	
	public void closeAutoMarch(String playerId) {
		autoMarchParamMap.remove(playerId);
	}
	
	/**
	 * 推送自动打野状态
	 * 
	 * @param status 1开启状态，0关闭状态
	 */
	public void pushAutoMarchStatus(Player player, int status) {
		if (player == null) {
			return;
		}
		
		AtkMonsterAutoMarchStatusPB.Builder builder = AtkMonsterAutoMarchStatusPB.newBuilder();
		builder.setStatus(status);
		player.sendProtocol(HawkProtocol.valueOf(HP.code.ATK_MONSTER_AUTO_MARCH_STATUS_S, builder));
	}
	
	/**
	 * 中断结束自动打野
	 * 
	 * @param player
	 * @param status
	 */
	public void breakAutoMarch(Player player, int status) {
		if (player == null) {
			return;
		}
		
		closeAutoMarch(player.getId());
		pushAutoMarchStatus(player, 0);
		if (status > 0) {
			player.sendError(HP.code.SWITCH_ATK_MONSTER_AUTO_MARCH_C_VALUE, status, 0);
		}
	}
	
	/**
	 * 获取点上的行军
	 * @param point
	 * @return
	 */
	public WorldMarch getMarchByPoint(WorldPoint point) {
		String marchId = "";
		if (point.getPointType() == WorldPointType.SUPER_WEAPON_VALUE) {
			marchId = WorldMarchService.getInstance().getSuperWeaponLeaderMarchId(point.getId());
		} else if(point.getPointType() == WorldPointType.XIAO_ZHAN_QU_VALUE){
			marchId = WorldMarchService.getInstance().getXZQLeaderMarchId(point.getId());
		}else if (point.getPointType() == WorldPointType.CAPITAL_TOWER_VALUE) {
			marchId = WorldMarchService.getInstance().getPresidentTowerLeaderMarchId(point.getId());
		} else if (point.getPointType() == WorldPointType.KING_PALACE_VALUE) {
			marchId = WorldMarchService.getInstance().getPresidentLeaderMarch();
		} else if (point.getPointType() == WorldPointType.CROSS_FORTRESS_VALUE) {
			marchId = WorldMarchService.getInstance().getFortressLeaderMarchId(point.getId());
		}
		
		return WorldMarchService.getInstance().getWorldMarch(marchId);
	}

	/**
	 * 获取玩家上次使用雪球攻击的时间
	 */
	public long getLastSnowballAtkTime(String playerId) {
		if (!lastSnowballAtkTime.containsKey(playerId)) {
			return 0L;
		}
		return lastSnowballAtkTime.get(playerId);
	}

	/**
	 * 设置玩家上次使用雪球攻击的时间
	 */
	public void putLastSnowballAtkTime(String playerId, long time) {
		lastSnowballAtkTime.put(playerId, time);
	}
	
	
	/**
	 * 装备科技新加的一个东西， 采集会掉落东西，隔一段时间掉一份。每天掉落有次数限制。
	 */
	public void calcExtraDrop2(IWorldMarch march) {

		int cityLevel = march.getPlayer().getCityLv();
		if (cityLevel > ConstProperty.getInstance().getResCollectExtraDropLevelLimitMax() || cityLevel < ConstProperty.getInstance().getResCollectExtraDropLevelLimitMin()) {
			return;
		}

		DailyDataEntity dailyDataEntity = march.getPlayer().getData().getDailyDataEntity();

		// 每日掉落次数限制
		int timesLimit = ConstProperty.getInstance().getResCollectExtraDropTimesLimit();
		if (dailyDataEntity.getResCollDropTimes() >= timesLimit) {
			return;
		}
		
		// 采集时长
		long collectTime = march.getMassReadyTime();
		// tick次数
		int addTimes = (int) ((collectTime / 1000) / ConstProperty.getInstance().getResCollectExtraDropTime());
		// 额外奖励id
		int extraAwardId = ConstProperty.getInstance().getResCollectExtraDropAward();
		
		AwardItems extraAward = AwardItems.valueOf();
		// 之前结算的
		List<ItemInfo> award = ItemInfo.valueListOf(march.getMarchEntity().getAwardExtraStr());
		extraAward.addItemInfos(award);
		
		addTimes = Math.min(addTimes, timesLimit - dailyDataEntity.getResCollDropTimes());
		for (int i = 0; i < addTimes; i++) {
			extraAward.addAward(extraAwardId);
		}
		
		march.getMarchEntity().setAwardExtraStr(extraAward.toString());
		
		// 设置次数
		dailyDataEntity.setResCollDropTimes(dailyDataEntity.getResCollDropTimes() + addTimes);
	}
	
	/**
	 * 国家科技新加的一个东西， 采集会掉落东西，隔一段时间掉一份。每天掉落有次数限制。
	 */
	public void calcExtraDrop3(IWorldMarch march) {
		NationTechCfg cfg = HawkConfigManager.getInstance().getConfigByKey(NationTechCfg.class, 11101);
		if (cfg == null) {
			return;
		}
		
		if (march.getPlayer() == null || march.getPlayer().isCsPlayer()) {
			return;
		}
		
		// param1 触发buffId
		int buffId = Integer.parseInt(cfg.getParam1());
		// 掉落次数限制
		int timesLimit = Integer.parseInt(cfg.getParam2());
		// 计算周期(秒)
		int tickPeriod = Integer.parseInt(cfg.getParam3());
		// 掉落奖励id
		int awardId = Integer.parseInt(cfg.getParam4());
		
		BuffCfg buffCfg = HawkConfigManager.getInstance().getConfigByKey(BuffCfg.class, buffId);
		if (buffCfg == null) {
			return;
		}
		
		Map<Integer, GlobalBuffEntity> buffMap = BuffService.getInstance().getBuffMap();
		GlobalBuffEntity globalBuffEntity = buffMap.get(buffCfg.getEffect());
		if (globalBuffEntity == null) {
			return;
		}
		if (globalBuffEntity.getEndTime() < HawkTime.getMillisecond()) {
			return;
		}
		
		// 每日掉落次数限制
		DailyDataEntity dailyDataEntity = march.getPlayer().getData().getDailyDataEntity();
		if (dailyDataEntity.getNationSkillDropTimes() >= timesLimit) {
			return;
		}
		
		// 采集时长
		long collectTime = march.getMassReadyTime();
		// tick次数
		int addTimes = (int) ((collectTime / 1000) / tickPeriod);
		
		AwardItems extraAward = AwardItems.valueOf();
		// 之前结算的
		List<ItemInfo> award = ItemInfo.valueListOf(march.getMarchEntity().getAwardExtraStr());
		extraAward.addItemInfos(award);
		
		addTimes = Math.min(addTimes, timesLimit - dailyDataEntity.getResCollDropTimes());
		for (int i = 0; i < addTimes; i++) {
			extraAward.addAward(awardId);
		}
		
		march.getMarchEntity().setAwardExtraStr(extraAward.toString());
		
		// 设置次数
		dailyDataEntity.setNationSkillDropTimes(dailyDataEntity.getResCollDropTimes() + addTimes);
	}
	
	/**
	 * 添加小战区行军
	 * @param pointId
	 * @param march
	 * @param isInit
	 */
	public void addXZQMarch(int pointId, IWorldMarch march, boolean isInit) {
		xzqMarchs.addXZQMarch(pointId, march, isInit);
	}
	
	/**
	 * 更换小战区驻军队长
	 * @param pointId
	 * @param targetPlayerId
	 */
	public void changeXZQMarchLeader(int pointId, String targetPlayerId) {
		xzqMarchs.changeXZQMarchLeader(pointId, targetPlayerId);
	}
	
	/**
	 * 解散小战区行军
	 */
	public void dissolveAllXZQQuarteredMarchs(int pointId) {
		xzqMarchs.dissolveAllXZQQuarteredMarchs(pointId);
	}
	/**
	 * 小战区队长
	 * @return
	 */
	public Player getXZQLeader(int pointId) {
		return xzqMarchs.getXZQLeader(pointId);
	}
	/**
	 * 获取小战区队长行军id
	 * @param pointId
	 * @return
	 */
	public String getXZQLeaderMarchId(int pointId) {
		return xzqMarchs.getXZQLeaderMarchId(pointId);
	}
	/**
	 * 获取小战区行军
	 * @param pointId
	 * @return
	 */
	public BlockingDeque<String> getXZQMarchs(int pointId) {
		return xzqMarchs.getXZQMarchs(pointId);
	}
	/**
	 * 获取小战区行军
	 * @param pointId
	 * @return
	 */
	public List<IWorldMarch> getXZQStayMarchs(int pointId) {
		return xzqMarchs.getXZQStayMarchs(pointId);
	}
	/**
	 * 是否有小战区行军
	 * @return
	 */
	public boolean hasXZQMarch(int index) {
		return xzqMarchs.hasXZQMarch(index);
	}
	
	/**
	 * 移除小战区行军
	 * @param pointId
	 * @param rmMarchId
	 */
	public void removeXZQMarch(int pointId, String rmMarchId) {
		xzqMarchs.removeXZQMarch(pointId, rmMarchId);
	}
	
	/**
	 * 获取行军开始时间
	 * @param marchId
	 * @return
	 */
	public long getMarchStartTime(String marchId) {
		WorldMarch worldMarch = getWorldMarch(marchId);
		return worldMarch.getStartTime();
	}
	
	/**
	 * 获取玩家加入集结的行军状态
	 * @param massMarchId
	 */
	public WorldMarchStatus getMassJoinMarchStatus(String playerId, String massMarchId) {
		try {
			IWorldMarch march = WorldMarchService.getInstance().getMarch(massMarchId);
			Set<IWorldMarch> massJoinMarchs = getMassJoinMarchs(march, false);
			for (IWorldMarch joinMarch : massJoinMarchs) {
				if (!joinMarch.getPlayerId().equals(playerId)) {
					continue;
				}
				return WorldMarchStatus.valueOf(joinMarch.getMarchStatus());
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return null;
	}
	
	public void armysCheckAndFix(Player player) {
		int marchCount = WorldMarchService.getInstance().getPlayerMarchCount(player.getId());
		if (marchCount > 0) {
			HawkLog.logPrintln("armysCheckAndFix, playerId: {}, marchCount: {}", player.getId(), marchCount);
			return;
		}

		List<Integer> armyIds = new ArrayList<Integer>();
		for (ArmyEntity armyEntity : player.getData().getArmyEntities()) {
			// 出征中的army数量
			int marchArmyCount = armyEntity.getMarch();
			if (marchArmyCount <= 0) {
				HawkLog.logPrintln("armysCheckAndFix, playerId: {}, marchArmyCount: {}", player.getId(), marchArmyCount);
				continue;
			}
			
			int armyId = armyEntity.getArmyId();
			armyIds.add(armyId);
			
			armyEntity.clearMarch();
			armyEntity.addFree(marchArmyCount);
			LogUtil.logArmyChange(player, armyEntity, marchArmyCount, ArmySection.FREE, ArmyChangeReason.MARCH_FIX);
			HawkLog.logPrintln("armysCheckAndFix, playerId:{}, armyId:{}, marchArmyCount:{}, armyFree:{}", armyEntity.getPlayerId(), armyEntity.getId(), marchArmyCount, armyEntity.getFree());
		}

		if (!armyIds.isEmpty() && player.isActiveOnline()) {
			player.getPush().syncArmyInfo(ArmyChangeCause.MARCH_BACK, armyIds.toArray(new Integer[armyIds.size()]));
		}
	}
	
}
