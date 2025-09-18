package com.hawk.robot.action.image;

import java.util.List;
import java.util.Random;import org.hawk.net.protocol.HawkProtocol;
import org.hawk.robot.HawkRobotAction;
import org.hawk.robot.HawkRobotEntity;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Player.ImageType;
import com.hawk.game.protocol.Player.UseImageOrCircle;
import com.hawk.robot.GameRobotEntity;

public class PlayerImageAction extends HawkRobotAction {

	@Override
	public void doAction(HawkRobotEntity entity) {
		doUse(entity);
	}

	private void doUse(HawkRobotEntity entity){
		GameRobotEntity robotData = (GameRobotEntity)entity;
		UseImageOrCircle.Builder build = UseImageOrCircle.newBuilder();
		Random random = new Random();
		int randType = random.nextInt(2);
		List<Integer> list = null;
		
		switch (randType) {
		case 1:
			list = robotData.getBasicData().getImageList();
			break;
		case 2:
			list = robotData.getBasicData().getCircleList();
			break;
		default:
			break;
		}

		if(list != null && !list.isEmpty()){
			Integer id = list.get(random.nextInt(list.size()));
			build.setId(id);
			if(randType == 1){
				build.setType(ImageType.IMAGE);
			}else{
				build.setType(ImageType.CIRCLE);
			}
		}

		HawkProtocol protocol = HawkProtocol.valueOf(HP.code.PLAYER_USE_NEW_IMAGE_OR_CIRCLE_VALUE, build);
		entity.sendProtocol(protocol);
	}
}
