package com.hawk.game.config;

import java.util.ArrayList;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

import com.hawk.game.item.RandomItem;
import com.hawk.game.util.RandomUtil;

/**
 * 新版野怪类型随机配置
 * @author golden
 *
 */
@HawkConfigManager.XmlResource(file = "xml/world_newMonster_model.xml")
public class WorldNewMonsterModelCfg extends HawkConfigBase {
	@Id
	protected final int id;
	
	/**
	 * 星期几
	 */
	protected final int week;
	
	/**
	 * 野怪类型
	 */
	protected final String modelType;
	
	/**
	 * 权重
	 */
	protected final String typeWeight;
	
	/**
	 * 老版野怪类型
	 */
	protected final String oldMonsterModelType;
	
	/**
	 * 老版野怪权重
	 */
	protected final String oldMonsterTypeWeight;
	
	/**
	 * 随机列表
	 */
	private List<RandomItem> modelRandom;
	
	/**
	 * 老版野怪随机列表
	 */
	private List<RandomItem> oldMonstermodelRandom;
	
	public WorldNewMonsterModelCfg() {
		id = 0;
		week = 1;
		modelType = "";
		typeWeight = "";
		oldMonsterModelType = "";
		oldMonsterTypeWeight = "";
	}

	public int getId() {
		return id;
	}

	public String getModelType() {
		return modelType;
	}

	public String getTypeWeight() {
		return typeWeight;
	}
	
	public int getWeek() {
		return week;
	}

	public String getOldMonsterModelType() {
		return oldMonsterModelType;
	}

	public String getOldMonsterTypeWeight() {
		return oldMonsterTypeWeight;
	}

	/**
	 * 随机刷新野怪modelType
	 * @return
	 */
	public int getRandomModelType() {
		return RandomUtil.random(modelRandom).getType();
	}
	
	/**
	 * 随机刷新老版野怪modelType
	 * @return
	 */
	public int getOldMonstermodelRandom() {
		return RandomUtil.random(oldMonstermodelRandom).getType();
	}

	@Override
	protected boolean assemble() {
		modelRandom = new ArrayList<>();
		if (!HawkOSOperator.isEmptyString(modelType) && !HawkOSOperator.isEmptyString(typeWeight)) {
			String[] modelTypeSplit = modelType.split("_");
			String[] typeWeightSplit = typeWeight.split("_");

			for (int i = 0; i < modelTypeSplit.length; i++) {
				modelRandom.add(new RandomItem(Integer.parseInt(modelTypeSplit[i]), Integer.parseInt(typeWeightSplit[i])));
			}
		}
		
		oldMonstermodelRandom = new ArrayList<>();
		if (!HawkOSOperator.isEmptyString(oldMonsterModelType) && !HawkOSOperator.isEmptyString(oldMonsterTypeWeight)) {
			String[] modelTypeSplit = oldMonsterModelType.split("_");
			String[] typeWeightSplit = oldMonsterTypeWeight.split("_");

			for (int i = 0; i < modelTypeSplit.length; i++) {
				oldMonstermodelRandom.add(new RandomItem(Integer.parseInt(modelTypeSplit[i]), Integer.parseInt(typeWeightSplit[i])));
			}
		}
		
		return true;
	}
}
