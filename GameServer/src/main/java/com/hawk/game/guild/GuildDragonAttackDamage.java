package com.hawk.game.guild;

import com.hawk.serialize.string.SerializeHelper;

public class GuildDragonAttackDamage implements Comparable<GuildDragonAttackDamage>{
	
	private String playerId;
	private long damage;
	private long time;
	
	
	public GuildDragonAttackDamage() {
		
	}
	
	public GuildDragonAttackDamage(String playerId,long damage,long time) {
		this.playerId = playerId;
		this.damage = damage;
		this.time = time;
	}
	
	
	
	public String serializ(){
		return this.playerId+ SerializeHelper.ATTRIBUTE_SPLIT + 
				String.valueOf(this.damage) + SerializeHelper.ATTRIBUTE_SPLIT +
				String.valueOf(time);
	}
	
	public void unSerializ(String str){
		String[] arr = str.split(SerializeHelper.ATTRIBUTE_SPLIT);
		this.playerId = arr[0];
		this.damage = Long.parseLong(arr[1]);
		this.time = Long.parseLong(arr[2]);
	}
	
	
	public String getPlayerId() {
		return playerId;
	}
	
	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}
	
	public long getDamage() {
		return damage;
	}
	
	public void setDamage(long damage) {
		this.damage = damage;
	}
	
	public void setTime(long time) {
		this.time = time;
	}
	
	public long getTime() {
		return time;
	}

	@Override
	public int compareTo(GuildDragonAttackDamage o) {
		if(this.damage != o.damage){
			return Long.compare(o.damage,this.damage);
		}
		if(this.time != o.damage){
			return Long.compare(this.damage,o.damage); 
		}
		return Integer.compare(this.playerId.hashCode(),o.playerId.hashCode());
	}
}
