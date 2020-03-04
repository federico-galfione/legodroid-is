package com.example.softwareengineeringapp.ui.level2;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.softwareengineeringapp.classes.Coordinates;
import com.example.softwareengineeringapp.R;

import java.util.ArrayList;
public class Level2Fragment extends Fragment {

    private TableLayout fieldContainer;
    private Button createGridButton;
    private Button startLevelButton;
    private ArrayList field = new ArrayList<Coordinates>();
    private ArrayList selectedCells = new ArrayList<Coordinates>();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_level2, container, false);

        fieldContainer = root.findViewById(R.id.field_container);
        createGridButton = root.findViewById(R.id.create_grid);
        startLevelButton = root.findViewById(R.id.start_level2_button);

        startLevelButton.setOnClickListener(this::startLevel2);

        startLevelButton.setVisibility(View.INVISIBLE);


        // Clicco Il bottone "CREA GRIGLIA"
        createGridButton.setOnClickListener((view) -> {
            int numRowLength = ((EditText) root.findViewById(R.id.num_row)).getText().length();
            int numColLength = ((EditText) root.findViewById(R.id.num_col)).getText().length();
            int numRow, numCol;
            if(numColLength != 0 && numRowLength != 0){
                numRow = Integer.parseInt(((EditText) root.findViewById(R.id.num_row)).getText().toString());
                numCol = Integer.parseInt(((EditText) root.findViewById(R.id.num_col)).getText().toString());
                this.createGrid(numRow, numCol);
            } else {
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(getActivity());
                AlertDialog alert = alertBuilder.setTitle("Errore creazione").setMessage("Devi inserire entrambi i valori per procedere").create();
                alert.show();
            }
        });

        return root;
    }

    private void createGrid(int numRow, int numCol){
        this.fieldContainer.removeAllViewsInLayout();
        this.field.clear();
        this.selectedCells.clear();
        // Questo ciclo for genera la grglia con il numero delle celle e lo switch
        for(int i = numRow - 1; i >= 0; i--){
            TableRow row = (TableRow) LayoutInflater.from(this.fieldContainer.getContext()).inflate(R.layout.grid_row, null);
            for(int j = 0; j < numCol; j++){
                TableLayout cell = (TableLayout) LayoutInflater.from(this.fieldContainer.getContext()).inflate(R.layout.grid_cell, null);
                cell.setTag("cell_"+i+"_"+j);
                ((TextView) cell.findViewById(R.id.cell_position)).setText(i+", "+j);
                this.field.add(new Coordinates(i, j, cell.findViewById(R.id.select_cell)));
                row.addView(cell, this.fieldContainer.getLayoutParams());
                row.invalidate();
            }
            this.fieldContainer.addView(row, this.fieldContainer.getLayoutParams());
            this.fieldContainer.invalidate();
        }
        // Rende visibile il bottone per l'avvio del livello
        startLevelButton.setVisibility(View.VISIBLE);
    }

    private void startLevel2(View view){
        // Riempie selectedCells con le celle selezionate in quel momento (quelle che contengono una palla)
        this.field.forEach((cell) -> {
            if(((Coordinates) cell).getHasBall()){
                this.selectedCells.add(cell);
            }
        });
    }

}