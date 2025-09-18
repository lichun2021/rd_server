package com.hawk.game.tsssdk.invoker;

import java.util.List;

import org.hawk.config.HawkConfigManager;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkTime;

import com.alibaba.fastjson.JSONObject;
import com.hawk.game.config.PlayerDressSendCfg;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.Dress.PlayerDressPlayerInfo;
import com.hawk.game.protocol.Mail.PBPlayerDressAskMailContent;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.SysProtocol.HPOperateSuccess;
import com.hawk.game.service.MailService;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.tsssdk.Category;
import com.hawk.game.tsssdk.GameMsgCategory;
import com.hawk.game.util.LogUtil;

@Category(scene = GameMsgCategory.PLAYER_ASK_DRESS)
public class PlayerDressAskInvoker implements TsssdkInvoker {

	@Override
	public int invoke(Player player, int result, String askMsg, int protocol, String callback) {
		if (result != 0) {
			player.sendError(protocol, Status.Error.ASK_DRESS_DIRTY_WORD, 0);
			return 0;
		}
		if(player.isGetDressEnough()){
			player.sendError(protocol, Status.Error.ASK_DRESS_ENOUGH_THIS_WEEK_ASK, 0);
			return 0;
		}
		
		JSONObject json = JSONObject.parseObject(callback);
		MailId mailId = MailId.valueOf(json.getIntValue("mailId"));
		int reqId = json.getIntValue("id");
		String reqPlayerId = json.getString("playerId");
		PlayerDressSendCfg cfg = HawkConfigManager.getInstance().getKVInstance(PlayerDressSendCfg.class);
		
		// 查cd
		List<PlayerDressPlayerInfo> list = player.getData().getPlayerDressAskEntities();
		long curMs = HawkTime.getMillisecond();
		if (list.stream().anyMatch(e -> e.getPlayerId().equals(reqPlayerId)
				&& e.getLastOpTime() + cfg.getGetCDMs() > curMs)) {
			player.sendError(protocol, Status.Error.ASK_DRESS_ASK_CD_VALUE, 0);
			return 0;
		}
		
		Player tPlayer = GlobalData.getInstance().makesurePlayer(reqPlayerId);
		PlayerDressPlayerInfo.Builder builder = PlayerDressPlayerInfo.newBuilder();
		builder.setPlayerId(tPlayer.getId());
		builder.setPlayerName(tPlayer.getName());
		builder.setLevel(tPlayer.getLevel());
		builder.setPfIcon(tPlayer.getPfIcon());
		builder.setCityLevel(tPlayer.getCityLevel());
		builder.setGuildId(tPlayer.getGuildId());
		builder.setGuildName((null == tPlayer.getGuildName()) ? "" : tPlayer.getGuildName());
		builder.setGuildTag((null == tPlayer.getGuildTag()) ? "" : tPlayer.getGuildTag());
		builder.setPower(tPlayer.getPower());
		builder.setLastOpTime(HawkTime.getMillisecond());
		builder.setAskDressId(reqId);
		// 添加记录
		list.add(0, builder.build());
		while (list.size() > 0 && list.size() > cfg.getGetLogNum()) {
			list.remove(list.size() - 1);
		}
		RedisProxy.getInstance().addPlayerAllDressAskInfo(player.getId(), builder.build(),
				cfg.getGetLogNum());

		HPOperateSuccess.Builder succResp = HPOperateSuccess.newBuilder().setHpCode(protocol);
		player.sendProtocol(HawkProtocol.valueOf(HP.sys.OPERATE_SUCCESS, succResp));

		// 给对方发邮件
		PBPlayerDressAskMailContent.Builder mailBuilder = PBPlayerDressAskMailContent.newBuilder()
				.setPlayerId(player.getId()).setPlayerName(player.getName()).setPfIcon(player.getPfIcon())
				.setLevel(player.getLevel()).setCityLevel(player.getCityLevel()).setPower(player.getPower())
				.setGuildTag((null == player.getGuildTag()) ? "" : player.getGuildTag())
				.setGuildName((null == tPlayer.getGuildName()) ? "" : tPlayer.getGuildName())
				.setGuildId(player.getGuildId()).setLastOpTime(HawkTime.getMillisecond())
				.setContent(askMsg).setAskDressId(reqId);

		// 发邮件
		MailService.getInstance()
				.sendMail(MailParames.newBuilder().setPlayerId(reqPlayerId).setMailId(mailId)
						.addSubTitles(player.getName()).addSubTitles(reqId).addContents(mailBuilder)
						.build());

		LogUtil.logPlayerDressSend(player, 2, reqId, reqPlayerId, "");
		
		return 0;
	}

}
