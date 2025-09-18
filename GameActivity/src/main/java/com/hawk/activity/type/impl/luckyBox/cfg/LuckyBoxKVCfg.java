package com.hawk.activity.type.impl.luckyBox.cfg;

import java.util.ArrayList;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

import com.google.common.collect.ImmutableList;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;

/**
 * 幸运转盘活动基础表格
 * @author hf
 */
@HawkConfigManager.KVResource(file = "activity/lucky_box/lucky_box_cfg.xml")
public class  LuckyBoxKVCfg extends HawkConfigBase {
	/**
	 * 服务器开服延时开启活动时间；单位:秒
	 */
	private final int serverDelay;
	/**
	 * 使用道具
	 */
	private final String itemOnce;
	/**
	 * 玩家单次可转动次数
	 */
	private final int rotationtimes;
	/**
	 * 道具可购买次数
	 */
	private final int buytimes;
	/**
	 * 道具购买花费,购买后获得gainItem指定的道具
	 */
	private final String buyprice;
	/**
	 * 限定保底次数，第must次时必中大奖
	 */
	private final String must;
	/**
	 *道具购买花费buyprice指定的货币，获得的物品
	 */
	private final String gainItem;
	/**
	 * 活动结束时回收道具ID，逻辑上应该就是gainItem，为了给策划提供灵活度，单独配（允许和gainItem不同）
	 */
	private final int recoverItem;
	/**
	 * 活动结束时回收gainItem以后兑换的物品
	 */
	private final String recoverSendItem;
	/**
	 * 解析字符串must后得到的数值列表
	 */
	private ImmutableList<Integer> mustList;

	public LuckyBoxKVCfg(){
		serverDelay =0;
		itemOnce = "";
		buytimes = 0;
		buyprice = "";
		gainItem = "";
		must = "";
		rotationtimes = 0;
		recoverItem = 0;
		recoverSendItem= "";
	}

	@Override
	protected boolean assemble() {
		List<Integer> musts = new ArrayList<>();
		if(!HawkOSOperator.isEmptyString(this.must)){
			String arr[] = this.must.split("_");
			for(String str : arr){
				int num = Integer.parseInt(str);
				musts.add(num);
			}
		}
		this.mustList = ImmutableList.copyOf(musts);
		return true;
	}

	@Override
	protected boolean checkValid() {
		return super.checkValid();
	}

	public long getServerDelay() {
		return serverDelay * 1000L;
	}

	public int getBuytimes() {
		return buytimes;
	}

	public ImmutableList<Integer> getMustList() {
		return mustList;
	}

	public int getRotationtimes() {
		return rotationtimes;
	}

	public int getRecoverItem() {
		return recoverItem;
	}

	public List<RewardItem.Builder> getRecoverSendItemList() {
		return RewardHelper.toRewardItemImmutableList(this.recoverSendItem);
	}

	public List<RewardItem.Builder> getOnceCostItemList() {
		return RewardHelper.toRewardItemImmutableList(this.itemOnce);
	}

	public List<RewardItem.Builder> getBuyItemPriceList() {
		return RewardHelper.toRewardItemImmutableList(this.buyprice);
	}
	
	public List<RewardItem.Builder> getGainItemList() {
		return RewardHelper.toRewardItemImmutableList(this.gainItem);
	}
}