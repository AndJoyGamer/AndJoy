package game.AndJoy.common;

import ygame.domain.YADomainLogic;
import ygame.domain.YDomain;
import ygame.framebuffer.YFBOScene;
import ygame.framework.YIResultCallback;
import ygame.framework.core.YRequest;
import ygame.framework.core.YScene;
import ygame.framework.core.YSystem;
import ygame.framework.domain.YBaseDomain;
import ygame.framework.domain.YWriteBundle;
import ygame.math.YMatrix;
import ygame.skeleton.YSkeleton;
import ygame.transformable.YMover;

public abstract class YASceneLogic extends YADomainLogic
{

	private YMover mover = new YMover();
	private YSkeleton skeleton;

	/** 0.07f */
	final private float fRadio;
	private boolean bRequestQuit;
	private boolean bRequestRun;
	private float fTime;

	public YASceneLogic(float fRadio)
	{
		this.fRadio = fRadio;
	}

	public YASceneLogic()
	{
		this.fRadio = 0.07f;
	}

	@Override
	protected void onAttach(YSystem system, YBaseDomain domainContext)
	{
		super.onAttach(system, domainContext);
		skeleton = new YMirrorXRectangle(system.YVIEW.getWidth()
				* fRadio, system.YVIEW.getHeight() * fRadio,
				false, true);
		reset();
	}

	@Override
	protected void onCycle(double dbElapseTime_s, YDomain domainContext,
			YWriteBundle bundle, YSystem system,
			YScene sceneCurrent, YMatrix matrix4pv,
			YMatrix matrix4Projection, YMatrix matrix4View)
	{
		dbElapseTime_s = dbElapseTime_s > 0.05 ? 0.05 : dbElapseTime_s;
		fTime += dbElapseTime_s;

		YFBOScene scene = (YFBOScene) sceneCurrent;
		applyTransform(fTime, (float) dbElapseTime_s, domainContext,
				scene, matrix4pv, mover, skeleton);

		if (canToRun(mover) && !bRequestRun)
		{
			scene.requestRun(null);
			bRequestRun = true;
		}

		if (canToUnmount(mover) && !bRequestQuit)
		{
			scene.requestUnmount(new YIResultCallback()
			{

				@Override
				public void onResultReceived(Object objResult)
				{
					if ((Boolean) objResult)
						reset();
				}
			});
			bRequestQuit = true;
		}
	}

	protected abstract boolean canToUnmount(YMover mover);

	protected abstract boolean canToRun(YMover mover);

	protected abstract void onReset(YMover mover);

	protected abstract void applyTransform(float fTime,
			float dbElapseTime_s, YDomain domainContext,
			YFBOScene scene, YMatrix matrix4pv, YMover mover,
			YSkeleton skeleton);

	@Override
	protected boolean onDealRequest(YRequest request, YSystem system,
			YScene sceneCurrent)
	{
		return false;
	}

	private void reset()
	{
		bRequestQuit = false;
		bRequestRun = false;
		fTime = 0;
		onReset(mover);
	}

}
