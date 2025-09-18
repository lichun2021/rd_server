package com.hawk.game.entity;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hawk.db.HawkDBManager;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.uuid.HawkUUIDGenerator;

import com.alibaba.fastjson.JSONObject;
import com.hawk.game.GsConfig;
import com.hawk.game.entity.item.GuildFormationObj;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.LocalRedis;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.module.spacemecha.data.SpaceMechaDataObject;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.GuildManager.VoiceRoomModel;
import com.hawk.game.service.SearchService;
import com.hawk.game.util.GsConst.GuildActiveType;

public class GuildInfoObject {

	private GuildInfoEntity entity = null;
	
	/** 上次排行刷新时战力*/
	private long lastRankPower = 0;
	
	/**
	 * 将字符串类型的联盟uuid转换成数值类型后的联盟id
	 */
	private String guildId;
	
	/** 标记联盟是否死盟 **/
	private GuildActiveType activeType;
	
	/** 联盟权重积分（此积分作用为联盟推荐排序，联盟推荐的时候动态的计算，要保证每次获取的时候只计算一次，不然排序不稳定） **/
	private int guildWeightScore = 0;
	/**
	 * 机甲召唤相关数据 
	 */
	private SpaceMechaDataObject spaceMechaData;
	
	public GuildInfoObject(GuildInfoEntity entity) {
		if (entity == null) {
			this.entity = new GuildInfoEntity();
		} else {
			this.entity = entity;
		}
		
		initSpaceMechaData();
	}

	public String getId() {
		return entity.getId();
	}
	
	public String getNumTypeId() {
		if (!HawkOSOperator.isEmptyString(guildId)) {
			return guildId;
		}
		
		guildId = String.valueOf(HawkUUIDGenerator.strUUID2Long(getId()));
		
		return guildId;
	}

	public String getName() {
		return entity.getName();
	}

	public String getTag() {
		return entity.getTag();
	}

	public int getFlagId() {
		return entity.getFlagId();
	}

	public String getLangId() {
		return entity.getLangId();
	}

	public int getLevel() {
		return entity.getLevel();
	}

	public String getLeaderId() {
		return entity.getLeaderId();
	}

	public String getLeaderName() {
		return entity.getLeaderName();
	}

	public String getColeaderId() {
		return entity.getColeaderId();
	}

	public String getColeaderName() {
		return entity.getColeaderName();
	}

	public boolean isNeedPermition() {
		return entity.isNeedPermition();
	}

	public String getAnnouncement() {
		return entity.getAnnouncement();
	}

	public String getNotice() {
		return entity.getNotice();
	}

	public long getCreateTime() {
		return entity.getCreateTime();
	}

	public String getL1Name() {
		return entity.getL1Name();
	}

	public String getL2Name() {
		return entity.getL2Name();
	}

	public String getL3Name() {
		return entity.getL3Name();
	}

	public String getL4Name() {
		return entity.getL4Name();
	}

	public String getL5Name() {
		return entity.getL5Name();
	}

	public int getNeedBuildingLevel() {
		return entity.getNeedBuildingLevel();
	}

	public int getNeedCommanderLevel() {
		return entity.getNeedCommanderLevel();
	}

	public int getNeedPower() {
		return entity.getNeedPower();
	}

	public String getNeedLang() {
		return entity.getNeedLang();
	}

	public long getLeaderOfflineTime() {
		return entity.getLeaderOfflineTime();
	}

	public long getScore() {
		long result = entity.getScore();
		if (result < -100000) {
			entity.setScore(Integer.MAX_VALUE);
			result = entity.getScore();
		}
		return result;
	}

	public long getLastDonateCheckTime() {
		return entity.getLastDonateCheckTime();
	}
	
	public boolean isHasChangeTag(){
		return entity.isHasChangeTag();
	}

	public boolean isInvalid() {
		return entity.isInvalid();
	}
	
	public long getLastRankPower() {
		return lastRankPower;
	}
	
	public String getGuildBoundId() {
		return entity.getGuildBoundId();
	}
	
	public Map<Integer, List<Integer>> getAuthMap() {
		return entity.getAuthMap();
	}
	
	public long getTaskRefreshTime(){
		return entity.getTaskRefreshTime();
	}

	/**
	 * 获取联盟语音模式
	 * @return
	 */
	public VoiceRoomModel getChatRoomModel() {
		return VoiceRoomModel.valueOf(entity.getChatRoomModel());
	}

	public void setLastRankPower(long lastRankPower) {
		this.lastRankPower = lastRankPower;
	}

	/**
	 * 包括联盟申请
	 *
	 * @param playerId
	 * @return
	 */
	public boolean containApply(String playerId) {
		String info = LocalRedis.getInstance().getGuildPlayerApply(getId(), playerId);
		if (HawkOSOperator.isEmptyString(info)) {
			return false;
		}
		return true;
	}

	public boolean create(String name, String tag, int flagId, Player leader, String announcement) {
		entity.setName(name);
		entity.setTag(tag);
		entity.setFlagId(flagId);
		entity.setLevel(1);
		entity.setLangId("");
		entity.setNeedPermition(false);
		entity.setLeaderId(leader.getId());
		entity.setLeaderName(leader.getName());
		entity.setLeaderPlatform(leader.getPlatform());
		if (!HawkOSOperator.isEmptyString(announcement)) {
			entity.setAnnouncement(announcement);
		}
		
		long now = HawkTime.getMillisecond();
		entity.setNeedBuildingLevel(0);
		entity.setNeedPower(0);
		entity.setNeedBuildingLevel(0);
		entity.setNeedLanguage("all");
		entity.setLeaderOfflineTime(now);
		entity.setLastDonateCheckTime(now);
		entity.setHasChangeTag(false);
		entity.setChatRoomModel(VoiceRoomModel.LIBERTY_VALUE);
		entity.setSpaceMechaInfo("");
		entity.setFormation(GuildFormationObj.load(entity, ""));
		if (!HawkDBManager.getInstance().create(entity)) {
			return false;
		}
		return true;
	}

	public boolean updateGuildName(String oriName, String name) {
		if (!oriName.equals(getName())) {
			return false;
		}
		entity.setName(name);
		// 通知搜索服务
		SearchService.getInstance().removeGuildInfo(oriName);
		SearchService.getInstance().addGuildInfo(getName(), getId());
		
		GlobalData.getInstance().changeGuildName(oriName, name);
		return true;
	}

	public boolean updateGuildTag(String oriTag, String tag) {
		if (!oriTag.equals(getTag())) {
			return false;
		}
		entity.setTag(tag);
		if(!entity.isHasChangeTag()){
			entity.setHasChangeTag(true);
		}
		// 通知搜索服务
		SearchService.getInstance().removeGuildTag(oriTag);
		SearchService.getInstance().addGuildTag(getTag(), getId());
				
		GlobalData.getInstance().changeGuildTag(oriTag, tag);
		return true;
	}

	public void updateGuildFlag(int flag) {
		entity.setFlagId(flag);
	}

	public void updateGuildLanguage(String lang) {
		entity.setLangId(lang);
	}

	public void updateGuildPermiton(boolean isOpen, int buildingLevel, int power, int commonderLevel, String lang) {
		entity.setNeedPermition(!isOpen);
		if (!isOpen) {
			if (buildingLevel != -1) {
				entity.setNeedBuildingLevel(buildingLevel);
			}
			if (power != -1) {
				entity.setNeedPower(power);
			}
			if (commonderLevel != -1) {
				entity.setNeedCommanderLevel(commonderLevel);
			}
			if (lang != null) {
				entity.setNeedLanguage(lang);
			}
		}
	}

	public void updateGuildLevelName(String[] names) {
		if (names[0] != null) {
			entity.setL1Name(names[0]);
		}
		if (names[1] != null) {
			entity.setL2Name(names[1]);
		}
		if (names[2] != null) {
			entity.setL3Name(names[2]);
		}
		if (names[3] != null) {
			entity.setL4Name(names[3]);
		}
		if (names[4] != null) {
			entity.setL5Name(names[4]);
		}
	}

	public void updateGuildAnnouncement(String announcement) {
		entity.setAnnouncement(announcement);
	}

	public void updateGuildNotice(String notice) {
		entity.setNotice(notice);
	}
	
	public void updateGuildBoundId(String guildBoundId) {
		entity.setGuildBoundId(guildBoundId);
	}

	public boolean updateGuildLeader(String oldLeaderId, String leaderId, String leaderName) {
		entity.setLeaderId(leaderId);
		entity.setLeaderName(leaderName);
		entity.setLeaderOfflineTime(HawkTime.getMillisecond());
		try {
			Player player = GlobalData.getInstance().makesurePlayer(leaderId);
			if (player != null) {
				entity.setLeaderPlatform(player.getPlatform());
			} else {
				entity.setLeaderPlatform("");
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return true;
	}

	public void updateGuildLeaderOfflineTime() {
		entity.setLeaderOfflineTime(HawkTime.getMillisecond());
	}

	public void incGuildScore(int addScore) {
		entity.setScore(entity.getScore() + addScore);
	}

	public void disGuildScore(int disScore) {
		long score = entity.getScore() - disScore;
		entity.setScore(score < 0 ? 0 : score);
	}

	public void updateLastDonateCheckTime(long lastDonateCheckTime) {
		entity.setLastDonateCheckTime(lastDonateCheckTime);
	}

	/**
	 * 刷新联盟商店商品数量
	 * @param itemId 商品id
	 * @param changeCount 变化数量(购买为负,补货为正)
	 * @return
	 */
	public int updateGuildShopItem(int itemId, int changeCount) {
		int count = RedisProxy.getInstance().getGuildShopItemCount(this.getId(), itemId);
		int finalCount = count + changeCount;
		finalCount = Math.max(0, finalCount);
		RedisProxy.getInstance().updateGuildShopInfo(entity.getId(), itemId, finalCount);
		return finalCount;
	}
	
	/**
	 * 修改联盟权限
	 * @param authId	权限id
	 * @param lvlList	权限列表
	 */
	public void updateAuthMap(int authId, List<Integer> lvlList) {
		entity.updateAuthMap(authId, lvlList);
	}
	
	/**
	 * 设置上次联盟任务刷新时间
	 * @param taskRefreshTime
	 */
	public void updateTaskRefreshTime(long taskRefreshTime){
		entity.setTaskRefreshTime(taskRefreshTime);
	}
	
	public GuildActiveType getActiveType() {
		return activeType;
	}

	public void setActiveType(GuildActiveType activeType) {
		this.activeType = activeType;
	}

	/**
	 * 解散联盟
	 * @param async
	 */
	public synchronized void delete() {
		entity.delete();
	}

	public GuildInfoEntity getEntity() {
		return entity;
	}
	
	public boolean deadGuild(){
		if(activeType != null && activeType != GuildActiveType.NONE){
			return true;
		}
		return false;
	}
	
	public boolean isLeader(String id){
		return entity.getLeaderId().equals(id.trim());
	}

	public int getGuildWeightScore() {
		return guildWeightScore;
	}

	public void setGuildWeightScore(int guildWeightScore) {
		this.guildWeightScore = guildWeightScore;
	}
	
	/**
	 * 获取区服id
	 * @return
	 */
	public String getServerId(){
		return GsConfig.getInstance().getServerId();
	}
	
	/**
	 * 是否跨服联盟数据
	 * @return
	 */
	public boolean isCrossGuild(){
		return false;
	}
	
	/**
	 * 修改联盟语音模式
	 * @param model
	 */
	public void updateChatRoomModel(VoiceRoomModel model) {
		entity.setChatRoomModel(model.getNumber());
	}
	
	
	public void addXZQTickets(int count){
		int has = entity.getXzqTickets();
		has += count;
		entity.setXzqTickets(has);
	}
	
	public int getXZQTickets(){
		return entity.getXzqTickets();
	}
	
	public void costXZQTickets(int count){
		int hasCount = this.getXZQTickets();
		hasCount -= count;
		hasCount = Math.max(hasCount, 0);
		entity.setXzqTickets(hasCount);
	}
	
	public void clearZQTickets(){
		entity.setXzqTickets(0);
	}
	
	/**
	 * 初始化星甲召唤数据
	 */
	private void initSpaceMechaData() {
		if (!HawkOSOperator.isEmptyString(entity.getSpaceMechaInfo())) {
			try {
				spaceMechaData = JSONObject.parseObject(entity.getSpaceMechaInfo(), SpaceMechaDataObject.class);
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		
		if (spaceMechaData == null) {
			spaceMechaData = new SpaceMechaDataObject();
		}
	}
	
	private void updateSpaceMechaData() {
		try {
			String data = JSONObject.toJSONString(spaceMechaData);
			entity.setSpaceMechaInfo(data);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	public int getSpaceMaxLv() {
		return spaceMechaData.getSpaceMaxLv();
	}

	public void setSpaceMaxLv(int spaceMaxLv) {
		spaceMechaData.setSpaceMaxLv(spaceMaxLv);
		updateSpaceMechaData();
	}
	
	public int getSpaceSelectedLv() {
		return spaceMechaData.getSpaceSelectedLv();
	}

	public void setSpaceSelectedLv(int spaceSelectedLv) {
		spaceMechaData.setSpaceSelectedLv(spaceSelectedLv);
		updateSpaceMechaData();
	}
	
	public int getSpaceSetTimes() {
		return spaceMechaData.getSpaceSetTimes();
	}

	public void setSpaceSetTimes(int spaceSetTimes) {
		spaceMechaData.setSpaceSetTimes(spaceSetTimes);
		updateSpaceMechaData();
	}

	public int getSpaceMechaTermId() {
		return spaceMechaData.getTermId();
	}

	public long getSpaceMechaGuildPoint() {
		return spaceMechaData.getGuildPoint();
	}

	/**
	 * 添加联盟星币数量：不同对象能同时进入，同一对象不能同时进入（能锁住同一个联盟的）
	 * 
	 * @param guildPoint 小于0表示消耗
	 */
	public synchronized void addSpaceMechaGuildPoint(long guildPoint) {
		long newPoint = Math.max(0, spaceMechaData.getGuildPoint() + guildPoint);
		spaceMechaData.setGuildPoint(newPoint);
		updateSpaceMechaData();
	}
	
	/**
	 * 重置星甲召唤数据：不同对象能同时进入，同一对象不能同时进入（能锁住同一个联盟的）
	 * 
	 * @param termId
	 */
	public synchronized void resetSpaceMechaData(int termId) {
		if (termId != this.getSpaceMechaTermId()) {
			spaceMechaData.resetData(termId);
			updateSpaceMechaData();
		}
	} 

	/**
	 * 获取编队信息
	 * @return
	 */
	public GuildFormationObj getFormation() {
		return entity.getFormation();
	}

	public Set<Integer> getRewardFlagSet(){
		return entity.getRewardFlagSet();
	}

	public boolean addRewardFlag(int flagId){
		Set<Integer> flagSet = entity.getRewardFlagSet();
		if(flagSet.contains(flagId)){
			return false;
		}
		flagSet.add(flagId);
		return true;
	}
}
