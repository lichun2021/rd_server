package com.hawk.game.module.nation;

import java.util.Map;
import java.util.Map.Entry;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkTime;

import com.hawk.game.config.NationConstCfg;
import com.hawk.game.config.NationTechCfg;
import com.hawk.game.entity.DailyDataEntity;
import com.hawk.game.global.LocalRedis;
import com.hawk.game.item.AwardItems;
import com.hawk.game.manager.AssembleDataManager;
import com.hawk.game.nation.NationService;
import com.hawk.game.nation.NationalConst;
import com.hawk.game.nation.tech.NationTechCenter;
import com.hawk.game.nation.tech.NationTechResearchTemp;
import com.hawk.game.nation.tech.skill.NationTechSkill107;
import com.hawk.game.nation.tech.skill.NationTechSkill111;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerModule;
import com.hawk.game.president.PresidentFightService;
import com.hawk.game.president.PresidentOfficier;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.Const.MailRewardStatus;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.National.NationBuildingState;
import com.hawk.game.protocol.National.NationRedDot;
import com.hawk.game.protocol.National.NationTechCommonReq;
import com.hawk.game.protocol.National.NationTechInfo;
import com.hawk.game.protocol.National.NationTechPageInfo;
import com.hawk.game.protocol.National.NationTechResearchInfo;
import com.hawk.game.protocol.National.NationTechSkillInfo;
import com.hawk.game.protocol.National.NationbuildingType;
import com.hawk.game.protocol.Status;
import com.hawk.game.service.chat.ChatParames;
import com.hawk.game.service.chat.ChatService;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mail.SystemMailService;
import com.hawk.game.util.GsConst;

/**
 * 国家科技
 * @author Golden
 *
 */
public class PlayerNationTechModule extends PlayerModule {
	public PlayerNationTechModule(Player player) {
		super(player);
	}

	@Override
	public boolean onTick() {
		// 检测小红点
		checkRD();
		return true;
	}
	
	/**
	 * 检测小红点
	 */
	private void checkRD() {
		NationTechCenter center = (NationTechCenter)NationService.getInstance().getNationBuildingByType(NationbuildingType.NATION_TECH_CENTER);
		if (center == null || center.getLevel() <= 0) {
			return;
		}
		
		// 是否有研究中的科技
		NationTechResearchTemp currTechResearch = center.getNationTechResearch();
		
		// 当日已经帮助次数
		int helpTimes = player.getData().getDailyDataEntity().getNationTechHelp();
		
		if (currTechResearch != null && helpTimes <= 0) {
			player.updateNationRD(NationRedDot.TECH_IDLE);
		} else {
			player.rmNationRD(NationRedDot.TECH_IDLE);
		}
	}
	
	/**
	 * 同步界面信息
	 */
	private void syncPageInfo() {
		NationTechCenter center = (NationTechCenter)NationService.getInstance().getNationBuildingByType(NationbuildingType.NATION_TECH_CENTER);
		if (center == null || center.getLevel() <= 0) {
			return;
		}
		center.doCheck();
		
		NationTechPageInfo.Builder builder = NationTechPageInfo.newBuilder();
		builder.setTechValue(center.getTechValue());
		builder.setBuildLevel(center.getLevel());
		
		Map<Integer, Integer> nationTechMap = center.getNationTech();
		for (Entry<Integer, Integer> nationTech : nationTechMap.entrySet()) {
			NationTechInfo.Builder techBuilder = NationTechInfo.newBuilder();
			techBuilder.setTechCfgId(nationTech.getKey());
			techBuilder.setLevel(nationTech.getValue());
			builder.addTech(techBuilder);
		}
		
		Map<Integer, Long> nationTechSkillMap = center.getNationTechSkill();
		for (Entry<Integer, Long> nationTechSkill : nationTechSkillMap.entrySet()) {
			NationTechSkillInfo.Builder skillBuilder = NationTechSkillInfo.newBuilder();
			int techCfgId = nationTechSkill.getKey();
			skillBuilder.setTechCfgId(techCfgId);
			NationTechCfg techCfg = AssembleDataManager.getInstance().getNationTech(techCfgId, 1);
			if (nationTechSkill.getValue() + techCfg.getTechSkillCd() > HawkTime.getMillisecond()) {
				skillBuilder.setRefreshTime(nationTechSkill.getValue() + techCfg.getTechSkillCd());
				builder.addTechSkill(skillBuilder);
			}
		}
		
		DailyDataEntity dailyDataEntity = player.getData().getDailyDataEntity();
		NationTechResearchTemp techResearch = center.getNationTechResearch();
		if (techResearch != null) {
			NationTechResearchInfo.Builder techResearchBuilder = NationTechResearchInfo.newBuilder();
			techResearchBuilder.setTechCfgId(techResearch.getTechCfgId());
			techResearchBuilder.setTarLevel(techResearch.getTarLevel());;
			techResearchBuilder.setFinishTime(techResearch.getEndTime());
			techResearchBuilder.setHelped(dailyDataEntity.getNationTechHelp() > 0);
			techResearchBuilder.setHelpTime(techResearch.getHelpTime());
			builder.setTechResearch(techResearchBuilder);
		}
		
		sendProtocol(HawkProtocol.valueOf(HP.code2.NATIONAL_TECH_PAGE_INFO_RESP_VALUE, builder));
	}
	
	/**
	 * 国家科技界面信息请求
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code2.NATIONAL_TECH_PAGE_INFO_REQ_VALUE)
	private void onPageInfoReq(HawkProtocol protocol) {
		syncPageInfo();
	}
	
	/**
	 * 国家科技研究
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code2.NATIONAL_TECH_RESEARCH_REQ_VALUE)
	private void onResearchReq(HawkProtocol protocol) {
		// 权限判断
		if (!PresidentFightService.getInstance().isPresidentPlayer(player.getId()) && !PresidentOfficier.getInstance().isMeritoriousOfficials(player.getId())) {
			sendError(protocol.getType(), Status.Error.ONLY_OFFICER_TO_OPER);
			return;
		}
		
		// 建筑判断
		NationTechCenter center = (NationTechCenter)NationService.getInstance().getNationBuildingByType(NationbuildingType.NATION_TECH_CENTER);
		if (center == null || center.getLevel() <= 0) {
			return;
		}
		
		// 升级中不能研究
		if (center.getBuildState() == NationBuildingState.BUILDING) {
			sendError(protocol.getType(), Status.Error.NATION_TECH_BUILD_VALUE);
			return;
		}
		
		// 当前有正在研究的科技
		NationTechResearchTemp currTechResearch = center.getNationTechResearch();
		if (currTechResearch != null) {
			sendError(protocol.getType(), Status.Error.NATION_TECH_RESEARCH_VALUE);
			syncPageInfo();
			return;
		}
		
		NationTechCommonReq req = protocol.parseProtocol(NationTechCommonReq.getDefaultInstance());
		int techCfgId = req.getTechCfgId();
		
		// 目标等级
		int tarLevel = 1;
		Integer nationTech = center.getNationTech().get(techCfgId);
		if (nationTech != null) {
			tarLevel = nationTech.intValue() + 1;
		}
		
		// 目标等级配置不存在
		NationTechCfg nationTechCfg = AssembleDataManager.getInstance().getNationTech(techCfgId, tarLevel);
		if (nationTechCfg == null) {
			syncPageInfo();
			syncPageInfo();
			return;
		}
		
		// 建筑等级限制
		if (center.getLevel() < nationTechCfg.getFrontBuildLv()) {
			syncPageInfo();
			return;
		}
		
		// 前置科技等级限制
		Map<Integer, Integer> frontTechMap = nationTechCfg.getFrontTechMap();
		for (Entry<Integer, Integer> frontTech : frontTechMap.entrySet()) {
			Integer tech = center.getNationTech().get(frontTech.getKey());
			if (tech == null || tech.intValue() < frontTech.getValue()) {
				syncPageInfo();
				return;
			}
		}
		
		// 检测/扣科技值
		if (!center.changeNationTechValue(0 - nationTechCfg.getTechCost())) {
			syncPageInfo();
			return;
		}
		
		// 研究结构体
		long endTime = HawkTime.getMillisecond() + nationTechCfg.getTechTime();
		NationTechResearchTemp techResearch = new NationTechResearchTemp(techCfgId, endTime, tarLevel, HawkTime.getYearDay());
		center.updateNationTechResearchInfo(techResearch);
		
		center.enterRunnigState();
		center.boardcastBuildState();
		
		// 回包
		player.responseSuccess(protocol.getType());
		syncPageInfo();
		
		center.tlogResearch(player, techCfgId, tarLevel, nationTechCfg.getTechCost(), center.getTechValue(), center.getLevel());
	}
	
	/**
	 * 国家科技研究
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code2.NATIONAL_TECH_GIVE_UP_REQ_VALUE)
	private void onGiveUpReq(HawkProtocol protocol) {
		// 权限判断
		if (!PresidentFightService.getInstance().isPresidentPlayer(player.getId()) && !PresidentOfficier.getInstance().isMeritoriousOfficials(player.getId())) {
			sendError(protocol.getType(), Status.Error.ONLY_OFFICER_TO_OPER);
			return;
		}
		
		// 建筑判断
		NationTechCenter center = (NationTechCenter)NationService.getInstance().getNationBuildingByType(NationbuildingType.NATION_TECH_CENTER);
		if (center == null || center.getLevel() <= 0) {
			return;
		}
		
		// 当前没有正在研究的科技
		NationTechResearchTemp currTechResearch = center.getNationTechResearch();
		if (currTechResearch == null) {
			syncPageInfo();
			return;
		}
		
		// 判断是否有取消CD
		long now = HawkTime.getMillisecond();
		long cd = LocalRedis.getInstance().getNationCancelCd(NationbuildingType.NATION_TECH_CENTER.toString());
		if(cd > 0 && cd > now){
			long left = cd - now;
			player.sendError(protocol.getType(), Status.Error.NATION_CANCEL_IN_CD_VALUE, 0, NationalConst.formatTime(left));
			return;
		}
		// 设置取消CD
		LocalRedis.getInstance().setNationCancelCd(NationbuildingType.NATION_TECH_CENTER.toString(), now + NationConstCfg.getInstance().getTechGiveUpCD() * 1000L);
		
		// 放弃科技
		center.updateNationTechResearchInfo(null);
		
		// 返还科技值
		NationTechCfg nationTechCfg = AssembleDataManager.getInstance().getNationTech(currTechResearch.getTechCfgId(), currTechResearch.getTarLevel());
		int returnTechValue = nationTechCfg.getTechCost() * NationConstCfg.getInstance().getCancelTechReturn() / GsConst.RANDOM_MYRIABIT_BASE;
		center.changeNationTechValue(returnTechValue);
		
		center.exitRunningState();
		center.boardcastBuildState();
		
		// 回包
		player.responseSuccess(protocol.getType());
		syncPageInfo();
		
		center.tlogGiveUp(player, currTechResearch.getTechCfgId(), currTechResearch.getTarLevel(), returnTechValue, center.getTechValue());
	}
	
	/**
	 * 助力
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code2.NATIONAL_TECH_HELP_VALUE)
	private void onHelp(HawkProtocol protocol) {
		// 建筑判断
		NationTechCenter center = (NationTechCenter)NationService.getInstance().getNationBuildingByType(NationbuildingType.NATION_TECH_CENTER);
		if (center == null || center.getLevel() <= 0) {
			return;
		}
		
		// 当前没有正在研究的科技
		NationTechResearchTemp currTechResearch = center.getNationTechResearch();
		if (currTechResearch == null) {
			sendError(protocol.getType(), Status.Error.NATION_TECH_NONE);
			syncPageInfo();
			return;
		}
		
		// 已经助力过
		int helped = player.getData().getDailyDataEntity().getNationTechHelp();
		if (helped > 0) {
			syncPageInfo();
			return;
		}
		player.getData().getDailyDataEntity().setNationTechHelp(1);
		
		// 助力减少时间
		long reduceTime = NationConstCfg.getInstance().getAssistTechTime();
		if (currTechResearch.getHelpTime() + NationConstCfg.getInstance().getAssistTechTime() >= NationConstCfg.getInstance().getAssistTechLimit()) {
			reduceTime = Math.max(0, NationConstCfg.getInstance().getAssistTechLimit() - currTechResearch.getHelpTime());
		}
		
		if (reduceTime > 0) {
			// 增加助力时间
			currTechResearch.addHelpTime(reduceTime);
			// 时间减少
			currTechResearch.reduceEndTime(reduceTime);
			center.updateNationTechResearchInfo(currTechResearch);
		}
		
		// 发奖
		AwardItems award = AwardItems.valueOf(NationConstCfg.getInstance().getAssistTechAward());
		SystemMailService.getInstance().sendMail(MailParames.newBuilder()
				.setPlayerId(player.getId())
				.setMailId(MailId.NATION_TECH_HELP_REWARD)
				.setRewards(award.getAwardItems())
				.setAwardStatus(MailRewardStatus.NOT_GET)
				.build());
		
		// 回包
		player.responseSuccess(protocol.getType());
		syncPageInfo();
		
		center.tlogHelp(player, currTechResearch.getTechCfgId(), currTechResearch.getTarLevel(), currTechResearch.getEndTime() - HawkTime.getMillisecond());
	}
	
	/**
	 * 使用技能
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code2.NATIONAL_TECH_USE_SKILL_VALUE)
	private void onUseSkill(HawkProtocol protocol) {
		// 权限判断
		if (!PresidentFightService.getInstance().isPresidentPlayer(player.getId()) && !PresidentOfficier.getInstance().isMeritoriousOfficials(player.getId())) {
			sendError(protocol.getType(), Status.Error.ONLY_OFFICER_TO_OPER);
			return;
		}
		
		// 建筑判断
		NationTechCenter center = (NationTechCenter)NationService.getInstance().getNationBuildingByType(NationbuildingType.NATION_TECH_CENTER);
		if (center == null || center.getLevel() <= 0) {
			return;
		}
		
		NationTechCommonReq req = protocol.parseProtocol(NationTechCommonReq.getDefaultInstance());
		int techId = req.getTechCfgId();
		
		NationTechCfg techCfg = AssembleDataManager.getInstance().getNationTech(techId, 1);
		
		// 没有这个科技
		int nationTechLevel = center.getNationTechLevel(techId);
		if (nationTechLevel <= 0) {
			return;
		}
		
		// 技能还在cd中
		long curretTime = HawkTime.getMillisecond();
		long lastTouchSkillTime = center.getNationTechSkill(techId);
		if (lastTouchSkillTime + techCfg.getTechSkillCd() > HawkTime.getMillisecond()) {
			return;
		}
		center.updateNationTechSkillInfo(techId, curretTime);
		
		// 消耗科技
		if (!center.changeNationTechValue(0 - techCfg.getSkillCost())) {
			return;
		}
		
		// 释放技能
		touchSkill(techId);
		
		// 回包
		player.responseSuccess(protocol.getType());
		syncPageInfo();
		
		center.tlogSkill(player, techId, nationTechLevel, techCfg.getSkillCost(), center.getTechValue());
	}
	
	/**
	 * 触发技能
	 * @param techId
	 */
	private void touchSkill(int techId) {
		try {
			if (techId == 107) {
				NationTechSkill107.touchSkill();
			} else if (techId == 111) {
				NationTechSkill111.touchSkill();
			} else {
				sendError(HP.code2.NATIONAL_TECH_USE_SKILL_VALUE, Status.Error.NATION_TECH_SKILL_NOT_OPEN_VALUE);
				return;
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		Const.NoticeCfgId noticeId = Const.NoticeCfgId.NATION_TECH_SKILL;
		ChatParames build = ChatParames.newBuilder()
			.setChatType(Const.ChatType.SPECIAL_BROADCAST)
			.setKey(noticeId)
			.setPlayer(player)
			.addParms(techId)
			.build();
		ChatService.getInstance().addWorldBroadcastMsg(build);
	}
}
