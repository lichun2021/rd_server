package com.hawk.game.config;

import java.util.ArrayList;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

/**
 * 世界地图怪物刷新配置
 * 
 * @author julia
 *
 */
@HawkConfigManager.XmlResource(file = "xml/world_enemyRefresh.xml")
public class WorldEnemyRefreshCfg extends HawkConfigBase {
	@Id
	protected final int id;
	// 区域等级
	protected final int openServiceTime;
	// 普通怪物
	protected final String normalEnemy;
	// 黑土地怪物
	protected final String capitalEnemy;
	// 新手期刷怪
	protected final String newTimeEnemy;
	// 怪物2/黄巾军/剿灭尤里
	protected final String yuriEnemy;
	protected final String angryYuriEnemy;
	// 怪物宝箱
	protected final String boxEnemy;
	protected final String nomalYuriEnemy;
	// 据点怪物
	protected final String strongpoint;
	
	// 自定义字段
	private List<Integer> capitalIds = new ArrayList<Integer>();
	private List<Integer> capitalWeights = new ArrayList<Integer>();
	private List<Integer> normalIds = new ArrayList<Integer>();
	private List<Integer> normalWeights = new ArrayList<Integer>();
	private List<Integer> newTimeIds = new ArrayList<Integer>();
	private List<Integer> newTimeWeights = new ArrayList<Integer>();
	private List<Integer> yuriIds = new ArrayList<Integer>();
	private List<Integer> yuriWeights = new ArrayList<Integer>();
	private List<Integer> boxEnemyIds = new ArrayList<Integer>();
	private List<Integer> boxEnemyWeights = new ArrayList<Integer>();
	
	private List<Integer> strongpointIds = new ArrayList<Integer>();
	private List<Integer> strongpointWeights = new ArrayList<Integer>();

	public WorldEnemyRefreshCfg() {
		id = 0;
		openServiceTime = 0;
		normalEnemy = "";
		capitalEnemy = "";
		newTimeEnemy = "";
		yuriEnemy = "";
		angryYuriEnemy = "";
		boxEnemy = "";
		nomalYuriEnemy = "";
		strongpoint = "";
	}

	public int getId() {
		return id;
	}

	public String getNormalEnemy() {
		return normalEnemy;
	}

	public String getCapitalEnemy() {
		return capitalEnemy;
	}

	public String getNewTimeEnemy() {
		return newTimeEnemy;
	}

	public List<Integer> getCapitalIds() {
		return capitalIds;
	}

	public List<Integer> getCapitalWeights() {
		return capitalWeights;
	}

	public List<Integer> getNormalIds() {
		return normalIds;
	}

	public List<Integer> getNormalWeights() {
		return normalWeights;
	}

	public List<Integer> getNewTimeIds() {
		return newTimeIds;
	}

	public List<Integer> getNewTimeWeights() {
		return newTimeWeights;
	}
	
	public List<Integer> getSuperIds() {
		return yuriIds;
	}

	public List<Integer> getSuperWeights() {
		return yuriWeights;
	}

	public List<Integer> getYuriIds() {
		return yuriIds;
	}

	public List<Integer> getYuriWeights() {
		return yuriWeights;
	}

	public List<Integer> getBoxEnemyIds() {
		return boxEnemyIds;
	}

	public List<Integer> getBoxEnemyWeights() {
		return boxEnemyWeights;
	}

	public String getStrongpoint() {
		return strongpoint;
	}
	
	public List<Integer> getStrongpointIds() {
		return strongpointIds;
	}

	public List<Integer> getStrongpointWeights() {
		return strongpointWeights;
	}

	public int getOpenServiceTime() {
		return openServiceTime;
	}

	@Override
	protected boolean assemble() {
		if (!HawkOSOperator.isEmptyString(yuriEnemy)) {
			String[] items = yuriEnemy.split(",");
			for (int i = 0; i < items.length; i++) {
				String[] keyWeight = items[i].split("_");
				yuriIds.add(Integer.parseInt(keyWeight[0]));
				yuriWeights.add(Integer.parseInt(keyWeight[1]));
			}
		}
		
		if (!HawkOSOperator.isEmptyString(normalEnemy)) {
			String[] items = normalEnemy.split(",");
			for (int i = 0; i < items.length; i++) {
				String[] keyWeight = items[i].split("_");
				normalIds.add(Integer.parseInt(keyWeight[0]));
				normalWeights.add(Integer.parseInt(keyWeight[1]));
			}
		}

		if (!HawkOSOperator.isEmptyString(capitalEnemy)) {
			String[] items = capitalEnemy.split(",");
			for (int i = 0; i < items.length; i++) {
				String[] keyWeight = items[i].split("_");
				capitalIds.add(Integer.parseInt(keyWeight[0]));
				capitalWeights.add(Integer.parseInt(keyWeight[1]));
			}
		}

		if (!HawkOSOperator.isEmptyString(newTimeEnemy)) {
			String[] items = newTimeEnemy.split(",");
			for (int i = 0; i < items.length; i++) {
				String[] keyWeight = items[i].split("_");
				newTimeIds.add(Integer.parseInt(keyWeight[0]));
				newTimeWeights.add(Integer.parseInt(keyWeight[1]));
			}
		}
		
		if (!HawkOSOperator.isEmptyString(boxEnemy)) {
			String[] items = boxEnemy.split(",");
			for (int i = 0; i < items.length; i++) {
				String[] keyWeight = items[i].split("_");
				boxEnemyIds.add(Integer.parseInt(keyWeight[0]));
				boxEnemyWeights.add(Integer.parseInt(keyWeight[1]));
			}
		}
		
		if (!HawkOSOperator.isEmptyString(strongpoint)) {
			String[] items = strongpoint.split(",");
			for (int i = 0; i < items.length; i++) {
				String[] keyWeight = items[i].split("_");
				strongpointIds.add(Integer.parseInt(keyWeight[0]));
				strongpointWeights.add(Integer.parseInt(keyWeight[1]));
			}
		}
		
		return true;
	}

	@Override
	protected boolean checkValid() {
		if (yuriIds != null) {
			for (Integer monsterId : yuriIds) {
				WorldEnemyCfg enemyCfg = HawkConfigManager.getInstance().getConfigByKey(WorldEnemyCfg.class, monsterId);
				if (enemyCfg == null) {
					return false;
				}
			}
		}
		
		if (normalIds != null) {
			for (Integer monsterId : normalIds) {
				WorldEnemyCfg enemyCfg = HawkConfigManager.getInstance().getConfigByKey(WorldEnemyCfg.class, monsterId);
				if (enemyCfg == null) {
					return false;
				}
			}
		}

		if (capitalIds != null) {
			for (Integer monsterId : capitalIds) {
				WorldEnemyCfg enemyCfg = HawkConfigManager.getInstance().getConfigByKey(WorldEnemyCfg.class, monsterId);
				if (enemyCfg == null) {
					return false;
				}
			}
		}

		if (newTimeIds != null) {
			for (Integer monsterId : newTimeIds) {
				WorldEnemyCfg enemyCfg = HawkConfigManager.getInstance().getConfigByKey(WorldEnemyCfg.class, monsterId);
				if (enemyCfg == null) {
					return false;
				}
			}
		}

		if (boxEnemyIds != null) {
			for (Integer monsterId : boxEnemyIds) {
				WorldEnemyCfg enemyCfg = HawkConfigManager.getInstance().getConfigByKey(WorldEnemyCfg.class, monsterId);
				if (enemyCfg == null) {
					return false;
				}
			}
		}
		
		if (strongpointIds != null) {
			for (Integer strongpointId : strongpointIds) {
				WorldStrongpointCfg pointCfg = HawkConfigManager.getInstance().getConfigByKey(WorldStrongpointCfg.class, strongpointId);
				if (pointCfg == null) {
					return false;
				}
			}
		}
		return super.checkValid();
	}

}
