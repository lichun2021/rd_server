package com.hawk.game.player.item.impl;

import org.hawk.net.protocol.HawkProtocol;

import com.hawk.game.config.ItemCfg;
import com.hawk.game.config.NationConstCfg;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.nation.NationService;
import com.hawk.game.nation.tech.NationTechCenter;
import com.hawk.game.player.Player;
import com.hawk.game.player.item.AbstractItemUseEffect;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.Const.ItemType;
import com.hawk.game.protocol.Const.MailRewardStatus;
import com.hawk.game.protocol.Item.HPItemTipsResp;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.National.NationbuildingType;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mail.SystemMailService;

public class NationTechItemUseEffect extends AbstractItemUseEffect {

	@Override
	public int itemType() {
		return Const.ToolType.NATIONAL_TECH_VALUE;
	}

	@Override
	public boolean useItemCheck(Player player, ItemCfg itemCfg, int itemId, int itemCount, int protoType, String targetId) {
		if(player.isCsPlayer()) {
			player.sendError(protoType, Status.CrossServerError.CROSS_PROTOCOL_SHIELD_VALUE, 0);
			return false;
		}
		NationTechCenter center = (NationTechCenter)NationService.getInstance().getNationBuildingByType(NationbuildingType.NATION_TECH_CENTER);
		if (center == null || center.getLevel() <= 0) {
			return false;
		}
		if (NationConstCfg.getInstance().getMissionWeekLimit() <= center.getDailyTechAdd()) {
			player.sendError(protoType, Status.Error.NATION_TECH_TOOL_ENOUGTH, 0);
			return false;
		}
		return true;
	}

	@Override
	public boolean useEffect(Player player, ItemCfg itemCfg, int itemCount, String targetId) {
		NationTechCenter center = (NationTechCenter)NationService.getInstance().getNationBuildingByType(NationbuildingType.NATION_TECH_CENTER);
		if (center == null || center.getLevel() <= 0) {
			return false;
		}
		
		HPItemTipsResp.Builder builder = HPItemTipsResp.newBuilder();
		builder.setItemId(itemCfg.getId());
		
		// 计算添加量
		int allVal = itemCfg.getNationTech()* itemCount;
		// 先算出剩余量
		int leftVal = NationConstCfg.getInstance().getMissionWeekLimit() - center.getDailyTechAdd();
		// 如果还够加，就直接加上
		if(leftVal >= allVal) {
			center.changeNationTechValue(allVal);
			center.addDailyTechAdd(allVal);
			builder.setItemCount(itemCount);
		} else {
			// 如果溢出了，计算出需要返还的道具数量
			// 算出剩余的数量 再加1个
			int needCount = (leftVal / itemCfg.getNationTech());
			if(leftVal % itemCfg.getNationTech() != 0){
				needCount++; // 有余数就多加一个
			}
			allVal = itemCfg.getNationTech() * needCount;
			
			int backCount = itemCount - needCount;
			
			// 这里直接加满就行
			center.changeNationTechValue(leftVal);
			center.addDailyTechAdd(leftVal);
			
			ItemInfo itemInfo = new ItemInfo();
			itemInfo.setType(ItemType.TOOL_VALUE);
			itemInfo.setItemId(itemCfg.getId());
			itemInfo.setCount(backCount);
			// 发送返还邮件
			SystemMailService.getInstance().sendMail(MailParames.newBuilder()
				.setPlayerId(player.getId())
				.setMailId(MailId.NATIONAL_TECH_BACK_ITEM)
				.addReward(itemInfo)
				.setAwardStatus(MailRewardStatus.NOT_GET)
				.build());
			
			builder.setItemCount(needCount);
		}
		// 发送使用成功消息
		player.sendProtocol(HawkProtocol.valueOf(HP.code.ITEM_USE_TIPS_VALUE, builder));
		
		center.tlogTools(player, center.getTechValue(), center.getDailyTechAdd());
		return true;
	}

}
