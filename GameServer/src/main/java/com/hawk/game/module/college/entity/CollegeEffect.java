package com.hawk.game.module.college.entity;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.hawk.config.HawkConfigManager;
import org.hawk.tuple.HawkTuple2;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.hawk.game.global.GlobalData;
import com.hawk.game.module.college.cfg.CollegeEffectCfg;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.service.college.CollegeService;

public class CollegeEffect {
	
	private String collegeId;
	/** 学院等级*/
	private int level;
	/** 在线人数*/
	private int memberOnlineCount;
	/** 官职做用号 */
	private ImmutableMap<EffType, Integer> levelEffVal = ImmutableMap.of();
	/** 出征做用号 */
	private ImmutableMap<EffType, Integer> onlineCountEffVal = ImmutableMap.of();
	
	

	public void checkEffUpdate(int level,int onlineCount,boolean push){
		Set<EffType> allEff = new HashSet<>();
		if(this.level != level || this.memberOnlineCount != onlineCount){
			this.level = level;
			allEff.addAll(this.levelEffVal.keySet());
			this.loadLevelEffVal(); 
			allEff.addAll(this.levelEffVal.keySet());
			
			this.memberOnlineCount = onlineCount;
			allEff.addAll(this.onlineCountEffVal.keySet());
			this.loadonlineCountEffVal(); 
			allEff.addAll(this.onlineCountEffVal.keySet());
		}
		if(allEff.size() > 0 && push){
			//推送
			List<String> list = CollegeService.getInstance().getCollegeOnlineMember(this.collegeId);
			for(String member: list){
				Player player = GlobalData.getInstance().getActivePlayer(member);
				if(Objects.nonNull(player)){
					player.getEffect().syncEffect(player, allEff.toArray(new EffType[allEff.size()]));
					CollegeService.getInstance().syncEffectInfo(player);
				}
			}
		}
	}
	
	
	public void syncCollegeEffect(String member){
		Set<EffType> allEff = new HashSet<>();
		allEff.addAll(this.levelEffVal.keySet());
		allEff.addAll(this.onlineCountEffVal.keySet());
		//推送
		Player player = GlobalData.getInstance().getActivePlayer(member);
		if(Objects.nonNull(player)){
			player.getEffect().syncEffect(player, allEff.toArray(new EffType[allEff.size()]));
		}
	}
	
	

	
	public void loadLevelEffVal(){
		CollegeEffectCfg effCfg = HawkConfigManager.getInstance().getConfigByKey(CollegeEffectCfg.class, this.level);
		if(Objects.isNull(effCfg)){
			return;
		}
		ImmutableList<HawkTuple2<Integer, Double>> list =  effCfg.getLevelEffList();
		Map<EffType, Integer> levelEffVal = new HashMap<>();
		for(HawkTuple2<Integer, Double> tuple : list){
			int etype = tuple.first;
			Double evalue = tuple.second;
			EffType effType = EffType.valueOf(etype);
			if(Objects.isNull(effType)){
				continue;
			}
			int val = levelEffVal.getOrDefault(effType,0);
			val += evalue.intValue();
			levelEffVal.put(effType, val);
		}
		this.levelEffVal = ImmutableMap.copyOf(levelEffVal);
	}
	
	
	
	public void loadonlineCountEffVal(){
		CollegeEffectCfg effCfg = HawkConfigManager.getInstance().getConfigByKey(CollegeEffectCfg.class, this.level);
		ImmutableList<HawkTuple2<Integer, Double>> list =  effCfg.getOnlineCountEffListByCount(this.memberOnlineCount);
		Map<EffType, Integer> onlineEffVal = new HashMap<>();
		if(Objects.nonNull(list)){
			for(HawkTuple2<Integer, Double> tuple : list){
				int etype = tuple.first;
				Double evalue = tuple.second;
				EffType effType = EffType.valueOf(etype);
				if(Objects.isNull(effType)){
					continue;
				}
				int val = onlineEffVal.getOrDefault(effType,0);
				val += evalue.intValue();
				onlineEffVal.put(effType, val);
			}
		}
//		List<EffectObject> list2 = CollegeConstCfg.getInstance().getEffectList();
//		for (EffectObject effObj : list2) {
//			EffType effType = EffType.valueOf(effObj.getEffectType());
//			if(Objects.isNull(effType)){
//				continue;
//			}
//			int val = levelEffVal.getOrDefault(effType,0);
//			val += effObj.getEffectValue() * this.memberOnlineCount;
//			onlineEffVal.put(effType, val);
//		}
		onlineCountEffVal = ImmutableMap.copyOf(onlineEffVal);
	}
	
	
	public int getEffValue(EffType type){
		int lval = this.levelEffVal.getOrDefault(type, 0);
		int oval = this.onlineCountEffVal.getOrDefault(type, 0);
		return lval + oval;
	}
	
	
	public String getCollegeId() {
		return collegeId;
	}
	
	public void setCollegeId(String collegeId) {
		this.collegeId = collegeId;
	}
	
	public int getLevel() {
		return level;
	}
	
	public void setLevel(int level) {
		this.level = level;
	}
	
	public ImmutableMap<EffType, Integer> getLevelEffVal() {
		return levelEffVal;
	}
	
	public ImmutableMap<EffType, Integer> getOnlineCountEffVal() {
		return onlineCountEffVal;
	}
	
	public void setLevelEffVal(ImmutableMap<EffType, Integer> levelEffVal) {
		this.levelEffVal = levelEffVal;
	}
	
	public void setOnlineCountEffVal(ImmutableMap<EffType, Integer> onlineCountEffVal) {
		this.onlineCountEffVal = onlineCountEffVal;
	}
	
	public void setMemberOnlineCount(int memberOnlineCount) {
		this.memberOnlineCount = memberOnlineCount;
	}
	
	public int getMemberOnlineCount() {
		return memberOnlineCount;
	}
	
	
}
