package TryTanks;

import battlecode.common.GameActionException;

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
    public static final int NUM_GARDENERS_CHANNEL = 100;





    public static final int URGENTLY_NEED_LUMBERJACKS_CHANNEL = 10;
    public static final int EARLY_GAME_SCOUT_SPAWNED_CHANNEL = 11;
    public static final int SCOUT_FOUND_CONTAINED_TANK_CHANNEL = 12;
    public static final int CONTAINED_TANK_X_CHANNEL = 13;
    public static final int CONTAINED_TANK_Y_CHANNEL = 14;
    public static final int FOUND_ENEMY_ARCHON_CHANNEL = 15;
    public static final int ENEMY_ARCHON_X = 16;
    public static final int ENEMY_ARCHON_Y = 17;
    public static final int FOUND_ENEMY_GARDENER_CHANNEL = 18;
    public static final int ENEMY_GARDENER_X = 19;
    public static final int ENEMY_GARDENER_Y = 20;
    public static final int FRIENDLY_ARCHON_UNDER_ATTACK_CHANNEL = 21;
    public static final int FRIENDLY_ARCHON_X = 22;
    public static final int FRIENDLY_ARCHON_Y = 23;
    public static final int FRIENDLY_GARDENER_UNDER_ATTACK_CHANNEL = 24;
    public static final int FRIENDLY_GARDENER_X = 25;
    public static final int FRIENDLY_GARDENER_Y = 26;
    public static final int NEED_TANK_GARDENER_CHANNEL = 27;

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

}
