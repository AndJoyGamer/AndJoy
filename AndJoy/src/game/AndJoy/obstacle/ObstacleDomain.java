package game.AndJoy.obstacle;

import org.jbox2d.dynamics.World;

import game.AndJoy.MainActivity;
import ygame.domain.YDomain;
import ygame.domain.YDomainView;
import ygame.extension.program.YTileProgram;

public class ObstacleDomain extends YDomain {

	public ObstacleDomain(String KEY, MainActivity activity , World world) {
		
		super(KEY, new ObstacleLogic(activity,world), new YDomainView(
				YTileProgram.getInstance(activity.getResources())));
		// TODO Auto-generated constructor stub
	}

}
