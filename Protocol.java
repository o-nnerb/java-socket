import java.util.ArrayList;
import java.util.Optional;

public class Protocol {
    final ResponseCode code;
    final Data data;
    final ContentType contentType;

    public Protocol(ResponseCode code, String message) {
        this.code = code;
        this.data = Data.init(message.getBytes());
        this.contentType = ContentType.string;
    }

    public Protocol(ResponseCode code, Data data) {
        this.code = code;
        this.data = data;
        this.contentType = ContentType.data;
    }

    public static Protocol error(String message) {
        return new Protocol(ResponseCode.error, message);
    }

    public static Protocol notFound() {
        return new Protocol(ResponseCode.notFound, "Not Found");
    }

    public static Protocol cutConnection() {
        return new Protocol(ResponseCode.cutConnection, "Cut Connection");
    }

    public static Protocol alive() {
        return new Protocol(ResponseCode.alive, "Alive");
    }

    public static Protocol data(byte[] data) {
        return new Protocol(ResponseCode.data, Data.init(data));
    }

    public static Protocol buffer(Integer bufferSize) {
        return new Protocol(ResponseCode.buffer, bufferSize.toString());
    }

    public static Protocol send(String fileName) {
        return new Protocol(ResponseCode.send, fileName);
    }

    public static Protocol receive(String fileName) {
        return new Protocol(ResponseCode.receive, fileName);
    }

    public static Protocol checkSum(String checkSum) {
        return new Protocol(ResponseCode.checkSum, checkSum);
    }

    public static Protocol fragment(Integer buffer, Optional<FileByte> file) {
        if (!file.isPresent()) {
            return new Protocol(ResponseCode.fragment, Data.empty());
        }

        return new Protocol(ResponseCode.fragment, Data.init(Protocol.fragmentSlice(file.get()
            .next(1, buffer - Protocol.fragment(0, Optional.empty())
                .asRaw()
                .size()
            )
        )));
    }

    private static byte[] fragmentSlice(Optional<ArrayList<Byte>> slice) {
        if (!slice.isPresent() || slice.get().isEmpty()) {
            return new byte[0];
        }

        ArrayList<Byte> array = slice.get();

        byte[] one = new byte[1];
        one[0] = array.remove(0);
        byte[] returned = Protocol.fragmentSlice(Optional.of(array));

        byte[] result = new byte[one.length + returned.length];

        System.arraycopy(one, 0, result, 0, one.length);
        System.arraycopy(returned, 0, result, one.length, returned.length);

        return result;
    }

    public static Protocol fileSizeExceeded() {
        return new Protocol(ResponseCode.fileSizeExceeded, "File Size Exceeded");
    }

    public static Protocol bytesError() {
        return new Protocol(ResponseCode.bytesError, "Bytes Errors");
    }

    public static Protocol accepted() {
        return new Protocol(ResponseCode.accepted, "Accepted");
    }

    public static Protocol refused() {
        return new Protocol(ResponseCode.refused, "Refused");
    }

    public static Protocol notAllowed() {
        return new Protocol(ResponseCode.notAllowed, "Not Allowed");
    }

    public Data asRaw() {
        return Data.init(("<Protocol-" + this.code.rawValue + "-" + this.contentType.rawValue.toString() + "-").getBytes())
            .merge(this.data)
            .merge(Data.init("->".getBytes()));
    }

    public static Integer byteSize(byte[] data) {
        return data.length;
    }

    // public static byte[] toBinary(String data) {
    //     return data.getBytes();
    // }

    private static ArrayList<Data> parameters(Data data, Integer max) {
        ArrayList<Data> parameters = new ArrayList<>();
        ArrayList<Byte> bytes = new ArrayList<>();
        Integer i = 0;

        for(; i < data.size(); i++) {
            if (data.bytes[i] == "-".getBytes()[0]) {
                parameters.add(Data.init(bytes));
                bytes.clear();
                if (parameters.size() >= max) {
                    i += 1;
                    break;
                }
            } else {
                bytes.add(data.bytes[i]);
            }
        }

        if (i < data.size()) {
            for(Integer j = i; j < data.size(); j++) {
                bytes.add(data.bytes[j]);
            }

            parameters.add(Data.init(bytes));
            bytes.clear();
        }

        return parameters;
    }

    public static Optional<Protocol> fromData(Data data) {
        if (!data.startsWith("<Protocol-")) {
            return Optional.empty();
        }

        if (!data.endsWith("->")) {
            return Optional.empty();
        }

        ArrayList<Data> params = parameters(data.slice("<Protocol-".length(), data.size() - "->".length()).get(), 2);

        Optional<ResponseCode> code = ResponseCode.init(Integer.parseInt(new String(params.get(0).bytes)));
        Optional<ContentType> contentType = ContentType.init(Integer.parseInt(new String(params.get(1).bytes)));
        Data middle = params.get(2);

        if (!code.isPresent()) {
            return Optional.empty();
        }

        if (!contentType.isPresent()) {
            return Optional.empty();
        }

        if (contentType.get() == ContentType.data) {
            return Optional.of(new Protocol(code.get(), middle));
        }

        return Optional.of(new Protocol(code.get(), new String(middle.bytes)));
    }

    public static Optional<Protocol> fromData(byte[] data) {
        return Protocol.fromData(Data.init(data));
    }

    public String asString() {
        return "Protocol "+ this.code.name() +"\nMessage: "+ new String(this.data.bytes) +"\n";
    }

    public Boolean isError() {
        if (this.code == ResponseCode.error) {
            return true;
        }

        if (this.code == ResponseCode.notFound) {
            return true;
        }

        if (this.code == ResponseCode.cutConnection) {
            return true;
        }

        if (this.code == ResponseCode.bytesError) {
            return true;
        }

        if (this.code == ResponseCode.refused) {
            return true;
        }

        if (this.code == ResponseCode.notAllowed) {
            return true;
        }

        return false;
    }
}