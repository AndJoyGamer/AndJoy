package game.AndJoy.sprite;

import ygame.framework.core.YScene;
import ygame.framework.core.YSystem;

public interface YIStateClocker
{
	void onClock(float fElapseTime_s, YASpriteDomainLogic<?> domainLogicContext,
			YSystem system, YScene sceneCurrent);
}
