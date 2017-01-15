package counterattack;

import battlecode.common.*;

public class GardenerBot extends BaseBot
{
    private static final Direction HOLE_TOWARDS_ENEMY = here.directionTo(closetInitalEnemyArchonLocation());
    private static final int ROUND_SPAWNED = rc.getRoundNum();
    private static Direction prevDirection = Direction.getNorth().rotateRightDegrees(60);
    private static Direction bounce = here.directionTo(centerOfAllInitialArchons);

    private static int treesBuiltByMe = 0;
    private static boolean urgentlyNeedLumberJacks = false;
    private static boolean justSpawnedLumberJack = false;
    private static boolean justSpawnedSoldier = false;

    public static void runGardener() throws GameActionException
    {
        try
        {

            visibleEnemies = rc.senseNearbyRobots(-1,them);
            callForHelpIfInDanger();
            if(rc.getHealth() < RobotType.GARDENER.maxHealth/10 && visibleEnemies.length>0)
            {
                rc.broadcast(NUM_GARDENERS_CHANNEL,(rc.readBroadcast(NUM_GARDENERS_CHANNEL)-1));

            }
            while (true)
            {

                here = rc.getLocation();
                if ((howManyTreesCanBePlanted(here) >= 4) && here.distanceTo(closetInitalAlliedArchonLocation()) > 7)
                {
                    break;
                }
                if (rc.getRoundNum() - ROUND_SPAWNED > (rc.getRoundNum()/4) && ROUND_SPAWNED > 10)
                {
                    if(rc.senseNearbyTrees(-1,Team.NEUTRAL).length > 2)
                        urgentlyNeedLumberJacks = true;
                    break;
                } else if (!rc.hasMoved())
                    wander();
            }
        } catch (GameActionException e)
        {
            e.printStackTrace();
        }

        while (true)
        {
            try
            {
                Direction dir = getNextDirection(prevDirection);
                here = rc.getLocation();
                rc.setIndicatorDot(here.add(dir, 2), 255, 0, 0);
                if (rc.readBroadcast(EARLY_GAME_SCOUT_SPAWNED_CHANNEL) == 0)
                {
                    if(spawnInAnyDirectionPossible(RobotType.SCOUT)){
                        rc.broadcast(NUM_SCOUTS_CHANNEL,(rc.readBroadcast(NUM_SCOUTS_CHANNEL)+1));
                    }
                }
                else  if (urgentlyNeedLumberJacks)
                {
                    if(spawnInAnyDirectionPossible(RobotType.LUMBERJACK))
                    {
                        rc.broadcast(NUM_LUMBERJACKS_CHANNEL, rc.readBroadcast(NUM_LUMBERJACKS_CHANNEL) + 1);
                        urgentlyNeedLumberJacks = false;
                    }

                }

                if (Math.abs(dir.degreesBetween(HOLE_TOWARDS_ENEMY)) >= 30)
                {
                    if (rc.hasTreeBuildRequirements() && (haveAllPreviousGardenersBuiltTheirTrees() || treesBuiltByMe < 5))
                    {
                        if (rc.canPlantTree(dir))
                        {
                            rc.plantTree(dir);
                            treesBuiltByMe++;
                        }
                    }
                    prevDirection = dir;
                }
                else if (Math.abs(dir.degreesBetween(HOLE_TOWARDS_ENEMY)) < 30 && rc.getTreeCount() >= 2)
                {

                    if(needScouts())
                    {
                        if (rc.hasRobotBuildRequirements(RobotType.SCOUT))
                        {
                            if (rc.canBuildRobot(RobotType.SCOUT, dir))
                            {
                                rc.buildRobot(RobotType.SCOUT, dir);
                                justSpawnedSoldier = false;
                                justSpawnedLumberJack = false;

                            }
                        }
                    }

                    else if (needSoldiers())
                    {
                        if (rc.hasRobotBuildRequirements(RobotType.SOLDIER))
                        {
                            if (rc.canBuildRobot(RobotType.SOLDIER, dir))
                            {
                                rc.buildRobot(RobotType.SOLDIER, dir);
                                justSpawnedSoldier = true;
                                justSpawnedLumberJack = false;
                            }
                        }
                    } else if(needLumberjacks())
                    {
                        if (rc.hasRobotBuildRequirements(RobotType.LUMBERJACK))
                        {
                            if (rc.canBuildRobot(RobotType.LUMBERJACK, dir))
                            {
                                rc.buildRobot(RobotType.LUMBERJACK, dir);
                                rc.broadcast(NUM_LUMBERJACKS_CHANNEL, rc.readBroadcast(NUM_LUMBERJACKS_CHANNEL) + 1);
                                justSpawnedLumberJack = true;
                                justSpawnedSoldier = false;

                            }
                        }
                    }
                    prevDirection = dir;

                } else
                {
                    prevDirection = dir;
                }
                visibleAlliedTrees = rc.senseNearbyTrees(1.5f, us);
                if (visibleAlliedTrees.length > 0)
                {
                    for (int i = visibleAlliedTrees.length; i-- > 0; )
                    {
                        if (rc.canWater(visibleAlliedTrees[i].getID()))
                        {
                            rc.water(visibleAlliedTrees[i].getID());
                            Clock.yield();
                        }
                    }
                }
                Clock.yield();
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    private static void callForHelpIfInDanger() throws GameActionException
    {
        if(rc.getHealth() < RobotType.GARDENER.maxHealth/1.5 && visibleEnemies.length > 0)
        {
            rc.broadcast(FRIENDLY_GARDENER_UNDER_ATTACK_CHANNEL,1);
            rc.broadcast(FRIENDLY_GARDENER_X,(int)rc.getLocation().x);
            rc.broadcast(FRIENDLY_GARDENER_Y,(int)rc.getLocation().y);
        }
        else
        {
            rc.broadcast(FRIENDLY_GARDENER_UNDER_ATTACK_CHANNEL,0);
        }
    }


    private static boolean needLumberjacks() throws GameActionException
    {
        return  (rc.readBroadcast(NUM_LUMBERJACKS_CHANNEL) < 100) || (justSpawnedSoldier);
    }

    private static boolean spawnInAnyDirectionPossible(RobotType robotType) throws GameActionException
    {
        Direction spawnDir = Direction.getEast();
        for (int i = 0; i <= 360; i++)
        {
            if (rc.canBuildRobot(robotType, spawnDir))
            {
                if (rc.hasRobotBuildRequirements(robotType))
                {
                    rc.buildRobot(robotType, spawnDir);
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean needScouts() throws GameActionException
    {
        return  (rc.senseNearbyTrees(-1,Team.NEUTRAL).length > 4);

    }

    public static boolean needSoldiers() throws GameActionException
    {
        visibleEnemies = rc.senseNearbyRobots(-1, them);
        return (visibleEnemies.length > 0) || (justSpawnedLumberJack);

    }


    private static Direction getNextDirection(Direction prev)
    {
        return prev.rotateLeftDegrees(60);
    }

    private static boolean haveAllPreviousGardenersBuiltTheirTrees() throws GameActionException
    {
        int numGardeners = rc.readBroadcast(NUM_GARDENERS_CHANNEL);
        int numTrees = rc.getTreeCount();
        return (numTrees / numGardeners >= 4 || numGardeners == 0 || numTrees == 0);
    }

    private static int howManyTreesCanBePlanted(MapLocation location)
    {
        int howMany = 0;
        Direction dir = location.directionTo(closetInitalEnemyArchonLocation());
        for (int i = 0; i <= 360; i += 60)
        {
            if (rc.canPlantTree(dir.rotateLeftDegrees(i)) && rc.senseNearbyRobots(3.8f, us).length == 0 &&
                    rc.senseNearbyTrees(5f).length == 0)
            {
                howMany++;
            }
        }
        return howMany;
    }

    private static void wander() throws GameActionException
    {
        if (!rc.hasMoved())
        {
            if (!rc.canMove(bounce))
            {
                bounce = randomDirection();
            } else
                tryMove(bounce);
        }

    }
}
