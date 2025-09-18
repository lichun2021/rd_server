package com.hawk.robot.action.hero;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.function.BiConsumer;

import org.hawk.annotation.RobotAction;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.robot.HawkRobotAction;
import org.hawk.robot.HawkRobotEntity;

import com.hawk.game.protocol.Const.GachaType;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Hero.PBHeroInfo;
import com.hawk.game.protocol.Hero.PBHeroItem;
import com.hawk.game.protocol.Hero.PBHeroOffice;
import com.hawk.game.protocol.Hero.PBHeroOfficeAppointRequest;
import com.hawk.game.protocol.Hero.PBHeroStarUpRequest;
import com.hawk.game.protocol.Hero.PBUnlockHeroRequest;
import com.hawk.game.protocol.Hero.PBUseHeroExpItemRequest;
import com.hawk.game.protocol.Item.HPGachaReq;
import com.hawk.game.protocol.Item.HPItemUseByItemIdReq;
import com.hawk.robot.GameRobotEntity;
import com.hawk.robot.data.GameRobotData;

@RobotAction(valid = true)
public class PlayerHeroAction extends HawkRobotAction {
	final int[] OFFICE_ID = { 10201, 10202, 10203, 10204, 10301, 10302, 10303, 10401, 10402, 10403, 10404 }; // 7个官职
	final int[] HERO_ID = { 1001, 1017, 1003, 1005, 1018, 1006}; //, 1007, 1008, 1009, 1010, 1011, 1013, 1012, 1014, 1015, 1016, 1002, 1004, 1019, 1020, 1021, 1022 
	static final ThreadLocal<Random> random = ThreadLocal.withInitial(Random::new);

	private List<BiConsumer<GameRobotEntity, PBHeroInfo>> actionList;

	public PlayerHeroAction() {
		actionList = new ArrayList<>();
		actionList.add(this::actionOfficeAppoint);
		actionList.add(this::actionAddExp);
		actionList.add(this::actionStarup);
		actionList.add(this::actionGacha);
	}

	@Override
	public void doAction(HawkRobotEntity robotEntity) {
		GameRobotEntity gameRobotEntity = (GameRobotEntity) robotEntity;
		GameRobotData robotData = gameRobotEntity.getData();
		Map<Integer, PBHeroInfo> herodata = robotData.getBasicData().getHeroInfo();
		int heroId = HERO_ID[random.get().nextInt(HERO_ID.length)];

		PBHeroInfo hero = herodata.get(heroId);

		if (Objects.isNull(hero)) {
			// 解锁
			unlockHero(gameRobotEntity, heroId);
			return;
		}

		BiConsumer<GameRobotEntity, PBHeroInfo> action = actionList.get(random.get().nextInt(actionList.size()));
		action.accept(gameRobotEntity, hero);
	}
	
	/**
	 * 招募
	 * @param gameRobotEntity
	 * @param hero
	 */
	private void actionGacha(GameRobotEntity gameRobotEntity, PBHeroInfo hero){
		GachaType[] values = GachaType.values();
		GachaType type = values[random.get().nextInt(values.length)];		
		HPGachaReq.Builder req = HPGachaReq.newBuilder().setGachaType(type.getNumber());
		gameRobotEntity.sendProtocol(HawkProtocol.valueOf(HP.code.GACHA_C_VALUE, req));
	}

	/** 加经验 */
	private void actionAddExp(GameRobotEntity gameRobotEntity, PBHeroInfo hero) {
		// 30000_1510005_1
		PBUseHeroExpItemRequest.Builder req = PBUseHeroExpItemRequest.newBuilder();
		req.setHeroId(hero.getHeroId());
		req.addItemUse(PBHeroItem.newBuilder().setItemId(1510005).setCount(1));

		gameRobotEntity.sendProtocol(HawkProtocol.valueOf(HP.code.HERO_ADD_EXP_C_VALUE, req));
	}

	/** 加经验 */
	private void actionStarup(GameRobotEntity gameRobotEntity, PBHeroInfo hero) {
		// 30000_1510005_1
		PBHeroStarUpRequest.Builder req = PBHeroStarUpRequest.newBuilder();
		req.setHeroId(hero.getHeroId());

		gameRobotEntity.sendProtocol(HawkProtocol.valueOf(HP.code.HERO_STAR_UP_C_VALUE, req));
	}

	/** 委任 */
	private void actionOfficeAppoint(GameRobotEntity gameRobotEntity, PBHeroInfo hero) {
		int randOffice = OFFICE_ID[random.get().nextInt(OFFICE_ID.length)];
		PBHeroOfficeAppointRequest.Builder req = PBHeroOfficeAppointRequest.newBuilder();
		req.addOffice(PBHeroOffice.newBuilder().setHeroId(hero.getHeroId()).setOffice(randOffice));
		gameRobotEntity.sendProtocol(HawkProtocol.valueOf(HP.code.HERO_OFFICE_APPOINT_C_VALUE, req));
	}

	private void unlockHero(GameRobotEntity gameRobotEntity, int heroId) {
		PBUnlockHeroRequest.Builder builder = PBUnlockHeroRequest.newBuilder();
		builder.setHeroId(heroId);
		gameRobotEntity.sendProtocol(HawkProtocol.valueOf(HP.code.UNLOCK_ROBOT_HERO, builder));
	}

	@SuppressWarnings("unused")
	private void useItem(GameRobotEntity robot, int itemId, int heroId) {
		int count = robot.getItemObjects().stream().filter(e -> e.getItemId() == itemId).mapToInt(e -> e.getCount()).sum();
		if (count <= 0) {
			return;
		}
		HPItemUseByItemIdReq.Builder builder = HPItemUseByItemIdReq.newBuilder();
		builder.setItemId(itemId);
		builder.setTargetId(heroId + "");
		builder.setItemCount(count > 1000 ? 1000 : count);
		robot.sendProtocol(HawkProtocol.valueOf(HP.code.ITEM_USE_BY_ITEMID_C_VALUE, builder));
	}
}
