package com.onyx.darie.calin.gentleglowonyxboox;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.onyx.darie.calin.gentleglowonyxboox.schedule.LightScheduler;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.function.Function;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

public class ScheduleTestFixture {
    public final LightConfigurationEditorTestFixture lightConfigurationEditorTestFixture =
            new LightConfigurationEditorTestFixture();
    public final LightScheduler lightScheduler;


    public ScheduleTestFixture() {
        MockitoAnnotations.openMocks(this);

        resetIntentMock();

        when(contextMock.getSystemService(Context.ALARM_SERVICE))
                .thenReturn(alarmManagerMock);

        lightScheduler = new LightScheduler(
                contextMock,
                new FakeStorage<LightScheduler.Schedule>(),
                lightConfigurationEditorTestFixture.configurationEditor,
                lightConfigurationEditorTestFixture.lightTestFixture.light
        ) {
            @Override
            protected PendingIntent getPendingIntent(Function<Intent, Void> intentConfig) {
                intentConfig.apply(intentMock);
                return pendingIntentMock;
            }
        };
    }

    public void simulateIntentReceived() {
        lightScheduler.handleAlarm(intentMock);
    }
    @Mock
    public AlarmManager alarmManagerMock;
    @Mock
    private Context contextMock;

    @Mock
    public Intent intentMock;

    @Mock
    private PendingIntent pendingIntentMock;

    public void resetIntentMock() {
        reset(intentMock);

        when(intentMock.putExtra(anyString(), anyString())).thenAnswer(invocation-> {
            String key = (String)invocation.getArgument(0);
            String value = (String)invocation.getArgument(1);

            when(intentMock.getStringExtra(key)).thenReturn(value);
            return intentMock;
        });
    }
}
