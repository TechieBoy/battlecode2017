package counterattack;

import battlecode.common.*;

public class ScoutBot extends BaseBot
{

    private static Direction bounce = here.directionTo(closetInitalEnemyArchonLocation());


    private static void wander() throws GameActionException
    {
        if (!rc.hasMoved())
        {
            if (!rc.canMove(bounce))
            {
                here = rc.getLocation();
                Direction dir = here.directionTo(closetInitalEnemyArchonLocation());
                bounce = dir.rotateLeftDegrees((float)((Math.random()*180) - 90));

            } else
                tryMove(bounce);
        }

    }
    public static void runScout() throws GameActionException
    {
        while (true)
        {
            try
            {
                if(rc.readBroadcast(EARLY_GAME_SCOUT_SPAWNED_CHANNEL) == 0)
                    rc.broadcast(EARLY_GAME_SCOUT_SPAWNED_CHANNEL,1);
                findTreeBulletsAndTanks();

                visibleEnemies = rc.senseNearbyRobots(-1,them);
                if (visibleEnemies.length > 0)
                {
                    if (rc.canFireSingleShot() && rc.getTeamBullets() > 120)
                    {
                        rc.fireSingleShot(rc.getLocation().directionTo(visibleEnemies[0].location));
                    }
                    else if(!rc.hasMoved())
                    {
                        tryMove(here.directionTo(visibleEnemies[0].location));
                    }
                    here = rc.getLocation();
                    visibleEnemies = rc.senseNearbyRobots(-1,them);
                }
                if(!rc.hasMoved())
                    wander();
                Clock.yield();

            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    private static void findTreeBulletsAndTanks() throws GameActionException
    {
        here = rc.getLocation();
        TreeInfo[] info = rc.senseNearbyTrees(-1, Team.NEUTRAL);
        if (info.length > 0)
        {
            for(TreeInfo i : info)
            {
                if(i.containedRobot == RobotType.TANK)
                {
                    rc.broadcast(SCOUT_FOUND_CONTAINED_TANK_CHANNEL,1);
                    rc.broadcast(CONTAINED_TANK_X_CHANNEL,(int)i.location.x);
                    rc.broadcast(CONTAINED_TANK_Y_CHANNEL,(int)i.location.y);
                }
                else if(i.containedBullets > 0)
                {
                    if(rc.canShake(i.ID))
                    {
                        rc.shake(i.ID);
                    }
                    else
                    {
                        if(!rc.hasMoved())
                            tryMove(here.directionTo(i.location));
                    }
                }

            }
        }

    }
}
