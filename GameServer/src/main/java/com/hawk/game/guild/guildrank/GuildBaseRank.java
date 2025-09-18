package com.hawk.game.guild.guildrank;

import java.util.List;
import java.util.Set;

import org.hawk.config.HawkConfigManager;

import com.hawk.game.config.ControlProperty;
import com.hawk.game.config.GuildRankCfg;
import com.hawk.game.entity.GuildMemberObject;
import com.hawk.game.global.GlobalData;
import com.hawk.game.guild.guildrank.GuildRankTypedef.GRankStatus;
import com.hawk.game.guild.guildrank.GuildRankTypedef.GRankType;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.GuildManager;
import com.hawk.game.protocol.GuildManager.GuildGetRankResp;
import com.hawk.game.protocol.GuildManager.GuildRankInfo;
import com.hawk.game.service.GuildService;
import com.hawk.game.util.GameUtil;

import redis.clients.jedis.Tuple;

/**
 * 榜单基类
 * 
 * @author hawk
 *
 */
abstract public class GuildBaseRank {
	/**
	 * 开启或者不开启
	 */
	private GRankStatus openStatus = GRankStatus.NOTOPEN;
	/**
	 * 排行榜枚举
	 */
	protected GRankType rankType;

	protected boolean isGuildRank = false;

	GuildBaseRank(GRankType rType) {
		rankType = rType;
	}

	public boolean getIsGuildRank() {
		return isGuildRank;
	}

	public GRankType getRankType() {
		return rankType;
	}

	public void onPassDay() {
		saveTop3();
	};

	abstract public boolean delRankKey(String guildid, String playerId);

	abstract public void addRankVal(String guildid, String playerId, long val);

	abstract public Set<Tuple> getRankList(String guildId);

	abstract public Set<Tuple> getYesterDayRankList(String guildId);

	/**
	 * 返回数据组装
	 * 
	 * @Desc
	 * @param guildId
	 * @param rankList
	 * @param builder
	 */
	final private void constructGuildRankRoleInfo(String guildId, Set<Tuple> rankList,
			GuildGetRankResp.Builder builder) {
		if (null != rankList && !rankList.isEmpty()) {
			for (Tuple rankInfo : rankList) {
				String playerId = rankInfo.getElement();
				if (GuildService.getInstance().isPlayerInGuild(guildId, playerId)) {
					Player snaoshot = GlobalData.getInstance().makesurePlayer(playerId);
					if (snaoshot == null) {
						continue;
					}
					GuildManager.GuildRankInfo.Builder roleRankInfo = GuildManager.GuildRankInfo.newBuilder();
					roleRankInfo.setPlayerId(playerId);
					roleRankInfo.setPlayerName(snaoshot.getName());
					roleRankInfo.setIcon(snaoshot.getIcon());
					roleRankInfo.setPfIcon(snaoshot.getPfIcon());
					roleRankInfo.setAuthority(GuildService.getInstance().getPlayerGuildAuthority(playerId));
					roleRankInfo.setScore((long) rankInfo.getScore());
					GuildMemberObject member = GuildService.getInstance().getGuildMemberObject(playerId);
					if (null != member) {
						roleRankInfo.setOfficeId(member.getOfficeId());
					}
					builder.addRankInfo(roleRankInfo);
				}
			}
		}
	}

	/**
	 * 昨日排行榜返回数据获取
	 * 
	 * @Desc
	 * @param guildId
	 * @return
	 */
	final public GuildGetRankResp.Builder prepareYesterdayRankResp(String guildId) {
		GuildGetRankResp.Builder builder = GuildGetRankResp.newBuilder();
		builder.setRankType(rankType.getTypeId());
		Set<Tuple> rankList = getYesterDayRankList(guildId);
		constructGuildRankRoleInfo(guildId, rankList, builder);
		return builder;
	}

	/**
	 * 当前排行榜数据获取
	 * 
	 * @Desc
	 * @param guildId
	 * @return
	 */
	final public GuildGetRankResp.Builder prepareRankResp(String guildId) {
		GuildGetRankResp.Builder builder = GuildGetRankResp.newBuilder();
		builder.setRankType(rankType.getTypeId());
		Set<Tuple> rankList = getRankList(guildId);
		constructGuildRankRoleInfo(guildId, rankList, builder);
		return builder;
	}

	/**
	 * 保存联盟前3
	 * 
	 * @Desc
	 */
	public void saveTop3() {
		// fix bug 58095634 排行榜未解锁也发了邮件
		// 榜单总开关关闭不结榜
		if (ControlProperty.getInstance().getGuildrankSwitch() != 1) {
			return;
		}

		GuildRankCfg cfg = HawkConfigManager.getInstance().getConfigByKey(GuildRankCfg.class,
				getRankType().getTypeId().getNumber());

		if (null == cfg) {
			return;
		}

		// 配置表配 保存榜单条数 0不结榜
		if (cfg.getMailTopNumber() == 0) {
			return;
		}

		int serverOpenDays = GameUtil.getServerOpenDays();
		// 昨天是服务器开服第几天
		int yesterDayOpenDays = serverOpenDays;
		if (yesterDayOpenDays <= 0) {
			return;
		}
		--yesterDayOpenDays;
		// 帮单在昨日未开放不结榜
		if (0 != cfg.getOpenDay() && yesterDayOpenDays < cfg.getOpenDay()) {
			return;
		}
		// 榜单在昨已关闭不结榜
		if (0 != cfg.getCloseDay() && yesterDayOpenDays > cfg.getCloseDay()) {
			return;
		}
		List<String> guildIds = GuildService.getInstance().getGuildIds();
		for (String guildId : guildIds) {
			saveTop3(guildId);
		}
	}

	/**
	 * 保存联盟前三榜单
	 * 
	 * @Desc
	 * @param guildId
	 */
	public void saveTop3(String guildId) {
		// 添加排名到待发奖励
		GuildGetRankResp.Builder resp = prepareYesterdayRankResp(guildId);
		if (null != resp) {
			GuildGetRankResp.Builder newResp = GuildGetRankResp.newBuilder();
			newResp.setRankType(rankType.getTypeId());
			for (GuildRankInfo rankInfo : resp.getRankInfoList()) {
				if (newResp.getRankInfoCount() >= rankType.getTopN()) {
					break;
				}
				newResp.addRankInfo(rankInfo);
			}
			if (!newResp.getRankInfoList().isEmpty()) {
				GuildRankMgr.getInstance().putGuildRankCount(guildId, newResp);
			}
		}
	}

	public boolean isOpen() {
		return openStatus == GRankStatus.OPEN && ControlProperty.getInstance().getGuildrankSwitch() == 1;
	}

	public boolean isClosed() {
		return openStatus == GRankStatus.CLOSE || ControlProperty.getInstance().getGuildrankSwitch() != 1;
	}

	/**
	 * 关闭排行榜
	 * 
	 * @Desc
	 */
	final public void close() {
		this.openStatus = GRankStatus.CLOSE;
	}

	/**
	 * 开启排行榜
	 * 
	 * @Desc
	 */
	final public void open() {
		this.openStatus = GRankStatus.OPEN;
	}

}
