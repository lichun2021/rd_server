package com.hawk.robot.action.president;

import java.util.ArrayList;
import java.util.List;

import org.hawk.annotation.RobotAction;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.enums.EnumUtil;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkRand;
import org.hawk.os.HawkTime;
import org.hawk.robot.HawkRobotAction;
import org.hawk.robot.HawkRobotEntity;

import com.hawk.game.protocol.Const.PlayerAttr;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.President.MemeberInfo;
import com.hawk.game.protocol.President.OfficerInfo;
import com.hawk.game.protocol.President.OfficerSetReq;
import com.hawk.game.protocol.President.PresidentResourceSetReq;
import com.hawk.game.protocol.President.PresidentSearchReq;
import com.hawk.game.protocol.President.PresidentSendGiftReq;
import com.hawk.robot.GameRobotEntity;
import com.hawk.robot.RobotLog;
import com.hawk.robot.action.friend.FriendAction;
import com.hawk.robot.config.OfficerCfg;
import com.hawk.robot.config.PresidentGiftCfg;

@RobotAction(valid = true)
public class PresidentOfficerGiftAction extends HawkRobotAction{
	@Override
	public void doAction(HawkRobotEntity entity) {
		GameRobotEntity gameRobotEntity = (GameRobotEntity) entity;
		PresidentOfficerGiftActionType type = EnumUtil.random(PresidentOfficerGiftActionType.class);
		switch (type) {
		case OFFICER_INFO:
			doOfficerInfo(gameRobotEntity);
			break;
		case OFFICER_SET:
			doOfficerSet(gameRobotEntity);
			break;
		case OFFICER_UNSET:
			doOfficerUnset(gameRobotEntity);
			break;
		case OFFICER_RECORD:
			doOfficerRecordReq(gameRobotEntity);
			break;
		case GIFT_INFO:
			doGiftInfoReq(gameRobotEntity);
			break;
		case GIFT_RECORD:
			doGiftRecordReq(gameRobotEntity);
			break;
		case GIFT_SEND:
			doGiftSendReq(gameRobotEntity);
			break;
		case RESOURCE_INFO:
			doResourceInfo(gameRobotEntity);
			break;
		case RESOURCE_SET:
			doResourceSet(gameRobotEntity);
			break;
		default:
			break;
		}
		
	}
	
	private void doResourceSet(GameRobotEntity gameRobotEntity) {
		PresidentResourceSetReq.Builder sbuilder = PresidentResourceSetReq.newBuilder();
		int[] attrs = new int[]{PlayerAttr.GOLDORE_UNSAFE_VALUE,PlayerAttr.OIL_UNSAFE_VALUE,
				PlayerAttr.STEEL_UNSAFE_VALUE,
				PlayerAttr.TOMBARTHITE_UNSAFE_VALUE};
		int attr = attrs[HawkRand.randInt(attrs.length - 1)];
		sbuilder.setAttrType(attr);
		
		gameRobotEntity.sendProtocol(HawkProtocol.valueOf(HP.code.PRESIDENT_RESOURCE_SET_REQ_VALUE));
		
	}

	private void doResourceInfo(GameRobotEntity gameRobotEntity) {
		HawkProtocol protocol = HawkProtocol.valueOf(HP.code.PRESIDENT_RESOURCE_INFO_REQ_VALUE);
		gameRobotEntity.sendProtocol(protocol);
	}

	private void doGiftSendReq(GameRobotEntity gameRobotEntity) {
		ConfigIterator<PresidentGiftCfg> giftIterator = HawkConfigManager.getInstance().getConfigIterator(PresidentGiftCfg.class);
		int i = 0;
		int index = HawkRand.randInt(giftIterator.size() - 1);
		PresidentGiftCfg cfg = null;
		do {
			cfg = giftIterator.next();
			i++;
		} while (i <= index);
		
		MemeberInfo memberInfo = this.getMember(gameRobotEntity);
		if (memberInfo == null) {
			return;
		}
		
		PresidentSendGiftReq.Builder sbuilder = PresidentSendGiftReq.newBuilder();
		sbuilder.setGiftId(cfg.getId());
		sbuilder.addPlayerIds(memberInfo.getMiniPlayerOrBuilder().getPlayerId());
		
		HawkProtocol protocol = HawkProtocol.valueOf(HP.code.PRESIDENT_SEND_GIFT_C);
		gameRobotEntity.sendProtocol(protocol);
	}

	private void doGiftRecordReq(GameRobotEntity gameRobotEntity) {
		HawkProtocol protocol = HawkProtocol.valueOf(HP.code.PRESIDENT_GIFT_RECORD_C_VALUE);
		gameRobotEntity.sendProtocol(protocol);
	}

	private void doGiftInfoReq(GameRobotEntity gameRobotEntity) {
		HawkProtocol protocol = HawkProtocol.valueOf(HP.code.PRESIDENT_GIFT_INFO_C_VALUE);
		gameRobotEntity.sendProtocol(protocol);
	}

	private void doOfficerRecordReq(GameRobotEntity gameRobotEntity) {
		HawkProtocol protocol = HawkProtocol.valueOf(HP.code.OFFICER_RECORD_SYNC_C_VALUE);
		gameRobotEntity.sendProtocol(protocol);
	}

	private List<OfficerInfo.Builder> getValidOfficerInfoList(GameRobotEntity entity) {
		List<OfficerInfo.Builder> officerList = entity.getBasicData().getOfficerList();
		List<OfficerInfo.Builder> rltList = new ArrayList<>();
		for (OfficerInfo.Builder ob : officerList) {
			if (ob.getPlayerMsg() != null) {
				rltList.add(ob);
			}
		}
		
		return rltList;
	}
	
	private void doOfficerUnset(GameRobotEntity entity) {
		List<OfficerInfo.Builder> officerList = entity.getBasicData().getOfficerList();
		boolean randomDelete = HawkRand.randInt(10) > 5;
		OfficerCfg officerCfg = this.getRandomOfficerCfg();
		OfficerSetReq.Builder sbuilder = OfficerSetReq.newBuilder(); 
		if (randomDelete || officerList.isEmpty()) {
			MemeberInfo memberInfo = this.getMember(entity);
			if (memberInfo == null) {
				return;
			}
			
			sbuilder.setOfficerId(officerCfg.getId());
			sbuilder.setPlayerId("");
			
		} else {
			
			OfficerInfo.Builder infoBuilder = officerList.get(HawkRand.randInt(officerList.size() - 1));
			sbuilder.setOfficerId(infoBuilder.getOfficerId());
			sbuilder.setPlayerId(infoBuilder.getPlayerMsg().getPlayerId());
		}
		
		entity.sendProtocol(HawkProtocol.valueOf(HP.code.OFFICER_SET_C_VALUE, sbuilder));
	}
	
	private OfficerCfg getRandomOfficerCfg() {
		ConfigIterator<OfficerCfg> configStorage = HawkConfigManager.getInstance().getConfigIterator(OfficerCfg.class);
		 
		int index = HawkRand.randInt(configStorage.size()-1);
		int i = 0;
		OfficerCfg officerCfg = null;
		do {
			officerCfg = configStorage.next();
			i++;
		} while (i <= index);
		
		return officerCfg;
	}


	/**
	 * 设置官职
	 * @param entity
	 */
	private void doOfficerSet(GameRobotEntity entity) {
		OfficerSetReq.Builder newBuilder = OfficerSetReq.newBuilder();
		String playerId = null;
		int officerId = 0;
		if (HawkRand.randInt(10) > 5) {
			List<OfficerInfo.Builder> infoList = this.getValidOfficerInfoList(entity);
			if (infoList.isEmpty()) {
				return;
			}
			
			if (infoList.size() >= 2) {
				OfficerInfo.Builder infoBuilder = infoList.get(HawkRand.randInt(infoList.size() - 1)); 
				playerId = infoBuilder.getPlayerMsg().getPlayerId();
				officerId = infoBuilder.getOfficerId();
			}
			 
		} else {
			OfficerCfg officerCfg = getRandomOfficerCfg();
			if (officerCfg != null) {					
				officerId = officerCfg.getId();
				MemeberInfo mi = this.getMember(entity);
				if (mi == null) {
					return;
				} else {
					playerId = mi.getMiniPlayer().getPlayerId();
				}			
							
			} else {
				return;
			}
		}
		
		
		newBuilder.setOfficerId(officerId);
		newBuilder.setPlayerId(playerId);
		
		entity.sendProtocol(HawkProtocol.valueOf(HP.code.OFFICER_INFO_SYNC_C_VALUE, newBuilder));
		
	}
	
	private MemeberInfo getMember(GameRobotEntity robotEntity) {
		List<MemeberInfo> memberInfoList = robotEntity.getBasicData().getMemberInfoList();
		if (memberInfoList.isEmpty()) {
			this.trySearchMember(robotEntity);
			
			return null;
		}
		
		return memberInfoList.remove(HawkRand.randInt(memberInfoList.size() - 1));
		
	}
	
	private void trySearchMember(HawkRobotEntity robotEntity) {
		long curTime = HawkTime.getMillisecond();
		GameRobotEntity gameRobotEntity = (GameRobotEntity) robotEntity;
		//搜索要超过五秒
		if (gameRobotEntity.getBasicData().getLastSeTime() + 5000 > curTime ) {
			return ;
		}
		
		this.searchMember(gameRobotEntity);
	}


	private void doOfficerInfo(HawkRobotEntity robotEntity) {
		HawkProtocol protocol = HawkProtocol.valueOf(HP.code.OFFICER_INFO_SYNC_C_VALUE);
		
		robotEntity.sendProtocol(protocol);
		RobotLog.cityPrintln("geT officer info");
	}
	
	private void searchMember(HawkRobotEntity entity) {
		boolean flag = HawkRand.randInt(10) > 5 ;
		String name ="";
		if (flag) {
			name = FriendAction.randomString(HawkRand.randInt() %4 + 1);
		}
		
		this.searchMember(entity, name);
	} 
	
	private void searchMember(HawkRobotEntity robotEntity, String id){
		PresidentSearchReq.Builder sbuilder = PresidentSearchReq.newBuilder();
		sbuilder.setName(id);
		
		robotEntity.sendProtocol(HawkProtocol.valueOf(HP.code.PRESIDENT_SEARCH_C, sbuilder));
	}


	private enum PresidentOfficerGiftActionType {
		OFFICER_INFO,
		OFFICER_SET,
		OFFICER_UNSET,
		OFFICER_RECORD,
		GIFT_INFO,
		GIFT_RECORD,
		GIFT_SEND,
		RESOURCE_SET,
		RESOURCE_INFO
	}

}
