package com.hawk.game.script;

import java.util.Map;

import com.hawk.game.config.LifetimeCardCfg;
import com.hawk.game.entity.LifetimeCardEntity;
import com.hawk.game.module.PlayerLifetimeCardModule;
import com.hawk.game.module.agency.PlayerAgencyModule;
import com.hawk.game.module.plantfactory.PlayerPlantFactoryModule;
import com.hawk.game.recharge.RechargeType;
import com.hawk.game.util.GsConst;
import org.hawk.app.HawkObjModule;
import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkTime;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;

import com.hawk.activity.ActivityManager;
import com.hawk.activity.event.impl.BuyMonthCardEvent;
import com.hawk.activity.type.impl.monthcard.entity.ActivityMonthCardEntity;
import com.hawk.activity.type.impl.monthcard.entity.MonthCardItem;
import com.hawk.game.config.PayGiftCfg;
import com.hawk.game.global.GlobalData;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const.MailRewardStatus;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mail.SystemMailService;
import com.hawk.game.util.GameUtil;
import org.hawk.task.HawkTaskManager;
import org.hawk.thread.HawkTask;

/**
 * 发放月卡
 *
 * localhost:8080/script/sendCard?playerId=&goodsId=
 *
 * @author lating
 */
public class SendMonthCardHandler extends HawkScript {
	
	
	@Override
	public String action(Map<String, String> params, HawkScriptHttpInfo httpInfo) {
		try {
			Player player = GlobalData.getInstance().scriptMakesurePlayer(params);
			if (player == null) {
				return HawkScript.failedResponse(HawkScript.SCRIPT_ERROR, "player not exist");
			}

			String playerId = player.getId();
			String goodsId = params.get("goodsId");
			PayGiftCfg payGiftCfg = HawkConfigManager.getInstance().getConfigByKey(PayGiftCfg.class, goodsId);
			if (!payGiftCfg.isMonthCard()) {
				HawkLog.logPrintln("sendMonthCard failed, goodsType not match, playerId: {}, goodsId: {}", playerId, goodsId);
				return HawkScript.failedResponse(HawkScript.SCRIPT_ERROR, "not card type goods");
			}
			
			ActivityMonthCardEntity entity = GameUtil.getMonthCardEntity(playerId);
			if (entity != null && !entity.getEfficientCardList(payGiftCfg.getMonthCardType()).isEmpty()) {
				MonthCardItem cardItem = entity.getEfficientCard(payGiftCfg.getMonthCardType());
				if (cardItem.getReady() == 0) {
					BuyMonthCardEvent event = new BuyMonthCardEvent(player.getId(), payGiftCfg.getMonthCardType(), payGiftCfg.getPayRMB() / 100);
					event.setReady(1);
					ActivityManager.getInstance().postEvent(event);
					
					SystemMailService.getInstance().sendMail(MailParames.newBuilder()
					        .setPlayerId(playerId)
					        .setMailId(MailId.REWARD_MAIL)
					        .setAwardStatus(MailRewardStatus.GET)
					        .addSubTitles("脚本补发")
					        .addContents("您当前月卡处于生效期，待生效期结束后将自动续补。")
					        .build());
					
					HawkLog.logPrintln("sendMonthCard future, the type monthCard exist, playerId: {}, goodsId: {}, type: {}", playerId, goodsId, payGiftCfg.getMonthCardType());
					return HawkScript.failedResponse(0, "card exist, send future");
				} else {
					HawkLog.logPrintln("sendMonthCard failed, the type monthCard exist, playerId: {}, goodsId: {}, type: {}", playerId, goodsId, payGiftCfg.getMonthCardType());
				}
				return HawkScript.failedResponse(HawkScript.SCRIPT_ERROR, "card exist and exist ready send");
			}
			
			ActivityManager.getInstance().postEvent(new BuyMonthCardEvent(playerId, payGiftCfg.getMonthCardType(), payGiftCfg.getPayRMB() / 100));
			
			HawkLog.logPrintln("sendMonthCard success, playerId: {}, goodsId: {}", playerId, goodsId);
			
			SystemMailService.getInstance().sendMail(MailParames.newBuilder()
			        .setPlayerId(playerId)
			        .setMailId(MailId.REWARD_MAIL)
			        .setAwardStatus(MailRewardStatus.GET)
			        .addSubTitles("脚本补发")
			        .addContents("脚本补发")
			        .build());
			
			return HawkScript.successResponse("ok");
		} catch (Exception e) {
			HawkException.catchException(e);
			return HawkException.formatStackMsg(e);
		}
	}

	private boolean buyLifeTimeCard(String giftId,Player player){
		LifetimeCardEntity entity = player.getData().getLifetimeCardEntity();
		PlayerLifetimeCardModule lifetimeCardModule = player.getModule(GsConst.ModuleType.LIFETIME_CARD);
		PlayerAgencyModule agencyModule = player.getModule(GsConst.ModuleType.AGENCY_MODULE);
		PlayerPlantFactoryModule plantFactoryModule = player.getModule(GsConst.ModuleType.PLANT_FACTORY);
		long currTime = HawkTime.getMillisecond();
		PayGiftCfg cfg = HawkConfigManager.getInstance().getConfigByKey(PayGiftCfg.class, giftId);
		if (cfg == null){
			return false;
		}
		if (cfg.getGiftType() == RechargeType.LIFETIME_COMMON_CARD){
			if (entity.isCommonUnlock()){
				return false;
			}
			entity.setCommonUnlockTime(currTime);

			if (player.isActiveOnline()){
				int threadIdx = player.getXid().getHashThread(HawkTaskManager.getInstance().getThreadNum());
				HawkTaskManager.getInstance().postTask(new HawkTask() {
					@Override
					public Object run() {
						// 刷新终身卡作用号
						player.getEffect().resetLifeTimeCard(player);
						// 同步终身卡界面
						lifetimeCardModule.syncLifetimeCardInfo();
						// 刷新军情任务
						agencyModule.addMission();
						agencyModule.pushPageInfo();
						return null;
					}
				}, threadIdx);
			}else {
				// 刷新终身卡作用号
				player.getEffect().resetLifeTimeCard(player);
				// 刷新军情任务
				agencyModule.addMission();
			}
			return true;
		}else if (cfg.getGiftType() == RechargeType.LIFETIME_ADVANCED_CARD){
			long advancedContinue = LifetimeCardCfg.getInstance().getAdvancedContinue();
			long beforeEndTime = entity.getAdvancedEndTime();

			if (beforeEndTime == 0L || beforeEndTime < currTime) {
				entity.setAdvancedEndTime(currTime + advancedContinue);
			} else {
				entity.setAdvancedEndTime(beforeEndTime + advancedContinue);
			}

			if (player.isActiveOnline()){
				int threadIdx = player.getXid().getHashThread(HawkTaskManager.getInstance().getThreadNum());
				HawkTaskManager.getInstance().postTask(new HawkTask() {
					@Override
					public Object run() {
						//重置作用号
						player.getEffect().resetLifeTimeCard(player);
						//同步终身卡信息
						lifetimeCardModule.syncLifetimeCardInfo();
						//推送泰能工厂信息
						plantFactoryModule.syncPlantFactoryInfo();
						return null;
					}
				}, threadIdx);
			}else {
				//重置作用号
				player.getEffect().resetLifeTimeCard(player);
			}
			return true;
		}
		return false;
	}

}
