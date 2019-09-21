import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Optional;

public class Transaction {
      final Provider provider;
      Integer buffer = 1024;

      Transaction(Provider provider) {
            this.provider = provider;
            this.buffer = 1024;
      }

      Transaction(Provider provider, Integer buffer) {
            this.provider = provider;
            this.buffer = buffer;
      }

      public static Optional<Transaction> connect(ProviderType providerType, Integer port) {
            Optional<Provider> provider = providerType == ProviderType.udp ?
                  Provider.udp(port) :
                  Provider.tcp(port);

            if (provider.isPresent()) {
                  return Optional.of(new Transaction(provider.get()));
            }

            return Optional.empty();
      }

      public Optional<TransactionSocket> accept() {
            Optional<SocketProvider> socket = SocketProvider.accept(this.provider);
            if (socket.isPresent()) {
                  return Optional.of(new TransactionSocket(this, socket.get(), this.buffer));
            } 

            return Optional.empty();
      }

      public void close() {
            this.provider.close();
            System.out.println("Server Connection Closed");
      }
}