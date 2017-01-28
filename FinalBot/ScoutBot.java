package FinalBot;

import battlecode.common.*;

import static FinalBot.Messaging.*;

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

            }
            else
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
                if(rc.getRoundNum() < 320)
                {
                    findTreeBulletsAndTanks();
                }
                visibleEnemies = rc.senseNearbyRobots(-1, them);
                if (visibleEnemies.length > 0)
                {
                    for (RobotInfo robotInfo : visibleEnemies)
                    {
                        if(robotInfo.getType()==RobotType.SOLDIER)
                        {
                            if(willMyBulletHit(robotInfo))
                            {
                                tryMove(robotInfo.location.directionTo(here),5,36);
                                if(rc.canFireSingleShot())
                                {
                                    rc.fireSingleShot(here.directionTo(robotInfo.location));
                                }
                            }
                            else if(here.distanceTo(robotInfo.location)>robotInfo.getType().sensorRadius)
                            {
                                tryMove(here.directionTo(robotInfo.location));
                            }
                        }
                        else if(robotInfo.getType()==RobotType.LUMBERJACK)
                        {
                            if(here.distanceTo(robotInfo.location)>robotInfo.getType().sensorRadius)
                            {
                                tryMove(here.directionTo(robotInfo.location));
                            }
                            else
                            {
                                tryMove(robotInfo.location.directionTo(here),5,36);
                                if(willMyBulletHit(robotInfo) && rc.canFireSingleShot())
                                {
                                    rc.fireSingleShot(here.directionTo(robotInfo.location));
                                }
                            }
                        }
                        else if (robotInfo.getType() == RobotType.ARCHON)
                        {
                            broadcastEnemyLocation(robotInfo);
                            if (rc.canFireSingleShot() && rc.getTreeCount() > 1)
                            {
                                rc.fireSingleShot(rc.getLocation().directionTo(robotInfo.location));
                            }
                            if(!rc.hasMoved() && here.distanceTo(robotInfo.location)>2*myType.strideRadius)
                            {
                                tryMove(here.directionTo(robotInfo.location));
                            }
                        }
                        else if(robotInfo.getType()== RobotType.TANK)
                        {
                            broadcastEnemyLocation(robotInfo);
                            if(!rc.hasMoved())
                            {
                                tryMove(robotInfo.location.directionTo(here),5,36);
                            }
                        }
                        else if (robotInfo.getType() == RobotType.GARDENER)
                        {
                            broadcastEnemyLocation(robotInfo);
                            if(here.distanceTo(robotInfo.location)>2.5*robotInfo.getRadius())
                            {
                                tryMove(here.directionTo(robotInfo.location));
                            }
                            else
                            {
                                visibleEnemyTrees = rc.senseNearbyTrees(-1, them);
                                if(visibleEnemyTrees.length>0)
                                {
                                    MapLocation treeLocation= visibleEnemyTrees[0].location;
                                    if(here.distanceTo(treeLocation)< rc.getType().strideRadius)
                                    {
                                        if(here.distanceTo(treeLocation)>0.01f && !rc.hasMoved() && rc.canMove(here.directionTo(treeLocation), here.distanceTo(treeLocation)))
                                        {
                                            rc.move(here.directionTo(treeLocation), here.distanceTo(treeLocation));
                                        }
                                        else
                                        {
                                            if(rc.canFireSingleShot())
                                            {
                                                rc.fireSingleShot(here.directionTo(robotInfo.location).rotateLeftDegrees(25));
                                            }
                                        }
                                    }
                                    else
                                    {
                                        tryMove(here.directionTo(treeLocation));
                                    }
                                }
                                else
                                {
                                    if (rc.canFireSingleShot())
                                    {
                                        rc.fireSingleShot(rc.getLocation().directionTo(robotInfo.location));
                                    }
                                    else if (!rc.hasMoved())
                                    {
                                        tryMove(here.directionTo(robotInfo.location));
                                    }
                                }
                            }
                        }
                    }
                }
                if (!rc.hasMoved())
                {
                    if (rc.getRoundNum() < 320)
                        findTreeBulletsAndTanks();
                    if(!rc.hasMoved())
                        respondToBroadCasts();
                    if(!rc.hasMoved())
                       wander();
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
        LocId archonLocId = getBestLocationToGo(FOUND_ENEMY_ARCHON_CHANNEL, FOUND_ENEMY_ARCHON_CHANNEL_END);
        LocId gardenerLocId = getBestLocationToGo(FOUND_ENEMY_GARDENER_CHANNEL, FOUND_ENEMY_GARDENER_CHANNEL_END);
        if (archonLocId != null)
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
        else if(gardenerLocId!=null)
        {
            if(here.isWithinDistance(gardenerLocId.location,myType.sensorRadius))
            {
                if(!rc.canSenseRobot(rc.readBroadcast(gardenerLocId.channelOfID)))
                {
                    rc.broadcast(gardenerLocId.channelOfID,0);
                }
                boolean found = false;
                visibleEnemies = rc.senseNearbyRobots(-1,them);
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
        else if (friendlyArchonUnderAttack)
        {
            MapLocation friendlyArchonLocation = new MapLocation(Float.intBitsToFloat(rc.readBroadcast(FRIENDLY_ARCHON_X)), Float.intBitsToFloat(rc.readBroadcast(FRIENDLY_ARCHON_Y)));
            if (here.isWithinDistance(friendlyArchonLocation, RobotType.SCOUT.sensorRadius))
            {
                if (rc.senseNearbyRobots(RobotType.SCOUT.sensorRadius, them).length == 0)
                    rc.broadcast(FRIENDLY_ARCHON_UNDER_ATTACK_CHANNEL, 0);
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
                    }
                    else
                    {
                        if (!rc.hasMoved())
                            tryMove(here.directionTo(i.location));
                    }
                }
            }
        }

    }
}
