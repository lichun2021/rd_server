package com.hawk.game.module;

import static com.hawk.game.util.GsConst.ModuleType.AUTO_GATHER;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.TreeSet;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.collections4.CollectionUtils;
import org.hawk.annotation.MessageHandler;
import org.hawk.annotation.ProtocolHandler;
import org.hawk.app.HawkApp;
import org.hawk.config.HawkConfigManager;
import org.hawk.delay.HawkDelayAction;
import org.hawk.helper.HawkAssert;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.task.HawkDelayTask;
import org.hawk.task.HawkTaskManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.googlecode.protobuf.format.JsonFormat;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.event.impl.SummonMonsterEvent;
import com.hawk.activity.type.impl.radiationWarTwo.RadiationWarTwoActivity;
import com.hawk.common.AccountRoleInfo;
import com.hawk.common.IDIPBanInfo;
import com.hawk.game.GsApp;
import com.hawk.game.GsConfig;
import com.hawk.game.city.CityManager;
import com.hawk.game.config.ConstProperty;
import com.hawk.game.config.CrossConstCfg;
import com.hawk.game.config.FoggyFortressCfg;
import com.hawk.game.config.GameConstCfg;
import com.hawk.game.config.ItemCfg;
import com.hawk.game.config.ProtoCheckConfig;
import com.hawk.game.config.ResTreasureCfg;
import com.hawk.game.config.ShopCfg;
import com.hawk.game.config.VipCfg;
import com.hawk.game.config.VipSuperCfg;
import com.hawk.game.config.WorldEnemyCfg;
import com.hawk.game.config.WorldMapConstProperty;
import com.hawk.game.crossproxy.CrossProxy;
import com.hawk.game.crossproxy.CrossService;
import com.hawk.game.crossproxy.model.CsPlayer;
import com.hawk.game.entity.ArmyEntity;
import com.hawk.game.entity.PlayerMonsterEntity;
import com.hawk.game.entity.StatusDataEntity;
import com.hawk.game.entity.StoryMissionEntity;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.LocalRedis;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.guild.manor.AbstractBuildable;
import com.hawk.game.invoker.GenerateMonsterMsgInvoker;
import com.hawk.game.invoker.WorldCityDefRecoverMsgInvoker;
import com.hawk.game.invoker.WorldMoveCityMsgInvoker;
import com.hawk.game.item.ConsumeItems;
import com.hawk.game.log.BehaviorLogger;
import com.hawk.game.log.BehaviorLogger.Params;
import com.hawk.game.manager.AssembleDataManager;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.march.AutoMonsterMarchParam;
import com.hawk.game.march.AutoMonsterMarchParam.AutoMarchInfo;
import com.hawk.game.march.MarchSet;
import com.hawk.game.module.autologic.PlayerAutoModule;
import com.hawk.game.module.dayazhizhan.playerteam.service.DYZZService;
import com.hawk.game.module.dayazhizhan.playerteam.service.DYZZTeamRoom;
import com.hawk.game.module.spacemecha.MechaSpaceConst.SpacePointIndex;
import com.hawk.game.module.spacemecha.MechaSpaceInfo;
import com.hawk.game.module.spacemecha.SpaceMechaService;
import com.hawk.game.module.spacemecha.worldpoint.SpaceWorldPoint;
import com.hawk.game.msg.AutoSearchMonsterMsg;
import com.hawk.game.msg.GuildJoinMsg;
import com.hawk.game.msg.WorldMoveCityMsg;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerModule;
import com.hawk.game.player.PlayerWorldMoveTask;
import com.hawk.game.player.hero.NPCHero;
import com.hawk.game.player.hero.NPCHeroFactory;
import com.hawk.game.player.hero.PlayerHero;
import com.hawk.game.player.manhattan.PlayerManhattanModule;
import com.hawk.game.player.skill.talent.Skill10104;
import com.hawk.game.player.skill.talent.TalentSkillContext;
import com.hawk.game.player.supersoldier.SuperSoldier;
import com.hawk.game.protocol.Armour.ArmourSuitType;
import com.hawk.game.protocol.Army.ArmyChangeCause;
import com.hawk.game.protocol.Army.ArmySoldierPB;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.GuildPositon;
import com.hawk.game.protocol.Const.PlayerAttr;
import com.hawk.game.protocol.Const.TerritoryType;
import com.hawk.game.protocol.Cross.CrossType;
import com.hawk.game.protocol.GuildManor.GuildBuildingNorStat;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.IDIP.NoticeMode;
import com.hawk.game.protocol.IDIP.NoticeType;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.MechaCore.MechaCoreSuitType;
import com.hawk.game.protocol.Player.MsgCategory;
import com.hawk.game.protocol.SpaceMecha.SpaceMechaStage;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.Status.AutoGatherErr;
import com.hawk.game.protocol.SuperWeapon.SuperWeaponPeriod;
import com.hawk.game.protocol.Talent.TalentType;
import com.hawk.game.protocol.WarFlag.FlageState;
import com.hawk.game.protocol.World.AutoMarchPB;
import com.hawk.game.protocol.World.CityMoveType;
import com.hawk.game.protocol.World.FoggyDetailInfo;
import com.hawk.game.protocol.World.GenWorldPointReq;
import com.hawk.game.protocol.World.GetFoggyDetailInfo;
import com.hawk.game.protocol.World.GuildMoveCityResp;
import com.hawk.game.protocol.World.ModelType;
import com.hawk.game.protocol.World.MonsterType;
import com.hawk.game.protocol.World.PlayerEnterWorld;
import com.hawk.game.protocol.World.PlayerLeaveWorld;
import com.hawk.game.protocol.World.PlayerPresetMarchInfo;
import com.hawk.game.protocol.World.PlayerWorldMove;
import com.hawk.game.protocol.World.PresetMarchInfo;
import com.hawk.game.protocol.World.PresetMarchNameChangeReq;
import com.hawk.game.protocol.World.ReqWorldPointDetail;
import com.hawk.game.protocol.World.RespWorldPointDetail;
import com.hawk.game.protocol.World.SearchType;
import com.hawk.game.protocol.World.ShareCoordinateReq;
import com.hawk.game.protocol.World.SuperBarrackMapInfo;
import com.hawk.game.protocol.World.SwitchAtkMonsterAutoMarchReq;
import com.hawk.game.protocol.World.WorldFavoriteAddReq;
import com.hawk.game.protocol.World.WorldFavoriteDelteReq;
import com.hawk.game.protocol.World.WorldFavoritePB;
import com.hawk.game.protocol.World.WorldFavoriteUpdateReq;
import com.hawk.game.protocol.World.WorldMarchReq;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.protocol.World.WorldMoveCityReq;
import com.hawk.game.protocol.World.WorldMoveCityResp;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.protocol.World.WorldSearchReq;
import com.hawk.game.protocol.World.WorldSearchResp;
import com.hawk.game.protocol.World.WorldSecondaryAllianceInfo;
import com.hawk.game.protocol.World.WorldSecondaryMapInfo;
import com.hawk.game.protocol.World.WorldSecondaryMapReq;
import com.hawk.game.service.ArmyService;
import com.hawk.game.service.GuildManorService;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.RelationService;
import com.hawk.game.service.WarFlagService;
import com.hawk.game.service.chat.ChatService;
import com.hawk.game.service.flag.FlagCollection;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mail.SystemMailService;
import com.hawk.game.service.mssion.MissionManager;
import com.hawk.game.service.mssion.event.EventGenOldMonsterMarch;
import com.hawk.game.service.mssion.event.EventMoveCity;
import com.hawk.game.service.warFlag.IFlag;
import com.hawk.game.superweapon.SuperWeaponService;
import com.hawk.game.superweapon.weapon.IWeapon;
import com.hawk.game.tsssdk.GameMsgCategory;
import com.hawk.game.tsssdk.GameTssService;
import com.hawk.game.util.AlgorithmPoint;
import com.hawk.game.util.EffectParams;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.GsConst.AutoSearchMonsterResultCode;
import com.hawk.game.util.GsConst.IDIPBanType;
import com.hawk.game.util.GsConst.ModuleType;
import com.hawk.game.util.GuildUtil;
import com.hawk.game.util.LogUtil;
import com.hawk.game.util.WorldUtil;
import com.hawk.game.warcollege.WarCollegeInstanceService;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.WorldScene;
import com.hawk.game.world.march.IWorldMarch;
import com.hawk.game.world.march.submarch.IReportPushMarch;
import com.hawk.game.world.object.AreaObject;
import com.hawk.game.world.object.DistancePoint;
import com.hawk.game.world.object.FoggyInfo;
import com.hawk.game.world.object.MapBlock;
import com.hawk.game.world.object.Point;
import com.hawk.game.world.service.WorldChristmasWarService;
import com.hawk.game.world.service.WorldFoggyFortressService;
import com.hawk.game.world.service.WorldGundamService;
import com.hawk.game.world.service.WorldMonsterService;
import com.hawk.game.world.service.WorldNianService;
import com.hawk.game.world.service.WorldPlayerService;
import com.hawk.game.world.service.WorldPointService;
import com.hawk.game.world.service.WorldResTreasurePointService;
import com.hawk.game.world.service.WorldRobotService;
import com.hawk.game.world.service.WorldSnowballService;
import com.hawk.game.world.thread.WorldTask;
import com.hawk.game.world.thread.WorldThreadScheduler;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.Action;
import com.hawk.log.LogConst.ArmyChangeReason;
import com.hawk.log.LogConst.ArmySection;
import com.hawk.log.Source;

/**
 * 玩家世界模块
 *
 * @author hawk
 */
public class PlayerWorldModule extends PlayerModule {
	static Logger logger = LoggerFactory.getLogger("Server");
	/**
	 * 当前同步的行军
	 */
	private MarchSet inviewMarchs;

	private PlayerWorldMoveTask worldMoveTask = new PlayerWorldMoveTask();
	/**
	 * 缓存数据
	 */
	private Map<Integer, PresetMarchInfo> reqInfoCacheMap = new HashMap<>();
	private AtomicInteger incrementor = new AtomicInteger(0);
	
	/**
	 * 构造函数
	 *
	 * @param player
	 */
	public PlayerWorldModule(Player player) {
		super(player);
		inviewMarchs = new MarchSet();
	}
	
	

	@Override
	public boolean onTick() {
		worldMoveTick();
		return super.onTick();
	}



	private void worldMoveTick() {
		if (worldMoveTask.getTickPeriod() == 0) {
			ProtoCheckConfig checkConfig = HawkConfigManager.getInstance().getConfigByKey(ProtoCheckConfig.class, HP.code.PLAYER_WORLD_MOVE_VALUE);
			if (Objects.nonNull(checkConfig)) {
				worldMoveTask.setTickPeriod(checkConfig.getTickPeriod());
			}
		}
		long now = HawkApp.getInstance().getCurrentTime();
		if (worldMoveTask.isNeedSync() && now - worldMoveTask.getLastSend() > worldMoveTask.getTickPeriod()) {
			playerWorldMove(worldMoveTask.getProtocol());
			worldMoveTask.setNeedSync(false);
			worldMoveTask.setLastSend(now);
		}
	}



	/**
	 * 玩家组装完成, 主要用来后期数据同步
	 */
	@Override
	protected boolean onPlayerAssemble() {
		WorldThreadScheduler.getInstance().postWorldTask(new WorldTask(GsConst.WorldTaskType.MOVE_CITY) {
			@Override
			public boolean onInvoke() {
				int[] posInfo = WorldPlayerService.getInstance().getPlayerPosXY(player.getId());
				if (posInfo[0] > 0 && posInfo[1] > 0) {
					return true;
				}
								
				boolean isFirstCrossServerLogin = false;
				if (player.isCsPlayer()) {
					CsPlayer csPlayer = player.getCsPlayer();
					//暂时是只有跨服初始化城点,其它跨服类型暂不做处理.
					if (csPlayer.isCrossType(CrossType.CROSS_VALUE)) {
						isFirstCrossServerLogin = csPlayer.isFirstCrossServerLogin(CrossType.CROSS_VALUE);
						if (isFirstCrossServerLogin) {					
							crossMoveCity();						
						} else {
							WorldPlayerService.getInstance().moveCity(player);						
							// 同步
							int[] playerPos = WorldPlayerService.getInstance().getPlayerPosXY(player.getId());
							if (playerPos[0] > 0 && playerPos[1] > 0) {
								WorldPlayerService.getInstance().sendCityWorldPoint(player, playerPos[0], playerPos[1]);
							}
						}
					}					
				} else {
					WorldPlayerService.getInstance().moveCity(player);				
					// 同步
					int[] playerPos = WorldPlayerService.getInstance().getPlayerPosXY(player.getId());
					if (playerPos[0] > 0 && playerPos[1] > 0) {
						WorldPlayerService.getInstance().sendCityWorldPoint(player, playerPos[0], playerPos[1]);
					}
				} 
				
				
								
				return true;
			}
		});
		return true;
	}

	/**
	 * 玩家上线处理, 数据同步
	 *
	 * @return
	 */
	@Override
	protected boolean onPlayerLogin() {

		inviewMarchs.clear();

		// 自己世界信息通报
		int[] posInfo = WorldPlayerService.getInstance().getPlayerPosXY(player.getId());
		if (posInfo[0] > 0 && posInfo[1] > 0) {
			WorldPlayerService.getInstance().sendCityWorldPoint(player, posInfo[0], posInfo[1]);
		}

		// 地图收藏夹信息通报
		player.getPush().syncWorldFavorite();
		player.setAoiObjId(0);

		// 同步野怪击杀等级
		player.getPush().syncMonsterKilled(0, true);
		
		// 同步世界上最大等级野怪
		player.getPush().syncMaxMonsterLevel();
		
		// 同步玩家预设信息
		PlayerPresetMarchInfo.Builder infos = makeMarchPresetBuilder();
		if(infos != null) {
			player.sendProtocol(HawkProtocol.valueOf(HP.code.GET_WORLD_MARCH_PRESET_INFO_S_VALUE, infos));
		}
		
		// 登录时若处于自动打野状态，要告诉客户端
		AutoMonsterMarchParam autoMarchParam = WorldMarchService.getInstance().getAutoMarchParam(player.getId());
		if (autoMarchParam != null && !autoMarchParam.isAutoMarchCDEnd()) {
			// 同步状态信息
			WorldMarchService.getInstance().pushAutoMarchStatus(player, 1);
		}
		
		long onFireEnd = player.getData().getPlayerBaseEntity().getOnFireEndTime();
		WorldPlayerService.getInstance().resetCityFireStatus(player, onFireEnd > HawkApp.getInstance().getCurrentTime() ? onFireEnd : 0);

		player.getData().updatePlayerPos(posInfo[0], posInfo[1]);
		
		if (player.isCsPlayer()) {
			CsPlayer csPlayer = (CsPlayer) player;
			if (csPlayer.isFirstCrossServerLogin()) {
				pushPlayerPos(posInfo);
			}
		}
		
		WorldSnowballService.getInstance().pushPlayerLastKick(player.getId());
		
		long cityShieldTime = player.getData().getCityShieldTime();
		if (cityShieldTime > HawkTime.getMillisecond()) {
			WorldPlayerService.getInstance().updateWorldPointProtected(player.getId(), cityShieldTime);
		}
		
		//同步数据
		player.getPush().syncMonsterKillData();
		logger.info("player pos, playerId:{}, x:{}, y:{}", player.getId(), posInfo[0], posInfo[1]);
		return true;
	}

	/**
	 * 同步玩家城点
	 * @param posInfo
	 */
	private void pushPlayerPos(int[] posInfo) {
		// 回复协议
		WorldMoveCityResp.Builder builder = WorldMoveCityResp.newBuilder();
		builder.setResult(true);
		builder.setX(posInfo[0]);
		builder.setY(posInfo[1]);
		player.sendProtocol(HawkProtocol.valueOf(HP.code.WORLD_MOVE_CITY_S_VALUE, builder));
	}

	/**
	 * 玩家下线处理
	 *
	 * @return
	 */
	@Override
	protected boolean onPlayerLogout() {
		if (player.getAoiObjId() > 0) {
			inviewMarchs.clear();

			// 从世界离开
			WorldScene.getInstance().leave(player.getAoiObjId());
			// 从行军观察者离开
			WorldMarchService.getInstance().onPlayerLeave(player);
			player.setAoiObjId(0);
		}
		
		AutoMonsterMarchParam autoMarchParam = WorldMarchService.getInstance().getAutoMarchParam(player.getId());
		// 玩家下线时若开启自动打野的时长达到指定时长后，就自动关闭
		if (autoMarchParam != null && autoMarchParam.isAutoMarchCDEnd()) {
			WorldMarchService.getInstance().closeAutoMarch(player.getId());
		}
		
		return true;
	}	
	
	/**
	 * 第一次跨服生成城点
	 */
	public void crossMoveCity() {
		int x = player.getHpLogin().getInnerEnterCrossMsg().getEnterCrossMsg().getX();
		int y = player.getHpLogin().getInnerEnterCrossMsg().getEnterCrossMsg().getY();
				
		WorldPlayerService.getInstance().removeCity(player.getId(), true);
		
		WorldPoint point = WorldPlayerService.getInstance().mantualSettleCity(player, x, y, 0);
		if (point == null) {
			Point randpoint = WorldPlayerService.getInstance().randomSettlePoint(player, false);
			point = new WorldPoint(randpoint.getX(), randpoint.getY(), randpoint.getAreaId(), randpoint.getZoneId(), WorldPointType.PLAYER_VALUE);
			point.initPlayerInfo(player.getData());
			WorldPointService.getInstance().createWorldPoint(point);
		}
		
		// 跨服后保护时间
		long crossProtect = CrossConstCfg.getInstance().getCrossProtect();
		// 跨服后保护结束时间
		long crossProtectEndTime = HawkTime.getMillisecond() + crossProtect;
		// 玩家原本保护罩结束时间
		StatusDataEntity entity = player.getData().getStatusById(Const.EffType.CITY_SHIELD_VALUE);
		long playerProtectEndTime = entity.getEndTime();
		
		long endTime = Math.max(playerProtectEndTime, crossProtectEndTime);
		point.setProtectedEndTime(endTime);
		
		try {
			StatusDataEntity addStatusBuff = player.getData().addStatusBuff(Const.EffType.CITY_SHIELD_VALUE, endTime);
			if (addStatusBuff != null) {
				player.getPush().syncPlayerStatusInfo(false, addStatusBuff);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}

		// 恢复城墙状态
		CityManager.getInstance().cityRecover(player);
		// 记录迁城时间
		player.getData().getStatisticsEntity().addCityMoveRecord(0, HawkTime.getMillisecond());
		// 记录玩家位置
		player.getData().updatePlayerPos(point.getX(), point.getY());
		// 回复协议
		WorldMoveCityResp.Builder builder = WorldMoveCityResp.newBuilder();
		builder.setResult(true);
		builder.setX(point.getX());
		builder.setY(point.getY());
		player.sendProtocol(HawkProtocol.valueOf(HP.code.WORLD_MOVE_CITY_S_VALUE, builder));
		
		try {
			WorldPointService.getInstance().removeShowDress(player.getId());
			WorldPointService.getInstance().removePlayerSignature(player.getId());
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 二级地图
	 *
	 * @param session
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.OPEN_SECONDARY_MAP_C_VALUE)
	private boolean onSecondaryMap(HawkProtocol protocol) {
		WorldSecondaryMapReq request = protocol.parseProtocol(WorldSecondaryMapReq.getDefaultInstance());
		String serverName = request.getServerName();

		WorldSecondaryMapInfo.Builder builder = WorldSecondaryMapInfo.newBuilder();
		if (GlobalData.getInstance().isLocalServer(serverName)) {
			String guildId = player.getGuildId();
			if (guildId != null) {
				Map<String, Integer> allianceMap = GuildService.getInstance().getGuildMemberAndPos(guildId);
				for (Entry<String, Integer> alliance : allianceMap.entrySet()) {
					WorldSecondaryAllianceInfo.Builder allianceBuilder = WorldSecondaryAllianceInfo.newBuilder();
					int pos[] = WorldPlayerService.getInstance().getPlayerPosXY(alliance.getKey());
					allianceBuilder.setGuildPositon(GuildPositon.valueOf(alliance.getValue()));
					allianceBuilder.setPointX(pos[0]);
					allianceBuilder.setPointY(pos[1]);
					builder.addAllianceInfo(allianceBuilder);
				}
			}
		}

		int superWeaponStatus = SuperWeaponService.getInstance().getStatus();
		if (superWeaponStatus == SuperWeaponPeriod.SIGNUP_VALUE
				|| superWeaponStatus == SuperWeaponPeriod.WARFARE_VALUE
				|| superWeaponStatus == SuperWeaponPeriod.CONTROL_VALUE) {
			Collection<IWeapon> weapons = SuperWeaponService.getInstance().getAllWeapon().values();
			for (IWeapon weapon : weapons) {
				boolean hasSignUp = false;
				String occupyTag = null;
				boolean hasAutoSignUp = false;
				
				if (!HawkOSOperator.isEmptyString(player.getGuildId()) && weapon.checkSignUp(player.getGuildId())) {
					hasSignUp = true;
				}
				if (!HawkOSOperator.isEmptyString(player.getGuildId()) && weapon.checkAutoSignUp(player.getGuildId())) {
					hasAutoSignUp = true;
				}
				if (!HawkOSOperator.isEmptyString(weapon.getGuildId())) {
					occupyTag = GuildService.getInstance().getGuildTag(weapon.getGuildId());
				}
				
				
				if (!hasSignUp && HawkOSOperator.isEmptyString(occupyTag)) {
					continue;
				}
				
				int[] pos = GameUtil.splitXAndY(weapon.getPointId());
				SuperBarrackMapInfo.Builder barrackInfo = SuperBarrackMapInfo.newBuilder();
				barrackInfo.setPointX(pos[0]);
				barrackInfo.setPointY(pos[1]);
				if (hasSignUp) {
					barrackInfo.setHasSignUp(hasSignUp);
				}
				barrackInfo.setHasAutoSignUp(hasAutoSignUp);
				if (!HawkOSOperator.isEmptyString(occupyTag)) {
					barrackInfo.setOccupyGuildTag(occupyTag);
				}
				builder.addSuperBarrack(barrackInfo);
			}
		}
		
		// 协议压缩发送
		player.sendProtocol((HawkProtocol.valueOf(HP.code.OPEN_SECONDARY_MAP_S, builder)));
		return true;
	}
	
	/**
	 * 进入世界地图
	 *
	 * @param session
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.PLAYER_ENTER_WORLD_VALUE)
	private boolean onPlayerEnterWorld(HawkProtocol protocol) {
		PlayerEnterWorld cmd = protocol.parseProtocol(PlayerEnterWorld.getDefaultInstance());

		String serverId = cmd.getServerId();
		if (HawkOSOperator.isEmptyString(serverId) || GlobalData.getInstance().isLocalServer(serverId)) {
			int aoiObjId = player.getAoiObjId();
			aoiObjId = WorldScene.getInstance().add(GsConst.WorldObjType.PLAYER, aoiObjId, cmd.getX(), cmd.getY(),
					0, 0, GameConstCfg.getInstance().getViewXRadius(), GameConstCfg.getInstance().getViewYRadius(), player);
			player.setAoiObjId(aoiObjId);

			inviewMarchs.clear();
			MarchSet marchSet = WorldMarchService.getInstance().onPlayerEnter(player, cmd.getX(), cmd.getY());
			inviewMarchs.addAll(marchSet);
			
			long cityShieldTime = player.getData().getCityShieldTime();
			if (cityShieldTime > HawkTime.getMillisecond()) {
				WorldPlayerService.getInstance().updateWorldPointProtected(player.getId(), cityShieldTime);
			}
			
		} else {
			// 跨服请求
			CrossProxy.getInstance().sendNotify(protocol, serverId, player.getId(), null);
		}

		player.responseSuccess(protocol.getType());
		return true;
	}

	/**
	 * 玩家在世界地图滑动视野(移动)
	 *
	 * @param session
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.PLAYER_WORLD_MOVE_VALUE)
	private boolean onPlayerWorldMove(HawkProtocol protocol) {
		worldMoveTask.setProtocol(protocol);
		worldMoveTask.setNeedSync(true);
		
		worldMoveTick();
		return true;
	}

	private void playerWorldMove(HawkProtocol protocol) {
		PlayerWorldMove cmd = protocol.parseProtocol(PlayerWorldMove.getDefaultInstance());
		String serverId = cmd.getServerId();
		if (HawkOSOperator.isEmptyString(serverId) || GlobalData.getInstance().isLocalServer(serverId)) {
			float moveSpeed = 0.0f;
			if (cmd.hasSpeed()) {
				moveSpeed = Math.max(0.0f, cmd.getSpeed());
				moveSpeed = Math.min(1.0f, cmd.getSpeed());
			}

			// fillpoint会做速度处理，此处忽略速度判断
			WorldScene.getInstance().move(player.getAoiObjId(), cmd.getX(), cmd.getY(), moveSpeed);

			// 行军同步
			if (HawkOSOperator.isZero(GameConstCfg.getInstance().getMoveSyncFactor()) || moveSpeed <= GameConstCfg.getInstance().getMoveSyncFactor() - 1.0f) {
				// 通知地图移动, 同步行军
				MarchSet currentSet = WorldMarchService.getInstance().onPlayerMove(player, inviewMarchs, cmd.getX(), cmd.getY());
				inviewMarchs.clear();
				inviewMarchs.addAll(currentSet);
			}
		} else {
			// 跨服请求
			CrossProxy.getInstance().sendNotify(protocol, serverId, player.getId(), null);
		}
	}

	/**
	 * 离开世界地图
	 *
	 * @param session
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.PLAYER_LEAVE_WORLD_VALUE)
	private boolean onPlayerLeaveWorld(HawkProtocol protocol) {
		protocol.parseProtocol(PlayerLeaveWorld.getDefaultInstance());
			
		WorldScene.getInstance().leave(player.getAoiObjId());
		
		WorldMarchService.getInstance().onPlayerLeave(player);
		
		inviewMarchs.clear();
		
		return true;
	}

	/**
	 * 获取视野内的世界点
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.FETCH_INVIEW_WORLD_POINT_VALUE)
	private boolean onFetchInviewWorldPoint(HawkProtocol protocol) {
		/*FetchInviewWorldPoint req = protocol.parseProtocol(FetchInviewWorldPoint.getDefaultInstance());

		float moveSpeed = 0.0f;
		if (req.hasSpeed()) {
			moveSpeed = Math.max(0.0f, req.getSpeed());
			moveSpeed = Math.min(1.0f, req.getSpeed());
		}
		// 构建回复的协议数据
		WorldPointSync.Builder builder = WorldPointSync.newBuilder();
		List<WorldPointPB.Builder> builderList = WorldPointService.getInstance().getWorldPointsInView(player.getId(),
				req.getX(), req.getY(), moveSpeed);
		for (WorldPointPB.Builder pointBuilder : builderList) {
			builder.addPoints(pointBuilder);
		}
		// 压缩协议并发送
		HawkProtocol rspProto = HawkProtocol.valueOf(HP.code.WORLD_POINT_SYNC_VALUE, builder);
		player.sendProtocol(rspProto);
		return true;*/
		
		throw new UnsupportedOperationException("this method does not use this time ");
	}

	private WorldSearchResp.Builder worldSearch(HawkProtocol protocol) {
		WorldSearchReq req = protocol.parseProtocol(WorldSearchReq.getDefaultInstance());
		SearchType type = req.getType();
		// 第n次查找
		int index = req.getIndex();
		
		int[] pos = WorldPlayerService.getInstance().getPlayerPosXY(player.getId());
		WorldPoint worldPoint = null;
		switch (type.getNumber()) {
		case SearchType.SEARCH_RESOURCE_VALUE:
			worldPoint = getTargetPoint(pos, WorldPointType.RESOURCE_VALUE, req.getId(), index, type.getNumber());
			break;

		case SearchType.SEARCH_MONSTER_VALUE:
			worldPoint = getTargetPoint(pos, WorldPointType.MONSTER_VALUE, req.getLevel(), index, type.getNumber());
			break;

		case SearchType.SEARCH_BOX_VALUE:
			worldPoint = getTargetPoint(pos, WorldPointType.BOX_VALUE, req.getId(), index, type.getNumber());
			break;
		
		case SearchType.SEARCH_YURI_FACTORY_VALUE:
			worldPoint = getTargetPoint(pos, WorldPointType.YURI_FACTORY_VALUE, req.getId(), index, type.getNumber());
			break;
		
		case SearchType.SEARCH_STRONGPOINT_VALUE:
			worldPoint = getTargetPoint(pos, WorldPointType.STRONG_POINT_VALUE, req.getId(), index, type.getNumber());
			break;
			
		case SearchType.SEARCH_FOGGY_VALUE:
			worldPoint = getTargetPoint(pos, WorldPointType.FOGGY_FORTRESS_VALUE, req.getLevel(), index, type.getNumber());
			break;
		case SearchType.SEARCH_NEW_MONSTER_VALUE:
			worldPoint = getTargetPoint(pos, WorldPointType.MONSTER_VALUE, req.getLevel(), index, type.getNumber());
			break;
		case SearchType.SEARCH_YURI_MONSTER_VALUE:
			worldPoint = getTargetPoint(pos, WorldPointType.MONSTER_VALUE, req.getLevel(), index, type.getNumber());
			break;
		case SearchType.SEARCH_SNOWBALL_VALUE:
			worldPoint = WorldSnowballService.getInstance().searchSnowball(pos, index);
			break;
		case SearchType.SEARCH_EMPTY_POINT_VALUE:
			WorldMapConstProperty worldMapConstProperty = WorldMapConstProperty.getInstance();
			int minDis = worldMapConstProperty.getMinStoryMonsterSerachRadius();
			int maxDis = worldMapConstProperty.getMaxStoryMonsterSearchRadius();
			worldPoint = searchFreePoint(pos, minDis, maxDis);
			break;
		case SearchType.SEARCH_GUNDAM_VALUE:
			worldPoint = searchGundamPoint(pos, index);
			break;
		case SearchType.SEARCH_NIAN_VALUE:
			worldPoint = searchNianPoint(pos, index);
			break;
		case SearchType.SEARCH_PYLON_VALUE:
			worldPoint = getTargetPoint(pos, WorldPointType.PYLON_VALUE, req.getId(), index, type.getNumber());
			break;
		case SearchType.SEARCH_CHRISTMAS_BOSS_VALUE:
			worldPoint = searchChristmasBoss(pos, index);
			break;
		case SearchType.SEARCH_CAKE_VALUE:
			worldPoint = searchCakePoint();
			break;
		case SearchType.SEARCH_NEW_ACT_MONSTER_VALUE:
			worldPoint = getTargetPoint(pos, WorldPointType.MONSTER_VALUE, req.getLevel(), index, type.getNumber());
			break;
		default:
			sendError(protocol.getType(), Status.SysError.PARAMS_INVALID_VALUE);
			return null;
		}

		logger.info("world search, type:{}, reqId:{}, manorType:{}, worldPoint:{}", type, req.getId(), req.getManorType(), worldPoint);

		// 没有找到合适的点
		if (worldPoint == null) {
			if (type == SearchType.SEARCH_GUILD_MANOR) {
				sendError(protocol.getType(), Status.Error.WORLD_SEARCH_NO_MANOR);
			} else if (type == SearchType.SEARCH_SNOWBALL) {
				sendError(protocol.getType(), Status.Error.SEARCH_SNOWBALL_NULL);
			} else {
				sendError(protocol.getType(), Status.Error.WORLD_SEARCH_NO_TARGET);
			}
			return null;
		}

		// 回复
		WorldSearchResp.Builder resp = WorldSearchResp.newBuilder();
		resp.setTargetX(worldPoint.getX());
		resp.setTargetY(worldPoint.getY());
		resp.setType(type);
		resp.setSuccess(true);
		return resp;
	}
	
	/**
	 * 搜索圣诞boss
	 * @param pos
	 * @param index
	 * @return
	 */
	private WorldPoint searchChristmasBoss(int[] pos, int index) {
		// 中心点
		AlgorithmPoint centerPoint = new AlgorithmPoint(pos[0], pos[1]);

		// 查找到的点集合
		TreeSet<WorldPoint> searchPoints = new TreeSet<>(new Comparator<WorldPoint>() {
			@Override
			public int compare(WorldPoint o1, WorldPoint o2) {
				double distance1 = centerPoint.distanceTo(new AlgorithmPoint(o1.getX(), o1.getY()));
				double distance2 = centerPoint.distanceTo(new AlgorithmPoint(o2.getX(), o2.getY()));
				return distance1 >= distance2 ? 1 : -1;
			}
		});
		
		Collection<WorldPoint> nians = WorldChristmasWarService.getInstance().getBoss().values();
		for (WorldPoint nian : nians) {
			searchPoints.add(nian);
		}
		
		if (searchPoints.isEmpty()) {
			return null;
		}
		
		List<WorldPoint> searchPointList = new ArrayList<WorldPoint>(searchPoints.size());
		searchPointList.addAll(searchPoints);
		
		return searchPointList.get(index % searchPointList.size());
	}

	/**
	 * 搜索高达点
	 */
	public WorldPoint searchGundamPoint(int[] pos, int index) {
		// 中心点
		AlgorithmPoint centerPoint = new AlgorithmPoint(pos[0], pos[1]);

		// 查找到的点集合
		TreeSet<WorldPoint> searchPoints = new TreeSet<>(new Comparator<WorldPoint>() {
			@Override
			public int compare(WorldPoint o1, WorldPoint o2) {
				double distance1 = centerPoint.distanceTo(new AlgorithmPoint(o1.getX(), o1.getY()));
				double distance2 = centerPoint.distanceTo(new AlgorithmPoint(o2.getX(), o2.getY()));
				return distance1 >= distance2 ? 1 : -1;
			}
		});
		
		Collection<WorldPoint> gundams = WorldGundamService.getInstance().getGundams();
		for (WorldPoint gundam : gundams) {
			searchPoints.add(gundam);
		}
		
		if (searchPoints.isEmpty()) {
			return null;
		}
		
		List<WorldPoint> searchPointList = new ArrayList<WorldPoint>(searchPoints.size());
		searchPointList.addAll(searchPoints);
		return searchPointList.get(index % searchPointList.size());
	}
	
	/**
	 * 搜索年兽点
	 */
	public WorldPoint searchNianPoint(int[] pos, int index) {
		// 中心点
		AlgorithmPoint centerPoint = new AlgorithmPoint(pos[0], pos[1]);

		// 查找到的点集合
		TreeSet<WorldPoint> searchPoints = new TreeSet<>(new Comparator<WorldPoint>() {
			@Override
			public int compare(WorldPoint o1, WorldPoint o2) {
				double distance1 = centerPoint.distanceTo(new AlgorithmPoint(o1.getX(), o1.getY()));
				double distance2 = centerPoint.distanceTo(new AlgorithmPoint(o2.getX(), o2.getY()));
				return distance1 >= distance2 ? 1 : -1;
			}
		});
		
		Collection<WorldPoint> nians = WorldNianService.getInstance().getNians();
		for (WorldPoint nian : nians) {
			searchPoints.add(nian);
		}
		
		if (searchPoints.isEmpty()) {
			return null;
		}
		
		List<WorldPoint> searchPointList = new ArrayList<WorldPoint>(searchPoints.size());
		searchPointList.addAll(searchPoints);
		return searchPointList.get(index % searchPointList.size());
	}


	/**
	 * 搜索空闲点并生成野怪
	 */
	private boolean searchFreePointForCreateMonster(HawkProtocol protocol) {
		// 暂时的特殊处理,剧情任务第九章领取完奖励就不走这个逻辑了
		StoryMissionEntity storyMission = player.getData().getStoryMissionEntity();
		if (storyMission != null && storyMission.getChapterId() >= 10) {
			return false;
		}
		
		WorldSearchReq req = protocol.parseProtocol(WorldSearchReq.getDefaultInstance());
		SearchType searchType = req.getType();
		int level = req.getLevel();
		int index = req.getIndex();
		if(searchType != SearchType.SEARCH_MONSTER && searchType != SearchType.SEARCH_YURI_MONSTER &&
				searchType != SearchType.SEARCH_NEW_ACT_MONSTER){
			return false;
		}
		int limitLevel = ConstProperty.getInstance().getWorldEnemyLevel();
		if (level > limitLevel){
			return false;
		}
		WorldEnemyCfg worldEnemyCfg = getWorldEnemyCfg(searchType, level);
		if(worldEnemyCfg == null){
			return false;
		}
		//玩家坐标
		int[] pos = WorldPlayerService.getInstance().getPlayerPosXY(player.getId());

		String disCfgStr = ConstProperty.getInstance().getWorldEnemyDistance();
		String[] disCfg = disCfgStr.split("_");
		int minDis = Integer.valueOf(disCfg[0]);  //2距离范围
		int maxDis = Integer.valueOf(disCfg[1]);  //10
		//小范围搜不到点,才生成
		WorldPoint worldPoint = getTargetPoint(pos, WorldPointType.MONSTER_VALUE, level, index, searchType.getNumber(), maxDis);
		if (worldPoint != null){
			return false;
		}
		//第一次搜怪走创建逻辑
		if (index != 0){
			return false;
		}
		// 投递世界线程执行
		WorldThreadScheduler.getInstance().postWorldTask(new WorldTask(GsConst.WorldTaskType.WORLD_MONSTER_POINT_GENERATE) {
			 @Override
			 public boolean onInvoke() {
				 //最优空白点
				 int freePoint = searchFreeBestPointPos(pos, minDis, maxDis);
				 if (freePoint != 0) {
					 int[] freePos = GameUtil.splitXAndY(freePoint);
					 int monsterPosX = freePos[0];
					 int monsterPosY = freePos[1];
					 // 中心点所在区域
					 AreaObject areaObj = WorldPointService.getInstance().getArea(monsterPosX, monsterPosY);
					 // 中心点所在资源带
					 int zoneId = WorldUtil.getPointResourceZone(monsterPosX, monsterPosY);
					 WorldPoint worldPoint = new WorldPoint(monsterPosX, monsterPosY, areaObj.getId(), zoneId, WorldPointType.MONSTER_VALUE);
					 worldPoint.setMonsterId(worldEnemyCfg.getId());
					 WorldPointService.getInstance().addPoint(worldPoint);
					 //
					 if(searchType == SearchType.SEARCH_MONSTER){
						 areaObj.addCommonMonster(worldEnemyCfg.getId(), worldPoint.getId());
					 }
					 else if(searchType == SearchType.SEARCH_YURI_MONSTER){
						 areaObj.addActivityMonster(worldEnemyCfg.getId(), worldPoint.getId());
					 }
					 logger.info("searchFreePointForCreateMonster success playerId:{}, monsterId:{}", player.getId(), worldEnemyCfg.getId());
					 //返回玩家消息
					 WorldSearchResp.Builder resp = WorldSearchResp.newBuilder();
					 resp.setTargetX(worldPoint.getX());
					 resp.setTargetY(worldPoint.getY());
					 resp.setSuccess(true);
					 player.sendProtocol(HawkProtocol.valueOf(HP.code.WORLD_SEARCH_S_VALUE, resp));
					 return true;
				 } else {
					 logger.info("searchFreePointForCreateMonster failed playerId:{}", player.getId());
					 sendError(protocol.getType(), Status.Error.MONSTER_SEARCH_FAIL);
					 return true;
				 }
			 }
		 });
		return true;
	}
	/**
	 * 查找最近空白点
	 *
	 * @param pos
	 * @param minDis
	 * @param maxDis
	 * @return
	 */
	public List<Point> searchFreeNearPointPos(int[] pos, int minDis, int maxDis) {
		WorldPointService worldPointService = WorldPointService.getInstance();
		//内圈空点
		List<Point> miniPointList = worldPointService.getRhoAroundPointsFree(pos[0], pos[1], minDis);
		//外圈空点
		List<Point> allPointList = worldPointService.getRhoAroundPointsFree(pos[0], pos[1], maxDis);
		//内外中间的空点
		allPointList.removeAll(miniPointList);
		allPointList.sort(new Comparator<Point>() {
			@Override
			public int compare(Point p1, Point p2) {
				double distance1 = WorldUtil.distance(pos[0], pos[1], p1.getX(), p1.getY());
				double distance2 = WorldUtil.distance(pos[0], pos[1], p2.getX(), p2.getY());
				return Double.compare(distance1,distance2);
			}
		});
		return allPointList;
	}
	/**
	 * 查找最优空白点
	 * @param pos
	 * @param minDis
	 * @param maxDis
	 * @return
	 */
	private int searchFreeBestPointPos(int[] pos, int minDis, int maxDis){
		WorldPointService worldPointService = WorldPointService.getInstance();
		//内圈空点
		List<Point> miniPointList = worldPointService.getRhoAroundPointsFree(pos[0], pos[1], minDis);
		//外圈空点
		List<Point> allPointList = worldPointService.getRhoAroundPointsFree(pos[0], pos[1], maxDis);
		//内外中间的空点
		allPointList.removeAll(miniPointList);
		//乱序
		Collections.shuffle(allPointList);
		//找到点返回
		for (Point point:allPointList) {
			if ((point.getX() + point.getY()) % 2 == 0){
				return point.getId();
			}
		}
		return 0;
	}
	/**
	 * 搜索空闲点
	 * @param pos
	 * @param minDis
	 * @param maxDis
	 * @author Codej
	 */
	private WorldPoint searchFreePoint(int[] pos, int minDis, int maxDis) {		
		WorldPointService worldPointService = WorldPointService.getInstance();
		List<Point> pointList = worldPointService.getRhoAroundPointsFree(pos[0], pos[1], minDis);
		
		//默认是降序
		boolean up = false;
		
		//如果最小的距离找不到
		if (pointList.isEmpty()) {
			pointList = worldPointService.getRhoAroundPointsFree(pos[0], pos[1], maxDis);
			up = true;
		}
		
		if (pointList.isEmpty()) {
			return null;
		}
		
		List<DistancePoint> distancePointList = new ArrayList<>(pointList.size());
		pointList.stream().forEach(point->{
			distancePointList.add(new DistancePoint(point, pos));
		});
		
		//默认降序
		Collections.sort(distancePointList);
		DistancePoint distancePoint = null;
		boolean found = false;
		if (up) {
			for (int i = distancePointList.size() - 1; i >= 0; i--) {
				distancePoint = distancePointList.get(i);
				if ((distancePoint.getPoint().getX() + distancePoint.getPoint().getY()) % 2 == 0) {
					found = true;
					break;
				} 
			}
			
		}  else {
			for (int i = 0; i < distancePointList.size(); i++) {
				distancePoint = distancePointList.get(i);
				if ((distancePoint.getPoint().getX() + distancePoint.getPoint().getY()) % 2 == 0) {
					found = true;
					break;
				} 
			} 
		}
		
		if (found) {
			WorldPoint wp = new WorldPoint();
			wp.setX(distancePoint.getPoint().getX());
			wp.setY(distancePoint.getPoint().getY());			
			return wp;
		} else {
			return null;
		}			
	}

	/**
	 * 任务前往世界的搜索
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.WORLD_TASK_GO_SEARCH_C_VALUE)
	private boolean onTaskGo(HawkProtocol protocol) {
		WorldSearchResp.Builder builder = worldSearch(protocol);
		if (builder != null) {
			player.sendProtocol(HawkProtocol.valueOf(HP.code.WORLD_TASK_GO_SEARCH_S, worldSearch(protocol)));
		}
		return true;
	}
	
	/**
	 * 开启自动打野行军
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.SWITCH_ATK_MONSTER_AUTO_MARCH_C_VALUE)
	private boolean onSwitchAtkMonsterAutoMarch(HawkProtocol protocol) {
		SwitchAtkMonsterAutoMarchReq req = protocol.parseProtocol(SwitchAtkMonsterAutoMarchReq.getDefaultInstance());
		// 关闭自动打野
		if (req.getIsOpen() == 0) {
			WorldMarchService.getInstance().breakAutoMarch(player, 0);
			LogUtil.logAutoMonsterSwitch(player, false);
			HawkLog.logPrintln("AtkMonsterAutoMarch close, playerId: {}", player.getId());
			return true;
		}

		PlayerAutoModule atuoModel = player.getModule(AUTO_GATHER);
		if( atuoModel.isAutoPut() ){
			WorldMarchService.getInstance().breakAutoMarch(player, 0);
			LogUtil.logAutoMonsterSwitch(player, false);
			HawkLog.logPrintln("AtkMonsterAutoMarch close, auto put is on, playerId: {}", player.getId());
			sendError(protocol.getType(), AutoGatherErr.AUTO_GATHER_RESOURCE_VALUE);
			return true;
		}

		StatusDataEntity statusEntity = player.getData().getStatusById(EffType.AUTO_ATK_MONSTER_VALUE);
		long timeNow = HawkTime.getMillisecond();
		if (statusEntity == null || statusEntity.getEndTime() < timeNow) {
			sendError(protocol.getType(), Status.Error.AUTO_ATK_MONSTER_NOT_FUNCTION_VALUE); 
			return false;
		}
		
		int autoMarchCount = 1, vipLevel = player.getVipLevel();
		VipCfg vipCfg = HawkConfigManager.getInstance().getConfigByKey(VipCfg.class, vipLevel);
		if (vipCfg != null) {
			autoMarchCount += vipCfg.getAutoFightQueue();
			autoMarchCount = Math.min(autoMarchCount, player.getMaxMarchNum());
		} else {
			HawkLog.logPrintln("OpenAtkMonsterAutoMarch exception, vipCfg error, playerId: {}, vipLevel: {}", player.getId(), vipLevel);
		}
		
		// 判断是否允许开启自动打野
		if (autoMarchCount <= 0) {
			sendError(protocol.getType(), Status.Error.AUTO_ATK_MONSTER_NOT_FUNCTION_VALUE); 
			return false;
		}
		
		List<AutoMarchPB> autoMarchPBList = req.getMarchInfoList();
		if (autoMarchPBList.isEmpty() || autoMarchPBList.size() > autoMarchCount) {
			sendError(protocol.getType(), Status.SysError.PARAMS_INVALID_VALUE);
			HawkLog.errPrintln("OpenAtkMonsterAutoMarch failed, marchCount error, playerId: {}, req count: {}, server count: {}", player.getId(), autoMarchPBList.size(), autoMarchCount);
			return false;
		}
		
		int minLevel = req.getMinLevel();
		int maxLevel = req.getMaxLevel();
		List<SearchType> searchTypeList = req.getSearchTypeList();
		
		int maxLevelKilled = player.getData().getMonsterEntity().getMaxLevel();
		if (minLevel <= 0 || minLevel > maxLevel || maxLevel > maxLevelKilled + 1 || searchTypeList.isEmpty()) {
			sendError(protocol.getType(), Status.SysError.PARAMS_INVALID_VALUE);
			HawkLog.errPrintln("OpenAtkMonsterAutoMarch failed, playerId: {}, minLevel: {}, maxLevel: {}, maxLevelKilled: {}, searchType empty: {}", player.getId(), minLevel, maxLevel, maxLevelKilled, searchTypeList.isEmpty());
			return false;
		}
		
		for (SearchType type : searchTypeList) {
			if (!GsConst.SEARCH_MONSTER_AUTO_ORDER.contains(type)) {
				sendError(protocol.getType(), Status.SysError.PARAMS_INVALID_VALUE);
				HawkLog.errPrintln("OpenAtkMonsterAutoMarch failed, playerId: {}, searchType: {}", player.getId(), type);
				return false;
			}
		}
		
		AutoMonsterMarchParam autoMarchParam = WorldMarchService.getInstance().getAutoMarchParam(player.getId());
		List<AutoMarchInfo> autoMarchSetInfoList = new ArrayList<AutoMarchInfo>();
		List<IWorldMarch> marchList = WorldMarchService.getInstance().getAutoMonsterMarch(player.getId());
		
		for (AutoMarchPB info : autoMarchPBList) {
			if (!autoMonterCheck(protocol, autoMarchParam, info, autoMarchSetInfoList, marchList)) {
				return false;
			}
		}
		
		List<Integer> searchType = new ArrayList<Integer>();
		for (SearchType type : GsConst.SEARCH_MONSTER_AUTO_ORDER) {
			if (searchTypeList.contains(type)) {
				searchType.add(type.getNumber());
			}
		}
		
		String paramBefore = "";
		if (autoMarchParam == null) {
			autoMarchParam = new AutoMonsterMarchParam();
			WorldMarchService.getInstance().addAutoMarchParam(player.getId(), autoMarchParam);
		} else {
			paramBefore = autoMarchParam.toString();
		}
		
		autoMarchParam.setMaxLevel(maxLevel);
		autoMarchParam.setMinLevel(minLevel);
		autoMarchParam.setSearchType(searchType);
		autoMarchParam.addAutoMarchInfo(autoMarchSetInfoList);

		// 同步状态信息
		WorldMarchService.getInstance().pushAutoMarchStatus(player, 1);
		
		for (int i = 0; i < autoMarchPBList.size(); i++) {
			try {
				searchMonsterAuto();
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		
		StringJoiner sj = new StringJoiner(",");
		for (AutoMarchInfo info : autoMarchSetInfoList) {
			sj.add(String.valueOf(info.getTroops()));
		}
		// 打点日志
		LogUtil.logAutoMonsterSwitch(player, true, minLevel, maxLevel, autoMarchPBList.size(), sj.toString());
		
		HawkLog.logPrintln("OpenAtkMonsterAutoMarch success, playerId: {}, paramBefore: {}, paramsAfter: {}", player.getId(), paramBefore, autoMarchParam);
		
		return true;
	}
	
	/**
	 * 自动打野条件检测
	 * 
	 * @param protocol
	 * @param autoMarchParam    原有的自动打野行军参数
	 * @param autoMarchPB  客户端传的自动打野行军参数
	 * @param autoMarchSetInfos 已校验通过的自动打野行军队列信息
	 * @param marchList    已出征的自动打野行军队列
	 * @return
	 */
	private boolean autoMonterCheck(HawkProtocol protocol, AutoMonsterMarchParam autoMarchParam, AutoMarchPB autoMarchPB, 
			List<AutoMarchInfo> autoMarchSetInfos, List<IWorldMarch> marchList) {
		List<ArmySoldierPB> armyList = autoMarchPB.getArmyList();
		List<Integer> heroIds = autoMarchPB.getHeroIdsList();
		int superSoldierId = autoMarchPB.getSuperSoldierId();
		ArmourSuitType armourSuit = autoMarchPB.getArmourSuit();
		
		EffectParams effParams = new EffectParams();
		effParams.setHeroIds(heroIds);
		effParams.setArmourSuit(armourSuit);
		effParams.setMechacoreSuit(autoMarchPB.getMechacoreSuit());
		effParams.setSuperSoliderId(superSoldierId);
		TalentType talentType = autoMarchPB.getTalentType();
		effParams.setTalent(talentType != null ? talentType.getNumber() : TalentType.TALENT_TYPE_DEFAULT_VALUE);
		effParams.setSuperLab(autoMarchPB.getSuperLab());

		if (autoMarchPB.hasSuperLab() && autoMarchPB.getSuperLab() != 0 && !player.isSuperLabActive(autoMarchPB.getSuperLab())) {
			sendError(protocol.getType(), Status.SysError.PARAMS_INVALID_VALUE);
			HawkLog.errPrintln("OpenAtkMonsterAutoMarch failed, super lab not active, playerId: {}, superLab: {}", player.getId(), autoMarchPB.getSuperLab());
			return false;
		}
		if (autoMarchPB.getManhattan().getManhattanAtkSwId() > 0 || autoMarchPB.getManhattan().getManhattanDefSwId() > 0) {
			// 检测超武信息,判断是否解锁，
			PlayerManhattanModule manhattanModule = player.getModule(GsConst.ModuleType.MANHATTAN);
			if (!manhattanModule.checkMarchReq(autoMarchPB.getManhattan())) {
				sendError(protocol.getType(), Status.SysError.PARAMS_INVALID_VALUE);
				HawkLog.errPrintln("OpenAtkMonsterAutoMarch failed, manhattan not active, playerId: {}, swAtkId: {}, swDeId: {}", player.getId(), autoMarchPB.getManhattan().getManhattanAtkSwId(),autoMarchPB.getManhattan().getManhattanDefSwId());
				return false;
			}
		}
		// 还未开启自动打野时，要先判断体力是否够打一次野
		if (autoMarchParam == null) {
			int buff = player.getEffect().getEffVal(EffType.ATK_MONSTER_VIT_ADD, effParams);
			int needVitMin = (int)(5 * (1 + buff * GsConst.EFF_PER));
			//体力减少
			int buffReduce =  player.getEffect().getEffVal(EffType.BACK_PRIVILEGE_ATK_MONSTER_VIT_REDUCE);
			needVitMin = (int) (needVitMin * (1 - buffReduce * GsConst.EFF_PER));
			needVitMin = Math.max(needVitMin, 1);
			
			if (player.getVit() < needVitMin) {
				sendError(protocol.getType(), Status.Error.VIT_NOT_ENOUGH_VALUE);
				return false;
			}
		}
		
		// 所传兵种为空
		if (armyList.isEmpty()) {
			sendError(protocol.getType(), Status.SysError.PARAMS_INVALID_VALUE);
			HawkLog.errPrintln("OpenAtkMonsterAutoMarch failed, army empty, playerId: {}", player.getId());
			return false;
		}
		
		List<ArmyInfo> armyInfoList = new ArrayList<ArmyInfo>();
		for (ArmySoldierPB armySoldier : armyList) {
			// 不存在的兵种（未训练过的兵种）
			ArmyEntity entity = player.getData().getArmyEntity(armySoldier.getArmyId());
			if (entity == null || armySoldier.getCount() <= 0) {
				sendError(protocol.getType(), Status.SysError.PARAMS_INVALID_VALUE);
				HawkLog.errPrintln("OpenAtkMonsterAutoMarch failed, playerId: {}, armyId: {}, count: {}", player.getId(), armySoldier.getArmyId(), armySoldier.getCount());
				return false;
			}
			
			ArmyInfo armyInfo = new ArmyInfo(armySoldier.getArmyId(), armySoldier.getCount());
			armyInfoList.add(armyInfo);
		}
		
		for (int heroId : heroIds) {
			Optional<PlayerHero> heroOP = player.getHeroByCfgId(heroId);
			// 不存在的英雄
			if (!heroOP.isPresent()) {
				sendError(protocol.getType(), Status.SysError.PARAMS_INVALID_VALUE);
				HawkLog.errPrintln("OpenAtkMonsterAutoMarch failed, hero not exist, playerId: {}, heroId: {}", player.getId(), heroId);
				return false;
			}
			
			for (AutoMarchInfo info : autoMarchSetInfos) {
				List<Integer> heroList = info.getHeroIds();
				if (heroList != null && heroList.contains(heroId)) {
					sendError(protocol.getType(), Status.SysError.PARAMS_INVALID_VALUE);
					HawkLog.errPrintln("OpenAtkMonsterAutoMarch failed, hero repeated, playerId: {}, heroId: {}, march1: {}, march2: {}", player.getId(), heroId, info.getPriority(), autoMarchPB.getPriority());
					return false;
				}
			}
		}
		
		if (superSoldierId > 0) {
			Optional<SuperSoldier> sso = player.getSuperSoldierByCfgId(superSoldierId);
			// 不存在的机甲
			if(!sso.isPresent()) {
				sendError(protocol.getType(), Status.SysError.PARAMS_INVALID_VALUE);
				HawkLog.errPrintln("OpenAtkMonsterAutoMarch failed, superSoldier not exist, playerId: {}, superSoldierId: {}", player.getId(), superSoldierId);
				return false;
			}
			
			for (AutoMarchInfo info : autoMarchSetInfos) {
				if (info.getSuperSoldierId() == superSoldierId) {
					sendError(protocol.getType(), Status.SysError.PARAMS_INVALID_VALUE);
					HawkLog.errPrintln("OpenAtkMonsterAutoMarch failed, superSoldier repeated, playerId: {}, superSoldierId: {}, march1: {}, march2: {}", player.getId(), superSoldierId, info.getPriority(), autoMarchPB.getPriority());
					return false;
				}
			}
		}
		
		MechaCoreSuitType mechacoreSuit = autoMarchPB.getMechacoreSuit();
		AutoMarchInfo autoMarchInfo = new AutoMarchInfo();
		autoMarchInfo.setArmy(armyInfoList);
		autoMarchInfo.setHeroIds(heroIds.subList(0, Math.min(heroIds.size(), 2)));
		autoMarchInfo.setSuperSoldierId(superSoldierId);
		autoMarchInfo.setPriority(autoMarchPB.getPriority());
		autoMarchInfo.setTroops(autoMarchPB.getTroops());
		autoMarchInfo.setArmourSuitType(armourSuit != null ? armourSuit.getNumber() : 0);
		autoMarchInfo.setMechacoreSuit(mechacoreSuit != null ? mechacoreSuit.getNumber() : MechaCoreSuitType.MECHA_ONE_VALUE);
		autoMarchInfo.setTalent(effParams.getTalent());
		autoMarchInfo.setSuperLab(autoMarchPB.getSuperLab());
		for (AutoMarchInfo info : autoMarchSetInfos) {
			if (info.getId() == autoMarchInfo.getId()) {
				sendError(protocol.getType(), Status.SysError.PARAMS_INVALID_VALUE);
				HawkLog.errPrintln("OpenAtkMonsterAutoMarch failed, auto march identify same, playerId: {}", player.getId());
				return false;
			}
		}
		
		autoMarchSetInfos.add(autoMarchInfo);
		for (IWorldMarch march : marchList) {
			if (march.getMarchEntity().getAutoMarchIdentify() == autoMarchInfo.getId()) {
				autoMarchInfo.setStatus(1);
				break;
			}
		}
		
		return true;
	}
	
	/**
	 * 自动搜索野怪
	 * 
	 * @param msg
	 * @return
	 */
	@MessageHandler
	private boolean onSearchMonsterAuto(AutoSearchMonsterMsg msg) {
		return searchMonsterAuto();
	}
	
	/**
	 * 自动打野
	 * 
	 * @return
	 */
	private boolean searchMonsterAuto() {
		AutoMonsterMarchParam autoMarchParam = WorldMarchService.getInstance().getAutoMarchParam(player.getId());
		if (autoMarchParam == null) {
			return false;
		}
		
		// 下线达到一定时长后关闭自动打野（防止断线重连的情况）
		if (!player.isActiveOnline() && autoMarchParam.isAutoMarchCDEnd()) {
			WorldMarchService.getInstance().closeAutoMarch(player.getId());
			return false;
		}
		
		// 所有的自动打野行军都已发出
		if (WorldMarchService.getInstance().getAutoMonsterMarch(player.getId()).size() >= autoMarchParam.getAutoMarchCount()) {
			HawkLog.logPrintln("AtkMonsterAutoMarch broken, no autoMarch left, playerId: {}", player.getId());
			return false;
		}
		
		// 作用号是否结束
		StatusDataEntity statusEntity = player.getData().getStatusById(EffType.AUTO_ATK_MONSTER_VALUE);
		if (statusEntity == null || statusEntity.getEndTime() < HawkTime.getMillisecond()) {
			HawkLog.logPrintln("AtkMonsterAutoMarch broken, monthCardBuff end, playerId: {}", player.getId());
			WorldMarchService.getInstance().breakAutoMarch(player, Status.Error.AUTO_ATK_MONSTER_BUFF_BREAK_VALUE);
			return false;
		}
			
		// 没有空闲队列了
		if (!WorldMarchService.getInstance().isHasFreeMarch(player)) {
			HawkLog.logPrintln("AtkMonsterAutoMarch broken, no free march, playerId: {}", player.getId());
			return false;
		}
		
		// 玩家主动召回所有行军的状态（迁城情况也算召回所有行军）
		if (autoMarchParam.isCityMoving()) {
			sendError(HP.code.SWITCH_ATK_MONSTER_AUTO_MARCH_C_VALUE, Status.Error.AUTO_ATK_MONSTER_MARCH_BACK);
			//结束自动打野  
			WorldMarchService.getInstance().breakAutoMarch(player, Status.Error.AUTO_ATK_MONSTER_MARCH_BACK_VALUE);
			HawkLog.logPrintln("AtkMonsterAutoMarch broken, city moving, playerId: {}", player.getId());
			return false;
		}
		
		int[] posInfo = WorldPlayerService.getInstance().getPlayerPosXY(player.getId());
		if (posInfo[0] <= 0 && posInfo[1] <= 0) {
			HawkTaskManager.getInstance().postMsg(player.getXid(), AutoSearchMonsterMsg.valueOf());
			HawkLog.logPrintln("AtkMonsterAutoMarch broken, city point creating, playerId: {}", player.getId());
			return false;
		}
		
		AutoMarchInfo autoMarchInfo = autoMarchParam.getAutoMarchByPriority();
		// 还是无队列可出征
		if (autoMarchInfo == null) {
			HawkLog.logPrintln("AtkMonsterAutoMarch broken, enable autoMarchInfo not found, playerId: {}", player.getId());
			return false;
		}
		
		int[] pos = WorldPlayerService.getInstance().getPlayerPosXY(player.getId());
		List<Integer> searchType = autoMarchParam.getSearchType();
		int level = autoMarchParam.getMaxLevel();
		int minLevel = autoMarchParam.getMinLevel();
		
		int status = Status.Error.AUTO_ATK_MONSTER_SEARCH_BREAK_VALUE;
		
		while (level >= minLevel) {
			int result = AutoSearchMonsterResultCode.KEEPTRYING;
			for (Integer type : searchType) {
				// index参数默认填0
				WorldPoint worldPoint = getTargetPoint(pos, WorldPointType.MONSTER_VALUE, level, 0, type);
				if (worldPoint != null) {
					result = checkAutoAtkMonster(worldPoint, autoMarchInfo);
					if (result != AutoSearchMonsterResultCode.KEEPTRYING) {
						break;
					}
				}
			}
			
			// 搜索到野怪并且成功发起行军，中断搜索
			if (result == AutoSearchMonsterResultCode.SUCCESS) {
				autoMarchInfo.setStatus(1);
				return true;
			}
			
			// 条件中断搜索
			if (result > AutoSearchMonsterResultCode.SUCCESS) {
				status = result;
				break;
			}
			
			// 降低野怪等级，继续搜索
			level--;
		}
		
		// 失败了，关闭打野功能
		autoMarchParam.removeAutoMarch(autoMarchInfo.getId());
		int remainCount = autoMarchParam.getAutoMarchCount();
		if (remainCount == 0 || Status.Error.AUTO_ATK_MONSTER_VIT_BREAK_VALUE == status) {
			WorldMarchService.getInstance().breakAutoMarch(player, status);
		}
		
		HawkLog.logPrintln("AtkMonsterAutoMarch broken, playerId: {}, level: {}, minLevel: {}, status: {}, removeId: {}, remainCount: {}", 
				player.getId(), level, minLevel, status, autoMarchInfo.getId(), remainCount);
		
		return true;
	}
	
	/**
	 * 发起自动打怪行军
	 * 
	 * @param targetPoint
	 * @param autoMonsterInfo
	 * 
	 * @return -1：继续搜索野怪，0：成功发起行军，1：中断搜索，关闭自动打野
	 * 
	 */
	private int checkAutoAtkMonster(WorldPoint targetPoint, AutoMarchInfo autoMonsterInfo) {
		// 专属怪, 不能被别人打
		if (!HawkOSOperator.isEmptyString(targetPoint.getOwnerId()) && !targetPoint.getOwnerId().equals(player.getId())) {
			HawkLog.errPrintln("world auto-attack monster failed, exclusive point, playerId: {}, x: {}, y: {}, ownerId: {}", 
					player.getId(), targetPoint.getX(), targetPoint.getY(), targetPoint.getOwnerId());
			return AutoSearchMonsterResultCode.KEEPTRYING;
		}

		// 野怪配置
		WorldEnemyCfg cfg = HawkConfigManager.getInstance().getConfigByKey(WorldEnemyCfg.class, targetPoint.getMonsterId());

		// 怪物配置错误
		if (cfg == null) {
			HawkLog.errPrintln("world auto-attack monster failed, cfg null, playerId: {}, monsterId: {}", player.getId(), targetPoint.getMonsterId());
			return AutoSearchMonsterResultCode.KEEPTRYING;
		}

		// 等级不足
		if (player.getLevel() < cfg.getLowerLimit()) {
			HawkLog.errPrintln("world auto-attack monster failed, playerId: {}, playerLevel: {}, lowerLimit: {}", player.getId(), player.getLevel(), cfg.getLowerLimit());
			return AutoSearchMonsterResultCode.KEEPTRYING;
		}

		int vitCost = cfg.getCostPhysicalPower();
		
		EffectParams effParams = new EffectParams();
		effParams.setHeroIds(autoMonsterInfo.getHeroIds());
		effParams.setArmourSuit(ArmourSuitType.valueOf(autoMonsterInfo.getArmourSuitType()));
		effParams.setMechacoreSuit(MechaCoreSuitType.valueOf(autoMonsterInfo.getMechacoreSuit()));
		effParams.setSuperSoliderId(autoMonsterInfo.getSuperSoldierId());
		effParams.setTalent(autoMonsterInfo.getTalent());
		effParams.setSuperLab(autoMonsterInfo.getSuperLab());
		
		// 自动打野编队参数带上
		int troop = autoMonsterInfo.getTroops();
		if (troop != 0) {
			WorldMarchReq.Builder marchReq = WorldMarchReq.newBuilder();
			marchReq.setFormation(autoMonsterInfo.getTroops());
			marchReq.setPosX(0);
			marchReq.setPosY(0);
			effParams.setWorldmarchReq(marchReq.build());
		}
		
		// 超能实验室算双倍体力消耗
		int buff = player.getEffect().getEffVal(EffType.ATK_MONSTER_VIT_ADD, effParams);
		vitCost = (int)(vitCost * (1 + buff * GsConst.EFF_PER));
		//体力减少
		int buffReduce =  player.getEffect().getEffVal(EffType.BACK_PRIVILEGE_ATK_MONSTER_VIT_REDUCE);
		vitCost = (int) (vitCost * (1 - buffReduce * GsConst.EFF_PER));
		vitCost = Math.max(vitCost, 1);
		// 体力消耗
		ConsumeItems consumeItems = ConsumeItems.valueOf(PlayerAttr.VIT, vitCost);
		// 体力不足
		if (!consumeItems.checkConsume(player)) {
			HawkLog.errPrintln("world auto-attack monster failed, vit not enough, playerId: {}, vit: {}", player.getId(), player.getVit());
			return Status.Error.AUTO_ATK_MONSTER_VIT_BREAK_VALUE;
		}
		
		List<Integer> heroIds = new ArrayList<Integer>();
		// 英雄是否可出征
		if (ArmyService.getInstance().heroCanMarch(player, autoMonsterInfo.getHeroIds())) {
			heroIds.addAll(heroIds);
		}
		
		int superSoldierId = 0;
		// 机甲是否可出征
		if (ArmyService.getInstance().superSoldierCsnMarch(player, autoMonsterInfo.getSuperSoldierId())) {
			superSoldierId = autoMonsterInfo.getSuperSoldierId();
		}
		
		List<ArmyInfo> armyList = autoMonsterInfo.getArmy();
		// 兵不足
		List<ArmyInfo> armyInfoList = ArmyService.getInstance().checkArmyInfo(player, armyList, heroIds, superSoldierId); 
		if (armyInfoList.isEmpty()) {
			HawkLog.errPrintln("world auto-attack monster failed, no free army, playerId: {}", player.getId());
			return Status.Error.AUTO_ATK_MONSTER_ARMY_BREAK_VALUE;
		}

		// 开启行军,目标放怪物ID
		String targetId = String.valueOf(targetPoint.getMonsterId());
		
		effParams.setArmys(armyInfoList);
		IWorldMarch march = WorldMarchService.getInstance().startMarch(player, WorldMarchType.ATTACK_MONSTER_VALUE, targetPoint.getId(), targetId, null, 0, autoMonsterInfo.getId(),0,0, effParams);
		
		if (march == null) {
			HawkLog.errPrintln("world auto-attack monster failed, start march failed, playerId: {}", player.getId());
			return AutoSearchMonsterResultCode.KEEPTRYING;
		}
		
		march.getMarchEntity().setVitCost(vitCost);
		// 扣除体力
		consumeItems.consumeAndPush(player, Action.FIGHT_MONSTER);
		// 行为日志
		BehaviorLogger.log4Service(player, Source.WORLD_ACTION, Action.WORLD_START_FIGHT_MONSTER, 
				Params.valueOf("marchData", march),
				Params.valueOf("autoMarch", "true"));
		
		MissionManager.getInstance().postMsg(player, new EventGenOldMonsterMarch(cfg.getLevel()));
		
		return AutoSearchMonsterResultCode.SUCCESS;
	}
	
	/**
	 * 世界地图搜索功能
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.WORLD_SEARCH_C_VALUE)
	private boolean onWorldSearch(HawkProtocol protocol) {
		WorldSearchReq req = protocol.parseProtocol(WorldSearchReq.getDefaultInstance());
		//搜索野怪1 2 特殊处理,,小范围先搜,搜不到走生成的逻辑
		boolean result = searchFreePointForCreateMonster(protocol);
		if (result){
			return true;
		}
		logger.info("searchFreePointForCreateMonster result:{}, index:{}", result, req.getIndex());
		//搜怪
		WorldSearchResp.Builder builder = worldSearch(protocol);
		if (builder != null) {
			player.sendProtocol(HawkProtocol.valueOf(HP.code.WORLD_SEARCH_S_VALUE, builder));
		} else {
			WorldSearchResp.Builder resp = WorldSearchResp.newBuilder();
			resp.setTargetX(0);
			resp.setTargetY(0);
			resp.setSuccess(false);
			player.sendProtocol(HawkProtocol.valueOf(HP.code.WORLD_SEARCH_S_VALUE, resp));
		}
		return true;
	}

	/**
	 * 查询野怪配置
	 * @param searchType
	 * @param level
	 * @return
	 */
	public WorldEnemyCfg getWorldEnemyCfg(SearchType searchType, int level){
		if (searchType == SearchType.SEARCH_MONSTER){
			return AssembleDataManager.getInstance().getOldMonsterCfg(ModelType.MONSTER_1_VALUE, level);
		}else if(searchType == SearchType.SEARCH_YURI_MONSTER){
			return AssembleDataManager.getInstance().getNewMonsterCfg(ModelType.MONSTER_2_VALUE, level);
		}else if(searchType == SearchType.SEARCH_NEW_ACT_MONSTER){
			return AssembleDataManager.getInstance().getNewMonsterCfgAct185(ModelType.MONSTER_2_VALUE, level);
		}
		return null;
	}


	private WorldPoint getTargetPoint(int[] pos, int pointType, int id, int index, int searchType) {
		WorldPoint worldPoint = getTargetPoint(pos, pointType, id, index, searchType, 0);
		return worldPoint;
	}
	/**
	 * 找到目标点的内容
	 * 
	 * @param pos
	 *            目标位置
	 * @param pointType
	 *            点类型
	 * @param id
	 *            怪物ID或者资源ID
	 * @return
	 */
	private WorldPoint getTargetPoint(int[] pos, int pointType, int id, int index, int searchType, int minDistance) {
		index = index < 0 ? 0 : index;
		
		// 距离判断
		int[] disArr = WorldMapConstProperty.getInstance().getWorldSearchRadius();
		int dis = disArr[disArr.length - 1];
		if (minDistance > 0){
			dis = minDistance;
		}
		// 中心点
		AlgorithmPoint centerPoint = new AlgorithmPoint(pos[0], pos[1]);
		
		// 查找到的点集合
		TreeSet<WorldPoint> searchPoints = new TreeSet<>(new Comparator<WorldPoint>() {
			@Override
			public int compare(WorldPoint o1, WorldPoint o2) {
				double distance1 = centerPoint.distanceTo(new AlgorithmPoint(o1.getX(), o1.getY()));
				double distance2 = centerPoint.distanceTo(new AlgorithmPoint(o2.getX(), o2.getY()));
				return distance1 >= distance2 ? 1 : -1;
			}
		});
		
		// 野怪快速查找
		if (pointType == WorldPointType.MONSTER_VALUE
				&& (searchType == SearchType.SEARCH_MONSTER_VALUE || searchType == SearchType.SEARCH_YURI_MONSTER_VALUE)
				&& id == 0) {
			return monsterFastSearch(pos, pointType, dis, searchPoints);
		}
		
		List<Integer> marchPoint = new ArrayList<>();
		BlockingQueue<IWorldMarch> playerMarch = WorldMarchService.getInstance().getPlayerMarch(player.getId());
		for (IWorldMarch march : playerMarch) {
			marchPoint.add(march.getOrigionId());
			marchPoint.add(march.getTerminalId());
		}
		
		List<WorldPoint> points = WorldPointService.getInstance().getAroundWorldPointsWithType(pos[0], pos[1], dis, dis, pointType);
		for (WorldPoint point : points) {
			if (point.getPointType() != pointType) {
				continue;
			}
			
			if (marchPoint.contains(point.getId())) {
				continue;
			}
			
			// 尤里工厂
			if (pointType == WorldPointType.YURI_FACTORY_VALUE) {
				searchPoints.add(point);
			}
			// 迷雾要赛
			if (pointType == WorldPointType.FOGGY_FORTRESS_VALUE) {
				FoggyFortressCfg foggyCfg = HawkConfigManager.getInstance().getConfigByKey(FoggyFortressCfg.class, point.getMonsterId());
				if (foggyCfg != null && foggyCfg.getLevel() == id) {
					searchPoints.add(point);
				}
			}
			// 宝箱
			if (pointType == WorldPointType.BOX_VALUE && point.getMonsterId() == id) {
				searchPoints.add(point);
			}
			// 据点
			if (pointType == WorldPointType.STRONG_POINT_VALUE && point.getMonsterId() == id) {
				searchPoints.add(point);
			}
			// 野怪
			if ((pointType == WorldPointType.MONSTER_VALUE) && (searchType == SearchType.SEARCH_MONSTER_VALUE)) {
				WorldEnemyCfg monsterCfg = HawkConfigManager.getInstance().getConfigByKey(WorldEnemyCfg.class, point.getMonsterId());
				if (monsterCfg != null && monsterCfg.getType() == MonsterType.TYPE_1_VALUE && monsterCfg.getLevel() == id) {
					searchPoints.add(point);
				}
			}
			// 叛军野怪
			if ((pointType == WorldPointType.MONSTER_VALUE) && (searchType == SearchType.SEARCH_YURI_MONSTER_VALUE)) {
				WorldEnemyCfg monsterCfg = HawkConfigManager.getInstance().getConfigByKey(WorldEnemyCfg.class, point.getMonsterId());
				if (monsterCfg != null && monsterCfg.getType() == MonsterType.TYPE_2_VALUE &&monsterCfg.getRelateActivity() == 184 &&
						monsterCfg.getLevel() == id) {
					searchPoints.add(point);
				}
			}
			// 新版野怪
			if ((pointType == WorldPointType.MONSTER_VALUE) && (searchType == SearchType.SEARCH_NEW_MONSTER_VALUE) && (point.getCityLevel() == id)) {
				WorldEnemyCfg monsterCfg = HawkConfigManager.getInstance().getConfigByKey(WorldEnemyCfg.class, point.getMonsterId());
				if (monsterCfg != null && monsterCfg.getType() == MonsterType.TYPE_7_VALUE ) {
					searchPoints.add(point);
				}
			// 资源点
			}
			if (pointType == WorldPointType.RESOURCE_VALUE && point.getResourceId() == id) {
				if (!HawkOSOperator.isEmptyString(point.getPlayerId())) {
					continue;
				}
				searchPoints.add(point);
			}
			// 能量塔
			if (pointType == WorldPointType.PYLON_VALUE && point.getResourceId() == id) {
				if (!HawkOSOperator.isEmptyString(point.getPlayerId())) {
					continue;
				}
				searchPoints.add(point);
			}
			// 雪球
			if (pointType == WorldPointType.SNOWBALL_VALUE) {
				searchPoints.add(point);
			}
			
			//185活动叛军野怪
			if ((pointType == WorldPointType.MONSTER_VALUE) && (searchType == SearchType.SEARCH_NEW_ACT_MONSTER_VALUE)) {
				WorldEnemyCfg monsterCfg = HawkConfigManager.getInstance().getConfigByKey(WorldEnemyCfg.class, point.getMonsterId());
				if (monsterCfg != null && monsterCfg.getRelateActivity() == 185 && 
						monsterCfg.getType() == MonsterType.TYPE_2_VALUE && monsterCfg.getLevel() == id) {
					searchPoints.add(point);
				}
			}
		}
		
		if (searchPoints.isEmpty()) {
			return null;
		}
		
		List<WorldPoint> searchPointList = new ArrayList<WorldPoint>(searchPoints.size());
		searchPointList.addAll(searchPoints);
		return searchPointList.get(index % searchPointList.size());
	}

	/**
	 * 野怪快速查找
	 * @param pos
	 * @param pointType
	 * @param dis
	 * @param searchPoints
	 * @return
	 */
	private WorldPoint monsterFastSearch(int[] pos, int pointType, int dis, TreeSet<WorldPoint> searchPoints) {
		PlayerMonsterEntity monsterEntity = player.getData().getMonsterEntity();
		// 已击杀怪物的最大等级
		int killedLvl = monsterEntity.getMaxLevel();
		for (int lvl = killedLvl; lvl >= 0; lvl--) {
			// 按格子查找
			List<WorldPoint> points = WorldPointService.getInstance().getAroundWorldPointsWithType(pos[0], pos[1], dis, dis, pointType);
			for (WorldPoint point : points) {
				// 怪物配置
				WorldEnemyCfg enemyCfg = HawkConfigManager.getInstance().getConfigByKey(WorldEnemyCfg.class, point.getMonsterId());
				if ((enemyCfg.getType() == MonsterType.TYPE_1_VALUE || enemyCfg.getType() == MonsterType.TYPE_2_VALUE)
						&& enemyCfg.getLevel() == lvl + 1) {
					searchPoints.add(point);
				}
			}

			if (searchPoints.isEmpty()) {
				continue;
			}

			List<WorldPoint> searchPointList = new ArrayList<WorldPoint>(searchPoints.size());
			searchPointList.addAll(searchPoints);
			return searchPointList.get(0);
		}
		return null;
	}
	
	@ProtocolHandler(code = HP.code.WORLD_FAVORITE_INFO_C_VALUE)
	private void reqWorldFavorite(HawkProtocol protocol){
		player.getPush().syncWorldFavorite();
	}
	/**
	 * 添加到收藏夹
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.WORLD_FAVORITE_ADD_C_VALUE)
	private boolean addWorldFavorite(HawkProtocol protocol) {
		IDIPBanInfo banInfo = RedisProxy.getInstance().getIDIPBanInfo(player.getId(), IDIPBanType.BAN_WORLD_FAVORITE);
		if (banInfo != null && banInfo.getEndTime() >  HawkTime.getMillisecond()) {
			player.sendIdipNotice(NoticeType.SEND_MSG, NoticeMode.NOTICE_MSG, 0, banInfo.getBanMsg());
			return false;
		}
		
		// 禁止输入文本
		if (!GameUtil.checkBanMsg(player)) {
			return false;
		}
		
		WorldFavoriteAddReq req = protocol.parseProtocol(WorldFavoriteAddReq.getDefaultInstance());

		// 判断限制
		if (LocalRedis.getInstance().getFavoriteCount(player.getId()) >= WorldMapConstProperty.getInstance().getFavoriteMax()) {
			logger.error("world add favorite point failed, favoriteCnt:{} favoriteMax:{}", LocalRedis.getInstance().getFavoriteCount(player.getId()),
					WorldMapConstProperty.getInstance().getFavoriteMax());
			sendError(protocol.getType(), Status.Error.FAVORITE_MAX_LIMIT_VALUE);
			return false;
		}
		
//		if (!GameUtil.checkBanMsg(player)) {
//			return false;
//		}

		// 存储
		WorldFavoritePB favorite = req.getInfo();
		String value = JsonFormat.printToString(favorite);
		JSONObject json = new JSONObject();
		json.put("msg_type", 0);
		json.put("post_id", favorite.getFavoriteId());
		json.put("alliance_id", player.hasGuild() ? player.getGuildId() : "");
		json.put("param_id", favorite.getFavoriteId());
		GameTssService.getInstance().wordUicChatFilter(player, favorite.getName(), 
				MsgCategory.WORLD_FAVORITE_ADD.getNumber(), GameMsgCategory.WORLD_FAVORITE_ADD, 
				value, json, protocol.getType());
		return true;
	}

	/**
	 * 更新收藏夹内容
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.WORLD_FAVORITE_UPDATE_C_VALUE)
	private boolean updateWorldFavorite(HawkProtocol protocol) {
		IDIPBanInfo banInfo = RedisProxy.getInstance().getIDIPBanInfo(player.getId(), IDIPBanType.BAN_WORLD_FAVORITE);
		if (banInfo != null && banInfo.getEndTime() >  HawkTime.getMillisecond()) {
			player.sendIdipNotice(NoticeType.SEND_MSG, NoticeMode.NOTICE_MSG, 0, banInfo.getBanMsg());
			return false;
		}
		// 禁止输入文本
		if (!GameUtil.checkBanMsg(player)) {
			return false;
		}
		
		WorldFavoriteUpdateReq req = protocol.parseProtocol(WorldFavoriteUpdateReq.getDefaultInstance());

		// 存储更新
		WorldFavoritePB favorite = req.getInfo();
		String value = JsonFormat.printToString(favorite);
		JSONObject json = new JSONObject();
		json.put("msg_type", 0);
		json.put("post_id", favorite.getFavoriteId());
		json.put("alliance_id", player.hasGuild() ? player.getGuildId() : "");
		json.put("param_id", favorite.getFavoriteId());
		GameTssService.getInstance().wordUicChatFilter(player, favorite.getName(), 
				MsgCategory.WORLD_FAVORITE_ADD.getNumber(), GameMsgCategory.WORLD_FAVORITE_UPDATE, 
				value, json, protocol.getType());
		return true;
	}

	/**
	 * 删除收藏夹内容
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.WORLD_FAVORITE_DELETE_C_VALUE)
	private boolean deleteWorldFavorite(HawkProtocol protocol) {
		WorldFavoriteDelteReq req = protocol.parseProtocol(WorldFavoriteDelteReq.getDefaultInstance());
		String[] idList = new String[req.getFavoriteIdCount()];
		req.getFavoriteIdList().toArray(idList);
		LocalRedis.getInstance().deleteWorldFavorite(player.getId(), idList);

		BehaviorLogger.log4Service(player, Source.WORLD_ACTION, Action.WORLD_REMOVE_FAVORITE, Params.valueOf("idList", idList));
		player.responseSuccess(protocol.getType());
		return true;
	}

	/**
	 * 玩家迁城
	 * 
	 * @param session
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.WORLD_MOVE_CITY_C_VALUE)
	private boolean onPlayerMoveCity(HawkProtocol protocol) {
		WorldMoveCityReq req = protocol.parseProtocol(WorldMoveCityReq.getDefaultInstance());
		return moveCity(req.getType(), req.getX(), req.getY(), req.hasForce() && req.getForce(), true);
	}

	public boolean moveCity(int moveCityType, int x, int y, boolean force, boolean needConsume) {
		// 迁移前城点
		WorldPoint beforePoint = WorldPlayerService.getInstance().getPlayerWorldPoint(player.getId());
		// 迁移前坐标
		int[] beforePos = (beforePoint == null) ? (new int[] { 0, 0 }) : GameUtil.splitXAndY(beforePoint.getId());
		// 迁移前城点行军信息
		Collection<IWorldMarch> beforePointMarchs = WorldMarchService.getInstance().getWorldPointMarch(beforePos[0], beforePos[1]);
		
		// 城点保护结束时间
		long protectedEndTime = (beforePoint == null) ? 0 : beforePoint.getProtectedEndTime();
		// 迁城消耗
		ConsumeItems consumeItems = ConsumeItems.valueOf();

		// 迁城检测
		if (!moveCityCheck(HP.code.WORLD_MOVE_CITY_C_VALUE, consumeItems, moveCityType, force, x, y, needConsume)) {
			return false;
		}
		
		// 投递世界线程执行
		WorldThreadScheduler.getInstance().postWorldTask(new WorldTask(GsConst.WorldTaskType.PLAYER_MOVE_CITY) {
			@Override
			public boolean onInvoke() {
				int posX = x;
				int posY = y;
				// 收回行军
				WorldMarchService.getInstance().mantualMoveCityProcessMarch(player);
				
				// 目标点
				if (WorldUtil.isRandomMoveCity(moveCityType)) {
					// 随机迁城
					Point point = WorldPlayerService.getInstance().randomSettlePoint(player, false);
					// 迁城失败
					if (point == null) {
						sendProtocol(HawkProtocol.valueOf(HP.code.WORLD_MOVE_CITY_S_VALUE, WorldMoveCityResp.newBuilder().setResult(false)));
						return false;
					}
					posX = point.getX();
					posY = point.getY();
				}
				//原点迁城不检测中心点
				if(beforePoint != null && beforePoint.getId() != GameUtil.combineXAndY(posX, posY)){
					Point tarPoint = WorldPointService.getInstance().getAreaPoint(posX, posY, true);
					if(tarPoint == null || !tarPoint.canPlayerSeat()){
						sendProtocol(HawkProtocol.valueOf(HP.code.WORLD_MOVE_CITY_S_VALUE, WorldMoveCityResp.newBuilder().setResult(false)));
						return false;
					}
				}
				// 定点迁城 //联盟迁城
				WorldPoint targetPoint = WorldPlayerService.getInstance().mantualSettleCity(player, posX, posY, protectedEndTime);
				// 迁城失败
				if (targetPoint == null) {
					sendProtocol(HawkProtocol.valueOf(HP.code.WORLD_MOVE_CITY_S_VALUE, WorldMoveCityResp.newBuilder().setResult(false)));
					return false;
				}
				// 迁城成功
				doMoveCitySuccess(moveCityType, beforePos, targetPoint);

				for (IWorldMarch beforePointMarch : beforePointMarchs) {
					beforePointMarch.updateMarch();
				}
				
				//假如有守护对象需要同步一下守护对象的信息 迁城应该是一个低频的操作,这里同步一下问题不大,
				//如果有优化的必要就计算一下之前的距离和现在的距离是否有达成守护特效的条件
				String guardPlayerId = RelationService.getInstance().getGuardPlayer(player.getId());
				if (!HawkOSOperator.isEmptyString(guardPlayerId)) {
					WorldPoint wp = WorldPlayerService.getInstance().getPlayerWorldPoint(guardPlayerId);
					if (wp != null) {
						WorldPointService.getInstance().getWorldScene().update(wp.getAoiObjId());
					}
				}
				// 投递回玩家线程：消耗道具
				if (needConsume) {
					player.dealMsg(MsgId.MOVE_CITY, new WorldMoveCityMsgInvoker(player, consumeItems, moveCityType));
					HawkApp.getInstance().postMsg(player.getXid(), WorldMoveCityMsg.valueOf());
				}
				
				MissionManager.getInstance().postMsg(player, new EventMoveCity(true));
				return true;
			}
		});
		return true;
	}

	/**
	 * 迁城成功
	 * @param hp
	 * @param req
	 * @param moveCityType
	 * @param beforePos
	 * @param targetPoint
	 */
	private void doMoveCitySuccess(int moveCityType, int[] beforePos, WorldPoint targetPoint) {

		// 回复协议
		WorldMoveCityResp.Builder builder = WorldMoveCityResp.newBuilder();
		builder.setResult(true);
		builder.setX(targetPoint.getX());
		builder.setY(targetPoint.getY());
		player.sendProtocol(HawkProtocol.valueOf(HP.code.WORLD_MOVE_CITY_S_VALUE, builder));

		// 恢复城墙状态，投递回玩家线程执行
		player.dealMsg(MsgId.CITY_DEF_RECOVER, new WorldCityDefRecoverMsgInvoker(player));

		// 记录迁城时间
		player.getData().getStatisticsEntity().addCityMoveRecord(moveCityType, HawkTime.getMillisecond());
		
		//清除尤里复仇当前行军
		String guildId = player.getGuildId();
		if(guildId != null){
			WorldFoggyFortressService.getInstance().removePlayerMonsterMarch(guildId, player.getId(), false);
			int authority = GuildService.getInstance().getPlayerGuildAuthority(player.getId());
			GuildService.getInstance().notifyGuildFavouriteRedPoint(guildId, GsConst.GuildFavourite.TYPE_GUILD_MEMBER, authority);
		}
		
		// 重推警报
		BlockingQueue<IWorldMarch> terminalPtMarchs = WorldMarchService.getInstance().getPlayerPassiveMarch(player.getId());
		if(terminalPtMarchs != null){
			for(IWorldMarch mar : terminalPtMarchs){
				if(mar instanceof IReportPushMarch){
					((IReportPushMarch) mar).removeAttackReport();
				}
			}
		}
		
		// 发送邮件---迁城邮件
		SystemMailService.getInstance().sendMail(MailParames.newBuilder()
				.setPlayerId(player.getId())
				.setMailId(MailId.MOVE_CITY_MAIL)
				.addTitles(targetPoint.getX(), targetPoint.getY())
				.addSubTitles(targetPoint.getX(), targetPoint.getY())
				.addContents(targetPoint.getX(), targetPoint.getY(), beforePos[0], beforePos[1])
				.build());

		player.getData().updatePlayerPos(targetPoint.getX(), targetPoint.getY());
		// 行为日志
		BehaviorLogger.log4Service(player, Source.WORLD_POINT, Action.RANDOM_MOVE_CITY,
				Params.valueOf("fromPos", String.format("(%d, %d)", beforePos[0], beforePos[1])),
				Params.valueOf("toPos", String.format("(%d, %d)", targetPoint.getX(), targetPoint.getY())));
	}

	/**
	 * 检测迁城消耗, 返回值非0表示错误码
	 * 
	 * @param type
	 * @param consumeItems
	 * @return
	 */
	private boolean checkMoveCityConsume(int hp, int type, ConsumeItems consumeItems, boolean forceMove) {
		// 强制迁城不消耗资源
		if (forceMove) {
			return true;
		}

		// 随机迁城判断
		if (type == CityMoveType.RANDOM_MOVE_VALUE) {
			// 迁城道具判断
			if (player.getData().getItemNumByItemId(Const.ItemId.ITEM_RANDOM_MOVE_CITY_VALUE) > 0) {
				consumeItems.addItemConsume(Const.ItemId.ITEM_RANDOM_MOVE_CITY_VALUE, 1);
			} else {
				ShopCfg shopCfg = ShopCfg.getShopCfgByItemId(Const.ItemId.ITEM_RANDOM_MOVE_CITY_VALUE);
				if (shopCfg == null) {
					sendError(hp, Status.Error.ITEM_NOT_FOUND_VALUE);
					return false;
				}
				consumeItems.addConsumeInfo(shopCfg.getPriceItemInfo(), true);
			}
		} else if (type == CityMoveType.SELECT_MOVE_VALUE) {
			// 迁城道具判断（优先使用新手高迁）
			if (player.getData().getItemNumByItemId(Const.ItemId.ITEM_SELECT_MOVE_CITY_NEW_VALUE) > 0) {
				consumeItems.addItemConsume(Const.ItemId.ITEM_SELECT_MOVE_CITY_NEW_VALUE, 1);
			} else if (player.getData().getItemNumByItemId(Const.ItemId.ITEM_SELECT_MOVE_CITY_VALUE) > 0) {
				consumeItems.addItemConsume(Const.ItemId.ITEM_SELECT_MOVE_CITY_VALUE, 1);
			} else {
				ShopCfg shopCfg = ShopCfg.getShopCfgByItemId(Const.ItemId.ITEM_SELECT_MOVE_CITY_VALUE);
				if (shopCfg == null) {
					sendError(hp, Status.Error.ITEM_NOT_FOUND_VALUE);
					return false;
				}
				consumeItems.addConsumeInfo(shopCfg.getPriceItemInfo(), true);
			}
		} else if (type == CityMoveType.GUILD_MOVE_VALUE) {
			// 迁城道具判断
			if (player.getData().getItemNumByItemId(Const.ItemId.ITEM_GUILD_MOVE_CITY_VALUE) <= 0) {
				return false;
			}
			consumeItems.addItemConsume(Const.ItemId.ITEM_GUILD_MOVE_CITY_VALUE, 1);
		} else if(type == CityMoveType.GUILD_CREATE_AUTO_MOVE_VALUE ||
				type == CityMoveType.GUILD_JOIN_AUTO_MOVE_VALUE){
			int cnt = player.getData().getPlayerOtherEntity().getAutoGuildCityMoveCnt();
			if(cnt >= WorldMapConstProperty.getInstance().getGuildAutoMoveCount()){
				return false;
			}
		}
		
		if (!consumeItems.checkConsume(player, hp)) {
			return false;
		}
		return true;
	}

	/**
	 * 迁城检测
	 * 1、消耗检测
	 * 2、根据类型进行迁城前检测
	 * 
	 * @param hp 协议号
	 * @param req 客户端请求
	 * @param consumeItems 消耗
	 * @return
	 */
	public boolean moveCityCheck(int hp, ConsumeItems consumeItems, int moveCityType, boolean force, int posX, int posY, boolean needConsume) {
		// 迁城类型
		if (!WorldUtil.isRandomMoveCity(moveCityType) && !WorldUtil.isSelectMoveCity(moveCityType) && !WorldUtil.isGuildMoveCity(moveCityType)
				&& !WorldUtil.isGuildAutoMoveCity(moveCityType)) {
			sendError(hp, Status.SysError.PARAMS_INVALID_VALUE);
			return false;
		}

		boolean forceMove = force ? true : false;
		// 对客户端发来的参数进行校验
		if (forceMove && player.getPlayerBaseEntity().getCityDefVal() > 0) {
			forceMove = false;
		}

		// 消耗检测
		if (needConsume && !checkMoveCityConsume(hp, moveCityType, consumeItems, forceMove)) {
			return false;
		}

		// 迁城检测
		if (WorldUtil.isRandomMoveCity(moveCityType) && !randomMoveCityCheck(forceMove)) {
			return false;

		} else if (WorldUtil.isSelectMoveCity(moveCityType) && !selectMoveCityCheck(hp, posX, posY)) {
			return false;

		} else if(WorldUtil.isGuildMoveCity(moveCityType) && !guildMoveCityCheck()){
			return false;
		}else if(WorldUtil.isGuildAutoMoveCity(moveCityType) && !guildAutoMoveCityCheck()){
			return false;
		}

		return true;
	}

	/**
	 * 随机迁城检测
	 * 
	 * 1)当玩家有部队在城点外面时，玩家无法使用随机迁城
	 * 2)当玩家处于被攻击或被侦查状态时，包括外面的资源点和驻扎点被攻击或侦查，都无法使用随机迁城
	 * 3)当玩家援助他人时，无法使用随机迁城
	 * 
	 * @return
	 */
	private boolean randomMoveCityCheck(boolean forceMove) {
		// 自己有出征队伍
		if (!forceMove && WorldMarchService.getInstance().getPlayerMarchCount(player.getId()) > 0) {
			logger.error("random move city failed, has march in world");
			sendError(HP.code.WORLD_MOVE_CITY_C_VALUE, Status.Error.HAS_MARCH_IN_WORLD);
			return false;
		}

		// 是否被攻击
		Set<IWorldMarch> marchList = WorldMarchService.getInstance().getPlayerPassiveMarchs(player.getId(),
				WorldMarchType.ATTACK_PLAYER_VALUE, WorldMarchStatus.MARCH_STATUS_MARCH_VALUE);
		if (marchList != null && marchList.size() > 0) {
			logger.error("random move city failed, being attacked");
			sendError(HP.code.WORLD_MOVE_CITY_C_VALUE, Status.Error.RANDOM_MOVE_CITY_BEING_ATTACTED);
			return false;
		}

		// 是否被侦查
		boolean beSpy = false;
		marchList = WorldMarchService.getInstance().getPlayerPassiveMarchs(player.getId(), WorldMarchType.SPY_VALUE, WorldMarchStatus.MARCH_STATUS_MARCH_VALUE);
		for (IWorldMarch march : marchList) {
			if (march.getTerminalId() == player.getPlayerPos()) {
				beSpy = true;
			}
		}
		if (beSpy) {
			logger.error("random move city failed, being investigated");
			sendError(HP.code.WORLD_MOVE_CITY_C_VALUE, Status.Error.CITY_BEEN_SPYING);
			return false;
		}
		
		return true;
	}

	/**
	 * 定点迁城检测
	 * @return
	 */
	private boolean selectMoveCityCheck(int hp, int posX, int poxY) {
		if (WorldPlayerService.getInstance().checkPlayerCanOccupy(player, posX, poxY)) {
			return true;
		}

		// 为避免数据不统一的情况,刷新一下当前看到的地图
		WorldScene.getInstance().move(player.getAoiObjId(), posX, poxY, 0);
		sendError(hp, Status.Error.WORLD_POINT_INVALID);
		return false;
	}
	
	/**
	 * 联盟迁城检测
	 * @return
	 */
	private boolean guildMoveCityCheck(){
		//判断玩家是否有联盟
		if(!player.hasGuild()){
			sendError(HP.code.GUILD_MOVE_CITY_C_VALUE, Status.Error.GUILD_NO_JOIN);
			return false;
		}
		return true;
	}
	
	
	/**
	 * 联盟迁城检测
	 * @return
	 */
	private boolean guildAutoMoveCityCheck(){
		//判断玩家是否有联盟
		if(!player.hasGuild()){
			sendError(HP.code.GUILD_MOVE_CITY_C_VALUE, Status.Error.GUILD_NO_JOIN);
			return false;
		}
		
		int cnt = player.getData().getPlayerOtherEntity().getAutoGuildCityMoveCnt();
		if(cnt >= WorldMapConstProperty.getInstance().getGuildAutoMoveCount()){
			return false;
		}
		return true;
	}
	
	
	/**
	 * 公会迁城
	 * 
	 * @param session
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.GUILD_MOVE_CITY_C_VALUE)
	private boolean onGuildMoveCity(HawkProtocol protocol) {
		// 判断玩家是否有联盟
		if (!player.hasGuild()) {
			sendError(HP.code.GUILD_MOVE_CITY_C_VALUE, Status.Error.GUILD_NO_JOIN);
			return false;
		}
		
		// 迁城消耗
		ConsumeItems consumeItems = ConsumeItems.valueOf();
		if (player.getData().getItemNumByItemId(Const.ItemId.ITEM_GUILD_MOVE_CITY_VALUE) <= 0) {
			return false;
		}
		consumeItems.addItemConsume(Const.ItemId.ITEM_GUILD_MOVE_CITY_VALUE, 1);
		if (!consumeItems.checkConsume(player, protocol.getType())) {
			return false;
		}
		
		int[] allianceMoveOffsetArr = WorldMapConstProperty.getInstance().getAllianceMoveOffsetArr();
		
		Integer manorPos = GuildManorService.getInstance().getGuildManorPointId(player.getGuildId());
		if (Objects.nonNull(manorPos)) {
			int[] retPos = GameUtil.splitXAndY(manorPos);
			
			GuildMoveCityResp.Builder builder = GuildMoveCityResp.newBuilder();
			builder.setResult(true);
			builder.setX(retPos[0] + allianceMoveOffsetArr[0]);
			builder.setY(retPos[1] + allianceMoveOffsetArr[1]);
			player.sendProtocol(HawkProtocol.valueOf(HP.code.GUILD_MOVE_CITY_S_VALUE, builder));
		} else {
			String leaderId = GuildService.getInstance().getGuildLeaderId(player.getGuildId());
			int leaderPos = WorldPlayerService.getInstance().getPlayerPos(leaderId);
			if (leaderPos <= 0) {
				sendError(protocol.getType(), Status.Error.GUILD_LEADER_NO_PLACE);
				return false;
			}
			
			int[] retPos = GameUtil.splitXAndY(leaderPos);
			// 回复协议
			GuildMoveCityResp.Builder builder = GuildMoveCityResp.newBuilder();
			builder.setResult(true);
			builder.setX(retPos[0] + allianceMoveOffsetArr[0]);
			builder.setY(retPos[1] + allianceMoveOffsetArr[1]);
			player.sendProtocol(HawkProtocol.valueOf(HP.code.GUILD_MOVE_CITY_S_VALUE, builder));
		}
		
		return true;
	}

	/**
	 * 获取世界点详细信息
	 * @param session
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.WORLD_POINT_DETAIL_C_VALUE)
	private boolean onReqWorldPointDetail(HawkProtocol protocol) {
		ReqWorldPointDetail req = protocol.parseProtocol(ReqWorldPointDetail.getDefaultInstance());
		WorldPoint worldPoint = WorldPointService.getInstance().getWorldPoint(req.getPointX(), req.getPointY());
		if (worldPoint == null) {
			sendError(protocol.getType(), Status.Error.WORLD_POINT_EMPTY_VALUE);
			return false;
		}
		
		if (worldPoint.getPointType() == WorldPointType.PLAYER_VALUE && !WorldRobotService.getInstance().isRobotId(worldPoint.getPlayerId())) {
			String playerId = worldPoint.getPlayerId();
			if (!HawkOSOperator.isEmptyString(playerId)) {
				try {
					Player player = GlobalData.getInstance().makesurePlayer(playerId);
					if (player != null) {
						
						WorldPoint playerPos = WorldPlayerService.getInstance().getPlayerWorldPoint(playerId);
						if (playerPos == null || GameUtil.combineXAndY(worldPoint.getX(), worldPoint.getY()) != GameUtil.combineXAndY(playerPos.getX(), playerPos.getY())) {
							
							if (playerPos == null) {
								logger.info("pointOperatron, player point fix, worldPointX:{}, worldPointY:{}", worldPoint.getX(), worldPoint.getY());
							} else {
								logger.info("pointOperatron, player point fix, playerPosX{}, playerPosY{}, worldPointX:{}, worldPointY:{}", playerPos.getX(), playerPos.getY(), worldPoint.getX(), worldPoint.getY());
							}
							
							WorldPointService.getInstance().removeWorldPoint(worldPoint.getId());
							if (playerPos != null) {
								WorldPlayerService.getInstance().addPlayerPos(playerPos);
							}
							return false;
						}
						
						AccountRoleInfo air = RedisProxy.getInstance().getAccountRole(player.getServerId(), player.getPlatform(), player.getOpenId());
						if (player.isCsPlayer() && air != null && !GsConfig.getInstance().getServerId().equals(air.getActiveServer())) {
							Player.logger.error("try fix cross player playerId:{}, AccountRoleInfo:{}", playerId, air.toString());							
							do {
								//正常的玩家引用关系应该是完整的原服记录跨出，目标服记录跨入．
								String fromServerId = CrossService.getInstance().getImmigrationPlayerServerId(playerId);
								if (!HawkOSOperator.isEmptyString(fromServerId)) {
									String key  = RedisProxy.getInstance().EMIGRATION_PLAYER + ":" + fromServerId;
									String toServerId = RedisProxy.getInstance().getRedisSession().hGet(key, playerId);
									if (!HawkOSOperator.isEmptyString(toServerId) && toServerId.equals(GsConfig.getInstance().getServerId())) {
										break;
									}
								}												
								
								Player.logger.error("fix cross player playerId:{}", playerId);
								try {
									//移除跨服玩家相关数据
									GuildService.getInstance().onCsPlayerOut(player);
									//移除跨服记录的装扮信息
									WorldPointService.getInstance().removeShowDress(player.getId());
									WorldPointService.getInstance().removePlayerSignature(player.getId());
									//有可能这个玩家是在线的,可能状态没有清理掉.
									GlobalData.getInstance().removeActivePlayer(player.getId());
								} catch (Exception e) {
									HawkException.catchException(e);
								}							
								//调用清理关系
								CrossService.getInstance().clearImmigrationPlayer(player.getId());
								//删除player对象
								HawkApp.getInstance().removeObj(player.getXid());
								//无效playerData
								GlobalData.getInstance().invalidatePlayerData(player.getId());
								
								return false;
							} while (false);
						}
							
					} else {
						WorldPointService.getInstance().removeWorldPoint(worldPoint.getId());
						return false;
					}
					
				} catch (Exception e) {
					HawkException.catchException(e);
					return false;
				}
				
			}
		}
		
		if (worldPoint.getPointType() == WorldPointType.TH_RESOURCE_VALUE) {
			if (!HawkOSOperator.isEmptyString(worldPoint.getPlayerId())
					&& !HawkOSOperator.isEmptyString(worldPoint.getMarchId())) {
				IWorldMarch march = WorldMarchService.getInstance().getMarch(worldPoint.getMarchId());
				if (march == null) {
					WorldPointService.getInstance().removeWorldPoint(worldPoint.getId());
					return false;
				}
			}
		}
		
		if (worldPoint.getPointType() == WorldPointType.GUILD_TERRITORY_VALUE) {
			AbstractBuildable buildable = GuildManorService.getInstance().getBuildable(worldPoint);
			if (buildable != null) {
				if (buildable.getBuildType().equals(TerritoryType.GUILD_MINE)) {
					if (buildable.getbuildStat() == GuildBuildingNorStat.LOCKED_N_VALUE || buildable.getbuildStat() == GuildBuildingNorStat.OPENED_N_VALUE) {
						WorldPointService.getInstance().removeWorldPoint(worldPoint.getId());
						return false;
					}
				}
			}
		}
		
		if (worldPoint.getPointType() == WorldPointType.WAR_FLAG_POINT_VALUE) {
			if (!WarFlagService.getInstance().isActivityOpen()) {
				BlockingDeque<String> marchs = WorldMarchService.getInstance().getFlagMarchs(worldPoint.getGuildBuildId());
				for (String marchId : marchs) {
					IWorldMarch march = WorldMarchService.getInstance().getMarch(marchId);
					if (march != null && !march.isReturnBackMarch()) {
						WorldMarchService.getInstance().onPlayerNoneAction(march, HawkTime.getMillisecond());
					}
				}
				WorldPointService.getInstance().removeWorldPoint(worldPoint.getId());
			} else {
				IFlag flag = FlagCollection.getInstance().getFlag(worldPoint.getGuildBuildId());
				if (flag == null || flag.getState() == FlageState.FLAG_UNLOCKED_VALUE || flag.getState() == FlageState.FLAG_LOCKED_VALUE) {
					try {
						BlockingDeque<String> marchs = WorldMarchService.getInstance().getFlagMarchs(worldPoint.getGuildBuildId());
						for (String marchId : marchs) {
							IWorldMarch march = WorldMarchService.getInstance().getMarch(marchId);
							if (march != null && !march.isReturnBackMarch()) {
								WorldMarchService.getInstance().onPlayerNoneAction(march, HawkTime.getMillisecond());
							}
						}
					} catch (Exception e) {
						HawkException.catchException(e);
					}
					WorldPointService.getInstance().removeWorldPoint(worldPoint.getId());
				}
				
			}
		}
		
		if (worldPoint.getPointType() == WorldPointType.SPACE_MECHA_MAIN_VALUE || worldPoint.getPointType() == WorldPointType.SPACE_MECHA_SLAVE_VALUE
				|| worldPoint.getPointType() == WorldPointType.SPACE_MECHA_MONSTER_VALUE || worldPoint.getPointType() == WorldPointType.SPACE_MECHA_STRONG_HOLD_VALUE
				|| worldPoint.getPointType() == WorldPointType.SPACE_MECHA_BOX_VALUE) {
			spaceMechaPointResp(worldPoint);
			return true;
		} 
		
		worldPointDetailResp(worldPoint);
		return true;
	}
	
	/**
	 * 星甲召唤功能点信息同步
	 * @param worldPoint
	 */
	private void spaceMechaPointResp(WorldPoint worldPoint) {
		if (!SpaceMechaService.getInstance().isActivityOpen()) {
			forceMarchBack(worldPoint);
			WorldPointService.getInstance().removeWorldPoint(worldPoint.getId());
			return;
		}
		
		MechaSpaceInfo spaceObj = SpaceMechaService.getInstance().getGuildSpace(worldPoint.getGuildId());
		if (!HawkOSOperator.isEmptyString(worldPoint.getGuildId()) && (spaceObj == null || spaceObj.getStage() == null)) {
			forceMarchBack(worldPoint);
			WorldPointService.getInstance().removeWorldPoint(worldPoint.getId());
			return;
		}
		
		if (spaceObj == null || (spaceObj.getStageVal() != SpaceMechaStage.SPACE_PREPARE && spaceObj.getStageVal() != SpaceMechaStage.SPACE_GUARD_1)) {
			worldPointDetailResp(worldPoint);
			return;
		}
		
		SpaceWorldPoint spacePoint = spaceObj.getSpaceWorldPoint(SpacePointIndex.MAIN_SPACE);
		if (spacePoint != null) {
			worldPointDetailResp(spacePoint);
		}
		
		SpaceWorldPoint spacePoint1 = spaceObj.getSpaceWorldPoint(SpacePointIndex.SUB_SPACE_1);
		if (spacePoint1 != null) {
			worldPointDetailResp(spacePoint1);
		}
		
		SpaceWorldPoint spacePoint2 = spaceObj.getSpaceWorldPoint(SpacePointIndex.SUB_SPACE_2);
		if (spacePoint2 != null) {
			worldPointDetailResp(spacePoint2);
		}
	}
	
	/**
	 * 星甲召唤遣返行军
	 * @param worldPoint
	 */
	private void forceMarchBack(WorldPoint worldPoint) {
		boolean callback = false;
		Collection<IWorldMarch> marchs = WorldMarchService.getInstance().getWorldPointMarch(worldPoint.getId());
		for (IWorldMarch march : marchs) {	
			if (march.getMarchStatus() != WorldMarchStatus.MARCH_STATUS_RETURN_BACK_VALUE) {
				callback = true;
				break;
			}
		}
		
		if (!callback) {
			return;
		}
		
		WorldThreadScheduler.getInstance().postWorldTask(new WorldTask(0) {
			@Override
			public boolean onInvoke() {
				Collection<IWorldMarch> marchs = WorldMarchService.getInstance().getWorldPointMarch(worldPoint.getId());
				for (IWorldMarch march : marchs) {	
					if (march.getMarchStatus() == WorldMarchStatus.MARCH_STATUS_RETURN_BACK_VALUE) {
						continue;
					}
					
					try {
						if (march.getMarchType() == WorldMarchType.SPACE_MECHA_MONSTER_ATTACK_MARCH || march.getMarchType() == WorldMarchType.SPACE_MECHA_EMPTY_MARCH) {
							WorldMarchService.getInstance().removeMarch(march);
							continue;
						}
						
						Set<IWorldMarch> list = WorldMarchService.getInstance().getMassJoinMarchs(march, true);
						for (IWorldMarch march1 : list) {
							WorldMarchService.getInstance().onPlayerNoneAction(march1, HawkTime.getMillisecond());
						}
						
						if (march.getMarchStatus() == WorldMarchStatus.MARCH_STATUS_WAITING_VALUE) {
							WorldMarchService.getInstance().onMarchReturnImmediately(march, march.getArmys());
						} else {
							WorldMarchService.getInstance().onPlayerNoneAction(march, HawkTime.getMillisecond());
						}
					} catch (Exception e) {
						HawkException.catchException(e);
					}
				}
				return false;
			}
		});
	}
	
	/**
	 * 世界点详情信息同步
	 * 
	 * @param worldPoint
	 */
	private void worldPointDetailResp(WorldPoint worldPoint) {
		RespWorldPointDetail.Builder resp = RespWorldPointDetail.newBuilder();
		resp.setPoint(worldPoint.toDetailBuilder(player.getId()));
		
		List<IWorldMarch> towardsMarch = new ArrayList<IWorldMarch>();
		
		// 查找朝向目标点行军的
		Collection<IWorldMarch> worldPointMarchs = WorldMarchService.getInstance().getWorldPointMarch(worldPoint.getX(), worldPoint.getY());
		for (IWorldMarch iWorldMarch : worldPointMarchs) {
			if (iWorldMarch.isReturnBackMarch()) {
				continue;
			}
			if (!iWorldMarch.isMarchState()) {
				continue;
			}
			towardsMarch.add(iWorldMarch);
		}
		
		resp.setHasMarchTowards(!towardsMarch.isEmpty());
		for (IWorldMarch iWorldMarch : towardsMarch) {
			resp.addRelationType(WorldUtil.getRelation(iWorldMarch.getMarchEntity(), player));
		}
		player.sendProtocol(HawkProtocol.valueOf(HP.code.WORLD_POINT_DETAIL_S_VALUE, resp));
	}

	/**
	 * 请求生成世界点
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.GEN_WORLD_POINT_C_VALUE)
	private boolean onGenWorldPointReq(HawkProtocol protocol) {

		GenWorldPointReq req = protocol.parseProtocol(GenWorldPointReq.getDefaultInstance());
		// 目前只有野怪需求，后续在这里加(<--------那我就追加了啊)
		WorldPointType pointType = req.getType();
		if (pointType.equals(WorldPointType.MONSTER)) {
			return genMonsterWorldPoint(protocol, req);
		}else if(pointType.equals(WorldPointType.RESOURC_TRESURE)){
			return genResourceTreasuWorldPoint(protocol, req);
		} else {
			sendError(protocol.getType(), Status.SysError.PARAMS_INVALID);
			logger.error("onGenWorldPointReq failed, params error, playerId:{}, pointType:{}", player.getId(), req.getType());
			return false;
		}

	}
	
	/**
	 * 生成资源宝库
	 */
	private boolean genResourceTreasuWorldPoint(HawkProtocol protocol, GenWorldPointReq req) {
		int posX = req.getPosX();
		int posY = req.getPosY();
		int itemId = req.getItemId();

		// 道具不足
		ConsumeItems consumeItems = ConsumeItems.valueOf();
		consumeItems.addItemConsume(req.getItemId(), 1);
		if (!consumeItems.checkConsume(player, protocol.getType())) {
			return false;
		}

		// 道具
		ItemCfg itemCfg = HawkConfigManager.getInstance().getConfigByKey(ItemCfg.class, itemId);
		//宝库id
		int restreId = itemCfg.getResTreasure();
		// 宝库配置
		ResTreasureCfg resTreCfg = HawkConfigManager.getInstance().getConfigByKey(ResTreasureCfg.class, restreId);

		// 宝库配置为空
		if (resTreCfg == null) {
			sendError(protocol.getType(), Status.SysError.PARAMS_INVALID);
			logger.error("onGenWorldPointReq failed, ResTreasureCfg null, playerId:{}, itemId:{}, cfgId:{}", player.getId(), itemId, restreId);
			return false;
		}
		
		return WorldPointService.getInstance().genResourceTreasuWorldPoint(posX, posY, resTreCfg, player, consumeItems);
	}
	
	/**
	 * 生成怪物点
	 * @param protocol
	 * @param req
	 * @return
	 */
	private boolean genMonsterWorldPoint(HawkProtocol protocol, GenWorldPointReq req) {
		int posX = req.getPosX();
		int posY = req.getPosY();
		int itemId = req.getItemId();

		// 道具不足
		ConsumeItems consumeItems = ConsumeItems.valueOf();
		consumeItems.addItemConsume(req.getItemId(), 1);
		if (!consumeItems.checkConsume(player, protocol.getType())) {
			sendError(protocol.getType(), Status.Error.ITEM_NOT_ENOUGH);
			logger.error("onGenWorldPointReq failed, tool not enougth, playerId:{}, itemId:{}", player.getId(), itemId);
			return false;
		}

		// 道具
		ItemCfg itemCfg = HawkConfigManager.getInstance().getConfigByKey(ItemCfg.class, itemId);
		// 野怪id
		int monsterId = itemCfg.getWorldEnemy();
		// 野怪配置
		WorldEnemyCfg monsterCfg = HawkConfigManager.getInstance().getConfigByKey(WorldEnemyCfg.class, monsterId);

		// 野怪配置为空
		if (monsterCfg == null) {
			sendError(protocol.getType(), Status.SysError.PARAMS_INVALID);
			logger.error("onGenWorldPointReq failed, monsterCfg null, playerId:{}, itemId:{}, monsterId:{}", player.getId(), itemId, monsterId);
			return false;
		}
		//活动道具使用限制
		if(RadiationWarTwoActivity.useBossItemLimit("", itemId)){
			sendError(protocol.getType(), Status.SysError.PARAMS_INVALID);
			logger.error("onGenWorldPointReq failed, RadiationWarTwoActivity useBossItemLimit, playerId:{}, itemId:{}, monsterId:{}", player.getId(), itemId, monsterId);
			return false;
		}

		// 目标点
		int pointId = GameUtil.combineXAndY(posX, posY);

		// 超出地图范围
		int worldMaxX = WorldMapConstProperty.getInstance().getWorldMaxX();
		int worldMaxY = WorldMapConstProperty.getInstance().getWorldMaxY();
		if (posX >= worldMaxX || posX <= 0 || posY >= worldMaxY || posY <= 0) {
			logger.error("onGenWorldPointReq failed, out or range, playerId:{}, x:{}, y:{}", player.getId(), posX, posY);
			return false;
		}

		// 请求点为阻挡点
		if (MapBlock.getInstance().isStopPoint(pointId)) {
			logger.error("onGenWorldPointReq failed, is stop point, playerId:{}, x:{}, y:{}", player.getId(), posX, posY);
			return false;
		}

		// 请求点再国王领地内
		if (WorldPointService.getInstance().isInCapitalArea(pointId)) {
			logger.error("onGenWorldPointReq failed, is int capital area, playerId:{}, x:{}, y:{}", player.getId(), posX, posY);
			return false;
		}

		// 投递世界线程执行
		WorldThreadScheduler.getInstance().postWorldTask(new WorldTask(GsConst.WorldTaskType.WORLD_MONSTER_POINT_GENERATE) {
			@Override
			public boolean onInvoke() {

				// 中心点不是空闲点
				Point centerPoint = WorldPointService.getInstance().getAreaPoint(posX, posY, true);
				if (centerPoint == null) {
					logger.error("onGenWorldPointReq failed, centerPoint point not free, playerId:{}, posX:{}, posY:{}", player.getId(), posX, posY);
					return false;
				}

				// 占用点奇偶判断
				if (!centerPoint.canMonsterGen(MonsterType.valueOf(monsterCfg.getType()))) {
					logger.error("onGenWorldPointReq failed, centerPoint can't gen monster point, playerId:{}, posX:{}, posY:{}", player.getId(), posX, posY);
					return false;
				}

				// 野怪占用半径
				int monsterRadius = WorldMonsterService.getInstance().getMonsterRadius(monsterId);

				// 获取周围点
				List<Point> aroundPoints = WorldPointService.getInstance().getRhoAroundPointsFree(pointId, monsterRadius);
				if (aroundPoints.size() != 2 * monsterRadius * (monsterRadius - 1)) {
					logger.error("onGenWorldPointReq failed, arround points has been occupy, playerId:{}, posX:{}, posY:{}", player.getId(), posX, posY);
					return false;
				}

				// 投递回玩家线程：消耗道具
				player.dealMsg(MsgId.GEN_MONSTER, new GenerateMonsterMsgInvoker(player, consumeItems));

				// 中心点所在区域
				AreaObject areaObj = WorldPointService.getInstance().getArea(posX, posY);
				// 中心点所在资源带
				int zoneId = WorldUtil.getPointResourceZone(posX, posY);

				// 生成野怪点
				WorldPoint worldPoint = new WorldPoint(posX, posY, areaObj.getId(), zoneId, WorldPointType.MONSTER_VALUE);
				worldPoint.setMonsterId(monsterId);
				worldPoint.setLifeStartTime(HawkTime.getMillisecond());
				// 设置怪物血量
				int maxEnemyBlood = WorldMonsterService.getInstance().getMaxEnemyBlood(monsterId);
				worldPoint.setRemainBlood(maxEnemyBlood);
				
				// 创建玩家使用的世界点信息
				if (!WorldPointService.getInstance().createWorldPoint(worldPoint)) {
					logger.error("onGenWorldPointReq failed, createWorldPoint failed, playerId:{}, posX:{}, posY:{}", player.getId(), posX, posY);
					return false;
				}

				// 区域添加野怪boss点
				areaObj.addMonsterBoss(pointId);
				ActivityManager.getInstance().postEvent(new SummonMonsterEvent(player.getId(), monsterId));
				
				return true;
			}
		});
		return true;
	}
	
	/**
	 * 获得本次奖励
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.YURI_EXPLORE_REWARD_C_VALUE)
	private boolean onGetYuriReward(HawkProtocol protocol){
//		//获取探索行军
//		List<IWorldMarch> worldMarchs = WorldMarchService.getInstance().getPlayerMarch(player.getId(), WorldMarchType.YURI_EXPLORE_VALUE);
//		if(worldMarchs == null || worldMarchs.isEmpty()){
//			sendError(protocol.getType(), Status.Error.EXPLORE_MARCH_NOT_EXSIT);
//			return false;
//		}
//		IWorldMarch march = worldMarchs.get(0);
//		if(march.getMarchEntity().getMarchStatus() != WorldMarchStatus.MARCH_STATUS_EXPLORE_VALUE){
//			sendError(protocol.getType(), Status.Error.EXPLORE_MARCH_NOT_REACH);
//			return false;
//		}
//		YuriExploreMarch yuriExploreMarch = (YuriExploreMarch) march;
//		//准备消息
//		YuriRewardList.Builder builder = YuriRewardList.newBuilder();
//		builder.setStartTime(march.getMarchEntity().getResStartTime());
//		builder.setEndTime(march.getMarchEntity().getResEndTime());
//		int i = 0;
//		for (ItemInfo itemInfo : yuriExploreMarch.getAwardList()) {
//			i++;
//			YuriReward.Builder rewardBuilder = YuriReward.newBuilder();
//			rewardBuilder.setIdx(i);
//			rewardBuilder.setRewardContent(itemInfo.toString());
//			builder.addReward(rewardBuilder.build());
//		}
//		protocol.response(HawkProtocol.valueOf(HP.code.YURI_EXPLORE_REWARD_S_VALUE, builder));
		return true;
	}
	
	/**
	 * 获得本次奖励
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.SHARE_COORDINATE_C_VALUE)
	private boolean onShareCoordinate(HawkProtocol protocol){
		ShareCoordinateReq req = protocol.parseProtocol(ShareCoordinateReq.getDefaultInstance());
		int posX = req.getPosX();
		int posY = req.getPosY();
		String pointName = req.getPointName();
		
		// 世界最大范围
		int worldMaxX = WorldMapConstProperty.getInstance().getWorldMaxX();
		int worldMaxY = WorldMapConstProperty.getInstance().getWorldMaxY();
		
		// 坐标错误
		if (posX <= 0 || posY <= 0 || posX >= worldMaxX || posY >= worldMaxY) {
			sendError(protocol.getType(), Status.Error.COORDINAGE_ERROR);
			return false;
		}
		
		ChatService.getInstance().addWorldBroadcastMsg(Const.ChatType.CHAT_ALLIANCE, Const.NoticeCfgId.SHARECOORDINATE, player, pointName, posX, posY);
		player.responseSuccess(protocol.getType());
		return true;
	}
	
	/**
	 * 请求世界上最大等级野怪
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.WORLD_MAX_MONSTER_LVL_REQ_VALUE)
	private boolean onWroldMaxMonsterLvlReq(HawkProtocol protocol){
		player.getPush().syncMaxMonsterLevel();
		return true;
	}
	
	/**
	 * 客户端从后台切回来的时候, 同步服务器数据
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.CLIENT_OPEN_FROM_BACKROUND_VALUE)
	private boolean onClientOpenFromBackround(HawkProtocol protocol){
		return true;
	}
	
	/**
	 * 修改编队名称
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.MARCH_PRESET_NAME_CHANGE_C_VALUE)
	private boolean onChangeMarchPresetName(HawkProtocol protocol) {
		IDIPBanInfo banInfo = RedisProxy.getInstance().getIDIPBanInfo(player.getId(), IDIPBanType.BAN_MARCH_PRSET_NAME);
		if (banInfo != null && banInfo.getEndTime() >  HawkTime.getMillisecond()) {
			player.sendIdipNotice(NoticeType.SEND_MSG, NoticeMode.NOTICE_MSG, 0, banInfo.getBanMsg());
			return false;
		}
		
		// 禁止输入文本
		if (!GameUtil.checkBanMsg(player)) {
			return false;
		}
		
		PresetMarchNameChangeReq req = protocol.parseProtocol(PresetMarchNameChangeReq.getDefaultInstance());
		String name = req.getName();
		int res = GuildUtil.checkPresetMarchName(name);
		if (res != Status.SysError.SUCCESS_OK_VALUE) {
			sendError(protocol.getType(), res);
			return false;
		}
		
		int presetNum = ConstProperty.getInstance().getIniTroopTeamNum();
		int vipLevel = player.getVipLevel();
		if (vipLevel > 0) {
			VipCfg cfg = HawkConfigManager.getInstance().getConfigByKey(VipCfg.class, vipLevel);
			presetNum = cfg != null ? cfg.getFormation() : presetNum; 
		}
		
		VipSuperCfg vipSuperCfg = HawkConfigManager.getInstance().getConfigByKey(VipSuperCfg.class, player.getActivatedVipSuperLevel());
		if (vipSuperCfg != null) {
			presetNum += vipSuperCfg.getFormation();
		}
		
		int index = req.getIdx() - 1;
		int newIndex = req.getIdx() > GsConst.DUEL_INDEX ? index - 1: index;
		if (index < 0 || newIndex >= presetNum) {
			sendError(protocol.getType(), Status.SysError.PARAMS_INVALID_VALUE);
			return false;
		}
		
		JSONObject json = new JSONObject();
		json.put("msg_type", 0);
		json.put("post_id", index);
		json.put("alliance_id", player.hasGuild() ? player.getGuildId() : "");
		json.put("param_id", String.valueOf(req.getIdx()));
		GameTssService.getInstance().wordUicChatFilter(player, name, 
				MsgCategory.MARCH_PRESET_NAME.getNumber(), GameMsgCategory.WORLD_MARCH_PRESET_UPDATE, 
				String.valueOf(req.getIdx()), json, protocol.getType());
		
		player.responseSuccess(protocol.getType());
		
		return true;
	}
	
	/**
	 * 添加预设行军信息
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.ADD_WORLD_MARCH_PRESET_INFO_C_VALUE)
	private boolean onAddWorldMarchPresetInfo(HawkProtocol protocol) {
		IDIPBanInfo banInfo = RedisProxy.getInstance().getIDIPBanInfo(player.getId(), IDIPBanType.BAN_MARCH_PRSET_NAME);
		if (banInfo != null && banInfo.getEndTime() >  HawkTime.getMillisecond()) {
			player.sendIdipNotice(NoticeType.SEND_MSG, NoticeMode.NOTICE_MSG, 0, banInfo.getBanMsg());
			return false;
		}
		
		// 禁止输入文本
		if (!GameUtil.checkBanMsg(player)) {
			return false;
		}
		
		int presetNum = ConstProperty.getInstance().getIniTroopTeamNum();
		int vipLevel = player.getVipLevel();
		if (vipLevel > 0) {
			VipCfg cfg = HawkConfigManager.getInstance().getConfigByKey(VipCfg.class, vipLevel);
			presetNum = cfg != null ? cfg.getFormation() : presetNum; 
		}
		
		VipSuperCfg vipSuperCfg = HawkConfigManager.getInstance().getConfigByKey(VipSuperCfg.class, player.getActivatedVipSuperLevel());
		if (vipSuperCfg != null) {
			presetNum += vipSuperCfg.getFormation();
		}
		
		PresetMarchInfo req = protocol.parseProtocol(PresetMarchInfo.getDefaultInstance());
		if (req.getSuperSoldierId() > 0) {
			if (player.getSuperSoldierByCfgId(req.getSuperSoldierId()) == null) {
				return false;
			}
		}
		if (req.getHeroIdsCount() > 2) { // 验证英雄不能超过2个
			return false;
		}
		// 检测皮肤信息
		PlayerMarchModule marckModule = player.getModule(ModuleType.WORLD_MARCH_MODULE);
		if (!marckModule.checkMarchDressReq(req.getMarchDressList())) {
			return false;
		}
		for (PlayerHero hero : player.getHeroByCfgId(req.getHeroIdsList())) { // 两英雄互斥
			if (req.getHeroIdsList().contains(hero.getConfig().getProhibitedHero())) {
				return false;
			}
		}
		
		if (req.getSuperLab() != 0 && !player.isSuperLabActive(req.getSuperLab())) {
			return false;
		}
		if (req.getManhattan().getManhattanAtkSwId() > 0 || req.getManhattan().getManhattanDefSwId() > 0) {
			// 检测超武信息,判断是否解锁，
			PlayerManhattanModule manhattanModule = player.getModule(ModuleType.MANHATTAN);
			if (!manhattanModule.checkMarchReq(req.getManhattan())) {
				return false;
			}
		}
		if (req.getIdx() == GsConst.DUEL_INDEX) { // 决斗队列特例
			if (!req.getPercentArmy()) {
				Skill10104 skill = TalentSkillContext.getInstance().getSkill(GsConst.SKILL_10104);
				int maxArmy = skill.maxSoldier(player);
				int armyCount = req.getArmyList().stream().mapToInt(ArmySoldierPB::getCount).sum();
				if (armyCount > maxArmy) {
					return false;
				}
			}
			
			JSONArray arr = getPlayerPresetMarchInfo();
			JSONObject obj = new JSONObject();
			addWorldMarchPresetInfo(req, arr, obj, GsConst.DUEL_INDEX - 1, 0);
		} else {
			int index = req.getIdx() - 1;
			int newIndex = req.getIdx() > GsConst.DUEL_INDEX ? index - 1: index;
			if (index < 0 || newIndex >= presetNum) {
				return false;
			}

			// 检查名字可用性
			if (!req.hasName()) {
				JSONArray arr = getPlayerPresetMarchInfo();
				JSONObject obj = new JSONObject();
				addWorldMarchPresetInfo(req, arr, obj, index, 0);
			} else {
				String name = req.getName();
				int res = GuildUtil.checkPresetMarchName(name);
				if (res != Status.SysError.SUCCESS_OK_VALUE) {
					sendError(protocol.getType(), res);
					return false;
				}

				JSONObject json = new JSONObject();
				json.put("msg_type", 0);
				json.put("post_id", index);
				json.put("alliance_id", player.hasGuild() ? player.getGuildId() : "");
				json.put("param_id", String.valueOf(req.getIdx()));
				
				int newCount = incrementor.incrementAndGet();
				reqInfoCacheMap.put(newCount, req); // 这里要将req缓存起来
				
				JSONObject callbackData = new JSONObject();
				callbackData.put("paramIdx", req.getIdx());
				callbackData.put("cacheKey", newCount);
				
				HawkLog.logPrintln("player preset march info, openid: {}, playerId: {}, paramIdx: {}, cacheKey: {}, size: {}", player.getOpenId(), player.getId(), req.getIdx(), newCount, reqInfoCacheMap.size());
				GameTssService.getInstance().wordUicChatFilter(player, name, 
						MsgCategory.MARCH_PRESET_NAME.getNumber(), GameMsgCategory.WORLD_MARCH_PRESET_ADD, 
						callbackData.toJSONString(), json, protocol.getType());
			}

		}
		return true;
	}
	
	public JSONArray getPlayerPresetMarchInfo() {
		JSONArray arr = new JSONArray(GsConst.MAX_PRESET_SIZE);
		String presetMarchStr = RedisProxy.getInstance().getPlayerPresetWorldMarch(player.getId());
		if (presetMarchStr != null) {
			arr.addAll(JSONArray.parseArray(presetMarchStr));
		}
		return arr;
	}
	
	public void addWorldMarchPresetInfo(PresetMarchInfo req, JSONArray arr, JSONObject obj, int index, int cacheKey) {
		if (req == null) {
			req = reqInfoCacheMap.get(cacheKey);
		}
		
		try {
			if(req.hasPercentArmy()){
				obj.put("percent", req.getPercentArmy());
			}
			if(req.hasCommandHero()){
				obj.put("commandHero", req.getCommandHero());
			}
			if(req.hasSameArmy()){
				obj.put("sameArmy", req.getSameArmy());
			}
			if(req.hasItemId()){
				obj.put("itemId", req.getItemId());
			}
//			if(req.hasIsActivateDressGroup()){
//				obj.put("isActivateDressGroup",req.getIsActivateDressGroup());
//			}
			obj.put("isActivateDressGroup",req.getIsActivateDressGroup());
			obj.put("superSoldierId", req.getSuperSoldierId());
			List<ArmySoldierPB> armylist = req.getArmyList();
			if(armylist != null && !armylist.isEmpty()){
				JSONArray armyArr = new JSONArray();
				for (ArmySoldierPB armySoldierPB : armylist) {
					HawkAssert.checkNonNegative(armySoldierPB.getCount());
					JSONObject armyObj = new JSONObject();
					armyObj.put("id", armySoldierPB.getArmyId());
					armyObj.put("count", armySoldierPB.getCount());
					armyArr.add(armyObj);
				}
				obj.put("army", armyArr);
			}
			List<Integer> herolist = req.getHeroIdsList();
			if(herolist != null && !herolist.isEmpty()){
				obj.put("hero", herolist);
			}
			int armourSuit = req.getArmourSuit().getNumber();
			if (armourSuit > 0 && armourSuit <= player.getEntity().getArmourSuitCount()) {
				obj.put("armourSuit", armourSuit);
			} else {
				obj.put("armourSuit", 0);
			}
			
			int mechacoreSuit = req.getMechacoreSuit().getNumber();
			boolean unlocked = player.getPlayerMechaCore().isSuitUnlocked(mechacoreSuit);
			obj.put("mechacoreSuit", unlocked ? mechacoreSuit : MechaCoreSuitType.MECHA_ONE_VALUE);
			
			TalentType talentType = req.getTalentType();
			if (talentType != null) {
				obj.put("talent", talentType.getNumber());
			} else {
				obj.put("talent", player.getEntity().getTalentType());
			}
			
			if (req.getSuperLab() > 0) {
				obj.put("superLab", req.getSuperLab());
			}
			if (req.getMarchDressCount() > 0) {
				obj.put("dress", req.getMarchDressList());
			}
			if (req.getManhattan().getManhattanAtkSwId()>0) {
				obj.put("manhattanAtkSwId", req.getManhattan().getManhattanAtkSwId());
			}
			if (req.getManhattan().getManhattanDefSwId()>0) {
				obj.put("manhattanDefSwId", req.getManhattan().getManhattanDefSwId());
			}
			arr.set(index, obj);

			RedisProxy.getInstance().addPlayerPresetWorldMarch(player.getId(), arr);
			reqInfoCacheMap.remove(cacheKey);  // 清理缓存数据
			PlayerPresetMarchInfo.Builder infos = makeMarchPresetBuilder();
			player.sendProtocol(HawkProtocol.valueOf(HP.code.GET_WORLD_MARCH_PRESET_INFO_S_VALUE, infos));
			
			player.responseSuccess(HP.code.ADD_WORLD_MARCH_PRESET_INFO_C_VALUE);
		} catch (Exception e) {
			HawkException.catchException(e);
			HawkLog.logPrintln("player preset march info callback, openid: {}, playerId: {}, paramIdx: {}, cacheKey: {}, size: {}", player.getOpenId(), player.getId(), index, cacheKey, reqInfoCacheMap.size());
		}
	}
	
	/**
	 * 获取玩家预设行军信息
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.GET_WORLD_MARCH_PRESET_INFO_C_VALUE)
	private boolean onGetWorldMarchPreSetInfo(HawkProtocol protocol){
		PlayerPresetMarchInfo.Builder infos = makeMarchPresetBuilder();
		if(infos != null) {
			player.sendProtocol(HawkProtocol.valueOf(HP.code.GET_WORLD_MARCH_PRESET_INFO_S_VALUE, infos));
		}
		return true;
	}

	/**
	 * 组装玩家预设信息
	 * @return
	 */
	public PlayerPresetMarchInfo.Builder makeMarchPresetBuilder() {
		PlayerPresetMarchInfo.Builder infos = GameUtil.makeMarchPresetBuilder(player.getId());
		if (infos == null) {
			player.sendProtocol(HawkProtocol.valueOf(HP.code.GET_WORLD_MARCH_PRESET_INFO_S_VALUE, PlayerPresetMarchInfo.newBuilder()));
			return null;
		}
		return infos;
	}
	
	
	/**
	 * 获取迷雾要塞敌军信息
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.FOGGY_DETAIL_INFO_REQ_C_VALUE)
	private boolean onGetFoggyDetailInfo(HawkProtocol protocol){
		GetFoggyDetailInfo req = protocol.parseProtocol(GetFoggyDetailInfo.getDefaultInstance());
		int x = req.getX();
		int y = req.getY();
		
		WorldPoint worldPoint = WorldPointService.getInstance().getWorldPoint(x, y);
		if(worldPoint == null || worldPoint.getPointType() != WorldPointType.FOGGY_FORTRESS_VALUE){
			sendError(protocol.getType(), Status.Error.POINT_NOT_EXIST_OR_TYPE_ERROR);
			return false;
		}
		FoggyDetailInfo.Builder builder = FoggyDetailInfo.newBuilder();
		
		FoggyInfo info = worldPoint.getFoggyInfoObj();
		//组装部队信息
		List<ArmyInfo> armylist = info.getArmyList();
		for (ArmyInfo armyInfo : armylist) {
			builder.addArmyInfo(armyInfo.toArmySoldierPB(info.getNpcPlayer()));
		}
		//组装英雄信息
		List<Integer> heroInfoIds = info.getHeroIds();
		for (Integer heroInfoId : heroInfoIds) {
			NPCHero npcHero = NPCHeroFactory.getInstance().get(heroInfoId);
			builder.addHeros(npcHero.toPBobj());
		}
		//发送给客户端
		player.sendProtocol(HawkProtocol.valueOf(HP.code.FOGGY_DETAIL_INFO_RESP_S_VALUE, builder));
		return true;
	}
	
	@ProtocolHandler(code = HP.code.GUNDAM_INFO_REQ_VALUE)
	private boolean onGundamInfo(HawkProtocol protocol){
		WorldGundamService.getInstance().pushGundamInfo(player);
		return true;
	}
	
	@ProtocolHandler(code = HP.code.NIAN_INFO_REQ_VALUE)
	private boolean onNianInfo(HawkProtocol protocol){
		WorldNianService.getInstance().pushNianInfo(player);
		return true;
	}
	
	@ProtocolHandler(code = HP.code.CHRISTMAS_INFO_REQ_VALUE)
	private void onChristmasInfoReq(HawkProtocol hawkProtocol) {
		WorldChristmasWarService.getInstance().pushChristmasInfo(player);
	}
	
	/**
	 * 雪球快速查找
	 */
	@ProtocolHandler(code = HP.code.WORLD_SNOWBALL_FAST_SEARCH_REQ_VALUE)
	private boolean onSnowballFastSearch(HawkProtocol protocol){
		WorldSearchResp.Builder resp = WorldSearchResp.newBuilder();
		
		int playerLastKick = WorldSnowballService.getInstance().getPlayerLastKick(player.getId());
		WorldPoint worldPoint = WorldSnowballService.getInstance().getSnowball(playerLastKick);
		if (playerLastKick == 0 || worldPoint == null) {
			resp.setType(SearchType.SEARCH_SNOWBALL);
			resp.setSuccess(false);
			resp.setTargetX(0);
			resp.setTargetY(0);
		} else {
			resp.setTargetX(worldPoint.getX());
			resp.setTargetY(worldPoint.getY());
			resp.setType(SearchType.SEARCH_SNOWBALL);
			resp.setSuccess(true);
		}
		player.sendProtocol(HawkProtocol.valueOf(HP.code.WORLD_SNOWBALL_FAST_SEARCH_RESP_VALUE, resp));
		return true;
	}

	/**
	 * 搜索周年庆蛋糕点
	 */
	public WorldPoint searchCakePoint() {
		WorldPoint cakePoint = WorldResTreasurePointService.getInstance().getCakeSharePoint();
		return cakePoint;
	}
	
	

	/**
	 * 原地高迁修复数据
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code2.MOVE_CITY_FIX_ARMY_REQ_VALUE)
	private boolean onMoveCityFixArmy(HawkProtocol protocol){
		if (player.isCsPlayer()) {
			player.sendError(HP.code2.MOVE_CITY_FIX_ARMY_REQ_VALUE, Status.Error.MOVE_CITY_FIX_CS_NOT, 0);
			return false;
		}
		DYZZTeamRoom team = DYZZService.getInstance().getPlayerTeamRoom(player.getId());
		if (player.isInDungeonMap() || player.getLmjyState() != null || team != null || WarCollegeInstanceService.getInstance().isInTeam(player.getId())) {
			player.sendError(HP.code2.MOVE_CITY_FIX_ARMY_REQ_VALUE, Status.Error.MOVE_CITY_FIX_DUNGEON_NOT, 0);
			return false;
		}
		if (!WorldMarchService.getInstance().getPlayerMarch(player.getId()).isEmpty()) {
			player.sendError(HP.code2.MOVE_CITY_FIX_ARMY_REQ_VALUE, Status.Error.MOVE_CITY_FIX_MARCH_NOT, 0);
			return false;
		}
		long curTime = HawkTime.getMillisecond();
		WorldPoint targetPoint = WorldPointService.getInstance().getWorldPoint(player.getPlayerPos());
		if (targetPoint.getShowProtectedEndTime() < curTime) { // 不开罩不行
			player.sendError(HP.code2.MOVE_CITY_FIX_ARMY_REQ_VALUE, Status.Error.MOVE_CITY_FIX_CITYSHIELDTIME_NOT, 0);
			return false;
		}
		// 城内有援助行军
		Set<IWorldMarch> marchList = WorldMarchService.getInstance().getPlayerPassiveMarchs(player.getId(), WorldMarchType.ASSISTANCE_VALUE,
				WorldMarchStatus.MARCH_STATUS_MARCH_ASSIST_VALUE);
		if (!marchList.isEmpty()) {
			player.sendError(HP.code2.MOVE_CITY_FIX_ARMY_REQ_VALUE, Status.Error.MOVE_CITY_FIX_ASST_NOT, 0);
			return false;
		}
		// 有被动行军
		BlockingQueue<IWorldMarch> passiveMarchs = WorldMarchService.getInstance().getPlayerPassiveMarch(player.getId());
		if (!CollectionUtils.isEmpty(passiveMarchs)) {
			int playerPos = WorldPlayerService.getInstance().getPlayerPos(player.getId());
			for (IWorldMarch march : passiveMarchs) {
				if (march == null || march.getMarchEntity() == null || march.getMarchEntity().isInvalid()) {
					continue;
				}
				if (march.getTerminalId() != playerPos) {
					continue;
				}
				if (march.getMarchType().getNumber() == WorldMarchType.ASSISTANCE_VALUE) {
					continue;
				}
				player.sendError(HP.code2.MOVE_CITY_FIX_ARMY_REQ_VALUE, Status.Error.MOVE_CITY_FIX_PASSIVEMARCH_NOT, 0);
				return false;
			}
		}
		//CD时间内
		long lastTime = RedisProxy.getInstance().getMoveCityFixArmyTime(player.getId());
		int cd = ConstProperty.getInstance().getMoveCityFixAramCD() * 1000;
		if (lastTime+cd > curTime) {
			player.sendError(HP.code2.MOVE_CITY_FIX_ARMY_REQ_VALUE, Status.Error.MOVE_CITY_FIX_CD_NOT, 0);
			return false;
		}
		//重置时间
		RedisProxy.getInstance().updateMoveCityFixArmyTime(player.getId(), curTime);
		//执行高迁
		WorldThreadScheduler.getInstance().postWorldTask(new WorldTask(GsConst.WorldTaskType.MOVE_CITY_IN_PLACE) {
			@Override
			public boolean onInvoke() {
				WorldPlayerService.getInstance().moveCity(player.getId(), false, true);
				GsApp.getInstance().addDelayAction(2000, new HawkDelayAction() {
					@Override
					protected void doAction() {
						int marchCount = WorldMarchService.getInstance().getPlayerMarchCount(player.getId());
						if (marchCount > 0) {
							HawkLog.logPrintln("onMoveCityFixArmy armysCheckAndFix, playerId: {}, marchCount: {}", player.getId(), marchCount);
							return;
						}
						List<Integer> armyIds = new ArrayList<Integer>();
						for (ArmyEntity armyEntity : player.getData().getArmyEntities()) {
							// 出征中的army数量
							int marchArmyCount = armyEntity.getMarch();
							if (marchArmyCount <= 0) {
								HawkLog.logPrintln("onMoveCityFixArmy armysCheckAndFix, playerId: {}, marchArmyCount: {}", player.getId(), marchArmyCount);
								continue;
							}
							int armyId = armyEntity.getArmyId();
							armyIds.add(armyId);
							
							armyEntity.clearMarch();
							armyEntity.addFree(marchArmyCount);
							LogUtil.logArmyChange(player, armyEntity, marchArmyCount, ArmySection.FREE, ArmyChangeReason.MARCH_FIX_SELF);
							HawkLog.logPrintln("onMoveCityFixArmy armysCheckAndFix, playerId:{}, armyId:{}, marchArmyCount:{}, armyFree:{}", armyEntity.getPlayerId(), armyEntity.getId(), marchArmyCount, armyEntity.getFree());
						}

						if (!armyIds.isEmpty()) {
							player.getPush().syncArmyInfo(ArmyChangeCause.MARCH_BACK, armyIds.toArray(new Integer[armyIds.size()]));
						}
					}
				});
				return true;
			}
		});
		player.responseSuccess(HP.code2.MOVE_CITY_FIX_ARMY_REQ_VALUE);
		return true;
	}
	
	
	
	@MessageHandler
	private void onPlayerJoinGuild(GuildJoinMsg msg){
		int threadIdx = this.player.getXid().getHashThread(HawkTaskManager.getInstance().getThreadNum());
		Player member = this.player;
		HawkTaskManager.getInstance().postTask(new HawkDelayTask(2000, 2000, 1) {
			@Override
			public Object run() {
				boolean isLeader = GuildService.getInstance().isGuildLeader(member.getId());
				if(isLeader){
					WorldPlayerService.getInstance().randomGuildCreatePoint(member);
				}else{
					String leaderId = GuildService.getInstance().getGuildLeaderId(msg.getGuildId());
					WorldPlayerService.getInstance().randomGuildJoinPoint(leaderId, member);
				}
				return null;
			}
			
		},threadIdx);
	}
}
