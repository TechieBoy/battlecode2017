package initialbot;

import battlecode.common.Clock;
import battlecode.common.GameActionException;

public class SoldierBot extends BaseBot
{

    public static void runSoldier() throws GameActionException
    {
        System.out.println("I'm an soldier!");

        while (true)
        {
            try
            {
                updateRobotInfos();
                here = rc.getLocation();
                checkIfAtWar();

                if(!atWar)
                {
                    if (visibleEnemies.length > 0)
                    {
                        // And we have enough bullets, and haven't attacked yet this turn...
                        if (rc.canFireSingleShot())
                        {
                            // ...Then fire a bullet in the direction of the enemy.
                            rc.fireSingleShot(rc.getLocation().directionTo(visibleEnemies[0].location));
                        }
                    }
                    if (rc.canMove(closetInitalEnemyArchonLocation()))
                    {
                        tryMove(here.directionTo(closetInitalEnemyArchonLocation()));
                    }
                }
                Clock.yield();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
}
