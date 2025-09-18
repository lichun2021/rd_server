package com.hawk.game.world.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.hawk.app.HawkApp;
import org.hawk.app.HawkAppObj;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.obj.HawkObjBase;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkRand;
import org.hawk.os.HawkTime;
import org.hawk.uuid.HawkUUIDGenerator;
import org.hawk.xid.HawkXID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.hawk.common.AccountRoleInfo;
import com.hawk.game.GsApp;
import com.hawk.game.GsConfig;
import com.hawk.game.battle.BattleService;
import com.hawk.game.config.WorldMapConstProperty;
import com.hawk.game.config.WorldRobotCfg;
import com.hawk.game.config.WorldRobotConstProperty;
import com.hawk.game.global.LocalRedis;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Rank.RankType;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.object.AreaObject;
import com.hawk.game.world.object.Point;
import com.hawk.game.world.robot.WORPlayer;
import com.hawk.game.world.thread.WorldTask;
import com.hawk.game.world.thread.WorldThreadScheduler;

import redis.clients.jedis.Tuple;

/**
 * 机器人
 * 
 * @author golden
 *
 */
public class WorldRobotService extends HawkAppObj {

	/**
	 * 生成机器人随机次数
	 */
	public static final int RANDTIMES = 100;

	/**
	 * 机器人playerId前缀
	 */
	public static final String ROBOT_PRE = BattleService.NPC_ID + "500DS-";

	/**
	 * 
	 */
	private Cache<String, WORPlayer> cache = CacheBuilder.newBuilder().recordStats().expireAfterAccess(12, TimeUnit.HOURS).build();

	/**
	 * 世界上机器人
	 */
	public Map<Integer, WorldPoint> robots;

	/**
	 * 上次tick时间
	 */
	public long lastTickTime;

	/**
	 * 排名列表
	 */
	public List<String> rankList;
	
	/**
	 * 日志对象
	 */
	public static Logger logger = LoggerFactory.getLogger("Server");

	/**
	 * 单例对象
	 */
	private static WorldRobotService instance = null;

	/**
	 * 获取单例对象
	 */
	public static WorldRobotService getInstance() {
		return instance;
	}

	/**
	 * 对象构造函数
	 */
	public WorldRobotService(HawkXID xid) {
		super(xid);
		instance = this;
	}

	/**
	 * 初始化
	 */
	public boolean init() {
		robots = new ConcurrentHashMap<>();
		return true;
	}

	@Override
	public boolean onTick() {

		if (!GsApp.getInstance().isInitOK()) {
			return true;
		}

		long tickPeroid = WorldRobotConstProperty.getInstance().getTickPeroid();
		if (HawkTime.getMillisecond() - lastTickTime < tickPeroid) {
			return true;
		}

		lastTickTime = HawkTime.getMillisecond();

		WorldThreadScheduler.getInstance().postWorldTask(new WorldTask(GsConst.WorldTaskType.REFRESH_ROBOT) {

			@Override
			public boolean onInvoke() {

				// 检测机器人移除
				checkRobotRemove();

				// 刷新机器人
				refreshRobot();

				return true;
			}
		});

		return true;
	}

	/**
	 * 获取机器人玩家
	 */
	public Player makesurePlayer(String playerId) {
		return cache.getIfPresent(playerId);
	}

	/**
	 * 是否是刷新机器人的周期
	 */
	public boolean isGenRobotPeriod() {
		return WorldRobotConstProperty.getInstance().isOpen();
	}

	/**
	 * 世界上机器人数量
	 */
	public int currentRobotCount() {
		return robots.size();
	}

	/**
	 * 刷新机器人
	 */
	public void refreshRobot() {
		// 不是生成机器人的周期
		if (!isGenRobotPeriod()) {
			logger.info("refreshRobot, not gen robot period...");
			return;
		}

		// 当前已经达到最大值，不需要刷
		int maxCount = getRefreshMaxCount();
		if (currentRobotCount() >= maxCount) {
			logger.info("refreshRobot, current:{}, refreshCount:{}", currentRobotCount(), maxCount);
			return;
		}

		int refreshCount = maxCount - currentRobotCount();

		// 加载下排行榜
		if (refreshCount > 0) {
			loadRankList();
		}
		
		for (int i = 0; i < refreshCount; i++) {

			try {
				createRobotPoint();
			} catch (Exception e) {
				HawkException.catchException(e);
			}

		}

		logger.info("refreshRobot, refresh:{}, current:{}", refreshCount, currentRobotCount());
	}

	/**
	 * 检测机器人移除
	 */
	public void checkRobotRemove() {

		// 移除过期机器人
		List<WorldPoint> rmRobots = new ArrayList<>();

		long lifeTime = WorldRobotConstProperty.getInstance().getLifeTime();

		for (WorldPoint robot : robots.values()) {

			if (HawkTime.getMillisecond() - robot.getLifeStartTime() < lifeTime) {
				continue;
			}

			rmRobots.add(robot);
		}

		logger.info("checkRobotRemove, count:{}", rmRobots.size());

		for (WorldPoint point : rmRobots) {
			WorldPointService.getInstance().removeWorldPoint(point.getId(), false);
		}
	}

	/**
	 * 通知机器人移除
	 */
	public void notifyRobotRemove(int pointId, String playerId) {
		robots.remove(pointId);
		int[] pos = GameUtil.splitXAndY(pointId);
		
		if (!HawkOSOperator.isEmptyString(playerId)) {
			HawkXID robotXid = HawkXID.valueOf(GsConst.ObjType.PLAYER, playerId);
			HawkApp.getInstance().removeObj(robotXid);
		}
		
		logger.info("notifyRobotRemove, posX:{}, posY:{}, currentCount:{}", pos[0], pos[1], robots.size());
	}

	/**
	 * 生成机器人世界点
	 */
	public WorldPoint createRobotPoint() {

		// 复制哪个范围排行榜内的玩家
		int genLevel = getGenLevel();

		// 生成机器人起始终止区域
		int maxPosX = WorldMapConstProperty.getInstance().getWorldMaxX();
		int maxPosY = WorldMapConstProperty.getInstance().getWorldMaxY();

		int randTimes = 0;

		while (true) {

			randTimes++;

			// 找点最多找100次
			if (randTimes > RANDTIMES) {
				break;
			}

			int randX = HawkRand.randInt(0, maxPosX);
			int randY = HawkRand.randInt(0, maxPosY);

			Point point = WorldPointService.getInstance().getAreaPoint(randX, randY, true);
			if (point == null) {
				continue;
			}

			AreaObject area = WorldPointService.getInstance().getArea(point.getAreaId());

			// 半径为2的点是否能在此坐标落座
			if (!point.canYuriSeat()) {
				continue;
			}

			// 不能在黑土地区域生成
			if (WorldPointService.getInstance().isInCapitalArea(point.getId())) {
				continue;
			}

			// 周边是否有占用点等
			if (!WorldPointService.getInstance().tryOccupied(area, point, GsConst.PLAYER_POINT_RADIUS)) {
				continue;
			}

			// 是否已经被占用
			if (WorldPointService.getInstance().getWorldPoint(point.getId()) != null) {
				continue;
			}

			// 复制的源playerId
			String sourcePlayerId = randSourcePlayerId(genLevel);

			// 没有随机到，证明排行榜没有这么多玩家
			if (HawkOSOperator.isEmptyString(sourcePlayerId)) {
				logger.info("createRobot, haveNoSourcePlayerId genLevel:{}", genLevel);
				break;
			}

			Player robot = createRobotPlayer(sourcePlayerId, point.getId());

			// 创建世界点对象
			WorldPoint worldPoint = new WorldPoint(point.getX(), point.getY(), point.getAreaId(), point.getZoneId(), WorldPointType.PLAYER_VALUE);
			worldPoint.setPlayerId(robot.getId());
			worldPoint.setPlayerName(robot.getName());
			worldPoint.setCityLevel(robot.getCityLevel());
			worldPoint.setPlayerIcon(robot.getIcon());
			worldPoint.setLifeStartTime(HawkTime.getMillisecond());
			worldPoint.setProtectedEndTime(0);
			worldPoint.setMonsterId(genLevel);
			WorldPointService.getInstance().addPoint(worldPoint);

			robots.put(worldPoint.getId(), worldPoint);

			logger.info("createRobot, x:{}, y:{}, areaId:{}, robotId:{}, genLevel:{}, sourcePlayerId:{}", point.getX(), point.getY(), point.getAreaId(), robot.getId(), genLevel,
					sourcePlayerId);

			return worldPoint;
		}

		return null;
	}

	/**
	 * 获取刷新最大数量
	 * @return
	 */
	public int getRefreshMaxCount() {
		int count = 0;
		ConfigIterator<WorldRobotCfg> cfgIterator = HawkConfigManager.getInstance().getConfigIterator(WorldRobotCfg.class);
		while (cfgIterator.hasNext()) {
			WorldRobotCfg cfg = cfgIterator.next();
			count += cfg.getCount();
		}
		return count;
	}

	/**
	 * 获取生成的等级(复制哪个范围排行榜内的玩家)
	 */
	public int getGenLevel() {
		Map<Integer, Integer> levelMap = new HashMap<>();
		for (WorldPoint point : robots.values()) {
			Integer count = levelMap.get(point.getMonsterId());
			if (count == null) {
				levelMap.put(point.getMonsterId(), 1);
				continue;
			}
			levelMap.put(point.getMonsterId(), count + 1);
		}

		ConfigIterator<WorldRobotCfg> cfgIterator = HawkConfigManager.getInstance().getConfigIterator(WorldRobotCfg.class);
		while (cfgIterator.hasNext()) {
			WorldRobotCfg cfg = cfgIterator.next();
			Integer currentCount = levelMap.get(cfg.getId());
			if (currentCount == null) {
				currentCount = 0;
			}
			if (currentCount < cfg.getCount()) {
				return cfg.getId();
			}
		}

		WorldRobotCfg cfg = HawkConfigManager.getInstance().getConfigByIndex(WorldRobotCfg.class, 0);
		return cfg.getId();
	}

	/**
	 * 随机源
	 */
	public String randSourcePlayerId(int genLevel) {
		WorldRobotCfg cfg = HawkConfigManager.getInstance().getConfigByKey(WorldRobotCfg.class, genLevel);
		if (cfg == null) {
			return null;
		}

		if (rankList == null || rankList.isEmpty()) {
			loadRankList();
		}

		if (cfg.getRankUp() > rankList.size()) {
			return null;
		}

		int up = Math.min(Math.max(0, cfg.getRankUp() - 1), rankList.size());

		int down = Math.min(cfg.getRankDown(), rankList.size());

		List<String> subList = rankList.subList(up, down);
		if (subList == null || subList.isEmpty()) {
			return null;
		}

		String playerId = subList.get(HawkRand.randInt(subList.size() - 1));
		if (HawkOSOperator.isEmptyString(playerId)) {
			return null;
		}

		return playerId;
	}

	/**
	 * 是否是机器人的玩家id
	 */
	public boolean isRobotId(String playerId) {
		if (HawkOSOperator.isEmptyString(playerId)) {
			return false;
		}
		return playerId.startsWith(ROBOT_PRE);
	}

	/**
	 * 创建机器人玩家 
	 */
	public Player createRobotPlayer(String sourcePlayerId, int pointId) {
		String robotId = WorldRobotService.ROBOT_PRE + HawkUUIDGenerator.genUUID();
		HawkXID roomXid = HawkXID.valueOf(GsConst.ObjType.PLAYER, robotId);
		
		long lifeTime = WorldRobotConstProperty.getInstance().getLifeTime();
		long visitTime = lifeTime + GsConst.HOUR_MILLI_SECONDS * 2L;
		
		HawkObjBase<HawkXID, HawkAppObj> obj = GsApp.getInstance().createObj(roomXid);
		obj.setVisitTime(HawkTime.getMillisecond() + visitTime);
		WORPlayer result = (WORPlayer) obj.getImpl();
		
		result.setPlayerPos(pointId);
		result.init(sourcePlayerId);
		cache.put(result.getId(), result);
		
		try { // 创建roleInfo
			AccountRoleInfo accountRoleInfo = AccountRoleInfo.newInstance().openId(result.getOpenId()).playerId(result.getId())
					.serverId(result.getServerId()).platform(result.getPlatform()).registerTime(result.getCreateTime());

			accountRoleInfo.playerName(result.getName()).playerLevel(result.getLevel()).cityLevel(result.getCityLevel())
					.vipLevel(result.getVipLevel()).battlePoint(result.getPower()).activeServer(GsConfig.getInstance().getServerId())
					.icon(result.getIcon()).loginWay(result.getEntity().getLoginWay()).loginTime(HawkTime.getMillisecond())
					.logoutTime(result.getLogoutTime());
			accountRoleInfo.pfIcon(result.getPfIcon());
			result.setAccountRoleInfo(accountRoleInfo);
		} catch (Exception e) {
			HawkException.catchException(e, result.getId());
		}
		
		return result;
	}

	public AccountRoleInfo getAccountRoleInfo(String playerId) {
		WORPlayer player = (WORPlayer) makesurePlayer(playerId);
		if(Objects.nonNull(player)){
			return player.getAccountRoleInfo();
		}
		return null;
	}

	/**
	 * 排名列表
	 */
	public void loadRankList() {
		List<String> rankList = new ArrayList<>();
		Set<Tuple> rankSet = LocalRedis.getInstance().getRankList(RankType.PLAYER_CASTLE_KEY, 1000);
		for (Tuple tuple : rankSet) {
			String playerId = tuple.getElement();
			if(GameUtil.isNpcPlayer(playerId)){
				continue;
			}
			rankList.add(tuple.getElement());
		}
		this.rankList = rankList;
	}
	
	public void invalidate(Player player) {
		cache.invalidate(player.getId());
	}
}
