package game.AndJoy.common;

import ygame.extension.primitives.YRectangle;

public class YMirrorYRectangle extends YRectangle
{

	public YMirrorYRectangle(float fWidth, float fHeight,
			boolean bCreateColor, boolean bCreateTexCoord)
	{
		super(fWidth, fHeight, bCreateColor, bCreateTexCoord);
		// 纹理坐标
		if (bCreateTexCoord)
		{
			// 坐标系镜像
			float[] fTexCoord =
			{ 1, 1, // 左下
					1, 0, // 左上
					0, 0, // 右上
					0, 1, // 右下
			};
			setTexCoords(fTexCoord);
		}
	}
}
