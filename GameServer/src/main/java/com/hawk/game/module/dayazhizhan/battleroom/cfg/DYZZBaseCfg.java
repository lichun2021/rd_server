package com.hawk.game.module.dayazhizhan.battleroom.cfg;

import org.apache.commons.lang.math.NumberUtils;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.tuple.HawkTuple2;
import org.hawk.tuple.HawkTuples;

@HawkConfigManager.KVResource(file = "xml/dyzz_base.xml")
public class DYZZBaseCfg extends HawkConfigBase {
	private final int redis;// = 1
	private final int initBlood;// = 10000
	// # 刷新点 x_y, 半径为偶数 x,y相加必须为奇数
	private final String refreshPointA;// = 43_42
	private final String refreshPointB;// 58_59

	// # 攻击间隔
	private final int atkCd;// = 30
	// # 对基地伤害
	private final int atkVal;// = 50
	// #能源井攻击
	private final int wellAtkVal;// = 3
	// #商人买
	private final int orderAtkVal;// = 100
	private HawkTuple2<Integer, Integer> baseAPos;
	private HawkTuple2<Integer, Integer> baseBPos;

	public DYZZBaseCfg() {
		redis = 1;
		initBlood = 10000;
		refreshPointA = "43_42";
		refreshPointB = "58_59";
		atkCd = 10000;
		atkVal = 0;
		wellAtkVal = 0;
		orderAtkVal = 0;
	}

	@Override
	protected boolean assemble() {
		{
			String[] x_y = refreshPointA.split("_");
			int[] pos = new int[2];
			pos[0] = NumberUtils.toInt(x_y[0]);
			pos[1] = NumberUtils.toInt(x_y[1]);
			baseAPos = HawkTuples.tuple(pos[0], pos[1]);
		}
		{

			String[] x_y = refreshPointB.split("_");
			int[] pos = new int[2];
			pos[0] = NumberUtils.toInt(x_y[0]);
			pos[1] = NumberUtils.toInt(x_y[1]);
			baseBPos = HawkTuples.tuple(pos[0], pos[1]);
		}

		return super.assemble();
	}

	public HawkTuple2<Integer, Integer> getBaseAPos() {
		return baseAPos;
	}

	public void setBaseAPos(HawkTuple2<Integer, Integer> baseAPos) {
		this.baseAPos = baseAPos;
	}

	public HawkTuple2<Integer, Integer> getBaseBPos() {
		return baseBPos;
	}

	public void setBaseBPos(HawkTuple2<Integer, Integer> baseBPos) {
		this.baseBPos = baseBPos;
	}

	public int getRedis() {
		return redis;
	}

	public int getInitBlood() {
		return initBlood;
	}

	public String getRefreshPointA() {
		return refreshPointA;
	}

	public String getRefreshPointB() {
		return refreshPointB;
	}

	public int getAtkCd() {
		return atkCd;
	}

	public int getAtkVal() {
		return atkVal;
	}

	public int getWellAtkVal() {
		return wellAtkVal;
	}

	public int getOrderAtkVal() {
		return orderAtkVal;
	}

}
