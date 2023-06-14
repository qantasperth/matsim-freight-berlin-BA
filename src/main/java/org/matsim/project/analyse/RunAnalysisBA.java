package org.matsim.project.analyse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.contrib.freight.events.FreightEventsReaders;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.events.MatsimEventsReader;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.project.prepare.CaseBA;
import org.matsim.vehicles.Vehicle;
import org.matsim.vehicles.VehicleType;
import org.matsim.vehicles.VehicleUtils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

public class RunAnalysisBA {

    private static final Logger log = LogManager.getLogger(TimeAndDistanceEventHandlerBA.class);

    private static final CaseBA CASE = CaseBA.B3;

    private static final String OUTPUT_DIR = "scenarios/case-" + CASE + "/output/";
    private static final String OUTPUT_ANALYSIS_DIR = "scenarios/case-" + CASE + "/analysis/";
    private static final String outputNetworkXml = OUTPUT_DIR + "output_network.xml.gz";
    private static final String outputEventsXml = OUTPUT_DIR + "output_events.xml.gz";
    private static final String outputVehiclesXml = OUTPUT_DIR + "output_allVehicles.xml.gz";
    private static final String outputAnalysisTsv = OUTPUT_ANALYSIS_DIR + "time-distance-analysis-" + CASE + ".tsv";

    public static void main(String[] args) throws IOException {

        Config config = ConfigUtils.createConfig();
        config.vehicles().setVehiclesFile(outputVehiclesXml);
        config.network().setInputFile(outputNetworkXml);
        config.plans().setInputFile(null);
        config.parallelEventHandling().setNumberOfThreads(null);
        config.parallelEventHandling().setEstimatedNumberOfEvents(null);
        config.global().setNumberOfThreads(1);

        Scenario scenario = ScenarioUtils.loadScenario(config);

        EventsManager eventsManager = EventsUtils.createEventsManager();

        TimeAndDistanceEventHandlerBA timeAndDistanceEventHandler = new TimeAndDistanceEventHandlerBA(scenario);
        eventsManager.addHandler(timeAndDistanceEventHandler);

        eventsManager.initProcessing();
            MatsimEventsReader eventsReader = FreightEventsReaders.createEventsReader(eventsManager);
            eventsReader.readFile(outputEventsXml);
        eventsManager.finishProcessing();

        log.info("Analysis completed.");
        log.info("Writing output...");
        log.info("Writing out Time & Distance & Costs ...");

        writeAnalysisTsv(scenario, timeAndDistanceEventHandler);
    }

    private static void writeAnalysisTsv(Scenario scenario, TimeAndDistanceEventHandlerBA handler) throws IOException {

        log.info("Writing TSV started");

        Map<Id<Vehicle>, Double> tourDurations = handler.getTourDurations();
        Map<Id<Vehicle>, Double> tourDistances = handler.getTourDistances();
        double sumDuration = 0.0;
        double sumDistance = 0.0;
        double sumTimeCosts = 0.0;
        double sumDistanceCosts = 0.0;
        double sumTotalCosts = 0.0;

        BufferedWriter writer = new BufferedWriter(new FileWriter(outputAnalysisTsv));

        writer.write("vehicleId \t tourDuration[s] \t travelDistance[m] \t " +
                            "costPerSecond[EUR/s] \t costPerMeter[EUR/m] \t fixedCosts[EUR] \t " +
                            "varCostsTime[EUR] \t varCostsDist[EUR] \t totalCosts[EUR]");
        writer.newLine();

        for (Id<Vehicle> vehicleId : tourDistances.keySet()) {

            VehicleType baykType = VehicleUtils.findVehicle(vehicleId, scenario).getType();

            double costsPerSecond = baykType.getCostInformation().getCostsPerSecond();
            double costsPerMeter = baykType.getCostInformation().getCostsPerMeter();
            double fixedCosts = baykType.getCostInformation().getFixedCosts();
            double timeCosts = costsPerSecond * tourDurations.get(vehicleId);
            double distanceCosts = costsPerMeter * tourDistances.get(vehicleId);
            double totalCosts = fixedCosts + timeCosts + distanceCosts;

            sumDuration += tourDurations.get(vehicleId);
            sumDistance += tourDistances.get(vehicleId);
            sumTimeCosts += timeCosts;
            sumDistanceCosts += distanceCosts;
            sumTotalCosts += totalCosts;

            writer.write(vehicleId.toString() + "\t"
                            + tourDurations.get(vehicleId) + "\t"
                            + tourDistances.get(vehicleId) + "\t"
                            + costsPerSecond + "\t"
                            + costsPerMeter + "\t"
                            + fixedCosts + "\t"
                            + timeCosts + "\t"
                            + distanceCosts + "\t"
                            + totalCosts
                    );

            writer.newLine();
        }

        int numVehicles = tourDistances.size();

        writer.write("AVERAGE" + "\t"
                + sumDuration/numVehicles + "\t"
                + sumDistance/numVehicles + "\t"
                + "-" + "\t" + "-" + "\t" + "-" + "\t"
                + sumTimeCosts/numVehicles + "\t"
                + sumDistanceCosts/numVehicles + "\t"
                + sumTotalCosts/numVehicles);
        writer.newLine();

        writer.write("TOTAL" + "\t"
                + sumDuration + "\t"
                + sumDistance + "\t"
                + "-" + "\t" + "-" + "\t" + "-" + "\t"
                + sumTimeCosts + "\t"
                + sumDistanceCosts + "\t"
                + sumTotalCosts);

        writer.close();
        log.info("Writing TSV completed");
    }
}
