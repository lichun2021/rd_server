package com.hawk.game.module.spacemecha.module;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

import org.hawk.annotation.MessageHandler;
import org.hawk.annotation.ProtocolHandler;
import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;

import com.hawk.activity.ActivityManager;
import com.hawk.activity.event.impl.SpaceMechaPlaceEvent;
import com.hawk.activity.type.impl.spaceguard.SpaceGuardActivity;
import com.hawk.game.config.WorldMapConstProperty;
import com.hawk.game.entity.CustomDataEntity;
import com.hawk.game.entity.GuildInfoObject;
import com.hawk.game.log.BehaviorLogger;
import com.hawk.game.log.BehaviorLogger.Params;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.module.PlayerMarchModule;
import com.hawk.game.module.spacemecha.MechaSpaceConst;
import com.hawk.game.module.spacemecha.MechaSpaceInfo;
import com.hawk.game.module.spacemecha.SpaceMechaService;
import com.hawk.game.module.spacemecha.MechaSpaceConst.SpaceMechaGrid;
import com.hawk.game.module.spacemecha.MechaSpaceConst.SpacePointIndex;
import com.hawk.game.module.spacemecha.config.SpaceMechaBoxCfg;
import com.hawk.game.module.spacemecha.config.SpaceMechaCabinCfg;
import com.hawk.game.module.spacemecha.config.SpaceMechaConstCfg;
import com.hawk.game.module.spacemecha.config.SpaceMechaLevelCfg;
import com.hawk.game.module.spacemecha.config.SpaceMechaStrongholdCfg;
import com.hawk.game.module.spacemecha.config.SpaceMechaSubcabinCfg;
import com.hawk.game.module.spacemecha.worldpoint.SpaceWorldPoint;
import com.hawk.game.module.spacemecha.worldpoint.StrongHoldWorldPoint;
import com.hawk.game.msg.GuildJoinMsg;
import com.hawk.game.msg.GuildQuitMsg;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerModule;
import com.hawk.game.protocol.Activity.SelectSpaceLevelReq;
import com.hawk.game.protocol.Activity.SelectSpaceLevelResp;
import com.hawk.game.protocol.Const.ChatType;
import com.hawk.game.protocol.Const.NoticeCfgId;
import com.hawk.game.protocol.GuildManager.AuthId;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.SpaceMecha.MechaSpaceQuarterInfoReq;
import com.hawk.game.protocol.SpaceMecha.PlaceSpaceMachineReq;
import com.hawk.game.protocol.SpaceMecha.SapceMechaSummary;
import com.hawk.game.protocol.SpaceMecha.SpaceMechaInfoPB;
import com.hawk.game.protocol.SpaceMecha.SpaceMechaStage;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.World.MonsterType;
import com.hawk.game.protocol.World.WorldMarchReq;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.service.ArmyService;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.MailService;
import com.hawk.game.service.chat.ChatParames;
import com.hawk.game.service.chat.ChatService;
import com.hawk.game.util.EffectParams;
import com.hawk.game.util.GsConst;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.march.IWorldMarch;
import com.hawk.game.world.object.AreaObject;
import com.hawk.game.world.object.Point;
import com.hawk.game.world.service.WorldPointService;
import com.hawk.game.world.thread.WorldTask;
import com.hawk.game.world.thread.WorldThreadScheduler;
import com.hawk.log.Action;
import com.hawk.log.Source;


/**
 * 星甲召唤
 * 
 * @author lating
 *
 */
public class PlayerSpaceMechaModule extends PlayerModule {
	
	public PlayerSpaceMechaModule(Player player) {
		super(player);
	}
	
	/**
	 * 玩家登录
	 */
	public boolean onPlayerLogin() {
		if (SpaceMechaService.getInstance().isActivityOpen()) {
			syncSpaceMechaInfo(0);
		}
		return true;
	}
	
	@Override
	protected boolean onPlayerLogout() {
		if (SpaceMechaService.getInstance().isActivityOpen()) {
			MailService.getInstance().clearMailBaiId(player.getId(),
					MailId.SPACE_MECHA_STRONG_HOLD_FIGHT_WIN,
					MailId.SPACE_MECHA_STRONG_HOLD_FIGHT_FAILED,
					MailId.SPACE_MECHA_STRONGHOLD_BROKEN,
					MailId.SPACE_MECHA_MAINSPACE_BROKEN,
					MailId.SPACE_MECHA_SUBSPACE_BROKEN,
					MailId.SPACE_MECHA_FIGHT_FAILED,
					MailId.SPACE_MECHA_FIGHT_WIN);
		}
		return super.onPlayerLogout();
	}


	/**
	 * 召唤机甲舱体放置
	 */
	@ProtocolHandler(code = HP.code2.SPACE_MECHA_PLACE_REQ_VALUE)
	private void placeGuildSpace(HawkProtocol protocol) {
		PlaceSpaceMachineReq req = protocol.parseProtocol(PlaceSpaceMachineReq.getDefaultInstance());
		placeGuildSpace(req.getX(), req.getY(), req.getLevel(), protocol.getType());
	}
	
	/**
	 * 放置舱体
	 * 
	 * @param posX
	 * @param posY
	 * @param level
	 * @param protocol
	 */
	private void placeGuildSpace(int posX, int posY, int level, int protocol) {
		if (!canPlaceGuildSpace(posX, posY, level, protocol)) {
			return;
		}
		
		WorldThreadScheduler.getInstance().postWorldTask(new WorldTask(GsConst.WorldTaskType.PLACE_GUILD_SPACE) {
			@Override
			public boolean onInvoke() {
				if (!canPlaceGuildSpace(posX, posY, level, protocol)) {
					return false;
				}
				
				boolean success = SpaceMechaService.getInstance().placeGuildSpace(player, posX, posY, level);
				if (success) {
					int times = updateSpaceSetInfo();
					MechaSpaceInfo spaceObj = SpaceMechaService.getInstance().getGuildSpace(player.getGuildId());
					SpaceMechaService.getInstance().consumeGuildPoint(player.getGuildId(), spaceObj.getCost());
					SpaceMechaService.getInstance().logSpaceMechaPlace(player, spaceObj, posX, posY, times);
					
					SpaceMechaConstCfg cfg = HawkConfigManager.getInstance().getKVInstance(SpaceMechaConstCfg.class);
					int second = (int) ((spaceObj.getStage().getEndTime() - HawkTime.getMillisecond() + cfg.getCabinFirstWaveTime()) / 1000);
					// 发送联盟消息通知
					Object[] objects = new Object[] { player.getName(), posX, posY, second };
					ChatService.getInstance().addWorldBroadcastMsg(ChatParames.newBuilder().setChatType(ChatType.CHAT_ALLIANCE).setKey(NoticeCfgId.SPACE_MECHA_DOWN_REMAIND).setGuildId(player.getGuildId()).addParms(objects).build());
					spaceObj.syncSpaceMechaInfo();
					
					// 生成野怪
					SpaceMechaLevelCfg levelCfg = HawkConfigManager.getInstance().getConfigByKey(SpaceMechaLevelCfg.class, level);
					List<WorldPoint> monsterPoints = SpaceMechaService.getInstance().generateMonster(player.getGuildId(), MonsterType.TYPE_12_VALUE, levelCfg.getEnemyNumByWave(1), Collections.emptyList());
					int terminalPointId = spaceObj.getSpacePointId(SpacePointIndex.MAIN_SPACE);
					// 发假行军
					for (WorldPoint point : monsterPoints) {
						WorldMarchService.getInstance().startMonsterMarch(player.getGuildId(), WorldMarchType.SPACE_MECHA_EMPTY_MARCH_VALUE, 
								point.getId(), terminalPointId, null, player.getGuildId(), Collections.emptyList(), Collections.emptyList(), false);
					}
					
					syncActivityInfoNotify();
				}
				
				player.responseSuccess(protocol);
				return true;
			}
		});
	}
	
	/**
	 * 更新舱体放置信息
	 */
	private int updateSpaceSetInfo() {
		GuildInfoObject guildInfoObj = GuildService.getInstance().getGuildInfoObject(player.getGuildId());
		SpaceGuardActivity activity = SpaceMechaService.getInstance().getActivityObject(true);
		// 放置舱体的次数
		if (guildInfoObj.getSpaceMechaTermId() == activity.getActivityTermId()) {
			guildInfoObj.setSpaceSetTimes(guildInfoObj.getSpaceSetTimes() + 1);
		} else {
			guildInfoObj.resetSpaceMechaData(activity.getActivityTermId());
			guildInfoObj.setSpaceSetTimes(1);
		}
		
		return guildInfoObj.getSpaceSetTimes();
	}
	
	/**
	 * 通知相关人员同步活动数据
	 */
	private void syncActivityInfoNotify() {
		for(String memberId : GuildService.getInstance().getOnlineMembers(player.getGuildId())) {
			// 舱体放置者放置完后停留在大世界界面，进活动界面时会重新请求数据，所以这里可以不用同步
			if (memberId.equals(player.getId())) {
				continue;
			}
			
			boolean guildAuthority = GuildService.getInstance().checkGuildAuthority(memberId, AuthId.SPACE_MECHA_PLACE_AUTH);
			if (guildAuthority) {
				ActivityManager.getInstance().postEvent(new SpaceMechaPlaceEvent(memberId));
			}
		}
	}
	
	/**
	 * 判断是否可以放置联盟机甲舱体
	 * 
	 * @param posX
	 * @param posY
	 * @param level
	 * @return
	 */
	private boolean canPlaceGuildSpace(int posX, int posY, int level, int protocol) {
		if (!SpaceMechaService.getInstance().placeSpaceTimeCheck()) {
			sendError(protocol, Status.Error.SPACE_MECHA_PLACE_TIME_ERROR);
			HawkLog.errPrintln("place mecha space failed, activity not opened, playerId: {}", player.getId());
			return false;
		}
		
		String guildId = player.getGuildId();
		if (HawkOSOperator.isEmptyString(guildId)) {
			sendError(protocol, Status.Error.SPACE_MECHA_GUILD_NEED);
			HawkLog.errPrintln("place mecha space failed, no guild, playerId: {}", player.getId());
			return false;
		}
		
		GuildInfoObject guildInfoObj = GuildService.getInstance().getGuildInfoObject(guildId);
		if (guildInfoObj == null) {
			sendError(protocol, Status.Error.SPACE_MECHA_GUILD_NEED);
			HawkLog.errPrintln("place mecha space failed, guild object not exist, playerId: {}, guildId: {}", player.getId(), guildId);
			return false;
		}
		
		SpaceMechaConstCfg constCfg = HawkConfigManager.getInstance().getKVInstance(SpaceMechaConstCfg.class);
		SpaceGuardActivity activity = SpaceMechaService.getInstance().getActivityObject(true);
		if (guildInfoObj.getSpaceMechaTermId() != activity.getActivityTermId()) {
			guildInfoObj.resetSpaceMechaData(activity.getActivityTermId());
		} else if (guildInfoObj.getSpaceSetTimes() >= constCfg.getSetLimitNum()) {
			sendError(protocol, Status.Error.SPACE_MECHA_SET_TIMES_LIMIT);
			HawkLog.errPrintln("place mecha space failed, space settimes uplimit, playerId: {}, guildId: {}, times: {}", player.getId(), guildId, guildInfoObj.getSpaceSetTimes());
			return false;
		}
		
		MechaSpaceInfo spaceObj = SpaceMechaService.getInstance().getGuildSpace(guildId);
		if (spaceObj != null && spaceObj.getStage() != null) {
			sendError(protocol, Status.Error.SPACE_MECHA_PLACED);
			HawkLog.errPrintln("place mecha space failed, previous not end, playerId: {}, guildId: {}", player.getId(), guildId);
			return false;
		}
		
		int maxLevel = 1;
		if (spaceObj != null) {
			maxLevel = spaceObj.getMaxLevel();
		} else {
			maxLevel = Math.max(1, guildInfoObj.getSpaceMaxLv());
		}
		
		// 判断难度等级是否符合限制要求
		if (level > maxLevel) {
			sendError(protocol, Status.Error.SPACE_MECHA_LEVEL_ERROR);
			HawkLog.errPrintln("place mecha space failed, select level error, playerId: {}, guildId: {}, level: {}, maxLevel: {}", player.getId(), guildId, level, maxLevel);
			return false;
		} 
		
		SpaceMechaCabinCfg cfg = SpaceMechaCabinCfg.getCfgByLevel(level);
		if (cfg == null) {
			sendError(protocol, Status.Error.SPACE_MECHA_LEVEL_ERROR);
			HawkLog.errPrintln("place mecha space failed, config not exist, playerId: {}, guildId: {}, level: {}", player.getId(), guildId, level);
			return false;
		}
		
		// 权限
		boolean guildAuthority = GuildService.getInstance().checkGuildAuthority(player.getId(), AuthId.SPACE_MECHA_PLACE_AUTH);
		if (!guildAuthority) {
			sendError(protocol, Status.Error.SPACE_MECHA_PLACE_AUTH_ERROR);
		    HawkLog.errPrintln("place mecha space failed, guildAuthority error, playerId: {}, guildId: {}", player.getId(), guildId);		
			return false;
		}
		
		long pointCount = SpaceMechaService.getInstance().getGuildPointCount(guildId);
		// 判断代币够不够
		if (pointCount < cfg.getCost()) {
			sendError(protocol, Status.Error.SPACE_MECHA_POINT_NOT_ENOUGH);
			HawkLog.errPrintln("place mecha space failed, cost point not enough, playerId: {}, guildId: {}, pointCount: {}, cost need: {}", player.getId(), guildId, pointCount, cfg.getCost());
			return false;
		}
		
		if (!checkCanOccupied(posX, posY)) {
			sendError(protocol, Status.Error.SPACE_MECHA_POINT_OCCUPIED);
			HawkLog.errPrintln("place mecha space failed, point occupied, playerId: {}, guildId: {}, posX: {}, posY: {}", player.getId(), guildId, posX, posY);
			return false;
		}
		
		return true;
	}
	
	/**
	 * 判断点是否可用
	 * 
	 * @param centerPoint
	 * @param radius
	 * @return
	 */
	protected boolean checkCanOccupied(int centerX, int centerY) {
		// 距离边界的长度
		int boundary = GsConst.WORLD_BOUNDARY_SIZE;
		int worldMaxX = WorldMapConstProperty.getInstance().getWorldMaxX();
		int worldMaxY = WorldMapConstProperty.getInstance().getWorldMaxY();
		// 边界坐标点不可用的检测
		if (centerX <= boundary || centerY <= boundary || centerX >= (worldMaxX - boundary) || centerY >= (worldMaxY - boundary)) {
			return false;
		}
		
		// 主舱
		int mainSpaceX = centerX;
		int mainSpaceY = centerY - 1;
        Point point = WorldPointService.getInstance().getAreaPoint(mainSpaceX, mainSpaceY, true);
        if (point == null || !point.canSpaceMechaSeat(WorldPointType.SPACE_MECHA_MAIN_VALUE)) {
            return false;
        }
        AreaObject area = WorldPointService.getInstance().getArea(point.getAreaId());
		if (!WorldPointService.getInstance().tryOccupied(area, point, SpaceMechaGrid.SPACE_MAIN_GRID)) {
			return false;
		}

        // 子舱1
        int x1 = centerX - 1;
        int y1 = centerY + 1;
        point = WorldPointService.getInstance().getAreaPoint(x1, y1, true);
        if (point == null || !point.canSpaceMechaSeat(WorldPointType.SPACE_MECHA_SLAVE_VALUE)) {
            return false;
        }
        area = WorldPointService.getInstance().getArea(point.getAreaId());
		if (!WorldPointService.getInstance().tryOccupied(area, point, SpaceMechaGrid.SPACE_SLAVE_GRID)) {
			return false;
		}
        
		// 子舱2
        int x2 = centerX + 1; 
        int y2 = centerY + 1;
        point = WorldPointService.getInstance().getAreaPoint(x2, y2, true);
        if (point == null || !point.canSpaceMechaSeat(WorldPointType.SPACE_MECHA_SLAVE_VALUE)) {
            return false;
        }
        area = WorldPointService.getInstance().getArea(point.getAreaId());
		if (!WorldPointService.getInstance().tryOccupied(area, point, SpaceMechaGrid.SPACE_SLAVE_GRID)) {
			return false;
		}
        
        return true;
	}
	
	/**
	 * 请求星甲召唤信息
	 * 
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code2.SPACE_MECHA_INFO_REQ_VALUE)
	private void onSpaceMechaInfoReq(HawkProtocol protocol) {
		if (!SpaceMechaService.getInstance().isActivityOpen()) {
			sendError(protocol.getType(), Status.Error.SPACE_MECHA_ACTIVITY_CLOSED);
			return;
		}
		
		syncSpaceMechaInfo(protocol.getType());
	}
	
	/**
	 * 同步星甲召唤信息
	 * 
	 * @param protocol
	 */
	private void syncSpaceMechaInfo(int protocol) {
		String guildId = player.getGuildId();
		if (HawkOSOperator.isEmptyString(guildId)) {
			if (protocol > 0) {
				sendError(protocol, Status.Error.SPACE_MECHA_GUILD_NEED);
			}
			return;
		}
		
		SpaceMechaInfoPB.Builder builder = null;
		MechaSpaceInfo spaceObj = SpaceMechaService.getInstance().getGuildSpace(guildId);
		if (spaceObj == null || spaceObj.getStage() == null) {
			if (protocol > 0) {
				sendError(protocol, Status.Error.SPACE_MECHA_PLACED_NEED);
				return;
			}
			
			builder = SpaceMechaInfoPB.newBuilder();
			SapceMechaSummary.Builder summary = SapceMechaSummary.newBuilder();
			summary.setStage(SpaceMechaStage.SPACE_END);
			builder.setSummary(summary);
		} else {
			CustomDataEntity customData = SpaceMechaService.getInstance().getCustomDataEntity(player, MechaSpaceConst.PERSONAL_STRONGHOD_AWARD_TOTAL);
			builder = spaceObj.toBuilder();
			builder.getSummaryBuilder().setStrongHoldRewardTimes(customData.getValue());
			builder.getSummaryBuilder().setStrongHoldAtkTimes(SpaceMechaService.getInstance().getAtkStrongHoldTimesToday(player));
			spaceObj.getStage().buildStageInfo(builder);
		}
		
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.SPACE_MECHA_DETAIL_INFO_SYNC, builder));
	}
	
	/**
	 * 加入联盟
	 * 
	 * @param msg
	 * @return
	 */
	@MessageHandler
	private boolean onGuildJoinMsg(GuildJoinMsg msg) {
		if (!SpaceMechaService.getInstance().isActivityOpen()) {
			return false;
		}
		
		syncSpaceMechaInfo(0);
		return true;
	}
	
	/**
	 * 退出联盟
	 * 
	 * @return
	 */
	@MessageHandler
	private boolean onGuildQuitMsg(GuildQuitMsg msg) {
		if (!SpaceMechaService.getInstance().isActivityOpen()) {
			return false;
		}
		
		String guildId = msg.getGuildId();
		MechaSpaceInfo spaceObj = SpaceMechaService.getInstance().getGuildSpace(guildId);
		if (spaceObj == null || spaceObj.getStage() == null) {
			return true;
		}
		
		String playerId = player.getId();
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.SPACE_MECHA_QUIT_GUILD));
		WorldThreadScheduler.getInstance().postWorldTask(new WorldTask(GsConst.WorldTaskType.SPACE_MECHA_LEAVE_GUILD) {
			@Override
			public boolean onInvoke() {
				spaceObj.removeDefenceMember(playerId);
				spaceObj.forceAllSpaceMarchBack(playerId);
				BlockingQueue<IWorldMarch> marchs = WorldMarchService.getInstance().getPlayerMarch(playerId);
				Iterator<IWorldMarch> iterator = marchs.iterator();
				while (iterator.hasNext()) {
					IWorldMarch march = iterator.next();
					if (march.getMarchStatus() == WorldMarchStatus.MARCH_STATUS_RETURN_BACK_VALUE) {
						continue;
					}
					
					WorldMarchType marchType = march.getMarchType();
					// 单人行军
					if (marchType == WorldMarchType.SPACE_MECHA_BOX_COLLECT || marchType == WorldMarchType.SPACE_MECHA_SLAVE_MARCH_SINGLE || 
							marchType == WorldMarchType.SPACE_MECHA_MAIN_MARCH_SINGLE || marchType == WorldMarchType.SPACE_MECHA_ATK_STRONG_HOLD_SINGLE || 
							marchType == WorldMarchType.SPACE_MECHA_MAIN_MARCH_MASS || marchType == WorldMarchType.SPACE_MECHA_ATK_STRONG_HOLD_MASS || 
							marchType == WorldMarchType.SPACE_MECHA_MAIN_MARCH_MASS_JOIN || marchType == WorldMarchType.SPACE_MECHA_ATK_STRONG_HOLD_MASS_JOIN) {
						march.moveCityProcess(HawkTime.getMillisecond());
						WorldMarchService.getInstance().removeMarch(march);
					}
				}
				
				return true;
			}
		});
		
		return true;
	}
	
	/**
	 * 星甲召唤联盟官员号召盟友
	 * 
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code2.SPACE_MECHA_NOTICE_SEND_REQ_VALUE)
	private void onSpaceMechaNoticeReq(HawkProtocol protocol) {
		if (!SpaceMechaService.getInstance().isActivityOpen()) {
			sendError(protocol.getType(), Status.Error.SPACE_MECHA_ACTIVITY_CLOSED);
			return;
		}
		
		String guildId = player.getGuildId();
		if (HawkOSOperator.isEmptyString(guildId)) {
			sendError(protocol.getType(), Status.Error.SPACE_MECHA_GUILD_NEED);
			return;
		}
		
		MechaSpaceInfo spaceObj = SpaceMechaService.getInstance().getGuildSpace(guildId);
		if (spaceObj == null || spaceObj.getStage() == null) {
			sendError(protocol.getType(), Status.Error.SPACE_MECHA_PLACED_NEED);
			return;
		}
		
		// 是否是盟主或官员
		boolean guildAuthority = GuildService.getInstance().checkGuildAuthority(player.getId(), AuthId.SPACE_MECHA_NOTICE_AUTH);
		if (!guildAuthority) {
			sendError(protocol.getType(), Status.Error.SPACE_MECHA_PLACE_AUTH_ERROR);
			return;
		}
		
		// 当日是否已发过号召
		CustomDataEntity customData = player.getData().getCustomDataEntity(MechaSpaceConst.GUILD_OFFICER_NOTICE);
		if (customData == null){
			player.getData().createCustomDataEntity(MechaSpaceConst.GUILD_OFFICER_NOTICE, HawkTime.getSeconds(), "");
		} else if (!HawkTime.isSameDay(customData.getValue() * 1000L, HawkTime.getMillisecond())) {
			customData.setValue(HawkTime.getSeconds());
		} else {
			sendError(protocol.getType(), Status.Error.SPACE_MECHA_CALL_TODAY);
			return;
		}
		
		ChatService.getInstance().addWorldBroadcastMsg(ChatParames.newBuilder().setChatType(ChatType.CHAT_ALLIANCE).setKey(NoticeCfgId.SPACE_MECHA_OFFICER_CALL_MEMBER).setGuildId(player.getGuildId()).build());
		// 这里借用放置舱体的事件，都是向客户端同步最新的活动页面相关信息
		ActivityManager.getInstance().postEvent(new SpaceMechaPlaceEvent(player.getId()));
		player.responseSuccess(protocol.getType());
	}
	
	/**
	 * 联盟盟主或官员选择舱体等级
	 * 
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code2.SPACE_MECHA_SELECT_LEVEL_REQ_VALUE)
	private void onSpaceMechaSelectLevelReq(HawkProtocol protocol) {
		String guildId = player.getGuildId();
		if (HawkOSOperator.isEmptyString(guildId)) {
			sendError(protocol.getType(), Status.Error.SPACE_MECHA_GUILD_NEED);
			return;
		}
		
		// 是否是盟主或官员
		boolean guildAuthority = GuildService.getInstance().checkGuildAuthority(player.getId(), AuthId.SPACE_MECHA_NOTICE_AUTH);
		if (!guildAuthority) {
			sendError(protocol.getType(), Status.Error.SPACE_MECHA_PLACE_AUTH_ERROR);
			return;
		}
		
		SelectSpaceLevelReq req = protocol.parseProtocol(SelectSpaceLevelReq.getDefaultInstance());
		int selectLv = req.getLevel();
		GuildInfoObject guildInfoObj = GuildService.getInstance().getGuildInfoObject(guildId);
		int maxLevel = Math.max(1, guildInfoObj.getSpaceMaxLv());
		if (selectLv > maxLevel || selectLv < 1) {
			sendError(protocol.getType(), Status.SysError.PARAMS_INVALID_VALUE);
			return;
		}
		
		SpaceGuardActivity activity = SpaceMechaService.getInstance().getActivityObject(true);
		if (guildInfoObj.getSpaceMechaTermId() != activity.getActivityTermId()) {
			guildInfoObj.resetSpaceMechaData(activity.getActivityTermId());
		}
		
		WorldThreadScheduler.getInstance().postWorldTask(new WorldTask(GsConst.WorldTaskType.SPACE_MECHA_LV_SELECT) {
			@Override
			public boolean onInvoke() {
				guildInfoObj.setSpaceSelectedLv(selectLv);
				return true;
			}
		});
		
		HawkLog.logPrintln("spaceMecha select level success, playerId: {}, guildId: {}, selectLevel: {}, maxLevel: {}", player.getId(), guildId, selectLv, maxLevel);
		
		SelectSpaceLevelResp.Builder builder = SelectSpaceLevelResp.newBuilder();
		builder.setLevel(selectLv);
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.SPACE_MECHA_SELECT_LEVEL_RESP_VALUE, builder));
	}
	
	/**
	 * 请求舱体驻扎行军信息
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code2.SPACE_MECHA_QUARTER_REQ_VALUE)
	private boolean getMechaSpaceQuarterInfo(HawkProtocol protocol) {
		if (!player.hasGuild()) {
			return false;
		}
		
		MechaSpaceQuarterInfoReq req = protocol.parseProtocol(MechaSpaceQuarterInfoReq.getDefaultInstance());
		int posX = req.getPosX();
		int posY = req.getPosY();
		WorldPoint point = WorldPointService.getInstance().getWorldPoint(posX, posY);
		if (point == null) {
			return false;
		}
		
		if (!player.getGuildId().equals(point.getGuildId())) {
			return false;
		}
		
		if (point.getPointType() != WorldPointType.SPACE_MECHA_MAIN_VALUE && point.getPointType() != WorldPointType.SPACE_MECHA_SLAVE_VALUE) {
			return false;
		}
		
		MechaSpaceInfo spaceObj = SpaceMechaService.getInstance().getGuildSpace(player.getGuildId());
		if (spaceObj == null || spaceObj.getStage() == null || spaceObj.getStageVal() == SpaceMechaStage.SPACE_GUARD_4) {
			return false;
		}
		
		SpaceWorldPoint spacePoint = (SpaceWorldPoint) point;
		SpaceMechaService.getInstance().syncSpaceQuarterMarchInfo(player, spacePoint);
		return true;
	}
	
	
	
	/**
	 * 星甲召唤：舱体守卫单人行军
	 * 
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code2.SPACE_MECHA_SINGLE_MARCH_VALUE)
	protected boolean onSpaceMechaSingleMarch(HawkProtocol protocol) {
		if (!SpaceMechaService.getInstance().isActivityOpen()) {
			sendError(protocol.getType(), Status.Error.SPACE_MECHA_ACTIVITY_CLOSED);
			return false;
		}
		
		WorldMarchReq req = protocol.parseProtocol(WorldMarchReq.getDefaultInstance());
		WorldMarchType marchType = req.getType();
		// 跨服行军类型拦截
		if (WorldMarchService.getInstance().isCrossMarchLimit(player, marchType)) {
			sendError(protocol.getType(), Status.CrossServerError.CROSS_PROTOCOL_SHIELD);
			return false;
		}
		
		// 联盟检测
		if (!player.hasGuild()) {
			sendError(protocol.getType(), Status.Error.SPACE_MECHA_GUILD_NEED);
			return false;
		}
		
		WorldPoint point = WorldPointService.getInstance().getWorldPoint(req.getPosX(), req.getPosY());
		if (point == null) {
			sendError(protocol.getType(), Status.Error.WORLD_POINT_EMPTY_VALUE);
			return false;
		}
		
		if (point.getPointType() != WorldPointType.SPACE_MECHA_MAIN_VALUE && point.getPointType() != WorldPointType.SPACE_MECHA_SLAVE_VALUE) {
			sendError(protocol.getType(), Status.Error.SPACE_MECHA_MARCH_TYPE_ERROR);
			return false;
		}
		
		MechaSpaceInfo spaceObj = SpaceMechaService.getInstance().getGuildSpace(player.getGuildId());
		if (spaceObj == null || spaceObj.getStage() == null) {
			sendError(protocol.getType(), Status.Error.SPACE_MECHA_PLACED_NEED);
			return false;
		}
		
		SpaceWorldPoint spacePoint = (SpaceWorldPoint) point;
		if (spaceObj.getSpacePointId(spacePoint.getSpaceIndex()) != point.getId()) {
			sendError(protocol.getType(), Status.Error.SPACE_MECHA_POINT_OWNER_ERROR);
			return false;
		}
		
		Action action = Action.SPACE_MECHA_MAIN_SPACE_MARCH;
		if (point.getPointType() == WorldPointType.SPACE_MECHA_MAIN_VALUE) {
			if (marchType != WorldMarchType.SPACE_MECHA_MAIN_MARCH_SINGLE) {
				sendError(protocol.getType(), Status.Error.SPACE_MECHA_MARCH_TYPE_ERROR);
				return false;
			}
			
			if (spaceObj.getStageVal() == SpaceMechaStage.SPACE_GUARD_4) {
				sendError(protocol.getType(), Status.Error.SPACE_MECHA_STAGE_ERROR);
				return false;
			}
			
			List<IWorldMarch> marchs = WorldMarchService.getInstance().getPlayerMarch(player.getId(), WorldMarchType.SPACE_MECHA_MAIN_MARCH_MASS_VALUE);
			marchs.addAll(WorldMarchService.getInstance().getPlayerMarch(player.getId(), WorldMarchType.SPACE_MECHA_MAIN_MARCH_MASS_JOIN_VALUE));
			marchs.addAll(WorldMarchService.getInstance().getPlayerMarch(player.getId(), WorldMarchType.SPACE_MECHA_MAIN_MARCH_SINGLE_VALUE));
			if (!marchs.isEmpty()) {
				Optional<IWorldMarch> optional = marchs.stream().filter(e -> e.getMarchStatus() != WorldMarchStatus.MARCH_STATUS_RETURN_BACK_VALUE).findAny();
				if (optional.isPresent()) {
					sendError(protocol.getType(), Status.Error.SPACE_MECHA_MARCH_EXIST);
					return false;
				}
			}
		} else {
			if (marchType != WorldMarchType.SPACE_MECHA_SLAVE_MARCH_SINGLE) {
				sendError(protocol.getType(), Status.Error.SPACE_MECHA_MARCH_TYPE_ERROR);
				return false;
			}
			
			if (point.getId() != spaceObj.getSpacePointId(SpacePointIndex.SUB_SPACE_1) && point.getId() != spaceObj.getSpacePointId(SpacePointIndex.SUB_SPACE_2)) {
				sendError(protocol.getType(), Status.Error.SPACE_MECHA_MARCH_TYPE_ERROR);
				return false;
			}
			
			if (spaceObj.getStageVal() != SpaceMechaStage.SPACE_PREPARE && spaceObj.getStageVal() != SpaceMechaStage.SPACE_GUARD_1) {
				sendError(protocol.getType(), Status.Error.SPACE_MECHA_STAGE_ERROR);
				return false;
			}
			
			if (spacePoint.getSpaceBlood() <= 0) {
				sendError(protocol.getType(), Status.Error.SPACE_MECHA_BROKEN);
				return false;
			}
			
			Collection<IWorldMarch> alreadyMarchs = WorldMarchService.getInstance().getWorldPointMarch(spacePoint.getId());
			int alreadyCount = (int) alreadyMarchs.stream().filter(e -> e.getMarchStatus() != WorldMarchStatus.MARCH_STATUS_RETURN_BACK_VALUE).count();
			SpaceMechaSubcabinCfg cfg = SpaceMechaSubcabinCfg.getCfg(spaceObj.getLevel());
			if (alreadyCount >= cfg.getGuardLimit()) {
				sendError(protocol.getType(), Status.Error.SPACE_MECHA_POINT_MARCH_LIMIT);
				return false;
			}
			
			List<IWorldMarch> marchs = WorldMarchService.getInstance().getPlayerMarch(player.getId(), WorldMarchType.SPACE_MECHA_SLAVE_MARCH_SINGLE_VALUE);
			if (!marchs.isEmpty()) {
				Optional<IWorldMarch> optional = marchs.stream().filter(e -> e.getTerminalId() == spacePoint.getId() && e.getMarchStatus() != WorldMarchStatus.MARCH_STATUS_RETURN_BACK_VALUE).findAny();
				if (optional.isPresent()) {
					sendError(protocol.getType(), Status.Error.SPACE_MECHA_MARCH_EXIST);
					return false;
				}
			}
			
			action = Action.SPACE_MECHA_SLAVE_SPACE_MARCH;
		}
		
		// 带兵出征通用检查
		List<ArmyInfo> armyList = new ArrayList<ArmyInfo>();
		PlayerMarchModule module = player.getModule(GsConst.ModuleType.WORLD_MARCH_MODULE);
		if (!module.checkMarchReq(req, protocol.getType(), armyList, null)) {
			return false;
		}
		
		// 扣兵
		if (!ArmyService.getInstance().checkArmyAndMarch(player, armyList, req.getHeroIdList(), req.getSuperSoldierId())) {
			sendError(protocol.getType(), Status.Error.WORLD_MARCH_ARMY_COUNT);
			return false;
		}
		
		IWorldMarch march = WorldMarchService.getInstance().startMarch(player, marchType.getNumber(), point.getId(), player.getGuildId(), null, 0, new EffectParams(req, armyList));
		WorldMarchService.getInstance().addGuildMarch(march);
		HawkLog.logPrintln("spaceMecha start space defMarch, guildId: {}, playerId: {}, targetX: {}, targetY: {}", player.getGuildId(), player.getId(), point.getX(), point.getY());
		BehaviorLogger.log4Service(player, Source.WORLD_ACTION, action, Params.valueOf("marchData", march));
		return true;
	}
	
	/**
	 * 星甲召唤: 发起进攻据点行军
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code2.SPACE_MECHA_STRONGHOLD_SINGLE_MARCH_VALUE)
	protected boolean onSpaceMechaStrongHoldMarch(HawkProtocol protocol) {
		if (!SpaceMechaService.getInstance().isActivityOpen()) {
			sendError(protocol.getType(), Status.Error.SPACE_MECHA_ACTIVITY_CLOSED);
			return false;
		}
		
		WorldMarchReq req = protocol.parseProtocol(WorldMarchReq.getDefaultInstance());
		WorldMarchType marchType = req.getType();
		// 跨服行军类型拦截
		if (WorldMarchService.getInstance().isCrossMarchLimit(player, marchType)) {
			sendError(protocol.getType(), Status.CrossServerError.CROSS_PROTOCOL_SHIELD);
			return false;
		}
		
		// 联盟检测
		if (!player.hasGuild()) {
			sendError(protocol.getType(), Status.Error.SPACE_MECHA_GUILD_NEED);
			return false;
		}
		
		if (marchType != WorldMarchType.SPACE_MECHA_ATK_STRONG_HOLD_SINGLE) {
			sendError(protocol.getType(), Status.Error.SPACE_MECHA_MARCH_TYPE_ERROR);
			return false;
		}
		
		WorldPoint point = WorldPointService.getInstance().getWorldPoint(req.getPosX(), req.getPosY());
		if (point == null || point.getPointType() != WorldPointType.SPACE_MECHA_STRONG_HOLD_VALUE) {
			sendError(protocol.getType(), Status.Error.WORLD_POINT_INVALID_VALUE);
			return false;
		}
		
		StrongHoldWorldPoint strongHoldPoint = (StrongHoldWorldPoint) point;
		SpaceMechaStrongholdCfg cfg = HawkConfigManager.getInstance().getConfigByKey(SpaceMechaStrongholdCfg.class, strongHoldPoint.getStrongHoldId());
		if (cfg == null) {
			sendError(protocol.getType(), Status.Error.WORLD_POINT_INVALID_VALUE);
			return false;
		}
		
		MechaSpaceInfo spaceObj = SpaceMechaService.getInstance().getGuildSpace(player.getGuildId());
		if (spaceObj == null || spaceObj.getStage() == null) {
			sendError(protocol.getType(), Status.Error.SPACE_MECHA_PLACED_NEED);
			return false;
		}
		
		if (spaceObj.getStageVal() != SpaceMechaStage.SPACE_GUARD_2) {
			sendError(protocol.getType(), Status.Error.SPACE_MECHA_STAGE_ERROR);
			return false;
		}
		
		// 还要判断是不是自己联盟的据点
		if (!spaceObj.getEnemyPointIds().contains(point.getId()) || !player.getGuildId().equals(point.getGuildId())) {
			sendError(protocol.getType(), Status.Error.SPACE_MECHA_POINT_OWNER_ERROR);
			return false;
		}
		
		List<IWorldMarch> marchList = WorldMarchService.getInstance().getPlayerMarch(player.getId(), WorldMarchType.SPACE_MECHA_ATK_STRONG_HOLD_SINGLE_VALUE);
		marchList.addAll(WorldMarchService.getInstance().getPlayerMarch(player.getId(), WorldMarchType.SPACE_MECHA_ATK_STRONG_HOLD_MASS_VALUE));
		Optional<IWorldMarch> optional = marchList.stream().filter(e -> e.getTerminalId() == point.getId() && e.getMarchStatus() != WorldMarchStatus.MARCH_STATUS_RETURN_BACK_VALUE).findAny();
		if (optional.isPresent()) {
			sendError(protocol.getType(), Status.Error.SPACE_MECHA_MARCH_EXIST);
			return false;
		}
		
		List<IWorldMarch> marchs = WorldMarchService.getInstance().getPlayerMarch(player.getId(), WorldMarchType.SPACE_MECHA_ATK_STRONG_HOLD_MASS_JOIN_VALUE);
		for (IWorldMarch march : marchs) {
			if (march.getMarchStatus() == WorldMarchStatus.MARCH_STATUS_RETURN_BACK_VALUE) {
				continue;
			}
			String leaderPlayerId = march.getMarchEntity().getLeaderPlayerId();
			List<IWorldMarch> leaderMarchs = WorldMarchService.getInstance().getPlayerMarch(leaderPlayerId, WorldMarchType.SPACE_MECHA_ATK_STRONG_HOLD_MASS_VALUE);
			Optional<IWorldMarch> leaderOptional = leaderMarchs.stream().filter(e -> e.getTerminalId() == point.getId() && e.getMarchStatus() != WorldMarchStatus.MARCH_STATUS_RETURN_BACK_VALUE).findAny();
			if (leaderOptional.isPresent()) {
				sendError(protocol.getType(), Status.Error.SPACE_MECHA_MARCH_EXIST);
				return false;
			}
		}
		
		// 带兵出征通用检查
		List<ArmyInfo> armyList = new ArrayList<ArmyInfo>();
		PlayerMarchModule module = player.getModule(GsConst.ModuleType.WORLD_MARCH_MODULE);
		if (!module.checkMarchReq(req, protocol.getType(), armyList, null)) {
			return false;
		}
		
		// 扣兵
		if (!ArmyService.getInstance().checkArmyAndMarch(player, armyList, req.getHeroIdList(), req.getSuperSoldierId())) {
			sendError(protocol.getType(), Status.Error.WORLD_MARCH_ARMY_COUNT);
			return false;
		}
		
		String targetId = String.valueOf(strongHoldPoint.getStrongHoldId());
		IWorldMarch march = WorldMarchService.getInstance().startMarch(player, WorldMarchType.SPACE_MECHA_ATK_STRONG_HOLD_SINGLE_VALUE, point.getId(), targetId, null, 0, new EffectParams(req, armyList));
		WorldMarchService.getInstance().addGuildMarch(march);
		HawkLog.logPrintln("spaceMecha start attack stronghold march, guildId: {}, playerId: {}, targetX: {}, targetY: {}", player.getGuildId(), player.getId(), point.getX(), point.getY());
		BehaviorLogger.log4Service(player, Source.WORLD_ACTION, Action.SPACE_MECHA_ATK_STRONGHOLD_MARCH, Params.valueOf("marchData", march));
		return true;
	}
	
	/**
	 * 星甲召唤： 宝箱采集行军
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code2.SPACE_MECHA_BOX_COLLECT_MARCH_VALUE)
	protected boolean onSpaceMechaBoxCollectMarch(HawkProtocol protocol) {
		if (!SpaceMechaService.getInstance().isActivityOpen()) {
			sendError(protocol.getType(), Status.Error.SPACE_MECHA_ACTIVITY_CLOSED);
			return false;
		}
		
		WorldMarchReq req = protocol.parseProtocol(WorldMarchReq.getDefaultInstance());
		WorldMarchType marchType = WorldMarchType.SPACE_MECHA_BOX_COLLECT;
		// 跨服行军类型拦截
		if (WorldMarchService.getInstance().isCrossMarchLimit(player, marchType)) {
			sendError(protocol.getType(), Status.CrossServerError.CROSS_PROTOCOL_SHIELD);
			return false;
		}
		
		// 联盟检测
		if (!player.hasGuild()) {
			sendError(protocol.getType(), Status.Error.SPACE_MECHA_GUILD_NEED);
			return false;
		}
		
		WorldPoint point = WorldPointService.getInstance().getWorldPoint(req.getPosX(), req.getPosY());
		if (point == null || point.getPointType() != WorldPointType.SPACE_MECHA_BOX_VALUE) {
			sendError(protocol.getType(), Status.Error.WORLD_POINT_INVALID_VALUE);
			return false;
		}
		
		Set<IWorldMarch> marchs = WorldMarchService.getInstance().getPlayerTypeMarchs(player.getId(), marchType.getNumber());
		for (IWorldMarch march : marchs) {
			if (march.getMarchStatus() != WorldMarchStatus.MARCH_STATUS_RETURN_BACK_VALUE) {
				sendError(protocol.getType(), Status.Error.SPACE_MECHA_COLLECT_MARCH_ERROR);
				return false;
			}
		}
		
		SpaceMechaBoxCfg boxCfg = HawkConfigManager.getInstance().getConfigByKey(SpaceMechaBoxCfg.class, point.getResourceId());
		if (Objects.isNull(boxCfg)) {
			sendError(protocol.getType(), Status.Error.WORLD_POINT_INVALID_VALUE);
			return false;
		}
		
		// 有人在里面了
		if (!HawkOSOperator.isEmptyString(point.getMarchId())) {
			sendError(protocol.getType(), Status.Error.SPACE_MECHA_BOX_OCCUPIED);
			return false;
		}
		
		if (!player.getGuildId().equals(point.getGuildId())) {
			sendError(protocol.getType(), Status.Error.SPACE_MECHA_POINT_OWNER_ERROR);
			return false;
		}
		
		MechaSpaceInfo spaceObj = SpaceMechaService.getInstance().getGuildSpace(player.getGuildId());
		if (spaceObj == null || spaceObj.getStage() == null) {
			sendError(protocol.getType(), Status.Error.SPACE_MECHA_PLACED_NEED);
			return false;
		}
		
		if (spaceObj.getStageVal() != SpaceMechaStage.SPACE_GUARD_4) {
			sendError(protocol.getType(), Status.Error.SPACE_MECHA_STAGE_ERROR);
			return false;
		}
		
		CustomDataEntity customData = SpaceMechaService.getInstance().getCustomDataEntity(player, MechaSpaceConst.PERSONAL_BOX_AWARD_TOTAL);
		SpaceMechaConstCfg constCfg = HawkConfigManager.getInstance().getKVInstance(SpaceMechaConstCfg.class);
		if (customData.getValue() >= constCfg.getBoxAwardLimit()) {
			player.sendError(protocol.getType(), Status.Error.SPACE_MECHA_COLLECT_LIMIT, 0);
			return false;
		}
		
		// 带兵出征通用检查
		List<ArmyInfo> armyList = new ArrayList<ArmyInfo>();
		PlayerMarchModule module = player.getModule(GsConst.ModuleType.WORLD_MARCH_MODULE);
		if (!module.checkMarchReq(req, protocol.getType(), armyList, null)) {
			return false;
		}
		
		// 扣兵
		if (!ArmyService.getInstance().checkArmyAndMarch(player, armyList, req.getHeroIdList(), req.getSuperSoldierId())) {
			sendError(protocol.getType(), Status.Error.WORLD_MARCH_ARMY_COUNT);
			return false;
		}
		
		IWorldMarch march = WorldMarchService.getInstance().startMarch(player, marchType.getNumber(), point.getId(), String.valueOf(point.getResourceId()), null, 0, new EffectParams(req, armyList));
		HawkLog.logPrintln("spaceMecha start collect march, guildId: {}, playerId: {}, targetX: {}, targetY: {}", player.getGuildId(), player.getId(), point.getX(), point.getY());
		BehaviorLogger.log4Service(player, Source.WORLD_ACTION, Action.SPACE_MECHA_COLLECT_BOX_MARCH, Params.valueOf("marchData", march));
		return true;
	}
	
	/**
	 * 主舱集结判断
	 * @param protocol
	 * @param point
	 * @return
	 */
	public boolean mainSpaceMassCheck(HawkProtocol protocol, WorldPoint point) {
		if (!SpaceMechaService.getInstance().isActivityOpen()) {
			sendError(protocol.getType(), Status.Error.SPACE_MECHA_ACTIVITY_CLOSED);
			return false;
		}
		if (point.getPointType() != WorldPointType.SPACE_MECHA_MAIN_VALUE) {
			sendError(protocol.getType(), Status.Error.SPACE_MECHA_MARCH_TYPE_ERROR);
			return false;
		}
		MechaSpaceInfo spaceObj = SpaceMechaService.getInstance().getGuildSpace(player.getGuildId());
		if (spaceObj == null || spaceObj.getStage() == null) {
			sendError(protocol.getType(), Status.Error.SPACE_MECHA_PLACED_NEED);
			return false;
		}
		
		if (spaceObj.getStageVal() == SpaceMechaStage.SPACE_GUARD_4) {
			sendError(protocol.getType(), Status.Error.SPACE_MECHA_STAGE_ERROR);
			return false;
		}
		
		SpaceWorldPoint spacePoint = (SpaceWorldPoint) point;
		if (!spacePoint.getGuildId().equals(player.getGuildId())) {
			sendError(protocol.getType(), Status.Error.SPACE_MECHA_POINT_OWNER_ERROR);
			return false;
		}
		
		List<IWorldMarch> marchs = WorldMarchService.getInstance().getPlayerMarch(player.getId(), WorldMarchType.SPACE_MECHA_MAIN_MARCH_MASS_VALUE);
		marchs.addAll(WorldMarchService.getInstance().getPlayerMarch(player.getId(), WorldMarchType.SPACE_MECHA_MAIN_MARCH_MASS_JOIN_VALUE));
		marchs.addAll(WorldMarchService.getInstance().getPlayerMarch(player.getId(), WorldMarchType.SPACE_MECHA_MAIN_MARCH_SINGLE_VALUE));
		if (!marchs.isEmpty()) {
			Optional<IWorldMarch> optional = marchs.stream().filter(e -> e.getMarchStatus() != WorldMarchStatus.MARCH_STATUS_RETURN_BACK_VALUE).findAny();
			if (optional.isPresent()) {
				sendError(protocol.getType(), Status.Error.SPACE_MECHA_MARCH_EXIST);
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * 据点集结判断
	 * 
	 * @param protocol
	 * @param point
	 * @return
	 */
	public boolean strongholdMassCheck(HawkProtocol protocol, WorldPoint point) {
		if (!SpaceMechaService.getInstance().isActivityOpen()) {
			sendError(protocol.getType(), Status.Error.SPACE_MECHA_ACTIVITY_CLOSED);
			return false;
		}
		if (point.getPointType() != WorldPointType.SPACE_MECHA_STRONG_HOLD_VALUE) {
			sendError(protocol.getType(), Status.Error.SPACE_MECHA_MARCH_TYPE_ERROR);
			return false;
		}
		MechaSpaceInfo spaceObj = SpaceMechaService.getInstance().getGuildSpace(player.getGuildId());
		if (spaceObj == null || spaceObj.getStage() == null) {
			sendError(protocol.getType(), Status.Error.SPACE_MECHA_PLACED_NEED);
			return false;
		}
		
		if (spaceObj.getStageVal() != SpaceMechaStage.SPACE_GUARD_2) {
			sendError(protocol.getType(), Status.Error.SPACE_MECHA_STAGE_ERROR);
			return false;
		}
		
		StrongHoldWorldPoint worldPoint = (StrongHoldWorldPoint) point;
		if (!worldPoint.getGuildId().equals(player.getGuildId())) {
			sendError(protocol.getType(), Status.Error.SPACE_MECHA_POINT_OWNER_ERROR);
			return false;
		}
		
		List<IWorldMarch> marchList = WorldMarchService.getInstance().getPlayerMarch(player.getId(), WorldMarchType.SPACE_MECHA_ATK_STRONG_HOLD_SINGLE_VALUE);
		marchList.addAll(WorldMarchService.getInstance().getPlayerMarch(player.getId(), WorldMarchType.SPACE_MECHA_ATK_STRONG_HOLD_MASS_VALUE));
		Optional<IWorldMarch> optional = marchList.stream().filter(e -> e.getTerminalId() == point.getId() && e.getMarchStatus() != WorldMarchStatus.MARCH_STATUS_RETURN_BACK_VALUE).findAny();
		if (optional.isPresent()) {
			sendError(protocol.getType(), Status.Error.SPACE_MECHA_MARCH_EXIST);
			return false;
		}
		
		List<IWorldMarch> marchs = WorldMarchService.getInstance().getPlayerMarch(player.getId(), WorldMarchType.SPACE_MECHA_ATK_STRONG_HOLD_MASS_JOIN_VALUE);
		for (IWorldMarch march : marchs) {
			String leaderId = march.getMarchEntity().getLeaderPlayerId();
			List<IWorldMarch> leaderMarchs = WorldMarchService.getInstance().getPlayerMarch(leaderId, WorldMarchType.SPACE_MECHA_ATK_STRONG_HOLD_MASS_VALUE);
			Optional<IWorldMarch> leaderOptional = leaderMarchs.stream().filter(e -> e.getTerminalId() == point.getId() && e.getMarchStatus() != WorldMarchStatus.MARCH_STATUS_RETURN_BACK_VALUE).findAny();
			if (leaderOptional.isPresent()) {
				sendError(protocol.getType(), Status.Error.SPACE_MECHA_MARCH_EXIST);
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * 主舱集结加入判断
	 * @param protocol
	 * @return
	 */
	public boolean mainSpaceMassJoinCheck(HawkProtocol protocol) {
		List<IWorldMarch> marchs = WorldMarchService.getInstance().getPlayerMarch(player.getId(), WorldMarchType.SPACE_MECHA_MAIN_MARCH_MASS_VALUE);
		marchs.addAll(WorldMarchService.getInstance().getPlayerMarch(player.getId(), WorldMarchType.SPACE_MECHA_MAIN_MARCH_MASS_JOIN_VALUE));
		marchs.addAll(WorldMarchService.getInstance().getPlayerMarch(player.getId(), WorldMarchType.SPACE_MECHA_MAIN_MARCH_SINGLE_VALUE));
		if (!marchs.isEmpty()) {
			Optional<IWorldMarch> optional = marchs.stream().filter(e -> e.getMarchStatus() != WorldMarchStatus.MARCH_STATUS_RETURN_BACK_VALUE).findAny();
			if (optional.isPresent()) {
				sendError(protocol.getType(), Status.Error.SPACE_MECHA_MARCH_EXIST);
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * 据点集结加入判断
	 * @param protocol
	 * @param point
	 * @return
	 */
	public boolean strongholdMassJoinCheck(HawkProtocol protocol, WorldPoint point) {
		List<IWorldMarch> marchList = WorldMarchService.getInstance().getPlayerMarch(player.getId(), WorldMarchType.SPACE_MECHA_ATK_STRONG_HOLD_SINGLE_VALUE);
		marchList.addAll(WorldMarchService.getInstance().getPlayerMarch(player.getId(), WorldMarchType.SPACE_MECHA_ATK_STRONG_HOLD_MASS_VALUE));
		Optional<IWorldMarch> optional = marchList.stream().filter(e -> e.getTerminalId() == point.getId() && e.getMarchStatus() != WorldMarchStatus.MARCH_STATUS_RETURN_BACK_VALUE).findAny();
		if (optional.isPresent()) {
			sendError(protocol.getType(), Status.Error.SPACE_MECHA_MARCH_EXIST);
			return false;
		}
		
		List<IWorldMarch> marchs = WorldMarchService.getInstance().getPlayerMarch(player.getId(), WorldMarchType.SPACE_MECHA_ATK_STRONG_HOLD_MASS_JOIN_VALUE);
		for (IWorldMarch march : marchs) {
			if (march.getMarchStatus() == WorldMarchStatus.MARCH_STATUS_RETURN_BACK_VALUE) {
				continue;
			}
			String leaderPlayerId = march.getMarchEntity().getLeaderPlayerId();
			List<IWorldMarch> leaderMarchs = WorldMarchService.getInstance().getPlayerMarch(leaderPlayerId, WorldMarchType.SPACE_MECHA_ATK_STRONG_HOLD_MASS_VALUE);
			Optional<IWorldMarch> leaderOptional = leaderMarchs.stream().filter(e -> e.getTerminalId() == point.getId() && e.getMarchStatus() != WorldMarchStatus.MARCH_STATUS_RETURN_BACK_VALUE).findAny();
			if (leaderOptional.isPresent()) {
				sendError(protocol.getType(), Status.Error.SPACE_MECHA_MARCH_EXIST);
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * 发送集结通告
	 * 
	 * @param point
	 * @param march
	 * @param leaderPosX
	 * @param leaderPosY
	 */
	public void sendMassNotice(WorldPoint point, IWorldMarch march, String leaderPosX, String leaderPosY) {
		if (point.getPointType() == WorldPointType.SPACE_MECHA_MAIN_VALUE) {
			SpaceWorldPoint spacePoint = (SpaceWorldPoint) point;
			SpaceMechaCabinCfg cfg = SpaceMechaCabinCfg.getCfgByLevel(spacePoint.getSpaceLevel());
			Object[] objects = new Object[] { cfg.getId(), point.getX(), point.getY(), march.getMarchId(), leaderPosX, leaderPosY };
			ChatService.getInstance().addWorldBroadcastMsg(ChatParames.newBuilder().setChatType(ChatType.CHAT_ALLIANCE).setKey(NoticeCfgId.SPACE_MECHA_MAIN_SPACE_MASS).setGuildId(player.getGuildId()).setPlayer(player).addParms(objects).build());
		} else if (point.getPointType() == WorldPointType.SPACE_MECHA_STRONG_HOLD_VALUE) {
			StrongHoldWorldPoint strongPoint = (StrongHoldWorldPoint) point;
			Object[] objects = new Object[] { strongPoint.getStrongHoldId(), point.getX(), point.getY(), march.getMarchId(), leaderPosX, leaderPosY };
			ChatService.getInstance().addWorldBroadcastMsg(ChatParames.newBuilder().setChatType(ChatType.CHAT_ALLIANCE).setKey(NoticeCfgId.SPACE_MECHA_STRONGHOLD_MASS).setGuildId(player.getGuildId()).setPlayer(player).addParms(objects).build());
		}
	}
	
}
