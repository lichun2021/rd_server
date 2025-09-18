package com.hawk.game.module.dayazhizhan.playerteam.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 赛博之战商店配置
 *
 * @author Jesse
 *
 */
@HawkConfigManager.XmlResource(file = "xml/dyzz_hero.xml")
public class DYZZHeroCfg extends HawkConfigBase {
    @Id
    private final int id;
    // 购买商品消耗
    private final int heroData;
    //
    private final int sign;
    
    
    public DYZZHeroCfg() {
        id = 0;
        heroData = 0;
        sign = 0;
    }

    public int getId() {
        return id;
    }

    
	public int getHeroData() {
		return heroData;
	}

	public int getSign() {
		return sign;
	}

	@Override
    protected boolean assemble() {
        return true;
    }

    @Override
    protected boolean checkValid() {
        return true;
    }
    

}
