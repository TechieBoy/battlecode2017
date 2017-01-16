package betterattack;

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
                bounce = dir.rotateLeftDegrees((float) ((Math.random() * 180) - 90));

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
                if (rc.readBroadcast(EARLY_GAME_SCOUT_SPAWNED_CHANNEL) == 0)
                    rc.broadcast(EARLY_GAME_SCOUT_SPAWNED_CHANNEL, 1);
                findTreeBulletsAndTanks();

                visibleEnemies = rc.senseNearbyRobots(-1, them);
                if (visibleEnemies.length > 0)
                {
                    for (RobotInfo robotInfo : visibleEnemies)
                    {
                        if (robotInfo.getType() == RobotType.ARCHON)
                        {
                            broadcastEnemyLocation(FOUND_ENEMY_ARCHON_CHANNEL, ENEMY_ARCHON_X, ENEMY_ARCHON_Y, robotInfo);
                            if (rc.canFireSingleShot() && rc.getTreeCount() > 1)
                            {
                                rc.fireSingleShot(rc.getLocation().directionTo(robotInfo.location));
                            } else if (!rc.hasMoved())
                            {
                                tryMove(here.directionTo(robotInfo.location));
                            }
                        } else if (robotInfo.getType() == RobotType.GARDENER)
                        {
                            broadcastEnemyLocation(FOUND_ENEMY_GARDENER_CHANNEL, ENEMY_GARDENER_X, ENEMY_GARDENER_Y, robotInfo);
                            while(true)
                            {
                                if (rc.canFireSingleShot() && rc.getTreeCount() > 1)
                                {
                                    rc.fireSingleShot(rc.getLocation().directionTo(robotInfo.location).rotateLeftDegrees(10));
                                } else if (!rc.hasMoved())
                                {
                                    tryMove(here.directionTo(robotInfo.location));
                                } else{
                                    break;
                                }
                            }
                        }
                    }

                    here = rc.getLocation();
                    visibleEnemies = rc.senseNearbyRobots(-1, them);
                }
                if (!rc.hasMoved())
                {
                    if (rc.getRoundNum() < 300)
                        wander();
                    else
                        respondToBroadCasts();
                }
                Clock.yield();

            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    public static boolean respondToBroadCasts() throws GameActionException
    {
        here = rc.getLocation();
        boolean foundArchon = rc.readBroadcast(FOUND_ENEMY_ARCHON_CHANNEL) == 1;
        if(foundArchon)
        {
            MapLocation enemyArchonLocation = new MapLocation((float)rc.readBroadcast(ENEMY_ARCHON_X),(int)rc.readBroadcast(ENEMY_ARCHON_Y));
            tryMove(here.directionTo(enemyArchonLocation));
            return true;
        }
        boolean foundEnemyGardener = rc.readBroadcast(FOUND_ENEMY_GARDENER_CHANNEL) == 1;
        if(foundEnemyGardener)
        {
            MapLocation enemyGardenerLocation = new MapLocation((float)rc.readBroadcast(ENEMY_GARDENER_X),(int)rc.readBroadcast(ENEMY_GARDENER_Y));
            tryMove(here.directionTo(enemyGardenerLocation));
            return true;
        }
        return false;
    }

    private static void broadcastEnemyLocation(int statusFlag, int x, int y, RobotInfo robotInfo) throws GameActionException
    {
        here = rc.getLocation();
        rc.broadcast(statusFlag, 1);
        rc.broadcast(x, (int)robotInfo.location.x);
        rc.broadcast(y, (int) robotInfo.location.y);

    }

    private static void findTreeBulletsAndTanks() throws GameActionException
    {
        here = rc.getLocation();
        TreeInfo[] info = rc.senseNearbyTrees(-1, Team.NEUTRAL);
        if (info.length > 0)
        {
            for (TreeInfo i : info)
            {
                if (i.containedRobot == RobotType.TANK)
                {
                    rc.broadcast(SCOUT_FOUND_CONTAINED_TANK_CHANNEL, 1);
                    rc.broadcast(CONTAINED_TANK_X_CHANNEL, (int) i.location.x);
                    rc.broadcast(CONTAINED_TANK_Y_CHANNEL, (int) i.location.y);
                } else if (i.containedBullets > 0)
                {
                    if (rc.canShake(i.ID))
                    {
                        rc.shake(i.ID);
                    } else
                    {
                        if (!rc.hasMoved())
                            tryMove(here.directionTo(i.location));
                    }
                }

            }
        }

    }
}
