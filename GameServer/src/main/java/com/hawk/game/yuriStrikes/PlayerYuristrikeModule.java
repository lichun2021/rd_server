package com.hawk.game.yuriStrikes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.hawk.annotation.MessageHandler;
import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.xid.HawkXID;

import com.hawk.game.battle.BattleOutcome;
import com.hawk.game.battle.BattleService;
import com.hawk.game.battle.NpcPlayer;
import com.hawk.game.battle.TemporaryMarch;
import com.hawk.game.battle.battleIncome.impl.PveBattleIncome;
import com.hawk.game.battle.effect.BattleConst;
import com.hawk.game.item.ConsumeItems;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.module.PlayerMarchModule;
import com.hawk.game.msg.YuriStrikeCleanFinishMsg;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerModule;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.World.WorldMarchReq;
import com.hawk.game.protocol.YuriStrike.PBAtkYuriResp;
import com.hawk.game.service.mail.FightMailService;
import com.hawk.game.util.GsConst;
import com.hawk.game.world.march.IWorldMarch;
import com.hawk.game.world.service.WorldPlayerService;
import com.hawk.log.Action;

/**
 * 联盟仓库
 * 
 * @author lwt
 *
 */
public class PlayerYuristrikeModule extends PlayerModule {

	public PlayerYuristrikeModule(Player player) {
		super(player);
	}

	@Override
	protected boolean onPlayerLogin() {
		getYuristrikeObj().onLogin(player);
		return super.onPlayerLogin();
	}

	@Override
	public boolean onTick() {
		getYuristrikeObj().tick(player);
		return super.onTick();
	}

	@MessageHandler
	private void onCleanFinishMsg(YuriStrikeCleanFinishMsg msg) {
		getYuristrikeObj().cleanOver(player);

	}

	/**
	 * 城内攻打尤里. 不发行军. 直接战斗
	 */
	@ProtocolHandler(code = HP.code.YURI_STRIKE_ATK_VALUE)
	private void onAtkYuri(HawkProtocol protocol) {
		WorldMarchReq req = protocol.parseProtocol(WorldMarchReq.getDefaultInstance());

		// 带兵出征通用检查
		PlayerMarchModule marchModule = player.getModule(GsConst.ModuleType.WORLD_MARCH_MODULE);
		List<ArmyInfo> armyList = new ArrayList<ArmyInfo>(); // 出征的士兵列表
		if (!marchModule.checkMarchReq(req, protocol.getType(), armyList, null,false)) {
			return;
		}
		List<IWorldMarch> atkMarchs = new ArrayList<>();
		TemporaryMarch playerMarch = new TemporaryMarch();
		playerMarch.setArmys(armyList);
		playerMarch.setPlayer(player);
		playerMarch.setHeros(player.getHeroByCfgId(req.getHeroIdList()));
		atkMarchs.add(playerMarch);

		TemporaryMarch yuriMarch = new TemporaryMarch();
		yuriMarch.setArmys(getYuristrikeObj().getCfg().getEnemyList());
		yuriMarch.setPlayer(new NpcPlayer(HawkXID.nullXid()));
		yuriMarch.setHeros(Collections.emptyList());

		List<Player> atkPlayers = new ArrayList<>();
		atkPlayers.add(player);
		int pointId = WorldPlayerService.getInstance().getPlayerPos(player.getId());
		int yuristrikeId = getYuristrikeObj().getCfg().getId();
		PveBattleIncome battleIncome =BattleService.getInstance().initYuristrikeBattleData(BattleConst.BattleType.ATTACK_YURI_STRIKE_PVE, pointId, yuristrikeId, atkPlayers, atkMarchs, yuriMarch);
		
		BattleOutcome battleOutcome = BattleService.getInstance().doBattle(battleIncome);
		FightMailService.getInstance().sendPveFightMail(BattleConst.BattleType.ATTACK_YURI_STRIKE_PVE, battleIncome, battleOutcome, null);
		// 战斗计算
		boolean atkWin = battleOutcome.isAtkWin();
		if (atkWin) {
			getYuristrikeObj().attackWin(player);
		}
		player.sendProtocol(HawkProtocol.valueOf(HP.code.YURI_STRIKE_ATK_RESP, PBAtkYuriResp.newBuilder().setWin(atkWin ? 1 : 0)));
	}

	/**
	 * 开始净化
	 */
	@ProtocolHandler(code = HP.code.YURI_STRIKE_CLEAN_VALUE)
	private void onCleanYuri(HawkProtocol protocol) {
		YuriStrike yuristrikeObj = getYuristrikeObj();

		ConsumeItems consumeItems = ConsumeItems.valueOf();
		consumeItems.addConsumeInfo(ItemInfo.valueListOf(yuristrikeObj.getCfg().getPurifyCost()));
		if (!consumeItems.checkConsume(player, protocol.getType())) {
			return;
		}
		consumeItems.consumeAndPush(player, Action.YURISTRIKE_CLEAN);
		
		yuristrikeObj.startClean(player);

		player.responseSuccess(protocol.getType());
	}

	/**
	 * 净化收取
	 */
	@ProtocolHandler(code = HP.code.YURI_STRIKE_OBTAIN_REWARD_VALUE)
	private void onObtainReward(HawkProtocol protocol) {

		getYuristrikeObj().obtainReward(player);

		player.responseSuccess(protocol.getType());
	}

	private YuriStrike getYuristrikeObj() {
		return player.getData().getYuriStrikeEntity().getYuriStrikeObj();
	}

}
