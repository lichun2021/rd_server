package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;


/**
 * 资源狂欢宝箱
 * @author PhilChen
 *
 */
@HawkConfigManager.XmlResource(file = "xml/cross_box.xml")
public class CrossBoxCfg extends HawkConfigBase {
	/** */
	@Id
	private final int id;
	/** */
	private final int award;
	
	
	public CrossBoxCfg() {
		id = 0;
		award = 0;
	}
	
	@Override
	protected boolean assemble() {
		return true;
	}
	
	@Override
	protected final boolean checkValid() {
		return super.checkValid();
	}

	public int getId() {
		return id;
	}

	public int getAward() {
		return award;
	}
	
}
