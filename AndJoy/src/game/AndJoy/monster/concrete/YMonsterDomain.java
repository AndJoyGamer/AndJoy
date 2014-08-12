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
	public final YRequest TO_DAMAGE;
	final YRequest TO_DEAD;

	public YMonsterDomain(String KEY, World world, MainActivity activity)
	{
		super(KEY, new YMonsterLogic(world, activity), new YDomainView(
				YTileProgram.getInstance(activity
						.getResources())));
		this.TO_WAIT = new YRequest(0);
		this.TO_WALK = new YRequest(1);
		this.TO_ATTACK1 = new YRequest(2);
		this.TO_DAMAGE = new YRequest(3);
		this.TO_DEAD = new YRequest(4);
	}

}
