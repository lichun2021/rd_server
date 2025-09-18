package com.hawk.game.tsssdk.invoker;

import org.hawk.config.HawkConfigManager;

import com.alibaba.fastjson.JSONObject;
import com.hawk.game.config.GuardianGiveKvCfg;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.LocalRedis;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.module.PlayerRelationModule;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.Friend.GuardSendAndBlagHistoryInfo;
import com.hawk.game.protocol.Mail.PBPlayerGuardDressMailContent;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.service.MailService;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.tsssdk.Category;
import com.hawk.game.tsssdk.GameMsgCategory;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.LogUtil;

@Category(scene = GameMsgCategory.BLAG_GUARD_DRESS)
public class GuardDressBlagInvoker implements TsssdkInvoker {

	@Override
	public int invoke(Player player, int result, String sendWord, int protocol, String callback) {
		if (result != 0) {
			player.sendError(protocol, Status.NameError.CONTAINS_FILTER_WORD, 0);
			return 0;
		}
		if(player.isGetDressEnough()){
			player.sendError(protocol, Status.Error.ASK_DRESS_ENOUGH_THIS_WEEK_ASK, 0);
			return 0;
		}
		JSONObject json = JSONObject.parseObject(callback);
		String tPlayerId = json.getString("playerId");
		int mailId = json.getIntValue("mailId");
		int dressId = json.getIntValue("dressId");
		
		Player tPlayer = GlobalData.getInstance().makesurePlayer(tPlayerId);
		GuardianGiveKvCfg kvCfg = HawkConfigManager.getInstance().getConfigByIndex(GuardianGiveKvCfg.class, 0);
		PlayerRelationModule module = player.getModule(GsConst.ModuleType.RELATION);
		//添加索要记录.
		GuardSendAndBlagHistoryInfo.Builder builder = module.builderHistoryInfo(tPlayer, null, dressId);										
		RedisProxy.getInstance().addGuardDressBlagHistory(player.getId(), builder.build(), kvCfg.getGetLogNum());

		player.responseSuccess(protocol);
		
		LocalRedis.getInstance().addGuardDressBlagLog(player.getId(), tPlayerId);
		// 发邮件
		PBPlayerGuardDressMailContent.Builder contenuBuilder = module.buildPBPlayerGuardDressMailContent(player, null, dressId, sendWord);
		MailService.getInstance()
		.sendMail(MailParames.newBuilder().setPlayerId(tPlayerId).setMailId(MailId.valueOf(mailId))
				.addSubTitles(player.getName()).addSubTitles(dressId).addContents(contenuBuilder)
				.build());
		
		LogUtil.logPlayerDressSend(player, 2, 0, tPlayerId, null);
		
		return 0;
	}

}
