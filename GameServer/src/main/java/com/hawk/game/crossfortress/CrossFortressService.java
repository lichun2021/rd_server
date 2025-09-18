package com.hawk.game.crossfortress;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingDeque;

import org.hawk.app.HawkAppObj;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.tickable.HawkPeriodTickable;
import org.hawk.xid.HawkXID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hawk.game.GsConfig;
import com.hawk.game.config.CrossFortressCfg;
import com.hawk.game.config.CrossFortressConstCfg;
import com.hawk.game.config.WorldEnemyCfg;
import com.hawk.game.crossactivity.CActivityInfo;
import com.hawk.game.crossactivity.CrossActivityService;
import com.hawk.game.crossactivity.CrossRankObject;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.LocalRedis;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.manager.AssembleDataManager;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.CrossActivity.CrossRankInfo;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.SuperWeapon.AllSuperWeaponInfo;
import com.hawk.game.protocol.SuperWeapon.SuperWeaponPeriod;
import com.hawk.game.protocol.SuperWeapon.SuperWeaponQuarterInfoResp;
import com.hawk.game.protocol.World.MonsterType;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.superweapon.SuperWeaponService;
import com.hawk.game.util.GameUtil;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.march.IWorldMarch;
import com.hawk.game.world.object.AreaObject;
import com.hawk.game.world.service.WorldFoggyFortressService;
import com.hawk.game.world.service.WorldMonsterService;
import com.hawk.game.world.service.WorldPlayerService;
import com.hawk.game.world.service.WorldPointService;
import com.hawk.game.world.service.WorldResourceService;
import com.hawk.game.world.service.WorldStrongPointService;
import com.hawk.game.world.thread.WorldThreadScheduler;

/**
 * 要塞(跨服战区)
 * @author golden
 *
 */
public class CrossFortressService extends HawkAppObj {

	/**
	 * 日志
	 */
	public static final Logger logger = LoggerFactory.getLogger("Server");

	/**
	 * 全局实例对象
	 */
	private static CrossFortressService instance = null;

	/**
	 * 期数
	 */
	public int turn;
	
	/**
	 * 要塞
	 */
	public Map<Integer, IFortress> allFortress;

	/**
	 * 占领信息
	 */
	public Map<String, FortressOccupyItem> occupyInfo;
	
	/**
	 * 上一次读取占领信息的时间
	 */
	public long lastReadOccupyInfoTime;
	
	/**
	 * 航海之星
	 */
	public Map<String, Integer> starMap;
	
	/**
	 * 航海之星记录
	 */
	public Map<Integer, FortressRecordItem> recordMap;
	
	/**
	 * 构造
	 */
	public CrossFortressService(HawkXID xid) {
		super(xid);
		instance = this;
	}

	/**
	 * 获取实例
	 */
	public static CrossFortressService getInstance() {
		return instance;
	}

	/**
	 * 初始化
	 */
	public boolean init() {

		// 期数
		turn = LocalRedis.getInstance().getFortressTurn();
		
		// 占领信息
		Integer serverGroup = AssembleDataManager.getInstance().getCrossServerCfgId(GsConfig.getInstance().getServerId());
		occupyInfo = RedisProxy.getInstance().getAllFortressOccupyInfo(serverGroup);
		
		// 上次读取redis占领信息时间
		lastReadOccupyInfoTime = HawkTime.getMillisecond();
		
		// 航海之星
		starMap = RedisProxy.getInstance().getAllFortressStar();
		
		// 航海之星记录
		recordMap = LocalRedis.getInstance().getAllFortressRecord();
		
		// 要塞
		allFortress = new HashMap<>();
		for (int[] pos : CrossFortressConstCfg.getInstance().getPosList()) {
			IFortress fotress = new IFortress(pos);
			allFortress.put(fotress.getPointId(), fotress);
		}
		
		long tickPeriod = CrossFortressConstCfg.getInstance().getTickPeriod();
		WorldThreadScheduler.getInstance().addWorldTickable(new HawkPeriodTickable(tickPeriod) {
			@Override
			public void onPeriodTick() {
				try {
					tick();
				} catch (Exception e) {
					HawkException.catchException(e);
				}
			}
		});

		// 检测错误点
		checkErrorPoint();
		
		return true;
	}

	/**
	 * tick
	 */
	public boolean tick() {

		int currentState = getCurrentState();
		
		switch (currentState) {

		// 和平阶段
		case SuperWeaponPeriod.PEACE_VALUE:
			
			break;

		// 战斗阶段
		case SuperWeaponPeriod.WARFARE_VALUE:
			for (IFortress fortress : allFortress.values()) {
				fortress.doWarfareTick();
			}
			break;

		default:
			break;

		}

		// 周期时间刷新占领信息
		tickOccupyInfo();
		
		return true;
	}

	/**
	 * 获取当前阶段
	 */
	public int getCurrentState() {
//		boolean fortressOpen = (CrossActivityService.getInstance().getFortressState() == CrossFortressState.OPEN); 
//		return fortressOpen ? SuperWeaponPeriod.WARFARE_VALUE : SuperWeaponPeriod.PEACE_VALUE;
		return SuperWeaponPeriod.PEACE_VALUE;
	}
	
	/**
	 * 获取要塞
	 */
	public IFortress getFortress(int pointId) {
		return allFortress.get(pointId);
	}
	
	/**
	 * 获取所有要塞
	 * @return 
	 */
	public Collection<IFortress> getAllFortress() {
		return allFortress.values();
	}
	
	/**
	 * 发送超级武器驻军信息
	 */
	public void sendCrossFortressQuarterInfo(Player player, IFortress fortress) {
		SuperWeaponQuarterInfoResp.Builder builder = SuperWeaponQuarterInfoResp.newBuilder();
		
		Player stayLeader = WorldMarchService.getInstance().getFortressLeader(fortress.getPointId());
		
		if (stayLeader == null || !player.hasGuild() || !player.getGuildId().equals(stayLeader.getGuildId())) {
			player.sendProtocol(HawkProtocol.valueOf(HP.code.CROSS_FORTRESS_QUARTER_INFO_S, builder));
			return;
		}
		
		BlockingDeque<String> marchs = WorldMarchService.getInstance().getFortressMarchs(fortress.getPointId());
		for (String marchId : marchs) {
			builder.addQuarterMarch(SuperWeaponService.getInstance().getSuperWeaponQuarterMarch(marchId));
		}
		
		String leaderId = WorldMarchService.getInstance().getFortressLeaderMarchId(fortress.getPointId());
		IWorldMarch leaderMarch = WorldMarchService.getInstance().getMarch(leaderId);
		int maxMassJoinSoldierNum = leaderMarch.getMaxMassJoinSoldierNum(leaderMarch.getPlayer());
		builder.setMassSoldierNum(maxMassJoinSoldierNum);
		
		player.sendProtocol(HawkProtocol.valueOf(HP.code.CROSS_FORTRESS_QUARTER_INFO_S, builder));
	}

	/**
	 * 广播航海要塞战信息
	 */
	public void broadcastFortressInfo(Player player) {
		try {
			AllSuperWeaponInfo.Builder builder = AllSuperWeaponInfo.newBuilder();
			builder.setTurnCount(0);
			
			builder.setPeriodType(getCurrentState());
			
			long periodEndTime = 0;
			if (getCurrentState() == SuperWeaponPeriod.WARFARE_VALUE) {
				CActivityInfo activityInfo = CrossActivityService.getInstance().getActivityInfo();
				periodEndTime = activityInfo.getEndTime();
			}
			builder.setPeriodEndTime(periodEndTime);
			
			for (IFortress fortress : allFortress.values()) {
				builder.addSuperWeaponInfo(fortress.genFortressInfoBuilder());
			}
			
			if (player != null) {
				HawkProtocol protocol = HawkProtocol.valueOf(HP.code.CROSS_FORTRESS_INFO_S, builder);
				player.sendProtocol(protocol);
			} else {
				for (Player sendPlayer : GlobalData.getInstance().getOnlinePlayers()) {
					HawkProtocol protocol = HawkProtocol.valueOf(HP.code.CROSS_FORTRESS_INFO_S, builder);
					sendPlayer.sendProtocol(protocol);
				}
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 周期时间刷新占领信息
	 */
	public void tickOccupyInfo() {
		long period = CrossFortressConstCfg.getInstance().getOccupyInfoRefPeriod();
		if (HawkTime.getMillisecond() - lastReadOccupyInfoTime < period) {
			return;
		}
		
		if (getCurrentState() == SuperWeaponPeriod.WARFARE_VALUE) {
			lastReadOccupyInfoTime = HawkTime.getMillisecond();
			Integer serverGroup = AssembleDataManager.getInstance().getCrossServerCfgId(GsConfig.getInstance().getServerId());
			occupyInfo =  RedisProxy.getInstance().getAllFortressOccupyInfo(serverGroup);
		}
		
		// 航海之星
		starMap = RedisProxy.getInstance().getAllFortressStar();
	}
	
	/**
	 * 获取当前所有占领信息
	 */
	public Map<String, FortressOccupyItem> getAllOccupyInfo() {
		if (getCurrentState() != SuperWeaponPeriod.WARFARE_VALUE) {
			return new HashMap<>();
		}
		
		// 是否实时读取占领信息: 如果实时读取就直接读redis，否则读occupyInfo里的数据
		if (CrossFortressConstCfg.getInstance().isOccupyInfoRT()) {
			Integer serverGroup = AssembleDataManager.getInstance().getCrossServerCfgId(GsConfig.getInstance().getServerId());
			return RedisProxy.getInstance().getAllFortressOccupyInfo(serverGroup);
		}
		
		return occupyInfo;
	}

	/**
	 * 修改占领信息
	 */
	public void addOccupyInfo(int pointId, Player leader) {
		
		// 当前服serverId
		String serverId = GsConfig.getInstance().getServerId();
		
		// 占领玩家所在serverId
		String occupyServerId = "";
		if (leader != null) {
			occupyServerId = leader.getMainServerId();
		}
		
		// 占领信息
		FortressOccupyItem info = new FortressOccupyItem(serverId, occupyServerId, pointId);
		
		// 修改占领信息
		occupyInfo.put(getOccupyInfoKey(serverId, pointId), info);
		
		RedisProxy.getInstance().updateCrossFortressOccupyCount(pointId, occupyServerId);
		
		// write to redis
		RedisProxy.getInstance().updateFortressOccupyInfo(AssembleDataManager.getInstance().getCrossServerCfgId(serverId), getOccupyInfoKey(serverId, pointId), info);
	}
	
	/**
	 * 清除占领信息
	 */
	public void clearOccupyInfo() {
		occupyInfo.clear();
		RedisProxy.getInstance().deleteFortressOccupyInfo(AssembleDataManager.getInstance().getCrossServerCfgId(GsConfig.getInstance().getServerId()));
	}
	
	/**
	 * 占领信息的key
	 */
	public String getOccupyInfoKey(String serverId, int pointId) {
		return serverId + "_" + pointId;
	}
	
	/**
	 * 活动开启
	 */
	public void onOpen() {
		try {
			if (allFortress == null) {
				return;
			}
			
			for (IFortress fortress : allFortress.values()) {
				fortress.doStateChange();
			}
			
			// 清除占领信息
			clearOccupyInfo();
			
			// 广播
			broadcastFortressInfo(null);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 活动结束
	 */
	public void onEnd(CrossRankObject serverRank) {
		try {
			if (allFortress == null) {
				return;
			}
			
			for (IFortress fortress : allFortress.values()) {
				fortress.doStateChange();
			}
			
			List<CrossRankInfo> rankInfos = serverRank.getRankInfos();
			for (CrossRankInfo rank : rankInfos) {
				// 获取航海之星数量
				int starCount = CrossFortressConstCfg.getInstance().getRankStar(rank.getRank());
				// 增加航海之星数量
				addFortressStar(rank.getServerId(), rank.getRank(), starCount);
				// 增加航海之星记录
				addFortressRecord(rank.getServerId(), turn, rank.getRank(), starCount);
			}
			
			// 清除占领信息
			clearOccupyInfo();
			
			// 增加期数
			addTurn();
			
			// 广播
			broadcastFortressInfo(null);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 获取航海之星排行
	 */
	public Map<String, Integer> getFortressStarRank() {
		return starMap;
	}
	
	/**
	 * 增加航海之星数量
	 */
	public void addFortressStar(String serverId, int rank, int count) {
		if (GsConfig.getInstance().getServerId().equals(serverId)) {
			RedisProxy.getInstance().increasaFortressStar(count);
		}
	}
	
	/**
	 * 获取航海之星记录
	 */
	public Map<Integer, FortressRecordItem> getFortressStarRecord() {
		return recordMap;
	}
	
	/**
	 * 增加航海之星记录
	 */
	public void addFortressRecord(String serverId, int turn, int rank, int count) {
		if (!GsConfig.getInstance().getServerId().equals(serverId)) {
			return;
		}
		FortressRecordItem record = new FortressRecordItem(turn, rank, count);
		recordMap.put(turn, record);
		LocalRedis.getInstance().addFortressRecord(record);
	}
	
	/**
	 * 增加期数
	 */
	public void addTurn() {
		turn = turn + 1;
		LocalRedis.getInstance().setFortressTurn(turn);
	}
	
	public CrossFortressCfg getCrossFortressCfg(int x, int y) {
		CrossFortressCfg retCfg = null;
		
		ConfigIterator<CrossFortressCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(CrossFortressCfg.class);
		while(iterator.hasNext()) {
			CrossFortressCfg cfg = iterator.next();
			if (cfg.getX() == x && cfg.getY() == y) {
				retCfg = cfg;
			}
		}
		
		return retCfg;
	}
	
	/**
	 * 检测错误点(由于航海要塞是后加的常驻建筑，所以要把占用范围内的点清掉)
	 */
	public void checkErrorPoint() {
		try {
			int radius = CrossFortressConstCfg.getInstance().getRadius();
			for (IFortress fortress : allFortress.values()) {
				List<Integer> pointIds = getAroundPointId(fortress.getPosX(), fortress.getPosY(), radius);
				for (Integer pointId : pointIds) {
					WorldPoint worldPoint = WorldPointService.getInstance().getWorldPoint(pointId);
					if (worldPoint == null) {
						continue;
					}
					if (worldPoint.getPointType() == WorldPointType.CROSS_FORTRESS_VALUE) {
						continue;
					}
					removeErrorPoint(worldPoint);
				}
			}	
		} catch(Exception e) {
			HawkException.catchException(e);
		}
	}
	
	public void removeErrorPoint(WorldPoint point) {
		switch (point.getPointType()) {
		
		// 玩家城点
		case WorldPointType.PLAYER_VALUE:
			WorldPlayerService.getInstance().removeCity(point.getPlayerId(), true);
			break;
			
		// 资源点
		case WorldPointType.RESOURCE_VALUE:
			if (!HawkOSOperator.isEmptyString(point.getMarchId())) {
				IWorldMarch march = WorldMarchService.getInstance().getMarch(point.getMarchId());
				march.onMarchCallback(HawkTime.getMillisecond(), point);
			}
			WorldResourceService.getInstance().removeResourcePoint(point, true);
			break;
		
		// 野怪点
		case WorldPointType.MONSTER_VALUE:
			
			// 野怪配置
			WorldEnemyCfg monsterCfg = HawkConfigManager.getInstance().getConfigByKey(WorldEnemyCfg.class, point.getMonsterId());
			if (monsterCfg == null) {
				break;
			}

			// 野怪类型
			if (MonsterType.valueOf(monsterCfg.getType()) == null) {
				break;
			}
			
			// 单打的怪
			if (monsterCfg.getType() == MonsterType.TYPE_1_VALUE || monsterCfg.getType() == MonsterType.TYPE_2_VALUE) {
				WorldMonsterService.getInstance().notifyMonsterKilled(point);
				
			// 集结的怪
			} else {
				WorldPointService.getInstance().removeWorldPoint(point.getId());
				AreaObject area = WorldPointService.getInstance().getArea(point.getAreaId());
				area.removeMonsterBoss(point.getId());
				
			}
			break;
		
		// 迷雾要塞
		case WorldPointType.FOGGY_FORTRESS_VALUE:
			WorldFoggyFortressService.getInstance().notifyFoggyFortressKilled(point.getId());
			break;
		
		// 据点
		case WorldPointType.STRONG_POINT_VALUE:
			if (!HawkOSOperator.isEmptyString(point.getMarchId())) {
				IWorldMarch march = WorldMarchService.getInstance().getMarch(point.getMarchId());
				march.onMarchCallback(HawkTime.getMillisecond(), point);
			}
			WorldStrongPointService.getInstance().removeStrongpoint(point, true);
			break;
			
		default:
			break;
		}
	}
	
	public List<Integer> getAroundPointId(int centerX, int centerY, int radius) {
		List<Integer> pointIds = new ArrayList<>();

		// 取x轴上的点
		for (int i = 1; i <= radius - 1; i++) {
			int x1 = centerX + i;
			int x2 = centerX - i;
			pointIds.add(GameUtil.combineXAndY(x1, centerY));
			pointIds.add(GameUtil.combineXAndY(x2, centerY));
		}

		// 取y轴上的点
		for (int i = 1; i <= radius - 1; i++) {
			int y1 = centerY + i;
			int y2 = centerY - i;
			pointIds.add(GameUtil.combineXAndY(centerX, y1));
			pointIds.add(GameUtil.combineXAndY(centerX, y2));
		}
		
		// 取其它点
		for (int i = 0; i <= radius - 1; i++) {
			for (int j = 0; j <= radius - 1 - i; j++) {
				if (i == 0 || j == 0) {
					continue;
				}
				int x1 = centerX + i;
				int x2 = centerX - i;
				int y1 = centerY + j;
				int y2 = centerY - j;
				pointIds.add(GameUtil.combineXAndY(x1, y1));
				pointIds.add(GameUtil.combineXAndY(x1, y2));
				pointIds.add(GameUtil.combineXAndY(x2, y1));
				pointIds.add(GameUtil.combineXAndY(x2, y2));
			}
		}
		return pointIds;
	}
}
