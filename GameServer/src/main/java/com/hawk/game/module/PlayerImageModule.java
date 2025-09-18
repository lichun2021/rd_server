package com.hawk.game.module;

import java.security.InvalidParameterException;
import java.util.HashSet;
import org.hawk.annotation.MessageHandler;
import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hawk.common.IDIPBanInfo;
import com.hawk.game.data.PlayerImageData;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.msg.PlayerImageFresh;
import com.hawk.game.msg.PlayerLockImageMsg;
import com.hawk.game.msg.PlayerLockImageMsg.LockParam;
import com.hawk.game.msg.PlayerLockImageMsg.LockType;
import com.hawk.game.msg.PlayerUnlockImageMsg;
import com.hawk.game.msg.PlayerUnlockImageMsg.PLAYERSTAT_PARAM;
import com.hawk.game.msg.PlayerUnlockImageMsg.UnlockType;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerModule;
import com.hawk.game.president.PresidentFightService;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.IDIP.NoticeMode;
import com.hawk.game.protocol.IDIP.NoticeType;
import com.hawk.game.protocol.Player.ImageDisPlayType;
import com.hawk.game.protocol.Player.ImageType;
import com.hawk.game.protocol.Player.PlayerImageDisplayOption;
import com.hawk.game.protocol.Player.UseImageOrCircle;
import com.hawk.game.protocol.Player.reqOtherImage;
import com.hawk.game.protocol.Player.resOtherResourceImage;
import com.hawk.game.protocol.Status.IdipMsgCode;
import com.hawk.game.protocol.Status.SysError;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.PlayerImageService;
import com.hawk.game.superweapon.SuperWeaponService;
import com.hawk.game.util.GsConst.BanPlayerOperType;
import com.hawk.game.util.GsConst.GlobalControlType;
import com.hawk.game.util.GsConst.IDIPBanType;

/***
 * 玩家平台模块(只针对平台头像新需求)
 * @author yang.rao
 *
 */
public class PlayerImageModule extends PlayerModule {

	static final Logger logger = LoggerFactory.getLogger("Server");
	
	public PlayerImageModule(Player player) {
		super(player);
	}

	@Override
	protected boolean onPlayerLogin() {
		PlayerImageService.getInstance().buildLoginInfo(player, true); //同步数据
		return super.onPlayerLogin();
	}
	
	
	
	@Override
	protected boolean onPlayerAssemble() {
		onInitEntity();
		// updatePlayerPficon();  // 更新AccountRoleInfo的逻辑放到PlayerIdleModle的onPlayerAssemble方法一块做，减少redis的访问次数
		return super.onPlayerAssemble();
	}
	
	private void onInitEntity(){
		PlayerImageData imageData =  PlayerImageService.getInstance().getPlayerImageData(player);
		imageData.onPlayerLoginClearTempList();
		//构建平台头像
		HashSet<Integer> imageIds = PlayerImageService.getInstance().buildPlatformImageIds(player);
		if(imageIds != null){
			for(Integer id : imageIds){
				imageData.addTempImage(id);
			}
		}
		
		//构建平台头像框
		HashSet<Integer> circleIds = PlayerImageService.getInstance().buildPlatformCircleIds(player);
		if(circleIds != null){
			for(Integer id : circleIds){
				imageData.addTempCircle(id);
			}
		}
		
		PlayerImageService.getInstance().buildPlayerLevelImageAndCircleIds(player); //等级列表
		PlayerImageService.getInstance().buildPlayerVipLevelImageAndCircleIds(player); //vip等级列表
		
		//解锁盟主的
		if(GuildService.getInstance().isGuildLeader(player.getId())){
			PlayerUnlockImageMsg msg = PlayerUnlockImageMsg.valueOf(UnlockType.PLAYERSTAT, PLAYERSTAT_PARAM.MENGZHU);
			PlayerImageService.getInstance().unlockImageAndCircle(msg, player);
		}
		//解锁大总统的
		if(PresidentFightService.getInstance().isPresidentPlayer(player.getId())){
			PlayerUnlockImageMsg msg = PlayerUnlockImageMsg.valueOf(UnlockType.PLAYERSTAT, PLAYERSTAT_PARAM.HUANGDI);
			PlayerImageService.getInstance().unlockImageAndCircle(msg, player);
		}
		//解锁战区司令的
		if(SuperWeaponService.getInstance().isSuperWeaponPriesdent(player.getId())){
			PlayerUnlockImageMsg msg = PlayerUnlockImageMsg.valueOf(UnlockType.PLAYERSTAT, PLAYERSTAT_PARAM.ZHANQUSILING);
			PlayerImageService.getInstance().unlockImageAndCircle(msg, player);
		}
	}
	
	/***
	 * 使用头像或者头像框
	 * @param protocol
	 */
	@ProtocolHandler(code=HP.code.PLAYER_USE_NEW_IMAGE_OR_CIRCLE_VALUE)
	private void playerUseImageOrCircle(HawkProtocol protocol){
		UseImageOrCircle req = protocol.parseProtocol(UseImageOrCircle.getDefaultInstance());
		ImageType type = req.getType(); //类型
		if (type == ImageType.IMAGE) {
			if (GlobalData.getInstance().isGlobalBan(GlobalControlType.CHANGE_ICON)) {
				String reason = GlobalData.getInstance().getGlobalBanReason(GlobalControlType.CHANGE_ICON);
				if (HawkOSOperator.isEmptyString(reason)) {
					sendError(protocol.getType(), SysError.GLOBAL_BAN_CHANGE_ICON);
				} else {
					player.sendIdipNotice(NoticeType.BAN_MSG, NoticeMode.NOTICE_MSG, 0, reason);
				}
				return;
			}
			
			IDIPBanInfo banInfo = RedisProxy.getInstance().getIDIPBanInfo(player.getId(), IDIPBanType.BAN_CHANGE_IMAGE);
			if (banInfo != null && banInfo.getEndTime() >  HawkTime.getMillisecond()) {
				player.sendIdipNotice(NoticeType.SEND_MSG, NoticeMode.NOTICE_MSG, 0, banInfo.getBanMsg());
				return;
			}
			
			long endTime = RedisProxy.getInstance().getPlayerBanEndTime(player.getId(), BanPlayerOperType.BAN_CHANGE_IMAGE);
			if (endTime > HawkTime.getMillisecond()) {
				sendError(protocol.getType(), IdipMsgCode.IDIP_BAN_CHANGE_ICON);
				return;
			}
		}
		
		int id = req.getId(); //id 
		//需要去校验类型和id
		int result = PlayerImageService.getInstance().changeImageOrCircle(player, type, id);
		if (result != 0) {
			sendError(HP.code.PLAYER_USE_NEW_IMAGE_OR_CIRCLE_VALUE, result);
		}
	}
	
	/****
	 * 玩家显示设置
	 * @param protocol
	 */
	@ProtocolHandler(code=HP.code.PLAYER_IMAGE_DISPLAY_OPRATATION_VALUE)
	private void playerSetImageOrCircleDisplay(HawkProtocol protocol){
		PlayerImageDisplayOption req = protocol.parseProtocol(PlayerImageDisplayOption.getDefaultInstance());
		ImageDisPlayType displayChatCircle = req.getDisplayChatCircle(); //显示聊天框
		ImageDisPlayType displayCircle     = req.getDisplayCircle(); //显示头像框
		ImageDisPlayType displayNoble      = req.getDisplayNobleIdentify(); //显示贵族标识
		ImageDisPlayType displayPlatform   = req.getDisplayPlatformPrivilegeImageCircle(); //显示平台特权头像框
		boolean disPlayChatCircle = displayChatCircle == ImageDisPlayType.VIEW ? true : false;
		boolean disPlayCircle = displayCircle == ImageDisPlayType.VIEW ? true : false;
		boolean disPlayNoble = displayNoble == ImageDisPlayType.VIEW ? true : false;
		boolean disPlayPlatform = displayPlatform == ImageDisPlayType.VIEW ? true : false;
		//PlayerImageEntity entity = player.getData().getPlayerImageEntity();
		PlayerImageData data = PlayerImageService.getInstance().getPlayerImageData(player);
		
		if(disPlayChatCircle != data.isShowChatCircle() || disPlayCircle != data.isShowImageCircle()
				|| disPlayNoble != data.isShowNobleIdentify() || disPlayPlatform != data.isShowPlatformPrivilegeImageCircle()){
			data.setShowChatCircle(disPlayChatCircle);
			data.setShowImageCircle(disPlayCircle);
			data.setShowNobleIdentify(disPlayNoble);
			data.setShowPlatformPrivilegeImageCircle(disPlayPlatform);
			PlayerImageService.getInstance().setPlayerImageData(player);
			PlayerImageService.getInstance().entityNotifyUpdate(player);
			PlayerImageDisplayOption.Builder result = PlayerImageDisplayOption.newBuilder();
			result.setDisplayChatCircle(displayChatCircle);
			result.setDisplayCircle(displayCircle);
			result.setDisplayNobleIdentify(displayNoble);
			result.setDisplayPlatformPrivilegeImageCircle(displayPlatform);
			sendProtocol(HawkProtocol.valueOf(HP.code.PLAYER_IMAGE_DISPLAY_OPRATATION_VALUE, result));
			player.getPush().syncPlayerInfo(); //同步pficon字段
		}
	}
	
	@ProtocolHandler(code=HP.code.PLAYER_REQ_OTHER_RESOURCE_IMAGE_INFO_VALUE)
	private void reqOtherPlayerResourceImage(HawkProtocol protocol){
		reqOtherImage req = protocol.parseProtocol(reqOtherImage.getDefaultInstance());
		String playerId = req.getPlayerId();
		Player p = GlobalData.getInstance().makesurePlayer(playerId);
		if(p == null){
			return;
		}
		String resourcePficon = PlayerImageService.getInstance().getResourcePfIcon(p);
		resOtherResourceImage.Builder build = resOtherResourceImage.newBuilder();
		build.setResource(resourcePficon);
		sendProtocol(HawkProtocol.valueOf(HP.code.PLAYER_REQ_OTHER_RESOURCE_IMAGE_INFO_VALUE, build));
	}
	
	/***
	 * 头像解锁消息
	 * @param msg
	 */
	@MessageHandler
	private void unlockImageOrCircle(PlayerUnlockImageMsg msg){
		if(!checkUnlockMsg(msg)){
			throw new InvalidParameterException("unlock image params error :" + msg.getUnlockType() + ",param :" + msg.getUnlockParam());
		}
		if(PlayerImageService.getInstance().unlockImageAndCircle(msg, player)){
			PlayerImageService.getInstance().entityNotifyUpdate(player);
			PlayerImageService.getInstance().buildLoginInfo(player, false);
			player.getPush().syncPlayerInfo();
		}
	}
	
	private boolean checkUnlockMsg(PlayerUnlockImageMsg msg){
		if(msg.getUnlockType() == UnlockType.PLAYERSTAT){
			if(!(msg.getUnlockParam() instanceof PLAYERSTAT_PARAM)){
				return false;
			}
		}
		return true;
	}
	
	/***
	 * 头像加锁消息(某些状态改变，会导致拥有的某些头像消失)
	 * @param msg
	 */
	@MessageHandler
	private void lockImageOrCircle(PlayerLockImageMsg msg){
		LockType type = msg.getLockType();
		LockParam lockParam = msg.getLockParam();
		boolean lock = PlayerImageService.getInstance().lockImageAndCircle(player, type, lockParam);
		if(lock){
			PlayerImageService.getInstance().buildLoginInfo(player, false);
			PlayerImageService.getInstance().entityNotifyUpdate(player);
		}
	}
	
	@MessageHandler
	private void freshImageOrCircle(PlayerImageFresh msg){
		PlayerImageService.getInstance().buildLoginInfo(player, false);
		player.getPush().syncPlayerInfo();
	}
	
}
