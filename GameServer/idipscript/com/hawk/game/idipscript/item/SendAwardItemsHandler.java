package com.hawk.game.idipscript.item;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hawk.log.HawkLog;
import org.hawk.os.HawkOSOperator;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hawk.common.AccountRoleInfo;
import com.hawk.game.global.RedisProxy;
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
 * 邮件发放道具（支持多个道具发送） -- 10282079
 *
 * localhost:8080/script/idip/4283
 * 
 * @param AreaId     大区：微信（1），手Q（2）
 * @param Partition  小区id
 * @param PlatId     平台:ios(0)，安卓（1）
 * @param OpenId     用户openId
 * @param RoleId     用户角色Id
 * @param SendItemList_count    发放道具列表的最大数量
 * @param SendItemList  发放道具列表
 *
 * @author lating
 */
@HawkScript.Declare(id = "idip/4283")
public class SendAwardItemsHandler extends IdipScriptHandler {
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
		
		String serialID = request.getJSONObject("body").getString("Serial");
		if (!RedisProxy.getInstance().saveIdipSerialID(serialID)) {
			HawkLog.errPrintln("SendAwardItems4283 request repeatd of serial: {}", serialID);
			result.getBody().put("Result", IdipConst.SysError.SERVER_BUSY);
			result.getBody().put("RetMsg", "request of Serial repeated");
			return result;
		}
		
		String title = request.getJSONObject("body").getString("MailTitle");
	    title =IdipUtil.decode(title);
		String content = request.getJSONObject("body").getString("MailContent");
		content =IdipUtil.decode(content);
		
		JSONArray itemList = request.getJSONObject("body").getJSONArray("SendItemList");
		List<ItemInfo> items = new ArrayList<ItemInfo>();
		for (int i = 0; i< itemList.size(); i++) {
			JSONObject json = itemList.getJSONObject(i);
			int itemId = json.getIntValue("ItemId");
			int itemNum = json.getIntValue("ItemNum");
			if (PlayerAttr.valueOf(itemId) != null) {
				items.add(new ItemInfo(ItemType.PLAYER_ATTR_VALUE * GsConst.ITEM_TYPE_BASE, itemId, itemNum));
			} else {
				items.add(new ItemInfo(ItemType.TOOL_VALUE * GsConst.ITEM_TYPE_BASE, itemId, itemNum));
			}
			if (player != null) {
				LogUtil.logIdipSensitivity(player, request, itemId, itemNum);
			}
		}
		
		SystemMailService.getInstance().sendMail(MailParames.newBuilder()
		        .setPlayerId(roleid)
		        .setMailId(MailId.REWARD_MAIL)
		        .setAwardStatus(MailRewardStatus.NOT_GET)
		        .addSubTitles(title)
		        .addContents(content)
		        .setRewards(items)
		        .build());
		
		result.getBody().put("Result", 0);
		result.getBody().put("RetMsg", "");
		
		return result;
	}

}
