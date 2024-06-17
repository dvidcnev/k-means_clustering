import subprocess
import re
import sys
import csv
import matplotlib.pyplot as plt

def run_bash_script(num_clusters, num_sites, mode, processors=None):
    if mode == 2:  # Distributed mode
        inputs = f"false\n{mode}\n{processors}\n{num_clusters}\n{num_sites}\n"
    else:
        inputs = f"false\n{mode}\n{num_clusters}\n{num_sites}\n"
    
    try:
        result = subprocess.run(['bash', 'run.sh'], input=inputs, text=True, capture_output=True, check=True)
        output = result.stdout
        print("Debug Output:\n", output)
        
        if mode == 0:
            time_match = re.search(r'\[SEQUENTIAL\] Total time: ([\d\.]+)s', output)
            cycles_match = re.search(r'\[SEQUENTIAL\] Total cycles: (\d+)', output)
        elif mode == 1:
            time_match = re.search(r'\[PARALLEL\] Total time: ([\d\.]+)s', output)
            cycles_match = re.search(r'\[PARALLEL\] Total cycles: (\d+)', output)
        else:
            time_match = re.search(r'\[DISTRIBUTED\] Total time: ([\d\.]+)s', output)
            cycles_match = re.search(r'\[DISTRIBUTED\] Total cycles: (\d+)', output)
        
        if time_match and cycles_match:
            total_time = float(time_match.group(1))
            total_cycles = int(cycles_match.group(1))
            return total_time, total_cycles
        else:
            raise ValueError("Output format unexpected: {}".format(output))
    
    except subprocess.CalledProcessError as e:
        print("Subprocess Error: ", e)
        print("Standard Output: ", e.stdout)
        print("Standard Error: ", e.stderr)
        sys.exit(1)

def get_increment(num_clusters):
    if num_clusters >= 1000:
        return 100
    elif num_clusters >= 100:
        return 50
    else:
        return 10

def main():
    initial_clusters = 10
    num_sites = 30000  # Fixed number of sites
    max_time_per_cycle = 5  # in seconds
    modes = [0, 1, 2]  # 0 = sequential, 1 = parallel, 2 = distributed
    results = []
    max_clusters_sequential = 0
    processors = 3  # Number of processors for distributed mode

    for mode in modes:
        num_clusters = initial_clusters
        mode_name = ["SEQUENTIAL", "PARALLEL", "DISTRIBUTED"][mode]

        while True:
            times = []
            cycles = []

            for _ in range(3):
                total_time, total_cycles = run_bash_script(num_clusters, num_sites, mode, processors if mode == 2 else None)
                times.append(total_time)
                cycles.append(total_cycles)
            
            avg_time = sum(times) / len(times)
            avg_cycles = sum(cycles) / len(cycles)
            avg_time_per_cycle = avg_time / avg_cycles

            print(f"For {num_clusters} clusters in {mode_name} mode: Average Time per Cycle = {avg_time_per_cycle:.3f}s")

            results.append((num_clusters, avg_time_per_cycle, mode_name))

            if mode == 0 and avg_time_per_cycle > max_time_per_cycle:
                print(f"Stopping {mode_name} mode as average time per cycle exceeds {max_time_per_cycle} seconds.")
                break

            if mode == 0:
                max_clusters_sequential = max(max_clusters_sequential, num_clusters)

            increment = get_increment(num_clusters)
            num_clusters += increment

            if mode > 0 and num_clusters > max_clusters_sequential + increment:
                print(f"Stopping {mode_name} mode as it reached the maximum number of clusters tested by sequential mode.")
                break

            if num_clusters > 10000:
                print(f"Stopping {mode_name} mode as it reached the maximum number of clusters.")
                break

    with open('results.csv', 'w', newline='') as csvfile:
        fieldnames = ['NumClusters', 'AvgTimePerCycle', 'Mode']
        writer = csv.DictWriter(csvfile, fieldnames=fieldnames)
        
        writer.writeheader()
        for num_clusters, avg_time_per_cycle, mode_name in results:
            writer.writerow({'NumClusters': num_clusters, 'AvgTimePerCycle': avg_time_per_cycle, 'Mode': mode_name})

    # Plotting the graph
    sequential_results = [r for r in results if r[2] == "SEQUENTIAL"]
    parallel_results = [r for r in results if r[2] == "PARALLEL"]
    distributed_results = [r for r in results if r[2] == "DISTRIBUTED"]

    plt.figure(figsize=(10, 6))
    
    if sequential_results:
        seq_num_clusters, seq_avg_time_per_cycle, _ = zip(*sequential_results)
        plt.plot(seq_num_clusters, seq_avg_time_per_cycle, marker='o', label='SEQUENTIAL')

    if parallel_results:
        par_num_clusters, par_avg_time_per_cycle, _ = zip(*parallel_results)
        plt.plot(par_num_clusters, par_avg_time_per_cycle, marker='o', label='PARALLEL')
    
    if distributed_results:
        dis_num_clusters, dis_avg_time_per_cycle, _ = zip(*distributed_results)
        plt.plot(dis_num_clusters, dis_avg_time_per_cycle, marker='o', label='DISTRIBUTED')

    plt.title('Average Time per Cycle vs. Number of Clusters')
    plt.xlabel('Number of Clusters')
    plt.ylabel('Average Time per Cycle (s)')
    plt.grid(True)
    plt.legend()
    plt.savefig('scaling_graph.png')
    plt.show()

if __name__ == "__main__":
    main()
