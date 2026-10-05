public class SmoothASCIIRotation {
    
    private static final char[] DEPTH_CHARS_CLASSIC = {'@', '#', '*', '+', '=', '-', ':', '.', ' '};
    // private static final char[] DEPTH_CHARS_DENSE = {'@', '&', '#', '*', '%', '+', '=', '-', ':', '.', ' '};
    
    // Pre-allocated screen buffer for better performance
    private static char[][] screenBuffer;
    private static double[][] depthBuffer;
    private static int screenWidth = 80;
    private static int screenHeight = 50;
    
    static {
        screenBuffer = new char[screenHeight][screenWidth];
        depthBuffer = new double[screenHeight][screenWidth];
    }
    
    public static void main(String[] args) {
        Vector origin = new Vector(0, 0, 0);
        Surface surface = new Surface(origin, "@", 18);
        
        System.out.println("Smooth ASCII 3D Rotation Demo:");
        System.out.println("1. Ultra-smooth classic rotation");
        System.out.println("2. Interpolated rotation");
        System.out.println("3. High-FPS wave rotation");
        System.out.println();
        
        animate(surface);
    }
    
    public static void animate(Surface surface) {
        System.out.println("Ultra-Smooth Rotation - Press Ctrl+C to stop");
        
        long lastTime = System.nanoTime();
        double time = 0;
        double targetFPS = 60.0;
        long frameTime = (long)(1_000_000_000.0 / targetFPS);
        
        System.out.print("\033[?25l");
        
        try {
            while (true) {
                long currentTime = System.nanoTime();
                double deltaTime = (currentTime - lastTime) / 1_000_000_000.0;
                
                double angleX = Math.sin(time * 0.8) * 0.6;
                double angleY = time * 1.2;
                double angleZ = Math.cos(time * 0.5) * 0.4;
                
                surface.setRotationAngles(angleX, angleY, angleZ);
                
                fastClearScreen();
                drawOptimizedASCII3D(surface, DEPTH_CHARS_CLASSIC);
                
                System.out.printf("\rFPS: %.1f | Time: %.1fs", 1.0/deltaTime, time);
                
                time += deltaTime;
                lastTime = currentTime;
                
                long sleepTime = frameTime - (System.nanoTime() - currentTime);
                if (sleepTime > 0) {
                    Thread.sleep(sleepTime / 1_000_000, (int)(sleepTime % 1_000_000));
                }
            }
        } catch (InterruptedException e) {
            System.out.print("\033[?25h");
        }
    }
    
    public static void interpolatedRotation(Surface surface) {
        System.out.println("Interpolated Rotation - Press Ctrl+C to stop");
        
        double currentAngleX = 0, currentAngleY = 0, currentAngleZ = 0;
        double targetAngleX = 0, targetAngleY = 0, targetAngleZ = 0;
        double time = 0;
        
        System.out.print("\033[?25l");
        
        try {
            while (true) {
                targetAngleX = Math.sin(time * 0.3) * 0.5;
                targetAngleY = time * 0.8;
                targetAngleZ = Math.cos(time * 0.2) * 0.3;
                
                double lerpFactor = 0.15;
                currentAngleX += (targetAngleX - currentAngleX) * lerpFactor;
                currentAngleY += (targetAngleY - currentAngleY) * lerpFactor;
                currentAngleZ += (targetAngleZ - currentAngleZ) * lerpFactor;
                
                surface.setRotationAngles(currentAngleX, currentAngleY, currentAngleZ);
                
                fastClearScreen();
                drawOptimizedASCII3D(surface, DEPTH_CHARS_CLASSIC);
                
                System.out.printf("\rSmooth interpolation | Time: %.1fs", time);
                
                time += 0.016; // ~60 FPS
                Thread.sleep(16);
            }
        } catch (InterruptedException e) {
            System.out.print("\033[?25h");
        }
    }
    
    public static void highFPSWaveRotation(Surface surface) {
        System.out.println("High-FPS Wave Rotation - Press Ctrl+C to stop");
        
        double time = 0;
        System.out.print("\033[?25l");
        
        try {
            while (true) {
                double waveX = Math.sin(time * 2.0) * 0.4;
                double waveY = time * 1.5;
                double waveZ = Math.sin(time * 1.8) * 0.3;
                
                surface.setRotationAngles(waveX, waveY, waveZ);
                
                fastClearScreen();
                drawOptimizedWaveASCII(surface, time);
                
                System.out.printf("\rHigh-FPS Wave | Time: %.2fs", time);
                
                time += 0.01; // 100 FPS target
                Thread.sleep(10);
            }
        } catch (InterruptedException e) {
            System.out.print("\033[?25h");
        }
    }
    
    public static void drawOptimizedASCII3D(Surface surface, char[] depthChars) {
        for (int i = 0; i < screenHeight; i++) {
            for (int j = 0; j < screenWidth; j++) {
                screenBuffer[i][j] = ' ';
                depthBuffer[i][j] = Double.MAX_VALUE;
            }
        }
        
        int surfaceSize = surface.getWidth();
        double scale = 2.0;
        
        for (int i = 0; i < surfaceSize; i++) {
            for (int j = 0; j < surfaceSize; j++) {
                for (int depth = -6; depth <= 6; depth++) {
                    Vector point = new Vector(
                        (i - surfaceSize/2.0) * scale,
                        (j - surfaceSize/2.0) * scale,
                        depth * 0.4
                    );
                    
                    Vector rotated = surface.rotateVector(point);
                    
                    int screenX = (int)(rotated.i + screenWidth/2);
                    int screenY = (int)(rotated.j + screenHeight/2);
                    
                    if (screenX >= 0 && screenX < screenWidth && 
                        screenY >= 0 && screenY < screenHeight) {
                        
                        if (rotated.k < depthBuffer[screenY][screenX]) {
                            depthBuffer[screenY][screenX] = rotated.k;
                            
                            int depthIndex = Math.min(Math.abs(depth), depthChars.length - 1);
                            screenBuffer[screenY][screenX] = depthChars[depthIndex];
                        }
                    }
                }
            }
        }
        
        StringBuilder output = new StringBuilder(screenWidth * screenHeight + screenHeight);
        for (int i = 0; i < screenHeight; i++) {
            for (int j = 0; j < screenWidth; j++) {
                output.append(screenBuffer[i][j]);
            }
            output.append('\n');
        }
        
        System.out.print(output.toString());
    }
    
    public static void drawOptimizedWaveASCII(Surface surface, double time) {
        // Clear buffers
        for (int i = 0; i < screenHeight; i++) {
            for (int j = 0; j < screenWidth; j++) {
                screenBuffer[i][j] = ' ';
                depthBuffer[i][j] = Double.MAX_VALUE;
            }
        }
        
        int surfaceSize = surface.getWidth();
        double scale = 1.8;
        
        for (int i = 0; i < surfaceSize; i++) {
            for (int j = 0; j < surfaceSize; j++) {
                double wave1 = Math.sin((i + j) * 0.4 + time * 4) * 1.5;
                double wave2 = Math.cos((i - j) * 0.3 + time * 2) * 0.8;
                double finalWave = wave1 + wave2;
                
                Vector point = new Vector(
                    (i - surfaceSize/2.0) * scale,
                    (j - surfaceSize/2.0) * scale,
                    finalWave
                );
                
                Vector rotated = surface.rotateVector(point);
                
                int screenX = (int)(rotated.i + screenWidth/2);
                int screenY = (int)(rotated.j + screenHeight/2);
                
                if (screenX >= 0 && screenX < screenWidth && 
                    screenY >= 0 && screenY < screenHeight) {
                    
                    if (rotated.k < depthBuffer[screenY][screenX]) {
                        depthBuffer[screenY][screenX] = rotated.k;
                        
                        char waveChar;
                        if (finalWave > 2.0) waveChar = '@';
                        else if (finalWave > 1.0) waveChar = '#';
                        else if (finalWave > 0.0) waveChar = '*';
                        else if (finalWave > -1.0) waveChar = '+';
                        else if (finalWave > -2.0) waveChar = '-';
                        else waveChar = '.';
                        
                        screenBuffer[screenY][screenX] = waveChar;
                    }
                }
            }
        }
        
        StringBuilder output = new StringBuilder(screenWidth * screenHeight + screenHeight);
        for (int i = 0; i < screenHeight; i++) {
            for (int j = 0; j < screenWidth; j++) {
                output.append(screenBuffer[i][j]);
            }
            output.append('\n');
        }
        
        System.out.print(output.toString());
    }
    
    public static void fastClearScreen() {
        System.out.print("\033[H"); 
        System.out.flush();
    }
    
    public static double easeInOutSine(double t) {
        return -(Math.cos(Math.PI * t) - 1) / 2;
    }
    
    public static double smoothStep(double edge0, double edge1, double x) {
        double t = Math.max(0, Math.min(1, (x - edge0) / (edge1 - edge0)));
        return t * t * (3 - 2 * t);
    }
}