package cliente_servidor;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

//classe que cria conexão
public class Servidor {

	// metodo main que cria um ServerSocket em uma porta especifica e espera a
	// conexão do cliente
	// também fecha a conexão com o cliente quando finalizada
	public static void main(String[] args) {
		ServerSocket server = null;
		try {
			server = new ServerSocket(12345); //iniciar servidor na porta escolhida
			System.out.println("Servidor iniciado na porta 12345");

			while (true) { //sempre ficar ouvindo quando um novo cliente conectar
				Socket cliente = server.accept(); //cliente conectado
				new ClientController(cliente); //iniciar thread do cliente para ficar ouvindo todas as alterações e mensagens enviadas


			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.err.println("A porta está ocupada"); //mensagem de erro
			try {
				if (!server.equals(null))
					server.close(); //fechar servidor caso haja conexão
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			e.printStackTrace();
		}
	}
}
