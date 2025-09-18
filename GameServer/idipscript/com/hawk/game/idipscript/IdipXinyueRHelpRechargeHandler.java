package com.hawk.game.idipscript;


import java.util.ArrayList;
import java.util.List;

import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;

import com.alibaba.fastjson.JSONObject;
import com.hawk.game.idipscript.util.IdipUtil;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const.ItemType;
import com.hawk.game.protocol.Const.MailRewardStatus;
import com.hawk.game.protocol.Const.PlayerAttr;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mail.SystemMailService;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.LogUtil;
import com.hawk.idip.IdipConst;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;

/**
 * 心悦大R代充接口 -- 10282016
 *
 * localhost:8080/script/idip/4155
 *
 * @author lating
 */
@HawkScript.Declare(id = "idip/4155")
public class IdipXinyueRHelpRechargeHandler extends IdipScriptHandler {
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		Player player = IdipUtil.checkAccountAndPlayer(request, result);
		if (player == null) {
			return result;
		}
		
		int moneyType = request.getJSONObject("body").getInteger("Type");  
		if (moneyType != PlayerAttr.GOLD_VALUE && moneyType != PlayerAttr.DIAMOND_VALUE) {
			result.getHead().put("Result", IdipConst.SysError.PARAM_ERROR);
			result.getHead().put("RetErrMsg", "Type param error");
			return result;
		}
		
		String mailTitle = request.getJSONObject("body").getString("MailTitle");
		String mailContent = request.getJSONObject("body").getString("MailContent");
		int moneyCount = request.getJSONObject("body").getInteger("ItemNum");
		String extendParam = request.getJSONObject("body").getString("ExtendParameter");
		
		ItemInfo itemInfo = new ItemInfo(ItemType.PLAYER_ATTR_VALUE * GsConst.ITEM_TYPE_BASE, moneyType, moneyCount);
		List<ItemInfo> items = new ArrayList<>();
		items.add(itemInfo);
		
		SystemMailService.getInstance().sendMail(MailParames.newBuilder()
		        .setPlayerId(player.getId())
		        .setMailId(MailId.REWARD_MAIL)
		        .setAwardStatus(MailRewardStatus.NOT_GET)
		        .addSubTitles(mailTitle)
		        .addContents(mailContent)
		        .setRewards(items)
		        .setAdditionalParam(extendParam)
		        .build());
		LogUtil.logIdipSensitivity(player, request, 0, moneyCount);

		result.getBody().put("Result", 0);
		result.getBody().put("RetMsg", "");
		return result;
	}
}
