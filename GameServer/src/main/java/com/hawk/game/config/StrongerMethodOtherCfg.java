package com.hawk.game.config;

import java.util.ArrayList;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.game.protocol.StrengthenGuide.StrengthenGuideType;

@HawkConfigManager.XmlResource(file = "xml/stronger_method_other.xml")
public class StrongerMethodOtherCfg extends HawkConfigBase {

	@Id
	private final int id;
	
	private final float percent;
	
	private final double perScore;
	
	private final int total;
	
	private final int methodType;
	
	//英雄类别算分项
	static private ArrayList<StrongerMethodOtherCfg> sghero = new ArrayList<>();
	
	//科技类别算分项
	static private ArrayList<StrongerMethodOtherCfg> sgscience = new ArrayList<>();
	
	//指挥官天赋成就类别算分项
	static private ArrayList<StrongerMethodOtherCfg> sgcommander = new ArrayList<>();
	
	public static ArrayList<StrongerMethodOtherCfg> getHero(){
		return sghero;
	}
	
	public static ArrayList<StrongerMethodOtherCfg> getScience(){
		return sgscience;
	}
	
	public static ArrayList<StrongerMethodOtherCfg> getCommander(){
		return sgcommander;
	}
	
	public StrongerMethodOtherCfg(){
		id = 1;
		percent = 1;
		perScore = 1;
		total = 14;
		methodType = 5;	
	}

	public int getId() {
		return id;
	}

	public float getPercent() {
		return percent;
	}

	public double getPerScore() {
		return perScore;
	}

	public int getTotal() {
		return total;
	}


	public int getMethodType() {
		return methodType;
	}
	
	@Override
	protected boolean assemble() {
		if(super.assemble()){
			if( StrengthenGuideType.Hero.getNumber() == this.getMethodType() ){
				sghero.add(this);
			}else if(StrengthenGuideType.Science.getNumber() == this.getMethodType() ){
				sgscience.add(this);
			}else if(StrengthenGuideType.Commander.getNumber() == this.getMethodType() ){
				sgcommander.add(this);
			}
			return true;
		}
		return false;
	}
}
