public class BlahBlah extends DbnPlayer {
  public void execute() {
    while (true) {
      graphics.paper(graphics.getMouse(1));
      try {
        Thread.sleep(5);
      } catch (InterruptedException e) { }
    }
  }
}
