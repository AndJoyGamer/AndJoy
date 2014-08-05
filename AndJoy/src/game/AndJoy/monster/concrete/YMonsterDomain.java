package game.AndJoy.monster.concrete;

import game.AndJoy.MainActivity;
import game.AndJoy.R;
import game.AndJoy.sprite.concrete.YSpriteDomain;

import org.jbox2d.dynamics.World;

import android.graphics.BitmapFactory;
import android.graphics.Rect;
import ygame.domain.YDomain;
import ygame.domain.YDomainView;
import ygame.extension.primitives.YRectangle;
import ygame.extension.program.YTextureProgram;
import ygame.extension.program.YTileProgram;
import ygame.framework.core.YGL_Configuration;
import ygame.framework.core.YRequest;
import ygame.framework.core.YScene;
import ygame.framework.core.YSystem;
import ygame.math.YMatrix;
import ygame.skeleton.YSkeleton;
import ygame.texture.YTexture;

public class YMonsterDomain extends YDomain
{
	public final YRequest TO_ATTACK1;
	public final YRequest TO_WALK;
	public final YRequest TO_WAIT;

	public YMonsterDomain(String KEY, World world, MainActivity activity, YSpriteDomain domainSprite)
	{
		super(KEY, new YMonsterLogic(world, activity , domainSprite), new YDomainView(
				YTileProgram.getInstance(activity
						.getResources())));
		this.TO_WAIT = new YRequest(0);
		this.TO_WALK = new YRequest(1);
		this.TO_ATTACK1 = new YRequest(2);
	}

}
