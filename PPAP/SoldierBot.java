package PPAP;

import battlecode.common.*;

import static PPAP.Messaging.*;

public class SoldierBot extends BaseBot
{
    private static Direction bounce = here.directionTo(closetInitalEnemyArchonLocation());
    private static Direction defensiveBounce = here.directionTo(closetInitalAlliedArchonLocation());
    private static RobotInfo myEnemy = null;
    private static boolean onOffense = rc.getRoundNum() < 200;

    public static void runSoldier() throws GameActionException
    {
        while (true)
        {
            try
            {
                here = rc.getLocation();
                visibleEnemies = rc.senseNearbyRobots(-1, them);

                if (visibleEnemies.length > 0 && myEnemy == null)
                {
                    for (RobotInfo robotInfo : visibleEnemies)
                    {
                        if (robotInfo.getType() == RobotType.ARCHON)
                        {
                            broadcastEnemyLocation(robotInfo);
                            myEnemy = robotInfo;
                            if (rc.canFirePentadShot())
                            {
                                rc.firePentadShot(rc.getLocation().directionTo(robotInfo.location));
                            }
                            else if (rc.canMove(here.directionTo(robotInfo.location)))
                            {
                                tryMove(here.directionTo(robotInfo.location));
                            }

                        }
                        else if (robotInfo.getType() == RobotType.GARDENER)
                        {
                            broadcastEnemyLocation(robotInfo);
                            myEnemy = robotInfo;
                            if (rc.canFireTriadShot() && !friendlyfire(robotInfo))
                            {
                                rc.fireTriadShot(rc.getLocation().directionTo(robotInfo.location));
                            }

                        }
                        else if (robotInfo.getType() == RobotType.TANK)
                        {
                            myEnemy = robotInfo;
                            if (rc.canFirePentadShot() && !friendlyfire(robotInfo))
                            {
                                rc.firePentadShot(rc.getLocation().directionTo(robotInfo.location));
                            }
                            else if (rc.canMove(here.directionTo(robotInfo.location)))
                            {
                                tryMove(here.directionTo(robotInfo.location));
                            }
                        }
                        else if (robotInfo.getType() == RobotType.LUMBERJACK && !friendlyfire(robotInfo))
                        {
                            if (rc.canFireSingleShot() && !friendlyfire(robotInfo))
                            {
                                rc.fireSingleShot(rc.getLocation().directionTo(robotInfo.location));
                            }
                            if (rc.canMove(robotInfo.location.directionTo(here)))
                            {
                                tryMove(robotInfo.location.directionTo(here));
                            }
                        }
                        else
                        {
                            if (rc.canFireTriadShot() && !friendlyfire(robotInfo))
                            {
                                rc.fireTriadShot(rc.getLocation().directionTo(robotInfo.location));
                            }
                            else if (rc.canFireSingleShot() && !friendlyfire(robotInfo))
                            {
                                rc.fireSingleShot(rc.getLocation().directionTo(robotInfo.location));
                            }
                        }
                    }
                }
                else if (visibleEnemies.length > 0 && myEnemy != null && !onOffense)
                {
                    if (rc.canSenseRobot(myEnemy.ID))
                    {
                        myEnemy = rc.senseRobot(myEnemy.ID);
                        tryMove(here.directionTo(myEnemy.location));
                        here = rc.getLocation();
                        if (rc.canFirePentadShot() && !friendlyfire(myEnemy))
                        {
                            rc.firePentadShot(rc.getLocation().directionTo(myEnemy.location));
                        }
                    }
                    else
                        myEnemy = null;
                }
                else
                {
                        respondToBroadCasts();
                }

                tryDodge();
                if (!rc.hasMoved())
                {
                    here = rc.getLocation();
                    if (onOffense)
                    {
                        if(!here.isWithinDistance(closetInitalEnemyArchonLocation(),myType.sensorRadius))
                            bugNav(closetInitalEnemyArchonLocation());
                        else
                            wander();
                    }
                    else
                        wander();
                }
                Clock.yield();

            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    private static void defensiveWander() throws GameActionException
    {
        if (!rc.hasMoved())
        {
            if (!rc.canMove(defensiveBounce))
            {
                here = rc.getLocation();
                visibleAlliedTrees = rc.senseNearbyTrees(-1, us);
                Direction dir = null;
                if (visibleAlliedTrees.length > 0)
                {
                    dir = here.directionTo(visibleAlliedTrees[0].location);
                }
                else
                {
                    dir = here.directionTo(closetInitalAlliedArchonLocation());
                }
                defensiveBounce = dir.rotateLeftDegrees((float) ((Math.random() * 180) - 90));

            }
            else
                tryMove(defensiveBounce);
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

            }
            else
                tryMove(bounce);
        }

    }

    private static boolean respondToDefensiveBroadCasts() throws GameActionException
    {
        here = rc.getLocation();
        boolean friendlyArchonUnderAttack = rc.readBroadcast(FRIENDLY_ARCHON_UNDER_ATTACK_CHANNEL) == 1;
        boolean friendlyGardenerUnderAttack = rc.readBroadcast(FRIENDLY_GARDENER_UNDER_ATTACK_CHANNEL) == 1;
        if (friendlyGardenerUnderAttack)
        {
            MapLocation friendlyGardenerLocation = new MapLocation(Float.intBitsToFloat(rc.readBroadcast(FRIENDLY_GARDENER_X)), Float.intBitsToFloat(rc.readBroadcast(FRIENDLY_GARDENER_Y)));
            if (here.isWithinDistance(friendlyGardenerLocation, RobotType.SOLDIER.sensorRadius / 2))
            {
                if (rc.senseNearbyRobots(RobotType.SOLDIER.sensorRadius / 2, them).length == 0)
                    rc.broadcast(FRIENDLY_GARDENER_UNDER_ATTACK_CHANNEL, 0);
            }
            else
                tryMove(here.directionTo(friendlyGardenerLocation));
            return true;
        }
        else if (friendlyArchonUnderAttack)
        {
            MapLocation friendlyArchonLocation = new MapLocation(Float.intBitsToFloat(rc.readBroadcast(FRIENDLY_ARCHON_X)), Float.intBitsToFloat(rc.readBroadcast(FRIENDLY_ARCHON_Y)));
            if (here.isWithinDistance(friendlyArchonLocation, RobotType.SOLDIER.sensorRadius))
            {
                if (rc.senseNearbyRobots(RobotType.SOLDIER.sensorRadius, them).length == 0)
                    rc.broadcast(FRIENDLY_ARCHON_UNDER_ATTACK_CHANNEL, 0);
            }
            else
                tryMove(here.directionTo(friendlyArchonLocation));
            return true;
        }
        return false;
    }


    private static boolean respondToBroadCasts() throws GameActionException
    {
        here = rc.getLocation();
        boolean friendlyArchonUnderAttack = rc.readBroadcast(FRIENDLY_ARCHON_UNDER_ATTACK_CHANNEL) == 1;
        boolean friendlyGardenerUnderAttack = rc.readBroadcast(FRIENDLY_GARDENER_UNDER_ATTACK_CHANNEL) == 1;
        LocId archonLocId = getBestLocationToGo(FOUND_ENEMY_ARCHON_CHANNEL, FOUND_ENEMY_ARCHON_CHANNEL_END);
        LocId gardenerLocId = getBestLocationToGo(FOUND_ENEMY_GARDENER_CHANNEL, FOUND_ENEMY_GARDENER_CHANNEL_END);
        if (friendlyArchonUnderAttack)
        {
            MapLocation friendlyArchonLocation = new MapLocation(Float.intBitsToFloat(rc.readBroadcast(FRIENDLY_ARCHON_X)), Float.intBitsToFloat(rc.readBroadcast(FRIENDLY_ARCHON_Y)));
            if (here.isWithinDistance(friendlyArchonLocation, RobotType.SOLDIER.sensorRadius))
            {
                if (rc.senseNearbyRobots(RobotType.SOLDIER.sensorRadius, them).length == 0)
                    rc.broadcast(FRIENDLY_ARCHON_UNDER_ATTACK_CHANNEL, 0);
            }
            else
                tryMove(here.directionTo(friendlyArchonLocation));
            return true;
        }
        else if (archonLocId != null)
        {
            if (here.isWithinDistance(archonLocId.location, myType.sensorRadius))
            {
                if (!rc.canSenseRobot(rc.readBroadcast(archonLocId.channelOfID)))
                {
                    rc.broadcast(archonLocId.channelOfID, 0);
                }
            }
            else
                tryMove(here.directionTo(archonLocId.location));
            return true;
        }
        else if (friendlyGardenerUnderAttack)
        {
            MapLocation friendlyGardenerLocation = new MapLocation(Float.intBitsToFloat(rc.readBroadcast(FRIENDLY_GARDENER_X)), Float.intBitsToFloat(rc.readBroadcast(FRIENDLY_GARDENER_Y)));
            if (here.isWithinDistance(friendlyGardenerLocation, RobotType.SOLDIER.sensorRadius / 2))
            {
                if (rc.senseNearbyRobots(RobotType.SOLDIER.sensorRadius / 2, them).length == 0)
                    rc.broadcast(FRIENDLY_GARDENER_UNDER_ATTACK_CHANNEL, 0);
            }
            else
                tryMove(here.directionTo(friendlyGardenerLocation));
            return true;
        }
        else if (gardenerLocId != null)
        {
            if (here.isWithinDistance(gardenerLocId.location, myType.sensorRadius))
            {
                if (!rc.canSenseRobot(rc.readBroadcast(gardenerLocId.channelOfID)))
                {
                    rc.broadcast(gardenerLocId.channelOfID, 0);
                }
            }
            else
                tryMove(here.directionTo(gardenerLocId.location));
            return true;
        }

        return false;
    }

    private static void tryDodge() throws GameActionException
    {
        bulletsInSenseRadius = rc.senseNearbyBullets(-1);
        if (!rc.hasMoved() && bulletsInSenseRadius.length > 0)
        {
            for (int i = bulletsInSenseRadius.length; i-- > 0; )
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
                    }
                    else if (rc.canMove(directionToRobot.rotateRightDegrees(90)))
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
        if (enemyInfo != null)
        {
            here = rc.getLocation();
            Direction fireDirection = here.directionTo(enemyInfo.location);
            float distanceToEnemy = here.distanceTo(enemyInfo.location);
            RobotInfo[] alliesInRange = rc.senseNearbyRobots(here.add(fireDirection, distanceToEnemy / 2), distanceToEnemy / 2, us);
            if (alliesInRange == null || alliesInRange.length == 0)
                return false;
            for (RobotInfo i : alliesInRange)
            {
                if (i.getType() == RobotType.ARCHON || i.getType() == RobotType.GARDENER)
                    return true;
            }
            if (((alliesInRange.length > 0 && alliesInRange[0].ID != rc.getID()) || alliesInRange[0].getHealth() < alliesInRange[0].getType().maxHealth / 5))
            {
                return true;
            }
            return false;
        }
        return false;
    }
}
