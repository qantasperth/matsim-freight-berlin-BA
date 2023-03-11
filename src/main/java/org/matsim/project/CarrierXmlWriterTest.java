package org.matsim.project;

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
import java.util.Collections;
import java.util.LinkedHashMap;

public class CarrierXmlWriterTest {

    // defining final vars
    private static final int FLEETSIZE = 10;
    private static final long DEPOT_LINK_ID = 116776;
    private static final String CARRIER_NAME = "Liefer-Startup";
    private static final int DELIVERY_SERVICE_TIME_MIN = 5;

    // defining paths to files
    public static String inputTypesXml = "scenarios/test/vehicleTypes.xml";
    public static String inputNetworkXml = "https://svn.vsp.tu-berlin.de/repos/public-svn/matsim/scenarios/countries/de/berlin/berlin-v5.5-10pct/input/berlin-v5.5-network.xml.gz";
    public static String outputCarrierXml = "scenarios/test/carrier-test.xml";
    public static String inputDeliveriesCsv = "scenarios/test/deliveries-test.csv";

    public static void main(String[] args) {

        // initializing vars
        Carriers carriers = new Carriers();
        Id<Link> depotLinkId = Id.createLinkId(DEPOT_LINK_ID);
        LinkedHashMap<String, Id<Link>> deliveries = initDeliveriesFromCsv(inputDeliveriesCsv);

        // load vehicle types
        CarrierVehicleTypes types = new CarrierVehicleTypes();
        new CarrierVehicleTypeReader(types).readFile(inputTypesXml);
        VehicleType baykType = types.getVehicleTypes().get(Id.create("bayk-bring-s", VehicleType.class));

        // create carrier
        Carrier carrier = CarrierUtils.createCarrier(Id.create(CARRIER_NAME, Carrier.class));
        CarrierUtils.setJspritIterations(carrier, 50);
        carrier.getCarrierCapabilities().setFleetSize(CarrierCapabilities.FleetSize.INFINITE);

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
            int size = createRansomShipmentSize();

            CarrierShipment shipment = CarrierShipment.Builder.newInstance(shipmentId, depotLinkId, nearestLink, size)
                    .setDeliveryServiceTime(DELIVERY_SERVICE_TIME_MIN*60)
                    .setDeliveryTimeWindow(TimeWindow.newInstance(6*60*60, 18*60*60))
                    .build();

            CarrierUtils.addShipment(carrier, shipment);
        }

        carriers.addCarrier(carrier);
        new CarrierPlanWriter(carriers).write(outputCarrierXml);
    }

    public static LinkedHashMap<String, Id<Link>> initDeliveriesFromCsv(String inputCsv) {

        // reading network file and filtering for TransportMode.car
        Network network = NetworkUtils.createNetwork();
        new MatsimNetworkReader(network).readFile(inputNetworkXml);
        Network carNetwork = NetworkUtils.createNetwork();
        new TransportModeNetworkFilter(network).filter(carNetwork, Collections.singleton(TransportMode.car));

        GeotoolsTransformation gT = new GeotoolsTransformation(TransformationFactory.WGS84,TransformationFactory.DHDN_GK4);

        // parsing deliveries from csv into LinkedHashMap
        LinkedHashMap<String, Id<Link>> deliveries = new LinkedHashMap<>();
        File deliveriesCsvFile = new File(inputCsv);
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
    public static int createRansomShipmentSize() {
        return 1;
    }
}

