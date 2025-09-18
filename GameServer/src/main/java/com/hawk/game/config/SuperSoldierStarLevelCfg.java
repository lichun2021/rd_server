package com.hawk.game.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.math.NumberUtils;
import org.hawk.collection.ConcurrentHashTable;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Table;
import com.hawk.serialize.string.SerializeHelper;

@HawkConfigManager.XmlResource(file = "xml/supersoldier_star_level.xml")
@HawkConfigBase.CombineId(fields = { "supersoldierId", "starLevel", "stepLevel" })
public class SuperSoldierStarLevelCfg extends HawkConfigBase {
	protected final int id;// ="100110"
	protected final int supersoldierId;// ="11"
	protected final int starLevel;// ="1"
	protected final int stepLevel;// ="0"
	protected final int maxLevel;// ="40"
	protected final String piecesForNextLevel;// ="30000_1000011_10"
	protected final double starPower;// ="50"
	protected final String starAttrValue;// ="101_0|102_0|103_0|104_0"
	protected final int maxSkillLevel;// ="10"
	protected final String starEffectAdd;// ="skillId_efId_val|";
	protected final String atkAttr;
	protected final String hpAttr;

	private Table<Integer,Integer,Integer> skillEffAdd = ConcurrentHashTable.create();
	public SuperSoldierStarLevelCfg() {
		this.id = 0;
		this.supersoldierId = 1;
		this.starLevel = 1;
		this.stepLevel = 1;
		this.maxLevel = 1;
		this.piecesForNextLevel = "";
		this.starPower = 1;
		this.starAttrValue = "";
		this.maxSkillLevel = 0;
		starEffectAdd = "";
		atkAttr = "";
		hpAttr = "";
	}

	@Override
	protected boolean assemble() {
		List<String> list = Splitter.on("|").omitEmptyStrings().splitToList(starEffectAdd);
		for(String s_e_v: list){
			String[] arr = s_e_v.split("_");
			if(arr.length!=3){
				continue;
			}
			skillEffAdd.put(NumberUtils.toInt(arr[0]), NumberUtils.toInt(arr[1]), NumberUtils.toInt(arr[2]));
		}
		return super.assemble();
	}
	
	public Map<Integer, Integer> starEffectAddMap(int skillId) {
		Map<Integer, Integer> result = new HashMap<>();
		result.putAll(skillEffAdd.row(skillId));
		return result;
	}
	
	public int starEffectAddVal(int skillId,int effId){
		if(skillEffAdd.contains(skillId, effId)){
			return skillEffAdd.get(skillId, effId);
		}
		return 0;
	}

	public int getSupersoldierId() {
		return supersoldierId;
	}

	public int getStarLevel() {
		return starLevel;
	}

	public int getMaxLevel() {
		return maxLevel;
	}

	public String getPiecesForNextLevel() {
		return piecesForNextLevel;
	}

	public double getStarPower() {
		return starPower;
	}

	public String getStarAttrValue() {
		return starAttrValue;
	}

	public void setStarAttrMap(ImmutableMap<Integer, Double> starAttrMap) {
		throw new UnsupportedOperationException();
	}

	public int getMaxSkillLevel() {
		return maxSkillLevel;
	}

	public int getId() {
		return id;
	}

	public int getStepLevel() {
		return stepLevel;
	}

	public String getStarEffectAdd() {
		return starEffectAdd;
	}

	public Map<Integer, Integer> getAtkAttr() {
		return SerializeHelper.cfgStr2Map(atkAttr);
	}

	public Map<Integer, Integer> getHpAttr() {
		return SerializeHelper.cfgStr2Map(atkAttr);
	}
}
