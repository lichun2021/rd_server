package com.hawk.game.module;

import org.hawk.annotation.MessageHandler;
import org.hawk.app.HawkApp;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkTime;
import org.hawk.profiler.HawkProfilerAnalyzer;

import com.hawk.activity.helper.PlayerAcrossDayLoginMsg;
import com.hawk.game.GsConfig;
import com.hawk.game.config.GameConstCfg;
import com.hawk.game.entity.DailyDataEntity;
import com.hawk.game.entity.PlayerGiftEntity;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.LocalRedis;
import com.hawk.game.msg.TimerEventMsg;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerModule;
import com.hawk.game.service.RelationService;
import com.hawk.game.service.TavernService;
import com.hawk.game.service.WishingService;
import com.hawk.game.util.GsConst;

public class PlayerTimerModule extends PlayerModule {
	private long lastPostTime = 0;
	
	public PlayerTimerModule(Player player) {
		super(player);
	}
	
	@MessageHandler
	private void triggerTimeEvent(TimerEventMsg msg) {
		switch(msg.getEventEnum()) {
		case ZERO_CLOCK:
			doZeroClockEvent();
			break;
			
		case FIVE_CLOCK:
			doFiveClockEvent();
			break;
			
		default:
			break;
		}		
	}
	
	/**
	 * 0点事件处理
	 */
	private void doZeroClockEvent() {
		long startTime = HawkTime.getMillisecond();
		//跨天事件处理
		doCrossDayEvent();
		
		// 刷新
		player.refreshVipBenefitBox();
		
		// 同步
		player.getPush().syncGiftList();
		
		// 贵族商城气泡红点刷新
		player.refreshVipShopRedPoint();
		
		// 刷新新兵救援信息
		player.refreshProctectSoldierInfo();
		
		// 每日数据
		player.getPush().synPlayerDailyData();
		
		//同步礼包hud
		RelationService.getInstance().synGuardHud(player);
		
		//同步击杀数量
		player.getPush().syncMonsterKillData();
		
		long costtime = HawkTime.getMillisecond() - startTime;
		if (costtime >= GsConfig.getInstance().getTaskTimeout()) {
			HawkProfilerAnalyzer.getInstance().addMsgHandleInfo("onlineCrossDay", costtime);
			HawkLog.logPrintln("player online cross day, playerId: {}, costtime: {}", player.getId(), costtime);
		}
	}
	
	
	private void doCrossDayEvent() {
		try {
			//更新酒馆数据
			TavernService.getInstance().refreshTavernInfo(player, false);
			WishingService.getInstance().refreshWishingInfo(player);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		// 玩家跨天消息事件
		PlayerAcrossDayLoginMsg msg = PlayerAcrossDayLoginMsg.valueOf(false);
		HawkApp.getInstance().postMsg(player.getXid(), msg);
		
		try {
			DailyDataEntity dailyDataEntity = player.getData().getDailyDataEntity();
			dailyDataEntity.clear();
			LocalRedis.getInstance().clearFriendPresentGift(player.getId());
			PlayerGiftEntity playerGiftEntity = player.getData().getPlayerGiftEntity();
			playerGiftEntity.clearDailyGiftAdvice();
			
			player.getPush().syncMilitaryRankAwardState();
			player.getPush().syncPlayerInfo();
			
			// 推送装扮信使礼包每周赠送次数
			player.getPush().syncSendDressGiftInfo();
			// 英雄档案礼包每月赠送次数
			PlayerHeroModule heroModule = player.getModule(GsConst.ModuleType.HERO);
			heroModule.syncHeroArchivesInfo();
			// 每月超值礼包刷新
			PlayerGiftModule giftModule = player.getModule(GsConst.ModuleType.GIFT_MOUDLE);
			giftModule.clearMonthGift();
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 5点事件处理
	 */
	private void doFiveClockEvent() {
		
	}
	
	@Override
	public boolean onTick() {
		// 全服同时在线人数超过设定值时,定时推送战力变化活动事件,降低事件抛出频率
		int onlineUserCnt = GlobalData.getInstance().getOnlineUserCount();
		long gap = 0;
		if (onlineUserCnt >= GameConstCfg.getInstance().getPowerChangeEventPostDelayCondition()) {
			gap = GameConstCfg.getInstance().getPowerChangeEventPostPeriod();
		}
		long currentTime = HawkTime.getMillisecond();
		if (currentTime >= lastPostTime + gap) {
			getPlayerData().getPowerElectric().postPowerChangeEvent();
			lastPostTime = currentTime;
		}
		return super.onTick();
	}

	@Override
	protected boolean onPlayerLogout() {
		// 玩家离线时抛出战力变化事件
		getPlayerData().getPowerElectric().postPowerChangeEvent();
		return super.onPlayerLogout();
	}
	
	
}
