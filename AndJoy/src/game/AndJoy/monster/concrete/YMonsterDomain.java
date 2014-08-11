package game.AndJoy.monster.concrete;

import game.AndJoy.MainActivity;

import org.jbox2d.dynamics.World;

import ygame.domain.YDomain;
import ygame.domain.YDomainView;
import ygame.extension.program.YTileProgram;
import ygame.framework.core.YRequest;

public class YMonsterDomain extends YDomain
{
	public final YRequest TO_ATTACK1;
	public final YRequest TO_WALK;
	public final YRequest TO_WAIT;

	public YMonsterDomain(String KEY, World world, MainActivity activity)
	{
		super(KEY, new YMonsterLogic(world, activity), new YDomainView(
				YTileProgram.getInstance(activity
						.getResources())));
		this.TO_WAIT = new YRequest(0);
		this.TO_WALK = new YRequest(1);
		this.TO_ATTACK1 = new YRequest(2);
	}

}
