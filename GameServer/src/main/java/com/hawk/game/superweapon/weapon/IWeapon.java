package com.hawk.game.superweapon.weapon;

import com.hawk.game.config.SuperWeaponCfg;
import com.hawk.game.config.SuperWeaponConstCfg;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.SuperWeapon.SuperWeaponInfo;
import com.hawk.game.world.march.IWorldMarch;

/**
 * 超级武器接口
 * @author zhenyu.shang
 * @since 2018年4月23日
 */
public interface IWeapon {

	/**
	 * 获取超级武器坐标值
	 * @return
	 */
	public int getPointId();
	
	/**
	 * 获取超级武器等级
	 * @return
	 */
	public int getWeaponLevel();
	
	/**
	 * 获取超级武器类型
	 * @return
	 */
	public SuperWeaponCfg getWeaponCfg();
	
	/**
	 * 初始化
	 * @return
	 */
	public void init() throws Exception;
	
	/**
	 * 心跳
	 */
	public void tick(long currentTime);
	
	/**
	 * 设置超级武器状态
	 * @param status
	 */
	public void setStatus(int status);
	
	/**
	 * 获取超级武器状态
	 * @return
	 */
	public int getStatus();
	
	/**
	 * 设置开始时间
	 * @param startTime
	 */
	public void setStartTime(long startTime);
	
	/**
	 * 获取当前占领司令的ID
	 * @return
	 */
	public String getCommanderPlayerId();
	
	/**
	 * 获取当前占领司令的guildId
	 * @return
	 */
	public String getCommanderGuildId();
	
	/**
	 * 生成单个超级武器信息builder
	 * @return
	 */
	public SuperWeaponInfo.Builder genSuperWeaponInfoBuilder();
	
	/**
	 * 广播超级武器单个信息
	 * @param player
	 */
	public void broadcastSingleSuperWeaponInfo(Player player);
	
	/**
	 * 获取当前占领的联盟
	 * @return
	 */
	public String getGuildId();
	
	/**
	 * 设置占领联盟
	 * @param guildId
	 */
	public void setGuildId(String guildId);
	
	/**
	 * 设置占领时间
	 * @param occupyTime
	 */
	public void setOccupyTime(long occupyTime);
	
	/**
	 * 检查是否已经报名
	 * @param guildId
	 * @return
	 */
	public boolean checkSignUp(String guildId);
	
	/**
	 * 检查是否已经自动报名
	 * @param guildId
	 * @return
	 */
	public boolean checkAutoSignUp(String guildId);
	
	/**
	 * 添加报名数据
	 * @param guildId
	 */
	public void addSignUp(String guildId);
	
	/**
	 * 移除报名数据
	 * @param guildId
	 */
	public void removeSignUp(String guildId);
	
	/**
	 * 删除报名数据
	 * @param guildId
	 */
	public void delSignUp(String guildId);
	
	/**
	 * 更换占领guildId
	 * @param guildId
	 */
	public void changeOccuption(String leaderName, String guildId);
	
	/**
	 * 援助超级武器处理
	 * @param massMarchList
	 */
	public void doSuperWeaponAssistance(IWorldMarch march);
	
	/**
	 * 超级武器战斗胜利处理
	 * @param march
	 */
	public void doSuperWeaponAttackWin(Player atkLeader, Player defLeader);
	
	/**
	 * 超级武器战斗胜利失败
	 * @param march
	 */
	public void doSuperWeaponAttackLose(Player atkLeader, Player defLeader);
	
	/**
	 * 是否可以攻击
	 * @param guildId
	 */
	public boolean canAttack(String guildId);
	
	/**
	 * 打印一下报名信息
	 */
	public void printSignUpLog();
	
	/**
	 * 发送即将开始邮件
	 */
	public void sendWillStartMail();
	
	/**
	 * 超级武器即将刷新
	 */
	public void sendWillRefreshMail();

	/**
	 * 是否占领过该超级武器
	 * @param guildId
	 * @return
	 */
	public boolean checkOccupyHistory(String guildId);

	/**
	 * 添加占领超级武器联盟
	 * @param guildId
	 */
	public void addOccupuHistory(String guildId);
	
	/**
	 * 删除占领超级武器联盟
	 * @param guildId
	 */
	public void delOccupuHistory(String guildId);

	/**
	 * 战争阶段到控制阶段数据处理
	 */
	public void doWarfareToControl();
	
	/**
	 * 控制阶段到和平阶段数据处理
	 */
	public void doControlToPace();
	
	/**
	 * 和平阶段到报名阶段数据处理
	 */
	public void doPaceToSignUp();
	
	
	/**
	 * 是否报名的联盟
	 * @return
	 */
	public boolean hasSignUpGuild();
	
	
	/**
	 * 无人获胜，直接进入和平期
	 * @param currTime
	 * @param constCfg
	 * @param status
	 */
	public void noCommanderOver(long currTime, SuperWeaponConstCfg constCfg, int status);
	
	/**
	 * 是否有npc
	 * @return
	 */
	public boolean hasNpc();
	
	/**
	 * 战胜npc
	 * @return
	 */
	public void fightNpcWin(String playerName, String guildId);
	
	/**
	 * 遣返行军
	 */
	public boolean repatriateMarch(Player player, String targetPlayerId);
	
	/**
	 * 任命队长
	 * @param player
	 * @param targetPlayerId
	 * @return
	 */
	public boolean cheangeQuarterLeader(Player player, String targetPlayerId);
}
