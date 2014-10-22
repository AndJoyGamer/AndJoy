package game.AndJoy.DamageDisp;

import game.AndJoy.R;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;

public class genBitmap {
	private int damegeValue = 0;
	private Resources res;
	private Bitmap btm_Num;// 数字原始bitmap

	public genBitmap(Resources res) {
		this.res = res;
		btm_Num = BitmapFactory.decodeResource(res, R.drawable.numbers);
	}

	public void setValue(int value) {
		this.damegeValue = value;
	}

	/**
	 * 通过value组合一个信的伤害bitmap
	 * 
	 * @param value
	 * @param btm_res
	 * @return
	 */
//	public Bitmap genBitmapByValues(int value, Bitmap btm_res) {
//		Integer.bitCount(i)
//	}
}
