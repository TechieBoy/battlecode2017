package NavAttackv2;

import battlecode.common.*;

public class SoldierBot extends BaseBot
{
    private static Direction bounce = here.directionTo(closetInitalEnemyArchonLocation());
    private static RobotInfo myEnemy = null;
    public static void runSoldier() throws GameActionException
    {
        while (true)
        {
            try
            {
                here = rc.getLocation();
                bugNav(closetInitalEnemyArchonLocation());
//                visibleEnemies = rc.senseNearbyRobots(-1,them);
//                if (visibleEnemies.length > 0 && myEnemy == null)
//                {
//                    for(RobotInfo robotInfo : visibleEnemies)
//                    {
//                        if(robotInfo.getType() == RobotType.ARCHON)
//                        {
//                            broadcastEnemyLocation(FOUND_ENEMY_ARCHON_CHANNEL, ENEMY_ARCHON_X, ENEMY_ARCHON_Y, robotInfo);
//                            myEnemy = robotInfo;
//                            if (rc.canFirePentadShot() && !friendlyfire(robotInfo))
//                            {
//                                rc.firePentadShot(rc.getLocation().directionTo(robotInfo.location));
//                            }
//                            if(rc.canMove(here.directionTo(robotInfo.location)))
//                            {
//                                tryMove(here.directionTo(robotInfo.location));
//                            }
//
//                        }
//                        else if(robotInfo.getType() == RobotType.GARDENER)
//                        {
//                            broadcastEnemyLocation(FOUND_ENEMY_GARDENER_CHANNEL, ENEMY_GARDENER_X, ENEMY_GARDENER_Y, robotInfo);
//                            myEnemy = robotInfo;
//                            if (rc.canFireTriadShot() && !friendlyfire(robotInfo))
//                            {
//                                rc.fireTriadShot(rc.getLocation().directionTo(robotInfo.location));
//                            }
//
//                        }
//                        else if(robotInfo.getType() == RobotType.TANK)
//                        {
//                            myEnemy = robotInfo;
//                            if (rc.canFirePentadShot() && !friendlyfire(robotInfo))
//                            {
//                                rc.firePentadShot(rc.getLocation().directionTo(robotInfo.location));
//                            }
//                            if(rc.canMove(here.directionTo(robotInfo.location)))
//                            {
//                                tryMove(here.directionTo(robotInfo.location));
//                            }
//                        }
//                        else
//                        {
//                            if (rc.canFireTriadShot() && !friendlyfire(robotInfo))
//                            {
//                                rc.fireTriadShot(rc.getLocation().directionTo(robotInfo.location));
//                            }
//                            else if(rc.canFireSingleShot() && !friendlyfire(robotInfo))
//                            {
//                                rc.fireSingleShot(rc.getLocation().directionTo(robotInfo.location));
//                            }
//                        }
//                    }
//                }
//                else if(visibleEnemies.length>0 && myEnemy!=null)
//                {
//                    if(rc.canSenseRobot(myEnemy.ID))
//                    {
//                        myEnemy = rc.senseRobot(myEnemy.ID);
//                        tryMove(here.directionTo(myEnemy.location));
//                        here = rc.getLocation();
//                        if (rc.canFirePentadShot() && !friendlyfire(myEnemy))
//                        {
//                            rc.firePentadShot(rc.getLocation().directionTo(myEnemy.location));
//                        }
//                    }
//                    else
//                        myEnemy = null;
//                }
//                else
//                    respondToBroadCasts();
//
//                tryDodge();
//                if(!rc.hasMoved())
//                {
//                    wander();
//                }
                Clock.yield();

            }
            catch (Exception e)
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

    private static void tryDodge() throws GameActionException
    {
        bulletsInSenseRadius = rc.senseNearbyBullets(-1);
        if (!rc.hasMoved() && bulletsInSenseRadius.length > 0)
        {
            for (int i = bulletsInSenseRadius.length ; i-- > 0; )
            {
                // Get relevant bullet information
                Direction propagationDirection = bulletsInSenseRadius[i].dir;
                MapLocation bulletLocation = bulletsInSenseRadius[i].location;

                // Calculate bullet relations to this robot
                Direction directionToRobot = bulletLocation.directionTo(here);
                float theta = propagationDirection.radiansBetween(directionToRobot);
                float distToRobot = bulletLocation.distanceTo(here);
                float perpendicularDist = (float) Math.abs(distToRobot * Math.sin(theta)); // soh cah toa :)

                // If theta > 90 degrees, then the bullet is traveling away from us and we can break early
                if (Math.abs(theta) < Math.PI / 2 && perpendicularDist <= rc.getType().bodyRadius)
                {
                    if (rc.canMove(directionToRobot.rotateLeftDegrees(90)))
                    {
                        tryMove(directionToRobot.rotateLeftDegrees(90));
                    } else if (rc.canMove(directionToRobot.rotateRightDegrees(90)))
                    {
                        tryMove(directionToRobot.rotateRightDegrees(90));
                    }
                    break;
                }
            }
        }
    }

    private static boolean friendlyfire(RobotInfo enemyInfo) throws GameActionException
    {
       if(enemyInfo!=null)
       {
           Float howFarIllFire = here.distanceTo(enemyInfo.location);
           RobotInfo[] alliesInRange = rc.senseNearbyRobots(enemyInfo.location,howFarIllFire,us);
           if(alliesInRange.length == 0) return false;
           if(alliesInRange.length<3 && alliesInRange[0].getHealth()>alliesInRange[0].getType().maxHealth/10)
           {
               return false;
           }
           return true;
       }
       return false;
    }
}
