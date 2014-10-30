package game.AndJoy.obstacle;

import org.jbox2d.dynamics.World;

import game.AndJoy.MainActivity;
import game.AndJoy.monster.concrete.YMonsterDomain;
import ygame.domain.YDomain;
import ygame.domain.YDomainView;
import ygame.extension.program.YTileProgram;
import ygame.extension.tiled.YBaseParsePlugin.YIDomainBuilder;
import ygame.extension.tiled.YDomainBuildInfo;
import ygame.framework.core.YABaseDomain;

public class ObstacleDomain extends YDomain {

	public ObstacleDomain(String KEY, MainActivity activity, World world,
			float x, float y, float z) {

		super(KEY, new ObstacleLogic(activity, world, x, y, z),
				new YDomainView(YTileProgram.getInstance(activity
						.getResources())));
		// TODO Auto-generated constructor stub
	}

	public static class YBuilder implements YIDomainBuilder {

		@Override
		public YABaseDomain build(YDomainBuildInfo info, Object[] extraParams) {
			String[] keys = info.key.split("\\:");
			final String domainKey = keys[0];
			return new ObstacleDomain(domainKey, (MainActivity) extraParams[1],
					(World) extraParams[0], info.x, info.y, info.z);
		}

	}
}
