package FinalBot;

import battlecode.common.*;

import static FinalBot.Messaging.*;

public class SoldierBot extends BaseBot
{
    private static Direction bounce = here.directionTo(closetInitalEnemyArchonLocation());
    private static Direction defensiveBounce = here.directionTo(closetInitalAlliedArchonLocation());
    private static RobotInfo myEnemy = null;
    private static MapLocation[] broadcastingLocations;
    private static boolean onDeffense = rc.getRoundNum() < 155;
    private static int enemyArchonNumber = 0;

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
                    RobotInfo robotInfo = visibleEnemies[0];

                    if (robotInfo.getHealth() <= robotInfo.getType().maxHealth * 0.4)
                        myEnemy = robotInfo;

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
                        else if (rc.canFireSingleShot())
                        {
                            rc.fireSingleShot(rc.getLocation().directionTo(robotInfo.location));
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
                    else if (robotInfo.getType() == RobotType.LUMBERJACK)
                    {
                        tryMove(robotInfo.location.directionTo(here), 5, 30);
                        here = rc.getLocation();
                        float midDistance = here.distanceTo(robotInfo.location) / 2;
                        MapLocation midLoc = here.add(here.directionTo(robotInfo.location), midDistance);
                        if (rc.senseNearbyRobots(midLoc, myType.sensorRadius, them).length > 1)
                        {
                            if (rc.canFireTriadShot())
                            {
                                rc.fireTriadShot(rc.getLocation().directionTo(robotInfo.location));
                            }
                        }
                        else if (rc.canFireSingleShot())
                        {
                            rc.fireSingleShot(rc.getLocation().directionTo(robotInfo.location));
                        }
                    }
                    else if (robotInfo.getType() == RobotType.SOLDIER)
                    {

                        if (here.distanceTo(robotInfo.location) < 3 * myType.strideRadius)
                        {
                            tryMove(robotInfo.location.directionTo(here));
                        }
                        else
                        {
                            bugNav(robotInfo.location);
                        }
                        if (rc.canFirePentadShot() && !friendlyfire(robotInfo))
                        {
                            rc.firePentadShot(here.directionTo(robotInfo.location));
                        }
                        else if (rc.canFireSingleShot())
                        {
                            rc.fireSingleShot(here.directionTo(robotInfo.location));
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
                else if (visibleEnemies.length > 0 && myEnemy != null)
                {
                    if (rc.canSenseRobot(myEnemy.ID))
                    {
                        here = rc.getLocation();
                        myEnemy = rc.senseRobot(myEnemy.ID);
                        if (here.distanceTo(myEnemy.location) > myType.strideRadius)
                            bugNav(myEnemy.location);
                        if (rc.canFirePentadShot() && !friendlyfire(myEnemy))
                        {
                            rc.firePentadShot(rc.getLocation().directionTo(myEnemy.location));
                        }
                        else if (rc.canFireSingleShot())
                        {
                            rc.fireSingleShot(rc.getLocation().directionTo(myEnemy.location));
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
                    if (onDeffense)
                    {
                        defensiveWander();
                    }
                    else
                    {
                        if (enemyArchonNumber > (numberOfInitialArchon - 1))
                            wander();
                        else if (here.distanceTo(theirInitialArchonLocations[enemyArchonNumber]) > myType.bodyRadius)
                            bugNav(theirInitialArchonLocations[enemyArchonNumber]);
                        else
                            enemyArchonNumber++;
                    }
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
            here = rc.getLocation();
            if(broadcastingLocations == null || rc.getRoundNum()%50 >= 30)
                broadcastingLocations = rc.senseBroadcastingRobotLocations();
            if(broadcastingLocations.length > 0)
            {
                for(MapLocation loc : broadcastingLocations)
                {
                    if(!here.isWithinDistance(loc,myType.strideRadius))
                    {
                        tryMove(here.directionTo(loc));
                        break;
                    }
                }

            }
            else
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
            else if (here.distanceTo(friendlyArchonLocation) > 35 * myType.strideRadius)
                return false;
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
                boolean found = false;
                visibleEnemies = rc.senseNearbyRobots(-1,them);
                for (RobotInfo i : visibleEnemies)
                {
                    if (i.getType() == RobotType.ARCHON)
                    {
                        rc.broadcast(archonLocId.channelOfID, i.ID);
                        found = true;
                        break;
                    }
                }
                if(!found)
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
            else if (here.distanceTo(friendlyGardenerLocation) > 35 * myType.strideRadius)
                return false;
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
                visibleEnemies = rc.senseNearbyRobots(-1, them);

                boolean found = false;
                for (RobotInfo i : visibleEnemies)
                {
                    if (i.getType() == RobotType.GARDENER)
                    {
                        rc.broadcast(gardenerLocId.channelOfID, i.ID);
                        found = true;
                        break;
                    }
                }
                if(!found)
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
            if (alliesInRange.length == 1 && alliesInRange[0].ID == rc.getID())
            {
                return false;
            }
            if (alliesInRange[0].health < alliesInRange[0].getType().maxHealth / 10)
                return true;
            else
                return false;
        }
        return false;
    }
}
