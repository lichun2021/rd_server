package com.hawk.activity.type.impl.heavenBlessing;

import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.constant.ObjType;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.HeavenBlessingActiveEvent;
import com.hawk.activity.event.impl.PayGiftBuyEvent;
import com.hawk.activity.msg.PlayerRewardFromActivityMsg;
import com.hawk.activity.type.ActivityState;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.achieve.AchieveContext;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.achieve.helper.AchievePushHelper;
import com.hawk.activity.type.impl.achieve.provider.AchieveItems;
import com.hawk.activity.type.impl.achieve.provider.AchieveProvider;
import com.hawk.activity.type.impl.heavenBlessing.cfg.*;
import com.hawk.activity.type.impl.heavenBlessing.entity.HeavenBlessingEntity;
import com.hawk.game.protocol.*;
import com.hawk.game.protocol.Activity.HeavenBlessingSync;
import com.hawk.log.Action;
import org.hawk.config.HawkConfigManager;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.os.HawkException;
import org.hawk.os.HawkTime;
import org.hawk.result.Result;
import org.hawk.task.HawkTaskManager;
import org.hawk.xid.HawkXID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 鸿福天降
 * 活动详情:针对腾讯提供的染色用户，通过vip等级和付费量来决定激活活动的等级
 */
public class HeavenBlessingActivity extends ActivityBase implements AchieveProvider {

    private static final Logger logger = LoggerFactory.getLogger("Server");
    public static final int NOT_PAY = 0;//还未购买礼包
    public static final int CAN_GET = 1;//已经购买礼包，还未领取
    public static final int HAVE_GET = 2;//已经领取

    /**
     * 构造函数
     *
     * @param activityId
     * @param activityEntity
     */
    public HeavenBlessingActivity(int activityId, ActivityEntity activityEntity) {
        super(activityId, activityEntity);
    }

    /**
     * 活动类型
     *
     * @return
     */
    @Override
    public ActivityType getActivityType() {
        return ActivityType.HEAVEN_BESSING_ACTIVTY;
    }
    
    @Override
	public int providerActivityId() {
		return this.getActivityType().intValue();
	}

    /**
     * 活动实例
     *
     * @param config
     * @param activityEntity
     * @return
     */
    @Override
    public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
        //创建活动实例
        HeavenBlessingActivity activity = new HeavenBlessingActivity(config.getActivityId(), activityEntity);
        //注册当前活动到成就系统
        AchieveContext.registeProvider(activity);
        return activity;
    }

    /**
     * 从数据库加载玩家活动数据
     *
     * @param playerId
     * @param termId
     * @return
     */
    @Override
    protected HawkDBEntity loadFromDB(String playerId, int termId) {
        //根据玩家id和活动期数去数据库里取数据
        List<HeavenBlessingEntity> queryList = HawkDBManager.getInstance()
                .query("from HeavenBlessingEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
        if (queryList != null && queryList.size() > 0) {
            HeavenBlessingEntity entity = queryList.get(0);
            return entity;
        }
        return null;
    }

    /**
     * 初始化玩家活动数据
     *
     * @param playerId
     * @param termId
     * @return
     */
    @Override
    protected HawkDBEntity createDataEntity(String playerId, int termId) {
        HeavenBlessingEntity entity = new HeavenBlessingEntity(playerId, termId);
        return entity;
    }

    /**
     * 登录触发逻辑
     *
     * @param playerId
     */
    @Override
    public void onPlayerLogin(String playerId) {
        //获得玩家活动数据
        Optional<HeavenBlessingEntity> opEntity = getPlayerDataEntity(playerId);
        if (!opEntity.isPresent()) {
            return;
        }
        HeavenBlessingEntity entity = opEntity.get();
        long now = HawkTime.getMillisecond();
        //获得活动基础配置
        HeavenBlessingKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(HeavenBlessingKVCfg.class);
        //如果未激活就进行检查
        if (!entity.isActiveState()) {
            //超过循环天数才可以激活下次
            if (cfg != null && now - entity.getActiveTime() > cfg.getLoopDays()) {
                //通过tx接口进行检查
                checkHeavenBlessingActivityOpen(playerId);
            }

        } else {
            int termId = getActivityTermId();
            long endTime = Math.min(entity.getActiveTime() + cfg.getContinuouslyTime(), getTimeControl().getEndTimeByTermId(termId));
            if (now > endTime) {
                if (entity.getCustomState() == CAN_GET) {
                    entity.setCustomState(HAVE_GET);
                    //读取当前档位配置
                    HeavenBlessingLevelCfg levelCfg = HawkConfigManager.getInstance().getConfigByKey(HeavenBlessingLevelCfg.class, entity.getLevel());
                    //如果当前档位配置为空直接返回错误码
                    if (levelCfg != null) {
                        //根据选择组合自定义奖励
                        List<Reward.RewardItem.Builder> rewardList = new ArrayList<>();
                        rewardList.add(levelCfg.getCustomAwards().get(entity.getChoose()));
                        rewardList.addAll(levelCfg.getConstantAwards());
                        //如果有奖励数据就进行发奖
                        if (!rewardList.isEmpty()) {
                            // 补发直购邮件
                            sendMailToPlayer(playerId, MailConst.MailId.HEAVEN_BLESSING_MAIL_END, null, null, null, rewardList, false);
                            this.getDataGeter().logHeavenBlessingAward(playerId, entity.getGroupId(), entity.getLevel(), entity.getPayCount(), entity.getChoose());
                        }
                    }
                }
                entity.setActiveState(false);
                entity.notifyUpdate();
                //超过循环天数才可以激活下次
                if (cfg != null && now - entity.getActiveTime() > cfg.getLoopDays()) {
                    //通过tx接口进行检查
                    checkHeavenBlessingActivityOpen(playerId);
                }
            }
            //给前端下发活动数据
            syncActivityInfo(playerId, entity);
        }
    }

    /**
     * 活动开启关闭条件
     *
     * @param playerId
     * @return
     */
    @Override
    public boolean isActivityClose(String playerId) {
        Optional<HeavenBlessingEntity> opEntity = getPlayerDataEntity(playerId);
        if (!opEntity.isPresent()) {
            return true;
        }
        HeavenBlessingEntity entity = opEntity.get();
        return !entity.isActiveState();
    }

    /**
     * 调用腾讯的接口判断玩家是否可以激活活动
     *
     * @param playerId
     */
    public void checkHeavenBlessingActivityOpen(String playerId) {
        this.getDataGeter().checkHeavenBlessingActivityOpen(playerId);
    }

    /**
     * 玩家活动激活事件响应逻辑
     *
     * @param event
     */
    @Subscribe
    public void onActiveEvent(HeavenBlessingActiveEvent event) {
        //获得玩家数据
        String playerId = event.getPlayerId();
        ActivityState state = getIActivityEntity(playerId).getActivityState();
        if (state != ActivityState.OPEN) {
            return;
        }
        Optional<HeavenBlessingEntity> opEntity = getPlayerDataEntity(playerId);
        //如果数据为空直接返回
        if (!opEntity.isPresent()) {
            return;
        }
        //获得对应vip等级配置
        HeavenBlessingVipCfg vipCfg = HawkConfigManager.getInstance().getConfigByKey(HeavenBlessingVipCfg.class, event.getVip());
        //如果配置为空直接返回
        if (vipCfg == null) {
            return;
        }
        //根据付费额获取激活的付费组
        Integer groupId = vipCfg.getGroupId(event.getMoney());
        //获得对应付费组配置
        HeavenBlessingGroupCfg groupCfg = HawkConfigManager.getInstance().getConfigByKey(HeavenBlessingGroupCfg.class, groupId);
        //如果配置为空直接返回
        if (groupCfg == null) {
            return;
        }
        //获得玩家数据
        HeavenBlessingEntity entity = opEntity.get();
        //付费次数已达上限直接返回
        if (groupCfg.getLevelList().size() <= 0) {
            return;
        }
        //获得当前激活档位
        int level = groupCfg.getLevelList().get(0);
        //获得当前档位配置
        HeavenBlessingLevelCfg levelCfg = HawkConfigManager.getInstance().getConfigByKey(HeavenBlessingLevelCfg.class, level);
        //如果等级配置为空直接返回
        if (levelCfg == null) {
            return;
        }
        //当前付费组id
        entity.setGroupId(groupId);
        //当前付费档位
        entity.setLevel(level);
        //付费次数,用于切换档位
        entity.setPayCount(0);
        //设置激活状态
        entity.setActiveState(true);
        entity.setActiveTime(HawkTime.getMillisecond());
        //设置自定义奖励索引默认值
        entity.setChoose(0);
        //设置为未付费状态
        entity.setCustomState(NOT_PAY);
        //初始化成就数据
        List<AchieveItem> itemList = new ArrayList<>();
        //获得活动基础配置
        HeavenBlessingKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(HeavenBlessingKVCfg.class);
        //获得免费奖励配置
        AchieveConfig freeAchieveCfg = getAchieveCfg(cfg.getFreeAward());
        //检查配置是否为空且类型是否正确
        if (freeAchieveCfg != null && freeAchieveCfg.getAchieveType() == AchieveType.HEAVEN_BLESSING_FREE) {
            //生成免费成就数据并且把成就状态设置成可领奖
            AchieveItem free = AchieveItem.valueOf(cfg.getFreeAward());
            free.setState(Activity.AchieveState.NOT_REWARD_VALUE);
            itemList.add(free);
        }
        //检查自定义奖励索引是否合法
        if (entity.getChoose() < levelCfg.getRandomAwards().size()) {
            //获得随机奖励成就Id
            int randomAchieveId = levelCfg.getRandomAwards().get(entity.getChoose());
            //获得配置
            AchieveConfig randomAchieveCfg = getAchieveCfg(randomAchieveId);
            //检查配置是否为空且类型是否正确
            if (randomAchieveCfg != null && randomAchieveCfg.getAchieveType() == AchieveType.HEAVEN_BLESSING_PAY) {
                //生成随机成就数据
                AchieveItem random = AchieveItem.valueOf(randomAchieveId);
                itemList.add(random);
            }
        }
        //原成就数据不为空则推送前端删除原有成就数据
        if (!entity.getItemList().isEmpty()) {
            AchievePushHelper.pushAchieveDelete(playerId, entity.getItemList());
        }
        //设置新的成就数据
        entity.setItemList(itemList);
        //更新到数据库
        entity.notifyUpdate();
        //把新的成就数据推送给前端
        AchievePushHelper.pushAchieveUpdate(playerId, entity.getItemList());
        //推送新的活动数据
        syncActivityInfo(playerId, entity);
        //更新活动状态
        syncActivityStateInfo(playerId);
        this.getDataGeter().logHeavenBlessingActive(playerId, entity.getGroupId());
    }

    /**
     * 礼包购买事件响应逻辑
     *
     * @param event
     */
    @Subscribe
    public void onBuyGiftEvent(PayGiftBuyEvent event) {
        //获得玩家数据
        String playerId = event.getPlayerId();
        Optional<HeavenBlessingEntity> opEntity = getPlayerDataEntity(playerId);
        if (!opEntity.isPresent()) {
            return;
        }
        HeavenBlessingEntity entity = opEntity.get();
        //获得当前档位配置
        HeavenBlessingLevelCfg levelCfg = HawkConfigManager.getInstance().getConfigByKey(HeavenBlessingLevelCfg.class, entity.getLevel());
        //配置为空直接返回
        if (levelCfg == null) {
            return;
        }
        //获得礼包id
        int buyId = Integer.parseInt(event.getGiftId());
        //如果购买的礼包不是当前档位的礼包的话直接返回
        if (levelCfg.getAndroidPayId() != buyId && levelCfg.getIosPayId() != buyId) {
            return;
        }
        //自定义奖励状态
        entity.setCustomState(CAN_GET);
        //需要更新的成就数据
        List<AchieveItem> updateList = new ArrayList<>();
        for (AchieveItem item : entity.getItemList()) {
            AchieveConfig config = getAchieveCfg(item.getAchieveId());
            if (config == null) {
                continue;
            }
            if (config.getAchieveType() == AchieveType.HEAVEN_BLESSING_PAY) {
                //更新成就状态为未领奖
                item.setState(Activity.AchieveState.NOT_REWARD_VALUE);
                updateList.add(item);
            }
        }
        //推送前端成就数据
        AchievePushHelper.pushAchieveUpdate(playerId, updateList);
        //推送活动数据
        syncActivityInfo(playerId, entity);
        this.getDataGeter().logHeavenBlessingPay(playerId, entity.getGroupId(), entity.getLevel(), entity.getPayCount(), entity.getChoose());
    }

    /**
     * 判断洪福天降礼包是否可以被购买
     *
     * @param playerId
     * @param giftId
     * @return
     */
    public boolean buyGiftCheck(String playerId, String giftId) {
        Optional<HeavenBlessingEntity> opEntity = getPlayerDataEntity(playerId);
        if (!opEntity.isPresent()) {
            logger.info("HeavenBlessingActivity pay error 1, playerId: {}, giftId: {}", playerId, giftId);
            return false;
        }
        HeavenBlessingEntity entity = opEntity.get();
        if (!entity.isActiveState()) {
            logger.info("HeavenBlessingActivity pay error 2, playerId: {}, giftId: {}", playerId, giftId);
            return false;
        }
        if (entity.getCustomState() != NOT_PAY) {
            logger.info("HeavenBlessingActivity pay error 3, playerId: {}, giftId: {}", playerId, giftId);
            return false;
        }
        //获得当前档位配置
        HeavenBlessingLevelCfg levelCfg = HawkConfigManager.getInstance().getConfigByKey(HeavenBlessingLevelCfg.class, entity.getLevel());
        //配置为空直接返回
        if (levelCfg == null) {
            logger.info("HeavenBlessingActivity pay error 4, playerId: {}, giftId: {}", playerId, giftId);
            return false;
        }
        //获得礼包id
        int buyId = Integer.parseInt(giftId);
        if (levelCfg.getAndroidPayId() != buyId && levelCfg.getIosPayId() != buyId) {
            logger.info("HeavenBlessingActivity pay error 5, playerId: {}, giftId: {}", playerId, giftId);
            return false;
        }
        return true;
    }

    /**
     * 选择自定义奖励
     *
     * @param playerId
     * @param choose
     * @return
     */
    public Result<Integer> choose(String playerId, int choose) {
        //获得玩家数据
        Optional<HeavenBlessingEntity> opEntity = getPlayerDataEntity(playerId);
        if (!opEntity.isPresent()) {
            return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
        }
        HeavenBlessingEntity entity = opEntity.get();
        //只有自定义奖励状态为未付费的情况下，否则返回错误码
        if (entity.getCustomState() != NOT_PAY) {
            return Result.fail(Status.Error.ACTIVITY_CONFIG_NOT_FOUND_VALUE);
        }
        //获得当前档位配置
        HeavenBlessingLevelCfg levelCfg = HawkConfigManager.getInstance().getConfigByKey(HeavenBlessingLevelCfg.class, entity.getLevel());
        //当前档位配置为空返回错误吗
        if (levelCfg == null) {
            return Result.fail(Status.Error.ACTIVITY_CONFIG_NOT_FOUND_VALUE);
        }
        //判断参数合法不合法，否则返回错误码
        if (choose < 0 || choose >= levelCfg.getCustomAwards().size()) {
            return Result.fail(Status.Error.ACTIVITY_CONFIG_NOT_FOUND_VALUE);
        }
        //设置选择数据
        entity.setChoose(choose);
        //老成就数据
        List<AchieveItem> oldList = entity.getItemList();
        //新成就数据
        List<AchieveItem> newList = new ArrayList<>();
        //老数据中保留免费成就
        for (AchieveItem item : oldList) {
            AchieveConfig config = getAchieveCfg(item.getAchieveId());
            //配置为空直接跳过
            if (config == null) {
                continue;
            }
            //免费成就保留，随机成就删除
            if (config.getAchieveType() == AchieveType.HEAVEN_BLESSING_FREE) {
                newList.add(item);
            }
        }
        //添加新的随机奖励
        AchieveItem random = AchieveItem.valueOf(levelCfg.getRandomAwards().get(choose));
        newList.add(random);
        //更改成就数据
        entity.setItemList(newList);
        AchievePushHelper.pushAchieveUpdate(playerId, newList);
        //更新活动数据
        syncActivityInfo(playerId, entity);
        //返回执行成功
        return Result.success();
    }


    /**
     * 领取自定义奖励
     *
     * @param playerId
     * @return
     */
    public Result<Integer> award(String playerId) {
        //获得玩家活动数据
        Optional<HeavenBlessingEntity> opEntity = getPlayerDataEntity(playerId);
        //如果没有直接返回错误码
        if (!opEntity.isPresent()) {
            return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
        }
        //获得玩家活动数据
        HeavenBlessingEntity entity = opEntity.get();
        //如果不是可领奖状态返回错误码
        if (entity.getCustomState() != CAN_GET) {
            return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
        }
        //修改领奖状态为已领取
        entity.setCustomState(HAVE_GET);
        //读取当前档位配置
        HeavenBlessingLevelCfg levelCfg = HawkConfigManager.getInstance().getConfigByKey(HeavenBlessingLevelCfg.class, entity.getLevel());
        //如果当前档位配置为空直接返回错误码
        if (levelCfg == null) {
            return Result.fail(Status.Error.ACTIVITY_CONFIG_NOT_FOUND_VALUE);
        }
        //根据选择组合自定义奖励
        List<Reward.RewardItem.Builder> rewardList = new ArrayList<>();
        rewardList.add(levelCfg.getCustomAwards().get(entity.getChoose()));
        rewardList.addAll(levelCfg.getConstantAwards());
        //如果有奖励数据就进行发奖
        if (!rewardList.isEmpty()) {
            HawkXID xid = HawkXID.valueOf(ObjType.PLAYER, playerId);
            Reward.RewardOrginType orginType = Reward.RewardOrginType.ACTIVITY_REWARD;
            PlayerRewardFromActivityMsg msg = PlayerRewardFromActivityMsg.valueOf(rewardList, this.takeRewardAction(), true, orginType, getActivityId());
            HawkTaskManager.getInstance().postMsg(xid, msg);
            // 收据邮件
            sendMailToPlayer(playerId, MailConst.MailId.HEAVEN_BLESSING_MAIL_PAY, null, null, null, rewardList, true);
        }
        this.getDataGeter().logHeavenBlessingAward(playerId, entity.getGroupId(), entity.getLevel(), entity.getPayCount(), entity.getChoose());
        if (!checkNextLevel(playerId, entity)) {
            syncActivityInfo(playerId, entity);
            syncActivityStateInfo(playerId);
        }
        //返回成功
        return Result.success();
    }

    /**
     * 同步活动数据
     *
     * @param playerId
     * @param entity
     */
    public void syncActivityInfo(String playerId, HeavenBlessingEntity entity) {
        //获得活动基础配置
        HeavenBlessingKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(HeavenBlessingKVCfg.class);
        if (cfg == null) {
            return;
        }
        //活动数据
        HeavenBlessingSync.Builder builder = HeavenBlessingSync.newBuilder();
        builder.setFreeAchieveId(0);
        builder.setRandomAchieveId(0);
        //获取成就数据
        for (AchieveItem item : entity.getItemList()) {
            AchieveConfig config = getAchieveCfg(item.getAchieveId());
            //没有配置的话跳过
            if (config == null) {
                continue;
            }
            if (config.getAchieveType() == AchieveType.HEAVEN_BLESSING_FREE) {
                //免费奖励成就Id
                builder.setFreeAchieveId(item.getAchieveId());
            }
            if (config.getAchieveType() == AchieveType.HEAVEN_BLESSING_PAY) {
                //随机奖励成就Id
                builder.setRandomAchieveId(item.getAchieveId());
            }
        }
        //自定义奖励选择索引
        builder.setCustomChoose(entity.getChoose());
        //自定义奖励状态
        builder.setCustomState(entity.getCustomState());
        //结束时间
        if (entity.isActiveState()) {
            int termId = getActivityTermId();
            long endTime = Math.min(entity.getActiveTime() + cfg.getContinuouslyTime(), getTimeControl().getEndTimeByTermId(termId));
            builder.setEndTime(endTime);
        } else {
            builder.setEndTime(0);
        }
        //当前激活档位
        builder.setLevelId(entity.getLevel());
        //发送给前端
        pushToPlayer(playerId, HP.code.HEAVEN_BLESSING_SYNC_VALUE, builder);
    }


    private boolean checkNextLevel(String playerId, HeavenBlessingEntity entity) {
        //如果成就没有都领取
        for (AchieveItem item : entity.getItemList()) {
            if (item.getState() != Activity.AchieveState.TOOK_VALUE) {
                return false;
            }
        }
        if (entity.getCustomState() != HAVE_GET) {
            return false;
        }
        //获得对应付费组配置
        HeavenBlessingGroupCfg groupCfg = HawkConfigManager.getInstance().getConfigByKey(HeavenBlessingGroupCfg.class, entity.getGroupId());
        //如果配置为空直接返回
        if (groupCfg == null) {
            return false;
        }
        //已达到最高等级
        if (entity.getPayCount() + 1 >= groupCfg.getLevelList().size()) {
            entity.setActiveState(false);
            return false;
        }
        //获取下一档位id
        int nextLevel = groupCfg.getLevelList().get(entity.getPayCount() + 1);
        //读取下一档位配置
        HeavenBlessingLevelCfg levelCfg = HawkConfigManager.getInstance().getConfigByKey(HeavenBlessingLevelCfg.class, nextLevel);
        //如果下一档位配置为空直接返回错误码
        if (levelCfg == null) {
            return false;
        }
        entity.setPayCount(entity.getPayCount() + 1);
        entity.setLevel(nextLevel);
        entity.setChoose(0);
        entity.setCustomState(NOT_PAY);


        //老成就数据
        List<AchieveItem> oldList = entity.getItemList();
        //新成就数据
        List<AchieveItem> newList = new ArrayList<>();
        //老数据中保留免费成就
        for (AchieveItem item : oldList) {
            AchieveConfig config = getAchieveCfg(item.getAchieveId());
            //配置为空直接跳过
            if (config == null) {
                continue;
            }
            //免费成就保留，随机成就删除
            if (config.getAchieveType() == AchieveType.HEAVEN_BLESSING_FREE) {
                newList.add(item);
            }
        }
        //添加新的随机奖励
        AchieveItem random = AchieveItem.valueOf(levelCfg.getRandomAwards().get(entity.getChoose()));
        newList.add(random);
        //更改成就数据
        entity.setItemList(newList);
        AchievePushHelper.pushAchieveUpdate(playerId, newList);
        //更新活动数据
        syncActivityInfo(playerId, entity);
        return true;
    }

    /**
     * 活动相关成就是否激活
     *
     * @param playerId
     * @return
     */
    @Override
    public boolean isProviderActive(String playerId) {
        return isOpening(playerId);
    }

    /**
     * 活动相关成就是否需要激活
     *
     * @param playerId
     * @return
     */
    @Override
    public boolean isProviderNeedSync(String playerId) {
        return isShow(playerId);
    }

    /**
     * 当前激活成就
     *
     * @param playerId
     * @return
     */
    @Override
    public Optional<AchieveItems> getAchieveItems(String playerId) {
        //获得玩家活动数据
        Optional<HeavenBlessingEntity> opEntity = getPlayerDataEntity(playerId);
        //如果数据为空直接返回
        if (!opEntity.isPresent()) {
            return Optional.empty();
        }
        //获得玩家活动数据
        HeavenBlessingEntity entity = opEntity.get();
        //如果成就数据为空返回空数据
        if (entity.getItemList().isEmpty()) {
            return Optional.empty();
        }
        //返回当前成就数据
        AchieveItems items = new AchieveItems(entity.getItemList(), entity);
        return Optional.of(items);
    }

    /**
     * 奖励配置
     *
     * @param achieveId
     * @return
     */
    @Override
    public AchieveConfig getAchieveCfg(int achieveId) {
        AchieveConfig config = HawkConfigManager.getInstance().getConfigByKey(HeavenBlessingAchieveCfg.class, achieveId);
        return config;
    }

    /**
     * 奖励来源
     *
     * @return
     */
    @Override
    public Action takeRewardAction() {
        return Action.HEAVEN_BLESSING_REWARD;
    }

    @Override
    public void onTakeRewardSuccess(String playerId) {

    }

    @Override
    public boolean isNeedPush(String playerId) {
        //获得玩家活动数据
        Optional<HeavenBlessingEntity> opEntity = getPlayerDataEntity(playerId);
        //如果没有直接返回错误码
        if (!opEntity.isPresent()) {
            return true;
        }
        //获得玩家活动数据
        HeavenBlessingEntity entity = opEntity.get();
        if (!checkNextLevel(playerId, entity)) {
            syncActivityInfo(playerId, entity);
            syncActivityStateInfo(playerId);
            return true;
        }
        return false;
    }

    @Override
    public void onTakeRewardSuccessAfter(String playerId, List<Reward.RewardItem.Builder> reweardList, int achieveId) {
        try {
            //获取配置
            AchieveConfig config = getAchieveCfg(achieveId);
            //配置为空直接返回
            if (config == null) {
                return;
            }
            switch (config.getAchieveType()) {
                case HEAVEN_BLESSING_FREE: {
                    // 收据邮件,免费奖励
                    sendMailToPlayer(playerId, MailConst.MailId.HEAVEN_BLESSING_MAIL_FREE, null, null, null, reweardList, true);
                }
                break;
                case HEAVEN_BLESSING_PAY: {
                    // 收据邮件，随机奖励
                    sendMailToPlayer(playerId, MailConst.MailId.HEAVEN_BLESSING_MAIL_RANDOM, null, null, null, reweardList, true);
                    this.getDataGeter().logHeavenBlessingRandomAward(playerId, reweardList.get(0));
                }
                break;
            }
        } catch (Exception e) {
            HawkException.catchException(e);
        }
    }
}
