import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;
import java.util.Optional;
import java.lang.Math;

public class SocketProvider {
      final private Object o;

      private SocketProvider(Object o) {
            this.o = o;
      }

      public static Optional<SocketProvider> accept(Provider provider) {
            try {
                  if (provider.tcp().isPresent()) {
                        return Optional.of(new SocketProvider(provider.tcp().get().accept()));
                  }

                  if (provider.udp().get().isClosed()) {
                        return Optional.of(new SocketProvider(new DatagramSocket(provider.getPort())));
                  }

                  return Optional.of(new SocketProvider(provider.udp().get()));
            } catch (Exception e) {
                  System.out.println(e.getMessage());
                  return Optional.empty();
            }
      }

      private Optional<Socket> tcp() {
            if (this.o instanceof Socket) {
                  return Optional.of((Socket) this.o);
            }

            return Optional.empty();
      }

      private Optional<DatagramSocket> udp() {
            if (this.o instanceof DatagramSocket) {
                  return Optional.of((DatagramSocket) this.o);
            }

            return Optional.empty();
      }

      private byte[] tcpRead(InputStream input, Integer buffer) throws Exception {
            Integer size = 0;
            Integer scale = 0;
            byte[] b = new byte[1];
            
            while (input.read(b) > 0) {
                  if (b[0] == "<".getBytes()[0]) {
                        final Integer length = size-1 >= buffer ? buffer : size-1;
                        byte[] readed = input.readNBytes(length);
                        // input.close();
                        return Data.init(b).merge(Data.init(readed)).bytes;
                  } else {
                        size = (int) ((size) * Math.pow(10.0, (double) scale)) + Integer.parseInt(new String(b));
                        scale += 1;
                  }
            }

            // input.close();
            return new byte[0];
      }

      public byte[] read(Integer buffer) {
            try {
                  if (this.tcp().isPresent()) {
                        return this.tcpRead(this.tcp().get().getInputStream(), buffer);
                  }

                  byte[] readed = new byte[buffer];
                  DatagramPacket packet = new DatagramPacket(readed, 
                        buffer);
                  this.udp().get().receive(packet);
                  return readed;
            } catch (Exception e) {
                  System.out.println(e.getMessage());
                  return new byte[0];
            }
      }

      public void write(byte[] bytes) {
            final Data send = Data.init(((Integer) (bytes.length)).toString().getBytes()).merge(Data.init(bytes));
            try {
                  if (!this.udp().isPresent()) {
                        OutputStream out = this.tcp().get().getOutputStream();
                        out.write(send.bytes);
                        // out.close();
                        return;
                  }

                  DatagramPacket packet = new DatagramPacket(send.bytes, send.bytes.length);
                  this.udp().get().send(packet);
                  return;
            } catch (Exception e) {
                  System.out.println(e.getMessage());
                  return;
            }
      }

      public void close() {
            this.tcp().ifPresent(socket -> {
                  try {
                        socket.close();
                  } catch (Exception e) {
                        System.out.println(e.getMessage());
                  }
            });
            
            this.udp().ifPresent(socket -> {
                  try {
                        socket.close();
                  } catch (Exception e) {
                        System.out.println(e.getMessage());
                  }
            });
      }

      public Boolean isOpen() {
            if (this.tcp().isPresent()) {
                  return this.tcp().get().isConnected();
            }

            return this.udp().get().isConnected();
      }

      public String ip() {
            if (this.tcp().isPresent()) {
                  return this.tcp().get().getInetAddress().getHostAddress();
            }
            
            return this.udp().get().getInetAddress().getHostAddress();
      }

      public Integer port() {
            if (this.tcp().isPresent()) {
                  return this.tcp().get().getPort();
            }
            
            return this.udp().get().getPort();
      }
}