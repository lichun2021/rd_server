package com.hawk.game.config;

import java.util.Collections;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

import com.hawk.game.item.ItemInfo;
import com.hawk.serialize.string.SerializeHelper;

/**
 * 剧情任务章节配置
 * @author golden
 *
 */
@HawkConfigManager.XmlResource(file = "xml/drama.xml")
public class StoryMissionChaptCfg extends HawkConfigBase {
	/**
	 * 配置id
	 */
	@Id
	private final int id;
	/**
	 * 等级
	 */
	private final int level;
	/**
	 * 奖励
	 */
	private final String reward;
	/**
	 * 是否开启
	 */
	private final boolean open;
	/**
	 * 后续章节
	 */
	private final String next;
	
	/**
	 * 奖励列表
	 */
	private List<ItemInfo> rewardItem;
	
	/**
	 * 后续章节任务ID
	 */
	private List<Integer> nextChapterIds;
	
	/**
	 * 开启平行章节任务的章节ID
	 */
	private static int paralledChapterId;

	
	public int getId() {
		return id;
	}

	public int getLevel() {
		return level;
	}

	public String getReward() {
		return reward;
	}

	public List<ItemInfo> getRewardItem() {
		return rewardItem;
	}
	
	public void setRewardItem(List<ItemInfo> rewardItem) {
		this.rewardItem = rewardItem;
	}

	public boolean isOpen() {
		return open;
	}
	
	public StoryMissionChaptCfg() {
		this.id = 0;
		this.level = 0;
		this.reward = "";
		this.open = true;
		this.next = "";
	}

	@Override
	protected boolean assemble() {
		if (HawkOSOperator.isEmptyString(reward)) {
			return false;
		}
		rewardItem = ItemInfo.valueListOf(reward);
		
		if (!HawkOSOperator.isEmptyString(next)) {
			paralledChapterId = paralledChapterId == 0 ? id : paralledChapterId;
			nextChapterIds = SerializeHelper.stringToList(Integer.class, next, ",");
			Collections.sort(nextChapterIds);
		} else {
			nextChapterIds = Collections.emptyList();
		}
		return true;
	}
	
	public List<Integer> getNextChapterIds() {
		return nextChapterIds;
	}

	public static int getParalledChapterId() {
		return paralledChapterId;
	}
}
