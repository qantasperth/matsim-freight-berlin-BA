package org.matsim.project;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.freight.carrier.*;
import org.matsim.vehicles.Vehicle;
import org.matsim.vehicles.VehicleType;
import org.matsim.vehicles.VehicleUtils;

import java.util.ArrayList;
import java.util.Collection;

public class CarrierBuilderFreightBerlinTest {


    public static void main(String[] args) {

        String inputCarrierXml = "scenarios/test/carrier.xml";
        String inputVehicleTypes = "scenarios/test/vehicleTypes.xml";
        String outputCarrierXml = "scenarios/test/carrier-modified.xml";
        String outputTypes = "scenarios/test/vehicleTypes-modified.xml";

        Carriers carriers = new Carriers();
        CarrierVehicleTypes carrierVehicleTypes = new CarrierVehicleTypes();
        CarrierPlanXmlReader carrierPlanXmlReader = new CarrierPlanXmlReader(carriers, carrierVehicleTypes);
        carrierPlanXmlReader.readFile(inputCarrierXml);

        System.out.print(carriers.getCarriers().get(0).getId().toString());


        /*
        // ArrayList<VehicleType> vehicleTypes = new ArrayList<>(1);
        CarrierVehicleTypes vehicleTypes = new CarrierVehicleTypes();
        CarrierVehicleTypeReader vehicleTypeReader = new CarrierVehicleTypeReader(vehicleTypes);
        vehicleTypeReader.readFile(inputVehicleTypes);











        Carriers carriers = new Carriers();
        Id<Carrier> carrierId = Id.create("test-carrier", Carrier.class);
        Carrier carrier = CarrierUtils.createCarrier(carrierId);
        carriers.addCarrier(carrier);

        CarrierCapabilities.Builder capabilitiesBuilder = CarrierCapabilities.Builder.newInstance();

        Id<Vehicle> vehicleId = Id.createVehicleId("bayk");
        Id<Link> locationId = Id.createLinkId("116776");
        Id<VehicleType> vehicleTypeId = Id.create("bayk-bring-s", VehicleType.class);

        VehicleType vehicleType = VehicleUtils.createVehicleType(vehicleTypeId)
                .setDescription("Cargobike BAYK Bring S")
                .setLength()
                .setWidth()
                .setMaximumVelocity(6.94)
                .set;


        Collection<VehicleType> types = carrier.getCarrierCapabilities().getVehicleTypes();


        CarrierVehicle.Builder vehicleBuilder = CarrierVehicle.Builder.newInstance(vehicleId, locationId, vehicleType);
        vehicleBuilder.set

        capabilitiesBuilder.setFleetSize(CarrierCapabilities.FleetSize.INFINITE);
        capabilitiesBuilder.addVehicle(vehicle);

        carrier.setCarrierCapabilities(carrierCapabilities);








        CarrierVehicleTypeWriter vehicleTypeWriter = new CarrierVehicleTypeWriter(types);
        vehicleTypeWriter.write(outputTypes);

        CarrierPlanWriter carrierPlanWriter = new CarrierPlanWriter(carriers);
        carrierPlanWriter.write(outputCarrierXml);

         */
    }


}
