package TrialMicro;

import battlecode.common.*;

import static TrialMicro.Messaging.*;

public class TankBot extends BaseBot
{
    private static Direction bounce = here.directionTo(closetInitalEnemyArchonLocation());

    public static void runTank() throws GameActionException
    {
        while(true)
        {
            try
            {
                here = rc.getLocation();
                visibleEnemies = rc.senseNearbyRobots(-1,them);
                if (visibleEnemies.length > 0)
                {
                    if(!rc.hasMoved())
                    {
                        tryMove(here.directionTo(visibleEnemies[0].location));
                    }
                    else if (rc.canFirePentadShot())
                    {
                        rc.firePentadShot(rc.getLocation().directionTo(visibleEnemies[0].location));
                    }

                }
                if(!rc.hasMoved())
                    respondToBroadCasts();
                if(!rc.hasMoved())
                    wander();

                Clock.yield();

            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }

    }
    private static boolean respondToBroadCasts() throws GameActionException
    {
        here = rc.getLocation();
        boolean foundEnemyGardener = rc.readBroadcast(FOUND_ENEMY_GARDENER_CHANNEL) == 1;
        boolean foundArchon = rc.readBroadcast(FOUND_ENEMY_ARCHON_CHANNEL) == 1;

        if(foundArchon)
        {
            MapLocation enemyArchonLocation = new MapLocation(Float.intBitsToFloat(rc.readBroadcast(ENEMY_ARCHON_X)),Float.intBitsToFloat(rc.readBroadcast(ENEMY_ARCHON_Y)));
            if(here.isWithinDistance(enemyArchonLocation,RobotType.SOLDIER.sensorRadius/2))
            {
                if(rc.senseNearbyRobots(RobotType.SOLDIER.sensorRadius/2,them).length == 0)
                    rc.broadcast(FOUND_ENEMY_ARCHON_CHANNEL,0);
            }
            else
                tryMove(here.directionTo(enemyArchonLocation));
            return true;
        }
        else if(foundEnemyGardener)
        {
            MapLocation enemyGardenerLocation = new MapLocation(Float.intBitsToFloat(rc.readBroadcast(ENEMY_GARDENER_X)),Float.intBitsToFloat(rc.readBroadcast(ENEMY_GARDENER_Y)));
            if(here.isWithinDistance(enemyGardenerLocation,RobotType.SOLDIER.sensorRadius/2))
            {
                if(rc.senseNearbyRobots(RobotType.SOLDIER.sensorRadius/2,them).length == 0)
                    rc.broadcast(FOUND_ENEMY_GARDENER_CHANNEL,0);
            }
            else
                tryMove(here.directionTo(enemyGardenerLocation));
            return true;
        }
        return false;
    }

    private static void wander() throws GameActionException
    {
        if (!rc.hasMoved())
        {
            if (!rc.canMove(bounce))
            {
                here = rc.getLocation();
                Direction dir = here.directionTo(closetInitalEnemyArchonLocation());
                bounce = dir.rotateLeftDegrees((float) ((Math.random() * 180) - 90));

            } else
                tryMove(bounce);
        }

    }
}
