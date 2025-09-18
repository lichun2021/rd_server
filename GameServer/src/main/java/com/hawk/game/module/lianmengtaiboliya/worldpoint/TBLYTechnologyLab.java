package com.hawk.game.module.lianmengtaiboliya.worldpoint;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkException;
import org.hawk.os.HawkRand;
import org.hawk.tuple.HawkTuple2;

import com.hawk.game.module.lianmengtaiboliya.TBLYBattleRoom;
import com.hawk.game.module.lianmengtaiboliya.cfg.TBLYBuildSkillCfg;
import com.hawk.game.module.lianmengtaiboliya.cfg.TBLYTechnologyLabCfg;
import com.hawk.game.module.lianmengtaiboliya.order.TBLYOrderCollection;
import com.hawk.game.module.lianmengtaiboliya.player.ITBLYPlayer;
import com.hawk.game.module.lianmengtaiboliya.worldmarch.ITBLYWorldMarch;
import com.hawk.game.module.lianmengtaiboliya.worldpoint.sub.ITBLYTechnologyLabProgressState;
import com.hawk.game.module.lianmengtaiboliya.worldpoint.sub.TBLYTechnologyLabProgressStateColse;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.TBLY.PBTBLYEffect;
import com.hawk.game.protocol.TBLY.PBTBLYTechonolgyLabEffect;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldPointPB.Builder;
import com.hawk.game.protocol.World.WorldPointType;

/**
 * 指挥部
 *
 */
public class TBLYTechnologyLab extends ITBLYBuilding {
	private long lastTick;
	private long protectedEndTime;

	public int campAZhanLingPct;

	public int openCnt;
	// 进度条争夺状态
	private ITBLYTechnologyLabProgressState progressState;

	private List<PBTBLYTechonolgyLabEffect.Builder> technologyLabBuffList = new ArrayList<>(3); // 秘密试验室开局随即buff

	public TBLYTechnologyLab(TBLYBattleRoom parent) {
		super(parent);

	}

	public void init() {
		protectedEndTime = getZhengDouKaishiTime();
		progressState = new TBLYTechnologyLabProgressStateColse(this);
		((TBLYTechnologyLabProgressStateColse) progressState).setOpenTime(protectedEndTime);

		campAZhanLingPct = getCfg().getTotalPoint() / 2;

		List<TBLYBuildSkillCfg> list = HawkConfigManager.getInstance().getConfigIterator(TBLYBuildSkillCfg.class).stream().filter(cfg -> cfg.getShowType() == 2)
				.collect(Collectors.toCollection(ArrayList::new));
		for (int i = 0; i < getCfg().getOpenTimeList().size(); i++) {
			HawkTuple2<Integer, Integer> tup = getCfg().getOpenTimeList().get(i);
			long start = tup.first * 60 * 1000 + getParent().getCreateTime();
			long end = tup.second * 60 * 1000 + getParent().getCreateTime();

			TBLYBuildSkillCfg cfg = HawkRand.randomObject(list);
			PBTBLYTechonolgyLabEffect.Builder buf = PBTBLYTechonolgyLabEffect.newBuilder();
			for (Entry<EffType, Integer> eff : cfg.getControleBuffMap().entrySet()) {
				buf.addEffect(PBTBLYEffect.newBuilder().setEffId(eff.getKey().getNumber()).setEffVal(eff.getValue()));
			}
			buf.setSkillId(cfg.getId());
			buf.setIndex(i);
			buf.setStartTime(start);
			buf.setEndTime(end);
			technologyLabBuffList.add(buf);
			list.remove(cfg);
		}
	}

	public int nextBuff() {
		for (PBTBLYTechonolgyLabEffect.Builder buf : getTechnologyLabBuffList()) {
			if (buf.getCamp() == 0) {
				return buf.getSkillId();
			}
		}
		return 0;
	}

	@Override
	public boolean onTick() {
		super.onTick();
		progressState.onTick();

		return true;
	}

	@Override
	public int getControlBuff(ITBLYPlayer player, EffType effType) {
		int result = 0;
		try {
			for (PBTBLYTechonolgyLabEffect.Builder buf : technologyLabBuffList) {
				if (buf.getCamp() != player.getCamp().intValue()) {
					continue;
				}
				for (PBTBLYEffect eff : buf.getEffectList()) {
					if (eff.getEffId() == effType.getNumber()) {
						result += eff.getEffVal();
					}
				}
			}
		} catch (Exception e) {
		}
		return result;
	}

	@Override
	public Builder toBuilder(ITBLYPlayer viewer) {
		// TODO Auto-generated method stub
		return super.toBuilder(viewer).setProtectedEndTime(protectedEndTime).setTblyCampAZhanLingPct(campAZhanLingPct).setTblyTotalPointPct(getCfg().getTotalPoint());
	}

	@Override
	public com.hawk.game.protocol.World.WorldPointDetailPB.Builder toDetailBuilder(ITBLYPlayer viewer) {
		// TODO Auto-generated method stub
		return super.toDetailBuilder(viewer).setProtectedEndTime(protectedEndTime).setTblyCampAZhanLingPct(campAZhanLingPct).setTblyTotalPointPct(getCfg().getTotalPoint());
	}

	public long getZhengDouKaishiTime() {
		long now = getParent().getCurTimeMil();
		for (HawkTuple2<Integer, Integer> tup : getCfg().getOpenTimeList()) {
			long start = tup.first * 60 * 1000 + getParent().getCreateTime();
			long end = tup.second * 60 * 1000 + getParent().getCreateTime();
			if (start < now && now < end) { // 开放中
				return start;
			}
			if (start > now) {
				return start;
			}
		}

		return Long.MAX_VALUE;
	}

	public long getNextZhengDouKaishiTime() {
		long now = getParent().getCurTimeMil();
		for (HawkTuple2<Integer, Integer> tup : getCfg().getOpenTimeList()) {
			long start = tup.first * 60 * 1000 + getParent().getCreateTime();
			if (start > now) {
				return start;
			}
		}
		return Long.MAX_VALUE;
	}

	public void cleanGuildMarch(String guildId) {
		try {
			List<ITBLYWorldMarch> pms = getParent().getPointMarches(this.getPointId());
			for (ITBLYWorldMarch march : pms) {
				if (StringUtils.isNotEmpty(guildId) && !Objects.equals(guildId, march.getParent().getGuildId())) {
					continue;
				}

				if (march.isMassMarch() && march.getMarchStatus() == WorldMarchStatus.MARCH_STATUS_WAITING_VALUE) {
					march.getMassJoinMarchs(true).forEach(jm -> jm.onMarchCallback());
					march.onMarchBack();
				} else {
					march.onMarchCallback();
				}
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	public static TBLYTechnologyLabCfg getCfg() {
		return HawkConfigManager.getInstance().getKVInstance(TBLYTechnologyLabCfg.class);
	}

	@Override
	public WorldPointType getPointType() {
		return WorldPointType.TBLY_TECHNOLOGY_LAB;
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

	public List<PBTBLYTechonolgyLabEffect.Builder> getTechnologyLabBuffList() {
		return technologyLabBuffList;
	}

	public ITBLYTechnologyLabProgressState getProgressState() {
		return progressState;
	}

	public void setProgressState(ITBLYTechnologyLabProgressState progressState) {
		campAZhanLingPct = getCfg().getTotalPoint() / 2;
		this.progressState = progressState;
	}

	public void setProtectedEndTime(long protectedEndTime) {
		this.protectedEndTime = protectedEndTime;
	}

	@Override
	public long getProtectedEndTime() {
		return protectedEndTime;
	}

}
