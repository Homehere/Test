package chat;


import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;

import javax.management.*;
import javax.swing.Timer;

//import javax.swing.Timer;
//import javax.management.timer.Timer;
//
import java.awt.event.*;
import Icq.Server;





public class ClientThread extends Thread {

	private final static int DELAY = 30000;

	private Socket socket;
	private Message c;
	private String login;
	private int inPacks = 0;
	private int outPacks = 0;
	private boolean flag = false;
	private Timer timer;
	//private getUse;

	public ClientThread(Socket socket) {
		this.socket = socket;
		this.start();
	}

	public void run() {
		   try {
		      //Создаем потоки ввода-вывода для работы с сокетом
		      final ObjectInputStream inputStream   = new ObjectInputStream(this.socket.getInputStream());
		      final ObjectOutputStream outputStream = new ObjectOutputStream(this.socket.getOutputStream());
		      
		      //Читаем Message из потока 
		      this.c =(Message) inputStream.readObject();

		      //Читаем логин отправителя
		      this.login = this.c.getLogin();

		      //Что же нам прислали?
		      if (! this.c.getMessage().equals(Config.HELLO_MESSAGE)) { //Если это не регистрационное сообщение
		         System.out.println("[" + this.c.getLogin() + "]: " + this.c.getMessage());
		         getChatHistory().addMessage(this.c); //То добавляем его к истории чата
		      } else { 
		         outputStream.writeObject(getChatHistory()); //Иначе, отправляем новичку историю чата
		         this.broadcast(getUserList().getClientsList(), new Message("Server-Bot", "The user " + login + " has been connect")); //И сообщаем всем клиентам, что подключился новый пользователь
		      }
		      //Добавляем к списку пользователей - нового
		      getUserList().addUser(login, socket, outputStream, inputStream);

		      //Для ответа, указываем список доступных пользователей
		      this.c.setOnlineUsers(getUserList().getUsers());

		      //Передаем всем сообщение пользователя
		      this.broadcast(getUserList().getClientsList(), this.c); 

		      //Запускаем таймер
		      this.timer = new Timer (DELAY, new ActionListener(){
		     
		         public void actionPerformed(ActionEvent e) {
			try { //Если количество входящих пакетов от клиента рано исходящему, значит клиент еще не в ауте
			   if (inPacks == outPacks) {
			      outputStream.writeObject(new Ping());
			      outPacks++;
			      System.out.println(outPacks + " out");
			   } else { //Иначе, в ауте
			      throw new SocketException();
			   }
			} catch (SocketException ex1) {
			      System.out.println("packages not clash");
			      System.out.println(login + " disconnected!");
		                    //Удаляем клиента из списка доступных и информируем всех
			      getUserList().deleteUser(login);
		                    broadcast(getUserList().getClientsList(), new Message("Server-Bot", "The user " + login + " has been disconnect", getUserList().getUsers()));
			      flag = true;
		                    timer.stop();
			}  catch (IOException ex2) {
		                        ex2.printStackTrace();
		              }
		           }
		      });

		     this.timer.start();

		     //Начинаем пинговать клиента
		     outputStream.writeObject(new Ping());
		     this.outPacks++;
		     System.out.println(outPacks + " out");

		     //А теперь нам остается только ждать от него сообщений
		     while (true) {
		         //Как только пинг пропал - заканчиваем
		         if(this.flag) {
		            this.flag = false;
		            break;
		         }
		         //Принимаем сообщение
		         this.c = (Message) inputStream.readObject();
		         
		         //Если это ping
		         if (this.c instanceof Ping) {
		                    this.inPacks++;
		                    System.out.println(this.inPacks + " in");
		         } else if (! c.getMessage().equals(Config.HELLO_MESSAGE)) {
			      System.out.println("[" + login + "]: " + c.getMessage());
			      getChatHistory().addMessage(this.c);
		         } else {
		                    outputStream.writeObject(getChatHistory());
			      this.broadcast(getUserList().getClientsList(), new Message("Server-Bot", "The user " + login + " has been connect"));
		          }

		          this.c.setOnlineUsers(getUserList().getUsers());

		          if (! (c instanceof Ping) && ! c.getMessage().equals(Config.HELLO_MESSAGE)) {
			System.out.println("Send broadcast Message: "" + c.getMessage() + """);
			this.broadcast(getUserList().getClientsList(), this.c);
		          }
		     }

		     } catch (SocketException e) {
		         System.out.println(login + " disconnected!");
		         this.broadcast(getUserList().getClientsList(), new Message("Server-Bot", "The user " + login + " has been disconnect", getUserList().getUsers()));
		         this.timer.stop();
		      } catch (IOException e) {
		         e.printStackTrace();
		      } catch (ClassNotFoundException e) {
		         e.printStackTrace();
		      }

	}
	private void broadcast(ArrayList<Client> clientsArrayList, Message message) {
		try {
			for (Client client : clientsArrayList) {
				client.getThisObjectOutputStream().writeObject(message);
			}
		} catch (SocketException e) {
			System.out.println("in broadcast: " + login + " disconnected!");
			getUserList().deleteUser(login);
			this.broadcast(getUserList().getClientsList(), new Message("Server-Bot", "The user " + login + " has been disconnected", getUserList().getUsers()));

			timer.stop();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}

