package com.hawk.game.module;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.hawk.annotation.MessageHandler;
import org.hawk.annotation.ProtocolHandler;
import org.hawk.app.HawkApp;
import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.util.service.HawkCdkService;

import com.alibaba.fastjson.JSONObject;
import com.hawk.activity.event.MsgArgsCallBack;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.msg.PlayerRewardByIdFromActivityMsg;
import com.hawk.activity.msg.PlayerRewardFromActivityMsg;
import com.hawk.game.config.AwardCfg;
import com.hawk.game.config.ConstProperty;
import com.hawk.game.config.GameConstCfg;
import com.hawk.game.config.SpecialCdkCfg;
import com.hawk.game.data.BubbleRewardInfo;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.LocalRedis;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.item.AwardItems;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.log.BehaviorLogger;
import com.hawk.game.log.BehaviorLogger.Params;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerModule;
import com.hawk.game.protocol.Cdk.HPUseCdk;
import com.hawk.game.protocol.Cdk.HPUseCdkRet;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Reward.BubbleRewardReq;
import com.hawk.game.protocol.Reward.RewardInfo;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.game.protocol.Reward.RewardOrginType;
import com.hawk.game.protocol.Status;
import com.hawk.game.queryentity.AccountInfo;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst.DiamondPresentReason;
import com.hawk.log.Action;
import com.hawk.log.Source;

/**
 * 奖励相关
 * 
 * @author lating
 *
 */
public class PlayerRewardModule extends PlayerModule {
	
	@Override
	public boolean onTick() {
		return super.onTick();
	}
	
	/**
	 * 构造
	 * 
	 * @param player
	 */
	public PlayerRewardModule(Player player) {
		super(player);
	}
	
	@Override
	protected boolean onPlayerAssemble() {
		Set<Integer> bubbleTypes = ConstProperty.getInstance().getBubbleTypes();
		AccountInfo accountInfo = GlobalData.getInstance().getAccountInfoByPlayerId(player.getId());
		if (accountInfo != null && accountInfo.isNewly()) {
			Map<Integer, BubbleRewardInfo> map = LocalRedis.getInstance().batchUpdateBubbleRewardInfo(player.getId(), bubbleTypes);
			for (Entry<Integer, BubbleRewardInfo> entry : map.entrySet()) {
				player.getData().updateBubbleRewardInfo(entry.getKey(), entry.getValue());
			}
			
		} else {
			
			try {
				for (Integer type : bubbleTypes) {
					BubbleRewardInfo bubbleInfo = LocalRedis.getInstance().getBubbleRewardInfo(player.getId(), type);
					if (bubbleInfo == null) {
						ItemInfo item = ConstProperty.getInstance().randomBubbleAwardItem(type);
						bubbleInfo = new BubbleRewardInfo(type, item);
						LocalRedis.getInstance().updateBubbleRewardInfo(player.getId(), bubbleInfo);
					} else if(!HawkTime.isSameDay(HawkApp.getInstance().getCurrentTime(), bubbleInfo.getLastTime())) {
						bubbleInfo.setLastTime(HawkApp.getInstance().getCurrentTime());
						bubbleInfo.setGotTimes(0);
						LocalRedis.getInstance().updateBubbleRewardInfo(player.getId(), bubbleInfo);
					}
					
					player.getData().updateBubbleRewardInfo(type, bubbleInfo);
				}
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
			
 		
		return true;
	}
	
	/**
	 * 玩家登陆处理(数据同步)
	 */
	@Override
	protected boolean onPlayerLogin() {
		player.getPush().syncBubbleRewardInfo(-1);
		return true;
	}

	/**
	 * 使用cdk
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.USE_CDK_C_VALUE)
	protected boolean onUseCdk(HawkProtocol protocol) {
		// 协议解析
		HPUseCdk cmd = protocol.parseProtocol(HPUseCdk.getDefaultInstance());
		
		// 特殊类型cdk检测
		if (rewardSpecialCdks(cmd.getCdkey())) {
			return true;
		}
		
		// 处理cdk字符串
		String cdk = cmd.getCdkey().trim().toLowerCase();
		
		int status = HawkCdkService.CDK_STATUS_OK;
		String type = HawkCdkService.getTypeNameFromCdk(cdk);

		// 判断是否使用过同类cdkey
		if (!GameConstCfg.getInstance().isCycleCdkType(type) && RedisProxy.getInstance().checkCdkTypeUsed(player.getId(), type)) {
			status = HawkCdkService.CDK_STATUS_TYPE_USED;
		}

		// 回复协议
		HPUseCdkRet.Builder builder = HPUseCdkRet.newBuilder();
		// 使用cdk
		StringBuilder cdkString = new StringBuilder();
		if (status == HawkCdkService.CDK_STATUS_OK) {
			JSONObject attrJson = new JSONObject();
			attrJson.put("platform", player.getPlatform());
			attrJson.put("channel", player.getChannel());
			status = HawkCdkService.getInstance().useCdk(player.getId(), attrJson.toJSONString(), cdk, cdkString);
			if (status == HawkCdkService.CDK_STATUS_OK) {
				// 发放奖励
				AwardItems awardItems = AwardItems.valueOf(cdkString.toString());
				if (awardItems != null) {
					RewardInfo.Builder rewardBuilder = awardItems.rewardTakeAffectAndPush(player, Action.USE_CDK, false);
					builder.setReward(rewardBuilder == null ? RewardInfo.newBuilder() : rewardBuilder);
				}

				//设置用户使用cdk类型
				RedisProxy.getInstance().updateCdkUsed(player.getId(), type, 1);
			}
		}

		builder.setCdkey(cdk);
		builder.setStatus(status);
		sendProtocol(HawkProtocol.valueOf(HP.code.USE_CDK_S, builder));

		// 日志记录
		BehaviorLogger.log4Service(player, Source.USER_OPERATION, Action.USE_CDK, 
				Params.valueOf("cdk", cdk), Params.valueOf("status", status), Params.valueOf("reward", cdkString.toString()));

		return true;
	}

	/**
	 * 特殊cdk
	 * 
	 * @param cdk
	 * @return
	 */
	private boolean rewardSpecialCdks(String cdk) {
		
		// 取CDK数据
		SpecialCdkCfg cdkCfg = HawkConfigManager.getInstance().getConfigByKey(SpecialCdkCfg.class, cdk);
		if(cdkCfg == null) {
			return false;
		}
		
		long now = HawkTime.getMillisecond();
		if(cdkCfg.getStartTime() > now || cdkCfg.getEndTime() < now) {
			return false;
		}
		
		// 查询是否使用过
		int status = HawkCdkService.CDK_STATUS_OK;

		// 检查是否使用
		if (RedisProxy.getInstance().checkCdkTypeUsed(player.getId(), cdk)) {
			status = HawkCdkService.CDK_STATUS_TYPE_USED;
		}

		// 回复协议
		HPUseCdkRet.Builder builder = HPUseCdkRet.newBuilder();
		
		// 发放奖励
		if (status == HawkCdkService.CDK_STATUS_OK) {
			AwardItems awardItems = cdkCfg.getAwardItems();
			if (awardItems != null) {
				RewardInfo.Builder rewardBuilder = awardItems.rewardTakeAffectAndPush(player, Action.USE_CDK, false);
				builder.setReward(rewardBuilder == null ? RewardInfo.newBuilder() : rewardBuilder);
				// 日志记录
				BehaviorLogger.log4Service(player, Source.USER_OPERATION, Action.USE_CDK, 
						Params.valueOf("cdk", cdk), Params.valueOf("status", status), Params.valueOf("reward", awardItems.toString()));
			}

			//设置用户使用cdk类型
			RedisProxy.getInstance().updateCdkUsed(player.getId(), cdk, 1);
		}

		builder.setCdkey(cdk);
		builder.setStatus(status);
		sendProtocol(HawkProtocol.valueOf(HP.code.USE_CDK_S, builder));
		return true;
	}
	
	/**
	 * 消息形式发放通用奖励
	 * @param msg
	 * @return
	 */
	@MessageHandler
	private boolean onSendReward(PlayerRewardFromActivityMsg msg) {
		// 零收益状态下，获取不到奖励
		if(player.isZeroEarningState()) {
			player.sendIDIPZeroEarningMsg();
			return true;
		}
		
		int diamonds = 0;
		List<RewardItem.Builder> list = msg.getRewardList();
		AwardItems awardItem = AwardItems.valueOf();
		for (RewardItem.Builder itemInfo : list) {
			if (GameUtil.isDiamond(itemInfo.getItemType(), itemInfo.getItemId())) {
				diamonds += itemInfo.getItemCount();
				continue;
			}
			awardItem.addItem(itemInfo.getItemType(), itemInfo.getItemId(), (int) itemInfo.getItemCount());
		}
		
		if (!msg.isMerge()) {
			awardItem.setShowItems(list.stream().map( e -> e.build()).collect(Collectors.toList()));
		}
		
		Action action = msg.getBehaviorAction();
		// 月卡/军魂承接
		if (diamonds > 0 && isDiamondsGrantAction(action)) {
			// 赠送原因 gameplay
			String presentReason = HawkOSOperator.isEmptyString(msg.getAwardReason()) ? DiamondPresentReason.GAMEPLAY : msg.getAwardReason();
			player.increaseDiamond(diamonds, action, null, presentReason);
		} else if (diamonds > 0) {
			HawkLog.logPrintln("PlayerRewardFromActivityMsg grant diamonds failed, playerId: {}, diamonds: {}", player.getId(), diamonds);
		}
		
		boolean alertFlag = msg.isAlert();
		RewardOrginType orginType = msg.getOrginType();
		if (orginType != null) {
			int[] orginArgs = new int[msg.getOrginArgs().size()];
			int index = 0;
			for (Integer arg : msg.getOrginArgs()) {
				orginArgs[index++] = arg;
			}
			awardItem.rewardTakeAffectAndPush(player, action, alertFlag, orginType, orginArgs);
		} else {
			awardItem.rewardTakeAffectAndPush(player, action, alertFlag);
		}
		
		return true;
	}
	
	private boolean isDiamondsGrantAction(Action action) {
		return action == Action.RECHARGE_BUY_MONTHCARD 
				|| action == Action.INHERIT_ACHIEVE_AWARD 
				|| action == Action.INVEST_CANCEL 
				|| action == Action.INVEST_PROFIT
				|| action == Action.RECHARGE_FUND_REWARD
				|| action == Action.CHRISTMAS_RECHARGE_REWARD
				|| action == Action.HONOR_REPAY_TASK_REWARD
				|| action == Action.PDD_COST_CANCEL
				|| action == Action.DIFF_INFO_SAVE_REWARD
				|| action == Action.LUCK_GET_GOLD_DRAW_GOLD_REWARD
				|| action == Action.CELEBRATION_FUND_REWARD
				|| action == Action.LOTTERY_TICKET_USE_ACHIEVE
				|| action == Action.ANNIVERSARY_GIFT_ACHIVE_REWARD;
		
	}

	@MessageHandler
	private boolean onSendRewardByRewardId(PlayerRewardByIdFromActivityMsg msg) {
		// 零收益状态下，获取不到奖励
		if(player.isZeroEarningState()) {
			player.sendIDIPZeroEarningMsg();
			return true;
		}
		
		int rewardId = msg.getRewardId();
		Action action = msg.getBehaviorAction();
		AwardCfg awardCfg = HawkConfigManager.getInstance().getConfigByKey(AwardCfg.class, rewardId);
		if(awardCfg == null) {
			HawkLog.errPrintln("player add reward by id error! playerId: {} rewardId: {} action: {}", player.getId(), rewardId, action.name());
			return false;
		}
		
		AwardItems awardItems = awardCfg.getRandomAward();
		awardItems.rewardTakeAffectAndPush(player, action);
		MsgArgsCallBack callBack = msg.getCallBack();
		if (callBack != null) {
			List<RewardItem.Builder> itemList = new ArrayList<>();
			for (ItemInfo itemInfo : awardItems.getAwardItems()) {
				itemList.add(RewardHelper.toRewardItem(itemInfo.getType(), itemInfo.getItemId(), itemInfo.getCount()));
			}
			callBack.execute(itemList);
		}
		return true;
	}
	
	/**
	 * 领取冒泡奖励
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.TAKE_BUBBLE_REWARD_C_VALUE)
	protected boolean onBubbleReward(HawkProtocol protocol) {
		BubbleRewardReq req = protocol.parseProtocol(BubbleRewardReq.getDefaultInstance());
		int type = req.getType();
		Set<Integer> bubbleTypes = ConstProperty.getInstance().getBubbleTypes();
		// 判断类型参数是否正确
		if (!bubbleTypes.contains(type)) {
			HawkLog.errPrintln("bubble reward type error, playerId: {}, type: {}", player.getId(), type);
			sendError(protocol.getType(), Status.SysError.PARAMS_INVALID);
			return false;
		}
		
		BubbleRewardInfo bubbleInfo = player.getData().getBubbleRewardInfo(type);
		// 跨天处理， 玩家在线跨天是否需要推送？？？
		if(!HawkTime.isSameDay(HawkTime.getMillisecond(), bubbleInfo.getLastTime())) {
			bubbleInfo.setGotTimes(0);
		}

		// 判断是否达到当天领取次数上限
		int maxAwardTims = ConstProperty.getInstance().getBubbleMaxTimes(type);
		if (bubbleInfo.getGotTimes() >= maxAwardTims) {
			HawkLog.errPrintln("bubble reward times over limit, playerId: {}, times: {}, maxTimes: {}", player.getId(), bubbleInfo.getGotTimes(), maxAwardTims);
			sendError(protocol.getType(), Status.Error.BUBBLE_REWARD_TIMES_OVER_LIMIT);
			return false;
		}
		
		// 发奖励
		if (player.isZeroEarningState()) {
			//player.sendIDIPZeroEarningMsg();
			sendError(protocol.getType(), Status.SysError.ZERO_EARNING_STATE);
		} else {
			AwardItems awardItem = AwardItems.valueOf();
			awardItem.addItem(bubbleInfo.getNextRewardItem().clone());
			awardItem.rewardTakeAffectAndPush(player, Action.BUBBLE_REWARD, true);
		}
		
		// 更新信息
		bubbleInfo.setLastTime(HawkApp.getInstance().getCurrentTime());
		bubbleInfo.setGotTimes(bubbleInfo.getGotTimes() + 1);
		bubbleInfo.setNextRewardItem(ConstProperty.getInstance().randomBubbleAwardItem(type));
		player.getData().updateBubbleRewardInfo(type, bubbleInfo);
		LocalRedis.getInstance().updateBubbleRewardInfo(player.getId(), bubbleInfo);
		player.responseSuccess(protocol.getType());
		
		// 同步最新信息
		player.getPush().syncBubbleRewardInfo(type);
		return true;
	}
	
}
