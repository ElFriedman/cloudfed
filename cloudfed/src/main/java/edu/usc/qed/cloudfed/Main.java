package edu.usc.qed.cloudfed;

import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParentCommand;
import picocli.CommandLine.ScopeType;

@Command(name = "cloudfed", mixinStandardHelpOptions = true, version = "cloudfed 1.0",
         description = "Cloud Federation Simulator",
         scope = ScopeType.INHERIT,
         subcommands = { Main.Workload.class, Main.Simulate.class, Main.Metrics.class })
public class Main {
    private final static Logger logger =  LoggerFactory.getLogger(Main.class);

    @Option(names = "--verbose", defaultValue = "false", scope = ScopeType.INHERIT,
            description = "Log additional debugging information (default: ${DEFAULT-VALUE})")
    private boolean verbose;

    @Command(name = "workload", description = "Generate a workload.")
    static class Workload implements Callable<Integer> {
        @ParentCommand private Main parent;

        @Override public Integer call() throws Exception {
            System.out.println("Genenerating a workload");
            // https://github.com/msgpack/msgpack-java/blob/develop/msgpack-core/src/test/java/org/msgpack/core/example/MessagePackExample.java
            return 0;
        }
    }

    @Command(name = "simulate", description = "Simulate a federation.")
    static class Simulate implements Callable<Integer> {
        @ParentCommand private Main parent;

        @Override public Integer call() throws Exception {
            System.out.println("Simulating an execution");
            return 0;
        }
    }

    @Command(name = "metrics", description = "Compute metrics from simulation output.")
    static class Metrics implements Callable<Integer> {
        @ParentCommand private Main parent;

        @Override public Integer call() throws Exception {
            System.out.println("Computing metrics");
            return 0;
        }
    }

    public static void main(String[] args) {
        CommandLine cmd = new CommandLine(new Main());
        int exitCode = cmd.execute(args);
        System.exit(exitCode);
    }
}
