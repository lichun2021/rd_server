package com.hawk.robot.action.guildgift;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Random;
import java.util.function.Consumer;

import org.apache.commons.lang.StringUtils;
import org.hawk.annotation.RobotAction;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.robot.HawkRobotAction;
import org.hawk.robot.HawkRobotEntity;

import com.hawk.game.protocol.GuildBigGift.PBDeleteGiftRequest;
import com.hawk.game.protocol.GuildBigGift.PBGetGiftAwardRequest;
import com.hawk.game.protocol.GuildBigGift.PBPlayerGuildGift;
import com.hawk.game.protocol.HP;
import com.hawk.robot.GameRobotEntity;
import com.hawk.robot.data.GameRobotData;

@RobotAction(valid = true)
public class PlayerGuildBigGiftAction extends HawkRobotAction {
	static final ThreadLocal<Random> random = ThreadLocal.withInitial(Random::new);
	private List<Consumer<GameRobotEntity>> actionList;

	public  PlayerGuildBigGiftAction() {
		actionList = new ArrayList<>();
		actionList.add(this::actionList);
		actionList.add(this::actionGet);
		actionList.add(this::actionDel);
	}
	
	@Override
	public void doAction(HawkRobotEntity entity) {
		GameRobotEntity gameRobotEntity = (GameRobotEntity) entity;
		if(StringUtils.isEmpty(gameRobotEntity.getGuildId())){
			return;
		}
		Consumer<GameRobotEntity> action = actionList.get(random.get().nextInt(actionList.size()));
		action.accept(gameRobotEntity);
	}

	/** 取得列表 */
	private void actionList(GameRobotEntity gameRobotEntity) {
		gameRobotEntity.sendProtocol(HawkProtocol.valueOf(HP.code.PLAYER_SMAIL_GIFT_LIST_C_VALUE));
	}

	/** 领奖 */
	private void actionGet(GameRobotEntity gameRobotEntity) {
		GameRobotData robotData = gameRobotEntity.getData();
		Map<String, PBPlayerGuildGift> map = robotData.getBasicData().getGuildGiftInfo();
		if (map.isEmpty()) {
			return;
		}
		PBGetGiftAwardRequest.Builder req = PBGetGiftAwardRequest.newBuilder();
		for (Entry<String, PBPlayerGuildGift> ent : map.entrySet()) {
			PBPlayerGuildGift gift = ent.getValue();
			if (gift.getState() == 0) {
				req.addIds(ent.getKey());
				break;
			}
		}

		if (req.getIdsCount() == 0) {
			return;
		}

		gameRobotEntity.sendProtocol(HawkProtocol.valueOf(HP.code.PLAYER_SMAIL_GIFT_GET_REWARD_C, req));
	}

	/** 删除 */
	private void actionDel(GameRobotEntity gameRobotEntity) {
		GameRobotData robotData = gameRobotEntity.getData();
		Map<String, PBPlayerGuildGift> map = robotData.getBasicData().getGuildGiftInfo();
		if (map.isEmpty()) {
			return;
		}
		PBDeleteGiftRequest.Builder req = PBDeleteGiftRequest.newBuilder();
		PBPlayerGuildGift giftDel = null;
		for (Entry<String, PBPlayerGuildGift> ent : map.entrySet()) {
			PBPlayerGuildGift gift = ent.getValue();
			if (gift.getState() == 1) {
				req.addIds(ent.getKey());
				giftDel = gift;
				break;
			}
		}
		if (Objects.isNull(giftDel)) {
			return;
		}

		map.remove(giftDel.getId());
		gameRobotEntity.sendProtocol(HawkProtocol.valueOf(HP.code.PLAYER_SMAIL_GIFT_DEL_REWARD_C, req));
	}

}
