package com.hawk.game.city;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.hawk.app.HawkApp;
import org.hawk.collection.ConcurrentHashSet;
import org.hawk.db.HawkDBManager;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.task.HawkTaskManager;
import org.hawk.tickable.HawkPeriodTickable;

import com.hawk.game.config.BuildingCfg;
import com.hawk.game.config.ConstProperty;
import com.hawk.game.crossproxy.CrossService;
import com.hawk.game.entity.PlayerBaseEntity;
import com.hawk.game.entity.StatusDataEntity;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.invoker.CityBrokenMoveInvoker;
import com.hawk.game.march.AutoMonsterMarchParam;
import com.hawk.game.msg.AutoSearchMonsterMsg;
import com.hawk.game.player.Player;
import com.hawk.game.player.hero.PlayerHero;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.Const.BuildingType;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.queryentity.CityDefInfo;
import com.hawk.game.queryentity.CityShieldInfo;
import com.hawk.game.service.GuildService;
import com.hawk.game.util.EffectParams;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.LogUtil;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.service.WorldPlayerService;
import com.hawk.game.world.service.WorldPointService;
import com.hawk.game.world.service.WorldRobotService;
import com.hawk.game.world.thread.WorldThreadScheduler;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.LogConst.CityWallChangeType;

/**
 * 城市管理器
 * 
 * @author lating
 *
 */
public class CityManager {

	/**
	 * <playerId, 玩家城防值降到0的时间点> 只包含城墙处于燃烧状态的玩家
	 */
	private Map<String, Long> playerCityBrokenTime = new ConcurrentHashMap<>();
	/**
	 * 玩家实际城防值的小数部分
	 */
	private Map<String, Double> playerCityDefFractions = new ConcurrentHashMap<>();
	/**
	 * 正在移除城点的玩家
	 */
	private Set<String> removeCityPlayers = new ConcurrentHashSet<>();
	/**
	 * 离线玩家破罩时间记录
	 */
	private Map<String, Long> playerCityShieldEndTimes = new ConcurrentHashMap<>();
	/**
	 * 正处于燃烧状态的玩家
	 */
	private Map<String, Long> cityOnFireEndTimes = new ConcurrentHashMap<String, Long>();
	/**
	 * 雪球燃烧结束时间  1、结束清除 2、雪球击中更新 3、算燃烧时间上限时判断
	 */
	private Map<String, Long> snowFireEndTimeMap = new ConcurrentHashMap<String, Long>();
	
	/**
	 * 实例对象
	 */
	private static CityManager instance = null;

	/**
	 * 获取实例对象
	 * 
	 * @return
	 */
	public static CityManager getInstance() {
		if (instance == null) {
			instance = new CityManager();
		}
		return instance;
	}

	/**
	 * 初始化
	 */
	public boolean init() {

		HawkLog.logPrintln("load cityDef info from db......");
		
		// 查找处于着火状态或着火状态结束待结算状态
		List<CityDefInfo> cityDefInfoList = HawkDBManager.getInstance().executeQuery(
				"select playerId, onFireEndTime, cityDefVal, cityDefConsumeTime from player_base where cityDefConsumeTime > 0",
				CityDefInfo.class);
		
		long now = HawkTime.getMillisecond();
		if (cityDefInfoList != null) {
			for(CityDefInfo cityDefInfo : cityDefInfoList) {
				// 如果时跨出去的服不处理
				if (CrossService.getInstance().isEmigrationPlayer(cityDefInfo.getPlayerId())) {
					continue;
				}
				checkCityDefBroken(cityDefInfo, now);
			}
		}
		
		// 查找处于保护状态或保护时间结束但还未处理的信息
		List<CityShieldInfo> cityShieldInfoList = HawkDBManager.getInstance().executeQuery(
				"select playerId, endTime from status_data where statusId = " + EffType.CITY_SHIELD_VALUE + " and endTime > " + now,
				CityShieldInfo.class);

		if (cityShieldInfoList != null) {
			for(CityShieldInfo cityShieldInfo : cityShieldInfoList) {
				// 如果时跨出去的服不处理
				if (CrossService.getInstance().isEmigrationPlayer(cityShieldInfo.getPlayerId())) {
					continue;
				}
				addCityShieldInfo(cityShieldInfo.getPlayerId(), cityShieldInfo.getEndTime());
			}
		}
		
		WorldThreadScheduler.getInstance().addWorldTickable(new HawkPeriodTickable(5000) {
			@Override
			public void onPeriodTick() {
				try {
					onCityDefTick();
					onCityFireEndTick();
					onCityShieldTick();
				} catch (Exception e) {
					HawkException.catchException(e);
				}
			}
		});
		
		return true;
	}
	
	/**
	 * 城防值检测是否破城
	 */
	private void onCityDefTick() {
		if(playerCityBrokenTime.isEmpty()) {
			return;
		}
		//遍历所有破城玩家
		for(String playerId : playerCityBrokenTime.keySet()) {
			moveCity(playerId, false);
		}
	}
	
	/**主城是否在燃烧*/
	public boolean cityIsFired(String playerId){
		return cityOnFireEndTimes.containsKey(playerId) && cityOnFireEndTimes.get(playerId) > HawkTime.getMillisecond();
	}
	
	/**
	 * 城墙燃烧状态结束判断
	 */
	private void onCityFireEndTick() {
		if(!cityOnFireEndTimes.isEmpty()) {
			long now = HawkTime.getMillisecond();
			//遍历所有破城玩家
			Iterator<Entry<String, Long>> iter = cityOnFireEndTimes.entrySet().iterator();
			while (iter.hasNext()) {
				Entry<String, Long> entry = iter.next();
				if (entry.getValue() <= now) {
					iter.remove();
				}
			}
		}
		
		if (!snowFireEndTimeMap.isEmpty()) {
			long now = HawkTime.getMillisecond();
			//遍历所有破城玩家
			Iterator<Entry<String, Long>> iter = snowFireEndTimeMap.entrySet().iterator();
			while (iter.hasNext()) {
				Entry<String, Long> entry = iter.next();
				if (entry.getValue() <= now) {
					iter.remove();
				}
			}
		}
		
	}
	
	/**
	 * 离线玩家破罩时间检测
	 */
	private void onCityShieldTick() {
		if (playerCityShieldEndTimes.isEmpty()) {
			return;
		}
		
		long now = HawkApp.getInstance().getCurrentTime();
		for (Entry<String, Long> entry: playerCityShieldEndTimes.entrySet()) {
			long endTime = entry.getValue();
			if (endTime > now + TimeUnit.MINUTES.toMillis(10)) {
				continue;
			}
			
			removeCityShield(entry.getKey(), now);
		}
	}
	
	/**
	 * 城防检测
	 * 
	 * @param cityDefInfo
	 * @param currentTime
	 */
	private void checkCityDefBroken(CityDefInfo cityDefInfo, long currentTime) {
		Player player = null;
		double speed = RedisProxy.getInstance().getWallFireSpeed(cityDefInfo.getPlayerId());
		if (speed <= 0) {
			HawkLog.logPrintln("CityManager load wallFireSpeed from redis failed, playerId: {}, speed: {}", cityDefInfo.getPlayerId(), speed);
			player = GlobalData.getInstance().makesurePlayer(cityDefInfo.getPlayerId());
			if (player != null) {
				speed = getWallFireSpeed(player);
			}
			
			if (speed <= 0) {
				HawkLog.logPrintln("CityManager cacul wallFireSpeed failed, playerId: {}, speed: {}, playerName: {}", cityDefInfo.getPlayerId(), speed, player == null ? "null" : player.getName());
				return;
			}
			
			if (cityDefInfo.getOnFireEndTime() > currentTime) {
				RedisProxy.getInstance().updateWallFireSpeed(cityDefInfo.getPlayerId(), speed, GsConst.MONTH_SECONDS);
			}
		}
		
		// 上一次检测城防的时间
		long lastCityDefDecTime = cityDefInfo.getCityDefConsumeTime();
		// 城防燃烧结束时间
		long onFireEndTime = cityDefInfo.getOnFireEndTime();
		
		long onFireTimeLong = onFireEndTime - lastCityDefDecTime;
		// 只计算此刻之前燃烧掉的城防
		if (onFireEndTime > currentTime) {
			onFireTimeLong = currentTime - lastCityDefDecTime;
			cityOnFireEndTimes.put(cityDefInfo.getPlayerId(), onFireEndTime);
		}
		
		double cityDefDec = speed * onFireTimeLong / GsConst.CITY_DEF_RATE;
		int cityDefNow = cityDefInfo.getCityDefVal();
		try {
			// 城防值降到0，直接迁城
			if(cityDefDec >= cityDefNow) {
				WorldPlayerService.getInstance().removeCity(cityDefInfo.getPlayerId(), true);
			} else if (onFireEndTime > currentTime) {
				// 燃烧状态还未结束，重新计算燃烧状态结束时间
				onFireTimeLong = onFireEndTime - lastCityDefDecTime;
				double cityDefConsume = speed * onFireTimeLong / GsConst.CITY_DEF_RATE;
				// 如果燃烧状态结束之前城防值降到0，则记录下城防值降到0的时间，否则不做处理
				if(cityDefConsume >= cityDefNow) {
					long brokenTime = (long) Math.ceil(cityDefNow / speed * GsConst.CITY_DEF_RATE + lastCityDefDecTime);
					playerCityBrokenTime.put(cityDefInfo.getPlayerId(), brokenTime);
				}
			}
			
			if (onFireEndTime < currentTime && player != null) {
				cityDefCalculate(player, false);
			}
			
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	public void addCityShieldInfo(String playerId, long endTime) {
		playerCityShieldEndTimes.put(playerId, endTime);
	}
	
	public void removeCityShieldInfo(String playerId) {
		playerCityShieldEndTimes.remove(playerId);
	}
	
	/**
	 * 城点保护罩破罩
	 * @param playerId
	 * @param currentTime
	 */
	private void removeCityShield(String playerId, long currentTime) {
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		if (player == null) {
			HawkLog.logPrintln("remove city shield failed, makesure player result null, playerId: {}", playerId);
			return;
		}
		
		StatusDataEntity entity = player.getData().getStatusById(Const.EffType.CITY_SHIELD_VALUE);
		long endTime = entity.getEndTime();
		if (endTime <= currentTime) {
			endTime = player.onCityShieldChange(entity, currentTime);
			WorldPlayerService.getInstance().updateWorldPointProtected(playerId, endTime);
		}
		
		player.cityShieldRemovePrepareNotice(entity, endTime - currentTime);
		if (endTime <= currentTime || endTime == Integer.MAX_VALUE) {
			removeCityShieldInfo(playerId);
		}
	}

	/**
	 * 计算城防值消耗
	 * @param player
	 * @param sync
	 */
	public void cityDefCalculate(Player player) {
		cityDefCalculate(player, false);
	}
	
	/**
	 * 计算城防值消耗
	 */
	public void cityDefCalculate(Player player, boolean sync) {
		PlayerBaseEntity playerBaseEntity = player.getPlayerBaseEntity();
		long lastConsumeTime = playerBaseEntity.getCityDefConsumeTime();
		
		// 处于着火状态或待结算状态
		if(lastConsumeTime > 0 && playerBaseEntity.getCityDefVal() > 0) {
			long now = HawkTime.getMillisecond();
			long time = Math.min(now, playerBaseEntity.getOnFireEndTime()); 
			// 城防值小数问题
			double cityDefConsume = getWallFireSpeed(player) * (time - lastConsumeTime) / GsConst.CITY_DEF_RATE;
			decCityDef(player, cityDefConsume);
			playerBaseEntity.setCityDefConsumeTime(now <= playerBaseEntity.getOnFireEndTime() ? now : 0);
			if (playerBaseEntity.getCityDefConsumeTime() == 0) {
				// 着火状态结束
				RedisProxy.getInstance().removeWallFireSpeed(player.getId());
				LogUtil.logCityWallDataChange(player, CityWallChangeType.CITY_ONFIRE_EDN);
				player.getPush().syncCityDef(false);
				return;
			}
		}
		
		// 同步最新城防值
		if (sync) {
			player.getPush().syncCityDef(false);
		}
	}
	
	/**
	 * 城墙着火，着火状态结束时间叠加
	 */
	public void cityOnFire(Player atkPlayer, Player defPlayer, List<Integer> heroIdList) {
		PlayerBaseEntity playerBaseEntity = defPlayer.getPlayerBaseEntity();
		int cityDefReduce = 0;
		
		EffectParams effParams = new EffectParams();
		effParams.setHeroIds(heroIdList);
		
		if (atkPlayer != null && heroIdList != null) {
			cityDefReduce = atkPlayer.getEffect().getEffVal(EffType.WAR_ATK_CITY_CITYDEF_HURT, effParams);
		}
		
		int cityDefBefore = playerBaseEntity.getCityDefVal(); 
		int cityDefAfter = cityDefBefore  - cityDefReduce;
		playerBaseEntity.setCityDefVal(cityDefAfter > 0 ? cityDefAfter : 0);
		
		long now = HawkApp.getInstance().getCurrentTime();
		long fireEndTime = playerBaseEntity.getOnFireEndTime();
		long remainTime = 0;
		if (snowFireEndTimeMap.containsKey(defPlayer.getId())) {
			remainTime = snowFireEndTimeMap.get(defPlayer.getId()) - now;
			if (remainTime > 0) {
				fireEndTime -= remainTime;
			}
		}
		
		long newEndTime = GameUtil.getOnFireEndTime(fireEndTime);
		if (remainTime > 0) {
			newEndTime += remainTime;
		}
		
		// 城防值减少或城墙着火状态发生变化
		if (cityDefBefore != playerBaseEntity.getCityDefVal() || playerBaseEntity.getOnFireEndTime() < now) {
			LogUtil.logCityWallDataChange(defPlayer, CityWallChangeType.CITY_ONFIRE);
		}
		
		// 不在燃烧状态
		if(now >= playerBaseEntity.getOnFireEndTime()) {
			playerBaseEntity.setCityDefConsumeTime(now);
			playerBaseEntity.setCityDefNextRepairTime(now);
		} else {
			cityDefCalculate(defPlayer);
		}
		
		playerBaseEntity.setOnFireEndTime(newEndTime);
		cityOnFireEndTimes.put(defPlayer.getId(), newEndTime);
		defPlayer.getPush().syncCityDef(true);
		refreshCityBrokenTime(defPlayer, true);
	}
	
	/**
	 * 城墙着火，燃烧时间不受上限控制
	 * 
	 * @param player
	 * @param fireTime
	 */
	public void cityOnFireNoLimit(Player player, long fireTime) {
		if (player == null) {
			return;
		}
		
		PlayerBaseEntity playerBaseEntity = player.getPlayerBaseEntity();
		long now = HawkTime.getMillisecond();
		long newEndTime = playerBaseEntity.getOnFireEndTime();
		
		if (snowFireEndTimeMap.containsKey(player.getId())) {
			long remainTime = snowFireEndTimeMap.get(player.getId()) - now;
			if (remainTime > 0) {
				newEndTime -= remainTime;
			}
		}
		
		// 正在燃烧中。。。。
		if (newEndTime > now) {
			newEndTime += fireTime;
			cityDefCalculate(player);
		} else {
			newEndTime = now + fireTime;
			playerBaseEntity.setCityDefConsumeTime(now);
			playerBaseEntity.setCityDefNextRepairTime(now);
			LogUtil.logCityWallDataChange(player, CityWallChangeType.CITY_ONFIRE);
		}
		
		snowFireEndTimeMap.put(player.getId(), now + fireTime);
		playerBaseEntity.setOnFireEndTime(newEndTime);
		cityOnFireEndTimes.put(player.getId(), newEndTime);
		player.getPush().syncCityDef(true);
		refreshCityBrokenTime(player, true);
	}
	
	/**
	 * 城墙燃烧速度发送变化（领地建成或拆除时、大本升级时调用此接口）
	 * 
	 * @param player 受影响的玩家
	 */
	public void cityWallFireSpeedChange(Player player) {
		if (player == null) {
			return;
		}

		if (!cityOnFireEndTimes.containsKey(player.getId())) {
			return;
		}
		
		try {
			cityDefCalculate(player);
			refreshCityBrokenTime(player, true);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 灭火
	 * @param playerId
	 */
	public void outFire(Player player) {
		cityBrokenRelieve(player.getId());
		
		// 灭火
		cityDefCalculate(player);
		long now = HawkTime.getMillisecond();
		if (player.getPlayerBaseEntity().getOnFireEndTime() > now) {
			player.getData().getPlayerBaseEntity().setOnFireEndTime(now);
			LogUtil.logCityWallDataChange(player, CityWallChangeType.CITY_OUTFIRE);
		}
		
		player.getData().getPlayerBaseEntity().setCityDefConsumeTime(0);
		RedisProxy.getInstance().removeWallFireSpeed(player.getId());
		// 同步最新城防值
		player.getPush().syncCityDef(true);
		// 通知世界点灭火
		WorldPointService.getInstance().notifyWorldPointOutFire(player.getId());
	}
	
	/**
	 * 修复城防，增加城防值
	 */
	public void repairCity(Player player) {
		// 先计算当前城防值
		cityDefCalculate(player);
		
		PlayerBaseEntity playerBaseEntity = player.getPlayerBaseEntity();
		int cityDefMax = player.getData().getRealMaxCityDef();
		long nextRepairTime = playerBaseEntity.getCityDefNextRepairTime();
		int cityDefVal = playerBaseEntity.getCityDefVal();
		long now = HawkTime.getMillisecond();
		// 如果还没到可修复时间，或当前城防值已经达到上限了，则不做处理
		if(nextRepairTime == 0 || nextRepairTime > now || cityDefVal >= cityDefMax) {
			HawkLog.logPrintln("CityManager repairCity trace return, playerId: {}, nextTime: {}, cityDefVal: {}, cityDefMax: {}", player.getId(), nextRepairTime, cityDefVal, cityDefMax);
			return;
		}
		
		// 单次修复增加的城防值
		int repairCD = ConstProperty.getInstance().getWallRepairCd();
		int cityDefAdd = ConstProperty.getInstance().getOnceWallRepair() + player.getData().getEffVal(Const.EffType.REPAIR_CITY_DEF_ADD);
		if(cityDefVal + cityDefAdd >= cityDefMax) {
			playerBaseEntity.setCityDefVal(cityDefMax);
			playerCityDefFractions.remove(player.getId());
			if (playerBaseEntity.getOnFireEndTime() > now) {
				playerBaseEntity.setCityDefNextRepairTime(now + repairCD * 1000L);
			} else {
				playerBaseEntity.setCityDefNextRepairTime(0);
			}
		} else {
			// 修复间隔时间 
			playerBaseEntity.setCityDefNextRepairTime(now + repairCD * 1000L);
			playerBaseEntity.setCityDefVal(cityDefAdd + cityDefVal);
		}
		
		if (cityDefVal != playerBaseEntity.getCityDefVal()) {
			LogUtil.logCityWallDataChange(player, CityWallChangeType.CITY_WALL_REPAIR);
		}
		
		// 同步最新城防值
		player.getPush().syncCityDef(false);
		if(playerCityBrokenTime.containsKey(player.getId())) {
			refreshCityBrokenTime(player, false);
		}
	}
	
	/**
	 * 迁城后立即自动修复城防值
	 */
	public void cityRecover(Player player) {
		AutoMonsterMarchParam autoMarchParam = WorldMarchService.getInstance().getAutoMarchParam(player.getId());
		if (autoMarchParam != null) {
			HawkTaskManager.getInstance().postMsg(player.getXid(), AutoSearchMonsterMsg.valueOf());
		}
		
		cityBrokenRelieve(player.getId());
		playerCityDefFractions.remove(player.getId());
		
		long endTime = player.getPlayerBaseEntity().getOnFireEndTime();
		long now = HawkTime.getMillisecond();
		player.getPlayerBaseEntity().setOnFireEndTime(now);
		player.getPlayerBaseEntity().setCityDefConsumeTime(0);
		player.getPlayerBaseEntity().setCityDefNextRepairTime(0);
		RedisProxy.getInstance().removeWallFireSpeed(player.getId());
		
		int maxCityDef = player.getData().getRealMaxCityDef();
		int curCityDef = player.getPlayerBaseEntity().getCityDefVal();  // 当前城防值
		player.getPlayerBaseEntity().setCityDefVal(maxCityDef);
		// 城防值变化或着火状态发生变化
		if (curCityDef != maxCityDef || endTime > now) {
			LogUtil.logCityWallDataChange(player, CityWallChangeType.CITY_MOVE_RECOVER);
		}

		// 同步状态
		player.getPush().syncCityDef(true);
		HawkLog.logPrintln("city manager recover city, playerId: {}, cityDef: {}", player.getId(), maxCityDef);
	}
	
	/**
	 * 重新计算破城时间
	 */
	private void refreshCityBrokenTime(Player player, boolean store) {
		PlayerBaseEntity playerBaseEntity = player.getPlayerBaseEntity(); 
		long lastCalculateTime = playerBaseEntity.getCityDefConsumeTime();
		if(lastCalculateTime <= 0) {
			cityBrokenRelieve(playerBaseEntity.getPlayerId());
			return;
		}
		
		double speed = getWallFireSpeed(player);
		double cityDefTotalConsume = speed * (playerBaseEntity.getOnFireEndTime() - lastCalculateTime) / GsConst.CITY_DEF_RATE;
		double realCityDef = getDoubleCityDef(player);
		
		long brokenTime = (long) Math.ceil(realCityDef / speed * GsConst.CITY_DEF_RATE + lastCalculateTime);
		if (store) {
			RedisProxy.getInstance().updateWallFireSpeed(player.getId(), speed, GsConst.MONTH_SECONDS);
		}
		
		// 如果着火状态结束之前城防值降到0则记录下城防值降到0的时间
		if(cityDefTotalConsume >= realCityDef) {
			playerCityBrokenTime.put(playerBaseEntity.getPlayerId(), brokenTime);
		} else {
			playerCityBrokenTime.remove(playerBaseEntity.getPlayerId());
		}
	}
	
	/**
	 * 城防值增加
	 * 
	 * @param player
	 * @param cityDefAdd
	 */
	public void increaseCityDef(Player player, int cityDefAdd) {
		int cityDefVal = player.getPlayerBaseEntity().getCityDefVal();
		player.getPlayerBaseEntity().setCityDefVal(cityDefVal + cityDefAdd);
		if (cityDefAdd > 0) {
			LogUtil.logCityWallDataChange(player, CityWallChangeType.CITY_DEF_INC);
		}
		
		long onFireEndTime = player.getPlayerBaseEntity().getOnFireEndTime();
		if(HawkTime.getMillisecond() < onFireEndTime) {
			cityDefCalculate(player);
			refreshCityBrokenTime(player, false);
		}
	}
	
	/**
	 * 移除城点
	 * 
	 * @param playerId
	 */
	public void removeCity(Player player) {
		if (!removeCityPlayers.contains(player.getId())) {
			return;
		}
		
		cityBrokenRelieve(player.getId());
		removeCityPlayers.remove(player.getId());
		long now = HawkTime.getMillisecond();
		player.getPlayerBaseEntity().setOnFireEndTime(now);
		player.getPlayerBaseEntity().setCityDefConsumeTime(0);
		player.getPlayerBaseEntity().setCityDefVal(0);
		RedisProxy.getInstance().removeWallFireSpeed(player.getId());
		LogUtil.logCityWallDataChange(player, CityWallChangeType.CITY_REMOVE);
		
		// 同步城防状态
		if (player.isActiveOnline()) {
			player.getPush().syncCityDef(true);
		} else {
			WorldPlayerService.getInstance().resetCityFireStatus(player, now);
		}
		
		HawkLog.logPrintln("city manager remove city, playerId: {}", player.getId());
	}
	
	/**
	 * 获取实时的城防值
	 * 
	 * @param player
	 * @return
	 */
	public int getRealCityDef(Player player) {
		if (!player.isSessionActive()) {
			cityDefCalculate(player);
		}
		
		return player.getPlayerBaseEntity().getCityDefVal();
	}
	
	/**
	 * 同步城防信息
	 * 
	 * @param playerId
	 */
	public synchronized void moveCity(String playerId, boolean forced) {
		try {
			//没到时间或者已经开始移除,则不加入
			if(!forced && (HawkApp.getInstance().getCurrentTime() < playerCityBrokenTime.get(playerId) || removeCityPlayers.contains(playerId))) {
				return;
			}
			
			//加入到已经移除集合, 防止多次移除 机器人不加入
			if (!WorldRobotService.getInstance().isRobotId(playerId)) {
				removeCityPlayers.add(playerId);
			}
			
			//判断玩家是否在线
			Player player = GlobalData.getInstance().getActivePlayer(playerId);
			if (player != null) {
				player.dealMsg(MsgId.CITY_DEF_RECOVER, new CityBrokenMoveInvoker(player, playerCityBrokenTime.get(playerId), removeCityPlayers, forced));
			} else {
				WorldPlayerService.getInstance().removeCity(playerId, true);
			}
			
			String guildId = GuildService.getInstance().getPlayerGuildId(playerId);
			if (!HawkOSOperator.isEmptyString(guildId)) {
				int authority = GuildService.getInstance().getPlayerGuildAuthority(playerId);		
				GuildService.getInstance().notifyGuildFavouriteRedPoint(guildId, GsConst.GuildFavourite.TYPE_GUILD_MEMBER, authority);
			}
			
			HawkLog.logPrintln("city manager move city, playerId: {}, action: {}, forced: {}", playerId, player != null ? "move" : "remove", forced);
			
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 获取城防值消耗速度
	 * 
	 * @return
	 */
	public double getWallFireSpeed(Player player) {
		// 城防值消耗
		BuildingCfg buildingCfg = player.getData().getBuildingCfgByType(BuildingType.CONSTRUCTION_FACTORY);
		if (buildingCfg == null) {
			return -1;
		}
		
		int wallFireSpeed = buildingCfg.getWallFireSpeed();
		try {
			// 黑土地城防值消耗速度
			int pointId = WorldPlayerService.getInstance().getPlayerPos(player.getId());
			if(WorldPointService.getInstance().isInCapitalArea(pointId)) {
				wallFireSpeed = buildingCfg.getWallFireSpeedOnBlackLand();
			}
			
			if (wallFireSpeed == 0) {
				return -1;
			}
			
			int effVal = 0;
			if (player != null) {
				List<Integer> heroIdList = player.getAllHero().stream().map(PlayerHero::getCfgId).collect(Collectors.toList());
				EffectParams effParams = new EffectParams();
				effParams.setHeroIds(heroIdList);
				
				effVal = player.getEffect().getEffVal(EffType.CITY_FIRE_SPD) - player.getEffect().getEffVal(EffType.CITY_FIRE_SPEED_SLOW, effParams);
			}
			
			// 实际燃烧速度 = 基础燃烧速度 *（1 + 作用值/10000）
			double speed = wallFireSpeed * (1 + effVal * GsConst.EFF_PER);
			return speed;
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		return wallFireSpeed;
	}
	
	/**
	 * 更新城防值，包括实际城防值的小数部分
	 * 
	 * @param player
	 * @param cityDefValAdd
	 */
	private void decCityDef(Player player, double cityDefConsume) {
		double realCityDef = getDoubleCityDef(player);
		realCityDef -= cityDefConsume;
		int cityDefIntVal = (int) Math.floor(realCityDef);
		int oldCityDef = player.getPlayerBaseEntity().getCityDefVal();
		player.getPlayerBaseEntity().setCityDefVal(cityDefIntVal < 0 ? 0 : cityDefIntVal);
		if (oldCityDef != player.getPlayerBaseEntity().getCityDefVal()) {
			LogUtil.logCityWallDataChange(player, CityWallChangeType.CITY_DEF_CONSUME);
		}
		
		if (cityDefIntVal > 0) {
			playerCityDefFractions.put(player.getId(), realCityDef - cityDefIntVal);
		}
		
		HawkLog.logPrintln("cityDef update by consume, playerId: {}, value: {}", player.getId(), player.getPlayerBaseEntity().getCityDefVal());
	}
	
	/**
	 * 获取玩家真实的城防值，包含小数部分
	 * 
	 * @param player
	 */
	private double getDoubleCityDef(Player player) {
		double realCityDef = player.getPlayerBaseEntity().getCityDefVal();
		if(playerCityDefFractions.containsKey(player.getId())) {
			realCityDef += playerCityDefFractions.get(player.getId());
		}
		
		return realCityDef;
	}
	
	/**
	 * 破城时间点移除
	 * 
	 * @param playerId
	 */
	private void cityBrokenRelieve(String playerId) {
		playerCityBrokenTime.remove(playerId);
		cityOnFireEndTimes.remove(playerId);
	}
	
	/**
	 * 城防燃烧速度redis存储
	 * 
	 * @param player
	 */
	public void wallFireSpeedStore(Player player) {
		if (!cityOnFireEndTimes.containsKey(player.getId())) {
			return;
		}
		
		try {
			double speed = getWallFireSpeed(player);
			RedisProxy.getInstance().updateWallFireSpeed(player.getId(), speed, GsConst.MONTH_SECONDS);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
}
