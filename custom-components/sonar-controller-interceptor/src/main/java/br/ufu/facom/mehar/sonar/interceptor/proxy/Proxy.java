package br.ufu.facom.mehar.sonar.interceptor.proxy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Proxy implements Runnable {

	private static final Logger logger = LoggerFactory.getLogger(Proxy.class);
	private final Socket in;
	private final Socket out;

	public Proxy(Socket in, Socket out) {
		this.in = in;
		this.out = out;
	}

	@Override
	public void run() {
		logger.info("Proxy {}:{} --> {}:{}", in.getInetAddress().getHostName(), in.getPort(),
				out.getInetAddress().getHostName(), out.getPort());
		try {
			InputStream inputStream = getInputStream();
			OutputStream outputStream = getOutputStream();

			if (inputStream == null || outputStream == null) {
				return;
			}

			byte[] reply = new byte[4096];
			int bytesRead;
			while (-1 != (bytesRead = inputStream.read(reply))) {
				outputStream.write(reply, 0, bytesRead);
			}
		} catch (SocketException ignored) {
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private InputStream getInputStream() {
		try {
			return in.getInputStream();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	private OutputStream getOutputStream() {
		try {
			return out.getOutputStream();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}
}