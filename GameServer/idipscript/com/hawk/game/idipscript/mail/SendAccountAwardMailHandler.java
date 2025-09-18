package com.hawk.game.idipscript.mail;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkOSOperator;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import com.alibaba.fastjson.JSONObject;
import com.hawk.common.AccountRoleInfo;
import com.hawk.game.config.ItemCfg;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.idipscript.util.IdipUtil;
import com.hawk.game.idipscript.util.IdipUtil.MailAwardItemType;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const.ItemType;
import com.hawk.game.protocol.Const.MailRewardStatus;
import com.hawk.game.protocol.Const.PlayerAttr;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mail.SystemMailService;
import com.hawk.game.util.LogUtil;
import com.hawk.game.util.GsConst;
import com.hawk.idip.IdipConst;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;

/**
 * 发送带附件邮件请求（指定账号） -- 10282008
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
		
		String title = request.getJSONObject("body").getString("Title");
	    title =IdipUtil.decode(title);
		String content = request.getJSONObject("body").getString("Content");
		content =IdipUtil.decode(content);
		
		int itemId = request.getJSONObject("body").getIntValue("ItemId");
		int itemNum = request.getJSONObject("body").getIntValue("ItemNum");
		int type = request.getJSONObject("body").getIntValue("Type");
		// 货币1，物品2
		type = (type == MailAwardItemType.MONEY ? ItemType.PLAYER_ATTR_VALUE : ItemType.TOOL_VALUE);
		if (type == ItemType.PLAYER_ATTR_VALUE && PlayerAttr.valueOf(itemId) == null) {
			result.getBody().put("Result", IdipConst.SysError.PARAM_ERROR);
			result.getBody().put("RetMsg", "param Type and ItemId not match");
			return result;
		}
		
		if (type == ItemType.TOOL_VALUE && HawkConfigManager.getInstance().getConfigByKey(ItemCfg.class, itemId) == null) {
			result.getBody().put("Result", IdipConst.SysError.PARAM_ERROR);
			result.getBody().put("RetMsg", "param Type and ItemId not match");
			return result;
		}
		
		String serialID = request.getJSONObject("body").getString("Serial");
		if (!RedisProxy.getInstance().saveIdipSerialID(serialID)) {
			HawkLog.errPrintln("SendAccountAwardMail4113 request repeatd of serial: {}", serialID);
			result.getBody().put("Result", IdipConst.SysError.SERVER_BUSY);
			result.getBody().put("RetMsg", "request of Serial repeated");
			return result;
		}
		
		ItemInfo itemInfo = new ItemInfo(type * GsConst.ITEM_TYPE_BASE, itemId, itemNum);
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
			LogUtil.logIdipSensitivity(player, request, type == ItemType.TOOL_VALUE ? itemId : 0, itemNum);
		}
		result.getBody().put("Result", 0);
		result.getBody().put("RetMsg", "");
		
		return result;
	}
}
