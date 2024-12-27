package com.onyx.darie.calin.gentleglowonyxboox.schedule;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.onyx.darie.calin.gentleglowonyxboox.R;
import com.onyx.darie.calin.gentleglowonyxboox.light.LightConfigurationEditor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

public class ScheduleEntryRecyclerViewAdapter
        extends RecyclerView.Adapter<ScheduleEntryRecyclerViewAdapter.ViewHolder>
{
    private final LightScheduler lightScheduler;
    private final LightConfigurationEditor lightConfigurationEditor;
    private ArrayList<ScheduleEntry> dataset;
    private Context context;

    public ScheduleEntryRecyclerViewAdapter(
            LightScheduler lightScheduler,
            LightConfigurationEditor lightConfigurationEditor,
            Context context,
            ArrayList<ScheduleEntry> dataset) {
        this.lightScheduler = lightScheduler;
        this.lightConfigurationEditor = lightConfigurationEditor;
        this.context = context;
        this.dataset = dataset;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_schedule_entry, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ScheduleEntry entry = dataset.get(position);
        holder.timeText.setText(entry.timeOfDay.toString());
        holder.lightOnSwitch.setChecked(entry.scheduledLightState.isOn);
        holder.lightConfigurationNameText.setText(entry.scheduledLightState.lightConfigurationNameMain);

        holder.lightOnSwitch.setOnClickListener(button -> {
            if (holder.lightOnSwitch.isChecked()) {
                final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(context, android.R.layout.select_dialog_item);

                final AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
                alertDialog.setTitle(context.getText(R.string.schedule_light_configuration));
                alertDialog.setOnCancelListener(dialog -> {
                    holder.lightOnSwitch.setChecked(false);
                });
                alertDialog.setSingleChoiceItems(arrayAdapter, -1, (dialog, selectedIndex) -> {
                    String item = arrayAdapter.getItem(selectedIndex);

                    ScheduleEntry editedEntry = new ScheduleEntry(entry.timeOfDay, ScheduledLightState.onWithConfiguration(item, selectedIndex));

                    lightScheduler.replace(entry.timeOfDay, editedEntry);
                    dataset.set(position, editedEntry);
                    holder.itemView.post(() -> notifyDataSetChanged());
                    dialog.dismiss();
                });
                arrayAdapter.addAll(
                        Arrays.stream(lightConfigurationEditor.getLightConfigurationChoice().getChoices())
                                .map(choice -> choice.name)
                                .collect(Collectors.toList()));

                alertDialog.show();
            } else {
                ScheduleEntry editedEntry = new ScheduleEntry(entry.timeOfDay, ScheduledLightState.off());
                lightScheduler.replace(entry.timeOfDay, editedEntry);
                dataset.set(position, editedEntry);
                holder.itemView.post(() -> notifyDataSetChanged());
            }
        });

        holder.deleteButton.setOnClickListener(v -> {
            dataset.remove(position);
            lightScheduler.remove(entry.timeOfDay);
            holder.itemView.post(() -> notifyDataSetChanged());
            Toast.makeText(context,
                    String.format(
                        this.context.getString(R.string.item_removed),
                        entry.timeOfDay
                    ),
                    Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return dataset.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView timeText;
        Switch lightOnSwitch;
        TextView lightConfigurationNameText;
        Button deleteButton;

        ViewHolder(View itemView) {
            super(itemView);
            timeText = itemView.findViewById(R.id.time_text);
            lightOnSwitch = itemView.findViewById((R.id.light_switch));
            lightConfigurationNameText = itemView.findViewById((R.id.light_configuration_name_text));
            deleteButton = itemView.findViewById(R.id.delete_button);
        }
    }
}
