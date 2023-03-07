package org.matsim.project;

import org.matsim.api.core.v01.Id;
import org.matsim.contrib.freight.carrier.*;
import org.matsim.vehicles.VehicleType;

public class VehicleTypeModifierTest {

    public static void main(String[] args) {

        String inputTypesXml = "scenarios/test/vehicleTypes.xml";
        String outputTypesXml = "scenarios/test/vehicleTypes-modified.xml";

        CarrierVehicleTypes types = new CarrierVehicleTypes();
        new CarrierVehicleTypeReader(types).readFile(inputTypesXml);
        types.getVehicleTypes().get(Id.create("bayk-bring-s", VehicleType.class)).setLength(5);

        new CarrierVehicleTypeWriter(types).write(outputTypesXml);

    }


}
