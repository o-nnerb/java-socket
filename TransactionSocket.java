import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Optional;

public class TransactionSocket {
      final private Transaction transaction;
      final SocketProvider socket;
      Integer buffer = 0;

      TransactionSocket(Transaction t, SocketProvider p, Integer buffer) {
            this.transaction = t;
            this.socket = p;
            this.buffer = buffer;
      }

      public void close() {
            this.socket.close();
            System.out.println("Connection Closed");
      }

      public void notifyTransaction() {
            this.transaction.socketDidClose();
      }

      public ResultType commit(Comunication comunication) {
            return comunication.buffer(this.buffer).execute(this.socket, false);
      }

      public ResultType accepted() {
            return Comunication.accepted().execute(this.socket, false);
      }

      public ResultType refused() {
            return Comunication.refused().execute(this.socket, false);
      }

      public Integer maxDataSize() {
            return 5242880;
      }
    
      public Integer maxBufferSize() {
          return 2048;
      }

      Optional<FileByte> file = Optional.empty();

      private ResultType _send(String fileName) {
            return Comunication.send(Protocol.receive(fileName)).buffer(this.buffer).onResponse(bufferSync -> {
                  final Optional<Protocol> bufferProtocol = Protocol.fromData(bufferSync);

                  if (!bufferProtocol.isPresent()) {
                        return ResultType.init(Comunication.refused());
                  }

                  if (bufferProtocol.get().code != ResponseCode.accepted) {
                        return ResultType.init(bufferProtocol.get());
                  }
                  
                  return ResultType.init(Comunication
                        .send(Protocol.buffer(this.buffer))
                        .buffer(this.buffer)
                        .onResponse(response -> {
                              final Optional<Protocol> responseProtocol = Protocol.fromData(response);

                              if (!responseProtocol.isPresent()) {
                                    return ResultType.init(Comunication.refused());
                              }

                              if (responseProtocol.get().code != ResponseCode.accepted) {
                                    return ResultType.init(bufferProtocol.get());
                              }
                              
                              if (this.file.get().isEndOfFile()) {
                                    return ResultType.init(Comunication.send(Protocol.checkSum(this.file.get().md5())));
                              }
                              return ResultType.init(false);
                        }));

            }).execute(this.socket, false);
      }

      public ResultType send(String fileName) {
            final Optional<FileByte> file = FileByte.open(fileName, this.buffer, false);
            
            if (!file.isPresent()) {
                  return Comunication.send(Protocol.notFound()).execute(this.socket, false);
            }

            if (file.get().file.length() > this.maxDataSize()) {
                  return Comunication.send(Protocol.fileSizeExceeded()).execute(this.socket, false);
            }

            this.file = file;
            final ResultType result = this._send(fileName);
            this.file.get().close();
            this.file = Optional.empty();
            return result; 
      }

      private ResultType _receive(final Integer bufferSize) {
            return Comunication.accepted()
                  .onResponse(new FileRecive(this, bufferSize))
                  .buffer(bufferSize)
                  .recursive(this.socket);
      }

      public ResultType receive(String fileName) {
            return Comunication.send(Protocol.send(fileName)).onResponse(requestResult -> {
                  Optional<Protocol> requestProtocol = Protocol.fromData(requestResult);

                  if (!requestProtocol.isPresent()) {
                        return ResultType.init(Comunication.refused());
                  }

                  if (requestProtocol.get().code != ResponseCode.receive) {
                        return ResultType.init(requestProtocol.get());
                  }
                  
                  return Comunication
                        .accepted()
                        .onResponse(bufferResult -> {
                              Optional<Protocol> bufferProtocol = Protocol.fromData(bufferResult);

                              if (!bufferProtocol.isPresent()) {
                                    return ResultType.init(Comunication.refused());
                              }

                              if (bufferProtocol.get().code != ResponseCode.buffer) {
                                    return ResultType.init(bufferProtocol.get());
                              }

                              final Integer buffer = Integer.parseInt(new String(bufferProtocol.get().data.bytes));
                              if (buffer > this.maxBufferSize()) {
                                    return ResultType.init(Comunication.refused());
                              }

                              this.file = FileByte.open(fileName, buffer, true);
                              ResultType result = this._receive(buffer);
                              this.file.get().saveAndClose();
                              this.file = Optional.empty();
                              return result;
                        }).buffer(this.buffer)
                        .execute(this.socket, false);

            }).buffer(this.buffer)
            .recursive(this.socket);
      }

      public ResultType clientSend(String fileName) {
            return Comunication
                  .send(Protocol.receive(fileName))
                  .onResponse(new ClientFileRecive(this, this.buffer, fileName))
                  .buffer(this.buffer)
                  .execute(this.socket, false);
      }

      public ResultType waitForResponse() {
            return Comunication.empty()  
                  .onResponse(result -> {
                        Optional<Protocol> protocol = Protocol.fromData(Data.init(result));

                        if (!protocol.isPresent()) {
                              return ResultType.init(false);
                        }

                        return ResultType.init(protocol.get());
                  }).buffer(this.buffer)
                  .execute(this.socket, false);
      }
}

class Receive implements ComunicationResult {
      final TransactionSocket transaction;
      final Integer buffer;

      Receive(TransactionSocket transaction, Integer bufferSize) {
            this.transaction = transaction;
            this.buffer = bufferSize;
      }

      public ResultType result(byte[] response) {
            return ResultType.init(false);
      }
}

class ClientFileRecive extends Receive {
      final String fileName;
      
      ClientFileRecive(TransactionSocket transaction, Integer bufferSize, String fileName) {
            super(transaction, bufferSize);
            this.fileName = fileName;
      }

      @Override
      public ResultType result(byte[] response) {
            Optional<Protocol> protocol = Protocol.fromData(response);
                  
            if (!protocol.isPresent()) {
                  return ResultType.init(Comunication.refused());
            }

            if (protocol.get().code != ResponseCode.send) {
                  return ResultType.init(protocol.get());
            }

            return Comunication
                  .send(Protocol.receive(this.fileName))
                  .onResponse(this)
                  .buffer(this.buffer)
                  .execute(this.transaction.socket, false);
      }
}

class FileRecive extends Receive {

      FileRecive(TransactionSocket transaction, Integer bufferSize) {
            super(transaction, bufferSize);
      }

      @Override
      public ResultType result(byte[] response) {
            Optional<Protocol> protocol = Protocol.fromData(response);

            if (!protocol.isPresent()) {
                  return ResultType.init(Comunication.refused());
            }

            if (protocol.get().code == ResponseCode.fragment) {
                  if (this.transaction.file.get().dataCount > this.transaction.maxDataSize()) {
                        return ResultType.init(Comunication.send(Protocol.fileSizeExceeded()));
                  }
                  
                  ArrayList<Byte> bytes = new ArrayList<>();
                  for(byte b: protocol.get().data.bytes) {
                        bytes.add(b);
                  }

                  this.transaction.file.get().append(bytes, 1);
                  return ResultType.init(Comunication.accepted()
                        .buffer(this.buffer)
                        .onResponse(this)
                  );
            }
            
            if (protocol.get().code != ResponseCode.checkSum) {
                  return ResultType.init(protocol.get());
            }

            this.transaction.file.get().saveAll();

            if (!(new String(protocol.get().data.bytes)).equals(this.transaction.file.get().md5())) {
                  return ResultType.init(Comunication
                        .send(Protocol.bytesError())
                        .buffer(this.transaction.buffer)
                  );
            }

            return ResultType.init(Comunication
                  .accepted()
                  .buffer(this.transaction.buffer)
            );
      }
}