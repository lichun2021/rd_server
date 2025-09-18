package com.hawk.game.module.dayazhizhan.battleroom.player.rogue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkRand;

import com.google.common.collect.ImmutableMap;
import com.hawk.game.config.FoggyHeroCfg;
import com.hawk.game.entity.ArmyEntity;
import com.hawk.game.module.dayazhizhan.battleroom.DYZZRoomManager.DYZZCAMP;
import com.hawk.game.module.dayazhizhan.battleroom.cfg.DYZZRogueArmsusabilityWeightCfg;
import com.hawk.game.module.dayazhizhan.battleroom.cfg.DYZZRogueBaseCfg;
import com.hawk.game.module.dayazhizhan.battleroom.cfg.DYZZRoguePoolCfg;
import com.hawk.game.module.dayazhizhan.battleroom.player.DYZZPlayerHero;
import com.hawk.game.module.dayazhizhan.battleroom.player.IDYZZPlayer;
import com.hawk.game.player.hero.HeroTalent;
import com.hawk.game.player.hero.PlayerHero;
import com.hawk.game.player.hero.TalentSlot;
import com.hawk.game.protocol.Army.ArmyChangeCause;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.DYZZ.PBDYZZRogueSync;
import com.hawk.game.protocol.HP;
import com.hawk.log.LogConst.PowerChangeReason;

public class DYZZRogueCollection {
	private final int SOLDYER_CATEGORY = 6;
	private List<DYZZRogue> rogues = new ArrayList<>();
	private final IDYZZPlayer parent;

	private List<DYZZRogueArmsusability> asbList = new ArrayList<>();

	private Map<Integer, Integer> asbWeight = new HashMap<>();
	private int minWeight;
	private ImmutableMap<EffType, Integer> collectBuffMap = ImmutableMap.of();

	private Set<Integer> rolledSet = new HashSet<>();

	public DYZZRogueCollection(IDYZZPlayer parent) {
		this.parent = parent;
		init();
	}

	private void init() {
		ConfigIterator<DYZZRogueArmsusabilityWeightCfg> wit = HawkConfigManager.getInstance().getConfigIterator(DYZZRogueArmsusabilityWeightCfg.class);
		minWeight = 10000;
		for (DYZZRogueArmsusabilityWeightCfg wcfg : wit) {
			minWeight = Math.min(minWeight, wcfg.getWeight());
		}

	}

	public int getEffVal(EffType effType) {
		return collectBuffMap.getOrDefault(effType, 0);
	}

	private DYZZRogueArmsusability asbByKey(int key) {
		for (DYZZRogueArmsusability r : asbList) {
			if (r.getKey() == key) {
				return r;
			}
		}
		DYZZRogueArmsusability r = new DYZZRogueArmsusability(key);
		asbList.add(r);
		return r;

	}

	private int getAsbWeight(int armsusability) {
		return asbWeight.getOrDefault(armsusability, minWeight);
	}

	private void resetAsbWeight() {
		try {
			Collections.sort(asbList, Comparator.comparingInt(DYZZRogueArmsusability::getValue).thenComparing(DYZZRogueArmsusability::getKey).reversed());
			asbWeight.clear();
			for (int i = 0; i < asbList.size(); i++) {
				DYZZRogueArmsusabilityWeightCfg wcfg = HawkConfigManager.getInstance().getConfigByKey(DYZZRogueArmsusabilityWeightCfg.class, i + 1);
				DYZZRogueArmsusability r = asbList.get(i);
				asbWeight.put(r.getKey(), wcfg.getWeight());
			}

		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	public void select(int index, int selected) {
		DYZZRogue rogue = rogues.get(index);
		if (rogue.getSelected() != 0) {
			HawkLog.errPrintln("rogue has selected index{} selected{}", index, rogue.getSelected());
		}
		if (!rogue.getRogueIds().contains(selected)) {
			HawkLog.errPrintln("rogue has not selected value {}", rogue.getRogueIds());
		}
		rogue.setSelected(selected);
		// 效果
		DYZZRogueBaseCfg baseCfg = rogue.getCfg();
		for (int key : baseCfg.getArmsusabilityList()) {
			DYZZRogueArmsusability r = asbByKey(key);
			r.incValue();
		}
		resetAsbWeight();

		// 给兵
		if (!baseCfg.getSoldierMap().isEmpty()) {
			List<ArmyEntity> armyList = getParent().getData().getArmyEntities();
			Map<Integer, Integer> map = new HashMap<Integer, Integer>();
			for (ArmyEntity armyEntity : armyList) {
				int free = baseCfg.getSoldierMap().getOrDefault(armyEntity.getArmyId(), 0);
				if (free > 0) {
					armyEntity.addFree(free);
					map.put(armyEntity.getArmyId(), free);
				}
			}

			// player.getPush().syncArmyInfo(ArmyChangeCause.SOLDIER_COLLECT, armyIdList.toArray(new Integer[0]));
			// 同步兵种数量变化信息
			getParent().getPush().syncArmyInfo(ArmyChangeCause.SOLDIER_COLLECT, map);
			getParent().refreshPowerElectric(PowerChangeReason.AWARD_SOLDIER);
		}
		if (baseCfg.getHeroData() > 0) {
			FoggyHeroCfg cfg = HawkConfigManager.getInstance().getConfigByKey(FoggyHeroCfg.class, baseCfg.getHeroData());
			PlayerHero hero = getParent().getHeroByCfgId(cfg.getHeroId()).orElse(null);
			if (hero == null) {
				hero = DYZZPlayerHero.create(getParent(), cfg);
				getParent().getPlayerData().getHeroEntityList().add(hero.getHeroEntity());
			}

			if (baseCfg.getHeroOffice() > 0) {
				hero.officeAppoint(baseCfg.getHeroOffice());
			}

			if (!baseCfg.getTalentList().isEmpty()) {
				Optional<TalentSlot> talentOp = hero.getTalentSlotByIndex(0);
				if (!talentOp.isPresent()) {
					return;
				}
				TalentSlot talentSlot = talentOp.get();

				talentSlot.setUnlock(true);
				int talentId = hero.getConfig().getPassiveTalent();
				talentSlot.setTalent(new HeroTalent(talentId));
				talentSlot.getTalent().addExp(10000);
			}

			int talentIndex = 0;
			for (int talent : baseCfg.getTalentList()) {
				talentIndex++;
				Optional<TalentSlot> talentOp = hero.getTalentSlotByIndex(talentIndex);
				if (!talentOp.isPresent()) {
					continue;
				}
				TalentSlot slot = talentOp.get();
				slot.setUnlock(true);
				slot.setTalent(new HeroTalent(talent));
				slot.getTalent().addExp(10000);
			}


			hero.notifyChange();
		}
		
		int col = baseCfg.getCollect();
		if (col > 0) {
			getParent().incrementCollectHonor(col);
			if (getParent().getCamp() == DYZZCAMP.A) {
				getParent().getParent().campAOrder += col;
			} else {
				getParent().getParent().campBOrder += col;
			}
		}
	}

	public void rogueOnce(DYZZRogueType type, int param) {
		try {
			DYZZRoguePoolCfg pcfg = getParent().getParent().randomRoguePool(type, param);
			DYZZRogue buff = randBuff(pcfg);
			buff.setIndex(rogues.size());
			rogues.add(buff);
			DYZZRogue result = randSoldier(pcfg);
			result.setIndex(rogues.size());
			rogues.add(result);
			sync();
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	private DYZZRogue randSoldier(DYZZRoguePoolCfg pcfg) {
		// 8兵种的再来一次
		DYZZRogue result = new DYZZRogue();
		for (int i = 0; i < 3; i++) {// 666
			int item = randOne(pcfg.getQuality(), SOLDYER_CATEGORY);
			rolledSet.add(item);
			result.getRogueIds().add(item);

		}
		rolledSet.removeAll(result.getRogueIds());
		return result;
	}

	private DYZZRogue randBuff(DYZZRoguePoolCfg pcfg) {

		DYZZRogue result = new DYZZRogue();
		for (int cat : pcfg.getCategoryList()) {// 443
			int item = randOne(pcfg.getQuality(), cat);
			rolledSet.add(item);
			result.getRogueIds().add(item);
		}
		return result;
	}

	private int randOne(int quality, int cat) {
		ConfigIterator<DYZZRogueBaseCfg> bit = HawkConfigManager.getInstance().getConfigIterator(DYZZRogueBaseCfg.class);
		List<DYZZRogueRandItem> allrogue = new LinkedList<>();
		for (DYZZRogueBaseCfg bcfg : bit) {
			if (rolledSet.contains(bcfg.getId())) {
				continue;
			}
			if (bcfg.getCategory() != cat) {
				continue;
			}
			if (quality > 0 && bcfg.getQuality() != quality) {
				continue;
			}
			for (int armsusability : bcfg.getArmsusabilityList()) {
				allrogue.add(new DYZZRogueRandItem(bcfg.getId(), getAsbWeight(armsusability)));
			}

		}
		DYZZRogueRandItem item = HawkRand.randomWeightObject(allrogue);
		return item.getBaseId();
	}

	public void sync() {
		PBDYZZRogueSync.Builder syncb = genPBObj();
		parent.sendProtocol(HawkProtocol.valueOf(HP.code2.DYZZ_ROGUE_SYNC_S, syncb));
	}

	private PBDYZZRogueSync.Builder genPBObj() {
		PBDYZZRogueSync.Builder syncb = PBDYZZRogueSync.newBuilder();
		for (DYZZRogue rogue : rogues) {
			syncb.addRogues(rogue.genPBObj());
		}
		return syncb;
	}

	public List<Integer> getAllRogueSelected() {
		return rogues.stream().filter(r -> r.getSelected() > 0).map(r -> r.getSelected()).collect(Collectors.toList());
	}

	public void notifyChange() {

		EffType[] arr = collectBuffMap.keySet().toArray(new EffType[0]);
		getParent().getPlayerPush().syncPlayerEffect(arr);

		sync();
	}

	public IDYZZPlayer getParent() {
		return parent;
	}

	public void ontick() {
		Map<EffType, Integer> map = new HashMap<>();

		for (DYZZRogue rogue : rogues) {
			DYZZRogueBaseCfg bcfg = HawkConfigManager.getInstance().getConfigByKey(DYZZRogueBaseCfg.class, rogue.getSelected());
			if (bcfg != null && !bcfg.getCollectBuffMap().isEmpty()) {
				for (Entry<EffType, int[]> ent : bcfg.getCollectBuffMap().entrySet()) {
					EffType key = ent.getKey();
					int value = effVal(key, ent.getValue());
					map.merge(key, value, (v1, v2) -> v1 + v2);
				}
			}
		}

		this.collectBuffMap = ImmutableMap.copyOf(map);

	}

	private int effVal(EffType key, int[] params) {
		int value = params[1];
		switch (key) {
		case DYZZ_9015: {
			int teamOrder = getParent().getParent().getCampOrder(getParent().getCamp());
			value = Math.min(teamOrder / params[2] * params[3], params[4]);
			break;
		}
		case DYZZ_9016:
		case DYZZ_9017:
		case DYZZ_9018: {
			long gametime = getParent().getParent().getCurTimeMil() - getParent().getParent().getCreateTime();
			value = (int) Math.min(gametime / 60000 * params[2], params[3]);
			break;
		}
		case DYZZ_9019:
		case DYZZ_9020:
		case DYZZ_9021: {
			// 9019_0_100000_15000_100000_7500_75000
			int v9019 = getParent().getKillCount() / params[2] * params[3] - getParent().getHurtCount() / params[4] * params[5];
			value = (int) Math.min(v9019, params[6]);
			break;
		}
		default:
			break;
		}

		return Math.max(0, value);
	}

}
