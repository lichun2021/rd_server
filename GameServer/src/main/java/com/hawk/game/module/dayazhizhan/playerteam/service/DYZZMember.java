package com.hawk.game.module.dayazhizhan.playerteam.service;

import java.util.ArrayList;
import java.util.List;

import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.ImmutableList;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.module.dayazhizhan.battleroom.extry.DYZZGamer;
import com.hawk.game.module.dayazhizhan.playerteam.cfg.DYZZHeroCfg;
import com.hawk.game.module.dayazhizhan.playerteam.cfg.DYZZSoldierCfg;
import com.hawk.game.module.dayazhizhan.playerteam.season.DYZZSeasonService;
import com.hawk.game.player.Player;
import com.hawk.game.player.hero.SerializJsonStrAble;
import com.hawk.game.protocol.DYZZWar.PBDYZZSoldier;
import com.hawk.game.protocol.DYZZWar.PBDYZZTeamMember;

public class DYZZMember implements SerializJsonStrAble{
	
	private String playerId;
	
	private String playerName;
	
	private int icon;
	
	private String pfIcon;
	
	private long battlePoint;
	
	private String serverId;
	
	private ImmutableList<Integer> heros;
	
	private ImmutableList<ArmyInfo> armys;
	
	private int seasonScore;
	private int winCount;
	
	public DYZZMember() {
	}
	
	
	public DYZZMember(Player player) {
		this.playerId = player.getId();
		this.playerName = player.getName();
		this.icon = player.getIcon();
		this.pfIcon = player.getPfIcon();
		this.serverId = player.getMainServerId();
		this.seasonScore = DYZZSeasonService.getInstance().getPlayerSeasonScore(player);
		this.winCount = DYZZRedisData.getInstance().getDYZZWincountToday(player.getId());
		List<Integer> heroList = new ArrayList<>();
		List<DYZZHeroCfg> heroCfgs = HawkConfigManager.getInstance().getConfigIterator(DYZZHeroCfg.class).toList();
		for(DYZZHeroCfg cfg : heroCfgs){
			heroList.add(cfg.getHeroData());
		}
		this.heros =  ImmutableList.copyOf(heroList);
		
		List<ArmyInfo> armyList = new ArrayList<>();
		List<DYZZSoldierCfg> soldierCfgs = HawkConfigManager.getInstance().getConfigIterator(DYZZSoldierCfg.class).toList();
		for(DYZZSoldierCfg cfg : soldierCfgs){
			ArmyInfo army = new ArmyInfo(cfg.getSoldierId(), cfg.getCount());
			army.setPlayerId(player.getId());
			armyList.add(army);
		}
		this.armys =  ImmutableList.copyOf(armyList);
	}
	
	
	
	
	
	
	
	public DYZZGamer getDYZZGamer(){
		DYZZGamer gamer = new DYZZGamer();
		gamer.setPlayerId(playerId);
		
		List<Integer> heroList = new ArrayList<>();
		heroList.addAll(this.heros);
		gamer.setFoggyHeros(heroList);
		List<ArmyInfo> armyList = new ArrayList<>();
		for(ArmyInfo army : this.armys){
			armyList.add(army.getCopy());
		}
		gamer.setArmys(armyList);
		
		gamer.setSeasonScore(this.seasonScore);
		gamer.setWinCount(this.winCount);
		
		return gamer;
	}
	
	@Override
	public String serializ() {
		JSONObject obj = new JSONObject();
		obj.put("1", playerId);
		obj.put("2", playerName);
		obj.put("3", icon);
		obj.put("4", pfIcon);
		obj.put("5", serverId);
		obj.put("6", battlePoint);
		
		JSONArray heroArr = new JSONArray();
		for(int hero : this.heros){
			heroArr.add(hero);
		}
		obj.put("7", heroArr.toJSONString());
		JSONArray soldierArr = new JSONArray();
		for(ArmyInfo soldier : this.armys){
			soldierArr.add(soldier.toString());
		}
		obj.put("8", soldierArr.toJSONString());
		obj.put("9", this.seasonScore);
		obj.put("10", this.winCount);
		return obj.toJSONString();
	}

	@Override
	public void mergeFrom(String serialiedStr) {
		JSONObject obj = JSONObject.parseObject(serialiedStr);
		this.playerId = obj.getString("1");
		this.playerName = obj.getString("2");
		this.icon = obj.getIntValue("3");
		this.pfIcon = obj.getString("4");
		this.serverId = obj.getString("5");
		this.battlePoint = obj.getIntValue("6");
		
		String heroStr = obj.getString("7");
		JSONArray heroArr = JSONArray.parseArray(heroStr);
		List<Integer> heroList = new ArrayList<>();
		for(int i=0;i<heroArr.size();i++){
			int heroId = heroArr.getInteger(i);
			heroList.add(heroId);
		}
		this.heros = ImmutableList.copyOf(heroList);
		
		String soldierListStr = obj.getString("8");
		JSONArray soldierArr = JSONArray.parseArray(soldierListStr);
		List<ArmyInfo> soldierList = new ArrayList<>();
		for(int i=0;i<soldierArr.size();i++){
			String soldierStr = soldierArr.getString(i);
			ArmyInfo soldier = new ArmyInfo(soldierStr);
			soldierList.add(soldier);
		}
		this.armys = ImmutableList.copyOf(soldierList);
		this.seasonScore =  obj.getIntValue("9");
		this.winCount =  obj.getIntValue("10");
	}
	
	
	public PBDYZZTeamMember.Builder genDYZZTeamMemberBuilder(){
		PBDYZZTeamMember.Builder builder =  PBDYZZTeamMember.newBuilder();
		builder.setPlayerId(this.playerId);
		builder.setPlayerName(this.playerName);
		builder.setIcon(this.icon);
		if(!HawkOSOperator.isEmptyString(this.pfIcon)){
			builder.setPfIcon(this.pfIcon);
		}
		builder.setBattlePoint(this.battlePoint);
		builder.setServerId(this.serverId);
		builder.addAllHeros(this.heros);
		for(ArmyInfo army : this.armys){
			PBDYZZSoldier.Builder sbuilder = PBDYZZSoldier.newBuilder();
			sbuilder.setSoldierId(army.getArmyId());
			sbuilder.setCount(army.getTotalCount());
			builder.addSoldiers(sbuilder);
		}
		builder.setSeasonScore(this.seasonScore);
		builder.setWinCount(this.winCount);
		return builder;
	}
	
	public void mergeFromDYZZTeamMemberBuilder(PBDYZZTeamMember builder){
		this.setPlayerId(builder.getPlayerId());
		this.setPlayerName(builder.getPlayerName());
		this.setIcon(builder.getIcon());
		if(builder.hasPfIcon()){
			this.setPfIcon(builder.getPfIcon());
		}
		this.setBattlePoint(builder.getBattlePoint());
		this.setServerId(builder.getServerId());
		List<Integer> heroList = new ArrayList<>();
		heroList.addAll(builder.getHerosList());
		this.heros =  ImmutableList.copyOf(heroList);
		
		List<ArmyInfo> armyList = new ArrayList<>();
		for(PBDYZZSoldier soldier : builder.getSoldiersList()){
			ArmyInfo army = new ArmyInfo(soldier.getSoldierId(), soldier.getCount());
			army.setPlayerId(builder.getPlayerId());
			armyList.add(army);
		}
		this.armys =  ImmutableList.copyOf(armyList);
		this.seasonScore = builder.getSeasonScore();
		this.winCount = builder.getWinCount();
	}
	
	
	
	public String getPlayerId() {
		return playerId;
	}

	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}

	public String getPlayerName() {
		return playerName;
	}

	public void setPlayerName(String playerName) {
		this.playerName = playerName;
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
	
	

	
	public long getBattlePoint() {
		return battlePoint;
	}


	public void setBattlePoint(long battlePoint) {
		this.battlePoint = battlePoint;
	}


	public String getServerId() {
		return serverId;
	}


	public void setServerId(String serverId) {
		this.serverId = serverId;
	}
	
	


	public ImmutableList<Integer> getHeros() {
		return heros;
	}


	public void setHeros(ImmutableList<Integer> heros) {
		this.heros = heros;
	}


	public ImmutableList<ArmyInfo> getArmys() {
		return armys;
	}


	public void setArmys(ImmutableList<ArmyInfo> armys) {
		this.armys = armys;
	}
	
	
	public void setSeasonScore(int seasonScore) {
		this.seasonScore = seasonScore;
	}
	
	public int getSeasonScore() {
		return seasonScore;
	}

	
	public void setWinCount(int winCount) {
		this.winCount = winCount;
	}
	
	public int getWinCount() {
		return winCount;
	}

	public static DYZZMember valueOf(Player player){
		DYZZMember member = new DYZZMember(player);
		return member;
	}
	
}
