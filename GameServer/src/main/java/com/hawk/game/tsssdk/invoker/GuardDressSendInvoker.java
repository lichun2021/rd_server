package com.hawk.game.tsssdk.invoker;

import org.hawk.config.HawkConfigManager;

import com.alibaba.fastjson.JSONObject;
import com.hawk.game.config.GuardianGiveKvCfg;
import com.hawk.game.config.GuardianItemDressCfg;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.item.ConsumeItems;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.module.PlayerRelationModule;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.Const.ItemType;
import com.hawk.game.protocol.Const.MailRewardStatus;
import com.hawk.game.protocol.Friend.GuardSendAndBlagHistoryInfo;
import com.hawk.game.protocol.Mail.PBPlayerGuardDressMailContent;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.service.MailService;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.tsssdk.Category;
import com.hawk.game.tsssdk.GameMsgCategory;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.LogUtil;
import com.hawk.log.Action;

@Category(scene = GameMsgCategory.SEND_GUARD_DRESS)
public class GuardDressSendInvoker implements TsssdkInvoker {

	@Override
	public int invoke(Player player, int result, String sendWord, int protocol, String callback) {
		if (result != 0) {
			player.sendError(protocol, Status.NameError.CONTAIN_ILLEGAL_CHART_VALUE, 0);
			return 0;
		}
		
		JSONObject json = JSONObject.parseObject(callback);
		String tPlayerId = json.getString("playerId");
		int mailId = json.getIntValue("mailId");
		int itemId = json.getIntValue("itemId");
		
		Player tPlayer = GlobalData.getInstance().makesurePlayer(tPlayerId);
		if(tPlayer.isGetDressEnough()){
			player.sendError(protocol, Status.Error.ASK_DRESS_ENOUGH_THIS_WEEK_SEND, 0);
			return 0;
		}
		GuardianGiveKvCfg kvCfg = HawkConfigManager.getInstance().getConfigByIndex(GuardianGiveKvCfg.class, 0);
		PlayerRelationModule module = player.getModule(GsConst.ModuleType.RELATION);
		
		GuardianItemDressCfg dressCfg = HawkConfigManager.getInstance().getConfigByKey(GuardianItemDressCfg.class, itemId);
		//在这里扣吧,不然又敏感字还扣了别人的东西就尴尬了
		ConsumeItems messagerCost = ConsumeItems.valueOf();		
		if( dressCfg.getValidTime() < 0){
			int num = player.getData().getItemNumByItemId(kvCfg.getItemList2().get(0).getItemId());
			if(num > 0){
				messagerCost.addConsumeInfo(kvCfg.getItemList2());
			}else{
				messagerCost.addConsumeInfo(kvCfg.getItemList3());
			}			
		}else{
			int num = player.getData().getItemNumByItemId(kvCfg.getItemList().get(0).getItemId());
			if(num > 0){
				messagerCost.addConsumeInfo(kvCfg.getItemList());
			}else{
				messagerCost.addConsumeInfo(kvCfg.getItemList3());
			}
		}
		
		ItemInfo sendItem = new ItemInfo(ItemType.TOOL_VALUE, itemId, 1);
		messagerCost.addConsumeInfo(sendItem, false);	
		if (!messagerCost.checkConsume(player)) {
			return 0;
		}
		messagerCost.consumeAndPush(player, Action.GUARD_DRESS_SEND);
		
		// 增加记录																
		GuardSendAndBlagHistoryInfo.Builder builder = module.builderHistoryInfo(tPlayer, sendItem, 0);
		RedisProxy.getInstance().addGuardDressSendHistory(player.getId(), builder.build(),
				kvCfg.getGiveLogNum());

		player.responseSuccess(protocol);
		tPlayer.addGetDressNum();
		// 给对方发邮件
		PBPlayerGuardDressMailContent.Builder contenuBuilder = module.buildPBPlayerGuardDressMailContent(player, sendItem, 0, sendWord);
		MailService.getInstance()
				.sendMail(MailParames.newBuilder().setPlayerId(tPlayerId).setMailId(MailId.valueOf(mailId))
						.addSubTitles(player.getName()).addSubTitles(itemId)
						.setRewards(sendItem.toString()).setAwardStatus(MailRewardStatus.NOT_GET)
						.addContents(contenuBuilder).build());
		
		LogUtil.logPlayerDressSend(player, 1, 0, tPlayerId, sendItem.toString());
		
		return 0;
	}

}
