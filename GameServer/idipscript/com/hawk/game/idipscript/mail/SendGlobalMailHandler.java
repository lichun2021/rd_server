package com.hawk.game.idipscript.mail;

import java.util.ArrayList;
import java.util.List;

import org.hawk.log.HawkLog;
import org.hawk.os.HawkTime;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import com.alibaba.fastjson.JSONObject;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.idipscript.util.IdipUtil;
import com.hawk.game.idipscript.util.IdipUtil.MailAwardItemType;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.protocol.Const.ItemType;
import com.hawk.game.protocol.Const.MailRewardStatus;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.service.GlobalMail;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mail.SystemMailService;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.LogUtil;
import com.hawk.idip.IdipConst;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;

/**
 * 发送全服带附件邮件  -- 10282011
 *
 * localhost:8080/script/idip/4119?Title=&Content=&ItemId=&ItemNum=&ItemGetTime=&Type=
 *
 * @param Title    邮件标题
 * @param Content  邮件内容
 * @param ItemId   道具id
 * @param ItemNum   道具数量
 * @param ItemGetTime 道具可领取时间
 * @param Type 类型
 * 
 * @author lating
 */
@HawkScript.Declare(id = "idip/4119")
public class SendGlobalMailHandler extends IdipScriptHandler {
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		
		JSONObject body = request.getJSONObject("body"); 
		String serialID = request.getJSONObject("body").getString("Serial");
		if (!RedisProxy.getInstance().saveIdipSerialID(serialID)) {
			HawkLog.errPrintln("SendGlobalMailHandler4119 request repeatd of serial: {}", serialID);
			result.getBody().put("Result", IdipConst.SysError.SERVER_BUSY);
			result.getBody().put("RetMsg", "request of Serial repeated");
			return result;
		}
		
		String title = body.getString("Title");
		title = IdipUtil.decode(title);
		String content = body.getString("Content");
		content = IdipUtil.decode(content);
		
		int itemId = body.getIntValue("ItemId");
		int itemNum = body.getIntValue("ItemNum");
		int type = body.getIntValue("Type");
		long time = 3600;
		if (body.containsKey("ItemGetTime") && body.getLong("ItemGetTime") > 0) {
			time = body.getLong("ItemGetTime");
		}
		
		// 1货币，2物品
		type = (type == MailAwardItemType.MONEY ? ItemType.PLAYER_ATTR_VALUE : ItemType.TOOL_VALUE);
		ItemInfo itemInfo = new ItemInfo(type * GsConst.ITEM_TYPE_BASE, itemId, itemNum);
		List<ItemInfo> items = new ArrayList<ItemInfo>(1);
		items.add(itemInfo);
		
		long currTime = HawkTime.getMillisecond();
		GlobalMail mail = SystemMailService.getInstance().addGlobalMail(MailParames.newBuilder()
                .setMailId(MailId.REWARD_MAIL)
                .setAwardStatus(MailRewardStatus.NOT_GET)
                .addSubTitles(title)
                .addContents(content)
                .addRewards(items)
                .build()
                ,currTime
                , currTime + time * 1000);
		
		LogUtil.logIdipSensitivity(null, request, type == ItemType.TOOL_VALUE ? itemId : 0, itemNum);
		
		result.getBody().put("Result", 0);
		result.getBody().put("RetMsg", mail.getUuid());
		return result;
	}
	
}
