package com.hawk.game.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

import com.hawk.game.util.GameUtil;

/**
 * 航海要塞(跨服建筑)
 * 
 * @author golden
 *
 */
@HawkConfigManager.KVResource(file = "xml/cross_fortress_const.xml")
public class CrossFortressConstCfg extends HawkConfigBase {

	/**
	 * 半径
	 */
	protected final int radius;
	
	/**
	 * 坐标
	 */
	protected final String pos;

	/**
	 * tick周期
	 */
	protected final long tickPeriod;

	/**
	 * 实时刷新占领信息
	 */
	protected final boolean occupyInfoRT;
	
	/**
	 * 占领信息刷新周期(s)
	 */
	protected final long occupyInfoRefPeriod;
	
	/**
	 * 排名获得航海之星数量
	 */
	protected final String rankStar;
	
	/**
	 * 积分tick周期(s)
	 */
	protected final int scoreTickPeriod;
	
	/**
	 * 要塞建筑坐标
	 */
	private List<int[]> posList;
	
	/**
	 * 排名获得航海之星数量
	 */
	private Map<Integer, Integer> rankStarMap;
	
	/**
	 * 静态对象
	 */
	private static CrossFortressConstCfg instance = null;

	/**
	 * 获取静态对象
	 */
	public static CrossFortressConstCfg getInstance() {
		return instance;
	}
	
	public CrossFortressConstCfg() {
		instance = this;
		radius = 4;
		pos = "";
		tickPeriod = 1000L;
		occupyInfoRT = false;
		occupyInfoRefPeriod = 300;
		rankStar = "";
		scoreTickPeriod = 60;
	}

	public int getRadius() {
		return radius;
	}

	public String getPos() {
		return pos;
	}

	public long getTickPeriod() {
		return tickPeriod;
	}

	public List<int[]> getPosList() {
		return posList;
	}

	public String getRankStar() {
		return rankStar;
	}
	
	public boolean isOccupyInfoRT() {
		return occupyInfoRT;
	}

	public long getOccupyInfoRefPeriod() {
		return occupyInfoRefPeriod * 1000L;
	}

	public long getScoreTickPeriod() {
		return scoreTickPeriod * 1000L;
	}

	public int getRankStar(int rank) {
		Integer star = rankStarMap.get(rank);
		if (star == null) {
			return 0;
		}
		return star;
	}

	@Override
	protected boolean assemble() {
		posList = GameUtil.splitPosList(pos);
		
		Map<Integer, Integer> rankStarMap = new HashMap<>();
		if (!HawkOSOperator.isEmptyString(rankStar)) {
			String[] star = rankStar.split(",");
			for (int i = 0; i < star.length; i++) {
				String[] info = star[i].split("_");
				rankStarMap.put(Integer.valueOf(info[0]), Integer.valueOf(info[1]));
			}
		}
		this.rankStarMap = rankStarMap;
		
		return true;
	}
}
