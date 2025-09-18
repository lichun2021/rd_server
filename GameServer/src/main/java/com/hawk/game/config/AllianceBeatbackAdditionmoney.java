package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;

import com.hawk.game.data.PlayerAddBountyInfo;
import com.hawk.game.item.ItemInfo;

/**
 *  联盟悬赏--赏金限制 配置表
 *  @Desc
 *	@author RickMei
 *  @Date 2018年11月20日 下午9:15:52
 */
@HawkConfigManager.XmlResource(file = "xml/alliance_beatback_additionmoney.xml")
public class AllianceBeatbackAdditionmoney extends HawkConfigBase {
	public AllianceBeatbackAdditionmoney() {
		this.id = 0;
		additionalMoney= "";
		beAddToMoney = "";
		rechargeMoney = 0;
	}

	@Id
	protected final int id;

	/**
	 * 悬赏上限控制
	 * 10000_1001_1000 
	 */
	protected final String additionalMoney;
	
	/**
	 * 被悬赏上限控制
	 * 10000_1001_1000
	 */
	protected final String beAddToMoney;
	
	/**
	 * 充值档位
	 * 1000
	 */
	protected final int rechargeMoney;
	
	/**
	 * 获取该充值下能追加悬赏的最大值
	 * @Desc
	 * @param chargeCount
	 * @return
	 */
	static public PlayerAddBountyInfo getAdditionMoneyByChargeAmount(int chargeCount){
		ConfigIterator<AllianceBeatbackAdditionmoney> iter = HawkConfigManager.getInstance().getConfigIterator(AllianceBeatbackAdditionmoney.class);
		AllianceBeatbackAdditionmoney cfg = iter.next();
		AllianceBeatbackAdditionmoney matchCfg = cfg;
		while(cfg != null){
			if(chargeCount < cfg.rechargeMoney){
				break;
			}
			matchCfg = cfg;
			cfg = iter.next();
		}
		
		if(null != matchCfg){
			 ItemInfo infoAdd = ItemInfo.valueOf(matchCfg.additionalMoney);
			 ItemInfo infoBeAdd = ItemInfo.valueOf(matchCfg.beAddToMoney);
			 if(null != infoAdd && null != infoBeAdd){
				 return PlayerAddBountyInfo.valueOf((int)infoAdd.getCount() , (int)infoBeAdd.getCount());
			 }
		}
		return new PlayerAddBountyInfo();
	}
	
	/**
	 * 获取该重置下能被追加悬赏的最大值
	 * @Desc 
	 * @param chargeCount
	 * @return
	 */
	static public int getbeAddToMoneyChargeAmount(int chargeCount){
		return 0;
	}
}
