package com.hawk.game.guild.manor.building;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.collections4.map.LRUMap;
import org.hawk.app.HawkApp;
import org.hawk.config.HawkConfigManager;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkTime;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hawk.game.config.GuildManorTowerCfg;
import com.hawk.game.entity.GuildBuildingEntity;
import com.hawk.game.global.GlobalData;
import com.hawk.game.guild.manor.GuildBuildingStat;
import com.hawk.game.guild.manor.GuildManorObj;
import com.hawk.game.guild.manor.ManorBastionStat;
import com.hawk.game.guild.manor.building.model.TowerDamageInfo;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const.TerritoryType;
import com.hawk.game.protocol.GuildManor.GuildBuildingNorStat;
import com.hawk.game.protocol.GuildManor.GuildManorList.Builder;
import com.hawk.game.protocol.GuildManor.GuildTowerBase;
import com.hawk.game.protocol.GuildManor.ManorBartizanDamageInfo;
import com.hawk.game.protocol.GuildManor.ManorBartizanDamageList;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.service.GuildManorService;
import com.hawk.game.util.AlgorithmPoint;
import com.hawk.game.util.LogUtil;
import com.hawk.game.util.WorldUtil;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.march.IWorldMarch;
import com.hawk.game.world.service.WorldPointService;
import com.hawk.log.LogConst.GuildAction;

/**
 * 联盟箭塔
 * 
 * @author zhenyu.shang
 * @since 2017年7月7日
 */
public class GuildManorTower extends AbstractGuildBuilding {

	public final static int RADIUS = 1;

	public final static String DAMAGE_KEY = "1";

	public final static String MARCHID_KEY = "2";

	private BlockingQueue<String> canAttackMarch;

	private Map<String, TowerDamageInfo> damageInfos;
	
	private long lastAtkTime;
	private long lastCheckTime = 0;
	public GuildManorTower(GuildBuildingEntity entity, TerritoryType buildType) {
		super(entity, buildType);
		canAttackMarch = new LinkedBlockingQueue<String>();
		damageInfos = new LRUMap<String, TowerDamageInfo>(10);
	}

	public GuildManorTowerCfg getCfg(){
		return HawkConfigManager.getInstance().getConfigByKey(GuildManorTowerCfg.class, getEntity().getBuildingId());
	}
	
	@Override
	public void tick() {
		checkUkock();
		super.tick();
	}

	/** 问题描述：玩家反馈联盟堡垒数量是满的，也都是满建筑值，但是联盟守备塔无法建立，辛苦大佬看下 */
	private void checkUkock() {
		try {
			long currentTime = HawkApp.getInstance().getCurrentTime();
			if (currentTime - lastCheckTime > 0123_4567) {
				lastCheckTime = currentTime;
				int manorCount = 0;
				List<GuildManorObj> manors = GuildManorService.getInstance().getGuildManors(getEntity().getGuildId());
				for (GuildManorObj manor : manors) {
					if (manor.getBastionStat() == ManorBastionStat.LOCKED || manor.getBastionStat() == ManorBastionStat.OPENED) {
						continue;
					}
					manorCount++;
				}
				if (getBuildStat() == GuildBuildingStat.LOCKED && getCfg().getManorCount() <= manorCount) {
					tryChangeBuildStat(GuildBuildingStat.OPENED.getIndex());
				}
			}

		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	@Override
	public void addProtocol2Builder(Builder builder) {
		GuildTowerBase.Builder towerBuilder = GuildTowerBase.newBuilder();
		towerBuilder.setTowerId(getEntity().getBuildingId());
		towerBuilder.setBuildLife((int) getEntity().getBuildLife());
		towerBuilder.setLevel(getEntity().getLevel());
		towerBuilder.setOverTime(getOverTime());
		towerBuilder.setStat(GuildBuildingNorStat.valueOf(getEntity().getBuildingStat()));
		towerBuilder.setX(getEntity().getPosX());
		towerBuilder.setY(getEntity().getPosY());

		builder.addAllTower(towerBuilder.build());
	}

	@Override
	public void parseBuildingParam(String buildParam) {
		if (buildParam != null) {
			JSONObject json = JSONObject.parseObject(buildParam);
			if (!json.isEmpty()) {
				JSONArray marchIds = json.getJSONArray(MARCHID_KEY);
				if (marchIds != null) {
					for (Object object : marchIds) {
						this.canAttackMarch.add((String) object);
					}
				}
				JSONObject damageJson = json.getJSONObject(DAMAGE_KEY);
				if (damageJson != null) {
					for (String key : damageJson.keySet()) {
						JSONObject obj = damageJson.getJSONObject(key);
						if (obj != null) {
							TowerDamageInfo info = obj.toJavaObject(TowerDamageInfo.class);
							damageInfos.put(key, info);
						}
					}
				}
			}
		}
	}

	@Override
	public String genBuildingParamStr() {
		JSONObject json = new JSONObject();
		if (!canAttackMarch.isEmpty()) {
			List<String> marchIds = new ArrayList<String>();
			for (String marchId : canAttackMarch) {
				marchIds.add(marchId);
			}
			json.put(MARCHID_KEY, marchIds);
		}
		if (!damageInfos.isEmpty()) {
			json.put(DAMAGE_KEY, damageInfos);
		}
		return json.toJSONString();
	}

	@Override
	public int getBuildingUpLimit() {
		return getCfg().getBuildingUpLimit();
	}

	@Override
	public boolean onBuildRemove() {
		canAttackMarch.clear();
		damageInfos.clear();
		LogUtil.logGuildAction(GuildAction.GUILD_TOWER_REMOVE, getGuildId());
		return super.onBuildRemove();
	}

	@Override
	public boolean onBuildComplete() {
		LogUtil.logGuildAction(GuildAction.GUILD_BUILD_TOWER_COMPLETE, getGuildId());
		return true;
	}
	
	public void addCanAttackMarch(String marchId) {
		this.canAttackMarch.add(marchId);
		getEntity().setChanged(true);
	}

	public void clearCanAttackMarch() {
		this.canAttackMarch.clear();
		getEntity().setChanged(true);
	}

	public void removeAttackMarch(String marchId) {
		this.canAttackMarch.remove(marchId);
		getEntity().setChanged(true);
	}

	@Override
	public void heartBeats(long interval) {
		long now = HawkTime.getMillisecond();
		GuildManorTowerCfg cfg = getCfg();
		long atkPeriod = cfg.getAtkPeriod() * 1000;
		if (now - lastAtkTime < atkPeriod) {
			return;
		}
		lastAtkTime = now;
		List<ManorBartizanDamageInfo> damageInfos = new ArrayList<ManorBartizanDamageInfo>();
		for (String marchId : canAttackMarch) {
			try {
				IWorldMarch march = WorldMarchService.getInstance().getMarch(marchId);
				if(march == null){
					removeAttackMarch(marchId);
					continue;
				}
				//判断行军类型，不对则直接移除
				if(!WorldUtil.canBeAttackedByBartizan(march.getMarchEntity())){
					removeAttackMarch(marchId);
					continue;
				}
				//行军状态不对，直接移除
				if(march.getMarchEntity().getMarchStatus() != WorldMarchStatus.MARCH_STATUS_MARCH_VALUE){
					removeAttackMarch(marchId);
					continue;
				}
				// 判断当前ponit是否可以被攻击
				int targetPointId = march.getMarchEntity().getTerminalId(); // 目标点
				WorldPoint targetPoint = WorldPointService.getInstance().getWorldPoint(targetPointId);
				if (targetPoint == null || targetPoint.getPointType() == WorldPointType.EMPTY_VALUE) { // 空点不添加
					removeAttackMarch(marchId);
					continue;
				}
				//判断目标点是否被箭塔保护
				if(!GuildManorService.getInstance().getBartizansCanBeAttacked(march.getPlayerId(), targetPoint, getGuildId())){
					removeAttackMarch(marchId);
					continue;
				}
				// 行军当前位置
				AlgorithmPoint currPoint = WorldUtil.getMarchCurrentPosition(march.getMarchEntity());
				// 判断当前位置是否可以攻击
				if (GuildManorService.getInstance().isInBartizanArea(getPositionId(), cfg.getTowerRadius(), currPoint)) {
					Player player = GlobalData.getInstance().makesurePlayer(march.getPlayerId());
					// 可攻击则进行一次攻击,并记录攻击结果,添加数据
					List<ArmyInfo> armys = march.getMarchEntity().getArmys();
					Map<Integer, Integer> res = new HashMap<Integer, Integer>();
					Map<Integer, Integer> resStar = new HashMap<Integer, Integer>();
					int sumKill = 0;
					for (ArmyInfo armyInfo : armys) {
						int kill = armyInfo.killByTower(cfg.getTowerAttack());
						res.put(armyInfo.getArmyId(), kill);
						resStar.put(armyInfo.getArmyId(), player.getSoldierStar(armyInfo.getArmyId()));
						sumKill += kill;
					}
					addDamageInfo(march, res, resStar, player);
					//添加联盟箭塔伤害
					ManorBartizanDamageInfo.Builder builder = ManorBartizanDamageInfo.newBuilder();
					builder.setMarchId(march.getMarchId());
					builder.setDeadAmry(sumKill);
					builder.setX((int) currPoint.getX());
					builder.setY((int) currPoint.getY());
					damageInfos.add(builder.build());
				}
			} catch (Exception e) {
				HawkException.catchException(e, marchId);
			}
		}
		if(!damageInfos.isEmpty()){
			ManorBartizanDamageList.Builder builder = ManorBartizanDamageList.newBuilder();
			builder.setBartizanIndex(getEntity().getBuildingId());
			builder.addAllDamageInfo(damageInfos);
			builder.setX(getEntity().getPosX());
			builder.setY(getEntity().getPosY());
			//广播消息
			WorldPointService.getInstance().broadcastScene(getEntity().getPosX(), getEntity().getPosY(), HawkProtocol.valueOf(HP.code.MANOR_BRATIZAN_DAMAGE_S, builder));
		}
	}

	/**
	 * 将伤害值添加至列表
	 * 
	 * @param march
	 * @param res
	 * @param player
	 */
	public void addDamageInfo(IWorldMarch march, Map<Integer, Integer> res, Map<Integer, Integer> resStar, Player player) {
		TowerDamageInfo info = null;
		if (damageInfos.containsKey(march.getMarchId())) {
			info = damageInfos.get(march.getMarchId());
			info.mergeDamageInfo(res, resStar);
		} else {
			info = new TowerDamageInfo();
			info.setName(player.getName());
			info.setPlayerId(player.getId());
			info.setPfIcon(player.getPfIcon());
			info.setIcon(player.getIcon());
			String tag = player.getGuildTag();
			info.setGuildTag(tag == null ? "" : tag);
			info.getDamage().putAll(res);
			info.getDamageStar().putAll(resStar);
			this.damageInfos.put(march.getMarchId(), info);
		}
		march.getMarchEntity().addTowerKillInfo(info.getDamage());
		getEntity().setChanged(true);
	}

	public Map<String, TowerDamageInfo> getDamageInfos() {
		return damageInfos;
	}
	
	@Override
	public int getbuildLimtUp() {
		return getCfg().getBuildingUpLimit();
	}
}
