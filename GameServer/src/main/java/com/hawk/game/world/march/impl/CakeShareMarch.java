package com.hawk.game.world.march.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;

import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkTime;
import org.hawk.tuple.HawkTuple2;
import org.hawk.tuple.HawkTuple3;

import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.event.impl.CakeShareGetRewardEvent;
import com.hawk.activity.event.impl.CakeShareRefreshEvent;
import com.hawk.activity.type.impl.cakeShare.CakeShareActivity;
import com.hawk.activity.type.impl.cakeShare.cfg.CakeShareKVCfg;
import com.hawk.game.config.AwardCfg;
import com.hawk.game.global.GlobalData;
import com.hawk.game.item.AwardItems;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Activity;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.Const.ChatType;
import com.hawk.game.protocol.Const.MailRewardStatus;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.service.chat.ChatParames;
import com.hawk.game.service.chat.ChatService;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mail.SystemMailService;
import com.hawk.game.util.LogUtil;
import com.hawk.game.world.WorldMarch;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.march.PlayerMarch;
import com.hawk.game.world.march.submarch.BasedMarch;
import com.hawk.game.world.object.CakeShareInfo;
import com.hawk.game.world.service.WorldPointService;

public class CakeShareMarch  extends PlayerMarch implements BasedMarch{

	public CakeShareMarch(WorldMarch marchEntity) {
		super(marchEntity);
	}

	@Override
	public WorldMarchType getMarchType() {
		return WorldMarchType.CAKE_SHARE_MARCH;
	}

	@Override
	public void onMarchReach(Player player) {
		// 行军
		WorldMarch march = getMarchEntity();
		// 目标点
		int terminalId = march.getTerminalId();
		// 目标野怪
		int cakeId = Integer.valueOf(march.getTargetId());
		// 点和怪信息
		WorldPoint point = WorldPointService.getInstance().getWorldPoint(terminalId);
		// 点为空
		if (point == null || point.getPointType() != WorldPointType.CAKE_SHARE_VALUE || 
				point.getCakeShareInfo() == null) {
			SystemMailService.getInstance().sendMail(MailParames.newBuilder()
					.setPlayerId(player.getId())
					.setMailId(MailId.CAKE_SHARE_POINT_CHANGE)
					.addTips(cakeId)
					.build());
			WorldMarchService.getInstance().onPlayerNoneAction(this, HawkTime.getMillisecond());
			WorldMarchService.logger.error("CakeShareMarch march reach error, point null, playerId:{},terminalId:{}",player.getId(), terminalId);
			return;
		}
		int termId = 0;
		Optional<ActivityBase> cakeShareActivityOp = ActivityManager.getInstance().getActivity(Activity.ActivityType.CAKE_SHARE_VALUE);
		if (!cakeShareActivityOp.isPresent()) {
			WorldMarchService.getInstance().onPlayerNoneAction(this, HawkTime.getMillisecond());
			WorldMarchService.logger.error("CakeShareMarch march reach error, cakeShareActivityOp null, playerId:{},terminalId:{}",player.getId(), terminalId);
			return;
		}
		CakeShareActivity activity = (CakeShareActivity) cakeShareActivityOp.get();
		boolean opening = activity.isOpening(player.getId());
		if(!opening){
			WorldMarchService.getInstance().onPlayerNoneAction(this, HawkTime.getMillisecond());
			SystemMailService.getInstance().sendMail(MailParames.newBuilder()
					.setPlayerId(player.getId())
					.setMailId(MailId.CAKE_SHARE_REWARD_LESS)
					.addTips(cakeId)
					.build());
			WorldMarchService.logger.error("CakeShareMarch march reach error, activity stop, playerId:{},terminalId:{}", player.getId(),terminalId);
			return;
		}
		
		long awardStartTime = point.getCakeShareInfo().getStartTime();
		long awardEndTime = point.getCakeShareInfo().getEndTime();
		long curTime = HawkTime.getMillisecond();
		if(curTime > awardEndTime || curTime < awardStartTime){
			WorldMarchService.getInstance().onPlayerNoneAction(this, HawkTime.getMillisecond());
			SystemMailService.getInstance().sendMail(MailParames.newBuilder()
					.setPlayerId(player.getId())
					.setMailId(MailId.CAKE_SHARE_REWARD_LESS)
					.addTips(cakeId)
					.build());
			WorldMarchService.logger.info("doCakeShareMarchReach curTime > awardEndTime, playerId:{},boatId:{}", player.getId(),cakeId);
			return;
		}
		termId = activity.getActivityTermId();
		//已经领取过
		CakeShareKVCfg kvcfg = HawkConfigManager.getInstance().getKVInstance(CakeShareKVCfg.class);
		CakeShareInfo info = point.getCakeShareInfo();
		if(info.isAward(player.getId())){
			WorldMarchService.getInstance().onPlayerNoneAction(this, HawkTime.getMillisecond());
			WorldMarchService.logger.error("CakeShareMarch march reach error, already award, playerId:{},terminalId:{}", player.getId(),terminalId);
			return;
		}
		//礼物不足
		if(info.getAwardRecordSize() >=  kvcfg.getGiftCount()){
			WorldMarchService.getInstance().onPlayerNoneAction(this, HawkTime.getMillisecond());
			SystemMailService.getInstance().sendMail(MailParames.newBuilder()
					.setPlayerId(player.getId())
					.setMailId(MailId.CAKE_SHARE_REWARD_LESS)
					.addTips(cakeId)
					.build());
			WorldMarchService.logger.error("CakeShareMarch march reach error, gift less, playerId:{},terminalId:{},giftCount:{}", player.getId(),terminalId,info.getAwardRecordSize());
			return;
		}
		// 行军返回
		WorldMarchService.getInstance().onPlayerNoneAction(this, getMarchEntity().getReachTime());
		//蛋糕行军到达
		doCakeShareMarchReach(player,point,termId);
		//如果礼物被领完刷新
		if(info.getAwardRecordSize() >= kvcfg.getGiftCount()){
			Set<String> players = GlobalData.getInstance().getOnlinePlayerIds();
			for(String pid : players){
				CakeShareRefreshEvent event = new CakeShareRefreshEvent(pid,cakeId);
				ActivityManager.getInstance().postEvent(event);
			}
		}
		
	}

	/**
	 * 蛋糕 行军到达
	 * @param player
	 */
	public void doCakeShareMarchReach(Player player,WorldPoint point,int termId) {
		CakeShareKVCfg kvcfg = HawkConfigManager.getInstance().getKVInstance(CakeShareKVCfg.class);
		int cakeId = point.getCakeShareInfo().getCakeId();
		point.getCakeShareInfo().addAwardRecord(player.getId(),HawkTime.getMillisecond());
		WorldMarchService.logger.info("doCakeShareMarchReach, playerId:{},boatId:{}", player.getId(),cakeId);
		
		List<ItemInfo> awardItems = new ArrayList<>();
		
		HawkTuple2<Integer, Integer> turnAward = kvcfg.getTurnAward(cakeId);
		if(turnAward == null){
			WorldMarchService.logger.info("doCakeShareMarchReach,turnAward null playerId:{},boatId:{}", player.getId(),cakeId);
			return;
		}
		//固定奖励
		AwardCfg reachAwardCfg = HawkConfigManager.getInstance().getConfigByKey(AwardCfg.class, turnAward.second);
		AwardItems reachAwardItems = reachAwardCfg.getRandomAward();
		awardItems.addAll(reachAwardItems.getAwardItems());
		//跑马灯
		for (ItemInfo itemInfo : reachAwardItems.getAwardItems()) {
			if(kvcfg.noticeItem(itemInfo.getType(), itemInfo.getItemId(), itemInfo.getCount())){
				ChatParames parames = ChatParames.newBuilder()
						.setChatType(Const.ChatType.SPECIAL_BROADCAST)
						.setKey(Const.NoticeCfgId.CAKE_SHARE_BIG_REWARD_CAST)
						.setPlayer(player)
						.addParms(player.getName(), itemInfo.toString())
						.build();
				ChatService.getInstance().addWorldBroadcastMsg(parames);
			}
		}
		SystemMailService.getInstance().sendMail(MailParames.newBuilder()
				.setMailId(MailId.CAKE_SHARE_REWARD_ACHIEVE)
				.setPlayerId(this.getPlayerId())
				.setRewards(awardItems)
				.setAwardStatus(MailRewardStatus.NOT_GET)
				.build());
		CakeShareGetRewardEvent event = new CakeShareGetRewardEvent(player.getId(), cakeId);
		ActivityManager.getInstance().postEvent(event);
		LogUtil.logCakeShareRewardAchieve(player, termId, 2, cakeId);
	}

}
