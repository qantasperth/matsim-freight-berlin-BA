/* *********************************************************************** *
 * project: org.matsim.*												   *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2008 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */
package org.matsim.project.run;

import org.matsim.api.core.v01.Scenario;
import org.matsim.contrib.freight.FreightConfigGroup;
import org.matsim.contrib.freight.controler.CarrierModule;
import org.matsim.contrib.freight.controler.FreightUtils;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy.OverwriteFileSetting;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.project.prepare.CaseBA;

import java.util.concurrent.ExecutionException;

/**
 * @author nagel
 *
 */
public class RunMatsimFreightBerlinBA {

	public static void main(String[] args) throws ExecutionException, InterruptedException {

		CaseBA CASE;
		if ( args==null || args.length==0 || args[0]==null )
			CASE = CaseBA.A3; // case specs for IDE
		else
			CASE = CaseBA.valueOf(args[0]); // case specs for shell

		System.out.println(CASE);

		// set up config
		Config config = ConfigUtils.loadConfig( "scenarios/config.xml" );
		config.plans().setInputFile(null);
		config.controler().setOutputDirectory("scenarios/case-" + CASE + "/output");
		config.controler().setLastIteration(0);

		// add freight config group
		FreightConfigGroup freightConfigGroup = ConfigUtils.addOrGetModule(config, FreightConfigGroup.class);
		freightConfigGroup.setCarriersFile("case-" + CASE + "/carrier-" + CASE + ".xml");
		freightConfigGroup.setCarriersVehicleTypesFile("vehicleTypes-BA.xml");

		// setting network input file
		config.controler().setOverwriteFileSetting( OverwriteFileSetting.deleteDirectoryIfExists );
		config.network().setInputFile("berlin-v5.5-network.xml.gz");

		// load carriers and run jsprit
		Scenario scenario = ScenarioUtils.loadScenario(config) ;
		FreightUtils.loadCarriersAccordingToFreightConfig(scenario);
		FreightUtils.runJsprit(scenario);

		// run matsim
		final Controler controler = new Controler( scenario ) ;
		controler.addOverridingModule(new CarrierModule());
		controler.run();
	}
}