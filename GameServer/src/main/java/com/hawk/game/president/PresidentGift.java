package com.hawk.game.president;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringJoiner;

import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.tickable.HawkPeriodTickable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.googlecode.protobuf.format.JsonFormat;
import com.googlecode.protobuf.format.JsonFormat.ParseException;
import com.hawk.game.config.AwardCfg;
import com.hawk.game.config.PresidentConstCfg;
import com.hawk.game.config.PresidentGiftCfg;
import com.hawk.game.entity.OfficerEntity;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.LocalRedis;
import com.hawk.game.log.BehaviorLogger;
import com.hawk.game.log.BehaviorLogger.Params;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.Const.ChatType;
import com.hawk.game.protocol.Const.MailRewardStatus;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.Player.MiniPlayerMsg;
import com.hawk.game.protocol.President.GiftInfo;
import com.hawk.game.protocol.President.GiftRecord;
import com.hawk.game.protocol.President.MemeberInfo;
import com.hawk.game.protocol.President.PresidentGiftInfo;
import com.hawk.game.protocol.President.PresidentGiftRecordRes;
import com.hawk.game.protocol.President.PresidentGiftRecordsUpdate;
import com.hawk.game.protocol.President.PresidentSearchRes;
import com.hawk.game.protocol.Rank.RankInfo;
import com.hawk.game.protocol.Rank.RankType;
import com.hawk.game.protocol.Status;
import com.hawk.game.rank.RankService;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.SearchService;
import com.hawk.game.service.chat.ChatParames;
import com.hawk.game.service.chat.ChatService;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mail.SystemMailService;
import com.hawk.game.util.BuilderUtil;
import com.hawk.game.util.GameUtil;
import com.hawk.log.Action;
import com.hawk.log.Source;

/**
 * 国王礼包
 * 
 * @author hawk
 * @date 2016-10-10 11:58:41
 */
public class PresidentGift {
	/**
	 * 全局实例对象
	 */
	static PresidentGift instance = null;
	private static Logger logger = LoggerFactory.getLogger("Server");
	/**
	 * 获取实例对象
	 * 
	 * @return
	 */
	public static PresidentGift getInstance() {
		if (instance == null) {
			instance = new PresidentGift();
		}
		return instance;
	}

	/**
	 * 构造
	 * 
	 * @param xid
	 */
	private PresidentGift() {

	}

	/**
	 * 初始化
	 * 
	 * @return
	 */
	public boolean init() {
		// 注册更新机制
		PresidentFightService.getInstance().addTickable(new HawkPeriodTickable(PresidentConstCfg.getInstance().getTickPeriod()) {
			@Override
			public void onPeriodTick() {
				onTickUpdate();
			}
		});
		return true;
	}

	/**
	 * 国王变更之后的通知(需要判断两个id是否一致, 连任的情况)
	 */
	protected void onPresidentChanged(String lastPresidentId, String currPresidentId) {
		try {
			// 清理之前存储的状态
			LocalRedis.getInstance().deleteAllGiftNumber();
			LocalRedis.getInstance().deleteAllGiftSend();
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/**
	 * 帧更新检测
	 */
	protected void onTickUpdate() {

	}

	/**
	 * 国王礼包初始化
	 * 
	 * @param player
	 */
	public void syncGiftInfo(Player player) {
		PresidentGiftInfo.Builder response = PresidentGiftInfo.newBuilder();
		// 礼包配置集合数据
		ConfigIterator<PresidentGiftCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(PresidentGiftCfg.class);
		while (iterator.hasNext()) {
			// 整理数据
			PresidentGiftCfg giftCfg = iterator.next();
			
			// 数据包构建
			GiftInfo.Builder giftInfo = GiftInfo.newBuilder();
			giftInfo.setGiftId(giftCfg.getId());
			int number = giftCfg.getTotalNumber() - LocalRedis.getInstance().getGiftNumber(giftCfg.getId()) ;
			giftInfo.setResidueNumber(Math.max(0, number));
			giftInfo.setTotalNumber(giftCfg.getTotalNumber());
			response.addGiftInfo(giftInfo);
		}
		player.sendProtocol(HawkProtocol.valueOf(HP.code.PRESIDENT_GIFT_INFO_S, response));
	}

	/**
	 * 颁发礼包逻辑处理
	 * 
	 * @param player
	 * @param giftCfg
	 * @param playerIds
	 */
	public void sendGiftLogic(Player player, PresidentGiftCfg giftCfg, List<String> playerIds) {
		int giftNumber = LocalRedis.getInstance().getGiftNumber(giftCfg.getId());
		int errCode = sendGiftCheck(playerIds, giftCfg.getTotalNumber(), giftNumber);
		if (errCode != 0) {
			player.sendError(HP.code.PRESIDENT_SEND_GIFT_C_VALUE, errCode, 0);
			return;
		}
		
		logger.info("president sendGift giftCfgId:{}, playerIds:{}", giftCfg.getId(), playerIds);
		
		List<GiftRecord.Builder> recordList = new ArrayList<>();
		ChatParames.Builder chatBuilder = ChatParames.newBuilder().setChatType(ChatType.SPECIAL_BROADCAST).
												setKey(Const.NoticeCfgId.PRESIDENT_GIFT).setPlayer(null);
		StringJoiner sj = new StringJoiner("，");
		// 发放礼包 推送消息 记录
		for (String playerId : playerIds) {
			// 发送邮件---大总统礼包
			AwardCfg awardCfg = HawkConfigManager.getInstance().getConfigByKey(AwardCfg.class, giftCfg.getAwardId());
			SystemMailService.getInstance().sendMail(MailParames.newBuilder()
			        .setPlayerId(playerId)
			        .setMailId(MailId.PRESIDENT_GIFT)
			        .setAwardStatus(MailRewardStatus.NOT_GET)
			        .addSubTitles(giftCfg.getGiftName())
			        .addContents(giftCfg.getGiftName())
			        .setRewards(awardCfg.getRandomAward().getAwardItems())
			        .build());
			
			// 添加日志
			BehaviorLogger.log4Service(player, Source.USER_OPERATION, Action.PRESIDENT_SEND_GIFT, 
					Params.valueOf("receiverId", playerId), Params.valueOf("giftId", giftCfg.getId()));

			// 广播推送消息
			Player sendPlayer = GlobalData.getInstance().makesurePlayer(playerId);
			sj.add(sendPlayer.getName());
			// 添加记录数据
			GiftRecord.Builder giftRecord = GiftRecord.newBuilder();
			giftRecord.setSendTime(HawkTime.getMillisecond());
			giftRecord.setPlayerName(sendPlayer.getName());
			giftRecord.setGiftId(giftCfg.getId());
			giftRecord.setPlayerId(sendPlayer.getId());
			//giftRecord.set
			giftRecord.setGuildTag((sendPlayer.getGuildTag()));
			giftRecord.setPresidentName(player.getName());
			giftRecord.setServerId(player.getMainServerId());
			LocalRedis.getInstance().updateGiftSend(playerId, JsonFormat.printToString(giftRecord.build()));
			
			recordList.add(giftRecord);
		}
		
		
		//合并跑马灯.
		chatBuilder.addParms(giftCfg.getGiftName());
		chatBuilder.addParms(sj.toString());
		ChatService.getInstance().addWorldBroadcastMsg(chatBuilder.build());

		// 礼包个数更新
		LocalRedis.getInstance().updateGiftNumber(giftCfg.getId(), giftNumber + playerIds.size());
		//同步礼物的个数
		syncGiftInfo(player);
		this.updateRecords(player, recordList);
		player.responseSuccess(HP.code.PRESIDENT_SEND_GIFT_C_VALUE);
	}
	
	public Set<String> searchPlayer(Player player) {
		int maxCount = 10;
		List<RankInfo> rankInfos = RankService.getInstance().getRankCache(RankType.PLAYER_FIGHT_RANK);
		Set<String> idSet = new HashSet<>(10);
		for (RankInfo ri : rankInfos) {
			if (idSet.size() >= maxCount) {
				break;
			}
			
			if(player.getId().equals(ri.getId())) {
				continue;
			}
			
			idSet.add(ri.getId());
		}
		
		return idSet;
	}
	
	private void synMember(Player player, Set<String> playerIds) {
		// 构建返回数据包
		PresidentSearchRes.Builder response = PresidentSearchRes.newBuilder();
		for (String playerId : playerIds) {
			Player searchPlayer = GlobalData.getInstance().makesurePlayer(playerId);
			if (null != searchPlayer) {
				MemeberInfo.Builder memeberInfo = MemeberInfo.newBuilder();
				MiniPlayerMsg.Builder miniPlayer = BuilderUtil.genMiniPlayer(playerId);
				memeberInfo.setMiniPlayer(miniPlayer);
				memeberInfo.setOfficer(GameUtil.getOfficerId(playerId));
				//String sendValue = LocalRedis.getInstance().getGiftSend(playerId); 这种写法有点过分.
				memeberInfo.setIsSendGift(true);
				memeberInfo.setBuildingLevel(player.getCityLevel());
				response.addMemeberInfo(memeberInfo);
			}
		}

		player.sendProtocol(HawkProtocol.valueOf(HP.code.PRESIDENT_SEARCH_S, response));
	}
	
	/**
	 * 搜索玩家处理
	 * 
	 * @param player
	 * @param name
	 */
	public void searchMemeber(Player player, String name, int type, boolean precise) {
		Set<String> ignoreSet = new HashSet<>();
		//礼包
		if (type == 1) {
			Map<String, String> map = LocalRedis.getInstance().getAllGiftSend();
			ignoreSet.addAll(map.keySet());			
		} else if (type == 2) {
			//好官职
			List<OfficerEntity> officerEntityList = PresidentOfficier.getInstance().getOfficerEntityList();
			officerEntityList.forEach(officer->{
				if (!HawkOSOperator.isEmptyString(officer.getPlayerId())) {
					ignoreSet.add(officer.getPlayerId());
				}
			});			
		} else if (type == 3) {
			//坏官职.
			List<OfficerEntity> officerEntityList = PresidentOfficier.getInstance().getOfficerEntityList();
			officerEntityList.forEach(officer->{
				if (!HawkOSOperator.isEmptyString(officer.getPlayerId())) {
					ignoreSet.add(officer.getPlayerId());
				}
			});	
			Set<String> counterAttackIds = GuildService.getInstance().guildCounterAttacker(player.getGuildId());
			ignoreSet.addAll(counterAttackIds);
			
		} else if (type == 4) {
			Set<String> rankPlayer = searchFighterRankPlayer();
			synMember(player, rankPlayer);
			
			return;
		}
		
		// type == 5 是指搜索所有的
		if (type != 3 && type != 5) {
			Collection<String> guildIdList = GuildService.getInstance().getGuildMembers(player.getGuildId());
			ignoreSet.addAll(guildIdList);
		}		
		
		int maxCount = PresidentConstCfg.getInstance().getSearchMaxCount();
		List<String> idList = SearchService.getInstance().searchPlayerByNameIgnore(name, 0, 0, maxCount, new ArrayList<>(ignoreSet), precise);
									
		synMember(player, new HashSet<>(idList));		
	}
	
	private Set<String> searchFighterRankPlayer() {
		int maxCount = PresidentConstCfg.getInstance().getSearchMaxCount();
		List<RankInfo> rankInfos = RankService.getInstance().getRankCache(RankType.PLAYER_FIGHT_RANK);
		Set<String> idSet = new HashSet<>(maxCount);
		for (RankInfo ri : rankInfos) {
			if (idSet.size() >= maxCount) {
				break;
			}					
			
			idSet.add(ri.getId());
		}
		
		return idSet;
	}

	public void updateRecords(Player player, List<GiftRecord.Builder> buildList) {
		if (buildList == null || buildList.isEmpty()) {
			return;
		}
		
		PresidentGiftRecordsUpdate.Builder sbuilder = PresidentGiftRecordsUpdate.newBuilder();
		for (GiftRecord.Builder builder : buildList) {
			sbuilder.addGiftRecord(builder.build());
		}
		
		player.sendProtocol(HawkProtocol.valueOf(HP.code.PRESIDENT_GIFT_RECORDS_UPDATE_VALUE, sbuilder));
	}
	/**
	 * 查看颁发礼包发放记录
	 * 
	 * @param player
	 */
	public void giftSendRecord(Player player) {
		PresidentGiftRecordRes.Builder response = PresidentGiftRecordRes.newBuilder();
		Map<String, String> sendMap = LocalRedis.getInstance().getAllGiftSend();
		Iterator<Entry<String, String>> iterator = sendMap.entrySet().iterator();

		while (iterator.hasNext()) {
			Entry<String, String> entry = iterator.next();
			GiftRecord.Builder giftRecord = GiftRecord.newBuilder();

			try {
				JsonFormat.merge(entry.getValue(), giftRecord);
			} catch (ParseException e) {
				HawkException.catchException(e);
			}
			response.addGiftRecord(giftRecord);
		}

		player.sendProtocol(HawkProtocol.valueOf(HP.code.PRESIDENT_GIFT_RECORD_S, response));
	}

	/**
	 * 礼包发放数据校验
	 * 
	 * @param requestList
	 * @param giftId
	 * @param giftNumber
	 * @return
	 */
	private int sendGiftCheck(List<String> playerIds, int totalNumber, int giftNumber) {
		// 数据整理
		Set<String> idSet = new HashSet<String>(playerIds);

		// 是否有相同的账号ID校验
		if (playerIds.size() != idSet.size()) {
			return Status.Error.PRESIDENT_PLAYER_REPEAT_VALUE;
		}

		Map<String, String> gfitRecordMap = LocalRedis.getInstance().getAllGiftSend();
		for (String playerId : playerIds) {
			
			if (!GlobalData.getInstance().isExistPlayerId(playerId)) {
				return Status.SysError.ACCOUNT_NOT_EXIST_VALUE;
			}
			
			if (!GlobalData.getInstance().isLocalPlayer(playerId)) {
				return Status.SysError.ACCOUNT_NOT_EXIST_VALUE;						
			}
			
			if (gfitRecordMap.containsKey(playerId)) {
				return Status.Error.PRESIDENT_PLAYER_GOT_GIFT_VALUE;
			}
		}

		// 礼包剩余个数是否够
		if (totalNumber - giftNumber < playerIds.size()) {
			return Status.Error.PRESIDENT_GIFT_NUMBER_VALUE;
		}

		return 0;
	}

}
