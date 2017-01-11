package initialbot;

import battlecode.common.Clock;
import battlecode.common.GameActionException;

public class LumberjackBot extends BaseBot
{

    public static void runLumberjack() throws GameActionException
    {
        System.out.println("I'm a LumberJack!");
        while(true)
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
                        rc.strike();
                    } else if (visibleNeutralTrees.length > 0)
                    {
                        if (rc.canMove(visibleNeutralTrees[0].location))
                        {
                            rc.move(here.directionTo(visibleNeutralTrees[0].location));
                        } else
                        {
                            if (rc.canChop(visibleNeutralTrees[0].getID()))
                            {
                                System.out.println("Trying to chop at " + here.distanceTo(visibleNeutralTrees[0].location));
                                rc.chop(visibleNeutralTrees[0].getID());
                            }
                        }
                    } else
                    {
                        tryMove(here.directionTo(centerOfAllInitialArchons));
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
