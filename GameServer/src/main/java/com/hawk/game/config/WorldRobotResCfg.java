package com.hawk.game.config;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.math.NumberUtils;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.tuple.HawkTuple3;
import org.hawk.tuple.HawkTuples;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;

/**
 * 联盟旗帜配置
 * 
 * @author shadow
 *
 */
@HawkConfigManager.XmlResource(file = "xml/world_robot_res.xml")
public class WorldRobotResCfg extends HawkConfigBase {
	@Id
	protected final int id;// ="1"
	protected final int down;// ="70"
	protected final int up;// ="130"
	protected final int res1007;// ="174991"
	protected final int res1008;// ="274991"
	protected final int res1009;// ="374991"
	protected final int res1010;// ="474991"
	protected final String soldier;// ="100101_150000_20000,100201_150000_20000,110018_150000_20000";

	private List<HawkTuple3<Integer, Integer, Integer>> soldierList;

	public WorldRobotResCfg() {
		this.id = 0;
		this.down = 70;
		this.up = 130;
		this.res1007 = 174991;
		this.res1008 = 174991;
		this.res1009 = 174991;
		this.res1010 = 174991;
		this.soldier = "100101_150000_20000,100201_150000_20000,110018_150000_20000";
	}

	public List<HawkTuple3<Integer, Integer, Integer>> getSoldierList() {
		return soldierList;
	}

	@Override
	protected boolean assemble() {
		List<String> attrs = Splitter.on(",").omitEmptyStrings().splitToList(soldier);
		List<HawkTuple3<Integer, Integer, Integer>> list = new ArrayList<>();
		for (String str : attrs) {
			String[] arr = Splitter.on("_").omitEmptyStrings().splitToList(str).toArray(new String[3]);
			list.add(HawkTuples.tuple(NumberUtils.toInt(arr[0]), NumberUtils.toInt(arr[1]), NumberUtils.toInt(arr[2])));
		}
		this.soldierList = ImmutableList.copyOf(list);
		return super.assemble();
	}

	public int getId() {
		return id;
	}

	public int getDown() {
		return down;
	}

	public int getUp() {
		return up;
	}

	public int getRes1007() {
		return res1007;
	}

	public int getRes1008() {
		return res1008;
	}

	public int getRes1009() {
		return res1009;
	}

	public int getRes1010() {
		return res1010;
	}

	public String getSoldier() {
		return soldier;
	}

}
