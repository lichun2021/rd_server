package com.hawk.game.service.tiberium;

import java.util.List;
import java.util.Map;

import com.hawk.game.entity.GuildInfoEntity;
import com.hawk.game.entity.GuildInfoObject;
import com.hawk.game.player.Player;
import com.hawk.game.util.GsConst.GuildActiveType;

public class TWGuildInfoObject extends GuildInfoObject {

	private TWGuildData guildInfo;

	public TWGuildInfoObject(GuildInfoEntity entity) {
		super(entity);
	}

	public TWGuildData getGuildInfo() {
		return guildInfo;
	}

	public void setGuildInfo(TWGuildData guildInfo) {
		this.guildInfo = guildInfo;
	}

	@Override
	public String getServerId() {
		return guildInfo.getServerId();
	}

	@Override
	public String getId() {
		return guildInfo.getId();
	}

	@Override
	public String getNumTypeId() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getName() {
		return guildInfo.getName();
	}

	@Override
	public String getTag() {
		return guildInfo.getTag();
	}

	@Override
	public int getFlagId() {
		return guildInfo.getFlag();
	}

	@Override
	public String getLangId() {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getLevel() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getLeaderId() {
		return null;
	}

	@Override
	public String getLeaderName() {
		return null;
	}

	@Override
	public String getColeaderId() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getColeaderName() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isNeedPermition() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getAnnouncement() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getNotice() {
		throw new UnsupportedOperationException();
	}

	@Override
	public long getCreateTime() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getL1Name() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getL2Name() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getL3Name() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getL4Name() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getL5Name() {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getNeedBuildingLevel() {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getNeedCommanderLevel() {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getNeedPower() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getNeedLang() {
		throw new UnsupportedOperationException();
	}

	@Override
	public long getLeaderOfflineTime() {
		throw new UnsupportedOperationException();
	}

	@Override
	public long getScore() {
		throw new UnsupportedOperationException();
	}

	@Override
	public long getLastDonateCheckTime() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isHasChangeTag() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isInvalid() {
		throw new UnsupportedOperationException();
	}

	@Override
	public long getLastRankPower() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getGuildBoundId() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Map<Integer, List<Integer>> getAuthMap() {
		throw new UnsupportedOperationException();
	}

	@Override
	public long getTaskRefreshTime() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setLastRankPower(long lastRankPower) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean containApply(String playerId) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean create(String name, String tag, int flagId, Player leader, String announcement) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean updateGuildName(String oriName, String name) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean updateGuildTag(String oriTag, String tag) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateGuildFlag(int flag) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateGuildLanguage(String lang) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateGuildPermiton(boolean isOpen, int buildingLevel, int power, int commonderLevel, String lang) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateGuildLevelName(String[] names) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateGuildAnnouncement(String announcement) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateGuildNotice(String notice) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateGuildBoundId(String guildBoundId) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean updateGuildLeader(String oldLeaderId, String leaderId, String leaderName) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateGuildLeaderOfflineTime() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void incGuildScore(int addScore) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void disGuildScore(int disScore) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateLastDonateCheckTime(long lastDonateCheckTime) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int updateGuildShopItem(int itemId, int changeCount) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateAuthMap(int authId, List<Integer> lvlList) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateTaskRefreshTime(long taskRefreshTime) {
		throw new UnsupportedOperationException();
	}

	@Override
	public GuildActiveType getActiveType() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setActiveType(GuildActiveType activeType) {
		throw new UnsupportedOperationException();
	}

	@Override
	public synchronized void delete() {
		throw new UnsupportedOperationException();
	}

	@Override
	public GuildInfoEntity getEntity() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean deadGuild() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isLeader(String id) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getGuildWeightScore() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setGuildWeightScore(int guildWeightScore) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isCrossGuild() {
		return true;
	}
	
	

}
