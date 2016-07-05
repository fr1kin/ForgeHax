package com.matt.forgehax.util;

import net.minecraft.client.multiplayer.ServerData;

public class ServerQueueManager {
    private final ServerData joiningServer;

    private int initPos = -1;
    private long initJoinTime = -1;

    private int pos = -1;

    public ServerQueueManager(ServerData data) {
        joiningServer = data;
    }

    /**
     * Gets pos in the queue
     */
    public int getPos() {
        return pos;
    }

    /**
     * Updates current queue position
     */
    public void setPos(int pos) {
        if(needsInitPos() || hasLostPlaceInQueue(pos)) {
            this.pos = initPos = pos;
            initJoinTime = System.currentTimeMillis();
        } else this.pos = pos;
    }

    /**
     * Check if the pos has been initially set
     */
    public boolean needsInitPos() {
        return this.pos == -1;
    }

    /**
     * Checks if the new queue position is greater than the last known queue pos
     * this indicates you have lost your slot
     */
    public boolean hasLostPlaceInQueue(int pos) {
        return this.pos > pos;
    }

    public boolean isFirst() {
        return pos == 1;
    }

    public long getEstimatedTime() {
        long deltaTime = System.currentTimeMillis() - initJoinTime;
        long deltaPos = initPos - pos;
        return deltaTime / (deltaPos+1);
    }

    /**
     * not abuse
     */
    @Override
    public boolean equals(Object o) {
        if(o instanceof ServerData)
            return joiningServer.serverIP.equals(((ServerData) o).serverIP);
        else
            return super.equals(o);
    }
}
