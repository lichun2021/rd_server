package com.hawk.game.idipscript.mail;

import java.util.ArrayList;
import java.util.List;
import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import org.hawk.uuid.HawkUUIDGenerator;

import com.alibaba.fastjson.JSONObject;
import com.hawk.game.GsConfig;
import com.hawk.game.config.ItemCfg;
import com.hawk.game.entity.GuildInfoObject;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.idipscript.util.IdipUtil;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.protocol.Const.ItemType;
import com.hawk.game.protocol.Const.MailRewardStatus;
import com.hawk.game.protocol.Const.PlayerAttr;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mail.SystemMailService;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.LogUtil;
import com.hawk.idip.IdipConst;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;

/**
 * 给指定联盟当前所有成员发放奖励 -- 10282198
 *
 * @author lating
 */
@HawkScript.Declare(id = "idip/4545")
public class SendGuildAwardMailHandler extends IdipScriptHandler {
	
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		String allianceId = request.getJSONObject("body").getString("GuildID");
		GuildInfoObject guildInfo = GuildService.getInstance().getGuildInfoObject(allianceId);
		if (guildInfo == null) {
			try {
				String guildId = HawkUUIDGenerator.longUUID2Str(Long.parseLong(allianceId));
				guildInfo = GuildService.getInstance().getGuildInfoObject(guildId);
			} catch (Exception e) {
				HawkLog.logPrintln("SendGuildAwardMailHandler failed, catch exception, GuildID: {}", allianceId);
				HawkException.catchException(e);
			} finally {
				if (guildInfo == null) {
					result.getBody().put("Result", IdipConst.SysError.ALLIANCE_NOT_FOUND);
					result.getBody().put("RetMsg", "guild not exist");
					return result;
				}
			}
		}
		
		int itemId = request.getJSONObject("body").getIntValue("ItemId");
		ItemCfg itemCfg = HawkConfigManager.getInstance().getConfigByKey(ItemCfg.class, itemId);
		if (itemCfg == null && PlayerAttr.valueOf(itemId) == null) {
			result.getBody().put("Result", IdipConst.SysError.API_EXCEPTION);
			result.getBody().put("RetMsg", "itemId error");
			return result;
		}
		
		String serialID = request.getJSONObject("body").getString("Serial");
		if (!RedisProxy.getInstance().saveIdipSerialID(serialID)) {
			HawkLog.errPrintln("SendGuildAwardMailHandler4545 request repeatd of serial: {}", serialID);
			result.getBody().put("Result", IdipConst.SysError.SERVER_BUSY);
			result.getBody().put("RetMsg", "request of Serial repeated");
			return result;
		}
		
		String title = request.getJSONObject("body").getString("MailTitle");
		title = IdipUtil.decode(title);
		String content = request.getJSONObject("body").getString("MailContent");
		content = IdipUtil.decode(content);
		int itemNum = request.getJSONObject("body").getIntValue("ItemNum");
		int type = itemCfg != null ? ItemType.TOOL_VALUE : ItemType.PLAYER_ATTR_VALUE;
		ItemInfo itemInfo = new ItemInfo(type * GsConst.ITEM_TYPE_BASE, itemId, itemNum);
		List<ItemInfo> awardItems = new ArrayList<ItemInfo>(1);
		awardItems.add(itemInfo);
		
		for (String playerId : GuildService.getInstance().getGuildMembers(guildInfo.getId())) {
			SystemMailService.getInstance().sendMail(MailParames.newBuilder()
					.setPlayerId(playerId)
					.setMailId(MailId.REWARD_MAIL)
					.setAwardStatus(MailRewardStatus.NOT_GET)
					.addSubTitles(title)
					.addContents(content)
					.setRewards(awardItems)
					.build());
		}
		
		HawkLog.logPrintln("idip sendGuildAward guildId: {}, local server: {}", guildInfo.getId(), GsConfig.getInstance().getServerId());
		LogUtil.logIdipSensitivity(null, request, 0, 0);
		result.getBody().put("Result", 0);
		result.getBody().put("RetMsg", "");
		return result;
	}
	
}
