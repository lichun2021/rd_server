package com.hawk.game.idipscript.online.pack0604;

import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;

import com.alibaba.fastjson.JSONObject;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.event.impl.BuyMonthCardEvent;
import com.hawk.activity.type.impl.monthcard.entity.ActivityMonthCardEntity;
import com.hawk.game.config.PayGiftCfg;
import com.hawk.game.idipscript.util.IdipUtil;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const.MailRewardStatus;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mail.SystemMailService;
import com.hawk.game.util.GameUtil;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;

/**
 * 发放月卡
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
		sendMonthCard(player.getId(), goodsId, title, content);
		
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
			HawkLog.logPrintln("sendMonthCard failed, the type monthCard exist, playerId: {}, goodsId: {}, type: {}", playerId, goodsId, payGiftCfg.getMonthCardType());
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
}


