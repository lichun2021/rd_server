package com.hawk.game.config;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.math.NumberUtils;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.tuple.HawkTuple2;
import org.hawk.tuple.HawkTuples;

@HawkConfigManager.XmlResource(file = "xml/lmjy_battle.xml")
public class LMJYBattleCfg extends HawkConfigBase {

	@Id
	protected final int id;// ="101"
	protected final int difficult;// ="1"
	protected final int prepairTime;
	protected final int fireSpeed;
	protected final int battleTime;// ="300"
	protected final String npc;// ="101,102,103"
	protected final String bornPointA;// ="24,25|26,27|22,22"
	protected final String bornPointB;// ="24,25|20,18|18,16"
	protected final double playerMarchSpeedUp;// ="1.5"
	protected final double npcMarchSpeedUp;// ="1.5"

	private List<HawkTuple2<Integer, Integer>> bornPointAList = new ArrayList<>();
	private List<HawkTuple2<Integer, Integer>> bornPointBList = new ArrayList<>();
	private List<Integer> npcList = new ArrayList<>();

	public LMJYBattleCfg() {
		this.id = 0;
		this.difficult = 1;
		this.npc = "101";
		this.bornPointA = "";
		this.bornPointB = "";
		this.battleTime = 300;
		prepairTime = 30;
		fireSpeed = 10;
		playerMarchSpeedUp = 1.5;
		npcMarchSpeedUp = 1.5;
	}

	@Override
	protected boolean assemble() {
		for (String xy : getBornPointA().trim().split("\\|")) {
			String[] x_y = xy.split("\\,");
			int[] pos = new int[2];
			pos[0] = NumberUtils.toInt(x_y[0]);
			pos[1] = NumberUtils.toInt(x_y[1]);
			bornPointAList.add(HawkTuples.tuple(pos[0], pos[1]));
		}
		for (String xy : getBornPointB().trim().split("\\|")) {
			String[] x_y = xy.split("\\,");
			int[] pos = new int[2];
			pos[0] = NumberUtils.toInt(x_y[0]);
			pos[1] = NumberUtils.toInt(x_y[1]);
			bornPointBList.add(HawkTuples.tuple(pos[0], pos[1]));
		}

		for (String npc : getNpc().trim().split("\\,")) {
			npcList.add(NumberUtils.toInt(npc));
		}
		return super.assemble();
	}

	public List<Integer> copyOfNpcList() {
		List<Integer> result = new ArrayList<>();
		result.addAll(npcList);
		return result;
	}

	public List<int[]> copyOfbornPointAList() {
		List<int[]> result = new LinkedList<int[]>();
		for (HawkTuple2<Integer, Integer> ht : bornPointAList) {
			result.add(new int[] { ht.first, ht.second });
		}
		return result;
	}

	public List<int[]> copyOfbornPointBList() {
		List<int[]> result = new LinkedList<int[]>();
		for (HawkTuple2<Integer, Integer> ht : bornPointBList) {
			result.add(new int[] { ht.first, ht.second });
		}
		return result;
	}

	public int getId() {
		return id;
	}

	public int getDifficult() {
		return difficult;
	}

	public String getBornPointA() {
		return bornPointA;
	}

	public String getBornPointB() {
		return bornPointB;
	}

	public String getNpc() {
		return npc;
	}

	public int getBattleTime() {
		return battleTime;
	}

	public int getPrepairTime() {
		return prepairTime;
	}

	public int getFireSpeed() {
		return fireSpeed;
	}

	public double getPlayerMarchSpeedUp() {
		return playerMarchSpeedUp;
	}

	public double getNpcMarchSpeedUp() {
		return npcMarchSpeedUp;
	}

}
