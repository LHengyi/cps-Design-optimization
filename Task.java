package hybrid;

import java.io.*;
import java.util.*;

public class Task{
	protected double wcet;
	protected double period;
	protected int loopID;
	protected int taskID;
	protected int maponcore;
	protected int priority;

	//constructor
	public Task(double a, double b, int c, int d){
		wcet = a;
		period =b;
		loopID = c;
		taskID = d;
		priority = 1;
	}
	public Task(double a, int c, int d){
		wcet = a;
		loopID = c;
		taskID = d;
	}
	//constructor

	//public methods

	public int getpriority(){
		return priority;
	}

	public void setpriority(int a){
		priority = a;
	}
	public double getwcet(){
		return wcet;
	}

	public double getperiod(){
		return period;
	}

	public void changeperiod(double a){
		period = a;
	}


	public int getloopID(){
		return loopID;
	}

	public int gettaskID(){
		return taskID;
	}

	public int getcoremapping(){
		return maponcore;
	}

	public void setcoremapping(int m){
		maponcore = m;
	}
	//public methods
}