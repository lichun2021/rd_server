package com.hawk.activity.type.impl.gratefulBenefits;

import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.event.impl.GratefulBenefitsShareEvent;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.alliesWishing.entity.WishMember;
import com.hawk.activity.type.impl.gratefulBenefits.cfg.GratefulBenefitsKVCfg;
import com.hawk.activity.type.impl.gratefulBenefits.entity.GratefulBenefitsEntity;
import com.hawk.game.protocol.*;
import com.hawk.game.protocol.Activity.GratefulBenefitsSync;
import com.hawk.gamelib.GameConst;
import com.hawk.log.Action;
import org.hawk.config.HawkConfigManager;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.result.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * 感恩福利活动类
 */
public class GratefulBenefitsActivity extends ActivityBase {
    private static final Logger logger = LoggerFactory.getLogger("Server");

    public GratefulBenefitsActivity(int activityId, ActivityEntity activityEntity) {
        super(activityId, activityEntity);
    }

    @Override
    public ActivityType getActivityType() {
        return ActivityType.GRATEFUL_BENEFITS_ACTIVTY;
    }

    @Override
    public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
        GratefulBenefitsActivity activity = new GratefulBenefitsActivity(config.getActivityId(), activityEntity);
        return activity;
    }

    @Override
    protected HawkDBEntity loadFromDB(String playerId, int termId) {
        List<GratefulBenefitsEntity> queryList = HawkDBManager.getInstance()
                .query("from GratefulBenefitsEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
        if (queryList != null && queryList.size() > 0) {
            GratefulBenefitsEntity entity = queryList.get(0);
            return entity;
        }
        return null;
    }

    @Override
    protected HawkDBEntity createDataEntity(String playerId, int termId) {
        GratefulBenefitsEntity entity = new GratefulBenefitsEntity(playerId, termId);
        return entity;
    }

    @Override
    public void onOpen() {
        Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
        for (String playerId : onlinePlayerIds) {
            callBack(playerId, GameConst.MsgId.GRATEFUL_BENEFITS_OPEN, () -> {
                syncActivityDataInfo(playerId);
            });
        }
    }

    @Override
    public void onEnd() {
        Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
        for (String playerId : onlinePlayerIds) {
            callBack(playerId, GameConst.MsgId.GRATEFUL_BENEFITS_END, () -> {
                syncActivityDataInfo(playerId);
            });
        }
    }

    @Override
    public void onPlayerLogin(String playerId) {
        syncActivityDataInfo(playerId);
    }

    @Override
    public void syncActivityDataInfo(String playerId) {
        Optional<GratefulBenefitsEntity> opDataEntity = getPlayerDataEntity(playerId);
        if (opDataEntity.isPresent()) {
            syncActivityInfo(playerId, opDataEntity.get());
        }
    }

    /**
     * 同步活动信息
     * @param playerId
     * @param entity
     */
    public void syncActivityInfo(String playerId, GratefulBenefitsEntity entity) {
        GratefulBenefitsKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(GratefulBenefitsKVCfg.class);
        long createTime = this.getDataGeter().getPlayerCreateTime(playerId);
        int termId = this.getActivityTermId();
        long endTime = this.getTimeControl().getEndTimeByTermId(termId);
        int createDays = (int) TimeUnit.MILLISECONDS.toDays(cfg.getRegistrationlineValue() - createTime);
        if(createDays < 0){
            createDays = 0;
        }
        long now = HawkTime.getMillisecond();
        //刷新分享次数
        if(!HawkTime.isSameDay(now, entity.getShareRefreshTime())){
            entity.setShareCount(0);
            entity.setShareRefreshTime(now);
        }
        //构造前端信息
        GratefulBenefitsSync.Builder builder = GratefulBenefitsSync.newBuilder();
        //签到结束时间
        builder.setEndTime(endTime);
        //邀请冷却时间
        builder.setCdTime(entity.getInviteCDTime());
        //注册天数
        builder.setCreateDays(createDays);
        //可领金币数
        builder.setGold(calGold(createDays, entity.getWishMemberList().size(), entity.getPunchCount(), cfg));
        //剩余分享次数
        builder.setShareCount(cfg.getShareCount() - entity.getShareCount());
        //是否第一次打开页面
        builder.setIsFirst(entity.isFirst());
        if(now > endTime){
            //领取阶段
            if(entity.isAward()){
                //已领奖
                builder.setState(Activity.PBGratefulBenefitsState.GRATEFUL_BENEFITS_FINISH);
            }else {
                //未领奖
                builder.setState(Activity.PBGratefulBenefitsState.GRATEFUL_BENEFITS_ACHIEVE);
            }
        }else {
            //签到阶段
            if(HawkTime.isSameDay(now, entity.getLastPunchTime())){
                //今日已签到
                builder.setState(Activity.PBGratefulBenefitsState.GRATEFUL_BENEFITS_PUNCHED);
            }else {
                //今日未签到
                builder.setState(Activity.PBGratefulBenefitsState.GRATEFUL_BENEFITS_PUNCH);
            }
        }
        //帮助玩家信息
        for(WishMember member : entity.getWishMemberList()){
            builder.addMembers(member.genPBWishMember());
        }
        //发送给前端
        pushToPlayer(playerId, HP.code.GRATEFUL_BENEFITS_SYNC_VALUE, builder);
    }

    /**
     * 计算金币数
     * @param createDays
     * @param helpCount
     * @param punchCount
     * @param cfg
     * @return
     */
    private int calGold(int createDays, int helpCount,int punchCount, GratefulBenefitsKVCfg cfg){
        return (int)(cfg.getBaseGold() + createDays + cfg.getPlayerWeight() * helpCount + cfg.getPunchWeight() * punchCount);
    }

    /**
     * 前端请求活动数据
     *
     * @param playerId
     * @return
     */
    public Result<Integer> request(String playerId) {
        //获得玩家活动数据
        Optional<GratefulBenefitsEntity> opDataEntity = getPlayerDataEntity(playerId);
        //如果没有直接返回错误码
        if (!opDataEntity.isPresent()) {
            return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
        }
        //获得玩家活动数据
        GratefulBenefitsEntity entity = opDataEntity.get();
        entity.setFirst(true);
        //同步数据
        syncActivityInfo(playerId, entity);
        //返回成功
        return Result.success();
    }

    /**
     * 签到
     *
     * @param playerId
     * @return
     */
    public Result<Integer> punch(String playerId) {
        //已经到了领取时间
        if(!isOpening(playerId)){
            return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
        }
        //获得玩家活动数据
        Optional<GratefulBenefitsEntity> opDataEntity = getPlayerDataEntity(playerId);
        //如果没有直接返回错误码
        if (!opDataEntity.isPresent()) {
            return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
        }
        //获得玩家活动数据
        GratefulBenefitsEntity entity = opDataEntity.get();
        long now = HawkTime.getMillisecond();
        if(HawkTime.isSameDay(now, entity.getLastPunchTime())){
            return Result.fail(Status.Error.ACTIVITY_REWARD_IS_TOOK_VALUE);
        }
        //记录签到时间
        entity.setLastPunchTime(now);
        entity.setPunchCount(entity.getPunchCount() + 1);
        //同步数据
        syncActivityInfo(playerId, entity);
        //返回成功
        return Result.success();
    }

    /**
     * 邀请
     *
     * @param playerId
     * @return
     */
    public Result<Integer> invite(String playerId) {
        //已经到了领取时间
        if(!isOpening(playerId)){
            return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
        }
        //获得玩家活动数据
        Optional<GratefulBenefitsEntity> opDataEntity = getPlayerDataEntity(playerId);
        //如果没有直接返回错误码
        if (!opDataEntity.isPresent()) {
            return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
        }
        //获得玩家活动数据
        GratefulBenefitsEntity entity = opDataEntity.get();
        //获得联盟id
        String guildId = this.getDataGeter().getGuildId(playerId);
        //联盟id为空不可操作
        if(HawkOSOperator.isEmptyString(guildId)){
            return Result.fail(Status.Error.DO_NOT_HAVE_A_GUILD_VALUE);
        }
        //如果当前在邀请冷却期不可操作
        long now = HawkTime.getMillisecond();
        if(now <= entity.getInviteCDTime()){
            return Result.fail(Status.Error.GUILD_WISH_INVITE_IN_CD_VALUE);
        }
        //获得配置
        GratefulBenefitsKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(GratefulBenefitsKVCfg.class);
        //设置冷却时间
        entity.setInviteCDTime(now + TimeUnit.HOURS.toMillis(cfg.getCdTime()));
        //同步数据
        syncActivityInfo(playerId, entity);
        //发消息到联盟聊天
        this.getDataGeter().addWorldBroadcastMsg(Const.ChatType.GUILD_HREF, guildId,
                Const.NoticeCfgId.GRATEFUL_BENEFITS_HELP_NOTICE, playerId, playerId);
        //返回成功
        return Result.success();
    }

    /**
     * 领奖
     *
     * @param playerId
     * @return
     */
    public Result<Integer> award(String playerId) {
        //获得玩家活动数据
        Optional<GratefulBenefitsEntity> opDataEntity = getPlayerDataEntity(playerId);
        //如果没有直接返回错误码
        if (!opDataEntity.isPresent()) {
            return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
        }
        //获得玩家活动数据
        GratefulBenefitsEntity entity = opDataEntity.get();
        if(entity.isAward()){
            return Result.fail(Status.Error.ACTIVITY_REWARD_IS_TOOK_VALUE);
        }
        //计算金币数
        GratefulBenefitsKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(GratefulBenefitsKVCfg.class);
        long createTime = this.getDataGeter().getPlayerCreateTime(playerId);
        long endTime = cfg.getRegistrationlineValue();
        int createDays = (int) TimeUnit.MILLISECONDS.toDays(endTime - createTime);
        if(createDays < 0){
            createDays = 0;
        }
        int gold = calGold(createDays, entity.getWishMemberList().size(), entity.getPunchCount(), cfg);
        //设置发奖标志
        entity.setAward(true);
        //发奖
        Reward.RewardItem.Builder goldItem = Reward.RewardItem.newBuilder();
        goldItem.setItemType(Const.ItemType.PLAYER_ATTR_VALUE * GameConst.ITEM_TYPE_BASE);
        goldItem.setItemId(Const.PlayerAttr.GOLD_VALUE);
        goldItem.setItemCount(gold);
        List<Reward.RewardItem.Builder> rewardList = new ArrayList<>();
        rewardList.add(goldItem);
        this.getDataGeter().takeReward(playerId, rewardList, 1 , Action.GRATEFUL_BENEFITS_REWARD, true, Reward.RewardOrginType.GRATEFUL_BENEFITS_GET_AWARD);
        //感恩福利领奖打点
        this.getDataGeter().logGrateBenefitsAward(playerId, entity.getPunchCount(), createDays, entity.getWishMemberList().size(), gold);
        //同步数据
        syncActivityInfo(playerId, entity);
        //返回成功
        return Result.success();
    }

    /**
     * 帮助
     *
     * @param playerId
     * @param targetPlayerId
     * @return
     */
    public Result<Integer> help(String playerId, String targetPlayerId) {
        //已经到了领取时间
        if(!isOpening(playerId)){
            return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
        }
        //目标玩家为空
        if(HawkOSOperator.isEmptyString(targetPlayerId)){
            return Result.fail(Status.Error.FULLY_ARMED_UNKNOW_ERR_VALUE);
        }
        //目标玩家是自己
        if(playerId.equals(targetPlayerId)){
            return Result.fail(Status.Error.DO_NOT_TO_YOUERSELF_VALUE);
        }
        //判断是否有联盟
        String guilId = this.getDataGeter().getGuildId(playerId);
        if(HawkOSOperator.isEmptyString(guilId)){
            return Result.fail(Status.Error.FULLY_ARMED_UNKNOW_ERR_VALUE);
        }
        //判断是否为同一联盟
        String targetGuilId = this.getDataGeter().getGuildId(targetPlayerId);
        if(!guilId.equals(targetGuilId)){
            return Result.fail(Status.Error.NOT_USEFUL_INFORMATION_VALUE);
        }
        //帮助目标玩家
        callBack(targetPlayerId, GameConst.MsgId.GRATEFUL_BENEFITS_HELP, () -> {
            //获得玩家活动数据
            Optional<GratefulBenefitsEntity> opDataEntity = getPlayerDataEntity(targetPlayerId);
            //如果没有直接返回错误码
            if (!opDataEntity.isPresent()) {
                logger.info("GratefulBenefits data is null");
                PlayerPushHelper.getInstance().sendError(playerId, HP.code.GRATEFUL_BENEFITS_HELP_VALUE,
                        Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
                return;
            }
            //获得配置
            GratefulBenefitsKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(GratefulBenefitsKVCfg.class);
            //获得玩家活动数据
            GratefulBenefitsEntity entity = opDataEntity.get();
            List<WishMember> wishList = entity.getWishMemberList();
            for(WishMember member: wishList){
                if(member.getPlayerId().equals(playerId)){
                    logger.info("GratefulBenefits helped");
                    PlayerPushHelper.getInstance().sendError(playerId, HP.code.GRATEFUL_BENEFITS_HELP_VALUE,
                            Status.Error.ALLIANCE_WISH_ACCEPT_HELP_ALREADY_VALUE);
                    return;
                }
            }
            if(entity.getWishMemberList().size() >= cfg.getMaxPlayer()){
                logger.info("GratefulBenefits help is max");
                PlayerPushHelper.getInstance().sendError(playerId, HP.code.GRATEFUL_BENEFITS_HELP_VALUE,
                        Status.Error.ALLIANCE_WISH_ACCEPT_HELP_OVER_VALUE);
                return;
            }
            //组装玩家信息
            String name = this.getDataGeter().getPlayerName(playerId);
            int icon = this.getDataGeter().getIcon(playerId);
            String pfIcon = this.getDataGeter().getPfIcon(playerId);
            WishMember member = new WishMember(playerId, name, icon, pfIcon);
            //添加帮助信息到玩家列表
            entity.addGuildWishMember(member);
            //同步数据
            syncActivityInfo(targetPlayerId, entity);
            //帮助成功
            PlayerPushHelper.getInstance().responseSuccess(playerId, HP.code.GRATEFUL_BENEFITS_HELP_VALUE);
        });
        //返回成功
        return Result.success();
    }


    @Subscribe
    public void onShareEvent(GratefulBenefitsShareEvent event){
        String playerId = event.getPlayerId();
        //获得玩家活动数据
        Optional<GratefulBenefitsEntity> opDataEntity = getPlayerDataEntity(playerId);
        //如果没有直接返回错误码
        if (!opDataEntity.isPresent()) {
            logger.info("GratefulBenefits data is null");
            return;
        }
        //获得玩家活动数据
        GratefulBenefitsEntity entity = opDataEntity.get();
        //刷新分享次数
        long now = HawkTime.getMillisecond();
        if(!HawkTime.isSameDay(now, entity.getShareRefreshTime())){
            entity.setShareCount(0);
            entity.setShareRefreshTime(now);
        }
        //获得配置
        GratefulBenefitsKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(GratefulBenefitsKVCfg.class);
        //判断是否还有分享次数
        if(entity.getShareCount() >= cfg.getShareCount()){
            logger.info("GratefulBenefits share is max");
            return;
        }
        //分享次数加一
        entity.setShareCount(entity.getShareCount() + 1);
        //同步数据
        syncActivityInfo(playerId, entity);
        //发奖
        List<Reward.RewardItem.Builder> rewardList = RewardHelper.toRewardItemImmutableList(cfg.getShareReward());
        this.getDataGeter().takeReward(playerId, rewardList, Action.GRATEFUL_BENEFITS_REWARD, true);
        // 收据邮件
        sendMailToPlayer(playerId, MailConst.MailId.GRATEFUL_BENEFITS_MAIL_SHARE, null, null, null, rewardList, true);
    }

    /**
     * 跨天事件
     * @param event
     */
    @Subscribe
    public void onContinueLoginEvent(ContinueLoginEvent event) {
        String playerId = event.getPlayerId();
        if (!isOpening(playerId)) {
            return;
        }
        syncActivityDataInfo(playerId);
    }
}
