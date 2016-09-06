package hybrid;

import java.io.*;
import java.util.*;
import org.apache.commons.math3.analysis.polynomials.*;
import org.apache.commons.math3.analysis.interpolation.*;
import org.apache.commons.exec.*;
import org.apache.commons.exec.environment.EnvironmentUtils;

public class Range_sml{
	private double[] global_smled_low;
	private double[] global_smled_high;
	private ArrayList<ArrayList<double[]>> loop_range;
	private int num_loops;
	private ArrayList<ArrayList<Double>> samplingpoint;
	private ArrayList<TreeMap<Double,Double>> performance;

	//Constructor
	public Range_sml(int a){
		num_loops = a;
		loop_range = new ArrayList<ArrayList<double[]>>();
		samplingpoint = new ArrayList<ArrayList<Double>>();
		performance = new ArrayList<TreeMap<Double,Double>>();
		for (int i = 0; i < num_loops ; i++ ) {
			loop_range.add(new ArrayList<double[]>());
			samplingpoint.add(new ArrayList<Double>());
			performance.add(new TreeMap<Double,Double>());
		}
	}
	//Constructor

	//public methods

	public void simulate() throws IOException{
		Xmlparse parse = new Xmlparse("4controller.xml");
		int[] samplelength = new int[num_loops];
		for (int i = 0; i < num_loops ; i++) {
			samplelength[i] = samplingpoint.get(i).size();
		}
		int[] point = new int[num_loops];
		for(int i = 0; i < max(samplelength); i++){
			Double[] tokens = new Double[num_loops];
			for (int j = 0; j < num_loops ; j++ ) {
				if (point[j] < samplelength[j]) {
					tokens[j] = samplingpoint.get(j).get(point[j]);
				}else{
					tokens[j] = null;
				}
			}
			parse.parseperiod(tokens);
			parse.saveXML();

			// java.lang.ProcessBuilder builder = new java.lang.ProcessBuilder("/Users/Hengyi/Documents/research/ptII/bin/ptexecute","/Users/hengyi/Dropbox/ptolemyexperiment/period/"+"4controller.xml");
			// Process process = builder.start();
			// java.util.Map env = builder.environment();
			// try{
			// 	process.waitFor();
			// 	System.out.println("wait over");
			// 	process.destroy();
			// }catch(InterruptedException e){
			// 	e.printStackTrace();
			// }
			System.out.print("Ptolemy Running");
			boolean exitvalue = apacheexec();
			System.out.print("Done!\n");

			for (int j = 0; j < num_loops ; j++ ) {
				if (point[j] < samplelength[j]) {
					File oldfile = new File("/Users/hengyi/Documents/research/Data/Display"+Integer.toString(j)+".txt");
					File newfile = new File("./Data/truedisplay"+Integer.toString(j)+"_"+tokens[j]+".txt");
					if (exitvalue == true) {
						oldfile.renameTo(newfile);
						System.out.println("File remove Success :"+newfile.toString());saveperformance(j, newfile, tokens[j]);
						saveperformance(j, newfile, tokens[j]);
					}
					oldfile.delete();
					System.out.println("File deleted: "+oldfile.toString());
				}
				point[j]++;
			}
			for (int j = 0 ; j < num_loops ; j++) {
				File file = new File("/Users/hengyi/Documents/research/Data/Display"+Integer.toString(j)+".txt");
				if(file.exists()){
					file.delete();
				}
			}
		}

		int count = 0;
		for (ArrayList<Double> ele : samplingpoint) {
			double[] x = new double[ele.size()];
			double[] y = new double[ele.size()];
			for (int i = 0; i < ele.size(); i ++) {
				x[i] = ele.get(i);
				y[i] = performance.get(count).get(x[i]);
			}
			count++;
			System.out.println("X length is :"+x.length);
			if (x.length > 1) {
				systemconfig(x,y);
			}
		}
		systemconfigend();
	}

	public void sasolver(){
		
	}

	public void initialrange(double[][] est_bound){
		for (int i = 0; i < num_loops ; i++) {
			double step = (est_bound[1][i] - est_bound[0][i])/4;
			samplingpoint.get(i).add(est_bound[0][i]);
			for (int j = 0; j < 3 ; j++ ) {
				samplingpoint.get(i).add(samplingpoint.get(i).get(j) + step);
			}
			samplingpoint.get(i).add(est_bound[1][i]);
			double[] temp = {est_bound[0][i], est_bound[0][i]};
			loop_range.get(i).add(temp);
		}
	}
	public void check_range(double[][] est_bound){
		for (int i = 0; i < num_loops ; i++ ) {
			samplingpoint.get(i).clear();
		}
		if (loop_range.get(0).size() == 0) {
			for (int i = 0; i < num_loops ; i++) {
				double step = (est_bound[1][i] - est_bound[0][i])/4;
				samplingpoint.get(i).add(est_bound[0][i]);
				for (int j = 0; j < 3 ; j++ ) {
					samplingpoint.get(i).add(samplingpoint.get(i).get(j) + step);
				}
				samplingpoint.get(i).add(est_bound[1][i]);
			}
			for (int i = 0; i < num_loops ; i++ ) {
				double[] temp = {est_bound[0][i], est_bound[1][i]};
				loop_range.get(i).add(temp);
			}
			//debug information
			for (int i = 0; i < samplingpoint.size(); i++) {
				System.out.println("sampling points for loop"+i+": (first time)");
				for (int j = 0; j < samplingpoint.get(i).size(); j++) {
					System.out.print(samplingpoint.get(i).get(j)+"\t");
				}
				System.out.print("\n");
			}
			//debug information
			return;
		}
		merge_range();
		for (int i = 0; i < num_loops ; i++ ) {
			if (est_bound[0][i] < loop_range.get(i).get(0)[0]) {
				if (est_bound[1][i] <= loop_range.get(i).get(0)[0]) {//est_bound not in range
					double step = (est_bound[1][i] - est_bound[0][i])/4;
					samplingpoint.get(i).add(est_bound[0][i]);
					for (int j = 0; j < 3 ; j++ ) {
						samplingpoint.get(i).add(samplingpoint.get(i).get(j) + step);
					}
					samplingpoint.get(i).add(est_bound[1][i]);
					System.out.print(Double.toString(samplingpoint.get(i).get(0))+"");
				}else{
					double step = 0.003;
					samplingpoint.get(i).add(est_bound[0][i]);
					while(samplingpoint.get(i).get(samplingpoint.get(i).size()-1) + step <loop_range.get(i).get(0)[0]){
						double temp = samplingpoint.get(i).get(samplingpoint.get(i).size()-1);
						samplingpoint.get(i).add(step + temp);
					}
					for (int j = 0; j < loop_range.get(i).size() ; j++ ) {
						if (est_bound[1][i] > loop_range.get(i).get(j)[1]) {
							if (j+1 < loop_range.get(i).size()) {
								if (est_bound[1][i] < loop_range.get(i).get(j+1)[0]) {
									while(samplingpoint.get(i).get(samplingpoint.get(i).size()-1) + step < est_bound[1][i]){
										double temp = samplingpoint.get(i).get(samplingpoint.get(i).size()-1);
										samplingpoint.get(i).add(step + temp);
									}
									break;
								}else{
									while(samplingpoint.get(i).get(samplingpoint.get(i).size()-1) + step < loop_range.get(i).get(j+1)[0]){
										double temp = samplingpoint.get(i).get(samplingpoint.get(i).size()-1);
										samplingpoint.get(i).add(step + temp);
									}
								}
							}else{
								while(samplingpoint.get(i).get(samplingpoint.get(i).size()-1) + step < est_bound[1][i]){
									double temp = samplingpoint.get(i).get(samplingpoint.get(i).size()-1);
									samplingpoint.get(i).add(step + temp);
								}
							}
						}
					}
					System.out.print(Double.toString(samplingpoint.get(i).get(0))+"");
				}
			}else{
				double step = 0.003;
				for (int j = 0; j < loop_range.get(i).size() ; j++) {
					if(est_bound[0][i] < loop_range.get(i).get(j)[1]){
						if (j+1 < loop_range.get(i).size()) {
							if (est_bound[1][i] > loop_range.get(i).get(j)[1]) {
								if (est_bound[1][i] < loop_range.get(i).get(j+1)[0]) {
									if ((est_bound[1][i]-loop_range.get(i).get(j)[1])>step) {
										samplingpoint.get(i).add(loop_range.get(i).get(j)[1]+step);
									}
									while(samplingpoint.get(i).get(samplingpoint.get(i).size()-1) + step < est_bound[1][i]){
										double temp = samplingpoint.get(i).get(samplingpoint.get(i).size()-1);
										samplingpoint.get(i).add(step + temp);
									}
								}else{
									if ((loop_range.get(i).get(j+1)[0]-loop_range.get(i).get(j)[1])>step) {
										samplingpoint.get(i).add(loop_range.get(i).get(j)[1]+step);
									}
									while(samplingpoint.get(i).get(samplingpoint.get(i).size()-1) + step < loop_range.get(i).get(j+1)[0]){
										double temp = samplingpoint.get(i).get(samplingpoint.get(i).size()-1);
										samplingpoint.get(i).add(step + temp);
									}
								}
							}
						}
					}else{
						if (j < loop_range.get(i).size()-1 && est_bound[0][i] < loop_range.get(i).get(j+1)[0]) {
							samplingpoint.get(i).add(est_bound[0][i]);
							while(samplingpoint.get(i).get(samplingpoint.get(i).size()-1) + step < Math.min(est_bound[1][i],loop_range.get(i).get(j+1)[0])){
								double temp = samplingpoint.get(i).get(samplingpoint.get(i).size()-1);
								samplingpoint.get(i).add(step + temp);
							}
						}else if( est_bound[0][i] > loop_range.get(i).get(loop_range.get(i).size()-1)[1]){
							step = (est_bound[1][i] - est_bound[0][i])/5;
							samplingpoint.get(i).add(est_bound[0][i]);
							for (int k = 0; k < 3 ; k++ ) {
								samplingpoint.get(i).add(samplingpoint.get(i).get(k) + step);
							}
							samplingpoint.get(i).add(est_bound[1][i]);
						}
						System.out.print(Double.toString(samplingpoint.get(i).get(0))+"");
					}
				}
			}
		}
		for (int i = 0; i < num_loops ; i++ ) {
			double[] temp = {est_bound[0][i], est_bound[1][i]};
			loop_range.get(i).add(temp);
		}
		// for (int i = 0; i <num_loops ; i++ ) {
		// 	System.out.println("loop"+i+" range size:"+"\t"+loop_range.get(i).size());
		// }

		//debug information
		for (int i = 0; i < samplingpoint.size(); i++) {
			System.out.println("sampling points for loop"+i+":");
			for (int j = 0; j < samplingpoint.get(i).size(); j++) {
				System.out.print(samplingpoint.get(i).get(j)+"\t");
			}
			System.out.print("\n");
		}
		//debug information
	}

	public boolean issampEmpty(){
		boolean result = true;
		for (int i = 0; i < num_loops ; i++) {
			if (samplingpoint.get(i).size() > 1) {
				result = false;
				return result;
			}
		}
		return result;
	}

	public void merge_range(){
		merge_sort();
		for (int i = 0; i < num_loops ; i++ ) {
			ArrayList<double[]> singleloop = loop_range.get(i);
			if (singleloop.size() > 1) {
				for (int j = 0; j < singleloop.size() - 1 ; j++ ) {
					if (singleloop.get(j+1)[0] < singleloop.get(j)[1]) {
						if (singleloop.get(j+1)[1] > singleloop.get(j)[1]) {
							double[] temp = {singleloop.get(j)[0], singleloop.get(j+1)[1]};
							loop_range.get(i).set(j,temp);
							loop_range.get(i).remove(j+1);
						}else if(singleloop.get(j+1)[1] <= singleloop.get(j)[1]){
							loop_range.get(i).remove(j+1);
						}
					}
				}
			}
		}
		//debug information
		for(int i = 0; i < num_loops; i++){
			System.out.println("Loop"+i+"simulated range:");
			for(int j = 0; j < loop_range.get(i).size(); j++){
				System.out.print("From: "+loop_range.get(i).get(j)[0]+"\t To: "+loop_range.get(i).get(j)[1]);
			}
			System.out.print("\n");
		}
	}

	public void merge_sort(){
		for (int i = 0; i < num_loops ; i++ ) {
			if (loop_range.get(i).size() > 1) {
				int midpoint = Math.floorDiv(loop_range.get(i).size(),2);
				ArrayList<double[]> left = new ArrayList<double[]>(loop_range.get(i).subList(0,midpoint));
				ArrayList<double[]> right = new ArrayList<double[]>(loop_range.get(i).subList(midpoint,loop_range.get(i).size()));
				left = merge_sort(left);
				right = merge_sort(right);
				loop_range.set(i,merge(left,right));
			}
		}
	}

	protected ArrayList<double[]> merge_sort(ArrayList<double[]> tasksett){
		if(tasksett.size() <= 1){
			return tasksett;
		}
		int midpoint = Math.floorDiv(tasksett.size(),2);
		ArrayList<double[]> left = new ArrayList<double[]>(tasksett.subList(0,midpoint));
		ArrayList<double[]> right = new ArrayList<double[]>(tasksett.subList(midpoint,tasksett.size()));
		left = merge_sort(left);
		right = merge_sort(right);

		return merge(left, right);
	}

	protected ArrayList<double[]> merge(ArrayList<double[]> left, ArrayList<double[]> right){
		ArrayList<double[]> result = new ArrayList<double[]>();
		while(!left.isEmpty() && !right.isEmpty()){
			if(left.get(0)[0] <= right.get(0)[0]){
				result.add(left.get(0));
				left.remove(0);
			} else{
				result.add(right.get(0));
				right.remove(0);
			}
		}

		if(!left.isEmpty()){
			result.addAll(left);
		}
		if(!right.isEmpty()){
			result.addAll(right);
		}

		return result;
	}

	protected int max(int[] array){
		int max = -1;
		for(int ele : array){
			if (ele > max) {
				max = ele;
			}
		}
		return max;
	}

	protected Double max(Double[] array){
		Double max = -1d;
		for(Double ele : array){
			if (ele > max) {
				max = ele;
			}
		}
		return max;
	}

	public void saveperformance(int loopid, File file, double token){
		ArrayList<Double> dataY = new ArrayList<Double>();
		double perf = 0;
		double setpoint = 0;
		try{
			setpoint = findsetpoint(file);
			FileInputStream fstream = new FileInputStream(file);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String thisline;
			while ( (thisline = br.readLine()) != null) {
				dataY.add(Double.parseDouble(thisline));
			}
		}catch (IOException e){
			e.printStackTrace();
		}
		for (int l=0 ; l <= dataY.size()-4 ; l=l+2 ) {
			perf = perf + Math.abs(dataY.get(l+2) + dataY.get(l)-2*setpoint)*(dataY.get(l+3) - dataY.get(l+1))/2;
		}
		performance.get(loopid).put(token,new Double(perf));
		Collection<Double> valuearr = performance.get(loopid).values();
		Double[] value = valuearr.toArray(new Double[valuearr.size()]);
		double maximum = max(value);
		//Set set = performance.get(loopid).entrySet();
		//Iterator i = set.iterator();
		for (Double key : performance.get(loopid).keySet() ) {
			double temp = performance.get(loopid).get(key)/maximum;
			performance.get(loopid).replace(key, temp);
		}
	}

	public double performance_est(double[] period){
		double result = 0;
		for (int i = 0; i < num_loops ; i++) {
			double floorkey = performance.get(i).floorKey(period[i]);
			double floorvalue = performance.get(i).get(floorkey);
			double ceilingkey = performance.get(i).ceilingKey(period[i]);
			double ceilingvalue = performance.get(i).get(ceilingkey);
			result += (period[i] - ceilingkey)/(floorkey - ceilingkey)*(floorvalue - ceilingvalue) + ceilingvalue;
			// double[] perf_value = new double[performance.get(i).size()];
			// LinearInterpolator lininterpolator = new LinearInterpolator();
			// int j =0;
			// for (Double value : performance.get(i).values() ) {
			// 	perf_value[j++] = value;
			// }
			// PolynomialSplineFunction function = lininterpolator.interpolate(perf_value,performance[i-1]);
		}
		return result;
	}

	public double findsetpoint(File file){
		double setpoint = 0;
		try{
			FileInputStream fstream = new FileInputStream(file);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String thisline;
			boolean flag = true;
			ArrayList<Double> truevalue = new ArrayList<Double>();
			while ( (thisline = br.readLine()) != null) {
				if (flag) {
					truevalue.add(Double.parseDouble(thisline));
				}
			}
			double sum = 0;
				for (int j=truevalue.size()-Math.round(truevalue.size()/4);j< truevalue.size();j++ ) {
					sum +=truevalue.get(j);
				}
			setpoint = sum/Math.round(truevalue.size()/4);
			br.close();
			in.close(); 
			fstream.close();
		}catch(IOException e){
			e.printStackTrace();
		}
		return setpoint;
	}

	public void systemconfig(double[] x, double[] y) throws IOException{
		File file = new File("/Users/Hengyi/Downloads/periodOpt/originalDAC/System.txt");
		FileWriter fw = new FileWriter(file,true);
		BufferedWriter bw = new BufferedWriter(fw);

		LinearInterpolator lininterpolator = new LinearInterpolator();
		PolynomialSplineFunction function = lininterpolator.interpolate(x,y);
		double[] knots = function.getKnots();
		PolynomialFunction[] pfunc = function.getPolynomials();

		bw.write("L"+"\t"+knots.length);
		for (int i = 0; i < knots.length ; i++ ) {
			bw.write("\t"+knots[i]);
		}
		for (PolynomialFunction ele : pfunc) {
			double[] coefficients = ele.getCoefficients();
			for (double coeff : coefficients) {
				bw.write("\t"+coeff);
			}
		}
		bw.write("\n");
		bw.close();
	}

	public void systemconfigend() throws IOException{
		File file = new File("/Users/Hengyi/Downloads/periodOpt/originalDAC/System.txt");
		FileWriter fw = new FileWriter(file,true);
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write("W");
		for (int i = 0; i < num_loops ; i++ ) {
			bw.write("\t"+1);
		}
		bw.write("\n");
		bw.close();
	}

	public boolean apacheexec(){
		CommandLine cmdLine = new CommandLine("/Users/Hengyi/Documents/research/ptII/bin/ptexecute");
	 	cmdLine.addArgument("/Users/hengyi/Dropbox/ptolemyexperiment/period/4controller.xml");
	 	DefaultExecutor executor = new DefaultExecutor();
	 	DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();
		try{
		 	executor.execute(cmdLine, resultHandler);
		 	resultHandler.waitFor();
		 	System.out.println("exitvalue is :"+resultHandler.getExitValue());
	 	}catch(IOException ioe){
	 		return false;
	 	}catch(InterruptedException inte){
	 		inte.printStackTrace();
	 	}
	 	if (resultHandler.getExitValue() != 0) {
	 		return false;
	 	}else{
	 		return true;
	 	}
	}
}