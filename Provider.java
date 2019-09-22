import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.util.Optional;

public class Provider {
      final private Object o;
      final private Integer port;

      private Provider(Object o, Integer port) {
            this.o = o;
            this.port = port;
      }

      public static Optional<Provider> tcp(Integer port) {
            try {
                  System.out.println("Aguardando conexão na porta " + port.toString());
                  return Optional.of(new Provider(new ServerSocket(port), port));
            } catch (Exception e) {
                  return Optional.empty();
            }
      }

      public static Optional<Provider> udp(Integer port) {
            try {
                  return Optional.of(new Provider(new DatagramSocket(port), port));
            } catch (Exception e) {
                  return Optional.empty();
            }
      }

      public Optional<ServerSocket> tcp() {
            if (this.o instanceof ServerSocket) {
                  return Optional.of((ServerSocket) this.o);
            }

            return Optional.empty();
      }

      public Optional<DatagramSocket> udp() {
            if (this.o instanceof DatagramSocket) {
                  return Optional.of((DatagramSocket) this.o);
            }

            return Optional.empty();
      }

      public Integer getPort() {
            return this.port;
      }

      public void close() {
            this.tcp().ifPresent(t -> {
                  try { t.close(); } catch (Exception e) { System.out.println(e.getMessage());}
            });
            this.udp().ifPresent(t -> {
                  try { t.close(); } catch (Exception e) { System.out.println(e.getMessage());}
            });
      }
}