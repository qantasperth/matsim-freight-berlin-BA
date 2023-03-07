package org.matsim.project;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.freight.carrier.*;
import org.matsim.vehicles.Vehicle;
import org.matsim.vehicles.VehicleType;

public class CarrierXmlWriterTest {

    private static final int FLEETSIZE = 10;
    private static final long DEPOT_LINK_ID = 116776;
    private static final String CARRIER_NAME = "Liefer-Startup";

    public static void main(String[] args) {

        // String inputCarrierXml = "scenarios/test/carrier.xml";
        String inputTypesXml = "scenarios/test/vehicleTypes.xml";
        String outputCarrierXml = "scenarios/test/carrier-test.xml";

        CarrierVehicleTypes types = new CarrierVehicleTypes();
        new CarrierVehicleTypeReader(types).readFile(inputTypesXml);
        VehicleType baykType = types.getVehicleTypes().get(Id.create("bayk-bring-s", VehicleType.class));

        Carriers carriers = new Carriers();
        // new CarrierPlanXmlReader(carriers, types).readFile(inputCarrierXml);

        Carrier carrier = CarrierUtils.createCarrier(Id.create(CARRIER_NAME, Carrier.class));
        CarrierUtils.setJspritIterations(carrier, 50);
        carrier.getCarrierCapabilities().setFleetSize(CarrierCapabilities.FleetSize.INFINITE);

        Id<Link> depotLinkId = Id.createLinkId(DEPOT_LINK_ID);

        for (int i=1; i < FLEETSIZE+1; i++) {

            CarrierVehicle.Builder builder = CarrierVehicle.Builder.newInstance(
                    Id.create("bayk" + i, Vehicle.class),
                    depotLinkId,
                    baykType);

            builder.
                    setEarliestStart(6*60*60).
                    setLatestEnd(22*60*60);
            CarrierVehicle vehicle = builder.build();

            carrier.getCarrierCapabilities().getCarrierVehicles().put(vehicle.getId(), vehicle);
        }

        carriers.addCarrier(carrier);
        new CarrierPlanWriter(carriers).write(outputCarrierXml);
    }
}
