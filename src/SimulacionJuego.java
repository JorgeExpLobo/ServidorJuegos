public class SimulacionJuego {
	public static void main(String[] args) {
		// Lanzamos 10 hilos (5 partidas de 2 personas)
		for (int i = 1; i <= 10; i++) {
			// Asignamos puertos distintos para evitar colisión en localhost
			// Jugador 1 puerto 7001, Jugador 2 puerto 7002, etc.
			int puertoP2P = 7000 + i;
			String nick = "Jugador" + i;

			Thread t = new Thread(new ClienteJugador(nick, puertoP2P));
			t.start();

			// Pequeña pausa para que no entren todos en el milisegundo exacto y ver el log ordenado
			try { Thread.sleep(200); } catch (InterruptedException e) {}
		}
	}
}
