package com.hawk.game.idipscript.item;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hawk.log.HawkLog;
import org.hawk.os.HawkOSOperator;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import com.alibaba.fastjson.JSONObject;
import com.hawk.common.AccountRoleInfo;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.idipscript.util.IdipUtil;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.player.Player;
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
 * 发送文本邮件请求（指定账号）-- 10282093
 *
 * localhost:8080/script/idip/4313?OpenId=&Title=&Content=
 *
 * @param OpenId  用户openId
 * @param MailTitle   邮件标题
 * @param MailContent 邮件内容
 * @param ItemId
 * @param ItemNum
 * 
 * @author lating
 */
@HawkScript.Declare(id = "idip/4313")
public class SendAccountItemHandler extends IdipScriptHandler {
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		Player player = IdipUtil.playerCheck(request, result);
		String roleid = request.getJSONObject("body").getString("RoleId");
		if (player == null) {
			if (HawkOSOperator.isEmptyString(roleid)) {
				return result;
			}
			String openid = request.getJSONObject("body").getString("OpenId");
			Map<String, String> map = RedisProxy.getInstance().getAccountRole(openid);
			AccountRoleInfo roleInfo = null;
			for (String value : map.values()) {
				AccountRoleInfo roleInfoObj = JSONObject.parseObject(value, AccountRoleInfo.class);
				if (roleInfoObj.getPlayerId().equals(roleid)) {
					roleInfo = roleInfoObj;
					break;
				}
			}
			
			if (roleInfo == null) {
				return result;
			}
		} else if (!player.getId().equals(roleid)) {
			roleid = player.getId();
		}
		
		int itemId = request.getJSONObject("body").getIntValue("ItemId");
		int itemNum = request.getJSONObject("body").getIntValue("ItemNum");
		if (itemId <= 0 || itemNum <= 0) {
			result.getHead().put("Result", IdipConst.SysError.PARAM_ERROR);
			result.getHead().put("RetErrMsg", "param error");
			return result;
		}
		
		String serialID = request.getJSONObject("body").getString("Serial");
		if (!RedisProxy.getInstance().saveIdipSerialID(serialID)) {
			HawkLog.errPrintln("SendAccountItem4313 request repeatd of serial: {}", serialID);
			result.getBody().put("Result", IdipConst.SysError.SERVER_BUSY);
			result.getBody().put("RetMsg", "request of Serial repeated");
			return result;
		}
		
		String title = request.getJSONObject("body").getString("MailTitle");
		title = IdipUtil.decode(title);
		String content = request.getJSONObject("body").getString("MailContent");
		content = IdipUtil.decode(content);
		
		ItemInfo itemInfo = new ItemInfo(ItemType.TOOL_VALUE * GsConst.ITEM_TYPE_BASE, itemId, itemNum);
		List<ItemInfo> items = new ArrayList<ItemInfo>(1);
		items.add(itemInfo);
		
		SystemMailService.getInstance().sendMail(MailParames.newBuilder()
		        .setPlayerId(roleid)
		        .setMailId(MailId.REWARD_MAIL)
		        .setAwardStatus(MailRewardStatus.NOT_GET)
		        .addSubTitles(title)
		        .addContents(content)
		        .setRewards(items)
		        .build());
		if (player != null) {
			LogUtil.logIdipSensitivity(player, request, itemId, itemNum);
		}
		
		result.getBody().put("Result", 0);
		result.getBody().put("RetMsg", "");
		return result;
	}
}
