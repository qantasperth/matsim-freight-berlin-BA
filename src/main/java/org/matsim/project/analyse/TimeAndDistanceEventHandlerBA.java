package org.matsim.project.analyse;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.events.Event;
import org.matsim.api.core.v01.events.LinkEnterEvent;
import org.matsim.contrib.freight.events.FreightTourEndEvent;
import org.matsim.contrib.freight.events.FreightTourStartEvent;
import org.matsim.core.events.handler.BasicEventHandler;
import org.matsim.vehicles.Vehicle;

import java.util.LinkedHashMap;
import java.util.Map;

public class TimeAndDistanceEventHandlerBA implements BasicEventHandler {

    private final Scenario scenario;
    private final Map<String, Double> tourStarts = new LinkedHashMap<>();
    private final Map<Id<Vehicle>, Double> tourDistances = new LinkedHashMap<>();
    private final Map<Id<Vehicle>, Double> tourDurations = new LinkedHashMap<>();

    public TimeAndDistanceEventHandlerBA(Scenario scenario) {
        this.scenario = scenario;
    }

    private void handleEvent(FreightTourStartEvent event) {
        String key = event.getCarrierId().toString() + "_" + event.getTourId().toString();
        tourStarts.put(key, event.getTime());
    }

    private void handleEvent(FreightTourEndEvent event) {
        String key = event.getCarrierId().toString() + "_" + event.getTourId().toString();
        tourDurations.put(event.getVehicleId(), event.getTime() - tourStarts.get(key));
    }

    private void handleEvent(LinkEnterEvent event) {
        double distance = scenario.getNetwork().getLinks().get(event.getLinkId()).getLength();
        tourDistances.merge(event.getVehicleId(), distance, Double::sum);
    }

    @Override
    public void handleEvent(Event event) {
        if (event instanceof FreightTourStartEvent startEvent) {
            handleEvent(startEvent);
        } else if (event instanceof FreightTourEndEvent endEvent) {
            handleEvent(endEvent);
        } else if (event instanceof LinkEnterEvent enterEvent) {
            handleEvent(enterEvent);
        }
    }

    public Map<Id<Vehicle>, Double> getTourDistances() {
        return tourDistances;
    }

    public Map<Id<Vehicle>, Double> getTourDurations() {
        return tourDurations;
    }
}
