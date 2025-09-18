package com.hawk.game.world.march.impl;

import java.util.Optional;
import java.util.Set;

import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkTime;

import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.event.impl.DragonBoatGiftAchieveEvent;
import com.hawk.activity.event.impl.DragonBoatRefreshEvent;
import com.hawk.activity.type.impl.drogenBoatFestival.gift.DragonBoatGiftActivity;
import com.hawk.activity.type.impl.drogenBoatFestival.gift.cfg.DragonBoatGiftKVCfg;
import com.hawk.game.config.AwardCfg;
import com.hawk.game.global.GlobalData;
import com.hawk.game.item.AwardItems;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Activity;
import com.hawk.game.protocol.Const.MailRewardStatus;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mail.SystemMailService;
import com.hawk.game.util.LogUtil;
import com.hawk.game.world.WorldMarch;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.march.PlayerMarch;
import com.hawk.game.world.march.submarch.BasedMarch;
import com.hawk.game.world.object.DragonBoatInfo;
import com.hawk.game.world.service.WorldPointService;

public class DragonboatMarch  extends PlayerMarch implements BasedMarch{

	public DragonboatMarch(WorldMarch marchEntity) {
		super(marchEntity);
	}

	@Override
	public WorldMarchType getMarchType() {
		return WorldMarchType.DRAGON_BOAT_MARCH;
	}

	@Override
	public void onMarchReach(Player player) {
		// 行军
		WorldMarch march = getMarchEntity();
		// 目标点
		int terminalId = march.getTerminalId();
		// 目标野怪
		int boatId = Integer.valueOf(march.getTargetId());
		// 点和怪信息
		WorldPoint point = WorldPointService.getInstance().getWorldPoint(terminalId);
		// 点为空
		if (point == null || point.getPointType() != WorldPointType.DRAGON_BOAT_VALUE || 
				point.getDragonBoatInfo() == null) {
			SystemMailService.getInstance().sendMail(MailParames.newBuilder()
					.setPlayerId(player.getId())
					.setMailId(MailId.DRAGON_BOAT_POINT_CHANGE)
					.addTips(boatId)
					.build());
			WorldMarchService.getInstance().onPlayerNoneAction(this, HawkTime.getMillisecond());
			WorldMarchService.logger.error("DragonboatMarch march reach error, point null, playerId:{},terminalId:{}",player.getId(), terminalId);
			return;
		}
		int termId = 0;
		Optional<ActivityBase> dbGiftboatActivityOP = ActivityManager.getInstance().getActivity(Activity.ActivityType.DRAGON_BOAT_GIFT_VALUE);
		if (!dbGiftboatActivityOP.isPresent()) {
			WorldMarchService.getInstance().onPlayerNoneAction(this, HawkTime.getMillisecond());
			WorldMarchService.logger.error("DragonboatMarch march reach error, dbGiftboatActivityOP null, playerId:{},terminalId:{}",player.getId(), terminalId);
			return;
		}
		DragonBoatGiftActivity activity = (DragonBoatGiftActivity) dbGiftboatActivityOP.get();
		boolean opening = activity.isOpening(player.getId());
		if(!opening){
			WorldMarchService.getInstance().onPlayerNoneAction(this, HawkTime.getMillisecond());
			SystemMailService.getInstance().sendMail(MailParames.newBuilder()
					.setPlayerId(player.getId())
					.setMailId(MailId.DRAGON_BOAT_GIFT_LESS)
					.addTips(boatId)
					.build());
			WorldMarchService.logger.error("DragonboatMarch march reach error, activity stop, playerId:{},terminalId:{}", player.getId(),terminalId);
			return;
		}
		termId = activity.getActivityTermId();
		//已经领取过
		DragonBoatGiftKVCfg kvcfg = HawkConfigManager.getInstance().getKVInstance(DragonBoatGiftKVCfg.class);
		DragonBoatInfo info = point.getDragonBoatInfo();
		if(info.isAward(player.getId())){
			WorldMarchService.getInstance().onPlayerNoneAction(this, HawkTime.getMillisecond());
			WorldMarchService.logger.error("DragonboatMarch march reach error, already award, playerId:{},terminalId:{}", player.getId(),terminalId);
			return;
		}
		//礼物不足
		if(info.getAwardRecordSize() >=  kvcfg.getGiftCount()){
			WorldMarchService.getInstance().onPlayerNoneAction(this, HawkTime.getMillisecond());
			SystemMailService.getInstance().sendMail(MailParames.newBuilder()
					.setPlayerId(player.getId())
					.setMailId(MailId.DRAGON_BOAT_GIFT_LESS)
					.addTips(boatId)
					.build());
			WorldMarchService.logger.error("DragonboatMarch march reach error, gift less, playerId:{},terminalId:{},giftCount:{}", player.getId(),terminalId,info.getAwardRecordSize());
			return;
		}
		// 行军返回
		WorldMarchService.getInstance().onPlayerNoneAction(this, getMarchEntity().getReachTime());
		doDragonBoatMarchReach(player,point,termId);
		//如果礼物被领完刷新
		if(info.getAwardRecordSize() >= kvcfg.getGiftCount()){
			Set<String> players = GlobalData.getInstance().getOnlinePlayerIds();
			for(String pid : players){
				DragonBoatRefreshEvent event = new DragonBoatRefreshEvent(pid,boatId);
				ActivityManager.getInstance().postEvent(event);
			}
		}
		
	}

	/**
	 * 端午龙船到达
	 * @param player
	 */
	public void doDragonBoatMarchReach(Player player,WorldPoint point,int termId) {
		long boatId = point.getDragonBoatInfo().getBoatId();
		point.getDragonBoatInfo().addAwardRecord(player.getId(),HawkTime.getMillisecond());
		WorldMarchService.logger.info("doTreasureHuntMarchReach, playerId:{},boatId:{}", player.getId(),boatId);
		DragonBoatGiftKVCfg cfg = HawkConfigManager.
				getInstance().getKVInstance(DragonBoatGiftKVCfg.class);
		int randomProduction = cfg.getBoatAward();
		AwardCfg awardCfg = HawkConfigManager.getInstance().
				getConfigByKey(AwardCfg.class, randomProduction);
		AwardItems radnomAwardItems = awardCfg.getRandomAward();
		SystemMailService.getInstance().sendMail(MailParames.newBuilder()
				.setMailId(MailId.DRAGON_BOAT_GIFT_ACHIEVE)
				.setPlayerId(this.getPlayerId())
				.setRewards(radnomAwardItems.getAwardItems())
				.setAwardStatus(MailRewardStatus.NOT_GET)
				.build());
		DragonBoatGiftAchieveEvent event = new DragonBoatGiftAchieveEvent(player.getId(), boatId);
		ActivityManager.getInstance().postEvent(event);
		LogUtil.logDragonBoatGiftAchieve(player, termId, 2, boatId);
	}

}
