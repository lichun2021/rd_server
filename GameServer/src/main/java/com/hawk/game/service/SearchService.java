package com.hawk.game.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.collections4.trie.PatriciaTrie;
import org.hawk.app.HawkAppObj;
import org.hawk.db.HawkDBManager;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkRand;
import org.hawk.xid.HawkXID;

import com.hawk.game.config.GuildConstProperty;
import com.hawk.game.entity.GuildMemberObject;
import com.hawk.game.global.GlobalData;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Player.GetPlayerBasicInfoResp;
import com.hawk.game.protocol.Player.HPPlayerBasicInfo;
import com.hawk.game.queryentity.NameIdInfo;
import com.hawk.game.util.BuilderUtil;

public class SearchService extends HawkAppObj {
	/**
	 * 玩家名的查询
	 */
	static final String PLAYER_NAME_INFO = "player_name";
	
	/**
	 * 联盟名的查询
	 */
	static final String GUILD_NAME_INFO = "guild_name";
	
	/**
	 * 玩家名字查询前缀树
	 */
	private PatriciaTrie<String> playerNameTrie;
	
	/**
	 * 联盟名字查询前缀树
	 */
	private PatriciaTrie<String> guildNameTrie;
	
	/**
	 * 联盟简称查询前缀树
	 */
	private PatriciaTrie<String> guildTagTrie;
	
	/**
	 * 军事学院教官查询前缀树
	 */
	private PatriciaTrie<String> coachNameTrie;
	
	/**
	 * 玩家名字缓存用作搜索
	 */
	private Map<String, List<String>> playerNameLow;

	/**
	 * 全局实例对象
	 */
	private static SearchService instance = null;

	/**
	 * 获取实例对象
	 *
	 * @return
	 */
	public static SearchService getInstance() {
		return instance;
	}

	/**
	 * 构造
	 *
	 */
	public SearchService(HawkXID xid) {
		super(xid);
		instance = this;
		playerNameTrie = new PatriciaTrie<String>();
		guildNameTrie = new PatriciaTrie<String>();
		guildTagTrie = new PatriciaTrie<String>();
		coachNameTrie = new PatriciaTrie<String>();
		playerNameLow = new ConcurrentHashMap<>(5000);
	}

	/**
	 * 初始化
	 * 
	 * @return
	 */
	public boolean init() {
		return true;
	}

	/**
	 * 添加玩家信息
	 * 
	 * @param name
	 * @param info
	 */
	public void addPlayerInfo(String name, String info, boolean isNew) {
		playerNameTrie.put(name, info);
	}

	/**
	 * 删除玩家信息
	 * 
	 * @param name
	 */
	public void removePlayerInfo(String name) {
		playerNameTrie.remove(name);
	}

	/**
	 * 查询本服玩家
	 * 
	 * @param name
	 * @return
	 */
	public String getPlayerInfo(String name) {
		String info = playerNameTrie.get(name);
		return info;
	}

	/**
	 * 模糊匹配查询玩家, 获取到的是玩家id信息列表, 最新信息从玩家快照中获得
	 * 
	 * @param namePrefix
	 * @return
	 */
	public List<String> matchingPlayerInfo(String namePrefix, int count, boolean isLocal) {
		List<String> playerIds = new LinkedList<String>();
		Map<String, String> localResult = playerNameTrie.prefixMap(namePrefix);
		playerIds.addAll(localResult.values());
		return playerIds;
	}

	/**
	 * 添加联盟信息
	 * 
	 * @param name
	 * @param info
	 */
	public void addGuildInfo(String name, String info) {
		synchronized (guildNameTrie) {
			guildNameTrie.put(name, info);
		}
	}

	/**
	 * 删除联盟信息
	 * 
	 * @param name
	 */
	public void removeGuildInfo(String name) {
		synchronized (guildNameTrie) {
			guildNameTrie.remove(name);
		}
	}

	/**
	 * 获取本服联盟
	 * 
	 * @param name
	 * @return
	 */
	public String getGuildInfo(String name) {
		String info = guildNameTrie.get(name);
		return info;
	}

	/**
	 * 模糊匹配联盟名字, 获取到的是联盟id信息列表
	 * 
	 * @param namePrefix
	 * @return
	 */
	public List<String> matchingGuildInfo(String namePrefix) {
		List<String> guildIds = new LinkedList<String>();
		synchronized (guildNameTrie) {
			Map<String, String> localResult = guildNameTrie.prefixMap(namePrefix);
			guildIds.addAll(localResult.values());
		}

		return guildIds;
	}
	

	/**
	 * 添加联盟简称信息
	 * 
	 * @param name
	 * @param info
	 */
	public void addGuildTag(String tag, String info) {
		synchronized (guildTagTrie) {
			guildTagTrie.put(tag, info);
		}
	}

	/**
	 * 删除联盟简称信息
	 * 
	 * @param name
	 */
	public void removeGuildTag(String tag) {
		synchronized (guildTagTrie) {
			guildTagTrie.remove(tag);
		}
	}

	/**
	 * 模糊匹配联盟简称, 获取到的是联盟id信息列表
	 * 
	 * @param namePrefix
	 * @return
	 */
	public List<String> matchingGuildTag(String namePrefix) {
		List<String> guildIds = new LinkedList<String>();
		synchronized (guildTagTrie) {
			Map<String, String> localResult = guildTagTrie.prefixMap(namePrefix);
			guildIds.addAll(localResult.values());
		}

		return guildIds;
	}

	/**
	 * 根据关键字模糊查询本服玩家id信息(谨慎调用, 用之前问一下hawk)
	 * 
	 * @param keyName
	 * @param maxCount
	 * @param avoidPlayerIds
	 * @return
	 */
	public Set<String> searchPlayerByName(String keyName, int maxCount) {
		Set<String> playerIds = new HashSet<String>();

		String querySql = null;
		if (maxCount > 0) {
			querySql = String.format(
					"select id as playerId, name as playerName from player where name like '%%%s%%' limit %d", keyName,
					maxCount);
		} else {
			querySql = String.format("select id as playerId, name as playerName from player where name like '%%%s%%'",
					keyName);
		}
		List<NameIdInfo> nameIdList = HawkDBManager.getInstance().executeQuery(querySql, NameIdInfo.class);
		HawkRand.randomOrder(nameIdList);

		if (nameIdList != null) {
			for (NameIdInfo info : nameIdList) {
				playerIds.add(info.getPlayerId());
			}
		}

		return playerIds;
	}

	public Map<String, List<String>> getPlayerNameLow() {
		return playerNameLow;
	}

	public void removePlayerNameLow(String name, String playerId) {
		String lowName = name.toLowerCase();
		List<String> strList = this.getPlayerNameLow().get(lowName);
		if (strList != null) {
			strList.remove(playerId);
		}
	}

	public void addPlayerNameLow(String name, String playerId) {
		String lowName = name.toLowerCase();			
		List<String> strList = this.getPlayerNameLow().get(lowName);
		if (strList == null) {
			strList = new CopyOnWriteArrayList<>();
			
			//防止并发new 导致的数据紊乱问题
			List<String> tmpList = this.getPlayerNameLow().putIfAbsent(lowName, strList);
			if (tmpList != null) {
				strList = tmpList;
			}
		}

		if (!strList.contains(playerId)) {
			strList.add(playerId);
		}
	}

	public List<String> searchPlayerByNameIgnore(String playerName, int sex, int location, List<String> ignoreIds) {
		return searchPlayerByNameIgnore(playerName, sex, location, 50, ignoreIds, false);
	}

	/**
	 * 
	 * @param playerName
	 * @return
	 */
	public List<String> searchPlayerByNameIgnore(String playerName, int sex, int location, int num,
			List<String> ignoreIds, boolean precise) {
		String lowName = playerName.toLowerCase();
		Map<String, List<String>> map = this.getPlayerNameLow();
		List<String> strList = new ArrayList<>();

		if (num <= 0) {
			return strList;
		}

		L1: for (Entry<String, List<String>> entry : map.entrySet()) {
			if (playerName.length() > entry.getKey().length()) {
				continue;
			}

			if (entry.getKey().contains(lowName)) {
				for (String str : entry.getValue()) {

					if (ignoreIds != null && ignoreIds.contains(str)) {
						continue;
					}

					if (precise && !entry.getKey().equals(lowName)) {
						continue;
					}
					
					if (precise) {
						Player snapshot = GlobalData.getInstance().makesurePlayer(str);
						if (snapshot != null  && !snapshot.getName().equals(playerName)) {
							continue;
						}
					}
					
					strList.add(str);

					if (strList.size() >= num) {
						break L1;
					}

				}

			}
		}

		return strList;
	}
	
	public GetPlayerBasicInfoResp.Builder searchNoGuildPlayerByName(String playerName, String selfId, boolean precise) {
		GetPlayerBasicInfoResp.Builder builder = GetPlayerBasicInfoResp.newBuilder();
		String lowName = playerName.toLowerCase();
		Map<String, List<String>> map = this.getPlayerNameLow();
		int count = 0;
		int maxCount = GuildConstProperty.getInstance().getSearchPlayerNumber();
		F1: for (Entry<String, List<String>> entry : map.entrySet()) {
			if (lowName.length() > entry.getKey().length()) {
				continue;
			}
			if (count >= maxCount) {
				break;
			}
			if (entry.getKey().contains(lowName)) {
				for (String playerId : entry.getValue()) {
					if (count >= maxCount) {
						break F1;
					}
					if (playerId.equals(selfId)) {
						continue;
					}
					if (precise && !entry.getKey().equals(lowName)) {
						continue;
					}
					GuildMemberObject member = GuildService.getInstance().getGuildMemberObject(playerId);
					// 玩家已加入联盟
					if (member != null && !HawkOSOperator.isEmptyString(member.getGuildId())) {
						continue;
					}
					Player snapshot = GlobalData.getInstance().makesurePlayer(playerId);
					if (snapshot == null) {
						continue;
					}
					
					if (precise && !snapshot.getName().equals(playerName)) {
						continue;
					}
					HPPlayerBasicInfo.Builder basicInfo = HPPlayerBasicInfo.newBuilder();
					basicInfo.setPlayerId(snapshot.getId());
					basicInfo.setPlayerName(snapshot.getName());
					basicInfo.setPower(snapshot.getPower());
					basicInfo.setIcon(snapshot.getIcon());
					basicInfo.setLanguage(snapshot.getLanguage());
					basicInfo.setKingdom(snapshot.getServerId());
					basicInfo.setVip(snapshot.getVipLevel());
					basicInfo.setCommon(BuilderUtil.genPlayerCommonBuilder(snapshot));
					if (!HawkOSOperator.isEmptyString(snapshot.getPfIcon())) {
						basicInfo.setPfIcon(snapshot.getPfIcon());
					}
					builder.addInfo(basicInfo);
					count++;
				}
			}
		}
		return builder;
	}
	
	/**
	 * 添加教官名字信息
	 * 
	 * @param coachName
	 * @param coachId
	 */
	public void addCoachName(String coachName, String coachId) {
		synchronized (coachNameTrie) {
			coachNameTrie.put(coachName, coachId);
		}
	}

	/**
	 * 删除教官名字信息
	 * 
	 * @param coachName
	 */
	public void removeCoachName(String coachName) {
		synchronized (coachNameTrie) {
			coachNameTrie.remove(coachName);
		}
	}

	/**
	 * 模糊匹配教官名字, 获取到的是教官id信息列表
	 * 
	 * @param namePrefix
	 * @return
	 */
	public List<String> matchingCoachName(String namePrefix) {
		List<String> coachIds = new LinkedList<String>();
		synchronized (coachNameTrie) {
			Map<String, String> localResult = coachNameTrie.prefixMap(namePrefix);
			coachIds.addAll(localResult.values());
		}

		return coachIds;
	}
}
