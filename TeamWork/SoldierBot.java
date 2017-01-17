package TeamWork;

import battlecode.common.*;

public class SoldierBot extends BaseBot
{
    private static Direction bounce = here.directionTo(closetInitalEnemyArchonLocation());
    public static void runSoldier() throws GameActionException
    {
        while (true)
        {
            try
            {
                here = rc.getLocation();
                visibleEnemies = rc.senseNearbyRobots(-1,them);
                respondToBroadCasts();
                if (visibleEnemies.length > 0)
                {
                    for(RobotInfo robotInfo : visibleEnemies)
                    {
                        if(robotInfo.getType() == RobotType.ARCHON)
                        {
                            broadcastEnemyLocation(FOUND_ENEMY_ARCHON_CHANNEL, ENEMY_ARCHON_X, ENEMY_ARCHON_Y, robotInfo);
                            if (rc.canFirePentadShot())
                            {
                                rc.firePentadShot(rc.getLocation().directionTo(robotInfo.location));
                            }

                        }
                        else if(robotInfo.getType() == RobotType.GARDENER)
                        {
                            broadcastEnemyLocation(FOUND_ENEMY_GARDENER_CHANNEL, ENEMY_GARDENER_X, ENEMY_GARDENER_Y, robotInfo);
                            if (rc.canFireTriadShot())
                            {
                                rc.fireTriadShot(rc.getLocation().directionTo(robotInfo.location));
                            }

                        }
                        else
                        {
                            if (rc.canFireSingleShot())
                            {
                                rc.fireSingleShot(rc.getLocation().directionTo(robotInfo.location));
                            }

                        }
                    }
                }
                else if(!rc.hasMoved())
                {
                    wander();
                }
                Clock.yield();

            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }
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
    private static boolean respondToBroadCasts() throws GameActionException
    {
        here = rc.getLocation();
        boolean friendlyArchonUnderAttack = rc.readBroadcast(FRIENDLY_ARCHON_UNDER_ATTACK_CHANNEL) == 1;
        boolean foundEnemyGardener = rc.readBroadcast(FOUND_ENEMY_GARDENER_CHANNEL) == 1;
        boolean foundArchon = rc.readBroadcast(FOUND_ENEMY_ARCHON_CHANNEL) == 1;
        boolean friendlyGardenerUnderAttack = rc.readBroadcast(FRIENDLY_GARDENER_UNDER_ATTACK_CHANNEL) == 1;
        if(friendlyArchonUnderAttack)
        {
            MapLocation friendlyArchonLocation = new MapLocation(Float.intBitsToFloat(rc.readBroadcast(FRIENDLY_ARCHON_X)),Float.intBitsToFloat(rc.readBroadcast(FRIENDLY_ARCHON_Y)));
            if(here.isWithinDistance(friendlyArchonLocation,RobotType.SOLDIER.sensorRadius))
            {
                if(rc.senseNearbyRobots(RobotType.SOLDIER.sensorRadius,them).length == 0)
                    rc.broadcast(FRIENDLY_ARCHON_UNDER_ATTACK_CHANNEL,0);
            }
            else
                tryMove(here.directionTo(friendlyArchonLocation));
            return true;
        }
        else if(foundArchon)
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
        else if(friendlyGardenerUnderAttack)
        {
            MapLocation friendlyGardenerLocation = new MapLocation(Float.intBitsToFloat(rc.readBroadcast(FRIENDLY_GARDENER_X)),Float.intBitsToFloat(rc.readBroadcast(FRIENDLY_GARDENER_Y)));
            if(here.isWithinDistance(friendlyGardenerLocation,RobotType.SOLDIER.sensorRadius/2))
            {
                if(rc.senseNearbyRobots(RobotType.SOLDIER.sensorRadius/2,them).length == 0)
                    rc.broadcast(FRIENDLY_GARDENER_UNDER_ATTACK_CHANNEL,0);
            }
            else
                tryMove(here.directionTo(friendlyGardenerLocation));
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
}
