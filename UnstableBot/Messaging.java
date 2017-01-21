package UnstableBot;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;

public class Messaging extends BaseBot
{
    public static final int SUM_GARDENERS_CHANNEL = 0;
    public static final int SUM_LUMBERJACKS_CHANNEL = 1;
    public static final int SUM_SCOUTS_CHANNEL = 2;
    public static final int SUM_SOLDIERS_CHANNEL = 3;
    public static final int SUM_TANKS_CHANNEL = 4;
    public static final int TOTAL_GARDENERS_CHANNEL = 5;
    public static final int TOTAL_LUMBERJACKS_CHANNEL = 6;
    public static final int TOTAL_SCOUTS_CHANNEL = 7;
    public static final int TOTAL_TANKS_CHANNEL = 8;
    public static final int TOTAL_SOLDIERS_CHANNEL = 9;


    public static final int URGENTLY_NEED_LUMBERJACKS_CHANNEL = 10;
    public static final int EARLY_GAME_SCOUT_SPAWNED_CHANNEL = 11;
    public static final int SCOUT_FOUND_CONTAINED_TANK_CHANNEL = 12;
    public static final int CONTAINED_TANK_X_CHANNEL = 13;
    public static final int CONTAINED_TANK_Y_CHANNEL = 14;
    public static final int NEED_TANK_GARDENER_CHANNEL = 15;
    public static final int NUM_GARDENERS_CHANNEL = 16;

    public static final int FOUND_ENEMY_ARCHON_CHANNEL = 100;
    public static final int ENEMY_ARCHON_X = 101;
    public static final int ENEMY_ARCHON_Y = 102;
    public static final int FOUND_ENEMY_ARCHON_CHANNEL_END = 110;

    public static final int FOUND_ENEMY_GARDENER_CHANNEL = 110;
    public static final int ENEMY_GARDENER_X = 111;
    public static final int ENEMY_GARDENER_Y = 112;
    public static final int FOUND_ENEMY_GARDENER_CHANNEL_END = 250;

    public static final int FRIENDLY_ARCHON_UNDER_ATTACK_CHANNEL = 250;
    public static final int FRIENDLY_ARCHON_X = 251;
    public static final int FRIENDLY_ARCHON_Y = 252;
    public static final int FRIENDLY_ARCHON_UNDER_ATTACK_CHANNEL_END = 260;


    public static final int FRIENDLY_GARDENER_UNDER_ATTACK_CHANNEL = 260;
    public static final int FRIENDLY_GARDENER_X = 261;
    public static final int FRIENDLY_GARDENER_Y = 262;
    public static final int FRIENDLY_GARDENER_UNDER_ATTACK_CHANNEL_END = 260;



    public static void safelyUpdateBroadcast(int channel,int value) throws GameActionException
    {
        if(rc.getRoundNum()%10 == 0)
        {
            rc.broadcast(channel,value);
        }
    }

    public static int safelyReadBroadcast(int channel) throws GameActionException
    {
        if(rc.getRoundNum()%10 > 0 && rc.getRoundNum()%10 < 9)
        {
            return rc.readBroadcast(channel);
        }
        return 0;
    }

    public static void safelyClearBroadcast(int channel) throws GameActionException
    {
        if(rc.getRoundNum()%10 == 9)
        {
            rc.broadcast(channel,0);
        }
    }

    public static void updateRobotCount(int sumChannel) throws GameActionException
    {
        rc.broadcast(sumChannel,(1+rc.readBroadcast(sumChannel)));
    }

    public static void archonUpdateRobotCounts() throws GameActionException
    {
            rc.broadcast(TOTAL_GARDENERS_CHANNEL, rc.readBroadcast(SUM_GARDENERS_CHANNEL));
            rc.broadcast(SUM_GARDENERS_CHANNEL, 0);

    }

    public static void broadcastEnemyLocation(RobotInfo robotInfo) throws GameActionException
    {
        switch (robotInfo.type)
        {
            case ARCHON:
                broadcastAtRightChannel(FOUND_ENEMY_ARCHON_CHANNEL,FOUND_ENEMY_ARCHON_CHANNEL_END,robotInfo);
                break;
            case GARDENER:
                broadcastAtRightChannel(FOUND_ENEMY_GARDENER_CHANNEL,FOUND_ENEMY_GARDENER_CHANNEL_END,robotInfo);
                break;
        }

    }

    private static void broadcastAtRightChannel(int channelStart,int channelEnd,RobotInfo robotInfo) throws GameActionException
    {
        int i = channelStart;
        while(rc.readBroadcast(i)!=0 && i!= channelEnd)
        {
            i+=3;
        }
        if(i<channelEnd)
        {
            rc.broadcast(i, Float.floatToIntBits(robotInfo.ID));
            rc.broadcast(i + 1, Float.floatToIntBits(robotInfo.location.x));
            rc.broadcast(i + 2, Float.floatToIntBits(robotInfo.location.y));
        }
    }

    public static int respondToParticularBroadcasts(RobotType myType, int channelStart,int channelEnd,RobotType enemyType,int offset) throws GameActionException
    {
        int o = offset;
        for(int i = channelStart+o;i<channelEnd;i+=3)
        {
            if(rc.readBroadcast(i)!=0)
            {
                MapLocation mapLocation = new MapLocation(Float.intBitsToFloat(rc.readBroadcast(i+1)),Float.intBitsToFloat(rc.readBroadcast(i+2)));
                if(here.isWithinDistance(mapLocation,myType.sensorRadius))
                {

                    boolean found = false;
                    RobotInfo[] enemies = rc.senseNearbyRobots(-1,them);
                    for(RobotInfo enemy:enemies)
                    {
                        if(enemy.type == enemyType)
                        {
                            found = true;
                            break;
                        }
                    }
                    if(!found)
                        o += 3;
                }
                else
                {
                    tryMove(here.directionTo(mapLocation));
                    break;
                }
            }
        }
        if(o > channelEnd-channelStart-2)
        {
            o = 0;
        }
        return o;
    }



}
