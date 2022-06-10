package edu.usc.qed.cloudfed.CloudModel;

import java.math.BigDecimal;

public class Server {
    private double workRate;
    private boolean inUse;

    private Request r;

    private BigDecimal uptime;
    private BigDecimal downtime;
    private BigDecimal lastTime;

    public Server (double workRate) {
        this.workRate = workRate;
        inUse = false;
        uptime = new BigDecimal (0);
        downtime = new BigDecimal (0);
        lastTime = new BigDecimal (0);
    }

    public boolean inUse() {
        return inUse;
    }

    public double getWorkRate () {
        return workRate;
    }

    public void employ (Request r, BigDecimal time) {
        if (inUse) {
            System.out.println("error: should not get here, server was already in use and got employed");
        } 
        inUse = true;
        this.r = r;

        downtime = downtime.add(time).subtract(lastTime);
        lastTime = time;
    }

    public void fire (BigDecimal time) {
        if (!inUse) {
            System.out.println("error: should not get here, server was not in use and got fired");
        }
        inUse = false;
        r = null;

        uptime = uptime.add(time).subtract(lastTime);
        lastTime = time;
    }

    public BigDecimal uptime1 () {
        return uptime.divide(uptime.add(downtime));
    }

    public BigDecimal uptime2 (BigDecimal time) {
        return uptime.divide(time);
    }
}

