package cliente_servidor;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

public class Client extends JFrame { // classe JFRAME de cliente onde todo o chat acontece

	private JPanel contentPane; // jpanel para interface grafica
	private static final long serialVersionUID = 1L;
	private JTextArea taEditor; // area de texto editavel onde envia as mensagens
	private JTextArea taVisor; // area de texto não editavel onde recebe mensagens
	private JList<String> lusuarios = new JList<String>(); // lista de usuarios
	private PrintWriter pw; // escritor
	private BufferedReader br; // leitor
	private Socket cliente;

	/**
	 * Launch the application.
	 */

	/**
	 * Create the frame.
	 */
	public Client() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // sair quando a janela for fechada
		setBounds(100, 100, 450, 300); // definir tamanho da tela
		contentPane = new JPanel(); // criar JPanel
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5)); // setar as bordas
		setContentPane(contentPane); // adicionar no contentPane
		contentPane.setLayout(null); // definir layout

		lusuarios = new JList<String>(); // criar a lista de usuarios
		lusuarios.setBounds(10, 12, 123, 167); // definir posição
		contentPane.add(lusuarios); // adcionar na janela

		JScrollPane scrollList = new JScrollPane(lusuarios); // criar barra de scroll para a lista
		scrollList.setBounds(10, 11, 123, 168);
		contentPane.add(scrollList);

		taVisor = new JTextArea(); // area de texto onde serão visualizadas as mensagens enviadas e recebidas
		taVisor.setEditable(false);
		taVisor.setBounds(149, 11, 275, 168);
		contentPane.add(taVisor);

		JScrollPane scrollVisor = new JScrollPane(taVisor); // scroll da area de texto onde vê as msgs
		scrollVisor.setBounds(149, 11, 275, 168);
		contentPane.add(scrollVisor);

		taEditor = new JTextArea();// area de texto editavel para escrever mensagens a enviar
		taEditor.setBounds(10, 189, 414, 22);
		contentPane.add(taEditor);

		this.setVisible(true); // janela visivel
		this.setResizable(false);// janela não pode mudar de tamaho

	}

	// metodo que recebe a lista de usuários e preenche na lista
	private void preencherUsuarios(String[] usuarios) {
		// TODO Auto-generated method stub
		DefaultListModel<String> modelo = new DefaultListModel<String>();
		lusuarios.setModel(modelo);

		for (String u : usuarios) {
			modelo.addElement(u);
		}
	}

	// metodo usado para enviar mensagem ao apertar Enter
	private void tratarEventos() {
		// TODO Auto-generated method stub
		taEditor.addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void keyReleased(KeyEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void keyPressed(KeyEvent e) { // utilizado key pressed, pois o evento acontece ao apertar tecla ENTER
				// TODO Auto-generated method stub
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {

					if (taEditor.getText().isEmpty()) { // retornar caso texto esteja vazio
						return;
					}

					Object usuario = lusuarios.getSelectedValue(); // receber os usuários
					if (usuario == null) { // usuario vazio

						if (taEditor.getText().equalsIgnoreCase(Comandos.SAIR)) {// sair caso use o comando sair
							System.exit(0);
						} else if (taEditor.getText().startsWith(Comandos.MUDAR_NOME)) { // comando alterar nick
							String nick = taEditor.getText().substring(Comandos.MUDAR_NOME.length());
							if (nick.startsWith("<") && nick.endsWith(">")) { // verificações de sintaxe
								nick = nick.replaceAll("<", "");
								nick = nick.replaceAll(">", "");
								mudarNome(nick); // função para mudar o nick
								e.consume(); // não ler a tecla usada pra ativar o evento
								taEditor.setText(""); // setar o texto vazio
							} else { // erro caso a sintaxe esteja errada
								pw.println(Comandos.NOME_NEGADO + ": SINTAXE ERRADA");
								e.consume();
								taEditor.setText("");
							}

						} else if (taEditor.getText().startsWith(Comandos.FILE)) { // comando FILE não implementado
							String filePath = taEditor.getText().substring(Comandos.FILE.length());
							sendFile(filePath); // função pra implementar envio de arquivos
							e.consume();
							taEditor.setText("");
						} else { // caso seja outra mensagem qualquer (com usuario não escolhido)
							String msg = taEditor.getText();
							sendGlobal(msg); // enviar para todos os clientes
							e.consume();
							taEditor.setText("");
						}

					} else { // usuario esclhido
						if (usuario.equals("MSG")) { // caso o cliente seja MSG (onde o envio é para todos)
							String msg = taEditor.getText();
							sendGlobal(msg);
							e.consume();
							taEditor.setText("");
						} else { // caso seja qualquer outro usuário
							taVisor.append("PRIVATE: para "); // conjunto de strings que vão aparecer na tela de quem
																// enviou a mensagem
							taVisor.append(usuario.toString());
							taVisor.append(" -> ");
							taVisor.append(taEditor.getText());
							taVisor.append("\n");

							pw.println(Comandos.DIRECT + usuario); // comando direct com o nome do usuario para mandar a
																	// mensagem
							pw.println(taEditor.getText()); // printar a msg enviada

							taEditor.setText("");
							e.consume();
						}
					}

				}
			}

			private void sendFile(String filePath) { // metodo enviar arquivos ainda não implementado
				// TODO Auto-generated method stub
				pw.println(Comandos.FILE + filePath);
			}

			private void sendGlobal(String msg) { // metodo enviar mensagem para todos os clientes
				// TODO Auto-generated method stub
				pw.println(Comandos.GLOBAL + msg);

			}

			private void mudarNome(String nick) { // metodo mudar nick
				// TODO Auto-generated method stub
				pw.println(Comandos.MUDAR_NOME + nick);
			}
		});

	}

	// metodo para listar os usuarios
	private void atualizarListaUsuarios() {
		// TODO Auto-generated method stub
		pw.println(Comandos.LISTAR);

	}

	// metodo de leitura do buffer
	private void initLeitor() {
		// TODO Auto-generated method stub
		try {
			while (true) {
				String msg = br.readLine(); // recebe a mensagem enviada pelo cliente ou definida nos metodos
				// JOptionPane.showMessageDialog(null, msg);
				if (msg == null || msg.isEmpty()) { // ignora caso for vazia
					continue;
				}

				if (msg.startsWith(Comandos.LISTAR)) { // caso seja comando listar
					String[] usuarios = br.readLine().split(","); // separar os usuarios por ,
					preencherUsuarios(usuarios); // metodo para preencher a lista de usuarios (feita toda vez que o
													// comando listar é utilizado
				} else if (msg.equals(Comandos.NOME)) { // caso seja comando nome (utilizado automaticamente quando
														// inicia)
					String nome = JOptionPane.showInputDialog("Qual o seu nick?");
					pw.println(nome); // recebe o nome no buffer
				} else if (msg.equals(Comandos.NOME_NEGADO)) { // caso o nome seja negado
					JOptionPane.showMessageDialog(this, "o login é inválido");
				} else if (msg.equals(Comandos.NOME_ACEITO)) { // caso nome seja aceito
					atualizarListaUsuarios(); // listar os usuarios adicionando o novo
				} else if (msg.startsWith(Comandos.MUDAR_NOME)) { // comando mudar nome para trocar o nick do cliente
					atualizarListaUsuarios(); // listar os usuarios alterando o nome
				} else if (msg.startsWith(Comandos.GLOBAL)) { // comando para enviar mensagem para todos os clientes
					taVisor.append(msg); // mensagens aparecidas na tela de quem enviou
					taVisor.append("\n");
					taVisor.setCaretPosition(taVisor.getDocument().getLength());
				} else if (msg.startsWith(Comandos.FILE)) { // comando enviar arquivo (não implementado a partir daqui)
					taVisor.append("envio de arquivo");
				} else { // outra mensagem qualquer (normalmente enviada pelo comando global)
					taVisor.append(msg);
					taVisor.append("\n");
					taVisor.setCaretPosition(taVisor.getDocument().getLength());
				}
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.err.println("a mensagem não pode ser lida"); // mensagem de erro
			e.printStackTrace();

		}
	}

	// metodo que inicia a conexão com o servidor
	public void initChat() {
		try {
			cliente = new Socket("127.0.0.1", 12345);

			pw = new PrintWriter(cliente.getOutputStream(), true); //escritor recebendo o output do cliente e dando autoflush
			br = new BufferedReader(new InputStreamReader(cliente.getInputStream())); //leitor recebendo o input do cliente 

		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.err.println("endereço inválido ou servidor fora do ar"); //mensagem de erro
			e.printStackTrace();
		}
	}

	// metodo main onde são iniciados os outros metodos necessarios para o programa
	public static void main(String[] args) {
		Client cv = new Client(); //instancia novo cliente (JFRAME)
		cv.initChat(); //inicializa a conexão com o servidor e estancia o leitor e escritor
		cv.tratarEventos(); //metodo de tratamento de evento ao apertar tecla ENTER com todos os possiveis comandos no chat
		cv.initLeitor();//ler as mensagens enviadas pelo cliente. adciona na sua propria tela.
	}

}
