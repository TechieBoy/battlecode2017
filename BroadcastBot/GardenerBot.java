package BroadcastBot;

import battlecode.common.*;
import static BroadcastBot.Messaging.*;

public class GardenerBot extends BaseBot
{
    private static final Direction HOLE_TOWARDS_ENEMY = here.directionTo(closetInitalEnemyArchonLocation());
    private static final int ROUND_SPAWNED = rc.getRoundNum();
    private static Direction prevDirection = Direction.getNorth().rotateRightDegrees(60);
    private static Direction bounce = here.directionTo(centerOfAllInitialArchons);

    private static int treesBuiltByMe = 0;
    private static boolean needExtraLumberjacks = false;
    private static boolean earlyLumberjack = rc.getRoundNum() < 100;
    private static boolean tankGardener = Math.random() > 0.3 && rc.getRoundNum() > 300;

    public static void runGardener() throws GameActionException
    {
        try
        {
            if (!tankGardener)
            {
                here = rc.getLocation();
                while (!(howManyTreesCanBePlanted(here) >= 4))
                {
                    here = rc.getLocation();
                    int currentRound = rc.getRoundNum();
                    if (currentRound - ROUND_SPAWNED > determineWanderDuration(currentRound))
                    {
                        if (rc.senseNearbyTrees(-1, Team.NEUTRAL).length >= 4)
                            needExtraLumberjacks = true;
                        break;

                    }
                    else if (!rc.hasMoved())
                        wander();
                }
            }
        } catch (GameActionException e)
        {
            e.printStackTrace();
        }

        while (true)

        {
            try
            {
                visibleEnemies = rc.senseNearbyRobots(4f, them);
                callForHelpIfInDangerAndDefend();
                if (rc.getHealth() < RobotType.GARDENER.maxHealth / 10 && visibleEnemies.length > 0)
                {
                    rc.broadcast(NUM_GARDENERS_CHANNEL, (rc.readBroadcast(NUM_GARDENERS_CHANNEL) - 1));

                }
                here = rc.getLocation();
                if(tankGardener)
                {
                    if(rc.getTeamBullets() > 400)
                    {
                        spawnInAnyDirectionPossible(RobotType.TANK);
                    }
                    wander();


                }
                else
                {


                    //rc.setIndicatorDot(here.add(dir, 2), 255, 0, 0);
                    if (rc.readBroadcast(EARLY_GAME_SCOUT_SPAWNED_CHANNEL) == 0)
                    {
                        if (spawnInAnyDirectionPossible(RobotType.SCOUT))
                        {
                            //rc.broadcast(NUM_SCOUTS_CHANNEL, (rc.readBroadcast(NUM_SCOUTS_CHANNEL) + 1));
                            rc.broadcast(EARLY_GAME_SCOUT_SPAWNED_CHANNEL, 1);
                            Clock.yield();
                        }
                    }

                    if (needExtraLumberjacks && rc.isBuildReady())
                    {
                        if (spawnInAnyDirectionPossible(RobotType.LUMBERJACK))
                        {
                            needExtraLumberjacks = false;
                            Clock.yield();
                        }

                    }
                    if (earlyLumberjack && rc.isBuildReady())
                    {
                        if (spawnInAnyDirectionPossible(RobotType.LUMBERJACK))
                        {
                            earlyLumberjack = false;
                            Clock.yield();
                        }
                    }


                    //Baith gaya
                    Direction dir = getNextDirection(prevDirection);
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
                        if (needScouts())
                        {
                            if (rc.hasRobotBuildRequirements(RobotType.SCOUT))
                            {
                                if (rc.canBuildRobot(RobotType.SCOUT, dir))
                                {
                                    rc.buildRobot(RobotType.SCOUT, dir);

                                }
                            }
                        }
                        else if (needLumberjacks())
                        {
                            if (rc.hasRobotBuildRequirements(RobotType.LUMBERJACK))
                            {
                                if (rc.canBuildRobot(RobotType.LUMBERJACK, dir))
                                {
                                    rc.buildRobot(RobotType.LUMBERJACK, dir);
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
                                }
                            }
                        }
                        prevDirection = dir;

                    }
                    else
                    {
                        prevDirection = dir;
                    }
                    visibleAlliedTrees = rc.senseNearbyTrees(1.5f, us);
                    if (visibleAlliedTrees.length > 0)
                    {
                        for (int i = visibleAlliedTrees.length; i-- > 0; )
                        {
                            if (rc.canWater(visibleAlliedTrees[i].ID))
                            {
                                rc.water(visibleAlliedTrees[i].ID);
                                Clock.yield();
                            }
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

    private static int determineWanderDuration(int currentRound) throws GameActionException
    {
        if (currentRound < 20)
            return 3;
        else if (currentRound < 50)
            return 10;
        else if (currentRound < 100)
            return 40;
        else if (currentRound < 200)
            return 60;
        else
            return 100;
    }

    private static void callForHelpIfInDangerAndDefend() throws GameActionException
    {
        if (visibleEnemies.length > 0)
        {
            if (rc.getHealth() < RobotType.GARDENER.maxHealth || rc.senseNearbyBullets(4f).length > 0)
            {
                rc.broadcast(FRIENDLY_GARDENER_UNDER_ATTACK_CHANNEL, 1);
                rc.broadcast(FRIENDLY_GARDENER_X, Float.floatToIntBits(rc.getLocation().x));
                rc.broadcast(FRIENDLY_GARDENER_Y, Float.floatToIntBits(rc.getLocation().y));
                spawnInAnyDirectionPossible(RobotType.LUMBERJACK);
            }
        }
    }


    private static boolean needLumberjacks() throws GameActionException
    {
        return (rc.readBroadcast(URGENTLY_NEED_LUMBERJACKS_CHANNEL) == 1) || (rc.senseNearbyTrees(-1, Team.NEUTRAL).length > 3);
    }

    private static boolean spawnInAnyDirectionPossible(RobotType robotType) throws GameActionException
    {
        for (int i = 0; i <= 360; i++)
        {
            Direction spawnDir = HOLE_TOWARDS_ENEMY.rotateLeftDegrees(i);
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
        return (rc.getRoundNum() < 100 && rc.getTreeCount() > 2);

    }

    public static boolean needSoldiers() throws GameActionException
    {
        return true;
    }


    private static Direction getNextDirection(Direction prev)
    {
        return prev.rotateLeftDegrees(60);
    }

    private static boolean haveAllPreviousGardenersBuiltTheirTrees() throws GameActionException
    {
        int numGardeners = rc.readBroadcast(NUM_GARDENERS_CHANNEL);
        int numTrees = rc.getTreeCount();
        if (numGardeners == 0 || numTrees == 0)
            return true;
        else
            return (numTrees / numGardeners >= 4);
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
            }
            else
                tryMove(bounce);
        }

    }
}
