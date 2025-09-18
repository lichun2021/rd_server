package com.hawk.game.service.mail;

import java.util.Objects;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.hawk.os.HawkException;

import com.alibaba.fastjson.JSONArray;
import com.google.protobuf.ByteString;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.LocalRedis;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const.MailRewardStatus;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.service.GlobalMail;
import com.hawk.game.service.MailService;
import com.hawk.game.service.mail.MailParames.Builder;

/**
 * 系统邮件服务类
 * 
 * @author Nannan.Gao
 * @date 2016-11-30 18:28:14
 */
public class SystemMailService extends MailService {

	private static final SystemMailService instance = new SystemMailService();

	public static SystemMailService getInstance() {

		return instance;
	}

	/**
	 * 发送全服邮件到账号
	 *
	 * @param player
	 * @param mail
	 * @return
	 */
	public boolean sendGlobalMail(Player player, GlobalMail mail) {
		if (player == null || mail == null) {
			return false;
		}

		// 国王战全服邮件只给有联盟的玩家发送
		if (!player.hasGuild() && isPresidentWarMailId(mail.getMailId())) {
			return false;
		}
		// 渠道,
		if (StringUtils.isNotEmpty(mail.getChannel()) && !Objects.equals(mail.getChannel(), player.getChannel())) {
			return false;
		}
		// 平台匹配
		if (StringUtils.isNotEmpty(mail.getPlatform()) && !Objects.equals(mail.getPlatform(), player.getPlatform())) {
			return false;
		}

		Builder paramers = MailParames.newBuilder()
				.setPlayerId(player.getId())
				.setMailId(MailId.valueOf(mail.getMailId()))
				.addTitles(mail.getTitle())
				.addSubTitles(mail.getSubTitle())
				.setRewards(mail.getReward())
				.setGlobalMailId(mail.getUuid());
		if (StringUtils.isNotEmpty(mail.getReward())) {
			paramers.setAwardStatus(MailRewardStatus.NOT_GET);
		}
		if(StringUtils.isNotEmpty(mail.getRewardStatus())){
			paramers.setAwardStatus(MailRewardStatus.valueOf(mail.getRewardStatus()));
		}
		mail.getMsg().forEach(paramers::addContents);
		return this.sendMail(paramers.build());
	}

	private boolean isPresidentWarMailId(int mailId) {
		return mailId == MailId.PRESIDENT_WAR_START_VALUE || mailId == MailId.PRESIDENT_WAR_END_VALUE;
	}

	public GlobalMail addGlobalMail(MailParames mailParames, long startTime, long endTime) {
		return this.addGlobalMail(mailParames, startTime, endTime, null, null);
	}

	/**
	 * 添加全服邮件
	 * 
	 * @param mailId
	 * @param title
	 *            主标题参数
	 * @param subTitle
	 *            副标题参数
	 * @param content
	 *            邮件体参数
	 * @param items
	 *            奖励信息
	 * @param startTime
	 *            生效时间
	 * @param endTime
	 *            失效时间
	 * @return
	 */
	public GlobalMail addGlobalMail(MailParames mailParames, long startTime, long endTime, String channel, String platform) {
		try {
			// 标题
			String _title = mailParames.getTitle();
			// 副标题
			String _subTitle = mailParames.getSubTitle();
			// 邮件内容参数
			JSONArray _content = new JSONArray();
			mailParames.getContent().getContentList().stream().map(ByteString::toStringUtf8).forEach(_content::add);
			// 奖励
			String _reward = mailParames.getReward();
			MailRewardStatus _rewstatus = mailParames.getAwardStatus();
			MailId mailId = mailParames.getMailId();

			GlobalMail mail = new GlobalMail();
			mail.setUuid(UUID.randomUUID().toString().replace("-", ""));
			mail.setMailId(mailId.getNumber());
			mail.setTitle(_title);
			mail.setSubTitle(_subTitle);
			mail.setMsg(_content);
			mail.setReward(_reward);
			mail.setStartTime(startTime);
			mail.setEndTime(endTime);
			mail.setChannel(channel);
			mail.setPlatform(platform);
			mail.setRewardStatus(_rewstatus.name());

			// 发送邮件
			if (LocalRedis.getInstance().addGlobalMail(mail)>0) {
				GlobalData.getInstance().addGlobalMail(mail);
			}
			return mail;
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return null;
	}

}
