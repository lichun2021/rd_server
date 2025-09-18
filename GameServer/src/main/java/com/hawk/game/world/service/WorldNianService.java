package com.hawk.game.world.service;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import com.hawk.game.config.MergeServerGroupCfg;
import org.hawk.app.HawkAppObj;
import org.hawk.config.HawkConfigManager;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkTime;
import org.hawk.uuid.HawkUUIDGenerator;
import org.hawk.xid.HawkXID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.type.ActivityState;
import com.hawk.activity.type.impl.machineAwakeTwo.cfg.MachineAwakeTwoActivityTimeCfg;
import com.hawk.game.GsConfig;
import com.hawk.game.cfgElement.EffectObject;
import com.hawk.game.config.WorldMapConstProperty;
import com.hawk.game.config.WorldNianCfg;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.LocalRedis;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Activity.ActivityType;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.World.NianInfo;
import com.hawk.game.protocol.World.NianPush;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.service.chat.ChatService;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.object.AreaObject;
import com.hawk.game.world.object.Point;

/**
 * 年兽-服务类
 * @author golden
 *
 */
public class WorldNianService extends HawkAppObj {
	
	/**
	 * 日志
	 */
	public static Logger logger = LoggerFactory.getLogger("Server");

	/**
	 * 实例
	 */
	private static WorldNianService instance = null;
	
	/**
	 * 世界上年兽点列表
	 */
	public Map<String, WorldPoint> nians;

	/**
	 * 刷新boss的时间
	 */
	public volatile long refreshAllTime;
	
	/**
	 * 上一次计算K值的时间
	 */
	public long lastCalcKTime;
	
	/**
	 * K值
	 */
	public long nianK;
	
	public long nianLastK;
	
	public boolean hasCalcK = false;
	
	/**
	 * 获取实例
	 */
	public static WorldNianService getInstance() {
		return instance;
	}
	
	/**
	 * 构造器
	 */
	public WorldNianService(HawkXID xid) {
		super(xid);
		instance = this;
	}
	
	/**
	 * 初始化
	 */
	public boolean init() {
		nians = new ConcurrentHashMap<String, WorldPoint>();
		
		List<WorldPoint> points = WorldPointService.getInstance().getWorldPointsByType(WorldPointType.NIAN);
		for (WorldPoint point : points) {
			nians.put(getNianUuid(point), point);
			logger.info("initNian, x:{}, y:{}, nianId:{}", point.getX(), point.getY(), point.getMonsterId());
		}
		
		//nianLastK = LocalRedis.getInstance().getNianLastK(); //TODO 改为存全局redis-202407171400
		
		nianLastK = RedisProxy.getInstance().getNianLastK(GsConfig.getInstance().getServerId());
		long certainTime = HawkTime.parseTime("2024-07-25 08:00:00");
		if (nianLastK <= 0 && HawkTime.getMillisecond() < certainTime) {
			nianLastK = LocalRedis.getInstance().getNianLastK();
			if (nianLastK > 0) {
				RedisProxy.getInstance().setNianLastK(GsConfig.getInstance().getServerId(), nianLastK);
			}
		}
		
		nianK = getLastNianK(); 
		return true;
	}

	@Override
	public boolean onTick() {
		if (refreshAllTime <= 0L) {
			return true;
		}
		
		calcNianBloodK();
		
		try {
			if (hasNianInWorld() && refreshAllTime > 0) {
				long nianRemoveTime = WorldMapConstProperty.getInstance().getNianRemoveTime();
				if (HawkTime.getMillisecond() - refreshAllTime > nianRemoveTime) {
					removeAllNian();
					Const.NoticeCfgId noticeId = isGhost() ? Const.NoticeCfgId.GHOST_7 : Const.NoticeCfgId.REMOVE_TIMEOUT_NIAN;
					ChatService.getInstance().addWorldBroadcastMsg(Const.ChatType.SPECIAL_BROADCAST, noticeId, null);
				}
				
				List<Integer> nianRmNoticeTimeArray = WorldMapConstProperty.getInstance().getNianRmNoticeTimeArray();
				for (int noticeRmTime : nianRmNoticeTimeArray) {
					long noticeTime = refreshAllTime + nianRemoveTime - noticeRmTime * 1000L;
					if (HawkTime.getMillisecond() >= noticeTime && lastCalcKTime < noticeTime) {
						Const.NoticeCfgId noticeId = isGhost() ? Const.NoticeCfgId.GHOST_8 : Const.NoticeCfgId.REMOVE_TIMEOUT_NIAN_NOTICE;
						ChatService.getInstance().addWorldBroadcastMsg(Const.ChatType.SPECIAL_BROADCAST, noticeId, null, noticeRmTime / 60);
						break;
					}
				}
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		lastCalcKTime = HawkTime.getMillisecond();
		
		return super.onTick();
	}

	/**
	 * 计算年兽血量参数K
	 */
	private void calcNianBloodK() {
		
		// 当前时间
		long currentTime = HawkTime.getMillisecond();
		
		if (!hasCalcK) {
			// 时间A(s) ：计算K值
			List<Integer> nianParamAArray = WorldMapConstProperty.getInstance().getNianParamAArray();
			for (int i = 0; i < nianParamAArray.size(); i++) {
				
				if (lastCalcKTime - refreshAllTime < nianParamAArray.get(i) * 1000L
						&& currentTime - refreshAllTime > nianParamAArray.get(i) * 1000L) {
					
					long allNianInitBlood = getAllNianInitBlood();
					long allNianRemainBlood = getAllNianRemainBlood();
					
					double pencent = ((double)(allNianInitBlood - allNianRemainBlood) / allNianInitBlood) * GsConst.RANDOM_MYRIABIT_BASE;
					if (pencent > WorldMapConstProperty.getInstance().getNianParamB()) {
						nianK = (long)(getLastNianK() * ((double)WorldMapConstProperty.getInstance().getNianParamCArray().get(i) / GsConst.RANDOM_MYRIABIT_BASE));
						hasCalcK = true;
					}
					logger.info("calcNianBloodK, time A, allNianInitBlood:{}, allNianRemainBlood:{}, lastNianK:{}, nianK:{}",
							allNianInitBlood, allNianRemainBlood, getLastNianK(), nianK);
					break;
				}
			}
			
			// 时间X(s) : 计算K值
			List<Integer> nianParamXArray = WorldMapConstProperty.getInstance().getNianParamXArray();
			for (int i = 0; i < nianParamXArray.size(); i++) {
				
				if (lastCalcKTime - refreshAllTime < nianParamXArray.get(i) * 1000L
						&& currentTime - refreshAllTime > nianParamXArray.get(i) * 1000L) {
					
					long allNianInitBlood = getAllNianInitBlood();
					long allNianRemainBlood = getAllNianRemainBlood();
					
					double pencent = ((double)(allNianInitBlood - allNianRemainBlood) / allNianInitBlood) * GsConst.RANDOM_MYRIABIT_BASE;
					if (pencent < WorldMapConstProperty.getInstance().getNianParamY()) {
						nianK = (long)(getLastNianK() * ((double)WorldMapConstProperty.getInstance().getNianParamZArray().get(i) / GsConst.RANDOM_MYRIABIT_BASE));
					}
					logger.info("calcNianBloodK, time X, allNianInitBlood:{}, allNianRemainBlood:{}, lastNianK:{}, nianK:{}",
							allNianInitBlood, allNianRemainBlood, getLastNianK(), nianK);
					break;
				}
			}
		}
	}
	
	/**
	 * 获取世界上年兽总剩余血量
	 * @return
	 */
	private long getAllNianRemainBlood() {
		long blood = 0;
		for (WorldPoint nian : nians.values()) {
			blood += nian.getRemainBlood();
		}
		return blood;
	}
	
	/**
	 * 获取世界上年兽总初始血量
	 * @return
	 */
	private long getAllNianInitBlood() {
		int nianCount = WorldMapConstProperty.getInstance().getNianRefreshPosArr().size();
		return 1L * getNianInitBlood(getRefresNianId()) * nianCount;
	}
	
	/**
	 * 通知年兽被击杀
	 */
	public void notifyNianKilled(int pointId) {
		WorldPoint point = WorldPointService.getInstance().getWorldPoint(pointId);
		nians.remove(getNianUuid(point));
		
		WorldPointService.getInstance().removeWorldPoint(pointId, true);
		
		logger.info("removeNians, isRemoveAll:{}, pointId:{}, x:{}, y:{}, blood:{}, nianId:{}", false, point.getId(), point.getX(), point.getY(), point.getRemainBlood(), point.getMonsterId());
	}
	
	/**
	 * 活动是否开启
	 * @return
	 */
	public boolean isActivityOpen() {
		Optional<ActivityBase> activity = ActivityManager.getInstance().getGameActivityByType(ActivityType.MACHINE_AWAKE_TWO_VALUE);
		return activity.isPresent() && activity.get().getActivityEntity().getActivityState() == ActivityState.OPEN;
	}
	
	public int getNianType() {
		int type = 1;
		Optional<ActivityBase> activity = ActivityManager.getInstance().getGameActivityByType(ActivityType.MACHINE_AWAKE_TWO_VALUE);
		if (activity.isPresent()) {
			int termId = activity.get().getActivityTermId();
			MachineAwakeTwoActivityTimeCfg cfg = HawkConfigManager.getInstance().getConfigByKey(MachineAwakeTwoActivityTimeCfg.class, termId);
			if (cfg != null && cfg.getNianType() == GsConst.WORLD_NIAN_GHODT) {
				type = GsConst.WORLD_NIAN_GHODT;
			}
		}
		return type;
	}
	
	/**
	 * 刷新年兽
	 */
	public void refreshNian() {
		removeAllNian();
		
		if (isActivityOpen()) {
			genAllNian();
			refreshAllTime = HawkTime.getMillisecond();
		}
	}
	
	/**
	 * 移除所有年兽
	 */
	public void removeAllNian() {
		refreshAllTime = 0L;
		
		if (!hasNianInWorld()) {
			if (nianK != 0) {
				
				int nianParamH = WorldMapConstProperty.getInstance().getNianParamH();
				int nianParamG = WorldMapConstProperty.getInstance().getNianParamG();
				nianK = Math.min(nianParamH, nianK);
				nianK = Math.max(nianK, nianParamG);
				//LocalRedis.getInstance().setNianLastK(nianK); //TODO 改为存全局redis-202407171400
				RedisProxy.getInstance().setNianLastK(GsConfig.getInstance().getServerId(), nianK);
				
				nianLastK = nianK;
				hasCalcK = false;
			}
			logger.info("removeallnians, nianK:{}", nianK);
			return;
		}
		
		for (WorldPoint point : nians.values()) {
			logger.info("removenians, isRemoveAll:{}, pointId:{}, x:{}, y:{}, blood:{}, nianId:{}", true, point.getId(), point.getX(), point.getY(), point.getRemainResNum(), point.getMonsterId());
			WorldPointService.getInstance().removeWorldPoint(point.getId(), true);
		}
		nians = new ConcurrentHashMap<>();
		refreshAllTime = 0L;
		
		int nianParamH = WorldMapConstProperty.getInstance().getNianParamH();
		int nianParamG = WorldMapConstProperty.getInstance().getNianParamG();
		nianK = Math.min(nianParamH, nianK);
		nianK = Math.max(nianK, nianParamG);
		//LocalRedis.getInstance().setNianLastK(nianK); //TODO 改为存全局redis-202407171400
		RedisProxy.getInstance().setNianLastK(GsConfig.getInstance().getServerId(), nianK);
		
		nianLastK = nianK;
		hasCalcK = false;
		
		logger.info("removeallnians, nianK:{}", nianK);
	}
	
	/**
	 * 生成所有年兽
	 */
	public void genAllNian() {
		List<int[]> posArr = WorldMapConstProperty.getInstance().getNianRefreshPosArr();
		for (int[] pos : posArr) {
			int pointId = GameUtil.combineXAndY(pos[0], pos[1]);
			genNian(pointId);
		}
		
		Const.NoticeCfgId noticeId = isGhost() ? Const.NoticeCfgId.GHOST_1: Const.NoticeCfgId.WORLD_NIAN_GUNDAM_REFRESH;
		ChatService.getInstance().addWorldBroadcastMsg(Const.ChatType.SPECIAL_BROADCAST, noticeId, null);
		
		LocalRedis.getInstance().setNianRefreshUuid(HawkUUIDGenerator.genUUID());
	}
	
	/**
	 * 生成年兽
	 */
	public void genNian(int pointId) {
		
		// 年兽刷新区域半径(以坐标为中心，半径内区域随机生成点)
		int areaRadius = WorldMapConstProperty.getInstance().getNianRefreshAreaRadius();
		
		List<Point> areaPoints = WorldPointService.getInstance().getRhoAroundPointsFree(pointId, areaRadius);
		
		Collections.shuffle(areaPoints);
		
		for (Point point : areaPoints) {
			
			AreaObject area = WorldPointService.getInstance().getArea(point.getAreaId());
			
			if (!point.canNianSeat()) {
				continue;
			}
			
			if (!WorldPointService.getInstance().tryOccupied(area, point, GsConst.NIAN_RADIUS)) {
				continue;
			}
			
			if (WorldPointService.getInstance().getWorldPoint(point.getId()) != null) {
				continue;
			}
			
			WorldPoint worldPoint = new WorldPoint(point.getX(), point.getY(), point.getAreaId(), point.getZoneId(), WorldPointType.NIAN_VALUE);
			int nianId = getRefresNianId();
			worldPoint.setMonsterId(nianId);
			worldPoint.setRemainBlood(getNianInitBlood(nianId));
			
			if (!WorldPointService.getInstance().createWorldPoint(worldPoint)) {
				logger.error("gennian error, create failed, x:{}, y:{}, areaId:{}, nianId:{}", point.getX(), point.getY(), point.getAreaId(), nianId);
				continue;
			}
			
			nians.put(getNianUuid(worldPoint), worldPoint);
			
			logger.info("gennian, x:{}, y:{}, areaId:{}, blood:{}, nianId:{}", point.getX(), point.getY(), point.getAreaId(), worldPoint.getRemainBlood(), nianId);
			
			break;
		}
	}
	
	/**
	 * 获取可以刷新的年兽Id
	 */
	public int getRefresNianId() {
		WorldNianCfg returnCfg = HawkConfigManager.getInstance().getConfigByIndex(WorldNianCfg.class, 0);
		
		int nianType = WorldNianService.getInstance().getNianType();
		if (nianType == GsConst.WORLD_NIAN_GHODT) {
			WorldNianCfg ghostCfg = HawkConfigManager.getInstance().getConfigByKey(WorldNianCfg.class, nianType);
			if (ghostCfg != null) {
				returnCfg = ghostCfg;
			}
		}
		return returnCfg.getId();
	}
	
	/**
	 * 获取所有年兽点
	 */
	public Collection<WorldPoint> getNians() {
		return nians.values();
	}
	
	/**
	 * 获取年兽初始血量
	 */
	public int getNianInitBlood(int nianId) {
		WorldNianCfg cfg = HawkConfigManager.getInstance().getConfigByKey(WorldNianCfg.class, nianId);
		if (cfg == null) {
			return 0;
		}
		
		long blood = 0;
		List<ArmyInfo> armyList = cfg.getArmyList();
		for (ArmyInfo army : armyList) {
			blood += army.getTotalCount();
		}
		
		blood = (int)(1L * (blood * getLastNianBloodK()) / GsConst.RANDOM_MYRIABIT_BASE);
		
		int hpNumber = cfg.getHpNumber();
		
		// 返回血量自适应，可以整除血条数量
		return (int)((((blood - 1) / hpNumber) + 1) * hpNumber); 
	}
	
	/**
	 * 世界上是否有年兽
	 * @return
	 */
	public boolean hasNianInWorld() {
		return nians.size() > 0;
	}
	
	/**
	 * 年兽id
	 * @param point
	 * @return
	 */
	public String getNianUuid(WorldPoint point) {
		return point.getId() + ":" + point.getCreateTime();
	}
	
	/**
	 * 年兽刷新uuid
	 */
	public String getNianRefreshUuid() {
		return LocalRedis.getInstance().getNianRefreshUuid();
	}
	
	/**
	 * 推送年兽信息
	 * @param player
	 */
	public void pushNianInfo(Player player) {
		NianPush.Builder builder = NianPush.newBuilder();
		if (!isActivityOpen()) {
			player.sendProtocol(HawkProtocol.valueOf(HP.code.NIAN_INFO_RESP, builder));
		}
		
		String nianRefreshUuid = getNianRefreshUuid();
		
		for (WorldPoint nian : nians.values()) {
			NianInfo.Builder nianInfo = NianInfo.newBuilder();
			nianInfo.setPosX(nian.getX());
			nianInfo.setPosY(nian.getY());
			
			int times = player.getAtkNianTimes(nianRefreshUuid);
			nianInfo.setAtkTimes(times);
			
			builder.addNian(nianInfo);
		}
		
		player.sendProtocol(HawkProtocol.valueOf(HP.code.NIAN_INFO_RESP, builder));
	}
	
	private long getLastNianK() {
		if (nianLastK == 0) {
			boolean isMergeServer = GlobalData.getInstance().isMerged();
			if (isMergeServer) {
				String mainServerId = GsConfig.getInstance().getServerId();
				long mergeNianK = RedisProxy.getInstance().getMergeNianK(mainServerId);
				List<String> slaveServerIds = MergeServerGroupCfg.getSlaveServerIds(mainServerId);
				int count = 1;
				if(slaveServerIds != null && !slaveServerIds.isEmpty()){
					for(String slaveServerId : slaveServerIds){
						mergeNianK += RedisProxy.getInstance().getMergeNianK(slaveServerId);
						count++;
					}
				}
				float ratio = WorldMapConstProperty.getInstance().getDifficultyRatio();
				int correction =WorldMapConstProperty.getInstance().getDifficultyCorrection();
				return (long) Math.ceil((double) mergeNianK * ratio) + correction;
			} else {
				return 1 * GsConst.RANDOM_MYRIABIT_BASE;
			}
		} else {
			return nianLastK;
		}
	}
	
	public long getLastNianBloodK() {
		int nianParamK = WorldMapConstProperty.getInstance().getNianParamK(); //nianParamK=1000000
		return Math.min(nianParamK, getLastNianK());
	}
	
	public boolean isGhost() {
		int nianType = WorldNianService.getInstance().getNianType();
		return nianType == GsConst.WORLD_NIAN_GHODT;		
	}
	
	public long getEffValue(EffectObject eff) {
		int nianParamK = WorldMapConstProperty.getInstance().getNianParamK();
		return Math.max(0, getLastNianK() * eff.getEffectValue() / nianParamK  - 10000);
	}
}