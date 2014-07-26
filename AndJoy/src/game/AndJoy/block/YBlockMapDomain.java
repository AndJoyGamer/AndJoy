package game.AndJoy.block;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.json.JSONException;

import ygame.domain.YADomainLogic;
import ygame.domain.YDomain;
import ygame.domain.YDomainView;
import ygame.exception.YException;
import ygame.extension.YTiledJsonParser;
import ygame.extension.primitives.YRectangle;
import ygame.extension.program.YTextureProgram;
import ygame.framework.core.YABaseDomain;
import ygame.framework.core.YClusterDomain;
import ygame.framework.core.YGL_Configuration;
import ygame.framework.core.YRequest;
import ygame.framework.core.YScene;
import ygame.framework.core.YSystem;
import ygame.framework.domain.YWriteBundle;
import ygame.math.YMatrix;
import ygame.math.vector.Vector2;
import ygame.skeleton.YSkeleton;
import ygame.texture.YTexture;
import ygame.transformable.YMover;
import ygame.utils.YLog;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Rect;

public class YBlockMapDomain extends YClusterDomain {
	private final int iMapResId;
	private final YTiledJsonParser parser;

	private int iInternalKey;

	public YBlockMapDomain(String KEY, int iMapResId, YTiledJsonParser parser) {
		super(KEY);
		this.iMapResId = iMapResId;
		this.parser = parser;
	}

	@Override
	protected void onGL_Initialize(YSystem system,
			YGL_Configuration configurationGL, int iWidth, int iHeight) {
		super.onGL_Initialize(system, configurationGL, iWidth, iHeight);
		addComponentDomains(splitToDomains(system, configurationGL), system);
	}

	private Collection<YABaseDomain> splitToDomains(YSystem system,
			YGL_Configuration configurationGL) {
		Options options = new Options();
		Resources resources = system.getContext().getResources();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeResource(resources, iMapResId, options);
		final int iMaxTexSize = configurationGL.getMaxTextureSize();
		if (iMaxTexSize >= options.outHeight && iMaxTexSize >= options.outWidth)
			return createDomainDirect(resources);
		try {
			if (iMaxTexSize >= options.outHeight
					&& iMaxTexSize < options.outWidth)
				return createDomainsSplitByWidth(resources, iMaxTexSize,
						options.outWidth, options.outHeight);
			else
				throw new YException("遇到不能解析的类型", getClass().getName(),
						"暂时不支持，待完善");
		} catch (Exception e) {
			e.printStackTrace();
			throw new YException(e.toString(), getClass().getName(),
					"请查阅图片资源是否放在/res/raw中？");
		}
	}

	/**
	 * @param resources
	 * @param iWidthPerBlock
	 *            像素为单位
	 * @param iWidthWholeMap
	 *            像素为单位
	 * @param iHeightWholeMap
	 *            像素为单位
	 * @return
	 * @throws IOException
	 * @throws JSONException
	 */
	private Collection<YABaseDomain> createDomainsSplitByWidth(
			Resources resources, int iWidthPerBlock, int iWidthWholeMap,
			int iHeightWholeMap) throws IOException, JSONException {
		InputStream is = null;
		try {
			is = resources.openRawResource(iMapResId);
			BitmapRegionDecoder decoder = BitmapRegionDecoder.newInstance(is,
					false);

			final int iNum = iWidthWholeMap / iWidthPerBlock;
			YSkeleton skeleton = new YRectangle(iWidthPerBlock
					/ parser.parseTileWidthInPixel()
					* parser.fRealWorldToTileWorldRadio, iHeightWholeMap
					/ parser.parseTileHeightInPixel()
					* parser.fRealWorldToTileWorldRadio, false, true);
			List<YABaseDomain> domains = new ArrayList<YABaseDomain>();
			YDomainView view = new YDomainView(
					YTextureProgram.getInstance(resources));
			for (int i = 0; i < iNum; i++) {
				Bitmap bitmap = decoder.decodeRegion(new Rect(i
						* iWidthPerBlock, 0, (i + 1) * iWidthPerBlock,
						iHeightWholeMap), null);
				YTexture texture = new YTexture(bitmap);
				int iStartX = -iWidthWholeMap / 2 + iWidthPerBlock / 2;
				Vector2 vector2Position = new Vector2(iStartX + i
						* iWidthPerBlock, 0);
				YDomain domain = new YDomain(KEY + generateInternalKey(),
						new YDebrisLogic(skeleton, texture, vector2Position),
						view);
				domains.add(domain);
			}

			int iRest = iWidthWholeMap % iWidthPerBlock;
			if (0 != iRest) {
				Bitmap bitmap = decoder.decodeRegion(new Rect(iNum
						* iWidthPerBlock, 0, iNum * iWidthPerBlock + iRest,
						iHeightWholeMap), null);
				YTexture texture = new YTexture(bitmap);
				Vector2 vector2Position = new Vector2(iWidthWholeMap / 2
						- iRest / 2, 0);
				YDomain domain = new YDomain(KEY + generateInternalKey(),
						new YDebrisLogic(skeleton, texture, vector2Position),
						view);
				domains.add(domain);
			}

			YLog.i(getClass().getName(), "按照每块宽度：" + iWidthPerBlock + " 拆分地图，"
					+ "总宽度为：" + iWidthWholeMap + "的地图被拆分为："
					+ (iNum + (0 == iRest ? 0 : 1)) + "块，不能凑整的部分宽度为：" + iRest);

			return domains;
		} finally {
			if (null != is)
				is.close();
		}
	}

	private Collection<YABaseDomain> createDomainDirect(Resources resources) {
		List<YABaseDomain> domains = new ArrayList<YABaseDomain>();
		YSkeleton skeleton = new YRectangle(parser.parseFinalMapColumnNum()
				* parser.fRealWorldToTileWorldRadio,
				parser.parseFinalMapRowNum()
						* parser.fRealWorldToTileWorldRadio, false, true);
		YDomainView view = new YDomainView(
				YTextureProgram.getInstance(resources));
		YTexture texture = new YTexture(BitmapFactory.decodeStream(resources
				.openRawResource(iMapResId)));
		YDomain domain = new YDomain(KEY + generateInternalKey(),
				new YDebrisLogic(skeleton, texture, new Vector2(0, 0)), view);
		domains.add(domain);

		YLog.i(getClass().getName(), "直接使用地图，不拆分");

		return domains;
	}

	private String generateInternalKey() {
		++iInternalKey;
		return "_" + iInternalKey;
	}

	private static class YDebrisLogic extends YADomainLogic {
		final private YMover mover;
		final private YSkeleton skeleton;
		final private YTexture texture;

		private YDebrisLogic(YSkeleton skeleton, YTexture texture,
				Vector2 vector2Position) {
			this.mover = new YMover();
			mover.setX(vector2Position.x).setY(vector2Position.y);
			this.skeleton = skeleton;
			this.texture = texture;
		}

		@Override
		protected void onCycle(double dbElapseTime_s, YDomain domainContext,
				YWriteBundle bundle, YSystem system, YScene sceneCurrent,
				YMatrix matrix4pv, YMatrix matrix4Projection,
				YMatrix matrix4View) {
			YTextureProgram.YAdapter adapter = (YTextureProgram.YAdapter) domainContext
					.getParametersAdapter();
			adapter.paramMatrixPV(matrix4pv).paramMover(mover)
					.paramSkeleton(skeleton).paramTexture(texture);
		}

		@Override
		protected boolean onDealRequest(YRequest request, YSystem system,
				YScene sceneCurrent) {
			return false;
		}
	}

}
