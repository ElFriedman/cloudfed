# CloudFed: Cloud Federation Simulator

## Usage

### Workload Generation

```
java -jar cloudfed.jar workload generate workload1.msgpack "Job[1000]" "type1:10:Exp[1.5]:Dist[0.7:1,0.3:2]:Unif[1,2]" "type2:5.5:Exp[2]:Det[1]:Unif[3,4]" 
```
Generate  workload file `workload1.msgpack` with a stopping criterion and two streams of jobs, until `1000` jobs have been created
- `type1`: QoS requirement of 10, exponential interarrival time with rate `1.5`, batch size of `1` with
  probability `0.7` or `2` with probability `0.3`, job size uniformly
  distributed in `[1,2]`
- `type2`: QoS requirement of 5.5, exponential interarrival time with rate `2`, batch size of `1`, job
  size uniformly distributed in `[3,4]`
```
java -jar cloudfed.jar workload generate workload2.msgpack "Time[150] "type3:8:Exp[2]:Dist[0.9:1,0.05:2,0.05:3]:Unif[2,3]"
```
Generate  workload file `workload2.msgpack` with a stopping criterion and one stream of jobs, until time = `150`
- `type1`: QoS requirement of `8`, exponential interarrival time with rate `2`, batch size of `1` with
  probability `0.9` or `2` with probability `0.05` or `3` with probability `0.05`, job size uniformly
  distributed in `[2,3]`


```
java -jar cloudfed.jar workload info workload1.msgpack -A
java -jar cloudfed.jar workload info workload2.msgpack "type3"
```

Show workload information of selected streams (end time, number of jobs, mean interarrival time,
mean batch size, mean job size).
- If `-A` or `--all` is added, all streams are selected
- Otherwise, only listed streams are selected

```
java -jar cloudfed.jar workload merge new-workload.msgpack workload1.msgpack workload2.msgpack
```

Merge multiple workloads.

### Simulation

```
java -jar cloudfed.jar simulate --cloud type1,100:2.0:50,20:1.0:5 --cloud type2,100:1.0:100 workload.msgpack output.msgpack
```

Simulate the input stream of jobs `workload.msgpack` by routing:
- Jobs of `type1` to a cloud with: `100` servers with rate `2.0`, `50` of which
  are shared with the federation; `20` servers with rate `1.0`, `5` of which are
  shared with the federation.
- Jobs of `type2` to a cloud with: `100` servers with rate `1.0`, all shared
  with the federation.

The output file `output.msgpack` includes information of each job of the workload.


### Output Analysis

```
java -jar cloudfed.jar metrics output.msgpack
```

Computes relevant metrics.


## Development

### Background Knowledge

- B. Song, M. Paolieri, L. Golubchik. [Performance and Revenue Analysis of Hybrid Cloud Federations with QoS Requirements](https://drive.google.com/file/d/1s06SPMAeulnwM_YeARgvUggQ4lM6izJS/view?usp=sharing), 2022
- Michael Altmann. [Writing a Discrete Event Simulation: Ten Easy Lessons](https://web.archive.org/web/20210506173656/https://users.cs.northwestern.edu/~agupta/_projects/networking/QueueSimulation/mm1.html), 2001
- Kyle Siegrist. [Pareto distribution](https://www.randomservices.org/random/special/Pareto.html)
- E. Smirni. [Poisson Process](https://www.cs.wm.edu/~esmirni/Teaching/cs526/section7.3.pdf), [Confidence Intervals](https://www.cs.wm.edu/~esmirni/Teaching/cs526/section8.1.pdf), [Batch Means](https://www.cs.wm.edu/~esmirni/Teaching/cs526/section8.4.pdf), [M/M/1 Metrics](https://www.cs.wm.edu/~esmirni/Teaching/cs526/section8.5.pdf)
- L. Leemis and S. Park. [Discrete Event Simulation: A First Course](https://www.google.com/search?q=%22Discrete+Event+Simulation+-+A+First+Course+-+Lemmis+Park%22), 2004

### Setup

To work on this project:
- Install Java 18:
  - Windows: Download the installer from [here](https://adoptium.net/temurin/releases?version=18)
  - MacOS: `brew install --cask temurin` (requires [Homebrew](https://brew.sh/))
  - Linux: `apt-get install temurin-18-jdk` or `yum install temurin-17-jdk`
- Install VS Code:
  - Windows/Linux: Download the installer from [here](https://code.visualstudio.com/Download)
  - MacOS: `brew install --cask visual-studio-code` (requires [Homebrew](https://brew.sh/))
- Configure VS Code:
  - Disable [telemetry](https://code.visualstudio.com/docs/getstarted/telemetry#_disable-telemetry-reporting).
  - Install the [Extension Pack for Java](https://marketplace.visualstudio.com/items?itemName=vscjava.vscode-java-pack).
- Clone this repository and open in VS Code:
  - `git clone git@github.com:qed-usc/cloudfed.git`
  - `code cloudfed`

Please check out modern Java features and libraries including:
- [Try-With-Resources](https://jenkov.com/tutorials/java-exception-handling/try-with-resources.html)
- [Objects.requireNonNull](https://docs.oracle.com/en/java/javase/18/docs/api/java.base/java/util/Objects.html#requireNonNull(T,java.lang.String)),
  [Objects.checkIndex](https://docs.oracle.com/en/java/javase/18/docs/api/java.base/java/util/Objects.html#checkIndex(int,int)),
  [Objects.hash](https://docs.oracle.com/javase/8/docs/api/java/util/Objects.html#hash-java.lang.Object...-)
- [Interface Default Methods](https://jenkov.com/tutorials/java/interfaces.html)
- [Streams](https://docs.oracle.com/en/java/javase/18/docs/api/java.base/java/util/stream/package-summary.html)
- [Type Inference](https://openjdk.java.net/jeps/286)
- [Switch Expressions](https://openjdk.java.net/jeps/361)
- [Text Blocks](https://openjdk.java.net/jeps/378)
- [Records](https://openjdk.java.net/jeps/395)
- [Sealed Classes](https://openjdk.java.net/jeps/409)
- [Pattern Matching for `instanceof`](https://openjdk.java.net/jeps/394)
- [Pattern Matching for `switch`](https://openjdk.java.net/jeps/427)
- [Executor Services](https://jenkov.com/tutorials/java-util-concurrent/executorservice.html)
- [AutoValue with Builders](https://github.com/google/auto/blob/master/value/userguide/builders.md)
- [MsgPack](https://github.com/msgpack/msgpack-java/blob/develop/msgpack-core/src/test/java/org/msgpack/core/example/MessagePackExample.java)
- [Enhanced Pseudo-Random Number Generators](https://openjdk.java.net/jeps/356)
- [BigDecimal](https://docs.oracle.com/en/java/javase/18/docs/api/java.base/java/math/BigDecimal.html)
- [PriorityQueue](https://docs.oracle.com/en/java/javase/18/docs/api/java.base/java/util/PriorityQueue.html)

### Running Tests and Checking Coverage

```
cd cloudfed
bash coverage.sh
cat coverage.txt
```

### Generating an Executable Jar (for release)

```
cd cloudfed
bash generate_jar.sh
```
