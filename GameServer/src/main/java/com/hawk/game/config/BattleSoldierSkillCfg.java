package com.hawk.game.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.math.NumberUtils;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.helper.HawkAssert;

import com.google.common.base.MoreObjects;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.PBSoldierSkill;

/**
 * 战斗技能配置
 * 
 * @author Link
 */
@HawkConfigManager.XmlResource(file = "xml/battle_soldier_skill.xml")
public class BattleSoldierSkillCfg extends HawkConfigBase {
	@Id
	protected final int id;// ="10101" soldierType*10000 + 技能index*100 + 等级
	protected final String des;// ="装甲坦克（盾兵）反步兵装甲(盾牌)1级trigger=触发概率;damage=伤害参数"
	protected final int trigger;// ="10000"
	protected final String p1;// ="2000"
	protected final String p2;
	protected final String p3;
	protected final String p4;
	protected final String p5;
	protected final String p6;
	private PBSoldierSkill index;
	
	private ImmutableMap<EffType, Integer> honor10buffMap;

	public BattleSoldierSkillCfg() {
		id = 0;
		des = "";
		trigger = 0;
		p1 = "";
		p2 = "";
		p3 = "";
		p4 = "";
		p5 = "";
		p6 = "";
	}

	@Override
	protected boolean assemble() {
		this.index = PBSoldierSkill.valueOf(id / 100);

		if (index.getNumber() % 100 == 34) {// 34号技能10星堡专有
			Map<EffType, Integer> lsit = new HashMap<>();
			List<String> tvList = Splitter.on("|").omitEmptyStrings().splitToList(p2);
			for (String xy : tvList) {
				String[] x_y = xy.split("_");
				if (x_y.length < 2) {
					continue;
				}
				int[] pos = new int[2];
				pos[0] = NumberUtils.toInt(x_y[0]);
				pos[1] = NumberUtils.toInt(x_y[1]);
				lsit.put(EffType.valueOf(pos[0]), pos[1]);
			}
			honor10buffMap = ImmutableMap.copyOf(lsit);
		}

		return super.assemble();
	}
	
	

	@Override
	protected boolean checkValid() {
		HawkAssert.notNull(index);
		return super.checkValid();
	}

	@Override
	public String toString() {
		String str = MoreObjects.toStringHelper(this)
				.add("id", id)
				.add("des", des)
				.add("trigger", trigger)
				.add("p1", p1)
				.add("p2", p2)
				.add("p3", p3)
				.add("p4", p4)
				.add("index", index)
				.toString();
		return str;
	}
	
	public int getHonor10buff(EffType eff) {
		if (honor10buffMap == null) {
			return 0;
		}
		return honor10buffMap.getOrDefault(eff, 0);
	}

	public int getId() {
		return id;
	}

	public int getTrigger() {
		return trigger;
	}

	public String getDes() {
		return des;
	}

	public String getP1() {
		return p1;
	}

	public int getP1IntVal() {
		return NumberUtils.toInt(p1);
	}

	public String getP2() {
		return p2;
	}
	
	public int getP2IntVal() {
		return NumberUtils.toInt(p2);
	}

	public String getP3() {
		return p3;
	}
	
	public int getP3IntVal() {
		return NumberUtils.toInt(p3);
	}

	public String getP4() {
		return p4;
	}
	
	public int getP4IntVal() {
		return NumberUtils.toInt(p4);
	}

	public PBSoldierSkill getIndex() {
		return index;
	}

	public String getP5() {
		return p5;
	}
	
	public int getP5IntVal() {
		return NumberUtils.toInt(p5);
	}

	public String getP6() {
		return p6;
	}

	public void setIndex(PBSoldierSkill index) {
		throw new UnsupportedOperationException();
	}

}
