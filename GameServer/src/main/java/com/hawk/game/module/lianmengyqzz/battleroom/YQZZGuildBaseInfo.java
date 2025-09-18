package com.hawk.game.module.lianmengyqzz.battleroom;

import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

import org.hawk.os.HawkException;

import com.google.common.collect.ImmutableMap;
import com.hawk.game.module.lianmengyqzz.battleroom.worldpoint.YQZZBuildType;
import com.hawk.game.protocol.Chat.ChatMsg;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.GuildManager.GuildSign;
import com.hawk.game.protocol.World.YQZZDeclareWar;

public class YQZZGuildBaseInfo {
	private final YQZZBattleRoom parent;

	public YQZZ_CAMP camp;
	public int campRootId;
	public String campGuild = "";
	public String campGuildName = "";
	public String campGuildTag = "";
	public String campServerId = "";
	public int campguildFlag;
	public String guildLeaderName = "";
	public String guildLeaderId = "";

	public int campGuildWarCount;
	public int campNuclearSendCount;
	public int campNianKillCount;
	public int campNianATKHonor;// 击杀机甲数
	public String campTeamName = "";
	public long campTeamPower;
	public boolean isCsGuild;
	public int declareWarPoint; // 宣战点
	public long lastDeclareWarPoint; // 宣战点上次恢复
	public int declareWarPointSpeed;// 宣战点回复速度
	public Set<String> playerIds = new HashSet<>();
	public Set<Integer> controlBuildIds = new HashSet<>();
	public Set<YQZZBuildType> controlBuildTypes = new HashSet<>();
	public ImmutableMap<EffType, Integer> battleEffVal = ImmutableMap.of();
	public int buildPlayerHonor;
	public int buildGuildHonor;
	public int buildNationHonor;

	public int playerNationHonor;
	public int pylonCnt;
	public Map<Integer, GuildSign> signMap = new ConcurrentHashMap<>();
	public List<YQZZDeclareWar> declareWarRecords = new ArrayList<>();
	
	public Deque<ChatMsg> guldMsgCache = new ConcurrentLinkedDeque<>();
	
	public YQZZGuildBaseInfo(YQZZBattleRoom parent) {
		this.parent = parent;
	}

	public void onTick() {
		int spaceLevel = parent.getNationInfo(campServerId).getNationLevel();
		declareWarPointSpeed = parent.getCfg().getDeclareWarOrderSpeed() * 1000 - parent.getCfg().declareWarOrderSpeedAdd(spaceLevel) * 60000;
		if (parent.getCurTimeMil() - lastDeclareWarPoint > declareWarPointSpeed) {
			declareWarPoint = Math.min(declareWarPoint + 1, parent.getCfg().getDeclareWarOrderMax());
			lastDeclareWarPoint = parent.getCurTimeMil();
		}
		if (declareWarPoint >= parent.getCfg().getDeclareWarOrderMax()) {
			declareWarPoint = parent.getCfg().getDeclareWarOrderMax();
			lastDeclareWarPoint = parent.getCurTimeMil();
		}
		parent.getGuildFormation(campGuild).checkMarchIdRemove();
	}

	public Map<Integer, GuildSign> getSignMap() {
		return signMap;
	}
	
	public void addGuldMsgCache(ChatMsg msg) {
		try {
			guldMsgCache.addFirst(msg);
			if (guldMsgCache.size() > 50) {
				guldMsgCache.removeLast();
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
}