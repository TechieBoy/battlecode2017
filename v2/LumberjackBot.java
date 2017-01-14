package v2;

import battlecode.common.*;

public class LumberjackBot extends BaseBot
{
    private static Direction bounce = here.directionTo(centerOfTheirInitialArchons);

    private static void wander() throws GameActionException
    {
        if (!rc.hasMoved())
        {
            if (!rc.canMove(bounce))
            {
                bounce = here.directionTo(closetInitalEnemyArchonLocation()).rotateLeftDegrees(((float)Math.random()*100) - 40);
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
                        tryMove(here.directionTo(info[0].location));
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
