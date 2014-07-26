package game.AndJoy.common;


public class YMirrorYSquare extends YMirrorYRectangle
{

	public YMirrorYSquare(float fSideLength, boolean bCreateColor,
			boolean bCreateTexCoord)
	{
		super(fSideLength, fSideLength, bCreateColor, bCreateTexCoord);
	}

	// public YMyMirrorXSquare(float fSideLength, boolean bCreateColor,
	// boolean bCreateTexCoord)
	// {
	// super(fSideLength, bCreateColor, bCreateTexCoord);
	// // 纹理坐标
	// if (bCreateTexCoord)
	// {
	// // 坐标系镜像
	// float[] fTexCoord =
	// { 1, 1, // 左下
	// 1, 0, // 左上
	// 0, 0, // 右上
	// 0, 1, // 右下
	// };
	// setTexCoords(fTexCoord);
	// }
	// }

}
