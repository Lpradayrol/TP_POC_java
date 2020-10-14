// -*- coding: utf-8 -*-

import java.util.ArrayList;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.*;

public class SeptNains {
    static private SimpleDateFormat sdf = 
        new SimpleDateFormat("hh'h 'mm'mn 'ss','SSS's'");    

    public static void main(String[] args) throws InterruptedException {
        Date début = new Date(System.currentTimeMillis());
        System.out.println("[" + sdf.format(début) + "] Début du programme.");
        
        final BlancheNeige bn = new BlancheNeige();
        final int nbNains = 7;
        final String noms [] = {"Simplet", "Dormeur",  "Atchoum", "Joyeux", 
                                "Grincheux",
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
            System.out.println("[" + sdf.format(new Date(System.currentTimeMillis()))
                 + "] Interruption 7 nains.");
            for(int n = 0; n < nbNains; n++){
                nain[n].interrupt();
            }
        }

        /*Question 3*/
        for(int n = 0; n < nbNains; n++){
            nain[n].join();
        }

        System.out.println("[" + sdf.format(new Date(System.currentTimeMillis())) 
            + "] Fin des nains.");
    }
}    

class BlancheNeige {

    /* file d'attente pour un partage équilibré pour l'accès à la ressource
    * http://blog.paumard.org/cours/java-api/chap05-concurrent-queues.html
    */
    private BlockingQueue<Nain> file_acces = new ArrayBlockingQueue<Nain>(7);


    private volatile boolean libre = true; // Initialement, Blanche-Neige est libre.
    public synchronized void requérir () {

        //le nain courant est ajouté à la file car il souhaite un accès à la ressource
        Nain threadCourant = (Nain) Thread.currentThread();
        file_acces.add(threadCourant);

        System.out.println("\t" + threadCourant.getName()
                           + " veut la ressource.");
    }

    public synchronized void accéder () throws InterruptedException {

        Nain threadCourant = (Nain) Thread.currentThread();

        /* il faut maintenant vérifier que Blanche-Neige est libre 
        * pour donner accès au nain s'il est premier dans la file de priorité
         https://www.geeksforgeeks.org/arrayblockingqueue-peek-method-in-java/
          (ref. peek())
        */

        while ( ! libre || !threadCourant.equals(file_acces.peek())) {
            wait(); // Le nain s'endort sur l'objet bn
        }                    
        libre = false;
        System.out.println("\t" + Thread.currentThread().getName()
                           + " accède à la ressource.");
    }

    public synchronized void relâcher () {
        System.out.println("\t" + Thread.currentThread().getName()
                           + " relâche la ressource.");
        libre = true;

        //le nain relâche la ressource, il faut donc le retirer de la file d'attente
        file_acces.poll();
        notifyAll();
    }
}

class Nain extends Thread {
    private BlancheNeige bn;

    static private SimpleDateFormat time = 
        new SimpleDateFormat("hh'h 'mm'mn 'ss','SSS's'");    

    public Nain(String nom, BlancheNeige bn) {
        this.setName(nom);
        this.bn = bn;
    }

    public void run() {
        /*Si le nain non interrompu, il peut avoir accès à la ressource*/
        while(!isInterrupted()){
            try {
                bn.requérir();
                bn.accéder();
                System.out.println("[" + time.format(
                        new Date(System.currentTimeMillis())) + "]" 
                        + getName() 
                        + " a un accès (exclusif) à Blanche-Neige.");

                try { sleep(2000); 
                } catch (InterruptedException exception1) {
                    long temps_interruption = System.currentTimeMillis();
                    long temps = System.currentTimeMillis() - temps_interruption;

                        try { sleep(temps); 
                        } catch (InterruptedException exception2) { 
                            this.interrupt(); 
                        } finally { this.interrupt(); } 

                    } finally { 
                    System.out.println("[" + time.format(
                        new Date(System.currentTimeMillis())) + "] " 
                        + getName() 
                        + " s'apprête à quitter Blanche-Neige.");
                    bn.relâcher(); }
            } catch(InterruptedException exception3) {
                this.interrupt();
            }

            //interruption nains, message de fin
            System.out.println("[" + time.format(
                new Date(System.currentTimeMillis())) 
                + "]" + getName() 
                + " a terminé!");
        }
    }   
} 
