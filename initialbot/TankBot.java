package initialbot;

import battlecode.common.Clock;
import battlecode.common.GameActionException;

public class TankBot extends BaseBot
{

    public static void runTank() throws GameActionException
    {
        System.out.println("I'm a Tank!");
        while(true)
        {
            Clock.yield();
        }

    }
}
