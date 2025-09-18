package com.hawk.robot.response.mail;

import java.util.List;
import com.hawk.robot.annotation.RobotResponse;
import org.hawk.net.protocol.HawkProtocol;

import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Mail.HPNewMailRes;
import com.hawk.game.protocol.Mail.MailLiteInfo;
import com.hawk.robot.GameRobotEntity;
import com.hawk.robot.response.RobotResponsor;

@RobotResponse(code = HP.code.MAIL_NEW_MAIL_S_VALUE)
public class NewMailResponsor extends RobotResponsor {

	@Override
	public void response(GameRobotEntity robotEntity, HawkProtocol protocol) {
		HPNewMailRes newMail = protocol.parseProtocol(HPNewMailRes.getDefaultInstance());
		List<MailLiteInfo> newLiteInfos = newMail.getMailList();
		robotEntity.getBasicData().refreshMailData(newLiteInfos);
	}

}
