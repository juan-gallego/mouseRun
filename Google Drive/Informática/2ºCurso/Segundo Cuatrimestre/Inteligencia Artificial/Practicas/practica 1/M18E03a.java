/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mouserun.mouse;

import mouserun.game.*;
import java.util.*;

/**
 *
 * @author usuario
 */
public class M18E03a extends Mouse {
    
    private List<Visitadas> visitada;//lista con las casillas visitadas
    private Stack<Integer> pila;//pila en la que almacena los movimientos que va haciendo el ratón
    int bomba;//una variable que sirve para poner una bomba cada 60 movientos

    public M18E03a() {
        super("RompeRecordsEx");
        //inicializamos las variables
        visitada = new ArrayList<>();
        bomba=0;
        pila = new Stack();
    }

    @Override
    public int move(Grid currentGrid, Cheese cheese) {
        int x = currentGrid.getX();
        int y = currentGrid.getY();
        int xder = x + 1;
        int xiz = x - 1;
        int yar = y + 1;
        int yab = y - 1;
        //Se crean los valores para combprobar si la casilla destino esta visitada
        int menor = 500;
        int next = 0;
        Grid nextIZ = new Grid(x, y);
        Grid nextUP = new Grid(x, y);
        Grid nextDE = new Grid(x, y);
        Grid nextDO = new Grid(x, y);

        bomba++;
        if (bomba == 60) {
            bomba = 0;
            return Mouse.BOMB;
        }
        //para poner las bombas
        if (currentGrid.canGoLeft() == true) {
            Grid cIz = new Grid(xiz, y);
            //Crea la casilla a la que iria
            if (buscar(cIz) == false) {
                //comprueba si la casilla esta en el vector de visitadas
                Visitadas nueva = new Visitadas(cIz);
                //crea un objeto de la clase visitada con los valores
                visitada.add(nueva);
                //lo mete en el vector
                this.incExploredGrids();
                //imcrementa el numero de casillas visitadas
                pila.add(Mouse.RIGHT);
                //añade el movimiento contrario al realizado para deshacer el movimiento si se encuentra en un callejón sin salida o sin casillas que no haya visitado
                return Mouse.LEFT;
                //devuelve el movimiento
            } else {
                int a = 999;
                a = visitar(cIz);
                if (a < menor) {
                    menor = a;
                    nextIZ = cIz;
                    next = 3;
                    //Comprueba si es la menos visitada de las casillas que tiene alrededor(esto es por si esta rodeado de casillas ya visitadas y tiene la pila de movimientos anteriores vacía poque hemos muerto por una bomba)
                }

            }
        }
        if (currentGrid.canGoUp() == true) {
            Grid cUp = new Grid(x, yar);
            if (buscar(cUp) == false) {
                Visitadas nueva = new Visitadas(cUp);
                visitada.add(nueva);
                this.incExploredGrids();
                pila.add(Mouse.DOWN);
                return Mouse.UP;
            } else {
                int a = 999;
                a = visitar(cUp);
                if (a < menor) {
                    menor = a;
                    nextUP = cUp;
                    next = 1;
                }

            }
        }
        if (currentGrid.canGoRight() == true) {
            Grid cDer = new Grid(xder, y);
            if (buscar(cDer) == false) {
                Visitadas nueva = new Visitadas(cDer);
                visitada.add(nueva);
                this.incExploredGrids();
                pila.add(Mouse.LEFT);
                return Mouse.RIGHT;
            } else {
                int a = 999;
                a = visitar(cDer);
                if (a < menor) {
                    menor = a;
                    nextDE = cDer;
                    next = 4;
                }

            }
        }
        if (currentGrid.canGoDown() == true) {
            Grid cDow = new Grid(x, yab);
            if (buscar(cDow) == false) {
                Visitadas nueva = new Visitadas(cDow);
                visitada.add(nueva);
                this.incExploredGrids();
                pila.add(Mouse.UP);
                return Mouse.DOWN;
            } else {
                int a = 999;
                a = visitar(cDow);
                if (a < menor) {
                    menor = a;
                    nextDO = cDow;
                    next = 2;
                }

            }
        }
       if(pila.empty()){
           if (next == 1) {
               incrementar(nextUP);

               return Mouse.UP;
           }
           if (next == 2) {
               incrementar(nextDO);
               return Mouse.DOWN;
           }
           if (next == 3) {
               incrementar(nextIZ);
               return Mouse.LEFT;
           }
           if (next == 4) {
               incrementar(nextDE);
               return Mouse.RIGHT;
           }
           //Cuando esta la pila de movimientos anteriores vacía el ratón va a la casilla que menos haya visitado
       }
        return pila.pop();
        //sin o tiene ningúna casilla a la que pueda ir que no estee visitado vuelve sobre sus anteriores pasos
    }

    @Override
    public void newCheese() {

    }

    @Override
    public void respawned() {

        pila.clear();

    }

    public int incrementar(Grid casilla) {
        //incrementa el número de veces que se ha estado en una casilla
        int pos = 0;
        while (pos < visitada.size()) {
            if (visitada.get(pos).equal(casilla)) {
                visitada.get(pos).incrementar();
                return 1;
            } else {
                pos++;
            }
        }
        return 1;
    }

    public int visitar(Grid casilla) {
        //devuelve el número de veces que se ha pasado por una casilla
        for (int i = 0; i < visitada.size(); i++) {
            if (visitada.get(i).equal(casilla) == true) {
                return visitada.get(i).getVisitadas();
            }
        }
        return 999;
    }

    public boolean buscar(Grid casilla) {
        //Busca la casilla en el vector de casillas visitadas
        for (int i = 0; i < visitada.size(); i++) {
            if (visitada.get(i).equal(casilla) == true) {
                return true;
            }
        }
        return false;

    }

    class Visitadas {

        private Grid grid;
        private int c;

        public Visitadas(Grid grid) {
            this.grid = grid;
            c = 1;
        }

        public void incrementar() {
            this.c++;
        }

        public Grid getGrid() {
            return grid;
        }

        public int getVisitadas() {
            return c;
        }

        public boolean equal(Grid a) {//comprueba si la casilla en la que estamos y la que le pasamos como parámetro, son iguales
            return (grid.getX() == a.getX() && grid.getY() == a.getY());
        }
    }

}
