package com.hawk.game.lianmengjunyan.player.npc.cache;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.hawk.uuid.HawkUUIDGenerator;

import com.hawk.game.config.LMJYNpcCfg;
import com.hawk.game.entity.ArmyEntity;
import com.hawk.game.entity.CommanderEntity;
import com.hawk.game.entity.DressEntity;
import com.hawk.game.entity.PlayerBaseEntity;
import com.hawk.game.entity.PlayerEntity;
import com.hawk.game.entity.StatisticsEntity;

public enum LMJYNPCPlayerDataKey {
	PlayerEntity {
		@Override
		Object load(String playerId, LMJYNpcCfg npcCfg) {

			PlayerEntity playerEntity = new PlayerEntity();
			playerEntity.setName(npcCfg.getPlayerName());
			playerEntity.setId(playerId);
			playerEntity.setIcon(npcCfg.getIcon());
			playerEntity.setBattlePoint(npcCfg.getPower());
			return playerEntity;
		}
	},

	PlayerBaseEntity {
		@Override
		Object load(String playerId, LMJYNpcCfg npcCfg) {
			PlayerBaseEntity baseEntity = new PlayerBaseEntity();
			baseEntity.setPlayerId(playerId);
			baseEntity.setLevel(npcCfg.getCityLevel());
			return baseEntity;
		}
	},

	StatisticsEntity {
		@Override
		Object load(String playerId, LMJYNpcCfg npcCfg) {
			StatisticsEntity statisticsEntity = new StatisticsEntity();
			statisticsEntity.setPlayerId(playerId);
			return statisticsEntity;
		}
	},

	PayStateEntity {
		@Override
		Object load(String playerId, LMJYNpcCfg npcCfg) {
			return null;
		}
	},

	TalentEntities {
		@Override
		Object load(String playerId, LMJYNpcCfg npcCfg) {
			return new ArrayList<>();
		}
	},

	ItemEntities {
		@Override
		Object load(String playerId, LMJYNpcCfg npcCfg) {
			return null;
		}
	},

	BuildingEntities {
		@Override
		Object load(String playerId, LMJYNpcCfg npcCfg) {
			return new ArrayList<>();
		}
	},

	TechnologyEntities {
		@Override
		Object load(String playerId, LMJYNpcCfg npcCfg) {
			return new ArrayList<>();
		}
	},

	PlayerGachaEntities {
		@Override
		Object load(String playerId, LMJYNpcCfg npcCfg) {
			return null;
		}
	},
	
	ArmourEntities {
		@Override
		Object load(String playerId, LMJYNpcCfg npcCfg) {
			return new ArrayList<>();
		}
	},

	QueueEntities {
		@Override
		Object load(String playerId, LMJYNpcCfg npcCfg) {
			return new ArrayList<>();
		}
	},

	ArmyEntities {
		@Override
		Object load(String playerId, LMJYNpcCfg npcCfg) {
			Map<Integer, Integer> sinfo = new HashMap<Integer, Integer>();
			sinfo.putAll(npcCfg.getRandSoldierInfo());
			sinfo.putAll(npcCfg.getRandTrapInfo());
			// army副本
			List<ArmyEntity> armyEntities = new ArrayList<>();
			for (Entry<Integer, Integer> ent : sinfo.entrySet()) {
				int armyid = ent.getKey();
				int num = ent.getValue();
				ArmyEntity army = new ArmyEntity();
				army.setPersistable(false);
				army.setId(HawkUUIDGenerator.genUUID());
				army.setPlayerId(playerId);
				army.setArmyId(armyid);
				army.addFree(num);
				armyEntities.add(army);
			}
			return armyEntities;
		}
	},

	EquipEntities {
		@Override
		Object load(String playerId, LMJYNpcCfg npcCfg) {
			return null;
		}
	},

	CommanderEntity {
		@Override
		Object load(String playerId, LMJYNpcCfg npcCfg) {
			CommanderEntity commanderEntity = new CommanderEntity();
			commanderEntity.setPlayerId(playerId);
			commanderEntity.afterRead();
			return commanderEntity;
		}
	},

	StatusDataEntities {
		@Override
		Object load(String playerId, LMJYNpcCfg npcCfg) {
			return new ArrayList<>();
		}
	},

	MissionEntities {
		@Override
		Object load(String playerId, LMJYNpcCfg npcCfg) {
			return null;
		}
	},

	StoryMissionEntity {
		@Override
		Object load(String playerId, LMJYNpcCfg npcCfg) {
			return null;
		}
	},

	PlayerAchieveEntity {
		@Override
		Object load(String playerId, LMJYNpcCfg npcCfg) {
			return null;
		}
	},

	PlayerRechargeEntities {
		@Override
		Object load(String playerId, LMJYNpcCfg npcCfg) {
			return null;
		}
	},

	QuestionnaireEntity {
		@Override
		Object load(String playerId, LMJYNpcCfg npcCfg) {
			return null;
		}
	},

	MonsterEntity {
		@Override
		Object load(String playerId, LMJYNpcCfg npcCfg) {
			return null;
		}
	},

	WishingEntity {
		@Override
		Object load(String playerId, LMJYNpcCfg npcCfg) {
			return null;
		}
	},

	TavernEntity {
		@Override
		Object load(String playerId, LMJYNpcCfg npcCfg) {
			return null;
		}
	},

	WharfEntity {
		@Override
		Object load(String playerId, LMJYNpcCfg npcCfg) {
			return null;
		}
	},

	HeroEntityList {
		@Override
		Object load(String playerId, LMJYNpcCfg npcCfg) {
			return new ArrayList<>();
		}
	},

	GuildGiftEntity {
		@Override
		Object load(String playerId, LMJYNpcCfg npcCfg) {
			return null;
		}
	},

	CustomDataEntities {
		@Override
		Object load(String playerId, LMJYNpcCfg npcCfg) {
			return Collections.emptyList(); 
		}
	},

	DailyDataEntity {
		@Override
		Object load(String playerId, LMJYNpcCfg npcCfg) {
			return null;
		}
	},

	PlotBattleEntity {
		@Override
		Object load(String playerId, LMJYNpcCfg npcCfg) {
			return null;
		}
	},

	PushGiftEntity {
		@Override
		Object load(String playerId, LMJYNpcCfg npcCfg) {
			return null;
		}
	},

	DressEntity {
		@Override
		Object load(String playerId, LMJYNpcCfg npcCfg) {
			return new DressEntity(playerId);
		}
	},

	AccumulateOnlineEntity {
		@Override
		Object load(String playerId, LMJYNpcCfg npcCfg) {
			return null;
		}
	},

	PlayerGiftEntity {
		@Override
		Object load(String playerId, LMJYNpcCfg npcCfg) {
			return null;
		}
	},

	MoneyReissueEntityList {
		@Override
		Object load(String playerId, LMJYNpcCfg npcCfg) {
			return null;
		}
	},

	PlayerResourceGiftEntity {
		@Override
		Object load(String playerId, LMJYNpcCfg npcCfg) {
			return null;
		}
	},

	SettingDatas {
		@Override
		Object load(String playerId, LMJYNpcCfg npcCfg) {
			return null;
		}
	},

	ShieldPlayers {
		@Override
		Object load(String playerId, LMJYNpcCfg npcCfg) {
			return null;
		}
	},

	BanRankInfos {
		@Override
		Object load(String playerId, LMJYNpcCfg npcCfg) {
			return new HashMap<>();
		}
	},

	StorehouseBase {
		@Override
		Object load(String playerId, LMJYNpcCfg npcCfg) {
			return null;
		}
	},

	StorehouseEntities {
		@Override
		Object load(String playerId, LMJYNpcCfg npcCfg) {
			return null;
		}
	},

	StorehouseHelpEntities {
		@Override
		Object load(String playerId, LMJYNpcCfg npcCfg) {
			return null;
		}
	},

	YuriStrike {
		@Override
		Object load(String playerId, LMJYNpcCfg npcCfg) {
			return null;
		}
	},

	GuildHospice {
		@Override
		Object load(String playerId, LMJYNpcCfg npcCfg) {
			return null;
		}
	},

	SuperSoldierEntityList {
		@Override
		Object load(String playerId, LMJYNpcCfg npcCfg) {
			return new ArrayList<>();
		}

	},
	PlayerNationalPolicyEntities {
		@Override
		Object load(String playerId, LMJYNpcCfg npcCfg) {
			return new ArrayList<>();
		}
	},
	PlayerWarCollegeEntity {
		@Override
		Object load(String playerId, LMJYNpcCfg npcCfg) {
			return null;
		}

	},
	;
	/**
	 * 加载接口
	 * 
	 * @param playerId
	 * @param newly
	 * @return
	 */
	abstract Object load(String playerId, LMJYNpcCfg npcCfg);

}
