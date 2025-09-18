package com.hawk.game.idipscript.first;

import java.util.ArrayList;
import java.util.List;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import com.alibaba.fastjson.JSONObject;
import com.hawk.game.idipscript.util.IdipUtil;
import com.hawk.game.idipscript.util.IdipUtil.MailAwardItemType;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const.ItemType;
import com.hawk.game.protocol.Const.MailRewardStatus;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mail.SystemMailService;
import com.hawk.game.util.LogUtil;
import com.hawk.game.util.GsConst;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;

/**
 * 发送带附件邮件请求（指定账号）
 *
 * localhost:8080/script/idip/4113?OpenId=&RoleId=&Title=&Content=&ItemId=&ItemNum=&ItemGetTime=&Type=
 *
 * @param OpenId  用户openId
 * @param RoleId  playerId
 * @param Title   邮件标题
 * @param Content 邮件内容
 * @param ItemId  道具id
 * @param ItemNum 道具数量
 * @param ItemGetTime   道具可领取时间
 * @param Type 类型：货币1，物品2
 * 
 * @author lating
 */
@HawkScript.Declare(id = "idip/4113")
public class SendAccountAwardMailHandler extends IdipScriptHandler {
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		Player player = IdipUtil.playerCheck(request, result);
		if (player == null) {
			return result;
		}
		
		String title = request.getJSONObject("body").getString("Title");
	    title =IdipUtil.decode(title);
		String content = request.getJSONObject("body").getString("Content");
		content =IdipUtil.decode(content);
		
		int itemId = request.getJSONObject("body").getIntValue("ItemId");
		int itemNum = request.getJSONObject("body").getIntValue("ItemNum");
		int type = request.getJSONObject("body").getIntValue("Type");
		// 货币1，物品2
		type = (type == MailAwardItemType.MONEY ? ItemType.PLAYER_ATTR_VALUE : ItemType.TOOL_VALUE);
		ItemInfo itemInfo = new ItemInfo(type * GsConst.ITEM_TYPE_BASE, itemId, itemNum);
		List<ItemInfo> items = new ArrayList<ItemInfo>(1);
		items.add(itemInfo);
		
		SystemMailService.getInstance().sendMail(MailParames.newBuilder()
		        .setPlayerId(player.getId())
		        .setMailId(MailId.REWARD_MAIL)
		        .setAwardStatus(MailRewardStatus.NOT_GET)
		        .addSubTitles(title)
		        .addContents(content)
		        .setRewards(items)
		        .build());
		LogUtil.logIdipSensitivity(player, request, type == ItemType.TOOL_VALUE ? itemId : 0, itemNum);
		result.getBody().put("Result", 0);
		result.getBody().put("RetMsg", "");
		
		return result;
	}
}
