package rankedbot;

import battlecode.common.*;

public class ScoutBot extends BaseBot
{
    static TreeInfo lastTreeSeen[] = new TreeInfo[50];
    static int curr = 0;
    static boolean haveBeenToCenter = false;
    public static void runScout() throws GameActionException
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
                e.printStackTrace();
            }
        }
    }
}
