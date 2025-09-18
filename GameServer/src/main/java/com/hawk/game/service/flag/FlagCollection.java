package com.hawk.game.service.flag;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import com.hawk.game.entity.WarFlagEntity;
import com.hawk.game.protocol.WarFlag.FlageState;
import com.hawk.game.service.warFlag.IFlag;

/**
 * 战地旗帜集合
 * @author golden
 *
 */
public class FlagCollection {

	/**
	 * 单例对象
	 */
	private static FlagCollection instance = null;

	/**
	 * 
	 */
	private Map<String, IFlag> flags;

	private Map<String, List<String>> ownerFlags;

	private Map<String, List<String>> currFlags;

	/**
	 * 母旗
	 */
	private Map<String, List<String>> centerFlags;
	
	/**
	 * 构造
	 */
	public FlagCollection() {
		flags = new ConcurrentHashMap<String, IFlag>();
		ownerFlags = new ConcurrentHashMap<String, List<String>>();
		currFlags = new ConcurrentHashMap<String, List<String>>();
		centerFlags = new ConcurrentHashMap<String, List<String>>();
	}

	public static FlagCollection getInstance() {
		if (instance == null) {
			instance = new FlagCollection();
		}
		return instance;
	}

	public void init(List<WarFlagEntity> initFlags) {
		for (WarFlagEntity flag : initFlags) {
			addFlag(IFlag.create(flag));
		}
	}

	public void addFlag(IFlag flag) {
		getFlags().put(flag.getFlagId(), flag);
		
		// 是否是母旗
		if (flag.isCenter()) {
			addCenterFlag(flag);
		} else {
			addOwnerFlag(flag);
			addCurrFlag(flag);
		}
	}

	private void addOwnerFlag(IFlag flag) {
		getOwnerFlagIds(flag.getOwnerId()).add(flag.getFlagId());
	}

	private void addCurrFlag(IFlag flag) {
		getCurrFlagIds(flag.getCurrentId()).add(flag.getFlagId());
	}

	private void addCenterFlag(IFlag flag) {
		getCenterFlagIds(flag.getOwnerId()).add(flag.getFlagId());
	}
	
	/**
	 * 获取旗帜列表
	 * @return
	 */
	public Map<String, IFlag> getFlags() {
		return flags;
	}

	/**
	 * 获取旗帜数量
	 * @return
	 */
	public int getFlagsCount() {
		return flags.size();
	}
	
	/**
	 * 获取旗帜
	 * @return
	 */
	public IFlag getFlag(String flagId) {
		return flags.get(flagId);
	}

	/**
	 * 删除旗帜
	 */
	public void removeFlag(IFlag flag) {
		List<String> curr = currFlags.get(flag.getCurrentId());
		if (curr != null) {
			curr.remove(flag.getFlagId());
		}
		
		List<String> owner = ownerFlags.get(flag.getOwnerId());
		if (owner != null) {
			owner.remove(flag.getFlagId());
		}
		
		List<String> center = centerFlags.get(flag.getOwnerId());
		if (center != null) {
			center.remove(flag.getFlagId());
		}
		
		flags.remove(flag.getFlagId());
	}
	
	/**
	 * 获取所属旗帜列表 
	 */
	public List<String> getOwnerFlagIds(String guildId) {
		List<String> flags = ownerFlags.get(guildId);
		if (flags == null) {
			flags = new CopyOnWriteArrayList<>();
			List<String> crrentFlags = ownerFlags.putIfAbsent(guildId, flags);
			if (crrentFlags != null) {
				flags = crrentFlags;
			}
		}
		return flags;
	}

	/**
	 * 获取联盟放置旗帜的数量
	 */
	public int getOwnerFlagCount(String guildId) {
		return getOwnerFlagIds(guildId).size();
	}

	/**
	 * 获取母旗列表
	 */
	public List<String> getCenterFlagIds(String guildId) {
		List<String> flags = centerFlags.get(guildId);
		if (flags == null) {
			flags = new CopyOnWriteArrayList<>();
			List<String> crrentFlags = centerFlags.putIfAbsent(guildId, flags);
			if (crrentFlags != null) {
				flags = crrentFlags;
			}
		}
		return flags;
	}
	
	public void removeCenterFlags(String guildId) {
		centerFlags.remove(guildId);
	}
	
	/**
	 * 获取母旗列表
	 */
	public List<IFlag> getCenterFlag(String guildId) {
		List<IFlag> centerFlag = new ArrayList<>();
		List<String> centerFlagIds = getCenterFlagIds(guildId);
		for (String flagId : centerFlagIds) {
			IFlag flag = getFlag(flagId);
			centerFlag.add(flag);
		}
		return centerFlag;
	}
	
	public int getCompCenterCount(String guildId) {
		int count = 0;
		List<IFlag> centerFlag = getCenterFlag(guildId);
		for (IFlag flag : centerFlag) {
			
			if (flag.getState() == FlageState.FLAG_LOCKED_VALUE
					|| flag.getState() == FlageState.FLAG_UNLOCKED_VALUE
					|| flag.getState() == FlageState.FLAG_PLACED_VALUE) {
				continue;
			}
			count++;
		}
		return count;
	}
	
	/**
	 * 获取母旗数量
	 */
	public int getCenterFlagCount(String guildId) {
		return getCenterFlagIds(guildId).size();
	}
	
	public int getCenterFlagPlaceCount(String guildId) {
		int count = 0;
		for (IFlag flag : getCenterFlag(guildId)) {
			if (flag.getState() == FlageState.FLAG_LOCKED_VALUE || flag.getState() == FlageState.FLAG_UNLOCKED_VALUE) {
				continue;
			}
			count++;
		}
		return count;
	}
	
	/**
	 * 获取当前旗帜列表 
	 */
	public List<String> getCurrFlagIds(String guildId) {
		List<String> flags = currFlags.get(guildId);
		if (flags == null) {
			flags = new CopyOnWriteArrayList<>();
			List<String> crrentFlags = currFlags.putIfAbsent(guildId, flags);
			if (crrentFlags != null) {
				flags = crrentFlags;
			}
		}
		return flags;
	}

	/**
	 * 获取当前旗帜列表 
	 */
	public Map<String, List<String>> getAllCurrFlagIds() {
		return currFlags;
	}

	/**
	 * 更换旗帜当前占领联盟
	 */
	public void changeFlagCurr(String flagId, String oldGuildId, String newGuildId) {
		List<String> oldGuildFlags = getCurrFlagIds(oldGuildId);
		oldGuildFlags.remove(flagId);

		List<String> newGuildFlags = getCurrFlagIds(newGuildId);
		newGuildFlags.add(flagId);
	}

	/**
	 * 获取被抢夺旗帜
	 */
	public List<IFlag> getBeLootFlags(String guildId) {
		List<IFlag> retFlags = new ArrayList<>();

		List<String> ownerFlagIds = getOwnerFlagIds(guildId);
		for (String flagId : ownerFlagIds) {
			IFlag flag = getFlag(flagId);
			if (flag.getCurrentId().equals(guildId)) {
				continue;
			}
			retFlags.add(flag);
		}

		return retFlags;
	}

	/**
	 * 获取自己联盟的旗帜
	 * @param guildId
	 * @param current 是否当前属于自己
	 * @param placed 是否已放置
	 * @param complete 是否已经完成
	 * @return
	 */
	public List<IFlag> getOwnCompFlags(String guildId, boolean current, boolean placed, boolean complete) {
		List<IFlag> retFlags = new ArrayList<>();

		List<String> ownerFlagIds = getOwnerFlagIds(guildId);
		for (String flagId : ownerFlagIds) {
			IFlag flag = getFlag(flagId);
			if (current && !flag.getCurrentId().equals(guildId)) {
				continue;
			}
			if (placed && !flag.isPlaced()) {
				continue;
			}
			
			if (complete && !flag.isBuildComplete()) {
				continue;
			}
			retFlags.add(flag);
		}
		
		return retFlags;
	}
	
	/**
	 * 获取已占领旗帜
	 */
	public List<IFlag> getOccupyFlags(String guildId) {
		List<IFlag> retFlags = new ArrayList<>();

		List<String> currFlagIds = getCurrFlagIds(guildId);
		for (String flagId : currFlagIds) {
			IFlag flag = getFlag(flagId);
			if (flag.getOwnerId().equals(guildId)) {
				continue;
			}
			retFlags.add(flag);
		}
		
		return retFlags;
	}
	
	/**
	 * 获取联盟拥有的旗帜数量(建造完成)
	 */
	public int getGuildCompFlagCount(String guildId) {
		int count = 0;
		List<String> currFlagIds = getCurrFlagIds(guildId);
		for (String flagId : currFlagIds) {
			IFlag flag = getFlag(flagId);
			if (!flag.isBuildComplete()) {
				continue;
			}
			count++;
		}
		return count;
	}
}
