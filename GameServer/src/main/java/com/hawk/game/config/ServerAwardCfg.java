package com.hawk.game.config;

import java.util.ArrayList;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;

import com.hawk.game.item.AwardItems;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.util.GameUtil;

/**
 * 奖励配置
 *
 * @author zhjx
 *
 */
@HawkConfigManager.XmlResource(file = "xml/server_award.xml")
public class ServerAwardCfg extends HawkConfigBase {
	@Id
	protected final int id;
	// 奖励配置
	private final int serverAwardId;
	// 起始开服时间
	protected final String openServerTimeFar;
	// 截止时间
	protected final String openServerTimeNear;
	// 奖励信息
	protected final String reward;
	// 客户端显示额度
	protected final String showPrice;

	// 奖励详细信息
	private List<ItemInfo> awardList;

	// 开服时间限制
	private long openTimeFar = 0;
	private long openTimeNear = 0;

	public ServerAwardCfg() {
		id = 0;
		serverAwardId = 0;
		openServerTimeFar = "";
		openServerTimeNear = "";
		reward = "";
		showPrice = "";
	}

	public int getId() {
		return id;
	}

	/**
	 * 获取确切的奖励信息
	 * 
	 * @return
	 */
	public AwardItems getAwardItems() {
		AwardItems awardItems = AwardItems.valueOf();
		for (ItemInfo itemInfo : awardList) {
			awardItems.addItem(itemInfo.clone());
		}
		return awardItems;
	}

	@Override
	protected boolean assemble() {
		awardList = new ArrayList<ItemInfo>();
		if (!HawkOSOperator.isEmptyString(reward)) {
			String[] awardItemArray = reward.split(",");
			for (String awardItemElem : awardItemArray) {
				ItemInfo itemInfo = new ItemInfo(awardItemElem);
				awardList.add(itemInfo);
			}
		}

		try {
			// 开始时间
			if (HawkOSOperator.isEmptyString(openServerTimeFar)) {
				openTimeFar = HawkTime.parseTime(openServerTimeFar, "yyyy_MM_dd");
			}

			// 截止时间
			if (HawkOSOperator.isEmptyString(openServerTimeNear)) {
				openTimeNear = HawkTime.parseTime(openServerTimeNear, "yyyy_MM_dd");
			}
		} catch (Exception e) {
			HawkException.catchException(e);
			return false;
		}
		return true;
	}

	@Override
	protected boolean checkValid() {
		for (ItemInfo itemInfo : awardList) {
			if (!itemInfo.checkItemInfo()) {
				return false;
			}
		}
		return true;
	}

	public List<ItemInfo> getAwardList() {
		return awardList;
	}

	public void setAwardList(List<ItemInfo> awardList) {
		this.awardList = awardList;
	}

	public String getOpenServerTimeFar() {
		return openServerTimeFar;
	}

	public String getOpenServerTimeNear() {
		return openServerTimeNear;
	}

	public String getReward() {
		return reward;
	}

	public String getShowPrice() {
		return showPrice;
	}

	public int getServerAwardId() {
		return serverAwardId;
	}

	/**
	 * 该条目对本服是否开放
	 * @return
	 */
	public boolean isThisServerOpen() {
		if (GameUtil.getServerOpenTime() >= openTimeFar && GameUtil.getServerOpenTime() <= openTimeNear) {
			return true;
		}
		return false;
	}
}
