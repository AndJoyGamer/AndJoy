package game.AndJoy.common;

import ygame.domain.YDomain;
import ygame.domain.YDomainView;
import ygame.extension.program.YTextureWaveProgram;
import ygame.extension.program.YTextureWaveProgram.YTextureWaveAdapter;
import ygame.framebuffer.YFBODomain;
import ygame.framebuffer.YFBOScene;
import ygame.framework.core.YSystem;
import ygame.math.YMatrix;
import ygame.math.vector.Vector3;
import ygame.skeleton.YSkeleton;
import ygame.transformable.YMover;
import android.content.res.Resources;
import android.opengl.GLES20;

public class YSceneDomain extends YFBODomain
{

	public YSceneDomain(String KEY, Resources resources)
	{
		super(KEY, new YSceneLogic1(), new YDomainView(
				YTextureWaveProgram.getInstance(resources)));
	}

	@Override
	protected void onDraw(YSystem system)
	{
		GLES20.glDisable(GLES20.GL_CULL_FACE);
		super.onDraw(system);
		GLES20.glEnable(GLES20.GL_CULL_FACE);
	}

	private static class YSceneLogic1 extends YASceneLogic
	{

		public YSceneLogic1()
		{
			super(0.07f);
		}

		@Override
		protected boolean canToUnmount(YMover mover)
		{
			return mover.getZ() >= 200;
		}

		@Override
		protected boolean canToRun(YMover mover)
		{
			return mover.getZ() >= 0;
		}

		@Override
		protected void onReset(YMover mover)
		{
			mover.setShaft(new Vector3(1, 1, 1)).setAngle(0)
					.setZ(-600);
		}

		@Override
		protected void applyTransform(float fTime,
				float dbElapseTime_s, YDomain domainContext,
				YFBOScene scene, YMatrix matrix4pv,
				YMover mover, YSkeleton skeleton)
		{
			mover.setAngle((float) (mover.getAngle() + 360 * dbElapseTime_s))
					.setZ((float) (mover.getZ() + 300 * dbElapseTime_s));

			YTextureWaveAdapter adapter = (YTextureWaveAdapter) domainContext
					.getParametersAdapter();
			adapter.paramMatrixPV(matrix4pv).paramMover(mover)
					.paramSkeleton(skeleton)
					.paramTexture(scene.asTexture());
		}
	}

	private static class YSceneLogic extends YASceneLogic
	{

		public YSceneLogic()
		{
			super(0.07f);
		}

		@Override
		protected boolean canToUnmount(YMover mover)
		{
			return mover.getAngle() <= -90;
		}

		@Override
		protected boolean canToRun(YMover mover)
		{
			return mover.getAngle() <= 0;
		}

		@Override
		protected void onReset(YMover mover)
		{
			mover.setAngle(90);
		}

		@Override
		protected void applyTransform(float fTime,
				float dbElapseTime_s, YDomain domainContext,
				YFBOScene scene, YMatrix matrix4pv,
				YMover mover, YSkeleton skeleton)
		{
			mover.setAngle((float) (mover.getAngle() - 25 * fTime
					* dbElapseTime_s));

			YTextureWaveAdapter adapter = (YTextureWaveAdapter) domainContext
					.getParametersAdapter();
			adapter.paramMatrixPV(matrix4pv).paramMover(mover)
					.paramSkeleton(skeleton)
					.paramTexture(scene.asTexture());
		}
	}
}
