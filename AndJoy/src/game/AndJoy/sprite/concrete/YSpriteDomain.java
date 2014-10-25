package game.AndJoy.sprite.concrete;

import game.AndJoy.MainActivity;

import org.jbox2d.dynamics.World;

import ygame.common.YConstants.Orientation;
import ygame.domain.YDomain;
import ygame.domain.YDomainView;
import ygame.extension.program.YTileProgram;
import ygame.extension.tiled.YBaseParsePlugin.YIDomainBuilder;
import ygame.extension.tiled.YDomainBuildInfo;
import ygame.framework.core.YABaseDomain;
import ygame.framework.core.YRequest;

public class YSpriteDomain extends YDomain
{
	static final int TO_DAMAGE = 0;
	static final int TO_ATTACK1 = 1;
	static final int TO_WALK = 2;
	static final int TO_WAIT = 3;
	static final int TO_JUMP = 4;

	protected YSpriteDomain(String KEY, World world, MainActivity activity,
			float initX, float initY, float skeletonSideLen)
	{
		super(KEY, new YSpriteLogic(world, activity, initX, initY,
				skeletonSideLen), new YDomainView(
				YTileProgram.getInstance(activity
						.getResources())));
	}

	public void damage(Orientation attackFrom)
	{
		SpriteReq request = new SpriteReq(TO_DAMAGE);
		request.orientation = attackFrom;
		sendRequest(request);
	}

	public void attack()
	{
		SpriteReq request = new SpriteReq(TO_ATTACK1);
		sendRequest(request);
	}

	public void walk()
	{
		SpriteReq request = new SpriteReq(TO_WALK);
		sendRequest(request);
	}

	public void waiting()
	{
		SpriteReq request = new SpriteReq(TO_WAIT);
		sendRequest(request);
	}

	public void jump()
	{
		SpriteReq request = new SpriteReq(TO_JUMP);
		sendRequest(request);
	}

	static final class SpriteReq extends YRequest
	{
		Orientation orientation;

		public SpriteReq(int iKEY)
		{
			super(iKEY);
		}
	}

	public static class YBuilder implements YIDomainBuilder
	{

		@Override
		public YABaseDomain build(YDomainBuildInfo info,
				Object[] extraParams)
		{
			return new YSpriteDomain(info.key,
					(World) extraParams[0],
					(MainActivity) extraParams[1], info.x,
					info.y, info.width);
		}
	}

}
