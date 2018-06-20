package edu.uic.cs442.msms.adapter;

/**
 * Created by jun on 3/9/18.
 */

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import clarifai2.dto.prediction.Concept;
import edu.uic.cs442.msms.R;
import edu.uic.cs442.msms.manager.DataManager;


import java.util.ArrayList;
import java.util.List;

public class RecognizeConceptsAdapter extends RecyclerView.Adapter<RecognizeConceptsAdapter.Holder> {

    @NonNull private List<Concept> concepts = new ArrayList<>();

    DataManager dataManager = DataManager.getInstance();


    public RecognizeConceptsAdapter setData(@NonNull List<Concept> concepts) {
        this.concepts = concepts;
        notifyDataSetChanged();
        return this;
    }

    @Override public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_concept, parent, false));
    }

    @Override public void onBindViewHolder(Holder holder, int position) {
        final Concept concept = concepts.get(position);
        holder.label.setText(concept.name() != null ? concept.name() : concept.id());
        holder.probability.setText(String.valueOf(concept.value()));

        if(dataManager.containsDanger(concept.name())){
             holder.label.setTextColor(Color.RED);
        }
    }

    @Override public int getItemCount() {
        return concepts.size();
    }

    final class Holder extends RecyclerView.ViewHolder {

        TextView label;
        TextView probability;

        private Holder(View view) {
            super(view);

            label = view.findViewById(R.id.label);
            probability = view.findViewById(R.id.probability);
        }
    }
}