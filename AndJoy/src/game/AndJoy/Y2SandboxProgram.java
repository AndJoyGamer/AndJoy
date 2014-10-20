package game.AndJoy;

import ygame.domain.YABaseShaderProgram;
import ygame.domain.YDomainView;
import ygame.framework.core.YSystem;
import ygame.framework.domain.YReadBundle;
import ygame.framework.domain.YWriteBundle;
import ygame.math.YMatrix;
import ygame.program.YAShaderProgram;
import ygame.program.YAttributeValue;
import ygame.skeleton.YSkeleton;
import ygame.transformable.YMover;
import ygame.utils.YTextFileUtils;
import android.content.res.Resources;

public class Y2SandboxProgram extends YAShaderProgram
{
	private static final int SKE = 0;
	private static final int PVM = 1;

	private YAttributeValue aPosition;
	private YAttributeValue aColor;
	private YAttributeValue aNormal;
	private YAttributeValue aTexCoord;
	private float time;

	Y2SandboxProgram(Resources resources)
	{
		fillCodeAndParam(YTextFileUtils.getStringFromAssets("mo.vsh",
				resources), YTextFileUtils.getStringFromAssets(
				"mo.fsh", resources), Y2SandboxAdapter.class);
	}

	@Override
	protected void onInitialize(int iProgramHandle)
	{
		super.onInitialize(iProgramHandle);
		aPosition = getAttribute("aPosition");
		aColor = getAttribute("aColor");
		aNormal = getAttribute("aNormal");
		aTexCoord = getAttribute("aTexCoord");
	}

	@Override
	protected void applyParams(int iProgramHandle, YReadBundle bundle,
			YSystem system, YDomainView domainView)
	{
		YSkeleton skeleton = (YSkeleton) bundle.readObject(SKE);
		bindAttribute(aPosition, skeleton.getPositionDataSource());
		if (null != aColor)
			bindAttribute(aColor, skeleton.getColorDataSource());
		if (null != aNormal)
			bindAttribute(aNormal, skeleton.getNormalDataSource());
		if (null != aTexCoord)
			bindAttribute(aTexCoord,
					skeleton.getTexCoordDataSource());

		setUniformMatrix("uPVMMatrix", bundle.readFloatArray(PVM));
		setUniformf("time", (time += .5f) % 256);

		if (skeleton.hasIBO())
			drawWithIBO(skeleton.getIndexHandle(),
					skeleton.getVertexNum(),
					domainView.iDrawMode);
		else
			drawWithVBO(skeleton.getVertexNum(),
					domainView.iDrawMode);
	}

	/**
	 * <b>参数适配器</b>
	 * 
	 * <p>
	 * <b>概述</b>： TODO
	 * 
	 * <p>
	 * <b>建议</b>： TODO
	 * 
	 * <p>
	 * <b>详细</b>： TODO
	 * 
	 * <p>
	 * <b>注</b>：您每次使用该适配器时，应该全部填写其前缀名为<b>param</b >的方法
	 * 
	 * <p>
	 * <b>例</b>：TODO
	 * 
	 * @author yunzhong
	 * 
	 */
	public static class Y2SandboxAdapter extends
			YABaseShaderProgram.YABaseParametersAdapter
	{
		private YMover mover;
		private YMatrix matrixPV;
		private YSkeleton skeleton;

		private YMatrix matrixPVM = new YMatrix();

		/**
		 * @param mover
		 *                移动子
		 * @return 参数适配器
		 */
		public Y2SandboxAdapter paramMover(YMover mover)
		{
			this.mover = mover;
			return this;
		}

		/**
		 * @param matrixPV
		 *                投影-视图矩阵
		 * @return 参数适配器
		 */
		public Y2SandboxAdapter paramMatrixPV(YMatrix matrixPV)
		{
			this.matrixPV = matrixPV;
			return this;
		}

		/**
		 * @param skeleton
		 *                渲染的骨架
		 * @return 参数适配器
		 */
		public Y2SandboxAdapter paramSkeleton(YSkeleton skeleton)
		{
			this.skeleton = skeleton;
			return this;
		}

		@Override
		protected void bundleMapping(YWriteBundle bundle)
		{
			bundle.writeObject(SKE, skeleton);

			YMatrix.multiplyMM(matrixPVM, matrixPV,
					mover.getMatrix());
			bundle.writeFloatArray(PVM, matrixPVM.toFloatValues());
		}
	}
}
