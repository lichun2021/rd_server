package com.hawk.game.idipscript.mail;

import java.util.ArrayList;
import java.util.List;

import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkTime;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hawk.game.config.ItemCfg;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.idipscript.util.IdipUtil;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.protocol.Const.ItemType;
import com.hawk.game.protocol.Const.MailRewardStatus;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mail.SystemMailService;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.LogUtil;
import com.hawk.idip.IdipConst;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;

/**
 * 全服邮件发送多个道具请求 -- 10282159
 *
 * @author lating
 */
@HawkScript.Declare(id = "idip/4467")
public class SendMuchAwardGlobalMailHandler extends IdipScriptHandler {
	
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		
		JSONObject body = request.getJSONObject("body"); 
		String title = body.getString("MailTitle");
		title = IdipUtil.decode(title);
		String content = body.getString("MailContent");
		content = IdipUtil.decode(content);
		
		JSONArray itemList = request.getJSONObject("body").getJSONArray("ItemList");
		if (itemList.isEmpty()) {
			result.getBody().put("Result", IdipConst.SysError.PARAM_ERROR);
			result.getBody().put("RetMsg", "request failed, invalid ItemList");
			return result;
		}
		
		String serialID = request.getJSONObject("body").getString("Serial");
		if (!RedisProxy.getInstance().saveIdipSerialID(serialID)) {
			HawkLog.errPrintln("SendMuchAwardGlobalMailHandler4467 request repeatd of serial: {}", serialID);
			result.getBody().put("Result", IdipConst.SysError.SERVER_BUSY);
			result.getBody().put("RetMsg", "request of Serial repeated");
			return result;
		}
		
		List<ItemInfo> items = new ArrayList<ItemInfo>();
		for (int i = 0; i< itemList.size(); i++) {
			JSONObject json = itemList.getJSONObject(i);
			int itemId = json.getIntValue("ItemId");
			ItemCfg cfg = HawkConfigManager.getInstance().getConfigByKey(ItemCfg.class, itemId);
			if (cfg == null) {
				result.getBody().put("Result", IdipConst.SysError.PARAM_ERROR);
				result.getBody().put("RetMsg", "request failed, invalid itemId: " + itemId);
				return result;
			}
			
			int itemNum = json.getIntValue("ItemNum");
			items.add(new ItemInfo(ItemType.TOOL_VALUE * GsConst.ITEM_TYPE_BASE, itemId, itemNum));
			LogUtil.logIdipSensitivity(null, request, itemId, itemNum);
		}
		
		long time = 3600 * 24 * 30;
		if (body.containsKey("ItemGetTime") && body.getLong("ItemGetTime") > 0) {
			time = body.getLong("ItemGetTime");
		}
		
		long currTime = HawkTime.getMillisecond();
		SystemMailService.getInstance().addGlobalMail(MailParames.newBuilder()
                .setMailId(MailId.REWARD_MAIL)
                .setAwardStatus(MailRewardStatus.NOT_GET)
                .addSubTitles(title)
                .addContents(content)
                .addRewards(items)
                .build()
                ,currTime
                , currTime + time * 1000);
		
		
		result.getBody().put("Result", 0);
		result.getBody().put("RetMsg", "");
		return result;
	}
	
}
