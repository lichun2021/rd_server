package com.hawk.game.idipscript.mail;

import java.util.ArrayList;
import java.util.List;

import org.hawk.log.HawkLog;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import org.hawk.task.HawkTaskManager;
import org.hawk.thread.HawkTask;
import org.hawk.thread.HawkThreadPool;

import com.alibaba.fastjson.JSONObject;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.idipscript.util.IdipUtil;
import com.hawk.game.item.AwardItems;
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
import com.hawk.log.Action;


/**
 * 邮件发放积分奖励 -- 10282148
 *
 * localhost:8081/idip/4445
 *
 * @author lating
 */
@HawkScript.Declare(id = "idip/4445")
public class SendMailScoreHandler extends IdipScriptHandler {
	
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		Player player = IdipUtil.checkAccountAndPlayer(request, result);
		if (player == null) {
			return result;
		}
		
		String mailTitle = request.getJSONObject("body").getString("MailTitle");
		String mailContent = request.getJSONObject("body").getString("MailContent");
		int scoreType = request.getJSONObject("body").getIntValue("PointsId");
		int addCount = request.getJSONObject("body").getIntValue("ItemNum");
		if (addCount <= 0) {
			result.getBody().put("Result", IdipConst.SysError.PARAM_ERROR);
			result.getBody().put("RetMsg", "ItemNum negative error");
			return result;
		}
		
		List<ItemInfo> items = new ArrayList<ItemInfo>(1);
		
		switch (scoreType) {
			case PlayerAttr.GOLD_VALUE:
			case PlayerAttr.COIN_VALUE:
			case PlayerAttr.LEVEL_VALUE:
			case PlayerAttr.EXP_VALUE:
			case PlayerAttr.VIP_POINT_VALUE:
			case PlayerAttr.VIT_VALUE:
			case PlayerAttr.GOLDORE_VALUE:
			case PlayerAttr.GOLDORE_UNSAFE_VALUE:
			case PlayerAttr.OIL_VALUE:
			case PlayerAttr.OIL_UNSAFE_VALUE:
			case PlayerAttr.STEEL_VALUE:
			case PlayerAttr.STEEL_UNSAFE_VALUE:
			case PlayerAttr.TOMBARTHITE_VALUE:
			case PlayerAttr.TOMBARTHITE_UNSAFE_VALUE:
			case PlayerAttr.GUILD_CONTRIBUTION_VALUE: // 联盟捐献荣誉值
			case PlayerAttr.MILITARY_SCORE_VALUE:     // 军演积分
			case PlayerAttr.ACTIVITY_SCORE_VALUE:
			case PlayerAttr.GUILD_SCORE_VALUE:
			case PlayerAttr.MILITARY_EXP_VALUE:
			case PlayerAttr.CYBORG_SCORE_VALUE: // 赛博积分
			case PlayerAttr.TRAVEL_SHOP_FRIENDLY_VALUE:
			case PlayerAttr.DYZZ_SCORE_VALUE:
			case PlayerAttr.CROSS_TALENT_POINT_VALUE:
			case PlayerAttr.NATION_MILITARY_VALUE:
				ItemInfo itemInfo = new ItemInfo(ItemType.PLAYER_ATTR_VALUE * GsConst.ITEM_TYPE_BASE, scoreType, addCount);
				items.add(itemInfo);
				break;
			default:
				HawkLog.errPrintln("SendMailScoreHandler unsupport award scoreType: {}, playerId: {}", scoreType, player.getId());
				result.getBody().put("Result", IdipConst.SysError.PARAM_ERROR);
				result.getBody().put("RetMsg", "unsupport PointsId");
				return result;
		}
		
		String serialID = request.getJSONObject("body").getString("Serial");
		if (!RedisProxy.getInstance().saveIdipSerialID(serialID)) {
			HawkLog.errPrintln("SendMailScore4445 request repeatd of serial: {}", serialID);
			result.getBody().put("Result", IdipConst.SysError.SERVER_BUSY);
			result.getBody().put("RetMsg", "request of Serial repeated");
			return result;
		}
		
		HawkLog.logPrintln("SendMailScoreHandler scoreType: {}, addCount: {}", scoreType, addCount);
		
		if (player.isActiveOnline()) {
			HawkThreadPool threadPool = HawkTaskManager.getInstance().getTaskExecutor();
			int threadIndex = Math.abs(player.getXid().hashCode() % threadPool.getThreadNum());
			HawkTaskManager.getInstance().postTask(new HawkTask() {
				@Override
				public Object run() {
					AwardItems awardItems = AwardItems.valueOf();
					awardItems.addItemInfos(items);
					awardItems.rewardTakeAffectAndPush(player, Action.GM_AWARD);
					return null;
				}
			}, threadIndex);
		} else {
			AwardItems awardItems = AwardItems.valueOf();
			awardItems.addItemInfos(items);
			awardItems.rewardTakeAffectAndPush(player, Action.GM_AWARD);
		}
		
		SystemMailService.getInstance().sendMail(MailParames.newBuilder()
		        .setPlayerId(player.getId())
		        .setMailId(MailId.REWARD_MAIL)
		        .setAwardStatus(MailRewardStatus.GET)
		        .addSubTitles(mailTitle)
		        .addContents(mailContent)
		        .setRewards(items)
		        .build());
		LogUtil.logIdipSensitivity(player, request, 0, addCount);
		result.getBody().put("Result", 0);
		result.getBody().put("RetMsg", "");
		return result;
	}
}


