package game.AndJoy.post_deal;

import ygame.skeleton.YSkeleton;

public class YDebrisTriangle extends YSkeleton
{
	public YDebrisTriangle(float[] f_arrTexCoords, float[] f_arrPositions)
	{
		setTexCoords(f_arrTexCoords);
		setPositions(f_arrPositions);
		setIndices(new short[]
		{ 0, 1, 2 });
	}
}
