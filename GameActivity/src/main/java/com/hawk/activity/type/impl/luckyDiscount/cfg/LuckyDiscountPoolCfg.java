package com.hawk.activity.type.impl.luckyDiscount.cfg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.tuple.HawkTuple2;


/**
 * 奖池配置配置
 * @author RickMei
 *
 */
@HawkConfigManager.XmlResource(file = "activity/lucky_discount/luckydiscount_pool.xml")
public class LuckyDiscountPoolCfg extends HawkConfigBase {
	/** */
	@Id
	private final int id;
	/** 组*/
	private final int team;
	/** 权重*/
	private final int weight;
	/** 折扣*/
	private final String discount;
	
	/**池子id*/
	private final int pool;
	private static Map<Integer, List<LuckyDiscountPoolCfg>> poolMap = new HashMap<>();
	
	private static Map<Integer, Integer> poolTotalWeight = new HashMap<>();
	
	public static Map<Integer, Integer> getPoolTotalWeight() {
		return poolTotalWeight;
	}

	public static HawkTuple2<Integer, List<LuckyDiscountPoolCfg>> getPoolWithTeamId( int teamId ){
		Integer totalWeight = poolTotalWeight.get(teamId);
		List<LuckyDiscountPoolCfg> list = poolMap.get(teamId);
		if( null != totalWeight &&  null != list ){
			return new HawkTuple2<Integer, List<LuckyDiscountPoolCfg>>(totalWeight, list);
		}
		return null;
	}
	
	public LuckyDiscountPoolCfg() {
		id = 0;
		team = 0;
		weight = 0;
		discount = "1.1";
		pool = 1;
	}
	
	public String getDiscount() {
		return discount;
	}

	@Override
	protected boolean assemble() {
		List<LuckyDiscountPoolCfg> pool = poolMap.get(this.team);
		if(null == pool){
			pool = new ArrayList<LuckyDiscountPoolCfg>();
			poolMap.put(team,pool);
		}
		pool.add( this );
		//计算weight
		Integer totalWeight = poolTotalWeight.get(this.team);
		if(null == totalWeight){
			poolTotalWeight.put(team, this.getWeight());
			return true;
		}
		poolTotalWeight.replace(team, totalWeight + this.getWeight());
		return true;
	}

	public int getId() {
		return id;
	}

	public int getTeam() {
		return team;
	}

	public int getWeight() {
		return weight;
	}

	public int getPool() {
		return pool;
	}
}
