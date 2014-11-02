package game.AndJoy.post_deal1;

import ygame.skeleton.YSkeleton;

//有波浪效果的纹理矩形
public class TextureRect extends YSkeleton
{
	private int vCount = 0;
	private final float WIDTH_SPAN = 3.3f;// 2.8f;//横向长度总跨度

	public TextureRect()
	{
		// 初始化顶点坐标与着色数据
		initVertexData();
	}

	// 初始化顶点坐标与着色数据的方法
	private void initVertexData()
	{
		final int cols = 12;// 列数
		final int rows = cols * 3 / 4;// 行数
		final float UNIT_SIZE = WIDTH_SPAN / cols;// 每格的单位长度
		// 顶点坐标数据的初始化================begin============================
		vCount = cols * rows * 6;// 每个格子两个三角形，每个三角形3个顶点
		float vertices[] = new float[vCount * 3];// 每个顶点xyz三个坐标
		int count = 0;// 顶点计数器
		for (int j = 0; j < rows; j++)
		{
			for (int i = 0; i < cols; i++)
			{
				// 计算当前格子左上侧点坐标
				float zsx = -UNIT_SIZE * cols / 2 + i
						* UNIT_SIZE;
				float zsy = UNIT_SIZE * rows / 2 - j
						* UNIT_SIZE;
				float zsz = 0;

				vertices[count++] = zsx;
				vertices[count++] = zsy;
				vertices[count++] = zsz;

				vertices[count++] = zsx;
				vertices[count++] = zsy - UNIT_SIZE;
				vertices[count++] = zsz;

				vertices[count++] = zsx + UNIT_SIZE;
				vertices[count++] = zsy;
				vertices[count++] = zsz;

				vertices[count++] = zsx + UNIT_SIZE;
				vertices[count++] = zsy;
				vertices[count++] = zsz;

				vertices[count++] = zsx;
				vertices[count++] = zsy - UNIT_SIZE;
				vertices[count++] = zsz;

				vertices[count++] = zsx + UNIT_SIZE;
				vertices[count++] = zsy - UNIT_SIZE;
				vertices[count++] = zsz;
			}
		}
		// 创建顶点坐标数据缓冲
		setPositions(vertices , true);
		// 顶点纹理坐标数据的初始化================begin============================
		float texCoor[] = generateTexCoor(cols, rows);
		setTexCoords(texCoor);
		setColors(createRandomColorData(vertices.length / 3));
	}

	// 自动切分纹理产生纹理数组的方法
	private float[] generateTexCoor(int bw, int bh)
	{
		float[] result = new float[bw * bh * 6 * 2];
		float sizew = 1.0f / bw;// 列数
		float sizeh = 0.75f / bh;// 行数
		int c = 0;
		for (int i = 0; i < bh; i++)
		{
			for (int j = 0; j < bw; j++)
			{
				// 每行列一个矩形，由两个三角形构成，共六个点，12个纹理坐标
				float s = j * sizew;
				float t = i * sizeh;

				result[c++] = s;
				result[c++] = t;

				result[c++] = s;
				result[c++] = t + sizeh;

				result[c++] = s + sizew;
				result[c++] = t;

				result[c++] = s + sizew;
				result[c++] = t;

				result[c++] = s;
				result[c++] = t + sizeh;

				result[c++] = s + sizew;
				result[c++] = t + sizeh;
			}
		}
		return result;
	}
}
