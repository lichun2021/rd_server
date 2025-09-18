package com.hawk.game.config;

import java.util.ArrayList;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

import com.hawk.game.march.ArmyInfo;

/**
 * 刷点
 *
 */
@HawkConfigManager.XmlResource(file = "xml/xzq_point.xml")
@HawkConfigBase.CombineId(fields = { "x", "y" })
public class XZQPointCfg extends HawkConfigBase {

	@Id
	protected final int id;
	/**
	 * 坐标x
	 */
	protected final int x;
	/**
	 * 坐标y
	 */
	protected final int y;

	/** 半径*/
	protected final int gridCnt;

	/**
	 * 超级武器等级
	 */
	protected final int level;

	/**
	 * 需要门票个数
	 */
	protected final int needTicketNum;

	/**
	 * 需要联盟成员个数
	 */
	protected final int guildMemberNeed;

	/**
	 * 前置
	 */
	protected final String prePoint;
	/**
	 * 英雄
	 */
	protected final String heroList;
	/**
	 * 兵
	 */
	protected final String soldierList;

	protected final String firstOccupiedAward;
	protected final String award;

	private List<Integer> perBuilds = new ArrayList<>();
	private List<Integer> awards = new ArrayList<>();
	private List<Integer> firstAwards = new ArrayList<>();
	private List<Integer> heroIdList;
	private List<ArmyInfo> armyList;

	public XZQPointCfg() {
		this.id = 0;
		this.x = 0;
		this.y = 0;
		this.level = 0;
		this.gridCnt = 2;
		needTicketNum = 0;
		guildMemberNeed = 0;
		heroList = "";
		soldierList = "";
		prePoint = "";
		firstOccupiedAward = "";
		award = "";
	}

	@Override
	protected boolean assemble() {
		if (!HawkOSOperator.isEmptyString(this.prePoint)) {
			List<Integer> list = new ArrayList<>();
			String[] arr = this.prePoint.split(",");
			for (String str : arr) {
				list.add(Integer.parseInt(str));
			}
			this.perBuilds = list;
		}
		if (!HawkOSOperator.isEmptyString(this.award)) {
			List<Integer> list = new ArrayList<>();
			String[] arr = this.award.split(",");
			for (String str : arr) {
				list.add(Integer.parseInt(str));
			}
			this.awards = list;
		}
		if (!HawkOSOperator.isEmptyString(this.firstOccupiedAward)) {
			List<Integer> list = new ArrayList<>();
			String[] arr = this.firstOccupiedAward.split(",");
			for (String str : arr) {
				list.add(Integer.parseInt(str));
			}
			this.firstAwards = list;
		}
		
		heroIdList = new ArrayList<>();
		if (!HawkOSOperator.isEmptyString(heroList)) {
			String[] heros = heroList.split("\\|");
			for (int i = 0; i < heros.length; i++) {
				heroIdList.add(Integer.valueOf(heros[i]));
			}
		}
		armyList = new ArrayList<>();
		if (!HawkOSOperator.isEmptyString(soldierList)) {
			String[] soldiers = soldierList.split("\\|");
			for (int i = 0; i < soldiers.length; i++) {
				String[] soldier = soldiers[i].split("_");
				armyList.add(new ArmyInfo(Integer.parseInt(soldier[0]), Integer.parseInt(soldier[1])));
			}
		}
		return true;
	}

	public int getId() {
		return id;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public int getLevel() {
		return level;
	}

	public int getGridCnt() {
		return gridCnt;
	}

	public int getNeedTicketNum() {
		return needTicketNum;
	}

	public int getGuildMemberNeed() {
		return guildMemberNeed;
	}

	public List<Integer> getPerBuilds() {
		return perBuilds;
	}

	public List<Integer> getAwards() {
		return awards;
	}

	public List<Integer> getFirstAwards() {
		return firstAwards;
	}
	

	public List<Integer> getHeroIdList() {
		return heroIdList;
	}

	public List<ArmyInfo> getArmyList() {
		List<ArmyInfo> list = new ArrayList<>();
		armyList.forEach(e -> list.add(e.getCopy()));
		return list;
	}
	
	
	public boolean hasPre(List<Integer> ids){
		if(this.perBuilds.size() <= 0){
			return true;
		}
		for(int per : this.perBuilds){
			if(ids.contains(per)){
				return true;
			}
		}
		return false;
	}
}
