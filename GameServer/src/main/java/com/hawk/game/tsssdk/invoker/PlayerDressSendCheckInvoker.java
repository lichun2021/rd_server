package com.hawk.game.tsssdk.invoker;

import java.util.ArrayList;
import java.util.List;

import org.hawk.config.HawkConfigManager;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkTime;

import com.alibaba.fastjson.JSONObject;
import com.hawk.game.config.PlayerDressSendCfg;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.item.ConsumeItems;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.Const.MailRewardStatus;
import com.hawk.game.protocol.Dress.PlayerDressPlayerInfo;
import com.hawk.game.protocol.Mail.PBPlayerDressAskMailContent;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.SysProtocol.HPOperateSuccess;
import com.hawk.game.service.MailService;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.tsssdk.Category;
import com.hawk.game.tsssdk.GameMsgCategory;
import com.hawk.game.util.LogUtil;
import com.hawk.log.Action;

@Category(scene = GameMsgCategory.PLAYER_SEND_DRESS)
public class PlayerDressSendCheckInvoker implements TsssdkInvoker {

	// mailId,reqId,reqPlayerId
	@Override
	public int invoke(Player player, int result, String sendMsg, int protocol, String callback) {
		if (result != 0) {
			player.sendError(protocol, Status.Error.ASK_DRESS_DIRTY_WORD, 0);
			return 0;
		}
		
		JSONObject json = JSONObject.parseObject(callback);
		MailId mailId = MailId.valueOf(json.getIntValue("mailId"));
		String destPlayerId = json.getString("destPlayerId");
		int sendItemId = json.getIntValue("itemId");
		String itemStr = json.getString("itemStr");
		json.remove("mailId");
		json.remove("destPlayerId");
		json.remove("itemId");
		json.remove("itemStr");
		Player tPlayer = GlobalData.getInstance().makesurePlayer(destPlayerId);
		if(tPlayer.isGetDressEnough()){
			player.sendError(protocol, Status.Error.ASK_DRESS_ENOUGH_THIS_WEEK_SEND, 0);
			return 0;
		}

		List<ItemInfo> needItems = new ArrayList<>();
		for (String key : json.keySet()) {
			needItems.add(ItemInfo.valueOf(json.getString(key)));
		}

		ConsumeItems consumteItems = ConsumeItems.valueOf();
		consumteItems.addConsumeInfo(needItems);
		if (!consumteItems.checkConsume(player)) {
			return 0;
		}
		consumteItems.consumeAndPush(player, Action.PLAYER_DRESS_SEND_COST);


		
		// 增加记录
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
		builder.setSendItem(itemStr);
		
		PlayerDressSendCfg dressSendCfg = HawkConfigManager.getInstance().getKVInstance(PlayerDressSendCfg.class);
		List<PlayerDressPlayerInfo> list = player.getData().getPlayerDressSendEntities();
		// 添加记录
		list.add(0, builder.build());
		while (list.size() > 0 && list.size() > dressSendCfg.getGiveLogNum()) {
			list.remove(list.size() - 1);
		}
		RedisProxy.getInstance().addPlayerAllDressSendInfo(player.getId(), builder.build(),
				dressSendCfg.getGiveLogNum());

		HPOperateSuccess.Builder succResp = HPOperateSuccess.newBuilder().setHpCode(protocol);
		player.sendProtocol(HawkProtocol.valueOf(HP.sys.OPERATE_SUCCESS, succResp));
		tPlayer.addGetDressNum();
		// 给对方发邮件
		PBPlayerDressAskMailContent.Builder mailBuilder = PBPlayerDressAskMailContent.newBuilder()
				.setPlayerId(player.getId()).setPlayerName(player.getName()).setPfIcon(player.getPfIcon())
				.setLevel(player.getLevel()).setCityLevel(player.getCityLevel()).setPower(player.getPower())
				.setGuildTag((null == player.getGuildTag()) ? "" : player.getGuildTag())
				.setGuildName((null == player.getGuildName()) ? "" : player.getGuildName())
				.setGuildId(player.getGuildId()).setLastOpTime(HawkTime.getMillisecond())
				.setSendItem(itemStr).setContent(sendMsg);
		MailService.getInstance()
				.sendMail(MailParames.newBuilder().setPlayerId(destPlayerId).setMailId(mailId)
						.addSubTitles(player.getName()).addSubTitles(sendItemId)
						.setRewards(itemStr).setAwardStatus(MailRewardStatus.NOT_GET)
						.addContents(mailBuilder).build());

		LogUtil.logPlayerDressSend(player, 1, 0, destPlayerId, itemStr);
		return 0;
	}

}
