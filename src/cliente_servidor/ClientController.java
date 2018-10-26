package cliente_servidor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

//thread de liga��o entre cliente e servidor
public class ClientController extends Thread {
	private Socket cliente; // socket que � recebido quando estanciado
	private String nomeCliente; // nome do usuario conectado
	private BufferedReader bfr; // buffer para leitura do que for escrito pelo usuario
	private PrintWriter pw; // writer para escrita do que foi digitado pelo usuario
	private static final Map<String, ClientController> clientes = new HashMap<String, ClientController>(); // hashmap
																											// para
																											// salvar as
																											// varias
																											// estancias
																											// do
																											// cliente

	public ClientController(Socket cliente) { // metodo construtor que recebe o cliente e starta a thread
		// TODO Auto-generated constructor stub
		this.cliente = cliente;
		start();
	}

	@Override
	public void run() {
		try {
			// estanciando reader e writer
			bfr = new BufferedReader(new InputStreamReader(this.cliente.getInputStream()));
			pw = new PrintWriter(this.cliente.getOutputStream(), true);
			// fileReader = new FileReader("c:desktop\teste.txt");

			logar(); // metodo de verifica��o de nome

			// enviar mensagem para a pessoa certa e comando sair
			String msg;
			while (true) {
				msg = bfr.readLine(); // l� os dados enviados pelo cliente
				if (msg.equalsIgnoreCase(Comandos.SAIR)) { // comando sair para fechar a conex�o
					this.cliente.close();
				} else if (msg.startsWith(Comandos.FILE)) { // comando file para enviar arquivo (n�o implementado)
					msg = msg.substring(Comandos.FILE.length()).trim();
					getPw().println("teste");
				} else if (msg.startsWith(Comandos.GLOBAL)) { // comando para enviar mensagem para todos os clientes
					msg = msg.substring(Comandos.GLOBAL.length());
					for (String c : clientes.keySet()) { // percorre os usuarios e envia a mensagem para cada um deles
						clientes.get(c).getPw().println("MSG: de " + this.getNomeCliente() + " -> " + msg);
					}
				} else if (msg.startsWith(Comandos.MUDAR_NOME)) { // comando de mudar nome/Mudar nick
					mudarNome(msg);

				} else if (msg.startsWith(Comandos.DIRECT)) { // comando direct para mandar mensagem para alguem
					String nomeDestino = msg.substring(Comandos.DIRECT.length(), msg.length()); // nome destino � o nome
																								// para quem sera
																								// enviado
					System.out.println("Enviando mensagem para " + nomeDestino);
					ClientController destino = clientes.get(nomeDestino); // destino � o cliente que a mensagem ser�
																			// enviada

					// caso o cliente n�o seja encontrado
					if (destino == null) {
						continue;

					} else { // caso contrario envia a mensagem para o destino correto
						destino.getPw().println("PRIVATE: " + this.nomeCliente + " -> " + bfr.readLine());
					}
				} else if (msg.equals(Comandos.LISTAR)) { // atualizar lista de usu�rios ao receber o comando listar
					atualizarListaUsuarios(this);
				} else {
					pw.println("MSG: " + this.getNomeCliente() + " -> " + msg); // enviar uma mensagem qualquer, sem
																				// nenhum comando
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			clientes.remove(this.nomeCliente); // caso a conex�o seja encerrada remover o cliente da lista
			atualizarListaUsuarios(this); // atualizar a lista para os outros clientes deixar de ver quem foi removido
			System.err.println("Conex�o encerrada");
		}
	}

	private void mudarNome(String msg) { // metodo de mudar de nick j� foi feita verifica��es de sintaxe
		// TODO Auto-generated method stub
		clientes.remove(this.nomeCliente); // remove o cliente exitente para depois adcionar um novo com novo nome
		this.nomeCliente = msg.substring(Comandos.MUDAR_NOME.length()).toLowerCase().replaceAll(",", "").trim(); // muda
																													// o
																													// nome
																													// do
																													// cliente
																													// para
																													// o
																													// recebido,
																													// sem
																													// virgulas
																													// e
																													// espa�o

		if (this.nomeCliente.equalsIgnoreCase("null") || this.nomeCliente.isEmpty()) { // caso nome seja nulo ou vazio
			pw.println(Comandos.NOME_NEGADO);
		} else if (clientes.containsKey(this.nomeCliente)) { // caso nome j� exista
			pw.println(Comandos.NOME_NEGADO);
		} else {
			pw.println(Comandos.NOME_ACEITO); // nome aceito
			pw.println("nome alterado para " + this.nomeCliente); // mensagem de confirma��o de altera��o de nome
			clientes.put(this.nomeCliente, this); // adic��o do cliente com o novo nome

			for (String c : clientes.keySet()) { // atualizar lista de usu�rios com o novo nome para todos os clientes
				atualizarListaUsuarios(clientes.get(c));
			}
		}
	}

	// metodo logar
	private void logar() throws IOException {
		// TODO Auto-generated method stub
		// clientes.put("GERAL", new ClientController());
		while (true) {
			pw.println(Comandos.NOME);
			this.nomeCliente = this.bfr.readLine().toLowerCase().replaceAll(",", "").trim(); // deixa o nome minusculo e
																								// retira todas as
																								// virgulas

			if (this.nomeCliente.equalsIgnoreCase("null") || this.nomeCliente.isEmpty()) { // negar conex�o caso seja
																							// nulo ou vazio
				pw.println(Comandos.NOME_NEGADO);
			} else if (clientes.containsKey(this.nomeCliente)) { // negar conex�o caso nome j� exista
				pw.println(Comandos.NOME_NEGADO);
			} else {
				pw.println(Comandos.NOME_ACEITO); // aceitar conex�o
				pw.println("ol� " + this.nomeCliente); // mensagem de boa vinda do servidor

				clientes.put(this.nomeCliente, this); // adicionar cliente na lista

				for (String c : clientes.keySet()) { // atualizar lista de usu�rios
					atualizarListaUsuarios(clientes.get(c));
				}
				break; // finalizar o while para seguir com o c�digo
			}
		}

	}

	// fun��o para atualizar a lista de usu�rios
	private void atualizarListaUsuarios(ClientController clientController) {
		// TODO Auto-generated method stub
		StringBuffer str = new StringBuffer(); // classe para montar string(a lista de usuarios � armazenada em uma
												// unica string com os nomes separados por ,

		for (String c : clientes.keySet()) {
			if (clientController.getNomeCliente().equals(c)) { // n�o colocar na lista o proprio nome
				str.append("MSG"); //adcionar na lista um campo msg, para enviar mensagens no chat geral
				str.append(","); //separador que define onde come�a e termina um nome
				continue;

			}
			str.append(c); // adicionar nome na String
			str.append(","); //adicionar separador
		}
		if (str.length() > 0) {
			str.delete(str.length() - 1, str.length()); // deletar virgula final
		}
		clientController.getPw().println(Comandos.LISTAR); //comando listar para pegar a lista
		clientController.getPw().println(str.toString()); //printar a lista de nomes
	}

	// metodos get()
	public PrintWriter getPw() {
		return pw;
	}

	public String getNomeCliente() {
		return nomeCliente;
	}
}
