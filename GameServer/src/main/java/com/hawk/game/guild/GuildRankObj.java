package com.hawk.game.guild;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkTime;

import com.hawk.game.config.AwardCfg;
import com.hawk.game.config.GuildDonateRankAwardCfg;
import com.hawk.game.entity.GuildInfoObject;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.LocalRedis;
import com.hawk.game.item.AwardItems;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const.MailRewardStatus;
import com.hawk.game.protocol.GuildManager.DonateRankType;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.mail.GuildMailService;
import com.hawk.game.service.mail.MailParames;


public class GuildRankObj {
	private String guildId;
	// 日排行
	private Map<String, GuildDonateRank> dailyRank;

	// 日排行
	private Map<String, GuildDonateRank> weekRank;

	// 日排行
	private Map<String, GuildDonateRank> totalRank;
	
	public GuildRankObj(String guildId){
		super();
		this.guildId = guildId;
		this.dailyRank = new HashMap<>();
		this.weekRank = new HashMap<>();
		this.totalRank = new HashMap<>();
	}
	
	public GuildRankObj(String guildId, Map<String, String> dailyMap, Map<String, String> weekMap, Map<String, String> totalMap) {
		super();
		this.guildId = guildId;
		this.dailyRank = initRankMap(dailyMap);
		this.weekRank = initRankMap(weekMap);
		this.totalRank = initRankMap(totalMap);
	}
	
	/**
	 * 解析redis读取的排行数据
	 * @param map
	 * @return
	 */
	private Map<String, GuildDonateRank> initRankMap(Map<String, String> map) {
		Map<String, GuildDonateRank> rankMap = new HashMap<>();
		if (map != null) {
			for (Entry<String, String> entry : map.entrySet()) {
				rankMap.put(entry.getKey(), new GuildDonateRank(entry.getKey(), entry.getValue()));
			}
		}
		return rankMap;
	}
	
	/**
	 * 跨天/跨周检测
	 */
	public void crossCheck(){
		GuildInfoObject guildInfo = GuildService.getInstance().getGuildInfoObject(this.guildId);
		long lastCheckTime = guildInfo.getLastDonateCheckTime();
		long currentTime = HawkTime.getMillisecond();
		if(!HawkTime.isSameDay(lastCheckTime, currentTime)){
			dailyRank.clear();
			clearRank(DonateRankType.DAILY_RANK);
		}
		if(!HawkTime.isSameWeek(lastCheckTime, currentTime)){
			sendAward();
			
			weekRank.clear();
			clearRank(DonateRankType.WEEK_RANK);
		}
		guildInfo.updateLastDonateCheckTime(currentTime);
	}
	
	/**
	 * 根据类型获取排行信息
	 * @param rankType
	 * @return
	 */
	public List<GuildDonateRank> getRankList(DonateRankType rankType) {
		List<GuildDonateRank> list = new ArrayList<>();
		switch (rankType) {
		case DAILY_RANK:
			list = new ArrayList<>(dailyRank.values());
			break;
		case WEEK_RANK:
			list = new ArrayList<>(weekRank.values());
			break;
		case TOTAL_RANK:
			list = new ArrayList<>(totalRank.values());
			break;
		default:
			list = new ArrayList<>();
			break;
		}
		if (list.isEmpty()) {
			return list;
		}
		// 根据联盟贡献进行排序
		Collections.sort(list, new Comparator<GuildDonateRank>() {
			@Override
			public int compare(GuildDonateRank arg0, GuildDonateRank arg1) {
				int gap = arg1.getContribution() - arg0.getContribution();
				if (gap != 0) {
					return gap;
				}
				gap = arg1.getDonate() - arg0.getDonate();
				if (gap != 0) {
					return gap;
				}
				return 0;
			}
		});
		return list;
	}
	
	/**
	 * 移除成员捐献信息
	 * @param playerId
	 */
	public void onMemberRemove(String playerId){
		dailyRank.remove(playerId);
		weekRank.remove(playerId);
		totalRank.remove(playerId);
		LocalRedis.getInstance().removePlayerGuildDonate(this.guildId, playerId);
	}
	
	/**
	 * 联盟解散移除联盟捐献信息
	 */
	public void onGuildDismiss(){
		LocalRedis.getInstance().removeGuildDonateInfo(this.guildId, DonateRankType.DAILY_RANK);
		LocalRedis.getInstance().removeGuildDonateInfo(this.guildId, DonateRankType.WEEK_RANK);
		LocalRedis.getInstance().removeGuildDonateInfo(this.guildId, DonateRankType.TOTAL_RANK);
	}
	
	/**
	 * 联盟成员捐献
	 */
	public void onMemberDonate(String playerId, int addDonate, int addCont) {
		if (dailyRank.containsKey(playerId)) {
			dailyRank.get(playerId).onDonate(addDonate, addCont);
		} else {
			dailyRank.put(playerId, new GuildDonateRank(playerId, addDonate, addCont));
		}
		if (weekRank.containsKey(playerId)) {
			weekRank.get(playerId).onDonate(addDonate, addCont);
		} else {
			weekRank.put(playerId, new GuildDonateRank(playerId, addDonate, addCont));
		}
		if (totalRank.containsKey(playerId)) {
			totalRank.get(playerId).onDonate(addDonate, addCont);
		} else {
			totalRank.put(playerId, new GuildDonateRank(playerId, addDonate, addCont));
		}
		LocalRedis.getInstance().updatePlayerGuildDonate(this.guildId, playerId, dailyRank.get(playerId).toString(), weekRank.get(playerId).toString(),
				totalRank.get(playerId).toString());
	}
	
	/**
	 * 清除榜单信息
	 * @param type
	 */
	public void clearRank(DonateRankType type){
		LocalRedis.getInstance().removeGuildDonateInfo(this.guildId, type);
	}
	
	/**
	 * 发送周排行奖励
	 */
	public void sendAward(){
		List<GuildDonateRank> rankList = getRankList(DonateRankType.WEEK_RANK);
		if(rankList.isEmpty()){
			return;
		}
		List<String> topNames = new ArrayList<>();
		int rank = 1;
		for (int i = 0; i < rankList.size(); i++) {
			GuildDonateRankAwardCfg cfg = HawkConfigManager.getInstance().getConfigByKey(GuildDonateRankAwardCfg.class, rank);

			if (cfg == null) {
				continue;
			}
			GuildDonateRank donateRank = rankList.get(i);
			String playerId = donateRank.getPlayerId();
			Player player = GlobalData.getInstance().makesurePlayer(playerId);
			if(player == null){
				continue;
			}
			if (player.isZeroEarningState()) {
				continue;
			}
			topNames.add(player.getName());
			AwardCfg awardCfg = HawkConfigManager.getInstance().getConfigByKey(AwardCfg.class, cfg.getAward());
			AwardItems killAward = AwardItems.valueOf();
			killAward.appendAward(awardCfg.getRandomAward());
			GuildMailService.getInstance().sendMail(MailParames.newBuilder().setPlayerId(donateRank.getPlayerId()).setMailId(MailId.GUILD_DONATE_RANK_AWARD).addContents(rank)
					.setRewards(killAward.getAwardItems()).setAwardStatus(MailRewardStatus.NOT_GET).build());
			rank++;
		}
		GuildMailService.getInstance().sendGuildMail(this.guildId, MailParames.newBuilder()
				.setMailId(MailId.GUILD_DONATE_RANK_NOTICE)
				.addContents(topNames.toArray()));
	}

}
