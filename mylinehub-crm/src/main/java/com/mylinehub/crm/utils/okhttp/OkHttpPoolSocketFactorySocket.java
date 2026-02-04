package com.mylinehub.crm.utils.okhttp;

import javax.net.SocketFactory;
import java.io.IOException;
import java.net.*;


/**
 * @author Anand Goel
 * @version 1.0
 */
public class OkHttpPoolSocketFactorySocket extends SocketFactory {
	
    @Override
    public Socket createSocket() throws IOException {
        int localPort;
        try {
            localPort = OkHttpSourcePortPoolMemoryData.workWithSourcePortPoolMemoryData("acquire",0);
        } catch (InterruptedException e) {
            throw new IOException("Interrupted while acquiring port", e);
        }

        Socket socket = new Socket() {
            @Override
            public void close() throws IOException {
                super.close();
                // Release port after socket closes
                try {
					OkHttpSourcePortPoolMemoryData.workWithSourcePortPoolMemoryData("offer",localPort);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					System.out.println("Exception while releasing okhttp request local source port number : "+localPort);
					e.printStackTrace();
				}
            }
        };

        socket.bind(new InetSocketAddress(localPort));
        return socket;
    }

    
    //Below methods are not used in project where as are kept here considering might be useful for future use.
    // Other overrides delegate to `createSocket()`
    @Override
    public Socket createSocket(String host, int port) throws IOException {
        Socket socket = createSocket();
        socket.connect(new InetSocketAddress(host, port));
        return socket;
    }

    @Override
    public Socket createSocket(InetAddress host, int port) throws IOException {
        return createSocket(host.getHostAddress(), port);
    }

    @Override
    public Socket createSocket(String host, int port, InetAddress localAddr, int localPort) throws IOException {
        Socket socket = new Socket();
        socket.bind(new InetSocketAddress(localAddr, localPort));
        socket.connect(new InetSocketAddress(host, port));
        return socket;
    }

    @Override
    public Socket createSocket(InetAddress addr, int port, InetAddress localAddr, int localPort) throws IOException {
        return createSocket(addr.getHostAddress(), port, localAddr, localPort);
    }
}

