package com.hawk.game.module.spacemecha.worldmarch;

import java.util.List;
import java.util.stream.Collectors;
import java.util.Set;

import org.hawk.os.HawkTime;
import org.hawk.xid.HawkXID;

import com.hawk.game.battle.NpcPlayer;
import com.hawk.game.global.GlobalData;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.module.spacemecha.MechaSpaceInfo;
import com.hawk.game.module.spacemecha.SpaceMechaService;
import com.hawk.game.module.spacemecha.worldpoint.SpaceWorldPoint;
import com.hawk.game.player.Player;
import com.hawk.game.player.hero.PlayerHero;
import com.hawk.game.protocol.GuildWar.GuildWarSingleInfo;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.world.WorldMarch;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.march.MonsterMarch;
import com.hawk.game.world.march.submarch.BasedMarch;
import com.hawk.game.world.march.submarch.IReportPushMarch;
import com.hawk.game.world.service.WorldPointService;

/**
 * 假行军
 * 
 */
public class SpaceMechaEmptyMarch extends MonsterMarch implements BasedMarch, IReportPushMarch {
	
	NpcPlayer npcPlayer = null;

	public SpaceMechaEmptyMarch(WorldMarch marchEntity) {
		super(marchEntity);
	}

	@Override
	public WorldMarchType getMarchType() {
		return WorldMarchType.SPACE_MECHA_EMPTY_MARCH;
	}

	@Override
	public Player getPlayer() {
		if (npcPlayer == null) {
			npcPlayer = new NpcPlayer(HawkXID.nullXid());
		}
		return npcPlayer;
	}

	@Override
	public void onMarchReach(Player player) {
		// 假行军达到后直接删除
		WorldMarchService.getInstance().removeMarch(this);
	}
	
	@Override
	public long getMarchNeedTime() {
		String guildId = this.getMarchEntity().getTargetId();
		MechaSpaceInfo spaceObject = SpaceMechaService.getInstance().getGuildSpace(guildId);
		return spaceObject.getStage().getEndTime() - HawkTime.getMillisecond();
	}

	/**
	 * 联盟战争界面里单人信息
	 * @param worldMarch
	 * @return
	 */
	@Override
	public GuildWarSingleInfo.Builder getGuildWarSingleInfo(WorldMarch worldMarch) {
		Player player = GlobalData.getInstance().makesurePlayer(worldMarch.getPlayerId());
		GuildWarSingleInfo.Builder builder = GuildWarSingleInfo.newBuilder();
		builder.setPlayerId(worldMarch.getPlayerId());
		builder.setPlayerName(player.getName());
		builder.setIconId(player.getIcon());
		builder.setPfIcon(player.getPfIcon());
		List<PlayerHero> heros = player.getHeroByCfgId(worldMarch.getHeroIdList());
		if (heros != null && !heros.isEmpty()) {
			for (PlayerHero hero : heros) {
				builder.addHeroInfo(hero.toPBobj());
			}
		}
		List<ArmyInfo> armys = worldMarch.getArmys();
		for (ArmyInfo army : armys) {
			builder.addArmys(army.toArmySoldierPB(player));
		}
		builder.setEndTime(worldMarch.getEndTime());
		return builder;
	}

	@Override
	public void onMarchStart() {		
	}

	@Override
	public void onMarchReturn() {
	}

	@Override
	public void remove() {
		super.remove();
	}

	@Override
	public Set<String> attackReportRecipients() {
		String guildId = this.getMarchEntity().getTargetId();
		MechaSpaceInfo spaceObject = SpaceMechaService.getInstance().getGuildSpace(guildId);
		SpaceWorldPoint spacePoint = (SpaceWorldPoint) WorldPointService.getInstance().getWorldPoint(this.getMarchEntity().getTerminalId());
		Set<String> defPlayerIds = spaceObject.getSpaceMarchs(spacePoint.getSpaceIndex()).stream().map(e -> e.getPlayerId()).collect(Collectors.toSet());
		return defPlayerIds;
	}

	@Override
	public boolean needShowInGuildWar() {
		return false;
	}

}
