package com.hawk.game.module.lianmengtaiboliya.worldpoint;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;

import com.hawk.game.config.FoggyFortressCfg;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.module.lianmengtaiboliya.TBLYBattleRoom;
import com.hawk.game.module.lianmengtaiboliya.TBLYRoomManager.CAMP;
import com.hawk.game.module.lianmengtaiboliya.cfg.TBLYCommandPostCfg;
import com.hawk.game.module.lianmengtaiboliya.cfg.TBLYNpcCfg;
import com.hawk.game.module.lianmengtaiboliya.npc.TBLYNpcPlayer;
import com.hawk.game.module.lianmengtaiboliya.order.TBLYOrderCollection;
import com.hawk.game.module.lianmengtaiboliya.player.ITBLYPlayer;
import com.hawk.game.module.lianmengtaiboliya.player.TBLYPlayer;
import com.hawk.game.module.lianmengtaiboliya.worldmarch.ITBLYWorldMarch;
import com.hawk.game.module.lianmengtaiboliya.worldmarch.TBLYBuildingMarchSingleNpc;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.protocol.World.WorldPointDetailPB;
import com.hawk.game.protocol.World.WorldPointDetailPB.Builder;
import com.hawk.game.protocol.World.WorldPointPB;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.util.EffectParams;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.RandomUtil;
import com.hawk.game.world.object.FoggyInfo;

/**
 * 指挥部
 *
 */
public class TBLYCommandPost extends ITBLYBuilding {
	private long nextMarch;

	public TBLYCommandPost(TBLYBattleRoom parent) {
		super(parent);
	}

	public static TBLYCommandPostCfg getCfg() {
		return HawkConfigManager.getInstance().getKVInstance(TBLYCommandPostCfg.class);
	}

	@Override
	public WorldPointType getPointType() {
		return WorldPointType.TBLY_COMMAND_POST;
	}

	@Override
	public boolean onTick() {
		super.onTick();

		final long oldmarchtime = nextMarch;
		marchTick();
		if (oldmarchtime != nextMarch) {
			getParent().worldPointUpdate(this);
		}

		return true;
	}

	@Override
	public WorldPointPB.Builder toBuilder(ITBLYPlayer viewer) {
		WorldPointPB.Builder result = super.toBuilder(viewer);
		result.setTblyNextNpcMarch(nextMarch);
		return result;
	}

	@Override
	public WorldPointDetailPB.Builder toDetailBuilder(ITBLYPlayer viewer) {
		WorldPointDetailPB.Builder result = super.toDetailBuilder(viewer);
		result.setTblyNextNpcMarch(nextMarch);
		return result;
	}

	private void marchTick() {

		if (nextMarch == 0) {
			nextMarch = getParent().getCreateTime() + getCfg().getNpcMarchFirst() * 1000;
		}
		if (getState() == TBLYBuildState.ZHAN_LING_ZHONG) {
			nextMarch = Long.MAX_VALUE;
		}
		if (getState() != TBLYBuildState.ZHAN_LING_ZHONG && nextMarch == Long.MAX_VALUE) {
			nextMarch = getParent().getCurTimeMil() + getCfg().getNpcMarchInterval() * 1000;
		}

		if (nextMarch > getParent().getCurTimeMil()) {
			return;
		}

		CAMP npcCamp = getState() == TBLYBuildState.ZHAN_LING ? getGuildCamp() : CAMP.NONE;
		List<ITBLYBuilding> buildList = getParent().getTBLYBuildingList().stream()
				.filter(b -> b.getState() != TBLYBuildState.ZHONG_LI)
				.filter(b -> b.getGuildCamp() != npcCamp)
				.collect(Collectors.toList());
		buildList.remove(this);
		if (buildList.isEmpty()) {
			nextMarch = getParent().getCurTimeMil() + getCfg().getNpcMarchInterval() * 1000;
			return;
		}

		ConfigIterator<TBLYNpcCfg> npcit = HawkConfigManager.getInstance().getConfigIterator(TBLYNpcCfg.class);
		TBLYNpcCfg npcCfg = RandomUtil.randomWeightObject(npcit.toList());
		String guildName = npcCfg.getGuildName();
		String guildTag = npcCfg.getGuildTag();
		String guildId = "大老A";
		if (npcCamp != CAMP.NONE) {
			guildName = getParent().getCampGuildName(npcCamp);
			guildTag = getParent().getCampGuildTag(npcCamp);
			guildId = getParent().getCampGuild(npcCamp);
		}

		ITBLYBuilding tPoint = RandomUtil.randomWeightObject(buildList);
		ITBLYWorldMarch leaderMarch = tPoint.getLeaderMarch();

		TBLYNpcPlayer npc = createNpc(npcCamp, npcCfg, guildName, guildTag, guildId, (TBLYPlayer) leaderMarch.getParent());
		EffectParams effParams = createNpcEffaParams(npcCfg, leaderMarch);

		TBLYBuildingMarchSingleNpc march = (TBLYBuildingMarchSingleNpc) getParent().startMarch(npc, this, tPoint, WorldMarchType.TBLY_HEADQUARTERS_SINGLE, "", 0, effParams);

		List<ITBLYWorldMarch> defMarchList = getParent().getPointMarches(tPoint.getPointId(), WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED);
		defMarchList.remove(leaderMarch);
		for (ITBLYWorldMarch defMarch : defMarchList) {
			TBLYNpcPlayer jnpc = createNpc(npcCamp, npcCfg, guildName, guildTag, guildId, (TBLYPlayer) defMarch.getParent());
			EffectParams jeffParams = createNpcEffaParams(npcCfg, defMarch);
			ITBLYWorldMarch jmarch = getParent().genMarch(jnpc, this, tPoint, WorldMarchType.TBLY_HEADQUARTERS_SINGLE, "", 0, jeffParams);
			march.getMassJoinMarchs(true).add(jmarch);
		}
		march.pushAttackReport();

		nextMarch = getParent().getCurTimeMil() + getCfg().getNpcMarchInterval() * 1000;
	}

	private EffectParams createNpcEffaParams(TBLYNpcCfg npcCfg, ITBLYWorldMarch leaderMarch) {
		EffectParams effParams = EffectParams.copyOf(leaderMarch.getMarchEntity().getEffectParams());
		List<ArmyInfo> armyCopy = leaderMarch.getMarchEntity().getArmyCopy();
		List<ArmyInfo> armyList = new ArrayList<>(armyCopy.size());
		for(ArmyInfo army : armyCopy){
			int count = (int) (army.getFreeCnt() * GsConst.EFF_PER * npcCfg.getMirror());
			count = Math.max(1, count);
			armyList.add(new ArmyInfo(army.getArmyId(), count));
		}
		effParams.setArmys(armyList);
		return effParams;
	}

	private TBLYNpcPlayer createNpc(CAMP npcCamp, TBLYNpcCfg npcCfg, String guildName, String guildTag, String guildId, TBLYPlayer source) {
		TBLYNpcPlayer npc = new TBLYNpcPlayer(source);
		npc.setPos(GameUtil.splitXAndY(getPointId()));
		npc.setParent(getParent());
		npc.init();
		npc.setCamp(npcCamp);
		npc.setCfgId(npcCfg.getId());
		npc.setName(npcCfg.getPlayerName());
		npc.setGuildName(guildName);
		npc.setGuildTag(guildTag);
		npc.setGuildId(guildId);
		npc.setIcon(npcCfg.getIcon());
		return npc;
	}

	public long getNextMarch() {
		return nextMarch;
	}

	public void setNextMarch(long nextMarch) {
		this.nextMarch = nextMarch;
	}

	@Override
	public int getControlCountDown() {
		return getCfg().getControlCountDown();
	}

	@Override
	public double getGuildHonorPerSecond() {
		int beiShu = getParent().isHotBloodModel() ? 2 : 1;
		if (getShowOrder().containsKey(TBLYOrderCollection.shuangbeijifen)) {
			beiShu *= 2;
		}
		return getCfg().getGuildHonor() * beiShu;
	}

	@Override
	public double getPlayerHonorPerSecond() {
		int beiShu = getParent().isHotBloodModel() ? 2 : 1;
		if (getShowOrder().containsKey(TBLYOrderCollection.shuangbeijifen)) {
			beiShu *= 2;
		}
		return getCfg().getHonor() * beiShu;
	}

	@Override
	public double getFirstControlGuildHonor() {
		int beiShu = getParent().isHotBloodModel() ? 2 : 1;
		return getCfg().getFirstControlGuildHonor() * beiShu;
	}

	@Override
	public double getFirstControlPlayerHonor() {
		int beiShu = getParent().isHotBloodModel() ? 2 : 1;
		return getCfg().getFirstControlHonor() * beiShu;
	}

	@Override
	public int getProtectTime() {
		return getCfg().getProtectTime();
	}

	@Override
	public int getCollectArmyMin() {
		return getCfg().getCollectArmyMin();
	}

	@Override
	public int getPointTime() {
		return getCfg().getPointTime();
	}

	@Override
	public double getPointBase() {
		return getCfg().getPointBase();
	}

	@Override
	public double getPointSpeed() {
		return getCfg().getPointSpeed();
	}

	@Override
	public double getPointMax() {
		return getCfg().getPointMax();
	}
}
