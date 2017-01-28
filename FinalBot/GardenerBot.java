package FinalBot;

import battlecode.common.*;

import static FinalBot.Messaging.*;

public class GardenerBot extends BaseBot
{
    private static final Direction HOLE_TOWARDS_ENEMY = here.directionTo(closetInitalEnemyArchonLocation());
    private static final int ROUND_SPAWNED = rc.getRoundNum();
    private static Direction bounce = here.directionTo(centerOfAllInitialArchons);

    private static Direction unitSpawnDirection = HOLE_TOWARDS_ENEMY;
    private static Direction[] treeSpawnDirections = null;

    private static int treesBuiltByMe = 0;
    private static boolean needExtraLumberjacks = false;
    private static boolean earlySoldier = rc.getRoundNum() < 100;
    private static boolean earlyLumberjack = rc.getRoundNum() < 100;
    private static boolean tankGardener = Math.random() > 0.4 && rc.getRoundNum() > 450;

    public static void runGardener() throws GameActionException
    {
        try
        {
            generateDirections();
            if (!tankGardener)
            {
                here = rc.getLocation();
                if(earlySoldier)
                {
                    int treesInPath = rc.senseNearbyTrees(here.add(HOLE_TOWARDS_ENEMY, myType.sensorRadius), -1, Team.NEUTRAL).length;
                    if (treesInPath > 5)
                    {
                        earlyLumberjack = true;
                        earlySoldier = false;
                    }
                }
                MapLocation bestLocationSoFar = here;
                int numTrees = howManyTreesCanBePlanted(here);
                int maxSoFar = numTrees;
                boolean brokeEarly = false;
                while (numTrees < 4)
                {

                    int currentRound = rc.getRoundNum();
                    if (currentRound - ROUND_SPAWNED > determineWanderDuration(currentRound))
                    {
                        if (rc.senseNearbyTrees(-1, Team.NEUTRAL).length >= 4)
                            needExtraLumberjacks = true;
                        brokeEarly = true;
                        break;

                    }
                    else if (!rc.hasMoved())
                    {
                        tryDodge();
                        wander();
                        Clock.yield();
                    }
                    here = rc.getLocation();
                    numTrees = howManyTreesCanBePlanted(here);
                    if (numTrees > maxSoFar)
                    {
                        maxSoFar = numTrees;
                        bestLocationSoFar = here;
                    }

                }
                if (brokeEarly)
                {
                    if (numTrees < maxSoFar)
                    {
                        here = rc.getLocation();
                        while (!here.isWithinDistance(bestLocationSoFar, myType.bodyRadius))
                        {
                            here = rc.getLocation();
                            tryDodge();
                            tryMove(here.directionTo(bestLocationSoFar));
                            Clock.yield();
                        }
                    }
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
                visibleEnemies = rc.senseNearbyRobots(-1, them);
                callForHelpIfInDangerAndDefend();
                if (rc.getHealth() < RobotType.GARDENER.maxHealth / 10 && visibleEnemies.length > 0)
                {
                    rc.broadcast(GARDENER_ABOUT_TO_DIE_CHANNEL,1);
                    rc.broadcast(NUM_GARDENERS_CHANNEL, (rc.readBroadcast(NUM_GARDENERS_CHANNEL) - 1));

                }
                here = rc.getLocation();
                if (tankGardener)
                {
                    if (rc.getTeamBullets() > 400)
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
                    else
                    {
                        if(rc.getRoundNum() < 150 && rc.isBuildReady() && !needExtraLumberjacks)
                        {
                            spawnInAnyDirectionPossible(RobotType.SOLDIER);
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
                    if (earlySoldier && rc.isBuildReady())
                    {
                        if (spawnInAnyDirectionPossible(RobotType.SOLDIER))
                        {
                            earlySoldier = false;
                            Clock.yield();
                        }
                    }
                    else
                    {
                        if(earlyLumberjack && rc.isBuildReady())
                        {
                            if(spawnInAnyDirectionPossible(RobotType.LUMBERJACK))
                            {
                                earlyLumberjack = false;
                                Clock.yield();
                            }
                        }
                    }


                    //Baith gaya
                    for (int i = treeSpawnDirections.length; i-- > 0; )
                    {
                        if (rc.hasTreeBuildRequirements())
                        {
                            if (rc.canPlantTree(treeSpawnDirections[i]))
                            {
                                rc.plantTree(treeSpawnDirections[i]);
                                treesBuiltByMe++;
                                break;
                            }
                        }
                    }

                    if (needScouts())
                    {
                        if (rc.hasRobotBuildRequirements(RobotType.SCOUT))
                        {
                            if (rc.canBuildRobot(RobotType.SCOUT, unitSpawnDirection))
                            {
                                rc.buildRobot(RobotType.SCOUT, unitSpawnDirection);

                            }
                        }
                    }
                    else if (needLumberjacks())
                    {
                        if (rc.hasRobotBuildRequirements(RobotType.LUMBERJACK))
                        {
                            if (rc.canBuildRobot(RobotType.LUMBERJACK, unitSpawnDirection))
                            {
                                rc.buildRobot(RobotType.LUMBERJACK, unitSpawnDirection);
                            }
                        }
                    }
                    else if (needSoldiers())
                    {
                        if (rc.hasRobotBuildRequirements(RobotType.SOLDIER))
                        {
                            if (rc.canBuildRobot(RobotType.SOLDIER, unitSpawnDirection))
                            {
                                rc.buildRobot(RobotType.SOLDIER, unitSpawnDirection);
                            }
                        }
                    }


                    visibleAlliedTrees = rc.senseNearbyTrees(1.5f, us);
                    if (visibleAlliedTrees.length > 0)
                    {
                        float minHealth = Float.MAX_VALUE;
                        int minTreeID = visibleAlliedTrees[0].ID;

                        for (int i = visibleAlliedTrees.length; i-- > 0; )
                        {
                            TreeInfo tree = visibleAlliedTrees[i];
                            if(tree.health < minHealth){
                                minHealth = tree.health;
                                minTreeID = tree.ID;
                            }

                        }
                        if (rc.canWater(minTreeID))
                        {
                            rc.water(minTreeID);
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

    private static void generateDirections()
    {
        treeSpawnDirections = new Direction[5];
        treeSpawnDirections[0] = HOLE_TOWARDS_ENEMY.rotateLeftDegrees(60);
        for (int i = 1; i < 5; i++)
        {
            treeSpawnDirections[i] = treeSpawnDirections[i - 1].rotateLeftDegrees(60);
        }
    }

    private static int determineWanderDuration(int currentRound) throws GameActionException
    {
        if (currentRound < 20)
            return 4;
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
                rc.broadcast(FRIENDLY_GARDENER_UNDER_ATTACK_CHANNEL, 1);
                rc.broadcast(FRIENDLY_GARDENER_X, Float.floatToIntBits(rc.getLocation().x));
                rc.broadcast(FRIENDLY_GARDENER_Y, Float.floatToIntBits(rc.getLocation().y));
                for (RobotInfo robotInfo : visibleEnemies)
                {
                    if (robotInfo.getType() == RobotType.SCOUT)
                    {
                        spawnInAnyDirectionPossible(RobotType.LUMBERJACK);
                        break;
                    }
                    else
                    {
                        spawnInAnyDirectionPossible(RobotType.SOLDIER);
                        break;
                    }
                }

        }
    }


    private static boolean needLumberjacks() throws GameActionException
    {
        if (rc.readBroadcast(URGENTLY_NEED_LUMBERJACKS_CHANNEL) == 1)
        {
            rc.broadcast(URGENTLY_NEED_LUMBERJACKS_CHANNEL, 0);
            return true;
        }
        return (rc.senseNearbyTrees(-1, Team.NEUTRAL).length > 3);
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


    private static int howManyTreesCanBePlanted(MapLocation location)
    {
        int howMany = 0;
        for (Direction d : treeSpawnDirections)
        {
            if (rc.canPlantTree(d) && rc.senseNearbyRobots(3.8f, us).length == 0 && rc.senseNearbyTrees(5f).length == 0)
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

    private static void tryDodge() throws GameActionException
    {
        bulletsInSenseRadius = rc.senseNearbyBullets();
        visibleEnemies = rc.senseNearbyRobots(-1,them);
        if (bulletsInSenseRadius.length > 0)
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
                    } else if (rc.canMove(directionToRobot.rotateRightDegrees(90)))
                    {
                        tryMove(directionToRobot.rotateRightDegrees(90));
                    }
                    break;
                }
            }
        } else if (!rc.hasMoved() && visibleEnemies.length > 0)
        {
            here = rc.getLocation();
            RobotInfo firstEnemy = visibleEnemies[0];
            Direction oppToFirstEnemy = firstEnemy.location.directionTo(here);
            if(rc.canMove(oppToFirstEnemy) && !rc.hasMoved()){
                tryMove(oppToFirstEnemy);
            }
        }
    }
}
