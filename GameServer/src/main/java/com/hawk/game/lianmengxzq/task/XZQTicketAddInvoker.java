package com.hawk.game.lianmengxzq.task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkMsg;
import org.hawk.msg.invoker.HawkMsgInvoker;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;

import com.hawk.game.config.XZQConstCfg;
import com.hawk.game.entity.GuildInfoObject;
import com.hawk.game.global.GlobalData;
import com.hawk.game.lianmengxzq.XZQRedisData;
import com.hawk.game.lianmengxzq.XZQService;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.XZQ.PBXZQTicketsSyncResp;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mail.SystemMailService;

/**
 * 申请联盟官员
 * 
 * @author Jesse
 *
 */
public class XZQTicketAddInvoker extends HawkMsgInvoker {
	/** 玩家 */
	private List<Player> players;
	
	public XZQTicketAddInvoker(List<Player> players) {
		this.players = players;
	}

	@Override
	public boolean onMessage(HawkAppObj targetObj, HawkMsg msg) {
		//如果找不到玩家相关的联盟，直接退出
		String guildId = players.get(0).getGuildId();
		GuildInfoObject guild = GuildService.getInstance().getGuildInfoObject(guildId);
		if(Objects.isNull(guild)){
			return false;
		}
		//计算增加量
		int count = 0;
		List<String> playerIds = new ArrayList<>();
		for(Player player : players){
			if(HawkOSOperator.isEmptyString(player.getGuildId())){
				return false;
			}
			playerIds.add(player.getId());
		}
		int termId = XZQService.getInstance().getXZQTermId();
		int day = HawkTime.getYearDay();
		int sendNum = 1;
		int sendLimit = XZQConstCfg.getInstance().getAttackAchiveticketNum();
		Map<String,Integer> countMap = XZQRedisData.getInstance().getPlayerTicketNum(termId, day, playerIds);
		Map<String,String> updateMap = new HashMap<>(); 
		for(Player player : players){
			if(HawkOSOperator.isEmptyString(player.getGuildId())){
				return false;
			}
			int getNum = countMap.getOrDefault(player.getId(), 0);
			int lastNum = sendLimit - getNum;
			if(lastNum <= 0){
				continue;
			}
			int achieveCount =  Math.min(lastNum, sendNum);
			count += achieveCount;
			updateMap.put(player.getId(), String.valueOf(getNum + achieveCount));
			//发邮件
			SystemMailService.getInstance().sendMail(MailParames.newBuilder()
					.setPlayerId(player.getId())
					.setMailId(MailId.XZQ_TICKET_ACHIVE)
					.addTips(achieveCount)
					.build());
		}
		if(updateMap.size() > 0){
			guild.addXZQTickets(count); 
			XZQRedisData.getInstance().updatePlayerTicketNum(termId, day, updateMap);
			int ticketCount = guild.getXZQTickets();
			//更新推送
			PBXZQTicketsSyncResp.Builder builder = PBXZQTicketsSyncResp.newBuilder();
			builder.setGuildId(guildId);
			builder.setTicketCount(ticketCount);
			HawkProtocol proto = HawkProtocol.valueOf(HP.code.XZQ_TICKETS_SYNC_S_VALUE, builder);
			for (String playerId : GuildService.getInstance().getGuildMembers(guildId)) {
				Player member = GlobalData.getInstance().getActivePlayer(playerId);
				if (member == null) {
					continue;
				}
				member.sendProtocol(proto);
			}
		}
		return true;
	}



}
