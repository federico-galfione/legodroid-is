package com.example.softwareengineeringapp.classes;

import java.util.ArrayList;
import java.util.Iterator;

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


    public ArrayList<String> calcola_percorso(int target_r, int target_c){

        ArrayList<String> result = new ArrayList<>();

        int temp_r = riga;
        int temp_c = colonna;
        String temp_o = orientazione;

        String posizione_target_c;
        String posizione_target_r;

        while(target_r!=temp_r && target_c!=temp_c){

            if(target_c > temp_c){
                posizione_target_c = Position.orientazione_destra;
            }else if(target_c < temp_c){
                posizione_target_c = Position.orientazione_sinistra;
            }else{
                posizione_target_c = "x";
            }

            if(target_r > temp_r){
                posizione_target_r = Position.orientazione_alto;
            }else if(target_r < temp_r){
                posizione_target_r = Position.orientazione_basso;
            }else{
                posizione_target_r = "x";
            }

            if(!posizione_target_r.equals(temp_o) && !posizione_target_r.equals("x")){

                switch (temp_o){
                    case Position.orientazione_basso:
                        result.add(Position.muovi_ruota_destra);
                        result.add(Position.muovi_ruota_destra);
                        break;
                    case Position.orientazione_alto:
                        result.add(Position.muovi_ruota_destra);
                        result.add(Position.muovi_ruota_destra);
                        break;
                    case Position.orientazione_destra:
                        if(posizione_target_r.equals(Position.orientazione_alto)){
                            result.add(Position.muovi_ruota_sinistra);
                        }else{
                            result.add(Position.muovi_ruota_destra);
                        }
                        break;
                    case Position.orientazione_sinistra:
                        if(posizione_target_r.equals(Position.orientazione_alto)){
                            result.add(Position.muovi_ruota_destra);
                        }else{
                            result.add(Position.muovi_ruota_sinistra);
                        }
                        break;
                }

                temp_o = posizione_target_r;

            }else if(posizione_target_r.equals("x")){

                if (!posizione_target_c.equals(temp_o) && !posizione_target_c.equals("x")) {
                    switch (temp_o) {
                        case Position.orientazione_basso:
                            if (posizione_target_c.equals(Position.orientazione_destra)) {
                                result.add(Position.muovi_ruota_sinistra);
                            } else {
                                result.add(Position.muovi_ruota_destra);
                            }
                            break;
                        case Position.orientazione_alto:
                            if (posizione_target_c.equals(Position.orientazione_destra)) {
                                result.add(Position.muovi_ruota_destra);
                            } else {
                                result.add(Position.muovi_ruota_sinistra);
                            }
                            break;
                        case Position.orientazione_destra:
                            result.add(Position.muovi_ruota_destra);
                            result.add(Position.muovi_ruota_destra);
                            break;
                        case Position.orientazione_sinistra:
                            result.add(Position.muovi_ruota_destra);
                            result.add(Position.muovi_ruota_destra);
                            break;
                    }

                    temp_o = posizione_target_r;
                }else if (posizione_target_c.equals(temp_o)){
                    switch (temp_o){
                        case Position.orientazione_destra:
                            temp_c++;
                            break;
                        case Position.orientazione_sinistra:
                            temp_c--;
                            break;
                    }
                    result.add(Position.muovi_avanti);
                }
            }else{
                switch (temp_o){
                    case Position.orientazione_alto:
                        temp_r++;
                        break;
                    case Position.orientazione_basso:
                        temp_r--;
                        break;
                }
                result.add(Position.muovi_avanti);
            }
        }

        return result;
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
                        //Poò uscire dal campo nella cella (-1,0)
                        if(this.riga-1 >= 0 || (this.riga-1==-1 && this.colonna==0)){
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
                        //Poò uscire dal campo nella cella (0,-1)
                        if(this.colonna-1 >= 0 || (this.colonna-1==-1 && this.riga==0)){
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
        if( (nuova_riga >= -1 && nuova_riga < numero_righe)
                && (nuova_colonna >=-1 && nuova_colonna < numero_colonne)
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

    public boolean ostacoliRiga(int c_target, ArrayList<Coordinates> ostacoli){
        boolean result=false;
        Iterator<Coordinates> iter=ostacoli.iterator();
        Coordinates c;
        while(iter.hasNext()) {
            c = iter.next();
            if(c.x==this.riga && c.y!=c_target ){
                if(this.colonna>c_target){
                    if(c.y>c_target && c.y<colonna){
                        result=true;
                    }
                }else if(this.colonna<c_target){
                    if(c.y>colonna && c.y<c_target){
                        result=true;
                    }
                }
            }
        }
        return result;
    }

    public boolean ostacoliColonna(int r_target, ArrayList<Coordinates> ostacoli){
        boolean result=false;
        Iterator<Coordinates> iter=ostacoli.iterator();
        Coordinates c;
        while(iter.hasNext()) {
            c = iter.next();
            if(c.y==this.colonna && c.x!=r_target){
                if(this.riga>r_target){
                    if(c.x>r_target && c.x<riga){
                        result=true;
                    }
                }else if(this.riga<r_target){
                    if(c.x>riga && c.x<r_target){
                        result=true;
                    }
                }
            }
        }
        return result;
    }

    public ArrayList<String> calcola_percorso_ostacoli(int target_r, int target_c, ArrayList<Coordinates> a){
        ArrayList<String> result = new ArrayList<>();
        Coordinates c;
        Iterator<Coordinates> iter;
        int[][] campo = new int[numero_righe][numero_colonne];

        //Calcolo punto d'accesso al bersaglio
        String access_o="";
        int access_r=-1, access_c=-1;
        boolean sopra=true, sotto=true, destra=true, sinistra=true;
        iter = a.iterator();
        //Verico se sono presenti altre mine attorno al bersaglio
        while (iter.hasNext()){
            c=iter.next();
            campo[c.x][c.y]=1;
            if(c.x==target_r && c.y==target_c-1){
                sinistra=false;
            }else if(c.x==target_r && c.y==target_c+1){
                destra=false;
            }else if(c.y==target_c && c.x==target_r+1){
                sopra=false;
            }else if(c.y==target_c && c.x==target_r-1) {
                sotto=false;
            }
        }
        //Verifico se il bersaglio si trova lungo il bordo del campo
        if(target_r==0){
            sotto=false;
        }
        if(target_r==numero_righe-1){
            sopra=false;
        }
        if(target_c==0){
            sinistra=false;
        }
        if(target_c==numero_colonne-1){
            destra=false;
        }

        //preferenza accesso: sinistra > sotto > sopra > destra
        if(sinistra){
            access_r=target_r;
            access_c=target_c-1;
            access_o=orientazione_destra;
        }else if(sotto){
            access_c=target_c;
            access_r=target_r-1;
            access_o=orientazione_alto;
        }else if(sopra){
            access_c=target_c;
            access_r=target_r+1;
            access_o=orientazione_basso;
        }else if(destra){
            access_r=target_r;
            access_c=target_c+1;
            access_o=orientazione_sinistra;
        }

        if (access_r!=-1){

            int numero_vertici=numero_righe*numero_colonne;
            int[][] m_adiacenza=new int[numero_vertici][numero_vertici];

            for(int i=0;i<numero_vertici;i++){
                if(i-numero_colonne>=0){
                    m_adiacenza[i][i-numero_colonne]=1;
                    m_adiacenza[i-numero_colonne][i]=1;
                }
                if(i+numero_colonne<=numero_vertici-1){
                    m_adiacenza[i][i+numero_colonne]=1;
                    m_adiacenza[i+numero_colonne][i]=1;
                }
                if((i+1)%numero_colonne!=0){
                    m_adiacenza[i][i+1]=1;
                    m_adiacenza[i+1][i]=1;
                }
                if((i)%numero_colonne!=0){
                    m_adiacenza[i][i-1]=1;
                    m_adiacenza[i-1][i]=1;
                }
            }

            int parent[] = primMST_alter(m_adiacenza,numero_vertici, a, numero_colonne);
            ArrayList<Coordinates> ac = new ArrayList<>();
            int p=access_r*numero_colonne+access_c;
            while(p!=0){
                ac.add(0,new Coordinates(p/numero_colonne,p%numero_colonne));
                p=parent[p];
            }

            iter = ac.iterator();
            Coordinates next;
            int c_r=0;
            int c_c=0;
            String c_o=orientazione;
            while(iter.hasNext()){
                next = iter.next();
                if(next.x==c_r){
                    if(next.y>c_c){
                        //destra
                        switch (c_o){
                            case orientazione_alto:
                                result.add(muovi_ruota_destra);
                                result.add(muovi_avanti);
                                c_o=orientazione_destra;
                                break;
                            case orientazione_basso:
                                result.add(muovi_ruota_sinistra);
                                result.add(muovi_avanti);
                                c_o=orientazione_destra;
                                break;
                            case orientazione_destra:
                                result.add(muovi_avanti);
                                break;
                            case orientazione_sinistra:
                                result.add(muovi_ruota_destra);
                                result.add(muovi_ruota_destra);
                                c_o=orientazione_destra;
                                break;
                        }
                    }else if(next.y<c_c){
                        //sinistra
                        switch (c_o){
                            case orientazione_alto:
                                result.add(muovi_ruota_sinistra);
                                result.add(muovi_avanti);
                                c_o=orientazione_sinistra;
                                break;
                            case orientazione_basso:
                                result.add(muovi_ruota_destra);
                                result.add(muovi_avanti);
                                c_o=orientazione_sinistra;
                                break;
                            case orientazione_destra:
                                result.add(muovi_ruota_destra);
                                result.add(muovi_ruota_destra);
                                result.add(muovi_avanti);
                                c_o=orientazione_sinistra;
                                break;
                            case orientazione_sinistra:
                                result.add(muovi_avanti);
                                break;
                        }
                    }
                }else if(next.y==c_c){
                    if(next.x>c_r){
                        //sopra
                        switch (c_o){
                            case orientazione_alto:
                                result.add(muovi_avanti);
                                break;
                            case orientazione_basso:
                                result.add(muovi_ruota_destra);
                                result.add(muovi_ruota_destra);
                                result.add(muovi_avanti);
                                c_o=orientazione_alto;
                                break;
                            case orientazione_destra:
                                result.add(muovi_ruota_sinistra);
                                result.add(muovi_avanti);
                                c_o=orientazione_alto;
                                break;
                            case orientazione_sinistra:
                                result.add(muovi_ruota_destra);
                                result.add(muovi_avanti);
                                c_o=orientazione_alto;
                                break;
                        }
                    }else if(next.x<c_r){
                        //sotto
                        switch (c_o){
                            case orientazione_alto:
                                result.add(muovi_ruota_destra);
                                result.add(muovi_ruota_destra);
                                result.add(muovi_avanti);
                                c_o=orientazione_basso;
                                break;
                            case orientazione_basso:
                                result.add(muovi_avanti);
                                break;
                            case orientazione_destra:
                                result.add(muovi_ruota_destra);
                                result.add(muovi_avanti);
                                c_o=orientazione_basso;
                                break;
                            case orientazione_sinistra:
                                result.add(muovi_ruota_sinistra);
                                result.add(muovi_avanti);
                                c_o=orientazione_basso;
                                break;
                        }
                    }
                }
                c_r=next.x;
                c_c=next.y;
            }

            switch (c_o){
                case orientazione_alto:
                    switch (access_o){
                        case orientazione_basso:
                            result.add(muovi_ruota_destra);
                            result.add(muovi_ruota_destra);
                            break;
                        case orientazione_destra:
                            result.add(muovi_ruota_destra);
                            break;
                        case orientazione_sinistra:
                            result.add(muovi_ruota_sinistra);
                            break;
                    }
                    break;
                case orientazione_basso:
                    switch (access_o){
                        case orientazione_alto:
                            result.add(muovi_ruota_destra);
                            result.add(muovi_ruota_destra);
                            break;
                        case orientazione_destra:
                            result.add(muovi_ruota_destra);
                            break;
                        case orientazione_sinistra:
                            result.add(muovi_ruota_sinistra);
                            break;
                    }
                    break;
                case orientazione_destra:
                    switch (access_o){
                        case orientazione_alto:
                            result.add(muovi_ruota_sinistra);
                            break;
                        case orientazione_basso:
                            result.add(muovi_ruota_destra);
                            break;
                        case orientazione_sinistra:
                            result.add(muovi_ruota_destra);
                            result.add(muovi_ruota_destra);
                            break;
                    }
                    break;
                case orientazione_sinistra:
                    switch (access_o){
                        case orientazione_alto:
                            result.add(muovi_ruota_destra);
                            break;
                        case orientazione_basso:
                            result.add(muovi_ruota_sinistra);
                            break;
                        case orientazione_destra:
                            result.add(muovi_ruota_destra);
                            result.add(muovi_ruota_destra);
                            break;
                    }
                    break;
            }

        }else{
            result.add("NON ACCESSIBILE");
        }
        return result;
    }

    static int[] primMST_alter(int graph[][], int V, ArrayList<Coordinates> ostacoli, int numero_colonne)
    {
        int parent[] = new int[V];
        int key[] = new int[V];
        Boolean mstSet[] = new Boolean[V];
        for (int i = 0; i < V; i++) {
            key[i] = Integer.MAX_VALUE;
            mstSet[i] = false;
        }
        key[0] = 0;
        parent[0] = -1;
        for (int count = 0; count < V - 1; count++) {
            int u = minKey(key, mstSet, V);
            mstSet[u] = true;
            boolean ok=true;
            Iterator<Coordinates> iter = ostacoli.iterator();
            Coordinates c;
            while (iter.hasNext()) {
                c = iter.next();
                if(c.x*numero_colonne + c.y == u){
                    ok=false;
                }
            }
            if(ok) {
                for (int v = 0; v < V; v++)
                    if (graph[u][v] != 0 && mstSet[v] == false && graph[u][v] < key[v]) {
                        parent[v] = u;
                        key[v] = graph[u][v];
                    }

            }
        }
        return parent;
    }

    static int minKey(int key[], Boolean mstSet[],int V)
    {
        int min = Integer.MAX_VALUE, min_index = -1;
        for (int v = 0; v < V; v++)
            if (mstSet[v] == false && key[v] < min) {
                min = key[v];
                min_index = v;
            }
        return min_index;
    }


    @Override
    public String toString(){
        return "R:" + this.numero_righe + " C:" + this.numero_colonne + " P:(" + this.riga + "," + this.colonna + ") O:"+this.orientazione;
    }

}
