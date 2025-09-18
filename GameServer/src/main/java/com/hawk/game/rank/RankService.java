package com.hawk.game.rank;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.hawk.app.HawkAppObj;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBManager;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.task.HawkTaskManager;
import org.hawk.thread.HawkTask;
import org.hawk.tickable.HawkPeriodTickable;
import org.hawk.tuple.HawkTuple2;
import org.hawk.xid.HawkXID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hawk.game.config.BuildingCfg;
import com.hawk.game.config.GameConstCfg;
import com.hawk.game.config.GuardianConstConfig;
import com.hawk.game.config.RankCfg;
import com.hawk.game.entity.BuildingBaseEntity;
import com.hawk.game.entity.GuildInfoObject;
import com.hawk.game.entity.PlayerBaseEntity;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.LocalRedis;
import com.hawk.game.global.RedisKey;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.invoker.guard.GuardRankAddInvoker;
import com.hawk.game.invoker.guard.GuardRankDeleteInvoker;
import com.hawk.game.module.nationMilitary.rank.NationMilitaryRankObj;
import com.hawk.game.player.Player;
import com.hawk.game.player.PowerElectric;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.Const.BuildingType;
import com.hawk.game.protocol.IDIP.NoticeMode;
import com.hawk.game.protocol.IDIP.NoticeType;
import com.hawk.game.protocol.Rank.RankInfo;
import com.hawk.game.protocol.Rank.RankType;
import com.hawk.game.protocol.Status.IdipMsgCode;
import com.hawk.game.rank.guardRank.GuardRankObject;
import com.hawk.game.service.GuildService;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.GsConst.ScoreType;
import com.hawk.gamelib.GameConst;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.gamelib.rank.RankScoreHelper;

import redis.clients.jedis.Tuple;



/**
 * 排行服务管理器
 */
public class RankService extends HawkAppObj {
	static Logger logger = LoggerFactory.getLogger("Server");

	/**
	 * 单例对象
	 */
	private static RankService instance = null;

	/**
	 * 排行对象
	 */
	Map<RankType, RankObject> rankObjects;

	/**
	 * 封禁对象
	 */
	Map<RankType, Map<String, Long>> banMaps;
	
	/**
	 * 玩家战力变更列表
	 */
	volatile Map<String, Double> powerChangeMap;
	
	/**
	 * 玩家去兵战力变更表
	 */
	volatile Map<String, Double> noArmyPowerChangeMap;
	
	/**
	 * 荣耀大本等级积分偏移
	 */
	public static int HONOR_CITY_OFFSET = 100;
	
	private GuardRankObject guardRankObject = null;
	/**
	 * 获取实体对象
	 * 
	 * @return
	 */
	public static RankService getInstance() {
		return instance;
	}

	/**
	 * 默认构造
	 * 
	 * @param xid
	 */
	public RankService(HawkXID xid) {
		super(xid);
		// 设置实例
		instance = this;
		rankObjects = new ConcurrentHashMap<>();
		banMaps = new ConcurrentHashMap<>();
	}

	/**
	 * 初始化排行信息
	 * 
	 * @return
	 */
	public boolean init() {
		// 加载排行对象
		loadRankObjects();
		
		// 加载封禁信息
		loadBanMaps();
		NationMilitaryRankObj.getInstance().refreshRank();
		// 定时检测封禁榜单数据
		int period = GameConstCfg.getInstance().getCheckRankBanPeriod();
		addTickable(new HawkPeriodTickable(period, period) {
			@Override
			public void onPeriodTick() {
				HawkTaskManager.getInstance().postExtraTask(new HawkTask() {
					@Override
					public Object run() {
						// 个人排行榜
						for (RankType rankType : GsConst.PERSONAL_RANK_TYPE) {
							if (checkBanInfo(rankType)) {
								loadAndRefreshRank(rankType);
							}
						}

						// 联盟排行榜
						for (RankType rankType : GsConst.GUILD_RANK_TYPE) {
							if (checkBanInfo(rankType)) {
								loadAndRefreshRank(rankType);
							}
						}
						NationMilitaryRankObj.getInstance().refreshRank();
						return null;
					}
				}, 0);
			}
		});
		
		// 定时刷新战力排行redis数据
		int updatePowerRankPeriod = GameConstCfg.getInstance().getUpdatePowerRankPeriod();
		powerChangeMap = new ConcurrentHashMap<>();
		noArmyPowerChangeMap = new ConcurrentHashMap<>();
		addTickable(new HawkPeriodTickable(updatePowerRankPeriod, updatePowerRankPeriod) {
			@Override
			public void onPeriodTick() {
				HawkTaskManager.getInstance().postExtraTask(new HawkTask() {
					@Override
					public Object run() {
						updateRedisPowerRank();
						return null;
					}
				}, 0);
			}
		});
		
		initGuardRank();
		
		return true;
	}

	
	private void initGuardRank() {
		this.guardRankObject = new GuardRankObject();
		this.guardRankObject.init();
		this.addTickable(new HawkPeriodTickable(GuardianConstConfig.getInstance().getPerioTime(), 
				GuardianConstConfig.getInstance().getPerioTime()) {
			
			@Override
			public void onPeriodTick() {
				guardRankObject.refreshRank();
				
			}
		});
		
	}

	/**
	 * 刷新战力排行redis存储
	 */
	public void updateRedisPowerRank() {
		RankType playerRankType = RankType.PLAYER_FIGHT_RANK;
		Map<String, Double> opMap = powerChangeMap;
		powerChangeMap = new ConcurrentHashMap<>();
		if (!opMap.isEmpty()) {
			LocalRedis.getInstance().updateRankScore(playerRankType, opMap);
		}
		
		// 刷新去兵战力排行
		RankType noArmyRankType = RankType.PLAYER_NOARMY_POWER_RANK;
		Map<String, Double> noArmyMap = noArmyPowerChangeMap;
		noArmyPowerChangeMap = new ConcurrentHashMap<>();
		if (!noArmyMap.isEmpty()) {
			LocalRedis.getInstance().updateRankScore(noArmyRankType, noArmyMap);
		}
		
		RankType guildRankType = RankType.ALLIANCE_FIGHT_KEY;
		Map<String, Double> needUpdateMap = new HashMap<>();
		List<String> guildIds = GuildService.getInstance().getGuildIds();
		for(String guildId : guildIds){
			// 若该联盟排行被封禁,则不进行排行刷新
			if(RankService.getInstance().isBan(guildId, guildRankType)){
				continue;
			}
			double power = GuildService.getInstance().getGuildBattlePoint(guildId);
			GuildInfoObject guildInfo = GuildService.getInstance().getGuildInfoObject(guildId);
			long lastPower = guildInfo.getLastRankPower();
			if(lastPower != power){
				needUpdateMap.put(guildId, power);
			}
		}
		if(needUpdateMap.size() > 0){
			LocalRedis.getInstance().updateRankScore(guildRankType, needUpdateMap);
		}
	}

	/**
	 * 加载排行榜数据
	 * 
	 * @return
	 */
	private boolean loadRankObjects() {
		ConfigIterator<RankCfg> rankCfgs = HawkConfigManager.getInstance().getConfigIterator(RankCfg.class);
		for (RankCfg cfg : rankCfgs) {
			final RankType rankType = RankType.valueOf(cfg.getRankId());
			try {
				if (!loadAndRefreshRank(rankType)) {
					HawkLog.errPrintln("load rank failed, rankType: {}", rankType);
				} else {

					// 合服后处理:基地/指挥官等级榜单如果为空,从db读取数据进行初始化
					// 从DB中加载数据,初始化大本等级排行
					if (rankType.equals(RankType.PLAYER_CASTLE_KEY) && getRankCache(rankType).isEmpty()) {
						loadCityLvlRank();
					}
					// 从DB中加载数据,初始化指挥官等级排行
					else if (rankType.equals(RankType.PLAYER_GRADE_KEY) && getRankCache(rankType).isEmpty()) {
						loadPlayerLvlRank();
					}

					HawkLog.logPrintln("load rank success, rankType: {}, rankCount: {}", rankType, rankObjects.get(rankType).getSortedCount());
					addTickable(new HawkPeriodTickable(cfg.getPeriod(), cfg.getPeriod()) {
						@Override
						public void onPeriodTick() {
							HawkTaskManager.getInstance().postExtraTask(new HawkTask() {
								@Override
								public Object run() {
									loadAndRefreshRank(rankType);
									return null;
								}
							}, 0);
						}
					});
					
					
				}
			} catch (Exception e) {
				HawkException.catchException(e);
				return false;
			}
		}
				
		return true;
	}
	
	/**
	 * 当排行榜被清除后,起服时加载db中大本数据,初始化排行
	 */
	public void loadCityLvlRank() {
		RankType type = RankType.PLAYER_CASTLE_KEY;
		RankCfg cfg = HawkConfigManager.getInstance().getConfigByKey(RankCfg.class, type.getNumber());
		List<BuildingBaseEntity> citys = HawkDBManager.getInstance().executeQuery(
				"select * from building where type = " + BuildingType.CONSTRUCTION_FACTORY_VALUE + " order by buildingCfgId desc, lastUpgradeTime limit " + cfg.getMaxCount(),
				BuildingBaseEntity.class);
		Map<String, Double> scoreMap = new HashMap<>();
		for (BuildingBaseEntity city : citys) {
			BuildingCfg buildingCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, city.getBuildingCfgId());
			if (buildingCfg == null) {
				HawkLog.logPrintln("loadCityLvlRank error, playerId: {}, cfgId: {}, rankScore: {}, id: {}, type: {}", city.getPlayerId(), city.getBuildingCfgId(), city.getId(), city.getType());
				continue;
			}
			// 刷新建筑工厂等级排行榜
			// 荣耀建筑等级处理
			long rankScore = buildingCfg.getLevel();
			int honor = buildingCfg.getHonor();
			int progress = buildingCfg.getProgress();
			// 当前大本建筑进行了荣耀升级
			if (honor > 0 || progress > 0) {
				rankScore = buildingCfg.getLevel() * RankService.HONOR_CITY_OFFSET + progress;
			}
			long updateTimeScond = city.getLastUpgradeTime() / 1000;
			long value = updateTimeScond - RankScoreHelper.rankSpecialSeconds;
			double calcScore = Double.valueOf(rankScore + "" + (RankScoreHelper.rankSpecialOffset - value));
			scoreMap.put(city.getPlayerId(), calcScore);
			HawkLog.logPrintln("loadCityLvlRank ,playerId: {}, cfgId: {}, rankScore: {}, updateTimeScond: {}, redisScore: {}", city.getPlayerId(), city.getBuildingCfgId(),
					rankScore, updateTimeScond, calcScore);
		}
		if (!scoreMap.isEmpty()) {
			LocalRedis.getInstance().updateRankScore(type, scoreMap);
		}
	}
	
	/**
	 * 当排行榜被清除后,起服时加载db中玩家等级数据,初始化排行
	 */
	public void loadPlayerLvlRank() {
		RankType type = RankType.PLAYER_GRADE_KEY;
		RankCfg cfg = HawkConfigManager.getInstance().getConfigByKey(RankCfg.class, type.getNumber());
		List<PlayerBaseEntity> playerBases = HawkDBManager.getInstance().executeQuery("select * from player_base order by level desc, exp desc limit "+cfg.getMaxCount(), PlayerBaseEntity.class);
		Map<String, Double> scoreMap = new HashMap<>();
		long now = HawkTime.getSeconds();
		int offset = cfg.getMaxCount();
		for (PlayerBaseEntity playerBase : playerBases) {
			int level = playerBase.getLevel();
			long calcTime = getPlayerLevelUpTime(playerBase, now, offset);
			long value = calcTime - RankScoreHelper.rankSpecialSeconds;
			double calcScore = Double.valueOf(level + "" + (RankScoreHelper.rankSpecialOffset - value));
			scoreMap.put(playerBase.getPlayerId(), calcScore);
			HawkLog.logPrintln("loadPlayerLvlRank, playerId: {}, level: {}, calcTime: {}, redisScore: {}", playerBase.getPlayerId(), playerBase.getLevel(), calcTime, calcScore);
		}
		if (!scoreMap.isEmpty()) {
			LocalRedis.getInstance().updateRankScore(type, scoreMap);
		}
	}
	
	/**
	 * 获取指挥官等级升级时间
	 * @param playerBase
	 * @param second
	 * @param offset
	 * @return
	 */
	private long getPlayerLevelUpTime(PlayerBaseEntity playerBase, long second, int offset) {
		long calcTime = playerBase.getLevelUpTime();
		if (calcTime <= 0 && playerBase.getLevel() >= 58) {
			String timeStr = RedisProxy.getInstance().getRedisSession().hGet(RedisKey.PLAYER_LEVELUP_TIME, playerBase.getPlayerId());
			if (!HawkOSOperator.isEmptyString(timeStr)) {
				calcTime = Long.parseLong(timeStr);
				playerBase.setLevelUpTime(calcTime);
			}
		}
		if (calcTime <= 0) {
			calcTime = second - offset;
		} else {
			calcTime = calcTime/1000;
		}
		return calcTime;
	}

	/**
	 * 根据id获取排行榜数据
	 * 
	 * @param rankId
	 * @return
	 */
	private boolean loadAndRefreshRank(RankType rankType) {
		RankCfg cfg = HawkConfigManager.getInstance().getConfigByKey(RankCfg.class, rankType.getNumber());
		// 先判断榜单id对象是否存在, 不存在即创建
		if (!rankObjects.containsKey(rankType)) {
			RankObject rankObject = new RankObject(rankType);
			rankObjects.put(rankType, rankObject);
		}
		
		Set<Tuple> rankSet = LocalRedis.getInstance().getRankList(rankType, cfg.getMaxCount());
		rankObjects.get(rankType).updateRank(rankSet, cfg);
		return true;
	}

	/**
	 * 获取排行榜缓存
	 * 
	 * @param rankType
	 * @return
	 */
	public List<RankInfo> getRankCache(RankType rankType) {
		List<RankInfo> rankInfoList = new ArrayList<>();
		if (rankObjects.containsKey(rankType)) {
			rankInfoList = rankObjects.get(rankType).getSortedRank();
		}
		return rankInfoList;
	}

	public List<RankInfo> getRankCache(RankType rankType, int limit) {
		List<RankInfo> retList = new ArrayList<>();
		if (rankObjects.containsKey(rankType)) {
			List<RankInfo> rankInfoList = rankObjects.get(rankType).getSortedRank();
			for (RankInfo rankInfo : rankInfoList) {
				if (rankInfo.getRank() <= limit) {
					retList.add(rankInfo);
				}
			}
		}
		return retList;
	}
	
	/**
	 * 获得各项排行榜首信息
	 * 
	 * @return
	 */
	public List<RankInfo> getTopRankInfos() {
		List<RankInfo> topList = new ArrayList<>();
		for (RankType rankType : RankType.values()) {
			List<RankInfo> rankInfoList = getRankCache(rankType);
			if (!rankInfoList.isEmpty()) {
				topList.add(rankInfoList.get(0));
			}
		}
		return topList;
	}

	/**
	 * 获取指定id的排行信息
	 * 
	 * @param rankType
	 * @param rankKey
	 * @return HawkTuple2<Integer, Long> <排名,积分>
	 */
	public HawkTuple2<Integer, Long> getRankTuple(RankType rankType, String rankKey, Player player) {
		// 榜单信息不存在
		if (!rankObjects.containsKey(rankType)) {
			return new HawkTuple2<Integer, Long>(0, 0L);
		}

		if (HawkOSOperator.isEmptyString(rankKey)) {
			return new HawkTuple2<Integer, Long>(0, 0L);
		}

		HawkTuple2<Integer, Long> rankData = rankObjects.get(rankType).getRankTuple(rankKey);
		if (rankData != null) {
			return new HawkTuple2<Integer, Long>(rankData.first, getSelfScore(player, rankType));
		}
		return new HawkTuple2<Integer, Long>(0, getSelfScore(player, rankType));
	}
	
	/**
	 * 获取指定玩家/联盟排名信息
	 * @param rankType
	 * @param rankKey
	 * @return
	 */
	public RankInfo getRankInfo(RankType rankType, String rankKey) {
		RankInfo rankInfo = rankObjects.get(rankType).getRankInfo(rankKey);
		return rankInfo;
	}
	
	/**
	 * 获取本人/本联盟排行积分
	 * @param player
	 * @param rankType
	 * @return
	 */
	private long getSelfScore(Player player, RankType rankType) {
		switch (rankType) {
		case PLAYER_FIGHT_RANK:
			return player.getPower();
		case PLAYER_KILL_ENEMY_RANK:
			return player.getData().getStatisticsEntity().getArmyKillCnt();
		case PLAYER_CASTLE_KEY:
			BuildingCfg buildingCfg = player.getData().getBuildingCfgByType(Const.BuildingType.CONSTRUCTION_FACTORY);
			if (buildingCfg == null) {
				return 0;
			}
			int rankScore = buildingCfg.getLevel();
			int honor = buildingCfg.getHonor();
			int progress = buildingCfg.getProgress();
			// 当前大本建筑进行了荣耀升级
			if (honor > 0 || progress > 0) {
				rankScore = buildingCfg.getLevel() * RankService.HONOR_CITY_OFFSET + progress;
			}
			return rankScore;
		case PLAYER_GRADE_KEY:
			return player.getLevel();
		case ALLIANCE_FIGHT_KEY:
			return GuildService.getInstance().getGuildBattlePoint(player.getGuildId());
		case ALLIANCE_KILL_ENEMY_KEY:
			return GuildService.getInstance().getGuildKillCount(player.getGuildId());
		case PLAYER_NOARMY_POWER_RANK:
			PowerElectric powerElectric = player.getData().getPowerElectric();
			long totalPoint = powerElectric.getPowerData().getTotalPoint();
			long armyPoint = powerElectric.getPowerData().getArmyBattlePoint();
			int trapPoint = powerElectric.getPowerData().getTrapBattlePoint();
			int noArmyPoint = (int) Math.max(totalPoint - armyPoint - trapPoint, 0);
			return noArmyPoint;
		default:
			break;
		}
		
		return 0;
	}
	
	/**
	 * 获取真实排行积分
	 * 
	 * @param score
	 * @return
	 */
	public long getRealScore(RankType rankType, long rankScore) {
		long scoreVal = rankScore;
		if (isSpecialRankType(rankType)) {
			scoreVal = RankScoreHelper.getRealScore(rankScore);
		}
		// 领主，基地和自身等级 默认为1
		if (scoreVal == 0 && (RankType.PLAYER_CASTLE_KEY.equals(rankType) || RankType.PLAYER_GRADE_KEY.equals(rankType))) {
			return 1;
		}
		return scoreVal;
	}
	
	public RankObject getRankObject(RankType rankType) {
		return rankObjects.get(rankType);
	}

	/**
	 * 是否是积分特殊处理的排行类型
	 * 
	 * @param rankType
	 * @return
	 */
	private boolean isSpecialRankType(RankType rankType) {
		return RankType.PLAYER_CASTLE_KEY.equals(rankType) || RankType.PLAYER_GRADE_KEY.equals(rankType);
	}

	/**
	 * 是否是联盟类型排行榜
	 * 
	 * @param rankType
	 * @return
	 */
	public boolean isGuildTypeRank(RankType rankType) {
		return RankType.ALLIANCE_FIGHT_KEY.equals(rankType) || RankType.ALLIANCE_KILL_ENEMY_KEY.equals(rankType);
	}

	/**
	 * 刷新排行榜数据
	 * 
	 * @param rankType
	 *            排行类型Const.RankType.xxx
	 * @param score
	 *            排行积分
	 * @param key
	 *            唯一标示
	 */
	public void updateRankScore(RankType rankType, long score, String key) {
		if(score == 0){
			return;
		}
		// 特定排行需要进行时间参数处理
		if (isSpecialRankType(rankType)) {
			score = RankScoreHelper.calcSpecialRankScore(score);
		}
		
		LocalRedis.getInstance().updateRankScore(rankType, score, key);
	}
	
	/**
	 * 刷新玩家战力
	 * @param playerId
	 * @param power
	 */
	public void updatePlayerPower(String playerId, double power){
		
		// NPC玩家不参与
		if(GameUtil.isNpcPlayer(playerId)){
			return;
		}
		
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		
		// 军演状态中的玩家不进行刷新
		if (player.isInDungeonMap()) {
			return; 
		}
		
		// 跨服玩家不参与
		if (player != null && player.isCsPlayer()) {
			return ;
		}
		powerChangeMap.put(playerId, power);
	}
	
	/**
	 * 刷新玩家去兵战力
	 * @param playerId
	 * @param power
	 */
	public void updatePlayerNoArmyPower(String playerId, double power){
		
		// NPC玩家不参与
		if(GameUtil.isNpcPlayer(playerId)){
			return;
		}
		
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		
		// 军演状态中的玩家不进行刷新
		if (player.isInDungeonMap()) {
			return; 
		}
		
		// 跨服玩家不参与
		if (player != null && player.isCsPlayer()) {
			return ;
		}
		
		noArmyPowerChangeMap.put(playerId, power);
	}
	
	/**
	 * 封禁排行到期检测
	 * 
	 * @param rankType
	 */
	private boolean checkBanInfo(RankType rankType) {
		boolean hasChange = false;
		Map<String, Long> banMap = banMaps.get(rankType);
		if (banMap == null || banMap.size() == 0) {
			return hasChange;
		}
		
		Iterator<Entry<String, Long>> it = banMap.entrySet().iterator();
		long now = HawkTime.getMillisecond();
		while (it.hasNext()) {
			Entry<String, Long> entry = it.next();
			if (entry.getValue() > now) {
				continue;
			} else {
				GlobalData.getInstance().removeBanRankInfo(entry.getKey(), rankType.name().toLowerCase());
				addToRank(rankType, entry.getKey());
				it.remove();
				hasChange = true;
			}
		}
		
		return hasChange;
	}

	/**
	 * 将封禁解除的玩家/联盟加入排行
	 * 
	 * @param rankType
	 * @param key
	 */
	private void addToRank(RankType rankType, String key) {
		long score = 0;
		boolean isPlayer = true;
		try {
			switch (rankType) {
			case PLAYER_KILL_ENEMY_RANK: {
				Player player = GlobalData.getInstance().makesurePlayer(key);
				score = player.getData().getStatisticsEntity().getArmyKillCnt();
				break;
			}
			case ALLIANCE_KILL_ENEMY_KEY: {
				score = GuildService.getInstance().getGuildKillCount(key);
				isPlayer = false;
				break;
			}
			case PLAYER_FIGHT_RANK: {
				Player player = GlobalData.getInstance().makesurePlayer(key);
				score = player.getPower();
				break;
			}
			case PLAYER_CASTLE_KEY: {
				Player player = GlobalData.getInstance().makesurePlayer(key);
				score = player.getCityLv();
				break;
			}
			case PLAYER_GRADE_KEY: {
				Player player = GlobalData.getInstance().makesurePlayer(key);
				score = player.getLevel();
				break;
			}
			
			case ALLIANCE_FIGHT_KEY: {
				score = GuildService.getInstance().getGuildBattlePoint(key);
				isPlayer = false;
				break;
			}
			case PLAYER_NOARMY_POWER_RANK: {
				Player player = GlobalData.getInstance().makesurePlayer(key);
				PowerElectric powerElectric = player.getData().getPowerElectric();
				long totalPoint = powerElectric.getPowerData().getTotalPoint();
				long armyPoint = powerElectric.getPowerData().getArmyBattlePoint();
				int trapPoint = powerElectric.getPowerData().getTrapBattlePoint();
				int noArmyPoint = (int) Math.max(totalPoint - armyPoint - trapPoint, 0);
				score = noArmyPoint;
				break;
			}
			
			default:
				isPlayer = false;
				break;
			}
		} catch (Exception e) {
			HawkException.catchException(e, rankType, key);
		}
		if (isPlayer) {
			//如果是跨服玩家的不参与排行.
			Player player = GlobalData.getInstance().makesurePlayer(key);
			if (player.isCsPlayer()) {
				return;
			}
		}
		updateRankScore(rankType, score, key);
	}

	/**
	 * 加载封禁榜单
	 */
	private void loadBanMaps() {
		// 个人排行榜
		for(RankType rankType : GsConst.PERSONAL_RANK_TYPE) {
			banMaps.put(rankType, loadBanMap(rankType));
		}
		
		// 联盟排行榜
		for (RankType rankType : GsConst.GUILD_RANK_TYPE) {
			banMaps.put(rankType, loadBanMap(rankType));
		}
	}

	/**
	 * 加载指定类型的封禁榜单
	 * 
	 * @param type
	 * @return
	 */
	private Map<String, Long> loadBanMap(RankType rankType) {
		String banType = rankType.name().toLowerCase();
		Map<String, String> playerBanMap = RedisProxy.getInstance().getBanRankInfoMap(banType);
		Map<String, Long> map = new ConcurrentHashMap<String, Long>();
		
		for (Entry<String, String> entry : playerBanMap.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();
			String[] banInfo = value.split(":");
			map.put(key, Long.parseLong(banInfo[1]));
			GlobalData.getInstance().addBanRankInfo(key, banType, value, 0);
		}
		
		return map;
	}

	/**
	 * 封禁参与排行
	 * 
	 * @param playerId
	 * @param rankType
	 * @param forbidTime
	 * @param isZero
	 * @param player
	 */
	public void banJoinRank(String playerId, RankType rankType, long forbidTime, boolean isZero, Player player) {
		LocalRedis.getInstance().removeFromRank(rankType, playerId);
		// 清零玩家击杀
		if (isZero && RankType.PLAYER_KILL_ENEMY_RANK.equals(rankType)) {
			player.getData().getStatisticsEntity().setArmyKillCnt(0);
			GameUtil.scoreBatch(player, ScoreType.KILL_ENEMY, 0);
		}
		
		if (!banMaps.containsKey(rankType)) {
			banMaps.put(rankType, new ConcurrentHashMap<>());
		}
		
		banMaps.get(rankType).put(playerId, forbidTime);
		// 刷新榜单信息
		loadAndRefreshRank(rankType);
	}

	/**
	 * 将指定玩家/联盟从封禁对象中移除
	 * 
	 * @param rankType
	 * @param targetId
	 */
	public void removeFromBan(RankType rankType, String targetId) {
		if (banMaps.containsKey(rankType)) {
			banMaps.get(rankType).remove(targetId);
		}
		
		GlobalData.getInstance().removeBanRankInfo(targetId, rankType.name().toLowerCase());
		addToRank(rankType, targetId);
		// 刷新榜单信息
		loadAndRefreshRank(rankType);
	}
	
	/**
	 * 判断特定榜单是否被封禁
	 * @return
	 */
	public boolean isBan(String targetId, RankType rankType) {
		String banInfo = GlobalData.getInstance().getBanRankInfo(targetId, rankType.name().toLowerCase());
		return !HawkOSOperator.isEmptyString(banInfo);
	}
	
	/**
	 * 判断是否有被封禁的个人榜单（联盟榜单除外）
	 * @param targetId
	 * @return
	 */
	public boolean isBan(String targetId) {
		for(RankType rankType : GsConst.PERSONAL_RANK_TYPE) {
			if (isBan(targetId, rankType)) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * 刷新排行榜
	 * @param rankType
	 */
	public void refreshRank(RankType rankType) {
		loadAndRefreshRank(rankType);
	}
	
	
	/**
	 * 发送禁止参与排行榜消息
	 * 
	 * @param player
	 * @param banEndTime
	 */
	public void sendBanRankNotice(Player player, long banEndTime, String msg) {
		if (banEndTime <= HawkTime.getMillisecond()) {
			player.sendIdipNotice(NoticeType.BAN_MSG, NoticeMode.MSG_BOX, 0, IdipMsgCode.IDIP_BAN_JOIN_RANK_RELEASE_VALUE);
		} else {
			player.sendIdipNotice(NoticeType.BAN_MSG, NoticeMode.NOTICE_MSG, banEndTime, msg);
		}
	}
	
	/**
	 * 刷新建筑工厂等级排行榜
	 */
	public void checkCityLvlRank(Player player) {
		try {
			BuildingCfg buildingCfg = player.getData().getBuildingCfgByType(BuildingType.CONSTRUCTION_FACTORY);
			if (buildingCfg != null) {
				// 荣耀建筑等级处理
				int rankScore = buildingCfg.getLevel();
				int honor = buildingCfg.getHonor();
				int progress = buildingCfg.getProgress();
				// 当前大本建筑进行了荣耀升级
				if (honor > 0 || progress > 0) {
					rankScore = buildingCfg.getLevel() * RankService.HONOR_CITY_OFFSET + progress;
				}
				long redisScore = LocalRedis.getInstance().getPlayerRankScore(player.getId(), RankType.PLAYER_CASTLE_KEY);
				long redisRealScore = RankService.getInstance().getRealScore(RankType.PLAYER_CASTLE_KEY, redisScore);
				// 玩家当前基地等级高于redis排行榜记录的数据,则刷新玩家基地等级排行榜
				if (rankScore > redisRealScore) {
					player.updateRankScore(MsgId.CITY_LEVEL_RANK_REFRESH, RankType.PLAYER_CASTLE_KEY, rankScore);
				}
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	
	/**
	 * 刷新指挥官等级排行榜
	 */
	public void checkPlayerLvlRank(Player player) {
		try {
			int rankScore = player.getLevel();
			long redisScore = LocalRedis.getInstance().getPlayerRankScore(player.getId(), RankType.PLAYER_GRADE_KEY);
			long redisRealScore = RankService.getInstance().getRealScore(RankType.PLAYER_GRADE_KEY, redisScore);
			// 玩家当前基地等级高于redis排行榜记录的数据,则刷新玩家基地等级排行榜
			if (rankScore > redisRealScore) {
				player.updateRankScore(MsgId.PLAYER_LEVELUP_RANK_REFRESH, RankType.PLAYER_GRADE_KEY, rankScore);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	public void addGuardRank(String playerId1, String playerId2, int guardValue, long operationTime) {
		this.dealMsg(GameConst.MsgId.GUARD_ADD_RANK, new GuardRankAddInvoker(playerId1, playerId2, guardValue, operationTime));
	} 
	
	public void deleteGuardRank(String playerId1, String playerId2) {
		this.dealMsg(GameConst.MsgId.GUARD_DELETE_RANK, new GuardRankDeleteInvoker(playerId1, playerId2));
	}

	public GuardRankObject getGuardRankObject() {
		return guardRankObject;
	}
	
	/**
	 * 获取其自己能在其它服去兵战力排行能排多少
	 * @param serverId
	 */
	public int getOtherServerNoArmyPowerRank(String serverId, long ownPower) {
		int rank = 1;
		Set<Tuple> otherServerRank = getOtherServerRank(serverId, RankType.PLAYER_NOARMY_POWER_RANK, 100);
		for (Tuple rankInfo : otherServerRank) {
			if (ownPower >= rankInfo.getScore()) {
				break;
			}
			rank++;
		}
		return rank;
	}
	
	/**
	 * 获取其它服排行榜
	 * @param serverId
	 * @param rankType
	 * @param maxCount
	 * @return
	 */
	public Set<Tuple> getOtherServerRank(String serverId, RankType rankType, int maxCount) {
		String serverIdentify = RedisProxy.getInstance().getServerIdentify(serverId);
		String rankString = RedisProxy.RANK_KEYS[rankType.getNumber()];
		String rankKey = serverId + ":" + serverIdentify + ":" +rankString;
		Set<Tuple> rankSet = RedisProxy.getInstance().getRankList(rankKey, maxCount);
		return rankSet;
	}
	
	/**
	 * 获取排行榜内最全成员
	 * @param rankType
	 * @return
	 */
	public Map<String, HawkTuple2<Integer, Long>> getRankDataMapCache(RankType rankType) {
		if (rankObjects.containsKey(rankType)) {
			return rankObjects.get(rankType).getRankDataMap();
		}
		return null;
	}
}
