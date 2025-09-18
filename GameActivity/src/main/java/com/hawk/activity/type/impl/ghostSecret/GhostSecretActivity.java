package com.hawk.activity.type.impl.ghostSecret;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkRand;
import org.hawk.result.Result;
import org.hawk.tuple.HawkTuple2;
import org.hawk.tuple.HawkTuple3;
import org.hawk.tuple.HawkTuples;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;
import com.google.common.primitives.Ints;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.ghostSecret.cfg.GhostSecretKVCfg;
import com.hawk.activity.type.impl.ghostSecret.cfg.GhostSecretRewardCfg;
import com.hawk.activity.type.impl.ghostSecret.cfg.GhostSecretWeightCfg;
import com.hawk.activity.type.impl.ghostSecret.entity.GhostSecretEntity;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.MailConst;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.Activity.DrewTreasureResp;
import com.hawk.game.protocol.Activity.DrewType;
import com.hawk.game.protocol.Activity.GhostTreasurePageInfo;
import com.hawk.game.protocol.Activity.ThreeRewardInfo;
import com.hawk.game.protocol.Activity.TreasureInfo;
import com.hawk.game.protocol.Activity.TreasureType;
import com.hawk.game.protocol.Const.ChatType;
import com.hawk.game.protocol.Const.NoticeCfgId;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.log.Action;
import com.hawk.serialize.string.SerializeHelper;

/**
 * @Desc:幽灵秘宝活动
 * @author:Winder
 * @date:2020年5月22日
 */
public class GhostSecretActivity extends ActivityBase{
	public final Logger logger = LoggerFactory.getLogger("Server");
	//中奖三连的二维数组
	private static int[][] rewardIndex = {{1,2,3},{4,5,6},{7,8,9},{1,5,9},{1,4,7},{2,5,8},{3,6,9},{3,5,7}};
	
	public GhostSecretActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.GHOST_SECRET_ACTIVITY;
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		GhostSecretActivity activity  = new GhostSecretActivity(config.getActivityId(), activityEntity);
		return activity;
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<GhostSecretEntity> queryList = HawkDBManager.getInstance()
				.query("from GhostSecretEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			GhostSecretEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		GhostSecretEntity entity = new GhostSecretEntity(playerId, termId);
		return entity;
	}
	
	@Subscribe
	public void onContinueLogin(ContinueLoginEvent event) { //1ab4-1c7kb8-1
		if (!isOpening(event.getPlayerId())) {
			return;
		}
		if (!event.isCrossDay()) {
			return;
		}
		String playerId = event.getPlayerId();
		Optional<GhostSecretEntity> opPlayerDataEntity = getPlayerDataEntity(playerId);
		if (!opPlayerDataEntity.isPresent()) {
			return;
		}
		//跨天重置当日中奖特等奖次数
		GhostSecretEntity entity = opPlayerDataEntity.get();
		entity.setSpecAwardGot(false);
		entity.setResetNum(0);
		entity.notifyUpdate();
		syncActivityDataInfo(playerId);
	}
	
	public String getCardValueByPlayerId(String playerId){
		Optional<GhostSecretEntity> opPlayerDataEntity = getPlayerDataEntity(playerId);
		//跨天重置当日中奖特等奖次数
		GhostSecretEntity entity = opPlayerDataEntity.get();
		String cardInfo = SerializeHelper.collectionToString(entity.getDrewInfoList(), SerializeHelper.ATTRIBUTE_SPLIT);
		return cardInfo;
	}
		
	/** 翻牌
	 * @param playerId
	 * @param cardIndex
	 * @param drewType
	 * @param protoType
	 * @return
	 */
	public Result<?> drewGhostSecret(String playerId, int cardIndex, DrewType drewType, int protoType){
		try {
			if (!isOpening(playerId)) {
				return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
			}
			Optional<GhostSecretEntity> opPlayerDataEntity = getPlayerDataEntity(playerId);
			if (!opPlayerDataEntity.isPresent()) {
				return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
			}
		
			GhostSecretEntity entity = opPlayerDataEntity.get();
			//卡的信息
			List<String> drewInfoList = entity.getDrewInfoList();
			//判断 1次/10次
			if (drewType == DrewType.ONE_TIMES_DIG) {
				int cardVaule = Integer.valueOf(drewInfoList.get(cardIndex - 1));
				if (cardVaule != TreasureType.TYPE_CLOSE_VALUE) {
					logger.info("this card is had drewed cardIndex:{}" , cardIndex - 1);
					return Result.fail(Status.Error.GHOST_SECRET_CARD_IS_DREWD_VALUE);
				}
			}
			//剩余未翻牌
			List<Integer> remainCardList = getRemainCardIndexs(drewInfoList);
			if (remainCardList.isEmpty()) {
				logger.info("all cards is already drewed ");
				return Result.fail(Status.Error.GHOST_SECRET_ALL_CARD_IS_DREWD_VALUE); //TODO
			}
			GhostSecretKVCfg ghostSecretKVCfg = HawkConfigManager.getInstance().getKVInstance(GhostSecretKVCfg.class);
			
			List<RewardItem.Builder> consumeItemList = new ArrayList<RewardItem.Builder>();
			
			//次数
			int times = (drewType == DrewType.ONE_TIMES_DIG) ? 1: remainCardList.size();
			//需要用金条购买的次数
			int needBuyCount = getDrewCardConsume(playerId, times, consumeItemList);
			
			boolean success = getDataGeter().consumeItems(playerId, consumeItemList, protoType, Action.GHOST_SECRET_DREW_CONSUME);
			if (!success) {
				logger.error("GhostSecretActivity drew secret consume not enought, playerId: {}", playerId);
				return Result.fail(Status.Error.ITEM_NOT_ENOUGH_VALUE);
			}
			//特等奖数量,一等奖一天只有一次,如果出现多次,则其它设置为二等奖
			//int specRewardNum = 0;
			List<HawkTuple3<Integer, String, Integer>>  allRewardList = new ArrayList<>();
			for (int i = 0; i < times; i++) {
				//多次时,从剩余的未翻卡里
				if (drewType == DrewType.ONE_KEY_DIG) {
					cardIndex = remainCardList.get(i);
				}
				//翻开的牌值
				int drewCardValue = calculateDrewCardValue(playerId, cardIndex);
				if(drewCardValue == 0){
					String cardValue = getCardValueByPlayerId(playerId);
					logger.error("GhostSecretActivity drew secret error drewCardValue == 0, playerId: {}, cardIndex:{}, cardValue", playerId, cardIndex, cardValue);
					continue;
				}
				//更新此牌的值
				drewInfoList.set((cardIndex - 1), String.valueOf(drewCardValue));
				entity.setDrewNum(entity.getDrewNum() + 1);
				boolean isGetSpac = entity.isSpecAwardGot();
				//计算新的三连的奖励<Index(三连在8组三连中的位置), cardIndex(1_2_3), 奖励Id>
				List<HawkTuple3<Integer, String, Integer>>  rewardInfoList = getDrewThreeRewardInfo(cardIndex, drewInfoList, isGetSpac); //TODO entity修改
				allRewardList.addAll(rewardInfoList);
				//发奖 三连的奖励
				for (HawkTuple3<Integer, String, Integer> threeTuple : rewardInfoList) {
					int rewardId = threeTuple.third;
					GhostSecretRewardCfg rewardCfg = HawkConfigManager.getInstance().getConfigByKey(GhostSecretRewardCfg.class, rewardId);
					if (rewardCfg == null) {
						continue;
					}
					this.getDataGeter().takeReward(playerId, rewardCfg.getRewardList(), Action.GHOST_SECRET_DREW_REWARD, false);
					if (rewardCfg.IsSpec()) {
						//specRewardNum ++;
						String playerName = getDataGeter().getPlayerName(playerId);
						this.addWorldBroadcastMsg(ChatType.SYS_BROADCAST, NoticeCfgId.ACTIVITY_GHOST_SECRET_AWARD,null, playerName);
						entity.setSpecAwardGot(true);
						//特等奖之后重置抽卡次数
						entity.setDrewNum(0);
					}
					//Tlog打点
					this.getDataGeter().logGhostSecretRewardInfo(playerId, entity.getTermId(), rewardId);
					//logger.info("drewGhostSecret  index:{}, cardIndex:{}, rewardId:{}" , threeTuple.first, threeTuple.second, threeTuple.third, entity.getDrewInfoList().toString());
				}
				List<RewardItem.Builder> extReward = RewardHelper.toRewardItemList(ghostSecretKVCfg.getExtReward());
				this.getDataGeter().takeReward(playerId, extReward, Action.GHOST_SECRET_DREW_REWARD, false);
				//随机奖励,邮件发送
				int randomAwardId = ghostSecretKVCfg.getRandReward();
				sendRandomRewardByMail(playerId, randomAwardId);
				
				//幽灵秘宝记录翻牌结果
				this.getDataGeter().logGhostSecretDrewResult(playerId, entity.getTermId(), drewCardValue);
				
				logger.info("DrewGhostSecret success, playerId: {}, cardIndex:{}, drewCardValue:{}", playerId, cardIndex ,drewCardValue);
			}
			entity.notifyUpdate();
			//push GhostTreasurePageInfo
			syncActivityDataInfo(playerId);
			//中奖信息
			DrewTreasureResp.Builder builder = genDrewTreasureResp(allRewardList);
			return Result.success(builder);
		} catch (Exception e) {
			HawkException.catchException(e);
			return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
		}
	}
	
	
	/**一等奖
	 * @return
	 */
	public int getSecondRewardId(){
		ConfigIterator<GhostSecretRewardCfg> ghIterator = HawkConfigManager.getInstance().getConfigIterator(GhostSecretRewardCfg.class);
		while (ghIterator.hasNext()) {
			GhostSecretRewardCfg cfg = ghIterator.next();
			if (cfg.getLevel() == 1) {
				return cfg.getId();
			}
			
		}
		return 0;
	} 
	/**幽灵秘宝重置
	 * @param playerId
	 * @param protoType
	 */
	public void resetGhostSecret(String playerId, int protoType){
		try {
			if (!isOpening(playerId)) {
				return ;
			}
			Optional<GhostSecretEntity> opPlayerDataEntity = getPlayerDataEntity(playerId);
			if (!opPlayerDataEntity.isPresent()) {
				return;
			}
			GhostSecretKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(GhostSecretKVCfg.class);
			
			GhostSecretEntity entity = opPlayerDataEntity.get();
			
			int dailyResetTimes = entity.getResetNum();
			if (dailyResetTimes >= cfg.getDailyResetTimes()) {
				logger.info("GhostSecretActivity reset secret resetTimes is limit, playerId: {}, restTimes:{}", playerId, entity.getResetNum());
				return ;
			}
			int drewedTimes = getHaveDrewedTimes(entity);
			if (drewedTimes == 0) {
				logger.info("GhostSecretActivity reset secret consume not enought, playerId: {}", playerId);
				return ;
			}
			
			List<RewardItem.Builder> counsumeItem = RewardHelper.toRewardItemList(cfg.getResetCost());
			boolean success = getDataGeter().consumeItems(playerId, counsumeItem, protoType, Action.GHOST_SECRET_RESET_CONSUME);
			if (!success) {
				logger.info("GhostSecretActivity reset secret consume not enought, playerId: {}", playerId);
				return;
			}
			//幽灵秘宝重置时记录本轮玩家已翻牌的次数
			this.getDataGeter().logGhostSecretResetInfo(playerId, entity.getTermId(), drewedTimes);
			
			entity.setResetNum(entity.getResetNum() + 1);
			entity.resetDrewInfoList();
			logger.info("GhostSecretActivity reset secret success, playerId: {}, resetNum:{}", playerId, entity.getResetNum());
			//同步
			syncActivityDataInfo(playerId);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	
	@Override
	public void syncActivityDataInfo(String playerId) {
		if (!isOpening(playerId)) {
			return;
		}
		Optional<GhostSecretEntity> opPlayerDataEntity = getPlayerDataEntity(playerId);
		if (!opPlayerDataEntity.isPresent()) {
			return;
		}
		//pb
		GhostTreasurePageInfo.Builder builder = genGhostTreasurePageInfo(opPlayerDataEntity.get());
		PlayerPushHelper.getInstance().pushToPlayer(playerId,HawkProtocol.valueOf(HP.code.GHOST_SECRET_INFO_SYNC_VALUE, builder));
	}
	
	/**获取已翻的次数
	 * @param entity
	 * @return
	 */
	public int getHaveDrewedTimes(GhostSecretEntity entity){
		List<String> drewList = entity.getDrewInfoList();
		int times= 0;
		for (String cardValue : drewList) {
			if (Integer.valueOf(cardValue)!= TreasureType.TYPE_CLOSE_VALUE) {
				times ++;
			}
		}
		return times;
	}
	/** 中奖pb
	 * @param allRewardList
	 * @return
	 */
	public DrewTreasureResp.Builder genDrewTreasureResp(List<HawkTuple3<Integer, String, Integer>>  allRewardList){
		DrewTreasureResp.Builder builder = DrewTreasureResp.newBuilder();
		for (HawkTuple3<Integer, String, Integer> rewardTuple : allRewardList) {
			ThreeRewardInfo.Builder threeBuilder = ThreeRewardInfo.newBuilder();
			threeBuilder.setCardIndex(rewardTuple.second);
			threeBuilder.setRewardId(rewardTuple.third);
			builder.addThreeRewardInfo(threeBuilder);
		}
		return builder;
	}
	/**九宫格信息pb
	 * @param entity
	 * @return
	 */
	public GhostTreasurePageInfo.Builder genGhostTreasurePageInfo(GhostSecretEntity entity){
		GhostSecretKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(GhostSecretKVCfg.class);
		
		GhostTreasurePageInfo.Builder builder = GhostTreasurePageInfo.newBuilder();
		builder.setIsReceiveBest(entity.isSpecAwardGot());
		int remainResetTimes = cfg.getDailyResetTimes() - entity.getResetNum();
		if (remainResetTimes < 0) {
			remainResetTimes = 0;
		}
		builder.setResetTimes(remainResetTimes);
		List<String> drewInfoList = entity.getDrewInfoList();
		for (int i = 0; i < drewInfoList.size(); i++) {
			TreasureInfo.Builder trBuilder = TreasureInfo.newBuilder();
			String drewInfo = drewInfoList.get(i);
			trBuilder.setIndex(i + 1);
			trBuilder.setTreasureType(TreasureType.valueOf(Integer.valueOf(drewInfo)));
			builder.addTreasureInfo(trBuilder);
		}
		return builder;
	}
	
	
	
	public List<Integer> getRemainCardIndexs(List<String> drewInfoList){
		List<Integer> remainCard = new ArrayList<>();
		for (int i = 0; i < drewInfoList.size(); i++) {
			String cardValue = drewInfoList.get(i);
			if (Integer.valueOf(cardValue) == TreasureType.TYPE_CLOSE_VALUE) {
				remainCard.add(i + 1);//下标从1开始
			}
			
		}
		Collections.sort(remainCard);
		return remainCard;
	}
	
	/**计算新的三连的奖励<1, 1_2_3>(下标,三连下标值)
	 * @param cardIndex
	 * @param drewCardValue
	 * @param drewInfoList
	 * @return
	 */
	public List<HawkTuple3<Integer, String, Integer>> getDrewThreeRewardInfo(int cardIndex, List<String> drewInfoList, boolean isGetSpac){
		//计算新的三连的奖励<1, 1_2_3>(下标,三连下标值)
		List<HawkTuple3<Integer, String, Integer>> rewardInfoList = new ArrayList<>();
		List<HawkTuple2<Integer, String>> rewardCardInfo = getThreeCardInfo(cardIndex, drewInfoList);
		//本循环也要排除多次特等奖
		boolean isSpecSign = false;
		for (HawkTuple2<Integer, String> tuple : rewardCardInfo) {
			int index = tuple.first;
			String value = tuple.second;
			//三连下标
			List<Integer> threeValueList = SerializeHelper.cfgStr2List(value);
			//三连牌值
			List<Integer> threeCardValue = new ArrayList<>();
			for (Integer threeIndex : threeValueList) {
				int cardValue = getCardValue(threeIndex - 1, drewInfoList);
				threeCardValue.add(cardValue);
			}
			//根据三连的牌值 计算此三连奖励
			int rewardId = getRewardIdByCards(threeCardValue);
			if (rewardId == 0) {
				continue;
			}
			GhostSecretRewardCfg cfg = HawkConfigManager.getInstance().getConfigByKey(GhostSecretRewardCfg.class, rewardId);
			//此配置是否是特等奖
			boolean isSpac = cfg.IsSpec();
			if (isSpac) {
				//特等奖只中一次(之前中过特等奖 或是 本轮次产生过特等奖)
				if (isGetSpac || isSpecSign) {
					rewardId = getSecondRewardId();
				}
				isSpecSign = true;
			}
			rewardInfoList.add(HawkTuples.tuple(index, value, rewardId));
		}
		return rewardInfoList;
	}
	/** 获取三连卡值,对应的奖励Id
	 * @param threeCardValue
	 * @return
	 */
	public int getRewardIdByCards(List<Integer> threeCardValue){
		Collections.sort(threeCardValue);
		ConfigIterator<GhostSecretRewardCfg> ghIterator = HawkConfigManager.getInstance().getConfigIterator(GhostSecretRewardCfg.class);
		while (ghIterator.hasNext()) {
			GhostSecretRewardCfg cfg = ghIterator.next();
			List<Integer> rewardCardValue = cfg.getCardInfo();
			String rewardCardString = SerializeHelper.collectionToString(rewardCardValue, SerializeHelper.ATTRIBUTE_SPLIT);
			String threeCardString = SerializeHelper.collectionToString(threeCardValue, SerializeHelper.ATTRIBUTE_SPLIT);
			if (rewardCardString.equals(threeCardString)) {
				return cfg.getId();
			}
		}
		return 0;
	}
	
	/** 发送随机奖励mail
	 * @param playerId
	 * @param rewardId
	 */
	public void sendRandomRewardByMail(String playerId, int rewardId){
		try {
			List<String> rewardList = getDataGeter().getAwardFromAwardCfg(rewardId);
			List<RewardItem.Builder> rewardItemList = new ArrayList<>();
			for (String rewardStr : rewardList) {
				List<RewardItem.Builder> rewardBuilders = RewardHelper.toRewardItemList(rewardStr);
				rewardItemList.addAll(rewardBuilders);
			}
			// 邮件发送奖励
			Object[] content = new Object[0];
			Object[] title = new Object[0];
			Object[] subTitle = new Object[0];
			//发邮件
			sendMailToPlayer(playerId, MailConst.MailId.GHOST_SECRET_EXTRA_REWARD, title, subTitle, content, rewardItemList);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**翻牌消耗
	 * @param playerId
	 * @param drewTimes
	 * @param consumeItemList
	 * @return
	 */
	private int getDrewCardConsume(String playerId, int drewTimes, List<RewardItem.Builder> consumeItemList) {
		GhostSecretKVCfg ghostSecretKVCfg = HawkConfigManager.getInstance().getKVInstance(GhostSecretKVCfg.class);
		//单次消耗
		RewardItem.Builder drewCounsumeItem = RewardHelper.toRewardItem(ghostSecretKVCfg.getTreasureCost());
		int drewCounsumItemId = drewCounsumeItem.getItemId();
		int haveDrewCount = this.getDataGeter().getItemNum(playerId, drewCounsumItemId);
		
		int totalCount = drewTimes*  (int)drewCounsumeItem.getItemCount();
		//需要购买的次数
		int needBuyCount = totalCount - haveDrewCount;
		
		if (needBuyCount > 0) {
			RewardItem.Builder buyCounsumeItem = RewardHelper.toRewardItem(ghostSecretKVCfg.getItemOnecePrice());
			buyCounsumeItem.setItemCount(buyCounsumeItem.getItemCount() * needBuyCount);
			consumeItemList.add(buyCounsumeItem);
			if (haveDrewCount > 0) {
				drewCounsumeItem.setItemCount(haveDrewCount);
				consumeItemList.add(drewCounsumeItem);
			}
		}else{
			drewCounsumeItem.setItemCount(totalCount);
			consumeItemList.add(drewCounsumeItem);
		}
		return needBuyCount;
		
	}
	
	
	
	/**
	 * 翻开当前卡牌,计算三连中已经出现S的数量
	 * 多个三连出现S的时候,取最多的那个计算
	 */
	public int calculateDrewCardValue(String playerId, int cardIndex){
		Optional<GhostSecretEntity> opPlayerDataEntity = getPlayerDataEntity(playerId);
		GhostSecretEntity entity = opPlayerDataEntity.get();
		List<String> cardInfo = entity.getDrewInfoList();
		//三连已经有s的个数,取最多那个
		int appearThreeNum = getThreeSpecCardNum(cardIndex, cardInfo);
		//今天是否中过大奖
		boolean specAwardGot = entity.isSpecAwardGot();
		//累计挖掘次数
		int drewTimes = entity.getDrewNum();
		//随机翻开的牌值
		int cardValue = getRandomCardSign(specAwardGot, drewTimes, appearThreeNum);
		return cardValue;
	}
	
	
	/**获取此位置的三连中,已经翻开S的次数,(多组取最大数)
	 * @param cardIndex
	 * @return
	 */
	public int getThreeSpecCardNum(int cardIndex, List<String> playerCardInfo){
		List<Integer> maxNumList = new ArrayList<>();
		for (int i = 0; i < rewardIndex.length; i++) {
			int[] js = rewardIndex[i];
			List<Integer> threeCardList = Ints.asList(js); //{1,2,3}index下标
			//三连包含此index
			if (threeCardList.contains(cardIndex)) {
				int maxNum = 0;
				//计算这个三连已经翻开几个S卡
				for (Integer threeIndex : threeCardList) {
					int num = Integer.valueOf(playerCardInfo.get(threeIndex - 1));
					if (num == TreasureType.TYPE_S_VALUE) {
						maxNum ++;
					}
				}
				maxNumList.add(maxNum);
			}
		}
		Collections.sort(maxNumList);
		int max = maxNumList.get(maxNumList.size() - 1);
		return max;
	}
	
	/** 计算中奖信息Map<Integer, String>(下标,三连下标值)
	 * @param cardIndex
	 * @param cardValue
	 * @param playerCardInfo
	 * @return
	 */
	public List<HawkTuple2<Integer, String>> getThreeCardInfo(int cardIndex, List<String> playerCardInfo){
		List<HawkTuple2<Integer, String>> suitCardList = new ArrayList<>();
		for (int i = 0; i < rewardIndex.length; i++) {
			int[] js = rewardIndex[i];
			List<Integer> threeCardList = Ints.asList(js); //{1,2,3}index下标
			//三连包含此index
			if (threeCardList.contains(cardIndex)) {
				boolean isSuit = true;
				//计算这个三连是否全部翻开
				for (Integer threeIndex : threeCardList) {
					int num = Integer.valueOf(playerCardInfo.get(threeIndex - 1));
					if (num == TreasureType.TYPE_CLOSE_VALUE) {
						isSuit = false;
						break;
					}
				}
				//符合条件
				if (isSuit) {
					String cardStr = SerializeHelper.collectionToString(threeCardList, SerializeHelper.ATTRIBUTE_SPLIT);
					//下标从1开始算
					suitCardList.add(HawkTuples.tuple(i + 1, cardStr));
				}
			}
		}
		return suitCardList;
	}
	
	
	public int getCardValue(int cardIndex,  List<String> playerCardInfo){
		return Integer.valueOf(playerCardInfo.get(cardIndex));
	}
	/**
	 * 获取翻卡牌的标记
	 * @param specAwardGot
	 * @param drewTimes
	 * @param specCardCount
	 * @return
	 */
	private int getRandomCardSign(boolean specAwardGot, int drewTimes, int specCardCount){
		ConfigIterator<GhostSecretWeightCfg> configItrator = HawkConfigManager.getInstance().getConfigIterator(GhostSecretWeightCfg.class);
		while(configItrator.hasNext()){
			GhostSecretWeightCfg cfg = configItrator.next();
			if (cfg.getSpecAwardGot() == specAwardGot
					&& cfg.getSpecCardCount() == specCardCount 
					&& (drewTimes >= cfg.getDrewMin() 
					&& drewTimes <= cfg.getDrewMax())) {
				
				int cardSign = HawkRand.randomWeightObject(cfg.getCardWeightMap());
				//logger.info("DrewGhostSecret getRandomCardSign, cardValue: {}, cardWeight:{}", cardSign, cfg.getCardWeight());
				return cardSign;
			}
		}
		logger.error("GhostSecretWeightCfg can not found specAwardGot:{} ,drewTimes:{},  specCardCount:{}",specAwardGot ,drewTimes ,specCardCount);
		return 0;
	}
	

}
