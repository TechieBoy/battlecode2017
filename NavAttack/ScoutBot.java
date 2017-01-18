package NavAttack;

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
                            }
                            else if (!rc.hasMoved())
                            {
                                    tryMove(here.directionTo(robotInfo.location));
                            }
                        }
                        else if (robotInfo.getType() == RobotType.GARDENER)
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
                }
                if (!rc.hasMoved())
                {
                    if (rc.getRoundNum() < 250)
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
        boolean friendlyArchonUnderAttack = rc.readBroadcast(FRIENDLY_ARCHON_UNDER_ATTACK_CHANNEL) == 1;
        boolean foundEnemyGardener = rc.readBroadcast(FOUND_ENEMY_GARDENER_CHANNEL) == 1;
        boolean foundArchon = rc.readBroadcast(FOUND_ENEMY_ARCHON_CHANNEL) == 1;

        if(foundArchon)
        {
            MapLocation enemyArchonLocation = new MapLocation(Float.intBitsToFloat(rc.readBroadcast(ENEMY_ARCHON_X)),Float.intBitsToFloat(rc.readBroadcast(ENEMY_ARCHON_Y)));
            if(here.isWithinDistance(enemyArchonLocation,RobotType.SCOUT.sensorRadius/2))
            {
                if(rc.senseNearbyRobots(RobotType.SCOUT.sensorRadius/2,them).length == 0)
                    rc.broadcast(FOUND_ENEMY_ARCHON_CHANNEL,0);
            }
            else
                tryMove(here.directionTo(enemyArchonLocation));
            return true;
        }
        else if(foundEnemyGardener)
        {
            MapLocation enemyGardenerLocation = new MapLocation(Float.intBitsToFloat(rc.readBroadcast(ENEMY_GARDENER_X)),Float.intBitsToFloat(rc.readBroadcast(ENEMY_GARDENER_Y)));
            if(here.isWithinDistance(enemyGardenerLocation,RobotType.SCOUT.sensorRadius/2))
            {
                if(rc.senseNearbyRobots(RobotType.SCOUT.sensorRadius/2,them).length == 0)
                    rc.broadcast(FRIENDLY_GARDENER_UNDER_ATTACK_CHANNEL,0);
            }
            else
                tryMove(here.directionTo(enemyGardenerLocation));
            return true;
        }
        else if(friendlyArchonUnderAttack)
        {
            MapLocation friendlyArchonLocation = new MapLocation(Float.intBitsToFloat(rc.readBroadcast(FRIENDLY_ARCHON_X)),Float.intBitsToFloat(rc.readBroadcast(FRIENDLY_ARCHON_Y)));
            if(here.isWithinDistance(friendlyArchonLocation,RobotType.SCOUT.sensorRadius))
            {
                if(rc.senseNearbyRobots(RobotType.SCOUT.sensorRadius,them).length == 0)
                    rc.broadcast(FRIENDLY_ARCHON_UNDER_ATTACK_CHANNEL,0);
            }
            else
                tryMove(here.directionTo(friendlyArchonLocation));
            return true;
        }
        return false;
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
                    rc.broadcast(CONTAINED_TANK_X_CHANNEL, Float.floatToIntBits(i.location.x));
                    rc.broadcast(CONTAINED_TANK_Y_CHANNEL, Float.floatToIntBits(i.location.y));
                }
                else if (i.containedBullets > 0)
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
