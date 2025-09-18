package com.hawk.activity.type.impl.doubleRecharge.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;


/**
 * 充值双倍活动配置
 * @author PhilChen
 *
 */
@HawkConfigManager.XmlResource(file = "activity/double_recharge/double_recharge_goods.xml")
public class DoubleRechagreGoodsCfg extends HawkConfigBase {
	/** 商品id*/
	@Id
	private final String goodsId;
	/** 赠送钻石数量*/
	private final int addNum;

	public DoubleRechagreGoodsCfg() {
		goodsId = "";
		addNum = 0;
	}
	
	public String getGoodsId() {
		return goodsId;
	}

	public int getAddNum() {
		return addNum;
	}

	@Override
	protected boolean checkValid() {
		return true;
	}
	
}
