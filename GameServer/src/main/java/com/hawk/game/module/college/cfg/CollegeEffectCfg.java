package com.hawk.game.module.college.cfg;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.math.NumberUtils;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.tuple.HawkTuple2;
import org.hawk.tuple.HawkTuples;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;


/**
 * 军事学院在线时长奖励
 * @author Jesse
 *
 */
@HawkConfigManager.XmlResource(file = "xml/college_effect.xml")
public class CollegeEffectCfg extends HawkConfigBase {
	
	/** */
	@Id
	private final int level;
	private final int id;
	
	/** 在线时长: 单位 s*/
	private final String permanentEffect;
	
	private final String onlineCount;
	
	private final String onlineEffect;
	
	private ImmutableList<HawkTuple2<Integer, Double>>levelEffList;
	
	private List<HawkTuple2<Integer, Integer>>onlineCountList;
	private List<ImmutableList<HawkTuple2<Integer, Double>>>onlineCountEffList;
	
	
	public CollegeEffectCfg() {
		id =0;
		level = 0;
		permanentEffect = "";
		onlineEffect = "";
		onlineCount = "";
	}
	
	
	@Override
	protected boolean assemble() {
		{	
			List<String> onlines = Splitter.on(",").omitEmptyStrings().splitToList(onlineCount);
			List<String> onlienEffs = Splitter.on("|").omitEmptyStrings().splitToList(onlineEffect);
			List<HawkTuple2<Integer, Integer>> onlineCountLimitTemp = new ArrayList<>();
			List<ImmutableList<HawkTuple2<Integer, Double>>> onlineCountEffListTemp = new ArrayList<>();
			if(onlines.size() != onlienEffs.size()){
				return false;
			}
			for(int i=0;i<onlines.size();i++){
				String onlineStr = onlines.get(i);
				String effStr = onlienEffs.get(i);
				String[] limit = onlineStr.split("_");
				String[] effArr = effStr.split(",");
				int min = Integer.parseInt(limit[0]);
				int max = Integer.parseInt(limit[1]);
				List<HawkTuple2<Integer, Double>> efflist = new ArrayList<>(effArr.length);
				for(String eff : effArr){
					String[] eArr = Splitter.on("_").omitEmptyStrings().splitToList(eff).toArray(new String[2]);
					efflist.add(HawkTuples.tuple(NumberUtils.toInt(eArr[0]), NumberUtils.toDouble(eArr[1])));
				}
				onlineCountLimitTemp.add(HawkTuples.tuple(min, max));
				ImmutableList<HawkTuple2<Integer, Double>> tableList =  ImmutableList.copyOf(efflist);
				onlineCountEffListTemp.add(tableList);
				
			}
			this.onlineCountList = onlineCountLimitTemp;
			this.onlineCountEffList = onlineCountEffListTemp;
		}
		{	
			List<String> levelEffList = Splitter.on(",").omitEmptyStrings().splitToList(permanentEffect);
			List<HawkTuple2<Integer, Double>> levelEffListTemp = new ArrayList<>();
			for(String effStr : levelEffList){
				String[] eArr = Splitter.on("_").omitEmptyStrings().splitToList(effStr).toArray(new String[2]);
				levelEffListTemp.add(HawkTuples.tuple(NumberUtils.toInt(eArr[0]), NumberUtils.toDouble(eArr[1])));
			}
			this.levelEffList = ImmutableList.copyOf(levelEffListTemp);;
		}
		return true;
	}
	
	@Override
	protected final boolean checkValid() {
		return super.checkValid();
	}
	
	public int getLevel() {
		return level;
	}
	
	public int getId() {
		return id;
	}
	
	public ImmutableList<HawkTuple2<Integer, Double>> getOnlineCountEffListByCount(int onlineCount) {
		for(int i=0;i<this.onlineCountList.size();i++){
			HawkTuple2<Integer, Integer> tuple = this.onlineCountList.get(i);
			if(tuple.first<= onlineCount && onlineCount <= tuple.second){
				return onlineCountEffList.get(i);
			}
		}
		//如果超过了最大，则取最后一个
		int last = this.onlineCountList.size() -1;
		HawkTuple2<Integer, Integer> tupleLast = this.onlineCountList.get(last);
		if(onlineCount > tupleLast.second){
			return onlineCountEffList.get(last);
		}
		return null;
	}
	
	public ImmutableList<HawkTuple2<Integer, Double>> getLevelEffList() {
		return levelEffList;
	}
	
}
