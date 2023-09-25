package com.onyx.darie.calin.gentleglowonyxboox;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.onyx.darie.calin.gentleglowonyxboox.light.Light;
import com.onyx.darie.calin.gentleglowonyxboox.light.LightConfigurationEditor;
import com.onyx.darie.calin.gentleglowonyxboox.schedule.LightScheduler;
import com.onyx.darie.calin.gentleglowonyxboox.schedule.ScheduleEntry;
import com.onyx.darie.calin.gentleglowonyxboox.setup.Dependencies;
import com.onyx.darie.calin.gentleglowonyxboox.setup.GentleGlowApplication;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.function.Function;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class ScheduleTestFixture {
    public final LightConfigurationEditorTestFixture lightConfigurationEditorTestFixture =
            new LightConfigurationEditorTestFixture();
    public final LightScheduler lightScheduler;


    public ScheduleTestFixture() {
        MockitoAnnotations.openMocks(this);
        when(intentMock.putExtra(anyString(), anyString())).thenAnswer(invocation-> {
            String key = (String)invocation.getArgument(0);
            String value = (String)invocation.getArgument(1);

            when(intentMock.getStringExtra(key)).thenReturn(value);
            return intentMock;
        });

        when(contextMock.getSystemService(Context.ALARM_SERVICE))
                .thenReturn(alarmManagerMock);
        when(contextMock.getApplicationContext()).thenReturn(new GentleGlowApplication(){
            @Override
            public Dependencies getDependencies() {
                return new Dependencies() {
                    @Override
                    public Light getLight() {
                        return lightConfigurationEditorTestFixture.lightTestFixture.light;
                    }

                    @Override
                    public LightConfigurationEditor getLightConfigurationEditor() {
                        return lightConfigurationEditorTestFixture.configurationEditor;
                    }
                };
            }
        });

        lightScheduler = new LightScheduler() {
            @Override
            protected PendingIntent getPendingIntent(Context context, Function<Intent, Void> intentConfig) {
                intentConfig.apply(intentMock);
                return pendingIntentMock;
            }
        };
    }

    public void add(ScheduleEntry scheduleEntry) {
        lightScheduler.add(contextMock, scheduleEntry);
    }

    public void simulateIntentReceived() {
        lightScheduler.onReceive(contextMock, intentMock);
    }

    @Mock
    private AlarmManager alarmManagerMock;
    @Mock
    private Context contextMock;
    @Mock
    private Intent intentMock;

    @Mock
    private PendingIntent pendingIntentMock;
}
