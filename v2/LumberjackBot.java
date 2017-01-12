package v2;

import battlecode.common.*;

public class LumberjackBot extends BaseBot
{
    private static Direction bounce = Direction.getNorth();

    private static void wander() throws GameActionException
    {
        if (!rc.hasMoved())
        {
            if (!rc.canMove(bounce))
            {
                bounce = randomDirection();
            } else
                tryMove(bounce);
        }

    }
    public static void runLumberjack() throws GameActionException
    {

        while(true)
        {
            try
            {
                if(rc.senseNearbyRobots(1,them).length>0)
                {
                    rc.strike();
                }
                else if (rc.senseNearbyTrees(-1, Team.NEUTRAL).length > 0)
                {
                    TreeInfo[] info = rc.senseNearbyTrees(-1,Team.NEUTRAL);
                    if (rc.canMove(here.directionTo(info[0].location)))
                    {
                        rc.move(here.directionTo(info[0].location));
                    }
                    else
                    {
                        if (rc.canChop(info[0].getID()))
                        {
                            rc.chop(info[0].getID());
                        }
                    }
                }
                else{
                    wander();
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
