package com.hawk.game.module;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.hawk.annotation.MessageHandler;
import org.hawk.annotation.ProtocolHandler;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;

import com.hawk.game.entity.GuildMemberObject;
import com.hawk.game.entity.item.GuildFormationCell;
import com.hawk.game.entity.item.GuildFormationObj;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.msg.GuildQuitMsg;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerModule;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.MassFormation.FormationBriefInfo;
import com.hawk.game.protocol.MassFormation.FormationBriefPage;
import com.hawk.game.protocol.MassFormation.FormationDetialInfo;
import com.hawk.game.protocol.MassFormation.FormationJoinNotice;
import com.hawk.game.protocol.MassFormation.FormationJoinNoticePush;
import com.hawk.game.protocol.MassFormation.FormationMemberInfo;
import com.hawk.game.protocol.MassFormation.FormationOper;
import com.hawk.game.protocol.MassFormation.FormationOperType;
import com.hawk.game.protocol.MassFormation.FormationRedPush;
import com.hawk.game.protocol.MassFormation.MassFormationIndex;
import com.hawk.game.protocol.MassFormation.RemoveFormationNotice;
import com.hawk.game.protocol.Player.MsgCategory;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.SysProtocol.DataHolder;
import com.hawk.game.protocol.SysProtocol.HPOperateSuccess;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.service.GuildService;
import com.hawk.game.tsssdk.GameMsgCategory;
import com.hawk.game.tsssdk.GameTssService;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.LogUtil;
import com.hawk.game.util.GsConst.GuildOffice;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.march.IWorldMarch;
import com.hawk.game.world.thread.WorldTask;
import com.hawk.game.world.thread.WorldThreadScheduler;
import com.hawk.gamelog.GameLog;
import com.hawk.gamelog.LogParam;
import com.hawk.log.LogConst.LogInfoType;

/**
 * 联盟编队
 * 
 * @author Golden
 *
 */
public class GuildFormationModule extends PlayerModule {

	/**
	 * 上次推送时间
	 */
	private long lastPushTime = 0;
	
	/**
	 * 推送小红点标记
	 */
	private String pushRedMark = "";
	
	/**
	 * 推送通知标记
	 */
	private String pushNoticeMark = "";
	
	/**
	 * 取消通知的行军ID
	 */
	private Set<String> delNoticeSet = new HashSet<>(); 
			
	public GuildFormationModule(Player player) {
		super(player);
	}

	@Override
	public boolean onTick() {
		if (player.isInDungeonMap()) {
			return true;
		}
		
		// 1s检查一次,控制下频率
		long currentTime = HawkTime.getMillisecond();
		if (currentTime - lastPushTime < 1000L) {
			return true;
		}
		lastPushTime = currentTime;
		
		checkRedPush();
		checkJoinPush();
		return true;
	}
	
	@Override
	protected boolean onPlayerLogin() {
		player.setGuildFormationChangeMark(1); //登录后必取一次redis数据
		return true;
	}
	
	/**
	 * 检测集结加入推送
	 */
	private void checkJoinPush() {
		Set<String> markSet = new HashSet<>();
		
		// 需要自己加入的集结&自己还没有派遣队列加入
		FormationJoinNoticePush.Builder push = FormationJoinNoticePush.newBuilder();
		if (player.hasGuild()) {
			GuildFormationObj formationObj = getGuildFormation();
			if (formationObj == null) {
				return;
			}
			for (GuildFormationCell formation : formationObj.getFormations()) {
				// 不需要出征
				if (!formation.fight(player.getId())) {
					continue;
				}
				Set<String> noticeMarchIds = formation.getNoticeJoinMarchIds(player.getId());
				for (String marchId : noticeMarchIds) {
					if (delNoticeSet.contains(marchId)) {
						continue;
					}
					IWorldMarch march = WorldMarchService.getInstance().getMarch(marchId);
					if (march == null) {
						continue;
					}
					FormationJoinNotice.Builder notice = FormationJoinNotice.newBuilder();
					notice.setIndex(formation.getIndex());
					notice.setMarchId(marchId);
					notice.setIsLeader(formation.isLeader(player.getId()));
					notice.setName(formation.getName());
					long marchStartTime = WorldMarchService.getInstance().getMarchStartTime(marchId);
					notice.setMassStartTime(marchStartTime);
					WorldMarchStatus status = WorldMarchService.getInstance().getMassJoinMarchStatus(player.getId(), marchId);
					markSet.add(marchId + status);
					if (status != null) {
						notice.setStatus(status);
					}
					push.addInfo(notice);
				}
			}
		}
		
		// 变化才推送
		String mark = markSet.toString();
		if (!mark.equals(pushNoticeMark)) {
			pushNoticeMark = mark;
			player.sendProtocol(HawkProtocol.valueOf(HP.code2.MASS_FORMATION_JOIN_NOTICE, push));
		}
	}

	/**
	 * 跨服处理,makeSure一下
	 * @return
	 */
	private GuildFormationObj getGuildFormation() {
		return GuildService.getInstance().getGuildFormation(player.getGuildId());
	}
	
	/**
	 * 编队操作
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code2.MASS_FORMATION_OPER_REQ_VALUE)
	private boolean formationOper(HawkProtocol protocol) {
		WorldThreadScheduler.getInstance().postWorldTask(new WorldTask(GsConst.WorldTaskType.GUILD_FORMATION) {
			@Override
			public boolean onInvoke() {
				FormationOper oper = protocol.parseProtocol(FormationOper.getDefaultInstance());
				formationOper(oper, protocol.getType());
				return true;
			}
		});
		return true;
	}

	private void formationOper(FormationOper oper, int proto) {
		// 没有联盟
		if (!player.hasGuild()) {
			return;
		}

		// 编队初始化检测
		GuildFormationObj obj = getGuildFormation();
		obj.initCheck();
		obj.checkMarchIdRemove();

		int operType = oper.getType().getNumber();

		switch (operType) {

		// 请求编队简要信息
		case FormationOperType.FORMATION_BRIEF_VALUE:
			formationBrief();
			break;

		// 请求编队详细信息
		case FormationOperType.FORMATION_DETIAL_VALUE:
			formationDetial(oper.getIndex());
			break;

		// 请求队长简要编队信息(用于出征)
		case FormationOperType.FORMATION_LEADER_BRIEF_VALUE:
			formationLeaderBrief(oper);
			break;

		// 设置队长
		case FormationOperType.SET_LEADER_VALUE:
			setLeader(oper);
			break;

		// 出战
		case FormationOperType.SET_FIGHT_VALUE:
		case FormationOperType.DEL_FIGHT_VALUE:
			fightOper(oper);
			break;

		// 设置编队名字
		case FormationOperType.SET_NAME_VALUE:
			setName(oper);
			break;

		// 删除小红点
		case FormationOperType.DEL_RED_VALUE:
			RedisProxy.getInstance().delGuildFormationRed(player.getId(), oper.getIndex().getNumber());
			break;
			
		default:
			break;
		}

		// 通用成功返回
		respSuccess(proto, operType);
	}

	/**
	 * 编队简要信息
	 */
	private void formationBrief() {
		FormationBriefPage.Builder builder = FormationBriefPage.newBuilder();
		GuildFormationObj obj = getGuildFormation();
		for (GuildFormationCell formation : obj.getFormations()) {
			try {
				FormationBriefInfo.Builder brief = FormationBriefInfo.newBuilder();
				brief.setIndex(formation.getIndex());
				brief.setName(formation.getName());

				String leaderId = formation.getLeaderId();
				if (!HawkOSOperator.isEmptyString(leaderId)) {
					Player leader = GlobalData.getInstance().makesurePlayer(leaderId);
					if (leader == null) {
						HawkLog.errPrintln("GuildFormationModule formationBrief break, leader empty: {}, playerId: {},", leaderId, player.getId());
						continue;
					}
					brief.setLdIcon(leader.getIcon());
					brief.setLdName(leader.getName());
					brief.setLdPfIcon(leader.getPfIcon());
					brief.setLeader(leaderId.equals(player.getId()));
					brief.setMassCount(leader.getMaxMassJoinMarchNum());
				}

				brief.setMemberCount(formation.getMemberCount());
				brief.setUpdateTime(formation.getUpdateTime());
				brief.setJoine(formation.fight(player.getId()));
				builder.addInfo(brief);
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.MASS_FORMATION_BRIEF_PAGE_RESP, builder));
	}

	/**
	 * 编队详细信息
	 */
	private void formationDetial(MassFormationIndex index) {
		GuildFormationObj obj = getGuildFormation();
		GuildFormationCell formation = obj.getFormation(index);

		FormationDetialInfo.Builder builder = FormationDetialInfo.newBuilder();
		builder.setIndex(formation.getIndex());
		builder.setName(formation.getName());
		Collection<String> members = GuildService.getInstance().getGuildMembers(player.getGuildId());
		for (String memberId : members) {
			try {
				Player member = GlobalData.getInstance().makesurePlayer(memberId);
				FormationMemberInfo.Builder memberInfo = FormationMemberInfo.newBuilder();
				memberInfo.setPlayerId(member.getId());
				memberInfo.setPlayerName(member.getName());
				memberInfo.setIcon(member.getIcon());
				memberInfo.setPfIcon(member.getPfIcon());
				memberInfo.setPower(member.getPower());
				memberInfo.setAuthory(GuildService.getInstance().getPlayerGuildAuthority(member.getId()));
				memberInfo.setOfficer(GuildService.getInstance().getGuildMemberOfficer(member.getId()));
				memberInfo.setFight(formation.fight(member.getId()));
				memberInfo.setIsLeader(member.getId().equals(formation.getLeaderId()));
				builder.addMemberInfo(memberInfo);
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.MASS_FORMATION_DETIAL_INFO_RESP, builder));
	}

	/**
	 * 队长简要编队信息(用于出征)
	 */
	private void formationLeaderBrief(FormationOper oper) {
		FormationBriefPage.Builder builder = FormationBriefPage.newBuilder();
		GuildFormationObj obj = getGuildFormation();
		for (GuildFormationCell formation : obj.getFormations()) {
			try {
				String leaderId = formation.getLeaderId();
				if (HawkOSOperator.isEmptyString(leaderId)) {
					continue;
				}
				if (!leaderId.equals(player.getId())) {
					continue;
				}
				FormationBriefInfo.Builder brief = FormationBriefInfo.newBuilder();
				brief.setIndex(formation.getIndex());
				brief.setName(formation.getName());
				builder.addInfo(brief);
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.MASS_FORMATION_BRIEF_PAGE_RESP, builder));
	}

	/**
	 * 设置队长
	 * 
	 * @param oper
	 */
	private void setLeader(FormationOper oper) {
		// 权限判断,盟主和官员有权限
		int officer = GuildService.getInstance().getGuildMemberOfficer(player.getId());
		if (officer == GuildOffice.NONE.value()) {
			return;
		}
		// 目标不存在
		GuildMemberObject tarMember = GuildService.getInstance().getGuildMemberObject(oper.getParam());
		if (tarMember == null) {
			return;
		}
		GuildFormationObj obj = getGuildFormation();
		GuildFormationCell formation = obj.getFormation(oper.getIndex());

		// 设置队长
		formation.setLeaderId(tarMember.getPlayerId());
		formation.addFight(tarMember.getPlayerId());
		formation.setUpdateTime(HawkTime.getMillisecond());
		obj.notifyUpdate();

		// 返回编队信息
		formationDetial(oper.getIndex());
		formationBrief();
		
		// 添加小红点小红点
		if (!oper.getParam().equals(player.getId())) {
			RedisProxy.getInstance().addGuildFormationRed(oper.getParam(), oper.getIndex().getNumber());
		}
		
		// 日志记录
		tlog(player, formation, oper.getType(), oper.getParam());
	}

	/**
	 * 设置出战
	 * 
	 * @param oper
	 */
	private void fightOper(FormationOper oper) {
		GuildFormationObj obj = getGuildFormation();
		GuildFormationCell formation = obj.getFormation(oper.getIndex());
		boolean isLeader = player.getId().equals(formation.getLeaderId());

		// 权限判断,盟主和官员、队长有权限
		int officer = GuildService.getInstance().getGuildMemberOfficer(player.getId());
		if (officer == GuildOffice.NONE.value() && !isLeader) {
			return;
		}
		
		// 目标不存在
		GuildMemberObject tarMember = GuildService.getInstance().getGuildMemberObject(oper.getParam());
		if (tarMember == null) {
			return;
		}
		
		// 队长不可以出战
		if (formation.isLeader(tarMember.getPlayerId())) {
			return;
		}

		// 设置出战
		if (oper.getType().getNumber() == FormationOperType.SET_FIGHT_VALUE) {
			formation.addFight(tarMember.getPlayerId());
			// 添加小红点
			if (!oper.getParam().equals(player.getId())) {
				RedisProxy.getInstance().addGuildFormationRed(oper.getParam(), oper.getIndex().getNumber());
			}
		} else {
			formation.delFight(tarMember.getPlayerId());
			// 删除小红点
			RedisProxy.getInstance().delGuildFormationRed(oper.getParam(), oper.getIndex().getNumber());
		}

		formation.setUpdateTime(HawkTime.getMillisecond());
		obj.notifyUpdate();

		// 返回编队信息
		formationDetial(oper.getIndex());
		formationBrief();
		
		// 日志记录
		tlog(player, formation, oper.getType(), oper.getParam());
	}

	/**
	 * 改名字
	 * 
	 * @param oper
	 */
	private void setName(FormationOper oper) {
		GuildFormationObj obj = getGuildFormation();
		GuildFormationCell formation = obj.getFormation(oper.getIndex());
		boolean isLeader = player.getId().equals(formation.getLeaderId());

		// 权限判断,盟主和官员、队长有权限
		int officer = GuildService.getInstance().getGuildMemberOfficer(player.getId());
		if (officer == GuildOffice.NONE.value() && !isLeader) {
			return;
		}
		
		// 禁言检测
		if (!GameUtil.checkBanMsg(player)) {
			return;
		}

		GameTssService.getInstance().wordUicChatFilter(player, oper.getParam(), MsgCategory.GUILD_FORMATION.getNumber(),
				GameMsgCategory.GUILD_FORMATION, String.valueOf(oper.getIndex().getNumber()), null,
				HP.code2.MASS_FORMATION_OPER_REQ_VALUE);
	}

	/**
	 * 改名字回调
	 * 
	 * @param success
	 * @param guildId
	 * @param name
	 */
	public void setNameInvoker(boolean success, int index, String name) {
		if (!success) {
			player.sendError(HP.code2.MASS_FORMATION_OPER_REQ_VALUE, Status.NameError.CONTAIN_ILLEGAL_CHART_VALUE, 0);
			return;
		}
		GuildFormationObj obj = getGuildFormation();
		MassFormationIndex iden = MassFormationIndex.valueOf(index);
		GuildFormationCell formation = obj.getFormation(iden);
		formation.setUpdateTime(HawkTime.getMillisecond());
		formation.setName(name);
		obj.notifyUpdate();
		formationDetial(iden);
		formationBrief();
		
		// 日志记录
		tlog(player, formation, FormationOperType.SET_NAME, name);
	}

	/**
	 * 成功返回
	 * 
	 * @param hp
	 * @param oper
	 */
	private void respSuccess(int hp, int oper) {
		HPOperateSuccess.Builder builder = HPOperateSuccess.newBuilder();
		builder.setHpCode(hp);
		DataHolder.Builder holder = DataHolder.newBuilder();
		holder.setInt32Val(oper);
		builder.addDatas(holder);
		player.sendProtocol(HawkProtocol.valueOf(HP.sys.OPERATE_SUCCESS, builder));
	}

	
	/**
	 * 小红点推送
	 * @param playerId
	 */
	private void checkRedPush() {
		try {
			Set<Integer> markSet = new HashSet<>();
			int changeMark = player.getGuildFormationChangeMark();
			FormationRedPush.Builder builder = FormationRedPush.newBuilder();
			if (player.hasGuild() && changeMark > 0) {
				Set<Integer> redSet = RedisProxy.getInstance().getGuildFormationRed(player.getId());
				for (int red : redSet) {
					markSet.add(red);
					builder.addIndex(MassFormationIndex.valueOf(red));
				}
				player.setGuildFormationChangeMark(0);
			}
			
			// 变化才推送
			String mark = markSet.toString();
			if (!mark.equals(pushRedMark) && changeMark > 0) {
				pushRedMark = mark;
				player.sendProtocol(HawkProtocol.valueOf(HP.code2.MASS_FORMATION_RED_PUSH_VALUE, builder));
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 退盟
	 * @param msg
	 */
	@MessageHandler
	private void onQuitGuild(GuildQuitMsg msg) {
		RedisProxy.getInstance().delGuildFormationRed(player.getId());
	}
	
	/**
	 * tlog日志
	 * @param player
	 * @param cell
	 * @param type
	 */
	public void tlog(Player player, GuildFormationCell cell, FormationOperType type, String param) {
//		if(Objects.isNull(cell)){
//			return;
//		}
//		try {
//			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.guild_formation);
//		    logParam.put("guildId", player.getGuildId())
//					.put("guildName", GuildService.getInstance().getGuildName(player.getGuildId()))
//					.put("formIndex", cell.getIndex())
//					.put("formName", cell.getName())
//					.put("leaderId", cell.getLeaderId())
//					.put("fightIds", cell.getFightIds().toString())
//					.put("formOper", type)
//					.put("param", param)
//					;
//			GameLog.getInstance().info(logParam);
//		} catch (Exception e) {
//			HawkException.catchException(e);
//		}
	}
	
	/**
	 * 取消待集结提醒
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code2.MASS_FORMATION_REMOVE_NOTICE_VALUE)
	private boolean delNotice(HawkProtocol protocol) {
		RemoveFormationNotice oper = protocol.parseProtocol(RemoveFormationNotice.getDefaultInstance());
		delNoticeSet.add(oper.getMarchId());
		return true;
	}
	
	/**
	 * 是否忽略了提醒
	 * @param marchId
	 * @return
	 */
	public boolean delNotice(String marchId) {
		return delNoticeSet.contains(marchId);
	}
}
