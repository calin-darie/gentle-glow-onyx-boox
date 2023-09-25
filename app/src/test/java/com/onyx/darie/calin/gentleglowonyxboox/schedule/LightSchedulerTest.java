package com.onyx.darie.calin.gentleglowonyxboox.schedule;

import com.onyx.darie.calin.gentleglowonyxboox.ScheduleTestFixture;

import org.junit.Before;
import org.junit.Test;

import java.time.LocalTime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class LightSchedulerTest {
    // disallow schedules within 30 minutes of each other
    // simplest UI: 48 fixed choices, half an hour apart 00:00, 00:30, .. 23:00, 23:30

    //? repeat vs window?

    // cases when all alarms need to be re-created:
    // 1. boot time - RECEIVE_BOOT_COMPLETED permission + BOOT_COMPLETED BroadcastReceiver
    // 2. update - ACTION_PACKAGE_REPLACED
    // 3. time or timezone change

    // check daylight saving one day in advance! - if now + 24h is a different time, redo all alarms for tomorrow.

    // fixture.lightScheduler.restoreAllJobs();
    // assert same as added

    // test restore all saved
    @Test
    public void schedulesProfileChange() {
        String scheduledConfiguration = "Dawn";
        fixture.lightConfigurationEditorTestFixture.configurationEditor.getChooseCurrentLightConfigurationRequest$().onNext(2);
        assertNotEquals(scheduledConfiguration, fixture.lightConfigurationEditorTestFixture.configurationEditor.getLightConfigurationChoice().getSelected().name);

        fixture.add(new ScheduleEntry(
                LocalTime.parse("20:00"),
                ScheduledLightState.onWithConfiguration(scheduledConfiguration ,1)
                ));

        fixture.simulateIntentReceived();

        assertEquals(scheduledConfiguration, fixture.lightConfigurationEditorTestFixture.configurationEditor.getLightConfigurationChoice().getSelected().name);
    }

    @Before
    public void beforeEach() {
        fixture = new ScheduleTestFixture();
    }

    ScheduleTestFixture fixture;
}
