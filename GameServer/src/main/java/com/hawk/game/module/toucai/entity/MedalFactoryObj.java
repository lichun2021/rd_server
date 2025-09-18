package com.hawk.game.module.toucai.entity;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkTime;

import com.alibaba.fastjson.JSONArray;
import com.hawk.game.global.GlobalData;
import com.hawk.game.item.AwardItems;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.module.toucai.MedalFactoryService;
import com.hawk.game.module.toucai.cfg.MedalFactoryConstCfg;
import com.hawk.game.module.toucai.cfg.MedalFactoryLevelCfg;
import com.hawk.game.module.toucai.cfg.MedalFactoryRewardCfg;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.MedalFactory.HPMedalFactoryInfo;

public class MedalFactoryObj {
	boolean needSync;
	private MedalEntity dbEntity;

	/**  生产线 */
	private List<MedalCollect> collects = new ArrayList<>() ;

	/** 偷取中*/
	private List<MedalSteal> steals = new ArrayList<>();

	private List<String> enemys = new ArrayList<>();
	// 上次刷新的
	private MedalRefresh refresh = new MedalRefresh();
	
	HPMedalFactoryInfo.Builder hpBuilder;
	MedalFactoryLevelCfg levelCfg;
	List<ItemInfo> stealItemList;
	private MedalFactoryObj() {
	}
	
	public int getEffect(EffType eff){
		return getLevelCfg().getEffect(eff);
	}

	public void addEnemy(String playerId) {
		enemys.remove(playerId);
		enemys.add(playerId);
		if (enemys.size() > 100) {
			enemys.remove(0);
		}
	}

	public boolean isUnlock() {
		MedalFactoryConstCfg kvcfg = HawkConfigManager.getInstance().getKVInstance(MedalFactoryConstCfg.class);
		return kvcfg.getOpenLevel() <= getParent().getCityLevel();
	}

	public HPMedalFactoryInfo.Builder toHP() {
		if(hpBuilder !=null){
			return hpBuilder;
		}
		Player player = getParent();
		HPMedalFactoryInfo.Builder builder = HPMedalFactoryInfo.newBuilder();
		builder.setOnwerId(dbEntity.getPlayerId());
		builder.setName(player.getName());
		builder.setPficon(player.getPfIcon());
		builder.setIcon(player.getIcon());
		builder.setDaySteal(ItemInfo.toString(getStealItemList()));
		builder.setLevelCfg(getLevelCfg().getId());
		builder.setDayRaward(dbEntity.getDailyReward() != HawkTime.getYearDay());
		builder.setExp(dbEntity.getExp());
		builder.setLeyuzhuren(dbEntity.getLeyuzhuren() > 0);
		for (MedalCollect collect : collects) {
			builder.addFactory(collect.toHP());
		}
		hpBuilder = builder;
		return builder;
	}

	public static void create(MedalEntity medalEntity) {
		MedalFactoryObj factory = new MedalFactoryObj();
		factory.setDbEntity(medalEntity);
		factory.init();
		medalEntity.recordFactoryObj(factory);
	}

	private void init() {
		this.collects = initCollects();
		this.steals = initSteals();
		this.enemys = initEnemys();
		this.refresh = initRefresh();
	}

	private MedalRefresh initRefresh() {
		if (StringUtils.isEmpty(dbEntity.getRefreshStr())) {// 新英雄
			return new MedalRefresh();
		}

		MedalRefresh re = new MedalRefresh();
		re.mergeFrom(dbEntity.getRefreshStr());
		return re;
	}

	public String serializRefresh() {
		return refresh.serializ();
	}

	private List<String> initEnemys() {
		List<String> list = new LinkedList<>();
		try {
			if (StringUtils.isEmpty(dbEntity.getEnemyStr())) {// 新英雄
				return list;
			}

			JSONArray arr = JSONArray.parseArray(dbEntity.getEnemyStr());
			arr.forEach(str -> {
				list.add(str.toString());
			});

		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return list;
	}

	public String serializEnemys() {
		JSONArray arr = new JSONArray();
		for (String str : enemys) {
			arr.add(str);
		}
		return arr.toJSONString();
	}

	private List<MedalSteal> initSteals() {
		List<MedalSteal> list = new ArrayList<>();
		try {
			if (StringUtils.isEmpty(dbEntity.getStealStr())) {// 新英雄
				return list;
			}

			JSONArray arr = JSONArray.parseArray(dbEntity.getStealStr());
			arr.forEach(str -> {
				MedalSteal slot = new MedalSteal();
				slot.mergeFrom(str.toString());
				list.add(slot);
			});
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return list;
	}

	public String serializSteal() {
		JSONArray arr = new JSONArray();
		for (MedalSteal st : steals) {
			arr.add(st.serializ());
		}
		return arr.toJSONString();
	}

	private List<MedalCollect> initCollects() {
		List<MedalCollect> list = new ArrayList<>();
		try {
			if (StringUtils.isEmpty(dbEntity.getCollectStr())) {// 新英雄
				MedalFactoryConstCfg kvcfg = HawkConfigManager.getInstance().getKVInstance(MedalFactoryConstCfg.class);
				for (int i = 0; i < kvcfg.getMaxProductionNum(); i++) {
					MedalCollect slot = new MedalCollect(this);
					slot.setIndex(i);
					list.add(slot);
				}

				return list;
			}

			JSONArray arr = JSONArray.parseArray(dbEntity.getCollectStr());
			arr.forEach(str -> {
				MedalCollect slot = new MedalCollect(this);
				slot.mergeFrom(str.toString());
				list.add(slot);
			});

		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return list;
	}

	public String serializCollect() {
		JSONArray arr = new JSONArray();
		for (MedalCollect col : collects) {
			arr.add(col.serializ());
		}
		return arr.toJSONString();
	}

	public int level() {
		ConfigIterator<MedalFactoryLevelCfg> it = HawkConfigManager.getInstance().getConfigIterator(MedalFactoryLevelCfg.class);
		int result = 1;
		int maxLevel = 1;
		int maxExp = 0;
		for (MedalFactoryLevelCfg cfg : it) {
			maxLevel = Math.max(maxLevel, cfg.getLevel());
			maxExp = Math.max(maxExp, cfg.getExp());
			if (dbEntity.getExp() >= cfg.getExp()) {
				result = cfg.getLevel() + 1;
			}
		}
		if (dbEntity.getExp() > maxExp) {
			dbEntity.setExp(maxExp);
		}
		
		return Math.min(result, maxLevel);
	}

	public MedalFactoryLevelCfg getLevelCfg() {
		if (levelCfg == null) {
			levelCfg = HawkConfigManager.getInstance().getConfigByKey(MedalFactoryLevelCfg.class, level());
		}
		return levelCfg;
	}

	public Player getParent() {
		return GlobalData.getInstance().makesurePlayer(dbEntity.getPlayerId());
	}

	public void sync() {
		getParent().sendProtocol(HawkProtocol.valueOf(HP.code2.TOUCAI_SYNC, toHP()));
		needSync = false;
	}

	public MedalCollect getCollect(int index) {
		for (MedalCollect collect : collects) {
			if (collect.getIndex() == index) {
				return collect;
			}
		}
		return null;
	}

	public MedalEntity getDbEntity() {
		return dbEntity;
	}

	public void setDbEntity(MedalEntity dbEntity) {
		this.dbEntity = dbEntity;
	}

	public List<MedalCollect> getCollects() {
		return collects;
	}

	public void setCollects(List<MedalCollect> collects) {
		this.collects = collects;
	}

	public List<MedalSteal> getSteals() {
		return steals;
	}

	public void setSteals(List<MedalSteal> steals) {
		this.steals = steals;
	}

	public void notifyChange() {
		boolean canSteal = false;
		for (MedalCollect c : collects) {
			if (c.getRewardCfgId() > 0 && c.getStealed().size() < c.getRewardCfg().getStealNumMax()) {
				canSteal = true;
				break;
			}
		}
		dbEntity.setCanSteal(canSteal ? 1 : 0);
		if (canSteal) {
			MedalFactoryService.getInstance().putCanSteal(dbEntity.getPlayerId());
		} else {
			MedalFactoryService.getInstance().removeCanSteal(dbEntity.getPlayerId());
		}
		needSync = true;
		hpBuilder = null;
		levelCfg = null;
		stealItemList = null;
	}

	public List<String> getEnemys() {
		return enemys;
	}

	public MedalRefresh getRefresh() {
		return refresh;
	}

	public boolean isNeedSync() {
		return needSync;
	}

	public void setNeedSync(boolean needSync) {
		this.needSync = needSync;
	}
	

	public List<ItemInfo> getStealItemList() {
		if (stealItemList == null) {
			List<ItemInfo> list = new ArrayList<>();
			try {
				list.addAll(ItemInfo.valueListOf(getDbEntity().getStealTodayStr()));
				for (MedalSteal ste : steals) {
					MedalFactoryRewardCfg reward = HawkConfigManager.getInstance().getConfigByKey(MedalFactoryRewardCfg.class, ste.getRewardCfgId());
					// 可偷物品
					list.addAll(ItemInfo.valueListOf(reward.getStealReward()));
				}

			} catch (Exception e) {
				HawkException.catchException(e);
			}
			AwardItems aw = AwardItems.valueOf();
			aw.addItemInfos(list);
			stealItemList = aw.getAwardItems();
		}
		return stealItemList;
	}

	public Map<Integer, Long> getStealCnt() {
		List<ItemInfo> list = getStealItemList();

		Map<Integer, Long> map = list.stream().collect(Collectors.toMap(ItemInfo::getItemId, ItemInfo::getCount));
		return map;
	}

}
