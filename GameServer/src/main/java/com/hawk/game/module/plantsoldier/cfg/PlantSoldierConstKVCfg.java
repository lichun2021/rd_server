package com.hawk.game.module.plantsoldier.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

@HawkConfigManager.KVResource(file = "xml/plant_soldier_const.xml")
public class PlantSoldierConstKVCfg extends HawkConfigBase {
	// # 泰能进化所-初始训练上限
	private final int evolutionNumber;
	// 泰能进化所 前置条件,必须创建前面的建筑
	protected final String evolutionFrontBuild;
	// # 泰能伤员中心-初始医院容量
	private final int hospitalNumber;

	// 取消进化，返还资源比例，万分比 前后端
	private final int cancelEvolutionRes;// 5000
	// 取消进化，返还勋章比例，万分比 前后端
	private final int cancelEvolutionItem;// 10000

	// 前置建筑
	protected int[] evolutionFrontBuildIds = new int[0];

	public PlantSoldierConstKVCfg() {
		evolutionNumber = 5000;
		evolutionFrontBuild = "";
		hospitalNumber = 10000;
		cancelEvolutionRes = 5000;
		cancelEvolutionItem = 10000;
	}

	@Override
	protected boolean assemble() {

		// 前置建筑id
		if (evolutionFrontBuild != null && !"".equals(evolutionFrontBuild) && !"0".equals(evolutionFrontBuild)) {
			String[] ids = evolutionFrontBuild.split(",");
			evolutionFrontBuildIds = new int[ids.length];
			int index = 0;
			for (String frontId : ids) {
				evolutionFrontBuildIds[index] = Integer.parseInt(frontId);
				index++;
			}
		}

		return true;
	}

	public int getEvolutionNumber() {
		return evolutionNumber;
	}

	public int[] getEvolutionFrontBuildIds() {
		return evolutionFrontBuildIds;
	}

	public int getHospitalNumber() {
		return hospitalNumber;
	}

	public String getEvolutionFrontBuild() {
		return evolutionFrontBuild;
	}

	public int getCancelEvolutionRes() {
		return cancelEvolutionRes;
	}

	public int getCancelEvolutionItem() {
		return cancelEvolutionItem;
	}

	public void setEvolutionFrontBuildIds(int[] evolutionFrontBuildIds) {
		this.evolutionFrontBuildIds = evolutionFrontBuildIds;
	}

}