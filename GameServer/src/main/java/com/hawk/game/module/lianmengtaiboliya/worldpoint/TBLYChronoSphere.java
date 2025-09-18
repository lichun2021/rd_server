package com.hawk.game.module.lianmengtaiboliya.worldpoint;

import org.hawk.config.HawkConfigManager;

import com.google.common.base.Objects;
import com.hawk.game.module.lianmengtaiboliya.TBLYBattleRoom;
import com.hawk.game.module.lianmengtaiboliya.cfg.TBLYChronoSphereCfg;
import com.hawk.game.module.lianmengtaiboliya.order.TBLYOrderChrono;
import com.hawk.game.module.lianmengtaiboliya.order.TBLYOrderCollection;
import com.hawk.game.module.lianmengtaiboliya.player.ITBLYPlayer;
import com.hawk.game.protocol.Const.ChatType;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.NoticeCfgId;
import com.hawk.game.protocol.GuildManager.AuthId;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.World.PBTBLYBuildSkill;
import com.hawk.game.protocol.World.WorldPointDetailPB;
import com.hawk.game.protocol.World.WorldPointPB;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.chat.ChatParames;
import com.hawk.game.util.GsConst;

/**
 * 超时空传送器
 *
 */
public class TBLYChronoSphere extends ITBLYBuilding {
	// TODO 改技能

	private long lastTick;

	private TBLYOrderChrono guildAorder = new TBLYOrderChrono();
	private TBLYOrderChrono guildBorder = new TBLYOrderChrono();

	public TBLYChronoSphere(TBLYBattleRoom parent) {
		super(parent);
		guildAorder.setParent(this);
		guildBorder.setParent(this);
	}

	@Override
	public boolean onTick() {
		super.onTick();
		long timePass = getParent().getCurTimeMil() - lastTick;
		if (timePass < 1000) {
			return true;
		}
		lastTick = getParent().getCurTimeMil();
		guildAorder.addControlTime(timePass);
		guildBorder.addControlTime(timePass);

		getParent().setChronoReadGuild("");
		getParent().setChronoReadLeader("");
		if (getState() == TBLYBuildState.ZHAN_LING) {
			long cool = getGuildOrder(getGuildId()).getContDown();
			if (cool < 0) {
				getParent().setChronoReadGuild(getGuildId());
				getParent().setChronoReadLeader(getPlayerId());
			}
		}

		guildAorder.onTick();
		guildBorder.onTick();

		return true;
	}

	public boolean onChronoShoot(ITBLYBuilding build, ITBLYPlayer comdPlayer) {
		if (getState() != TBLYBuildState.ZHAN_LING) {
			return false;
		}
		TBLYOrderChrono guildOrder = getGuildOrder(getGuildId());
		long cool = guildOrder.getContDown();
		if (cool > 0) {
			return false;
		}
		// 不是本盟的没有权限操作
		if (!comdPlayer.getGuildId().equals(getGuildId())) {
			return false;
		}

		// R4盟主队长可以遣返
		boolean guildAuthority = GuildService.getInstance().checkGuildAuthority(comdPlayer.getId(), AuthId.ALLIANCE_MANOR_SET);
		boolean isLeader = comdPlayer.getId().equals(getLeaderMarch().getMarchEntity().getPlayerId());
		if (!guildAuthority && !isLeader) {
			comdPlayer.sendError(HP.code.TBLY_CHRONO_SEND_REQ_VALUE, Status.Error.GUILD_LOW_AUTHORITY_VALUE, 0);
			return false;
		}

		guildOrder.setTarget(build);
		guildOrder.setControlTime((long) (guildOrder.MissileCoolDownTime * GsConst.EFF_PER * getParent().getCurBuff530Val(EffType.TBLY530_658)));

		guildOrder.setStartTime(getParent().getCurTimeMil());
		guildOrder.setEffectEnd(getParent().getCurTimeMil() + getCfg().getMissileEffectTime() * 1000);

		PBTBLYBuildSkill buff = PBTBLYBuildSkill.newBuilder()
				.setSkillId(TBLYOrderCollection.shuangbeijifen)
				.setCamp(comdPlayer.getCamp().intValue())
				.setX(build.getX())
				.setY(build.getY())
				.setStartTime(guildOrder.getStartTime())
				.setEndTime(guildOrder.getEffectEnd())
				.build();
		build.getShowOrder().put(TBLYOrderCollection.shuangbeijifen, buff);

		// 1. 发送时机：某方使用增加积分技能时
		// 2. 发送内容：[{0}]{1}对{2}({3},{4})使用了增加积分技能！
		// 3. 参数说明：{0}联盟简称，{1}玩家名称，{2}建筑名称，{3}X坐标，{4}Y坐标。
		ChatParames parames = ChatParames.newBuilder().setChatType(ChatType.CHAT_FUBEN_SPECIAL_BROADCAST).setKey(NoticeCfgId.TBLY_DOUBLE_JIFEN)
				.addParms(comdPlayer.getGuildTag())
				.addParms(comdPlayer.getName())
				.addParms(build.getX())
				.addParms(build.getY()).build();
		getParent().addWorldBroadcastMsg(parames);
		getParent().worldPointUpdate(this);
		return true;
	}

	@Override
	public WorldPointPB.Builder toBuilder(ITBLYPlayer viewer) {
		WorldPointPB.Builder result = super.toBuilder(viewer);
		result.setTblyChronoReadyTime(getGuildOrder(viewer.getGuildId()).getReadyTime()); // 核弹发射OK时间
		return result;
	}

	@Override
	public WorldPointDetailPB.Builder toDetailBuilder(ITBLYPlayer viewer) {
		WorldPointDetailPB.Builder result = super.toDetailBuilder(viewer);
		result.setTblyChronoReadyTime(getGuildOrder(viewer.getGuildId()).getReadyTime()); // 核弹发射OK时间
		return result;
	}

	@Override
	public WorldPointType getPointType() {
		return WorldPointType.TBLY_CHRONO_SPHERE;
	}

	public static TBLYChronoSphereCfg getCfg() {
		return HawkConfigManager.getInstance().getKVInstance(TBLYChronoSphereCfg.class);
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
	public int getControlBuff(ITBLYPlayer player, EffType effType) {
		if (underGuildControl(player.getGuildId())) {
			Integer iroVal = TBLYChronoSphere.getCfg().getControleBuffMap().getOrDefault(effType, 0);
			return iroVal;
		}
		return 0;
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

	private TBLYOrderChrono getGuildOrder(String guildId) {
		if (Objects.equal(guildId, getParent().getCampAGuild())) {
			return guildAorder;
		}
		return guildBorder;
	}

	public TBLYOrderChrono getGuildAorder() {
		return guildAorder;
	}

	public void setGuildAorder(TBLYOrderChrono guildAorder) {
		this.guildAorder = guildAorder;
	}

	public TBLYOrderChrono getGuildBorder() {
		return guildBorder;
	}

	public void setGuildBorder(TBLYOrderChrono guildBorder) {
		this.guildBorder = guildBorder;
	}

}
