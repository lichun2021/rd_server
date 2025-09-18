package com.hawk.game.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.hawk.game.cfgElement.EffectObject;
import com.hawk.serialize.string.SerializeHelper;

/**
 * 铠甲套装
 * @author golden
 *
 */
@HawkConfigManager.XmlResource(file = "xml/armour_suit.xml")
public class ArmourSuitCfg extends HawkConfigBase {

	/**
	 * 套装id
	 */
	@Id
	protected final int id;
	
	/**
	 * 两件套激活属性
	 */
	protected final String suitAttribute1;
	
	/**
	 * 四件套激活属性
	 */
	protected final String suitAttribute2;
	
	/**
	 * 六件套激活属性
	 */
	protected final String suitAttribute3;
	
	/**
	 * 七件套激活属性
	 */
	protected final String suitAttribute4;


	/**
	 * 两件套激活量子属性
	 */
	protected final String suitQuantumAttr1;

	/**
	 * 四件套激活量子属性
	 */
	protected final String suitQuantumAttr2;

	/**
	 * 六件套激活量子属性
	 */
	protected final String suitQuantumAttr3;

	/**
	 * 七件套激活量子属性
	 */
	protected final String suitQuantumAttr4;
	
	protected final String atkAttr1;
	protected final String atkAttr2;
	protected final String atkAttr3;
	protected final String atkAttr4;
	
	protected final String hpAttr1;
	protected final String hpAttr2;
	protected final String hpAttr3;
	protected final String hpAttr4;
	
	
	/**
	 * 两件套激活属性
	 */
	private Map<Integer, List<EffectObject>> suitAttribute1Eff;
	
	/**
	 * 四件套激活属性
	 */
	private Map<Integer, List<EffectObject>> suitAttribute2Eff;
	
	/**
	 * 六件套激活属性
	 */
	private Map<Integer, List<EffectObject>> suitAttribute3Eff;
	
	/**
	 * 七件套激活属性
	 */
	private Map<Integer, List<EffectObject>> suitAttribute4Eff;

	private List<EffectObject> suitQuantumEff1;
	private List<EffectObject> suitQuantumEff2;
	private List<EffectObject> suitQuantumEff3;
	private List<EffectObject> suitQuantumEff4;
	
	private Table<Integer, Integer, Integer> atkAttr1Map;
	private Table<Integer, Integer, Integer> atkAttr2Map;
	private Table<Integer, Integer, Integer> atkAttr3Map;
	private Table<Integer, Integer, Integer> atkAttr4Map;
	
	private Table<Integer, Integer, Integer> hpAttr1Map;
	private Table<Integer, Integer, Integer> hpAttr2Map;
	private Table<Integer, Integer, Integer> hpAttr3Map;
	private Table<Integer, Integer, Integer> hpAttr4Map;
	
	public ArmourSuitCfg() {
		id = 0;
		suitAttribute1 = "";
		suitAttribute2 = "";
		suitAttribute3 = "";
		suitAttribute4 = "";
		suitQuantumAttr1 = "";
		suitQuantumAttr2 = "";
		suitQuantumAttr3 = "";
		suitQuantumAttr4 = "";
		atkAttr1 = "";
		atkAttr2 = "";
		atkAttr3 = "";
		atkAttr4 = "";
		hpAttr1 = "";
		hpAttr2 = "";
		hpAttr3 = "";
		hpAttr4 = "";
	}
	
	/**
	 * 两件套激活属性
	 */
	public List<EffectObject> getSuitAttribute1Eff(int quality) {
		return suitAttribute1Eff.get(quality);
	}

	/**
	 * 四件套激活属性
	 */
	public List<EffectObject> getSuitAttribute2Eff(int quality) {
		return suitAttribute2Eff.get(quality);
	}

	/**
	 * 六件套激活属性
	 */
	public List<EffectObject> getSuitAttribute3Eff(int quality) {
		return suitAttribute3Eff.get(quality);
	}

	/**
	 * 七件套激活属性
	 */
	public List<EffectObject> getSuitAttribute4Eff(int quality) {
		return suitAttribute4Eff.get(quality);
	}

	public List<EffectObject> getSuitQuantumEff1() {
		return suitQuantumEff1;
	}

	public List<EffectObject> getSuitQuantumEff2() {
		return suitQuantumEff2;
	}

	public List<EffectObject> getSuitQuantumEff3() {
		return suitQuantumEff3;
	}

	public List<EffectObject> getSuitQuantumEff4() {
		return suitQuantumEff4;
	}

	public int getAtkAttr1(int quality, int soldierType) {
		if (!atkAttr1Map.contains(quality, soldierType)) {
			return 0;
		}
		return atkAttr1Map.get(quality, soldierType);
	}
	
	public int getAtkAttr2(int quality, int soldierType) {
		if (!atkAttr2Map.contains(quality, soldierType)) {
			return 0;
		}
		return atkAttr2Map.get(quality, soldierType);
	}
	
	public int getAtkAttr3(int quality, int soldierType) {
		if (!atkAttr3Map.contains(quality, soldierType)) {
			return 0;
		}
		return atkAttr3Map.get(quality, soldierType);
	}
	
	public int getAtkAttr4(int quality, int soldierType) {
		if (!atkAttr4Map.contains(quality, soldierType)) {
			return 0;
		}
		return atkAttr4Map.get(quality, soldierType);
	}
	
	public int getHpAttr1(int quality, int soldierType) {
		if (!hpAttr1Map.contains(quality, soldierType)) {
			return 0;
		}
		return hpAttr1Map.get(quality, soldierType);
	}
	
	public int getHpAttr2(int quality, int soldierType) {
		if (!hpAttr2Map.contains(quality, soldierType)) {
			return 0;
		}
		return hpAttr2Map.get(quality, soldierType);
	}
	
	public int getHpAttr3(int quality, int soldierType) {
		if (!hpAttr3Map.contains(quality, soldierType)) {
			return 0;
		}
		return hpAttr3Map.get(quality, soldierType);
	}
	
	public int getHpAttr4(int quality, int soldierType) {
		if (!hpAttr4Map.contains(quality, soldierType)) {
			return 0;
		}
		return hpAttr4Map.get(quality, soldierType);
	}
	@Override
	protected boolean assemble() {
		Map<Integer, List<EffectObject>> suitAttribute1Eff = new HashMap<>();
		if (!HawkOSOperator.isEmptyString(suitAttribute1)) {
			String[] suitAttr = suitAttribute1.split(SerializeHelper.SEMICOLON_ITEMS);
			for (int i = 0; i < suitAttr.length; i++) {
				List<EffectObject> eff = SerializeHelper.stringToList(EffectObject.class, suitAttr[i], SerializeHelper.SEMICOLON_ITEMS, SerializeHelper.ATTRIBUTE_SPLIT, new ArrayList<>());
				suitAttribute1Eff.put(i + 1, eff);
			}
		}
		this.suitAttribute1Eff = suitAttribute1Eff;
		
		Map<Integer, List<EffectObject>> suitAttribute2Eff = new HashMap<>();
		if (!HawkOSOperator.isEmptyString(suitAttribute2)) {
			String[] suitAttr = suitAttribute2.split(SerializeHelper.SEMICOLON_ITEMS);
			for (int i = 0; i < suitAttr.length; i++) {
				List<EffectObject> eff = SerializeHelper.stringToList(EffectObject.class, suitAttr[i], SerializeHelper.SEMICOLON_ITEMS, SerializeHelper.ATTRIBUTE_SPLIT, new ArrayList<>());
				suitAttribute2Eff.put(i + 1, eff);
			}
		}
		this.suitAttribute2Eff = suitAttribute2Eff;
		
		Map<Integer, List<EffectObject>> suitAttribute3Eff = new HashMap<>();
		if (!HawkOSOperator.isEmptyString(suitAttribute3)) {
			String[] suitAttr = suitAttribute3.split(SerializeHelper.SEMICOLON_ITEMS);
			for (int i = 0; i < suitAttr.length; i++) {
				List<EffectObject> eff = SerializeHelper.stringToList(EffectObject.class, suitAttr[i], SerializeHelper.SEMICOLON_ITEMS, SerializeHelper.ATTRIBUTE_SPLIT, new ArrayList<>());
				suitAttribute3Eff.put(i + 1, eff);
			}
		}
		this.suitAttribute3Eff = suitAttribute3Eff;
		
		Map<Integer, List<EffectObject>> suitAttribute4Eff = new HashMap<>();
		if (!HawkOSOperator.isEmptyString(suitAttribute4)) {
			String[] suitAttr = suitAttribute4.split(SerializeHelper.SEMICOLON_ITEMS);
			for (int i = 0; i < suitAttr.length; i++) {
				List<EffectObject> eff = SerializeHelper.stringToList(EffectObject.class, suitAttr[i], SerializeHelper.SEMICOLON_ITEMS, SerializeHelper.ATTRIBUTE_SPLIT, new ArrayList<>());
				suitAttribute4Eff.put(i + 1, eff);
			}
		}
		this.suitAttribute4Eff = suitAttribute4Eff;

		this.suitQuantumEff1 = SerializeHelper.stringToList(EffectObject.class, suitQuantumAttr1, SerializeHelper.BETWEEN_ITEMS, SerializeHelper.ATTRIBUTE_SPLIT, new ArrayList<>());
		this.suitQuantumEff2 = SerializeHelper.stringToList(EffectObject.class, suitQuantumAttr2, SerializeHelper.BETWEEN_ITEMS, SerializeHelper.ATTRIBUTE_SPLIT, new ArrayList<>());
		this.suitQuantumEff3 = SerializeHelper.stringToList(EffectObject.class, suitQuantumAttr3, SerializeHelper.BETWEEN_ITEMS, SerializeHelper.ATTRIBUTE_SPLIT, new ArrayList<>());
		this.suitQuantumEff4 = SerializeHelper.stringToList(EffectObject.class, suitQuantumAttr4, SerializeHelper.BETWEEN_ITEMS, SerializeHelper.ATTRIBUTE_SPLIT, new ArrayList<>());

		
		Table<Integer, Integer, Integer> atkAttr1Map = HashBasedTable.create();
		if (!HawkOSOperator.isEmptyString(atkAttr1)) {
			String[] split = atkAttr1.split(";");
			for (int i= 0; i < split.length; i++) {
				String[] split2 = split[i].split(",");
				for (int j = 0; j < split2.length; j++) {
					String[] split3 = split2[j].split("_");
					atkAttr1Map.put(i + 1, Integer.valueOf(split3[0]), Integer.valueOf(split3[1]));
				}
			}
		}
		this.atkAttr1Map = atkAttr1Map;
		
		Table<Integer, Integer, Integer> atkAttr2Map = HashBasedTable.create();
		if (!HawkOSOperator.isEmptyString(atkAttr2)) {
			String[] split = atkAttr2.split(";");
			for (int i= 0; i < split.length; i++) {
				String[] split2 = split[i].split(",");
				for (int j = 0; j < split2.length; j++) {
					String[] split3 = split2[j].split("_");
					atkAttr2Map.put(i + 1, Integer.valueOf(split3[0]), Integer.valueOf(split3[1]));
				}
			}
		}
		this.atkAttr2Map = atkAttr2Map;
		
		Table<Integer, Integer, Integer> atkAttr3Map = HashBasedTable.create();
		if (!HawkOSOperator.isEmptyString(atkAttr3)) {
			String[] split = atkAttr3.split(";");
			for (int i= 0; i < split.length; i++) {
				String[] split2 = split[i].split(",");
				for (int j = 0; j < split2.length; j++) {
					String[] split3 = split2[j].split("_");
					atkAttr3Map.put(i + 1, Integer.valueOf(split3[0]), Integer.valueOf(split3[1]));
				}
			}
		}
		this.atkAttr3Map = atkAttr3Map;
		
		Table<Integer, Integer, Integer> atkAttr4Map = HashBasedTable.create();
		if (!HawkOSOperator.isEmptyString(atkAttr4)) {
			String[] split = atkAttr4.split(";");
			for (int i= 0; i < split.length; i++) {
				String[] split2 = split[i].split(",");
				for (int j = 0; j < split2.length; j++) {
					String[] split3 = split2[j].split("_");
					atkAttr4Map.put(i + 1, Integer.valueOf(split3[0]), Integer.valueOf(split3[1]));
				}
			}
		}
		this.atkAttr4Map = atkAttr4Map;
		
		Table<Integer, Integer, Integer> hpAttr1Map = HashBasedTable.create();
		if (!HawkOSOperator.isEmptyString(hpAttr1)) {
			String[] split = hpAttr1.split(";");
			for (int i= 0; i < split.length; i++) {
				String[] split2 = split[i].split(",");
				for (int j = 0; j < split2.length; j++) {
					String[] split3 = split2[j].split("_");
					hpAttr1Map.put(i + 1, Integer.valueOf(split3[0]), Integer.valueOf(split3[1]));
				}
			}
		}
		this.hpAttr1Map = hpAttr1Map;
		
		Table<Integer, Integer, Integer> hpAttr2Map = HashBasedTable.create();
		if (!HawkOSOperator.isEmptyString(hpAttr2)) {
			String[] split = hpAttr2.split(";");
			for (int i= 0; i < split.length; i++) {
				String[] split2 = split[i].split(",");
				for (int j = 0; j < split2.length; j++) {
					String[] split3 = split2[j].split("_");
					hpAttr2Map.put(i + 1, Integer.valueOf(split3[0]), Integer.valueOf(split3[1]));
				}
			}
		}
		this.hpAttr2Map = hpAttr2Map;
		
		Table<Integer, Integer, Integer> hpAttr3Map = HashBasedTable.create();
		if (!HawkOSOperator.isEmptyString(hpAttr3)) {
			String[] split = hpAttr3.split(";");
			for (int i= 0; i < split.length; i++) {
				String[] split2 = split[i].split(",");
				for (int j = 0; j < split2.length; j++) {
					String[] split3 = split2[j].split("_");
					hpAttr3Map.put(i + 1, Integer.valueOf(split3[0]), Integer.valueOf(split3[1]));
				}
			}
		}
		this.hpAttr3Map = hpAttr3Map;
		
		Table<Integer, Integer, Integer> hpAttr4Map = HashBasedTable.create();
		if (!HawkOSOperator.isEmptyString(hpAttr4)) {
			String[] split = hpAttr4.split(";");
			for (int i= 0; i < split.length; i++) {
				String[] split2 = split[i].split(",");
				for (int j = 0; j < split2.length; j++) {
					String[] split3 = split2[j].split("_");
					hpAttr4Map.put(i + 1, Integer.valueOf(split3[0]), Integer.valueOf(split3[1]));
				}
			}
		}
		this.hpAttr4Map = hpAttr4Map;
		return true;
	}
}
