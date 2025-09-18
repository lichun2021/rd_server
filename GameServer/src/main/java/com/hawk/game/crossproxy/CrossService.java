package com.hawk.game.crossproxy;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import com.hawk.activity.ActivityManager;
import com.hawk.activity.event.impl.NewStartActiveEvent;
import com.hawk.activity.type.impl.newStart.cfg.NewStartBaseCfg;
import com.hawk.common.AccountRoleInfo;
import com.hawk.game.activity.impl.inherit.InheritNewService;
import com.hawk.game.config.*;
import com.hawk.game.entity.EquipResearchEntity;
import com.hawk.game.module.homeland.rank.HomeLandService;
import com.hawk.game.msg.xhjz.XHJZBackServerMsg;
import com.hawk.game.msg.xhjz.XHJZExitCrossInstanceMsg;
import com.hawk.game.msg.xhjz.XHJZMoveBackCrossPlayerMsg;
import com.hawk.game.msg.xhjz.XHJZPrepareExitCrossInstanceMsg;
import com.hawk.game.msg.xqhx.XQHXBackServerMsg;
import com.hawk.game.msg.xqhx.XQHXExitCrossInstanceMsg;
import com.hawk.game.msg.xqhx.XQHXMoveBackCrossPlayerMsg;
import com.hawk.game.msg.xqhx.XQHXPrepareExitCrossInstanceMsg;
import com.hawk.game.player.hero.PlayerHero;
import com.hawk.game.player.supersoldier.SuperSoldier;
import com.hawk.game.protocol.*;
import com.hawk.game.service.guildTeam.ipml.TBLYGuildTeamManager;
import com.hawk.game.service.guildTeam.model.GuildTeamData;
import com.hawk.game.service.guildTeam.model.GuildTeamPlayerData;
import com.hawk.game.service.tblyTeam.TBLYSeasonService;
import com.hawk.game.service.tblyTeam.TBLYWarService;
import com.hawk.game.service.tblyTeam.state.TBLYWarStateEnum;
import com.hawk.game.service.xqhxWar.XQHXWarService;
import org.hawk.annotation.ProtocolHandler;
import org.hawk.app.HawkAppObj;
import org.hawk.config.HawkConfigManager;
import org.hawk.db.HawkDBEntity;
import org.hawk.delay.HawkDelayAction;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.net.session.HawkSession;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.task.HawkTaskManager;
import org.hawk.thread.HawkTask;
import org.hawk.thread.HawkThreadPool;
import org.hawk.tickable.HawkPeriodTickable;
import org.hawk.tuple.HawkTuple2;
import org.hawk.xid.HawkXID;

import com.hawk.common.ServerStatus;
import com.hawk.game.GsApp;
import com.hawk.game.GsConfig;
import com.hawk.game.crossactivity.CrossActivityService;
import com.hawk.game.crossproxy.model.CsPlayer;
import com.hawk.game.crossproxy.model.CsPlayerData;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.gmproxy.GmProxyHelper;
import com.hawk.game.log.DungeonRedisLog;
import com.hawk.game.manager.AssembleDataManager;
import com.hawk.game.module.dayazhizhan.marchserver.service.DYZZMatchService;
import com.hawk.game.module.dayazhizhan.playerteam.cfg.DYZZWarCfg;
import com.hawk.game.module.dayazhizhan.playerteam.msg.DYZZBackServerMsg;
import com.hawk.game.module.dayazhizhan.playerteam.msg.DYZZExitCrossInstanceMsg;
import com.hawk.game.module.dayazhizhan.playerteam.msg.DYZZMoveBackCrossPlayerMsg;
import com.hawk.game.module.dayazhizhan.playerteam.msg.DYZZPrepareExitCrossInstanceMsg;
import com.hawk.game.module.dayazhizhan.playerteam.service.DYZZGameRoomData;
import com.hawk.game.module.dayazhizhan.playerteam.service.DYZZPlayerData;
import com.hawk.game.module.dayazhizhan.playerteam.service.DYZZRedisData;
import com.hawk.game.module.dayazhizhan.playerteam.service.DYZZService;
import com.hawk.game.module.lianmengyqzz.march.cfg.YQZZWarConstCfg;
import com.hawk.game.module.lianmengyqzz.march.data.global.YQZZGameData;
import com.hawk.game.module.lianmengyqzz.march.data.global.YQZZJoinPlayerData;
import com.hawk.game.module.lianmengyqzz.march.msg.YQZZBackServerMsg;
import com.hawk.game.module.lianmengyqzz.march.msg.YQZZExitCrossInstanceMsg;
import com.hawk.game.module.lianmengyqzz.march.msg.YQZZMoveBackCrossPlayerMsg;
import com.hawk.game.module.lianmengyqzz.march.msg.YQZZPrepareExitCrossInstanceMsg;
import com.hawk.game.module.lianmengyqzz.march.service.YQZZMatchService;
import com.hawk.game.module.material.MeterialTransportService;
import com.hawk.game.msg.cross.BackServerMsg;
import com.hawk.game.msg.cross.ExitCrossMsg;
import com.hawk.game.msg.cross.MoveBackCrossPlayerMsg;
import com.hawk.game.msg.cross.PrepareExitCrossMsg;
import com.hawk.game.msg.cyborg.CyborgBackServerMsg;
import com.hawk.game.msg.cyborg.CyborgExitCrossInstanceMsg;
import com.hawk.game.msg.cyborg.CyborgMoveBackCrossPlayerMsg;
import com.hawk.game.msg.cyborg.CyborgPrepareExitCrossInstanceMsg;
import com.hawk.game.msg.starwars.StarWarsBackServerMsg;
import com.hawk.game.msg.starwars.StarWarsExitCrossInstanceMsg;
import com.hawk.game.msg.starwars.StarWarsPrepareExitCrossInstanceMsg;
import com.hawk.game.msg.tiberium.TiberiumBackServerMsg;
import com.hawk.game.msg.tiberium.TiberiumExitCrossInstanceMsg;
import com.hawk.game.msg.tiberium.TiberiumMoveBackCrossPlayerMsg;
import com.hawk.game.msg.tiberium.TiberiumPrepareExitCrossInstanceMsg;
import com.hawk.game.nation.NationService;
import com.hawk.game.nation.tech.NationTechCenter;
import com.hawk.game.player.Player;
import com.hawk.game.player.cache.PlayerDataKey;
import com.hawk.game.player.cache.PlayerDataSerializer;
import com.hawk.game.president.PresidentCity;
import com.hawk.game.president.PresidentFightService;
import com.hawk.game.protocol.Common.KeyValuePairStr;
import com.hawk.game.protocol.Cross.CrossMoveBackReq;
import com.hawk.game.protocol.Cross.InnerBackServerReq;
import com.hawk.game.protocol.Cross.InnerEnterCrossReq;
import com.hawk.game.protocol.Cross.RpcCommonResp;
import com.hawk.game.protocol.CyborgWar.CWTimeChoose;
import com.hawk.game.protocol.CyborgWar.CyborgWarInnerBackServerReq;
import com.hawk.game.protocol.CyborgWar.CyborgWarMoveBackReq;
import com.hawk.game.protocol.DYZZWar.PBDYZZWarInnerBackServerReq;
import com.hawk.game.protocol.DYZZWar.PBDYZZWarMoveBackReq;
import com.hawk.game.protocol.Immgration.ImmgrationServerInfoDetailResp;
import com.hawk.game.protocol.Immgration.PBBackImmgrationTargetServerRankResp;
import com.hawk.game.protocol.Immgration.PBCrossBackImmgrationTargetServerRankReq;
import com.hawk.game.protocol.National.NationbuildingType;
import com.hawk.game.protocol.Player.CrossPlayerStruct;
import com.hawk.game.protocol.President.PresidentInfoSync;
import com.hawk.game.protocol.Rank.RankType;
import com.hawk.game.protocol.StarWarsOfficer.StarWarsSearchPlayerReq;
import com.hawk.game.protocol.StarWarsOfficer.StarWarsSearchPlayerResp;
import com.hawk.game.protocol.SysProtocol.CsPlayerDataSync;
import com.hawk.game.protocol.TiberiumWar.TiberiumWarInnerBackServerReq;
import com.hawk.game.protocol.TiberiumWar.TiberiumWarMoveBackReq;
import com.hawk.game.protocol.World.FetchInviewWorldPoint;
import com.hawk.game.protocol.World.MarchData;
import com.hawk.game.protocol.World.MarchEvent;
import com.hawk.game.protocol.World.MarchEventSync;
import com.hawk.game.protocol.World.PlayerEnterWorld;
import com.hawk.game.protocol.World.WorldMarchPB;
import com.hawk.game.protocol.World.WorldMarchRelation;
import com.hawk.game.protocol.World.WorldPointPB;
import com.hawk.game.protocol.World.WorldPointSync;
import com.hawk.game.protocol.YQZZ.CrossNationTechSkillUse;
import com.hawk.game.protocol.YQZZWar.PBYQZZWarInnerBackServerReq;
import com.hawk.game.protocol.YQZZWar.PBYQZZWarMoveBackReq;
import com.hawk.game.rank.RankService;
import com.hawk.game.service.ImmgrationService;
import com.hawk.game.service.cyborgWar.CWPlayerData;
import com.hawk.game.service.cyborgWar.CWRoomData;
import com.hawk.game.service.cyborgWar.CWTeamJoinData;
import com.hawk.game.service.cyborgWar.CyborgWarRedis;
import com.hawk.game.service.cyborgWar.CyborgWarService;
import com.hawk.game.service.starwars.SWPlayerData;
import com.hawk.game.service.starwars.SWRoomData;
import com.hawk.game.service.starwars.StarWarsActivityService;
import com.hawk.game.util.BuilderUtil;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.GsConst.PlayerCrossStatus;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.march.IWorldMarch;
import com.hawk.game.world.service.WorldPlayerService;
import com.hawk.game.world.service.WorldPointService;
import com.hawk.game.world.thread.WorldThreadScheduler;
import com.hawk.game.world.thread.tasks.CrossEndRemoveCityTask;

public class CrossService extends HawkAppObj {
	/**
	 * 等待通知通报的玩家id集合
	 */
	private Map<String, String> waitSyncPlayer;

	/**
	 * 从别的服过来的玩家
	 */
	private Map<String, String> immigrationPlayers;

	/**
	 * 跨服出去的玩家
	 */
	private Map<String, String> emigrationPlayers;
	/**
	 * 发起退服的玩家.
	 */
	private Map<String, Long> exitCrossPlayers;
	/**
	 * 发起泰伯利亚退出.
	 */
	private Map<String, Long> exitTimberiumPlayers;
	/**
	 * 发起星球大战.
	 */
	private Map<String, Long> exitStarWarsPlayers;
	/**
	 * 发起赛博退出.
	 */
	private Map<String, Long> exitCyborgPlayers;
	/**
	 * 发起星海退出.
	 */
	private Map<String, Long> exitXhjzgPlayers;

	/**
	 * 发起先驱回响退出.
	 */
	private Map<String, Long> exitXqhxPlayers;
	
	/**
	 * 发起达雅退出
	 */
	private Map<String, Long> exitDyzzPlayers;
	
	
	/**
	 * 发起达雅退出
	 */
	private Map<String, Long> exitYqzzPlayers;
	
	/**
	 * 单例对象
	 */
	private static CrossService instance = null;

	/**
	 * 获取实体对象
	 * 
	 * @return
	 */
	public static CrossService getInstance() {
		return instance;
	}
	
	/**
	 * 记录玩家的session，用于踢号的时候.使用的时候请确保你已读懂此逻辑.
	 */
	private Map<String, HawkSession> playerIdSession;
	/**
	 * 玩家主动跨服的时间记录.
	 */
	private Map<String, Integer> prepareCrossTime;
	/**
	 * 默认构造
	 * 
	 * @param xid
	 */
	public CrossService(HawkXID xid) {
		super(xid);
		// 设置实例
		instance = this;
	}

	/**
	 * 初始化跨服通信
	 * 
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public boolean init() {
						
		waitSyncPlayer = new ConcurrentHashMap<String, String>();

		emigrationPlayers = new ConcurrentHashMap<>(100);
		Map<String, String> redisToCrossPlayers = RedisProxy.getInstance()
				.getEmigrationPlayers(GsConfig.getInstance().getServerId());
		emigrationPlayers.putAll(redisToCrossPlayers);

		immigrationPlayers = new ConcurrentHashMap<>(100);
		Map<String, String> redisFromCrossPlayers = RedisProxy.getInstance()
				.getImmigrationPlayer(GsConfig.getInstance().getServerId());
		immigrationPlayers.putAll(redisFromCrossPlayers);
		
		/**玩家Id和连接*/
		playerIdSession = new ConcurrentHashMap<>();
		
		/**玩家主动跨服的时间.*/
		prepareCrossTime = new ConcurrentHashMap<String, Integer>();
		
		// 把跨服过来的玩家添加到globalData里面去.
		GlobalData globalData = GlobalData.getInstance();
		for (Entry<String, String> entry : immigrationPlayers.entrySet()) {
			try {
				Player crossPlayer = globalData.makesurePlayer(entry.getKey());
				globalData.updateAccountInfo(crossPlayer.getPuid(), crossPlayer.getServerId(), crossPlayer.getId(), -1,
					crossPlayer.getName(), false);
			} catch (Exception e) {
				HawkException.catchException(e);
			} 
		}

		// 定时通知本体所在服从redis落地到mysql
		long csEntityPeriod = GsConfig.getInstance().getCsEntityPeriod();
		if (csEntityPeriod > 0) {
			addTickable(new HawkPeriodTickable(csEntityPeriod, csEntityPeriod) {
				@Override
				public void onPeriodTick() {
					//检测数据同步
					notifyPlayerDataSync();
					//检测session
					CsSessionManager.getInstance().onTick();
				}
			});
		}

		exitCrossPlayers = new ConcurrentHashMap<>();
		exitTimberiumPlayers = new ConcurrentHashMap<String, Long>();
		exitStarWarsPlayers = new ConcurrentHashMap<String, Long>();
		exitCyborgPlayers = new ConcurrentHashMap<String, Long>();
		exitDyzzPlayers = new ConcurrentHashMap<String, Long>();
		exitYqzzPlayers = new ConcurrentHashMap<String, Long>();
		exitXhjzgPlayers = new ConcurrentHashMap<String, Long>();
		exitXqhxPlayers = new ConcurrentHashMap<String, Long>();
		// 定时检测尝试退出退出跨服的玩家.
		long crossPeriodTime = GameConstCfg.getInstance().getExitCrossPeriodTime();
		if (crossPeriodTime > 0) {
			addTickable(new HawkPeriodTickable(crossPeriodTime, crossPeriodTime) {

				@Override
				public void onPeriodTick() {
					//航海远征.
					checkExitCrossPlayers();
					
					//泰伯利亚
					checkExitTiberiumPlayers();
					
					//星球大战
					checkExitStarWarsPlayers();
					
					//赛博
					checkExitCyborgPlayers();
					
					//达雅之战
					checkExitDyzzPlayers();
					
					//月球之战
					checkExitYqzzPlayers();

					//星海激战
					checkExitXhjzPlayers();
					
					//先驱回响
					checkExitXqhxPlayers();
				}
			});
		}
		

		// 同步回来所有有差异的数据实体
		syncAllCsPlayerData();
		
		//尝试修复玩家.
		boolean fixPlayer = tryFixCrossPlayers();
		if (!fixPlayer) {
			return false;
		}			
		
		//初始化跨服连接.
		this.initCrossProxy();		
		
		return true;
	}

	private boolean tryFixCrossPlayers() {
		Map<String, Integer> map = RedisProxy.getInstance().getAllPlayerCrossStatus(GsConfig.getInstance().getServerId());
		if (map == null) {
			return false;
		}
		
		for (Entry<String, Integer> entry : map.entrySet()) {
			startServerFixCrossPlayer(GsConfig.getInstance().getServerId(), entry.getKey(), entry.getValue());
		}
		
		return true;
	}
	
	/**
	 * 登录的时候尝试修复泰伯利亚玩家.
	 * @return
	 */
	public boolean loginFixTiberiumPlayer(String serverId, String playerId) {
		int status = RedisProxy.getInstance().getPlayerCrossStatus(serverId, playerId);
		if (status < 0) {
			DungeonRedisLog.log(playerId,"playerId:{} getPlayerCrossStatus fail", playerId);

			return false;
		}
		// 只有在跨服中的状态才需要处理.
		if (status == PlayerCrossStatus.PREPARE_EXIT_CROSS) {
			// 把数据从redis反序列化回来.
			PlayerDataSerializer.csSyncPlayerData(playerId, true);

			// 从跨出玩家集合删除
			this.removeEmigrationPlayer(playerId);

			return true;
		} else {
			boolean needFix = false;
			if(TBLYSeasonService.getInstance().isPlayerInSeason(playerId)){
				TBLYWarStateEnum stateEnum = TBLYSeasonService.getInstance().getState();
				if(stateEnum != TBLYWarStateEnum.SEASON_WAR_OPEN){
					DungeonRedisLog.log(playerId,"tiberium player check need fix state is not battle state:{}, playerId:{}, isInLeaguaWar:{}", stateEnum, playerId, true);
					needFix = true;
				}
			}else {
				TBLYWarStateEnum stateEnum = TBLYWarService.getInstance().getState();
				if(stateEnum != TBLYWarStateEnum.BATTLE){
					DungeonRedisLog.log(playerId,"tiberium player check need fix state is not battle state:{}, playerId:{}, isInLeaguaWar:{}", stateEnum, playerId, false);
					needFix = true;
				}else {
					GuildTeamPlayerData playerData = TBLYGuildTeamManager.getInstance().load(playerId);
					if(playerData == null){
						DungeonRedisLog.log(playerId,"tiberium player check need fix twPlayerIsNull, playerId:{}, isInLeaguaWar:{}", playerId, false);
						needFix = true;
					}else {
						GuildTeamData teamData = TBLYWarService.getInstance().getBattleTeam(playerData.teamId);
						if(teamData == null){
							int termId = TBLYWarService.getInstance().getTermId();
							DungeonRedisLog.log(playerId,"tiberium player check need fix twTeamDataIsNull, playerId:{}, termId:{}, isInLeaguaWar:{}", playerId, termId, playerId, false);
							needFix = true;
						}else {
							List<TiberiumWar.WarTimeChoose> timeList = TBLYWarService.getInstance().getChooses();
							if(teamData.timeIndex > 0 && timeList.size() >= teamData.timeIndex){
								TiberiumWar.WarTimeChoose battleTime = timeList.get(teamData.timeIndex - 1);
								long warStartTime = battleTime.getTime();
								long warEndTime = warStartTime + TiberiumConstCfg.getInstance().getWarOpenTime();
								long curTime = HawkTime.getMillisecond();
								if(curTime > warEndTime + TiberiumConstCfg.getInstance().getForceMoveBackTime()){
									DungeonRedisLog.log(playerId,"tiberium player check need fix curTime:{}, endTime:{} id:{}", curTime, warEndTime, playerId);
									needFix = true;
								}
							}
						}
					}
				}
			}


			if (needFix) {
				DungeonRedisLog.log(playerId,"tiberium player login fix id:{}", playerId);
				String toServerId = this.getEmigrationPlayerServerId(playerId);
				// 把数据从redis反序列化回来.
				PlayerDataSerializer.csSyncPlayerData(playerId, true);
				// 清除redis信息.
				this.removeEmigrationPlayer(playerId);
				// 清理本地的信息,
				RedisProxy.getInstance().removeImmigrationPlayer(toServerId, playerId);
				// 清理对方服的信息.
				HawkTaskManager.getInstance().postExtraTask(new HawkTask() {

					@Override
					public Object run() {
						GmProxyHelper.proxyCall(toServerId, "tiberiumMoveBack", "opt=4&playerId" + playerId, 2000);
						return null;
					}
				});

				return true;
			}

			return false;
		}
//		int status = RedisProxy.getInstance().getPlayerCrossStatus(serverId, playerId);
//		if (status < 0) {
//			DungeonRedisLog.log(playerId,"playerId:{} getPlayerCrossStatus fail", playerId);
//
//			return false;
//		}
//
//		// 只有在跨服中的状态才需要处理.
//		if (status == PlayerCrossStatus.PREPARE_EXIT_CROSS) {
//			// 把数据从redis反序列化回来.
//			PlayerDataSerializer.csSyncPlayerData(playerId, true);
//
//			// 从跨出玩家集合删除
//			this.removeEmigrationPlayer(playerId);
//
//			return true;
//		} else {
//			// 判断一下时间. 时间过期了则修复.
//			TiberiumWarService tiberiumWarService = TiberiumWarService.getInstance();
//			TiberiumConstCfg constConfig = TiberiumConstCfg.getInstance();
//			boolean needFix = false;
//			String guildId = GuildService.getInstance().getPlayerGuildId(playerId);
//			boolean isInLeaguaWar = TiberiumLeagueWarService.getInstance().isJointLeaguaWar(guildId);
//			int termId = 0;
//			if (!isInLeaguaWar) {
//				termId = TiberiumWarService.getInstance().getTermId();
//			} else {
//				TLWActivityData tlwActivityInfo = TiberiumLeagueWarService.getInstance().getActivityInfo();
//				termId = tlwActivityInfo.getMark();
//			}
//			TWPlayerData twPlayerData = RedisProxy.getInstance().getTWPlayerData(playerId, termId);
//			if (twPlayerData == null || twPlayerData.getEnterTime() <= 0) {
//				DungeonRedisLog.log(playerId,"tiberium player check need fix twPlayerIsNull:{}, playerId:{}, isInLeaguaWar:{}", twPlayerData == null, playerId, isInLeaguaWar);
//				needFix = true;
//			} else {
//				// 想节省redis访问发现判断太鸡儿难做了，就这样吧.
//				TWGuildData twGuildData = RedisProxy.getInstance().getTWGuildData(twPlayerData.getGuildId(), termId);
//				if (twGuildData == null) {
//					DungeonRedisLog.log(playerId,"tiberium player check need fix guildId:{}, termId:{}, playerId:{}, isInLeaguaWar:{}", twPlayerData.getGuildId(), termId, playerId, isInLeaguaWar);
//					needFix = true;
//				} else {
//					TWRoomData twRoomData = RedisProxy.getInstance().getTWRoomData(twGuildData.getRoomId(), termId);
//					if (twRoomData == null) {
//						DungeonRedisLog.log(playerId,"tiberium player check need fix twRoomDataIsNull, guildId:{}, termId:{}, playerId:{}, isInLeaguaWar:{}", twPlayerData.getGuildId(), termId, playerId, isInLeaguaWar);
//						needFix = true;
//					} else {
//						if (!twRoomData.isActive(constConfig.getPerioTime())) {
//							long curTime = HawkTime.getMillisecond();
//							long endTime = 0;
//							if (isInLeaguaWar) {
//								TiberiumSeasonTimeCfg timeCfg = TiberiumLeagueWarService.getInstance().getCurrTimeCfg();
//								if (timeCfg != null) {
//									endTime = timeCfg.getWarStartTimeValue() + constConfig.getWarOpenTime();
//								}
//							} else {
//								int timeIndex = twGuildData.getTimeIndex();
//								WarTimeChoose warTimeChoose = tiberiumWarService.getWarTimeChoose(timeIndex);
//								endTime = warTimeChoose.getTime() + constConfig.getWarOpenTime();
//							}
//							// 结束时间大于当前时间 或者结束后的一段时间内(这个时间可能 玩家正在等待签回)
//							if (endTime > curTime || curTime - endTime > constConfig.getForceMoveBackTime()) {
//								DungeonRedisLog.log(playerId,"tiberium player check need fix curTime:{}, endTime:{} id:{}", curTime, endTime, playerId);
//								needFix = true;
//							}
//						}
//					}
//
//				}
//			}
//
//			if (needFix) {
//				DungeonRedisLog.log(playerId,"tiberium player login fix id:{}", playerId);
//				String toServerId = this.getEmigrationPlayerServerId(playerId);
//				// 把数据从redis反序列化回来.
//				PlayerDataSerializer.csSyncPlayerData(playerId, true);
//				// 清除redis信息.
//				this.removeEmigrationPlayer(playerId);
//				// 清理本地的信息,
//				RedisProxy.getInstance().removeImmigrationPlayer(toServerId, playerId);
//				// 清理对方服的信息.
//				HawkTaskManager.getInstance().postExtraTask(new HawkTask() {
//
//					@Override
//					public Object run() {
//						GmProxyHelper.proxyCall(toServerId, "tiberiumMoveBack", "opt=4&playerId" + playerId, 2000);
//						return null;
//					}
//				});
//
//				return true;
//			}
//
//			return false;
//		}
	}
	
	public boolean loginFixCyborgPlayer(String serverId, String playerId) {
		int status = RedisProxy.getInstance().getPlayerCrossStatus(serverId, playerId);
		if (status < 0) {
			DungeonRedisLog.log(playerId,"playerId:{} getPlayerCrossStatus fail", playerId);

			return false;
		}

		// 只有在跨服中的状态才需要处理.
		if (status == PlayerCrossStatus.PREPARE_EXIT_CROSS) {
			// 把数据从redis反序列化回来.
			PlayerDataSerializer.csSyncPlayerData(playerId, true);

			// 从跨出玩家集合删除
			this.removeEmigrationPlayer(playerId);

			return true;
		} else {
			// 判断一下时间. 时间过期了则修复.
			CyborgWarService cyborgWarService = CyborgWarService.getInstance();
			CyborgConstCfg constConfig = CyborgConstCfg.getInstance();
			boolean needFix = false;
			int termId = cyborgWarService.getTermId();
			CWPlayerData cwPlayerData = CyborgWarRedis.getInstance().getCWPlayerData(playerId, termId);
			if (cwPlayerData == null || cwPlayerData.getEnterTime() <= 0) {
				DungeonRedisLog.log(playerId,"cyborg player check need fix cwPlayerIsNull:{}, playerId:{}", cwPlayerData == null, playerId);
				needFix = true;
			} else {
				CWTeamJoinData cwTeamData = CyborgWarRedis.getInstance().getCWJoinTeamData(cwPlayerData.getTeamId(), termId);
				if (cwTeamData == null) {
					DungeonRedisLog.log(playerId,"cyborg player check need fix guildId:{}, teamId:{},  termId:{}, playerId:{}", cwPlayerData.getGuildId(), cwPlayerData.getTeamId(), termId, playerId);
					needFix = true;
				} else {
					CWRoomData cwRoomData = CyborgWarRedis.getInstance().getCWRoomData(cwTeamData.getRoomId(), termId);
					if (cwRoomData == null) {
						DungeonRedisLog.log(playerId,"cyborg player check need fix cwRoomDataIsNull, guildId:{}, teamId:{}, termId:{}, playerId:{}", cwPlayerData.getGuildId(), cwPlayerData.getTeamId(), termId, playerId);
						needFix = true;
					} else {
						if (!cwRoomData.isActive(constConfig.getPerioTime())) {
							long curTime = HawkTime.getMillisecond();
							long endTime = 0;
							int timeIndex = cwTeamData.getTimeIndex();
							CWTimeChoose warTimeChoose = cyborgWarService.getWarTimeChoose(timeIndex);
							endTime = warTimeChoose.getTime() + constConfig.getWarOpenTime();
							// 结束时间大于当前时间 或者结束后的一段时间内(这个时间可能 玩家正在等待签回)
							if (endTime > curTime || curTime - endTime > constConfig.getForceMoveBackTime()) {
								DungeonRedisLog.log(playerId,"cyborg player check need fix curTime:{}, endTime:{} id:{}", curTime, endTime, playerId);
								needFix = true;
							}
						}
					}

				}
			}
			/***************************/
			if (needFix) {
				DungeonRedisLog.log(playerId,"cyborg player login fix id:{}", playerId);
				String toServerId = this.getEmigrationPlayerServerId(playerId);
				// 把数据从redis反序列化回来.
				PlayerDataSerializer.csSyncPlayerData(playerId, true);
				// 清除redis信息.
				this.removeEmigrationPlayer(playerId);
				// 清理本地的信息,
				RedisProxy.getInstance().removeImmigrationPlayer(toServerId, playerId);
				// 清理对方服的信息.
				HawkTaskManager.getInstance().postExtraTask(new HawkTask() {

					@Override
					public Object run() {
						GmProxyHelper.proxyCall(toServerId, "cyborgMoveBack", "opt=4&playerId" + playerId, 2000);
						return null;
					}
				});

				return true;
			}

			return false;
		}
	}
	
	
	public boolean loginFixDyzzPlayer(String serverId, String playerId) {
		int status = RedisProxy.getInstance().getPlayerCrossStatus(serverId, playerId);
		if (status < 0) {
			DungeonRedisLog.log(playerId,"playerId:{} getPlayerCrossStatus fail", playerId);

			return false;
		}

		// 只有在跨服中的状态才需要处理.
		if (status == PlayerCrossStatus.PREPARE_EXIT_CROSS) {
			// 把数据从redis反序列化回来.
			PlayerDataSerializer.csSyncPlayerData(playerId, true);

			// 从跨出玩家集合删除
			this.removeEmigrationPlayer(playerId);

			return true;
		} else {
			// 判断一下时间. 时间过期了则修复.
			DYZZService dyzzWarService = DYZZService.getInstance();
			DYZZWarCfg constConfig = HawkConfigManager.getInstance().getKVInstance(DYZZWarCfg.class);
			boolean needFix = false;
			int termId = dyzzWarService.getDYZZWarTerm();
			DYZZPlayerData cwPlayerData = DYZZRedisData.getInstance().getDYZZPlayerData(termId, playerId);
			if (cwPlayerData == null || cwPlayerData.getEnterTime() <= 0) {
				DungeonRedisLog.log(playerId,"cyborg player check need fix cwPlayerIsNull:{}, playerId:{}", cwPlayerData == null, playerId);
				needFix = true;
			} else {
				DYZZGameRoomData dyzzGameData = DYZZRedisData.getInstance().getDYZZGameData(termId, cwPlayerData.getGameId());
				if (dyzzGameData == null) {
					DungeonRedisLog.log(playerId,"cyborg player check need fix dyzzRoomDataIsNull,  teamId:{}, gameID:{}, playerId:{}", cwPlayerData.getTermId(), cwPlayerData.getGameId(), playerId);
					needFix = true;
				} else {
					if (!dyzzGameData.isActive(constConfig.getPerioTime())) {
						long curTime = HawkTime.getMillisecond();
						long endTime = dyzzGameData.getLastActiveTime() + 10 * 1000 + constConfig.getPerioTime() * 1000;
						// 结束时间大于当前时间 或者结束后的一段时间内(这个时间可能 玩家正在等待签回)
						if (endTime > curTime || curTime - endTime > constConfig.getForceMoveBackTime() * 1000) {
							DungeonRedisLog.log(playerId,"cyborg player check need fix curTime:{}, endTime:{} id:{}", curTime, endTime, playerId);
							needFix = true;
						}
					}
				}

			}
			/***************************/
			if (needFix) {
				DungeonRedisLog.log(playerId,"dyzz player login fix id:{}", playerId);
				String toServerId = this.getEmigrationPlayerServerId(playerId);
				// 把数据从redis反序列化回来.
				PlayerDataSerializer.csSyncPlayerData(playerId, true);
				// 清除redis信息.
				this.removeEmigrationPlayer(playerId);
				// 清理本地的信息,
				RedisProxy.getInstance().removeImmigrationPlayer(toServerId, playerId);
				// 清理对方服的信息.
				HawkTaskManager.getInstance().postExtraTask(new HawkTask() {

					@Override
					public Object run() {
						GmProxyHelper.proxyCall(toServerId, "DyzzMoveBack", "opt=4&playerId" + playerId, 2000);
						return null;
					}
				});

				return true;
			}

			return false;
		}
	}


	public boolean loginFixYqzzPlayer(String serverId, String playerId) {
		int status = RedisProxy.getInstance().getPlayerCrossStatus(serverId, playerId);
		if (status < 0) {
			DungeonRedisLog.log(playerId,"playerId:{} getPlayerCrossStatus fail", playerId);
			return false;
		}
		// 只有在跨服中的状态才需要处理.
		if (status == PlayerCrossStatus.PREPARE_EXIT_CROSS) {
			// 把数据从redis反序列化回来.
			PlayerDataSerializer.csSyncPlayerData(playerId, true);
			// 从跨出玩家集合删除
			this.removeEmigrationPlayer(playerId);
			return true;
		} else {
			// 判断一下时间. 时间过期了则修复.
			YQZZMatchService yqzzService = YQZZMatchService.getInstance();
			YQZZWarConstCfg constConfig = HawkConfigManager.getInstance().getKVInstance(YQZZWarConstCfg.class);
			boolean needFix = false;
			int termId = yqzzService.getDataManger().getStateData().getTermId();
			YQZZJoinPlayerData yqzzPlayerData = yqzzService.getDataManger().loadYQZZJoinPlayerData(termId, playerId);
			if (yqzzPlayerData == null) {
				DungeonRedisLog.log(playerId,"yqzz player check need fix yqPlayerIsNull:{}, playerId:{}", yqzzPlayerData == null, playerId);
				needFix = true;
			} else {
				YQZZGameData yqzzGameData = yqzzService.getDataManger().loadYQZZGameData(termId, yqzzPlayerData.getRoomId());
				if (yqzzGameData == null) {
					DungeonRedisLog.log(playerId,"yqzz player check need fix yqzzRoomDataIsNull,  teamId:{}, gameID:{}, playerId:{}", yqzzPlayerData.getTermId(), yqzzPlayerData.getRoomId(), playerId);
					needFix = true;
				} else {
					if (!yqzzGameData.isActive(constConfig.getPerioTime())) {
						long curTime = HawkTime.getMillisecond();
						long endTime = yqzzGameData.getLastActiveTime() + 10 * 1000 + constConfig.getPerioTime() * 1000;
						// 结束时间大于当前时间 或者结束后的一段时间内(这个时间可能 玩家正在等待签回)
						if (endTime > curTime || curTime - endTime > constConfig.getForceMoveBackTime() * 1000) {
							DungeonRedisLog.log(playerId,"yqzz player check need fix curTime:{}, endTime:{} id:{}", curTime, endTime, playerId);
							needFix = true;
						}
					}
				}
			}
			/***************************/
			if (needFix) {
				DungeonRedisLog.log(playerId,"yqzz player login fix id:{}", playerId);
				String toServerId = this.getEmigrationPlayerServerId(playerId);
				// 把数据从redis反序列化回来.
				PlayerDataSerializer.csSyncPlayerData(playerId, true);
				// 清除redis信息.
				this.removeEmigrationPlayer(playerId);
				// 清理本地的信息,
				RedisProxy.getInstance().removeImmigrationPlayer(toServerId, playerId);
				// 清理对方服的信息.
				HawkTaskManager.getInstance().postExtraTask(new HawkTask() {
					@Override
					public Object run() {
						GmProxyHelper.proxyCall(toServerId, "YqzzMoveBack", "opt=4&playerId" + playerId, 2000);
						return null;
					}
				});
				return true;
			}
			return false;
		}
	}

	public boolean loginFixXqhxPlayer(String serverId, String playerId) {
		int status = RedisProxy.getInstance().getPlayerCrossStatus(serverId, playerId);
		if (status < 0) {
			DungeonRedisLog.log(playerId,"playerId:{} getPlayerCrossStatus fail", playerId);
			return false;
		}
		// 只有在跨服中的状态才需要处理.
		if (status == PlayerCrossStatus.PREPARE_EXIT_CROSS) {
			// 把数据从redis反序列化回来.
			PlayerDataSerializer.csSyncPlayerData(playerId, true);
			// 从跨出玩家集合删除
			this.removeEmigrationPlayer(playerId);
			return true;
		} else {
			boolean needFix = !XQHXWarService.getInstance().isInBattle(playerId);
			/***************************/
			if (needFix) {
				DungeonRedisLog.log(playerId,"xqhx player login fix id:{}", playerId);
				String toServerId = this.getEmigrationPlayerServerId(playerId);
				// 把数据从redis反序列化回来.
				PlayerDataSerializer.csSyncPlayerData(playerId, true);
				// 清除redis信息.
				this.removeEmigrationPlayer(playerId);
				// 清理本地的信息,
				RedisProxy.getInstance().removeImmigrationPlayer(toServerId, playerId);
				// 清理对方服的信息.
				HawkTaskManager.getInstance().postExtraTask(new HawkTask() {
					@Override
					public Object run() {
						GmProxyHelper.proxyCall(toServerId, "XqhxMoveBack", "opt=4&playerId" + playerId, 2000);
						return null;
					}
				});
				return true;
			}
			return false;
		}
	}

	
	/**
	 * 玩家P从A服跨到B服 
	 * A记录玩家已经离开A服,　但是B服没有把玩家录入,这种情况,走一遍重新登录即可.
	 * A记录玩家已经离开A服, redis的状态为退出跨服, 那么需要玩家重新.
	 * @param serverId
	 * @param playerId
	 * @return
	 */
	public boolean loginFixCrossPlayer(String serverId, String playerId) {
		//这边记录了玩家还是跨服,但是跨服那边已经把玩家打回来了
		if (this.isEmigrationPlayer(playerId)) {
			int status = RedisProxy.getInstance().getPlayerCrossStatus(serverId, playerId);
			if (status < 0) {
				DungeonRedisLog.log(playerId,"playerId:{} getPlayerCrossStatus fail", playerId);
				
				return false;
			}
			
			//只有在跨服中的状态才需要处理.
			if (status != PlayerCrossStatus.PREPARE_EXIT_CROSS) {
				return false;
			}
			
			//把数据从redis反序列化回来.
			PlayerDataSerializer.csSyncPlayerData(playerId, true);
			
			//从跨出玩家集合删除
			this.removeEmigrationPlayer(playerId);					
			
			return true;
		}
		
		return false;
	}
	
	
	public boolean startServerFixCrossPlayer(String serverId, String playerId, int redisStatus) {
		//非跨入玩家, 不处理.
		if (!this.isEmigrationPlayer(playerId)) {
			//清理状态
			RedisProxy.getInstance().removePlayerCrossStatus(serverId, playerId);
			return false;
		}
		
		//只有在跨服中的状态才需要处理.
		if (redisStatus != PlayerCrossStatus.PREPARE_EXIT_CROSS) {
			return false;
		}
		
		//从跨出玩家集合删除
		this.removeEmigrationPlayer(playerId);	
		
		//清理玩家的状态
		RedisProxy.getInstance().setPlayerCrossStatus(serverId, playerId, GsConst.PlayerCrossStatus.NOTHING);
		
		return true;
	}
	
	/**
	 * 检测尝试退出泰伯利亚的玩家.
	 */
	private void checkExitCyborgPlayers() {
		long curTime = HawkTime.getMillisecond();
		CsPlayer csPlayer = null;
		Player player = null;
		Iterator<Entry<String, Long>> iterator = exitCyborgPlayers.entrySet().iterator();
		Entry<String, Long> entry;
		CyborgConstCfg cyborgConstCfg = CyborgConstCfg.getInstance(); 
		while (iterator.hasNext()) {
			entry = iterator.next();
			
			//还没有到最小的检测等待时间
			if (cyborgConstCfg.getMinBackServerWaitTime() + entry.getValue() > curTime) {
				continue;
			}
			
			// 时间到了强制发出退出指令.			
			if (entry.getValue() + cyborgConstCfg.getMaxBackServerWaitTime()< curTime) {
				DungeonRedisLog.log(entry.getKey(),"cybore csplayer exit wait timeout playerId:{}", entry.getKey());
				//从集合里面删除
				iterator.remove();
				
				//发消息通知可以强制退出了
				CyborgExitCrossInstanceMsg exitCrossInstanceMsg = new CyborgExitCrossInstanceMsg();
				GsApp.getInstance().postMsg(HawkXID.valueOf(GsConst.ObjType.PLAYER, entry.getKey()), exitCrossInstanceMsg);

				continue;
			}
 
			player =  GlobalData.getInstance().makesurePlayer(entry.getKey());
			if (player == null || !player.isCsPlayer()) {
				DungeonRedisLog.log(entry.getKey(),"cyobrg check exit cross player fail  player:{} is null or not a csPlayer", entry.getKey());

				continue;
			}
			
			csPlayer = player.getCsPlayer();			
			// 玩家的状态如果都已检测完毕，则发出实际的退出跨服请求.
			if (csPlayer.checkExit() == Status.SysError.SUCCESS_OK_VALUE) {
				DungeonRedisLog.log(csPlayer.getId(),"cyborg csplayer can exit send a message playerId:{}", csPlayer.getId());
				//从集合里面删除
				iterator.remove();
				
				//发消息通知可以强制退出了
				CyborgExitCrossInstanceMsg exitCrossInstanceMsg = new CyborgExitCrossInstanceMsg();
				GsApp.getInstance().postMsg(csPlayer.getXid(), exitCrossInstanceMsg);
			}
		}
	}


	/**
	 * 检测尝试退出星海激战的玩家.
	 */
	private void checkExitXhjzPlayers() {
		long curTime = HawkTime.getMillisecond();
		CsPlayer csPlayer = null;
		Player player = null;
		Iterator<Entry<String, Long>> iterator = exitXhjzgPlayers.entrySet().iterator();
		Entry<String, Long> entry;
		XHJZConstCfg constCfg = HawkConfigManager.getInstance().getKVInstance(XHJZConstCfg.class);
		while (iterator.hasNext()) {
			entry = iterator.next();

			//还没有到最小的检测等待时间
			if (constCfg.getMinBackServerWaitTime() + entry.getValue() > curTime) {
				continue;
			}

			// 时间到了强制发出退出指令.
			if (entry.getValue() + constCfg.getMaxBackServerWaitTime()< curTime) {
				DungeonRedisLog.log(entry.getKey(),"xhjz csplayer exit wait timeout playerId:{}", entry.getKey());
				//从集合里面删除
				iterator.remove();

				//发消息通知可以强制退出了
				XHJZExitCrossInstanceMsg exitCrossInstanceMsg = new XHJZExitCrossInstanceMsg();
				GsApp.getInstance().postMsg(HawkXID.valueOf(GsConst.ObjType.PLAYER, entry.getKey()), exitCrossInstanceMsg);

				continue;
			}

			player =  GlobalData.getInstance().makesurePlayer(entry.getKey());
			if (player == null || !player.isCsPlayer()) {
				DungeonRedisLog.log(entry.getKey(),"xhjz check exit cross player fail  player:{} is null or not a csPlayer", entry.getKey());

				continue;
			}

			csPlayer = player.getCsPlayer();
			// 玩家的状态如果都已检测完毕，则发出实际的退出跨服请求.
			if (csPlayer.checkExit() == Status.SysError.SUCCESS_OK_VALUE) {
				DungeonRedisLog.log(csPlayer.getId(),"xhjz csplayer can exit send a message playerId:{}", csPlayer.getId());
				//从集合里面删除
				iterator.remove();

				//发消息通知可以强制退出了
				XHJZExitCrossInstanceMsg exitCrossInstanceMsg = new XHJZExitCrossInstanceMsg();
				GsApp.getInstance().postMsg(csPlayer.getXid(), exitCrossInstanceMsg);
			}
		}
	}


	/**
	 * 检测尝试退出先驱回响的玩家.
	 */
	private void checkExitXqhxPlayers() {
		long curTime = HawkTime.getMillisecond();
		CsPlayer csPlayer = null;
		Player player = null;
		Iterator<Entry<String, Long>> iterator = exitXqhxPlayers.entrySet().iterator();
		Entry<String, Long> entry;
		XQHXConstCfg constCfg = HawkConfigManager.getInstance().getKVInstance(XQHXConstCfg.class);
		while (iterator.hasNext()) {
			entry = iterator.next();

			//还没有到最小的检测等待时间
			if (constCfg.getMinBackServerWaitTime() + entry.getValue() > curTime) {
				continue;
			}

			// 时间到了强制发出退出指令.
			if (entry.getValue() + constCfg.getMaxBackServerWaitTime()< curTime) {
				DungeonRedisLog.log(entry.getKey(),"xqhx csplayer exit wait timeout playerId:{}", entry.getKey());
				//从集合里面删除
				iterator.remove();

				//发消息通知可以强制退出了
				XQHXExitCrossInstanceMsg exitCrossInstanceMsg = new XQHXExitCrossInstanceMsg();
				GsApp.getInstance().postMsg(HawkXID.valueOf(GsConst.ObjType.PLAYER, entry.getKey()), exitCrossInstanceMsg);

				continue;
			}

			player =  GlobalData.getInstance().makesurePlayer(entry.getKey());
			if (player == null || !player.isCsPlayer()) {
				DungeonRedisLog.log(entry.getKey(),"xqhx check exit cross player fail  player:{} is null or not a csPlayer", entry.getKey());

				continue;
			}

			csPlayer = player.getCsPlayer();
			// 玩家的状态如果都已检测完毕，则发出实际的退出跨服请求.
			if (csPlayer.checkExit() == Status.SysError.SUCCESS_OK_VALUE) {
				DungeonRedisLog.log(csPlayer.getId(),"xqhx csplayer can exit send a message playerId:{}", csPlayer.getId());
				//从集合里面删除
				iterator.remove();

				//发消息通知可以强制退出了
				XQHXExitCrossInstanceMsg exitCrossInstanceMsg = new XQHXExitCrossInstanceMsg();
				GsApp.getInstance().postMsg(csPlayer.getXid(), exitCrossInstanceMsg);
			}
		}
	}
	
	/**
	 * 检测尝试退出泰伯利亚的玩家.
	 */
	private void checkExitDyzzPlayers() {
		long curTime = HawkTime.getMillisecond();
		CsPlayer csPlayer = null;
		Player player = null;
		Iterator<Entry<String, Long>> iterator = exitDyzzPlayers.entrySet().iterator();
		Entry<String, Long> entry;
		DYZZWarCfg constCfg = HawkConfigManager.getInstance().getKVInstance(DYZZWarCfg.class);
		while (iterator.hasNext()) {
			entry = iterator.next();
			//还没有到最小的检测等待时间
			if (constCfg.getMinBackServerWaitTime() + entry.getValue() > curTime) {
				continue;
			}
			// 时间到了强制发出退出指令.			
			if (entry.getValue() + constCfg.getMaxBackServerWaitTime()< curTime) {
				DungeonRedisLog.log(entry.getKey(),"dyzz csplayer exit wait timeout playerId:{}", entry.getKey());
				//从集合里面删除
				iterator.remove();
				//发消息通知可以强制退出了
				DYZZExitCrossInstanceMsg exitCrossInstanceMsg = new DYZZExitCrossInstanceMsg();
				GsApp.getInstance().postMsg(HawkXID.valueOf(GsConst.ObjType.PLAYER, entry.getKey()), exitCrossInstanceMsg);
				continue;
			}
 
			player =  GlobalData.getInstance().makesurePlayer(entry.getKey());
			if (player == null || !player.isCsPlayer()) {
				DungeonRedisLog.log(entry.getKey(),"dyzz check exit cross player fail  player:{} is null or not a csPlayer", entry.getKey());

				continue;
			}
			
			csPlayer = player.getCsPlayer();			
			// 玩家的状态如果都已检测完毕，则发出实际的退出跨服请求.
			if (csPlayer.checkExit() == Status.SysError.SUCCESS_OK_VALUE) {
				DungeonRedisLog.log(csPlayer.getId(),"dyzz csplayer can exit send a message playerId:{}", csPlayer.getId());
				//从集合里面删除
				iterator.remove();
				//发消息通知可以强制退出了
				DYZZExitCrossInstanceMsg exitCrossInstanceMsg = new DYZZExitCrossInstanceMsg();
				GsApp.getInstance().postMsg(csPlayer.getXid(), exitCrossInstanceMsg);
			}
		}
	}
	
	
	
	/**
	 * 检测尝试退出月球之战的玩家.
	 */
	private void checkExitYqzzPlayers() {
		long curTime = HawkTime.getMillisecond();
		CsPlayer csPlayer = null;
		Player player = null;
		Iterator<Entry<String, Long>> iterator = exitYqzzPlayers.entrySet().iterator();
		Entry<String, Long> entry;
		YQZZWarConstCfg constCfg = HawkConfigManager.getInstance().getKVInstance(YQZZWarConstCfg.class);
		while (iterator.hasNext()) {
			entry = iterator.next();
			//还没有到最小的检测等待时间
			if (constCfg.getMinBackServerWaitTime() + entry.getValue() > curTime) {
				continue;
			}
			// 时间到了强制发出退出指令.			
			if (entry.getValue() + constCfg.getMaxBackServerWaitTime()< curTime) {
				DungeonRedisLog.log(entry.getKey(),"yqzz csplayer exit wait timeout playerId:{}", entry.getKey());
				//从集合里面删除
				iterator.remove();
				//发消息通知可以强制退出了
				YQZZExitCrossInstanceMsg exitCrossInstanceMsg = new YQZZExitCrossInstanceMsg();
				GsApp.getInstance().postMsg(HawkXID.valueOf(GsConst.ObjType.PLAYER, entry.getKey()), exitCrossInstanceMsg);
				continue;
			}
 
			player =  GlobalData.getInstance().makesurePlayer(entry.getKey());
			if (player == null || !player.isCsPlayer()) {
				DungeonRedisLog.log(entry.getKey(),"yqzz check exit cross player fail  player:{} is null or not a csPlayer", entry.getKey());
				continue;
			}
			
			csPlayer = player.getCsPlayer();			
			// 玩家的状态如果都已检测完毕，则发出实际的退出跨服请求.
			if (csPlayer.checkExit() == Status.SysError.SUCCESS_OK_VALUE) {
				DungeonRedisLog.log(csPlayer.getId(),"yqzz csplayer can exit send a message playerId:{}", csPlayer.getId());
				//从集合里面删除
				iterator.remove();
				//发消息通知可以强制退出了
				YQZZExitCrossInstanceMsg exitCrossInstanceMsg = new YQZZExitCrossInstanceMsg();
				GsApp.getInstance().postMsg(csPlayer.getXid(), exitCrossInstanceMsg);
			}
		}
	}
	
	/**
	 * 星球大战.
	 */
	private void checkExitStarWarsPlayers() {
		long curTime = HawkTime.getMillisecond();
		CsPlayer csPlayer = null;
		Player player = null;
		Iterator<Entry<String, Long>> iterator = exitStarWarsPlayers.entrySet().iterator();
		Entry<String, Long> entry;
		StarWarsConstCfg constCfg = StarWarsConstCfg.getInstance(); 
		while (iterator.hasNext()) {
			entry = iterator.next();
			
			//还没有到最小的检测等待时间
			if (constCfg.getMinWaitTime() + entry.getValue() > curTime) {
				continue;
			}
			
			player =  GlobalData.getInstance().makesurePlayer(entry.getKey());
			if (player == null || !player.isCsPlayer()) {
				DungeonRedisLog.log(entry.getKey(),"starwars check exit cross player fail  player:{} is null or not a csPlayer", entry.getKey());

				continue;
			}
			
			csPlayer = player.getCsPlayer();	
			// 时间到了强制发出退出指令.			
			if (entry.getValue() + constCfg.getMaxWaitTime() < curTime) {
				DungeonRedisLog.log(entry.getKey(),"starwars csplayer exit wait timeout playerId:{}", entry.getKey());
				//从集合里面删除
				iterator.remove();
				
				//发消息通知可以强制退出了
				StarWarsExitCrossInstanceMsg exitCrossInstanceMsg = new StarWarsExitCrossInstanceMsg();
				GsApp.getInstance().postMsg(HawkXID.valueOf(GsConst.ObjType.PLAYER, entry.getKey()), exitCrossInstanceMsg);

				continue;
			}
 					
			// 玩家的状态如果都已检测完毕，则发出实际的退出跨服请求.
			if (csPlayer.checkExit() == Status.SysError.SUCCESS_OK_VALUE) {
				DungeonRedisLog.log(csPlayer.getId(),"starwars csplayer can exit send a message playerId:{}", csPlayer.getId());
				//从集合里面删除
				iterator.remove();
				
				//发消息通知可以强制退出了
				StarWarsExitCrossInstanceMsg exitCrossInstanceMsg = new StarWarsExitCrossInstanceMsg();
				GsApp.getInstance().postMsg(csPlayer.getXid(), exitCrossInstanceMsg);
			}
		}
	}
	
	
	/**
	 * 检测尝试退出泰伯利亚的玩家.
	 */
	private void checkExitTiberiumPlayers() {
		long curTime = HawkTime.getMillisecond();
		CsPlayer csPlayer = null;
		Player player = null;
		Iterator<Entry<String, Long>> iterator = exitTimberiumPlayers.entrySet().iterator();
		Entry<String, Long> entry;
		TiberiumConstCfg tiberiumConstCfg = TiberiumConstCfg.getInstance(); 
		while (iterator.hasNext()) {
			entry = iterator.next();
			
			//还没有到最小的检测等待时间
			if (tiberiumConstCfg.getMinBackServerWaitTime() + entry.getValue() > curTime) {
				continue;
			}
			
			// 时间到了强制发出退出指令.			
			if (entry.getValue() + tiberiumConstCfg.getMaxBackServerWaitTime()< curTime) {
				DungeonRedisLog.log(entry.getKey(),"tiberium csplayer exit wait timeout playerId:{}", entry.getKey());
				//从集合里面删除
				iterator.remove();
				
				//发消息通知可以强制退出了
				TiberiumExitCrossInstanceMsg exitCrossInstanceMsg = new TiberiumExitCrossInstanceMsg();
				GsApp.getInstance().postMsg(HawkXID.valueOf(GsConst.ObjType.PLAYER, entry.getKey()), exitCrossInstanceMsg);

				continue;
			}
 
			player =  GlobalData.getInstance().makesurePlayer(entry.getKey());
			if (player == null || !player.isCsPlayer()) {
				DungeonRedisLog.log(entry.getKey(),"tiberium check exit cross player fail  player:{} is null or not a csPlayer", entry.getKey());

				continue;
			}
			
			csPlayer = player.getCsPlayer();			
			// 玩家的状态如果都已检测完毕，则发出实际的退出跨服请求.
			if (csPlayer.checkExit() == Status.SysError.SUCCESS_OK_VALUE) {
				DungeonRedisLog.log(csPlayer.getId(),"tiberium csplayer can exit send a message playerId:{}", csPlayer.getId());
				//从集合里面删除
				iterator.remove();
				
				//发消息通知可以强制退出了
				TiberiumExitCrossInstanceMsg exitCrossInstanceMsg = new TiberiumExitCrossInstanceMsg();
				GsApp.getInstance().postMsg(csPlayer.getXid(), exitCrossInstanceMsg);
			}
		}
	}
	/**
	 * 检测尝试退出跨服的玩家.
	 */
	private void checkExitCrossPlayers() {
		long curTime = HawkTime.getMillisecond();
		CsPlayer csPlayer = null;
		Player player = null;
		Iterator<Entry<String, Long>> iterator = exitCrossPlayers.entrySet().iterator();
		Entry<String, Long> entry;
		GameConstCfg gameConstCfg = GameConstCfg.getInstance(); 
		while (iterator.hasNext()) {
			entry = iterator.next();
			//还没有到最小的检测等待时间
			if (gameConstCfg.getExitCrossMinWaitTime() + entry.getValue() > curTime) {
				continue;
			}
			
			// 时间到了强制发出退出指令.			
			if (entry.getValue() + gameConstCfg.getExitCrossMaxWaitTime() < curTime) {
				DungeonRedisLog.log(entry.getKey(),"csplayer exit wait timeout playerId:{}", entry.getKey());
				//从集合里面删除
				iterator.remove();
				//发消息通知可以强制退出了
				ExitCrossMsg exitCorssMsg = new ExitCrossMsg();
				GsApp.getInstance().postMsg(HawkXID.valueOf(GsConst.ObjType.PLAYER, entry.getKey()), exitCorssMsg);
				continue;
			}
			
			player = GlobalData.getInstance().makesurePlayer(entry.getKey());
			if (player == null || !player.isCsPlayer()) {
				DungeonRedisLog.log(entry.getKey(),"check exit cross player fail  player:{} is null", entry.getKey());
				continue;
			}
			
			csPlayer = player.getCsPlayer();
			// 玩家的状态如果都已检测完毕，则发出实际的退出跨服请求.
			if (csPlayer.checkExit() == Status.SysError.SUCCESS_OK_VALUE) {
				DungeonRedisLog.log(csPlayer.getId(),"csplayer can exit send a message playerId:{}", csPlayer.getId());
				//从集合里面删除
				iterator.remove();
				//发消息通知可以强制退出了
				ExitCrossMsg exitCrossMsg = new ExitCrossMsg();
				GsApp.getInstance().postMsg(csPlayer.getXid(), exitCrossMsg);
			}
		}
	}

	/**
	 * 添加需要等待的玩家信息
	 * 
	 * @param playerId
	 * @param serverId
	 * @param dataKey
	 */
	public void addWaitSyncPlayer(String playerId, String serverId, PlayerDataKey dataKey) {
		waitSyncPlayer.put(playerId, serverId);
	}

	/**
	 * 移除等待同步玩家id
	 * 
	 * @param playerId
	 */
	public void removeWaitSyncPlayer(String playerId) {
		waitSyncPlayer.remove(playerId);
	}
	
	/**
	 * 刷新跨服的entity数据
	 */
	public void notifyPlayerDataSync() {
		if (waitSyncPlayer.size() <= 0) {
			return;
		}
		
		Map<String, String> syncPlayers = new HashMap<String, String>(waitSyncPlayer);
		waitSyncPlayer.clear();		
		for (Entry<String, String> entry : syncPlayers.entrySet()) {
			CsPlayerDataSync.Builder builder = CsPlayerDataSync.newBuilder();
			builder.setPlayerId(entry.getKey());			
			
			// 通知
			HawkProtocol protocol = HawkProtocol.valueOf(HP.sys.CS_PLAYER_DATA_SYNC_VALUE, builder);
			CrossProxy.getInstance().sendNotify(protocol, entry.getValue(), null);
		}
	}

	/**
	 * 通知同步落地玩家数据
	 * 
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.sys.CS_PLAYER_DATA_SYNC_VALUE)
	private void onNotifyPlayerDataSycn(HawkProtocol protocol) {
		CsPlayerDataSync cmd = protocol.parseProtocol(CsPlayerDataSync.getDefaultInstance());
		String playerId = cmd.getPlayerId();

		// 同步
		HawkThreadPool threadPool = HawkTaskManager.getInstance().getThreadPool("task");
		if (threadPool != null) {
			int threadIdx = Math.abs(playerId.hashCode()) % threadPool.getThreadNum();
			threadPool.addTask(new CsEntitySyncTask(playerId), threadIdx, false);
		} else {
			PlayerDataSerializer.csSyncPlayerData(playerId);
		}
	}

	/**
	 * 刷新跨服的所有玩家的entity数据
	 */
	public void syncAllCsPlayerData() {
		Map<String, String> csPlayers = CrossService.getInstance().getEmigrationPlayers();
		for (Entry<String, String> entry : csPlayers.entrySet()) {
			PlayerDataSerializer.csSyncPlayerData(entry.getKey());
		}
	}
	
	/**
	 * 该方法通过RPC调用,玩家准备跨服的时候先发该协议把数据反序列化出来.
	 * 跨服,和九鼎都走此类.
	 * 
	 */
	@ProtocolHandler(code = CHP.code.INNER_ENTER_CROSS_REQ_VALUE) 
	public void prepareEnterCross(HawkProtocol protocol){
		ProxyHeader proxyHeader = (ProxyHeader)protocol.getUserData();
		String playerId = proxyHeader.getSource();
		
		InnerEnterCrossReq req = protocol.parseProtocol(InnerEnterCrossReq.getDefaultInstance());
		int curTime = HawkTime.getSeconds();
		if (curTime > req.getCurTime() && curTime - req.getCurTime() >= GameConstCfg.getInstance().getCrossProtocolValidTime()) {
			DungeonRedisLog.log(playerId,"inner enter cross timeout curTime:{} protocolTime:{} playerId:{}", curTime, req.getCurTime(), playerId);		
			return;
		}		
		//如果是战斗服,则走异步,讲道理战斗服的额外线程应该都是空的才对,  否则维持原有逻辑.
		if (req.hasCrossType()) {
			int index = Math.abs(playerId.hashCode() % HawkTaskManager.getInstance().getThreadNum());
			HawkTaskManager.getInstance().postTask(new HawkTask() {				
				@Override
				public Object run() {					
					try {
						doPrepareEnterCross(playerId, proxyHeader);
					} catch (Exception e) {
						HawkException.catchException(e);						
					}
									
					return null;
				}
			}, index);
		} else {
			doPrepareEnterCross(playerId, proxyHeader);
			
		}
	}
	
	private void doPrepareEnterCross(String playerId, ProxyHeader proxyHeader) {
		
		int errorCode = Status.SysError.SUCCESS_OK_VALUE;
		int curTime = HawkTime.getSeconds();
		//已经是迁入玩家.
		String fromServerId = this.getImmigrationPlayerServerId(playerId); 
		if (!HawkOSOperator.isEmptyString(fromServerId)) {
			DungeonRedisLog.log(playerId,"playerId:{}is immigration player  fromServerId:{}", playerId, fromServerId);
		}				
		
		label:
		{
			int maxSessionSize = GlobalData.getInstance().getServerSettingData().getMaxOnlineCount();
			if (maxSessionSize > 0 && GlobalData.getInstance().getOnlineUserCount() >= maxSessionSize) {
				errorCode = Status.SysError.SERVER_BUSY_LIMIT_VALUE;
				break label;
			}
			
			CsPlayerData csPlayerData = new CsPlayerData();
			csPlayerData.loadPlayerData(playerId);
			try {
				PlayerDataSerializer.buildFromRedis(csPlayerData.getDataCache(), false);
				GlobalData.getInstance().addPlayerDataToCache(playerId, csPlayerData);
			} catch (Exception e) {
				HawkException.catchException(e);
				errorCode = Status.SysError.EXCEPTION_VALUE;
				
				break label;
			}
			
			prepareCrossTime.put(playerId, curTime);
		}
		
		DungeonRedisLog.log(playerId,"playerId:{} prepare cross server code:{}", playerId, errorCode);
		
		rpcCommonResp(proxyHeader, errorCode);
	}
	
	public void rpcCommonResp(ProxyHeader proxyHeader, int errorCode) {
		RpcCommonResp.Builder builder = RpcCommonResp.newBuilder(); 
		builder.setErrorCode(errorCode);
		HawkProtocol respProtocol = HawkProtocol.valueOf(CHP.code.INNER_ENTER_CROSS_RESP, builder);
		CrossProxy.getInstance().rpcResponse(proxyHeader, respProtocol);
	}

	@ProtocolHandler(code = HP.code.STAR_WARS_SEARCH_PLAYER_REQ_VALUE) 
	private void onStarWarsSearchPlayerReq(HawkProtocol protocol){
		//playerId必不为空不必校验.
		StarWarsSearchPlayerReq req = protocol.parseProtocol(StarWarsSearchPlayerReq.getDefaultInstance());
		StarWarsSearchPlayerResp.Builder resp = StarWarsSearchPlayerResp.newBuilder();
		resp.setPart(req.getPart());
		Player player = GlobalData.getInstance().makesurePlayer(req.getPlayerId());
		if (player != null) {
			CrossPlayerStruct struct = BuilderUtil.buildCrossPlayer(player).build();
			resp.setPlayer(struct);
			RedisProxy.getInstance().updateCrossPlayerStruct(req.getPlayerId(), struct, 86400);
		}
		HawkProtocol hawkProtocol = HawkProtocol.valueOf(HP.code.STAR_WARS_SEARCH_PLAYER_RESP_VALUE, resp);
		ProxyHeader header = protocol.getUserData();
		CrossProxy.getInstance().rpcResponse(header, hawkProtocol);
	}
	
	/**
	 * 跨服占领他国王城
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code2.NATIONAL_PRESIDENT_NOTIFY_VALUE) 
	private void onNationalPresidentCityNotify(HawkProtocol protocol){
		// 占领他国王城后，更新状态
		NationService.getInstance().checkAndUpdateNationStatus();
		
		// 刷9级矿
		String serverId = GsConfig.getInstance().getServerId();
		CrossActivityService.getInstance().updateSpecialResRefreshTime();
		RedisProxy.getInstance().updateSpecialResRefresh(serverId, HawkTime.getMillisecond() + CrossConstCfg.getInstance().getSpecialResourcePeriod());
		CrossActivityService.getInstance().refreshSpecialRes(true);
		
		// 征服排行榜
		RedisProxy.getInstance().updateCrossConquerRank();
		
		HawkLog.logPrintln("rec national president win over...");
	}
	
	/**
	 * 进入世界地图
	 *
	 */
	@ProtocolHandler(code = HP.code.PLAYER_ENTER_WORLD_VALUE)
	private void onPlayerEnterWorld(HawkProtocol protocol) {
		PlayerEnterWorld cmd = protocol.parseProtocol(PlayerEnterWorld.getDefaultInstance());
		if (!cmd.getServerId().equals(GsConfig.getInstance().getServerId())) {
			return;
		}

		syncPoints(protocol.getUserData(), cmd.getX(), cmd.getY(), 0);
//		syncCrossPresidentInfo(protocol.getUserData());
	}
	
	/**
	 * 世界移动
	 * 
	 */
	@ProtocolHandler(code = HP.code.PLAYER_WORLD_MOVE_VALUE)
	private void onFetchInviewWorldPoint(HawkProtocol protocol) {
		FetchInviewWorldPoint cmd = protocol.parseProtocol(FetchInviewWorldPoint.getDefaultInstance());
		if (!GlobalData.getInstance().isLocalServer(cmd.getServerId())) {
			return;
		}

		syncPoints(protocol.getUserData(), cmd.getX(), cmd.getY(), cmd.getSpeed());
		syncMarchs(protocol.getUserData(), cmd);
	}

	/**
	 * 同步世界点
	 * 
	 * @param protocol
	 * @param cmd
	 */
	private void syncPoints(ProxyHeader header, int x, int y, float speed) {
		WorldPointSync.Builder builder = WorldPointSync.newBuilder();
		List<WorldPointPB.Builder> pointList = WorldPointService.getInstance().getPlayerViewObjs(null, x, y, speed);
		for (WorldPointPB.Builder wp : pointList) {
			builder.addPoints(wp);
		}
		builder.setServerId(GsConfig.getInstance().getServerId());

		HawkProtocol sendProtocol = HawkProtocol.valueOf(HP.code.WORLD_POINT_SYNC_VALUE, builder);
		CrossProxy.getInstance().sendProtocol(sendProtocol, header.getFrom(), header.getSource());
	}

	/**
	 * 同步世界行军
	 * 
	 * @param protocol
	 * @param cmd
	 */
	private void syncMarchs(ProxyHeader header, FetchInviewWorldPoint cmd) {
		Set<IWorldMarch> marchs = WorldMarchService.getInstance().calcInviewMarchs(null, cmd.getX(), cmd.getY());

		MarchEventSync.Builder builder = MarchEventSync.newBuilder();
		builder.setEventType(MarchEvent.MARCH_SYNC_VALUE);

		WorldMarchPB.Builder tmp = WorldMarchPB.newBuilder();

		for (IWorldMarch march : marchs) {
			if (march == null) {
				continue;
			}

			MarchData.Builder dataBuilder = MarchData.newBuilder();
			dataBuilder.setMarchId(march.getMarchId());
			dataBuilder.setMarchPB(march.getMarchEntity().toBuilder(tmp.clear(), WorldMarchRelation.NONE));

			builder.addMarchData(dataBuilder);
		}

		// 发送剩余数据
		HawkProtocol sendProtocol = HawkProtocol.valueOf(HP.code.WORLD_MARCH_EVENT_SYNC_VALUE, builder);
		CrossProxy.getInstance().sendProtocol(sendProtocol, header.getFrom(), header.getSource());
	}

	/**
	 * 获取跨服王城信息
	 */
	public boolean syncCrossPresidentInfo(ProxyHeader header) {
		
		try {
			PresidentInfoSync.Builder builder = PresidentInfoSync.newBuilder();
			builder.setServerName(GsConfig.getInstance().getServerId());
			PresidentCity city = PresidentFightService.getInstance().getPresidentCity();
			builder.setInfo(city.genPresidentInfoBuilder());

			HawkProtocol sendProtocol = HawkProtocol.valueOf(HP.code.PRESIDENT_INFO_SYNC, builder);
			CrossProxy.getInstance().sendProtocol(sendProtocol, header.getFrom(), header.getSource());
			
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		return true;
	}
	
	/**
	 * 判断是否为本服的实体对象
	 * 
	 * @param entity
	 * @return
	 */
	public boolean isCsEntity(HawkDBEntity entity) {
		if (!HawkOSOperator.isEmptyString(entity.getOwnerKey())
				&& !GlobalData.getInstance().isLocalPlayer(entity.getOwnerKey())) {
			return true;
		}

		return false;
	}

	/**
	 * 获取从其他服跨到本服的玩家集合 不可修改.
	 * 
	 * @return
	 */
	public Map<String, String> getImmigrationPlayers() {
		return Collections.unmodifiableMap(immigrationPlayers);
	}

	/**
	 * 获取从本服跨到其它的玩家集合 不可修改
	 * 
	 * @return
	 */
	public Map<String, String> getEmigrationPlayers() {
		return Collections.unmodifiableMap(emigrationPlayers);
	}

	/**
	 * 从其它服跨到本服的玩家
	 * 
	 * @param playerId
	 * @return
	 */
	public boolean isImmigrationPlayer(String playerId) {
		return immigrationPlayers.containsKey(playerId);
	}

	/**
	 * 本服跨到其它服的玩家
	 * 
	 * @param playerId
	 * @return
	 */
	public boolean isEmigrationPlayer(String playerId) {
		return emigrationPlayers.containsKey(playerId);
	}

	/**
	 * 其它服跨过来的玩家
	 * 
	 * @param playerId
	 * @return
	 */
	public String getImmigrationPlayerServerId(String playerId) {
		return immigrationPlayers.get(playerId);
	}

	/**
	 * 获取玩家从什么服跨过来
	 * 
	 * @param playerId
	 * @return
	 */
	public String getEmigrationPlayerServerId(String playerId) {
		return emigrationPlayers.get(playerId);
	}

	/**
	 * 其它服跨到本服
	 * 
	 * @param playerId
	 * @param fromServerId
	 * @return
	 */
	public boolean addImmigrationPlayer(String playerId, String fromServerId) {
		immigrationPlayers.put(playerId, fromServerId);		
		int expireTime = CrossActivityService.getInstance().getCrossKeyExpireTime();
		RedisProxy.getInstance().saveImmigrationPlayer(GsConfig.getInstance().getServerId(), playerId, fromServerId, expireTime);
		
		return true;
	}

	/**
	 * 从本服跨服到别服
	 * 
	 * @param playerId
	 * @param targetServerId
	 * @return
	 */
	public boolean addEmigrationPlayer(String playerId, String targetServerId) {
		emigrationPlayers.put(playerId, targetServerId);
		
		int expireTime = CrossActivityService.getInstance().getCrossKeyExpireTime();
		RedisProxy.getInstance().saveEmigrationPlayer(GsConfig.getInstance().getServerId(), playerId, targetServerId, expireTime);
		
		return true;
	}

	/**
	 * 移除玩家 从其他服跨服过来的玩家列表中移除
	 * 
	 * @param playerId
	 * @return
	 */
	public String removeImmigrationPlayer(String playerId) {
		String fromServerId = immigrationPlayers.remove(playerId);
		if (HawkOSOperator.isEmptyString(fromServerId)) {
			DungeonRedisLog.log(playerId,"remove immigration playerId:{} from server id is null", playerId);
		}
		
		//不管你是不是我都给你清.
		RedisProxy.getInstance().removeImmigrationPlayer(GsConfig.getInstance().getServerId(), playerId);
		
		return fromServerId;
	}

	/**
	 * 移除玩家 从本服跨服走的玩家列表中移除
	 * 
	 * @param playerId
	 * @return
	 */
	public String removeEmigrationPlayer(String playerId) {
		String toServerId = emigrationPlayers.remove(playerId);
		if (HawkOSOperator.isEmptyString(toServerId)) {
			DungeonRedisLog.log(playerId,"remove emigration playerId:{} to server id is null", playerId);
		}
		
		//怂了，不管你有没有跨出去，只要你调了我就给你清.
		RedisProxy.getInstance().removeEmigrationPlayer(GsConfig.getInstance().getServerId(), playerId);
		
		return toServerId;
	}

	/**
	 * 不区分是跨出去,还是跨进来.
	 * 
	 * @param playerId
	 * @return
	 */
	public boolean isCrossPlayer(String playerId) {
		return this.isImmigrationPlayer(playerId) || isEmigrationPlayer(playerId);
	}

	/**
	 * 判断两个服是不是跨服
	 * 
	 * @param sourceServerId
	 * @param targetServerId
	 * @return
	 */
	public boolean isCrossServer(String sourceServerId, String targetServerId) {
		if (HawkOSOperator.isEmptyString(sourceServerId) || HawkOSOperator.isEmptyString(targetServerId)) {
			return false;
		}
		// 不能同服跨
		if (sourceServerId.equals(targetServerId)) {
			return false;
		}
		AssembleDataManager assembleDataManager = AssembleDataManager.getInstance();
		CrossServerListCfg listCfg = assembleDataManager.getCrossServerListCfg(sourceServerId);
		if (listCfg == null) {
			return false;
		}
		
		return listCfg.getServerList().contains(targetServerId);
	}

	/**
	 * 把玩家添加到等待退出列表.
	 * 
	 * @author jm 2019 上午10:11:32
	 * @param playerId
	 * @return
	 */
	
	/**
	 * 添加到检测队列
	 */
	public boolean addExitCrossPlayer(String playerId) {
		
		DungeonRedisLog.log(playerId,"add exit cross csplayer:{}", playerId);
		// 5秒
		long curTime = HawkTime.getMillisecond();
		return exitCrossPlayers.putIfAbsent(playerId, curTime) != null;
	}
	
	/**
	 * 退出泰伯利亚.
	 * @param playerId
	 * @return
	 */
	public boolean addExitTiberiumPlayer(String playerId) {
		DungeonRedisLog.log(playerId,"add exit tiberium csplayer:{}", playerId);
		long curTime = HawkTime.getMillisecond();
		if (!this.isImmigrationPlayer(playerId)) {
			DungeonRedisLog.log(playerId,"player:{} is not a immigration player", playerId);
			return false;
		}
		
		return exitTimberiumPlayers.putIfAbsent(playerId, curTime) != null;
	}
	
	
	/**
	 * 退出泰伯利亚.
	 * @param playerId
	 * @return
	 */
	public boolean addExitDyzzPlayer(String playerId) {
		DungeonRedisLog.log(playerId,"add exit dyzz csplayer:{}", playerId);
		long curTime = HawkTime.getMillisecond();
		if (!this.isImmigrationPlayer(playerId)) {
			DungeonRedisLog.log(playerId,"player:{} is not a immigration player", playerId);
			return false;
		}
		
		return exitDyzzPlayers.putIfAbsent(playerId, curTime) != null;
	}
	
	
	/**
	 * 退出月球之战
	 * @param playerId
	 * @return
	 */
	public boolean addExitYqzzPlayer(String playerId) {
		DungeonRedisLog.log(playerId,"add exit Yqzz csplayer:{}", playerId);
		long curTime = HawkTime.getMillisecond();
		if (!this.isImmigrationPlayer(playerId)) {
			DungeonRedisLog.log(playerId,"player:{} is not a immigration player", playerId);
			return false;
		}
		return exitYqzzPlayers.putIfAbsent(playerId, curTime) != null;
	}
	
	/**
	 * 退出星球大战
	 * @param playerId
	 * @return
	 */
	public boolean addExitStarWarsPlayer(String playerId) {
		DungeonRedisLog.log(playerId,"add exit starwars csPlayer:{}", playerId);
		long curTime = HawkTime.getMillisecond();
		if (!this.isImmigrationPlayer(playerId)) {
			DungeonRedisLog.log(playerId,"player:{} is not a immigration player", playerId);
			
			return false;
		}
		
		return exitStarWarsPlayers.putIfAbsent(playerId, curTime) != null;
	}
	
	/**
	 * 退出泰伯利亚.
	 * @param playerId
	 * @return
	 */
	public boolean addExitCyborgPlayer(String playerId) {
		DungeonRedisLog.log(playerId,"add exit cyborg csplayer:{}", playerId);
		long curTime = HawkTime.getMillisecond();
		if (!this.isImmigrationPlayer(playerId)) {
			DungeonRedisLog.log(playerId,"player:{} is not a immigration player", playerId);
			return false;
		}
		
		return exitCyborgPlayers.putIfAbsent(playerId, curTime) != null;
	}

	public boolean addExitXhjzPlayer(String playerId) {
		DungeonRedisLog.log(playerId,"add exit xhjz csplayer:{}", playerId);
		long curTime = HawkTime.getMillisecond();
		if (!this.isImmigrationPlayer(playerId)) {
			DungeonRedisLog.log(playerId,"player:{} is not a immigration player", playerId);
			return false;
		}

		return exitXhjzgPlayers.putIfAbsent(playerId, curTime) != null;
	}

	public boolean addExitXqhxPlayer(String playerId) {
		DungeonRedisLog.log(playerId,"add exit xqhx csplayer:{}", playerId);
		long curTime = HawkTime.getMillisecond();
		if (!this.isImmigrationPlayer(playerId)) {
			DungeonRedisLog.log(playerId,"player:{} is not a immigration player", playerId);
			return false;
		}

		return exitXqhxPlayers.putIfAbsent(playerId, curTime) != null;
	}

	/**
	 * 强制删除所有玩家的城点
	 */
	public void forceRemoveAllPlayerCity() {
		Set<String> playerIdSet = new HashSet<>(this.immigrationPlayers.keySet());
		WorldThreadScheduler.getInstance().postWorldTask(new CrossEndRemoveCityTask(playerIdSet, GsConst.WorldTaskType.CROSS_END_REMOVE_CITY));
	}
	/**
	 * 强制遣返所有的玩家.
	 * 
	 * @author jm
	 * @return
	 */
	public void forceRepatriatePlayers() {
		HawkLog.logPrintln("force repatriate players");
		Player player = null;
		for (String playerId : this.immigrationPlayers.keySet()) {
			try {
				player = GlobalData.getInstance().makesurePlayer(playerId);
				if (player == null) {
					DungeonRedisLog.log(playerId,"force repatriate csplayer fail player is null playerId:{}", playerId);

					continue;
				}
				PrepareExitCrossMsg msg = new PrepareExitCrossMsg();
				GsApp.getInstance().postMsg(player.getXid(), msg);
			} catch (Exception e) {
				HawkException.catchException(e);
			}
			
		}
	}
	
	/**
	 * 读取redis数据判断服务器是否开着的.
	 * @param serverId
	 * @return
	 */
	public boolean isServerOpen(String serverId) {
		//debug状态下,不校验.
		if (GsConfig.getInstance().isDebug()) {
			return true;
		}
		if (HawkOSOperator.isEmptyString(serverId)) {
			return false;
		}
		
		ServerStatus ss = RedisProxy.getInstance().getServerStatus(serverId);
		if (ss == null) {
			return false;
		}
		
		int periodTime = GameConstCfg.getInstance().getShowActivePlayerPeriod();
		long curTime = HawkTime.getMillisecond();
		int fixTime = GameConstCfg.getInstance().getServerActiveFixTime();
		//预留 两秒的误差,
		if (curTime - ss.getActiveTime() * 1000l > (periodTime + fixTime) ) {
			return false;
		} else {
			return true;
		}
	}
	
	/**
	 * 玩家在跨服状态下登录,此处才有值. 
	 * @param playerId
	 * @return
	 */
	public HawkSession getPlayerIdSession(String playerId) {
		return playerIdSession.get(playerId);
	}
	
	/**
	 * 玩家ID和Session的连接,只有在跨服登录下此处才会被调用.
	 * @param playerId
	 * @param session
	 */
	public void addPlayerIdSession(String playerId, HawkSession session) {
		playerIdSession.put(playerId, session);
	}
	
	/**
	 * 删除玩家一些星球大战的玩家信息.
	 * @param playerId
	 */
	public void clearStarWarsImmigrationPlayer(String playerId) {
		clearStarWarsImmigrationPlayer(playerId, true);
	}
	
	/**
	 * 清除星球大战.
	 * @param playerId
	 * @param flushRedis
	 */
	public void clearStarWarsImmigrationPlayer(String playerId, boolean flushRedis) {
		try {			
			Player player = GlobalData.getInstance().queryPlayer(playerId);
			if (player != null && player.getData() != null && player.getData().getDataCache() != null) {
				if (flushRedis) {
					PlayerDataSerializer.flushToRedis(player.getData().getDataCache(), null, true);
				}				
				if (player.isActiveOnline()) {
					player.kickout(Status.SysError.ADMIN_OPERATION_VALUE, true, "");
				}
			}
		} catch (Exception e) {
			HawkException.catchException(e);						
		}			
		
		//这里不能做player的清理因为 world 那边会重新make 出来，就不做无用功了.
		
		String fromServerId = this.removeImmigrationPlayer(playerId);
		GlobalData.getInstance().removeAccountInfoOnExitCross(playerId);		
		
		HawkLog.warnPrintln("starwar clear immigration player playerId:{} from serverId:{}", playerId, fromServerId);
	}
	
	public void clearTiberiumImmigrationPlayer(String playerId, boolean flushRedis) {
		try {			
			Player player = GlobalData.getInstance().queryPlayer(playerId);
			if (player != null && player.getData() != null && player.getData().getDataCache() != null) {
				if (flushRedis) {
					PlayerDataSerializer.flushToRedis(player.getData().getDataCache(), null, true);
				}				
				if (player.isActiveOnline()) {
					player.kickout(Status.SysError.ADMIN_OPERATION_VALUE, true, "");
				}
			}
		} catch (Exception e) {
			HawkException.catchException(e);						
		}			
		
		//这里不能做player的清理因为 world 那边会重新make 出来，就不做无用功了.
		
		String fromServerId = this.removeImmigrationPlayer(playerId);
		GlobalData.getInstance().removeAccountInfoOnExitCross(playerId);		
		
		HawkLog.warnPrintln("tiberium clear immigration player playerId:{} from serverId:{}", playerId, fromServerId);
	}
	/**
	 * 清除泰伯利亚迁入的玩家.
	 * @param playerId
	 */
	public void clearTiberiumImmigrationPlayer(String playerId) {
		clearTiberiumImmigrationPlayer(playerId, true);
	}
	
	
	public void clearCyborgImmigrationPlayer(String playerId, boolean flushRedis) {
		try {			
			Player player = GlobalData.getInstance().queryPlayer(playerId);
			if (player != null && player.getData() != null && player.getData().getDataCache() != null) {
				if (flushRedis) {
					PlayerDataSerializer.flushToRedis(player.getData().getDataCache(), null, true);
				}				
				if (player.isActiveOnline()) {
					player.kickout(Status.SysError.ADMIN_OPERATION_VALUE, true, "");
				}
			}
		} catch (Exception e) {
			HawkException.catchException(e);						
		}			
		
		//这里不能做player的清理因为 world 那边会重新make 出来，就不做无用功了.
		
		String fromServerId = this.removeImmigrationPlayer(playerId);
		GlobalData.getInstance().removeAccountInfoOnExitCross(playerId);		
		
		HawkLog.warnPrintln("cyborg clear immigration player playerId:{} from serverId:{}", playerId, fromServerId);
	}
	
	
	public void clearDyzzImmigrationPlayer(String playerId, boolean flushRedis) {
		try {			
			Player player = GlobalData.getInstance().queryPlayer(playerId);
			if (player != null && player.getData() != null && player.getData().getDataCache() != null) {
				if (flushRedis) {
					PlayerDataSerializer.flushToRedis(player.getData().getDataCache(), null, true);
				}				
				if (player.isActiveOnline()) {
					player.kickout(Status.SysError.ADMIN_OPERATION_VALUE, true, "");
				}
			}
		} catch (Exception e) {
			HawkException.catchException(e);						
		}			
		
		//这里不能做player的清理因为 world 那边会重新make 出来，就不做无用功了.
		
		String fromServerId = this.removeImmigrationPlayer(playerId);
		GlobalData.getInstance().removeAccountInfoOnExitCross(playerId);		
		
		HawkLog.warnPrintln("dyzz clear immigration player playerId:{} from serverId:{}", playerId, fromServerId);
	}
	
	
	public void clearYqzzImmigrationPlayer(String playerId, boolean flushRedis) {
		try {			
			Player player = GlobalData.getInstance().queryPlayer(playerId);
			if (player != null && player.getData() != null && player.getData().getDataCache() != null) {
				if (flushRedis) {
					PlayerDataSerializer.flushToRedis(player.getData().getDataCache(), null, true);
				}				
				if (player.isActiveOnline()) {
					player.kickout(Status.SysError.ADMIN_OPERATION_VALUE, true, "");
				}
			}
		} catch (Exception e) {
			HawkException.catchException(e);						
		}			
		
		//这里不能做player的清理因为 world 那边会重新make 出来，就不做无用功了.
		
		String fromServerId = this.removeImmigrationPlayer(playerId);
		GlobalData.getInstance().removeAccountInfoOnExitCross(playerId);		
		
		HawkLog.warnPrintln("dyzz clear immigration player playerId:{} from serverId:{}", playerId, fromServerId);
	}

	public void clearXhjzImmigrationPlayer(String playerId, boolean flushRedis) {
		try {
			Player player = GlobalData.getInstance().queryPlayer(playerId);
			if (player != null && player.getData() != null && player.getData().getDataCache() != null) {
				if (flushRedis) {
					PlayerDataSerializer.flushToRedis(player.getData().getDataCache(), null, true);
				}
				if (player.isActiveOnline()) {
					player.kickout(Status.SysError.ADMIN_OPERATION_VALUE, true, "");
				}
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}

		//这里不能做player的清理因为 world 那边会重新make 出来，就不做无用功了.

		String fromServerId = this.removeImmigrationPlayer(playerId);
		GlobalData.getInstance().removeAccountInfoOnExitCross(playerId);

		HawkLog.warnPrintln("dyzz clear immigration player playerId:{} from serverId:{}", playerId, fromServerId);
	}

	public void clearXqhxImmigrationPlayer(String playerId, boolean flushRedis) {
		try {
			Player player = GlobalData.getInstance().queryPlayer(playerId);
			if (player != null && player.getData() != null && player.getData().getDataCache() != null) {
				if (flushRedis) {
					PlayerDataSerializer.flushToRedis(player.getData().getDataCache(), null, true);
				}
				if (player.isActiveOnline()) {
					player.kickout(Status.SysError.ADMIN_OPERATION_VALUE, true, "");
				}
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}

		//这里不能做player的清理因为 world 那边会重新make 出来，就不做无用功了.

		String fromServerId = this.removeImmigrationPlayer(playerId);
		GlobalData.getInstance().removeAccountInfoOnExitCross(playerId);

		HawkLog.warnPrintln("xqhx clear immigration player playerId:{} from serverId:{}", playerId, fromServerId);
	}
	
	
	
	
	/**
	 * 清除泰伯利亚迁入的玩家.
	 * @param playerId
	 */
	public void clearDyzzImmigrationPlayer(String playerId) {
		clearDyzzImmigrationPlayer(playerId, true);
	}
	
	public void clearYqzzImmigrationPlayer(String playerId) {
		clearYqzzImmigrationPlayer(playerId, true);
	}

	public void clearXhjzImmigrationPlayer(String playerId) {
		clearXhjzImmigrationPlayer(playerId, true);
	}

	public void clearXqhxImmigrationPlayer(String playerId) {
		clearXqhxImmigrationPlayer(playerId, true);
	}
	/**
	 * 强制清理玩家的关系链
	 */
	public void clearImmigrationPlayer(String playerId) {
		
		try {
			WorldPlayerService.getInstance().removeCity(playerId, true);
		} catch (Exception e) {
			HawkException.catchException(e);							
		}			
		
		try {
			Player player = GlobalData.getInstance().queryPlayer(playerId);
			if (player != null && player.getData() != null && player.getData().getDataCache() != null) {
				PlayerDataSerializer.flushToRedis(player.getData().getDataCache(), null, true);
				if (player.isActiveOnline()) {
					player.kickout(Status.SysError.ADMIN_OPERATION_VALUE, true, "");
				}
			}
		} catch (Exception e) {
			HawkException.catchException(e);						
		}			
		
		//这里不能做player的清理因为 world 那边会重新make 出来，就不做无用功了.
		
		String fromServerId = this.removeImmigrationPlayer(playerId);
		GlobalData.getInstance().removeAccountInfoOnExitCross(playerId);		
		
		HawkLog.warnPrintln("clear immigration player playerId:{} from serverId:{}", playerId, fromServerId);
	}
		
	/**
	 * 
	 * @param playerId
	 */
	public void clearTiberiumEmigrationPlayer(String playerId) {
		try {
			Player player = GlobalData.getInstance().makesurePlayer(playerId);
			if (player != null ) {
				if (player.isActiveOnline()) {
					player.kickout(Status.SysError.ADMIN_OPERATION_VALUE, true, null);
				}
				
				//这里还是要同步一次redis。尽力吧
				PlayerDataSerializer.csSyncPlayerData(playerId);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		} finally {
			this.removeEmigrationPlayer(playerId);			
		}
		
		DungeonRedisLog.log(playerId,"tiberium clear emigration player playerId:{}", playerId);
	}
	
	/**
	 * 清理迁出的星球大战玩家.
	 * @param playerId
	 */
	public void clearStarWarsEmigrationPlayer(String playerId) {
		try {
			Player player = GlobalData.getInstance().makesurePlayer(playerId);
			if (player != null ) {
				if (player.isActiveOnline()) {
					player.kickout(Status.SysError.ADMIN_OPERATION_VALUE, true, null);
				}
				
				//这里还是要同步一次redis。尽力吧
				PlayerDataSerializer.csSyncPlayerData(playerId);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		} finally {
			this.removeEmigrationPlayer(playerId);			
		}
		
		DungeonRedisLog.log(playerId,"starwars clear emigration player playerId:{}", playerId);
	}
	
	/**
	 * 
	 * @param playerId
	 */
	public void clearCyborgEmigrationPlayer(String playerId) {
		try {
			Player player = GlobalData.getInstance().makesurePlayer(playerId);
			if (player != null ) {
				if (player.isActiveOnline()) {
					player.kickout(Status.SysError.ADMIN_OPERATION_VALUE, true, null);
				}
				
				//这里还是要同步一次redis。尽力吧
				PlayerDataSerializer.csSyncPlayerData(playerId);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		} finally {
			this.removeEmigrationPlayer(playerId);			
		}
		
		DungeonRedisLog.log(playerId,"cyborg clear emigration player playerId:{}", playerId);
	}
	
	/**
	 * 
	 * @param playerId
	 */
	public void clearDyzzEmigrationPlayer(String playerId) {
		try {
			Player player = GlobalData.getInstance().makesurePlayer(playerId);
			if (player != null ) {
				if (player.isActiveOnline()) {
					player.kickout(Status.SysError.ADMIN_OPERATION_VALUE, true, null);
				}
				
				//这里还是要同步一次redis。尽力吧
				PlayerDataSerializer.csSyncPlayerData(playerId);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		} finally {
			this.removeEmigrationPlayer(playerId);			
		}
		
		DungeonRedisLog.log(playerId,"dyzz clear emigration player playerId:{}", playerId);
	}
	
	
	/**
	 * 
	 * @param playerId
	 */
	public void clearYqzzEmigrationPlayer(String playerId) {
		try {
			Player player = GlobalData.getInstance().makesurePlayer(playerId);
			if (player != null ) {
				if (player.isActiveOnline()) {
					player.kickout(Status.SysError.ADMIN_OPERATION_VALUE, true, null);
				}
				
				//这里还是要同步一次redis。尽力吧
				PlayerDataSerializer.csSyncPlayerData(playerId);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		} finally {
			this.removeEmigrationPlayer(playerId);			
		}
		
		DungeonRedisLog.log(playerId,"yqzz clear emigration player playerId:{}", playerId);
	}

	/**
	 *
	 * @param playerId
	 */
	public void clearXhjzEmigrationPlayer(String playerId) {
		try {
			Player player = GlobalData.getInstance().makesurePlayer(playerId);
			if (player != null ) {
				if (player.isActiveOnline()) {
					player.kickout(Status.SysError.ADMIN_OPERATION_VALUE, true, null);
				}

				//这里还是要同步一次redis。尽力吧
				PlayerDataSerializer.csSyncPlayerData(playerId);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		} finally {
			this.removeEmigrationPlayer(playerId);
		}

		DungeonRedisLog.log(playerId,"xhjz clear emigration player playerId:{}", playerId);
	}

	/**
	 *
	 * @param playerId
	 */
	public void clearXqhxEmigrationPlayer(String playerId) {
		try {
			Player player = GlobalData.getInstance().makesurePlayer(playerId);
			if (player != null ) {
				if (player.isActiveOnline()) {
					player.kickout(Status.SysError.ADMIN_OPERATION_VALUE, true, null);
				}

				//这里还是要同步一次redis。尽力吧
				PlayerDataSerializer.csSyncPlayerData(playerId);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		} finally {
			this.removeEmigrationPlayer(playerId);
		}

		DungeonRedisLog.log(playerId,"xqhx clear emigration player playerId:{}", playerId);
	}
	
	
	/**
	 * 强制清理迁出去的玩家.
	 * @param playerId
	 */
	public void clearEmigrationPlayer(String playerId) {
		try {
			Player player = GlobalData.getInstance().makesurePlayer(playerId);
			if (player != null ) {
				if (player.isActiveOnline()) {
					player.kickout(Status.SysError.ADMIN_OPERATION_VALUE, true, null);
				}
				
				//这里还是要同步一次redis。尽力吧
				PlayerDataSerializer.csSyncPlayerData(playerId);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		} finally {
			this.removeEmigrationPlayer(playerId);			
		}
		
		DungeonRedisLog.log(playerId,"clear emigration player playerId:{}", playerId);
	}
	
	@ProtocolHandler(code = HP.code.TIBERIUM_WAR_INNER_BACK_SERVER_REQ_VALUE)
	public void sourceOnTiberiumBackServer(HawkProtocol hawkProtocol) {		
		TiberiumWarInnerBackServerReq req = hawkProtocol.parseProtocol(TiberiumWarInnerBackServerReq.getDefaultInstance());
		
		Player.logger.info("tiberium  inner back server playerId:{}", req.getPlayerId());
		
		Player player = GlobalData.getInstance().makesurePlayer(req.getPlayerId());
		if (player == null) {
			Player.logger.info("tiberium  inner back server playerId:{} makesure player fail", req.getPlayerId());
			
			return;
		}
		
		GsApp.getInstance().postMsg(player.getXid(), new TiberiumBackServerMsg());
	}
	
	@ProtocolHandler(code = HP.code.STAR_WARS_INNER_BACK_SERVER_REQ_VALUE)
	public void sourceOnStarWarsBackServer(HawkProtocol hawkProtocol) {		
		TiberiumWarInnerBackServerReq req = hawkProtocol.parseProtocol(TiberiumWarInnerBackServerReq.getDefaultInstance());
		
		Player.logger.info("starwars  inner back server playerId:{}", req.getPlayerId());
		
		Player player = GlobalData.getInstance().makesurePlayer(req.getPlayerId());
		if (player == null) {
			Player.logger.info("starwars  inner back server playerId:{} makesure player fail", req.getPlayerId());
			
			return;
		}
		
		GsApp.getInstance().postMsg(player.getXid(), new StarWarsBackServerMsg());
	}
	
	@ProtocolHandler(code = HP.code.CYBORG_WAR_INNER_BACK_SERVER_REQ_VALUE)
	public void sourceOnCyborgBackServer(HawkProtocol hawkProtocol) {		
		CyborgWarInnerBackServerReq req = hawkProtocol.parseProtocol(CyborgWarInnerBackServerReq.getDefaultInstance());
		
		Player.logger.info("cyborg  inner back server playerId:{}", req.getPlayerId());
		
		Player player = GlobalData.getInstance().makesurePlayer(req.getPlayerId());
		if (player == null) {
			Player.logger.info("cyborg  inner back server playerId:{} makesure player fail", req.getPlayerId());
			
			return;
		}
		
		GsApp.getInstance().postMsg(player.getXid(), new CyborgBackServerMsg());
	}
	
	@ProtocolHandler(code = HP.code2.DYZZ_WAR_INNER_BACK_SERVER_REQ_VALUE)
	public void sourceOnDyzzBackServer(HawkProtocol hawkProtocol) {		
		PBDYZZWarInnerBackServerReq req = hawkProtocol.parseProtocol(PBDYZZWarInnerBackServerReq.getDefaultInstance());
		Player.logger.info("dyzz  inner back server playerId:{}", req.getPlayerId());
		Player player = GlobalData.getInstance().makesurePlayer(req.getPlayerId());
		if (player == null) {
			Player.logger.info("dyzz  inner back server playerId:{} makesure player fail", req.getPlayerId());
			return;
		}
		GsApp.getInstance().postMsg(player.getXid(), new DYZZBackServerMsg());
	}
	
	@ProtocolHandler(code = HP.code2.YQZZ_WAR_INNER_BACK_SERVER_REQ_VALUE)
	public void sourceOnYqzzBackServer(HawkProtocol hawkProtocol) {		
		PBYQZZWarInnerBackServerReq req = hawkProtocol.parseProtocol(PBYQZZWarInnerBackServerReq.getDefaultInstance());
		Player.logger.info("yqzz  inner back server playerId:{}", req.getPlayerId());
		Player player = GlobalData.getInstance().makesurePlayer(req.getPlayerId());
		if (player == null) {
			Player.logger.info("yqzz  inner back server playerId:{} makesure player fail", req.getPlayerId());
			return;
		}
		GsApp.getInstance().postMsg(player.getXid(), new YQZZBackServerMsg());
	}

	@ProtocolHandler(code = HP.code2.XHJZ_WAR_INNER_BACK_SERVER_REQ_VALUE)
	public void sourceOnXhjzBackServer(HawkProtocol hawkProtocol) {
		XHJZWar.PBXHJZWarInnerBackServerReq req = hawkProtocol.parseProtocol(XHJZWar.PBXHJZWarInnerBackServerReq.getDefaultInstance());
		Player.logger.info("xhjz  inner back server playerId:{}", req.getPlayerId());
		Player player = GlobalData.getInstance().makesurePlayer(req.getPlayerId());
		if (player == null) {
			Player.logger.info("xhjz  inner back server playerId:{} makesure player fail", req.getPlayerId());
			return;
		}
		GsApp.getInstance().postMsg(player.getXid(), new XHJZBackServerMsg());
	}

	@ProtocolHandler(code = HP.code2.XQHX_WAR_INNER_BACK_SERVER_REQ_VALUE)
	public void sourceOnXqhxBackServer(HawkProtocol hawkProtocol) {
		XQHXWar.PBXQHXWarInnerBackServerReq req = hawkProtocol.parseProtocol(XQHXWar.PBXQHXWarInnerBackServerReq.getDefaultInstance());
		Player.logger.info("xqhx inner back server playerId:{}", req.getPlayerId());
		Player player = GlobalData.getInstance().makesurePlayer(req.getPlayerId());
		if (player == null) {
			Player.logger.info("xqhx  inner back server playerId:{} makesure player fail", req.getPlayerId());
			return;
		}
		GsApp.getInstance().postMsg(player.getXid(), new XQHXBackServerMsg());
	}
	
	/**
	 * 跨服结束后,把玩家打回原服,原服处理.
	 * @param hawkProtocol
	 */
	@ProtocolHandler(code = CHP.code.INNER_BACK_SERVER_VALUE)
	public void sourceOnBackServer(HawkProtocol hawkProtocol) {	
		InnerBackServerReq req = hawkProtocol.parseProtocol(InnerBackServerReq.getDefaultInstance());
		
		Player.logger.info("cs player inner back server playerId:{}", req.getPlayerId());
		
		Player player = GlobalData.getInstance().makesurePlayer(req.getPlayerId());
		if (player == null) {
			Player.logger.info("cs player inner back server playerId:{} makesure player fail", req.getPlayerId());
			return;
		}
		
		GsApp.getInstance().postMsg(player.getXid(), new BackServerMsg());
	}
	
	@ProtocolHandler(code = HP.code.CYBORG_WAR_MOVE_BACK_REQ_VALUE) 
	public void targetOnCyborgMoveBack(HawkProtocol hawkProtocol){
		CyborgWarMoveBackReq req = hawkProtocol.parseProtocol(CyborgWarMoveBackReq.getDefaultInstance());
		String playerId = req.getPlayerId();
		if (!this.isImmigrationPlayer(playerId)) {
			Player.logger.error("cyborg playerId:{} is not a immigration player", playerId);
			
			return;
		}
		
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		if (player == null) {
			Player.logger.error("cyborg playerId:{} make sure player fail on move back ", playerId);
			
			return;
		}
		
		CyborgMoveBackCrossPlayerMsg msg = new CyborgMoveBackCrossPlayerMsg();
		GsApp.getInstance().postMsg(player.getXid(), msg);
	}
	
	@ProtocolHandler(code = HP.code.TIBERIUM_WAR_MOVE_BACK_REQ_VALUE) 
	public void targetOnTiberiumMoveBack(HawkProtocol hawkProtocol){
		TiberiumWarMoveBackReq req = hawkProtocol.parseProtocol(TiberiumWarMoveBackReq.getDefaultInstance());
		String playerId = req.getPlayerId();
		if (!this.isImmigrationPlayer(playerId)) {
			Player.logger.error("tiberium playerId:{} is not a immigration player", playerId);
			
			return;
		}
		
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		if (player == null) {
			Player.logger.error("tiberium playerId:{} make sure player fail on move back ", playerId);
			
			return;
		}
		
		TiberiumMoveBackCrossPlayerMsg msg = new TiberiumMoveBackCrossPlayerMsg();
		GsApp.getInstance().postMsg(player.getXid(), msg);
	}
	
	
	@ProtocolHandler(code = HP.code2.DYZZ_WAR_MOVE_BACK_REQ_VALUE) 
	public void targetOnDyzzMoveBack(HawkProtocol hawkProtocol){
		PBDYZZWarMoveBackReq req = hawkProtocol.parseProtocol(PBDYZZWarMoveBackReq.getDefaultInstance());
		String playerId = req.getPlayerId();
		if (!this.isImmigrationPlayer(playerId)) {
			Player.logger.error("dyzz playerId:{} is not a immigration player", playerId);
			return;
		}
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		if (player == null) {
			Player.logger.error("dyzz playerId:{} make sure player fail on move back ", playerId);
			return;
		}
		
		DYZZMoveBackCrossPlayerMsg msg = new DYZZMoveBackCrossPlayerMsg();
		GsApp.getInstance().postMsg(player.getXid(), msg);
	}
	
	
	@ProtocolHandler(code = HP.code2.YQZZ_WAR_MOVE_BACK_REQ_VALUE) 
	public void targetOnYqzzMoveBack(HawkProtocol hawkProtocol){
		PBYQZZWarMoveBackReq req = hawkProtocol.parseProtocol(PBYQZZWarMoveBackReq.getDefaultInstance());
		String playerId = req.getPlayerId();
		if (!this.isImmigrationPlayer(playerId)) {
			Player.logger.error("yqzz playerId:{} is not a immigration player", playerId);
			return;
		}
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		if (player == null) {
			Player.logger.error("yqzz playerId:{} make sure player fail on move back ", playerId);
			return;
		}
		
		YQZZMoveBackCrossPlayerMsg msg = new YQZZMoveBackCrossPlayerMsg();
		GsApp.getInstance().postMsg(player.getXid(), msg);
	}

	@ProtocolHandler(code = HP.code2.XHJZ_WAR_MOVE_BACK_REQ_VALUE)
	public void targetOnXhjzMoveBack(HawkProtocol hawkProtocol){
		XHJZWar.PBXHJZWarMoveBackReq req = hawkProtocol.parseProtocol(XHJZWar.PBXHJZWarMoveBackReq.getDefaultInstance());
		String playerId = req.getPlayerId();
		if (!this.isImmigrationPlayer(playerId)) {
			Player.logger.error("yqzz playerId:{} is not a immigration player", playerId);
			return;
		}
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		if (player == null) {
			Player.logger.error("yqzz playerId:{} make sure player fail on move back ", playerId);
			return;
		}

		XHJZMoveBackCrossPlayerMsg msg = new XHJZMoveBackCrossPlayerMsg();
		GsApp.getInstance().postMsg(player.getXid(), msg);
	}

	@ProtocolHandler(code = HP.code2.XQHX_WAR_MOVE_BACK_REQ_VALUE)
	public void targetOnXqhxMoveBack(HawkProtocol hawkProtocol){
		XQHXWar.PBXQHXWarMoveBackReq req = hawkProtocol.parseProtocol(XQHXWar.PBXQHXWarMoveBackReq.getDefaultInstance());
		String playerId = req.getPlayerId();
		if (!this.isImmigrationPlayer(playerId)) {
			Player.logger.error("xqhx playerId:{} is not a immigration player", playerId);
			return;
		}
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		if (player == null) {
			Player.logger.error("xqhx playerId:{} make sure player fail on move back ", playerId);
			return;
		}

		XQHXMoveBackCrossPlayerMsg msg = new XQHXMoveBackCrossPlayerMsg();
		GsApp.getInstance().postMsg(player.getXid(), msg);
	}
	
	
	@ProtocolHandler(code = CHP.code.CROSS_MOVE_BACK_VALUE)
	public void targetOnMoveBack(HawkProtocol hawkProtocol) {
		CrossMoveBackReq req = hawkProtocol.parseProtocol(CrossMoveBackReq.getDefaultInstance());
		String playerId = req.getPlayerId();
		if (!this.isImmigrationPlayer(playerId)) {
			Player.logger.error("playerId:{} is not a immigration player", playerId);
			
			return;
		}
		
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		if (player == null) {
			Player.logger.error("playerId:{} make sure player fail on move back ", playerId);
			
			return;
		}
		
		MoveBackCrossPlayerMsg msg = new MoveBackCrossPlayerMsg((ProxyHeader)hawkProtocol.getUserData(), req.getForce() > 0);
		GsApp.getInstance().postMsg(player.getXid(), msg);
	}
	
	public  boolean initCrossProxy() {		
		//没有开启跨服就不去初始化cross proxy		
		if (!CrossProxy.getInstance().isInit()) {
			if (!CrossProxy.getInstance().init()) {
				return false;
			}
		}		
		
		return true;
	}

	public Map<String, Integer> getPrepareCrossTime() {
		return prepareCrossTime;
	}

	public void setPrepareCrossTime(Map<String, Integer> prepareCrossTime) {
		this.prepareCrossTime = prepareCrossTime;
	}
	
	/**
	 * 删除并且check这个预跨服时间戳
	 * @param playerId
	 * @return
	 */
	public boolean removeAndCheckPrepareCrossTime(String playerId) {
		Integer prepareTime = prepareCrossTime.remove(playerId);
		if (prepareTime == null) {
			return false;
		}
		
		return Math.abs(HawkTime.getSeconds() - prepareTime) <= GameConstCfg.getInstance().getCrossProtocolValidTime();
	}
	
	/**
	 * 强制退出星球大战
	 * 退出副本的时候.
	 * @param playerId
	 */
	public void addForceMoveBackStarWarsPlayer(String playerId) {
		DungeonRedisLog.log(playerId,"try add force move back starWars playerId:{}", playerId);
		StarWarsConstCfg constCfg = StarWarsConstCfg.getInstance();  
		this.addDelayAction(constCfg.getForceMoveBackTime(), new HawkDelayAction() {
			
			@Override
			protected void doAction() {
				//30秒之后还在迁入玩家列表里面再走一半退出流程.
				if (isImmigrationPlayer(playerId)) {
					StarWarsPrepareExitCrossInstanceMsg exitCrossInstance = new StarWarsPrepareExitCrossInstanceMsg();
					DungeonRedisLog.log(playerId,"try to post starwars prepare exit msg playerId:{}", playerId);
					HawkXID hawkXid = HawkXID.valueOf(GsConst.ObjType.PLAYER, playerId);
					GsApp.getInstance().postMsg(hawkXid, exitCrossInstance);
				} else {
					DungeonRedisLog.log(playerId,"starwars player already back server playerId:{}", playerId);
				}
			}
		});
	}
	
	/**
	 * 退出泰伯利亚
	 * @param playerId
	 */
	public void addForceMoveBackTiberiumPlayer(String playerId) {
		DungeonRedisLog.log(playerId,"try add force move back tiberium playerId:{}", playerId);
		TiberiumConstCfg constCfg = TiberiumConstCfg.getInstance();  
		this.addDelayAction(constCfg.getForceMoveBackTime(), new HawkDelayAction() {
			
			@Override
			protected void doAction() {
				//30秒之后还在迁入玩家列表里面再走一半退出流程.
				if (isImmigrationPlayer(playerId)) {
					TiberiumPrepareExitCrossInstanceMsg exitCrossInstance = new TiberiumPrepareExitCrossInstanceMsg();
					DungeonRedisLog.log(playerId,"try to post tiberium prepare exit msg playerId:{}", playerId);
					HawkXID hawkXid = HawkXID.valueOf(GsConst.ObjType.PLAYER, playerId);
					GsApp.getInstance().postMsg(hawkXid, exitCrossInstance);
				} else {
					DungeonRedisLog.log(playerId,"tiberium player already back server playerId:{}", playerId);
				}
			}
		});
	}
	
	public void addForceMoveBackCyborgPlayer(String playerId) {
		DungeonRedisLog.log(playerId,"try add force move back cyborg playerId:{}", playerId);
		CyborgConstCfg constCfg = CyborgConstCfg.getInstance();  
		this.addDelayAction(constCfg.getForceMoveBackTime(), new HawkDelayAction() {
			
			@Override
			protected void doAction() {
				//30秒之后还在迁入玩家列表里面再走一半退出流程.
				if (isImmigrationPlayer(playerId)) {
					CyborgPrepareExitCrossInstanceMsg  exitCrossInstance = new CyborgPrepareExitCrossInstanceMsg();
					DungeonRedisLog.log(playerId,"try to post cyborg prepare exit msg playerId:{}", playerId);
					HawkXID hawkXid = HawkXID.valueOf(GsConst.ObjType.PLAYER, playerId);
					GsApp.getInstance().postMsg(hawkXid, exitCrossInstance);
				} else {
					DungeonRedisLog.log(playerId,"cyborg player already back server playerId:{}", playerId);
				}
			}
		});
	}
	
	
	public void addForceMoveBackDyzzPlayer(String playerId) {
		DungeonRedisLog.log(playerId,"try add force move back dyzz playerId:{}", playerId);
		DYZZWarCfg constCfg = HawkConfigManager.getInstance().getKVInstance(DYZZWarCfg.class);
		this.addDelayAction(constCfg.getForceMoveBackTime(), new HawkDelayAction() {
			
			@Override
			protected void doAction() {
				//30秒之后还在迁入玩家列表里面再走一半退出流程.
				if (isImmigrationPlayer(playerId)) {
					DYZZPrepareExitCrossInstanceMsg  exitCrossInstance = new DYZZPrepareExitCrossInstanceMsg();
					DungeonRedisLog.log(playerId,"try to post dyzz prepare exit msg playerId:{}", playerId);
					HawkXID hawkXid = HawkXID.valueOf(GsConst.ObjType.PLAYER, playerId);
					GsApp.getInstance().postMsg(hawkXid, exitCrossInstance);
				} else {
					DungeonRedisLog.log(playerId,"dyzz player already back server playerId:{}", playerId);
				}
			}
		});
	}
	
	
	
	public void addForceMoveBackYqzzPlayer(String playerId) {
		DungeonRedisLog.log(playerId,"try add force move back yqzz playerId:{}", playerId);
		YQZZWarConstCfg constCfg = HawkConfigManager.getInstance().getKVInstance(YQZZWarConstCfg.class);
		this.addDelayAction(constCfg.getForceMoveBackTime(), new HawkDelayAction() {
			
			@Override
			protected void doAction() {
				//30秒之后还在迁入玩家列表里面再走一半退出流程.
				if (isImmigrationPlayer(playerId)) {
					YQZZPrepareExitCrossInstanceMsg  exitCrossInstance = new YQZZPrepareExitCrossInstanceMsg();
					DungeonRedisLog.log(playerId,"try to post yqzz prepare exit msg playerId:{}", playerId);
					HawkXID hawkXid = HawkXID.valueOf(GsConst.ObjType.PLAYER, playerId);
					GsApp.getInstance().postMsg(hawkXid, exitCrossInstance);
				} else {
					DungeonRedisLog.log(playerId,"yqzz player already back server playerId:{}", playerId);
				}
			}
		});
	}

	public void addForceMoveBackXhjzPlayer(String playerId) {
		DungeonRedisLog.log(playerId,"try add force move back xhjz playerId:{}", playerId);
		XHJZConstCfg constCfg = HawkConfigManager.getInstance().getKVInstance(XHJZConstCfg.class);
		//todo
		this.addDelayAction(constCfg.getForceMoveBackTime(), new HawkDelayAction() {

			@Override
			protected void doAction() {
				//30秒之后还在迁入玩家列表里面再走一半退出流程.
				if (isImmigrationPlayer(playerId)) {
					XHJZPrepareExitCrossInstanceMsg exitCrossInstance = new XHJZPrepareExitCrossInstanceMsg();
					DungeonRedisLog.log(playerId,"try to post xhjz prepare exit msg playerId:{}", playerId);
					HawkXID hawkXid = HawkXID.valueOf(GsConst.ObjType.PLAYER, playerId);
					GsApp.getInstance().postMsg(hawkXid, exitCrossInstance);
				} else {
					DungeonRedisLog.log(playerId,"xhjz player already back server playerId:{}", playerId);
				}
			}
		});
	}

	public void addForceMoveBackXqhxPlayer(String playerId) {
		DungeonRedisLog.log(playerId,"try add force move back xqhx playerId:{}", playerId);
		XQHXConstCfg constCfg = HawkConfigManager.getInstance().getKVInstance(XQHXConstCfg.class);
		//todo
		this.addDelayAction(constCfg.getForceMoveBackTime(), new HawkDelayAction() {

			@Override
			protected void doAction() {
				//30秒之后还在迁入玩家列表里面再走一半退出流程.
				if (isImmigrationPlayer(playerId)) {
					XQHXPrepareExitCrossInstanceMsg exitCrossInstance = new XQHXPrepareExitCrossInstanceMsg();
					DungeonRedisLog.log(playerId,"try to post xqhx prepare exit msg playerId:{}", playerId);
					HawkXID hawkXid = HawkXID.valueOf(GsConst.ObjType.PLAYER, playerId);
					GsApp.getInstance().postMsg(hawkXid, exitCrossInstance);
				} else {
					DungeonRedisLog.log(playerId,"xqhx player already back server playerId:{}", playerId);
				}
			}
		});
	}
	
	/**
	 * 修复星球大战.
	 * @param realServerId
	 * @param playerId
	 * @return
	 */
	public boolean loginFixStarWarsPlayer(String realServerId, String playerId) {
		int status = RedisProxy.getInstance().getPlayerCrossStatus(realServerId, playerId);
		if (status < 0) {
			DungeonRedisLog.log(playerId,"playerId:{} getPlayerCrossStatus fail", playerId);

			return false;
		}

		// 只有在跨服中的状态才需要处理.
		if (status == PlayerCrossStatus.PREPARE_EXIT_CROSS) {
			// 把数据从redis反序列化回来.
			PlayerDataSerializer.csSyncPlayerData(playerId, true);

			// 从跨出玩家集合删除
			this.removeEmigrationPlayer(playerId);

			return true;
		} else {
			Player player = GlobalData.getInstance().makesurePlayer(playerId);
			// 判断一下时间. 时间过期了则修复.
			StarWarsActivityService starWarsService = StarWarsActivityService.getInstance();
			StarWarsConstCfg constConfig = StarWarsConstCfg.getInstance();
			boolean needFix = false;
			SWRoomData roomData = starWarsService.getCurrRoomData(player);
			if (roomData == null || !roomData.isActive(constConfig.getPerioTime())) {
				needFix = true;
				DungeonRedisLog.log(playerId,"starwars player login fix room check id:{}", playerId);
			} else {
				SWPlayerData swPlayerData = starWarsService.getCurrentPlayerData(player);
				if (swPlayerData == null) {
					DungeonRedisLog.log(playerId,"starwars player login fix swPlayerData check id:{}", playerId);
					needFix = true;
				}
			}

			if (needFix) {
				DungeonRedisLog.log(playerId,"starwars player login fix id:{}", playerId);
				String toServerId = this.getEmigrationPlayerServerId(playerId);
				// 把数据从redis反序列化回来.
				PlayerDataSerializer.csSyncPlayerData(playerId, true);
				// 清除redis信息.
				this.removeEmigrationPlayer(playerId);
				// 清理本地的信息,
				RedisProxy.getInstance().removeImmigrationPlayer(toServerId, playerId);
				// 清理对方服的信息.
				HawkTaskManager.getInstance().postExtraTask(new HawkTask() {

					@Override
					public Object run() {
						GmProxyHelper.proxyCall(toServerId, "starWarsMoveBack", "opt=4&playerId" + playerId, 2000);
						return null;
					}
				});

				return true;
			}

			return false;
		}
	}

	
	@Override
	public boolean onProtocol(HawkProtocol protocol) {
		boolean dYZZMatchProtocal = DYZZMatchService.getInstance().isListenProto(protocol.getType());
		if(dYZZMatchProtocal){
			HawkTaskManager.getInstance().postProtocol(DYZZMatchService.getInstance().getXid(), protocol);
			return true;
		}
		boolean dYZZProtocal = DYZZService.getInstance().isListenProto(protocol.getType());
		if(dYZZProtocal){
			HawkTaskManager.getInstance().postProtocol(DYZZService.getInstance().getXid(), protocol);
			return true;
		}
		boolean xqhxProtocal = XQHXWarService.getInstance().isListenProto(protocol.getType());
		if(xqhxProtocal){
			HawkTaskManager.getInstance().postProtocol(XQHXWarService.getInstance().getXid(), protocol);
			return true;
		}
		if(MeterialTransportService.getInstance().isListenProto(protocol.getType())){
			HawkTaskManager.getInstance().postProtocol(MeterialTransportService.getInstance().getXid(), protocol);
			return true;
		}
		if(HomeLandService.getInstance().isListenProto(protocol.getType())){
			HawkTaskManager.getInstance().postProtocol(HomeLandService.getInstance().getXid(), protocol);
			return true;
		}
		return super.onProtocol(protocol);
	}

	/**
	 * 迁服处理(A迁B，B处理)
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code2.IMMGRATION_SERVER_REQ_VALUE)
	private void onImmgrationReq(HawkProtocol protocol) {
		ImmgrationService.getInstance().onImmgrationReq(protocol);
	}
	
	/**
	 * 迁服处理(A迁B，B返回，这里A接收B的返回)
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code2.IMMGRATION_SERVER_RESP_VALUE)
	private void onImmgrationResp(HawkProtocol protocol) {
		ImmgrationService.getInstance().onImmgrationResp(protocol);
	}
	
	/**
	 * 迁服服务器信息获取
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code2.IMMGRATION_SERVER_DETAIL_INFO_RESP_VALUE)
	private void onImmgrationServerInfoReq(HawkProtocol protocol) {
		ImmgrationServerInfoDetailResp builderInfo = protocol.parseProtocol(ImmgrationServerInfoDetailResp.getDefaultInstance());
		
		ImmgrationServerInfoDetailResp.Builder builder = ImmgrationServerInfoDetailResp.newBuilder();
		builder.setServerId(builderInfo.getServerId());
		builder.setServerName(builderInfo.getServerName());
		builder.setServerType(builderInfo.getServerType());
		builder.setNumLimit(builderInfo.getNumLimit());
		builder.setPowerLimit(builderInfo.getPowerLimit());
		builder.setCurrentNum(builderInfo.getCurrentNum());
		builder.addAllRankInfo(RankService.getInstance().getRankCache(RankType.ALLIANCE_FIGHT_KEY, 10));
		builder.addAllRankInfo(RankService.getInstance().getRankCache(RankType.ALLIANCE_KILL_ENEMY_KEY, 10));
		builder.setImmgrationCost(builderInfo.getImmgrationCost());
		HawkProtocol sendProtocol = HawkProtocol.valueOf(HP.code2.IMMGRATION_SERVER_DETAIL_INFO_RESP, builder);
		
		ProxyHeader proxyHeader = (ProxyHeader)protocol.getUserData();
		String playerId = proxyHeader.getSource();
		CrossProxy.getInstance().sendProtocol(sendProtocol, proxyHeader.getFrom(), playerId);
	}
	
	@ProtocolHandler(code = HP.code2.CROSS_PRESIDENT_MAIL_VALUE)
	private void crossPresidentMail(HawkProtocol protocol) {
		KeyValuePairStr req = protocol.parseProtocol(KeyValuePairStr.getDefaultInstance());
		String[] params = req.getVal().split(":");
		CrossActivityService.getInstance().sendPresidentWinMail(params[0], params[1]);
	}
	
	/**
	 * 跨服修改国家科技值
	 * @param protocol
	 */
	@ProtocolHandler(code = CHP.code.CROSS_CHANGE_TECH_VALUE_VALUE)
	private void onChangeNationTech(HawkProtocol protocol) {
		KeyValuePairStr req = protocol.parseProtocol(KeyValuePairStr.getDefaultInstance());
		NationTechCenter center = (NationTechCenter)NationService.getInstance().getNationBuildingByType(NationbuildingType.NATION_TECH_CENTER);
		if (center == null || center.getLevel() <= 0) {
			return;
		}
		center.changeNationTechValue(req.getKey());
	}
	
	/**
	 * 跨服使用国家科技技能
	 * @param protocol
	 */
	@ProtocolHandler(code = CHP.code.CROSS_NATATION_TECH_SKILL_USE_VALUE)
	private void onCrossNationTechSkillUse(HawkProtocol protocol) {
		CrossNationTechSkillUse req = protocol.parseProtocol(CrossNationTechSkillUse.getDefaultInstance());
		NationTechCenter center = (NationTechCenter)NationService.getInstance().getNationBuildingByType(NationbuildingType.NATION_TECH_CENTER);
		if (center == null || center.getLevel() <= 0) {
			return;
		}
		center.updateNationTechSkillInfo(req.getTechId(), req.getEffectStartTime());
	}
	
	
	/**
	 * 迁服服务器信息获取
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code2.BACK_CROSS_IMMGRATION_SERVER_RANK_REQ_VALUE)
	private void onBackCrossImmgrationServerRankReq(HawkProtocol protocol) {
		PBCrossBackImmgrationTargetServerRankReq builderInfo = protocol.parseProtocol(PBCrossBackImmgrationTargetServerRankReq.getDefaultInstance());
		String playerId = builderInfo.getPlayerId();
		long noArmyPower = builderInfo.getPower();
		int selfPowerRank =0;
		int cityLevel = builderInfo.getCityLevel();
		int selfCityRank = 0;
		
		PBBackImmgrationTargetServerRankResp.Builder builderResp = PBBackImmgrationTargetServerRankResp.newBuilder();
		builderResp.setPlayerId(playerId);
		builderResp.addAllPowerRank(RankService.getInstance().getRankCache(RankType.PLAYER_NOARMY_POWER_RANK, 30));
		builderResp.addAllCityRank(RankService.getInstance().getRankCache(RankType.PLAYER_CASTLE_KEY, 30));
		
		Map<String, HawkTuple2<Integer, Long>> powerCache = RankService.getInstance().getRankDataMapCache(RankType.PLAYER_NOARMY_POWER_RANK);
		for(HawkTuple2<Integer, Long> tupe: powerCache.values()){
			if(tupe.second >= noArmyPower && selfPowerRank < tupe.first){
				selfPowerRank = tupe.first;
			}
		}
		if(selfPowerRank == 0){
			selfPowerRank = 1;
		}else{
			selfPowerRank += 1;
		}
		Map<String, HawkTuple2<Integer, Long>> cityCache = RankService.getInstance().getRankDataMapCache(RankType.PLAYER_CASTLE_KEY);
		for(HawkTuple2<Integer, Long> tupe: cityCache.values()){
			if(tupe.second >= cityLevel && selfCityRank < tupe.first){
				selfCityRank = tupe.first;
			}
		}
		if(selfCityRank == 0){
			selfCityRank = 1;
		}else{
			selfCityRank += 1;
		}
		builderResp.setCityRankSelf(selfCityRank);
		builderResp.setPowerRankSelf(selfPowerRank);
		
		HawkProtocol sendProtocol = HawkProtocol.valueOf(HP.code2.BACK_IMMGRATION_SERVER_RANK_RESP, builderResp);
		ProxyHeader proxyHeader = (ProxyHeader)protocol.getUserData();
		CrossProxy.getInstance().sendProtocol(sendProtocol, proxyHeader.getFrom(), playerId);
	}

	public static final String NEW_START_ACTIVE_TIME = "NEW_START:ACTIVE_TIME:";

	public long getNewStartActiveTime(Player player){
		String key = NEW_START_ACTIVE_TIME + player.getOpenId();
		String timeStr = RedisProxy.getInstance().getRedisSession().getString(key);
		if(!HawkOSOperator.isEmptyString(timeStr)){
			return Long.parseLong(timeStr);
		}
		return 0;
	}

	public void updateNewStartActiveTime(Player player){
		String key = NEW_START_ACTIVE_TIME+player.getOpenId();
		String now = String.valueOf(HawkTime.getMillisecond());
		RedisProxy.getInstance().getRedisSession().setString(key, now);
	}


	public void newStartCheck(Player player){
		try {
			NewStartBaseCfg baseCfg = HawkConfigManager.getInstance().getKVInstance(NewStartBaseCfg.class);
			long now = HawkTime.getMillisecond();
			long lastTime = getNewStartActiveTime(player);
			if(now - lastTime < baseCfg.getIntervalTime()){
				return;
			}
			List<AccountRoleInfo> roleList = InheritNewService.getInstance().getPlayerAccountInfos(player);
			roleList.sort((o1, o2) -> {
				if (o1.getVipLevel() != o2.getVipLevel()) {
					return o1.getVipLevel() > o2.getVipLevel() ? -1 : 1;
				}
				if (o1.getCityLevel() != o2.getCityLevel()) {
					return o1.getCityLevel() > o2.getCityLevel() ? -1 : 1;
				}
				if (o1.getBattlePoint() != o2.getBattlePoint()) {
					return o1.getBattlePoint() > o2.getBattlePoint() ? -1 : 1;
				}
				if (o1.getRegisterTime() != o2.getRegisterTime()) {
					return o1.getRegisterTime() < o2.getRegisterTime() ? -1 : 1;
				}
				return 0;
			});
			AccountRoleInfo roleInfo = null;
			for(AccountRoleInfo info : roleList){
				if(info.getVipLevel() >= baseCfg.getVipLimit()
						&& info.getCityLevel() >= baseCfg.getBaseLimit()
						&& info.getRegisterTime() <= now - TimeUnit.DAYS.toMillis(baseCfg.getTimeLimit())){
					roleInfo = info;
					break;
				}
			}
			if(roleInfo == null){
				return;
			}
			updateNewStartActiveTime(player);
			Activity.NewStartCSInfoReq.Builder req = Activity.NewStartCSInfoReq.newBuilder();
			req.setNewPlayerId(player.getId());
			req.setNewServerId(GsConfig.getInstance().getServerId());
			req.setPlayerId(roleInfo.getPlayerId());
			String mainServerId = GlobalData.getInstance().getMainServerId(roleInfo.getServerId());
			HawkProtocol reqProtocol = HawkProtocol.valueOf(CHP.code.NEW_START_CS_INFO_REQ_VALUE, req);
			CrossProxy.getInstance().sendNotify(reqProtocol, mainServerId, player.getId(), null);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	public int getHeroCount(Player player){
		int heroCount = 0;
		for (PlayerHero hero : player.getAllHero()) {
			if(hero.getConfig().getQualityColor() == 5){
				heroCount ++;
			}
		}
		return heroCount;
	}

	public int getEquipTechLevel(Player player){
		int equipTechLevel = 0;
		for(EquipResearchEntity researchEntity : player.getData().getEquipResearchEntityList()){
			equipTechLevel += researchEntity.getResearchLevel();
		}
		return equipTechLevel;
	}

	public int getJijiaLevel(Player player){
		int jijiaLevel = 0;
		for(SuperSoldier superSoldier : player.getAllSuperSoldier()){
			if(superSoldier.getStar() > jijiaLevel){
				jijiaLevel = superSoldier.getStar();
			}
		}
		return jijiaLevel;
	}

	@ProtocolHandler(code = CHP.code.NEW_START_CS_INFO_REQ_VALUE)
	public void newStartCSInfoReq(HawkProtocol protocol){
		Activity.NewStartCSInfoReq req = protocol.parseProtocol(Activity.NewStartCSInfoReq.getDefaultInstance());
		String playerId = req.getPlayerId();
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		if(player == null){
			return;
		}
		String name = player.getName();
		int icon = player.getIcon();
		String pfIcon = player.getPfIcon();
		String oldPlayerId = player.getId();
		String oldServerId = player.getServerId();
		int playerLevel = player.getLevel();
		int vipLevel = player.getVipLevel();
		int baseLevel = player.getCityLevel();
		int heroCount = getHeroCount(player);
		int equipTechLevel = getEquipTechLevel(player);
		int jijiaLevel = getJijiaLevel(player);
		Activity.NewStartCSInfoResp.Builder resp = Activity.NewStartCSInfoResp.newBuilder();
		resp.setPlayerId(req.getNewPlayerId());
		resp.setName(name);
		resp.setIcon(icon);
		resp.setPfIcon(pfIcon);
		resp.setOldPlayerId(oldPlayerId);
		resp.setOldServerId(oldServerId);
		resp.setPlayerLevel(playerLevel);
		resp.setVipLevel(vipLevel);
		resp.setBaseLevel(baseLevel);
		resp.setHeroCount(heroCount);
		resp.setEquipTechLevel(equipTechLevel);
		resp.setJijiaLevel(jijiaLevel);
		HawkProtocol respProtocol = HawkProtocol.valueOf(CHP.code.NEW_START_CS_INFO_RESP_VALUE, resp);
		CrossProxy.getInstance().sendNotify(respProtocol, req.getNewServerId(), playerId, null);

	}

	@ProtocolHandler(code = CHP.code.NEW_START_CS_INFO_RESP_VALUE)
	public void newStartCSInfoResp(HawkProtocol protocol){
		Activity.NewStartCSInfoResp resp = protocol.parseProtocol(Activity.NewStartCSInfoResp.getDefaultInstance());
		NewStartActiveEvent event = new NewStartActiveEvent(resp.getPlayerId(), resp.getName(), resp.getIcon(), resp.getPfIcon(), resp.getOldPlayerId(), resp.getOldServerId(),
				resp.getPlayerLevel(), resp.getVipLevel(), resp.getBaseLevel(), resp.getHeroCount(), resp.getEquipTechLevel(), resp.getJijiaLevel());
		ActivityManager.getInstance().postEvent(event);
	}
}
