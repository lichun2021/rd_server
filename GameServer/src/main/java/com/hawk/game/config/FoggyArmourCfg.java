package com.hawk.game.config;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;

/**
 *
 * @author zhenyu.shang
 * @since 2018年2月22日
 */
@HawkConfigManager.XmlResource(file = "xml/foggy_armour.xml")
public class FoggyArmourCfg extends HawkConfigBase {

	@Id
	protected final int id;

	protected final int armourPoolId;

	protected final int level;
	protected final String extrAttrs; // armour_additional.xml 配置id

	private ImmutableList<Integer> extrAttrsList;

	@Override
	protected boolean checkValid() {
		ArmourPoolCfg poolCfg = HawkConfigManager.getInstance().getConfigByKey(ArmourPoolCfg.class, armourPoolId);
		if (Objects.isNull(poolCfg)) {
			throw new RuntimeException("FoggyArmourCfg id = " + id + " poolCfg null " + armourPoolId);
		}
		for (int key : getExtrAttrsList()) {
			ArmourAdditionalCfg randAttrCfg = HawkConfigManager.getInstance().getConfigByKey(ArmourAdditionalCfg.class, key);
			if (Objects.isNull(randAttrCfg)) {
				throw new RuntimeException("FoggyArmourCfg id = " + id + " randAttrCfg null " + key);
			}
		}
		return super.checkValid();
	}

	public FoggyArmourCfg() {
		this.id = 0;
		this.armourPoolId = 0;
		this.level = 0;
		this.extrAttrs = "";
	}

	@Override
	protected boolean assemble() {
		List<Integer> list = Splitter.on(",").omitEmptyStrings().trimResults().splitToList(extrAttrs).stream()
				.map(Integer::valueOf).collect(Collectors.toList());
		this.extrAttrsList = ImmutableList.copyOf(list);
		return super.assemble();
	}

	public ImmutableList<Integer> getExtrAttrsList() {
		return extrAttrsList;
	}

	public int getId() {
		return id;
	}

	public int getArmourPoolId() {
		return armourPoolId;
	}

	public int getLevel() {
		return level;
	}

	public String getExtrAttrs() {
		return extrAttrs;
	}

}
