package com.hawk.game.module.dayazhizhan.battleroom.worldpoint;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

import org.hawk.net.protocol.HawkProtocol;
import org.hawk.uuid.HawkUUIDGenerator;

import com.hawk.game.module.dayazhizhan.battleroom.DYZZBattleRoom;
import com.hawk.game.module.dayazhizhan.battleroom.DYZZConst.DYZZShotType;
import com.hawk.game.module.dayazhizhan.battleroom.DYZZConst.DYZZState;
import com.hawk.game.module.dayazhizhan.battleroom.player.IDYZZPlayer;
import com.hawk.game.protocol.DYZZ.DYZZBuildState;
import com.hawk.game.protocol.DYZZ.PBDYZZNuclearSync;
import com.hawk.game.protocol.HP;
import com.hawk.game.util.GameUtil;

public abstract class IDYZZNuclearShootAble extends IDYZZBuilding {
	private List<PBDYZZNuclearSync> shotList = new CopyOnWriteArrayList<>();
	private long nextShot;

	public IDYZZNuclearShootAble(DYZZBattleRoom parent) {
		super(parent);
		nextShot = parent.getCollectStartTime() + getAtkCd() * 1000;
	}

	public final boolean onShootTick() {

		for (PBDYZZNuclearSync shot : shotList) {
			nuclearShootCheck(shot);
		}

		if (getState() != DYZZBuildState.ZHAN_LING) {
			nextShot = getParent().getCurTimeMil() + getAtkCd() * 1000;
		}

		if (getParent().getCurTimeMil() > nextShot && onNuclearShoot(getAtkVal(), DYZZShotType.Turn)) {
			nextShot = getParent().getCurTimeMil() + getAtkCd() * 1000;

			// System.out.println(getClass().getSimpleName() + getGuildId() + "攻击基地中 " + ", "+ getX() +" "+ getY());
		}

		return true;
	}

	private void nuclearShootCheck(PBDYZZNuclearSync sendRecord) {
		if (Objects.isNull(sendRecord)) {
			return;
		}

		if (sendRecord.getSendOver() > getParent().getCurTimeMil()) {
			return;
		}
		shotList.remove(sendRecord);

		int pointId = GameUtil.combineXAndY(sendRecord.getTarX(), sendRecord.getTarY());
		DYZZBase tpoint = (DYZZBase) getParent().getWorldPoint(pointId).orElse(null);
		int hp = tpoint.getHp();
		tpoint.dcresHP(sendRecord.getAtkVal());
		int lostHp = hp - tpoint.getHp();
		
		sendRecord = sendRecord.toBuilder().setAtkVal(lostHp).build();
		this.incNuclearShotRec(sendRecord);
		tpoint.incNuclearDefRec(sendRecord);

	}

	/**
	 * 
	 * @param atkVal
	 * @param shotType 发动类型 1 周期 2 井  3 商人
	 * @return
	 */
	public final boolean onNuclearShoot(int atkVal, DYZZShotType shotType) {
		if (getState() != DYZZBuildState.ZHAN_LING) {
			return false;
		}

		DYZZBase target = getParent().getDYZZBuildingByClass(DYZZBase.class).stream()
				.filter(base -> !Objects.equals(base.getGuildId(), getGuildId())).findAny()
				.get();

		if (getParent().IS_GO_MODEL) {
			System.out.println(getClass().getSimpleName() + " " + getX() + " ," + getY() + "发射  打" + target.getGuildId() + " " + target.getX() + "  " + target.getY() + " hp: "
					+ target.getHp());
		}
		// 发射
		PBDYZZNuclearSync.Builder bul = PBDYZZNuclearSync.newBuilder();
		bul.setId(HawkUUIDGenerator.genUUID());
		bul.setGuildId(getGuildId());
		bul.setSendStart(getParent().getCurTimeMil());
		bul.setSendOver(getParent().getCurTimeMil() + 10000);
		bul.setTarX(target.getX());
		bul.setTarY(target.getY());
		bul.setFromX(getX());
		bul.setFromY(getY());
		bul.setAtkVal(atkVal);
		bul.setShotType(shotType.intValue());
		shotList.add(bul.build());

		List<IDYZZPlayer> plist = getParent().getPlayerList(DYZZState.GAMEING);
		for (IDYZZPlayer player : plist) {
			player.sendProtocol(HawkProtocol.valueOf(HP.code2.DYZZ_NUCLEAR_SEND_S, bul));
		}

		return true;
	}

	public long getNextShot() {
		if (nextShot > getParent().getCurTimeMil() + 60 * 60 * 1000) {
			return 0;
		}

		return nextShot;
	}

	public abstract int getAtkCd();

	public abstract int getAtkVal();

	public abstract int getWellAtkVal();

	public abstract int getOrderAtkVal();
}
