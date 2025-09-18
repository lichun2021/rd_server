package com.hawk.game.service.mail;

import java.util.List;

import com.hawk.game.protocol.GuildManager.AuthId;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.MailService;

/**
 * 联盟邮件服务类
 * 
 * @author Nannan.Gao
 * @date 2016-11-30 18:28:14
 */
public class GuildMailService extends MailService {

	private static final GuildMailService instance = new GuildMailService();
	
	public static GuildMailService getInstance() {
		
		return instance;
	}
	
	
	/**
	 * 给全联盟成员发送邮件
	 * @param guildId
	 * @param parames
	 */
	public void sendGuildMail(String guildId, MailParames.Builder mailParames){
		for (String playerId : GuildService.getInstance().getGuildMembers(guildId)) {
			mailParames.setPlayerId(playerId);
			this.sendMail(mailParames.build());
		}
	}
	
	/**
	 * 给全联盟有相应权限的成员发送邮件
	 * @param guildId
	 * @param parames
	 */
	public void sendGuildMail(String guildId, AuthId authority,MailParames.Builder mailParames){
		List<String> list = GuildService.getInstance().getGuildMemberIdsHasAuthority(guildId, authority);
		if(list == null){
			return;
		}
		if(list.isEmpty()){
			return;
		}
		for (String playerId :list) {
			mailParames.setPlayerId(playerId);
			this.sendMail(mailParames.build());
		}
	}
	
}
