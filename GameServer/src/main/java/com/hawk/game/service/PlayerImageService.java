package com.hawk.game.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.task.HawkTaskManager;
import org.hawk.util.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hawk.game.config.BuffCfg;
import com.hawk.game.config.CustomKeyCfg;
import com.hawk.game.config.PlayerFrameCfg;
import com.hawk.game.config.PlayerImageCfg;
import com.hawk.game.data.PlayerImageData;
import com.hawk.game.entity.CustomDataEntity;
import com.hawk.game.entity.StatusDataEntity;
import com.hawk.game.global.GlobalData;
import com.hawk.game.msg.PlayerLockImageMsg.LockParam;
import com.hawk.game.msg.PlayerLockImageMsg.LockType;
import com.hawk.game.msg.PlayerImageFresh;
import com.hawk.game.msg.PlayerUnlockImageMsg;
import com.hawk.game.msg.PlayerUnlockImageMsg.PLAYERSTAT_PARAM;
import com.hawk.game.msg.PlayerUnlockImageMsg.UnlockType;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.Player.ImageDisPlayType;
import com.hawk.game.protocol.Player.ImageOrCircleProperties;
import com.hawk.game.protocol.Player.ImageSource;
import com.hawk.game.protocol.Player.ImageType;
import com.hawk.game.protocol.Player.ImageUseProperties;
import com.hawk.game.protocol.Player.LoginWay;
import com.hawk.game.protocol.Player.PlayerImageDisplayOption;
import com.hawk.game.protocol.Player.PlayerImageOrCircleInfo;
import com.hawk.sdk.SDKConst.UserType;
import com.hawk.serialize.string.SerializeHelper;

/***
 * 玩家头像业务类
 * @author yang.rao
 *
 */
public class PlayerImageService {
	
	static Logger logger = LoggerFactory.getLogger("Server");
	
	private static PlayerImageService service = new PlayerImageService();
	
	//玩家imageData列表
	private ConcurrentHashMap<String, PlayerImageData> imageData = new ConcurrentHashMap<>();
	
	private PlayerImageService() {}
	
	public static PlayerImageService getInstance() {
		return service;
	}
	
	/**
	 * 修改指挥官头像
	 * 
	 * @param player
	 * @param type
	 * @param id
	 * @return
	 */
	public int changeImageOrCircle(Player player, ImageType type, int id) {
		int result = useImageOrCircle(player, type, id);
		if(result == 1) {
			//使用成功
			changeImageOrCircleSuccess(player);
			return 0;
		} else if(result == -2) {
			int errorCode = -1;
			if(type == ImageType.IMAGE){
				errorCode = Status.Error.THIS_IMAGE_INUSE_VALUE;
			}else{
				errorCode = Status.Error.THIS_CIRCLE_INUSE_VALUE;
			}
			
			return errorCode;
			
		} else {
			int errorCode = -1;
			if(type == ImageType.IMAGE){
				errorCode = Status.Error.NOT_GET_THIS_IMAGE_VALUE;
			}else{
				errorCode = Status.Error.NOT_GET_THIS_IMAGE_CIRCLE_VALUE;
			}
			
			return errorCode;
		}
	}
	
	/**
	 * 成功变更头像或头像框
	 * @param player
	 */
	public void changeImageOrCircleSuccess(Player player) {
		buildLoginInfo(player, false);
		entityNotifyUpdate(player);
		player.getPush().syncPlayerInfo(); //同步pficon字段
		updatePlayerPficon(player);
	}
	
	public void buildLoginInfo(Player player, boolean login){
		PlayerImageData data = PlayerImageService.getInstance().getPlayerImageData(player);
		data.abandonDisplay();
		PlayerImageOrCircleInfo.Builder build = PlayerImageOrCircleInfo.newBuilder();
		//构建未获得的头像和头像框
		PlayerImageService.getInstance().buildNotGainImageAndCircle(build, data, getchannel(player));
		PlayerImageService.getInstance().buildEffectImageOrCirlce(build, player);
		data.buildLoginInfo(build);
		PlayerImageDisplayOption.Builder option = PlayerImageDisplayOption.newBuilder();
		option.setDisplayChatCircle(data.isShowChatCircle() ? ImageDisPlayType.VIEW : ImageDisPlayType.HIDDEN);
		option.setDisplayCircle(data.isShowImageCircle() ? ImageDisPlayType.VIEW : ImageDisPlayType.HIDDEN);
		option.setDisplayNobleIdentify(data.isShowNobleIdentify() ? ImageDisPlayType.VIEW : ImageDisPlayType.HIDDEN);
		option.setDisplayPlatformPrivilegeImageCircle(data.isShowPlatformPrivilegeImageCircle() ? ImageDisPlayType.VIEW : ImageDisPlayType.HIDDEN);
		build.setDisplayOption(option);
		String resourcePfIcon = PlayerImageService.getInstance().getResourcePfIcon(player);
		build.setImageResource(resourcePfIcon);
		build.setImageSmall(resourcePfIcon);
		player.sendProtocol(HawkProtocol.valueOf(HP.code.PUSH_PLAYER_IMAGE_LIST_INFO_VALUE, build));
	}
	
	private int getchannel(Player player){
		if(UserType.getByChannel(player.getChannel()) == UserType.QQ){
			return UserType.QQ;
		}else if(UserType.getByChannel(player.getChannel()) == UserType.WX){
			return UserType.WX;
		}else {
			return UserType.GUEST;
		}
	}
	
	private void updatePlayerPficon(Player player) {
		String pfIcon = PlayerImageService.getInstance().getPfIcon(player);
		GlobalData.getInstance().updateAccountRolePfIconInfo(player.getId(), pfIcon);
	}
	
	/***
	 * 玩家使用新的头像或者头像框
	 * @param player
	 * @param type
	 * @param id
	 * @return
	 */
	public int useImageOrCircle(Player player, ImageType type, int id){
		int result = use(type, id, player);
		if(result == 1){
			setPlayerImageData(player);
		}
		return result;
	} 

	
	public boolean unlockImageAndCircle(PlayerUnlockImageMsg msg, Player player){
		PlayerImageData data = getPlayerImageData(player);
		UnlockType type = msg.getUnlockType();
		Object param = msg.getUnlockParam();
		//QQ和微信的直接不处理
		if(type == UnlockType.PLAYERSTAT && (param == PLAYERSTAT_PARAM.QQ || param == PLAYERSTAT_PARAM.WEIXIN)){
			return false;
		}
		if(msg.getType() != null){
			if(msg.getType() == com.hawk.game.msg.PlayerUnlockImageMsg.ImageType.IMAGE){
				boolean result = addImageService(type, param, data);
				if(result){
					setPlayerImageData(player);
				}
				return result;
			}else if(msg.getType() == com.hawk.game.msg.PlayerUnlockImageMsg.ImageType.FRAME){
				boolean result = addCircleService(type, param, data);
				if(result){
					setPlayerImageData(player);
				}
				return result;
			}
		}else{
			boolean imageResult = addImageService(type, param, data);
			boolean circleResult = addCircleService(type, param, data);
			boolean result = (imageResult || circleResult);
			if(result){
				setPlayerImageData(player);
			}
			return result;
		}
		return false;
	}
	
	/**
	 * 状态变化给头像上锁
	 * @param 
	 * @param player
	 */
	public boolean lockImageAndCircle(Player player, LockType type, LockParam lockParam){
		boolean imageResult = lockService(player, type, lockParam, ImageType.IMAGE);
		boolean circleResult = lockService(player, type, lockParam, ImageType.CIRCLE);
		return (imageResult || circleResult);
	}

	private int use(ImageType type, int id, Player player){
		PlayerImageData data = getPlayerImageData(player);
		if(data.isIdInuse(type, id)){ //头像正在使用中
			return -2;
		}
		//判断是不是buff头像
		if(effectImageOrCircle(type, id, player)){
			data.useImageOrCircle(type, id);
			return 1;
		}
		if(!data.containId(type, id)){
			return -1; //没有拥有头像或者头像框
		}
		data.useImageOrCircle(type, id);
		return 1; //使用成功标识
	}
	
	private boolean effectImageOrCircle(ImageType type, int id, Player player){
		if(type == ImageType.IMAGE){
			PlayerImageCfg cfg = getimageCfg(id);
			if(cfg == null){
				return false;
			}
			if(cfg.getUnlockType() == UnlockType.EFFECT.getValue()){
				BuffCfg buffCfg = HawkConfigManager.getInstance().getConfigByKey(BuffCfg.class, cfg.getUnlockParam());
				if(buffCfg == null){
					return false;
				}
				int effectId = buffCfg.getEffect();
				StatusDataEntity entity = player.getData().getStatusById(effectId);
				if(entity != null && entity.getEndTime() > HawkTime.getMillisecond()){
					return true;
				}
			}
			return false;
		}else{
			PlayerFrameCfg cfg = getcircleCfg(id);
			if(cfg == null){
				return false;
			}
			if(cfg.getUnlockType() == UnlockType.EFFECT.getValue()){
				BuffCfg buffCfg = HawkConfigManager.getInstance().getConfigByKey(BuffCfg.class, cfg.getUnlockParam());
				if(buffCfg == null){
					return false;
				}
				int effectId = buffCfg.getEffect();
				StatusDataEntity entity = player.getData().getStatusById(effectId);
				if(entity != null && entity.getEndTime() > HawkTime.getMillisecond()){
					return true;
				}
			}
			return false;
		}
	}

	public void buildNotGainImageAndCircle(PlayerImageOrCircleInfo.Builder build, PlayerImageData data, int channel){
		ConfigIterator<PlayerImageCfg> imageIte = HawkConfigManager.getInstance().getConfigIterator(PlayerImageCfg.class);
		while(imageIte.hasNext()){
			PlayerImageCfg config = imageIte.next();
			if(!data.containId(ImageType.IMAGE, config.getId())){
				if(skipConfig(channel, config.getUnlockType(), config.getUnlockParam())){
					continue;
				}
				ImageOrCircleProperties.Builder pro = ImageOrCircleProperties.newBuilder();
				pro.setId(config.getId());
				pro.setType(ImageType.IMAGE);
				pro.setUseType(ImageUseProperties.IMAGE_NOTGAIN);
				build.addInfos(pro);
			}
		}
		
		ConfigIterator<PlayerFrameCfg> circleIte = HawkConfigManager.getInstance().getConfigIterator(PlayerFrameCfg.class);
		while(circleIte.hasNext()){
			PlayerFrameCfg config = circleIte.next();
			if(!data.containId(ImageType.CIRCLE, config.getId())){
				if(skipConfig(channel, config.getUnlockType(), config.getUnlockParam())){
					continue;
				}
				ImageOrCircleProperties.Builder pro = ImageOrCircleProperties.newBuilder();
				pro.setId(config.getId());
				pro.setType(ImageType.CIRCLE);
				pro.setUseType(ImageUseProperties.IMAGE_NOTGAIN);
				build.addInfos(pro);
			}
		}
	}
	
	/***
	 * 是否跳过不同渠道的配置
	 * @param channel
	 * @param unlockType
	 * @param unlockParam
	 * @return
	 */
	private boolean skipConfig(int channel, int unlockType, int unlockParam){
		if(channel == UserType.QQ){
			//微信的渠道头像都不要
			if (unlockType == UnlockType.PLAYERSTAT.getValue()
					&& unlockParam == PLAYERSTAT_PARAM.WEIXIN.getValue()) {
				return true;
			}
		}
		if(channel == UserType.WX){
			if (unlockType == UnlockType.PLAYERSTAT.getValue()
					&& (unlockParam == PLAYERSTAT_PARAM.QQ.getValue()
							|| unlockParam == PLAYERSTAT_PARAM.QQSVIP.getValue())) {
				return true;
			}
		}
		
		return false;
	}
	
	private boolean addImageService(UnlockType type, Object param, PlayerImageData data){
		List<Integer> idList = getImageId(type, param);
		boolean notify = false;
		for(int imageId : idList){
			if(type == UnlockType.EFFECT){
				if(data.addImage(imageId)){
					notify = true;
				}
			}else{
				if(data.addTempImage(imageId)){
					notify = true;
				}
			}
		}
		return notify;
	}
	
	private boolean addCircleService(UnlockType type, Object param, PlayerImageData data){
		List<Integer> idList = getCircleId(type, param);
		boolean notify = false;
		for(int circleId : idList){
			if(type == UnlockType.EFFECT){
				if(data.addCircle(circleId)){
					notify = true;
				}
			}else{
				if(data.addTempCircle(circleId)){
					notify = true;
				}
			}
		}		
		return notify;
	}
	
	/***
	 * 获取头像id
	 * @param unlocktype
	 * @param param
	 * @return
	 */
	private List<Integer> getImageId(UnlockType unlocktype, Object param){
		int unlock = unlocktype.getValue();
		int unlockParam = getUnlockParam(unlocktype, param);
		List<Integer> idList = new ArrayList<>();
		ConfigIterator<PlayerImageCfg> imageIte = HawkConfigManager.getInstance().getConfigIterator(PlayerImageCfg.class);
		while(imageIte.hasNext()){
			PlayerImageCfg cfg = imageIte.next();
			if(unlocktype == UnlockType.PLAYERSTAT){
				if(cfg.getUnlockType() == unlock && cfg.getUnlockParam() == unlockParam){
					idList.add(cfg.getId());
				}
			}else{
				if(cfg.getUnlockType() == unlock && cfg.getUnlockParam() <= unlockParam){
					idList.add(cfg.getId());
				}
			}
		}
		return idList;
	}
	
	/***
	 * 获取头像框id
	 * @param unlocktype
	 * @param param
	 * @return
	 */
	private List<Integer> getCircleId(UnlockType unlocktype, Object param){
		int unlock = unlocktype.getValue();
		int unlockParam = getUnlockParam(unlocktype, param);
		List<Integer> idList = new ArrayList<>();
		ConfigIterator<PlayerFrameCfg> circleIte = HawkConfigManager.getInstance().getConfigIterator(PlayerFrameCfg.class);
		while(circleIte.hasNext()){
			PlayerFrameCfg cfg = circleIte.next();
			if(unlocktype == UnlockType.PLAYERSTAT){
				if(cfg.getUnlockType() == unlock && cfg.getUnlockParam() == unlockParam){
					idList.add(cfg.getId());
				}
			}else{
				if(cfg.getUnlockType() == unlock && cfg.getUnlockParam() <= unlockParam){
					idList.add(cfg.getId());
				}
			}
		}
		return idList;
	}
	
	/***
	 * 获取解锁参数(以后有新增参数，这个地方添加就行了)
	 * @param unlocktype
	 * @param param
	 * @return
	 */
	private int getUnlockParam(UnlockType unlocktype, Object param){
		int unlockParam = -1;
		if(unlocktype == UnlockType.PLAYERSTAT){
			PLAYERSTAT_PARAM stat_param = (PLAYERSTAT_PARAM)param;
			unlockParam = stat_param.getValue();
		}else{
			unlockParam = (int)param;
		}
		return unlockParam;
	}

	
	private boolean lockService(Player player, LockType type, LockParam lockParam, ImageType imageType){
		boolean result = false;
		PlayerImageData data = getPlayerImageData(player);
		List<Integer> list = null;
		if(imageType == ImageType.IMAGE){
			list = getLockImageIds(type, lockParam);
		}else{
			list = getLockCircleIds(type, lockParam);
		}
		boolean inUse = false;
		for(Integer i : list){
			data.remove(imageType, i);
			if(data.isIdInuse(imageType, i)){
				inUse = true;
			}
			result = true;
		}
		//如果该头像为正在使用的头像，则改变玩家的头像为默认头像
		if(inUse){
			resetPlayerImageData(data, imageType);
			//刷新playerInfo
			player.getPush().syncPlayerInfo();
		}
		if(result){
			setPlayerImageData(player);
		}
		return result;
	}
	

	private List<Integer> getLockImageIds(LockType type, LockParam lockParam){
		int unlock = type.getValue();
		int unlockParam = lockParam.getValue();
		List<Integer> idList = new ArrayList<>();
		ConfigIterator<PlayerImageCfg> imageIte = HawkConfigManager.getInstance().getConfigIterator(PlayerImageCfg.class);
		while(imageIte.hasNext()){
			PlayerImageCfg cfg = imageIte.next();
			if(cfg.getUnlockType() == unlock && cfg.getUnlockParam() == unlockParam){
				idList.add(cfg.getId());
			}
		}
		return idList;
	}
	
	private List<Integer> getLockCircleIds(LockType type, LockParam lockParam){
		int unlock = type.getValue();
		int unlockParam = lockParam.getValue();
		List<Integer> idList = new ArrayList<>();
		ConfigIterator<PlayerFrameCfg> circleIte = HawkConfigManager.getInstance().getConfigIterator(PlayerFrameCfg.class);
		while(circleIte.hasNext()){
			PlayerFrameCfg cfg = circleIte.next();
			if(cfg.getUnlockType() == unlock && cfg.getUnlockParam() == unlockParam){
				idList.add(cfg.getId());
			}
		}
		return idList;
	}
	
	public String getResourcePfIcon(Player player){
		String pfIcon = player.getData().getIMPfIcon();
		return pfIcon == null ? "" : pfIcon;
	}
	
	public String getPfIcon(Player player){
		PlayerImageData data = getPlayerImageData(player);
		boolean isDefaultImageId = false;
		if(isDefineImage(data.getUseImageId())){
			isDefaultImageId = true;
		}
		StringBuilder sb = new StringBuilder();
		if(isDefaultImageId){
			String impFicon =  player.getData().getIMPfIcon();
			// 如果未取到平台头像,则返回系统头像
			if(HawkOSOperator.isEmptyString(impFicon)){
				sb.append(ImageSource.FROMGAME_VALUE)
				.append(SerializeHelper.ATTRIBUTE_SPLIT)
				.append(data.getUseImageId());
			}else{
				sb.append(ImageSource.FROMIM_VALUE)
				.append(SerializeHelper.ATTRIBUTE_SPLIT)
				.append(impFicon);
			}
		}else{
			sb.append(ImageSource.FROMGAME_VALUE)
			.append(SerializeHelper.ATTRIBUTE_SPLIT)
			.append(data.getUseImageId());
		}
		int defaultCircle = getSuperAuthorityCircle(player);
		sb.append(SerializeHelper.ATTRIBUTE_SPLIT)
		.append(defaultCircle);
		if(!data.isChangeCircle()){
			if(data.getUseCircleId() != defaultCircle){
				data.setUseCircleId(defaultCircle);
				HawkTaskManager.getInstance().postMsg(player.getXid(), new PlayerImageFresh());
			}
		}
		return sb.toString();
	}
	
	/***
	 * 获取玩家需要显示的vip等级
	 * @param player
	 * @return
	 */
	public int getShowVIPLevel(Player player){
		PlayerImageData data = getPlayerImageData(player);
		//贵族头像框优先级最高
		int vipLevel = player.getData().getPlayerEntity().getVipLevel();
		boolean changeCircle = data.isChangeCircle();
		if(!changeCircle && vipLevel > 0){
			//获取最高等级vip头像框返回
			int circleId = getVipCircleId(vipLevel);
			PlayerFrameCfg cfg = getcircleCfg(circleId);
			if(cfg != null){
				return cfg.getUnlockParam();
			}
		}
		int circleId = data.getUseCircleId(); //是否是对应等级的vip，如果不是就返回vip0
		PlayerFrameCfg cfg = getcircleCfg(circleId);
		if(cfg != null){
			if(cfg.getUnlockType() == UnlockType.VIPLEVEL.getValue()){
				return cfg.getUnlockParam();
			}
		}
		return 0;
	}
	
	public int getDefaultImage(){
		ConfigIterator<PlayerImageCfg> circleIte = HawkConfigManager.getInstance().getConfigIterator(PlayerImageCfg.class);
		while(circleIte.hasNext()){
			PlayerImageCfg cfg = circleIte.next();
			if(cfg.getDefine() == PlayerFrameCfg.define_frame && cfg.isSpecialType()){
				return cfg.getId();
			}
		}
		return -1;
	}
	
	public ImageUseProperties getImageUseProperties(PlayerImageData data, ImageType type ,int id){
		ImageUseProperties useType = null;
		int defaultId = -1;
		if(type == ImageType.IMAGE){
			defaultId = getDefaultImage();
		}else{
			defaultId = getDefaultImageFrame();
		}
		if(data.isIdInuse(type, id)){
			useType = ImageUseProperties.IMAGE_INUSE;
		}else if(defaultId == id){
			useType = ImageUseProperties.IMAGE_DEFAULT;
		}else{
			useType = ImageUseProperties.IMAGE_CANUSE;
		}
		return useType;
	}
	
	/***
	 * 获取一个默认的头像框
	 * @return
	 */
	private int getDefaultImageFrame(){
		ConfigIterator<PlayerFrameCfg> circleIte = HawkConfigManager.getInstance().getConfigIterator(PlayerFrameCfg.class);
		while(circleIte.hasNext()){
			PlayerFrameCfg cfg = circleIte.next();
			if(cfg.getDefine() == PlayerFrameCfg.define_frame){
				return cfg.getId();
			}
		}
		return -1;
	}
	
	public boolean isDefineImage(int id){
		ConfigIterator<PlayerImageCfg> imageIte = HawkConfigManager.getInstance().getConfigIterator(PlayerImageCfg.class);
		while(imageIte.hasNext()){
			PlayerImageCfg config = imageIte.next();
			if(config.getId() == id && config.isSpecialType()){
				return true;
			}
		}
		return false;
	}
	
	/***
	 * 构建一个默认的playerImageData
	 * @return
	 */
	public PlayerImageData initDefaultPlayerImageData(){
		PlayerImageData data = new PlayerImageData();
		data.setImageSource(ImageSource.FROMIM_VALUE);
		data.setUseImageId(getDefaultImage());
		data.setUseCircleId(getDefaultImageFrame());
		return data;
	}
	
	private void resetPlayerImageData(PlayerImageData data, ImageType type){
		if(type == ImageType.IMAGE){
			data.setImageSource(ImageSource.FROMIM_VALUE);
			data.setUseImageId(getDefaultImage());
			data.setUseCircleId(data.getUseCircleId());
		}else{
			data.setUseCircleId(getDefaultImageFrame());
		}
	}
	
	public HashSet<Integer> buildPlatformImageIds(Player player){
		HashSet<Integer> result = new HashSet<>();
		UnlockType type = UnlockType.PLAYERSTAT;
		if(UserType.getByChannel(player.getChannel()) == UserType.QQ && player.getLoginWay() == LoginWay.GAMECENTER_LOGIN){
			List<Integer> ids = getImageId(type, PLAYERSTAT_PARAM.QQ);
			if(ids != null && !ids.isEmpty()){
				for(Integer id : ids){
					result.add(id);
				}
			}
		}
		else if(UserType.getByChannel(player.getChannel()) == UserType.WX && player.getLoginWay() == LoginWay.GAMECENTER_LOGIN){
			List<Integer> ids = getImageId(type, PLAYERSTAT_PARAM.WEIXIN);
			if(ids != null && !ids.isEmpty()){
				for(Integer id : ids){
					result.add(id);
				}
			}
		}		
		return result;
	}
	
	public HashSet<Integer> buildPlatformCircleIds(Player player){
		HashSet<Integer> result = new HashSet<>();
		UnlockType type = UnlockType.PLAYERSTAT;
		if(UserType.getByChannel(player.getChannel()) == UserType.QQ && player.getLoginWay() == LoginWay.GAMECENTER_LOGIN){
			List<Integer> ids = getCircleId(type, PLAYERSTAT_PARAM.QQ);
			if(ids != null && !ids.isEmpty()){
				for(Integer id : ids){
					result.add(id);
				}
			}
		}
		else if(UserType.getByChannel(player.getChannel()) == UserType.WX && player.getLoginWay() == LoginWay.GAMECENTER_LOGIN){
			List<Integer> ids = getCircleId(type, PLAYERSTAT_PARAM.WEIXIN);
			if(ids != null && !ids.isEmpty()){
				for(Integer id : ids){
					result.add(id);
				}
			}
		}		
		return result;
	}
	
	public void buildPlayerLevelImageAndCircleIds(Player player){
		int level = player.getData().getPlayerBaseEntity().getLevel();
		PlayerUnlockImageMsg msg = new PlayerUnlockImageMsg(UnlockType.PLAYERLEVEL, level);
		unlockImageAndCircle(msg, player);
	}
	
	public void buildPlayerVipLevelImageAndCircleIds(Player player){
		int vipLevel = player.getData().getPlayerEntity().getVipLevel();
		if(vipLevel >= 1){
			PlayerUnlockImageMsg msg = new PlayerUnlockImageMsg(UnlockType.VIPLEVEL, vipLevel);
			unlockImageAndCircle(msg, player);
		}
	}
	
	public void buildEffectImageOrCirlce(PlayerImageOrCircleInfo.Builder build, Player player){
		PlayerImageData data = getPlayerImageData(player);
		List<Integer> imageList = new ArrayList<>();
		List<Integer> circleList = new ArrayList<>();
		ConfigIterator<PlayerImageCfg> imageIte = HawkConfigManager.getInstance().getConfigIterator(PlayerImageCfg.class);		
		while(imageIte.hasNext()){
			PlayerImageCfg config = imageIte.next();
			if(config.getUnlockType() == UnlockType.EFFECT.getValue()){
				BuffCfg buffCfg = HawkConfigManager.getInstance().getConfigByKey(BuffCfg.class, config.getUnlockParam());
				if(buffCfg == null){
					continue;
				}
				int effectId = buffCfg.getEffect();
				StatusDataEntity entity = player.getData().getStatusById(effectId);
				if(entity != null && entity.getEndTime() > HawkTime.getMillisecond()){
					ImageOrCircleProperties.Builder pro = ImageOrCircleProperties.newBuilder();
					pro.setId(config.getId());
					pro.setType(ImageType.IMAGE);
					if(data.getUseImageId() == buffCfg.getId()){
						pro.setUseType(ImageUseProperties.IMAGE_INUSE);
					}else{
						pro.setUseType(ImageUseProperties.IMAGE_CANUSE);
					}
					build.addInfos(pro);
					imageList.add(config.getId());
				}
			}
		}
		
		ConfigIterator<PlayerFrameCfg> frameIte = HawkConfigManager.getInstance().getConfigIterator(PlayerFrameCfg.class);
		while(frameIte.hasNext()){
			PlayerFrameCfg config = frameIte.next();
			if(config.getUnlockType() == UnlockType.EFFECT.getValue()){
				BuffCfg buffCfg = HawkConfigManager.getInstance().getConfigByKey(BuffCfg.class, config.getUnlockParam());
				if(buffCfg == null){
					continue;
				}
				int effectId = buffCfg.getEffect();
				StatusDataEntity entity = player.getData().getStatusById(effectId);
				if(entity != null && entity.getEndTime() > HawkTime.getMillisecond()){
					ImageOrCircleProperties.Builder pro = ImageOrCircleProperties.newBuilder();
					pro.setId(config.getId());
					pro.setType(ImageType.CIRCLE);
					if(data.getUseCircleId() == buffCfg.getId()){
						pro.setUseType(ImageUseProperties.IMAGE_INUSE);
					}else{
						pro.setUseType(ImageUseProperties.IMAGE_CANUSE);
					}
					build.addInfos(pro);
					circleList.add(config.getId());
				}
			}
		}
		
		checkInuseExpire(data, imageList, circleList);
	}
	
	/***
	 * 刷新列表的时候，检查头像是否失效
	 * @param data
	 * @param imageList
	 * @param circleList
	 * @return
	 */
	private void checkInuseExpire(PlayerImageData data, List<Integer> imageList, List<Integer> circleList){
		PlayerImageCfg imageCfg = getimageCfg(data.getUseImageId());
		if(imageCfg != null){
			if(imageCfg.getUnlockType() == UnlockType.EFFECT.getValue()){
				Integer id = data.getUseImageId();
				if(!imageList.contains(id)){
					resetPlayerImageData(data, ImageType.IMAGE);
				}
			}
		}else{
			logger.error("designer remove resource, so imageid:" + data.getUseImageId() + "can't find in the portrait_img.xml");
			resetPlayerImageData(data, ImageType.IMAGE);
		}
		
		PlayerFrameCfg frameCfg = getcircleCfg(data.getUseCircleId());
		if(frameCfg != null){
			if(frameCfg.getUnlockType() == UnlockType.EFFECT.getValue()){
				Integer id = data.getUseCircleId();
				if(!circleList.contains(id)){
					resetPlayerImageData(data, ImageType.CIRCLE);
				}
			}
		}else{
			logger.error("designer remove resource, so frameid:" + data.getUseCircleId() + "can't find in the portrait_frame.xml");
			resetPlayerImageData(data, ImageType.CIRCLE);
		}
	}
	
	private PlayerImageCfg getimageCfg(int id){
		ConfigIterator<PlayerImageCfg> ita = HawkConfigManager.getInstance().getConfigIterator(PlayerImageCfg.class);
		while(ita.hasNext()){
			PlayerImageCfg cfg  = ita.next();
			if(cfg.getId() == id){
				return cfg;
			}
		}
		return null;
	}
	
	private PlayerFrameCfg getcircleCfg(int id){
		ConfigIterator<PlayerFrameCfg> ita = HawkConfigManager.getInstance().getConfigIterator(PlayerFrameCfg.class);
		while(ita.hasNext()){
			PlayerFrameCfg cfg  = ita.next();
			if(cfg.getId() == id){
				return cfg;
			}
		}
		return null;
	}
	
	//构建列表
	public PlayerImageData getPlayerImageData(Player player){
		if(imageData.containsKey(player.getId())){
			return imageData.get(player.getId());
		}
		CustomKeyCfg cfg = HawkConfigManager.getInstance().getConfigByKey(CustomKeyCfg.class, PlayerImageData.id);
		if(cfg == null){
			throw new RuntimeException("can't find id = " + PlayerImageData.id + " config from Custom_keys.xml");
		}
		CustomDataEntity entity = player.getData().getCustomDataEntity(cfg.getKey());
		if(entity == null){
			PlayerImageData imageData = PlayerImageService.getInstance().initDefaultPlayerImageData();
			entity = player.getData().createCustomDataEntity(cfg.getKey(), 0, JsonUtils.Object2Json(imageData));
			addImageData(player, imageData);
			return imageData;
		}
		if(entity.getArg() != null){
			PlayerImageData imageData = JsonUtils.String2Object(entity.getArg(), PlayerImageData.class);
			imageData.abandonDisplay();
			addImageData(player, imageData);
			return imageData;
		}
		return null;
	}
	
	//序列化
	public void setPlayerImageData(Player player){
		PlayerImageData data = getPlayerImageData(player);
		if(data == null){
			return;
		}
		CustomKeyCfg cfg = HawkConfigManager.getInstance().getConfigByKey(CustomKeyCfg.class, PlayerImageData.id);
		if(cfg == null){
			throw new RuntimeException("can't find id = " + PlayerImageData.id + " config from Custom_keys.xml");
		}
		CustomDataEntity entity = player.getData().getCustomDataEntity(cfg.getKey());
		if(entity != null){
			entity.setArg(JsonUtils.Object2Json(data));
		}
	}
	
	public void addImageData(Player player, PlayerImageData data){
		imageData.put(player.getId(), data);
	}
	
	public void entityNotifyUpdate(Player player){
		CustomKeyCfg cfg = HawkConfigManager.getInstance().getConfigByKey(CustomKeyCfg.class, PlayerImageData.id);
		if(cfg == null){
			throw new RuntimeException("can't find id = " + PlayerImageData.id + " config from Custom_keys.xml");
		}
		PlayerImageData data = getPlayerImageData(player);
		if(data == null){
			return;
		}
		CustomDataEntity entity = player.getData().getCustomDataEntity(cfg.getKey());
		entity.setArg(JsonUtils.Object2Json(data));
		entity.notifyUpdate();
	}
	
	/***
	 * 获取一个最高权限的头像框
	 * 玩家使用的默认头像框才会调用此函数
	 * @param player
	 * @return
	 */
	public int getSuperAuthorityCircle(Player player){
		PlayerImageData data = getPlayerImageData(player);
		boolean changeCircle = data.isChangeCircle();
		if(data.getUseCircleId() != getDefaultImageFrame() && changeCircle){
			return data.getUseCircleId();
		}
		//贵族头像框优先级最高
		int vipLevel = player.getData().getPlayerEntity().getVipLevel();
		if(!changeCircle && vipLevel > 0){
			//获取最高等级vip头像框返回
			return getVipCircleId(vipLevel);
		}else if(!changeCircle){
			//优先显示svip
			List<Integer> list = null;
			if((list=getCircleId(UnlockType.PLAYERSTAT, PLAYERSTAT_PARAM.QQSVIP))!= null && list.size() > 0){
				int chose = data.containCircleIds(list);
				if(chose != -1){
					return chose;
				}
			}
			if(UserType.getByChannel(player.getChannel()) == UserType.QQ){
				List<Integer> ids = getCircleId(UnlockType.PLAYERSTAT, PLAYERSTAT_PARAM.QQ);
				if(ids != null && !ids.isEmpty()){
					int chose = data.containCircleIds(ids);
					if(chose != -1){
						return chose;
					}
				}
			}else if(UserType.getByChannel(player.getChannel()) == UserType.WX){
				List<Integer> ids = getCircleId(UnlockType.PLAYERSTAT, PLAYERSTAT_PARAM.WEIXIN);
				if(ids != null && !ids.isEmpty()){
					int chose = data.containCircleIds(ids);
					if(chose != -1){
						return chose;
					}
				}
			}
		}
		return getDefaultImageFrame();
	}
	
	private int getVipCircleId(int vipLevel){
		Map<Integer, Integer> map = new HashMap<Integer, Integer>();
		ConfigIterator<PlayerFrameCfg> circleIte = HawkConfigManager.getInstance().getConfigIterator(PlayerFrameCfg.class);
		while(circleIte.hasNext()){
			PlayerFrameCfg cfg = circleIte.next();
			if (cfg.getUnlockType() == UnlockType.VIPLEVEL.getValue() && cfg.getUnlockParam() <= vipLevel) {
				map.put(cfg.getUnlockParam(), cfg.getId());
			}
		}
		while(vipLevel > 0){
			if(map.containsKey(vipLevel)){
				return map.get(vipLevel);
			}
			vipLevel --;
		}
		return -1;
	}
}
