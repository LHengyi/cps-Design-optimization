package hybrid;

import java.io.*;
import java.util.*;

public class TopFrame{
	private int num_cores, num_tasks, num_loops;
	private Allocation allocation;
	private Range_sml range_sml;
	private Allocation result;
	private double performace;
	// private double[] global_smled_low;
	// private double[] global_smled_high;
	//Constructor
	public TopFrame(){
	num_cores = 4;
	num_tasks = 8;
	num_loops = 4;
	allocation = new Allocation(num_cores, num_tasks, num_loops);
	range_sml = new Range_sml(num_loops);
	result = new Allocation(num_cores, num_tasks, num_loops);
	performace = 100;
	// global_smled_low = new double[num_loops];
	// global_smled_high = new double[num_loops];
	}
	//Constructor
	public static void main(String[] args) {
		double performace = Double.MAX_VALUE;
		TopFrame topframe = new TopFrame();
		topframe.cleanspace();
		topframe.settasks();
		topframe.allocation.initialassign();
		double[][] est_bound = topframe.allocation.period_est();//period estimation
		//topframe.range_sml.initialrange(est_bound);
		//topframe.range_sml(est_lowerbound);
		for (int i = 0; i < 10 ; i++ ) {
			
			topframe.range_sml.check_range(est_bound);
			try{
				topframe.allocation.writeCSV(est_bound);
				topframe.allocation.config4controller();
				topframe.range_sml.simulate();
				if (!topframe.range_sml.issampEmpty()) {
					topframe.allocation.run_period();
				}
			}catch(IOException e){
				e.printStackTrace();
			}
			//double[] period = new double[topframe.num_loops];
			//topframe.gpsolver();
			//double temp = topframe.range_sml.performance_est(period);
			//performace = performace < temp ? performace : temp;
			topframe.allocation.est_flag = false;
			while(topframe.allocation.est_flag == false){
				topframe.allocation.randomset();
				est_bound = topframe.allocation.period_est();
			}
		}

		topframe.cleanspace();
	}

	//methods
	private void settasks(){
		ArrayList<Task> tasklist = new ArrayList<Task>(num_tasks);
		//debug informaiton
		System.out.println("total number of tasks:\t"+num_tasks);
		//debug information
		tasklist.add(0, new Task(0.005, 0, 0));//wcet, loopID, taskID
		tasklist.add(1, new Task(0.0075, 1, 1));
		tasklist.add(2, new Task(0.01, 2, 2));
		tasklist.add(3, new Task(0.0075, 3, 3));
		tasklist.add(4, new Task(0.005, 0, 4));
		tasklist.add(5, new Task(0.0075, 1, 5));
		tasklist.add(6, new Task(0.01, 2, 6));
		tasklist.add(7, new Task(0.0075, 3, 7));
		allocation.addtask(tasklist);
	}

	private void cleanspace(){
		try{
			Process p = new ProcessBuilder("/bin/sh", "/home/hengyi/Dropbox/ptolemyexperiment/period/cleanup.sh").start();
			p.waitFor();
			p.destroy();
		}catch (IOException e){
			e.printStackTrace();
		}catch (InterruptedException d){
			d.printStackTrace();
		}
	}

	private void store_bestresult(){
		double[] sp = new double[num_loops];
		for (int i = 0; i <num_loops ; i++ ) {
			sp[i] = allocation.taskset.get(i).getperiod();
		}
		double temp = range_sml.performance_est(sp);
		if (temp < performace) {
			result.allocation = allocation.allocation;
			result.priority = allocation.priority;
			result.taskset = allocation.taskset;
			performace = temp;
		}
	}
	// private void range_sml(double[][] est_bound){
	// 	double[] upperbound = est_bound[1];
	// 	double[] difference = new double[num_loops];
	// 	double[] step = new double[num_loops];
	// 	int iter_times = 5;
	// 	//specify range
	// 	for (int i = 0; i < difference.length ; i++ ) {
	// 		difference[i] = global_smled_low[i] - lowerbound[i];
	// 		step[i] = difference[i]/(iter_times - 1);
	// 	}
	// 	double[][] ts_samplep_point = new double[iter_times][num_loops];
	// 	ts_samplep_point[0] = lowerbound;
	// 	ts_samplep_point[ts_samplep_point.length-1] = upperbound;
	// 	for (int i = 0; i < ts_samplep_point.length-2 ; i++ ) {
	// 		for (int j = 0; j < num_loops ; j++ ) {
	// 			ts_samplep_point[i+1][j] = ts_samplep_point[i][j] + step[j];
	// 		}
	// 	}
	// 	//specify range

	// 	//debug information
	// 	System.out.println("Sampling Point:");
	// 	for (int i = 0; i < iter_times ; i++ ) {
	// 		for (int j = 0; j < num_loops ; j++) {
	// 			System.out.print(ts_samplep_point[i][j]+"\t");
	// 		}
	// 		System.out.print("\n");
	// 	}

	// 	//debug information
	// }
}