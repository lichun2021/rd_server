package com.hawk.game.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.lang.math.NumberUtils;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.tuple.HawkTuple2;
import org.hawk.tuple.HawkTuples;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.hawk.game.entity.BuildingBaseEntity;
import com.hawk.game.protocol.Const.BuildingType;

@HawkConfigManager.XmlResource(file = "xml/hero_office.xml")
public class HeroOfficeCfg extends HawkConfigBase {
	@Id
	protected final int id;// ="10304"
	protected final int attrId;// ="103"
	protected final int unlockBuildingType;//
	protected final int unlockLevel;// ="4" 改为建筑等级
	protected final String attrOfficeEffect;// ="101_50"
	protected final String threadUsed;// ="4_12_13" 有建筑正在使用英雄
	protected final String prioritySkill;
	protected final int isglory;
	protected final int weight;
	protected final int staffOfficer;
	protected final int taiLevel;
	private ImmutableList<HawkTuple2<Integer, Double>> attrOfficeEffectList;
	private ImmutableList<Integer> queueUsedList;
	private BuildingType buildingType;

	public HeroOfficeCfg() {
		this.id = 0;
		this.attrId = 0;
		this.unlockLevel = 0;
		this.attrOfficeEffect = "";
		this.threadUsed = "";
		unlockBuildingType = 2010;
		prioritySkill = "";
		weight = 0;
		isglory = 0;
		staffOfficer = 0;
		taiLevel=0;
	}

	/** 给点建筑集合中是否满足任命需求 */
	public boolean checkOfficeBuilding(List<BuildingBaseEntity> buildList) {
		for (BuildingBaseEntity build : buildList) {
			if (build.getType() != this.getUnlockBuildingType()) {
				continue;
			}
			BuildingCfg buildingCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, build.getBuildingCfgId());
			if (Objects.isNull(buildingCfg) || buildingCfg.getLevel() < this.getUnlockLevel()) {
				continue;
			}
			return true;
		}
		return false;
	}

	@Override
	protected boolean assemble() {
		{
			List<String> attrs = Splitter.on("|").omitEmptyStrings().splitToList(attrOfficeEffect);
			List<HawkTuple2<Integer, Double>> list = new ArrayList<>(attrs.size());
			for (String str : attrs) {
				String[] arr = Splitter.on("_").omitEmptyStrings().splitToList(str).toArray(new String[2]);
				list.add(HawkTuples.tuple(NumberUtils.toInt(arr[0]), NumberUtils.toDouble(arr[1])));
			}
			this.attrOfficeEffectList = ImmutableList.copyOf(list);
		}
		{
			List<Integer> list = Splitter.on("_").omitEmptyStrings().splitToList(threadUsed).stream()
					.mapToInt(Integer::valueOf)
					.mapToObj(Integer::valueOf)
					.collect(Collectors.toList());
			this.queueUsedList = ImmutableList.copyOf(list);
		}

		buildingType = BuildingType.valueOf(unlockBuildingType);
		return super.assemble();
	}

	public int getId() {
		return id;
	}

	public int getAttrId() {
		return attrId;
	}

	public int getUnlockLevel() {
		return unlockLevel;
	}

	public int getTaiLevel() {
		return taiLevel;
	}

	public ImmutableList<HawkTuple2<Integer, Double>> getAttrOfficeEffectList() {
		return attrOfficeEffectList;
	}

	public void setAttrOfficeEffectList(ImmutableList<HawkTuple2<Integer, Double>> attrOfficeEffectList) {
		throw new UnsupportedOperationException();
	}

	public ImmutableList<Integer> getQueueUsedList() {
		return queueUsedList;
	}

	public void setQueueUsedList(ImmutableList<Integer> queueUsedList) {
		throw new UnsupportedOperationException();
	}

	public String getThreadUsed() {
		return threadUsed;
	}

	public String getAttrOfficeEffect() {
		return attrOfficeEffect;
	}

	public BuildingType getBuildingType() {
		return buildingType;
	}

	public String getPrioritySkill() {
		return prioritySkill;
	}

	public int getWeight() {
		return weight;
	}

	public int getUnlockBuildingType() {
		return unlockBuildingType;
	}

	public int getIsglory() {
		return isglory;
	}

	public int getStaffOfficer() {
		return staffOfficer;
	}

}
