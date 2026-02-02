import java.io.*;
import java.net.*;
import java.util.Random;

public class ClienteJugador implements Runnable {
	private String nickname;
	private int miPuertoP2P; // Puerto int para recibir conexiones si soy host

	public ClienteJugador(String nickname, int puertoP2P) {
		this.nickname = nickname;
		this.miPuertoP2P = puertoP2P;
	}

	@Override
	public void run() {
		try {
			// 1. CONEXIÓN AL LOBBY
			Socket socketLobby = new Socket("localhost", 6000);
			PrintWriter outLobby = new PrintWriter(socketLobby.getOutputStream(), true);
			BufferedReader inLobby = new BufferedReader(new InputStreamReader(socketLobby.getInputStream()));

			// Enviar petición: NUEVO_JUEGO;TIPO;NICK;HOST;PUERTO
			System.out.println(nickname + " conectando al lobby...");
			outLobby.println("NUEVO_JUEGO;DADOS;" + nickname + ";localhost;" + miPuertoP2P);

			// Esperar respuesta (Bloqueante hasta que haya 2 jugadores)
			String respuesta = inLobby.readLine();
			socketLobby.close(); // Ya tenemos los datos, cerramos con Lobby temporalmente

			// Parsear respuesta: INICIO_PARTIDA;ID;JUGADORES(#);SOY_HOST
			String[] partes = respuesta.split(";");
			String idPartida = partes[1];
			String listaJugadoresRaw = partes[2]; // nick,ip,port,host#nick,ip,port,host...
			boolean soyAnfitrion = Boolean.parseBoolean(partes[3]);

			JugadorInfo rival = obtenerRival(listaJugadoresRaw, this.nickname);

			System.out.println(nickname + " comienza. ¿Soy Host?: " + soyAnfitrion + ". Rival: " + rival.getNickname());

			// 2. FASE DE JUEGO (P2P)
			if (soyAnfitrion) {
				jugarComoAnfitrion(idPartida);
			} else {
				jugarComoInvitado(rival);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private JugadorInfo obtenerRival(String raw, String miNick) {
		String[] jugadores = raw.split("#");
		for (String j : jugadores) {
			String[] datos = j.split(",");
			// datos: 0=nick, 1=ip, 2=port, 3=esHost
			if (!datos[0].equals(miNick)) {
				return new JugadorInfo(datos[0], datos[1], Integer.parseInt(datos[2]));
			}
		}
		return null;
	}

	// --- LÓGICA ANFITRION ---
	private void jugarComoAnfitrion(String idPartida) {
		try (ServerSocket serverGame = new ServerSocket(miPuertoP2P)) {
			// Esperar al invitado
			Socket socketInvitado = serverGame.accept();
			BufferedReader in = new BufferedReader(new InputStreamReader(socketInvitado.getInputStream()));
			PrintWriter out = new PrintWriter(socketInvitado.getOutputStream(), true);
			Random rand = new Random();

			boolean juegoTerminado = false;
			while (!juegoTerminado) {
				// 1. Esperar dado del invitado
				int dadoInvitado = Integer.parseInt(in.readLine());

				// 2. Tirar mi dado
				int dadoHost = rand.nextInt(6) + 1;
				System.out.println(nickname + " (Host) tiró: " + dadoHost + " vs Invitado: " + dadoInvitado);

				// 3. Calcular resultado
				String resultado;
				if (dadoHost > dadoInvitado) {
					resultado = "V"; // Victoria Host
					juegoTerminado = true;
				} else if (dadoHost < dadoInvitado) {
					resultado = "D"; // Derrota Host
					juegoTerminado = true;
				} else {
					resultado = "E"; // Empate
				}
				// 4. Enviar resultado
				out.println(resultado);
				out.flush(); // Importante forzar el envío
			}
			// Cerrar conexión P2P
			socketInvitado.close();
			// 5. NOTIFICAR AL LOBBY EL FIN
			notificarFinPartida(idPartida);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	// --- LÓGICA INVITADO ---
	private void jugarComoInvitado(JugadorInfo anfitrion) {
		// Pequeña pausa para asegurar que el host levantó el server
		try { Thread.sleep(500); } catch (InterruptedException e) {}

		try (Socket socketHost = new Socket(anfitrion.getHostIp(), anfitrion.getPuerto())) {
			PrintWriter out = new PrintWriter(socketHost.getOutputStream(), true);
			BufferedReader in = new BufferedReader(new InputStreamReader(socketHost.getInputStream()));
			Random rand = new Random();

			boolean juegoTerminado = false;
			while (!juegoTerminado) {
				// 1. Tirar dado y enviar
				int miDado = rand.nextInt(6) + 1;
				System.out.println(nickname + " (Invitado) tiró: " + miDado);
				out.println(miDado);

				// 2. Recibir resultado
				String resultado = in.readLine();
				if (resultado.equals("V")) {
					System.out.println(nickname + ": Perdí contra el anfitrión.");
					juegoTerminado = true;
				} else if (resultado.equals("D")) {
					System.out.println(nickname + ": ¡Gané al anfitrión!");
					juegoTerminado = true;
				} else {
					System.out.println(nickname + ": Empate. Tirando de nuevo...");
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void notificarFinPartida(String idPartida) {
		try (Socket s = new Socket("localhost", 6000);
		     PrintWriter pw = new PrintWriter(s.getOutputStream(), true)) {
			pw.println("FINALIZAR;" + idPartida);
			System.out.println(nickname + ": Notificado fin de partida " + idPartida);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
