package com.hawk.game.battle;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.Map.Entry;

import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.tuple.HawkTuple2;
import com.hawk.game.battle.battleIncome.IBattleIncome;
import com.hawk.game.battle.effect.BattleConst.BattleType;
import com.hawk.game.config.BattleSoldierCfg;
import com.hawk.game.crossproxy.CrossService;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Rank.RankInfo;
import com.hawk.game.protocol.Rank.RankType;
import com.hawk.game.protocol.World.PresetMarchManhattan;
import com.hawk.game.rank.RankService;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.LogUtil;
import com.hawk.gamelog.GameLog;
import com.hawk.gamelog.LogParam;
import com.hawk.log.LogConst.LogInfoType;

public class BattleLogHelper {
	
	// 战斗发起方玩家
	private Player player;
	
	// 是否是PVP战斗
	private boolean isPVP;
	
	// 战斗类型
	private int battleType;
	
	// 进攻方是否胜利了
	private boolean isAtkWin;
	
	// 集结战斗流水号
	private String battleflowId = "0";
	
	// 是否为集结队长
	private boolean isLeader;
	
	// 进攻方区ID
	private String atkZoneAreaID;
	// 进攻方openid
	private String atkOpenid;
	// 进攻方玩家等级
	private int atkLevel;
	// 进攻方玩家vip等级
	private int atkVipLevel;
	// 进攻方玩家主城等级
	private int atkCityLevel;
	// 进攻方玩家战力
	private long atkPower;
		
	// 进攻方队长id
	private String atkId;
	
	// 进攻方队长名称
	private String atkName;
	// 进攻方所在联盟ID
	private String atkGuildId;
	
	// 进攻方部队信息
	private String atkArmyInfo;
	
	// 进攻方部队总数
	private int atkTotalCnt;
	
	// 进攻方部队击杀数
	private int atkKillCnt;
	
	// 进攻方部队损失数
	private int atkLoseCnt;
	
	// 进攻方伤兵伤兵数
	private int atkWoundedCnt;
	// 进攻方死兵数
	private int atkDeadCnt;
	
	// 防守方区ID
	private String defZoneAreaID;
	// 防守方openid
	private String defOpenid;
	// 防守方玩家等级
	private int defLevel;
	// 防守方玩家vip等级
	private int defVipLevel;
	// 防守方玩家主城等级
	private int defCityLevel;
	// 防守方玩家战力
	private long defPower;
	
	// 防御方部队队长id
	private String defId;
	
	// 防御方部队队长id
	private String defName;
	// 防御方所在联盟ID
	private String defGuildId;
	
	// 防御方部队信息
	private String defArmyInfo;
	
	// 防御方部队总数
	private int defTotalCnt;
	
	// 防御方部队击杀数
	private int defKillCnt;
	
	// 防御方部队损失数
	private int defLoseCnt;
	
	// 防御方伤兵伤兵数
	private int defWoundedCnt;
	// 防御方死兵数
	private int defDeadCnt;
	
	//兵力总战力
	private long defArmyTotalPower;
	
	private IBattleIncome battleIncome;
	
	private BattleOutcome battleOutcome;
	
	public BattleLogHelper(IBattleIncome battleIncome ,BattleOutcome battleOutcome, boolean atkWin) {
		this(battleIncome, battleOutcome);
		this.isAtkWin = atkWin;
	}
	
	public BattleLogHelper(IBattleIncome battleIncome, BattleOutcome battleOutcome) {
		this.battleIncome = battleIncome;
		this.battleOutcome = battleOutcome;
		this.isAtkWin = battleOutcome.isAtkWin();
	}
	
	/**
	 * 记录战斗日志
	 */
	public void logBattleFlow() {
		try {
			Battle battle = battleIncome.getBattle();
			Player atkPlayer = battleIncome.getAtkPlayers().get(0);
			Player defPlayer = battleIncome.getDefPlayers().get(0);
			this.battleType = battle.getType().intVal();
			this.player = atkPlayer;
			this.isPVP = true;
			
			// 设置防守方信息
			setDefenderInfo(defPlayer);
			
			Map<String, List<ArmyInfo>> battleArmyMapAtk = battleOutcome.getBattleArmyMapAtk();
			
			// 尤里复仇战斗,战斗发起方玩家为防守方队长
			if(this.battleType == BattleType.YURI_YURIREVENGE.intVal() 
					|| this.battleType == BattleType.GHOST_MARCH.intVal()
					|| this.battleType == BattleType.SPACE_MECHA_PVE.intVal()){
				this.player = defPlayer;
				this.isLeader = false;
				setAttackerInfo(atkPlayer, battleArmyMapAtk.get(atkPlayer.getId()));
				LogUtil.logBattleFlow(defPlayer, this);
			} else {
				// 集结战斗流水号设置			
				if (battleArmyMapAtk.size() > 1) {
					battleflowId = HawkTime.getMillisecond() + ":" + atkPlayer.getId();
				}
				
				for (Entry<String, List<ArmyInfo>> entry : battleArmyMapAtk.entrySet()) {
					try {
						String playerId = entry.getKey();
						this.isLeader = playerId.equals(atkPlayer.getId());
						Player member = battleIncome.getPlayer(playerId);
						setAttackerInfo(member, entry.getValue());
						LogUtil.logBattleFlow(member, this);
					} catch (Exception e) {
						HawkException.catchException(e);
					}
				}
				
				//集结战斗打点记录
				if (battleArmyMapAtk.size() > 1) {
					try {
						logMassBattleFlow(atkPlayer);
					} catch (Exception e) {
						HawkException.catchException(e);
					}
				}
				
				//机甲 战斗日志
				for(Player player :  battleIncome.getAtkPlayers() ){
					int mechaId = battleIncome.getAtkPlayerSuperSoldier(player.getId());
					LogUtil.logMechaMarch(player, mechaId, this.battleType);
				}
				
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 集结战斗打点
	 */
	private void logMassBattleFlow(Player player) {
		//- 进攻方玩家中，去兵战力排行榜的最高名次 done
		//- 防守方玩家中，去兵战力排行榜的最高名次 done
		//- 双方参战玩家人数 done
		//- 双方部队总战力 done
		//- 联盟战力排行     done
		Map<String, List<ArmyInfo>> battleArmyMapAtk = battleOutcome.getBattleArmyMapAtk();
		Map<String, List<ArmyInfo>> battleArmyMapDef = battleOutcome.getBattleArmyMapDef();
		HawkTuple2<String, String> atkTuple = calcArmyInfo(battleArmyMapAtk);
		String atkArmyInfo = atkTuple.first;
		String[] infos = atkTuple.second.split("_");
		int atkTotalCnt = Integer.parseInt(infos[0]); 
		int atkKillCnt = Integer.parseInt(infos[1]);  
		int atkWoundedCnt = Integer.parseInt(infos[2]);
		int atkDeadCnt = Integer.parseInt(infos[3]);   
		long atkArmyTotalPower = Long.parseLong(infos[4]);
		int atkLoseCnt = atkWoundedCnt + atkDeadCnt;
		
		String atkId = player.getId();
		LogParam logParam = LogUtil.getNonPersonalLogParam(LogInfoType.mass_battle_flow);
		logParam.put("battleType", battleType)
		.put("pvp", isPVP ? 1 : 0)
		.put("atkWin", isAtkWin ? 1 : 0)
		.put("battleflowId", battleflowId)
		.put("atkLeaderServerId", player.getData().getPlayerEntity().getServerId())
		.put("atkLeaderOpenid", player.getOpenId())
		.put("atkLeaderPower", player.getPower())
		.put("atkLeaderId", player.getId())
		.put("atkLeaderName", player.getName())
		.put("atkArmyInfo", atkArmyInfo)
		.put("atkTotalCnt", atkTotalCnt)
		.put("atkKillCnt", atkKillCnt)
		.put("atkLoseCnt", atkLoseCnt)
		.put("atkWoundedCnt", atkWoundedCnt)
		.put("atkDeadCnt", atkDeadCnt)
		.put("atkGuildId", HawkOSOperator.isEmptyString(atkGuildId) ? "NULL" : atkGuildId)
		.put("atkCross", CrossService.getInstance().isCrossPlayer(atkId) ? 1 : 0)
		.put("atkPlayerCnt", battleArmyMapAtk.size())
		.put("atkArmyTotalPower", atkArmyTotalPower)

		.put("defLeaderServerId", defZoneAreaID)
		.put("defLeaderOpenid", defOpenid)
		.put("defLeaderPower", defPower)
		.put("defLeaderId", defId)
		.put("defLeaderName", defName)
		.put("defArmyInfo", defArmyInfo)
		.put("defTotalCnt", defTotalCnt)
		.put("defKillCnt", defKillCnt)
		.put("defLoseCnt", defLoseCnt) 
		.put("defWoundedCnt", defWoundedCnt)
		.put("defDeadCnt", defDeadCnt)
	    .put("defGuildId", HawkOSOperator.isEmptyString(defGuildId) ? "NULL" : defGuildId)
	    .put("defCross", CrossService.getInstance().isCrossPlayer(defId) ? 1 : 0)
	    .put("defPlayerCnt", battleArmyMapDef.size())
	    .put("defArmyTotalPower", defArmyTotalPower);
		
		RankInfo atkRankInfo = HawkOSOperator.isEmptyString(atkGuildId) ? null : RankService.getInstance().getRankInfo(RankType.ALLIANCE_FIGHT_KEY, atkGuildId);
		RankInfo defRankInfo = HawkOSOperator.isEmptyString(defGuildId) ? null : RankService.getInstance().getRankInfo(RankType.ALLIANCE_FIGHT_KEY, defGuildId);
		logParam.put("atkGuildRank", atkRankInfo == null ? -1 : atkRankInfo.getRank());
		logParam.put("defGuildRank", defRankInfo == null ? -1 : defRankInfo.getRank());
		
		int atkHighRank = Integer.MAX_VALUE - 1;
		String aktHighRankPlayer = "";
		for (Entry<String, List<ArmyInfo>> entry : battleArmyMapAtk.entrySet()) {
			String playerId = entry.getKey();
			RankInfo rankInfo = RankService.getInstance().getRankInfo(RankType.PLAYER_NOARMY_POWER_RANK, playerId);
			if (rankInfo != null && rankInfo.getRank() < atkHighRank) {
				atkHighRank = rankInfo.getRank();
				aktHighRankPlayer = playerId;
			}
		}
		
		int defHighRank = Integer.MAX_VALUE - 1;
		String defHighRankPlayer = "";
		for (Entry<String, List<ArmyInfo>> entry : battleArmyMapDef.entrySet()) {
			String playerId = entry.getKey();
			RankInfo rankInfo = RankService.getInstance().getRankInfo(RankType.PLAYER_NOARMY_POWER_RANK, playerId);
			if (rankInfo != null && rankInfo.getRank() < defHighRank) {
				defHighRank = rankInfo.getRank();
				defHighRankPlayer = playerId;
			}
		}
	
		logParam.put("atkHighRank", atkHighRank);
		logParam.put("atkHighRankPlayer", aktHighRankPlayer);
		logParam.put("defHighRank", defHighRank);
		logParam.put("defHighRankPlayer", defHighRankPlayer);
		
	    int[] p = GameUtil.splitXAndY(battleIncome.getBattle().getPointId());
	    logParam.put("worldPoint", Arrays.toString(p));
	    
		logParam.put("dungeon", battleIncome.getDungeon()); //副本名 默认""
		logParam.put("dungeonId", battleIncome.getDungeonId()); // 战场id
		logParam.put("isLeaguaWar", battleIncome.getIsLeaguaWar()); //是否是联赛
		logParam.put("season", battleIncome.getSeason()); // 联赛赛季
		
		GameLog.getInstance().info(logParam);
	}
	
	/**
	 * 设置进攻方信息
	 * 
	 * @param atkPlayer
	 */
	private void setAttackerInfo(Player atkPlayer, List<ArmyInfo> armyList) {
		if (!GameUtil.isNpcPlayer(atkPlayer.getId())) {
			this.atkName = atkPlayer.getNameEncoded();
			this.atkId = atkPlayer.getId();
			this.atkZoneAreaID = atkPlayer.getServerId();
			this.atkOpenid = atkPlayer.getOpenId();
			this.atkLevel = atkPlayer.getLevel();
			this.atkVipLevel = atkPlayer.getVipLevel();
			this.atkCityLevel = atkPlayer.getCityLevel();
			this.atkPower = atkPlayer.getPower();
			this.atkGuildId = atkPlayer.getGuildId();
		} else {
			this.isPVP = false;
			this.atkName = "NULL";
			this.atkId = atkPlayer.getId();
			this.atkZoneAreaID = "0";
			this.atkOpenid = "NULL";
			this.atkLevel = 0;
			this.atkVipLevel = 0;
			this.atkCityLevel = 0;
			this.atkPower = 0;
		}
		
		this.atkTotalCnt = 0;
		this.atkKillCnt = 0;
		this.atkLoseCnt = 0;
		this.atkWoundedCnt = 0;
		this.atkDeadCnt = 0;
		StringBuilder sb = new StringBuilder();
		for(ArmyInfo army : armyList){
			int totalCnt = army.getTotalCount();
			int killCnt = army.getKillCount();
			int loseCnt = army.getDeadCount() + army.getWoundedCount();
			this.atkTotalCnt += totalCnt;
			this.atkKillCnt += killCnt;
			this.atkLoseCnt += loseCnt;
			this.atkWoundedCnt += army.getWoundedCount();
			this.atkDeadCnt += army.getDeadCount();
			sb.append(",").append(army.getArmyId()).append("_").append(totalCnt).append("_").append(killCnt).append("_").append(loseCnt);
		}
		
		if(sb.length() > 0) {
			sb.replace(0, 1, "");
		}
		
		this.atkArmyInfo = sb.toString();
	}
	
	/**
	 * 设置防守方信息
	 * 
	 * @param defPlayer
	 */
	private void setDefenderInfo(Player defPlayer) {
		if (!GameUtil.isNpcPlayer(defPlayer.getId())) {
			this.defName = defPlayer.getNameEncoded();
			this.defId = defPlayer.getId();
			this.defZoneAreaID = defPlayer.getServerId();
			this.defOpenid = defPlayer.getOpenId();
			this.defLevel = defPlayer.getLevel();
			this.defVipLevel = defPlayer.getVipLevel();
			this.defCityLevel = defPlayer.getCityLevel();
			this.defPower = defPlayer.getPower();
			this.defGuildId = defPlayer.getGuildId();
		} else {
			this.isPVP = false;
			this.defName = "NULL";
			this.defId = defPlayer.getId();
			this.defZoneAreaID = "0";
			this.defOpenid = "NULL";
			this.defLevel = 0;
			this.defVipLevel = 0;
			this.defCityLevel = 0;
			this.defPower = 0;
		}
		
		// 防御方部队信息
		Map<String, List<ArmyInfo>> battleArmyMapDef = battleOutcome.getBattleArmyMapDef();
		HawkTuple2<String, String> defTuple = calcArmyInfo(battleArmyMapDef);
		this.defArmyInfo = defTuple.first;
		String[] infos = defTuple.second.split("_");
		this.defTotalCnt = Integer.parseInt(infos[0]);  
		this.defKillCnt = Integer.parseInt(infos[1]);   
		this.defWoundedCnt = Integer.parseInt(infos[2]);
		this.defDeadCnt = Integer.parseInt(infos[3]);
		this.defArmyTotalPower = Long.parseLong(infos[4]);
		this.defLoseCnt = defWoundedCnt + defDeadCnt;
	}

	/**
	 * 统计部队信息
	 * 
	 * @param battleArmyMapAtk
	 * @return
	 */
	private HawkTuple2<String, String> calcArmyInfo(Map<String, List<ArmyInfo>> battleArmyMapAtk) {
		Map<Integer, ArmyInfo> atkArmyMap = new HashMap<>();
		for(List<ArmyInfo> armyList : battleArmyMapAtk.values()){
			for(ArmyInfo army : armyList){
				if(!atkArmyMap.containsKey(army.getArmyId())){
					atkArmyMap.put(army.getArmyId(), army.getCopy());
				}
				else{
					ArmyInfo armyInfo = atkArmyMap.get(army.getArmyId());
					armyInfo.setTotalCount(armyInfo.getTotalCount() + army.getTotalCount());
					armyInfo.setKillCount(armyInfo.getKillCount() + army.getKillCount());
					armyInfo.setDeadCount(armyInfo.getDeadCount() + army.getDeadCount());
					armyInfo.setWoundedCount(armyInfo.getWoundedCount() + army.getWoundedCount());
				}
			}
		}
		
		int atkTotalCount = 0;
		int atkKillCount = 0;
		int atkWoundedCnt = 0;
		int atkDeadCnt = 0;
		long armyBattlePoint = 0;
		StringBuilder sb = new StringBuilder();
		for(ArmyInfo army : atkArmyMap.values()){
			int totalCnt = army.getTotalCount();
			int killCnt = army.getKillCount();
			int loseCnt = army.getDeadCount() + army.getWoundedCount();
			sb.append(",").append(army.getArmyId()).append("_").append(totalCnt).append("_").append(killCnt).append("_").append(loseCnt);
			atkTotalCount += totalCnt;
			atkKillCount += killCnt;
			atkWoundedCnt += army.getWoundedCount();
			atkDeadCnt += army.getDeadCount();
			BattleSoldierCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, army.getArmyId());
			if (cfg != null) {
				armyBattlePoint += (cfg.getPower() * totalCnt);
			}
		}
		
		if(sb.length() > 0){
			sb.replace(0, 1, "");
		}
		
		StringJoiner sj = new StringJoiner("_");
		sj.add(String.valueOf(atkTotalCount));
		sj.add(String.valueOf(atkKillCount));
		sj.add(String.valueOf(atkWoundedCnt));
		sj.add(String.valueOf(atkDeadCnt));
		sj.add(String.valueOf(armyBattlePoint));
		HawkTuple2<String, String> atkTuple = new HawkTuple2<String, String>(sb.toString(), sj.toString());
		return atkTuple;
	}

	
	public Player getPlayer() {
		return player;
	}


	/**
	 * 构建战斗日志参数集合
	 * @return
	 */
	public void genLogParam(LogParam logParam) {
		logParam.put("battleType", battleType)
			.put("pvp", isPVP ? 1 : 0)
			.put("atkWin", isAtkWin ? 1 : 0)
			.put("battleflowId", battleflowId)
			.put("captain", isLeader ? 1 : 0)
	
			.put("atkZoneAreaID", atkZoneAreaID)
			.put("atkOpenid", atkOpenid)
			.put("atkLevel", atkLevel)
			.put("atkVipLevel", atkVipLevel)
			.put("atkCityLevel", atkCityLevel)
			.put("atkPower", atkPower)
			.put("atkId", atkId)
			.put("atkName", atkName)
			.put("atkArmyInfo", atkArmyInfo)
			.put("atkTotalCnt", atkTotalCnt)
			.put("atkKillCnt", atkKillCnt)
			.put("atkLoseCnt", atkLoseCnt)
			.put("atkWoundedCnt", atkWoundedCnt)
			.put("atkDeadCnt", atkDeadCnt)
			.put("atkGuildId", HawkOSOperator.isEmptyString(atkGuildId) ? "NULL" : atkGuildId)
			.put("atkCross", CrossService.getInstance().isCrossPlayer(atkId) ? 1 : 0)
	
			.put("defZoneAreaID", defZoneAreaID)
			.put("defOpenid", defOpenid)
			.put("defLevel", defLevel)
			.put("defVipLevel", defVipLevel)
			.put("defCityLevel", defCityLevel)
			.put("defPower", defPower)
			.put("defId", defId)
			.put("defName", defName)
			.put("defArmyInfo", defArmyInfo)
			.put("defTotalCnt", defTotalCnt)
			.put("defKillCnt", defKillCnt)
			.put("defLoseCnt", defLoseCnt) 
			.put("defWoundedCnt", defWoundedCnt)
			.put("defDeadCnt", defDeadCnt)
		    .put("defGuildId", HawkOSOperator.isEmptyString(defGuildId) ? "NULL" : defGuildId)
		    .put("defCross", CrossService.getInstance().isCrossPlayer(defId) ? 1 : 0);
		
			List<Integer> atkHeroList = battleIncome.getAtkPlayerHeros(atkId);
			StringJoiner atkHero = new StringJoiner(",");
			for (Integer hero : atkHeroList) {
				atkHero.add(String.valueOf(hero));
			}
			
			List<Integer> defHeroList = battleIncome.getDefPlayerHeros(defId);
			StringJoiner defHero = new StringJoiner(",");
			for (Integer hero : defHeroList) {
				defHero.add(String.valueOf(hero));
			}
			
		    logParam.put("atkHero", atkHero.toString())
		    .put("defHero", defHero.toString());
		    logParam.put("atkSupSoldier", battleIncome.getAtkPlayerSuperSoldier(atkId));
		    logParam.put("defSupSoldier", battleIncome.getDefPlayerSuperSoldier(defId));
		    int[] p = GameUtil.splitXAndY(battleIncome.getBattle().getPointId());
		    logParam.put("battlePoint", Arrays.toString(p));
		    
			logParam.put("dungeon", battleIncome.getDungeon()); //副本名 默认""
			logParam.put("dungeonId", battleIncome.getDungeonId()); // 战场id
			logParam.put("isLeaguaWar", battleIncome.getIsLeaguaWar()); //是否是联赛
			logParam.put("season", battleIncome.getSeason()); // 联赛赛季
			
			//战力排行、去兵战力排行、联盟战力排行、超武id
			RankInfo atkRankInfo = HawkOSOperator.isEmptyString(atkGuildId) ? null : RankService.getInstance().getRankInfo(RankType.ALLIANCE_FIGHT_KEY, atkGuildId);
			RankInfo defRankInfo = HawkOSOperator.isEmptyString(defGuildId) ? null : RankService.getInstance().getRankInfo(RankType.ALLIANCE_FIGHT_KEY, defGuildId);
			logParam.put("atkGuildRank", atkRankInfo == null ? -1 : atkRankInfo.getRank());
			logParam.put("defGuildRank", defRankInfo == null ? -1 : defRankInfo.getRank());
			
			atkRankInfo = RankService.getInstance().getRankInfo(RankType.PLAYER_NOARMY_POWER_RANK, atkId);
			defRankInfo = RankService.getInstance().getRankInfo(RankType.PLAYER_NOARMY_POWER_RANK, defId);
			logParam.put("atkNoarmyPowerRank", atkRankInfo == null ? -1 : atkRankInfo.getRank());
			logParam.put("defNoarmyPowerRank", defRankInfo == null ? -1 : defRankInfo.getRank());
			
			atkRankInfo = RankService.getInstance().getRankInfo(RankType.PLAYER_FIGHT_RANK, atkId);
			defRankInfo = RankService.getInstance().getRankInfo(RankType.PLAYER_FIGHT_RANK, defId);
			logParam.put("atkPowerRank", atkRankInfo == null ? -1 : atkRankInfo.getRank());
			logParam.put("defPowerRank", defRankInfo == null ? -1 : defRankInfo.getRank());
			
			PresetMarchManhattan atkManhattan = battleIncome.getAtkPlayerManhattan(atkId);
			PresetMarchManhattan defManhattan = battleIncome.getAtkPlayerManhattan(defId);
			logParam.put("atkManhattan", atkManhattan == null ? -1 : atkManhattan.getManhattanAtkSwId());
			logParam.put("defManhattan", defManhattan == null ? -1 : defManhattan.getManhattanDefSwId());
			logParam.put("atkforceFieldMax", battleIncome.getBattle().getAttacker().getForceFieldMax());
			logParam.put("atkforceField", battleIncome.getBattle().getAttacker().getForceField());
			logParam.put("defforceFieldMax", battleIncome.getBattle().getDefencer().getForceFieldMax());
			logParam.put("defforceField", battleIncome.getBattle().getDefencer().getForceField());
	}
	
}
