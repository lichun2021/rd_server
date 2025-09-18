package com.hawk.game.script;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

import org.hawk.os.HawkException;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import com.hawk.game.global.GlobalData;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const.MailRewardStatus;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.Script.ScriptError;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mail.SystemMailService;

/**
 * 给指定玩家发送带奖励邮件
 *
 * localhost:8080/script/mail?playerName=&title=&content=&award=a_b_c,a_b_c&count=10
 *
 * @param playerName  或 playerId
 * @param title       邮件标题
 * @param content     邮件内容
 * @param award       奖励物品
 * @param count       一次发送count封邮件
 * 
 * @author lating
 */
public class SendPlayerMailHandler extends HawkScript {
	@Override
	public String action(Map<String, String> params, HawkScriptHttpInfo httpInfo) {
		try {
			Player player = GlobalData.getInstance().scriptMakesurePlayer(params);
			if (player == null) {
				return HawkScript.failedResponse(ScriptError.ACCOUNT_NOT_EXIST_VALUE, params.toString());
			}
			
			String title = params.get("title");
			String content = params.get("content");
			try {
				title = new String(title.getBytes(), "utf-8");
				content = new String(content.getBytes(), "utf-8");
			} catch (UnsupportedEncodingException e) {
				HawkException.catchException(e);
			}
			
			int count = 1;
			if (params.containsKey("count")) {
				count = Integer.valueOf(params.get("count"));
				if (count <= 0) {
					count = 1;
				}
			}
			
			String awardStr = params.get("award");
			List<ItemInfo> items = ItemInfo.valueListOf(awardStr);
			for (int i=0; i<count; i++) {
				SystemMailService.getInstance().sendMail(MailParames.newBuilder()
						.setPlayerId(player.getId())
						.setMailId(MailId.REWARD_MAIL)
						.setAwardStatus(MailRewardStatus.NOT_GET)
						.addSubTitles(title)
						.addContents(content)
						.setRewards(items)
						.build());
			}
			
			logger.info("send account reward mail, playerId: {}, playerName: {}, award: {}", player.getId(), player.getName(), awardStr);
			
			return HawkScript.successResponse(null);
		} catch(Exception e) {
			HawkException.catchException(e);
		}
		
		return HawkScript.failedResponse(ScriptError.EXCEPTION_VALUE, "");
	}
}
