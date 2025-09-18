package com.hawk.game.service.college;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.hawk.app.HawkApp;
import org.hawk.app.HawkAppObj;
import org.hawk.collection.ConcurrentHashSet;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBManager;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkRand;
import org.hawk.os.HawkTime;
import org.hawk.tuple.HawkTuple2;
import org.hawk.tuple.HawkTuple4;
import org.hawk.tuple.HawkTuples;
import org.hawk.uuid.HawkUUIDGenerator;
import org.hawk.xid.HawkXID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;
import com.hawk.game.GsConfig;
import com.hawk.game.crossproxy.CrossService;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.LocalRedis;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.invoker.CollegeCoahAuthChangeMsg;
import com.hawk.game.item.AwardItems;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.log.BehaviorLogger;
import com.hawk.game.log.BehaviorLogger.Params;
import com.hawk.game.module.college.CollegeConst.GiftRefreshType;
import com.hawk.game.module.college.CollegeConst.ShopRefreshType;
import com.hawk.game.module.college.cfg.CollegeConstCfg;
import com.hawk.game.module.college.cfg.CollegeLevelCfg;
import com.hawk.game.module.college.cfg.CollegeOnlineRewardCfg;
import com.hawk.game.module.college.cfg.CollegePurchaseCfg;
import com.hawk.game.module.college.cfg.CollegeShopCfg;
import com.hawk.game.module.college.entity.CollegeEffect;
import com.hawk.game.module.college.entity.CollegeEntity;
import com.hawk.game.module.college.entity.CollegeMemberEntity;
import com.hawk.game.module.college.entity.CollegeMemberGiftEntity;
import com.hawk.game.module.college.entity.CollegeMemberShopEntity;
import com.hawk.game.module.college.entity.CollegeMissionEntityItem;
import com.hawk.game.module.college.entity.CollegeStatisticsEntity;
import com.hawk.game.msg.CollegeJoinMsg;
import com.hawk.game.msg.CollegeQuitMsg;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.MailRewardStatus;
import com.hawk.game.protocol.Const.PlayerAttr;
import com.hawk.game.protocol.Const.PushMsgType;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.MilitaryCollege.CollegeAuth;
import com.hawk.game.protocol.MilitaryCollege.CollegeBaseInfo;
import com.hawk.game.protocol.MilitaryCollege.CollegeEffectUpdateResp;
import com.hawk.game.protocol.MilitaryCollege.CollegeExchangeResp;
import com.hawk.game.protocol.MilitaryCollege.CollegeGiftBuyResp;
import com.hawk.game.protocol.MilitaryCollege.CollegeGiftItem;
import com.hawk.game.protocol.MilitaryCollege.CollegeInfo;
import com.hawk.game.protocol.MilitaryCollege.CollegeMemberData;
import com.hawk.game.protocol.MilitaryCollege.CollegeMemberInfo;
import com.hawk.game.protocol.MilitaryCollege.CollegeMission;
import com.hawk.game.protocol.MilitaryCollege.CollegeRecommendListResp;
import com.hawk.game.protocol.MilitaryCollege.CollegeScore;
import com.hawk.game.protocol.MilitaryCollege.CollegeSearchResp;
import com.hawk.game.protocol.MilitaryCollege.CollegeShopItem;
import com.hawk.game.protocol.MilitaryCollege.CollegeVitality;
import com.hawk.game.protocol.MilitaryCollege.CollegeVitalityItem;
import com.hawk.game.protocol.MilitaryCollege.CollegeVitalitySendResp;
import com.hawk.game.protocol.MilitaryCollege.GetApplyListResp;
import com.hawk.game.protocol.MilitaryCollege.GetCollegeLeaderResp;
import com.hawk.game.protocol.MilitaryCollege.OnlineRewardInfo;
import com.hawk.game.protocol.MilitaryCollege.OnlineRewardInfoResp;
import com.hawk.game.protocol.MilitaryCollege.PBCollegeEffect;
import com.hawk.game.protocol.MilitaryCollege.RewardInfo;
import com.hawk.game.protocol.MilitaryCollege.RewardStatus;
import com.hawk.game.protocol.Status;
import com.hawk.game.queryentity.AccountInfo;
import com.hawk.game.service.MailService;
import com.hawk.game.service.PushService;
import com.hawk.game.service.SearchService;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mail.SystemMailService;
import com.hawk.game.util.BuilderUtil;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.LogUtil;
import com.hawk.game.world.service.WorldPointService;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.Action;
import com.hawk.log.Source;

/**
 *
 * 联盟服务管理器
 * 
 * @author shadow
 *
 */
public class CollegeService extends HawkAppObj {
	static Logger logger = LoggerFactory.getLogger("Server");
	/**
	 * 服务器学院信息
	 */
	private Map<String, CollegeEntity> collegeData = new ConcurrentHashMap<>();

	/**
	 * 服务器学院成员信息
	 */
	private Map<String, Set<String>> collegeMembers = new ConcurrentHashMap<>();
	
	/**
	 * 学院做用号
	 */
	private Map<String,CollegeEffect> collegeEffect = new ConcurrentHashMap<>();

	/**
	 * tick
	 */
	private long tickTime;
	/**
	 * 单例
	 */
	private static CollegeService instance;

	/**
	 * 
	 * @return 单例
	 */
	public static CollegeService getInstance() {
		return instance;
	}

	/**
	 * 联盟服务
	 * 
	 * @param xid
	 */
	public CollegeService(HawkXID xid) {
		super(xid);
		if (instance == null) {
			instance = this;
		}
	}
	
	/**当天登录过党员数*/
	public int getLoginMemberDay(String collegeId){
		CollegeEntity entity = this.collegeData.get(collegeId);
		if(Objects.isNull(entity)){
			return 0;
		}
		return entity.getStatisticsEntity().getLoginMemberCount();
	}

	/**
	 * 加载联盟信息
	 * 
	 * @return
	 */
	public boolean init() {
		List<CollegeEntity> collegeEntities = HawkDBManager.getInstance().query("from CollegeEntity where invalid = 0");
		for (CollegeEntity collegeEntitie : collegeEntities) {
			String id = collegeEntitie.getId();
			AccountInfo coach = GlobalData.getInstance().getAccountInfoByPlayerId(collegeEntitie.getCoachId());
			if (coach != null) {
				SearchService.getInstance().addCoachName(coach.getPlayerName(), coach.getPlayerId());
			}
			collegeData.put(id, collegeEntitie);
		}
		
		List<CollegeMemberEntity> collegeMemberEntities = HawkDBManager.getInstance().query("from CollegeMemberEntity where invalid = 0");
		for (CollegeMemberEntity collegeMemberEntity : collegeMemberEntities) {
			String collegeId = collegeMemberEntity.getCollegeId();
			if (HawkOSOperator.isEmptyString(collegeId)) {
				continue;
			}
			if (!collegeData.containsKey(collegeId)) {
				continue;
			}
			if (collegeMembers.containsKey(collegeId)) {
				collegeMembers.get(collegeId).add(collegeMemberEntity.getPlayerId());
			} else {
				Set<String> set = new ConcurrentHashSet<String>();
				collegeMembers.put(collegeId, set);
				set.add(collegeMemberEntity.getPlayerId());
			}
		}
		
		//检查学院数据，主要是针对拆服后的数据处理
		this.checkCollegeData(collegeMemberEntities);

		return true;
	}
	
	
	
	/**
	 * 加载时检查数据
	 * @param collegeMemberEntities
	 */
	public void checkCollegeData(List<CollegeMemberEntity> collegeMemberEntities){
		//检查一下学院是否有名字
		Set<String> collegeNameSet = new HashSet<String>();
		int nameAdd = 1;
		for (CollegeEntity entity : this.collegeData.values()) {
			if(HawkOSOperator.isEmptyString(entity.getCollegeName())){
				String namePer = CollegeConstCfg.getInstance().getRandomName();
				entity.setCollegeName(namePer + entity.getId());
			}
			//是否重名，和服后可能重名
			if(collegeNameSet.contains(entity.getCollegeName())){
				entity.setCollegeName(entity.getCollegeName()+"-" + String.valueOf(nameAdd));
				nameAdd ++;
			}
			collegeNameSet.add(entity.getCollegeName());
			//重新算一下等级经验
			HawkTuple2<Integer, Integer> tuple = this.calCollegeLevel(entity.getExpTotal());
			if(entity.getLevel() != tuple.first || entity.getExp() != tuple.second){
				entity.setLevel(tuple.first);
				entity.setExp(tuple.second);
			}
		}
		//学员分数
		Map<String,Long> memberScore = new HashMap<>();
		for(CollegeMemberEntity mentiy : collegeMemberEntities){
			try {
				mentiy.afterRead();
				memberScore.put(mentiy.getPlayerId(), mentiy.getScoreData().getScore());
			} catch (Exception e) {
				continue;
			}
		}
		//检查无效的学院和无教官的学院重置教官
		List<CollegeEntity> dels = new ArrayList<>();
		for (CollegeEntity collegeEntitie : this.collegeData.values()) {
			Set<String> set = this.collegeMembers.get(collegeEntitie.getId());
			//如果学院没有人了就删掉了，主要是拆服后，可能有这个情况
			if(Objects.isNull(set) || set.size() <= 0){
				dels.add(collegeEntitie);
				continue;
			}
			//查看一下学院的教官是否还在，拆服后有可能教官不在了，拿积分最高的人当教官
			AccountInfo coach = GlobalData.getInstance().getAccountInfoByPlayerId(collegeEntitie.getCoachId());
			if (Objects.isNull(coach)) {
				String cid = this.checkScoreMaxMember(set, memberScore);
				Player coachPlayer = GlobalData.getInstance().makesurePlayer(cid);
				if (Objects.nonNull(coachPlayer)) {
					collegeEntitie.setCoachId(cid);
					coachPlayer.getData().getCollegeMemberEntity().setAuth(CollegeAuth.COACH_VALUE);
					coachPlayer.getData().getCollegeMemberEntity().notifyUpdate(false, 0);
				}
			} 
		}
		//删除无效的学院
		for(CollegeEntity delEntity : dels){
			delEntity.delete();
			this.collegeData.remove(delEntity.getId());
		}
		
	}
	public String checkScoreMaxMember(Set<String> set,Map<String,Long> memberScore){
		long maxScore = -1;
		String target = "";
		for(String mid : set){
			long score = memberScore.getOrDefault(mid, 0l);
			if(score > maxScore){
				maxScore = score;
				target = mid;
			}
		}
		return target;
	}
	
	
	@Override
	public boolean onTick() {
		long curTime = HawkTime.getMillisecond();
		if(curTime >= this.tickTime + 5 * 1000){
			this.tickTime = curTime;
			List<CollegeEntity> list = new ArrayList<>();
			list.addAll(this.collegeData.values());
			for(CollegeEntity entity : list){
				//更新统计数据
				this.updateCollegeStatistics(entity);
				//更新做用号
				this.updateEff(entity,true);
			}
			
			//查看是否有跨服的体力消耗
			this.updateCollegeVitCost();
			
		}
		return true;
	}
	
	
	/**
	 * 查看REDIS是否存了 相应学院的跨服体力消耗
	 */
	public void updateCollegeVitCost(){
		String serverId = GsConfig.getInstance().getServerId();
		Map<String,Integer> costMap = RedisProxy.getInstance().getCollegeMemberVitCostAnddelete(serverId);
		if(Objects.isNull(costMap)){
			return;
		}
		for (Entry<String, Integer> entry : costMap.entrySet()) {
			String collegeId = entry.getKey();
			int value = entry.getValue();
			this.addVit(collegeId, value);
		}
	}

	
	public void updateCollegeStatistics(CollegeEntity entity){
		//重置
		boolean update1 = entity.getStatisticsEntity().refreshLoginMember();
		boolean update2 = entity.getStatisticsEntity().refreshWeekScore();
		//新添统计
		List<String> onlines = this.getCollegeOnlineMember(entity.getId());
		boolean update3 = entity.getStatisticsEntity().addLoginMember(onlines);
		if(update1 || update2 || update3){
			entity.notifyUpdate();
		}
	}
	
	
	/**
	 * 检查做用号
	 * @param entity
	 */
	public void updateEff(CollegeEntity entity,boolean push){
		int level = entity.getLevel();
		int loginCount = entity.getStatisticsEntity().getLoginMemberCount();
		CollegeEffect effData = this.collegeEffect.get(entity.getId());
		if(Objects.isNull(effData)){
			effData = new CollegeEffect();
			effData.setCollegeId(entity.getId());
			this.collegeEffect.put(effData.getCollegeId(), effData);
		}
		effData.checkEffUpdate(level, loginCount, push);
	}
	
	public List<String> getCollegeOnlineMember(String collegeId){
		List<String> list = new ArrayList<>();
		Set<String> members = this.collegeMembers.get(collegeId);
		if(Objects.isNull(members)){
			return list;
		}
		for(String mid : members){
			if(GlobalData.getInstance().isOnline(mid)){
				list.add(mid);
			}
		}
		return list;
	}
	
	
	public List<String> getCollegeAllMember(String collegeId){
		List<String> list = new ArrayList<>();
		Set<String> members = this.collegeMembers.get(collegeId);
		if(Objects.isNull(members)){
			return list;
		}
		list.addAll(members);
		return list;
	}
	
	
	/**
	 * 创建学院
	 * @param player
	 * @return
	 */
	public int createCollege(Player player,String collegeName) {
		CollegeEntity collegeEntity = new CollegeEntity();
		collegeEntity.setCoachId(player.getId());
		collegeEntity.setCollegeName(collegeName);
		collegeEntity.setLevel(1);
		collegeEntity.setJoinFree(1);
		collegeEntity.setStatisticsEntity(new CollegeStatisticsEntity());
		if (HawkDBManager.getInstance().create(collegeEntity)) {
			collegeData.put(collegeEntity.getId(), collegeEntity);

			Set<String> memberIds = new HashSet<>();
			memberIds.add(player.getId());
			collegeMembers.put(collegeEntity.getId(), memberIds);
			
			//更新统计数据
			this.updateCollegeStatistics(collegeEntity);
			//更新做用号
			this.updateEff(collegeEntity,false);
			
			HawkApp.getInstance().postMsg(player.getXid(), CollegeJoinMsg.valueOf(collegeEntity.getId(), player.getId(), player.getName(), true, false));
			SearchService.getInstance().addCoachName(player.getName(), player.getId());
			LocalRedis.getInstance().removeApplyedColleges(player.getId());
			LogUtil.logCollegeCreate(player, collegeEntity.getId());
			HawkLog.logPrintln("createCollege entity create success, playerId:{}, playerName:{}, collegeId:{}", player.getId(), player.getName(), collegeEntity.getId());
			// 行为日志
			BehaviorLogger.log4Service(player, Source.COLLEGE, Action.CREATE_COLLEGE, 
					Params.valueOf("collegeId", collegeEntity.getId()),
					Params.valueOf("coachId", player.getId()),
					Params.valueOf("coachName", player.getName()));
			return Status.SysError.SUCCESS_OK_VALUE;
		}
		HawkLog.logPrintln("createCollege entity create failed, playerId:{}, playerName:{}", player.getId(), player.getName());
		return Status.SysError.DATA_ERROR_VALUE;
	}
	
	/**
	 * 解散学院
	 * @param player
	 * @return
	 */
	public int dismissCollege(Player player) {
		String collegeId = player.getCollegeId();
		int memberCnt = getCollegeMemberCnt(collegeId);
		CollegeEntity collegeEntity = collegeData.get(collegeId);
		Set<String> members = collegeMembers.get(collegeId);
		collegeMembers.remove(collegeId);
		collegeData.remove(collegeId);
		collegeEntity.delete();

		SearchService.getInstance().removeCoachName(player.getName());
		LocalRedis.getInstance().removeCollegeApplys(collegeId);

		if (members != null && members.size() > 0) {
			for (String memberId : members) {
				Player targetPlayer = GlobalData.getInstance().makesurePlayer(memberId);
				HawkApp.getInstance().postMsg(targetPlayer.getXid(), CollegeQuitMsg.valueOf(collegeId, player.getId(), player.getName(), false, true));
				//发放邮件
				MailService.getInstance().sendMail(MailParames.newBuilder()
						.setPlayerId(memberId)
						.setMailId(MailId.COLLEGE_DISMISS)
						.addSubTitles(collegeEntity.getCollegeName())
						.addSubTitles(collegeEntity.getLevel())
						.addContents(collegeEntity.getCollegeName())
						.addContents(player.getName())
						.build());
			}
		}
		//同步一下做用号
		CollegeEffect eff = this.collegeEffect.remove(collegeId);
		if (Objects.nonNull(eff) &&Objects.nonNull(members)) {
			for (String memberId : members) {
				Player targetPlayer = GlobalData.getInstance().getActivePlayer(memberId);
				if(Objects.nonNull(targetPlayer)){
					eff.syncCollegeEffect(memberId);
				}
			}
		}
		
		LogUtil.logCollegeDismiss(player, collegeId, memberCnt);
		HawkLog.logPrintln("createCollege entity create success, playerId:{}, playerName:{}, collegeId:{}, members:{}, memberCnt: {}", player.getId(), player.getName(),
				collegeEntity.getId(), members, memberCnt);
		// 行为日志
		BehaviorLogger.log4Service(player, Source.COLLEGE, Action.DISSMISS_COLLEGE, 
				Params.valueOf("collegeId", collegeId),
				Params.valueOf("coachId", player.getId()),
				Params.valueOf("coachName", player.getName()),
				Params.valueOf("memberCnt", memberCnt));
		return Status.SysError.SUCCESS_OK_VALUE;
	}

	/**
	 * 构建本学院学员列表
	 * @param player
	 * @return
	 */
	public List<CollegeMemberInfo> genCollegeMemberList(Player player) {
		List<CollegeMemberInfo> list = new ArrayList<>();
		CollegeMemberEntity self = player.getData().getCollegeMemberEntity();
		String collegeId = self.getCollegeId();
		if (HawkOSOperator.isEmptyString(collegeId) || !collegeMembers.containsKey(collegeId)) {
			return list;
		}
		CollegeEntity collegeEntity = collegeData.get(collegeId);
		String coachId = collegeEntity.getCoachId();
		Set<String> members = collegeMembers.get(collegeId);
		for (String memberId : members) {
			// 教官不列入学院列表
			if (memberId.equals(coachId)) {
				continue;
			}
			
			CollegeMemberInfo.Builder builder = genMemberInfo(memberId);
			if (builder == null) {
				continue;
			}
			List<RewardInfo> rewardInfoList = buildRewareInfoList(self, memberId);
			builder.addAllReward(rewardInfoList);
			list.add(builder.build());
		}
		return list;
	}
	
	
	public void checkCoahChange(Player player){
		if(!player.hasCollege()){
			return;
		}
		if(CrossService.getInstance().isCrossPlayer(player.getId())){
			return;
		}
		if(player.getCollegeAuth() == CollegeAuth.COACH_VALUE){
			return;
		}
		String cId = player.getCollegeId();
		CollegeEntity collegeEntity = this.collegeData.get(cId);
		if(Objects.isNull(collegeEntity)){
			return;
		}
		Player coach = GlobalData.getInstance().makesurePlayer(collegeEntity.getCoachId());
		if(Objects.isNull(coach)){
			HawkLog.logPrintln("checkCoahChange coach null, playerId: {},coachId: {}", player.getId(),collegeEntity.getCoachId());
			return;
		}
		if(coach.isActiveOnline()){
			HawkLog.logPrintln("checkCoahChange coach online, playerId: {},coachId: {}", player.getId(),collegeEntity.getCoachId());
			return;
		}
		long logoutTime = coach.getLogoutTime();
		long loginTime = coach.getLoginTime();
		long checkTime = Math.max(logoutTime, loginTime);
		if(checkTime <= 0){
			HawkLog.logPrintln("checkCoahChange logoutTime zero, playerId: {},coachId: {}", player.getId(),collegeEntity.getCoachId());
			return;
		}
		long curTime = HawkTime.getMillisecond();
		if(curTime - checkTime < CollegeConstCfg.getInstance().getChangeCoach() * HawkTime.DAY_MILLI_SECONDS){
			HawkLog.logPrintln("checkCoahChange logoutTime less, playerId: {},coachId: {},logoutTime:{}", player.getId(),collegeEntity.getCoachId(),checkTime);
			return;
		}
		long weekScore = player.getData().getCollegeMemberEntity()
				.getScoreData().getWeekScore();
		Set<String> members = collegeMembers.get(cId);
		for (String memberId : members) {
			if(memberId.equals(player.getId())){
				continue;
			}
			if(memberId.equals(collegeEntity.getCoachId())){
				continue;
			}
			Player memeber = GlobalData.getInstance().makesurePlayer(memberId);
			if(Objects.isNull(memeber)){
				continue;
			}
			long memberScore =  memeber.getData().getCollegeMemberEntity()
					.getScoreData().getWeekScore();
			if(weekScore < memberScore){
				HawkLog.logPrintln("checkCoahChange weekScore less, playerId: {},score: {},memberId:{},memberScore:{}", 
						player.getId(),weekScore,memberId,memberScore);
				return;
			}
		}
		int rlt = this.setCollegecoach(coach, player);
		if(rlt == 0){
			coach.rpcCall(MsgId.COLLEGE_AUTH_CHANGE,player,
					new CollegeCoahAuthChangeMsg(coach,player,0));
		}
	}
	
	
	
	/**
	 * 获取学院成员数量
	 * @param collegeId
	 * @return
	 */
	public int getCollegeMemberCnt(String collegeId) {
		if (!collegeMembers.containsKey(collegeId)) {
			return 0;
		}
		int totalCnt = collegeMembers.get(collegeId).size();
		return Math.max(totalCnt - 1, 0);
	}



	/**
	 * 构建学院成员信息
	 * @param memberId
	 * @return
	 */
	public CollegeMemberInfo.Builder genMemberInfo(String memberId) {
		Player player = GlobalData.getInstance().makesurePlayer(memberId);
		if (player == null) {
			return null;
		}
		CollegeMemberInfo.Builder memberInfo = CollegeMemberInfo.newBuilder();
		CollegeMemberEntity member = player.getData().getCollegeMemberEntity();
		if (player.hasCollege()) {
			memberInfo.setCollegeId(player.getCollegeId());
		}
		memberInfo.setId(player.getId());
		memberInfo.setName(player.getName());
		memberInfo.setLevel(player.getLevel());
		memberInfo.setCityLvl(player.getCityLevel());
		memberInfo.setPower(player.getPower());
		memberInfo.setAuth(CollegeAuth.valueOf(member.getAuth()));
		memberInfo.setIcon(player.getIcon());
		if (!HawkOSOperator.isEmptyString(player.getPfIcon())) {
			memberInfo.setPfIcon(player.getPfIcon());
		}
		memberInfo.setOnline(player.isActiveOnline());
		memberInfo.setOfflineTime(player.getLogoutTime());
		memberInfo.setOnlineTime(member.getOnlineTimeToday());
		memberInfo.setLastNotifyedTime(member.getLastNotifyedTime());
		memberInfo.setCommon(BuilderUtil.genPlayerCommonBuilder(player.getData()));
		memberInfo.setWeekScore((int) member.getScoreData().getWeekScore());
		return memberInfo;
	}
	
	/**
	 * 构建学院基础信息
	 * @param collegeId
	 * @return
	 */
	public CollegeBaseInfo.Builder genColleagBaseInfo(String collegeId) {
		CollegeEntity college = collegeData.get(collegeId);
		if (college == null) {
			return null;
		}
		CollegeBaseInfo.Builder builder = CollegeBaseInfo.newBuilder();
		Player coach = GlobalData.getInstance().makesurePlayer(college.getCoachId());
		if (coach == null) {
			return null;
		}
		builder.setId(collegeId);
		builder.setCoachId(coach.getId());
		builder.setCoachName(coach.getName());
		builder.setMemberCnt(getCollegeMemberCnt(collegeId));
		builder.setCommon(BuilderUtil.genPlayerCommonBuilder(coach));
		
		builder.setLevel(college.getLevel());
		builder.setExp(college.getExp());
		builder.setCollegeName(college.getCollegeName());
		builder.setJoinFree(college.getJoinFree());
		
		builder.setLoginCount(college.getStatisticsEntity().getLoginMemberCount());
		builder.setReNameCount(college.getReNameCount());
		return builder;
	}
	
	

	/**
	 * 同步学院信息
	 * @param player
	 */
	public void syncCollegeInfo(Player player) {
		if (!player.isCsPlayer()) {
			CollegeInfo.Builder builder = CollegeInfo.newBuilder();
			CollegeMemberEntity self = player.getData().getCollegeMemberEntity();
			if (player.hasCollege()) {
				String collegeId = self.getCollegeId();
				CollegeEntity college = collegeData.get(collegeId);

				CollegeBaseInfo.Builder collegeInfo = genColleagBaseInfo(collegeId);
				builder.setBaseInfo(collegeInfo);

				CollegeMemberInfo.Builder coachInfo = genMemberInfo(college.getCoachId());
				builder.setCoachInfo(coachInfo);

				builder.addAllMemberInfo(genCollegeMemberList(player));
			} else {
				CollegeMemberInfo.Builder selfInfo = genMemberInfo(player.getId());
				builder.addMemberInfo(selfInfo);
			}
			player.sendProtocol(HawkProtocol.valueOf(HP.code.COLLEGE_INFO_SYNC_S, builder));
		}
	}
	
	
	/**
	 * 同步学院信息
	 * @param player
	 */
	public void syncCollegeMemberData(Player player) {
		if (!player.isCsPlayer()) {
			CollegeMemberData.Builder builder = CollegeMemberData.newBuilder();
			if (player.hasCollege()) {
				builder.setScoreInfo(genScoreBuilder(player));
				builder.addAllShopInfo(genShopBuilder(player));
				builder.addAllGiftInfo(genGfitBuilder(player));
				builder.addAllMissionInfo(genMissionBuilder(player));
				builder.addAllOnlineEffectInfo(genOnlineEffectBuilder(player));
				builder.addAllLevelEffectInfo(genLevelEffectBuilder(player));
				builder.addAllOnlineRewards(genOnlienRewardsBuilder(player));
				player.sendProtocol(HawkProtocol.valueOf(HP.code2.COLLEGE_MEMBER_DATA_RESP_S_VALUE, builder));
			}
		}

	}
	
	
	public void syncCollegeBaseInfo(Player player){
		if (!player.isCsPlayer()) {
			if (player.hasCollege()) {
				CollegeBaseInfo.Builder builder = this.genColleagBaseInfo(player.getCollegeId());
				player.sendProtocol(HawkProtocol.valueOf(HP.code2.COLLEGE_BASE_INFO_RESP_S_VALUE, builder));
			}
		}
	}
	
	public void sysnCollegeExchangeInfo(Player player){
		if (!player.isCsPlayer()) {
			if (player.hasCollege()) {
				CollegeExchangeResp.Builder builder = CollegeExchangeResp.newBuilder();
				builder.addAllShopInfo(this.genShopBuilder(player));
				player.sendProtocol(HawkProtocol.valueOf(HP.code2.COLLEGE_EXCHANGE_S, builder));
			}
		}
	}
	
	
	public void sysnCollegeGiftInfo(Player player){
		if (!player.isCsPlayer()) {
			if (player.hasCollege()) {
				// 给客户端返回信息
				CollegeGiftBuyResp.Builder builder = CollegeGiftBuyResp.newBuilder();
				builder.addAllGiftInfo(this.genGfitBuilder(player));
				player.sendProtocol(HawkProtocol.valueOf(HP.code2.COLLEGE_GIFT_BUY_S, builder));
			}
		}
	}

	
	public void syncOnlineRewardInfo(Player player){
		if (!player.isCsPlayer()) {
			if (player.hasCollege()) {
				// 给客户端返回信息
				OnlineRewardInfoResp.Builder builder = OnlineRewardInfoResp.newBuilder();
				builder.addAllOnlineRewards(this.genOnlienRewardsBuilder(player));
				player.sendProtocol(HawkProtocol.valueOf(HP.code2.COLLEGE_ONLINE_REWARD_INFO_RESP_S, builder));
			}
		}
	}
	
	
	public void syncEffectInfo(Player player){
		if (!player.isCsPlayer()) {
			if (player.hasCollege()) {
				// 给客户端返回信息
				CollegeEffectUpdateResp.Builder builder = CollegeEffectUpdateResp.newBuilder();
				builder.addAllOnlineEffectInfo(genOnlineEffectBuilder(player));
				builder.addAllLevelEffectInfo(genLevelEffectBuilder(player));
				player.sendProtocol(HawkProtocol.valueOf(HP.code2.COLLEGE_EFFECT_RESP_S, builder));
			}
		}
		
	}
	
	

	/**
	 * 是否同一个学院的玩家
	 * @param playerIds
	 * @return
	 */
	public boolean isSameCollege(String... playerIds) {
		String collegeId = null;
		for (String playerId : playerIds) {
			Player player = GlobalData.getInstance().makesurePlayer(playerId);
			if (player == null) {
				return false;
			}
			String currCollegeId = player.getCollegeId();
			if (HawkOSOperator.isEmptyString(currCollegeId)) {
				currCollegeId = UUID.randomUUID().toString();
			}
	
			if (collegeId == null) {
				collegeId = currCollegeId;
			}
	
			if (!collegeId.equals(currCollegeId)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 构建可领奖列表信息
	 * @param self
	 * @param memberId
	 * @return
	 */
	private List<RewardInfo> buildRewareInfoList(CollegeMemberEntity self, String memberId) {
		List<RewardInfo> list = new ArrayList<>();
		Player memberPlayer = GlobalData.getInstance().makesurePlayer(memberId);
		if (memberPlayer == null) {
			return list;
		}
		CollegeMemberEntity member = memberPlayer.getData().getCollegeMemberEntity();
		long onlintTime = member.getOnlineTimeToday();
	
		Map<String, List<Integer>> rewardedMap = self.getOnlineTookMap();
		List<Integer> rewardedList = rewardedMap.get(memberId);
		if (rewardedList == null) {
			rewardedList = Collections.emptyList();
		}
		for (CollegeOnlineRewardCfg cfg : HawkConfigManager.getInstance().getConfigIterator(CollegeOnlineRewardCfg.class)) {
			int id = cfg.getId();
			long limit = cfg.getOnlineTime();
			RewardStatus statue = RewardStatus.NOT_REACH;
			if (onlintTime >= limit) {
				if (rewardedList.contains(id)) {
					statue = RewardStatus.TOOKEN;
				} else {
					statue = RewardStatus.NOT_REWARD;
				}
			}
			RewardInfo.Builder rewardInfo = RewardInfo.newBuilder();
			rewardInfo.setId(id);
			rewardInfo.setStatus(statue);
			rewardInfo.setValue((int) (Math.min(limit, onlintTime) / 1000));
			list.add(rewardInfo.build());
		}
		return list;
	}

	/**
	 * 申请加入学院
	 * @param player
	 * @param collegeId
	 * @return
	 */
	public int applyCollege(Player player, String collegeId) {
		CollegeEntity college = collegeData.get(collegeId);
		if (college == null) {
			return Status.Error.COLLEGE_NOT_EXIST_VALUE;
		}
		// 学院已满员
		if (getCollegeMemberCnt(collegeId) >= CollegeConstCfg.getInstance().getCollegeMemberMaxCnt()) {
			return Status.Error.COLLEGE_MEMBER_MAX_VALUE;
		}

		Map<String, String> applyMap = LocalRedis.getInstance().getApplyedColleges(player.getId());
		// 未申请过该学院,或者上次申请超过冷却时间的,重新添加申请 
		if (!applyMap.containsKey(collegeId) || HawkTime.getMillisecond() - Long.valueOf(applyMap.get(collegeId)) > CollegeConstCfg.getInstance().getApplyEffectTime()) {
			LocalRedis.getInstance().addCollegeApply(collegeId, player.getId());
		}

		// 给教官同步申请信息
		Player coach = GlobalData.getInstance().makesurePlayer(college.getCoachId());
		if (coach != null && coach.isActiveOnline()) {
			GetApplyListResp.Builder applyInfo = CollegeService.getInstance().getApplyList(collegeId);
			coach.sendProtocol(HawkProtocol.valueOf(HP.code.COLLEGE_APPLY_SYNC, applyInfo));
		}
		return Status.SysError.SUCCESS_OK_VALUE;
	}

	/**
	 * 获取学院申请学员列表
	 * @param collegeId
	 * @return
	 */
	public GetApplyListResp.Builder getApplyList(String collegeId) {
		GetApplyListResp.Builder builder = GetApplyListResp.newBuilder();
		Map<String, String> applyMap = LocalRedis.getInstance().getCollegeApplys(collegeId);
		List<String> needRemove = new ArrayList<>();
		long now = HawkTime.getMillisecond();
		int sizeLimit = CollegeConstCfg.getInstance().getApplyListSize();
		int count = 0;
		for (Entry<String, String> entry : applyMap.entrySet()) {
			String playerId = entry.getKey();
			long applyTime = Long.valueOf(entry.getValue());
			// 超过时长的不显示 
			if (now - applyTime > CollegeConstCfg.getInstance().getApplyEffectTime()) {
				needRemove.add(playerId);
				continue;
			}
			// 已有学院的成员不显示
			CollegeMemberInfo.Builder memberInfo = genMemberInfo(playerId);
			if (memberInfo.hasCollegeId()) {
				needRemove.add(playerId);
				continue;
			}
			builder.addMember(memberInfo);
			count++;
			if (count >= sizeLimit) {
				break;
			}
		}

		// 存在需要移除的申请信息
		if (needRemove.size() > 0) {
			LocalRedis.getInstance().delCollegeApplys(collegeId, needRemove.toArray(new String[needRemove.size()]));
		}
		return builder;
	}
	
	/**
	 * 获取可申请的教官的列表
	 * @param player
	 * @return
	 */
	public GetCollegeLeaderResp.Builder getCanApplyCoachList(Player player) {
		GetCollegeLeaderResp.Builder builder = GetCollegeLeaderResp.newBuilder();
		List<CollegeEntity> list = new ArrayList<>(collegeData.values());
		if (list.isEmpty()) {
			return builder;
		}
		Collections.shuffle(list);
		int sizeLimit = CollegeConstCfg.getInstance().getCoachListSize();
		int memberLimit = CollegeConstCfg.getInstance().getCollegeMemberMaxCnt();
		int cnt = 0;
		// 已经申请过的学院的列表
		Map<String, String> applyMap = LocalRedis.getInstance().getApplyedColleges(player.getId());
		for (CollegeEntity college : list) {
			String collegeId = college.getId();
			// 当前有生效中的申请
			if (applyMap.containsKey(collegeId) && HawkTime.getMillisecond() - Long.valueOf(applyMap.get(collegeId)) < CollegeConstCfg.getInstance().getApplyEffectTime()) {
				continue;
			}

			int memberCnt = getCollegeMemberCnt(collegeId);
			// 学院已满
			if (memberCnt >= memberLimit) {
				continue;
			}
			String coachId = college.getCoachId();
			CollegeInfo.Builder collegeInfo = CollegeInfo.newBuilder();
			CollegeMemberInfo.Builder coachInfo = genMemberInfo(coachId);
			CollegeBaseInfo.Builder baseInfo = genColleagBaseInfo(collegeId);
			if (coachInfo == null || baseInfo == null) {
				continue;
			}
			collegeInfo.setBaseInfo(baseInfo);
			collegeInfo.setCoachInfo(coachInfo);
			builder.addMember(collegeInfo);
			cnt++;
			if (cnt >= sizeLimit) {
				break;
			}
		}
		return builder;
	}
	
	/**
	 * 同意申请
	 * @param player
	 * @param applyerId
	 * @return
	 */
	public int onAgreeApply(Player player, String applyerId) {
		CollegeMemberEntity coach = player.getData().getCollegeMemberEntity();
		String collegeId = coach.getCollegeId();

		int memberCnt = getCollegeMemberCnt(collegeId);
		if (memberCnt >= CollegeConstCfg.getInstance().getCollegeMemberMaxCnt()) {
			return Status.Error.COLLEGE_MEMBER_MAX_VALUE;
		}

		// 对方没在申请列表中
		Map<String, String> applyMap = LocalRedis.getInstance().getCollegeApplys(collegeId);
		if (!applyMap.containsKey(applyerId)) {
			return Status.Error.COLLEGE_APPLY_NOT_EXIST_VALUE;
		}
		Player applyer = GlobalData.getInstance().makesurePlayer(applyerId);
		if (applyer == null) {
			return Status.SysError.ACCOUNT_NOT_EXIST_VALUE;
		}

		CollegeMemberEntity member = applyer.getData().getCollegeMemberEntity();
		if (!HawkOSOperator.isEmptyString(member.getCollegeId())) {
			return Status.Error.TARGET_ALREADY_IN_COLLEGE_VALUE;
		}
		CollegeEntity college = this.collegeData.get(collegeId);
		if(Objects.isNull(college)){
			return Status.Error.COLLEGE_NOT_EXIST_VALUE;
		}
		collegeMembers.get(collegeId).add(applyerId);
		HawkApp.getInstance().postMsg(applyer.getXid(), CollegeJoinMsg.valueOf(collegeId, player.getId(), player.getName(), false, false));

		LocalRedis.getInstance().delCollegeApplys(collegeId, applyerId);
		LocalRedis.getInstance().delApplyCollege(applyerId, collegeId);
		
		//同步做用号
		CollegeEffect eff = this.collegeEffect.get(collegeId);
		if (eff!= null) {
			eff.syncCollegeEffect(applyerId);
		}
		
		MailService.getInstance().sendMail(MailParames.newBuilder()
				.setPlayerId(applyerId)
				.setMailId(MailId.COLLEGE_APPLY_AGREE_JOIN)
				.addSubTitles(college.getCollegeName())
				.addSubTitles(college.getLevel())
				.addContents(college.getCollegeName())
				.build());
		return Status.SysError.SUCCESS_OK_VALUE;
	}

	/**
	 * 拒绝学院成员申请
	 * @param player
	 * @param applyerId
	 */
	public void onRefuseApply(Player player, String applyerId) {
		CollegeMemberEntity coach = player.getData().getCollegeMemberEntity();
		String collegeId = coach.getCollegeId();
		LocalRedis.getInstance().delCollegeApplys(collegeId, applyerId);
		LocalRedis.getInstance().delApplyCollege(applyerId, collegeId);
	}

	/**
	 * 同步所有学院成员的学院信息
	 * @param collegeId
	 */
	public void broadcastSyncCollegeInfo(String collegeId) {
		Set<String> memberIds = collegeMembers.get(collegeId);
		if (memberIds == null || memberIds.isEmpty()) {
			return;
		}
		for (String memberId : memberIds) {
			Player member = GlobalData.getInstance().makesurePlayer(memberId);
			if (member != null && member.isActiveOnline()) {
				syncCollegeInfo(member);
				syncCollegeMemberData(member);
			}
		}
	}
	
	/**
	 * 搜索教官
	 * @param player
	 * @param name
	 * @return
	 */
	public GetCollegeLeaderResp.Builder getSearchCoach(Player player, String name, boolean precise) {
		GetCollegeLeaderResp.Builder builder = GetCollegeLeaderResp.newBuilder();
		if (HawkOSOperator.isEmptyString(name)) {
			return builder;
		}
		List<String> coachIds = SearchService.getInstance().matchingCoachName(name);
		if (coachIds == null) {
			return builder;
		}
		for (String coachId : coachIds) {
			CollegeInfo.Builder collegeInfo = CollegeInfo.newBuilder();
			CollegeMemberInfo.Builder coachInfo = genMemberInfo(coachId);
			if (coachId != null && !player.getId().equals(coachId)) {
				CollegeBaseInfo.Builder baseInfo = genColleagBaseInfo(coachInfo.getCollegeId());
				if(baseInfo == null){
					continue;
				}
				if (precise && !name.equals(baseInfo.getCoachName())) {
					continue;
				}
				collegeInfo.setBaseInfo(baseInfo);
				collegeInfo.setCoachInfo(coachInfo);
				builder.addMember(collegeInfo);
			}
		}
		return builder;
	}
	
	/**
	 * 领取在线奖励
	 * @param player
	 * @param memberId
	 * @param rewardIdList
	 */
	public int getOnlineReward(Player player, String memberId, List<Integer> rewardIdList) {
		
		if(!isSameCollege(player.getId(), memberId)){
			return Status.Error.NOT_IN_SAME_COLLEGE_VALUE;
		}
		
		CollegeMemberEntity self = player.getData().getCollegeMemberEntity();
		Map<String, List<Integer>> rewardedMap = self.getOnlineTookMap();
		int cntLimit = CollegeConstCfg.getInstance().getOnlineRewardLimit();
		int cnt = 0;
		for (List<Integer> list : rewardedMap.values()) {
			cnt += list.size();
		}
		List<Integer> canGet = new ArrayList<>();
		Collections.sort(rewardIdList);
		Collections.reverse(rewardIdList);
		List<Integer> rewardedList = rewardedMap.get(memberId);
		if (rewardedList == null) {
			rewardedList = new ArrayList<>();
		}
		Player memberPlayer = GlobalData.getInstance().makesurePlayer(memberId);
		CollegeMemberEntity member = memberPlayer.getData().getCollegeMemberEntity();
		long onlineTime = member.getOnlineTimeToday();
		boolean overLimit = false;
		boolean notMatch = false;
		
		for (Integer id : rewardIdList) {
			if (rewardedList.contains(id)) {
				continue;
			}
			if (cnt < cntLimit) {
				CollegeOnlineRewardCfg cfg = HawkConfigManager.getInstance().getConfigByKey(CollegeOnlineRewardCfg.class, id);
				if (onlineTime < cfg.getOnlineTime()) {
					notMatch = true;
					continue;
				}
				canGet.add(id);
				cnt++;
			} else {
				overLimit = true;
				break;
			}
		}
		
		if (canGet.isEmpty()) {
			if(overLimit){
				return Status.Error.COLLEGE_ONLINE_REWARD_MAX_VALUE;
			}
			else if(notMatch){
				return Status.Error.COLLEGE_NO_ONLINE_REWARD_VALUE;
			}
		}
		rewardedList.addAll(canGet);
		Collections.sort(rewardedList);
		rewardedMap.put(memberId, rewardedList);
		self.notifyUpdate();
		AwardItems awardItems = AwardItems.valueOf();
		for(Integer cfgId : canGet){
			CollegeOnlineRewardCfg cfg = HawkConfigManager.getInstance().getConfigByKey(CollegeOnlineRewardCfg.class, cfgId);
			if(cfg == null){
				continue;
			}
			awardItems.addItemInfos(cfg.getRewardItemInfo());
		}
		awardItems.rewardTakeAffectAndPush(player, Action.COLLEGE_ONLINE_REWARD, true);
		syncCollegeInfo(player);
		LogUtil.logCollegeOnlineRewarded(player, self.getCollegeId(), memberId, 1);
		// 行为日志
		BehaviorLogger.log4Service(player, Source.COLLEGE, Action.COLLEGE_ONLINE_REWARD, 
				Params.valueOf("collegeId", self.getCollegeId()),
				Params.valueOf("targetPlayerId", memberId),
				Params.valueOf("targetPlayerName", memberPlayer.getName()),
				Params.valueOf("ids", canGet));
		return Status.SysError.SUCCESS_OK_VALUE;
	}

	public int quitCollege(Player player) {
		String collegeId = player.getCollegeId();
		CollegeEntity college = collegeData.get(collegeId);
		String coachId = college.getCoachId();
		Player coach = GlobalData.getInstance().makesurePlayer(coachId);
		Set<String> memberIds = collegeMembers.get(collegeId);
		memberIds.remove(player.getId());
		HawkApp.getInstance().postMsg(player.getXid(), CollegeQuitMsg.valueOf(collegeId, coachId, coach.getName(), false, false));
		//同步做用号
		CollegeEffect eff = this.collegeEffect.get(collegeId);
		if (Objects.nonNull(eff)) {
			eff.syncCollegeEffect(player.getId());
		}
		return Status.SysError.SUCCESS_OK_VALUE;
	}
	
	/**
	 * 踢出成员
	 * @param player
	 * @param targetId
	 * @return
	 */
	public int kickMember(Player player, List<String> targetIdList) {
		int kickCnt = 0;
		int status = Status.SysError.SUCCESS_OK_VALUE;
		long curTime = HawkTime.getMillisecond();
		CollegeEntity college = collegeData.get(player.getCollegeId());
		if(Objects.isNull(college)){
			return Status.Error.COLLEGE_NOT_EXIST_VALUE;
		}
		for (String targetId : targetIdList) {
			// 不是同一学院
			if (!isSameCollege(player.getId(), targetId)) {
				status = Status.Error.NOT_IN_SAME_COLLEGE_VALUE;
				continue;
			}
			Player targetPlayer = GlobalData.getInstance().makesurePlayer(targetId);
			String collegeId = player.getCollegeId();
			Set<String> memberIds = collegeMembers.get(collegeId);
			memberIds.remove(targetId);
			college.addKickoutTime(targetId, curTime);
			HawkApp.getInstance().postMsg(targetPlayer.getXid(), CollegeQuitMsg.valueOf(collegeId, player.getId(), player.getName(), true, false));
			HawkLog.logPrintln("kickMember member success, coachId: {}, cocahName: {}, collegeId: {}, targetId: {}", player.getId(), player.getName(), collegeId, targetId);
			kickCnt++;
			//同步做用号
			CollegeEffect eff = this.collegeEffect.get(collegeId);
			if (Objects.nonNull(eff)) {
				eff.syncCollegeEffect(targetId);
			}
		}
		if (kickCnt == 0) {
			return status;
		}
		return Status.SysError.SUCCESS_OK_VALUE;
	}
	
	/**
	 * 邀请好友上线
	 * @param player
	 * @param memberId
	 * @return
	 */
	public int inviteLogin(Player player, String memberId, boolean isPlatFriend) {
		if (!isSameCollege(player.getId(), memberId)) {
			return Status.Error.NOT_IN_SAME_COLLEGE_VALUE;
		}
		Player memberPlayer = GlobalData.getInstance().makesurePlayer(memberId);
		if (memberPlayer == null) {
			return Status.SysError.ACCOUNT_NOT_EXIST_VALUE;
		}
		// 玩家在线
		if (memberPlayer.isActiveOnline()) {
			return Status.Error.MEMBER_IS_ONLINE_VALUE;
		}

		long now = HawkTime.getMillisecond();
		// 离线时间不满足
		if (now - memberPlayer.getLogoutTime() < CollegeConstCfg.getInstance().getRemindTime()) {
			return Status.Error.MEMBER_OFFLINE_NOT_ENOUGH_VALUE;
		}

		CollegeMemberEntity member = memberPlayer.getData().getCollegeMemberEntity();
		if (!isPlatFriend) {
			// 邀请上线冷却中
			if (now - member.getLastNotifyedTime() < CollegeConstCfg.getInstance().getRemindTimeLimitCD()) {
				return Status.Error.INVITE_LOGIN_CD_VALUE;
			}
			// 推送提醒
			PushService.getInstance().pushMsg(memberId, PushMsgType.COLLEGE_INVITE_LOGIN_VALUE, player.getName());
		}
		member.setLastNotifyedTime(now);
		syncCollegeInfo(memberPlayer);
		return Status.SysError.SUCCESS_OK_VALUE;
	}
	
	
	/**
	 * 同意邀请加入学院
	 * @param player
	 * @param collegeId
	 * @return
	 */
	public int acceptInvite(Player player, String collegeId,String applyId) {
		int memberCnt = getCollegeMemberCnt(collegeId);
		if (memberCnt >= CollegeConstCfg.getInstance().getCollegeMemberMaxCnt()) {
			return Status.Error.COLLEGE_MEMBER_MAX_VALUE;
		}
	
		CollegeMemberEntity member = player.getData().getCollegeMemberEntity();
		if (!HawkOSOperator.isEmptyString(member.getCollegeId())) {
			return Status.Error.TARGET_ALREADY_IN_COLLEGE_VALUE;
		}
		CollegeEntity college = collegeData.get(collegeId);
		if(Objects.isNull(college)){
			return Status.Error.COLLEGE_NOT_EXIST_VALUE;
		}
		String coachId = college.getCoachId();
		
		if (CrossService.getInstance().isCrossPlayer(coachId)) {
			return Status.CrossServerError.CROSS_OPERATION_NOT_SUPPOERT_VALUE;
		}
		if(!HawkOSOperator.isEmptyString(applyId) &&
				this.joinFromGuildApplyInCd(college,player.getId())){
			//如果是从联盟邀请进入的,并且玩家是被踢出的,查看是否在加入冷却时间内
			return Status.Error.COLLEGE_KICK_GUILD_APPLY_JOIN_CD_VALUE;
		}
		
		if(!HawkOSOperator.isEmptyString(applyId) &&
				this.guildApplyOutTime(college, applyId)){
			//如果是从联盟邀请进入的,查看邀请是否失效
			return Status.Error.COLLEGE_GUILD_APPLY_OUT_TIME_VALUE;
		}
		
		Player coach = GlobalData.getInstance().makesurePlayer(coachId);
		collegeMembers.get(collegeId).add(player.getId());
		HawkApp.getInstance().postMsg(player.getXid(), CollegeJoinMsg.valueOf(collegeId, coachId, coach.getName(), false, true));
		
		LocalRedis.getInstance().delCollegeApplys(collegeId, player.getId());
		LocalRedis.getInstance().delApplyCollege(player.getId(), collegeId);
			
		return Status.SysError.SUCCESS_OK_VALUE;
	}
	
	/**
	 * 判定玩家在线时长是否触发可领取奖励
	 * @param befTime
	 * @param aftTime
	 * @return
	 */
	public boolean triggerOnlineReward(long befTime, long aftTime) {
		for (CollegeOnlineRewardCfg cfg : HawkConfigManager.getInstance().getConfigIterator(CollegeOnlineRewardCfg.class)) {
			long needTime = cfg.getOnlineTime();
			if (befTime < needTime && aftTime >= needTime) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 检测玩家的学院状态,如果学院信息不存在,则将玩家学员信息置为未加入状态
	 * @param player
	 */
	public void checkData(Player player) {
		try {
			if (!player.hasCollege()) {
				return;
			}
			CollegeMemberEntity self = player.getData().getCollegeMemberEntity();
			String collegeId = self.getCollegeId();
			CollegeEntity college = collegeData.get(collegeId);
			if (college != null) {
				CollegeService.getInstance().updateExchangeShopData(self, HawkApp.getInstance().getCurrentTime());
				CollegeService.getInstance().updateGiftShopData(self, HawkApp.getInstance().getCurrentTime());
				//更新一下学院的外显信息
				String show = WorldPointService.getInstance().getCollegeNameShow(player.getId());
				if(!college.getCollegeName().equals(show)){
					WorldPointService.getInstance().updateCollegeNameShow(player.getId(), college.getCollegeName());
				}
				return;
			}
			
			self.quit(); // 学院不存在,则清理玩家的学院成员信息
			HawkLog.logPrintln("college service checkInitMemberInfo, collegeId: {}, playerId: {}, playerName: {}", collegeId, player.getId(), player.getName());
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	
	
	/**
	 * 获取可申请的教官的列表
	 * @param player
	 * @return
	 */
	public CollegeRecommendListResp.Builder getCollegeRecommendListBuilder(Player player,int page) {
		CollegeRecommendListResp.Builder builder = CollegeRecommendListResp.newBuilder();
		List<String> reList = this.getCollegeRecommendList(player);
		int pageNum = 20;
		int startIndex = (page -1) * pageNum;
		for (int i = startIndex; i < startIndex + pageNum; i++) {
			if (reList.size() <= i) {
				break;
			}
			String cId = reList.get(i);
			CollegeBaseInfo.Builder cbuilder = this.genColleagBaseInfo(cId);
			if(Objects.nonNull(cbuilder)){
				builder.addColleges(cbuilder);
			}
		}
		return builder;
	}
	
	
	
	/**
	 * 获取可申请的教官的列表
	 * @param player
	 * @return
	 */
	public List<String> getCollegeRecommendList(Player player) {
		List<HawkTuple4<String, Integer, Integer,Integer>> list = new ArrayList<>();
		List<String> rlts = new ArrayList<>();
		for(CollegeEntity entity : this.collegeData.values()){
			int memberCount = this.getCollegeMemberCnt(entity.getId());
			String collegeId = entity.getId();
			int memberLimit = CollegeConstCfg.getInstance().getCollegeMemberMaxCnt();
			if(memberCount >= memberLimit){
				continue;
			}
			int lastCount = memberLimit - memberCount;
			int joinFree = entity.getJoinFree();
			int weekSocre = entity.getStatisticsEntity().getWeekScore();
			list.add(HawkTuples.tuple(collegeId, lastCount, joinFree,weekSocre));
		}
		Collections.sort(list, new Comparator<HawkTuple4<String, Integer, Integer,Integer>>() {
			@Override
			public int compare(HawkTuple4<String, Integer, Integer, Integer> o1,
					HawkTuple4<String, Integer, Integer, Integer> o2) {
				if(o1.fourth != o2.fourth){
					return o2.fourth - o1.fourth;
				}
				if(o1.second != o2.second){
					return o1.second - o2.second;
				}
				if(o1.third != o2.third){
					return o2.third - o1.third;
				}
				return o1.first.compareTo(o2.first);
			}
		});
		list.forEach(t->rlts.add(t.first));
		return rlts;
	}
	
	
	/**
	 * 设置自由加入开关
	 * @param player
	 * @param collegeId
	 * @param type
	 */
	public void setCollegeJoinFree(Player player,int type){
		CollegeMemberEntity entity = player.getData().getCollegeMemberEntity();
		String collegeId = entity.getCollegeId();
		CollegeEntity college = collegeData.get(collegeId);
		if(Objects.isNull(college)){
			return;
		}
		String coachId = college.getCoachId();
		if(!player.getId().equals(coachId)){
			HawkLog.logPrintln("setCollegeJoinFree fail not coach, playerId:{}, collegeId:{},coachId:{}", 
					player.getId(), collegeId,coachId);
			return;
		}
		if(type == 0){
			college.setJoinFree(0);
		}else{
			college.setJoinFree(1);
		}
		HawkLog.logPrintln("setCollegeJoinFree sucess, playerId:{}, collegeId:{},free:{}", player.getId(), collegeId,type);
		
	}
	
	/**
	 * 转入教官
	 * @param player
	 * @param targetId
	 */
	public int setCollegecoach(Player player,Player targetPlayer){
		if(Objects.isNull(targetPlayer)){
			return Status.SysError.DATA_ERROR_VALUE;
		}
		if(Objects.isNull(player)){
			return Status.SysError.DATA_ERROR_VALUE;
		}
		boolean targetCross = CrossService.getInstance().isCrossPlayer(player.getId());
		if(targetCross){
			HawkLog.logPrintln("setCollegecoach fail player in cross, playerId:{}, collegeId:{},targetId:{}", 
					player.getId(), player.getCollegeId(),targetPlayer.getId());
			return Status.CrossServerError.CROSS_PROTOCOL_SHIELD_VALUE;
		}
		targetCross = CrossService.getInstance().isCrossPlayer(targetPlayer.getId());
		if(targetCross){
			HawkLog.logPrintln("setCollegecoach fail target in cross, playerId:{}, collegeId:{},targetId:{}", 
					player.getId(), player.getCollegeId(),targetPlayer.getId());
			return Status.CrossServerError.CROSS_PROTOCOL_SHIELD_VALUE;
		}
		CollegeMemberEntity entity = player.getData().getCollegeMemberEntity();
		String collegeId = entity.getCollegeId();
		CollegeEntity college = collegeData.get(collegeId);
		String coachId = college.getCoachId();
		if(!player.getId().equals(coachId)){
			HawkLog.logPrintln("setCollegecoach fail not coach, playerId:{}, collegeId:{},targetId:{}", 
					player.getId(), player.getCollegeId(),targetPlayer.getId());
			return Status.Error.COLLEGE_NOT_COACH_VALUE;
		}
		boolean same = this.isSameCollege(player.getId(),targetPlayer.getId());
		if(!same){
			HawkLog.logPrintln("setCollegecoach fail not same college, playerId:{}, collegeId:{},targetId:{},targetCoach:{}", 
					player.getId(), player.getCollegeId(),targetPlayer.getId(),targetPlayer.getCollegeId());
			return Status.Error.COLLEGE_NOT_COACH_VALUE;
		}
		CollegeMemberEntity targetEntity = targetPlayer.getData().getCollegeMemberEntity();
		if(!targetEntity.getCollegeId().equals(collegeId)){
			HawkLog.logPrintln("setCollegecoach fail not same college2, playerId:{}, collegeId:{},targetId:{},targetCoach:{}", 
					player.getId(), player.getCollegeId(),targetPlayer.getId(),targetPlayer.getCollegeId());
			return Status.SysError.DATA_ERROR_VALUE;
		}
		college.setCoachId(targetPlayer.getId());
		HawkLog.logPrintln("setCollegecoach sucess, playerId:{}, collegeId:{},targetId:{}", 
				player.getId(), player.getCollegeId(),targetPlayer.getId());
		
		List<String> mlist = this.getCollegeAllMember(collegeId);
		for(String mid : mlist){
			MailService.getInstance().sendMail(MailParames.newBuilder()
					.setPlayerId(mid)
					.setMailId(MailId.COLLEGE_COACH_CHANGE)
					.addSubTitles(college.getCollegeName())
					.addSubTitles(college.getLevel())
					.addContents(player.getName())
					.addContents(targetPlayer.getName())
					.build());
		}
		
		return Status.SysError.SUCCESS_OK_VALUE;
	}
	
	/**
	 * 学院是否开启自由加入
	 * @param collegeId
	 * @return
	 */
	public boolean joinCollegeFree(String collegeId){
		CollegeEntity collegeEntity = collegeData.get(collegeId);
		if(Objects.isNull(collegeEntity)){
			return false;
		}
		return collegeEntity.getJoinFree() > 0;
	}
	
	
	/**
	 * 快速加入
	 * @param player
	 * @param protocolType
	 */
	public int joinCollegeFast(Player player,String targetId,int protocolType){
		CollegeMemberEntity entity = player.getData().getCollegeMemberEntity();
		String memberCollegeId = entity.getCollegeId();
		if (!HawkOSOperator.isEmptyString(memberCollegeId)) {
			return Status.Error.ALREADY_IN_COLLEGE_VALUE;
		}
		
		if(HawkOSOperator.isEmptyString(targetId)){
			return Status.Error.COLLEGE_NOT_EXIST_VALUE;
		}
		CollegeEntity collegeEntity = this.collegeData.get(targetId);
		if(Objects.isNull(collegeEntity)){
			return Status.Error.COLLEGE_NOT_EXIST_VALUE;
		}
		int memberLimit = CollegeConstCfg.getInstance().getCollegeMemberMaxCnt();
		int memberCnt = getCollegeMemberCnt(targetId);
		if(memberCnt >= memberLimit){
			return Status.Error.COLLEGE_MEMBER_MAX_VALUE;
		}
		collegeMembers.get(targetId).add(player.getId());
		HawkApp.getInstance().postMsg(player.getXid(), CollegeJoinMsg.valueOf(targetId, player.getId(), player.getName(), false, false));
		LocalRedis.getInstance().delCollegeApplys(targetId, player.getId());
		LocalRedis.getInstance().delApplyCollege(player.getId(), targetId);
		//同步做用号
		CollegeEffect eff = this.collegeEffect.get(targetId);
		if (eff!= null) {
			eff.syncCollegeEffect(player.getId());
		}
		return Status.SysError.SUCCESS_OK_VALUE;
	}
	
	
	/**
	 * 快速加入
	 * @param player
	 * @param protocolType
	 */
	public int joinCollegeFast(Player player,int protocolType){
		CollegeMemberEntity entity = player.getData().getCollegeMemberEntity();
		String memberCollegeId = entity.getCollegeId();
		if (!HawkOSOperator.isEmptyString(memberCollegeId)) {
			return Status.Error.ALREADY_IN_COLLEGE_VALUE;
		}
		List<String> rlt = this.getCollegeRecommendList(player);
		String targetId = "";
		for(String collegeId : rlt){
			CollegeEntity collegeEntity = this.collegeData.get(collegeId);
			if(Objects.isNull(collegeEntity)){
				continue;
			}
			int memberLimit = CollegeConstCfg.getInstance().getCollegeMemberMaxCnt();
			int memberCnt = getCollegeMemberCnt(collegeId);
			if(memberCnt >= memberLimit){
				continue;
			}
			//自由加入没有打开
			if(collegeEntity.getJoinFree() <= 0){
				continue;
			}
			targetId = collegeEntity.getId();
			break;
		}
		if(HawkOSOperator.isEmptyString(targetId)){
			return Status.Error.COLLEGE_FAST_JOIN_NO_FIND_VALUE;
		}
		collegeMembers.get(targetId).add(player.getId());
		HawkApp.getInstance().postMsg(player.getXid(), CollegeJoinMsg.valueOf(targetId, player.getId(), player.getName(), false, false));
		LocalRedis.getInstance().delCollegeApplys(targetId, player.getId());
		LocalRedis.getInstance().delApplyCollege(player.getId(), targetId);
		//同步做用号
		CollegeEffect eff = this.collegeEffect.get(targetId);
		if (eff!= null) {
			eff.syncCollegeEffect(player.getId());
		}
		return Status.SysError.SUCCESS_OK_VALUE;
	}
	
	
	
	
	public int onchangeName(Player player,String name){
		CollegeEntity college = this.collegeData.get(player.getCollegeId());
		if(Objects.isNull(college)){
			return Status.Error.COLLEGE_NOT_EXIST_VALUE;
		}
		if(!college.getCoachId().equals(player.getId())){
			return Status.Error.COLLEGE_NOT_COACH_VALUE;
		}
		String nameBef = college.getCollegeName();
		college.setCollegeName(name);
		//累计重命名次数
		int reNameCount = college.getReNameCount();
		college.setReNameCount(reNameCount + 1);
		HawkLog.logPrintln("onchangeName sucess, playerId:{}, collegeId:{},name1:{},name2:{},recount:{}", 
				player.getId(), player.getCollegeId(),nameBef,name,reNameCount);
		//广播
		return Status.SysError.SUCCESS_OK_VALUE;
	}
	
	
	
	
	public ItemInfo getReNameCost(String collegeId){
		CollegeEntity college = this.collegeData.get(collegeId);
		if(Objects.isNull(college)){
			return null;
		}
		int count = college.getReNameCount() + 1;
		return CollegeConstCfg.getInstance().getRenameExpendItem(count);
	}
	
	
	public CollegeSearchResp.Builder searchCollegeByName(Player player,String name,int protocolType){
		CollegeSearchResp.Builder builder = CollegeSearchResp.newBuilder();
		for(CollegeEntity college : this.collegeData.values()){
			if(college.getCollegeName().equals(name)){
				builder.addColleges(genColleagBaseInfo(college.getId()));
				break;
			}
		}
		return builder;
	}
	
	public int getCollegeLevel(String collegeId){
		CollegeEntity entity = this.collegeData.get(collegeId);
		if(Objects.isNull(entity)){
			return 0;
		}
		return entity.getLevel();
	}
	
	
	public String getCollegeName(String collegeId){
		CollegeEntity entity = this.collegeData.get(collegeId);
		if(Objects.isNull(entity)){
			return null;
		}
		return entity.getCollegeName();
	}
	
	/**
	 * 检测联盟名字合法性
	 * 
	 * @param name 联盟名字
	 * @return
	 */
	public int checkCollegeName(String name) {
		if(HawkOSOperator.isEmptyString(name)){
			return Status.NameError.NAME_BLANK_ERROR_VALUE;
		}
		int nameLength = GameUtil.getStringLength(name);
		if (nameLength > CollegeConstCfg.getInstance().getNameLenLimitMax() || 
				nameLength < CollegeConstCfg.getInstance().getNameLenLimitMin()) {
			return Status.Error.COLLEGE_NANME_LEN_ERR_VALUE;
		}

		int regexType = GsConst.RegexType.ALLLETTER + GsConst.RegexType.NUM + GsConst.RegexType.CHINESELETTER;
		if (!GameUtil.stringOnlyContain(name, regexType, "-_")) {
			return Status.NameError.CONTAIN_ILLEGAL_CHART_VALUE;
		}
		for(CollegeEntity college : this.collegeData.values()){
			if(name.equals(college.getCollegeName())){
				return Status.NameError.ALREADY_EXISTS_VALUE;
			}
		}
		return Status.SysError.SUCCESS_OK_VALUE;
	}
	
	
	public void addVit(String collegeId,int vit){
		if(vit <= 0){
			return;
		}
		CollegeEntity entity = this.collegeData.get(collegeId);
		if(Objects.isNull(entity)){
			return;
		}
		double vitTotal = entity.getVitality() + vit * GsConst.EFF_PER * CollegeConstCfg.getInstance().getStrengthBack();
		vitTotal = Math.min(vitTotal, CollegeConstCfg.getInstance().getMaxStrength());
		vitTotal = Math.max(0, vitTotal);
		entity.setVitality(vitTotal);
	}
	
	/**
	 * 统计周积分
	 * @param collegeId
	 * @param score
	 */
	public void addWeekScore(String collegeId,int score){
		CollegeEntity entity = this.collegeData.get(collegeId);
		if(Objects.isNull(entity)){
			return;
		}
		entity.getStatisticsEntity().addWeekScore(score);
	}
	
	
	/**
	 * 添加经验
	 * @param collegeId
	 * @param exp
	 */
	public void addCollegeExp(String collegeId,int exp){
		CollegeEntity entity = this.collegeData.get(collegeId);
		if(Objects.isNull(entity)){
			return;
		}
		
		int expBef = entity.getExpTotal();
		int levelBef = entity.getLevel();
		if(expBef >= CollegeLevelCfg.getExpMax()){
			return;
		}
		int totalExp = expBef + exp;
		if(totalExp > CollegeLevelCfg.getExpMax()){
			totalExp = CollegeLevelCfg.getExpMax();
		}
		HawkTuple2<Integer, Integer> tuple = this.calCollegeLevel(totalExp);
		entity.setExpTotal(totalExp);
		entity.setLevel(tuple.first);
		entity.setExp(tuple.second);
		if(levelBef != entity.getLevel()){
			this.onCollegeLevelUp(entity);
		}
	}
	
	public void onCollegeLevelUp(CollegeEntity enity){
		//升级
		Set<String> members = this.collegeMembers.get(enity.getId());
		if(Objects.isNull(members)){
			return;
		}
		for(String member : members){
			Player player = GlobalData.getInstance().getActivePlayer(member);
			if (player == null) {
				continue;
			}
			this.syncCollegeBaseInfo(player);
		}
	}
	
	/**
	 * 计算等级经验
	 * @param exp
	 * @return
	 */
	public HawkTuple2<Integer, Integer> calCollegeLevel(int exp){
		//初始等级为1
		int level = 1;
		int levelExp = exp;
		if(levelExp > CollegeLevelCfg.getExpMax()){
			levelExp = CollegeLevelCfg.getExpMax();
		}
		for(int i=1;i<= CollegeLevelCfg.getLevelMax();i++){
			CollegeLevelCfg levelCfg = HawkConfigManager.getInstance()
					.getConfigByKey(CollegeLevelCfg.class,i);
			int expGap = levelExp - levelCfg.getCollegeExp();
			if(expGap >= 0 && level < CollegeLevelCfg.getLevelMax()){
				level =i+1;
				levelExp -= levelCfg.getCollegeExp();
			}
		}
		return HawkTuples.tuple(level, levelExp);
	}
	
	
	public int getCollegeEff(String playerId,EffType type){
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		if(Objects.isNull(player)){
			return 0;
		}
		CollegeMemberEntity data = player.getData().getCollegeMemberEntity();
		if(Objects.isNull(data)){
			return 0;
		}
		String collegeId = data.getCollegeId();
		if(HawkOSOperator.isEmptyString(collegeId)){
			return 0;
		}
		Set<String> members = this.collegeMembers.get(collegeId);
		if(Objects.isNull(members)){
			return 0;
		}
		if(!members.contains(playerId)){
			return 0;
		}
		CollegeEntity centity = this.collegeData.get(collegeId);
		if(Objects.isNull(centity)){
			return 0;
		}
		CollegeEffect ceffet = this.collegeEffect.get(collegeId);
		if(Objects.isNull(ceffet)){
			return 0;
		}
		return ceffet.getEffValue(type);
	}
	
	
	public CollegeScore.Builder genScoreBuilder(Player player){
		CollegeScore.Builder builder = CollegeScore.newBuilder();
		CollegeMemberEntity data = player.getData().getCollegeMemberEntity();
		if(Objects.isNull(data)){
			return builder;
		}
		builder.setScore((int)data.getScoreData().getScore());
		builder.setWeekScore((int)data.getScoreData().getWeekScore());
		return builder;
	}
	
	public List<CollegeShopItem> genShopBuilder(Player player){
		List<CollegeShopItem> list = new ArrayList<>();
		CollegeMemberEntity entity = player.getData().getCollegeMemberEntity();
		if(!Objects.isNull(entity)){
			entity.getShopDataMap().values().stream().forEach(shopData -> list.add(shopData.toBuilder().build()));
		}
		return list;
	}
	
	public List<CollegeGiftItem> genGfitBuilder(Player player){
		List<CollegeGiftItem> list = new ArrayList<>();
		CollegeMemberEntity entity = player.getData().getCollegeMemberEntity();
		if(!Objects.isNull(entity)){
			Map<Integer, Integer> gmap = entity.getGiftData().getGiftMap();
			for(Map.Entry<Integer, Integer> entry : gmap.entrySet()){
				CollegePurchaseCfg cfg =  HawkConfigManager.getInstance().getConfigByKey(CollegePurchaseCfg.class, entry.getKey());
				if(Objects.isNull(cfg)){
					continue;
				}
				//终身不刷新并且买完了，就不给客户端显示了
				if(cfg.getRefreshType() == GiftRefreshType.NEVER 
						&& entry.getValue() >=cfg.getTimes()){
					continue;
				}
				CollegeGiftItem.Builder cbuilder = CollegeGiftItem.newBuilder();
				cbuilder.setGiftId(entry.getKey());
				cbuilder.setBuyCount(entry.getValue());
				list.add(cbuilder.build());
			}
		}
		return list;
	}
	
	public List<CollegeMission> genMissionBuilder(Player player){
		List<CollegeMission> list = new ArrayList<>();
		CollegeMemberEntity data = player.getData().getCollegeMemberEntity();
		if(Objects.isNull(data)){
			return list;
		}
		List<CollegeMissionEntityItem> missions = data.getMissionList();
		for(CollegeMissionEntityItem mission : missions){
			CollegeMission.Builder builder = CollegeMission.newBuilder();
			builder.setMissionId(mission.getCfgId());
			builder.setNum((int)mission.getValue());
			builder.setState(mission.getState());
			list.add(builder.build());
		}
		return list;
	}
	
	public List<PBCollegeEffect> genLevelEffectBuilder(Player player){
		List<PBCollegeEffect> list = new ArrayList<>();
		CollegeMemberEntity self = player.getData().getCollegeMemberEntity();
		String collegeId = self.getCollegeId();
		if (HawkOSOperator.isEmptyString(collegeId) || 
				!collegeMembers.containsKey(collegeId)) {
			return list;
		}
		CollegeEffect collegeEffect = this.collegeEffect.get(collegeId);
		if(Objects.isNull(collegeEffect)){
			return list;
		}
		ImmutableMap<EffType, Integer> emap = collegeEffect.getLevelEffVal();
		for(Map.Entry<EffType, Integer> en : emap.entrySet()){
			PBCollegeEffect.Builder eb = PBCollegeEffect.newBuilder();
			eb.setEffectId(en.getKey().getNumber());
			eb.setValue(en.getValue());
			list.add(eb.build());
		}
		return list;
	}
	
	public List<PBCollegeEffect> genOnlineEffectBuilder(Player player){
		List<PBCollegeEffect> list = new ArrayList<>();
		CollegeMemberEntity self = player.getData().getCollegeMemberEntity();
		String collegeId = self.getCollegeId();
		if (HawkOSOperator.isEmptyString(collegeId) || 
				!collegeMembers.containsKey(collegeId)) {
			return list;
		}
		CollegeEffect collegeEffect = this.collegeEffect.get(collegeId);
		if(Objects.isNull(collegeEffect)){
			return list;
		}
		ImmutableMap<EffType, Integer> emap = collegeEffect.getOnlineCountEffVal();
		for(Map.Entry<EffType, Integer> en : emap.entrySet()){
			PBCollegeEffect.Builder eb = PBCollegeEffect.newBuilder();
			eb.setEffectId(en.getKey().getNumber());
			eb.setValue(en.getValue());
			list.add(eb.build());
		}
		return list;
	}
	
	public CollegeVitality.Builder genVitBuilder(Player player){
		CollegeVitality.Builder builder = CollegeVitality.newBuilder();
		builder.setVitality(0);
		CollegeMemberEntity self = player.getData().getCollegeMemberEntity();
		String collegeId = self.getCollegeId();
		if (HawkOSOperator.isEmptyString(collegeId) || 
				!collegeMembers.containsKey(collegeId)) {
			return builder;
		}
		CollegeEntity collegeEntity = collegeData.get(collegeId);
		builder.setVitality(collegeEntity.getCanVitalitySendValue());
		Set<String> members = collegeMembers.get(collegeId);
		Map<String,Integer> vitMap = RedisProxy.getInstance().getCollegeMemberVitSendToday(members);
		for (String memberId : members) {
			Player member = GlobalData.getInstance().makesurePlayer(memberId);
			if (member == null) {
				continue;
			}
			int sendVit = vitMap.getOrDefault(memberId, 0);
			CollegeVitalityItem.Builder ibuilder = CollegeVitalityItem.newBuilder();
			ibuilder.setMember(memberId);
			ibuilder.setSendCount(sendVit);
			builder.addVitalityInfo(ibuilder);
		}
		return builder;
	}
	
	
	
	
	
	/**
	 * 构建可领奖列表信息
	 * @param self
	 * @param memberId
	 * @return
	 */
	private List<OnlineRewardInfo> genOnlienRewardsBuilder(Player player) {
		List<OnlineRewardInfo> rltList = new ArrayList<>();
		CollegeMemberEntity data = player.getData().getCollegeMemberEntity();
		if(Objects.isNull(data)){
			return rltList;
		}
		String collegeId = data.getCollegeId();
		if (HawkOSOperator.isEmptyString(collegeId) || 
				!collegeMembers.containsKey(collegeId)) {
			return rltList;
		}
		CollegeEntity college = this.collegeData.get(collegeId);
		if (Objects.isNull(college)) {
			return rltList;
		}
		Map<String, List<Integer>> rewardedMap = data.getOnlineTookMap();
		
		Map<Integer,Set<String>> rewardIdMap = new HashMap<>();
		for(Map.Entry<String,List<Integer>> entry : rewardedMap.entrySet()){
			String mid = entry.getKey();
			List<Integer> rlist = entry.getValue();
			for(int rid : rlist){
				Set<String> rset = rewardIdMap.get(rid);
				if(Objects.isNull(rset)){
					rset = new HashSet<>();
					rewardIdMap.put(rid, rset);
				}
				rset.add(mid);
			}
		}
		
		Set<String> members = this.collegeMembers.get(collegeId);
		Map<String,Long> memberOnlineTime = new HashMap<String,Long>();
		for(String memberId : members){
			if(memberId.equals(college.getCoachId())){
				continue;
			}
			Player memberPlayer = GlobalData.getInstance().makesurePlayer(memberId);
			if (memberPlayer == null) {
				continue;
			}
			CollegeMemberEntity member = memberPlayer.getData().getCollegeMemberEntity();
			long onlintTime = member.getOnlineTimeToday();
			memberOnlineTime.put(memberId, onlintTime);
		}
		for (CollegeOnlineRewardCfg cfg : HawkConfigManager.getInstance().getConfigIterator(CollegeOnlineRewardCfg.class)) {
			int id = cfg.getId();
			int takeLimit = CollegeConstCfg.getInstance().getOnlineRewardLimit();
			
			OnlineRewardInfo.Builder obuilder = OnlineRewardInfo.newBuilder();
			obuilder.setId(id);
			
			Set<String> rset = rewardIdMap.getOrDefault(id, new HashSet<>());
			int takeCount = rset.size();
			
			if(takeCount >= takeLimit){
				//已经完成
				obuilder.setCanTakeCount(0);
				obuilder.setTakeCount(takeLimit);
				rltList.add(obuilder.build());
				continue;
			}
			
			int canTakeCount = 0;
			for(Map.Entry<String,Long> mtentry : memberOnlineTime.entrySet()){
				String mid = mtentry.getKey();
				long otime = mtentry.getValue();
				//已经领取过这个玩家的奖励
				if(rset.contains(mid)){
					continue;
				}
				//时间不达标
				if(otime < cfg.getOnlineTime()){
					continue;
				}
				canTakeCount ++;
			}
			obuilder.setCanTakeCount(canTakeCount);
			obuilder.setTakeCount(takeCount);
			rltList.add(obuilder.build());
		}
		return rltList;
	}

	/**
	 * 发放体力
	 * @param player
	 * @param vits
	 */
	public int vitalitySend(Player player,Map<String,Integer> vits){
		int count = 0;
		Set<String> members = this.collegeMembers.get(player.getCollegeId());
		if(Objects.isNull(members)){
			return Status.SysError.PARAMS_INVALID_VALUE;
		}
		Map<String,Integer> vitMap = RedisProxy.getInstance().getCollegeMemberVitSendToday(vits.keySet());
		Map<String,Integer> sendMap = new HashMap<>();
		for(Map.Entry<String, Integer> entry : vits.entrySet()){
			String memberId = entry.getKey();
			int sendValue = entry.getValue();
			if(sendValue <= 0){
				return Status.SysError.PARAMS_INVALID_VALUE;
			}
			if(!members.contains(memberId)){
				return Status.SysError.PARAMS_INVALID_VALUE;
			}
			Player member = GlobalData.getInstance().makesurePlayer(memberId);
			if (member == null) {
				return Status.SysError.PARAMS_INVALID_VALUE;
			}
			int sendVit = vitMap.getOrDefault(memberId, 0);
			int canSend = CollegeConstCfg.getInstance().getDayMaxStrength() - sendVit;
			canSend = Math.min(canSend, sendValue);
			if(canSend > 0){
				sendMap.put(memberId, canSend);
				count += canSend;
			}
		}
		CollegeEntity entity = this.collegeData.get(player.getCollegeId());
		int vit = entity.getCanVitalitySendValue();
		if(vit < count){
			return Status.Error.COLLEGE_VIT_SEND_LESS_VALUE;
		}
		
		double curVit = entity.getVitality() - count;
		curVit = Math.max(0, curVit);
		entity.setVitality(curVit);
		RedisProxy.getInstance().addCollegeMemberVitSendToday(sendMap);
		
		for(Map.Entry<String, Integer> entry : sendMap.entrySet()){
			String memberId = entry.getKey();
			int sendValue = entry.getValue();
			if(sendValue <= 0){
				continue;
			}
			if(!members.contains(memberId)){
				continue;
			}
			Player member = GlobalData.getInstance().makesurePlayer(memberId);
			if (member == null) {
				continue;
			}
			AwardItems awardItems = AwardItems.valueOf();
			awardItems.addItem(Const.ItemType.PLAYER_ATTR_VALUE, PlayerAttr.VIT_VALUE, sendValue);
			SystemMailService.getInstance().sendMail(MailParames.newBuilder()
					.setPlayerId(member.getId())
					.setMailId(MailId.COLLEGE_VIT_SEND)
					.setRewards(awardItems.getAwardItems())
					.setAwardStatus(MailRewardStatus.NOT_GET)
					.build());
			
			HawkLog.logPrintln("college service vitalitySend, collegeId: {}, coachId: {},memberId:{},send:{},last:{}", 
					entity.getId(), memberId,sendValue,entity.getCanVitalitySendValue());
		}
		CollegeVitalitySendResp.Builder resp = CollegeVitalitySendResp.newBuilder();
		resp.setVitality(CollegeService.getInstance().genVitBuilder(player));
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.COLLEGE_VITALITY_INFO_RESP_S_VALUE, resp));
		return Status.SysError.SUCCESS_OK_VALUE;
		
	}
	

	
	/**
	 * 兑换商店商品刷新检测（日刷新(1)，周刷新(2)，月刷新(3)，终身一次(4)）
	 * @param memberEntity
	 * @param curTime
	 * @return
	 */
	public boolean updateExchangeShopData(CollegeMemberEntity memberEntity,long curTime){
		try {
			CollegeEntity collegeEntity = collegeData.get(memberEntity.getCollegeId());
			if (Objects.isNull(collegeEntity)) {
				return false;
			}
			List<Integer> dels= new ArrayList<>();
			List<Integer> add= new ArrayList<>();
			List<Integer> update= new ArrayList<>();
			for(CollegeMemberShopEntity entity : memberEntity.getShopDataMap().values()){
				CollegeShopCfg cfg =  HawkConfigManager.getInstance().getConfigByKey(CollegeShopCfg.class, entity.getId());
				if(Objects.isNull(cfg)){
					dels.add(entity.getId());
					continue;
				}
				if(cfg.getRefreshType() == ShopRefreshType.REFRESH_NEVER){
					continue;
				}
				//每天刷新
				if(cfg.getRefreshType() == ShopRefreshType.REFRESH_DAILY &&
						!HawkTime.isSameDay(curTime, entity.getRefreshTime())){
					entity.setRefreshTime(curTime);
					entity.setBuyCount(0);
					update.add(cfg.getId());
					continue;
				}
				//每周刷新
				if(cfg.getRefreshType() == ShopRefreshType.REFRESH_WEEKLY &&
						!HawkTime.isSameWeek(curTime, entity.getRefreshTime())){
					entity.setRefreshTime(curTime);
					entity.setBuyCount(0);
					update.add(cfg.getId());
					continue;
				}
				//每月刷新
				if(cfg.getRefreshType() == ShopRefreshType.REFRESH_MONTH &&
						!this.sameMonth(curTime, entity.getRefreshTime())){
					entity.setRefreshTime(curTime);
					entity.setBuyCount(0);
					update.add(cfg.getId());
					continue;
				}
			}
			//删除
			dels.forEach(id ->memberEntity.getShopDataMap().remove(id));
			//添加
			ConfigIterator<CollegeShopCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(CollegeShopCfg.class);
			while (iterator.hasNext()) {
				CollegeShopCfg shopCfg = iterator.next();
				if(memberEntity.getShopDataMap().containsKey(shopCfg.getId())){
					continue;
				}
				CollegeMemberShopEntity data = CollegeMemberShopEntity.valueOf(shopCfg.getId(),curTime);
				memberEntity.getShopDataMap().put(data.getId(), data);
				add.add(data.getId());
			}
			if(dels.size() > 0 || add.size() > 0 || update.size() > 0){
				memberEntity.notifyUpdate();
				return true;
			}
			return false;
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return false;
		
	}

	/**
	 * 直购商店商品刷新检测
	 * @param memberEntity
	 * @param curTime
	 * @return
	 */
	public boolean updateGiftShopData(CollegeMemberEntity memberEntity, long curTime){
		try {
			CollegeEntity collegeEntity = collegeData.get(memberEntity.getCollegeId());
			if (Objects.isNull(collegeEntity)) {
				return false;
			}
			int collegeLevel = collegeEntity.getLevel();
			CollegeLevelCfg levelCfg = HawkConfigManager.getInstance()
					.getConfigByKey(CollegeLevelCfg.class,collegeLevel);
			if (Objects.isNull(levelCfg)) {
				return false;
			}
			int openCount = levelCfg.getCollegeShopGrid();
			List<Integer> dels= new ArrayList<>();
			List<Integer> refershdels= new ArrayList<>();
			List<Integer> add= new ArrayList<>();
			
			CollegeMemberGiftEntity giftEntity = memberEntity.getGiftData();
			long refreshTime = giftEntity.getRefreshTime();
			Map<Integer,Integer> giftMap = giftEntity.getGiftMap();
			for(int gid : giftMap.keySet()){
				CollegePurchaseCfg cfg =  HawkConfigManager.getInstance().getConfigByKey(CollegePurchaseCfg.class, gid);
				if(Objects.isNull(cfg)){
					dels.add(gid);
				}
			}
			//删除没有配置的数据
			dels.forEach(id -> giftMap.remove(id));
			//每周刷新
			if(!HawkTime.isSameWeek(curTime, refreshTime)){
				giftEntity.setRefreshTime(curTime);
				for(int gid : giftMap.keySet()){
					CollegePurchaseCfg cfg =  HawkConfigManager.getInstance().getConfigByKey(CollegePurchaseCfg.class, gid);
					if(Objects.isNull(cfg)){
						continue;
					}
					//每周刷新
					if(cfg.getRefreshType() == GiftRefreshType.WEEKLY){
						refershdels.add(gid);
						continue;
					}
				}
				//删除周刷新的
				refershdels.forEach(id -> giftMap.remove(id));
				
				int need = openCount - giftMap.size();
				Map<CollegePurchaseCfg,Integer> ranList = new HashMap<>();
				List<CollegePurchaseCfg> mustList = new ArrayList<>();
				int serverOpenDays = GlobalData.getInstance().getServerOpenDays();
				
				ConfigIterator<CollegePurchaseCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(CollegePurchaseCfg.class);
				while (iterator.hasNext()) {
					CollegePurchaseCfg giftCfg = iterator.next();
					//等级不合适
					if (giftCfg.getLimitLevel() > collegeLevel) {
						continue;
					}
					//开服天数不合适
					int startDay = giftCfg.getStartDay();
					int endDay = giftCfg.getEndDay();
					if (serverOpenDays < startDay || serverOpenDays > endDay) {
						continue;
					}
					//必出
					if (giftCfg.getFixed() > 0 && 
							!giftMap.containsKey(giftCfg.getId())) {
						mustList.add(giftCfg);
						continue;
					}
					//随机
					if(giftCfg.getFixed() <= 0 &&
							!giftMap.containsKey(giftCfg.getId())){
						ranList.put(giftCfg, giftCfg.getWeight());
					}
				}
				if(ranList.size() > 0 || mustList.size()>0){
					for(int i=0;i<need;i++){
						CollegePurchaseCfg chose = null;
						if(mustList.size() >0){
							chose = mustList.remove(0);
						}else if(ranList.size() > 0){
							chose = HawkRand.randomWeightObject(ranList);
							ranList.remove(chose);
						}
						if(Objects.isNull(chose)){
							continue;
						}
						giftMap.put(chose.getId(), 0);
						add.add(chose.getId());
					}
				}
				
			}
			if(dels.size() > 0 || refershdels.size() >0 || add.size() > 0 || !HawkTime.isSameWeek(curTime, refreshTime)){
				memberEntity.notifyUpdate();
				return true;
			}
			return false;
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return false;
	}
	
	
	public void sendGiftDispenseReward(String buyerName,String collegeId,String reward){
		if(HawkOSOperator.isEmptyString(reward)){
			return;
		}
		Set<String> set = this.collegeMembers.get(collegeId);
		if(Objects.isNull(set)){
			return;
		}
		for(String mid : set){
			AwardItems awardItems = AwardItems.valueOf();
			awardItems.addItemInfos(ItemInfo.valueListOf(reward));
			SystemMailService.getInstance().sendMail(MailParames.newBuilder()
					.setPlayerId(mid)
					.setMailId(MailId.COLLEGE_DISPENSE_REWARD)
					.setRewards(awardItems.getAwardItems())
					.setAwardStatus(MailRewardStatus.NOT_GET)
					.addContents(buyerName)
					.build());
		}
	}
	
	
	public boolean sameMonth(long time1,long time2){
		Calendar calendar = HawkTime.getCalendar(true);
		calendar.setTimeInMillis(time1);
		int year1 = calendar.get(1);
		int month1 = calendar.get(2);
		calendar.setTimeInMillis(time2);
		int year2 = calendar.get(1);
		int month2 = calendar.get(2);
		if(year1 == year2 && month1 == month2){
			return true;
		}
		return false;
	}
	
	
	
	public String createGuildApplayId(String collegeId){
		CollegeEntity collegeEntity = collegeData.get(collegeId);
		if (Objects.isNull(collegeEntity)) {
			return null;
		}
		Map<String, Long> map = collegeEntity.getGuildApplyMap();
		String id = HawkUUIDGenerator.genUUID();
		long time = HawkTime.getMillisecond();
		map.put(id, time);
		collegeEntity.resetGuildApplyTime(time);
		return id;
	}
	
	public boolean sendGuildApplyInCd(String collegeId){
		CollegeEntity collegeEntity = collegeData.get(collegeId);
		if (Objects.isNull(collegeEntity)) {
			return true;
		}
		long time = collegeEntity.getGuildApplyTime();
		long curTime = HawkTime.getMillisecond();
		if(curTime < time + CollegeConstCfg.getInstance().getLetterSendCD() * 1000){
			return true;
		}
		return false;
	}
	
	public boolean guildApplyOutTime(CollegeEntity collegeEntity,String applyId){
		Map<String, Long> map = collegeEntity.getGuildApplyMap();
		long sendTime = map.getOrDefault(applyId, 0l);
		long time = HawkTime.getMillisecond();
		if(time > sendTime + CollegeConstCfg.getInstance().getLetterContinuedCD() * 1000){
			return true;
		}
		return false;
	}
	
	
	public boolean joinFromGuildApplyInCd(CollegeEntity collegeEntity,String playerId){
		Map<String, Long> map = collegeEntity.getKickOutTimeMap();
		long kickTime = map.getOrDefault(playerId, 0l);
		long time = HawkTime.getMillisecond();
		if(time > kickTime + CollegeConstCfg.getInstance().getLetterAgainJoinCD() * 1000){
			return false;
		}
		return true;
	}
	
	
	public boolean sendInviteMailInCd(String collegeId, String targetId){
		CollegeEntity collegeEntity = this.collegeData.get(collegeId);
		if(Objects.isNull(collegeEntity)){
			return true;
		}
		Map<String, Long> map = collegeEntity.getInviteMailSendMap();
		long mailTime = map.getOrDefault(targetId, 0l);
		long time = HawkTime.getMillisecond();
		if(time > mailTime + CollegeConstCfg.getInstance().getLetterSendCD() * 1000){
			return false;
		}
		return true;
	}
	
	public void restInviteMailSendTime(String collegeId,String targetId){
		CollegeEntity collegeEntity = this.collegeData.get(collegeId);
		if(Objects.isNull(collegeEntity)){
			return;
		}
		long curTime = HawkTime.getMillisecond();
		Map<String, Long> map = collegeEntity.getInviteMailSendMap();
		map.put(targetId, curTime);
	}
	
	
	
	/**
	 * 构建可领奖列表信息
	 * @param self
	 * @param memberId
	 * @return
	 */
	public int achieveOnlienRewards(Player player,int tid) {
		CollegeMemberEntity data = player.getData().getCollegeMemberEntity();
		if(Objects.isNull(data)){
			return Status.Error.COLLEGE_NOT_EXIST_VALUE;
		}
		String collegeId = data.getCollegeId();
		if (HawkOSOperator.isEmptyString(collegeId) || 
				!collegeMembers.containsKey(collegeId)) {
			return Status.Error.COLLEGE_NOT_EXIST_VALUE;
		}
		CollegeEntity college = this.collegeData.get(collegeId);
		if (Objects.isNull(college)) {
			return Status.Error.COLLEGE_NOT_EXIST_VALUE;
		}
		List<Integer> tabkeIds = new ArrayList<>();
		if(tid == 0){
			//全领取
			for (CollegeOnlineRewardCfg cfg : HawkConfigManager.getInstance().getConfigIterator(CollegeOnlineRewardCfg.class)) {
				tabkeIds.add(cfg.getId());
			}
		}else{
			//指定领取
			tabkeIds.add(tid);
		}
		List<Integer> canGet = new ArrayList<>();
		Map<String, List<Integer>> rewardedMap = data.getOnlineTookMap();
		Map<Integer,Set<String>> rewardIdMap = new HashMap<>();
		for(Map.Entry<String,List<Integer>> entry : rewardedMap.entrySet()){
			String mid = entry.getKey();
			List<Integer> rlist = entry.getValue();
			for(int rid : rlist){
				Set<String> rset = rewardIdMap.get(rid);
				if(Objects.isNull(rset)){
					rset = new HashSet<>();
					rewardIdMap.put(rid, rset);
				}
				rset.add(mid);
			}
		}
		Set<String> members = this.collegeMembers.get(collegeId);
		Map<String,Long> memberOnlineTime = new HashMap<String,Long>();
		for(String memberId : members){
			if(memberId.equals(college.getCoachId())){
				continue;
			}
			Player memberPlayer = GlobalData.getInstance().makesurePlayer(memberId);
			if (memberPlayer == null) {
				continue;
			}
			CollegeMemberEntity member = memberPlayer.getData().getCollegeMemberEntity();
			long onlintTime = member.getOnlineTimeToday();
			memberOnlineTime.put(memberId, onlintTime);
		}
		
		for (CollegeOnlineRewardCfg cfg : HawkConfigManager.getInstance().getConfigIterator(CollegeOnlineRewardCfg.class)) {
			if(!tabkeIds.contains(cfg.getId())){
				continue;
			}
			int cfgId = cfg.getId();
			int takeLimit = CollegeConstCfg.getInstance().getOnlineRewardLimit();
			
			Set<String> rset = rewardIdMap.get(cfg.getId());
			if(Objects.isNull(rset)){
				rset = new HashSet<>();
				rewardIdMap.put(cfg.getId(), rset);
			}
			for(Map.Entry<String,Long> mtentry : memberOnlineTime.entrySet()){
				String mid = mtentry.getKey();
				long otime = mtentry.getValue();
				if(rset.size() >= takeLimit){
					//已经完成
					continue;
				}
				//已经领取过这个玩家的奖励
				if(rset.contains(mid)){
					continue;
				}
				//时间不达标
				if(otime < cfg.getOnlineTime()){
					continue;
				}
				canGet.add(cfgId);
				rset.add(mid);
				List<Integer> rewardedList = rewardedMap.get(mid);
				if (rewardedList == null) {
					rewardedList = new ArrayList<>();
					rewardedMap.put(mid, rewardedList);
				}
				rewardedList.add(cfgId);
				LogUtil.logCollegeOnlineRewarded(player, data.getCollegeId(), mid, cfgId);
			}
			
		}
		
		
		if (canGet.isEmpty()) {
			return Status.Error.COLLEGE_NO_ONLINE_REWARD_VALUE;
		}
		data.notifyUpdate();
		AwardItems awardItems = AwardItems.valueOf();
		for(Integer cfgId : canGet){
			CollegeOnlineRewardCfg cfg = HawkConfigManager.getInstance().getConfigByKey(CollegeOnlineRewardCfg.class, cfgId);
			if(cfg == null){
				continue;
			}
			awardItems.addItemInfos(cfg.getRewardItemInfo());
		}
		awardItems.rewardTakeAffectAndPush(player, Action.COLLEGE_ONLINE_REWARD, true);
		this.syncOnlineRewardInfo(player);
		// 行为日志
		BehaviorLogger.log4Service(player, Source.COLLEGE, Action.COLLEGE_ONLINE_REWARD, 
				Params.valueOf("collegeId", data.getCollegeId()),
				Params.valueOf("targetPlayerId", ""),
				Params.valueOf("targetPlayerName", ""),
				Params.valueOf("ids", canGet));
		return Status.SysError.SUCCESS_OK_VALUE;
		
	}
	
}
