package com.hawk.game.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkRand;

import com.google.common.collect.HashMultimap;
import com.hawk.game.util.GameUtil;
import com.hawk.game.item.AwardItems;
import com.hawk.game.item.OddsItemInfo;
import com.hawk.game.util.GsConst;

/**
 * 奖励配置
 *
 * @author lating
 *
 */
@HawkConfigManager.XmlResource(file = "xml/award.xml")
public class AwardCfg extends HawkConfigBase {
	@Id
	protected final int id;
	// 类型
	protected final int type;
	// 奖励信息
	protected final String award;
	// 奖励详细信息
	private List<List<OddsItemInfo>> awardList;
	// 所有可能出现的itemid
	private List<Integer> allItemIds;
	// key物品  V 存在于award
	private static HashMultimap<Integer, Integer> itemInAward = HashMultimap.create();

	public AwardCfg() {
		id = 0;
		type = 0;
		award = "";
	}

	public int getId() {
		return id;
	}

	public int getType() {
		return type;
	}

	public String getAward() {
		return award;
	}

	public List<List<OddsItemInfo>> getAwardList() {
		return awardList;
	}

	/**
	 * 获取确切的奖励信息
	 * 
	 * @return
	 */
	public AwardItems getRandomAward() {
		AwardItems awardItems = AwardItems.valueOf();
		if (type == GsConst.AwardDropType.ODDS) {
			for (List<OddsItemInfo> items : awardList) {
				// 随机出的万分比值
				int randomProbability = GameUtil.randomProbability();
				// 奖励掉落概率万分比值叠加
				int probabilityAdd = 0;
				for (OddsItemInfo itemInfo : items) {
					if (randomProbability > probabilityAdd && randomProbability <= probabilityAdd + itemInfo.getProbability()) {
						awardItems.addItem(itemInfo.clone());
						break;
					}
					
					probabilityAdd += itemInfo.getProbability();
				}
			}
			
			return awardItems;
		}

		if (type == GsConst.AwardDropType.WEIGHT) {
			for (List<OddsItemInfo> items : awardList) {
				List<Integer> weights = new ArrayList<Integer>();
				for (OddsItemInfo itemInfo : items) {
					weights.add(itemInfo.getProbability());
				}
				OddsItemInfo item = HawkRand.randomWeightObject(items, weights);
				awardItems.addItem(item.clone());
			}
		}
		
		return awardItems;
	}

	@Override
	protected boolean assemble() {
		awardList = new ArrayList<List<OddsItemInfo>>();
		allItemIds = new ArrayList<>();
		if (!HawkOSOperator.isEmptyString(award)) {
			String[] awardItemArray = award.split(";");
			for (String awardItemElem : awardItemArray) {
				String[] awardItems = awardItemElem.split(",");
				List<OddsItemInfo> itemInfos = new ArrayList<OddsItemInfo>();
				int probabilityTotal = 0;
				for (String awardItem : awardItems) {
					OddsItemInfo itemInfo = OddsItemInfo.valueOf(awardItem);
					if (itemInfo != null) {
						probabilityTotal += itemInfo.getProbability();
						itemInfos.add(itemInfo);
						allItemIds.add(itemInfo.getItemId());
						itemInAward.put(itemInfo.getItemId(), this.id);
					}
				}
				
				if (type == GsConst.AwardDropType.ODDS && probabilityTotal > GsConst.RANDOM_MYRIABIT_BASE) {
					HawkLog.errPrintln("award probability total over limit: {}, awardCfgId: {}", GsConst.RANDOM_MYRIABIT_BASE, id);
					return false;
				}
				
				awardList.add(itemInfos);
			}
		}
		return true;
	}

	/**
	 * 返回所有可能出现itemId物品的awardId 集合
	 * @param itemId
	 * @return
	 */
	public static Set<Integer> itemInAward(int itemId) {
		return itemInAward.get(itemId);
	}

	@Override
	protected boolean checkValid() {
		for (List<OddsItemInfo> itemList : awardList) {
			for (OddsItemInfo itemInfo : itemList) {
				if (!itemInfo.checkItemInfo()) {
					return false;
				}
			}
		}
		return true;
	}

	public List<Integer> getAllItemIds() {
		return allItemIds;
	}
	
	public static boolean isExistAwardId(int id) {
		return Objects.nonNull(HawkConfigManager.getInstance().getConfigByKey(AwardCfg.class, id));
	}
}
