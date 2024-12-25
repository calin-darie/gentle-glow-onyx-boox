package com.onyx.darie.calin.gentleglowonyxboox.schedule;

import android.app.PendingIntent;

import com.onyx.darie.calin.gentleglowonyxboox.ScheduleTestFixture;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalTime;

import static org.mockito.Mockito.verify;

public class LightSchedulerTest {
    @Test
    public void canRemoveScheduledLightConfigurationChange() {
        ScheduleEntry scheduleEntry = new ScheduleEntry(LocalTime.parse("15:00"),
                ScheduledLightState.onWithConfiguration("Day", 2));
        fixture.lightScheduler.add(scheduleEntry);
        fixture.resetIntentMock();
        fixture.lightScheduler.remove(LocalTime.parse("15:00"));

        verify(fixture.alarmManagerMock).cancel(ArgumentCaptor.forClass(PendingIntent.class).capture());
    }

    @Before
    public void beforeEach() {
        fixture = new ScheduleTestFixture();
    }

    ScheduleTestFixture fixture;
}
