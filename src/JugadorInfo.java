import java.io.Serializable;

public class JugadorInfo implements Serializable {
	private String nickname;
	private String hostIp;
	private int puerto;
	private boolean esAnfitrion;

	public JugadorInfo(String nickname, String hostIp, int puerto) {
		this.nickname = nickname;
		this.hostIp = hostIp;
		this.puerto = puerto;
		this.esAnfitrion = false; // Se decide luego
	}

	// Getters y Setters tipados correctamente
	public boolean isEsAnfitrion() { return esAnfitrion; }
	public void setEsAnfitrion(boolean esAnfitrion) { this.esAnfitrion = esAnfitrion; }
	public int getPuerto() { return puerto; }
	public String getHostIp() { return hostIp; }
	public String getNickname() { return nickname; }

	@Override
	public String toString() {
		return nickname + ";" + hostIp + ";" + puerto + ";" + esAnfitrion;
	}
}
