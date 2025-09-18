package com.hawk.game.lianmengxzq;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.hawk.config.HawkConfigManager;
import org.hawk.net.protocol.HawkProtocol;

import com.google.common.collect.ImmutableMap;
import com.hawk.game.cfgElement.EffectObject;
import com.hawk.game.config.XZQBuffCfg;
import com.hawk.game.global.GlobalData;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.XZQ.PBXZQEffect;
import com.hawk.game.protocol.XZQ.PBXZQEffectSyncResp;
import com.hawk.game.service.GuildService;

public class XZQEffect {
	
	private String guildId;
	private ImmutableMap<Integer,Integer> levelCounts;
	private ImmutableMap<EffType, Integer> xzqEffVal;

	public XZQEffect(String guildId) {
		this.guildId = guildId;
		Map<Integer, Integer> mapLevels = new HashMap<>();
		Map<EffType, Integer> mapEffs = new HashMap<>();
		levelCounts = ImmutableMap.copyOf(mapLevels);
		xzqEffVal = ImmutableMap.copyOf(mapEffs);
	}
	
	public void updateEffect(Map<Integer,Integer> map,boolean push){
		boolean change = this.effChange(this.levelCounts, map);
		if(!change){
			return;
		}
		Map<EffType, Integer> effVal = new HashMap<>();
		for(int level : map.keySet()){
			int count = map.get(level);
			XZQBuffCfg cfg = this.getXZQBuffCfg(level, count);
			if(cfg !=null){
				this.mergeEffval(effVal, cfg.getEffectList());
			}
		}
		//作用号集合
		Set<EffType> allEff = new HashSet<>();
		allEff.addAll(effVal.keySet());
		allEff.addAll(xzqEffVal.keySet());
		//重置
		levelCounts = ImmutableMap.copyOf(map);
		xzqEffVal = ImmutableMap.copyOf(effVal);
		if(push){
			//推送
			EffType[] arr = allEff.toArray(new EffType[allEff.size()]);
			List<PBXZQEffect> elist = this.genXZQEffValListBuilder();
			PBXZQEffectSyncResp.Builder builder = PBXZQEffectSyncResp.newBuilder();
			builder.addAllEffcts(elist);
			for (String playerId : GuildService.getInstance().getGuildMembers(this.guildId)) {
				Player player = GlobalData.getInstance().getActivePlayer(playerId);
				if (player != null && player.isActiveOnline()) {
					//推送作用号改变
					player.getPush().syncPlayerEffect(arr);
					//同步小站区作用号
					player.sendProtocol(HawkProtocol.valueOf(HP.code.XZQ_EFFECT_SYNC_S_VALUE, builder));
				}
			}
		}
		
		
	}
	
	public XZQBuffCfg getXZQBuffCfg(int level,int count){
		for(int i=count; i >0; i--){
			XZQBuffCfg cfg = HawkConfigManager.getInstance().getCombineConfig(XZQBuffCfg.class, level,i);
			if(cfg != null){
				return cfg;
			}
		}
		return null;
	}
	
	public void onPlayerSync(Player player){
		List<PBXZQEffect> elist = this.genXZQEffValListBuilder();
		PBXZQEffectSyncResp.Builder builder = PBXZQEffectSyncResp.newBuilder();
		builder.addAllEffcts(elist);
		player.sendProtocol(HawkProtocol.valueOf(HP.code.XZQ_EFFECT_SYNC_S_VALUE,builder));
	}
	
	
	public void onPlayerJoinGuildSync(Player player){
		Set<EffType> set = new HashSet<>();
		set.addAll(this.xzqEffVal.keySet());
		EffType[] arr = set.toArray(new EffType[set.size()]);
		
		List<PBXZQEffect> elist = this.genXZQEffValListBuilder();
		PBXZQEffectSyncResp.Builder builder = PBXZQEffectSyncResp.newBuilder();
		builder.addAllEffcts(elist);
		
		player.getPush().syncPlayerEffect(arr);
		player.sendProtocol(HawkProtocol.valueOf(HP.code.XZQ_EFFECT_SYNC_S_VALUE,builder));
	}
	
	
	public void onPlayerQuitGuildSync(Player player){
		Set<EffType> set = new HashSet<>();
		set.addAll(this.xzqEffVal.keySet());
		EffType[] arr = set.toArray(new EffType[set.size()]);
		player.getPush().syncPlayerEffect(arr);
		player.sendProtocol(HawkProtocol.valueOf(HP.code.XZQ_EFFECT_SYNC_S_VALUE,
					PBXZQEffectSyncResp.newBuilder()));
	}
	
	
	private boolean effChange(Map<Integer,Integer> map1,Map<Integer,Integer> map2){
		if(map1.size() != map2.size()){
			return true;
		}
		for(int key : map1.keySet()){
			int val1 = map1.get(key);
			int val2 = map2.getOrDefault(key, 0);
			if(val1!= val2){
				return true;
			}
		}
		return false;
	}

	
	private void mergeEffval(Map<EffType, Integer> effVal, List<EffectObject> effValList) {
		for (EffectObject effobj : effValList) {
			EffType type = EffType.valueOf(effobj.getEffectType());
			if (type == null) {
				continue;
			}
			effVal.merge(type, effobj.getEffectValue(), (v1, v2) -> v1 + v2);
		}
	}
	

	
	
	
	public String getGuildId() {
		return guildId;
	}

	
	public int getEffectVal(int effType){
		EffType type = EffType.valueOf(effType);
		return this.xzqEffVal.getOrDefault(type, 0);
	}
	
	public Map<EffType,Integer> getEffects(){
		 Map<EffType,Integer> map = new HashMap<>();
		 map.putAll(this.xzqEffVal);
		 return map;
	}
	
	public List<PBXZQEffect> genXZQEffValListBuilder(){
		List<PBXZQEffect> list = new ArrayList<>();
		for(Entry<EffType, Integer> entry : this.xzqEffVal.entrySet()){
			PBXZQEffect.Builder ebuilder = PBXZQEffect.newBuilder();
			ebuilder.setEffectId(entry.getKey().getNumber());
			ebuilder.setValue(entry.getValue());
			list.add(ebuilder.build());
		}
		return list;
	}
	

}
