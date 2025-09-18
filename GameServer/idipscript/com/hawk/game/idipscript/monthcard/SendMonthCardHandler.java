package com.hawk.game.idipscript.monthcard;

import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkTime;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import org.hawk.task.HawkTaskManager;
import org.hawk.thread.HawkTask;

import com.alibaba.fastjson.JSONObject;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.event.impl.BuyMonthCardEvent;
import com.hawk.activity.type.impl.monthcard.entity.ActivityMonthCardEntity;
import com.hawk.activity.type.impl.monthcard.entity.MonthCardItem;
import com.hawk.game.config.PayGiftCfg;
import com.hawk.game.entity.LifetimeCardEntity;
import com.hawk.game.idipscript.util.IdipUtil;
import com.hawk.game.module.PlayerLifetimeCardModule;
import com.hawk.game.module.agency.PlayerAgencyModule;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const.MailRewardStatus;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.recharge.RechargeType;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mail.SystemMailService;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;

/**
 * 发放月卡 -- 10282116
 *
 * localhost:8080/script/idip/4375
 *
 * @author lating
 */
@HawkScript.Declare(id = "idip/4375")
public class SendMonthCardHandler extends IdipScriptHandler {
	
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		Player player = IdipUtil.checkAccountAndPlayer(request, result);
		if (player == null) {
			return result;
		}
		
		String goodsId = request.getJSONObject("body").getString("GoodsId");
		String title = request.getJSONObject("body").getString("MailTitle");
		title = IdipUtil.decode(title);
		String content = request.getJSONObject("body").getString("MailContent");
		content = IdipUtil.decode(content);
		boolean success = sendLifeTimeCard(player, goodsId, title, content);
		if (!success) {
			sendMonthCard(player.getId(), goodsId, title, content);
		}
		
		result.getBody().put("Result", 0);
		result.getBody().put("RetMsg", "");
		return result;
	}
	
	/**
	 * 月卡补发
	 * 
	 * @param playerId
	 */
	private void sendMonthCard(String playerId, String goodsId, String title, String content) {
		PayGiftCfg payGiftCfg = HawkConfigManager.getInstance().getConfigByKey(PayGiftCfg.class, goodsId);
		if (!payGiftCfg.isMonthCard()) {
			HawkLog.logPrintln("sendMonthCard failed, goodsType not match, playerId: {}, goodsId: {}", playerId, goodsId);
			return;
		}
		
		ActivityMonthCardEntity entity = GameUtil.getMonthCardEntity(playerId);
		if (entity != null && !entity.getEfficientCardList(payGiftCfg.getMonthCardType()).isEmpty()) {
			MonthCardItem cardItem = entity.getEfficientCard(payGiftCfg.getMonthCardType());
			if (cardItem.getReady() == 0) {
				BuyMonthCardEvent event = new BuyMonthCardEvent(playerId, payGiftCfg.getMonthCardType(), payGiftCfg.getPayRMB() / 100);
				event.setReady(1);
				ActivityManager.getInstance().postEvent(event);
				
				SystemMailService.getInstance().sendMail(MailParames.newBuilder()
				        .setPlayerId(playerId)
				        .setMailId(MailId.REWARD_MAIL)
				        .setAwardStatus(MailRewardStatus.GET)
				        .addSubTitles(title)
				        .addContents(content+"（由于您当前月卡正处于有效期，待有效期结束后将自动续补。）")
				        .build());
				
				HawkLog.logPrintln("sendMonthCard future, the type monthCard exist, playerId: {}, goodsId: {}, type: {}", playerId, goodsId, payGiftCfg.getMonthCardType());
			} else {
				HawkLog.logPrintln("sendMonthCard failed, the type monthCard exist, playerId: {}, goodsId: {}, type: {}", playerId, goodsId, payGiftCfg.getMonthCardType());
			}
			return;
		}
		
		ActivityManager.getInstance().postEvent(new BuyMonthCardEvent(playerId, payGiftCfg.getMonthCardType(), payGiftCfg.getPayRMB() / 100));
		
		HawkLog.logPrintln("sendMonthCard success, playerId: {}, goodsId: {}", playerId, goodsId);
		
		SystemMailService.getInstance().sendMail(MailParames.newBuilder()
		        .setPlayerId(playerId)
		        .setMailId(MailId.REWARD_MAIL)
		        .setAwardStatus(MailRewardStatus.GET)
		        .addSubTitles(title)
		        .addContents(content)
		        .build());
	}
	
	/**
	 * 终身卡补发
	 * @param player
	 * @param giftId
	 * @param title
	 * @param content
	 * @return
	 */
	private boolean sendLifeTimeCard(Player player, String giftId, String title, String content){
		PayGiftCfg cfg = HawkConfigManager.getInstance().getConfigByKey(PayGiftCfg.class, giftId);
		if (cfg == null){
			HawkLog.logPrintln("sendLifeTimeCard failed, PayGiftCfg not exist, playerId: {}, goodsId: {}", player.getId(), giftId);
			return false;
		}
		
		if (cfg.getGiftType() == RechargeType.LIFETIME_COMMON_CARD){
			sendLifetimeCommCard(player, giftId, title, content);
			return true;
		}
		
		if (cfg.getGiftType() == RechargeType.LIFETIME_ADVANCED_CARD){
			sendLifetimeAdvCard(player, giftId, title, content);
			return true;
		}
		
		return false;
	}
	
	/**
	 * 终身卡
	 * @param player
	 * @param giftId
	 * @param title
	 * @param content
	 */
	private void sendLifetimeCommCard(Player player, String giftId, String title, String content) {
		HawkLog.logPrintln("sendLifeTimeCard common start, playerId: {}, goodsId: {}", player.getId(), giftId);
		LifetimeCardEntity entity = player.getData().getLifetimeCardEntity();
		if (entity.isCommonUnlock()){
			HawkLog.logPrintln("sendLifeTimeCard common success, LifetimeCard Unlock, playerId: {}, goodsId: {}", player.getId(), giftId);
			return;
		}
		
		entity.setCommonUnlockTime(HawkTime.getMillisecond());
		SystemMailService.getInstance().sendMail(MailParames.newBuilder()
		        .setPlayerId(player.getId())
		        .setMailId(MailId.REWARD_MAIL)
		        .setAwardStatus(MailRewardStatus.GET)
		        .addSubTitles(title)
		        .addContents(content)
		        .build());
		
		if (player.isActiveOnline()){
			int threadIdx = player.getXid().getHashThread(HawkTaskManager.getInstance().getThreadNum());
			HawkTaskManager.getInstance().postTask(new HawkTask() {
				@Override
				public Object run() {
					// 刷新终身卡作用号
					player.getEffect().resetLifeTimeCard(player);
					PlayerLifetimeCardModule lifetimeCardModule = player.getModule(GsConst.ModuleType.LIFETIME_CARD);
					// 同步终身卡界面
					lifetimeCardModule.syncLifetimeCardInfo();
					PlayerAgencyModule agencyModule = player.getModule(GsConst.ModuleType.AGENCY_MODULE);
					// 刷新军情任务
					agencyModule.addMission();
					agencyModule.pushPageInfo();
					return null;
				}
			}, threadIdx);
		} else {
			// 刷新终身卡作用号
			player.getEffect().resetLifeTimeCard(player);
			PlayerAgencyModule agencyModule = player.getModule(GsConst.ModuleType.AGENCY_MODULE);
			// 刷新军情任务
			agencyModule.addMission();
		}
		
		HawkLog.logPrintln("sendLifeTimeCard common success, playerId: {}, goodsId: {}", player.getId(), giftId);
	}
	
	/**
	 * 终身卡进阶版
	 * @param player
	 * @param giftId
	 * @param title
	 * @param content
	 */
	private void sendLifetimeAdvCard(Player player, String giftId, String title, String content) {
        HawkLog.logPrintln("sendLifeTimeCard advance start, playerId: {}, goodsId: {}", player.getId(), giftId);
        LifetimeCardEntity entity = player.getData().getLifetimeCardEntity();
        entity.setReady(1);
        entity.notifyUpdate();
        SystemMailService.getInstance().sendMail(MailParames.newBuilder()
                .setPlayerId(player.getId())
                .setMailId(MailId.REWARD_MAIL)
                .setAwardStatus(MailRewardStatus.GET)
                .addSubTitles(title)
                .addContents(content)
                .build());

        HawkLog.logPrintln("sendLifeTimeCard advance success, playerId: {}, goodsId: {}", player.getId(), giftId);
	}
	
}


