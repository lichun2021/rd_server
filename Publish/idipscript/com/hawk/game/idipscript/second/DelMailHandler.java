package com.hawk.game.idipscript.second;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkMsg;
import org.hawk.msg.invoker.HawkMsgInvoker;
import org.hawk.os.HawkException;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import com.alibaba.fastjson.JSONObject;
import com.hawk.game.idipscript.util.IdipUtil;
import com.hawk.game.item.AwardItems;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.Mail.MailLiteInfo;
import com.hawk.game.protocol.Reward.RewardOrginType;
import com.hawk.game.service.MailService;
import com.hawk.game.util.LogUtil;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.idip.IdipConst;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;
import com.hawk.log.Action;

/**
 * 删除邮件
 *
 * localhost:8080/script/idip/4177
 * 
 * @author lating
 */
@HawkScript.Declare(id = "idip/4177")
public class DelMailHandler extends IdipScriptHandler {
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		Player player = IdipUtil.playerCheck(request, result);
		if (player == null) {
			return result;
		}

		String mailId = request.getJSONObject("body").getString("MailId");
		if (player.isActiveOnline()) {
			player.dealMsg(MsgId.IDIP_CHANGE_PLAYER_INFO, new MailDeleteMsgInvoker(player, mailId));
		} else {
			try {
				deleteMail(player, mailId);
			} catch (Exception e) {
				HawkException.catchException(e);
				result.getBody().put("Result", IdipConst.SysError.API_EXCEPTION);
				result.getBody().put("RetMsg", "mail remove failed");
				return result;
			}
		}
		
		LogUtil.logIdipSensitivity(player, request, 0, 1);
		result.getBody().put("Result", 0);
		result.getBody().put("RetMsg", "");
		return result;
	}
	
	/**
	 * 删除邮件
	 * 
	 * @param player
	 * @param mailId
	 */
	private static MailLiteInfo.Builder deleteMail(Player player, String mailId) {
		MailLiteInfo.Builder mail = MailService.getInstance().getMailEntity(mailId);
		AwardItems award = AwardItems.valueOf();
		if(Objects.nonNull(mail)){
			MailService.getInstance().getMailReward(player, mail, award);
			MailService.getInstance().delMail(mail);
		}
		
		if (award.getAwardItems().size() > 0) {
			award.rewardTakeMailAffectAndPush(player, Action.SYS_MAIL_AWARD, true, RewardOrginType.MAIL_REWARD);
		}
		
		if (player.isActiveOnline()) {
			player.kickout(Status.IdipMsgCode.IDIP_DEL_MAIL_VALUE, true, null);
		}
		
		return mail;
	}
	
	public static class MailDeleteMsgInvoker extends HawkMsgInvoker {
		
		private Player player;
		private String mailId;
		
		public MailDeleteMsgInvoker(Player player, String mailId) {
			this.player = player;
			this.mailId = mailId;
		}
		
		@Override
		public boolean onMessage(HawkAppObj targetObj, HawkMsg msg) {
			MailLiteInfo.Builder mail = deleteMail(player, mailId);
			if (mail != null) {
				List<String> mailIds = new ArrayList<>();
				mailIds.add(mailId);
				player.getPush().notifyMailDeleted(mail.getType(), mailIds);
			}
			return true;
		}
	}
}
