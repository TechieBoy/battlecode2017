package rankedbot;

import battlecode.common.Clock;
import battlecode.common.GameActionException;

public class SoldierBot extends BaseBot
{

    public static void runSoldier() throws GameActionException
    {
        while (true)
        {
            try
            {
                here = rc.getLocation();
                visibleEnemies = rc.senseNearbyRobots(-1,them);
                while (visibleEnemies.length > 0)
                {
                    if (rc.canFireSingleShot())
                    {
                        rc.fireSingleShot(rc.getLocation().directionTo(visibleEnemies[0].location));
                    }
                    else
                        if(!rc.hasMoved())
                        {
                            tryMove(here.directionTo(visibleEnemies[0].location));
                        }
                    here = rc.getLocation();
                    visibleEnemies = rc.senseNearbyRobots(-1,them);
                }
                if(!rc.hasMoved())
                    tryMove(here.directionTo(closetInitalEnemyArchonLocation()));
                Clock.yield();

            } catch (Exception e)
            {
                System.out.println("Soldier Exception");
                e.printStackTrace();
            }
        }
    }
}
