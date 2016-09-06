 package hybrid;

import java.io.*;
import java.util.*;
import java.util.stream.DoubleStream;
import org.apache.commons.exec.*;
import org.apache.commons.exec.environment.EnvironmentUtils;
import org.jdom2.*;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.*;

public class Allocation{
	protected static int num_core;
	protected static int num_tasks;
	protected static int num_loops;
	protected static double[] latencyupper = {0.12,0.12,0.12,0.12};//bound for sampling period + latency
	protected int[][] allocation;
	protected int[][] priority;
	protected ArrayList<Task> taskset;
	protected boolean est_flag;

	//constructor
	public Allocation(int a, int b, int c){
		num_core = a;
		num_tasks = b;
		num_loops = c;
		allocation = new int[num_core][num_tasks];//allocation[i][j] = 1 if task j is mapped to core i
		priority = new int[num_tasks][num_tasks];// priority[i][j] = 1 if task i,j are mapped to same core and i's priority is higher;
		taskset = new ArrayList<Task>();
		est_flag = false;
	}
	//constructor

	//public methods
	public void addtask(Task e){
		taskset.add(e);
	}

	public void addtask(ArrayList<Task> ele){
		taskset.addAll(ele);
	}

	public ArrayList<Task> initialassign(){
		int average_task = Math.floorDiv(num_tasks,num_core);
		int remainder = num_tasks - num_core*average_task;
		ArrayList<Task> task_sort = merge_sort(taskset);

		int j = 0;
		//set assignment
		for ( int i = 0 ; i < num_tasks; i++ ) {
			taskset.get(task_sort.get(i).gettaskID()).setcoremapping(j);
			allocation[j++][task_sort.get(i).gettaskID()] = 1;
			if (j > 3) {
				j = 0;
			}
		}
		//set scheduing/priority
		for ( int i = 0; i < num_core ; i++ ) {
			ArrayList<Integer> taskindex = new ArrayList<Integer>();
			for(j = 0; j < num_tasks; j++){
				if (allocation[i][j] == 1) {
					taskindex.add(j);
				}
			}

			for(j = 0; j < taskindex.size(); j++){
				for(int k = j+1; k < taskindex.size(); k++){
					if(taskset.get(taskindex.get(j)).getwcet() < taskset.get(taskindex.get(k)).getwcet()){
						priority[taskindex.get(j)][taskindex.get(k)] = 1;
						taskset.get(taskindex.get(k)).priority++;
					}
				}
			}
		}
		//debug information
		System.out.println("Initial Allocation Matrix");
		for (int i = 0; i < num_core ; i++) {
			for ( j = 0; j < num_tasks ; j++ ) {
				System.out.print(allocation[i][j]+"\t");
			}
			System.out.print("\n");
		}
		System.out.println("Initial Priority Matrix");
		for (int i = 0; i < num_tasks ; i++) {
			for ( j = 0; j < num_tasks ; j++ ) {
				System.out.print(priority[i][j]+"\t");
			}
			System.out.print("\n");
		}
		//debug information
		return task_sort;
	}


	public double[][] period_est(){
		double[] boundl = new double[num_loops];//[lowerboundl
		double[] boundh = new double[num_loops];
		double[][] bound = new double[2][num_loops];
		Arrays.fill(boundl, 100);
		//double[] responsetime = new double[num_tasks];
		//boolean[] est_tag = new boolean[taskset.size()];
		//find lower bound
		//double miniperiod = minimal_prd();
		for (int i =0 ; i < num_loops ; i++ ) {
			int[] taskonloop = find_task_onloop(i);
			//debug information
			System.out.print("Task On Loop"+i+":\n");
			for (int ele : taskonloop ) {
				System.out.print(ele+"\t");
			}
			System.out.print("\n");
			//debug information
			for (int j = 0; j < taskonloop.length ; j++) {
				int[] taskonECU = find_alltask_onECU(taskonloop[j]);
				double wcetall = 0;
				double utility = 1;
				//debug informaiton
				System.out.println("Tasks on ECU:");
				for ( int ele : taskonECU ) {
					System.out.print(ele+"\t");
				}
				System.out.print("\n");
				//debug information
				for (int ele : taskonECU ) {
					if (taskset.get(ele).getloopID() != i) {
						utility -= taskset.get(ele).getwcet()/latencyupper[taskset.get(ele).getloopID()];
					}else {
						wcetall += taskset.get(ele).getwcet();
					}
				}
				double temp = wcetall/utility;
				if (boundl[i] == 100) {
					boundl[i] = temp;
				}else{
					boundl[i] = (boundl[i] > temp) ? boundl[i] : temp;
				}
			}
			//find upper bound
			double est_lat = 0;
			double t_this_loop = latencyupper[i];
			double step = t_this_loop/20;
			double latsvalue = 0;
			double tlast = 0;
			do{
				est_lat = taskonloop.length*t_this_loop;
				for (int j = 0; j < taskonloop.length ; j++) {
					est_lat += getresponstime(taskonloop[j], t_this_loop);
				}
				//System.out.print("estimated latency for loop"+i+"\t:"+est_lat+"\n");

				if (latsvalue > latencyupper[i] && est_lat < latencyupper[i]) {
					t_this_loop = t_this_loop + (latsvalue - est_lat)/10;
					step = (latsvalue - est_lat)/10;
				}else if(est_lat > latencyupper[i]){
					t_this_loop -= step;
				}else {
					t_this_loop += step;
				}
				latsvalue = est_lat;
				tlast = t_this_loop;
				if (t_this_loop < 0) {
					est_flag = false;
					return bound;
				}
			}while(est_lat>latencyupper[i] || Math.abs((est_lat - latencyupper[i])/latencyupper[i]) > 0.05);
			boundh[i] = t_this_loop + step;
		}
		//debug information
		System.out.println("period Lower bound Estimated:");
		for (int i = 0; i < num_loops ;i++ ) {
			System.out.print(boundl[i]+"\t");
		}
		System.out.print("\n");

		System.out.println("period upper bound Estimated:");
		for (int i = 0; i < num_loops ;i++ ) {
			System.out.print(boundh[i]+"\t");
		}
		System.out.print("\n");
		//debug information
		bound[0] = boundl;
		bound[1] = boundh;
		est_flag = true;
		try{
			savebound(bound);
		}catch(IOException ioe){
			ioe.printStackTrace();
		}
		return bound;
	}

	public double[][] latencyrange(double[][] periodrange){
		double[][] result = new double[2][num_loops];
		for (int j = 0; j < num_tasks ; j++ ) {
			taskset.get(j).changeperiod(periodrange[0][taskset.get(j).getloopID()]);
		}
		for (int i = 0; i < num_loops ; i++ ) {
			int[] taskonloop = find_task_onloop(i);
			for (int j = 0; j < taskonloop.length ; j++ ) {
				taskset.get(taskonloop[j]).changeperiod(periodrange[1][i]);
			}
			double[] r_time = new double[taskonloop.length];
			for (int j = 0; j < taskonloop.length; j++) {
				r_time[j] = getresponstime(taskonloop[j]);
				result[1][i] += (r_time[j] + taskset.get(taskonloop[j]).getperiod());
			}
			for (int j = 0; j < taskonloop.length ; j++ ) {
				taskset.get(taskonloop[j]).changeperiod(periodrange[0][i]);
			}
		}

		for (int j = 0; j < num_tasks ; j++ ) {
			taskset.get(j).changeperiod(periodrange[1][taskset.get(j).getloopID()]);
		}
		for (int i = 0; i < num_loops ; i++ ) {
			int[] taskonloop = find_task_onloop(i);
			for (int j = 0; j < taskonloop.length ; j++ ) {
				taskset.get(taskonloop[j]).changeperiod(periodrange[0][i]);
			}
			double[] r_time = new double[taskonloop.length];
			for (int j = 0; j < taskonloop.length; j++) {
				r_time[j] = getresponstime(taskonloop[j]);
				result[0][i] += (r_time[j] + taskset.get(taskonloop[j]).getperiod());
			}
			for (int j = 0; j < taskonloop.length ; j++ ) {
				taskset.get(taskonloop[j]).changeperiod(periodrange[1][i]);
			}
			if (result[0][i] > result[1][i]) {
				double temp = result[0][i];
				result[0][i] = result[1][i];
				result[1][i] = temp;
			}
		}
		return result;
	}

	public void randomset(){
		Random randnumber = new Random();
		for (int i = 0; i < num_tasks ; i++) {
			for (int j =0; j < num_tasks ; j++ ) {
				priority[i][j] = 0;
			}
		}
		for (int i = 0; i < num_tasks ; i++ ) {
			allocation[taskset.get(i).getcoremapping()][taskset.get(i).gettaskID()] = 0;
			taskset.get(i).setcoremapping(randnumber.nextInt(num_core));
			allocation[taskset.get(i).getcoremapping()][taskset.get(i).gettaskID()] = 1;
			taskset.get(i).setpriority(1);
		}

		System.out.println("Allocation Matrix");
		for (int i = 0; i < num_core ; i++) {
			for ( int j = 0; j < num_tasks ; j++ ) {
				System.out.print(allocation[i][j]+"\t");
			}
			System.out.print("\n");
		}

		for(int i = 0; i < num_core; i++){
			int[] taskonecu = ecuindextasks(i);
			ArrayList<Integer> shufflelist = new ArrayList<Integer>();
			for (int j = 0; j <taskonecu.length ;j++ ) {
				shufflelist.add(taskonecu[j]);
			}
			Collections.shuffle(shufflelist);
			for (int j = 0; j < shufflelist.size() ;j++ ) {
				taskonecu[j] = shufflelist.get(j);
				//System.out.print("taskonecu"+taskonecu[j]);
			}
			System.out.print("\n");

			for(int j = 0; j < taskonecu.length; j++){
				for (int k = j+1; k < taskonecu.length ; k++ ) {
					priority[taskonecu[j]][taskonecu[k]] = 1;
					taskset.get(taskonecu[k]).priority++;
				}
			}
		}
		System.out.println("Priority Matrix");
		for (int i = 0; i < num_tasks ; i++) {
			for ( int j = 0; j < num_tasks ; j++ ) {
				System.out.print(priority[i][j]+"\t");
			}
			System.out.print("\n");
		}
	}

	public void writeCSV(double[][] est_bound) throws IOException{
		File file = new File("/Users/Hengyi/Downloads/periodOpt/originalDAC/System.txt");
		if (!file.exists()) {
			file.createNewFile();
		}
		FileWriter fw = new FileWriter(file);
		BufferedWriter bw = new BufferedWriter(fw);
		for (int i = 0; i < num_tasks ; i++ ) {
			bw.write("N"+" "+"task"+i+"\t"+"n"+"\t"+taskset.get(i).getwcet()+"\t"+(est_bound[0][taskset.get(i).getloopID()]+est_bound[1][taskset.get(i).getloopID()])/2+"\t"+taskset.get(i).getcoremapping()+"\t");
			int temp = 1;
			for (int j = 0; j < num_tasks ; j++ ) {
				temp += priority[j][i];
			}
			bw.write(""+temp+"\n");
		}

		for (int i = 0; i < num_loops ; i++ ) {
			bw.write("E"+"\t"+"task"+i+"\t"+"task"+(i+4)+"\n");
		}

		for (int i = 0; i < num_loops ; i++ ) {
			int[] taskonloop = find_task_onloop(i);
			bw.write("C"+"\t"+latencyupper[i]);
			for (int tasknumb : taskonloop) {
				bw.write("\t"+"task"+tasknumb);
			}
			bw.write("\n");
		}
		double[] latency = gete2elatency();
		for (int i = 0; i < num_loops ; i++) {
			bw.write("P"+"\t"+"Path"+i+"\t"+latencyupper[i]);
			bw.write("\n");
		}
		// for(int i = 0; i < num_tasks/2; i++){
		// 	bw.write("E"+" "+"task"+i+"\t"+"task"+Math.addExact(i,4)+"\n");
		// }
		bw.close();
	}

	public void config4controller(){
		String parsefile = "4controller.xml";
		 SAXBuilder saxBuilder = new SAXBuilder();
		 Document document = null;
		try{
			File inputFile = new File(parsefile);
			if (!inputFile.exists()) {
				System.out.println("error: cannot find 4controller.xml");
				return;
			}
			String xml = "<?xml ...";
			xml = xml.trim().replaceFirst("^([\\W]+)<","<");
			document = saxBuilder.build(inputFile);
			System.out.println("Configure 4controller assignment and scheduling");
			Element root = document.getRootElement();
			Element ptides = null;
			List<Element> entitylist = root.getChildren("entity");
			for (Element ele : entitylist ) {
				String name = ele.getAttributeValue("name");
				if (name.equals("PtidesPlatform")) {
					List<Element> subentitylsit = ele.getChildren("entity");
					for (Element subele : subentitylsit ) {
						name = subele.getAttributeValue("name");
						if (name.equals("PtidesPlatformContents")) {
							ptides = subele;
							break;
						}
					}
					break;
					// char lastchar = name.charAt(name.length);
					// int index = Character.getNumericValue(lastchar);
					// propertylist = ele.getChildren("property");
					// for(Element mem : propertylsit){
					// 	String pname = mem.getAttributeValue("name");
					// 	if(pname.contains("DecoratorAttributesFor_FixedPriorityScheduler")){
							
					// 	}
					// }
				}
			}
			entitylist = ptides.getChildren("entity");
			for(Element ele : entitylist){
				String name = ele.getAttributeValue("name");
				if(name.contains("task")){
					char lastchar = name.charAt(name.length()-1);
					int index = Character.getNumericValue(lastchar);
					int ecuindex = taskset.get(index).getcoremapping();
					int thispriority = taskset.get(index).getpriority();
					List<Element> schelist = ele.getChildren("property");
					for (Element subele : schelist) {
						String sname = subele.getAttributeValue("name");
						if (sname.contains("DecoratorAttributesFor_FixedPriorityScheduler")) {
							int thisindex = Character.getNumericValue(sname.charAt(sname.length()-1));
							if (thisindex == ecuindex) {
								List<Element> propertylsit = subele.getChildren("property");
								for(Element finele : propertylsit){
									if (finele.getAttributeValue("name") == "enable") {
										finele.getAttribute("value").setValue("true");
									}else if(finele.getAttributeValue("name") == "priority"){
										finele.getAttribute("value").setValue(thispriority+"");
									}
								}
							}else{
								List<Element> propertylsit = subele.getChildren("property");
								for(Element finele : propertylsit){
									if (finele.getAttributeValue("name") == "enable") {
										finele.getAttribute("value").setValue("false");
									}
								}
							}
						}
					}
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	public void run_period(){
		Map map = new HashMap();
		map.put("file", new File("/Users/Hengyi/Downloads/periodOpt/originalDAC/System.txt"));
		CommandLine cmdLine = new CommandLine("/Users/Hengyi/Downloads/periodOpt/originalDAC/period_assignment");
		cmdLine.addArgument("${file}");
		cmdLine.addArgument("0");
		cmdLine.addArgument("4");
		cmdLine.addArgument("0");
		cmdLine.addArgument("3");
		cmdLine.addArgument("0");
		cmdLine.addArgument("4");
		cmdLine.addArgument("1");
		cmdLine.addArgument("1");
		cmdLine.setSubstitutionMap(map);
		DefaultExecutor executor = new DefaultExecutor();
		DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();
		executor.setExitValue(1);
		try{
			File direc = new File("/Users/Hengyi/Downloads/periodOpt/originalDAC/");
			executor.setWorkingDirectory(direc);
			executor.execute(cmdLine, resultHandler);
			resultHandler.waitFor();
		}catch(IOException ioe){
			ioe.printStackTrace();
		}catch(InterruptedException inte){
			inte.printStackTrace();
		}
		result_to_period();
	}
	//find maximum ts when tasks on same loop are on same ECU.
	// protected double[] findTmax(){
	// 	double[] result = new double[num_loops];
	// 	int count; ArrayList<ArrayList<Integer>> orderedtask = new ArrayList<ArrayList<Integer>>(num_loops);
	// 	for (int i =0; i < num_loops; i++ ) {
	// 		orderedtask.add(new ArrayList<Integer>());
	// 	}
	// 	for (int i = 0; i < num_tasks ; i++ ) {
	// 		orderedtask.get(taskset.get(i).getloopID()).add(taskset.get(i).gettaskID());
	// 	}
	// 	for (int i = 0; i < num_loops; i++){
	// 		Collections.sort(orderedtask.get(i));
	// 		//double estimated_tmax = taskset.get(taskset.get(orderedtask.get(i).get(0)).getwcet());
	// 		double t = 0;
	// 		double esti_lp;
	// 		for (int j =0; j < orderedtask.get(i).size() ; j++ ) {
	// 				t += taskset.get(orderedtask.get(i).get(j)).getwcet();
	// 			}
	// 		do {
	// 			double[] list_r_i = getresponstime(orderedtask.get(i), t);
	// 			esti_lp = (1+orderedtask.get(i).size())*t + DoubleStream.of(list_r_i).sum();
	// 			t = t*1.05;
	// 		} while(Math.abs(esti_lp - latencyupper[i])/latencyupper[i] >= 0.10);
	// 		result[i] = t/1.05;
	// 	}
	// 	return result;
	// }
	//public methods

	//protected methods

	protected double getresponstime(int taskid, double tmax){
		double estimated_r = taskset.get(taskid).getwcet();
		double r_i = taskset.get(taskid).getwcet();
		double stepsize = r_i/20;
		int k = 0;
		int[] t_on_ECU = find_task_onECU(taskid);
		do {
			r_i = estimated_r; //last estimated_r
			estimated_r = taskset.get(taskid).getwcet() + k*stepsize;
			k++;
			for ( int i = 0; i < t_on_ECU.length ; i++) {
				if (taskset.get(taskid).getloopID() != taskset.get(t_on_ECU[i]).getloopID()) {
					estimated_r += Math.ceil(r_i/latencyupper[taskset.get(t_on_ECU[i]).getloopID()])*taskset.get(t_on_ECU[i]).getwcet();
				}else{
					estimated_r += Math.ceil(r_i/tmax)*taskset.get(t_on_ECU[i]).getwcet();
				}
			} //new estimated_r
			if ( k >= 100) {
				return r_i;
			}
		} while (Math.abs(r_i - estimated_r)/r_i >= 0.05);
		return r_i;
	}

	protected ArrayList<Task> merge_sort(ArrayList<Task> tasksett){
		if(tasksett.size() <= 1){
			return tasksett;
		}
		int midpoint = Math.floorDiv(tasksett.size(),2);
		ArrayList<Task> left = new ArrayList<Task>(tasksett.subList(0,midpoint));
		ArrayList<Task> right = new ArrayList<Task>(tasksett.subList(midpoint,tasksett.size()));
		left = merge_sort(left); right = merge_sort(right);

		return merge(left, right);
	}

	protected ArrayList<Task> merge(ArrayList<Task> left, ArrayList<Task> right){
		ArrayList<Task> result = new ArrayList<Task>();
		while(!left.isEmpty() && !right.isEmpty()){
			if(left.get(0).getwcet() <= right.get(0).getwcet()){
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

	protected double[] gete2elatency(){
		double[] result = new double[num_loops];
		for ( int i = 0 ; i < taskset.size() ; i++ ) {
			result[taskset.get(i).getloopID()] = taskset.get(i).getperiod() + getresponstime(taskset.get(i).gettaskID());
		}
		return result;
	}

	protected double[] gete2elatency(double[] responsetime){
		double[] result = new double[num_loops];
		for (int i = 0; i < taskset.size(); i++){
			result[taskset.get(i).getloopID()] = taskset.get(i).getperiod()+ responsetime[i];
		}
		return result;
	}

	protected double getresponstime(int taskid){
		double estimated_r = taskset.get(taskid).getwcet();
		double r_i = taskset.get(taskid).getwcet();
		double stepsize = r_i/20;
		int k = 0;
		int[] t_on_ECU = find_task_onECU(taskid);
		do {
			r_i = estimated_r; //last estimated_r
			estimated_r = taskset.get(taskid).getwcet() + k*stepsize;
			k++;
			for ( int i = 0; i < t_on_ECU.length ; i++) {
				estimated_r += Math.ceil(r_i/taskset.get(t_on_ECU[i]).getperiod())*taskset.get(t_on_ECU[i]).getwcet();
			} //new estimated_r
			if ( k >= 100) {
				return r_i;
			}
		} while (Math.abs(r_i - estimated_r)/r_i >= 0.05);
		return r_i;
	}

	// protected double[] getresponstime(ArrayList<Integer> list, double t){//
	// 	double[] result = new double[list.size()];
	// 	// double t;
	// 	// for (int i =0; i < list.size() ; i++ ) {
	// 	// 	t += taskset.get(list.get(i)).getwcet();
	// 	// }
	// 	for (int i = 0 ; i < list.size() ; i++ ) {
	// 		double estimated_r  =taskset.get(list.get(i)).getwcet();
	// 		double r_i = taskset.get(list.get(i)).getwcet();
	// 		double stepsize = r_i/20;
	// 		int k = 0;
	// 		do{
	// 			r_i = estimated_r;
	// 			estimated_r = taskset.get(list.get(i)).getwcet()+k*stepsize;
	// 			k++;
	// 			for(int j = 0; j < i; j++){
	// 				estimated_r += Math.ceil(r_i/t)*taskset.get(list.get(i)).getwcet();
	// 			}
	// 			if (k >= 100){
	// 				break;
	// 			}
	// 		} while(Math.abs(r_i - estimated_r)/r_i >=0.05);
	// 		result[i] = r_i;
	// 	}
	// 	return result;
	// }

	//find hight priority task on same ECU
	protected int[] find_task_onECU(int taskid){
		ArrayList<Integer> result = new ArrayList<Integer>();
		for (int i = 0; i < num_tasks ; i++) {
			if ( priority[i][taskid] == 1) {
				result.add(i);
			}
		}
		int[] final_result = result.stream().mapToInt(i -> i).toArray();
		return final_result;
	}

	protected int[] find_alltask_onECU(int taskid){
		ArrayList<Integer> result = new ArrayList<Integer>();
		int coreid = taskset.get(taskid).getcoremapping();
		for (int i = 0; i < num_tasks ; i++) {
			if ( allocation[coreid][i] == 1) {
				result.add(i);
			}
		}
		int[] final_result = result.stream().mapToInt(i -> i).toArray();
		return final_result;
	}

	protected int[] ecuindextasks(int coreID){
		List<Integer> task = new ArrayList<Integer>();
		for (Task ele : taskset) {
			if (ele.getcoremapping() == coreID) {
				task.add(ele.gettaskID());
			}
		}
		int[] result = new int[task.size()];
		for (int i = 0; i < task.size(); i++) {
			result[i] = task.get(i);
			//System.out.print("ecuindextasks"+result[i]);
		}
		//System.out.print("\n");
		return result;
	}

	protected int[] find_task_onloop(int loopnumber){
		ArrayList<Integer> result = new ArrayList<Integer>();
		for (int i = 0; i < num_tasks; i++){
			if(taskset.get(i).getloopID() == loopnumber){
				result.add(i);
			}
		}
		int[] final_result = result.stream().mapToInt(i -> i).toArray();
		return final_result;
	}

	protected double minimal_prd(){
		double minimalperiod = 100;
		for ( Task ele : taskset ) {
			if (ele.getperiod() < minimalperiod) {
				minimalperiod = ele.getperiod();
			}
		}
		return minimalperiod;
	}

	protected void result_to_period(){
		try{
			File file = new File("/Users/Hengyi/Downloads/periodOpt/originalDAC/results.txt");
			FileInputStream fstream = new FileInputStream(file);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			if (!file.exists()) {
				System.out.println("period optimization failed");
				return;
			}else{
				String thisline;
				String identifier = "assignment of sampling period";
				while((thisline = br.readLine()) != null){
					if (thisline.contains(identifier)) {
						for (int i = 0; i < num_loops ; i++ ) {
							thisline = br.readLine();
							String[] token = thisline.split(" ");
							int[] task = find_task_onloop(i);
							for (int j = 0; j < task.length ; j++ ) {
								taskset.get(task[j]).changeperiod(Double.parseDouble(token[1]));
							}
						}
						return;
					}
				}
			}
		}catch(IOException ioe){
			ioe.printStackTrace();
		}
	}

	protected void savebound(double[][] bound) throws IOException{
		File file = new File("period_est.txt");
		if(!file.exists()){
			file.createNewFile();
		}
		FileWriter fw = new FileWriter(file,true);
		BufferedWriter bw = new BufferedWriter(fw);
		for (int i = 0; i < num_loops ;i++ ) {
			bw.write(bound[0][i]+"\t"+bound[1][i]+"\n");
		}
		bw.close();
	}
	//protected methods
}