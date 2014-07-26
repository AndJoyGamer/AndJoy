package game.AndJoy.common;

import ygame.extension.primitives.YRectangle;

public class YMirrorXRectangle extends YRectangle
{

	public YMirrorXRectangle(float fWidth, float fHeight,
			boolean bCreateColor, boolean bCreateTexCoord)
	{
		super(fWidth, fHeight, bCreateColor, bCreateTexCoord);
		// 纹理坐标
		if (bCreateTexCoord)
		{
			// 坐标系镜像
			float[] fTexCoord =
			{ 0, 0, // 左上
					0, 1, // 左下
					1, 1, // 右下
					1, 0, // 右上
			};
			setTexCoords(fTexCoord);
		}
	}

}
