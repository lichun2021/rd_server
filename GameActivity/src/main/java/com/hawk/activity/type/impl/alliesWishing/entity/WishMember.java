package com.hawk.activity.type.impl.alliesWishing.entity;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.hawk.os.HawkOSOperator;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hawk.game.protocol.Activity.PBWishMember;

public class WishMember{
	
	private String playerId;
	
	private String name;
	
	private int icon;
	
	private String pfIcon;
	
	
	public WishMember() {
		
	}
	
	
	
	public WishMember(String playerId, String name, int icon, String pfIcon) {
		super();
		this.playerId = playerId;
		this.name = name;
		this.icon = icon;
		if(HawkOSOperator.isEmptyString(pfIcon)){
			this.pfIcon = "";
		}else{
			this.pfIcon = pfIcon;
		}
	}


	
	public PBWishMember.Builder genPBWishMember(){
		PBWishMember.Builder builder = PBWishMember.newBuilder();
		builder.setPlayerId(this.playerId);
		builder.setName(this.name);
		builder.setIcon(this.icon);
		if(!HawkOSOperator.isEmptyString(this.pfIcon)){
			builder.setPfIcon(this.pfIcon);
		}
		return builder;
	}

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

	public int getIcon() {
		return icon;
	}

	public void setIcon(int icon) {
		this.icon = icon;
	}

	public String getPfIcon() {
		return pfIcon;
	}

	public void setPfIcon(String pfIcon) {
		this.pfIcon = pfIcon;
	}

	


	public String serializ() {
		JSONObject obj = new JSONObject();
		obj.put("playerId", playerId);
		obj.put("name", name);
		obj.put("icon", icon);
		if(!HawkOSOperator.isEmptyString(pfIcon)){
			obj.put("pfIcon", pfIcon);
		}
		return obj.toJSONString();
	}

	public void mergeFrom(String serialiedStr) {
		JSONObject obj = JSON.parseObject(serialiedStr);
		this.playerId = obj.getString("playerId");
		this.name = obj.getString("name");
		this.icon = obj.getIntValue("icon");
		this.pfIcon = obj.getString("pfIcon");
	}
	
	
	public static String serializList(List<WishMember> list){
		JSONArray memberArr = new JSONArray();
		for(WishMember member : list){
			memberArr.add(member.serializ());
		}
		return memberArr.toString();
	}
	
	public static List<WishMember> mergeFromList(String str){
		List<WishMember> list = new CopyOnWriteArrayList<WishMember>();
		if(HawkOSOperator.isEmptyString(str)){
			return list;
		}
		JSONArray memberArr = JSONArray.parseArray(str);
		for(int i=0;i<memberArr.size();i++){
			String memberStr = memberArr.getString(i);
			WishMember member = new WishMember();
			member.mergeFrom(memberStr);
			list.add(member);
		}
		return list;
	}

}
