package com.hawk.game.config;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.hawk.game.protocol.SuperSoldier;
import org.apache.commons.lang.math.NumberUtils;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.google.common.base.Splitter;

@HawkConfigManager.XmlResource(file = "xml/supersoldier_skin.xml")
public class SuperSoldierSkinCfg extends HawkConfigBase {
	@Id
	private final int skinId;// ="40001" 对应buff表 effectId.
	private final int supersoldierId;// ="1006"
	private final String attrSkin;// ="101_100|102_100|103_100"
	private final double powerSkin;// ="2000"
	private final String cost;// ="10000_1004_1000"

	private ImmutableList<SuperSoldier.PBSuperSoldierEffect> effectList;

	public SuperSoldierSkinCfg() {
		this.skinId = 0;
		this.supersoldierId = 0;
		this.attrSkin = "";
		this.powerSkin = 0;
		this.cost = "";
	}

	@Override
	protected boolean assemble() {
		List<String> attrs = Splitter.on("|").omitEmptyStrings().splitToList(attrSkin);
		List<SuperSoldier.PBSuperSoldierEffect> templist = new ArrayList<>();
		for (String str : attrs) {
			String[] arr = str.split("_");
			SuperSoldier.PBSuperSoldierEffect ef = SuperSoldier.PBSuperSoldierEffect.newBuilder().setEffectId(NumberUtils.toInt(arr[0])).setValue(NumberUtils.toInt(arr[1])).build();
			templist.add(ef);
		}
		effectList = ImmutableList.copyOf(templist);
		return super.assemble();
	}

	public int getSkinId() {
		return skinId;
	}

	public int getSupersoldierId() {
		return supersoldierId;
	}

	public double getPowerSkin() {
		return powerSkin;
	}

	public String cost() {
		return cost;
	}

	public ImmutableList<SuperSoldier.PBSuperSoldierEffect> getEffectList() {
		return effectList;
	}
}
