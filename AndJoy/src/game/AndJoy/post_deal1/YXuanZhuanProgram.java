package game.AndJoy.post_deal1;

import android.content.res.Resources;
import ygame.domain.YDomainView;
import ygame.framework.core.YSystem;
import ygame.framework.domain.YReadBundle;
import ygame.framework.domain.YWriteBundle;
import ygame.math.YMatrix;
import ygame.program.YAShaderProgram;
import ygame.skeleton.YSkeleton;
import ygame.texture.YTexture;
import ygame.transformable.YMover;
import ygame.utils.YTextFileUtils;

public class YXuanZhuanProgram extends YAShaderProgram
{
	private static final int iPVM = 0;
	private static final int iSKE = 1;
	private static final int iTEX = 2;

	private static YXuanZhuanProgram instance;

	public static YXuanZhuanProgram getInstance(Resources resources)
	{
		if (null == instance)
			synchronized (YXuanZhuanProgram.class)
			{
				if (null == instance)
					instance = new YXuanZhuanProgram(
							resources);
			}
		return instance;
	}

	protected YXuanZhuanProgram(Resources resources)
	{
		fillCodeAndParam(YTextFileUtils.getStringFromAssets(
				"xuanzhuan_vsh", resources),
				YTextFileUtils.getStringFromAssets(
						"xuanzhuan_fsh", resources),
				YXuanZhuanAdapter.class);
	}

	@Override
	protected void applyParams(int iProgramHandle, YReadBundle bundle,
			YSystem system, YDomainView domainView)
	{
		setUniformMatrix("uMVPMatrix", bundle.readFloatArray(iPVM));
		YSkeleton skeleton = (YSkeleton) bundle.readObject(iSKE);
		setAttribute("aPosition", skeleton.getPositionDataSource());
		setAttribute("aTexCoor", skeleton.getTexCoordDataSource());
		setUniformf("ratio", 0.5f);
		setUniformTexture("sTexture",
				(YTexture) bundle.readObject(iTEX), 0);
	}

	public static class YXuanZhuanAdapter extends YABaseParametersAdapter
	{
		private YMover mover;
		private YTexture texture;
		private YSkeleton skeleton;
		private YMatrix matrixPV;
		private YMatrix matrixPVM = new YMatrix();

		@Override
		protected void bundleMapping(YWriteBundle bundle)
		{
			YMatrix.multiplyMM(matrixPVM, matrixPV,
					mover.getMatrix());
			bundle.writeFloatArray(iPVM, matrixPVM.toFloatValues());

			bundle.writeObject(iSKE, skeleton);
			bundle.writeObject(iTEX, texture);
		}

		public YXuanZhuanAdapter paramMover(YMover mover)
		{
			this.mover = mover;
			return this;
		}

		public YXuanZhuanAdapter paramTexture(YTexture texture)
		{
			this.texture = texture;
			return this;
		}

		public YXuanZhuanAdapter paramSkeleton(YSkeleton skeleton)
		{
			this.skeleton = skeleton;
			return this;
		}

		public YXuanZhuanAdapter paramMatrixPV(YMatrix matrixPV)
		{
			this.matrixPV = matrixPV;
			return this;
		}
	}

}
