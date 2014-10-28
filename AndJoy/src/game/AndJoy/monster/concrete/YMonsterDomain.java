package game.AndJoy.monster.concrete;

import game.AndJoy.MainActivity;

import org.jbox2d.dynamics.World;

import ygame.domain.YDomain;
import ygame.domain.YDomainView;
import ygame.extension.program.YTileProgram;
import ygame.extension.tiled.YBaseParsePlugin.YIDomainBuilder;
import ygame.extension.tiled.YDomainBuildInfo;
import ygame.framework.core.YABaseDomain;
import ygame.framework.core.YRequest;

public class YMonsterDomain extends YDomain
{
	public final YRequest TO_ATTACK1;
	public final YRequest TO_WALK;
	public final YRequest TO_WAIT;
	public final YRequest TO_DAMAGE;
	final YRequest TO_DEAD;

	protected YMonsterDomain(String KEY, String keyHP, World world,
			MainActivity activity, float fInitX_M, float fInitY_M,
			float z)
	{
		super(KEY, new YMonsterLogic(world, keyHP, activity, fInitX_M,
				fInitY_M, z), new YDomainView(
				YTileProgram.getInstance(activity
						.getResources())));
		this.TO_WAIT = new YRequest(0);
		TO_WAIT.setName("待机");
		this.TO_WALK = new YRequest(1);
		TO_WALK.setName("行走");
		this.TO_ATTACK1 = new YRequest(2);
		TO_ATTACK1.setName("攻1");
		this.TO_DAMAGE = new YRequest(3);
		TO_DAMAGE.setName("受伤");
		this.TO_DEAD = new YRequest(4);
		TO_DEAD.setName("死亡");
	}

	public static class YBuilder implements YIDomainBuilder
	{

		@Override
		public YABaseDomain build(YDomainBuildInfo info,
				Object[] extraParams)
		{
			String[] keys = info.key.split("\\:");
			final String domainKey = keys[0];
			final String hpKey = keys[1];
			return new YMonsterDomain(domainKey, hpKey,
					(World) extraParams[0],
					(MainActivity) extraParams[1], info.x,
					info.y, info.z);
		}
	}

}
