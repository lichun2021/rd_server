package com.hawk.game.invoker;

import org.hawk.app.HawkAppObj;
import org.hawk.config.HawkConfigManager;
import org.hawk.msg.HawkMsg;
import org.hawk.msg.invoker.HawkMsgInvoker;

import com.hawk.game.config.TreasureHuntResCfg;
import com.hawk.game.config.WorldGundamCfg;
import com.hawk.game.config.WorldMarchConstProperty;
import com.hawk.game.config.WorldPylonCfg;
import com.hawk.game.config.WorldStrongpointCfg;
import com.hawk.game.item.AwardItems;
import com.hawk.game.item.ItemInfo;
import com.hawk.log.Action;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.Const.PlayerAttr;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.world.march.IWorldMarch;

public class MarchVitReturnBackMsgInvoker extends HawkMsgInvoker {
	
	private Player player;
	private IWorldMarch march;
	
	public MarchVitReturnBackMsgInvoker(Player player, IWorldMarch march) {
		this.player = player;
		this.march = march;
	}
	
	@Override
	public boolean onMessage(HawkAppObj targetObj, HawkMsg msg) {
		if (march.getMarchType() == WorldMarchType.ATTACK_MONSTER
				|| march.getMarchType() == WorldMarchType.MONSTER_MASS
				|| march.getMarchType() == WorldMarchType.MONSTER_MASS_JOIN) {
			
			AwardItems awardItems = AwardItems.valueOf();
			awardItems.addItem(Const.ItemType.PLAYER_ATTR_VALUE, PlayerAttr.VIT_VALUE, march.getMarchEntity().getVitCost());
			awardItems.rewardTakeAffectAndPush(player, Action.FIGHT_MONSTER);
		}
		
		if (march.getMarchType() == WorldMarchType.STRONGPOINT) {
			String targetId = march.getMarchEntity().getTargetId();
			WorldStrongpointCfg strongPointCfg = HawkConfigManager.getInstance().getConfigByKey(WorldStrongpointCfg.class, Integer.valueOf(targetId));
			AwardItems awardItems = AwardItems.valueOf();
			int vitCost = strongPointCfg.getStrongpointCost();
			awardItems.addItem(Const.ItemType.PLAYER_ATTR_VALUE, PlayerAttr.VIT_VALUE, vitCost);
			awardItems.rewardTakeAffectAndPush(player, Action.WORLD_STRONGPOINT_MARCH);
		}
		
		if (march.getMarchType() == WorldMarchType.GUNDAM_SINGLE
				|| march.getMarchType() == WorldMarchType.GUNDAM_MASS
				|| march.getMarchType() == WorldMarchType.GUNDAM_MASS_JOIN) {
			String targetId = march.getMarchEntity().getTargetId();
			WorldGundamCfg strongPointCfg = HawkConfigManager.getInstance().getConfigByKey(WorldGundamCfg.class, Integer.valueOf(targetId));
			AwardItems awardItems = AwardItems.valueOf();
			int vitCost = strongPointCfg.getCostPhysicalPower();
			awardItems.addItem(Const.ItemType.PLAYER_ATTR_VALUE, PlayerAttr.VIT_VALUE, vitCost);
			awardItems.rewardTakeAffectAndPush(player, Action.GUNDAM_MARCH);
		}
		
		if (march.getMarchType() == WorldMarchType.TREASURE_HUNT_RESOURCE) {
			String targetId = march.getMarchEntity().getTargetId();
			TreasureHuntResCfg cfg = HawkConfigManager.getInstance().getConfigByKey(TreasureHuntResCfg.class, Integer.valueOf(targetId));
			AwardItems awardItems = AwardItems.valueOf();
			int vitCost = cfg.getStrongpointCost();
			awardItems.addItem(Const.ItemType.PLAYER_ATTR_VALUE, PlayerAttr.VIT_VALUE, vitCost);
			awardItems.rewardTakeAffectAndPush(player, Action.TREASURE_HUNT_RES_VIT_RETURN);
		}
		
		if (march.getMarchType() == WorldMarchType.PYLON_MARCH) {
			String targetId = march.getMarchEntity().getTargetId();
			WorldPylonCfg cfg = HawkConfigManager.getInstance().getConfigByKey(WorldPylonCfg.class, Integer.valueOf(targetId));
			AwardItems awardItems = AwardItems.valueOf();
			int vitCost = cfg.getStrongpointCost();
			awardItems.addItem(Const.ItemType.PLAYER_ATTR_VALUE, PlayerAttr.VIT_VALUE, vitCost);
			awardItems.rewardTakeAffectAndPush(player, Action.TREASURE_HUNT_RES_VIT_RETURN);
		}
		
		if (march.getMarchType() == WorldMarchType.TREASURE_HUNT_MONSTER_MASS
				|| march.getMarchType() == WorldMarchType.TREASURE_HUNT_MONSTER_MASS_JOIN) {
			
			AwardItems awardItems = AwardItems.valueOf();
			awardItems.addItem(Const.ItemType.PLAYER_ATTR_VALUE, PlayerAttr.VIT_VALUE, march.getMarchEntity().getVitCost());
			awardItems.rewardTakeAffectAndPush(player, Action.TREASURE_HUNT_MONSTER_VIT_RETURN);
		}
		
		if (march.getMarchType() == WorldMarchType.ESPIONAGE_MARCH) {
			AwardItems awardItems = AwardItems.valueOf();
			String cost = WorldMarchConstProperty.getInstance().getEspionageCost();
			awardItems.addItemInfos(ItemInfo.valueListOf(cost));
			awardItems.rewardTakeAffectAndPush(player, Action.WORLD_ESPIONAGE_TOOL_RETURN);
		}
		
		
		if (march.getMarchType() == WorldMarchType.AGENCY_MARCH_MONSTER
				|| march.getMarchType() == WorldMarchType.AGENCY_MARCH_RESCUR
				|| march.getMarchType() == WorldMarchType.AGENCY_MARCH_COASTER) {
			AwardItems awardItems = AwardItems.valueOf();
			awardItems.addItem(Const.ItemType.PLAYER_ATTR_VALUE, PlayerAttr.VIT_VALUE, march.getMarchEntity().getVitCost());
			awardItems.rewardTakeAffectAndPush(player, Action.AGENCY_MARCH_VIT_RETURN);
		}
		return true;
	}

	public Player getPlayer() {
		return player;
	}

	public void setPlayer(Player player) {
		this.player = player;
	}

	public IWorldMarch getMarch() {
		return march;
	}

	public void setMarch(IWorldMarch march) {
		this.march = march;
	}
	
}
