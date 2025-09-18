
package com.hawk.game.module;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.task.HawkTaskManager;
import org.hawk.thread.HawkTask;
import org.hawk.thread.HawkThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hawk.game.config.PresidentConstCfg;
import com.hawk.game.config.SuperWeaponAwardCfg;
import com.hawk.game.config.SuperWeaponConstCfg;
import com.hawk.game.config.SuperWeaponSpecialAwardCfg;
import com.hawk.game.crossfortress.CrossFortressService;
import com.hawk.game.crossfortress.IFortress;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.LocalRedis;
import com.hawk.game.module.schedule.ScheduleService;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerModule;
import com.hawk.game.protocol.GuildManager.AuthId;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.SuperWeapon.SWAllGiftReceiveCountResp;
import com.hawk.game.protocol.SuperWeapon.SWCancelSignUp;
import com.hawk.game.protocol.SuperWeapon.SWGiftReceiveCountReq;
import com.hawk.game.protocol.SuperWeapon.SWGiftReceiveCountResp;
import com.hawk.game.protocol.SuperWeapon.SuperWeaponDetailFightRecodReq;
import com.hawk.game.protocol.SuperWeapon.SuperWeaponEvent;
import com.hawk.game.protocol.SuperWeapon.SuperWeaponFightRecodResp;
import com.hawk.game.protocol.SuperWeapon.SuperWeaponGiftRecodReq;
import com.hawk.game.protocol.SuperWeapon.SuperWeaponPeriod;
import com.hawk.game.protocol.SuperWeapon.SuperWeaponPresident;
import com.hawk.game.protocol.SuperWeapon.SuperWeaponPresidentRecodReq;
import com.hawk.game.protocol.SuperWeapon.SuperWeaponPresidentRecodResp;
import com.hawk.game.protocol.SuperWeapon.SuperWeaponQuarterInfoReq;
import com.hawk.game.protocol.SuperWeapon.SuperWeaponSendGiftInfo;
import com.hawk.game.protocol.SuperWeapon.SuperWeaponSendGiftReq;
import com.hawk.game.protocol.SuperWeapon.SuperWeaponSignUp;
import com.hawk.game.protocol.SuperWeapon.SuperWeaponSignUpInfo;
import com.hawk.game.protocol.SuperWeapon.SuperWeaponSignUpInfoResp;
import com.hawk.game.service.GuildService;
import com.hawk.game.superweapon.SuperWeaponGift;
import com.hawk.game.superweapon.SuperWeaponService;
import com.hawk.game.superweapon.weapon.IWeapon;
import com.hawk.game.util.GameUtil;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.service.WorldPointService;

/**
 * 超级武器(名城)
 * 
 * @author golden
 *
 */
public class PlayerSuperWeaponModule extends PlayerModule {

	/**
	 * 日志
	 */
	private static final Logger logger = LoggerFactory.getLogger("Server");
	
	/**
	 * 构造
	 * @param player
	 */
	public PlayerSuperWeaponModule(Player player) {
		super(player);
	}

	/**
	 * 组装完成的同步
	 */
	@Override
	protected boolean onPlayerAssemble() {
		return true;
	}

	/**
	 * 玩家上线处理
	 *
	 * @return
	 */
	@Override
	protected boolean onPlayerLogin() {
		SuperWeaponService.getInstance().broadcastSuperWeaponInfo(player);
		CrossFortressService.getInstance().broadcastFortressInfo(player);
		return true;
	}

	/**
	 * 超级武器报名信息
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.SUPER_WEAPON_WAR_SIGN_UP_INFO_C_VALUE)
	private void onSuperWeaponSignUpInfo(HawkProtocol protocol) {
		if (!player.hasGuild() || player.isCsPlayer()) {
			return;
		}
		pushSignUpInfo();
	}

	public void pushSignUpInfo() {
		SuperWeaponSignUpInfoResp.Builder builder = SuperWeaponSignUpInfoResp.newBuilder();
		String guildId = player.getGuildId();
		Map<Integer, IWeapon> allWeapon = SuperWeaponService.getInstance().getAllWeapon();
		for (IWeapon superWeapon : allWeapon.values()) {
			if (!superWeapon.checkSignUp(guildId)) {
				continue;
			}
			int pos[] = GameUtil.splitXAndY(superWeapon.getPointId());
			SuperWeaponSignUpInfo.Builder signUpInfo = SuperWeaponSignUpInfo.newBuilder();
			signUpInfo.setPosX(pos[0]);
			signUpInfo.setPosY(pos[1]);
			signUpInfo.setIsSignUp(true);
			signUpInfo.setIsAutoSignUp(superWeapon.checkAutoSignUp(guildId));
			
			boolean hasGift = false;
			int turnCount = SuperWeaponService.getInstance().getTurnCount();
			Map<String, String> superWeaponGiftInfo = LocalRedis.getInstance().getSuperWeaponGiftInfo(turnCount, superWeapon.getPointId(), guildId);
			for (String key : superWeaponGiftInfo.keySet()) {
				if (key.split(":")[0].equals(guildId)) {
					hasGift = true;
					break;
				}
			}
			
			signUpInfo.setHasGift(hasGift);
			builder.addSignUpInfo(signUpInfo);
			
		}
		player.sendProtocol(HawkProtocol.valueOf(HP.code.SUPER_WEAPON_WAR_SIGN_UP_INFO_S_VALUE, builder));
	}
	
	/**
	 * 超级武器报名
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.SUPER_WEAPON_WAR_SIGN_UP_C_VALUE)
	private void onSuperWeaponSignUp(HawkProtocol protocol) {
		// 没有联盟,不能报名超级武器
		if (!player.hasGuild()) {
			sendError(protocol.getType(), Status.SuperWeaponError.SUPER_WEAPON_SIGN_UP_NO_GUILD);
			return;
		}
		// 没有权限报名超级武器
		if (!GuildService.getInstance().checkGuildAuthority(player.getId(), AuthId.SUPER_WEAPON_SIGN_UP)) {
			sendError(protocol.getType(), Status.SuperWeaponError.SUPER_WEAPON_SIGN_UP_NO_AUTHORITY);
			return;
		}
		// 不是报名阶段,不能报名超级武器
		if (SuperWeaponService.getInstance().getStatus() != SuperWeaponPeriod.SIGNUP_VALUE) {
			sendError(protocol.getType(), Status.SuperWeaponError.SUPER_WEAPON_NOT_SIGN_UP_PERIOD);
			return;
		}
		
		SuperWeaponSignUp req = protocol.parseProtocol(SuperWeaponSignUp.getDefaultInstance());
		
		// 超级武器
		int pointId = GameUtil.combineXAndY(req.getPosX(), req.getPosY());
		IWeapon weapon = SuperWeaponService.getInstance().getWeapon(pointId);
		
		// 报名信息已经改变
		if (weapon == null || weapon.checkSignUp(player.getGuildId())) {
			sendError(protocol.getType(), Status.SuperWeaponError.SUPER_WEAPON_SIGN_UP_INFO_CHANGE);
			return;
		}
		
		int signUpCount = 0;
		Map<Integer, IWeapon> allWeapon = SuperWeaponService.getInstance().getAllWeapon();
		for (IWeapon thisWeapon : allWeapon.values()) {
			if (!thisWeapon.checkAutoSignUp(player.getGuildId()) && thisWeapon.checkSignUp(player.getGuildId())) {
				signUpCount++;
			}
		}
		
		int maxSighUp = SuperWeaponConstCfg.getInstance().getMaxSighUp();
		if (signUpCount >= maxSighUp) {
			sendError(protocol.getType(), Status.SuperWeaponError.SIGN_UP_COUNT_LIMIT);
			return;
		}
		
		// 是否报名成功
		if (!SuperWeaponService.getInstance().signUpWar(pointId, player.getGuildId())) {
			return;
		}
		
		player.responseSuccess(protocol.getType());
		player.sendProtocol(HawkProtocol.valueOf(HP.code.SUPER_WEAPON_WAR_SIGN_UP_S_VALUE, req.toBuilder()));
		
		WorldPoint worldPoint = WorldPointService.getInstance().getWorldPoint(pointId);
		if (worldPoint != null) {
			WorldPointService.getInstance().getWorldScene().update(worldPoint.getAoiObjId());
		}
		logger.info("super weapon sign up, playerId:{}, guildId:{}, pointId:{}", player.getId(), player.getGuildId(), pointId);
	}
	
	/**
	 * 超级武器简要战争记录
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.SUPER_WEAPON_BRIEF_FIGHT_RECORD_C_VALUE)
	private void onSuperWeaponFightRecord(HawkProtocol protocol) {
		SuperWeaponFightRecodResp.Builder builder = SuperWeaponFightRecodResp.newBuilder();
		// 获取超级武器事件列表
		List<SuperWeaponEvent.Builder> records = LocalRedis.getInstance().getSuperWeaponBriefEvent();
		for (SuperWeaponEvent.Builder record : records) {
			builder.addEvent(record);
		}
		player.sendProtocol(HawkProtocol.valueOf(HP.code.SUPER_WEAPON_BRIEF_FIGHT_RECORD_S_VALUE, builder));
	}
	
	/**
	 * 超级武器详细战争记录
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.SUPER_WEAPON_DETIAL_FIGHT_RECORD_C_VALUE)
	private void onSuperWeaponDetialFightRecord(HawkProtocol protocol) {
		SuperWeaponDetailFightRecodReq req = protocol.parseProtocol(SuperWeaponDetailFightRecodReq.getDefaultInstance());
		int pointId = GameUtil.combineXAndY(req.getPosX(), req.getPosY());
		
		SuperWeaponFightRecodResp.Builder builder = SuperWeaponFightRecodResp.newBuilder();
		// 获取超级武器事件列表
		List<SuperWeaponEvent.Builder> records = LocalRedis.getInstance().getSuperWeaponDetailEvent(pointId, PresidentConstCfg.getInstance().getMaxEventCount());
		for (SuperWeaponEvent.Builder record : records) {
			builder.addEvent(record);
		}
		player.sendProtocol(HawkProtocol.valueOf(HP.code.SUPER_WEAPON_DETIAL_FIGHT_RECORD_S_VALUE, builder));
	}
	
	/**
	 * 超级武器国王记录
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.SUPER_WEAPON_PRESIDENT_RECORD_C_VALUE)
	private void onSuperWeaponPresidentRecord(HawkProtocol protocol) {
		SuperWeaponPresidentRecodReq req = protocol.parseProtocol(SuperWeaponPresidentRecodReq.getDefaultInstance());
		// 超级武器id
		int pointId = GameUtil.combineXAndY(req.getPosX(), req.getPosY());
		// 获取历届国王记录
		List<SuperWeaponPresident.Builder> historys = LocalRedis.getInstance().getElectedSuperWeapon(PresidentConstCfg.getInstance().getMaxEventCount(), pointId);
		SuperWeaponPresidentRecodResp.Builder builder = SuperWeaponPresidentRecodResp.newBuilder();
		for (SuperWeaponPresident.Builder history : historys) {
			builder.addHistory(history);
		}
		player.sendProtocol(HawkProtocol.valueOf(HP.code.SUPER_WEAPON_PRESIDENT_RECORD_S_VALUE, builder));
	}
	
	/**
	 * 超级武器礼包记录
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.SUPER_WEAPON_GIFT_RECORD_C_VALUE)
	private void onSuperWeaponGiftRecord(HawkProtocol protocol) {
		if (!player.hasGuild()) {
			return;
		}
		SuperWeaponGiftRecodReq req = protocol.parseProtocol(SuperWeaponGiftRecodReq.getDefaultInstance());
		SuperWeaponGift.getInstance().syncSuperWeaponGiftSendRecord(player, GameUtil.combineXAndY(req.getPosX(), req.getPosY()));
	}
	
	/**
	 * 超级武器礼包信息
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.SUPER_WEAPON_GIFT_INFO_C_VALUE)
	private void onSuperWeaponGiftInfo(HawkProtocol protocol) {
		if (!player.hasGuild()) {
			return;
		}
		SuperWeaponGift.getInstance().syncSuperWeaponGiftInfo(player);
	}
	
	/**
	 * 超级武器颁发礼包
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.SUPER_WEAPON_SENT_GIFT_C_VALUE)
	private void onSendGift(HawkProtocol protocol) {
		SuperWeaponSendGiftReq req = protocol.parseProtocol(SuperWeaponSendGiftReq.getDefaultInstance());
		int pointId = GameUtil.combineXAndY(req.getPosX(), req.getPosY());
		if (!player.hasGuild()) {
			return;
		}
		// 超级武器不存在
		IWeapon weapon = SuperWeaponService.getInstance().getWeapon(pointId);
		if (weapon == null) {
			return;
		}
		sendGift(protocol, req, pointId, weapon);
	}

	/**
	 * 颁发普通礼包
	 * @param protocol
	 * @param req
	 * @param pointId
	 * @param weapon
	 */
	private void sendGift(HawkProtocol protocol, SuperWeaponSendGiftReq req, int pointId, IWeapon weapon) {
		// 礼包配置不存在
		int giftId = req.getGiftId();
		
		SuperWeaponAwardCfg giftCfg = HawkConfigManager.getInstance().getConfigByKey(SuperWeaponAwardCfg.class, giftId);
		SuperWeaponSpecialAwardCfg specialGiftCfg = HawkConfigManager.getInstance().getConfigByKey(SuperWeaponSpecialAwardCfg.class, giftId);
		if (giftCfg == null && specialGiftCfg == null) {
			return;
		}
		
		// 颁发玩家无效
		List<SuperWeaponSendGiftInfo> playerInfoList = req.getPlayerInfoList();
		if (playerInfoList == null || playerInfoList.isEmpty()) {
			return;
		}
		
		// 礼包发放数据校验
		int turnCount = SuperWeaponService.getInstance().getTurnCount();
		String giftInfo = LocalRedis.getInstance().getSuperWeaponGiftInfo(turnCount, pointId, player.getGuildId(), giftId);
		if (HawkOSOperator.isEmptyString(giftInfo)) {
			return;
		}
		String[] giftInfoSplit = giftInfo.split("_");
		int sendNum = Integer.parseInt(giftInfoSplit[0]);
		int totalNum = Integer.parseInt(giftInfoSplit[1]);
		
		// 不能给不是本盟的玩家颁发
		for (SuperWeaponSendGiftInfo playerInfo : playerInfoList) {
			if (!GuildService.getInstance().isInTheSameGuild(player.getId(), playerInfo.getPlayerId())) {
				sendError(protocol.getType(), Status.Error.GUILD_PLAYER_HASNOT_GUILD);
				return;
			}
		}
		
		// 不是盟主不能颁发
		if (!GuildService.getInstance().checkGuildAuthority(player.getId(), AuthId.SNED_SUPER_WEAPON_GIFT)) {
			sendError(protocol.getType(), Status.SuperWeaponError.SEND_GIFT_AUTH_LIMIT);
			return;
		}
		
		// 需要颁发的数量
		int needSendCount = 0;
		for (SuperWeaponSendGiftInfo playerInfo : playerInfoList) {
			needSendCount += playerInfo.getCount();
		}
		
		if (!SuperWeaponGift.getInstance().sendGiftCheck(pointId, needSendCount, totalNum, sendNum)) {
			HawkLog.logPrintln("super weapon send gift failed, playerId: {}, giftId: {}, pointId: {}, needSendCount: {}, totalNum: {}, sendNum: {}", player.getId(), giftId, pointId, needSendCount, totalNum, sendNum);
			return;
		}
		
		for (SuperWeaponSendGiftInfo playerInfo : playerInfoList) {
			if (SuperWeaponGift.getInstance().isOnePlayerCountLimit(pointId, giftId, playerInfo.getPlayerId(), playerInfo.getCount())) {
				HawkLog.logPrintln("super weapon send gift failed, playerId: {}, giftId: {}, toPlayerId: {}, sendCount: {}, pointId: {}", player.getId(), giftId, playerInfo.getPlayerId(), playerInfo.getCount(), pointId);
				sendError(protocol.getType(), Status.SuperWeaponError.SEND_GIFT_ONE_LIMIT);
				return;
			}
		}
		
		HawkLog.logPrintln("super weapon send gift ready, playerId: {}, giftId: {}, sendTotalCount: {}, totalNum: {}, weaponId: {}", player.getId(), giftId, needSendCount, totalNum, weapon.getPointId());
		HawkThreadPool threadPool = HawkTaskManager.getInstance().getThreadPool("task");
		// 颁发是否成功
		for (SuperWeaponSendGiftInfo playerInfo : playerInfoList) {
			if (!HawkOSOperator.isEmptyString(playerInfo.getPlayerId())) {
				sendGift(threadPool, playerInfo, giftId, weapon.getPointId());
				LocalRedis.getInstance().incSpPlayerReceiveCount(turnCount, pointId, playerInfo.getPlayerId(), giftId, playerInfo.getCount());
			}
		}
		
		// 礼包个数更新
		LocalRedis.getInstance().updateSuperWeaponGiftInfo(turnCount, pointId, player.getGuildId(), giftId, sendNum + needSendCount, totalNum);
		// 通用成功返回
		player.responseSuccess(protocol.getType());
		// 礼包信息刷新
		SuperWeaponGift.getInstance().syncSuperWeaponGiftInfo(player);
	}
	
	/**
	 * 发放礼物
	 * @param threadPool
	 * @param playerInfo
	 * @param giftId
	 * @param weaponPointId
	 */
	private void sendGift(HawkThreadPool threadPool, SuperWeaponSendGiftInfo playerInfo, int giftId, int weaponPointId) {
		if (threadPool != null) {
			HawkTask task = new HawkTask() {
				@Override
				public Object run() {
					Player sendPlayer = GlobalData.getInstance().makesurePlayer(playerInfo.getPlayerId());
					SuperWeaponGift.getInstance().sendGift(weaponPointId, sendPlayer, giftId, playerInfo.getCount(), player);
					return null;
				}
			};
			int threadIdx = Math.abs(playerInfo.getPlayerId().hashCode()) % threadPool.getThreadNum();
			threadPool.addTask(task, threadIdx, false);
			return;
		}
		
		try {
			Player sendPlayer = GlobalData.getInstance().makesurePlayer(playerInfo.getPlayerId());
			SuperWeaponGift.getInstance().sendGift(weaponPointId, sendPlayer, giftId, playerInfo.getCount(), player);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	
	/**
	 * 获取超级武器驻军列表
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.SUPER_WEAPON_QUARTER_INFO_C_VALUE)
	private boolean getPresidentTowerQuarterInfo(HawkProtocol protocol) {
		SuperWeaponQuarterInfoReq req = protocol.parseProtocol(SuperWeaponQuarterInfoReq.getDefaultInstance());
		int pointId = GameUtil.combineXAndY(req.getPosX(), req.getPosY());
		IWeapon weapon = SuperWeaponService.getInstance().getWeapon(pointId);
		if (weapon == null) {
			return false;
		}
		
		SuperWeaponService.getInstance().sendSuperWeaponQuarterInfo(player, weapon);
		return true;
	}
	
	/**
	 * 获取航海要塞列表
	 */
	@ProtocolHandler(code = HP.code.CROSS_FORTRESS_QUARTER_INFO_C_VALUE)
	private boolean getCrossFortressQuarterInfo(HawkProtocol protocol) {
		SuperWeaponQuarterInfoReq req = protocol.parseProtocol(SuperWeaponQuarterInfoReq.getDefaultInstance());
		int pointId = GameUtil.combineXAndY(req.getPosX(), req.getPosY());
		IFortress fortress = CrossFortressService.getInstance().getFortress(pointId);
		if (fortress == null) {
			return false;
		}
		
		CrossFortressService.getInstance().sendCrossFortressQuarterInfo(player, fortress);
		return true;
	}
	
	@ProtocolHandler(code = HP.code2.SUPER_WEAPON_GIFT_ALL_RECEIVE_COUNT_C_VALUE)
	private void onAllSWGiftReceiveCountReq(HawkProtocol protocol) {
		SWGiftReceiveCountReq req = protocol.parseProtocol(SWGiftReceiveCountReq.getDefaultInstance());
		int giftId = req.getGiftId();
		int posX = req.getPosX();
		int posY = req.getPosY();
		
		int limitCount = 0;
		if (SuperWeaponGift.getInstance().isSpecialGift(giftId)) {
			SuperWeaponSpecialAwardCfg config = HawkConfigManager.getInstance().getConfigByKey(SuperWeaponSpecialAwardCfg.class, giftId);
			limitCount = config.getNumberLimit();
		} else {
			SuperWeaponAwardCfg config = HawkConfigManager.getInstance().getConfigByKey(SuperWeaponAwardCfg.class, giftId);
			limitCount = config.getNumberLimit();
		}
		
		Map<String, Integer> sendCount = new HashMap<>();
		Map<String, String> allSpPlayerReceiveCount = LocalRedis.getInstance().getAllSpPlayerReceiveCount(SuperWeaponService.getInstance().getTurnCount(), GameUtil.combineXAndY(posX, posY));
		if (allSpPlayerReceiveCount != null) {
			for (Entry<String, String> info : allSpPlayerReceiveCount.entrySet()) {
				String sendPlayerId = info.getKey().split(":")[0];
				int sendGiftId = Integer.parseInt(info.getKey().split(":")[1]);
				int count = Integer.parseInt(info.getValue());
				if (sendGiftId != giftId) {
					continue;
				}
				sendCount.put(sendPlayerId, count);
			}
		}
		
		SWAllGiftReceiveCountResp.Builder builder = SWAllGiftReceiveCountResp.newBuilder();
		for (Entry<String, Integer> sendInfo : sendCount.entrySet()) {
			SWGiftReceiveCountResp.Builder sendBuilder = SWGiftReceiveCountResp.newBuilder();
			sendBuilder.setReceiveCount(sendInfo.getValue());
			sendBuilder.setLimitCount(limitCount);
			sendBuilder.setPlayerId(sendInfo.getKey());
			sendBuilder.setGiftId(giftId);
			sendBuilder.setPosX(posX);
			sendBuilder.setPosY(posY);
			builder.addSendInfo(sendBuilder);
		}
		
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.SUPER_WEAPON_GIFT_ALL_RECEIVE_COUNT_S, builder));
	}
	
	/**
	 * 超级武器礼包接收数量
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.SUPER_WEAPON_GIFT_RECEIVE_COUNT_C_VALUE)
	private void onSWGiftReceiveCountReq(HawkProtocol protocol) {
		SWGiftReceiveCountReq req = protocol.parseProtocol(SWGiftReceiveCountReq.getDefaultInstance());
		String playerId = req.getPlayerId();
		int giftId = req.getGiftId();
		int posX = req.getPosX();
		int posY = req.getPosY();
		int pointId = GameUtil.combineXAndY(posX, posY);
		
		SWGiftReceiveCountResp.Builder builder = SWGiftReceiveCountResp.newBuilder();
		int limitCount = 0;
		if (SuperWeaponGift.getInstance().isSpecialGift(giftId)) {
			SuperWeaponSpecialAwardCfg config = HawkConfigManager.getInstance().getConfigByKey(SuperWeaponSpecialAwardCfg.class, giftId);
			limitCount = config.getNumberLimit();
		} else {
			SuperWeaponAwardCfg config = HawkConfigManager.getInstance().getConfigByKey(SuperWeaponAwardCfg.class, giftId);
			limitCount = config.getNumberLimit();
		}
		int turnCount = SuperWeaponService.getInstance().getTurnCount();
		int receiveCount = LocalRedis.getInstance().getSpPlayerReceiveCount(turnCount, pointId, playerId, String.valueOf(giftId));
		builder.setReceiveCount(receiveCount);
		builder.setLimitCount(limitCount);
		builder.setPlayerId(playerId);
		builder.setGiftId(giftId);
		builder.setPosX(posX);
		builder.setPosY(posY);
		
		player.sendProtocol(HawkProtocol.valueOf(HP.code.SUPER_WEAPON_GIFT_RECEIVE_COUNT_S, builder));
	}
	
	/**
	 * 获取排行榜
	 */
	@ProtocolHandler(code = HP.code.SUPER_WEAPON_SEASON_RANK_C_VALUE)
	private boolean getSWSeasonRankInfo(HawkProtocol protocol) {
		SuperWeaponService.getInstance().pushSeasonRank(player);
		return true;
	}
	
	/**
	 * 取消报名
	 */
	@ProtocolHandler(code = HP.code.SUPER_WEAPON_CANCEL_SINGUP_C_VALUE)
	private boolean SWCancelSignUpReq(HawkProtocol protocol) {
		if (!player.hasGuild() || player.isCsPlayer()) {
			return true;
		}
		// 没有权限取消报名超级武器
		if (!GuildService.getInstance().checkGuildAuthority(player.getId(), AuthId.SUPER_WEAPON_SIGN_UP)) {
			sendError(protocol.getType(), Status.SuperWeaponError.SUPER_WEAPON_CANCEL_SIGN_AUTHORITY);
			return true;
		}
		// 不是报名阶段,不能取消报名超级武器
		if (SuperWeaponService.getInstance().getStatus() != SuperWeaponPeriod.SIGNUP_VALUE) {
			sendError(protocol.getType(), Status.SuperWeaponError.SUPER_WEAPON_NOT_SIGN_UP_PERIOD);
			return true;
		}
		
		SWCancelSignUp req = protocol.parseProtocol(SWCancelSignUp.getDefaultInstance());
		int pointId = GameUtil.combineXAndY(req.getPosX(), req.getPosY());
		IWeapon weapon = SuperWeaponService.getInstance().getWeapon(pointId);
		weapon.removeSignUp(player.getGuildId());
		player.responseSuccess(protocol.getType());
		pushSignUpInfo();
		ScheduleService.getInstance().syncScheduleInfo(player);
		ScheduleService.getInstance().notifyGuildMember(player, player.getGuildId());
		WorldPointService.getInstance().notifyPointUpdate(req.getPosX(), req.getPosY());
		return true;
	}
}
