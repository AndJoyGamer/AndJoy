package game.AndJoy.common;

import android.app.Application;
import android.content.res.Resources;

public class AndjoyApp extends Application
{
	private static Resources resources;

	@Override
	public void onCreate()
	{
		super.onCreate();
		resources = getResources();

	}

	public static Resources getResource()
	{
		return resources;
	}

	public static int getScreenWidth()
	{
		return resources.getDisplayMetrics().widthPixels;
	}

	public static int getScreenHeight()
	{
		return resources.getDisplayMetrics().heightPixels;
	}

	public static float getScreenDen()
	{
		return resources.getDisplayMetrics().density;
	}

	@Override
	public void onTerminate()
	{
		super.onTerminate();
		resources = null;
	}
}
