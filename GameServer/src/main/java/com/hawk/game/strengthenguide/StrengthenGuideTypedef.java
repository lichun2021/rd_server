package com.hawk.game.strengthenguide;

import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.stream.Collectors;

import com.hawk.game.config.StrengthenGuideConstProperty;
import com.hawk.game.global.LocalRedis;
import com.hawk.game.protocol.StrengthenGuide.StrengthenGuideType;
import com.hawk.game.strengthenguide.entity.SGPlayerEntity;

import org.hawk.tuple.HawkTuple2;

public class StrengthenGuideTypedef {

	public enum SGuideType {
		Building(StrengthenGuideType.Building, "building"),
		Army(StrengthenGuideType.Army, "army"),
		Hero(StrengthenGuideType.Hero, "hero"),
		Science(StrengthenGuideType.Science, "science"),
		Commander(StrengthenGuideType.Commander, "commander"),
		Total(StrengthenGuideType.Total, "total");

		SGuideType(StrengthenGuideType sgType, String fixStr) {
			this.sgType = sgType;
			this.fixStr = fixStr;
		}

		private StrengthenGuideType sgType;

		private String fixStr;

		// 各分段的人数分布图 0 ~ 100 个段
		private int[] scoreDistribution = new int[StrengthenGuideConstProperty.getInstance().getScoreStages()];

		public int getDistribution(int index) {
			return scoreDistribution[index % StrengthenGuideConstProperty.getInstance().getScoreStages()];
		}

		public void setDistribution(int index, int score) {
			scoreDistribution[index % StrengthenGuideConstProperty.getInstance().getScoreStages()] = score;
		}

		public void decDistribution(int index) {
			if (getDistribution(index) > 0) {
				--scoreDistribution[index % StrengthenGuideConstProperty.getInstance().getScoreStages()];
			}
		}

		public void incDistribution(int index) {
			++scoreDistribution[index % StrengthenGuideConstProperty.getInstance().getScoreStages()];
		}

		public static SGuideType getSGuideType(StrengthenGuideType sgType) {
			if (StrengthenGuideType.Building == sgType) {
				return Building;
			} else if (StrengthenGuideType.Army == sgType) {
				return Army;
			} else if (StrengthenGuideType.Hero == sgType) {
				return Hero;
			} else if (StrengthenGuideType.Science == sgType) {
				return Science;
			} else if (StrengthenGuideType.Commander == sgType) {
				return Commander;
			} else {
				return Total;
			}
		}

		public String getFixStr() {
			return fixStr;
		}

		public StrengthenGuideType getSgType() {
			return sgType;
		}

		/**
		 * 更新分数和分布图
		 * 
		 * @param playerEntity
		 * @param score
		 * @param changedIndexSet
		 * @return
		 */
		public boolean updateScore(SGPlayerEntity playerEntity, int score, TreeSet<Integer> changedIndexSet) {

			if (null == playerEntity) {
				return false;
			}
			
			int oldScore = playerEntity.getScoreByType(this.sgType);

			int oldScoreIndex = playerEntity.getScoreIndexByType(this.sgType);

			int newScoreIndex = StrengthenGuideConstProperty.getInstance().getScoreIndex(score);

			if (oldScore != score) {
				// 之前的得分是0不计算变更 直接变更
				if (oldScore == 0) {
					incDistribution(newScoreIndex);

					playerEntity.setScoreIndexByType(this.sgType, newScoreIndex);
					playerEntity.setScoreByType(this.sgType, score);

					changedIndexSet.add(newScoreIndex);

					LocalRedis.getInstance().updateServerStrengthenGuideDistribution(getFixStr(), newScoreIndex,
							getDistribution(newScoreIndex));

					LocalRedis.getInstance().updatePlayerStrengthenGuideScore(playerEntity);
					
					

				} else {
					if (oldScoreIndex != newScoreIndex) {

						if (getDistribution(oldScoreIndex) > 0) {
							decDistribution(oldScoreIndex);
						}
						incDistribution(newScoreIndex);

						playerEntity.setScoreIndexByType(this.sgType, newScoreIndex);
						playerEntity.setScoreByType(this.sgType, score);

						changedIndexSet.add(oldScoreIndex);
						changedIndexSet.add(newScoreIndex);

						LocalRedis.getInstance().updateServerStrengthenGuideDistribution(getFixStr(), oldScoreIndex,
								getDistribution(oldScoreIndex), newScoreIndex, getDistribution(newScoreIndex));

						// 排名变更时才存积分
						LocalRedis.getInstance().updatePlayerStrengthenGuideScore(playerEntity);
						
						
					}
				}

				return true;
			}

			return false;
		}

		/**
		 * 重新计算 总分部 榜单 该分段 的人数
		 * 
		 * @param i
		 * @return
		 */
		public static int reCalculateTotalIndexCount(int i) {
			return (Building.getDistribution(i) + Army.getDistribution(i) + Hero.getDistribution(i)
					+ Science.getDistribution(i) + Commander.getDistribution(i)) / 5;
		}

		/**
		 * 从redis 加载
		 */
		public void loadFromRedis() {
			Map<String, String> redisRetMap = LocalRedis.getInstance()
					.loadServerStrengthenGuideDistribution(getFixStr());
			if (!redisRetMap.isEmpty()) {
				List<HawkTuple2<Integer, Integer>> redisRetList = redisRetMap.entrySet().stream()
						.map(iter -> new HawkTuple2<Integer, Integer>(Integer.valueOf(iter.getKey()),
								Integer.valueOf(iter.getValue())))
						.collect(Collectors.toList());

				for (HawkTuple2<Integer, Integer> pairIter : redisRetList) {
					setDistribution(pairIter.first, pairIter.second);
				}
			}
		}

		// 获取某个分段在全局统计的比值 万分比
		public int getRateByIndex(int index) {
			double beyond = 0;
			double total = 0;
			for (int i = 1; i <= scoreDistribution.length; i++) {
				if (index >= (i - 1)) {
					beyond += getDistribution(i - 1);
				}
				total += getDistribution(i - 1);
			}
			return (int) (beyond * 10000.0f / total);
		}

		// 获取本服平均平均水平 万分比
		public int getServerAvg() {
//			if (this == SGuideType.Total) {
//				recalcTotalDistribution();
//			}
			double total = 0;
			int peopleCount = 0;
			for (int i = 1; i <= scoreDistribution.length; i++) {
				total += (i * getDistribution(i - 1));
				peopleCount += getDistribution(i - 1);
			}

			return (int) (total * 100 / peopleCount);
		}

//		private void recalcTotalDistribution() {
//			int distriCount = StrengthenGuideConstProperty.getInstance().getScoreStages();
//			for (int i = 0; i < distriCount; i++) {
//				int tmpPeopleCount = SGuideType.Building.getDistribution(i) + SGuideType.Army.getDistribution(i)
//						+ SGuideType.Hero.getDistribution(i) + SGuideType.Commander.getDistribution(i)
//						+ SGuideType.Science.getDistribution(i);
//				
//				SGuideType.Total.setDistribution(i, tmpPeopleCount);
//			}
//		}
		
//		static public void updateTotalWithIndex( Collection<Integer> idxs){
//			for(Integer index : idxs){
//				 SGuideType.Total.setDistribution(index, reCalculateTotalIndexCount(index));
//			}
//		}
	}
}
