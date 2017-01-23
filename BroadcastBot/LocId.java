package BroadcastBot;

import battlecode.common.MapLocation;

public class LocId
{
    public MapLocation location;
    public int channelOfID;

    public LocId(MapLocation location,int ID)
    {
        this.location = location;
        this.channelOfID = ID;
    }

}
