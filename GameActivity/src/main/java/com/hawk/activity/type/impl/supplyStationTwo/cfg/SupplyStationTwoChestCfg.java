package com.hawk.activity.type.impl.supplyStationTwo.cfg;

import java.util.ArrayList;
import java.util.List;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.gamelib.activity.ConfigChecker;

/**
 * 复制盟军补给站
 * timelover_shop 配置文件没错
 * @author jm
 *
 */
@HawkConfigManager.XmlResource(file = "activity/time_lover/timelover_shop.xml")
public class SupplyStationTwoChestCfg extends HawkConfigBase {
	
	@Id
	private final int id;
	
	/** 宝箱价格 **/
	private final String price;
	
	private final int awardId;
	
	/** 购买上限 **/
	private final int limit;
	
	public SupplyStationTwoChestCfg(){
		id = 0;
		price = "";
		limit = 0;
		awardId = 0;
	}

	@Override
	protected boolean assemble() {
		try {
			RewardHelper.toRewardItemImmutableList(this.price);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return super.assemble();
	}

	public int getId() {
		return id;
	}

	public String getPrice() {
		return price;
	}

	public int getLimit() {
		return limit;
	}
	
	public int getAwardId() {
		return awardId;
	}

	/***
	 * 构建价格
	 * @param buyCnt
	 * @return
	 */
	public List<RewardItem.Builder> buildPrize(int buyCnt){
		List<RewardItem.Builder> list = new ArrayList<RewardItem.Builder>();
		if(buyCnt <= 0){
			return list;
		}
		for(int i = 0 ; i < buyCnt ; i ++){
			list.addAll(RewardHelper.toRewardItemList(this.price));
		}
		return list;
	}
	
	@Override
	protected boolean checkValid() {
		if(limit <= 0){
			throw new RuntimeException(String.format("复制联盟补给站,又称时空恋人,timelover_shop.xml 每日限制次数异常 :%d",limit));
		}
		if(!ConfigChecker.getDefaultChecker().chectAwardIdValid(awardId)){
			throw new RuntimeException(String.format("复制联盟补给站,又称时空恋人, timelover_shop.xml 配置id:%d,在award.xml中不存在", id));
		}
		return super.checkValid();
	}
}
