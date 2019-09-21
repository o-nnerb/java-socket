import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Optional;

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

      public byte[] read(Integer buffer) {
            try {
                  if (this.tcp().isPresent()) {
                        InputStream input = this.tcp().get().getInputStream();
                        byte[] readed = input.readNBytes(buffer);
                        input.close();
                        return readed;
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
            try {
                  if (this.udp().isPresent()) {
                        OutputStream out = this.tcp().get().getOutputStream();
                        out.write(bytes);
                        out.close();
                        return;
                  }

                  DatagramPacket packet = new DatagramPacket(bytes, bytes.length);
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

}