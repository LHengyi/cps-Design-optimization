package hybrid;

import java.io.*;
import javax.xml.parsers.*;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import java.util.*;
//import ptolemy.actor.gui.MoMLSimpleApplication;
//import Domparser;

public class ControlOptimization{

	protected static String inputxml;
	protected static int numberofloops;
	//protected static String samplingfile;
	//public static ArrayList<ArrayList<String>> samplingpoint = new ArrayList<ArrayList<String>>();


	//Constructor
	public ControlOptimization(){

		inputxml = null;
		numberofloops = 0;
		//samplingfile = null;
	}

	public ControlOptimization(String[] args){
		int i = 0;
		inputxml = args[i++];
		numberofloops = Integer.parseInt(args[i++]);
		samplingfile =args[i++];
	} 
	//Constructor

	public void run(String[] args) throws IOException {
		int i = 0;
		inputxml = args[i++];
		numberofloops = Integer.parseInt(args[i++]);
		samplingfile =args[i++];

		try {
			String thisline;
			int j = 0;
			FileInputStream fstream = new FileInputStream(samplingfile);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			Xmlparse parseobj = new Xmlparse(inputxml);
			String[] tokes = null;
			while ( (thisline = br.readLine()) != null) {
				System.out.println(thisline);
				System.out.println("check samplingpoint");
				samplingpoint.add(new ArrayList<String>());
				tokes = thisline.split(" ");
				for (i=0;i<numberofloops ;i++ ) {
					samplingpoint.get(j).add(tokes[i]);
				}
				j++;
				

				//parse xml file and replace related parameters
				parseobj.parse(tokes, numberofloops);
				parseobj.saveXML();
				//parse xml file and replace related parameters
				//MoMLSimpleApplication builder = new MoMLSimpleApplication(inputxml);


				//run ptolemy simulation
				//java.lang.ProcessBuilder builder = new java.lang.ProcessBuilder("java","-classpath","/home/hengyi/Research/ptolemy/ptII","ptolemy.actor.gui.MoMLSimpleApplication","/home/hengyi/Dropbox/ptolemyexperiment/Domparser/"+inputxml);
				java.lang.ProcessBuilder builder = new java.lang.ProcessBuilder("/home/hengyi/Research/ptolemy/ptII/bin/ptexecute","/home/hengyi/Dropbox/ptolemyexperiment/Domparser/period/"+inputxml);
				//System.out.println("process build");
				Process process = builder.start();
				java.util.Map env = builder.environment();
				process.waitFor();
				System.out.println("wait over");
				//run ptolemy simulation

				//move file
				for (i=1;i<=numberofloops;i++){
					File oldfile = new File("/home/hengyi/Documents/Display"+Integer.toString(i)+".txt");
					File newfile = new File("./Data/truedisplay"+Integer.toString(i)+"_"+tokes[i-1]+".txt");
					if (oldfile.renameTo(newfile)) {
						System.out.println("File remove Success :"+newfile.toString());
					}
					oldfile.delete();
				}
				//move filejava
			}
		} catch (Throwable e){
			e.printStackTrace();
		}

		
		//try {
			// java.lang.ProcessBuilder builder = new java.lang.ProcessBuilder("/home/hengyi/Documents/ptII/bin/ptexecute","/home/hengyi/Dropbox/ptolemyexperiment/"+inputxml);
			// Process process = builder.start();
			//builder.close();
			// java.util.Map env = builder.environment();
		//} catch (IOException ioe) {
			//ioe.printStackTrace();
		//}
	}

	public void saveperformance(double[][] performance){
		BufferedWriter bw = null;
		try {
			File file = new File("performance.txt");
			if(!file.exists()){
				file.createNewFile();
			}
			FileWriter fw = new FileWriter(file);
			bw = new BufferedWriter(fw);
			for (int i = 0; i< numberofloops ; i++ ) {
				for (int j = 0; j<samplingpoint.size() ; j++ ) {
					bw.write(Double.toString(performance[i][j])+" ");
					//System.out.println(performance[i][j]+"");
				}
				bw.write("\n");
			}
		} catch(IOException ioe){
			ioe.printStackTrace();
		}
		finally
		{
			try{
				if(bw!=null)
					bw.close();
			}catch(Exception ex){
				System.out.println("Error in Closing BufferedWriter"+ex);
			}
		}
	}

	public ArrayList<ArrayList<String>> getsamplepoint(){
		return samplingpoint;
	}

	// public static void main(String[] args) throws IOException{
	// 	ControlOptimization cOp = new ControlOptimization();
	// 	cOp.run(args);
	// 	AnnealSim annealing = new AnnealSim();
	// 	double[][] performance = annealing.read(numberofloops,samplingpoint);
	// 	cOp.saveperformance(performance);
	// 	annealing.annealcore(samplingpoint,performance,1);
	// }
}
