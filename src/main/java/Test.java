import java.io.File;
import java.util.ArrayList;

public class Test {
    public static void main (String[] args) {
        File testFile = new File("/home/credman0/Downloads/testcubes/innistrad($59.95).txt");
        TCGPlayerInteractor interactor = new TCGPlayerInteractor(testFile, "Innistrad");
        try {
            interactor.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        interactor.getScreenshot("test.png");
    }

    protected void testCubes () {
        File testdir = new File("/home/credman0/Downloads/testcubes");
        ArrayList<TCGPlayerInteractor> interactors = new ArrayList<>();
        for (File testCube:testdir.listFiles()) {
            TCGPlayerInteractor interactor = new TCGPlayerInteractor(testCube, "Innistrad");
            interactor.name = testCube.getName();
            interactors.add(interactor);
        }
        for (TCGPlayerInteractor interactor:interactors) {
            try {
                interactor.join();
                interactor.getScreenshot(interactor.name+".png");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
}
