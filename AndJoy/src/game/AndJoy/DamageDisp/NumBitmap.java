package game.AndJoy.DamageDisp;

import game.AndJoy.R;
import game.AndJoy.common.AndjoyApp;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;

public class NumBitmap {


	/**
	 * 通过value组合一个信的伤害bitmap
	 * 
	 * @param value
	 * @param btm_res
	 * @return
	 */
	public static Bitmap getDmgBtmByValues(int value) {
		Resources res = AndjoyApp.getResource();
		if (res != null) {
			Bitmap btm_res = BitmapFactory.decodeResource(res,
					R.drawable.numbers);
			int bitNum = new String(value + "").length();
			Bitmap[] btm_nums = new Bitmap[bitNum];
			int[] nums = new int[bitNum];
			Bitmap newbtm = Bitmap.createBitmap(bitNum * btm_res.getWidth()
					/ 10, btm_res.getHeight(), Config.ARGB_8888);
			Canvas cv = new Canvas(newbtm);
			for (int i = 0; i < bitNum; i++) {
				nums[i] = (int) ((value % (Math.pow(10, bitNum - i))) / Math
						.pow(10, bitNum - i - 1));
				btm_nums[i] = getBitmapByNum(nums[i], btm_res);
				// 进行bitmap组合
				cv.drawBitmap(btm_nums[i], btm_nums[i].getWidth() * i, 0, null);
			}
			cv.save(Canvas.ALL_SAVE_FLAG);// 保存
			cv.restore();// 存储
//			newbtm = Bitmap.createScaledBitmap(newbtm, 10, 2, false);
			return newbtm;
		} else {
			Log.e("NumBitmap", "没有传入正确的Resources");
			return null;
		}
	}

	/**
	 * 通过value组合一个信的伤害bitmap
	 * 
	 * @param value
	 * @param btm_res
	 * @return
	 */
	private static Bitmap getBitmapByNum(int num, Bitmap btm_res) {
		int x, y, width, height;
		x = num * btm_res.getWidth() / 10;
		y = 0;
		width = btm_res.getWidth() / 10;
		height = btm_res.getHeight();
		Bitmap btm_num = Bitmap.createBitmap(btm_res, x, y, width, height);
		return btm_num;
	}

}
