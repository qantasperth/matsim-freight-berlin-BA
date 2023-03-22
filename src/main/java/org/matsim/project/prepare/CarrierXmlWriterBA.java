package org.matsim.project.prepare;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.freight.carrier.*;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.algorithms.TransportModeNetworkFilter;
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.core.utils.geometry.transformations.GeotoolsTransformation;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;
import org.matsim.vehicles.Vehicle;
import org.matsim.vehicles.VehicleType;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;

public class CarrierXmlWriterBA {

    // defining final vars
    private static final int FLEETSIZE = 2;
    private static final long DEPOT_LINK_ID = 116776;
    private static final String CARRIER_NAME = "Liefer-Startup";
    private static final int DELIVERY_SERVICE_TIME_MIN = 5;
    private static final CaseBA CASE = CaseBA.A2;

    // defining paths to files
    private static final String inputTypesXml = "input/vehicleTypes-BA.xml";
    private static final String inputDeliveriesCsv = "input/deliveries-test-100.csv";
    private static final String inputNetworkXml = "input/berlin-v5.5-network.xml.gz";
    private static final String outputCarrierXml = "scenarios/case-a2/carrier-caseA2-100.xml";

    public static void main(String[] args) {

        // initializing vars
        Carriers carriers = new Carriers();
        Id<Link> depotLinkId = Id.createLinkId(DEPOT_LINK_ID);
        LinkedHashMap<String, Id<Link>> deliveries = initDeliveriesFromCsv();

        // load vehicle types
        CarrierVehicleTypes types = new CarrierVehicleTypes();
        new CarrierVehicleTypeReader(types).readFile(inputTypesXml);
        VehicleType baykType = types.getVehicleTypes().get(Id.create("bayk-bring-s", VehicleType.class));

        // create carrier
        Carrier carrier = CarrierUtils.createCarrier(Id.create(CARRIER_NAME, Carrier.class));
        CarrierUtils.setJspritIterations(carrier, 50);
        carrier.getCarrierCapabilities().setFleetSize(CarrierCapabilities.FleetSize.FINITE);

        // create vehicles and assigning to the carrier
        for (int i=1; i < FLEETSIZE+1; i++) {

            CarrierVehicle.Builder builder = CarrierVehicle.Builder.newInstance(
                    Id.create("bayk" + i, Vehicle.class),
                    depotLinkId,
                    baykType);

            builder.setEarliestStart(6*60*60);
            builder.setLatestEnd(22*60*60);
            CarrierVehicle vehicle = builder.build();

            carrier.getCarrierCapabilities().getCarrierVehicles().put(vehicle.getId(), vehicle);
        }

        // creating shipments and assigning to the carrier
        for (String id : deliveries.keySet()) {

            Id<CarrierShipment> shipmentId = Id.create(id, CarrierShipment.class);
            Id<Link> nearestLink = deliveries.get(id);
            int size = createRandomShipmentSize();

            CarrierShipment shipment = CarrierShipment.Builder.newInstance(shipmentId, depotLinkId, nearestLink, size)
                    .setDeliveryServiceTime(DELIVERY_SERVICE_TIME_MIN*60)
                    .setDeliveryTimeWindow(loadDeliveryTimeWindow())
                    .build();

            CarrierUtils.addShipment(carrier, shipment);
        }

        carriers.addCarrier(carrier);
        new CarrierPlanWriter(carriers).write(outputCarrierXml);
    }

    private static LinkedHashMap<String, Id<Link>> initDeliveriesFromCsv() {

        // reading network file and filtering for TransportMode.car
        Network network = NetworkUtils.createNetwork();
        new MatsimNetworkReader(network).readFile(inputNetworkXml);
        Network carNetwork = NetworkUtils.createNetwork();
        new TransportModeNetworkFilter(network).filter(carNetwork, Collections.singleton(TransportMode.car));

        GeotoolsTransformation gT = new GeotoolsTransformation(TransformationFactory.WGS84,TransformationFactory.DHDN_GK4);

        // parsing deliveries from csv into LinkedHashMap
        LinkedHashMap<String, Id<Link>> deliveries = new LinkedHashMap<>();
        File deliveriesCsvFile = new File(CarrierXmlWriterBA.inputDeliveriesCsv);
        CSVParser parser = null;

        try {
            parser = CSVParser.parse(deliveriesCsvFile, Charset.defaultCharset(), CSVFormat.EXCEL);
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (CSVRecord row : parser) {
            Coord coord = new Coord(Double.parseDouble(row.get(2)), Double.parseDouble(row.get(1)));
            Coord epsg31468Coord = gT.transform(coord);
            Id<Link> linkId = NetworkUtils.getNearestRightEntryLink(carNetwork, epsg31468Coord).getId();
            deliveries.put(row.get(0), linkId);
        }

        return deliveries;
    }


    // TODO: 08.03.2023 create shipment size randomizer 
    private static int createRandomShipmentSize() {

        // double minSize = 0.5;
        // double maxSize = 5;
        // Random random = new Random(1000);

        return 1;
    }

    private static TimeWindow loadDeliveryTimeWindow() {

        Random random = new Random();

        switch (CarrierXmlWriterBA.CASE) {
            case TEST -> { // no TW, working hours 6-22
                return TimeWindow.newInstance(6 * 60 * 60, 22 * 60 * 60);
            }
            case A1 -> { // randomly assigned consecutive 4-hour TW from 8-20 (3 options)
                return switch (random.nextInt(3)) {
                    case 0 -> TimeWindow.newInstance(8 * 60 * 60, 12 * 60 * 60);
                    case 1 -> TimeWindow.newInstance(12 * 60 * 60, 16 * 60 * 60);
                    case 2 -> TimeWindow.newInstance(16 * 60 * 60, 20 * 60 * 60);
                    default -> throw new IllegalStateException("Unexpected value: " + random.nextInt(6));
                };
            }
            case A2 -> { // randomly assigned consecutive 2-hour TW from 8-20 (6 options)
                return switch (random.nextInt(6)) {
                    case 0 -> TimeWindow.newInstance(8 * 60 * 60, 10 * 60 * 60);
                    case 1 -> TimeWindow.newInstance(10 * 60 * 60, 12 * 60 * 60);
                    case 2 -> TimeWindow.newInstance(12 * 60 * 60, 14 * 60 * 60);
                    case 3 -> TimeWindow.newInstance(14 * 60 * 60, 16 * 60 * 60);
                    case 4 -> TimeWindow.newInstance(16 * 60 * 60, 18 * 60 * 60);
                    case 5 -> TimeWindow.newInstance(18 * 60 * 60, 20 * 60 * 60);
                    default -> throw new IllegalStateException("Unexpected value: " + random.nextInt(6));
                };
            }

        }
        return TimeWindow.newInstance(0.0, 24*60*60); // default time window
    }

    private enum CaseBA { TEST, A1, A2, A3, B1, B2, B3 }
}

