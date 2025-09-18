package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.helper.HawkAssert;

import com.hawk.game.item.ItemInfo;
import com.hawk.game.module.mechacore.cfg.MechaCoreModuleCfg;
import com.hawk.game.protocol.Const.ItemType;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.WeightAble;

/**
 * 兵种强化
 * 
 * @author lwt
 *
 */
@HawkConfigManager.XmlResource(file = "xml/gacha_pool.xml")
public class GachaPoolCfg extends HawkConfigBase implements WeightAble {

	protected final int id;// ="10100"
	protected final int lvMin;// ="1"
	protected final int lvMax;// ="20"
	protected final String dropType;// ="30000_1100001"
	protected final int numMin;// ="1"
	protected final int numMax;// ="1"
	protected final int dropWeight;// ="10" />

	public GachaPoolCfg() {
		this.id = 0;
		this.lvMin = 0;
		this.lvMax = 0;
		this.dropType = "";
		this.numMax = 0;
		this.numMin = 0;
		this.dropWeight = 0;
	}

	@Override
	protected boolean checkValid() {
		ItemInfo item = ItemInfo.valueOf(dropType+"_" + numMin);
		if (item.getType() == ItemType.ARMOUR_VALUE * GsConst.ITEM_TYPE_BASE) {
			ArmourPoolCfg armourPoolCfg = HawkConfigManager.getInstance().getConfigByKey(ArmourPoolCfg.class, item.getItemId());
			HawkAssert.notNull(armourPoolCfg, "dropType cant find : " + dropType);
		} else if(item.getType() == ItemType.MECHA_CORE_MODULE_VALUE * GsConst.ITEM_TYPE_BASE) {
			MechaCoreModuleCfg moduleCfg = HawkConfigManager.getInstance().getConfigByKey(MechaCoreModuleCfg.class, item.getItemId());
			HawkAssert.notNull(moduleCfg, "mechacore dropType cant find : " + dropType);
		} else {
			ItemCfg itemCfg = HawkConfigManager.getInstance().getConfigByKey(ItemCfg.class, item.getItemId());
			HawkAssert.notNull(itemCfg, "dropType cant find : " + dropType);
		}
		return super.checkValid();
	}

	public int getId() {
		return id;
	}

	public int getLvMin() {
		return lvMin;
	}

	public int getLvMax() {
		return lvMax;
	}

	public String getDropType() {
		return dropType;
	}

	public int getNumMin() {
		return numMin;
	}

	public int getNumMax() {
		return numMax;
	}

	public int getDropWeight() {
		return dropWeight;
	}

	@Override
	public int getWeight() {
		return dropWeight;
	}

}
