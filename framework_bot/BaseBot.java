package framework_bot;

import battlecode.common.*;


public class BaseBot {
    public static RobotController rc;
    public static MapLocation here;
    public static Team us;
    public static Team them;
    public static int myID;
    public static RobotType myType;
    public static int roundNum;

    public static int numberOfInitialArchon;

    public static MapLocation[] ourInitialArchonLocations;
    public static MapLocation[] theirInitialArchonLocations;

    public static MapLocation centerOfOurInitialArchons;
    public static MapLocation centerOfTheirInitialArchons;
    public static MapLocation centerOfAllInitialArchons;

    public static RobotInfo[] visibleEnemies = null;
    public static RobotInfo[] visibleAllies = null;

    public static TreeInfo[] visibleAlliedTrees = null;
    public static TreeInfo[] visibleEnemyTrees = null;
    public static TreeInfo[] visibleNeutralTrees = null;

    public static BulletInfo[] bulletsInSenseRadius = null;

    public static void init(RobotController myRC)
    {
        rc = myRC;
        us = rc.getTeam();
        them = us.opponent();
        myID = rc.getID();
        myType = rc.getType();
        ourInitialArchonLocations = rc.getInitialArchonLocations(us);
        theirInitialArchonLocations = rc.getInitialArchonLocations(them);
        numberOfInitialArchon = ourInitialArchonLocations.length;
        centerOfOurInitialArchons = new MapLocation(0,0);
        centerOfTheirInitialArchons = new MapLocation(0,0);
        centerOfAllInitialArchons = new MapLocation(0,0);
        for (MapLocation a : ourInitialArchonLocations)
        {
            centerOfOurInitialArchons = FastMath.addVec(centerOfOurInitialArchons, a);
        }
        for (MapLocation a : theirInitialArchonLocations)
        {
            centerOfTheirInitialArchons = FastMath.addVec(centerOfTheirInitialArchons, a);
        }
        centerOfAllInitialArchons = FastMath.addVec(centerOfOurInitialArchons, centerOfTheirInitialArchons);
        centerOfOurInitialArchons = FastMath.multiplyVec(1.0 / (double)numberOfInitialArchon, centerOfOurInitialArchons);
        centerOfTheirInitialArchons = FastMath.multiplyVec(1.0 / (double)numberOfInitialArchon, centerOfTheirInitialArchons);
        centerOfAllInitialArchons = FastMath.multiplyVec(0.5 / (double)numberOfInitialArchon, centerOfAllInitialArchons);
        here = rc.getLocation();
    }

    public static MapLocation closetInitalEnemyArchonLocation()
    {
        MapLocation ret = null;
        float minDistSq = Float.MAX_VALUE;
        for (int i = theirInitialArchonLocations.length; i --> 0; ) {
            float distSq = here.distanceSquaredTo(theirInitialArchonLocations[i]);
            if (distSq < minDistSq) {
                minDistSq = distSq;
                ret = theirInitialArchonLocations[i];
            }
        }
        return ret;
    }

    public static void update()
    {
        here = rc.getLocation();
        roundNum = rc.getRoundNum();
    }

    public static void updateRobotInfos()
    {
        visibleAllies = rc.senseNearbyRobots(-1, us);
        visibleEnemies =  rc.senseNearbyRobots(-1, them);
        visibleAlliedTrees = rc.senseNearbyTrees(-1,us);
        visibleEnemyTrees = rc.senseNearbyTrees(-1,them);
        visibleNeutralTrees = rc.senseNearbyTrees(-1,Team.NEUTRAL);
        bulletsInSenseRadius = rc.senseNearbyBullets();
    }

}
