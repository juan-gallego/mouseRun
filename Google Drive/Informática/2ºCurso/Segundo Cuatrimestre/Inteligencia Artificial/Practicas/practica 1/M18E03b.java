/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mouserun.mouse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Stack;
import mouserun.game.Mouse;
import mouserun.game.Grid;
import mouserun.game.Cheese;

public class M18E03b
	extends Mouse {
private HashMap<Pair<Integer, Integer>, mouseNode> visitado;
     //Contiene las casillas conocidas del laberinto
     //se almacena las cordenadas x e y en un pair y la casilla en mouseNode
    private Stack<Integer> camino;
    //Contiene los movimientos a realizar. Bien para llegar a un Cheese,
    //o para llegar a una casilla no explorada.
    private List<mouseNode> noExploradasArea;
    //Contiene las casillas no exploradas que bordean
    //la casilla donde esta el Cheese.
    private int cuentaMov;  //Cuenta los movimientos. Se reinicia al colocar una bomba.
    private int bombasLibres;  //Cuenta las bombas que quedan por poner.
    private Pair<Integer, Integer> bordeMapa;// Almacena los limites conocidos del mapa
    private int tamMapa; ////Almacena el tamaño del mapa, basado en borderMap

	public M18E03b()
	{
		super("RompeRecords");	
                 cuentaMov = 0;
        bombasLibres = 5;
        visitado=new HashMap<>();
        camino=new Stack<>();
        noExploradasArea = new ArrayList<>();      
        bordeMapa = new Pair<>(5,5);
        tamMapa = bordeMapa.first * bordeMapa.second;

                
	}
	
	public int move(Grid currentGrid, Cheese cheese)
	{
            //Creamos un Pair, con la posicion actual y una referancia a un mouseNode
        Pair<Integer, Integer> currentPos = new Pair<>(currentGrid.getX(), currentGrid.getY());
        
                     mouseNode nodoActual;
                     //Actualizamos los bordes del mapa, con la posicion y las coordenadas del Cheese
        if (cheese.getX() > bordeMapa.first) {
            bordeMapa.first = cheese.getX();
            tamMapa = (bordeMapa.first + 1) * (bordeMapa.second + 1);
        }
        if (cheese.getY() > bordeMapa.second) {
            bordeMapa.second = cheese.getY();
            tamMapa = (bordeMapa.first + 1) * (bordeMapa.second + 1);
        }
        if (currentPos.first > bordeMapa.first) {
            bordeMapa.first = currentPos.first;
            tamMapa = (bordeMapa.first + 1) * (bordeMapa.second + 1);
        }
        if (currentPos.second > bordeMapa.second) {
            bordeMapa.second = currentPos.second;
            tamMapa = (bordeMapa.first + 1) * (bordeMapa.second + 1);
        }
         //Buscamos en el mapa la posicion actual. Si esta, currentNode sera el nodo almacenado
        //en caso contrario, se crea un nuevo nodo y se almacena.
        if (visitado.containsKey(currentPos)) {
            nodoActual = visitado.get(currentPos);
        } else {
            nodoActual = new mouseNode(
                    currentPos,
                    currentGrid.canGoUp(), currentGrid.canGoDown(),
                    currentGrid.canGoLeft(), currentGrid.canGoRight()
            );
            incExploredGrids();

            visitado.put(currentPos, nodoActual);
        }
        //En caso de que nos encontremos en la casilla del cheese,
        //abandonamos la casilla y volvemos a ella
        if (cheese.getX() == nodoActual.x && cheese.getY() == nodoActual.y && camino.isEmpty()) {
            if (currentGrid.canGoUp()) {
                camino.add(Mouse.DOWN);
                camino.add(Mouse.UP);
            } else {
                if (currentGrid.canGoDown()) {
                    camino.add(Mouse.UP);
                    camino.add(Mouse.DOWN);
                } else {
                    if (currentGrid.canGoLeft()) {
                        camino.add(Mouse.RIGHT);
                        camino.add(Mouse.LEFT);
                    } else {
                        if (currentGrid.canGoRight()) {
                            camino.add(Mouse.LEFT);
                            camino.add(Mouse.RIGHT);
                        }
                    }
                }
            }
        }
         //Comprobamos si quedan bombas
        if (bombasLibres > 0) {
            int exitCount = 0;
            //Almacena la cantidad de direcciones por las que
            //se puede avanzar, desde el nodo actual.

            if (nodoActual.up) {
                exitCount++;
            }
            if (nodoActual.down) {
                exitCount++;
            }
            if (nodoActual.left) {
                exitCount++;
            }
            if (nodoActual.right) {
                exitCount++;
            }

            //Segun el numero de movimientos y el numero de salidas, se decide
            //si colocar, o no, una bomba.
            if (cuentaMov > 30 && exitCount > 2) {
                cuentaMov = 0;
                bombasLibres--;
                return Mouse.BOMB;
            } else {
                    cuentaMov++;
                
            }
        }
        //Si no hay ningun camino, generamos uno.
        if (camino.isEmpty()) {
            Pair<Integer, Integer> queso = new Pair<>(cheese.getX(), cheese.getY());
            getCamino(nodoActual, queso);
            //Obtenemos un camino al Cheese
            //o a una casilla no explorada.
        }

        return camino.pop();
                   
     }
    
 
            @Override
    public void newCheese() {
        camino.clear();
        noExploradasArea.clear();
    }

    @Override
    public void respawned() {
        camino.clear();
        noExploradasArea.clear();
    }
    /**
     * Empleamos una busqueda en profundidad, para obtener el camino a queso, o a
     * una casilla no explorada.
     *
     * @param nodoRaiz Nodo inicial, del que parte la busqueda
     * @param queso Posicion objetivo
     */
    private void getCamino(mouseNode nodoRaiz, Pair<Integer, Integer> queso) {
        List<mouseNode> noExploradas = new ArrayList<>();
        List<mouseNode> area = new ArrayList<>();
        //Almacena las casillas de noExploradasArea que son accesibles

        //Obtenemos un nuevo HashMap, que contiene el nodo anterior al dado.
        //De esta manera obtenemos el camino.
        HashMap<Pair<Integer, Integer>, mouseNode> anteriores = getAnteriores(nodoRaiz, queso, noExploradas, area);

        mouseNode nodoObjetivo;
        mouseNode w;

        //Comprobamos si tenemos camino directo al queso, en caso contrario
        //elegimos una casilla no explorada de noExploradas o area.
        if (visitado.containsKey(queso) && anteriores.containsKey(queso)) {
            nodoObjetivo = visitado.get(queso);
        } else {
            int i;
            if (!area.isEmpty()) {
                i = getDistanciaMin(area, queso);
                nodoObjetivo = area.get(i);
            } else {
                i = getDistanciaMin(noExploradas, queso);
                nodoObjetivo = noExploradas.get(i);
            }
        }

        //Finalmente obtenemos el camino al nodoqueso obtenido.
        w = anteriores.get(nodoObjetivo.getPos());
        camino.add(getDirection(w.getPos(), nodoObjetivo.getPos()));

        while (w != nodoRaiz) {
            Pair<Integer, Integer> posicionObjetivo = w.getPos();
            w = anteriores.get(w.getPos());
            camino.add(getDirection(w.getPos(), posicionObjetivo));
        }
    }

     /**
     * Obtiene los predecesores de los nodos, para poder calcular el camino
     * posteriormente. Realiza una busqueda en profundidad.
     *
     * @param nodoRaiz nodo inicial
     * @param queso posicion objetivo
     * @param noExploradas lista de nodos no explorados accesibles
     * @param area lista de nodos no explorados de noExploradasArea, accesibles
     * @return Devuelve un HashMap de Pair<Integer, Integer> y mouseNode. Este
     * contiene el nodo anterior a la posición pasada como clave.
     */    
    private HashMap<Pair<Integer, Integer>, mouseNode> getAnteriores(mouseNode nodoRaiz, Pair<Integer, Integer> queso, List<mouseNode> noExploradas, List<mouseNode> area) {
        HashMap<Pair<Integer, Integer>, mouseNode> anteriores = new HashMap<>();
        
        //cola para
        Queue<mouseNode> cola = new LinkedList<>();
        List<mouseNode> visitados = new ArrayList<>();

        cola.add(nodoRaiz);
        visitados.add(nodoRaiz);
        
        nodoRaiz.distancia = 0;

        while (!cola.isEmpty()) {
            
            //poll saca el primer elemento de la cola 
            mouseNode elemento = cola.poll();

            if (elemento.getPos() == queso) {
                //break hace que el bucle pare cuando se ejecuta, es decir cuando v es la posicion
                //que buscamos el bucle deja de ejecutarse
                break;
            }

            mouseNode w;
            mouseNode notExplored;
            Pair<Integer, Integer> targetPos;

            //UP
            /*
            v.up lo que hace es que si lo conoce y no esta metido en visitado  lo mete y lo vuelve a meter en la cola
            y en anteriores que es un mapa que luego devuelve y el else  es que si no esta en conocido,
            mete en notexplored que es una lista de nodos que no se han explorado y son accesibles 
            */
            if (elemento.up) {
                targetPos = elemento.getPos();
                //targetPos.second es la y del nodo que buscamos
            //  targetPos = v.getPos();
            //y eso hace lo que hace es asignarle el pair de de v a targetPos
                 targetPos.second += 1;

                if (visitado.containsKey(targetPos)) {
                    //a w se le asigna el nodo que tiene como clave targetPos
                    w = visitado.get(targetPos);
                        
                    if (!visitados.contains(w)) {
                        visitados.add(w);
                        //se añade w por si se encuentra una pared para que vuelva al anterior
                        // si no se muere el raton
                        cola.add(w);
                       
                        anteriores.put(w.getPos(), elemento);
                        w.distancia = elemento.distancia + 1;
                    }
                } else {
                    notExplored = new mouseNode(targetPos);
                    visitados.add(notExplored);
                    anteriores.put(notExplored.getPos(), elemento);
                    noExploradas.add(notExplored);
                    notExplored.distancia = elemento.distancia + 1;

                    if (noExploradasArea.contains(notExplored)) {
                        area.add(notExplored);
                    }
                }
            }

            //DOWN
            if (elemento.down) {
                targetPos = elemento.getPos();
                targetPos.second -= 1;

                if (visitado.containsKey(targetPos)) {
                    w = visitado.get(targetPos);

                    if (!visitados.contains(w)) {
                        visitados.add(w);
                        cola.add(w);
                      
                        anteriores.put(w.getPos(), elemento);
                        w.distancia = elemento.distancia + 1;
                    }
                } else {
                    notExplored = new mouseNode(targetPos);
                    visitados.add(notExplored);
                    anteriores.put(notExplored.getPos(), elemento);
                    noExploradas.add(notExplored);
                    notExplored.distancia = elemento.distancia + 1;

                    if (noExploradasArea.contains(notExplored)) {
                        area.add(notExplored);
                    }
                }
            }

            //LEFT
            if (elemento.left) {
                targetPos = elemento.getPos();
                targetPos.first -= 1;

                if (visitado.containsKey(targetPos)) {
                    w = visitado.get(targetPos);

                    if (!visitados.contains(w)) {
                        visitados.add(w);
                        cola.add(w);
                       
                        anteriores.put(w.getPos(), elemento);
                        w.distancia = elemento.distancia + 1;
                    }
                } else {
                    notExplored = new mouseNode(targetPos);
                    visitados.add(notExplored);
                    anteriores.put(notExplored.getPos(), elemento);
                    noExploradas.add(notExplored);
                    notExplored.distancia = elemento.distancia + 1;

                    if (noExploradasArea.contains(notExplored)) {
                        area.add(notExplored);
                    }
                }
            }
            //RIGHT
            if (elemento.right) {
                targetPos = elemento.getPos();
                targetPos.first += 1;

                if (visitado.containsKey(targetPos)) {
                    w = visitado.get(targetPos);
                      
                    if (!visitados.contains(w)) {
                        visitados.add(w);
                        cola.add(w);
                      
                        anteriores.put(w.getPos(), elemento);
                        w.distancia = elemento.distancia + 1;
                    }
                } else {
                    notExplored = new mouseNode(targetPos);
                    visitados.add(notExplored);
                    anteriores.put(notExplored.getPos(), elemento);
                    noExploradas.add(notExplored);
                    notExplored.distancia = elemento.distancia + 1;

                    if (noExploradasArea.contains(notExplored)) {
                        area.add(notExplored);
                    }
                }
            }
        }

        return anteriores;
    }

    /**
     * Dada una lista de nodos, trata encontrar el  nodo con menor
     * valor según la distancia y devuelve su indice.
     *
     * @param nodes lista de nodos candidatos
     * @param target posicion objetivo
     * @return Devuelve el indice de la lista nodes con menor valor.
     */
    private int getDistanciaMin(List<mouseNode> nodes, Pair<Integer, Integer> target) {
        double minValue = 9e99;
        int minPos = 0;

        for (int i = 0; i < nodes.size(); i++) {
            if (nodes.get(i).getPos() == target) {
                return i;
            }

            double curValue = getValue(nodes.get(i), target);

            if (curValue < minValue) {
                minPos = i;
                minValue = curValue;
            }
        }

        return minPos;
    }

    /**
     * El nodo de entrada es evaluado, respecto a target y se devuelve el resultado
     *
     * @param init nodo a calcular
     * @param target posicion objetivo
     * @return Valor de la funcion heuristica.
     */
    private double getValue(mouseNode init, Pair<Integer, Integer> target) {

        double percentMapExplored = visitado.size() / tamMapa;
        //distQueso=raiz cuadrada de((xf-xi)^2+(yf-yi)^2) es como un modulo
        double distQueso = Math.sqrt(
                (target.first - init.getPos().first) * (target.first - init.getPos().first)
                + (target.second - init.getPos().second) * (target.second - init.getPos().second)
        );
        int costeCasilla = init.distancia;

        if (costeCasilla <= 3) {
            distQueso = distQueso * 0.1 * costeCasilla;
        }
        
        return (1 - percentMapExplored) * distQueso * 2 + percentMapExplored * costeCasilla;
        
    }

    /**
     * Dadas dos posiciones, devuelve la direccion a seguir por el raton para
     * llegar de una a otra.
     *
     * @param init posicion inicial
     * @param target posicion destino
     * @return Movimiento para ir de init a target.
     */
    private int getDirection(Pair<Integer, Integer> init, Pair<Integer, Integer> target) {
        //Si target.second -1(y) es igual a la y del nodo inicial devuelve un movimiento arriba
        if (target.second - 1 == init.second) {
            return Mouse.UP;
        }
        //Si target.second +1(y) es igual a la y del nodo inicial devuelve un movimiento abajo
         if (target.second + 1 == init.second) {
            return Mouse.DOWN;
        }
         //Si target.first -1(x) es igual a la y del nodo inicial devuelve un movimiento derecha
         if (target.first - 1 == init.first) {
            return Mouse.RIGHT;
         //Si target.first -1(x) es igual a la y del nodo inicial devuelve un movimiento izquierda
        } else {
            return Mouse.LEFT;
        }
    }
//        class Arbol {
//            private Nodo raiz;
//
//        public Arbol() {
//            raiz=null;
//        }
//
//        public Nodo getRaiz() {
//            return raiz;
//        }
//
//        public void setRaiz(Nodo raiz) {
//            this.raiz = raiz;
//        }
//        
//            
//        }
//	
//        class Nodo {
//            private Nodo padre;
//            private ArrayList<Nodo> hijos;
//            
//            private int x;
//            private int y;
//
//        public Nodo() {
//            this.padre = null;
//            this.hijos = new ArrayList();
//            this.x = 0;
//            this.y = 0;
//
//        }
//
//        public Nodo(int x, int y) {
//            this.padre = null;
//            this.hijos = new ArrayList();
//            this.x = x;
//            this.y = y;
//        }
//          
//        public int getX() {
//            return x;
//        }
//
//        public void setX(int x) {
//            this.x = x;
//        }
//
//        public int getY() {
//            return y;
//        }
//        public Nodo getPos(){
//            Nodo nodo=new Nodo(x,y);
//            return nodo;
//        }
//        public Boolean up() {
//            Grid grid= new Grid(getX(), getY());
//            if(grid.canGoUp()){
//                return true;
//            }else {
//                return false;
//            }
//        }
//        public Boolean down() {
//            Grid grid= new Grid(getX(), getY());
//            if(grid.canGoDown()){
//                return true;
//            }else {
//                return false;
//            }
//        }
//
//       public Boolean right() {
//            Grid grid= new Grid(getX(), getY());
//            if(grid.canGoRight()){
//                return true;
//            }else {
//                return false;
//            }
//        }
//        public Boolean left() {
//            Grid grid= new Grid(getX(), getY());
//            if(grid.canGoLeft()){
//                return true;
//            }else {
//                return false;
//            }
//        }
//
//
//        public void setY(int y) {
//            this.y = y;
//        }
//
//
//            
//        }
//          public int incrementar(Grid casilla) {
//        int pos = 0;
//        while (pos < visitada.size()) {
//            if (visitada.get(pos).equal(casilla)) {
//                visitada.get(pos).incrementar();
//                return 1;
//            } else {
//                pos++;
//            }
//        }
//        return 1;
//    }
//
//    public int visitar(Grid casilla) {
//        for (int i = 0; i < visitada.size(); i++) {
//            if (visitada.get(i).equal(casilla) == true) {
//                return visitada.get(i).getVisitadas();
//            }
//        }
//        return 999;
//    }
//
//    public boolean buscar(Grid casilla) {
//        for (int i = 0; i < visitada.size(); i++) {
//            if (visitada.get(i).equal(casilla) == true) {
//                return true;
//            }
//        }
//        return false;
//
//    }
//
//    class Visitadas {
//
//        private Grid grid;
//        private int c;
//
//        public Visitadas(Grid grid) {
//            this.grid = grid;
//            c = 1;
//        }
//
//        public void incrementar() {
//            this.c++;
//        }
//
//        public Grid getGrid() {
//            return grid;
//        }
//
//        public int getVisitadas() {
//            return c;
//        }
//
//        public boolean equal(Grid a) {//comprueba si la casilla en la que estamos y la que le pasamos como parámetro, son iguales
//            return (grid.getX() == a.getX() && grid.getY() == a.getY());
//        }
//    }
//    public void busquedaProfundidad(Nodo raton, Grid aaa){
//        List<Nodo> noExploradas=new ArrayList<>();
//        List<Nodo> area= new ArrayList<>();
//        
//        
//        HashMap<Pair<Integer, Integer>, Nodo> anteriores = getAnteriores(raton, queso, noExploradas, area);
//        
//        Nodo nodoQueso;
//        Nodo w;
//        
//        if(buscar(aaa)&& anteriores.containsKey(w))
//        
//    }
//    public ArrayList<Nodo> getAnteriores(Nodo nodoInicial, Visitadas queso, List<Nodo> noExploradas, List<Nodo> area) {
//
//        HashMap<Pair<Integer, Integer>, Nodo> anteriores = new HashMap<>();
//        Queue<Nodo> q =new LinkedList<>();
//         List<Nodo> visitados = new ArrayList<>();
//         
//         q.add(nodoInicial);
//         visitados.add(nodoInicial);
//         
//         while(!q.isEmpty()) {
//             Nodo v= q.poll();
//              if (v.getPos() == queso) {
//              break;  
//              }
//              Nodo w;
//              Nodo noExplorado;
//              Pair<Integer,Integer> posi
//              if(v.up()){
//                  
//              }
//              
//         }
//              
//    }
//    
//    public Integer explora(Grid currentGrid){
//        int x = currentGrid.getX();
//        int y = currentGrid.getY();
//        int xder = x + 1;
//        int xiz = x - 1;
//        int yar = y + 1;
//        int yab = y - 1;
//
//        int menor = 500;
//        int next = 0;
//        Grid nextIZ = new Grid(x, y);
//        Grid nextUP = new Grid(x, y);
//        Grid nextDE = new Grid(x, y);
//        Grid nextDO = new Grid(x, y);
//
//        bomba++;
//        if (bomba == 60) {
//            bomba = 0;
//            return Mouse.BOMB;
//        }
//        if (currentGrid.canGoLeft() == true) {
//            Grid cIz = new Grid(xiz, y);
//            if (buscar(cIz) == false) {
//                Visitadas nueva = new Visitadas(cIz);
//                visitada.add(nueva);
//                this.incExploredGrids();
//                pila.add(Mouse.RIGHT);
//                return Mouse.LEFT;
//            } else {
//                int a = 999;
//                a = visitar(cIz);
//                if (a < menor) {
//                    menor = a;
//                    nextIZ = cIz;
//                    next = 3;
//                }
//
//            }
//        }
//        if (currentGrid.canGoUp() == true) {
//            Grid cUp = new Grid(x, yar);
//            if (buscar(cUp) == false) {
//                Visitadas nueva = new Visitadas(cUp);
//                visitada.add(nueva);
//                this.incExploredGrids();
//                pila.add(Mouse.DOWN);
//                return Mouse.UP;
//            } else {
//                int a = 999;
//                a = visitar(cUp);
//                if (a < menor) {
//                    menor = a;
//                    nextUP = cUp;
//                    next = 1;
//                }
//
//            }
//        }
//        if (currentGrid.canGoRight() == true) {
//            Grid cDer = new Grid(xder, y);
//            if (buscar(cDer) == false) {
//                Visitadas nueva = new Visitadas(cDer);
//                visitada.add(nueva);
//                this.incExploredGrids();
//                pila.add(Mouse.LEFT);
//                return Mouse.RIGHT;
//            } else {
//                int a = 999;
//                a = visitar(cDer);
//                if (a < menor) {
//                    menor = a;
//                    nextDE = cDer;
//                    next = 4;
//                }
//
//            }
//        }
//        if (currentGrid.canGoDown() == true) {
//            Grid cDow = new Grid(x, yab);
//            if (buscar(cDow) == false) {
//                Visitadas nueva = new Visitadas(cDow);
//                visitada.add(nueva);
//                this.incExploredGrids();
//                pila.add(Mouse.UP);
//                return Mouse.DOWN;
//            } else {
//                int a = 999;
//                a = visitar(cDow);
//                if (a < menor) {
//                    menor = a;
//                    nextDO = cDow;
//                    next = 2;
//                }
//
//            }
//        }
//       if(pila.empty()){
//           if (next == 1) {
//               incrementar(nextUP);
//
//               return Mouse.UP;
//           }
//           if (next == 2) {
//               incrementar(nextDO);
//               return Mouse.DOWN;
//           }
//           if (next == 3) {
//               incrementar(nextIZ);
//               return Mouse.LEFT;
//           }
//           if (next == 4) {
//               incrementar(nextDE);
//               return Mouse.RIGHT;
//           }
//       }
//        return pila.pop();
//    }
        
   private class Pair<A, B> {

        public A first;
        public B second;

        public Pair() {
        }

        public Pair(A _first, B _second) {
            first = _first;
            second = _second;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Pair)) {
                return false;
            }
            Pair key = (Pair) o;
            return first == key.first && second == key.second;
        }

        @Override
        public int hashCode() {
            if (first instanceof Integer && second instanceof Integer) {
                Integer result = (Integer) first;
                Integer sec = (Integer) second;
                return result * 1000 + sec;
            }

            return 0;
        }

        @Override
        public String toString() {
            return "X: " + first + " Y: " + second;
        }
    }

    /**
     * Almacena una posicion(x,y) y las direcciones accesibles desde la misma.
     * Esto ultimo, solo sera valido si el nodo esta marcado como explored.
     */
    private class mouseNode {
        public int x;
        public int y;

        public boolean up;
        public boolean down;
        public boolean left;
        public boolean right;
        
        public boolean explored;
        public int distancia;
        //Para cada camino generado, los nodos tendran un peso,
        //basado en el nivel en que se encuentran

        
        public mouseNode(int _x, int _y, boolean _up, boolean _down, boolean _left, boolean _right) {
            x = _x;
            y = _y;

            up = _up;
            down = _down;
            left = _left;
            right = _right;
            explored = true;

        }

        public mouseNode(Pair<Integer, Integer> pos, boolean _up, boolean _down, boolean _left, boolean _right) {
            this(pos.first, pos.second, _up, _down, _left, _right);
        }

        public mouseNode(int _x, int _y) {
            x = _x;
            y = _y;
            explored = false;
        }

        public mouseNode(Pair<Integer, Integer> pos) {
            this(pos.first, pos.second);
        }

        public Pair<Integer, Integer> getPos() {
            return new Pair(x, y);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof mouseNode)) {
                return false;
            }
            mouseNode node = (mouseNode) o;
            return x == node.x && y == node.y;
        }

        @Override
        public int hashCode() {
            return x * 10000 + y;
        }

        @Override
        public String toString() {
            return "X: " + x + " Y: " + y;
        }
    }
	
	
}