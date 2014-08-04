package game.AndJoy.monster.concrete;

import game.AndJoy.MainActivity;
import game.AndJoy.R;

import org.jbox2d.dynamics.World;

import ygame.domain.YDomain;
import ygame.domain.YDomainView;
import ygame.extension.primitives.YRectangle;
import ygame.extension.program.YTileProgram;
import ygame.framework.core.YGL_Configuration;
import ygame.framework.core.YRequest;
import ygame.framework.core.YScene;
import ygame.framework.core.YSystem;
import ygame.skeleton.YSkeleton;
import ygame.texture.YTexture;
import android.graphics.BitmapFactory;

public class MonsterDomain extends YDomain
{
	public final YRequest TO_ATTACK1;
	public final YRequest TO_WALK;
	public final YRequest TO_WAIT;
	public final YRequest TO_JUMP;
	private YSkeleton skeleton;
	private YTexture texture;
	
	

	public MonsterDomain(String KEY, World world, MainActivity activity)
	{
		super(KEY, new MonsterLogic(world, activity), new YDomainView(
				YTileProgram.getInstance(activity
						.getResources())));
		this.TO_WAIT = new YRequest(0);
		this.TO_WALK = new YRequest(1);
		this.TO_JUMP = new YRequest(2);
		this.TO_ATTACK1 = new YRequest(3);
		
		skeleton = new YRectangle(20 * 5, 20 * 5, false,
				true);
		texture = new YTexture(
				BitmapFactory.decodeResource(activity.getResources(),
						R.drawable.ic_launcher));
		
	}
	
//	@Override
//	protected void onClockCycle(double dbElapseTime_s, YSystem system,
//			YScene sceneCurrent, YMatrix matrix4pv, YMatrix matrix4Projection,
//			YMatrix matrix4View) {
//		// TODO Auto-generated method stub
//		super.onClockCycle(dbElapseTime_s, system, sceneCurrent, matrix4pv,
//				matrix4Projection, matrix4View);
//		YTextureProgram.YAdapter adapter = (YTextureProgram.YAdapter) domainContext
//				.getParametersAdapter();
//		adapter.paramMatrixPV(matrix4pv).paramMover(mover)
//				.paramSkeleton(skeleton).paramTexture(texture);
//	}
	
	
	@Override
	protected boolean onReceiveRequest(YRequest request, YSystem system,
			YScene sceneCurrent) {
		// TODO Auto-generated method stub
		return super.onReceiveRequest(request, system, sceneCurrent);
	}
	
	@Override
	protected void onGL_Initialize(YSystem system,
			YGL_Configuration configurationGL, int iWidth, int iHeight) {
		// TODO Auto-generated method stub
		super.onGL_Initialize(system, configurationGL, iWidth, iHeight);
	}

}
