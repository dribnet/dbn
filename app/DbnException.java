class DbnException extends Exception {
    int line = -1;

    public DbnException(String message) {
        super(message);
    }

    public DbnException(String message, int line) {
        super(message);
	this.line = line;
    }

    public DbnException(String message, DbnToken token) {
	super(message + ", token: " + token.toString());
    }
}

