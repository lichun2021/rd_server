package com.hawk.game.player.cache;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import com.hawk.game.entity.*;
import com.hawk.game.module.homeland.entity.PlayerHomeLandEntity;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;

import com.alibaba.fastjson.JSON;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.game.cfgElement.ArmourStarExplores;
import com.hawk.game.config.TavernScoreCfg;
import com.hawk.game.config.YuristrikeCfg;
import com.hawk.game.crossproxy.model.CsPlayerDataLoader;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.LocalRedis;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.item.PlayerAchieveItem;
import com.hawk.game.item.mission.MissionEntityItem;
import com.hawk.game.module.college.entity.CollegeMemberEntity;
import com.hawk.game.module.hospice.HospiceObj;
import com.hawk.game.module.lianmengyqzz.march.entitiy.PlayerYQZZEntity;
import com.hawk.game.module.mechacore.PlayerMechaCore;
import com.hawk.game.module.mechacore.entity.MechaCoreEntity;
import com.hawk.game.module.mechacore.entity.MechaCoreModuleEntity;
import com.hawk.game.module.nationMilitary.entity.NationMilitaryEntity;
import com.hawk.game.module.plantsoldier.advance.PlantSoldierAdvanceEntity;
import com.hawk.game.module.plantsoldier.science.PlantScienceEntity;
import com.hawk.game.module.plantsoldier.strengthen.PlantSoldierSchoolEntity;
import com.hawk.game.module.toucai.entity.MedalEntity;
import com.hawk.game.player.equip.CommanderObject;
import com.hawk.game.protocol.Const.PlayerState;
import com.hawk.game.protocol.Const.StateType;
import com.hawk.game.protocol.Dress.PlayerDressPlayerInfo;
import com.hawk.game.protocol.MechaCore.MechaCoreSuitType;
import com.hawk.game.protocol.MilitaryCollege.CollegeAuth;
import com.hawk.game.recharge.RechargeInfo;
import com.hawk.game.yuriStrikes.YuriStrike;

/**
 *这里加了代码之后请注意 CsPlayerDataLoader那边.
 *注意注意!!!!!!!!!!!
 *{@link CsPlayerDataLoader#load(PlayerDataKey)}
 * @author jm
 *
 */
public enum PlayerDataKey {
	PlayerEntity {
		@Override
		 public Object load(String playerId, boolean newly) {
			PlayerEntity playerEntity = GlobalData.getInstance().getCacheEntity(playerId);
			if (playerEntity == null) {
				List<PlayerEntity> playerEntitys = HawkDBManager.getInstance().query("from PlayerEntity where id = ? and invalid = 0", playerId);
				if (playerEntitys != null && playerEntitys.size() > 0) {
					playerEntity = playerEntitys.get(0);
				}
			} else {
				GlobalData.getInstance().removeCacheEntity(playerId);
			}
			return playerEntity;
		}
		
		@Override
		Class<? extends HawkDBEntity> entityType() {
			return com.hawk.game.entity.PlayerEntity.class;
		};
		
		@Override
		public boolean listMode() {
			return false;
		};
	},

	PlayerBaseEntity {
		@Override
		public Object load(String playerId, boolean newly) {
			PlayerBaseEntity playerBaseEntity = null;

			List<PlayerBaseEntity> playerBaseEntities = HawkDBManager.getInstance().query(
					"from PlayerBaseEntity where playerId = ? and invalid = 0", playerId);

			if (playerBaseEntities != null && playerBaseEntities.size() > 0) {
				playerBaseEntity = playerBaseEntities.get(0);
			} else {
				playerBaseEntity = new PlayerBaseEntity();
				playerBaseEntity.setPlayerId(playerId);
				if (!HawkDBManager.getInstance().create(playerBaseEntity)) {
					return null;
				}
			}
			playerBaseEntity.assemble();

			return playerBaseEntity;
		}
		
		@Override
		Class<? extends HawkDBEntity> entityType() {
			return com.hawk.game.entity.PlayerBaseEntity.class;
		};
		
		@Override
		public boolean listMode() {
			return false;
		};
	},

	StatisticsEntity {
		@Override
		public Object load(String playerId, boolean newly) {
			StatisticsEntity statisticsEntity = null;

			List<StatisticsEntity> statisticsEntities = HawkDBManager.getInstance().query(
					"from StatisticsEntity where playerId = ? and invalid = 0", playerId);

			if (statisticsEntities != null && statisticsEntities.size() > 0) {
				statisticsEntity = statisticsEntities.get(0);
			} else {
				statisticsEntity = new StatisticsEntity();
				statisticsEntity.setPlayerId(playerId);
				if (!HawkDBManager.getInstance().create(statisticsEntity)) {
					return null;
				}
			}

			return statisticsEntity;
		}
		
		@Override
		Class<? extends HawkDBEntity> entityType() {
			return com.hawk.game.entity.StatisticsEntity.class;
		};
		
		@Override
		public boolean listMode() {
			return false;
		};
	},

	PayStateEntity {
		@Override
		public Object load(String playerId, boolean newly) {
			PayStateEntity payStateEntity = null;

			List<PayStateEntity> payStateEntities = HawkDBManager.getInstance().query(
					"from PayStateEntity where playerId = ? and invalid = 0", playerId);

			if (payStateEntities != null && payStateEntities.size() > 0) {
				payStateEntity = payStateEntities.get(0);
				if (!HawkOSOperator.isEmptyString(payStateEntity.getRechargeInfo())) {
					List<RechargeInfo> rechargeInfos = JSON.parseArray(payStateEntity.getRechargeInfo(), RechargeInfo.class);
					if (rechargeInfos != null) {
						Map<String, RechargeInfo> map = payStateEntity.getRechargeInfoMap();
						for (RechargeInfo rechargeInfo : rechargeInfos) {
							map.put(rechargeInfo.getGoodsId(), rechargeInfo);
						}
					}
				}
			} else {
				payStateEntity = new PayStateEntity();
				payStateEntity.setPlayerId(playerId);
				if (!HawkDBManager.getInstance().create(payStateEntity)) {
					return null;
				}
			}

			return payStateEntity;
		}
		
		@Override
		Class<? extends HawkDBEntity> entityType() {
			return com.hawk.game.entity.PayStateEntity.class;
		};
		
		@Override
		public boolean listMode() {
			return false;
		};
	},

	TalentEntities {
		@Override
		public Object load(String playerId, boolean newly) {
			List<TalentEntity> talentEntities = null;
			if (newly) {
				talentEntities = new ArrayList<TalentEntity>();
			} else {
				talentEntities = HawkDBManager.getInstance().query(
						"from TalentEntity where playerId = ? and invalid = 0", playerId);
			}

			return talentEntities;
		}
		
		@Override
		Class<? extends HawkDBEntity> entityType() {
			return com.hawk.game.entity.TalentEntity.class;
		};
		
		@Override
		public boolean listMode() {
			return true;
		};
	},

	ItemEntities {
		@Override
		public Object load(String playerId, boolean newly) {
			List<ItemEntity> itemEntities = new CopyOnWriteArrayList<ItemEntity>();
			if (!newly) {
				List<ItemEntity> entityList = HawkDBManager.getInstance().query(
						"from ItemEntity where playerId = ? and invalid = 0", playerId);
				itemEntities.addAll(entityList);
			}

			return itemEntities;
		}
		
		@Override
		Class<? extends HawkDBEntity> entityType() {
			return com.hawk.game.entity.ItemEntity.class;
		};
		
		@Override
		public boolean listMode() {
			return true;
		};
	},

	BuildingEntities {
		@Override
		public Object load(String playerId, boolean newly) {
			List<BuildingBaseEntity> buildingEntities = new CopyOnWriteArrayList<BuildingBaseEntity>();
			if (!newly) {
				List<BuildingBaseEntity> list = HawkDBManager.getInstance().query(
						"from BuildingBaseEntity where playerId = ? and invalid = 0", playerId);

				buildingEntities.addAll(list);
			}
			return buildingEntities;
		}
		
		@Override
		Class<? extends HawkDBEntity> entityType() {
			return com.hawk.game.entity.BuildingBaseEntity.class;
		};
		
		@Override
		public boolean listMode() {
			return true;
		};
	},

	TechnologyEntities {
		@Override
		public Object load(String playerId, boolean newly) {
			List<TechnologyEntity> technologyEntities = null;
			if (newly) {
				technologyEntities = new ArrayList<TechnologyEntity>();
			} else {
				technologyEntities = HawkDBManager.getInstance().query(
						"from TechnologyEntity where playerId = ? and invalid = 0", playerId);
			}

			return technologyEntities;
		}
		
		@Override
		Class<? extends HawkDBEntity> entityType() {
			return com.hawk.game.entity.TechnologyEntity.class;
		};
		
		@Override
		public boolean listMode() {
			return true;
		};
	},

	PlayerGachaEntities {
		@Override
		public Object load(String playerId, boolean newly) {
			List<PlayerGachaEntity> playerGachaEntities = HawkDBManager.getInstance().query(
					"from PlayerGachaEntity where playerId = ? and invalid = 0", playerId);

			playerGachaEntities = Optional.ofNullable(playerGachaEntities).orElseGet(ArrayList::new);
			Collections.sort(playerGachaEntities, Comparator.comparingLong(PlayerGachaEntity::getCreateTime).reversed());
			return playerGachaEntities;
		}
		
		@Override
		Class<? extends HawkDBEntity> entityType() {
			return com.hawk.game.entity.PlayerGachaEntity.class;
		};
		
		@Override
		public boolean listMode() {
			return true;
		};
	},

	QueueEntities {
		@Override
		public Object load(String playerId, boolean newly) {
			List<QueueEntity> queueEntities = null;
			if (newly) {
				queueEntities = new ArrayList<QueueEntity>();
			} else {
				queueEntities = HawkDBManager.getInstance().query(
						"from QueueEntity where playerId = ? and invalid = 0", playerId);
			}
			return queueEntities;
		}
		
		@Override
		Class<? extends HawkDBEntity> entityType() {
			return com.hawk.game.entity.QueueEntity.class;
		};
		
		@Override
		public boolean listMode() {
			return true;
		};
	},

	ArmyEntities {
		@Override
		public Object load(String playerId, boolean newly) {
			List<ArmyEntity> armyEntities = null;
			if (newly) {
				armyEntities = new ArrayList<ArmyEntity>();
			} else {
				armyEntities = HawkDBManager.getInstance().query(
						"from ArmyEntity where playerId = ? and invalid = 0", playerId);
			}

			return armyEntities;
		}
		
		@Override
		Class<? extends HawkDBEntity> entityType() {
			return com.hawk.game.entity.ArmyEntity.class;
		};
		
		@Override
		public boolean listMode() {
			return true;
		};
	},

	EquipEntities {
		@Override
		public Object load(String playerId, boolean newly) {
			List<EquipEntity> equipEntities = null;
			if (newly) {
				equipEntities = new ArrayList<EquipEntity>();
			} else {
				equipEntities = HawkDBManager.getInstance().query(
						"from EquipEntity where playerId = ? and invalid = 0", playerId);
			}

			return equipEntities;
		}
		
		@Override
		Class<? extends HawkDBEntity> entityType() {
			return com.hawk.game.entity.EquipEntity.class;
		};
		
		@Override
		public boolean listMode() {
			return true;
		};
	},

	CommanderEntity {
		@Override
		public Object load(String playerId, boolean newly) {
			CommanderEntity commanderEntity = null;

			List<CommanderEntity> commanderEntities = HawkDBManager.getInstance().query(
					"from CommanderEntity where playerId = ? and invalid = 0", playerId);
			if (commanderEntities != null && commanderEntities.size() > 0) {
				commanderEntity = commanderEntities.get(0);
			} else {
				commanderEntity = new CommanderEntity();
				commanderEntity.setPlayerId(playerId);
				commanderEntity.setStarExplores(ArmourStarExplores.unSerialize(commanderEntity, "", ""));
				CommanderObject.create(commanderEntity);
				if (!HawkDBManager.getInstance().create(commanderEntity)) {
					return null;
				}
			}

			return commanderEntity;
		}
		
		@Override
		Class<? extends HawkDBEntity> entityType() {
			return com.hawk.game.entity.CommanderEntity.class;
		};
		
		@Override
		public boolean listMode() {
			return false;
		};
	},

	StatusDataEntities {
		@Override
		public Object load(String playerId, boolean newly) {
			List<StatusDataEntity> statusDataEntities = new ArrayList<StatusDataEntity>();
			if (!newly) {
				statusDataEntities = HawkDBManager.getInstance().query(
						"from StatusDataEntity where playerId = ? and invalid = 0 ", playerId);

				// 查询是否有升级领奖状态数据
				StatusDataEntity lvUpStateEntity = null;
				for (StatusDataEntity entity : statusDataEntities) {
					if (entity.getStatusId() == PlayerState.REWARD_LEVEL_VALUE && entity.getType() == StateType.PLAYER_STATE_VALUE) {
						lvUpStateEntity = entity;
						break;
					}
				}

				if (lvUpStateEntity == null) {
					lvUpStateEntity = new StatusDataEntity();
					lvUpStateEntity.setPlayerId(playerId);
					lvUpStateEntity.setType(StateType.PLAYER_STATE_VALUE);
					lvUpStateEntity.setStatusId(PlayerState.REWARD_LEVEL_VALUE);
					lvUpStateEntity.setVal(1);
					lvUpStateEntity.setUuid(HawkOSOperator.randomUUID());
					statusDataEntities.add(lvUpStateEntity);
					if (!HawkDBManager.getInstance().create(lvUpStateEntity)) {
						return null;
					}
				}
			}

			return statusDataEntities;
		}
		
		@Override
		Class<? extends HawkDBEntity> entityType() {
			return com.hawk.game.entity.StatusDataEntity.class;
		};
		
		@Override
		public boolean listMode() {
			return true;
		};
	},

	MissionEntities {
		@Override
		public Object load(String playerId, boolean newly) {
			List<MissionEntity> missionEntities = new CopyOnWriteArrayList<MissionEntity>();
			if (!newly) {
				List<MissionEntity> list = HawkDBManager.getInstance().query("from MissionEntity where playerId = ? "
						+ "and invalid = 0 ", playerId);

				missionEntities.addAll(list);
			}

			return missionEntities;
		}
		
		@Override
		Class<? extends HawkDBEntity> entityType() {
			return com.hawk.game.entity.MissionEntity.class;
		};
		
		@Override
		public boolean listMode() {
			return true;
		};
	},

	StoryMissionEntity {
		@Override
		public Object load(String playerId, boolean newly) {
			StoryMissionEntity storyMissionEntity = null;

			List<StoryMissionEntity> storyMissionEntities = HawkDBManager.getInstance().query("from StoryMissionEntity where playerId = ? " 
					+ "and invalid = 0 ORDER BY createTime", playerId);
			if (storyMissionEntities != null && storyMissionEntities.size() > 0) {
				storyMissionEntity = storyMissionEntities.get(0);
			} else {
				storyMissionEntity = new StoryMissionEntity();
				storyMissionEntity.setPlayerId(playerId);
				storyMissionEntity.setMissionItems(new ArrayList<MissionEntityItem>());
				if (!HawkDBManager.getInstance().create(storyMissionEntity)) {
					return null;
				}
			}

			return storyMissionEntity;
		}
		
		@Override
		Class<? extends HawkDBEntity> entityType() {
			return com.hawk.game.entity.StoryMissionEntity.class;
		};
		
		@Override
		public boolean listMode() {
			return false;
		};
	},

	PlayerAchieveEntity {
		@Override
		public Object load(String playerId, boolean newly) {
			PlayerAchieveEntity playerAchieveEntity = null;

			List<PlayerAchieveEntity> playerAchieveEntities = HawkDBManager.getInstance().query("from PlayerAchieveEntity where playerId = ? " + "and invalid = 0", playerId);
			if (playerAchieveEntities != null && playerAchieveEntities.size() > 0) {
				playerAchieveEntity = playerAchieveEntities.get(0);
			} else {
				playerAchieveEntity = new PlayerAchieveEntity();
				playerAchieveEntity.setPlayerId(playerId);
				playerAchieveEntity.updateMissionItems(new ArrayList<PlayerAchieveItem>());
				if (!HawkDBManager.getInstance().create(playerAchieveEntity)) {
					return null;
				}
			}

			return playerAchieveEntity;
		}
		
		@Override
		Class<? extends HawkDBEntity> entityType() {
			return com.hawk.game.entity.PlayerAchieveEntity.class;
		};
		
		@Override
		public boolean listMode() {
			return false;
		};
	},

	PlayerRechargeEntities {
		@Override
		public Object load(String playerId, boolean newly) {
			List<RechargeEntity> playerRechargeEntities = new CopyOnWriteArrayList<RechargeEntity>();
			if (!newly) {
				List<RechargeEntity> list = HawkDBManager.getInstance().query("from RechargeEntity where playerId = ? and invalid = 0", playerId);
				playerRechargeEntities.addAll(list);
			}

			return playerRechargeEntities;
		}
		
		@Override
		Class<? extends HawkDBEntity> entityType() {
			return com.hawk.game.entity.RechargeEntity.class;
		};
		
		@Override
		public boolean listMode() {
			return true;
		};
	},
	
	PlayerRechargeDailyEntities {
		@Override
		public Object load(String playerId, boolean newly) {
			List<RechargeDailyEntity> playerRechargeEntities = new CopyOnWriteArrayList<RechargeDailyEntity>();
			if (!newly) {
				List<RechargeDailyEntity> list = HawkDBManager.getInstance().query("from RechargeDailyEntity where playerId = ? and invalid = 0", playerId);
				playerRechargeEntities.addAll(list);
			}

			return playerRechargeEntities;
		}
		
		@Override
		Class<? extends HawkDBEntity> entityType() {
			return com.hawk.game.entity.RechargeDailyEntity.class;
		};
		
		@Override
		public boolean listMode() {
			return true;
		};
	},
	
	PlayerRechargeBackEntities {
		@Override
		public Object load(String playerId, boolean newly) {
			List<RechargeBackEntity> playerRechargeEntities = null;
			if (newly) {
				playerRechargeEntities = new ArrayList<RechargeBackEntity>();
			} else {
				playerRechargeEntities = HawkDBManager.getInstance().query(
						"from RechargeBackEntity where playerId = ? and invalid = 0", playerId);
			}

			return playerRechargeEntities;
		}
		
		@Override
		Class<? extends HawkDBEntity> entityType() {
			return com.hawk.game.entity.RechargeBackEntity.class;
		};
		
		@Override
		public boolean listMode() {
			return true;
		};
	},

	QuestionnaireEntity {
		@Override
		public Object load(String playerId, boolean newly) {
			QuestionnaireEntity questionnaireEntity = null;

			List<QuestionnaireEntity> questionnaireEntities = HawkDBManager.getInstance()
					.query("from QuestionnaireEntity where playerId = ? and invalid = 0", playerId);
			if (questionnaireEntities != null && questionnaireEntities.size() > 0) {
				questionnaireEntity = questionnaireEntities.get(0);
			} else {
				questionnaireEntity = new QuestionnaireEntity();
				questionnaireEntity.setPlayerId(playerId);
				questionnaireEntity.setLastCheckTime(HawkTime.getMillisecond());
				if (!HawkDBManager.getInstance().create(questionnaireEntity)) {
					return null;
				}
			}

			return questionnaireEntity;
		}
		
		@Override
		Class<? extends HawkDBEntity> entityType() {
			return com.hawk.game.entity.QuestionnaireEntity.class;
		};
		
		@Override
		public boolean listMode() {
			return false;
		};
	},

	MonsterEntity {
		@Override
		public Object load(String playerId, boolean newly) {
			PlayerMonsterEntity monsterEntity = null;

			List<PlayerMonsterEntity> entities = HawkDBManager.getInstance().query("from PlayerMonsterEntity where playerId = ? "
					+ "and invalid = 0 ORDER BY createTime", playerId);
			if (entities != null && entities.size() > 0) {
				monsterEntity = entities.get(0);
			} else {
				monsterEntity = new PlayerMonsterEntity();
				monsterEntity.setPlayerId(playerId);
				monsterEntity.setMaxLevel(0);
				monsterEntity.setCurrentLevelCount(0);
				if (!HawkDBManager.getInstance().create(monsterEntity)) {
					return null;
				}
			}

			return monsterEntity;
		}
		
		@Override
		Class<? extends HawkDBEntity> entityType() {
			return com.hawk.game.entity.PlayerMonsterEntity.class;
		};
		
		@Override
		public boolean listMode() {
			return false;
		};
	},

	WishingEntity {
		@Override
		public Object load(String playerId, boolean newly) {
			WishingWellEntity wishingEntity = null;

			List<WishingWellEntity> entities = HawkDBManager.getInstance().query("from WishingWellEntity where playerId = ? and invalid = 0", playerId);
			if (entities != null && entities.size() > 0) {
				wishingEntity = entities.get(0);
			} else {
				wishingEntity = new WishingWellEntity(playerId);
				if (!HawkDBManager.getInstance().create(wishingEntity)) {
					return null;
				}
			}

			return wishingEntity;
		}
		
		@Override
		Class<? extends HawkDBEntity> entityType() {
			return com.hawk.game.entity.WishingWellEntity.class;
		};
		
		@Override
		public boolean listMode() {
			return false;
		};
	},

	TavernEntity {
		@Override
		public Object load(String playerId, boolean newly) {
			TavernEntity tavernEntity = null;

			List<TavernEntity> entities = HawkDBManager.getInstance().query("from TavernEntity where playerId = ? and invalid = 0", playerId);
			if (entities != null && entities.size() > 0) {
				tavernEntity = entities.get(0);
			} else {
				tavernEntity = new TavernEntity(playerId, HawkTime.getMillisecond());
				// 积分成就项
				ConfigIterator<TavernScoreCfg> scoreIterator = HawkConfigManager.getInstance().getConfigIterator(TavernScoreCfg.class);
				while (scoreIterator.hasNext()) {
					TavernScoreCfg cfg = scoreIterator.next();
					AchieveItem item = AchieveItem.valueOf(cfg.getAchieveId());
					tavernEntity.addScoreItem(item);
				}
				if (!HawkDBManager.getInstance().create(tavernEntity)) {
					return null;
				}
			}

			return tavernEntity;
		}
		
		@Override
		Class<? extends HawkDBEntity> entityType() {
			return com.hawk.game.entity.TavernEntity.class;
		};
		
		@Override
		public boolean listMode() {
			return false;
		};
	},

	WharfEntity {
		@Override
		public Object load(String playerId, boolean newly) {
			WharfEntity wharfEntity = null;

			List<WharfEntity> entities = HawkDBManager.getInstance().query("from WharfEntity where playerId = ? and invalid = 0", playerId);
			if (entities != null && entities.size() > 0) {
				wharfEntity = entities.get(0);
			} else {
				wharfEntity = new WharfEntity(playerId);
				if (!HawkDBManager.getInstance().create(wharfEntity)) {
					return null;
				}
			}

			return wharfEntity;
		}
		
		@Override
		Class<? extends HawkDBEntity> entityType() {
			return com.hawk.game.entity.WharfEntity.class;
		};
		
		@Override
		public boolean listMode() {
			return false;
		};
	},

	HeroEntityList {
		@Override
		public Object load(String playerId, boolean newly) {
			List<HeroEntity> heroEntities = new CopyOnWriteArrayList<HeroEntity>();
			if (!newly) {
				List<HeroEntity> entities = HawkDBManager.getInstance().query(
						"from HeroEntity where playerId = ? and invalid = 0 ", playerId);
				heroEntities.addAll(entities);
			}
			return heroEntities;
		}
		
		@Override
		Class<? extends HawkDBEntity> entityType() {
			return com.hawk.game.entity.HeroEntity.class;
		};
		
		@Override
		public boolean listMode() {
			return true;
		};
	},
	
	PlantSoldierSchoolEntity {
		@Override
		public Object load(String playerId, boolean newly) {
			PlantSoldierSchoolEntity dailyDataEntity = null;

			List<PlantSoldierSchoolEntity> entities = HawkDBManager.getInstance().query("from PlantSoldierSchoolEntity where playerId = ? and invalid = 0", playerId);
			if (entities != null && entities.size() > 0) {
				dailyDataEntity = entities.get(0);
			} else {
				dailyDataEntity = new PlantSoldierSchoolEntity();
				dailyDataEntity.setPlayerId(playerId);
				dailyDataEntity.afterRead();
				if (!HawkDBManager.getInstance().create(dailyDataEntity)) {
					return null;
				}
			}

			return dailyDataEntity;
		}

		@Override
		Class<? extends HawkDBEntity> entityType() {
			return PlantSoldierSchoolEntity.class;
		};

		@Override
		public boolean listMode() {
			return false;
		};
	},

	PlantSoldierAdvanceEntity {
		@Override
		public Object load(String playerId, boolean newly) {
			PlantSoldierAdvanceEntity dailyDataEntity = null;

			List<PlantSoldierAdvanceEntity> entities = HawkDBManager.getInstance().query("from PlantSoldierAdvanceEntity where playerId = ? and invalid = 0", playerId);
			if (entities != null && entities.size() > 0) {
				dailyDataEntity = entities.get(0);
			} else {
				dailyDataEntity = new PlantSoldierAdvanceEntity();
				dailyDataEntity.setPlayerId(playerId);
				dailyDataEntity.afterRead();
				if (!HawkDBManager.getInstance().create(dailyDataEntity)) {
					return null;
				}
			}

			return dailyDataEntity;
		}

		@Override
		Class<? extends HawkDBEntity> entityType() {
			return PlantSoldierAdvanceEntity.class;
		};

		@Override
		public boolean listMode() {
			return false;
		};
	},
	
	GuildGiftEntity {
		@Override
		public Object load(String playerId, boolean newly) {
			List<PlayerGuildGiftEntity> guildGiftEntities = null;
			if (newly) {
				guildGiftEntities = new LinkedList<PlayerGuildGiftEntity>();
			} else {
				List<PlayerGuildGiftEntity> query = HawkDBManager.getInstance().query("from PlayerGuildGiftEntity where playerId=? and invalid=0", playerId);
				guildGiftEntities = new LinkedList<PlayerGuildGiftEntity>(query);
			}
			return guildGiftEntities;
		}
		
		@Override
		Class<? extends HawkDBEntity> entityType() {
			return com.hawk.game.entity.PlayerGuildGiftEntity.class;
		};
		
		@Override
		public boolean listMode() {
			return true;
		};
	},

	CustomDataEntities {
		@Override
		public Object load(String playerId, boolean newly) {
			List<CustomDataEntity> customDataEntities = null;
			if (newly) {
				customDataEntities = new ArrayList<CustomDataEntity>();
			} else {
				customDataEntities = HawkDBManager.getInstance().query(
						"from CustomDataEntity where playerId = ? and invalid = 0", playerId);
			}
			return customDataEntities;
		}
		
		@Override
		Class<? extends HawkDBEntity> entityType() {
			return com.hawk.game.entity.CustomDataEntity.class;
		};
		
		@Override
		public boolean listMode() {
			return true;
		};
	},

	DailyDataEntity {
		@Override
		public Object load(String playerId, boolean newly) {
			DailyDataEntity dailyDataEntity = null;

			List<DailyDataEntity> entities = HawkDBManager.getInstance().query("from DailyDataEntity where playerId = ? and invalid = 0", playerId);
			if (entities != null && entities.size() > 0) {
				dailyDataEntity = entities.get(0);
			} else {
				dailyDataEntity = new DailyDataEntity(playerId);
				if (!HawkDBManager.getInstance().create(dailyDataEntity)) {
					return null;
				}
			}

			return dailyDataEntity;
		}
		
		@Override
		Class<? extends HawkDBEntity> entityType() {
			return com.hawk.game.entity.DailyDataEntity.class;
		};
		
		@Override
		public boolean listMode() {
			return false;
		};
	},

	PlotBattleEntity {
		@Override
		public Object load(String playerId, boolean newly) {
			PlotBattleEntity plotBattleEntity = null;

			List<PlotBattleEntity> plotBattleEntities = HawkDBManager.getInstance().query("from PlotBattleEntity where playerId=? and invalid=0", playerId);
			if (plotBattleEntities != null && !plotBattleEntities.isEmpty()) {
				plotBattleEntity = plotBattleEntities.get(0);
			} else {
				plotBattleEntity = new PlotBattleEntity();
				plotBattleEntity.setPlayerId(playerId);
				if (!HawkDBManager.getInstance().create(plotBattleEntity)) {
					return null;
				}
			}

			return plotBattleEntity;
		}
		
		@Override
		Class<? extends HawkDBEntity> entityType() {
			return com.hawk.game.entity.PlotBattleEntity.class;
		};
		
		@Override
		public boolean listMode() {
			return false;
		};
	},

	PushGiftEntity {
		@Override
		public Object load(String playerId, boolean newly) {
			List<PushGiftEntity> pushGiftEntityList = HawkDBManager.getInstance().query("from PushGiftEntity where playerId=? and invalid=0", playerId);
			PushGiftEntity pushGiftEntity = null;
			if (pushGiftEntityList != null && !pushGiftEntityList.isEmpty()) {
				pushGiftEntity = pushGiftEntityList.get(0);
			} else {
				pushGiftEntity = new PushGiftEntity();
				pushGiftEntity.setPlayerId(playerId);
				pushGiftEntity.afterRead();

				if (!HawkDBManager.getInstance().create(pushGiftEntity)) {
					return null;
				}
			}

			return pushGiftEntity;
		}
		
		@Override
		Class<? extends HawkDBEntity> entityType() {
			return com.hawk.game.entity.PushGiftEntity.class;
		};
		
		@Override
		public boolean listMode() {
			return false;
		};
	},

	DressEntity {
		@Override
		public Object load(String playerId, boolean newly) {
			DressEntity dressEntity = null;

			List<DressEntity> entities = HawkDBManager.getInstance().query("from DressEntity where playerId = ? and invalid = 0", playerId);
			if (entities != null && entities.size() > 0) {
				dressEntity = entities.get(0);
			} else {
				dressEntity = new DressEntity(playerId);
				if (!HawkDBManager.getInstance().create(dressEntity)) {
					return null;
				}
			}

			return dressEntity;
		}
		
		@Override
		Class<? extends HawkDBEntity> entityType() {
			return com.hawk.game.entity.DressEntity.class;
		};
		
		@Override
		public boolean listMode() {
			return false;
		};
	},

	AccumulateOnlineEntity {
		@Override
		public Object load(String playerId, boolean newly) {
			AccumulateOnlineEntity accumulateOnlineEnitiy = null;

			List<AccumulateOnlineEntity> entities = HawkDBManager.getInstance().query("from AccumulateOnlineEntity where playerId = ? " + "and invalid = 0", playerId);
			if (entities != null && !entities.isEmpty()) {
				accumulateOnlineEnitiy = entities.get(0);
			} else {
				accumulateOnlineEnitiy = new AccumulateOnlineEntity();
				accumulateOnlineEnitiy.setPlayerId(playerId);
				if (!HawkDBManager.getInstance().create(accumulateOnlineEnitiy)) {
					return null;
				}
			}

			return accumulateOnlineEnitiy;
		}
		
		@Override
		Class<? extends HawkDBEntity> entityType() {
			return com.hawk.game.entity.AccumulateOnlineEntity.class;
		};
		
		@Override
		public boolean listMode() {
			return false;
		};
	},

	PlayerGiftEntity {
		@Override
		public Object load(String playerId, boolean newly) {
			PlayerGiftEntity playerGiftEntity = null;

			List<PlayerGiftEntity> entityList = HawkDBManager.getInstance().query(
					"from PlayerGiftEntity where playerId = ? and invalid = 0", playerId);

			if (entityList.isEmpty()) {
				playerGiftEntity = new PlayerGiftEntity();
				playerGiftEntity.setPlayerId(playerId);
				playerGiftEntity.afterRead();
				if (!HawkDBManager.getInstance().create(playerGiftEntity)) {
					return null;
				}
			} else {
				playerGiftEntity = entityList.get(0);
			}

			return playerGiftEntity;
		}
		
		@Override
		Class<? extends HawkDBEntity> entityType() {
			return com.hawk.game.entity.PlayerGiftEntity.class;
		};
		
		@Override
		public boolean listMode() {
			return false;
		};
	},

	MoneyReissueEntityList {
		@Override
		public Object load(String playerId, boolean newly) {
			List<MoneyReissueEntity> moneyReissueEntityList = null;
			if (newly) {
				moneyReissueEntityList = new ArrayList<MoneyReissueEntity>();
			} else {
				moneyReissueEntityList = HawkDBManager.getInstance().query(
						"from MoneyReissueEntity where playerId = ? and invalid = 0 ", playerId);
			}
			return moneyReissueEntityList;
		}
		
		@Override
		Class<? extends HawkDBEntity> entityType() {
			return com.hawk.game.entity.MoneyReissueEntity.class;
		};
		
		@Override
		public boolean listMode() {
			return true;
		};
	},

	PlayerResourceGiftEntity {
		@Override
		public Object load(String playerId, boolean newly) {
			PlayerResourceGiftEntity playerResourceGiftEntity = null;

			List<PlayerResourceGiftEntity> resGiftEntityList = HawkDBManager.getInstance().query("from PlayerResourceGiftEntity where playerId=? and invalid=0", playerId);
			if (resGiftEntityList != null && !resGiftEntityList.isEmpty()) {
				playerResourceGiftEntity = resGiftEntityList.get(0);
			}

			if (playerResourceGiftEntity == null) {
				playerResourceGiftEntity = new PlayerResourceGiftEntity();
				playerResourceGiftEntity.setPlayerId(playerId);
				playerResourceGiftEntity.afterRead();

				if (!HawkDBManager.getInstance().create(playerResourceGiftEntity)) {
					return null;
				}
			}

			return playerResourceGiftEntity;
		}
		
		@Override
		Class<? extends HawkDBEntity> entityType() {
			return com.hawk.game.entity.PlayerResourceGiftEntity.class;
		};
		
		@Override
		public boolean listMode() {
			return false;
		};
	},

	SettingDatas {
		@Override
		public Object load(String playerId, boolean newly) {
			Map<String, String> dataMap = RedisProxy.getInstance().getAllSettingData(playerId);
			Map<Integer, Integer> settingDatas = new ConcurrentHashMap<>();
			for (Entry<String, String> entry : dataMap.entrySet()) {
				settingDatas.put(Integer.valueOf(entry.getKey()), Integer.valueOf(entry.getValue()));
			}
			return settingDatas;
		}
		
		@Override
		boolean isSerializeable() { 
			return false;
		}
		
		@Override
		Class<? extends HawkDBEntity> entityType() {
			return null;
		};
	},

	ShieldPlayers {
		@Override
		public Object load(String playerId, boolean newly) {
			return new HashSet<>(LocalRedis.getInstance().getShieldPlayer(playerId));
		}
		
		@Override
		boolean isSerializeable() { 
			return false;
		}
		
		@Override
		Class<? extends HawkDBEntity> entityType() {
			return null;
		};
	},

	BanRankInfos {
		@Override
		public Object load(String playerId, boolean newly) {
			Map<String, String> banInfos = new ConcurrentHashMap<>();
			try {
				List<Object> returnObjs = RedisProxy.getInstance().fetchBanRankInfoBatch(playerId);
				for (Object banInfo : returnObjs) {
					if (banInfo == null) {
						continue;
					}

					String banInfoStr = (String) banInfo;
					String[] infos = banInfoStr.split(":");
					banInfos.put(infos[3], banInfoStr);
				}
			} catch (Exception e) {
				HawkException.catchException(e);
			}

			return banInfos;
		}
		
		@Override
		boolean isSerializeable() { 
			return false;
		}
		
		@Override
		Class<? extends HawkDBEntity> entityType() {
			return null;
		};
	},

	StorehouseBase {
		@Override
		public Object load(String playerId, boolean newly) {
			StorehouseBaseEntity storehouseBase = null;
			List<StorehouseBaseEntity> entities = HawkDBManager.getInstance().query("from StorehouseBaseEntity where playerId = ? and invalid = 0", playerId);
			if (!entities.isEmpty()) {
				storehouseBase = entities.get(0);
			}

			if (storehouseBase == null) {
				storehouseBase = new StorehouseBaseEntity();
				storehouseBase.setPlayerId(playerId);
				if (!HawkDBManager.getInstance().create(storehouseBase)) {
					return null;
				}
			}
			return storehouseBase;
		}
		
		@Override
		Class<? extends HawkDBEntity> entityType() {
			return com.hawk.game.entity.StorehouseBaseEntity.class;
		};
		
		@Override
		public boolean listMode() {
			return false;
		};
	},

	StorehouseEntities {
		@Override
		public Object load(String playerId, boolean newly) {
			List<StorehouseEntity> storehouseEntities = new CopyOnWriteArrayList<StorehouseEntity>();
			if (!newly) {
				List<StorehouseEntity> list = HawkDBManager.getInstance().query(
						"from StorehouseEntity where playerId = ? and invalid = 0 and collect = false", playerId);
				storehouseEntities.addAll(list);
			}
			return storehouseEntities;
		}
		
		@Override
		Class<? extends HawkDBEntity> entityType() {
			return com.hawk.game.entity.StorehouseEntity.class;
		};
		
		@Override
		public boolean listMode() {
			return true;
		};
	},

	StorehouseHelpEntities {
		@Override
		public Object load(String playerId, boolean newly) {
			List<StorehouseHelpEntity> storehouseHelpEntities = null;
			if (newly) {
				storehouseHelpEntities = new ArrayList<StorehouseHelpEntity>();
			} else {
				storehouseHelpEntities = HawkDBManager.getInstance().query(
						"from StorehouseHelpEntity where playerId = ? and invalid = 0 and collect = false", playerId);
			}
			return storehouseHelpEntities;
		}
		
		@Override
		Class<? extends HawkDBEntity> entityType() {
			return com.hawk.game.entity.StorehouseHelpEntity.class;
		};
		
		@Override
		public boolean listMode() {
			return true;
		};
	},

	YuriStrike {
		@Override
		public Object load(String playerId, boolean newly) {
			YuriStrikeEntity entity = null;
			List<YuriStrikeEntity> entities = HawkDBManager.getInstance().query("from YuriStrikeEntity where playerId = ? and invalid = 0", playerId);
			if (!entities.isEmpty()) {
				entity = entities.get(0);
			}

			if (entity == null) {
				Integer nextCfg = YuristrikeCfg.higherCfgId(0);
				entity = new YuriStrikeEntity();
				entity.setCfgId(nextCfg.intValue());
				entity.setPlayerId(playerId);
				YuriStrike bean = new YuriStrike();
				bean.setDbEntity(entity);
				if (!HawkDBManager.getInstance().create(entity)) {
					return null;
				}
			}
			return entity;
		}
		
		@Override
		Class<? extends HawkDBEntity> entityType() {
			return com.hawk.game.entity.YuriStrikeEntity.class;
		};
		
		@Override
		public boolean listMode() {
			return false;
		};
	},

	GuildHospice {
		@Override
		public Object load(String playerId, boolean newly) {
			GuildHospiceEntity entity = null;
			List<GuildHospiceEntity> entities = HawkDBManager.getInstance().query("from GuildHospiceEntity where playerId = ? and invalid = 0", playerId);
			if (!entities.isEmpty()) {
				entity = entities.get(0);
			}

			if (entity == null) {
				entity = new GuildHospiceEntity();
				entity.setPlayerId(playerId);
				HospiceObj bean = new HospiceObj();
				bean.setDbEntity(entity);
				if (!HawkDBManager.getInstance().create(entity)) {
					return null;
				}
			}
			return entity;
		}
		
		@Override
		Class<? extends HawkDBEntity> entityType() {
			return com.hawk.game.entity.GuildHospiceEntity.class;
		};
		
		@Override
		public boolean listMode() {
			return false;
		};
	},
	
	SuperSoldierEntityList {
		@Override
		public Object load(String playerId, boolean newly) {
			List<SuperSoldierEntity> superSoldierEntities = HawkDBManager.getInstance().query(
					"from SuperSoldierEntity where playerId = ? and invalid = 0 ", playerId);
			return superSoldierEntities;
		}
		
		@Override
		Class<? extends HawkDBEntity> entityType() {
			return com.hawk.game.entity.SuperSoldierEntity.class;
		};
		
		@Override
		public boolean listMode() {
			return true;
		};
	},
	
	PlayerWarCollegeEntity {
		@Override
		public Object load(String playerId, boolean newLy) {
			List<PlayerWarCollegeEntity> entityList = HawkDBManager.getInstance().query("from PlayerWarCollegeEntity where playerId = ? and invalid = 0", playerId);
			if (entityList == null || entityList.isEmpty()) {
				PlayerWarCollegeEntity playerWarCollegeEntity = new PlayerWarCollegeEntity();
				playerWarCollegeEntity.setPlayerId(playerId);
				playerWarCollegeEntity.afterRead();
				if (!HawkDBManager.getInstance().create(playerWarCollegeEntity)) {
					return null;
				}
				
				return playerWarCollegeEntity;
			} else {
				return entityList.get(0);
			}
		}
		
		@Override
		Class<? extends HawkDBEntity> entityType() {
			return com.hawk.game.entity.PlayerWarCollegeEntity.class;
		};
		
		@Override
		public boolean listMode() {
			return false;
		};
	},
	CollegeMemberEntity {
		@Override
		public Object load(String playerId, boolean newly) {
			CollegeMemberEntity collegeMemberEntity = null;

			List<CollegeMemberEntity> entities = HawkDBManager.getInstance()
					.query("from CollegeMemberEntity where playerId = ? and invalid = 0", playerId);
			if (entities != null && entities.size() > 0) {
				collegeMemberEntity = entities.get(0);
			} else {
				collegeMemberEntity = new CollegeMemberEntity();
				collegeMemberEntity.setPlayerId(playerId);
				collegeMemberEntity.setAuth(CollegeAuth.NOJOIN_VALUE);
				collegeMemberEntity.setCollegeId("");
				collegeMemberEntity.setOnlineTookInfo("");
				if (!HawkDBManager.getInstance().create(collegeMemberEntity)) {
					return null;
				}
			}

			return collegeMemberEntity;
		}
		
		@Override
		Class<? extends HawkDBEntity> entityType() {
			return CollegeMemberEntity.class;
		};
		
		@Override
		public boolean listMode() {
			return false;
		};
	},
	PlayerDressSendEntities{

		@Override
		public Object load(String playerId, boolean newly) {
			if (newly) {
				return new ArrayList<PlayerDressPlayerInfo>();
			} else {
				return RedisProxy.getInstance().getPlayerAllDressSendInfo(playerId);
			}
		}
			
		@Override
		boolean isSerializeable() { 
			return false;
		}
		
		@Override
		Class<? extends HawkDBEntity> entityType() {
			return null;
		};
		
	},
	
	PlayerDressAskEntities{
		@Override
		public Object load(String playerId, boolean newly) {
			if (newly) {
				return new ArrayList<PlayerDressPlayerInfo>();
			} else {
				return RedisProxy.getInstance().getPlayerAllDressAskInfo(playerId);
			}
		}
			
		@Override
		boolean isSerializeable() { 
			return false;
		}
		
		@Override
		Class<? extends HawkDBEntity> entityType() {
			return null;
		};
	},
	
	ArmourEntities {
		@Override
		public Object load(String playerId, boolean newly) {
			List<ArmourEntity> armourEntities = new CopyOnWriteArrayList<ArmourEntity>();
			if (!newly) {
				List<ArmourEntity> list = HawkDBManager.getInstance().query("from ArmourEntity where playerId = ? and invalid = 0", playerId);
				armourEntities.addAll(list);
			}
			return armourEntities;
		}
		
		@Override
		Class<? extends HawkDBEntity> entityType() {
			return com.hawk.game.entity.ArmourEntity.class;
		};
		
		@Override
		public boolean listMode() {
			return true;
		};
	},
	
	EquipResearchEntities {
		@Override
		public Object load(String playerId, boolean newly) {
			List<EquipResearchEntity> equipResearchEntities = new CopyOnWriteArrayList<EquipResearchEntity>();
			if (!newly) {
				List<EquipResearchEntity> list = HawkDBManager.getInstance().query(
						"from EquipResearchEntity where playerId = ? and invalid = 0", playerId);
				equipResearchEntities.addAll(list);
			}

			return equipResearchEntities;
		}
		
		@Override
		Class<? extends HawkDBEntity> entityType() {
			return com.hawk.game.entity.EquipResearchEntity.class;
		};
		
		@Override
		public boolean listMode() {
			return true;
		};
	},
	
	PlayerOtherEntity {
		@Override
		public Object load(String playerId, boolean newly) {
			PlayerOtherEntity playerOtherEntity = null;

			List<PlayerOtherEntity> entityList = HawkDBManager.getInstance().query(
					"from PlayerOtherEntity where playerId = ? and invalid = 0", playerId);

			if (entityList.isEmpty()) {
				playerOtherEntity = new PlayerOtherEntity();
				playerOtherEntity.setPlayerId(playerId);				
				playerOtherEntity.afterRead();
				if (!HawkDBManager.getInstance().create(playerOtherEntity)) {
					return null;
				}
			} else {
				playerOtherEntity = entityList.get(0);
			}

			return playerOtherEntity;
		}
		
		@Override
		Class<? extends HawkDBEntity> entityType() {
			return com.hawk.game.entity.PlayerOtherEntity.class;
		};
		
		@Override
		public boolean listMode() {
			return false;
		};
	},
	
	LaboratoryEntityList {
		@Override
		public Object load(String playerId, boolean newly) {
			List<LaboratoryEntity> resykt = new CopyOnWriteArrayList<LaboratoryEntity>();
			if (!newly) {
				List<LaboratoryEntity> list = HawkDBManager.getInstance().query("from LaboratoryEntity where playerId = ? and invalid = 0 ", playerId);
				resykt.addAll(list);
			}
			return resykt;
		}
		
		@Override
		Class<? extends HawkDBEntity> entityType() {
			return com.hawk.game.entity.LaboratoryEntity.class;
		};
		
		@Override
		public boolean listMode() {
			return true;
		};
	},

    ManhattanEntityList {
		@Override
		public Object load(String playerId, boolean newly) {
			List<ManhattanEntity> manhattanEntities = null;
			if (newly) {
				manhattanEntities = new ArrayList<>();
			} else {
				manhattanEntities = HawkDBManager.getInstance().query("from ManhattanEntity where playerId = ? and invalid = 0 ", playerId);
			}
			return manhattanEntities;
		}
		
		@Override
		Class<? extends HawkDBEntity> entityType() {
			return com.hawk.game.entity.ManhattanEntity.class;
		};
		
		@Override
		public boolean listMode() {
			return true;
		};
	},
    
    MechaCoreEntity {
		@Override
		public Object load(String playerId, boolean newly) {
			MechaCoreEntity mechaCoreEntity = null;
			List<MechaCoreEntity> entities = HawkDBManager.getInstance().query("from MechaCoreEntity where playerId = ? and invalid = 0  ORDER BY createTime", playerId);
			if (entities != null && entities.size() > 0) {
				mechaCoreEntity = entities.get(0);
			} else {
				mechaCoreEntity = new MechaCoreEntity();
				mechaCoreEntity.setPlayerId(playerId);
				mechaCoreEntity.setWorkSuit(MechaCoreSuitType.MECHA_ONE_VALUE);
				if (!HawkDBManager.getInstance().create(mechaCoreEntity)) {
					return null;
				}
				PlayerMechaCore.create(mechaCoreEntity);
			}

			return mechaCoreEntity;
		}
		
		@Override
		Class<? extends HawkDBEntity> entityType() {
			return MechaCoreEntity.class;
		};
		
		@Override
		public boolean listMode() {
			return false;
		};
	},
    
    MechaCoreModuleEntities {
		@Override
		public Object load(String playerId, boolean newly) {
			List<MechaCoreModuleEntity> moduleEntities = new CopyOnWriteArrayList<MechaCoreModuleEntity>();
			if (!newly) {
				List<MechaCoreModuleEntity> list = HawkDBManager.getInstance().query("from MechaCoreModuleEntity where playerId = ? and invalid = 0", playerId);
				moduleEntities.addAll(list);
			}
			return moduleEntities;
		}
		
		@Override
		Class<? extends HawkDBEntity> entityType() {
			return com.hawk.game.module.mechacore.entity.MechaCoreModuleEntity.class;
		};
		
		@Override
		public boolean listMode() {
			return true;
		};
	},
	
	CrossTechEntities {
		@Override
		public Object load(String playerId, boolean newly) {
			List<CrossTechEntity> crossTechEntities = new CopyOnWriteArrayList<CrossTechEntity>();
			if (!newly) {
				List<CrossTechEntity> list = HawkDBManager.getInstance().query(
						"from CrossTechEntity where playerId = ? and invalid = 0", playerId);
				crossTechEntities.addAll(list);
			}

			return crossTechEntities;
		}
		
		@Override
		Class<? extends HawkDBEntity> entityType() {
			return com.hawk.game.entity.CrossTechEntity.class;
		};
		
		@Override
		public boolean listMode() {
			return true;
		};
	},
		
	
	PlayerGhostTowerEntity{
	@Override
	public Object load(String playerId, boolean newly) {
		PlayerGhostTowerEntity playerGhostTowerEntity = null;
		List<PlayerGhostTowerEntity> entityList = HawkDBManager.getInstance().query(
				"from PlayerGhostTowerEntity where playerId = ? and invalid = 0", playerId);
		if (entityList.isEmpty()) {
			playerGhostTowerEntity = new PlayerGhostTowerEntity();
			playerGhostTowerEntity.setPlayerId(playerId);				
			playerGhostTowerEntity.afterRead();
			if (!HawkDBManager.getInstance().create(playerGhostTowerEntity)) {
				return null;
			}
		} else {
			playerGhostTowerEntity = entityList.get(0);
		}

		return playerGhostTowerEntity;
	}
	

	@Override
	public boolean listMode() {
		return false;
	};
	
	@Override
	Class<? extends HawkDBEntity> entityType() {
		return com.hawk.game.entity.PlayerGhostTowerEntity.class;
	};
	
	
	},

	ObeliskEntities {
		@Override
		public Object load(String playerId, boolean newly) {
			List<ObeliskEntity> obeliskEntities = null;
			if (newly) {
				obeliskEntities = new ArrayList<ObeliskEntity>();
			} else {
				obeliskEntities = HawkDBManager.getInstance().query(
						"from ObeliskEntity where playerId = ? and invalid = 0", playerId);
			}

			return obeliskEntities;
		}

		@Override
		Class<? extends HawkDBEntity> entityType() {
			return com.hawk.game.entity.ObeliskEntity.class;
		};

		@Override
		public boolean listMode() {
			return true;
		};
	},
	PlantFactoryEntities {
		@Override
		public Object load(String playerId, boolean newly) {
			List<PlantFactoryEntity> talentEntities = null;
			if (newly) {
				talentEntities = new ArrayList<PlantFactoryEntity>();
			} else {
				talentEntities = HawkDBManager.getInstance().query(
						"from PlantFactoryEntity where playerId = ? and invalid = 0", playerId);
			}

			return talentEntities;
		}
		
		@Override
		Class<? extends HawkDBEntity> entityType() {
			return com.hawk.game.entity.PlantFactoryEntity.class;
		};
		
		@Override
		public boolean listMode() {
			return true;
		};
	},

	PlantTechEntities {
		@Override
		public Object load(String playerId, boolean newly) {
			List<PlantTechEntity> talentEntities = null;
			if (newly) {
				talentEntities = new ArrayList<PlantTechEntity>();
			} else {
				talentEntities = HawkDBManager.getInstance().query(
						"from PlantTechEntity where playerId = ? and invalid = 0", playerId);
			}

			return talentEntities;
		}
		
		@Override
		Class<? extends HawkDBEntity> entityType() {
			return com.hawk.game.entity.PlantTechEntity.class;
		};
		
		@Override
		public boolean listMode() {
			return true;
		};
	},
	
	
	AgencyEntity {
		@Override
		public Object load(String playerId, boolean newly) {
			AgencyEntity agencyEntity = null;
			if (newly) {
				agencyEntity = new AgencyEntity();
				agencyEntity.setPlayerId(playerId);
				if (!HawkDBManager.getInstance().create(agencyEntity)) {
					return null;
				}
				return agencyEntity;
			}
			List<AgencyEntity> entities = HawkDBManager.getInstance().query("from AgencyEntity where playerId = ? and invalid = 0 ", playerId);
			if (entities != null && entities.size() > 0) {
				agencyEntity = entities.get(0);
			} else {
				agencyEntity = new AgencyEntity();
				agencyEntity.setPlayerId(playerId);
				if (!HawkDBManager.getInstance().create(agencyEntity)) {
					return null;
				}
			}
			return agencyEntity;
		}
		
		@Override
		Class<? extends HawkDBEntity> entityType() {
			return AgencyEntity.class;
		};
		
		@Override
		public boolean listMode() {
			return false;
		};
		
		
	},
	
	PlantScienceEntity{
		@Override
		public Object load(String playerId, boolean newly) {
			PlantScienceEntity entity = null;
			List<PlantScienceEntity> entityList = HawkDBManager.getInstance().query(
					"from PlantScienceEntity where playerId = ? and invalid = 0", playerId);
			if (entityList.isEmpty()) {
				entity = new PlantScienceEntity();
				entity.setPlayerId(playerId);				
				entity.afterRead();
				if (!HawkDBManager.getInstance().create(entity)) {
					return null;
				}
			} else {
				entity = entityList.get(0);
			}

			return entity;
		}
		
		@Override
		Class<? extends HawkDBEntity> entityType() {
			return com.hawk.game.module.plantsoldier.science.PlantScienceEntity.class;
		}
		
		@Override
		public boolean listMode() {
			return false;
		}
	},
	
	PlayerYqzzData{
		@Override
		public Object load(String playerId, boolean newly) {
			PlayerYQZZEntity entity = null;
			List<PlayerYQZZEntity> entityList = HawkDBManager.getInstance().query(
					"from PlayerYQZZEntity where playerId = ? and invalid = 0", playerId);
			if (entityList.isEmpty()) {
				entity = new PlayerYQZZEntity();
				entity.setPlayerId(playerId);				
				entity.afterRead();
				if (!HawkDBManager.getInstance().create(entity)) {
					return null;
				}
			} else {
				entity = entityList.get(0);
			}

			return entity;
		}
		
		@Override
		Class<? extends HawkDBEntity> entityType() {
			return com.hawk.game.module.lianmengyqzz.march.entitiy.PlayerYQZZEntity.class;
		}
		
		@Override
		public boolean listMode() {
			return false;
		}
	},
	
	NationMissionEntity {
		@Override
		public Object load(String playerId, boolean newly) {
			NationMissionEntity entity = null;

			List<NationMissionEntity> entities = HawkDBManager.getInstance().query("from NationMissionEntity where playerId = ? " + "and invalid = 0", playerId);
			if (entities != null && entities.size() > 0) {
				entity = entities.get(0);
			} else {
				entity = new NationMissionEntity();
				entity.setPlayerId(playerId);
				entity.setType(1);
				if (!HawkDBManager.getInstance().create(entity)) {
					return null;
				}
			}

			return entity;
		}
		
		@Override
		Class<? extends HawkDBEntity> entityType() {
			return com.hawk.game.entity.NationMissionEntity.class;
		};
		
		@Override
		public boolean listMode() {
			return false;
		};
	},
	
	NationalBuildQuestEntity{
		@Override
		public Object load(String playerId, boolean newly) {
			NationBuildQuestEntity entity = null;
			List<NationBuildQuestEntity> entityList = HawkDBManager.getInstance().query(
					"from NationBuildQuestEntity where playerId = ? and invalid = 0", playerId);
			if (entityList.isEmpty()) {
				entity = new NationBuildQuestEntity();
				entity.setPlayerId(playerId);				
				entity.afterRead();
				if (!HawkDBManager.getInstance().create(entity)) {
					return null;
				}
			} else {
				entity = entityList.get(0);
			}

			return entity;
		}
		
		@Override
		Class<? extends HawkDBEntity> entityType() {
			return com.hawk.game.entity.NationBuildQuestEntity.class;
		}
		
		@Override
		public boolean listMode() {
			return false;
		}
	},
	
	NationMilitaryEntity {
		@Override
		public Object load(String playerId, boolean newly) {
			NationMilitaryEntity entity = null;

			List<NationMilitaryEntity> entities = HawkDBManager.getInstance().query("from NationMilitaryEntity where playerId = ? " + "and invalid = 0", playerId);
			if (entities != null && entities.size() > 0) {
				entity = entities.get(0);
			} else {
				entity = new NationMilitaryEntity();
				entity.setPlayerId(playerId);
				if (!HawkDBManager.getInstance().create(entity)) {
					return null;
				}
			}

			return entity;
		}
		
		@Override
		Class<? extends HawkDBEntity> entityType() {
			return NationMilitaryEntity.class;
		};
		
		@Override
		public boolean listMode() {
			return false;
		};
	},
	
	HeroArchivesEntity {
		@Override
		public Object load(String playerId, boolean newly) {
			HeroArchivesEntity heroArchivesEntity = null;

			List<HeroArchivesEntity> entities = HawkDBManager.getInstance().query("from HeroArchivesEntity where playerId = ? and invalid = 0", playerId);
			if (entities != null && entities.size() > 0) {
				heroArchivesEntity = entities.get(0);
			} else {
				heroArchivesEntity = new HeroArchivesEntity(playerId);
				if (!HawkDBManager.getInstance().create(heroArchivesEntity)) {
					return null;
				}
			}

			return heroArchivesEntity;
		}
		
		@Override
		Class<? extends HawkDBEntity> entityType() {
			return HeroArchivesEntity.class;
		};
		
		@Override
		public boolean listMode() {
			return false;
		};
	},
	LifetimeCardEntity {
		@Override
		public Object load(String playerId, boolean newly) {
			LifetimeCardEntity entity = null;
			List<LifetimeCardEntity> entities = HawkDBManager.getInstance().query("from LifetimeCardEntity where playerId = ? " + "and invalid = 0", playerId);
			if (entities != null && entities.size() > 0) {
				entity = entities.get(0);
			} else {
				entity = new LifetimeCardEntity();
				entity.setPlayerId(playerId);
				if (!HawkDBManager.getInstance().create(entity)) {
					return null;
				}
			}
			return entity;
		}
		@Override
		Class<? extends HawkDBEntity> entityType() {
			return LifetimeCardEntity.class;
		};
		@Override
		public boolean listMode() {
			return false;
		};
	},
	MedalEntity {
		@Override
		public Object load(String playerId, boolean newly) {
			MedalEntity entity = null;
			List<MedalEntity> entities = HawkDBManager.getInstance().query("from MedalEntity where playerId = ? " + "and invalid = 0", playerId);
			if (entities != null && entities.size() > 0) {
				entity = entities.get(0);
			} else {
				entity = new MedalEntity();
				entity.setPlayerId(playerId);
				entity.afterRead();
				if (!HawkDBManager.getInstance().create(entity)) {
					return null;
				}
			}
			return entity;
		}

		@Override
		Class<? extends HawkDBEntity> entityType() {
			return MedalEntity.class;
		}

		@Override
		public boolean listMode() {
			return false;
		}
		
	},
	
	PlayerDailyGiftBuy{
		@Override
		public Object load(String playerId, boolean newly) {
			PlayerDailyGiftBuyEntity entity = null;
			List<PlayerDailyGiftBuyEntity> entityList = HawkDBManager.getInstance().query(
					"from PlayerDailyGiftBuyEntity where playerId = ? and invalid = 0  ORDER BY createTime", playerId);
			if (entityList.isEmpty()) {
				entity = new PlayerDailyGiftBuyEntity();
				entity.setPlayerId(playerId);				
				entity.afterRead();
				if (!HawkDBManager.getInstance().create(entity)) {
					return null;
				}
			} else {
				entity = entityList.get(0);
			}

			return entity;
		}
		
		@Override
		Class<? extends HawkDBEntity> entityType() {
			return com.hawk.game.entity.PlayerDailyGiftBuyEntity.class;
		}
		
		@Override
		public boolean listMode() {
			return false;
		}
	},

	PlayerShopEntityList{
		@Override
		public Object load(String playerId, boolean newly) {
			List<PlayerShopEntity> shopEntities = new CopyOnWriteArrayList<>();
			if (!newly) {
				List<PlayerShopEntity> entities = HawkDBManager.getInstance().query(
						"from PlayerShopEntity where playerId = ? and invalid = 0 ", playerId);
				shopEntities.addAll(entities);
			}
			return shopEntities;
		}

		@Override
		Class<? extends HawkDBEntity> entityType() {
			return PlayerShopEntity.class;
		}

		@Override
		public boolean listMode() {
			return true;
		}
	},

	PlayerXQHXTalentEntityList{
		@Override
		public Object load(String playerId, boolean newly) {
			List<PlayerXQHXTalentEntity> talentEntities = new CopyOnWriteArrayList<>();
			if(!newly){
				List<PlayerXQHXTalentEntity> entities = HawkDBManager.getInstance().query(
						"from PlayerXQHXTalentEntity where playerId = ? and invalid = 0 ", playerId);
				talentEntities.addAll(entities);
			}
			return talentEntities;
		}

		@Override
		Class<? extends HawkDBEntity> entityType() {
			return PlayerXQHXTalentEntity.class;
		}

		@Override
		public boolean listMode() {
			return true;
		}
	},

	PlayerXQHXEntity {
		@Override
		public Object load(String playerId, boolean newly) {
			PlayerXQHXEntity entity = null;

			List<PlayerXQHXEntity> entities = HawkDBManager.getInstance().query("from PlayerXQHXEntity where playerId = ? and invalid = 0", playerId);
			if (entities != null && !entities.isEmpty()) {
				entity = entities.get(0);
			} else {
				entity = new PlayerXQHXEntity();
				entity.setPlayerId(playerId);
				entity.afterRead();
				if (!HawkDBManager.getInstance().create(entity)) {
					return null;
				}
			}
			return entity;
		}

		@Override
		Class<? extends HawkDBEntity> entityType() {
			return PlayerXQHXEntity.class;
		};

		@Override
		public boolean listMode() {
			return false;
		};
	},
	PlayerHomeLand {
		@Override
		public Object load(String playerId, boolean newly) {
			PlayerHomeLandEntity entity = null;
			List<PlayerHomeLandEntity> entities = HawkDBManager.getInstance().query("from PlayerHomeLandEntity where playerId = ? and invalid = 0 ORDER BY createTime", playerId);
			if (entities != null && !entities.isEmpty()) {
				entity = entities.get(0);
			} else {
				entity = new PlayerHomeLandEntity();
				entity.setPlayerId(playerId);
				entity.afterRead();
				if (!HawkDBManager.getInstance().create(entity)) {
					return null;
				}
			}
			return entity;
		}

		@Override
		Class<? extends HawkDBEntity> entityType() {
			return PlayerHomeLandEntity.class;
		}

		@Override
		public boolean listMode() {
			return false;
		}
    },
	;
	
	/**
	 * 加载接口
	 * 注意注意!!!!!!!!!
	 * {@link PlayerDataKey}
	 * @param playerId
	 * @param newly
	 * @return
	 */
	public abstract Object load(String playerId, boolean newly);
	
	/**
	 * 是否可序列化
	 * 
	 * @return
	 */
	boolean isSerializeable() { 
		return true;
	}
	
	/**
	 * 实体对象类型, 为null就表示没有
	 */
	Class<? extends HawkDBEntity> entityType() {
		return null;
	};
	
	/**
	 * 是否为list模式
	 */
	public boolean listMode() {
		return false;
	};
	
	/***
	 * 跨服是否可用
	 */
	public boolean crossLoad(){
		return true;
	}
}
