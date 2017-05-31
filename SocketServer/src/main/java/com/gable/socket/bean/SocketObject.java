package com.gable.socket.bean;

import java.net.Socket;

public class SocketObject {
	private Socket socket;
	private String name;
	public Socket getSocket() {
		return socket;
	}
	public void setSocket(Socket socket) {
		this.socket = socket;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public SocketObject(Socket socket,String name) {
		this.socket = socket;
		this.name = name;
	}
}
