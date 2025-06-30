Java program simulating four CPU scheduling algorithms:
- Priority Scheduling (Non-preemptive)
- Shortest Job First (SJF) (Non-preemptive, solves starvation)
- Shortest Remaining Time First (SRTF) (Preemptive, solves starvation)
- FCAI Scheduling – a custom hybrid algorithm combining priority, arrival time, and remaining burst time into a single FCAI Factor to dynamically manage the execution order and quantum allocation for processes.
    - FCAI Factor = (10−Priority) + (Arrival Time/V1) + (Remaining Burst Time/V2), 
      Where:
      
             - V1 = last arrival time of all processes/10
             - V2 = max burst time of all processes/10
      
    - Quantum Allocation Rules:
      
           - Each process starts with a unique quantum.
           - When processes are preempted or added back to the queue, their quantum is updated dynamically:
                - Q= Q + 2 (if process completes its quantum and still has remaining work)
                - Q=Q + unused quantum (if process is preempted)
      
    - Non-Preemptive and Preemptive Execution:

           - A process executes non-preemptively for the first 40% of its quantum.
           - After 40% execution, preemption is allowed.
