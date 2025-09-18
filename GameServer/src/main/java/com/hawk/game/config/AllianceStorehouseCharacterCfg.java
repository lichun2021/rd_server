package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.game.util.WeightAble;

@HawkConfigManager.XmlResource(file = "xml/alliance_storehouse_character.xml")
public class AllianceStorehouseCharacterCfg extends HawkConfigBase {
	@Id
	protected final int id;
	protected final int weight;
	protected final int crystalWeight; // 付费刷新概率
	protected final long openTime;

	private static int Max_Id;

	public AllianceStorehouseCharacterCfg() {
		id = 0;
		weight = 0;
		openTime = 0;
		crystalWeight = 0;
	}

	@Override
	protected boolean assemble() {
		Max_Id = Math.max(Max_Id, id);
		return super.assemble();
	}

	/** 最高品质 */
	public static int maxGroup() {
		return Max_Id;
	}

	public int getId() {
		return id;
	}

	public int getWeight() {
		return weight;
	}

	public long getOpenTime() {
		return openTime;
	}

	public int getCrystalWeight() {
		return crystalWeight;
	}

	public WeightObj toWeightObj(boolean free) {
		WeightObj obj = new WeightObj();
		obj.cfg = this;
		obj.free = free;
		return obj;
	}

	public class WeightObj implements WeightAble {
		private AllianceStorehouseCharacterCfg cfg;
		private boolean free;

		@Override
		public int getWeight() {
			if (free) {
				return cfg.getWeight();
			}
			return cfg.getCrystalWeight();
		}

		public AllianceStorehouseCharacterCfg getCfg() {
			return cfg;
		}

	}
}
