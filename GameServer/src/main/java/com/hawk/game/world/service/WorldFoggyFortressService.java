package com.hawk.game.world.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.hawk.app.HawkApp;
import org.hawk.app.HawkAppObj;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkRand;
import org.hawk.os.HawkTime;
import org.hawk.tickable.HawkPeriodTickable;
import org.hawk.xid.HawkXID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hawk.game.GsConfig;
import com.hawk.game.activity.impl.yurirevenge.YuriRevengeService;
import com.hawk.game.config.FoggyFortressCfg;
import com.hawk.game.config.WorldFoggyFortressRefreshCfg;
import com.hawk.game.config.WorldMapConstProperty;
import com.hawk.game.config.YuriRevengeMonsterCfg;
import com.hawk.game.guild.manor.GuildManorObj;
import com.hawk.game.invoker.YuriRevengeFinishMsgInvoker;
import com.hawk.game.item.AwardItems;
import com.hawk.game.protocol.Const.PushMsgType;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.service.GuildManorService;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.PushService;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mail.SystemMailService;
import com.hawk.game.util.AlgorithmPoint;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.WorldUtil;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.march.IWorldMarch;
import com.hawk.game.world.object.AreaObject;
import com.hawk.game.world.object.FoggyInfo;
import com.hawk.game.world.object.GuildYuriFactory;
import com.hawk.game.world.object.Point;
import com.hawk.game.world.object.YuriFactoryPoint;
import com.hawk.game.world.proxy.WorldPointProxy;
import com.hawk.game.world.thread.WorldThreadScheduler;
import com.hawk.gamelib.GameConst.MsgId;

/**
 * 迷雾要塞服务类
 * @author zhenyu.shang
 * @since 2018年2月22日
 */
public class WorldFoggyFortressService extends HawkAppObj{
	
	private static Logger logger = LoggerFactory.getLogger("Server");
	
	private static WorldFoggyFortressService instance = null;
	
	/**
	 * 所有尤里点映射表
	 */
	private Map<Integer, YuriFactoryPoint> factoryPoint;

	/**
	 * 公会对应的尤里复仇活动点
	 */
	private Map<String, YuriFactoryPoint> yuriRevengeMap;

	
	public static WorldFoggyFortressService getInstance() {
		return instance;
	}

	public WorldFoggyFortressService(HawkXID xid) {
		super(xid);
		instance = this;
		factoryPoint = new ConcurrentHashMap<Integer, YuriFactoryPoint>();
		yuriRevengeMap = new ConcurrentHashMap<String, YuriFactoryPoint>();
	}

	public boolean init() {
		HawkLog.logPrintln("world foggyFortress service init start");
		List<WorldPoint> foggyFortressPoints = WorldPointService.getInstance().getWorldPointsByType(WorldPointType.FOGGY_FORTRESS);
		if (foggyFortressPoints.isEmpty()) {
			createFoggyFortressPoints();
		} else {
			for (WorldPoint worldPoint : foggyFortressPoints) {
				if(worldPoint.isLifeEndDead(0)){
					continue;
				}
				//加入到尤里
				factoryPoint.put(worldPoint.getId(), new YuriFactoryPoint(worldPoint));
			}
		}
		
		// 注册yuri的更新周期, 以秒为单位, 算生命周期
		WorldThreadScheduler.getInstance().addWorldTickable(new HawkPeriodTickable(1000, 1000) {
			@Override
			public void onPeriodTick() {
				long beginTimeMs = HawkTime.getMillisecond();
				try {
					updateYuriPoint();
				} catch (Exception e) {
					HawkException.catchException(e);
				} finally {
					// 时间消耗的统计信息
					long costTimeMs = HawkTime.getMillisecond() - beginTimeMs;
					if (costTimeMs > GsConfig.getInstance().getProtoTimeout()) {
						logger.warn("process updateYuriPoint tick too much time, costtime: {}", costTimeMs);
					}
				}
			}
		});
		HawkLog.logPrintln("world foggyFortress service init end");
		return true;
	}
	
	/**
	 * 尤里矿场的心跳
	 */
	public void updateYuriPoint() {
		for (YuriFactoryPoint yuri : factoryPoint.values()) {
			yuri.heartbeat();
		}
	}

	/**
	 * 初次创建世界所有怪点
	 * @return
	 */
	private void createFoggyFortressPoints() {
		// 按区域生成怪
		logger.info("init foggyFortress points...");
		for (AreaObject areaObj : WorldPointService.getInstance().getAreaVales()) {
			long startTime = HawkTime.getMillisecond();
			logger.info("start create foggyFortress points, areaId: {}", areaObj.getId());
			int monsterCount = areaObj.getTotalPointCount() * WorldMapConstProperty.getInstance().getWorldfoggyFortressRefreshMax() / 1000 / GsConst.POINT_TO_GRUD;
			// 生成怪物
			this.bornFoggyFortressOnArea(areaObj, null, monsterCount, true, false);
			// 记录信息
			logger.info("creat foggyFortress  finish, areaId: {}, pointCount: {}, costtime: {}", areaObj.getId(), monsterCount, HawkTime.getMillisecond() - startTime);
		}
	}

	/**
	 * 刷一个怪点
	 * 
	 * @param bornPoint
	 * @param enemyRefresh
	 * @param monsterIds
	 * @param monsterWeights
	 * @param monsterType
	 * @return
	 */
	private WorldPoint createMonsterWorldPoint(Point bornPoint, FoggyFortressCfg foggyFortressCfg, boolean isInit, AreaObject areaObject) {
		// 黑土地的怪进行特殊处理
		WorldFoggyFortressRefreshCfg cfg = getCurrentForggyRefreshCfg();
		if(cfg == null){
			logger.error("born foggy on area failed, cant find zone cfg, zoneId: {}", bornPoint.getZoneId());
			return null;
		}
		
		if(foggyFortressCfg == null){
			int foggyId = cfg.getRandomFoggy(bornPoint.getZoneId());
			//计算保底
			int needFoggyId = areaObject.getNeedFoggyId(cfg.getFoggyIds(bornPoint.getZoneId()));
			if(needFoggyId > 0){
				foggyId = needFoggyId;
			}
			// 怪物配置
			foggyFortressCfg = HawkConfigManager.getInstance().getConfigByKey(FoggyFortressCfg.class, foggyId);
			if (foggyFortressCfg == null) {
				logger.error("born foggy on area failed, cant find foggy, foggyId: {}", foggyId);
				return null;
			}
		}
		
		WorldPoint worldPoint = new WorldPoint(bornPoint.getX(), bornPoint.getY(), bornPoint.getAreaId(), bornPoint.getZoneId(), WorldPointType.FOGGY_FORTRESS_VALUE);

		//创建要塞基本信息
		FoggyInfo foggyInfo = new FoggyInfo();
		foggyInfo.setTrapInfo(foggyFortressCfg.getRandTrapInfo());
		foggyInfo.setSoliderInfo(foggyFortressCfg.getRandSoldierInfo());
		foggyInfo.setHeroIds(foggyFortressCfg.getRandHeroId(2));
		
		worldPoint.initFoggyInfo(foggyInfo);
		worldPoint.setMonsterId(foggyFortressCfg.getId());
		long lifeStartTime = HawkTime.getMillisecond();
		if (isInit) {
			lifeStartTime += HawkRand.randInt(0, foggyFortressCfg.getLifeTime() * 1000);
		}
		worldPoint.setLifeStartTime(lifeStartTime);
		return worldPoint;
	}

	/**
	 * 在指定区域内出生N个怪
	 * @param areaObj
	 * @return
	 */
	public List<WorldPoint> bornFoggyFortressOnArea(AreaObject areaObj, FoggyFortressCfg enemyCfg, int count, boolean isInit, boolean isInCaptalArea) {
		long startTime = HawkTime.getMillisecond();

		List<WorldPoint> bornMonsterList = new ArrayList<WorldPoint>(count);

		// 不需要重生 || 没有世界点的区域
		if (count <= 0 || areaObj.getTotalPointCount() <= 0) {
			return bornMonsterList;
		}

		// 找出刷新怪物的点
		List<Point> pointList = areaObj.getValidPoints(WorldPointType.FOGGY_FORTRESS, null, !isInit, isInCaptalArea);
		if (pointList == null || pointList.size() <= 0) {
			logger.error("born foggy on area failed, areaId: {}, freePointSize: {}, monsterType:{}", areaObj.getId(), pointList.size());
			return bornMonsterList;
		}
		int size = pointList.size();
		// 乱序
		Collections.shuffle(pointList);
		logger.info("current area id:{}, need create foggy count :{}, free point count :{}", areaObj.getId(), count, size);
		//由于半径是2，所以存在重叠的情况，所以此处需要一直生成到有为止。
		int addCount = 0;
		// 生成所需要的点
		for (int i = 0; i < size; i++) {
			Point bornPoint = pointList.get(i);
			// 检查是否能被占用
			if (!WorldPointService.getInstance().tryOccupied(areaObj, bornPoint, GsConst.PLAYER_POINT_RADIUS)) {
				continue;
			}
			boolean isInCapitalArea = WorldPointService.getInstance().isInCapitalArea(bornPoint.getId());
			if (isInit && isInCapitalArea && HawkRand.randInt(10000) > WorldMapConstProperty.getInstance().getCapitalFoggyFortressFixRate()) {
				addCount++;
				if (addCount >= count) {
					break;
				}
				continue;
			}
			WorldPoint worldPoint = createMonsterWorldPoint(bornPoint, enemyCfg, false, areaObj);
			if (worldPoint == null) {
				continue;
			}
			// 将点加入到世界
			WorldPointService.getInstance().addPoint(worldPoint);
			// 将点加入到尤里工程
			factoryPoint.put(worldPoint.getId(), new YuriFactoryPoint(worldPoint));
			// 加入存库
			bornMonsterList.add(worldPoint);
			addCount++;
			if (addCount >= count) {
				break;
			}
		}
		
		// 创建所有需要占用点
		WorldPointProxy.getInstance().batchCreate(bornMonsterList);
		logger.info("born foggy on area, count: {}, costtime: {}", count, HawkTime.getMillisecond() - startTime);
		return bornMonsterList;
	}

	/**
	 * 通知据点资源被采尽
	 * 
	 * @param worldPoint
	 * @return
	 */
	public void notifyFoggyFortressKilled(int pointId) {
		try {
			WorldPoint worldPoint = WorldPointService.getInstance().getWorldPoint(pointId);
			boolean isInCapitalArera = WorldPointService.getInstance().isInCapitalArea(pointId);
			// 点为null || (不是野怪 && 不是机器人)
			if (worldPoint == null || worldPoint.getPointType() != WorldPointType.FOGGY_FORTRESS_VALUE) {
				return;
			}
			// 世界点消失处理
			doPointDisappear(worldPoint);
			int foggyId = worldPoint.getMonsterId();
			// 怪物配置
			FoggyFortressCfg enemyCfg = HawkConfigManager.getInstance().getConfigByKey(FoggyFortressCfg.class, foggyId);
			if (enemyCfg == null) {
				return;
			}
			// 删除原有的怪点数据
			WorldPointService.getInstance().removeWorldPoint(worldPoint.getX(), worldPoint.getY());
			//移除尤里点
			removeYuriPoint(worldPoint.getId());
			
			logger.info("remove strongpoint worldPoint, pos: ({}, {})", worldPoint.getX(), worldPoint.getY());
			AreaObject areaObj = WorldPointService.getInstance().getArea(worldPoint.getAreaId());
			if (areaObj == null) {
				return;
			}
			// 减去迷雾点区域数量
			areaObj.delFoggyIdNum(enemyCfg.getLevel());
			// 出生新的据点
			bornFoggyFortressOnArea(areaObj, enemyCfg, 1, false, isInCapitalArera);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 世界点消失处理
	 * @param point
	 */
	public void doPointDisappear(WorldPoint point) {
		Collection<IWorldMarch> marchs = WorldMarchService.getInstance().getWorldPointMarch(point.getX(), point.getY());
		for (IWorldMarch march : marchs) {
			if (!march.getMarchType().equals(WorldMarchType.FOGGY_FORTRESS_MASS)) {
				continue;
			}
			if (march.isReturnBackMarch()) {
				continue;
			}
			// 集结解散
			doMassMarchDissolve(march);
			// 世界点消失，发送邮件
			sendPointDisappearMail(march, MailId.WORLD_FOGGY_MARCH_POINT_DISAPPEAR);
		}
	}
	
	/**
	 * 队伍解散
	 * @param leaderMarch
	 * @param needSendMail
	 */
	private void doMassMarchDissolve(IWorldMarch leaderMarch) {
		long currTime = HawkTime.getMillisecond();
		// 队员返回
		Set<IWorldMarch> joinMarchs = WorldMarchService.getInstance().getMassJoinMarchs(leaderMarch, false);
		for (IWorldMarch joinMarch : joinMarchs) {
			if (joinMarch.isReturnBackMarch()) {
				continue;
			}
			if (joinMarch.getMarchEntity().getMarchStatus() == WorldMarchStatus.MARCH_STATUS_WAITING_VALUE) {
				WorldMarchService.getInstance().onPlayerNoneAction(joinMarch, currTime);
			}
			if (joinMarch.getMarchEntity().getMarchStatus() == WorldMarchStatus.MARCH_STATUS_MARCH_VALUE
					|| joinMarch.getMarchEntity().getMarchStatus() == WorldMarchStatus.MARCH_STATUS_JOIN_MARCH_VALUE) {
				AlgorithmPoint currPoint = WorldUtil.getMarchCurrentPosition(joinMarch.getMarchEntity());
				WorldMarchService.getInstance().onMarchReturn(joinMarch, currTime, AwardItems.valueOf(), joinMarch.getMarchEntity().getArmys(), currPoint.getX(), currPoint.getY());
			}
		}
		
		// 队长返回
		if (leaderMarch.getMarchEntity().getMarchStatus() == WorldMarchStatus.MARCH_STATUS_WAITING_VALUE) {
			WorldMarchService.getInstance().onMarchReturnImmediately(leaderMarch, leaderMarch.getMarchEntity().getArmys());
		} else {
			AlgorithmPoint currPoint = WorldUtil.getMarchCurrentPosition(leaderMarch.getMarchEntity());
			WorldMarchService.getInstance().onMarchReturn(leaderMarch, currTime, AwardItems.valueOf(), leaderMarch.getMarchEntity().getArmys(), currPoint.getX(), currPoint.getY());
		}
	}
	
	/**
	 * 世界点消失，发送邮件
	 * @param leaderMarch
	 */
	public void sendPointDisappearMail(IWorldMarch leaderMarch, MailId mailId) {
		// 需要发送邮件的玩家id列表
		List<String> needSendPlayerIds = new ArrayList<>();
		needSendPlayerIds.add(leaderMarch.getPlayerId());
		
		Set<IWorldMarch> joinMarchs = WorldMarchService.getInstance().getMassJoinMarchs(leaderMarch, false);
		for (IWorldMarch joinMarch : joinMarchs) {
			if (joinMarch.isReturnBackMarch()) {
				continue;
			}
			needSendPlayerIds.add(joinMarch.getPlayerId());
		}
		
		// 发送邮件
		for (String playerId : needSendPlayerIds) {
			SystemMailService.getInstance().sendMail(MailParames.newBuilder()
					.setPlayerId(playerId)
					.setMailId(mailId)
					.build());
		}
	}
	
	/**
	 * 获取尤里矿点
	 * 
	 * @param pointId
	 * @return
	 */
	public YuriFactoryPoint getYuriFactoryPoint(int pointId) {
		return factoryPoint.get(pointId);
	}
	
	/**
	 * 删除点
	 * @param pointId
	 */
	public void removeYuriPoint(int pointId){
		factoryPoint.remove(pointId);
	}

	/**
	 * 开启尤里的复仇活动
	 * 
	 * @param guildId
	 * @return
	 */
	public int openYuriRevenge(String guildId) {
		// 判断此公会是否已经开启的活动
		if (yuriRevengeMap.containsKey(guildId)) {
			return Status.Error.YURI_REVENGE_ALREADY_OPEN_VALUE;
		}
		YuriFactoryPoint yuriPoint = findYuriPoint(guildId);
		if (yuriPoint == null) {
			return Status.Error.YURI_REVENGE_CAN_NOT_FIND_POINT_VALUE;
		}
		// 开启活动
		yuriPoint.startYuriRevenge(guildId);
		// 添加到全局
		yuriRevengeMap.put(guildId, yuriPoint);
		// 添加推送
		for (String playerId : GuildService.getInstance().getGuildMembers(guildId)) {
			PushService.getInstance().pushMsg(playerId, PushMsgType.YURI_ACTIVITY_OPEN_VALUE);
		}
		return Status.SysError.SUCCESS_OK_VALUE;
	}

	/**
	 * 关闭尤里复仇活动
	 * 
	 * @param guildId
	 */
	public void closeYuriRevenge(String guildId, boolean clearMarch) {
		YuriFactoryPoint factoryPoint = yuriRevengeMap.remove(guildId);
		if (factoryPoint != null) {
			factoryPoint.closeYuriRevenge(guildId, clearMarch);
		}
		if (!clearMarch) {
			YuriRevengeService.getInstance().dealMsg(MsgId.YUNI_REVENGE_FINISH, new YuriRevengeFinishMsgInvoker(guildId));
		}
	}

	/**
	 * 查找符号要求的点
	 * 
	 * @param guildId
	 * @return
	 */
	private YuriFactoryPoint findYuriPoint(String guildId) {
		// 查找合适的点
		List<GuildManorObj> manors = GuildManorService.getInstance().getGuildManors(guildId);
		int pointId = -1;
		for (GuildManorObj guildManorObj : manors) {
			if (guildManorObj.isComplete()) {
				pointId = guildManorObj.getPositionId();
				break;
			}
		}
		WorldPoint worldPoint = null;
		// 如果没有找到，则在盟主所在区域随机一个尤里点
		if (pointId > 0) {
			// 如果有联盟堡垒, 取离堡垒最近的
			worldPoint = WorldPointService.getInstance().getWorldPoint(pointId);
		} else {
			// 取离会长最近的
			worldPoint = WorldPlayerService.getInstance().getPlayerWorldPoint(GuildService.getInstance().getGuildLeaderId(guildId));
		}
		//如果取不到相对点, 则直接在地图上随机一个不在活动中的尤里点
		if(worldPoint == null){
			List<YuriFactoryPoint> list = new ArrayList<YuriFactoryPoint>();
			for (YuriFactoryPoint yuriFactoryPoint : factoryPoint.values()) {
				if(!yuriFactoryPoint.isInActive()){
					list.add(yuriFactoryPoint);
				}
			}
			//极限情况,如果所有尤里点都在活动中, 则全量随机
			if(list.isEmpty()){
				for (YuriFactoryPoint yuriFactoryPoint : factoryPoint.values()) {
					list.add(yuriFactoryPoint);
				}
			}
			return HawkRand.randomObject(list);
		}
		return getNearestPointByArea(worldPoint);
	}

	/**
	 * 获取离目标点最近的一个尤里点
	 * 
	 * @param areaId
	 * @return
	 */
	public YuriFactoryPoint getNearestPointByArea(WorldPoint worldPoint) {
		YuriFactoryPoint yuriPoint = null;
		List<Point> allPoints = WorldPointService.getInstance().getArea(worldPoint.getAreaId()).getAllPointList();
		double dis = 0.0;
		for (Point point : allPoints) {
			YuriFactoryPoint tempPoint = factoryPoint.get(point.getId());
			if (tempPoint != null) {
				double thisDis = WorldUtil.distance(point.getX(), point.getY(), worldPoint.getX(), worldPoint.getY());
				if (dis == 0.0 || thisDis < dis) {
					dis = thisDis;
					yuriPoint = tempPoint;
				}
			}
		}
		return yuriPoint;

	}

	/**
	 * 获取公会对应的点
	 * 
	 * @param guildId
	 * @return
	 */
	public YuriFactoryPoint getGuildYuriPoint(String guildId) {
		return yuriRevengeMap.get(guildId);
	}

	/**
	 * 移除向某个玩家的所有行军
	 * 
	 * @param playerId
	 * @param clearPlayer
	 *            是否删除玩家，退出联盟或者踢出联盟为true
	 */
	public void removePlayerMonsterMarch(String guildId, String playerId, boolean clearPlayer) {
		YuriFactoryPoint point = this.yuriRevengeMap.get(guildId);
		if (point != null) {
			GuildYuriFactory factory = point.getGuildYuriFactory(guildId);
			if(factory != null){
				factory.removePlayerAllMonsterMarch(playerId, clearPlayer);
			}
		}
	}

	/**
	 * 战斗结束，或者移除某个玩家的一条行军
	 * 
	 * @param playerId
	 * @param marchId
	 */
	public void removeCurrentMonsterMarch(String guildId, String marchId) {
		YuriFactoryPoint point = this.yuriRevengeMap.get(guildId);
		if (point != null) {
			point.getGuildYuriFactory(guildId).removePlayerMonsterMarch(marchId);
		}
	}
	
	public int[] getYuriInOpenTime(){
		int[] res = new int[2];
		//计算当前开服天数, 向上取整
		Double openTime = Math.ceil((HawkApp.getInstance().getCurrentTime() - GameUtil.getServerOpenTime()) / 86400000.0);
		ConfigIterator<YuriRevengeMonsterCfg> configIt = HawkConfigManager.getInstance().getConfigIterator(YuriRevengeMonsterCfg.class);
		int day = openTime.intValue();
		while (configIt.hasNext()) {
			YuriRevengeMonsterCfg yuriRevengeMonsterCfg = configIt.next();
			if(day >= yuriRevengeMonsterCfg.getOpenServiceStartDay() && day <= yuriRevengeMonsterCfg.getOpenServiceEndDay()){
				res[0] = yuriRevengeMonsterCfg.getOpenServiceStartDay();
				res[1] = yuriRevengeMonsterCfg.getOpenServiceEndDay();
				break;
			}
		}
		return res;
	}
	
	/**
	 * 根据开服时间 获取当前怪物刷新规则
	 * @return
	 */
	public WorldFoggyFortressRefreshCfg getCurrentForggyRefreshCfg(){
		long hasOpenTime = HawkApp.getInstance().getCurrentTime() - GameUtil.getServerOpenTime();
		//如果还没到开服时间，则取第一组就行
		if(hasOpenTime <= 0){
			return HawkConfigManager.getInstance().getConfigByIndex(WorldFoggyFortressRefreshCfg.class, 0);
		}
		WorldFoggyFortressRefreshCfg res = null;
		//跟据开服时间, 取出最大的一组
		ConfigIterator<WorldFoggyFortressRefreshCfg> it = HawkConfigManager.getInstance().getConfigIterator(WorldFoggyFortressRefreshCfg.class);
		long lastTime = 0;
		while (it.hasNext()) {
			WorldFoggyFortressRefreshCfg cfg = it.next();
			long openServiceTime = cfg.getOpenServiceTimeLowerLimit() * 1000L;
			if (openServiceTime >= lastTime && hasOpenTime > openServiceTime){
				res = cfg;
				lastTime = openServiceTime;
			}
		}
		return res;
	}
	
	/**
	 * 获取当前刷新的最大等级的尖塔
	 * @return
	 */
	public int getCurrentMaxForggyLevel() {
		WorldFoggyFortressRefreshCfg cfg = getCurrentForggyRefreshCfg();
		return cfg.getMaxLevel();
	}
	
	/**
	 * 检查这个点在不在活动中
	 * @param pointId
	 * @return
	 */
	public boolean checkPointIsInActive(int pointId){
		YuriFactoryPoint yuri = factoryPoint.get(pointId);
		if(yuri == null){
			return false;
		}
		return yuri.isInActive();
	}
}
