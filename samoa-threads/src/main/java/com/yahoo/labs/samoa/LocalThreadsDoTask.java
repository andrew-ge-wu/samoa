package com.yahoo.labs.samoa;

import java.util.ArrayList;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.javacliparser.ClassOption;
import com.yahoo.labs.samoa.tasks.Task;
import com.yahoo.labs.samoa.topology.impl.ThreadsComponentFactory;
import com.yahoo.labs.samoa.topology.impl.ThreadsEngine;

/**
 * @author Anh Thu Vu
 *
 */
public class LocalThreadsDoTask {
    private static final Logger logger = LoggerFactory.getLogger(LocalThreadsDoTask.class);

    /**
     * The main method.
     * 
     * @param args
     *            the arguments
     */
    public static void main(String[] args) {

        ArrayList<String> tmpArgs = new ArrayList<>(Arrays.asList(args));
        
        // Get number of threads for multithreading mode
        int numThreads = 1;
        for (int i=0; i<tmpArgs.size()-1; i++) {
        	if (tmpArgs.get(i).equals("-t")) {
        		try {
        			numThreads = Integer.parseInt(tmpArgs.get(i+1));
        			tmpArgs.remove(i+1);
        			tmpArgs.remove(i);
        		} catch (NumberFormatException e) {
        			System.err.println("Invalid number of threads.");
        			System.err.println(Arrays.toString(e.getStackTrace()));
        		}
        	}
        }
        logger.info("Number of threads:{}", numThreads);
        
        args = tmpArgs.toArray(new String[tmpArgs.size()]);

        StringBuilder cliString = new StringBuilder();
        for (String arg : args) {
            cliString.append(" ").append(arg);
        }
        logger.debug("Command line string = {}", cliString.toString());
        System.out.println("Command line string = " + cliString.toString());

        Task task = null;
        try {
            task = ClassOption.cliStringToObject(cliString.toString(), Task.class, null);
            logger.info("Sucessfully instantiating {}", task.getClass().getCanonicalName());
        } catch (Exception e) {
            logger.error("Fail to initialize the task", e);
            System.out.println("Fail to initialize the task" + e);
            return;
        }
        task.setFactory(new ThreadsComponentFactory());
        task.init();
        
        ThreadsEngine.submitTopology(task.getTopology(), numThreads);
    }
}
