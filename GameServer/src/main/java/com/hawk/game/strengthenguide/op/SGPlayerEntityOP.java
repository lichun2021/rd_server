package com.hawk.game.strengthenguide.op;

import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.os.HawkTime;
import org.hawk.task.HawkTaskManager;
import org.hawk.thread.HawkTask;

import com.hawk.game.config.ConstProperty;
import com.hawk.game.config.HeroCfg;
import com.hawk.game.config.PlayerAchieveCfg;
import com.hawk.game.config.PlayerLevelExpCfg;
import com.hawk.game.config.StrengthenGuideConstProperty;
import com.hawk.game.config.StrongerMethodBuildCfg;
import com.hawk.game.config.StrongerMethodOtherCfg;
import com.hawk.game.config.StrongerMethodSoldierCfg;
import com.hawk.game.config.TechnologyCfg;
import com.hawk.game.config.VipCfg;
import com.hawk.game.entity.ArmyEntity;
import com.hawk.game.entity.TechnologyEntity;
import com.hawk.game.item.PlayerAchieveItem;
import com.hawk.game.manager.AssembleDataManager;
import com.hawk.game.player.Player;
import com.hawk.game.player.hero.PlayerHero;
import com.hawk.game.protocol.StrengthenGuide;
import com.hawk.game.protocol.StrengthenGuide.HeroQualityColorType;
import com.hawk.game.protocol.StrengthenGuide.StrengthenGuideInfoRes;
import com.hawk.game.protocol.StrengthenGuide.StrengthenGuideOtherIDType;
import com.hawk.game.protocol.StrengthenGuide.StrengthenGuideType;
import com.hawk.game.protocol.StrengthenGuide.TechClassType;
import com.hawk.game.strengthenguide.StrengthenGuideData;
import com.hawk.game.strengthenguide.StrengthenGuideTypedef.SGuideType;
import com.hawk.game.strengthenguide.entity.SGPlayerEntity;

import org.hawk.tuple.HawkTuple2;

/**
 * * 我要变强主逻辑
 * 
 * @author RickMei
 *
 */

public class SGPlayerEntityOP {

	static ConcurrentHashMap<String, HawkTuple2<Long, StrengthenGuideInfoRes.Builder>> respCache = new ConcurrentHashMap<>();

	public static StrengthenGuideInfoRes.Builder getPlayerStrengthenGuideInfoResp(Player player) {
		HawkTuple2<Long, StrengthenGuideInfoRes.Builder> findResp = respCache.get(player.getId());
		if (null != findResp && findResp.first + HawkTime.MINUTE_MILLI_SECONDS > HawkTime.getMillisecond()) {
			return findResp.second;
		} else {
			StrengthenGuideInfoRes.Builder builder = StrengthenGuideInfoRes.newBuilder();
			SGPlayerEntity entity = StrengthenGuideData.get(player.getId());
			if (null != entity) {

				for (SGuideType sType : SGuideType.values()) {
					StrengthenGuide.StrengthenGuideInfo.Builder infoBuilder = builder.addInfosBuilder();
					infoBuilder.setSType(sType.getSgType());
					int tmpVal = sType.getServerAvg();
					int tmpMyVal = 0;
					//得分为零强制改成操作0的玩家
					if(0 != entity.getScoreByType(sType.getSgType())){
						tmpMyVal = sType.getRateByIndex(entity.getScoreIndexByType(sType.getSgType()));
					}
					infoBuilder.setStageVal(tmpVal);
					infoBuilder.setMyStageVal(tmpMyVal);
				}
			}
			respCache.put(player.getId(),
					new HawkTuple2<Long, StrengthenGuideInfoRes.Builder>(HawkTime.getMillisecond(), builder));
			return builder;
		}
	}

	public static boolean updateWithPlayer(Player player) {
		int threadIdx = player.getXid().getId() % HawkTaskManager.getInstance().getThreadNum();
		HawkTaskManager.getInstance().postTask(new HawkTask(){
			@Override
			public Object run() {
				int buildScore = (int) getPlayerSGBuildScore(player);
				int soldierScore = (int) getPlayerSGSoldierScore(player);
				int commanderScore = (int) getPlayerSGCommanderScore(player);
				int heroScore = (int) getPlayerSGHeroScore(player);
				int scienceScore = (int) getPlayerSGScienceScore(player);
				HawkTaskManager.getInstance().postExtraTask(new HawkTask(){
					@Override
					public Object run() {
						TreeSet<Integer> changeIds = new TreeSet<>();

						SGPlayerEntity playerSGEntity = StrengthenGuideData.get(player.getId());
						if (null == playerSGEntity) {
							playerSGEntity = SGPlayerEntity.valueOf(player.getId());
							StrengthenGuideData.put(player.getId(), playerSGEntity);
						}
						boolean changed = SGuideType.Army.updateScore(playerSGEntity, soldierScore, changeIds);
						changed |= SGuideType.Building.updateScore(playerSGEntity, buildScore, changeIds);
						changed |= SGuideType.Commander.updateScore(playerSGEntity, commanderScore, changeIds);
						changed |= SGuideType.Hero.updateScore(playerSGEntity, heroScore, changeIds);
						changed |= SGuideType.Science.updateScore(playerSGEntity, scienceScore, changeIds);

						if(changed && !changeIds.isEmpty()){
							SGuideType.Total.updateScore(playerSGEntity, playerSGEntity.getTotalNewScore(), changeIds);
						}
						return null;
					}
				}, 0);
				return null;
			}
		}, threadIdx);
		return true;
	}

	public static boolean updateWithType(StrengthenGuideType sgType, Player player) {
		int threadIdx = player.getXid().getId() % HawkTaskManager.getInstance().getThreadNum();
		HawkTaskManager.getInstance().postTask( new HawkTask(){
			@Override
			public Object run() {
				String playerId = player.getId();
				int score = 0;
				if (StrengthenGuideType.Army == sgType) {
					score = (int) getPlayerSGSoldierScore(player);
				} else if (StrengthenGuideType.Building == sgType) {
					score = (int) getPlayerSGBuildScore(player);
				} else if (StrengthenGuideType.Commander == sgType) {
					score = (int) getPlayerSGCommanderScore(player);
				} else if (StrengthenGuideType.Hero == sgType) {
					score = (int) getPlayerSGHeroScore(player);
				} else if (StrengthenGuideType.Science == sgType) {
					score = (int) getPlayerSGScienceScore(player);
				}
				final int finalScore = score;
				HawkTaskManager.getInstance().postExtraTask( new HawkTask(){

					@Override
					public Object run() {
						TreeSet<Integer> changeIds = new TreeSet<>();
						SGPlayerEntity playerSGEntity = StrengthenGuideData.get(playerId);
						if (null == playerSGEntity) {
							playerSGEntity = SGPlayerEntity.valueOf(playerId);
							StrengthenGuideData.put(playerId, playerSGEntity);
						}
						boolean changed = SGuideType.getSGuideType(sgType).updateScore(playerSGEntity, finalScore, changeIds);
						if(changed && !changeIds.isEmpty()){
							SGuideType.Total.updateScore(playerSGEntity, playerSGEntity.getTotalNewScore(), changeIds);
						}
						return null;
					}
				},0);
				return null;
			}
			
		},threadIdx);

		return true;
	}

	static double getPlayerSGBuildScore(Player player) {
		try {
			double buildingScore = 0;
			ConfigIterator<StrongerMethodBuildCfg> cfgIter = HawkConfigManager.getInstance()
					.getConfigIterator(StrongerMethodBuildCfg.class);

			while (cfgIter.hasNext()) {
				StrongerMethodBuildCfg cfg = cfgIter.next();
				if (cfg != null) {
					int buildLevel = player.getData().getBuildingMaxLevel(cfg.getBuildType()); // 城墙等级
					double score = cfg.getPercent() * cfg.getScore(buildLevel);
					buildingScore += score;
				}
			}
			return buildingScore;
		} catch (Exception e) {
			return 0;
		}
	}

	static double getPlayerSGSoldierScore(Player player) {
		try {
			double soldierScore = 0;
			ConfigIterator<StrongerMethodSoldierCfg> cfgIter = HawkConfigManager.getInstance()
					.getConfigIterator(StrongerMethodSoldierCfg.class);
			while (cfgIter.hasNext()) {
				StrongerMethodSoldierCfg cfg = cfgIter.next();
				int[] ids = cfg.getSoldiers();
				double[] scores = cfg.getScores();
				for (int i = 0; i < ids.length; i++) {
					ArmyEntity armyEntity = player.getData().getArmyEntity(ids[i]);
					if (null != armyEntity) {
						soldierScore += armyEntity.getTotal() * scores[i] * cfg.getPercent();
					}
				}
			}
			return soldierScore / StrengthenGuideConstProperty.getInstance().getSoldierMutiple(); // (计算出的士兵分值要
																									// 除以
																									// 10000)
		} catch (Exception e) {
			return 0;
		}
	}

	static double getPlayerSGCommanderScore(Player player) {
		double armsScore = 0, armsClassScore = 0, achieveScore = 0;
		for (StrongerMethodOtherCfg otherCfg : StrongerMethodOtherCfg.getCommander()) {
			if (StrengthenGuideOtherIDType.TalentPoints.getNumber() == otherCfg.getId()) {
				PlayerLevelExpCfg cfg = HawkConfigManager.getInstance().getConfigByKey(PlayerLevelExpCfg.class,
						player.getLevel());
				if (null != cfg) {
					armsScore = cfg.getSkillPoint() * otherCfg.getPercent() * otherCfg.getPerScore();
				}
			} else if (StrengthenGuideOtherIDType.TalentClasses.getNumber() == otherCfg.getId()) {
				int classes = 1;
				if (player.getCityLevel() >= ConstProperty.getInstance().getUnlockTalentLine2NeedCityLevel()) {
					classes++;
				}
				if (player.getCityLevel() >= ConstProperty.getInstance().getUnlockTalentLine3NeedCityLevel()) {
					VipCfg vipCfg = HawkConfigManager.getInstance().getConfigByKey(VipCfg.class, player.getVipLevel());
					if (vipCfg != null && vipCfg.getUnlockTalentLine3() != 0) {
						classes++;
					}
				}
				armsClassScore = classes * otherCfg.getPerScore() * otherCfg.getPercent();
			} else if (StrengthenGuideOtherIDType.TalentAchieve.getNumber() == otherCfg.getId()) {
				int achievePoints = 0;
				for (PlayerAchieveItem item : player.getData().getPlayerAchieveEntity().getMissionItems()) {
					for (int i = 1; i <= item.getState(); i++) {
						// 这里 achieveitem state 表示的意思是已经领取的等级
						PlayerAchieveCfg achieveCfg = AssembleDataManager.getInstance()
								.getPlayerAchieve(item.getCfgId(), i);
						if (achieveCfg == null) {
							continue;
						}
						achievePoints += achieveCfg.getStar();
					}
				}
				achieveScore = achievePoints * otherCfg.getPerScore() * otherCfg.getPercent();
			}
		}
		return armsScore + armsClassScore + achieveScore;
	}

	static double getPlayerSGHeroScore(Player player) {
		int purpleHero = 0, orangeHero = 0, heroLevel = 0, heroStar = 0;
		double purpleHeroScore = 0, orangeHeroScore = 0, heroLevelScore = 0, heroStarScore = 0;

		List<PlayerHero> heros = player.getAllHero();
		for (PlayerHero hero : heros) {
			HeroCfg heroCfg = HawkConfigManager.getInstance().getConfigByKey(HeroCfg.class, hero.getCfgId());
			if (heroCfg != null) {
				if (HeroQualityColorType.Orange.getNumber() == heroCfg.getQualityColor()) {
					orangeHero++;
				} else if (HeroQualityColorType.Purple.getNumber() == heroCfg.getQualityColor()) {
					purpleHero++;
				}
			}
			heroStar += hero.getStar();
			heroLevel += hero.getLevel();
		}

		List<StrongerMethodOtherCfg> sgHeroCfgs = StrongerMethodOtherCfg.getHero();

		for (StrongerMethodOtherCfg cfg : sgHeroCfgs) {
			if (StrengthenGuideOtherIDType.HeroPurpleCount.getNumber() == cfg.getId()) {
				purpleHeroScore = cfg.getPerScore() * cfg.getPercent() * purpleHero;
			} else if (StrengthenGuideOtherIDType.HeroOrangeCount.getNumber() == cfg.getId()) {
				orangeHeroScore = cfg.getPerScore() * cfg.getPercent() * orangeHero;
			} else if (StrengthenGuideOtherIDType.HeroLevelCount.getNumber() == cfg.getId()) {
				heroLevelScore = cfg.getPerScore() * cfg.getPercent() * heroLevel;
			} else if (StrengthenGuideOtherIDType.HeroStarCount.getNumber() == cfg.getId()) {
				heroStarScore = cfg.getPerScore() * cfg.getPercent() * heroStar;
			}
		}

		return purpleHeroScore + orangeHeroScore + heroLevelScore + heroStarScore;
	}

	static double getPlayerSGScienceScore(Player player) {

		int armsScience = 0, developScience = 0, resScience = 0, defenceScience = 0;
		double armsScienceScore = 0, developScienceScore = 0, resScienceScore = 0, defenceScienceScore = 0;

		for (TechnologyEntity tech : player.getData().getTechnologyEntities()) {
			TechnologyCfg techCfg = HawkConfigManager.getInstance().getConfigByKey(TechnologyCfg.class,
					tech.getCfgId());
			if (techCfg != null) {
				if (TechClassType.Arms.getNumber() == techCfg.getTechType()) {
					armsScience += tech.getLevel();
				} else if (TechClassType.Develop.getNumber() == techCfg.getTechType()) {
					developScience += tech.getLevel();
				} else if (TechClassType.Res.getNumber() == techCfg.getTechType()) {
					resScience += tech.getLevel();
				} else if (TechClassType.Defence.getNumber() == techCfg.getTechType()) {
					defenceScience += tech.getLevel();
				}
			}
		}

		for (StrongerMethodOtherCfg cfg : StrongerMethodOtherCfg.getScience()) {
			if (StrengthenGuideOtherIDType.ScienceArms.getNumber() == cfg.getId()) {
				armsScienceScore = armsScience * cfg.getPercent() * cfg.getPerScore();
			} else if (StrengthenGuideOtherIDType.ScienceDevelop.getNumber() == cfg.getId()) {
				developScienceScore = developScience * cfg.getPercent() * cfg.getPerScore();
			} else if (StrengthenGuideOtherIDType.ScienceRes.getNumber() == cfg.getId()) {
				resScienceScore = resScience * cfg.getPercent() * cfg.getPerScore();
			} else if (StrengthenGuideOtherIDType.ScienceDefence.getNumber() == cfg.getId()) {
				defenceScienceScore = defenceScience * cfg.getPercent() * cfg.getPerScore();
			}
		}
		return armsScienceScore + developScienceScore + resScienceScore + defenceScienceScore;
	}
}
