package com.campersamu.infiniterunner;

import android.util.Log;

import processing.core.*;
import processing.data.*;
import processing.sound.*;

import java.util.ArrayList;

public class infinite_runner_android extends PApplet {

    SaveUtils s;    //Classe che gestisce i salvataggi

    //Sound
    SoundFile music;    //Libreria per l'audio
    float musicVolume;    //Volume

    PImage background;    //Sfondo
    Game g;    //Classe che gestisce il funzionamento del gioco e coordina le classi tra loro


    public void setup() {
        //Dimensione della finestra
        frameRate(60);      //framerate bloccato a 60
        orientation(PORTRAIT);


        s = new SaveUtils();  //Inizializzo SaveUtils
        s.checkSaveFile();    //Check del file di salvataggio iniziale
        s.firstTimeSetup();   //setup del file di salvataggio in caso manca il file/manca qualcosa

        //Sound
        music = new SoundFile(this, "audio/music.wav");

        try {    //Ottenimento del valore del volume salvato, se non esiste, viene creato nel file di savlataggio e assegnato
            musicVolume = s.savetable.getFloat(0, "musicVolume");
        } catch (Exception e) {
            s.savetable.addColumn("musicVolume");
            s.savetable.setFloat(0, "musicVolume", 1);
            musicVolume = s.savetable.getFloat(0, "musicVolume");
        }

        //gestione del music player
        music.play();
        music.loop();
        music.amp(musicVolume);


        g = new Game("mainmenu");    //inizizalizzazione del gioco
        background = loadImage("assets/background.png");  //caricamento sfondo

        background.resize(displayWidth, displayHeight);

//  println("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n");  //pulizia della console
    }

    public void backPressed() {
        g.gameState = "mainmenu";
    }

    public void mousePressed() {    //rilevamento della pressione dei bottoni
        g.b.isMouseClicked();
    }

    public void mouseReleased() {    //rilevamento del rilasciamento dei bottoni
        g.b.isMouseReleased();
    }

    public void keyPressed() {    //rilevamento pressione pulsanti
        if (keyCode == g.c.ctrlLeft) {
            g.c.goLeft = true;
        }
        if (keyCode == g.c.ctrlRight) {
            g.c.goRight = true;
        }
        if (keyCode == g.c.ctrlSprint) {
            g.c.goSprint = true;
        }
        if (g.c.remapping) {
            g.c.getKey = keyCode;
        }
    }

    public void keyReleased() {    //rilevamento rilasciamento pulsanti
        if (keyCode == g.c.ctrlLeft) {
            g.c.goLeft = false;
        }
        if (keyCode == g.c.ctrlRight) {
            g.c.goRight = false;
        }
        if (keyCode == g.c.ctrlSprint) {
            g.c.goSprint = false;
        }
    }

    @Override
    public void onPause() {
        music.pause();
    }

    @Override
    public void onResume() {
        if (music != null) {
            music.play();
            music.loop();
        }
    }

    public void draw() {
        background(0xff123456);
        g.background(background);  //impostazione dello sfondo animato (va solo quando si è in partita)

        try {  //controllo del volume
            musicVolume = s.savetable.getFloat(0, "musicVolume");
        } catch (Exception e) {
            s.savetable.addColumn("musicVolume");
            s.savetable.setFloat(0, "musicVolume", 0);
            musicVolume = s.savetable.getFloat(0, "musicVolume");
        }
//  music.setGain(musicVolume);
        g.sceneHandler();  //Handling delle scene


    }

    boolean mouseClicked;  //variabile globale per bypassare un bug nel rilevamento del click di Processing

    abstract class Button {  //Classe che crea e gestisce i bottoni
        //Attributi
        float x, y;  //Coordinate del bottone
        float w, h;  //dimensione del bottone
        String buttonType;  //tipo di bottone (tipi disponibili: text/label, image, labelimage, tickbox, cosmetics_buybutton, invalid_type (fallback)
        String label;  //etichetta del pulsante
        int baseColor, textColor;  //colore del bottone e del testo
        PImage image;  //immagine da includere nel bottone se necessario
        int strokeColor, oldStrokeColor;    //colore della linea
        boolean highlighted;  //Evidenziato

        PImage tick;  //Con spunta
        boolean enabled;  //Se è abilitato/sta facendo qualcosa

        String buystring = "wtf";  //Stringa/label di un bottone in caso è per un negozio. Se per qualche ragione non viene cambiato quando usato è uguale a "What a Terrible Failure"
        char buystate = 'w'; //Stato del bottone se è per un negozio. Può essere (l)ocked, (u)nlocked o (a)ctive


        Button() {  //costruttore vuoto
            this.x = 0;
            this.y = 0;
            this.w = 0;
            this.h = 0;
            this.buttonType = null;
            this.label = null;
            this.image = null;
            this.baseColor = 255;
            this.textColor = 0;
        }

        /**
         * @param x          X coordinate
         * @param y          Y coordinate
         * @param w          Width
         * @param h          Height
         * @param buttonType Button type, can be text, label, labelImage, image, tickbox, cosmetic_buybutton
         * @param label      Specify the label
         * @param baseColor  Base color of the button
         * @param textColor  Color of the text
         */
        Button(float x, float y, float w, float h, String buttonType, String label, int baseColor, int textColor) {  //costruttore per text/label mode
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
            this.buttonType = buttonType;
            this.label = label;
            this.image = null;
            this.baseColor = baseColor;
            this.textColor = textColor;
        }

        /**
         * @param x          X coordinate
         * @param y          Y coordinate
         * @param w          Width
         * @param h          Height
         * @param label      Specify the label
         * @param image      Specify the image to use
         * @param buttonType Button type, can be text, label, labelImage, image, tickbox, cosmetic_buybutton
         * @param baseColor  Base color of the button
         * @param textColor  Color of the text
         */
        Button(float x, float y, float w, float h, String buttonType, String label, PImage image, int baseColor, int textColor) {  //costruttore per labelimage mode
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
            this.buttonType = buttonType;
            this.label = label;
            this.image = image;
            this.baseColor = baseColor;
            this.textColor = textColor;
        }

        /**
         * @param x          X coordinate
         * @param y          Y coordinate
         * @param w          Width
         * @param h          Height
         * @param buttonType Button type, can be text, label, labelImage, image, tickbox, cosmetic_buybutton
         * @param image      Specify the image to use
         * @param baseColor  Base color of the button
         * @param textColor  Color of the text
         */
        Button(float x, float y, float w, float h, String buttonType, PImage image, int baseColor, int textColor) {  //costruttore per image mode
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
            this.buttonType = buttonType;
            this.label = null;
            this.image = image;
            this.baseColor = baseColor;
            this.textColor = textColor;
        }

        /**
         * @param x          X coordinate
         * @param y          Y coordinate
         * @param w          Width
         * @param h          Height
         * @param buttonType Button type, can be text, label, labelImage, image, tickbox, cosmetic_buybutton
         * @param baseColor  Base color of the button
         * @param textColor  Color of the text
         * @param enabled    Specifies if the button is enabled
         */
        @SuppressWarnings("unused")
        Button(float x, float y, float w, float h, String buttonType, int baseColor, int textColor, boolean enabled) {  //costruttore per tickbox mode
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
            this.buttonType = buttonType;
            this.label = "tickbox";
            tick = loadImage("assets/tick.png");
            this.image = tick;
            this.baseColor = baseColor;
            this.textColor = textColor;
            this.enabled = enabled;
        }

        public void display() {  //display del bottone
            stroke(strokeColor);
            strokeWeight(2);
            switch (buttonType) {
                case "text":
                case "label":   //text/label mode
                    rectMode(CENTER);
                    fill(baseColor);
                    try {    //prova ad applicare i colori relativi allo stato dell'acquisto se destinato ad un negozio
                        if (buystate == 'a') {
                            fill(0xff00AA00);
                        } else if (buystate == 'u') {
                            fill(0xffAA0000);
                        } else if (buystate == 'l') {
                            fill(0xff999999);
                        }
                    } catch (Exception e) {
                        Log.e("InfiniteRunner", e.toString());
                    }
                    rect(x, y, w, h, 5);
                    fill(textColor);
                    textSize(h / 2);
                    textAlign(CENTER, CENTER);
                    text(label, x, y, w, h);
                    break;
                case "labelimage":   //labelimage mode
                    rectMode(CENTER);
                    fill(baseColor);
                    rect(x, y, w, h, 5);
                    imageMode(CENTER);
                    image(image, x, y, w, h);
                    fill(textColor);
                    textSize(h / 2);
                    textAlign(CENTER, CENTER);
                    text(label, x, y, w, h);
                    break;
                case "image":   //image mode
                    try {  //prova a inserire l'immagine, se manca compare una scritta null sul bottone
                        imageMode(CENTER);
                        image(image, x, y, w, h);
                    } catch (Exception e) {
                        rectMode(CENTER);
                        fill(baseColor);
                        rect(x, y, w, h, 5);
                        fill(255);
                        textAlign(CENTER, CENTER);
                        text("null", x, y, w, h);
                    }
                    break;
                case "tickbox":   //tickbox mode
                    rectMode(CENTER);
                    fill(baseColor);
                    rect(x, y, w, h, 5);
                    fill(255);
                    if (enabled) {  //se la tickbox è attiva, compare una spunta sul pulsante
                        imageMode(CENTER);
                        image(tick, x, y, w, h);
                    }
                    break;
                case "cosmetics_buybutton":   //cosmetics_buybutton mode, usato nel negozio cosmetici
                    rectMode(CENTER);
                    fill(baseColor);
                    try {    //prova ad applicare i colori relativi allo stato dell'acquisto se destinato ad un negozio
                        if (buystate == 'a') {
                            fill(0xff00AA00);
                        } else if (buystate == 'u') {
                            fill(0xffAA0000);
                        } else if (buystate == 'l') {
                            fill(0xff999999);
                        }
                    } catch (Exception e) {
                        fill(baseColor);
                    }
                    rect(x, y, w, h, 5);
                    fill(200, 200, 200);
                    rect(x, y, image.width / 1.5f, image.height / 1.5f, 5);
                    imageMode(CENTER);
                    image(image, x, y, w / 2, h / 2);
                    fill(textColor);
                    textSize(w / 4);
                    textAlign(CENTER, CENTER);
                    text(label, x, y + h / 2.5f, w, h);
                    break;
                default:   //Bottone con tipo non valido/non specificato
                    rectMode(CENTER);
                    fill(baseColor);
                    rect(x, y, w, h, 5);
                    fill(255);
                    textAlign(CENTER);
                    text("invalid_type", x, y);
                    break;
            }
            if (isMouseOver() && mouseClicked) {  //se premuto, attiva il contenuto di onClick();
                onClick();
                mouseClicked = false;
            }
        }

        public abstract void onClick();    //classe astratta attivata al click

        public void isMouseClicked() {  //controlla se il mouse viene premuto
            mouseClicked = true;
        }

        public void isMouseReleased() {  //controlla se il pulsante del mouse viene rilasciato
            mouseClicked = false;
        }

        @SuppressWarnings("unused")
        public void tickBox() {    //cambia lo stato della tickbox
            enabled = !enabled;
        }

        @SuppressWarnings("unused")
        public void changeStrokeColor(int strokeColor) {    //cambia il colore del contorno
            this.strokeColor = strokeColor;
        }

        public boolean isMouseOver() {    //controlla se il mouse è sopra il bottone, in caso restituisce vero.
            return mouseX >= x - w / 2 && mouseX <= x + w / 2 && mouseY >= y - h / 2 && mouseY <= y + h / 2;
        }

        public void higlightButton(int c) {  //evidenzia il bottone con un colore dato
            //oldStrokeColor = strokeColor;
            strokeColor = c;
            highlighted = true;
        }

        public void unhighlightButton() {  //rimuove l'evidenziatura al bordo di un bottone
            strokeColor = oldStrokeColor;
            highlighted = false;
        }

        public void switchHighlightButton(int strokeColor) {  //switcha tra evidenziato e non
            if (!highlighted) {
                higlightButton(strokeColor);
                highlighted = true;
            } else {
                unhighlightButton();
                highlighted = false;
            }
        }

        public void updateLabel(String label) {  //aggiorna il label del bottone
            this.label = label;
        }

        public void updateBuystate(char state) throws IllegalArgumentException {    //Aggiorna lo stato di acquisto di un bottone in caso è per un negozio. (AGGIUNTO ESCLUSIVAMENTE PER IL GIOCO)
            if (state == 'l') {  //da acquistare
                buystring = "Compra";
                label = "Compra";
                //label = buystring; bt[j].updateBuystate('l');
                buystate = 'l';

            } else if (state == 'u') {  //acquistato
                buystring = "Usa";
                label = "Usa";
                //label = buystring;
                buystate = 'u';
            } else if (state == 'a') {  //acquistato ed attualmente attivo
                buystring = "Attiva";
                label = "Attiva";
                //label = buystring;
                buystate = 'a';
            } else {  //se viene passato un valore non valido, rilascia un errore.
                throw new IllegalArgumentException("Unknown buystate " + state + ".");
            }
        }

        public void updateLabelAfterBuystring() {  //aggiorna il label del bottone con quello per il negozio. (INUTILIZZATO)
            label = buystring;
        }

    }

    class Controls {  //Classe che gestisce i controlli dell'utente

        //attributi
        int ctrlLeft, ctrlRight, ctrlSprint;  //variabili dove vengono salvati i keyCode dei comandi
        boolean mouseControls;  //variabile per capire se l'utente usa i controlli tramite mouse


        boolean goLeft = false, goRight = false, goSprint = false;  //variabile per ottenere l'imput dell'utente, aggiornate in keyPressed() e keyReleased(). Serve per aggirare un bug nell'input di Processing

        float x, y;  //coordinate dell'oggetto comandato
        float accelleration;  //accellerazione dell'oggetto comandato
        float sprintaccelleration;  //accellerazione veloce (sprint) dell'oggetto comandato
        int getKey, oldKey;  //variabili usate dal remapper per sapere se il tasto per un'azione cambia
        boolean remapping;  //variabile che segnala se si sta attualmente remappando i comandi


        Controls(float accelleration, float sprintaccelleration) {  //costruttore parametrico
            this.accelleration = accelleration;
            this.sprintaccelleration = sprintaccelleration;
            remapping = false;
            getKey = -1;
            oldKey = getKey;

            //ottenimento dei controlli salvati nel file, se non sono presenti, vengono reimpostati sul momento ai valori default
            try {
                ctrlLeft = s.savetable.getInt(0, "ctrlLeft");
            } catch (Exception e) {
                s.savetable.addColumn("ctrlLeft");
                s.savetable.setInt(0, "ctrlLeft", 37);
                saveTable(s.savetable, s.savelocation);
                ctrlLeft = s.savetable.getInt(0, "ctrlLeft");
            }
            try {
                ctrlRight = s.savetable.getInt(0, "ctrlRight");
            } catch (Exception e) {
                s.savetable.addColumn("ctrlRight");
                s.savetable.setInt(0, "ctrlRight", 39);
                saveTable(s.savetable, s.savelocation);
                ctrlRight = s.savetable.getInt(0, "ctrlRight");
            }
            try {
                ctrlSprint = s.savetable.getInt(0, "ctrlSprint");
            } catch (Exception e) {
                s.savetable.addColumn("ctrlSprint");
                s.savetable.setInt(0, "ctrlSprint", 16);
                saveTable(s.savetable, s.savelocation);
                ctrlSprint = s.savetable.getInt(0, "ctrlSprint");
            }
            try {
                mouseControls = s.convertIntToBoolean(s.savetable.getInt(0, "mouseControls"));
            } catch (Exception e) {
                s.savetable.addColumn("mouseControls");
                s.savetable.setInt(0, "mouseControls", 1);
                saveTable(s.savetable, s.savelocation);
                mouseControls = s.convertIntToBoolean(s.savetable.getInt(0, "mouseControls"));
            }
        }

        public void attachToObject(float objX, float objY) {  //Aggiornamento delle coordinate con quelle di un oggetto/coordinate date
            x = objX;
            y = objY;
        }

        public void keyboardControls() {  //Comandi da tastiera

            if (goLeft) {
                if (x >= 0) {
                    if (goSprint) {
                        x -= sprintaccelleration;
                    } else {
                        x -= accelleration;
                    }
                }
            }
            if (goRight) {
                if (x <= width) {
                    if (goSprint) {
                        x += sprintaccelleration;
                    } else {
                        x += accelleration;
                    }
                }
            }
        }

        @SuppressWarnings("unused")
        public void changeDefaultAccelleration(float accelleration, float sprintaccelleration) {  //TODO cambiamento dell'accellerazione del personaggio (non ho fatto in tempo ad implementarlo nelle impostazioni)
            this.accelleration = accelleration;
            this.sprintaccelleration = sprintaccelleration;
        }

        public void remap(String control) {  //remapper dei comandi


            if (!remapping) {
                if (control.equals("ctrlLeft")) {
                    getKey = ctrlLeft;
                    getKey = oldKey;
                }
                if (control.equals("ctrlRight")) {
                    getKey = ctrlRight;
                    getKey = oldKey;
                }
                if (control.equals("ctrlSprint")) {
                    getKey = ctrlSprint;
                    getKey = oldKey;
                }

                remapping = true;
            }

            if (getKey != oldKey) {
                if (control.equals("ctrlLeft")) {
                    ctrlLeft = getKey;
                    remapping = false;
                }
                if (control.equals("ctrlRight")) {
                    ctrlRight = getKey;
                    remapping = false;
                }
                if (control.equals("ctrlSprint")) {
                    ctrlSprint = getKey;
                    remapping = false;
                }
                try {  //tenta di salvare i controlli
                    s.saveControls();
                } catch (Exception ignored) {
                }
            }
        }

        public String fixSymbols(int og) {  //Converte alcuni simboli nei simboli effettivi (basato su tastiera QWERTY)
            String c = "" + (char) og;

            switch (c) {
                case "" + '%':
                    c = "←";
                    break;
                case "" + (char) 39:
                    c = "→";
                    break;
                case "(":
                    c = "↓";
                    break;
                case "&":
                    c = "↑";
                    break;
                case "" + (char) 16:
                    c = "LSHIFT";
                    break;
                case "" + (char) 17:
                    c = "LCONTROL";
                    break;
            }
            return c;
        }

        public void mouseControls() {  //Controlli con il mouse
            x = mouseX;
            stroke(0);
            strokeWeight(3);
            if (touchIsStarted)
                line(x, y, mouseX, mouseY);
        }

        @SuppressWarnings("unused")
        public void switchMouseControls() {  //cambia e salva l'opzione dei controlli tramite mouse
            mouseControls = !mouseControls;
            s.savetable.setInt(0, "mouseControls", s.convertBooleanToInt(mouseControls));
        }
    }

    class Enemy {  //Classe che gestisce i nemici e le taniche di benzina
        float x, y;  //posizione
        float w, h;  //dimensione
        float scx;   //coordinata x salvata per lo spostamento del nemico

        boolean busy;  //vero se impegnato in un'azione (es. spostamento sull'asse x)
        @SuppressWarnings("unused")
        boolean obstructed;  //vero se lo spostamento è impedito da un altro nemico
        char movingto;  //segna verso si sta muovendo il nemico. Può essere (S)inistra, (D)estra, (N)/A

        float accelleration;  //accellerazione
        float maccelleration = 16;  //accellerazioen massima

        PImage enemyt;  //texture nemici

        PImage jerrycan;  //texture tanica di gas
        boolean isJerrycan;  //Vero se il nemico in verità è una tanica
        boolean remove = false;  //Vero se il nemico/tanica va rimosso/a

        Enemy(float x, float y, float w, float h, float accelleration) {  //costruttore parametrico
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
            this.accelleration = accelleration;
            scx = -1;
            enemyt = loadImage("assets/car_enemy.png");
            isJerrycan = false;
        }

        @SuppressWarnings("unused")
        Enemy(float x, float y, float w, float h) {  //costruttore parametrico (nemico default)
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
            accelleration = 1;
            scx = -1;
            enemyt = loadImage("assets/car_enemy.png");
            isJerrycan = false;
        }

        Enemy(float x, float y, float w, float h, float accelleration, boolean isJerrycan) {  //costruttore parametrico (tanica di gas)
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
            this.accelleration = accelleration;
            jerrycan = loadImage("assets/jerrycan.png");
            this.isJerrycan = isJerrycan;
        }

        @SuppressWarnings("unused")
        public void updatePos(float x, float y) {  //aggiorna la posizione del nemico/tanica
            this.x = x;
            this.y = y;
        }

        public void move() {  //sposta il/la nemico/tanica sulla coordinata y
            if (accelleration >= maccelleration) {
                accelleration = maccelleration;
            }
            y += accelleration;
        }

        public void saveCoordinateX(float scx) {  //salva la coordinata x di dove deve andare il nemico
            this.scx = scx;
        }

        public void moveToX() {  //sposta il nemico sulla coordinata x
            if (scx == -1) {  //se scx è -1 non si sposta
                //nothing
                busy = false;
            } else if (x < scx) {  //sposta verso destra
                busy = true;
                x++;
            } else if (x > scx) {  //sposta verso sinistra
                busy = true;
                x--;
            } else {  //non fa nulla
                busy = false;
                //nothing
            }
        }

        public void display() {  //mostra il/la nemico/tanica di gas sullo schermo
            imageMode(CENTER);
            if (!isJerrycan) {  //se non è una tanica di gas
                image(enemyt, x, y, w, h);
            } else {  //se è un nemico
                image(jerrycan, x, y, w, h);
            }
        }

        @SuppressWarnings("unused")
        public void changeAccelleration(float accelleration) {  //modifica l'accellerazione
            this.accelleration = accelleration;
        }

        public boolean isDead() {  //se è fuori dallo schermo, restituisce vero
            return y > height || x > width + w || x < 0 - w;
        }
    }

    class Game {  //Classe che coordina e gestisce tutto il gioco. FATTA APPOSTA SOLO PER QUESTO GIOCO, NON RIUTILIZZABILE.

        //Attributi
        boolean setupGame;  //variabile booleana che serve per reimpostare il gioco quando si entra in gioco.
        float scrollingbg = 0;  //variabile usata per controllare lo scrolling dello sfondo
        float bgspeed = 3;  //velocità dello sfondo
        String gameState;  //stato del gioco.
        float defaultenemyaccelleration = 3;  //accellerazione default dei nemici
        float enemyintaccelleration;  //accellerazione incrementale dei nemici, si addiziona a default enemy accelleration.
        int score;  //variabile che conta il punteggio (+1 per frame)
        PImage menuIcon, backIcon;  //icone per i pulsanti menu e indietro
        HScrollbar msb;  //Scrollbar per il volume
        @SuppressWarnings("unused")
        int hiscore, money; //TODO sistema la merda che prende direttamente dal savefile a cazzo di cane le cose ogni frame idiota.

        ArrayList<Enemy> enemies = new ArrayList<>();  //Arraylist che contiene i nemici

        Button b;  //bottone generico riutilizzato
        Button[] bs = new Button[3];    //Bottoni extra per remapping e opzioni

        @SuppressWarnings("unused")
        boolean areCheatsActive = false;  //variabile che controlla se i trucchi sono attivi
        boolean maxspeed, nofuel, godmode;  //variabili specifiche per i truchci

        Controls c;  //classe controlli
        Player p;  //classe giocatore
        ShopManager sm;  //classe negozio

        Game(String gameState) {  //costruttore default
            this.gameState = gameState;  //impostazione stato del gioco iniziale
            score = 0;  //punteggio default
            b = new Button() {    //bottone per uso generale, viene riutilizzato
                public @Override
                void onClick() {
                }
            };
            for (int i = 0; i < bs.length; i++) {  //bottoni per le opzioni
                bs[i] = new Button() {
                    public @Override
                    void onClick() {
                    }
                };
            }
            c = new Controls(3.5f, 9.5f);  //inizializzazione controlli
            p = new Player(width / 2f, height - height / 4f, width / 10f, height / 9.5f, 800);  //inizializzazione giocatore
            bs[0] = new Button(width / 1.5f, height / 2f - height / 20f, 50 + width / 20f, 50 + width / 20f, "text", "" + c.fixSymbols(c.ctrlLeft), 0xffff0000, 0xffffffff) {  //bottone remapping tasto sinistra
                public @Override
                void onClick() {  //al click
                    c.remapping = false;  //resetta remapping
                    switchHighlightButton(0xffFFf700);  //imposta l'evidenziamento
                    bs[1].unhighlightButton();  //rimuove l'evidenziamento agli altri pulsanti
                    bs[2].unhighlightButton();
                    if (c.remapping) {    //variabile che segna se stiamo remappando i pulsanti
                        c.remapping = false;
                    }

                }
            };
            bs[1] = new Button(width / 1.5f, height / 2f, 50 + width / 20f, 50 + width / 20f, "text", "" + c.fixSymbols(c.ctrlRight), 0xffff0000, 0xffffffff) {  //bottone remapping tasto destra  //tutti i pulsanti di remapping sono identici, non commento.
                public @Override
                void onClick() {
                    c.remapping = false;
                    switchHighlightButton(0xffFFf700);
                    bs[0].unhighlightButton();
                    bs[2].unhighlightButton();
                    if (c.remapping) {
                        c.remapping = false;
                    }
                }
            };
            bs[2] = new Button(width / 1.5f, height / 2f + height / 20f, 50 + width / 20f, 50 + width / 20f, "text", "" + c.fixSymbols(c.ctrlSprint), 0xffff0000, 0xffffffff) {  //bottone remapping tasto sprint
                public @Override
                void onClick() {
                    c.remapping = false;
                    switchHighlightButton(0xffFFf700);
                    bs[0].unhighlightButton();
                    bs[1].unhighlightButton();
                    if (c.remapping) {
                        c.remapping = false;
                    }
                }
            };
//    bs[3] = new Button(width/1.5f, height/2+height/20+60, 50, 50, "tickbox", 0xffff0000, 0xffffffff, c.mouseControls) {      //bottone tickbox controlli con il mouse
//      public @Override void onClick() {
//        if (c.mouseControls) {  //switch dell'impostazione, disabilita/abilita i controlli con il mouse e la tickbox
//          enabled = false;
//        } else {
//          enabled = true;
//        }
//        c.switchMouseControls();
//      }
//    };

            sm = new ShopManager();  //inizializzo il negozio

            //assegno le texture dei pulsanti del menu e del tasto indietro alle variabili a cui appartengono
            menuIcon = loadImage("assets/menuIcon.png");
            backIcon = loadImage("assets/backIcon.png");

            //setup della Scrollbar (La scrollbar è stata presa direttamente dal sito di Processing)
            //sincronizzo la scrollbar con il volume
            msb = new HScrollbar(width / 2f - (250 + width / 5f) / 2f, height / 2f - height / 100f, 250 + width / 5, 16 + height / 135, 2);
            msb.setPos(musicVolume, 0, 1);

            msb.newspos = map(musicVolume, 0, 1, msb.sposMin * msb.ratio, msb.sposMax * msb.ratio) / msb.ratio;
            msb.spos = map(musicVolume, 0, 1, msb.sposMin * msb.ratio, msb.sposMax * msb.ratio) / msb.ratio;
        }


        public void background(PImage background) {  //funzione che gestisce lo sfondo e lo scrolling
            imageMode(CORNER);  //setup delle 2 immagini
            image(background, 0, -background.height + scrollingbg);
            image(background, 0, scrollingbg);

            if (gameState.equals("ingame")) {  //quando siamo in gioco
                if (scrollingbg + bgspeed >= height) {  //se lo sfondo esce dallo schermo al prossimo frame
                    scrollingbg = height;  //imposta la posizione dello sfondo ad height
                }
                if (scrollingbg >= height) {  //se lo sfondo è arrivato alla fine, rimettilo in cima
                    scrollingbg = 0;
                } else {  //sposta lo sfondo
                    scrollingbg += bgspeed;
                }
            }
        }

        public void displayBackgroundEnemies() {
            nofuel = true;
            score = -1;
            enemyintaccelleration = 10;
            randomizeEnemySpawn(69);
            displayEnemies();
            nofuel = false;
        }

        public void randomizeEnemySpawn(int chance) {  //randomizzazione e gestione movimento/generazione dei nemici
            if ((int) random(chance) == 1) {  //generazione nemici normali
                enemies.add(new Enemy(random(0, width), 0, width / 10f, height / 9.5f, defaultenemyaccelleration + enemyintaccelleration));
            }
            if (score % 1000 == 0) {  //generazione di nemici veloci ogni 1000 punti, numero randomico incrementale per ogni 1000 punti
                enemyintaccelleration += random(random(1, 3));
                for (int i = (int) random(1, score / 1000f + 1); i > 0; i--) {
                    enemies.add(new Enemy(random(0, width), 0, width / 10f, height / 9.5f, defaultenemyaccelleration * 2 + enemyintaccelleration));
                }
            }
            if (!nofuel) {  //disattiva il carburante in caso il cheat carburante infinito è attivo
                if ((int) random(0, p.maxfuel / 2f + 1) - (score / 1000 * 3) == 1 || p.fuel == p.maxfuel / 2 || p.fuel == p.maxfuel / 3 || p.fuel == p.maxfuel / 1.5) {  //generazione carburante
                    enemies.add(new Enemy(random(0, width), 0, 100, 100, defaultenemyaccelleration + enemyintaccelleration, true));
                }
            }
            if (enemies != null) {  //gestione spostamento nemici, provano a schivarsi se sanno che nel giro di qualche frame potrebbero schiantarsi, in caso si spostano a sinistra a destra in base alla loro posizione e/o alla posizione delle altre macchine per evitare le altre macchine. Non funziona benissimo ma funziona, il mio cervello in depravazione da sonno/stanchezza dopo ore di programmaizone filata non trovava soluzioni migliori.
                for (int i = enemies.size() - 1; i > 0; i--) {
                    for (int j = 0; j < enemies.size(); j++) {

                        if (i != j) {
                            if (!enemies.get(j).isJerrycan) {  //non fa spostare le taniche di benzina
                                try {
                                    if (enemies.get(i).y < enemies.get(j).y) {
                                        if (collisionDetected(enemies.get(i).x, enemies.get(i).y + (enemies.get(i).accelleration * 4), enemies.get(i).w, enemies.get(i).h, enemies.get(j).x, enemies.get(j).y, enemies.get(j).w, enemies.get(j).h) && !enemies.get(i).busy) {
                                            if (enemies.get(i).x == enemies.get(i).scx) {
                                                enemies.get(i).movingto = 'n';
                                            } else if (enemies.get(i).x < enemies.get(i).scx || collisionDetected(enemies.get(i).x + enemies.get(i).w, enemies.get(i).y + (enemies.get(i).accelleration * 4), enemies.get(i).w, enemies.get(i).h, enemies.get(j).x, enemies.get(j).y, enemies.get(j).w, enemies.get(j).h) || enemies.get(i).movingto == 'd') {
                                                enemies.get(i).saveCoordinateX(enemies.get(i).x + (enemies.get(i).w * 2));
                                                enemies.get(i).movingto = 'd';
                                            } else if (enemies.get(i).x > enemies.get(i).scx || collisionDetected(enemies.get(i).x - enemies.get(i).w, enemies.get(i).y + (enemies.get(i).accelleration * 4), enemies.get(i).w, enemies.get(i).h, enemies.get(j).x, enemies.get(j).y, enemies.get(j).w, enemies.get(j).h) || enemies.get(i).movingto == 's') {
                                                enemies.get(i).saveCoordinateX(enemies.get(i).x - (enemies.get(i).w * 2));
                                                enemies.get(i).movingto = 's';
                                            }
                                        }
                                    }
                                    if (collisionDetected(enemies.get(i).x, enemies.get(i).y, enemies.get(i).w, enemies.get(i).h, enemies.get(j).x, enemies.get(j).y, enemies.get(j).w, enemies.get(j).h)) {
                                        enemies.remove(i);
                                        enemies.remove(j);
                                    }
                                } catch (Exception ignored) {
                                }
                            }
                        }

                    }
                    try {
                        if (enemies.get(i).isDead() || enemies.get(i).remove) {  //se i/le nemici/taniche sono fuori dallo schermo o vanno distrutte, rimuovile dall'arraylist e distruggile.
                            enemies.remove(i);
                        }
                    } catch (Exception ignored) {
                    }
                }
            }
        }


        public void modifyGameState(String gameState) {  //Funzione che modifica lo stato del gioco
            this.gameState = gameState;
        }

        public void displayEnemies() {  //funzione che mostra e sposta i nemici
            for (Enemy e : enemies) {
                e.display();
                e.move();

                if (!e.isJerrycan) {  //blocca il movimento delle taniche di benzina
                    e.moveToX();
                }
            }
        }

        public void inGameRoutine() {  //Funzione che gestisce la routine base del gioco (quando ingame) (all'inizio doveva servire a molta più roba ma alla fine aggiorna solo il punteggio)
            score++;
        }

        public boolean areCheatsEnabled() {  //funzione che controlla se i trucchi sono abilitati, in caso restituisce vero
            return godmode || maxspeed || nofuel;
        }

        public void sceneHandler() {  //Gestione delle scene
            switch (gameState) {
                case "ingame":   //se in gioco
                    if (setupGame) {  //imposta il gioco
                        p = new Player(width / 2f, height - height / 4f, width / 10f, height / 9.5f, 800);
                        score = 0;
                        enemies.clear();
                        enemyintaccelleration = 2;
                        setupGame = false;

                        maxspeed = false;
                        godmode = false;
                        nofuel = false;
                        for (int i = 0; i < s.savetable.getRowCount(); i++) {  //controllo trucchi
                            if (s.savetable.getString(i, "activecheats").equals("maxspeed")) {
                                enemyintaccelleration = 100;
                                maxspeed = true;
                            }
                            if (s.savetable.getString(i, "activecheats").equals("godmode")) {
                                godmode = true;
                            }
                            if (s.savetable.getString(i, "activecheats").equals("nofuel")) {
                                println(true);
                                nofuel = true;
                            }
                        }
                    }

                    //controlli giocatore
                    c.attachToObject(p.x, p.y);
//      if (!c.mouseControls) {  //controlla se i comandi con il mouse sono attivi, in caso li usa al posto della tastiera.
                    c.keyboardControls();
                    c.mouseControls();
                    p.updatePos(c.x, c.y);  //aggiorna la posizione


                    randomizeEnemySpawn(150 - (score / 1000) * 5);  //randomizzazione generazione nemici/carburante

                    //display nemici, giocatore e carburante
                    displayEnemies();
                    p.display();
                    if (!nofuel) {  //blocco carburante in caso dell'attivazione del trucco carburante infinito
                        p.updateFuel(1, 'd');
                    }

                    inGameRoutine();  //routine base del gioco


                    if (!nofuel) {  //blocco carburante in caso dell'attivazione del trucco carburante infinito
                        p.fuelBar(width - width / 20f, height / 7f, width / 50f * width / 520f, height / 10f * height / 1500f);
                    }
                    rectMode(CENTER);
                    fill(0xffFF0000);
                    rect(width / 2f, 20, 180 * width / 520f, 50 * height / 520f, 5);
                    textAlign(CENTER);
                    textSize(30 * width / 520f);
                    fill(0xffFFFF00);
                    text(score, width / 2f, 35 * height / 1150f);
                    textAlign(RIGHT);

                    if (!nofuel) { //blocco carburante in caso dell'attivazione del trucco carburante infinito
                        if (p.fuel <= 0) {
                            if (!godmode) {    //blocco morte in caso il cheat invincibilità è attivo
                                modifyGameState("gameover");
                            }
                        }
                    }

                    for (Enemy e : enemies) {
                        if (collisionDetected(e.x, e.y, e.w, e.h, p.x, p.y, p.w, p.h)) {
                            if (!e.isJerrycan) {
                                if (!areCheatsEnabled()) {  //controllo se i trucchi sono attivi, in caso non dare soldi
                                    s.savetable.setInt(0, "money", s.savetable.getInt(0, "money") + score / 500);
                                    saveTable(s.savetable, s.savelocation);
                                }
                                if (!godmode) {    //blocco morte in caso il cheat invincibilità è attivo
                                    modifyGameState("gameover");
                                }
                            } else {
                                if (!nofuel) {    //blocco carburante in caso dell'attivazione del trucco carburante infinito
                                    p.updateFuel((int) random(p.maxfuel / 3f, p.maxfuel), 'a');
                                    score += 50;
                                    e.remove = true;
                                }
                            }
                        }
                    }

                    b = new Button(width / 14f, height / 30f, 60 + width / 20f, 60 + width / 20f, "image", menuIcon, 0xffff0000, 0xffffffff) {
                        @Override
                        public void onClick() {
                            gameState = "mainmenu";
                            setupGame = true;
                        }
                    };
                    b.display();
                    break;
                case "gameover":   //Stato gameover
                    displayEnemies();
                    textAlign(CENTER);
                    textSize(50 * width / 700f);
                    fill(0xffFF0000);
                    text("Hai Perso!\n" + "Score: " + score, width / 2f, height / 3.5f);
                    if (s.saveHiscore(score)) {  //salva il record e se vero stampa "(RECORD!)" sullo schermo
                        text("(RECORD!)", width / 2f, height / 3f + 100);
                        s.reloadTable();
                    }
                    b = new Button(width / 2f, height / 2f, 230 * width / 1000f, 100 * height / 1500f, "text", "Gioca", 0xffff0000, 0xffffffff) {
                        @Override
                        public void onClick() {
                            gameState = "ingame";
                            setupGame = true;
                        }
                    };
                    b.display();

                    b = new Button(width / 2f, height / 2f + 150 + height / 40f, 230 * width / 1000f, 100 * height / 1500f, "text", "Menu", 0xffff0000, 0xffffffff) {
                        @Override
                        public void onClick() {
                            gameState = "mainmenu";
                        }
                    };
                    b.display();
                    break;
                case "mainmenu":   //Stato menù principale
                    displayBackgroundEnemies();
                    textAlign(CENTER);
                    textSize(80 * width / 1000f);
                    fill(0xffFF0000);
                    text("INFINITE RUNNER", width / 2f, height / 3.5f);
                    rectMode(CENTER);
                    fill(0xffFF0000);
                    rect(width / 2f, 20, 180 * width / 520f, 50 * height / 520f, 5); //rect record

                    rect(width - 45, 30, 200 * width / 520f, 50 * height / 1400f, 5); //rect soldi

                    fill(0xffFFFF00);
                    textSize(30 * width / 690f); //TODO NON. CARICARE. SOLDI E PUNTEGGIO. DAL. FOTTUTO. SAVEFILE. OGNI. MINCHIA. DI. FRAME. RITARDATO.

                    text("Record:\n" + s.savetable.getInt(0, "hiscore"), width / 2f, 35 * height / 1420f);  //display record

                    textAlign(CORNER, CORNER);
                    text("$" + s.savetable.getInt(0, "money"), width - width / 11f, 35 * height / 1420f);  //display soldi


                    b = new Button(width / 2f, height / 2f, 230 * width / 1000f, 100 * height / 1800f, "text", "Gioca", 0xffff0000, 0xffffffff) {  //pulsante gioco
                        @Override
                        public void onClick() {
                            gameState = "ingame";
                            setupGame = true;
                        }
                    };
                    b.display();
                    b = new Button(width / 2f, height / 2f + 150 * height / 1800f, 230 * width / 1000f, 100 * height / 1800f, "text", "Opzioni", 0xffFFAA00, 0xffffffff) {  //pulsante opzioni
                        @Override
                        public void onClick() {
                            gameState = "options";
                        }
                    };
                    b.display();
                    b = new Button(width / 2f, height / 2f + 300 * height / 1800f, 230 * width / 1000f, 100 * height / 1800f, "text", "Negozio", 0xff00FF00, 0xffffffff) {  //pulsante negozio
                        @Override
                        public void onClick() {
                            gameState = "shop";
                        }
                    };
                    b.display();
                    break;
                case "options":   //stato opzioni
                    textAlign(CENTER);  //TODO: OPZIONI AGGIORNATE

                    textSize(80 * width / 1000f);
                    fill(0xffFF0000);
                    text("OPZIONI", width / 2f, height / 3.5f);
                    b = new Button(width / 2f, height / 2f + 150 * height / 1800f, 230 * width / 900f, 100 * height / 1700f, "text", "Comandi", 0xffff0000, 0xffffffff) {  //pulsante comandi
                        @Override
                        public void onClick() {
                            gameState = "controls";
                        }
                    };
                    b.display();
                    b = new Button(width / 14f, height / 30f, 60 + width / 20f, 60 + width / 20f, "image", backIcon, 0xffff0000, 0xffffffff) {  //indietro
                        @Override
                        public void onClick() {
                            gameState = "mainmenu";
                        }
                    };
                    b.display();

                    textSize(height / 30f);
                    fill(0);
                    text("Musica", width / 2f, height / 2f - height / 24f);  //scrollbar impostazione volume

                    msb.update();
                    msb.display();

                    s.savetable.setFloat(0, "musicVolume", map(msb.getPos(), msb.sposMin * msb.ratio, msb.sposMax * msb.ratio, 0, 1));
                    musicVolume = map(msb.getPos(), msb.sposMin * msb.ratio, msb.sposMax * msb.ratio, 0, 1);
                    music.amp(musicVolume);
                    s.saveAudio();
                    break;
                case "controls":   //stato controlli
                    displayBackgroundEnemies();
                    textAlign(CENTER);
                    textSize(80 * width / 1000f);
                    fill(0xffFF0000);
                    text("COMANDI", width / 2f, height / 3.5f);
                    textAlign(LEFT);
                    textSize(height / 30f);
                    fill(0);
                    text("Sinistra: ", width / 2.5f - width / 10f, height / 2f - height / 27f);

                    //remapping dei controlli
                    if (bs[0].highlighted) {
                        c.remap("ctrlLeft");
                        bs[0].updateLabel("" + c.fixSymbols(c.ctrlLeft));
                    }
                    textAlign(LEFT);
                    textSize(height / 30f);
                    fill(0);
                    text("Destra: ", width / 2.5f - width / 10f, height / 2f + height / 80f);
                    if (bs[1].highlighted) {
                        c.remap("ctrlRight");
                        bs[1].updateLabel("" + c.fixSymbols(c.ctrlRight));
                    }
                    textAlign(LEFT);
                    textSize(height / 30f);
                    fill(0);
                    text("Sprint: ", width / 2.5f - width / 10f, height / 2f + height / 17f);
                    if (bs[2].highlighted) {
                        c.remap("ctrlSprint");
                        bs[2].updateLabel("" + c.fixSymbols(c.ctrlSprint));
                    }

//      text("Mouse: ", width/2.5f, height/2+height/17+height/30+20);

                    bs[0].display();
                    bs[1].display();
                    bs[2].display();
//      bs[3].display();  //pulsante tickbox controlli mouse

                    b = new Button(width / 14f, height / 30f, 60 + width / 20f, 60 + width / 20f, "image", backIcon, 0xffff0000, 0xffffffff) {
                        @Override
                        public void onClick() {
                            gameState = "options";
                            bs[0].unhighlightButton();
                            bs[1].unhighlightButton();
                            bs[2].unhighlightButton();
                            c.remapping = false;
                        }
                    };
                    b.display();
                    break;
                case "shop":   //stato negozio
                    displayBackgroundEnemies();
                    textAlign(CENTER);
                    textSize(80 * width / 1000f);
                    fill(0xffFF0000);
                    text("NEGOZIO", width / 2f, height / 3.5f);
                    rectMode(CENTER);
                    fill(0xffFF0000);
                    rect(width - 45, 30, 200 * width / 520f, 50 * height / 1400f, 5);
                    fill(0xffFFFF00);
                    textSize(30 * width / 690f);    //TODO: qui devi risolvere lo schifo stupido del load data from savefile every frame

                    textAlign(CORNER, CORNER);
                    text("$" + s.savetable.getInt(0, "money"), width - width / 11f, 35 * height / 1420f);
                    b = new Button(width / 2f, height / 2f, 230 * width / 700f, 100 * height / 1600f, "text", "Macchine", 0xffff0000, 0xffffffff) {  //pulsante Cosmetici
                        @Override
                        public void onClick() {
                            gameState = "cosmetics";
                        }
                    };
                    b.display();
                    b = new Button(width / 2f, height / 2f + 150 * height / 1800f, 230 * width / 700f, 100 * height / 1600f, "text", "Trucchi", 0xffff0000, 0xffffffff) {  //cosmetici Trucchi
                        @Override
                        public void onClick() {
                            gameState = "cheats";
                        }
                    };
                    b.display();
                    b = new Button(width / 14f, height / 30f, 60 + width / 20f, 60 + width / 20f, "image", backIcon, 0xffff0000, 0xffffffff) {
                        @Override
                        public void onClick() {
                            gameState = "mainmenu";
                        }
                    };
                    b.display();
                    break;
                case "cosmetics":   //Stato cosmetici
                    displayBackgroundEnemies();
                    textAlign(CENTER);
                    textSize(80 * width / 1000f);
                    fill(0xffFF0000);
                    text("COSMETICI ($100)", width / 2f, height / 3.5f);
                    rectMode(CENTER);
                    fill(0xffFF0000);
                    rect(width - 45, 30, 200 * width / 520f, 50 * height / 1400f, 5);
                    fill(0xffFFFF00);
                    textSize(30 * width / 690f);
                    textAlign(CORNER, CORNER);  //TODO fixa dio perchè leggi roba ogni frame

                    text("$" + s.savetable.getInt(0, "money"), width - width / 11f, 35 * height / 1420f);
                    b = new Button(width / 14f, height / 30f, 60 + width / 20f, 60 + width / 20f, "image", backIcon, 0xffff0000, 0xffffffff) {
                        @Override
                        public void onClick() {
                            gameState = "shop";
                        }
                    };
                    b.display();
                    sm.displayCosmeticsButtons();
                    break;
                case "cheats":   //Stato trucchi
                    displayBackgroundEnemies();
                    textAlign(CENTER);
                    textSize(80 * width / 1000f);
                    fill(0xffFF0000);
                    text("TRUCCHI($500)", width / 2f, height / 3.5f);
                    rectMode(CENTER);
                    fill(0xffFF0000);
                    rect(width - 45, 30, 200 * width / 520f, 50 * height / 1400f, 5);
                    fill(0xffFFFF00);
                    textSize(30 * width / 690f);
                    textAlign(CORNER, CORNER);
                    text("$" + s.savetable.getInt(0, "money"), width - width / 11f, 35 * height / 1420f);    //TODO savefile aborto da fixare

                    b = new Button(width / 14f, height / 30f, 60 + width / 20f, 60 + width / 20f, "image", backIcon, 0xffff0000, 0xffffffff) {
                        @Override
                        public void onClick() {
                            gameState = "shop";
                        }
                    };
                    b.display();
                    textAlign(LEFT);
                    textSize(height / 30f);
                    fill(0);
                    text("Invincibilità:", 100, height / 2f - 43 - height / 30f);
                    text("Benzina infinita:", 100, height / 2f + 3);
                    text("Velocità massima:", 100, height / 2f + 50 + height / 30f);
                    sm.displayCheatsButtons();
                    break;
            }
        }
    }

    class HScrollbar { //Scrollbar preda dal sito di Processing (" https://processing.org/examples/scrollbar.html ")
        int swidth, sheight;    // width and height of bar
        float xpos, ypos;       // x and y position of bar
        float spos, newspos;    // x position of slider
        float sposMin, sposMax; // max and min values of slider
        int loose;              // how loose/heavy
        boolean over;           // is the mouse over the slider?
        boolean locked;
        float ratio;

        HScrollbar(float xp, float yp, int sw, int sh, int l) {
            swidth = sw;
            sheight = sh;
            int widthtoheight = sw - sh;
            ratio = (float) sw / (float) widthtoheight;
            xpos = xp;
            ypos = yp - sheight / 2f;
            newspos = spos;
            sposMin = xpos;
            sposMax = xpos + swidth - sheight;
            loose = l;
        }

        public void update() {
            over = overEvent();
            if (mousePressed && over) {
                locked = true;
            }
            if (!mousePressed) {
                locked = false;
            }
            if (locked) {
                newspos = constrain(mouseX - sheight / 2f, sposMin, sposMax);
            }
            if (abs(newspos - spos) > 1) {
                spos = spos + (newspos - spos) / loose;
            }
        }

        public float constrain(float val, float minv, float maxv) {
            return min(max(val, minv), maxv);
        }

        public boolean overEvent() {
            return mouseX > xpos && mouseX < xpos + swidth &&
                    mouseY > ypos && mouseY < ypos + sheight;
        }

        public void display() {
            rectMode(CORNER);
            noStroke();
            fill(204);
            rect(xpos, ypos, swidth, sheight);
            if (over || locked) {
                fill(0, 0, 0);
            } else {
                fill(102, 102, 102);
            }
            rect(spos, ypos, sheight, sheight);
        }

        public float getPos() {
            // Convert spos to be values between
            // 0 and the total width of the scrollbar
            return spos * ratio;
        }

        public void setPos(float pos, float minpos, float maxpos) {
            spos = map(pos, minpos, maxpos, sposMin * ratio, sposMax * ratio) / ratio;
        }
    }


    @SuppressWarnings("SuspiciousNameCombination")
    public boolean collisionDetected(float x1, float y1, float w1, float h1, float x2, float y2, float w2, float h2) {  //funzione che controlla se avviene una collisione tra 2 oggett
        if (dist(x1, x1, x2, x2) <= w2 + w1 / 2) {
            return dist(y1, y1, y2, y2) <= h2 + h1 / 2;
        }
        return false;
    }

    class Player {
        float x, y;  //posizione
        float w, h;  //dimensione

        String activeskin;  //nome texture per il presonaggio attiva in questo momento
        PImage activeskint;  //texture per il presonaggio attiva in questo momento

        int fuel, maxfuel;  //carburante e carburante massimo
        PImage jerrycan;  //immagine tanica di benzina per UI (fuelBar())


        Player(float x, float y, float w, float h, int fuel) {  //costruttore parametrico default
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
            try {  //cerca di ottenere il nome texture attivo salvato, se non riesce va in fallback su quella default ("red")
                activeskin = s.savetable.getString(0, "activeskin");
            } catch (Exception e) {
                s.savetable.addColumn("activeskin");
                s.savetable.setString(0, "activeskin", "red");
                saveTable(s.savetable, s.savelocation);
            }
            changeSkin(activeskin);  //cambia la texture in base al nome texture attuale
            this.fuel = fuel;
            maxfuel = this.fuel;
            jerrycan = loadImage("assets/jerrycan.png");
        }

        public void display() {  //mostra il personaggio
            imageMode(CENTER);
            image(activeskint, x, y, w, h);
        }

        public void updatePos(float x, float y) {  //aggiorna la posizione del personaggio
            this.x = x;
            this.y = y;
        }

        public void changeSkin(String skin) {  //funzione che gestisce il cambiamento della texture in base al nome dato
            switch (skin) {
                case "red":
                    activeskin = "red";
                    activeskint = loadImage("assets/car_red.png");
                    break;
                case "black":
                    activeskin = "black";
                    activeskint = loadImage("assets/car_black.png");
                    break;
                case "blue":
                    activeskin = "blue";
                    activeskint = loadImage("assets/car_blue.png");
                    break;
                case "green":
                    activeskin = "green";
                    activeskint = loadImage("assets/car_green.png");
                    break;
                case "lightblue":
                    activeskin = "lightblue";
                    activeskint = loadImage("assets/car_lightblue.png");
                    break;
                case "magenta":
                    activeskin = "magenta";
                    activeskint = loadImage("assets/car_green.png");
                    break;
                case "white":
                    activeskin = "white";
                    activeskint = loadImage("assets/car_white.png");
                    break;
                case "yellow":
                    activeskin = "yellow";
                    activeskint = loadImage("assets/car_yellow.png");
                    break;
                default:
                    Log.e("InfiniteRunner", "!INVALID SKIN NAME, FALLBACK TO DEFAULT ('red')!");
                    activeskin = "red";
                    activeskint = loadImage("assets/car_red.png");
                    break;
            }
        }

        public void updateFuel(int fuel, char t) throws IllegalArgumentException {  //funzione che aggiorna e modifica il valore del fuel. t può essere (S)et, (A)dd o (D)eincrement. Se viene passato un tipo di azione (t) non valido, lancia un errore.
            if (fuel == -1 || t == 'd') {  //-
                this.fuel -= fuel;
            } else if (t == 's') {  //=
                this.fuel = fuel;
            } else if (t == 'a') {  //+
                if (this.fuel + fuel < maxfuel) {  //se non sfora il carburante massimo, aggiunge normalmente
                    this.fuel += fuel;
                } else if (this.fuel + fuel >= maxfuel) {  //se sfora il carburante massimo, imposta il carburante al massimo.
                    this.fuel = maxfuel;
                }
            } else {
                throw new IllegalArgumentException("Invalid action type " + t + ".");
            }
        }

        public void fuelBar(float x, float y, float w, float h) {  //Elemento di UI che mostra sullo schermo una barra che indica quanto carburante è rimasto.
            rectMode(CENTER);
            fill(255, 255, 255, 0);
            rect(x, y, w, h);
            fill(0xffFF0000);
            rectMode(CORNER);
            pushMatrix();
            translate(x + w / 2, y + h / 2);
            rotate(PI);
            rect(0, 0, w, map(fuel, 0, maxfuel, 0, h));
            popMatrix();
            imageMode(CENTER);
            image(jerrycan, x, y + h / 2 + w * 1.5f, w * 1.5f, w * 1.5f);
        }
    }

    class SaveUtils {  //Classe che gestisce i salvataggi

        //Attributi
        Table savetable;  //tabella del salvataggio
        boolean firstTime;  //Vero se è il primo avvio del programma, crea la tabella default
        String savelocation;  //posizione del file di salvataggio.

        SaveUtils() {  //costruttore parametrico default
            savetable = new Table();
            savelocation = "save.csv";
        }

        public void checkSaveFile() {  //controlla se esiste il file di salvataggio, se non esiste lo crea
            try {
                savetable = loadTable(savelocation, "header");
                savetable.getString(0, 0);
                firstTime = false;
            } catch (Exception e) {
                savetable = new Table();
                saveTable(savetable, savelocation);
                firstTime = true;
            }
        }

        public void saveControls() {  //salva i comandi e li ripara se danneggiati.
            try {
                saveTable(savetable, savelocation);
            } catch (Exception e) {
                checkSaveFile();
                firstTimeSetup();
            }
            try {
                savetable.setInt(0, "ctrlLeft", g.c.ctrlLeft);
            } catch (Exception e) {
                savetable.addColumn("ctrlLeft");
                savetable.setInt(0, "ctrlLeft", 37);
            }
            try {
                savetable.setInt(0, "ctrlRight", g.c.ctrlRight);
            } catch (Exception e) {
                savetable.addColumn("ctrlRight");
                savetable.setInt(0, "ctrlRight", 39);
            }
            try {
                savetable.setInt(0, "ctrlSprint", g.c.ctrlSprint);
            } catch (Exception e) {
                savetable.addColumn("ctrlSprint");
                savetable.setInt(0, "ctrlSprint", 16);
            }
            try {
                savetable.setInt(0, "mouseControls", convertBooleanToInt(g.c.mouseControls));
            } catch (Exception e) {
                savetable.addColumn("mouseControls");
                savetable.setInt(0, "mouseControls", 0);
            }
            saveTable(savetable, savelocation);
        }

        public boolean saveHiscore(int score) {  //compara il punteggio massimo con il punteggio ottenuto, se è più alto ritorna vero e salva il punteggio in tabellla.
            if (savetable.getInt(0, "hiscore") < score) {
                savetable.setInt(0, "hiscore", score);
                saveTable(savetable, savelocation);
                //reloadTable();
                return true;
            }
            return false;
        }

        public void reloadTable() {  //ricarica la tabella
            savetable = loadTable(savelocation, "header");
        }

        public void firstTimeSetup() {  //Crea la tabella da 0 e la salva quando necessario
            if (firstTime) {
                //controlli
                savetable.addColumn("ctrlLeft");
                savetable.setInt(0, "ctrlLeft", 37);
                savetable.addColumn("ctrlRight");
                savetable.setInt(0, "ctrlRight", 39);
                savetable.addColumn("ctrlSprint");
                savetable.setInt(0, "ctrlSprint", 16);
                savetable.addColumn("mouseControls");
                savetable.setInt(0, "mouseControls", 0);

                //Record e soldi
                savetable.addColumn("hiscore");
                savetable.setInt(0, "hiscore", 0);
                savetable.addColumn("money");
                savetable.setInt(0, "money", 0);

                //Cosmetici
                savetable.addColumn("activeskin");
                savetable.setString(0, "activeskin", "red");
                savetable.addColumn("unlockedskins");
                savetable.setString(0, "unlockedskins", "red");

                //Trucchi
                savetable.addColumn("unlockedcheats");
                savetable.addColumn("activecheats");

                //Audio
                savetable.addColumn("musicVolume");
                savetable.setFloat(0, "musicVolume", 0);


                saveTable(savetable, savelocation);
            }
        }

        public boolean convertIntToBoolean(int value) throws IllegalArgumentException {  //converte un valore int (0,1) in booleano (0 = false, 1 = true). Lancia un errore in caso viene passato un valore non corretto
            if (value == 1) {
                return true;
            } else if (value == 0) {
                return false;
            } else {
                throw new IllegalArgumentException("Invalid value " + value + ".\nUnable to convert to boolean.");
            }
        }

        public int convertBooleanToInt(boolean value) {  //converte un booleano in int (false = 0, true = 1)
            if (value) {
                return 1;
            } else {
                return 0;
            }
        }

        @SuppressWarnings("unused")
        public void saveActiveSkin(String skin) {  //Prova a salvare la texture del personaggio attualmente attiva, se non riesce a salvarla aggiusta il file per permettere il salvataggio.
            try {
                savetable.setString(0, "activeskin", skin);
                saveTable(savetable, savelocation);
            } catch (Exception e) {
                savetable.addColumn("activeskin");
                savetable.setString(0, "activeskin", skin);
                saveTable(savetable, savelocation);
            }
        }

        /**
         * @param table    Specify the input table
         * @param column   Specify the column
         * @param elements Specify the ArrayList
         */
        @SuppressWarnings("unused")
        public void saveStringArraylistToTable(Table table, String column, ArrayList<String> elements) {  //Prova a salvare un Arraylist di stringhe in tabella, se non è possibile, aggiusta il file di salvataggio per permettere il salvataggio dei valori.
            int i = 0;
            for (String el : elements) {
                try {
                    table.setString(i, column, el);
                    saveTable(table, savelocation);
                } catch (Exception e) {
                    table.addColumn(column);
                    table.setString(i, column, el);
                    saveTable(table, savelocation);
                }
                i++;
            }
        }

        public ArrayList<String> getStringArraylistFromTable(Table table, String column) {  //Trasforma un insieme di stringhe prese da una colonna in ArrayList di stringhe
            ArrayList<String> elements = new ArrayList<>();

            for (int i = 0; i < table.getRowCount(); i++) {
                try {
                    elements.add(table.getString(i, column));
                } catch (Exception e) {
                    table.addColumn(column);
                    saveTable(savetable, savelocation);
                }
            }
            return elements;
        }

        public void saveAudio() {  //Salva le impostazioni audio
            try {
                savetable.setFloat(0, "musicVolume", musicVolume);
                saveTable(savetable, savelocation);
            } catch (Exception e) {
                savetable.addColumn("musicVolume");
                savetable.setFloat(0, "musicVolume", 0);
                saveTable(savetable, savelocation);
            }
        }
    }

    class ShopManager {  //Classe che gestisce il negozio.  SPECIFICA E FATTA APPOSTA SOLO PER QUESTO GIOCO, VA MODIFICATA PER ESSERE RESA RIUTILIZZABILE
        //Attributi
        Button[] bc = new Button[8];  //Bottoni per i cosmetici
        PImage[] ct = new PImage[8];  //texture legate ai cosmetici
        int[] price = new int[12];  //prezzi negozio

        ArrayList<String> unlockedskins;  //arraylist con i cosmetici sbloccati

        Button[] bt = new Button[3];  //bottoni per i trucchi

        ShopManager() {  //costruttore default
            unlockedskins = s.getStringArraylistFromTable(s.savetable, "unlockedskins");  //prende l'elenco di cosmetici sbloccati e li salva in un Arraylist

            //preload texture cosmetici
            ct[0] = loadImage("assets/car_red.png");
            ct[1] = loadImage("assets/car_black.png");
            ct[2] = loadImage("assets/car_blue.png");
            ct[3] = loadImage("assets/car_green.png");
            ct[4] = loadImage("assets/car_lightblue.png");
            ct[5] = loadImage("assets/car_magenta.png");
            ct[6] = loadImage("assets/car_white.png");
            ct[7] = loadImage("assets/car_yellow.png");

            //prezzi
            for (int i = 0; i < 8; i++) {  //prezzi cosmetici
                price[i] = 100;
            }
            for (int i = 9; i < price.length; i++) {  //prezzi trucchi
                price[i] = 500;
            }


            //bottoni
            //COSMETICI
            bc[0] = new Button(width / 4f, height / 3f + height / 85f, 100 + width / 25f, 180 + height / 60f, "cosmetics_buybutton", "red", ct[0], 0xffFF0000, 0xffFFFFFF) {  //macchina rossa
                @Override
                public void onClick() {  //quando viene premuto
                    if (buystate == 'l') {  //se non è acquistato
                        if (s.savetable.getInt(0, "money") >= price[0]) {  //se il giocatore ha i soldi
                            updateBuystate('u');  //sblocca il cosmetico
                            s.savetable.setInt(0, "money", s.savetable.getInt(0, "money") - price[0]);  //rimuove i soldi
                            s.savetable.addRow();  //aggiunge una riga alla tabella
                            s.savetable.setString(s.savetable.findRowIndex(null, "unlockedskins"), "unlockedskins", "red");  //aggiunge il cosmetico appena salvato in tabella
                            saveTable(s.savetable, s.savelocation);  //salva la tabella
                        }
                    } else if (buystate == 'u') {  //se è già acquistato
                        disableSkins();  //disabilita gli altri cosmetici
                        updateBuystate('a');  //aggiorna lo stato ad attivo
                        s.savetable.setString(0, "activeskin", "red");  //aggiorna lo stato in tabella
                        saveTable(s.savetable, s.savelocation);  //salva la tabella
                    }  //se è attivo, non fare nulla.

                }
            };
            bc[1] = new Button(width / 2f, height / 3f + height / 85f, 100 + width / 25f, 180 + height / 60f, "cosmetics_buybutton", "black", ct[1], 0xffFF0000, 0xffFFFFFF) {  //macchina nera  //tutti i cosmetici funzionano in modo identico al primo.
                @Override
                public void onClick() {
                    if (buystate == 'l') {
                        if (s.savetable.getInt(0, "money") >= price[0]) {
                            updateBuystate('u');
                            s.savetable.setInt(0, "money", s.savetable.getInt(0, "money") - 100);
                            s.savetable.addRow();
                            s.savetable.setString(s.savetable.findRowIndex(null, "unlockedskins"), "unlockedskins", "black");
                            saveTable(s.savetable, s.savelocation);
                        }
                    } else if (buystate == 'u') {
                        disableSkins();
                        updateBuystate('a');
                        s.savetable.setString(0, "activeskin", "black");
                        saveTable(s.savetable, s.savelocation);
                    }
                }
            };
            bc[2] = new Button(width / 2f + width / 4f, height / 3f + height / 85f, 100 + width / 25f, 180 + height / 60f, "cosmetics_buybutton", "blue", ct[2], 0xffFF0000, 0xffFFFFFF) {  //macchina blu
                @Override
                public void onClick() {
                    if (buystate == 'l') {
                        if (s.savetable.getInt(0, "money") >= price[0]) {
                            updateBuystate('u');
                            s.savetable.setInt(0, "money", s.savetable.getInt(0, "money") - 100);
                            s.savetable.addRow();
                            s.savetable.setString(s.savetable.findRowIndex(null, "unlockedskins"), "unlockedskins", "blue");
                            saveTable(s.savetable, s.savelocation);
                        }
                    } else if (buystate == 'u') {
                        disableSkins();
                        updateBuystate('a');
                        s.savetable.setString(0, "activeskin", "blue");
                        saveTable(s.savetable, s.savelocation);
                    }
                }
            };
            bc[3] = new Button(width / 4f, height / 3f + 200 + height / 10f, 100 + width / 25f, 180 + height / 60f, "cosmetics_buybutton", "green", ct[3], 0xffFF0000, 0xffFFFFFF) {  //macchina verde
                @Override
                public void onClick() {
                    if (buystate == 'l') {
                        if (s.savetable.getInt(0, "money") >= price[0]) {
                            updateBuystate('u');
                            s.savetable.setInt(0, "money", s.savetable.getInt(0, "money") - 100);
                            s.savetable.addRow();
                            s.savetable.setString(s.savetable.findRowIndex(null, "unlockedskins"), "unlockedskins", "green");
                            saveTable(s.savetable, s.savelocation);
                        }
                    } else if (buystate == 'u') {
                        disableSkins();
                        updateBuystate('a');
                        s.savetable.setString(0, "activeskin", "green");
                        saveTable(s.savetable, s.savelocation);
                    }
                }
            };
            bc[4] = new Button(width / 2f, height / 3f + 200 + height / 10f, 100 + width / 25f, 180 + height / 60f, "cosmetics_buybutton", "lightblue", ct[4], 0xffFF0000, 0xffFFFFFF) {  //macchina azzurra
                @Override
                public void onClick() {
                    if (buystate == 'l') {
                        if (s.savetable.getInt(0, "money") >= price[0]) {
                            updateBuystate('u');
                            s.savetable.setInt(0, "money", s.savetable.getInt(0, "money") - 100);
                            s.savetable.addRow();
                            s.savetable.setString(s.savetable.findRowIndex(null, "unlockedskins"), "unlockedskins", "lightblue");
                            saveTable(s.savetable, s.savelocation);
                        }
                    } else if (buystate == 'u') {
                        disableSkins();
                        updateBuystate('a');
                        s.savetable.setString(0, "activeskin", "lightblue");
                        saveTable(s.savetable, s.savelocation);
                    }
                }
            };
            bc[5] = new Button(width / 2f + width / 4f, height / 3f + 200 + height / 10f, 100 + width / 25f, 180 + height / 60f, "cosmetics_buybutton", "magenta", ct[5], 0xffFF0000, 0xffFFFFFF) {  //macchina rosa magenta
                @Override
                public void onClick() {
                    if (buystate == 'l') {
                        if (s.savetable.getInt(0, "money") >= price[0]) {
                            updateBuystate('u');
                            s.savetable.setInt(0, "money", s.savetable.getInt(0, "money") - 100);
                            s.savetable.addRow();
                            s.savetable.setString(s.savetable.findRowIndex(null, "unlockedskins"), "unlockedskins", "magenta");
                            saveTable(s.savetable, s.savelocation);
                        }
                    } else if (buystate == 'u') {
                        disableSkins();
                        updateBuystate('a');
                        s.savetable.setString(0, "activeskin", "magenta");
                        saveTable(s.savetable, s.savelocation);
                    }
                }
            };
            bc[6] = new Button(width / 3f, height / 3f + 400 + height / 5.5f, 100 + width / 25f, 180 + height / 60f, "cosmetics_buybutton", "white", ct[6], 0xffFF0000, 0xffFFFFFF) {  //macchina bianca
                @Override
                public void onClick() {
                    if (buystate == 'l') {
                        if (s.savetable.getInt(0, "money") >= price[0]) {
                            updateBuystate('u');
                            s.savetable.setInt(0, "money", s.savetable.getInt(0, "money") - 100);
                            s.savetable.addRow();
                            s.savetable.setString(s.savetable.findRowIndex(null, "unlockedskins"), "unlockedskins", "white");
                            saveTable(s.savetable, s.savelocation);
                        }
                    } else if (buystate == 'u') {
                        disableSkins();
                        updateBuystate('a');
                        s.savetable.setString(0, "activeskin", "white");
                        saveTable(s.savetable, s.savelocation);
                    }
                }
            };
            bc[7] = new Button(width - width / 3f, height / 3f + 400 + height / 5.5f, 100 + width / 25f, 180 + height / 60f, "cosmetics_buybutton", "yellow", ct[7], 0xffFF0000, 0xffFFFFFF) {  //macchina gialla
                @Override
                public void onClick() {
                    if (buystate == 'l') {
                        if (s.savetable.getInt(0, "money") >= price[0]) {
                            updateBuystate('u');
                            s.savetable.setInt(0, "money", s.savetable.getInt(0, "money") - 100);
                            s.savetable.addRow();
                            s.savetable.setString(s.savetable.findRowIndex(null, "unlockedskins"), "unlockedskins", "yellow");
                            saveTable(s.savetable, s.savelocation);
                        }
                    } else if (buystate == 'u') {
                        disableSkins();
                        updateBuystate('a');
                        s.savetable.setString(0, "activeskin", "yellow");
                        saveTable(s.savetable, s.savelocation);
                    }
                }
            };

            //TRUCCHI  //L'USO DI TRUCCHI BLOCCA IL GUADAGNO DEI SOLDI MENTRE SONO ATTIVI
            bt[0] = new Button(width - width / 4f, height / 2f - 60 - height / 28f, 100 + width / 18f, 50 + height / 60f, "text", "godmode", 0xffff0000, 0xffffffff) {  //invincibilità
                @Override
                public void onClick() {
                    if (buystate == 'l') {  //se non è acquistato
                        if (s.savetable.getInt(0, "money") >= price[9]) {  //se il giocatore ha i soldi
                            updateBuystate('u');  //aggiorna lo stato di acquisto a sbloccato
                            s.savetable.setInt(0, "money", s.savetable.getInt(0, "money") - price[9]);  //rimuove i soldi in base al prezzo
                            s.savetable.setString(s.savetable.findRowIndex("N/A", "unlockedcheats"), "unlockedcheats", "godmode");  //cambia uno spazio libero della colonna "unlockedcheats" con il suo nome
                            saveTable(s.savetable, s.savelocation);  //salva la tabella
                        }
                    } else if (buystate == 'u') {  //se già acquistato
                        updateBuystate('a');  //imposta lo stato ad attivo
                        s.savetable.setString(s.savetable.findRowIndex("N/A", "activecheats"), "activecheats", "godmode");  //sostituisce uno spazio libero nella colonna "activecheats" con se stesso
                        saveTable(s.savetable, s.savelocation);  //salva la tabella
                    } else if (buystate == 'a') {  //se è già attivo
                        updateBuystate('u');  //imposta lo stato a sbloccato (lo disabilita)
                        s.savetable.setString(s.savetable.findRowIndex("godmode", "activecheats"), "activecheats", "N/A");  //ripristina lo spazio in tabella dove stava in uno spazio libero ("N/A")
                        saveTable(s.savetable, s.savelocation);  //salva la tabella
                    }
                }
            };
            bt[1] = new Button(width - width / 4f, height / 2f - height / 100f, 100 + width / 18f, 50 + height / 60f, "text", "nofuel", 0xffff0000, 0xffffffff) {  //carburante infinito  //Tutti i bottoni per l'acquisto/utilizzo dei trucchi sono uguali, non ricommento
                @Override
                public void onClick() {
                    if (buystate == 'l') {
                        if (s.savetable.getInt(0, "money") >= price[9]) {
                            updateBuystate('u');
                            s.savetable.setInt(0, "money", s.savetable.getInt(0, "money") - price[10]);
                            s.savetable.setString(s.savetable.findRowIndex("N/A", "unlockedcheats"), "unlockedcheats", "nofuel");
                            saveTable(s.savetable, s.savelocation);
                        }
                    } else if (buystate == 'u') {
                        updateBuystate('a');
                        s.savetable.setString(s.savetable.findRowIndex("N/A", "activecheats"), "activecheats", "nofuel");
                        saveTable(s.savetable, s.savelocation);
                    } else if (buystate == 'a') {
                        updateBuystate('u');
                        s.savetable.setString(s.savetable.findRowIndex("nofuel", "activecheats"), "activecheats", "N/A");
                        saveTable(s.savetable, s.savelocation);
                    }
                }
            };
            bt[2] = new Button(width - width / 4f, height / 2f + 60 + height / 60f, 100 + width / 18f, 50 + height / 60f, "text", "maxspeed", 0xffff0000, 0xffffffff) {  //velocità massima gioco
                @Override
                public void onClick() {
                    if (buystate == 'l') {
                        if (s.savetable.getInt(0, "money") >= price[9]) {
                            updateBuystate('u');
                            s.savetable.setInt(0, "money", s.savetable.getInt(0, "money") - price[11]);
                            s.savetable.setString(s.savetable.findRowIndex("N/A", "unlockedcheats"), "unlockedcheats", "maxspeed");
                            saveTable(s.savetable, s.savelocation);
                        }
                    } else if (buystate == 'u') {
                        updateBuystate('a');
                        s.savetable.setString(s.savetable.findRowIndex("N/A", "activecheats"), "activecheats", "maxspeed");
                        saveTable(s.savetable, s.savelocation);
                    } else if (buystate == 'a') {
                        updateBuystate('u');
                        s.savetable.setString(s.savetable.findRowIndex("maxspeed", "activecheats"), "activecheats", "N/A");
                        saveTable(s.savetable, s.savelocation);
                    }
                }
            };

            assingLockstate();  //assegna ad ogni pulsante il suo stato di acquisto
        }

        public void displayCosmeticsButtons() {  //display dei bottoni per l'acquisto/modifica dei cosmetici
            for (Button button : bc) {
                button.updateLabelAfterBuystring();
                button.display();
            }
        }

        public void displayCheatsButtons() {  //display dei bottoni per l'acquisto/modifica dei trucchi
            for (Button button : bt) {
                //bt[i].updateLabelAfterBuystring();
                button.display();
            }
        }

        public void assingLockstate() {  //funzione che assegna ad ogni pulsante il proprio stato di acquisto.
            //controlla nelle colonne della tabella di salvataggio la presenza dei cosmetici/trucchi, se attivi vengono impostati attivi, se sbloccati vengono impostati acquistati, altrtimenti vengono impostati su bloccati
            //Cosmetici
            for (int i = 0; i < s.savetable.getRowCount(); i++) {
                if (s.savetable.getRow(i) != null) {
                    for (Button button : bc) {
                        if (s.savetable.getString(0, "activeskin").equals(button.label)) {
                            button.updateBuystate('a');
                        } else if (s.savetable.getString(i, "unlockedskins").equals(button.label) && !button.label.equals("Attiva") && !button.label.equals("Compra")) {

                            button.updateBuystate('u');
                        }
                    }
                }
            }
            for (int i = 0; i < s.savetable.getRowCount(); i++) {
                for (Button button : bc) {
                    if (!s.savetable.getString(i, "unlockedskins").equals(button.label) && !button.label.equals("Usa") && !button.label.equals("Attiva")) {
                        button.updateBuystate('l');
                    }
                }
            }

            //Trucchi
            for (int i = 0; i < s.savetable.getRowCount(); i++) {
                for (Button button : bt) {
                    try {
                        if (s.savetable.getString(i, "activecheats").equals(button.label)) {
                            button.updateBuystate('a');
                        }
                    } catch (Exception e) {
                        s.savetable.setString(i, "activecheats", "N/A");
                        saveTable(s.savetable, s.savelocation);
                    }
                }
            }
            for (int i = 0; i < s.savetable.getRowCount(); i++) {
                for (Button button : bt) {
                    try {
                        if (s.savetable.getString(i, "unlockedcheats").equals(button.label) && !button.label.equals("Attiva") && !button.label.equals("Compra")) {
                            button.updateBuystate('u');
                        }
                    } catch (Exception e) {
                        s.savetable.setString(i, "unlockedcheats", "N/A");
                        saveTable(s.savetable, s.savelocation);
                        button.updateBuystate('l');
                    }
                }
            }
            for (int i = 0; i < s.savetable.getRowCount(); i++) {
                for (Button button : bt) {
                    if (!s.savetable.getString(i, "unlockedcheats").equals(button.label) && !button.label.equals("Usa") && !button.label.equals("Attiva")) {
                        button.updateBuystate('l');
                    }
                }
            }
        }

        public void disableSkins() {  //Funzione che disabilita i cosmetici
            for (Button button : bc) {
                if (button.label.equals("Attiva")) {
                    button.updateBuystate('u');
                }
            }
        }
    }

    public void settings() {
        fullScreen();
    }
}
