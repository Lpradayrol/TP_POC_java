// -*- coding: utf-8 -*-

import java.util.ArrayList;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SeptNains {
    static private SimpleDateFormat sdf = new SimpleDateFormat("hh'h 'mm'mn 'ss','SSS's'");    

    public static void main(String[] args) throws InterruptedException {
        Date début = new Date(System.currentTimeMillis());
        System.out.println("[" + sdf.format(début) + "] Début du programme.");
        
        final BlancheNeige bn = new BlancheNeige();
        final int nbNains = 7;
        final String noms [] = {"Simplet", "Dormeur",  "Atchoum", "Joyeux", "Grincheux",
                                "Prof", "Timide"};
        final Nain nain [] = new Nain [nbNains];
        for(int i = 0; i < nbNains; i++) nain[i] = new Nain(noms[i],bn);
        for(int i = 0; i < nbNains; i++) nain[i].start();

        /*Question 1
            java.lang.InterruptedException: sleep interrupted
            at java.base/java.lang.Thread.sleep(Native Method)
            at Nain.run(SeptNains.java:71)
        */
        try { 
            Thread.sleep(5000);
        } catch(InterruptedException exception) { 
            exception.printStackTrace();
        } finally {
            System.out.println("[" + sdf.format(new Date(System.currentTimeMillis())) + "] Interruption 7 nains.");
            for(int n = 0; n < nbNains; n++){
                nain[n].interrupt();
            }
        }

        /*Question 3*/
        for(int n = 0; n < nbNains; n++){
            nain[n].join();
        }

        System.out.println("[" + sdf.format(new Date(System.currentTimeMillis())) + "] Fin des nains.");
    }
}    

class BlancheNeige {
    private volatile boolean libre = true;        // Initialement, Blanche-Neige est libre.
    public synchronized void requérir () {
        System.out.println("\t" + Thread.currentThread().getName()
                           + " veut la ressource.");
    }

    public synchronized void accéder () throws InterruptedException {
        if ( ! libre ) wait();                    // Le nain s'endort sur l'objet bn
        libre = false;
        System.out.println("\t" + Thread.currentThread().getName()
                           + " accède à la ressource.");
    }

    public synchronized void relâcher () {
        System.out.println("\t" + Thread.currentThread().getName()
                           + " relâche la ressource.");
        libre = true;
        notifyAll();
    }
}

class Nain extends Thread {
    private BlancheNeige bn;

    static private SimpleDateFormat time = new SimpleDateFormat("hh'h 'mm'mn 'ss','SSS's'");    

    public Nain(String nom, BlancheNeige bn) {
        this.setName(nom);
        this.bn = bn;
    }

    public void run() {
        /*Si le nain non interrompu, il peut avoir accès à la ressource*/
        while(!isInterrupted()) {
            try {
                bn.requérir();
                bn.accéder();
                System.out.println("[" + time.format(new Date(System.currentTimeMillis())) + "]" + getName() + " a un accès (exclusif) à Blanche-Neige.");

                try {
                    sleep(2000);
                } catch (InterruptedException exception){
                    this.interrupt();
                } finally {
                    System.out.println("[" + time.format(new Date(System.currentTimeMillis())) + "]" + getName() + " s'apprête à quitter Blanche-Neige.");
                    bn.relâcher();
                }
            } catch(InterruptedException exception){
                this.interrupt();
            }
        }

        //interruption nain, message de fin
        System.out.println("[" + time.format(new Date(System.currentTimeMillis())) + "]" + getName() + " a terminé!");
    }
        
    //System.out.println(getName() + " a terminé!");
} 

/*
$ make
$ java SeptNains
[09h 34mn 01,834s] Début du programme.
	Simplet veut la ressource.
	Simplet accède à la ressource.
	Timide veut la ressource.
	Prof veut la ressource.
	Grincheux veut la ressource.
	Joyeux veut la ressource.
	Atchoum veut la ressource.
	Dormeur veut la ressource.
Simplet a un accès (exclusif) à Blanche-Neige.
Simplet s'apprête à quitter à Blanche-Neige.
	Simplet relâche la ressource.
	Simplet veut la ressource.
	Simplet accède à la ressource.
Simplet a un accès (exclusif) à Blanche-Neige.
	Timide accède à la ressource.
Timide a un accès (exclusif) à Blanche-Neige.
	Dormeur accède à la ressource.
Dormeur a un accès (exclusif) à Blanche-Neige.
	Atchoum accède à la ressource.
Atchoum a un accès (exclusif) à Blanche-Neige.
	Joyeux accède à la ressource.
Joyeux a un accès (exclusif) à Blanche-Neige.
	Grincheux accède à la ressource.
Grincheux a un accès (exclusif) à Blanche-Neige.
	Prof accède à la ressource.
Prof a un accès (exclusif) à Blanche-Neige.
^C
*/
