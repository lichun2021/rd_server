package com.hawk.game.superweapon;

import java.util.ArrayList;
import java.util.List;

import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.googlecode.protobuf.format.JsonFormat;
import com.googlecode.protobuf.format.JsonFormat.ParseException;
import com.hawk.game.config.SuperWeaponAwardCfg;
import com.hawk.game.config.SuperWeaponSpecialAwardCfg;
import com.hawk.game.global.LocalRedis;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const.MailRewardStatus;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.MailConst;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.SuperWeapon.SuperWeaponGiftInfo;
import com.hawk.game.protocol.SuperWeapon.SuperWeaponGiftRecord;
import com.hawk.game.protocol.SuperWeapon.SuperWeaponGiftRecordResp;
import com.hawk.game.protocol.SuperWeapon.SuperWeaponPeriod;
import com.hawk.game.protocol.SuperWeapon.SuperWeapontGiftInfoResp;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mail.SystemMailService;
import com.hawk.game.superweapon.weapon.IWeapon;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst.SuperWeaponAwardType;

/**
 * 超级武器礼包
 * 
 * @author golden
 *
 */
public class SuperWeaponGift {

	/**
	 * 日志记录
	 */
	static Logger logger = LoggerFactory.getLogger("Server");
	
	/**
	 * 实例
	 */
	private static SuperWeaponGift instance = null;
	
	/**
	 * 获取实例
	 * @return
	 */
	public static SuperWeaponGift getInstance() {
		if (instance == null) {
			instance = new SuperWeaponGift();
		}
		return instance;
	}
	
	/**
	 * 私有化默认构造
	 */
	private SuperWeaponGift() {
		
	}
	
	/**
	 * 初始化
	 * @return
	 */
	public void init() {
		
	}
	
	/**
	 * 礼包发放数据校验
	 * 
	 * @param requestList
	 * @param giftId
	 * @param giftNumber
	 * @return
	 */
	public boolean sendGiftCheck(int pointId, int count, int totalNumber, int hasSendNum) {
		if (totalNumber - hasSendNum < count) {
			return false;
		}
		return true;
	}
	
	/**
	 * 是否被每人可获取上限限制
	 * @param pointId
	 * @param giftId
	 * @param playerId
	 * @return
	 */
	public boolean isOnePlayerCountLimit(int pointId, int giftId, String playerId, int count) {
		int limitCount = 0;
		if (isSpecialGift(giftId)) {
			SuperWeaponSpecialAwardCfg config = HawkConfigManager.getInstance().getConfigByKey(SuperWeaponSpecialAwardCfg.class, giftId);
			limitCount = config.getNumberLimit();
		} else {
			SuperWeaponAwardCfg config = HawkConfigManager.getInstance().getConfigByKey(SuperWeaponAwardCfg.class, giftId);
			limitCount = config.getNumberLimit();
		}
		int turnCount = SuperWeaponService.getInstance().getTurnCount();
		int beforeCount = LocalRedis.getInstance().getSpPlayerReceiveCount(turnCount, pointId, playerId, String.valueOf(giftId));
		if (beforeCount + count > limitCount) {
			return true;
		}
		return false;
	}
	
	/**
	 * 是否是特殊礼包(不包含在普通礼包里面就判定是特殊礼包)
	 * @param giftId
	 * @return
	 */
	public boolean isSpecialGift(int giftId) {
		SuperWeaponAwardCfg awardCfg = HawkConfigManager.getInstance().getConfigByKey(SuperWeaponAwardCfg.class, giftId);
		return awardCfg == null;
	}
	
	/**
	 * 颁发礼包
	 * @param weapon
	 * @param player
	 * @param giftCfg
	 * @param playerIds
	 * @return
	 */
	public boolean sendGift(int pointId, Player player, int giftId, int count, Player presidentPlayer) {
		if (count <= 0) {
			return false;
		}
		int giftType = 0;
		String giftName = "";
		List<ItemInfo> rewards = new ArrayList<>();
		
		if (isSpecialGift(giftId)) {
			SuperWeaponSpecialAwardCfg config = HawkConfigManager.getInstance().getConfigByKey(SuperWeaponSpecialAwardCfg.class, giftId);
			giftType = config.getType();
			giftName = config.getGiftName();
			rewards = config.getRewardItem();
		} else {
			SuperWeaponAwardCfg config = HawkConfigManager.getInstance().getConfigByKey(SuperWeaponAwardCfg.class, giftId);
			giftType = config.getType();
			giftName = config.getGiftName();
			rewards = config.getRewardItem();
		}
		
		for (ItemInfo reward : rewards) {
			reward.setCount(reward.getCount() * count);
		}
		
		int[] pos = GameUtil.splitXAndY(pointId);
		MailId mailId = getGiftSendMailId(giftType);
		if (mailId == null) {
			return false;
		}
		
		int turnCount = SuperWeaponService.getInstance().getTurnCount();
		
		// 发送邮件---国王礼包
		SystemMailService.getInstance().sendMail(MailParames.newBuilder()
				.setPlayerId(player.getId())
				.setMailId(mailId)
				.setAwardStatus(MailRewardStatus.NOT_GET)
				.addContents(pos[0], pos[1], giftName)
				.setRewards(rewards)
				.build());
		
		//Player sendPlayer = GlobalData.getInstance().makesurePlayer(player.getId());
		// 添加记录数据
		SuperWeaponGiftRecord.Builder giftRecord = SuperWeaponGiftRecord.newBuilder();
		giftRecord.setSendTime(HawkTime.getMillisecond());
		giftRecord.setPlayerName(player.getName());
		giftRecord.setGiftId(giftId);
		giftRecord.setPlayerId(player.getId());
		giftRecord.setGuildTag((player.getGuildTag()));
		giftRecord.setPresidentName(presidentPlayer.getName());
		giftRecord.setPosX(pos[0]);
		giftRecord.setPosY(pos[1]);
		String record = JsonFormat.printToString(giftRecord.build());
		String[] array = new String[count];
		for (int i = 0; i < count; i++) {
			array[i] = record;
		}
		LocalRedis.getInstance().addSuperWeaponGiftSend(turnCount, pointId, player.getGuildId(), array);
		
		logger.info("super weapon send gift, player: {}, sendPlayer: {}, giftId: {}, weaponId: {}, giftCount: {}", presidentPlayer.getId(), player.getId(), giftId, pointId, count);
		return true;
	}
	
	private MailId getGiftSendMailId(int giftType) {
		MailId mailId = null;
		switch (SuperWeaponAwardType.valueOf(giftType)) {
		case ATTACK_LEADER_SEND_AWARD:
			mailId = MailConst.MailId.SUPER_WEAPON_GIFT_SEND_ATTACK;
			break;
		case OCCUPY_LEADER_SEND_AWARD:
			mailId = MailConst.MailId.SUPER_WEAPON_GIFT_SEND_OCCUPY;
			break;
		case CONTROL_LEADER_SEND_AWARD:
			mailId = MailConst.MailId.SUPER_WEAPON_GIFT_SEND_CONTROL;
			break;
		default:
			break;
		}
		return mailId;
	}
	
	/**
	 * 同步超级武器礼包信息
	 * @param player
	 */
	public void syncSuperWeaponGiftInfo(Player player) {
		List<Integer> signUp = new ArrayList<>();
		
		for (IWeapon weapon : SuperWeaponService.getInstance().getAllWeapon().values()) {
			if (!weapon.checkSignUp(player.getGuildId())) {
				continue;
			}
			signUp.add(weapon.getPointId());
		}
		
		SuperWeapontGiftInfoResp.Builder response = SuperWeapontGiftInfoResp.newBuilder();
		if (signUp.isEmpty()) {
			player.sendProtocol(HawkProtocol.valueOf(HP.code.SUPER_WEAPON_GIFT_INFO_S, response));
			return;
		}
		
		int status = SuperWeaponService.getInstance().getStatus();
		if (status != SuperWeaponPeriod.WARFARE_VALUE && status != SuperWeaponPeriod.CONTROL_VALUE) {
			player.sendProtocol(HawkProtocol.valueOf(HP.code.SUPER_WEAPON_GIFT_INFO_S, response));
			return;
		}
		
		int turnCount = SuperWeaponService.getInstance().getTurnCount();
		int maxRound = SuperWeaponService.getInstance().getSpecialAwardMaxRound();
		int findRound = turnCount <= maxRound ? turnCount : (turnCount % maxRound);
		if (findRound == 0) {
			findRound = maxRound;
		}
		
		ConfigIterator<SuperWeaponAwardCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(SuperWeaponAwardCfg.class);
		while (iterator.hasNext()) {
			SuperWeaponAwardCfg giftCfg = iterator.next();
			if (SuperWeaponAwardType.valueOf(giftCfg.getType()) != SuperWeaponAwardType.ATTACK_LEADER_SEND_AWARD
				&& SuperWeaponAwardType.valueOf(giftCfg.getType()) != SuperWeaponAwardType.OCCUPY_LEADER_SEND_AWARD
				&& SuperWeaponAwardType.valueOf(giftCfg.getType()) != SuperWeaponAwardType.CONTROL_LEADER_SEND_AWARD) {
				continue;
			}

			for (int pointId : signUp) {
				
				if (GameUtil.combineXAndY(giftCfg.getX(), giftCfg.getY()) != pointId) {
					continue;
				}
				
				int[] pos = GameUtil.splitXAndY(pointId);
				SuperWeaponGiftInfo.Builder giftInfo = SuperWeaponGiftInfo.newBuilder();
				giftInfo.setGiftId(giftCfg.getId());
				String giftNumInfo = LocalRedis.getInstance().getSuperWeaponGiftInfo(turnCount, pointId, player.getGuildId(), giftCfg.getId());
				if (HawkOSOperator.isEmptyString(giftNumInfo)) {
					giftInfo.setResidueNumber(0);
					giftInfo.setTotalNumber(0);
				} else {
					String[] giftNumSplit = giftNumInfo.split("_");
					giftInfo.setResidueNumber(Integer.parseInt(giftNumSplit[1]) - Integer.parseInt(giftNumSplit[0]));
					giftInfo.setTotalNumber(Integer.parseInt(giftNumSplit[1]));
				}
				giftInfo.setPosX(pos[0]);
				giftInfo.setPosY(pos[1]);
				response.addGiftInfo(giftInfo);
			}
		}
		
		ConfigIterator<SuperWeaponSpecialAwardCfg> specialIterator = HawkConfigManager.getInstance().getConfigIterator(SuperWeaponSpecialAwardCfg.class);
		while (specialIterator.hasNext()) {
			SuperWeaponSpecialAwardCfg giftCfg = specialIterator.next();
			if (SuperWeaponAwardType.valueOf(giftCfg.getType()) != SuperWeaponAwardType.ATTACK_LEADER_SEND_AWARD
				&& SuperWeaponAwardType.valueOf(giftCfg.getType()) != SuperWeaponAwardType.OCCUPY_LEADER_SEND_AWARD
				&& SuperWeaponAwardType.valueOf(giftCfg.getType()) != SuperWeaponAwardType.CONTROL_LEADER_SEND_AWARD) {
				continue;
			}

			for (int pointId : signUp) {
				
				if (GameUtil.combineXAndY(giftCfg.getX(), giftCfg.getY()) != pointId) {
					continue;
				}
				
				if (findRound != giftCfg.getRound()) {
					continue;
				}
				
				int[] pos = GameUtil.splitXAndY(pointId);
				SuperWeaponGiftInfo.Builder giftInfo = SuperWeaponGiftInfo.newBuilder();
				giftInfo.setGiftId(giftCfg.getId());
				String giftNumInfo = LocalRedis.getInstance().getSuperWeaponGiftInfo(turnCount, pointId, player.getGuildId(), giftCfg.getId());
				if (HawkOSOperator.isEmptyString(giftNumInfo)) {
					giftInfo.setResidueNumber(0);
					giftInfo.setTotalNumber(0);
				} else {
					String[] giftNumSplit = giftNumInfo.split("_");
					giftInfo.setResidueNumber(Integer.parseInt(giftNumSplit[1]) - Integer.parseInt(giftNumSplit[0]));
					giftInfo.setTotalNumber(Integer.parseInt(giftNumSplit[1]));
				}
				giftInfo.setPosX(pos[0]);
				giftInfo.setPosY(pos[1]);
				response.addGiftInfo(giftInfo);
			}
		}
		
		player.sendProtocol(HawkProtocol.valueOf(HP.code.SUPER_WEAPON_GIFT_INFO_S, response));
	}
	
	/**
	 * 同步礼包发放记录
	 * 
	 * @param player
	 */
	public void syncSuperWeaponGiftSendRecord(Player player, int pointId) {
		SuperWeaponGiftRecordResp.Builder response = SuperWeaponGiftRecordResp.newBuilder();
		
		int turnCount = SuperWeaponService.getInstance().getTurnCount();
		for (IWeapon superWeapon : SuperWeaponService.getInstance().getAllWeapon().values()) {
			if (!superWeapon.checkSignUp(player.getGuildId())) {
				continue;
			}
			List<String> sendMap = LocalRedis.getInstance().getAllSuperWeaponGiftSend(turnCount, superWeapon.getPointId(), player.getGuildId());
			for (String info : sendMap) {
				SuperWeaponGiftRecord.Builder giftRecord = SuperWeaponGiftRecord.newBuilder();
				try {
					JsonFormat.merge(info, giftRecord);
				} catch (ParseException e) {
					HawkException.catchException(e);
				}
				response.addGiftRecord(giftRecord);
			}
		}
		player.sendProtocol(HawkProtocol.valueOf(HP.code.SUPER_WEAPON_GIFT_RECORD_S, response));
	}
}
