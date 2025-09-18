package com.hawk.game.module.dayazhizhan.playerteam.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 赛博之战商店配置
 *
 * @author Jesse
 *
 */
@HawkConfigManager.XmlResource(file = "xml/dyzz_soldier.xml")
public class DYZZSoldierCfg extends HawkConfigBase {
    @Id
    private final int id;
    
    private final int soldierId;
    //
    private final int count;
    
    
    public DYZZSoldierCfg() {
    	id = 0;
    	soldierId = 0;
    	count = 0;
    }

   

	public int getId() {
		return id;
	}



	public int getSoldierId() {
		return soldierId;
	}



	public int getCount() {
		return count;
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
