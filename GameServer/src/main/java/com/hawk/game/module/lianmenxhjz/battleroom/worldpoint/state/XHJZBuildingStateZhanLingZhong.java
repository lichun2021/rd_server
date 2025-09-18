package com.hawk.game.module.lianmenxhjz.battleroom.worldpoint.state;

import java.util.Objects;

import com.hawk.game.module.lianmenxhjz.battleroom.XHJZGuildBaseInfo;
import com.hawk.game.module.lianmenxhjz.battleroom.worldmarch.IXHJZWorldMarch;
import com.hawk.game.module.lianmenxhjz.battleroom.worldpoint.IXHJZBuilding;
import com.hawk.game.module.lianmenxhjz.battleroom.worldpoint.XHJZBuildState;
import com.hawk.game.protocol.World.WorldPointDetailPB;
import com.hawk.game.protocol.World.WorldPointPB;
import com.hawk.game.protocol.World.XHJZHoldRec;
import com.hawk.game.protocol.World.XHJZQuateredState;

public class XHJZBuildingStateZhanLingZhong extends IXHJZBuildingState {
	private long zhanlingStart; // 占领开始
	private long zhanlingJieshu;
	private XHJZGuildBaseInfo lastGuildid;
	private long lastTick;

	public XHJZBuildingStateZhanLingZhong(IXHJZBuilding build) {
		super(build);
		IXHJZWorldMarch leaderMarch = getParent().getLeaderMarch();
		lastGuildid = getParent().getParent().getCampBase(leaderMarch.getParent().getGuildId());
		zhanlingStart = getParent().getParent().getCurTimeMil();
		zhanlingJieshu = (long) (getParent().getControlCountDown() * 1000 * (1 - lastGuildid.getCoolDownReducePercentage()*0.01) + zhanlingStart);
		lastTick = this.zhanlingStart;
	}

	@Override
	public void init() {
		
	}

	@Override
	public boolean onTick() {
		long timePass = getParent().getParent().getCurTimeMil() - lastTick;
		lastTick = getParent().getParent().getCurTimeMil();
		IXHJZWorldMarch leaderMarch = getParent().getLeaderMarch();
		if (Objects.isNull(leaderMarch)) {
				getParent().setStateObj(new XHJZBuildingStateZhanLing(getParent()));
			return true;
		}
		
		if (!Objects.equals(leaderMarch.getParent().getGuildId(), lastGuildid.getGuildId())) {
			getParent().setStateObj(new XHJZBuildingStateZhanLingZhong(getParent()));
//			// 广播通知(主动占领未成功)
//			ChatParames paramesAtk = ChatParames.newBuilder()
//					.setChatType(ChatType.CHAT_FUBEN_TEAM_SPECIAL_BROADCAST)
//					.setKey(NoticeCfgId.XHJZ_GUILD_CONTORY_BUILD_FAIL)
//					.setGuildId(lastGuildid.getGuildId())
//					.addParms(getParent().getX())
//					.addParms(getParent().getY())
//					.build();
//			getParent().getParent().addWorldBroadcastMsg(paramesAtk);
			return true;
		}

		if (Objects.equals(leaderMarch.getParent().getGuildId(), getParent().getOnwerGuildId())) {
			getParent().setStateObj(new XHJZBuildingStateZhanLing(getParent()));
//			// 广播通知(防守成功)
//
//			ChatParames paramesAtk = ChatParames.newBuilder()
//					.setChatType(ChatType.CHAT_FUBEN_TEAM_SPECIAL_BROADCAST)
//					.setKey(NoticeCfgId.XHJZ_GUILD_DEFEND_BUILD_SUCESS)
//					.setGuildId(getParent().getGuildId())
//					.addParms(this.lastGuildid.getServerId())
//					.addParms(this.lastGuildid.getGuildName())
//					.addParms(getParent().getX())
//					.addParms(getParent().getY())
//					.build();
//			getParent().getParent().addWorldBroadcastMsg(paramesAtk);
			return true;
		}

		if (getParent().getParent().getCurTimeMil() > zhanlingJieshu) {

			getParent().setOnwerGuild(lastGuildid);
			getParent().setStateObj(new XHJZBuildingStateZhanLing(getParent()));

			// 占领记录
			XHJZHoldRec.Builder hrec = XHJZHoldRec.newBuilder()
					.setGuildName(lastGuildid.getGuildName())
					.setHoldTime(zhanlingJieshu)
					.setPlayerName(getParent().getPlayerName())
					.setGuildTag(lastGuildid.getGuildTag())
					.setPtype(getParent().getPointType())
					.setX(getParent().getX())
					.setY(getParent().getY())
					.setFlagView(lastGuildid.getCamp().intValue())
					.setGuildId(lastGuildid.getGuildId());
			getParent().getHoldRecList().add(hrec);
			return true;
		}

		return true;
	}

	

	@Override
	public XHJZBuildState getState() {
		// TODO Auto-generated method stub
		return XHJZBuildState.ZHAN_LING_ZHONG;
	}

	@Override
	public void fillBuilder(WorldPointPB.Builder builder) {
		builder.setManorBuildTime(zhanlingStart); // 占领开始时间
		builder.setManorComTime(zhanlingJieshu); // 占领结束时间
	}

	@Override
	public void fillDetailBuilder(WorldPointDetailPB.Builder builder) {
		builder.setManorBuildTime(zhanlingStart); // 占领开始时间
		builder.setManorComTime(zhanlingJieshu); // 占领结束时间

	}

	@Override
	public XHJZQuateredState getMarchQuateredStatus(IXHJZWorldMarch march) {
		XHJZQuateredState.Builder state = XHJZQuateredState.newBuilder();
		state.setState(getState().intValue());
		state.setZhanLingJieShu(zhanlingJieshu);
		state.setZhuShouKaishi(zhanlingStart);
		return state.build();
	}

}
