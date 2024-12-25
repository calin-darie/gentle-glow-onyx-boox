package com.onyx.darie.calin.gentleglowonyxboox.schedule;

import android.app.Activity;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Switch;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.onyx.darie.calin.gentleglowonyxboox.R;
import com.onyx.darie.calin.gentleglowonyxboox.light.LightConfigurationEditor;
import com.onyx.darie.calin.gentleglowonyxboox.setup.GentleGlowApplication;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;

public class ScheduleActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        lightScheduler = ((GentleGlowApplication)getApplication()).getScheduleDependencies().getLightScheduler();
        LightConfigurationEditor lightConfigurationEditor = ((GentleGlowApplication)getApplication()).getDependencies().getLightConfigurationEditor();

        scheduleEntries = lightScheduler.getSchedule().entries;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);

        bindScheduleSwitch();
        bindScheduleEntryList(lightConfigurationEditor);
    }

    private void bindScheduleSwitch() {
        final Switch scheduleSwitch = findViewById(R.id.schedule_switch);
        scheduleSwitch.setChecked((lightScheduler.isOn()));

        scheduleSwitch.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            if (isChecked) {
                lightScheduler.turnOn();
            } else {
                lightScheduler.turnOff();
            }
        });
    }

    private void bindScheduleEntryList(LightConfigurationEditor lightConfigurationEditor) {
        alarmRecyclerView = findViewById(R.id.schedule_recycler_view);
        alarmRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        scheduleEntryRecyclerViewAdapter = new ScheduleEntryRecyclerViewAdapter(
                lightScheduler,
                lightConfigurationEditor,
                this, scheduleEntries);
        alarmRecyclerView.setAdapter(scheduleEntryRecyclerViewAdapter);

        Button addAlarmButton = findViewById(R.id.add_schedule_entry);
        addAlarmButton.setOnClickListener(v -> {
            Calendar currentTime = Calendar.getInstance();
            int initialHour = currentTime.get(Calendar.HOUR_OF_DAY);
            int initialMinute = currentTime.get(Calendar.MINUTE);

            TimePickerDialog timePicker = new TimePickerDialog(
                    ScheduleActivity.this,
                    R.style.time_picker_dialog_clock,
                    (view, selectedHour, selectedMinute) -> {
                ScheduleEntry newEntry = new ScheduleEntry(LocalTime.of(selectedHour, selectedMinute), ScheduledLightState.off());
                boolean success = lightScheduler.add(newEntry);
                if (! success)
                    return;
                scheduleEntries.add(newEntry);
                scheduleEntries.sort(Comparator.comparing(e -> e.timeOfDay));
                scheduleEntryRecyclerViewAdapter.notifyDataSetChanged();
            }, initialHour, initialMinute, true);
            timePicker.show();
        });
    }

    private RecyclerView alarmRecyclerView;
    private ScheduleEntryRecyclerViewAdapter scheduleEntryRecyclerViewAdapter;
    private LightScheduler lightScheduler;
    private ArrayList<ScheduleEntry> scheduleEntries;
}