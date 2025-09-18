package com.hawk.robot.response.guildgift;

import java.util.List;
import java.util.Map;

import org.hawk.net.protocol.HawkProtocol;

import com.hawk.game.protocol.GuildBigGift.PBPlayerGuildGift;
import com.hawk.game.protocol.GuildBigGift.PBPlayerGuildGiftUpdate;
import com.hawk.game.protocol.HP;
import com.hawk.robot.GameRobotEntity;
import com.hawk.robot.annotation.RobotResponse;
import com.hawk.robot.data.GameRobotData;
import com.hawk.robot.response.RobotResponsor;

@RobotResponse(code = HP.code.PLAYER_SMAIL_GIFT_UPDATE_VALUE)
public class GuildBigGiftUpdateResponsor  extends RobotResponsor{

	@Override
	public void response(GameRobotEntity robotEntity, HawkProtocol protocol) {
		GameRobotData robotData = robotEntity.getData();
		PBPlayerGuildGiftUpdate giftList = protocol.parseProtocol(PBPlayerGuildGiftUpdate.getDefaultInstance());
		List<PBPlayerGuildGift> list = giftList.getGiftListList();
		Map<String, PBPlayerGuildGift> guildGiftInfo = robotData.getBasicData().getGuildGiftInfo();
		guildGiftInfo.clear();
		for(PBPlayerGuildGift gift:list){
			guildGiftInfo.put(gift.getId(), gift);
		}
		
	}

}
