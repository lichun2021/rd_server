package com.hawk.game.guild.manor.building.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.hawk.game.protocol.Common.KeyValuePairInt;
import com.hawk.game.protocol.GuildManor.ManorPlayerInfo;
import com.hawk.game.protocol.GuildManor.PanelState;

/**
 * 伤害数据
 * @author zhenyu.shang
 * @since 2017年7月18日
 */
public class TowerDamageInfo {
	
	private String playerId;
	
	private String name;
	
	private String pfIcon;
	
	private int icon;
	
	private String guildTag;
	
	private int state = 1;
	
	private Map<Integer, Integer> damage = new HashMap<Integer, Integer>();
	
	private Map<Integer, Integer> damageStar = new HashMap<Integer, Integer>();

	public String getPlayerId() {
		return playerId;
	}

	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPfIcon() {
		return pfIcon;
	}

	public void setPfIcon(String pfIcon) {
		this.pfIcon = pfIcon;
	}
	
	public void putDamage(Integer sid, Integer num){
		this.damage.put(sid, num);
	}

	public Map<Integer, Integer> getDamage() {
		return damage;
	}
	
	public Map<Integer, Integer> getDamageStar() {
		return damageStar;
	}

	public void setDamageStar(Map<Integer, Integer> damageStar) {
		this.damageStar = damageStar;
	}

	public void setDamage(Map<Integer, Integer> damage) {
		this.damage = damage;
	}

	public int getIcon() {
		return icon;
	}

	public void setIcon(int icon) {
		this.icon = icon;
	}

	public String getGuildTag() {
		return guildTag;
	}

	public void setGuildTag(String guildTag) {
		this.guildTag = guildTag;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public void mergeDamageInfo(Map<Integer, Integer> map, Map<Integer, Integer> mapStar){
		for (Integer key : map.keySet()) {
			if(this.damage.containsKey(key)){
				this.damage.put(key, map.get(key) + this.damage.get(key));
				if (mapStar.containsKey(key)) {
					this.damageStar.put(key, mapStar.get(key));
				}
			} else {
				this.damage.put(key, map.get(key));
				if (mapStar.containsKey(key)) {
					this.damageStar.put(key, mapStar.get(key));
				}
			}
		}
	}
	
	public ManorPlayerInfo changeMsgInfo(){
		ManorPlayerInfo.Builder builder = ManorPlayerInfo.newBuilder();
		builder.setName(name);
		builder.setPfIcon(pfIcon);
		builder.setPlayerId(playerId);
		builder.setIcon(icon);
		builder.setState(PanelState.valueOf(state));
		builder.setGuildTag(guildTag == null ? "" : guildTag);
		for (Entry<Integer, Integer> entry : damage.entrySet()) {
			KeyValuePairInt.Builder kb = KeyValuePairInt.newBuilder();
			kb.setKey(entry.getKey());
			kb.setVal(entry.getValue());
			if (damageStar != null && damageStar.containsKey(entry.getKey())) {
				kb.setSoldierStar(damageStar.get(entry.getKey()));
			}
			builder.addArmy(kb.build());
		}
		return builder.build();
	}
}
