package edu.usc.qed.cloudfed.Simulate;

public class Listener {
    public int arrivals;
    public int departures;
    public int overflow;
    public int rejections;
    
    public Listener () {
        arrivals = 0;
        departures = 0;
        overflow = 0;
        rejections = 0;
    }

    public void notify(Noise e) {
        Noise.Type t = e.getType();
        switch (t) {
            case ARR:
                arrivals++;
                break;
            case DEP:
                departures++;
                break;
            case REJ:
                rejections++;
                break;
            case ENQ:
                break;
            case SER:
                break;
            case OVR:
                overflow++;
                break;
        }
    }
}
