package game;

import board.Board;
import players.AIPlayer;
import players.HumanPlayer;
import players.Player;

public class MegaTicTacToeMain {

	public void run() {
		Board b = new Board();
		Player p1 = new AIPlayer(5000);
		Player p2 = new AIPlayer(50000);
		
		while (true) {
			System.out.println(b.toString());
			if (b.isPlayer2Turn()) p2.run(b);
			else p1.run(b);
			if (b.hasWon()) {
				System.out.println("That was the winning move!");
				break;
			}
			if (b.isTied()) {
				System.out.println("Tied");
				break;
			}
		}
		System.out.println(b.toString());
//		MoveTreeNode start = new MoveTreeNode(0, 0, 0);
//		start.executeMove(b);
//		for (int i = 0; i < 80; ++i) start.simulate(b);
//		start.printTreeString(0);
	}
	
	public static void main(String[] args) {
		new MegaTicTacToeMain().run();
	}
}
