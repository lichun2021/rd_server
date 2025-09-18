package com.hawk.game.guild.guildrank.data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.googlecode.protobuf.format.JsonFormat;
import com.hawk.game.protocol.GuildManager.AuthId;
import com.hawk.game.protocol.GuildManager.GuildGetRankResp;
import com.hawk.game.protocol.GuildManager.GuildRankType;
import com.hawk.game.protocol.Mail.PBGuildRankContent;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.MailService;
import com.hawk.game.service.mail.MailParames;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 联盟排行榜跨天数据统计 (邮件待发数据)
 * 
 * @Desc
 * @author RickMei
 * @Date 2018年11月14日 下午6:57:34
 */
public class GuildMailCount {
	private String guildId;

	private Map<GuildRankType, GuildGetRankResp> rankMap;

	public String getGuildId() {
		return guildId;
	}

	public Map<GuildRankType, GuildGetRankResp> getRankMap() {
		return rankMap;
	}

	public GuildMailCount(String guildid) {
		this.guildId = guildid;
		this.rankMap = new HashMap<>();
	}

	/**
	 * 添加统计数据
	 * 
	 * @Desc
	 * @param rType
	 * @param newResp
	 * @author RickMei
	 * @Date 2018年11月14日 下午7:23:31
	 */
	public void putRankCount(GuildRankType rType, GuildGetRankResp newResp) {
		rankMap.put(rType, newResp);
	}

	/**
	 * 按照guild 分发邮件
	 * 
	 * @Desc
	 * @author RickMei
	 * @Date 2018年11月14日 下午7:24:22
	 */
	public void sendMemberRankMail() {
		List<String> members = GuildService.getInstance().getGuildMemberIdsHasAuthority(guildId, AuthId.RECV_RANKMAIL);
		PBGuildRankContent.Builder contents = PBGuildRankContent.newBuilder();
		for (Map.Entry<GuildRankType, GuildGetRankResp> entry : rankMap.entrySet()) {
			if (entry.getValue().getRankInfoCount() > 0) {
				contents.addRanks(entry.getValue());
			}
		}

		if (!contents.getRanksList().isEmpty() && !members.isEmpty()) {
			for (String playerId : members) {
				MailParames.Builder builder = MailParames.newBuilder().setPlayerId(playerId)
						.setMailId(MailId.GUILD_RANK_DAILY_PUSH_TOP3).addContents(contents);
				MailService.getInstance().sendMail(builder.build());
			}
			// 发的邮件记录日志 每个联盟一封
			Logger logger = LoggerFactory.getLogger("Server");
			if (null != logger) {
				logger.info("guildrank sendmail guildId = {}, content = {}", getGuildId(),
						JsonFormat.printToString(contents.build()));
			}
		}
	}
}
