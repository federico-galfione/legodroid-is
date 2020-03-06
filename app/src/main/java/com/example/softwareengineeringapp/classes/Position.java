package com.example.softwareengineeringapp.classes;

import android.util.Log;

public class Position {

    /*

    r = righa_max
    c = numero_colonne

    r,0 ___________ r,c
       |_|_|_|_|_|_|
       |_|_|_|_|_|_|
       |_|_|_|_|_|_|
       |_|_|_|_|_|_|
       |_|_|_|_|_|_|
       |_|_|_|_|_|_|
    0,0             0,c

    */

    public static final String orientazione_alto = "ALTO";
    public static final String orientazione_basso = "BASSO";
    public static final String orientazione_destra = "DESTRA";
    public static final String orientazione_sinistra = "SINISTRA";

    public static final String muovi_avanti = "MUOVI_AVANTI";
    public static final String muovi_indietro = "MUOVI_INDIETRO";
    public static final String muovi_ruota_destra = "RUOTA_DESTRA";
    public static final String muovi_ruota_sinistra = "RUOTA_SINISTRA";

    private int numero_righe=1;
    private int numero_colonne=1;

    private int riga=0;
    private int colonna=0;
    private String orientazione=Position.orientazione_alto;


    public Position(int numero_righe, int numero_colonne, int riga_partenza, int colonna_partenza, String orientazione_partenza){
        aggiorna_campo(numero_righe,numero_colonne);
        aggiorna_posizione(riga_partenza,colonna_partenza,orientazione_partenza);
    }

    public Position(int numero_righe, int numero_colonne){
        aggiorna_campo(numero_righe,numero_colonne);
    }


    public boolean muovi(String azione){
        boolean result = false;
        switch (azione){

            case Position.muovi_avanti:
                switch (orientazione){
                    case Position.orientazione_alto:
                        if(this.riga+1 < numero_righe){
                            aggiorna_posizione(this.riga+1,this.colonna);
                            result = true;
                        }
                        break;
                    case Position.orientazione_basso:
                        if(this.riga-1 >= 0){
                            aggiorna_posizione(this.riga-1,this.colonna);
                            result = true;
                        }
                        break;
                    case Position.orientazione_destra:
                        if(this.colonna+1 < numero_colonne){
                            aggiorna_posizione(this.riga,this.colonna+1);
                            result = true;
                        }
                        break;
                    case Position.orientazione_sinistra:
                        if(this.colonna-1 >= 0){
                            aggiorna_posizione(this.riga,this.colonna-1);
                            result = true;
                        }
                        break;
                }
                break;

            case Position.muovi_indietro:
                switch (orientazione){
                    case Position.orientazione_alto:
                        if(this.riga-1 >= 0){
                            aggiorna_posizione(this.riga-1,this.colonna);
                            result = true;
                        }
                        break;
                    case Position.orientazione_basso:
                        if(this.riga+1 < numero_righe){
                            aggiorna_posizione(this.riga+1,this.colonna);
                            result = true;
                        }
                        break;
                    case Position.orientazione_destra:
                        if(this.colonna-1 >= 0){
                            aggiorna_posizione(this.riga,this.colonna-1);
                            result = true;
                        }
                        break;
                    case Position.orientazione_sinistra:
                        if(this.colonna+1 < numero_colonne){
                            aggiorna_posizione(this.riga,this.colonna+1);
                            result = true;
                        }
                        break;
                }
                break;

            case Position.muovi_ruota_destra:
                switch (orientazione){
                    case Position.orientazione_alto:
                        aggiorna_posizione(Position.orientazione_destra);
                        break;
                    case Position.orientazione_basso:
                        aggiorna_posizione(Position.orientazione_sinistra);
                        break;
                    case Position.orientazione_destra:
                        aggiorna_posizione(Position.orientazione_basso);
                        break;
                    case Position.orientazione_sinistra:
                        aggiorna_posizione(Position.orientazione_alto);
                        break;
                }
                result = true;
                break;

            case Position.muovi_ruota_sinistra:
                switch (orientazione){
                    case Position.orientazione_alto:
                        aggiorna_posizione(Position.orientazione_sinistra);
                        break;
                    case Position.orientazione_basso:
                        aggiorna_posizione(Position.orientazione_destra);
                        break;
                    case Position.orientazione_destra:
                        aggiorna_posizione(Position.orientazione_alto);
                        break;
                    case Position.orientazione_sinistra:
                        aggiorna_posizione(Position.orientazione_basso);
                        break;
                }
                result = true;
                break;
        }
        return result;
    }
    

    //AGGIORNA POSIZIONE E CAMPO ATOMICI
    private boolean aggiorna_posizione(int nuova_riga, int nuova_colonna, String nuova_orientazione){
        int temp_riga=this.riga;
        int temp_colonna=this.colonna;
        boolean r1 = aggiorna_posizione(nuova_riga,nuova_colonna);
        if (!r1){
            return false;
        }
        boolean r2 = aggiorna_posizione(nuova_orientazione);
        if (!r2){
            aggiorna_posizione(temp_riga,temp_colonna);
            return false;
        }
        return true;
    }

    private boolean aggiorna_posizione(int nuova_riga, int nuova_colonna){
        if( (nuova_riga >= 0 && nuova_riga < numero_righe)
                && (nuova_colonna >=0 && nuova_colonna < numero_colonne)
        ){
            this.riga=nuova_riga;
            this.colonna=nuova_colonna;
            return true;
        }else{
            return false;
        }
    }

    private boolean aggiorna_posizione(String nuova_orientazione){
        if(nuova_orientazione.equals(Position.orientazione_alto)
            || nuova_orientazione.equals(Position.orientazione_basso)
            || nuova_orientazione.equals(Position.orientazione_destra)
            || nuova_orientazione.equals(Position.orientazione_sinistra)
        ){
            this.orientazione=nuova_orientazione;
            return true;
        }else{
            return false;
        }
    }

    public boolean aggiorna_campo(int nuova_numero_righe, int nuova_numero_colonne){
        if(nuova_numero_righe > 0 && nuova_numero_colonne > 0){
            this.numero_righe = nuova_numero_righe;
            this.numero_colonne = nuova_numero_colonne;
            return true;
        }else{
            return false;
        }
    }

    public int getRiga() {
        return riga;
    }

    public int getColonna() {
        return colonna;
    }

    public String getOrientazione() {
        return orientazione;
    }

    public int getNumero_righe() {
        return numero_righe;
    }

    public int getNumero_colonne() {
        return numero_colonne;
    }

}
