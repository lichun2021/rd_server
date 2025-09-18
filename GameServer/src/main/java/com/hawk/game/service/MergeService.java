package com.hawk.game.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBManager;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.task.HawkTaskManager;
import org.hawk.thread.HawkTask;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.slf4j.LoggerFactory;

import com.hawk.activity.config.MergeServerTimeCfg;
import com.hawk.common.AccountRoleInfo;
import com.hawk.game.GsConfig;
import com.hawk.game.entity.GuildInfoObject;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.RedisKey;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.invoker.CityRecoverMsgInvoker;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.GsConst.WorldTaskType;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.object.Point;
import com.hawk.game.world.service.WorldPlayerService;
import com.hawk.game.world.service.WorldPointService;
import com.hawk.game.world.thread.WorldTask;
import com.hawk.game.world.thread.WorldThreadScheduler;
import com.hawk.gamelib.GameConst.MsgId;

public class MergeService {

	/**
	 * 清除空的联盟数据
	 */
	public static void clearEmptyGuild() {
		List<GuildInfoObject> entities = new ArrayList<>();
		GuildService.getInstance().getGuildEntities(entities);
		for (GuildInfoObject guildInfo : entities) {
			try {
				int memberCnt = GuildService.getInstance().getGuildMemberNum(guildInfo.getId());
				if (memberCnt <= 0) {
					HawkLog.logPrintln("mergeService clearEmptyGuild, guildId: {}", guildInfo.getId());
					GuildService.getInstance().onDissmiseGuild(guildInfo.getId(), null);
				}
			} catch (Exception e) {
				HawkException.catchException(e, guildInfo != null ? guildInfo.getId() : "");
			}
		}
		
		HawkLog.logPrintln("mergeService clearEmptyGuild end");
	}
	
	/**
	 * 创建玩家点
	 * @param num
	 * @param addRoleFlag
	 */
	public static void createPoint(int num, boolean addRoleFlag) {
		/*String sql = "select playerId from building b "
				+ "where b.type = 2010 and b.buildingCfgId > 201007 and  "
				+ "exists (select 1 from player_base pb where pb.playerId = b.playerId and pb.cityDefVal > 0)  limit 0," + num;*/
		String sql = "select playerId from player_base where cityDefVal > 0  order by level desc limit 0," + num; 
		Session session = HawkDBManager.getInstance().getSession();
		SQLQuery costTimeMs = session.createSQLQuery(sql);// 1268		
		@SuppressWarnings("unchecked")
		List<String> ids = costTimeMs.list();
		session.flush();
		session.close();
		AtomicInteger counter = new AtomicInteger();
		long startTime = HawkTime.getMillisecond();
		AtomicInteger postExtraTaskCounter = new AtomicInteger();  
		for (String playerId : ids) {
			HawkTaskManager.getInstance().postExtraTask(new HawkTask() {				
				@Override
				public Object run() {
					createPlayerAndCreatePoint(playerId, addRoleFlag);
					int count = counter.incrementAndGet();
					if (count >= ids.size()) {
						HawkLog.logPrintln("post extra task is finish costTime:{}", HawkTime.getMillisecond() - startTime);
					}
					postExtraTaskCounter.incrementAndGet();
					return null;
				}
			});
		}
		
		HawkLog.logPrintln("load player num:{}", ids.size());
		long waitStartTime = HawkTime.getMillisecond();
		//等20分钟或者 全部抛到额外任务.
		while (postExtraTaskCounter.get() < ids.size() && HawkTime.getMillisecond() - waitStartTime < 20 * 60 * 1000) {
			HawkOSOperator.sleep();
		}
		HawkLog.logPrintln("load player num:{}, costTime:{}", ids.size(),HawkTime.getMillisecond() - waitStartTime);
	}
	
	@SuppressWarnings("deprecation")
	private static void createPlayerAndCreatePoint(String playerId, boolean addAccountRole) {
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		if (player == null) {
			HawkLog.errPrintln("mergeservice createPlayerAndCreatePoint makesure player null, playerId: {}", playerId);
		}
		if (addAccountRole) {
			AccountRoleInfo accountRoleInfo = GlobalData.getInstance().getAccountRoleInfo(player.getId());
			if (accountRoleInfo == null) {
				accountRoleInfo = AccountRoleInfo.newInstance().openId(player.getOpenId()).playerId(player.getId())
						.serverId(player.getServerId()).platform(player.getPlatform()).registerTime(player.getCreateTime());
			}
			
			try {
				accountRoleInfo.playerName(player.getName()).playerLevel(player.getLevel()).cityLevel(player.getCityLevel())
				.vipLevel(player.getVipLevel()).battlePoint(player.getPower()).activeServer(GsConfig.getInstance().getServerId())
				.icon(player.getIcon()).loginWay(player.getEntity().getLoginWay()).loginTime(HawkTime.getMillisecond())
				.logoutTime(player.getLogoutTime()).cityPlantLevel(player.getCityPlantLv());
				accountRoleInfo.pfIcon(PlayerImageService.getInstance().getPfIcon(player));
			} catch (Exception e) {
				HawkException.catchException(e, player.getId());
			}
			GlobalData.getInstance().addOrUpdateAccountRoleInfo(accountRoleInfo);
		}
		
		WorldThreadScheduler.getInstance().postWorldTask(new WorldTask(WorldTaskType.MOVE_CITY){			
			@Override
			public boolean onInvoke() {
				WorldPlayerService worldPlayerService = WorldPlayerService.getInstance();
				boolean isFirst = player.getData().getPlayerBaseEntity().getOnFireEndTime() <= 0;
				worldPlayerService.removeCity(player.getId(), true);
				Point newPoint = worldPlayerService.randomSettlePoint(player, isFirst);
				if (newPoint != null) {
					// 生成玩家城堡占用点
					WorldPoint targetPoint = new WorldPoint(newPoint.getX(), newPoint.getY(), newPoint.getAreaId(), newPoint.getZoneId(), WorldPointType.PLAYER_VALUE);
					targetPoint.initPlayerInfo(player.getData());
					targetPoint.setProtectedEndTime(0);

					// 创建玩家使用的世界点信息
					if (!WorldPointService.getInstance().createWorldPoint(targetPoint)) {
						LoggerFactory.getLogger("Server").error("random settle point failed, playerId: {}, pos: ({}, {}), isBornPos: {}", player.getId(), targetPoint.getX(), targetPoint.getY(), isFirst);
						return false;
					}
																				
					// 投递回玩家线程执行
					player.dealMsg(MsgId.CITY_DEF_RECOVER, new CityRecoverMsgInvoker(player));
					
					if (!isFirst) {
						int cityDef = player.getPlayerBaseEntity().getCityDefVal();
						player.sendProtocol(HawkProtocol.valueOf(cityDef > 0 ? HP.code.NEWLY_MOVE_CITY_NOTIFY_PUSH : HP.code.MOVE_CITY_NOTIFY_PUSH));
						worldPlayerService.sendCityWorldPoint(player, newPoint.getX(), newPoint.getY());
					}

					// 新手保护时间
					long cityShieldTime = player.getData().getCityShieldTime();
					if (cityShieldTime > HawkTime.getMillisecond()) {
						worldPlayerService.updateWorldPointProtected(player.getId(), cityShieldTime);
					}
					LoggerFactory.getLogger("Server").info("player random world point success, playerId: {}, x: {} , y: {}, isBornPos: {}", player.getId(), newPoint.getX(), newPoint.getY(), isFirst);
				} else {
					LoggerFactory.getLogger("Server").error("player random world point failed, playerId: {}, isBornPos: {}", player.getId(), isFirst);
				}
				
				return true;
			}
		});
	}
	
	/**
	 * 更新合服信息，给idip中转服（GM服）提供信息
	 */
	public static void updateMergeServerInfo() {
		ConfigIterator<MergeServerTimeCfg> cfgIterator = HawkConfigManager.getInstance().getConfigIterator(MergeServerTimeCfg.class);
		while (cfgIterator.hasNext()) {
			try {
				MergeServerTimeCfg timeCfg = cfgIterator.next();
				if (!HawkTime.isToday(timeCfg.getMergeTimeValue())) {
					continue;
				}
				//主服，从服
				String master = timeCfg.getMasterServer();
				for (String slave : timeCfg.getSlaveServerIdList()) {
					RedisProxy.getInstance().getRedisSession().hSet(RedisKey.MERGE_SLAVE_MASTER, slave, master, GsConst.DAY_SECONDS * 30);
				}
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
	}
	
}
