package com.hawk.game.module.toucai.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 铁血军团活动时间配置
 * @author Jesse
 *
 */
@HawkConfigManager.XmlResource(file = "xml/medal_factory_product.xml")
public class MedalFactoryProductCfg extends HawkConfigBase {
	@Id
	private final int id;// ="10101"
	private final String uesItem;// ="30000_15900001_1"
	private final int getExp;// ="1"
	private final int productTime;// ="36000"
	private final int library;// ="4940001"

	public MedalFactoryProductCfg() {
		id = 0;
		getExp = 0;
		productTime = 0;
		uesItem = "";
		library = 0;
	}

	public int getId() {
		return id;
	}

	public String getUesItem() {
		return uesItem;
	}

	public int getGetExp() {
		return getExp;
	}

	public int getProductTime() {
		return productTime;
	}

	public int getLibrary() {
		return library;
	}

}
