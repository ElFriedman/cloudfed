# Cloud Federation Simulator

We are interested in creating a simulator to predict the performance of a cloud federation that can handle non-markovian processes. Given a system of N clouds (2 in the graph), each with a number of servers, they form a federation by contributing a subset of their servers to the federation and forwarding excessive requests to the federation servers. 
![](https://i.imgur.com/qi0aMBV.png)
The federation exists if at least 1 cloud contributes 1 server to the federation. 

Each cloud in the system has an independent queue and pool of servers. The federation has a separate queue that accepts overflow from each cloud. We are interested in the performance of a federation, namely the overflow from the federation based on the sharing strategy (number of servers shared to the federation) by each cloud.

## User Stories
- A user can specify a federation setup including:
    - Service
        - Number of servers in a cloud ($n$)
        - Service time distribution within a cloud (Assign average service rate ($1/\mu$) to each server)
    - Workload
        - Input traffic intensity ($\lambda$=number of customers per unit of time)
        - Workload distribution ($\mu$=units of time to complete a request)
        - Arrival Process (Distribution of Interarrival time ($1/\lambda$), the probability distribution of numbers of requests arriving at the same time*)
    - Queueing
        - Capacity (maximum expected wait-time or latency tolerance, the maximum number of jobs*)
        - Scheduling policy (FCFS, prioritize based on the assigned priority number*)
    - Sharing
        - number of servers shared with the federation
        - the amount of overflow sent to the federation*
    - User specified setting should not conflict. (Assert and throw an error with proper reason if setting conflicts.)
    - Setting should be summarized as part of the experimental report.

- A user can obtain a performance report:
    - the amount of overflow from each cloud and the federation 
    - the amount of workload performed by each cloud and the federation
    - the amount of input workload towards each cloud and the federation
    - output instantaneous value and on average after the system is stable (average number starting from an iteration)

- A user can obtain a graphic report or text report (a text report only needs average values after the system is stable).

- A developer will be able to examine the correctness of the simulator according to simple examples. (Test cases with normal, extremely large, and extremely small numbers)

- A user can run multiple experiments at the same time.*

- A developer can import major functions from this library and obtain outputs. (Able to write a Python wrapper to calculate the Shapley Value of each cloud.)*

- A developer will be able to extend functionalities under the main framework such as defining distributions for service time and interarrival time beyond exponential distribution, including more scheduling policies, adding metrics for performance, adding policies to define queuing capacities, etc.

Note: Objects with \* are not part of the MVP.

## Minimal Viable Product

The MVP of the project is to simulate a federation of clouds with homogeneous servers (servers with the same service rate). Each cloud has an independent Poisson arrival process (interarrival time is exponentially distributed with 1 request arriving at a time). The overflow of each cloud is sent to the federation. If there is no server at the federation, the overflow is immediately considered an overflow at the federation. 

When serving requests, each server can accommodate 1 request at a time. The service time is exponentially distributed around one mean value.

The scheduling process is first come first serve. The capacity of the queue is defined by the maximum amount of wait-time allowed by a cloud based on the average service time. The capacity of the queue at the federation is subjected to each cloud and based on their wait-time tolerance.

## Timeline

- Find related (math) libraries and choose a programming language - before Jun 6th
- Implement a single cloud and output its performance a queue (M/M/n/n+q) - before Jun 27th
- Test correctness - before July 1st
- Implement a federation with queueing - before July 15th
- Test correctness - before July 20th
- Allow heterogeneous workload  - before July 30th
- Test correctness - before Aug 10th
- Able to generate report  - before Aug 15th
> Note: completion of each should be accompanied by unit (and e2e test if applicable).

## Implementation

Consider the following notations:
- $\lambda$ = mean arrival rate
- mst = mean service time of a request
- n = number of servers
- $\mu$ = mean service rate
- Q = maximumly allowed mean wait time
- Ba = batch arrival probabilities (probabilities for numbers of requests to arrive simultaneously)


```
Class Arrival(interval_fxn, Ba=[1], mst=1)
```
- Describes an arrival process with distribution of inter-arrival time, batch arrival probabilities, and the mean service time of each request.
- interval_fxn describes the interarrival process. By default, the interarrival time should follow an exponential distribution with a mean arrival rate $\lambda$.

```
Class Service(servers, service_fxn, Q)
```
- Describes service rates of all servers and their queueing spaces
- service_fxn describes the service rate where the service time for a unit of workload. Default to a constant.
- Q describes the maximally allowed queueing time before service

```
Class Cloud(arrival, service, shared_servers)
```
- Every cloud has 1 arrival process
- Every cloud has 1 service time distribution
- and is willing to share a number of servers
```
Class Federation([cloud])
```
- generates all performance metrics for the clouds in the federation when they share all servers in the `shared_servers` and also consider when they do not share any of those servers.
- When generate report, give options to show each metric 
- Methods:
```
    - get_federation_overflow()
    - get_cloud_overflow(share=True)
    - get_federation_served()
    - get_cloud_served(share=True)
    - get_cloud_arrival()
    - generate_report(Text=True)
    ...
```
When describing overflow, we need the number of request per unit of time and the size of the job, so that we can use them to calculate the charges at the public cloud.

We are also interested in
- service wait time (time from a request entering a queue to getting service)
- service completion time (time from service begin and service end)
- simulation time (performance of the simulator)*


For each request, we need to label the workload stream origin, so that we can get those metrics for each arrival streams. 

### Queueing

When admitting a request, in theory, we use the average service time to calculate the maximum queueing capacity in terms of the number of requests in a queue. In a simulation, we are given the service time of a request, and we can use it to predict the exact queueing capacity. However, this can deviate from the theory.

<details>
  <summary>Reasons for using exact time or mean service time:</summary>

Using exact time for admitting a request:

Pros:
- The queueing capacity is accurate (In exactly the specified amount of time, the request can start service.)
- We can use this actual queueing capacity to see if the rejection rate is what we expected.

Cons:
- In reality, requests do not normally come with expected service time or even exact service time.
- It might be different from the expected queueing capacity and deviate from our model. (not sure if it is a good thing)
- It can get very complicated when servers have different service rates. 

Using mean service time to calculate the queueing capacity:

Pros:
- It follows the assumption of the theory.
- It becomes more realistic since we will at most be given an expected service time than the exact one.
- It is easier to implement.
- We can even check how many requests are not getting served in time.

Cons:
- The queueing time is not accurate. (requests may be served later than expected waiting time)
- Unless using other methods in calculating the capacity, requests with service times following a heavy tail distribution will have more requests not following the expected waiting time.
</details>

We consider the following methods to calculate the queueing capacity:
- Keep track of the origin of all requests (which stream of arrival it came from) admitted to the system (both in sevrice and in queue).
- Given that we know the mean and variance of the service time for an arrival stream, we can use this to calculate the queueing capacity based on a weighted average for all the requests in the system.


## Project Partition

The above describes the core library of the simulator. To produce results/report for a set of federations and their cloud settings, we consider the following 3 parts of the workflow pipeline.

### Workload Generator
Produce randomized input workload to the simulator.
- Save (serialize) workload sequence so that the simulator can reuse the same arrival streams to obtain and average result.
- Create a set (a large fixed number) of workload inputs for each federation setting. (Randomized)

### Simulator Runner
Run simulator with the given workload several times and get the mean and the variance. 
- Number of running times should be a tuning parameter.
- Number of iteration should be a tunning parameter.
- Number of iteration can also be stopped by some criteria.
- Save the output for evaluator to investigate further.

### Metric Evaluator
Generate metrics and report from saved simulator output. 

## Testing

We consider correctness as the essential objective of testing. Both correctness of the implementation, closeness to the numerical result, and in computation range such as inputting extremely big and small numbers. 

For correctness, we compare simulated result with the numerical result from Prism. Since Prism can model upto homogeneous workload with upto 5 cloud federation, each cloud with 50 servers, we can compare our simulation with Prism's result. Prism can produce simulation as well as numerical results. While comparing simulated result is hard, we can focus on the average on the repeated experiment to compare to the numerical one.

Test against precision in big and small numbers.*

When assigning distribution of interarrival time and service time, we would like to check the correctness of those distribution by plotting histograms to check if the distribution follows the input function.

## Resources
Simply use linked list for queue/priority queue if we consider request priorities.

Avoid using the big number libraries to avoid slowdown. We still need to ensure correctness. (maybe just to a certain range? Assert simulation never pass that range.)

Consider methods to detect steady state (find the steady iteration) 
- Checkout Ross' [simulation book](http://www.ru.ac.bd/wp-content/uploads/sites/25/2019/03/308_01_Ross_Simulation_Fifth_Ed.pdf).
- Optionally, use a threshold for variance of a moving window. 

https://github.com/gonum

https://en.wikipedia.org/wiki/Poisson_distribution#Generating_Poisson-distributed_random_variables


## Shapley Value Wrapper

To compute the Shapley Value of a federation, we need to obtain the overflow of the federation under every subgroup of clouds. Under each subgroup federation, we compute the average overflow after system stabilizes. Using the performance outputs, we obtain the Shapley value of each cloud.


## Game Theory Wrapper

Using the similar concept obtaining stable performance result, we can adjust sharing strategies of clouds in the federation. 

## Extra
- For each cloud, we should enable several arrival streams (mostly independent). (Our current assumption is to have each cloud with a single arrival stream, and the heterogeneous arrival processes are at the federation level)
- For each cloud, we should allow heterogeneous service rates. This is to simulate servers of different speeds. We do not consider server and workload affinity since Shapley Value of those can be added. (Our current assumption only allows heterogeneous service times at the federation level)
- Allow different request size distribution (exponential, uniform, Bernoulli)
- Allow Markov-modulated Poisson processes (MMPP), specifically 2 state MMPP to model day and night time job arrivals.
- Allow request to move to faster server during execution when available. (This is important for the proof of federation with heterogeneous service rates)

## Open Questions

- What is the best way to determine the earliest iteration when the system stabilizes?
- How to test the precision of the simulator? What if the inputs are very large or small values?
- For workloads with non-exponentially distributed service time, what is a good way to guarantee QoS? (change the current setup where  we reject workload in a queue when they have spent more than permitted amount of time waiting?) 
