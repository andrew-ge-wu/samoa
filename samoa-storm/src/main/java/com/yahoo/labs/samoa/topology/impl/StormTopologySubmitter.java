package com.yahoo.labs.samoa.topology.impl;

/*
 * #%L
 * SAMOA
 * %%
 * Copyright (C) 2013 Yahoo! Inc.
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import backtype.storm.Config;
import backtype.storm.StormSubmitter;
import backtype.storm.generated.AlreadyAliveException;
import backtype.storm.generated.InvalidTopologyException;
import backtype.storm.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

/**
 * Helper class to submit SAMOA task into Storm without the need of submitting the jar file.
 * The jar file must be submitted first using StormJarSubmitter class.
 * @author Arinto Murdopo
 *
 */
public class StormTopologySubmitter {

	public static String YJP_OPTIONS_KEY="YjpOptions";

	private static Logger logger = LoggerFactory.getLogger(StormTopologySubmitter.class);

	public static void main(String[] args) throws IOException{
		Properties props = StormSamoaUtils.getProperties();

		String uploadedJarLocation = props.getProperty(StormJarSubmitter.UPLOADED_JAR_LOCATION_KEY);
		if(uploadedJarLocation == null){
			logger.error("Invalid properties file. It must have key {}",
					StormJarSubmitter.UPLOADED_JAR_LOCATION_KEY);
			return;
		}

		List<String> tmpArgs = new ArrayList<>(Arrays.asList(args));
		int numWorkers = StormSamoaUtils.numWorkers(tmpArgs);

		args = tmpArgs.toArray(new String[tmpArgs.size()]);
		StormTopology stormTopo = StormSamoaUtils.argsToTopology(args);

		Config conf = new Config();
		conf.putAll(Utils.readStormConfig());
		conf.putAll(Utils.readCommandLineOpts());
		conf.setDebug(false);
		conf.setNumWorkers(numWorkers);

		String profilerOption =
				props.getProperty(StormTopologySubmitter.YJP_OPTIONS_KEY);
		if(profilerOption != null){
			String topoWorkerChildOpts =  (String) conf.get(Config.TOPOLOGY_WORKER_CHILDOPTS);
			StringBuilder optionBuilder = new StringBuilder();
			if(topoWorkerChildOpts != null){
				optionBuilder.append(topoWorkerChildOpts);
				optionBuilder.append(' ');
			}
			optionBuilder.append(profilerOption);
			conf.put(Config.TOPOLOGY_WORKER_CHILDOPTS, optionBuilder.toString());
		}
		Config config = new Config();
		config.putAll(Utils.readStormConfig());
		String topologyName = stormTopo.getTopologyName();
		try {
			System.out.println("Submitting topology with name: "
					+ topologyName);
            StormSubmitter.submitTopology(topologyName,config,stormTopo.getStormBuilder().createTopology());
			System.out.println(topologyName + " is successfully submitted");

		} catch (AlreadyAliveException aae) {
			System.out.println("Fail to submit " + topologyName
					+ "\nError message: " + aae.get_msg());
		} catch (InvalidTopologyException ite) {
			System.out.println("Invalid topology for " + topologyName);
			ite.printStackTrace();
		}
	}
}
