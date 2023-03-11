package org.matsim.project;

import org.matsim.api.core.v01.Id;
import org.matsim.contrib.freight.carrier.*;
import org.matsim.vehicles.VehicleType;
import org.matsim.vehicles.VehicleUtils;

/// TODO: 11.03.2023 set actual costs, length, speed and other parameters

public class VehicleTypeXmlWriterBA {

    public static void main(String[] args) {

        // String inputTypesXml = "scenarios/test/vehicleTypes.xml";
        String outputTypesXml = "input/vehicleTypes-BA.xml";

        CarrierVehicleTypes types = new CarrierVehicleTypes();
        // new CarrierVehicleTypeReader(types).readFile(inputTypesXml);
        // VehicleType bayk = types.getVehicleTypes().get(Id.create("bayk-bring-s", VehicleType.class));

        Id<VehicleType> baykId = Id.create("bayk-bring-s", VehicleType.class);
        VehicleType bayk = VehicleUtils.createVehicleType(baykId);

        bayk.setDescription("Bayk Bring S");
        bayk.getCapacity().setOther(50);
        bayk.setLength(3.0)
            .setWidth(1.0);
        bayk.setMaximumVelocity(25 / 3.6);
        bayk.setNetworkMode("car");
        bayk.setFlowEfficiencyFactor(1);
        bayk.getCostInformation()
                .setFixedCost(100.0)
                .setCostsPerMeter(1.5)
                .setCostsPerSecond(2.5);

        types.getVehicleTypes().put(baykId, bayk);

        new CarrierVehicleTypeWriter(types).write(outputTypesXml);
    }
}
