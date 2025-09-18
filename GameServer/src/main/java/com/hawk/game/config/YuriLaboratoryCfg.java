package com.hawk.game.config;

import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;

import com.hawk.game.item.ItemInfo;
import com.hawk.game.protocol.Const.PlayerAttr;

/**
 *
 * @author zhenyu.shang
 * @since 2017年10月12日
 */
@HawkConfigManager.XmlResource(file = "xml/yuriLaboratory.xml")
public class YuriLaboratoryCfg  extends HawkConfigBase {
	
	/** Id */
	@Id
	protected final int id; 
	
	protected final int playerExp; 
	
	protected final int award; 
	
	protected final int gold;
	
	protected final int oil;
	
	protected final int uranium;
	
	protected final int steel;

	public YuriLaboratoryCfg() {
		id = 0; 
		playerExp = 0; 
		award = 0; 
		gold = 0;
		oil = 0;
		uranium = 0;
		steel = 0;
	}

	public int getId() {
		return id;
	}

	public int getPlayerExp() {
		return playerExp;
	}

	public int getAward() {
		return award;
	}

	public int getGold() {
		return gold;
	}

	public int getOil() {
		return oil;
	}

	public int getUranium() {
		return uranium;
	}

	public int getSteel() {
		return steel;
	}
	
	public int getResByType(int resType){
		int num = 0;
		switch (resType) {
		case PlayerAttr.GOLDORE_UNSAFE_VALUE:
			num = gold;
			break;
		case PlayerAttr.OIL_UNSAFE_VALUE:
			num = gold;		
			break;
		case PlayerAttr.STEEL_UNSAFE_VALUE:
			num = uranium;
			break;
		case PlayerAttr.TOMBARTHITE_UNSAFE_VALUE:
			num = steel;
			break;
		default:
			break;
		}
		return num;
	}
	
	@Override
	protected boolean checkValid() {
		
		AwardCfg awardCfg = HawkConfigManager.getInstance().getConfigByKey(AwardCfg.class, award);
		if (awardCfg == null) {
			HawkLog.errPrintln("YuriExplore award failed, awardCfg is null, awardId: {}", award);
			return false;
		}
		
		List<ItemInfo> awardItems = awardCfg.getRandomAward().getAwardItems();
		if(awardItems == null || awardItems.isEmpty()){
			HawkLog.errPrintln("YuriExplore award failed, awardItems is null, awardId: {}", award);
			return false;
		}
		
		return true;
	}
}
