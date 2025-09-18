package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.os.HawkRand;

import com.hawk.game.util.RandomUtil;
import com.hawk.game.util.WeightAble;

@HawkConfigManager.XmlResource(file = "xml/world_robot_name.xml")
public class WorldRobotNameCfg extends HawkConfigBase implements WeightAble{
	protected final String xing;// ="快乐的" ming="艾比盖"
	protected final String ming;// ="快乐的" ming="艾比盖"

	public WorldRobotNameCfg() {
		this.xing = "快乐的";
		this.ming = "艾比盖";
	}
	
	public static String randmName(){
		ConfigIterator<WorldRobotNameCfg> poolList = HawkConfigManager.getInstance().getConfigIterator(WorldRobotNameCfg.class);
		WorldRobotNameCfg xingCfg = RandomUtil.random(poolList.toList());
		WorldRobotNameCfg mingCfg = RandomUtil.random(poolList.toList());
		
		String result = xingCfg.getXing()+mingCfg.getMing();
		
		if(result.length()>7){
			int start = HawkRand.randInt(0, result.length()-7);
			result = result.substring(start, start+7);
		}
		return result;
	}

	public String getXing() {
		return xing;
	}

	public String getMing() {
		return ming;
	}

	@Override
	public int getWeight() {
		return 1;
	}

}
