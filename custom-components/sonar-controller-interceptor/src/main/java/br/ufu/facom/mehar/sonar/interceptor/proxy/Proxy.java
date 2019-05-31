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
	
	private final Direction direction;
	private final Interceptor interceptor;
	
	//private static final int BUFFER_SIZE=4096;
	private static final int BUFFER_SIZE=8192;

	public Proxy(Direction direction, Interceptor interceptor, Socket in, Socket out) {
		this.direction = direction;
		this.interceptor = interceptor;
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

			byte[] reply = new byte[BUFFER_SIZE];
			int bytesRead;
			while (-1 != (bytesRead = inputStream.read(reply))) {
				if(interceptor != null) {
					interceptor.intercept(direction, in.getInetAddress(), out.getInetAddress(), reply, bytesRead);
				}
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