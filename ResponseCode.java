import java.util.Optional;

public enum ResponseCode {
    error(0),
    notFound(1),
    cutConnection(2),
    alive(3),

    data(10),
    listFiles(11),
    buffer(100),

    // actions
    send(101),
    receive(102),
    fragment(103),
    checkSum(104),
    fileSizeExceeded(105),
    bytesError(106),

    auth(110),
    logout(111),

    // success
    accepted(200),
    refused(400),
    notAllowed(401);

    final Integer rawValue;

    ResponseCode(Integer rawValue) {
       this.rawValue = rawValue; 
    }

    public static Optional<ResponseCode> init(Integer rawValue) {
        for(ResponseCode code : ResponseCode.values()) {
            if (code.rawValue == rawValue) {
                return Optional.of(code);
            }
        }

        return Optional.empty();
    }

    public static Optional<ResponseCode> init(String rawValue) {
        for(ResponseCode code : ResponseCode.values()) {
            if (code.name().equals(rawValue)) {
                return Optional.of(code);
            }
        }

        return Optional.empty();
    }
}