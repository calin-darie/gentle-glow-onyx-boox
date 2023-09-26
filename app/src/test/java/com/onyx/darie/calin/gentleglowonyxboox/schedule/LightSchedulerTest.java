package com.onyx.darie.calin.gentleglowonyxboox.schedule;

import android.app.PendingIntent;

import com.google.gson.Gson;
import com.onyx.darie.calin.gentleglowonyxboox.ScheduleTestFixture;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalTime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.Mockito.verify;

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
    public void schedulesLightConfigurationChange() {
        String scheduledConfiguration = "Dawn";
        fixture.lightConfigurationEditorTestFixture.configurationEditor.getChooseCurrentLightConfigurationRequest$().onNext(2);
        assertNotEquals(scheduledConfiguration, fixture.lightConfigurationEditorTestFixture.configurationEditor.getLightConfigurationChoice().getSelected().name);

        fixture.lightScheduler.add(new ScheduleEntry(
                LocalTime.parse("20:00"),
                ScheduledLightState.onWithConfiguration(scheduledConfiguration ,1)
                ));

        fixture.simulateIntentReceived();

        assertEquals(scheduledConfiguration, fixture.lightConfigurationEditorTestFixture.configurationEditor.getLightConfigurationChoice().getSelected().name);
    }

    @Test
    public void canSchedulesLightOff() {
        fixture.lightConfigurationEditorTestFixture.lightTestFixture.light.turnOn();

        fixture.lightScheduler.add(new ScheduleEntry(
                LocalTime.parse("20:00"),
                ScheduledLightState.off()
                ));

        fixture.simulateIntentReceived();

        fixture.lightConfigurationEditorTestFixture.lightTestFixture.verifyTurnedOff();
    }

    @Test
    public void canRemoveScheduledLightConfigurationChange() {
        ScheduleEntry scheduleEntry = new ScheduleEntry(LocalTime.parse("15:00"),
                ScheduledLightState.onWithConfiguration("Day", 2));
        fixture.lightScheduler.add(scheduleEntry);
        fixture.resetIntentMock();
        fixture.lightScheduler.remove(LocalTime.parse("15:00"));

        assertEquals(
                new Gson().toJson(scheduleEntry),
                fixture.intentMock.getStringExtra(LightScheduler.ScheduleEntryIntentKey)
        );

        verify(fixture.alarmManagerMock).cancel(ArgumentCaptor.forClass(PendingIntent.class).capture());
    }

    @Before
    public void beforeEach() {
        fixture = new ScheduleTestFixture();
    }

    ScheduleTestFixture fixture;
}
