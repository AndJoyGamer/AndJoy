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

	@Override
	public void onTerminate()
	{
		super.onTerminate();
		resources = null;
	}
}
