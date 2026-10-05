
public class Main {

public static void main(String[] args) {
    Vector origin = new Vector(0, 0, 0);
    Surface surface = new Surface(origin, "@", 18);
    
    SmoothASCIIRotation.animate(surface);
}
    
}