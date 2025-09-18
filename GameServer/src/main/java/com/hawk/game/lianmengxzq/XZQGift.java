package com.hawk.game.lianmengxzq;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.hawk.config.HawkConfigManager;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.googlecode.protobuf.format.JsonFormat;
import com.googlecode.protobuf.format.JsonFormat.ParseException;
import com.hawk.game.config.XZQAwardCfg;
import com.hawk.game.config.XZQConstCfg;
import com.hawk.game.config.XZQPointCfg;
import com.hawk.game.global.GlobalData;
import com.hawk.game.item.AwardItems;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.lianmengxzq.worldpoint.XZQWorldPoint;
import com.hawk.game.manager.AssembleDataManager;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const.MailRewardStatus;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.MailConst;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.XZQ.PBXZQGiftInfo;
import com.hawk.game.protocol.XZQ.PBXZQGiftInfoResp;
import com.hawk.game.protocol.XZQ.PBXZQGiftRecord;
import com.hawk.game.protocol.XZQ.PBXZQGiftRecordResp;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mail.SystemMailService;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst.XZQAwardType;
import com.hawk.game.world.march.IWorldMarch;

/**
 * 小站区礼包
 * 
 * @author golden
 *
 */
public class XZQGift {

	/**
	 * 日志记录
	 */
	static Logger logger = LoggerFactory.getLogger("Server");
	
	/**
	 * 实例
	 */
	private static XZQGift instance = null;
	
	/**
	 * 获取实例
	 * @return
	 */
	public static XZQGift getInstance() {
		if (instance == null) {
			instance = new XZQGift();
		}
		return instance;
	}
	
	/**
	 * 私有化默认构造
	 */
	private XZQGift() {
		
	}
	
	/**
	 * 初始化
	 * @return
	 */
	public void init() {
		
	}
	
	/**
	 * 攻破奖励
	 * @param xzqPoint
	 * @param guildId
	 * @param marchList
	 * @param atkPlayers
	 */
	public void sendAttackAward(XZQWorldPoint xzqPoint, String guildId, List<IWorldMarch> marchList, List<Player> atkPlayers) {
		boolean occupyed =xzqPoint.checkOccupyHistory(guildId);
		if(occupyed){
			return;
		}
		XZQPointCfg xzqCfg = xzqPoint.getXzqCfg();
		int[] pos = GameUtil.splitXAndY(xzqPoint.getId());
		// 攻占者奖励
		AwardItems memberAwards = AwardItems.valueOf();
		List<XZQAwardCfg> memberAwardCfgs = null;
		if(xzqPoint.isInitOccupyed()){
			//历史上第一次攻破
			memberAwardCfgs = AssembleDataManager.getInstance().getXZQFirstAwards(xzqPoint.getId(), XZQAwardType.ATTACK_MEMBER_AWARD);
		}else{
			//当前期第一次攻破
			memberAwardCfgs = AssembleDataManager.getInstance().getXZQAwards(xzqPoint.getId(), XZQAwardType.ATTACK_MEMBER_AWARD);
		}
		if(memberAwardCfgs != null && !memberAwardCfgs.isEmpty()){
			for (XZQAwardCfg cfg : memberAwardCfgs) {
				for (int i = 0; i < cfg.getTotalNumber(); i++) {
					memberAwards.addItemInfos(cfg.getRewardItem());
				}
			}
			for (Player player : atkPlayers) {
				SystemMailService.getInstance().sendMail(MailParames.newBuilder()
						.setPlayerId(player.getId())
						.addContents(xzqCfg.getId(), pos[0], pos[1],XZQConstCfg.getInstance().getBattleControlTime())
						.setMailId(MailId.XZQ_ATTACK_MEMBER_AWARD)
						.setRewards(memberAwards.getAwardItems())
						.setAwardStatus(MailRewardStatus.NOT_GET)
						.build());
			}
		}
		// 联盟成员奖励
		AwardItems guildAwards = AwardItems.valueOf();
		List<XZQAwardCfg> guildAwardCfgs = null;
		if(xzqPoint.isInitOccupyed()){
			//历史上第一次攻破
			guildAwardCfgs = AssembleDataManager.getInstance().getXZQFirstAwards(xzqPoint.getId(), XZQAwardType.ATTACK_GUILD_MEMBER_AWARD);
		}else{
			//当前期第一次攻破
			guildAwardCfgs = AssembleDataManager.getInstance().getXZQAwards(xzqPoint.getId(), XZQAwardType.ATTACK_GUILD_MEMBER_AWARD);
		}
		if(guildAwardCfgs != null && !guildAwardCfgs.isEmpty()){
			for (XZQAwardCfg cfg : guildAwardCfgs) {
				for (int i = 0; i < cfg.getTotalNumber(); i++) {
					guildAwards.addItemInfos(cfg.getRewardItem());
				}
			}
			for (String playerId : GuildService.getInstance().getGuildMembers(guildId)) {
				SystemMailService.getInstance().sendMail(MailParames.newBuilder()
						.setPlayerId(playerId)
						.addContents(xzqCfg.getId(), pos[0], pos[1])
						.setMailId(MailId.XZQ_ATTACK_GUILD_AWARD)
						.setRewards(guildAwards.getAwardItems())
						.setAwardStatus(MailRewardStatus.NOT_GET)
						.build());
			}
		}
		
		// 礼包
		int termId = XZQService.getInstance().getXZQTermId();
		List<XZQAwardCfg> leaderSendAwardCfgs = null;
		if(xzqPoint.isInitOccupyed()){
			//历史上第一次攻破
			leaderSendAwardCfgs = AssembleDataManager.getInstance().getXZQFirstAwards(xzqPoint.getId(), XZQAwardType.ATTACK_LEADER_SEND_AWARD);
		}else{
			//当前期第一次攻破
			leaderSendAwardCfgs = AssembleDataManager.getInstance().getXZQAwards(xzqPoint.getId(), XZQAwardType.ATTACK_LEADER_SEND_AWARD);
		}
		if(leaderSendAwardCfgs != null && !leaderSendAwardCfgs.isEmpty()){
			for (XZQAwardCfg cfg : leaderSendAwardCfgs) {
				String xzqGiftInfo = XZQRedisData.getInstance().getXZQGiftInfo(termId, guildId, xzqPoint.getXzqCfg().getId(), cfg.getId());
				String afterInfo = null;
				if (HawkOSOperator.isEmptyString(xzqGiftInfo)) {
					int sendCount = 0;
					int totalCount = cfg.getTotalNumber();
					afterInfo = String.valueOf(sendCount) + "_" + totalCount;
				} else {
					String[] splitInfo = xzqGiftInfo.split("_");
					int sendCount = Integer.parseInt(splitInfo[0]);
					int totalCount = Integer.parseInt(splitInfo[1]) + cfg.getTotalNumber();
					afterInfo = String.valueOf(sendCount) + "_" + totalCount;
				}
				XZQRedisData.getInstance().updateXZQGiftInfo(termId, guildId, xzqPoint.getXzqCfg().getId(), cfg.getId(), afterInfo);
			}
		}
		
	}
	
	/**
	 * 发控制奖
	 */
	public void sendControlAward(XZQWorldPoint point) {
		String controlGuild = point.getGuildControl();
		if(HawkOSOperator.isEmptyString(controlGuild)){
			return;
		}
		boolean guildExist = GuildService.getInstance().isGuildExist(controlGuild);
		if(!guildExist){
			return;
		}
		XZQPointCfg xzqPointCfg = point.getXzqCfg();
		// 联盟成员奖励
		AwardItems guildAwards = AwardItems.valueOf();
		List<XZQAwardCfg> guildAwardCfgs = null;
		if(point.isInitControl()){
			guildAwardCfgs = AssembleDataManager.getInstance().getXZQFirstAwards(point.getId(), XZQAwardType.CONTROL_GUILD_MEMEBER_AWARD);
		}else{
			guildAwardCfgs = AssembleDataManager.getInstance().getXZQAwards(point.getId(), XZQAwardType.CONTROL_GUILD_MEMEBER_AWARD);
		}
		for (XZQAwardCfg cfg : guildAwardCfgs) {
			for (int i = 0; i < cfg.getTotalNumber(); i++) {
				guildAwards.addItemInfos(cfg.getRewardItem());
			}
		}
		if(!guildAwardCfgs.isEmpty()){
			int[] pos = GameUtil.splitXAndY(point.getId());
			for (String playerId : GuildService.getInstance().getGuildMembers(controlGuild)) {
				SystemMailService.getInstance().sendMail(MailParames.newBuilder()
						.setPlayerId(playerId)
						.addContents(xzqPointCfg.getId(), pos[0], pos[1])
						.setMailId(MailId.XZQ_CONTROL_GUILD_AWARD)
						.setRewards(guildAwards.getAwardItems())
						.setAwardStatus(MailRewardStatus.NOT_GET)
						.build());
				
				logger.info("XZQWorldPoint sendControlAward XZQ_CONTROL_GUILD_AWARD,point:{},x:{},y:{},playerId:{},guild:{}",
						point.getId(),point.getX(),point.getY(),playerId,playerId);
			}
		}
		//发放分配奖励
		int termId = XZQService.getInstance().getXZQTermId();
		List<XZQAwardCfg> leaderSendAwardCfgs = null;
		if(point.isInitControl()){
			leaderSendAwardCfgs = AssembleDataManager.getInstance().getXZQFirstAwards(point.getId(), XZQAwardType.CONTROL_LEADER_SEND_AWARD);
		}else{
			leaderSendAwardCfgs = AssembleDataManager.getInstance().getXZQAwards(point.getId(), XZQAwardType.CONTROL_LEADER_SEND_AWARD);
		}
		for (XZQAwardCfg cfg : leaderSendAwardCfgs) {
			String xzqGiftInfo = XZQRedisData.getInstance().getXZQGiftInfo(termId, controlGuild, xzqPointCfg.getId(), cfg.getId());
			String afterInfo = null;
			if (HawkOSOperator.isEmptyString(xzqGiftInfo)) {
				int sendCount = 0;
				int totalCount = cfg.getTotalNumber();
				afterInfo = String.valueOf(sendCount) + "_" + totalCount;
			} else {
				String[] splitInfo = xzqGiftInfo.split("_");
				int sendCount = Integer.parseInt(splitInfo[0]);
				int totalCount = Integer.parseInt(splitInfo[1]) + cfg.getTotalNumber();
				afterInfo = String.valueOf(sendCount) + "_" + totalCount;
			}
			XZQRedisData.getInstance().updateXZQGiftInfo(termId, controlGuild, xzqPointCfg.getId(), cfg.getId(), afterInfo);
			logger.info("XZQWorldPoint sendControlAward sendXZQGift,point:{},x:{},y:{},termId:{},guild:{},giftId:{},giftInfo:{}",
					xzqPointCfg.getId(),point.getX(),point.getY(),termId,controlGuild, cfg.getId(), afterInfo);
		}
		
	}

	
	
	
	/**
	 * 礼包发放数据校验
	 * 
	 * @param requestList
	 * @param giftId
	 * @param giftNumber
	 * @return
	 */
	public boolean sendGiftCheck(int count, int totalNumber, int hasSendNum) {
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
	public boolean isOnePlayerCountLimit(int pointId, int giftId, Map<String,Integer> sendList) {
		XZQAwardCfg config = HawkConfigManager.getInstance().getConfigByKey(XZQAwardCfg.class, giftId);
		int termId = XZQService.getInstance().getXZQTermId();
		Map<String,Integer> sendCount = XZQRedisData.getInstance().getXZQPlayerReceiveCounts(termId, pointId, giftId);
		Map<String,String> aftCount = new HashMap<>();
		for(Entry<String, Integer> entry : sendList.entrySet() ){
			String playerId = entry.getKey();
			int sendNum = entry.getValue();
			int beforeCount = sendCount.getOrDefault(playerId, 0);
			int afterCount = beforeCount + sendNum;
			if (afterCount > config.getNumberLimit()) {
				return true;
			}
			aftCount.put(playerId, String.valueOf(afterCount));
		}
		XZQRedisData.getInstance().updateXZQPlayerReceiveCounts(termId, pointId,giftId, aftCount);
		return false;
	}
	
	
	
	/**
	 * 颁发礼包
	 * @param xzqPoint
	 * @param player
	 * @param giftCfg
	 * @param playerIds
	 * @return
	 */
	public boolean sendGift(int termId,int pointId, String reciverId, int giftId, int count, Player presidentPlayer) {
		int giftType = 0;
		String giftName = "";
		List<ItemInfo> rewards = new ArrayList<>();
		XZQPointCfg pointCfg = HawkConfigManager.getInstance().getConfigByKey(XZQPointCfg.class, pointId);
		XZQAwardCfg config = HawkConfigManager.getInstance().getConfigByKey(XZQAwardCfg.class, giftId);
		giftType = config.getType();
		giftName = config.getGiftName();
		rewards = config.getRewardItem();
		for (ItemInfo reward : rewards) {
			reward.setCount(reward.getCount() * count);
		}
		MailId mailId = getGiftSendMailId(giftType);
		if (mailId == null) {
			return false;
		}
		// 发送邮件---国王礼包
		SystemMailService.getInstance().sendMail(MailParames.newBuilder()
				.setPlayerId(reciverId)
				.setMailId(mailId)
				.setAwardStatus(MailRewardStatus.NOT_GET)
				.addContents(pointCfg.getId(),pointCfg.getX(),pointCfg.getY(), giftName)
				.setRewards(rewards)
				.build());
		
		Player sendPlayer = GlobalData.getInstance().makesurePlayer(reciverId);
		// 添加记录数据
		PBXZQGiftRecord.Builder giftRecord = PBXZQGiftRecord.newBuilder();
		giftRecord.setSendTime(HawkTime.getMillisecond());
		giftRecord.setPlayerName(sendPlayer.getName());
		giftRecord.setGiftId(giftId);
		giftRecord.setPlayerId(sendPlayer.getId());
		giftRecord.setGuildTag((sendPlayer.getGuildTag()));
		giftRecord.setSender(presidentPlayer.getName());
		giftRecord.setPosX(pointCfg.getX());
		giftRecord.setPosY(pointCfg.getY());
		for (int i = 0; i < count; i++) {
			XZQRedisData.getInstance().addXZQGiftSend(termId,sendPlayer.getGuildId(),JsonFormat.printToString(giftRecord.build()));
		}
		
		logger.info("super xzq send gift, reciverId:{}, presidentPlayer:{}, giftId:{}, pointId:{}", reciverId, presidentPlayer.getId(), giftId, pointCfg.getId());
		return true;
	}
	
	private MailId getGiftSendMailId(int giftType) {
		MailId mailId = null;
		switch (XZQAwardType.valueOf(giftType)) {
		case ATTACK_LEADER_SEND_AWARD:
			mailId = MailConst.MailId.XZQ_GIFT_SEND_ATTACK;
			break;
		case OCCUPY_LEADER_SEND_AWARD:
			mailId = MailConst.MailId.XZQ_GIFT_SEND_OCCUPY;
			break;
		case CONTROL_LEADER_SEND_AWARD:
			mailId = MailConst.MailId.XZQ_GIFT_SEND_CONTROL;
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
	public void syncXZQGiftInfo(Player player) {
		String guildId = player.getGuildId();
		if(HawkOSOperator.isEmptyString(guildId)){
			return;
		}
		if(!GuildService.getInstance().isGuildExist(guildId)){
			return;
		}
		int termId = XZQService.getInstance().getXZQTermId();
		Map<String,String> gfitInfo = XZQRedisData.getInstance().getAllXZQGiftInfo(termId, guildId);
		PBXZQGiftInfoResp.Builder response = PBXZQGiftInfoResp.newBuilder();
		for(Entry<String, String> entry : gfitInfo.entrySet()){
			String key = entry.getKey();
			String value = entry.getValue();
			String[] keyArr = key.split(":");
			String[] valArr = value.split("_");
			
			int pointId = Integer.parseInt(keyArr[0]);
			int giftId = Integer.parseInt(keyArr[1]);
			int residueNumber = Integer.parseInt(valArr[1]) - Integer.parseInt(valArr[0]);
			int totalNumber = Integer.parseInt(valArr[1]);
			XZQPointCfg pointCfg = HawkConfigManager.getInstance().getConfigByKey(XZQPointCfg.class, pointId);
			if(pointCfg == null){
				continue;
			}
			XZQAwardCfg acfg = HawkConfigManager.getInstance().getConfigByKey(XZQAwardCfg.class, giftId);
			if(acfg == null){
				continue;
			}
			PBXZQGiftInfo.Builder giftInfo = PBXZQGiftInfo.newBuilder();
			giftInfo.setGiftId(giftId);
			giftInfo.setResidueNumber(residueNumber);
			giftInfo.setTotalNumber(totalNumber);
			giftInfo.setPosX(pointCfg.getX());
			giftInfo.setPosY(pointCfg.getY());
			response.addGiftInfo(giftInfo);
			
		}
		player.sendProtocol(HawkProtocol.valueOf(HP.code.XZQ_GIFT_INFO_S, response));
	}
	
	
	/**
	 * 同步礼包发放记录
	 * 
	 * @param player
	 */
	public void syncXZQGiftSendRecord(Player player, int pointId) {
		String guildId = player.getGuildId();
		if(HawkOSOperator.isEmptyString(guildId)){
			return;
		}
		if(!GuildService.getInstance().isGuildExist(guildId)){
			return;
		}
		int termId = XZQService.getInstance().getXZQTermId();
		List<String> sendMap = XZQRedisData.getInstance().getAllXZQGiftSend(termId,player.getGuildId());
		PBXZQGiftRecordResp.Builder response = PBXZQGiftRecordResp.newBuilder();
		for (String info : sendMap) {
			PBXZQGiftRecord.Builder giftRecord = PBXZQGiftRecord.newBuilder();
			try {
				JsonFormat.merge(info, giftRecord);
			} catch (ParseException e) {
				HawkException.catchException(e);
			}
			response.addGiftRecord(giftRecord);
		}
		player.sendProtocol(HawkProtocol.valueOf(HP.code.XZQ_GIFT_RECORD_S, response));
	}
}
