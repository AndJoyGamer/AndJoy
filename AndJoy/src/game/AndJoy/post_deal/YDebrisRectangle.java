package game.AndJoy.post_deal;

import ygame.extension.primitives.YRectangle;

public class YDebrisRectangle extends YRectangle
{

	public YDebrisRectangle(float fWidth, float fHeight,
			boolean bCreateColor, float[] f_arrTexCoords)
	{
		super(fWidth, fHeight, bCreateColor, false);
		setTexCoords(f_arrTexCoords);
	}
}
