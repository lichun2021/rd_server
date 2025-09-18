package com.hawk.robot.action.mail;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hawk.annotation.RobotAction;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkRand;
import org.hawk.robot.HawkRobotAction;
import org.hawk.robot.HawkRobotEntity;

import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Mail.HPCancelSaveMailReq;
import com.hawk.game.protocol.Mail.HPCheckMailReq;
import com.hawk.game.protocol.Mail.HPDelMailByIdReq;
import com.hawk.game.protocol.Mail.HPDelMailByTypeReq;
import com.hawk.game.protocol.Mail.HPGetMailRewardReq;
import com.hawk.game.protocol.Mail.HPMarkReadMailReq;
import com.hawk.game.protocol.Mail.HPSaveMailReq;
import com.hawk.game.protocol.Mail.MailLiteInfo;
import com.hawk.robot.GameRobotEntity;

/**
 * 模拟邮件
 * 
 * @author Nannan.Gao
 * @date 2017-1-17 14:19:21
 */
@RobotAction(valid = false)
public class PlayerMailAction extends HawkRobotAction {
	
	@Override
	public void doAction(HawkRobotEntity robotEntity) {
		int requestType = HawkRand.randInt(18);
		int mailType = HawkRand.randInt(6);
		GameRobotEntity gameRobotEntity = (GameRobotEntity) robotEntity;
		
		if (mailType == 0) {
			return;
		}
		
		Map<String, MailLiteInfo> mailObjects = gameRobotEntity.getBasicData().getMailObjects();
		
		List<MailLiteInfo> mailList = new ArrayList<MailLiteInfo>();
		for (MailLiteInfo mailInfo : mailObjects.values()) {
			if (mailInfo.getType() == mailType) {
				mailList.add(mailInfo);
			}
		}
		
		if (requestType == 1) {
			// 标记邮件已读
			HPMarkReadMailReq.Builder builder = HPMarkReadMailReq.newBuilder();
			builder.setType(mailType);
			
			int index = HawkRand.randInt(mailList.size() -1);
			MailLiteInfo mailInfo = mailList.get(index);
			builder.addId(mailInfo.getId());
			
			gameRobotEntity.sendProtocol(HawkProtocol.valueOf(HP.code.MAIL_MARK_READ_C_VALUE, builder));
			return;
			
		} else if (requestType == 2) {
			// 通过邮件ID删除邮件
			HPDelMailByIdReq.Builder builder = HPDelMailByIdReq.newBuilder();
			builder.setType(mailType);
			
			int index = HawkRand.randInt(mailList.size() -1);
			MailLiteInfo mailInfo = mailList.get(index);
			builder.addId(mailInfo.getId());
			
			mailObjects.remove(mailInfo.getId());
			
			gameRobotEntity.sendProtocol(HawkProtocol.valueOf(HP.code.MAIL_DEL_MAIL_BY_ID_C_VALUE, builder));
			return;
			
		} else if (requestType == 3) {
			// 通过邮件类型删除邮件
			HPDelMailByTypeReq.Builder builder = HPDelMailByTypeReq.newBuilder();
			builder.setType(mailType);
			
			for (MailLiteInfo mailInfo : mailList) {
				mailObjects.remove(mailInfo.getId());
			}
			
			gameRobotEntity.sendProtocol(HawkProtocol.valueOf(HP.code.MAIL_DEL_MAIL_BY_TYPE_C_VALUE, builder));
			return;
			
		} else if (requestType == 4) {
//			MAIL_CREATE_CHATROOM_C		= 2106;	//创建聊天室

		} else if (requestType == 5) {
//			MAIL_SEND_CHATROOM_MSG_C	= 2108;	//发送聊天室消息

		} else if (requestType == 6) {
			// 查看邮件
			HPCheckMailReq.Builder builder = HPCheckMailReq.newBuilder();
			builder.setType(mailType);
			
			int index = HawkRand.randInt(mailList.size() -1);
			MailLiteInfo mailInfo = mailList.get(index);
			builder.setId(mailInfo.getId());
			
			gameRobotEntity.sendProtocol(HawkProtocol.valueOf(HP.code.MAIL_CHECK_MAIL_C_VALUE, builder));
			return;
			
		} else if (requestType == 7) {
			// 收藏邮件
			HPSaveMailReq.Builder builder = HPSaveMailReq.newBuilder();
			builder.setType(mailType);
			
			int index = HawkRand.randInt(mailList.size() -1);
			MailLiteInfo mailInfo = mailList.get(index);
			builder.addId(mailInfo.getId());
			
			gameRobotEntity.sendProtocol(HawkProtocol.valueOf(HP.code.MAIL_SAVE_C_VALUE, builder));
			return;
			
		} else if (requestType == 8) {
			// 取消收藏邮件
			HPCancelSaveMailReq.Builder builder = HPCancelSaveMailReq.newBuilder();
			builder.setType(mailType);
			
			int index = HawkRand.randInt(mailList.size() -1);
			MailLiteInfo mailInfo = mailList.get(index);
			builder.addId(mailInfo.getId());
			
			gameRobotEntity.sendProtocol(HawkProtocol.valueOf(HP.code.MAIL_CANCEL_SAVE_C_VALUE, builder));
			return;
			
		} else if (requestType == 9) {
			// 领取奖励
			HPGetMailRewardReq.Builder builder = HPGetMailRewardReq.newBuilder();
			builder.setType(mailType);
			
			int index = HawkRand.randInt(mailList.size() -1);
			MailLiteInfo mailInfo = mailList.get(index);
			builder.setId(mailInfo.getId());
			
			gameRobotEntity.sendProtocol(HawkProtocol.valueOf(HP.code.MAIL_REWARD_C_VALUE, builder));
			return;
			
		} else if (requestType == 10) {
//			MAIL_CHECK_MAIL_BY_TYPE_C	= 2118;	//查看某一类邮件
			
		} else if (requestType == 11) {
//			MAIL_SHARE_C				= 2120;	//邮件分享

		} else if (requestType == 12) {
//			MAIL_CHECK_OTHERPLAYER_MAIL_C	= 2121;	//查看其它玩家邮件

		} else if (requestType == 13) {
//			MAIL_SEND_GUILD_MAIL_C			= 2123;	//发送联盟全体邮件

		} else if (requestType == 14) {
//			MAIL_CHAT_HISTORY_PLAYERS_C		= 2124;	//请求历史交互玩家

		} else if (requestType == 15) {
//			MAIL_CHAT_ADD_PLAYERS_C			= 2126;	//添加玩家

		} else if (requestType == 16) {
//			MAIL_CHAT_DEL_PLAYERS_C			= 2127;	//踢出玩家

		} else if (requestType == 17) {
//			MAIL_LEAVE_CHATROOM_C			= 2128;	//退出聊天室

		} else if (requestType == 18) {
//			MAIL_CHANGE_CHATROOM_NAME_C		= 2129;	//修改聊天室名称

		}
		
	}
}
