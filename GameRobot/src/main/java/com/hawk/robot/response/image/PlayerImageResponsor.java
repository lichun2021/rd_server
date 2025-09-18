package com.hawk.robot.response.image;

import java.util.ArrayList;
import java.util.List;

import org.hawk.net.protocol.HawkProtocol;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Player.ImageOrCircleProperties;
import com.hawk.game.protocol.Player.ImageType;
import com.hawk.game.protocol.Player.ImageUseProperties;
import com.hawk.game.protocol.Player.PlayerImageOrCircleInfo;
import com.hawk.robot.GameRobotEntity;
import com.hawk.robot.annotation.RobotResponse;
import com.hawk.robot.response.RobotResponsor;

@RobotResponse(code=HP.code.PUSH_PLAYER_IMAGE_LIST_INFO_VALUE)
public class PlayerImageResponsor extends RobotResponsor {

	@Override
	public void response(GameRobotEntity robotEntity, HawkProtocol protocol) {
		PlayerImageOrCircleInfo res = protocol.parseProtocol(PlayerImageOrCircleInfo.getDefaultInstance());
		List<ImageOrCircleProperties> infos = res.getInfosList();
		List<Integer> imageList = new ArrayList<Integer>();
		List<Integer> circleList = new ArrayList<Integer>();
		for(ImageOrCircleProperties pro : infos){
			if(pro.getType() == ImageType.IMAGE && pro.getUseType() == ImageUseProperties.IMAGE_CANUSE){
				imageList.add(pro.getId());
			}else if(pro.getType() == ImageType.CIRCLE && pro.getUseType() == ImageUseProperties.IMAGE_CANUSE){
				circleList.add(pro.getId());
			}
		}
		robotEntity.getBasicData().setImageList(imageList);
		robotEntity.getBasicData().setCircleList(circleList);
	}

}
