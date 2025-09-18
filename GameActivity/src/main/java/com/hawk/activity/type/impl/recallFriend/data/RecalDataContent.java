package com.hawk.activity.type.impl.recallFriend.data;

import com.google.common.base.Objects;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.redis.ActivityGlobalRedis;
import com.hawk.activity.redis.ActivityRedisKey;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.recallFriend.cfg.RecallFriendCfg;
import com.hawk.activity.type.impl.recallFriend.cfg.RecallFriendGuildTaskCfg;
import com.hawk.serialize.string.SerializeHelper;
import org.apache.commons.lang.StringUtils;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class RecalDataContent {
	static Logger logger = LoggerFactory.getLogger("Server");
	/**
	 * 联盟成就缓存数据
	 */
	private Map<String, List<AchieveItem>> allGuildAchieveItems = new ConcurrentHashMap<>();


	private Map<String, RecalPlayer> allReclPlayerMap = new ConcurrentHashMap<>();
	/**
	 * 联盟可召回盟友列表, 无需写进redis,,init会初始化一份,tick定时刷新
	 */
	private Map<String, List<String>> guildCanRecallPlayerMap = new ConcurrentHashMap<>();


	private final int termId;

	public RecalDataContent(int termId) {
		this.termId = termId;
	}

	public RecalPlayer getRecalPlayer(String playerId) {
		// TODO Auto-generated method stub
		return allReclPlayerMap.get(playerId);
	}

	public RecalPlayer newReclPlayer(String playerId) {
		RecalPlayer player = new RecalPlayer();
		player.setPlayerId(playerId);
		allReclPlayerMap.put(player.getPlayerId(), player);
		return player;
	}

	/**
	 * 今日被召回次数
	 * @param playerId
	 * @return
	 */
	public int getTodayCalCnt(String playerId) {
		RecalPlayer player = getRecalPlayer(playerId);
		return player.getTodayCalCnt();
	}

	public List<String> findValidGuildCanRecallPlayer() {
		List<RecalPlayer> all = new ArrayList<>(allReclPlayerMap.values());

		return all.stream()
				.filter(p -> StringUtils.isEmpty(p.getInitGuildId()))
				.filter(p -> StringUtils.isEmpty(p.getJoinGuildId()))
				.map(p -> p.getPlayerId())
				.collect(Collectors.toList());
	}

	public List<String> getGuildHasRecallPlayer(String guildId) {
		List<RecalPlayer> all = new ArrayList<>(allReclPlayerMap.values());
		int facLvLimit = RecallFriendCfg.getInstance().getBaseLimit();
		return all.stream()
				.filter(p -> p.isValidDoEvent(guildId))
				.map(p -> p.getPlayerId())
				.collect(Collectors.toList());
	}

	/**
	 * 初始化单个联盟的成就-新联盟创建调用
	 * @param guildId
	 */
	public synchronized void initGuildAchieveItems(String guildId) {
		ConfigIterator<RecallFriendGuildTaskCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(RecallFriendGuildTaskCfg.class);
		List<AchieveItem> achieveItemList = new ArrayList<>();
		while (configIterator.hasNext()) {
			RecallFriendGuildTaskCfg next = configIterator.next();
			AchieveItem item = AchieveItem.valueOf(next.getAchieveId());
			achieveItemList.add(item);
		}
		allGuildAchieveItems.put(guildId, achieveItemList);
	}

	/**
	 * 获取联盟成就
	 * @param guildId
	 * @return
	 */
	public List<AchieveItem> getGuildAchieveItem(String guildId) {
		if (!allGuildAchieveItems.containsKey(guildId)) {
			this.initGuildAchieveItems(guildId);
		}
		List<AchieveItem> achieveItemList = allGuildAchieveItems.getOrDefault(guildId, new ArrayList<>());
		return achieveItemList;
	}

	/**
	 * 联盟成就缓存写redis
	 */
	public void flushRedisGuildAchieveItems() {
		for (Map.Entry<String, List<AchieveItem>> entry : allGuildAchieveItems.entrySet()) {
			String guildId = entry.getKey();
			List<AchieveItem> guildAchieveItems = entry.getValue();
			String achieveItems = SerializeHelper.collectionToString(guildAchieveItems, SerializeHelper.ELEMENT_DELIMITER);
			ActivityGlobalRedis.getInstance().hset(getRecallGuildAchieveRedisKey(), guildId, achieveItems);
		}
	}
	/**
	 * 回流玩家信息缓存写redis
	 */
	public void flushRedisRecallBackFlowPlayer(){
		//活动中所有回流玩家
		for (RecalPlayer recalPlayer : allReclPlayerMap.values()) {
			String infoStr = recalPlayer.serializ();
			String playerId = recalPlayer.getPlayerId();
			ActivityGlobalRedis.getInstance().hset(getAllRecallBackFlowPlayerRedisKey(), playerId, infoStr);
		}
	}

	/**
	 * 联盟成就redis读进缓存
	 */
	public void readRedisGuildAchieveItems() {
		Map<String, List<AchieveItem>> newAllGuildAchieveItems = new ConcurrentHashMap<>();

		Map<String, String> guildAchieveItemMap = ActivityGlobalRedis.getInstance().hgetAll(getRecallGuildAchieveRedisKey());
		if (guildAchieveItemMap.isEmpty()) {
			return;
		}
		for (Map.Entry<String, String> entry : guildAchieveItemMap.entrySet()) {
			String guildId = entry.getKey();
			String achieveItemsStr = entry.getValue();
			List<AchieveItem> guildAchieveItems = SerializeHelper.stringToList(AchieveItem.class, achieveItemsStr);
			newAllGuildAchieveItems.put(guildId, guildAchieveItems);
		}
		allGuildAchieveItems = newAllGuildAchieveItems;

	}

	/**
	 * 回流玩家redis读进缓存
	 */
	public void readRedisRecallBackFlowPlayer(){
		//活动中所有回流玩家
		Map<String, String> allBackFlowPlayerMap = ActivityGlobalRedis.getInstance().hgetAll(getAllRecallBackFlowPlayerRedisKey());
		for (String infoStr : allBackFlowPlayerMap.values()) {
			RecalPlayer recalPlayer = new RecalPlayer();
			recalPlayer.mergeFrom(infoStr);
			allReclPlayerMap.put(recalPlayer.getPlayerId(), recalPlayer);
		}
	}
	/**
	 * 联盟成就数据redis key
	 * @return
	 */
	private String getRecallGuildAchieveRedisKey() {
		String key = ActivityRedisKey.RECALL_GUILD_ACHIEVE + ":" + termId;
		return key;
	}

	/**
	 * 回流玩家数据redis key
	 * @return
	 */
	private String getAllRecallBackFlowPlayerRedisKey() {
		String key = ActivityRedisKey.RECALL_ALL_BACK_FLOW + ":" + termId;
		return key;
	}

	/**
	 * 写入redis
	 */
	public void flushToRedis() {
		//联盟成就写入redis
		flushRedisGuildAchieveItems();
		//回流玩家信息缓存写redis
		flushRedisRecallBackFlowPlayer();
	}

	/**
	 * 加载数据
	 */
	public void loadData() {
		//从redis中加载联盟成就数据
		readRedisGuildAchieveItems();
		//回流玩家redis读进缓存
		readRedisRecallBackFlowPlayer();
	}

	/**
	 * 联盟可召回玩家
	 * @param guildId
	 * @return
	 */
	public List<String> getGuildCanRecallPlayer(String guildId) {
		return guildCanRecallPlayerMap.getOrDefault(guildId, new ArrayList<>());
	}
	/**
	 * 刷新召回池
	 */
	public void refreshGuildCanRecallPlayer(){
		List<String> recallPlayerIds = findValidGuildCanRecallPlayer();
		if (recallPlayerIds.isEmpty()){
			guildCanRecallPlayerMap = new ConcurrentHashMap<>();
			logger.info("RecalDataContent refreshGuildCanRecallPlayer recallPlayerIds is empty");
			return;
		}

		//随机池
		List<String> randomBackFlowPlayerIds = new ArrayList<>();
		for (String playerId : recallPlayerIds) {
			String playerGuildId = ActivityManager.getInstance().getDataGeter().getGuildId(playerId);
			if (!StringUtils.isEmpty(playerGuildId)){
				continue;
			}
			int facLv = ActivityManager.getInstance().getDataGeter().getConstructionFactoryLevel(playerId);
			if (facLv < RecallFriendCfg.getInstance().getBaseLimit()){
				continue;
			}
			//添加到随机召回池
			randomBackFlowPlayerIds.add(playerId);
		}
		List<String> allGuilds = ActivityManager.getInstance().getDataGeter().getGuildIds();
		//防止运气不好总刷不到一个玩家. 每个联盟按顺序取
		Collections.shuffle(randomBackFlowPlayerIds);
		int limit = RecallFriendCfg.getInstance().getShowNumber();
		//随机池大小
		int totalSize = randomBackFlowPlayerIds.size();
		int start = 0;
		int end = start + limit;
		for (int i = 0; i < allGuilds.size(); i++) {
			String guildId = allGuilds.get(i);
			//数量不满足重乱续,重新乱序下
			List<String> guildList = new ArrayList<>();
			if (start > totalSize){
				start = 0;
				end = start + limit;
			}
			if (end > totalSize){
				end = totalSize;
			}
			guildList.addAll(randomBackFlowPlayerIds.subList(start, end));
			int guildSize = guildList.size();
			if (guildSize < limit && guildSize < totalSize){
				int dif = limit - guildSize;
				guildList.addAll(randomBackFlowPlayerIds.subList(0, dif));
			}
			guildCanRecallPlayerMap.put(guildId, guildList);
			start +=limit;
			end += limit;
		}
	}
}
