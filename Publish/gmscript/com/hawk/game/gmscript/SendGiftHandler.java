package com.hawk.game.gmscript;

import java.net.URLDecoder;
import java.util.Map;

import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;

import com.hawk.game.config.ConstProperty;
import com.hawk.game.global.GlobalData;
import com.hawk.game.item.AwardItems;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const.MailRewardStatus;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.Script.ScriptError;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mail.SystemMailService;

/**
 * 给平台好友发送礼物
 *
 * localhost:8080/script/sendGift?playerId=7py-4uwfp-1&source=&award=
 *
 * @param playerId
 * @param source 赠送礼物的玩家的信息，格式为[guildTag]playerName
 * @param award 礼物内容，为空时自己生产，不为空时只接使用，格式为itemType_itemId_count;itemType_itemId_count
 * @author lating
 */
public class SendGiftHandler extends HawkScript {

	@Override
	public String action(Map<String, String> params, HawkScriptHttpInfo httpInfo) {
		try {
			Player player = GlobalData.getInstance().scriptMakesurePlayer(params);
			if (player == null) {
				return HawkScript.failedResponse(ScriptError.ACCOUNT_NOT_EXIST_VALUE, params.toString());
			}
			
			String source = params.get("source");
			if (HawkOSOperator.isEmptyString(source)) {
				return HawkScript.failedResponse(ScriptError.PARAMS_ERROR_VALUE, params.toString());
			}
			
			String awardStr = params.get("award");
			if (HawkOSOperator.isEmptyString(awardStr)) {
				awardStr = ConstProperty.getInstance().getGiveFriendGift();
			}
			
			AwardItems award = AwardItems.valueOf(awardStr);
			
			try {
				source = URLDecoder.decode(source, "utf-8");
			} catch (Exception e) {
				HawkException.catchException(e);
			}
			
			String sendGiftPlayerName = source;
			if (source.indexOf("[") > 0 && source.indexOf("]") > 0) {
				sendGiftPlayerName = source.split("]")[1];
			}
			
			SystemMailService.getInstance().sendMail(MailParames.newBuilder()
					.setPlayerId(player.getId())
					.setMailId(MailId.PRESENT_PLATFORM_FRIEND_GIFT)
					.setRewards(award.getAwardItems())
					.setAwardStatus(MailRewardStatus.NOT_GET)
					.addContents(player.getChannel(), sendGiftPlayerName)
					.addTitles(player.getChannel(), sendGiftPlayerName)
					.addSubTitles(player.getChannel(), sendGiftPlayerName)
					.build());
			
			return HawkScript.successResponse("");
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		return HawkScript.failedResponse(HawkScript.SCRIPT_ERROR, null);
	}
}
