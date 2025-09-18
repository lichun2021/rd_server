package com.hawk.game.module.nationMilitary.rank;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hawk.game.global.GlobalData;
import com.hawk.game.module.nationMilitary.entity.NationMilitaryEntity;
import com.hawk.game.player.Player;
import com.hawk.game.player.hero.SerializJsonStrAble;
import com.hawk.game.protocol.Rank.NationMilitaryRankInfo;
import com.hawk.game.util.BuilderUtil;

public class MilitaryRankCollection implements SerializJsonStrAble {
	private Map<String, MilitaryRank> rankMap = new HashMap<>();
	/**
	 * 排序好的榜单
	 */
	private List<NationMilitaryRankInfo> sortedRank;
	
	/** 军衔榜*/
	public NationMilitaryRankInfo buildRankInfo(Player player) {
		MilitaryRank rank = this.getRankByPlayerId(player.getId());

		NationMilitaryRankInfo.Builder rankInfo = genRankInfo(player, rank);
		return rankInfo.build();
	}
	
	public void buildSortedRank() {
		List<NationMilitaryRankInfo> sorankList = new ArrayList<>();
		ArrayList<MilitaryRank> rankAll = this.getAllRank();
		for (MilitaryRank rank : rankAll) {
			Player player = GlobalData.getInstance().makesurePlayer(rank.getPlayerId());
			if (player == null) {
				continue;
			}
//			NationMilitaryRankInfo buil = buildRankInfo(rank,player);
			NationMilitaryRankInfo.Builder rankInfo = genRankInfo(player, rank);
			sorankList.add(rankInfo.build());
		}
		this.sortedRank = sorankList;
		
	}
	
	public NationMilitaryRankInfo.Builder genRankInfo(Player player, MilitaryRank rank) {
		NationMilitaryEntity nationMilitaryEntity = player.getData().getNationMilitaryEntity();
		NationMilitaryRankInfo.Builder rankInfo = NationMilitaryRankInfo.newBuilder();
		if (Objects.nonNull(rank)) {
			rankInfo.setRank(rank.getRank());
			rankInfo.setRankInfoValue(rank.getScore());
			rankInfo.setMilitaryLevel(rank.getCfgId());
		} else {
			rankInfo.setRank(-1);
			rankInfo.setRankInfoValue(nationMilitaryEntity.getNationMilitaryExp());
			rankInfo.setMilitaryLevel(nationMilitaryEntity.getNationMilitarLlevel());
		}
		rankInfo.setPlayerName(player.getName());
		rankInfo.setAllianceIcon(player.getGuildFlag());
		rankInfo.setAllianceName(player.getGuildName());
		rankInfo.setIcon(player.getIcon());
		rankInfo.setPfIcon(player.getPfIcon());
		rankInfo.setVipLevel(player.getVipLevel());
		rankInfo.setCommon(BuilderUtil.genPlayerCommonBuilder(player));
		rankInfo.setPlayerId(player.getId());
		rankInfo.setGuildTag(player.getGuildTag());
		return rankInfo;
	}
	
	@Override
	public String serializ() {
		JSONArray arr = new JSONArray();
		for (MilitaryRank rank : rankMap.values()) {
			JSONObject obj = new JSONObject();
			obj.put("playerId", rank.getPlayerId());
			obj.put("cfgId", rank.getCfgId());
			obj.put("rank", rank.getRank());
			obj.put("score", rank.getScore());
			arr.add(obj);
		}
		return arr.toJSONString();
	}

	@Override
	public void mergeFrom(String serialiedStr) {
		JSONArray arr = JSONArray.parseArray(serialiedStr);
		for (Object item : arr) {
			JSONObject obj = (JSONObject) item;
			MilitaryRank rank = new MilitaryRank();
			rank.setPlayerId(obj.getString("playerId"));
			rank.setCfgId(obj.getIntValue("cfgId"));
			rank.setRank(obj.getIntValue("rank"));
			rank.setScore(obj.getIntValue("score"));
			addRank(rank);
		}

	}
	
	public ArrayList<MilitaryRank> getAllRank(){
		ArrayList<MilitaryRank> result = new ArrayList<>(rankMap.values());
		Collections.sort(result, Comparator.comparingInt(MilitaryRank::getRank));
		return result;
	}

	public void addRank(MilitaryRank rankInfo) {
		rankMap.put(rankInfo.getPlayerId(), rankInfo);

	}

	public MilitaryRank getRankByPlayerId(String id) {
		return rankMap.get(id);
	}

	public List<NationMilitaryRankInfo> getSortedRank() {
		return sortedRank;
	}

}
