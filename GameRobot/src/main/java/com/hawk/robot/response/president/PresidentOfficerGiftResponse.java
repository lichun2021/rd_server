package com.hawk.robot.response.president;

import java.util.List;

import org.hawk.net.protocol.HawkProtocol;

import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.President.GiftRecord;
import com.hawk.game.protocol.President.MemeberInfo;
import com.hawk.game.protocol.President.OfficerInfo;
import com.hawk.game.protocol.President.OfficerInfoOrBuilder;
import com.hawk.game.protocol.President.OfficerInfoSync;
import com.hawk.game.protocol.President.PresidentGiftInfo;
import com.hawk.game.protocol.President.PresidentGiftRecordRes;
import com.hawk.game.protocol.President.PresidentGiftRecordsUpdate;
import com.hawk.game.protocol.President.PresidentResourceInfoSyn;
import com.hawk.game.protocol.President.PresidentSearchRes;
import com.hawk.robot.GameRobotEntity;
import com.hawk.robot.RobotLog;
import com.hawk.robot.annotation.RobotResponse;
import com.hawk.robot.response.RobotResponsor;

@RobotResponse(code={HP.code.PRESIDENT_SEARCH_S_VALUE,
					HP.code.PRESIDENT_GIFT_INFO_S_VALUE,
					HP.code.PRESIDENT_GIFT_RECORD_S_VALUE,
					HP.code.OFFICER_RECORD_SYNC_S_VALUE,
					HP.code.PRESIDENT_GIFT_RECORDS_UPDATE_VALUE,
					HP.code.PRESIDENT_RESOURCE_INFO_SYN_VALUE,
					HP.code.OFFICER_INFO_SYNC_S_VALUE
					})
public class PresidentOfficerGiftResponse extends RobotResponsor {

	@Override
	public void response(GameRobotEntity robotEntity, HawkProtocol protocol) {
		switch (protocol.getType()) {
		case HP.code.PRESIDENT_SEARCH_S_VALUE:
			doSearchResp(robotEntity, protocol);
			break;
		case HP.code.PRESIDENT_GIFT_INFO_S_VALUE:
			doGiftInfoResp(robotEntity, protocol);
			break;
		case HP.code.PRESIDENT_GIFT_RECORD_S_VALUE:
			doGiftRecordResp(robotEntity, protocol);
			break;
		case HP.code.PRESIDENT_GIFT_RECORDS_UPDATE_VALUE:
			doGiftRecordUpdateResp(robotEntity, protocol);
			break;
		case HP.code.PRESIDENT_RESOURCE_INFO_SYN_VALUE:
			doResourceInfoResp(robotEntity, protocol);
			break;
		case HP.code.OFFICER_INFO_SYNC_S_VALUE:
			doOfficerInfoSyncResp(robotEntity, protocol);
			break;
		}

	}

	private void doOfficerInfoSyncResp(GameRobotEntity robotEntity, HawkProtocol protocol) {
		OfficerInfoSync sinfo = protocol.parseProtocol(OfficerInfoSync.getDefaultInstance());
		boolean isHave = false;
		if (sinfo.getOfficersList() != null && !sinfo.getOfficersList().isEmpty()) {
			for (OfficerInfoOrBuilder officerInfo1 : sinfo.getOfficersOrBuilderList()) {
				isHave = false;
				for (OfficerInfo.Builder officerInfo2 : robotEntity.getBasicData().getOfficerList()) {
					if (officerInfo1.getOfficerId() == officerInfo2.getOfficerId()) {
						officerInfo2.setOfficerId(officerInfo1.getOfficerId());
						officerInfo2.setEndTime(officerInfo1.getEndTime());
						officerInfo2.setPlayerMsg(officerInfo1.getPlayerMsg());
						
						isHave = true;
					}
				}
				
				if (!isHave) {
					OfficerInfo.Builder infoBuilder = OfficerInfo.newBuilder();
					infoBuilder.setEndTime(officerInfo1.getEndTime());
					if (officerInfo1.hasPlayerMsg()) {
						infoBuilder.setPlayerMsg(officerInfo1.getPlayerMsg());
					}					
					infoBuilder.setOfficerId(officerInfo1.getOfficerId());
					robotEntity.getBasicData().getOfficerList().add(infoBuilder);
				}
			}
		}
		
		RobotLog.cityPrintln("officer info:{}", sinfo.getOfficersList() == null ? "" : sinfo.getOfficersList());
	}

	private void doResourceInfoResp(GameRobotEntity robotEntity, HawkProtocol protocol) {
		PresidentResourceInfoSyn resourceInfo = protocol.parseProtocol(PresidentResourceInfoSyn.getDefaultInstance()); 
		
		RobotLog.cityPrintln("resceour info syn player:{}, info:{}", robotEntity.getPlayerId(), resourceInfo.toString());
	}

	private void doGiftRecordUpdateResp(GameRobotEntity robotEntity, HawkProtocol protocol) {
		PresidentGiftRecordsUpdate cparam = protocol.parseProtocol(PresidentGiftRecordsUpdate.getDefaultInstance());
		StringBuilder sb = new StringBuilder(256);
		for (GiftRecord gr : cparam.getGiftRecordList()) {
			sb.append(gr.toString());
			sb.append("\r\n");
		}
		
		RobotLog.cityPrintln("do giftRecordUpdateResp player:{}, record:{}", robotEntity.getPlayerId(), sb.toString());
	}

	private void doGiftRecordResp(GameRobotEntity robotEntity, HawkProtocol protocol) {
		PresidentGiftRecordRes sresp = protocol.parseProtocol(PresidentGiftRecordRes.getDefaultInstance()); 
		List<GiftRecord> recordList = sresp.getGiftRecordList();
		StringBuilder sb = new StringBuilder();
		if (recordList != null && !recordList.isEmpty()) {			
			for (GiftRecord gr : recordList) {
				sb.append(gr.toString());
				sb.append("\r\n");
			}
		}
		
		RobotLog.cityPrintln("receive gift playerId:{}, record:{}", robotEntity.getPlayerId(), sb.toString());
	}

	private void doGiftInfoResp(GameRobotEntity robotEntity, HawkProtocol protocol) {
		PresidentGiftInfo sinfo = protocol.parseProtocol(PresidentGiftInfo.getDefaultInstance());
		if (sinfo.getGiftInfoList() != null && !sinfo.getGiftInfoList().isEmpty()) {
			robotEntity.getBasicData().getGiftList().clear();
			robotEntity.getBasicData().getGiftList().addAll(sinfo.getGiftInfoList());
		}
		
		RobotLog.cityPrintln("receive gift info");
		
	}

	private void doSearchResp(GameRobotEntity robotEntity, HawkProtocol protocol) {
		PresidentSearchRes cparam = protocol.parseProtocol(PresidentSearchRes.getDefaultInstance());
		List<MemeberInfo> memberInfoList = cparam.getMemeberInfoList();
		
		if (memberInfoList != null && !memberInfoList.isEmpty()) {
			robotEntity.getBasicData().getMemberInfoList().clear();
			robotEntity.getBasicData().getMemberInfoList().addAll(memberInfoList);
		}
		
		RobotLog.cityPrintln("receive type:{}", protocol.getType());
	}

}
 