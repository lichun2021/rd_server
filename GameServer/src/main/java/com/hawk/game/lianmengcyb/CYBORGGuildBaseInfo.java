package com.hawk.game.lianmengcyb;

import java.util.HashSet;
import java.util.Set;

import com.hawk.game.lianmengcyb.CYBORGRoomManager.CYBORG_CAMP;
import com.hawk.game.lianmengcyb.order.CYBORGOrderCollection;

public class CYBORGGuildBaseInfo {
	public CYBORG_CAMP camp;
	public int campRootId;
	public String campGuild = "";
	public String campGuildName = "";
	public String campGuildTag = "";
	public String campServerId = "";
	public int campguildFlag;

	public int campGuildWarCount;
	public int campNuclearSendCount;
	public int campNianKillCount;
	public int campNianATKHonor;// 击杀机甲数
	public String campTeamName = "";
	public long campTeamPower;
	public boolean isCsGuild;
	public Set<String> playerIds = new HashSet<>();
	public CYBORGOrderCollection orderCollection;
	public int buildBuffLevel;
	public int buildBuffLevelExtra;
	public int buildBuffCfgLevel;

	public int honorRate; // 积分占权重 万分比
	public int cyborgItemTotal; // 瓜分赛博药 数量
}
