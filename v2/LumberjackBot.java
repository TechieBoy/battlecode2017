package v2;

import battlecode.common.*;

public class LumberjackBot extends BaseBot
{
    private static Direction bounce = Direction.getEast();

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
                if(rc.senseNearbyRobots(GameConstants.LUMBERJACK_STRIKE_RADIUS,them).length>0)
                {
                    if(rc.canStrike())
                        rc.strike();
                    else
                        break;
                }
                TreeInfo[] info = rc.senseNearbyTrees(-1,Team.NEUTRAL);
                if (info.length > 0)
                {
                    if (rc.canChop(info[0].ID))
                    {
                        rc.chop(info[0].ID);
                    }
                    else
                    {
                        if (rc.canMove(here.directionTo(info[0].location)))
                        {
                            rc.move(here.directionTo(info[0].location));
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
