package com.hawk.activity.type.impl.aftercompetition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import org.hawk.app.HawkApp;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.redis.HawkRedisSession;
import org.hawk.uuid.HawkUUIDGenerator;

import com.alibaba.fastjson.JSONObject;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.event.impl.AfterCompetitionGiftRecEvent;
import com.hawk.activity.event.impl.AfterCompetitionHomageRefreshEvent;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.type.impl.aftercompetition.cfg.AfterCompetitionConstCfg;
import com.hawk.activity.type.impl.aftercompetition.cfg.AfterCompetitionShopCfg;
import com.hawk.activity.type.impl.aftercompetition.data.ChampionGuildInfo;
import com.hawk.activity.type.impl.aftercompetition.data.GiftBigAwardInfo;
import com.hawk.activity.type.impl.aftercompetition.data.RecGiftInfo;
import com.hawk.activity.type.impl.aftercompetition.data.SendGiftInfo;
import com.hawk.activity.type.impl.aftercompetition.entity.ACGiftItem;
import com.hawk.activity.type.impl.aftercompetition.entity.AfterCompetitionEntity;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.Activity.ACGiftInfoPB;
import com.hawk.game.protocol.Activity.AfterCompetitionActivityInfo;
import com.hawk.game.protocol.Activity.BigAwardPermissionPB;
import com.hawk.game.protocol.Activity.GiftRecAmountPB;
import com.hawk.game.protocol.Const.ChatType;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.log.LogConst.LogInfoType;

/**
 * 礼物管理
 * 
 * @author lating
 *
 */
public class AfterCompetitionGiftManager {
	/**
	 * 系统是否已发放过全服大奖
	 */
	private boolean systemSendBigReward = false;
	/**
	 * 礼包信息
	 */
	protected Map<Integer, GiftBigAwardInfo> giftInfoMap = new ConcurrentHashMap<>();
	/**
	 * 活动类
	 */
	private AfterCompetitionActivity activity;
	

	public AfterCompetitionGiftManager(AfterCompetitionActivity activity) {
		this.activity = activity;
	}
	
	/**
	 * 起服初始化礼物相关信息
	 */
	protected void initGiftInfo() {
		Map<String, String> giftBuyCountMap = getRedis().hGetAll(activity.getGiftBuyCountKey());
		Map<String, String> giftUnlockTimes = getRedis().hGetAll(activity.getGiftUnlockKey());
		for (Entry<String, String> entry : giftUnlockTimes.entrySet()) {
			int giftId = Integer.parseInt(entry.getKey());
			long unlockTime = Long.parseLong(entry.getValue());
			int buyCount = Integer.parseInt(giftBuyCountMap.getOrDefault(entry.getKey(), "0"));
			giftInfoMap.put(giftId, new GiftBigAwardInfo(giftId, unlockTime, buyCount));
			refreshGiftInfo(giftInfoMap.get(giftId), true);
		}
	}
	
	/**
	 * 检测是否要让系统发大奖
	 */
	protected void systemSendBigRewardCheck() {
		long currTime = HawkApp.getInstance().getCurrentTime();
		//活动结束前X小时，系统自动将所有未发送的冠军大赏发放（分别以手Q冠军联盟盟主、微信冠军联盟盟主的名义）
		long achieveEndAutoSendTime = AfterCompetitionConstCfg.getInstance().getAchieveEndAutoSendTime();
		if (!systemSendBigReward && activity.getEndTime() - currTime <= achieveEndAutoSendTime) {
			systemSendBigReward = true;
			systemDistBigAward();
		}
	}
	
	/**
	 * 检测是否解锁出新的礼包
	 * @param homageValGlobal
	 */
	protected void newGiftUnlockCheck(int homageValGlobal) {
		long currTime = HawkApp.getInstance().getCurrentTime();
		ConfigIterator<AfterCompetitionShopCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(AfterCompetitionShopCfg.class);
		while (iterator.hasNext()) {
			AfterCompetitionShopCfg giftCfg = iterator.next();
			//解锁新的礼包
			if (homageValGlobal >= giftCfg.getHomageValue() && !giftInfoMap.containsKey(giftCfg.getId())) {
				giftInfoMap.put(giftCfg.getId(), new GiftBigAwardInfo(giftCfg.getId(), currTime, 0));
				getRedis().hSetNx(activity.getGiftUnlockKey(), String.valueOf(giftCfg.getId()), String.valueOf(currTime));
				getRedis().expire(activity.getGiftUnlockKey(), activity.getRedisExpire());
			}
		}
		
		Set<String> onlinePlayers = activity.getDataGeter().getOnlinePlayers();
		for(String playerId : onlinePlayers){
			ActivityManager.getInstance().postEvent(new AfterCompetitionHomageRefreshEvent(playerId, homageValGlobal));
		}
	}
	
	/**
	 * 检测是否更新礼包信息
	 */
	protected void giftInfoUpdateCheck() {
		Map<String, String> giftBuyCountMap = null;
		for (GiftBigAwardInfo giftInfo : giftInfoMap.values()) {
			if (giftBuyCountMap == null) {
				 giftBuyCountMap = getRedis().hGetAll(activity.getGiftBuyCountKey());
			}
			int buyCount = Integer.parseInt(giftBuyCountMap.getOrDefault(String.valueOf(giftInfo.getGiftId()), "0"));
			giftInfo.setGlobalBuyCount(buyCount);
			refreshGiftInfo(giftInfo, false);
		}
	}
	
	/**
	 * 刷新礼物数据
	 * @param giftInfo
	 */
	protected void refreshGiftInfo(GiftBigAwardInfo giftInfo, boolean init) {
		AfterCompetitionShopCfg giftCfg = HawkConfigManager.getInstance().getConfigByKey(AfterCompetitionShopCfg.class, giftInfo.getGiftId());
		if (giftCfg == null || giftInfo.getGlobalBuyCount() < giftCfg.getBuyGoodsNeedCount()) {
			return;
		}
		
		int sendTotal = giftInfo.getGlobalBuyCount() / giftCfg.getBuyGoodsNeedCount();
		if (giftInfo.getQQSendAwardIds().size() < sendTotal) {
			BigAwardPermissionPB.Builder qqBuilder = buildPermissionInfo(null, AfterCompetitionConst.CHANNEL_QQ);
			Set<String> uuidSet = getRedis().sMembers(activity.getGlobalGiftKey(AfterCompetitionConst.CHANNEL_QQ, giftInfo.getGiftId())); //TODO
			if (qqBuilder != null) {
				for (String uuid : uuidSet) {
					boolean send = false;
					if (!init && !giftInfo.getQQSendAwardIds().contains(uuid)) {
						send = true;
						activity.getDataGeter().addWorldBroadcastMsg(ChatType.CHAT_WORLD, qqBuilder.getGuildId(), Const.NoticeCfgId.AFTER_COMPETITION_PARTY_BIG_AWARD, 
								qqBuilder.getSenderId(), giftInfo.getGiftId(), uuid);
					}
					HawkLog.logPrintln("AfterCompetitionActivity giftManager refreshGift new, serverId: {}, playerId: {}, guildId: {}, giftId: {}, channel: {}, awardUuid: {}, send: {}", 
							activity.getDataGeter().getServerId(), qqBuilder.getSenderId(), qqBuilder.getGuildId(), giftInfo.getGiftId(), "qq", uuid, send);
				}
			}
			giftInfo.getQQSendAwardIds().addAll(uuidSet);
		}
		
		if (giftInfo.getWXSendAwardIds().size() < sendTotal) {
			BigAwardPermissionPB.Builder wxBuilder = buildPermissionInfo(null, AfterCompetitionConst.CHANNEL_WX);
			Set<String> uuidSet = getRedis().sMembers(activity.getGlobalGiftKey(AfterCompetitionConst.CHANNEL_WX, giftInfo.getGiftId()));
			if (wxBuilder != null) {
				for (String uuid : uuidSet) {
					boolean send = false;
					if (!init && !giftInfo.getWXSendAwardIds().contains(uuid)) {
						send = true;
						activity.getDataGeter().addWorldBroadcastMsg(ChatType.CHAT_WORLD, wxBuilder.getGuildId(), Const.NoticeCfgId.AFTER_COMPETITION_PARTY_BIG_AWARD, 
								wxBuilder.getSenderId(), giftInfo.getGiftId(), uuid);
					}
					HawkLog.logPrintln("AfterCompetitionActivity giftManager refreshGift new, serverId: {}, playerId: {}, guildId: {}, giftId: {}, channel: {}, awardUuid: {}, send: {}", 
							activity.getDataGeter().getServerId(), wxBuilder.getSenderId(), wxBuilder.getGuildId(), giftInfo.getGiftId(), "wx", uuid, send);
				}
			}
			giftInfo.getWXSendAwardIds().addAll(uuidSet);
		}
	}
	
	/**
	 * 系统发放全服大奖
	 */
	protected void systemDistBigAward() {
		try {
			BigAwardPermissionPB.Builder qqBuilder = buildPermissionInfo(null, AfterCompetitionConst.CHANNEL_QQ);
			BigAwardPermissionPB.Builder wxBuilder = buildPermissionInfo(null, AfterCompetitionConst.CHANNEL_WX);
			for (GiftBigAwardInfo giftInfo : giftInfoMap.values()) {
				int giftId = giftInfo.getGiftId();
				int buyCount = giftInfo.getGlobalBuyCount();
				distBigAward(giftId, buyCount, AfterCompetitionConst.CHANNEL_QQ, qqBuilder.getGuildId(), qqBuilder.getSenderId(), true);
				distBigAward(giftId, buyCount, AfterCompetitionConst.CHANNEL_WX, wxBuilder.getGuildId(), wxBuilder.getSenderId(), true);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 个人发放全服大奖
	 * @param channel
	 * @param playerId
	 * @param guildId
	 */
	protected Set<String> playerDistBigAward(int channel, String playerId, String guildId, int giftId) {
		GiftBigAwardInfo giftInfo = giftInfoMap.get(giftId);
		if (giftInfo != null) {
			int buyCount = giftInfo.getGlobalBuyCount();
			return distBigAward(giftId, buyCount, channel, guildId, playerId, false);
		}
		
		return Collections.emptySet();
	}
	
	/**
	 * 发放全服大奖
	 * @param giftId
	 * @param buyCount
	 * @param channel
	 * @param guildId
	 * @param playerId
	 */
	protected Set<String> distBigAward(int giftId, int buyCount, int channel, String guildId, String playerId, boolean system) {
		if(!activity.getDataGeter().checkPlayerExist(playerId)) {
			return Collections.emptySet();
		}
		
		AfterCompetitionShopCfg giftCfg = HawkConfigManager.getInstance().getConfigByKey(AfterCompetitionShopCfg.class, giftId);
		if (giftCfg == null || buyCount < giftCfg.getBuyGoodsNeedCount()) {
			return Collections.emptySet();
		}
		int needCount = giftCfg.getBuyGoodsNeedCount();
		Set<String> uuidSet = giftInfoMap.get(giftId).getSendAwardIds(channel);
		if (uuidSet.size() >= buyCount/needCount) {
			return Collections.emptySet();
		}
		
		Set<String> set = new HashSet<>();
		String globalGiftKey = activity.getGlobalGiftKey(channel, giftId);
		for (int i = uuidSet.size() + 1; i <= buyCount/needCount; i++) {
			String awardUuid = HawkUUIDGenerator.genUUID();
			set.add(awardUuid);
			getRedis().sAdd(globalGiftKey, activity.getRedisExpire(), awardUuid);
			activity.getDataGeter().addWorldBroadcastMsg(ChatType.CHAT_WORLD, guildId, Const.NoticeCfgId.AFTER_COMPETITION_PARTY_BIG_AWARD, playerId, giftId, awardUuid); //TODO
		}
		
		if (!set.isEmpty()) {
			uuidSet.addAll(set);
		}
		
		HawkLog.logPrintln("AfterCompetitionActivity giftManager distBigAward, serverId: {}, system: {}, playerId: {}, guildId: {}, giftId: {}, buyCount: {}, channel: {}, awardUuid: {}", 
				activity.getDataGeter().getServerId(), system, playerId, guildId, giftId, buyCount, channel, set);
		
		String globalCount = getRedis().hGet(activity.getGiftBuyCountKey(), String.valueOf(giftId));
		int globalBuyTotal = globalCount == null ? 0 : Integer.parseInt(globalCount);
		//发放全服大赏
		Map<String, Object> param = new HashMap<>();
        param.put("giftId", giftId);                 //礼包id
        param.put("channel", channel);               //渠道
        param.put("guildId", guildId);               //联盟id
        param.put("globalBuyCache", buyCount);       //当前礼包全服购买次数（缓存数据）
        param.put("globalBuy", globalBuyTotal);      //当前礼包全服购买次数
        param.put("systemDist", system ? 1 : 0);     //当前礼包全服购买次数
        param.put("awardUuid", set);                 //发放的奖励uuid
        activity.getDataGeter().logActivityCommon(playerId, LogInfoType.after_comp_dist_bigaward, param);
		return set;
	}
	
	/**
	 * 记录送礼信息
	 * @param entity
	 * @param giftId
	 * @param targetPlayerId
	 */
	protected void addSendRecord(AfterCompetitionEntity entity, int giftId, String targetPlayerId, String itemInfo) {
		//需要有赠礼记录，记录自己送出赠礼时：赠礼目标、赠礼内容、赠礼时间
		long timeNow = HawkTime.getMillisecond();
		String toPlayerName = activity.getDataGeter().getPlayerName(targetPlayerId);
		SendGiftInfo sendRecord = new SendGiftInfo(targetPlayerId, toPlayerName, giftId, timeNow, itemInfo);
		entity.addSendGiftRecord(sendRecord);
		getRedis().lPush(activity.getGiftSendRecordKey(entity.getPlayerId()), activity.getRedisExpire(), JSONObject.toJSONString(sendRecord));
	}
	
	/**
	 * 给目标玩家赠礼
	 * @param playerId
	 * @param giftCfg
	 * @param targetPlayerId
	 */
	protected void sendToPlayer(String playerId, AfterCompetitionShopCfg giftCfg, String targetPlayerId) {
		long timeNow = HawkTime.getMillisecond();
		String fromPlayerName = activity.getDataGeter().getPlayerName(playerId);
		List<RewardItem.Builder> rewardList = new ArrayList<>();
		String itemInfo = getGiftRewardItems(giftCfg.getGiveGoods(), rewardList);
		Object[] content = new Object[2];
		content[0] = fromPlayerName;  //赠送礼物的玩家id
		content[1] = giftCfg.getId(); //礼物id
		activity.sendMailToPlayer(targetPlayerId, MailId.ACTIVITY_371_RECIEVE_GIFT, null, null, content, rewardList); //给赠送对象发奖励邮件
		
		//自己收到赠礼时：发送者信息、赠礼内容、赠礼时间
		RecGiftInfo recRecord = new RecGiftInfo(playerId, fromPlayerName, giftCfg.getId(), timeNow, itemInfo);
		getRedis().lPush(activity.getGiftRecRecordKey(targetPlayerId), activity.getRedisExpire(), JSONObject.toJSONString(recRecord));
		
		//判断接收者是否在线，在线给ta发消息
		if (activity.getDataGeter().isOnlinePlayer(targetPlayerId)) {
			ActivityManager.getInstance().postEvent(new AfterCompetitionGiftRecEvent(targetPlayerId, playerId, fromPlayerName, giftCfg.getId(), timeNow, itemInfo));
		}
	}
	
	/**
	 * 获取礼物物品信息
	 * @param rewardId
	 * @return
	 */
	protected String getGiftRewardItems(int rewardId, List<RewardItem.Builder> rewardItemList){
		List<String> rewardList = activity.getDataGeter().getAwardFromAwardCfg(rewardId);
		StringJoiner sj = new StringJoiner(",");
		for (String rewardStr : rewardList) {
			sj.add(rewardStr);
			List<RewardItem.Builder> rewardBuilders = RewardHelper.toRewardItemImmutableList(rewardStr);
			rewardItemList.addAll(rewardBuilders);
		}
		return sj.toString();
	}
	
	/**
	 * 刷新个人礼物信息
	 * @param playerId
	 * @param entity
	 */
	protected void refreshPlayerGiftInfo(String playerId, AfterCompetitionEntity entity) {
		Map<String, String> map = getRedis().hGetAll(activity.getGiftRecKey(playerId));
		for (Entry<String, String> entry : map.entrySet()) {
			int giftId = Integer.parseInt(entry.getKey()), count = Integer.parseInt(entry.getValue());
			ACGiftItem giftInfo = entity.getGiftInfo(giftId);
			if (giftInfo == null) {
				giftInfo = entity.addGiftItem(giftId);
			}
			giftInfo.setSelfRecCount(count);
		}
		
		activity.syncActivityInfo(playerId, entity);
		
		List<String> sendRecords = getRedis().lRange(activity.getGiftSendRecordKey(playerId), 0, -1, 0);
		entity.getSendGiftRecordList().clear();
		for (String record : sendRecords) {
			entity.addSendGiftRecord(JSONObject.parseObject(record, SendGiftInfo.class));
		}
		
		List<String> recRecords = getRedis().lRange(activity.getGiftRecRecordKey(playerId), 0, -1, 0);
		entity.getRecGiftRecordList().clear();
		for (String record : recRecords) {
			entity.addRecGiftRecord(JSONObject.parseObject(record, RecGiftInfo.class));
		}
		
		entity.notifyUpdate();
	}
	
	
	protected GiftBigAwardInfo getGiftInfo(int giftId) {
		return giftInfoMap.get(giftId);
	}
	
	public HawkRedisSession getRedis() {
		return activity.getRedis();
	}
	
	/**
	 * 构建发放大奖权限信息
	 * @param playerId
	 * @param channel
	 * @return
	 */
	protected BigAwardPermissionPB.Builder buildPermissionInfo(String playerId, int channel) {
		for (int rank = 1; rank <= 10; rank++) {
			ChampionGuildInfo guildInfo = activity.getGuildInfo(channel, rank);
			if (guildInfo == null) {
				continue;
			}
			if (HawkOSOperator.isEmptyString(guildInfo.getLeaderId())) {
				HawkLog.logPrintln("AfterCompetitionGiftManager buildPermissionInfo, guild leader null, channel: {}, rank: {}, guildId: {}", channel, rank, guildInfo.getGuildId());
				continue;
			}
			BigAwardPermissionPB.Builder builder = BigAwardPermissionPB.newBuilder();
			builder.setGuildId(guildInfo.getGuildId());
			builder.setGuildName(guildInfo.getGuildName());
			builder.setSenderId(guildInfo.getLeaderId());
			builder.setSenderName(guildInfo.getLeaderName());
			builder.setSenderIcon(guildInfo.getLeaderIcon());
			builder.setSenderPfIcon(guildInfo.getLeaderPfIcon() == null ? "" : guildInfo.getLeaderPfIcon());
			builder.setPermission(guildInfo.getLeaderId().equals(playerId) ? 1 : 0);
			return builder;
		}
		
		return null;
	}
	
	/**
	 * 构建礼物信息
	 * @param entity
	 * @param builder
	 */
	protected void buildGiftInfo(AfterCompetitionEntity entity, AfterCompetitionActivityInfo.Builder builder) {
		for (GiftBigAwardInfo giftInfo : giftInfoMap.values()) {
			ACGiftItem giftItem = entity.getGiftInfo(giftInfo.getGiftId());
			if (giftItem == null) {
				giftItem = entity.addGiftItem(giftInfo.getGiftId());
			}
			
			ACGiftInfoPB.Builder giftBuilder = giftItem.toBuilder(giftInfo.getGlobalBuyCount());
			giftBuilder.addAllSendBigRewardQQ(giftInfo.getQQSendAwardIds());
			giftBuilder.addAllSendBigRewardWX(giftInfo.getWXSendAwardIds());
			
			if (!HawkOSOperator.isEmptyString(giftItem.getDefaultSendPlayer())) {
				String redisKey = activity.getGiftRecKey(giftItem.getDefaultSendPlayer());
				Map<String, String> map = getRedis().hGetAll(redisKey);
				for (Entry<String, String> entry : map.entrySet()) {
					GiftRecAmountPB.Builder b = GiftRecAmountPB.newBuilder();
					b.setGiftId(Integer.parseInt(entry.getKey()));
					b.setCount(Integer.parseInt(entry.getValue()));
					giftBuilder.addGiftRecInfo(b);
				}
			}
			
			builder.addGiftInfo(giftBuilder);
		}
	}
	
}
