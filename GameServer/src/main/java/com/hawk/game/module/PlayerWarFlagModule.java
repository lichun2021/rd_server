package com.hawk.game.module;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.type.impl.guildbanner.GuildBannerActivity;
import com.hawk.game.config.WarFlagConstProperty;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.item.WarFlagSignUpItem;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerModule;
import com.hawk.game.protocol.Activity.ActivityType;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.WarFlag.CenterFlagInfoReq;
import com.hawk.game.protocol.WarFlag.CenterFlagSignUp;
import com.hawk.game.protocol.WarFlag.CenterFlagUnSignUp;
import com.hawk.game.protocol.WarFlag.Flag;
import com.hawk.game.protocol.WarFlag.FlagMapGuildInfoReq;
import com.hawk.game.protocol.WarFlag.FlagMapGuildInfoResp;
import com.hawk.game.protocol.WarFlag.FlagQuarterInfoReq;
import com.hawk.game.protocol.WarFlag.FlagResource;
import com.hawk.game.protocol.WarFlag.FlagResourceCountResp;
import com.hawk.game.protocol.WarFlag.FlageState;
import com.hawk.game.protocol.WarFlag.PlaceWarFlagReq;
import com.hawk.game.protocol.WarFlag.SyncWarFlagList;
import com.hawk.game.protocol.WarFlag.TakeBackFlagReq;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.WarFlagService;
import com.hawk.game.service.flag.FlagCollection;
import com.hawk.game.service.warFlag.IFlag;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.service.WorldPointService;
import com.hawk.game.world.thread.WorldTask;
import com.hawk.game.world.thread.WorldThreadScheduler;

/**
 * 战地之王
 * @author golden
 *
 */
public class PlayerWarFlagModule extends PlayerModule {

	protected static Logger logger = LoggerFactory.getLogger("Server");
	
	/**
	 * 上次检测时间
	 */
	private long lastCheckTime = 0L;
	
	/**
	 * 构造
	 * @param player
	 */
	public PlayerWarFlagModule(Player player) {
		super(player);
	}

	@Override
	public boolean onTick() {
		if (lastCheckTime == 0L) {
			lastCheckTime = HawkTime.getMillisecond();
		}
		
		if (HawkTime.getMillisecond() - lastCheckTime < 300000) {
			return true;
		}
		
		lastCheckTime = HawkTime.getMillisecond();
		
		WarFlagService.getInstance().pushRedPoint(player);
		return true;
	}
	
	@Override
	protected boolean onPlayerLogin() {
		
		if (!player.hasGuild()) {
			return true;
		}
		
		// 检测联盟旗帜数量
		WorldThreadScheduler.getInstance().postWorldTask(new WorldTask(GsConst.WorldTaskType.CHECK_WAR_FLAG_COUNT) {
			@Override
			public boolean onInvoke() {
				WarFlagService.getInstance().checkWarFlagCount(player.getGuildId());
				return false;
			}
		});
		
		WarFlagService.getInstance().pushRedPoint(player);
		return true;
	}
	
	/**
	 * 旗帜列表
	 */
	@ProtocolHandler(code = HP.code.WAR_FLAG_LIST_REQ_VALUE)
	private void warFlagListC(HawkProtocol protocol) {
		SyncWarFlagList.Builder list = SyncWarFlagList.newBuilder();
		
		if (player.hasGuild()) {
			Set<String> flagIds = new HashSet<>();
			flagIds.addAll(FlagCollection.getInstance().getOwnerFlagIds(player.getGuildId()));
			flagIds.addAll(FlagCollection.getInstance().getCurrFlagIds(player.getGuildId()));
			flagIds.addAll(FlagCollection.getInstance().getCenterFlagIds(player.getGuildId()));
			
			for (String flagId : flagIds) {
				IFlag flag = FlagCollection.getInstance().getFlag(flagId);
				
				Flag.Builder flagBuilder = Flag.newBuilder();
				flagBuilder.setFlagId(flag.getFlagId());
				flagBuilder.setState(FlageState.valueOf(flag.getState()));
				if (FlageState.valueOf(flag.getState()) == FlageState.FLAG_DEFEND
						&& !flag.getCurrentId().equals(player.getGuildId())) {
					flagBuilder.setState(FlageState.FLAG_BEINVADED);	
				}
				int[] pos = GameUtil.splitXAndY(flag.getPointId());
				flagBuilder.setX(pos[0]);
				flagBuilder.setY(pos[1]);
				flagBuilder.setLife(flag.getLife());
				flagBuilder.setOwnerId(flag.getOwnerId());
				flagBuilder.setOccupyId(flag.getCurrentId());
				
				String guildTag = GuildService.getInstance().getGuildTag(flag.getOwnerId());
				guildTag = (guildTag == null) ? "" : guildTag;
				flagBuilder.setOwnerTag(guildTag);
				flagBuilder.setOwnerIndex(flag.getOwnIndex());
				flagBuilder.setOccupyLife(flag.getOccupyLife());				
				flagBuilder.setIsCenter(flag.isCenter());
				
				list.addWarFlag(flagBuilder);
				
			}
		}
		
		player.sendProtocol(HawkProtocol.valueOf(HP.code.WAR_FLAG_LIST_RESP, list));
	}
	
	/**
	 * 放置旗帜
	 */
	@ProtocolHandler(code = HP.code.PLACE_WAR_FLAG_REQ_VALUE)
	private void placeWarFlag(HawkProtocol protocol) {
		PlaceWarFlagReq req = protocol.parseProtocol(PlaceWarFlagReq.getDefaultInstance());
		
		WorldThreadScheduler.getInstance().postWorldTask(new WorldTask(GsConst.WorldTaskType.PLACE_WAR_FLAG) {
			@Override
			public boolean onInvoke() {
				if (!WarFlagService.getInstance().canWarFlagPlace(player, req.getX(), req.getY(), req.getFlagId(), protocol.getType())) {
					return false;
				}
				
				WarFlagService.getInstance().placeWarFlag(player, req.getX(), req.getY(), req.getFlagId());
				
				player.responseSuccess(protocol.getType());
				return true;
			}
		});
	}
	
	/**
	 * 收回旗帜
	 */
	@ProtocolHandler(code = HP.code.TAKE_BACK_WAR_FLAG_REQ_VALUE)
	private void TakeBackWarFlag(HawkProtocol protocol) {
		TakeBackFlagReq req = protocol.parseProtocol(TakeBackFlagReq.getDefaultInstance());
		if (!WarFlagService.getInstance().canWarFlagTakeBack(protocol.getType(), player, req.getFlagId())) {
			return;
		}
		
		long removeFlagTime = WarFlagConstProperty.getInstance().getRemoveFlagTime();
		IFlag flag = FlagCollection.getInstance().getFlag(req.getFlagId());
		if (flag.getRemoveTime() > 0 && flag.getRemoveTime() > HawkTime.getMillisecond()) {
			return;
		}
		
		flag.setRemoveTime(HawkTime.getMillisecond() + removeFlagTime);
		
		player.responseSuccess(protocol.getType());
	}
	
	/**
	 * 收取战旗资源
	 */
	@ProtocolHandler(code = HP.code.COLLECT_FLAG_RESOURCE_REQ_VALUE)
	public void collectFlagResource(HawkProtocol protocol) {
		WarFlagService.getInstance().collectFlaResource(player);
		player.responseSuccess(protocol.getType());
	}
	
	/**
	 * 获取当前旗帜资源数量
	 */
	@ProtocolHandler(code = HP.code.FLAG_RESOURCE_COUNT_REQ_VALUE)
	public void flagResourceCountReq(HawkProtocol protocol) {
		FlagResourceCountResp.Builder resp = FlagResourceCountResp.newBuilder();
		
		List<ItemInfo> resource = WarFlagService.getInstance().getPlayerFlagResource(player.getId());
		for (ItemInfo item : resource) {
			FlagResource.Builder builder = FlagResource.newBuilder();
			builder.setId(item.getItemId());
			builder.setCount((int)item.getCount());
			resp.addResource(builder);
		}
		
		player.sendProtocol(HawkProtocol.valueOf(HP.code.FLAG_RESOURCE_COUNT_RESP, resp));
	}
	
	/**
	 * 推送战旗格局 
	 */
	@ProtocolHandler(code = HP.code.FLAG_PATTERN_REQ_VALUE)
	public void getFlagPattner(HawkProtocol protocol) {
		WarFlagService.getInstance().pushFlagPatternInfo(player);
	}
	
	/**
	 * 获取旗帜驻军信息
	 */
	@ProtocolHandler(code = HP.code.FLAG_QUARTER_INFO_REQ_VALUE)
	public void getFlagQuarterInfo(HawkProtocol protocol) {
		FlagQuarterInfoReq req = protocol.parseProtocol(FlagQuarterInfoReq.getDefaultInstance());
		WarFlagService.getInstance().pushFlagQuarterInfo(player, req.getFlagId());
	}
	
	/**
	 * 二级地图点击联盟信息
	 */
	@ProtocolHandler(code = HP.code.FLAG_MAP_GUILD_INFO_REQ_VALUE)
	public void flagMapGuildInfoReq(HawkProtocol protocol) {
		FlagMapGuildInfoReq req = protocol.parseProtocol(FlagMapGuildInfoReq.getDefaultInstance());
		
		String guildId = null;
		
		int flagMapRatio = WarFlagConstProperty.getInstance().getFlagMapRatio();
		for (int posX = req.getPosX() * flagMapRatio; posX < (req.getPosX() + 1) * flagMapRatio; posX++) {
			for (int posY = req.getPosY() * flagMapRatio; posY < (req.getPosY() + 1) * flagMapRatio; posY++) {
				WorldPoint worldPoint = WorldPointService.getInstance().getWorldPoint(posX, posY);
				if (worldPoint == null) {
					continue;
				}
				if (worldPoint.getPointType() != WorldPointType.WAR_FLAG_POINT_VALUE) {
					continue;
				}
				IFlag flag = FlagCollection.getInstance().getFlag(worldPoint.getGuildBuildId());
				if (flag == null) {
					continue;
				}
				guildId = flag.getCurrentId();
			}
		}
		
		FlagMapGuildInfoResp.Builder resp = FlagMapGuildInfoResp.newBuilder();
		if (!HawkOSOperator.isEmptyString(guildId)) {
			int rank = 0;
			Optional<ActivityBase> activity = ActivityManager.getInstance().getGameActivityByType(ActivityType.GUILD_BANNER_VALUE);
			if (activity.isPresent()) {
				GuildBannerActivity acti = (GuildBannerActivity)activity.get();
				rank = acti.getGuildRank(guildId);
			}
			resp.setRank(rank);
			
			int flagCount = 0;
			int compCount = 0;
			
			for (String flagId : FlagCollection.getInstance().getCurrFlagIds(guildId)) {
				IFlag flag = FlagCollection.getInstance().getFlag(flagId);
				if (flag.getState() == FlageState.FLAG_LOCKED_VALUE || flag.getState() == FlageState.FLAG_UNLOCKED_VALUE) {
					continue;
				}
				
				if (flag.getState() != FlageState.FLAG_BUILDING_VALUE && flag.getState() != FlageState.FLAG_PLACED_VALUE) {
					compCount++;
				}
				
				flagCount++;
			}
			
			resp.setFlagCount(flagCount);
			resp.setCompCount(compCount);
			
			String guildName = GuildService.getInstance().getGuildName(guildId);
			resp.setGuildName(HawkOSOperator.isEmptyString(guildName) ? "" : guildName);
			
			String guildTag = GuildService.getInstance().getGuildTag(guildId);
			resp.setGuildTag(HawkOSOperator.isEmptyString(guildTag) ? "" : guildTag);
			resp.setCompCenterCount(FlagCollection.getInstance().getCompCenterCount(guildId));
		} else {
			resp.setRank(0);
			resp.setFlagCount(0);
			resp.setGuildName("");
			resp.setGuildTag("");
		}

		resp.setPosX(req.getPosX());
		resp.setPosY(req.getPosY());
		player.sendProtocol(HawkProtocol.valueOf(HP.code.FLAG_MAP_GUILD_INFO_RESP, resp));
	}
	
	/**
	 * 母旗信息
	 */
	@ProtocolHandler(code = HP.code.FLAG_CENTER_INFO_REQ_VALUE)
	public void getCenterFlagInfo(HawkProtocol protocol) {
		WorldThreadScheduler.getInstance().postWorldTask(new WorldTask(GsConst.WorldTaskType.CENTER_FLAG_ACTION) {
			@Override
			public boolean onInvoke() {
				CenterFlagInfoReq req = protocol.parseProtocol(CenterFlagInfoReq.getDefaultInstance());
				String flagId = req.getFlagId();
				
				IFlag flag = FlagCollection.getInstance().getFlag(flagId);
				
				// 不是母旗
				if (flag == null || !flag.isCenter()) {
					return true;
				}
				
				// 联盟校验
//				if (!player.hasGuild() || !player.getGuildId().equals(flag.getOwnerId())) {
//					return true;
//				}
				
				WarFlagService.getInstance().syncCenterFlagInfo(player, flagId);
				
				return true;
			}
		});
	}
	
	/**
	 * 母旗报名
	 */
	@ProtocolHandler(code = HP.code.FLAG_CENTER_SIGNUP_REQ_VALUE)
	public void getCenterFlagSignUp(HawkProtocol protocol) {
		
		// TODO 母旗状态检测
		WorldThreadScheduler.getInstance().postWorldTask(new WorldTask(GsConst.WorldTaskType.CENTER_FLAG_ACTION) {
			@Override
			public boolean onInvoke() {
				
				CenterFlagSignUp req = protocol.parseProtocol(CenterFlagSignUp.getDefaultInstance());
				
				String flagId = req.getFlagId();
				
				IFlag flag = FlagCollection.getInstance().getFlag(flagId);
				
				// 不是母旗
				if (flag == null || !flag.isCenter()) {
					sendError(protocol.getType(), Status.WarFlagError.CENTER_FLAG_SINGUP_NOT_CENTER_VALUE);
					return true;
				}
				
				// 联盟校验
				if (!player.hasGuild() || !player.getGuildId().equals(flag.getOwnerId())) {
					sendError(protocol.getType(), Status.WarFlagError.CENTER_FLAG_SINGUP_GUILD_ERROR_VALUE);
					return true;
				}
				
				// 已经报名过,不能重复报名
				Map<String, WarFlagSignUpItem> signUpSet = flag.getSignUpInfos();
				if (signUpSet.containsKey(player.getId())) {
					sendError(protocol.getType(), Status.WarFlagError.CENTER_FLAG_SINGUP_ALREADY_VALUE);
					return true;
				}
				
				// 报名格子已满
				if (signUpSet.size() >= WarFlagConstProperty.getInstance().getBigFlagCells()) {
					WarFlagService.getInstance().syncCenterFlagInfo(player, flagId);
					sendError(protocol.getType(), Status.WarFlagError.CENTER_FLAG_SINGUP_COUNT_LIMIT_VALUE);
					return true;
				}
				
				// 报名
				flag.signUp(WarFlagSignUpItem.valueOf(player.getId()));
				
				// 通用成功返回
				player.responseSuccess(protocol.getType());
				
				// 推送界面信息
				WarFlagService.getInstance().syncCenterFlagInfo(player, flagId);
				
				// 日志
				logger.info("centerFlagSignUp, playerId:{}, flagId:{}", player.getId(), flagId);
				
				return true;
			}
		});
	}
	
	/**
	 * 母旗取消报名
	 */
	@ProtocolHandler(code = HP.code.FLAG_CENTER_LEAVE_REQ_VALUE)
	public void getCenterFlagUndoSignUp(HawkProtocol protocol) {
		
		WorldThreadScheduler.getInstance().postWorldTask(new WorldTask(GsConst.WorldTaskType.CENTER_FLAG_ACTION) {
			@Override
			public boolean onInvoke() {
				
				CenterFlagUnSignUp req = protocol.parseProtocol(CenterFlagUnSignUp.getDefaultInstance());
				
				String flagId = req.getFlagId();
				
				IFlag flag = FlagCollection.getInstance().getFlag(flagId);
				
				// 不是母旗
				if (flag == null || !flag.isCenter()) {
					sendError(protocol.getType(), Status.WarFlagError.CENTER_FLAG_SINGUP_NOT_CENTER_VALUE);
					return true;
				}
				
				// 敌方占领中不能取消报名
				if (flag.getState() == FlageState.FLAG_BEINVADED_VALUE) {
					sendError(protocol.getType(), Status.WarFlagError.CENTER_FLAG_SINGUP_BE_OCCUPY_VALUE);
					return true;
				}
				
				// 拆除中，不能取消报名
				if (flag.getRemoveTime() > 0) {
					sendError(protocol.getType(), Status.WarFlagError.CENTER_FLAG_SINGUP_BE_REMOVE_VALUE);
					return true;
				}
				
				// 联盟校验
				if (!player.hasGuild() || !player.getGuildId().equals(flag.getOwnerId())) {
					sendError(protocol.getType(), Status.WarFlagError.CENTER_FLAG_SINGUP_GUILD_ERROR_VALUE);
					return true;
				}
				
				// 没有报名过
				Map<String, WarFlagSignUpItem> signUpSet = flag.getSignUpInfos();
				if (!signUpSet.containsKey(player.getId())) {
					player.responseSuccess(protocol.getType());
					WarFlagService.getInstance().syncCenterFlagInfo(player, flagId);
					return true;
				}
				
				// 取消报名
				flag.rmSignUpInfo(player.getId());
				
				// 通用成功返回
				player.responseSuccess(protocol.getType());
				
				// 推送界面信息
				WarFlagService.getInstance().syncCenterFlagInfo(player, flagId);
				
				// 日志
				logger.info("centerFlagUndoSignUp, playerId:{}, flagId:{}", player.getId(), flagId);
				
				return true;
			}
		});
	}
	
	/**
	 * 母旗取消报名
	 */
	@ProtocolHandler(code = HP.code.FLAG_CENTER_REMOVE_REQ_VALUE)
	public void getCenterFlagRemoveSignUp(HawkProtocol protocol) {
		
		// 暂时移除取消报名
//		WorldThreadScheduler.getInstance().postWorldTask(new WorldTask(GsConst.WorldTaskType.CENTER_FLAG_ACTION) {
//			@Override
//			public boolean onInvoke() {
//				
//				CenterFlagRmoveSignUp req = protocol.parseProtocol(CenterFlagRmoveSignUp.getDefaultInstance());
//				
//				String flagId = req.getFlagId();
//				String playerId = req.getPlayerId();
//				
//				IFlag flag = FlagCollection.getInstance().getFlag(flagId);
//				
//				// TODO 权限检测
//				
//				// 不是母旗
//				if (flag == null || !flag.isCenter()) {
//					sendError(protocol.getType(), Status.WarFlagError.CENTER_FLAG_SINGUP_NOT_CENTER_VALUE);
//					return true;
//				}
//				
//				// 联盟校验
//				if (!player.hasGuild() || !player.getGuildId().equals(flag.getOwnerId())) {
//					sendError(protocol.getType(), Status.WarFlagError.CENTER_FLAG_SINGUP_GUILD_ERROR_VALUE);
//					return true;
//				}
//				
//				// 没有报名过
//				Map<String, WarFlagSignUpItem> signUpSet = flag.getSignUpInfos();
//				if (!signUpSet.containsKey(playerId)) {
//					player.responseSuccess(protocol.getType());
//					WarFlagService.getInstance().syncCenterFlagInfo(player, flagId);
//					return true;
//				}
//				
//				// 取消报名
//				flag.rmSignUpInfo(playerId);
//				
//				// 通用成功返回
//				player.responseSuccess(protocol.getType());
//				
//				// 推送界面信息
//				WarFlagService.getInstance().syncCenterFlagInfo(player, flagId);
//				
//				// 日志
//				logger.info("centerFlagRemoveSignUp, playerId:{}, bePlayerId:{}, flagId:{}", player.getId(), playerId, flagId);
//				
//				return true;
//			}
//		});
	}
}
