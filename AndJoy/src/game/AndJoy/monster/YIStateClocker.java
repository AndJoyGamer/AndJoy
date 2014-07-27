package game.AndJoy.monster;

import ygame.framework.core.YScene;
import ygame.framework.core.YSystem;

public interface YIStateClocker
{
	void onClock(float fElapseTime_s, AMonsterDomainLogic<?> domainLogicContext,
			YSystem system, YScene sceneCurrent);
}
