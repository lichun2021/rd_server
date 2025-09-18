package com.hawk.robot.response.mail;

import java.util.List;
import com.hawk.robot.annotation.RobotResponse;
import org.hawk.net.protocol.HawkProtocol;

import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Mail.HPMailListSync;
import com.hawk.game.protocol.Mail.MailLiteInfo;
import com.hawk.robot.GameRobotEntity;
import com.hawk.robot.response.RobotResponsor;

@RobotResponse(code = HP.code.MAIL_LIST_SYNC_S_VALUE)
public class MailListResponsor extends RobotResponsor {

	@Override
	public void response(GameRobotEntity robotEntity, HawkProtocol protocol) {
		HPMailListSync mailList = protocol.parseProtocol(HPMailListSync.getDefaultInstance());
		List<MailLiteInfo> liteInfos = mailList.getMailList();
		robotEntity.getBasicData().refreshMailData(liteInfos);
	}

}
