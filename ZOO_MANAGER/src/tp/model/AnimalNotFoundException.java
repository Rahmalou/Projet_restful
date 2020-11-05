package tp.model;

public class AnimalNotFoundException extends CenterException {
    private static final long serialVersionUID = -1254270872712247933L;

    public AnimalNotFoundException(String message) {
        super(message);
    }
}