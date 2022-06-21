package edu.usc.qed.cloudfed.Simulate;

public class Listener {
    public Listener () {

    }

    public void notify(Noise e) {
        Noise.Type t = e.getType();
        switch (t) {
            case ARR:
                System.out.println();
                break;

            case DEP:
                System.out.println();
                break;

            
            case REJ:
                System.out.println();
                break;
            
            case ENQ:
                System.out.println();
                break;

            case DEQ:
                System.out.println();
                break;

            case SER:
                System.out.println();
                break;
        }
    }
}
