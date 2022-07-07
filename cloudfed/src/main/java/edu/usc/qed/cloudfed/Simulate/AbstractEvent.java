package edu.usc.qed.cloudfed.Simulate;

public interface AbstractEvent extends Comparable {
    public void execute(AbstractSimulator simulator) throws Exception;
}