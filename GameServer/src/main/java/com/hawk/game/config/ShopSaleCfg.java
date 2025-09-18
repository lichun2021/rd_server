package com.hawk.game.config;

import java.util.ArrayList;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 商城热卖信息配置
 *
 * @author lating
 *
 */
@HawkConfigManager.XmlResource(file = "xml/shopSale.xml")
public class ShopSaleCfg extends HawkConfigBase {
	@Id
	protected final int id;
	// 商品id
	protected final int shopId;
	// 热卖价
	protected final int salePrice;

	// 热卖商品列表
	static List<Integer> salesIdList = new ArrayList<Integer>();

	public ShopSaleCfg() {
		id = 0;
		shopId = 0;
		salePrice = 0;
	}

	public int getId() {
		return id;
	}

	public int getShopId() {
		return shopId;
	}

	public int getSalePrice() {
		return salePrice;
	}

	@Override
	protected boolean assemble() {
		salesIdList.add(id);
		return true;
	}

	@Override
	protected boolean checkValid() {
		ShopCfg shopCfg = HawkConfigManager.getInstance().getConfigByKey(ShopCfg.class, shopId);
		if (shopCfg == null) {
			return false;
		}

		return true;
	}

	public static int getShopSaleIdByIndex(int index) {
		return salesIdList.get(index % salesIdList.size());
	}
}
