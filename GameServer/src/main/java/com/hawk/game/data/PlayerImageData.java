package com.hawk.game.data;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.annotation.JSONField;
import com.hawk.game.protocol.Player.ImageOrCircleProperties;
import com.hawk.game.protocol.Player.ImageSource;
import com.hawk.game.protocol.Player.ImageType;
import com.hawk.game.protocol.Player.PlayerImageOrCircleInfo;
import com.hawk.game.service.PlayerImageService;

/***
 * 头像实体数据
 * @author yang.rao
 *
 */
public class PlayerImageData {
	
	/** custom_keys.xml配置表对应的id字段 **/
	public static final String id = "playerImage";
	
	/** 是否显示聊天框 **/
	private boolean showChatCircle = false;
	
	/** 是否显示头像框 **/
	private boolean showImageCircle = false;
	
	/** 是否显示贵族标识 **/
	private boolean showNobleIdentify = false;
	
	/** 是否显示平台标识头像框 **/
	private boolean showPlatformPrivilegeImageCircle = false;
	
	/** 是否修改过头像框 **/
	private boolean changeCircle = false;
	
	/** 这三个字段，标识一个完整的pfIcon **/
	private int imageSource; //值对应Player.proto的ImageSource枚举
	
	private int useImageId;
	
	private int useCircleId;
	
	private ArrayList<Integer> imageIds; //可使用头像列表(永久性列表)
	
	private ArrayList<Integer> circleIds; //可使用头像框列表(永久性列表)
	
	@JSONField(serialize=false)
	private ArrayList<Integer> tempImageIds; //临时性列表，登录的时候初始化
	
	@JSONField(serialize=false)
	private ArrayList<Integer> tempCircleIds; //临时性列表，登录的时候初始化
	
	public PlayerImageData(){
		this.imageIds = new ArrayList<>();
		this.circleIds = new ArrayList<>();
		this.tempImageIds = new ArrayList<>();
		this.tempCircleIds = new ArrayList<>();
	}
	
	public void abandonDisplay(){
		showChatCircle = false;
		showImageCircle = false;
		showNobleIdentify = false;
		showPlatformPrivilegeImageCircle = false;
	}

	public boolean isShowChatCircle() {
		return showChatCircle;
	}

	public void setShowChatCircle(boolean showChatCircle) {
		this.showChatCircle = showChatCircle;
	}

	public boolean isShowImageCircle() {
		return showImageCircle;
	}

	public void setShowImageCircle(boolean showImageCircle) {
		this.showImageCircle = showImageCircle;
	}

	public boolean isShowNobleIdentify() {
		return showNobleIdentify;
	}

	public void setShowNobleIdentify(boolean showNobleIdentify) {
		this.showNobleIdentify = showNobleIdentify;
	}

	public boolean isShowPlatformPrivilegeImageCircle() {
		return showPlatformPrivilegeImageCircle;
	}

	public void setShowPlatformPrivilegeImageCircle(boolean showPlatformPrivilegeImageCircle) {
		this.showPlatformPrivilegeImageCircle = showPlatformPrivilegeImageCircle;
	}

	public int getImageSource() {
		return imageSource;
	}

	public void setImageSource(int imageSource) {
		this.imageSource = imageSource;
	}

	public int getUseImageId() {
		return useImageId;
	}

	public void setUseImageId(int useImageId) {
		this.useImageId = useImageId;
	}

	public int getUseCircleId() {
		return useCircleId;
	}

	public void setUseCircleId(int useCircleId) {
		this.useCircleId = useCircleId;
	}
	
	public boolean isChangeCircle() {
		return changeCircle;
	}

	public void setChangeCircle(boolean changeCircle) {
		this.changeCircle = changeCircle;
	}

	public ArrayList<Integer> getImageIds() {
		return imageIds;
	}

	public void setImageIds(ArrayList<Integer> imageIds) {
		this.imageIds = imageIds;
	}

	public ArrayList<Integer> getCircleIds() {
		return circleIds;
	}

	public void setCircleIds(ArrayList<Integer> circleIds) {
		this.circleIds = circleIds;
	}

	public ArrayList<Integer> getTempImageIds() {
		return tempImageIds;
	}

	public void setTempImageIds(ArrayList<Integer> tempImageIds) {
		this.tempImageIds = tempImageIds;
	}

	public ArrayList<Integer> getTempCircleIds() {
		return tempCircleIds;
	}

	public void setTempCircleIds(ArrayList<Integer> tempCircleIds) {
		this.tempCircleIds = tempCircleIds;
	}

	public boolean addImage(int imageId){
		if(!imageIds.contains(imageId)){
			imageIds.add(imageId);
			return true;
		}
		return false;
	}
	
	public boolean addTempImage(int imageId){
		if(!tempImageIds.contains(imageId)){
			tempImageIds.add(imageId);
			return true;
		}
		return false;
	}
	
	public boolean addCircle(int circleId){
		if(!circleIds.contains(circleId)){
			circleIds.add(circleId);
			return true;
		}
		return false;
	}
	
	public boolean addTempCircle(int circleId){
		if(!tempCircleIds.contains(circleId)){
			tempCircleIds.add(circleId);
			return true;
		}
		return false;
	}
	
	public void remove(ImageType type, Integer id){
		if(type == ImageType.IMAGE){
			removeImage(id);
		}else if(type == ImageType.CIRCLE){
			removeCircle(id);
		}
	}
	
	public void removeImage(Integer imageId){
		if(imageIds.contains(imageId)){
			imageIds.remove(imageId);
		}
		if(tempImageIds.contains(imageId)){
			tempImageIds.remove(imageId);
		}
	}
	
	public void removeCircle(Integer circleId){
		if(circleIds.contains(circleId)){
			circleIds.remove(circleId);
		}
		if(tempCircleIds.contains(circleId)){
			tempCircleIds.remove(circleId);
		}
	}
	
	public void useImageOrCircle(ImageType type, int id){
		if(type == ImageType.IMAGE){
			useImageId = id;
			setImageSource(type, id);
			return;
		}else if(type == ImageType.CIRCLE){
			useCircleId = id;
			changeCircle = true;
			return;
		}
	}
	
	private void setImageSource(ImageType type, int id){
		if(type == ImageType.CIRCLE){
			return;
		}
		if(PlayerImageService.getInstance().isDefineImage(id)){
			imageSource = ImageSource.FROMIM_VALUE;
		}else{
			imageSource = ImageSource.FROMGAME_VALUE;
		}
	}
	
	/***
	 * 判断头像或者头像框是否正在使用
	 * @param type 类型
	 * @param id 
	 * @return
	 */
	public boolean isIdInuse(ImageType type, int id){
		if(type == ImageType.IMAGE){
			if(id == useImageId){
				return true;
			}
		}else if(type == ImageType.CIRCLE){
			if(id == useCircleId){
				return true;
			}
		}
		
		return false;
	}
	
	/***
	 * 是否拥有此头像
	 * @param type
	 * @param id
	 * @return
	 */
	public boolean containId(ImageType type, int id){
		if(type == ImageType.IMAGE){
			if(imageIds.contains(id)){
				return true;
			}
			if(tempImageIds.contains(id)){
				return true;
			}
		}else if(type == ImageType.CIRCLE){
			if(circleIds.contains(id)){
				return true;
			}
			if(tempCircleIds.contains(id)){
				return true;
			}
		}
		return false;
	}
	
	public void buildLoginInfo(PlayerImageOrCircleInfo.Builder build){
		ArrayList<Integer> imageList = new ArrayList<>();
		imageList.addAll(imageIds);
		imageList.addAll(tempImageIds);
		ArrayList<Integer> circleList = new ArrayList<>();
		circleList.addAll(circleIds);
		circleList.addAll(tempCircleIds);
		
		for(int id : imageList){
			ImageOrCircleProperties.Builder pro = ImageOrCircleProperties.newBuilder();
			pro.setId(id);
			pro.setType(ImageType.IMAGE);
			pro.setUseType(PlayerImageService.getInstance().getImageUseProperties(this, ImageType.IMAGE ,id));
			build.addInfos(pro);
		}
		for(int id : circleList){
			ImageOrCircleProperties.Builder pro = ImageOrCircleProperties.newBuilder();
			pro.setId(id);
			pro.setType(ImageType.CIRCLE);
			pro.setUseType(PlayerImageService.getInstance().getImageUseProperties(this, ImageType.CIRCLE ,id));
			build.addInfos(pro);
		}
	}
	
	/***
	 * 返回一个可用的头像框id
	 * @param ids
	 * @return
	 */
	public int containCircleIds(List<Integer> ids){
		for(Integer id : ids){
			if(circleIds.contains(id)){
				return id;
			}
			if(tempCircleIds.contains(id)){
				return id;
			}
		}
		return -1;
	}
	
	public void onPlayerLoginClearTempList(){
		tempImageIds.clear();
		tempCircleIds.clear();
	}
}
