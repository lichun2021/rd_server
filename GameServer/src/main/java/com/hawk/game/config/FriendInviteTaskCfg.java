package com.hawk.game.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

import com.hawk.game.item.ItemInfo;

/**
 * QQ密友邀请任务
 *
 * @author lating
 *
 */
@HawkConfigManager.XmlResource(file = "xml/invite_friend.xml")
public class FriendInviteTaskCfg extends HawkConfigBase {
	@Id
	protected final int id;
	// 任务类型
	protected final int type;
	// 人数
	protected final int count;
	// 城堡等级
	protected final int cityLevel;
    // 任务奖励
	protected final String award;
	
	private List<ItemInfo> awardItemList;
	
	private static Set<Integer> cityLevels = new HashSet<Integer>();
	
	private static Set<Integer> playerCountTypeTasks = new HashSet<>();
	
	private static Set<Integer> cityLevelTypeTasks = new HashSet<>();

	public FriendInviteTaskCfg() {
		id = 0;
		type = 0;
		count = 0;
		cityLevel = 0;
		award = "";
	}

	public int getId() {
		return id;
	}

	public int getType() {
		return type;
	}

	public int getCount() {
		return count;
	}

	public int getCityLevel() {
		return cityLevel;
	}

	public String getAward() {
		return award;
	}
	
	public List<ItemInfo> getAwardItems() {
		return awardItemList.stream().map(ItemInfo::clone).collect(Collectors.toList());
	}
	
	public boolean assemble() {
		if (!HawkOSOperator.isEmptyString(award)) {
			awardItemList = new ArrayList<ItemInfo>();
			awardItemList.addAll(ItemInfo.valueListOf(award));
		} else {
			awardItemList = Collections.emptyList();
		}
		
		if (cityLevel > 0) {
			cityLevels.add(cityLevel);
		}
		
		if (type == 1) {
			playerCountTypeTasks.add(id);
		} else if (type == 2) {
			cityLevelTypeTasks.add(id);
		}
		
		return true;
	}
	
	public static Set<Integer> getCityLevels() {
		return cityLevels;
	}

	public static Set<Integer> getPlayerCountTypeTasks() {
		return playerCountTypeTasks;
	}

	public static Set<Integer> getCityLevelTypeTasks() {
		return cityLevelTypeTasks;
	}
}
